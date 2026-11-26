@file:OptIn(ExperimentalUnsignedTypes::class)

package tests.objc

import natives.test.objc.MyEnum
import natives.test.objc.MyInterface
import withObjcLib
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Objc_Critical {

    @Test
    fun criticalPrimitives() = withObjcLib {
        assertTrue(natives.test.objc.criticalPrimitives(
            'a', true,
            1, UByte.MAX_VALUE,
            3, UShort.MAX_VALUE,
            5, UInt.MAX_VALUE,
            7, ULong.MAX_VALUE,
            1f, 2.0
        ))
    }

    @Test
    fun criticalEnum() = withObjcLib {
        assertTrue(natives.test.objc.criticalEnum(MyEnum.CASE1))
    }

    @Test
    fun criticalString() = withObjcLib {
        assertTrue(natives.test.objc.criticalString("test string"))
    }

    @Test
    fun criticalStringEmpty() = withObjcLib {
        assertTrue(natives.test.objc.criticalStringEmpty(""))
    }

    @Test
    fun criticalStringN() = withObjcLib {
        assertTrue(natives.test.objc.criticalStringN(null))
    }

    @Test
    fun criticalInterface() = withObjcLib {
        MyInterface().use { obj ->
            assertTrue(natives.test.objc.criticalInterface(obj))
        }
    }

    @Test
    fun criticalInterfaceN() = withObjcLib {
        assertTrue(natives.test.objc.criticalInterfaceN(null))
    }

    @Test
    fun criticalPrimitivesArray() = withObjcLib {
        assertTrue(natives.test.objc.criticalPrimitivesArray(
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
    fun criticalEnumArray() = withObjcLib {
        assertTrue(natives.test.objc.criticalEnumArray(
            arrayOf(MyEnum.CASE1, MyEnum.CASE2)
        ))
    }

    @Test
    fun criticalPrimitivesArrayEmpty() = withObjcLib {
        assertTrue(natives.test.objc.criticalPrimitivesArrayEmpty(
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
    fun criticalEnumArrayEmpty() = withObjcLib {
        assertTrue(natives.test.objc.criticalEnumArrayEmpty(
            arrayOf()
        ))
    }

    @Test
    fun criticalPrimitivesArrayN() = withObjcLib {
        assertTrue(natives.test.objc.criticalPrimitivesArrayN(
            null, null,
            null, null,
            null, null,
            null, null,
            null, null,
            null, null
        ))
    }

    @Test
    fun criticalEnumArrayN() = withObjcLib {
        assertTrue(natives.test.objc.criticalEnumArrayN(null))
    }

    @Test
    fun criticalReturnChar() = withObjcLib {
        assertEquals('a', natives.test.objc.criticalReturnChar())
    }

    @Test
    fun criticalReturnBoolean() = withObjcLib {
        assertEquals(true, natives.test.objc.criticalReturnBoolean())
    }

    @Test
    fun criticalReturnByte() = withObjcLib {
        assertEquals(1, natives.test.objc.criticalReturnByte())
    }

    @Test
    fun criticalReturnUByte() = withObjcLib {
        assertEquals(UByte.MAX_VALUE, natives.test.objc.criticalReturnUbyte())
    }

    @Test
    fun criticalReturnShort() = withObjcLib {
        assertEquals(1, natives.test.objc.criticalReturnShort())
    }

    @Test
    fun criticalReturnUShort() = withObjcLib {
        assertEquals(UShort.MAX_VALUE, natives.test.objc.criticalReturnUshort())
    }

    @Test
    fun criticalReturnInt() = withObjcLib {
        assertEquals(1, natives.test.objc.criticalReturnInt())
    }

    @Test
    fun criticalReturnUInt() = withObjcLib {
        assertEquals(UInt.MAX_VALUE, natives.test.objc.criticalReturnUint())
    }

    @Test
    fun criticalReturnLong() = withObjcLib {
        assertEquals(1, natives.test.objc.criticalReturnLong())
    }

    @Test
    fun criticalReturnULong() = withObjcLib {
        assertEquals(ULong.MAX_VALUE, natives.test.objc.criticalReturnUlong())
    }

    @Test
    fun criticalReturnFloat() = withObjcLib {
        assertEquals(1f, natives.test.objc.criticalReturnFloat())
    }

    @Test
    fun criticalReturnDouble() = withObjcLib {
        assertEquals(1.0, natives.test.objc.criticalReturnDouble())
    }

    @Test
    fun criticalReturnEnum() = withObjcLib {
        assertEquals(MyEnum.CASE1, natives.test.objc.criticalReturnEnum())
    }

    @Test
    fun criticalReturnInterface() = withObjcLib {
        natives.test.objc.criticalReturnInterface().use { obj ->
            assertTrue(obj.test())
        }
    }
}
