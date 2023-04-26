package java.util.concurrent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class Exchanger<V> {

    private static final int ASHIFT = 7;

    private static final int MMASK = 0xff;

    private static final int SEQ = MMASK + 1;

    private static final int NCPU = Runtime.getRuntime().availableProcessors();

    static final int FULL = (NCPU >= (MMASK << 1)) ? MMASK : NCPU >>> 1;

    private static final int SPINS = 1 << 10;

    private static final Object NULL_ITEM = new Object();

    private static final Object TIMED_OUT = new Object();

    @sun.misc.Contended
    static final class Node {

        int index;

        int bound;

        int collides;

        int hash;

        Object item;

        volatile Object match;

        volatile Thread parked;
    }

    static final class Participant extends ThreadLocal<Node> {

        public Node initialValue() {
            return new Node();
        }
    }

    private final Participant participant;

    private volatile Node[] arena;

    private volatile Node slot;

    private volatile int bound;

    private final Object arenaExchange(Object item, boolean timed, long ns) {
        Node[] a = arena;
        Node p = participant.get();
        for (int i = p.index; ; ) {
            int b, m, c;
            long j;
            Node q = (Node) U.getObjectVolatile(a, j = (i << ASHIFT) + ABASE);
            if (q != null && U.compareAndSwapObject(a, j, q, null)) {
                Object v = q.item;
                q.match = item;
                Thread w = q.parked;
                if (w != null)
                    U.unpark(w);
                return v;
            } else if (i <= (m = (b = bound) & MMASK) && q == null) {
                p.item = item;
                if (U.compareAndSwapObject(a, j, null, p)) {
                    long end = (timed && m == 0) ? System.nanoTime() + ns : 0L;
                    Thread t = Thread.currentThread();
                    for (int h = p.hash, spins = SPINS; ; ) {
                        Object v = p.match;
                        if (v != null) {
                            U.putOrderedObject(p, MATCH, null);
                            p.item = null;
                            p.hash = h;
                            return v;
                        } else if (spins > 0) {
                            h ^= h << 1;
                            h ^= h >>> 3;
                            h ^= h << 10;
                            if (h == 0)
                                h = SPINS | (int) t.getId();
                            else if (h < 0 && (--spins & ((SPINS >>> 1) - 1)) == 0)
                                Thread.yield();
                        } else if (U.getObjectVolatile(a, j) != p)
                            spins = SPINS;
                        else if (!t.isInterrupted() && m == 0 && (!timed || (ns = end - System.nanoTime()) > 0L)) {
                            U.putObject(t, BLOCKER, this);
                            p.parked = t;
                            if (U.getObjectVolatile(a, j) == p)
                                U.park(false, ns);
                            p.parked = null;
                            U.putObject(t, BLOCKER, null);
                        } else if (U.getObjectVolatile(a, j) == p && U.compareAndSwapObject(a, j, p, null)) {
                            if (m != 0)
                                U.compareAndSwapInt(this, BOUND, b, b + SEQ - 1);
                            p.item = null;
                            p.hash = h;
                            i = p.index >>>= 1;
                            if (Thread.interrupted())
                                return null;
                            if (timed && m == 0 && ns <= 0L)
                                return TIMED_OUT;
                            break;
                        }
                    }
                } else
                    p.item = null;
            } else {
                if (p.bound != b) {
                    p.bound = b;
                    p.collides = 0;
                    i = (i != m || m == 0) ? m : m - 1;
                } else if ((c = p.collides) < m || m == FULL || !U.compareAndSwapInt(this, BOUND, b, b + SEQ + 1)) {
                    p.collides = c + 1;
                    i = (i == 0) ? m : i - 1;
                } else
                    i = m + 1;
                p.index = i;
            }
        }
    }

    private final Object slotExchange(Object item, boolean timed, long ns) {
        Node p = participant.get();
        Thread t = Thread.currentThread();
        if (t.isInterrupted())
            return null;
        for (Node q; ; ) {
            if ((q = slot) != null) {
                if (U.compareAndSwapObject(this, SLOT, q, null)) {
                    Object v = q.item;
                    q.match = item;
                    Thread w = q.parked;
                    if (w != null)
                        U.unpark(w);
                    return v;
                }
                if (NCPU > 1 && bound == 0 && U.compareAndSwapInt(this, BOUND, 0, SEQ))
                    arena = new Node[(FULL + 2) << ASHIFT];
            } else if (arena != null)
                return null;
            else {
                p.item = item;
                if (U.compareAndSwapObject(this, SLOT, null, p))
                    break;
                p.item = null;
            }
        }
        int h = p.hash;
        long end = timed ? System.nanoTime() + ns : 0L;
        int spins = (NCPU > 1) ? SPINS : 1;
        Object v;
        while ((v = p.match) == null) {
            if (spins > 0) {
                h ^= h << 1;
                h ^= h >>> 3;
                h ^= h << 10;
                if (h == 0)
                    h = SPINS | (int) t.getId();
                else if (h < 0 && (--spins & ((SPINS >>> 1) - 1)) == 0)
                    Thread.yield();
            } else if (slot != p)
                spins = SPINS;
            else if (!t.isInterrupted() && arena == null && (!timed || (ns = end - System.nanoTime()) > 0L)) {
                U.putObject(t, BLOCKER, this);
                p.parked = t;
                if (slot == p)
                    U.park(false, ns);
                p.parked = null;
                U.putObject(t, BLOCKER, null);
            } else if (U.compareAndSwapObject(this, SLOT, p, null)) {
                v = timed && ns <= 0L && !t.isInterrupted() ? TIMED_OUT : null;
                break;
            }
        }
        U.putOrderedObject(p, MATCH, null);
        p.item = null;
        p.hash = h;
        return v;
    }

    public Exchanger() {
        participant = new Participant();
    }

    @SuppressWarnings("unchecked")
    public V exchange(V x) throws InterruptedException {
        Object v;
        Object item = (x == null) ? NULL_ITEM : x;
        if ((arena != null || (v = slotExchange(item, false, 0L)) == null) && ((Thread.interrupted() || (v = arenaExchange(item, false, 0L)) == null)))
            throw new InterruptedException();
        return (v == NULL_ITEM) ? null : (V) v;
    }

    @SuppressWarnings("unchecked")
    public V exchange(V x, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        Object v;
        Object item = (x == null) ? NULL_ITEM : x;
        long ns = unit.toNanos(timeout);
        if ((arena != null || (v = slotExchange(item, true, ns)) == null) && ((Thread.interrupted() || (v = arenaExchange(item, true, ns)) == null)))
            throw new InterruptedException();
        if (v == TIMED_OUT)
            throw new TimeoutException();
        return (v == NULL_ITEM) ? null : (V) v;
    }

    private static final sun.misc.Unsafe U;

    private static final long BOUND;

    private static final long SLOT;

    private static final long MATCH;

    private static final long BLOCKER;

    private static final int ABASE;

    static {
        int s;
        try {
            U = sun.misc.Unsafe.getUnsafe();
            Class<?> ek = Exchanger.class;
            Class<?> nk = Node.class;
            Class<?> ak = Node[].class;
            Class<?> tk = Thread.class;
            BOUND = U.objectFieldOffset(ek.getDeclaredField("bound"));
            SLOT = U.objectFieldOffset(ek.getDeclaredField("slot"));
            MATCH = U.objectFieldOffset(nk.getDeclaredField("match"));
            BLOCKER = U.objectFieldOffset(tk.getDeclaredField("parkBlocker"));
            s = U.arrayIndexScale(ak);
            ABASE = U.arrayBaseOffset(ak) + (1 << ASHIFT);
        } catch (Exception e) {
            throw new Error(e);
        }
        if ((s & (s - 1)) != 0 || s > (1 << ASHIFT))
            throw new Error("Unsupported array scale");
    }
}