@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.swift

import natives.test.swift.MyEnum
import natives.test.swift.MyInterface
import withSwiftLib
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class Swift_Critical {

    @Test
    fun criticalPrimitives() = withSwiftLib {
        assertTrue(natives.test.swift.criticalPrimitives(
            'a', true,
            1, UByte.MAX_VALUE,
            3, UShort.MAX_VALUE,
            5, UInt.MAX_VALUE,
            7, ULong.MAX_VALUE,
            1f, 2.0
        ))
    }

    @Test
    fun criticalEnum() = withSwiftLib {
        assertTrue(natives.test.swift.criticalEnum(MyEnum.CASE1))
    }

    @Test
    fun criticalString() = withSwiftLib {
        assertTrue(natives.test.swift.criticalString("test string"))
    }

    @Test
    fun criticalStringEmpty() = withSwiftLib {
        assertTrue(natives.test.swift.criticalStringEmpty(""))
    }

    @Test
    fun criticalStringN() = withSwiftLib {
        assertTrue(natives.test.swift.criticalStringN(null))
    }

    @Test
    fun criticalInterface() = withSwiftLib {
        assertTrue(natives.test.swift.criticalInterface(MyInterface()))
    }

    @Test
    fun criticalInterfaceN() = withSwiftLib {
        assertTrue(natives.test.swift.criticalInterfaceN(null))
    }

    @Test
    fun criticalPrimitivesArray() = withSwiftLib {
        assertTrue(natives.test.swift.criticalPrimitivesArray(
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
    fun criticalEnumArray() = withSwiftLib {
        assertTrue(natives.test.swift.criticalEnumArray(
            arrayOf(MyEnum.CASE1, MyEnum.CASE2)
        ))
    }

    @Test
    fun criticalPrimitivesArrayEmpty() = withSwiftLib {
        assertTrue(natives.test.swift.criticalPrimitivesArrayEmpty(
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
    fun criticalEnumArrayEmpty() = withSwiftLib {
        assertTrue(natives.test.swift.criticalEnumArrayEmpty(
            arrayOf()
        ))
    }

    @Test
    fun criticalPrimitivesArrayN() = withSwiftLib {
        assertTrue(natives.test.swift.criticalPrimitivesArrayN(
            null, null,
            null, null,
            null, null,
            null, null,
            null, null,
            null, null
        ))
    }

    @Test
    fun criticalEnumArrayN() = withSwiftLib {
        assertTrue(natives.test.swift.criticalEnumArrayN(null))
    }

    @Test
    fun criticalReturnChar() = withSwiftLib {
        assertEquals('a', natives.test.swift.criticalReturnChar())
    }

    @Test
    fun criticalReturnBoolean() = withSwiftLib {
        assertEquals(true, natives.test.swift.criticalReturnBoolean())
    }

    @Test
    fun criticalReturnByte() = withSwiftLib {
        assertEquals(1, natives.test.swift.criticalReturnByte())
    }

    @Test
    fun criticalReturnUByte() = withSwiftLib {
        assertEquals(UByte.MAX_VALUE, natives.test.swift.criticalReturnUbyte())
    }

    @Test
    fun criticalReturnShort() = withSwiftLib {
        assertEquals(1, natives.test.swift.criticalReturnShort())
    }

    @Test
    fun criticalReturnUShort() = withSwiftLib {
        assertEquals(UShort.MAX_VALUE, natives.test.swift.criticalReturnUshort())
    }

    @Test
    fun criticalReturnInt() = withSwiftLib {
        assertEquals(1, natives.test.swift.criticalReturnInt())
    }

    @Test
    fun criticalReturnUInt() = withSwiftLib {
        assertEquals(UInt.MAX_VALUE, natives.test.swift.criticalReturnUint())
    }

    @Test
    fun criticalReturnLong() = withSwiftLib {
        assertEquals(1, natives.test.swift.criticalReturnLong())
    }

    @Test
    fun criticalReturnULong() = withSwiftLib {
        assertEquals(ULong.MAX_VALUE, natives.test.swift.criticalReturnUlong())
    }

    @Test
    fun criticalReturnFloat() = withSwiftLib {
        assertEquals(1f, natives.test.swift.criticalReturnFloat())
    }

    @Test
    fun criticalReturnDouble() = withSwiftLib {
        assertEquals(1.0, natives.test.swift.criticalReturnDouble())
    }

    @Test
    fun criticalReturnEnum() = withSwiftLib {
        assertEquals(MyEnum.CASE1, natives.test.swift.criticalReturnEnum())
    }

    @Test
    fun criticalReturnInterface() = withSwiftLib {
        natives.test.swift.criticalReturnInterface()
    }
}