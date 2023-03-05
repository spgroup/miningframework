package com.sun.jmx.examples.scandir;

import static com.sun.jmx.examples.scandir.ScanManagerMXBean.ScanState.*;
import com.sun.jmx.examples.scandir.ScanManagerMXBean.ScanState;
import com.sun.jmx.examples.scandir.config.DirectoryScannerConfig;
import com.sun.jmx.examples.scandir.config.ScanManagerConfig;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.AttributeChangeNotification;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.JMX;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

public class ScanManager implements ScanManagerMXBean, NotificationEmitter, MBeanRegistration {

    private static final Logger LOG = Logger.getLogger(ScanManager.class.getName());

    public final static ObjectName SCAN_MANAGER_NAME = makeSingletonName(ScanManagerMXBean.class);

    private static long seqNumber = 0;

    private final NotificationBroadcasterSupport broadcaster;

    private volatile MBeanServer mbeanServer;

    private final BlockingQueue<Notification> pendingNotifs;

    private volatile ScanState state = STOPPED;

    private final Map<ObjectName, DirectoryScannerMXBean> scanmap;

    private final Map<ObjectName, ScanDirConfigMXBean> configmap;

    private final ResultLogManager log;

    private final Semaphore sequencer = new Semaphore(1);

    private volatile ScanDirConfigMXBean config = null;

    private static <K, V> Map<K, V> newConcurrentHashMap() {
        return new ConcurrentHashMap<K, V>();
    }

    private static <K, V> Map<K, V> newHashMap() {
        return new HashMap<K, V>();
    }

    public final static ObjectName makeSingletonName(Class clazz) {
        try {
            final Package p = clazz.getPackage();
            final String packageName = (p == null) ? null : p.getName();
            final String className = clazz.getSimpleName();
            final String domain;
            if (packageName == null || packageName.length() == 0) {
                domain = ScanDirAgent.class.getSimpleName();
            } else {
                domain = packageName;
            }
            final ObjectName name = new ObjectName(domain, "type", className);
            return name;
        } catch (Exception x) {
            final IllegalArgumentException iae = new IllegalArgumentException(String.valueOf(clazz), x);
            throw iae;
        }
    }

    public static final ObjectName makeMBeanName(Class clazz, String name) {
        try {
            return ObjectName.getInstance(makeSingletonName(clazz).toString() + ",name=" + name);
        } catch (MalformedObjectNameException x) {
            final IllegalArgumentException iae = new IllegalArgumentException(String.valueOf(name), x);
            throw iae;
        }
    }

    public static final ObjectName makeDirectoryScannerName(String name) {
        return makeMBeanName(DirectoryScannerMXBean.class, name);
    }

    public static final ObjectName makeScanDirConfigName(String name) {
        return makeMBeanName(ScanDirConfigMXBean.class, name);
    }

    public static ScanManagerMXBean register(MBeanServerConnection mbs) throws IOException, JMException {
        final ObjectInstance moi = mbs.createMBean(ScanManager.class.getName(), SCAN_MANAGER_NAME);
        final ScanManagerMXBean proxy = JMX.newMXBeanProxy(mbs, moi.getObjectName(), ScanManagerMXBean.class, true);
        return proxy;
    }

    public static ScanManagerMXBean newSingletonProxy(MBeanServerConnection mbs) {
        final ScanManagerMXBean proxy = JMX.newMXBeanProxy(mbs, SCAN_MANAGER_NAME, ScanManagerMXBean.class, true);
        return proxy;
    }

    public static ScanManagerMXBean newSingletonProxy() {
        return newSingletonProxy(ManagementFactory.getPlatformMBeanServer());
    }

    public static ScanManagerMXBean register() throws IOException, JMException {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        return register(mbs);
    }

    public ScanManager() {
        broadcaster = new NotificationBroadcasterSupport();
        pendingNotifs = new LinkedBlockingQueue<Notification>(100);
        scanmap = newConcurrentHashMap();
        configmap = newConcurrentHashMap();
        log = new ResultLogManager();
    }

