package rust

import natives.testrs.A
import natives.testrs.B
import withRustLib
import kotlin.test.Test

class Interfaces {

    @Test
    fun simple() = withRustLib {
        val a = A()
        a.test(B())
        a.close()

    }
}