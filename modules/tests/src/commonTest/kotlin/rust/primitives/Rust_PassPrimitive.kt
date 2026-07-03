package rust.primitives

import withRustLib
import natives.testrs.MyDictionary
import natives.testrs.MyEnum
import kotlin.test.Test
import kotlin.test.assertTrue

class Rust_PassPrimitive {

    @Test
    fun passVoid() = withRustLib {
        assertTrue(natives.testrs.passVoid())
    }

    @Test
    fun passChar() = withRustLib {
        assertTrue(natives.testrs.passChar('a'))
    }

    @Test
    fun passBoolean() = withRustLib {
        assertTrue(natives.testrs.passBoolean(true))
    }

    @Test
    fun passByte() = withRustLib {
        assertTrue(natives.testrs.passByte(1.toByte()))
    }

    @Test
    fun passUByte() = withRustLib {
        assertTrue(natives.testrs.passUByte(UByte.MAX_VALUE))
    }

    @Test
    fun passShort() = withRustLib {
        assertTrue(natives.testrs.passShort(1.toShort()))
    }

    @Test
    fun passUShort() = withRustLib {
        assertTrue(natives.testrs.passUShort(UShort.MAX_VALUE))
    }

    @Test
    fun passInt() = withRustLib {
        assertTrue(natives.testrs.passInt(99))
    }

    @Test
    fun passUInt() = withRustLib {
        assertTrue(natives.testrs.passUInt(UInt.MAX_VALUE))
    }

    @Test
    fun passLong() = withRustLib {
        assertTrue(natives.testrs.passLong(9223372036854775805L))
    }

    @Test
    fun passULong() = withRustLib {
        assertTrue(natives.testrs.passULong(ULong.MAX_VALUE))
    }

    @Test
    fun passFloat() = withRustLib {
        assertTrue(natives.testrs.passFloat(99.9f))
    }

    @Test
    fun passDouble() = withRustLib {
        assertTrue(natives.testrs.passDouble(1.1))
    }

    @Test
    fun passString() = withRustLib {
        assertTrue(natives.testrs.passString("test string"))
    }

    @Test
    fun passStringN() = withRustLib {
        assertTrue(natives.testrs.passStringN(null))
    }

    @Test
    fun passEnum() = withRustLib {
        assertTrue(natives.testrs.passEnum(MyEnum.CASE2))
    }

    @Test
    fun passDictionary() = withRustLib {
        assertTrue(natives.testrs.passDictionary(MyDictionary(1, 2, 3, 4)))
    }

    @Test
    fun passDictionaryN() = withRustLib {
        assertTrue(natives.testrs.passDictionaryN(null))
    }
}