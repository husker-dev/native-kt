@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.c.arrays

import withCLib
import natives.test.MyDictionary
import natives.test.MyEnum
import kotlin.test.Test
import kotlin.test.assertContentEquals

class C_ReturnArray {

    @Test
    fun returnCharArray() = withCLib {
        assertContentEquals(charArrayOf('a', 'b'), natives.test.returnCharArray())
    }

    @Test
    fun returnCharArrayEmpty() = withCLib {
        assertContentEquals(charArrayOf(), natives.test.returnCharArrayEmpty())
    }

    @Test
    fun returnCharArrayN() = withCLib {
        assertContentEquals(null, natives.test.returnCharArrayN())
    }

    @Test
    fun returnCharNArray() = withCLib {
        assertContentEquals(arrayOf(null, 'a'), natives.test.returnCharNArray())
    }

    @Test
    fun returnBooleanArray() = withCLib {
        assertContentEquals(booleanArrayOf(true, false), natives.test.returnBooleanArray())
    }

    @Test
    fun returnBooleanNArray() = withCLib {
        assertContentEquals(arrayOf(null, true), natives.test.returnBooleanNArray())
    }

    @Test
    fun returnByteArray() = withCLib {
        assertContentEquals(byteArrayOf(1, 2), natives.test.returnByteArray())
    }

    @Test
    fun returnByteNArray() = withCLib {
        assertContentEquals(arrayOf(null, 1), natives.test.returnByteNArray())
    }

    @Test
    fun returnUByteArray() = withCLib {
        assertContentEquals(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE), natives.test.returnUByteArray())
    }

    @Test
    fun returnUByteNArray() = withCLib {
        assertContentEquals(arrayOf(null, UByte.MAX_VALUE), natives.test.returnUByteNArray())
    }

    @Test
    fun returnShortArray() = withCLib {
        assertContentEquals(shortArrayOf(1, 2), natives.test.returnShortArray())
    }

    @Test
    fun returnShortNArray() = withCLib {
        assertContentEquals(arrayOf(null, 1), natives.test.returnShortNArray())
    }

    @Test
    fun returnUShortArray() = withCLib {
        assertContentEquals(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE), natives.test.returnUShortArray())
    }

    @Test
    fun returnUShortNArray() = withCLib {
        assertContentEquals(arrayOf(null, UShort.MAX_VALUE), natives.test.returnUShortNArray())
    }

    @Test
    fun returnIntArray() = withCLib {
        assertContentEquals(intArrayOf(1, 2), natives.test.returnIntArray())
    }

    @Test
    fun returnIntNArray() = withCLib {
        assertContentEquals(arrayOf(null, 1), natives.test.returnIntNArray())
    }

    @Test
    fun returnUIntArray() = withCLib {
        assertContentEquals(uintArrayOf(1.toUInt(), UInt.MAX_VALUE), natives.test.returnUIntArray())
    }

    @Test
    fun returnUIntNArray() = withCLib {
        assertContentEquals(arrayOf(null, UInt.MAX_VALUE), natives.test.returnUIntNArray())
    }

    @Test
    fun returnLongArray() = withCLib {
        assertContentEquals(longArrayOf(1, 2), natives.test.returnLongArray())
    }

    @Test
    fun returnLongNArray() = withCLib {
        assertContentEquals(arrayOf(null, 1), natives.test.returnLongNArray())
    }

    @Test
    fun returnULongArray() = withCLib {
        assertContentEquals(ulongArrayOf(1.toULong(), ULong.MAX_VALUE), natives.test.returnULongArray())
    }

    @Test
    fun returnULongNArray() = withCLib {
        assertContentEquals(arrayOf(null, ULong.MAX_VALUE), natives.test.returnULongNArray())
    }

    @Test
    fun returnFloatArray() = withCLib {
        assertContentEquals(floatArrayOf(1.1f, 2.2f), natives.test.returnFloatArray())
    }

    @Test
    fun returnFloatNArray() = withCLib {
        assertContentEquals(arrayOf(null, 1.1f), natives.test.returnFloatNArray())
    }

    @Test
    fun returnDoubleArray() = withCLib {
        assertContentEquals(doubleArrayOf(1.1, 2.2), natives.test.returnDoubleArray())
    }

    @Test
    fun returnDoubleNArray() = withCLib {
        assertContentEquals(arrayOf(null, 1.1), natives.test.returnDoubleNArray())
    }

    @Test
    fun returnStringArray() = withCLib {
        assertContentEquals(arrayOf("string1", "string2"), natives.test.returnStringArray())
    }

    @Test
    fun returnStringArrayN() = withCLib {
        assertContentEquals(arrayOf(null, null), natives.test.returnStringArrayN())
    }

    @Test
    fun returnEnumArray() = withCLib {
        assertContentEquals(arrayOf(MyEnum.CASE1, MyEnum.CASE2), natives.test.returnEnumArray())
    }

    @Test
    fun returnEnumNArray() = withCLib {
        assertContentEquals(arrayOf(null, MyEnum.CASE2), natives.test.returnEnumNArray())
    }

    @Test
    fun returnDictionaryArray() = withCLib {
        assertContentEquals(
            arrayOf(
                MyDictionary(1, 2, 3, 4),
                MyDictionary(5, 6, 7, 8)
            ), natives.test.returnDictionaryArray()
        )
    }

    @Test
    fun returnDictionaryArrayN() = withCLib {
        assertContentEquals(arrayOf(null, null), natives.test.returnDictionaryArrayN())
    }
}