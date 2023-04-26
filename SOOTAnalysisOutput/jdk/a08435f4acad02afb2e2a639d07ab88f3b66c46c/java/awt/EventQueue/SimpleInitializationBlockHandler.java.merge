package java.awt;

import java.awt.event.*;
import java.awt.peer.ComponentPeer;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.EmptyStackException;
import sun.awt.*;
import sun.awt.dnd.SunDropTargetEvent;
import sun.util.logging.PlatformLogger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.atomic.AtomicInteger;
import java.security.AccessControlContext;
import sun.misc.SharedSecrets;
import sun.misc.JavaSecurityAccess;

public class EventQueue {

    private static final AtomicInteger threadInitNumber = new AtomicInteger(0);

    private static final int LOW_PRIORITY = 0;

    private static final int NORM_PRIORITY = 1;

    private static final int HIGH_PRIORITY = 2;

    private static final int ULTIMATE_PRIORITY = 3;

    private static final int NUM_PRIORITIES = ULTIMATE_PRIORITY + 1;

    private Queue[] queues = new Queue[NUM_PRIORITIES];

    private EventQueue nextQueue;

    private EventQueue previousQueue;

    private final Lock pushPopLock;

    private final Condition pushPopCond;

    private final static Runnable dummyRunnable = new Runnable() {

        public void run() {
        }
    };

    private EventDispatchThread dispatchThread;

    private final ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();

    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    private long mostRecentEventTime = System.currentTimeMillis();

    private long mostRecentKeyEventTime = System.currentTimeMillis();

    private WeakReference<AWTEvent> currentEvent;

    private volatile int waitForID;

    private final AppContext appContext;

    private final String name = "AWT-EventQueue-" + threadInitNumber.getAndIncrement();

    private FwDispatcher fwDispatcher;

    private static final PlatformLogger eventLog = PlatformLogger.getLogger("java.awt.event.EventQueue");

    static {
        AWTAccessor.setEventQueueAccessor(new AWTAccessor.EventQueueAccessor() {

            public Thread getDispatchThread(EventQueue eventQueue) {
                return eventQueue.getDispatchThread();
            }

            public boolean isDispatchThreadImpl(EventQueue eventQueue) {
                return eventQueue.isDispatchThreadImpl();
            }

            public void removeSourceEvents(EventQueue eventQueue, Object source, boolean removeAllEvents) {
                eventQueue.removeSourceEvents(source, removeAllEvents);
            }

            public boolean noEvents(EventQueue eventQueue) {
                return eventQueue.noEvents();
            }

            public void wakeup(EventQueue eventQueue, boolean isShutdown) {
                eventQueue.wakeup(isShutdown);
            }

            public void invokeAndWait(Object source, Runnable r) throws InterruptedException, InvocationTargetException {
                EventQueue.invokeAndWait(source, r);
            }

            public void setFwDispatcher(EventQueue eventQueue, FwDispatcher dispatcher) {
                eventQueue.setFwDispatcher(dispatcher);
            }
        });
    }

    public EventQueue() {
        for (int i = 0; i < NUM_PRIORITIES; i++) {
            queues[i] = new Queue();
        }
        appContext = AppContext.getAppContext();
        pushPopLock = (Lock) appContext.get(AppContext.EVENT_QUEUE_LOCK_KEY);
        pushPopCond = (Condition) appContext.get(AppContext.EVENT_QUEUE_COND_KEY);
    }

    public void postEvent(AWTEvent theEvent) {
        SunToolkit.flushPendingEvents(appContext);
        postEventPrivate(theEvent);
    }

    private final void postEventPrivate(AWTEvent theEvent) {
        theEvent.isPosted = true;
        pushPopLock.lock();
        try {
            if (nextQueue != null) {
                nextQueue.postEventPrivate(theEvent);
                return;
            }
            if (dispatchThread == null) {
                if (theEvent.getSource() == AWTAutoShutdown.getInstance()) {
                    return;
                } else {
                    initDispatchThread();
                }
            }
            postEvent(theEvent, getPriority(theEvent));
        } finally {
            pushPopLock.unlock();
        }
    }

