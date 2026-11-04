package tests.rust.primitives

import withRustLib
import natives.test.rust.MyDictionary
import natives.test.rust.MyEnum
import natives.test.rust.MyInterface
import kotlin.test.Test
import kotlin.test.assertTrue

class Rust_PassPrimitive {

    @Test
    fun passVoid() = withRustLib {
        assertTrue(natives.test.rust.passVoid())
    }

    @Test
    fun passChar() = withRustLib {
        assertTrue(natives.test.rust.passChar('a'))
    }

    @Test
    fun passCharN() = withRustLib {
        assertTrue(natives.test.rust.passCharN(null))
        assertTrue(natives.test.rust.passCharN('a'))
    }

    @Test
    fun passBoolean() = withRustLib {
        assertTrue(natives.test.rust.passBoolean(true))
    }

    @Test
    fun passBooleanN() = withRustLib {
        assertTrue(natives.test.rust.passBooleanN(null))
        assertTrue(natives.test.rust.passBooleanN(true))
    }

    @Test
    fun passByte() = withRustLib {
        assertTrue(natives.test.rust.passByte(1.toByte()))
    }

    @Test
    fun passByteN() = withRustLib {
        assertTrue(natives.test.rust.passByteN(null))
        assertTrue(natives.test.rust.passByteN(1.toByte()))
    }

    @Test
    fun passUByte() = withRustLib {
        assertTrue(natives.test.rust.passUbyte(UByte.MAX_VALUE))
    }

    @Test
    fun passUByteN() = withRustLib {
        assertTrue(natives.test.rust.passUbyteN(null))
        assertTrue(natives.test.rust.passUbyteN(UByte.MAX_VALUE))
    }

    @Test
    fun passShort() = withRustLib {
        assertTrue(natives.test.rust.passShort(1.toShort()))
    }

    @Test
    fun passShortN() = withRustLib {
        assertTrue(natives.test.rust.passShortN(null))
        assertTrue(natives.test.rust.passShortN(1.toShort()))
    }

    @Test
    fun passUShort() = withRustLib {
        assertTrue(natives.test.rust.passUshort(UShort.MAX_VALUE))
    }

    @Test
    fun passUShortN() = withRustLib {
        assertTrue(natives.test.rust.passUshortN(null))
        assertTrue(natives.test.rust.passUshortN(UShort.MAX_VALUE))
    }

    @Test
    fun passInt() = withRustLib {
        assertTrue(natives.test.rust.passInt(99))
    }

    @Test
    fun passIntN() = withRustLib {
        assertTrue(natives.test.rust.passIntN(null))
        assertTrue(natives.test.rust.passIntN(99))
    }

    @Test
    fun passUInt() = withRustLib {
        assertTrue(natives.test.rust.passUint(UInt.MAX_VALUE))
    }

    @Test
    fun passUIntN() = withRustLib {
        assertTrue(natives.test.rust.passUintN(null))
        assertTrue(natives.test.rust.passUintN(UInt.MAX_VALUE))
    }

    @Test
    fun passLong() = withRustLib {
        assertTrue(natives.test.rust.passLong(9223372036854775805L))
    }

    @Test
    fun passLongN() = withRustLib {
        assertTrue(natives.test.rust.passLongN(null))
        assertTrue(natives.test.rust.passLongN(9223372036854775805L))
    }

    @Test
    fun passULong() = withRustLib {
        assertTrue(natives.test.rust.passUlong(ULong.MAX_VALUE))
    }

    @Test
    fun passULongN() = withRustLib {
        assertTrue(natives.test.rust.passUlongN(null))
        assertTrue(natives.test.rust.passUlongN(ULong.MAX_VALUE))
    }

    @Test
    fun passFloat() = withRustLib {
        assertTrue(natives.test.rust.passFloat(99.9f))
    }

    @Test
    fun passFloatN() = withRustLib {
        assertTrue(natives.test.rust.passFloatN(null))
        assertTrue(natives.test.rust.passFloatN(99.9f))
    }

    @Test
    fun passDouble() = withRustLib {
        assertTrue(natives.test.rust.passDouble(1.1))
    }

    @Test
    fun passDoubleN() = withRustLib {
        assertTrue(natives.test.rust.passDoubleN(null))
        assertTrue(natives.test.rust.passDoubleN(1.1))
    }

    @Test
    fun passString() = withRustLib {
        assertTrue(natives.test.rust.passString("test string"))
    }

    @Test
    fun passStringEmpty() = withRustLib {
        assertTrue(natives.test.rust.passStringEmpty(""))
    }

    @Test
    fun passStringN() = withRustLib {
        assertTrue(natives.test.rust.passStringN(null))
    }

    @Test
    fun passEnum() = withRustLib {
        assertTrue(natives.test.rust.passEnum(MyEnum.CASE2))
    }

    @Test
    fun passEnumN() = withRustLib {
        assertTrue(natives.test.rust.passEnumN(null))
        assertTrue(natives.test.rust.passEnumN(MyEnum.CASE2))
    }

    @Test
    fun passDictionary() = withRustLib {
        assertTrue(natives.test.rust.passDictionary(MyDictionary(1, 2, 3, 4)))
    }

    @Test
    fun passDictionaryN() = withRustLib {
        assertTrue(natives.test.rust.passDictionaryN(null))
    }

    @Test
    fun passInterface() = withRustLib {
        val item = MyInterface()
        assertTrue(natives.test.rust.passInterface(item))
        item.close()
    }

    @Test
    fun passInterfaceN() = withRustLib {
        assertTrue(natives.test.rust.passInterfaceN(null))
    }
}
