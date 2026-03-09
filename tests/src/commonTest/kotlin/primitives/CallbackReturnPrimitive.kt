package primitives

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
        val toPass = {}
        assertEquals(toPass, natives.test.callbackReturnCallback { toPass })
    }
}