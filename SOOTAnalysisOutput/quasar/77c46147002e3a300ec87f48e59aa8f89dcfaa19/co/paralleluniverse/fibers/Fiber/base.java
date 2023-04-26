package co.paralleluniverse.fibers;

import co.paralleluniverse.common.monitoring.FlightRecorder;
import co.paralleluniverse.common.monitoring.FlightRecorderMessage;
import co.paralleluniverse.common.util.Debug;
import co.paralleluniverse.common.util.Exceptions;
import co.paralleluniverse.common.util.ExtendedStackTrace;
import co.paralleluniverse.common.util.ExtendedStackTraceElement;
import co.paralleluniverse.common.util.Objects;
import co.paralleluniverse.common.util.Pair;
import co.paralleluniverse.common.util.SystemProperties;
import co.paralleluniverse.common.util.UtilUnsafe;
import co.paralleluniverse.common.util.VisibleForTesting;
import co.paralleluniverse.concurrent.util.ThreadAccess;
import co.paralleluniverse.concurrent.util.ThreadUtil;
import co.paralleluniverse.fibers.FiberForkJoinScheduler.FiberForkJoinTask;
import co.paralleluniverse.fibers.instrument.SuspendableHelper;
import co.paralleluniverse.io.serialization.ByteArraySerializer;
import co.paralleluniverse.io.serialization.kryo.KryoSerializer;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.Stranded;
import co.paralleluniverse.strands.SuspendableCallable;
import co.paralleluniverse.strands.SuspendableRunnable;
import co.paralleluniverse.strands.SuspendableUtils.VoidSuspendableCallable;
import static co.paralleluniverse.strands.SuspendableUtils.runnableToCallable;
import co.paralleluniverse.strands.dataflow.Val;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import java.lang.reflect.Member;

public class Fiber<V> extends Strand implements Joinable<V>, Serializable, Future<V> {

    static final boolean USE_VAL_FOR_RESULT = true;

    private static final boolean traceInterrupt = SystemProperties.isEmptyOrTrue("co.paralleluniverse.fibers.traceInterrupt");

    private static final boolean disableAgentWarning = SystemProperties.isEmptyOrTrue("co.paralleluniverse.fibers.disableAgentWarning");

    public static final int DEFAULT_STACK_SIZE = 32;

    private static final Object SERIALIZER_BLOCKER = new Object();

    private static final boolean MAINTAIN_ACCESS_CONTROL_CONTEXT = (System.getSecurityManager() != null);

    private static final long serialVersionUID = 2783452871536981L;

    protected static final FlightRecorder flightRecorder = Debug.isDebug() ? Debug.getGlobalFlightRecorder() : null;

    private static final boolean verifyInstrumentation = SystemProperties.isEmptyOrTrue("co.paralleluniverse.fibers.verifyInstrumentation");

    static {
        if (Debug.isDebug())
            System.err.println("QUASAR WARNING: Debug mode enabled. This may harm performance.");
        if (Debug.isAssertionsEnabled())
            System.err.println("QUASAR WARNING: Assertions enabled. This may harm performance.");
        if (!SuspendableHelper.isJavaAgentActive() && !disableAgentWarning)
            System.err.println("QUASAR WARNING: Quasar Java Agent isn't running. If you're using another instrumentation method you can ignore this message; " + "otherwise, please refer to the Getting Started section in the Quasar documentation.");
        assert printVerifyInstrumentationWarning();
    }

    private static boolean printVerifyInstrumentationWarning() {
        if (verifyInstrumentation)
            System.err.println("QUASAR WARNING: Fibers are set to verify instrumentation. This may *severely* harm performance.");
        return true;
    }

    private static volatile UncaughtExceptionHandler defaultUncaughtExceptionHandler = new UncaughtExceptionHandler() {

        @Override
        public void uncaughtException(Strand s, Throwable e) {
            System.err.print("Exception in Fiber \"" + s.getName() + "\" ");
            if (e instanceof NullPointerException || e instanceof ClassCastException || Exceptions.unwrap(e) instanceof NullPointerException || Exceptions.unwrap(e) instanceof ClassCastException)
                System.err.println("If this exception looks strange, perhaps you've forgotten to instrument a blocking method. Run your program with -Dco.paralleluniverse.fibers.verifyInstrumentation to catch the culprit!");
            System.err.println(e);
            Strand.printStackTrace(threadToFiberStack(e.getStackTrace()), System.err);
            checkInstrumentation(ExtendedStackTrace.of(e));
        }
    };

    private static final AtomicLong idGen = new AtomicLong(10000000L);

    private static long nextFiberId() {
        return idGen.incrementAndGet();
    }

    private transient FiberScheduler scheduler;

    private transient FiberTask<V> task;

    private String name;

    private int initialStackSize;

    private transient long fid;

    final Stack stack;

    private volatile State state;

    private InterruptedException interruptStack;

    private volatile boolean interrupted;

    private long run;

    private transient boolean noPreempt;

    private transient Thread runningThread;

    private final SuspendableCallable<V> target;

    private transient ClassLoader contextClassLoader;

    private transient AccessControlContext inheritedAccessControlContext;

    private Object fiberLocals;

    private Object inheritableFiberLocals;

    private long sleepStart;

    private transient Future<Void> timeoutTask;

    private transient ParkAction prePark;

    private transient ParkAction postPark;

    private transient Object result;

    private transient boolean getStackTrace;

    private volatile UncaughtExceptionHandler uncaughtExceptionHandler;

    transient DummyRunnable fiberRef = new DummyRunnable(this);

    @SuppressWarnings("LeakingThisInConstructor")
    public Fiber(String name, FiberScheduler scheduler, int stackSize, SuspendableCallable<V> target) {
        this.state = State.NEW;
        this.fid = nextFiberId();
        this.scheduler = scheduler;
        setName(name);
        Strand parent = Strand.currentStrand();
        this.target = target;
        this.task = scheduler != null ? scheduler.newFiberTask(this) : new FiberForkJoinTask(this);
        this.initialStackSize = stackSize;
        this.stack = new Stack(this, stackSize > 0 ? stackSize : DEFAULT_STACK_SIZE);
        if (Debug.isDebug())
            record(1, "Fiber", "<init>", "Creating fiber name: %s, scheduler: %s, parent: %s, target: %s, task: %s, stackSize: %s", name, scheduler, parent, target, task, stackSize);
        if (target != null) {
            verifyInstrumentedTarget(target);
            if (target instanceof Stranded)
                ((Stranded) target).setStrand(this);
        } else if (!isInstrumented(this.getClass())) {
            throw new IllegalArgumentException("Fiber class " + this.getClass().getName() + " has not been instrumented.");
        }
        final Thread currentThread = Thread.currentThread();
        Object inheritableThreadLocals = ThreadAccess.getInheritableThreadLocals(currentThread);
        if (inheritableThreadLocals != null)
            this.inheritableFiberLocals = ThreadAccess.createInheritedMap(inheritableThreadLocals);
        this.contextClassLoader = ThreadAccess.getContextClassLoader(currentThread);
        if (MAINTAIN_ACCESS_CONTROL_CONTEXT)
            this.inheritedAccessControlContext = AccessController.getContext();
        if (USE_VAL_FOR_RESULT)
            this.result = new Val<V>();
        record(1, "Fiber", "<init>", "Created fiber %s", this);
    }

