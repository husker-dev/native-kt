package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.plugin.Language
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.IdlResolver
import java.io.File

class CEmscriptenPrinter(
    val idl: IdlResolver,
    target: File,
    language: Language,
    val jsMangle: Map<String, String>,
    val classPath: String,
    val moduleName: String
) {
    init {
        val builder = StringBuilder()

        val headerExtension = language.headerExtension ?: "h"

        builder.append("""
            #include "api.$headerExtension"
            #include <emscripten.h>
            
            #ifdef __cplusplus
            extern "C" {
            #endif
            
            EMSCRIPTEN_KEEPALIVE void ${jsMangle["karray_free"]}(KArray* self, void (*free_op)(void*)) {
                ${mangle("karray_free")}(self, free_op);
            }
            
        """.trimIndent())

        listOf(
            "KString",
            "KCharArray",
            "KBooleanArray",
            "KByteArray",
            "KShortArray",
            "KIntArray",
            "KLongArray",
            "KFloatArray",
            "KDoubleArray",
        ).forEach { name ->
            val lowerName = name.snakeCase()
            builder.append("""
                
                EMSCRIPTEN_KEEPALIVE void ${jsMangle["${lowerName}_free"]}($name* self) {
                    ${mangle("${lowerName}_free")}(self);
                }
                
                EMSCRIPTEN_KEEPALIVE void* ${jsMangle["${lowerName}_free_addr"]}() {
                    return (void*) &${mangle("${lowerName}_free")};
                }
                
            """.trimIndent())
        }

        idl.dictionaries.values.forEach {
            val name = it.name.lowercase()
            val funcFree = it.subCFunc(classPath, moduleName, "free")
            builder.append("""
                
                EMSCRIPTEN_KEEPALIVE void ${jsMangle["${name}_free"]}(${it.cname}* self) {
                    $funcFree(self);
                }
                
                EMSCRIPTEN_KEEPALIVE void* ${jsMangle["${name}_free_addr"]}() {
                    return (void*) &$funcFree;
                }
                
            """.trimIndent())
        }

        idl.allOperators().forEach { function ->
            val name = function.cname
            val mangledName = function.cnameMangled(classPath, moduleName)
            val type = function.type.toCType()
            val args = function.args.joinToString {
                "${it.type.toCType()} ${it.cname}"
            }
            val argNames = function.args.joinToString { it.cname }

            builder.append("""
                
                EMSCRIPTEN_KEEPALIVE $type ${jsMangle[name]}($args) {
                    ${if(function.type.isVoid()) "" else "return "}$mangledName($argNames);
                }
                
            """.trimIndent())
        }

        builder.append("""
            
            #ifdef __cplusplus
            }
            #endif
        """.trimIndent())
        target.writeText(builder.toString())
    }

    private fun mangle(name: String) =
        mangle(classPath, moduleName, "_$name")
}