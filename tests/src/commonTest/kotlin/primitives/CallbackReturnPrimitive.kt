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
    fun callbackReturnShort() = withLib {
        assertTrue(natives.test.callbackReturnShort { 1.toShort() })
    }

    @Test
    fun callbackReturnInt() = withLib {
        assertTrue(natives.test.callbackReturnInt { 1 })
    }

    @Test
    fun callbackReturnLong() = withLib {
        assertTrue(natives.test.callbackReturnLong { 1.toLong() })
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
    fun callbackReturnCallback() = withLib {
        val toPass = VoidCallback {}
        assertEquals(toPass, natives.test.callbackReturnCallback { toPass })
    }

    @Test
    fun callbackReturnEnum() = withLib {
        assertTrue(natives.test.callbackReturnEnum { MyEnum.CASE2 })
    }

    @Test
    fun callbackReturnDictionary() = withLib {
        assertTrue(natives.test.callbackReturnDictionary { MyDictionary(1, 2, 3, 4) })
    }
}