package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.plugin.NativeKtExtension
import com.huskerdev.nativekt.plugin.NativeModule
import com.huskerdev.nativekt.printers.KotlinStubPrinter
import com.huskerdev.webidl.resolver.IdlResolver
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

internal fun configureStub(
    extension: NativeKtExtension,
    idl: IdlResolver,
    module: NativeModule,
    sourceSet: KotlinSourceSet,
    srcGenDir: File
) {
    val stubGenDir = File(srcGenDir, "stub/src")
    stubGenDir.mkdirs()

    val classPathFile = File(stubGenDir, module.classPath.replace(".", "/"))

    sourceSet.kotlin.srcDir(stubGenDir)

    // Create stub sources
    KotlinStubPrinter(
        idl = idl,
        target = File(classPathFile, "${module.name}.kt"),
        classPath = module.classPath,
        moduleName = module.name,
        useCoroutines = extension.useCoroutines
    )
}