@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.rust

import natives.test.rust.MyEnum
import natives.test.rust.MyInterface
import withRustLib
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class Rust_Critical {

    @Test
    fun criticalPrimitives() = withRustLib {
        assertTrue(natives.test.rust.criticalPrimitives(
            'a', true,
            1, UByte.MAX_VALUE,
            3, UShort.MAX_VALUE,
            5, UInt.MAX_VALUE,
            7, ULong.MAX_VALUE,
            1f, 2.0
        ))
    }

    @Test
    fun criticalEnum() = withRustLib {
        assertTrue(natives.test.rust.criticalEnum(MyEnum.CASE1))
    }

    @Test
    fun criticalString() = withRustLib {
        assertTrue(natives.test.rust.criticalString("test string"))
    }

    @Test
    fun criticalStringEmpty() = withRustLib {
        assertTrue(natives.test.rust.criticalStringEmpty(""))
    }

    @Test
    fun criticalStringN() = withRustLib {
        assertTrue(natives.test.rust.criticalStringN(null))
    }

    @Test
    fun criticalInterface() = withRustLib {
        assertTrue(natives.test.rust.criticalInterface(MyInterface()))
    }

    @Test
    fun criticalInterfaceN() = withRustLib {
        assertTrue(natives.test.rust.criticalInterfaceN(null))
    }

    @Test
    fun criticalPrimitivesArray() = withRustLib {
        assertTrue(natives.test.rust.criticalPrimitivesArray(
            charArrayOf('a', 'b'),
            booleanArrayOf(true, false),
            byteArrayOf(1, 2),
            ubyteArrayOf(1.toUByte(), UByte.MAX_VALUE),
            shortArrayOf(1, 2),
            ushortArrayOf(1.toUShort(), UShort.MAX_VALUE),
            intArrayOf(1, 2),
            uintArrayOf(1.toUInt(), UInt.MAX_VALUE),
            longArrayOf(1, 2),
            ulongArrayOf(1.toULong(), ULong.MAX_VALUE),
            floatArrayOf(1.1f, 2.2f),
            doubleArrayOf(1.1, 2.2)
        ))
    }

    @Test
    fun criticalEnumArray() = withRustLib {
        assertTrue(natives.test.rust.criticalEnumArray(
            arrayOf(MyEnum.CASE1, MyEnum.CASE2)
        ))
    }

    @Test
    fun criticalPrimitivesArrayEmpty() = withRustLib {
        assertTrue(natives.test.rust.criticalPrimitivesArrayEmpty(
            charArrayOf(),
            booleanArrayOf(),
            byteArrayOf(),
            ubyteArrayOf(),
            shortArrayOf(),
            ushortArrayOf(),
            intArrayOf(),
            uintArrayOf(),
            longArrayOf(),
            ulongArrayOf(),
            floatArrayOf(),
            doubleArrayOf()
        ))
    }

    @Test
    fun criticalEnumArrayEmpty() = withRustLib {
        assertTrue(natives.test.rust.criticalEnumArrayEmpty(
            arrayOf()
        ))
    }

    @Test
    fun criticalPrimitivesArrayN() = withRustLib {
        assertTrue(natives.test.rust.criticalPrimitivesArrayN(
            null, null,
            null, null,
            null, null,
            null, null,
            null, null,
            null, null
        ))
    }

    @Test
    fun criticalEnumArrayN() = withRustLib {
        assertTrue(natives.test.rust.criticalEnumArrayN(null))
    }

    @Test
    fun criticalReturnChar() = withRustLib {
        assertEquals('a', natives.test.rust.criticalReturnChar())
    }

    @Test
    fun criticalReturnBoolean() = withRustLib {
        assertEquals(true, natives.test.rust.criticalReturnBoolean())
    }

    @Test
    fun criticalReturnByte() = withRustLib {
        assertEquals(1, natives.test.rust.criticalReturnByte())
    }

    @Test
    fun criticalReturnUByte() = withRustLib {
        assertEquals(UByte.MAX_VALUE, natives.test.rust.criticalReturnUbyte())
    }

    @Test
    fun criticalReturnShort() = withRustLib {
        assertEquals(1, natives.test.rust.criticalReturnShort())
    }

    @Test
    fun criticalReturnUShort() = withRustLib {
        assertEquals(UShort.MAX_VALUE, natives.test.rust.criticalReturnUshort())
    }

    @Test
    fun criticalReturnInt() = withRustLib {
        assertEquals(1, natives.test.rust.criticalReturnInt())
    }

    @Test
    fun criticalReturnUInt() = withRustLib {
        assertEquals(UInt.MAX_VALUE, natives.test.rust.criticalReturnUint())
    }

    @Test
    fun criticalReturnLong() = withRustLib {
        assertEquals(1, natives.test.rust.criticalReturnLong())
    }

    @Test
    fun criticalReturnULong() = withRustLib {
        assertEquals(ULong.MAX_VALUE, natives.test.rust.criticalReturnUlong())
    }

    @Test
    fun criticalReturnFloat() = withRustLib {
        assertEquals(1f, natives.test.rust.criticalReturnFloat())
    }

    @Test
    fun criticalReturnDouble() = withRustLib {
        assertEquals(1.0, natives.test.rust.criticalReturnDouble())
    }

    @Test
    fun criticalReturnEnum() = withRustLib {
        assertEquals(MyEnum.CASE1, natives.test.rust.criticalReturnEnum())
    }

    @Test
    fun criticalReturnInterface() = withRustLib {
        natives.test.rust.criticalReturnInterface()
    }
}