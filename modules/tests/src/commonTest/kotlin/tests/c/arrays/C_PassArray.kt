@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.c.arrays

import withCLib
import natives.test.MyDictionary
import natives.test.MyEnum
import natives.test.MyInterface
import kotlin.test.Test
import kotlin.test.assertTrue

class C_PassArray {

    @Test
    fun passCharArray() = withCLib {
        assertTrue(natives.test.passCharArray(charArrayOf('a', 'b')))
    }

    @Test
    fun passCharArrayEmpty() = withCLib {
        assertTrue(natives.test.passCharArrayEmpty(charArrayOf()))
    }

    @Test
    fun passCharArrayN() = withCLib {
        assertTrue(natives.test.passCharArrayN(null))
    }

    @Test
    fun passCharNArray() = withCLib {
        assertTrue(natives.test.passCharNArray(arrayOf(null, 'a')))
    }

    @Test
    fun passBooleanArray() = withCLib {
        assertTrue(natives.test.passBooleanArray(booleanArrayOf(true, false)))
    }

    @Test
    fun passBooleanNArray() = withCLib {
        assertTrue(natives.test.passBooleanNArray(arrayOf(null, true)))
    }

    @Test
    fun passByteArray() = withCLib {
        assertTrue(natives.test.passByteArray(byteArrayOf(1, 2)))
    }

    @Test
    fun passByteNArray() = withCLib {
        assertTrue(natives.test.passByteNArray(arrayOf(null, 1.toByte())))
    }

    @Test
    fun passUByteArray() = withCLib {
        assertTrue(natives.test.passUByteArray(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)))
    }

    @Test
    fun passUByteNArray() = withCLib {
        assertTrue(natives.test.passUByteNArray(arrayOf(null, UByte.MAX_VALUE)))
    }

    @Test
    fun passShortArray() = withCLib {
        assertTrue(natives.test.passShortArray(shortArrayOf(1, 2)))
    }

    @Test
    fun passShortNArray() = withCLib {
        assertTrue(natives.test.passShortNArray(arrayOf(null, 1.toShort())))
    }

    @Test
    fun passUShortArray() = withCLib {
        assertTrue(natives.test.passUShortArray(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE)))
    }

    @Test
    fun passUShortNArray() = withCLib {
        assertTrue(natives.test.passUShortNArray(arrayOf(null, UShort.MAX_VALUE)))
    }

    @Test
    fun passIntArray() = withCLib {
        assertTrue(natives.test.passIntArray(intArrayOf(1, 2)))
    }

    @Test
    fun passIntNArray() = withCLib {
        assertTrue(natives.test.passIntNArray(arrayOf(null, 1)))
    }

    @Test
    fun passUIntArray() = withCLib {
        assertTrue(natives.test.passUIntArray(uintArrayOf(1.toUInt(), UInt.MAX_VALUE)))
    }

    @Test
    fun passUIntNArray() = withCLib {
        assertTrue(natives.test.passUIntNArray(arrayOf(null, UInt.MAX_VALUE)))
    }

    @Test
    fun passLongArray() = withCLib {
        assertTrue(natives.test.passLongArray(longArrayOf(1, 2)))
    }

    @Test
    fun passLongNArray() = withCLib {
        assertTrue(natives.test.passLongNArray(arrayOf(null, 1L)))
    }

    @Test
    fun passULongArray() = withCLib {
        assertTrue(natives.test.passULongArray(ulongArrayOf(1.toULong(), ULong.MAX_VALUE)))
    }

    @Test
    fun passULongNArray() = withCLib {
        assertTrue(natives.test.passULongNArray(arrayOf(null, ULong.MAX_VALUE)))
    }

    @Test
    fun passFloatArray() = withCLib {
        assertTrue(natives.test.passFloatArray(floatArrayOf(1.1f, 2.2f)))
    }

    @Test
    fun passFloatNArray() = withCLib {
        assertTrue(natives.test.passFloatNArray(arrayOf(null, 1.1f)))
    }

    @Test
    fun passDoubleArray() = withCLib {
        assertTrue(natives.test.passDoubleArray(doubleArrayOf(1.1, 2.2)))
    }

    @Test
    fun passDoubleNArray() = withCLib {
        assertTrue(natives.test.passDoubleNArray(arrayOf(null, 1.1)))
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
    fun passEnumNArray() = withCLib {
        assertTrue(natives.test.passEnumNArray(arrayOf(null, MyEnum.CASE2)))
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

    @Test
    fun passInterfaceArray() = withCLib {
        val array = arrayOf(
            MyInterface(),
            MyInterface()
        )
        assertTrue(natives.test.passInterfaceArray(array))
        array.forEach { it.close() }
    }

    @Test
    fun passInterfaceArrayN() = withCLib {
        assertTrue(natives.test.passInterfaceArrayN(arrayOf(null, null)))
    }
}