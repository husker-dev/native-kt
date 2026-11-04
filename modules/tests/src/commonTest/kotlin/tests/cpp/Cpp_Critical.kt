@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.cpp

import natives.test.cpp.MyEnum
import natives.test.cpp.MyInterface
import withCppLib
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Cpp_Critical {

    @Test
    fun criticalPrimitives() = withCppLib {
        assertTrue(natives.test.cpp.criticalPrimitives(
            'a', true,
            1, UByte.MAX_VALUE,
            3, UShort.MAX_VALUE,
            5, UInt.MAX_VALUE,
            7, ULong.MAX_VALUE,
            1f, 2.0
        ))
    }

    @Test
    fun criticalEnum() = withCppLib {
        assertTrue(natives.test.cpp.criticalEnum(MyEnum.CASE1))
    }

    @Test
    fun criticalString() = withCppLib {
        assertTrue(natives.test.cpp.criticalString("test string"))
    }

    @Test
    fun criticalStringEmpty() = withCppLib {
        assertTrue(natives.test.cpp.criticalStringEmpty(""))
    }

    @Test
    fun criticalStringN() = withCppLib {
        assertTrue(natives.test.cpp.criticalStringN(null))
    }

    @Test
    fun criticalInterface() = withCppLib {
        assertTrue(natives.test.cpp.criticalInterface(MyInterface()))
    }

    @Test
    fun criticalInterfaceN() = withCppLib {
        assertTrue(natives.test.cpp.criticalInterfaceN(null))
    }

    @Test
    fun criticalPrimitivesArray() = withCppLib {
        assertTrue(natives.test.cpp.criticalPrimitivesArray(
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
    fun criticalEnumArray() = withCppLib {
        assertTrue(natives.test.cpp.criticalEnumArray(
            arrayOf(MyEnum.CASE1, MyEnum.CASE2)
        ))
    }

    @Test
    fun criticalPrimitivesArrayEmpty() = withCppLib {
        assertTrue(natives.test.cpp.criticalPrimitivesArrayEmpty(
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
    fun criticalEnumArrayEmpty() = withCppLib {
        assertTrue(natives.test.cpp.criticalEnumArrayEmpty(
            arrayOf()
        ))
    }

    @Test
    fun criticalPrimitivesArrayN() = withCppLib {
        assertTrue(natives.test.cpp.criticalPrimitivesArrayN(
            null, null,
            null, null,
            null, null,
            null, null,
            null, null,
            null, null
        ))
    }

    @Test
    fun criticalEnumArrayN() = withCppLib {
        assertTrue(natives.test.cpp.criticalEnumArrayN(null))
    }

    @Test
    fun criticalReturnChar() = withCppLib {
        assertEquals('a', natives.test.cpp.criticalReturnChar())
    }

    @Test
    fun criticalReturnBoolean() = withCppLib {
        assertEquals(true, natives.test.cpp.criticalReturnBoolean())
    }

    @Test
    fun criticalReturnByte() = withCppLib {
        assertEquals(1, natives.test.cpp.criticalReturnByte())
    }

    @Test
    fun criticalReturnUByte() = withCppLib {
        assertEquals(UByte.MAX_VALUE, natives.test.cpp.criticalReturnUbyte())
    }

    @Test
    fun criticalReturnShort() = withCppLib {
        assertEquals(1, natives.test.cpp.criticalReturnShort())
    }

    @Test
    fun criticalReturnUShort() = withCppLib {
        assertEquals(UShort.MAX_VALUE, natives.test.cpp.criticalReturnUshort())
    }

    @Test
    fun criticalReturnInt() = withCppLib {
        assertEquals(1, natives.test.cpp.criticalReturnInt())
    }

    @Test
    fun criticalReturnUInt() = withCppLib {
        assertEquals(UInt.MAX_VALUE, natives.test.cpp.criticalReturnUint())
    }

    @Test
    fun criticalReturnLong() = withCppLib {
        assertEquals(1, natives.test.cpp.criticalReturnLong())
    }

    @Test
    fun criticalReturnULong() = withCppLib {
        assertEquals(ULong.MAX_VALUE, natives.test.cpp.criticalReturnUlong())
    }

    @Test
    fun criticalReturnFloat() = withCppLib {
        assertEquals(1f, natives.test.cpp.criticalReturnFloat())
    }

    @Test
    fun criticalReturnDouble() = withCppLib {
        assertEquals(1.0, natives.test.cpp.criticalReturnDouble())
    }

    @Test
    fun criticalReturnEnum() = withCppLib {
        assertEquals(MyEnum.CASE1, natives.test.cpp.criticalReturnEnum())
    }

    @Test
    fun criticalReturnInterface() = withCppLib {
        natives.test.cpp.criticalReturnInterface()
    }
}