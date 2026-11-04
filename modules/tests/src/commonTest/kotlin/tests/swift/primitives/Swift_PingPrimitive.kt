package tests.swift.primitives

import withSwiftLib
import natives.test.swift.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Swift_PingPrimitive {

    @Test
    fun pingChar() = withSwiftLib {
        assertEquals('a', pingChar('a'))
    }

    @Test
    fun pingBoolean() = withSwiftLib {
        assertEquals(true, pingBoolean(true))
    }

    @Test
    fun pingByte() = withSwiftLib {
        assertEquals(99.toByte(), pingByte(99.toByte()))
    }

    @Test
    fun pingUByte() = withSwiftLib {
        assertEquals(UByte.MAX_VALUE, natives.test.swift.pingUbyte(UByte.MAX_VALUE))
    }

    @Test
    fun pingShort() = withSwiftLib {
        assertEquals(99.toShort(), pingShort(99.toShort()))
    }

    @Test
    fun pingUShort() = withSwiftLib {
        assertEquals(UShort.MAX_VALUE, natives.test.swift.pingUshort(UShort.MAX_VALUE))
    }

    @Test
    fun pingInt() = withSwiftLib {
        assertEquals(99, pingInt(99))
    }

    @Test
    fun pingUInt() = withSwiftLib {
        assertEquals(UInt.MAX_VALUE, natives.test.swift.pingUint(UInt.MAX_VALUE))
    }

    @Test
    fun pingLong() = withSwiftLib {
        assertEquals(9223372036854775805L, pingLong(9223372036854775805L))
    }

    @Test
    fun pingULong() = withSwiftLib {
        assertEquals(ULong.MAX_VALUE, natives.test.swift.pingUlong(ULong.MAX_VALUE))
    }

    @Test
    fun pingFloat() = withSwiftLib {
        assertEquals(99f, pingFloat(99f))
    }

    @Test
    fun pingDouble() = withSwiftLib {
        assertEquals(99.0, pingDouble(99.0))
    }

    @Test
    fun pingString() = withSwiftLib {
        assertEquals("test string", pingString("test string"))
    }

    @Test
    fun pingStringEmpty() = withSwiftLib {
        assertEquals("", natives.test.swift.pingStringEmpty(""))
    }

    @Test
    fun pingStringN() = withSwiftLib {
        assertNull(natives.test.swift.pingStringN(null))
    }

    @Test
    fun pingEnum() = withSwiftLib {
        assertEquals(MyEnum.CASE2, natives.test.swift.pingEnum(MyEnum.CASE2))
    }

    @Test
    fun pingDictionary() = withSwiftLib {
        assertEquals(
            MyDictionary(1, 2, 3, 4),
            natives.test.swift.pingDictionary(MyDictionary(1, 2, 3, 4))
        )
    }

    @Test
    fun pingEmptyDictionary() = withSwiftLib {
        assertEquals(
            EmptyDictionary(),
            natives.test.swift.pingEmptyDictionary(EmptyDictionary())
        )
    }

    @Test
    fun pingDictionaryN() = withSwiftLib {
        assertNull(natives.test.swift.pingDictionaryN(null))
    }

    @Test
    fun pingInterface() = withSwiftLib {
        val obj = MyInterface()
        val copy = natives.test.swift.pingInterface(obj)
        assertEquals(obj, copy)
        obj.close()
        copy.close()
    }

    @Test
    fun pingInterfaceN() = withSwiftLib {
        assertNull(natives.test.swift.pingInterfaceN(null))
    }

}