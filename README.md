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

## Benchmark

Tested on Apple M4 Pro:
- BoltFFI
- Gobley/UniFFI
- native-kt (this project)
- JVM

Cases:
- Empty function
- Sum of two integers
- Pass string

```agsl
Benchmark                         Mode  Cnt     Score      Error  Units
boltffi_empty                     avgt    5     2,245 ±    0,003  ns/op
boltffi_add                       avgt    5     2,246 ±    0,007  ns/op
boltffi_string                    avgt    5    77,523 ±    0,981  ns/op

gobley_empty                      avgt    5  2594,781 ±  649,496  ns/op
gobley_add                        avgt    5  2773,574 ± 1258,296  ns/op
gobley_string                     avgt    5  7089,865 ± 1244,831  ns/op

nativekt_foreign_empty            avgt    5     3,379 ±    0,015  ns/op
nativekt_foreign_add              avgt    5     3,750 ±    0,008  ns/op
nativekt_foreign_string           avgt    5    26,740 ±    0,260  ns/op

nativekt_foreign_critical_empty   avgt    5     2,999 ±    0,005  ns/op
nativekt_foreign_critical_add     avgt    5     3,246 ±    0,015  ns/op
nativekt_foreign_critical_string  avgt    5     7,617 ±    0,047  ns/op

nativekt_jni_empty                avgt    5     2,494 ±    0,016  ns/op
nativekt_jni_add                  avgt    5     2,606 ±    0,046  ns/op
nativekt_jni_string               avgt    5   231,412 ±    3,335  ns/op
nativekt_jni_critical_string      avgt    5   210,009 ±    3,698  ns/op

nativekt_jvmci_empty              avgt    5     1,240 ±    0,005  ns/op
nativekt_jvmci_add                avgt    5     1,247 ±    0,012  ns/op
nativekt_jvmci_string             avgt    5     2,836 ±    0,151  ns/op

jvm_add                           avgt    5     0,348 ±    0,114  ns/op
```
