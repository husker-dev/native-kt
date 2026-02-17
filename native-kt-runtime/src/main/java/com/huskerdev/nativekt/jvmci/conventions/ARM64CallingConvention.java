package com.huskerdev.nativekt.jvmci.conventions;

import com.huskerdev.nativekt.jvmci.Buffer;
import com.huskerdev.nativekt.jvmci.CallingConvention;
import jdk.vm.ci.code.site.DataPatch;
import jdk.vm.ci.code.site.DataSectionReference;
import jdk.vm.ci.code.site.Mark;
import jdk.vm.ci.code.site.Site;
import jdk.vm.ci.hotspot.HotSpotCompiledCode;
import jdk.vm.ci.hotspot.HotSpotCompiledNmethod;
import jdk.vm.ci.hotspot.HotSpotResolvedJavaMethod;
import jdk.vm.ci.meta.Assumptions;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.runtime.JVMCICompiler;

import java.lang.reflect.Method;


public class ARM64CallingConvention extends CallingConvention {

    @Override
    public HotSpotCompiledNmethod createNMethod(String name, byte[] code, HotSpotResolvedJavaMethod resolvedMethod) {
        DataSectionReference a = new DataSectionReference();
        a.setOffset(0);

        return new HotSpotCompiledNmethod(
                name,
                code,
                code.length,
                new Site[] { new Mark(code.length - 4, ENTRY_BARRIER_PATCH), new DataPatch(code.length - 4, a) },
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
    protected void emitPrologue(Buffer buf, Method method) { }

    @Override
    protected void emitConversion(Buffer buf, Method method) {
        // 8th integer Java argument clashes with the 1st native arg
        for(int i = 0, ints = 0; i < method.getParameterCount(); i++) {
            Class<?> type = method.getParameterTypes()[i];
            if(isIntegerType(type))
                ints++;

            if(ints == 8) {
                if (type.isArray())
                    emitAddImm(buf, 8, 0, getArrayOffset(type));
                else
                    emitMovReg(buf, 8, 0, type);
                break;
            }
        }

        for(int i = 0, ints = 0; i < method.getParameterCount(); i++) {
            Class<?> type = method.getParameterTypes()[i];

            if(type.isArray()) {
                int offset = getArrayOffset(type);

                if(ints < 9) {
                    // in registers
                    int dst = ints;
                    int src = ++ints;
                    emitAddImm(buf, dst, src, offset);
                } else {
                    // in stack (stack -> reg -> stack)
                    int stackOffset = (ints - 9) * 8;
                    emitLdrSpOffset(buf, 15, stackOffset);
                    emitAddImm(buf, 15, 15, offset);
                    emitStrSpOffset(buf, 15, stackOffset);
                }
            } else if(!isFloatingPointType(type)) {
                // mov x x+1
                int dst = ints;
                int src = ++ints;
                emitMovReg(buf, dst, src, type);
            }
        }
    }

    @Override
    protected void emitCall(Buffer buf, Method method, long address) {
        // write 64-bit address to x9
        emitMovImm64(buf, 9, address);

        // bl x9
        buf.emitInt(0xD61F0000 | (9 << 5));
    }

    @Override
    protected void emitEpilogue(Buffer buf, Method method) {
        // nmethod entry barrier simulation:
        // LDR W8, [PC, #0]
        buf.emitInt(0x18000008);
    }

    // ==========================================================
    // INSTRUCTION
    // ==========================================================

    private void emitMovReg(Buffer buf, int dst, int src, Class<?> type) {
        // mov x{dst}, x{src} = orr x{dst}, xzr, x{src}
        int insn = (type == long.class ? 0xAA0003E0 : 0x2a0003e0) | (src << 16) | dst;
        buf.emitInt(insn);
    }

    private void emitAddImm(Buffer buf, int dst, int src, int imm) {
        // add x{dst}, x{src}, #imm
        int insn = 0x91000000 | (imm << 10) | (src << 5) | dst;
        buf.emitInt(insn);
    }

    private void emitMovImm64(Buffer buf, int reg, long value) {
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

    private void emitStrSpOffset(Buffer buf, int reg, int offset) {
        // str x{reg}, [sp, #offset]
        // Format: 0xF90003E0 | (offset/8 << 10) | reg
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
}
