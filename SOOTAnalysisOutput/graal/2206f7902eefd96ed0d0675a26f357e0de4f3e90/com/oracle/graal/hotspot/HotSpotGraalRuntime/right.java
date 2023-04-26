package com.oracle.graal.hotspot;

import static com.oracle.graal.compiler.common.GraalOptions.*;
import static com.oracle.graal.compiler.common.UnsafeAccess.*;
import static com.oracle.graal.hotspot.HotSpotGraalRuntime.Options.*;
import java.lang.reflect.*;
import java.util.*;
import sun.misc.*;
import sun.reflect.*;
import com.oracle.graal.api.code.*;
import com.oracle.graal.api.code.stack.*;
import com.oracle.graal.api.meta.*;
import com.oracle.graal.api.replacements.*;
import com.oracle.graal.api.runtime.*;
import com.oracle.graal.compiler.common.*;
import com.oracle.graal.compiler.target.*;
import com.oracle.graal.hotspot.bridge.*;
import com.oracle.graal.hotspot.logging.*;
import com.oracle.graal.hotspot.meta.*;
import com.oracle.graal.options.*;
import com.oracle.graal.runtime.*;

public final class HotSpotGraalRuntime implements GraalRuntime, RuntimeProvider, StackIntrospection {

    private static final HotSpotGraalRuntime instance = new HotSpotGraalRuntime();

    static {
        instance.completeInitialization();
    }

