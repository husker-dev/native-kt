@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.c.arrays

import withCLib
import natives.test.c.MyDictionary
import natives.test.c.MyEnum
import kotlin.test.Test
import kotlin.test.assertContentEquals

class C_ReturnArray {

    @Test
    fun returnCharArray() = withCLib {
        assertContentEquals(charArrayOf('a', 'b'), natives.test.c.returnCharArray())
    }

    @Test
    fun returnCharArrayEmpty() = withCLib {
        assertContentEquals(charArrayOf(), natives.test.c.returnCharArrayEmpty())
    }

    @Test
    fun returnCharArrayN() = withCLib {
        assertContentEquals(null, natives.test.c.returnCharArrayN())
    }

    @Test
    fun returnCharNArray() = withCLib {
        assertContentEquals(arrayOf(null, 'a'), natives.test.c.returnCharNArray())
    }

    @Test
    fun returnBooleanArray() = withCLib {
        assertContentEquals(booleanArrayOf(true, false), natives.test.c.returnBooleanArray())
    }

    @Test
    fun returnBooleanNArray() = withCLib {
        assertContentEquals(arrayOf(null, true), natives.test.c.returnBooleanNArray())
    }

    @Test
    fun returnByteArray() = withCLib {
        assertContentEquals(byteArrayOf(1, 2), natives.test.c.returnByteArray())
    }

    @Test
    fun returnByteNArray() = withCLib {
        assertContentEquals(arrayOf(null, 1), natives.test.c.returnByteNArray())
    }

    @Test
    fun returnUByteArray() = withCLib {
        assertContentEquals(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE), natives.test.c.returnUbyteArray())
    }

    @Test
    fun returnUByteNArray() = withCLib {
        assertContentEquals(arrayOf(null, UByte.MAX_VALUE), natives.test.c.returnUbyteNArray())
    }

    @Test
    fun returnShortArray() = withCLib {
        assertContentEquals(shortArrayOf(1, 2), natives.test.c.returnShortArray())
    }

    @Test
    fun returnShortNArray() = withCLib {
        assertContentEquals(arrayOf(null, 1), natives.test.c.returnShortNArray())
    }

    @Test
    fun returnUShortArray() = withCLib {
        assertContentEquals(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE), natives.test.c.returnUshortArray())
    }

    @Test
    fun returnUShortNArray() = withCLib {
        assertContentEquals(arrayOf(null, UShort.MAX_VALUE), natives.test.c.returnUshortNArray())
    }

    @Test
    fun returnIntArray() = withCLib {
        assertContentEquals(intArrayOf(1, 2), natives.test.c.returnIntArray())
    }

    @Test
    fun returnIntNArray() = withCLib {
        assertContentEquals(arrayOf(null, 1), natives.test.c.returnIntNArray())
    }

    @Test
    fun returnUIntArray() = withCLib {
        assertContentEquals(uintArrayOf(1.toUInt(), UInt.MAX_VALUE), natives.test.c.returnUintArray())
    }

    @Test
    fun returnUIntNArray() = withCLib {
        assertContentEquals(arrayOf(null, UInt.MAX_VALUE), natives.test.c.returnUintNArray())
    }

    @Test
    fun returnLongArray() = withCLib {
        assertContentEquals(longArrayOf(1, 2), natives.test.c.returnLongArray())
    }

    @Test
    fun returnLongNArray() = withCLib {
        assertContentEquals(arrayOf(null, 1), natives.test.c.returnLongNArray())
    }

    @Test
    fun returnULongArray() = withCLib {
        assertContentEquals(ulongArrayOf(1.toULong(), ULong.MAX_VALUE), natives.test.c.returnUlongArray())
    }

    @Test
    fun returnULongNArray() = withCLib {
        assertContentEquals(arrayOf(null, ULong.MAX_VALUE), natives.test.c.returnUlongNArray())
    }

    @Test
    fun returnFloatArray() = withCLib {
        assertContentEquals(floatArrayOf(1.1f, 2.2f), natives.test.c.returnFloatArray())
    }

    @Test
    fun returnFloatNArray() = withCLib {
        assertContentEquals(arrayOf(null, 1.1f), natives.test.c.returnFloatNArray())
    }

    @Test
    fun returnDoubleArray() = withCLib {
        assertContentEquals(doubleArrayOf(1.1, 2.2), natives.test.c.returnDoubleArray())
    }

    @Test
    fun returnDoubleNArray() = withCLib {
        assertContentEquals(arrayOf(null, 1.1), natives.test.c.returnDoubleNArray())
    }

    @Test
    fun returnStringArray() = withCLib {
        assertContentEquals(arrayOf("string1", "string2"), natives.test.c.returnStringArray())
    }

    @Test
    fun returnStringArrayN() = withCLib {
        assertContentEquals(arrayOf(null, null), natives.test.c.returnStringArrayN())
    }

    @Test
    fun returnEnumArray() = withCLib {
        assertContentEquals(arrayOf(MyEnum.CASE1, MyEnum.CASE2), natives.test.c.returnEnumArray())
    }

    @Test
    fun returnEnumNArray() = withCLib {
        assertContentEquals(arrayOf(null, MyEnum.CASE2), natives.test.c.returnEnumNArray())
    }

    @Test
    fun returnDictionaryArray() = withCLib {
        assertContentEquals(
            arrayOf(
                MyDictionary(1, 2, 3, 4),
                MyDictionary(5, 6, 7, 8)
            ), natives.test.c.returnDictionaryArray()
        )
    }

    @Test
    fun returnDictionaryArrayN() = withCLib {
        assertContentEquals(arrayOf(null, null), natives.test.c.returnDictionaryArrayN())
    }
}