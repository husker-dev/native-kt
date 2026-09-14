package tests.c

import natives.test.MyInterface
import withCLib
import kotlin.test.Test

class C_Interfaces {

    @Test
    fun simple() = withCLib {
        val a = MyInterface()
        a.test()
        a.testCritical()
        a.close()
    }
}