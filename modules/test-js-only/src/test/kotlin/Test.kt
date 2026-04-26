import kotlinx.coroutines.test.runTest
import natives.jsOnlyTest.loadLibJsOnlyTest
import kotlin.test.Test

class Test {

    @Test
    fun helloWorld() = runTest {
        loadLibJsOnlyTest()
        natives.jsOnlyTest.helloWorld()
    }
}