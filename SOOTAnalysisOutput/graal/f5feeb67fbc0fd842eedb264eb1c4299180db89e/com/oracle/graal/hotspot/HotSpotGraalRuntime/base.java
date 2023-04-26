package com.oracle.graal.hotspot;

import static com.oracle.graal.compiler.GraalDebugConfig.*;
import static com.oracle.graal.compiler.common.GraalOptions.*;
import static com.oracle.graal.compiler.common.UnsafeAccess.*;
import static com.oracle.graal.hotspot.CompileTheWorld.Options.*;
import static com.oracle.graal.hotspot.HotSpotGraalRuntime.Options.*;
import static com.oracle.graal.hotspot.InitTimer.*;
import java.lang.reflect.*;
import java.util.*;
import sun.misc.*;
import com.oracle.graal.api.code.*;
import com.oracle.graal.api.code.stack.*;
import com.oracle.graal.api.collections.*;
import com.oracle.graal.api.meta.*;
import com.oracle.graal.api.replacements.*;
import com.oracle.graal.api.runtime.*;
import com.oracle.graal.compiler.common.*;
import com.oracle.graal.compiler.target.*;
import com.oracle.graal.debug.*;
import com.oracle.graal.graph.*;
import com.oracle.graal.hotspot.CompileTheWorld.Config;
import com.oracle.graal.hotspot.bridge.*;
import com.oracle.graal.hotspot.debug.*;
import com.oracle.graal.hotspot.events.*;
import com.oracle.graal.hotspot.logging.*;
import com.oracle.graal.hotspot.meta.*;
import com.oracle.graal.options.*;
import com.oracle.graal.printer.*;
import com.oracle.graal.replacements.*;
import com.oracle.graal.runtime.*;

public final class HotSpotGraalRuntime implements HotSpotGraalRuntimeProvider, HotSpotProxified {

    private static final HotSpotGraalRuntime instance;

    static {
        try (InitTimer t = timer("initialize HotSpotOptions")) {
            HotSpotOptions.initialize();
        }
        try (InitTimer t = timer("HotSpotGraalRuntime.<init>")) {
            instance = new HotSpotGraalRuntime();
        }
        try (InitTimer t = timer("HotSpotGraalRuntime.completeInitialization")) {
            instance.completeInitialization();
        }
    }

    public static HotSpotGraalRuntime runtime() {
        assert instance != null;
        return instance;
    }

    public void completeInitialization() {
        TTY.initialize(Options.LogFile.getStream(compilerToVm));
        CompilerToVM toVM = this.compilerToVm;
        if (CountingProxy.ENABLED) {
            toVM = CountingProxy.getProxy(CompilerToVM.class, toVM);
        }
        if (Logger.ENABLED) {
            toVM = LoggingProxy.getProxy(CompilerToVM.class, toVM);
        }
        this.compilerToVm = toVM;
        if (Log.getValue() == null && !areScopedMetricsOrTimersEnabled() && Dump.getValue() == null && Verify.getValue() == null) {
            if (MethodFilter.getValue() != null) {
                TTY.println("WARNING: Ignoring MethodFilter option since Log, Meter, Time, TrackMemUse, Dump and Verify options are all null");
            }
        }
        if (Debug.isEnabled()) {
            DebugEnvironment.initialize(TTY.cachedOut);
            String summary = DebugValueSummary.getValue();
            if (summary != null) {
                switch(summary) {
                    case "Name":
                    case "Partial":
                    case "Complete":
                    case "Thread":
                        break;
                    default:
                        throw new GraalInternalError("Unsupported value for DebugSummaryValue: %s", summary);
                }
            }
        }
        if (Debug.areUnconditionalMetricsEnabled() || Debug.areUnconditionalTimersEnabled() || (Debug.isEnabled() && areScopedMetricsOrTimersEnabled())) {
            debugValuesPrinter = new DebugValuesPrinter();
        }
        try (InitTimer st = timer(hostBackend.getTarget().arch.getName(), ".completeInitialization")) {
            hostBackend.completeInitialization();
        }
        for (HotSpotBackend backend : backends.values()) {
            if (backend != hostBackend) {
                try (InitTimer st = timer(backend.getTarget().arch.getName(), ".completeInitialization")) {
                    backend.completeInitialization();
                }
            }
        }
        BenchmarkCounters.initialize(toVM);
        assert checkArrayIndexScaleInvariants();
        runtimeStartTime = System.nanoTime();
    }

