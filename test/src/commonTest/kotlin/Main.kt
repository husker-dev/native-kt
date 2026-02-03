
import kotlinx.coroutines.test.runTest
import natives.test.loadLibTest
import natives.test.testFunc
import kotlin.test.Test


class JvmTests {

    private fun withLib(block: suspend () -> Unit) = runTest {
        loadLibTest()
        block()
    }

    @Test
    fun test() = withLib {
        testFunc(1, "asd")
    }
}