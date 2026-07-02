package com.huskerdev.nativekt.jvm.jvmci.conventions;


import com.huskerdev.nativekt.jvm.jvmci.Buffer

import java.lang.reflect.Method

class AMD64WindowsCallingConvention: AbstractAMD64CallingConvention(
    hotspotIntReg = intArrayOf(RDX, R8, R9, RDI, RSI, RCX),
    hotspotFloatReg = intArrayOf(XMM0, XMM1, XMM2, XMM3, XMM4, XMM5, XMM6, XMM7),
    amd64IntReg = intArrayOf(RCX, RDX, R8, R9),
    amd64FloatReg = intArrayOf(XMM0, XMM1, XMM2, XMM3),
    shadowSpace = 32,
    continuousIndexing = false
) {
    override fun emitRegToReg(
        buf: Buffer,
        method: Method,
        floatOps: List<RegToReg>,
        intOps: List<RegToReg>
    ) {
        // Floats (from last to first)
        for(i in floatOps.size-1 downTo 0)
            floatOps[i].emit(buf)

        // Integers
        if(isIntegerType(method.parameterTypes[0])) {
            // If integer is the first arg, then iterate from start to end
            intOps.forEach {
                it.emit(buf)
            }
        } else {
            // Else iterate from end down to start
            for(i in intOps.size-1 downTo 0)
                intOps[i].emit(buf)
        }
    }
}
