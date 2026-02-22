package com.huskerdev.nativekt.jvmci;

import com.huskerdev.nativekt.NativeKtUtils;
import jdk.vm.ci.code.site.*;
import jdk.vm.ci.hotspot.*;
import jdk.vm.ci.meta.*;
import jdk.vm.ci.runtime.JVMCI;
import jdk.vm.ci.runtime.JVMCIBackend;
import java.lang.reflect.Method;

import static com.huskerdev.nativekt.NativeKtUtils.addExports;

public class JVMCIUtils {

    static {
        if(NativeKtUtils.isAutoExportEnabled()) {
            addExports(JVMCIUtils.class.getModule(), "jdk.internal.vm.ci", new String[] {
                    "jdk.vm.ci.code",
                    "jdk.vm.ci.runtime",
                    "jdk.vm.ci.meta",
                    "jdk.vm.ci.hotspot",
                    "jdk.vm.ci.code.site"
            });
            addExports("jdk.graal.compiler", "java.base", new String[] {
                    "jdk.internal.misc"
            });
        }
    }

    public static void linkNativeCall(Method method, long address){
        CallingConvention convention = CallingConvention.current();

        JVMCIBackend jvmci = JVMCI.getRuntime().getHostJVMCIBackend();
        HotSpotResolvedJavaMethod resolvedMethod = (HotSpotResolvedJavaMethod) jvmci.getMetaAccess().lookupJavaMethod(method);

        jvmci.getCodeCache().setDefaultCode(resolvedMethod, convention.createNMethod(
                method.getName(),
                convention.createNativeCall(method, address),
                resolvedMethod
        ));
    }
}
