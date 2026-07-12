@file:OptIn(ExperimentalUnsignedTypes::class)

package cpp.primitives.arrays

import natives.testcpp.*
import withCppLib
import kotlin.test.Test
import kotlin.test.assertContentEquals

class Cpp_PingArray {

    @Test
    fun pingCharArray() = withCppLib {
        val arr = charArrayOf('a', 'b')
        assertContentEquals(arr, pingCharArray(arr))
    }

    @Test
    fun pingCharArrayN() = withCppLib {
        val arr = null
        assertContentEquals(arr, pingCharArrayN(arr))
    }

    @Test
    fun pingBooleanArray() = withCppLib {
        val arr = booleanArrayOf(true, false)
        assertContentEquals(arr, pingBooleanArray(arr))
    }

    @Test
    fun pingByteArray() = withCppLib {
        val arr = byteArrayOf(1, 2)
        assertContentEquals(arr, pingByteArray(arr))
    }

    @Test
    fun pingUByteArray() = withCppLib {
        val arr = ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)
        assertContentEquals(arr, pingUByteArray(arr))
    }

    @Test
    fun pingShortArray() = withCppLib {
        val arr = shortArrayOf(1, 2)
        assertContentEquals(arr, pingShortArray(arr))
    }

    @Test
    fun pingUShortArray() = withCppLib {
        val arr = ushortArrayOf(1.toUShort(), UShort.MAX_VALUE)
        assertContentEquals(arr, pingUShortArray(arr))
    }

    @Test
    fun pingIntArray() = withCppLib {
        val arr = intArrayOf(1, 2)
        assertContentEquals(arr, pingIntArray(arr))
    }

    @Test
    fun pingUIntArray() = withCppLib {
        val arr = uintArrayOf(1.toUInt(), UInt.MAX_VALUE)
        assertContentEquals(arr, pingUIntArray(arr))
    }

    @Test
    fun pingLongArray() = withCppLib {
        val arr = longArrayOf(1, 2)
        assertContentEquals(arr, pingLongArray(arr))
    }

    @Test
    fun pingULongArray() = withCppLib {
        val arr = ulongArrayOf(1.toULong(), ULong.MAX_VALUE)
        assertContentEquals(arr, pingULongArray(arr))
    }

    @Test
    fun pingFloatArray() = withCppLib {
        val arr = floatArrayOf(1.1f, 2.2f)
        assertContentEquals(arr, pingFloatArray(arr))
    }

    @Test
    fun pingDoubleArray() = withCppLib {
        val arr = doubleArrayOf(1.1, 2.2)
        assertContentEquals(arr, pingDoubleArray(arr))
    }

    @Test
    fun pingStringArray() = withCppLib {
        val arr = arrayOf("string1", "string2")
        assertContentEquals(arr, pingStringArray(arr))
    }

    @Test
    fun pingStringArrayN() = withCppLib {
        val arr: Array<String?> = arrayOf(null, null)
        assertContentEquals(arr, pingStringArrayN(arr))
    }

    @Test
    fun pingEnumArray() = withCppLib {
        val arr = arrayOf(MyEnum.CASE1, MyEnum.CASE2)
        assertContentEquals(arr, pingEnumArray(arr))
    }

    @Test
    fun pingDictionaryArray() = withCppLib {
        val arr = arrayOf(
            MyDictionary(1, 2, 3, 4),
            MyDictionary(5, 6, 7, 8)
        )
        assertContentEquals(arr, pingDictionaryArray(arr))
    }

    @Test
    fun pingDictionaryArrayN() = withCppLib {
        val arr: Array<MyDictionary?> = arrayOf(null, null)
        assertContentEquals(arr, pingDictionaryArrayN(arr))
    }
}