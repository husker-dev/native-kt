
import natives.jvmNatives.jvmOnlyFunc
import natives.jvmNatives.loadLibJvmNativesSync
import kotlin.test.Test


class Tests {

    @Test
    fun test() {
        loadLibJvmNativesSync()
        jvmOnlyFunc(1, "asd")
    }
}