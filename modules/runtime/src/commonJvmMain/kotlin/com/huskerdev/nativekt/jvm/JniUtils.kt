package com.huskerdev.nativekt.jvm

import java.lang.reflect.Method

object JniUtils {
    fun getSignature(method: Method) = buildString {
        append("(")
        method.parameterTypes.forEach {
            append(getDescriptor(it))
        }
        append(")")
        append(getDescriptor(method.returnType))
    }

    private fun getDescriptor(clazz: Class<*>): String = when {
        clazz.isPrimitive -> when (clazz) {
            Void.TYPE -> "V"
            Int::class.javaPrimitiveType -> "I"
            Boolean::class.javaPrimitiveType -> "Z"
            Byte::class.javaPrimitiveType -> "B"
            Char::class.javaPrimitiveType -> "C"
            Short::class.javaPrimitiveType -> "S"
            Double::class.javaPrimitiveType -> "D"
            Float::class.javaPrimitiveType -> "F"
            Long::class.javaPrimitiveType -> "J"
            else -> throw IllegalArgumentException("Unknown primitive: $clazz")
        }
        clazz.isArray -> clazz.getName().replace('.', '/')
        else -> "L${clazz.getName().replace('.', '/')};"
    }

    fun bytesToStrings(arr: Array<ByteArray?>?): Array<String?>? {
        if(arr == null)
            return null
        return Array(arr.size) { arr[it]?.decodeToString() }
    }

    fun stringsToBytes(arr: Array<String?>?): Array<ByteArray?>? {
        if(arr == null)
            return null
        return Array(arr.size) { arr[it]?.encodeToByteArray() }
    }

    @Suppress("unchecked_cast")
    @JvmName("bytesToStringsNotNull")
    fun bytesToStrings(arr: Array<ByteArray>?): Array<String>? =
        bytesToStrings(arr as Array<ByteArray?>?) as Array<String>?

    @Suppress("unchecked_cast")
    @JvmName("stringsToBytesNotNull")
    fun stringsToBytes(arr: Array<String>?): Array<ByteArray>? =
        stringsToBytes(arr as Array<String?>?) as Array<ByteArray>?
}