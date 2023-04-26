package org.graalvm.compiler.serviceprovider;

import static java.lang.Thread.currentThread;
import static org.graalvm.compiler.serviceprovider.GraalServices.JMXService.jmx;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.concurrent.atomic.AtomicLong;
import jdk.vm.ci.services.JVMCIPermission;
import jdk.vm.ci.services.Services;

public final class GraalServices {

    private static int getJavaSpecificationVersion() {
        String value = System.getProperty("java.specification.version");
        if (value.startsWith("1.")) {
            value = value.substring(2);
        }
        return Integer.parseInt(value);
    }

    public static final int JAVA_SPECIFICATION_VERSION = getJavaSpecificationVersion();

    public static final boolean Java8OrEarlier = JAVA_SPECIFICATION_VERSION <= 8;

    public static final boolean Java11OrLater = JAVA_SPECIFICATION_VERSION >= 11;

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

    public static String getExecutionID() {
        try {
            String runtimeName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
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

    public static class JMXService {

        private final com.sun.management.ThreadMXBean threadMXBean = (com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean();

        protected long getThreadAllocatedBytes(long id) {
            return threadMXBean.getThreadAllocatedBytes(id);
        }

        protected long getCurrentThreadCpuTime() {
            return threadMXBean.getCurrentThreadCpuTime();
        }

        protected boolean isThreadAllocatedMemorySupported() {
            return threadMXBean.isThreadAllocatedMemorySupported();
        }

        protected boolean isCurrentThreadCpuTimeSupported() {
            return threadMXBean.isCurrentThreadCpuTimeSupported();
        }

        protected List<String> getInputArguments() {
            return java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments();
        }

        static final JMXService jmx = new JMXService();
    }

    public static long getThreadAllocatedBytes(long id) {
        return jmx.getThreadAllocatedBytes(id);
    }

    public static long getCurrentThreadAllocatedBytes() {
        return getThreadAllocatedBytes(currentThread().getId());
    }

    public static long getCurrentThreadCpuTime() {
        return jmx.getCurrentThreadCpuTime();
    }

    public static boolean isThreadAllocatedMemorySupported() {
        return jmx.isThreadAllocatedMemorySupported();
    }

    public static boolean isCurrentThreadCpuTimeSupported() {
        return jmx.isCurrentThreadCpuTimeSupported();
    }

    public static List<String> getInputArguments() {
        return jmx.getInputArguments();
    }
}
