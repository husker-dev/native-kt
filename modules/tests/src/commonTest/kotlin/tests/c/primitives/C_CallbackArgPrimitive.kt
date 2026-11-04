package tests.c.primitives

import withCLib
import natives.test.c.MyDictionary
import natives.test.c.MyEnum
import natives.test.c.VoidCallback
import natives.test.c.MyInterface
import kotlin.test.Test
import kotlin.test.assertTrue

class C_CallbackArgPrimitive {

    @Test
    fun callbackVoid() = withCLib {
        var pass = false
        natives.test.c.callbackVoid { pass = true }
        assertTrue(pass)
    }

    @Test
    fun callbackVoidN() = withCLib {
        assertTrue(natives.test.c.callbackVoidN(null))
    }

    @Test
    fun callbackArgChar() = withCLib {
        assertTrue(natives.test.c.callbackArgChar { it == 'a' })
    }

    @Test
    fun callbackArgBoolean() = withCLib {
        assertTrue(natives.test.c.callbackArgBoolean { it })
    }

    @Test
    fun callbackArgByte() = withCLib {
        assertTrue(natives.test.c.callbackArgByte { it == 1.toByte() })
    }

    @Test
    fun callbackArgUByte() = withCLib {
        assertTrue(natives.test.c.callbackArgUbyte { it == UByte.MAX_VALUE })
    }

    @Test
    fun callbackArgShort() = withCLib {
        assertTrue(natives.test.c.callbackArgShort { it == 1.toShort() })
    }

    @Test
    fun callbackArgUShort() = withCLib {
        assertTrue(natives.test.c.callbackArgUshort { it == UShort.MAX_VALUE })
    }

    @Test
    fun callbackArgInt() = withCLib {
        assertTrue(natives.test.c.callbackArgInt { it == 1 })
    }

    @Test
    fun callbackArgUInt() = withCLib {
        assertTrue(natives.test.c.callbackArgUint { it == UInt.MAX_VALUE })
    }

    @Test
    fun callbackArgLong() = withCLib {
        assertTrue(natives.test.c.callbackArgLong { it == 1.toLong() })
    }

    @Test
    fun callbackArgULong() = withCLib {
        assertTrue(natives.test.c.callbackArgUlong { it == ULong.MAX_VALUE })
    }

    @Test
    fun callbackArgFloat() = withCLib {
        assertTrue(natives.test.c.callbackArgFloat { it == 1.1f })
    }

    @Test
    fun callbackArgDouble() = withCLib {
        assertTrue(natives.test.c.callbackArgDouble { it == 1.1 })
    }

    @Test
    fun callbackArgString() = withCLib {
        assertTrue(natives.test.c.callbackArgString { it == "test string" })
    }

    @Test
    fun callbackArgStringEmpty() = withCLib {
        assertTrue(natives.test.c.callbackArgStringEmpty { it == "" })
    }

    @Test
    fun callbackArgStringN() = withCLib {
        assertTrue(natives.test.c.callbackArgStringN { it == null })
    }

    @Test
    fun callbackArgCallback() = withCLib {
        val toPass = VoidCallback {}
        assertTrue(natives.test.c.callbackArgCallback(toPass) { it == toPass })
    }

    @Test
    fun callbackArgCallbackN() = withCLib {
        assertTrue(natives.test.c.callbackArgCallbackN { it == null })
    }

    @Test
    fun callbackArgEnum() = withCLib {
        assertTrue(natives.test.c.callbackArgEnum { it == MyEnum.CASE2 })
    }

    @Test
    fun callbackArgDictionary() = withCLib {
        assertTrue(natives.test.c.callbackArgDictionary { it == MyDictionary(1, 2, 3, 4) })
    }

    @Test
    fun callbackArgDictionaryN() = withCLib {
        assertTrue(natives.test.c.callbackArgDictionaryN { it == null })
    }

    @Test
    fun callbackArgInterface() = withCLib {
        val obj = MyInterface()
        assertTrue(natives.test.c.callbackArgInterface(obj) { arg ->
            arg.use { arg == obj }
        })
        obj.close()
    }

    @Test
    fun callbackArgInterfaceN() = withCLib {
        assertTrue(natives.test.c.callbackArgInterfaceN { it == null })
    }
}