    private static int getPriority(AWTEvent theEvent) {
        if (theEvent instanceof PeerEvent) {
            PeerEvent peerEvent = (PeerEvent) theEvent;
            if ((peerEvent.getFlags() & PeerEvent.ULTIMATE_PRIORITY_EVENT) != 0) {
                return ULTIMATE_PRIORITY;
            }
            if ((peerEvent.getFlags() & PeerEvent.PRIORITY_EVENT) != 0) {
                return HIGH_PRIORITY;
            }
            if ((peerEvent.getFlags() & PeerEvent.LOW_PRIORITY_EVENT) != 0) {
                return LOW_PRIORITY;
            }
        }
        int id = theEvent.getID();
        if ((id >= PaintEvent.PAINT_FIRST) && (id <= PaintEvent.PAINT_LAST)) {
            return LOW_PRIORITY;
        }
        return NORM_PRIORITY;
    }

    private void postEvent(AWTEvent theEvent, int priority) {
        if (coalesceEvent(theEvent, priority)) {
            return;
        }
        EventQueueItem newItem = new EventQueueItem(theEvent);
        cacheEQItem(newItem);
        boolean notifyID = (theEvent.getID() == this.waitForID);
        if (queues[priority].head == null) {
            boolean shouldNotify = noEvents();
            queues[priority].head = queues[priority].tail = newItem;
            if (shouldNotify) {
                if (theEvent.getSource() != AWTAutoShutdown.getInstance()) {
                    AWTAutoShutdown.getInstance().notifyThreadBusy(dispatchThread);
                }
                pushPopCond.signalAll();
            } else if (notifyID) {
                pushPopCond.signalAll();
            }
        } else {
            queues[priority].tail.next = newItem;
            queues[priority].tail = newItem;
            if (notifyID) {
                pushPopCond.signalAll();
            }
        }
    }

    private boolean coalescePaintEvent(PaintEvent e) {
        ComponentPeer sourcePeer = ((Component) e.getSource()).peer;
        if (sourcePeer != null) {
            sourcePeer.coalescePaintEvent(e);
        }
        EventQueueItem[] cache = ((Component) e.getSource()).eventCache;
        if (cache == null) {
            return false;
        }
        int index = eventToCacheIndex(e);
        if (index != -1 && cache[index] != null) {
            PaintEvent merged = mergePaintEvents(e, (PaintEvent) cache[index].event);
            if (merged != null) {
                cache[index].event = merged;
                return true;
            }
        }
        return false;
    }

    private PaintEvent mergePaintEvents(PaintEvent a, PaintEvent b) {
        Rectangle aRect = a.getUpdateRect();
        Rectangle bRect = b.getUpdateRect();
        if (bRect.contains(aRect)) {
            return b;
        }
        if (aRect.contains(bRect)) {
            return a;
        }
        return null;
    }

    private boolean coalesceMouseEvent(MouseEvent e) {
        EventQueueItem[] cache = ((Component) e.getSource()).eventCache;
        if (cache == null) {
            return false;
        }
        int index = eventToCacheIndex(e);
        if (index != -1 && cache[index] != null) {
            cache[index].event = e;
            return true;
        }
        return false;
    }

    private boolean coalescePeerEvent(PeerEvent e) {
        EventQueueItem[] cache = ((Component) e.getSource()).eventCache;
        if (cache == null) {
            return false;
        }
        int index = eventToCacheIndex(e);
        if (index != -1 && cache[index] != null) {
            e = e.coalesceEvents((PeerEvent) cache[index].event);
            if (e != null) {
                cache[index].event = e;
                return true;
            } else {
                cache[index] = null;
            }
        }
        return false;
    }

