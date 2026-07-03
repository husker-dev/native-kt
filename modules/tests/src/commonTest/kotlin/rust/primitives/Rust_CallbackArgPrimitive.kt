package rust.primitives

import withRustLib
import natives.testrs.MyDictionary
import natives.testrs.MyEnum
import natives.testrs.VoidCallback
import kotlin.test.Test
import kotlin.test.assertTrue

class Rust_CallbackArgPrimitive {

    @Test
    fun callbackVoid() = withRustLib {
        var pass = false
        natives.testrs.callbackVoid { pass = true }
        assertTrue(pass)
    }

    @Test
    fun callbackVoidN() = withRustLib {
        assertTrue(natives.testrs.callbackVoidN(null))
    }

    @Test
    fun callbackArgChar() = withRustLib {
        assertTrue(natives.testrs.callbackArgChar { it == 'a' })
    }

    @Test
    fun callbackArgBoolean() = withRustLib {
        assertTrue(natives.testrs.callbackArgBoolean { it })
    }

    @Test
    fun callbackArgByte() = withRustLib {
        assertTrue(natives.testrs.callbackArgByte { it == 1.toByte() })
    }

    @Test
    fun callbackArgUByte() = withRustLib {
        assertTrue(natives.testrs.callbackArgUByte { it == UByte.MAX_VALUE })
    }

    @Test
    fun callbackArgShort() = withRustLib {
        assertTrue(natives.testrs.callbackArgShort { it == 1.toShort() })
    }

    @Test
    fun callbackArgUShort() = withRustLib {
        assertTrue(natives.testrs.callbackArgUShort { it == UShort.MAX_VALUE })
    }

    @Test
    fun callbackArgInt() = withRustLib {
        assertTrue(natives.testrs.callbackArgInt { it == 1 })
    }

    @Test
    fun callbackArgUInt() = withRustLib {
        assertTrue(natives.testrs.callbackArgUInt { it == UInt.MAX_VALUE })
    }

    @Test
    fun callbackArgLong() = withRustLib {
        assertTrue(natives.testrs.callbackArgLong { it == 1.toLong() })
    }

    @Test
    fun callbackArgULong() = withRustLib {
        assertTrue(natives.testrs.callbackArgULong { it == ULong.MAX_VALUE })
    }

    @Test
    fun callbackArgFloat() = withRustLib {
        assertTrue(natives.testrs.callbackArgFloat { it == 1.1f })
    }

    @Test
    fun callbackArgDouble() = withRustLib {
        assertTrue(natives.testrs.callbackArgDouble { it == 1.1 })
    }

    @Test
    fun callbackArgString() = withRustLib {
        assertTrue(natives.testrs.callbackArgString { it == "test string" })
    }

    @Test
    fun callbackArgStringN() = withRustLib {
        assertTrue(natives.testrs.callbackArgStringN { it == null })
    }

    @Test
    fun callbackArgCallback() = withRustLib {
        val toPass = VoidCallback {}
        assertTrue(natives.testrs.callbackArgCallback(toPass) { it == toPass })
    }

    @Test
    fun callbackArgCallbackN() = withRustLib {
        assertTrue(natives.testrs.callbackArgCallbackN { it == null })
    }

    @Test
    fun callbackArgEnum() = withRustLib {
        assertTrue(natives.testrs.callbackArgEnum { it == MyEnum.CASE2 })
    }

    @Test
    fun callbackArgDictionary() = withRustLib {
        assertTrue(natives.testrs.callbackArgDictionary { it == MyDictionary(1, 2, 3, 4) })
    }

    @Test
    fun callbackArgDictionaryN() = withRustLib {
        assertTrue(natives.testrs.callbackArgDictionaryN { it == null })
    }
}