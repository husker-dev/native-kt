@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.swift.arrays

import withSwiftLib
import natives.test.swift.MyDictionary
import natives.test.swift.MyEnum
import kotlin.test.Test
import kotlin.test.assertContentEquals

class Swift_ReturnArray {

    @Test
    fun returnCharArray() = withSwiftLib {
        assertContentEquals(charArrayOf('a', 'b'), natives.test.swift.returnCharArray())
    }

    @Test
    fun returnCharArrayEmpty() = withSwiftLib {
        assertContentEquals(charArrayOf(), natives.test.swift.returnCharArrayEmpty())
    }

    @Test
    fun returnCharArrayN() = withSwiftLib {
        assertContentEquals(null, natives.test.swift.returnCharArrayN())
    }

    @Test
    fun returnCharNArray() = withSwiftLib {
        assertContentEquals(arrayOf(null, 'a'), natives.test.swift.returnCharNArray())
    }

    @Test
    fun returnBooleanArray() = withSwiftLib {
        assertContentEquals(booleanArrayOf(true, false), natives.test.swift.returnBooleanArray())
    }

    @Test
    fun returnBooleanNArray() = withSwiftLib {
        assertContentEquals(arrayOf(null, true), natives.test.swift.returnBooleanNArray())
    }

    @Test
    fun returnByteArray() = withSwiftLib {
        assertContentEquals(byteArrayOf(1, 2), natives.test.swift.returnByteArray())
    }

    @Test
    fun returnByteNArray() = withSwiftLib {
        assertContentEquals(arrayOf(null, 1), natives.test.swift.returnByteNArray())
    }

    @Test
    fun returnUByteArray() = withSwiftLib {
        assertContentEquals(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE), natives.test.swift.returnUbyteArray())
    }

    @Test
    fun returnUByteNArray() = withSwiftLib {
        assertContentEquals(arrayOf(null, UByte.MAX_VALUE), natives.test.swift.returnUbyteNArray())
    }

    @Test
    fun returnShortArray() = withSwiftLib {
        assertContentEquals(shortArrayOf(1, 2), natives.test.swift.returnShortArray())
    }

    @Test
    fun returnShortNArray() = withSwiftLib {
        assertContentEquals(arrayOf(null, 1), natives.test.swift.returnShortNArray())
    }

    @Test
    fun returnUShortArray() = withSwiftLib {
        assertContentEquals(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE), natives.test.swift.returnUshortArray())
    }

    @Test
    fun returnUShortNArray() = withSwiftLib {
        assertContentEquals(arrayOf(null, UShort.MAX_VALUE), natives.test.swift.returnUshortNArray())
    }

    @Test
    fun returnIntArray() = withSwiftLib {
        assertContentEquals(intArrayOf(1, 2), natives.test.swift.returnIntArray())
    }

    @Test
    fun returnIntNArray() = withSwiftLib {
        assertContentEquals(arrayOf(null, 1), natives.test.swift.returnIntNArray())
    }

    @Test
    fun returnUIntArray() = withSwiftLib {
        assertContentEquals(uintArrayOf(1.toUInt(), UInt.MAX_VALUE), natives.test.swift.returnUintArray())
    }

    @Test
    fun returnUIntNArray() = withSwiftLib {
        assertContentEquals(arrayOf(null, UInt.MAX_VALUE), natives.test.swift.returnUintNArray())
    }

    @Test
    fun returnLongArray() = withSwiftLib {
        assertContentEquals(longArrayOf(1, 2), natives.test.swift.returnLongArray())
    }

    @Test
    fun returnLongNArray() = withSwiftLib {
        assertContentEquals(arrayOf(null, 1), natives.test.swift.returnLongNArray())
    }

    @Test
    fun returnULongArray() = withSwiftLib {
        assertContentEquals(ulongArrayOf(1.toULong(), ULong.MAX_VALUE), natives.test.swift.returnUlongArray())
    }

    @Test
    fun returnULongNArray() = withSwiftLib {
        assertContentEquals(arrayOf(null, ULong.MAX_VALUE), natives.test.swift.returnUlongNArray())
    }

    @Test
    fun returnFloatArray() = withSwiftLib {
        assertContentEquals(floatArrayOf(1.1f, 2.2f), natives.test.swift.returnFloatArray())
    }

    @Test
    fun returnFloatNArray() = withSwiftLib {
        assertContentEquals(arrayOf(null, 1.1f), natives.test.swift.returnFloatNArray())
    }

    @Test
    fun returnDoubleArray() = withSwiftLib {
        assertContentEquals(doubleArrayOf(1.1, 2.2), natives.test.swift.returnDoubleArray())
    }

    @Test
    fun returnDoubleNArray() = withSwiftLib {
        assertContentEquals(arrayOf(null, 1.1), natives.test.swift.returnDoubleNArray())
    }

    @Test
    fun returnStringArray() = withSwiftLib {
        assertContentEquals(arrayOf("string1", "string2"), natives.test.swift.returnStringArray())
    }

    @Test
    fun returnStringArrayN() = withSwiftLib {
        assertContentEquals(arrayOf(null, null), natives.test.swift.returnStringArrayN())
    }

    @Test
    fun returnEnumArray() = withSwiftLib {
        assertContentEquals(arrayOf(MyEnum.CASE1, MyEnum.CASE2), natives.test.swift.returnEnumArray())
    }

    @Test
    fun returnEnumNArray() = withSwiftLib {
        assertContentEquals(arrayOf(null, MyEnum.CASE2), natives.test.swift.returnEnumNArray())
    }

    @Test
    fun returnDictionaryArray() = withSwiftLib {
        assertContentEquals(
            arrayOf(
                MyDictionary(1, 2, 3, 4),
                MyDictionary(5, 6, 7, 8)
            ), natives.test.swift.returnDictionaryArray()
        )
    }

    @Test
    fun returnDictionaryArrayN() = withSwiftLib {
        assertContentEquals(arrayOf(null, null), natives.test.swift.returnDictionaryArrayN())
    }
}