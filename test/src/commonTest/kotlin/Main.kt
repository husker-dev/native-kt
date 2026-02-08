
import kotlinx.coroutines.test.runTest
import natives.test.consume
import natives.test.consumeBoolean
import natives.test.consumeByte
import natives.test.consumeChar
import natives.test.consumeDouble
import natives.test.consumeFloat
import natives.test.consumeInt
import natives.test.consumeLong
import natives.test.consumeString
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
import natives.test.loadLibTest
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
import kotlin.test.assertTrue


class JvmTests {

    fun withLib(block: () -> Unit) = runTest {
        loadLibTest()
        block()
    }

    // Call

    @Test
    fun call() = withLib {
        assertTrue(consume())
    }

    @Test
    fun callInt() = withLib {
        assertTrue(consumeInt(99))
    }

    @Test
    fun callLong() = withLib {
        assertTrue(consumeLong(9223372036854775805L))
    }

    @Test
    fun callFloat() = withLib {
        assertTrue(consumeFloat(99f))
    }

    @Test
    fun callDouble() = withLib {
        assertTrue(consumeDouble(1.0))
    }

    @Test
    fun callByte() = withLib {
        assertTrue(consumeByte(1.toByte()))
    }

    @Test
    fun callBoolean() = withLib {
        assertTrue(consumeBoolean(true))
    }

    @Test
    fun callChar() = withLib {
        assertTrue(consumeChar('a'))
    }

    @Test
    fun callString() = withLib {
        assertTrue(consumeString("test string"))
    }

    // Receive

    @Test
    fun receive() = withLib {
        assertEquals(Unit, get())
    }

    @Test
    fun receiveInt() = withLib {
        assertEquals(99, getInt())
    }

    @Test
    fun receiveLong() = withLib {
        assertEquals(9223372036854775805L, getLong())
    }

    @Test
    fun receiveFloat() = withLib {
        assertEquals(99f, getFloat())
    }

    @Test
    fun receiveDouble() = withLib {
        assertEquals(99.0, getDouble())
    }

    @Test
    fun receiveByte() = withLib {
        assertEquals(99.toByte(), getByte())
    }

    @Test
    fun receiveBoolean() = withLib {
        assertEquals(true, getBoolean())
    }

    @Test
    fun receiveChar() = withLib {
        assertEquals('a', getChar())
    }


    @Test
    fun receiveStringLiteral() = withLib {
        assertEquals("test string", getStringLiteral())
    }

    @Test
    fun receiveString() = withLib {
        assertEquals("test string", getString())
    }

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