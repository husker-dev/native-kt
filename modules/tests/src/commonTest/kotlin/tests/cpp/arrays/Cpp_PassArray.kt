@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.cpp.arrays

import natives.test.cpp.MyDictionary
import natives.test.cpp.MyEnum
import natives.test.cpp.MyInterface
import withCppLib
import kotlin.test.Test
import kotlin.test.assertTrue

class Cpp_PassArray {

    @Test
    fun passCharArray() = withCppLib {
        assertTrue(natives.test.cpp.passCharArray(charArrayOf('a', 'b')))
    }

    @Test
    fun passCharArrayEmpty() = withCppLib {
        assertTrue(natives.test.cpp.passCharArrayEmpty(charArrayOf()))
    }

    @Test
    fun passCharArrayN() = withCppLib {
        assertTrue(natives.test.cpp.passCharArrayN(null))
    }

    @Test
    fun passCharNArray() = withCppLib {
        assertTrue(natives.test.cpp.passCharNArray(arrayOf(null, 'a')))
    }

    @Test
    fun passBooleanArray() = withCppLib {
        assertTrue(natives.test.cpp.passBooleanArray(booleanArrayOf(true, false)))
    }

    @Test
    fun passBooleanNArray() = withCppLib {
        assertTrue(natives.test.cpp.passBooleanNArray(arrayOf(null, true)))
    }

    @Test
    fun passByteArray() = withCppLib {
        assertTrue(natives.test.cpp.passByteArray(byteArrayOf(1, 2)))
    }

    @Test
    fun passByteNArray() = withCppLib {
        assertTrue(natives.test.cpp.passByteNArray(arrayOf(null, 1.toByte())))
    }

    @Test
    fun passUByteArray() = withCppLib {
        assertTrue(natives.test.cpp.passUbyteArray(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)))
    }

    @Test
    fun passUByteNArray() = withCppLib {
        assertTrue(natives.test.cpp.passUbyteNArray(arrayOf(null, UByte.MAX_VALUE)))
    }

    @Test
    fun passShortArray() = withCppLib {
        assertTrue(natives.test.cpp.passShortArray(shortArrayOf(1, 2)))
    }

    @Test
    fun passShortNArray() = withCppLib {
        assertTrue(natives.test.cpp.passShortNArray(arrayOf(null, 1.toShort())))
    }

    @Test
    fun passUShortArray() = withCppLib {
        assertTrue(natives.test.cpp.passUshortArray(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE)))
    }

    @Test
    fun passUShortNArray() = withCppLib {
        assertTrue(natives.test.cpp.passUshortNArray(arrayOf(null, UShort.MAX_VALUE)))
    }

    @Test
    fun passIntArray() = withCppLib {
        assertTrue(natives.test.cpp.passIntArray(intArrayOf(1, 2)))
    }

    @Test
    fun passIntNArray() = withCppLib {
        assertTrue(natives.test.cpp.passIntNArray(arrayOf(null, 1)))
    }

    @Test
    fun passUIntArray() = withCppLib {
        assertTrue(natives.test.cpp.passUintArray(uintArrayOf(1.toUInt(), UInt.MAX_VALUE)))
    }

    @Test
    fun passUIntNArray() = withCppLib {
        assertTrue(natives.test.cpp.passUintNArray(arrayOf(null, UInt.MAX_VALUE)))
    }

    @Test
    fun passLongArray() = withCppLib {
        assertTrue(natives.test.cpp.passLongArray(longArrayOf(1, 2)))
    }

    @Test
    fun passLongNArray() = withCppLib {
        assertTrue(natives.test.cpp.passLongNArray(arrayOf(null, 1L)))
    }

    @Test
    fun passULongArray() = withCppLib {
        assertTrue(natives.test.cpp.passUlongArray(ulongArrayOf(1.toULong(), ULong.MAX_VALUE)))
    }

    @Test
    fun passULongNArray() = withCppLib {
        assertTrue(natives.test.cpp.passUlongNArray(arrayOf(null, ULong.MAX_VALUE)))
    }

    @Test
    fun passFloatArray() = withCppLib {
        assertTrue(natives.test.cpp.passFloatArray(floatArrayOf(1.1f, 2.2f)))
    }

    @Test
    fun passFloatNArray() = withCppLib {
        assertTrue(natives.test.cpp.passFloatNArray(arrayOf(null, 1.1f)))
    }

    @Test
    fun passDoubleArray() = withCppLib {
        assertTrue(natives.test.cpp.passDoubleArray(doubleArrayOf(1.1, 2.2)))
    }

    @Test
    fun passDoubleNArray() = withCppLib {
        assertTrue(natives.test.cpp.passDoubleNArray(arrayOf(null, 1.1)))
    }

    @Test
    fun passStringArray() = withCppLib {
        assertTrue(natives.test.cpp.passStringArray(arrayOf("string1", "string2")))
    }

    @Test
    fun passStringArrayN() = withCppLib {
        assertTrue(natives.test.cpp.passStringArrayN(arrayOf(null, null)))
    }

    @Test
    fun passEnumArray() = withCppLib {
        assertTrue(natives.test.cpp.passEnumArray(arrayOf(MyEnum.CASE1, MyEnum.CASE2)))
    }

    @Test
    fun passEnumNArray() = withCppLib {
        assertTrue(natives.test.cpp.passEnumNArray(arrayOf(null, MyEnum.CASE2)))
    }

    @Test
    fun passDictionaryArray() = withCppLib {
        assertTrue(
            natives.test.cpp.passDictionaryArray(
                arrayOf(
                    MyDictionary(1, 2, 3, 4),
                    MyDictionary(5, 6, 7, 8)
                )
            )
        )
    }

    @Test
    fun passDictionaryArrayN() = withCppLib {
        assertTrue(natives.test.cpp.passDictionaryArrayN(arrayOf(null, null)))
    }

    @Test
    fun passInterfaceArray() = withCppLib {
        val elements = arrayOf(
            MyInterface(),
            MyInterface()
        )
        assertTrue(natives.test.cpp.passInterfaceArray(elements))
        elements.forEach { it.close() }
    }

    @Test
    fun passInterfaceArrayN() = withCppLib {
        assertTrue(natives.test.cpp.passInterfaceArrayN(arrayOf(null, null)))
    }
}