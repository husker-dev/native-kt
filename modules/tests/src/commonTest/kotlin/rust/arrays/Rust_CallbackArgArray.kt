@file:OptIn(ExperimentalUnsignedTypes::class)

package rust.arrays

import withRustLib
import natives.testrs.MyDictionary
import natives.testrs.MyEnum
import kotlin.test.Test
import kotlin.test.assertTrue

class Rust_CallbackArgArray {

    @Test
    fun callbackArgCharArray() = withRustLib {
        assertTrue(natives.testrs.callbackArgCharArray { it.contentEquals(charArrayOf('a', 'b')) })
    }

    @Test
    fun callbackArgCharArrayN() = withRustLib {
        assertTrue(natives.testrs.callbackArgCharArrayN { it == null })
    }

    @Test
    fun callbackArgBooleanArray() = withRustLib {
        assertTrue(natives.testrs.callbackArgBooleanArray { it.contentEquals(booleanArrayOf(true, false)) })
    }

    @Test
    fun callbackArgByteArray() = withRustLib {
        assertTrue(natives.testrs.callbackArgByteArray { it.contentEquals(byteArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUByteArray() = withRustLib {
        assertTrue(natives.testrs.callbackArgUByteArray { it.contentEquals(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)) })
    }

    @Test
    fun callbackArgShortArray() = withRustLib {
        assertTrue(natives.testrs.callbackArgShortArray { it.contentEquals(shortArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUShortArray() = withRustLib {
        assertTrue(natives.testrs.callbackArgUShortArray {
            it.contentEquals(
                ushortArrayOf(
                    1.toUShort(),
                    UShort.MAX_VALUE
                )
            )
        })
    }

    @Test
    fun callbackArgIntArray() = withRustLib {
        assertTrue(natives.testrs.callbackArgIntArray { it.contentEquals(intArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUIntArray() = withRustLib {
        assertTrue(natives.testrs.callbackArgUIntArray { it.contentEquals(uintArrayOf(1.toUInt(), UInt.MAX_VALUE)) })
    }

    @Test
    fun callbackArgLongArray() = withRustLib {
        assertTrue(natives.testrs.callbackArgLongArray { it.contentEquals(longArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgULongArray() = withRustLib {
        assertTrue(natives.testrs.callbackArgULongArray { it.contentEquals(ulongArrayOf(1.toULong(), ULong.MAX_VALUE)) })
    }

    @Test
    fun callbackArgFloatArray() = withRustLib {
        assertTrue(natives.testrs.callbackArgFloatArray { it.contentEquals(floatArrayOf(1.1f, 2.2f)) })
    }

    @Test
    fun callbackArgDoubleArray() = withRustLib {
        assertTrue(natives.testrs.callbackArgDoubleArray { it.contentEquals(doubleArrayOf(1.1, 2.2)) })
    }

    @Test
    fun callbackArgStringArray() = withRustLib {
        assertTrue(natives.testrs.callbackArgStringArray { it.contentEquals(arrayOf("string1", "string2")) })
    }

    @Test
    fun callbackArgStringArrayN() = withRustLib {
        assertTrue(natives.testrs.callbackArgStringArrayN { it.contentEquals(arrayOf(null, null)) })
    }

    @Test
    fun callbackArgEnumArray() = withRustLib {
        assertTrue(natives.testrs.callbackArgEnumArray { it.contentEquals(arrayOf(MyEnum.CASE1, MyEnum.CASE2)) })
    }

    @Test
    fun callbackArgDictionaryArray() = withRustLib {
        assertTrue(natives.testrs.callbackArgDictionaryArray {
            it.contentEquals(
                arrayOf(
                    MyDictionary(1, 2, 3, 4),
                    MyDictionary(5, 6, 7, 8)
                )
            )
        })
    }

    @Test
    fun callbackArgDictionaryArrayN() = withRustLib {
        assertTrue(natives.testrs.callbackArgDictionaryArrayN {
            it.contentEquals(arrayOf(null, null))
        })
    }
}