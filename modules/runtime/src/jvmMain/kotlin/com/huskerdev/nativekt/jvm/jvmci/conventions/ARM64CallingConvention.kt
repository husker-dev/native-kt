@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package com.huskerdev.nativekt.jvm.jvmci.conventions;

import com.huskerdev.nativekt.jvm.jvmci.Buffer
import com.huskerdev.nativekt.jvm.jvmci.CallingConvention
import jdk.vm.ci.code.site.DataPatch
import jdk.vm.ci.code.site.DataSectionReference
import jdk.vm.ci.code.site.Mark
import jdk.vm.ci.hotspot.HotSpotCompiledCode
import jdk.vm.ci.hotspot.HotSpotCompiledNmethod
import jdk.vm.ci.hotspot.HotSpotResolvedJavaMethod
import jdk.vm.ci.meta.Assumptions
import jdk.vm.ci.meta.ResolvedJavaMethod
import jdk.vm.ci.runtime.JVMCICompiler
import java.lang.reflect.Method


class ARM64CallingConvention: CallingConvention() {

    override fun createNMethod(
        name: String,
        code: ByteArray,
        resolvedMethod: HotSpotResolvedJavaMethod
    ): HotSpotCompiledNmethod {
        val a = DataSectionReference()
        a.setOffset(0)

        return HotSpotCompiledNmethod(
            name,
            code,
            code.size,
            arrayOf(
                Mark(code.size - 4, ENTRY_BARRIER_PATCH.toInt()),
                DataPatch(code.size - 4, a)
            ),
            emptyArray<Assumptions.Assumption>(),
            emptyArray<ResolvedJavaMethod>(),
            emptyArray<HotSpotCompiledCode.Comment>(),
            byteArrayOf(0, 0, 0, 0),
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
    }


    override fun emitConversion(
        buf: Buffer,
        method: Method
    ) {
        // 8th integer Java argument clashes with the 1st native arg
        var ints = 0
        for(i in 0 until method.parameterCount) {
            val type = method.parameterTypes[i]
            if(isIntegerType(type))
                ints++

            if(ints == 8) {
                if (type.isArray)
                    emitAddImm(buf, 8, 0, getArrayOffset(type))
                else
                    emitMovReg(buf, 8, 0, type)
                break
            }
        }

        ints = 0
        for(i in 0 until method.parameterCount) {
            val type = method.parameterTypes[i]

            if(type.isArray) {
                val offset = getArrayOffset(type)

                if(ints < 8) {
                    // in registers
                    val dst = ints
                    val src = ++ints
                    emitAddImm(buf, dst, src, offset)
                } else {
                    // in stack (stack -> reg -> stack)
                    val stackOffset = (ints++ - 8) * 8
                    emitLdrSpOffset(buf, 15, stackOffset)
                    emitAddImm(buf, 15, 15, offset)
                    emitStrSpOffset(buf, 15, stackOffset)
                }
            } else if(!isFloatingPointType(type)) {
                // mov x x+1
                val dst = ints
                val src = ++ints
                emitMovReg(buf, dst, src, type)
            }
        }
    }

    override fun emitCall(
        buf: Buffer,
        method: Method,
        address: Long
    ) {
        // write 64-bit address to x9
        emitMovImm64(buf, 9, address)

        // bl x9
        buf.emitInt((0xD61F0000 or (9 shl 5)).toInt())
    }

    override fun emitEpilogue(
        buf: Buffer,
        method: Method
    ) {
        // nmethod entry barrier simulation:
        // LDR W8, [PC, #0]
        buf.emitInt(0x18000008);
    }

    // ==========================================================
    // INSTRUCTION
    // ==========================================================

    private fun emitMovReg(buf: Buffer, dst: Int, src: Int, type: Class<*>) {
        buf.emitInt(when(type) {
            Long::class.java -> {
                // mov x{dst}, x{src}
                0xAA0003E0.toInt() or (src shl 16) or dst
            }
            Byte::class.java -> {
                // uxtb w{dst}, w{src}
                0x53001C00 or (src shl 5) or dst
            }
            Short::class.java, Char::class.java -> {
                // uxth w{dst}, w{src}
                0x53003C00 or (src shl 5) or dst
            }
            else -> {
                // orr w{dst}, wzr, w{src}
                0x2a0003e0 or (src shl 16) or dst
            }
        })
    }

    private fun emitAddImm(buf: Buffer, dst: Int, src: Int, imm: Int) {
        // add x{dst}, x{src}, #imm
        val insn = 0x91000000.toInt() or (imm shl 10) or (src shl 5) or dst
        buf.emitInt(insn)
    }

    private fun emitMovImm64(buf: Buffer, reg: Int, value: Long) {
        // movz x{reg}, #(value & 0xFFFF), lsl #0
        val insn1 = 0xD2800000.toInt() or ((value and 0xFFFF).toInt() shl 5) or reg
        buf.emitInt(insn1)

        // movk x{reg}, #((value >> 16) & 0xFFFF), lsl #16
        if ((value shr 16).toInt() != 0) {
            val insn2 = 0xF2A00000.toInt() or (((value shr 16).toInt() and 0xFFFF) shl 5) or reg
            buf.emitInt(insn2)
        }

        // movk x{reg}, #((value >> 32) & 0xFFFF), lsl #32
        if ((value shr 32).toInt() != 0) {
            val insn3 = 0xF2C00000.toInt() or (((value shr 32).toInt() and 0xFFFF) shl 5) or reg
            buf.emitInt(insn3)
        }

        // movk x{reg}, #((value >> 48) & 0xFFFF), lsl #48
        if ((value shr 48).toInt() != 0) {
            val insn4 = 0xF2E00000.toInt() or (((value shr 48).toInt() and 0xFFFF) shl 5) or reg
            buf.emitInt(insn4)
        }
    }

    private fun emitStrSpOffset(buf: Buffer, reg: Int, offset: Int) {
        // str x{reg}, [sp, #offset]
        // Format: 0xF90003E0 | (offset/8 << 10) | reg
        val offsetEncoded = (offset / 8) and 0xFFF
        val insn = 0xF90003E0.toInt() or (offsetEncoded shl 10) or reg
        buf.emitInt(insn)
    }

    private fun emitLdrSpOffset(buf: Buffer, reg: Int, offset: Int) {
        // ldr x{reg}, [sp, #offset]
        // Format: 0xF94003E0 | (offset/8 << 10) | reg
        val offsetEncoded = (offset / 8) and 0xFFF
        val insn = 0xF94003E0.toInt() or (offsetEncoded shl 10) or reg
        buf.emitInt(insn)
    }
}
