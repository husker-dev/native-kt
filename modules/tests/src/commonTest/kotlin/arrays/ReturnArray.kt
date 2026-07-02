@file:OptIn(ExperimentalUnsignedTypes::class)

package arrays

import natives.test.MyDictionary
import natives.test.MyEnum
import withLib
import kotlin.test.Test
import kotlin.test.assertContentEquals

class ReturnArray {

    @Test
    fun returnCharArray() = withLib {
        assertContentEquals(charArrayOf('a', 'b'), natives.test.returnCharArray())
    }

    @Test
    fun returnCharArrayN() = withLib {
        assertContentEquals(null, natives.test.returnCharArrayN())
    }

    @Test
    fun returnBooleanArray() = withLib {
        assertContentEquals(booleanArrayOf(true, false), natives.test.returnBooleanArray())
    }

    @Test
    fun returnByteArray() = withLib {
        assertContentEquals(byteArrayOf(1, 2), natives.test.returnByteArray())
    }

    @Test
    fun returnUByteArray() = withLib {
        assertContentEquals(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE), natives.test.returnUByteArray())
    }

    @Test
    fun returnShortArray() = withLib {
        assertContentEquals(shortArrayOf(1, 2), natives.test.returnShortArray())
    }

    @Test
    fun returnUShortArray() = withLib {
        assertContentEquals(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE), natives.test.returnUShortArray())
    }

    @Test
    fun returnIntArray() = withLib {
        assertContentEquals(intArrayOf(1, 2), natives.test.returnIntArray())
    }

    @Test
    fun returnUIntArray() = withLib {
        assertContentEquals(uintArrayOf(1.toUInt(), UInt.MAX_VALUE), natives.test.returnUIntArray())
    }

    @Test
    fun returnLongArray() = withLib {
        assertContentEquals(longArrayOf(1, 2), natives.test.returnLongArray())
    }

    @Test
    fun returnULongArray() = withLib {
        assertContentEquals(ulongArrayOf(1.toULong(), ULong.MAX_VALUE), natives.test.returnULongArray())
    }

    @Test
    fun returnFloatArray() = withLib {
        assertContentEquals(floatArrayOf(1.1f, 2.2f), natives.test.returnFloatArray())
    }

    @Test
    fun returnDoubleArray() = withLib {
        assertContentEquals(doubleArrayOf(1.1, 2.2), natives.test.returnDoubleArray())
    }

    @Test
    fun returnStringArray() = withLib {
        assertContentEquals(arrayOf("string1", "string2"), natives.test.returnStringArray())
    }

    @Test
    fun returnStringArrayN() = withLib {
        assertContentEquals(arrayOf(null, null), natives.test.returnStringArrayN())
    }

    @Test
    fun returnEnumArray() = withLib {
        assertContentEquals(arrayOf(MyEnum.CASE1, MyEnum.CASE2), natives.test.returnEnumArray())
    }

    @Test
    fun returnDictionaryArray() = withLib {
        assertContentEquals(arrayOf(
            MyDictionary(1, 2, 3, 4),
            MyDictionary(5, 6, 7, 8)
        ), natives.test.returnDictionaryArray())
    }

    @Test
    fun returnDictionaryArrayN() = withLib {
        assertContentEquals(arrayOf(null, null), natives.test.returnDictionaryArrayN())
    }
}