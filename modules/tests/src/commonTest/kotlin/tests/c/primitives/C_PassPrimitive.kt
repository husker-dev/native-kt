package tests.c.primitives

import withCLib
import natives.test.c.MyDictionary
import natives.test.c.MyEnum
import natives.test.c.MyInterface
import kotlin.test.Test
import kotlin.test.assertTrue

class C_PassPrimitive {

    @Test
    fun passVoid() = withCLib {
        assertTrue(natives.test.c.passVoid())
    }

    @Test
    fun passChar() = withCLib {
        assertTrue(natives.test.c.passChar('a'))
    }

    @Test
    fun passCharN() = withCLib {
        assertTrue(natives.test.c.passCharN(null))
        assertTrue(natives.test.c.passCharN('a'))
    }

    @Test
    fun passBoolean() = withCLib {
        assertTrue(natives.test.c.passBoolean(true))
    }

    @Test
    fun passBooleanN() = withCLib {
        assertTrue(natives.test.c.passBooleanN(null))
        assertTrue(natives.test.c.passBooleanN(true))
    }

    @Test
    fun passByte() = withCLib {
        assertTrue(natives.test.c.passByte(1.toByte()))
    }

    @Test
    fun passByteN() = withCLib {
        assertTrue(natives.test.c.passByteN(null))
        assertTrue(natives.test.c.passByteN(1.toByte()))
    }

    @Test
    fun passUByte() = withCLib {
        assertTrue(natives.test.c.passUbyte(UByte.MAX_VALUE))
    }

    @Test
    fun passUByteN() = withCLib {
        assertTrue(natives.test.c.passUbyteN(null))
        assertTrue(natives.test.c.passUbyteN(UByte.MAX_VALUE))
    }

    @Test
    fun passShort() = withCLib {
        assertTrue(natives.test.c.passShort(1.toShort()))
    }

    @Test
    fun passShortN() = withCLib {
        assertTrue(natives.test.c.passShortN(null))
        assertTrue(natives.test.c.passShortN(1.toShort()))
    }

    @Test
    fun passUShort() = withCLib {
        assertTrue(natives.test.c.passUshort(UShort.MAX_VALUE))
    }

    @Test
    fun passUShortN() = withCLib {
        assertTrue(natives.test.c.passUshortN(null))
        assertTrue(natives.test.c.passUshortN(UShort.MAX_VALUE))
    }

    @Test
    fun passInt() = withCLib {
        assertTrue(natives.test.c.passInt(99))
    }

    @Test
    fun passIntN() = withCLib {
        assertTrue(natives.test.c.passIntN(null))
        assertTrue(natives.test.c.passIntN(99))
    }

    @Test
    fun passUInt() = withCLib {
        assertTrue(natives.test.c.passUint(UInt.MAX_VALUE))
    }

    @Test
    fun passUIntN() = withCLib {
        assertTrue(natives.test.c.passUintN(null))
        assertTrue(natives.test.c.passUintN(UInt.MAX_VALUE))
    }

    @Test
    fun passLong() = withCLib {
        assertTrue(natives.test.c.passLong(9223372036854775805L))
    }

    @Test
    fun passLongN() = withCLib {
        assertTrue(natives.test.c.passLongN(null))
        assertTrue(natives.test.c.passLongN(9223372036854775805L))
    }

    @Test
    fun passULong() = withCLib {
        assertTrue(natives.test.c.passUlong(ULong.MAX_VALUE))
    }

    @Test
    fun passULongN() = withCLib {
        assertTrue(natives.test.c.passUlongN(null))
        assertTrue(natives.test.c.passUlongN(ULong.MAX_VALUE))
    }

    @Test
    fun passFloat() = withCLib {
        assertTrue(natives.test.c.passFloat(99.9f))
    }

    @Test
    fun passFloatN() = withCLib {
        assertTrue(natives.test.c.passFloatN(null))
        assertTrue(natives.test.c.passFloatN(99.9f))
    }

    @Test
    fun passDouble() = withCLib {
        assertTrue(natives.test.c.passDouble(1.1))
    }

    @Test
    fun passDoubleN() = withCLib {
        assertTrue(natives.test.c.passDoubleN(null))
        assertTrue(natives.test.c.passDoubleN(1.1))
    }

    @Test
    fun passString() = withCLib {
        assertTrue(natives.test.c.passString("test string"))
    }

    @Test
    fun passStringEmpty() = withCLib {
        assertTrue(natives.test.c.passStringEmpty(""))
    }

    @Test
    fun passStringN() = withCLib {
        assertTrue(natives.test.c.passStringN(null))
    }

    @Test
    fun passEnum() = withCLib {
        assertTrue(natives.test.c.passEnum(MyEnum.CASE2))
    }

    @Test
    fun passEnumN() = withCLib {
        assertTrue(natives.test.c.passEnumN(null))
        assertTrue(natives.test.c.passEnumN(MyEnum.CASE2))
    }

    @Test
    fun passDictionary() = withCLib {
        assertTrue(natives.test.c.passDictionary(MyDictionary(1, 2, 3, 4)))
    }

    @Test
    fun passDictionaryN() = withCLib {
        assertTrue(natives.test.c.passDictionaryN(null))
    }

    @Test
    fun passInterface() = withCLib {
        val item = MyInterface()
        assertTrue(natives.test.c.passInterface(item))
        item.close()
    }

    @Test
    fun passInterfaceN() = withCLib {
        assertTrue(natives.test.c.passInterfaceN(null))
    }
}
