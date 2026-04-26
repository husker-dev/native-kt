import kotlinx.coroutines.test.runTest
import natives.jvmOnlyTest.loadLibJvmOnlyTest
import kotlin.test.Test

class Test {

    @Test
    fun helloWorld() = runTest {
        loadLibJvmOnlyTest()
        natives.jvmOnlyTest.helloWorld()
    }
}