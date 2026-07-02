@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package com.huskerdev.nativekt.jvm.jvmci

import com.huskerdev.nativekt.jvm.NativeKtUtils
import jdk.vm.ci.hotspot.HotSpotResolvedJavaMethod
import jdk.vm.ci.runtime.JVMCI
import java.lang.reflect.Method

object JVMCIUtils {
    init {
        if(NativeKtUtils.isAutoExportEnabled()) {
            NativeKtUtils.addExports(
                JVMCIUtils::class.java.module,
                "jdk.internal.vm.ci",
                arrayOf(
                    "jdk.vm.ci.code",
                    "jdk.vm.ci.runtime",
                    "jdk.vm.ci.meta",
                    "jdk.vm.ci.hotspot",
                    "jdk.vm.ci.code.site"
                )
            )
            NativeKtUtils.addExports(
                "jdk.graal.compiler",
                "java.base",
                arrayOf("jdk.internal.misc")
            )
        }
    }

    val emptyCharArray = charArrayOf()
    val emptyBooleanArray = booleanArrayOf()
    val emptyByteArray = byteArrayOf()
    val emptyShortArray = shortArrayOf()
    val emptyIntArray = intArrayOf()
    val emptyLongArray = longArrayOf()
    val emptyFloatArray = floatArrayOf()
    val emptyDoubleArray = doubleArrayOf()

    fun linkNativeCall(method: Method, address: Long){
        val convention = CallingConvention.current

        val jvmci = JVMCI.getRuntime().hostJVMCIBackend
        val resolvedMethod = jvmci.metaAccess.lookupJavaMethod(method) as HotSpotResolvedJavaMethod

        jvmci.codeCache.setDefaultCode(
            resolvedMethod,
            convention.createNMethod(
                method.name,
                convention.createNativeCall(method, address),
                resolvedMethod
            )
        )
    }
}
