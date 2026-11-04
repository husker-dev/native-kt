@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.cpp.arrays

import natives.test.cpp.MyDictionary
import natives.test.cpp.MyEnum
import natives.test.cpp.MyInterface
import withCppLib
import kotlin.test.Test
import kotlin.test.assertTrue

class Cpp_CallbackReturnArray {

    @Test
    fun callbackReturnCharArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnCharArray { charArrayOf('a', 'b') })
    }

    @Test
    fun callbackReturnCharArrayEmpty() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnCharArrayEmpty { charArrayOf() })
    }

    @Test
    fun callbackReturnCharArrayN() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnCharArrayN { null })
    }

    @Test
    fun callbackReturnBooleanArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnBooleanArray { booleanArrayOf(true, false) })
    }

    @Test
    fun callbackReturnByteArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnByteArray { byteArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUByteArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnUbyteArray { ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE) })
    }

    @Test
    fun callbackReturnShortArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnShortArray { shortArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUShortArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnUshortArray { ushortArrayOf(1.toUShort(), UShort.MAX_VALUE) })
    }

    @Test
    fun callbackReturnIntArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnIntArray { intArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUIntArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnUintArray { uintArrayOf(1.toUInt(), UInt.MAX_VALUE) })
    }

    @Test
    fun callbackReturnLongArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnLongArray { longArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnULongArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnUlongArray { ulongArrayOf(1.toULong(), ULong.MAX_VALUE) })
    }

    @Test
    fun callbackReturnFloatArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnFloatArray { floatArrayOf(1.1f, 2.2f) })
    }

    @Test
    fun callbackReturnDoubleArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnDoubleArray { doubleArrayOf(1.1, 2.2) })
    }

    @Test
    fun callbackReturnStringArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnStringArray { arrayOf("string1", "string2") })
    }

    @Test
    fun callbackReturnStringArrayN() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnStringArrayN { arrayOf(null, null) })
    }

    @Test
    fun callbackReturnEnumArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnEnumArray { arrayOf(MyEnum.CASE1, MyEnum.CASE2) })
    }

    @Test
    fun callbackReturnDictionaryArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnDictionaryArray {
            arrayOf(
                MyDictionary(1, 2, 3, 4),
                MyDictionary(5, 6, 7, 8)
            )
        })
    }

    @Test
    fun callbackReturnDictionaryArrayN() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnDictionaryArrayN {
            arrayOf(null, null)
        })
    }

    @Test
    fun callbackReturnInterfaceArray() = withCppLib {
        val array = arrayOf(
            MyInterface(),
            MyInterface()
        )
        assertTrue(natives.test.cpp.callbackReturnInterfaceArray {
            array
        })
        array.forEach { it.close() }
    }

    @Test
    fun callbackReturnInterfaceArrayN() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnInterfaceArrayN {
            arrayOf(null, null)
        })
    }
}