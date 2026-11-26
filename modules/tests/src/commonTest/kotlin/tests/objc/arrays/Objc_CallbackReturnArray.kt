@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.objc.arrays

import natives.test.objc.MyDictionary
import natives.test.objc.MyEnum
import natives.test.objc.MyInterface
import withObjcLib
import kotlin.test.Test
import kotlin.test.assertTrue

class Objc_CallbackReturnArray {

    @Test
    fun callbackReturnCharArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnCharArray { charArrayOf('a', 'b') })
    }

    @Test
    fun callbackReturnCharArrayEmpty() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnCharArrayEmpty { charArrayOf() })
    }

    @Test
    fun callbackReturnCharArrayN() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnCharArrayN { null })
    }

    @Test
    fun callbackReturnBooleanArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnBooleanArray { booleanArrayOf(true, false) })
    }

    @Test
    fun callbackReturnByteArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnByteArray { byteArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUByteArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnUbyteArray { ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE) })
    }

    @Test
    fun callbackReturnShortArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnShortArray { shortArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUShortArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnUshortArray { ushortArrayOf(1.toUShort(), UShort.MAX_VALUE) })
    }

    @Test
    fun callbackReturnIntArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnIntArray { intArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUIntArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnUintArray { uintArrayOf(1.toUInt(), UInt.MAX_VALUE) })
    }

    @Test
    fun callbackReturnLongArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnLongArray { longArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnULongArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnUlongArray { ulongArrayOf(1.toULong(), ULong.MAX_VALUE) })
    }

    @Test
    fun callbackReturnFloatArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnFloatArray { floatArrayOf(1.1f, 2.2f) })
    }

    @Test
    fun callbackReturnDoubleArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnDoubleArray { doubleArrayOf(1.1, 2.2) })
    }

    @Test
    fun callbackReturnStringArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnStringArray { arrayOf("string1", "string2") })
    }

    @Test
    fun callbackReturnStringArrayN() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnStringArrayN { arrayOf(null, null) })
    }

    @Test
    fun callbackReturnEnumArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnEnumArray { arrayOf(MyEnum.CASE1, MyEnum.CASE2) })
    }

    @Test
    fun callbackReturnDictionaryArray() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnDictionaryArray {
            arrayOf(
                MyDictionary(1, 2, 3, 4),
                MyDictionary(5, 6, 7, 8)
            )
        })
    }

    @Test
    fun callbackReturnDictionaryArrayN() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnDictionaryArrayN {
            arrayOf(null, null)
        })
    }

    @Test
    fun callbackReturnInterfaceArray() = withObjcLib {
        val array = arrayOf(
            MyInterface(),
            MyInterface()
        )
        assertTrue(natives.test.objc.callbackReturnInterfaceArray {
            array
        })
        array.forEach { it.close() }
    }

    @Test
    fun callbackReturnInterfaceArrayN() = withObjcLib {
        assertTrue(natives.test.objc.callbackReturnInterfaceArrayN {
            arrayOf(null, null)
        })
    }
}
