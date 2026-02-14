package com.huskerdev.nativekt.jvmci.conventions;

import com.huskerdev.nativekt.jvmci.Buffer;
import com.huskerdev.nativekt.jvmci.CallingConvention;
import jdk.vm.ci.code.RegisterValue;
import jdk.vm.ci.code.StackSlot;
import jdk.vm.ci.code.site.DataPatch;
import jdk.vm.ci.code.site.DataSectionReference;
import jdk.vm.ci.code.site.Mark;
import jdk.vm.ci.code.site.Site;
import jdk.vm.ci.hotspot.HotSpotCompiledCode;
import jdk.vm.ci.hotspot.HotSpotCompiledNmethod;
import jdk.vm.ci.hotspot.HotSpotResolvedJavaMethod;
import jdk.vm.ci.meta.AllocatableValue;
import jdk.vm.ci.meta.Assumptions;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.meta.Value;
import jdk.vm.ci.runtime.JVMCICompiler;

import java.lang.reflect.Method;

public class RISCV64CallingConvention extends CallingConvention {

    private int getAlignedStack(HotSpotResolvedJavaMethod resolvedMethod) {
        return align16(getNativeCC(resolvedMethod).getStackSize());
    }

    @Override
    public HotSpotCompiledNmethod createNMethod(String name, byte[] code, HotSpotResolvedJavaMethod resolvedMethod) {
        DataSectionReference a = new DataSectionReference();
        a.setOffset(0);

        return new HotSpotCompiledNmethod(
                name,
                code,
                code.length,
                new Site[] { new Mark(4, ENTRY_BARRIER_PATCH), new DataPatch(4, a) },
                new Assumptions.Assumption[0],
                new ResolvedJavaMethod[0],
                new HotSpotCompiledCode.Comment[0],
                new byte[] { 0, 0, 0, 0 },
                1,
                new DataPatch[0],
                true,
                0,
                null,
                resolvedMethod,
                JVMCICompiler.INVOCATION_ENTRY_BCI,
                1,
                0,
                false
        );
    }

    @Override
    protected void emitPrologue(Buffer buf, Method method) {
        // nmethod entry barrier simulation:

        // auipc t0, 0
        buf.emitInt(0x00000297);

        // lwu t0, 8(t0)
        emitLwu(buf, 5, 5, 8);

        // Guard value (32-bit)
        buf.emitInt(0x00000000);
    }

    @Override
    protected void emitConversion(Buffer buf, Method method) {
        if (method.getParameterCount() == 0)
            return;

        HotSpotResolvedJavaMethod resolvedMethod = resolveJavaMethod(method);
        jdk.vm.ci.code.CallingConvention javaCC = getJavaCC(resolvedMethod);
        jdk.vm.ci.code.CallingConvention nativeCC = getNativeCC(resolvedMethod);

        int alignedStack = getAlignedStack(resolvedMethod);

        // Пролог: выделяем стек и сохраняем ra (x1) и s0/fp (x8)
        if (alignedStack > 0) {
            // addi sp, sp, -alignedStack
            emitAddi(buf, 2, 2, -alignedStack);
            // sd ra, (alignedStack-8)(sp)
            emitSd(buf, 1, 2, alignedStack - 8);
            // sd s0, (alignedStack-16)(sp)
            emitSd(buf, 8, 2, alignedStack - 16);
        } else {
            // Минимальный стек 16 байт для ra и s0
            emitAddi(buf, 2, 2, -16);
            emitSd(buf, 1, 2, 8);
            emitSd(buf, 8, 2, 0);
        }

        for (int i = 1; i < javaCC.getArgumentCount(); i++) {
            emitMove(
                    buf,
                    javaCC.getArgument(i),
                    nativeCC.getArgument(i - 1),
                    method.getParameterTypes()[i - 1]
            );
        }
    }

    @Override
    protected void emitCall(Buffer buf, Method method, long address) {
        // Загрузка 64-битного адреса в t0 (x5)
        emitLoadImm64(buf, 5, address);

        // jalr ra, 0(t0)
        emitJalr(buf, 1, 5, 0);
    }

