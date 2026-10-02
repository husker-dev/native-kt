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

Tested:
- [BoltFFI](https://github.com/boltffi/boltffi) (v0.31.0)
- [Gobley/UniFFI](https://github.com/gobley/gobley) (v0.29.4)
- native-kt (this project)
- JVM (GraalVM 23)

Cases:
- Empty function
- Sum of two integers
- Pass string

Targets:
- Apple M4 Pro (macOS, arm64)
- Ryzen AI 9 HX 370 (Windows, x86-64)

|                                  | Apple M4 Pro | Ryzen AI 9 HX 370 |
|----------------------------------|-------------:|------------------:|
| boltffi_empty                    |        2,253 |             6,130 |
| boltffi_add                      |        2,332 |             6,180 |
| boltffi_string                   |       78,223 |            34,885 |
| gobley_empty                     |     2490,096 |          4653,434 |
| gobley_add                       |     2951,996 |          4769,567 |
| gobley_string                    |     7155,289 |         11746,325 |
| nativekt_foreign_empty           |        3,408 |             6,678 |
| nativekt_foreign_add             |        3,753 |             6,979 |
| nativekt_foreign_string          |       26,302 |            49,330 |
| nativekt_foreign_critical_empty  |        3,000 |             3,053 |
| nativekt_foreign_critical_add    |        3,254 |             3,066 |
| nativekt_foreign_critical_string |        7,403 |             7,763 |
| nativekt_jni_empty               |        2,499 |             6,112 |
| nativekt_jni_add                 |        2,522 |             6,046 |
| nativekt_jni_string              |       52,576 |            52,273 |
| nativekt_jni_critical_string     |       36,366 |            18,412 |
| nativekt_jvmci_empty             |        1,249 |             1,007 |
| nativekt_jvmci_add               |        1,254 |             1,217 |
| nativekt_jvmci_string            |        2,998 |             2,548 |
| jvm_add                          |        0,348 |             0,269 |
