@file:OptIn(ExperimentalUnsignedTypes::class)

package c.arrays

import withCLib
import natives.test.MyDictionary
import natives.test.MyEnum
import kotlin.test.Test
import kotlin.test.assertTrue

class C_CallbackReturnArray {

    @Test
    fun callbackReturnCharArray() = withCLib {
        assertTrue(natives.test.callbackReturnCharArray { charArrayOf('a', 'b') })
    }

    @Test
    fun callbackReturnCharArrayN() = withCLib {
        assertTrue(natives.test.callbackReturnCharArrayN { null })
    }

    @Test
    fun callbackReturnBooleanArray() = withCLib {
        assertTrue(natives.test.callbackReturnBooleanArray { booleanArrayOf(true, false) })
    }

    @Test
    fun callbackReturnByteArray() = withCLib {
        assertTrue(natives.test.callbackReturnByteArray { byteArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUByteArray() = withCLib {
        assertTrue(natives.test.callbackReturnUByteArray { ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE) })
    }

    @Test
    fun callbackReturnShortArray() = withCLib {
        assertTrue(natives.test.callbackReturnShortArray { shortArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUShortArray() = withCLib {
        assertTrue(natives.test.callbackReturnUShortArray { ushortArrayOf(1.toUShort(), UShort.MAX_VALUE) })
    }

    @Test
    fun callbackReturnIntArray() = withCLib {
        assertTrue(natives.test.callbackReturnIntArray { intArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUIntArray() = withCLib {
        assertTrue(natives.test.callbackReturnUIntArray { uintArrayOf(1.toUInt(), UInt.MAX_VALUE) })
    }

    @Test
    fun callbackReturnLongArray() = withCLib {
        assertTrue(natives.test.callbackReturnLongArray { longArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnULongArray() = withCLib {
        assertTrue(natives.test.callbackReturnULongArray { ulongArrayOf(1.toULong(), ULong.MAX_VALUE) })
    }

    @Test
    fun callbackReturnFloatArray() = withCLib {
        assertTrue(natives.test.callbackReturnFloatArray { floatArrayOf(1.1f, 2.2f) })
    }

    @Test
    fun callbackReturnDoubleArray() = withCLib {
        assertTrue(natives.test.callbackReturnDoubleArray { doubleArrayOf(1.1, 2.2) })
    }

    @Test
    fun callbackReturnStringArray() = withCLib {
        assertTrue(natives.test.callbackReturnStringArray { arrayOf("string1", "string2") })
    }

    @Test
    fun callbackReturnStringArrayN() = withCLib {
        assertTrue(natives.test.callbackReturnStringArrayN { arrayOf(null, null) })
    }

    @Test
    fun callbackReturnEnumArray() = withCLib {
        assertTrue(natives.test.callbackReturnEnumArray { arrayOf(MyEnum.CASE1, MyEnum.CASE2) })
    }

    @Test
    fun callbackReturnDictionaryArray() = withCLib {
        assertTrue(natives.test.callbackReturnDictionaryArray {
            arrayOf(
                MyDictionary(1, 2, 3, 4),
                MyDictionary(5, 6, 7, 8)
            )
        })
    }

    @Test
    fun callbackReturnDictionaryArrayN() = withCLib {
        assertTrue(natives.test.callbackReturnDictionaryArrayN {
            arrayOf(null, null)
        })
    }
}