package tests.swift

import natives.test.swift.MyInterface
import withSwiftLib
import kotlin.test.Test

class Swift_Interfaces {

    @Test
    fun simple() = withSwiftLib {
        val a = MyInterface()
        a.test()
        a.testCritical()
        a.snakeCase()
        a.close()
    }
}