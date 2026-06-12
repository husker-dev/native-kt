package primitives

import natives.test.MyDictionary
import natives.test.MyEnum
import natives.test.pingBoolean
import natives.test.pingByte
import natives.test.pingChar
import natives.test.pingDouble
import natives.test.pingFloat
import natives.test.pingInt
import natives.test.pingLong
import natives.test.pingString
import natives.test.pingShort
import withLib
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PingPrimitive {

    @Test
    fun pingChar() = withLib {
        assertEquals('a', pingChar('a'))
    }

    @Test
    fun pingBoolean() = withLib {
        assertEquals(true, pingBoolean(true))
    }

    @Test
    fun pingByte() = withLib {
        assertEquals(99.toByte(), pingByte(99.toByte()))
    }

    @Test
    fun pingShort() = withLib {
        assertEquals(99.toShort(), pingShort(99.toShort()))
    }

    @Test
    fun pingInt() = withLib {
        assertEquals(99, pingInt(99))
    }

    @Test
    fun pingLong() = withLib {
        assertEquals(9223372036854775805L, pingLong(9223372036854775805L))
    }

    @Test
    fun pingFloat() = withLib {
        assertEquals(99f, pingFloat(99f))
    }

    @Test
    fun pingDouble() = withLib {
        assertEquals(99.0, pingDouble(99.0))
    }

    @Test
    fun pingString() = withLib {
        assertEquals("test string", pingString("test string"))
    }

    @Test
    fun pingStringN() = withLib {
        assertNull(natives.test.pingStringN(null))
    }

    @Test
    fun pingEnum() = withLib {
        assertEquals(MyEnum.CASE2, natives.test.pingEnum(MyEnum.CASE2))
    }

    @Test
    fun pingDictionary() = withLib {
        assertEquals(
            MyDictionary(1, 2, 3, 4),
            natives.test.pingDictionary(MyDictionary(1, 2, 3, 4))
        )
    }

    @Test
    fun pingDictionaryN() = withLib {
        assertNull(natives.test.pingDictionaryN(null))
    }

}