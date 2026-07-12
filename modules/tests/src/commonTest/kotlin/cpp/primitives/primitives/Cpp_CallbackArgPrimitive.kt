package cpp.primitives.primitives

import natives.testcpp.MyDictionary
import natives.testcpp.MyEnum
import natives.testcpp.VoidCallback
import withCppLib
import kotlin.test.Test
import kotlin.test.assertTrue

class Cpp_CallbackArgPrimitive {

    @Test
    fun callbackVoid() = withCppLib {
        var pass = false
        natives.testcpp.callbackVoid { pass = true }
        assertTrue(pass)
    }

    @Test
    fun callbackVoidN() = withCppLib {
        assertTrue(natives.testcpp.callbackVoidN(null))
    }

    @Test
    fun callbackArgChar() = withCppLib {
        assertTrue(natives.testcpp.callbackArgChar { it == 'a' })
    }

    @Test
    fun callbackArgBoolean() = withCppLib {
        assertTrue(natives.testcpp.callbackArgBoolean { it })
    }

    @Test
    fun callbackArgByte() = withCppLib {
        assertTrue(natives.testcpp.callbackArgByte { it == 1.toByte() })
    }

    @Test
    fun callbackArgUByte() = withCppLib {
        assertTrue(natives.testcpp.callbackArgUByte { it == UByte.MAX_VALUE })
    }

    @Test
    fun callbackArgShort() = withCppLib {
        assertTrue(natives.testcpp.callbackArgShort { it == 1.toShort() })
    }

    @Test
    fun callbackArgUShort() = withCppLib {
        assertTrue(natives.testcpp.callbackArgUShort { it == UShort.MAX_VALUE })
    }

    @Test
    fun callbackArgInt() = withCppLib {
        assertTrue(natives.testcpp.callbackArgInt { it == 1 })
    }

    @Test
    fun callbackArgUInt() = withCppLib {
        assertTrue(natives.testcpp.callbackArgUInt { it == UInt.MAX_VALUE })
    }

    @Test
    fun callbackArgLong() = withCppLib {
        assertTrue(natives.testcpp.callbackArgLong { it == 1.toLong() })
    }

    @Test
    fun callbackArgULong() = withCppLib {
        assertTrue(natives.testcpp.callbackArgULong { it == ULong.MAX_VALUE })
    }

    @Test
    fun callbackArgFloat() = withCppLib {
        assertTrue(natives.testcpp.callbackArgFloat { it == 1.1f })
    }

    @Test
    fun callbackArgDouble() = withCppLib {
        assertTrue(natives.testcpp.callbackArgDouble { it == 1.1 })
    }

    @Test
    fun callbackArgString() = withCppLib {
        assertTrue(natives.testcpp.callbackArgString { it == "test string" })
    }

    @Test
    fun callbackArgStringN() = withCppLib {
        assertTrue(natives.testcpp.callbackArgStringN { it == null })
    }

    @Test
    fun callbackArgCallback() = withCppLib {
        val toPass = VoidCallback {}
        assertTrue(natives.testcpp.callbackArgCallback(toPass) { it == toPass })
    }

    @Test
    fun callbackArgCallbackN() = withCppLib {
        assertTrue(natives.testcpp.callbackArgCallbackN { it == null })
    }

    @Test
    fun callbackArgEnum() = withCppLib {
        assertTrue(natives.testcpp.callbackArgEnum { it == MyEnum.CASE2 })
    }

    @Test
    fun callbackArgDictionary() = withCppLib {
        assertTrue(natives.testcpp.callbackArgDictionary { it == MyDictionary(1, 2, 3, 4) })
    }

    @Test
    fun callbackArgDictionaryN() = withCppLib {
        assertTrue(natives.testcpp.callbackArgDictionaryN { it == null })
    }
}