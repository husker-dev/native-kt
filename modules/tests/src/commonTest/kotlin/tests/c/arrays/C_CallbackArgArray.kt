@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.c.arrays

import withCLib
import natives.test.c.MyDictionary
import natives.test.c.MyEnum
import kotlin.test.Test
import kotlin.test.assertTrue

class C_CallbackArgArray {

    @Test
    fun callbackArgCharArray() = withCLib {
        assertTrue(natives.test.c.callbackArgCharArray { it.contentEquals(charArrayOf('a', 'b')) })
    }

    @Test
    fun callbackArgCharArrayEmpty() = withCLib {
        assertTrue(natives.test.c.callbackArgCharArrayEmpty { it.contentEquals(charArrayOf()) })
    }

    @Test
    fun callbackArgCharArrayN() = withCLib {
        assertTrue(natives.test.c.callbackArgCharArrayN { it == null })
    }

    @Test
    fun callbackArgBooleanArray() = withCLib {
        assertTrue(natives.test.c.callbackArgBooleanArray { it.contentEquals(booleanArrayOf(true, false)) })
    }

    @Test
    fun callbackArgByteArray() = withCLib {
        assertTrue(natives.test.c.callbackArgByteArray { it.contentEquals(byteArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUByteArray() = withCLib {
        assertTrue(natives.test.c.callbackArgUbyteArray { it.contentEquals(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)) })
    }

    @Test
    fun callbackArgShortArray() = withCLib {
        assertTrue(natives.test.c.callbackArgShortArray { it.contentEquals(shortArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUShortArray() = withCLib {
        assertTrue(natives.test.c.callbackArgUshortArray {
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
        assertTrue(natives.test.c.callbackArgIntArray { it.contentEquals(intArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUIntArray() = withCLib {
        assertTrue(natives.test.c.callbackArgUintArray { it.contentEquals(uintArrayOf(1.toUInt(), UInt.MAX_VALUE)) })
    }

    @Test
    fun callbackArgLongArray() = withCLib {
        assertTrue(natives.test.c.callbackArgLongArray { it.contentEquals(longArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgULongArray() = withCLib {
        assertTrue(natives.test.c.callbackArgUlongArray { it.contentEquals(ulongArrayOf(1.toULong(), ULong.MAX_VALUE)) })
    }

    @Test
    fun callbackArgFloatArray() = withCLib {
        assertTrue(natives.test.c.callbackArgFloatArray { it.contentEquals(floatArrayOf(1.1f, 2.2f)) })
    }

    @Test
    fun callbackArgDoubleArray() = withCLib {
        assertTrue(natives.test.c.callbackArgDoubleArray { it.contentEquals(doubleArrayOf(1.1, 2.2)) })
    }

    @Test
    fun callbackArgStringArray() = withCLib {
        assertTrue(natives.test.c.callbackArgStringArray { it.contentEquals(arrayOf("string1", "string2")) })
    }

    @Test
    fun callbackArgStringArrayN() = withCLib {
        assertTrue(natives.test.c.callbackArgStringArrayN { it.contentEquals(arrayOf(null, null)) })
    }

    @Test
    fun callbackArgEnumArray() = withCLib {
        assertTrue(natives.test.c.callbackArgEnumArray { it.contentEquals(arrayOf(MyEnum.CASE1, MyEnum.CASE2)) })
    }

    @Test
    fun callbackArgDictionaryArray() = withCLib {
        assertTrue(natives.test.c.callbackArgDictionaryArray {
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
        assertTrue(natives.test.c.callbackArgDictionaryArrayN {
            it.contentEquals(arrayOf(null, null))
        })
    }

    @Test
    fun callbackArgInterfaceArray() = withCLib {
        assertTrue(natives.test.c.callbackArgInterfaceArray {
            val result = it[0].test() && it[1].test()
            it.forEach { it.close() }
            result
        })
    }

    @Test
    fun callbackArgInterfaceArrayN() = withCLib {
        assertTrue(natives.test.c.callbackArgInterfaceArrayN {
            it.contentEquals(arrayOf(null, null))
        })
    }
}