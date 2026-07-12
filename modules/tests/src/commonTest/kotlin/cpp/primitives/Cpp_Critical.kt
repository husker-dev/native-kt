@file:OptIn(ExperimentalUnsignedTypes::class)

package cpp.primitives

import natives.testcpp.MyEnum
import withCppLib
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Cpp_Critical {

    @Test
    fun criticalPrimitives() = withCppLib {
        assertTrue(natives.testcpp.criticalPrimitives(
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
        assertTrue(natives.testcpp.criticalEnum(MyEnum.CASE1))
    }

    @Test
    fun criticalString() = withCppLib {
        assertTrue(natives.testcpp.criticalString("test string"))
    }

    @Test
    fun criticalStringN() = withCppLib {
        assertTrue(natives.testcpp.criticalStringN(null))
    }

    @Test
    fun criticalPrimitivesArray() = withCppLib {
        assertTrue(natives.testcpp.criticalPrimitivesArray(
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
    fun criticalPrimitivesArrayN() = withCppLib {
        assertTrue(natives.testcpp.criticalPrimitivesArrayN(
            null, null,
            null, null,
            null, null,
            null, null,
            null, null,
            null, null
        ))
    }

    @Test
    fun criticalEnumArray() = withCppLib {
        assertTrue(natives.testcpp.criticalEnumArray(
            arrayOf(MyEnum.CASE1, MyEnum.CASE2)
        ))
    }

    @Test
    fun criticalEnumArrayN() = withCppLib {
        assertTrue(natives.testcpp.criticalEnumArrayN(null))
    }

    @Test
    fun criticalReturnChar() = withCppLib {
        assertEquals('a', natives.testcpp.criticalReturnChar())
    }

    @Test
    fun criticalReturnBoolean() = withCppLib {
        assertEquals(true, natives.testcpp.criticalReturnBoolean())
    }

    @Test
    fun criticalReturnByte() = withCppLib {
        assertEquals(1, natives.testcpp.criticalReturnByte())
    }

    @Test
    fun criticalReturnUByte() = withCppLib {
        assertEquals(UByte.MAX_VALUE, natives.testcpp.criticalReturnUByte())
    }

    @Test
    fun criticalReturnShort() = withCppLib {
        assertEquals(1, natives.testcpp.criticalReturnShort())
    }

    @Test
    fun criticalReturnUShort() = withCppLib {
        assertEquals(UShort.MAX_VALUE, natives.testcpp.criticalReturnUShort())
    }

    @Test
    fun criticalReturnInt() = withCppLib {
        assertEquals(1, natives.testcpp.criticalReturnInt())
    }

    @Test
    fun criticalReturnUInt() = withCppLib {
        assertEquals(UInt.MAX_VALUE, natives.testcpp.criticalReturnUInt())
    }

    @Test
    fun criticalReturnLong() = withCppLib {
        assertEquals(1, natives.testcpp.criticalReturnLong())
    }

    @Test
    fun criticalReturnULong() = withCppLib {
        assertEquals(ULong.MAX_VALUE, natives.testcpp.criticalReturnULong())
    }

    @Test
    fun criticalReturnFloat() = withCppLib {
        assertEquals(1f, natives.testcpp.criticalReturnFloat())
    }

    @Test
    fun criticalReturnDouble() = withCppLib {
        assertEquals(1.0, natives.testcpp.criticalReturnDouble())
    }

    @Test
    fun criticalReturnEnum() = withCppLib {
        assertEquals(MyEnum.CASE1, natives.testcpp.criticalReturnEnum())
    }
}