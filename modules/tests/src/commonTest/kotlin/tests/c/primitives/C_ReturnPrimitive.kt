package tests.c.primitives

import withCLib
import natives.test.c.MyDictionary
import natives.test.c.MyEnum
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class C_ReturnPrimitive {

    @Test
    fun returnVoid() = withCLib {
        assertEquals(Unit, natives.test.c.returnVoid())
    }

    @Test
    fun returnChar() = withCLib {
        assertEquals('a', natives.test.c.returnChar())
    }

    @Test
    fun returnCharN() = withCLib {
        assertNull(natives.test.c.returnCharN())
    }

    @Test
    fun returnBoolean() = withCLib {
        assertEquals(true, natives.test.c.returnBoolean())
    }

    @Test
    fun returnBooleanN() = withCLib {
        assertEquals(true, natives.test.c.returnBooleanN())
    }

    @Test
    fun returnByte() = withCLib {
        assertEquals(99.toByte(), natives.test.c.returnByte())
    }

    @Test
    fun returnByteN() = withCLib {
        assertNull(natives.test.c.returnByteN())
    }

    @Test
    fun returnUByte() = withCLib {
        assertEquals(UByte.MAX_VALUE, natives.test.c.returnUbyte())
    }

    @Test
    fun returnUByteN() = withCLib {
        assertEquals(UByte.MAX_VALUE, natives.test.c.returnUbyteN())
    }

    @Test
    fun returnShort() = withCLib {
        assertEquals(99.toShort(), natives.test.c.returnShort())
    }

    @Test
    fun returnShortN() = withCLib {
        assertNull(natives.test.c.returnShortN())
    }

    @Test
    fun returnUShort() = withCLib {
        assertEquals(UShort.MAX_VALUE, natives.test.c.returnUshort())
    }

    @Test
    fun returnUShortN() = withCLib {
        assertEquals(UShort.MAX_VALUE, natives.test.c.returnUshortN())
    }

    @Test
    fun returnInt() = withCLib {
        assertEquals(99, natives.test.c.returnInt())
    }

    @Test
    fun returnIntN() = withCLib {
        assertNull(natives.test.c.returnIntN())
    }

    @Test
    fun returnUInt() = withCLib {
        assertEquals(UInt.MAX_VALUE, natives.test.c.returnUint())
    }

    @Test
    fun returnUIntN() = withCLib {
        assertEquals(UInt.MAX_VALUE, natives.test.c.returnUintN())
    }

    @Test
    fun returnLong() = withCLib {
        assertEquals(9223372036854775805L, natives.test.c.returnLong())
    }

    @Test
    fun returnLongN() = withCLib {
        assertNull(natives.test.c.returnLongN())
    }

    @Test
    fun returnULong() = withCLib {
        assertEquals(ULong.MAX_VALUE, natives.test.c.returnUlong())
    }

    @Test
    fun returnULongN() = withCLib {
        assertEquals(ULong.MAX_VALUE, natives.test.c.returnUlongN())
    }

    @Test
    fun returnFloat() = withCLib {
        assertEquals(99f, natives.test.c.returnFloat())
    }

    @Test
    fun returnFloatN() = withCLib {
        assertNull(natives.test.c.returnFloatN())
    }

    @Test
    fun returnDouble() = withCLib {
        assertEquals(99.0, natives.test.c.returnDouble())
    }

    @Test
    fun returnDoubleN() = withCLib {
        assertEquals(99.0, natives.test.c.returnDoubleN())
    }

    @Test
    fun returnString() = withCLib {
        assertEquals("test string", natives.test.c.returnString())
    }

    @Test
    fun returnStringEmpty() = withCLib {
        assertEquals("", natives.test.c.returnStringEmpty())
    }

    @Test
    fun returnStringN() = withCLib {
        assertNull(natives.test.c.returnStringN())
    }

    @Test
    fun returnEnum() = withCLib {
        assertEquals(MyEnum.CASE2, natives.test.c.returnEnum())
    }

    @Test
    fun returnEnumN() = withCLib {
        assertEquals(MyEnum.CASE2, natives.test.c.returnEnumN())
    }

    @Test
    fun returnDictionary() = withCLib {
        assertEquals(MyDictionary(1, 2, 3, 4), natives.test.c.returnDictionary())
    }

    @Test
    fun returnDictionaryN() = withCLib {
        assertNull(natives.test.c.returnDictionaryN())
    }
}
