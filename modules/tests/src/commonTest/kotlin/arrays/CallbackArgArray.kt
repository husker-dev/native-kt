package arrays

import natives.test.MyDictionary
import natives.test.MyEnum
import withLib
import kotlin.test.Test
import kotlin.test.assertTrue

class CallbackArgArray {

    @Test
    fun callbackArgCharArray() = withLib {
        assertTrue(natives.test.callbackArgCharArray { it.contentEquals(charArrayOf('a', 'b')) })
    }

    @Test
    fun callbackArgBooleanArray() = withLib {
        assertTrue(natives.test.callbackArgBooleanArray { it.contentEquals(booleanArrayOf(true, false)) })
    }

    @Test
    fun callbackArgByteArray() = withLib {
        assertTrue(natives.test.callbackArgByteArray { it.contentEquals(byteArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgShortArray() = withLib {
        assertTrue(natives.test.callbackArgShortArray { it.contentEquals(shortArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgIntArray() = withLib {
        assertTrue(natives.test.callbackArgIntArray { it.contentEquals(intArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgLongArray() = withLib {
        assertTrue(natives.test.callbackArgLongArray { it.contentEquals(longArrayOf(1, 2)) })
    }

    @Test
    fun callbackArgFloatArray() = withLib {
        assertTrue(natives.test.callbackArgFloatArray { it.contentEquals(floatArrayOf(1.1f, 2.2f)) })
    }

    @Test
    fun callbackArgDoubleArray() = withLib {
        assertTrue(natives.test.callbackArgDoubleArray { it.contentEquals(doubleArrayOf(1.1, 2.2)) })
    }

    @Test
    fun callbackArgStringArray() = withLib {
        assertTrue(natives.test.callbackArgStringArray { it.contentEquals(arrayOf("string1", "string2")) })
    }

    @Test
    fun callbackArgEnumArray() = withLib {
        assertTrue(natives.test.callbackArgEnumArray { it.contentEquals(arrayOf(MyEnum.CASE1, MyEnum.CASE2)) })
    }

    @Test
    fun callbackArgDictionaryArray() = withLib {
        assertTrue(natives.test.callbackArgDictionaryArray {
            it.contentEquals(arrayOf(
                MyDictionary(1, 2, 3, 4),
                MyDictionary(5, 6, 7, 8)
            ))
        })
    }
}