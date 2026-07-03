@file:OptIn(ExperimentalUnsignedTypes::class)

package rust.arrays

import withRustLib
import natives.testrs.MyDictionary
import natives.testrs.MyEnum
import kotlin.test.Test
import kotlin.test.assertContentEquals

class Rust_ReturnArray {

    @Test
    fun returnCharArray() = withRustLib {
        assertContentEquals(charArrayOf('a', 'b'), natives.testrs.returnCharArray())
    }

    @Test
    fun returnCharArrayN() = withRustLib {
        assertContentEquals(null, natives.testrs.returnCharArrayN())
    }

    @Test
    fun returnBooleanArray() = withRustLib {
        assertContentEquals(booleanArrayOf(true, false), natives.testrs.returnBooleanArray())
    }

    @Test
    fun returnByteArray() = withRustLib {
        assertContentEquals(byteArrayOf(1, 2), natives.testrs.returnByteArray())
    }

    @Test
    fun returnUByteArray() = withRustLib {
        assertContentEquals(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE), natives.testrs.returnUByteArray())
    }

    @Test
    fun returnShortArray() = withRustLib {
        assertContentEquals(shortArrayOf(1, 2), natives.testrs.returnShortArray())
    }

    @Test
    fun returnUShortArray() = withRustLib {
        assertContentEquals(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE), natives.testrs.returnUShortArray())
    }

    @Test
    fun returnIntArray() = withRustLib {
        assertContentEquals(intArrayOf(1, 2), natives.testrs.returnIntArray())
    }

    @Test
    fun returnUIntArray() = withRustLib {
        assertContentEquals(uintArrayOf(1.toUInt(), UInt.MAX_VALUE), natives.testrs.returnUIntArray())
    }

    @Test
    fun returnLongArray() = withRustLib {
        assertContentEquals(longArrayOf(1, 2), natives.testrs.returnLongArray())
    }

    @Test
    fun returnULongArray() = withRustLib {
        assertContentEquals(ulongArrayOf(1.toULong(), ULong.MAX_VALUE), natives.testrs.returnULongArray())
    }

    @Test
    fun returnFloatArray() = withRustLib {
        assertContentEquals(floatArrayOf(1.1f, 2.2f), natives.testrs.returnFloatArray())
    }

    @Test
    fun returnDoubleArray() = withRustLib {
        assertContentEquals(doubleArrayOf(1.1, 2.2), natives.testrs.returnDoubleArray())
    }

    @Test
    fun returnStringArray() = withRustLib {
        assertContentEquals(arrayOf("string1", "string2"), natives.testrs.returnStringArray())
    }

    @Test
    fun returnStringArrayN() = withRustLib {
        assertContentEquals(arrayOf(null, null), natives.testrs.returnStringArrayN())
    }

    @Test
    fun returnEnumArray() = withRustLib {
        assertContentEquals(arrayOf(MyEnum.CASE1, MyEnum.CASE2), natives.testrs.returnEnumArray())
    }

    @Test
    fun returnDictionaryArray() = withRustLib {
        assertContentEquals(
            arrayOf(
                MyDictionary(1, 2, 3, 4),
                MyDictionary(5, 6, 7, 8)
            ), natives.testrs.returnDictionaryArray()
        )
    }

    @Test
    fun returnDictionaryArrayN() = withRustLib {
        assertContentEquals(arrayOf(null, null), natives.testrs.returnDictionaryArrayN())
    }
}