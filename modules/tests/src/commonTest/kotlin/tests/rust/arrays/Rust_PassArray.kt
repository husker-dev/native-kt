@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.rust.arrays

import withRustLib
import natives.testrs.MyDictionary
import natives.testrs.MyEnum
import natives.testrs.MyInterface
import kotlin.test.Test
import kotlin.test.assertTrue

class Rust_PassArray {

    @Test
    fun passCharArray() = withRustLib {
        assertTrue(natives.testrs.passCharArray(charArrayOf('a', 'b')))
    }

    @Test
    fun passCharArrayEmpty() = withRustLib {
        assertTrue(natives.testrs.passCharArrayEmpty(charArrayOf()))
    }

    @Test
    fun passCharArrayN() = withRustLib {
        assertTrue(natives.testrs.passCharArrayN(null))
    }

    @Test
    fun passCharNArray() = withRustLib {
        assertTrue(natives.testrs.passCharNArray(arrayOf(null, 'a')))
    }

    @Test
    fun passBooleanArray() = withRustLib {
        assertTrue(natives.testrs.passBooleanArray(booleanArrayOf(true, false)))
    }

    @Test
    fun passBooleanNArray() = withRustLib {
        assertTrue(natives.testrs.passBooleanNArray(arrayOf(null, true)))
    }

    @Test
    fun passByteArray() = withRustLib {
        assertTrue(natives.testrs.passByteArray(byteArrayOf(1, 2)))
    }

    @Test
    fun passByteNArray() = withRustLib {
        assertTrue(natives.testrs.passByteNArray(arrayOf(null, 1.toByte())))
    }

    @Test
    fun passUByteArray() = withRustLib {
        assertTrue(natives.testrs.passUByteArray(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)))
    }

    @Test
    fun passUByteNArray() = withRustLib {
        assertTrue(natives.testrs.passUByteNArray(arrayOf(null, UByte.MAX_VALUE)))
    }

    @Test
    fun passShortArray() = withRustLib {
        assertTrue(natives.testrs.passShortArray(shortArrayOf(1, 2)))
    }

    @Test
    fun passShortNArray() = withRustLib {
        assertTrue(natives.testrs.passShortNArray(arrayOf(null, 1.toShort())))
    }

    @Test
    fun passUShortArray() = withRustLib {
        assertTrue(natives.testrs.passUShortArray(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE)))
    }

    @Test
    fun passUShortNArray() = withRustLib {
        assertTrue(natives.testrs.passUShortNArray(arrayOf(null, UShort.MAX_VALUE)))
    }

    @Test
    fun passIntArray() = withRustLib {
        assertTrue(natives.testrs.passIntArray(intArrayOf(1, 2)))
    }

    @Test
    fun passIntNArray() = withRustLib {
        assertTrue(natives.testrs.passIntNArray(arrayOf(null, 1)))
    }

    @Test
    fun passUIntArray() = withRustLib {
        assertTrue(natives.testrs.passUIntArray(uintArrayOf(1.toUInt(), UInt.MAX_VALUE)))
    }

    @Test
    fun passUIntNArray() = withRustLib {
        assertTrue(natives.testrs.passUIntNArray(arrayOf(null, UInt.MAX_VALUE)))
    }

    @Test
    fun passLongArray() = withRustLib {
        assertTrue(natives.testrs.passLongArray(longArrayOf(1, 2)))
    }

    @Test
    fun passLongNArray() = withRustLib {
        assertTrue(natives.testrs.passLongNArray(arrayOf(null, 1L)))
    }

    @Test
    fun passULongArray() = withRustLib {
        assertTrue(natives.testrs.passULongArray(ulongArrayOf(1.toULong(), ULong.MAX_VALUE)))
    }

    @Test
    fun passULongNArray() = withRustLib {
        assertTrue(natives.testrs.passULongNArray(arrayOf(null, ULong.MAX_VALUE)))
    }

    @Test
    fun passFloatArray() = withRustLib {
        assertTrue(natives.testrs.passFloatArray(floatArrayOf(1.1f, 2.2f)))
    }

    @Test
    fun passFloatNArray() = withRustLib {
        assertTrue(natives.testrs.passFloatNArray(arrayOf(null, 1.1f)))
    }

    @Test
    fun passDoubleArray() = withRustLib {
        assertTrue(natives.testrs.passDoubleArray(doubleArrayOf(1.1, 2.2)))
    }

    @Test
    fun passDoubleNArray() = withRustLib {
        assertTrue(natives.testrs.passDoubleNArray(arrayOf(null, 1.1)))
    }

    @Test
    fun passStringArray() = withRustLib {
        assertTrue(natives.testrs.passStringArray(arrayOf("string1", "string2")))
    }

    @Test
    fun passStringArrayN() = withRustLib {
        assertTrue(natives.testrs.passStringArrayN(arrayOf(null, null)))
    }

    @Test
    fun passEnumArray() = withRustLib {
        assertTrue(natives.testrs.passEnumArray(arrayOf(MyEnum.CASE1, MyEnum.CASE2)))
    }

    @Test
    fun passEnumNArray() = withRustLib {
        assertTrue(natives.testrs.passEnumNArray(arrayOf(null, MyEnum.CASE2)))
    }

    @Test
    fun passDictionaryArray() = withRustLib {
        assertTrue(
            natives.testrs.passDictionaryArray(
                arrayOf(
                    MyDictionary(1, 2, 3, 4),
                    MyDictionary(5, 6, 7, 8)
                )
            )
        )
    }

    @Test
    fun passDictionaryArrayN() = withRustLib {
        assertTrue(natives.testrs.passDictionaryArrayN(arrayOf(null, null)))
    }

    @Test
    fun passInterfaceArray() = withRustLib {
        val elements = arrayOf(
            MyInterface(),
            MyInterface()
        )
        assertTrue(natives.testrs.passInterfaceArray(elements))
        elements.forEach { it.close() }
    }

    @Test
    fun passInterfaceArrayN() = withRustLib {
        assertTrue(natives.testrs.passInterfaceArrayN(arrayOf(null, null)))
    }
}