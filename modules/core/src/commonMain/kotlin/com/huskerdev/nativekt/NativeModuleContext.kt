package com.huskerdev.nativekt

import com.huskerdev.nativekt.NativeModuleContext.CommandExecutor
import com.huskerdev.nativekt.printers.c.cname
import com.huskerdev.nativekt.serialzation.ContextSerializer
import com.huskerdev.nativekt.serialzation.nativektJson
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.*
import com.huskerdev.webidl.parser.*
import com.huskerdev.webidl.resolver.*
import com.huskerdev.webidl.resolver.WebIDLBuiltinKind.*
import io.github.vinceglb.filekit.*
import kotlinx.serialization.*


/**
 * A storage class for information about the native module.
 * Contains the module, NDL, and some helper functions for analyzing the NDL tree.
 */
@Serializable(with = ContextSerializer::class)
class NativeModuleContext(
    val configuration: NativeKtConfiguration,
    val module: NativeProject,
    val buildDir: PlatformFile,
    var executor: CommandExecutor?,
    var logger: Logger?,
    validate: Boolean = true
) {
    companion object {
        fun deserialize(
            json: String,
            executor: CommandExecutor,
            logger: Logger
        ) = nativektJson.decodeFromString<NativeModuleContext>(json).also {
            it.executor = executor
            it.logger = logger
        }
    }

    private val idl = WebIDL.resolve(
        text = module.resolveNdlFile().readSync(),
        env = NdlEnv
    )

    val debug = configuration.debug

    val classPath = module.resolveClassPath()
    val moduleName = module.name
    val buildSystem = module.buildSystem

    val language = buildSystem.language

    val dictionaries = idl.dictionaries.values.sortedWith { d1, d2 ->
        when {
            d1 == d2.implements -> -1
            d1.implements == d2 -> 1
            else -> d1.name.compareTo(d2.name)
        }
    }

    val interfaces = idl.interfaces.values.sortedBy { it.name }
    val callbacks = idl.callbacks.values.sortedBy { it.name }
    val enums = idl.enums.values.sortedBy { it.name }

    val operations = idl.namespaces.values.flatMap { it.operations }

    val interfaceOperations = interfaces.associateWith { it.toOperations() }
    val allOperations = operations + interfaceOperations.values.flatten()

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

    val toNativeDeclaration = toNativeTypes.map { it.declaration }.toHashSet()
    val toKotlinDeclaration = toKotlinTypes.map { it.declaration }.toHashSet()
    val castedDeclarations = (toNativeDeclaration + toKotlinDeclaration).sortedBy { it.name }

    val hasNullableToKotlin = toKotlinTypes.any { it.isNullable }
    val hasNullableToNative = toNativeTypes.any { it.isNullable }
    val hasNullable = hasNullableToKotlin || hasNullableToNative

    val hasEnumToNative = toNativeTypes.any(ResolvedIdlType::isEnum)
    val hasEnumToKotlin = toKotlinTypes.any(ResolvedIdlType::isEnum)
    val hasEnums = hasEnumToNative || hasEnumToKotlin
    val castedEnums = castedDeclarations.filterIsInstance<ResolvedIdlEnum>().sortedBy { it.name }

    val hasDictionaryToNative = toNativeTypes.any(ResolvedIdlType::isDictionary)
    val hasDictionaryToKotlin = toKotlinTypes.any(ResolvedIdlType::isDictionary)
    val hasDictionary = hasDictionaryToNative || hasDictionaryToKotlin
    val castedDictionaries = castedDeclarations.filterIsInstance<ResolvedIdlDictionary>().sortedBy { it.name }

    val hasInterfaceToNative = toNativeTypes.any(ResolvedIdlType::isInterface)
    val hasInterfaceToKotlin = toKotlinTypes.any(ResolvedIdlType::isInterface)
    val hasInterface = hasInterfaceToNative || hasInterfaceToKotlin
    val castedInterfaces = castedDeclarations.filterIsInstance<ResolvedIdlInterface>().sortedBy { it.name }

    val hasCallbackToNative = toNativeTypes.any(ResolvedIdlType::isCallback)
    val hasCallbackToKotlin = toKotlinTypes.any(ResolvedIdlType::isCallback)
    val hasCallback = hasCallbackToNative || hasCallbackToKotlin
    val castedCallbacks = castedDeclarations.filterIsInstance<ResolvedIdlCallbackFunction>().sortedBy { it.name }

    val hasStringToNative = toNativeTypes.any(ResolvedIdlType::isString)
    val hasStringToKotlin = toKotlinTypes.any(ResolvedIdlType::isString)
    val hasString = hasStringToNative || hasStringToKotlin

    val hasCharNullableToNative = toNativeTypes.any { it.isChar(isNullable = true) }
    val hasCharNullableToKotlin = toKotlinTypes.any { it.isChar(isNullable = true) }
    val hasCharNullable = hasCharNullableToNative || hasCharNullableToKotlin
    val hasCharArrayToNative = toNativeTypes.any { it.isCharArray(parameterIsNullable = false) }
    val hasCharArrayToKotlin = toKotlinTypes.any { it.isCharArray(parameterIsNullable = false) }
    val hasCharArray = hasCharArrayToNative || hasCharArrayToKotlin
    val hasCharNullableArrayToNative = toNativeTypes.any { it.isCharArray(parameterIsNullable = true) }
    val hasCharNullableArrayToKotlin = toKotlinTypes.any { it.isCharArray(parameterIsNullable = true) }
    val hasCharNullableArray = hasCharNullableArrayToNative || hasCharNullableArrayToKotlin

    val hasBooleanNullableToNative = toNativeTypes.any { it.isBoolean(isNullable = true) }
    val hasBooleanNullableToKotlin = toKotlinTypes.any { it.isBoolean(isNullable = true) }
    val hasBooleanNullable = hasBooleanNullableToNative || hasBooleanNullableToKotlin
    val hasBooleanArrayToNative = toNativeTypes.any { it.isBooleanArray(parameterIsNullable = false) }
    val hasBooleanArrayToKotlin = toKotlinTypes.any { it.isBooleanArray(parameterIsNullable = false) }
    val hasBooleanArray = hasBooleanArrayToNative || hasBooleanArrayToKotlin
    val hasBooleanNullableArrayToNative = toNativeTypes.any { it.isBooleanArray(parameterIsNullable = true) }
    val hasBooleanNullableArrayToKotlin = toKotlinTypes.any { it.isBooleanArray(parameterIsNullable = true) }
    val hasBooleanNullableArray = hasBooleanNullableArrayToNative || hasBooleanNullableArrayToKotlin

    val hasByteNullableToNative = toNativeTypes.any { it.isByte(isNullable = true) }
    val hasByteNullableToKotlin = toKotlinTypes.any { it.isByte(isNullable = true) }
    val hasByteNullable = hasByteNullableToNative || hasByteNullableToKotlin
    val hasByteArrayToNative = toNativeTypes.any { it.isByteArray(parameterIsNullable = false) }
    val hasByteArrayToKotlin = toKotlinTypes.any { it.isByteArray(parameterIsNullable = false) }
    val hasByteArray = hasByteArrayToNative || hasByteArrayToKotlin
    val hasByteNullableArrayToNative = toNativeTypes.any { it.isByteArray(parameterIsNullable = true) }
    val hasByteNullableArrayToKotlin = toKotlinTypes.any { it.isByteArray(parameterIsNullable = true) }
    val hasByteNullableArray = hasByteNullableArrayToNative || hasByteNullableArrayToKotlin

    val hasUByteNullableToNative = toNativeTypes.any { it.isUByte(isNullable = true) }
    val hasUByteNullableToKotlin = toKotlinTypes.any { it.isUByte(isNullable = true) }
    val hasUByteNullable = hasUByteNullableToNative || hasUByteNullableToKotlin
    val hasUByteArrayToNative = toNativeTypes.any { it.isUByteArray(parameterIsNullable = false) }
    val hasUByteArrayToKotlin = toKotlinTypes.any { it.isUByteArray(parameterIsNullable = false) }
    val hasUByteArray = hasUByteArrayToNative || hasUByteArrayToKotlin
    val hasUByteNullableArrayToNative = toNativeTypes.any { it.isUByteArray(parameterIsNullable = true) }
    val hasUByteNullableArrayToKotlin = toKotlinTypes.any { it.isUByteArray(parameterIsNullable = true) }
    val hasUByteNullableArray = hasUByteNullableArrayToNative || hasUByteNullableArrayToKotlin

    val hasShortNullableToNative = toNativeTypes.any { it.isShort(isNullable = true) }
    val hasShortNullableToKotlin = toKotlinTypes.any { it.isShort(isNullable = true) }
    val hasShortNullable = hasShortNullableToNative || hasShortNullableToKotlin
    val hasShortArrayToNative = toNativeTypes.any { it.isShortArray(parameterIsNullable = false) }
    val hasShortArrayToKotlin = toKotlinTypes.any { it.isShortArray(parameterIsNullable = false) }
    val hasShortArray = hasShortArrayToNative || hasShortArrayToKotlin
    val hasShortNullableArrayToNative = toNativeTypes.any { it.isShortArray(parameterIsNullable = true) }
    val hasShortNullableArrayToKotlin = toKotlinTypes.any { it.isShortArray(parameterIsNullable = true) }
    val hasShortNullableArray = hasShortNullableArrayToNative || hasShortNullableArrayToKotlin

    val hasUShortNullableToNative = toNativeTypes.any { it.isUShort(isNullable = true) }
    val hasUShortNullableToKotlin = toKotlinTypes.any { it.isUShort(isNullable = true) }
    val hasUShortNullable = hasUShortNullableToNative || hasUShortNullableToKotlin
    val hasUShortArrayToNative = toNativeTypes.any { it.isUShortArray(parameterIsNullable = false) }
    val hasUShortArrayToKotlin = toKotlinTypes.any { it.isUShortArray(parameterIsNullable = false) }
    val hasUShortArray = hasUShortArrayToNative || hasUShortArrayToKotlin
    val hasUShortNullableArrayToNative = toNativeTypes.any { it.isUShortArray(parameterIsNullable = true) }
    val hasUShortNullableArrayToKotlin = toKotlinTypes.any { it.isUShortArray(parameterIsNullable = true) }
    val hasUShortNullableArray = hasUShortNullableArrayToNative || hasUShortNullableArrayToKotlin

    val hasIntNullableToNative = toNativeTypes.any { it.isInt(isNullable = true) }
    val hasIntNullableToKotlin = toKotlinTypes.any { it.isInt(isNullable = true) }
    val hasIntNullable = hasIntNullableToNative || hasIntNullableToKotlin
    val hasIntArrayToNative = toNativeTypes.any { it.isIntArray(parameterIsNullable = false) }
    val hasIntArrayToKotlin = toKotlinTypes.any { it.isIntArray(parameterIsNullable = false) }
    val hasIntArray = hasIntArrayToNative || hasIntArrayToKotlin
    val hasIntNullableArrayToNative = toNativeTypes.any { it.isIntArray(parameterIsNullable = true) }
    val hasIntNullableArrayToKotlin = toKotlinTypes.any { it.isIntArray(parameterIsNullable = true) }
    val hasIntNullableArray = hasIntNullableArrayToNative || hasIntNullableArrayToKotlin

    val hasUIntNullableToNative = toNativeTypes.any { it.isUInt(isNullable = true) }
    val hasUIntNullableToKotlin = toKotlinTypes.any { it.isUInt(isNullable = true) }
    val hasUIntNullable = hasUIntNullableToNative || hasUIntNullableToKotlin
    val hasUIntArrayToNative = toNativeTypes.any { it.isUIntArray(parameterIsNullable = false) }
    val hasUIntArrayToKotlin = toKotlinTypes.any { it.isUIntArray(parameterIsNullable = false) }
    val hasUIntArray = hasUIntArrayToNative || hasUIntArrayToKotlin
    val hasUIntNullableArrayToNative = toNativeTypes.any { it.isUIntArray(parameterIsNullable = true) }
    val hasUIntNullableArrayToKotlin = toKotlinTypes.any { it.isUIntArray(parameterIsNullable = true) }
    val hasUIntNullableArray = hasUIntNullableArrayToNative || hasUIntNullableArrayToKotlin

    val hasLongNullableToNative = toNativeTypes.any { it.isLong(isNullable = true) }
    val hasLongNullableToKotlin = toKotlinTypes.any { it.isLong(isNullable = true) }
    val hasLongNullable = hasLongNullableToNative || hasLongNullableToKotlin
    val hasLongArrayToNative = toNativeTypes.any { it.isLongArray(parameterIsNullable = false) }
    val hasLongArrayToKotlin = toKotlinTypes.any { it.isLongArray(parameterIsNullable = false) }
    val hasLongArray = hasLongArrayToNative || hasLongArrayToKotlin
    val hasLongNullableArrayToNative = toNativeTypes.any { it.isLongArray(parameterIsNullable = true) }
    val hasLongNullableArrayToKotlin = toKotlinTypes.any { it.isLongArray(parameterIsNullable = true) }
    val hasLongNullableArray = hasLongNullableArrayToNative || hasLongNullableArrayToKotlin

    val hasULongNullableToNative = toNativeTypes.any { it.isULong(isNullable = true) }
    val hasULongNullableToKotlin = toKotlinTypes.any { it.isULong(isNullable = true) }
    val hasULongNullable = hasULongNullableToNative || hasULongNullableToKotlin
    val hasULongArrayToNative = toNativeTypes.any { it.isULongArray(parameterIsNullable = false) }
    val hasULongArrayToKotlin = toKotlinTypes.any { it.isULongArray(parameterIsNullable = false) }
    val hasULongArray = hasULongArrayToNative || hasULongArrayToKotlin
    val hasULongNullableArrayToNative = toNativeTypes.any { it.isULongArray(parameterIsNullable = true) }
    val hasULongNullableArrayToKotlin = toKotlinTypes.any { it.isULongArray(parameterIsNullable = true) }
    val hasULongNullableArray = hasULongNullableArrayToNative || hasULongNullableArrayToKotlin

    val hasFloatNullableToNative = toNativeTypes.any { it.isFloat(isNullable = true) }
    val hasFloatNullableToKotlin = toKotlinTypes.any { it.isFloat(isNullable = true) }
    val hasFloatNullable = hasFloatNullableToNative || hasFloatNullableToKotlin
    val hasFloatArrayToNative = toNativeTypes.any { it.isFloatArray(parameterIsNullable = false) }
    val hasFloatArrayToKotlin = toKotlinTypes.any { it.isFloatArray(parameterIsNullable = false) }
    val hasFloatArray = hasFloatArrayToNative || hasFloatArrayToKotlin
    val hasFloatNullableArrayToNative = toNativeTypes.any { it.isFloatArray(parameterIsNullable = true) }
    val hasFloatNullableArrayToKotlin = toKotlinTypes.any { it.isFloatArray(parameterIsNullable = true) }
    val hasFloatNullableArray = hasFloatNullableArrayToNative || hasFloatNullableArrayToKotlin

    val hasDoubleNullableToNative = toNativeTypes.any { it.isDouble(isNullable = true) }
    val hasDoubleNullableToKotlin = toKotlinTypes.any { it.isDouble(isNullable = true) }
    val hasDoubleNullable = hasDoubleNullableToNative || hasDoubleNullableToKotlin
    val hasDoubleArrayToNative = toNativeTypes.any { it.isDoubleArray(parameterIsNullable = false) }
    val hasDoubleArrayToKotlin = toKotlinTypes.any { it.isDoubleArray(parameterIsNullable = false) }
    val hasDoubleArray = hasDoubleArrayToNative || hasDoubleArrayToKotlin
    val hasDoubleNullableArrayToNative = toNativeTypes.any { it.isDoubleArray(parameterIsNullable = true) }
    val hasDoubleNullableArrayToKotlin = toKotlinTypes.any { it.isDoubleArray(parameterIsNullable = true) }
    val hasDoubleNullableArray = hasDoubleNullableArrayToNative || hasDoubleNullableArrayToKotlin

    val hasEnumNullableToNative = toNativeTypes.any { it.isEnum(isNullable = true) }
    val hasEnumNullableToKotlin = toKotlinTypes.any { it.isEnum(isNullable = true) }
    val hasEnumNullable = hasEnumNullableToNative || hasEnumNullableToKotlin
    val hasEnumArrayToNative = toNativeTypes.any { it.isEnumArray(parameterIsNullable = false) }
    val hasEnumArrayToKotlin = toKotlinTypes.any { it.isEnumArray(parameterIsNullable = false) }
    val hasEnumArray = hasEnumArrayToNative || hasEnumArrayToKotlin
    val hasEnumNullableArrayToNative = toNativeTypes.any { it.isEnumArray(parameterIsNullable = true) }
    val hasEnumNullableArrayToKotlin = toKotlinTypes.any { it.isEnumArray(parameterIsNullable = true) }
    val hasEnumNullableArray = hasEnumNullableArrayToNative || hasEnumNullableArrayToKotlin

    val hasStringArrayToNative = toNativeTypes.any(ResolvedIdlType::isStringArray)
    val hasStringArrayToKotlin = toKotlinTypes.any(ResolvedIdlType::isStringArray)
    val hasStringArray = hasStringArrayToNative || hasStringArrayToKotlin

    val usedObjectArrayToKotlin = toKotlinTypes
        .filter { it.isNonPrimitiveArray() && !it.isEnumArray() }
        .map { it.arrayTypeOrNull()!!.declaration }
    val usedObjectArrayToNative = toNativeTypes
        .filter { it.isNonPrimitiveArray() && !it.isEnumArray() }
        .map { it.arrayTypeOrNull()!!.declaration }
    val usedObjectArray = usedObjectArrayToKotlin + usedObjectArrayToNative

    val hasObjectArrays = usedObjectArray.isNotEmpty()

    val hasPrimitiveArray = hasCharArray || hasBooleanArray || hasByteArray ||
            hasUByteArray || hasShortArray || hasUShortArray || hasIntArray ||
            hasUIntArray || hasLongArray || hasULongArray || hasFloatArray || hasDoubleArray

    val hasPrimitiveNullableArray = hasCharNullableArray || hasBooleanNullableArray || hasByteNullableArray ||
            hasUByteNullableArray || hasShortNullableArray || hasUShortNullableArray || hasIntNullableArray ||
            hasUIntNullableArray || hasLongNullableArray || hasULongNullableArray || hasFloatNullableArray || hasDoubleNullableArray

    val hasPrimitiveNullable = hasCharNullable || hasBooleanNullable || hasByteNullable ||
            hasUByteNullable || hasShortNullable || hasUShortNullable || hasIntNullable ||
            hasUIntNullable || hasLongNullable || hasULongNullable || hasFloatNullable || hasDoubleNullable

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
            hasPrimitiveNullable || hasEnumNullable ||
            criticalOperations.flatMap { it.args }
                .any { it.type.isReleasable() }

    val allFields = dictionaries.associateWith { it.collectAllFields() }

    val jsMangle = createJsMangleMap(this)

    init {
        if(validate) {
            if (!module.name.matches("^[a-zA-Z_][a-zA-Z0-9_]*$".toRegex()))
                throw Exception("Native module '${module.name}' name contains an unsupported characters.\nAvailable: a-z, A-Z, underscore, digits")

            if (module.name.startsWith("_") || module.name.endsWith("_"))
                throw Exception("Native module '${module.name}' name must not begin or end with an underscore.")

            if (!module.resolveClassPath().matches("^([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*$".toRegex()))
                throw Exception("The classpath of the native module '${module.name}' must comply with Kotlin standards.")

            validateIDL()
        }
    }

    fun mangle(funcName: String, isInternal: Boolean = true): String =
        "nativekt" +
                "_${classPath.split(".").joinToString("_") { it.lowercase() }}" +
                "_${moduleName.snakeCase()}" +
                "_${if(isInternal) "_" else ""}$funcName"

    fun validateIDL() {
        fun checkType(type: ResolvedIdlType, isInsideArray: Boolean = false) {
            when(type) {
                is ResolvedIdlType.Union ->
                    throw UnsupportedOperationException("Union types are not supported: $type")
                is ResolvedIdlType.Default -> when(val declaration = type.declaration) {
                    is BuiltinIdlDeclaration -> when(declaration.kind) {
                        ANY, MUTABLE_LIST, MAP, PROMISE, USV_STRING, BIG_INT,
                        UNRESTRICTED_FLOAT, UNRESTRICTED_DOUBLE, BYTE_SEQUENCE,
                        OBJECT -> throw UnsupportedOperationException("Unsupported type: ${declaration.kind}")
                        LIST -> {
                            if(isInsideArray)
                                throw UnsupportedOperationException("Nested arrays are not supported: $type")
                            checkType(type.parameters[0], true)
                        }
                        STRING, VOID,
                        BOOLEAN, CHAR, INT, UNSIGNED_INT, FLOAT,
                        DOUBLE, BYTE, UNSIGNED_BYTE, SHORT,
                        UNSIGNED_SHORT, LONG, UNSIGNED_LONG -> Unit
                    }
                    is ResolvedIdlCallbackFunction -> {
                        if(isInsideArray)
                            throw UnsupportedOperationException("Callback arrays are not supported yet")
                    }
                    is ResolvedIdlEnum,
                    is ResolvedIdlInterface,
                    is ResolvedIdlDictionary,
                    is ResolvedIdlNamespace,
                    is ResolvedIdlTypeDef -> Unit // ok
                }
                is ResolvedIdlType.Void -> Unit // ok
            }
        }
        fun checkName(name: String) {
            if(name.startsWith("_"))
                throw UnsupportedOperationException("Identifiers cannot begin with an underscore: $name")
        }
        fun checkField(field: ResolvedIdlField) {
            checkType(field.type)
            checkName(field.name)
        }
        fun checkOperation(operation: ResolvedIdlOperation) {
            checkType(operation.type)
            checkName(operation.name)
            operation.args.forEach { checkField(it) }
        }

        idl.namespaces.values.forEach { namespace ->
            if(namespace.name != "global")
                throw UnsupportedOperationException("Only 'global' namespace is supported yet")
            namespace.operations.forEach { checkOperation(it) }
        }

        idl.callbacks.values.forEach { callback ->
            checkType(callback.type)
            checkName(callback.name)
            callback.args.forEach { checkField(it) }
        }

        idl.dictionaries.values.forEach { dictionary ->
            checkName(dictionary.name)
            dictionary.fields.forEach { checkField(it) }
        }

        idl.enums.values.forEach { enum ->
            checkName(enum.name)
            if(enum.elements.isEmpty())
                throw UnsupportedOperationException("Use of empty enum '${enum.name}'")
            enum.elements.forEach { checkName(it) }
        }

        allOperations.forEach { operation ->
            val isInterfaceConstructor = operation.isInterfaceOperationConstructor()
            val isInterfaceOperation = operation.isInterfaceOperation()

            if(operation.isCritical() && !operation.isCriticalCapable())
                throw UnsupportedOperationException("Operation '${operation.name}' is not critical capable")

            checkType(operation.type)
            checkName(operation.name)
            operation.args.forEachIndexed { i, it ->
                if(i == 0 && isInterfaceOperation && !isInterfaceConstructor)
                    return@forEach
                checkField(it)
            }
        }
    }

    fun execute(
        command: String,
        workingDir: PlatformFile? = null,
        silent: Boolean = false,
        errAsStd: Boolean = false,
        environment: Map<String, String>? = null
    ) = executor?.exec(this, command, workingDir, silent, errAsStd, environment)
        ?: throw NullPointerException("Executor is null")

    fun serialize() = nativektJson.encodeToString<NativeModuleContext>(this)

    interface CommandExecutor {
        fun exec(
            context: NativeModuleContext,
            command: String,
            workingDir: PlatformFile? = null,
            silent: Boolean = false,
            errAsStd: Boolean = false,
            environment: Map<String, String>? = null
        ): String
    }
    interface Logger {
        fun error(text: String)
        fun info(text: String)
    }
}



