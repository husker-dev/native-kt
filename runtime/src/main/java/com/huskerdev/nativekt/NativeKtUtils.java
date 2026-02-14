package com.huskerdev.nativekt;

import sun.misc.Unsafe;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class NativeKtUtils {

    public enum Os {
        MACOS("dylib"),
        WINDOWS("dll"),
        LINUX("so");

        final String extension;
        Os(String extension) {
            this.extension = extension;
        }

        public static Os current() {
            String osName = System.getProperty("os.name", "generic").toLowerCase();
            if(osName.contains("mac") || osName.contains("darwin"))
                return Os.MACOS;
            else if(osName.contains("win"))
                return Os.WINDOWS;
            else if(osName.contains("nux"))
                return Os.LINUX;
            else throw new UnsupportedOperationException("Unsupported OS");
        }
    }

    public enum Arch {
        X86,
        X64,
        ARM64,
        RISCV64
        ;

        public static Arch current() {
            String archName = System.getProperty("os.arch").toLowerCase();
            if(archName.contains("riscv"))
                return Arch.RISCV64;
            if(archName.equals("aarch") || archName.contains("arm"))
                return Arch.ARM64;
            else if(archName.equals("amd64"))
                return Arch.X64;
            else
                return Arch.X86;
        }
    }

    public static boolean isAutoExportEnabled(){
        return !System.getProperty("nativekt.jvm.autoExport", "true").equals("false") &&
                !System.getProperty("java.version").startsWith("1.");
    }

    public static boolean isForeignAvailable(){
        try {
            Class.forName("java.lang.foreign.Linker");
            return true;
        } catch (ClassNotFoundException e) {}
        return false;
    }

    public static boolean isJvmciAvailable(){
        try {
            Class.forName("jdk.vm.ci.runtime.JVMCI");
            return Arch.current() != Arch.X86;
        } catch (ClassNotFoundException e) {}
        return false;
    }

    public static String loadLibrary(String baseName, boolean macosUniversal) throws Exception {

        // Get OS
        Os os = Os.current();

        // Get lib arch
        String arch;
        if(macosUniversal && os == Os.MACOS)
            arch = "universal";
        else
            arch = Arch.current().name().toLowerCase();

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

    public static void addExports(String forModule, String ofModule, String[] paths){
        Optional<Module> forModuleOpt = ModuleLayer.boot().findModule(forModule);
        if(forModuleOpt.isEmpty())
            throw new NullPointerException("Module '" + forModule + "' is not presented");
        addExports(forModuleOpt.get(), ofModule, paths);
    }

    public static void addExports(Module forModule, String ofModule, String[] paths){
        try {
            Optional<Module> moduleOpt = ModuleLayer.boot().findModule(ofModule);
            if(moduleOpt.isEmpty())
                throw new NullPointerException("Module '" + ofModule + "' is not presented");

            Method addOpensMethodImpl = Module.class.getDeclaredMethod("implAddExports", String.class, Module.class);

            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");

            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.trySetAccessible();
            Unsafe unsafe = (Unsafe) unsafeField.get(null);

            long firstFieldOffset = (long) unsafeClass.getDeclaredMethod("objectFieldOffset", Field.class)
                    .invoke(unsafe, OffsetProvider.class.getDeclaredField("first"));

            unsafeClass.getDeclaredMethod("putBoolean", Object.class, long.class, boolean.class)
                    .invoke(unsafe, addOpensMethodImpl, firstFieldOffset, true);

            for(String pkg : paths)
                addOpensMethodImpl.invoke(moduleOpt.get(), pkg, forModule);

        } catch (Throwable e) {
            throw new UnsupportedOperationException("Could not add exports of '" + ofModule + "' to " + forModule, e);
        }
    }

    private static class OffsetProvider {
        int first;
    }
}
