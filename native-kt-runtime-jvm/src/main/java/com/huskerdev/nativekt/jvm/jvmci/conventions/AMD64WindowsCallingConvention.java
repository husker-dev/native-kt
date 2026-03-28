package com.huskerdev.nativekt.jvm.jvmci.conventions;


import com.huskerdev.nativekt.jvm.jvmci.Buffer;

import java.lang.reflect.Method;

public class AMD64WindowsCallingConvention extends AbstractAMD64CallingConvention {
    public AMD64WindowsCallingConvention() {
        super(
                /* hotspot ints */ new int[] { RDX, R8, R9, RDI, RSI, RCX },
                /* hotspot floats */ new int[] { XMM0, XMM1, XMM2, XMM3, XMM4, XMM5, XMM6, XMM7 },
                /* amd64 ints */ new int[] { RCX, RDX, R8, R9 },
                /* amd64 floats */ new int[] { XMM0, XMM1, XMM2, XMM3 },
                32, false
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
        if(isIntegerType(method.getParameterTypes()[0])) {
            // If integer is the first arg, then iterate from start to end
            for(RegToReg s : intOps)
                s.emit(buf);
        } else {
            // Else iterate from end down to start
            for(int i = intOps.length-1; i >= 0; i--)
                intOps[i].emit(buf);
        }
    }
}
