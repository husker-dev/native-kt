@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.rust.arrays

import withRustLib
import natives.test.rust.MyDictionary
import natives.test.rust.MyEnum
import kotlin.test.Test
import kotlin.test.assertContentEquals

class Rust_ReturnArray {

    @Test
    fun returnCharArray() = withRustLib {
        assertContentEquals(charArrayOf('a', 'b'), natives.test.rust.returnCharArray())
    }

    @Test
    fun returnCharArrayEmpty() = withRustLib {
        assertContentEquals(charArrayOf(), natives.test.rust.returnCharArrayEmpty())
    }

    @Test
    fun returnCharArrayN() = withRustLib {
        assertContentEquals(null, natives.test.rust.returnCharArrayN())
    }

    @Test
    fun returnCharNArray() = withRustLib {
        assertContentEquals(arrayOf(null, 'a'), natives.test.rust.returnCharNArray())
    }

    @Test
    fun returnBooleanArray() = withRustLib {
        assertContentEquals(booleanArrayOf(true, false), natives.test.rust.returnBooleanArray())
    }

    @Test
    fun returnBooleanNArray() = withRustLib {
        assertContentEquals(arrayOf(null, true), natives.test.rust.returnBooleanNArray())
    }

    @Test
    fun returnByteArray() = withRustLib {
        assertContentEquals(byteArrayOf(1, 2), natives.test.rust.returnByteArray())
    }

    @Test
    fun returnByteNArray() = withRustLib {
        assertContentEquals(arrayOf(null, 1), natives.test.rust.returnByteNArray())
    }

    @Test
    fun returnUByteArray() = withRustLib {
        assertContentEquals(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE), natives.test.rust.returnUbyteArray())
    }

    @Test
    fun returnUByteNArray() = withRustLib {
        assertContentEquals(arrayOf(null, UByte.MAX_VALUE), natives.test.rust.returnUbyteNArray())
    }

    @Test
    fun returnShortArray() = withRustLib {
        assertContentEquals(shortArrayOf(1, 2), natives.test.rust.returnShortArray())
    }

    @Test
    fun returnShortNArray() = withRustLib {
        assertContentEquals(arrayOf(null, 1), natives.test.rust.returnShortNArray())
    }

    @Test
    fun returnUShortArray() = withRustLib {
        assertContentEquals(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE), natives.test.rust.returnUshortArray())
    }

    @Test
    fun returnUShortNArray() = withRustLib {
        assertContentEquals(arrayOf(null, UShort.MAX_VALUE), natives.test.rust.returnUshortNArray())
    }

    @Test
    fun returnIntArray() = withRustLib {
        assertContentEquals(intArrayOf(1, 2), natives.test.rust.returnIntArray())
    }

    @Test
    fun returnIntNArray() = withRustLib {
        assertContentEquals(arrayOf(null, 1), natives.test.rust.returnIntNArray())
    }

    @Test
    fun returnUIntArray() = withRustLib {
        assertContentEquals(uintArrayOf(1.toUInt(), UInt.MAX_VALUE), natives.test.rust.returnUintArray())
    }

    @Test
    fun returnUIntNArray() = withRustLib {
        assertContentEquals(arrayOf(null, UInt.MAX_VALUE), natives.test.rust.returnUintNArray())
    }

    @Test
    fun returnLongArray() = withRustLib {
        assertContentEquals(longArrayOf(1, 2), natives.test.rust.returnLongArray())
    }

    @Test
    fun returnLongNArray() = withRustLib {
        assertContentEquals(arrayOf(null, 1), natives.test.rust.returnLongNArray())
    }

    @Test
    fun returnULongArray() = withRustLib {
        assertContentEquals(ulongArrayOf(1.toULong(), ULong.MAX_VALUE), natives.test.rust.returnUlongArray())
    }

    @Test
    fun returnULongNArray() = withRustLib {
        assertContentEquals(arrayOf(null, ULong.MAX_VALUE), natives.test.rust.returnUlongNArray())
    }

    @Test
    fun returnFloatArray() = withRustLib {
        assertContentEquals(floatArrayOf(1.1f, 2.2f), natives.test.rust.returnFloatArray())
    }

    @Test
    fun returnFloatNArray() = withRustLib {
        assertContentEquals(arrayOf(null, 1.1f), natives.test.rust.returnFloatNArray())
    }

    @Test
    fun returnDoubleArray() = withRustLib {
        assertContentEquals(doubleArrayOf(1.1, 2.2), natives.test.rust.returnDoubleArray())
    }

    @Test
    fun returnDoubleNArray() = withRustLib {
        assertContentEquals(arrayOf(null, 1.1), natives.test.rust.returnDoubleNArray())
    }

    @Test
    fun returnStringArray() = withRustLib {
        assertContentEquals(arrayOf("string1", "string2"), natives.test.rust.returnStringArray())
    }

    @Test
    fun returnStringArrayN() = withRustLib {
        assertContentEquals(arrayOf(null, null), natives.test.rust.returnStringArrayN())
    }

    @Test
    fun returnEnumArray() = withRustLib {
        assertContentEquals(arrayOf(MyEnum.CASE1, MyEnum.CASE2), natives.test.rust.returnEnumArray())
    }

    @Test
    fun returnEnumNArray() = withRustLib {
        assertContentEquals(arrayOf(null, MyEnum.CASE2), natives.test.rust.returnEnumNArray())
    }

    @Test
    fun returnDictionaryArray() = withRustLib {
        assertContentEquals(
            arrayOf(
                MyDictionary(1, 2, 3, 4),
                MyDictionary(5, 6, 7, 8)
            ), natives.test.rust.returnDictionaryArray()
        )
    }

    @Test
    fun returnDictionaryArrayN() = withRustLib {
        assertContentEquals(arrayOf(null, null), natives.test.rust.returnDictionaryArrayN())
    }
}