package tests.objc.primitives

import natives.test.objc.MyDictionary
import natives.test.objc.MyEnum
import withObjcLib
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Objc_ReturnPrimitive {

    @Test
    fun returnVoid() = withObjcLib {
        assertEquals(Unit, natives.test.objc.returnVoid())
    }

    @Test
    fun returnChar() = withObjcLib {
        assertEquals('a', natives.test.objc.returnChar())
    }

    @Test
    fun returnCharN() = withObjcLib {
        assertNull(natives.test.objc.returnCharN())
    }

    @Test
    fun returnBoolean() = withObjcLib {
        assertEquals(true, natives.test.objc.returnBoolean())
    }

    @Test
    fun returnBooleanN() = withObjcLib {
        assertEquals(true, natives.test.objc.returnBooleanN())
    }

    @Test
    fun returnByte() = withObjcLib {
        assertEquals(99.toByte(), natives.test.objc.returnByte())
    }

    @Test
    fun returnByteN() = withObjcLib {
        assertNull(natives.test.objc.returnByteN())
    }

    @Test
    fun returnUByte() = withObjcLib {
        assertEquals(UByte.MAX_VALUE, natives.test.objc.returnUbyte())
    }

    @Test
    fun returnUByteN() = withObjcLib {
        assertEquals(UByte.MAX_VALUE, natives.test.objc.returnUbyteN())
    }

    @Test
    fun returnShort() = withObjcLib {
        assertEquals(99.toShort(), natives.test.objc.returnShort())
    }

    @Test
    fun returnShortN() = withObjcLib {
        assertNull(natives.test.objc.returnShortN())
    }

    @Test
    fun returnUShort() = withObjcLib {
        assertEquals(UShort.MAX_VALUE, natives.test.objc.returnUshort())
    }

    @Test
    fun returnUShortN() = withObjcLib {
        assertEquals(UShort.MAX_VALUE, natives.test.objc.returnUshortN())
    }

    @Test
    fun returnInt() = withObjcLib {
        assertEquals(99, natives.test.objc.returnInt())
    }

    @Test
    fun returnIntN() = withObjcLib {
        assertNull(natives.test.objc.returnIntN())
    }

    @Test
    fun returnUInt() = withObjcLib {
        assertEquals(UInt.MAX_VALUE, natives.test.objc.returnUint())
    }

    @Test
    fun returnUIntN() = withObjcLib {
        assertEquals(UInt.MAX_VALUE, natives.test.objc.returnUintN())
    }

    @Test
    fun returnLong() = withObjcLib {
        assertEquals(9223372036854775805L, natives.test.objc.returnLong())
    }

    @Test
    fun returnLongN() = withObjcLib {
        assertNull(natives.test.objc.returnLongN())
    }

    @Test
    fun returnULong() = withObjcLib {
        assertEquals(ULong.MAX_VALUE, natives.test.objc.returnUlong())
    }

    @Test
    fun returnULongN() = withObjcLib {
        assertEquals(ULong.MAX_VALUE, natives.test.objc.returnUlongN())
    }

    @Test
    fun returnFloat() = withObjcLib {
        assertEquals(99f, natives.test.objc.returnFloat())
    }

    @Test
    fun returnFloatN() = withObjcLib {
        assertNull(natives.test.objc.returnFloatN())
    }

    @Test
    fun returnDouble() = withObjcLib {
        assertEquals(99.0, natives.test.objc.returnDouble())
    }

    @Test
    fun returnDoubleN() = withObjcLib {
        assertEquals(99.0, natives.test.objc.returnDoubleN())
    }

    @Test
    fun returnString() = withObjcLib {
        assertEquals("test string", natives.test.objc.returnString())
    }

    @Test
    fun returnStringEmpty() = withObjcLib {
        assertEquals("", natives.test.objc.returnStringEmpty())
    }

    @Test
    fun returnStringN() = withObjcLib {
        assertNull(natives.test.objc.returnStringN())
    }

    @Test
    fun returnEnum() = withObjcLib {
        assertEquals(MyEnum.CASE2, natives.test.objc.returnEnum())
    }

    @Test
    fun returnEnumN() = withObjcLib {
        assertEquals(MyEnum.CASE2, natives.test.objc.returnEnumN())
    }

    @Test
    fun returnDictionary() = withObjcLib {
        assertEquals(MyDictionary(1, 2, 3, 4), natives.test.objc.returnDictionary())
    }

    @Test
    fun returnDictionaryN() = withObjcLib {
        assertNull(natives.test.objc.returnDictionaryN())
    }
}
