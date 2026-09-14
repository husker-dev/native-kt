package tests.rust

import natives.testrs.MyInterface
import withRustLib
import kotlin.test.Test

class Rust_Interfaces {

    @Test
    fun simple() = withRustLib {
        val a = MyInterface()
        a.test()
        a.testCritical()
        a.close()
    }
}