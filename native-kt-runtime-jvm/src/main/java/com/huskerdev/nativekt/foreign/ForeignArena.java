package com.huskerdev.nativekt.foreign;

import java.io.Closeable;
import java.lang.foreign.*;
import java.util.ArrayList;

import static com.huskerdev.nativekt.foreign.ForeignUtils.fromKString;

@SuppressWarnings("unused")
public class ForeignArena implements Closeable {

    public final Arena heap = Arena.ofConfined();
    private final ArrayList<Long> allocated = new ArrayList<>();
    private final ArrayList<MemorySegment> callbacks = new ArrayList<>();

    private boolean notContains(long address) {
        for(Long segment : allocated)
            if(segment == address)
                return false;
        return true;
    }

    // String

    public MemorySegment cstr(String of) {
        MemorySegment struct = heap.allocate(ForeignUtils.STRING_STRUCT);
        MemorySegment data = heap.allocateFrom(of);
        ForeignUtils.stringDataVarHandle.set(struct, 0L, data);
        ForeignUtils.stringLengthVarHandle.set(struct, 0L, of.length());

        allocated.add(data.address());
        return struct;
    }

    public String asString(MemorySegment struct, boolean dealloc) throws Throwable {
        MemorySegment data = (MemorySegment)ForeignUtils.stringDataVarHandle.get(struct, 0L);

        String result = fromKString(struct);
        if(dealloc && notContains(data.address()))
            ForeignUtils.freeHandle.invoke(data);
        return result;
    }

    // Array: char

    public MemorySegment toNativeCharArray(char[] arr) {
        MemorySegment struct = ForeignUtils.toNativeCharArray(arr, heap, heap);
        allocated.add(ForeignUtils.arrayElementsAddress(struct));
        return struct;
    }

    public char[] toJvmCharArray(MemorySegment struct, boolean dealloc) throws Throwable {
        long address = ForeignUtils.arrayElementsAddress(struct);
        return ForeignUtils.toJvmCharArray(struct, dealloc && notContains(address));
    }

    // Array: boolean

    public MemorySegment toNativeBooleanArray(boolean[] arr) {
        MemorySegment struct = ForeignUtils.toNativeBooleanArray(arr, heap, heap);
        allocated.add(ForeignUtils.arrayElementsAddress(struct));
        return struct;
    }

    public boolean[] toJvmBooleanArray(MemorySegment struct, boolean dealloc) throws Throwable {
        long address = ForeignUtils.arrayElementsAddress(struct);
        return ForeignUtils.toJvmBooleanArray(struct, dealloc && notContains(address));
    }

    // Array: byte

    public MemorySegment toNativeByteArray(byte[] arr) {
        MemorySegment struct = ForeignUtils.toNativeByteArray(arr, heap, heap);
        allocated.add(ForeignUtils.arrayElementsAddress(struct));
        return struct;
    }

    public byte[] toJvmByteArray(MemorySegment struct, boolean dealloc) throws Throwable {
        long address = ForeignUtils.arrayElementsAddress(struct);
        return ForeignUtils.toJvmByteArray(struct, dealloc && notContains(address));
    }

    // Array: short

    public MemorySegment toNativeShortArray(short[] arr) {
        MemorySegment struct = ForeignUtils.toNativeShortArray(arr, heap, heap);
        allocated.add(ForeignUtils.arrayElementsAddress(struct));
        return struct;
    }

    public short[] toJvmShortArray(MemorySegment struct, boolean dealloc) throws Throwable {
        long address = ForeignUtils.arrayElementsAddress(struct);
        return ForeignUtils.toJvmShortArray(struct, dealloc && notContains(address));
    }

    // Array: int

    public MemorySegment toNativeIntArray(int[] arr) {
        MemorySegment struct = ForeignUtils.toNativeIntArray(arr, heap, heap);
        allocated.add(ForeignUtils.arrayElementsAddress(struct));
        return struct;
    }

    public int[] toJvmIntArray(MemorySegment struct, boolean dealloc) throws Throwable {
        long address = ForeignUtils.arrayElementsAddress(struct);
        return ForeignUtils.toJvmIntArray(struct, dealloc && notContains(address));
    }

    // Array: long

    public MemorySegment toNativeLongArray(long[] arr) {
        MemorySegment struct = ForeignUtils.toNativeLongArray(arr, heap, heap);
        allocated.add(ForeignUtils.arrayElementsAddress(struct));
        return struct;
    }

    public long[] toJvmLongArray(MemorySegment struct, boolean dealloc) throws Throwable {
        long address = ForeignUtils.arrayElementsAddress(struct);
        return ForeignUtils.toJvmLongArray(struct, dealloc && notContains(address));
    }

    // Array: float

    public MemorySegment toNativeFloatArray(float[] arr) {
        MemorySegment struct = ForeignUtils.toNativeFloatArray(arr, heap, heap);
        allocated.add(ForeignUtils.arrayElementsAddress(struct));
        return struct;
    }

    public float[] toJvmFloatArray(MemorySegment struct, boolean dealloc) throws Throwable {
        long address = ForeignUtils.arrayElementsAddress(struct);
        return ForeignUtils.toJvmFloatArray(struct, dealloc && notContains(address));
    }

    // Array: double

    public MemorySegment toNativeDoubleArray(double[] arr) {
        MemorySegment struct = ForeignUtils.toNativeDoubleArray(arr, heap, heap);
        allocated.add(ForeignUtils.arrayElementsAddress(struct));
        return struct;
    }

    public double[] toJvmDoubleArray(MemorySegment struct, boolean dealloc) throws Throwable {
        long address = ForeignUtils.arrayElementsAddress(struct);
        return ForeignUtils.toJvmDoubleArray(struct, dealloc && notContains(address));
    }

    // Callbacks

    public MemorySegment callback(MemorySegment callback) {
        allocated.add(callback.address());
        callbacks.add(callback);
        return callback;
    }

    @SuppressWarnings("unchecked")
    public <T> T asCallback(MemorySegment segment, boolean dealloc) throws Throwable {
        Object result = ForeignUtils.callbacks.get(segment.address());
        if(dealloc && notContains(segment.address()))
            ForeignUtils.freeHandle.invoke(segment);
        return (T) result;
    }

    public void close() {
        for(MemorySegment callback : callbacks)
            ForeignUtils.callbackFree(callback);
        heap.close();
    }
}
