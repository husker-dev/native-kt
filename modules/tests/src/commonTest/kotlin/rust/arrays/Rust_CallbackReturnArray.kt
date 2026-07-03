@file:OptIn(ExperimentalUnsignedTypes::class)

package rust.arrays

import withRustLib
import natives.testrs.MyDictionary
import natives.testrs.MyEnum
import kotlin.test.Test
import kotlin.test.assertTrue

class Rust_CallbackReturnArray {

    @Test
    fun callbackReturnCharArray() = withRustLib {
        assertTrue(natives.testrs.callbackReturnCharArray { charArrayOf('a', 'b') })
    }

    @Test
    fun callbackReturnCharArrayN() = withRustLib {
        assertTrue(natives.testrs.callbackReturnCharArrayN { null })
    }

    @Test
    fun callbackReturnBooleanArray() = withRustLib {
        assertTrue(natives.testrs.callbackReturnBooleanArray { booleanArrayOf(true, false) })
    }

    @Test
    fun callbackReturnByteArray() = withRustLib {
        assertTrue(natives.testrs.callbackReturnByteArray { byteArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUByteArray() = withRustLib {
        assertTrue(natives.testrs.callbackReturnUByteArray { ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE) })
    }

    @Test
    fun callbackReturnShortArray() = withRustLib {
        assertTrue(natives.testrs.callbackReturnShortArray { shortArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUShortArray() = withRustLib {
        assertTrue(natives.testrs.callbackReturnUShortArray { ushortArrayOf(1.toUShort(), UShort.MAX_VALUE) })
    }

    @Test
    fun callbackReturnIntArray() = withRustLib {
        assertTrue(natives.testrs.callbackReturnIntArray { intArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUIntArray() = withRustLib {
        assertTrue(natives.testrs.callbackReturnUIntArray { uintArrayOf(1.toUInt(), UInt.MAX_VALUE) })
    }

    @Test
    fun callbackReturnLongArray() = withRustLib {
        assertTrue(natives.testrs.callbackReturnLongArray { longArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnULongArray() = withRustLib {
        assertTrue(natives.testrs.callbackReturnULongArray { ulongArrayOf(1.toULong(), ULong.MAX_VALUE) })
    }

    @Test
    fun callbackReturnFloatArray() = withRustLib {
        assertTrue(natives.testrs.callbackReturnFloatArray { floatArrayOf(1.1f, 2.2f) })
    }

    @Test
    fun callbackReturnDoubleArray() = withRustLib {
        assertTrue(natives.testrs.callbackReturnDoubleArray { doubleArrayOf(1.1, 2.2) })
    }

    @Test
    fun callbackReturnStringArray() = withRustLib {
        assertTrue(natives.testrs.callbackReturnStringArray { arrayOf("string1", "string2") })
    }

    @Test
    fun callbackReturnStringArrayN() = withRustLib {
        assertTrue(natives.testrs.callbackReturnStringArrayN { arrayOf(null, null) })
    }

    @Test
    fun callbackReturnEnumArray() = withRustLib {
        assertTrue(natives.testrs.callbackReturnEnumArray { arrayOf(MyEnum.CASE1, MyEnum.CASE2) })
    }

    @Test
    fun callbackReturnDictionaryArray() = withRustLib {
        assertTrue(natives.testrs.callbackReturnDictionaryArray {
            arrayOf(
                MyDictionary(1, 2, 3, 4),
                MyDictionary(5, 6, 7, 8)
            )
        })
    }

    @Test
    fun callbackReturnDictionaryArrayN() = withRustLib {
        assertTrue(natives.testrs.callbackReturnDictionaryArrayN {
            arrayOf(null, null)
        })
    }
}