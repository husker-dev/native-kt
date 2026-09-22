<div>
  <img width="150" src="./.github/logo-rect.svg" alt="logo" align="left">
  <div>
        <h3>native-kt</h3>
        Gradle plugin for convenient C/C++/Rust integration into a Kotlin Multiplatform project. 
        <br>
        Supports JVM, Android, Native, Wasm and JS targets
  </div>
</div>
<br>

<a href="LICENSE"><img src="https://flat.badgen.net/github/license/husker-dev/native-kt?color=grey"></a>
<a href="https://github.com/husker-dev/native-kt/releases/latest"><img src="https://flat.badgen.net/github/release/husker-dev/native-kt"></a>

## Docs
https://native-kt.com/

## JVM Benchmark

Tested on Apple M4 Pro (GraalVM 23):
- [BoltFFI](https://github.com/boltffi/boltffi)
- [Gobley/UniFFI](https://github.com/gobley/gobley)
- native-kt (this project)
- JVM

Cases:
- Empty function
- Sum of two integers
- Pass string

```agsl
Benchmark                         Mode  Cnt     Score      Error  Units
boltffi_empty                     avgt    5     2,253 ±    0,012  ns/op
boltffi_add                       avgt    5     2,332 ±    0,618  ns/op
boltffi_string                    avgt    5    78,223 ±    0,334  ns/op

gobley_empty                      avgt    5  2490,096 ±  684,183  ns/op
gobley_add                        avgt    5  2951,996 ± 2526,199  ns/op
gobley_string                     avgt    5  7155,289 ± 3574,407  ns/op

nativekt_foreign_empty            avgt    5     3,408 ±    0,223  ns/op
nativekt_foreign_add              avgt    5     3,753 ±    0,022  ns/op
nativekt_foreign_string           avgt    5    26,302 ±    0,872  ns/op

nativekt_foreign_critical_empty   avgt    5     3,000 ±    0,014  ns/op
nativekt_foreign_critical_add     avgt    5     3,254 ±    0,019  ns/op
nativekt_foreign_critical_string  avgt    5     7,403 ±    0,183  ns/op

nativekt_jni_empty                avgt    5     2,499 ±    0,006  ns/op
nativekt_jni_add                  avgt    5     2,522 ±    0,092  ns/op
nativekt_jni_string               avgt    5    83,313 ±    0,808  ns/op
nativekt_jni_critical_string      avgt    5    62,610 ±    0,813  ns/op

nativekt_jvmci_empty              avgt    5     1,249 ±    0,011  ns/op
nativekt_jvmci_add                avgt    5     1,254 ±    0,021  ns/op
nativekt_jvmci_string             avgt    5     2,998 ±    0,044  ns/op

jvm_add                           avgt    5     0,348 ±    0,116  ns/op
```
