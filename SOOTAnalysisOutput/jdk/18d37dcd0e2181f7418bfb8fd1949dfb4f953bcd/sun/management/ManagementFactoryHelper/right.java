package sun.management;

import java.lang.management.*;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import jdk.internal.misc.JavaNioAccess;
import jdk.internal.misc.SharedSecrets;
import sun.util.logging.LoggingSupport;
import java.util.ArrayList;
import java.util.List;

public class ManagementFactoryHelper {

    static {
        jdk.internal.misc.Unsafe.getUnsafe().ensureClassInitialized(ManagementFactory.class);
    }

    private static final VMManagement jvm = new VMManagementImpl();

    private ManagementFactoryHelper() {
    }

    public static VMManagement getVMManagement() {
        return jvm;
    }

    private static ClassLoadingImpl classMBean = null;

    private static MemoryImpl memoryMBean = null;

    private static ThreadImpl threadMBean = null;

    private static RuntimeImpl runtimeMBean = null;

    private static CompilationImpl compileMBean = null;

    private static BaseOperatingSystemImpl osMBean = null;

    public static synchronized ClassLoadingMXBean getClassLoadingMXBean() {
        if (classMBean == null) {
            classMBean = new ClassLoadingImpl(jvm);
        }
        return classMBean;
    }

    public static synchronized MemoryMXBean getMemoryMXBean() {
        if (memoryMBean == null) {
            memoryMBean = new MemoryImpl(jvm);
        }
        return memoryMBean;
    }

    public static synchronized ThreadMXBean getThreadMXBean() {
        if (threadMBean == null) {
            threadMBean = new ThreadImpl(jvm);
        }
        return threadMBean;
    }

    public static synchronized RuntimeMXBean getRuntimeMXBean() {
        if (runtimeMBean == null) {
            runtimeMBean = new RuntimeImpl(jvm);
        }
        return runtimeMBean;
    }

    public static synchronized CompilationMXBean getCompilationMXBean() {
        if (compileMBean == null && jvm.getCompilerName() != null) {
            compileMBean = new CompilationImpl(jvm);
        }
        return compileMBean;
    }

    public static synchronized OperatingSystemMXBean getOperatingSystemMXBean() {
        if (osMBean == null) {
            osMBean = new BaseOperatingSystemImpl(jvm);
        }
        return osMBean;
    }

    public static List<MemoryPoolMXBean> getMemoryPoolMXBeans() {
        MemoryPoolMXBean[] pools = MemoryImpl.getMemoryPools();
        List<MemoryPoolMXBean> list = new ArrayList<>(pools.length);
        for (MemoryPoolMXBean p : pools) {
            list.add(p);
        }
        return list;
    }

    public static List<MemoryManagerMXBean> getMemoryManagerMXBeans() {
        MemoryManagerMXBean[] mgrs = MemoryImpl.getMemoryManagers();
        List<MemoryManagerMXBean> result = new ArrayList<>(mgrs.length);
        for (MemoryManagerMXBean m : mgrs) {
            result.add(m);
        }
        return result;
    }

    public static List<GarbageCollectorMXBean> getGarbageCollectorMXBeans() {
        MemoryManagerMXBean[] mgrs = MemoryImpl.getMemoryManagers();
        List<GarbageCollectorMXBean> result = new ArrayList<>(mgrs.length);
        for (MemoryManagerMXBean m : mgrs) {
            if (GarbageCollectorMXBean.class.isInstance(m)) {
                result.add(GarbageCollectorMXBean.class.cast(m));
            }
        }
        return result;
    }

    public static PlatformLoggingMXBean getPlatformLoggingMXBean() {
        if (LoggingSupport.isAvailable()) {
            return PlatformLoggingImpl.instance;
        } else {
            return null;
        }
    }

    public interface LoggingMXBean extends PlatformLoggingMXBean, java.util.logging.LoggingMXBean {
    }

    static class PlatformLoggingImpl implements LoggingMXBean {

        final static PlatformLoggingMXBean instance = new PlatformLoggingImpl();

        final static String LOGGING_MXBEAN_NAME = "java.util.logging:type=Logging";

