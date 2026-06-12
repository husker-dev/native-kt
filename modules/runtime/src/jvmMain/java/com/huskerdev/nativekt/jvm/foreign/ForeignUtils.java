package com.huskerdev.nativekt.jvm.foreign;

import com.huskerdev.nativekt.jvm.NativeKtUtils;
import com.huskerdev.nativekt.jvm.jvmci.JVMCIUtils;
import jdk.internal.foreign.MemorySessionImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public static MemorySegment toNativeKStringOnArena(Arena arena, @Nullable String of) {
        if(of == null)
            return MemorySegment.NULL;
        MemorySegment stringMem = arena.allocateFrom(of);

        MemorySegment struct = arena.allocate(layoutString.getSize());
        struct.set(ADDRESS, layoutString.get(0), stringMem);
        struct.set(JAVA_LONG, layoutString.get(1), stringMem.byteSize() - 1);
        struct.set(JAVA_INT, layoutString.get(2), of.length());
        struct.set(JAVA_INT, layoutString.get(3), FLAG_ON_STACK);
        return struct;
    }

    public static MemorySegment toNativeKString(@Nullable String of) {
        if(of == null)
            return MemorySegment.NULL;

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

    @Nullable
    public static String toJvmKString(MemorySegment struct) {
        if(struct.address() == 0L)
            return null;

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

    @NotNull
    public static MemorySegment toNativeKCharArrayDirect(@Nullable char[] arr) {
        if(arr == null) return MemorySegment.NULL;
        return MemorySegment.ofArray(arr);
    }

    @NotNull
    public static MemorySegment toNativeKCharArrayOnArena(@NotNull Arena arena, @Nullable char[] arr) {
        if(arr == null) return MemorySegment.NULL;
        MemorySegment dataMem = arena.allocateFrom(JAVA_CHAR, arr);
        return fillArray(arena.allocate(layoutArray.getSize()), dataMem, arr.length, FLAG_ON_STACK);
    }

    @NotNull
    public static MemorySegment toNativeKCharArray(@Nullable char[] arr) {
        if(arr == null) return MemorySegment.NULL;
        MemorySegment dataMem = malloc(JAVA_CHAR.byteSize() * arr.length);
        MemorySegment.copy(arr, 0, dataMem, JAVA_CHAR, 0, arr.length);
        return fillArray(malloc(layoutArray.getSize()), dataMem, arr.length, FLAG_RELEASABLE);
    }

    @Nullable
    public static char[] toJvmKCharArray(@NotNull MemorySegment struct) {
        if(struct.address() == 0L) return null;
        struct = struct.reinterpret(layoutArray.getSize());
        char[] result = new char[getLength(struct)];
        copyElements(struct, result, result.length, JAVA_CHAR);
        return result;
    }

    // Array: boolean

    @NotNull
    public static MemorySegment toNativeKBooleanArrayDirect(@Nullable boolean[] arr) {
        if(arr == null) return MemorySegment.NULL;
        byte[] bytes = new byte[arr.length];
        for(int i = 0; i < arr.length; i++)
            bytes[i] = (byte)(arr[i] ? 1 : 0);
        return MemorySegment.ofArray(bytes);
    }

    @NotNull
    public static MemorySegment toNativeKBooleanArrayOnArena(@NotNull Arena arena, @Nullable boolean[] arr) {
        if(arr == null) return MemorySegment.NULL;
        byte[] bytes = new byte[arr.length];
        for(int i = 0; i < arr.length; i++)
            bytes[i] = (byte)(arr[i] ? 1 : 0);
        return toNativeKByteArrayOnArena(arena, bytes);
    }

    @NotNull
    public static MemorySegment toNativeKBooleanArray(@Nullable boolean[] arr) {
        if(arr == null) return MemorySegment.NULL;
        byte[] bytes = new byte[arr.length];
        for(int i = 0; i < arr.length; i++)
            bytes[i] = (byte)(arr[i] ? 1 : 0);
        return toNativeKByteArray(bytes);
    }

    @Nullable
    public static boolean[] toJvmKBooleanArray(@NotNull MemorySegment struct) {
        if(struct.address() == 0L) return null;
        byte[] bytes = toJvmKByteArray(struct);
        boolean[] result = new boolean[bytes.length];
        for(int i = 0; i < bytes.length; i++)
            result[i] = (bytes[i] == 1);
        return result;
    }

    // Array: byte

    @NotNull
    public static MemorySegment toNativeKByteArrayDirect(@Nullable byte[] arr) {
        if(arr == null) return MemorySegment.NULL;
        return MemorySegment.ofArray(arr);
    }

    @NotNull
    public static MemorySegment toNativeKByteArrayOnArena(@NotNull Arena arena, @Nullable byte[] arr) {
        if(arr == null) return MemorySegment.NULL;
        MemorySegment dataMem = arena.allocateFrom(JAVA_BYTE, arr);
        return fillArray(arena.allocate(layoutArray.getSize()), dataMem, arr.length, FLAG_ON_STACK);
    }

    @NotNull
    public static MemorySegment toNativeKByteArray(@Nullable byte[] arr) {
        if(arr == null) return MemorySegment.NULL;
        MemorySegment dataMem = malloc(arr.length);
        MemorySegment.copy(arr, 0, dataMem, JAVA_BYTE, 0, arr.length);
        return fillArray(malloc(layoutArray.getSize()), dataMem, arr.length, FLAG_RELEASABLE);
    }

    @Nullable
    public static byte[] toJvmKByteArray(@NotNull MemorySegment struct) {
        if(struct.address() == 0L) return null;
        struct = struct.reinterpret(layoutArray.getSize());
        byte[] result = new byte[getLength(struct)];
        copyElements(struct, result, result.length, JAVA_BYTE);
        return result;
    }

    // Array: short

    @NotNull
    public static MemorySegment toNativeKShortArrayDirect(@Nullable short[] arr) {
        if(arr == null) return MemorySegment.NULL;
        return MemorySegment.ofArray(arr);
    }

    @NotNull
    public static MemorySegment toNativeKShortArrayOnArena(@NotNull Arena arena, @Nullable short[] arr) {
        if(arr == null) return MemorySegment.NULL;
        MemorySegment dataMem = arena.allocateFrom(JAVA_SHORT, arr);
        return fillArray(arena.allocate(layoutArray.getSize()), dataMem, arr.length, FLAG_ON_STACK);
    }

    @NotNull
    public static MemorySegment toNativeKShortArray(@Nullable short[] arr) {
        if(arr == null) return MemorySegment.NULL;
        MemorySegment dataMem = malloc(JAVA_SHORT.byteSize() * arr.length);
        MemorySegment.copy(arr, 0, dataMem, JAVA_SHORT, 0, arr.length);
        return fillArray(malloc(layoutArray.getSize()), dataMem, arr.length, FLAG_RELEASABLE);
    }

    @Nullable
    public static short[] toJvmKShortArray(@NotNull MemorySegment struct) {
        if(struct.address() == 0L) return null;
        struct = struct.reinterpret(layoutArray.getSize());
        short[] result = new short[getLength(struct)];
        copyElements(struct, result, result.length, JAVA_SHORT);
        return result;
    }

    // Array: int

    @NotNull
    public static MemorySegment toNativeKIntArrayDirect(@Nullable int[] arr) {
        if(arr == null) return MemorySegment.NULL;
        return MemorySegment.ofArray(arr);
    }

    @NotNull
    public static MemorySegment toNativeKIntArrayOnArena(@NotNull Arena arena, @Nullable int[] arr) {
        if(arr == null) return MemorySegment.NULL;
        MemorySegment dataMem = arena.allocateFrom(JAVA_INT, arr);
        return fillArray(arena.allocate(layoutArray.getSize()), dataMem, arr.length, FLAG_ON_STACK);
    }

    @NotNull
    public static MemorySegment toNativeKIntArray(@Nullable int[] arr) {
        if(arr == null) return MemorySegment.NULL;
        MemorySegment dataMem = malloc(JAVA_INT.byteSize() * arr.length);
        MemorySegment.copy(arr, 0, dataMem, JAVA_INT, 0, arr.length);
        return fillArray(malloc(layoutArray.getSize()), dataMem, arr.length, FLAG_RELEASABLE);
    }

    @Nullable
    public static int[] toJvmKIntArray(@NotNull MemorySegment struct) {
        if(struct.address() == 0L) return null;
        struct = struct.reinterpret(layoutArray.getSize());
        int[] result = new int[getLength(struct)];
        copyElements(struct, result, result.length, JAVA_INT);
        return result;
    }

    // Array: long

    @NotNull
    public static MemorySegment toNativeKLongArrayDirect(@Nullable long[] arr) {
        if(arr == null) return MemorySegment.NULL;
        return MemorySegment.ofArray(arr);
    }

    @NotNull
    public static MemorySegment toNativeKLongArrayOnArena(@NotNull Arena arena, @Nullable long[] arr) {
        if(arr == null) return MemorySegment.NULL;
        MemorySegment dataMem = arena.allocateFrom(JAVA_LONG, arr);
        return fillArray(arena.allocate(layoutArray.getSize()), dataMem, arr.length, FLAG_ON_STACK);
    }

    @NotNull
    public static MemorySegment toNativeKLongArray(@Nullable long[] arr) {
        if(arr == null) return MemorySegment.NULL;
        MemorySegment dataMem = malloc(JAVA_LONG.byteSize() * arr.length);
        MemorySegment.copy(arr, 0, dataMem, JAVA_LONG, 0, arr.length);
        return fillArray(malloc(layoutArray.getSize()), dataMem, arr.length, FLAG_RELEASABLE);
    }

    @Nullable
    public static long[] toJvmKLongArray(@NotNull MemorySegment struct) {
        if(struct.address() == 0L) return null;
        struct = struct.reinterpret(layoutArray.getSize());
        long[] result = new long[getLength(struct)];
        copyElements(struct, result, result.length, JAVA_LONG);
        return result;
    }

    // Array: float

    @NotNull
    public static MemorySegment toNativeKFloatArrayDirect(@Nullable float[] arr) {
        if(arr == null) return MemorySegment.NULL;
        return MemorySegment.ofArray(arr);
    }

    @NotNull
    public static MemorySegment toNativeKFloatArrayOnArena(@NotNull Arena arena, @Nullable float[] arr) {
        if(arr == null) return MemorySegment.NULL;
        MemorySegment dataMem = arena.allocateFrom(JAVA_FLOAT, arr);
        return fillArray(arena.allocate(layoutArray.getSize()), dataMem, arr.length, FLAG_ON_STACK);
    }

    @NotNull
    public static MemorySegment toNativeKFloatArray(@Nullable float[] arr) {
        if(arr == null) return MemorySegment.NULL;
        MemorySegment dataMem = malloc(JAVA_FLOAT.byteSize() * arr.length);
        MemorySegment.copy(arr, 0, dataMem, JAVA_FLOAT, 0, arr.length);
        return fillArray(malloc(layoutArray.getSize()), dataMem, arr.length, FLAG_RELEASABLE);
    }

    @Nullable
    public static float[] toJvmKFloatArray(@NotNull MemorySegment struct) {
        if(struct.address() == 0L) return null;
        struct = struct.reinterpret(layoutArray.getSize());
        float[] result = new float[getLength(struct)];
        copyElements(struct, result, result.length, JAVA_FLOAT);
        return result;
    }

    // Array: double

    @NotNull
    public static MemorySegment toNativeKDoubleArrayDirect(@Nullable double[] arr) {
        if(arr == null) return MemorySegment.NULL;
        return MemorySegment.ofArray(arr);
    }

    @NotNull
    public static MemorySegment toNativeKDoubleArrayOnArena(@NotNull Arena arena, @Nullable double[] arr) {
        if(arr == null) return MemorySegment.NULL;
        MemorySegment dataMem = arena.allocateFrom(JAVA_DOUBLE, arr);
        return fillArray(arena.allocate(layoutArray.getSize()), dataMem, arr.length, FLAG_ON_STACK);
    }

    @NotNull
    public static MemorySegment toNativeKDoubleArray(@Nullable double[] arr) {
        if(arr == null) return MemorySegment.NULL;
        MemorySegment dataMem = malloc(JAVA_DOUBLE.byteSize() * arr.length);
        MemorySegment.copy(arr, 0, dataMem, JAVA_DOUBLE, 0, arr.length);
        return fillArray(malloc(layoutArray.getSize()), dataMem, arr.length, FLAG_RELEASABLE);
    }

    @Nullable
    public static double[] toJvmKDoubleArray(@NotNull MemorySegment struct) {
        if(struct.address() == 0L) return null;
        struct = struct.reinterpret(layoutArray.getSize());
        double[] result = new double[getLength(struct)];
        copyElements(struct, result, result.length, JAVA_DOUBLE);
        return result;
    }

    // Array: enum

    @NotNull
    public static <T extends Enum<T>> MemorySegment toNativeEnumArrayDirect(@Nullable T[] arr) {
        if(arr == null) return MemorySegment.NULL;
        int[] intEnums = new int[arr.length];
        for(int i = 0; i < arr.length; i++)
            intEnums[i] = arr[i].ordinal();
        return MemorySegment.ofArray(intEnums);
    }

    @NotNull
    public static <T extends Enum<T>> MemorySegment toNativeEnumArrayOnArena(@NotNull Arena arena, @Nullable T[] arr) {
        if(arr == null) return MemorySegment.NULL;
        int[] intEnums = new int[arr.length];
        for(int i = 0; i < arr.length; i++)
            intEnums[i] = arr[i].ordinal();
        return toNativeKIntArrayOnArena(arena, intEnums);
    }

    @NotNull
    public static <T extends Enum<T>> MemorySegment toNativeEnumArray(@Nullable T[] arr) {
        if(arr == null) return MemorySegment.NULL;
        int[] intEnums = new int[arr.length];
        for(int i = 0; i < arr.length; i++)
            intEnums[i] = arr[i].ordinal();
        return toNativeKIntArray(intEnums);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends Enum<T>> T[] toJvmEnumArray(
            @NotNull MemorySegment struct,
            @NotNull Class<T> enumClass
    ) {
        if(struct.address() == 0L) return null;
        int[] ordinals = toJvmKIntArray(struct);

        // Convert integers to enum values
        T[] enumConstants = enumClass.getEnumConstants();
        T[] result = (T[]) java.lang.reflect.Array.newInstance(enumClass, ordinals.length);
        for(int i = 0; i < ordinals.length; i++)
            result[i] = enumConstants[ordinals[i]];
        return result;
    }

    // Array: object
    @NotNull
    public static <T> MemorySegment toNativeKArrayOnArena(
            @NotNull Arena arena,
            @Nullable T[] arr,
            @NotNull BiFunction<Arena, T, MemorySegment> cast
    ) {
        if(arr == null) return MemorySegment.NULL;
        MemorySegment dataMem = arena.allocate(ADDRESS, arr.length);
        VarHandle ptrHandle = ADDRESS.arrayElementVarHandle();
        for(int i = 0; i < arr.length; i++)
            ptrHandle.set(dataMem, 0L, (long)i, cast.apply(arena, arr[i]));
        return fillArray(arena.allocate(layoutArray.getSize()), dataMem, arr.length, FLAG_ON_STACK);
    }

    @NotNull
    public static <T> MemorySegment toNativeKArray(
            @Nullable T[] arr,
            @NotNull Function<T, MemorySegment> cast
    ) {
        if(arr == null) return MemorySegment.NULL;
        MemorySegment dataMem = malloc(ADDRESS.byteSize() * arr.length);
        VarHandle ptrHandle = ADDRESS.arrayElementVarHandle();
        for(int i = 0; i < arr.length; i++)
            ptrHandle.set(dataMem, 0L, (long)i, cast.apply(arr[i]));
        return fillArray(malloc(layoutArray.getSize()), dataMem, arr.length, FLAG_RELEASABLE);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T[] toJvmKArray(
            @NotNull MemorySegment struct,
            @NotNull Function<MemorySegment, T> cast,
            @NotNull Class<T> clazz
    ) {
        if(struct.address() == 0L) return null;
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

    @NotNull
    public static MemorySegment createCallbackOnArena(
            @NotNull Arena arena,
            @Nullable Object callback,
            @NotNull MemorySegment upcall
    ){
        if(callback == null) return MemorySegment.NULL;
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

    @NotNull
    public static MemorySegment createCallback(
            @Nullable Object callback,
            @NotNull MemorySegment upcall
    ) {
        if(callback == null) return MemorySegment.NULL;
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
    @Nullable
    public static <T> T toJvmCallback(@NotNull MemorySegment segment) {
        if(segment.address() == 0L) return null;
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
