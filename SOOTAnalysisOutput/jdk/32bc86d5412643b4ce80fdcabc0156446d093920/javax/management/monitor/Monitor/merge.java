package javax.management.monitor;

import static com.sun.jmx.defaults.JmxProperties.MONITOR_LOGGER;
import com.sun.jmx.mbeanserver.GetPropertyAction;
import com.sun.jmx.mbeanserver.Introspector;
import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import static javax.management.monitor.MonitorNotification.*;

public abstract class Monitor extends NotificationBroadcasterSupport implements MonitorMBean, MBeanRegistration {

    static class ObservedObject {

        public ObservedObject(ObjectName observedObject) {
            this.observedObject = observedObject;
        }

        public final ObjectName getObservedObject() {
            return observedObject;
        }

        public final synchronized int getAlreadyNotified() {
            return alreadyNotified;
        }

        public final synchronized void setAlreadyNotified(int alreadyNotified) {
            this.alreadyNotified = alreadyNotified;
        }

        public final synchronized Object getDerivedGauge() {
            return derivedGauge;
        }

        public final synchronized void setDerivedGauge(Object derivedGauge) {
            this.derivedGauge = derivedGauge;
        }

        public final synchronized long getDerivedGaugeTimeStamp() {
            return derivedGaugeTimeStamp;
        }

        public final synchronized void setDerivedGaugeTimeStamp(long derivedGaugeTimeStamp) {
            this.derivedGaugeTimeStamp = derivedGaugeTimeStamp;
        }

        private final ObjectName observedObject;

        private int alreadyNotified;

        private Object derivedGauge;

        private long derivedGaugeTimeStamp;
    }

    private String observedAttribute;

    private long granularityPeriod = 10000;

    private boolean isActive = false;

    private final AtomicLong sequenceNumber = new AtomicLong();

    private boolean isComplexTypeAttribute = false;

    private String firstAttribute;

    private final List<String> remainingAttributes = new CopyOnWriteArrayList<String>();

    private static final AccessControlContext noPermissionsACC = new AccessControlContext(new ProtectionDomain[] { new ProtectionDomain(null, null) });

