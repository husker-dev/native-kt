package com.huskerdev.nativekt.jvmci;

import com.huskerdev.nativekt.NativeKtUtils;
import com.huskerdev.nativekt.jvmci.conventions.AMD64CallingConvention;
import com.huskerdev.nativekt.jvmci.conventions.ARM64CallingConvention;
import com.huskerdev.nativekt.jvmci.conventions.RISCV64CallingConvention;
import jdk.vm.ci.code.CodeCacheProvider;
import jdk.vm.ci.code.ValueKindFactory;
import jdk.vm.ci.hotspot.HotSpotCallingConventionType;
import jdk.vm.ci.hotspot.HotSpotResolvedJavaMethod;
import jdk.vm.ci.meta.JavaKind;
import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.PlatformKind;
import jdk.vm.ci.meta.ValueKind;
import jdk.vm.ci.runtime.JVMCI;
import jdk.vm.ci.runtime.JVMCIBackend;

import java.lang.reflect.Method;

abstract public class CallingConvention {

    public static CallingConvention current() {
        switch (NativeKtUtils.Arch.current()) {
            case ARM64: return new ARM64CallingConvention();
            case X64: return new AMD64CallingConvention();
            case RISCV64: return new RISCV64CallingConvention();
            case X86: throw new UnsupportedOperationException("Unsupported CPU architecture");
        }
        throw new UnsupportedOperationException();
    }

    protected final JVMCIBackend jvmci = JVMCI.getRuntime().getHostJVMCIBackend();
    protected final CodeCacheProvider codeCache = jvmci.getCodeCache();
    protected final MetaAccessProvider meta = jvmci.getMetaAccess();

    public byte[] createNativeCall(Method method, long address) {
        Buffer buf = new Buffer();

        emitEpilogue(buf);
        emitConversion(buf, method);
        emitCall(buf, method, address);
        emitPrologue(buf, method);

        return buf.finish();
    }

    abstract protected void emitEpilogue(Buffer buf);

    abstract protected void emitConversion(Buffer buf, Method method);

    abstract protected void emitCall(Buffer buf, Method method, long address);

    abstract protected void emitPrologue(Buffer buf, Method method);


    protected HotSpotResolvedJavaMethod resolveJavaMethod(Method method){
        return (HotSpotResolvedJavaMethod) jvmci.getMetaAccess().lookupJavaMethod(method);
    }

    protected jdk.vm.ci.code.CallingConvention getCC(HotSpotResolvedJavaMethod method, jdk.vm.ci.code.CallingConvention.Type type){
        return codeCache.getRegisterConfig().getCallingConvention(
                type,
                method.getSignature().getReturnType(method.getDeclaringClass()),
                method.getSignature().toParameterTypes(method.getDeclaringClass()),
                (ValueKindFactory<ValueKindStub>) kind -> new ValueKindStub(codeCache.getTarget().arch.getPlatformKind(kind))
        );
    }

    protected jdk.vm.ci.code.CallingConvention getNativeCC(HotSpotResolvedJavaMethod method){
        return getCC(method, HotSpotCallingConventionType.NativeCall);
    }

    protected jdk.vm.ci.code.CallingConvention getJavaCC(HotSpotResolvedJavaMethod method){
        return getCC(method, HotSpotCallingConventionType.JavaCall);
    }

    protected int getArrayOffset(Class<?> javaClass){
        JavaKind elementKind = JavaKind.fromJavaClass(javaClass.getComponentType());
        return meta.getArrayBaseOffset(elementKind);
    }

    protected boolean isFloat(Class<?> type) {
        return type == float.class || type == double.class;
    }

    protected boolean isDouble(Class<?> type) {
        return type == double.class;
    }

    protected int align16(int v) {
        return (v + 15) & -16;
    }


    private static class ValueKindStub extends ValueKind<ValueKindStub> {
        ValueKindStub(PlatformKind kind) {
            super(kind);
        }

        @Override
        public ValueKindStub changeType(PlatformKind kind) {
            return new ValueKindStub(kind);
        }
    }
}
