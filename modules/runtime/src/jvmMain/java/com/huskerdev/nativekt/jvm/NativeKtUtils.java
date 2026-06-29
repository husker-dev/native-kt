package com.huskerdev.nativekt.jvm;

import com.huskerdev.nativekt.Arch;
import com.huskerdev.nativekt.OS;
import sun.misc.Unsafe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Helper class for native-kt.
 */
public class NativeKtUtils {

    public enum Invoker {
        JNI,
        FOREIGN
    }

    private static Unsafe unsafe;
    private static Method putBooleanMethod;
    private static long firstFieldOffset;

    private static Method findNativeMethod, findNativeMethod1;

    /***
     * @return true if can use "addExports" function
     */
    public static boolean isAutoExportEnabled(){
        return !System.getProperty("nativekt.jvm.autoExport", "true").equals("false") &&
                !System.getProperty("java.version").startsWith("1.");
    }

    /***
     * @return true if "Foreign Function and Memory API" is supported in JVM
     */
    public static boolean isForeignAvailable(){
        try {
            Class.forName("java.lang.foreign.Linker");
            return true;
        } catch (ClassNotFoundException ignored) {}
        return false;
    }

    /***
     * @return true if JVMCI is supported in JVM
     */
    public static boolean isJVMCIAvailable(){
        if(Objects.equals(System.getProperty("nativekt.jvm.disableJVMCI", "false"), "true"))
            return false;
        try {
            Class.forName("jdk.vm.ci.runtime.JVMCI");
            Arch arch = Arch.current();

            boolean isX86 = arch == Arch.X86;
            boolean isRISCV64 = arch == Arch.RISCV64;
            boolean isX64macOS = arch == Arch.X64 && OS.current() == OS.MACOS;

            return !isX86 && !isRISCV64 && !isX64macOS;
        } catch (ClassNotFoundException ignored) {}
        return false;
    }

    /**
     * Retrieves native invoker type that is supported in the current JVM.
     * @return Native invoker type
     */
    public static Invoker getInvoker(){
        String forced = System.getProperty("nativekt.jvm.forceInvoker");
        if(forced != null) {
            for(Invoker invoker : Invoker.values()) {
                if (invoker.name().toLowerCase(Locale.US).equals(forced))
                    return invoker;
            }
            throw new UnsupportedOperationException("Unknown native invoker: " + forced);
        }

        if(isForeignAvailable())
            return Invoker.FOREIGN;
        return Invoker.JNI;
    }

    /**
     *
     * @param baseName base library name, without prefix, extension and arch (e.g. 'some')
     * @param macosUniversal indicates if macOS lib is universal (fat)
     * @return Full library file path (e.g. C:/libsome-x64.dll)
     * @throws IOException If file not found in resources
     */
    public static String resolveLibraryFile(String baseName, boolean macosUniversal) throws IOException {

        // Get OS
        OS os = OS.current();

        // Get lib arch
        String arch = (macosUniversal && os == OS.MACOS) ?
                "universal" : Arch.current().name().toLowerCase(Locale.US);

        // Construct full lib file name
        String fileName = "lib" + baseName + "-" + arch + "." + os.getDylibExtension();

        // Create tmp dir
        File tempDir = Files.createTempDirectory("natives-kt").toFile();
        File libPath = new File(tempDir, fileName);
        libPath.deleteOnExit();
        tempDir.deleteOnExit();

        // Copy lib from resources
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if(classLoader == null)
            classLoader = ClassLoader.getSystemClassLoader();

        try(InputStream input = classLoader.getResourceAsStream(fileName)) {
            if(input == null)
                throw new NullPointerException("File '" + fileName + "' was not found in resources");
            Files.copy(input, libPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return libPath.getAbsolutePath();
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
    public static void addExports(String forModule, String ofModule, String[] paths){
        Optional<Module> forModuleOpt = ModuleLayer.boot().findModule(forModule);
        if(!forModuleOpt.isPresent())
            return;
        addExports(forModuleOpt.get(), ofModule, paths);
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
    public static void addExports(Module forModule, String ofModule, String[] paths){
        try {
            Module moduleOpt = ModuleLayer.boot().findModule(ofModule)
                .orElseThrow(() -> new NullPointerException("Module '" + ofModule + "' is not presented"));

            Method addOpensMethodImpl = Module.class.getDeclaredMethod("implAddExports", String.class, Module.class);
            setAccessible(addOpensMethodImpl);

            for(String pkg : paths)
                addOpensMethodImpl.invoke(moduleOpt, pkg, forModule);

        } catch (Throwable e) {
            throw new UnsupportedOperationException("Could not add exports of '" + ofModule + "' to '" + forModule + "'", e);
        }
    }

    /**
     * Returns native function address from loaded libraries
     * @param funcName Function name
     * @return Native address
     * @throws Exception When Unsafe or reflection is not available
     */
    public static long findAddress(String funcName) throws Exception {
        if(findNativeMethod == null && findNativeMethod1 == null) {
            try {
                findNativeMethod = ClassLoader.class.getDeclaredMethod("findNative", ClassLoader.class, Class.class, String.class, String.class);
                setAccessible(findNativeMethod);
            } catch (Exception e) {
                findNativeMethod1 = ClassLoader.class.getDeclaredMethod("findNative", ClassLoader.class, String.class);
                setAccessible(findNativeMethod1);
            }
        }
        if(findNativeMethod != null) {
            return (long) findNativeMethod.invoke(
                    null,
                    NativeKtUtils.class.getClassLoader(),
                    NativeKtUtils.class,
                    funcName,
                    funcName
            );
        } else {
            return (long) findNativeMethod1.invoke(
                    null,
                    NativeKtUtils.class.getClassLoader(),
                    funcName
            );
        }
    }

    /**
     * Set accessible flag to true
     * @param obj AccessibleObject to access
     * @throws Exception When Unsafe or reflection is not available
     */
    public static void setAccessible(AccessibleObject obj) throws Exception {
        // Init unsafe and methods
        if(unsafe == null) {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");

            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.trySetAccessible();
            unsafe = (Unsafe) unsafeField.get(null);

            putBooleanMethod = unsafeClass.getDeclaredMethod("putBoolean", Object.class, long.class, boolean.class);

            firstFieldOffset = (long) unsafeClass.getDeclaredMethod("objectFieldOffset", Field.class)
                    .invoke(unsafe, OffsetProvider.class.getDeclaredField("first"));
        }

        // Invoke
        putBooleanMethod.invoke(unsafe, obj, firstFieldOffset, true);
    }

    private static class OffsetProvider {
        int first;
    }
}
