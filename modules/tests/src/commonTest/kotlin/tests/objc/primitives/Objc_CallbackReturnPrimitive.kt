package tests.objc.primitives

import natives.test.objc.MyDictionary
import natives.test.objc.MyEnum
import natives.test.objc.VoidCallback
import natives.test.objc.MyInterface
import withObjcLib
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Objc_CallbackReturnPrimitive {

    @Test
    fun callbackReturnChar() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnChar { 'a' })
    }

    @Test
    fun callbackReturnBoolean() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnBoolean { true })
    }

    @Test
    fun callbackReturnByte() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnByte { 1.toByte() })
    }

    @Test
    fun callbackReturnUByte() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnUbyte { UByte.MAX_VALUE })
    }

    @Test
    fun callbackReturnShort() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnShort { 1.toShort() })
    }

    @Test
    fun callbackReturnUShort() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnUshort { UShort.MAX_VALUE })
    }

    @Test
    fun callbackReturnInt() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnInt { 1 })
    }

    @Test
    fun callbackReturnUInt() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnUint { UInt.MAX_VALUE })
    }

    @Test
    fun callbackReturnLong() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnLong { 1.toLong() })
    }

    @Test
    fun callbackReturnULong() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnUlong { ULong.MAX_VALUE })
    }

    @Test
    fun callbackReturnFloat() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnFloat { 1.1f })
    }

    @Test
    fun callbackReturnDouble() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnDouble { 1.1 })
    }

    @Test
    fun callbackReturnString() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnString { "test string" })
    }

    @Test
    fun callbackReturnStringEmpty() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnStringEmpty { "" })
    }

    @Test
    fun callbackReturnStringN() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnStringN { null })
    }

    @Test
    fun callbackReturnCallback() = withObjcLib {
        var invoked = false
        val toPass = VoidCallback { invoked = true }
        val returned = natives.test.objc.callbackReturnCallback { toPass }
        assertEquals(toPass, returned)
        returned.invoke()
        assertTrue(invoked)
    }

    @Test
    fun callbackReturnCallbackN() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnCallbackN { null })
    }

    @Test
    fun callbackReturnEnum() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnEnum { MyEnum.CASE2 })
    }

    @Test
    fun callbackReturnDictionary() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnDictionary { MyDictionary(1, 2, 3, 4) })
    }

    @Test
    fun callbackReturnDictionaryN() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnDictionaryN { null })
    }

    @Test
    fun callbackReturnInterface() = withObjcLib {
        val item = MyInterface()
        assertTrue(natives.test.objc.callbackReturnInterface { item })
        item.close()
    }

    @Test
    fun callbackReturnInterfaceN() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnInterfaceN { null })
    }
}
