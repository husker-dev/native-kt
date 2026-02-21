package com.huskerdev.nativekt.foreign;

import java.io.Closeable;
import java.lang.foreign.*;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class ForeignArena implements Closeable {

    private final Arena arena = Arena.ofConfined();
    private final ArrayList<MemorySegment> allocated = new ArrayList<>();
    private final ArrayList<MemorySegment> callbacks = new ArrayList<>();

    private boolean notContains(long address) {
        for(MemorySegment segment : allocated)
            if(segment.address() == address)
                return false;
        return true;
    }

    public MemorySegment cstr(String of) {
        MemorySegment result = arena.allocateFrom(of);
        allocated.add(result);
        return result;
    }

    public MemorySegment callback(MemorySegment callback) {
        allocated.add(callback);
        callbacks.add(callback);
        return callback;
    }

    public String asString(MemorySegment segment, boolean dealloc) throws Throwable {
        String result = segment.reinterpret(Long.MAX_VALUE).getString(0);
        if(dealloc && notContains(segment.address()))
            ForeignUtils.freeHandle.invoke(segment);
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
        arena.close();
    }
}