/**
 * Creates a NdlContext and validates the configuration and NDL file.
 */
fun createContext(
    buildDir: PlatformFile,
    configuration: NativeKtConfiguration,
    module: NativeProject,
    executor: CommandExecutor,
    logger: NativeModuleContext.Logger
): NativeModuleContext? {
    if(!module.resolveNdlFile().exists())
        return null

    return NativeModuleContext(
        configuration = configuration,
        module = module,
        buildDir = buildDir,
        executor = executor,
        logger = logger
    )
}

private fun createJsMangleMap(context: NativeModuleContext) = buildList {
    if(context.needsAllocFunctions) {
        add("alloc")
        add("dealloc")
    }

    // Boxed primitives
    if(context.hasPrimitiveNullable || context.hasEnumNullable) {
        listOf(
            "char" to context.hasCharNullable,
            "boolean" to context.hasBooleanNullable,
            "byte" to (context.hasByteNullable || context.hasUByteNullable),
            "short" to (context.hasShortNullable || context.hasUShortNullable),
            "int" to (context.hasIntNullable || context.hasUIntNullable || context.hasEnumNullable),
            "long" to (context.hasLongNullable || context.hasULongNullable),
            "float" to context.hasFloatNullable,
            "double" to context.hasDoubleNullable
        ).forEach { (name, hasCast) ->
            if(!hasCast) return@forEach
            addAll(listOf(
                "${name}_new",
                "${name}_get"
            ))
        }
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
        (context.castedDictionaries + context.castedInterfaces)
            .mapTo(this) { it.name.camelCase().lowercase() }

        if(context.hasStringArray) add("string")
        if(context.hasCharNullableArray)    add("char")
        if(context.hasBooleanNullableArray) add("boolean")
        if(context.hasByteNullableArray || context.hasUByteNullableArray)    add("byte")
        if(context.hasShortNullableArray || context.hasUShortNullableArray)  add("short")
        if(context.hasIntNullableArray || context.hasUIntNullableArray || context.hasEnumNullableArray) add("int")
        if(context.hasLongNullableArray || context.hasULongNullableArray)    add("long")
        if(context.hasFloatNullableArray)   add("float")
        if(context.hasDoubleNullableArray)  add("double")
    }.flatMapTo(this) {
        listOf(
            "array_${it}_new", "array_${it}_length",
            "array_${it}_push", "array_${it}_get",
            "array_${it}_free"
        )
    }
    // Dictionaries
    context.castedDictionaries.forEach { dictionary ->
        val name = dictionary.name.camelCase().lowercase()
        if(dictionary in context.toNativeDeclaration)
            add("${name}_new")
        if(dictionary in context.toKotlinDeclaration) {
            add("${name}_free")
            context.allFields[dictionary]!!.mapTo(this) {
                "${name}__${it.name.camelCase().lowercase()}"
            }
        }
    }

    // Callbacks
    context.castedCallbacks.flatMapTo(this) { callback ->
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

fun ResolvedIdlInterface.toOperations() = buildList {
    val interfaceTagAttribute = IdlExtendedAttribute.StringValue(IdlName("__interface"), name)
    val criticalAttribute = IdlExtendedAttribute.NoArgs(IdlName("critical"))

    val longType = ResolvedIdlType.Default(
        BuiltinIdlDeclaration("long", LONG),
        emptyList(),
        false
    )
    val rawInterfaceType = ResolvedIdlType.Default(
        this@toOperations,
        listOf(longType),
        false
    )
    val rawInterfaceArg = ResolvedIdlField.Argument(
        "_self", rawInterfaceType, null,
        isOptional = false, isVariadic = false, attributes = IdlAttributes(listOf(interfaceTagAttribute))
    )

    constructors.forEachIndexed { index, constructor ->
        add(ResolvedIdlOperation(
            name = "INTERFACE_CONSTRUCTOR",
            type = rawInterfaceType,
            args = constructor.args,
            isStatic = false,
            attributes = IdlAttributes(buildList {
                add(interfaceTagAttribute)
                add(IdlExtendedAttribute.IntegerValue(IdlName("__interface_new"), index))
                addAll(constructor.attributes ?: emptyList())
            })
        ))
    }
    operations.forEach { operation ->
        add(ResolvedIdlOperation(
            name = "INTERFACE_FUNCTION",
            type = operation.type,
            args = buildList {
                add(rawInterfaceArg)
                addAll(operation.args)
            },
            isStatic = false,
            attributes = IdlAttributes(buildList {
                add(interfaceTagAttribute)
                add(IdlExtendedAttribute.StringValue(IdlName("__interface_fn"), operation.name))
                addAll(operation.attributes ?: emptyList())
            })
        ))
    }

    // free
    add(ResolvedIdlOperation(
        name = "INTERFACE_FREE",
        type = ResolvedIdlType.Void("void"),
        args = listOf(rawInterfaceArg),
        isStatic = false,
        attributes = IdlAttributes(listOf(interfaceTagAttribute, IdlExtendedAttribute.NoArgs(IdlName(("__interface_free")))))
    ))

    // clone
    add(ResolvedIdlOperation(
        name = "INTERFACE_CLONE",
        type = rawInterfaceType,
        args = listOf(rawInterfaceArg),
        isStatic = false,
        attributes = IdlAttributes(listOf(interfaceTagAttribute, criticalAttribute, IdlExtendedAttribute.NoArgs(IdlName("__interface_clone"))))
    ))

    // address
    add(ResolvedIdlOperation(
        name = "INTERFACE_ADDRESS",
        type = longType,
        args = listOf(rawInterfaceArg),
        isStatic = false,
        attributes = IdlAttributes(listOf(interfaceTagAttribute, criticalAttribute, IdlExtendedAttribute.NoArgs(IdlName("__interface_address"))))
    ))
}