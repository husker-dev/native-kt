@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.swift

import natives.test.swift.MyDictionary
import natives.test.swift.MyEnum
import natives.test.swift.MyInterface
import natives.test.swift.TypeDictionary
import natives.test.swift.VoidCallback
import withSwiftLib
import kotlin.test.*

class Swift_BigDictionaries {

    @Test
    fun passBigDictionary() = withSwiftLib {
        val obj = createTestDictionary(
            {},
            MyInterface()
        )
        assertTrue(natives.test.swift.passBigDictionary(obj))
    }

    @Test
    fun returnBigDictionary() = withSwiftLib {
        val callback = VoidCallback {}
        val inter = MyInterface()
        assertEquals(
            createTestDictionary(callback, inter),
            natives.test.swift.returnBigDictionary(callback, inter)
        )
    }

    @Test
    fun pingBigDictionary() = withSwiftLib {
        val obj = createTestDictionary(
            {},
            MyInterface()
        )
        assertEquals(obj, natives.test.swift.pingBigDictionary(obj))
    }

    @Test
    fun passBigDictionaryN() = withSwiftLib {
        assertTrue(natives.test.swift.passBigDictionaryN(null))
    }

    @Test
    fun returnBigDictionaryN() = withSwiftLib {
        assertEquals(null, natives.test.swift.returnBigDictionaryN())
    }

    @Test
    fun pingBigDictionaryN() = withSwiftLib {
        assertEquals(null, natives.test.swift.pingBigDictionaryN(null))
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