    private boolean coalesceOtherEvent(AWTEvent e, int priority) {
        int id = e.getID();
        Component source = (Component) e.getSource();
        for (EventQueueItem entry = queues[priority].head; entry != null; entry = entry.next) {
            if (entry.event.getSource() == source && entry.event.getID() == id) {
                AWTEvent coalescedEvent = source.coalesceEvents(entry.event, e);
                if (coalescedEvent != null) {
                    entry.event = coalescedEvent;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean coalesceEvent(AWTEvent e, int priority) {
        if (!(e.getSource() instanceof Component)) {
            return false;
        }
        if (e instanceof PeerEvent) {
            return coalescePeerEvent((PeerEvent) e);
        }
        if (((Component) e.getSource()).isCoalescingEnabled() && coalesceOtherEvent(e, priority)) {
            return true;
        }
        if (e instanceof PaintEvent) {
            return coalescePaintEvent((PaintEvent) e);
        }
        if (e instanceof MouseEvent) {
            return coalesceMouseEvent((MouseEvent) e);
        }
        return false;
    }

    private void cacheEQItem(EventQueueItem entry) {
        int index = eventToCacheIndex(entry.event);
        if (index != -1 && entry.event.getSource() instanceof Component) {
            Component source = (Component) entry.event.getSource();
            if (source.eventCache == null) {
                source.eventCache = new EventQueueItem[CACHE_LENGTH];
            }
            source.eventCache[index] = entry;
        }
    }

    private void uncacheEQItem(EventQueueItem entry) {
        int index = eventToCacheIndex(entry.event);
        if (index != -1 && entry.event.getSource() instanceof Component) {
            Component source = (Component) entry.event.getSource();
            if (source.eventCache == null) {
                return;
            }
            source.eventCache[index] = null;
        }
    }

    private static final int PAINT = 0;

    private static final int UPDATE = 1;

    private static final int MOVE = 2;

    private static final int DRAG = 3;

    private static final int PEER = 4;

    private static final int CACHE_LENGTH = 5;

    private static int eventToCacheIndex(AWTEvent e) {
        switch(e.getID()) {
            case PaintEvent.PAINT:
                return PAINT;
            case PaintEvent.UPDATE:
                return UPDATE;
            case MouseEvent.MOUSE_MOVED:
                return MOVE;
            case MouseEvent.MOUSE_DRAGGED:
                return e instanceof SunDropTargetEvent ? -1 : DRAG;
            default:
                return e instanceof PeerEvent ? PEER : -1;
        }
    }

    private boolean noEvents() {
        for (int i = 0; i < NUM_PRIORITIES; i++) {
            if (queues[i].head != null) {
                return false;
            }
        }
        return true;
    }

    public AWTEvent getNextEvent() throws InterruptedException {
        do {
            SunToolkit.flushPendingEvents(appContext);
            pushPopLock.lock();
            try {
                AWTEvent event = getNextEventPrivate();
                if (event != null) {
                    return event;
                }
                AWTAutoShutdown.getInstance().notifyThreadFree(dispatchThread);
                pushPopCond.await();
            } finally {
                pushPopLock.unlock();
            }
        } while (true);
    }

    AWTEvent getNextEventPrivate() throws InterruptedException {
        for (int i = NUM_PRIORITIES - 1; i >= 0; i--) {
            if (queues[i].head != null) {
                EventQueueItem entry = queues[i].head;
                queues[i].head = entry.next;
                if (entry.next == null) {
                    queues[i].tail = null;
                }
                uncacheEQItem(entry);
                return entry.event;
            }
        }
        return null;
    }

    AWTEvent getNextEvent(int id) throws InterruptedException {
        do {
            SunToolkit.flushPendingEvents(appContext);
            pushPopLock.lock();
            try {
                for (int i = 0; i < NUM_PRIORITIES; i++) {
                    for (EventQueueItem entry = queues[i].head, prev = null; entry != null; prev = entry, entry = entry.next) {
                        if (entry.event.getID() == id) {
                            if (prev == null) {
                                queues[i].head = entry.next;
                            } else {
                                prev.next = entry.next;
                            }
                            if (queues[i].tail == entry) {
                                queues[i].tail = prev;
                            }
                            uncacheEQItem(entry);
                            return entry.event;
                        }
                    }
                }
                waitForID = id;
                pushPopCond.await();
                waitForID = 0;
            } finally {
                pushPopLock.unlock();
            }
        } while (true);
    }

    public AWTEvent peekEvent() {
        pushPopLock.lock();
        try {
            for (int i = NUM_PRIORITIES - 1; i >= 0; i--) {
                if (queues[i].head != null) {
                    return queues[i].head.event;
                }
            }
        } finally {
            pushPopLock.unlock();
        }
        return null;
    }

    public AWTEvent peekEvent(int id) {
        pushPopLock.lock();
        try {
            for (int i = NUM_PRIORITIES - 1; i >= 0; i--) {
                EventQueueItem q = queues[i].head;
                for (; q != null; q = q.next) {
                    if (q.event.getID() == id) {
                        return q.event;
                    }
                }
            }
        } finally {
            pushPopLock.unlock();
        }
        return null;
    }

    private static final JavaSecurityAccess javaSecurityAccess = SharedSecrets.getJavaSecurityAccess();

    protected void dispatchEvent(final AWTEvent event) {
        final Object src = event.getSource();
        final PrivilegedAction<Void> action = new PrivilegedAction<Void>() {

            public Void run() {
                if (fwDispatcher == null) {
                    dispatchEventImpl(event, src);
                } else {
                    fwDispatcher.scheduleDispatch(new Runnable() {

                        @Override
                        public void run() {
                            dispatchEventImpl(event, src);
                        }
                    });
                }
                return null;
            }
        };
        final AccessControlContext stack = AccessController.getContext();
        final AccessControlContext srcAcc = getAccessControlContextFrom(src);
        final AccessControlContext eventAcc = event.getAccessControlContext();
        if (srcAcc == null) {
            javaSecurityAccess.doIntersectionPrivilege(action, stack, eventAcc);
        } else {
            javaSecurityAccess.doIntersectionPrivilege(new PrivilegedAction<Void>() {

                public Void run() {
                    javaSecurityAccess.doIntersectionPrivilege(action, eventAcc);
                    return null;
                }
            }, stack, srcAcc);
        }
    }

    private static AccessControlContext getAccessControlContextFrom(Object src) {
        return src instanceof Component ? ((Component) src).getAccessControlContext() : src instanceof MenuComponent ? ((MenuComponent) src).getAccessControlContext() : src instanceof TrayIcon ? ((TrayIcon) src).getAccessControlContext() : null;
    }

    private void dispatchEventImpl(final AWTEvent event, final Object src) {
        event.isPosted = true;
        if (event instanceof ActiveEvent) {
            setCurrentEventAndMostRecentTimeImpl(event);
            ((ActiveEvent) event).dispatch();
        } else if (src instanceof Component) {
            ((Component) src).dispatchEvent(event);
            event.dispatched();
        } else if (src instanceof MenuComponent) {
            ((MenuComponent) src).dispatchEvent(event);
        } else if (src instanceof TrayIcon) {
            ((TrayIcon) src).dispatchEvent(event);
        } else if (src instanceof AWTAutoShutdown) {
            if (noEvents()) {
                dispatchThread.stopDispatching();
            }
        } else {
            if (eventLog.isLoggable(PlatformLogger.Level.FINE)) {
                eventLog.fine("Unable to dispatch event: " + event);
            }
        }
    }

    public static long getMostRecentEventTime() {
        return Toolkit.getEventQueue().getMostRecentEventTimeImpl();
    }

    private long getMostRecentEventTimeImpl() {
        pushPopLock.lock();
        try {
            return (Thread.currentThread() == dispatchThread) ? mostRecentEventTime : System.currentTimeMillis();
        } finally {
            pushPopLock.unlock();
        }
    }

    long getMostRecentEventTimeEx() {
        pushPopLock.lock();
        try {
            return mostRecentEventTime;
        } finally {
            pushPopLock.unlock();
        }
    }

    public static AWTEvent getCurrentEvent() {
        return Toolkit.getEventQueue().getCurrentEventImpl();
    }

    private AWTEvent getCurrentEventImpl() {
        pushPopLock.lock();
        try {
            return (Thread.currentThread() == dispatchThread) ? currentEvent.get() : null;
        } finally {
            pushPopLock.unlock();
        }
    }

    public void push(EventQueue newEventQueue) {
        if (eventLog.isLoggable(PlatformLogger.Level.FINE)) {
            eventLog.fine("EventQueue.push(" + newEventQueue + ")");
        }
        pushPopLock.lock();
        try {
            EventQueue topQueue = this;
            while (topQueue.nextQueue != null) {
                topQueue = topQueue.nextQueue;
            }
            if (topQueue.fwDispatcher != null) {
                throw new RuntimeException("push() to queue with fwDispatcher");
            }
            if ((topQueue.dispatchThread != null) && (topQueue.dispatchThread.getEventQueue() == this)) {
                newEventQueue.dispatchThread = topQueue.dispatchThread;
                topQueue.dispatchThread.setEventQueue(newEventQueue);
            }
            while (topQueue.peekEvent() != null) {
                try {
                    newEventQueue.postEventPrivate(topQueue.getNextEventPrivate());
                } catch (InterruptedException ie) {
                    if (eventLog.isLoggable(PlatformLogger.Level.FINE)) {
                        eventLog.fine("Interrupted push", ie);
                    }
                }
            }
            topQueue.postEventPrivate(new InvocationEvent(topQueue, dummyRunnable));
            newEventQueue.previousQueue = topQueue;
            topQueue.nextQueue = newEventQueue;
            if (appContext.get(AppContext.EVENT_QUEUE_KEY) == topQueue) {
                appContext.put(AppContext.EVENT_QUEUE_KEY, newEventQueue);
            }
            pushPopCond.signalAll();
        } finally {
            pushPopLock.unlock();
        }
    }

    protected void pop() throws EmptyStackException {
        if (eventLog.isLoggable(PlatformLogger.Level.FINE)) {
            eventLog.fine("EventQueue.pop(" + this + ")");
        }
        pushPopLock.lock();
        try {
            EventQueue topQueue = this;
            while (topQueue.nextQueue != null) {
                topQueue = topQueue.nextQueue;
            }
            EventQueue prevQueue = topQueue.previousQueue;
            if (prevQueue == null) {
                throw new EmptyStackException();
            }
            topQueue.previousQueue = null;
            prevQueue.nextQueue = null;
            while (topQueue.peekEvent() != null) {
                try {
                    prevQueue.postEventPrivate(topQueue.getNextEventPrivate());
                } catch (InterruptedException ie) {
                    if (eventLog.isLoggable(PlatformLogger.Level.FINE)) {
                        eventLog.fine("Interrupted pop", ie);
                    }
                }
            }
            if ((topQueue.dispatchThread != null) && (topQueue.dispatchThread.getEventQueue() == this)) {
                prevQueue.dispatchThread = topQueue.dispatchThread;
                topQueue.dispatchThread.setEventQueue(prevQueue);
            }
            if (appContext.get(AppContext.EVENT_QUEUE_KEY) == this) {
                appContext.put(AppContext.EVENT_QUEUE_KEY, prevQueue);
            }
            topQueue.postEventPrivate(new InvocationEvent(topQueue, dummyRunnable));
            pushPopCond.signalAll();
        } finally {
            pushPopLock.unlock();
        }
    }

    public SecondaryLoop createSecondaryLoop() {
        return createSecondaryLoop(null, null, 0);
    }

    SecondaryLoop createSecondaryLoop(Conditional cond, EventFilter filter, long interval) {
        pushPopLock.lock();
        try {
            if (nextQueue != null) {
                return nextQueue.createSecondaryLoop(cond, filter, interval);
            }
            if (fwDispatcher != null) {
                return fwDispatcher.createSecondaryLoop();
            }
            if (dispatchThread == null) {
                initDispatchThread();
            }
            return new WaitDispatchSupport(dispatchThread, cond, filter, interval);
        } finally {
            pushPopLock.unlock();
        }
    }

    public static boolean isDispatchThread() {
        EventQueue eq = Toolkit.getEventQueue();
        return eq.isDispatchThreadImpl();
    }

    final boolean isDispatchThreadImpl() {
        EventQueue eq = this;
        pushPopLock.lock();
        try {
            EventQueue next = eq.nextQueue;
            while (next != null) {
                eq = next;
                next = eq.nextQueue;
            }
            if (eq.fwDispatcher != null) {
                return eq.fwDispatcher.isDispatchThread();
            }
            return (Thread.currentThread() == eq.dispatchThread);
        } finally {
            pushPopLock.unlock();
        }
    }

    final void initDispatchThread() {
        pushPopLock.lock();
        try {
            if (dispatchThread == null && !threadGroup.isDestroyed() && !appContext.isDisposed()) {
                dispatchThread = AccessController.doPrivileged(new PrivilegedAction<EventDispatchThread>() {

                    public EventDispatchThread run() {
                        EventDispatchThread t = new EventDispatchThread(threadGroup, name, EventQueue.this);
                        t.setContextClassLoader(classLoader);
                        t.setPriority(Thread.NORM_PRIORITY + 1);
                        t.setDaemon(false);
                        return t;
                    }
                });
                AWTAutoShutdown.getInstance().notifyThreadBusy(dispatchThread);
                dispatchThread.start();
            }
        } finally {
            pushPopLock.unlock();
        }
    }

    final boolean detachDispatchThread(EventDispatchThread edt, boolean forceDetach) {
        SunToolkit.flushPendingEvents(appContext);
        pushPopLock.lock();
        try {
            if (edt == dispatchThread) {
                if (!forceDetach && (peekEvent() != null)) {
                    return false;
                }
                dispatchThread = null;
            }
            AWTAutoShutdown.getInstance().notifyThreadFree(edt);
            return true;
        } finally {
            pushPopLock.unlock();
        }
    }

    final EventDispatchThread getDispatchThread() {
        pushPopLock.lock();
        try {
            return dispatchThread;
        } finally {
            pushPopLock.unlock();
        }
    }

    final void removeSourceEvents(Object source, boolean removeAllEvents) {
        SunToolkit.flushPendingEvents(appContext);
        pushPopLock.lock();
        try {
            for (int i = 0; i < NUM_PRIORITIES; i++) {
                EventQueueItem entry = queues[i].head;
                EventQueueItem prev = null;
                while (entry != null) {
                    if ((entry.event.getSource() == source) && (removeAllEvents || !(entry.event instanceof SequencedEvent || entry.event instanceof SentEvent || entry.event instanceof FocusEvent || entry.event instanceof WindowEvent || entry.event instanceof KeyEvent || entry.event instanceof InputMethodEvent))) {
                        if (entry.event instanceof SequencedEvent) {
                            ((SequencedEvent) entry.event).dispose();
                        }
                        if (entry.event instanceof SentEvent) {
                            ((SentEvent) entry.event).dispose();
                        }
                        if (prev == null) {
                            queues[i].head = entry.next;
                        } else {
                            prev.next = entry.next;
                        }
                        uncacheEQItem(entry);
                    } else {
                        prev = entry;
                    }
                    entry = entry.next;
                }
                queues[i].tail = prev;
            }
        } finally {
            pushPopLock.unlock();
        }
    }

    synchronized long getMostRecentKeyEventTime() {
        pushPopLock.lock();
        try {
            return mostRecentKeyEventTime;
        } finally {
            pushPopLock.unlock();
        }
    }

    static void setCurrentEventAndMostRecentTime(AWTEvent e) {
        Toolkit.getEventQueue().setCurrentEventAndMostRecentTimeImpl(e);
    }

    private void setCurrentEventAndMostRecentTimeImpl(AWTEvent e) {
        pushPopLock.lock();
        try {
            if (Thread.currentThread() != dispatchThread) {
                return;
            }
            currentEvent = new WeakReference<>(e);
            long mostRecentEventTime2 = Long.MIN_VALUE;
            if (e instanceof InputEvent) {
                InputEvent ie = (InputEvent) e;
                mostRecentEventTime2 = ie.getWhen();
                if (e instanceof KeyEvent) {
                    mostRecentKeyEventTime = ie.getWhen();
                }
            } else if (e instanceof InputMethodEvent) {
                InputMethodEvent ime = (InputMethodEvent) e;
                mostRecentEventTime2 = ime.getWhen();
            } else if (e instanceof ActionEvent) {
                ActionEvent ae = (ActionEvent) e;
                mostRecentEventTime2 = ae.getWhen();
            } else if (e instanceof InvocationEvent) {
                InvocationEvent ie = (InvocationEvent) e;
                mostRecentEventTime2 = ie.getWhen();
            }
            mostRecentEventTime = Math.max(mostRecentEventTime, mostRecentEventTime2);
        } finally {
            pushPopLock.unlock();
        }
    }

    public static void invokeLater(Runnable runnable) {
        Toolkit.getEventQueue().postEvent(new InvocationEvent(Toolkit.getDefaultToolkit(), runnable));
    }

    public static void invokeAndWait(Runnable runnable) throws InterruptedException, InvocationTargetException {
        invokeAndWait(Toolkit.getDefaultToolkit(), runnable);
    }

    static void invokeAndWait(Object source, Runnable runnable) throws InterruptedException, InvocationTargetException {
        if (EventQueue.isDispatchThread()) {
            throw new Error("Cannot call invokeAndWait from the event dispatcher thread");
        }
        class AWTInvocationLock {
        }
        Object lock = new AWTInvocationLock();
        InvocationEvent event = new InvocationEvent(source, runnable, lock, true);
        synchronized (lock) {
            Toolkit.getEventQueue().postEvent(event);
            while (!event.isDispatched()) {
                lock.wait();
            }
        }
        Throwable eventThrowable = event.getThrowable();
        if (eventThrowable != null) {
            throw new InvocationTargetException(eventThrowable);
        }
    }

    private void wakeup(boolean isShutdown) {
        pushPopLock.lock();
        try {
            if (nextQueue != null) {
                nextQueue.wakeup(isShutdown);
            } else if (dispatchThread != null) {
                pushPopCond.signalAll();
            } else if (!isShutdown) {
                initDispatchThread();
            }
        } finally {
            pushPopLock.unlock();
        }
    }

    private void setFwDispatcher(FwDispatcher dispatcher) {
        if (nextQueue != null) {
            nextQueue.setFwDispatcher(dispatcher);
        } else {
            fwDispatcher = dispatcher;
        }
    }
}

class Queue {

    EventQueueItem head;

    EventQueueItem tail;
}