    DirectoryScannerMXBean createDirectoryScanner(DirectoryScannerConfig config) {
        return new DirectoryScanner(config, log);
    }

    private void applyConfiguration(ScanManagerConfig bean) throws IOException, JMException {
        if (bean == null)
            return;
        if (!sequencer.tryAcquire()) {
            throw new IllegalStateException("Can't acquire lock");
        }
        try {
            unregisterScanners();
            final DirectoryScannerConfig[] scans = bean.getScanList();
            if (scans == null)
                return;
            for (DirectoryScannerConfig scan : scans) {
                addDirectoryScanner(scan);
            }
            log.setConfig(bean.getInitialResultLogConfig());
        } finally {
            sequencer.release();
        }
    }

    public void applyConfiguration(boolean fromMemory) throws IOException, JMException {
        if (fromMemory == false)
            config.load();
        applyConfiguration(config.getConfiguration());
    }

    public void applyCurrentResultLogConfig(boolean toMemory) throws IOException, JMException {
        final ScanManagerConfig bean = config.getConfiguration();
        bean.setInitialResultLogConfig(log.getConfig());
        config.setConfiguration(bean);
        if (toMemory == false)
            config.save();
    }

    public void setConfigurationMBean(ScanDirConfigMXBean config) {
        this.config = config;
    }

    public ScanDirConfigMXBean getConfigurationMBean() {
        return config;
    }

    private DirectoryScannerMXBean addDirectoryScanner(DirectoryScannerConfig bean) throws JMException {
        try {
            final DirectoryScannerMXBean scanner;
            final ObjectName scanName;
            synchronized (this) {
                if (state != STOPPED && state != COMPLETED)
                    throw new IllegalStateException(state.toString());
                scanner = createDirectoryScanner(bean);
                scanName = makeDirectoryScannerName(bean.getName());
            }
            LOG.fine("server: " + mbeanServer);
            LOG.fine("scanner: " + scanner);
            LOG.fine("scanName: " + scanName);
            final ObjectInstance moi = mbeanServer.registerMBean(scanner, scanName);
            final ObjectName moiName = moi.getObjectName();
            final DirectoryScannerMXBean proxy = JMX.newMXBeanProxy(mbeanServer, moiName, DirectoryScannerMXBean.class, true);
            scanmap.put(moiName, proxy);
            return proxy;
        } catch (RuntimeException x) {
            final String msg = "Operation failed: " + x;
            if (LOG.isLoggable(Level.FINEST))
                LOG.log(Level.FINEST, msg, x);
            else
                LOG.fine(msg);
            throw x;
        } catch (JMException x) {
            final String msg = "Operation failed: " + x;
            if (LOG.isLoggable(Level.FINEST))
                LOG.log(Level.FINEST, msg, x);
            else
                LOG.fine(msg);
            throw x;
        }
    }

    public ScanDirConfigMXBean createOtherConfigurationMBean(String name, String filename) throws JMException {
        final ScanDirConfig profile = new ScanDirConfig(filename);
        final ObjectName profName = makeScanDirConfigName(name);
        final ObjectInstance moi = mbeanServer.registerMBean(profile, profName);
        final ScanDirConfigMXBean proxy = JMX.newMXBeanProxy(mbeanServer, profName, ScanDirConfigMXBean.class, true);
        configmap.put(moi.getObjectName(), proxy);
        return proxy;
    }

    public Map<String, DirectoryScannerMXBean> getDirectoryScanners() {
        final Map<String, DirectoryScannerMXBean> proxyMap = newHashMap();
        for (Entry<ObjectName, DirectoryScannerMXBean> item : scanmap.entrySet()) {
            proxyMap.put(item.getKey().getKeyProperty("name"), item.getValue());
        }
        return proxyMap;
    }

    private final static Map<String, EnumSet<ScanState>> allowedStates;

