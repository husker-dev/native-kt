@file:OptIn(ExperimentalUnsignedTypes::class)
@file:Suppress("unused", "JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package com.huskerdev.nativekt.jvm.foreign

import com.huskerdev.nativekt.jvm.NativeKtUtils
import com.huskerdev.nativekt.jvm.NativeKtUtils.addExports
import com.huskerdev.nativekt.jvm.foreign.ForeignUtils.Companion.callbacks
import com.huskerdev.nativekt.jvm.foreign.ForeignUtils.Companion.freeHandle
import com.huskerdev.nativekt.jvm.foreign.ForeignUtils.Companion.layoutArray
import com.huskerdev.nativekt.jvm.foreign.ForeignUtils.Companion.layoutCallback
import com.huskerdev.nativekt.jvm.foreign.ForeignUtils.Companion.layoutString
import com.huskerdev.nativekt.jvm.foreign.ForeignUtils.Companion.linker
import com.huskerdev.nativekt.jvm.foreign.ForeignUtils.Companion.mallocHandle
import com.huskerdev.nativekt.jvm.foreign.ForeignUtils.Companion.ptrHandle
import jdk.internal.foreign.MemorySessionImpl
import java.lang.foreign.*
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout.*
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiFunction
import java.util.function.Function

// Flags
const val FLAG_RELEASABLE = 1.toByte()

class ForeignUtils {
    companion object {
        init {
            if (NativeKtUtils.isAutoExportEnabled()) {
                addExports(
                    this::class.java.module,
                    "java.base",
                    arrayOf("jdk.internal.foreign")
                )
            }
        }

        internal val linker = Linker.nativeLinker()
        internal val lookup = MethodHandles.lookup()
        internal val ptrHandle = ADDRESS.arrayElementVarHandle()

        // Layouts

        internal val layoutString = CStructLayout(
            ADDRESS,
            JAVA_LONG,
            JAVA_INT,
            JAVA_BYTE
        )

        internal val layoutArray = layoutString // Same as string (for now)

        internal val layoutCallback = CStructLayout(
            JAVA_BYTE,
            ADDRESS,
            ADDRESS,
            ADDRESS,
            ADDRESS,
            ADDRESS
        )

        // Callbacks

        internal val callbacks = ConcurrentHashMap<Long, Any>()

        internal val callbackClone = lookup.upcall(
            "callbackClone",
            MethodType.methodType(MemorySegment::class.java, MemorySegment::class.java),
            FunctionDescriptor.of(ADDRESS, ADDRESS)
        )

        internal val callbackEquals = lookup.upcall(
            "callbackEquals",
            MethodType.methodType(Boolean::class.java, MemorySegment::class.java, MemorySegment::class.java),
            FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS, ADDRESS)
        )

        internal val callbackHashCode = lookup.upcall(
            "callbackHashCode",
            MethodType.methodType(Int::class.java, MemorySegment::class.java),
            FunctionDescriptor.of(JAVA_INT, ADDRESS)
        )

        internal val callbackFree = lookup.upcall(
            "callbackFree",
            MethodType.methodType(Void::class.javaPrimitiveType, MemorySegment::class.java),
            FunctionDescriptor.ofVoid(ADDRESS)
        )

        // Malloc function
        internal val mallocHandle = linker.downcallHandle(
            linker.defaultLookup().find("malloc").orElseThrow(),
            FunctionDescriptor.of(ADDRESS, JAVA_LONG)
        )

        // Free function
        internal val freeHandle: MethodHandle = linker.downcallHandle(
            linker.defaultLookup().find("free").orElseThrow(),
            FunctionDescriptor.ofVoid(ADDRESS)
        )

        @JvmStatic
        private fun callbackClone(self: MemorySegment): MemorySegment {
            return malloc(layoutCallback.size).apply {
                set(JAVA_BYTE, layoutCallback[0], FLAG_RELEASABLE)
                set(
                    ADDRESS,
                    layoutCallback[1],
                    self.reinterpret(layoutCallback.size).get(ADDRESS, 8L)
                )
                set(ADDRESS, layoutCallback[2], ForeignUtils.Companion.callbackClone)
                set(ADDRESS, layoutCallback[3], ForeignUtils.Companion.callbackEquals)
                set(ADDRESS, layoutCallback[4], ForeignUtils.Companion.callbackHashCode)
                set(ADDRESS, layoutCallback[5], ForeignUtils.Companion.callbackFree)

                callbacks[address()] = callbacks[self.address()]!!
            }
        }

        @JvmStatic
        private fun callbackEquals(self: MemorySegment, obj: MemorySegment): Boolean =
            callbacks[self.address()] == callbacks[obj.address()]

        @JvmStatic
        private fun callbackHashCode(self: MemorySegment): Int =
            callbacks[self.address()].hashCode()

        @JvmStatic
        fun callbackFree(callback: MemorySegment) {
            val callback = callback.reinterpret(layoutCallback.size)
            callbacks.remove(callback.address())

            if ((callback.get(JAVA_INT, 0L) and FLAG_RELEASABLE.toInt()) == FLAG_RELEASABLE.toInt())
                free(callback)
        }
    }
}

