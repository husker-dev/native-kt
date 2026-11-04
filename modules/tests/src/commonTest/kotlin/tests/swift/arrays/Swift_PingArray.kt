@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.swift.arrays

import withSwiftLib
import natives.test.swift.*
import kotlin.test.Test
import kotlin.test.assertContentEquals

class Swift_PingArray {

    @Test
    fun pingCharArray() = withSwiftLib {
        val arr = charArrayOf('a', 'b')
        assertContentEquals(arr, pingCharArray(arr))
    }

    @Test
    fun pingCharArrayEmpty() = withSwiftLib {
        val arr = charArrayOf()
        assertContentEquals(arr, pingCharArrayEmpty(arr))
    }

    @Test
    fun pingCharArrayN() = withSwiftLib {
        val arr = null
        assertContentEquals(arr, pingCharArrayN(arr))
    }

    @Test
    fun pingBooleanArray() = withSwiftLib {
        val arr = booleanArrayOf(true, false)
        assertContentEquals(arr, pingBooleanArray(arr))
    }

    @Test
    fun pingByteArray() = withSwiftLib {
        val arr = byteArrayOf(1, 2)
        assertContentEquals(arr, pingByteArray(arr))
    }

    @Test
    fun pingUByteArray() = withSwiftLib {
        val arr = ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)
        assertContentEquals(arr, pingUbyteArray(arr))
    }

    @Test
    fun pingShortArray() = withSwiftLib {
        val arr = shortArrayOf(1, 2)
        assertContentEquals(arr, pingShortArray(arr))
    }

    @Test
    fun pingUShortArray() = withSwiftLib {
        val arr = ushortArrayOf(1.toUShort(), UShort.MAX_VALUE)
        assertContentEquals(arr, pingUshortArray(arr))
    }

    @Test
    fun pingIntArray() = withSwiftLib {
        val arr = intArrayOf(1, 2)
        assertContentEquals(arr, pingIntArray(arr))
    }

    @Test
    fun pingUIntArray() = withSwiftLib {
        val arr = uintArrayOf(1.toUInt(), UInt.MAX_VALUE)
        assertContentEquals(arr, pingUintArray(arr))
    }

    @Test
    fun pingLongArray() = withSwiftLib {
        val arr = longArrayOf(1, 2)
        assertContentEquals(arr, pingLongArray(arr))
    }

    @Test
    fun pingULongArray() = withSwiftLib {
        val arr = ulongArrayOf(1.toULong(), ULong.MAX_VALUE)
        assertContentEquals(arr, pingUlongArray(arr))
    }

    @Test
    fun pingFloatArray() = withSwiftLib {
        val arr = floatArrayOf(1.1f, 2.2f)
        assertContentEquals(arr, pingFloatArray(arr))
    }

    @Test
    fun pingDoubleArray() = withSwiftLib {
        val arr = doubleArrayOf(1.1, 2.2)
        assertContentEquals(arr, pingDoubleArray(arr))
    }

    @Test
    fun pingStringArray() = withSwiftLib {
        val arr = arrayOf("string1", "string2")
        assertContentEquals(arr, pingStringArray(arr))
    }

    @Test
    fun pingStringArrayN() = withSwiftLib {
        val arr: Array<String?> = arrayOf(null, null)
        assertContentEquals(arr, pingStringArrayN(arr))
    }

    @Test
    fun pingEnumArray() = withSwiftLib {
        val arr = arrayOf(MyEnum.CASE1, MyEnum.CASE2)
        assertContentEquals(arr, pingEnumArray(arr))
    }

    @Test
    fun pingDictionaryArray() = withSwiftLib {
        val arr = arrayOf(
            MyDictionary(1, 2, 3, 4),
            MyDictionary(5, 6, 7, 8)
        )
        assertContentEquals(arr, pingDictionaryArray(arr))
    }

    @Test
    fun pingDictionaryArrayN() = withSwiftLib {
        val arr: Array<MyDictionary?> = arrayOf(null, null)
        assertContentEquals(arr, pingDictionaryArrayN(arr))
    }

    @Test
    fun pingInterfaceArray() = withSwiftLib {
        val arr = arrayOf(
            MyInterface(),
            MyInterface()
        )
        val cloned = pingInterfaceArray(arr)
        assertContentEquals(arr, cloned)
        arr.forEach { it.close() }
        cloned.forEach { it.close() }
    }

    @Test
    fun pingInterfaceArrayN() = withSwiftLib {
        val arr: Array<MyInterface?> = arrayOf(null, null)
        assertContentEquals(arr, pingInterfaceArrayN(arr))
    }
}