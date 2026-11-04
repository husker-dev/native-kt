@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.rust

import natives.test.rust.MyDictionary
import natives.test.rust.MyEnum
import natives.test.rust.MyInterface
import natives.test.rust.TypeDictionary
import natives.test.rust.VoidCallback
import withRustLib
import kotlin.test.*

class Rust_BigDictionaries {

    @Test
    fun passBigDictionary() = withRustLib {
        val obj = createTestDictionary(
            {},
            MyInterface()
        )
        assertTrue(natives.test.rust.passBigDictionary(obj))
    }

    @Test
    fun returnBigDictionary() = withRustLib {
        val callback = VoidCallback {}
        val inter = MyInterface()
        assertEquals(
            createTestDictionary(callback, inter),
            natives.test.rust.returnBigDictionary(callback, inter)
        )
    }

    @Test
    fun pingBigDictionary() = withRustLib {
        val obj = createTestDictionary(
            {},
            MyInterface()
        )
        assertEquals(obj, natives.test.rust.pingBigDictionary(obj))
    }

    @Test
    fun passBigDictionaryN() = withRustLib {
        assertTrue(natives.test.rust.passBigDictionaryN(null))
    }

    @Test
    fun returnBigDictionaryN() = withRustLib {
        assertEquals(null, natives.test.rust.returnBigDictionaryN())
    }

    @Test
    fun pingBigDictionaryN() = withRustLib {
        assertEquals(null, natives.test.rust.pingBigDictionaryN(null))
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