package tests.swift.primitives

import withSwiftLib
import natives.test.swift.MyDictionary
import natives.test.swift.MyEnum
import natives.test.swift.MyInterface
import natives.test.swift.VoidCallback
import kotlin.test.Test
import kotlin.test.assertTrue

class Swift_CallbackArgPrimitive {

    @Test
    fun callbackVoid() = withSwiftLib {
        var pass = false
        natives.test.swift.callbackVoid { pass = true }
        assertTrue(pass)
    }

    @Test
    fun callbackVoidN() = withSwiftLib {
        assertTrue(natives.test.swift.callbackVoidN(null))
    }

    @Test
    fun callbackArgChar() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgChar { it == 'a' })
    }

    @Test
    fun callbackArgBoolean() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgBoolean { it })
    }

    @Test
    fun callbackArgByte() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgByte { it == 1.toByte() })
    }

    @Test
    fun callbackArgUByte() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgUbyte { it == UByte.MAX_VALUE })
    }

    @Test
    fun callbackArgShort() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgShort { it == 1.toShort() })
    }

    @Test
    fun callbackArgUShort() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgUshort { it == UShort.MAX_VALUE })
    }

    @Test
    fun callbackArgInt() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgInt { it == 1 })
    }

    @Test
    fun callbackArgUInt() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgUint { it == UInt.MAX_VALUE })
    }

    @Test
    fun callbackArgLong() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgLong { it == 1.toLong() })
    }

    @Test
    fun callbackArgULong() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgUlong { it == ULong.MAX_VALUE })
    }

    @Test
    fun callbackArgFloat() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgFloat { it == 1.1f })
    }

    @Test
    fun callbackArgDouble() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgDouble { it == 1.1 })
    }

    @Test
    fun callbackArgString() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgString { it == "test string" })
    }

    @Test
    fun callbackArgStringEmpty() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgStringEmpty { it == "" })
    }

    @Test
    fun callbackArgStringN() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgStringN { it == null })
    }

    @Test
    fun callbackArgCallback() = withSwiftLib {
        val toPass = VoidCallback {}
        assertTrue(natives.test.swift.callbackArgCallback(toPass) { it == toPass })
    }

    @Test
    fun callbackArgCallbackN() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgCallbackN { it == null })
    }

    @Test
    fun callbackArgEnum() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgEnum { it == MyEnum.CASE2 })
    }

    @Test
    fun callbackArgDictionary() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgDictionary { it == MyDictionary(1, 2, 3, 4) })
    }

    @Test
    fun callbackArgDictionaryN() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgDictionaryN { it == null })
    }

    @Test
    fun callbackArgInterface() = withSwiftLib {
        val obj = MyInterface()
        assertTrue(natives.test.swift.callbackArgInterface(obj) { it == obj })
    }

    @Test
    fun callbackArgInterfaceN() = withSwiftLib {
        assertTrue(natives.test.swift.callbackArgInterfaceN { it == null })
    }
}