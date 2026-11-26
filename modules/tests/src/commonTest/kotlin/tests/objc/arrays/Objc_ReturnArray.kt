@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.objc.arrays

import natives.test.objc.MyDictionary
import natives.test.objc.MyEnum
import withObjcLib
import kotlin.test.Test
import kotlin.test.assertContentEquals

class Objc_ReturnArray {

    @Test
    fun returnCharArray() = withObjcLib {
        assertContentEquals(charArrayOf('a', 'b'), natives.test.objc.returnCharArray())
    }

    @Test
    fun returnCharArrayEmpty() = withObjcLib {
        assertContentEquals(charArrayOf(), natives.test.objc.returnCharArrayEmpty())
    }

    @Test
    fun returnCharArrayN() = withObjcLib {
        assertContentEquals(null, natives.test.objc.returnCharArrayN())
    }

    @Test
    fun returnCharNArray() = withObjcLib {
        assertContentEquals(arrayOf(null, 'a'), natives.test.objc.returnCharNArray())
    }

    @Test
    fun returnBooleanArray() = withObjcLib {
        assertContentEquals(booleanArrayOf(true, false), natives.test.objc.returnBooleanArray())
    }

    @Test
    fun returnBooleanNArray() = withObjcLib {
        assertContentEquals(arrayOf(null, true), natives.test.objc.returnBooleanNArray())
    }

    @Test
    fun returnByteArray() = withObjcLib {
        assertContentEquals(byteArrayOf(1, 2), natives.test.objc.returnByteArray())
    }

    @Test
    fun returnByteNArray() = withObjcLib {
        assertContentEquals(arrayOf(null, 1), natives.test.objc.returnByteNArray())
    }

    @Test
    fun returnUByteArray() = withObjcLib {
        assertContentEquals(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE), natives.test.objc.returnUbyteArray())
    }

    @Test
    fun returnUByteNArray() = withObjcLib {
        assertContentEquals(arrayOf(null, UByte.MAX_VALUE), natives.test.objc.returnUbyteNArray())
    }

    @Test
    fun returnShortArray() = withObjcLib {
        assertContentEquals(shortArrayOf(1, 2), natives.test.objc.returnShortArray())
    }

    @Test
    fun returnShortNArray() = withObjcLib {
        assertContentEquals(arrayOf(null, 1), natives.test.objc.returnShortNArray())
    }

    @Test
    fun returnUShortArray() = withObjcLib {
        assertContentEquals(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE), natives.test.objc.returnUshortArray())
    }

    @Test
    fun returnUShortNArray() = withObjcLib {
        assertContentEquals(arrayOf(null, UShort.MAX_VALUE), natives.test.objc.returnUshortNArray())
    }

    @Test
    fun returnIntArray() = withObjcLib {
        assertContentEquals(intArrayOf(1, 2), natives.test.objc.returnIntArray())
    }

    @Test
    fun returnIntNArray() = withObjcLib {
        assertContentEquals(arrayOf(null, 1), natives.test.objc.returnIntNArray())
    }

    @Test
    fun returnUIntArray() = withObjcLib {
        assertContentEquals(uintArrayOf(1.toUInt(), UInt.MAX_VALUE), natives.test.objc.returnUintArray())
    }

    @Test
    fun returnUIntNArray() = withObjcLib {
        assertContentEquals(arrayOf(null, UInt.MAX_VALUE), natives.test.objc.returnUintNArray())
    }

    @Test
    fun returnLongArray() = withObjcLib {
        assertContentEquals(longArrayOf(1, 2), natives.test.objc.returnLongArray())
    }

    @Test
    fun returnLongNArray() = withObjcLib {
        assertContentEquals(arrayOf(null, 1), natives.test.objc.returnLongNArray())
    }

    @Test
    fun returnULongArray() = withObjcLib {
        assertContentEquals(ulongArrayOf(1.toULong(), ULong.MAX_VALUE), natives.test.objc.returnUlongArray())
    }

    @Test
    fun returnULongNArray() = withObjcLib {
        assertContentEquals(arrayOf(null, ULong.MAX_VALUE), natives.test.objc.returnUlongNArray())
    }

    @Test
    fun returnFloatArray() = withObjcLib {
        assertContentEquals(floatArrayOf(1.1f, 2.2f), natives.test.objc.returnFloatArray())
    }

    @Test
    fun returnFloatNArray() = withObjcLib {
        assertContentEquals(arrayOf(null, 1.1f), natives.test.objc.returnFloatNArray())
    }

    @Test
    fun returnDoubleArray() = withObjcLib {
        assertContentEquals(doubleArrayOf(1.1, 2.2), natives.test.objc.returnDoubleArray())
    }

    @Test
    fun returnDoubleNArray() = withObjcLib {
        assertContentEquals(arrayOf(null, 1.1), natives.test.objc.returnDoubleNArray())
    }

    @Test
    fun returnStringArray() = withObjcLib {
        assertContentEquals(arrayOf("string1", "string2"), natives.test.objc.returnStringArray())
    }

    @Test
    fun returnStringArrayN() = withObjcLib {
        assertContentEquals(arrayOf(null, null), natives.test.objc.returnStringArrayN())
    }

    @Test
    fun returnEnumArray() = withObjcLib {
        assertContentEquals(arrayOf(MyEnum.CASE1, MyEnum.CASE2), natives.test.objc.returnEnumArray())
    }

    @Test
    fun returnEnumNArray() = withObjcLib {
        assertContentEquals(arrayOf(null, MyEnum.CASE2), natives.test.objc.returnEnumNArray())
    }

    @Test
    fun returnDictionaryArray() = withObjcLib {
        assertContentEquals(
            arrayOf(
                MyDictionary(1, 2, 3, 4),
                MyDictionary(5, 6, 7, 8)
            ), natives.test.objc.returnDictionaryArray()
        )
    }

    @Test
    fun returnDictionaryArrayN() = withObjcLib {
        assertContentEquals(arrayOf(null, null), natives.test.objc.returnDictionaryArrayN())
    }
}
