@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package com.huskerdev.nativekt.jvm.jvmci

import com.huskerdev.nativekt.Arch
import com.huskerdev.nativekt.OS
import com.huskerdev.nativekt.jvm.jvmci.conventions.AMD64LinuxCallingConvention
import com.huskerdev.nativekt.jvm.jvmci.conventions.AMD64WindowsCallingConvention
import com.huskerdev.nativekt.jvm.jvmci.conventions.ARM64CallingConvention
import jdk.vm.ci.hotspot.HotSpotCompiledNmethod
import jdk.vm.ci.hotspot.HotSpotJVMCIRuntime
import jdk.vm.ci.hotspot.HotSpotResolvedJavaMethod
import jdk.vm.ci.hotspot.HotSpotVMConfigAccess
import jdk.vm.ci.meta.JavaKind
import jdk.vm.ci.meta.MetaAccessProvider
import jdk.vm.ci.runtime.JVMCI
import jdk.vm.ci.runtime.JVMCIBackend
import java.lang.reflect.Method

abstract class CallingConvention {

    fun createNativeCall(method: Method, address: Long): ByteArray {
        val buf = Buffer()

        emitConversion(buf, method)
        emitCall(buf, method, address)
        emitEpilogue(buf, method)

        return buf.finish()
    }

    abstract fun createNMethod(
        name: String,
        code: ByteArray,
        resolvedMethod: HotSpotResolvedJavaMethod
    ): HotSpotCompiledNmethod

    protected abstract fun emitConversion(buf: Buffer, method: Method)

    protected abstract fun emitCall(buf: Buffer, method: Method, address: Long)

    protected abstract fun emitEpilogue(buf: Buffer, method: Method)

    companion object {
        protected val jvmci: JVMCIBackend = JVMCI.getRuntime().hostJVMCIBackend
        protected val meta: MetaAccessProvider = jvmci.metaAccess
        protected val config = HotSpotVMConfigAccess(HotSpotJVMCIRuntime.runtime().getConfigStore())

        val ENTRY_BARRIER_PATCH: Long = config.getConstant("CodeInstaller::ENTRY_BARRIER_PATCH", Long::class.javaObjectType)

        val current: CallingConvention = when (Arch.current) {
            Arch.ARM64 -> ARM64CallingConvention()
            Arch.X64 -> when {
                OS.current() == OS.WINDOWS -> AMD64WindowsCallingConvention()
                OS.current() == OS.LINUX -> AMD64LinuxCallingConvention()
                else -> throw UnsupportedOperationException("Unsupported OS")
            }
            else -> throw UnsupportedOperationException("Unsupported CPU architecture")
        }

        fun getArrayOffset(javaClass: Class<*>): Int {
            val elementKind = JavaKind.fromJavaClass(javaClass.componentType)
            return meta.getArrayBaseOffset(elementKind)
        }

        fun isFloatingPointType(type: Class<*>): Boolean =
            type == Float::class.java || type == Double::class.java

        fun isIntegerType(type: Class<*>): Boolean =
            type == Byte::class.java || type == Short::class.java ||
            type == Int::class.java || type == Long::class.java ||
            type == Boolean::class.java || type == Char::class.java ||
            !type.isPrimitive

        fun isDouble(type: Class<*>): Boolean =
            type == Double::class.java

        fun align16(v: Int): Int =
            (v + 15) and -16
    }
}
