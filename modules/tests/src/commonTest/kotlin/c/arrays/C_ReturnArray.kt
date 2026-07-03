@file:OptIn(ExperimentalUnsignedTypes::class)

package c.arrays

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
    fun returnCharArrayN() = withCLib {
        assertContentEquals(null, natives.test.returnCharArrayN())
    }

    @Test
    fun returnBooleanArray() = withCLib {
        assertContentEquals(booleanArrayOf(true, false), natives.test.returnBooleanArray())
    }

    @Test
    fun returnByteArray() = withCLib {
        assertContentEquals(byteArrayOf(1, 2), natives.test.returnByteArray())
    }

    @Test
    fun returnUByteArray() = withCLib {
        assertContentEquals(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE), natives.test.returnUByteArray())
    }

    @Test
    fun returnShortArray() = withCLib {
        assertContentEquals(shortArrayOf(1, 2), natives.test.returnShortArray())
    }

    @Test
    fun returnUShortArray() = withCLib {
        assertContentEquals(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE), natives.test.returnUShortArray())
    }

    @Test
    fun returnIntArray() = withCLib {
        assertContentEquals(intArrayOf(1, 2), natives.test.returnIntArray())
    }

    @Test
    fun returnUIntArray() = withCLib {
        assertContentEquals(uintArrayOf(1.toUInt(), UInt.MAX_VALUE), natives.test.returnUIntArray())
    }

    @Test
    fun returnLongArray() = withCLib {
        assertContentEquals(longArrayOf(1, 2), natives.test.returnLongArray())
    }

    @Test
    fun returnULongArray() = withCLib {
        assertContentEquals(ulongArrayOf(1.toULong(), ULong.MAX_VALUE), natives.test.returnULongArray())
    }

    @Test
    fun returnFloatArray() = withCLib {
        assertContentEquals(floatArrayOf(1.1f, 2.2f), natives.test.returnFloatArray())
    }

    @Test
    fun returnDoubleArray() = withCLib {
        assertContentEquals(doubleArrayOf(1.1, 2.2), natives.test.returnDoubleArray())
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