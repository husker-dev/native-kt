package tests.cpp.primitives

import natives.test.cpp.MyDictionary
import natives.test.cpp.MyEnum
import withCppLib
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Cpp_ReturnPrimitive {

    @Test
    fun returnVoid() = withCppLib {
        assertEquals(Unit, natives.test.cpp.returnVoid())
    }

    @Test
    fun returnChar() = withCppLib {
        assertEquals('a', natives.test.cpp.returnChar())
    }

    @Test
    fun returnCharN() = withCppLib {
        assertNull(natives.test.cpp.returnCharN())
    }

    @Test
    fun returnBoolean() = withCppLib {
        assertEquals(true, natives.test.cpp.returnBoolean())
    }

    @Test
    fun returnBooleanN() = withCppLib {
        assertEquals(true, natives.test.cpp.returnBooleanN())
    }

    @Test
    fun returnByte() = withCppLib {
        assertEquals(99.toByte(), natives.test.cpp.returnByte())
    }

    @Test
    fun returnByteN() = withCppLib {
        assertNull(natives.test.cpp.returnByteN())
    }

    @Test
    fun returnUByte() = withCppLib {
        assertEquals(UByte.MAX_VALUE, natives.test.cpp.returnUbyte())
    }

    @Test
    fun returnUByteN() = withCppLib {
        assertEquals(UByte.MAX_VALUE, natives.test.cpp.returnUbyteN())
    }

    @Test
    fun returnShort() = withCppLib {
        assertEquals(99.toShort(), natives.test.cpp.returnShort())
    }

    @Test
    fun returnShortN() = withCppLib {
        assertNull(natives.test.cpp.returnShortN())
    }

    @Test
    fun returnUShort() = withCppLib {
        assertEquals(UShort.MAX_VALUE, natives.test.cpp.returnUshort())
    }

    @Test
    fun returnUShortN() = withCppLib {
        assertEquals(UShort.MAX_VALUE, natives.test.cpp.returnUshortN())
    }

    @Test
    fun returnInt() = withCppLib {
        assertEquals(99, natives.test.cpp.returnInt())
    }

    @Test
    fun returnIntN() = withCppLib {
        assertNull(natives.test.cpp.returnIntN())
    }

    @Test
    fun returnUInt() = withCppLib {
        assertEquals(UInt.MAX_VALUE, natives.test.cpp.returnUint())
    }

    @Test
    fun returnUIntN() = withCppLib {
        assertEquals(UInt.MAX_VALUE, natives.test.cpp.returnUintN())
    }

    @Test
    fun returnLong() = withCppLib {
        assertEquals(9223372036854775805L, natives.test.cpp.returnLong())
    }

    @Test
    fun returnLongN() = withCppLib {
        assertNull(natives.test.cpp.returnLongN())
    }

    @Test
    fun returnULong() = withCppLib {
        assertEquals(ULong.MAX_VALUE, natives.test.cpp.returnUlong())
    }

    @Test
    fun returnULongN() = withCppLib {
        assertEquals(ULong.MAX_VALUE, natives.test.cpp.returnUlongN())
    }

    @Test
    fun returnFloat() = withCppLib {
        assertEquals(99f, natives.test.cpp.returnFloat())
    }

    @Test
    fun returnFloatN() = withCppLib {
        assertNull(natives.test.cpp.returnFloatN())
    }

    @Test
    fun returnDouble() = withCppLib {
        assertEquals(99.0, natives.test.cpp.returnDouble())
    }

    @Test
    fun returnDoubleN() = withCppLib {
        assertEquals(99.0, natives.test.cpp.returnDoubleN())
    }

    @Test
    fun returnString() = withCppLib {
        assertEquals("test string", natives.test.cpp.returnString())
    }

    @Test
    fun returnStringEmpty() = withCppLib {
        assertEquals("", natives.test.cpp.returnStringEmpty())
    }

    @Test
    fun returnStringN() = withCppLib {
        assertNull(natives.test.cpp.returnStringN())
    }

    @Test
    fun returnEnum() = withCppLib {
        assertEquals(MyEnum.CASE2, natives.test.cpp.returnEnum())
    }

    @Test
    fun returnEnumN() = withCppLib {
        assertEquals(MyEnum.CASE2, natives.test.cpp.returnEnumN())
    }

    @Test
    fun returnDictionary() = withCppLib {
        assertEquals(MyDictionary(1, 2, 3, 4), natives.test.cpp.returnDictionary())
    }

    @Test
    fun returnDictionaryN() = withCppLib {
        assertNull(natives.test.cpp.returnDictionaryN())
    }
}