    @CallerSensitive
    public static HotSpotGraalRuntime runtime() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            Class<?> cc = Reflection.getCallerClass();
            if (cc != null && cc.getClassLoader() != null) {
                sm.checkPermission(Graal.ACCESS_PERMISSION);
            }
        }
        assert instance != null;
        return instance;
    }

    static {
        Reflection.registerFieldsToFilter(HotSpotGraalRuntime.class, "instance");
    }

    public void completeInitialization() {
        VMToCompiler toCompiler = this.vmToCompiler;
        CompilerToVM toVM = this.compilerToVm;
        if (CountingProxy.ENABLED) {
            toCompiler = CountingProxy.getProxy(VMToCompiler.class, toCompiler);
            toVM = CountingProxy.getProxy(CompilerToVM.class, toVM);
        }
        if (Logger.ENABLED) {
            toCompiler = LoggingProxy.getProxy(VMToCompiler.class, toCompiler);
            toVM = LoggingProxy.getProxy(CompilerToVM.class, toVM);
        }
        this.vmToCompiler = toCompiler;
        this.compilerToVm = toVM;
    }

    static class Options {

        @Option(help = "The runtime configuration to use")
        static final OptionValue<String> GraalRuntime = new OptionValue<>("");
    }

    private static HotSpotBackendFactory findFactory(String architecture) {
        HotSpotBackendFactory basic = null;
        HotSpotBackendFactory selected = null;
        HotSpotBackendFactory nonBasic = null;
        int nonBasicCount = 0;
        for (HotSpotBackendFactory factory : ServiceLoader.loadInstalled(HotSpotBackendFactory.class)) {
            if (factory.getArchitecture().equalsIgnoreCase(architecture)) {
                if (factory.getGraalRuntimeName().equals(GraalRuntime.getValue())) {
                    assert selected == null || checkFactoryOverriding(selected, factory);
                    selected = factory;
                }
                if (factory.getGraalRuntimeName().equals("basic")) {
                    assert basic == null || checkFactoryOverriding(basic, factory);
                    basic = factory;
                } else {
                    nonBasic = factory;
                    nonBasicCount++;
                }
            }
        }
        if (selected != null) {
            return selected;
        } else {
            if (!GraalRuntime.getValue().equals("")) {
                throw new GraalInternalError("Specified runtime \"%s\" not available for the %s architecture", GraalRuntime.getValue(), architecture);
            } else if (nonBasicCount == 1) {
                return nonBasic;
            } else {
                return basic;
            }
        }
    }

    private static boolean checkFactoryOverriding(HotSpotBackendFactory baseFactory, HotSpotBackendFactory overridingFactory) {
        return baseFactory.getClass().isAssignableFrom(overridingFactory.getClass());
    }

    public static Kind getHostWordKind() {
        return instance.getHostBackend().getTarget().wordKind;
    }

    public static long unsafeReadWord(long address) {
        return unsafe.getAddress(address);
    }

    public static long unsafeReadKlassPointer(Object object) {
        return instance.getCompilerToVM().readUnsafeKlassPointer(object);
    }

    public static long unsafeReadWord(Object object, long offset) {
        if (getHostWordKind() == Kind.Long) {
            return unsafe.getLong(object, offset);
        }
        return unsafe.getInt(object, offset) & 0xFFFFFFFFL;
    }

    protected CompilerToVM compilerToVm;

    protected VMToCompiler vmToCompiler;

    protected final HotSpotVMConfig config;

    private final HotSpotBackend hostBackend;

    private final ClassValue<ResolvedJavaType> graalMirrors = new ClassValue<ResolvedJavaType>() {

        @Override
        protected ResolvedJavaType computeValue(Class<?> javaClass) {
            if (javaClass.isPrimitive()) {
                Kind kind = Kind.fromJavaClass(javaClass);
                return new HotSpotResolvedPrimitiveType(kind);
            } else {
                return new HotSpotResolvedObjectType(javaClass);
            }
        }
    };

    private final Map<Class<? extends Architecture>, HotSpotBackend> backends = new HashMap<>();

    private HotSpotGraalRuntime() {
        CompilerToVM toVM = new CompilerToVMImpl();
        VMToCompiler toCompiler = new VMToCompilerImpl(this);
        compilerToVm = toVM;
        vmToCompiler = toCompiler;
        config = new HotSpotVMConfig(compilerToVm);
        CompileTheWorld.Options.overrideWithNativeOptions(config);
        if (HotSpotPrintCompilation.getValue() == false) {
            HotSpotPrintCompilation.setValue(config.printCompilation);
        }
        if (HotSpotPrintInlining.getValue() == false) {
            HotSpotPrintInlining.setValue(config.printInlining);
        }
        if (HotSpotCIPrintCompilerName.getValue() == false) {
            HotSpotCIPrintCompilerName.setValue(config.printCompilerName);
        }
        if (Boolean.valueOf(System.getProperty("graal.printconfig"))) {
            printConfig(config);
        }
        String hostArchitecture = config.getHostArchitectureName();
        hostBackend = registerBackend(findFactory(hostArchitecture).createBackend(this, null));
        String[] gpuArchitectures = getGPUArchitectureNames(compilerToVm);
        for (String arch : gpuArchitectures) {
            HotSpotBackendFactory factory = findFactory(arch);
            if (factory == null) {
                throw new GraalInternalError("No backend available for specified GPU architecture \"%s\"", arch);
            }
            registerBackend(factory.createBackend(this, hostBackend));
        }
    }

    private HotSpotBackend registerBackend(HotSpotBackend backend) {
        Class<? extends Architecture> arch = backend.getTarget().arch.getClass();
        HotSpotBackend oldValue = backends.put(arch, backend);
        assert oldValue == null : "cannot overwrite existing backend for architecture " + arch.getSimpleName();
        return backend;
    }

    public ResolvedJavaType fromClass(Class<?> javaClass) {
        return graalMirrors.get(javaClass);
    }

    private static String[] getGPUArchitectureNames(CompilerToVM c2vm) {
        String gpuList = c2vm.getGPUs();
        if (!gpuList.isEmpty()) {
            String[] gpus = gpuList.split(",");
            return gpus;
        }
        return new String[0];
    }

    private static void printConfig(HotSpotVMConfig config) {
        Field[] fields = config.getClass().getDeclaredFields();
        Map<String, Field> sortedFields = new TreeMap<>();
        for (Field f : fields) {
            f.setAccessible(true);
            sortedFields.put(f.getName(), f);
        }
        for (Field f : sortedFields.values()) {
            try {
                Logger.info(String.format("%9s %-40s = %s", f.getType().getSimpleName(), f.getName(), Logger.pretty(f.get(config))));
            } catch (Exception e) {
            }
        }
    }

    public HotSpotVMConfig getConfig() {
        return config;
    }

    public TargetDescription getTarget() {
        return hostBackend.getTarget();
    }

    public CompilerToVM getCompilerToVM() {
        return compilerToVm;
    }

    public VMToCompiler getVMToCompiler() {
        return vmToCompiler;
    }

    public JavaType lookupType(String name, HotSpotResolvedObjectType accessingType, boolean resolve) {
        if (name.length() == 1) {
            Kind kind = Kind.fromPrimitiveOrVoidTypeChar(name.charAt(0));
            return HotSpotResolvedPrimitiveType.fromKind(kind);
        }
        Class<?> accessingClass = null;
        if (accessingType != null) {
            accessingClass = accessingType.mirror();
        }
        final long metaspaceKlass = compilerToVm.lookupType(name, accessingClass, resolve);
        if (metaspaceKlass == 0) {
            return HotSpotUnresolvedJavaType.create(name);
        }
        return HotSpotResolvedObjectType.fromMetaspaceKlass(metaspaceKlass);
    }

    public HotSpotProviders getHostProviders() {
        return getHostBackend().getProviders();
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Class<T> clazz) {
        if (clazz == RuntimeProvider.class) {
            return (T) this;
        } else if (clazz == StackIntrospection.class) {
            return (T) this;
        } else if (clazz == SnippetReflectionProvider.class) {
            return (T) getHostProviders().getSnippetReflection();
        }
        return null;
    }

    public HotSpotBackend getHostBackend() {
        return hostBackend;
    }

    public <T extends Architecture> Backend getBackend(Class<T> arch) {
        assert arch != Architecture.class;
        return backends.get(arch);
    }

    public Map<Class<? extends Architecture>, HotSpotBackend> getBackends() {
        return Collections.unmodifiableMap(backends);
    }

    public static int getArrayBaseOffset(Kind kind) {
        switch(kind) {
            case Boolean:
                return Unsafe.ARRAY_BOOLEAN_BASE_OFFSET;
            case Byte:
                return Unsafe.ARRAY_BYTE_BASE_OFFSET;
            case Char:
                return Unsafe.ARRAY_CHAR_BASE_OFFSET;
            case Short:
                return Unsafe.ARRAY_SHORT_BASE_OFFSET;
            case Int:
                return Unsafe.ARRAY_INT_BASE_OFFSET;
            case Long:
                return Unsafe.ARRAY_LONG_BASE_OFFSET;
            case Float:
                return Unsafe.ARRAY_FLOAT_BASE_OFFSET;
            case Double:
                return Unsafe.ARRAY_DOUBLE_BASE_OFFSET;
            case Object:
                return Unsafe.ARRAY_OBJECT_BASE_OFFSET;
            default:
                throw GraalInternalError.shouldNotReachHere();
        }
    }

    public static int getArrayIndexScale(Kind kind) {
        switch(kind) {
            case Boolean:
                return Unsafe.ARRAY_BOOLEAN_INDEX_SCALE;
            case Byte:
                return Unsafe.ARRAY_BYTE_INDEX_SCALE;
            case Char:
                return Unsafe.ARRAY_CHAR_INDEX_SCALE;
            case Short:
                return Unsafe.ARRAY_SHORT_INDEX_SCALE;
            case Int:
                return Unsafe.ARRAY_INT_INDEX_SCALE;
            case Long:
                return Unsafe.ARRAY_LONG_INDEX_SCALE;
            case Float:
                return Unsafe.ARRAY_FLOAT_INDEX_SCALE;
            case Double:
                return Unsafe.ARRAY_DOUBLE_INDEX_SCALE;
            case Object:
                return Unsafe.ARRAY_OBJECT_INDEX_SCALE;
            default:
                throw GraalInternalError.shouldNotReachHere();
        }
    }

    public Iterable<InspectedFrame> getStackTrace(ResolvedJavaMethod[] initialMethods, ResolvedJavaMethod[] matchingMethods, int initialSkip) {
        final long[] initialMetaMethods = toMeta(initialMethods);
        final long[] matchingMetaMethods = toMeta(matchingMethods);
        class StackFrameIterator implements Iterator<InspectedFrame> {

            private HotSpotStackFrameReference current = compilerToVm.getNextStackFrame(null, initialMetaMethods, initialSkip);

            private boolean advanced = true;

            public boolean hasNext() {
                update();
                return current != null;
            }

            public InspectedFrame next() {
                update();
                advanced = false;
                return current;
            }

            private void update() {
                if (!advanced) {
                    current = compilerToVm.getNextStackFrame(current, matchingMetaMethods, 0);
                    advanced = true;
                }
            }
        }
        return new Iterable<InspectedFrame>() {

            public Iterator<InspectedFrame> iterator() {
                return new StackFrameIterator();
            }
        };
    }

    private static long[] toMeta(ResolvedJavaMethod[] methods) {
        if (methods == null) {
            return null;
        } else {
            long[] result = new long[methods.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = ((HotSpotResolvedJavaMethod) methods[i]).getMetaspaceMethod();
            }
            return result;
        }
    }
}
