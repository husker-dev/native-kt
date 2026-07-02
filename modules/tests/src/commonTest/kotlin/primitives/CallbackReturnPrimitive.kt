package primitives

import natives.test.MyDictionary
import natives.test.MyEnum
import natives.test.VoidCallback
import withLib
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CallbackReturnPrimitive {

    @Test
    fun callbackReturnChar() = withLib {
        assertTrue(natives.test.callbackReturnChar { 'a' })
    }

    @Test
    fun callbackReturnBoolean() = withLib {
        assertTrue(natives.test.callbackReturnBoolean { true })
    }

    @Test
    fun callbackReturnByte() = withLib {
        assertTrue(natives.test.callbackReturnByte { 1.toByte() })
    }

    @Test
    fun callbackReturnUByte() = withLib {
        assertTrue(natives.test.callbackReturnUByte { UByte.MAX_VALUE })
    }

    @Test
    fun callbackReturnShort() = withLib {
        assertTrue(natives.test.callbackReturnShort { 1.toShort() })
    }

    @Test
    fun callbackReturnUShort() = withLib {
        assertTrue(natives.test.callbackReturnUShort { UShort.MAX_VALUE })
    }

    @Test
    fun callbackReturnInt() = withLib {
        assertTrue(natives.test.callbackReturnInt { 1 })
    }

    @Test
    fun callbackReturnUInt() = withLib {
        assertTrue(natives.test.callbackReturnUInt { UInt.MAX_VALUE })
    }

    @Test
    fun callbackReturnLong() = withLib {
        assertTrue(natives.test.callbackReturnLong { 1.toLong() })
    }

    @Test
    fun callbackReturnULong() = withLib {
        assertTrue(natives.test.callbackReturnULong { ULong.MAX_VALUE })
    }

    @Test
    fun callbackReturnFloat() = withLib {
        assertTrue(natives.test.callbackReturnFloat { 1.1f })
    }

    @Test
    fun callbackReturnDouble() = withLib {
        assertTrue(natives.test.callbackReturnDouble { 1.1 })
    }

    @Test
    fun callbackReturnString() = withLib {
        assertTrue(natives.test.callbackReturnString { "test string" })
    }

    @Test
    fun callbackReturnStringN() = withLib {
        assertTrue(natives.test.callbackReturnStringN { null })
    }

    @Test
    fun callbackReturnCallback() = withLib {
        val toPass = VoidCallback {}
        assertEquals(toPass, natives.test.callbackReturnCallback { toPass })
    }

    @Test
    fun callbackReturnCallbackN() = withLib {
        assertTrue(natives.test.callbackReturnCallbackN { null })
    }

    @Test
    fun callbackReturnEnum() = withLib {
        assertTrue(natives.test.callbackReturnEnum { MyEnum.CASE2 })
    }

    @Test
    fun callbackReturnDictionary() = withLib {
        assertTrue(natives.test.callbackReturnDictionary { MyDictionary(1, 2, 3, 4) })
    }

    @Test
    fun callbackReturnDictionaryN() = withLib {
        assertTrue(natives.test.callbackReturnDictionaryN { null })
    }
}