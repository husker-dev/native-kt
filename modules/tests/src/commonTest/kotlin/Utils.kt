import com.huskerdev.stdlibx.EnvironmentKind
import kotlinx.coroutines.test.runTest
import natives.test.c.loadLibTestC
import natives.test.cpp.loadLibTestCpp
import natives.test.rust.loadLibTestRust
import natives.test.swift.loadLibTestSwift

fun withCLib(block: suspend () -> Unit) = runTest {
    loadLibTestC()
    block()
}

fun withCppLib(block: suspend () -> Unit) = runTest {
    loadLibTestCpp()
    block()
}

fun withRustLib(block: suspend () -> Unit) = runTest {
    loadLibTestRust()
    block()
}

fun withSwiftLib(block: suspend () -> Unit) = runTest {
    if(EnvironmentKind.current == EnvironmentKind.WEB || EnvironmentKind.current == EnvironmentKind.NODEJS)
        return@runTest
    loadLibTestSwift()
    block()
}