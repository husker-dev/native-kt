package c.primitives

import withCLib
import natives.test.MyDictionary
import natives.test.MyEnum
import natives.test.VoidCallback
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class C_CallbackReturnPrimitive {

    @Test
    fun callbackReturnChar() = withCLib {
        assertTrue(natives.test.callbackReturnChar { 'a' })
    }

    @Test
    fun callbackReturnBoolean() = withCLib {
        assertTrue(natives.test.callbackReturnBoolean { true })
    }

    @Test
    fun callbackReturnByte() = withCLib {
        assertTrue(natives.test.callbackReturnByte { 1.toByte() })
    }

    @Test
    fun callbackReturnUByte() = withCLib {
        assertTrue(natives.test.callbackReturnUByte { UByte.MAX_VALUE })
    }

    @Test
    fun callbackReturnShort() = withCLib {
        assertTrue(natives.test.callbackReturnShort { 1.toShort() })
    }

    @Test
    fun callbackReturnUShort() = withCLib {
        assertTrue(natives.test.callbackReturnUShort { UShort.MAX_VALUE })
    }

    @Test
    fun callbackReturnInt() = withCLib {
        assertTrue(natives.test.callbackReturnInt { 1 })
    }

    @Test
    fun callbackReturnUInt() = withCLib {
        assertTrue(natives.test.callbackReturnUInt { UInt.MAX_VALUE })
    }

    @Test
    fun callbackReturnLong() = withCLib {
        assertTrue(natives.test.callbackReturnLong { 1.toLong() })
    }

    @Test
    fun callbackReturnULong() = withCLib {
        assertTrue(natives.test.callbackReturnULong { ULong.MAX_VALUE })
    }

    @Test
    fun callbackReturnFloat() = withCLib {
        assertTrue(natives.test.callbackReturnFloat { 1.1f })
    }

    @Test
    fun callbackReturnDouble() = withCLib {
        assertTrue(natives.test.callbackReturnDouble { 1.1 })
    }

    @Test
    fun callbackReturnString() = withCLib {
        assertTrue(natives.test.callbackReturnString { "test string" })
    }

    @Test
    fun callbackReturnStringN() = withCLib {
        assertTrue(natives.test.callbackReturnStringN { null })
    }

    @Test
    fun callbackReturnCallback() = withCLib {
        val toPass = VoidCallback {}
        assertEquals(toPass, natives.test.callbackReturnCallback { toPass })
    }

    @Test
    fun callbackReturnCallbackN() = withCLib {
        assertTrue(natives.test.callbackReturnCallbackN { null })
    }

    @Test
    fun callbackReturnEnum() = withCLib {
        assertTrue(natives.test.callbackReturnEnum { MyEnum.CASE2 })
    }

    @Test
    fun callbackReturnDictionary() = withCLib {
        assertTrue(natives.test.callbackReturnDictionary { MyDictionary(1, 2, 3, 4) })
    }

    @Test
    fun callbackReturnDictionaryN() = withCLib {
        assertTrue(natives.test.callbackReturnDictionaryN { null })
    }
}