    static {
        allowedStates = newHashMap();
        allowedStates.put("stop", EnumSet.allOf(ScanState.class));
        allowedStates.put("close", EnumSet.of(STOPPED, COMPLETED, CLOSED));
        allowedStates.put("schedule", EnumSet.of(STOPPED, COMPLETED));
        allowedStates.put("scan-running", EnumSet.of(SCHEDULED));
        allowedStates.put("scan-scheduled", EnumSet.of(RUNNING));
        allowedStates.put("scan-done", EnumSet.of(RUNNING));
    }

    public ScanState getState() {
        return state;
    }

    private void queueStateChangedNotification(long sequence, long time, ScanState old, ScanState current) {
        final AttributeChangeNotification n = new AttributeChangeNotification(SCAN_MANAGER_NAME, sequence, time, "ScanManager State changed to " + current, "State", ScanState.class.getName(), old.toString(), current.toString());
        try {
            if (!pendingNotifs.offer(n, 2, TimeUnit.SECONDS)) {
                LOG.fine("Can't queue Notification: " + n);
            }
        } catch (InterruptedException x) {
            LOG.fine("Can't queue Notification: " + x);
        }
    }

    private void sendQueuedNotifications() {
        Notification n;
        while ((n = pendingNotifs.poll()) != null) {
            broadcaster.sendNotification(n);
        }
    }

    private ScanState switchState(ScanState desired, String forOperation) {
        return switchState(desired, allowedStates.get(forOperation));
    }

    private ScanState switchState(ScanState desired, EnumSet<ScanState> allowed) {
        final ScanState old;
        final long timestamp;
        final long sequence;
        synchronized (this) {
            old = state;
            if (!allowed.contains(state))
                throw new IllegalStateException(state.toString());
            state = desired;
            timestamp = System.currentTimeMillis();
            sequence = getNextSeqNumber();
        }
        LOG.fine("switched state: " + old + " -> " + desired);
        if (old != desired)
            queueStateChangedNotification(sequence, timestamp, old, desired);
        return old;
    }

    private Timer timer = null;

    public void schedule(long delay, long interval) {
        if (!sequencer.tryAcquire()) {
            throw new IllegalStateException("Can't acquire lock");
        }
        try {
            LOG.fine("scheduling new task: state=" + state);
            final ScanState old = switchState(SCHEDULED, "schedule");
            final boolean scheduled = scheduleSession(new SessionTask(interval), delay);
            if (scheduled)
                LOG.fine("new task scheduled: state=" + state);
        } finally {
            sequencer.release();
        }
        sendQueuedNotifications();
    }

    private synchronized boolean scheduleSession(SessionTask task, long delay) {
        if (state == STOPPED)
            return false;
        if (timer == null)
            timer = new Timer("ScanManager");
        tasklist.add(task);
        timer.schedule(task, delay);
        return true;
    }

    public void start() throws IOException, InstanceNotFoundException {
        schedule(0, 0);
    }

    public void stop() {
        if (!sequencer.tryAcquire())
            throw new IllegalStateException("Can't acquire lock");
        int errcount = 0;
        final StringBuilder b = new StringBuilder();
        try {
            switchState(STOPPED, "stop");
            errcount += cancelSessionTasks(b);
            errcount += stopDirectoryScanners(b);
        } finally {
            sequencer.release();
        }
        sendQueuedNotifications();
        if (errcount > 0) {
            b.insert(0, "stop partially failed with " + errcount + " error(s):");
            throw new RuntimeException(b.toString());
        }
    }

    public void close() {
        switchState(CLOSED, "close");
        sendQueuedNotifications();
    }

    private void append(StringBuilder b, String prefix, Throwable t) {
        final String first = (prefix == null) ? "\n" : "\n" + prefix;
        b.append(first).append(String.valueOf(t));
        Throwable cause = t;
        while ((cause = cause.getCause()) != null) {
            b.append(first).append("Caused by:").append(first);
            b.append('\t').append(String.valueOf(cause));
        }
    }

    private int cancelSessionTasks(StringBuilder b) {
        int errcount = 0;
        for (SessionTask task : tasklist) {
            try {
                task.cancel();
                tasklist.remove(task);
            } catch (Exception ex) {
                errcount++;
                append(b, "\t", ex);
            }
        }
        return errcount;
    }

