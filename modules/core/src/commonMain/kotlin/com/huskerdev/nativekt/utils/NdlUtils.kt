@file:OptIn(ExperimentalContracts::class)

package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.NativeKtJsConfiguration
import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.TargetType
import com.huskerdev.webidl.*
import com.huskerdev.webidl.parser.*
import com.huskerdev.webidl.resolver.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun ResolvedIdlType.isReleasable(): Boolean =
    isArray() || isString() || isDictionary() || isCallback() || isInterface()

fun ResolvedIdlType.isRawInterface(): Boolean {
    contract {
        returns(true) implies(this@isRawInterface is ResolvedIdlType.Default)
    }
    return isInterface() && parameters.isNotEmpty()
}

fun <T> ResolvedIdlType.Default.arrayType(block: (type: ResolvedIdlType.Default) -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block(arrayTypeOrNull()!!)
}

private fun ResolvedIdlOperation.hasAttribute(name: String): Boolean =
    attributes.any { it.name.lowercase() == name }


internal fun ResolvedIdlOperation.getInterface(context: NativeModuleContext) =
    context.interfaces.firstOrNull { it.name == interfaceName() }

internal fun ResolvedIdlOperation.isInterfaceOperation() = hasAttribute("__interface")

internal fun ResolvedIdlOperation.interfaceName(): String = attributes
    .filterIsInstance<IdlExtendedAttribute.StringValue>()
    .first { it.name.lowercase() == "__interface" }
    .value

internal fun ResolvedIdlOperation.interfaceFunctionName(): String = attributes
    .filterIsInstance<IdlExtendedAttribute.StringValue>()
    .first { it.name.lowercase() == "__interface_fn" }
    .value

internal fun ResolvedIdlOperation.interfaceConstructorNameOrNull(): String? = attributes
    .filterIsInstance<IdlExtendedAttribute.IdentifierValue>()
    .firstOrNull { it.name.lowercase() == "name" }
    ?.identifier

internal fun ResolvedIdlOperation.interfaceConstructorIndex(): Int = attributes
    .filterIsInstance<IdlExtendedAttribute.IntegerValue>()
    .first { it.name.lowercase() == "__interface_new" }
    .value

internal fun ResolvedIdlOperation.isInterfaceOperationConstructor() = hasAttribute("__interface_new")
internal fun ResolvedIdlOperation.isInterfaceOperationFn() = hasAttribute("__interface_fn")
internal fun ResolvedIdlOperation.isInterfaceOperationFree() = hasAttribute("__interface_free")
internal fun ResolvedIdlOperation.isInterfaceOperationClone() = hasAttribute("__interface_clone")
internal fun ResolvedIdlOperation.isInterfaceOperationAddress() = hasAttribute("__interface_address")

internal fun ResolvedIdlOperation.isCritical(): Boolean = hasAttribute("critical")

internal fun ResolvedIdlOperation.isCriticalCapable(): Boolean =
    (type.isVoid() || type.isPrimitive() || type.isEnum() || type.isInterface()) &&
            args.all {
                it.type.isPrimitive() || it.type.isEnum()
                        || it.type.isString() || it.type.isInterface()
                        || it.type.isPrimitiveArray() || it.type.isEnumArray()
            }

// Same as default critical, but without array and string args
internal fun ResolvedIdlOperation.isAndroidCriticalCapable(): Boolean =
    !type.isArray() && !type.isString() && !type.isDictionary() &&
            args.all { !it.type.isArray() && !it.type.isString() }

internal fun ResolvedIdlEnum.defaultValue(): String? =
    attributes.filterIsInstance<IdlExtendedAttribute.IdentifierValue>()
        .firstOrNull { it.name == "default" }?.identifier

fun checkForLongTypes(
    context: NativeModuleContext,
    extension: NativeKtJsConfiguration,
    targetName: TargetType
) {
    if(extension.useJsBigInt)
        return

    // Collect long fields/operations

    val errors = arrayListOf<String>()

    fun ResolvedIdlType.isAnyLong(): Boolean =
        !isRawInterface() && (isLong() || isULong() || (this is ResolvedIdlType.Default && parameters.any { it.isAnyLong() }) )

    fun checkOperation(operation: ResolvedIdlOperation) {
        if(operation.type.isAnyLong())
            errors += "Function ${operation.name}: return type"
        operation.args.forEach {
            if(it.type.isAnyLong())
                errors += "Function ${operation.name}: argument ${it.name}"
        }
    }

    context.allOperations.forEach {
        if(it.isInterfaceOperationAddress())
            return@forEach
        checkOperation(it)
    }
    context.dictionaries.forEach { dictionary ->
        dictionary.fields.forEach { arg ->
            if(arg.type.isAnyLong())
                errors += "Dictionary ${dictionary.name}: field ${arg.name}"
        }
    }
    context.interfaces.forEach {
        it.operations.forEach(::checkOperation)
    }
    context.callbacks.forEach { callback ->
        if(callback.type.isAnyLong())
            errors += "Callback ${callback.name}: return type"
        callback.args.forEach {
            if(it.type.isAnyLong())
                errors += "Callback ${callback.name}: argument ${it.name}"
        }
    }

    // Print result

    if(errors.isNotEmpty()) {
        throw UnsupportedOperationException(buildString {
            appendLine("A Long type was detected in your .ndl file, but it is not enabled by the current Kotlin/JS configuration.")

            errors.forEach { append("\n- $it") }

            append("""
                
                
                To fix this issue:
                
                1. Make sure your Kotlin Multiplatform version is >= 2.2.20
                2. Set 'useJsBigInt = true' in the plugin configuration.
                3. Add the following compiler options to the Kotlin Multiplatform JS target:
                
                kotlin {
                    ${if(targetName == TargetType.JS) "js" else "wasmJs"} {
                        compilerOptions {
                            freeCompilerArgs.addAll(
                                "-Xes-long-as-bigint", 
                                "-XXLanguage:+JsAllowLongInExportedDeclarations"
                            )
                        }
                    }
                }
            """.trimIndent())
        })
    }
}