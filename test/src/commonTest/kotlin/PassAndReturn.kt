import natives.test.pingBoolean
import natives.test.pingByte
import natives.test.pingChar
import natives.test.pingDouble
import natives.test.pingFloat
import natives.test.pingInt
import natives.test.pingLong
import natives.test.pingString
import kotlin.test.Test
import kotlin.test.assertEquals

class PassAndReturn {

    // Call & Receive

    @Test
    fun callAndReceiveInt() = withLib {
        assertEquals(99, pingInt(99))
    }

    @Test
    fun callAndReceiveLong() = withLib {
        assertEquals(9223372036854775805L, pingLong(9223372036854775805L))
    }

    @Test
    fun callAndReceiveFloat() = withLib {
        assertEquals(99f, pingFloat(99f))
    }

    @Test
    fun callAndReceiveDouble() = withLib {
        assertEquals(99.0, pingDouble(99.0))
    }

    @Test
    fun callAndReceiveByte() = withLib {
        assertEquals(99.toByte(), pingByte(99.toByte()))
    }

    @Test
    fun callAndReceiveChar() = withLib {
        assertEquals('a', pingChar('a'))
    }

    @Test
    fun callAndReceiveBoolean() = withLib {
        assertEquals(true, pingBoolean(true))
    }

    @Test
    fun callAndReceiveString() = withLib {
        assertEquals("test string", pingString("test string"))
    }

}