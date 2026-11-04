package tests.rust.primitives

import natives.test.rust.EmptyDictionary
import withRustLib
import natives.test.rust.MyDictionary
import natives.test.rust.MyEnum
import natives.test.rust.MyInterface
import natives.test.rust.pingBoolean
import natives.test.rust.pingByte
import natives.test.rust.pingChar
import natives.test.rust.pingDouble
import natives.test.rust.pingFloat
import natives.test.rust.pingInt
import natives.test.rust.pingLong
import natives.test.rust.pingString
import natives.test.rust.pingShort
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
        assertEquals(UByte.MAX_VALUE, natives.test.rust.pingUbyte(UByte.MAX_VALUE))
    }

    @Test
    fun pingShort() = withRustLib {
        assertEquals(99.toShort(), pingShort(99.toShort()))
    }

    @Test
    fun pingUShort() = withRustLib {
        assertEquals(UShort.MAX_VALUE, natives.test.rust.pingUshort(UShort.MAX_VALUE))
    }

    @Test
    fun pingInt() = withRustLib {
        assertEquals(99, pingInt(99))
    }

    @Test
    fun pingUInt() = withRustLib {
        assertEquals(UInt.MAX_VALUE, natives.test.rust.pingUint(UInt.MAX_VALUE))
    }

    @Test
    fun pingLong() = withRustLib {
        assertEquals(9223372036854775805L, pingLong(9223372036854775805L))
    }

    @Test
    fun pingULong() = withRustLib {
        assertEquals(ULong.MAX_VALUE, natives.test.rust.pingUlong(ULong.MAX_VALUE))
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
    fun pingStringEmpty() = withRustLib {
        assertEquals("", natives.test.rust.pingStringEmpty(""))
    }

    @Test
    fun pingStringN() = withRustLib {
        assertNull(natives.test.rust.pingStringN(null))
    }

    @Test
    fun pingEnum() = withRustLib {
        assertEquals(MyEnum.CASE2, natives.test.rust.pingEnum(MyEnum.CASE2))
    }

    @Test
    fun pingDictionary() = withRustLib {
        assertEquals(
            MyDictionary(1, 2, 3, 4),
            natives.test.rust.pingDictionary(MyDictionary(1, 2, 3, 4))
        )
    }

    @Test
    fun pingEmptyDictionary() = withRustLib {
        assertEquals(
            EmptyDictionary(),
            natives.test.rust.pingEmptyDictionary(EmptyDictionary())
        )
    }

    @Test
    fun pingDictionaryN() = withRustLib {
        assertNull(natives.test.rust.pingDictionaryN(null))
    }

    @Test
    fun pingInterface() = withRustLib {
        val obj = MyInterface()
        val copy = natives.test.rust.pingInterface(obj)
        assertEquals(obj, copy)
        obj.close()
        copy.close()
    }

    @Test
    fun pingInterfaceN() = withRustLib {
        assertNull(natives.test.rust.pingInterfaceN(null))
    }

}