fun malloc(size: Long): MemorySegment =
    (mallocHandle.invoke(size) as MemorySegment).reinterpret(size)

fun free(mem: MemorySegment) =
    freeHandle.invoke(mem) as Unit

// Lookup functions
fun address(lookup: SymbolLookup, name: String): MemorySegment =
    lookup.find(name).orElseThrow()

fun handle(
    address: MemorySegment,
    isCritical: Boolean,
    retType: MemoryLayout?,
    vararg argTypes: MemoryLayout
): MethodHandle {
    val function = if (retType == null)
        FunctionDescriptor.ofVoid(*argTypes)
    else FunctionDescriptor.of(retType, *argTypes)

    return if (isCritical)
        linker.downcallHandle(address, function, Linker.Option.critical(true))
    else linker.downcallHandle(address, function)
}

fun lookup(
    lookup: SymbolLookup,
    name: String,
    isCritical: Boolean,
    retType: MemoryLayout?,
    vararg argTypes: MemoryLayout
) = handle(address(lookup, name), isCritical, retType, *argTypes)

fun MethodHandles.Lookup.upcall(
    name: String,
    methodType: MethodType,
    functionDescriptor: FunctionDescriptor?
): MemorySegment = linker.upcallStub(
    findStatic(lookupClass(), name, methodType),
    functionDescriptor,
    Arena.global()
)

// String
fun toNativeKStringOnArena(arena: Arena, of: String?): MemorySegment {
    of ?: return MemorySegment.NULL
    val stringMem = arena.allocateFrom(of)

    return arena.allocate(layoutString.size).apply {
        set(ADDRESS, layoutString[0], stringMem)
        set(JAVA_LONG, layoutString[1], stringMem.byteSize() - 1)
        set(JAVA_INT, layoutString[2], of.length)
        set(JAVA_BYTE, layoutString[3], 0.toByte())
    }
}

fun toNativeKString(of: String?): MemorySegment {
    of ?: return MemorySegment.NULL

    val bytes = of.toByteArray(StandardCharsets.UTF_8)
    val stringMem = malloc(bytes.size.toLong())
    MemorySegment.copy(bytes, 0, stringMem, JAVA_BYTE, 0, bytes.size)

    return malloc(layoutString.size).apply {
        set(ADDRESS, layoutString[0], stringMem)
        set(JAVA_LONG, layoutString[1], bytes.size.toLong())
        set(JAVA_INT, layoutString[2], of.length)
        set(JAVA_BYTE, layoutString[3], FLAG_RELEASABLE)
    }
}

fun toKotlinKString(struct: MemorySegment): String? {
    if (struct.address() == 0L) return null

    val struct = struct.reinterpret(layoutString.size)
    val data = struct.get(ADDRESS, layoutString[0])
    val size = struct.get(JAVA_LONG, layoutString[1]).toInt()

    val bytes = ByteArray(size)
    MemorySegment.copy(data.reinterpret(size.toLong()), JAVA_BYTE, 0, bytes, 0, size)
    return String(bytes, StandardCharsets.UTF_8)
}


// Arrays
private fun getLength(array: MemorySegment): Int =
    array.get(JAVA_INT, layoutArray[2])

private fun getSize(array: MemorySegment): Long =
    array.get(JAVA_LONG, layoutArray[1])

private fun getElements(array: MemorySegment): MemorySegment =
    array.get(ADDRESS, layoutArray[0]).reinterpret(getSize(array))

private fun fillArray(mem: MemorySegment, data: MemorySegment, length: Int, flag: Byte = 0) = mem.apply {
    set(ADDRESS, layoutArray[0], data)
    set(JAVA_LONG, layoutArray[1], data.byteSize())
    set(JAVA_INT, layoutArray[2], length)
    set(JAVA_BYTE, layoutArray[3], flag)
}

