@file:OptIn(ExperimentalUnsignedTypes::class)

package cpp.primitives.arrays

import natives.testcpp.MyDictionary
import natives.testcpp.MyEnum
import withCppLib
import kotlin.test.Test
import kotlin.test.assertTrue

class Cpp_CallbackArgArray {

    @Test
    fun callbackArgCharArray() = withCppLib {
        assertTrue(natives.testcpp.callbackArgCharArray { it.contentEquals(charArrayOf('a', 'b')) })
    }

    @Test
    fun callbackArgCharArrayN() = withCppLib {
        assertTrue(natives.testcpp.callbackArgCharArrayN { it == null })
    }

    @Test
    fun callbackArgBooleanArray() = withCppLib {
        assertTrue(natives.testcpp.callbackArgBooleanArray { it.contentEquals(booleanArrayOf(true, false)) })
    }

    @Test
    fun callbackArgByteArray() = withCppLib {
        assertTrue(natives.testcpp.callbackArgByteArray { it.contentEquals(byteArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUByteArray() = withCppLib {
        assertTrue(natives.testcpp.callbackArgUByteArray { it.contentEquals(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)) })
    }

    @Test
    fun callbackArgShortArray() = withCppLib {
        assertTrue(natives.testcpp.callbackArgShortArray { it.contentEquals(shortArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUShortArray() = withCppLib {
        assertTrue(natives.testcpp.callbackArgUShortArray {
            it.contentEquals(
                ushortArrayOf(
                    1.toUShort(),
                    UShort.MAX_VALUE
                )
            )
        })
    }

    @Test
    fun callbackArgIntArray() = withCppLib {
        assertTrue(natives.testcpp.callbackArgIntArray { it.contentEquals(intArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUIntArray() = withCppLib {
        assertTrue(natives.testcpp.callbackArgUIntArray { it.contentEquals(uintArrayOf(1.toUInt(), UInt.MAX_VALUE)) })
    }

    @Test
    fun callbackArgLongArray() = withCppLib {
        assertTrue(natives.testcpp.callbackArgLongArray { it.contentEquals(longArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgULongArray() = withCppLib {
        assertTrue(natives.testcpp.callbackArgULongArray { it.contentEquals(ulongArrayOf(1.toULong(), ULong.MAX_VALUE)) })
    }

    @Test
    fun callbackArgFloatArray() = withCppLib {
        assertTrue(natives.testcpp.callbackArgFloatArray { it.contentEquals(floatArrayOf(1.1f, 2.2f)) })
    }

    @Test
    fun callbackArgDoubleArray() = withCppLib {
        assertTrue(natives.testcpp.callbackArgDoubleArray { it.contentEquals(doubleArrayOf(1.1, 2.2)) })
    }

    @Test
    fun callbackArgStringArray() = withCppLib {
        assertTrue(natives.testcpp.callbackArgStringArray { it.contentEquals(arrayOf("string1", "string2")) })
    }

    @Test
    fun callbackArgStringArrayN() = withCppLib {
        assertTrue(natives.testcpp.callbackArgStringArrayN { it.contentEquals(arrayOf(null, null)) })
    }

    @Test
    fun callbackArgEnumArray() = withCppLib {
        assertTrue(natives.testcpp.callbackArgEnumArray { it.contentEquals(arrayOf(MyEnum.CASE1, MyEnum.CASE2)) })
    }

    @Test
    fun callbackArgDictionaryArray() = withCppLib {
        assertTrue(natives.testcpp.callbackArgDictionaryArray {
            it.contentEquals(
                arrayOf(
                    MyDictionary(1, 2, 3, 4),
                    MyDictionary(5, 6, 7, 8)
                )
            )
        })
    }

    @Test
    fun callbackArgDictionaryArrayN() = withCppLib {
        assertTrue(natives.testcpp.callbackArgDictionaryArrayN {
            it.contentEquals(arrayOf(null, null))
        })
    }
}