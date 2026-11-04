package tests.swift.primitives

import withSwiftLib
import natives.test.swift.MyDictionary
import natives.test.swift.MyEnum
import natives.test.swift.MyInterface
import natives.test.swift.VoidCallback
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Swift_CallbackReturnPrimitive {

    @Test
    fun callbackReturnChar() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnChar { 'a' })
    }

    @Test
    fun callbackReturnBoolean() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnBoolean { true })
    }

    @Test
    fun callbackReturnByte() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnByte { 1.toByte() })
    }

    @Test
    fun callbackReturnUByte() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnUbyte { UByte.MAX_VALUE })
    }

    @Test
    fun callbackReturnShort() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnShort { 1.toShort() })
    }

    @Test
    fun callbackReturnUShort() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnUshort { UShort.MAX_VALUE })
    }

    @Test
    fun callbackReturnInt() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnInt { 1 })
    }

    @Test
    fun callbackReturnUInt() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnUint { UInt.MAX_VALUE })
    }

    @Test
    fun callbackReturnLong() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnLong { 1.toLong() })
    }

    @Test
    fun callbackReturnULong() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnUlong { ULong.MAX_VALUE })
    }

    @Test
    fun callbackReturnFloat() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnFloat { 1.1f })
    }

    @Test
    fun callbackReturnDouble() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnDouble { 1.1 })
    }

    @Test
    fun callbackReturnString() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnString { "test string" })
    }

    @Test
    fun callbackReturnStringEmpty() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnStringEmpty { "" })
    }

    @Test
    fun callbackReturnStringN() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnStringN { null })
    }

    @Test
    fun callbackReturnCallback() = withSwiftLib {
        val toPass = VoidCallback {}
        assertEquals(toPass, natives.test.swift.callbackReturnCallback { toPass })
    }

    @Test
    fun callbackReturnCallbackN() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnCallbackN { null })
    }

    @Test
    fun callbackReturnEnum() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnEnum { MyEnum.CASE2 })
    }

    @Test
    fun callbackReturnDictionary() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnDictionary { MyDictionary(1, 2, 3, 4) })
    }

    @Test
    fun callbackReturnDictionaryN() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnDictionaryN { null })
    }

    @Test
    fun callbackReturnInterface() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnInterface { MyInterface() })
    }

    @Test
    fun callbackReturnInterfaceN() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnInterfaceN { null })
    }
}