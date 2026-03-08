package com.huskerdev.nativekt.foreign;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

@SuppressWarnings("unused")
public class ForeignUtils {

    private static final Linker linker = Linker.nativeLinker();

    public static final StructLayout STRING_STRUCT = MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withName("data"),
            ValueLayout.JAVA_INT.withName("length"),
            MemoryLayout.paddingLayout(4) // align to 16
    );
    public static final VarHandle stringDataVarHandle = STRING_STRUCT.varHandle(MemoryLayout.PathElement.groupElement("data"));
    public static final VarHandle stringLengthVarHandle = STRING_STRUCT.varHandle(MemoryLayout.PathElement.groupElement("length"));

    public static final HashMap<Long, Object> callbacks = new HashMap<>();

    private static final StructLayout callbackStructLayout = MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withName("m"),
            ValueLayout.ADDRESS.withName("invoke"),
            ValueLayout.ADDRESS.withName("free")
    );
    private static final VarHandle callbackInvokeVarHandle = callbackStructLayout.varHandle(MemoryLayout.PathElement.groupElement("invoke"));
    private static final VarHandle callbackFreeVarHandle = callbackStructLayout.varHandle(MemoryLayout.PathElement.groupElement("free"));

    private static final MemorySegment callbackFree;

    public static ValueLayout C_CHAR = ValueLayout.JAVA_CHAR;
    public static ValueLayout C_BYTE = ValueLayout.JAVA_BYTE;
    public static ValueLayout C_BOOLEAN = ValueLayout.JAVA_BOOLEAN;
    public static ValueLayout C_SHORT = ValueLayout.JAVA_SHORT;
    public static ValueLayout C_INT = ValueLayout.JAVA_INT;
    public static ValueLayout C_LONG = ValueLayout.JAVA_LONG;
    public static ValueLayout C_FLOAT = ValueLayout.JAVA_FLOAT;
    public static ValueLayout C_DOUBLE = ValueLayout.JAVA_DOUBLE;
    public static ValueLayout C_ADDRESS = ValueLayout.ADDRESS;

    public static final MethodHandle freeHandle = linker.downcallHandle(
            linker.defaultLookup().find("free").orElseThrow(),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );


    public static MemorySegment heapStr(String str) {
        return MemorySegment.ofArray(str.getBytes());
    }

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

    public static void fillKString(MemorySegment struct, MemorySegment data, int length) {
        stringDataVarHandle.set(struct, 0L, data);
        stringLengthVarHandle.set(struct, 0L, length);
    }

    public static MemorySegment getKStringData(MemorySegment struct) {
        return (MemorySegment)stringDataVarHandle.get(struct, 0L);
    }

    public static int getKStringLength(MemorySegment struct) {
        return (int)stringLengthVarHandle.get(struct, 0L);
    }

    public static String fromKString(MemorySegment data, int length) {
        final byte[] bytes = new byte[length];
        MemorySegment.copy(data.reinterpret(length), JAVA_BYTE, 0, bytes, 0, length);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static MemorySegment cstr(String of) {
        MemorySegment struct = Arena.ofAuto().allocate(STRING_STRUCT);
        fillKString(struct, Arena.global().allocateFrom(of), of.length());
        return struct;
    }

    public static String asString(MemorySegment segment, boolean dealloc) throws Throwable {
        MemorySegment data = getKStringData(segment);
        int length = getKStringLength(segment);

        String result = fromKString(data, length);
        if(dealloc)
            freeHandle.invoke(data);
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T asCallback(MemorySegment segment, boolean dealloc) throws Throwable{
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