    private int stopDirectoryScanners(StringBuilder b) {
        int errcount = 0;
        for (DirectoryScannerMXBean s : scanmap.values()) {
            try {
                s.stop();
            } catch (Exception ex) {
                errcount++;
                append(b, "\t", ex);
            }
        }
        return errcount;
    }

    private void scanAllDirectories() throws IOException, InstanceNotFoundException {
        int errcount = 0;
        final StringBuilder b = new StringBuilder();
        for (ObjectName key : scanmap.keySet()) {
            final DirectoryScannerMXBean s = scanmap.get(key);
            try {
                if (state == STOPPED)
                    return;
                s.scan();
            } catch (Exception ex) {
                LOG.log(Level.FINE, key + " failed to scan: " + ex, ex);
                errcount++;
                append(b, "\t", ex);
            }
        }
        if (errcount > 0) {
            b.insert(0, "scan partially performed with " + errcount + " error(s):");
            throw new RuntimeException(b.toString());
        }
    }

    private final ConcurrentLinkedQueue<SessionTask> tasklist = new ConcurrentLinkedQueue<SessionTask>();

    private volatile static long taskcount = 0;

    private class SessionTask extends TimerTask {

        final long delayBeforeNext;

        final long taskid;

        volatile boolean cancelled = false;

        SessionTask(long scheduleNext) {
            delayBeforeNext = scheduleNext;
            taskid = taskcount++;
        }

        private boolean notifyStateChange(ScanState newState, String condition) {
            synchronized (ScanManager.this) {
                if (state == STOPPED || state == CLOSED)
                    return false;
                switchState(newState, condition);
            }
            sendQueuedNotifications();
            return true;
        }

        public boolean cancel() {
            cancelled = true;
            return super.cancel();
        }

