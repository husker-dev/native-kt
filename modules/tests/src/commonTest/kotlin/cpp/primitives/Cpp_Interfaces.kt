package cpp.primitives

import natives.testcpp.*
import withCppLib
import kotlin.test.Test

class Cpp_Interfaces {

    @Test
    fun simple() = withCppLib {
        val a = A()
        val b = B()
        a.test(b)
        a.close()
        b.close()
    }
}