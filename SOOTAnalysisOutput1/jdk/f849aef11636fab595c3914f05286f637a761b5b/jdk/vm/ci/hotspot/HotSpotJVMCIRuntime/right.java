package jdk.vm.ci.hotspot;

import static jdk.vm.ci.inittimer.InitTimer.timer;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import jdk.vm.ci.code.Architecture;
import jdk.vm.ci.code.CompilationResult;
import jdk.vm.ci.code.InstalledCode;
import jdk.vm.ci.common.JVMCIError;
import jdk.vm.ci.inittimer.InitTimer;
import jdk.vm.ci.meta.JVMCIMetaAccessContext;
import jdk.vm.ci.meta.JavaKind;
import jdk.vm.ci.meta.JavaType;
import jdk.vm.ci.meta.ResolvedJavaType;
import jdk.vm.ci.runtime.JVMCI;
import jdk.vm.ci.runtime.JVMCIBackend;
import jdk.vm.ci.runtime.JVMCICompiler;
import jdk.vm.ci.service.Services;

public final class HotSpotJVMCIRuntime implements HotSpotJVMCIRuntimeProvider, HotSpotProxified {

    @SuppressWarnings("try")
    static class DelayedInit {

        private static final HotSpotJVMCIRuntime instance;

        static {
            try (InitTimer t = timer("HotSpotJVMCIRuntime.<init>")) {
                instance = new HotSpotJVMCIRuntime();
            }
        }
    }

    public static HotSpotJVMCIRuntime runtime() {
        JVMCI.initialize();
        return DelayedInit.instance;
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
        return runtime().getHostJVMCIBackend().getCodeCache().getTarget().wordJavaKind;
    }

    protected final CompilerToVM compilerToVm;

    protected final HotSpotVMConfig config;

    private final JVMCIBackend hostBackend;

    private volatile JVMCICompiler compiler;

    protected final JVMCIMetaAccessContext metaAccessContext;

    private final Map<Class<? extends Architecture>, JVMCIBackend> backends = new HashMap<>();

    private final Iterable<HotSpotVMEventListener> vmEventListeners;

    @SuppressWarnings("unused")
    private final String[] trivialPrefixes;

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
        try (InitTimer t = timer("create JVMCI backend:", hostArchitecture)) {
            hostBackend = registerBackend(factory.createJVMCIBackend(this, null));
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
        if (Boolean.valueOf(System.getProperty("jvmci.printconfig"))) {
            printConfig(config, compilerToVm);
        }
        trivialPrefixes = HotSpotJVMCICompilerConfig.getCompilerFactory().getTrivialPrefixes();
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

    public JVMCICompiler getCompiler() {
        if (compiler == null) {
            synchronized (this) {
                if (compiler == null) {
                    compiler = HotSpotJVMCICompilerConfig.getCompilerFactory().createCompiler(this);
                }
            }
        }
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

    public Map<Class<? extends Architecture>, JVMCIBackend> getJVMCIBackends() {
        return Collections.unmodifiableMap(backends);
    }

    @SuppressWarnings({ "unused" })
    private void compileMethod(HotSpotResolvedJavaMethod method, int entryBCI, long jvmciEnv, int id) {
        getCompiler().compileMethod(new HotSpotCompilationRequest(method, entryBCI, jvmciEnv, id));
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

    private static void printConfig(HotSpotVMConfig config, CompilerToVM vm) {
        Field[] fields = config.getClass().getDeclaredFields();
        Map<String, Field> sortedFields = new TreeMap<>();
        for (Field f : fields) {
            if (!f.isSynthetic() && !Modifier.isStatic(f.getModifiers())) {
                f.setAccessible(true);
                sortedFields.put(f.getName(), f);
            }
        }
        for (Field f : sortedFields.values()) {
            try {
                String line = String.format("%9s %-40s = %s%n", f.getType().getSimpleName(), f.getName(), pretty(f.get(config)));
                byte[] lineBytes = line.getBytes();
                vm.writeDebugOutput(lineBytes, 0, lineBytes.length);
                vm.flushDebugOutput();
            } catch (Exception e) {
            }
        }
    }

    private static String pretty(Object value) {
        if (value == null) {
            return "null";
        }
        Class<?> klass = value.getClass();
        if (value instanceof String) {
            return "\"" + value + "\"";
        } else if (value instanceof Method) {
            return "method \"" + ((Method) value).getName() + "\"";
        } else if (value instanceof Class<?>) {
            return "class \"" + ((Class<?>) value).getSimpleName() + "\"";
        } else if (value instanceof Integer) {
            if ((Integer) value < 10) {
                return value.toString();
            }
            return value + " (0x" + Integer.toHexString((Integer) value) + ")";
        } else if (value instanceof Long) {
            if ((Long) value < 10 && (Long) value > -10) {
                return value + "l";
            }
            return value + "l (0x" + Long.toHexString((Long) value) + "l)";
        } else if (klass.isArray()) {
            StringBuilder str = new StringBuilder();
            int dimensions = 0;
            while (klass.isArray()) {
                dimensions++;
                klass = klass.getComponentType();
            }
            int length = Array.getLength(value);
            str.append(klass.getSimpleName()).append('[').append(length).append(']');
            for (int i = 1; i < dimensions; i++) {
                str.append("[]");
            }
            str.append(" {");
            for (int i = 0; i < length; i++) {
                str.append(pretty(Array.get(value, i)));
                if (i < length - 1) {
                    str.append(", ");
                }
            }
            str.append('}');
            return str.toString();
        }
        return value.toString();
    }

    public OutputStream getLogStream() {
        return new OutputStream() {

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                if (b == null) {
                    throw new NullPointerException();
                } else if (off < 0 || off > b.length || len < 0 || (off + len) > b.length || (off + len) < 0) {
                    throw new IndexOutOfBoundsException();
                } else if (len == 0) {
                    return;
                }
                compilerToVm.writeDebugOutput(b, off, len);
            }

            @Override
            public void write(int b) throws IOException {
                write(new byte[] { (byte) b }, 0, 1);
            }

            @Override
            public void flush() throws IOException {
                compilerToVm.flushDebugOutput();
            }
        };
    }

    public long[] collectCounters() {
        return compilerToVm.collectCounters();
    }
}
