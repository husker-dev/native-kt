@file:OptIn(ExperimentalUnsignedTypes::class)

package c.arrays

import withCLib
import natives.test.MyDictionary
import natives.test.MyEnum
import kotlin.test.Test
import kotlin.test.assertTrue

class C_CallbackArgArray {

    @Test
    fun callbackArgCharArray() = withCLib {
        assertTrue(natives.test.callbackArgCharArray { it.contentEquals(charArrayOf('a', 'b')) })
    }

    @Test
    fun callbackArgCharArrayN() = withCLib {
        assertTrue(natives.test.callbackArgCharArrayN { it == null })
    }

    @Test
    fun callbackArgBooleanArray() = withCLib {
        assertTrue(natives.test.callbackArgBooleanArray { it.contentEquals(booleanArrayOf(true, false)) })
    }

    @Test
    fun callbackArgByteArray() = withCLib {
        assertTrue(natives.test.callbackArgByteArray { it.contentEquals(byteArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUByteArray() = withCLib {
        assertTrue(natives.test.callbackArgUByteArray { it.contentEquals(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)) })
    }

    @Test
    fun callbackArgShortArray() = withCLib {
        assertTrue(natives.test.callbackArgShortArray { it.contentEquals(shortArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUShortArray() = withCLib {
        assertTrue(natives.test.callbackArgUShortArray {
            it.contentEquals(
                ushortArrayOf(
                    1.toUShort(),
                    UShort.MAX_VALUE
                )
            )
        })
    }

    @Test
    fun callbackArgIntArray() = withCLib {
        assertTrue(natives.test.callbackArgIntArray { it.contentEquals(intArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUIntArray() = withCLib {
        assertTrue(natives.test.callbackArgUIntArray { it.contentEquals(uintArrayOf(1.toUInt(), UInt.MAX_VALUE)) })
    }

    @Test
    fun callbackArgLongArray() = withCLib {
        assertTrue(natives.test.callbackArgLongArray { it.contentEquals(longArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgULongArray() = withCLib {
        assertTrue(natives.test.callbackArgULongArray { it.contentEquals(ulongArrayOf(1.toULong(), ULong.MAX_VALUE)) })
    }

    @Test
    fun callbackArgFloatArray() = withCLib {
        assertTrue(natives.test.callbackArgFloatArray { it.contentEquals(floatArrayOf(1.1f, 2.2f)) })
    }

    @Test
    fun callbackArgDoubleArray() = withCLib {
        assertTrue(natives.test.callbackArgDoubleArray { it.contentEquals(doubleArrayOf(1.1, 2.2)) })
    }

    @Test
    fun callbackArgStringArray() = withCLib {
        assertTrue(natives.test.callbackArgStringArray { it.contentEquals(arrayOf("string1", "string2")) })
    }

    @Test
    fun callbackArgStringArrayN() = withCLib {
        assertTrue(natives.test.callbackArgStringArrayN { it.contentEquals(arrayOf(null, null)) })
    }

    @Test
    fun callbackArgEnumArray() = withCLib {
        assertTrue(natives.test.callbackArgEnumArray { it.contentEquals(arrayOf(MyEnum.CASE1, MyEnum.CASE2)) })
    }

    @Test
    fun callbackArgDictionaryArray() = withCLib {
        assertTrue(natives.test.callbackArgDictionaryArray {
            it.contentEquals(
                arrayOf(
                    MyDictionary(1, 2, 3, 4),
                    MyDictionary(5, 6, 7, 8)
                )
            )
        })
    }

    @Test
    fun callbackArgDictionaryArrayN() = withCLib {
        assertTrue(natives.test.callbackArgDictionaryArrayN {
            it.contentEquals(arrayOf(null, null))
        })
    }
}