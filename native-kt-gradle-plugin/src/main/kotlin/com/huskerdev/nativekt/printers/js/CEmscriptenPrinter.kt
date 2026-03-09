package com.huskerdev.nativekt.printers.js

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlCallbackFunction
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import com.huskerdev.webidl.resolver.ResolvedIdlType
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
                .field("elements", POINTER_FIELD(elements, Name, Type*)) \
                .field("size", &Name::size);							 \

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
                    .field("length", &KString::length);
                    
                K_ARRAY_DECL(KCharArray, KChar)
                K_ARRAY_DECL(KBooleanArray, KBoolean)
                K_ARRAY_DECL(KByteArray, KByte)
                K_ARRAY_DECL(KShortArray, KShort)
                K_ARRAY_DECL(KIntArray, KInt)
                K_ARRAY_DECL(KLongArray, KLong)
                K_ARRAY_DECL(KFloatArray, KFloat)
                K_ARRAY_DECL(KDoubleArray, KDouble)
            
            
        """.trimIndent())

        if(idl.callbacks.isNotEmpty()) {
            builder.append("\tfunction(\"_setCallback\", &_setCallback);\n\n")
        }

        idl.globalOperators().forEach {
            if(it.hasPointers())
                printOverride(builder, it)
            else
                printSimple(builder, it)
        }

        builder.append("\n}")
        target.writeText(builder.toString())
    }

    private fun ResolvedIdlOperation.hasPointers() =
        type.isCallback() || args.any { it.type.isCallback() }

    private fun printSimple(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\tfunction(\"${function.name}\", &${function.name});\n")
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
            if(callback.type.isCallback())
                append("(${callback.type.toCDefType()})")

            val args = listOf("(intptr_t)_c") + callback.args.map {
                if(it.type.isCallback())
                    "(intptr_t)${it.name}"
                else it.name
            }

            append("${valNames[callback]}(${args.joinToString()})")

            if(callback.type.isCallback())
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

    private fun printOverride(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\tfunction(\"")
        append(function.name)
        append("\", optional_override([](")

        function.args.joinTo(builder) {
            "${it.type.toCType(callbackAsPtr = true)} ${it.name}"
        }
        append(")")

        if(function.type !is ResolvedIdlType.Void) {
            append(" -> ")
            append(function.type.toCType(callbackAsPtr = true))
        }
        append(" {\n\t\t")

        if(function.type !is ResolvedIdlType.Void)
            append("return ")
        if(function.type.isCallback())
            append("(intptr_t)")

        val args = function.args.joinToString {
            if(it.type.isCallback())
                "(${(it.type as ResolvedIdlType.Default).declaration.name}*)${it.name}"
            else it.name
        }

        append("${function.name}($args);\n")
        append("\t}));\n")
    }
}