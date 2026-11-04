package tests.swift.primitives

import withSwiftLib
import natives.test.swift.MyDictionary
import natives.test.swift.MyEnum
import natives.test.swift.MyInterface
import kotlin.test.Test
import kotlin.test.assertTrue

class Swift_PassPrimitive {

    @Test
    fun passVoid() = withSwiftLib {
        assertTrue(natives.test.swift.passVoid())
    }

    @Test
    fun passChar() = withSwiftLib {
        assertTrue(natives.test.swift.passChar('a'))
    }

    @Test
    fun passCharN() = withSwiftLib {
        assertTrue(natives.test.swift.passCharN(null))
        assertTrue(natives.test.swift.passCharN('a'))
    }

    @Test
    fun passBoolean() = withSwiftLib {
        assertTrue(natives.test.swift.passBoolean(true))
    }

    @Test
    fun passBooleanN() = withSwiftLib {
        assertTrue(natives.test.swift.passBooleanN(null))
        assertTrue(natives.test.swift.passBooleanN(true))
    }

    @Test
    fun passByte() = withSwiftLib {
        assertTrue(natives.test.swift.passByte(1.toByte()))
    }

    @Test
    fun passByteN() = withSwiftLib {
        assertTrue(natives.test.swift.passByteN(null))
        assertTrue(natives.test.swift.passByteN(1.toByte()))
    }

    @Test
    fun passUByte() = withSwiftLib {
        assertTrue(natives.test.swift.passUbyte(UByte.MAX_VALUE))
    }

    @Test
    fun passUByteN() = withSwiftLib {
        assertTrue(natives.test.swift.passUbyteN(null))
        assertTrue(natives.test.swift.passUbyteN(UByte.MAX_VALUE))
    }

    @Test
    fun passShort() = withSwiftLib {
        assertTrue(natives.test.swift.passShort(1.toShort()))
    }

    @Test
    fun passShortN() = withSwiftLib {
        assertTrue(natives.test.swift.passShortN(null))
        assertTrue(natives.test.swift.passShortN(1.toShort()))
    }

    @Test
    fun passUShort() = withSwiftLib {
        assertTrue(natives.test.swift.passUshort(UShort.MAX_VALUE))
    }

    @Test
    fun passUShortN() = withSwiftLib {
        assertTrue(natives.test.swift.passUshortN(null))
        assertTrue(natives.test.swift.passUshortN(UShort.MAX_VALUE))
    }

    @Test
    fun passInt() = withSwiftLib {
        assertTrue(natives.test.swift.passInt(99))
    }

    @Test
    fun passIntN() = withSwiftLib {
        assertTrue(natives.test.swift.passIntN(null))
        assertTrue(natives.test.swift.passIntN(99))
    }

    @Test
    fun passUInt() = withSwiftLib {
        assertTrue(natives.test.swift.passUint(UInt.MAX_VALUE))
    }

    @Test
    fun passUIntN() = withSwiftLib {
        assertTrue(natives.test.swift.passUintN(null))
        assertTrue(natives.test.swift.passUintN(UInt.MAX_VALUE))
    }

    @Test
    fun passLong() = withSwiftLib {
        assertTrue(natives.test.swift.passLong(9223372036854775805L))
    }

    @Test
    fun passLongN() = withSwiftLib {
        assertTrue(natives.test.swift.passLongN(null))
        assertTrue(natives.test.swift.passLongN(9223372036854775805L))
    }

    @Test
    fun passULong() = withSwiftLib {
        assertTrue(natives.test.swift.passUlong(ULong.MAX_VALUE))
    }

    @Test
    fun passULongN() = withSwiftLib {
        assertTrue(natives.test.swift.passUlongN(null))
        assertTrue(natives.test.swift.passUlongN(ULong.MAX_VALUE))
    }

    @Test
    fun passFloat() = withSwiftLib {
        assertTrue(natives.test.swift.passFloat(99.9f))
    }

    @Test
    fun passFloatN() = withSwiftLib {
        assertTrue(natives.test.swift.passFloatN(null))
        assertTrue(natives.test.swift.passFloatN(99.9f))
    }

    @Test
    fun passDouble() = withSwiftLib {
        assertTrue(natives.test.swift.passDouble(1.1))
    }

    @Test
    fun passDoubleN() = withSwiftLib {
        assertTrue(natives.test.swift.passDoubleN(null))
        assertTrue(natives.test.swift.passDoubleN(1.1))
    }

    @Test
    fun passString() = withSwiftLib {
        assertTrue(natives.test.swift.passString("test string"))
    }

    @Test
    fun passStringEmpty() = withSwiftLib {
        assertTrue(natives.test.swift.passStringEmpty(""))
    }

    @Test
    fun passStringN() = withSwiftLib {
        assertTrue(natives.test.swift.passStringN(null))
    }

    @Test
    fun passEnum() = withSwiftLib {
        assertTrue(natives.test.swift.passEnum(MyEnum.CASE2))
    }

    @Test
    fun passEnumN() = withSwiftLib {
        assertTrue(natives.test.swift.passEnumN(null))
        assertTrue(natives.test.swift.passEnumN(MyEnum.CASE2))
    }

    @Test
    fun passDictionary() = withSwiftLib {
        assertTrue(natives.test.swift.passDictionary(MyDictionary(1, 2, 3, 4)))
    }

    @Test
    fun passDictionaryN() = withSwiftLib {
        assertTrue(natives.test.swift.passDictionaryN(null))
    }

    @Test
    fun passInterface() = withSwiftLib {
        val item = MyInterface()
        assertTrue(natives.test.swift.passInterface(item))
        item.close()
    }

    @Test
    fun passInterfaceN() = withSwiftLib {
        assertTrue(natives.test.swift.passInterfaceN(null))
    }
}
