@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.cpp.arrays

import natives.test.cpp.MyDictionary
import natives.test.cpp.MyEnum
import withCppLib
import kotlin.test.Test
import kotlin.test.assertTrue

class Cpp_CallbackArgArray {

    @Test
    fun callbackArgCharArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgCharArray { it.contentEquals(charArrayOf('a', 'b')) })
    }

    @Test
    fun callbackArgCharArrayEmpty() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgCharArrayEmpty { it.contentEquals(charArrayOf()) })
    }

    @Test
    fun callbackArgCharArrayN() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgCharArrayN { it == null })
    }

    @Test
    fun callbackArgBooleanArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgBooleanArray { it.contentEquals(booleanArrayOf(true, false)) })
    }

    @Test
    fun callbackArgByteArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgByteArray { it.contentEquals(byteArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUByteArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgUbyteArray { it.contentEquals(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)) })
    }

    @Test
    fun callbackArgShortArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgShortArray { it.contentEquals(shortArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUShortArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgUshortArray {
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
        assertTrue(natives.test.cpp.callbackArgIntArray { it.contentEquals(intArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUIntArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgUintArray { it.contentEquals(uintArrayOf(1.toUInt(), UInt.MAX_VALUE)) })
    }

    @Test
    fun callbackArgLongArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgLongArray { it.contentEquals(longArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgULongArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgUlongArray { it.contentEquals(ulongArrayOf(1.toULong(), ULong.MAX_VALUE)) })
    }

    @Test
    fun callbackArgFloatArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgFloatArray { it.contentEquals(floatArrayOf(1.1f, 2.2f)) })
    }

    @Test
    fun callbackArgDoubleArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgDoubleArray { it.contentEquals(doubleArrayOf(1.1, 2.2)) })
    }

    @Test
    fun callbackArgStringArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgStringArray { it.contentEquals(arrayOf("string1", "string2")) })
    }

    @Test
    fun callbackArgStringArrayN() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgStringArrayN { it.contentEquals(arrayOf(null, null)) })
    }

    @Test
    fun callbackArgEnumArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgEnumArray { it.contentEquals(arrayOf(MyEnum.CASE1, MyEnum.CASE2)) })
    }

    @Test
    fun callbackArgDictionaryArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgDictionaryArray {
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
        assertTrue(natives.test.cpp.callbackArgDictionaryArrayN {
            it.contentEquals(arrayOf(null, null))
        })
    }

    @Test
    fun callbackArgInterfaceArray() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgInterfaceArray {
            val result = it[0].test() && it[1].test()
            it.forEach { it.close() }
            result
        })
    }

    @Test
    fun callbackArgInterfaceArrayN() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgInterfaceArrayN {
            it.contentEquals(arrayOf(null, null))
        })
    }
}