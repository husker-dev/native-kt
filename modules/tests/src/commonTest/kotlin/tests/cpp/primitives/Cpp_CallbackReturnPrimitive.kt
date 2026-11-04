package tests.cpp.primitives

import natives.test.cpp.MyDictionary
import natives.test.cpp.MyEnum
import natives.test.cpp.VoidCallback
import natives.test.cpp.MyInterface
import withCppLib
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Cpp_CallbackReturnPrimitive {

    @Test
    fun callbackReturnChar() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnChar { 'a' })
    }

    @Test
    fun callbackReturnBoolean() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnBoolean { true })
    }

    @Test
    fun callbackReturnByte() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnByte { 1.toByte() })
    }

    @Test
    fun callbackReturnUByte() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnUbyte { UByte.MAX_VALUE })
    }

    @Test
    fun callbackReturnShort() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnShort { 1.toShort() })
    }

    @Test
    fun callbackReturnUShort() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnUshort { UShort.MAX_VALUE })
    }

    @Test
    fun callbackReturnInt() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnInt { 1 })
    }

    @Test
    fun callbackReturnUInt() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnUint { UInt.MAX_VALUE })
    }

    @Test
    fun callbackReturnLong() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnLong { 1.toLong() })
    }

    @Test
    fun callbackReturnULong() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnUlong { ULong.MAX_VALUE })
    }

    @Test
    fun callbackReturnFloat() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnFloat { 1.1f })
    }

    @Test
    fun callbackReturnDouble() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnDouble { 1.1 })
    }

    @Test
    fun callbackReturnString() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnString { "test string" })
    }

    @Test
    fun callbackReturnStringEmpty() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnStringEmpty { "" })
    }

    @Test
    fun callbackReturnStringN() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnStringN { null })
    }

    @Test
    fun callbackReturnCallback() = withCppLib {
        val toPass = VoidCallback {}
        assertEquals(toPass, natives.test.cpp.callbackReturnCallback { toPass })
    }

    @Test
    fun callbackReturnCallbackN() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnCallbackN { null })
    }

    @Test
    fun callbackReturnEnum() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnEnum { MyEnum.CASE2 })
    }

    @Test
    fun callbackReturnDictionary() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnDictionary { MyDictionary(1, 2, 3, 4) })
    }

    @Test
    fun callbackReturnDictionaryN() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnDictionaryN { null })
    }

    @Test
    fun callbackReturnInterface() = withCppLib {
        val item = MyInterface()
        assertTrue(natives.test.cpp.callbackReturnInterface { item })
        item.close()
    }

    @Test
    fun callbackReturnInterfaceN() = withCppLib {
        assertTrue(natives.test.cpp.callbackReturnInterfaceN { null })
    }
}