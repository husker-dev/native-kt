package com.huskerdev.nativekt.jvmci;

import com.huskerdev.nativekt.NativeKtUtils;
import jdk.vm.ci.code.site.DataPatch;
import jdk.vm.ci.code.site.Mark;
import jdk.vm.ci.code.site.Site;
import jdk.vm.ci.hotspot.*;
import jdk.vm.ci.meta.*;
import jdk.vm.ci.runtime.JVMCI;
import jdk.vm.ci.runtime.JVMCIBackend;
import jdk.vm.ci.runtime.JVMCICompiler;
import java.lang.reflect.Method;

import static com.huskerdev.nativekt.NativeKtUtils.addExports;

public class JVMCIUtils {

    private static final int ENTRY_BARRIER_PATCH;

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

        HotSpotVMConfigAccess config = new HotSpotVMConfigAccess(HotSpotJVMCIRuntime.runtime().getConfigStore());
        ENTRY_BARRIER_PATCH = config.getConstant("CodeInstaller::ENTRY_BARRIER_PATCH", Integer.class);
    }

    public static void linkNativeCall(Method method, long address){
        byte[] code = CallingConvention.current().createNativeCall(method, address);

        JVMCIBackend jvmci = JVMCI.getRuntime().getHostJVMCIBackend();
        HotSpotResolvedJavaMethod resolvedMethod = (HotSpotResolvedJavaMethod) jvmci.getMetaAccess().lookupJavaMethod(method);

        jvmci.getCodeCache().setDefaultCode(resolvedMethod, new HotSpotCompiledNmethod(
                method.getName(),
                code,
                code.length,
                new Site[] { new Mark(0, ENTRY_BARRIER_PATCH) },
                new Assumptions.Assumption[0],
                new ResolvedJavaMethod[0],
                new HotSpotCompiledCode.Comment[0],
                new byte[0],
                1,
                new DataPatch[0],
                true,
                0,
                null,
                resolvedMethod,
                JVMCICompiler.INVOCATION_ENTRY_BCI,
                1,
                0,
                false
        ));
    }
}
