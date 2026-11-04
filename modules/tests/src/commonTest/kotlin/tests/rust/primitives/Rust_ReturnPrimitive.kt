package tests.rust.primitives

import withRustLib
import natives.test.rust.MyDictionary
import natives.test.rust.MyEnum
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Rust_ReturnPrimitive {

    @Test
    fun returnVoid() = withRustLib {
        assertEquals(Unit, natives.test.rust.returnVoid())
    }

    @Test
    fun returnChar() = withRustLib {
        assertEquals('a', natives.test.rust.returnChar())
    }

    @Test
    fun returnCharN() = withRustLib {
        assertNull(natives.test.rust.returnCharN())
    }

    @Test
    fun returnBoolean() = withRustLib {
        assertEquals(true, natives.test.rust.returnBoolean())
    }

    @Test
    fun returnBooleanN() = withRustLib {
        assertEquals(true, natives.test.rust.returnBooleanN())
    }

    @Test
    fun returnByte() = withRustLib {
        assertEquals(99.toByte(), natives.test.rust.returnByte())
    }

    @Test
    fun returnByteN() = withRustLib {
        assertNull(natives.test.rust.returnByteN())
    }

    @Test
    fun returnUByte() = withRustLib {
        assertEquals(UByte.MAX_VALUE, natives.test.rust.returnUbyte())
    }

    @Test
    fun returnUByteN() = withRustLib {
        assertEquals(UByte.MAX_VALUE, natives.test.rust.returnUbyteN())
    }

    @Test
    fun returnShort() = withRustLib {
        assertEquals(99.toShort(), natives.test.rust.returnShort())
    }

    @Test
    fun returnShortN() = withRustLib {
        assertNull(natives.test.rust.returnShortN())
    }

    @Test
    fun returnUShort() = withRustLib {
        assertEquals(UShort.MAX_VALUE, natives.test.rust.returnUshort())
    }

    @Test
    fun returnUShortN() = withRustLib {
        assertEquals(UShort.MAX_VALUE, natives.test.rust.returnUshortN())
    }

    @Test
    fun returnInt() = withRustLib {
        assertEquals(99, natives.test.rust.returnInt())
    }

    @Test
    fun returnIntN() = withRustLib {
        assertNull(natives.test.rust.returnIntN())
    }

    @Test
    fun returnUInt() = withRustLib {
        assertEquals(UInt.MAX_VALUE, natives.test.rust.returnUint())
    }

    @Test
    fun returnUIntN() = withRustLib {
        assertEquals(UInt.MAX_VALUE, natives.test.rust.returnUintN())
    }

    @Test
    fun returnLong() = withRustLib {
        assertEquals(9223372036854775805L, natives.test.rust.returnLong())
    }

    @Test
    fun returnLongN() = withRustLib {
        assertNull(natives.test.rust.returnLongN())
    }

    @Test
    fun returnULong() = withRustLib {
        assertEquals(ULong.MAX_VALUE, natives.test.rust.returnUlong())
    }

    @Test
    fun returnULongN() = withRustLib {
        assertEquals(ULong.MAX_VALUE, natives.test.rust.returnUlongN())
    }

    @Test
    fun returnFloat() = withRustLib {
        assertEquals(99f, natives.test.rust.returnFloat())
    }

    @Test
    fun returnFloatN() = withRustLib {
        assertNull(natives.test.rust.returnFloatN())
    }

    @Test
    fun returnDouble() = withRustLib {
        assertEquals(99.0, natives.test.rust.returnDouble())
    }

    @Test
    fun returnDoubleN() = withRustLib {
        assertEquals(99.0, natives.test.rust.returnDoubleN())
    }

    @Test
    fun returnString() = withRustLib {
        assertEquals("test string", natives.test.rust.returnString())
    }

    @Test
    fun returnStringEmpty() = withRustLib {
        assertEquals("", natives.test.rust.returnStringEmpty())
    }

    @Test
    fun returnStringN() = withRustLib {
        assertNull(natives.test.rust.returnStringN())
    }

    @Test
    fun returnEnum() = withRustLib {
        assertEquals(MyEnum.CASE2, natives.test.rust.returnEnum())
    }

    @Test
    fun returnEnumN() = withRustLib {
        assertEquals(MyEnum.CASE2, natives.test.rust.returnEnumN())
    }

    @Test
    fun returnDictionary() = withRustLib {
        assertEquals(MyDictionary(1, 2, 3, 4), natives.test.rust.returnDictionary())
    }

    @Test
    fun returnDictionaryN() = withRustLib {
        assertNull(natives.test.rust.returnDictionaryN())
    }
}
