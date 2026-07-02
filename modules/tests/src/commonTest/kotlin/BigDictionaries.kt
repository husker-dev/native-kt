@file:OptIn(ExperimentalUnsignedTypes::class)

import natives.test.MyDictionary
import natives.test.MyEnum
import natives.test.TypeDictionary
import natives.test.VoidCallback
import kotlin.test.*

class Dictionaries {

    @Test
    fun passBigDictionary() = withLib {
        assertTrue(natives.test.passBigDictionary(createTestDictionary {}))
    }

    @Test
    fun returnBigDictionary() = withLib {
        val callback = VoidCallback {}
        assertEquals(createTestDictionary(callback), natives.test.returnBigDictionary(callback))
    }

    @Test
    fun pingBigDictionary() = withLib {
        val callback = VoidCallback {}
        assertEquals(createTestDictionary(callback), natives.test.pingBigDictionary(createTestDictionary(callback)))
    }

    @Test
    fun passBigDictionaryN() = withLib {
        assertTrue(natives.test.passBigDictionaryN(null))
    }

    @Test
    fun returnBigDictionaryN() = withLib {
        assertEquals(null, natives.test.returnBigDictionaryN())
    }

    @Test
    fun pingBigDictionaryN() = withLib {
        assertEquals(null, natives.test.pingBigDictionaryN(null))
    }
}

private fun createTestDictionary(callback: VoidCallback) = TypeDictionary(
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
    arrayOf(MyDictionary(1, 2, 3, 4), MyDictionary(5, 6, 7, 8))
)