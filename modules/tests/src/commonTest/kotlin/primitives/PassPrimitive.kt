package primitives

import natives.test.MyDictionary
import natives.test.MyEnum
import withLib
import kotlin.test.Test
import kotlin.test.assertTrue

class PassPrimitive {

    @Test
    fun passVoid() = withLib {
        assertTrue(natives.test.passVoid())
    }

    @Test
    fun passChar() = withLib {
        assertTrue(natives.test.passChar('a'))
    }

    @Test
    fun passBoolean() = withLib {
        assertTrue(natives.test.passBoolean(true))
    }

    @Test
    fun passByte() = withLib {
        assertTrue(natives.test.passByte(1.toByte()))
    }

    @Test
    fun passUByte() = withLib {
        assertTrue(natives.test.passUByte(UByte.MAX_VALUE))
    }

    @Test
    fun passShort() = withLib {
        assertTrue(natives.test.passShort(1.toShort()))
    }

    @Test
    fun passUShort() = withLib {
        assertTrue(natives.test.passUShort(UShort.MAX_VALUE))
    }

    @Test
    fun passInt() = withLib {
        assertTrue(natives.test.passInt(99))
    }

    @Test
    fun passUInt() = withLib {
        assertTrue(natives.test.passUInt(UInt.MAX_VALUE))
    }

    @Test
    fun passLong() = withLib {
        assertTrue(natives.test.passLong(9223372036854775805L))
    }

    @Test
    fun passULong() = withLib {
        assertTrue(natives.test.passULong(ULong.MAX_VALUE))
    }

    @Test
    fun passFloat() = withLib {
        assertTrue(natives.test.passFloat(99.9f))
    }

    @Test
    fun passDouble() = withLib {
        assertTrue(natives.test.passDouble(1.1))
    }

    @Test
    fun passString() = withLib {
        assertTrue(natives.test.passString("test string"))
    }

    @Test
    fun passStringN() = withLib {
        assertTrue(natives.test.passStringN(null))
    }

    @Test
    fun passEnum() = withLib {
        assertTrue(natives.test.passEnum(MyEnum.CASE2))
    }

    @Test
    fun passDictionary() = withLib {
        assertTrue(natives.test.passDictionary(MyDictionary(1, 2, 3, 4)))
    }

    @Test
    fun passDictionaryN() = withLib {
        assertTrue(natives.test.passDictionaryN(null))
    }
}