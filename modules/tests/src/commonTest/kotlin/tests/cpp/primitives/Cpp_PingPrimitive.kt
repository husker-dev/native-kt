package tests.cpp.primitives

import natives.test.cpp.EmptyDictionary
import natives.test.cpp.MyDictionary
import natives.test.cpp.MyEnum
import natives.test.cpp.pingBoolean
import natives.test.cpp.pingByte
import natives.test.cpp.pingChar
import natives.test.cpp.pingDouble
import natives.test.cpp.pingFloat
import natives.test.cpp.pingInt
import natives.test.cpp.pingLong
import natives.test.cpp.pingString
import natives.test.cpp.pingShort
import natives.test.cpp.MyInterface
import withCppLib
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Cpp_PingPrimitive {

    @Test
    fun pingChar() = withCppLib {
        assertEquals('a', pingChar('a'))
    }

    @Test
    fun pingBoolean() = withCppLib {
        assertEquals(true, pingBoolean(true))
    }

    @Test
    fun pingByte() = withCppLib {
        assertEquals(99.toByte(), pingByte(99.toByte()))
    }

    @Test
    fun pingUByte() = withCppLib {
        assertEquals(UByte.MAX_VALUE, natives.test.cpp.pingUbyte(UByte.MAX_VALUE))
    }

    @Test
    fun pingShort() = withCppLib {
        assertEquals(99.toShort(), pingShort(99.toShort()))
    }

    @Test
    fun pingUShort() = withCppLib {
        assertEquals(UShort.MAX_VALUE, natives.test.cpp.pingUshort(UShort.MAX_VALUE))
    }

    @Test
    fun pingInt() = withCppLib {
        assertEquals(99, pingInt(99))
    }

    @Test
    fun pingUInt() = withCppLib {
        assertEquals(UInt.MAX_VALUE, natives.test.cpp.pingUint(UInt.MAX_VALUE))
    }

    @Test
    fun pingLong() = withCppLib {
        assertEquals(9223372036854775805L, pingLong(9223372036854775805L))
    }

    @Test
    fun pingULong() = withCppLib {
        assertEquals(ULong.MAX_VALUE, natives.test.cpp.pingUlong(ULong.MAX_VALUE))
    }

    @Test
    fun pingFloat() = withCppLib {
        assertEquals(99f, pingFloat(99f))
    }

    @Test
    fun pingDouble() = withCppLib {
        assertEquals(99.0, pingDouble(99.0))
    }

    @Test
    fun pingString() = withCppLib {
        assertEquals("test string", pingString("test string"))
    }

    @Test
    fun pingStringEmpty() = withCppLib {
        assertEquals("", pingString(""))
    }

    @Test
    fun pingStringN() = withCppLib {
        assertNull(natives.test.cpp.pingStringN(null))
    }

    @Test
    fun pingEnum() = withCppLib {
        assertEquals(MyEnum.CASE2, natives.test.cpp.pingEnum(MyEnum.CASE2))
    }

    @Test
    fun pingDictionary() = withCppLib {
        assertEquals(
            MyDictionary(1, 2, 3, 4),
            natives.test.cpp.pingDictionary(MyDictionary(1, 2, 3, 4))
        )
    }

    @Test
    fun pingDictionaryEmpty() = withCppLib {
        assertEquals(
            EmptyDictionary(),
            natives.test.cpp.pingEmptyDictionary(EmptyDictionary())
        )
    }

    @Test
    fun pingDictionaryN() = withCppLib {
        assertNull(natives.test.cpp.pingDictionaryN(null))
    }

    @Test
    fun pingInterface() = withCppLib {
        val obj = MyInterface()
        val copy = natives.test.cpp.pingInterface(obj)
        assertEquals(obj, copy)
        obj.close()
        copy.close()
    }

    @Test
    fun pingInterfaceN() = withCppLib {
        assertNull(natives.test.cpp.pingInterfaceN(null))
    }

}