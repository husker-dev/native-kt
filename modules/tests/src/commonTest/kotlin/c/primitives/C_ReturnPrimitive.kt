package c.primitives

import withCLib
import natives.test.MyDictionary
import natives.test.MyEnum
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class C_ReturnPrimitive {

    @Test
    fun returnVoid() = withCLib {
        assertEquals(Unit, natives.test.returnVoid())
    }

    @Test
    fun returnChar() = withCLib {
        assertEquals('a', natives.test.returnChar())
    }

    @Test
    fun returnBoolean() = withCLib {
        assertEquals(true, natives.test.returnBoolean())
    }

    @Test
    fun returnByte() = withCLib {
        assertEquals(99.toByte(), natives.test.returnByte())
    }

    @Test
    fun returnUByte() = withCLib {
        assertEquals(UByte.MAX_VALUE, natives.test.returnUByte())
    }

    @Test
    fun returnShort() = withCLib {
        assertEquals(99.toShort(), natives.test.returnShort())
    }

    @Test
    fun returnUShort() = withCLib {
        assertEquals(UShort.MAX_VALUE, natives.test.returnUShort())
    }

    @Test
    fun returnInt() = withCLib {
        assertEquals(99, natives.test.returnInt())
    }

    @Test
    fun returnUInt() = withCLib {
        assertEquals(UInt.MAX_VALUE, natives.test.returnUInt())
    }

    @Test
    fun returnLong() = withCLib {
        assertEquals(9223372036854775805L, natives.test.returnLong())
    }

    @Test
    fun returnULong() = withCLib {
        assertEquals(ULong.MAX_VALUE, natives.test.returnULong())
    }

    @Test
    fun returnFloat() = withCLib {
        assertEquals(99f, natives.test.returnFloat())
    }

    @Test
    fun returnDouble() = withCLib {
        assertEquals(99.0, natives.test.returnDouble())
    }

    @Test
    fun returnString() = withCLib {
        assertEquals("test string", natives.test.returnString())
    }

    @Test
    fun returnStringN() = withCLib {
        assertNull(natives.test.returnStringN())
    }

    @Test
    fun returnEnum() = withCLib {
        assertEquals(MyEnum.CASE2, natives.test.returnEnum())
    }

    @Test
    fun returnDictionary() = withCLib {
        assertEquals(MyDictionary(1, 2, 3, 4), natives.test.returnDictionary())
    }

    @Test
    fun returnDictionaryN() = withCLib {
        assertNull(natives.test.returnDictionaryN())
    }
}