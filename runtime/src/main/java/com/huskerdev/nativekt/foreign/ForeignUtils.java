package com.huskerdev.nativekt.foreign;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

@SuppressWarnings("unused")
public class ForeignUtils {

    public static ValueLayout C_CHAR = ValueLayout.JAVA_CHAR;
    public static ValueLayout C_BOOLEAN = ValueLayout.JAVA_BOOLEAN;
    public static ValueLayout C_SHORT = ValueLayout.JAVA_SHORT;
    public static ValueLayout C_INT = ValueLayout.JAVA_INT;
    public static ValueLayout C_LONG = ValueLayout.JAVA_LONG;
    public static ValueLayout C_FLOAT = ValueLayout.JAVA_FLOAT;
    public static ValueLayout C_DOUBLE = ValueLayout.JAVA_DOUBLE;
    public static ValueLayout C_ADDRESS = ValueLayout.ADDRESS;

    public static MemorySegment heapStr(String str) {
        return MemorySegment.ofArray(str.getBytes());
    }

    public static MethodHandle lookup(String name, boolean isCritical, ValueLayout retType, ValueLayout... argTypes) {
        FunctionDescriptor function = retType == null ?
                FunctionDescriptor.ofVoid(argTypes) :
                FunctionDescriptor.of(retType, argTypes);

        MemorySegment address = SymbolLookup.loaderLookup().findOrThrow(name);

        if(isCritical)
            return Linker.nativeLinker().downcallHandle(address, function, Linker.Option.critical(true));
        else
            return Linker.nativeLinker().downcallHandle(address, function);
    }
}
