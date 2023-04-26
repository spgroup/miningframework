package org.graalvm.compiler.serviceprovider;

import static java.lang.Thread.currentThread;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jdk.vm.ci.code.DebugInfo;
import jdk.vm.ci.code.VirtualObject;
import jdk.vm.ci.code.site.Infopoint;
import jdk.vm.ci.code.site.InfopointReason;
import jdk.vm.ci.meta.ConstantPool;
import jdk.vm.ci.meta.EncodedSpeculationReason;
import jdk.vm.ci.meta.JavaType;
import jdk.vm.ci.meta.ResolvedJavaType;
import jdk.vm.ci.meta.SpeculationLog.SpeculationReason;
import jdk.vm.ci.services.JVMCIPermission;
import jdk.vm.ci.services.Services;

public final class GraalServices {

    private GraalServices() {
    }

    public static <S> Iterable<S> load(Class<S> service) {
        assert !service.getName().startsWith("jdk.vm.ci") : "JVMCI services must be loaded via " + Services.class.getName();
        return Services.load(service);
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

    @SuppressWarnings("unused")
    public static InputStream getClassfileAsStream(Class<?> c) throws IOException {
        String classfilePath = c.getName().replace('.', '/') + ".class";
        ClassLoader cl = c.getClassLoader();
        if (cl == null) {
            return ClassLoader.getSystemResourceAsStream(classfilePath);
        }
        return cl.getResourceAsStream(classfilePath);
    }

    private static final ClassLoader JVMCI_LOADER = GraalServices.class.getClassLoader();

    private static final ClassLoader JVMCI_PARENT_LOADER = JVMCI_LOADER == null ? null : JVMCI_LOADER.getParent();

    static {
        assert JVMCI_PARENT_LOADER == null || JVMCI_PARENT_LOADER.getParent() == null;
    }

    public static boolean isToStringTrusted(Class<?> c) {
        ClassLoader cl = c.getClassLoader();
        return cl == null || cl == JVMCI_LOADER || cl == JVMCI_PARENT_LOADER;
    }

    static SpeculationReason createSpeculationReason(int groupId, String groupName, Object... context) {
        SpeculationEncodingAdapter adapter = new SpeculationEncodingAdapter();
        return new EncodedSpeculationReason(groupId, groupName, adapter.flatten(context));
    }

    public static String getExecutionID() {
        try {
            if (Lazy.runtimeMXBean == null) {
                return String.valueOf(getGlobalTimeStamp());
            }
            String runtimeName = Lazy.runtimeMXBean.getName();
            try {
                int index = runtimeName.indexOf('@');
                if (index != -1) {
                    long pid = Long.parseLong(runtimeName.substring(0, index));
                    return Long.toString(pid);
                }
            } catch (NumberFormatException e) {
            }
            return runtimeName;
        } catch (LinkageError err) {
            return String.valueOf(getGlobalTimeStamp());
        }
    }

    private static final AtomicLong globalTimeStamp = new AtomicLong();

    public static long getGlobalTimeStamp() {
        if (globalTimeStamp.get() == 0) {
            globalTimeStamp.compareAndSet(0, System.currentTimeMillis());
        }
        return globalTimeStamp.get();
    }

    static class Lazy {

        static final com.sun.management.ThreadMXBean threadMXBean;

        static final RuntimeMXBean runtimeMXBean;

        static {
            com.sun.management.ThreadMXBean resultThread;
            RuntimeMXBean resultRuntime;
            try {
                resultThread = (com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean();
                resultRuntime = java.lang.management.ManagementFactory.getRuntimeMXBean();
            } catch (UnsatisfiedLinkError | NoClassDefFoundError | UnsupportedOperationException e) {
                resultThread = null;
                resultRuntime = null;
            }
            threadMXBean = resultThread;
            runtimeMXBean = resultRuntime;
        }
    }

    public static long getThreadAllocatedBytes(long id) {
        if (Lazy.threadMXBean == null) {
            throw new UnsupportedOperationException();
        }
        return Lazy.threadMXBean.getThreadAllocatedBytes(id);
    }

    public static long getCurrentThreadAllocatedBytes() {
        return getThreadAllocatedBytes(currentThread().getId());
    }

    public static long getCurrentThreadCpuTime() {
        if (Lazy.threadMXBean == null) {
            throw new UnsupportedOperationException();
        }
        return Lazy.threadMXBean.getCurrentThreadCpuTime();
    }

    public static boolean isThreadAllocatedMemorySupported() {
        if (Lazy.threadMXBean == null) {
            return false;
        }
        return Lazy.threadMXBean.isThreadAllocatedMemorySupported();
    }

    public static boolean isCurrentThreadCpuTimeSupported() {
        if (Lazy.threadMXBean == null) {
            return false;
        }
        return Lazy.threadMXBean.isCurrentThreadCpuTimeSupported();
    }

    public static List<String> getInputArguments() {
        if (Lazy.runtimeMXBean == null) {
            return null;
        }
        return Lazy.runtimeMXBean.getInputArguments();
    }

    public static float fma(float a, float b, float c) {
        float result = (float) (((double) a * (double) b) + c);
        return result;
    }

    public static double fma(double a, double b, double c) {
        if (Double.isNaN(a) || Double.isNaN(b) || Double.isNaN(c)) {
            return Double.NaN;
        } else {
            boolean infiniteA = Double.isInfinite(a);
            boolean infiniteB = Double.isInfinite(b);
            boolean infiniteC = Double.isInfinite(c);
            double result;
            if (infiniteA || infiniteB || infiniteC) {
                if (infiniteA && b == 0.0 || infiniteB && a == 0.0) {
                    return Double.NaN;
                }
                double product = a * b;
                if (Double.isInfinite(product) && !infiniteA && !infiniteB) {
                    assert Double.isInfinite(c);
                    return c;
                } else {
                    result = product + c;
                    assert !Double.isFinite(result);
                    return result;
                }
            } else {
                BigDecimal product = (new BigDecimal(a)).multiply(new BigDecimal(b));
                if (c == 0.0) {
                    if (a == 0.0 || b == 0.0) {
                        return a * b + c;
                    } else {
                        return product.doubleValue();
                    }
                } else {
                    return product.add(new BigDecimal(c)).doubleValue();
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public static VirtualObject createVirtualObject(ResolvedJavaType type, int id, boolean isAutoBox) {
        return VirtualObject.get(type, id, isAutoBox);
    }

    public static int getJavaUpdateVersion() {
        Pattern p = Pattern.compile("\\d+\\.([^-]+)-.*");
        String vmVersion = Services.getSavedProperties().get("java.vm.version");
        Matcher matcher = p.matcher(vmVersion);
        if (!matcher.matches()) {
            throw new InternalError("Unexpected java.vm.version value: " + vmVersion);
        }
        return Integer.parseInt(matcher.group(1));
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

    private static final Constructor<?> implicitExceptionDispatchConstructor;

    static {
        Constructor<?> tempConstructor;
        try {
            Class<?> implicitExceptionDispatch = Class.forName("jdk.vm.ci.code.site.ImplicitExceptionDispatch");
            tempConstructor = implicitExceptionDispatch.getConstructor(int.class, int.class, DebugInfo.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            tempConstructor = null;
        }
        implicitExceptionDispatchConstructor = tempConstructor;
    }

    public static boolean supportsArbitraryImplicitException() {
        return implicitExceptionDispatchConstructor != null && !"sparcv9".equals(Services.getSavedProperties().get("os.arch"));
    }

    public static Infopoint genImplicitException(int pcOffset, int dispatchOffset, DebugInfo debugInfo) {
        if (implicitExceptionDispatchConstructor == null) {
            if (pcOffset != dispatchOffset) {
                throw new InternalError("This JVMCI version doesn't support dispatching implicit exception to an arbitrary address.");
            }
            return new Infopoint(pcOffset, debugInfo, InfopointReason.IMPLICIT_EXCEPTION);
        }
        try {
            return (Infopoint) implicitExceptionDispatchConstructor.newInstance(pcOffset, dispatchOffset, debugInfo);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new InternalError("Exception when instantiating implicit exception dispatch", e);
        }
    }
}
