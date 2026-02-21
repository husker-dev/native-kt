import natives.test.get
import natives.test.getBoolean
import natives.test.getByte
import natives.test.getChar
import natives.test.getDouble
import natives.test.getFloat
import natives.test.getInt
import natives.test.getLong
import natives.test.getString
import natives.test.getStringLiteral
import kotlin.test.Test
import kotlin.test.assertEquals

class Return {

    @Test
    fun returnVoid() = withLib {
        assertEquals(Unit, get())
    }

    @Test
    fun returnInt() = withLib {
        assertEquals(99, getInt())
    }

    @Test
    fun returnLong() = withLib {
        assertEquals(9223372036854775805L, getLong())
    }

    @Test
    fun returnFloat() = withLib {
        assertEquals(99f, getFloat())
    }

    @Test
    fun returnDouble() = withLib {
        assertEquals(99.0, getDouble())
    }

    @Test
    fun returnByte() = withLib {
        assertEquals(99.toByte(), getByte())
    }

    @Test
    fun returnBoolean() = withLib {
        assertEquals(true, getBoolean())
    }

    @Test
    fun returnChar() = withLib {
        assertEquals('a', getChar())
    }

    @Test
    fun returnStringLiteral() = withLib {
        assertEquals("test string", getStringLiteral())
    }

    @Test
    fun returnString() = withLib {
        assertEquals("test string", getString())
    }
}