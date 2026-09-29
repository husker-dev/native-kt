package tests.cpp.primitives

import natives.testcpp.MyDictionary
import natives.testcpp.MyEnum
import natives.testcpp.MyInterface
import withCppLib
import kotlin.test.Test
import kotlin.test.assertTrue

class Cpp_PassPrimitive {

    @Test
    fun passVoid() = withCppLib {
        assertTrue(natives.testcpp.passVoid())
    }

    @Test
    fun passChar() = withCppLib {
        assertTrue(natives.testcpp.passChar('a'))
    }

    @Test
    fun passCharN() = withCppLib {
        assertTrue(natives.testcpp.passCharN(null))
        assertTrue(natives.testcpp.passCharN('a'))
    }

    @Test
    fun passBoolean() = withCppLib {
        assertTrue(natives.testcpp.passBoolean(true))
    }

    @Test
    fun passBooleanN() = withCppLib {
        assertTrue(natives.testcpp.passBooleanN(null))
        assertTrue(natives.testcpp.passBooleanN(true))
    }

    @Test
    fun passByte() = withCppLib {
        assertTrue(natives.testcpp.passByte(1.toByte()))
    }

    @Test
    fun passByteN() = withCppLib {
        assertTrue(natives.testcpp.passByteN(null))
        assertTrue(natives.testcpp.passByteN(1.toByte()))
    }

    @Test
    fun passUByte() = withCppLib {
        assertTrue(natives.testcpp.passUByte(UByte.MAX_VALUE))
    }

    @Test
    fun passUByteN() = withCppLib {
        assertTrue(natives.testcpp.passUByteN(null))
        assertTrue(natives.testcpp.passUByteN(UByte.MAX_VALUE))
    }

    @Test
    fun passShort() = withCppLib {
        assertTrue(natives.testcpp.passShort(1.toShort()))
    }

    @Test
    fun passShortN() = withCppLib {
        assertTrue(natives.testcpp.passShortN(null))
        assertTrue(natives.testcpp.passShortN(1.toShort()))
    }

    @Test
    fun passUShort() = withCppLib {
        assertTrue(natives.testcpp.passUShort(UShort.MAX_VALUE))
    }

    @Test
    fun passUShortN() = withCppLib {
        assertTrue(natives.testcpp.passUShortN(null))
        assertTrue(natives.testcpp.passUShortN(UShort.MAX_VALUE))
    }

    @Test
    fun passInt() = withCppLib {
        assertTrue(natives.testcpp.passInt(99))
    }

    @Test
    fun passIntN() = withCppLib {
        assertTrue(natives.testcpp.passIntN(null))
        assertTrue(natives.testcpp.passIntN(99))
    }

    @Test
    fun passUInt() = withCppLib {
        assertTrue(natives.testcpp.passUInt(UInt.MAX_VALUE))
    }

    @Test
    fun passUIntN() = withCppLib {
        assertTrue(natives.testcpp.passUIntN(null))
        assertTrue(natives.testcpp.passUIntN(UInt.MAX_VALUE))
    }

    @Test
    fun passLong() = withCppLib {
        assertTrue(natives.testcpp.passLong(9223372036854775805L))
    }

    @Test
    fun passLongN() = withCppLib {
        assertTrue(natives.testcpp.passLongN(null))
        assertTrue(natives.testcpp.passLongN(9223372036854775805L))
    }

    @Test
    fun passULong() = withCppLib {
        assertTrue(natives.testcpp.passULong(ULong.MAX_VALUE))
    }

    @Test
    fun passULongN() = withCppLib {
        assertTrue(natives.testcpp.passULongN(null))
        assertTrue(natives.testcpp.passULongN(ULong.MAX_VALUE))
    }

    @Test
    fun passFloat() = withCppLib {
        assertTrue(natives.testcpp.passFloat(99.9f))
    }

    @Test
    fun passFloatN() = withCppLib {
        assertTrue(natives.testcpp.passFloatN(null))
        assertTrue(natives.testcpp.passFloatN(99.9f))
    }

    @Test
    fun passDouble() = withCppLib {
        assertTrue(natives.testcpp.passDouble(1.1))
    }

    @Test
    fun passDoubleN() = withCppLib {
        assertTrue(natives.testcpp.passDoubleN(null))
        assertTrue(natives.testcpp.passDoubleN(1.1))
    }

    @Test
    fun passString() = withCppLib {
        assertTrue(natives.testcpp.passString("test string"))
    }

    @Test
    fun passStringEmpty() = withCppLib {
        assertTrue(natives.testcpp.passStringEmpty(""))
    }

    @Test
    fun passStringN() = withCppLib {
        assertTrue(natives.testcpp.passStringN(null))
    }

    @Test
    fun passEnum() = withCppLib {
        assertTrue(natives.testcpp.passEnum(MyEnum.CASE2))
    }

    @Test
    fun passEnumN() = withCppLib {
        assertTrue(natives.testcpp.passEnumN(null))
        assertTrue(natives.testcpp.passEnumN(MyEnum.CASE2))
    }

    @Test
    fun passDictionary() = withCppLib {
        assertTrue(natives.testcpp.passDictionary(MyDictionary(1, 2, 3, 4)))
    }

    @Test
    fun passDictionaryN() = withCppLib {
        assertTrue(natives.testcpp.passDictionaryN(null))
    }

    @Test
    fun passInterface() = withCppLib {
        val item = MyInterface()
        assertTrue(natives.testcpp.passInterface(item))
        item.close()
    }

    @Test
    fun passInterfaceN() = withCppLib {
        assertTrue(natives.testcpp.passInterfaceN(null))
    }
}
