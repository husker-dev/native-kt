package com.huskerdev.nativekt.jvmci.conventions;

import com.huskerdev.nativekt.NativeKtUtils;
import com.huskerdev.nativekt.jvmci.Buffer;
import com.huskerdev.nativekt.jvmci.CallingConvention;
import jdk.vm.ci.code.RegisterValue;
import jdk.vm.ci.code.StackSlot;
import jdk.vm.ci.code.site.DataPatch;
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

public class AMD64CallingConvention extends CallingConvention {

    private static final int WINDOWS_SHADOW_SPACE = 32;

    private int getAlignedStack(HotSpotResolvedJavaMethod resolvedMethod){
        int stackSize = getNativeCC(resolvedMethod).getStackSize() +
                (NativeKtUtils.Os.current() == NativeKtUtils.Os.WINDOWS ? WINDOWS_SHADOW_SPACE : 0);

        return align16(stackSize) +
                (NativeKtUtils.Os.current() == NativeKtUtils.Os.WINDOWS ? 8 : 0);
    }

    @Override
    public HotSpotCompiledNmethod createNMethod(String name, byte[] code, HotSpotResolvedJavaMethod resolvedMethod) {
        return new HotSpotCompiledNmethod(
                name,
                code,
                code.length,
                new Site[] { new Mark(0, ENTRY_BARRIER_PATCH) },
                new Assumptions.Assumption[0],
                new ResolvedJavaMethod[0],
                new HotSpotCompiledCode.Comment[0],
                new byte[0],
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
    protected void emitEpilogue(Buffer buf, Method method) {
        // nmethod entry barrier simulation:
        // cmp dword ptr 0, 0x00000000
        buf.emitByte(0x41);
        buf.emitByte(0x81);
        buf.emitByte(0x7f);
        buf.emitByte(0);
        buf.emitInt(0);
    }

    @Override
    protected void emitConversion(Buffer buf, Method method) {
        if(method.getParameterCount() == 0)
            return;

        HotSpotResolvedJavaMethod resolvedMethod = resolveJavaMethod(method);
        jdk.vm.ci.code.CallingConvention javaCC = getJavaCC(resolvedMethod);
        jdk.vm.ci.code.CallingConvention nativeCC = getNativeCC(resolvedMethod);

        if(method.getParameterCount() > 4)
            emitSubRsp(buf, getAlignedStack(resolvedMethod));

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
        // mov rax, target
        buf.emitByte(0x48);
        buf.emitByte(0xB8);
        buf.emitLong(address);

        if(method.getParameterCount() > 4) {
            // call rax
            buf.emitByte(0xFF);
            buf.emitByte(0xD0);
        } else {
            // jmp rax
            buf.emitByte(0xFF);
            buf.emitByte(0xE0);
        }
    }

    @Override
    protected void emitPrologue(Buffer buf, Method method) {
        if(method.getParameterCount() > 4) {
            emitAddRsp(buf, getAlignedStack(resolveJavaMethod(method)));
            buf.emitByte(0xC3);
        }
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
        if(from instanceof RegisterValue && to instanceof RegisterValue)
            emitRegToReg(buf, (RegisterValue) from, (RegisterValue) to, type);
        else if(from instanceof RegisterValue && to instanceof StackSlot)
            emitRegToStack(buf, (RegisterValue) from, (StackSlot) to, type);
        else if(from instanceof StackSlot && to instanceof RegisterValue)
            emitStackToReg(buf, (StackSlot) from, (RegisterValue) to, type);
        else if(from instanceof StackSlot && to instanceof StackSlot) {
            // stack → rax/xmm15 → stack
            if (isFloatingPointType(type)) {
                emitStackToXmm(buf, (StackSlot) from, 15, isDouble(type));
                emitXmmToStack(buf, 15, (StackSlot) to, isDouble(type));
            } else {
                emitStackToRegRaw(buf, (StackSlot) from, 0);
                emitRegToStackRaw(buf, 0, (StackSlot) to);
            }
        } else throw new UnsupportedOperationException("Unsupported move: " + from + " -> " + to);
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
        if (isFloatingPointType(type)) {
            emitXmmToXmm(buf, from.getRegister().encoding, to.getRegister().encoding, isDouble(type));
            return;
        }

        if (type.isArray()) {
            emitLea(buf,
                    to.getRegister().encoding,
                    from.getRegister().encoding,
                    getArrayOffset(type));
        } else {
            emitMovRegToReg(buf,
                    from.getRegister().encoding,
                    to.getRegister().encoding);
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
        if (isFloatingPointType(type)) {
            emitXmmToStack(buf, from.getRegister().encoding, to, isDouble(type));
            return;
        }

        if (type.isArray()) {
            emitLea(buf, 0, from.getRegister().encoding, getArrayOffset(type));
            emitRegToStackRaw(buf, 0, to);
        } else
            emitRegToStackRaw(buf, from.getRegister().encoding, to);
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
        if (isFloatingPointType(type)) {
            emitStackToXmm(buf, from, to.getRegister().encoding, isDouble(type));
            return;
        }

        emitStackToRegRaw(buf, from, 0);

        if (type.isArray())
            emitLea(buf, to.getRegister().encoding, 0, getArrayOffset(type));
        else
            emitMovRegToReg(buf, 0, to.getRegister().encoding);
    }

    // ==========================================================
    // RAW ENCODERS
    // ==========================================================

    private void emitMovRegToReg(
            Buffer buf,
            int src,
            int dst
    ) {
        // REX prefix с учётом extended registers
        int rex = 0x48;  // REX.W
        if (src >= 8) rex |= 0x04;  // REX.R
        if (dst >= 8) rex |= 0x01;  // REX.B

        buf.emitByte(rex);
        buf.emitByte(0x89);

        int srcReg = src & 0x07;
        int dstReg = dst & 0x07;
        buf.emitByte(0xC0 | (srcReg << 3) | dstReg);
    }

    private void emitLea(
            Buffer buf,
            int dst,
            int src,
            int imm
    ) {
        // REX.W prefix с учётом extended registers
        int rex = 0x48;
        if (dst >= 8) rex |= 0x04;  // REX.R
        if (src >= 8) rex |= 0x01;  // REX.B

        buf.emitByte(rex);
        buf.emitByte(0x8D);

        // ModR/M byte
        int dstReg = dst & 0x07;  // Младшие 3 бита
        int srcReg = src & 0x07;  // Младшие 3 бита
        buf.emitByte(0x80 | (dstReg << 3) | srcReg);

        buf.emitInt(imm);
    }

    private void emitStackToRegRaw(
            Buffer buf,
            StackSlot slot,
            int dst
    ) {
        // Нужен REX с учётом dst >= 8
        int rex = 0x48;
        if (dst >= 8) rex |= 0x01;  // REX.B

        buf.emitByte(rex);
        buf.emitByte(0x8B);
        buf.emitByte(0x84 | ((dst & 0x07) << 3));  // [rsp+disp32]
        buf.emitByte(0x24);  // SIB для rsp
        buf.emitInt(slot.getRawOffset());
    }

    private void emitRegToStackRaw(
            Buffer buf,
            int src,
            StackSlot slot
    ) {
        int rex = 0x48;
        if (src >= 8) rex |= 0x04;  // REX.R

        buf.emitByte(rex);
        buf.emitByte(0x89);
        buf.emitByte(0x84 | ((src & 0x07) << 3));  // [rsp+disp32]
        buf.emitByte(0x24);  // SIB
        buf.emitInt(slot.getRawOffset());
    }

    private void emitXmmToXmm(
            Buffer buf,
            int src,
            int dst,
            boolean isDouble
    ) {
        // REX prefix для xmm8-xmm15
        int rex = 0;
        if (dst >= 8) rex |= 0x04;  // REX.R
        if (src >= 8) rex |= 0x01;  // REX.B
        if (rex != 0) buf.emitByte(0x40 | rex);

        buf.emitByte(isDouble ? 0xF2 : 0xF3);
        buf.emitByte(0x0F);
        buf.emitByte(0x10);  // MOVSS
        buf.emitByte(0xC0 | ((dst & 0x07) << 3) | (src & 0x07));
    }

    private void emitStackToXmm(
            Buffer buf,
            StackSlot slot,
            int dst,
            boolean isDouble
    ) {
        // REX prefix для xmm8-xmm15
        if (dst >= 8) buf.emitByte(0x44);  // REX.R (0x40 | 0x04)

        buf.emitByte(isDouble ? 0xF2 : 0xF3);
        buf.emitByte(0x0F);
        buf.emitByte(0x10);

        // ModR/M: [rsp+disp32] с SIB
        buf.emitByte(0x84 | ((dst & 0x07) << 3));  // ModR/M
        buf.emitByte(0x24);  // SIB для rsp
        buf.emitInt(slot.getRawOffset());
    }

    private void emitXmmToStack(
            Buffer buf,
            int src,
            StackSlot slot,
            boolean isDouble
    ) {
        // REX prefix для xmm8-xmm15
        if (src >= 8) buf.emitByte(0x44);  // REX.R (0x40 | 0x04)

        buf.emitByte(isDouble ? 0xF2 : 0xF3);
        buf.emitByte(0x0F);
        buf.emitByte(0x11);  // MOVSS to memory (не 0x10!)

        // ModR/M: [rsp+disp32] с SIB
        buf.emitByte(0x84 | ((src & 0x07) << 3));  // ModR/M
        buf.emitByte(0x24);  // SIB для rsp
        buf.emitInt(slot.getRawOffset());
    }

    private void emitSubRsp(Buffer buf, int v) {
        buf.emitByte(0x48);
        buf.emitByte(0x81);
        buf.emitByte(0xEC);
        buf.emitInt(v);
    }

    private void emitAddRsp(Buffer buf, int v) {
        buf.emitByte(0x48);
        buf.emitByte(0x81);
        buf.emitByte(0xC4);
        buf.emitInt(v);
    }
}
