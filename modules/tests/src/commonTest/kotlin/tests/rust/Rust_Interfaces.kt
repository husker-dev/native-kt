package tests.rust

import natives.test.rust.MyInterface
import withRustLib
import kotlin.test.Test

class Rust_Interfaces {

    @Test
    fun simple() = withRustLib {
        val a = MyInterface()
        a.test()
        a.testCritical()
        a.snakeCase()
        a.close()
    }
}