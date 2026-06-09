package com.huskerdev.nativekt.jvm.foreign;

import com.huskerdev.nativekt.jvm.NativeKtUtils;
import com.huskerdev.nativekt.jvm.jvmci.JVMCIUtils;
import jdk.internal.foreign.MemorySessionImpl;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.huskerdev.nativekt.jvm.NativeKtUtils.addExports;
import static java.lang.foreign.ValueLayout.*;

@SuppressWarnings("unused")
public class ForeignUtils {

    static {
        if(NativeKtUtils.isAutoExportEnabled()) {
            addExports(JVMCIUtils.class.getModule(), "java.base", new String[]{
                    "jdk.internal.foreign"
            });
        }
    }

    private static final Linker linker = Linker.nativeLinker();

    // Flags

    public static final byte FLAG_RELEASABLE = 1;
    public static final byte FLAG_ON_STACK = 2;

    // Layouts

    private static final CStructLayout layoutString = new CStructLayout(
            ADDRESS, JAVA_LONG, JAVA_INT, JAVA_BYTE
    );
    private static final CStructLayout layoutArray = new CStructLayout(
            ADDRESS, JAVA_LONG, JAVA_INT, JAVA_BYTE
    );
    private static final CStructLayout layoutCallback = new CStructLayout(
            JAVA_BYTE, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS
    );

    // Callbacks

    public static final ConcurrentMap<Long, Object> callbacks = new ConcurrentHashMap<>();

    private static final MemorySegment callbackClone, callbackEquals, callbackHashCode, callbackFree;

    // Malloc function

    private static final MethodHandle mallocHandle = linker.downcallHandle(
            linker.defaultLookup().find("malloc").orElseThrow(),
            FunctionDescriptor.of(ADDRESS, ValueLayout.JAVA_LONG)
    );

