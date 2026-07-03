import kotlinx.coroutines.test.runTest
import natives.test.loadLibTest
import natives.testrs.loadLibTestrs

fun withCLib(block: () -> Unit) = runTest {
    loadLibTest()
    block()
}

fun withRustLib(block: () -> Unit) = runTest {
    loadLibTestrs()
    block()
}