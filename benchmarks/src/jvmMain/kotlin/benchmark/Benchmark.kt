package benchmark

import kotlinx.benchmark.*
import natives.foreignBindings.callCriticalForeign
import natives.foreignBindings.callCriticalForeignAdd
import natives.foreignBindings.callCriticalForeignString
import natives.foreignBindings.callForeign
import natives.foreignBindings.callForeignAdd
import natives.foreignBindings.callForeignString
import natives.foreignBindings.loadLibForeignBindingsSync
import natives.jniBindings.callCriticalJni
import natives.jniBindings.callCriticalJniString
import natives.jniBindings.callJni
import natives.jniBindings.callJniAdd
import natives.jniBindings.callJniString
import natives.jniBindings.loadLibJniBindingsSync
import natives.jvmciBindings.callCriticalJVMCI
import natives.jvmciBindings.callCriticalJVMCIAdd
import natives.jvmciBindings.callCriticalJVMCIString
import natives.jvmciBindings.loadLibJvmciBindingsSync
import java.util.concurrent.ThreadLocalRandom

fun main() {
    loadLibJvmciBindingsSync()
    callCriticalJVMCIAdd(2, 6)
}

@State(Scope.Benchmark)
@Suppress("unused")
open class NativeKtBenchmark {

    var a: Int = 0
    var b: Int = 0

    @Setup
    open fun prepare() {
        a = ThreadLocalRandom.current().nextInt()
        b = ThreadLocalRandom.current().nextInt()

        loadLibJvmciBindingsSync()
        System.setProperty("nativekt.jvm.disableJVMCI", "true")

        System.setProperty("nativekt.jvm.forceInvoker", "jni")
        loadLibJniBindingsSync()

        System.setProperty("nativekt.jvm.forceInvoker", "foreign")
        loadLibForeignBindingsSync()
    }

    @Benchmark
    open fun jvmAdd(): Int =
        a + b

    @Benchmark
    open fun jni() =
        callJni()

    @Benchmark
    open fun jniAdd() =
        callJniAdd(a, b)

    @Benchmark
    open fun jniString() =
        callJniString("test")

    @Benchmark
    open fun foreign() =
        callForeign()

    @Benchmark
    open fun foreignAdd() =
        callForeignAdd(a, b)

    @Benchmark
    open fun foreignString() =
        callForeignString("test")

    // Critical

    @Benchmark
    open fun criticalJni() =
        callCriticalJni()

    @Benchmark
    open fun criticalJniString() =
        callCriticalJniString("test")

    @Benchmark
    open fun criticalForeign() =
        callCriticalForeign()

    @Benchmark
    open fun criticalForeignAdd() =
        callCriticalForeignAdd(a, b)

    @Benchmark
    open fun criticalForeignString() =
        callCriticalForeignString("test")

    @Benchmark
    open fun criticalJVMCI() =
        callCriticalJVMCI()

    @Benchmark
    open fun criticalJVMCIAdd() =
        callCriticalJVMCIAdd(a, b)

    @Benchmark
    open fun criticalJVMCIString() =
        callCriticalJVMCIString("test")

}