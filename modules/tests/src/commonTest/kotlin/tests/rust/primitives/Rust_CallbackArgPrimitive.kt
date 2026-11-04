package tests.rust.primitives

import withRustLib
import natives.test.rust.MyDictionary
import natives.test.rust.MyEnum
import natives.test.rust.MyInterface
import natives.test.rust.VoidCallback
import kotlin.test.Test
import kotlin.test.assertTrue

class Rust_CallbackArgPrimitive {

    @Test
    fun callbackVoid() = withRustLib {
        var pass = false
        natives.test.rust.callbackVoid { pass = true }
        assertTrue(pass)
    }

    @Test
    fun callbackVoidN() = withRustLib {
        assertTrue(natives.test.rust.callbackVoidN(null))
    }

    @Test
    fun callbackArgChar() = withRustLib {
        assertTrue(natives.test.rust.callbackArgChar { it == 'a' })
    }

    @Test
    fun callbackArgBoolean() = withRustLib {
        assertTrue(natives.test.rust.callbackArgBoolean { it })
    }

    @Test
    fun callbackArgByte() = withRustLib {
        assertTrue(natives.test.rust.callbackArgByte { it == 1.toByte() })
    }

    @Test
    fun callbackArgUByte() = withRustLib {
        assertTrue(natives.test.rust.callbackArgUbyte { it == UByte.MAX_VALUE })
    }

    @Test
    fun callbackArgShort() = withRustLib {
        assertTrue(natives.test.rust.callbackArgShort { it == 1.toShort() })
    }

    @Test
    fun callbackArgUShort() = withRustLib {
        assertTrue(natives.test.rust.callbackArgUshort { it == UShort.MAX_VALUE })
    }

    @Test
    fun callbackArgInt() = withRustLib {
        assertTrue(natives.test.rust.callbackArgInt { it == 1 })
    }

    @Test
    fun callbackArgUInt() = withRustLib {
        assertTrue(natives.test.rust.callbackArgUint { it == UInt.MAX_VALUE })
    }

    @Test
    fun callbackArgLong() = withRustLib {
        assertTrue(natives.test.rust.callbackArgLong { it == 1.toLong() })
    }

    @Test
    fun callbackArgULong() = withRustLib {
        assertTrue(natives.test.rust.callbackArgUlong { it == ULong.MAX_VALUE })
    }

    @Test
    fun callbackArgFloat() = withRustLib {
        assertTrue(natives.test.rust.callbackArgFloat { it == 1.1f })
    }

    @Test
    fun callbackArgDouble() = withRustLib {
        assertTrue(natives.test.rust.callbackArgDouble { it == 1.1 })
    }

    @Test
    fun callbackArgString() = withRustLib {
        assertTrue(natives.test.rust.callbackArgString { it == "test string" })
    }

    @Test
    fun callbackArgStringEmpty() = withRustLib {
        assertTrue(natives.test.rust.callbackArgStringEmpty { it == "" })
    }

    @Test
    fun callbackArgStringN() = withRustLib {
        assertTrue(natives.test.rust.callbackArgStringN { it == null })
    }

    @Test
    fun callbackArgCallback() = withRustLib {
        val toPass = VoidCallback {}
        assertTrue(natives.test.rust.callbackArgCallback(toPass) { it == toPass })
    }

    @Test
    fun callbackArgCallbackN() = withRustLib {
        assertTrue(natives.test.rust.callbackArgCallbackN { it == null })
    }

    @Test
    fun callbackArgEnum() = withRustLib {
        assertTrue(natives.test.rust.callbackArgEnum { it == MyEnum.CASE2 })
    }

    @Test
    fun callbackArgDictionary() = withRustLib {
        assertTrue(natives.test.rust.callbackArgDictionary { it == MyDictionary(1, 2, 3, 4) })
    }

    @Test
    fun callbackArgDictionaryN() = withRustLib {
        assertTrue(natives.test.rust.callbackArgDictionaryN { it == null })
    }

    @Test
    fun callbackArgInterface() = withRustLib {
        val obj = MyInterface()
        assertTrue(natives.test.rust.callbackArgInterface(obj) { it == obj })
    }

    @Test
    fun callbackArgInterfaceN() = withRustLib {
        assertTrue(natives.test.rust.callbackArgInterfaceN { it == null })
    }
}