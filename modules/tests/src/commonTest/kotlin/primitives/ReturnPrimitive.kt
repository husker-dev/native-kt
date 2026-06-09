package primitives

import natives.test.MyDictionary
import natives.test.MyEnum
import withLib
import kotlin.test.Test
import kotlin.test.assertEquals

class ReturnPrimitive {

    @Test
    fun returnVoid() = withLib {
        assertEquals(Unit, natives.test.returnVoid())
    }

    @Test
    fun returnChar() = withLib {
        assertEquals('a', natives.test.returnChar())
    }

    @Test
    fun returnBoolean() = withLib {
        assertEquals(true, natives.test.returnBoolean())
    }

    @Test
    fun returnByte() = withLib {
        assertEquals(99.toByte(), natives.test.returnByte())
    }

    @Test
    fun returnShort() = withLib {
        assertEquals(99.toShort(), natives.test.returnShort())
    }

    @Test
    fun returnInt() = withLib {
        assertEquals(99, natives.test.returnInt())
    }

    @Test
    fun returnLong() = withLib {
        assertEquals(9223372036854775805L, natives.test.returnLong())
    }

    @Test
    fun returnFloat() = withLib {
        assertEquals(99f, natives.test.returnFloat())
    }

    @Test
    fun returnDouble() = withLib {
        assertEquals(99.0, natives.test.returnDouble())
    }

    @Test
    fun returnString() = withLib {
        assertEquals("test string", natives.test.returnString())
    }

    @Test
    fun returnEnum() = withLib {
        assertEquals(MyEnum.CASE2, natives.test.returnEnum())
    }

    @Test
    fun returnDictionary() = withLib {
        assertEquals(MyDictionary(1, 2, 3, 4), natives.test.returnDictionary())
    }
}