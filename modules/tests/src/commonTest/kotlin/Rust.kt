import kotlinx.coroutines.test.runTest
import natives.testrs.loadLibTestrs
import natives.testrs.test_func
import kotlin.test.Test


class Rust {

    @Test
    fun test() = runTest {
        loadLibTestrs()

        test_func("Hello World!")
        test_func(null)
    }
}