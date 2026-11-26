@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.objc

import natives.test.objc.MyDictionary
import natives.test.objc.MyEnum
import natives.test.objc.MyInterface
import natives.test.objc.TypeDictionary
import natives.test.objc.VoidCallback
import withObjcLib
import kotlin.test.*

class Objc_BigDictionaries {

    @Test
    fun passBigDictionary() = withObjcLib {
        MyInterface().use { inter ->
            val obj = createTestDictionary({}, inter)
            assertTrue(natives.test.objc.passBigDictionary(obj))
        }
    }

    @Test
    fun returnBigDictionary() = withObjcLib {
        val callback = VoidCallback {}
        MyInterface().use { inter ->
            val actual = natives.test.objc.returnBigDictionary(callback, inter)
            try {
                assertEquals(createTestDictionary(callback, inter), actual)
                assertEquals(callback, actual.a19)
                assertEquals(inter, actual.a17)
            } finally {
                actual.a17.close()
                actual.a35.forEach { it.close() }
            }
        }
    }

    @Test
    fun pingBigDictionary() = withObjcLib {
        MyInterface().use { inter ->
            val obj = createTestDictionary({}, inter)
            val actual = natives.test.objc.pingBigDictionary(obj)
            try {
                assertEquals(obj, actual)
            } finally {
                actual.a17.close()
                actual.a35.forEach { it.close() }
            }
        }
    }

    @Test
    fun passBigDictionaryN() = withObjcLib {
        assertTrue(natives.test.objc.passBigDictionaryN(null))
    }

    @Test
    fun returnBigDictionaryN() = withObjcLib {
        assertEquals(null, natives.test.objc.returnBigDictionaryN())
    }

    @Test
    fun pingBigDictionaryN() = withObjcLib {
        assertEquals(null, natives.test.objc.pingBigDictionaryN(null))
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
