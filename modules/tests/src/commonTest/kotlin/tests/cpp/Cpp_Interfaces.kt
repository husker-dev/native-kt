package tests.cpp

import natives.testcpp.*
import withCppLib
import kotlin.test.Test

class Cpp_Interfaces {

    @Test
    fun simple() = withCppLib {
        val a = MyInterface()
        a.test()
        a.testCritical()
        a.close()
    }
}