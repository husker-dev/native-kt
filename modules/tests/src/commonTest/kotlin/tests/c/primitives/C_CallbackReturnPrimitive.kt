package tests.c.primitives

import withCLib
import natives.test.c.MyDictionary
import natives.test.c.MyEnum
import natives.test.c.VoidCallback
import natives.test.c.MyInterface
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class C_CallbackReturnPrimitive {

    @Test
    fun callbackReturnChar() = withCLib {
        assertTrue(natives.test.c.callbackReturnChar { 'a' })
    }

    @Test
    fun callbackReturnBoolean() = withCLib {
        assertTrue(natives.test.c.callbackReturnBoolean { true })
    }

    @Test
    fun callbackReturnByte() = withCLib {
        assertTrue(natives.test.c.callbackReturnByte { 1.toByte() })
    }

    @Test
    fun callbackReturnUByte() = withCLib {
        assertTrue(natives.test.c.callbackReturnUbyte { UByte.MAX_VALUE })
    }

    @Test
    fun callbackReturnShort() = withCLib {
        assertTrue(natives.test.c.callbackReturnShort { 1.toShort() })
    }

    @Test
    fun callbackReturnUShort() = withCLib {
        assertTrue(natives.test.c.callbackReturnUshort { UShort.MAX_VALUE })
    }

    @Test
    fun callbackReturnInt() = withCLib {
        assertTrue(natives.test.c.callbackReturnInt { 1 })
    }

    @Test
    fun callbackReturnUInt() = withCLib {
        assertTrue(natives.test.c.callbackReturnUint { UInt.MAX_VALUE })
    }

    @Test
    fun callbackReturnLong() = withCLib {
        assertTrue(natives.test.c.callbackReturnLong { 1.toLong() })
    }

    @Test
    fun callbackReturnULong() = withCLib {
        assertTrue(natives.test.c.callbackReturnUlong { ULong.MAX_VALUE })
    }

    @Test
    fun callbackReturnFloat() = withCLib {
        assertTrue(natives.test.c.callbackReturnFloat { 1.1f })
    }

    @Test
    fun callbackReturnDouble() = withCLib {
        assertTrue(natives.test.c.callbackReturnDouble { 1.1 })
    }

    @Test
    fun callbackReturnString() = withCLib {
        assertTrue(natives.test.c.callbackReturnString { "test string" })
    }

    @Test
    fun callbackReturnStringEmpty() = withCLib {
        assertTrue(natives.test.c.callbackReturnStringEmpty { "" })
    }

    @Test
    fun callbackReturnStringN() = withCLib {
        assertTrue(natives.test.c.callbackReturnStringN { null })
    }

    @Test
    fun callbackReturnCallback() = withCLib {
        val toPass = VoidCallback {}
        assertEquals(toPass, natives.test.c.callbackReturnCallback { toPass })
    }

    @Test
    fun callbackReturnCallbackN() = withCLib {
        assertTrue(natives.test.c.callbackReturnCallbackN { null })
    }

    @Test
    fun callbackReturnEnum() = withCLib {
        assertTrue(natives.test.c.callbackReturnEnum { MyEnum.CASE2 })
    }

    @Test
    fun callbackReturnDictionary() = withCLib {
        assertTrue(natives.test.c.callbackReturnDictionary { MyDictionary(1, 2, 3, 4) })
    }

    @Test
    fun callbackReturnDictionaryN() = withCLib {
        assertTrue(natives.test.c.callbackReturnDictionaryN { null })
    }

    @Test
    fun callbackReturnInterface() = withCLib {
        val item = MyInterface()
        assertTrue(natives.test.c.callbackReturnInterface { item })
        item.close()
    }

    @Test
    fun callbackReturnInterfaceN() = withCLib {
        assertTrue(natives.test.c.callbackReturnInterfaceN { null })
    }
}