@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.rust.arrays

import withRustLib
import natives.test.rust.MyDictionary
import natives.test.rust.MyEnum
import natives.test.rust.MyInterface
import kotlin.test.Test
import kotlin.test.assertTrue

class Rust_CallbackReturnArray {

    @Test
    fun callbackReturnCharArray() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnCharArray { charArrayOf('a', 'b') })
    }

    @Test
    fun callbackReturnCharArrayEmpty() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnCharArrayEmpty { charArrayOf() })
    }

    @Test
    fun callbackReturnCharArrayN() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnCharArrayN { null })
    }

    @Test
    fun callbackReturnBooleanArray() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnBooleanArray { booleanArrayOf(true, false) })
    }

    @Test
    fun callbackReturnByteArray() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnByteArray { byteArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUByteArray() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnUbyteArray { ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE) })
    }

    @Test
    fun callbackReturnShortArray() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnShortArray { shortArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUShortArray() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnUshortArray { ushortArrayOf(1.toUShort(), UShort.MAX_VALUE) })
    }

    @Test
    fun callbackReturnIntArray() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnIntArray { intArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUIntArray() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnUintArray { uintArrayOf(1.toUInt(), UInt.MAX_VALUE) })
    }

    @Test
    fun callbackReturnLongArray() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnLongArray { longArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnULongArray() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnUlongArray { ulongArrayOf(1.toULong(), ULong.MAX_VALUE) })
    }

    @Test
    fun callbackReturnFloatArray() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnFloatArray { floatArrayOf(1.1f, 2.2f) })
    }

    @Test
    fun callbackReturnDoubleArray() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnDoubleArray { doubleArrayOf(1.1, 2.2) })
    }

    @Test
    fun callbackReturnStringArray() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnStringArray { arrayOf("string1", "string2") })
    }

    @Test
    fun callbackReturnStringArrayN() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnStringArrayN { arrayOf(null, null) })
    }

    @Test
    fun callbackReturnEnumArray() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnEnumArray { arrayOf(MyEnum.CASE1, MyEnum.CASE2) })
    }

    @Test
    fun callbackReturnDictionaryArray() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnDictionaryArray {
            arrayOf(
                MyDictionary(1, 2, 3, 4),
                MyDictionary(5, 6, 7, 8)
            )
        })
    }

    @Test
    fun callbackReturnDictionaryArrayN() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnDictionaryArrayN {
            arrayOf(null, null)
        })
    }

    @Test
    fun callbackReturnInterfaceArray() = withRustLib {
        val elements = arrayOf(
            MyInterface(),
            MyInterface()
        )
        assertTrue(natives.test.rust.callbackReturnInterfaceArray {
            elements
        })
        elements.forEach { it.close() }
    }

    @Test
    fun callbackReturnInterfaceArrayN() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnInterfaceArrayN {
            arrayOf(null, null)
        })
    }
}