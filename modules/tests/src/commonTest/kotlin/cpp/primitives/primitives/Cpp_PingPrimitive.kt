package cpp.primitives.primitives

import natives.testcpp.MyDictionary
import natives.testcpp.MyEnum
import natives.testcpp.pingBoolean
import natives.testcpp.pingByte
import natives.testcpp.pingChar
import natives.testcpp.pingDouble
import natives.testcpp.pingFloat
import natives.testcpp.pingInt
import natives.testcpp.pingLong
import natives.testcpp.pingString
import natives.testcpp.pingShort
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
        assertEquals(UByte.MAX_VALUE, natives.testcpp.pingUByte(UByte.MAX_VALUE))
    }

    @Test
    fun pingShort() = withCppLib {
        assertEquals(99.toShort(), pingShort(99.toShort()))
    }

    @Test
    fun pingUShort() = withCppLib {
        assertEquals(UShort.MAX_VALUE, natives.testcpp.pingUShort(UShort.MAX_VALUE))
    }

    @Test
    fun pingInt() = withCppLib {
        assertEquals(99, pingInt(99))
    }

    @Test
    fun pingUInt() = withCppLib {
        assertEquals(UInt.MAX_VALUE, natives.testcpp.pingUInt(UInt.MAX_VALUE))
    }

    @Test
    fun pingLong() = withCppLib {
        assertEquals(9223372036854775805L, pingLong(9223372036854775805L))
    }

    @Test
    fun pingULong() = withCppLib {
        assertEquals(ULong.MAX_VALUE, natives.testcpp.pingULong(ULong.MAX_VALUE))
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
    fun pingStringN() = withCppLib {
        assertNull(natives.testcpp.pingStringN(null))
    }

    @Test
    fun pingEnum() = withCppLib {
        assertEquals(MyEnum.CASE2, natives.testcpp.pingEnum(MyEnum.CASE2))
    }

    @Test
    fun pingDictionary() = withCppLib {
        assertEquals(
            MyDictionary(1, 2, 3, 4),
            natives.testcpp.pingDictionary(MyDictionary(1, 2, 3, 4))
        )
    }

    @Test
    fun pingDictionaryN() = withCppLib {
        assertNull(natives.testcpp.pingDictionaryN(null))
    }

}