package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.NdlEnv
import com.huskerdev.nativekt.TargetType
import com.huskerdev.nativekt.plugin.DebugKind
import com.huskerdev.nativekt.plugin.NativeProject
import com.huskerdev.osutils.Arch
import com.huskerdev.osutils.OS
import com.huskerdev.webidl.WebIDL
import com.huskerdev.webidl.jvm.iterator
import com.huskerdev.webidl.resolver.*
import com.huskerdev.webidl.resolver.WebIDLBuiltinKind.*
import org.gradle.api.Task
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream


val File.posixPath: String
    get() = absolutePath.replace("\\", "/")

fun NativeProject.idl() = ndlFile().reader().use {
    WebIDL.resolve(
        iterable = it.iterator(),
        env = NdlEnv()
    )
}

fun Task.dependsOnProjectReload() {
    // Invoke task when reloading using IDEA
    project.rootProject.tasks
        .matching { it.name == "prepareKotlinBuildScriptModel" }
        .configureEach {
            dependsOn(this@dependsOnProjectReload)
        }
}

internal fun locate(execOps: ExecOperations, context: NativeModuleContext, binary: String): File? {
    return try {
        File(execOps.exec(context,
            command = when(OS.current) {
                OS.WINDOWS -> "where $binary"
                else -> "which $binary"
            },
            silent = true
        )).run { if (exists()) this else null }
    } catch (_: Throwable) {
        null
    }
}

fun ExecOperations.exec(
    context: NativeModuleContext,
    command: String,
    workingDir: File? = null,
    silent: Boolean = false,
    errAsStd: Boolean = false,
    env: Map<String, String>? = null
): String {
    val printExec = DebugKind.PRINT_EXEC in context.debug
    if(printExec)
        System.err.println("[nativekt] Executed command for '${context.moduleName}': \n\t$command\n\tat: ${workingDir?.absolutePath ?: File("./").absolutePath}")

    class StringOutputStream(
        private val delegate: OutputStream,
        private val bytes: ByteArrayOutputStream = ByteArrayOutputStream()
    ): OutputStream() {
        override fun write(b: Int) {
            bytes.write(b)
            if(!silent) delegate.write(b)
        }
        override fun write(b: ByteArray, off: Int, len: Int) {
            bytes.write(b, off, len)
            if(!silent) delegate.write(b, off, len)
        }
        override fun flush() = delegate.flush()
        fun toDecodedString() = String(bytes.toByteArray(), Charsets.UTF_8)
    }

    fun String.stripAnsi() = replace(
        "\u001B\\[[0-?]*[ -/]*[@-~]|\u001B][^\u0007]*\u0007".toRegex(),
        ""
    )

    val stdOut = StringOutputStream(System.out)
    val errOut = StringOutputStream(System.err)

    return exec {
        isIgnoreExitValue = true
        if(workingDir != null)
            this.workingDir = workingDir

        if(OS.current == OS.WINDOWS)
            commandLine("cmd.exe", "/c", command)
        else
            commandLine("/bin/bash", "-c", command)

        standardOutput = stdOut
        errorOutput = if(errAsStd) stdOut else errOut
        env?.forEach(environment::put)
    }.run {
        if(exitValue != 0) throw Exception(buildString {
            appendLine("Failed to execute command (code=${exitValue}): ")
            appendLine(command)
            if(env != null) {
                append("Environment:")
                env.forEach { (key, value) -> append("\n   $key = $value") }
                append("\n")
            }
            appendLine("Error:")
            append(if(errAsStd) stdOut.toDecodedString() else errOut.toDecodedString())
        })
        stdOut.toDecodedString().stripAnsi().trim()
    }
}

fun currentTargetType(): TargetType = when(OS.current) {
    OS.WINDOWS -> TargetType.MINGW_X64
    OS.MACOS -> when(Arch.current) {
        Arch.ARM64 -> TargetType.MACOS_ARM64
        else -> TargetType.MACOS_X64
    }
    OS.LINUX -> when(Arch.current) {
        Arch.ARM64 -> TargetType.LINUX_ARM64
        else -> TargetType.LINUX_X64
    }
    else -> throw UnsupportedOperationException()
}

