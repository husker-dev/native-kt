package rust

import natives.testrs.A
import natives.testrs.B
import withRustLib
import kotlin.test.Test

class Rust_Interfaces {

    @Test
    fun simple() = withRustLib {
        val a = A()
        val b = B()
        a.test(b)
        a.close()
        b.close()
    }
}