package com.huskerdev.nativekt.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.mordant.platform.MultiplatformSystem.exitProcess
import com.huskerdev.nativekt.BuildSystem
import com.huskerdev.nativekt.Language
import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.createContext
import com.huskerdev.nativekt.parseModuleFromToml
import com.huskerdev.nativekt.printers.c.CApiHeaderPrinter
import com.huskerdev.nativekt.printers.c.CApiImplPrinter
import com.huskerdev.nativekt.printers.cpp.CppApiHeaderPrinter
import com.huskerdev.nativekt.printers.cpp.CppApiImplPrinter
import com.huskerdev.nativekt.printers.rust.RustPrinter
import com.huskerdev.stdlibx.fs.SyncPath

fun main(argv: Array<String>) = NativeKtCli()
    .subcommands(PrintNdlPath(), Generate())
    .main(argv)

class NativeKtCli: CliktCommand(name = "nativekt-cli") {
    override fun run() = Unit
}

class PrintNdlPath: CliktCommand(name = "print-ndl-path") {
    val config by option(help = "Toml configuration file").required()

    override fun run() {
        try {
            val module = parseModuleFromToml(SyncPath(config))
            echo(module.apiFile.absoluteString())
        } catch (e: Throwable) {
            e.printStackTrace()
            exitProcess(1)
        }
    }
}

class Generate: CliktCommand(name = "generate") {
    val config by option(help = "Toml configuration file").required()
    val outputDir by option("--output", help = "Directory where the files will be generated").required()

    override fun run() {
        try {
            val outputPath = SyncPath(outputDir)

            val module = parseModuleFromToml(SyncPath(config))
            val context = createContext(
                buildDir = outputPath,
                module = module,
                executor = NativeModuleContext.CommandExecutor.Noop(),
                logger = NativeModuleContext.Logger.Noop()
            )!!

            when (val buildSystem = module.buildSystem) {
                is BuildSystem.CMake -> {
                    val sourcesDir = outputPath.resolve("sources").createDirectories()
                    val includeDir = outputPath.resolve("include").createDirectories()

                    when (buildSystem.language) {
                        Language.C -> {
                            CApiHeaderPrinter(
                                context = context,
                                target = includeDir.resolve("nativekt.h"),
                            )
                            CApiHeaderPrinter(
                                context = context,
                                target = includeDir.resolve("nativekt_impl.h"),
                                isImpl = true
                            )
                            CApiImplPrinter(
                                context = context,
                                target = sourcesDir.resolve("nativekt.c"),
                                includePath = "nativekt_impl.h"
                            )
                        }
                        Language.CPP -> {
                            CppApiHeaderPrinter(
                                context = context,
                                target = includeDir.resolve("nativekt.hpp"),
                                tppTarget = includeDir.resolve("nativekt.tpp")
                            )
                            CppApiImplPrinter(
                                context = context,
                                target = sourcesDir.resolve("nativekt.cpp"),
                                includePath = "nativekt.hpp"
                            )
                        }
                        else -> throw UnsupportedOperationException()
                    }
                }
                is BuildSystem.Cargo -> RustPrinter(
                    context = context,
                    target = outputPath.resolve("nativekt.rs")
                )
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            exitProcess(1)
        }
    }
}