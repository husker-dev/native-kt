package rust.primitives

import withRustLib
import natives.testrs.MyDictionary
import natives.testrs.MyEnum
import natives.testrs.pingBoolean
import natives.testrs.pingByte
import natives.testrs.pingChar
import natives.testrs.pingDouble
import natives.testrs.pingFloat
import natives.testrs.pingInt
import natives.testrs.pingLong
import natives.testrs.pingString
import natives.testrs.pingShort
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Rust_PingPrimitive {

    @Test
    fun pingChar() = withRustLib {
        assertEquals('a', pingChar('a'))
    }

    @Test
    fun pingBoolean() = withRustLib {
        assertEquals(true, pingBoolean(true))
    }

    @Test
    fun pingByte() = withRustLib {
        assertEquals(99.toByte(), pingByte(99.toByte()))
    }

    @Test
    fun pingUByte() = withRustLib {
        assertEquals(UByte.MAX_VALUE, natives.testrs.pingUByte(UByte.MAX_VALUE))
    }

    @Test
    fun pingShort() = withRustLib {
        assertEquals(99.toShort(), pingShort(99.toShort()))
    }

    @Test
    fun pingUShort() = withRustLib {
        assertEquals(UShort.MAX_VALUE, natives.testrs.pingUShort(UShort.MAX_VALUE))
    }

    @Test
    fun pingInt() = withRustLib {
        assertEquals(99, pingInt(99))
    }

    @Test
    fun pingUInt() = withRustLib {
        assertEquals(UInt.MAX_VALUE, natives.testrs.pingUInt(UInt.MAX_VALUE))
    }

    @Test
    fun pingLong() = withRustLib {
        assertEquals(9223372036854775805L, pingLong(9223372036854775805L))
    }

    @Test
    fun pingULong() = withRustLib {
        assertEquals(ULong.MAX_VALUE, natives.testrs.pingULong(ULong.MAX_VALUE))
    }

    @Test
    fun pingFloat() = withRustLib {
        assertEquals(99f, pingFloat(99f))
    }

    @Test
    fun pingDouble() = withRustLib {
        assertEquals(99.0, pingDouble(99.0))
    }

    @Test
    fun pingString() = withRustLib {
        assertEquals("test string", pingString("test string"))
    }

    @Test
    fun pingStringN() = withRustLib {
        assertNull(natives.testrs.pingStringN(null))
    }

    @Test
    fun pingEnum() = withRustLib {
        assertEquals(MyEnum.CASE2, natives.testrs.pingEnum(MyEnum.CASE2))
    }

    @Test
    fun pingDictionary() = withRustLib {
        assertEquals(
            MyDictionary(1, 2, 3, 4),
            natives.testrs.pingDictionary(MyDictionary(1, 2, 3, 4))
        )
    }

    @Test
    fun pingDictionaryN() = withRustLib {
        assertNull(natives.testrs.pingDictionaryN(null))
    }

}