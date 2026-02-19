package com.huskerdev.nativekt;

import sun.misc.Unsafe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

    public enum OS {
        MACOS("dylib"),
        WINDOWS("dll"),
        LINUX("so")
        ;

        private final String extension;
        OS(String extension) {
            this.extension = extension;
        }

        public static OS current() {
            String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.US);
            if(osName.contains("mac") || osName.contains("darwin"))
                return MACOS;
            if(osName.contains("win"))
                return WINDOWS;
            if(osName.contains("nux"))
                return LINUX;
            throw new UnsupportedOperationException("Unsupported OS");
        }
    }

    public enum Arch {
        X86,
        X64,
        ARM32,
        ARM64,
        RISCV32,
        RISCV64
        ;

        public static Arch current() {
            String name = System.getProperty("os.arch").toLowerCase(Locale.US);
            if(name.matches("^(x8632|x86|i[3-6]86|ia32|x32)$"))
                return X86;
            if(name.matches("^(x8664|amd64|ia32e|em64t|x64)$"))
                return X64;
            if(name.matches("^(arm|arm32)$"))
                return ARM32;
            if(name.equals("aarch64"))
                return ARM64;
            if(name.matches("^(riscv|riscv32)$"))
                return RISCV32;
            if(name.equals("riscv64"))
                return RISCV64;
            throw new UnsupportedOperationException("CPU architecture is not supported");
        }
    }

    public enum Invoker {
        JNI,
        FOREIGN
    }

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
        } catch (ClassNotFoundException e) {}
        return false;
    }

    /***
     * @return true if JVMCI is supported in JVM
     */
    public static boolean isJvmciAvailable(){
        if(Objects.equals(System.getProperty("nativekt.jvm.disableJVMCI", "false"), "true"))
            return false;
        try {
            Class.forName("jdk.vm.ci.runtime.JVMCI");
            Arch arch = Arch.current();
            return arch != Arch.X86 && arch != Arch.RISCV64;
        } catch (ClassNotFoundException e) {}
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
     * @return Full library file name (e.g. libsome-x64.dll)
     * @throws IOException If file not found in resources
     */
    public static String loadLibrary(String baseName, boolean macosUniversal) throws IOException {

        // Get OS
        OS os = OS.current();

        // Get lib arch
        String arch = (macosUniversal && os == OS.MACOS) ?
                "universal" : Arch.current().name().toLowerCase(Locale.US);

        // Construct full lib file name
        String fileName = "lib" + baseName + "-" + arch + "." + os.extension;

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

        // Load library
        System.load(libPath.getAbsolutePath());

        return fileName;
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

            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");

            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.trySetAccessible();
            Unsafe unsafe = (Unsafe) unsafeField.get(null);

            long firstFieldOffset = (long) unsafeClass.getDeclaredMethod("objectFieldOffset", Field.class)
                    .invoke(unsafe, OffsetProvider.class.getDeclaredField("first"));

            Method addOpensMethodImpl = Module.class.getDeclaredMethod("implAddExports", String.class, Module.class);

            unsafeClass.getDeclaredMethod("putBoolean", Object.class, long.class, boolean.class)
                    .invoke(unsafe, addOpensMethodImpl, firstFieldOffset, true);

            for(String pkg : paths)
                addOpensMethodImpl.invoke(moduleOpt, pkg, forModule);

        } catch (Throwable e) {
            throw new UnsupportedOperationException("Could not add exports of '" + ofModule + "' to '" + forModule + "'", e);
        }
    }

    private static class OffsetProvider {
        int first;
    }
}
