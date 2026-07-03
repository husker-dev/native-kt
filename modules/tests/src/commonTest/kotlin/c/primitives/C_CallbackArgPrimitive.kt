package c.primitives

import withCLib
import natives.test.MyDictionary
import natives.test.MyEnum
import natives.test.VoidCallback
import kotlin.test.Test
import kotlin.test.assertTrue

class C_CallbackArgPrimitive {

    @Test
    fun callbackVoid() = withCLib {
        var pass = false
        natives.test.callbackVoid { pass = true }
        assertTrue(pass)
    }

    @Test
    fun callbackVoidN() = withCLib {
        assertTrue(natives.test.callbackVoidN(null))
    }

    @Test
    fun callbackArgChar() = withCLib {
        assertTrue(natives.test.callbackArgChar { it == 'a' })
    }

    @Test
    fun callbackArgBoolean() = withCLib {
        assertTrue(natives.test.callbackArgBoolean { it })
    }

    @Test
    fun callbackArgByte() = withCLib {
        assertTrue(natives.test.callbackArgByte { it == 1.toByte() })
    }

    @Test
    fun callbackArgUByte() = withCLib {
        assertTrue(natives.test.callbackArgUByte { it == UByte.MAX_VALUE })
    }

    @Test
    fun callbackArgShort() = withCLib {
        assertTrue(natives.test.callbackArgShort { it == 1.toShort() })
    }

    @Test
    fun callbackArgUShort() = withCLib {
        assertTrue(natives.test.callbackArgUShort { it == UShort.MAX_VALUE })
    }

    @Test
    fun callbackArgInt() = withCLib {
        assertTrue(natives.test.callbackArgInt { it == 1 })
    }

    @Test
    fun callbackArgUInt() = withCLib {
        assertTrue(natives.test.callbackArgUInt { it == UInt.MAX_VALUE })
    }

    @Test
    fun callbackArgLong() = withCLib {
        assertTrue(natives.test.callbackArgLong { it == 1.toLong() })
    }

    @Test
    fun callbackArgULong() = withCLib {
        assertTrue(natives.test.callbackArgULong { it == ULong.MAX_VALUE })
    }

    @Test
    fun callbackArgFloat() = withCLib {
        assertTrue(natives.test.callbackArgFloat { it == 1.1f })
    }

    @Test
    fun callbackArgDouble() = withCLib {
        assertTrue(natives.test.callbackArgDouble { it == 1.1 })
    }

    @Test
    fun callbackArgString() = withCLib {
        assertTrue(natives.test.callbackArgString { it == "test string" })
    }

    @Test
    fun callbackArgStringN() = withCLib {
        assertTrue(natives.test.callbackArgStringN { it == null })
    }

    @Test
    fun callbackArgCallback() = withCLib {
        val toPass = VoidCallback {}
        assertTrue(natives.test.callbackArgCallback(toPass) { it == toPass })
    }

    @Test
    fun callbackArgCallbackN() = withCLib {
        assertTrue(natives.test.callbackArgCallbackN { it == null })
    }

    @Test
    fun callbackArgEnum() = withCLib {
        assertTrue(natives.test.callbackArgEnum { it == MyEnum.CASE2 })
    }

    @Test
    fun callbackArgDictionary() = withCLib {
        assertTrue(natives.test.callbackArgDictionary { it == MyDictionary(1, 2, 3, 4) })
    }

    @Test
    fun callbackArgDictionaryN() = withCLib {
        assertTrue(natives.test.callbackArgDictionaryN { it == null })
    }
}