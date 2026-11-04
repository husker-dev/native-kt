@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.cpp

import natives.test.cpp.MyDictionary
import natives.test.cpp.MyEnum
import natives.test.cpp.MyInterface
import natives.test.cpp.TypeDictionary
import natives.test.cpp.VoidCallback
import withCppLib
import kotlin.test.*

class Cpp_BigDictionaries {

    @Test
    fun passBigDictionary() = withCppLib {
        val obj = createTestDictionary(
            {},
            MyInterface()
        )
        assertTrue(natives.test.cpp.passBigDictionary(obj))
    }

    @Test
    fun returnBigDictionary() = withCppLib {
        val callback = VoidCallback {}
        val inter = MyInterface()
        assertEquals(
            createTestDictionary(callback, inter),
            natives.test.cpp.returnBigDictionary(callback, inter)
        )
    }

    @Test
    fun pingBigDictionary() = withCppLib {
        val obj = createTestDictionary(
            {},
            MyInterface()
        )
        assertEquals(obj, natives.test.cpp.pingBigDictionary(obj))
    }

    @Test
    fun passBigDictionaryN() = withCppLib {
        assertTrue(natives.test.cpp.passBigDictionaryN(null))
    }

    @Test
    fun returnBigDictionaryN() = withCppLib {
        assertEquals(null, natives.test.cpp.returnBigDictionaryN())
    }

    @Test
    fun pingBigDictionaryN() = withCppLib {
        assertEquals(null, natives.test.cpp.pingBigDictionaryN(null))
    }
}

private fun createTestDictionary(
    callback: VoidCallback,
    inter: MyInterface
) = TypeDictionary(
    'a',
    true,
    123.toByte(),
    123.toUByte(),
    123.toShort(),
    123.toUShort(),
    123,
    123.toUInt(),
    Long.MAX_VALUE,
    Long.MAX_VALUE.toULong(),
    123.0f,
    123.4,
    "test string",
    MyEnum.CASE2,
    MyDictionary(1, 2, 3, 4),
    null,
    inter,
    null,
    callback,
    charArrayOf('a', 'b'),
    booleanArrayOf(true, false),
    byteArrayOf(1, 2),
    ubyteArrayOf(1.toUByte(), 2.toUByte()),
    shortArrayOf(1, 2),
    ushortArrayOf(1.toUShort(), 2.toUShort()),
    intArrayOf(1, 2),
    uintArrayOf(1.toUInt(), 2.toUInt()),
    longArrayOf(1, 2),
    ulongArrayOf(1.toULong(), 2.toULong()),
    floatArrayOf(1.2f, 3.4f),
    doubleArrayOf(1.2, 3.4),
    arrayOf("string1", "string2"),
    arrayOf(MyEnum.CASE1, MyEnum.CASE2),
    arrayOf(MyDictionary(1, 2, 3, 4), MyDictionary(5, 6, 7, 8)),
    arrayOf(inter, inter)
)