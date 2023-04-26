package java.util.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

public class ForkJoinPool extends AbstractExecutorService {

    private static void checkPermission() {
        SecurityManager security = System.getSecurityManager();
        if (security != null)
            security.checkPermission(modifyThreadPermission);
    }

    public static interface ForkJoinWorkerThreadFactory {

        public ForkJoinWorkerThread newThread(ForkJoinPool pool);
    }

    static final class DefaultForkJoinWorkerThreadFactory implements ForkJoinWorkerThreadFactory {

        public final ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            return new ForkJoinWorkerThread(pool);
        }
    }

    static final class EmptyTask extends ForkJoinTask<Void> {

        private static final long serialVersionUID = -7721805057305804111L;

        EmptyTask() {
            status = ForkJoinTask.NORMAL;
        }

        public final Void getRawResult() {
            return null;
        }

        public final void setRawResult(Void x) {
        }

        public final boolean exec() {
            return true;
        }
    }

    static final class WorkQueue {

        static final int INITIAL_QUEUE_CAPACITY = 1 << 13;

        static final int MAXIMUM_QUEUE_CAPACITY = 1 << 26;

        volatile long pad00, pad01, pad02, pad03, pad04, pad05, pad06;

        int seed;

        volatile int eventCount;

        int nextWait;

        int hint;

        int poolIndex;

        final int mode;

        int nsteals;

        volatile int qlock;

        volatile int base;

        int top;

        ForkJoinTask<?>[] array;

        final ForkJoinPool pool;

        final ForkJoinWorkerThread owner;

        volatile Thread parker;

        volatile ForkJoinTask<?> currentJoin;

        ForkJoinTask<?> currentSteal;

        volatile Object pad10, pad11, pad12, pad13, pad14, pad15, pad16, pad17;

        volatile Object pad18, pad19, pad1a, pad1b, pad1c, pad1d;

        WorkQueue(ForkJoinPool pool, ForkJoinWorkerThread owner, int mode, int seed) {
            this.pool = pool;
            this.owner = owner;
            this.mode = mode;
            this.seed = seed;
            base = top = INITIAL_QUEUE_CAPACITY >>> 1;
        }

        final int queueSize() {
            int n = base - top;
            return (n >= 0) ? 0 : -n;
        }

        final boolean isEmpty() {
            ForkJoinTask<?>[] a;
            int m, s;
            int n = base - (s = top);
            return (n >= 0 || (n == -1 && ((a = array) == null || (m = a.length - 1) < 0 || U.getObject(a, (long) ((m & (s - 1)) << ASHIFT) + ABASE) == null)));
        }

        final void push(ForkJoinTask<?> task) {
            ForkJoinTask<?>[] a;
            ForkJoinPool p;
            int s = top, m, n;
            if ((a = array) != null) {
                int j = (((m = a.length - 1) & s) << ASHIFT) + ABASE;
                U.putOrderedObject(a, j, task);
                if ((n = (top = s + 1) - base) <= 2) {
                    if ((p = pool) != null)
                        p.signalWork(this);
                } else if (n >= m)
                    growArray();
            }
        }

        final ForkJoinTask<?>[] growArray() {
            ForkJoinTask<?>[] oldA = array;
            int size = oldA != null ? oldA.length << 1 : INITIAL_QUEUE_CAPACITY;
            if (size > MAXIMUM_QUEUE_CAPACITY)
                throw new RejectedExecutionException("Queue capacity exceeded");
            int oldMask, t, b;
            ForkJoinTask<?>[] a = array = new ForkJoinTask<?>[size];
            if (oldA != null && (oldMask = oldA.length - 1) >= 0 && (t = top) - (b = base) > 0) {
                int mask = size - 1;
                do {
                    ForkJoinTask<?> x;
                    int oldj = ((b & oldMask) << ASHIFT) + ABASE;
                    int j = ((b & mask) << ASHIFT) + ABASE;
                    x = (ForkJoinTask<?>) U.getObjectVolatile(oldA, oldj);
                    if (x != null && U.compareAndSwapObject(oldA, oldj, x, null))
                        U.putObjectVolatile(a, j, x);
                } while (++b != t);
            }
            return a;
        }

        final ForkJoinTask<?> pop() {
            ForkJoinTask<?>[] a;
            ForkJoinTask<?> t;
            int m;
            if ((a = array) != null && (m = a.length - 1) >= 0) {
                for (int s; (s = top - 1) - base >= 0; ) {
                    long j = ((m & s) << ASHIFT) + ABASE;
                    if ((t = (ForkJoinTask<?>) U.getObject(a, j)) == null)
                        break;
                    if (U.compareAndSwapObject(a, j, t, null)) {
                        top = s;
                        return t;
                    }
                }
            }
            return null;
        }

        final ForkJoinTask<?> pollAt(int b) {
            ForkJoinTask<?> t;
            ForkJoinTask<?>[] a;
            if ((a = array) != null) {
                int j = (((a.length - 1) & b) << ASHIFT) + ABASE;
                if ((t = (ForkJoinTask<?>) U.getObjectVolatile(a, j)) != null && base == b && U.compareAndSwapObject(a, j, t, null)) {
                    base = b + 1;
                    return t;
                }
            }
            return null;
        }

        final ForkJoinTask<?> poll() {
            ForkJoinTask<?>[] a;
            int b;
            ForkJoinTask<?> t;
            while ((b = base) - top < 0 && (a = array) != null) {
                int j = (((a.length - 1) & b) << ASHIFT) + ABASE;
                t = (ForkJoinTask<?>) U.getObjectVolatile(a, j);
                if (t != null) {
                    if (base == b && U.compareAndSwapObject(a, j, t, null)) {
                        base = b + 1;
                        return t;
                    }
                } else if (base == b) {
                    if (b + 1 == top)
                        break;
                    Thread.yield();
                }
            }
            return null;
        }

        final ForkJoinTask<?> nextLocalTask() {
            return mode == 0 ? pop() : poll();
        }

        final ForkJoinTask<?> peek() {
            ForkJoinTask<?>[] a = array;
            int m;
            if (a == null || (m = a.length - 1) < 0)
                return null;
            int i = mode == 0 ? top - 1 : base;
            int j = ((i & m) << ASHIFT) + ABASE;
            return (ForkJoinTask<?>) U.getObjectVolatile(a, j);
        }

        final boolean tryUnpush(ForkJoinTask<?> t) {
            ForkJoinTask<?>[] a;
            int s;
            if ((a = array) != null && (s = top) != base && U.compareAndSwapObject(a, (((a.length - 1) & --s) << ASHIFT) + ABASE, t, null)) {
                top = s;
                return true;
            }
            return false;
        }

        final void cancelAll() {
            ForkJoinTask.cancelIgnoringExceptions(currentJoin);
            ForkJoinTask.cancelIgnoringExceptions(currentSteal);
            for (ForkJoinTask<?> t; (t = poll()) != null; ) ForkJoinTask.cancelIgnoringExceptions(t);
        }

        final int nextSeed() {
            int r = seed;
            r ^= r << 13;
            r ^= r >>> 17;
            return seed = r ^= r << 5;
        }

        private void popAndExecAll() {
            ForkJoinTask<?>[] a;
            int m, s;
            long j;
            ForkJoinTask<?> t;
            while ((a = array) != null && (m = a.length - 1) >= 0 && (s = top - 1) - base >= 0 && (t = ((ForkJoinTask<?>) U.getObject(a, j = ((m & s) << ASHIFT) + ABASE))) != null) {
                if (U.compareAndSwapObject(a, j, t, null)) {
                    top = s;
                    t.doExec();
                }
            }
        }

        private void pollAndExecAll() {
            for (ForkJoinTask<?> t; (t = poll()) != null; ) t.doExec();
        }

        final boolean tryRemoveAndExec(ForkJoinTask<?> task) {
            boolean stat = true, removed = false, empty = true;
            ForkJoinTask<?>[] a;
            int m, s, b, n;
            if ((a = array) != null && (m = a.length - 1) >= 0 && (n = (s = top) - (b = base)) > 0) {
                for (ForkJoinTask<?> t; ; ) {
                    int j = ((--s & m) << ASHIFT) + ABASE;
                    t = (ForkJoinTask<?>) U.getObjectVolatile(a, j);
                    if (t == null)
                        break;
                    else if (t == task) {
                        if (s + 1 == top) {
                            if (!U.compareAndSwapObject(a, j, task, null))
                                break;
                            top = s;
                            removed = true;
                        } else if (base == b)
                            removed = U.compareAndSwapObject(a, j, task, new EmptyTask());
                        break;
                    } else if (t.status >= 0)
                        empty = false;
                    else if (s + 1 == top) {
                        if (U.compareAndSwapObject(a, j, t, null))
                            top = s;
                        break;
                    }
                    if (--n == 0) {
                        if (!empty && base == b)
                            stat = false;
                        break;
                    }
                }
            }
            if (removed)
                task.doExec();
            return stat;
        }

        final boolean pollAndExecCC(ForkJoinTask<?> root) {
            ForkJoinTask<?>[] a;
            int b;
            Object o;
            outer: while ((b = base) - top < 0 && (a = array) != null) {
                long j = (((a.length - 1) & b) << ASHIFT) + ABASE;
                if ((o = U.getObject(a, j)) == null || !(o instanceof CountedCompleter))
                    break;
                for (CountedCompleter<?> t = (CountedCompleter<?>) o, r = t; ; ) {
                    if (r == root) {
                        if (base == b && U.compareAndSwapObject(a, j, t, null)) {
                            base = b + 1;
                            t.doExec();
                            return true;
                        } else
                            break;
                    }
                    if ((r = r.completer) == null)
                        break outer;
                }
            }
            return false;
        }

        final void runTask(ForkJoinTask<?> t) {
            if (t != null) {
                (currentSteal = t).doExec();
                currentSteal = null;
                ++nsteals;
                if (base - top < 0) {
                    if (mode == 0)
                        popAndExecAll();
                    else
                        pollAndExecAll();
                }
            }
        }

        final void runSubtask(ForkJoinTask<?> t) {
            if (t != null) {
                ForkJoinTask<?> ps = currentSteal;
                (currentSteal = t).doExec();
                currentSteal = ps;
            }
        }

        final boolean isApparentlyUnblocked() {
            Thread wt;
            Thread.State s;
            return (eventCount >= 0 && (wt = owner) != null && (s = wt.getState()) != Thread.State.BLOCKED && s != Thread.State.WAITING && s != Thread.State.TIMED_WAITING);
        }

        private static final sun.misc.Unsafe U;

        private static final long QLOCK;

        private static final int ABASE;

        private static final int ASHIFT;

        static {
            try {
                U = sun.misc.Unsafe.getUnsafe();
                Class<?> k = WorkQueue.class;
                Class<?> ak = ForkJoinTask[].class;
                QLOCK = U.objectFieldOffset(k.getDeclaredField("qlock"));
                ABASE = U.arrayBaseOffset(ak);
                int scale = U.arrayIndexScale(ak);
                if ((scale & (scale - 1)) != 0)
                    throw new Error("data type scale not a power of two");
                ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    public static final ForkJoinWorkerThreadFactory defaultForkJoinWorkerThreadFactory;

    private static final RuntimePermission modifyThreadPermission;

    static final ForkJoinPool common;

    static final int commonParallelism;

    private static int poolNumberSequence;

    private static final synchronized int nextPoolId() {
        return ++poolNumberSequence;
    }

    private static final long IDLE_TIMEOUT = 2000L * 1000L * 1000L;

    private static final long FAST_IDLE_TIMEOUT = 200L * 1000L * 1000L;

    private static final long TIMEOUT_SLOP = 2000000L;

    private static final int MAX_HELP = 64;

    private static final int SEED_INCREMENT = 0x61c88647;

    private static final int AC_SHIFT = 48;

    private static final int TC_SHIFT = 32;

    private static final int ST_SHIFT = 31;

    private static final int EC_SHIFT = 16;

    private static final int SMASK = 0xffff;

    private static final int MAX_CAP = 0x7fff;

    private static final int EVENMASK = 0xfffe;

    private static final int SQMASK = 0x007e;

    private static final int SHORT_SIGN = 1 << 15;

    private static final int INT_SIGN = 1 << 31;

    private static final long STOP_BIT = 0x0001L << ST_SHIFT;

    private static final long AC_MASK = ((long) SMASK) << AC_SHIFT;

    private static final long TC_MASK = ((long) SMASK) << TC_SHIFT;

    private static final long TC_UNIT = 1L << TC_SHIFT;

    private static final long AC_UNIT = 1L << AC_SHIFT;

    private static final int UAC_SHIFT = AC_SHIFT - 32;

    private static final int UTC_SHIFT = TC_SHIFT - 32;

    private static final int UAC_MASK = SMASK << UAC_SHIFT;

    private static final int UTC_MASK = SMASK << UTC_SHIFT;

    private static final int UAC_UNIT = 1 << UAC_SHIFT;

    private static final int UTC_UNIT = 1 << UTC_SHIFT;

    private static final int E_MASK = 0x7fffffff;

    private static final int E_SEQ = 1 << EC_SHIFT;

    private static final int SHUTDOWN = 1 << 31;

    private static final int PL_LOCK = 2;

    private static final int PL_SIGNAL = 1;

    private static final int PL_SPINS = 1 << 8;

    static final int LIFO_QUEUE = 0;

    static final int FIFO_QUEUE = 1;

    static final int SHARED_QUEUE = -1;

    private static final int MIN_SCAN = 0x1ff;

    private static final int MAX_SCAN = 0x1ffff;

    volatile long pad00, pad01, pad02, pad03, pad04, pad05, pad06;

    volatile long stealCount;

    volatile long ctl;

    volatile int plock;

    volatile int indexSeed;

    final int config;

    WorkQueue[] workQueues;

    final ForkJoinWorkerThreadFactory factory;

    final UncaughtExceptionHandler ueh;

    final String workerNamePrefix;

    volatile Object pad10, pad11, pad12, pad13, pad14, pad15, pad16, pad17;

    volatile Object pad18, pad19, pad1a, pad1b;

    private int acquirePlock() {
        int spins = PL_SPINS, ps, nps;
        for (; ; ) {
            if (((ps = plock) & PL_LOCK) == 0 && U.compareAndSwapInt(this, PLOCK, ps, nps = ps + PL_LOCK))
                return nps;
            else if (spins >= 0) {
                if (ThreadLocalRandom.nextSecondarySeed() >= 0)
                    --spins;
            } else if (U.compareAndSwapInt(this, PLOCK, ps, ps | PL_SIGNAL)) {
                synchronized (this) {
                    if ((plock & PL_SIGNAL) != 0) {
                        try {
                            wait();
                        } catch (InterruptedException ie) {
                            try {
                                Thread.currentThread().interrupt();
                            } catch (SecurityException ignore) {
                            }
                        }
                    } else
                        notifyAll();
                }
            }
        }
    }

    private void releasePlock(int ps) {
        plock = ps;
        synchronized (this) {
            notifyAll();
        }
    }

    private void tryAddWorker() {
        long c;
        int u;
        while ((u = (int) ((c = ctl) >>> 32)) < 0 && (u & SHORT_SIGN) != 0 && (int) c == 0) {
            long nc = (long) (((u + UTC_UNIT) & UTC_MASK) | ((u + UAC_UNIT) & UAC_MASK)) << 32;
            if (U.compareAndSwapLong(this, CTL, c, nc)) {
                ForkJoinWorkerThreadFactory fac;
                Throwable ex = null;
                ForkJoinWorkerThread wt = null;
                try {
                    if ((fac = factory) != null && (wt = fac.newThread(this)) != null) {
                        wt.start();
                        break;
                    }
                } catch (Throwable e) {
                    ex = e;
                }
                deregisterWorker(wt, ex);
                break;
            }
        }
    }

    final WorkQueue registerWorker(ForkJoinWorkerThread wt) {
        UncaughtExceptionHandler handler;
        WorkQueue[] ws;
        int s, ps;
        wt.setDaemon(true);
        if ((handler = ueh) != null)
            wt.setUncaughtExceptionHandler(handler);
        do {
        } while (!U.compareAndSwapInt(this, INDEXSEED, s = indexSeed, s += SEED_INCREMENT) || s == 0);
        WorkQueue w = new WorkQueue(this, wt, config >>> 16, s);
        if (((ps = plock) & PL_LOCK) != 0 || !U.compareAndSwapInt(this, PLOCK, ps, ps += PL_LOCK))
            ps = acquirePlock();
        int nps = (ps & SHUTDOWN) | ((ps + PL_LOCK) & ~SHUTDOWN);
        try {
            if ((ws = workQueues) != null) {
                int n = ws.length, m = n - 1;
                int r = (s << 1) | 1;
                if (ws[r &= m] != null) {
                    int probes = 0;
                    int step = (n <= 4) ? 2 : ((n >>> 1) & EVENMASK) + 2;
                    while (ws[r = (r + step) & m] != null) {
                        if (++probes >= n) {
                            workQueues = ws = Arrays.copyOf(ws, n <<= 1);
                            m = n - 1;
                            probes = 0;
                        }
                    }
                }
                w.eventCount = w.poolIndex = r;
                ws[r] = w;
            }
        } finally {
            if (!U.compareAndSwapInt(this, PLOCK, ps, nps))
                releasePlock(nps);
        }
        wt.setName(workerNamePrefix.concat(Integer.toString(w.poolIndex)));
        return w;
    }

    final void deregisterWorker(ForkJoinWorkerThread wt, Throwable ex) {
        WorkQueue w = null;
        if (wt != null && (w = wt.workQueue) != null) {
            int ps;
            w.qlock = -1;
            long ns = w.nsteals, sc;
            do {
            } while (!U.compareAndSwapLong(this, STEALCOUNT, sc = stealCount, sc + ns));
            if (((ps = plock) & PL_LOCK) != 0 || !U.compareAndSwapInt(this, PLOCK, ps, ps += PL_LOCK))
                ps = acquirePlock();
            int nps = (ps & SHUTDOWN) | ((ps + PL_LOCK) & ~SHUTDOWN);
            try {
                int idx = w.poolIndex;
                WorkQueue[] ws = workQueues;
                if (ws != null && idx >= 0 && idx < ws.length && ws[idx] == w)
                    ws[idx] = null;
            } finally {
                if (!U.compareAndSwapInt(this, PLOCK, ps, nps))
                    releasePlock(nps);
            }
        }
        long c;
        do {
        } while (!U.compareAndSwapLong(this, CTL, c = ctl, (((c - AC_UNIT) & AC_MASK) | ((c - TC_UNIT) & TC_MASK) | (c & ~(AC_MASK | TC_MASK)))));
        if (!tryTerminate(false, false) && w != null && w.array != null) {
            w.cancelAll();
            WorkQueue[] ws;
            WorkQueue v;
            Thread p;
            int u, i, e;
            while ((u = (int) ((c = ctl) >>> 32)) < 0 && (e = (int) c) >= 0) {
                if (e > 0) {
                    if ((ws = workQueues) == null || (i = e & SMASK) >= ws.length || (v = ws[i]) == null)
                        break;
                    long nc = (((long) (v.nextWait & E_MASK)) | ((long) (u + UAC_UNIT) << 32));
                    if (v.eventCount != (e | INT_SIGN))
                        break;
                    if (U.compareAndSwapLong(this, CTL, c, nc)) {
                        v.eventCount = (e + E_SEQ) & E_MASK;
                        if ((p = v.parker) != null)
                            U.unpark(p);
                        break;
                    }
                } else {
                    if ((short) u < 0)
                        tryAddWorker();
                    break;
                }
            }
        }
        if (ex == null)
            ForkJoinTask.helpExpungeStaleExceptions();
        else
            ForkJoinTask.rethrow(ex);
    }

    final void externalPush(ForkJoinTask<?> task) {
        WorkQueue[] ws;
        WorkQueue q;
        int z, m;
        ForkJoinTask<?>[] a;
        if ((z = ThreadLocalRandom.getProbe()) != 0 && plock > 0 && (ws = workQueues) != null && (m = (ws.length - 1)) >= 0 && (q = ws[m & z & SQMASK]) != null && U.compareAndSwapInt(q, QLOCK, 0, 1)) {
            int b = q.base, s = q.top, n, an;
            if ((a = q.array) != null && (an = a.length) > (n = s + 1 - b)) {
                int j = (((an - 1) & s) << ASHIFT) + ABASE;
                U.putOrderedObject(a, j, task);
                q.top = s + 1;
                q.qlock = 0;
                if (n <= 2)
                    signalWork(q);
                return;
            }
            q.qlock = 0;
        }
        fullExternalPush(task);
    }

    private void fullExternalPush(ForkJoinTask<?> task) {
        int r;
        if ((r = ThreadLocalRandom.getProbe()) == 0) {
            ThreadLocalRandom.localInit();
            r = ThreadLocalRandom.getProbe();
        }
        for (; ; ) {
            WorkQueue[] ws;
            WorkQueue q;
            int ps, m, k;
            boolean move = false;
            if ((ps = plock) < 0)
                throw new RejectedExecutionException();
            else if (ps == 0 || (ws = workQueues) == null || (m = ws.length - 1) < 0) {
                int p = config & SMASK;
                int n = (p > 1) ? p - 1 : 1;
                n |= n >>> 1;
                n |= n >>> 2;
                n |= n >>> 4;
                n |= n >>> 8;
                n |= n >>> 16;
                n = (n + 1) << 1;
                WorkQueue[] nws = ((ws = workQueues) == null || ws.length == 0 ? new WorkQueue[n] : null);
                if (((ps = plock) & PL_LOCK) != 0 || !U.compareAndSwapInt(this, PLOCK, ps, ps += PL_LOCK))
                    ps = acquirePlock();
                if (((ws = workQueues) == null || ws.length == 0) && nws != null)
                    workQueues = nws;
                int nps = (ps & SHUTDOWN) | ((ps + PL_LOCK) & ~SHUTDOWN);
                if (!U.compareAndSwapInt(this, PLOCK, ps, nps))
                    releasePlock(nps);
            } else if ((q = ws[k = r & m & SQMASK]) != null) {
                if (q.qlock == 0 && U.compareAndSwapInt(q, QLOCK, 0, 1)) {
                    ForkJoinTask<?>[] a = q.array;
                    int s = q.top;
                    boolean submitted = false;
                    try {
                        if ((a != null && a.length > s + 1 - q.base) || (a = q.growArray()) != null) {
                            int j = (((a.length - 1) & s) << ASHIFT) + ABASE;
                            U.putOrderedObject(a, j, task);
                            q.top = s + 1;
                            submitted = true;
                        }
                    } finally {
                        q.qlock = 0;
                    }
                    if (submitted) {
                        signalWork(q);
                        return;
                    }
                }
                move = true;
            } else if (((ps = plock) & PL_LOCK) == 0) {
                q = new WorkQueue(this, null, SHARED_QUEUE, r);
                if (((ps = plock) & PL_LOCK) != 0 || !U.compareAndSwapInt(this, PLOCK, ps, ps += PL_LOCK))
                    ps = acquirePlock();
                if ((ws = workQueues) != null && k < ws.length && ws[k] == null)
                    ws[k] = q;
                int nps = (ps & SHUTDOWN) | ((ps + PL_LOCK) & ~SHUTDOWN);
                if (!U.compareAndSwapInt(this, PLOCK, ps, nps))
                    releasePlock(nps);
            } else
                move = true;
            if (move)
                r = ThreadLocalRandom.advanceProbe(r);
        }
    }

    final void incrementActiveCount() {
        long c;
        do {
        } while (!U.compareAndSwapLong(this, CTL, c = ctl, c + AC_UNIT));
    }

    final void signalWork(WorkQueue q) {
        int hint = q.poolIndex;
        long c;
        int e, u, i, n;
        WorkQueue[] ws;
        WorkQueue w;
        Thread p;
        while ((u = (int) ((c = ctl) >>> 32)) < 0) {
            if ((e = (int) c) > 0) {
                if ((ws = workQueues) != null && ws.length > (i = e & SMASK) && (w = ws[i]) != null && w.eventCount == (e | INT_SIGN)) {
                    long nc = (((long) (w.nextWait & E_MASK)) | ((long) (u + UAC_UNIT) << 32));
                    if (U.compareAndSwapLong(this, CTL, c, nc)) {
                        w.hint = hint;
                        w.eventCount = (e + E_SEQ) & E_MASK;
                        if ((p = w.parker) != null)
                            U.unpark(p);
                        break;
                    }
                    if (q.top - q.base <= 0)
                        break;
                } else
                    break;
            } else {
                if ((short) u < 0)
                    tryAddWorker();
                break;
            }
        }
    }

    final void runWorker(WorkQueue w) {
        w.growArray();
        do {
            w.runTask(scan(w));
        } while (w.qlock >= 0);
    }

    private final ForkJoinTask<?> scan(WorkQueue w) {
        WorkQueue[] ws;
        int m;
        int ps = plock;
        if (w != null && (ws = workQueues) != null && (m = ws.length - 1) >= 0) {
            int ec = w.eventCount;
            int r = w.seed;
            r ^= r << 13;
            r ^= r >>> 17;
            w.seed = r ^= r << 5;
            w.hint = -1;
            int j = ((m + m + 1) | MIN_SCAN) & MAX_SCAN;
            do {
                WorkQueue q;
                ForkJoinTask<?>[] a;
                int b;
                if ((q = ws[(r + j) & m]) != null && (b = q.base) - q.top < 0 && (a = q.array) != null) {
                    int i = (((a.length - 1) & b) << ASHIFT) + ABASE;
                    ForkJoinTask<?> t = (ForkJoinTask<?>) U.getObjectVolatile(a, i);
                    if (q.base == b && ec >= 0 && t != null && U.compareAndSwapObject(a, i, t, null)) {
                        if ((q.base = b + 1) - q.top < 0)
                            signalWork(q);
                        return t;
                    } else if ((ec < 0 || j < m) && (int) (ctl >> AC_SHIFT) <= 0) {
                        w.hint = (r + j) & m;
                        break;
                    }
                }
            } while (--j >= 0);
            int h, e, ns;
            long c, sc;
            WorkQueue q;
            if ((ns = w.nsteals) != 0) {
                if (U.compareAndSwapLong(this, STEALCOUNT, sc = stealCount, sc + ns))
                    w.nsteals = 0;
            } else if (plock != ps)
                ;
            else if ((e = (int) (c = ctl)) < 0)
                w.qlock = -1;
            else {
                if ((h = w.hint) < 0) {
                    if (ec >= 0) {
                        long nc = (((long) ec | ((c - AC_UNIT) & (AC_MASK | TC_MASK))));
                        w.nextWait = e;
                        w.eventCount = ec | INT_SIGN;
                        if (ctl != c || !U.compareAndSwapLong(this, CTL, c, nc))
                            w.eventCount = ec;
                        else if ((int) (c >> AC_SHIFT) == 1 - (config & SMASK))
                            idleAwaitWork(w, nc, c);
                    } else if (w.eventCount < 0 && ctl == c) {
                        Thread wt = Thread.currentThread();
                        Thread.interrupted();
                        U.putObject(wt, PARKBLOCKER, this);
                        w.parker = wt;
                        if (w.eventCount < 0)
                            U.park(false, 0L);
                        w.parker = null;
                        U.putObject(wt, PARKBLOCKER, null);
                    }
                }
                if ((h >= 0 || (h = w.hint) >= 0) && (ws = workQueues) != null && h < ws.length && (q = ws[h]) != null) {
                    WorkQueue v;
                    Thread p;
                    int u, i, s;
                    for (int n = (config & SMASK) - 1; ; ) {
                        int idleCount = (w.eventCount < 0) ? 0 : -1;
                        if (((s = idleCount - q.base + q.top) <= n && (n = s) <= 0) || (u = (int) ((c = ctl) >>> 32)) >= 0 || (e = (int) c) <= 0 || m < (i = e & SMASK) || (v = ws[i]) == null)
                            break;
                        long nc = (((long) (v.nextWait & E_MASK)) | ((long) (u + UAC_UNIT) << 32));
                        if (v.eventCount != (e | INT_SIGN) || !U.compareAndSwapLong(this, CTL, c, nc))
                            break;
                        v.hint = h;
                        v.eventCount = (e + E_SEQ) & E_MASK;
                        if ((p = v.parker) != null)
                            U.unpark(p);
                        if (--n <= 0)
                            break;
                    }
                }
            }
        }
        return null;
    }

    private void idleAwaitWork(WorkQueue w, long currentCtl, long prevCtl) {
        if (w != null && w.eventCount < 0 && !tryTerminate(false, false) && (int) prevCtl != 0 && ctl == currentCtl) {
            int dc = -(short) (currentCtl >>> TC_SHIFT);
            long parkTime = dc < 0 ? FAST_IDLE_TIMEOUT : (dc + 1) * IDLE_TIMEOUT;
            long deadline = System.nanoTime() + parkTime - TIMEOUT_SLOP;
            Thread wt = Thread.currentThread();
            while (ctl == currentCtl) {
                Thread.interrupted();
                U.putObject(wt, PARKBLOCKER, this);
                w.parker = wt;
                if (ctl == currentCtl)
                    U.park(false, parkTime);
                w.parker = null;
                U.putObject(wt, PARKBLOCKER, null);
                if (ctl != currentCtl)
                    break;
                if (deadline - System.nanoTime() <= 0L && U.compareAndSwapLong(this, CTL, currentCtl, prevCtl)) {
                    w.eventCount = (w.eventCount + E_SEQ) | E_MASK;
                    w.hint = -1;
                    w.qlock = -1;
                    break;
                }
            }
        }
    }

    private void helpSignal(ForkJoinTask<?> task, int origin) {
        WorkQueue[] ws;
        WorkQueue w;
        Thread p;
        long c;
        int m, u, e, i, s;
        if (task != null && task.status >= 0 && (u = (int) (ctl >>> 32)) < 0 && (u >> UAC_SHIFT) < 0 && (ws = workQueues) != null && (m = ws.length - 1) >= 0) {
            outer: for (int k = origin, j = m; j >= 0; --j) {
                WorkQueue q = ws[k++ & m];
                for (int n = m; ; ) {
                    if (task.status < 0)
                        break outer;
                    if (q == null || ((s = -q.base + q.top) <= n && (n = s) <= 0))
                        break;
                    if ((u = (int) ((c = ctl) >>> 32)) >= 0 || (e = (int) c) <= 0 || m < (i = e & SMASK) || (w = ws[i]) == null)
                        break outer;
                    long nc = (((long) (w.nextWait & E_MASK)) | ((long) (u + UAC_UNIT) << 32));
                    if (w.eventCount != (e | INT_SIGN))
                        break outer;
                    if (U.compareAndSwapLong(this, CTL, c, nc)) {
                        w.eventCount = (e + E_SEQ) & E_MASK;
                        if ((p = w.parker) != null)
                            U.unpark(p);
                        if (--n <= 0)
                            break;
                    }
                }
            }
        }
    }

    private int tryHelpStealer(WorkQueue joiner, ForkJoinTask<?> task) {
        int stat = 0, steps = 0;
        if (joiner != null && task != null) {
            restart: for (; ; ) {
                ForkJoinTask<?> subtask = task;
                for (WorkQueue j = joiner, v; ; ) {
                    WorkQueue[] ws;
                    int m, s, h;
                    if ((s = task.status) < 0) {
                        stat = s;
                        break restart;
                    }
                    if ((ws = workQueues) == null || (m = ws.length - 1) <= 0)
                        break restart;
                    if ((v = ws[h = (j.hint | 1) & m]) == null || v.currentSteal != subtask) {
                        for (int origin = h; ; ) {
                            if (((h = (h + 2) & m) & 15) == 1 && (subtask.status < 0 || j.currentJoin != subtask))
                                continue restart;
                            if ((v = ws[h]) != null && v.currentSteal == subtask) {
                                j.hint = h;
                                break;
                            }
                            if (h == origin)
                                break restart;
                        }
                    }
                    for (; ; ) {
                        ForkJoinTask[] a;
                        int b;
                        if (subtask.status < 0)
                            continue restart;
                        if ((b = v.base) - v.top < 0 && (a = v.array) != null) {
                            int i = (((a.length - 1) & b) << ASHIFT) + ABASE;
                            ForkJoinTask<?> t = (ForkJoinTask<?>) U.getObjectVolatile(a, i);
                            if (subtask.status < 0 || j.currentJoin != subtask || v.currentSteal != subtask)
                                continue restart;
                            stat = 1;
                            if (t != null && v.base == b && U.compareAndSwapObject(a, i, t, null)) {
                                v.base = b + 1;
                                joiner.runSubtask(t);
                            } else if (v.base == b && ++steps == MAX_HELP)
                                break restart;
                        } else {
                            ForkJoinTask<?> next = v.currentJoin;
                            if (subtask.status < 0 || j.currentJoin != subtask || v.currentSteal != subtask)
                                continue restart;
                            else if (next == null || ++steps == MAX_HELP)
                                break restart;
                            else {
                                subtask = next;
                                j = v;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return stat;
    }

    private int helpComplete(ForkJoinTask<?> task, int mode) {
        WorkQueue[] ws;
        WorkQueue q;
        int m, n, s, u;
        if (task != null && (ws = workQueues) != null && (m = ws.length - 1) >= 0) {
            for (int j = 1, origin = j; ; ) {
                if ((s = task.status) < 0)
                    return s;
                if ((q = ws[j & m]) != null && q.pollAndExecCC(task)) {
                    origin = j;
                    if (mode == SHARED_QUEUE && ((u = (int) (ctl >>> 32)) >= 0 || (u >> UAC_SHIFT) >= 0))
                        break;
                } else if ((j = (j + 2) & m) == origin)
                    break;
            }
        }
        return 0;
    }

    final boolean tryCompensate() {
        int pc = config & SMASK, e, i, tc;
        long c;
        WorkQueue[] ws;
        WorkQueue w;
        Thread p;
        if ((ws = workQueues) != null && (e = (int) (c = ctl)) >= 0) {
            if (e != 0 && (i = e & SMASK) < ws.length && (w = ws[i]) != null && w.eventCount == (e | INT_SIGN)) {
                long nc = ((long) (w.nextWait & E_MASK) | (c & (AC_MASK | TC_MASK)));
                if (U.compareAndSwapLong(this, CTL, c, nc)) {
                    w.eventCount = (e + E_SEQ) & E_MASK;
                    if ((p = w.parker) != null)
                        U.unpark(p);
                    return true;
                }
            } else if ((tc = (short) (c >>> TC_SHIFT)) >= 0 && (int) (c >> AC_SHIFT) + pc > 1) {
                long nc = ((c - AC_UNIT) & AC_MASK) | (c & ~AC_MASK);
                if (U.compareAndSwapLong(this, CTL, c, nc))
                    return true;
            } else if (tc + pc < MAX_CAP) {
                long nc = ((c + TC_UNIT) & TC_MASK) | (c & ~TC_MASK);
                if (U.compareAndSwapLong(this, CTL, c, nc)) {
                    ForkJoinWorkerThreadFactory fac;
                    Throwable ex = null;
                    ForkJoinWorkerThread wt = null;
                    try {
                        if ((fac = factory) != null && (wt = fac.newThread(this)) != null) {
                            wt.start();
                            return true;
                        }
                    } catch (Throwable rex) {
                        ex = rex;
                    }
                    deregisterWorker(wt, ex);
                }
            }
        }
        return false;
    }

    final int awaitJoin(WorkQueue joiner, ForkJoinTask<?> task) {
        int s = 0;
        if (joiner != null && task != null && (s = task.status) >= 0) {
            ForkJoinTask<?> prevJoin = joiner.currentJoin;
            joiner.currentJoin = task;
            do {
            } while ((s = task.status) >= 0 && !joiner.isEmpty() && joiner.tryRemoveAndExec(task));
            if (s >= 0 && (s = task.status) >= 0) {
                helpSignal(task, joiner.poolIndex);
                if ((s = task.status) >= 0 && (task instanceof CountedCompleter))
                    s = helpComplete(task, LIFO_QUEUE);
            }
            while (s >= 0 && (s = task.status) >= 0) {
                if ((!joiner.isEmpty() || (s = tryHelpStealer(joiner, task)) == 0) && (s = task.status) >= 0) {
                    helpSignal(task, joiner.poolIndex);
                    if ((s = task.status) >= 0 && tryCompensate()) {
                        if (task.trySetSignal() && (s = task.status) >= 0) {
                            synchronized (task) {
                                if (task.status >= 0) {
                                    try {
                                        task.wait();
                                    } catch (InterruptedException ie) {
                                    }
                                } else
                                    task.notifyAll();
                            }
                        }
                        long c;
                        do {
                        } while (!U.compareAndSwapLong(this, CTL, c = ctl, c + AC_UNIT));
                    }
                }
            }
            joiner.currentJoin = prevJoin;
        }
        return s;
    }

    final void helpJoinOnce(WorkQueue joiner, ForkJoinTask<?> task) {
        int s;
        if (joiner != null && task != null && (s = task.status) >= 0) {
            ForkJoinTask<?> prevJoin = joiner.currentJoin;
            joiner.currentJoin = task;
            do {
            } while ((s = task.status) >= 0 && !joiner.isEmpty() && joiner.tryRemoveAndExec(task));
            if (s >= 0 && (s = task.status) >= 0) {
                helpSignal(task, joiner.poolIndex);
                if ((s = task.status) >= 0 && (task instanceof CountedCompleter))
                    s = helpComplete(task, LIFO_QUEUE);
            }
            if (s >= 0 && joiner.isEmpty()) {
                do {
                } while (task.status >= 0 && tryHelpStealer(joiner, task) > 0);
            }
            joiner.currentJoin = prevJoin;
        }
    }

    private WorkQueue findNonEmptyStealQueue(int r) {
        for (; ; ) {
            int ps = plock, m;
            WorkQueue[] ws;
            WorkQueue q;
            if ((ws = workQueues) != null && (m = ws.length - 1) >= 0) {
                for (int j = (m + 1) << 2; j >= 0; --j) {
                    if ((q = ws[(((r + j) << 1) | 1) & m]) != null && q.base - q.top < 0)
                        return q;
                }
            }
            if (plock == ps)
                return null;
        }
    }

    final void helpQuiescePool(WorkQueue w) {
        for (boolean active = true; ; ) {
            long c;
            WorkQueue q;
            ForkJoinTask<?> t;
            int b;
            while ((t = w.nextLocalTask()) != null) {
                if (w.base - w.top < 0)
                    signalWork(w);
                t.doExec();
            }
            if ((q = findNonEmptyStealQueue(w.nextSeed())) != null) {
                if (!active) {
                    active = true;
                    do {
                    } while (!U.compareAndSwapLong(this, CTL, c = ctl, c + AC_UNIT));
                }
                if ((b = q.base) - q.top < 0 && (t = q.pollAt(b)) != null) {
                    if (q.base - q.top < 0)
                        signalWork(q);
                    w.runSubtask(t);
                }
            } else if (active) {
                long nc = (c = ctl) - AC_UNIT;
                if ((int) (nc >> AC_SHIFT) + (config & SMASK) == 0)
                    return;
                if (U.compareAndSwapLong(this, CTL, c, nc))
                    active = false;
            } else if ((int) ((c = ctl) >> AC_SHIFT) + (config & SMASK) == 0 && U.compareAndSwapLong(this, CTL, c, c + AC_UNIT))
                return;
        }
    }

    final ForkJoinTask<?> nextTaskFor(WorkQueue w) {
        for (ForkJoinTask<?> t; ; ) {
            WorkQueue q;
            int b;
            if ((t = w.nextLocalTask()) != null)
                return t;
            if ((q = findNonEmptyStealQueue(w.nextSeed())) == null)
                return null;
            if ((b = q.base) - q.top < 0 && (t = q.pollAt(b)) != null) {
                if (q.base - q.top < 0)
                    signalWork(q);
                return t;
            }
        }
    }

    static int getSurplusQueuedTaskCount() {
        Thread t;
        ForkJoinWorkerThread wt;
        ForkJoinPool pool;
        WorkQueue q;
        if (((t = Thread.currentThread()) instanceof ForkJoinWorkerThread)) {
            int p = (pool = (wt = (ForkJoinWorkerThread) t).pool).config & SMASK;
            int n = (q = wt.workQueue).top - q.base;
            int a = (int) (pool.ctl >> AC_SHIFT) + p;
            return n - (a > (p >>>= 1) ? 0 : a > (p >>>= 1) ? 1 : a > (p >>>= 1) ? 2 : a > (p >>>= 1) ? 4 : 8);
        }
        return 0;
    }

    private boolean tryTerminate(boolean now, boolean enable) {
        int ps;
        if (this == common)
            return false;
        if ((ps = plock) >= 0) {
            if (!enable)
                return false;
            if ((ps & PL_LOCK) != 0 || !U.compareAndSwapInt(this, PLOCK, ps, ps += PL_LOCK))
                ps = acquirePlock();
            int nps = ((ps + PL_LOCK) & ~SHUTDOWN) | SHUTDOWN;
            if (!U.compareAndSwapInt(this, PLOCK, ps, nps))
                releasePlock(nps);
        }
        for (long c; ; ) {
            if (((c = ctl) & STOP_BIT) != 0) {
                if ((short) (c >>> TC_SHIFT) == -(config & SMASK)) {
                    synchronized (this) {
                        notifyAll();
                    }
                }
                return true;
            }
            if (!now) {
                WorkQueue[] ws;
                WorkQueue w;
                if ((int) (c >> AC_SHIFT) != -(config & SMASK))
                    return false;
                if ((ws = workQueues) != null) {
                    for (int i = 0; i < ws.length; ++i) {
                        if ((w = ws[i]) != null) {
                            if (!w.isEmpty()) {
                                signalWork(w);
                                return false;
                            }
                            if ((i & 1) != 0 && w.eventCount >= 0)
                                return false;
                        }
                    }
                }
            }
            if (U.compareAndSwapLong(this, CTL, c, c | STOP_BIT)) {
                for (int pass = 0; pass < 3; ++pass) {
                    WorkQueue[] ws;
                    WorkQueue w;
                    Thread wt;
                    if ((ws = workQueues) != null) {
                        int n = ws.length;
                        for (int i = 0; i < n; ++i) {
                            if ((w = ws[i]) != null) {
                                w.qlock = -1;
                                if (pass > 0) {
                                    w.cancelAll();
                                    if (pass > 1 && (wt = w.owner) != null) {
                                        if (!wt.isInterrupted()) {
                                            try {
                                                wt.interrupt();
                                            } catch (Throwable ignore) {
                                            }
                                        }
                                        U.unpark(wt);
                                    }
                                }
                            }
                        }
                        int i, e;
                        long cc;
                        Thread p;
                        while ((e = (int) (cc = ctl) & E_MASK) != 0 && (i = e & SMASK) < n && i >= 0 && (w = ws[i]) != null) {
                            long nc = ((long) (w.nextWait & E_MASK) | ((cc + AC_UNIT) & AC_MASK) | (cc & (TC_MASK | STOP_BIT)));
                            if (w.eventCount == (e | INT_SIGN) && U.compareAndSwapLong(this, CTL, cc, nc)) {
                                w.eventCount = (e + E_SEQ) & E_MASK;
                                w.qlock = -1;
                                if ((p = w.parker) != null)
                                    U.unpark(p);
                            }
                        }
                    }
                }
            }
        }
    }

    static WorkQueue commonSubmitterQueue() {
        ForkJoinPool p;
        WorkQueue[] ws;
        int m, z;
        return ((z = ThreadLocalRandom.getProbe()) != 0 && (p = common) != null && (ws = p.workQueues) != null && (m = ws.length - 1) >= 0) ? ws[m & z & SQMASK] : null;
    }

    static boolean tryExternalUnpush(ForkJoinTask<?> t) {
        ForkJoinPool p;
        WorkQueue[] ws;
        WorkQueue q;
        ForkJoinTask<?>[] a;
        int m, s, z;
        if (t != null && (z = ThreadLocalRandom.getProbe()) != 0 && (p = common) != null && (ws = p.workQueues) != null && (m = ws.length - 1) >= 0 && (q = ws[m & z & SQMASK]) != null && (s = q.top) != q.base && (a = q.array) != null) {
            long j = (((a.length - 1) & (s - 1)) << ASHIFT) + ABASE;
            if (U.getObject(a, j) == t && U.compareAndSwapInt(q, QLOCK, 0, 1)) {
                if (q.array == a && q.top == s && U.compareAndSwapObject(a, j, t, null)) {
                    q.top = s - 1;
                    q.qlock = 0;
                    return true;
                }
                q.qlock = 0;
            }
        }
        return false;
    }

    private void externalHelpComplete(WorkQueue q, ForkJoinTask<?> root) {
        ForkJoinTask<?>[] a;
        int m;
        if (q != null && (a = q.array) != null && (m = (a.length - 1)) >= 0 && root != null && root.status >= 0) {
            for (; ; ) {
                int s, u;
                Object o;
                CountedCompleter<?> task = null;
                if ((s = q.top) - q.base > 0) {
                    long j = ((m & (s - 1)) << ASHIFT) + ABASE;
                    if ((o = U.getObject(a, j)) != null && (o instanceof CountedCompleter)) {
                        CountedCompleter<?> t = (CountedCompleter<?>) o, r = t;
                        do {
                            if (r == root) {
                                if (U.compareAndSwapInt(q, QLOCK, 0, 1)) {
                                    if (q.array == a && q.top == s && U.compareAndSwapObject(a, j, t, null)) {
                                        q.top = s - 1;
                                        task = t;
                                    }
                                    q.qlock = 0;
                                }
                                break;
                            }
                        } while ((r = r.completer) != null);
                    }
                }
                if (task != null)
                    task.doExec();
                if (root.status < 0 || (config != 0 && ((u = (int) (ctl >>> 32)) >= 0 || (u >> UAC_SHIFT) >= 0)))
                    break;
                if (task == null) {
                    helpSignal(root, q.poolIndex);
                    if (root.status >= 0)
                        helpComplete(root, SHARED_QUEUE);
                    break;
                }
            }
        }
    }

    static void externalHelpJoin(ForkJoinTask<?> t) {
        ForkJoinPool p;
        WorkQueue[] ws;
        WorkQueue q, w;
        ForkJoinTask<?>[] a;
        int m, s, n, z;
        if (t != null && (z = ThreadLocalRandom.getProbe()) != 0 && (p = common) != null && (ws = p.workQueues) != null && (m = ws.length - 1) >= 0 && (q = ws[m & z & SQMASK]) != null && (a = q.array) != null) {
            int am = a.length - 1;
            if ((s = q.top) != q.base) {
                long j = ((am & (s - 1)) << ASHIFT) + ABASE;
                if (U.getObject(a, j) == t && U.compareAndSwapInt(q, QLOCK, 0, 1)) {
                    if (q.array == a && q.top == s && U.compareAndSwapObject(a, j, t, null)) {
                        q.top = s - 1;
                        q.qlock = 0;
                        t.doExec();
                    } else
                        q.qlock = 0;
                }
            }
            if (t.status >= 0) {
                if (t instanceof CountedCompleter)
                    p.externalHelpComplete(q, t);
                else
                    p.helpSignal(t, q.poolIndex);
            }
        }
    }

    public ForkJoinPool() {
        this(Math.min(MAX_CAP, Runtime.getRuntime().availableProcessors()), defaultForkJoinWorkerThreadFactory, null, false);
    }

    public ForkJoinPool(int parallelism) {
        this(parallelism, defaultForkJoinWorkerThreadFactory, null, false);
    }

    public ForkJoinPool(int parallelism, ForkJoinWorkerThreadFactory factory, UncaughtExceptionHandler handler, boolean asyncMode) {
        this(checkParallelism(parallelism), checkFactory(factory), handler, asyncMode, "ForkJoinPool-" + nextPoolId() + "-worker-");
        checkPermission();
    }

    private static int checkParallelism(int parallelism) {
        if (parallelism <= 0 || parallelism > MAX_CAP)
            throw new IllegalArgumentException();
        return parallelism;
    }

    private static ForkJoinWorkerThreadFactory checkFactory(ForkJoinWorkerThreadFactory factory) {
        if (factory == null)
            throw new NullPointerException();
        return factory;
    }

    private ForkJoinPool(int parallelism, ForkJoinWorkerThreadFactory factory, UncaughtExceptionHandler handler, boolean asyncMode, String workerNamePrefix) {
        this.workerNamePrefix = workerNamePrefix;
        this.factory = factory;
        this.ueh = handler;
        this.config = parallelism | (asyncMode ? (FIFO_QUEUE << 16) : 0);
        long np = (long) (-parallelism);
        this.ctl = ((np << AC_SHIFT) & AC_MASK) | ((np << TC_SHIFT) & TC_MASK);
    }

    public static ForkJoinPool commonPool() {
        return common;
    }

    public <T> T invoke(ForkJoinTask<T> task) {
        if (task == null)
            throw new NullPointerException();
        externalPush(task);
        return task.join();
    }

    public void execute(ForkJoinTask<?> task) {
        if (task == null)
            throw new NullPointerException();
        externalPush(task);
    }

    public void execute(Runnable task) {
        if (task == null)
            throw new NullPointerException();
        ForkJoinTask<?> job;
        if (task instanceof ForkJoinTask<?>)
            job = (ForkJoinTask<?>) task;
        else
            job = new ForkJoinTask.RunnableExecuteAction(task);
        externalPush(job);
    }

    public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
        if (task == null)
            throw new NullPointerException();
        externalPush(task);
        return task;
    }

    public <T> ForkJoinTask<T> submit(Callable<T> task) {
        ForkJoinTask<T> job = new ForkJoinTask.AdaptedCallable<T>(task);
        externalPush(job);
        return job;
    }

    public <T> ForkJoinTask<T> submit(Runnable task, T result) {
        ForkJoinTask<T> job = new ForkJoinTask.AdaptedRunnable<T>(task, result);
        externalPush(job);
        return job;
    }

    public ForkJoinTask<?> submit(Runnable task) {
        if (task == null)
            throw new NullPointerException();
        ForkJoinTask<?> job;
        if (task instanceof ForkJoinTask<?>)
            job = (ForkJoinTask<?>) task;
        else
            job = new ForkJoinTask.AdaptedRunnableAction(task);
        externalPush(job);
        return job;
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
        ArrayList<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
        boolean done = false;
        try {
            for (Callable<T> t : tasks) {
                ForkJoinTask<T> f = new ForkJoinTask.AdaptedCallable<T>(t);
                futures.add(f);
                externalPush(f);
            }
            for (int i = 0, size = futures.size(); i < size; i++) ((ForkJoinTask<?>) futures.get(i)).quietlyJoin();
            done = true;
            return futures;
        } finally {
            if (!done)
                for (int i = 0, size = futures.size(); i < size; i++) futures.get(i).cancel(false);
        }
    }

    public ForkJoinWorkerThreadFactory getFactory() {
        return factory;
    }

    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return ueh;
    }

    public int getParallelism() {
        int par = (config & SMASK);
        return (par > 0) ? par : 1;
    }

    public static int getCommonPoolParallelism() {
        return commonParallelism;
    }

    public int getPoolSize() {
        return (config & SMASK) + (short) (ctl >>> TC_SHIFT);
    }

    public boolean getAsyncMode() {
        return (config >>> 16) == FIFO_QUEUE;
    }

    public int getRunningThreadCount() {
        int rc = 0;
        WorkQueue[] ws;
        WorkQueue w;
        if ((ws = workQueues) != null) {
            for (int i = 1; i < ws.length; i += 2) {
                if ((w = ws[i]) != null && w.isApparentlyUnblocked())
                    ++rc;
            }
        }
        return rc;
    }

    public int getActiveThreadCount() {
        int r = (config & SMASK) + (int) (ctl >> AC_SHIFT);
        return (r <= 0) ? 0 : r;
    }

    public boolean isQuiescent() {
        return (int) (ctl >> AC_SHIFT) + (config & SMASK) == 0;
    }

    public long getStealCount() {
        long count = stealCount;
        WorkQueue[] ws;
        WorkQueue w;
        if ((ws = workQueues) != null) {
            for (int i = 1; i < ws.length; i += 2) {
                if ((w = ws[i]) != null)
                    count += w.nsteals;
            }
        }
        return count;
    }

    public long getQueuedTaskCount() {
        long count = 0;
        WorkQueue[] ws;
        WorkQueue w;
        if ((ws = workQueues) != null) {
            for (int i = 1; i < ws.length; i += 2) {
                if ((w = ws[i]) != null)
                    count += w.queueSize();
            }
        }
        return count;
    }

    public int getQueuedSubmissionCount() {
        int count = 0;
        WorkQueue[] ws;
        WorkQueue w;
        if ((ws = workQueues) != null) {
            for (int i = 0; i < ws.length; i += 2) {
                if ((w = ws[i]) != null)
                    count += w.queueSize();
            }
        }
        return count;
    }

    public boolean hasQueuedSubmissions() {
        WorkQueue[] ws;
        WorkQueue w;
        if ((ws = workQueues) != null) {
            for (int i = 0; i < ws.length; i += 2) {
                if ((w = ws[i]) != null && !w.isEmpty())
                    return true;
            }
        }
        return false;
    }

    protected ForkJoinTask<?> pollSubmission() {
        WorkQueue[] ws;
        WorkQueue w;
        ForkJoinTask<?> t;
        if ((ws = workQueues) != null) {
            for (int i = 0; i < ws.length; i += 2) {
                if ((w = ws[i]) != null && (t = w.poll()) != null)
                    return t;
            }
        }
        return null;
    }

    protected int drainTasksTo(Collection<? super ForkJoinTask<?>> c) {
        int count = 0;
        WorkQueue[] ws;
        WorkQueue w;
        ForkJoinTask<?> t;
        if ((ws = workQueues) != null) {
            for (int i = 0; i < ws.length; ++i) {
                if ((w = ws[i]) != null) {
                    while ((t = w.poll()) != null) {
                        c.add(t);
                        ++count;
                    }
                }
            }
        }
        return count;
    }

    public String toString() {
        long qt = 0L, qs = 0L;
        int rc = 0;
        long st = stealCount;
        long c = ctl;
        WorkQueue[] ws;
        WorkQueue w;
        if ((ws = workQueues) != null) {
            for (int i = 0; i < ws.length; ++i) {
                if ((w = ws[i]) != null) {
                    int size = w.queueSize();
                    if ((i & 1) == 0)
                        qs += size;
                    else {
                        qt += size;
                        st += w.nsteals;
                        if (w.isApparentlyUnblocked())
                            ++rc;
                    }
                }
            }
        }
        int pc = (config & SMASK);
        int tc = pc + (short) (c >>> TC_SHIFT);
        int ac = pc + (int) (c >> AC_SHIFT);
        if (ac < 0)
            ac = 0;
        String level;
        if ((c & STOP_BIT) != 0)
            level = (tc == 0) ? "Terminated" : "Terminating";
        else
            level = plock < 0 ? "Shutting down" : "Running";
        return super.toString() + "[" + level + ", parallelism = " + pc + ", size = " + tc + ", active = " + ac + ", running = " + rc + ", steals = " + st + ", tasks = " + qt + ", submissions = " + qs + "]";
    }

    public void shutdown() {
        checkPermission();
        tryTerminate(false, true);
    }

    public List<Runnable> shutdownNow() {
        checkPermission();
        tryTerminate(true, true);
        return Collections.emptyList();
    }

    public boolean isTerminated() {
        long c = ctl;
        return ((c & STOP_BIT) != 0L && (short) (c >>> TC_SHIFT) == -(config & SMASK));
    }

    public boolean isTerminating() {
        long c = ctl;
        return ((c & STOP_BIT) != 0L && (short) (c >>> TC_SHIFT) != -(config & SMASK));
    }

    public boolean isShutdown() {
        return plock < 0;
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (this == common) {
            awaitQuiescence(timeout, unit);
            return false;
        }
        long nanos = unit.toNanos(timeout);
        if (isTerminated())
            return true;
        long startTime = System.nanoTime();
        boolean terminated = false;
        synchronized (this) {
            for (long waitTime = nanos, millis = 0L; ; ) {
                if (terminated = isTerminated() || waitTime <= 0L || (millis = unit.toMillis(waitTime)) <= 0L)
                    break;
                wait(millis);
                waitTime = nanos - (System.nanoTime() - startTime);
            }
        }
        return terminated;
    }

    public boolean awaitQuiescence(long timeout, TimeUnit unit) {
        long nanos = unit.toNanos(timeout);
        ForkJoinWorkerThread wt;
        Thread thread = Thread.currentThread();
        if ((thread instanceof ForkJoinWorkerThread) && (wt = (ForkJoinWorkerThread) thread).pool == this) {
            helpQuiescePool(wt.workQueue);
            return true;
        }
        long startTime = System.nanoTime();
        WorkQueue[] ws;
        int r = 0, m;
        boolean found = true;
        while (!isQuiescent() && (ws = workQueues) != null && (m = ws.length - 1) >= 0) {
            if (!found) {
                if ((System.nanoTime() - startTime) > nanos)
                    return false;
                Thread.yield();
            }
            found = false;
            for (int j = (m + 1) << 2; j >= 0; --j) {
                ForkJoinTask<?> t;
                WorkQueue q;
                int b;
                if ((q = ws[r++ & m]) != null && (b = q.base) - q.top < 0) {
                    found = true;
                    if ((t = q.pollAt(b)) != null) {
                        if (q.base - q.top < 0)
                            signalWork(q);
                        t.doExec();
                    }
                    break;
                }
            }
        }
        return true;
    }

    static void quiesceCommonPool() {
        common.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    public static interface ManagedBlocker {

        boolean block() throws InterruptedException;

        boolean isReleasable();
    }

    public static void managedBlock(ManagedBlocker blocker) throws InterruptedException {
        Thread t = Thread.currentThread();
        if (t instanceof ForkJoinWorkerThread) {
            ForkJoinPool p = ((ForkJoinWorkerThread) t).pool;
            while (!blocker.isReleasable()) {
                WorkQueue[] ws;
                WorkQueue q;
                int m, u;
                if ((ws = p.workQueues) != null && (m = ws.length - 1) >= 0) {
                    for (int i = 0; i <= m; ++i) {
                        if (blocker.isReleasable())
                            return;
                        if ((q = ws[i]) != null && q.base - q.top < 0) {
                            p.signalWork(q);
                            if ((u = (int) (p.ctl >>> 32)) >= 0 || (u >> UAC_SHIFT) >= 0)
                                break;
                        }
                    }
                }
                if (p.tryCompensate()) {
                    try {
                        do {
                        } while (!blocker.isReleasable() && !blocker.block());
                    } finally {
                        p.incrementActiveCount();
                    }
                    break;
                }
            }
        } else {
            do {
            } while (!blocker.isReleasable() && !blocker.block());
        }
    }

    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new ForkJoinTask.AdaptedRunnable<T>(runnable, value);
    }

    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new ForkJoinTask.AdaptedCallable<T>(callable);
    }

    private static final sun.misc.Unsafe U;

    private static final long CTL;

    private static final long PARKBLOCKER;

    private static final int ABASE;

    private static final int ASHIFT;

    private static final long STEALCOUNT;

    private static final long PLOCK;

    private static final long INDEXSEED;

    private static final long QLOCK;

    static {
        try {
            U = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ForkJoinPool.class;
            CTL = U.objectFieldOffset(k.getDeclaredField("ctl"));
            STEALCOUNT = U.objectFieldOffset(k.getDeclaredField("stealCount"));
            PLOCK = U.objectFieldOffset(k.getDeclaredField("plock"));
            INDEXSEED = U.objectFieldOffset(k.getDeclaredField("indexSeed"));
            Class<?> tk = Thread.class;
            PARKBLOCKER = U.objectFieldOffset(tk.getDeclaredField("parkBlocker"));
            Class<?> wk = WorkQueue.class;
            QLOCK = U.objectFieldOffset(wk.getDeclaredField("qlock"));
            Class<?> ak = ForkJoinTask[].class;
            ABASE = U.arrayBaseOffset(ak);
            int scale = U.arrayIndexScale(ak);
            if ((scale & (scale - 1)) != 0)
                throw new Error("data type scale not a power of two");
            ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
        } catch (Exception e) {
            throw new Error(e);
        }
        defaultForkJoinWorkerThreadFactory = new DefaultForkJoinWorkerThreadFactory();
        modifyThreadPermission = new RuntimePermission("modifyThread");
        common = java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<ForkJoinPool>() {

            public ForkJoinPool run() {
                return makeCommonPool();
            }
        });
        int par = common.config;
        commonParallelism = par > 0 ? par : 1;
    }

    private static ForkJoinPool makeCommonPool() {
        int parallelism = -1;
        ForkJoinWorkerThreadFactory factory = defaultForkJoinWorkerThreadFactory;
        UncaughtExceptionHandler handler = null;
        try {
            String pp = System.getProperty("java.util.concurrent.ForkJoinPool.common.parallelism");
            String fp = System.getProperty("java.util.concurrent.ForkJoinPool.common.threadFactory");
            String hp = System.getProperty("java.util.concurrent.ForkJoinPool.common.exceptionHandler");
            if (pp != null)
                parallelism = Integer.parseInt(pp);
            if (fp != null)
                factory = ((ForkJoinWorkerThreadFactory) ClassLoader.getSystemClassLoader().loadClass(fp).newInstance());
            if (hp != null)
                handler = ((UncaughtExceptionHandler) ClassLoader.getSystemClassLoader().loadClass(hp).newInstance());
        } catch (Exception ignore) {
        }
        if (parallelism < 0)
            parallelism = Runtime.getRuntime().availableProcessors();
        if (parallelism > MAX_CAP)
            parallelism = MAX_CAP;
        return new ForkJoinPool(parallelism, factory, handler, false, "ForkJoinPool.commonPool-worker-");
    }
}
