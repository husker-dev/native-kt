package tests.objc.primitives

import natives.test.objc.MyDictionary
import natives.test.objc.MyEnum
import natives.test.objc.VoidCallback
import natives.test.objc.MyInterface
import withObjcLib
import kotlin.test.Test
import kotlin.test.assertTrue

class Objc_CallbackArgPrimitive {

    @Test
    fun callbackVoid() = withObjcLib {
        var pass = false
        natives.test.objc.callbackVoid { pass = true }
        assertTrue(pass)
    }

    @Test
    fun callbackVoidN() = withObjcLib {
        assertTrue(natives.test.objc.callbackVoidN(null))
    }

    @Test
    fun callbackArgChar() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgChar { it == 'a' })
    }

    @Test
    fun callbackArgBoolean() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgBoolean { it })
    }

    @Test
    fun callbackArgByte() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgByte { it == 1.toByte() })
    }

    @Test
    fun callbackArgUByte() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgUbyte { it == UByte.MAX_VALUE })
    }

    @Test
    fun callbackArgShort() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgShort { it == 1.toShort() })
    }

    @Test
    fun callbackArgUShort() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgUshort { it == UShort.MAX_VALUE })
    }

    @Test
    fun callbackArgInt() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgInt { it == 1 })
    }

    @Test
    fun callbackArgUInt() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgUint { it == UInt.MAX_VALUE })
    }

    @Test
    fun callbackArgLong() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgLong { it == 1.toLong() })
    }

    @Test
    fun callbackArgULong() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgUlong { it == ULong.MAX_VALUE })
    }

    @Test
    fun callbackArgFloat() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgFloat { it == 1.1f })
    }

    @Test
    fun callbackArgDouble() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgDouble { it == 1.1 })
    }

    @Test
    fun callbackArgString() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgString { it == "test string" })
    }

    @Test
    fun callbackArgStringEmpty() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgStringEmpty { it == "" })
    }

    @Test
    fun callbackArgStringN() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgStringN { it == null })
    }

    @Test
    fun callbackArgCallback() = withObjcLib {
        val toPass = VoidCallback {}
        assertTrue(natives.test.objc.callbackArgCallback(toPass) { it == toPass })
    }

    @Test
    fun callbackArgCallbackN() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgCallbackN { it == null })
    }

    @Test
    fun callbackArgEnum() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgEnum { it == MyEnum.CASE2 })
    }

    @Test
    fun callbackArgDictionary() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgDictionary { it == MyDictionary(1, 2, 3, 4) })
    }

    @Test
    fun callbackArgDictionaryN() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgDictionaryN { it == null })
    }

    @Test
    fun callbackArgInterface() = withObjcLib {
        val obj = MyInterface()
        assertTrue(natives.test.objc.callbackArgInterface(obj) { arg ->
            arg.use { arg == obj }
        })
        obj.close()
    }

    @Test
    fun callbackArgInterfaceN() = withObjcLib {
        assertTrue(natives.test.objc.callbackArgInterfaceN { it == null })
    }
}
