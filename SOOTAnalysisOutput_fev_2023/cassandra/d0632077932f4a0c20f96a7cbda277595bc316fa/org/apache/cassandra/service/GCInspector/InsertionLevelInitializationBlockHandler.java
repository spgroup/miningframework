package org.apache.cassandra.service;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.lifecycle.LifecycleTransaction;
import org.apache.cassandra.utils.StatusLogger;

public class GCInspector implements NotificationListener, GCInspectorMXBean {

    public static final String MBEAN_NAME = "org.apache.cassandra.service:type=GCInspector";

    private static final Logger logger = LoggerFactory.getLogger(GCInspector.class);

    final static long MIN_LOG_DURATION = 200;

    final static long GC_WARN_THRESHOLD_IN_MS = DatabaseDescriptor.getGCWarnThreshold();

    final static long STAT_THRESHOLD = Math.min(GC_WARN_THRESHOLD_IN_MS != 0 ? GC_WARN_THRESHOLD_IN_MS : MIN_LOG_DURATION, MIN_LOG_DURATION);

    final static Field BITS_TOTAL_CAPACITY;

    static {<<<<<<< MINE

        Field temp = null;
        try {
            Class<?> bitsClass = Class.forName("java.nio.Bits");
            Field f = bitsClass.getDeclaredField("totalCapacity");
            f.setAccessible(true);
            temp = f;
        } catch (Throwable t) {
            logger.debug("Error accessing field of java.nio.Bits", t);
        }
        BITS_TOTAL_CAPACITY = temp;
    
=======

        Field temp = null;
        try {
            Class<?> bitsClass = Class.forName("java.nio.Bits");
            Field f = bitsClass.getDeclaredField("totalCapacity");
            f.setAccessible(true);
            temp = f;
        } catch (Throwable t) {
            logger.debug("Error accessing field of java.nio.Bits", t);
        }
        BITS_TOTAL_CAPACITY = temp;
    
>>>>>>> YOURS
}

    static final class State {

        final double maxRealTimeElapsed;

        final double totalRealTimeElapsed;

        final double sumSquaresRealTimeElapsed;

        final double totalBytesReclaimed;

        final double count;

        final long startNanos;

        State(double extraElapsed, double extraBytes, State prev) {
            this.totalRealTimeElapsed = prev.totalRealTimeElapsed + extraElapsed;
            this.totalBytesReclaimed = prev.totalBytesReclaimed + extraBytes;
            this.sumSquaresRealTimeElapsed = prev.sumSquaresRealTimeElapsed + (extraElapsed * extraElapsed);
            this.startNanos = prev.startNanos;
            this.count = prev.count + 1;
            this.maxRealTimeElapsed = Math.max(prev.maxRealTimeElapsed, extraElapsed);
        }

        State() {
            count = maxRealTimeElapsed = sumSquaresRealTimeElapsed = totalRealTimeElapsed = totalBytesReclaimed = 0;
            startNanos = System.nanoTime();
        }
    }

    static final class GCState {

        final GarbageCollectorMXBean gcBean;

        final boolean assumeGCIsPartiallyConcurrent;

        final boolean assumeGCIsOldGen;

        private String[] keys;

        long lastGcTotalDuration = 0;

        GCState(GarbageCollectorMXBean gcBean, boolean assumeGCIsPartiallyConcurrent, boolean assumeGCIsOldGen) {
            this.gcBean = gcBean;
            this.assumeGCIsPartiallyConcurrent = assumeGCIsPartiallyConcurrent;
            this.assumeGCIsOldGen = assumeGCIsOldGen;
        }

        String[] keys(GarbageCollectionNotificationInfo info) {
            if (keys != null)
                return keys;
            keys = info.getGcInfo().getMemoryUsageBeforeGc().keySet().toArray(new String[0]);
            Arrays.sort(keys);
            return keys;
        }
    }

    final AtomicReference<State> state = new AtomicReference<>(new State());

    final Map<String, GCState> gcStates = new HashMap<>();

