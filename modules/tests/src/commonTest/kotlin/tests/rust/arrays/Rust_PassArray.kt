@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.rust.arrays

import withRustLib
import natives.test.rust.MyDictionary
import natives.test.rust.MyEnum
import natives.test.rust.MyInterface
import kotlin.test.Test
import kotlin.test.assertTrue

class Rust_PassArray {

    @Test
    fun passCharArray() = withRustLib {
        assertTrue(natives.test.rust.passCharArray(charArrayOf('a', 'b')))
    }

    @Test
    fun passCharArrayEmpty() = withRustLib {
        assertTrue(natives.test.rust.passCharArrayEmpty(charArrayOf()))
    }

    @Test
    fun passCharArrayN() = withRustLib {
        assertTrue(natives.test.rust.passCharArrayN(null))
    }

    @Test
    fun passCharNArray() = withRustLib {
        assertTrue(natives.test.rust.passCharNArray(arrayOf(null, 'a')))
    }

    @Test
    fun passBooleanArray() = withRustLib {
        assertTrue(natives.test.rust.passBooleanArray(booleanArrayOf(true, false)))
    }

    @Test
    fun passBooleanNArray() = withRustLib {
        assertTrue(natives.test.rust.passBooleanNArray(arrayOf(null, true)))
    }

    @Test
    fun passByteArray() = withRustLib {
        assertTrue(natives.test.rust.passByteArray(byteArrayOf(1, 2)))
    }

    @Test
    fun passByteNArray() = withRustLib {
        assertTrue(natives.test.rust.passByteNArray(arrayOf(null, 1.toByte())))
    }

    @Test
    fun passUByteArray() = withRustLib {
        assertTrue(natives.test.rust.passUbyteArray(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)))
    }

    @Test
    fun passUByteNArray() = withRustLib {
        assertTrue(natives.test.rust.passUbyteNArray(arrayOf(null, UByte.MAX_VALUE)))
    }

    @Test
    fun passShortArray() = withRustLib {
        assertTrue(natives.test.rust.passShortArray(shortArrayOf(1, 2)))
    }

    @Test
    fun passShortNArray() = withRustLib {
        assertTrue(natives.test.rust.passShortNArray(arrayOf(null, 1.toShort())))
    }

    @Test
    fun passUShortArray() = withRustLib {
        assertTrue(natives.test.rust.passUshortArray(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE)))
    }

    @Test
    fun passUShortNArray() = withRustLib {
        assertTrue(natives.test.rust.passUshortNArray(arrayOf(null, UShort.MAX_VALUE)))
    }

    @Test
    fun passIntArray() = withRustLib {
        assertTrue(natives.test.rust.passIntArray(intArrayOf(1, 2)))
    }

    @Test
    fun passIntNArray() = withRustLib {
        assertTrue(natives.test.rust.passIntNArray(arrayOf(null, 1)))
    }

    @Test
    fun passUIntArray() = withRustLib {
        assertTrue(natives.test.rust.passUintArray(uintArrayOf(1.toUInt(), UInt.MAX_VALUE)))
    }

    @Test
    fun passUIntNArray() = withRustLib {
        assertTrue(natives.test.rust.passUintNArray(arrayOf(null, UInt.MAX_VALUE)))
    }

    @Test
    fun passLongArray() = withRustLib {
        assertTrue(natives.test.rust.passLongArray(longArrayOf(1, 2)))
    }

    @Test
    fun passLongNArray() = withRustLib {
        assertTrue(natives.test.rust.passLongNArray(arrayOf(null, 1L)))
    }

    @Test
    fun passULongArray() = withRustLib {
        assertTrue(natives.test.rust.passUlongArray(ulongArrayOf(1.toULong(), ULong.MAX_VALUE)))
    }

    @Test
    fun passULongNArray() = withRustLib {
        assertTrue(natives.test.rust.passUlongNArray(arrayOf(null, ULong.MAX_VALUE)))
    }

    @Test
    fun passFloatArray() = withRustLib {
        assertTrue(natives.test.rust.passFloatArray(floatArrayOf(1.1f, 2.2f)))
    }

    @Test
    fun passFloatNArray() = withRustLib {
        assertTrue(natives.test.rust.passFloatNArray(arrayOf(null, 1.1f)))
    }

    @Test
    fun passDoubleArray() = withRustLib {
        assertTrue(natives.test.rust.passDoubleArray(doubleArrayOf(1.1, 2.2)))
    }

    @Test
    fun passDoubleNArray() = withRustLib {
        assertTrue(natives.test.rust.passDoubleNArray(arrayOf(null, 1.1)))
    }

    @Test
    fun passStringArray() = withRustLib {
        assertTrue(natives.test.rust.passStringArray(arrayOf("string1", "string2")))
    }

    @Test
    fun passStringArrayN() = withRustLib {
        assertTrue(natives.test.rust.passStringArrayN(arrayOf(null, null)))
    }

    @Test
    fun passEnumArray() = withRustLib {
        assertTrue(natives.test.rust.passEnumArray(arrayOf(MyEnum.CASE1, MyEnum.CASE2)))
    }

    @Test
    fun passEnumNArray() = withRustLib {
        assertTrue(natives.test.rust.passEnumNArray(arrayOf(null, MyEnum.CASE2)))
    }

    @Test
    fun passDictionaryArray() = withRustLib {
        assertTrue(
            natives.test.rust.passDictionaryArray(
                arrayOf(
                    MyDictionary(1, 2, 3, 4),
                    MyDictionary(5, 6, 7, 8)
                )
            )
        )
    }

    @Test
    fun passDictionaryArrayN() = withRustLib {
        assertTrue(natives.test.rust.passDictionaryArrayN(arrayOf(null, null)))
    }

    @Test
    fun passInterfaceArray() = withRustLib {
        val elements = arrayOf(
            MyInterface(),
            MyInterface()
        )
        assertTrue(natives.test.rust.passInterfaceArray(elements))
        elements.forEach { it.close() }
    }

    @Test
    fun passInterfaceArrayN() = withRustLib {
        assertTrue(natives.test.rust.passInterfaceArrayN(arrayOf(null, null)))
    }
}