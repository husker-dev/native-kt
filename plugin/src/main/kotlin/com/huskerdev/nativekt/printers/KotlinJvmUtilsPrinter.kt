package com.huskerdev.nativekt.printers


import java.io.File

class KotlinJvmUtilsPrinter(
    root: File,
) {

    init {
        File(root, "natives/util/NativesUtil.kt").apply {
            parentFile.mkdirs()
        }.writeText($$"""
            package natives.util

            import java.nio.file.Files
            import java.nio.file.StandardCopyOption
            
            
            object NativesUtil {
            
                val supportsForeign: Boolean = try {
                    Class.forName("java.lang.foreign.SymbolLookup")
                    true
                } catch (_: Exception) {
                    false
                }
            
                fun loadLib(name: String){
                    val os = System.getProperty("os.name", "generic").lowercase()
                    val extension = when {
                        "mac" in os || "darwin" in os -> "dylib"
                        "win" in os -> "dll"
                        "nux" in os -> "so"
                        else -> throw UnsupportedOperationException("Unsupported platform")
                    }
                    val fileName = "$name.$extension"
                    
                    val tempDir = Files.createTempDirectory("kmp-natives")
                    tempDir.toFile().deleteOnExit()
            
                    val dllPath = tempDir.resolve(fileName)
            
                    val classLoader = Thread.currentThread().contextClassLoader ?: ClassLoader.getSystemClassLoader()
            
                    classLoader.getResourceAsStream(fileName).use { input ->
                        Files.copy(input!!, dllPath, StandardCopyOption.REPLACE_EXISTING)
                    }
            
                    System.load(dllPath.toAbsolutePath().toString())
                }
            }
        """.trimIndent())

        File(root, "natives/util/ForeignUtil.kt").apply {
            parentFile.mkdirs()
        }.writeText("""
            package natives.util

            import java.lang.foreign.AddressLayout
            import java.lang.foreign.FunctionDescriptor
            import java.lang.foreign.Linker
            import java.lang.foreign.MemoryLayout
            import java.lang.foreign.SymbolLookup
            import java.lang.foreign.ValueLayout
            import java.lang.invoke.MethodHandle
            
            object ForeignUtils {
                private val lookup = SymbolLookup.loaderLookup()
            
                val _char = type("char")
                val _int = type("int")
                val _longlong = type("long long")
                val _pointer = (type("void*") as AddressLayout)
                    .withTargetLayout(MemoryLayout.sequenceLayout(Long.MAX_VALUE, _char))
            
                private fun type(name: String) =
                    Linker.nativeLinker().canonicalLayouts()[name] as ValueLayout
            
                fun lookup(name: String, retType: ValueLayout, vararg argTypes: ValueLayout): MethodHandle {
                    return Linker.nativeLinker().downcallHandle(
                        lookup.findOrThrow(name),
                        FunctionDescriptor.of(retType, *argTypes)
                    )
                }
            }
        """.trimIndent())
    }

}