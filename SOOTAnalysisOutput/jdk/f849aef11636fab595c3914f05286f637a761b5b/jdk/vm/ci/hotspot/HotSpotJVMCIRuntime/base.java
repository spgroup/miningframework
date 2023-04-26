package jdk.vm.ci.hotspot;

import static jdk.vm.ci.inittimer.InitTimer.*;
import java.util.*;
import jdk.vm.ci.code.*;
import jdk.vm.ci.common.*;
import jdk.vm.ci.compiler.*;
import jdk.vm.ci.compiler.Compiler;
import jdk.vm.ci.inittimer.*;
import jdk.vm.ci.meta.*;
import jdk.vm.ci.runtime.*;
import jdk.vm.ci.service.*;

public final class HotSpotJVMCIRuntime implements HotSpotJVMCIRuntimeProvider, HotSpotProxified {

    static {
        JVMCI.initialize();
    }

    @SuppressWarnings("try")
    static class DelayedInit {

        private static final HotSpotJVMCIRuntime instance;

        static {
            try (InitTimer t0 = timer("HotSpotJVMCIRuntime.<clinit>")) {
                try (InitTimer t = timer("StartupEventListener.beforeJVMCIStartup")) {
                    for (StartupEventListener l : Services.load(StartupEventListener.class)) {
                        l.beforeJVMCIStartup();
                    }
                }
                try (InitTimer t = timer("HotSpotJVMCIRuntime.<init>")) {
                    instance = new HotSpotJVMCIRuntime();
                }
                try (InitTimer t = timer("HotSpotJVMCIRuntime.completeInitialization")) {
                    instance.completeInitialization();
                }
            }
        }
    }

    public static HotSpotJVMCIRuntime runtime() {
        assert DelayedInit.instance != null;
        return DelayedInit.instance;
    }

    public void completeInitialization() {
        compiler = HotSpotJVMCICompilerConfig.getCompilerFactory().createCompiler(this);
        for (HotSpotVMEventListener vmEventListener : vmEventListeners) {
            vmEventListener.completeInitialization(this);
        }
    }

    public static HotSpotJVMCIBackendFactory findFactory(String architecture) {
        for (HotSpotJVMCIBackendFactory factory : Services.load(HotSpotJVMCIBackendFactory.class)) {
            if (factory.getArchitecture().equalsIgnoreCase(architecture)) {
                return factory;
            }
        }
        throw new JVMCIError("No JVMCI runtime available for the %s architecture", architecture);
    }

    public static JavaKind getHostWordKind() {
        return runtime().getHostJVMCIBackend().getCodeCache().getTarget().wordKind;
    }

    protected final CompilerToVM compilerToVm;

    protected final HotSpotVMConfig config;

    private final JVMCIBackend hostBackend;

    private Compiler compiler;

    protected final JVMCIMetaAccessContext metaAccessContext;

    private final Map<Class<? extends Architecture>, JVMCIBackend> backends = new HashMap<>();

    private final Iterable<HotSpotVMEventListener> vmEventListeners;

    @SuppressWarnings("try")
    private HotSpotJVMCIRuntime() {
        compilerToVm = new CompilerToVM();
        try (InitTimer t = timer("HotSpotVMConfig<init>")) {
            config = new HotSpotVMConfig(compilerToVm);
        }
        String hostArchitecture = config.getHostArchitectureName();
        HotSpotJVMCIBackendFactory factory;
        try (InitTimer t = timer("find factory:", hostArchitecture)) {
            factory = findFactory(hostArchitecture);
        }
        CompilerFactory compilerFactory = HotSpotJVMCICompilerConfig.getCompilerFactory();
        try (InitTimer t = timer("create JVMCI backend:", hostArchitecture)) {
            hostBackend = registerBackend(factory.createJVMCIBackend(this, compilerFactory, null));
        }
        vmEventListeners = Services.load(HotSpotVMEventListener.class);
        JVMCIMetaAccessContext context = null;
        for (HotSpotVMEventListener vmEventListener : vmEventListeners) {
            context = vmEventListener.createMetaAccessContext(this);
            if (context != null) {
                break;
            }
        }
        if (context == null) {
            context = new HotSpotJVMCIMetaAccessContext();
        }
        metaAccessContext = context;
    }

    private JVMCIBackend registerBackend(JVMCIBackend backend) {
        Class<? extends Architecture> arch = backend.getCodeCache().getTarget().arch.getClass();
        JVMCIBackend oldValue = backends.put(arch, backend);
        assert oldValue == null : "cannot overwrite existing backend for architecture " + arch.getSimpleName();
        return backend;
    }

    public ResolvedJavaType fromClass(Class<?> javaClass) {
        return metaAccessContext.fromClass(javaClass);
    }

    public HotSpotVMConfig getConfig() {
        return config;
    }

    public CompilerToVM getCompilerToVM() {
        return compilerToVm;
    }

    public JVMCIMetaAccessContext getMetaAccessContext() {
        return metaAccessContext;
    }

    public Compiler getCompiler() {
        return compiler;
    }

    public JavaType lookupType(String name, HotSpotResolvedObjectType accessingType, boolean resolve) {
        Objects.requireNonNull(accessingType, "cannot resolve type without an accessing class");
        if (name.length() == 1) {
            JavaKind kind = JavaKind.fromPrimitiveOrVoidTypeChar(name.charAt(0));
            return fromClass(kind.toJavaClass());
        }
        HotSpotResolvedObjectTypeImpl hsAccessingType = (HotSpotResolvedObjectTypeImpl) accessingType;
        final HotSpotResolvedObjectTypeImpl klass = compilerToVm.lookupType(name, hsAccessingType.mirror(), resolve);
        if (klass == null) {
            assert resolve == false;
            return HotSpotUnresolvedJavaType.create(this, name);
        }
        return klass;
    }

    public JVMCIBackend getHostJVMCIBackend() {
        return hostBackend;
    }

    public <T extends Architecture> JVMCIBackend getJVMCIBackend(Class<T> arch) {
        assert arch != Architecture.class;
        return backends.get(arch);
    }

    public Map<Class<? extends Architecture>, JVMCIBackend> getBackends() {
        return Collections.unmodifiableMap(backends);
    }

    @SuppressWarnings({ "unused" })
    private void compileMethod(HotSpotResolvedJavaMethod method, int entryBCI, long jvmciEnv, int id) {
        compiler.compileMethod(method, entryBCI, jvmciEnv, id);
    }

    @SuppressWarnings({ "unused" })
    private void shutdown() throws Exception {
        for (HotSpotVMEventListener vmEventListener : vmEventListeners) {
            vmEventListener.notifyShutdown();
        }
    }

    void notifyInstall(HotSpotCodeCacheProvider hotSpotCodeCacheProvider, InstalledCode installedCode, CompilationResult compResult) {
        for (HotSpotVMEventListener vmEventListener : vmEventListeners) {
            vmEventListener.notifyInstall(hotSpotCodeCacheProvider, installedCode, compResult);
        }
    }
}
