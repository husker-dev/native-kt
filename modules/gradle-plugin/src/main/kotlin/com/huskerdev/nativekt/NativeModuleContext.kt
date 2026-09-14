package com.huskerdev.nativekt

import com.huskerdev.nativekt.plugin.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.ResolvedIdlCallbackFunction
import com.huskerdev.webidl.resolver.ResolvedIdlDictionary
import com.huskerdev.webidl.resolver.ResolvedIdlEnum
import com.huskerdev.webidl.resolver.ResolvedIdlInterface
import com.huskerdev.webidl.resolver.ResolvedIdlType
import java.io.*


/**
 * A storage class for information about the native module.
 * Contains the module, NDL, and some helper functions for analyzing the NDL tree.
 */
class NativeModuleContext(
    val extension: NativeKtCommonInterface,
    val module: NativeProject,
    val nativesBuildDir: File,
    val srcGenDir: File
): Serializable {

    private val idl = module.idl().also { validateIDL(it) }

    val debug = extension.debug

    val classPath = module.classPath
    val moduleName = module.name
    val buildSystem = module.buildSystem

    val language = buildSystem.language

    val dictionaries = idl.dictionaries.values
    val interfaces = idl.interfaces.values
    val callbacks = idl.callbacks.values
    val enums = idl.enums.values

    val allOperations = idl.allOperations()
    val globalOperations = idl.globalOperators()

    val criticalOperations = allOperations.filter { it.isCritical() }

    /**
     * Types that needs to be cast into native one.
     * Calculated from entry points (operation arguments)
     */
    val toNativeTypes: Set<ResolvedIdlType.Default> = buildSet {
        fun addType(type: ResolvedIdlType) {
            if(type !is ResolvedIdlType.Default || type in this)
                return
            add(type)
            type.parameters.forEach { addType(it) }

            when {
                type.isCallback() -> addType((type.declaration as ResolvedIdlCallbackFunction).type)
                type.isDictionary() -> {
                    val dictionary = type.declaration as ResolvedIdlDictionary
                    dictionary.fields.forEach { addType(it.type) }
                    dictionary.implements?.let { addType(ResolvedIdlType.Default(it, emptyList(), false)) }
                }
                type.isArray() -> addType(type.arrayTypeOrNull()!!)
            }
        }
        allOperations.forEach { op ->
            op.args.forEach { addType(it.type) }
        }
    }

    /**
     * Types that needs to be cast into kotlin one.
     * Calculated from entry points (operation returns)
     */
    val toKotlinTypes: Set<ResolvedIdlType.Default> = buildSet {
        fun addType(type: ResolvedIdlType) {
            if(type !is ResolvedIdlType.Default || type in this)
                return
            add(type)
            type.parameters.forEach { addType(it) }

            when {
                type.isCallback() -> addType((type.declaration as ResolvedIdlCallbackFunction).type)
                type.isDictionary() -> {
                    val dictionary = type.declaration as ResolvedIdlDictionary
                    dictionary.fields.forEach { addType(it.type) }
                    dictionary.implements?.let { addType(ResolvedIdlType.Default(it, emptyList(), false)) }
                }
                type.isArray() -> addType(type.arrayTypeOrNull()!!)
            }
        }
        allOperations.forEach { op ->
            addType(op.type)
        }
    }

    val usedTypes = toNativeTypes + toKotlinTypes

    val toNativeDeclarations = toNativeTypes.map { it.declaration }.toHashSet()
    val toKotlinDeclarations = toKotlinTypes.map { it.declaration }.toHashSet()
    val usedDeclarations = toNativeDeclarations + toKotlinDeclarations

    val hasNullableToKotlin = toKotlinTypes.any { it.isNullable }
    val hasNullableToNative = toNativeTypes.any { it.isNullable }
    val hasNullable = hasNullableToKotlin || hasNullableToNative

    val hasAnyLong = usedTypes.any { it.isLong() || it.isULong() }

    val hasEnumToNativeCast = toNativeTypes.any(ResolvedIdlType::isEnum)
    val hasEnumToKotlinCast = toKotlinTypes.any(ResolvedIdlType::isEnum)
    val hasEnums = hasEnumToNativeCast || hasEnumToKotlinCast
    val usedEnums = usedDeclarations.filterIsInstance<ResolvedIdlEnum>().sortedBy { it.name }

    val hasDictionaryToNativeCast = toNativeTypes.any(ResolvedIdlType::isDictionary)
    val hasDictionaryToKotlinCast = toKotlinTypes.any(ResolvedIdlType::isDictionary)
    val hasDictionaries = hasDictionaryToNativeCast || hasDictionaryToKotlinCast
    val usedDictionaries = usedDeclarations.filterIsInstance<ResolvedIdlDictionary>().sortedWith { d1, d2 ->
        when {
            d1 == d2.implements -> -1
            d1.implements == d2 -> 1
            else -> d1.name.compareTo(d2.name)
        }
    }

    val hasInterfaceToNativeCast = toNativeTypes.any(ResolvedIdlType::isInterface)
    val hasInterfaceToKotlinCast = toKotlinTypes.any(ResolvedIdlType::isInterface)
    val hasInterfaces = hasInterfaceToNativeCast || hasInterfaceToKotlinCast
    val usedInterfaces = usedDeclarations.filterIsInstance<ResolvedIdlInterface>().sortedBy { it.name }

    val hasCallbackToNativeCast = toNativeTypes.any(ResolvedIdlType::isCallback)
    val hasCallbackToKotlinCast = toKotlinTypes.any(ResolvedIdlType::isCallback)
    val hasCallbacks = hasCallbackToNativeCast || hasCallbackToKotlinCast
    val usedCallbacks = usedDeclarations.filterIsInstance<ResolvedIdlCallbackFunction>().sortedBy { it.name }

    val hasStringToNativeCast = toNativeTypes.any(ResolvedIdlType::isString)
    val hasStringToKotlinCast = toKotlinTypes.any(ResolvedIdlType::isString)
    val hasString = hasStringToNativeCast || hasStringToKotlinCast

    val hasCharArrayToNativeCast = toNativeTypes.any(ResolvedIdlType::isCharArray)
    val hasCharArrayToKotlinCast = toKotlinTypes.any(ResolvedIdlType::isCharArray)
    val hasCharArray = hasCharArrayToNativeCast || hasCharArrayToKotlinCast

    val hasBooleanArrayToNativeCast = toNativeTypes.any(ResolvedIdlType::isBooleanArray)
    val hasBooleanArrayToKotlinCast = toKotlinTypes.any(ResolvedIdlType::isBooleanArray)
    val hasBooleanArray = hasBooleanArrayToNativeCast || hasBooleanArrayToKotlinCast

    val hasByteArrayToNativeCast = toNativeTypes.any(ResolvedIdlType::isByteArray)
    val hasByteArrayToKotlinCast = toKotlinTypes.any(ResolvedIdlType::isByteArray)
    val hasByteArray = hasByteArrayToNativeCast || hasByteArrayToKotlinCast

    val hasUByteArrayToNativeCast = toNativeTypes.any(ResolvedIdlType::isUByteArray)
    val hasUByteArrayToKotlinCast = toKotlinTypes.any(ResolvedIdlType::isUByteArray)
    val hasUByteArray = hasUByteArrayToNativeCast || hasUByteArrayToKotlinCast

    val hasShortArrayToNativeCast = toNativeTypes.any(ResolvedIdlType::isShortArray)
    val hasShortArrayToKotlinCast = toKotlinTypes.any(ResolvedIdlType::isShortArray)
    val hasShortArray = hasShortArrayToNativeCast || hasShortArrayToKotlinCast

    val hasUShortArrayToNativeCast = toNativeTypes.any(ResolvedIdlType::isUShortArray)
    val hasUShortArrayToKotlinCast = toKotlinTypes.any(ResolvedIdlType::isUShortArray)
    val hasUShortArray = hasUShortArrayToNativeCast || hasUShortArrayToKotlinCast

    val hasIntArrayToNativeCast = toNativeTypes.any(ResolvedIdlType::isIntArray)
    val hasIntArrayToKotlinCast = toKotlinTypes.any(ResolvedIdlType::isIntArray)
    val hasIntArray = hasIntArrayToNativeCast || hasIntArrayToKotlinCast

    val hasUIntArrayToNativeCast = toNativeTypes.any(ResolvedIdlType::isUIntArray)
    val hasUIntArrayToKotlinCast = toKotlinTypes.any(ResolvedIdlType::isUIntArray)
    val hasUIntArray = hasUIntArrayToNativeCast || hasUIntArrayToKotlinCast

    val hasLongArrayToNativeCast = toNativeTypes.any(ResolvedIdlType::isLongArray)
    val hasLongArrayToKotlinCast = toKotlinTypes.any(ResolvedIdlType::isLongArray)
    val hasLongArray = hasLongArrayToNativeCast || hasLongArrayToKotlinCast

    val hasULongArrayToNativeCast = toNativeTypes.any(ResolvedIdlType::isULongArray)
    val hasULongArrayToKotlinCast = toKotlinTypes.any(ResolvedIdlType::isULongArray)
    val hasULongArray = hasULongArrayToNativeCast || hasULongArrayToKotlinCast

    val hasFloatArrayToNativeCast = toNativeTypes.any(ResolvedIdlType::isFloatArray)
    val hasFloatArrayToKotlinCast = toKotlinTypes.any(ResolvedIdlType::isFloatArray)
    val hasFloatArray = hasFloatArrayToNativeCast || hasFloatArrayToKotlinCast

    val hasDoubleArrayToNativeCast = toNativeTypes.any(ResolvedIdlType::isDoubleArray)
    val hasDoubleArrayToKotlinCast = toKotlinTypes.any(ResolvedIdlType::isDoubleArray)
    val hasDoubleArray = hasDoubleArrayToNativeCast || hasDoubleArrayToKotlinCast

    val hasEnumArrayToNativeCast = toNativeTypes.any(ResolvedIdlType::isEnumArray)
    val hasEnumArrayToKotlinCast = toKotlinTypes.any(ResolvedIdlType::isEnumArray)
    val hasEnumArray = hasEnumArrayToNativeCast || hasEnumArrayToKotlinCast

    val hasStringArrayToNativeCast = toNativeTypes.any(ResolvedIdlType::isStringArray)
    val hasStringArrayToKotlinCast = toKotlinTypes.any(ResolvedIdlType::isStringArray)
    val hasStringArray = hasStringArrayToNativeCast || hasStringArrayToKotlinCast

    val usedObjectArrayToKotlinCast = toKotlinTypes.filter {
        it.isArray() && !it.isEnumArray() && !it.arrayTypeOrNull()!!.isPrimitive()
    }.map { it.arrayTypeOrNull()!!.declaration }
    val usedObjectArrayToNativeCast = toNativeTypes.filter {
        it.isArray() && !it.isEnumArray() && !it.arrayTypeOrNull()!!.isPrimitive()
    }.map { it.arrayTypeOrNull()!!.declaration }
    val usedObjectArrayCast = usedObjectArrayToKotlinCast + usedObjectArrayToNativeCast

    val hasObjectArrays = usedObjectArrayCast.isNotEmpty()

    val hasPrimitiveArray = hasCharArray || hasBooleanArray || hasByteArray ||
            hasUByteArray || hasShortArray || hasUShortArray || hasIntArray ||
            hasUIntArray || hasLongArray || hasULongArray || hasFloatArray || hasDoubleArray

    var hasCriticalString = allOperations.any {
        it.isCritical() && it.args.any { arg -> arg.type.isString() && !arg.type.isNullable }
    }
    var hasCriticalStringOpt = allOperations.any {
        it.isCritical() && it.args.any { arg -> arg.type.isString() && arg.type.isNullable }
    }
    var hasCriticalArray = allOperations.any {
        it.isCritical() && it.args.any { arg -> arg.type.isArray() && !arg.type.isNullable }
    }
    var hasCriticalArrayOpt = allOperations.any {
        it.isCritical() && it.args.any { arg -> arg.type.isArray() && arg.type.isNullable }
    }

    val needsAllocFunctions = hasPrimitiveArray || hasString || hasEnumArray ||
            criticalOperations.flatMap { it.args }
                .any { it.type.isReleasable() }

    val allFields = dictionaries.associateWith { it.allFields() }

    val jsMangle = createJsMangleMap(this)


    fun mangle(name: String) =
        mangle(classPath, moduleName, "_$name")

    // =====================
    //     Serialization
    // =====================

    @Throws(ObjectStreamException::class)
    private fun writeReplace(): Any = Proxy(this)

    @Throws(InvalidObjectException::class)
    @Suppress("unused")
    private fun readObject(stream: ObjectInputStream?) {
        throw InvalidObjectException("Proxy required")
    }

    private class Proxy(real: NativeModuleContext): Serializable {
        // Extract from Gradle wrapped object
        private val extension = when(val ext = real.extension) {
            is NativeKtMultiplatformExtension -> ext.impl
            is NativeKtJvmExtension -> ext.impl
            is NativeKtJsExtension -> ext.impl
            else -> ext
        }
        private val module = real.module
        private val nativesBuildDir = real.nativesBuildDir
        private val srcGenDir = real.srcGenDir

        @Throws(ObjectStreamException::class)
        fun readResolve(): Any = NativeModuleContext(
            extension,
            module,
            nativesBuildDir,
            srcGenDir
        )
    }
}