        private volatile ObjectName objname;

        @Override
        public ObjectName getObjectName() {
            ObjectName result = objname;
            if (result == null) {
                synchronized (this) {
                    result = objname;
                    if (result == null) {
                        result = Util.newObjectName(LOGGING_MXBEAN_NAME);
                        objname = result;
                    }
                }
            }
            return result;
        }

        @Override
        public java.util.List<String> getLoggerNames() {
            return LoggingSupport.getLoggerNames();
        }

        @Override
        public String getLoggerLevel(String loggerName) {
            return LoggingSupport.getLoggerLevel(loggerName);
        }

        @Override
        public void setLoggerLevel(String loggerName, String levelName) {
            LoggingSupport.setLoggerLevel(loggerName, levelName);
        }

        @Override
        public String getParentLoggerName(String loggerName) {
            return LoggingSupport.getParentLoggerName(loggerName);
        }
    }

    private static List<BufferPoolMXBean> bufferPools = null;

    public static synchronized List<BufferPoolMXBean> getBufferPoolMXBeans() {
        if (bufferPools == null) {
            bufferPools = new ArrayList<>(2);
            bufferPools.add(createBufferPoolMXBean(SharedSecrets.getJavaNioAccess().getDirectBufferPool()));
            bufferPools.add(createBufferPoolMXBean(sun.nio.ch.FileChannelImpl.getMappedBufferPool()));
        }
        return bufferPools;
    }

    private final static String BUFFER_POOL_MXBEAN_NAME = "java.nio:type=BufferPool";

    private static BufferPoolMXBean createBufferPoolMXBean(final JavaNioAccess.BufferPool pool) {
        return new BufferPoolMXBean() {

            private volatile ObjectName objname;

            @Override
            public ObjectName getObjectName() {
                ObjectName result = objname;
                if (result == null) {
                    synchronized (this) {
                        result = objname;
                        if (result == null) {
                            result = Util.newObjectName(BUFFER_POOL_MXBEAN_NAME + ",name=" + pool.getName());
                            objname = result;
                        }
                    }
                }
                return result;
            }

            @Override
            public String getName() {
                return pool.getName();
            }

            @Override
            public long getCount() {
                return pool.getCount();
            }

            @Override
            public long getTotalCapacity() {
                return pool.getTotalCapacity();
            }

            @Override
            public long getMemoryUsed() {
                return pool.getMemoryUsed();
            }
        };
    }

    private static HotspotRuntime hsRuntimeMBean = null;

    private static HotspotClassLoading hsClassMBean = null;

    private static HotspotThread hsThreadMBean = null;

    private static HotspotCompilation hsCompileMBean = null;

    private static HotspotMemory hsMemoryMBean = null;

    public static synchronized HotspotRuntimeMBean getHotspotRuntimeMBean() {
        if (hsRuntimeMBean == null) {
            hsRuntimeMBean = new HotspotRuntime(jvm);
        }
        return hsRuntimeMBean;
    }

    public static synchronized HotspotClassLoadingMBean getHotspotClassLoadingMBean() {
        if (hsClassMBean == null) {
            hsClassMBean = new HotspotClassLoading(jvm);
        }
        return hsClassMBean;
    }

    public static synchronized HotspotThreadMBean getHotspotThreadMBean() {
        if (hsThreadMBean == null) {
            hsThreadMBean = new HotspotThread(jvm);
        }
        return hsThreadMBean;
    }

    public static synchronized HotspotMemoryMBean getHotspotMemoryMBean() {
        if (hsMemoryMBean == null) {
            hsMemoryMBean = new HotspotMemory(jvm);
        }
        return hsMemoryMBean;
    }

    public static synchronized HotspotCompilationMBean getHotspotCompilationMBean() {
        if (hsCompileMBean == null) {
            hsCompileMBean = new HotspotCompilation(jvm);
        }
        return hsCompileMBean;
    }