    private volatile AccessControlContext acc = noPermissionsACC;

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("Scheduler"));

    private static final Map<ThreadPoolExecutor, Void> executors = new WeakHashMap<ThreadPoolExecutor, Void>();

    private static final Object executorsLock = new Object();

    private static final int maximumPoolSize;

    static {
        final String maximumPoolSizeSysProp = "jmx.x.monitor.maximum.pool.size";
        final String maximumPoolSizeStr = AccessController.doPrivileged(new GetPropertyAction(maximumPoolSizeSysProp));
        if (maximumPoolSizeStr == null || maximumPoolSizeStr.trim().length() == 0) {
            maximumPoolSize = 10;
        } else {
            int maximumPoolSizeTmp = 10;
            try {
                maximumPoolSizeTmp = Integer.parseInt(maximumPoolSizeStr);
            } catch (NumberFormatException e) {
                if (MONITOR_LOGGER.isLoggable(Level.FINER)) {
                    MONITOR_LOGGER.logp(Level.FINER, Monitor.class.getName(), "<static initializer>", "Wrong value for " + maximumPoolSizeSysProp + " system property", e);
                    MONITOR_LOGGER.logp(Level.FINER, Monitor.class.getName(), "<static initializer>", maximumPoolSizeSysProp + " defaults to 10");
                }
                maximumPoolSizeTmp = 10;
            }
            if (maximumPoolSizeTmp < 1) {
                maximumPoolSize = 1;
            } else {
                maximumPoolSize = maximumPoolSizeTmp;
            }
        }
    }

    private Future<?> monitorFuture;

    private final SchedulerTask schedulerTask = new SchedulerTask();

    private ScheduledFuture<?> schedulerFuture;

    protected final static int capacityIncrement = 16;

    protected int elementCount = 0;

    @Deprecated
    protected int alreadyNotified = 0;

    protected int[] alreadyNotifieds = new int[capacityIncrement];

    protected MBeanServer server;

    protected static final int RESET_FLAGS_ALREADY_NOTIFIED = 0;

    protected static final int OBSERVED_OBJECT_ERROR_NOTIFIED = 1;

    protected static final int OBSERVED_ATTRIBUTE_ERROR_NOTIFIED = 2;

    protected static final int OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED = 4;

    protected static final int RUNTIME_ERROR_NOTIFIED = 8;

    @Deprecated
    protected String dbgTag = Monitor.class.getName();

    final List<ObservedObject> observedObjects = new CopyOnWriteArrayList<ObservedObject>();

    static final int THRESHOLD_ERROR_NOTIFIED = 16;

    enum NumericalType {

        BYTE,
        SHORT,
        INTEGER,
        LONG,
        FLOAT,
        DOUBLE
    }

    static final Integer INTEGER_ZERO = 0;

    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        MONITOR_LOGGER.logp(Level.FINER, Monitor.class.getName(), "preRegister(MBeanServer, ObjectName)", "initialize the reference on the MBean server");
        this.server = server;
        return name;
    }

    public void postRegister(Boolean registrationDone) {
    }

    public void preDeregister() throws Exception {
        MONITOR_LOGGER.logp(Level.FINER, Monitor.class.getName(), "preDeregister()", "stop the monitor");
        stop();
    }

    public void postDeregister() {
    }

    public abstract void start();

    public abstract void stop();

    @Deprecated
    public synchronized ObjectName getObservedObject() {
        if (observedObjects.isEmpty()) {
            return null;
        } else {
            return observedObjects.get(0).getObservedObject();
        }
    }

    @Deprecated
    public synchronized void setObservedObject(ObjectName object) throws IllegalArgumentException {
        if (object == null)
            throw new IllegalArgumentException("Null observed object");
        if (observedObjects.size() == 1 && containsObservedObject(object))
            return;
        observedObjects.clear();
        addObservedObject(object);
    }

    public synchronized void addObservedObject(ObjectName object) throws IllegalArgumentException {
        if (object == null) {
            throw new IllegalArgumentException("Null observed object");
        }
        if (containsObservedObject(object))
            return;
        ObservedObject o = createObservedObject(object);
        o.setAlreadyNotified(RESET_FLAGS_ALREADY_NOTIFIED);
        o.setDerivedGauge(INTEGER_ZERO);
        o.setDerivedGaugeTimeStamp(System.currentTimeMillis());
        observedObjects.add(o);
        createAlreadyNotified();
    }

    public synchronized void removeObservedObject(ObjectName object) {
        if (object == null)
            return;
        final ObservedObject o = getObservedObject(object);
        if (o != null) {
            observedObjects.remove(o);
            createAlreadyNotified();
        }
    }

    public synchronized boolean containsObservedObject(ObjectName object) {
        return getObservedObject(object) != null;
    }

    public synchronized ObjectName[] getObservedObjects() {
        ObjectName[] names = new ObjectName[observedObjects.size()];
        for (int i = 0; i < names.length; i++) names[i] = observedObjects.get(i).getObservedObject();
        return names;
    }

    public synchronized String getObservedAttribute() {
        return observedAttribute;
    }

    public void setObservedAttribute(String attribute) throws IllegalArgumentException {
        if (attribute == null) {
            throw new IllegalArgumentException("Null observed attribute");
        }
        synchronized (this) {
            if (observedAttribute != null && observedAttribute.equals(attribute))
                return;
            observedAttribute = attribute;
            cleanupIsComplexTypeAttribute();
            int index = 0;
            for (ObservedObject o : observedObjects) {
                resetAlreadyNotified(o, index++, OBSERVED_ATTRIBUTE_ERROR_NOTIFIED | OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED);
            }
        }
    }

    public synchronized long getGranularityPeriod() {
        return granularityPeriod;
    }

    public synchronized void setGranularityPeriod(long period) throws IllegalArgumentException {
        if (period <= 0) {
            throw new IllegalArgumentException("Nonpositive granularity " + "period");
        }
        if (granularityPeriod == period)
            return;
        granularityPeriod = period;
        if (isActive()) {
            cleanupFutures();
            schedulerFuture = scheduler.schedule(schedulerTask, period, TimeUnit.MILLISECONDS);
        }
    }

    public synchronized boolean isActive() {
        return isActive;
    }

    void doStart() {
        MONITOR_LOGGER.logp(Level.FINER, Monitor.class.getName(), "doStart()", "start the monitor");
        synchronized (this) {
            if (isActive()) {
                MONITOR_LOGGER.logp(Level.FINER, Monitor.class.getName(), "doStart()", "the monitor is already active");
                return;
            }
            isActive = true;
            cleanupIsComplexTypeAttribute();
            acc = AccessController.getContext();
            cleanupFutures();
            schedulerTask.setMonitorTask(new MonitorTask());
            schedulerFuture = scheduler.schedule(schedulerTask, getGranularityPeriod(), TimeUnit.MILLISECONDS);
        }
    }

    void doStop() {
        MONITOR_LOGGER.logp(Level.FINER, Monitor.class.getName(), "doStop()", "stop the monitor");
        synchronized (this) {
            if (!isActive()) {
                MONITOR_LOGGER.logp(Level.FINER, Monitor.class.getName(), "doStop()", "the monitor is not active");
                return;
            }
            isActive = false;
            cleanupFutures();
            acc = noPermissionsACC;
            cleanupIsComplexTypeAttribute();
        }
    }

    synchronized Object getDerivedGauge(ObjectName object) {
        final ObservedObject o = getObservedObject(object);
        return o == null ? null : o.getDerivedGauge();
    }

    synchronized long getDerivedGaugeTimeStamp(ObjectName object) {
        final ObservedObject o = getObservedObject(object);
        return o == null ? 0 : o.getDerivedGaugeTimeStamp();
    }

    Object getAttribute(MBeanServerConnection mbsc, ObjectName object, String attribute) throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        final boolean lookupMBeanInfo;
        synchronized (this) {
            if (!isActive())
                throw new IllegalArgumentException("The monitor has been stopped");
            if (!attribute.equals(getObservedAttribute()))
                throw new IllegalArgumentException("The observed attribute has been changed");
            lookupMBeanInfo = (firstAttribute == null && attribute.indexOf('.') != -1);
        }
        final MBeanInfo mbi;
        if (lookupMBeanInfo) {
            try {
                mbi = mbsc.getMBeanInfo(object);
            } catch (IntrospectionException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            mbi = null;
        }
        final String fa;
        synchronized (this) {
            if (!isActive())
                throw new IllegalArgumentException("The monitor has been stopped");
            if (!attribute.equals(getObservedAttribute()))
                throw new IllegalArgumentException("The observed attribute has been changed");
            if (firstAttribute == null) {
                if (attribute.indexOf('.') != -1) {
                    MBeanAttributeInfo[] mbaiArray = mbi.getAttributes();
                    for (MBeanAttributeInfo mbai : mbaiArray) {
                        if (attribute.equals(mbai.getName())) {
                            firstAttribute = attribute;
                            break;
                        }
                    }
                    if (firstAttribute == null) {
                        String[] tokens = attribute.split("\\.", -1);
                        firstAttribute = tokens[0];
                        for (int i = 1; i < tokens.length; i++) remainingAttributes.add(tokens[i]);
                        isComplexTypeAttribute = true;
                    }
                } else {
                    firstAttribute = attribute;
                }
            }
            fa = firstAttribute;
        }
        return mbsc.getAttribute(object, fa);
    }

    Comparable<?> getComparableFromAttribute(ObjectName object, String attribute, Object value) throws AttributeNotFoundException {
        if (isComplexTypeAttribute) {
            Object v = value;
            for (String attr : remainingAttributes) v = Introspector.elementFromComplex(v, attr);
            return (Comparable<?>) v;
        } else {
            return (Comparable<?>) value;
        }
    }

    boolean isComparableTypeValid(ObjectName object, String attribute, Comparable<?> value) {
        return true;
    }

    String buildErrorNotification(ObjectName object, String attribute, Comparable<?> value) {
        return null;
    }

    void onErrorNotification(MonitorNotification notification) {
    }

    Comparable<?> getDerivedGaugeFromComparable(ObjectName object, String attribute, Comparable<?> value) {
        return (Comparable<?>) value;
    }

    MonitorNotification buildAlarmNotification(ObjectName object, String attribute, Comparable<?> value) {
        return null;
    }

    boolean isThresholdTypeValid(ObjectName object, String attribute, Comparable<?> value) {
        return true;
    }

    static Class<? extends Number> classForType(NumericalType type) {
        switch(type) {
            case BYTE:
                return Byte.class;
            case SHORT:
                return Short.class;
            case INTEGER:
                return Integer.class;
            case LONG:
                return Long.class;
            case FLOAT:
                return Float.class;
            case DOUBLE:
                return Double.class;
            default:
                throw new IllegalArgumentException("Unsupported numerical type");
        }
    }

    static boolean isValidForType(Object value, Class<? extends Number> c) {
        return ((value == INTEGER_ZERO) || c.isInstance(value));
    }

    synchronized ObservedObject getObservedObject(ObjectName object) {
        for (ObservedObject o : observedObjects) if (o.getObservedObject().equals(object))
            return o;
        return null;
    }

    ObservedObject createObservedObject(ObjectName object) {
        return new ObservedObject(object);
    }

    synchronized void createAlreadyNotified() {
        elementCount = observedObjects.size();
        alreadyNotifieds = new int[elementCount];
        for (int i = 0; i < elementCount; i++) {
            alreadyNotifieds[i] = observedObjects.get(i).getAlreadyNotified();
        }
        updateDeprecatedAlreadyNotified();
    }

    synchronized void updateDeprecatedAlreadyNotified() {
        if (elementCount > 0)
            alreadyNotified = alreadyNotifieds[0];
        else
            alreadyNotified = 0;
    }

    synchronized void updateAlreadyNotified(ObservedObject o, int index) {
        alreadyNotifieds[index] = o.getAlreadyNotified();
        if (index == 0)
            updateDeprecatedAlreadyNotified();
    }

    synchronized boolean isAlreadyNotified(ObservedObject o, int mask) {
        return ((o.getAlreadyNotified() & mask) != 0);
    }

    synchronized void setAlreadyNotified(ObservedObject o, int index, int mask, int[] an) {
        final int i = computeAlreadyNotifiedIndex(o, index, an);
        if (i == -1)
            return;
        o.setAlreadyNotified(o.getAlreadyNotified() | mask);
        updateAlreadyNotified(o, i);
    }

    synchronized void resetAlreadyNotified(ObservedObject o, int index, int mask) {
        o.setAlreadyNotified(o.getAlreadyNotified() & ~mask);
        updateAlreadyNotified(o, index);
    }

    synchronized void resetAllAlreadyNotified(ObservedObject o, int index, int[] an) {
        final int i = computeAlreadyNotifiedIndex(o, index, an);
        if (i == -1)
            return;
        o.setAlreadyNotified(RESET_FLAGS_ALREADY_NOTIFIED);
        updateAlreadyNotified(o, index);
    }

    synchronized int computeAlreadyNotifiedIndex(ObservedObject o, int index, int[] an) {
        if (an == alreadyNotifieds) {
            return index;
        } else {
            return observedObjects.indexOf(o);
        }
    }

    private void sendNotification(String type, long timeStamp, String msg, Object derGauge, Object trigger, ObjectName object, boolean onError) {
        if (!isActive())
            return;
        if (MONITOR_LOGGER.isLoggable(Level.FINER)) {
            MONITOR_LOGGER.logp(Level.FINER, Monitor.class.getName(), "sendNotification", "send notification: " + "\n\tNotification observed object = " + object + "\n\tNotification observed attribute = " + observedAttribute + "\n\tNotification derived gauge = " + derGauge);
        }
        long seqno = sequenceNumber.getAndIncrement();
        MonitorNotification mn = new MonitorNotification(type, this, seqno, timeStamp, msg, object, observedAttribute, derGauge, trigger);
        if (onError)
            onErrorNotification(mn);
        sendNotification(mn);
    }

    private void monitor(ObservedObject o, int index, int[] an) {
        String attribute;
        String notifType = null;
        String msg = null;
        Object derGauge = null;
        Object trigger = null;
        ObjectName object;
        Comparable<?> value = null;
        MonitorNotification alarm = null;
        if (!isActive())
            return;
        synchronized (this) {
            object = o.getObservedObject();
            attribute = getObservedAttribute();
            if (object == null || attribute == null) {
                return;
            }
        }
        Object attributeValue = null;
        try {
            attributeValue = getAttribute(server, object, attribute);
            if (attributeValue == null)
                if (isAlreadyNotified(o, OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED))
                    return;
                else {
                    notifType = OBSERVED_ATTRIBUTE_TYPE_ERROR;
                    setAlreadyNotified(o, index, OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED, an);
                    msg = "The observed attribute value is null.";
                    MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", msg);
                }
        } catch (NullPointerException np_ex) {
            if (isAlreadyNotified(o, RUNTIME_ERROR_NOTIFIED))
                return;
            else {
                notifType = RUNTIME_ERROR;
                setAlreadyNotified(o, index, RUNTIME_ERROR_NOTIFIED, an);
                msg = "The monitor must be registered in the MBean " + "server or an MBeanServerConnection must be " + "explicitly supplied.";
                MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", msg);
                MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", np_ex.toString());
            }
        } catch (InstanceNotFoundException inf_ex) {
            if (isAlreadyNotified(o, OBSERVED_OBJECT_ERROR_NOTIFIED))
                return;
            else {
                notifType = OBSERVED_OBJECT_ERROR;
                setAlreadyNotified(o, index, OBSERVED_OBJECT_ERROR_NOTIFIED, an);
                msg = "The observed object must be accessible in " + "the MBeanServerConnection.";
                MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", msg);
                MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", inf_ex.toString());
            }
        } catch (AttributeNotFoundException anf_ex) {
            if (isAlreadyNotified(o, OBSERVED_ATTRIBUTE_ERROR_NOTIFIED))
                return;
            else {
                notifType = OBSERVED_ATTRIBUTE_ERROR;
                setAlreadyNotified(o, index, OBSERVED_ATTRIBUTE_ERROR_NOTIFIED, an);
                msg = "The observed attribute must be accessible in " + "the observed object.";
                MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", msg);
                MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", anf_ex.toString());
            }
        } catch (MBeanException mb_ex) {
            if (isAlreadyNotified(o, RUNTIME_ERROR_NOTIFIED))
                return;
            else {
                notifType = RUNTIME_ERROR;
                setAlreadyNotified(o, index, RUNTIME_ERROR_NOTIFIED, an);
                msg = mb_ex.getMessage() == null ? "" : mb_ex.getMessage();
                MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", msg);
                MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", mb_ex.toString());
            }
        } catch (ReflectionException ref_ex) {
            if (isAlreadyNotified(o, RUNTIME_ERROR_NOTIFIED)) {
                return;
            } else {
                notifType = RUNTIME_ERROR;
                setAlreadyNotified(o, index, RUNTIME_ERROR_NOTIFIED, an);
                msg = ref_ex.getMessage() == null ? "" : ref_ex.getMessage();
                MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", msg);
                MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", ref_ex.toString());
            }
        } catch (IOException io_ex) {
            if (isAlreadyNotified(o, RUNTIME_ERROR_NOTIFIED))
                return;
            else {
                notifType = RUNTIME_ERROR;
                setAlreadyNotified(o, index, RUNTIME_ERROR_NOTIFIED, an);
                msg = io_ex.getMessage() == null ? "" : io_ex.getMessage();
                MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", msg);
                MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", io_ex.toString());
            }
        } catch (RuntimeException rt_ex) {
            if (isAlreadyNotified(o, RUNTIME_ERROR_NOTIFIED))
                return;
            else {
                notifType = RUNTIME_ERROR;
                setAlreadyNotified(o, index, RUNTIME_ERROR_NOTIFIED, an);
                msg = rt_ex.getMessage() == null ? "" : rt_ex.getMessage();
                MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", msg);
                MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", rt_ex.toString());
            }
        }
        synchronized (this) {
            if (!isActive())
                return;
            if (!attribute.equals(getObservedAttribute()))
                return;
            if (msg == null) {
                try {
                    value = getComparableFromAttribute(object, attribute, attributeValue);
                } catch (ClassCastException e) {
                    if (isAlreadyNotified(o, OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED))
                        return;
                    else {
                        notifType = OBSERVED_ATTRIBUTE_TYPE_ERROR;
                        setAlreadyNotified(o, index, OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED, an);
                        msg = "The observed attribute value does not " + "implement the Comparable interface.";
                        MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", msg);
                        MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", e.toString());
                    }
                } catch (AttributeNotFoundException e) {
                    if (isAlreadyNotified(o, OBSERVED_ATTRIBUTE_ERROR_NOTIFIED))
                        return;
                    else {
                        notifType = OBSERVED_ATTRIBUTE_ERROR;
                        setAlreadyNotified(o, index, OBSERVED_ATTRIBUTE_ERROR_NOTIFIED, an);
                        msg = "The observed attribute must be accessible in " + "the observed object.";
                        MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", msg);
                        MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", e.toString());
                    }
                } catch (RuntimeException e) {
                    if (isAlreadyNotified(o, RUNTIME_ERROR_NOTIFIED))
                        return;
                    else {
                        notifType = RUNTIME_ERROR;
                        setAlreadyNotified(o, index, RUNTIME_ERROR_NOTIFIED, an);
                        msg = e.getMessage() == null ? "" : e.getMessage();
                        MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", msg);
                        MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", e.toString());
                    }
                }
            }
            if (msg == null) {
                if (!isComparableTypeValid(object, attribute, value)) {
                    if (isAlreadyNotified(o, OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED))
                        return;
                    else {
                        notifType = OBSERVED_ATTRIBUTE_TYPE_ERROR;
                        setAlreadyNotified(o, index, OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED, an);
                        msg = "The observed attribute type is not valid.";
                        MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", msg);
                    }
                }
            }
            if (msg == null) {
                if (!isThresholdTypeValid(object, attribute, value)) {
                    if (isAlreadyNotified(o, THRESHOLD_ERROR_NOTIFIED))
                        return;
                    else {
                        notifType = THRESHOLD_ERROR;
                        setAlreadyNotified(o, index, THRESHOLD_ERROR_NOTIFIED, an);
                        msg = "The threshold type is not valid.";
                        MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", msg);
                    }
                }
            }
            if (msg == null) {
                msg = buildErrorNotification(object, attribute, value);
                if (msg != null) {
                    if (isAlreadyNotified(o, RUNTIME_ERROR_NOTIFIED))
                        return;
                    else {
                        notifType = RUNTIME_ERROR;
                        setAlreadyNotified(o, index, RUNTIME_ERROR_NOTIFIED, an);
                        MONITOR_LOGGER.logp(Level.FINEST, Monitor.class.getName(), "monitor", msg);
                    }
                }
            }
            if (msg == null) {
                resetAllAlreadyNotified(o, index, an);
                derGauge = getDerivedGaugeFromComparable(object, attribute, value);
                o.setDerivedGauge(derGauge);
                o.setDerivedGaugeTimeStamp(System.currentTimeMillis());
                alarm = buildAlarmNotification(object, attribute, (Comparable<?>) derGauge);
            }
        }
        if (msg != null)
            sendNotification(notifType, System.currentTimeMillis(), msg, derGauge, trigger, object, true);
        if (alarm != null && alarm.getType() != null)
            sendNotification(alarm.getType(), System.currentTimeMillis(), alarm.getMessage(), derGauge, alarm.getTrigger(), object, false);
    }

    private synchronized void cleanupFutures() {
        if (schedulerFuture != null) {
            schedulerFuture.cancel(false);
            schedulerFuture = null;
        }
        if (monitorFuture != null) {
            monitorFuture.cancel(false);
            monitorFuture = null;
        }
    }

    private synchronized void cleanupIsComplexTypeAttribute() {
        firstAttribute = null;
        remainingAttributes.clear();
        isComplexTypeAttribute = false;
    }

    private class SchedulerTask implements Runnable {

        private MonitorTask task;

        public SchedulerTask() {
        }

        public void setMonitorTask(MonitorTask task) {
            this.task = task;
        }

        public void run() {
            synchronized (Monitor.this) {
                Monitor.this.monitorFuture = task.submit();
            }
        }
    }

    private class MonitorTask implements Runnable {

        private ThreadPoolExecutor executor;

        public MonitorTask() {
            SecurityManager s = System.getSecurityManager();
            ThreadGroup group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            synchronized (executorsLock) {
                for (ThreadPoolExecutor e : executors.keySet()) {
                    DaemonThreadFactory tf = (DaemonThreadFactory) e.getThreadFactory();
                    ThreadGroup tg = tf.getThreadGroup();
                    if (tg == group) {
                        executor = e;
                        break;
                    }
                }
                if (executor == null) {
                    executor = new ThreadPoolExecutor(maximumPoolSize, maximumPoolSize, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new DaemonThreadFactory("ThreadGroup<" + group.getName() + "> Executor", group));
                    executor.allowCoreThreadTimeOut(true);
                    executors.put(executor, null);
                }
            }
        }

        public Future<?> submit() {
            return executor.submit(this);
        }

        public void run() {
            final ScheduledFuture<?> sf;
            final AccessControlContext ac;
            synchronized (Monitor.this) {
                sf = Monitor.this.schedulerFuture;
                ac = Monitor.this.acc;
            }
            PrivilegedAction<Void> action = new PrivilegedAction<Void>() {

                public Void run() {
                    if (Monitor.this.isActive()) {
                        final int[] an = alreadyNotifieds;
                        int index = 0;
                        for (ObservedObject o : Monitor.this.observedObjects) {
                            if (Monitor.this.isActive()) {
                                Monitor.this.monitor(o, index++, an);
                            }
                        }
                    }
                    return null;
                }
            };
            if (ac == null) {
                throw new SecurityException("AccessControlContext cannot be null");
            }
            AccessController.doPrivileged(action, ac);
            synchronized (Monitor.this) {
                if (Monitor.this.isActive() && Monitor.this.schedulerFuture == sf) {
                    Monitor.this.monitorFuture = null;
                    Monitor.this.schedulerFuture = scheduler.schedule(Monitor.this.schedulerTask, Monitor.this.getGranularityPeriod(), TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    private static class DaemonThreadFactory implements ThreadFactory {

        final ThreadGroup group;

        final AtomicInteger threadNumber = new AtomicInteger(1);

        final String namePrefix;

        static final String nameSuffix = "]";

        public DaemonThreadFactory(String poolName) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "JMX Monitor " + poolName + " Pool [Thread-";
        }

        public DaemonThreadFactory(String poolName, ThreadGroup threadGroup) {
            group = threadGroup;
            namePrefix = "JMX Monitor " + poolName + " Pool [Thread-";
        }

        public ThreadGroup getThreadGroup() {
            return group;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement() + nameSuffix, 0);
            t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