private fun <T : Any> readArray(struct: MemorySegment, array: (Int) -> T, layout: ValueLayout): T? {
    if (struct.address() == 0L)
        return null
    val struct = struct.reinterpret(layoutArray.size)
    val length = getLength(struct)
    val data = getElements(struct)
    val array = array(length)

    MemorySegment.copy(data, layout, 0L, array, 0, length)
    return array
}

private fun allocateFrom(elementLayout: ValueLayout, elements: Any, length: Int): MemorySegment {
    val allocated = malloc(elementLayout.byteSize() * length)
    MemorySegment.copy(elements, 0, allocated, elementLayout, 0, length)
    return allocated
}


// Array: char
fun toNativeKCharArrayDirect(arr: CharArray?): MemorySegment = arr?.run {
    MemorySegment.ofArray(arr)
} ?: MemorySegment.NULL

fun toNativeKCharArrayOnArena(arena: Arena, arr: CharArray?): MemorySegment = arr?.run {
    fillArray(
        mem = arena.allocate(layoutArray.size),
        data = arena.allocateFrom(JAVA_CHAR, *arr),
        length = arr.size
    )
} ?: MemorySegment.NULL

fun toNativeKCharArray(arr: CharArray?): MemorySegment = arr?.run {
    fillArray(
        mem = malloc(layoutArray.size),
        data = allocateFrom(JAVA_CHAR, arr, arr.size),
        length = arr.size,
        flag = FLAG_RELEASABLE
    )
} ?: MemorySegment.NULL

fun toKotlinKCharArray(struct: MemorySegment): CharArray? =
    readArray(struct, ::CharArray, JAVA_CHAR)


// Array: boolean
fun toNativeKBooleanArrayDirect(arr: BooleanArray?): MemorySegment = arr?.run {
    MemorySegment.ofArray(ByteArray(arr.size) { if (arr[it]) 1.toByte() else 0.toByte() })
} ?: MemorySegment.NULL

fun toNativeKBooleanArrayOnArena(arena: Arena, arr: BooleanArray?): MemorySegment = arr?.run {
    toNativeKByteArrayOnArena(arena, ByteArray(arr.size) { if (arr[it]) 1.toByte() else 0.toByte() })
} ?: MemorySegment.NULL

fun toNativeKBooleanArray(arr: BooleanArray?): MemorySegment = arr?.run {
    toNativeKByteArray(ByteArray(arr.size) { if (arr[it]) 1.toByte() else 0.toByte() })
} ?: MemorySegment.NULL

fun toKotlinKBooleanArray(struct: MemorySegment): BooleanArray? =
    toKotlinKByteArray(struct)?.run {
        BooleanArray(size) { this[it] == 1.toByte() }
    }


// Array: byte
fun toNativeKByteArrayDirect(arr: ByteArray?): MemorySegment = arr?.run {
    MemorySegment.ofArray(arr)
} ?: MemorySegment.NULL

fun toNativeKByteArrayOnArena(arena: Arena, arr: ByteArray?): MemorySegment = arr?.run {
    fillArray(
        mem = arena.allocate(layoutArray.size),
        data = arena.allocateFrom(JAVA_BYTE, *arr),
        length = arr.size
    )
} ?: MemorySegment.NULL

fun toNativeKByteArray(arr: ByteArray?): MemorySegment = arr?.run {
    fillArray(
        mem = malloc(layoutArray.size),
        data = allocateFrom(JAVA_BYTE, arr, arr.size),
        length = arr.size,
        flag = FLAG_RELEASABLE
    )
} ?: MemorySegment.NULL

fun toKotlinKByteArray(struct: MemorySegment): ByteArray? =
    readArray(struct, ::ByteArray, JAVA_BYTE)


// Array: short
fun toNativeKShortArrayDirect(arr: ShortArray?): MemorySegment = arr?.run {
    MemorySegment.ofArray(arr)
} ?: MemorySegment.NULL

fun toNativeKShortArrayOnArena(arena: Arena, arr: ShortArray?): MemorySegment = arr?.run {
    fillArray(
        mem = arena.allocate(layoutArray.size),
        data = arena.allocateFrom(JAVA_SHORT, *arr),
        length = arr.size
    )
} ?: MemorySegment.NULL

fun toNativeKShortArray(arr: ShortArray?): MemorySegment = arr?.run {
    fillArray(
        mem = malloc(layoutArray.size),
        data = allocateFrom(JAVA_SHORT, arr, arr.size),
        length = arr.size,
        flag = FLAG_RELEASABLE
    )
} ?: MemorySegment.NULL

fun toKotlinKShortArray(struct: MemorySegment): ShortArray? =
    readArray(struct, ::ShortArray, JAVA_SHORT)


