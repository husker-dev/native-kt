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

    public MemorySegment cstr(String of) {
        MemorySegment struct = heap.allocate(ForeignUtils.STRING_STRUCT);
        MemorySegment data = heap.allocateFrom(of);
        ForeignUtils.fillKString(struct, data, of.length());

        allocated.add(data.address());
        return struct;
    }

    public MemorySegment callback(MemorySegment callback) {
        allocated.add(callback.address());
        callbacks.add(callback);
        return callback;
    }

    public String asString(MemorySegment segment, boolean dealloc) throws Throwable {
        MemorySegment data = ForeignUtils.getKStringData(segment);
        int length = ForeignUtils.getKStringLength(segment);

        String result = fromKString(data, length);
        if(dealloc && notContains(data.address()))
            ForeignUtils.freeHandle.invoke(data);
        return result;
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
