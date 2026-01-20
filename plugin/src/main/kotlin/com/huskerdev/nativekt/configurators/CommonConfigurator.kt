package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.plugin.Configuration
import com.huskerdev.nativekt.plugin.dir
import com.huskerdev.nativekt.plugin.idl
import com.huskerdev.nativekt.printers.HeaderPrinter
import com.huskerdev.nativekt.printers.KotlinCommonPrinter
import org.gradle.api.Project
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.File

fun configureCommon(
    project: Project,
    config: Configuration,
    kotlin: KotlinMultiplatformExtension,
    genDir: File
) {
    val commonGenDir = File(genDir, "common")
    commonGenDir.mkdirs()

    kotlin.sourceSets {
        findByName("commonMain")?.kotlin?.srcDir(commonGenDir)
    }

    config.forEach { module ->
        val idl = module.idl(project)
        val classPathFile = module.classPath.replace(".", "/")

        KotlinCommonPrinter(
            idl = idl,
            target = File(commonGenDir, "$classPathFile/${module.name}.kt"),
            classPath = module.classPath,
            moduleName = module.name
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
}