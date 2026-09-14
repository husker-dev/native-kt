import kotlinx.coroutines.test.runTest
import natives.test.loadLibTest
import natives.testcpp.loadLibTestcpp
import natives.testrs.loadLibTestrs

fun withCLib(block: suspend () -> Unit) = runTest {
    loadLibTest()
    block()
}

fun withCppLib(block: suspend () -> Unit) = runTest {
    loadLibTestcpp()
    block()
}

fun withRustLib(block: suspend () -> Unit) = runTest {
    loadLibTestrs()
    block()
}