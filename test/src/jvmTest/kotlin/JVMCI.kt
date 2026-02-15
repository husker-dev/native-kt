import com.huskerdev.nativekt.NativeKtUtils
import kotlinx.coroutines.test.runTest
import natives.test.jvmci1
import natives.test.jvmci10
import natives.test.jvmci11
import natives.test.jvmci12
import natives.test.jvmci13
import natives.test.jvmci14
import natives.test.jvmci15
import natives.test.jvmci2
import natives.test.jvmci3
import natives.test.jvmci4
import natives.test.jvmci5
import natives.test.jvmci6
import natives.test.jvmci7
import natives.test.jvmci8
import natives.test.jvmci9
import natives.test.loadLibTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail


class JVMCI {

    fun withLib(block: () -> Unit) = runTest {
        if(!NativeKtUtils.isJvmciAvailable())
            fail("JVMCI is unavailable")
        loadLibTest()
        block()
    }


    @Test
    fun jvmciEmpty() = withLib {
        assertTrue(jvmci1())
    }

    @Test
    fun jvmci1Arg() = withLib {
        assertTrue(jvmci2(1))
    }

    @Test
    fun jvmci2Args() = withLib {
        assertTrue(jvmci3(1, 2))
    }

    @Test
    fun jvmciALotSameIntArgs() = withLib {
        assertTrue(jvmci4(1, 2, 3, 4, 5, 6, 7, 8, 9))
    }

    @Test
    fun jvmciALotDifIntArgs() = withLib {
        assertTrue(jvmci5(1, 2, 3, 4, 5, 6, 7, 8, 9))
    }

    @Test
    fun jvmciALotSameFloatingArgs() = withLib {
        assertTrue(jvmci6(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10, 11, 12, 13))
    }

    @Test
    fun jvmciALotDifFloatingArgs() = withLib {
        assertTrue(jvmci7(1f, 2.0, 3f, 4.0, 5f, 6.0, 7f, 8f, 9.0))
    }

    @Test
    fun jvmciAllPrimitiveTypesArgs() = withLib {
        assertTrue(jvmci8(1, 2.0, 3f, 4))
    }

    @Test
    fun jvmciALotAllTypesArgs() = withLib {
        assertTrue(jvmci9(1, 2.0, 3f, 4, 5, 6.0, 7f, 8f, 9))
    }

    @Test
    fun jvmciAllTypesArgs() = withLib {
        assertTrue(jvmci10("string1", 2.0, 3f, 4, 5, 6.0, "string7", 8f, 9))
    }

    @Test
    fun jvmciIterate() = withLib {
        assertTrue(jvmci11(1f, 2, 3f, 4, 5f, 6, 7f, 8, 9f, 10, 11f, 12, 13f, 14, 15f, 16, 17f))
    }

    @Test
    fun jvmciReturnsInt() = withLib {
        assertEquals(1, jvmci12())
    }

    @Test
    fun jvmciReturnsLong() = withLib {
        assertEquals(1, jvmci13())
    }

    @Test
    fun jvmciReturnsFloat() = withLib {
        assertEquals(1.5f, jvmci14())
    }

    @Test
    fun jvmciReturnsDouble() = withLib {
        assertEquals(1.5, jvmci15())
    }
}