// Array: int
fun toNativeKIntArrayDirect(arr: IntArray?): MemorySegment = arr?.run {
    MemorySegment.ofArray(arr)
} ?: MemorySegment.NULL

fun toNativeKIntArrayOnArena(arena: Arena, arr: IntArray?): MemorySegment = arr?.run {
    fillArray(
        mem = arena.allocate(layoutArray.size),
        data = arena.allocateFrom(JAVA_INT, *arr),
        length = arr.size
    )
} ?: MemorySegment.NULL

fun toNativeKIntArray(arr: IntArray?): MemorySegment = arr?.run {
    fillArray(
        mem = malloc(layoutArray.size),
        data = allocateFrom(JAVA_INT, arr, arr.size),
        length = arr.size,
        flag = FLAG_RELEASABLE
    )
} ?: MemorySegment.NULL

fun toKotlinKIntArray(struct: MemorySegment): IntArray? =
    readArray(struct, ::IntArray, JAVA_INT)


// Array: long
fun toNativeKLongArrayDirect(arr: LongArray?): MemorySegment = arr?.run {
    MemorySegment.ofArray(arr)
} ?: MemorySegment.NULL

fun toNativeKLongArrayOnArena(arena: Arena, arr: LongArray?): MemorySegment = arr?.run {
    fillArray(
        mem = arena.allocate(layoutArray.size),
        data = arena.allocateFrom(JAVA_LONG, *arr),
        length = arr.size
    )
} ?: MemorySegment.NULL

fun toNativeKLongArray(arr: LongArray?): MemorySegment = arr?.run {
    fillArray(
        mem = malloc(layoutArray.size),
        data = allocateFrom(JAVA_LONG, arr, arr.size),
        length = arr.size,
        flag = FLAG_RELEASABLE
    )
} ?: MemorySegment.NULL

fun toKotlinKLongArray(struct: MemorySegment): LongArray? =
    readArray(struct, ::LongArray, JAVA_LONG)


// Array: float
fun toNativeKFloatArrayDirect(arr: FloatArray?): MemorySegment = arr?.run {
    MemorySegment.ofArray(arr)
} ?: MemorySegment.NULL

fun toNativeKFloatArrayOnArena(arena: Arena, arr: FloatArray?): MemorySegment = arr?.run {
    fillArray(
        mem = arena.allocate(layoutArray.size),
        data = arena.allocateFrom(JAVA_FLOAT, *arr),
        length = arr.size
    )
} ?: MemorySegment.NULL

fun toNativeKFloatArray(arr: FloatArray?): MemorySegment = arr?.run {
    fillArray(
        mem = malloc(layoutArray.size),
        data = allocateFrom(JAVA_FLOAT, arr, arr.size),
        length = arr.size,
        flag = FLAG_RELEASABLE
    )
} ?: MemorySegment.NULL

fun toKotlinKFloatArray(struct: MemorySegment): FloatArray? =
    readArray(struct, ::FloatArray, JAVA_FLOAT)


// Array: double
fun toNativeKDoubleArrayDirect(arr: DoubleArray?): MemorySegment = arr?.run {
    MemorySegment.ofArray(arr)
} ?: MemorySegment.NULL

fun toNativeKDoubleArrayOnArena(arena: Arena, arr: DoubleArray?): MemorySegment = arr?.run {
    fillArray(
        mem = arena.allocate(layoutArray.size),
        data = arena.allocateFrom(JAVA_DOUBLE, *arr),
        length = arr.size
    )
} ?: MemorySegment.NULL

fun toNativeKDoubleArray(arr: DoubleArray?): MemorySegment = arr?.run {
    fillArray(
        mem = malloc(layoutArray.size),
        data = allocateFrom(JAVA_DOUBLE, arr, arr.size),
        length = arr.size,
        flag = FLAG_RELEASABLE
    )
} ?: MemorySegment.NULL

fun toKotlinKDoubleArray(struct: MemorySegment): DoubleArray? =
    readArray(struct, ::DoubleArray, JAVA_DOUBLE)


// Array: enum
fun <T : Enum<T>> toNativeEnumArrayDirect(arr: Array<T>?): MemorySegment = arr?.run {
    MemorySegment.ofArray(IntArray(arr.size) { arr[it].ordinal })
} ?: MemorySegment.NULL

fun <T : Enum<T>> toNativeEnumArrayOnArena(arena: Arena, arr: Array<T>?): MemorySegment = arr?.run {
    toNativeKIntArrayOnArena(arena, IntArray(arr.size) { arr[it].ordinal })
} ?: MemorySegment.NULL

