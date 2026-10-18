package com.huskerdev.nativekt.cli

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.stdlibx.fs.SyncPath
import com.huskerdev.stdlibx.printlnError
import com.huskerdev.stdlibx.process.Process
import com.huskerdev.stdlibx.process.exitProcess


fun error(message: Any?, code: Int = 1): Nothing {
    printlnError(message)
    exitProcess(code)
}

class NativeKtExecutor: NativeModuleContext.CommandExecutor {
    override fun exec(
        context: NativeModuleContext,
        command: String,
        workingDir: SyncPath?,
        silent: Boolean,
        errAsStd: Boolean,
        environment: Map<String, String>?
    ): String = Process.exec(
        arguments = command.split(" ").toTypedArray(),
        workingDirectory = workingDir,
        environment = environment,
        silent = silent,
        errAsStd = errAsStd
    ).stdOut
}

class NativeKtLogger: NativeModuleContext.Logger {
    override fun error(text: String) = printlnError(text)
    override fun info(text: String) = println(text)
}

