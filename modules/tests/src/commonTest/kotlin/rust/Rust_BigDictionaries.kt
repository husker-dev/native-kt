@file:OptIn(ExperimentalUnsignedTypes::class)

package rust

import natives.testrs.MyDictionary
import natives.testrs.MyEnum
import natives.testrs.TypeDictionary
import natives.testrs.VoidCallback
import withRustLib
import kotlin.test.*

class Rust_BigDictionaries {

    @Test
    fun passBigDictionary() = withRustLib {
        assertTrue(natives.testrs.passBigDictionary(createTestDictionary {}))
    }

    @Test
    fun returnBigDictionary() = withRustLib {
        val callback = VoidCallback {}
        assertEquals(createTestDictionary(callback), natives.testrs.returnBigDictionary(callback))
    }

    @Test
    fun pingBigDictionary() = withRustLib {
        val callback = VoidCallback {}
        assertEquals(createTestDictionary(callback), natives.testrs.pingBigDictionary(createTestDictionary(callback)))
    }

    @Test
    fun passBigDictionaryN() = withRustLib {
        assertTrue(natives.testrs.passBigDictionaryN(null))
    }

    @Test
    fun returnBigDictionaryN() = withRustLib {
        assertEquals(null, natives.testrs.returnBigDictionaryN())
    }

    @Test
    fun pingBigDictionaryN() = withRustLib {
        assertEquals(null, natives.testrs.pingBigDictionaryN(null))
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