@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.cpp.arrays

import natives.test.cpp.MyDictionary
import natives.test.cpp.MyEnum
import withCppLib
import kotlin.test.Test
import kotlin.test.assertContentEquals

class Cpp_ReturnArray {

    @Test
    fun returnCharArray() = withCppLib {
        assertContentEquals(charArrayOf('a', 'b'), natives.test.cpp.returnCharArray())
    }

    @Test
    fun returnCharArrayEmpty() = withCppLib {
        assertContentEquals(charArrayOf(), natives.test.cpp.returnCharArrayEmpty())
    }

    @Test
    fun returnCharArrayN() = withCppLib {
        assertContentEquals(null, natives.test.cpp.returnCharArrayN())
    }

    @Test
    fun returnCharNArray() = withCppLib {
        assertContentEquals(arrayOf(null, 'a'), natives.test.cpp.returnCharNArray())
    }

    @Test
    fun returnBooleanArray() = withCppLib {
        assertContentEquals(booleanArrayOf(true, false), natives.test.cpp.returnBooleanArray())
    }

    @Test
    fun returnBooleanNArray() = withCppLib {
        assertContentEquals(arrayOf(null, true), natives.test.cpp.returnBooleanNArray())
    }

    @Test
    fun returnByteArray() = withCppLib {
        assertContentEquals(byteArrayOf(1, 2), natives.test.cpp.returnByteArray())
    }

    @Test
    fun returnByteNArray() = withCppLib {
        assertContentEquals(arrayOf(null, 1), natives.test.cpp.returnByteNArray())
    }

    @Test
    fun returnUByteArray() = withCppLib {
        assertContentEquals(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE), natives.test.cpp.returnUbyteArray())
    }

    @Test
    fun returnUByteNArray() = withCppLib {
        assertContentEquals(arrayOf(null, UByte.MAX_VALUE), natives.test.cpp.returnUbyteNArray())
    }

    @Test
    fun returnShortArray() = withCppLib {
        assertContentEquals(shortArrayOf(1, 2), natives.test.cpp.returnShortArray())
    }

    @Test
    fun returnShortNArray() = withCppLib {
        assertContentEquals(arrayOf(null, 1), natives.test.cpp.returnShortNArray())
    }

    @Test
    fun returnUShortArray() = withCppLib {
        assertContentEquals(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE), natives.test.cpp.returnUshortArray())
    }

    @Test
    fun returnUShortNArray() = withCppLib {
        assertContentEquals(arrayOf(null, UShort.MAX_VALUE), natives.test.cpp.returnUshortNArray())
    }

    @Test
    fun returnIntArray() = withCppLib {
        assertContentEquals(intArrayOf(1, 2), natives.test.cpp.returnIntArray())
    }

    @Test
    fun returnIntNArray() = withCppLib {
        assertContentEquals(arrayOf(null, 1), natives.test.cpp.returnIntNArray())
    }

    @Test
    fun returnUIntArray() = withCppLib {
        assertContentEquals(uintArrayOf(1.toUInt(), UInt.MAX_VALUE), natives.test.cpp.returnUintArray())
    }

    @Test
    fun returnUIntNArray() = withCppLib {
        assertContentEquals(arrayOf(null, UInt.MAX_VALUE), natives.test.cpp.returnUintNArray())
    }

    @Test
    fun returnLongArray() = withCppLib {
        assertContentEquals(longArrayOf(1, 2), natives.test.cpp.returnLongArray())
    }

    @Test
    fun returnLongNArray() = withCppLib {
        assertContentEquals(arrayOf(null, 1), natives.test.cpp.returnLongNArray())
    }

    @Test
    fun returnULongArray() = withCppLib {
        assertContentEquals(ulongArrayOf(1.toULong(), ULong.MAX_VALUE), natives.test.cpp.returnUlongArray())
    }

    @Test
    fun returnULongNArray() = withCppLib {
        assertContentEquals(arrayOf(null, ULong.MAX_VALUE), natives.test.cpp.returnUlongNArray())
    }

    @Test
    fun returnFloatArray() = withCppLib {
        assertContentEquals(floatArrayOf(1.1f, 2.2f), natives.test.cpp.returnFloatArray())
    }

    @Test
    fun returnFloatNArray() = withCppLib {
        assertContentEquals(arrayOf(null, 1.1f), natives.test.cpp.returnFloatNArray())
    }

    @Test
    fun returnDoubleArray() = withCppLib {
        assertContentEquals(doubleArrayOf(1.1, 2.2), natives.test.cpp.returnDoubleArray())
    }

    @Test
    fun returnDoubleNArray() = withCppLib {
        assertContentEquals(arrayOf(null, 1.1), natives.test.cpp.returnDoubleNArray())
    }

    @Test
    fun returnStringArray() = withCppLib {
        assertContentEquals(arrayOf("string1", "string2"), natives.test.cpp.returnStringArray())
    }

    @Test
    fun returnStringArrayN() = withCppLib {
        assertContentEquals(arrayOf(null, null), natives.test.cpp.returnStringArrayN())
    }

    @Test
    fun returnEnumArray() = withCppLib {
        assertContentEquals(arrayOf(MyEnum.CASE1, MyEnum.CASE2), natives.test.cpp.returnEnumArray())
    }

    @Test
    fun returnEnumNArray() = withCppLib {
        assertContentEquals(arrayOf(null, MyEnum.CASE2), natives.test.cpp.returnEnumNArray())
    }

    @Test
    fun returnDictionaryArray() = withCppLib {
        assertContentEquals(
            arrayOf(
                MyDictionary(1, 2, 3, 4),
                MyDictionary(5, 6, 7, 8)
            ), natives.test.cpp.returnDictionaryArray()
        )
    }

    @Test
    fun returnDictionaryArrayN() = withCppLib {
        assertContentEquals(arrayOf(null, null), natives.test.cpp.returnDictionaryArrayN())
    }
}