package rust.primitives

import withRustLib
import natives.testrs.MyDictionary
import natives.testrs.MyEnum
import natives.testrs.VoidCallback
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Rust_CallbackReturnPrimitive {

    @Test
    fun callbackReturnChar() = withRustLib {
        assertTrue(natives.testrs.callbackReturnChar { 'a' })
    }

    @Test
    fun callbackReturnBoolean() = withRustLib {
        assertTrue(natives.testrs.callbackReturnBoolean { true })
    }

    @Test
    fun callbackReturnByte() = withRustLib {
        assertTrue(natives.testrs.callbackReturnByte { 1.toByte() })
    }

    @Test
    fun callbackReturnUByte() = withRustLib {
        assertTrue(natives.testrs.callbackReturnUByte { UByte.MAX_VALUE })
    }

    @Test
    fun callbackReturnShort() = withRustLib {
        assertTrue(natives.testrs.callbackReturnShort { 1.toShort() })
    }

    @Test
    fun callbackReturnUShort() = withRustLib {
        assertTrue(natives.testrs.callbackReturnUShort { UShort.MAX_VALUE })
    }

    @Test
    fun callbackReturnInt() = withRustLib {
        assertTrue(natives.testrs.callbackReturnInt { 1 })
    }

    @Test
    fun callbackReturnUInt() = withRustLib {
        assertTrue(natives.testrs.callbackReturnUInt { UInt.MAX_VALUE })
    }

    @Test
    fun callbackReturnLong() = withRustLib {
        assertTrue(natives.testrs.callbackReturnLong { 1.toLong() })
    }

    @Test
    fun callbackReturnULong() = withRustLib {
        assertTrue(natives.testrs.callbackReturnULong { ULong.MAX_VALUE })
    }

    @Test
    fun callbackReturnFloat() = withRustLib {
        assertTrue(natives.testrs.callbackReturnFloat { 1.1f })
    }

    @Test
    fun callbackReturnDouble() = withRustLib {
        assertTrue(natives.testrs.callbackReturnDouble { 1.1 })
    }

    @Test
    fun callbackReturnString() = withRustLib {
        assertTrue(natives.testrs.callbackReturnString { "test string" })
    }

    @Test
    fun callbackReturnStringN() = withRustLib {
        assertTrue(natives.testrs.callbackReturnStringN { null })
    }

    @Test
    fun callbackReturnCallback() = withRustLib {
        val toPass = VoidCallback {}
        assertEquals(toPass, natives.testrs.callbackReturnCallback { toPass })
    }

    @Test
    fun callbackReturnCallbackN() = withRustLib {
        assertTrue(natives.testrs.callbackReturnCallbackN { null })
    }

    @Test
    fun callbackReturnEnum() = withRustLib {
        assertTrue(natives.testrs.callbackReturnEnum { MyEnum.CASE2 })
    }

    @Test
    fun callbackReturnDictionary() = withRustLib {
        assertTrue(natives.testrs.callbackReturnDictionary { MyDictionary(1, 2, 3, 4) })
    }

    @Test
    fun callbackReturnDictionaryN() = withRustLib {
        assertTrue(natives.testrs.callbackReturnDictionaryN { null })
    }
}