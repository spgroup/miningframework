package jdk.nashorn.internal.runtime.linker;

import static jdk.nashorn.internal.codegen.CompilerConstants.staticCallNoLookup;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import jdk.internal.dynalink.CallSiteDescriptor;
import jdk.internal.dynalink.DynamicLinker;
import jdk.internal.dynalink.DynamicLinkerFactory;
import jdk.internal.dynalink.GuardedInvocationFilter;
import jdk.internal.dynalink.beans.BeansLinker;
import jdk.internal.dynalink.beans.StaticClass;
import jdk.internal.dynalink.linker.GuardedInvocation;
import jdk.internal.dynalink.linker.LinkRequest;
import jdk.internal.dynalink.linker.LinkerServices;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.internal.codegen.CompilerConstants.Call;
import jdk.nashorn.internal.codegen.ObjectClassGenerator;
import jdk.nashorn.internal.codegen.RuntimeCallSite;
import jdk.nashorn.internal.lookup.MethodHandleFactory;
import jdk.nashorn.internal.lookup.MethodHandleFunctionality;
import jdk.nashorn.internal.runtime.JSType;
import jdk.nashorn.internal.runtime.OptimisticReturnFilters;
import jdk.nashorn.internal.runtime.ScriptFunction;
import jdk.nashorn.internal.runtime.ScriptRuntime;
import jdk.nashorn.internal.runtime.options.Options;

public final class Bootstrap {

    public static final Call BOOTSTRAP = staticCallNoLookup(Bootstrap.class, "bootstrap", CallSite.class, Lookup.class, String.class, MethodType.class, int.class);

    private static final MethodHandleFunctionality MH = MethodHandleFactory.getFunctionality();

    private static final int NASHORN_DEFAULT_UNSTABLE_RELINK_THRESHOLD = ObjectClassGenerator.OBJECT_FIELDS_ONLY ? 8 : 16;

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
        factory.setPrelinkFilter(new GuardedInvocationFilter() {

            @Override
            public GuardedInvocation filter(final GuardedInvocation inv, final LinkRequest request, final LinkerServices linkerServices) {
                final CallSiteDescriptor desc = request.getCallSiteDescriptor();
                return OptimisticReturnFilters.filterOptimisticReturnValue(inv, desc).asType(linkerServices, desc.getMethodType());
            }
        });
        final int relinkThreshold = Options.getIntProperty("nashorn.unstable.relink.threshold", NASHORN_DEFAULT_UNSTABLE_RELINK_THRESHOLD);
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

    public static CallSite mathBootstrap(final MethodHandles.Lookup lookup, final String name, final MethodType type, final int programPoint) {
        final MethodHandle mh;
        switch(name) {
            case "iadd":
                mh = JSType.ADD_EXACT.methodHandle();
                break;
            case "isub":
                mh = JSType.SUB_EXACT.methodHandle();
                break;
            case "imul":
                mh = JSType.MUL_EXACT.methodHandle();
                break;
            case "idiv":
                mh = JSType.DIV_EXACT.methodHandle();
                break;
            case "irem":
                mh = JSType.REM_EXACT.methodHandle();
                break;
            case "ineg":
                mh = JSType.NEGATE_EXACT.methodHandle();
                break;
            case "ladd":
                mh = JSType.ADD_EXACT_LONG.methodHandle();
                break;
            case "lsub":
                mh = JSType.SUB_EXACT_LONG.methodHandle();
                break;
            case "lmul":
                mh = JSType.MUL_EXACT_LONG.methodHandle();
                break;
            case "ldiv":
                mh = JSType.DIV_EXACT_LONG.methodHandle();
                break;
            case "lrem":
                mh = JSType.REM_EXACT_LONG.methodHandle();
                break;
            case "lneg":
                mh = JSType.NEGATE_EXACT_LONG.methodHandle();
                break;
            default:
                throw new AssertionError("unsupported math intrinsic");
        }
        return new ConstantCallSite(MH.insertArguments(mh, mh.type().parameterCount() - 1, programPoint));
    }

    public static MethodHandle createDynamicInvoker(final String opDesc, final Class<?> rtype, final Class<?>... ptypes) {
        return createDynamicInvoker(opDesc, MethodType.methodType(rtype, ptypes));
    }

    public static MethodHandle createDynamicInvoker(final String opDesc, final MethodType type) {
        return bootstrap(MethodHandles.publicLookup(), opDesc, type, 0).dynamicInvoker();
    }

    public static Object bindDynamicMethod(final Object dynamicMethod, final Object boundThis) {
        return new BoundDynamicMethod(dynamicMethod, boundThis);
    }

    public static Object createSuperAdapter(final Object adapter) {
        return new JavaSuperAdapter(adapter);
    }

    public static void checkReflectionAccess(final Class<?> clazz, final boolean isStatic) {
        ReflectionCheckLinker.checkReflectionAccess(clazz, isStatic);
    }

    public static LinkerServices getLinkerServices() {
        return dynamicLinker.getLinkerServices();
    }

    static GuardedInvocation asTypeSafeReturn(final GuardedInvocation inv, final LinkerServices linkerServices, final CallSiteDescriptor desc) {
        return inv == null ? null : inv.asTypeSafeReturn(linkerServices, desc.getMethodType());
    }
}
