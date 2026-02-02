
import android.util.Log
import kotlinx.coroutines.test.runTest
import natives.test.loadLibTest
import natives.test.testFunc
import kotlin.test.Test


class Tests {

    private fun withLib(block: suspend () -> Unit) = runTest {
        loadLibTest()
        block()
    }

    @Test
    fun test() = withLib {
        Log.v("native-kt", "start")
        val result = testFunc(1, "asd")
        Log.v("native-kt", "result: $result")
    }
}