package tests.rust.primitives

import withRustLib
import natives.test.rust.MyDictionary
import natives.test.rust.MyEnum
import natives.test.rust.MyInterface
import natives.test.rust.VoidCallback
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Rust_CallbackReturnPrimitive {

    @Test
    fun callbackReturnChar() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnChar { 'a' })
    }

    @Test
    fun callbackReturnBoolean() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnBoolean { true })
    }

    @Test
    fun callbackReturnByte() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnByte { 1.toByte() })
    }

    @Test
    fun callbackReturnUByte() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnUbyte { UByte.MAX_VALUE })
    }

    @Test
    fun callbackReturnShort() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnShort { 1.toShort() })
    }

    @Test
    fun callbackReturnUShort() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnUshort { UShort.MAX_VALUE })
    }

    @Test
    fun callbackReturnInt() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnInt { 1 })
    }

    @Test
    fun callbackReturnUInt() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnUint { UInt.MAX_VALUE })
    }

    @Test
    fun callbackReturnLong() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnLong { 1.toLong() })
    }

    @Test
    fun callbackReturnULong() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnUlong { ULong.MAX_VALUE })
    }

    @Test
    fun callbackReturnFloat() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnFloat { 1.1f })
    }

    @Test
    fun callbackReturnDouble() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnDouble { 1.1 })
    }

    @Test
    fun callbackReturnString() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnString { "test string" })
    }

    @Test
    fun callbackReturnStringEmpty() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnStringEmpty { "" })
    }

    @Test
    fun callbackReturnStringN() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnStringN { null })
    }

    @Test
    fun callbackReturnCallback() = withRustLib {
        val toPass = VoidCallback {}
        assertEquals(toPass, natives.test.rust.callbackReturnCallback { toPass })
    }

    @Test
    fun callbackReturnCallbackN() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnCallbackN { null })
    }

    @Test
    fun callbackReturnEnum() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnEnum { MyEnum.CASE2 })
    }

    @Test
    fun callbackReturnDictionary() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnDictionary { MyDictionary(1, 2, 3, 4) })
    }

    @Test
    fun callbackReturnDictionaryN() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnDictionaryN { null })
    }

    @Test
    fun callbackReturnInterface() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnInterface { MyInterface() })
    }

    @Test
    fun callbackReturnInterfaceN() = withRustLib {
        assertTrue(natives.test.rust.callbackReturnInterfaceN { null })
    }
}