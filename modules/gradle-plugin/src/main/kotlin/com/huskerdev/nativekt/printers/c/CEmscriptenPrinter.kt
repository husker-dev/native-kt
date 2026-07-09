package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.isVoid
import com.huskerdev.nativekt.utils.snakeCase
import com.huskerdev.nativekt.utils.toCType
import com.huskerdev.nativekt.utils.toOperations
import com.huskerdev.nativekt.utils.upperCamelCase
import com.huskerdev.webidl.resolver.IdlResolver
import java.io.File

class CEmscriptenPrinter(
    val idl: IdlResolver,
    target: File,
    val jsMangle: Map<String, String>,
    val classPath: String,
    val moduleName: String
) {
    init {
        val builder = StringBuilder()
        builder.append("#include \"api.h\"\n")
        builder.append("#include <emscripten.h>\n")

        builder.append("""
            
            EMSCRIPTEN_KEEPALIVE void ${jsMangle["KArray_free"]}(const KArray* self, void (*free_op)(void*)) {
                ${mangle("KArray_free")}(self, free_op);
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
            *idl.dictionaries.values.map { it.name.upperCamelCase() }.toTypedArray()
        ).forEach { name ->
            builder.append("""
                
                EMSCRIPTEN_KEEPALIVE void ${jsMangle["${name}_free"]}($name* self) {
                    ${mangle("${name}_free")}(self);
                }
                
                EMSCRIPTEN_KEEPALIVE void* ${jsMangle["${name}_free_addr"]}() {
                    return (void*) &${mangle("${name}_free")};
                }
                
            """.trimIndent())
        }

        listOf(
            *idl.globalOperators().toTypedArray(),
            *idl.interfaces.values.flatMap { it.toOperations() }.toTypedArray()
        ).forEach { function ->
            val name = function.name.snakeCase()
            val type = function.type.toCType()
            val args = function.args.joinToString {
                "${it.type.toCType()} ${it.name.snakeCase()}"
            }
            val argNames = function.args.joinToString { it.name.snakeCase() }

            builder.append("""
                
                EMSCRIPTEN_KEEPALIVE $type ${jsMangle[name]}($args) {
                    ${if(function.type.isVoid()) "" else "return "}${mangle(name)}($argNames);
                }
                
            """.trimIndent())
        }

        target.writeText(builder.toString())
    }

    private fun mangle(name: String) =
        com.huskerdev.nativekt.utils.mangle(classPath, moduleName, name)
}