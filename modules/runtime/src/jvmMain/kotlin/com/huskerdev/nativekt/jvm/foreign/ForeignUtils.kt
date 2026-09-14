@file:Suppress("unused", "JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package com.huskerdev.nativekt.jvm.foreign

import java.lang.foreign.*
import java.lang.foreign.ValueLayout.*
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method


private val linker = Linker.nativeLinker()

fun lookup(
    lookup: SymbolLookup,
    name: String,
    isCritical: Boolean,
    retType: MemoryLayout?,
    vararg argTypes: MemoryLayout
): MethodHandle {
    val address = lookup.find(name).orElseThrow()

    val function = if (retType == null)
        FunctionDescriptor.ofVoid(*argTypes)
    else FunctionDescriptor.of(retType, *argTypes)

    return if (isCritical)
        linker.downcallHandle(address, function, Linker.Option.critical(true))
    else linker.downcallHandle(address, function)
}

fun upcall(target: Any): MemorySegment {
    val lookup = MethodHandles.privateLookupIn(target.javaClass, MethodHandles.lookup())
    val method = target.javaClass.declaredMethods.first { !it.isSynthetic }
    val methodHandle = lookup.unreflect(method).bindTo(target)
    val descriptor = descriptorFor(method)
    return linker.upcallStub(methodHandle, descriptor, Arena.ofAuto())
}

private fun descriptorFor(method: Method): FunctionDescriptor {
    val args = method.parameterTypes.map(::toLayout)
    val ret = method.returnType
    return if (ret == Void.TYPE)
        FunctionDescriptor.ofVoid(*args.toTypedArray())
    else FunctionDescriptor.of(toLayout(ret), *args.toTypedArray())
}

private fun toLayout(type: Class<*>): MemoryLayout = when (type) {
    MemorySegment::class.java -> ADDRESS
    Byte::class.javaPrimitiveType -> JAVA_BYTE
    Short::class.javaPrimitiveType -> JAVA_SHORT
    Int::class.javaPrimitiveType -> JAVA_INT
    Long::class.javaPrimitiveType -> JAVA_LONG
    Float::class.javaPrimitiveType -> JAVA_FLOAT
    Double::class.javaPrimitiveType -> JAVA_DOUBLE
    Char::class.javaPrimitiveType -> JAVA_CHAR
    Boolean::class.javaPrimitiveType -> JAVA_BOOLEAN
    else -> throw IllegalArgumentException("Unsupported type: ${type.name}")
}