    private static void addMBean(MBeanServer mbs, Object mbean, String mbeanName) {
        try {
            final ObjectName objName = Util.newObjectName(mbeanName);
            final MBeanServer mbs0 = mbs;
            final Object mbean0 = mbean;
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {

                public Void run() throws MBeanRegistrationException, NotCompliantMBeanException {
                    try {
                        mbs0.registerMBean(mbean0, objName);
                        return null;
                    } catch (InstanceAlreadyExistsException e) {
                    }
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw Util.newException(e.getException());
        }
    }

    private final static String HOTSPOT_CLASS_LOADING_MBEAN_NAME = "sun.management:type=HotspotClassLoading";

    private final static String HOTSPOT_COMPILATION_MBEAN_NAME = "sun.management:type=HotspotCompilation";

    private final static String HOTSPOT_MEMORY_MBEAN_NAME = "sun.management:type=HotspotMemory";

    private static final String HOTSPOT_RUNTIME_MBEAN_NAME = "sun.management:type=HotspotRuntime";

    private final static String HOTSPOT_THREAD_MBEAN_NAME = "sun.management:type=HotspotThreading";

    static void registerInternalMBeans(MBeanServer mbs) {
        addMBean(mbs, getHotspotClassLoadingMBean(), HOTSPOT_CLASS_LOADING_MBEAN_NAME);
        addMBean(mbs, getHotspotMemoryMBean(), HOTSPOT_MEMORY_MBEAN_NAME);
        addMBean(mbs, getHotspotRuntimeMBean(), HOTSPOT_RUNTIME_MBEAN_NAME);
        addMBean(mbs, getHotspotThreadMBean(), HOTSPOT_THREAD_MBEAN_NAME);
        if (getCompilationMXBean() != null) {
            addMBean(mbs, getHotspotCompilationMBean(), HOTSPOT_COMPILATION_MBEAN_NAME);
        }
    }

    private static void unregisterMBean(MBeanServer mbs, String mbeanName) {
        try {
            final ObjectName objName = Util.newObjectName(mbeanName);
            final MBeanServer mbs0 = mbs;
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {

                public Void run() throws MBeanRegistrationException, RuntimeOperationsException {
                    try {
                        mbs0.unregisterMBean(objName);
                    } catch (InstanceNotFoundException e) {
                    }
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw Util.newException(e.getException());
        }
    }

    static void unregisterInternalMBeans(MBeanServer mbs) {
        unregisterMBean(mbs, HOTSPOT_CLASS_LOADING_MBEAN_NAME);
        unregisterMBean(mbs, HOTSPOT_MEMORY_MBEAN_NAME);
        unregisterMBean(mbs, HOTSPOT_RUNTIME_MBEAN_NAME);
        unregisterMBean(mbs, HOTSPOT_THREAD_MBEAN_NAME);
        if (getCompilationMXBean() != null) {
            unregisterMBean(mbs, HOTSPOT_COMPILATION_MBEAN_NAME);
        }
    }

    public static boolean isThreadSuspended(int state) {
        return ((state & JMM_THREAD_STATE_FLAG_SUSPENDED) != 0);
    }

    public static boolean isThreadRunningNative(int state) {
        return ((state & JMM_THREAD_STATE_FLAG_NATIVE) != 0);
    }

    public static Thread.State toThreadState(int state) {
        int threadStatus = state & ~JMM_THREAD_STATE_FLAG_MASK;
        return sun.misc.VM.toThreadState(threadStatus);
    }

    private static final int JMM_THREAD_STATE_FLAG_MASK = 0xFFF00000;

    private static final int JMM_THREAD_STATE_FLAG_SUSPENDED = 0x00100000;

    private static final int JMM_THREAD_STATE_FLAG_NATIVE = 0x00400000;

    private static MemoryPoolMXBean createMemoryPool(String name, boolean isHeap, long uThreshold, long gcThreshold) {
        return new MemoryPoolImpl(name, isHeap, uThreshold, gcThreshold);
    }

    private static MemoryManagerMXBean createMemoryManager(String name) {
        return new MemoryManagerImpl(name);
    }

    private static GarbageCollectorMXBean createGarbageCollector(String name, String type) {
        return new GarbageCollectorImpl(name);
    }
}
