package org.graalvm.compiler.serviceprovider;

import static java.lang.Thread.currentThread;
import static jdk.vm.ci.services.Services.IS_BUILDING_NATIVE_IMAGE;
import static jdk.vm.ci.services.Services.IS_IN_NATIVE_IMAGE;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicLong;
import jdk.vm.ci.code.VirtualObject;
import jdk.vm.ci.meta.ConstantPool;
import jdk.vm.ci.meta.JavaType;
import jdk.vm.ci.meta.ResolvedJavaType;
import jdk.vm.ci.meta.SpeculationLog.SpeculationReason;
import jdk.vm.ci.runtime.JVMCI;
import jdk.vm.ci.services.JVMCIPermission;
import jdk.vm.ci.services.Services;

public final class GraalServices {

    private static final Map<Class<?>, List<?>> servicesCache = IS_BUILDING_NATIVE_IMAGE ? new HashMap<>() : null;

    private static final Constructor<? extends SpeculationReason> encodedSpeculationReasonConstructor;

    static {
        Constructor<? extends SpeculationReason> constructor = null;
        try {
            @SuppressWarnings("unchecked")
            Class<? extends SpeculationReason> theClass = (Class<? extends SpeculationReason>) Class.forName("jdk.vm.ci.meta.EncodedSpeculationReason");
            constructor = theClass.getDeclaredConstructor(Integer.TYPE, String.class, Object[].class);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
            throw new InternalError("EncodedSpeculationReason exists but constructor is missing", e);
        }
        encodedSpeculationReasonConstructor = constructor;
    }

    private GraalServices() {
    }

    @SuppressWarnings("unchecked")
    public static <S> Iterable<S> load(Class<S> service) {
        if (IS_IN_NATIVE_IMAGE || IS_BUILDING_NATIVE_IMAGE) {
            List<?> list = servicesCache.get(service);
            if (list != null) {
                return (Iterable<S>) list;
            }
            if (IS_IN_NATIVE_IMAGE) {
                throw new InternalError(String.format("No %s providers found when building native image", service.getName()));
            }
        }
        Iterable<S> providers = load0(service);
        if (IS_BUILDING_NATIVE_IMAGE) {
            synchronized (servicesCache) {
                ArrayList<S> providersList = new ArrayList<>();
                for (S provider : providers) {
                    Module module = provider.getClass().getModule();
                    if (isHotSpotGraalModule(module.getName())) {
                        providersList.add(provider);
                    }
                }
                providers = providersList;
                servicesCache.put(service, providersList);
                return providers;
            }
        }
        return providers;
    }

    private static boolean isHotSpotGraalModule(String name) {
        if (name != null) {
            return name.equals("jdk.internal.vm.compiler") || name.equals("jdk.internal.vm.compiler.management") || name.equals("com.oracle.graal.graal_enterprise");
        }
        return false;
    }

    protected static <S> Iterable<S> load0(Class<S> service) {
        Module module = GraalServices.class.getModule();
        if (!module.canUse(service)) {
            module.addUses(service);
        }
        ModuleLayer layer = module.getLayer();
        Iterable<S> iterable = ServiceLoader.load(layer, service);
        return new Iterable<>() {

            @Override
            public Iterator<S> iterator() {
                Iterator<S> iterator = iterable.iterator();
                return new Iterator<>() {

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public S next() {
                        S provider = iterator.next();
                        openJVMCITo(provider.getClass());
                        return provider;
                    }

                    @Override
                    public void remove() {
                        iterator.remove();
                    }
                };
            }
        };
    }

    static void openJVMCITo(Class<?> other) {
        if (IS_IN_NATIVE_IMAGE) {
            return;
        }
        Module jvmciModule = JVMCI_MODULE;
        Module otherModule = other.getModule();
        if (jvmciModule != otherModule) {
            for (String pkg : jvmciModule.getPackages()) {
                if (!jvmciModule.isOpen(pkg, otherModule)) {
                    JVMCI.getRuntime();
                    jvmciModule.addOpens(pkg, otherModule);
                }
            }
        }
    }

    public static <S> S loadSingle(Class<S> service, boolean required) {
        assert !service.getName().startsWith("jdk.vm.ci") : "JVMCI services must be loaded via " + Services.class.getName();
        Iterable<S> providers = load(service);
        S singleProvider = null;
        try {
            for (Iterator<S> it = providers.iterator(); it.hasNext(); ) {
                singleProvider = it.next();
                if (it.hasNext()) {
                    S other = it.next();
                    throw new InternalError(String.format("Multiple %s providers found: %s, %s", service.getName(), singleProvider.getClass().getName(), other.getClass().getName()));
                }
            }
        } catch (ServiceConfigurationError e) {
        }
        if (singleProvider == null) {
            if (required) {
                throw new InternalError(String.format("No provider for %s found", service.getName()));
            }
        }
        return singleProvider;
    }

    public static InputStream getClassfileAsStream(Class<?> c) throws IOException {
        String classfilePath = c.getName().replace('.', '/') + ".class";
        return c.getModule().getResourceAsStream(classfilePath);
    }