fun File.fresh(): File {
    deleteRecursively()
    mkdirs()
    return this
}

fun validateIDL(idl: IdlResolver) {
    fun checkType(type: ResolvedIdlType, isInsideArray: Boolean = false) {
        when(type) {
            is ResolvedIdlType.Union ->
                throw UnsupportedOperationException("Union types are not supported: $type")
            is ResolvedIdlType.Default -> when(val declaration = type.declaration) {
                is BuiltinIdlDeclaration -> when(declaration.kind) {
                    ANY,
                    MUTABLE_LIST,
                    MAP,
                    PROMISE,
                    USV_STRING,
                    BIG_INT,
                    UNRESTRICTED_FLOAT,
                    UNRESTRICTED_DOUBLE,
                    BYTE_SEQUENCE,
                    OBJECT -> throw UnsupportedOperationException("Unsupported type: ${declaration.kind}")
                    STRING,
                    VOID,
                    BOOLEAN,
                    CHAR,
                    INT,
                    UNSIGNED_INT,
                    FLOAT,
                    DOUBLE,
                    BYTE,
                    UNSIGNED_BYTE,
                    SHORT,
                    UNSIGNED_SHORT,
                    LONG,
                    UNSIGNED_LONG -> Unit // ok
                    LIST -> {
                        if(isInsideArray)
                            throw UnsupportedOperationException("Nested arrays are not supported: $type")
                        checkType(type.parameters[0], true)
                    }
                }
                is ResolvedIdlCallbackFunction -> {
                    if(isInsideArray)
                        throw UnsupportedOperationException("Callback arrays are not supported yet")
                }
                is ResolvedIdlInterface,
                is ResolvedIdlDictionary,
                is ResolvedIdlEnum,
                is ResolvedIdlNamespace,
                is ResolvedIdlTypeDef -> Unit // ok
            }
            is ResolvedIdlType.Void -> Unit // ok
        }
    }
    fun checkName(name: String) {
        if(name.startsWith("_"))
            throw UnsupportedOperationException("Identifiers cannot begin with an underscore: $name")
    }
    fun checkField(field: ResolvedIdlField) {
        checkType(field.type)
        checkName(field.name)
    }
    fun checkOperation(operation: ResolvedIdlOperation) {
        checkType(operation.type)
        checkName(operation.name)
        operation.args.forEach { checkField(it) }
    }

    idl.namespaces.values.forEach { namespace ->
        if(namespace.name != "global")
            throw UnsupportedOperationException("Only 'global' namespace is supported yet")
        namespace.operations.forEach { checkOperation(it) }
    }

    idl.callbacks.values.forEach { callback ->
        checkType(callback.type)
        checkName(callback.name)
        callback.args.forEach { checkField(it) }
    }

    idl.dictionaries.values.forEach { dictionary ->
        checkName(dictionary.name)
        dictionary.fields.forEach { checkField(it) }
    }

    idl.enums.values.forEach { enum ->
        checkName(enum.name)
        if(enum.elements.isEmpty())
            throw UnsupportedOperationException("Use of empty enum '${enum.name}'")
        enum.elements.forEach { checkName(it) }
    }

    idl.allOperations().forEach { operation ->
        val isInterfaceConstructor = operation.isInterfaceOperationConstructor()
        val isInterfaceOperation = operation.isInterfaceOperation()

        if(operation.isCritical() && !operation.isCriticalCapable())
            throw UnsupportedOperationException("Operation '${operation.name}' is not critical capable")

        checkType(operation.type)
        checkName(operation.name)
        operation.args.forEachIndexed { i, it ->
            if(i == 0 && isInterfaceOperation && !isInterfaceConstructor)
                return@forEach
            checkField(it)
        }
    }
}