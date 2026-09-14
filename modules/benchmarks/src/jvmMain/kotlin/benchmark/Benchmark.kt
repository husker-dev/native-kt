@file:Suppress("FunctionName")

package benchmark

import com.example.boltFFI.BoltFFI
import com.huskerdev.*
import kotlinx.benchmark.*
import natives.foreignBindings.*
import natives.jniBindings.*
import natives.jvmciBindings.*
import java.util.concurrent.ThreadLocalRandom

@State(Scope.Benchmark)
@Suppress("unused")
open class NativeKtBenchmark {

    private var a: Int = 0
    private var b: Int = 0

    @Setup
    open fun prepare() {
        println("JVM: ${System.getProperty("java.vendor.version")}")
        a = ThreadLocalRandom.current().nextInt()
        b = ThreadLocalRandom.current().nextInt()

        loadLibJvmciBindingsSync()
        System.setProperty("nativekt.jvm.disableJVMCI", "true")

        System.setProperty("nativekt.jvm.forceInvoker", "jni")
        loadLibJniBindingsSync()

        System.setProperty("nativekt.jvm.forceInvoker", "foreign")
        loadLibForeignBindingsSync()

        uniffiEnsureInitialized()

        // Call once to initialize
        BoltFFI.empty()
    }

    // Java

    @Benchmark
    open fun jvm_add(): Int =
        a + b

    // native-kt (JNI)

    @Benchmark
    open fun nativekt_jni_empty() =
        callJni()

    @Benchmark
    open fun nativekt_jni_add() =
        callJniAdd(a, b)

    @Benchmark
    open fun nativekt_jni_string() =
        callJniString("test")

    @Benchmark
    open fun nativekt_jni_critical_string() =
        callCriticalJniString("test")

    // native-kt (Foreign)

    @Benchmark
    open fun nativekt_foreign_empty() =
        callForeign()

    @Benchmark
    open fun nativekt_foreign_add() =
        callForeignAdd(a, b)

    @Benchmark
    open fun nativekt_foreign_string() =
        callForeignString("test")

    @Benchmark
    open fun nativekt_foreign_critical_empty() =
        callCriticalForeign()

    @Benchmark
    open fun nativekt_foreign_critical_add() =
        callCriticalForeignAdd(a, b)

    @Benchmark
    open fun nativekt_foreign_critical_string() =
        callCriticalForeignString("test")

    // native-kt (JVMCI)

    @Benchmark
    open fun nativekt_jvmci_empty() =
        callCriticalJvmci()

    @Benchmark
    open fun nativekt_jvmci_add() =
        callCriticalJvmciAdd(a, b)

    @Benchmark
    open fun nativekt_jvmci_string() =
        callCriticalJvmciString("test")

    // Gobley/UniFFI

    @Benchmark
    open fun gobley_empty() =
        empty()

    @Benchmark
    open fun gobley_add() =
        addNumbers(a, b)

    @Benchmark
    open fun gobley_string() =
        passString("test")

    // BoltFFI

    @Benchmark
    open fun boltffi_empty() =
        BoltFFI.empty()

    @Benchmark
    open fun boltffi_add() =
        BoltFFI.addNumbers(a, b)

    @Benchmark
    open fun boltffi_string() =
        BoltFFI.passString("test")
}