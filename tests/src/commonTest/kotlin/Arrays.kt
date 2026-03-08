import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

class Arrays {
    @Test
    fun passArray() = withLib {
        assertTrue(natives.test.passArray(intArrayOf(1, 2)))
    }

    @Test
    fun returnArray() = withLib {
        assertContentEquals(intArrayOf(1, 2), natives.test.returnArray())
    }

    @Test
    fun pingArray() = withLib {
        val array = intArrayOf(1, 2)
        assertContentEquals(array, natives.test.pingArray(array))
    }
}