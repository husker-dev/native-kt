package tests.swift.primitives

import withSwiftLib
import natives.test.swift.MyDictionary
import natives.test.swift.MyEnum
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Swift_ReturnPrimitive {

    @Test
    fun returnVoid() = withSwiftLib {
        assertEquals(Unit, natives.test.swift.returnVoid())
    }

    @Test
    fun returnChar() = withSwiftLib {
        assertEquals('a', natives.test.swift.returnChar())
    }

    @Test
    fun returnCharN() = withSwiftLib {
        assertNull(natives.test.swift.returnCharN())
    }

    @Test
    fun returnBoolean() = withSwiftLib {
        assertEquals(true, natives.test.swift.returnBoolean())
    }

    @Test
    fun returnBooleanN() = withSwiftLib {
        assertEquals(true, natives.test.swift.returnBooleanN())
    }

    @Test
    fun returnByte() = withSwiftLib {
        assertEquals(99.toByte(), natives.test.swift.returnByte())
    }

    @Test
    fun returnByteN() = withSwiftLib {
        assertNull(natives.test.swift.returnByteN())
    }

    @Test
    fun returnUByte() = withSwiftLib {
        assertEquals(UByte.MAX_VALUE, natives.test.swift.returnUbyte())
    }

    @Test
    fun returnUByteN() = withSwiftLib {
        assertEquals(UByte.MAX_VALUE, natives.test.swift.returnUbyteN())
    }

    @Test
    fun returnShort() = withSwiftLib {
        assertEquals(99.toShort(), natives.test.swift.returnShort())
    }

    @Test
    fun returnShortN() = withSwiftLib {
        assertNull(natives.test.swift.returnShortN())
    }

    @Test
    fun returnUShort() = withSwiftLib {
        assertEquals(UShort.MAX_VALUE, natives.test.swift.returnUshort())
    }

    @Test
    fun returnUShortN() = withSwiftLib {
        assertEquals(UShort.MAX_VALUE, natives.test.swift.returnUshortN())
    }

    @Test
    fun returnInt() = withSwiftLib {
        assertEquals(99, natives.test.swift.returnInt())
    }

    @Test
    fun returnIntN() = withSwiftLib {
        assertNull(natives.test.swift.returnIntN())
    }

    @Test
    fun returnUInt() = withSwiftLib {
        assertEquals(UInt.MAX_VALUE, natives.test.swift.returnUint())
    }

    @Test
    fun returnUIntN() = withSwiftLib {
        assertEquals(UInt.MAX_VALUE, natives.test.swift.returnUintN())
    }

    @Test
    fun returnLong() = withSwiftLib {
        assertEquals(9223372036854775805L, natives.test.swift.returnLong())
    }

    @Test
    fun returnLongN() = withSwiftLib {
        assertNull(natives.test.swift.returnLongN())
    }

    @Test
    fun returnULong() = withSwiftLib {
        assertEquals(ULong.MAX_VALUE, natives.test.swift.returnUlong())
    }

    @Test
    fun returnULongN() = withSwiftLib {
        assertEquals(ULong.MAX_VALUE, natives.test.swift.returnUlongN())
    }

    @Test
    fun returnFloat() = withSwiftLib {
        assertEquals(99f, natives.test.swift.returnFloat())
    }

    @Test
    fun returnFloatN() = withSwiftLib {
        assertNull(natives.test.swift.returnFloatN())
    }

    @Test
    fun returnDouble() = withSwiftLib {
        assertEquals(99.0, natives.test.swift.returnDouble())
    }

    @Test
    fun returnDoubleN() = withSwiftLib {
        assertEquals(99.0, natives.test.swift.returnDoubleN())
    }

    @Test
    fun returnString() = withSwiftLib {
        assertEquals("test string", natives.test.swift.returnString())
    }

    @Test
    fun returnStringEmpty() = withSwiftLib {
        assertEquals("", natives.test.swift.returnStringEmpty())
    }

    @Test
    fun returnStringN() = withSwiftLib {
        assertNull(natives.test.swift.returnStringN())
    }

    @Test
    fun returnEnum() = withSwiftLib {
        assertEquals(MyEnum.CASE2, natives.test.swift.returnEnum())
    }

    @Test
    fun returnEnumN() = withSwiftLib {
        assertEquals(MyEnum.CASE2, natives.test.swift.returnEnumN())
    }

    @Test
    fun returnDictionary() = withSwiftLib {
        assertEquals(MyDictionary(1, 2, 3, 4), natives.test.swift.returnDictionary())
    }

    @Test
    fun returnDictionaryN() = withSwiftLib {
        assertNull(natives.test.swift.returnDictionaryN())
    }
}