    @Override
    protected void emitEpilogue(Buffer buf, Method method) {
        HotSpotResolvedJavaMethod resolvedMethod = resolveJavaMethod(method);

        int alignedStack = getAlignedStack(resolvedMethod);

        // Эпилог: восстанавливаем ra и s0, освобождаем стек
        if (alignedStack > 0) {
            // ld ra, (alignedStack-8)(sp)
            emitLd(buf, 1, 2, alignedStack - 8);
            // ld s0, (alignedStack-16)(sp)
            emitLd(buf, 8, 2, alignedStack - 16);
            // addi sp, sp, alignedStack
            emitAddi(buf, 2, 2, alignedStack);
        } else {
            emitLd(buf, 1, 2, 8);
            emitLd(buf, 8, 2, 0);
            emitAddi(buf, 2, 2, 16);
        }

        // ret (jalr x0, 0(ra))
        emitRet(buf);
    }

    // ==========================================================
    // MOVE LOGIC
    // ==========================================================

    private void emitMove(
            Buffer buf,
            AllocatableValue from,
            Value to,
            Class<?> type
    ) {
        if (from instanceof RegisterValue && to instanceof RegisterValue) {
            emitRegToReg(buf, (RegisterValue) from, (RegisterValue) to, type);
        } else if (from instanceof RegisterValue && to instanceof StackSlot) {
            emitRegToStack(buf, (RegisterValue) from, (StackSlot) to, type);
        } else if (from instanceof StackSlot && to instanceof RegisterValue) {
            emitStackToReg(buf, (StackSlot) from, (RegisterValue) to, type);
        } else if (from instanceof StackSlot && to instanceof StackSlot) {
            // stack → temporary register → stack
            if (isFloatingPointType(type)) {
                // Используем ft11 (f31) как временный регистр
                emitStackToFloat(buf, (StackSlot) from, 31, isDouble(type));
                emitFloatToStack(buf, 31, (StackSlot) to, isDouble(type));
            } else {
                // Используем t6 (x31) как временный регистр
                emitStackToRegRaw(buf, (StackSlot) from, 31);
                emitRegToStackRaw(buf, 31, (StackSlot) to);
            }
        } else {
            throw new UnsupportedOperationException("Unsupported move: " + from + " -> " + to);
        }
    }

    // ==========================================================
    // REGISTER → REGISTER
    // ==========================================================

    private void emitRegToReg(
            Buffer buf,
            RegisterValue from,
            RegisterValue to,
            Class<?> type
    ) {
        int srcReg = from.getRegister().encoding;
        int dstReg = to.getRegister().encoding;

        if (isFloatingPointType(type)) {
            emitFloatToFloat(buf, srcReg, dstReg, isDouble(type));
            return;
        }

        if (type.isArray()) {
            int offset = getArrayOffset(type);
            emitAddi(buf, dstReg, srcReg, offset);
        } else {
            emitMv(buf, dstReg, srcReg);
        }
    }

    // ==========================================================
    // REGISTER → STACK
    // ==========================================================

    private void emitRegToStack(
            Buffer buf,
            RegisterValue from,
            StackSlot to,
            Class<?> type
    ) {
        int srcReg = from.getRegister().encoding;

        if (isFloatingPointType(type)) {
            emitFloatToStack(buf, srcReg, to, isDouble(type));
            return;
        }

        if (type.isArray()) {
            int offset = getArrayOffset(type);
            // addi t6, srcReg, offset
            emitAddi(buf, 31, srcReg, offset);
            // sd t6, slot_offset(sp)
            emitRegToStackRaw(buf, 31, to);
        } else {
            emitRegToStackRaw(buf, srcReg, to);
        }
    }

    private void emitRegToStackRaw(Buffer buf, int reg, StackSlot slot) {
        int offset = slot.getRawOffset();
        emitSd(buf, reg, 2, offset);
    }

    // ==========================================================
    // STACK → REGISTER
    // ==========================================================

