@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.swift.arrays

import withSwiftLib
import natives.test.swift.MyDictionary
import natives.test.swift.MyEnum
import natives.test.swift.MyInterface
import kotlin.test.Test
import kotlin.test.assertTrue

class Swift_PassArray {

    @Test
    fun passCharArray() = withSwiftLib {
        assertTrue(natives.test.swift.passCharArray(charArrayOf('a', 'b')))
    }

    @Test
    fun passCharArrayEmpty() = withSwiftLib {
        assertTrue(natives.test.swift.passCharArrayEmpty(charArrayOf()))
    }

    @Test
    fun passCharArrayN() = withSwiftLib {
        assertTrue(natives.test.swift.passCharArrayN(null))
    }

    @Test
    fun passCharNArray() = withSwiftLib {
        assertTrue(natives.test.swift.passCharNArray(arrayOf(null, 'a')))
    }

    @Test
    fun passBooleanArray() = withSwiftLib {
        assertTrue(natives.test.swift.passBooleanArray(booleanArrayOf(true, false)))
    }

    @Test
    fun passBooleanNArray() = withSwiftLib {
        assertTrue(natives.test.swift.passBooleanNArray(arrayOf(null, true)))
    }

    @Test
    fun passByteArray() = withSwiftLib {
        assertTrue(natives.test.swift.passByteArray(byteArrayOf(1, 2)))
    }

    @Test
    fun passByteNArray() = withSwiftLib {
        assertTrue(natives.test.swift.passByteNArray(arrayOf(null, 1.toByte())))
    }

    @Test
    fun passUByteArray() = withSwiftLib {
        assertTrue(natives.test.swift.passUbyteArray(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)))
    }

    @Test
    fun passUByteNArray() = withSwiftLib {
        assertTrue(natives.test.swift.passUbyteNArray(arrayOf(null, UByte.MAX_VALUE)))
    }

    @Test
    fun passShortArray() = withSwiftLib {
        assertTrue(natives.test.swift.passShortArray(shortArrayOf(1, 2)))
    }

    @Test
    fun passShortNArray() = withSwiftLib {
        assertTrue(natives.test.swift.passShortNArray(arrayOf(null, 1.toShort())))
    }

    @Test
    fun passUShortArray() = withSwiftLib {
        assertTrue(natives.test.swift.passUshortArray(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE)))
    }

    @Test
    fun passUShortNArray() = withSwiftLib {
        assertTrue(natives.test.swift.passUshortNArray(arrayOf(null, UShort.MAX_VALUE)))
    }

    @Test
    fun passIntArray() = withSwiftLib {
        assertTrue(natives.test.swift.passIntArray(intArrayOf(1, 2)))
    }

    @Test
    fun passIntNArray() = withSwiftLib {
        assertTrue(natives.test.swift.passIntNArray(arrayOf(null, 1)))
    }

    @Test
    fun passUIntArray() = withSwiftLib {
        assertTrue(natives.test.swift.passUintArray(uintArrayOf(1.toUInt(), UInt.MAX_VALUE)))
    }

    @Test
    fun passUIntNArray() = withSwiftLib {
        assertTrue(natives.test.swift.passUintNArray(arrayOf(null, UInt.MAX_VALUE)))
    }

    @Test
    fun passLongArray() = withSwiftLib {
        assertTrue(natives.test.swift.passLongArray(longArrayOf(1, 2)))
    }

    @Test
    fun passLongNArray() = withSwiftLib {
        assertTrue(natives.test.swift.passLongNArray(arrayOf(null, 1L)))
    }

    @Test
    fun passULongArray() = withSwiftLib {
        assertTrue(natives.test.swift.passUlongArray(ulongArrayOf(1.toULong(), ULong.MAX_VALUE)))
    }

    @Test
    fun passULongNArray() = withSwiftLib {
        assertTrue(natives.test.swift.passUlongNArray(arrayOf(null, ULong.MAX_VALUE)))
    }

    @Test
    fun passFloatArray() = withSwiftLib {
        assertTrue(natives.test.swift.passFloatArray(floatArrayOf(1.1f, 2.2f)))
    }

    @Test
    fun passFloatNArray() = withSwiftLib {
        assertTrue(natives.test.swift.passFloatNArray(arrayOf(null, 1.1f)))
    }

    @Test
    fun passDoubleArray() = withSwiftLib {
        assertTrue(natives.test.swift.passDoubleArray(doubleArrayOf(1.1, 2.2)))
    }

    @Test
    fun passDoubleNArray() = withSwiftLib {
        assertTrue(natives.test.swift.passDoubleNArray(arrayOf(null, 1.1)))
    }

    @Test
    fun passStringArray() = withSwiftLib {
        assertTrue(natives.test.swift.passStringArray(arrayOf("string1", "string2")))
    }

    @Test
    fun passStringArrayN() = withSwiftLib {
        assertTrue(natives.test.swift.passStringArrayN(arrayOf(null, null)))
    }

    @Test
    fun passEnumArray() = withSwiftLib {
        assertTrue(natives.test.swift.passEnumArray(arrayOf(MyEnum.CASE1, MyEnum.CASE2)))
    }

    @Test
    fun passEnumNArray() = withSwiftLib {
        assertTrue(natives.test.swift.passEnumNArray(arrayOf(null, MyEnum.CASE2)))
    }

    @Test
    fun passDictionaryArray() = withSwiftLib {
        assertTrue(
            natives.test.swift.passDictionaryArray(
                arrayOf(
                    MyDictionary(1, 2, 3, 4),
                    MyDictionary(5, 6, 7, 8)
                )
            )
        )
    }

    @Test
    fun passDictionaryArrayN() = withSwiftLib {
        assertTrue(natives.test.swift.passDictionaryArrayN(arrayOf(null, null)))
    }

    @Test
    fun passInterfaceArray() = withSwiftLib {
        val elements = arrayOf(
            MyInterface(),
            MyInterface()
        )
        assertTrue(natives.test.swift.passInterfaceArray(elements))
        elements.forEach { it.close() }
    }

    @Test
    fun passInterfaceArrayN() = withSwiftLib {
        assertTrue(natives.test.swift.passInterfaceArrayN(arrayOf(null, null)))
    }
}