@file:OptIn(ExperimentalUnsignedTypes::class)

package rust

import natives.testrs.MyEnum
import withRustLib
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class Rust_Critical {

    @Test
    fun criticalPrimitives() = withRustLib {
        assertTrue(natives.testrs.criticalPrimitives(
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
        assertTrue(natives.testrs.criticalEnum(MyEnum.CASE1))
    }

    @Test
    fun criticalString() = withRustLib {
        assertTrue(natives.testrs.criticalString("test string"))
    }

    @Test
    fun criticalStringN() = withRustLib {
        assertTrue(natives.testrs.criticalStringN(null))
    }

    @Test
    fun criticalPrimitivesArray() = withRustLib {
        assertTrue(natives.testrs.criticalPrimitivesArray(
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
    fun criticalPrimitivesArrayN() = withRustLib {
        assertTrue(natives.testrs.criticalPrimitivesArrayN(
            null, null,
            null, null,
            null, null,
            null, null,
            null, null,
            null, null
        ))
    }

    @Test
    fun criticalEnumArray() = withRustLib {
        assertTrue(natives.testrs.criticalEnumArray(
            arrayOf(MyEnum.CASE1, MyEnum.CASE2)
        ))
    }

    @Test
    fun criticalEnumArrayN() = withRustLib {
        assertTrue(natives.testrs.criticalEnumArrayN(null))
    }

    @Test
    fun criticalReturnChar() = withRustLib {
        assertEquals('a', natives.testrs.criticalReturnChar())
    }

    @Test
    fun criticalReturnBoolean() = withRustLib {
        assertEquals(true, natives.testrs.criticalReturnBoolean())
    }

    @Test
    fun criticalReturnByte() = withRustLib {
        assertEquals(1, natives.testrs.criticalReturnByte())
    }

    @Test
    fun criticalReturnUByte() = withRustLib {
        assertEquals(UByte.MAX_VALUE, natives.testrs.criticalReturnUByte())
    }

    @Test
    fun criticalReturnShort() = withRustLib {
        assertEquals(1, natives.testrs.criticalReturnShort())
    }

    @Test
    fun criticalReturnUShort() = withRustLib {
        assertEquals(UShort.MAX_VALUE, natives.testrs.criticalReturnUShort())
    }

    @Test
    fun criticalReturnInt() = withRustLib {
        assertEquals(1, natives.testrs.criticalReturnInt())
    }

    @Test
    fun criticalReturnUInt() = withRustLib {
        assertEquals(UInt.MAX_VALUE, natives.testrs.criticalReturnUInt())
    }

    @Test
    fun criticalReturnLong() = withRustLib {
        assertEquals(1, natives.testrs.criticalReturnLong())
    }

    @Test
    fun criticalReturnULong() = withRustLib {
        assertEquals(ULong.MAX_VALUE, natives.testrs.criticalReturnULong())
    }

    @Test
    fun criticalReturnFloat() = withRustLib {
        assertEquals(1f, natives.testrs.criticalReturnFloat())
    }

    @Test
    fun criticalReturnDouble() = withRustLib {
        assertEquals(1.0, natives.testrs.criticalReturnDouble())
    }

    @Test
    fun criticalReturnEnum() = withRustLib {
        assertEquals(MyEnum.CASE1, natives.testrs.criticalReturnEnum())
    }
}