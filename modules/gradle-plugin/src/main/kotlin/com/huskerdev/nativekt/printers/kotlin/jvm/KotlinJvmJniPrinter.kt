package com.huskerdev.nativekt.printers.kotlin.jvm

import com.huskerdev.nativekt.utils.*
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

        builder.append("class $name(libraryPath: String)")
        if(parentClass != null)
            builder.append(": $parentClass")
        builder.append(" {\n")

        val unmangle = buildString {
            append("\n$indent\t\t")
            append("val names = listOf(\n\t\t\t")

            idl.allOperators()
                .map { it to it.kname }
                .chunked(6)
                .joinTo(this, separator = ",\n$indent\t\t\t") {
                    it.joinToString { el ->
                        val operator = el.first
                        val name = el.second

                        if(isAndroid && isAndroidCriticalEnabled && operator.isCritical() && operator.isAndroidCriticalCapable())
                            "c(\"$name\")"
                        else "\"$name\""
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

            idl.allOperators().forEach { function ->
                val isInterfaceOperation = function.isInterfaceOperation()

                val type = if(!function.type.isVoid()) {
                    ": " + if(function.isInterfaceOperationConstructor())
                        "Long"
                    else function.type.toKotlinType()
                } else ""

                builder.append("${indent}\t\t@JvmStatic ")

                // If function is critical but contains arrays or string, then apply @FastNative
                if(isAndroidCriticalEnabled && function.isCritical() && !function.isAndroidCriticalCapable())
                    builder.append("@FastNative ")

                printFunctionHeader(
                    builder, function,
                    isExternal = true,
                    printType = false
                )
                builder.append(type)

                if(isAndroidCriticalEnabled && function.isCritical() && function.isAndroidCriticalCapable()) {
                    builder.append("\n${indent}\t\t@JvmStatic @CriticalNative ")
                    printFunctionHeader(
                        builder, function,
                        name = "c_${function.kname}",
                        printType = !isInterfaceOperation,
                        isExternal = true,
                        enumAsInt = true,
                        arraysLen = true,
                        stringAsBytes = true,
                    )
                    if(isInterfaceOperation)
                        builder.append(type)
                }
                builder.append("\n")
            }
            builder.append("${indent}\t}\n")
            builder.append($$"""
                init {
                    fun c(name: String) = if(supportsCritical) "c_$name" else name
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

            idl.allOperators().forEach { function ->
                val isInterfaceConstructor = function.isInterfaceOperationConstructor()

                builder.append("$indent\t")
                printFunctionHeader(
                    builder, function,
                    printType = !isInterfaceConstructor,
                    isExternal = true,
                    isOverride = parentClass != null
                )
                if(isInterfaceConstructor)
                    builder.append(": Long")
                builder.append("\n")
            }
        }
        builder.append("${indent}}")
    }
}