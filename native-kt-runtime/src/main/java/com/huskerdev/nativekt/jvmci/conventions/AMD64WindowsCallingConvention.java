package com.huskerdev.nativekt.jvmci.conventions;

import com.huskerdev.nativekt.jvmci.Buffer;
import com.huskerdev.nativekt.jvmci.CallingConvention;
import jdk.vm.ci.code.site.DataPatch;
import jdk.vm.ci.code.site.Mark;
import jdk.vm.ci.code.site.Site;
import jdk.vm.ci.hotspot.HotSpotCompiledCode;
import jdk.vm.ci.hotspot.HotSpotCompiledNmethod;
import jdk.vm.ci.hotspot.HotSpotResolvedJavaMethod;
import jdk.vm.ci.meta.Assumptions;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.runtime.JVMCICompiler;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class AMD64WindowsCallingConvention extends CallingConvention {

    private static final int RAX = 0, RCX = 1, RDX = 2, R8 = 8, R9 = 9, RDI = 7, RSI = 6;
    private static final int XMM0 = 0, XMM1 = 1, XMM2 = 2, XMM3 = 3, XMM4 = 4, XMM5 = 5, XMM6 = 6, XMM7 = 7, XMM15 = 15;

    private static final int[] hotspotIntReg = new int[] { RDX, R8, R9, RDI, RSI, RCX };
    private static final int[] hotspotFloatReg = new int[] { XMM0, XMM1, XMM2, XMM3, XMM4, XMM5, XMM6, XMM7 };

    private static final int[] amd64IntReg = new int[] { RCX, RDX, R8, R9 };
    private static final int[] amd64FloatReg = new int[] { XMM0, XMM1, XMM2, XMM3 };

    private static final int WINDOWS_SHADOW_SPACE = 32;

    private int getAlignedStack(Method method){
        // Don't know why +8
        return WINDOWS_SHADOW_SPACE + align16((method.getParameterCount() - 4) * 8) + 8;
    }

    @Override
    public HotSpotCompiledNmethod createNMethod(String name, byte[] code, HotSpotResolvedJavaMethod resolvedMethod) {
        return new HotSpotCompiledNmethod(
                name,
                code,
                code.length,
                new Site[] { new Mark(code.length - 8, ENTRY_BARRIER_PATCH) },
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
    protected void emitPrologue(Buffer buf, Method method) { }

    @Override
    protected void emitConversion(Buffer buf, Method method) {
        if(method.getParameterCount() == 0)
            return;

        int stackShift = 0;
        if(method.getParameterCount() > 4) {
            stackShift = getAlignedStack(method);
            emitSubRsp(buf, stackShift);

            // Don't know why +8
            stackShift += 8;
        }

        // Collect move actions

        ArrayList<RegToReg> regToReg = new ArrayList<>();
        ArrayList<RegToStack> regToStack = new ArrayList<>();
        ArrayList<StackToStack> stackToStack = new ArrayList<>();

        for (int i = 0, floats = 0, ints = 0, jStack = 0, cStack = 0; i < method.getParameterCount(); i++) {
            Class<?> type = method.getParameterTypes()[i];

            if(isFloatingPointType(type)){
                if(i < amd64FloatReg.length)
                    regToReg.add(new RegToReg(hotspotFloatReg[floats], amd64FloatReg[i], type));
                else if(floats < hotspotFloatReg.length)
                    regToStack.add(new RegToStack(hotspotFloatReg[floats], 32 + 8*cStack++, type));
                else
                    stackToStack.add(new StackToStack(stackShift + 8*jStack++, 32 + 8*cStack++, type));

                floats++;
            } else {
                if(i < amd64IntReg.length)
                    regToReg.add(new RegToReg(hotspotIntReg[ints], amd64IntReg[i], type));
                else if(ints < hotspotIntReg.length)
                    regToStack.add(new RegToStack(hotspotIntReg[ints], 32 + 8*cStack++, type));
                else
                    stackToStack.add(new StackToStack(stackShift + 8*jStack++, 32 + 8*cStack++, type));

                ints++;
            }
        }

        // Move available to stack
        for(RegToStack s : regToStack)
            s.emit(buf);
        for(StackToStack s : stackToStack)
            s.emit(buf);

        // Floats
        for(int i = regToReg.size()-1; i >= 0; i--) {
            RegToReg s = regToReg.get(i);
            if(!isFloatingPointType(s.type))
                continue;
            s.emit(buf);
        }

        // Integers
        if(isIntegerType(method.getParameterTypes()[0])) {
            // If integer is the first arg, then iterate from start to end
            for(RegToReg s : regToReg) {
                if(!isIntegerType(s.type))
                    continue;
                s.emit(buf);
            }
        } else {
            // If integer is the last arg, then iterate from end down to start
            for(int i = regToReg.size()-1; i >= 0; i--) {
                RegToReg s = regToReg.get(i);
                if(!isIntegerType(s.type))
                    continue;
                s.emit(buf);
            }
        }
    }

    class RegToReg {
        final int from, to;
        final Class<?> type;

        public RegToReg(int from, int to, Class<?> type) {
            this.from = from;
            this.to = to;
            this.type = type;
        }

        void emit(Buffer buf){
            if (type.isArray())
                emitLea(buf, to, from, getArrayOffset(type));
            else if(from != to) {
                if (isFloatingPointType(type))
                    emitMovXmm(buf, from, to, type);
                else
                    emitMov(buf, from, to);
            }
        }
    }

    class RegToStack {
        final int from, to;
        final Class<?> type;

        public RegToStack(int from, int to, Class<?> type) {
            this.from = from;
            this.to = to;
            this.type = type;
        }

        void emit(Buffer buf){
            if (type.isArray()) {
                emitLea(buf, 0, from, getArrayOffset(type));
                emitRegToStack(buf, 0, to);
            } else if(from != to) {
                if (isFloatingPointType(type))
                    emitXmmToStack(buf, from, to, isDouble(type));
                else
                    emitRegToStack(buf, from, to);
            }
        }
    }

    class StackToReg {
        final int from, to;
        final Class<?> type;

        public StackToReg(int from, int to, Class<?> type) {
            this.from = from;
            this.to = to;
            this.type = type;
        }

        void emit(Buffer buf){
            if (isFloatingPointType(type))
                emitStackToXmm(buf, from, to, type);
            else {
                emitStackToReg(buf, from, RAX);
                if (type.isArray())
                    emitLea(buf, to, RAX, getArrayOffset(type));
                else
                    emitMov(buf, RAX, to);
            }
        }
    }

    class StackToStack {
        final int from, to;
        final Class<?> type;

        public StackToStack(int from, int to, Class<?> type) {
            this.from = from;
            this.to = to;
            this.type = type;
        }

        void emit(Buffer buf){
            int reg = isFloatingPointType(type) ? XMM15 : RAX;

            new StackToReg(from, reg, type).emit(buf);
            new RegToStack(reg, to, type).emit(buf);
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
    protected void emitEpilogue(Buffer buf, Method method) {
        if(method.getParameterCount() > 4) {
            emitAddRsp(buf, getAlignedStack(method));
            buf.emitByte(0xC3);
        }

        // align to 4
        while(buf.position() % 4 != 0)
            buf.emitByte(0x90);

        // nmethod entry barrier simulation:
        // cmp dword ptr 0, 0x00000000
        buf.emitByte(0x41);
        buf.emitByte(0x81);
        buf.emitByte(0x7f);
        buf.emitByte(0);
        buf.emitInt(0);
    }

    // ==========================================================
    // RAW ENCODERS
    // ==========================================================

    private static void emitMov(
            Buffer buf,
            int src,
            int dst
    ) {
        // REX prefix for extended registers
        int rex = 0x48;  // REX.W
        if (src >= 8) rex |= 0x04;  // REX.R
        if (dst >= 8) rex |= 0x01;  // REX.B

        buf.emitByte(rex);
        buf.emitByte(0x89);

        int srcReg = src & 0x07;
        int dstReg = dst & 0x07;
        buf.emitByte(0xC0 | (srcReg << 3) | dstReg);
    }

    private static void emitLea(
            Buffer buf,
            int dst,
            int src,
            int imm
    ) {
        // REX.W prefix for extended registers
        int rex = 0x48;
        if (dst >= 8) rex |= 0x04;  // REX.R
        if (src >= 8) rex |= 0x01;  // REX.B

        buf.emitByte(rex);
        buf.emitByte(0x8D);

        // ModR/M byte
        int dstReg = dst & 0x07;
        int srcReg = src & 0x07;
        buf.emitByte(0x80 | (dstReg << 3) | srcReg);

        buf.emitInt(imm);
    }

    private static void emitStackToReg(
            Buffer buf,
            int src,
            int dst
    ) {
        // REX.W prefix for extended registers
        int rex = 0x48;
        if (dst >= 8) rex |= 0x01;  // REX.B

        buf.emitByte(rex);
        buf.emitByte(0x8B);
        buf.emitByte(0x84 | ((dst & 0x07) << 3));  // [rsp+disp32]
        buf.emitByte(0x24);  // SIB для rsp
        buf.emitInt(src);
    }

    private static void emitRegToStack(
            Buffer buf,
            int src,
            int dst
    ) {
        // REX.W prefix for extended registers
        int rex = 0x48;
        if (src >= 8) rex |= 0x04;  // REX.R

        buf.emitByte(rex);
        buf.emitByte(0x89);
        buf.emitByte(0x84 | ((src & 0x07) << 3));  // [rsp+disp32]
        buf.emitByte(0x24);  // SIB
        buf.emitInt(dst);
    }

    private static void emitMovXmm(
            Buffer buf,
            int src,
            int dst,
            Class<?> type
    ) {
        buf.emitByte(isDouble(type) ? 0xF2 : 0xF3);
        buf.emitByte(0x0F);
        buf.emitByte(0x10);
        buf.emitByte(0xC0 | ((dst & 0x07) << 3) | (src & 0x07));
    }

    private static void emitStackToXmm(
            Buffer buf,
            int src,
            int dst,
            Class<?> type
    ) {
        // REX prefix для xmm8-xmm15
        if (dst >= 8) buf.emitByte(0x44);  // REX.R (0x40 | 0x04)

        buf.emitByte(isDouble(type) ? 0xF2 : 0xF3);
        buf.emitByte(0x0F);
        buf.emitByte(0x10);

        // ModR/M: [rsp+disp32] с SIB
        buf.emitByte(0x84 | ((dst & 0x07) << 3));  // ModR/M
        buf.emitByte(0x24);  // SIB для rsp
        buf.emitInt(src);
    }

    private static void emitXmmToStack(
            Buffer buf,
            int src,
            int dst,
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
        buf.emitInt(dst);
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
