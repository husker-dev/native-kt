package c

import natives.test.A
import natives.test.B
import withCLib
import kotlin.test.Test

class C_Interfaces {

    @Test
    fun simple() = withCLib {
        val a = A()
        val b = B()
        a.test(b)
        a.close()
        b.close()
    }
}