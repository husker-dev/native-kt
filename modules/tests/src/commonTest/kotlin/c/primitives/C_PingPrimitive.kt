package c.primitives

import withCLib
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class C_PingPrimitive {

    @Test
    fun pingChar() = withCLib {
        assertEquals('a', pingChar('a'))
    }

    @Test
    fun pingBoolean() = withCLib {
        assertEquals(true, pingBoolean(true))
    }

    @Test
    fun pingByte() = withCLib {
        assertEquals(99.toByte(), pingByte(99.toByte()))
    }

    @Test
    fun pingUByte() = withCLib {
        assertEquals(UByte.MAX_VALUE, natives.test.pingUByte(UByte.MAX_VALUE))
    }

    @Test
    fun pingShort() = withCLib {
        assertEquals(99.toShort(), pingShort(99.toShort()))
    }

    @Test
    fun pingUShort() = withCLib {
        assertEquals(UShort.MAX_VALUE, natives.test.pingUShort(UShort.MAX_VALUE))
    }

    @Test
    fun pingInt() = withCLib {
        assertEquals(99, pingInt(99))
    }

    @Test
    fun pingUInt() = withCLib {
        assertEquals(UInt.MAX_VALUE, natives.test.pingUInt(UInt.MAX_VALUE))
    }

    @Test
    fun pingLong() = withCLib {
        assertEquals(9223372036854775805L, pingLong(9223372036854775805L))
    }

    @Test
    fun pingULong() = withCLib {
        assertEquals(ULong.MAX_VALUE, natives.test.pingULong(ULong.MAX_VALUE))
    }

    @Test
    fun pingFloat() = withCLib {
        assertEquals(99f, pingFloat(99f))
    }

    @Test
    fun pingDouble() = withCLib {
        assertEquals(99.0, pingDouble(99.0))
    }

    @Test
    fun pingString() = withCLib {
        assertEquals("test string", pingString("test string"))
    }

    @Test
    fun pingStringN() = withCLib {
        assertNull(natives.test.pingStringN(null))
    }

    @Test
    fun pingEnum() = withCLib {
        assertEquals(MyEnum.CASE2, natives.test.pingEnum(MyEnum.CASE2))
    }

    @Test
    fun pingDictionary() = withCLib {
        assertEquals(
            MyDictionary(1, 2, 3, 4),
            natives.test.pingDictionary(MyDictionary(1, 2, 3, 4))
        )
    }

    @Test
    fun pingDictionaryN() = withCLib {
        assertNull(natives.test.pingDictionaryN(null))
    }

}