    private void emitStackToReg(
            Buffer buf,
            StackSlot from,
            RegisterValue to,
            Class<?> type
    ) {
        int dstReg = to.getRegister().encoding;

        if (isFloatingPointType(type)) {
            emitStackToFloat(buf, from, dstReg, isDouble(type));
            return;
        }

        emitStackToRegRaw(buf, from, dstReg);

        if (type.isArray()) {
            int offset = getArrayOffset(type);
            // addi dstReg, dstReg, offset
            emitAddi(buf, dstReg, dstReg, offset);
        }
    }

    private void emitStackToRegRaw(Buffer buf, StackSlot slot, int reg) {
        int offset = slot.getRawOffset();
        emitLd(buf, reg, 2, offset);
    }

    // ==========================================================
    // RAW RISC-V INSTRUCTION ENCODERS
    // ==========================================================

    private void emitAddi(Buffer buf, int rd, int rs1, int imm) {
        // addi rd, rs1, imm
        // Format: imm[11:0] | rs1 | 000 | rd | 0010011
        int imm12 = imm & 0xFFF;
        int insn = (imm12 << 20) | ((rs1 & 0x1F) << 15) | (0x0 << 12) | ((rd & 0x1F) << 7) | 0x13;
        buf.emitInt(insn);
    }

    private void emitMv(Buffer buf, int rd, int rs1) {
        // mv rd, rs1 (pseudo-instruction: addi rd, rs1, 0)
        emitAddi(buf, rd, rs1, 0);
    }

    private void emitLd(Buffer buf, int rd, int rs1, int offset) {
        // ld rd, offset(rs1)
        // Format: offset[11:0] | rs1 | 011 | rd | 0000011
        int imm12 = offset & 0xFFF;
        int insn = (imm12 << 20) | ((rs1 & 0x1F) << 15) | (0x3 << 12) | ((rd & 0x1F) << 7) | 0x03;
        buf.emitInt(insn);
    }

    private void emitSd(Buffer buf, int rs2, int rs1, int offset) {
        // sd rs2, offset(rs1)
        // Format: offset[11:5] | rs2 | rs1 | 011 | offset[4:0] | 0100011
        int imm11_5 = (offset >> 5) & 0x7F;
        int imm4_0 = offset & 0x1F;
        int insn = (imm11_5 << 25) | ((rs2 & 0x1F) << 20) | ((rs1 & 0x1F) << 15) | (0x3 << 12) | (imm4_0 << 7) | 0x23;
        buf.emitInt(insn);
    }

    private void emitLoadImm64(Buffer buf, int rd, long value) {
        // Загрузка 64-битного значения через lui, addi и сдвиги

        long imm = value >> 17;
        long upper = imm;
        long lower = imm;
        lower = (lower << 52) >> 52; // Знаковое расширение 12 бит
        upper -= lower;

        int a0 = (int) upper;
        int a1 = (int) lower;
        int a2 = (int) ((value >> 6) & 0x7FF);
        int a3 = (int) (value & 0x3F);

        // lui rd, a0
        emitLui(buf, rd, a0);

        // addi rd, rd, a1
        if (a1 != 0) emitAddi(buf, rd, rd, a1);

        // slli rd, rd, 11
        emitSlli(buf, rd, rd, 11);

        // addi rd, rd, a2
        if (a2 != 0) emitAddi(buf, rd, rd, a2);

        // slli rd, rd, 6
        emitSlli(buf, rd, rd, 6);

        // addi rd, rd, a3
        if (a3 != 0) emitAddi(buf, rd, rd, a3);
    }

    private void emitLui(Buffer buf, int rd, int imm) {
        // lui rd, imm
        // Format: imm[31:12] | rd | 0110111
        int imm20 = imm & 0xFFFFF;
        int insn = (imm20 << 12) | ((rd & 0x1F) << 7) | 0x37;
        buf.emitInt(insn);
    }

    private void emitSlli(Buffer buf, int rd, int rs1, int shamt) {
        // slli rd, rs1, shamt
        // Format: 0000000 | shamt | rs1 | 001 | rd | 0010011
        int insn = ((shamt & 0x3F) << 20) | ((rs1 & 0x1F) << 15) | (0x1 << 12) | ((rd & 0x1F) << 7) | 0x13;
        buf.emitInt(insn);
    }

