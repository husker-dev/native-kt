package rust.primitives

import withRustLib
import natives.testrs.MyDictionary
import natives.testrs.MyEnum
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Rust_ReturnPrimitive {

    @Test
    fun returnVoid() = withRustLib {
        assertEquals(Unit, natives.testrs.returnVoid())
    }

    @Test
    fun returnChar() = withRustLib {
        assertEquals('a', natives.testrs.returnChar())
    }

    @Test
    fun returnBoolean() = withRustLib {
        assertEquals(true, natives.testrs.returnBoolean())
    }

    @Test
    fun returnByte() = withRustLib {
        assertEquals(99.toByte(), natives.testrs.returnByte())
    }

    @Test
    fun returnUByte() = withRustLib {
        assertEquals(UByte.MAX_VALUE, natives.testrs.returnUByte())
    }

    @Test
    fun returnShort() = withRustLib {
        assertEquals(99.toShort(), natives.testrs.returnShort())
    }

    @Test
    fun returnUShort() = withRustLib {
        assertEquals(UShort.MAX_VALUE, natives.testrs.returnUShort())
    }

    @Test
    fun returnInt() = withRustLib {
        assertEquals(99, natives.testrs.returnInt())
    }

    @Test
    fun returnUInt() = withRustLib {
        assertEquals(UInt.MAX_VALUE, natives.testrs.returnUInt())
    }

    @Test
    fun returnLong() = withRustLib {
        assertEquals(9223372036854775805L, natives.testrs.returnLong())
    }

    @Test
    fun returnULong() = withRustLib {
        assertEquals(ULong.MAX_VALUE, natives.testrs.returnULong())
    }

    @Test
    fun returnFloat() = withRustLib {
        assertEquals(99f, natives.testrs.returnFloat())
    }

    @Test
    fun returnDouble() = withRustLib {
        assertEquals(99.0, natives.testrs.returnDouble())
    }

    @Test
    fun returnString() = withRustLib {
        assertEquals("test string", natives.testrs.returnString())
    }

    @Test
    fun returnStringN() = withRustLib {
        assertNull(natives.testrs.returnStringN())
    }

    @Test
    fun returnEnum() = withRustLib {
        assertEquals(MyEnum.CASE2, natives.testrs.returnEnum())
    }

    @Test
    fun returnDictionary() = withRustLib {
        assertEquals(MyDictionary(1, 2, 3, 4), natives.testrs.returnDictionary())
    }

    @Test
    fun returnDictionaryN() = withRustLib {
        assertNull(natives.testrs.returnDictionaryN())
    }
}