    public Fiber(String name, int stackSize, SuspendableCallable<V> target) {
        this(name, defaultScheduler(), stackSize, target);
    }

    private static FiberScheduler defaultScheduler() {
        final Fiber parent = currentFiber();
        if (parent == null)
            return DefaultFiberScheduler.getInstance();
        else
            return parent.getScheduler();
    }

    private static Fiber verifyParent() {
        final Fiber parent = currentFiber();
        if (parent == null)
            throw new IllegalStateException("This constructor may only be used from within a Fiber");
        return parent;
    }

    private static void verifyInstrumentedTarget(SuspendableCallable<?> target) {
        Object t = target;
        if (target instanceof VoidSuspendableCallable)
            t = ((VoidSuspendableCallable) target).getRunnable();
        if (t.getClass().getName().contains("$$Lambda$"))
            return;
        if (verifyInstrumentation && !isInstrumented(t.getClass()))
            throw new VerifyInstrumentationException("Target class " + t.getClass() + " has not been instrumented.");
    }

    private Future<V> future() {
        return USE_VAL_FOR_RESULT ? (Val<V>) result : task;
    }

    public final SuspendableCallable<V> getTarget() {
        return target;
    }

    @Override
    public final int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public final boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final Fiber<V> setName(String name) {
        if (state != State.NEW)
            throw new IllegalStateException("Fiber name cannot be changed once it has started");
        if (name != null)
            this.name = name;
        else
            this.name = "fiber-" + ((scheduler != null && scheduler != DefaultFiberScheduler.getInstance()) ? (scheduler.getName() + '-') : "") + fid;
        return this;
    }

    @Override
    public long getId() {
        return fid;
    }

    public Object getTask() {
        return task;
    }

    public FiberScheduler getScheduler() {
        return scheduler;
    }

    long getRun() {
        return run;
    }

    public Fiber(String name, FiberScheduler scheduler, SuspendableCallable<V> target) {
        this(name, scheduler, -1, target);
    }

    public Fiber(FiberScheduler scheduler, SuspendableCallable<V> target) {
        this(null, scheduler, -1, target);
    }

    public Fiber(String name, FiberScheduler scheduler, int stackSize, SuspendableRunnable target) {
        this(name, scheduler, stackSize, (SuspendableCallable<V>) runnableToCallable(target));
    }

    public Fiber(String name, FiberScheduler scheduler, SuspendableRunnable target) {
        this(name, scheduler, -1, target);
    }

    public Fiber(FiberScheduler scheduler, SuspendableRunnable target) {
        this(null, scheduler, -1, target);
    }

    public Fiber(String name, FiberScheduler scheduler, int stackSize) {
        this(name, scheduler, stackSize, (SuspendableCallable) null);
    }

    public Fiber(String name, FiberScheduler scheduler) {
        this(name, scheduler, -1, (SuspendableCallable) null);
    }

    public Fiber(FiberScheduler scheduler) {
        this(null, scheduler, -1, (SuspendableCallable) null);
    }

    public Fiber(String name, SuspendableCallable<V> target) {
        this(name, -1, target);
    }

    public Fiber(SuspendableCallable<V> target) {
        this(null, -1, target);
    }

    public Fiber(String name, int stackSize, SuspendableRunnable target) {
        this(name, stackSize, (SuspendableCallable<V>) runnableToCallable(target));
    }

    public Fiber(String name, SuspendableRunnable target) {
        this(name, -1, target);
    }

    public Fiber(SuspendableRunnable target) {
        this(null, -1, target);
    }

    public Fiber(String name, int stackSize) {
        this(name, stackSize, (SuspendableCallable) null);
    }

    public Fiber(String name) {
        this(name, -1, (SuspendableCallable) null);
    }

    public Fiber() {
        this(null, -1, (SuspendableCallable) null);
    }

    public Fiber(Fiber fiber, SuspendableCallable<V> target) {
        this(fiber.name, fiber.scheduler, fiber.initialStackSize, target);
    }

    public Fiber(Fiber fiber, SuspendableRunnable target) {
        this(fiber.name, fiber.scheduler, fiber.initialStackSize, target);
    }

    public Fiber(Fiber fiber, FiberScheduler scheduler, SuspendableCallable<V> target) {
        this(fiber.name, scheduler, fiber.initialStackSize, target);
    }

    public Fiber(Fiber fiber, FiberScheduler scheduler, SuspendableRunnable target) {
        this(fiber.name, scheduler, fiber.initialStackSize, target);
    }

    public static Fiber currentFiber() {
        return getCurrentFiber();
    }

    public static boolean isCurrentFiber() {
        return FiberForkJoinScheduler.isFiberThread(Thread.currentThread()) || getCurrentFiber() != null;
    }

    public static long getCurrentRun() {
        Fiber f = currentFiber();
        if (f == null)
            throw new IllegalStateException("Not in fiber");
        return f.getRun();
    }

    @Override
    public final boolean isFiber() {
        return true;
    }

    @Override
    public final Object getUnderlying() {
        return this;
    }

    static boolean park(Object blocker, ParkAction postParkActions, long timeout, TimeUnit unit) throws SuspendExecution {
        return verifySuspend().park1(blocker, postParkActions, timeout, unit);
    }

    static boolean park(Object blocker, ParkAction postParkActions) throws SuspendExecution {
        return park(blocker, postParkActions, 0, null);
    }

    public static boolean park(Object blocker, long timeout, TimeUnit unit) throws SuspendExecution {
        return park(blocker, null, timeout, unit);
    }

    public static void park(Object blocker) throws SuspendExecution {
        park(blocker, null, 0, null);
    }

    public static void park(long timeout, TimeUnit unit) throws SuspendExecution {
        park(null, null, timeout, unit);
    }

    public static void park() throws SuspendExecution {
        park(null, null, 0, null);
    }

    public static void yield() throws SuspendExecution {
        verifySuspend().yield1();
    }

    public static void parkAndUnpark(Fiber other) throws SuspendExecution {
        parkAndUnpark(other, null);
    }

    public static void parkAndUnpark(Fiber other, Object blocker) throws SuspendExecution {
        verifySuspend().parkAndUnpark1(other, blocker, 0, TimeUnit.NANOSECONDS);
    }

    public static void yieldAndUnpark(Fiber other, Object blocker) throws SuspendExecution {
        verifySuspend().yieldAndUnpark1(other, blocker, 0, TimeUnit.NANOSECONDS);
    }

    public static void yieldAndUnpark(Fiber other) throws SuspendExecution {
        yieldAndUnpark(other, null);
    }

    public static void sleep(long millis) throws InterruptedException, SuspendExecution {
        sleep(millis, TimeUnit.MILLISECONDS);
    }

