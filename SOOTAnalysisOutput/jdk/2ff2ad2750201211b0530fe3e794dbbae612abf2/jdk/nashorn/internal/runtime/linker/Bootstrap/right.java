package jdk.nashorn.internal.runtime.linker;

import static jdk.nashorn.internal.codegen.CompilerConstants.staticCallNoLookup;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import jdk.internal.dynalink.CallSiteDescriptor;
import jdk.internal.dynalink.DynamicLinker;
import jdk.internal.dynalink.DynamicLinkerFactory;
import jdk.internal.dynalink.beans.BeansLinker;
import jdk.internal.dynalink.beans.StaticClass;
import jdk.internal.dynalink.linker.GuardedInvocation;
import jdk.internal.dynalink.linker.LinkerServices;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.internal.codegen.CompilerConstants.Call;
import jdk.nashorn.internal.codegen.RuntimeCallSite;
import jdk.nashorn.internal.runtime.JSType;
import jdk.nashorn.internal.runtime.ScriptFunction;
import jdk.nashorn.internal.runtime.ScriptRuntime;
import jdk.nashorn.internal.runtime.options.Options;

public final class Bootstrap {

    public static final Call BOOTSTRAP = staticCallNoLookup(Bootstrap.class, "bootstrap", CallSite.class, Lookup.class, String.class, MethodType.class, int.class);

    private Bootstrap() {
    }

    private static final DynamicLinker dynamicLinker;

    static {
        final DynamicLinkerFactory factory = new DynamicLinkerFactory();
        final NashornBeansLinker nashornBeansLinker = new NashornBeansLinker();
        final JSObjectLinker jsObjectLinker = new JSObjectLinker(nashornBeansLinker);
        factory.setPrioritizedLinkers(new NashornLinker(), new NashornPrimitiveLinker(), new NashornStaticClassLinker(), new BoundDynamicMethodLinker(), new JavaSuperAdapterLinker(), jsObjectLinker, new ReflectionCheckLinker());
        factory.setFallbackLinkers(nashornBeansLinker, new NashornBottomLinker());
        factory.setSyncOnRelink(true);
        final int relinkThreshold = Options.getIntProperty("nashorn.unstable.relink.threshold", -1);
        if (relinkThreshold > -1) {
            factory.setUnstableRelinkThreshold(relinkThreshold);
        }
        factory.setClassLoader(Bootstrap.class.getClassLoader());
        dynamicLinker = factory.createLinker();
    }

    public static boolean isCallable(final Object obj) {
        if (obj == ScriptRuntime.UNDEFINED || obj == null) {
            return false;
        }
        return obj instanceof ScriptFunction || ((obj instanceof JSObject) && ((JSObject) obj).isFunction()) || isDynamicMethod(obj) || isFunctionalInterfaceObject(obj) || obj instanceof StaticClass;
    }

    public static boolean isDynamicMethod(final Object obj) {
        return obj instanceof BoundDynamicMethod || BeansLinker.isDynamicMethod(obj);
    }

    public static boolean isFunctionalInterfaceObject(final Object obj) {
        return !JSType.isPrimitive(obj) && (NashornBottomLinker.getFunctionalInterfaceMethod(obj.getClass()) != null);
    }

    public static CallSite bootstrap(final Lookup lookup, final String opDesc, final MethodType type, final int flags) {
        return dynamicLinker.link(LinkerCallSite.newLinkerCallSite(lookup, opDesc, type, flags));
    }

    public static CallSite runtimeBootstrap(final MethodHandles.Lookup lookup, final String initialName, final MethodType type) {
        return new RuntimeCallSite(type, initialName);
    }

    public static MethodHandle createDynamicInvoker(final String opDesc, final Class<?> rtype, final Class<?>... ptypes) {
        return createDynamicInvoker(opDesc, MethodType.methodType(rtype, ptypes));
    }

    public static MethodHandle createDynamicInvoker(final String opDesc, final MethodType type) {
        return bootstrap(MethodHandles.publicLookup(), opDesc, type, 0).dynamicInvoker();
    }

    public static Object bindDynamicMethod(Object dynamicMethod, Object boundThis) {
        return new BoundDynamicMethod(dynamicMethod, boundThis);
    }

    public static Object createSuperAdapter(final Object adapter) {
        return new JavaSuperAdapter(adapter);
    }

    public static void checkReflectionAccess(Class<?> clazz, boolean isStatic) {
        ReflectionCheckLinker.checkReflectionAccess(clazz, isStatic);
    }

    public static LinkerServices getLinkerServices() {
        return dynamicLinker.getLinkerServices();
    }

    static GuardedInvocation asType(final GuardedInvocation inv, final LinkerServices linkerServices, final CallSiteDescriptor desc) {
        return inv == null ? null : inv.asType(linkerServices, desc.getMethodType());
    }
}
