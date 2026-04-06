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
    fun passBooleanArray() = withLib {
        assertTrue(natives.test.passBooleanArray(booleanArrayOf(true, false)))
    }

    @Test
    fun passByteArray() = withLib {
        assertTrue(natives.test.passByteArray(byteArrayOf(1, 2)))
    }

    @Test
    fun passShortArray() = withLib {
        assertTrue(natives.test.passShortArray(shortArrayOf(1, 2)))
    }

    @Test
    fun passIntArray() = withLib {
        assertTrue(natives.test.passIntArray(intArrayOf(1, 2)))
    }

    @Test
    fun passLongArray() = withLib {
        assertTrue(natives.test.passLongArray(longArrayOf(1, 2)))
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
}