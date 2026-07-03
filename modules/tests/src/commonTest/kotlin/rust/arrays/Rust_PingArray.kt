@file:OptIn(ExperimentalUnsignedTypes::class)

package rust.arrays

import withRustLib
import natives.testrs.*
import kotlin.test.Test
import kotlin.test.assertContentEquals

class Rust_PingArray {

    @Test
    fun pingCharArray() = withRustLib {
        val arr = charArrayOf('a', 'b')
        assertContentEquals(arr, pingCharArray(arr))
    }

    @Test
    fun pingCharArrayN() = withRustLib {
        val arr = null
        assertContentEquals(arr, pingCharArrayN(arr))
    }

    @Test
    fun pingBooleanArray() = withRustLib {
        val arr = booleanArrayOf(true, false)
        assertContentEquals(arr, pingBooleanArray(arr))
    }

    @Test
    fun pingByteArray() = withRustLib {
        val arr = byteArrayOf(1, 2)
        assertContentEquals(arr, pingByteArray(arr))
    }

    @Test
    fun pingUByteArray() = withRustLib {
        val arr = ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)
        assertContentEquals(arr, pingUByteArray(arr))
    }

    @Test
    fun pingShortArray() = withRustLib {
        val arr = shortArrayOf(1, 2)
        assertContentEquals(arr, pingShortArray(arr))
    }

    @Test
    fun pingUShortArray() = withRustLib {
        val arr = ushortArrayOf(1.toUShort(), UShort.MAX_VALUE)
        assertContentEquals(arr, pingUShortArray(arr))
    }

    @Test
    fun pingIntArray() = withRustLib {
        val arr = intArrayOf(1, 2)
        assertContentEquals(arr, pingIntArray(arr))
    }

    @Test
    fun pingUIntArray() = withRustLib {
        val arr = uintArrayOf(1.toUInt(), UInt.MAX_VALUE)
        assertContentEquals(arr, pingUIntArray(arr))
    }

    @Test
    fun pingLongArray() = withRustLib {
        val arr = longArrayOf(1, 2)
        assertContentEquals(arr, pingLongArray(arr))
    }

    @Test
    fun pingULongArray() = withRustLib {
        val arr = ulongArrayOf(1.toULong(), ULong.MAX_VALUE)
        assertContentEquals(arr, pingULongArray(arr))
    }

    @Test
    fun pingFloatArray() = withRustLib {
        val arr = floatArrayOf(1.1f, 2.2f)
        assertContentEquals(arr, pingFloatArray(arr))
    }

    @Test
    fun pingDoubleArray() = withRustLib {
        val arr = doubleArrayOf(1.1, 2.2)
        assertContentEquals(arr, pingDoubleArray(arr))
    }

    @Test
    fun pingStringArray() = withRustLib {
        val arr = arrayOf("string1", "string2")
        assertContentEquals(arr, pingStringArray(arr))
    }

    @Test
    fun pingStringArrayN() = withRustLib {
        val arr: Array<String?> = arrayOf(null, null)
        assertContentEquals(arr, pingStringArrayN(arr))
    }

    @Test
    fun pingEnumArray() = withRustLib {
        val arr = arrayOf(MyEnum.CASE1, MyEnum.CASE2)
        assertContentEquals(arr, pingEnumArray(arr))
    }

    @Test
    fun pingDictionaryArray() = withRustLib {
        val arr = arrayOf(
            MyDictionary(1, 2, 3, 4),
            MyDictionary(5, 6, 7, 8)
        )
        assertContentEquals(arr, pingDictionaryArray(arr))
    }

    @Test
    fun pingDictionaryArrayN() = withRustLib {
        val arr: Array<MyDictionary?> = arrayOf(null, null)
        assertContentEquals(arr, pingDictionaryArrayN(arr))
    }
}