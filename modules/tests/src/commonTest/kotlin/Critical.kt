import natives.test.MyEnum
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Critical {

    @Test
    fun criticalPrimitives() = withLib {
        assertTrue(natives.test.criticalPrimitives(
            'a', true, 1, 2, 3, 4, 1f, 2.0
        ))
    }

    @Test
    fun criticalEnum() = withLib {
        assertTrue(natives.test.criticalEnum(MyEnum.CASE1))
    }

    @Test
    fun criticalString() = withLib {
        assertTrue(natives.test.criticalString("test string"))
    }

    @Test
    fun criticalPrimitivesArray() = withLib {
        assertTrue(natives.test.criticalPrimitivesArray(
            charArrayOf('a', 'b'),
            booleanArrayOf(true, false),
            byteArrayOf(1, 2),
            shortArrayOf(1, 2),
            intArrayOf(1, 2),
            longArrayOf(1, 2),
            floatArrayOf(1.1f, 2.2f),
            doubleArrayOf(1.1, 2.2)
        ))
    }

    @Test
    fun criticalEnumArray() = withLib {
        assertTrue(natives.test.criticalEnumArray(
            arrayOf(MyEnum.CASE1, MyEnum.CASE2)
        ))
    }

    @Test
    fun criticalReturnChar() = withLib {
        assertEquals('a', natives.test.criticalReturnChar())
    }

    @Test
    fun criticalReturnBoolean() = withLib {
        assertEquals(true, natives.test.criticalReturnBoolean())
    }

    @Test
    fun criticalReturnByte() = withLib {
        assertEquals(1, natives.test.criticalReturnByte())
    }

    @Test
    fun criticalReturnShort() = withLib {
        assertEquals(1, natives.test.criticalReturnShort())
    }

    @Test
    fun criticalReturnInt() = withLib {
        assertEquals(1, natives.test.criticalReturnInt())
    }

    @Test
    fun criticalReturnLong() = withLib {
        assertEquals(1, natives.test.criticalReturnLong())
    }
    @Test
    fun criticalReturnFloat() = withLib {
        assertEquals(1f, natives.test.criticalReturnFloat())
    }

    @Test
    fun criticalReturnDouble() = withLib {
        assertEquals(1.0, natives.test.criticalReturnDouble())
    }

    @Test
    fun criticalReturnEnum() = withLib {
        assertEquals(MyEnum.CASE1, natives.test.criticalReturnEnum())
    }
}