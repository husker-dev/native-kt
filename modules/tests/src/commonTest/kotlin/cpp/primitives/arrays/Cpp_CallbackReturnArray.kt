@file:OptIn(ExperimentalUnsignedTypes::class)

package cpp.primitives.arrays

import natives.testcpp.MyDictionary
import natives.testcpp.MyEnum
import withCppLib
import kotlin.test.Test
import kotlin.test.assertTrue

class Cpp_CallbackReturnArray {

    @Test
    fun callbackReturnCharArray() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnCharArray { charArrayOf('a', 'b') })
    }

    @Test
    fun callbackReturnCharArrayN() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnCharArrayN { null })
    }

    @Test
    fun callbackReturnBooleanArray() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnBooleanArray { booleanArrayOf(true, false) })
    }

    @Test
    fun callbackReturnByteArray() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnByteArray { byteArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUByteArray() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnUByteArray { ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE) })
    }

    @Test
    fun callbackReturnShortArray() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnShortArray { shortArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUShortArray() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnUShortArray { ushortArrayOf(1.toUShort(), UShort.MAX_VALUE) })
    }

    @Test
    fun callbackReturnIntArray() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnIntArray { intArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUIntArray() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnUIntArray { uintArrayOf(1.toUInt(), UInt.MAX_VALUE) })
    }

    @Test
    fun callbackReturnLongArray() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnLongArray { longArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnULongArray() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnULongArray { ulongArrayOf(1.toULong(), ULong.MAX_VALUE) })
    }

    @Test
    fun callbackReturnFloatArray() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnFloatArray { floatArrayOf(1.1f, 2.2f) })
    }

    @Test
    fun callbackReturnDoubleArray() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnDoubleArray { doubleArrayOf(1.1, 2.2) })
    }

    @Test
    fun callbackReturnStringArray() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnStringArray { arrayOf("string1", "string2") })
    }

    @Test
    fun callbackReturnStringArrayN() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnStringArrayN { arrayOf(null, null) })
    }

    @Test
    fun callbackReturnEnumArray() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnEnumArray { arrayOf(MyEnum.CASE1, MyEnum.CASE2) })
    }

    @Test
    fun callbackReturnDictionaryArray() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnDictionaryArray {
            arrayOf(
                MyDictionary(1, 2, 3, 4),
                MyDictionary(5, 6, 7, 8)
            )
        })
    }

    @Test
    fun callbackReturnDictionaryArrayN() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnDictionaryArrayN {
            arrayOf(null, null)
        })
    }
}