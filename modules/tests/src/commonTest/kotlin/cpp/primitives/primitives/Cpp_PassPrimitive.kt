package cpp.primitives.primitives

import natives.testcpp.MyDictionary
import natives.testcpp.MyEnum
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
    fun passBoolean() = withCppLib {
        assertTrue(natives.testcpp.passBoolean(true))
    }

    @Test
    fun passByte() = withCppLib {
        assertTrue(natives.testcpp.passByte(1.toByte()))
    }

    @Test
    fun passUByte() = withCppLib {
        assertTrue(natives.testcpp.passUByte(UByte.MAX_VALUE))
    }

    @Test
    fun passShort() = withCppLib {
        assertTrue(natives.testcpp.passShort(1.toShort()))
    }

    @Test
    fun passUShort() = withCppLib {
        assertTrue(natives.testcpp.passUShort(UShort.MAX_VALUE))
    }

    @Test
    fun passInt() = withCppLib {
        assertTrue(natives.testcpp.passInt(99))
    }

    @Test
    fun passUInt() = withCppLib {
        assertTrue(natives.testcpp.passUInt(UInt.MAX_VALUE))
    }

    @Test
    fun passLong() = withCppLib {
        assertTrue(natives.testcpp.passLong(9223372036854775805L))
    }

    @Test
    fun passULong() = withCppLib {
        assertTrue(natives.testcpp.passULong(ULong.MAX_VALUE))
    }

    @Test
    fun passFloat() = withCppLib {
        assertTrue(natives.testcpp.passFloat(99.9f))
    }

    @Test
    fun passDouble() = withCppLib {
        assertTrue(natives.testcpp.passDouble(1.1))
    }

    @Test
    fun passString() = withCppLib {
        assertTrue(natives.testcpp.passString("test string"))
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
    fun passDictionary() = withCppLib {
        assertTrue(natives.testcpp.passDictionary(MyDictionary(1, 2, 3, 4)))
    }

    @Test
    fun passDictionaryN() = withCppLib {
        assertTrue(natives.testcpp.passDictionaryN(null))
    }
}