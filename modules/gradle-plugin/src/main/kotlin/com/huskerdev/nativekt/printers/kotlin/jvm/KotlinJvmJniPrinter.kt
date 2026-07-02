package com.huskerdev.nativekt.printers.kotlin.jvm

import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.isAndroidCriticalCapable
import com.huskerdev.nativekt.utils.isCritical
import com.huskerdev.nativekt.utils.printFunctionHeader
import com.huskerdev.webidl.resolver.IdlResolver

class KotlinJvmJniPrinter(
    idl: IdlResolver,
    builder: StringBuilder,
    name: String = "JNI",
    parentClass: String? = null,
    isAndroid: Boolean,
    isAndroidCriticalEnabled: Boolean,
    indent: String = ""
) {
    init {
        builder.append(indent)
        if(!isAndroid)
            builder.append("private ")

        builder.append("class ")
        builder.append(name)
        builder.append("(libraryPath: String)")
        if(parentClass != null)
            builder.append(": $parentClass")
        builder.append(" {\n")

        val unmangle = buildString {
            append("\n$indent\t\t")
            append("val names = listOf(\n\t\t\t")
            idl.globalOperators().chunked(6).joinTo(this, separator = ",\n$indent\t\t\t") {
                it.joinToString { el ->
                    if(isAndroid && isAndroidCriticalEnabled && el.isCritical() && el.isAndroidCriticalCapable())
                        "c(\"${el.name}\")"
                    else "\"${el.name}\""
                }
            }
            append("\n")

            append("""
                ).associateWith { it }.toMutableMap()
                
                // Unmangle
                ${name}::class.java.methods.forEach { method ->
                    val mangled = method.name
                    val original = mangled.substringBefore("-")
                    if(mangled != original && original in names)
                        names[original] = mangled
                }
            """.replaceIndent("$indent\t\t"))
            append("\n")
        }

        if(isAndroid) {
            // Static functions
            builder.append("$indent\tcompanion object {\n")
            builder.append("$indent\t\t@JvmStatic external fun nJNILoad(mangledNames: Array<String>${if(isAndroidCriticalEnabled) ", critical: Boolean" else ""})")
            builder.append("\n")

            idl.globalOperators().forEach { function ->
                builder.append("${indent}\t\t@JvmStatic ")

                // If function is critical but contains arrays or string, then apply @FastNative
                if(isAndroidCriticalEnabled && function.isCritical() && !function.isAndroidCriticalCapable())
                    builder.append("@FastNative ")

                printFunctionHeader(
                    builder, function,
                    isExternal = true
                )

                if(isAndroidCriticalEnabled && function.isCritical() && function.isAndroidCriticalCapable()) {
                    builder.append("\n${indent}\t\t@JvmStatic @CriticalNative ")
                    printFunctionHeader(
                        builder, function,
                        name = "_${function.name}",
                        isExternal = true,
                        enumAsInt = true,
                        arraysLen = true,
                        stringAsBytes = true,
                    )
                }
                builder.append("\n")
            }
            builder.append("${indent}\t}\n")
            builder.append($$"""
                init {
                    fun c(name: String) = if(supportsCritical) "_$name" else name
            """.replaceIndent("$indent\t"))
            builder.append(unmangle)
            builder.append("""
                    
                    System.loadLibrary(libraryPath)
                    nJNILoad(names.values.toTypedArray()${if (isAndroidCriticalEnabled) ", supportsCritical" else ""})
                }
            """.replaceIndent("$indent\t"))
            builder.append("\n")
        } else {
            // Instance methods
            builder.append("""
                companion object {
                    @JvmStatic external fun nJNILoad(mangledNames: Array<String>)
                }
                init {
            """.replaceIndent("$indent\t"))
            builder.append(unmangle)
            builder.append("""
                    
                    System.load(libraryPath)
                    nJNILoad(names.values.toTypedArray())
                }
                
                override fun _address(name: String): Long =
                    NativeKtUtils.findAddress(name)
                
            """.replaceIndent("$indent\t"))
            builder.append("\n")

            idl.globalOperators().forEach { function ->
                builder.append("${indent}\t")
                printFunctionHeader(
                    builder, function,
                    isExternal = true,
                    isOverride = parentClass != null
                )
                builder.append("\n")
            }
        }
        builder.append("${indent}}")
    }
}