package primitives

import withLib
import kotlin.test.Test
import kotlin.test.assertTrue

class CallbackArgPrimitive {

    @Test
    fun callbackVoid() = withLib {
        var pass = false
        natives.test.callbackVoid { pass = true }
        assertTrue(pass)
    }

    @Test
    fun callbackArgChar() = withLib {
        assertTrue(natives.test.callbackArgChar {
            println("${it.code} | ${it == 'a'}")
            it == 'a'
        })
    }

    @Test
    fun callbackArgBoolean() = withLib {
        assertTrue(natives.test.callbackArgBoolean { it })
    }

    @Test
    fun callbackArgByte() = withLib {
        assertTrue(natives.test.callbackArgByte { it == 1.toByte() })
    }

    @Test
    fun callbackArgShort() = withLib {
        assertTrue(natives.test.callbackArgShort { it == 1.toShort() })
    }

    @Test
    fun callbackArgInt() = withLib {
        assertTrue(natives.test.callbackArgInt { it == 1 })
    }

    @Test
    fun callbackArgLong() = withLib {
        assertTrue(natives.test.callbackArgLong { it == 1.toLong() })
    }

    @Test
    fun callbackArgFloat() = withLib {
        assertTrue(natives.test.callbackArgFloat { it == 1.1f })
    }

    @Test
    fun callbackArgDouble() = withLib {
        assertTrue(natives.test.callbackArgDouble { it == 1.1 })
    }

    @Test
    fun callbackArgString() = withLib {
        assertTrue(natives.test.callbackArgString { it == "test string" })
    }

    @Test
    fun callbackArgCallback() = withLib {
        val toPass = {}
        assertTrue(natives.test.callbackArgCallback(toPass) { it == toPass })
    }
}