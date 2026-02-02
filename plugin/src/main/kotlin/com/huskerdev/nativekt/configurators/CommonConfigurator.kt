package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.plugin.NativeKtExtension
import com.huskerdev.nativekt.plugin.Multiplatform
import com.huskerdev.nativekt.plugin.dir
import com.huskerdev.nativekt.printers.HeaderPrinter
import com.huskerdev.nativekt.printers.KotlinCommonPrinter
import com.huskerdev.webidl.resolver.IdlResolver
import org.gradle.api.Project
import org.gradle.internal.extensions.stdlib.capitalized
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

@OptIn(ExperimentalKotlinGradlePluginApi::class)
internal fun configureCommon(
    project: Project,
    configuration: NativeKtExtension,
    idl: IdlResolver,
    module: Multiplatform,
    sourceSet: KotlinSourceSet,
    srcGenDir: File
) {

    val commonGenDir = File(srcGenDir, "common")
    commonGenDir.mkdirs()

    val classPathFile = File(commonGenDir, module.classPath.replace(".", "/"))

    // TODO: try generatedKotlin
    sourceSet.kotlin.srcDir(commonGenDir)

    KotlinCommonPrinter(
        idl = idl,
        target = File(classPathFile, "${module.name}.kt"),
        classPath = module.classPath,
        moduleName = module.name,
        useCoroutines = configuration.useCoroutines
    )

    project.tasks.register("generateHeader${module.name.capitalized()}") {
        group = "native"
        doLast {
            HeaderPrinter(
                idl = idl,
                target = File(module.dir(project), "api.h"),
                guardName = module.name.uppercase()
            )
        }
    }
}