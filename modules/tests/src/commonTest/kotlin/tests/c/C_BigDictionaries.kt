@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.c

import natives.test.c.MyDictionary
import natives.test.c.MyEnum
import natives.test.c.MyInterface
import natives.test.c.TypeDictionary
import natives.test.c.VoidCallback
import withCLib
import kotlin.test.*

class C_BigDictionaries {

    @Test
    fun passBigDictionary() = withCLib {
        val obj = createTestDictionary(
            {},
            MyInterface()
        )
        assertTrue(natives.test.c.passBigDictionary(obj))
    }

    @Test
    fun returnBigDictionary() = withCLib {
        val callback = VoidCallback {}
        val inter = MyInterface()
        assertEquals(
            createTestDictionary(callback, inter),
            natives.test.c.returnBigDictionary(callback, inter)
        )
    }

    @Test
    fun pingBigDictionary() = withCLib {
        val obj = createTestDictionary(
            {},
            MyInterface()
        )
        assertEquals(obj, natives.test.c.pingBigDictionary(obj))
    }

    @Test
    fun passBigDictionaryN() = withCLib {
        assertTrue(natives.test.c.passBigDictionaryN(null))
    }

    @Test
    fun returnBigDictionaryN() = withCLib {
        assertEquals(null, natives.test.c.returnBigDictionaryN())
    }

    @Test
    fun pingBigDictionaryN() = withCLib {
        assertEquals(null, natives.test.c.pingBigDictionaryN(null))
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