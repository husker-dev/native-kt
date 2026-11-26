@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.objc.arrays

import natives.test.objc.*
import natives.test.objc.MyInterface
import withObjcLib
import kotlin.test.Test
import kotlin.test.assertContentEquals

class Objc_PingArray {

    @Test
    fun pingCharArray() = withObjcLib {
        val arr = charArrayOf('a', 'b')
        assertContentEquals(arr, pingCharArray(arr))
    }

    @Test
    fun pingCharArrayEmpty() = withObjcLib {
        val arr = charArrayOf()
        assertContentEquals(arr, pingCharArrayEmpty(arr))
    }

    @Test
    fun pingCharArrayN() = withObjcLib {
        val arr = null
        assertContentEquals(arr, pingCharArrayN(arr))
    }

    @Test
    fun pingBooleanArray() = withObjcLib {
        val arr = booleanArrayOf(true, false)
        assertContentEquals(arr, pingBooleanArray(arr))
    }

    @Test
    fun pingByteArray() = withObjcLib {
        val arr = byteArrayOf(1, 2)
        assertContentEquals(arr, pingByteArray(arr))
    }

    @Test
    fun pingUByteArray() = withObjcLib {
        val arr = ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)
        assertContentEquals(arr, pingUbyteArray(arr))
    }

    @Test
    fun pingShortArray() = withObjcLib {
        val arr = shortArrayOf(1, 2)
        assertContentEquals(arr, pingShortArray(arr))
    }

    @Test
    fun pingUShortArray() = withObjcLib {
        val arr = ushortArrayOf(1.toUShort(), UShort.MAX_VALUE)
        assertContentEquals(arr, pingUshortArray(arr))
    }

    @Test
    fun pingIntArray() = withObjcLib {
        val arr = intArrayOf(1, 2)
        assertContentEquals(arr, pingIntArray(arr))
    }

    @Test
    fun pingUIntArray() = withObjcLib {
        val arr = uintArrayOf(1.toUInt(), UInt.MAX_VALUE)
        assertContentEquals(arr, pingUintArray(arr))
    }

    @Test
    fun pingLongArray() = withObjcLib {
        val arr = longArrayOf(1, 2)
        assertContentEquals(arr, pingLongArray(arr))
    }

    @Test
    fun pingULongArray() = withObjcLib {
        val arr = ulongArrayOf(1.toULong(), ULong.MAX_VALUE)
        assertContentEquals(arr, pingUlongArray(arr))
    }

    @Test
    fun pingFloatArray() = withObjcLib {
        val arr = floatArrayOf(1.1f, 2.2f)
        assertContentEquals(arr, pingFloatArray(arr))
    }

    @Test
    fun pingDoubleArray() = withObjcLib {
        val arr = doubleArrayOf(1.1, 2.2)
        assertContentEquals(arr, pingDoubleArray(arr))
    }

    @Test
    fun pingStringArray() = withObjcLib {
        val arr = arrayOf("string1", "string2")
        assertContentEquals(arr, pingStringArray(arr))
    }

    @Test
    fun pingStringArrayN() = withObjcLib {
        val arr: Array<String?> = arrayOf(null, null)
        assertContentEquals(arr, pingStringArrayN(arr))
    }

    @Test
    fun pingEnumArray() = withObjcLib {
        val arr = arrayOf(MyEnum.CASE1, MyEnum.CASE2)
        assertContentEquals(arr, pingEnumArray(arr))
    }

    @Test
    fun pingDictionaryArray() = withObjcLib {
        val arr = arrayOf(
            MyDictionary(1, 2, 3, 4),
            MyDictionary(5, 6, 7, 8)
        )
        assertContentEquals(arr, pingDictionaryArray(arr))
    }

    @Test
    fun pingDictionaryArrayN() = withObjcLib {
        val arr: Array<MyDictionary?> = arrayOf(null, null)
        assertContentEquals(arr, pingDictionaryArrayN(arr))
    }

    @Test
    fun pingInterfaceArray() = withObjcLib {
        val arr = arrayOf(
            MyInterface(),
            MyInterface()
        )
        val copy = pingInterfaceArray(arr)
        assertContentEquals(arr, copy)
        arr.forEach { it.close() }
        copy.forEach { it.close() }
    }

    @Test
    fun pingInterfaceArrayRepeated() = withObjcLib {
        MyInterface().use { obj ->
            val arr = arrayOf(obj, obj)
            val copy = pingInterfaceArray(arr)
            try {
                assertContentEquals(arr, copy)
            } finally {
                copy.forEach { it.close() }
            }
        }
    }

    @Test
    fun pingInterfaceArrayN() = withObjcLib {
        val arr: Array<MyInterface?> = arrayOf(null, null)
        assertContentEquals(arr, pingInterfaceArrayN(arr))
    }
}
