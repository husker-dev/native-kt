@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.swift.arrays

import natives.test.swift.MyDictionary
import natives.test.swift.MyEnum
import withSwiftLib
import kotlin.test.Test
import kotlin.test.assertTrue

class Swift_CallbackArgArray {

    @Test
    fun callbackArgCharArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgCharArray { it.contentEquals(charArrayOf('a', 'b')) })
    }

    @Test
    fun callbackArgCharArrayEmpty() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgCharArrayEmpty { it.contentEquals(charArrayOf()) })
    }

    @Test
    fun callbackArgCharArrayN() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgCharArrayN { it == null })
    }

    @Test
    fun callbackArgBooleanArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgBooleanArray { it.contentEquals(booleanArrayOf(true, false)) })
    }

    @Test
    fun callbackArgByteArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgByteArray { it.contentEquals(byteArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUByteArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgUbyteArray { it.contentEquals(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)) })
    }

    @Test
    fun callbackArgShortArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgShortArray { it.contentEquals(shortArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUShortArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgUshortArray {
            it.contentEquals(
                ushortArrayOf(
                    1.toUShort(),
                    UShort.MAX_VALUE
                )
            )
        })
    }

    @Test
    fun callbackArgIntArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgIntArray { it.contentEquals(intArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUIntArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgUintArray { it.contentEquals(uintArrayOf(1.toUInt(), UInt.MAX_VALUE)) })
    }

    @Test
    fun callbackArgLongArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgLongArray { it.contentEquals(longArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgULongArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgUlongArray { it.contentEquals(ulongArrayOf(1.toULong(), ULong.MAX_VALUE)) })
    }

    @Test
    fun callbackArgFloatArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgFloatArray { it.contentEquals(floatArrayOf(1.1f, 2.2f)) })
    }

    @Test
    fun callbackArgDoubleArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgDoubleArray { it.contentEquals(doubleArrayOf(1.1, 2.2)) })
    }

    @Test
    fun callbackArgStringArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgStringArray { it.contentEquals(arrayOf("string1", "string2")) })
    }

    @Test
    fun callbackArgStringArrayN() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgStringArrayN { it.contentEquals(arrayOf(null, null)) })
    }

    @Test
    fun callbackArgEnumArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgEnumArray { it.contentEquals(arrayOf(MyEnum.CASE1, MyEnum.CASE2)) })
    }

    @Test
    fun callbackArgDictionaryArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgDictionaryArray {
            it.contentEquals(
                arrayOf(
                    MyDictionary(1, 2, 3, 4),
                    MyDictionary(5, 6, 7, 8)
                )
            )
        })
    }

    @Test
    fun callbackArgDictionaryArrayN() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgDictionaryArrayN {
            it.contentEquals(arrayOf(null, null))
        })
    }

    @Test
    fun callbackArgInterfaceArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgInterfaceArray {
            val result = it[0].test() && it[1].test()
            it.forEach { e -> e.close() }
            result
        })
    }

    @Test
    fun callbackArgInterfaceArrayN() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgInterfaceArrayN {
            it.contentEquals(arrayOf(null, null))
        })
    }
}