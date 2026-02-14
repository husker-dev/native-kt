package com.huskerdev.nativekt.jvmci.conventions;

import com.huskerdev.nativekt.jvmci.Buffer;
import com.huskerdev.nativekt.jvmci.CallingConvention;
import jdk.vm.ci.code.RegisterValue;
import jdk.vm.ci.code.StackSlot;
import jdk.vm.ci.hotspot.HotSpotResolvedJavaMethod;
import jdk.vm.ci.meta.AllocatableValue;
import jdk.vm.ci.meta.Value;

import java.lang.reflect.Method;

public class ARM64CallingConvention extends CallingConvention {

    private int getAlignedStack(HotSpotResolvedJavaMethod resolvedMethod) {
        return align16(getNativeCC(resolvedMethod).getStackSize());
    }

    @Override
    protected void emitEpilogue(Buffer buf) {
        // nmethod entry barrier simulation:
        // ldr w0, [pc, #0]
        buf.emitInt(0x18000000);
    }

    @Override
    protected void emitConversion(Buffer buf, Method method) {
        if (method.getParameterCount() == 0)
            return;

        HotSpotResolvedJavaMethod resolvedMethod = resolveJavaMethod(method);
        jdk.vm.ci.code.CallingConvention javaCC = getJavaCC(resolvedMethod);
        jdk.vm.ci.code.CallingConvention nativeCC = getNativeCC(resolvedMethod);

        int alignedStack = getAlignedStack(resolvedMethod);

        // Пролог: сохраняем frame pointer и link register, выделяем стек
        if (alignedStack > 0) {
            // stp x29, x30, [sp, #-stackSize]!
            emitStpPreIndex(buf, 29, 30, alignedStack);
        } else {
            // Если нет стека, всё равно сохраняем fp и lr
            emitStpPreIndex(buf, 29, 30, 16);
        }

        for (int i = 1; i < javaCC.getArgumentCount(); i++) {
            emitMove(buf,
                    javaCC.getArgument(i),
                    nativeCC.getArgument(i - 1),
                    method.getParameterTypes()[i - 1]
            );
        }
    }

    @Override
    protected void emitCall(Buffer buf, Method method, long address) {
        // Загрузка адреса в x9 (caller-saved register)
        emitMovImm64(buf, 9, address);

        // blr x9 (branch with link to register)
        emitBlr(buf, 9);
    }