    private void emitJalr(Buffer buf, int rd, int rs1, int offset) {
        // jalr rd, offset(rs1)
        // Format: offset[11:0] | rs1 | 000 | rd | 1100111
        int imm12 = offset & 0xFFF;
        int insn = (imm12 << 20) | ((rs1 & 0x1F) << 15) | ((rd & 0x1F) << 7) | 0x67;
        buf.emitInt(insn);
    }

    private void emitRet(Buffer buf) {
        // ret (pseudo-instruction: jalr x0, 0(ra))
        emitJalr(buf, 0, 1, 0);
    }

    private void emitLwu(Buffer buf, int rd, int rs1, int offset) {
        // lwu rd, offset(rs1)
        // Format: offset[11:0] | rs1 | 110 | rd | 0000011
        int imm12 = offset & 0xFFF;
        int insn = (imm12 << 20) | ((rs1 & 0x1F) << 15) | (0x6 << 12) | ((rd & 0x1F) << 7) | 0x03;
        buf.emitInt(insn);
    }

    // ==========================================================
    // FLOATING-POINT INSTRUCTIONS
    // ==========================================================

    private void emitFloatToFloat(Buffer buf, int frs1, int frd, boolean isDouble) {
        if (isDouble) {
            // fmv.d frd, frs1 (реализуется как fsgnj.d frd, frs1, frs1)
            // Format: 0010001 | frs1 | frs1 | 000 | frd | 1010011
            int insn = (0x11 << 25) | ((frs1 & 0x1F) << 20) | ((frs1 & 0x1F) << 15) | ((frd & 0x1F) << 7) | 0x53;
            buf.emitInt(insn);
        } else {
            // fmv.s frd, frs1 (реализуется как fsgnj.s frd, frs1, frs1)
            // Format: 0010000 | frs1 | frs1 | 000 | frd | 1010011
            int insn = (0x10 << 25) | ((frs1 & 0x1F) << 20) | ((frs1 & 0x1F) << 15) | ((frd & 0x1F) << 7) | 0x53;
            buf.emitInt(insn);
        }
    }

    private void emitStackToFloat(Buffer buf, StackSlot slot, int frd, boolean isDouble) {
        int offset = slot.getRawOffset();
        int imm12 = offset & 0xFFF;

        if (isDouble) {
            // fld frd, offset(sp)
            // Format: offset[11:0] | rs1 | 011 | frd | 0000111
            int insn = (imm12 << 20) | ((2 & 0x1F) << 15) | (0x3 << 12) | ((frd & 0x1F) << 7) | 0x07;
            buf.emitInt(insn);
        } else {
            // flw frd, offset(sp)
            // Format: offset[11:0] | rs1 | 010 | frd | 0000111
            int insn = (imm12 << 20) | ((2 & 0x1F) << 15) | (0x2 << 12) | ((frd & 0x1F) << 7) | 0x07;
            buf.emitInt(insn);
        }
    }

    private void emitFloatToStack(Buffer buf, int frs2, StackSlot slot, boolean isDouble) {
        int offset = slot.getRawOffset();
        int imm11_5 = (offset >> 5) & 0x7F;
        int imm4_0 = offset & 0x1F;

        if (isDouble) {
            // fsd frs2, offset(sp)
            // Format: offset[11:5] | frs2 | rs1 | 011 | offset[4:0] | 0100111
            int insn = (imm11_5 << 25) | ((frs2 & 0x1F) << 20) | ((2 & 0x1F) << 15) | (0x3 << 12) | (imm4_0 << 7) | 0x27;
            buf.emitInt(insn);
        } else {
            // fsw frs2, offset(sp)
            // Format: offset[11:5] | frs2 | rs1 | 010 | offset[4:0] | 0100111
            int insn = (imm11_5 << 25) | ((frs2 & 0x1F) << 20) | ((2 & 0x1F) << 15) | (0x2 << 12) | (imm4_0 << 7) | 0x27;
            buf.emitInt(insn);
        }
    }
}