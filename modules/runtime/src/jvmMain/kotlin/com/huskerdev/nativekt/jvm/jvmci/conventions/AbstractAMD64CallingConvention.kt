@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package com.huskerdev.nativekt.jvm.jvmci.conventions

import com.huskerdev.nativekt.jvm.jvmci.Buffer;
import com.huskerdev.nativekt.jvm.jvmci.CallingConvention;
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

internal const val RAX = 0
internal const val RCX = 1
internal const val RDX = 2
internal const val R8 = 8
internal const val R9 = 9
internal const val RDI = 7
internal const val RSI = 6
internal const val XMM0 = 0
internal const val XMM1 = 1
internal const val XMM2 = 2
internal const val XMM3 = 3
internal const val XMM4 = 4
internal const val XMM5 = 5
internal const val XMM6 = 6
internal const val XMM7 = 7
internal const val XMM15 = 15

abstract class AbstractAMD64CallingConvention(
    protected val hotspotIntReg: IntArray,
    protected val hotspotFloatReg: IntArray,
    protected val amd64IntReg: IntArray,
    protected val amd64FloatReg: IntArray,
    protected val shadowSpace: Int,
    protected val continuousIndexing: Boolean
): CallingConvention() {

    abstract fun emitRegToReg(buf: Buffer, method: Method, floatOps: List<RegToReg>, intOps: List<RegToReg>)

    private fun getAlignedStack(method: Method): Int {
        // (+8 is probably for calling shift)
        return shadowSpace + align16((method.parameterCount - amd64IntReg.size) * 8) + 8
    }

    private fun shouldUseStack(method: Method): Boolean {
        var ints = 0
        var floats = 0
        for(i in 0 until method.parameterCount) {
            val type = method.parameterTypes[i]
            if (isIntegerType(type))
                ints++
            if(isFloatingPointType(type)) 
                floats++
            if(ints > amd64IntReg.size || floats > amd64FloatReg.size)
                return true
        }
        return false
    }

    override fun createNMethod(
        name: String,
        code: ByteArray,
        resolvedMethod: HotSpotResolvedJavaMethod
    ) = HotSpotCompiledNmethod(
        name,
        code,
        code.size,
        arrayOf<Site>( Mark(code.size - 8, ENTRY_BARRIER_PATCH) ),
        emptyArray<Assumptions.Assumption>(),
        emptyArray<ResolvedJavaMethod>(),
        emptyArray<HotSpotCompiledCode.Comment>(),
        byteArrayOf(),
        1,
        emptyArray<DataPatch>(),
        true,
        0,
        null,
        resolvedMethod,
        JVMCICompiler.INVOCATION_ENTRY_BCI,
        1,
        0,
        false
    )

    override fun emitConversion(buf: Buffer, method: Method) {
        if(method.parameterCount == 0)
            return;

        var stackShift = 0
        if(shouldUseStack(method)) {
            stackShift = getAlignedStack(method)
            emitSubRsp(buf, stackShift)

            // (+8 is probably for calling shift)
            stackShift += 8
        }

        // Collect move actions

        val regToReg = arrayListOf<RegToReg>()
        val regToStack = arrayListOf<RegToStack>()
        val stackToStack = arrayListOf<StackToStack>()

        var floats = 0
        var ints = 0
        var jStack = 0
        var cStack = 0
        for(i in 0 until method.parameterCount) {
            val type = method.parameterTypes[i]

            if(isFloatingPointType(type)){
                val r = if(continuousIndexing) floats else i

                if(r < amd64FloatReg.size)
                    regToReg.add(RegToReg(hotspotFloatReg[floats], amd64FloatReg[r], type))
                else if(floats < hotspotFloatReg.size)
                    regToStack.add(RegToStack(hotspotFloatReg[floats], shadowSpace + 8 * cStack++, type))
                else
                    stackToStack.add(StackToStack(stackShift + 8 * jStack++, shadowSpace + 8 * cStack++, type))
                floats++
            } else {
                val r = if(continuousIndexing) ints else i

                if(r < amd64IntReg.size)
                    regToReg.add(RegToReg(hotspotIntReg[ints], amd64IntReg[r], type))
                else if(ints < hotspotIntReg.size)
                    regToStack.add(RegToStack(hotspotIntReg[ints], shadowSpace + 8 * cStack++, type))
                else
                    stackToStack.add(StackToStack(stackShift + 8 * jStack++, shadowSpace + 8 * cStack++, type))
                ints++
            }
        }

        // Move available to stack
        regToStack.forEach { it.emit(buf) }
        stackToStack.forEach { it.emit(buf) }

        emitRegToReg(
            buf, method,
            regToReg.filter { isFloatingPointType(it.type) },
            regToReg.filter { isIntegerType(it.type) }
        )
    }

    inner class RegToReg(
        val from: Int,
        val to: Int,
        val type: Class<*>
    ) {
        fun emit(buf: Buffer) {
            if (type.isArray)
                emitLea(buf, to, from, getArrayOffset(type))
            else if(from != to) {
                if (isFloatingPointType(type))
                    emitMovXmm(buf, from, to, type)
                else
                    emitMov(buf, from, to)
            }
        }
    }

    inner class RegToStack(
        val from: Int,
        val to: Int,
        val type: Class<*>
    ) {
        fun emit(buf: Buffer){
            if (type.isArray) {
                emitLea(buf, 0, from, getArrayOffset(type))
                emitRegToStack(buf, 0, to)
            } else {
                if (isFloatingPointType(type))
                    emitXmmToStack(buf, from, to, isDouble(type))
                else
                    emitRegToStack(buf, from, to)
            }
        }
    }

    inner class StackToReg(
        val from: Int,
        val to: Int,
        val type: Class<*>
    ) {
        fun emit(buf: Buffer){
            if (isFloatingPointType(type))
                emitStackToXmm(buf, from, to, type)
            else {
                emitStackToReg(buf, from, RAX)
                if (type.isArray)
                    emitLea(buf, to, RAX, getArrayOffset(type))
                else
                    emitMov(buf, RAX, to)
            }
        }
    }

    inner class StackToStack(
        val from: Int,
        val to: Int,
        val type: Class<*>
    ) {
        fun emit(buf: Buffer){
            val reg = if(isFloatingPointType(type))
                XMM15 else RAX
            val transferType = if(type.isArray)
                Long::class.java else type

            StackToReg(from, reg, type).emit(buf)
            RegToStack(reg, to, transferType).emit(buf)
        }
    }

    override fun emitCall(buf: Buffer, method: Method, address: Long) {
        // mov rax, target
        buf.emitByte(0x48)
        buf.emitByte(0xB8)
        buf.emitLong(address)

        if(shouldUseStack(method)) {
            // call rax
            buf.emitByte(0xFF)
            buf.emitByte(0xD0)
        } else {
            // jmp rax
            buf.emitByte(0xFF)
            buf.emitByte(0xE0)
        }
    }

    override fun emitEpilogue(buf: Buffer, method: Method) {
        if(shouldUseStack(method)) {
            emitAddRsp(buf, getAlignedStack(method))
            buf.emitByte(0xC3)
        }

        // align to 4
        while(buf.position() % 4 != 0)
            buf.emitByte(0x90)

        // nmethod entry barrier simulation:
        // cmp dword ptr 0, 0x00000000
        buf.emitByte(0x41)
        buf.emitByte(0x81)
        buf.emitByte(0x7f)
        buf.emitByte(0)
        buf.emitInt(0)
    }

    // ==========================================================
    // RAW ENCODERS
    // ==========================================================

    private fun emitMov(
        buf: Buffer,
        src: Int,
        dst: Int
    ) {
        // REX prefix for extended registers
        var rex = 0x48  // REX.W
        if (src >= 8) rex = rex or 0x04  // REX.R
        if (dst >= 8) rex = rex or 0x01  // REX.B

        buf.emitByte(rex)
        buf.emitByte(0x89)

        val srcReg = src and 0x07
        val dstReg = dst and 0x07
        buf.emitByte(0xC0 or (srcReg shl 3) or dstReg)
    }

    private fun emitLea(
        buf: Buffer,
        dst: Int,
        src: Int,
        imm: Int
    ) {
        // REX.W prefix for extended registers
        var rex = 0x48;
        if (dst >= 8) rex = rex or 0x04  // REX.R
        if (src >= 8) rex = rex or 0x01  // REX.B

        buf.emitByte(rex)
        buf.emitByte(0x8D)

        // ModR/M byte
        val dstReg = dst and 0x07
        val srcReg = src and 0x07
        buf.emitByte(0x80 or (dstReg shl 3) or srcReg)

        buf.emitInt(imm)
    }

    private fun emitStackToReg(
        buf: Buffer ,
        src: Int,
        dst: Int
    ) {
        // REX.W prefix for extended registers
        var rex = 0x48;
        if (dst >= 8) rex = rex or 0x01  // REX.B

        buf.emitByte(rex)
        buf.emitByte(0x8B)
        buf.emitByte(0x84 or ((dst and 0x07) shl 3))  // [rsp+disp32]
        buf.emitByte(0x24)  // SIB для rsp
        buf.emitInt(src)
    }

    private fun emitRegToStack(
        buf: Buffer,
        src: Int,
        dst: Int
    ) {
        // REX.W prefix for extended registers
        var rex = 0x48
        if (src >= 8) rex = rex or 0x04  // REX.R

        buf.emitByte(rex)
        buf.emitByte(0x89)
        buf.emitByte(0x84 or ((src and 0x07) shl 3))  // [rsp+disp32]
        buf.emitByte(0x24)  // SIB
        buf.emitInt(dst)
    }

    private fun emitMovXmm(
        buf: Buffer,
        src: Int,
        dst: Int,
        type: Class<*>
    ) {
        buf.emitByte(if(isDouble(type)) 0xF2 else 0xF3)
        buf.emitByte(0x0F)
        buf.emitByte(0x10)
        buf.emitByte(0xC0 or ((dst and 0x07) shl 3) or (src and 0x07))
    }

    private fun emitStackToXmm(
        buf: Buffer ,
        src: Int,
        dst: Int,
        type: Class<*>
    ) {
        buf.emitByte(if(isDouble(type)) 0xF2 else 0xF3)

        // REX prefix для xmm8-xmm15
        if (dst >= 8) buf.emitByte(0x44)  // REX.R (0x40 | 0x04)

        buf.emitByte(0x0F)
        buf.emitByte(0x10)

        // ModR/M: [rsp+disp32] с SIB
        buf.emitByte(0x84 or ((dst and 0x07) shl 3))  // ModR/M
        buf.emitByte(0x24)  // SIB для rsp
        buf.emitInt(src)
    }

    private fun emitXmmToStack(
        buf: Buffer,
        src: Int,
        dst: Int,
        isDouble: Boolean
    ) {
        buf.emitByte(if(isDouble) 0xF2 else 0xF3)

        // REX prefix для xmm8-xmm15
        if (src >= 8) buf.emitByte(0x44)  // REX.R (0x40 | 0x04)

        buf.emitByte(0x0F)
        buf.emitByte(0x11)  // MOVSS to memory (не 0x10!)

        // ModR/M: [rsp+disp32] с SIB
        buf.emitByte(0x84 or ((src and 0x07) shl 3))  // ModR/M
        buf.emitByte(0x24)  // SIB для rsp
        buf.emitInt(dst)
    }

    private fun emitSubRsp(buf: Buffer, v: Int) {
        buf.emitByte(0x48)
        buf.emitByte(0x81)
        buf.emitByte(0xEC)
        buf.emitInt(v)
    }

    private fun emitAddRsp(buf: Buffer, v: Int) {
        buf.emitByte(0x48)
        buf.emitByte(0x81)
        buf.emitByte(0xC4)
        buf.emitInt(v)
    }
}
