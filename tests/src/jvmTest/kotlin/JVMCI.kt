import com.huskerdev.nativekt.NativeKtUtils
import natives.test.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail


class JVMCI {

    fun withJVMCI(block: () -> Unit) {
        if(!NativeKtUtils.isJvmciAvailable())
            fail("JVMCI is unavailable")

        return withLib(block)
    }

    @Test
    fun jvmciEmpty() = withJVMCI {
        assertTrue(jvmci1())
    }

    @Test
    fun jvmci1Arg() = withJVMCI {
        assertTrue(jvmci2(1))
    }

    @Test
    fun jvmci2Args() = withJVMCI {
        assertTrue(jvmci3(1, 2))
    }

    @Test
    fun jvmciALotSameIntArgs() = withJVMCI {
        assertTrue(jvmci4(1, 2, 3, 4, 5, 6, 7, 8, 9))
    }

    @Test
    fun jvmciALotDifIntArgs() = withJVMCI {
        assertTrue(jvmci5(1, 2, 3, 4, 5, 6, 7, 8, 9))
    }

    @Test
    fun jvmciALotSameFloatingArgs() = withJVMCI {
        assertTrue(jvmci6(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10, 11, 12, 13))
    }

    @Test
    fun jvmciALotDifFloatingArgs() = withJVMCI {
        assertTrue(jvmci7(1f, 2.0, 3f, 4.0, 5f, 6.0, 7f, 8f, 9.0))
    }

    @Test
    fun jvmciAllPrimitiveTypesArgs() = withJVMCI {
        assertTrue(jvmci8(1, 2.0, 3f, 4))
    }

    @Test
    fun jvmciALotAllTypesArgs() = withJVMCI {
        assertTrue(jvmci9(1, 2.0, 3f, 4, 5, 6.0, 7f, 8f, 9))
    }

    @Test
    fun jvmciAllTypesArgs() = withJVMCI {
        assertTrue(jvmci10("string1", 2.0, 3f, 4, 5, 6.0, "string7", 8f, 9))
    }

    @Test
    fun jvmciIterate() = withJVMCI {
        assertTrue(jvmci11(1f, 2, 3f, 4, 5f, 6, 7f, 8, 9f, 10, 11f, 12, 13f, 14, 15f, 16, 17f))
    }

    @Test
    fun jvmciReturnsInt() = withJVMCI {
        assertEquals(1, jvmci12())
    }

    @Test
    fun jvmciReturnsLong() = withJVMCI {
        assertEquals(1, jvmci13())
    }

    @Test
    fun jvmciReturnsFloat() = withJVMCI {
        assertEquals(1.5f, jvmci14())
    }

    @Test
    fun jvmciReturnsDouble() = withJVMCI {
        assertEquals(1.5, jvmci15())
    }
}