    private boolean checkArrayIndexScaleInvariants() {
        assert getArrayIndexScale(Kind.Byte) == 1;
        assert getArrayIndexScale(Kind.Boolean) == 1;
        assert getArrayIndexScale(Kind.Char) == 2;
        assert getArrayIndexScale(Kind.Short) == 2;
        assert getArrayIndexScale(Kind.Int) == 4;
        assert getArrayIndexScale(Kind.Long) == 8;
        assert getArrayIndexScale(Kind.Float) == 4;
        assert getArrayIndexScale(Kind.Double) == 8;
        return true;
    }

    public static class Options {

        @Option(help = "The runtime configuration to use", type = OptionType.Expert)
        static final OptionValue<String> GraalRuntime = new OptionValue<>("");

        @Option(help = "File to which logging is sent.  A %p in the name will be replaced with a string identifying the process, usually the process id.", type = OptionType.Expert)
        public static final PrintStreamOption LogFile = new PrintStreamOption();
    }

    private static HotSpotBackendFactory findFactory(String architecture) {
        HotSpotBackendFactory basic = null;
        HotSpotBackendFactory selected = null;
        HotSpotBackendFactory nonBasic = null;
        int nonBasicCount = 0;
        for (HotSpotBackendFactory factory : Services.load(HotSpotBackendFactory.class)) {
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

    protected final HotSpotVMConfig config;

    private final HotSpotBackend hostBackend;

    private DebugValuesPrinter debugValuesPrinter;

    private final ClassValue<ResolvedJavaType> graalMirrors = new ClassValue<ResolvedJavaType>() {

        @Override
        protected ResolvedJavaType computeValue(Class<?> javaClass) {
            if (javaClass.isPrimitive()) {
                Kind kind = Kind.fromJavaClass(javaClass);
                return new HotSpotResolvedPrimitiveType(kind);
            } else {
                return new HotSpotResolvedObjectTypeImpl(javaClass);
            }
        }
    };

    private final Map<Class<? extends Architecture>, HotSpotBackend> backends = new HashMap<>();

    private HotSpotGraalRuntime() {
        CompilerToVM toVM = new CompilerToVMImpl();
        compilerToVm = toVM;
        try (InitTimer t = timer("HotSpotVMConfig<init>")) {
            config = new HotSpotVMConfig(compilerToVm);
        }
        CompileTheWorld.Options.overrideWithNativeOptions(config);
        if (HotSpotPrintInlining.getValue() == false) {
            HotSpotPrintInlining.setValue(config.printInlining);
        }
        if (Boolean.valueOf(System.getProperty("graal.printconfig"))) {
            printConfig(config);
        }
        String hostArchitecture = config.getHostArchitectureName();
        HotSpotBackendFactory factory;
        try (InitTimer t = timer("find factory:", hostArchitecture)) {
            factory = findFactory(hostArchitecture);
        }
        try (InitTimer t = timer("create backend:", hostArchitecture)) {
            hostBackend = registerBackend(factory.createBackend(this, null));
        }
        String[] gpuArchitectures = getGPUArchitectureNames(compilerToVm);
        for (String arch : gpuArchitectures) {
            try (InitTimer t = timer("find factory:", arch)) {
                factory = findFactory(arch);
            }
            if (factory == null) {
                throw new GraalInternalError("No backend available for specified GPU architecture \"%s\"", arch);
            }
            try (InitTimer t = timer("create backend:", arch)) {
                registerBackend(factory.createBackend(this, hostBackend));
            }
        }
        try (InitTimer t = timer("createEventProvider")) {
            eventProvider = createEventProvider();
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

    public JavaType lookupType(String name, HotSpotResolvedObjectType accessingType, boolean resolve) {
        Objects.requireNonNull(accessingType, "cannot resolve type without an accessing class");
        if (name.length() == 1) {
            Kind kind = Kind.fromPrimitiveOrVoidTypeChar(name.charAt(0));
            return fromClass(kind.toJavaClass());
        }
        HotSpotResolvedObjectTypeImpl hsAccessingType = (HotSpotResolvedObjectTypeImpl) accessingType;
        final long metaspaceKlass = compilerToVm.lookupType(name, hsAccessingType.mirror(), resolve);
        if (metaspaceKlass == 0L) {
            assert resolve == false;
            return HotSpotUnresolvedJavaType.create(this, name);
        }
        return HotSpotResolvedObjectTypeImpl.fromMetaspaceKlass(metaspaceKlass);
    }

    public HotSpotProviders getHostProviders() {
        return getHostBackend().getProviders();
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    private final NodeCollectionsProvider nodeCollectionsProvider = new DefaultNodeCollectionsProvider();

    private final EventProvider eventProvider;

    private EventProvider createEventProvider() {
        if (config.flightRecorder) {
            Iterable<EventProvider> sl = Services.load(EventProvider.class);
            EventProvider singleProvider = null;
            for (EventProvider ep : sl) {
                assert singleProvider == null : String.format("multiple %s service implementations found: %s and %s", EventProvider.class.getName(), singleProvider.getClass().getName(), ep.getClass().getName());
                singleProvider = ep;
            }
            return singleProvider;
        }
        return new EmptyEventProvider();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Class<T> clazz) {
        if (clazz == RuntimeProvider.class) {
            return (T) this;
        } else if (clazz == CollectionsProvider.class || clazz == NodeCollectionsProvider.class) {
            return (T) nodeCollectionsProvider;
        } else if (clazz == StackIntrospection.class) {
            return (T) this;
        } else if (clazz == SnippetReflectionProvider.class) {
            return (T) getHostProviders().getSnippetReflection();
        } else if (clazz == EventProvider.class) {
            return (T) eventProvider;
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

    public int getArrayBaseOffset(Kind kind) {
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

    public int getArrayIndexScale(Kind kind) {
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

    @Override
    public <T> T iterateFrames(ResolvedJavaMethod[] initialMethods, ResolvedJavaMethod[] matchingMethods, int initialSkip, InspectedFrameVisitor<T> visitor) {
        final long[] initialMetaMethods = toMeta(initialMethods);
        final long[] matchingMetaMethods = toMeta(matchingMethods);
        HotSpotStackFrameReference current = compilerToVm.getNextStackFrame(null, initialMetaMethods, initialSkip);
        while (current != null) {
            T result = visitor.visitFrame(current);
            if (result != null) {
                return result;
            }
            current = compilerToVm.getNextStackFrame(current, matchingMetaMethods, 0);
        }
        return null;
    }

    private static long[] toMeta(ResolvedJavaMethod[] methods) {
        if (methods == null) {
            return null;
        } else {
            long[] result = new long[methods.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = ((HotSpotResolvedJavaMethodImpl) methods[i]).getMetaspaceMethod();
            }
            return result;
        }
    }

    private long runtimeStartTime;

    static void phaseTransition(String phase) {
        CompilationStatistics.clear(phase);
    }

    @SuppressWarnings("unused")
    private void compileTheWorld() throws Throwable {
        int iterations = CompileTheWorld.Options.CompileTheWorldIterations.getValue();
        for (int i = 0; i < iterations; i++) {
            getCompilerToVM().resetCompilationStatistics();
            TTY.println("CompileTheWorld : iteration " + i);
            CompileTheWorld ctw = new CompileTheWorld(CompileTheWorldClasspath.getValue(), new Config(CompileTheWorldConfig.getValue()), CompileTheWorldStartAt.getValue(), CompileTheWorldStopAt.getValue(), CompileTheWorldMethodFilter.getValue(), CompileTheWorldExcludeMethodFilter.getValue(), CompileTheWorldVerbose.getValue());
            ctw.compile();
        }
        System.exit(0);
    }

    @SuppressWarnings("unused")
    private void shutdown() throws Exception {
        if (debugValuesPrinter != null) {
            debugValuesPrinter.printDebugValues();
        }
        phaseTransition("final");
        SnippetCounter.printGroups(TTY.out().out());
        BenchmarkCounters.shutdown(getCompilerToVM(), runtimeStartTime);
    }
}
