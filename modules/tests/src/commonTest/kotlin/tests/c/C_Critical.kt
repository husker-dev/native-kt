@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.c

import natives.test.c.MyEnum
import natives.test.c.MyInterface
import withCLib
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class C_Critical {

    @Test
    fun criticalPrimitives() = withCLib {
        assertTrue(natives.test.c.criticalPrimitives(
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
        assertTrue(natives.test.c.criticalEnum(MyEnum.CASE1))
    }

    @Test
    fun criticalString() = withCLib {
        assertTrue(natives.test.c.criticalString("test string"))
    }

    @Test
    fun criticalStringEmpty() = withCLib {
        assertTrue(natives.test.c.criticalStringEmpty(""))
    }

    @Test
    fun criticalStringN() = withCLib {
        assertTrue(natives.test.c.criticalStringN(null))
    }

    @Test
    fun criticalInterface() = withCLib {
        assertTrue(natives.test.c.criticalInterface(MyInterface()))
    }

    @Test
    fun criticalInterfaceN() = withCLib {
        assertTrue(natives.test.c.criticalInterfaceN(null))
    }

    @Test
    fun criticalPrimitivesArray() = withCLib {
        assertTrue(natives.test.c.criticalPrimitivesArray(
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
    fun criticalEnumArray() = withCLib {
        assertTrue(natives.test.c.criticalEnumArray(
            arrayOf(MyEnum.CASE1, MyEnum.CASE2)
        ))
    }

    @Test
    fun criticalPrimitivesArrayEmpty() = withCLib {
        assertTrue(natives.test.c.criticalPrimitivesArrayEmpty(
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
    fun criticalEnumArrayEmpty() = withCLib {
        assertTrue(natives.test.c.criticalEnumArrayEmpty(
            arrayOf()
        ))
    }

    @Test
    fun criticalPrimitivesArrayN() = withCLib {
        assertTrue(natives.test.c.criticalPrimitivesArrayN(
            null, null,
            null, null,
            null, null,
            null, null,
            null, null,
            null, null
        ))
    }

    @Test
    fun criticalEnumArrayN() = withCLib {
        assertTrue(natives.test.c.criticalEnumArrayN(null))
    }

    @Test
    fun criticalReturnChar() = withCLib {
        assertEquals('a', natives.test.c.criticalReturnChar())
    }

    @Test
    fun criticalReturnBoolean() = withCLib {
        assertEquals(true, natives.test.c.criticalReturnBoolean())
    }

    @Test
    fun criticalReturnByte() = withCLib {
        assertEquals(1, natives.test.c.criticalReturnByte())
    }

    @Test
    fun criticalReturnUByte() = withCLib {
        assertEquals(UByte.MAX_VALUE, natives.test.c.criticalReturnUbyte())
    }

    @Test
    fun criticalReturnShort() = withCLib {
        assertEquals(1, natives.test.c.criticalReturnShort())
    }

    @Test
    fun criticalReturnUShort() = withCLib {
        assertEquals(UShort.MAX_VALUE, natives.test.c.criticalReturnUshort())
    }

    @Test
    fun criticalReturnInt() = withCLib {
        assertEquals(1, natives.test.c.criticalReturnInt())
    }

    @Test
    fun criticalReturnUInt() = withCLib {
        assertEquals(UInt.MAX_VALUE, natives.test.c.criticalReturnUint())
    }

    @Test
    fun criticalReturnLong() = withCLib {
        assertEquals(1, natives.test.c.criticalReturnLong())
    }

    @Test
    fun criticalReturnULong() = withCLib {
        assertEquals(ULong.MAX_VALUE, natives.test.c.criticalReturnUlong())
    }

    @Test
    fun criticalReturnFloat() = withCLib {
        assertEquals(1f, natives.test.c.criticalReturnFloat())
    }

    @Test
    fun criticalReturnDouble() = withCLib {
        assertEquals(1.0, natives.test.c.criticalReturnDouble())
    }

    @Test
    fun criticalReturnEnum() = withCLib {
        assertEquals(MyEnum.CASE1, natives.test.c.criticalReturnEnum())
    }

    @Test
    fun criticalReturnInterface() = withCLib {
        natives.test.c.criticalReturnInterface()
    }
}