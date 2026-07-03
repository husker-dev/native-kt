package com.huskerdev.nativekt.jvm.jvmci.conventions


import com.huskerdev.nativekt.jvm.jvmci.Buffer

import java.lang.reflect.Method

class AMD64LinuxCallingConvention: AbstractAMD64CallingConvention(
    hotspotIntReg = intArrayOf(RSI, RDX, RCX, R8, R9, RDI),
    hotspotFloatReg = intArrayOf(XMM0, XMM1, XMM2, XMM3, XMM4, XMM5, XMM6, XMM7),
    amd64IntReg = intArrayOf(RDI, RSI, RDX, RCX, R8, R9),
    amd64FloatReg = intArrayOf(XMM0, XMM1, XMM2, XMM3, XMM4, XMM5, XMM6, XMM7),
    shadowSpace = 0,
    continuousIndexing = true
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
        if(intOps.size == 6) {
            RegToReg(RDI, RAX, intOps[5].type).emit(buf)

            for(i in intOps.indices)
                intOps[i].emit(buf)

            RegToReg(RAX, R9, Int::class.java).emit(buf)
        } else {
            intOps.forEach {
                it.emit(buf)
            }
        }
    }
}