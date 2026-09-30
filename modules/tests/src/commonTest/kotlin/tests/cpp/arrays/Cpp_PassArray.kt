@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.cpp.arrays

import natives.testcpp.MyDictionary
import natives.testcpp.MyEnum
import natives.testcpp.MyInterface
import withCppLib
import kotlin.test.Test
import kotlin.test.assertTrue

class Cpp_PassArray {

    @Test
    fun passCharArray() = withCppLib {
        assertTrue(natives.testcpp.passCharArray(charArrayOf('a', 'b')))
    }

    @Test
    fun passCharArrayEmpty() = withCppLib {
        assertTrue(natives.testcpp.passCharArrayEmpty(charArrayOf()))
    }

    @Test
    fun passCharArrayN() = withCppLib {
        assertTrue(natives.testcpp.passCharArrayN(null))
    }

    @Test
    fun passCharNArray() = withCppLib {
        assertTrue(natives.testcpp.passCharNArray(arrayOf(null, 'a')))
    }

    @Test
    fun passBooleanArray() = withCppLib {
        assertTrue(natives.testcpp.passBooleanArray(booleanArrayOf(true, false)))
    }

    @Test
    fun passBooleanNArray() = withCppLib {
        assertTrue(natives.testcpp.passBooleanNArray(arrayOf(null, true)))
    }

    @Test
    fun passByteArray() = withCppLib {
        assertTrue(natives.testcpp.passByteArray(byteArrayOf(1, 2)))
    }

    @Test
    fun passByteNArray() = withCppLib {
        assertTrue(natives.testcpp.passByteNArray(arrayOf(null, 1.toByte())))
    }

    @Test
    fun passUByteArray() = withCppLib {
        assertTrue(natives.testcpp.passUByteArray(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)))
    }

    @Test
    fun passUByteNArray() = withCppLib {
        assertTrue(natives.testcpp.passUByteNArray(arrayOf(null, UByte.MAX_VALUE)))
    }

    @Test
    fun passShortArray() = withCppLib {
        assertTrue(natives.testcpp.passShortArray(shortArrayOf(1, 2)))
    }

    @Test
    fun passShortNArray() = withCppLib {
        assertTrue(natives.testcpp.passShortNArray(arrayOf(null, 1.toShort())))
    }

    @Test
    fun passUShortArray() = withCppLib {
        assertTrue(natives.testcpp.passUShortArray(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE)))
    }

    @Test
    fun passUShortNArray() = withCppLib {
        assertTrue(natives.testcpp.passUShortNArray(arrayOf(null, UShort.MAX_VALUE)))
    }

    @Test
    fun passIntArray() = withCppLib {
        assertTrue(natives.testcpp.passIntArray(intArrayOf(1, 2)))
    }

    @Test
    fun passIntNArray() = withCppLib {
        assertTrue(natives.testcpp.passIntNArray(arrayOf(null, 1)))
    }

    @Test
    fun passUIntArray() = withCppLib {
        assertTrue(natives.testcpp.passUIntArray(uintArrayOf(1.toUInt(), UInt.MAX_VALUE)))
    }

    @Test
    fun passUIntNArray() = withCppLib {
        assertTrue(natives.testcpp.passUIntNArray(arrayOf(null, UInt.MAX_VALUE)))
    }

    @Test
    fun passLongArray() = withCppLib {
        assertTrue(natives.testcpp.passLongArray(longArrayOf(1, 2)))
    }

    @Test
    fun passLongNArray() = withCppLib {
        assertTrue(natives.testcpp.passLongNArray(arrayOf(null, 1L)))
    }

    @Test
    fun passULongArray() = withCppLib {
        assertTrue(natives.testcpp.passULongArray(ulongArrayOf(1.toULong(), ULong.MAX_VALUE)))
    }

    @Test
    fun passULongNArray() = withCppLib {
        assertTrue(natives.testcpp.passULongNArray(arrayOf(null, ULong.MAX_VALUE)))
    }

    @Test
    fun passFloatArray() = withCppLib {
        assertTrue(natives.testcpp.passFloatArray(floatArrayOf(1.1f, 2.2f)))
    }

    @Test
    fun passFloatNArray() = withCppLib {
        assertTrue(natives.testcpp.passFloatNArray(arrayOf(null, 1.1f)))
    }

    @Test
    fun passDoubleArray() = withCppLib {
        assertTrue(natives.testcpp.passDoubleArray(doubleArrayOf(1.1, 2.2)))
    }

    @Test
    fun passDoubleNArray() = withCppLib {
        assertTrue(natives.testcpp.passDoubleNArray(arrayOf(null, 1.1)))
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
    fun passEnumNArray() = withCppLib {
        assertTrue(natives.testcpp.passEnumNArray(arrayOf(null, MyEnum.CASE2)))
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

    @Test
    fun passInterfaceArray() = withCppLib {
        val elements = arrayOf(
            MyInterface(),
            MyInterface()
        )
        assertTrue(natives.testcpp.passInterfaceArray(elements))
        elements.forEach { it.close() }
    }

    @Test
    fun passInterfaceArrayN() = withCppLib {
        assertTrue(natives.testcpp.passInterfaceArrayN(arrayOf(null, null)))
    }
}