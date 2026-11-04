@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.rust.arrays

import natives.test.rust.MyDictionary
import natives.test.rust.MyEnum
import withRustLib
import kotlin.test.Test
import kotlin.test.assertTrue

class Rust_CallbackArgArray {

    @Test
    fun callbackArgCharArray() = withRustLib {
        assertTrue(natives.test.rust.callbackArgCharArray { it.contentEquals(charArrayOf('a', 'b')) })
    }

    @Test
    fun callbackArgCharArrayEmpty() = withRustLib {
        assertTrue(natives.test.rust.callbackArgCharArrayEmpty { it.contentEquals(charArrayOf()) })
    }

    @Test
    fun callbackArgCharArrayN() = withRustLib {
        assertTrue(natives.test.rust.callbackArgCharArrayN { it == null })
    }

    @Test
    fun callbackArgBooleanArray() = withRustLib {
        assertTrue(natives.test.rust.callbackArgBooleanArray { it.contentEquals(booleanArrayOf(true, false)) })
    }

    @Test
    fun callbackArgByteArray() = withRustLib {
        assertTrue(natives.test.rust.callbackArgByteArray { it.contentEquals(byteArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUByteArray() = withRustLib {
        assertTrue(natives.test.rust.callbackArgUbyteArray { it.contentEquals(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)) })
    }

    @Test
    fun callbackArgShortArray() = withRustLib {
        assertTrue(natives.test.rust.callbackArgShortArray { it.contentEquals(shortArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUShortArray() = withRustLib {
        assertTrue(natives.test.rust.callbackArgUshortArray {
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
        assertTrue(natives.test.rust.callbackArgIntArray { it.contentEquals(intArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUIntArray() = withRustLib {
        assertTrue(natives.test.rust.callbackArgUintArray { it.contentEquals(uintArrayOf(1.toUInt(), UInt.MAX_VALUE)) })
    }

    @Test
    fun callbackArgLongArray() = withRustLib {
        assertTrue(natives.test.rust.callbackArgLongArray { it.contentEquals(longArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgULongArray() = withRustLib {
        assertTrue(natives.test.rust.callbackArgUlongArray { it.contentEquals(ulongArrayOf(1.toULong(), ULong.MAX_VALUE)) })
    }

    @Test
    fun callbackArgFloatArray() = withRustLib {
        assertTrue(natives.test.rust.callbackArgFloatArray { it.contentEquals(floatArrayOf(1.1f, 2.2f)) })
    }

    @Test
    fun callbackArgDoubleArray() = withRustLib {
        assertTrue(natives.test.rust.callbackArgDoubleArray { it.contentEquals(doubleArrayOf(1.1, 2.2)) })
    }

    @Test
    fun callbackArgStringArray() = withRustLib {
        assertTrue(natives.test.rust.callbackArgStringArray { it.contentEquals(arrayOf("string1", "string2")) })
    }

    @Test
    fun callbackArgStringArrayN() = withRustLib {
        assertTrue(natives.test.rust.callbackArgStringArrayN { it.contentEquals(arrayOf(null, null)) })
    }

    @Test
    fun callbackArgEnumArray() = withRustLib {
        assertTrue(natives.test.rust.callbackArgEnumArray { it.contentEquals(arrayOf(MyEnum.CASE1, MyEnum.CASE2)) })
    }

    @Test
    fun callbackArgDictionaryArray() = withRustLib {
        assertTrue(natives.test.rust.callbackArgDictionaryArray {
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
        assertTrue(natives.test.rust.callbackArgDictionaryArrayN {
            it.contentEquals(arrayOf(null, null))
        })
    }

    @Test
    fun callbackArgInterfaceArray() = withRustLib {
        assertTrue(natives.test.rust.callbackArgInterfaceArray {
            val result = it[0].test() && it[1].test()
            it.forEach { e -> e.close() }
            result
        })
    }

    @Test
    fun callbackArgInterfaceArrayN() = withRustLib {
        assertTrue(natives.test.rust.callbackArgInterfaceArrayN {
            it.contentEquals(arrayOf(null, null))
        })
    }
}