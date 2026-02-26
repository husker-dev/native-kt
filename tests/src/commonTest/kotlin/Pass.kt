import natives.test.consume
import natives.test.consumeBoolean
import natives.test.consumeByte
import natives.test.consumeChar
import natives.test.consumeDouble
import natives.test.consumeFloat
import natives.test.consumeInt
import natives.test.consumeLong
import natives.test.consumeString
import kotlin.test.Test
import kotlin.test.assertTrue

class Pass {

    @Test
    fun passEmpty() = withLib {
        assertTrue(consume())
    }

    @Test
    fun passInt() = withLib {
        assertTrue(consumeInt(99))
    }

    @Test
    fun passLong() = withLib {
        assertTrue(consumeLong(9223372036854775805L))
    }

    @Test
    fun passFloat() = withLib {
        assertTrue(consumeFloat(99f))
    }

    @Test
    fun passDouble() = withLib {
        assertTrue(consumeDouble(1.0))
    }

    @Test
    fun passByte() = withLib {
        assertTrue(consumeByte(1.toByte()))
    }

    @Test
    fun passBoolean() = withLib {
        assertTrue(consumeBoolean(true))
    }

    @Test
    fun passChar() = withLib {
        assertTrue(consumeChar('a'))
    }

    @Test
    fun passString() = withLib {
        assertTrue(consumeString("test string"))
    }
}