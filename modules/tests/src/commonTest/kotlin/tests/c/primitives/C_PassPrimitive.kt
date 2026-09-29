package tests.c.primitives

import withCLib
import natives.test.MyDictionary
import natives.test.MyEnum
import natives.test.MyInterface
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
    fun passCharN() = withCLib {
        assertTrue(natives.test.passCharN(null))
        assertTrue(natives.test.passCharN('a'))
    }

    @Test
    fun passBoolean() = withCLib {
        assertTrue(natives.test.passBoolean(true))
    }

    @Test
    fun passBooleanN() = withCLib {
        assertTrue(natives.test.passBooleanN(null))
        assertTrue(natives.test.passBooleanN(true))
    }

    @Test
    fun passByte() = withCLib {
        assertTrue(natives.test.passByte(1.toByte()))
    }

    @Test
    fun passByteN() = withCLib {
        assertTrue(natives.test.passByteN(null))
        assertTrue(natives.test.passByteN(1.toByte()))
    }

    @Test
    fun passUByte() = withCLib {
        assertTrue(natives.test.passUByte(UByte.MAX_VALUE))
    }

    @Test
    fun passUByteN() = withCLib {
        assertTrue(natives.test.passUByteN(null))
        assertTrue(natives.test.passUByteN(UByte.MAX_VALUE))
    }

    @Test
    fun passShort() = withCLib {
        assertTrue(natives.test.passShort(1.toShort()))
    }

    @Test
    fun passShortN() = withCLib {
        assertTrue(natives.test.passShortN(null))
        assertTrue(natives.test.passShortN(1.toShort()))
    }

    @Test
    fun passUShort() = withCLib {
        assertTrue(natives.test.passUShort(UShort.MAX_VALUE))
    }

    @Test
    fun passUShortN() = withCLib {
        assertTrue(natives.test.passUShortN(null))
        assertTrue(natives.test.passUShortN(UShort.MAX_VALUE))
    }

    @Test
    fun passInt() = withCLib {
        assertTrue(natives.test.passInt(99))
    }

    @Test
    fun passIntN() = withCLib {
        assertTrue(natives.test.passIntN(null))
        assertTrue(natives.test.passIntN(99))
    }

    @Test
    fun passUInt() = withCLib {
        assertTrue(natives.test.passUInt(UInt.MAX_VALUE))
    }

    @Test
    fun passUIntN() = withCLib {
        assertTrue(natives.test.passUIntN(null))
        assertTrue(natives.test.passUIntN(UInt.MAX_VALUE))
    }

    @Test
    fun passLong() = withCLib {
        assertTrue(natives.test.passLong(9223372036854775805L))
    }

    @Test
    fun passLongN() = withCLib {
        assertTrue(natives.test.passLongN(null))
        assertTrue(natives.test.passLongN(9223372036854775805L))
    }

    @Test
    fun passULong() = withCLib {
        assertTrue(natives.test.passULong(ULong.MAX_VALUE))
    }

    @Test
    fun passULongN() = withCLib {
        assertTrue(natives.test.passULongN(null))
        assertTrue(natives.test.passULongN(ULong.MAX_VALUE))
    }

    @Test
    fun passFloat() = withCLib {
        assertTrue(natives.test.passFloat(99.9f))
    }

    @Test
    fun passFloatN() = withCLib {
        assertTrue(natives.test.passFloatN(null))
        assertTrue(natives.test.passFloatN(99.9f))
    }

    @Test
    fun passDouble() = withCLib {
        assertTrue(natives.test.passDouble(1.1))
    }

    @Test
    fun passDoubleN() = withCLib {
        assertTrue(natives.test.passDoubleN(null))
        assertTrue(natives.test.passDoubleN(1.1))
    }

    @Test
    fun passString() = withCLib {
        assertTrue(natives.test.passString("test string"))
    }

    @Test
    fun passStringEmpty() = withCLib {
        assertTrue(natives.test.passStringEmpty(""))
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
    fun passEnumN() = withCLib {
        assertTrue(natives.test.passEnumN(null))
        assertTrue(natives.test.passEnumN(MyEnum.CASE2))
    }

    @Test
    fun passDictionary() = withCLib {
        assertTrue(natives.test.passDictionary(MyDictionary(1, 2, 3, 4)))
    }

    @Test
    fun passDictionaryN() = withCLib {
        assertTrue(natives.test.passDictionaryN(null))
    }

    @Test
    fun passInterface() = withCLib {
        val item = MyInterface()
        assertTrue(natives.test.passInterface(item))
        item.close()
    }

    @Test
    fun passInterfaceN() = withCLib {
        assertTrue(natives.test.passInterfaceN(null))
    }
}
