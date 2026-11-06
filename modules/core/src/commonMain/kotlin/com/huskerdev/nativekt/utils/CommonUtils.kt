package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.Language
import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.TargetType
import com.huskerdev.stdlibx.Arch
import com.huskerdev.stdlibx.OS
import com.huskerdev.stdlibx.fs.SyncPath


fun asyncLoadFunctionName(context: NativeModuleContext) =
    "loadLib${context.moduleName.upperCamelCase()}"

fun syncLoadFunctionName(context: NativeModuleContext) =
    "loadLib${context.moduleName.upperCamelCase()}Sync"

fun loadFieldName(context: NativeModuleContext) =
    "isLib${context.moduleName.upperCamelCase()}Loaded"

private fun String.splitCommon(): List<String> {
    val result = arrayListOf<String>()
    val buffer = StringBuilder()

    fun push() {
        if (buffer.isEmpty()) return
        result += buffer.toString().lowercase()
        buffer.clear()
    }

    forEachIndexed { i, cur ->
        if (cur == '_') {
            push()
            return@forEachIndexed
        } else if (cur.isUpperCase() && buffer.isNotEmpty()) {
            val prev = buffer.last()
            val next = getOrNull(i + 1)
            if (
                prev.isLowerCase() ||
                prev.isDigit() ||
                (prev.isUpperCase() && next?.isLowerCase() == true)
            ) push()
        }
        buffer.append(cur)
    }
    push()
    return result
}

fun String.snakeCase(): String = splitCommon().joinToString(separator = "_")

fun String.camelCase(): String {
    return splitCommon()
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

val Language.supportsEnumIntArray: Boolean
    get() = this != Language.SWIFT

val currentSystemTarget: TargetType
    get() = when(OS.current) {
        OS.WINDOWS -> TargetType.MINGW_X64
        OS.MACOS -> if(Arch.current == Arch.ARM64) TargetType.MACOS_ARM64 else TargetType.MACOS_X64
        OS.LINUX -> if(Arch.current == Arch.ARM64) TargetType.LINUX_ARM64 else TargetType.LINUX_X64
        else -> throw UnsupportedOperationException()
    }