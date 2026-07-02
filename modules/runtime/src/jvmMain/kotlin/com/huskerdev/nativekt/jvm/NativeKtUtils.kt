package com.huskerdev.nativekt.jvm

import com.huskerdev.nativekt.Arch
import com.huskerdev.nativekt.OS
import sun.misc.Unsafe
import java.io.File
import java.io.IOException
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.nio.file.Files
import java.util.*

/**
 * Helper class for native-kt.
 */
object NativeKtUtils {

    enum class Invoker {
        JNI,
        FOREIGN
    }

    private var unsafe: Unsafe? = null
    private var putBooleanMethod: Method? = null
    private var firstFieldOffset = 0L

    private var findNativeMethod: Method? = null
    private var findNativeMethod1: Method? = null

    /***
     * @return true if can use "addExports" function
     */
    fun isAutoExportEnabled(): Boolean =
        System.getProperty("nativekt.jvm.autoExport", "true") != "false" &&
        !System.getProperty("java.version").startsWith("1.")


    /***
     * @return true if "Foreign Function and Memory API" is supported in JVM
     */
    fun isForeignAvailable(): Boolean {
        try {
            Class.forName("java.lang.foreign.Linker")
            return true
        } catch (_: ClassNotFoundException) {
            return false
        }
    }

    /***
     * @return true if JVMCI is supported in JVM
     */
    fun isJVMCIAvailable(): Boolean {
        if(System.getProperty("nativekt.jvm.disableJVMCI", "false") == "true")
            return false
        try {
            Class.forName("jdk.vm.ci.runtime.JVMCI")
            val arch = Arch.current
            val os = OS.current()

            return arch == Arch.ARM64 || (arch == Arch.X64 && os != OS.MACOS)
        } catch (_: ClassNotFoundException) {
            return false
        }
    }

    /**
     * Retrieves native invoker type that is supported in the current JVM.
     * @return Native invoker type
     */
    fun getInvoker(): Invoker {
        val forced = System.getProperty("nativekt.jvm.forceInvoker")
        return when {
            forced != null -> {
                Invoker.entries.firstOrNull {
                    it.name.lowercase(Locale.US) == forced
                } ?: throw UnsupportedOperationException("Unknown forced native invoker: $forced")
            }
            isForeignAvailable() -> Invoker.FOREIGN
            else -> Invoker.JNI
        }
    }

    /**
     *
     * @param baseName base library name, without prefix, extension and arch (e.g. 'some')
     * @param macosUniversal indicates if macOS lib is universal (fat)
     * @return Full library file path (e.g. C:/libsome-x64.dll)
     * @throws IOException If file not found in resources
     */
    fun resolveLibraryFile(baseName: String, macosUniversal: Boolean): String {

        // Get OS
        val os = OS.current()

        // Get lib arch
        val arch = if(macosUniversal && os == OS.MACOS)
            "universal"
        else Arch.current.name.lowercase(Locale.US)

        // Construct full lib file name
        val fileName = "lib$baseName-$arch.${os.dylibExtension}"

        // Create tmp dir
        val tempDir = Files.createTempDirectory("natives-kt").toFile()
        val libPath = File(tempDir, fileName)
        libPath.deleteOnExit()
        tempDir.deleteOnExit()

        // Copy lib from resources
        val classLoader = Thread.currentThread().getContextClassLoader()
            ?: ClassLoader.getSystemClassLoader()

        classLoader.getResourceAsStream(fileName)?.use { inp ->
            libPath.outputStream().use { out ->
                inp.copyTo(out)
            }
        } ?: throw NullPointerException("File '$fileName' was not found in resources")

        return libPath.absolutePath
    }

    /**
     * Alternative to this JVM argument:<br>
     * <code>
     *     --add-exports jdk.internal.vm.ci/jdk.vm.ci.code=ALL-UNNAMED
     * </code>
     *
     * @param forModule Target module
     * @param ofModule Source module
     * @param paths Class paths to be accessible
     */
    fun addExports(forModule: String, ofModule: String, paths: Array<String>){
        val forModuleOpt = ModuleLayer.boot().findModule(forModule)
        if(!forModuleOpt.isPresent)
            return
        addExports(forModuleOpt.get(), ofModule, paths)
    }

    /**
     * Alternative to this JVM argument:<br>
     * <code>
     *     --add-exports jdk.internal.vm.ci/jdk.vm.ci.code=ALL-UNNAMED
     * </code>
     *
     * @param forModule Target module
     * @param ofModule Source module
     * @param paths Class paths to be accessible
     */
    fun addExports(forModule: Module, ofModule: String, paths: Array<String>){
        try {
            val moduleOpt = ModuleLayer.boot().findModule(ofModule)
                .orElseThrow { NullPointerException("Module '$ofModule' is not presented") }

            val addOpensMethodImpl = Module::class.java.getDeclaredMethod(
                "implAddExports",
                String::class.java, Module::class.java
            )
            setAccessible(addOpensMethodImpl)

            paths.forEach { pkg ->
                addOpensMethodImpl.invoke(moduleOpt, pkg, forModule)
            }
        } catch (e: Throwable) {
            throw UnsupportedOperationException("Could not add exports of '$ofModule' to '$forModule'", e)
        }
    }

    /**
     * Returns native function address from loaded libraries
     * @param funcName Function name
     * @return Native address
     * @throws Exception When Unsafe or reflection is not available
     */
    fun findAddress(funcName: String): Long {
        if(findNativeMethod == null && findNativeMethod1 == null) {
            try {
                findNativeMethod = ClassLoader::class.java.getDeclaredMethod(
                    "findNative",
                    ClassLoader::class.java, Class::class.java, String::class.java, String::class.java
                )
                setAccessible(findNativeMethod!!)
            } catch (_: Exception) {
                findNativeMethod1 = ClassLoader::class.java.getDeclaredMethod(
                    "findNative",
                    ClassLoader::class.java, String::class.java
                )
                setAccessible(findNativeMethod1!!)
            }
        }
        return if(findNativeMethod != null) {
            findNativeMethod!!.invoke(
                null,
                NativeKtUtils::class.java.getClassLoader(),
                NativeKtUtils::class,
                funcName,
                funcName
            ) as Long
        } else {
             findNativeMethod1!!.invoke(
                null,
                NativeKtUtils::class.java.getClassLoader(),
                funcName
            ) as Long
        }
    }

    /**
     * Set accessible flag to true
     * @param obj AccessibleObject to access
     * @throws Exception When Unsafe or reflection is not available
     */
    fun setAccessible(obj: AccessibleObject) {
        // Init unsafe and methods
        if(unsafe == null) {
            val unsafeClass = Class.forName("sun.misc.Unsafe")

            val unsafeField = unsafeClass.getDeclaredField("theUnsafe")
            unsafeField.trySetAccessible()
            unsafe = unsafeField[null] as Unsafe

            putBooleanMethod = unsafeClass.getDeclaredMethod(
                "putBoolean",
                Any::class.java, Long::class.java, Boolean::class.java
            )

            firstFieldOffset = unsafeClass.getDeclaredMethod(
                "objectFieldOffset",
                Field::class.java
            ).invoke(unsafe, OffsetProvider::class.java.getDeclaredField("first")) as Long
        }

        // Invoke
        putBooleanMethod!!.invoke(unsafe, obj, firstFieldOffset, true)
    }
}

private class OffsetProvider {
    @Suppress("unused")
    val first: Int = 0
}