    public static void sleep(long millis, int nanos) throws InterruptedException, SuspendExecution {
        sleep(TimeUnit.MILLISECONDS.toNanos(millis) + nanos, TimeUnit.NANOSECONDS);
    }

    public static void sleep(long duration, TimeUnit unit) throws InterruptedException, SuspendExecution {
        verifySuspend().sleep1(duration, unit);
    }

    public static boolean interrupted() {
        final Fiber current = currentFiber();
        if (current == null)
            throw new IllegalStateException("Not called on a fiber");
        final boolean interrupted = current.isInterrupted();
        if (interrupted)
            current.interrupted = false;
        return interrupted;
    }

    private boolean park1(Object blocker, ParkAction postParkAction, long timeout, TimeUnit unit) throws SuspendExecution {
        record(1, "Fiber", "park", "Parking %s blocker: %s", this, blocker);
        if (isRecordingLevel(2) && !getStackTrace)
            record(2, "Fiber", "park", "Parking %s at %s", this, Arrays.toString(getStackTrace()));
        if (prePark != null)
            prePark.run(this);
        this.postPark = postParkAction;
        if (timeout > 0 && unit != null)
            this.timeoutTask = scheduler.schedule(this, blocker, timeout, unit);
        return task.park(blocker, postParkAction != null);
    }

    private void yield1() throws SuspendExecution {
        if (isRecordingLevel(2))
            record(2, "Fiber", "yield", "Yielding %s at %s", this, Arrays.toString(getStackTrace()));
        if (prePark != null)
            prePark.run(this);
        task.yield();
    }

    private void parkAndUnpark1(Fiber other, Object blocker, long timeout, TimeUnit unit) throws SuspendExecution {
        record(1, "Fiber", "parkAndUnpark", "Parking %s and unparking %s blocker: %s", this, other, blocker);
        if (!other.exec(blocker, timeout, unit))
            other.unpark(blocker);
        park1(blocker, null, -1, null);
    }

    private void yieldAndUnpark1(Fiber other, Object blocker, long timeout, TimeUnit unit) throws SuspendExecution {
        record(1, "Fiber", "yieldAndUnpark", "Yielding %s and unparking %s blocker: %s", this, other, blocker);
        if (!other.exec(blocker, timeout, unit)) {
            other.unpark(blocker);
            yield1();
        }
    }

    void preempt() throws SuspendExecution {
        if (isRecordingLevel(2))
            record(2, "Fiber", "preempt", "Preempting %s at %s", this, Arrays.toString(getStackTrace()));
        task.yield();
    }

    boolean exec() {
        if (future().isDone())
            return true;
        if (state == State.RUNNING)
            throw new IllegalStateException("Not new or suspended");
        cancelTimeoutTask();
        final FibersMonitor monitor = getMonitor();
        if (Debug.isDebug())
            record(1, "Fiber", "exec", "running %s %s %s", state, this, run);
        final Thread currentThread = Thread.currentThread();
        final Object old = getCurrentTarget(currentThread);
        installFiberDataInThread(currentThread);
        run++;
        runningThread = currentThread;
        state = State.RUNNING;
        boolean restored = false;
        try {
            try {
                final V res = run1();
                runningThread = null;
                state = State.TERMINATED;
                record(1, "Fiber", "exec", "finished %s %s res: %s", state, this, this.result);
                monitorFiberTerminated(monitor);
                onCompletion();
                setResult(res);
                return true;
            } catch (RuntimeSuspendExecution e) {
                throw (SuspendExecution) e.getCause();
            }
        } catch (SuspendExecution ex) {
            assert ex == SuspendExecution.PARK || ex == SuspendExecution.YIELD;
            stack.resumeStack();
            runningThread = null;
            orderedSetState(timeoutTask != null ? State.TIMED_WAITING : State.WAITING);
            final ParkAction ppa = postPark;
            clearRunSettings();
            restoreThreadData(currentThread, old);
            restored = true;
            record(1, "Fiber", "exec", "parked %s %s", state, this);
            task.doPark(ex == SuspendExecution.YIELD);
            assert ppa == null || ex == SuspendExecution.PARK;
            if (ppa != null)
                ppa.run(this);
            return false;
        } catch (Throwable t) {
            clearRunSettings();
            runningThread = null;
            if (Debug.isDebug()) {
                if (t instanceof InterruptedException)
                    record(1, "Fiber", "exec", "InterruptedException: %s, %s", state, this);
                else {
                    StringWriter sw = new StringWriter();
                    t.printStackTrace(new PrintWriter(sw));
                    record(1, "Fiber", "exec", "Exception in %s %s: %s %s", state, this, t, sw.toString());
                }
            }
            try {
                if (t instanceof InterruptedException) {
                    throw new RuntimeException(t);
                } else {
                    onException(t);
                    throw Exceptions.rethrow(t);
                }
            } finally {
                state = State.TERMINATED;
                task.setState(0);
                monitorFiberTerminated(monitor);
                setException(t);
            }
        } finally {
            if (!restored)
                restoreThreadData(currentThread, old);
            if (scheduler instanceof FiberForkJoinScheduler)
                ((FiberForkJoinScheduler) scheduler).tryOnIdle();
        }
    }

    void setResult(V res) {
        try {
            if (USE_VAL_FOR_RESULT)
                ((Val<V>) this.result).set(res);
            else
                this.result = res;
        } catch (IllegalStateException e) {
        }
    }

    private void setException(Throwable t) {
        try {
            if (USE_VAL_FOR_RESULT)
                ((Val<V>) this.result).setException(t);
        } catch (IllegalStateException e) {
        }
    }

    private void clearRunSettings() {
        this.prePark = null;
        this.postPark = null;
        this.noPreempt = false;
    }

    private StackTraceElement[] execStackTrace1() {
        if (future().isDone())
            return null;
        if (state == State.RUNNING)
            throw new IllegalStateException("Not new or suspended");
        this.getStackTrace = true;
        final Thread currentThread = Thread.currentThread();
        final Object old = getCurrentTarget(currentThread);
        setCurrentFiber(this, currentThread);
        try {
            try {
                run1();
                throw new AssertionError();
            } catch (RuntimeSuspendExecution e) {
                throw (SuspendExecution) e.getCause();
            }
        } catch (SuspendExecution | IllegalStateException ex) {
            assert ex != SuspendExecution.PARK && ex != SuspendExecution.YIELD;
            stack.resumeStack();
            setCurrentTarget(old, currentThread);
            this.noPreempt = false;
            this.getStackTrace = false;
            task.doPark(false);
            StackTraceElement[] st = ex.getStackTrace();
            if (ex instanceof IllegalStateException) {
                int index = -1;
                for (int i = 0; i < st.length; i++) {
                    if (Fiber.class.getName().equals(st[i].getClassName()) && "sleep".equals(st[i].getMethodName())) {
                        index = i;
                        break;
                    }
                }
                assert index >= 0;
                st = skipStackTraceElements(st, index);
            } else
                st = skipStackTraceElements(st, 2);
            return st;
        } catch (Throwable ex) {
            throw new AssertionError(ex);
        }
    }

