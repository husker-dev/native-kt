package tests.cpp.primitives

import natives.test.cpp.MyDictionary
import natives.test.cpp.MyEnum
import natives.test.cpp.VoidCallback
import natives.test.cpp.MyInterface
import withCppLib
import kotlin.test.Test
import kotlin.test.assertTrue

class Cpp_CallbackArgPrimitive {

    @Test
    fun callbackVoid() = withCppLib {
        var pass = false
        natives.test.cpp.callbackVoid { pass = true }
        assertTrue(pass)
    }

    @Test
    fun callbackVoidN() = withCppLib {
        assertTrue(natives.test.cpp.callbackVoidN(null))
    }

    @Test
    fun callbackArgChar() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgChar { it == 'a' })
    }

    @Test
    fun callbackArgBoolean() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgBoolean { it })
    }

    @Test
    fun callbackArgByte() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgByte { it == 1.toByte() })
    }

    @Test
    fun callbackArgUByte() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgUbyte { it == UByte.MAX_VALUE })
    }

    @Test
    fun callbackArgShort() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgShort { it == 1.toShort() })
    }

    @Test
    fun callbackArgUShort() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgUshort { it == UShort.MAX_VALUE })
    }

    @Test
    fun callbackArgInt() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgInt { it == 1 })
    }

    @Test
    fun callbackArgUInt() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgUint { it == UInt.MAX_VALUE })
    }

    @Test
    fun callbackArgLong() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgLong { it == 1.toLong() })
    }

    @Test
    fun callbackArgULong() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgUlong { it == ULong.MAX_VALUE })
    }

    @Test
    fun callbackArgFloat() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgFloat { it == 1.1f })
    }

    @Test
    fun callbackArgDouble() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgDouble { it == 1.1 })
    }

    @Test
    fun callbackArgString() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgString { it == "test string" })
    }

    @Test
    fun callbackArgStringEmpty() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgStringEmpty { it == "" })
    }

    @Test
    fun callbackArgStringN() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgStringN { it == null })
    }

    @Test
    fun callbackArgCallback() = withCppLib {
        val toPass = VoidCallback {}
        assertTrue(natives.test.cpp.callbackArgCallback(toPass) { it == toPass })
    }

    @Test
    fun callbackArgCallbackN() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgCallbackN { it == null })
    }

    @Test
    fun callbackArgEnum() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgEnum { it == MyEnum.CASE2 })
    }

    @Test
    fun callbackArgDictionary() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgDictionary { it == MyDictionary(1, 2, 3, 4) })
    }

    @Test
    fun callbackArgDictionaryN() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgDictionaryN { it == null })
    }

    @Test
    fun callbackArgInterface() = withCppLib {
        val obj = MyInterface()
        assertTrue(natives.test.cpp.callbackArgInterface(obj) { arg ->
            arg.use { arg == obj }
        })
        obj.close()
    }

    @Test
    fun callbackArgInterfaceN() = withCppLib {
        assertTrue(natives.test.cpp.callbackArgInterfaceN { it == null })
    }
}