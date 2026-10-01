import kotlinx.coroutines.test.runTest
import natives.jvmOnlyTest.MyDictionary
import natives.jvmOnlyTest.loadLibJvmOnlyTest
import kotlin.test.Test

class Test {

    @Test
    fun helloWorld() = runTest {
        loadLibJvmOnlyTest()

        MyDictionary(123)
        natives.jvmOnlyTest.helloWorld()
    }
}