        private boolean execute() {
            final String tag = "Scheduled session[" + taskid + "]";
            try {
                if (cancelled) {
                    LOG.finer(tag + " cancelled: done");
                    return false;
                }
                if (!notifyStateChange(RUNNING, "scan-running")) {
                    LOG.finer(tag + " stopped: done");
                    return false;
                }
                scanAllDirectories();
            } catch (Exception x) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(Level.FINEST, tag + " failed to scan: " + x, x);
                } else if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine(tag + " failed to scan: " + x);
                }
            }
            return true;
        }

        private boolean scheduleNext() {
            final String tag = "Scheduled session[" + taskid + "]";
            try {
                LOG.finer(tag + ": scheduling next session for " + delayBeforeNext + "ms");
                if (cancelled || !notifyStateChange(SCHEDULED, "scan-scheduled")) {
                    LOG.finer(tag + " stopped: do not reschedule");
                    return false;
                }
                final SessionTask nextTask = new SessionTask(delayBeforeNext);
                if (!scheduleSession(nextTask, delayBeforeNext))
                    return false;
                LOG.finer(tag + ": next session successfully scheduled");
            } catch (Exception x) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(Level.FINEST, tag + " failed to schedule next session: " + x, x);
                } else if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine(tag + " failed to schedule next session: " + x);
                }
            }
            return true;
        }

        public void run() {
            final String tag = "Scheduled session[" + taskid + "]";
            LOG.entering(SessionTask.class.getName(), "run");
            LOG.finer(tag + " starting...");
            try {
                if (execute() == false)
                    return;
                LOG.finer(tag + " terminating - state is " + state + ((delayBeforeNext > 0) ? (" next session is due in " + delayBeforeNext + " ms.") : " no additional session scheduled"));
                if (delayBeforeNext <= 0) {
                    if (!notifyStateChange(COMPLETED, "scan-done"))
                        LOG.finer(tag + " stopped: done");
                    else
                        LOG.finer(tag + " completed: done");
                    return;
                }
                scheduleNext();
            } finally {
                tasklist.remove(this);
                LOG.finer(tag + " finished...");
                LOG.exiting(SessionTask.class.getName(), "run");
            }
        }
    }

    public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException {
        broadcaster.addNotificationListener(listener, filter, handback);
    }

    public MBeanNotificationInfo[] getNotificationInfo() {
        return new MBeanNotificationInfo[] { new MBeanNotificationInfo(new String[] { AttributeChangeNotification.ATTRIBUTE_CHANGE }, AttributeChangeNotification.class.getName(), "Emitted when the State attribute changes") };
    }

    public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
        broadcaster.removeNotificationListener(listener);
    }

    public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException {
        broadcaster.removeNotificationListener(listener, filter, handback);
    }

    static synchronized long getNextSeqNumber() {
        return seqNumber++;
    }

    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        if (name != null) {
            if (!SCAN_MANAGER_NAME.equals(name))
                throw new IllegalArgumentException(String.valueOf(name));
        }
        mbeanServer = server;
        return SCAN_MANAGER_NAME;
    }

    static String getDefaultConfigurationFileName() {
        final String user = System.getProperty("user.home");
        final String defconf = user + File.separator + "jmx-scandir.xml";
        return defconf;
    }

    public void postRegister(Boolean registrationDone) {
        if (!registrationDone)
            return;
        Exception test = null;
        try {
            mbeanServer.registerMBean(log, ResultLogManager.RESULT_LOG_MANAGER_NAME);
            final String defconf = getDefaultConfigurationFileName();
            final String conf = System.getProperty("scandir.config.file", defconf);
            final String confname = ScanDirConfig.guessConfigName(conf, defconf);
            final ObjectName defaultProfileName = makeMBeanName(ScanDirConfigMXBean.class, confname);
            if (!mbeanServer.isRegistered(defaultProfileName))
                mbeanServer.registerMBean(new ScanDirConfig(conf), defaultProfileName);
            config = JMX.newMXBeanProxy(mbeanServer, defaultProfileName, ScanDirConfigMXBean.class, true);
            configmap.put(defaultProfileName, config);
        } catch (Exception x) {
            LOG.config("Failed to populate MBeanServer: " + x);
            close();
            return;
        }
        try {
            config.load();
        } catch (Exception x) {
            LOG.finest("No config to load: " + x);
            test = x;
        }
        if (test == null) {
            try {
                applyConfiguration(config.getConfiguration());
            } catch (Exception x) {
                if (LOG.isLoggable(Level.FINEST))
                    LOG.log(Level.FINEST, "Failed to apply config: " + x, x);
                LOG.config("Failed to apply config: " + x);
            }
        }
    }

    private void unregisterScanners() throws JMException {
        unregisterMBeans(scanmap);
    }

    private void unregisterConfigs() throws JMException {
        unregisterMBeans(configmap);
    }

    private void unregisterMBeans(Map<ObjectName, ?> map) throws JMException {
        for (ObjectName key : map.keySet()) {
            if (mbeanServer.isRegistered(key))
                mbeanServer.unregisterMBean(key);
            map.remove(key);
        }
    }

    private void unregisterResultLogManager() throws JMException {
        final ObjectName name = ResultLogManager.RESULT_LOG_MANAGER_NAME;
        if (mbeanServer.isRegistered(name)) {
            mbeanServer.unregisterMBean(name);
        }
    }

    public void preDeregister() throws Exception {
        try {
            close();
            if (!sequencer.tryAcquire())
                throw new IllegalStateException("can't acquire lock");
            try {
                unregisterScanners();
                unregisterConfigs();
                unregisterResultLogManager();
            } finally {
                sequencer.release();
            }
        } catch (Exception x) {
            LOG.log(Level.FINEST, "Failed to unregister: " + x, x);
            throw x;
        }
    }

    public synchronized void postDeregister() {
        if (timer != null) {
            try {
                timer.cancel();
            } catch (Exception x) {
                if (LOG.isLoggable(Level.FINEST))
                    LOG.log(Level.FINEST, "Failed to cancel timer", x);
                else if (LOG.isLoggable(Level.FINE))
                    LOG.fine("Failed to cancel timer: " + x);
            } finally {
                timer = null;
            }
        }
    }
}
