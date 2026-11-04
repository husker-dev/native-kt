@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.swift.arrays

import withSwiftLib
import natives.test.swift.MyDictionary
import natives.test.swift.MyEnum
import natives.test.swift.MyInterface
import kotlin.test.Test
import kotlin.test.assertTrue

class Swift_CallbackReturnArray {

    @Test
    fun callbackReturnCharArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnCharArray { charArrayOf('a', 'b') })
    }

    @Test
    fun callbackReturnCharArrayEmpty() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnCharArrayEmpty { charArrayOf() })
    }

    @Test
    fun callbackReturnCharArrayN() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnCharArrayN { null })
    }

    @Test
    fun callbackReturnBooleanArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnBooleanArray { booleanArrayOf(true, false) })
    }

    @Test
    fun callbackReturnByteArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnByteArray { byteArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUByteArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnUbyteArray { ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE) })
    }

    @Test
    fun callbackReturnShortArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnShortArray { shortArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUShortArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnUshortArray { ushortArrayOf(1.toUShort(), UShort.MAX_VALUE) })
    }

    @Test
    fun callbackReturnIntArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnIntArray { intArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnUIntArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnUintArray { uintArrayOf(1.toUInt(), UInt.MAX_VALUE) })
    }

    @Test
    fun callbackReturnLongArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnLongArray { longArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnULongArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnUlongArray { ulongArrayOf(1.toULong(), ULong.MAX_VALUE) })
    }

    @Test
    fun callbackReturnFloatArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnFloatArray { floatArrayOf(1.1f, 2.2f) })
    }

    @Test
    fun callbackReturnDoubleArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnDoubleArray { doubleArrayOf(1.1, 2.2) })
    }

    @Test
    fun callbackReturnStringArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnStringArray { arrayOf("string1", "string2") })
    }

    @Test
    fun callbackReturnStringArrayN() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnStringArrayN { arrayOf(null, null) })
    }

    @Test
    fun callbackReturnEnumArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnEnumArray { arrayOf(MyEnum.CASE1, MyEnum.CASE2) })
    }

    @Test
    fun callbackReturnDictionaryArray() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnDictionaryArray {
            arrayOf(
                MyDictionary(1, 2, 3, 4),
                MyDictionary(5, 6, 7, 8)
            )
        })
    }

    @Test
    fun callbackReturnDictionaryArrayN() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnDictionaryArrayN {
            arrayOf(null, null)
        })
    }

    @Test
    fun callbackReturnInterfaceArray() = withSwiftLib {
        val elements = arrayOf(
            MyInterface(),
            MyInterface()
        )
        assertTrue(natives.test.swift.callbackReturnInterfaceArray {
            elements
        })
        elements.forEach { it.close() }
    }

    @Test
    fun callbackReturnInterfaceArrayN() = withSwiftLib {
        assertTrue(natives.test.swift.callbackReturnInterfaceArrayN {
            arrayOf(null, null)
        })
    }
}