    @Override
    protected void emitPrologue(Buffer buf, Method method) {
        HotSpotResolvedJavaMethod resolvedMethod = resolveJavaMethod(method);

        int alignedStack = getAlignedStack(resolvedMethod);

        // Восстановление frame pointer и link register
        if (alignedStack > 0) {
            // ldp x29, x30, [sp], #stackSize
            emitLdpPostIndex(buf, 29, 30, alignedStack);
        } else {
            emitLdpPostIndex(buf, 29, 30, 16);
        }

        // ret (return using x30)
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
        if (from instanceof RegisterValue && to instanceof RegisterValue)
            emitRegToReg(buf, (RegisterValue) from, (RegisterValue) to, type);
        else if (from instanceof RegisterValue && to instanceof StackSlot)
            emitRegToStack(buf, (RegisterValue) from, (StackSlot) to, type);
        else if (from instanceof StackSlot && to instanceof RegisterValue)
            emitStackToReg(buf, (StackSlot) from, (RegisterValue) to, type);
        else if (from instanceof StackSlot && to instanceof StackSlot) {
            // stack → x9/v31 → stack (используем временный регистр)
            if (isFloat(type)) {
                emitStackToVec(buf, (StackSlot) from, 31, isDouble(type));
                emitVecToStack(buf, 31, (StackSlot) to, isDouble(type));
            } else {
                emitStackToReg(buf, (StackSlot) from, 9);
                emitRegToStack(buf, 9, (StackSlot) to);
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

        if (isFloat(type)) {
            emitVecToVec(buf, srcReg, dstReg, isDouble(type));
            return;
        }

        if (type.isArray()) {
            // add dst, src, #offset
            int offset = getArrayOffset(type);
            emitAddImm(buf, dstReg, srcReg, offset);
        } else {
            // mov dst, src
            emitMovReg(buf, dstReg, srcReg);
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

        if (isFloat(type)) {
            emitVecToStack(buf, srcReg, to, isDouble(type));
            return;
        }

        if (type.isArray()) {
            // add x9, src, #offset
            int offset = getArrayOffset(type);
            emitAddImm(buf, 9, srcReg, offset);
            // str x9, [sp, #slot_offset]
            emitRegToStack(buf, 9, to);
        } else {
            // str src, [sp, #slot_offset]
            emitRegToStack(buf, srcReg, to);
        }
    }

    private void emitRegToStack(Buffer buf, int reg, StackSlot slot) {
        // str x{reg}, [sp, #offset]
        int offset = slot.getRawOffset();
        emitStrSpOffset(buf, reg, offset);
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

        if (isFloat(type)) {
            emitStackToVec(buf, from, dstReg, isDouble(type));
            return;
        }

        // ldr dst, [sp, #offset]
        emitStackToReg(buf, from, dstReg);

        if (type.isArray()) {
            // add dst, dst, #offset
            int offset = getArrayOffset(type);
            emitAddImm(buf, dstReg, dstReg, offset);
        }
    }

    private void emitStackToReg(Buffer buf, StackSlot slot, int reg) {
        // ldr x{reg}, [sp, #offset]
        int offset = slot.getRawOffset();
        emitLdrSpOffset(buf, reg, offset);
    }

    // ==========================================================
    // RAW ARM64 INSTRUCTION ENCODERS
    // ==========================================================

    private void emitMovReg(Buffer buf, int dst, int src) {
        // mov x{dst}, x{src} = orr x{dst}, xzr, x{src}
        // Format: 0xAA0003E0 | (src << 16) | dst
        int insn = 0xAA0003E0 | (src << 16) | dst;
        buf.emitInt(insn);
    }

    private void emitAddImm(Buffer buf, int dst, int src, int imm) {
        // add x{dst}, x{src}, #imm
        // Format: 0x91000000 | (imm << 10) | (src << 5) | dst
        // imm должен быть 12-битным
        if (imm < 0 || imm > 4095) {
            throw new IllegalArgumentException("Immediate value out of range: " + imm);
        }
        int insn = 0x91000000 | (imm << 10) | (src << 5) | dst;
        buf.emitInt(insn);
    }

    private void emitMovImm64(Buffer buf, int reg, long value) {
        // Загрузка 64-битного значения через серию MOVZ/MOVK
        // movz x{reg}, #(value & 0xFFFF), lsl #0
        int insn1 = 0xD2800000 | ((int)(value & 0xFFFF) << 5) | reg;
        buf.emitInt(insn1);

        // movk x{reg}, #((value >> 16) & 0xFFFF), lsl #16
        if ((value >> 16) != 0) {
            int insn2 = 0xF2A00000 | ((int)((value >> 16) & 0xFFFF) << 5) | reg;
            buf.emitInt(insn2);
        }

        // movk x{reg}, #((value >> 32) & 0xFFFF), lsl #32
        if ((value >> 32) != 0) {
            int insn3 = 0xF2C00000 | ((int)((value >> 32) & 0xFFFF) << 5) | reg;
            buf.emitInt(insn3);
        }

        // movk x{reg}, #((value >> 48) & 0xFFFF), lsl #48
        if ((value >> 48) != 0) {
            int insn4 = 0xF2E00000 | ((int)((value >> 48) & 0xFFFF) << 5) | reg;
            buf.emitInt(insn4);
        }
    }

    private void emitBlr(Buffer buf, int reg) {
        // blr x{reg}
        // Format: 0xD63F0000 | (reg << 5)
        int insn = 0xD63F0000 | (reg << 5);
        buf.emitInt(insn);
    }

    private void emitRet(Buffer buf) {
        // ret (returns using x30)
        // Format: 0xD65F03C0
        buf.emitInt(0xD65F03C0);
    }

    private void emitStpPreIndex(Buffer buf, int rt1, int rt2, int offset) {
        // stp x{rt1}, x{rt2}, [sp, #-offset]!
        // Format: 0xA9800000 | (offset/8 << 15) | (rt2 << 10) | (31 << 5) | rt1
        // offset должен быть кратен 8 и в диапазоне -512..504
        int offsetEncoded = (-offset / 8) & 0x7F;
        int insn = 0xA9800000 | (offsetEncoded << 15) | (rt2 << 10) | (31 << 5) | rt1;
        buf.emitInt(insn);
    }

    private void emitLdpPostIndex(Buffer buf, int rt1, int rt2, int offset) {
        // ldp x{rt1}, x{rt2}, [sp], #offset
        // Format: 0xA8C00000 | (offset/8 << 15) | (rt2 << 10) | (31 << 5) | rt1
        int offsetEncoded = (offset / 8) & 0x7F;
        int insn = 0xA8C00000 | (offsetEncoded << 15) | (rt2 << 10) | (31 << 5) | rt1;
        buf.emitInt(insn);
    }

    private void emitStrSpOffset(Buffer buf, int reg, int offset) {
        // str x{reg}, [sp, #offset]
        // Format: 0xF90003E0 | (offset/8 << 10) | reg
        // offset должен быть кратен 8
        int offsetEncoded = (offset / 8) & 0xFFF;
        int insn = 0xF90003E0 | (offsetEncoded << 10) | reg;
        buf.emitInt(insn);
    }

    private void emitLdrSpOffset(Buffer buf, int reg, int offset) {
        // ldr x{reg}, [sp, #offset]
        // Format: 0xF94003E0 | (offset/8 << 10) | reg
        int offsetEncoded = (offset / 8) & 0xFFF;
        int insn = 0xF94003E0 | (offsetEncoded << 10) | reg;
        buf.emitInt(insn);
    }

    // ==========================================================
    // VECTOR (SIMD) INSTRUCTIONS
    // ==========================================================

    private void emitVecToVec(Buffer buf, int src, int dst, boolean isDouble) {
        // fmov d{dst}, d{src} или fmov s{dst}, s{src}
        // Format для double: 0x1E604000 | (src << 5) | dst
        // Format для float: 0x1E204000 | (src << 5) | dst
        int insn = (isDouble ? 0x1E604000 : 0x1E204000) | (src << 5) | dst;
        buf.emitInt(insn);
    }

    private void emitStackToVec(Buffer buf, StackSlot slot, int reg, boolean isDouble) {
        // ldr d{reg}, [sp, #offset] или ldr s{reg}, [sp, #offset]
        int offset = slot.getRawOffset();

        if (isDouble) {
            // ldr d{reg}, [sp, #offset]
            // Format: 0xFD4003E0 | (offset/8 << 10) | reg
            int offsetEncoded = (offset / 8) & 0xFFF;
            int insn = 0xFD4003E0 | (offsetEncoded << 10) | reg;
            buf.emitInt(insn);
        } else {
            // ldr s{reg}, [sp, #offset]
            // Format: 0xBD4003E0 | (offset/4 << 10) | reg
            int offsetEncoded = (offset / 4) & 0xFFF;
            int insn = 0xBD4003E0 | (offsetEncoded << 10) | reg;
            buf.emitInt(insn);
        }
    }

    private void emitVecToStack(Buffer buf, int reg, StackSlot slot, boolean isDouble) {
        // str d{reg}, [sp, #offset] или str s{reg}, [sp, #offset]
        int offset = slot.getRawOffset();

        if (isDouble) {
            // str d{reg}, [sp, #offset]
            // Format: 0xFD0003E0 | (offset/8 << 10) | reg
            int offsetEncoded = (offset / 8) & 0xFFF;
            int insn = 0xFD0003E0 | (offsetEncoded << 10) | reg;
            buf.emitInt(insn);
        } else {
            // str s{reg}, [sp, #offset]
            // Format: 0xBD0003E0 | (offset/4 << 10) | reg
            int offsetEncoded = (offset / 4) & 0xFFF;
            int insn = 0xBD0003E0 | (offsetEncoded << 10) | reg;
            buf.emitInt(insn);
        }
    }
}
