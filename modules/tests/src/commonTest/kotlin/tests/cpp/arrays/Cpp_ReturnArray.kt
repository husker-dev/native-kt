@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.cpp.arrays

import natives.testcpp.MyDictionary
import natives.testcpp.MyEnum
import withCppLib
import kotlin.test.Test
import kotlin.test.assertContentEquals

class Cpp_ReturnArray {

    @Test
    fun returnCharArray() = withCppLib {
        assertContentEquals(charArrayOf('a', 'b'), natives.testcpp.returnCharArray())
    }

    @Test
    fun returnCharArrayEmpty() = withCppLib {
        assertContentEquals(charArrayOf(), natives.testcpp.returnCharArrayEmpty())
    }

    @Test
    fun returnCharArrayN() = withCppLib {
        assertContentEquals(null, natives.testcpp.returnCharArrayN())
    }

    @Test
    fun returnCharNArray() = withCppLib {
        assertContentEquals(arrayOf(null, 'a'), natives.testcpp.returnCharNArray())
    }

    @Test
    fun returnBooleanArray() = withCppLib {
        assertContentEquals(booleanArrayOf(true, false), natives.testcpp.returnBooleanArray())
    }

    @Test
    fun returnBooleanNArray() = withCppLib {
        assertContentEquals(arrayOf(null, true), natives.testcpp.returnBooleanNArray())
    }

    @Test
    fun returnByteArray() = withCppLib {
        assertContentEquals(byteArrayOf(1, 2), natives.testcpp.returnByteArray())
    }

    @Test
    fun returnByteNArray() = withCppLib {
        assertContentEquals(arrayOf(null, 1), natives.testcpp.returnByteNArray())
    }

    @Test
    fun returnUByteArray() = withCppLib {
        assertContentEquals(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE), natives.testcpp.returnUByteArray())
    }

    @Test
    fun returnUByteNArray() = withCppLib {
        assertContentEquals(arrayOf(null, UByte.MAX_VALUE), natives.testcpp.returnUByteNArray())
    }

    @Test
    fun returnShortArray() = withCppLib {
        assertContentEquals(shortArrayOf(1, 2), natives.testcpp.returnShortArray())
    }

    @Test
    fun returnShortNArray() = withCppLib {
        assertContentEquals(arrayOf(null, 1), natives.testcpp.returnShortNArray())
    }

    @Test
    fun returnUShortArray() = withCppLib {
        assertContentEquals(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE), natives.testcpp.returnUShortArray())
    }

    @Test
    fun returnUShortNArray() = withCppLib {
        assertContentEquals(arrayOf(null, UShort.MAX_VALUE), natives.testcpp.returnUShortNArray())
    }

    @Test
    fun returnIntArray() = withCppLib {
        assertContentEquals(intArrayOf(1, 2), natives.testcpp.returnIntArray())
    }

    @Test
    fun returnIntNArray() = withCppLib {
        assertContentEquals(arrayOf(null, 1), natives.testcpp.returnIntNArray())
    }

    @Test
    fun returnUIntArray() = withCppLib {
        assertContentEquals(uintArrayOf(1.toUInt(), UInt.MAX_VALUE), natives.testcpp.returnUIntArray())
    }

    @Test
    fun returnUIntNArray() = withCppLib {
        assertContentEquals(arrayOf(null, UInt.MAX_VALUE), natives.testcpp.returnUIntNArray())
    }

    @Test
    fun returnLongArray() = withCppLib {
        assertContentEquals(longArrayOf(1, 2), natives.testcpp.returnLongArray())
    }

    @Test
    fun returnLongNArray() = withCppLib {
        assertContentEquals(arrayOf(null, 1), natives.testcpp.returnLongNArray())
    }

    @Test
    fun returnULongArray() = withCppLib {
        assertContentEquals(ulongArrayOf(1.toULong(), ULong.MAX_VALUE), natives.testcpp.returnULongArray())
    }

    @Test
    fun returnULongNArray() = withCppLib {
        assertContentEquals(arrayOf(null, ULong.MAX_VALUE), natives.testcpp.returnULongNArray())
    }

    @Test
    fun returnFloatArray() = withCppLib {
        assertContentEquals(floatArrayOf(1.1f, 2.2f), natives.testcpp.returnFloatArray())
    }

    @Test
    fun returnFloatNArray() = withCppLib {
        assertContentEquals(arrayOf(null, 1.1f), natives.testcpp.returnFloatNArray())
    }

    @Test
    fun returnDoubleArray() = withCppLib {
        assertContentEquals(doubleArrayOf(1.1, 2.2), natives.testcpp.returnDoubleArray())
    }

    @Test
    fun returnDoubleNArray() = withCppLib {
        assertContentEquals(arrayOf(null, 1.1), natives.testcpp.returnDoubleNArray())
    }

    @Test
    fun returnStringArray() = withCppLib {
        assertContentEquals(arrayOf("string1", "string2"), natives.testcpp.returnStringArray())
    }

    @Test
    fun returnStringArrayN() = withCppLib {
        assertContentEquals(arrayOf(null, null), natives.testcpp.returnStringArrayN())
    }

    @Test
    fun returnEnumArray() = withCppLib {
        assertContentEquals(arrayOf(MyEnum.CASE1, MyEnum.CASE2), natives.testcpp.returnEnumArray())
    }

    @Test
    fun returnEnumNArray() = withCppLib {
        assertContentEquals(arrayOf(null, MyEnum.CASE2), natives.testcpp.returnEnumNArray())
    }

    @Test
    fun returnDictionaryArray() = withCppLib {
        assertContentEquals(
            arrayOf(
                MyDictionary(1, 2, 3, 4),
                MyDictionary(5, 6, 7, 8)
            ), natives.testcpp.returnDictionaryArray()
        )
    }

    @Test
    fun returnDictionaryArrayN() = withCppLib {
        assertContentEquals(arrayOf(null, null), natives.testcpp.returnDictionaryArrayN())
    }
}