    public static MemorySegment malloc(long size) {
        try {
            return ((MemorySegment) mallocHandle.invoke(size)).reinterpret(size);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // Free function

    private static final MethodHandle freeHandle = linker.downcallHandle(
            linker.defaultLookup().find("free").orElseThrow(),
            FunctionDescriptor.ofVoid(ADDRESS)
    );

    public static void free(MemorySegment mem) {
        try {
            freeHandle.invoke(mem);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // Lookup functions

    public static MemorySegment address(SymbolLookup lookup, String name) {
        return lookup.find(name).orElseThrow();
    }

    public static MethodHandle handle(MemorySegment address, boolean isCritical, MemoryLayout retType, MemoryLayout... argTypes) {
        FunctionDescriptor function = retType == null ?
                FunctionDescriptor.ofVoid(argTypes) :
                FunctionDescriptor.of(retType, argTypes);

        if(isCritical)
            return linker.downcallHandle(address, function, Linker.Option.critical(true));
        else
            return linker.downcallHandle(address, function);
    }

    public static MethodHandle lookup(SymbolLookup lookup, String name, boolean isCritical, MemoryLayout retType, MemoryLayout... argTypes) {
        return handle(address(lookup, name), isCritical, retType, argTypes);
    }

    // String

    public static MemorySegment toNativeStringOnArena(Arena arena, String of) {
        MemorySegment stringMem = arena.allocateFrom(of);

        MemorySegment struct = arena.allocate(layoutString.getSize());
        struct.set(ADDRESS, layoutString.get(0), stringMem);
        struct.set(JAVA_LONG, layoutString.get(1), stringMem.byteSize() - 1);
        struct.set(JAVA_INT, layoutString.get(2), of.length());
        struct.set(JAVA_INT, layoutString.get(3), FLAG_ON_STACK);
        return struct;
    }

    public static MemorySegment toNativeString(String of) {
        byte[] bytes = of.getBytes(StandardCharsets.UTF_8);
        MemorySegment stringMem = malloc(bytes.length);
        MemorySegment.copy(bytes, 0, stringMem, JAVA_BYTE, 0, bytes.length);

        MemorySegment struct = malloc(layoutString.getSize());
        struct.set(ADDRESS, layoutString.get(0), stringMem);
        struct.set(JAVA_LONG, layoutString.get(1), bytes.length);
        struct.set(JAVA_INT, layoutString.get(2), of.length());
        struct.set(JAVA_INT, layoutString.get(3), FLAG_RELEASABLE);
        return struct;
    }

    public static String toJvmString(MemorySegment struct) {
        struct = struct.reinterpret(layoutString.getSize());
        MemorySegment data = struct.get(ADDRESS, layoutString.get(0));
        int size = (int) struct.get(JAVA_LONG, layoutString.get(1));

        final byte[] bytes = new byte[size];
        MemorySegment.copy(data.reinterpret(size), JAVA_BYTE, 0, bytes, 0, size);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    // Arrays

    private static MemorySegment fillArray(MemorySegment mem, MemorySegment data, int length, int flag) {
        mem.set(ADDRESS, layoutArray.get(0), data);
        mem.set(JAVA_LONG, layoutArray.get(1), data.byteSize());
        mem.set(JAVA_INT, layoutArray.get(2), length);
        mem.set(JAVA_INT, layoutArray.get(3), flag);
        return mem;
    }

    private static int getLength(MemorySegment array) {
        return array.get(JAVA_INT, layoutArray.get(2));
    }

    private static long getSize(MemorySegment array) {
        return array.get(JAVA_LONG, layoutArray.get(1));
    }

    private static MemorySegment getElements(MemorySegment array) {
        return array.get(ADDRESS, layoutArray.get(0)).reinterpret(getSize(array));
    }

    private static void copyElements(MemorySegment array, Object dstArray, int length, ValueLayout srcLayout) {
        MemorySegment elements = getElements(array);
        MemorySegment.copy(elements, srcLayout, 0L, dstArray, 0, length);
    }

    // Array: char

    public static MemorySegment toNativeCharArrayOnArena(Arena arena, char[] arr) {
        MemorySegment dataMem = arena.allocateFrom(JAVA_CHAR, arr);
        return fillArray(arena.allocate(layoutArray.getSize()), dataMem, arr.length, FLAG_ON_STACK);
    }

    public static MemorySegment toNativeCharArray(char[] arr) {
        MemorySegment dataMem = malloc(JAVA_CHAR.byteSize() * arr.length);
        MemorySegment.copy(arr, 0, dataMem, JAVA_CHAR, 0, arr.length);
        return fillArray(malloc(layoutArray.getSize()), dataMem, arr.length, FLAG_RELEASABLE);
    }

    public static char[] toJvmCharArray(MemorySegment struct) {
        struct = struct.reinterpret(layoutArray.getSize());
        char[] result = new char[getLength(struct)];
        copyElements(struct, result, result.length, JAVA_CHAR);
        return result;
    }

    // Array: boolean

    public static MemorySegment toNativeBooleanArrayOnArena(Arena arena, boolean[] arr) {
        byte[] bytes = new byte[arr.length];
        for(int i = 0; i < arr.length; i++)
            bytes[i] = (byte)(arr[i] ? 1 : 0);
        return toNativeByteArrayOnArena(arena, bytes);
    }

    public static MemorySegment toNativeBooleanArray(boolean[] arr) {
        byte[] bytes = new byte[arr.length];
        for(int i = 0; i < arr.length; i++)
            bytes[i] = (byte)(arr[i] ? 1 : 0);
        return toNativeByteArray(bytes);
    }

    public static boolean[] toJvmBooleanArray(MemorySegment struct) {
        byte[] bytes = toJvmByteArray(struct);
        boolean[] result = new boolean[bytes.length];
        for(int i = 0; i < bytes.length; i++)
            result[i] = (bytes[i] == 1);
        return result;
    }

    // Array: byte

    public static MemorySegment toNativeByteArrayOnArena(Arena arena, byte[] arr) {
        MemorySegment dataMem = arena.allocateFrom(JAVA_BYTE, arr);
        return fillArray(arena.allocate(layoutArray.getSize()), dataMem, arr.length, FLAG_ON_STACK);
    }

    public static MemorySegment toNativeByteArray(byte[] arr) {
        MemorySegment dataMem = malloc(arr.length);
        MemorySegment.copy(arr, 0, dataMem, JAVA_BYTE, 0, arr.length);
        return fillArray(malloc(layoutArray.getSize()), dataMem, arr.length, FLAG_RELEASABLE);
    }

    public static byte[] toJvmByteArray(MemorySegment struct) {
        struct = struct.reinterpret(layoutArray.getSize());
        byte[] result = new byte[getLength(struct)];
        copyElements(struct, result, result.length, JAVA_BYTE);
        return result;
    }

    // Array: short

    public static MemorySegment toNativeShortArrayOnArena(Arena arena, short[] arr) {
        MemorySegment dataMem = arena.allocateFrom(JAVA_SHORT, arr);
        return fillArray(arena.allocate(layoutArray.getSize()), dataMem, arr.length, FLAG_ON_STACK);
    }

    public static MemorySegment toNativeShortArray(short[] arr) {
        MemorySegment dataMem = malloc(JAVA_SHORT.byteSize() * arr.length);
        MemorySegment.copy(arr, 0, dataMem, JAVA_SHORT, 0, arr.length);
        return fillArray(malloc(layoutArray.getSize()), dataMem, arr.length, FLAG_RELEASABLE);
    }

    public static short[] toJvmShortArray(MemorySegment struct) {
        struct = struct.reinterpret(layoutArray.getSize());
        short[] result = new short[getLength(struct)];
        copyElements(struct, result, result.length, JAVA_SHORT);
        return result;
    }

    // Array: int

    public static MemorySegment toNativeIntArrayOnArena(Arena arena, int[] arr) {
        MemorySegment dataMem = arena.allocateFrom(JAVA_INT, arr);
        return fillArray(arena.allocate(layoutArray.getSize()), dataMem, arr.length, FLAG_ON_STACK);
    }

    public static MemorySegment toNativeIntArray(int[] arr) {
        MemorySegment dataMem = malloc(JAVA_INT.byteSize() * arr.length);
        MemorySegment.copy(arr, 0, dataMem, JAVA_INT, 0, arr.length);
        return fillArray(malloc(layoutArray.getSize()), dataMem, arr.length, FLAG_RELEASABLE);
    }

    public static int[] toJvmIntArray(MemorySegment struct) {
        struct = struct.reinterpret(layoutArray.getSize());
        int[] result = new int[getLength(struct)];
        copyElements(struct, result, result.length, JAVA_INT);
        return result;
    }

    // Array: long

    public static MemorySegment toNativeLongArrayOnArena(Arena arena, long[] arr) {
        MemorySegment dataMem = arena.allocateFrom(JAVA_LONG, arr);
        return fillArray(arena.allocate(layoutArray.getSize()), dataMem, arr.length, FLAG_ON_STACK);
    }

    public static MemorySegment toNativeLongArray(long[] arr) {
        MemorySegment dataMem = malloc(JAVA_LONG.byteSize() * arr.length);
        MemorySegment.copy(arr, 0, dataMem, JAVA_LONG, 0, arr.length);
        return fillArray(malloc(layoutArray.getSize()), dataMem, arr.length, FLAG_RELEASABLE);
    }

    public static long[] toJvmLongArray(MemorySegment struct) {
        struct = struct.reinterpret(layoutArray.getSize());
        long[] result = new long[getLength(struct)];
        copyElements(struct, result, result.length, JAVA_LONG);
        return result;
    }

    // Array: float

    public static MemorySegment toNativeFloatArrayOnArena(Arena arena, float[] arr) {
        MemorySegment dataMem = arena.allocateFrom(JAVA_FLOAT, arr);
        return fillArray(arena.allocate(layoutArray.getSize()), dataMem, arr.length, FLAG_ON_STACK);
    }

    public static MemorySegment toNativeFloatArray(float[] arr) {
        MemorySegment dataMem = malloc(JAVA_FLOAT.byteSize() * arr.length);
        MemorySegment.copy(arr, 0, dataMem, JAVA_FLOAT, 0, arr.length);
        return fillArray(malloc(layoutArray.getSize()), dataMem, arr.length, FLAG_RELEASABLE);
    }

    public static float[] toJvmFloatArray(MemorySegment struct) {
        struct = struct.reinterpret(layoutArray.getSize());
        float[] result = new float[getLength(struct)];
        copyElements(struct, result, result.length, JAVA_FLOAT);
        return result;
    }

    // Array: double

    public static MemorySegment toNativeDoubleArrayOnArena(Arena arena, double[] arr) {
        MemorySegment dataMem = arena.allocateFrom(JAVA_DOUBLE, arr);
        return fillArray(arena.allocate(layoutArray.getSize()), dataMem, arr.length, FLAG_ON_STACK);
    }

    public static MemorySegment toNativeDoubleArray(double[] arr) {
        MemorySegment dataMem = malloc(JAVA_DOUBLE.byteSize() * arr.length);
        MemorySegment.copy(arr, 0, dataMem, JAVA_DOUBLE, 0, arr.length);
        return fillArray(malloc(layoutArray.getSize()), dataMem, arr.length, FLAG_RELEASABLE);
    }

    public static double[] toJvmDoubleArray(MemorySegment struct) {
        struct = struct.reinterpret(layoutArray.getSize());
        double[] result = new double[getLength(struct)];
        copyElements(struct, result, result.length, JAVA_DOUBLE);
        return result;
    }

    // Array: enum

    public static <T extends Enum<T>> MemorySegment toNativeEnumArrayOnArena(Arena arena, T[] arr) {
        int[] intEnums = new int[arr.length];
        for(int i = 0; i < arr.length; i++)
            intEnums[i] = arr[i].ordinal();
        return toNativeIntArrayOnArena(arena, intEnums);
    }

    public static <T extends Enum<T>> MemorySegment toNativeEnumArray(T[] arr) {
        int[] intEnums = new int[arr.length];
        for(int i = 0; i < arr.length; i++)
            intEnums[i] = arr[i].ordinal();
        return toNativeIntArray(intEnums);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T[] toJvmEnumArray(
            MemorySegment struct,
            Class<T> enumClass
    ) {
        int[] ordinals = toJvmIntArray(struct);

        // Convert integers to enum values
        T[] enumConstants = enumClass.getEnumConstants();
        T[] result = (T[]) java.lang.reflect.Array.newInstance(enumClass, ordinals.length);
        for(int i = 0; i < ordinals.length; i++)
            result[i] = enumConstants[ordinals[i]];
        return result;
    }

    // Array: object

    public static <T> MemorySegment toNativeArrayOnArena(Arena arena, T[] arr, BiFunction<Arena, T, MemorySegment> cast) {
        MemorySegment dataMem = arena.allocate(ADDRESS, arr.length);
        VarHandle ptrHandle = ADDRESS.arrayElementVarHandle();
        for(int i = 0; i < arr.length; i++)
            ptrHandle.set(dataMem, 0L, (long)i, cast.apply(arena, arr[i]));
        return fillArray(arena.allocate(layoutArray.getSize()), dataMem, arr.length, FLAG_ON_STACK);
    }

    public static <T> MemorySegment toNativeArray(T[] arr, Function<T, MemorySegment> cast) {
        MemorySegment dataMem = malloc(ADDRESS.byteSize() * arr.length);
        VarHandle ptrHandle = ADDRESS.arrayElementVarHandle();
        for(int i = 0; i < arr.length; i++)
            ptrHandle.set(dataMem, 0L, (long)i, cast.apply(arr[i]));
        return fillArray(malloc(layoutArray.getSize()), dataMem, arr.length, FLAG_RELEASABLE);
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] toJvmArray(MemorySegment struct, Function<MemorySegment, T> cast, Class<T> clazz) {
        struct = struct.reinterpret(layoutArray.getSize());
        T[] result = (T[]) java.lang.reflect.Array.newInstance(clazz, getLength(struct));

        MemorySegment elementsPtr = getElements(struct).reinterpret(ADDRESS.byteSize() * result.length);
        VarHandle ptrHandle = ADDRESS.arrayElementVarHandle();
        for(int i = 0; i < result.length; i++)
            result[i] = cast.apply((MemorySegment) ptrHandle.get(elementsPtr, 0L, (long)i));

        return result;
    }

    // Callbacks

    public static MemorySegment upcall(
            MethodHandles.Lookup lookup,
            String name,
            MethodType methodType,
            FunctionDescriptor functionDescriptor
    ) throws Throwable {
        return linker.upcallStub(
                lookup.findStatic(lookup.lookupClass(), name, methodType),
                functionDescriptor,
                Arena.global()
        );
    }

    public static MemorySegment createCallbackOnArena(
            Arena arena,
            Object callback,
            MemorySegment upcall
    ){
        MemorySegment struct = arena.allocate(layoutCallback.getSize());
        struct.set(JAVA_BYTE, layoutCallback.get(0), FLAG_ON_STACK);
        struct.set(ADDRESS, layoutCallback.get(1), upcall);
        struct.set(ADDRESS, layoutCallback.get(2), callbackClone);
        struct.set(ADDRESS, layoutCallback.get(3), callbackEquals);
        struct.set(ADDRESS, layoutCallback.get(4), callbackHashCode);
        struct.set(ADDRESS, layoutCallback.get(5), callbackFree);

        long address = struct.address();
        callbacks.put(address, callback);

        // Remove callback from list when Arena is closed
        MemorySessionImpl.toMemorySession(arena).addCloseAction(() ->
            callbacks.remove(address)
        );
        return struct;
    }

    public static MemorySegment createCallback(
            Object callback,
            MemorySegment upcall
    ) {
        MemorySegment struct = malloc(layoutCallback.getSize());
        struct.set(JAVA_BYTE, layoutCallback.get(0), FLAG_RELEASABLE);
        struct.set(ADDRESS, layoutCallback.get(1), upcall);
        struct.set(ADDRESS, layoutCallback.get(2), callbackClone);
        struct.set(ADDRESS, layoutCallback.get(3), callbackEquals);
        struct.set(ADDRESS, layoutCallback.get(4), callbackHashCode);
        struct.set(ADDRESS, layoutCallback.get(5), callbackFree);
        callbacks.put(struct.address(), callback);
        return struct;
    }

    public static MemorySegment callbackClone(MemorySegment self) {
        MemorySegment struct = malloc(layoutCallback.getSize());
        struct.set(JAVA_BYTE, layoutCallback.get(0), FLAG_RELEASABLE);
        struct.set(ADDRESS, layoutCallback.get(1), self.reinterpret(layoutCallback.getSize()).get(ADDRESS, 8L));
        struct.set(ADDRESS, layoutCallback.get(2), callbackClone);
        struct.set(ADDRESS, layoutCallback.get(3), callbackEquals);
        struct.set(ADDRESS, layoutCallback.get(4), callbackHashCode);
        struct.set(ADDRESS, layoutCallback.get(5), callbackFree);
        callbacks.put(struct.address(), callbacks.get(self.address()));
        return struct;
    }

    public static boolean callbackEquals(MemorySegment self, MemorySegment obj) {
        return callbacks.get(self.address()).equals(callbacks.get(obj.address()));
    }

    public static int callbackHashCode(MemorySegment self) {
        return callbacks.get(self.address()).hashCode();
    }

    public static void callbackFree(MemorySegment callback) {
        callback = callback.reinterpret(layoutCallback.getSize());
        callbacks.remove(callback.address());

        if((callback.get(JAVA_INT, 0L) & FLAG_RELEASABLE) == FLAG_RELEASABLE)
            free(callback);
    }

    @SuppressWarnings("unchecked")
    public static <T> T toJvmCallback(MemorySegment segment) {
        return (T) callbacks.get(segment.address());
    }

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            callbackClone = upcall(
                    lookup,
                    "callbackClone",
                    MethodType.methodType(MemorySegment.class, MemorySegment.class),
                    FunctionDescriptor.of(ADDRESS, ADDRESS)
            );
            callbackEquals = upcall(
                    lookup,
                    "callbackEquals",
                    MethodType.methodType(boolean.class, MemorySegment.class, MemorySegment.class),
                    FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ADDRESS, ADDRESS)
            );
            callbackHashCode = upcall(
                    lookup,
                    "callbackHashCode",
                    MethodType.methodType(int.class, MemorySegment.class),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ADDRESS)
            );
            callbackFree = upcall(
                    lookup,
                    "callbackFree",
                    MethodType.methodType(void.class, MemorySegment.class),
                    FunctionDescriptor.ofVoid(ADDRESS)
            );
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
