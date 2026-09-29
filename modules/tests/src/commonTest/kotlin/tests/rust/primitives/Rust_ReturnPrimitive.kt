package tests.rust.primitives

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
    fun returnCharN() = withRustLib {
        assertNull(natives.testrs.returnCharN())
    }

    @Test
    fun returnBoolean() = withRustLib {
        assertEquals(true, natives.testrs.returnBoolean())
    }

    @Test
    fun returnBooleanN() = withRustLib {
        assertEquals(true, natives.testrs.returnBooleanN())
    }

    @Test
    fun returnByte() = withRustLib {
        assertEquals(99.toByte(), natives.testrs.returnByte())
    }

    @Test
    fun returnByteN() = withRustLib {
        assertNull(natives.testrs.returnByteN())
    }

    @Test
    fun returnUByte() = withRustLib {
        assertEquals(UByte.MAX_VALUE, natives.testrs.returnUByte())
    }

    @Test
    fun returnUByteN() = withRustLib {
        assertEquals(UByte.MAX_VALUE, natives.testrs.returnUByteN())
    }

    @Test
    fun returnShort() = withRustLib {
        assertEquals(99.toShort(), natives.testrs.returnShort())
    }

    @Test
    fun returnShortN() = withRustLib {
        assertNull(natives.testrs.returnShortN())
    }

    @Test
    fun returnUShort() = withRustLib {
        assertEquals(UShort.MAX_VALUE, natives.testrs.returnUShort())
    }

    @Test
    fun returnUShortN() = withRustLib {
        assertEquals(UShort.MAX_VALUE, natives.testrs.returnUShortN())
    }

    @Test
    fun returnInt() = withRustLib {
        assertEquals(99, natives.testrs.returnInt())
    }

    @Test
    fun returnIntN() = withRustLib {
        assertNull(natives.testrs.returnIntN())
    }

    @Test
    fun returnUInt() = withRustLib {
        assertEquals(UInt.MAX_VALUE, natives.testrs.returnUInt())
    }

    @Test
    fun returnUIntN() = withRustLib {
        assertEquals(UInt.MAX_VALUE, natives.testrs.returnUIntN())
    }

    @Test
    fun returnLong() = withRustLib {
        assertEquals(9223372036854775805L, natives.testrs.returnLong())
    }

    @Test
    fun returnLongN() = withRustLib {
        assertNull(natives.testrs.returnLongN())
    }

    @Test
    fun returnULong() = withRustLib {
        assertEquals(ULong.MAX_VALUE, natives.testrs.returnULong())
    }

    @Test
    fun returnULongN() = withRustLib {
        assertEquals(ULong.MAX_VALUE, natives.testrs.returnULongN())
    }

    @Test
    fun returnFloat() = withRustLib {
        assertEquals(99f, natives.testrs.returnFloat())
    }

    @Test
    fun returnFloatN() = withRustLib {
        assertNull(natives.testrs.returnFloatN())
    }

    @Test
    fun returnDouble() = withRustLib {
        assertEquals(99.0, natives.testrs.returnDouble())
    }

    @Test
    fun returnDoubleN() = withRustLib {
        assertEquals(99.0, natives.testrs.returnDoubleN())
    }

    @Test
    fun returnString() = withRustLib {
        assertEquals("test string", natives.testrs.returnString())
    }

    @Test
    fun returnStringEmpty() = withRustLib {
        assertEquals("", natives.testrs.returnStringEmpty())
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
    fun returnEnumN() = withRustLib {
        assertEquals(MyEnum.CASE2, natives.testrs.returnEnumN())
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
