@file:OptIn(ExperimentalUnsignedTypes::class)

package c.arrays

import withCLib
import natives.test.*
import kotlin.test.Test
import kotlin.test.assertContentEquals

class C_PingArray {

    @Test
    fun pingCharArray() = withCLib {
        val arr = charArrayOf('a', 'b')
        assertContentEquals(arr, pingCharArray(arr))
    }

    @Test
    fun pingCharArrayN() = withCLib {
        val arr = null
        assertContentEquals(arr, pingCharArrayN(arr))
    }

    @Test
    fun pingBooleanArray() = withCLib {
        val arr = booleanArrayOf(true, false)
        assertContentEquals(arr, pingBooleanArray(arr))
    }

    @Test
    fun pingByteArray() = withCLib {
        val arr = byteArrayOf(1, 2)
        assertContentEquals(arr, pingByteArray(arr))
    }

    @Test
    fun pingUByteArray() = withCLib {
        val arr = ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)
        assertContentEquals(arr, pingUByteArray(arr))
    }

    @Test
    fun pingShortArray() = withCLib {
        val arr = shortArrayOf(1, 2)
        assertContentEquals(arr, pingShortArray(arr))
    }

    @Test
    fun pingUShortArray() = withCLib {
        val arr = ushortArrayOf(1.toUShort(), UShort.MAX_VALUE)
        assertContentEquals(arr, pingUShortArray(arr))
    }

    @Test
    fun pingIntArray() = withCLib {
        val arr = intArrayOf(1, 2)
        assertContentEquals(arr, pingIntArray(arr))
    }

    @Test
    fun pingUIntArray() = withCLib {
        val arr = uintArrayOf(1.toUInt(), UInt.MAX_VALUE)
        assertContentEquals(arr, pingUIntArray(arr))
    }

    @Test
    fun pingLongArray() = withCLib {
        val arr = longArrayOf(1, 2)
        assertContentEquals(arr, pingLongArray(arr))
    }

    @Test
    fun pingULongArray() = withCLib {
        val arr = ulongArrayOf(1.toULong(), ULong.MAX_VALUE)
        assertContentEquals(arr, pingULongArray(arr))
    }

    @Test
    fun pingFloatArray() = withCLib {
        val arr = floatArrayOf(1.1f, 2.2f)
        assertContentEquals(arr, pingFloatArray(arr))
    }

    @Test
    fun pingDoubleArray() = withCLib {
        val arr = doubleArrayOf(1.1, 2.2)
        assertContentEquals(arr, pingDoubleArray(arr))
    }

    @Test
    fun pingStringArray() = withCLib {
        val arr = arrayOf("string1", "string2")
        assertContentEquals(arr, pingStringArray(arr))
    }

    @Test
    fun pingStringArrayN() = withCLib {
        val arr: Array<String?> = arrayOf(null, null)
        assertContentEquals(arr, pingStringArrayN(arr))
    }

    @Test
    fun pingEnumArray() = withCLib {
        val arr = arrayOf(MyEnum.CASE1, MyEnum.CASE2)
        assertContentEquals(arr, pingEnumArray(arr))
    }

    @Test
    fun pingDictionaryArray() = withCLib {
        val arr = arrayOf(
            MyDictionary(1, 2, 3, 4),
            MyDictionary(5, 6, 7, 8)
        )
        assertContentEquals(arr, pingDictionaryArray(arr))
    }

    @Test
    fun pingDictionaryArrayN() = withCLib {
        val arr: Array<MyDictionary?> = arrayOf(null, null)
        assertContentEquals(arr, pingDictionaryArrayN(arr))
    }
}