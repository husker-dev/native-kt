package c.primitives

import withCLib
import natives.test.MyDictionary
import natives.test.MyEnum
import kotlin.test.Test
import kotlin.test.assertTrue

class C_PassPrimitive {

    @Test
    fun passVoid() = withCLib {
        assertTrue(natives.test.passVoid())
    }

    @Test
    fun passChar() = withCLib {
        assertTrue(natives.test.passChar('a'))
    }

    @Test
    fun passBoolean() = withCLib {
        assertTrue(natives.test.passBoolean(true))
    }

    @Test
    fun passByte() = withCLib {
        assertTrue(natives.test.passByte(1.toByte()))
    }

    @Test
    fun passUByte() = withCLib {
        assertTrue(natives.test.passUByte(UByte.MAX_VALUE))
    }

    @Test
    fun passShort() = withCLib {
        assertTrue(natives.test.passShort(1.toShort()))
    }

    @Test
    fun passUShort() = withCLib {
        assertTrue(natives.test.passUShort(UShort.MAX_VALUE))
    }

    @Test
    fun passInt() = withCLib {
        assertTrue(natives.test.passInt(99))
    }

    @Test
    fun passUInt() = withCLib {
        assertTrue(natives.test.passUInt(UInt.MAX_VALUE))
    }

    @Test
    fun passLong() = withCLib {
        assertTrue(natives.test.passLong(9223372036854775805L))
    }

    @Test
    fun passULong() = withCLib {
        assertTrue(natives.test.passULong(ULong.MAX_VALUE))
    }

    @Test
    fun passFloat() = withCLib {
        assertTrue(natives.test.passFloat(99.9f))
    }

    @Test
    fun passDouble() = withCLib {
        assertTrue(natives.test.passDouble(1.1))
    }

    @Test
    fun passString() = withCLib {
        assertTrue(natives.test.passString("test string"))
    }

    @Test
    fun passStringN() = withCLib {
        assertTrue(natives.test.passStringN(null))
    }

    @Test
    fun passEnum() = withCLib {
        assertTrue(natives.test.passEnum(MyEnum.CASE2))
    }

    @Test
    fun passDictionary() = withCLib {
        assertTrue(natives.test.passDictionary(MyDictionary(1, 2, 3, 4)))
    }

    @Test
    fun passDictionaryN() = withCLib {
        assertTrue(natives.test.passDictionaryN(null))
    }
}