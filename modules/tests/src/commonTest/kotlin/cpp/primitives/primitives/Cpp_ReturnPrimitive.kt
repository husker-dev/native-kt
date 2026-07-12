package cpp.primitives.primitives

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
    fun returnBoolean() = withCppLib {
        assertEquals(true, natives.testcpp.returnBoolean())
    }

    @Test
    fun returnByte() = withCppLib {
        assertEquals(99.toByte(), natives.testcpp.returnByte())
    }

    @Test
    fun returnUByte() = withCppLib {
        assertEquals(UByte.MAX_VALUE, natives.testcpp.returnUByte())
    }

    @Test
    fun returnShort() = withCppLib {
        assertEquals(99.toShort(), natives.testcpp.returnShort())
    }

    @Test
    fun returnUShort() = withCppLib {
        assertEquals(UShort.MAX_VALUE, natives.testcpp.returnUShort())
    }

    @Test
    fun returnInt() = withCppLib {
        assertEquals(99, natives.testcpp.returnInt())
    }

    @Test
    fun returnUInt() = withCppLib {
        assertEquals(UInt.MAX_VALUE, natives.testcpp.returnUInt())
    }

    @Test
    fun returnLong() = withCppLib {
        assertEquals(9223372036854775805L, natives.testcpp.returnLong())
    }

    @Test
    fun returnULong() = withCppLib {
        assertEquals(ULong.MAX_VALUE, natives.testcpp.returnULong())
    }

    @Test
    fun returnFloat() = withCppLib {
        assertEquals(99f, natives.testcpp.returnFloat())
    }

    @Test
    fun returnDouble() = withCppLib {
        assertEquals(99.0, natives.testcpp.returnDouble())
    }

    @Test
    fun returnString() = withCppLib {
        assertEquals("test string", natives.testcpp.returnString())
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
    fun returnDictionary() = withCppLib {
        assertEquals(MyDictionary(1, 2, 3, 4), natives.testcpp.returnDictionary())
    }

    @Test
    fun returnDictionaryN() = withCppLib {
        assertNull(natives.testcpp.returnDictionaryN())
    }
}