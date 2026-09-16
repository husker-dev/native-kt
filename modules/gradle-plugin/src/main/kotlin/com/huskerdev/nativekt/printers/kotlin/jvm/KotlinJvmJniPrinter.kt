package com.huskerdev.nativekt.printers.kotlin.jvm

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.plugin.NativeKtAndroidInterface
import com.huskerdev.nativekt.utils.*

class KotlinJvmJniPrinter(
    val context: NativeModuleContext,
    builder: StringBuilder,
    name: String,
    parentClass: String? = null,
    val isAndroid: Boolean,
    indent: String = ""
) {
    private val isAndroidCriticalEnabled = isAndroid && (context.extension as? NativeKtAndroidInterface)?.useAndroidCriticalNative ?: false
    private val indent1 = indent + "\t"
    private val indent2 = indent1 + "\t"

    init {
        builder.apply {
            val private = if(!isAndroid) "private " else ""
            val parentClass = if(parentClass != null) ": $parentClass" else ""

            append("$indent${private}class $name(libraryPath: String)$parentClass {")

            if(isAndroid) {
                append("\n${indent1}companion object {")
                printFunctions(indent2, isStatic = true)
                val utils = printUtilFunctions(indent2)
                append("\n${indent1}}")
                printInit(utils)
            } else {
                val utilsContent = StringBuilder()
                val utils = utilsContent.printUtilFunctions(indent2)

                if(utilsContent.isNotEmpty()) {
                    append("\n${indent1}companion object {")
                    append(utilsContent)
                    append("\n${indent1}}")
                }
                printFunctions(indent1, isStatic = false)
                printInit(utils)
            }

            if(!isAndroid) builder.appendLine("""
                
                override fun _address(name: String): Long =
                    NativeKtUtils.findAddress(name)
            """.replaceIndent(indent1))

            builder.append("${indent}}")
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
            props["critical"] = if ((context.extension as NativeKtAndroidInterface).useAndroidCriticalNative)
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
        append("\n${indent1}}\n")
    }

    private fun StringBuilder.printFunctions(indent: String, isStatic: Boolean) {
        if(context.allOperations.isEmpty())
            return

        context.allOperations.forEachIndexed { i, function ->
            val isAndroidFastNative = isAndroidCriticalEnabled && function.isCritical() && !function.isAndroidCriticalCapable()
            val isAndroidCriticalNative = isAndroidCriticalEnabled && function.isCritical() && function.isAndroidCriticalCapable()

            val type = when {
                isAndroidCriticalNative && function.type.isInterface() -> ": Long"
                isAndroidCriticalNative && function.type.isEnum() -> ": Int"
                !function.type.isVoid() -> ": ${function.type.toKotlinType()}"
                else -> ""
            }

            val args = function.args.mapIndexed { i, it ->
                when {
                    isAndroidCriticalNative && it.type.isInterface() -> "${it.kname}: Long"
                    isAndroidCriticalNative && it.type.isEnum() -> "${it.kname}: Int"
                    else -> "${it.kname}: ${it.type.toKotlinType()}"
                }
            }.joinToString()

            val modifiers = if(isStatic) {
                val androidCritical = when {
                    isAndroidFastNative -> "@FastNative "
                    isAndroidCriticalNative -> "@CriticalNative "
                    else -> ""
                }
                "@JvmStatic ${androidCritical}external"
            } else "external override"

            append("\n${indent}@Marker($i) $modifiers fun ${function.kname}($args)$type")
        }
        if(!isStatic)
            append("\n")
    }

    private fun StringBuilder.printUtilFunctions(indent: String): List<String> {
        // Util functions
        var i = context.allOperations.size
        val staticFunctions = arrayListOf<String>()

        // Enum
        context.castedEnums.forEach { enum ->
            if (enum in context.toKotlinDeclarationCasts) {
                append("\n$indent@Marker(${i++}) @JvmStatic fun cast_${enum.name.camelCase().lowercase()}(of: Int) = ${enum.kname}.entries[of]")
                staticFunctions += "cast_${enum.name.camelCase().lowercase()}"
            }
        }

        // Dictionaries
        context.castedDictionaries.forEach { dictionary ->
            val fields = dictionary.allFields()
            val name = dictionary.kname
            val lower = dictionary.name.camelCase().lowercase()

            if(dictionary in context.toKotlinDeclarationCasts) {
                val args = fields.joinToString { "${it.kname}: ${it.type.toKotlinType()}" }
                val argNames = fields.joinToString { it.kname }
                append("\n$indent@Marker(${i++}) @JvmStatic fun constructor_$lower($args) = ${dictionary.kname}($argNames)")
                staticFunctions += "constructor_$lower"
            }
            if(dictionary in context.toNativeDeclarationCasts) {
                fields.forEach {
                    val fieldLower = it.name.camelCase().lowercase()
                    append("\n$indent@Marker(${i++}) @JvmStatic fun field_${lower}_$fieldLower(of: $name) = of.${it.kname}")
                    staticFunctions += "field_${lower}_$fieldLower"
                }
            }
        }

        // Interfaces
        if(context.hasInterfaceCast) {
            if(context.hasInterfaceCastToNative) {
                append("\n$indent@Marker(${i++}) @JvmStatic fun interface_ptr(of: NativeKtRcObject) = of.rcPtr")
                staticFunctions += "interface_ptr"
            }
            context.castedInterfaces.forEach { inter ->
                if(inter in context.toNativeDeclarationCasts) {
                    append("\n$indent@Marker(${i++}) @JvmStatic fun constructor_${inter.name.camelCase().lowercase()}(ptr: Long) = ${inter.kname}(Unit, ptr)")
                    staticFunctions += "constructor_${inter.name.camelCase().lowercase()}"
                }
            }
        }

        // Callbacks
        if(context.hasCallbackCast) {
            append("\n$indent@Marker(${i++}) @JvmStatic fun callback_equals(c1: Any, c2: Any) = c1 == c2")
            append("\n$indent@Marker(${i++}) @JvmStatic fun callback_hashCode(c: Any) = c.hashCode()")

            staticFunctions += "callback_equals"
            staticFunctions += "callback_hashCode"

            context.castedCallbacks.forEach { callback ->
                val lower = callback.name.camelCase().lowercase()
                val args = buildList {
                    add("of: ${callback.kname}")
                    callback.args.mapTo(this){ "${it.kname}: ${it.type.toKotlinType()}" }
                }.joinToString()
                val argNames = callback.args.joinToString { it.kname }

                append("\n$indent@Marker(${i++}) @JvmStatic fun invoke_$lower($args) = of($argNames)")
                staticFunctions += "invoke_$lower"
            }
        }
        return staticFunctions
    }
}