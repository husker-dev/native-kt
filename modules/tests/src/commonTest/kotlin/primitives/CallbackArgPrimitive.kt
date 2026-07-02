package primitives

import natives.test.MyDictionary
import natives.test.MyEnum
import natives.test.VoidCallback
import withLib
import kotlin.test.Test
import kotlin.test.assertTrue

class CallbackArgPrimitive {

    @Test
    fun callbackVoid() = withLib {
        var pass = false
        natives.test.callbackVoid { pass = true }
        assertTrue(pass)
    }

    @Test
    fun callbackVoidN() = withLib {
        assertTrue(natives.test.callbackVoidN(null))
    }

    @Test
    fun callbackArgChar() = withLib {
        assertTrue(natives.test.callbackArgChar { it == 'a' })
    }

    @Test
    fun callbackArgBoolean() = withLib {
        assertTrue(natives.test.callbackArgBoolean { it })
    }

    @Test
    fun callbackArgByte() = withLib {
        assertTrue(natives.test.callbackArgByte { it == 1.toByte() })
    }

    @Test
    fun callbackArgUByte() = withLib {
        assertTrue(natives.test.callbackArgUByte { it == UByte.MAX_VALUE })
    }

    @Test
    fun callbackArgShort() = withLib {
        assertTrue(natives.test.callbackArgShort { it == 1.toShort() })
    }

    @Test
    fun callbackArgUShort() = withLib {
        assertTrue(natives.test.callbackArgUShort { it == UShort.MAX_VALUE })
    }

    @Test
    fun callbackArgInt() = withLib {
        assertTrue(natives.test.callbackArgInt { it == 1 })
    }

    @Test
    fun callbackArgUInt() = withLib {
        assertTrue(natives.test.callbackArgUInt { it == UInt.MAX_VALUE })
    }

    @Test
    fun callbackArgLong() = withLib {
        assertTrue(natives.test.callbackArgLong { it == 1.toLong() })
    }

    @Test
    fun callbackArgULong() = withLib {
        assertTrue(natives.test.callbackArgULong { it == ULong.MAX_VALUE })
    }

    @Test
    fun callbackArgFloat() = withLib {
        assertTrue(natives.test.callbackArgFloat { it == 1.1f })
    }

    @Test
    fun callbackArgDouble() = withLib {
        assertTrue(natives.test.callbackArgDouble { it == 1.1 })
    }

    @Test
    fun callbackArgString() = withLib {
        assertTrue(natives.test.callbackArgString { it == "test string" })
    }

    @Test
    fun callbackArgStringN() = withLib {
        assertTrue(natives.test.callbackArgStringN { it == null })
    }

    @Test
    fun callbackArgCallback() = withLib {
        val toPass = VoidCallback {}
        assertTrue(natives.test.callbackArgCallback(toPass) { it == toPass })
    }

    @Test
    fun callbackArgCallbackN() = withLib {
        assertTrue(natives.test.callbackArgCallbackN { it == null })
    }

    @Test
    fun callbackArgEnum() = withLib {
        assertTrue(natives.test.callbackArgEnum { it == MyEnum.CASE2 })
    }

    @Test
    fun callbackArgDictionary() = withLib {
        assertTrue(natives.test.callbackArgDictionary { it == MyDictionary(1, 2, 3, 4) })
    }

    @Test
    fun callbackArgDictionaryN() = withLib {
        assertTrue(natives.test.callbackArgDictionaryN { it == null })
    }
}