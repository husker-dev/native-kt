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
        env = NdlEnv()
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

    val toNativeDeclarationCasts = toNativeTypes.map { it.declaration }.toHashSet()
    val toKotlinDeclarationCasts = toKotlinTypes.map { it.declaration }.toHashSet()
    val castedDeclarations = (toNativeDeclarationCasts + toKotlinDeclarationCasts).sortedBy { it.name }

    val hasNullableCastToKotlin = toKotlinTypes.any { it.isNullable }
    val hasNullableCastToNative = toNativeTypes.any { it.isNullable }
    val hasNullableCast = hasNullableCastToKotlin || hasNullableCastToNative

    val hasEnumToCastNative = toNativeTypes.any(ResolvedIdlType::isEnum)
    val hasEnumToCastKotlin = toKotlinTypes.any(ResolvedIdlType::isEnum)
    val hasEnumsCast = hasEnumToCastNative || hasEnumToCastKotlin
    val castedEnums = castedDeclarations.filterIsInstance<ResolvedIdlEnum>().sortedBy { it.name }

    val hasDictionaryCastToNative = toNativeTypes.any(ResolvedIdlType::isDictionary)
    val hasDictionaryCastToKotlin = toKotlinTypes.any(ResolvedIdlType::isDictionary)
    val hasDictionaryCast = hasDictionaryCastToNative || hasDictionaryCastToKotlin
    val castedDictionaries = castedDeclarations.filterIsInstance<ResolvedIdlDictionary>().sortedBy { it.name }

    val hasInterfaceCastToNative = toNativeTypes.any(ResolvedIdlType::isInterface)
    val hasInterfaceCastToKotlin = toKotlinTypes.any(ResolvedIdlType::isInterface)
    val hasInterfaceCast = hasInterfaceCastToNative || hasInterfaceCastToKotlin
    val castedInterfaces = castedDeclarations.filterIsInstance<ResolvedIdlInterface>().sortedBy { it.name }

    val hasCallbackCastToNative = toNativeTypes.any(ResolvedIdlType::isCallback)
    val hasCallbackCastToKotlin = toKotlinTypes.any(ResolvedIdlType::isCallback)
    val hasCallbackCast = hasCallbackCastToNative || hasCallbackCastToKotlin
    val castedCallbacks = castedDeclarations.filterIsInstance<ResolvedIdlCallbackFunction>().sortedBy { it.name }

    val hasStringCastToNative = toNativeTypes.any(ResolvedIdlType::isString)
    val hasStringCastToKotlin = toKotlinTypes.any(ResolvedIdlType::isString)
    val hasStringCast = hasStringCastToNative || hasStringCastToKotlin

    val hasCharArrayCastToNative = toNativeTypes.any(ResolvedIdlType::isCharArray)
    val hasCharArrayCastToKotlin = toKotlinTypes.any(ResolvedIdlType::isCharArray)
    val hasCharArrayCast = hasCharArrayCastToNative || hasCharArrayCastToKotlin

    val hasBooleanArrayCastToNative = toNativeTypes.any(ResolvedIdlType::isBooleanArray)
    val hasBooleanArrayCastToKotlin = toKotlinTypes.any(ResolvedIdlType::isBooleanArray)
    val hasBooleanArrayCast = hasBooleanArrayCastToNative || hasBooleanArrayCastToKotlin

    val hasByteArrayCastToNative = toNativeTypes.any(ResolvedIdlType::isByteArray)
    val hasByteArrayCastToKotlin = toKotlinTypes.any(ResolvedIdlType::isByteArray)
    val hasByteArrayCast = hasByteArrayCastToNative || hasByteArrayCastToKotlin

    val hasUByteArrayCastToNative = toNativeTypes.any(ResolvedIdlType::isUByteArray)
    val hasUByteArrayCastToKotlin = toKotlinTypes.any(ResolvedIdlType::isUByteArray)
    val hasUByteArrayCast = hasUByteArrayCastToNative || hasUByteArrayCastToKotlin

    val hasShortArrayCastToNative = toNativeTypes.any(ResolvedIdlType::isShortArray)
    val hasShortArrayCastToKotlin = toKotlinTypes.any(ResolvedIdlType::isShortArray)
    val hasShortArrayCast = hasShortArrayCastToNative || hasShortArrayCastToKotlin

    val hasUShortArrayCastToNative = toNativeTypes.any(ResolvedIdlType::isUShortArray)
    val hasUShortArrayCastToKotlin = toKotlinTypes.any(ResolvedIdlType::isUShortArray)
    val hasUShortArrayCast = hasUShortArrayCastToNative || hasUShortArrayCastToKotlin

    val hasIntArrayCastToNative = toNativeTypes.any(ResolvedIdlType::isIntArray)
    val hasIntArrayCastToKotlin = toKotlinTypes.any(ResolvedIdlType::isIntArray)
    val hasIntArrayCast = hasIntArrayCastToNative || hasIntArrayCastToKotlin

    val hasUIntArrayCastToNative = toNativeTypes.any(ResolvedIdlType::isUIntArray)
    val hasUIntArrayCastToKotlin = toKotlinTypes.any(ResolvedIdlType::isUIntArray)
    val hasUIntArrayCast = hasUIntArrayCastToNative || hasUIntArrayCastToKotlin

    val hasLongArrayCastToNative = toNativeTypes.any(ResolvedIdlType::isLongArray)
    val hasLongArrayCastToKotlin = toKotlinTypes.any(ResolvedIdlType::isLongArray)
    val hasLongArrayCast = hasLongArrayCastToNative || hasLongArrayCastToKotlin

    val hasULongArrayCastToNative = toNativeTypes.any(ResolvedIdlType::isULongArray)
    val hasULongArrayCastToKotlin = toKotlinTypes.any(ResolvedIdlType::isULongArray)
    val hasULongArrayCast = hasULongArrayCastToNative || hasULongArrayCastToKotlin

    val hasFloatArrayCastToNative = toNativeTypes.any(ResolvedIdlType::isFloatArray)
    val hasFloatArrayCastToKotlin = toKotlinTypes.any(ResolvedIdlType::isFloatArray)
    val hasFloatArrayCast = hasFloatArrayCastToNative || hasFloatArrayCastToKotlin

    val hasDoubleArrayCastToNative = toNativeTypes.any(ResolvedIdlType::isDoubleArray)
    val hasDoubleArrayCastToKotlin = toKotlinTypes.any(ResolvedIdlType::isDoubleArray)
    val hasDoubleArrayCast = hasDoubleArrayCastToNative || hasDoubleArrayCastToKotlin

    val hasEnumArrayCastToNative = toNativeTypes.any(ResolvedIdlType::isEnumArray)
    val hasEnumArrayCastToKotlin = toKotlinTypes.any(ResolvedIdlType::isEnumArray)
    val hasEnumArrayCast = hasEnumArrayCastToNative || hasEnumArrayCastToKotlin

    val hasStringArrayCastToNative = toNativeTypes.any(ResolvedIdlType::isStringArray)
    val hasStringArrayCastToKotlin = toKotlinTypes.any(ResolvedIdlType::isStringArray)
    val hasStringArrayCast = hasStringArrayCastToNative || hasStringArrayCastToKotlin

    val usedObjectArrayCastToKotlin = toKotlinTypes
        .filter { it.isNonPrimitiveArray() && !it.isEnumArray() }
        .map { it.arrayTypeOrNull()!!.declaration }
    val usedObjectArrayCastToNative = toNativeTypes
        .filter { it.isNonPrimitiveArray() && !it.isEnumArray() }
        .map { it.arrayTypeOrNull()!!.declaration }
    val usedObjectArrayCast = usedObjectArrayCastToKotlin + usedObjectArrayCastToNative

    val hasObjectArraysCast = usedObjectArrayCast.isNotEmpty()

    val hasPrimitiveArrayCast = hasCharArrayCast || hasBooleanArrayCast || hasByteArrayCast ||
            hasUByteArrayCast || hasShortArrayCast || hasUShortArrayCast || hasIntArrayCast ||
            hasUIntArrayCast || hasLongArrayCast || hasULongArrayCast || hasFloatArrayCast || hasDoubleArrayCast

    var hasCriticalStringCast = allOperations.any {
        it.isCritical() && it.args.any { arg -> arg.type.isString() && !arg.type.isNullable }
    }
    var hasCriticalStringOptCast = allOperations.any {
        it.isCritical() && it.args.any { arg -> arg.type.isString() && arg.type.isNullable }
    }
    var hasCriticalArrayCast = allOperations.any {
        it.isCritical() && it.args.any { arg -> arg.type.isArray() && !arg.type.isNullable }
    }
    var hasCriticalArrayOptCast = allOperations.any {
        it.isCritical() && it.args.any { arg -> arg.type.isArray() && arg.type.isNullable }
    }

    val needsAllocFunctions = hasPrimitiveArrayCast || hasStringCast || hasEnumArrayCast ||
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

                        STRING, VOID -> Unit
                        BOOLEAN, CHAR, INT, UNSIGNED_INT, FLOAT,
                        DOUBLE, BYTE, UNSIGNED_BYTE, SHORT,
                        UNSIGNED_SHORT, LONG, UNSIGNED_LONG -> {
                            if(type.isNullable)
                                throw UnsupportedOperationException("Nullable primitives are not supported: $type")
                        }
                        LIST -> {
                            if(isInsideArray)
                                throw UnsupportedOperationException("Nested arrays are not supported: $type")
                            checkType(type.parameters[0], true)
                        }
                    }
                    is ResolvedIdlCallbackFunction -> {
                        if(isInsideArray)
                            throw UnsupportedOperationException("Callback arrays are not supported yet")
                    }
                    is ResolvedIdlEnum -> {
                        if(type.isNullable)
                            throw UnsupportedOperationException("Nullable enums are not supported: ${type.declaration.name}")
                    }
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

    // String
    if(context.hasStringCast) {
        addAll(listOf(
            "string_new", "string_data",
            "string_size", "string_length",
            "string_free"
        ))
    }

    // Primitive arrays
    buildList {
        if(context.hasCharArrayCast) add("char")
        if(context.hasBooleanArrayCast) add("boolean")
        if(context.hasByteArrayCast || context.hasUByteArrayCast || context.hasCriticalStringCast) add("byte")
        if(context.hasShortArrayCast || context.hasUShortArrayCast) add("short")
        if(context.hasIntArrayCast || context.hasUIntArrayCast || context.hasEnumArrayCast) add("int")
        if(context.hasLongArrayCast || context.hasULongArrayCast) add("long")
        if(context.hasFloatArrayCast) add("float")
        if(context.hasDoubleArrayCast) add("double")
    }.flatMapTo(this) {
        val name = "${it}array"
        listOf("${name}_new", "${name}_elements", "${name}_length", "${name}_free")
    }

    // Typed arrays
    buildList {
        (context.castedDictionaries + context.castedInterfaces)
            .mapTo(this) { it.name.camelCase().lowercase() }
        if(context.hasStringArrayCast)
            add("string")
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
        if(dictionary in context.toNativeDeclarationCasts)
            add("${name}_new")
        if(dictionary in context.toKotlinDeclarationCasts) {
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
    val interfaceTagAttribute = IdlExtendedAttribute.StringValue("__interface", name)
    val criticalAttribute = IdlExtendedAttribute.NoArgs("critical")

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
        isOptional = false, isVariadic = false, attributes = listOf(interfaceTagAttribute)
    )

    constructors.forEachIndexed { index, constructor ->
        add(ResolvedIdlOperation(
            name = "INTERFACE_CONSTRUCTOR",
            type = rawInterfaceType,
            args = constructor.args,
            isStatic = false,
            attributes = buildList {
                add(interfaceTagAttribute)
                add(IdlExtendedAttribute.IntegerValue("__interface_new", index))
                addAll(constructor.attributes)
            }
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
            attributes = buildList {
                add(interfaceTagAttribute)
                add(IdlExtendedAttribute.StringValue("__interface_fn", operation.name))
                addAll(operation.attributes)
            }
        ))
    }

    // free
    add(ResolvedIdlOperation(
        name = "INTERFACE_FREE",
        type = ResolvedIdlType.Void("void"),
        args = listOf(rawInterfaceArg),
        isStatic = false,
        attributes = listOf(interfaceTagAttribute, IdlExtendedAttribute.NoArgs("__interface_free"))
    ))

    // clone
    add(ResolvedIdlOperation(
        name = "INTERFACE_CLONE",
        type = rawInterfaceType,
        args = listOf(rawInterfaceArg),
        isStatic = false,
        attributes = listOf(interfaceTagAttribute, criticalAttribute, IdlExtendedAttribute.NoArgs("__interface_clone"))
    ))

    // address
    add(ResolvedIdlOperation(
        name = "INTERFACE_ADDRESS",
        type = longType,
        args = listOf(rawInterfaceArg),
        isStatic = false,
        attributes = listOf(interfaceTagAttribute, criticalAttribute, IdlExtendedAttribute.NoArgs("__interface_address"))
    ))
}