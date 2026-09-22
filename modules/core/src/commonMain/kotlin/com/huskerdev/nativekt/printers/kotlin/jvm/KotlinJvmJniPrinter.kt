package com.huskerdev.nativekt.printers.kotlin.jvm

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.kotlin.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.*
import com.huskerdev.webidl.resolver.ResolvedIdlType

class KotlinJvmJniPrinter(
    val context: NativeModuleContext,
    builder: StringBuilder,
    name: String,
    parentClass: String? = null,
    val isAndroid: Boolean,
    indent: String = ""
) {
    private val isAndroidCriticalEnabled =
        isAndroid && (context.configuration as? NativeKtAndroidConfiguration)?.useAndroidCriticalNative ?: false

    private val indent1 = indent + "\t"
    private val indent2 = indent1 + "\t"

    init {
        builder.apply {
            val private = if(!isAndroid) "private " else ""
            val parentClass = if(parentClass != null) ": $parentClass" else ""

            append("$indent${private}class $name(libraryPath: String)$parentClass {")

            append("\n${indent1}companion object {")
            printFunctions()
            val utils = printUtilFunctions()
            append("\n${indent1}}")

            printInit(utils)
            printFunctionLinks()

            if(!isAndroid) builder.append("""
                
                override fun _address(name: String): Long =
                    NativeKtUtils.findAddress(name)
            """.replaceIndent(indent1))

            builder.append("\n${indent}}")
        }
    }

    private fun StringBuilder.printInit(utils: List<String>) {
        val methodNames = """
            val methods = this::class.java.declaredMethods
                .mapNotNull { method ->
                    method.getAnnotation(Marker::class.java)
                        ?.id?.let { it to method }
                }
                .sortedBy { it.first }
                .map { it.second }
        """.replaceIndent(indent2)

        val classes = (context.castedEnums + context.castedDictionaries + context.castedInterfaces)
            .filter { it in context.toKotlinDeclarationCasts }
            .map { "${it.kname}::class.java" }
            .chunked(3)
            .joinListToString(prefix = "${indent2}val classes = listOf<Class<*>>(", postfix = ")", baseIndent = indent2) {
                it.joinToString()
            }

        val props = linkedMapOf(
            "class" to """this::class.java.canonicalName!!.replace(".", "/")""",
            "names" to """methods.joinToString(",") { it.name }""",
            "signatures" to """methods.joinToString(",") { JniUtils.getSignature(it) }""",
            "classes" to """classes.joinToString(",") { it.canonicalName!!.replace(".", "/") }"""
        )
        if(isAndroid) {
            props["critical"] = if (isAndroidCriticalEnabled)
                "if(android.os.Build.VERSION.SDK_INT >= 26) \"1\" else \"0\""
            else "\"0\""
        }
        val loadFunc = if(isAndroid) "loadLibrary" else "load"

        // Instance methods
        append("\n${indent1}init {\n$methodNames\n$classes\n")
        props.forEach { (key, value) ->
            append("\n${indent2}System.setProperty(\"nativekt.${context.moduleName}.$key\", $value)")
        }
        append("\n${indent2}System.$loadFunc(libraryPath)")
        props.forEach { (key, _) ->
            append("\n${indent2}System.clearProperty(\"nativekt.${context.moduleName}.$key\")")
        }

        // Keep utils
        if(utils.isNotEmpty()) {
            utils.map { "::$it" }
                .chunked(4)
                .joinListTo(this, prefix = "\n${indent2}keep(", postfix = ")", baseIndent = indent2) {
                    it.joinToString()
                }
        }

        // End
        append("\n${indent1}}")
    }

    private fun StringBuilder.printFunctionLinks() {
        if(context.allOperations.isEmpty())
            return

        context.allOperations.forEach { function ->
            val critical = function.isCritical()
            val isAndroidCriticalNative = isAndroidCriticalEnabled && function.isCritical() && function.isAndroidCriticalCapable()
            val type = when {
                isAndroidCriticalNative && function.type.isInterface() -> ": Long"
                isAndroidCriticalNative && function.type.isEnum() -> ": Int"
                !function.type.isVoid() -> ": ${function.type.toKotlinType()}"
                else -> ""
            }

            val args = function.args.joinToString {
                when {
                    isAndroidCriticalNative && it.type.isInterface() -> "${it.kname}: Long"
                    isAndroidCriticalNative && it.type.isEnum() -> "${it.kname}: Int"
                    else -> "${it.kname}: ${it.type.toKotlinType()}"
                }
            }

            val casts = arrayListOf<String>()
            val castedArgs = function.args.joinToString {
                when {
                    critical && it.type.isString() -> {
                        if(it.type.isNullable) {
                            casts += "val _${it.kname}_bytes = ${it.kname}?.encodeToByteArray()"
                            "_${it.kname}_bytes, _${it.kname}_bytes?.size ?: -1"
                        } else {
                            casts += "val _${it.kname}_bytes = ${it.kname}.encodeToByteArray()"
                            "_${it.kname}_bytes, _${it.kname}_bytes.size"
                        }
                    }
                    critical && it.type.isArray() ->
                        if(it.type.isNullable) "${it.kname}, ${it.kname}?.size ?: -1"
                        else "${it.kname}, ${it.kname}.size"
                    else -> castToNative(it.type, it.kname)
                }
            }

            val override = if(isAndroid) "" else "override "

            val call = castToKotlin(function.type, "_${function.kname}($castedArgs)")

            append("\n$indent1${override}fun ${function.kname}($args)$type")

            if(casts.isNotEmpty()) {
                append(" {")
                casts.forEach { append("\n$indent2$it") }
                append("\n$indent2")
                if(!function.type.isVoid()) append("return ")
                append(call)
                append("\n$indent1}")
            } else append(" = $call")
        }
        append("\n")
    }

    private fun StringBuilder.printFunctions() {
        if(context.allOperations.isEmpty())
            return

        context.allOperations.forEachIndexed { i, function ->
            val critical = function.isCritical()
            val isAndroidFastNative = isAndroidCriticalEnabled && function.isCritical() && !function.isAndroidCriticalCapable()
            val isAndroidCriticalNative = isAndroidCriticalEnabled && function.isCritical() && function.isAndroidCriticalCapable()

            val type = when {
                isAndroidCriticalNative && function.type.isInterface() -> ": Long"
                isAndroidCriticalNative && function.type.isEnum() -> ": Int"
                !function.type.isVoid() -> ": ${function.type.toKotlinType(stringAsBytes = true)}"
                else -> ""
            }

            val args = function.args.joinToString {
                when {
                    isAndroidCriticalNative && it.type.isInterface() -> "${it.kname}: Long"
                    isAndroidCriticalNative && it.type.isEnum() -> "${it.kname}: Int"
                    critical && it.type.isString() -> "${it.kname}: ${it.type.toKotlinType(stringAsBytes = true)}, _${it.kname}_size: Int"
                    critical && it.type.isArray() -> "${it.kname}: ${it.type.toKotlinType(stringAsBytes = true)}, _${it.kname}_length: Int"
                    else -> "${it.kname}: ${it.type.toKotlinType(stringAsBytes = true)}"
                }
            }

            val androidCritical = when {
                isAndroidFastNative -> "@FastNative "
                isAndroidCriticalNative -> "@CriticalNative "
                else -> ""
            }
            val modifiers = "@JvmStatic ${androidCritical}external"

            append("\n${indent2}@Marker($i) $modifiers fun _${function.kname}($args)$type")
        }
    }

    private fun StringBuilder.printUtilFunctions(): List<String> {
        // Util functions
        var i = context.allOperations.size
        val staticFunctions = arrayListOf<String>()

        // Enum
        context.castedEnums.forEach { enum ->
            if (enum in context.toKotlinDeclarationCasts) {
                append("\n$indent2@Marker(${i++}) @JvmStatic fun _cast_${enum.name.camelCase().lowercase()}(of: Int) = ${enum.kname}.entries[of]")
                staticFunctions += "_cast_${enum.name.camelCase().lowercase()}"
            }
        }

        // Dictionaries
        context.castedDictionaries.forEach { dictionary ->
            val fields = context.allFields[dictionary]!!
            val name = dictionary.kname
            val lower = dictionary.name.camelCase().lowercase()

            if(dictionary in context.toKotlinDeclarationCasts) {
                val args = fields.joinToString {
                    "${it.kname}: ${it.type.toKotlinType(stringAsBytes = true)}"
                }
                val argNames = fields.joinToString {
                    castToKotlin(it.type, it.kname)
                }
                append("\n$indent2@Marker(${i++}) @JvmStatic fun _constructor_$lower($args) = ${dictionary.kname}($argNames)")
                staticFunctions += "_constructor_$lower"
            }
            if(dictionary in context.toNativeDeclarationCasts) {
                fields.forEach {
                    val fieldLower = it.name.camelCase().lowercase()
                    append("\n$indent2@Marker(${i++}) @JvmStatic fun _field_${lower}_$fieldLower(of: $name) = ${castToNative(it.type, "of.${it.kname}")}")
                    staticFunctions += "_field_${lower}_$fieldLower"
                }
            }
        }

        // Interfaces
        if(context.hasInterfaceCast) {
            if(context.hasInterfaceCastToNative) {
                append("\n$indent2@Marker(${i++}) @JvmStatic fun _interface_ptr(of: NativeKtRcObject) = of.rcPtr")
                staticFunctions += "_interface_ptr"
            }
            context.castedInterfaces.forEach { inter ->
                if(inter in context.toNativeDeclarationCasts) {
                    append("\n$indent2@Marker(${i++}) @JvmStatic fun _constructor_${inter.name.camelCase().lowercase()}(ptr: Long) = ${inter.kname}(Unit, ptr)")
                    staticFunctions += "_constructor_${inter.name.camelCase().lowercase()}"
                }
            }
        }

        // Callbacks
        if(context.hasCallbackCast) {
            append("\n$indent2@Marker(${i++}) @JvmStatic fun _callback_equals(c1: Any, c2: Any) = c1 == c2")
            append("\n$indent2@Marker(${i++}) @JvmStatic fun _callback_hashCode(c: Any) = c.hashCode()")

            staticFunctions += "_callback_equals"
            staticFunctions += "_callback_hashCode"

            context.castedCallbacks.forEach { callback ->
                val lower = callback.name.camelCase().lowercase()
                val args = buildList {
                    add("of: ${callback.kname}")
                    callback.args.mapTo(this){
                        "${it.kname}: ${it.type.toKotlinType(stringAsBytes = true)}"
                    }
                }.joinToString()
                val argNames = callback.args.joinToString {
                    castToKotlin(it.type, it.kname)
                }

                append("\n$indent2@Marker(${i++}) @JvmStatic fun _invoke_$lower($args) = ${castToNative(callback.type, "of($argNames)")}")
                staticFunctions += "_invoke_$lower"
            }
        }
        return staticFunctions
    }

    private fun castToNative(
        type: ResolvedIdlType,
        content: String
    ) = when {
        type.isString() ->
            if(type.isNullable) "$content?.encodeToByteArray()"
            else "$content.encodeToByteArray()"
        type.isStringArray() ->
            if(type.isNullable) "JniUtils.stringsToBytes($content)"
            else "JniUtils.stringsToBytes($content)!!"
        else -> content
    }

    private fun castToKotlin(
        type: ResolvedIdlType,
        content: String
    ) = when {
        type.isString() ->
            if(type.isNullable) "$content?.decodeToString()"
            else "$content.decodeToString()"
        type.isStringArray() ->
            if(type.isNullable) "JniUtils.bytesToStrings($content)"
            else "JniUtils.bytesToStrings($content)!!"
        else -> content
    }
}