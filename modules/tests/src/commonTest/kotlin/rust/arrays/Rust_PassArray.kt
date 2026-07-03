@file:OptIn(ExperimentalUnsignedTypes::class)

package rust.arrays

import withRustLib
import natives.testrs.MyDictionary
import natives.testrs.MyEnum
import kotlin.test.Test
import kotlin.test.assertTrue

class Rust_PassArray {

    @Test
    fun passCharArray() = withRustLib {
        assertTrue(natives.testrs.passCharArray(charArrayOf('a', 'b')))
    }

    @Test
    fun passCharArrayN() = withRustLib {
        assertTrue(natives.testrs.passCharArrayN(null))
    }

    @Test
    fun passBooleanArray() = withRustLib {
        assertTrue(natives.testrs.passBooleanArray(booleanArrayOf(true, false)))
    }

    @Test
    fun passByteArray() = withRustLib {
        assertTrue(natives.testrs.passByteArray(byteArrayOf(1, 2)))
    }

    @Test
    fun passUByteArray() = withRustLib {
        assertTrue(natives.testrs.passUByteArray(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)))
    }

    @Test
    fun passShortArray() = withRustLib {
        assertTrue(natives.testrs.passShortArray(shortArrayOf(1, 2)))
    }

    @Test
    fun passUShortArray() = withRustLib {
        assertTrue(natives.testrs.passUShortArray(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE)))
    }

    @Test
    fun passIntArray() = withRustLib {
        assertTrue(natives.testrs.passIntArray(intArrayOf(1, 2)))
    }

    @Test
    fun passUIntArray() = withRustLib {
        assertTrue(natives.testrs.passUIntArray(uintArrayOf(1.toUInt(), UInt.MAX_VALUE)))
    }

    @Test
    fun passLongArray() = withRustLib {
        assertTrue(natives.testrs.passLongArray(longArrayOf(1, 2)))
    }

    @Test
    fun passULongArray() = withRustLib {
        assertTrue(natives.testrs.passULongArray(ulongArrayOf(1.toULong(), ULong.MAX_VALUE)))
    }

    @Test
    fun passFloatArray() = withRustLib {
        assertTrue(natives.testrs.passFloatArray(floatArrayOf(1.1f, 2.2f)))
    }

    @Test
    fun passDoubleArray() = withRustLib {
        assertTrue(natives.testrs.passDoubleArray(doubleArrayOf(1.1, 2.2)))
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
}