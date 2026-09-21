package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.osutils.OS
import io.github.vinceglb.filekit.*
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString


fun asyncLoadFunctionName(context: NativeModuleContext) =
    "loadLib${context.moduleName.upperCamelCase()}"

fun syncLoadFunctionName(context: NativeModuleContext) =
    "loadLib${context.moduleName.upperCamelCase()}Sync"

fun loadFieldName(context: NativeModuleContext) =
    "isLib${context.moduleName.upperCamelCase()}Loaded"

val PlatformFile.posixPath: String
    get() = absolutePath().replace("\\", "/")

fun PlatformFile.writeSync(text: String) {
    parent()?.createDirectories()
    SystemFileSystem.sink(Path(posixPath)).buffered().use {
        it.writeString(text)
        it.flush()
    }
}

fun PlatformFile.readSync(): String {
    return SystemFileSystem.source(Path(posixPath)).buffered().use {
        it.readString()
    }
}

fun PlatformFile.copyToSync(destination: PlatformFile) {
    destination.parent()?.createDirectories()
    val source = SystemFileSystem.source(Path(posixPath)).buffered()
    val sink = SystemFileSystem.sink(Path(destination.posixPath)).buffered()
    try {
        sink.transferFrom(source)
        sink.flush()
    } finally {
        source.close()
        sink.close()
    }
}

fun PlatformFile.deleteSync() {
    val path = Path(posixPath)
    if (isDirectory())
        SystemFileSystem.list(path).forEach { PlatformFile(it).deleteSync() }
    SystemFileSystem.delete(path)
}

fun PlatformFile.fresh(): PlatformFile {
    deleteSync()
    createDirectories()
    return this
}

fun String.snakeCase(): String = buildString {
    this@snakeCase.forEachIndexed { index, c ->
        if(c.isUpperCase() &&
            index > 1 &&
            index < this@snakeCase.length &&
            c.isLowerCase() != this@snakeCase[index-1].isLowerCase()
        ) append('_')

        append(c.lowercase())
    }
}

fun String.camelCase(): String {
    return split("_")
        .joinToString("") { it.uppercaseFirstChar() }
        .replaceFirstChar { it.lowercase() }
}

fun String.upperCamelCase(): String =
    camelCase().uppercaseFirstChar()

fun String.uppercaseFirstChar() =
    replaceFirstChar { c -> c.uppercase() }

fun String.splitRespectingQuotes(): List<String> =
    """[^\s"']+|"([^"]*)"|'([^']*)'""".toRegex()
        .findAll(this)
        .map { it.value.trim('"', '\'').trim() }
        .toList()

fun <T> Iterable<T>.joinListToString(
    separator: String = ",",
    prefix: String = "",
    postfix: String = "",
    baseIndent: String = "",
    transform: ((T) -> CharSequence) = { it.toString() }
) = buildString {
    joinListTo(this, separator, prefix, postfix, baseIndent, transform)
}

fun <T, A : Appendable> Iterable<T>.joinListTo(
    buffer: A,
    separator: String = ",",
    prefix: String = "",
    postfix: String = "",
    baseIndent: String = "",
    transform: ((T) -> CharSequence) = { it.toString() }
) {
    buffer.append(prefix)
    val iterator = iterator()
    while(iterator.hasNext()) {
        buffer.append('\n').append(baseIndent).append('\t').append(transform(iterator.next()))
        if(iterator.hasNext())
            buffer.append(separator)
        else buffer.append('\n').append(baseIndent)
    }
    buffer.append(postfix)
}

fun StringBuilder.printLabel(text: String, padding: Int = 5, indent: String = "") {
    // line 1
    append("\n")
    append(indent)
    append("// ╔")
    append("═".repeat(text.length + padding*2))
    append("╗\n")

    // line 2
    append(indent)
    append("// ║")
    append(" ".repeat(padding))
    append(text)
    append(" ".repeat(padding))
    append("║\n")

    // line 3
    append(indent)
    append("// ╚")
    append("═".repeat(text.length + padding*2))
    append("╝\n")
}

internal fun locate(context: NativeModuleContext, binary: String): PlatformFile? {
    return try {
        PlatformFile(context.execute(
            command = when(OS.current) {
                OS.WINDOWS -> "where $binary"
                else -> "which $binary"
            },
            silent = true
        )).run {
            if (exists()) this else null
        }
    } catch (_: Throwable) {
        null
    }
}