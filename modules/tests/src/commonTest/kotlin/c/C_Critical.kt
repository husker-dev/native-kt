@file:OptIn(ExperimentalUnsignedTypes::class)

package c

import natives.test.MyEnum
import withCLib
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class C_Critical {

    @Test
    fun criticalPrimitives() = withCLib {
        assertTrue(natives.test.criticalPrimitives(
            'a', true,
            1, UByte.MAX_VALUE,
            3, UShort.MAX_VALUE,
            5, UInt.MAX_VALUE,
            7, ULong.MAX_VALUE,
            1f, 2.0
        ))
    }

    @Test
    fun criticalEnum() = withCLib {
        assertTrue(natives.test.criticalEnum(MyEnum.CASE1))
    }

    @Test
    fun criticalString() = withCLib {
        assertTrue(natives.test.criticalString("test string"))
    }

    @Test
    fun criticalStringN() = withCLib {
        assertTrue(natives.test.criticalStringN(null))
    }

    @Test
    fun criticalPrimitivesArray() = withCLib {
        assertTrue(natives.test.criticalPrimitivesArray(
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
    fun criticalPrimitivesArrayN() = withCLib {
        assertTrue(natives.test.criticalPrimitivesArrayN(
            null, null,
            null, null,
            null, null,
            null, null,
            null, null,
            null, null
        ))
    }

    @Test
    fun criticalEnumArray() = withCLib {
        assertTrue(natives.test.criticalEnumArray(
            arrayOf(MyEnum.CASE1, MyEnum.CASE2)
        ))
    }

    @Test
    fun criticalEnumArrayN() = withCLib {
        assertTrue(natives.test.criticalEnumArrayN(null))
    }

    @Test
    fun criticalReturnChar() = withCLib {
        assertEquals('a', natives.test.criticalReturnChar())
    }

    @Test
    fun criticalReturnBoolean() = withCLib {
        assertEquals(true, natives.test.criticalReturnBoolean())
    }

    @Test
    fun criticalReturnByte() = withCLib {
        assertEquals(1, natives.test.criticalReturnByte())
    }

    @Test
    fun criticalReturnUByte() = withCLib {
        assertEquals(UByte.MAX_VALUE, natives.test.criticalReturnUByte())
    }

    @Test
    fun criticalReturnShort() = withCLib {
        assertEquals(1, natives.test.criticalReturnShort())
    }

    @Test
    fun criticalReturnUShort() = withCLib {
        assertEquals(UShort.MAX_VALUE, natives.test.criticalReturnUShort())
    }

    @Test
    fun criticalReturnInt() = withCLib {
        assertEquals(1, natives.test.criticalReturnInt())
    }

    @Test
    fun criticalReturnUInt() = withCLib {
        assertEquals(UInt.MAX_VALUE, natives.test.criticalReturnUInt())
    }

    @Test
    fun criticalReturnLong() = withCLib {
        assertEquals(1, natives.test.criticalReturnLong())
    }

    @Test
    fun criticalReturnULong() = withCLib {
        assertEquals(ULong.MAX_VALUE, natives.test.criticalReturnULong())
    }

    @Test
    fun criticalReturnFloat() = withCLib {
        assertEquals(1f, natives.test.criticalReturnFloat())
    }

    @Test
    fun criticalReturnDouble() = withCLib {
        assertEquals(1.0, natives.test.criticalReturnDouble())
    }

    @Test
    fun criticalReturnEnum() = withCLib {
        assertEquals(MyEnum.CASE1, natives.test.criticalReturnEnum())
    }
}