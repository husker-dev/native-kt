@file:OptIn(ExperimentalUnsignedTypes::class)

package cpp.primitives.arrays

import natives.testcpp.MyDictionary
import natives.testcpp.MyEnum
import withCppLib
import kotlin.test.Test
import kotlin.test.assertTrue

class Cpp_PassArray {

    @Test
    fun passCharArray() = withCppLib {
        assertTrue(natives.testcpp.passCharArray(charArrayOf('a', 'b')))
    }

    @Test
    fun passCharArrayN() = withCppLib {
        assertTrue(natives.testcpp.passCharArrayN(null))
    }

    @Test
    fun passBooleanArray() = withCppLib {
        assertTrue(natives.testcpp.passBooleanArray(booleanArrayOf(true, false)))
    }

    @Test
    fun passByteArray() = withCppLib {
        assertTrue(natives.testcpp.passByteArray(byteArrayOf(1, 2)))
    }

    @Test
    fun passUByteArray() = withCppLib {
        assertTrue(natives.testcpp.passUByteArray(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)))
    }

    @Test
    fun passShortArray() = withCppLib {
        assertTrue(natives.testcpp.passShortArray(shortArrayOf(1, 2)))
    }

    @Test
    fun passUShortArray() = withCppLib {
        assertTrue(natives.testcpp.passUShortArray(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE)))
    }

    @Test
    fun passIntArray() = withCppLib {
        assertTrue(natives.testcpp.passIntArray(intArrayOf(1, 2)))
    }

    @Test
    fun passUIntArray() = withCppLib {
        assertTrue(natives.testcpp.passUIntArray(uintArrayOf(1.toUInt(), UInt.MAX_VALUE)))
    }

    @Test
    fun passLongArray() = withCppLib {
        assertTrue(natives.testcpp.passLongArray(longArrayOf(1, 2)))
    }

    @Test
    fun passULongArray() = withCppLib {
        assertTrue(natives.testcpp.passULongArray(ulongArrayOf(1.toULong(), ULong.MAX_VALUE)))
    }

    @Test
    fun passFloatArray() = withCppLib {
        assertTrue(natives.testcpp.passFloatArray(floatArrayOf(1.1f, 2.2f)))
    }

    @Test
    fun passDoubleArray() = withCppLib {
        assertTrue(natives.testcpp.passDoubleArray(doubleArrayOf(1.1, 2.2)))
    }

    @Test
    fun passStringArray() = withCppLib {
        assertTrue(natives.testcpp.passStringArray(arrayOf("string1", "string2")))
    }

    @Test
    fun passStringArrayN() = withCppLib {
        assertTrue(natives.testcpp.passStringArrayN(arrayOf(null, null)))
    }

    @Test
    fun passEnumArray() = withCppLib {
        assertTrue(natives.testcpp.passEnumArray(arrayOf(MyEnum.CASE1, MyEnum.CASE2)))
    }

    @Test
    fun passDictionaryArray() = withCppLib {
        assertTrue(
            natives.testcpp.passDictionaryArray(
                arrayOf(
                    MyDictionary(1, 2, 3, 4),
                    MyDictionary(5, 6, 7, 8)
                )
            )
        )
    }

    @Test
    fun passDictionaryArrayN() = withCppLib {
        assertTrue(natives.testcpp.passDictionaryArrayN(arrayOf(null, null)))
    }
}