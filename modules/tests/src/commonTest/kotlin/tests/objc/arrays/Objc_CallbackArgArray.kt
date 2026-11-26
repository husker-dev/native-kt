@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.objc.arrays

import natives.test.objc.MyDictionary
import natives.test.objc.MyEnum
import withObjcLib
import kotlin.test.Test
import kotlin.test.assertTrue

class Objc_CallbackArgArray {

    @Test
    fun callbackArgCharArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgCharArray { it.contentEquals(charArrayOf('a', 'b')) })
    }

    @Test
    fun callbackArgCharArrayEmpty() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgCharArrayEmpty { it.contentEquals(charArrayOf()) })
    }

    @Test
    fun callbackArgCharArrayN() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgCharArrayN { it == null })
    }

    @Test
    fun callbackArgBooleanArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgBooleanArray { it.contentEquals(booleanArrayOf(true, false)) })
    }

    @Test
    fun callbackArgByteArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgByteArray { it.contentEquals(byteArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUByteArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgUbyteArray { it.contentEquals(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)) })
    }

    @Test
    fun callbackArgShortArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgShortArray { it.contentEquals(shortArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUShortArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgUshortArray {
            it.contentEquals(
                ushortArrayOf(
                    1.toUShort(),
                    UShort.MAX_VALUE
                )
            )
        })
    }

    @Test
    fun callbackArgIntArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgIntArray { it.contentEquals(intArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgUIntArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgUintArray { it.contentEquals(uintArrayOf(1.toUInt(), UInt.MAX_VALUE)) })
    }

    @Test
    fun callbackArgLongArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgLongArray { it.contentEquals(longArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgULongArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgUlongArray { it.contentEquals(ulongArrayOf(1.toULong(), ULong.MAX_VALUE)) })
    }

    @Test
    fun callbackArgFloatArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgFloatArray { it.contentEquals(floatArrayOf(1.1f, 2.2f)) })
    }

    @Test
    fun callbackArgDoubleArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgDoubleArray { it.contentEquals(doubleArrayOf(1.1, 2.2)) })
    }

    @Test
    fun callbackArgStringArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgStringArray { it.contentEquals(arrayOf("string1", "string2")) })
    }

    @Test
    fun callbackArgStringArrayN() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgStringArrayN { it.contentEquals(arrayOf(null, null)) })
    }

    @Test
    fun callbackArgEnumArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgEnumArray { it.contentEquals(arrayOf(MyEnum.CASE1, MyEnum.CASE2)) })
    }

    @Test
    fun callbackArgDictionaryArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgDictionaryArray {
            it.contentEquals(
                arrayOf(
                    MyDictionary(1, 2, 3, 4),
                    MyDictionary(5, 6, 7, 8)
                )
            )
        })
    }

    @Test
    fun callbackArgDictionaryArrayN() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgDictionaryArrayN {
            it.contentEquals(arrayOf(null, null))
        })
    }

    @Test
    fun callbackArgInterfaceArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgInterfaceArray {
            val result = it[0].test() && it[1].test()
            it.forEach { it.close() }
            result
        })
    }

    @Test
    fun callbackArgInterfaceArrayN() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgInterfaceArrayN {
            it.contentEquals(arrayOf(null, null))
        })
    }
}
