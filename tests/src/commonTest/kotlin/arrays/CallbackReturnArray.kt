package arrays

import withLib
import kotlin.test.Test
import kotlin.test.assertTrue

class CallbackReturnArray {

    @Test
    fun callbackReturnCharArray() = withLib {
        assertTrue(natives.test.callbackReturnCharArray { charArrayOf('a', 'b') })
    }

    @Test
    fun callbackReturnBooleanArray() = withLib {
        assertTrue(natives.test.callbackReturnBooleanArray { booleanArrayOf(true, false) })
    }

    @Test
    fun callbackReturnByteArray() = withLib {
        assertTrue(natives.test.callbackReturnByteArray { byteArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnShortArray() = withLib {
        assertTrue(natives.test.callbackReturnShortArray { shortArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnIntArray() = withLib {
        assertTrue(natives.test.callbackReturnIntArray { intArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnLongArray() = withLib {
        assertTrue(natives.test.callbackReturnLongArray { longArrayOf(1, 2) })
    }

    @Test
    fun callbackReturnFloatArray() = withLib {
        assertTrue(natives.test.callbackReturnFloatArray { floatArrayOf(1.1f, 2.2f) })
    }

    @Test
    fun callbackReturnDoubleArray() = withLib {
        assertTrue(natives.test.callbackReturnDoubleArray { doubleArrayOf(1.1, 2.2) })
    }
}