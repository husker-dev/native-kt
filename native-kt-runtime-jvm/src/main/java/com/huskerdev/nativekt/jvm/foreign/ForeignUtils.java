package com.huskerdev.nativekt.jvm.foreign;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

@SuppressWarnings("unused")
public class ForeignUtils {

    private static final Linker linker = Linker.nativeLinker();

    // KString struct

    public static final StructLayout STRING_STRUCT = MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withName("data"),
            ValueLayout.JAVA_INT.withName("length"),
            MemoryLayout.paddingLayout(4) // align to 16
    );
    public static final VarHandle stringDataVarHandle = STRING_STRUCT.varHandle(MemoryLayout.PathElement.groupElement("data"));
    public static final VarHandle stringLengthVarHandle = STRING_STRUCT.varHandle(MemoryLayout.PathElement.groupElement("length"));

    // KArray struct

    public static final StructLayout ARRAY_STRUCT = MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withName("elements"),
            ValueLayout.JAVA_INT.withName("size"),
            MemoryLayout.paddingLayout(4) // align to 16
    );
    public static final VarHandle arrayElementsVarHandle = ARRAY_STRUCT.varHandle(MemoryLayout.PathElement.groupElement("elements"));
    public static final VarHandle arraySizeVarHandle = ARRAY_STRUCT.varHandle(MemoryLayout.PathElement.groupElement("size"));

    // Callbacks

    public static final HashMap<Long, Object> callbacks = new HashMap<>();

    private static final StructLayout callbackStructLayout = MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withName("m"),
            ValueLayout.ADDRESS.withName("invoke"),
            ValueLayout.ADDRESS.withName("free")
    );
    private static final VarHandle callbackInvokeVarHandle = callbackStructLayout.varHandle(MemoryLayout.PathElement.groupElement("invoke"));
    private static final VarHandle callbackFreeVarHandle = callbackStructLayout.varHandle(MemoryLayout.PathElement.groupElement("free"));

    private static final MemorySegment callbackFree;

    // Types

    public static ValueLayout C_CHAR = ValueLayout.JAVA_CHAR;
    public static ValueLayout C_BYTE = ValueLayout.JAVA_BYTE;
    public static ValueLayout C_BOOLEAN = ValueLayout.JAVA_BOOLEAN;
    public static ValueLayout C_SHORT = ValueLayout.JAVA_SHORT;
    public static ValueLayout C_INT = ValueLayout.JAVA_INT;
    public static ValueLayout C_LONG = ValueLayout.JAVA_LONG;
    public static ValueLayout C_FLOAT = ValueLayout.JAVA_FLOAT;
    public static ValueLayout C_DOUBLE = ValueLayout.JAVA_DOUBLE;
    public static ValueLayout C_ADDRESS = ValueLayout.ADDRESS;

    // Free function

    public static final MethodHandle freeHandle = linker.downcallHandle(
            linker.defaultLookup().find("free").orElseThrow(),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );

    public static MethodHandle lookup(String name, boolean isCritical, MemoryLayout retType, MemoryLayout... argTypes) {
        FunctionDescriptor function = retType == null ?
                FunctionDescriptor.ofVoid(argTypes) :
                FunctionDescriptor.of(retType, argTypes);

        MemorySegment address = SymbolLookup.loaderLookup().find(name).orElseThrow();

        if(isCritical)
            return linker.downcallHandle(address, function, Linker.Option.critical(true));
        else
            return linker.downcallHandle(address, function);
    }

    // String

    public static MemorySegment toNativeHeapString(String str) {
        return MemorySegment.ofArray(str.getBytes());
    }

    public static MemorySegment toNativeString(String of) {
        MemorySegment struct = Arena.ofAuto().allocate(STRING_STRUCT);
        stringDataVarHandle.set(struct, 0L, Arena.global().allocateFrom(of));
        stringLengthVarHandle.set(struct, 0L, of.length());
        return struct;
    }

    public static String toJvmString(MemorySegment struct, boolean dealloc) throws Throwable {
        MemorySegment data = (MemorySegment)stringDataVarHandle.get(struct, 0L);
        int length = (int)stringLengthVarHandle.get(struct, 0L);

        final byte[] bytes = new byte[length];
        MemorySegment.copy(data.reinterpret(length), JAVA_BYTE, 0, bytes, 0, length);
        String result = new String(bytes, StandardCharsets.UTF_8);

        if(dealloc)
            freeHandle.invoke((MemorySegment)ForeignUtils.stringDataVarHandle.get(struct, 0L));
        return result;
    }

    // Arrays

    public static int arraySize(MemorySegment struct) {
        return (int)arraySizeVarHandle.get(struct, 0L);
    }

    public static MemorySegment arrayElements(MemorySegment struct) {
        return (MemorySegment)ForeignUtils.arrayElementsVarHandle.get(struct, 0L);
    }

    public static long arrayElementsAddress(MemorySegment struct) {
        return arrayElements(struct).address();
    }

    // Array: char

    public static MemorySegment toNativeCharArray(char[] arr) {
        return toNativeCharArray(arr, Arena.global(), Arena.ofAuto());
    }

    public static MemorySegment toNativeCharArray(char[] arr, Arena elementsArena, Arena structArena) {
        MemorySegment struct = structArena.allocate(ARRAY_STRUCT);
        arrayElementsVarHandle.set(struct, 0L,
                elementsArena.allocate((long) arr.length * C_CHAR.byteSize(), C_CHAR.byteAlignment())
                        .copyFrom(MemorySegment.ofArray(arr))
        );
        arraySizeVarHandle.set(struct, 0L, arr.length);
        return struct;
    }

    public static char[] toJvmCharArray(MemorySegment struct, boolean dealloc) throws Throwable {
        char[] result = new char[arraySize(struct)];
        MemorySegment elements = arrayElements(struct).reinterpret(result.length * C_CHAR.byteSize());
        MemorySegment.copy(elements, C_CHAR, 0L, result, 0, result.length);
        if(dealloc) freeHandle.invoke(elements);
        return result;
    }

    // Array: boolean

    public static MemorySegment toNativeBooleanArray(boolean[] arr) {
        return toNativeBooleanArray(arr, Arena.global(), Arena.ofAuto());
    }

    public static MemorySegment toNativeBooleanArray(boolean[] arr, Arena elementsArena, Arena structArena) {
        byte[] bytes = new byte[arr.length];
        for(int i = 0; i < arr.length; i++)
            bytes[i] = (byte)(arr[i] ? 1 : 0);
        return toNativeByteArray(bytes, elementsArena, structArena);
    }

    public static boolean[] toJvmBooleanArray(MemorySegment struct, boolean dealloc) throws Throwable {
        byte[] bytes = toJvmByteArray(struct, dealloc);
        boolean[] result = new boolean[bytes.length];
        for(int i = 0; i < bytes.length; i++)
            result[i] = (bytes[i] == 1);
        return result;
    }

    // Array: byte

    public static MemorySegment toNativeByteArray(byte[] arr) {
        return toNativeByteArray(arr, Arena.global(), Arena.ofAuto());
    }

    public static MemorySegment toNativeByteArray(byte[] arr, Arena elementsArena, Arena structArena) {
        MemorySegment struct = structArena.allocate(ARRAY_STRUCT);
        arrayElementsVarHandle.set(struct, 0L,
                elementsArena.allocate((long) arr.length * C_BYTE.byteSize(), C_BYTE.byteAlignment())
                        .copyFrom(MemorySegment.ofArray(arr))
        );
        arraySizeVarHandle.set(struct, 0L, arr.length);
        return struct;
    }

    public static byte[] toJvmByteArray(MemorySegment struct, boolean dealloc) throws Throwable {
        byte[] result = new byte[arraySize(struct)];
        MemorySegment elements = arrayElements(struct).reinterpret(result.length * C_BYTE.byteSize());
        MemorySegment.copy(elements, C_BYTE, 0L, result, 0, result.length);
        if(dealloc) freeHandle.invoke(elements);
        return result;
    }

    // Array: short

    public static MemorySegment toNativeShortArray(short[] arr) {
        return toNativeShortArray(arr, Arena.global(), Arena.ofAuto());
    }

    public static MemorySegment toNativeShortArray(short[] arr, Arena elementsArena, Arena structArena) {
        MemorySegment struct = structArena.allocate(ARRAY_STRUCT);
        arrayElementsVarHandle.set(struct, 0L,
                elementsArena.allocate((long) arr.length * C_SHORT.byteSize(), C_SHORT.byteAlignment())
                        .copyFrom(MemorySegment.ofArray(arr))
        );
        arraySizeVarHandle.set(struct, 0L, arr.length);
        return struct;
    }

    public static short[] toJvmShortArray(MemorySegment struct, boolean dealloc) throws Throwable {
        short[] result = new short[arraySize(struct)];
        MemorySegment elements = arrayElements(struct).reinterpret(result.length * C_SHORT.byteSize());
        MemorySegment.copy(elements, C_SHORT, 0L, result, 0, result.length);
        if(dealloc) freeHandle.invoke(elements);
        return result;
    }

    // Array: int

    public static MemorySegment toNativeIntArray(int[] arr) {
        return toNativeIntArray(arr, Arena.global(), Arena.ofAuto());
    }

    public static MemorySegment toNativeIntArray(int[] arr, Arena elementsArena, Arena structArena) {
        MemorySegment struct = structArena.allocate(ARRAY_STRUCT);
        arrayElementsVarHandle.set(struct, 0L,
                elementsArena.allocate((long) arr.length * C_INT.byteSize(), C_INT.byteAlignment())
                        .copyFrom(MemorySegment.ofArray(arr))
        );
        arraySizeVarHandle.set(struct, 0L, arr.length);
        return struct;
    }

    public static int[] toJvmIntArray(MemorySegment struct, boolean dealloc) throws Throwable {
        int[] result = new int[arraySize(struct)];
        MemorySegment elements = arrayElements(struct).reinterpret(result.length * C_INT.byteSize());
        MemorySegment.copy(elements, C_INT, 0L, result, 0, result.length);
        if(dealloc) freeHandle.invoke(elements);
        return result;
    }

    // Array: long

    public static MemorySegment toNativeLongArray(long[] arr) {
        return toNativeLongArray(arr, Arena.global(), Arena.ofAuto());
    }

    public static MemorySegment toNativeLongArray(long[] arr, Arena elementsArena, Arena structArena) {
        MemorySegment struct = structArena.allocate(ARRAY_STRUCT);
        arrayElementsVarHandle.set(struct, 0L,
                elementsArena.allocate((long) arr.length * C_LONG.byteSize(), C_LONG.byteAlignment())
                        .copyFrom(MemorySegment.ofArray(arr))
        );
        arraySizeVarHandle.set(struct, 0L, arr.length);
        return struct;
    }

    public static long[] toJvmLongArray(MemorySegment struct, boolean dealloc) throws Throwable {
        long[] result = new long[arraySize(struct)];
        MemorySegment elements = arrayElements(struct).reinterpret(result.length * C_LONG.byteSize());
        MemorySegment.copy(elements, C_LONG, 0L, result, 0, result.length);
        if(dealloc) freeHandle.invoke(elements);
        return result;
    }

    // Array: float

    public static MemorySegment toNativeFloatArray(float[] arr) {
        return toNativeFloatArray(arr, Arena.global(), Arena.ofAuto());
    }

    public static MemorySegment toNativeFloatArray(float[] arr, Arena elementsArena, Arena structArena) {
        MemorySegment struct = structArena.allocate(ARRAY_STRUCT);
        arrayElementsVarHandle.set(struct, 0L,
                elementsArena.allocate((long) arr.length * C_FLOAT.byteSize(), C_FLOAT.byteAlignment())
                        .copyFrom(MemorySegment.ofArray(arr))
        );
        arraySizeVarHandle.set(struct, 0L, arr.length);
        return struct;
    }

    public static float[] toJvmFloatArray(MemorySegment struct, boolean dealloc) throws Throwable {
        float[] result = new float[arraySize(struct)];
        MemorySegment elements = arrayElements(struct).reinterpret(result.length * C_FLOAT.byteSize());
        MemorySegment.copy(elements, C_FLOAT, 0L, result, 0, result.length);
        if(dealloc) freeHandle.invoke(elements);
        return result;
    }

    // Array: double

    public static MemorySegment toNativeDoubleArray(double[] arr) {
        return toNativeDoubleArray(arr, Arena.global(), Arena.ofAuto());
    }

    public static MemorySegment toNativeDoubleArray(double[] arr, Arena elementsArena, Arena structArena) {
        MemorySegment struct = structArena.allocate(ARRAY_STRUCT);
        arrayElementsVarHandle.set(struct, 0L,
                elementsArena.allocate((long) arr.length * C_DOUBLE.byteSize(), C_DOUBLE.byteAlignment())
                        .copyFrom(MemorySegment.ofArray(arr))
        );
        arraySizeVarHandle.set(struct, 0L, arr.length);
        return struct;
    }

    public static double[] toJvmDoubleArray(MemorySegment struct, boolean dealloc) throws Throwable {
        double[] result = new double[arraySize(struct)];
        MemorySegment elements = arrayElements(struct).reinterpret(result.length * C_DOUBLE.byteSize());
        MemorySegment.copy(elements, C_DOUBLE, 0L, result, 0, result.length);
        if(dealloc) freeHandle.invoke(elements);
        return result;
    }

    // Array: enum

    public static <T extends Enum<T>> MemorySegment toNativeEnumArray(T[] arr) {
        return toNativeEnumArray(arr, Arena.global(), Arena.ofAuto());
    }

    public static <T extends Enum<T>> MemorySegment toNativeEnumArray(T[] arr, Arena elementsArena, Arena structArena) {
        int[] intEnums = new int[arr.length];
        for(int i = 0; i < arr.length; i++)
            intEnums[i] = arr[i].ordinal();
        return toNativeIntArray(intEnums, elementsArena, structArena);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T[] toJvmEnumArray(
            MemorySegment struct,
            boolean dealloc,
            Class<T> enumClass
    ) throws Throwable {
        int[] ordinals = toJvmIntArray(struct, dealloc);

        // Convert integers to enum values
        T[] enumConstants = enumClass.getEnumConstants();
        T[] result = (T[]) java.lang.reflect.Array.newInstance(enumClass, ordinals.length);
        for(int i = 0; i < ordinals.length; i++)
            result[i] = enumConstants[ordinals[i]];
        return result;
    }

    // Array: object

    public static <T> MemorySegment toNativeArray(T[] elements, Function<T, MemorySegment> cast) {
        MemorySegment elementsPtr = Arena.global().allocate(MemoryLayout.sequenceLayout(elements.length, ValueLayout.ADDRESS));
        VarHandle ptrHandle = ValueLayout.ADDRESS.arrayElementVarHandle();
        for(int i = 0; i < elements.length; i++)
            ptrHandle.set(elementsPtr, 0L, (long)i, cast.apply(elements[i]));

        MemorySegment struct = Arena.ofAuto().allocate(ARRAY_STRUCT);
        arrayElementsVarHandle.set(struct, 0L, elementsPtr);
        arraySizeVarHandle.set(struct, 0L, elements.length);
        return struct;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] toJvmArray(MemorySegment struct, BiFunction<MemorySegment, Boolean, T> cast, Class<T> clazz, boolean dealloc, boolean deallocContent) throws Throwable {
        T[] result = (T[]) java.lang.reflect.Array.newInstance(clazz, arraySize(struct));

        MemorySegment elementsPtr = arrayElements(struct).reinterpret(result.length * C_ADDRESS.byteSize());
        VarHandle ptrHandle = ValueLayout.ADDRESS.arrayElementVarHandle();
        for(int i = 0; i < result.length; i++)
            result[i] = cast.apply((MemorySegment) ptrHandle.get(elementsPtr, 0L, (long)i), deallocContent);

        if(dealloc) freeHandle.invoke(elementsPtr);
        return result;
    }

    // Callbacks

    public static MemorySegment createCallback(
            Object callback,
            MethodHandle invokeHandle,
            FunctionDescriptor invokeDesc
    ){
        Arena arena = Arena.global();
        MemorySegment struct = arena.allocate(callbackStructLayout);
        callbacks.put(struct.address(), callback);

        callbackInvokeVarHandle.set(struct, 0L, linker.upcallStub(
                invokeHandle,
                invokeDesc,
                arena
        ));
        callbackFreeVarHandle.set(struct, 0L, callbackFree);
        return struct;
    }

    public static void callbackFree(MemorySegment callback) {
        callbacks.remove(callback.address());
        try {
            freeHandle.invoke(callback);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T toJvmCallback(MemorySegment segment, boolean dealloc) throws Throwable{
        Object result = callbacks.get(segment.address());
        if(dealloc)
            freeHandle.invoke(segment);
        return (T) result;
    }

    static {
        try {
            callbackFree = linker.upcallStub(
                    MethodHandles.lookup().findStatic(
                            ForeignUtils.class,
                            "callbackFree",
                            MethodType.methodType(void.class, MemorySegment.class)
                    ),
                    FunctionDescriptor.ofVoid(ValueLayout.ADDRESS),
                    Arena.global()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
