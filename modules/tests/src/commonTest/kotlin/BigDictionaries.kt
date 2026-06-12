
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
    123.toShort(),
    123,
    Long.MAX_VALUE,
    123.0f,
    123.4,
    "test string",
    MyEnum.CASE2,
    MyDictionary(1, 2, 3, 4),
    callback,
    charArrayOf('a', 'b'),
    booleanArrayOf(true, false),
    byteArrayOf(1, 2),
    shortArrayOf(1, 2),
    intArrayOf(1, 2),
    longArrayOf(1, 2),
    floatArrayOf(1.2f, 3.4f),
    doubleArrayOf(1.2, 3.4),
    arrayOf("string1", "string2"),
    arrayOf(MyEnum.CASE1, MyEnum.CASE2),
    arrayOf(MyDictionary(1, 2, 3, 4), MyDictionary(5, 6, 7, 8))
)