import kotlinx.coroutines.test.runTest
import natives.test.loadLibTest
import natives.testcpp.loadLibTestcpp
import natives.testrs.loadLibTestrs

fun withCLib(block: () -> Unit) = runTest {
    loadLibTest()
    block()
}

fun withCppLib(block: () -> Unit) = runTest {
    loadLibTestcpp()
    block()
}

fun withRustLib(block: () -> Unit) = runTest {
    loadLibTestrs()
    block()
}