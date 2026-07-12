package cpp.primitives.primitives

import natives.testcpp.MyDictionary
import natives.testcpp.MyEnum
import natives.testcpp.VoidCallback
import withCppLib
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Cpp_CallbackReturnPrimitive {

    @Test
    fun callbackReturnChar() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnChar { 'a' })
    }

    @Test
    fun callbackReturnBoolean() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnBoolean { true })
    }

    @Test
    fun callbackReturnByte() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnByte { 1.toByte() })
    }

    @Test
    fun callbackReturnUByte() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnUByte { UByte.MAX_VALUE })
    }

    @Test
    fun callbackReturnShort() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnShort { 1.toShort() })
    }

    @Test
    fun callbackReturnUShort() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnUShort { UShort.MAX_VALUE })
    }

    @Test
    fun callbackReturnInt() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnInt { 1 })
    }

    @Test
    fun callbackReturnUInt() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnUInt { UInt.MAX_VALUE })
    }

    @Test
    fun callbackReturnLong() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnLong { 1.toLong() })
    }

    @Test
    fun callbackReturnULong() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnULong { ULong.MAX_VALUE })
    }

    @Test
    fun callbackReturnFloat() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnFloat { 1.1f })
    }

    @Test
    fun callbackReturnDouble() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnDouble { 1.1 })
    }

    @Test
    fun callbackReturnString() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnString { "test string" })
    }

    @Test
    fun callbackReturnStringN() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnStringN { null })
    }

    @Test
    fun callbackReturnCallback() = withCppLib {
        val toPass = VoidCallback {}
        assertEquals(toPass, natives.testcpp.callbackReturnCallback { toPass })
    }

    @Test
    fun callbackReturnCallbackN() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnCallbackN { null })
    }

    @Test
    fun callbackReturnEnum() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnEnum { MyEnum.CASE2 })
    }

    @Test
    fun callbackReturnDictionary() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnDictionary { MyDictionary(1, 2, 3, 4) })
    }

    @Test
    fun callbackReturnDictionaryN() = withCppLib {
        assertTrue(natives.testcpp.callbackReturnDictionaryN { null })
    }
}