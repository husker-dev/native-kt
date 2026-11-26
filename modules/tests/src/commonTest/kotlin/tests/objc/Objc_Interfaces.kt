package tests.objc

import natives.test.objc.MyInterface
import withObjcLib
import kotlin.test.Test

class Objc_Interfaces {

    @Test
    fun simple() = withObjcLib {
        val a = MyInterface()
        a.test()
        a.testCritical()
        a.snakeCase()
        a.close()
    }
}