    public FibersMonitor getMonitor() {
        if (scheduler == null)
            return null;
        return scheduler.getMonitor();
    }

    private void monitorFiberTerminated(FibersMonitor monitor) {
        if (monitor != null)
            monitor.fiberTerminated(this);
    }

    private void cancelTimeoutTask() {
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
            timeoutTask = null;
        }
    }

    private void installFiberDataInThread(Thread currentThread) {
        record(1, "Fiber", "installFiberDataInThread", "%s <-> %s", this, currentThread);
        installFiberLocals(currentThread);
        installFiberContextClassLoader(currentThread);
        if (MAINTAIN_ACCESS_CONTROL_CONTEXT)
            installFiberInheritedAccessControlContext(currentThread);
        setCurrentFiber(this, currentThread);
    }

    private void restoreThreadData(Thread currentThread, Object old) {
        record(1, "Fiber", "restoreThreadData", "%s <-> %s", this, currentThread);
        restoreThreadLocals(currentThread);
        restoreThreadContextClassLoader(currentThread);
        if (MAINTAIN_ACCESS_CONTROL_CONTEXT)
            restoreThreadInheritedAccessControlContext(currentThread);
        setCurrentTarget(old, currentThread);
    }

    void installFiberLocals(Thread currentThread) {
        switchFiberAndThreadLocals(currentThread, true);
    }

    void restoreThreadLocals(Thread currentThread) {
        switchFiberAndThreadLocals(currentThread, false);
    }

    private void switchFiberAndThreadLocals(Thread currentThread, boolean install) {
        if (scheduler == null)
            return;
        Object tmpThreadLocals = ThreadAccess.getThreadLocals(currentThread);
        Object tmpInheritableThreadLocals = ThreadAccess.getInheritableThreadLocals(currentThread);
        if (isRecordingLevel(2)) {
            record(2, "Fiber", "switchFiberAndThreadLocals", "fiberLocals: %s", ThreadUtil.getThreadLocalsString(install ? this.fiberLocals : tmpThreadLocals));
            record(2, "Fiber", "switchFiberAndThreadLocals", "inheritableFiberLocals: %s", ThreadUtil.getThreadLocalsString(install ? this.inheritableFiberLocals : tmpInheritableThreadLocals));
        }
        ThreadAccess.setThreadLocals(currentThread, this.fiberLocals);
        ThreadAccess.setInheritableThreadLocals(currentThread, this.inheritableFiberLocals);
        this.fiberLocals = tmpThreadLocals;
        this.inheritableFiberLocals = tmpInheritableThreadLocals;
    }

    private void installFiberContextClassLoader(Thread currentThread) {
        final ClassLoader origContextClassLoader = ThreadAccess.getContextClassLoader(currentThread);
        ThreadAccess.setContextClassLoader(currentThread, contextClassLoader);
        this.contextClassLoader = origContextClassLoader;
    }

    private void restoreThreadContextClassLoader(Thread currentThread) {
        final ClassLoader origContextClassLoader = contextClassLoader;
        this.contextClassLoader = ThreadAccess.getContextClassLoader(currentThread);
        ThreadAccess.setContextClassLoader(currentThread, origContextClassLoader);
    }

    private void installFiberInheritedAccessControlContext(Thread currentThread) {
        final AccessControlContext origAcc = ThreadAccess.getInheritedAccessControlContext(currentThread);
        ThreadAccess.setInheritedAccessControlContext(currentThread, inheritedAccessControlContext);
        this.inheritedAccessControlContext = origAcc;
    }

    private void restoreThreadInheritedAccessControlContext(Thread currentThread) {
        final AccessControlContext origAcc = inheritedAccessControlContext;
        this.inheritedAccessControlContext = ThreadAccess.getInheritedAccessControlContext(currentThread);
        ThreadAccess.setInheritedAccessControlContext(currentThread, origAcc);
    }

    private void setCurrentFiber(Fiber fiber, Thread currentThread) {
        if (scheduler != null)
            scheduler.setCurrentFiber(fiber, currentThread);
        else
            currentStrand.set(fiber);
    }

    private void setCurrentTarget(Object target, Thread currentThread) {
        if (scheduler != null)
            scheduler.setCurrentTarget(target, currentThread);
        else
            currentStrand.set(null);
    }

    private Object getCurrentTarget(Thread currentThread) {
        if (scheduler == null)
            return null;
        return scheduler.getCurrentTarget(currentThread);
    }

    private static Fiber getCurrentFiber() {
        final Thread currentThread = Thread.currentThread();
        if (FiberForkJoinScheduler.isFiberThread(currentThread))
            return FiberForkJoinScheduler.getTargetFiber(currentThread);
        else {
            final Strand s = currentStrand.get();
            return s instanceof Fiber ? (Fiber) s : null;
        }
    }

    static final class DummyRunnable implements Runnable {

        final Fiber fiber;

        public DummyRunnable(Fiber fiber) {
            this.fiber = fiber;
        }

        @Override
        public void run() {
            throw new RuntimeException("This method shouldn't be run. This object is a placeholder.");
        }
    }

    private V run1() throws SuspendExecution, InterruptedException {
        return run();
    }

    protected V run() throws SuspendExecution, InterruptedException {
        if (target != null)
            return target.run();
        return null;
    }

    public Fiber inheritThreadLocals() {
        if (state != State.NEW)
            throw new IllegalStateException("Method called on a started fiber");
        this.fiberLocals = ThreadAccess.cloneThreadLocalMap(ThreadAccess.getThreadLocals(Thread.currentThread()));
        return this;
    }

    @Override
    public final Fiber<V> start() {
        if (!casState(State.NEW, State.STARTED)) {
            if (state == State.TERMINATED && future().isCancelled())
                return this;
            throw new IllegalThreadStateException("Fiber has already been started or has died");
        }
        getMonitor().fiberStarted(this);
        task.submit();
        return this;
    }

    protected void onParked() {
    }

    protected void onResume() throws SuspendExecution, InterruptedException {
        if (getStackTrace) {
            try {
                park1(null, null, 0, null);
            } catch (SuspendExecution e) {
            }
            SuspendExecution ex = new SuspendExecution();
            ex.setStackTrace(new Throwable().getStackTrace());
            throw ex;
        }
        record(1, "Fiber", "onResume", "Resuming %s", this);
        if (isRecordingLevel(2))
            record(2, "Fiber", "onResume", "Resuming %s at: %s", this, Arrays.toString(getStackTrace()));
    }

    final void preemptionPoint(int type) throws SuspendExecution {
        if (noPreempt)
            return;
        if (shouldPreempt(type))
            preempt();
    }

    protected boolean shouldPreempt(int type) {
        return false;
    }

    protected void onCompletion() {
    }

    protected void onException(Throwable t) {
        try {
            UncaughtExceptionHandler ueh;
            if ((ueh = uncaughtExceptionHandler) != null)
                ueh.uncaughtException(this, t);
            else if ((ueh = defaultUncaughtExceptionHandler) != null)
                ueh.uncaughtException(this, t);
        } catch (Exception e) {
            if (e != t && t != null)
                t.addSuppressed(e);
        }
        throw Exceptions.rethrow(t);
    }

    @Override
    public final void interrupt() {
        if (traceInterrupt)
            interruptStack = new InterruptedException();
        interrupted = true;
        unpark(FiberTask.EMERGENCY_UNBLOCKER);
    }

    @Override
    public final boolean isInterrupted() {
        return interrupted;
    }

    @Override
    public final InterruptedException getInterruptStack() {
        if (!traceInterrupt)
            return null;
        return interruptStack;
    }

    @Override
    public final boolean isAlive() {
        return state != State.NEW && !future().isDone();
    }

    @Override
    public final State getState() {
        return state;
    }

    @Override
    public final boolean isTerminated() {
        return state == State.TERMINATED;
    }

    @Override
    public final Object getBlocker() {
        return task.getBlocker();
    }

    final boolean exec(Object blocker, long timeout, TimeUnit unit) {
        if (!scheduler.isCurrentThreadInScheduler())
            return false;
        record(1, "Fiber", "exec", "Blocker %s attempting to immediately execute %s", blocker, this);
        if (!tryUnpark(blocker, timeout, unit)) {
            record(1, "Fiber", "exec", "Blocker %s attempt to immediately execute %s FAILED", blocker, this);
            return false;
        }
        immediateExecHelper();
        return true;
    }

    final boolean exec(Object blocker, ParkAction prePark) {
        if (!scheduler.isCurrentThreadInScheduler())
            return false;
        record(1, "Fiber", "exec", "Blocker %s attempting to immediately execute %s", blocker, this);
        if (blocker != getBlocker() || !task.tryUnpark(blocker)) {
            record(1, "Fiber", "exec", "Blocker %s attempt to immediately execute %s FAILED", blocker, this);
            return false;
        }
        this.prePark = prePark;
        immediateExecHelper();
        return true;
    }

    private void immediateExecHelper() {
        this.noPreempt = true;
        task.doExec();
    }

    private StackTraceElement[] execStackTrace(long timeout, TimeUnit unit) {
        if (!tryUnpark(null, timeout, unit))
            return null;
        this.noPreempt = true;
        return execStackTrace1();
    }

    private FiberInfo execFiberInfo(long timeout, TimeUnit unit) {
        if (!tryUnpark(null, timeout, unit))
            return null;
        final State s = this.state;
        this.noPreempt = true;
        final StackTraceElement[] st = execStackTrace1();
        final Object blocker = getBlocker();
        return makeFiberInfo(s, blocker, st);
    }

    private boolean tryUnpark(Object unblocker, long timeout, TimeUnit unit) {
        long start = 0;
        for (int i = 0; ; i++) {
            Object b = getBlocker();
            boolean tu;
            if ((unblocker != null ? b == unblocker : true) && (tu = task.tryUnpark(unblocker)))
                return true;
            if ((start = isTimeoutExpired(i, start, timeout, unit)) < 0)
                return false;
        }
    }

    private long isTimeoutExpired(int iter, long start, long timeout, TimeUnit unit) {
        if (unit != null && timeout == 0)
            return -1;
        if (unit != null && timeout > 0 && iter > (1 << 12)) {
            if (start == 0)
                start = System.nanoTime();
            else if (iter % 100 == 0) {
                if (System.nanoTime() - start > unit.toNanos(timeout))
                    return -1;
            }
        }
        return start;
    }

    Object getUnparker() {
        return task.getUnparker();
    }

    StackTraceElement[] getUnparkStackTrace() {
        return task.getUnparkStackTrace();
    }

    @Override
    public final void unpark() {
        record(1, "Fiber", "unpark", "Unpark %s", this);
        task.unpark();
    }

    @Override
    public final void unpark(Object unblocker) {
        record(1, "Fiber", "unpark", "Unpark %s by %s", this, unblocker);
        task.unpark(unblocker);
    }

    @Override
    @Suspendable
    public final void join() throws ExecutionException, InterruptedException {
        get();
    }

    @Override
    @Suspendable
    public final void join(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        get(timeout, unit);
    }

    public final Fiber<V> joinNoSuspend() throws ExecutionException, InterruptedException {
        task.get();
        return this;
    }

    public final Fiber<V> joinNoSuspend(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        task.get(timeout, unit);
        return this;
    }

    @Override
    @Suspendable
    public final V get() throws ExecutionException, InterruptedException {
        try {
            return future().get();
        } catch (RuntimeExecutionException t) {
            throw new ExecutionException(t.getCause());
        }
    }

    @Override
    @Suspendable
    public final V get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        try {
            return future().get(timeout, unit);
        } catch (RuntimeExecutionException t) {
            throw new ExecutionException(t.getCause());
        }
    }

    @Override
    public final boolean isDone() {
        return isTerminated();
    }

    @Override
    public final boolean cancel(boolean mayInterruptIfRunning) {
        if (casState(State.NEW, State.TERMINATED))
            future().cancel(mayInterruptIfRunning);
        else
            interrupt();
        return !isDone();
    }

    @Override
    public final boolean isCancelled() {
        return future().isCancelled();
    }

    private void sleep1(long timeout, TimeUnit unit) throws InterruptedException, SuspendExecution {
        if (getStackTrace) {
            onResume();
            assert false : "shouldn't get here";
        }
        try {
            for (; ; ) {
                if (interrupted)
                    throw new InterruptedException();
                final long now = System.nanoTime();
                if (sleepStart == 0)
                    this.sleepStart = now;
                final long deadline = sleepStart + unit.toNanos(timeout);
                final long left = deadline - now;
                if (left <= 0) {
                    this.sleepStart = 0;
                    return;
                }
                park1(null, null, left, TimeUnit.NANOSECONDS);
            }
        } catch (SuspendExecution s) {
            throw s;
        } catch (Throwable t) {
            this.sleepStart = 0;
            throw t;
        }
    }

    @Override
    public final void setUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
        this.uncaughtExceptionHandler = eh;
    }

    @Override
    public final UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return uncaughtExceptionHandler;
    }

    public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler() {
        return defaultUncaughtExceptionHandler;
    }

    public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
        Fiber.defaultUncaughtExceptionHandler = eh;
    }

    static void setCurrentStrand(Strand f) {
        currentStrand.set(f);
    }

    static Strand getCurrentStrand() {
        return currentStrand.get();
    }

    Thread getRunningThread() {
        return runningThread;
    }

    @Override
    public final StackTraceElement[] getStackTrace() {
        StackTraceElement[] threadStack = null;
        if (currentFiber() == this)
            threadStack = skipStackTraceElements(Thread.currentThread().getStackTrace(), 1);
        else {
            for (; ; ) {
                if (state == State.TERMINATED || state == State.NEW)
                    break;
                if (state == State.RUNNING) {
                    final long r = run;
                    final Thread t = runningThread;
                    if (t != null)
                        threadStack = t.getStackTrace();
                    if (t != null && state == State.RUNNING && run == r && runningThread == t)
                        break;
                } else {
                    threadStack = execStackTrace(1, TimeUnit.MILLISECONDS);
                    if (threadStack != null) {
                        unpark("getStackTrace");
                        break;
                    }
                }
            }
        }
        return threadToFiberStack(threadStack);
    }

    final FiberInfo getFiberInfo(boolean stack) {
        if (currentFiber() == this)
            return makeFiberInfo(State.RUNNING, null, stack ? skipStackTraceElements(Thread.currentThread().getStackTrace(), 1) : null);
        else {
            for (; ; ) {
                if (state == State.TERMINATED || state == State.NEW)
                    return makeFiberInfo(state, null, null);
                if (state == State.RUNNING) {
                    if (stack) {
                        final long r = run;
                        final Thread t = runningThread;
                        StackTraceElement[] threadStack = null;
                        if (t != null)
                            threadStack = t.getStackTrace();
                        if (state == State.RUNNING && run == r && runningThread == t)
                            return makeFiberInfo(State.RUNNING, null, threadStack);
                    } else
                        return makeFiberInfo(State.RUNNING, null, null);
                } else {
                    if (stack) {
                        FiberInfo fi = execFiberInfo(1, TimeUnit.MILLISECONDS);
                        if (fi != null) {
                            unpark();
                            return fi;
                        }
                    } else {
                        State s;
                        if ((s = state) == State.WAITING || s == State.TIMED_WAITING) {
                            Object blocker = getBlocker();
                            if ((s = state) == State.WAITING || s == State.TIMED_WAITING)
                                return makeFiberInfo(s, blocker, null);
                        }
                    }
                }
            }
        }
    }

    private FiberInfo makeFiberInfo(State state, Object blocker, StackTraceElement[] stackTrace) {
        return new FiberInfo(fid, getName(), state, blocker, threadToFiberStack(stackTrace));
    }

    private static StackTraceElement[] threadToFiberStack(StackTraceElement[] threadStack) {
        if (threadStack == null)
            return null;
        if (threadStack.length == 0)
            return threadStack;
        int count = 0;
        for (StackTraceElement ste : threadStack) {
            count++;
            if (Fiber.class.getName().equals(ste.getClassName())) {
                if ("run".equals(ste.getMethodName()))
                    break;
                if ("run1".equals(ste.getMethodName())) {
                    count--;
                    break;
                }
            }
        }
        StackTraceElement[] fiberStack = new StackTraceElement[count];
        System.arraycopy(threadStack, 0, fiberStack, 0, count);
        return fiberStack;
    }

    public static void dumpStack() {
        verifyCurrent();
        printStackTrace(new Exception("Stack trace"), System.err);
    }

    @SuppressWarnings("CallToThrowablePrintStackTrace")
    private static void printStackTrace(Throwable t, java.io.OutputStream out) {
        t.printStackTrace(new java.io.PrintStream(out) {

            boolean seenExec;

            @Override
            public void println(String x) {
                if (x.startsWith("\tat ")) {
                    if (seenExec)
                        return;
                    if (x.startsWith("\tat " + Fiber.class.getName() + ".exec")) {
                        seenExec = true;
                        return;
                    }
                }
                super.println(x);
            }
        });
    }

    V getResult() {
        if (USE_VAL_FOR_RESULT)
            return null;
        return (V) result;
    }

    @Override
    public final String toString() {
        return "Fiber@" + fid + (name != null ? (':' + name) : "") + "[task: " + task + ", target: " + Objects.systemToString(target) + ", scheduler: " + scheduler + ']';
    }

    final Stack getStack() {
        return stack;
    }

    static interface ParkAction {

        void run(Fiber current);
    }

    private static Fiber verifySuspend() {
        return verifySuspend(verifyCurrent());
    }

    static Fiber verifySuspend(Fiber current) {
        if (verifyInstrumentation)
            checkInstrumentation();
        return current;
    }

    private static Fiber verifyCurrent() {
        Fiber current = currentFiber();
        if (current == null) {
            final Stack stack = Stack.getStack();
            if (stack != null) {
                current = stack.getFiber();
                if (!current.getStackTrace)
                    throw new AssertionError();
                return current;
            }
            throw new IllegalStateException("Not called on a fiber (current strand: " + Strand.currentStrand() + ")");
        }
        return current;
    }

    private static String sourceLineToDesc(int sourceLine) {
        if (sourceLine == -1)
            return "UNKNOWN";
        else
            return Integer.toString(sourceLine);
    }

    private static boolean checkInstrumentation() {
        return checkInstrumentation(ExtendedStackTrace.here());
    }

    @SuppressWarnings("null")
    private static boolean checkInstrumentation(ExtendedStackTrace st) {
        boolean ok = true;
        StringBuilder stackTrace = null;
        final ExtendedStackTraceElement[] stes = st.get();
        for (int i = 0; i < stes.length; i++) {
            final ExtendedStackTraceElement ste = stes[i];
            if (ste.getClassName().equals(Thread.class.getName()) && ste.getMethodName().equals("getStackTrace"))
                continue;
            if (ste.getClassName().equals(ExtendedStackTrace.class.getName()))
                continue;
            if (!ok)
                printTraceLine(stackTrace, ste);
            if (ste.getClassName().contains("$$Lambda$"))
                continue;
            if (!ste.getClassName().equals(Fiber.class.getName()) && !ste.getClassName().startsWith(Fiber.class.getName() + '$') && !ste.getClassName().equals(Stack.class.getName()) && !SuspendableHelper.isWaiver(ste.getClassName(), ste.getMethodName())) {
                final Class<?> clazz = ste.getDeclaringClass();
                boolean classInstrumented = SuspendableHelper.isInstrumented(clazz);
                final Member m = SuspendableHelper.lookupMethod(ste);
                if (m != null) {
                    boolean methodInstrumented = SuspendableHelper.isInstrumented(m);
                    Pair<Boolean, int[]> callSiteInstrumented = SuspendableHelper.isCallSiteInstrumented(m, ste.getLineNumber(), stes, i);
                    if (!classInstrumented || !methodInstrumented || !callSiteInstrumented.getFirst()) {
                        if (ok)
                            stackTrace = initTrace(i, stes);
                        if (!classInstrumented || !methodInstrumented)
                            stackTrace.append(" **");
                        else if (!callSiteInstrumented.getFirst())
                            stackTrace.append(" !! (instrumented suspendable calls at: ").append(callSiteInstrumented.getSecond() == null ? "[]" : Arrays.toString(callSiteInstrumented.getSecond())).append(")");
                        ok = false;
                    }
                } else {
                    if (ok)
                        stackTrace = initTrace(i, stes);
                    stackTrace.append(" **");
                    ok = false;
                }
            } else if (ste.getClassName().equals(Fiber.class.getName()) && ste.getMethodName().equals("run1")) {
                if (!ok) {
                    final String str = "Uninstrumented methods (marked '**') or call-sites (marked '!!') detected on the call stack: " + stackTrace;
                    if (Debug.isUnitTest())
                        throw new VerifyInstrumentationException(str);
                    System.err.println("WARNING: " + str);
                }
                return ok;
            }
        }
        throw new IllegalStateException("Not run through Fiber.exec(). (trace: " + Arrays.toString(stes) + ")");
    }

    private static StringBuilder initTrace(int i, ExtendedStackTraceElement[] stes) {
        final StringBuilder stackTrace = new StringBuilder();
        for (int j = 0; j <= i; j++) {
            final ExtendedStackTraceElement ste2 = stes[j];
            if (ste2.getClassName().equals(Thread.class.getName()) && ste2.getMethodName().equals("getStackTrace"))
                continue;
            printTraceLine(stackTrace, ste2);
        }
        return stackTrace;
    }

    private static void printTraceLine(StringBuilder stackTrace, ExtendedStackTraceElement ste) {
        stackTrace.append("\n\tat ").append(ste);
        final Member m = SuspendableHelper.lookupMethod(ste);
        if (SuspendableHelper.isOptimized(m))
            stackTrace.append(" (optimized)");
    }

    @SuppressWarnings("unchecked")
    private static boolean isInstrumented(Class clazz) {
        boolean res = clazz.isAnnotationPresent(Instrumented.class);
        if (!res)
            res = isInstrumented0(clazz);
        return res;
    }

    private static boolean isInstrumented0(Class clazz) {
        Class superclazz = clazz.getSuperclass();
        if (superclazz != null) {
            if (superclazz.isAnnotationPresent(Instrumented.class)) {
                Method[] ms = clazz.getDeclaredMethods();
                for (Method m : ms) {
                    for (Class et : m.getExceptionTypes()) {
                        if (et.equals(SuspendExecution.class))
                            return false;
                    }
                    if (m.isAnnotationPresent(Suspendable.class))
                        return false;
                }
                return true;
            } else
                return isInstrumented0(superclazz);
        } else
            return false;
    }

    @VisibleForTesting
    void resetState() {
        task.tryUnpark(null);
        assert task.getState() == FiberTask.RUNNABLE;
    }

    @VisibleForTesting
    void reset() {
        stack.resetStack();
    }

    private static final sun.misc.Unsafe UNSAFE = UtilUnsafe.getUnsafe();

    private static final long stateOffset;

    static {
        try {
            stateOffset = UNSAFE.objectFieldOffset(Fiber.class.getDeclaredField("state"));
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }

    private boolean casState(State expected, State update) {
        return UNSAFE.compareAndSwapObject(this, stateOffset, expected, update);
    }

    private void orderedSetState(State value) {
        UNSAFE.putOrderedObject(this, stateOffset, value);
    }

    protected final boolean isRecordingLevel(int level) {
        if (!Debug.isDebug())
            return false;
        final FlightRecorder.ThreadRecorder recorder = flightRecorder != null ? flightRecorder.get() : null;
        if (recorder == null)
            return false;
        return recorder.recordsLevel(level);
    }

    protected final void record(int level, String clazz, String method, String format) {
        if (flightRecorder != null)
            record(flightRecorder.get(), level, clazz, method, format);
    }

    protected final void record(int level, String clazz, String method, String format, Object arg1) {
        if (flightRecorder != null)
            record(flightRecorder.get(), level, clazz, method, format, arg1);
    }

    protected final void record(int level, String clazz, String method, String format, Object arg1, Object arg2) {
        if (flightRecorder != null)
            record(flightRecorder.get(), level, clazz, method, format, arg1, arg2);
    }

    protected final void record(int level, String clazz, String method, String format, Object arg1, Object arg2, Object arg3) {
        if (flightRecorder != null)
            record(flightRecorder.get(), level, clazz, method, format, arg1, arg2, arg3);
    }

    protected final void record(int level, String clazz, String method, String format, Object arg1, Object arg2, Object arg3, Object arg4) {
        if (flightRecorder != null)
            record(flightRecorder.get(), level, clazz, method, format, arg1, arg2, arg3, arg4);
    }

    protected final void record(int level, String clazz, String method, String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        if (flightRecorder != null)
            record(flightRecorder.get(), level, clazz, method, format, arg1, arg2, arg3, arg4, arg5);
    }

    protected final void record(int level, String clazz, String method, String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
        if (flightRecorder != null)
            record(flightRecorder.get(), level, clazz, method, format, arg1, arg2, arg3, arg4, arg5, arg6);
    }

    protected final void record(int level, String clazz, String method, String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
        if (flightRecorder != null)
            record(flightRecorder.get(), level, clazz, method, format, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
    }

    protected final void record(int level, String clazz, String method, String format, Object... args) {
        if (flightRecorder != null)
            record(flightRecorder.get(), level, clazz, method, format, args);
    }

    private static void record(FlightRecorder.ThreadRecorder recorder, int level, String clazz, String method, String format) {
        if (recorder != null)
            recorder.record(level, makeFlightRecorderMessage(recorder, clazz, method, format, null));
    }

    private static void record(FlightRecorder.ThreadRecorder recorder, int level, String clazz, String method, String format, Object arg1) {
        if (recorder != null)
            recorder.record(level, makeFlightRecorderMessage(recorder, clazz, method, format, new Object[] { arg1 }));
    }

    private static void record(FlightRecorder.ThreadRecorder recorder, int level, String clazz, String method, String format, Object arg1, Object arg2) {
        if (recorder != null)
            recorder.record(level, makeFlightRecorderMessage(recorder, clazz, method, format, new Object[] { arg1, arg2 }));
    }

    private static void record(FlightRecorder.ThreadRecorder recorder, int level, String clazz, String method, String format, Object arg1, Object arg2, Object arg3) {
        if (recorder != null)
            recorder.record(level, makeFlightRecorderMessage(recorder, clazz, method, format, new Object[] { arg1, arg2, arg3 }));
    }

    private static void record(FlightRecorder.ThreadRecorder recorder, int level, String clazz, String method, String format, Object arg1, Object arg2, Object arg3, Object arg4) {
        if (recorder != null)
            recorder.record(level, makeFlightRecorderMessage(recorder, clazz, method, format, new Object[] { arg1, arg2, arg3, arg4 }));
    }

    private static void record(FlightRecorder.ThreadRecorder recorder, int level, String clazz, String method, String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        if (recorder != null)
            recorder.record(level, makeFlightRecorderMessage(recorder, clazz, method, format, new Object[] { arg1, arg2, arg3, arg4, arg5 }));
    }

    private static void record(FlightRecorder.ThreadRecorder recorder, int level, String clazz, String method, String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
        if (recorder != null)
            recorder.record(level, makeFlightRecorderMessage(recorder, clazz, method, format, new Object[] { arg1, arg2, arg3, arg4, arg5, arg6 }));
    }

    private static void record(FlightRecorder.ThreadRecorder recorder, int level, String clazz, String method, String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
        if (recorder != null)
            recorder.record(level, makeFlightRecorderMessage(recorder, clazz, method, format, new Object[] { arg1, arg2, arg3, arg4, arg5, arg6, arg7 }));
    }

    private static void record(FlightRecorder.ThreadRecorder recorder, int level, String clazz, String method, String format, Object... args) {
        if (recorder != null)
            recorder.record(level, makeFlightRecorderMessage(recorder, clazz, method, format, args));
    }

    private static FlightRecorderMessage makeFlightRecorderMessage(FlightRecorder.ThreadRecorder recorder, String clazz, String method, String format, Object[] args) {
        return new FlightRecorderMessage(clazz, method, format, args);
    }

    private static StackTraceElement[] skipStackTraceElements(StackTraceElement[] st, int skip) {
        if (skip >= st.length)
            return st;
        final StackTraceElement[] st1 = new StackTraceElement[st.length - skip];
        System.arraycopy(st, skip, st1, 0, st1.length);
        return st1;
    }

    @SuppressWarnings("empty-statement")
    public static void parkAndSerialize(final FiberWriter writer) throws SuspendExecution {
        while (!park(SERIALIZER_BLOCKER, new ParkAction() {

            @Override
            public void run(Fiber f) {
                f.record(1, "Fiber", "parkAndSerialize", "Serializing fiber %s", f);
                writer.write(f, getFiberSerializer());
            }
        })) ;
    }

    public static <V> Fiber<V> unparkSerialized(byte[] serFiber, FiberScheduler scheduler) {
        final Fiber<V> f = (Fiber<V>) getFiberSerializer().read(serFiber);
        return unparkDeserialized(f, scheduler);
    }

    public static <V> Fiber<V> unparkDeserialized(Fiber<V> f, FiberScheduler scheduler) {
        f.record(1, "Fiber", "unparkDeserialized", "Deserialized fiber %s", f);
        final Thread currentThread = Thread.currentThread();
        f.fiberRef = new DummyRunnable(f);
        f.fid = nextFiberId();
        f.scheduler = scheduler;
        f.task = scheduler.newFiberTask(f);
        f.task.setState(FiberTask.PARKED);
        if (USE_VAL_FOR_RESULT)
            f.result = new Val<V>();
        f.contextClassLoader = ThreadAccess.getContextClassLoader(currentThread);
        if (MAINTAIN_ACCESS_CONTROL_CONTEXT)
            f.inheritedAccessControlContext = AccessController.getContext();
        f.record(1, "Fiber", "unparkDeserialized", "Unparking deserialized fiber %s", f);
        f.unpark(SERIALIZER_BLOCKER);
        return f;
    }

    public static ByteArraySerializer getFiberSerializer() {
        return getFiberSerializer(true);
    }

    public static ByteArraySerializer getFiberSerializer(boolean includeThreadLocals) {
        final KryoSerializer s = new KryoSerializer();
        s.getKryo().addDefaultSerializer(Fiber.class, new FiberSerializer(includeThreadLocals));
        s.getKryo().addDefaultSerializer(ThreadLocal.class, new ThreadLocalSerializer());
        s.getKryo().addDefaultSerializer(FiberWriter.class, new FiberWriterSerializer());
        s.getKryo().register(Fiber.class);
        s.getKryo().register(ThreadLocal.class);
        s.getKryo().register(InheritableThreadLocal.class);
        s.getKryo().register(ThreadLocalSerializer.DEFAULT.class);
        s.getKryo().register(FiberWriter.class);
        return s;
    }

    private static class FiberSerializer extends Serializer<Fiber> {

        private boolean includeThreadLocals;

        public FiberSerializer(boolean includeThreadLocals) {
            this.includeThreadLocals = includeThreadLocals;
            setImmutable(true);
        }

        @Override
        @SuppressWarnings({ "CallToPrintStackTrace", "unchecked" })
        public void write(Kryo kryo, Output output, Fiber f) {
            final Thread currentThread = Thread.currentThread();
            if (!includeThreadLocals) {
                Object tmpFiberLocals = f.fiberLocals;
                Object tmpInheritableFiberLocals = f.inheritableFiberLocals;
                f.fiberLocals = null;
                f.inheritableFiberLocals = null;
                try {
                    f.stack.resumeStack();
                    kryo.writeClass(output, f.getClass());
                    new FieldSerializer(kryo, f.getClass()).write(kryo, output, f);
                } finally {
                    f.fiberLocals = tmpFiberLocals;
                    f.inheritableFiberLocals = tmpInheritableFiberLocals;
                }
            } else {
                final Object tmpThreadLocals = ThreadAccess.getThreadLocals(currentThread);
                final Object tmpInheritableThreadLocals = ThreadAccess.getInheritableThreadLocals(currentThread);
                ThreadAccess.setThreadLocals(currentThread, f.fiberLocals);
                ThreadAccess.setInheritableThreadLocals(currentThread, f.inheritableFiberLocals);
                Object realFiberLocals = f.fiberLocals;
                Object realInheritableFiberLocals = f.inheritableFiberLocals;
                try {
                    f.fiberLocals = realFiberLocals != null ? ThreadAccess.toMap(realFiberLocals).keySet().toArray() : null;
                    f.inheritableFiberLocals = realInheritableFiberLocals != null ? ThreadAccess.toMap(realInheritableFiberLocals).keySet().toArray() : null;
                    f.stack.resumeStack();
                    kryo.writeClass(output, f.getClass());
                    new FieldSerializer(kryo, f.getClass()).write(kryo, output, f);
                } catch (Throwable t) {
                    t.printStackTrace();
                    throw t;
                } finally {
                    f.fiberLocals = realFiberLocals;
                    f.inheritableFiberLocals = realInheritableFiberLocals;
                    ThreadAccess.setThreadLocals(currentThread, tmpThreadLocals);
                    ThreadAccess.setInheritableThreadLocals(currentThread, tmpInheritableThreadLocals);
                }
            }
        }

        @Override
        @SuppressWarnings("CallToPrintStackTrace")
        public Fiber read(Kryo kryo, Input input, Class<Fiber> type) {
            final Fiber f;
            final Thread currentThread = Thread.currentThread();
            final Object tmpThreadLocals = ThreadAccess.getThreadLocals(currentThread);
            final Object tmpInheritableThreadLocals = ThreadAccess.getInheritableThreadLocals(currentThread);
            ThreadAccess.setThreadLocals(currentThread, null);
            ThreadAccess.setInheritableThreadLocals(currentThread, null);
            try {
                final Registration reg = kryo.readClass(input);
                if (reg == null)
                    return null;
                f = (Fiber) new FieldSerializer(kryo, reg.getType()).read(kryo, input, reg.getType());
                f.fiberLocals = ThreadAccess.getThreadLocals(currentThread);
                f.inheritableFiberLocals = ThreadAccess.getInheritableThreadLocals(currentThread);
                return f;
            } catch (Throwable t) {
                t.printStackTrace();
                throw t;
            } finally {
                ThreadAccess.setThreadLocals(currentThread, tmpThreadLocals);
                ThreadAccess.setInheritableThreadLocals(currentThread, tmpInheritableThreadLocals);
            }
        }
    }
}
