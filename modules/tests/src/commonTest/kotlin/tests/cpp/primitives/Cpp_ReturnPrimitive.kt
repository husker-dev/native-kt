package tests.cpp.primitives

import natives.testcpp.MyDictionary
import natives.testcpp.MyEnum
import withCppLib
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Cpp_ReturnPrimitive {

    @Test
    fun returnVoid() = withCppLib {
        assertEquals(Unit, natives.testcpp.returnVoid())
    }

    @Test
    fun returnChar() = withCppLib {
        assertEquals('a', natives.testcpp.returnChar())
    }

    @Test
    fun returnCharN() = withCppLib {
        assertNull(natives.testcpp.returnCharN())
    }

    @Test
    fun returnBoolean() = withCppLib {
        assertEquals(true, natives.testcpp.returnBoolean())
    }

    @Test
    fun returnBooleanN() = withCppLib {
        assertEquals(true, natives.testcpp.returnBooleanN())
    }

    @Test
    fun returnByte() = withCppLib {
        assertEquals(99.toByte(), natives.testcpp.returnByte())
    }

    @Test
    fun returnByteN() = withCppLib {
        assertNull(natives.testcpp.returnByteN())
    }

    @Test
    fun returnUByte() = withCppLib {
        assertEquals(UByte.MAX_VALUE, natives.testcpp.returnUByte())
    }

    @Test
    fun returnUByteN() = withCppLib {
        assertEquals(UByte.MAX_VALUE, natives.testcpp.returnUByteN())
    }

    @Test
    fun returnShort() = withCppLib {
        assertEquals(99.toShort(), natives.testcpp.returnShort())
    }

    @Test
    fun returnShortN() = withCppLib {
        assertNull(natives.testcpp.returnShortN())
    }

    @Test
    fun returnUShort() = withCppLib {
        assertEquals(UShort.MAX_VALUE, natives.testcpp.returnUShort())
    }

    @Test
    fun returnUShortN() = withCppLib {
        assertEquals(UShort.MAX_VALUE, natives.testcpp.returnUShortN())
    }

    @Test
    fun returnInt() = withCppLib {
        assertEquals(99, natives.testcpp.returnInt())
    }

    @Test
    fun returnIntN() = withCppLib {
        assertNull(natives.testcpp.returnIntN())
    }

    @Test
    fun returnUInt() = withCppLib {
        assertEquals(UInt.MAX_VALUE, natives.testcpp.returnUInt())
    }

    @Test
    fun returnUIntN() = withCppLib {
        assertEquals(UInt.MAX_VALUE, natives.testcpp.returnUIntN())
    }

    @Test
    fun returnLong() = withCppLib {
        assertEquals(9223372036854775805L, natives.testcpp.returnLong())
    }

    @Test
    fun returnLongN() = withCppLib {
        assertNull(natives.testcpp.returnLongN())
    }

    @Test
    fun returnULong() = withCppLib {
        assertEquals(ULong.MAX_VALUE, natives.testcpp.returnULong())
    }

    @Test
    fun returnULongN() = withCppLib {
        assertEquals(ULong.MAX_VALUE, natives.testcpp.returnULongN())
    }

    @Test
    fun returnFloat() = withCppLib {
        assertEquals(99f, natives.testcpp.returnFloat())
    }

    @Test
    fun returnFloatN() = withCppLib {
        assertNull(natives.testcpp.returnFloatN())
    }

    @Test
    fun returnDouble() = withCppLib {
        assertEquals(99.0, natives.testcpp.returnDouble())
    }

    @Test
    fun returnDoubleN() = withCppLib {
        assertEquals(99.0, natives.testcpp.returnDoubleN())
    }

    @Test
    fun returnString() = withCppLib {
        assertEquals("test string", natives.testcpp.returnString())
    }

    @Test
    fun returnStringEmpty() = withCppLib {
        assertEquals("", natives.testcpp.returnStringEmpty())
    }

    @Test
    fun returnStringN() = withCppLib {
        assertNull(natives.testcpp.returnStringN())
    }

    @Test
    fun returnEnum() = withCppLib {
        assertEquals(MyEnum.CASE2, natives.testcpp.returnEnum())
    }

    @Test
    fun returnEnumN() = withCppLib {
        assertEquals(MyEnum.CASE2, natives.testcpp.returnEnumN())
    }

    @Test
    fun returnDictionary() = withCppLib {
        assertEquals(MyDictionary(1, 2, 3, 4), natives.testcpp.returnDictionary())
    }

    @Test
    fun returnDictionaryN() = withCppLib {
        assertNull(natives.testcpp.returnDictionaryN())
    }
}
