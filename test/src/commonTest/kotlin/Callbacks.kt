
import natives.test.SimpleCallback
import natives.test.callbackReturn
import natives.test.simpleCallback
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Callbacks {

    @Test
    fun simpleTest() = withLib {
        var a = 0
        simpleCallback {
            a = it
        }
        assertEquals(2, a)
    }

    @Test
    fun callbackEquality() = withLib {
        val callback: SimpleCallback = {  }
        val result = callbackReturn(callback)
        assertEquals(callback, result)
    }

    @Test
    fun callbackReturnString() = withLib {
        assertTrue(natives.test.callbackReturnString { "test" })
    }

    @Test
    fun callbackPingString() = withLib {
        assertTrue(natives.test.callbackPingString { it })
    }

    @Test
    fun callbackPingCallback() = withLib {
        val item = { _: Int -> }
        val response = natives.test.callbackPingCallback({ it }, item)
        assertEquals(item, response)
    }
}