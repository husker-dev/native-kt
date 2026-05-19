package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.utils.allFields
import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.isCallback
import com.huskerdev.nativekt.utils.isDictionary
import com.huskerdev.nativekt.utils.printLabel
import com.huskerdev.nativekt.utils.toCDefType
import com.huskerdev.nativekt.utils.toCType
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlCallbackFunction
import com.huskerdev.webidl.resolver.ResolvedIdlDictionary
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import com.huskerdev.webidl.resolver.ResolvedIdlType
import org.gradle.internal.extensions.stdlib.capitalized
import java.io.File

class CEmscriptenPrinter(
    val idl: IdlResolver,
    target: File
) {
    init {
        val builder = StringBuilder()
        builder.append("""
            #include "api.h"
            #include <stdlib.h>
            #include <string.h>
            #include <emscripten/bind.h>
            
            using namespace emscripten;
            
            #define POINTER_FIELD(Name, SelfType, PointerType)	\
            optional_override([](const SelfType& s) -> int {	\
            	return (int)(intptr_t)s.Name;					\
            }),													\
            optional_override([](SelfType& s, int v) {			\
            	s.Name = (PointerType)(intptr_t)v;				\
            })
            
            #define K_ARRAY_DECL(Name, Type)							 \
            value_object<Name>(#Name)									 \
                .field("elements", POINTER_FIELD(elements, Name, Type))  \
                .field("size", &Name::size)							     \
                .field("releasable", &Name::releasable)               \
                .field("released", &Name::released);                  \

        """.trimIndent())

        if(idl.callbacks.isNotEmpty()) {
            printLabel(builder, "Callbacks")
            printCallbacks(builder, idl.callbacks.values)
        }

        printLabel(builder, "Functions")

        builder.append("""
            
            EMSCRIPTEN_BINDINGS(my_module) {
            
            	value_object<KString>("KString")
                    .field("data", POINTER_FIELD(data, KString, const char*))
                    .field("length", &KString::length)
                    .field("releasable", &KString::releasable)
                    .field("released", &KString::released);
                    
                K_ARRAY_DECL(KCharArray,	KChar*)
                K_ARRAY_DECL(KBooleanArray, KBoolean*)
                K_ARRAY_DECL(KByteArray,	KByte*)
                K_ARRAY_DECL(KShortArray,	KShort*)
                K_ARRAY_DECL(KIntArray,		KInt*)
                K_ARRAY_DECL(KLongArray,	KLong*)
                K_ARRAY_DECL(KFloatArray,	KFloat*)
                K_ARRAY_DECL(KDoubleArray,  KDouble*)
                K_ARRAY_DECL(KArray,		const void**)
            
        """.trimIndent())

        idl.dictionaries.values.forEach { printDictionary(builder, it) }

        if(idl.enums.isNotEmpty()) {
            idl.enums.values.forEach { enum ->
                builder.append("\n\tenum_<${enum.name}>(\"${enum.name}\", enum_value_type::number)\n\t\t")
                enum.elements.joinTo(builder, separator = "\n\t\t") {
                    ".value(\"$it\", ${enum.name.capitalized()}_$it)"
                }
                builder.append(";\n")
            }
            builder.append("\n")
        }

        if(idl.callbacks.isNotEmpty()) {
            builder.append("\tfunction(\"_setCallback\", &_setCallback);\n\n")
        }

        idl.globalOperators().forEach {
            if(it.hasPointers())
                printFunctionOverride(builder, it)
            else
                printFunctionSimple(builder, it)
        }

        builder.append("\n}")
        target.writeText(builder.toString())
    }

    private fun printDictionary(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        append("\n\tvalue_object<")
        append(dictionary.name)
        append(">(\"")
        append(dictionary.name)
        append("\")\n\t\t")

        dictionary.allFields().joinTo(builder, separator = "\n\t\t") {
            val link = if(it.type.isDictionary() || it.type.isCallback())
                "POINTER_FIELD(${it.name}, ${dictionary.name}, ${it.type.toCType()})"
            else "&${dictionary.name}::${it.name}"
            ".field(\"${it.name}\", $link)"
        }
        append(";\n")
    }

    private fun printCallbacks(builder: StringBuilder, callbacks: Collection<ResolvedIdlCallbackFunction>) = builder.apply {
        val valNames = callbacks.associateWith { "_callback${it.name}" }
        val invokeNames = callbacks.associateWith { "_invoke${it.name}" }

        // Fields
        append("\n")
        callbacks.forEach {
            append("val ${valNames[it]} = val::undefined();\n")
        }

        // invoke functions
        callbacks.forEach { callback ->
            append("\nstatic ")
            append(callback.type.toCDefType())
            append(" ")
            append(invokeNames[callback])
            append("(")

            (
                listOf("${callback.name}* _c") +
                callback.args.map { "${it.type.toCDefType()} ${it.name}" }
            ).joinTo(builder)

            append(") {\n\t")

            if(callback.type !is ResolvedIdlType.Void)
                append("return ")
            if(callback.type.isPointer())
                append("(${callback.type.toCDefType()})")

            val args = listOf("(intptr_t)_c") + callback.args.map {
                if(it.type.isPointer())
                    "(intptr_t)${it.name}"
                else it.name
            }

            append("${valNames[callback]}(${args.joinToString()})")

            if(callback.type.isPointer())
                append(".as<intptr_t>()")
            else
                append(".as<${callback.type.toCDefType()}>()")

            append(";\n}\n")
        }

        // setter
        append("\nstatic intptr_t _setCallback(int index, val value) {\n")
        append("\tswitch (index) {\n")
        callbacks.forEachIndexed { index, callback ->
            append("\t\tcase $index:\n")
            append("\t\t\t${valNames[callback]} = value;\n")
            append("\t\t\treturn (intptr_t)${invokeNames[callback]};\n")
        }
        append("\t}\n")
        append("\treturn 0;\n}\n")
    }

    private fun printFunctionSimple(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\tfunction(\"${function.name}\", &${function.name});\n")
    }

    private fun printFunctionOverride(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\tfunction(\"")
        append(function.name)
        append("\", optional_override([](")

        function.args.joinTo(builder) {
            "${it.type.toCType(callbackAsPtr = true, dictionaryAsPtr = true)} ${it.name}"
        }
        append(")")

        if(function.type !is ResolvedIdlType.Void) {
            append(" -> ")
            append(function.type.toCType(callbackAsPtr = true, dictionaryAsPtr = true))
        }
        append(" {\n\t\t")

        if(function.type !is ResolvedIdlType.Void)
            append("return ")
        if(function.type.isPointer())
            append("(intptr_t)")

        val args = function.args.joinToString {
            if(it.type.isPointer())
                "(${(it.type as ResolvedIdlType.Default).declaration.name}*)${it.name}"
            else it.name
        }

        append("${function.name}($args);\n")
        append("\t}));\n")
    }

    private fun ResolvedIdlType.isPointer() =
        isCallback() || isDictionary()

    private fun ResolvedIdlOperation.hasPointers() =
        type.isPointer() || args.any { it.type.isPointer() }
}