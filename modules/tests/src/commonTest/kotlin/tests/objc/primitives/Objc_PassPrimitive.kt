package tests.objc.primitives

import natives.test.objc.MyDictionary
import natives.test.objc.MyEnum
import natives.test.objc.MyInterface
import withObjcLib
import kotlin.test.Test
import kotlin.test.assertTrue

class Objc_PassPrimitive {

    @Test
    fun passVoid() = withObjcLib {
        assertTrue(natives.test.objc.passVoid())
    }

    @Test
    fun passChar() = withObjcLib {
        assertTrue(natives.test.objc.passChar('a'))
    }

    @Test
    fun passCharN() = withObjcLib {
        assertTrue(natives.test.objc.passCharN(null))
        assertTrue(natives.test.objc.passCharN('a'))
    }

    @Test
    fun passBoolean() = withObjcLib {
        assertTrue(natives.test.objc.passBoolean(true))
    }

    @Test
    fun passBooleanN() = withObjcLib {
        assertTrue(natives.test.objc.passBooleanN(null))
        assertTrue(natives.test.objc.passBooleanN(true))
    }

    @Test
    fun passByte() = withObjcLib {
        assertTrue(natives.test.objc.passByte(1.toByte()))
    }

    @Test
    fun passByteN() = withObjcLib {
        assertTrue(natives.test.objc.passByteN(null))
        assertTrue(natives.test.objc.passByteN(1.toByte()))
    }

    @Test
    fun passUByte() = withObjcLib {
        assertTrue(natives.test.objc.passUbyte(UByte.MAX_VALUE))
    }

    @Test
    fun passUByteN() = withObjcLib {
        assertTrue(natives.test.objc.passUbyteN(null))
        assertTrue(natives.test.objc.passUbyteN(UByte.MAX_VALUE))
    }

    @Test
    fun passShort() = withObjcLib {
        assertTrue(natives.test.objc.passShort(1.toShort()))
    }

    @Test
    fun passShortN() = withObjcLib {
        assertTrue(natives.test.objc.passShortN(null))
        assertTrue(natives.test.objc.passShortN(1.toShort()))
    }

    @Test
    fun passUShort() = withObjcLib {
        assertTrue(natives.test.objc.passUshort(UShort.MAX_VALUE))
    }

    @Test
    fun passUShortN() = withObjcLib {
        assertTrue(natives.test.objc.passUshortN(null))
        assertTrue(natives.test.objc.passUshortN(UShort.MAX_VALUE))
    }

    @Test
    fun passInt() = withObjcLib {
        assertTrue(natives.test.objc.passInt(99))
    }

    @Test
    fun passIntN() = withObjcLib {
        assertTrue(natives.test.objc.passIntN(null))
        assertTrue(natives.test.objc.passIntN(99))
    }

    @Test
    fun passUInt() = withObjcLib {
        assertTrue(natives.test.objc.passUint(UInt.MAX_VALUE))
    }

    @Test
    fun passUIntN() = withObjcLib {
        assertTrue(natives.test.objc.passUintN(null))
        assertTrue(natives.test.objc.passUintN(UInt.MAX_VALUE))
    }

    @Test
    fun passLong() = withObjcLib {
        assertTrue(natives.test.objc.passLong(9223372036854775805L))
    }

    @Test
    fun passLongN() = withObjcLib {
        assertTrue(natives.test.objc.passLongN(null))
        assertTrue(natives.test.objc.passLongN(9223372036854775805L))
    }

    @Test
    fun passULong() = withObjcLib {
        assertTrue(natives.test.objc.passUlong(ULong.MAX_VALUE))
    }

    @Test
    fun passULongN() = withObjcLib {
        assertTrue(natives.test.objc.passUlongN(null))
        assertTrue(natives.test.objc.passUlongN(ULong.MAX_VALUE))
    }

    @Test
    fun passFloat() = withObjcLib {
        assertTrue(natives.test.objc.passFloat(99.9f))
    }

    @Test
    fun passFloatN() = withObjcLib {
        assertTrue(natives.test.objc.passFloatN(null))
        assertTrue(natives.test.objc.passFloatN(99.9f))
    }

    @Test
    fun passDouble() = withObjcLib {
        assertTrue(natives.test.objc.passDouble(1.1))
    }

    @Test
    fun passDoubleN() = withObjcLib {
        assertTrue(natives.test.objc.passDoubleN(null))
        assertTrue(natives.test.objc.passDoubleN(1.1))
    }

    @Test
    fun passString() = withObjcLib {
        assertTrue(natives.test.objc.passString("test string"))
    }

    @Test
    fun passStringEmpty() = withObjcLib {
        assertTrue(natives.test.objc.passStringEmpty(""))
    }

    @Test
    fun passStringN() = withObjcLib {
        assertTrue(natives.test.objc.passStringN(null))
    }

    @Test
    fun passEnum() = withObjcLib {
        assertTrue(natives.test.objc.passEnum(MyEnum.CASE2))
    }

    @Test
    fun passEnumN() = withObjcLib {
        assertTrue(natives.test.objc.passEnumN(null))
        assertTrue(natives.test.objc.passEnumN(MyEnum.CASE2))
    }

    @Test
    fun passDictionary() = withObjcLib {
        assertTrue(natives.test.objc.passDictionary(MyDictionary(1, 2, 3, 4)))
    }

    @Test
    fun passDictionaryN() = withObjcLib {
        assertTrue(natives.test.objc.passDictionaryN(null))
    }

    @Test
    fun passInterface() = withObjcLib {
        val item = MyInterface()
        assertTrue(natives.test.objc.passInterface(item))
        item.close()
    }

    @Test
    fun passInterfaceN() = withObjcLib {
        assertTrue(natives.test.objc.passInterfaceN(null))
    }
}
