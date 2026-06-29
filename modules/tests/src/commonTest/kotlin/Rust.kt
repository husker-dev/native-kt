import kotlinx.coroutines.test.runTest
import natives.testrs.loadLibTestrs
import natives.testrs.test_func
import kotlin.test.Test


class Rust {

    @Test
    fun test() = runTest {
        loadLibTestrs()

        println(test_func("Hello world! Привет!", 1))
        test_func(null, 2)
    }
}