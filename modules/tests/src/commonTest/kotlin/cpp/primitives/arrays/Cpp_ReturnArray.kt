@file:OptIn(ExperimentalUnsignedTypes::class)

package cpp.primitives.arrays

import natives.testcpp.MyDictionary
import natives.testcpp.MyEnum
import withCppLib
import kotlin.test.Test
import kotlin.test.assertContentEquals

class Cpp_ReturnArray {

    @Test
    fun returnCharArray() = withCppLib {
        assertContentEquals(charArrayOf('a', 'b'), natives.testcpp.returnCharArray())
    }

    @Test
    fun returnCharArrayN() = withCppLib {
        assertContentEquals(null, natives.testcpp.returnCharArrayN())
    }

    @Test
    fun returnBooleanArray() = withCppLib {
        assertContentEquals(booleanArrayOf(true, false), natives.testcpp.returnBooleanArray())
    }

    @Test
    fun returnByteArray() = withCppLib {
        assertContentEquals(byteArrayOf(1, 2), natives.testcpp.returnByteArray())
    }

    @Test
    fun returnUByteArray() = withCppLib {
        assertContentEquals(ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE), natives.testcpp.returnUByteArray())
    }

    @Test
    fun returnShortArray() = withCppLib {
        assertContentEquals(shortArrayOf(1, 2), natives.testcpp.returnShortArray())
    }

    @Test
    fun returnUShortArray() = withCppLib {
        assertContentEquals(ushortArrayOf(1.toUShort(), UShort.MAX_VALUE), natives.testcpp.returnUShortArray())
    }

    @Test
    fun returnIntArray() = withCppLib {
        assertContentEquals(intArrayOf(1, 2), natives.testcpp.returnIntArray())
    }

    @Test
    fun returnUIntArray() = withCppLib {
        assertContentEquals(uintArrayOf(1.toUInt(), UInt.MAX_VALUE), natives.testcpp.returnUIntArray())
    }

    @Test
    fun returnLongArray() = withCppLib {
        assertContentEquals(longArrayOf(1, 2), natives.testcpp.returnLongArray())
    }

    @Test
    fun returnULongArray() = withCppLib {
        assertContentEquals(ulongArrayOf(1.toULong(), ULong.MAX_VALUE), natives.testcpp.returnULongArray())
    }

    @Test
    fun returnFloatArray() = withCppLib {
        assertContentEquals(floatArrayOf(1.1f, 2.2f), natives.testcpp.returnFloatArray())
    }

    @Test
    fun returnDoubleArray() = withCppLib {
        assertContentEquals(doubleArrayOf(1.1, 2.2), natives.testcpp.returnDoubleArray())
    }

    @Test
    fun returnStringArray() = withCppLib {
        assertContentEquals(arrayOf("string1", "string2"), natives.testcpp.returnStringArray())
    }

    @Test
    fun returnStringArrayN() = withCppLib {
        assertContentEquals(arrayOf(null, null), natives.testcpp.returnStringArrayN())
    }

    @Test
    fun returnEnumArray() = withCppLib {
        assertContentEquals(arrayOf(MyEnum.CASE1, MyEnum.CASE2), natives.testcpp.returnEnumArray())
    }

    @Test
    fun returnDictionaryArray() = withCppLib {
        assertContentEquals(
            arrayOf(
                MyDictionary(1, 2, 3, 4),
                MyDictionary(5, 6, 7, 8)
            ), natives.testcpp.returnDictionaryArray()
        )
    }

    @Test
    fun returnDictionaryArrayN() = withCppLib {
        assertContentEquals(arrayOf(null, null), natives.testcpp.returnDictionaryArrayN())
    }
}