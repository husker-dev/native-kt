package tests.cpp.primitives

import natives.test.cpp.MyDictionary
import natives.test.cpp.MyEnum
import natives.test.cpp.MyInterface
import withCppLib
import kotlin.test.Test
import kotlin.test.assertTrue

class Cpp_PassPrimitive {

    @Test
    fun passVoid() = withCppLib {
        assertTrue(natives.test.cpp.passVoid())
    }

    @Test
    fun passChar() = withCppLib {
        assertTrue(natives.test.cpp.passChar('a'))
    }

    @Test
    fun passCharN() = withCppLib {
        assertTrue(natives.test.cpp.passCharN(null))
        assertTrue(natives.test.cpp.passCharN('a'))
    }

    @Test
    fun passBoolean() = withCppLib {
        assertTrue(natives.test.cpp.passBoolean(true))
    }

    @Test
    fun passBooleanN() = withCppLib {
        assertTrue(natives.test.cpp.passBooleanN(null))
        assertTrue(natives.test.cpp.passBooleanN(true))
    }

    @Test
    fun passByte() = withCppLib {
        assertTrue(natives.test.cpp.passByte(1.toByte()))
    }

    @Test
    fun passByteN() = withCppLib {
        assertTrue(natives.test.cpp.passByteN(null))
        assertTrue(natives.test.cpp.passByteN(1.toByte()))
    }

    @Test
    fun passUByte() = withCppLib {
        assertTrue(natives.test.cpp.passUbyte(UByte.MAX_VALUE))
    }

    @Test
    fun passUByteN() = withCppLib {
        assertTrue(natives.test.cpp.passUbyteN(null))
        assertTrue(natives.test.cpp.passUbyteN(UByte.MAX_VALUE))
    }

    @Test
    fun passShort() = withCppLib {
        assertTrue(natives.test.cpp.passShort(1.toShort()))
    }

    @Test
    fun passShortN() = withCppLib {
        assertTrue(natives.test.cpp.passShortN(null))
        assertTrue(natives.test.cpp.passShortN(1.toShort()))
    }

    @Test
    fun passUShort() = withCppLib {
        assertTrue(natives.test.cpp.passUshort(UShort.MAX_VALUE))
    }

    @Test
    fun passUShortN() = withCppLib {
        assertTrue(natives.test.cpp.passUshortN(null))
        assertTrue(natives.test.cpp.passUshortN(UShort.MAX_VALUE))
    }

    @Test
    fun passInt() = withCppLib {
        assertTrue(natives.test.cpp.passInt(99))
    }

    @Test
    fun passIntN() = withCppLib {
        assertTrue(natives.test.cpp.passIntN(null))
        assertTrue(natives.test.cpp.passIntN(99))
    }

    @Test
    fun passUInt() = withCppLib {
        assertTrue(natives.test.cpp.passUint(UInt.MAX_VALUE))
    }

    @Test
    fun passUIntN() = withCppLib {
        assertTrue(natives.test.cpp.passUintN(null))
        assertTrue(natives.test.cpp.passUintN(UInt.MAX_VALUE))
    }

    @Test
    fun passLong() = withCppLib {
        assertTrue(natives.test.cpp.passLong(9223372036854775805L))
    }

    @Test
    fun passLongN() = withCppLib {
        assertTrue(natives.test.cpp.passLongN(null))
        assertTrue(natives.test.cpp.passLongN(9223372036854775805L))
    }

    @Test
    fun passULong() = withCppLib {
        assertTrue(natives.test.cpp.passUlong(ULong.MAX_VALUE))
    }

    @Test
    fun passULongN() = withCppLib {
        assertTrue(natives.test.cpp.passUlongN(null))
        assertTrue(natives.test.cpp.passUlongN(ULong.MAX_VALUE))
    }

    @Test
    fun passFloat() = withCppLib {
        assertTrue(natives.test.cpp.passFloat(99.9f))
    }

    @Test
    fun passFloatN() = withCppLib {
        assertTrue(natives.test.cpp.passFloatN(null))
        assertTrue(natives.test.cpp.passFloatN(99.9f))
    }

    @Test
    fun passDouble() = withCppLib {
        assertTrue(natives.test.cpp.passDouble(1.1))
    }

    @Test
    fun passDoubleN() = withCppLib {
        assertTrue(natives.test.cpp.passDoubleN(null))
        assertTrue(natives.test.cpp.passDoubleN(1.1))
    }

    @Test
    fun passString() = withCppLib {
        assertTrue(natives.test.cpp.passString("test string"))
    }

    @Test
    fun passStringEmpty() = withCppLib {
        assertTrue(natives.test.cpp.passStringEmpty(""))
    }

    @Test
    fun passStringN() = withCppLib {
        assertTrue(natives.test.cpp.passStringN(null))
    }

    @Test
    fun passEnum() = withCppLib {
        assertTrue(natives.test.cpp.passEnum(MyEnum.CASE2))
    }

    @Test
    fun passEnumN() = withCppLib {
        assertTrue(natives.test.cpp.passEnumN(null))
        assertTrue(natives.test.cpp.passEnumN(MyEnum.CASE2))
    }

    @Test
    fun passDictionary() = withCppLib {
        assertTrue(natives.test.cpp.passDictionary(MyDictionary(1, 2, 3, 4)))
    }

    @Test
    fun passDictionaryN() = withCppLib {
        assertTrue(natives.test.cpp.passDictionaryN(null))
    }

    @Test
    fun passInterface() = withCppLib {
        val item = MyInterface()
        assertTrue(natives.test.cpp.passInterface(item))
        item.close()
    }

    @Test
    fun passInterfaceN() = withCppLib {
        assertTrue(natives.test.cpp.passInterfaceN(null))
    }
}
