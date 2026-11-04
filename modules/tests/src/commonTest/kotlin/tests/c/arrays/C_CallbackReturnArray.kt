@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.c.arrays

import withCLib
import natives.test.c.MyDictionary
import natives.test.c.MyEnum
import natives.test.c.MyInterface
import kotlin.test.Test
import kotlin.test.assertTrue

class C_CallbackReturnArray {

    @Test
    fun callbackReturnCharArray() = withCLib {
        assertTrue(natives.test.c.callbackReturnCharArray { charArrayOf('a', 'b') })
    }

    @Test
    fun callbackReturnCharArrayEmpty() = withCLib {
        assertTrue(natives.test.c.callbackReturnCharArrayEmpty { charArrayOf() })
    }

    @Test
    fun callbackReturnCharArrayN() = withCLib {
        assertTrue(natives.test.c.callbackReturnCharArrayN { null })
    }

    @Test
    fun callbackReturnBooleanArray() = withCLib {
        assertTrue(natives.test.c.callbackReturnBooleanArray { booleanArrayOf(true, false) })
    }

    @Test
    fun callbackReturnByteArray() = withCLib {
        assertTrue(natives.test.c.callbackReturnByteArray { byteArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUByteArray() = withCLib {
        assertTrue(natives.test.c.callbackReturnUbyteArray { ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE) })
    }

    @Test
    fun callbackReturnShortArray() = withCLib {
        assertTrue(natives.test.c.callbackReturnShortArray { shortArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUShortArray() = withCLib {
        assertTrue(natives.test.c.callbackReturnUshortArray { ushortArrayOf(1.toUShort(), UShort.MAX_VALUE) })
    }

    @Test
    fun callbackReturnIntArray() = withCLib {
        assertTrue(natives.test.c.callbackReturnIntArray { intArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUIntArray() = withCLib {
        assertTrue(natives.test.c.callbackReturnUintArray { uintArrayOf(1.toUInt(), UInt.MAX_VALUE) })
    }

    @Test
    fun callbackReturnLongArray() = withCLib {
        assertTrue(natives.test.c.callbackReturnLongArray { longArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnULongArray() = withCLib {
        assertTrue(natives.test.c.callbackReturnUlongArray { ulongArrayOf(1.toULong(), ULong.MAX_VALUE) })
    }

    @Test
    fun callbackReturnFloatArray() = withCLib {
        assertTrue(natives.test.c.callbackReturnFloatArray { floatArrayOf(1.1f, 2.2f) })
    }

    @Test
    fun callbackReturnDoubleArray() = withCLib {
        assertTrue(natives.test.c.callbackReturnDoubleArray { doubleArrayOf(1.1, 2.2) })
    }

    @Test
    fun callbackReturnStringArray() = withCLib {
        assertTrue(natives.test.c.callbackReturnStringArray { arrayOf("string1", "string2") })
    }

    @Test
    fun callbackReturnStringArrayN() = withCLib {
        assertTrue(natives.test.c.callbackReturnStringArrayN { arrayOf(null, null) })
    }

    @Test
    fun callbackReturnEnumArray() = withCLib {
        assertTrue(natives.test.c.callbackReturnEnumArray { arrayOf(MyEnum.CASE1, MyEnum.CASE2) })
    }

    @Test
    fun callbackReturnDictionaryArray() = withCLib {
        assertTrue(natives.test.c.callbackReturnDictionaryArray {
            arrayOf(
                MyDictionary(1, 2, 3, 4),
                MyDictionary(5, 6, 7, 8)
            )
        })
    }

    @Test
    fun callbackReturnDictionaryArrayN() = withCLib {
        assertTrue(natives.test.c.callbackReturnDictionaryArrayN {
            arrayOf(null, null)
        })
    }

    @Test
    fun callbackReturnInterfaceArray() = withCLib {
        val array = arrayOf(
            MyInterface(),
            MyInterface()
        )
        assertTrue(natives.test.c.callbackReturnInterfaceArray {
            array
        })
        array.forEach { it.close() }
    }

    @Test
    fun callbackReturnInterfaceArrayN() = withCLib {
        assertTrue(natives.test.c.callbackReturnInterfaceArrayN {
            arrayOf(null, null)
        })
    }
}