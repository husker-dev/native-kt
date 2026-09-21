package com.huskerdev.nativekt.printers.rust

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.c.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.*
import com.huskerdev.webidl.resolver.*
import io.github.vinceglb.filekit.*
import kotlinx.io.SystemLineSeparator

class RustPrinter(
    val context: NativeModuleContext,
    target: PlatformFile,
) {
    init {
        target.parent()!!.createDirectories()
        target.writeSync(buildString {
            if((context.buildSystem as BuildSystem.Cargo).printApi)
                printApi()
            printHeaderDef(context)
            printStringDef(context)
            printArraysDef(context)
            printCriticalFuncDef(context)
            printDictionaries()
            printCallbacks()
            printEnums()
            printFunctions()
        }.replace("\n", SystemLineSeparator))
    }

    private fun StringBuilder.printApi() {
        printLabel("API")

        if(context.interfaces.isNotEmpty())
            append("/*\n======================= Interfaces ========================= *\\")

        context.interfaces.forEach { inter ->
            val name = inter.rustName.replace("crate::", "")

            append("\n\npub struct $name")
            if(inter.fields.isEmpty())
                append(";")

            if(inter.operations.isNotEmpty()) {
                append("\n\nimpl $name {")

                inter.toOperations().forEach { operation ->
                    val args = operation.args.map {
                        val ref = if(it.type.isReleasable()) "&" else ""
                        "${it.rustName}: $ref${it.type.toRustType()}"
                    }
                    val type = if(!operation.type.isVoid())
                        " -> ${operation.type.toRustType()}"
                    else ""

                    when {
                        operation.isInterfaceOperationConstructor() -> {
                            append("\n\tfn ${operation.rustName}(${args.joinToString()}) -> Self {}")
                        }
                        operation.isInterfaceOperationFn() -> {
                            val args = buildList {
                                add("&self")
                                addAll(args.drop(1))
                            }.joinToString()

                            append("\n\tfn ${operation.rustName}($args)$type {}")
                        }
                    }
                }
                append("\n}")
            }
        }

        if(context.operations.isNotEmpty())
            append("\n\n======================= Functions =========================== *\\")

        context.operations.forEach { operation ->
            append("\n\npub fn ${operation.rustName}(")
            operation.args.joinTo(this, ",") {
                val ref = if(it.type.isReleasable()) "&" else ""
                "\n\t${it.rustName}: $ref${it.type.toRustType()}"
            }
            if(operation.args.isNotEmpty())
                append("\n")
            append(")")

            if(!operation.type.isVoid())
                append(" -> ${operation.type.toRustType()}")
            append(" {}")
        }
        append("\n\n=============================================================== */\n\n")
    }

    private fun StringBuilder.printDictionaries() {
        if(context.dictionaries.isEmpty())
            return

        printLabel("Structs")
        context.dictionaries.forEach { dictionary ->
            val name = dictionary.rustName
            val fields = context.allFields[dictionary]!!

            val funcNew = dictionary.subCFunc(context, "new")
            val funcFree = dictionary.subCFunc(context, "free")
            val funcNewJs = context.jsMangle["${name.camelCase().lowercase()}_new"]
            val funcFreeJs = context.jsMangle["${name.camelCase().lowercase()}_free"]

            // Struct
            append("""
                
                #[derive(Clone)]
                pub struct $name {
            """.trimIndent())
            fields.joinTo(this) {
                "\n\tpub ${it.rustName}: ${it.type.toRustType()}"
            }
            append("\n}\n")

            if(dictionary in context.castedDeclarations) {
                append("export_fn! {")

                if(dictionary in context.toNativeDeclarationCasts) {
                    // new
                    append("\n\tfn $funcNew(")
                    fields.joinTo(this) {
                        "${it.rustName}: ${it.type.toNativeRustType()}"
                    }

                    append(") -> *mut $name as $funcNewJs {")
                    append("\n\t\tinto_raw($name { ")

                    fields.joinTo(this) {
                        val key = it.rustName
                        val value = toRustType(it.type, it.rustName)
                        if (key != value)
                            "$key: $value"
                        else value
                    }
                    append(" })\n\t}")
                }
                if(dictionary in context.toKotlinDeclarationCasts) {
                    // free
                    append("\n\tfn $funcFree(of: *mut $name) -> () as $funcFreeJs { from_raw(of); }")

                    // field getters
                    fields.joinTo(this, separator = "") {
                        val funcName = dictionary.subFieldCFunc(context, it)
                        val funcNameJs = context.jsMangle["${name.camelCase().lowercase()}__${it.name.camelCase().lowercase()}"]

                        val type = it.type.toNativeRustType(ptrType = "const")
                        val call = "(*of).${it.rustName}"
                        val castedCall = when {
                            it.type.isEnum() -> "$call.clone()"
                            it.type.isNullable -> "(&$call).as_ref().map_or(core::ptr::null(), |it| it as $type)"
                            it.type.isReleasable() -> "&$call"
                            else -> call
                        }
                        "\n\tfn $funcName(of: *mut $name) -> $type as $funcNameJs { unsafe { $castedCall } }"
                    }
                }
                append("\n}\n")
            }
        }
    }

    private fun StringBuilder.printCallbacks() {
        if(context.callbacks.isEmpty())
            return
        printLabel("Callbacks")

        append($$"""
            
            macro_rules! impl_callback {
                ($name:ident, $invoke_type:ty, $invoke_body:item, 
            	$funcNew:ident, $funcNewJs:ident, 
            	$funcId:ident, $funcIdJs:ident,
            	$funcFree:ident, $funcFreeJs:ident) => {
                    pub struct $name {
            			id: isize,
            			invoke: $invoke_type,
            			equals: external_fn_type!(bool; isize, isize),
            			hash_code: i32,
            			free: external_fn_type!((); isize)
            		}
            		impl $name {
            			$invoke_body
            		}
            		impl Drop for $name {
                        fn drop(&mut self) { external_fn_call!((); self.free; self.id) }
                    }
                    impl Hash for $name {
                        fn hash<H: Hasher>(&self, state: &mut H) { self.hash_code.hash(state); }
                    }
                    impl PartialEq for $name {
                        fn eq(&self, other: &Self) -> bool { external_fn_call!(bool; self.equals; self.id, other.id) }
                    }
                    impl Eq for $name {}
            		
            		export_fn! {
            			fn $funcNew(
            				id: isize,
            				hash_code: i32,
            				invoke: $invoke_type,
            				equals: external_fn_type!(bool; isize, isize),
            				free: external_fn_type!((); isize)
            			) -> *mut Arc<$name> as $funcNewJs {
            				into_raw(Arc::new($name { id, invoke, equals, hash_code, free }))
            			}
            			fn $funcId(_self: *mut Arc<$name>) -> isize as $funcIdJs {
            				unsafe { (&*_self).id }
            			}
            			fn $funcFree(_self: *mut Arc<$name>) -> () as $funcFreeJs {
            				from_raw(_self);
            			}
            		}
                };
            }
            
        """.trimIndent())

        context.callbacks.forEach { callback ->
            val name = callback.rustName
            val lower = callback.name.camelCase().lowercase()
            val returnType = callback.type.toNativeRustType()

            append("""
                impl_callback!(
                    $name,
            """.trimIndent())

            // Invoke type
            buildList {
                add("isize")
                callback.args.mapTo(this) {
                    it.type.toNativeRustType()
                }
            }.joinTo(this, prefix = "\n\texternal_fn_type!($returnType; ", postfix = "),")

            // Invoke function head
            append("\n\tpub fn invoke")
            buildList {
                add("&self")
                callback.args.mapTo(this) {
                    "${it.rustName}: ${it.type.toRustType()}"
                }
            }.joinTo(this, prefix = "(", postfix = ")")
            if(!callback.type.isVoid())
                append(" -> ${callback.type.toRustType()}")

            // Invoke function body
            val call = buildList {
                add(if(callback.type.isEnum()) "enum $returnType" else returnType)
                add("self.invoke")
                add(buildList {
                    add("self.id")
                    callback.args.mapTo(this) {
                        toNativeType(it.type, it.rustName)
                    }
                }.joinToString())
            }.joinToString("; ", prefix = "external_fn_call!(", postfix = ")")
            append(" { ${toRustType(callback.type, call)} },")

            // func new/free
            append("\n\t${context.mangle("${lower}_new")}, ${context.jsMangle["${lower}_new"]},")
            append("\n\t${context.mangle("${lower}_id")}, ${context.jsMangle["${lower}_id"]},")
            append("\n\t${context.mangle("${lower}_free")}, ${context.jsMangle["${lower}_free"]}")

            append("\n);\n")
        }
    }

    private fun StringBuilder.printEnums() {
        if(context.enums.isEmpty())
            return
        printLabel("Enums")

        context.enums.forEach { enum ->
            val name = enum.rustName
            val defaultValue = enum.defaultValue()
            val defaultValueExt = if(defaultValue != null && defaultValue in enum.elements)
                ", Default" else ""

            append("""
                
                #[cfg_attr(target_arch = "wasm32", wasm_bindgen)]
                #[repr(C)]
                #[derive(PartialEq, Eq, Clone, Copy, Debug$defaultValueExt)]
                pub enum $name {
            """.trimIndent())

            enum.elements.mapIndexed { index, value ->
                if(value == defaultValue)
                    "\n\t#[default]\n\t$value = $index"
                else
                    "\n\t$value = $index"
            }.joinTo(this, ",")
            append("\n}\n")
        }
    }

    private fun StringBuilder.printFunctions() {
        if(context.allOperations.isEmpty())
            return

        printLabel("Functions")
        context.allOperations.forEach { operation ->
            val critical = operation.isCritical()
            val cname = operation.cnameMangled(context)
            val jsName = context.jsMangle[operation.cname]
            val rustName = operation.rustName
            val type = if(operation.isInterfaceOperationAddress())
                " -> usize"
            else " -> ${operation.type.toNativeRustType()}"

            val args = operation.args.joinToString {
                val name = it.rustName
                when {
                    critical && it.type.isString() -> "$name: *mut u8, _${name}_length: i32, _${name}_size: i32"
                    critical && it.type.isArray() -> "$name: *mut ${it.type.arrayTypeOrNull()!!.toRustType()}, _${name}_length: i32"
                    else -> "$name: ${it.type.toNativeRustType()}"
                }
            }

            val castedArgs = operation.args.map {
                val name = it.rustName
                (if (it.type.isReleasable()) "&" else "") + when {
                    critical && it.type.isString() ->
                        if(it.type.isNullable) "critical_string_opt($name, _${name}_length, _${name}_size)"
                        else "critical_string($name, _${name}_size)"
                    critical && it.type.isArray() ->
                        if(it.type.isNullable) "critical_array_opt($name, _${name}_length)"
                        else "critical_array($name, _${name}_length)"
                    else -> toRustType(it.type, name)
                }
            }

            val call = if (operation.isInterfaceOperation()) {
                val inter = operation.getInterface(context)!!
                val interName = inter.rustName
                val self = operation.args.getOrNull(0)?.rustName
                when {
                    operation.isInterfaceOperationConstructor() ->
                        "into_raw(Arc::new($interName::$rustName(${castedArgs.joinToString()})))"
                    operation.isInterfaceOperationFn() ->
                        toNativeType(operation.type, "unsafe { &*$self }.$rustName(${castedArgs.drop(1).joinToString()})")
                    operation.isInterfaceOperationFree() ->
                        "from_raw($self);"
                    operation.isInterfaceOperationClone() ->
                        "into_raw(unsafe { &*$self }.clone())"
                    operation.isInterfaceOperationAddress() ->
                        "Arc::as_ptr(unsafe { &*$self }) as usize"
                    else -> throw UnsupportedOperationException()
                }
            } else toNativeType(operation.type, "crate::$rustName(${castedArgs.joinToString()})")

            // Print
            append("""
                
                export_fn!{ fn $cname($args)$type as $jsName {
                    $call
                }}
            """.trimIndent())
        }
    }

    private fun toNativeType(type: ResolvedIdlType, content: String): String = when {
        type.isVoid() || type.isPrimitive() || type.isEnum() -> content
        type.isPrimitive() -> content
        else -> if (type.isNullable) "obj_opt($content, into_raw)"
                else "into_raw($content)"
    }

    private fun toRustType(type: ResolvedIdlType, content: String): String = when {
        type.isVoid() || type.isPrimitive() || type.isEnum() -> content
        else -> if (type.isNullable) "ptr_opt($content, from_raw)"
                else "from_raw($content)"
    }
}