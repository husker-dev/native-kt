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
    fun passShort() = withLib {
        assertTrue(natives.test.passShort(1.toShort()))
    }

    @Test
    fun passInt() = withLib {
        assertTrue(natives.test.passInt(99))
    }

    @Test
    fun passLong() = withLib {
        assertTrue(natives.test.passLong(9223372036854775805L))
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
    fun passEnum() = withLib {
        assertTrue(natives.test.passEnum(MyEnum.CASE2))
    }

    @Test
    fun passDictionary() = withLib {
        assertTrue(natives.test.passDictionary(MyDictionary(1, 2, 3, 4)))
    }
}