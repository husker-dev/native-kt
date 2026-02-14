package com.huskerdev.nativekt.foreign;

import java.io.Closeable;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class CustomForeignArena implements Closeable {

    private final Arena arena = Arena.ofConfined();
    private final ArrayList<MemorySegment> allocated = new ArrayList<>();

    private final Linker linker = Linker.nativeLinker();

    private final MethodHandle freeHandle = linker.downcallHandle(
            linker.defaultLookup().find("free").get(),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );

    private boolean contains(long address) {
        for(MemorySegment segment : allocated)
            if(segment.address() == address)
                return true;
        return false;
    }

    public MemorySegment cstr(String of) {
        MemorySegment result = arena.allocateFrom(of);
        allocated.add(result);
        return result;
    }

    public String asString(MemorySegment segment, boolean dealloc) throws Throwable {
        String result = segment.reinterpret(Long.MAX_VALUE).getString(0);
        if(dealloc && contains(segment.address()))
            freeHandle.invoke(this);
        return result;
    }

    public void close() {
        arena.close();
    }
}
