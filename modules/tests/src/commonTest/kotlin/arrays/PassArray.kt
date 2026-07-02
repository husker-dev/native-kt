@file:OptIn(ExperimentalUnsignedTypes::class)

package arrays

import natives.test.MyDictionary
import natives.test.MyEnum
import withLib
import kotlin.test.Test
import kotlin.test.assertTrue

class PassArray {

    @Test
    fun passCharArray() = withLib {
        assertTrue(natives.test.passCharArray(charArrayOf('a', 'b')))
    }

    @Test
    fun passCharArrayN() = withLib {
        assertTrue(natives.test.passCharArrayN(null))
    }

    @Test
    fun passBooleanArray() = withLib {
        assertTrue(natives.test.passBooleanArray(booleanArrayOf(true, false)))
    }

    @Test
    fun passByteArray() = withLib {
        assertTrue(natives.test.passByteArray(byteArrayOf(1, 2)))
    }

    @Test
    fun passUByteArray() = withLib {
        assertTrue(natives.test.passUByteArray(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)))
    }

    @Test
    fun passShortArray() = withLib {
        assertTrue(natives.test.passShortArray(shortArrayOf(1, 2)))
    }

    @Test
    fun passUShortArray() = withLib {
        assertTrue(natives.test.passUShortArray(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE)))
    }

    @Test
    fun passIntArray() = withLib {
        assertTrue(natives.test.passIntArray(intArrayOf(1, 2)))
    }

    @Test
    fun passUIntArray() = withLib {
        assertTrue(natives.test.passUIntArray(uintArrayOf(1.toUInt(), UInt.MAX_VALUE)))
    }

    @Test
    fun passLongArray() = withLib {
        assertTrue(natives.test.passLongArray(longArrayOf(1, 2)))
    }

    @Test
    fun passULongArray() = withLib {
        assertTrue(natives.test.passULongArray(ulongArrayOf(1.toULong(), ULong.MAX_VALUE)))
    }

    @Test
    fun passFloatArray() = withLib {
        assertTrue(natives.test.passFloatArray(floatArrayOf(1.1f, 2.2f)))
    }

    @Test
    fun passDoubleArray() = withLib {
        assertTrue(natives.test.passDoubleArray(doubleArrayOf(1.1, 2.2)))
    }

    @Test
    fun passStringArray() = withLib {
        assertTrue(natives.test.passStringArray(arrayOf("string1", "string2")))
    }

    @Test
    fun passStringArrayN() = withLib {
        assertTrue(natives.test.passStringArrayN(arrayOf(null, null)))
    }

    @Test
    fun passEnumArray() = withLib {
        assertTrue(natives.test.passEnumArray(arrayOf(MyEnum.CASE1, MyEnum.CASE2)))
    }

    @Test
    fun passDictionaryArray() = withLib {
        assertTrue(natives.test.passDictionaryArray(arrayOf(
            MyDictionary(1, 2, 3, 4),
            MyDictionary(5, 6, 7, 8)
        )))
    }

    @Test
    fun passDictionaryArrayN() = withLib {
        assertTrue(natives.test.passDictionaryArrayN(arrayOf(null, null)))
    }
}