    public GCInspector() {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName gcName = new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");
            for (ObjectName name : mbs.queryNames(gcName, null)) {
                GarbageCollectorMXBean gc = ManagementFactory.newPlatformMXBeanProxy(mbs, name.getCanonicalName(), GarbageCollectorMXBean.class);
                gcStates.put(gc.getName(), new GCState(gc, assumeGCIsPartiallyConcurrent(gc), assumeGCIsOldGen(gc)));
            }
            mbs.registerMBean(this, new ObjectName(MBEAN_NAME));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void register() throws Exception {
        GCInspector inspector = new GCInspector();
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName gcName = new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");
        for (ObjectName name : server.queryNames(gcName, null)) {
            server.addNotificationListener(name, inspector, null, null);
        }
    }

    private static boolean assumeGCIsPartiallyConcurrent(GarbageCollectorMXBean gc) {
        switch(gc.getName()) {
            case "Copy":
            case "MarkSweepCompact":
            case "PS MarkSweep":
            case "PS Scavenge":
            case "G1 Young Generation":
            case "ParNew":
                return false;
            case "ConcurrentMarkSweep":
            case "G1 Old Generation":
                return true;
            default:
                return true;
        }
    }

    private static boolean assumeGCIsOldGen(GarbageCollectorMXBean gc) {
        switch(gc.getName()) {
            case "Copy":
            case "PS Scavenge":
            case "G1 Young Generation":
            case "ParNew":
                return false;
            case "MarkSweepCompact":
            case "PS MarkSweep":
            case "ConcurrentMarkSweep":
            case "G1 Old Generation":
                return true;
            default:
                return false;
        }
    }

    public void handleNotification(final Notification notification, final Object handback) {
        String type = notification.getType();
        if (type.equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
            CompositeData cd = (CompositeData) notification.getUserData();
            GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from(cd);
            String gcName = info.getGcName();
            GcInfo gcInfo = info.getGcInfo();
            long duration = gcInfo.getDuration();
            GCState gcState = gcStates.get(gcName);
            if (gcState.assumeGCIsPartiallyConcurrent) {
                long previousTotal = gcState.lastGcTotalDuration;
                long total = gcState.gcBean.getCollectionTime();
                gcState.lastGcTotalDuration = total;
                duration = total - previousTotal;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(info.getGcName()).append(" GC in ").append(duration).append("ms.  ");
            long bytes = 0;
            Map<String, MemoryUsage> beforeMemoryUsage = gcInfo.getMemoryUsageBeforeGc();
            Map<String, MemoryUsage> afterMemoryUsage = gcInfo.getMemoryUsageAfterGc();
            for (String key : gcState.keys(info)) {
                MemoryUsage before = beforeMemoryUsage.get(key);
                MemoryUsage after = afterMemoryUsage.get(key);
                if (after != null && after.getUsed() != before.getUsed()) {
                    sb.append(key).append(": ").append(before.getUsed());
                    sb.append(" -> ");
                    sb.append(after.getUsed());
                    if (!key.equals(gcState.keys[gcState.keys.length - 1]))
                        sb.append("; ");
                    bytes += before.getUsed() - after.getUsed();
                }
            }
            while (true) {
                State prev = state.get();
                if (state.compareAndSet(prev, new State(duration, bytes, prev)))
                    break;
            }
            String st = sb.toString();
            if (GC_WARN_THRESHOLD_IN_MS != 0 && duration > GC_WARN_THRESHOLD_IN_MS)
                logger.warn(st);
            else if (duration > MIN_LOG_DURATION)
                logger.info(st);
            else if (logger.isDebugEnabled())
                logger.debug(st);
            if (duration > STAT_THRESHOLD)
                StatusLogger.log();
            if (gcState.assumeGCIsOldGen)
                LifecycleTransaction.rescheduleFailedDeletions();
        }
    }

    public State getTotalSinceLastCheck() {
        return state.getAndSet(new State());
    }

    public double[] getAndResetStats() {
        State state = getTotalSinceLastCheck();
        double[] r = new double[7];
        r[0] = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - state.startNanos);
        r[1] = state.maxRealTimeElapsed;
        r[2] = state.totalRealTimeElapsed;
        r[3] = state.sumSquaresRealTimeElapsed;
        r[4] = state.totalBytesReclaimed;
        r[5] = state.count;
        r[6] = getAllocatedDirectMemory();
        return r;
    }

    private static long getAllocatedDirectMemory() {
        if (BITS_TOTAL_CAPACITY == null)
            return -1;
        try {
            return BITS_TOTAL_CAPACITY.getLong(null);
        } catch (Throwable t) {
            logger.trace("Error accessing field of java.nio.Bits", t);
            return -1;
        }
    }
}