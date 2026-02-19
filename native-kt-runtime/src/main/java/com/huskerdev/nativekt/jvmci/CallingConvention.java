package com.huskerdev.nativekt.jvmci;

import com.huskerdev.nativekt.NativeKtUtils;
import com.huskerdev.nativekt.jvmci.conventions.AMD64WindowsCallingConvention;
import com.huskerdev.nativekt.jvmci.conventions.ARM64CallingConvention;
import jdk.vm.ci.hotspot.*;
import jdk.vm.ci.meta.JavaKind;
import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.runtime.JVMCI;
import jdk.vm.ci.runtime.JVMCIBackend;

import java.lang.reflect.Method;

abstract public class CallingConvention {

    public static CallingConvention current() {
        switch (NativeKtUtils.Arch.current()) {
            case ARM64: return new ARM64CallingConvention();
            case X64: {
                if(NativeKtUtils.OS.current() == NativeKtUtils.OS.WINDOWS)
                    return new AMD64WindowsCallingConvention();
            }
            default: throw new UnsupportedOperationException("Unsupported CPU architecture");
        }
    }

    protected final JVMCIBackend jvmci = JVMCI.getRuntime().getHostJVMCIBackend();
    protected final MetaAccessProvider meta = jvmci.getMetaAccess();
    protected final HotSpotVMConfigAccess config = new HotSpotVMConfigAccess(HotSpotJVMCIRuntime.runtime().getConfigStore());

    protected final int ENTRY_BARRIER_PATCH = config.getConstant("CodeInstaller::ENTRY_BARRIER_PATCH", Integer.class);

    public byte[] createNativeCall(Method method, long address) {
        Buffer buf = new Buffer();

        emitPrologue(buf, method);
        emitConversion(buf, method);
        emitCall(buf, method, address);
        emitEpilogue(buf, method);

        return buf.finish();
    }

    abstract public HotSpotCompiledNmethod createNMethod(
            String name,
            byte[] code,
            HotSpotResolvedJavaMethod resolvedMethod
    );

    abstract protected void emitPrologue(Buffer buf, Method method);

    abstract protected void emitConversion(Buffer buf, Method method);

    abstract protected void emitCall(Buffer buf, Method method, long address);

    abstract protected void emitEpilogue(Buffer buf, Method method);

    protected int getArrayOffset(Class<?> javaClass){
        JavaKind elementKind = JavaKind.fromJavaClass(javaClass.getComponentType());
        return meta.getArrayBaseOffset(elementKind);
    }

    protected static boolean isFloatingPointType(Class<?> type) {
        return type == float.class || type == double.class;
    }

    protected static boolean isIntegerType(Class<?> type) {
        return type == byte.class || type == short.class ||
                type == int.class || type == long.class ||
                type == boolean.class || type == char.class ||
                !type.isPrimitive();
    }

    protected static boolean isDouble(Class<?> type) {
        return type == double.class;
    }

    protected int align16(int v) {
        return (v + 15) & -16;
    }
}