    private static final Module JVMCI_MODULE = Services.class.getModule();

    private static final String JVMCI_RUNTIME_PACKAGE = "jdk.vm.ci.runtime";

    static {
        assert JVMCI_MODULE.getPackages().contains(JVMCI_RUNTIME_PACKAGE);
    }

    public static boolean isToStringTrusted(Class<?> c) {
        if (IS_IN_NATIVE_IMAGE) {
            return true;
        }
        Module module = c.getModule();
        Module jvmciModule = JVMCI_MODULE;
        if (module == jvmciModule || jvmciModule.isOpen(JVMCI_RUNTIME_PACKAGE, module)) {
            return true;
        }
        return false;
    }

    static SpeculationReason createSpeculationReason(int groupId, String groupName, Object... context) {
        if (encodedSpeculationReasonConstructor != null) {
            SpeculationEncodingAdapter adapter = new SpeculationEncodingAdapter();
            try {
                Object[] flattened = adapter.flatten(context);
                return encodedSpeculationReasonConstructor.newInstance(groupId, groupName, flattened);
            } catch (Throwable throwable) {
                throw new InternalError(throwable);
            }
        }
        return new UnencodedSpeculationReason(groupId, groupName, context);
    }

    public static String getExecutionID() {
        return Long.toString(ProcessHandle.current().pid());
    }

    private static final AtomicLong globalTimeStamp = new AtomicLong();

    public static long getGlobalTimeStamp() {
        if (globalTimeStamp.get() == 0) {
            globalTimeStamp.compareAndSet(0, System.currentTimeMillis());
        }
        return globalTimeStamp.get();
    }

    public static long getThreadAllocatedBytes(long id) {
        JMXService jmx = JMXService.instance;
        if (jmx == null) {
            throw new UnsupportedOperationException();
        }
        return jmx.getThreadAllocatedBytes(id);
    }

    public static long getCurrentThreadAllocatedBytes() {
        return getThreadAllocatedBytes(currentThread().getId());
    }

    public static long getCurrentThreadCpuTime() {
        JMXService jmx = JMXService.instance;
        if (jmx == null) {
            throw new UnsupportedOperationException();
        }
        return jmx.getCurrentThreadCpuTime();
    }

    public static boolean isThreadAllocatedMemorySupported() {
        JMXService jmx = JMXService.instance;
        if (jmx == null) {
            return false;
        }
        return jmx.isThreadAllocatedMemorySupported();
    }

    public static boolean isCurrentThreadCpuTimeSupported() {
        JMXService jmx = JMXService.instance;
        if (jmx == null) {
            return false;
        }
        return jmx.isCurrentThreadCpuTimeSupported();
    }

    public static List<String> getInputArguments() {
        JMXService jmx = JMXService.instance;
        if (jmx == null) {
            return null;
        }
        return jmx.getInputArguments();
    }

    public static float fma(float a, float b, float c) {
        return Math.fma(a, b, c);
    }

    public static double fma(double a, double b, double c) {
        return Math.fma(a, b, c);
    }

    static final Method virtualObjectGetMethod;

    static {
        Method virtualObjectGet = null;
        try {
            virtualObjectGet = VirtualObject.class.getDeclaredMethod("get", ResolvedJavaType.class, Integer.TYPE, Boolean.TYPE);
        } catch (Exception e) {
        }
        virtualObjectGetMethod = virtualObjectGet;
    }

    public static VirtualObject createVirtualObject(ResolvedJavaType type, int id, boolean isAutoBox) {
        if (virtualObjectGetMethod != null) {
            try {
                return (VirtualObject) virtualObjectGetMethod.invoke(null, type, id, isAutoBox);
            } catch (Throwable throwable) {
                throw new InternalError(throwable);
            }
        }
        return VirtualObject.get(type, id);
    }

    public static int getJavaUpdateVersion() {
        return Runtime.version().update();
    }

    private static final Method constantPoolLookupReferencedType;

    static {
        Method lookupReferencedType = null;
        Class<?> constantPool = ConstantPool.class;
        try {
            lookupReferencedType = constantPool.getDeclaredMethod("lookupReferencedType", Integer.TYPE, Integer.TYPE);
        } catch (NoSuchMethodException e) {
        }
        constantPoolLookupReferencedType = lookupReferencedType;
    }

    public static JavaType lookupReferencedType(ConstantPool constantPool, int cpi, int opcode) {
        if (constantPoolLookupReferencedType != null) {
            try {
                return (JavaType) constantPoolLookupReferencedType.invoke(constantPool, cpi, opcode);
            } catch (Error e) {
                throw e;
            } catch (Throwable throwable) {
                throw new InternalError(throwable);
            }
        }
        throw new InternalError("This JVMCI version doesn't support ConstantPool.lookupReferencedType()");
    }

    public static boolean hasLookupReferencedType() {
        return constantPoolLookupReferencedType != null;
    }
}