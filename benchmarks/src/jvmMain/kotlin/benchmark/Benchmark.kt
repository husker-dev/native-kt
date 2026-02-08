package benchmark

import kotlinx.benchmark.*
import natives.foreignBindings.loadLibForeignBindingsSync
import natives.jniBindings.loadLibJniBindingsSync


@State(Scope.Benchmark)
@Suppress("unused")
open class MyBenchmark {

    @Setup
    open fun prepare() {
        System.setProperty("nativekt.jvm.forceInvoker", "jni")
        loadLibJniBindingsSync()

        System.setProperty("nativekt.jvm.forceInvoker", "foreign")
        loadLibForeignBindingsSync()
    }

    @Benchmark
    open fun callJni(): Int =
        natives.jniBindings.callJni()

    @Benchmark
    open fun callJniCritical(): Int =
        natives.jniBindings.callJniCritical()

    @Benchmark
    open fun callForeign(): Int =
        natives.foreignBindings.callForeign()

    @Benchmark
    open fun callForeignCritical(): Int =
        natives.foreignBindings.callForeignCritical()
}