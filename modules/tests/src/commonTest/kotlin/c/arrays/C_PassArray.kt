@file:OptIn(ExperimentalUnsignedTypes::class)

package c.arrays

import withCLib
import natives.test.MyDictionary
import natives.test.MyEnum
import kotlin.test.Test
import kotlin.test.assertTrue

class C_PassArray {

    @Test
    fun passCharArray() = withCLib {
        assertTrue(natives.test.passCharArray(charArrayOf('a', 'b')))
    }

    @Test
    fun passCharArrayN() = withCLib {
        assertTrue(natives.test.passCharArrayN(null))
    }

    @Test
    fun passBooleanArray() = withCLib {
        assertTrue(natives.test.passBooleanArray(booleanArrayOf(true, false)))
    }

    @Test
    fun passByteArray() = withCLib {
        assertTrue(natives.test.passByteArray(byteArrayOf(1, 2)))
    }

    @Test
    fun passUByteArray() = withCLib {
        assertTrue(natives.test.passUByteArray(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)))
    }

    @Test
    fun passShortArray() = withCLib {
        assertTrue(natives.test.passShortArray(shortArrayOf(1, 2)))
    }

    @Test
    fun passUShortArray() = withCLib {
        assertTrue(natives.test.passUShortArray(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE)))
    }

    @Test
    fun passIntArray() = withCLib {
        assertTrue(natives.test.passIntArray(intArrayOf(1, 2)))
    }

    @Test
    fun passUIntArray() = withCLib {
        assertTrue(natives.test.passUIntArray(uintArrayOf(1.toUInt(), UInt.MAX_VALUE)))
    }

    @Test
    fun passLongArray() = withCLib {
        assertTrue(natives.test.passLongArray(longArrayOf(1, 2)))
    }

    @Test
    fun passULongArray() = withCLib {
        assertTrue(natives.test.passULongArray(ulongArrayOf(1.toULong(), ULong.MAX_VALUE)))
    }

    @Test
    fun passFloatArray() = withCLib {
        assertTrue(natives.test.passFloatArray(floatArrayOf(1.1f, 2.2f)))
    }

    @Test
    fun passDoubleArray() = withCLib {
        assertTrue(natives.test.passDoubleArray(doubleArrayOf(1.1, 2.2)))
    }

    @Test
    fun passStringArray() = withCLib {
        assertTrue(natives.test.passStringArray(arrayOf("string1", "string2")))
    }

    @Test
    fun passStringArrayN() = withCLib {
        assertTrue(natives.test.passStringArrayN(arrayOf(null, null)))
    }

    @Test
    fun passEnumArray() = withCLib {
        assertTrue(natives.test.passEnumArray(arrayOf(MyEnum.CASE1, MyEnum.CASE2)))
    }

    @Test
    fun passDictionaryArray() = withCLib {
        assertTrue(
            natives.test.passDictionaryArray(
                arrayOf(
                    MyDictionary(1, 2, 3, 4),
                    MyDictionary(5, 6, 7, 8)
                )
            )
        )
    }

    @Test
    fun passDictionaryArrayN() = withCLib {
        assertTrue(natives.test.passDictionaryArrayN(arrayOf(null, null)))
    }
}