fun <T : Enum<T>> toNativeEnumArray(arr: Array<T>?): MemorySegment = arr?.run {
    toNativeKIntArray(IntArray(arr.size) { arr[it].ordinal })
} ?: MemorySegment.NULL

@Suppress("unchecked_cast")
fun <T : Enum<T>> toKotlinEnumArray(
    struct: MemorySegment,
    enumClass: Class<T>
): Array<T>? {
    if (struct.address() == 0L) return null
    val ordinals = toKotlinKIntArray(struct)!!

    // Convert integers to enum values
    val enumConstants = enumClass.getEnumConstants()
    val result = java.lang.reflect.Array.newInstance(enumClass, ordinals.size) as Array<T>
    for (i in ordinals.indices)
        result[i] = enumConstants[ordinals[i]]
    return result
}

// Array: object
fun <T> toNativeKArrayOnArena(
    arena: Arena,
    arr: Array<T>?,
    cast: BiFunction<Arena, T, MemorySegment?>
): MemorySegment = arr?.run {
    val dataMem = arena.allocate(ADDRESS, arr.size.toLong())
    for (i in arr.indices)
        ptrHandle.set(dataMem, 0L, i.toLong(), cast.apply(arena, arr[i]))
    return fillArray(
        mem = arena.allocate(layoutArray.size),
        data = dataMem,
        length = arr.size
    )
} ?: MemorySegment.NULL

fun <T> toNativeKArray(
    arr: Array<T>?,
    cast: Function<T, MemorySegment>
): MemorySegment = arr?.run {
    val dataMem = malloc(ADDRESS.byteSize() * arr.size)
    for (i in arr.indices)
        ptrHandle.set(dataMem, 0L, i.toLong(), cast.apply(arr[i]))
    return fillArray(
        mem = malloc(layoutArray.size),
        data = dataMem,
        length = arr.size,
        flag = FLAG_RELEASABLE
    )
} ?: MemorySegment.NULL

@Suppress("unchecked_cast")
fun <T> toKotlinKArray(
    struct: MemorySegment,
    cast: Function<MemorySegment, T?>,
    elementClass: Class<*>
): Array<T>? {
    if (struct.address() == 0L) return null
    val struct = struct.reinterpret(layoutArray.size)
    val length = getLength(struct)

    val elementsPtr = getElements(struct).reinterpret(ADDRESS.byteSize() * length)

    val result = java.lang.reflect.Array.newInstance(elementClass, length) as Array<T>
    for (i in result.indices)
        result[i] = cast.apply(ptrHandle.get(elementsPtr, 0L, i.toLong()) as MemorySegment) as T
    return result
}


// Callbacks

fun createCallbackOnArena(
    arena: Arena,
    callback: Any?,
    upcall: MemorySegment
): MemorySegment = callback?.run {
    arena.allocate(layoutCallback.size).apply {
        set(JAVA_BYTE, layoutCallback[0], 0.toByte())
        set(ADDRESS, layoutCallback[1], upcall)
        set(ADDRESS, layoutCallback[2], ForeignUtils.Companion.callbackClone)
        set(ADDRESS, layoutCallback[3], ForeignUtils.Companion.callbackEquals)
        set(ADDRESS, layoutCallback[4], ForeignUtils.Companion.callbackHashCode)
        set(ADDRESS, layoutCallback[5], ForeignUtils.Companion.callbackFree)

        callbacks[address()] = callback

        // Remove callback from list when Arena is closed
        MemorySessionImpl.toMemorySession(arena).addCloseAction {
            callbacks.remove(address())
        }
    }
} ?: MemorySegment.NULL

fun createCallback(
    callback: Any?,
    upcall: MemorySegment
): MemorySegment = callback?.run {
    malloc(layoutCallback.size).apply {
        set(JAVA_BYTE, layoutCallback[0], FLAG_RELEASABLE)
        set(ADDRESS, layoutCallback[1], upcall)
        set(ADDRESS, layoutCallback[2], ForeignUtils.Companion.callbackClone)
        set(ADDRESS, layoutCallback[3], ForeignUtils.Companion.callbackEquals)
        set(ADDRESS, layoutCallback[4], ForeignUtils.Companion.callbackHashCode)
        set(ADDRESS, layoutCallback[5], ForeignUtils.Companion.callbackFree)

        callbacks[address()] = callback
    }
} ?: MemorySegment.NULL

@Suppress("unchecked_cast")
fun <T> toKotlinCallback(segment: MemorySegment): T? {
    if (segment.address() == 0L) return null
    return callbacks[segment.address()] as T?
}