/**
 * Creates a NdlContext and validates the configuration and NDL file.
 */
internal fun createContext(
    buildDir: File,
    extension: NativeKtCommonInterface,
    module: NativeProject
): NativeModuleContext? {
    if(!module.name.matches("^[a-zA-Z_][a-zA-Z0-9_]*$".toRegex()))
        throw Exception("Native module '${module.name}' name contains an unsupported characters.\nAvailable: a-z, A-Z, underscore, digits")

    if(module.name.startsWith("_") || module.name.endsWith("_"))
        throw Exception("Native module '${module.name}' name must not begin or end with an underscore.")

    if(!module.classPath.matches("^([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*$".toRegex()))
        throw Exception("The classpath of the native module '${module.name}' must comply with Kotlin standards.")

    if(!module.ndlFile().exists())
        return null

    return NativeModuleContext(
        extension = extension,
        module = module,
        nativesBuildDir = File(buildDir, "nativekt/${module.name}"),
        srcGenDir = File(buildDir, "generated/natives/${module.name}")
    )
}

private fun createJsMangleMap(context: NativeModuleContext) = buildList {
    if(context.needsAllocFunctions) {
        add("alloc")
        add("dealloc")
    }

    // String
    if(context.hasString) {
        addAll(listOf(
            "string_new", "string_data",
            "string_size", "string_length",
            "string_free"
        ))
    }

    // Primitive arrays
    buildList {
        if(context.hasCharArray) add("char")
        if(context.hasBooleanArray) add("boolean")
        if(context.hasByteArray || context.hasUByteArray || context.hasCriticalString) add("byte")
        if(context.hasShortArray || context.hasUShortArray) add("short")
        if(context.hasIntArray || context.hasUIntArray || context.hasEnumArray) add("int")
        if(context.hasLongArray || context.hasULongArray) add("long")
        if(context.hasFloatArray) add("float")
        if(context.hasDoubleArray) add("double")
    }.flatMapTo(this) {
        val name = "${it}array"
        listOf("${name}_new", "${name}_elements", "${name}_length", "${name}_free")
    }

    // Typed arrays
    buildList {
        (context.usedDictionaries + context.usedInterfaces)
            .mapTo(this) { it.name.camelCase().lowercase() }
        if(context.hasStringArray)
            add("string")
    }.flatMapTo(this) {
        listOf(
            "array_${it}_new", "array_${it}_length",
            "array_${it}_push", "array_${it}_get",
            "array_${it}_free"
        )
    }
    // Dictionaries
    context.usedDictionaries.forEach { dictionary ->
        val name = dictionary.name.camelCase().lowercase()
        if(dictionary in context.toNativeDeclarations)
            add("${name}_new")
        if(dictionary in context.toKotlinDeclarations) {
            add("${name}_free")
            context.allFields[dictionary]!!.mapTo(this) {
                "${name}__${it.name.camelCase().lowercase()}"
            }
        }
    }

    // Callbacks
    context.usedCallbacks.flatMapTo(this) { callback ->
        val name = callback.name.camelCase().lowercase()
        listOf("${name}_new", "${name}_id", "${name}_free")
    }

    // Operations
    context.allOperations.forEach { operation ->
        add(operation.cname)
    }
}.mapIndexed { index, name ->
    val sb = StringBuilder()
    var n = index
    do {
        val d = n % 52
        sb.insert(0, (if (d < 26) 'A'.code + d else 'a'.code + d - 26).toChar())
        n /= 52
    } while (n > 0)
    name to (sb.toString() + "_")
}.toMap()