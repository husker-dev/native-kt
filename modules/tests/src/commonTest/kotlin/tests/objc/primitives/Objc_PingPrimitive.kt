package tests.objc.primitives

import natives.test.objc.EmptyDictionary
import natives.test.objc.MyDictionary
import natives.test.objc.MyEnum
import natives.test.objc.pingBoolean
import natives.test.objc.pingByte
import natives.test.objc.pingChar
import natives.test.objc.pingDouble
import natives.test.objc.pingFloat
import natives.test.objc.pingInt
import natives.test.objc.pingLong
import natives.test.objc.pingString
import natives.test.objc.pingShort
import natives.test.objc.MyInterface
import withObjcLib
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Objc_PingPrimitive {

    @Test
    fun pingChar() = withObjcLib {
        assertEquals('a', pingChar('a'))
    }

    @Test
    fun pingBoolean() = withObjcLib {
        assertEquals(true, pingBoolean(true))
    }

    @Test
    fun pingByte() = withObjcLib {
        assertEquals(99.toByte(), pingByte(99.toByte()))
    }

    @Test
    fun pingUByte() = withObjcLib {
        assertEquals(UByte.MAX_VALUE, natives.test.objc.pingUbyte(UByte.MAX_VALUE))
    }

    @Test
    fun pingShort() = withObjcLib {
        assertEquals(99.toShort(), pingShort(99.toShort()))
    }

    @Test
    fun pingUShort() = withObjcLib {
        assertEquals(UShort.MAX_VALUE, natives.test.objc.pingUshort(UShort.MAX_VALUE))
    }

    @Test
    fun pingInt() = withObjcLib {
        assertEquals(99, pingInt(99))
    }

    @Test
    fun pingUInt() = withObjcLib {
        assertEquals(UInt.MAX_VALUE, natives.test.objc.pingUint(UInt.MAX_VALUE))
    }

    @Test
    fun pingLong() = withObjcLib {
        assertEquals(9223372036854775805L, pingLong(9223372036854775805L))
    }

    @Test
    fun pingULong() = withObjcLib {
        assertEquals(ULong.MAX_VALUE, natives.test.objc.pingUlong(ULong.MAX_VALUE))
    }

    @Test
    fun pingFloat() = withObjcLib {
        assertEquals(99f, pingFloat(99f))
    }

    @Test
    fun pingDouble() = withObjcLib {
        assertEquals(99.0, pingDouble(99.0))
    }

    @Test
    fun pingString() = withObjcLib {
        assertEquals("test string", pingString("test string"))
    }

    @Test
    fun pingStringUnicodeAndEmbeddedNull() = withObjcLib {
        val value = "Привет\u0000🌍"
        assertEquals(value, pingString(value))
    }

    @Test
    fun pingStringEmpty() = withObjcLib {
        assertEquals("", natives.test.objc.pingStringEmpty(""))
    }

    @Test
    fun pingStringN() = withObjcLib {
        assertNull(natives.test.objc.pingStringN(null))
    }

    @Test
    fun pingEnum() = withObjcLib {
        assertEquals(MyEnum.CASE2, natives.test.objc.pingEnum(MyEnum.CASE2))
    }

    @Test
    fun pingDictionary() = withObjcLib {
        assertEquals(
            MyDictionary(1, 2, 3, 4),
            natives.test.objc.pingDictionary(MyDictionary(1, 2, 3, 4))
        )
    }

    @Test
    fun pingEmptyDictionary() = withObjcLib {
        assertEquals(
            EmptyDictionary(),
            natives.test.objc.pingEmptyDictionary(EmptyDictionary())
        )
    }

    @Test
    fun pingDictionaryN() = withObjcLib {
        assertNull(natives.test.objc.pingDictionaryN(null))
    }

    @Test
    fun pingInterface() = withObjcLib {
        val obj = MyInterface()
        val copy = natives.test.objc.pingInterface(obj)
        assertEquals(obj, copy)
        obj.close()
        copy.close()
    }

    @Test
    fun pingInterfaceN() = withObjcLib {
        assertNull(natives.test.objc.pingInterfaceN(null))
    }

}
