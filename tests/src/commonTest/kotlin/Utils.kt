import kotlinx.coroutines.test.runTest
import natives.test.loadLibTest

fun withLib(block: () -> Unit) = runTest {
    loadLibTest()
    block()
}