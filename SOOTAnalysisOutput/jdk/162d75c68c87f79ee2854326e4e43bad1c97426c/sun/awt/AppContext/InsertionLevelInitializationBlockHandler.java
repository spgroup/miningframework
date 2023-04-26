package sun.awt;

import java.awt.EventQueue;
import java.awt.Window;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.Toolkit;
import java.awt.GraphicsEnvironment;
import java.awt.event.InvocationEvent;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import sun.util.logging.PlatformLogger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class AppContext {

    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.AppContext");

    public static final Object EVENT_QUEUE_KEY = new StringBuffer("EventQueue");

    public final static Object EVENT_QUEUE_LOCK_KEY = new StringBuilder("EventQueue.Lock");

    public final static Object EVENT_QUEUE_COND_KEY = new StringBuilder("EventQueue.Condition");

    private static final Map<ThreadGroup, AppContext> threadGroup2appContext = Collections.synchronizedMap(new IdentityHashMap<ThreadGroup, AppContext>());

    public static Set<AppContext> getAppContexts() {
        synchronized (threadGroup2appContext) {
            return new HashSet<AppContext>(threadGroup2appContext.values());
        }
    }

    private static volatile AppContext mainAppContext = null;

    private final Map<Object, Object> table = new HashMap<>();

    private final ThreadGroup threadGroup;

    private PropertyChangeSupport changeSupport = null;

    public static final String DISPOSED_PROPERTY_NAME = "disposed";

    public static final String GUI_DISPOSED = "guidisposed";

    private volatile boolean isDisposed = false;

    public boolean isDisposed() {
        return isDisposed;
    }

    static {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

            public Void run() {
                ThreadGroup currentThreadGroup = Thread.currentThread().getThreadGroup();
                ThreadGroup parentThreadGroup = currentThreadGroup.getParent();
                while (parentThreadGroup != null) {
                    currentThreadGroup = parentThreadGroup;
                    parentThreadGroup = currentThreadGroup.getParent();
                }
                mainAppContext = new AppContext(currentThreadGroup);
                numAppContexts = 1;
                return null;
            }
        });
    }

    private static volatile int numAppContexts;

    private final ClassLoader contextClassLoader;

    AppContext(ThreadGroup threadGroup) {
        numAppContexts++;
        this.threadGroup = threadGroup;
        threadGroup2appContext.put(threadGroup, this);
        this.contextClassLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
        Lock eventQueuePushPopLock = new ReentrantLock();
        put(EVENT_QUEUE_LOCK_KEY, eventQueuePushPopLock);
        Condition eventQueuePushPopCond = eventQueuePushPopLock.newCondition();
        put(EVENT_QUEUE_COND_KEY, eventQueuePushPopCond);
    }

    private static final ThreadLocal<AppContext> threadAppContext = new ThreadLocal<AppContext>();

    public final static AppContext getAppContext() {
        if (numAppContexts == 1)
            return mainAppContext;
        AppContext appContext = threadAppContext.get();
        if (null == appContext) {
            appContext = AccessController.doPrivileged(new PrivilegedAction<AppContext>() {

                public AppContext run() {
                    ThreadGroup currentThreadGroup = Thread.currentThread().getThreadGroup();
                    ThreadGroup threadGroup = currentThreadGroup;
                    AppContext context = threadGroup2appContext.get(threadGroup);
                    while (context == null) {
                        threadGroup = threadGroup.getParent();
                        if (threadGroup == null) {
                            throw new RuntimeException("Invalid ThreadGroup");
                        }
                        context = threadGroup2appContext.get(threadGroup);
                    }
                    for (ThreadGroup tg = currentThreadGroup; tg != threadGroup; tg = tg.getParent()) {
                        threadGroup2appContext.put(tg, context);
                    }
                    threadAppContext.set(context);
                    return context;
                }
            });
        }
        if (appContext == mainAppContext) {
            SecurityManager securityManager = System.getSecurityManager();
            if ((securityManager != null) && (securityManager instanceof AWTSecurityManager)) {
                AWTSecurityManager awtSecMgr = (AWTSecurityManager) securityManager;
                AppContext secAppContext = awtSecMgr.getAppContext();
                if (secAppContext != null) {
                    appContext = secAppContext;
                }
            }
        }
        return appContext;
    }

    final static AppContext getMainAppContext() {
        return mainAppContext;
    }

    private long DISPOSAL_TIMEOUT = 5000;

    private long THREAD_INTERRUPT_TIMEOUT = 1000;

    public void dispose() throws IllegalThreadStateException {
        if (this.threadGroup.parentOf(Thread.currentThread().getThreadGroup())) {
            throw new IllegalThreadStateException("Current Thread is contained within AppContext to be disposed.");
        }
        synchronized (this) {
            if (this.isDisposed) {
                return;
            }
            this.isDisposed = true;
        }
        final PropertyChangeSupport changeSupport = this.changeSupport;
        if (changeSupport != null) {
            changeSupport.firePropertyChange(DISPOSED_PROPERTY_NAME, false, true);
        }
        final Object notificationLock = new Object();
        Runnable runnable = new Runnable() {

            public void run() {
                Window[] windowsToDispose = Window.getOwnerlessWindows();
                for (Window w : windowsToDispose) {
                    try {
                        w.dispose();
                    } catch (Throwable t) {
                        log.finer("exception occured while disposing app context", t);
                    }
                }
                AccessController.doPrivileged(new PrivilegedAction<Void>() {

                    public Void run() {
                        if (!GraphicsEnvironment.isHeadless() && SystemTray.isSupported()) {
                            SystemTray systemTray = SystemTray.getSystemTray();
                            TrayIcon[] trayIconsToDispose = systemTray.getTrayIcons();
                            for (TrayIcon ti : trayIconsToDispose) {
                                systemTray.remove(ti);
                            }
                        }
                        return null;
                    }
                });
                if (changeSupport != null) {
                    changeSupport.firePropertyChange(GUI_DISPOSED, false, true);
                }
                synchronized (notificationLock) {
                    notificationLock.notifyAll();
                }
            }
        };
        synchronized (notificationLock) {
            SunToolkit.postEvent(this, new InvocationEvent(Toolkit.getDefaultToolkit(), runnable));
            try {
                notificationLock.wait(DISPOSAL_TIMEOUT);
            } catch (InterruptedException e) {
            }
        }
        runnable = new Runnable() {

            public void run() {
                synchronized (notificationLock) {
                    notificationLock.notifyAll();
                }
            }
        };
        synchronized (notificationLock) {
            SunToolkit.postEvent(this, new InvocationEvent(Toolkit.getDefaultToolkit(), runnable));
            try {
                notificationLock.wait(DISPOSAL_TIMEOUT);
            } catch (InterruptedException e) {
            }
        }
        this.threadGroup.interrupt();
        long startTime = System.currentTimeMillis();
        long endTime = startTime + THREAD_INTERRUPT_TIMEOUT;
        while ((this.threadGroup.activeCount() > 0) && (System.currentTimeMillis() < endTime)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
        this.threadGroup.stop();
        startTime = System.currentTimeMillis();
        endTime = startTime + THREAD_INTERRUPT_TIMEOUT;
        while ((this.threadGroup.activeCount() > 0) && (System.currentTimeMillis() < endTime)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
        int numSubGroups = this.threadGroup.activeGroupCount();
        if (numSubGroups > 0) {
            ThreadGroup[] subGroups = new ThreadGroup[numSubGroups];
            numSubGroups = this.threadGroup.enumerate(subGroups);
            for (int subGroup = 0; subGroup < numSubGroups; subGroup++) {
                threadGroup2appContext.remove(subGroups[subGroup]);
            }
        }
        threadGroup2appContext.remove(this.threadGroup);
        threadAppContext.set(null);
        try {
            this.threadGroup.destroy();
        } catch (IllegalThreadStateException e) {
        }
        synchronized (table) {
            this.table.clear();
        }
        numAppContexts--;
        mostRecentKeyValue = null;
    }

    static final class PostShutdownEventRunnable implements Runnable {

        private final AppContext appContext;

        public PostShutdownEventRunnable(AppContext ac) {
            appContext = ac;
        }

        public void run() {
            final EventQueue eq = (EventQueue) appContext.get(EVENT_QUEUE_KEY);
            if (eq != null) {
                eq.postEvent(AWTAutoShutdown.getShutdownEvent());
            }
        }
    }

    static final class CreateThreadAction implements PrivilegedAction<Thread> {

        private final AppContext appContext;

        private final Runnable runnable;

        public CreateThreadAction(AppContext ac, Runnable r) {
            appContext = ac;
            runnable = r;
        }

        public Thread run() {
            Thread t = new Thread(appContext.getThreadGroup(), runnable);
            t.setContextClassLoader(appContext.getContextClassLoader());
            t.setPriority(Thread.NORM_PRIORITY + 1);
            t.setDaemon(true);
            return t;
        }
    }

    static void stopEventDispatchThreads() {
        for (AppContext appContext : getAppContexts()) {
            if (appContext.isDisposed()) {
                continue;
            }
            Runnable r = new PostShutdownEventRunnable(appContext);
            if (appContext != AppContext.getAppContext()) {
                PrivilegedAction<Thread> action = new CreateThreadAction(appContext, r);
                Thread thread = AccessController.doPrivileged(action);
                thread.start();
            } else {
                r.run();
            }
        }
    }

    private MostRecentKeyValue mostRecentKeyValue = null;

    private MostRecentKeyValue shadowMostRecentKeyValue = null;

    public Object get(Object key) {
        synchronized (table) {
            MostRecentKeyValue recent = mostRecentKeyValue;
            if ((recent != null) && (recent.key == key)) {
                return recent.value;
            }
            Object value = table.get(key);
            if (mostRecentKeyValue == null) {
                mostRecentKeyValue = new MostRecentKeyValue(key, value);
                shadowMostRecentKeyValue = new MostRecentKeyValue(key, value);
            } else {
                MostRecentKeyValue auxKeyValue = mostRecentKeyValue;
                shadowMostRecentKeyValue.setPair(key, value);
                mostRecentKeyValue = shadowMostRecentKeyValue;
                shadowMostRecentKeyValue = auxKeyValue;
            }
            return value;
        }
    }

    public Object put(Object key, Object value) {
        synchronized (table) {
            MostRecentKeyValue recent = mostRecentKeyValue;
            if ((recent != null) && (recent.key == key))
                recent.value = value;
            return table.put(key, value);
        }
    }

    public Object remove(Object key) {
        synchronized (table) {
            MostRecentKeyValue recent = mostRecentKeyValue;
            if ((recent != null) && (recent.key == key))
                recent.value = null;
            return table.remove(key);
        }
    }

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    public ClassLoader getContextClassLoader() {
        return contextClassLoader;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[threadGroup=" + threadGroup.getName() + "]";
    }

    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        if (changeSupport == null) {
            return new PropertyChangeListener[0];
        }
        return changeSupport.getPropertyChangeListeners();
    }

    public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (changeSupport == null) {
            changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (listener == null || changeSupport == null) {
            return;
        }
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    public synchronized PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        if (changeSupport == null) {
            return new PropertyChangeListener[0];
        }
        return changeSupport.getPropertyChangeListeners(propertyName);
    }

    static {
        sun.misc.SharedSecrets.setJavaAWTAccess(new sun.misc.JavaAWTAccess() {

            public Object get(Object key) {
                return getAppContext().get(key);
            }

            public void put(Object key, Object value) {
                getAppContext().put(key, value);
            }

            public void remove(Object key) {
                getAppContext().remove(key);
            }

            public boolean isDisposed() {
                return getAppContext().isDisposed();
            }

            public boolean isMainAppContext() {
                return (numAppContexts == 1);
            }
        });
    }
}

final class MostRecentKeyValue {

    Object key;

    Object value;

    MostRecentKeyValue(Object k, Object v) {
        key = k;
        value = v;
    }

    void setPair(Object k, Object v) {
        key = k;
        value = v;
    }
}