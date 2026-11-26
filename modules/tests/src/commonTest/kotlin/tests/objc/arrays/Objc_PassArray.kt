@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.objc.arrays

import natives.test.objc.MyDictionary
import natives.test.objc.MyEnum
import natives.test.objc.MyInterface
import withObjcLib
import kotlin.test.Test
import kotlin.test.assertTrue

class Objc_PassArray {

    @Test
    fun passCharArray() = withObjcLib {
        assertTrue(natives.test.objc.passCharArray(charArrayOf('a', 'b')))
    }

    @Test
    fun passCharArrayEmpty() = withObjcLib {
        assertTrue(natives.test.objc.passCharArrayEmpty(charArrayOf()))
    }

    @Test
    fun passCharArrayN() = withObjcLib {
        assertTrue(natives.test.objc.passCharArrayN(null))
    }

    @Test
    fun passCharNArray() = withObjcLib {
        assertTrue(natives.test.objc.passCharNArray(arrayOf(null, 'a')))
    }

    @Test
    fun passBooleanArray() = withObjcLib {
        assertTrue(natives.test.objc.passBooleanArray(booleanArrayOf(true, false)))
    }

    @Test
    fun passBooleanNArray() = withObjcLib {
        assertTrue(natives.test.objc.passBooleanNArray(arrayOf(null, true)))
    }

    @Test
    fun passByteArray() = withObjcLib {
        assertTrue(natives.test.objc.passByteArray(byteArrayOf(1, 2)))
    }

    @Test
    fun passByteNArray() = withObjcLib {
        assertTrue(natives.test.objc.passByteNArray(arrayOf(null, 1.toByte())))
    }

    @Test
    fun passUByteArray() = withObjcLib {
        assertTrue(natives.test.objc.passUbyteArray(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE)))
    }

    @Test
    fun passUByteNArray() = withObjcLib {
        assertTrue(natives.test.objc.passUbyteNArray(arrayOf(null, UByte.MAX_VALUE)))
    }

    @Test
    fun passShortArray() = withObjcLib {
        assertTrue(natives.test.objc.passShortArray(shortArrayOf(1, 2)))
    }

    @Test
    fun passShortNArray() = withObjcLib {
        assertTrue(natives.test.objc.passShortNArray(arrayOf(null, 1.toShort())))
    }

    @Test
    fun passUShortArray() = withObjcLib {
        assertTrue(natives.test.objc.passUshortArray(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE)))
    }

    @Test
    fun passUShortNArray() = withObjcLib {
        assertTrue(natives.test.objc.passUshortNArray(arrayOf(null, UShort.MAX_VALUE)))
    }

    @Test
    fun passIntArray() = withObjcLib {
        assertTrue(natives.test.objc.passIntArray(intArrayOf(1, 2)))
    }

    @Test
    fun passIntNArray() = withObjcLib {
        assertTrue(natives.test.objc.passIntNArray(arrayOf(null, 1)))
    }

    @Test
    fun passUIntArray() = withObjcLib {
        assertTrue(natives.test.objc.passUintArray(uintArrayOf(1.toUInt(), UInt.MAX_VALUE)))
    }

    @Test
    fun passUIntNArray() = withObjcLib {
        assertTrue(natives.test.objc.passUintNArray(arrayOf(null, UInt.MAX_VALUE)))
    }

    @Test
    fun passLongArray() = withObjcLib {
        assertTrue(natives.test.objc.passLongArray(longArrayOf(1, 2)))
    }

    @Test
    fun passLongNArray() = withObjcLib {
        assertTrue(natives.test.objc.passLongNArray(arrayOf(null, 1L)))
    }

    @Test
    fun passULongArray() = withObjcLib {
        assertTrue(natives.test.objc.passUlongArray(ulongArrayOf(1.toULong(), ULong.MAX_VALUE)))
    }

    @Test
    fun passULongNArray() = withObjcLib {
        assertTrue(natives.test.objc.passUlongNArray(arrayOf(null, ULong.MAX_VALUE)))
    }

    @Test
    fun passFloatArray() = withObjcLib {
        assertTrue(natives.test.objc.passFloatArray(floatArrayOf(1.1f, 2.2f)))
    }

    @Test
    fun passFloatNArray() = withObjcLib {
        assertTrue(natives.test.objc.passFloatNArray(arrayOf(null, 1.1f)))
    }

    @Test
    fun passDoubleArray() = withObjcLib {
        assertTrue(natives.test.objc.passDoubleArray(doubleArrayOf(1.1, 2.2)))
    }

    @Test
    fun passDoubleNArray() = withObjcLib {
        assertTrue(natives.test.objc.passDoubleNArray(arrayOf(null, 1.1)))
    }

    @Test
    fun passStringArray() = withObjcLib {
        assertTrue(natives.test.objc.passStringArray(arrayOf("string1", "string2")))
    }

    @Test
    fun passStringArrayN() = withObjcLib {
        assertTrue(natives.test.objc.passStringArrayN(arrayOf(null, null)))
    }

    @Test
    fun passEnumArray() = withObjcLib {
        assertTrue(natives.test.objc.passEnumArray(arrayOf(MyEnum.CASE1, MyEnum.CASE2)))
    }

    @Test
    fun passEnumNArray() = withObjcLib {
        assertTrue(natives.test.objc.passEnumNArray(arrayOf(null, MyEnum.CASE2)))
    }

    @Test
    fun passDictionaryArray() = withObjcLib {
        assertTrue(
            natives.test.objc.passDictionaryArray(
                arrayOf(
                    MyDictionary(1, 2, 3, 4),
                    MyDictionary(5, 6, 7, 8)
                )
            )
        )
    }

    @Test
    fun passDictionaryArrayN() = withObjcLib {
        assertTrue(natives.test.objc.passDictionaryArrayN(arrayOf(null, null)))
    }

    @Test
    fun passInterfaceArray() = withObjcLib {
        val elements = arrayOf(
            MyInterface(),
            MyInterface()
        )
        assertTrue(natives.test.objc.passInterfaceArray(elements))
        elements.forEach { it.close() }
    }

    @Test
    fun passInterfaceArrayRepeated() = withObjcLib {
        MyInterface().use { obj ->
            assertTrue(natives.test.objc.passInterfaceArray(arrayOf(obj, obj)))
        }
    }

    @Test
    fun passInterfaceArrayN() = withObjcLib {
        assertTrue(natives.test.objc.passInterfaceArrayN(arrayOf(null, null)))
    }
}
