package com.huskerdev.nativekt.jvm.jvmci.conventions;


import com.huskerdev.nativekt.jvm.jvmci.Buffer;

import java.lang.reflect.Method;

public class AMD64LinuxCallingConvention extends AbstractAMD64CallingConvention {
    public AMD64LinuxCallingConvention() {
        super(
                /* hotspot ints */ new int[] { RSI, RDX, RCX, R8, R9, RDI },
                /* hotspot floats */ new int[] { XMM0, XMM1, XMM2, XMM3, XMM4, XMM5, XMM6, XMM7 },
                /* amd64 ints */ new int[] { RDI, RSI, RDX, RCX, R8, R9 },
                /* amd64 floats */ new int[] { XMM0, XMM1, XMM2, XMM3, XMM4, XMM5, XMM6, XMM7 },
                0, true
        );
    }

    @Override
    void emitRegToReg(
            Buffer buf,
            Method method,
            RegToReg[] floatOps,
            RegToReg[] intOps
    ) {
        // Floats (from last to first)
        for(int i = floatOps.length-1; i >= 0; i--)
            floatOps[i].emit(buf);

        // Integers
        if(intOps.length == 6) {
            new RegToReg(RDI, RAX, intOps[5].type).emit(buf);

            for(int i = 0; i < intOps.length-1; i++)
                intOps[i].emit(buf);

            new RegToReg(RAX, R9, Integer.class).emit(buf);
        } else {
            for(RegToReg op : intOps)
                op.emit(buf);
        }
    }
}