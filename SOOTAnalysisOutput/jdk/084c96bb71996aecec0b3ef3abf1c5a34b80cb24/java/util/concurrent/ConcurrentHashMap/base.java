package java.util.concurrent;

import java.io.Serializable;
import java.io.ObjectStreamField;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
public class ConcurrentHashMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V>, Serializable {

    private static final long serialVersionUID = 7249069246763182397L;

    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private static final int DEFAULT_CAPACITY = 16;

    static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;

    private static final float LOAD_FACTOR = 0.75f;

    private static final int TREE_THRESHOLD = 8;

    private static final int MIN_TRANSFER_STRIDE = 16;

    static final int MOVED = 0x80000000;

    static final int HASH_BITS = 0x7fffffff;

    static final int NCPU = Runtime.getRuntime().availableProcessors();

    private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField("segments", Segment[].class), new ObjectStreamField("segmentMask", Integer.TYPE), new ObjectStreamField("segmentShift", Integer.TYPE) };

    @sun.misc.Contended
    static final class Cell {

        volatile long value;

        Cell(long x) {
            value = x;
        }
    }

    transient volatile Node<K, V>[] table;

    private transient volatile Node<K, V>[] nextTable;

    private transient volatile long baseCount;

    private transient volatile int sizeCtl;

    private transient volatile int transferIndex;

    private transient volatile int transferOrigin;

    private transient volatile int cellsBusy;

    private transient volatile Cell[] counterCells;

    private transient KeySetView<K, V> keySet;

    private transient ValuesView<K, V> values;

    private transient EntrySetView<K, V> entrySet;

    static final <K, V> Node<K, V> tabAt(Node<K, V>[] tab, int i) {
        return (Node<K, V>) U.getObjectVolatile(tab, ((long) i << ASHIFT) + ABASE);
    }

    static final <K, V> boolean casTabAt(Node<K, V>[] tab, int i, Node<K, V> c, Node<K, V> v) {
        return U.compareAndSwapObject(tab, ((long) i << ASHIFT) + ABASE, c, v);
    }

    static final <K, V> void setTabAt(Node<K, V>[] tab, int i, Node<K, V> v) {
        U.putObjectVolatile(tab, ((long) i << ASHIFT) + ABASE, v);
    }

    static class Node<K, V> implements Map.Entry<K, V> {

        final int hash;

        final Object key;

        volatile V val;

        Node<K, V> next;

        Node(int hash, Object key, V val, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.val = val;
            this.next = next;
        }

        public final K getKey() {
            return (K) key;
        }

        public final V getValue() {
            return val;
        }

        public final int hashCode() {
            return key.hashCode() ^ val.hashCode();
        }

        public final String toString() {
            return key + "=" + val;
        }

        public final V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        public final boolean equals(Object o) {
            Object k, v, u;
            Map.Entry<?, ?> e;
            return ((o instanceof Map.Entry) && (k = (e = (Map.Entry<?, ?>) o).getKey()) != null && (v = e.getValue()) != null && (k == key || k.equals(key)) && (v == (u = val) || v.equals(u)));
        }
    }

    static final class MapEntry<K, V> implements Map.Entry<K, V> {

        final K key;

        V val;

        final ConcurrentHashMap<K, V> map;

        MapEntry(K key, V val, ConcurrentHashMap<K, V> map) {
            this.key = key;
            this.val = val;
            this.map = map;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return val;
        }

        public int hashCode() {
            return key.hashCode() ^ val.hashCode();
        }

        public String toString() {
            return key + "=" + val;
        }

        public boolean equals(Object o) {
            Object k, v;
            Map.Entry<?, ?> e;
            return ((o instanceof Map.Entry) && (k = (e = (Map.Entry<?, ?>) o).getKey()) != null && (v = e.getValue()) != null && (k == key || k.equals(key)) && (v == val || v.equals(val)));
        }

        public V setValue(V value) {
            if (value == null)
                throw new NullPointerException();
            V v = val;
            val = value;
            map.put(key, value);
            return v;
        }
    }

    static final class TreeNode<K, V> extends Node<K, V> {

        TreeNode<K, V> parent;

        TreeNode<K, V> left;

        TreeNode<K, V> right;

        TreeNode<K, V> prev;

        boolean red;

        TreeNode(int hash, Object key, V val, Node<K, V> next, TreeNode<K, V> parent) {
            super(hash, key, val, next);
            this.parent = parent;
        }
    }

    static Class<?> comparableClassFor(Class<?> c) {
        Class<?> s, cmpc;
        Type[] ts, as;
        Type t;
        ParameterizedType p;
        if (c == String.class)
            return c;
        if (c != null && (cmpc = Comparable.class).isAssignableFrom(c)) {
            while (cmpc.isAssignableFrom(s = c.getSuperclass())) c = s;
            if ((ts = c.getGenericInterfaces()) != null) {
                for (int i = 0; i < ts.length; ++i) {
                    if (((t = ts[i]) instanceof ParameterizedType) && ((p = (ParameterizedType) t).getRawType() == cmpc) && (as = p.getActualTypeArguments()) != null && as.length == 1 && as[0] == c)
                        return c;
                }
            }
        }
        return null;
    }

    static final class TreeBin<K, V> extends StampedLock {

        private static final long serialVersionUID = 2249069246763182397L;

        transient TreeNode<K, V> root;

        transient TreeNode<K, V> first;

        private void rotateLeft(TreeNode<K, V> p) {
            if (p != null) {
                TreeNode<K, V> r = p.right, pp, rl;
                if ((rl = p.right = r.left) != null)
                    rl.parent = p;
                if ((pp = r.parent = p.parent) == null)
                    root = r;
                else if (pp.left == p)
                    pp.left = r;
                else
                    pp.right = r;
                r.left = p;
                p.parent = r;
            }
        }

        private void rotateRight(TreeNode<K, V> p) {
            if (p != null) {
                TreeNode<K, V> l = p.left, pp, lr;
                if ((lr = p.left = l.right) != null)
                    lr.parent = p;
                if ((pp = l.parent = p.parent) == null)
                    root = l;
                else if (pp.right == p)
                    pp.right = l;
                else
                    pp.left = l;
                l.right = p;
                p.parent = l;
            }
        }

        final TreeNode<K, V> getTreeNode(int h, Object k, TreeNode<K, V> p, Class<?> cc) {
            while (p != null) {
                int dir, ph;
                Object pk;
                Class<?> pc;
                if ((ph = p.hash) != h)
                    dir = (h < ph) ? -1 : 1;
                else if ((pk = p.key) == k || k.equals(pk))
                    return p;
                else if (cc == null || pk == null || ((pc = pk.getClass()) != cc && comparableClassFor(pc) != cc) || (dir = ((Comparable<Object>) k).compareTo(pk)) == 0) {
                    TreeNode<K, V> r, pr;
                    if ((pr = p.right) != null && (r = getTreeNode(h, k, pr, cc)) != null)
                        return r;
                    else
                        dir = -1;
                }
                p = (dir > 0) ? p.right : p.left;
            }
            return null;
        }

        final V getValue(int h, Object k) {
            Class<?> cc = comparableClassFor(k.getClass());
            Node<K, V> r = null;
            for (Node<K, V> e = first; e != null; e = e.next) {
                long s;
                if ((s = tryReadLock()) != 0L) {
                    try {
                        r = getTreeNode(h, k, root, cc);
                    } finally {
                        unlockRead(s);
                    }
                    break;
                } else if (e.hash == h && k.equals(e.key)) {
                    r = e;
                    break;
                }
            }
            return r == null ? null : r.val;
        }

        final TreeNode<K, V> putTreeNode(int h, Object k, V v) {
            Class<?> cc = comparableClassFor(k.getClass());
            TreeNode<K, V> pp = root, p = null;
            int dir = 0;
            while (pp != null) {
                int ph;
                Object pk;
                Class<?> pc;
                p = pp;
                if ((ph = p.hash) != h)
                    dir = (h < ph) ? -1 : 1;
                else if ((pk = p.key) == k || k.equals(pk))
                    return p;
                else if (cc == null || pk == null || ((pc = pk.getClass()) != cc && comparableClassFor(pc) != cc) || (dir = ((Comparable<Object>) k).compareTo(pk)) == 0) {
                    TreeNode<K, V> r, pr;
                    if ((pr = p.right) != null && (r = getTreeNode(h, k, pr, cc)) != null)
                        return r;
                    else
                        dir = -1;
                }
                pp = (dir > 0) ? p.right : p.left;
            }
            TreeNode<K, V> f = first;
            TreeNode<K, V> x = first = new TreeNode<K, V>(h, k, v, f, p);
            if (p == null)
                root = x;
            else {
                if (f != null)
                    f.prev = x;
                if (dir <= 0)
                    p.left = x;
                else
                    p.right = x;
                x.red = true;
                for (TreeNode<K, V> xp, xpp, xppl, xppr; ; ) {
                    if ((xp = x.parent) == null) {
                        (root = x).red = false;
                        break;
                    } else if (!xp.red || (xpp = xp.parent) == null) {
                        TreeNode<K, V> r = root;
                        if (r != null && r.red)
                            r.red = false;
                        break;
                    } else if ((xppl = xpp.left) == xp) {
                        if ((xppr = xpp.right) != null && xppr.red) {
                            xppr.red = false;
                            xp.red = false;
                            xpp.red = true;
                            x = xpp;
                        } else {
                            if (x == xp.right) {
                                rotateLeft(x = xp);
                                xpp = (xp = x.parent) == null ? null : xp.parent;
                            }
                            if (xp != null) {
                                xp.red = false;
                                if (xpp != null) {
                                    xpp.red = true;
                                    rotateRight(xpp);
                                }
                            }
                        }
                    } else {
                        if (xppl != null && xppl.red) {
                            xppl.red = false;
                            xp.red = false;
                            xpp.red = true;
                            x = xpp;
                        } else {
                            if (x == xp.left) {
                                rotateRight(x = xp);
                                xpp = (xp = x.parent) == null ? null : xp.parent;
                            }
                            if (xp != null) {
                                xp.red = false;
                                if (xpp != null) {
                                    xpp.red = true;
                                    rotateLeft(xpp);
                                }
                            }
                        }
                    }
                }
            }
            assert checkInvariants();
            return null;
        }

        final void deleteTreeNode(TreeNode<K, V> p) {
            TreeNode<K, V> next = (TreeNode<K, V>) p.next;
            TreeNode<K, V> pred = p.prev;
            if (pred == null)
                first = next;
            else
                pred.next = next;
            if (next != null)
                next.prev = pred;
            else if (pred == null) {
                root = null;
                return;
            }
            TreeNode<K, V> replacement;
            TreeNode<K, V> pl = p.left;
            TreeNode<K, V> pr = p.right;
            if (pl != null && pr != null) {
                TreeNode<K, V> s = pr, sl;
                while ((sl = s.left) != null) s = sl;
                boolean c = s.red;
                s.red = p.red;
                p.red = c;
                TreeNode<K, V> sr = s.right;
                TreeNode<K, V> pp = p.parent;
                if (s == pr) {
                    p.parent = s;
                    s.right = p;
                } else {
                    TreeNode<K, V> sp = s.parent;
                    if ((p.parent = sp) != null) {
                        if (s == sp.left)
                            sp.left = p;
                        else
                            sp.right = p;
                    }
                    if ((s.right = pr) != null)
                        pr.parent = s;
                }
                p.left = null;
                if ((p.right = sr) != null)
                    sr.parent = p;
                if ((s.left = pl) != null)
                    pl.parent = s;
                if ((s.parent = pp) == null)
                    root = s;
                else if (p == pp.left)
                    pp.left = s;
                else
                    pp.right = s;
                if (sr != null)
                    replacement = sr;
                else
                    replacement = p;
            } else if (pl != null)
                replacement = pl;
            else if (pr != null)
                replacement = pr;
            else
                replacement = p;
            if (replacement != p) {
                TreeNode<K, V> pp = replacement.parent = p.parent;
                if (pp == null)
                    root = replacement;
                else if (p == pp.left)
                    pp.left = replacement;
                else
                    pp.right = replacement;
                p.left = p.right = p.parent = null;
            }
            if (!p.red) {
                for (TreeNode<K, V> x = replacement; x != null; ) {
                    TreeNode<K, V> xp, xpl, xpr;
                    if (x.red || (xp = x.parent) == null) {
                        x.red = false;
                        break;
                    } else if ((xpl = xp.left) == x) {
                        if ((xpr = xp.right) != null && xpr.red) {
                            xpr.red = false;
                            xp.red = true;
                            rotateLeft(xp);
                            xpr = (xp = x.parent) == null ? null : xp.right;
                        }
                        if (xpr == null)
                            x = xp;
                        else {
                            TreeNode<K, V> sl = xpr.left, sr = xpr.right;
                            if ((sr == null || !sr.red) && (sl == null || !sl.red)) {
                                xpr.red = true;
                                x = xp;
                            } else {
                                if (sr == null || !sr.red) {
                                    if (sl != null)
                                        sl.red = false;
                                    xpr.red = true;
                                    rotateRight(xpr);
                                    xpr = (xp = x.parent) == null ? null : xp.right;
                                }
                                if (xpr != null) {
                                    xpr.red = (xp == null) ? false : xp.red;
                                    if ((sr = xpr.right) != null)
                                        sr.red = false;
                                }
                                if (xp != null) {
                                    xp.red = false;
                                    rotateLeft(xp);
                                }
                                x = root;
                            }
                        }
                    } else {
                        if (xpl != null && xpl.red) {
                            xpl.red = false;
                            xp.red = true;
                            rotateRight(xp);
                            xpl = (xp = x.parent) == null ? null : xp.left;
                        }
                        if (xpl == null)
                            x = xp;
                        else {
                            TreeNode<K, V> sl = xpl.left, sr = xpl.right;
                            if ((sl == null || !sl.red) && (sr == null || !sr.red)) {
                                xpl.red = true;
                                x = xp;
                            } else {
                                if (sl == null || !sl.red) {
                                    if (sr != null)
                                        sr.red = false;
                                    xpl.red = true;
                                    rotateLeft(xpl);
                                    xpl = (xp = x.parent) == null ? null : xp.left;
                                }
                                if (xpl != null) {
                                    xpl.red = (xp == null) ? false : xp.red;
                                    if ((sl = xpl.left) != null)
                                        sl.red = false;
                                }
                                if (xp != null) {
                                    xp.red = false;
                                    rotateRight(xp);
                                }
                                x = root;
                            }
                        }
                    }
                }
            }
            if (p == replacement) {
                TreeNode<K, V> pp;
                if ((pp = p.parent) != null) {
                    if (p == pp.left)
                        pp.left = null;
                    else if (p == pp.right)
                        pp.right = null;
                    p.parent = null;
                }
            }
            assert checkInvariants();
        }

        final boolean checkInvariants() {
            TreeNode<K, V> r = root;
            if (r == null)
                return (first == null);
            else
                return (first != null) && checkTreeNode(r);
        }

        final boolean checkTreeNode(TreeNode<K, V> t) {
            TreeNode<K, V> tp = t.parent, tl = t.left, tr = t.right, tb = t.prev, tn = (TreeNode<K, V>) t.next;
            if (tb != null && tb.next != t)
                return false;
            if (tn != null && tn.prev != t)
                return false;
            if (tp != null && t != tp.left && t != tp.right)
                return false;
            if (tl != null && (tl.parent != t || tl.hash > t.hash))
                return false;
            if (tr != null && (tr.parent != t || tr.hash < t.hash))
                return false;
            if (t.red && tl != null && tl.red && tr != null && tr.red)
                return false;
            if (tl != null && !checkTreeNode(tl))
                return false;
            if (tr != null && !checkTreeNode(tr))
                return false;
            return true;
        }
    }

    private static final int spread(int h) {
        h ^= (h >>> 18) ^ (h >>> 12);
        return (h ^ (h >>> 10)) & HASH_BITS;
    }

    private final void replaceWithTreeBin(Node<K, V>[] tab, int index, Object key) {
        if (tab != null && comparableClassFor(key.getClass()) != null) {
            TreeBin<K, V> t = new TreeBin<K, V>();
            for (Node<K, V> e = tabAt(tab, index); e != null; e = e.next) t.putTreeNode(e.hash, e.key, e.val);
            setTabAt(tab, index, new Node<K, V>(MOVED, t, null, null));
        }
    }

    private final V internalGet(Object k) {
        int h = spread(k.hashCode());
        V v = null;
        Node<K, V>[] tab;
        Node<K, V> e;
        if ((tab = table) != null && (e = tabAt(tab, (tab.length - 1) & h)) != null) {
            for (; ; ) {
                int eh;
                Object ek;
                if ((eh = e.hash) < 0) {
                    if ((ek = e.key) instanceof TreeBin) {
                        v = ((TreeBin<K, V>) ek).getValue(h, k);
                        break;
                    } else if (!(ek instanceof Node[]) || (e = tabAt(tab = (Node<K, V>[]) ek, (tab.length - 1) & h)) == null)
                        break;
                } else if (eh == h && ((ek = e.key) == k || k.equals(ek))) {
                    v = e.val;
                    break;
                } else if ((e = e.next) == null)
                    break;
            }
        }
        return v;
    }

    private final V internalReplace(Object k, V v, Object cv) {
        int h = spread(k.hashCode());
        V oldVal = null;
        for (Node<K, V>[] tab = table; ; ) {
            Node<K, V> f;
            int i, fh;
            Object fk;
            if (tab == null || (f = tabAt(tab, i = (tab.length - 1) & h)) == null)
                break;
            else if ((fh = f.hash) < 0) {
                if ((fk = f.key) instanceof TreeBin) {
                    TreeBin<K, V> t = (TreeBin<K, V>) fk;
                    long stamp = t.writeLock();
                    boolean validated = false;
                    boolean deleted = false;
                    try {
                        if (tabAt(tab, i) == f) {
                            validated = true;
                            Class<?> cc = comparableClassFor(k.getClass());
                            TreeNode<K, V> p = t.getTreeNode(h, k, t.root, cc);
                            if (p != null) {
                                V pv = p.val;
                                if (cv == null || cv == pv || cv.equals(pv)) {
                                    oldVal = pv;
                                    if (v != null)
                                        p.val = v;
                                    else {
                                        deleted = true;
                                        t.deleteTreeNode(p);
                                    }
                                }
                            }
                        }
                    } finally {
                        t.unlockWrite(stamp);
                    }
                    if (validated) {
                        if (deleted)
                            addCount(-1L, -1);
                        break;
                    }
                } else
                    tab = (Node<K, V>[]) fk;
            } else {
                boolean validated = false;
                boolean deleted = false;
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        validated = true;
                        for (Node<K, V> e = f, pred = null; ; ) {
                            Object ek;
                            if (e.hash == h && ((ek = e.key) == k || k.equals(ek))) {
                                V ev = e.val;
                                if (cv == null || cv == ev || cv.equals(ev)) {
                                    oldVal = ev;
                                    if (v != null)
                                        e.val = v;
                                    else {
                                        deleted = true;
                                        Node<K, V> en = e.next;
                                        if (pred != null)
                                            pred.next = en;
                                        else
                                            setTabAt(tab, i, en);
                                    }
                                }
                                break;
                            }
                            pred = e;
                            if ((e = e.next) == null)
                                break;
                        }
                    }
                }
                if (validated) {
                    if (deleted)
                        addCount(-1L, -1);
                    break;
                }
            }
        }
        return oldVal;
    }

    private final V internalPut(K k, V v, boolean onlyIfAbsent) {
        if (k == null || v == null)
            throw new NullPointerException();
        int h = spread(k.hashCode());
        int len = 0;
        for (Node<K, V>[] tab = table; ; ) {
            int i, fh;
            Node<K, V> f;
            Object fk;
            if (tab == null)
                tab = initTable();
            else if ((f = tabAt(tab, i = (tab.length - 1) & h)) == null) {
                if (casTabAt(tab, i, null, new Node<K, V>(h, k, v, null)))
                    break;
            } else if ((fh = f.hash) < 0) {
                if ((fk = f.key) instanceof TreeBin) {
                    TreeBin<K, V> t = (TreeBin<K, V>) fk;
                    long stamp = t.writeLock();
                    V oldVal = null;
                    try {
                        if (tabAt(tab, i) == f) {
                            len = 2;
                            TreeNode<K, V> p = t.putTreeNode(h, k, v);
                            if (p != null) {
                                oldVal = p.val;
                                if (!onlyIfAbsent)
                                    p.val = v;
                            }
                        }
                    } finally {
                        t.unlockWrite(stamp);
                    }
                    if (len != 0) {
                        if (oldVal != null)
                            return oldVal;
                        break;
                    }
                } else
                    tab = (Node<K, V>[]) fk;
            } else {
                V oldVal = null;
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        len = 1;
                        for (Node<K, V> e = f; ; ++len) {
                            Object ek;
                            if (e.hash == h && ((ek = e.key) == k || k.equals(ek))) {
                                oldVal = e.val;
                                if (!onlyIfAbsent)
                                    e.val = v;
                                break;
                            }
                            Node<K, V> last = e;
                            if ((e = e.next) == null) {
                                last.next = new Node<K, V>(h, k, v, null);
                                if (len > TREE_THRESHOLD)
                                    replaceWithTreeBin(tab, i, k);
                                break;
                            }
                        }
                    }
                }
                if (len != 0) {
                    if (oldVal != null)
                        return oldVal;
                    break;
                }
            }
        }
        addCount(1L, len);
        return null;
    }

    private final V internalComputeIfAbsent(K k, Function<? super K, ? extends V> mf) {
        if (k == null || mf == null)
            throw new NullPointerException();
        int h = spread(k.hashCode());
        V val = null;
        int len = 0;
        for (Node<K, V>[] tab = table; ; ) {
            Node<K, V> f;
            int i;
            Object fk;
            if (tab == null)
                tab = initTable();
            else if ((f = tabAt(tab, i = (tab.length - 1) & h)) == null) {
                Node<K, V> node = new Node<K, V>(h, k, null, null);
                synchronized (node) {
                    if (casTabAt(tab, i, null, node)) {
                        len = 1;
                        try {
                            if ((val = mf.apply(k)) != null)
                                node.val = val;
                        } finally {
                            if (val == null)
                                setTabAt(tab, i, null);
                        }
                    }
                }
                if (len != 0)
                    break;
            } else if (f.hash < 0) {
                if ((fk = f.key) instanceof TreeBin) {
                    TreeBin<K, V> t = (TreeBin<K, V>) fk;
                    long stamp = t.writeLock();
                    boolean added = false;
                    try {
                        if (tabAt(tab, i) == f) {
                            len = 2;
                            Class<?> cc = comparableClassFor(k.getClass());
                            TreeNode<K, V> p = t.getTreeNode(h, k, t.root, cc);
                            if (p != null)
                                val = p.val;
                            else if ((val = mf.apply(k)) != null) {
                                added = true;
                                t.putTreeNode(h, k, val);
                            }
                        }
                    } finally {
                        t.unlockWrite(stamp);
                    }
                    if (len != 0) {
                        if (!added)
                            return val;
                        break;
                    }
                } else
                    tab = (Node<K, V>[]) fk;
            } else {
                boolean added = false;
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        len = 1;
                        for (Node<K, V> e = f; ; ++len) {
                            Object ek;
                            V ev;
                            if (e.hash == h && ((ek = e.key) == k || k.equals(ek))) {
                                val = e.val;
                                break;
                            }
                            Node<K, V> last = e;
                            if ((e = e.next) == null) {
                                if ((val = mf.apply(k)) != null) {
                                    added = true;
                                    last.next = new Node<K, V>(h, k, val, null);
                                    if (len > TREE_THRESHOLD)
                                        replaceWithTreeBin(tab, i, k);
                                }
                                break;
                            }
                        }
                    }
                }
                if (len != 0) {
                    if (!added)
                        return val;
                    break;
                }
            }
        }
        if (val != null)
            addCount(1L, len);
        return val;
    }

    private final V internalCompute(K k, boolean onlyIfPresent, BiFunction<? super K, ? super V, ? extends V> mf) {
        if (k == null || mf == null)
            throw new NullPointerException();
        int h = spread(k.hashCode());
        V val = null;
        int delta = 0;
        int len = 0;
        for (Node<K, V>[] tab = table; ; ) {
            Node<K, V> f;
            int i, fh;
            Object fk;
            if (tab == null)
                tab = initTable();
            else if ((f = tabAt(tab, i = (tab.length - 1) & h)) == null) {
                if (onlyIfPresent)
                    break;
                Node<K, V> node = new Node<K, V>(h, k, null, null);
                synchronized (node) {
                    if (casTabAt(tab, i, null, node)) {
                        try {
                            len = 1;
                            if ((val = mf.apply(k, null)) != null) {
                                node.val = val;
                                delta = 1;
                            }
                        } finally {
                            if (delta == 0)
                                setTabAt(tab, i, null);
                        }
                    }
                }
                if (len != 0)
                    break;
            } else if ((fh = f.hash) < 0) {
                if ((fk = f.key) instanceof TreeBin) {
                    TreeBin<K, V> t = (TreeBin<K, V>) fk;
                    long stamp = t.writeLock();
                    try {
                        if (tabAt(tab, i) == f) {
                            len = 2;
                            Class<?> cc = comparableClassFor(k.getClass());
                            TreeNode<K, V> p = t.getTreeNode(h, k, t.root, cc);
                            if (p != null || !onlyIfPresent) {
                                V pv = (p == null) ? null : p.val;
                                if ((val = mf.apply(k, pv)) != null) {
                                    if (p != null)
                                        p.val = val;
                                    else {
                                        delta = 1;
                                        t.putTreeNode(h, k, val);
                                    }
                                } else if (p != null) {
                                    delta = -1;
                                    t.deleteTreeNode(p);
                                }
                            }
                        }
                    } finally {
                        t.unlockWrite(stamp);
                    }
                    if (len != 0)
                        break;
                } else
                    tab = (Node<K, V>[]) fk;
            } else {
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        len = 1;
                        for (Node<K, V> e = f, pred = null; ; ++len) {
                            Object ek;
                            if (e.hash == h && ((ek = e.key) == k || k.equals(ek))) {
                                val = mf.apply(k, e.val);
                                if (val != null)
                                    e.val = val;
                                else {
                                    delta = -1;
                                    Node<K, V> en = e.next;
                                    if (pred != null)
                                        pred.next = en;
                                    else
                                        setTabAt(tab, i, en);
                                }
                                break;
                            }
                            pred = e;
                            if ((e = e.next) == null) {
                                if (!onlyIfPresent && (val = mf.apply(k, null)) != null) {
                                    pred.next = new Node<K, V>(h, k, val, null);
                                    delta = 1;
                                    if (len > TREE_THRESHOLD)
                                        replaceWithTreeBin(tab, i, k);
                                }
                                break;
                            }
                        }
                    }
                }
                if (len != 0)
                    break;
            }
        }
        if (delta != 0)
            addCount((long) delta, len);
        return val;
    }

    private final V internalMerge(K k, V v, BiFunction<? super V, ? super V, ? extends V> mf) {
        if (k == null || v == null || mf == null)
            throw new NullPointerException();
        int h = spread(k.hashCode());
        V val = null;
        int delta = 0;
        int len = 0;
        for (Node<K, V>[] tab = table; ; ) {
            int i;
            Node<K, V> f;
            Object fk;
            if (tab == null)
                tab = initTable();
            else if ((f = tabAt(tab, i = (tab.length - 1) & h)) == null) {
                if (casTabAt(tab, i, null, new Node<K, V>(h, k, v, null))) {
                    delta = 1;
                    val = v;
                    break;
                }
            } else if (f.hash < 0) {
                if ((fk = f.key) instanceof TreeBin) {
                    TreeBin<K, V> t = (TreeBin<K, V>) fk;
                    long stamp = t.writeLock();
                    try {
                        if (tabAt(tab, i) == f) {
                            len = 2;
                            Class<?> cc = comparableClassFor(k.getClass());
                            TreeNode<K, V> p = t.getTreeNode(h, k, t.root, cc);
                            val = (p == null) ? v : mf.apply(p.val, v);
                            if (val != null) {
                                if (p != null)
                                    p.val = val;
                                else {
                                    delta = 1;
                                    t.putTreeNode(h, k, val);
                                }
                            } else if (p != null) {
                                delta = -1;
                                t.deleteTreeNode(p);
                            }
                        }
                    } finally {
                        t.unlockWrite(stamp);
                    }
                    if (len != 0)
                        break;
                } else
                    tab = (Node<K, V>[]) fk;
            } else {
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        len = 1;
                        for (Node<K, V> e = f, pred = null; ; ++len) {
                            Object ek;
                            if (e.hash == h && ((ek = e.key) == k || k.equals(ek))) {
                                val = mf.apply(e.val, v);
                                if (val != null)
                                    e.val = val;
                                else {
                                    delta = -1;
                                    Node<K, V> en = e.next;
                                    if (pred != null)
                                        pred.next = en;
                                    else
                                        setTabAt(tab, i, en);
                                }
                                break;
                            }
                            pred = e;
                            if ((e = e.next) == null) {
                                delta = 1;
                                val = v;
                                pred.next = new Node<K, V>(h, k, val, null);
                                if (len > TREE_THRESHOLD)
                                    replaceWithTreeBin(tab, i, k);
                                break;
                            }
                        }
                    }
                }
                if (len != 0)
                    break;
            }
        }
        if (delta != 0)
            addCount((long) delta, len);
        return val;
    }

    private final void internalPutAll(Map<? extends K, ? extends V> m) {
        tryPresize(m.size());
        long delta = 0L;
        boolean npe = false;
        try {
            for (Map.Entry<?, ? extends V> entry : m.entrySet()) {
                Object k;
                V v;
                if (entry == null || (k = entry.getKey()) == null || (v = entry.getValue()) == null) {
                    npe = true;
                    break;
                }
                int h = spread(k.hashCode());
                for (Node<K, V>[] tab = table; ; ) {
                    int i;
                    Node<K, V> f;
                    int fh;
                    Object fk;
                    if (tab == null)
                        tab = initTable();
                    else if ((f = tabAt(tab, i = (tab.length - 1) & h)) == null) {
                        if (casTabAt(tab, i, null, new Node<K, V>(h, k, v, null))) {
                            ++delta;
                            break;
                        }
                    } else if ((fh = f.hash) < 0) {
                        if ((fk = f.key) instanceof TreeBin) {
                            TreeBin<K, V> t = (TreeBin<K, V>) fk;
                            long stamp = t.writeLock();
                            boolean validated = false;
                            try {
                                if (tabAt(tab, i) == f) {
                                    validated = true;
                                    Class<?> cc = comparableClassFor(k.getClass());
                                    TreeNode<K, V> p = t.getTreeNode(h, k, t.root, cc);
                                    if (p != null)
                                        p.val = v;
                                    else {
                                        ++delta;
                                        t.putTreeNode(h, k, v);
                                    }
                                }
                            } finally {
                                t.unlockWrite(stamp);
                            }
                            if (validated)
                                break;
                        } else
                            tab = (Node<K, V>[]) fk;
                    } else {
                        int len = 0;
                        synchronized (f) {
                            if (tabAt(tab, i) == f) {
                                len = 1;
                                for (Node<K, V> e = f; ; ++len) {
                                    Object ek;
                                    if (e.hash == h && ((ek = e.key) == k || k.equals(ek))) {
                                        e.val = v;
                                        break;
                                    }
                                    Node<K, V> last = e;
                                    if ((e = e.next) == null) {
                                        ++delta;
                                        last.next = new Node<K, V>(h, k, v, null);
                                        if (len > TREE_THRESHOLD)
                                            replaceWithTreeBin(tab, i, k);
                                        break;
                                    }
                                }
                            }
                        }
                        if (len != 0) {
                            if (len > 1) {
                                addCount(delta, len);
                                delta = 0L;
                            }
                            break;
                        }
                    }
                }
            }
        } finally {
            if (delta != 0L)
                addCount(delta, 2);
        }
        if (npe)
            throw new NullPointerException();
    }

    private final void internalClear() {
        long delta = 0L;
        int i = 0;
        Node<K, V>[] tab = table;
        while (tab != null && i < tab.length) {
            Node<K, V> f = tabAt(tab, i);
            if (f == null)
                ++i;
            else if (f.hash < 0) {
                Object fk;
                if ((fk = f.key) instanceof TreeBin) {
                    TreeBin<K, V> t = (TreeBin<K, V>) fk;
                    long stamp = t.writeLock();
                    try {
                        if (tabAt(tab, i) == f) {
                            for (Node<K, V> p = t.first; p != null; p = p.next) --delta;
                            t.first = null;
                            t.root = null;
                            ++i;
                        }
                    } finally {
                        t.unlockWrite(stamp);
                    }
                } else
                    tab = (Node<K, V>[]) fk;
            } else {
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        for (Node<K, V> e = f; e != null; e = e.next) --delta;
                        setTabAt(tab, i, null);
                        ++i;
                    }
                }
            }
        }
        if (delta != 0L)
            addCount(delta, -1);
    }

    private static final int tableSizeFor(int c) {
        int n = c - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    private final Node<K, V>[] initTable() {
        Node<K, V>[] tab;
        int sc;
        while ((tab = table) == null) {
            if ((sc = sizeCtl) < 0)
                Thread.yield();
            else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
                try {
                    if ((tab = table) == null) {
                        int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                        table = tab = (Node<K, V>[]) new Node[n];
                        sc = n - (n >>> 2);
                    }
                } finally {
                    sizeCtl = sc;
                }
                break;
            }
        }
        return tab;
    }

    private final void addCount(long x, int check) {
        Cell[] as;
        long b, s;
        if ((as = counterCells) != null || !U.compareAndSwapLong(this, BASECOUNT, b = baseCount, s = b + x)) {
            Cell a;
            long v;
            int m;
            boolean uncontended = true;
            if (as == null || (m = as.length - 1) < 0 || (a = as[ThreadLocalRandom.getProbe() & m]) == null || !(uncontended = U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))) {
                fullAddCount(x, uncontended);
                return;
            }
            if (check <= 1)
                return;
            s = sumCount();
        }
        if (check >= 0) {
            Node<K, V>[] tab, nt;
            int sc;
            while (s >= (long) (sc = sizeCtl) && (tab = table) != null && tab.length < MAXIMUM_CAPACITY) {
                if (sc < 0) {
                    if (sc == -1 || transferIndex <= transferOrigin || (nt = nextTable) == null)
                        break;
                    if (U.compareAndSwapInt(this, SIZECTL, sc, sc - 1))
                        transfer(tab, nt);
                } else if (U.compareAndSwapInt(this, SIZECTL, sc, -2))
                    transfer(tab, null);
                s = sumCount();
            }
        }
    }

    private final void tryPresize(int size) {
        int c = (size >= (MAXIMUM_CAPACITY >>> 1)) ? MAXIMUM_CAPACITY : tableSizeFor(size + (size >>> 1) + 1);
        int sc;
        while ((sc = sizeCtl) >= 0) {
            Node<K, V>[] tab = table;
            int n;
            if (tab == null || (n = tab.length) == 0) {
                n = (sc > c) ? sc : c;
                if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
                    try {
                        if (table == tab) {
                            table = (Node<K, V>[]) new Node[n];
                            sc = n - (n >>> 2);
                        }
                    } finally {
                        sizeCtl = sc;
                    }
                }
            } else if (c <= sc || n >= MAXIMUM_CAPACITY)
                break;
            else if (tab == table && U.compareAndSwapInt(this, SIZECTL, sc, -2))
                transfer(tab, null);
        }
    }

    private final void transfer(Node<K, V>[] tab, Node<K, V>[] nextTab) {
        int n = tab.length, stride;
        if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)
            stride = MIN_TRANSFER_STRIDE;
        if (nextTab == null) {
            try {
                nextTab = (Node<K, V>[]) new Node[n << 1];
            } catch (Throwable ex) {
                sizeCtl = Integer.MAX_VALUE;
                return;
            }
            nextTable = nextTab;
            transferOrigin = n;
            transferIndex = n;
            Node<K, V> rev = new Node<K, V>(MOVED, tab, null, null);
            for (int k = n; k > 0; ) {
                int nextk = (k > stride) ? k - stride : 0;
                for (int m = nextk; m < k; ++m) nextTab[m] = rev;
                for (int m = n + nextk; m < n + k; ++m) nextTab[m] = rev;
                U.putOrderedInt(this, TRANSFERORIGIN, k = nextk);
            }
        }
        int nextn = nextTab.length;
        Node<K, V> fwd = new Node<K, V>(MOVED, nextTab, null, null);
        boolean advance = true;
        for (int i = 0, bound = 0; ; ) {
            int nextIndex, nextBound;
            Node<K, V> f;
            Object fk;
            while (advance) {
                if (--i >= bound)
                    advance = false;
                else if ((nextIndex = transferIndex) <= transferOrigin) {
                    i = -1;
                    advance = false;
                } else if (U.compareAndSwapInt(this, TRANSFERINDEX, nextIndex, nextBound = (nextIndex > stride ? nextIndex - stride : 0))) {
                    bound = nextBound;
                    i = nextIndex - 1;
                    advance = false;
                }
            }
            if (i < 0 || i >= n || i + n >= nextn) {
                for (int sc; ; ) {
                    if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, ++sc)) {
                        if (sc == -1) {
                            nextTable = null;
                            table = nextTab;
                            sizeCtl = (n << 1) - (n >>> 1);
                        }
                        return;
                    }
                }
            } else if ((f = tabAt(tab, i)) == null) {
                if (casTabAt(tab, i, null, fwd)) {
                    setTabAt(nextTab, i, null);
                    setTabAt(nextTab, i + n, null);
                    advance = true;
                }
            } else if (f.hash >= 0) {
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        int runBit = f.hash & n;
                        Node<K, V> lastRun = f, lo = null, hi = null;
                        for (Node<K, V> p = f.next; p != null; p = p.next) {
                            int b = p.hash & n;
                            if (b != runBit) {
                                runBit = b;
                                lastRun = p;
                            }
                        }
                        if (runBit == 0)
                            lo = lastRun;
                        else
                            hi = lastRun;
                        for (Node<K, V> p = f; p != lastRun; p = p.next) {
                            int ph = p.hash;
                            Object pk = p.key;
                            V pv = p.val;
                            if ((ph & n) == 0)
                                lo = new Node<K, V>(ph, pk, pv, lo);
                            else
                                hi = new Node<K, V>(ph, pk, pv, hi);
                        }
                        setTabAt(nextTab, i, lo);
                        setTabAt(nextTab, i + n, hi);
                        setTabAt(tab, i, fwd);
                        advance = true;
                    }
                }
            } else if ((fk = f.key) instanceof TreeBin) {
                TreeBin<K, V> t = (TreeBin<K, V>) fk;
                long stamp = t.writeLock();
                try {
                    if (tabAt(tab, i) == f) {
                        TreeNode<K, V> root;
                        Node<K, V> ln = null, hn = null;
                        if ((root = t.root) != null) {
                            Node<K, V> e, p;
                            TreeNode<K, V> lr, rr;
                            int lh;
                            TreeBin<K, V> lt = null, ht = null;
                            for (lr = root; lr.left != null; lr = lr.left) ;
                            for (rr = root; rr.right != null; rr = rr.right) ;
                            if ((lh = lr.hash) == rr.hash) {
                                if ((lh & n) == 0)
                                    lt = t;
                                else
                                    ht = t;
                            } else {
                                lt = new TreeBin<K, V>();
                                ht = new TreeBin<K, V>();
                                int lc = 0, hc = 0;
                                for (e = t.first; e != null; e = e.next) {
                                    int h = e.hash;
                                    Object k = e.key;
                                    V v = e.val;
                                    if ((h & n) == 0) {
                                        ++lc;
                                        lt.putTreeNode(h, k, v);
                                    } else {
                                        ++hc;
                                        ht.putTreeNode(h, k, v);
                                    }
                                }
                                if (lc < TREE_THRESHOLD) {
                                    for (p = lt.first; p != null; p = p.next) ln = new Node<K, V>(p.hash, p.key, p.val, ln);
                                    lt = null;
                                }
                                if (hc < TREE_THRESHOLD) {
                                    for (p = ht.first; p != null; p = p.next) hn = new Node<K, V>(p.hash, p.key, p.val, hn);
                                    ht = null;
                                }
                            }
                            if (ln == null && lt != null)
                                ln = new Node<K, V>(MOVED, lt, null, null);
                            if (hn == null && ht != null)
                                hn = new Node<K, V>(MOVED, ht, null, null);
                        }
                        setTabAt(nextTab, i, ln);
                        setTabAt(nextTab, i + n, hn);
                        setTabAt(tab, i, fwd);
                        advance = true;
                    }
                } finally {
                    t.unlockWrite(stamp);
                }
            } else
                advance = true;
        }
    }

    final long sumCount() {
        Cell[] as = counterCells;
        Cell a;
        long sum = baseCount;
        if (as != null) {
            for (int i = 0; i < as.length; ++i) {
                if ((a = as[i]) != null)
                    sum += a.value;
            }
        }
        return sum;
    }

    private final void fullAddCount(long x, boolean wasUncontended) {
        int h;
        if ((h = ThreadLocalRandom.getProbe()) == 0) {
            ThreadLocalRandom.localInit();
            h = ThreadLocalRandom.getProbe();
            wasUncontended = true;
        }
        boolean collide = false;
        for (; ; ) {
            Cell[] as;
            Cell a;
            int n;
            long v;
            if ((as = counterCells) != null && (n = as.length) > 0) {
                if ((a = as[(n - 1) & h]) == null) {
                    if (cellsBusy == 0) {
                        Cell r = new Cell(x);
                        if (cellsBusy == 0 && U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                            boolean created = false;
                            try {
                                Cell[] rs;
                                int m, j;
                                if ((rs = counterCells) != null && (m = rs.length) > 0 && rs[j = (m - 1) & h] == null) {
                                    rs[j] = r;
                                    created = true;
                                }
                            } finally {
                                cellsBusy = 0;
                            }
                            if (created)
                                break;
                            continue;
                        }
                    }
                    collide = false;
                } else if (!wasUncontended)
                    wasUncontended = true;
                else if (U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))
                    break;
                else if (counterCells != as || n >= NCPU)
                    collide = false;
                else if (!collide)
                    collide = true;
                else if (cellsBusy == 0 && U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                    try {
                        if (counterCells == as) {
                            Cell[] rs = new Cell[n << 1];
                            for (int i = 0; i < n; ++i) rs[i] = as[i];
                            counterCells = rs;
                        }
                    } finally {
                        cellsBusy = 0;
                    }
                    collide = false;
                    continue;
                }
                h = ThreadLocalRandom.advanceProbe(h);
            } else if (cellsBusy == 0 && counterCells == as && U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                boolean init = false;
                try {
                    if (counterCells == as) {
                        Cell[] rs = new Cell[2];
                        rs[h & 1] = new Cell(x);
                        counterCells = rs;
                        init = true;
                    }
                } finally {
                    cellsBusy = 0;
                }
                if (init)
                    break;
            } else if (U.compareAndSwapLong(this, BASECOUNT, v = baseCount, v + x))
                break;
        }
    }

    static class Traverser<K, V> {

        Node<K, V>[] tab;

        Node<K, V> next;

        int index;

        int baseIndex;

        int baseLimit;

        final int baseSize;

        Traverser(Node<K, V>[] tab, int size, int index, int limit) {
            this.tab = tab;
            this.baseSize = size;
            this.baseIndex = this.index = index;
            this.baseLimit = limit;
            this.next = null;
        }

        final Node<K, V> advance() {
            Node<K, V> e;
            if ((e = next) != null)
                e = e.next;
            for (; ; ) {
                Node<K, V>[] t;
                int i, n;
                Object ek;
                if (e != null)
                    return next = e;
                if (baseIndex >= baseLimit || (t = tab) == null || (n = t.length) <= (i = index) || i < 0)
                    return next = null;
                if ((e = tabAt(t, index)) != null && e.hash < 0) {
                    if ((ek = e.key) instanceof TreeBin)
                        e = ((TreeBin<K, V>) ek).first;
                    else {
                        tab = (Node<K, V>[]) ek;
                        e = null;
                        continue;
                    }
                }
                if ((index += baseSize) >= n)
                    index = ++baseIndex;
            }
        }
    }

    static class BaseIterator<K, V> extends Traverser<K, V> {

        final ConcurrentHashMap<K, V> map;

        Node<K, V> lastReturned;

        BaseIterator(Node<K, V>[] tab, int size, int index, int limit, ConcurrentHashMap<K, V> map) {
            super(tab, size, index, limit);
            this.map = map;
            advance();
        }

        public final boolean hasNext() {
            return next != null;
        }

        public final boolean hasMoreElements() {
            return next != null;
        }

        public final void remove() {
            Node<K, V> p;
            if ((p = lastReturned) == null)
                throw new IllegalStateException();
            lastReturned = null;
            map.internalReplace((K) p.key, null, null);
        }
    }

    static final class KeyIterator<K, V> extends BaseIterator<K, V> implements Iterator<K>, Enumeration<K> {

        KeyIterator(Node<K, V>[] tab, int index, int size, int limit, ConcurrentHashMap<K, V> map) {
            super(tab, index, size, limit, map);
        }

        public final K next() {
            Node<K, V> p;
            if ((p = next) == null)
                throw new NoSuchElementException();
            K k = (K) p.key;
            lastReturned = p;
            advance();
            return k;
        }

        public final K nextElement() {
            return next();
        }
    }

    static final class ValueIterator<K, V> extends BaseIterator<K, V> implements Iterator<V>, Enumeration<V> {

        ValueIterator(Node<K, V>[] tab, int index, int size, int limit, ConcurrentHashMap<K, V> map) {
            super(tab, index, size, limit, map);
        }

        public final V next() {
            Node<K, V> p;
            if ((p = next) == null)
                throw new NoSuchElementException();
            V v = p.val;
            lastReturned = p;
            advance();
            return v;
        }

        public final V nextElement() {
            return next();
        }
    }

    static final class EntryIterator<K, V> extends BaseIterator<K, V> implements Iterator<Map.Entry<K, V>> {

        EntryIterator(Node<K, V>[] tab, int index, int size, int limit, ConcurrentHashMap<K, V> map) {
            super(tab, index, size, limit, map);
        }

        public final Map.Entry<K, V> next() {
            Node<K, V> p;
            if ((p = next) == null)
                throw new NoSuchElementException();
            K k = (K) p.key;
            V v = p.val;
            lastReturned = p;
            advance();
            return new MapEntry<K, V>(k, v, map);
        }
    }

    static final class KeySpliterator<K, V> extends Traverser<K, V> implements Spliterator<K> {

        long est;

        KeySpliterator(Node<K, V>[] tab, int size, int index, int limit, long est) {
            super(tab, size, index, limit);
            this.est = est;
        }

        public Spliterator<K> trySplit() {
            int i, f, h;
            return (h = ((i = baseIndex) + (f = baseLimit)) >>> 1) <= i ? null : new KeySpliterator<K, V>(tab, baseSize, baseLimit = h, f, est >>>= 1);
        }

        public void forEachRemaining(Consumer<? super K> action) {
            if (action == null)
                throw new NullPointerException();
            for (Node<K, V> p; (p = advance()) != null; ) action.accept((K) p.key);
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            if (action == null)
                throw new NullPointerException();
            Node<K, V> p;
            if ((p = advance()) == null)
                return false;
            action.accept((K) p.key);
            return true;
        }

        public long estimateSize() {
            return est;
        }

        public int characteristics() {
            return Spliterator.DISTINCT | Spliterator.CONCURRENT | Spliterator.NONNULL;
        }
    }

    static final class ValueSpliterator<K, V> extends Traverser<K, V> implements Spliterator<V> {

        long est;

        ValueSpliterator(Node<K, V>[] tab, int size, int index, int limit, long est) {
            super(tab, size, index, limit);
            this.est = est;
        }

        public Spliterator<V> trySplit() {
            int i, f, h;
            return (h = ((i = baseIndex) + (f = baseLimit)) >>> 1) <= i ? null : new ValueSpliterator<K, V>(tab, baseSize, baseLimit = h, f, est >>>= 1);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            if (action == null)
                throw new NullPointerException();
            for (Node<K, V> p; (p = advance()) != null; ) action.accept(p.val);
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            if (action == null)
                throw new NullPointerException();
            Node<K, V> p;
            if ((p = advance()) == null)
                return false;
            action.accept(p.val);
            return true;
        }

        public long estimateSize() {
            return est;
        }

        public int characteristics() {
            return Spliterator.CONCURRENT | Spliterator.NONNULL;
        }
    }

    static final class EntrySpliterator<K, V> extends Traverser<K, V> implements Spliterator<Map.Entry<K, V>> {

        final ConcurrentHashMap<K, V> map;

        long est;

        EntrySpliterator(Node<K, V>[] tab, int size, int index, int limit, long est, ConcurrentHashMap<K, V> map) {
            super(tab, size, index, limit);
            this.map = map;
            this.est = est;
        }

        public Spliterator<Map.Entry<K, V>> trySplit() {
            int i, f, h;
            return (h = ((i = baseIndex) + (f = baseLimit)) >>> 1) <= i ? null : new EntrySpliterator<K, V>(tab, baseSize, baseLimit = h, f, est >>>= 1, map);
        }

        public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
            if (action == null)
                throw new NullPointerException();
            for (Node<K, V> p; (p = advance()) != null; ) action.accept(new MapEntry<K, V>((K) p.key, p.val, map));
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
            if (action == null)
                throw new NullPointerException();
            Node<K, V> p;
            if ((p = advance()) == null)
                return false;
            action.accept(new MapEntry<K, V>((K) p.key, p.val, map));
            return true;
        }

        public long estimateSize() {
            return est;
        }

        public int characteristics() {
            return Spliterator.DISTINCT | Spliterator.CONCURRENT | Spliterator.NONNULL;
        }
    }

    public ConcurrentHashMap() {
    }

    public ConcurrentHashMap(int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException();
        int cap = ((initialCapacity >= (MAXIMUM_CAPACITY >>> 1)) ? MAXIMUM_CAPACITY : tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1));
        this.sizeCtl = cap;
    }

    public ConcurrentHashMap(Map<? extends K, ? extends V> m) {
        this.sizeCtl = DEFAULT_CAPACITY;
        internalPutAll(m);
    }

    public ConcurrentHashMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, 1);
    }

    public ConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        if (!(loadFactor > 0.0f) || initialCapacity < 0 || concurrencyLevel <= 0)
            throw new IllegalArgumentException();
        if (initialCapacity < concurrencyLevel)
            initialCapacity = concurrencyLevel;
        long size = (long) (1.0 + (long) initialCapacity / loadFactor);
        int cap = (size >= (long) MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : tableSizeFor((int) size);
        this.sizeCtl = cap;
    }

    public static <K> KeySetView<K, Boolean> newKeySet() {
        return new KeySetView<K, Boolean>(new ConcurrentHashMap<K, Boolean>(), Boolean.TRUE);
    }

    public static <K> KeySetView<K, Boolean> newKeySet(int initialCapacity) {
        return new KeySetView<K, Boolean>(new ConcurrentHashMap<K, Boolean>(initialCapacity), Boolean.TRUE);
    }

    public boolean isEmpty() {
        return sumCount() <= 0L;
    }

    public int size() {
        long n = sumCount();
        return ((n < 0L) ? 0 : (n > (long) Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) n);
    }

    public long mappingCount() {
        long n = sumCount();
        return (n < 0L) ? 0L : n;
    }

    public V get(Object key) {
        return internalGet(key);
    }

    public V getOrDefault(Object key, V defaultValue) {
        V v;
        return (v = internalGet(key)) == null ? defaultValue : v;
    }

    public boolean containsKey(Object key) {
        return internalGet(key) != null;
    }

    public boolean containsValue(Object value) {
        if (value == null)
            throw new NullPointerException();
        Node<K, V>[] t;
        if ((t = table) != null) {
            Traverser<K, V> it = new Traverser<K, V>(t, t.length, 0, t.length);
            for (Node<K, V> p; (p = it.advance()) != null; ) {
                V v;
                if ((v = p.val) == value || value.equals(v))
                    return true;
            }
        }
        return false;
    }

    public boolean contains(Object value) {
        return containsValue(value);
    }

    public V put(K key, V value) {
        return internalPut(key, value, false);
    }

    public V putIfAbsent(K key, V value) {
        return internalPut(key, value, true);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        internalPutAll(m);
    }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return internalComputeIfAbsent(key, mappingFunction);
    }

    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return internalCompute(key, true, remappingFunction);
    }

    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return internalCompute(key, false, remappingFunction);
    }

    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return internalMerge(key, value, remappingFunction);
    }

    public V remove(Object key) {
        return internalReplace(key, null, null);
    }

    public boolean remove(Object key, Object value) {
        if (key == null)
            throw new NullPointerException();
        return value != null && internalReplace(key, null, value) != null;
    }

    public boolean replace(K key, V oldValue, V newValue) {
        if (key == null || oldValue == null || newValue == null)
            throw new NullPointerException();
        return internalReplace(key, newValue, oldValue) != null;
    }

    public V replace(K key, V value) {
        if (key == null || value == null)
            throw new NullPointerException();
        return internalReplace(key, value, null);
    }

    public void clear() {
        internalClear();
    }

    public KeySetView<K, V> keySet() {
        KeySetView<K, V> ks = keySet;
        return (ks != null) ? ks : (keySet = new KeySetView<K, V>(this, null));
    }

    public KeySetView<K, V> keySet(V mappedValue) {
        if (mappedValue == null)
            throw new NullPointerException();
        return new KeySetView<K, V>(this, mappedValue);
    }

    public Collection<V> values() {
        ValuesView<K, V> vs = values;
        return (vs != null) ? vs : (values = new ValuesView<K, V>(this));
    }

    public Set<Map.Entry<K, V>> entrySet() {
        EntrySetView<K, V> es = entrySet;
        return (es != null) ? es : (entrySet = new EntrySetView<K, V>(this));
    }

    public Enumeration<K> keys() {
        Node<K, V>[] t;
        int f = (t = table) == null ? 0 : t.length;
        return new KeyIterator<K, V>(t, f, 0, f, this);
    }

    public Enumeration<V> elements() {
        Node<K, V>[] t;
        int f = (t = table) == null ? 0 : t.length;
        return new ValueIterator<K, V>(t, f, 0, f, this);
    }

    public int hashCode() {
        int h = 0;
        Node<K, V>[] t;
        if ((t = table) != null) {
            Traverser<K, V> it = new Traverser<K, V>(t, t.length, 0, t.length);
            for (Node<K, V> p; (p = it.advance()) != null; ) h += p.key.hashCode() ^ p.val.hashCode();
        }
        return h;
    }

    public String toString() {
        Node<K, V>[] t;
        int f = (t = table) == null ? 0 : t.length;
        Traverser<K, V> it = new Traverser<K, V>(t, f, 0, f);
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        Node<K, V> p;
        if ((p = it.advance()) != null) {
            for (; ; ) {
                K k = (K) p.key;
                V v = p.val;
                sb.append(k == this ? "(this Map)" : k);
                sb.append('=');
                sb.append(v == this ? "(this Map)" : v);
                if ((p = it.advance()) == null)
                    break;
                sb.append(',').append(' ');
            }
        }
        return sb.append('}').toString();
    }

    public boolean equals(Object o) {
        if (o != this) {
            if (!(o instanceof Map))
                return false;
            Map<?, ?> m = (Map<?, ?>) o;
            Node<K, V>[] t;
            int f = (t = table) == null ? 0 : t.length;
            Traverser<K, V> it = new Traverser<K, V>(t, f, 0, f);
            for (Node<K, V> p; (p = it.advance()) != null; ) {
                V val = p.val;
                Object v = m.get(p.key);
                if (v == null || (v != val && !v.equals(val)))
                    return false;
            }
            for (Map.Entry<?, ?> e : m.entrySet()) {
                Object mk, mv, v;
                if ((mk = e.getKey()) == null || (mv = e.getValue()) == null || (v = internalGet(mk)) == null || (mv != v && !mv.equals(v)))
                    return false;
            }
        }
        return true;
    }

    static class Segment<K, V> extends ReentrantLock implements Serializable {

        private static final long serialVersionUID = 2249069246763182397L;

        final float loadFactor;

        Segment(float lf) {
            this.loadFactor = lf;
        }
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        int sshift = 0;
        int ssize = 1;
        while (ssize < DEFAULT_CONCURRENCY_LEVEL) {
            ++sshift;
            ssize <<= 1;
        }
        int segmentShift = 32 - sshift;
        int segmentMask = ssize - 1;
        Segment<K, V>[] segments = (Segment<K, V>[]) new Segment<?, ?>[DEFAULT_CONCURRENCY_LEVEL];
        for (int i = 0; i < segments.length; ++i) segments[i] = new Segment<K, V>(LOAD_FACTOR);
        s.putFields().put("segments", segments);
        s.putFields().put("segmentShift", segmentShift);
        s.putFields().put("segmentMask", segmentMask);
        s.writeFields();
        Node<K, V>[] t;
        if ((t = table) != null) {
            Traverser<K, V> it = new Traverser<K, V>(t, t.length, 0, t.length);
            for (Node<K, V> p; (p = it.advance()) != null; ) {
                s.writeObject(p.key);
                s.writeObject(p.val);
            }
        }
        s.writeObject(null);
        s.writeObject(null);
        segments = null;
    }

    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        long size = 0L;
        Node<K, V> p = null;
        for (; ; ) {
            K k = (K) s.readObject();
            V v = (V) s.readObject();
            if (k != null && v != null) {
                int h = spread(k.hashCode());
                p = new Node<K, V>(h, k, v, p);
                ++size;
            } else
                break;
        }
        if (p != null) {
            boolean init = false;
            int n;
            if (size >= (long) (MAXIMUM_CAPACITY >>> 1))
                n = MAXIMUM_CAPACITY;
            else {
                int sz = (int) size;
                n = tableSizeFor(sz + (sz >>> 1) + 1);
            }
            int sc = sizeCtl;
            boolean collide = false;
            if (n > sc && U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
                try {
                    if (table == null) {
                        init = true;
                        Node<K, V>[] tab = (Node<K, V>[]) new Node[n];
                        int mask = n - 1;
                        while (p != null) {
                            int j = p.hash & mask;
                            Node<K, V> next = p.next;
                            Node<K, V> q = p.next = tabAt(tab, j);
                            setTabAt(tab, j, p);
                            if (!collide && q != null && q.hash == p.hash)
                                collide = true;
                            p = next;
                        }
                        table = tab;
                        addCount(size, -1);
                        sc = n - (n >>> 2);
                    }
                } finally {
                    sizeCtl = sc;
                }
                if (collide) {
                    Node<K, V>[] tab = table;
                    for (int i = 0; i < tab.length; ++i) {
                        int c = 0;
                        for (Node<K, V> e = tabAt(tab, i); e != null; e = e.next) {
                            if (++c > TREE_THRESHOLD && (e.key instanceof Comparable)) {
                                replaceWithTreeBin(tab, i, e.key);
                                break;
                            }
                        }
                    }
                }
            }
            if (!init) {
                while (p != null) {
                    internalPut((K) p.key, p.val, false);
                    p = p.next;
                }
            }
        }
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        if (action == null)
            throw new NullPointerException();
        Node<K, V>[] t;
        if ((t = table) != null) {
            Traverser<K, V> it = new Traverser<K, V>(t, t.length, 0, t.length);
            for (Node<K, V> p; (p = it.advance()) != null; ) {
                action.accept((K) p.key, p.val);
            }
        }
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        if (function == null)
            throw new NullPointerException();
        Node<K, V>[] t;
        if ((t = table) != null) {
            Traverser<K, V> it = new Traverser<K, V>(t, t.length, 0, t.length);
            for (Node<K, V> p; (p = it.advance()) != null; ) {
                K k = (K) p.key;
                internalPut(k, function.apply(k, p.val), false);
            }
        }
    }

    final int batchFor(long b) {
        long n;
        if (b == Long.MAX_VALUE || (n = sumCount()) <= 1L || n < b)
            return 0;
        int sp = ForkJoinPool.getCommonPoolParallelism() << 2;
        return (b <= 0L || (n /= b) >= sp) ? sp : (int) n;
    }

    public void forEach(long parallelismThreshold, BiConsumer<? super K, ? super V> action) {
        if (action == null)
            throw new NullPointerException();
        new ForEachMappingTask<K, V>(null, batchFor(parallelismThreshold), 0, 0, table, action).invoke();
    }

    public <U> void forEach(long parallelismThreshold, BiFunction<? super K, ? super V, ? extends U> transformer, Consumer<? super U> action) {
        if (transformer == null || action == null)
            throw new NullPointerException();
        new ForEachTransformedMappingTask<K, V, U>(null, batchFor(parallelismThreshold), 0, 0, table, transformer, action).invoke();
    }

    public <U> U search(long parallelismThreshold, BiFunction<? super K, ? super V, ? extends U> searchFunction) {
        if (searchFunction == null)
            throw new NullPointerException();
        return new SearchMappingsTask<K, V, U>(null, batchFor(parallelismThreshold), 0, 0, table, searchFunction, new AtomicReference<U>()).invoke();
    }

    public <U> U reduce(long parallelismThreshold, BiFunction<? super K, ? super V, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceMappingsTask<K, V, U>(null, batchFor(parallelismThreshold), 0, 0, table, null, transformer, reducer).invoke();
    }

    public double reduceToDoubleIn(long parallelismThreshold, ToDoubleBiFunction<? super K, ? super V> transformer, double basis, DoubleBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceMappingsToDoubleTask<K, V>(null, batchFor(parallelismThreshold), 0, 0, table, null, transformer, basis, reducer).invoke();
    }

    public long reduceToLong(long parallelismThreshold, ToLongBiFunction<? super K, ? super V> transformer, long basis, LongBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceMappingsToLongTask<K, V>(null, batchFor(parallelismThreshold), 0, 0, table, null, transformer, basis, reducer).invoke();
    }

    public int reduceToInt(long parallelismThreshold, ToIntBiFunction<? super K, ? super V> transformer, int basis, IntBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceMappingsToIntTask<K, V>(null, batchFor(parallelismThreshold), 0, 0, table, null, transformer, basis, reducer).invoke();
    }

    public void forEachKey(long parallelismThreshold, Consumer<? super K> action) {
        if (action == null)
            throw new NullPointerException();
        new ForEachKeyTask<K, V>(null, batchFor(parallelismThreshold), 0, 0, table, action).invoke();
    }

    public <U> void forEachKey(long parallelismThreshold, Function<? super K, ? extends U> transformer, Consumer<? super U> action) {
        if (transformer == null || action == null)
            throw new NullPointerException();
        new ForEachTransformedKeyTask<K, V, U>(null, batchFor(parallelismThreshold), 0, 0, table, transformer, action).invoke();
    }

    public <U> U searchKeys(long parallelismThreshold, Function<? super K, ? extends U> searchFunction) {
        if (searchFunction == null)
            throw new NullPointerException();
        return new SearchKeysTask<K, V, U>(null, batchFor(parallelismThreshold), 0, 0, table, searchFunction, new AtomicReference<U>()).invoke();
    }

    public K reduceKeys(long parallelismThreshold, BiFunction<? super K, ? super K, ? extends K> reducer) {
        if (reducer == null)
            throw new NullPointerException();
        return new ReduceKeysTask<K, V>(null, batchFor(parallelismThreshold), 0, 0, table, null, reducer).invoke();
    }

    public <U> U reduceKeys(long parallelismThreshold, Function<? super K, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceKeysTask<K, V, U>(null, batchFor(parallelismThreshold), 0, 0, table, null, transformer, reducer).invoke();
    }

    public double reduceKeysToDouble(long parallelismThreshold, ToDoubleFunction<? super K> transformer, double basis, DoubleBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceKeysToDoubleTask<K, V>(null, batchFor(parallelismThreshold), 0, 0, table, null, transformer, basis, reducer).invoke();
    }

    public long reduceKeysToLong(long parallelismThreshold, ToLongFunction<? super K> transformer, long basis, LongBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceKeysToLongTask<K, V>(null, batchFor(parallelismThreshold), 0, 0, table, null, transformer, basis, reducer).invoke();
    }

    public int reduceKeysToInt(long parallelismThreshold, ToIntFunction<? super K> transformer, int basis, IntBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceKeysToIntTask<K, V>(null, batchFor(parallelismThreshold), 0, 0, table, null, transformer, basis, reducer).invoke();
    }

    public void forEachValue(long parallelismThreshold, Consumer<? super V> action) {
        if (action == null)
            throw new NullPointerException();
        new ForEachValueTask<K, V>(null, batchFor(parallelismThreshold), 0, 0, table, action).invoke();
    }

    public <U> void forEachValue(long parallelismThreshold, Function<? super V, ? extends U> transformer, Consumer<? super U> action) {
        if (transformer == null || action == null)
            throw new NullPointerException();
        new ForEachTransformedValueTask<K, V, U>(null, batchFor(parallelismThreshold), 0, 0, table, transformer, action).invoke();
    }

    public <U> U searchValues(long parallelismThreshold, Function<? super V, ? extends U> searchFunction) {
        if (searchFunction == null)
            throw new NullPointerException();
        return new SearchValuesTask<K, V, U>(null, batchFor(parallelismThreshold), 0, 0, table, searchFunction, new AtomicReference<U>()).invoke();
    }

    public V reduceValues(long parallelismThreshold, BiFunction<? super V, ? super V, ? extends V> reducer) {
        if (reducer == null)
            throw new NullPointerException();
        return new ReduceValuesTask<K, V>(null, batchFor(parallelismThreshold), 0, 0, table, null, reducer).invoke();
    }

    public <U> U reduceValues(long parallelismThreshold, Function<? super V, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceValuesTask<K, V, U>(null, batchFor(parallelismThreshold), 0, 0, table, null, transformer, reducer).invoke();
    }

    public double reduceValuesToDouble(long parallelismThreshold, ToDoubleFunction<? super V> transformer, double basis, DoubleBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceValuesToDoubleTask<K, V>(null, batchFor(parallelismThreshold), 0, 0, table, null, transformer, basis, reducer).invoke();
    }

    public long reduceValuesToLong(long parallelismThreshold, ToLongFunction<? super V> transformer, long basis, LongBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceValuesToLongTask<K, V>(null, batchFor(parallelismThreshold), 0, 0, table, null, transformer, basis, reducer).invoke();
    }

    public int reduceValuesToInt(long parallelismThreshold, ToIntFunction<? super V> transformer, int basis, IntBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceValuesToIntTask<K, V>(null, batchFor(parallelismThreshold), 0, 0, table, null, transformer, basis, reducer).invoke();
    }

    public void forEachEntry(long parallelismThreshold, Consumer<? super Map.Entry<K, V>> action) {
        if (action == null)
            throw new NullPointerException();
        new ForEachEntryTask<K, V>(null, batchFor(parallelismThreshold), 0, 0, table, action).invoke();
    }

    public <U> void forEachEntry(long parallelismThreshold, Function<Map.Entry<K, V>, ? extends U> transformer, Consumer<? super U> action) {
        if (transformer == null || action == null)
            throw new NullPointerException();
        new ForEachTransformedEntryTask<K, V, U>(null, batchFor(parallelismThreshold), 0, 0, table, transformer, action).invoke();
    }

    public <U> U searchEntries(long parallelismThreshold, Function<Map.Entry<K, V>, ? extends U> searchFunction) {
        if (searchFunction == null)
            throw new NullPointerException();
        return new SearchEntriesTask<K, V, U>(null, batchFor(parallelismThreshold), 0, 0, table, searchFunction, new AtomicReference<U>()).invoke();
    }

    public Map.Entry<K, V> reduceEntries(long parallelismThreshold, BiFunction<Map.Entry<K, V>, Map.Entry<K, V>, ? extends Map.Entry<K, V>> reducer) {
        if (reducer == null)
            throw new NullPointerException();
        return new ReduceEntriesTask<K, V>(null, batchFor(parallelismThreshold), 0, 0, table, null, reducer).invoke();
    }

    public <U> U reduceEntries(long parallelismThreshold, Function<Map.Entry<K, V>, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceEntriesTask<K, V, U>(null, batchFor(parallelismThreshold), 0, 0, table, null, transformer, reducer).invoke();
    }

    public double reduceEntriesToDouble(long parallelismThreshold, ToDoubleFunction<Map.Entry<K, V>> transformer, double basis, DoubleBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceEntriesToDoubleTask<K, V>(null, batchFor(parallelismThreshold), 0, 0, table, null, transformer, basis, reducer).invoke();
    }

    public long reduceEntriesToLong(long parallelismThreshold, ToLongFunction<Map.Entry<K, V>> transformer, long basis, LongBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceEntriesToLongTask<K, V>(null, batchFor(parallelismThreshold), 0, 0, table, null, transformer, basis, reducer).invoke();
    }

    public int reduceEntriesToInt(long parallelismThreshold, ToIntFunction<Map.Entry<K, V>> transformer, int basis, IntBinaryOperator reducer) {
        if (transformer == null || reducer == null)
            throw new NullPointerException();
        return new MapReduceEntriesToIntTask<K, V>(null, batchFor(parallelismThreshold), 0, 0, table, null, transformer, basis, reducer).invoke();
    }

    abstract static class CollectionView<K, V, E> implements Collection<E>, java.io.Serializable {

        private static final long serialVersionUID = 7249069246763182397L;

        final ConcurrentHashMap<K, V> map;

        CollectionView(ConcurrentHashMap<K, V> map) {
            this.map = map;
        }

        public ConcurrentHashMap<K, V> getMap() {
            return map;
        }

        public final void clear() {
            map.clear();
        }

        public final int size() {
            return map.size();
        }

        public final boolean isEmpty() {
            return map.isEmpty();
        }

        public abstract Iterator<E> iterator();

        public abstract boolean contains(Object o);

        public abstract boolean remove(Object o);

        private static final String oomeMsg = "Required array size too large";

        public final Object[] toArray() {
            long sz = map.mappingCount();
            if (sz > MAX_ARRAY_SIZE)
                throw new OutOfMemoryError(oomeMsg);
            int n = (int) sz;
            Object[] r = new Object[n];
            int i = 0;
            for (E e : this) {
                if (i == n) {
                    if (n >= MAX_ARRAY_SIZE)
                        throw new OutOfMemoryError(oomeMsg);
                    if (n >= MAX_ARRAY_SIZE - (MAX_ARRAY_SIZE >>> 1) - 1)
                        n = MAX_ARRAY_SIZE;
                    else
                        n += (n >>> 1) + 1;
                    r = Arrays.copyOf(r, n);
                }
                r[i++] = e;
            }
            return (i == n) ? r : Arrays.copyOf(r, i);
        }

        public final <T> T[] toArray(T[] a) {
            long sz = map.mappingCount();
            if (sz > MAX_ARRAY_SIZE)
                throw new OutOfMemoryError(oomeMsg);
            int m = (int) sz;
            T[] r = (a.length >= m) ? a : (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), m);
            int n = r.length;
            int i = 0;
            for (E e : this) {
                if (i == n) {
                    if (n >= MAX_ARRAY_SIZE)
                        throw new OutOfMemoryError(oomeMsg);
                    if (n >= MAX_ARRAY_SIZE - (MAX_ARRAY_SIZE >>> 1) - 1)
                        n = MAX_ARRAY_SIZE;
                    else
                        n += (n >>> 1) + 1;
                    r = Arrays.copyOf(r, n);
                }
                r[i++] = (T) e;
            }
            if (a == r && i < n) {
                r[i] = null;
                return r;
            }
            return (i == n) ? r : Arrays.copyOf(r, i);
        }

        public final String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            Iterator<E> it = iterator();
            if (it.hasNext()) {
                for (; ; ) {
                    Object e = it.next();
                    sb.append(e == this ? "(this Collection)" : e);
                    if (!it.hasNext())
                        break;
                    sb.append(',').append(' ');
                }
            }
            return sb.append(']').toString();
        }

        public final boolean containsAll(Collection<?> c) {
            if (c != this) {
                for (Object e : c) {
                    if (e == null || !contains(e))
                        return false;
                }
            }
            return true;
        }

        public final boolean removeAll(Collection<?> c) {
            boolean modified = false;
            for (Iterator<E> it = iterator(); it.hasNext(); ) {
                if (c.contains(it.next())) {
                    it.remove();
                    modified = true;
                }
            }
            return modified;
        }

        public final boolean retainAll(Collection<?> c) {
            boolean modified = false;
            for (Iterator<E> it = iterator(); it.hasNext(); ) {
                if (!c.contains(it.next())) {
                    it.remove();
                    modified = true;
                }
            }
            return modified;
        }
    }

    public static class KeySetView<K, V> extends CollectionView<K, V, K> implements Set<K>, java.io.Serializable {

        private static final long serialVersionUID = 7249069246763182397L;

        private final V value;

        KeySetView(ConcurrentHashMap<K, V> map, V value) {
            super(map);
            this.value = value;
        }

        public V getMappedValue() {
            return value;
        }

        public boolean contains(Object o) {
            return map.containsKey(o);
        }

        public boolean remove(Object o) {
            return map.remove(o) != null;
        }

        public Iterator<K> iterator() {
            Node<K, V>[] t;
            ConcurrentHashMap<K, V> m = map;
            int f = (t = m.table) == null ? 0 : t.length;
            return new KeyIterator<K, V>(t, f, 0, f, m);
        }

        public boolean add(K e) {
            V v;
            if ((v = value) == null)
                throw new UnsupportedOperationException();
            return map.internalPut(e, v, true) == null;
        }

        public boolean addAll(Collection<? extends K> c) {
            boolean added = false;
            V v;
            if ((v = value) == null)
                throw new UnsupportedOperationException();
            for (K e : c) {
                if (map.internalPut(e, v, true) == null)
                    added = true;
            }
            return added;
        }

        public int hashCode() {
            int h = 0;
            for (K e : this) h += e.hashCode();
            return h;
        }

        public boolean equals(Object o) {
            Set<?> c;
            return ((o instanceof Set) && ((c = (Set<?>) o) == this || (containsAll(c) && c.containsAll(this))));
        }

        public Spliterator<K> spliterator() {
            Node<K, V>[] t;
            ConcurrentHashMap<K, V> m = map;
            long n = m.sumCount();
            int f = (t = m.table) == null ? 0 : t.length;
            return new KeySpliterator<K, V>(t, f, 0, f, n < 0L ? 0L : n);
        }

        public void forEach(Consumer<? super K> action) {
            if (action == null)
                throw new NullPointerException();
            Node<K, V>[] t;
            if ((t = map.table) != null) {
                Traverser<K, V> it = new Traverser<K, V>(t, t.length, 0, t.length);
                for (Node<K, V> p; (p = it.advance()) != null; ) action.accept((K) p.key);
            }
        }
    }

    static final class ValuesView<K, V> extends CollectionView<K, V, V> implements Collection<V>, java.io.Serializable {

        private static final long serialVersionUID = 2249069246763182397L;

        ValuesView(ConcurrentHashMap<K, V> map) {
            super(map);
        }

        public final boolean contains(Object o) {
            return map.containsValue(o);
        }

        public final boolean remove(Object o) {
            if (o != null) {
                for (Iterator<V> it = iterator(); it.hasNext(); ) {
                    if (o.equals(it.next())) {
                        it.remove();
                        return true;
                    }
                }
            }
            return false;
        }

        public final Iterator<V> iterator() {
            ConcurrentHashMap<K, V> m = map;
            Node<K, V>[] t;
            int f = (t = m.table) == null ? 0 : t.length;
            return new ValueIterator<K, V>(t, f, 0, f, m);
        }

        public final boolean add(V e) {
            throw new UnsupportedOperationException();
        }

        public final boolean addAll(Collection<? extends V> c) {
            throw new UnsupportedOperationException();
        }

        public Spliterator<V> spliterator() {
            Node<K, V>[] t;
            ConcurrentHashMap<K, V> m = map;
            long n = m.sumCount();
            int f = (t = m.table) == null ? 0 : t.length;
            return new ValueSpliterator<K, V>(t, f, 0, f, n < 0L ? 0L : n);
        }

        public void forEach(Consumer<? super V> action) {
            if (action == null)
                throw new NullPointerException();
            Node<K, V>[] t;
            if ((t = map.table) != null) {
                Traverser<K, V> it = new Traverser<K, V>(t, t.length, 0, t.length);
                for (Node<K, V> p; (p = it.advance()) != null; ) action.accept(p.val);
            }
        }
    }

    static final class EntrySetView<K, V> extends CollectionView<K, V, Map.Entry<K, V>> implements Set<Map.Entry<K, V>>, java.io.Serializable {

        private static final long serialVersionUID = 2249069246763182397L;

        EntrySetView(ConcurrentHashMap<K, V> map) {
            super(map);
        }

        public boolean contains(Object o) {
            Object k, v, r;
            Map.Entry<?, ?> e;
            return ((o instanceof Map.Entry) && (k = (e = (Map.Entry<?, ?>) o).getKey()) != null && (r = map.get(k)) != null && (v = e.getValue()) != null && (v == r || v.equals(r)));
        }

        public boolean remove(Object o) {
            Object k, v;
            Map.Entry<?, ?> e;
            return ((o instanceof Map.Entry) && (k = (e = (Map.Entry<?, ?>) o).getKey()) != null && (v = e.getValue()) != null && map.remove(k, v));
        }

        public Iterator<Map.Entry<K, V>> iterator() {
            ConcurrentHashMap<K, V> m = map;
            Node<K, V>[] t;
            int f = (t = m.table) == null ? 0 : t.length;
            return new EntryIterator<K, V>(t, f, 0, f, m);
        }

        public boolean add(Entry<K, V> e) {
            return map.internalPut(e.getKey(), e.getValue(), false) == null;
        }

        public boolean addAll(Collection<? extends Entry<K, V>> c) {
            boolean added = false;
            for (Entry<K, V> e : c) {
                if (add(e))
                    added = true;
            }
            return added;
        }

        public final int hashCode() {
            int h = 0;
            Node<K, V>[] t;
            if ((t = map.table) != null) {
                Traverser<K, V> it = new Traverser<K, V>(t, t.length, 0, t.length);
                for (Node<K, V> p; (p = it.advance()) != null; ) {
                    h += p.hashCode();
                }
            }
            return h;
        }

        public final boolean equals(Object o) {
            Set<?> c;
            return ((o instanceof Set) && ((c = (Set<?>) o) == this || (containsAll(c) && c.containsAll(this))));
        }

        public Spliterator<Map.Entry<K, V>> spliterator() {
            Node<K, V>[] t;
            ConcurrentHashMap<K, V> m = map;
            long n = m.sumCount();
            int f = (t = m.table) == null ? 0 : t.length;
            return new EntrySpliterator<K, V>(t, f, 0, f, n < 0L ? 0L : n, m);
        }

        public void forEach(Consumer<? super Map.Entry<K, V>> action) {
            if (action == null)
                throw new NullPointerException();
            Node<K, V>[] t;
            if ((t = map.table) != null) {
                Traverser<K, V> it = new Traverser<K, V>(t, t.length, 0, t.length);
                for (Node<K, V> p; (p = it.advance()) != null; ) action.accept(new MapEntry<K, V>((K) p.key, p.val, map));
            }
        }
    }

    abstract static class BulkTask<K, V, R> extends CountedCompleter<R> {

        Node<K, V>[] tab;

        Node<K, V> next;

        int index;

        int baseIndex;

        int baseLimit;

        final int baseSize;

        int batch;

        BulkTask(BulkTask<K, V, ?> par, int b, int i, int f, Node<K, V>[] t) {
            super(par);
            this.batch = b;
            this.index = this.baseIndex = i;
            if ((this.tab = t) == null)
                this.baseSize = this.baseLimit = 0;
            else if (par == null)
                this.baseSize = this.baseLimit = t.length;
            else {
                this.baseLimit = f;
                this.baseSize = par.baseSize;
            }
        }

        final Node<K, V> advance() {
            Node<K, V> e;
            if ((e = next) != null)
                e = e.next;
            for (; ; ) {
                Node<K, V>[] t;
                int i, n;
                Object ek;
                if (e != null)
                    return next = e;
                if (baseIndex >= baseLimit || (t = tab) == null || (n = t.length) <= (i = index) || i < 0)
                    return next = null;
                if ((e = tabAt(t, index)) != null && e.hash < 0) {
                    if ((ek = e.key) instanceof TreeBin)
                        e = ((TreeBin<K, V>) ek).first;
                    else {
                        tab = (Node<K, V>[]) ek;
                        e = null;
                        continue;
                    }
                }
                if ((index += baseSize) >= n)
                    index = ++baseIndex;
            }
        }
    }

    static final class ForEachKeyTask<K, V> extends BulkTask<K, V, Void> {

        final Consumer<? super K> action;

        ForEachKeyTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Consumer<? super K> action) {
            super(p, b, i, f, t);
            this.action = action;
        }

        public final void compute() {
            final Consumer<? super K> action;
            if ((action = this.action) != null) {
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    new ForEachKeyTask<K, V>(this, batch >>>= 1, baseLimit = h, f, tab, action).fork();
                }
                for (Node<K, V> p; (p = advance()) != null; ) action.accept((K) p.key);
                propagateCompletion();
            }
        }
    }

    static final class ForEachValueTask<K, V> extends BulkTask<K, V, Void> {

        final Consumer<? super V> action;

        ForEachValueTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Consumer<? super V> action) {
            super(p, b, i, f, t);
            this.action = action;
        }

        public final void compute() {
            final Consumer<? super V> action;
            if ((action = this.action) != null) {
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    new ForEachValueTask<K, V>(this, batch >>>= 1, baseLimit = h, f, tab, action).fork();
                }
                for (Node<K, V> p; (p = advance()) != null; ) action.accept(p.val);
                propagateCompletion();
            }
        }
    }

    static final class ForEachEntryTask<K, V> extends BulkTask<K, V, Void> {

        final Consumer<? super Entry<K, V>> action;

        ForEachEntryTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Consumer<? super Entry<K, V>> action) {
            super(p, b, i, f, t);
            this.action = action;
        }

        public final void compute() {
            final Consumer<? super Entry<K, V>> action;
            if ((action = this.action) != null) {
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    new ForEachEntryTask<K, V>(this, batch >>>= 1, baseLimit = h, f, tab, action).fork();
                }
                for (Node<K, V> p; (p = advance()) != null; ) action.accept(p);
                propagateCompletion();
            }
        }
    }

    static final class ForEachMappingTask<K, V> extends BulkTask<K, V, Void> {

        final BiConsumer<? super K, ? super V> action;

        ForEachMappingTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, BiConsumer<? super K, ? super V> action) {
            super(p, b, i, f, t);
            this.action = action;
        }

        public final void compute() {
            final BiConsumer<? super K, ? super V> action;
            if ((action = this.action) != null) {
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    new ForEachMappingTask<K, V>(this, batch >>>= 1, baseLimit = h, f, tab, action).fork();
                }
                for (Node<K, V> p; (p = advance()) != null; ) action.accept((K) p.key, p.val);
                propagateCompletion();
            }
        }
    }

    static final class ForEachTransformedKeyTask<K, V, U> extends BulkTask<K, V, Void> {

        final Function<? super K, ? extends U> transformer;

        final Consumer<? super U> action;

        ForEachTransformedKeyTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Function<? super K, ? extends U> transformer, Consumer<? super U> action) {
            super(p, b, i, f, t);
            this.transformer = transformer;
            this.action = action;
        }

        public final void compute() {
            final Function<? super K, ? extends U> transformer;
            final Consumer<? super U> action;
            if ((transformer = this.transformer) != null && (action = this.action) != null) {
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    new ForEachTransformedKeyTask<K, V, U>(this, batch >>>= 1, baseLimit = h, f, tab, transformer, action).fork();
                }
                for (Node<K, V> p; (p = advance()) != null; ) {
                    U u;
                    if ((u = transformer.apply((K) p.key)) != null)
                        action.accept(u);
                }
                propagateCompletion();
            }
        }
    }

    static final class ForEachTransformedValueTask<K, V, U> extends BulkTask<K, V, Void> {

        final Function<? super V, ? extends U> transformer;

        final Consumer<? super U> action;

        ForEachTransformedValueTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Function<? super V, ? extends U> transformer, Consumer<? super U> action) {
            super(p, b, i, f, t);
            this.transformer = transformer;
            this.action = action;
        }

        public final void compute() {
            final Function<? super V, ? extends U> transformer;
            final Consumer<? super U> action;
            if ((transformer = this.transformer) != null && (action = this.action) != null) {
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    new ForEachTransformedValueTask<K, V, U>(this, batch >>>= 1, baseLimit = h, f, tab, transformer, action).fork();
                }
                for (Node<K, V> p; (p = advance()) != null; ) {
                    U u;
                    if ((u = transformer.apply(p.val)) != null)
                        action.accept(u);
                }
                propagateCompletion();
            }
        }
    }

    static final class ForEachTransformedEntryTask<K, V, U> extends BulkTask<K, V, Void> {

        final Function<Map.Entry<K, V>, ? extends U> transformer;

        final Consumer<? super U> action;

        ForEachTransformedEntryTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Function<Map.Entry<K, V>, ? extends U> transformer, Consumer<? super U> action) {
            super(p, b, i, f, t);
            this.transformer = transformer;
            this.action = action;
        }

        public final void compute() {
            final Function<Map.Entry<K, V>, ? extends U> transformer;
            final Consumer<? super U> action;
            if ((transformer = this.transformer) != null && (action = this.action) != null) {
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    new ForEachTransformedEntryTask<K, V, U>(this, batch >>>= 1, baseLimit = h, f, tab, transformer, action).fork();
                }
                for (Node<K, V> p; (p = advance()) != null; ) {
                    U u;
                    if ((u = transformer.apply(p)) != null)
                        action.accept(u);
                }
                propagateCompletion();
            }
        }
    }

    static final class ForEachTransformedMappingTask<K, V, U> extends BulkTask<K, V, Void> {

        final BiFunction<? super K, ? super V, ? extends U> transformer;

        final Consumer<? super U> action;

        ForEachTransformedMappingTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, BiFunction<? super K, ? super V, ? extends U> transformer, Consumer<? super U> action) {
            super(p, b, i, f, t);
            this.transformer = transformer;
            this.action = action;
        }

        public final void compute() {
            final BiFunction<? super K, ? super V, ? extends U> transformer;
            final Consumer<? super U> action;
            if ((transformer = this.transformer) != null && (action = this.action) != null) {
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    new ForEachTransformedMappingTask<K, V, U>(this, batch >>>= 1, baseLimit = h, f, tab, transformer, action).fork();
                }
                for (Node<K, V> p; (p = advance()) != null; ) {
                    U u;
                    if ((u = transformer.apply((K) p.key, p.val)) != null)
                        action.accept(u);
                }
                propagateCompletion();
            }
        }
    }

    static final class SearchKeysTask<K, V, U> extends BulkTask<K, V, U> {

        final Function<? super K, ? extends U> searchFunction;

        final AtomicReference<U> result;

        SearchKeysTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Function<? super K, ? extends U> searchFunction, AtomicReference<U> result) {
            super(p, b, i, f, t);
            this.searchFunction = searchFunction;
            this.result = result;
        }

        public final U getRawResult() {
            return result.get();
        }

        public final void compute() {
            final Function<? super K, ? extends U> searchFunction;
            final AtomicReference<U> result;
            if ((searchFunction = this.searchFunction) != null && (result = this.result) != null) {
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    if (result.get() != null)
                        return;
                    addToPendingCount(1);
                    new SearchKeysTask<K, V, U>(this, batch >>>= 1, baseLimit = h, f, tab, searchFunction, result).fork();
                }
                while (result.get() == null) {
                    U u;
                    Node<K, V> p;
                    if ((p = advance()) == null) {
                        propagateCompletion();
                        break;
                    }
                    if ((u = searchFunction.apply((K) p.key)) != null) {
                        if (result.compareAndSet(null, u))
                            quietlyCompleteRoot();
                        break;
                    }
                }
            }
        }
    }

    static final class SearchValuesTask<K, V, U> extends BulkTask<K, V, U> {

        final Function<? super V, ? extends U> searchFunction;

        final AtomicReference<U> result;

        SearchValuesTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Function<? super V, ? extends U> searchFunction, AtomicReference<U> result) {
            super(p, b, i, f, t);
            this.searchFunction = searchFunction;
            this.result = result;
        }

        public final U getRawResult() {
            return result.get();
        }

        public final void compute() {
            final Function<? super V, ? extends U> searchFunction;
            final AtomicReference<U> result;
            if ((searchFunction = this.searchFunction) != null && (result = this.result) != null) {
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    if (result.get() != null)
                        return;
                    addToPendingCount(1);
                    new SearchValuesTask<K, V, U>(this, batch >>>= 1, baseLimit = h, f, tab, searchFunction, result).fork();
                }
                while (result.get() == null) {
                    U u;
                    Node<K, V> p;
                    if ((p = advance()) == null) {
                        propagateCompletion();
                        break;
                    }
                    if ((u = searchFunction.apply(p.val)) != null) {
                        if (result.compareAndSet(null, u))
                            quietlyCompleteRoot();
                        break;
                    }
                }
            }
        }
    }

    static final class SearchEntriesTask<K, V, U> extends BulkTask<K, V, U> {

        final Function<Entry<K, V>, ? extends U> searchFunction;

        final AtomicReference<U> result;

        SearchEntriesTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Function<Entry<K, V>, ? extends U> searchFunction, AtomicReference<U> result) {
            super(p, b, i, f, t);
            this.searchFunction = searchFunction;
            this.result = result;
        }

        public final U getRawResult() {
            return result.get();
        }

        public final void compute() {
            final Function<Entry<K, V>, ? extends U> searchFunction;
            final AtomicReference<U> result;
            if ((searchFunction = this.searchFunction) != null && (result = this.result) != null) {
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    if (result.get() != null)
                        return;
                    addToPendingCount(1);
                    new SearchEntriesTask<K, V, U>(this, batch >>>= 1, baseLimit = h, f, tab, searchFunction, result).fork();
                }
                while (result.get() == null) {
                    U u;
                    Node<K, V> p;
                    if ((p = advance()) == null) {
                        propagateCompletion();
                        break;
                    }
                    if ((u = searchFunction.apply(p)) != null) {
                        if (result.compareAndSet(null, u))
                            quietlyCompleteRoot();
                        return;
                    }
                }
            }
        }
    }

    static final class SearchMappingsTask<K, V, U> extends BulkTask<K, V, U> {

        final BiFunction<? super K, ? super V, ? extends U> searchFunction;

        final AtomicReference<U> result;

        SearchMappingsTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, BiFunction<? super K, ? super V, ? extends U> searchFunction, AtomicReference<U> result) {
            super(p, b, i, f, t);
            this.searchFunction = searchFunction;
            this.result = result;
        }

        public final U getRawResult() {
            return result.get();
        }

        public final void compute() {
            final BiFunction<? super K, ? super V, ? extends U> searchFunction;
            final AtomicReference<U> result;
            if ((searchFunction = this.searchFunction) != null && (result = this.result) != null) {
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    if (result.get() != null)
                        return;
                    addToPendingCount(1);
                    new SearchMappingsTask<K, V, U>(this, batch >>>= 1, baseLimit = h, f, tab, searchFunction, result).fork();
                }
                while (result.get() == null) {
                    U u;
                    Node<K, V> p;
                    if ((p = advance()) == null) {
                        propagateCompletion();
                        break;
                    }
                    if ((u = searchFunction.apply((K) p.key, p.val)) != null) {
                        if (result.compareAndSet(null, u))
                            quietlyCompleteRoot();
                        break;
                    }
                }
            }
        }
    }

    static final class ReduceKeysTask<K, V> extends BulkTask<K, V, K> {

        final BiFunction<? super K, ? super K, ? extends K> reducer;

        K result;

        ReduceKeysTask<K, V> rights, nextRight;

        ReduceKeysTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, ReduceKeysTask<K, V> nextRight, BiFunction<? super K, ? super K, ? extends K> reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.reducer = reducer;
        }

        public final K getRawResult() {
            return result;
        }

        public final void compute() {
            final BiFunction<? super K, ? super K, ? extends K> reducer;
            if ((reducer = this.reducer) != null) {
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    (rights = new ReduceKeysTask<K, V>(this, batch >>>= 1, baseLimit = h, f, tab, rights, reducer)).fork();
                }
                K r = null;
                for (Node<K, V> p; (p = advance()) != null; ) {
                    K u = (K) p.key;
                    r = (r == null) ? u : u == null ? r : reducer.apply(r, u);
                }
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    ReduceKeysTask<K, V> t = (ReduceKeysTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        K tr, sr;
                        if ((sr = s.result) != null)
                            t.result = (((tr = t.result) == null) ? sr : reducer.apply(tr, sr));
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    static final class ReduceValuesTask<K, V> extends BulkTask<K, V, V> {

        final BiFunction<? super V, ? super V, ? extends V> reducer;

        V result;

        ReduceValuesTask<K, V> rights, nextRight;

        ReduceValuesTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, ReduceValuesTask<K, V> nextRight, BiFunction<? super V, ? super V, ? extends V> reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.reducer = reducer;
        }

        public final V getRawResult() {
            return result;
        }

        public final void compute() {
            final BiFunction<? super V, ? super V, ? extends V> reducer;
            if ((reducer = this.reducer) != null) {
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    (rights = new ReduceValuesTask<K, V>(this, batch >>>= 1, baseLimit = h, f, tab, rights, reducer)).fork();
                }
                V r = null;
                for (Node<K, V> p; (p = advance()) != null; ) {
                    V v = p.val;
                    r = (r == null) ? v : reducer.apply(r, v);
                }
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    ReduceValuesTask<K, V> t = (ReduceValuesTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        V tr, sr;
                        if ((sr = s.result) != null)
                            t.result = (((tr = t.result) == null) ? sr : reducer.apply(tr, sr));
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    static final class ReduceEntriesTask<K, V> extends BulkTask<K, V, Map.Entry<K, V>> {

        final BiFunction<Map.Entry<K, V>, Map.Entry<K, V>, ? extends Map.Entry<K, V>> reducer;

        Map.Entry<K, V> result;

        ReduceEntriesTask<K, V> rights, nextRight;

        ReduceEntriesTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, ReduceEntriesTask<K, V> nextRight, BiFunction<Entry<K, V>, Map.Entry<K, V>, ? extends Map.Entry<K, V>> reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.reducer = reducer;
        }

        public final Map.Entry<K, V> getRawResult() {
            return result;
        }

        public final void compute() {
            final BiFunction<Map.Entry<K, V>, Map.Entry<K, V>, ? extends Map.Entry<K, V>> reducer;
            if ((reducer = this.reducer) != null) {
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    (rights = new ReduceEntriesTask<K, V>(this, batch >>>= 1, baseLimit = h, f, tab, rights, reducer)).fork();
                }
                Map.Entry<K, V> r = null;
                for (Node<K, V> p; (p = advance()) != null; ) r = (r == null) ? p : reducer.apply(r, p);
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    ReduceEntriesTask<K, V> t = (ReduceEntriesTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        Map.Entry<K, V> tr, sr;
                        if ((sr = s.result) != null)
                            t.result = (((tr = t.result) == null) ? sr : reducer.apply(tr, sr));
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    static final class MapReduceKeysTask<K, V, U> extends BulkTask<K, V, U> {

        final Function<? super K, ? extends U> transformer;

        final BiFunction<? super U, ? super U, ? extends U> reducer;

        U result;

        MapReduceKeysTask<K, V, U> rights, nextRight;

        MapReduceKeysTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceKeysTask<K, V, U> nextRight, Function<? super K, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.reducer = reducer;
        }

        public final U getRawResult() {
            return result;
        }

        public final void compute() {
            final Function<? super K, ? extends U> transformer;
            final BiFunction<? super U, ? super U, ? extends U> reducer;
            if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    (rights = new MapReduceKeysTask<K, V, U>(this, batch >>>= 1, baseLimit = h, f, tab, rights, transformer, reducer)).fork();
                }
                U r = null;
                for (Node<K, V> p; (p = advance()) != null; ) {
                    U u;
                    if ((u = transformer.apply((K) p.key)) != null)
                        r = (r == null) ? u : reducer.apply(r, u);
                }
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    MapReduceKeysTask<K, V, U> t = (MapReduceKeysTask<K, V, U>) c, s = t.rights;
                    while (s != null) {
                        U tr, sr;
                        if ((sr = s.result) != null)
                            t.result = (((tr = t.result) == null) ? sr : reducer.apply(tr, sr));
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    static final class MapReduceValuesTask<K, V, U> extends BulkTask<K, V, U> {

        final Function<? super V, ? extends U> transformer;

        final BiFunction<? super U, ? super U, ? extends U> reducer;

        U result;

        MapReduceValuesTask<K, V, U> rights, nextRight;

        MapReduceValuesTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceValuesTask<K, V, U> nextRight, Function<? super V, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.reducer = reducer;
        }

        public final U getRawResult() {
            return result;
        }

        public final void compute() {
            final Function<? super V, ? extends U> transformer;
            final BiFunction<? super U, ? super U, ? extends U> reducer;
            if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    (rights = new MapReduceValuesTask<K, V, U>(this, batch >>>= 1, baseLimit = h, f, tab, rights, transformer, reducer)).fork();
                }
                U r = null;
                for (Node<K, V> p; (p = advance()) != null; ) {
                    U u;
                    if ((u = transformer.apply(p.val)) != null)
                        r = (r == null) ? u : reducer.apply(r, u);
                }
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    MapReduceValuesTask<K, V, U> t = (MapReduceValuesTask<K, V, U>) c, s = t.rights;
                    while (s != null) {
                        U tr, sr;
                        if ((sr = s.result) != null)
                            t.result = (((tr = t.result) == null) ? sr : reducer.apply(tr, sr));
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    static final class MapReduceEntriesTask<K, V, U> extends BulkTask<K, V, U> {

        final Function<Map.Entry<K, V>, ? extends U> transformer;

        final BiFunction<? super U, ? super U, ? extends U> reducer;

        U result;

        MapReduceEntriesTask<K, V, U> rights, nextRight;

        MapReduceEntriesTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceEntriesTask<K, V, U> nextRight, Function<Map.Entry<K, V>, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.reducer = reducer;
        }

        public final U getRawResult() {
            return result;
        }

        public final void compute() {
            final Function<Map.Entry<K, V>, ? extends U> transformer;
            final BiFunction<? super U, ? super U, ? extends U> reducer;
            if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    (rights = new MapReduceEntriesTask<K, V, U>(this, batch >>>= 1, baseLimit = h, f, tab, rights, transformer, reducer)).fork();
                }
                U r = null;
                for (Node<K, V> p; (p = advance()) != null; ) {
                    U u;
                    if ((u = transformer.apply(p)) != null)
                        r = (r == null) ? u : reducer.apply(r, u);
                }
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    MapReduceEntriesTask<K, V, U> t = (MapReduceEntriesTask<K, V, U>) c, s = t.rights;
                    while (s != null) {
                        U tr, sr;
                        if ((sr = s.result) != null)
                            t.result = (((tr = t.result) == null) ? sr : reducer.apply(tr, sr));
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    static final class MapReduceMappingsTask<K, V, U> extends BulkTask<K, V, U> {

        final BiFunction<? super K, ? super V, ? extends U> transformer;

        final BiFunction<? super U, ? super U, ? extends U> reducer;

        U result;

        MapReduceMappingsTask<K, V, U> rights, nextRight;

        MapReduceMappingsTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceMappingsTask<K, V, U> nextRight, BiFunction<? super K, ? super V, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.reducer = reducer;
        }

        public final U getRawResult() {
            return result;
        }

        public final void compute() {
            final BiFunction<? super K, ? super V, ? extends U> transformer;
            final BiFunction<? super U, ? super U, ? extends U> reducer;
            if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    (rights = new MapReduceMappingsTask<K, V, U>(this, batch >>>= 1, baseLimit = h, f, tab, rights, transformer, reducer)).fork();
                }
                U r = null;
                for (Node<K, V> p; (p = advance()) != null; ) {
                    U u;
                    if ((u = transformer.apply((K) p.key, p.val)) != null)
                        r = (r == null) ? u : reducer.apply(r, u);
                }
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    MapReduceMappingsTask<K, V, U> t = (MapReduceMappingsTask<K, V, U>) c, s = t.rights;
                    while (s != null) {
                        U tr, sr;
                        if ((sr = s.result) != null)
                            t.result = (((tr = t.result) == null) ? sr : reducer.apply(tr, sr));
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    static final class MapReduceKeysToDoubleTask<K, V> extends BulkTask<K, V, Double> {

        final ToDoubleFunction<? super K> transformer;

        final DoubleBinaryOperator reducer;

        final double basis;

        double result;

        MapReduceKeysToDoubleTask<K, V> rights, nextRight;

        MapReduceKeysToDoubleTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceKeysToDoubleTask<K, V> nextRight, ToDoubleFunction<? super K> transformer, double basis, DoubleBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Double getRawResult() {
            return result;
        }

        public final void compute() {
            final ToDoubleFunction<? super K> transformer;
            final DoubleBinaryOperator reducer;
            if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
                double r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    (rights = new MapReduceKeysToDoubleTask<K, V>(this, batch >>>= 1, baseLimit = h, f, tab, rights, transformer, r, reducer)).fork();
                }
                for (Node<K, V> p; (p = advance()) != null; ) r = reducer.applyAsDouble(r, transformer.applyAsDouble((K) p.key));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    MapReduceKeysToDoubleTask<K, V> t = (MapReduceKeysToDoubleTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsDouble(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    static final class MapReduceValuesToDoubleTask<K, V> extends BulkTask<K, V, Double> {

        final ToDoubleFunction<? super V> transformer;

        final DoubleBinaryOperator reducer;

        final double basis;

        double result;

        MapReduceValuesToDoubleTask<K, V> rights, nextRight;

        MapReduceValuesToDoubleTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceValuesToDoubleTask<K, V> nextRight, ToDoubleFunction<? super V> transformer, double basis, DoubleBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Double getRawResult() {
            return result;
        }

        public final void compute() {
            final ToDoubleFunction<? super V> transformer;
            final DoubleBinaryOperator reducer;
            if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
                double r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    (rights = new MapReduceValuesToDoubleTask<K, V>(this, batch >>>= 1, baseLimit = h, f, tab, rights, transformer, r, reducer)).fork();
                }
                for (Node<K, V> p; (p = advance()) != null; ) r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.val));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    MapReduceValuesToDoubleTask<K, V> t = (MapReduceValuesToDoubleTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsDouble(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    static final class MapReduceEntriesToDoubleTask<K, V> extends BulkTask<K, V, Double> {

        final ToDoubleFunction<Map.Entry<K, V>> transformer;

        final DoubleBinaryOperator reducer;

        final double basis;

        double result;

        MapReduceEntriesToDoubleTask<K, V> rights, nextRight;

        MapReduceEntriesToDoubleTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceEntriesToDoubleTask<K, V> nextRight, ToDoubleFunction<Map.Entry<K, V>> transformer, double basis, DoubleBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Double getRawResult() {
            return result;
        }

        public final void compute() {
            final ToDoubleFunction<Map.Entry<K, V>> transformer;
            final DoubleBinaryOperator reducer;
            if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
                double r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    (rights = new MapReduceEntriesToDoubleTask<K, V>(this, batch >>>= 1, baseLimit = h, f, tab, rights, transformer, r, reducer)).fork();
                }
                for (Node<K, V> p; (p = advance()) != null; ) r = reducer.applyAsDouble(r, transformer.applyAsDouble(p));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    MapReduceEntriesToDoubleTask<K, V> t = (MapReduceEntriesToDoubleTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsDouble(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    static final class MapReduceMappingsToDoubleTask<K, V> extends BulkTask<K, V, Double> {

        final ToDoubleBiFunction<? super K, ? super V> transformer;

        final DoubleBinaryOperator reducer;

        final double basis;

        double result;

        MapReduceMappingsToDoubleTask<K, V> rights, nextRight;

        MapReduceMappingsToDoubleTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceMappingsToDoubleTask<K, V> nextRight, ToDoubleBiFunction<? super K, ? super V> transformer, double basis, DoubleBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Double getRawResult() {
            return result;
        }

        public final void compute() {
            final ToDoubleBiFunction<? super K, ? super V> transformer;
            final DoubleBinaryOperator reducer;
            if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
                double r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    (rights = new MapReduceMappingsToDoubleTask<K, V>(this, batch >>>= 1, baseLimit = h, f, tab, rights, transformer, r, reducer)).fork();
                }
                for (Node<K, V> p; (p = advance()) != null; ) r = reducer.applyAsDouble(r, transformer.applyAsDouble((K) p.key, p.val));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    MapReduceMappingsToDoubleTask<K, V> t = (MapReduceMappingsToDoubleTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsDouble(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    static final class MapReduceKeysToLongTask<K, V> extends BulkTask<K, V, Long> {

        final ToLongFunction<? super K> transformer;

        final LongBinaryOperator reducer;

        final long basis;

        long result;

        MapReduceKeysToLongTask<K, V> rights, nextRight;

        MapReduceKeysToLongTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceKeysToLongTask<K, V> nextRight, ToLongFunction<? super K> transformer, long basis, LongBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Long getRawResult() {
            return result;
        }

        public final void compute() {
            final ToLongFunction<? super K> transformer;
            final LongBinaryOperator reducer;
            if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
                long r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    (rights = new MapReduceKeysToLongTask<K, V>(this, batch >>>= 1, baseLimit = h, f, tab, rights, transformer, r, reducer)).fork();
                }
                for (Node<K, V> p; (p = advance()) != null; ) r = reducer.applyAsLong(r, transformer.applyAsLong((K) p.key));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    MapReduceKeysToLongTask<K, V> t = (MapReduceKeysToLongTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsLong(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    static final class MapReduceValuesToLongTask<K, V> extends BulkTask<K, V, Long> {

        final ToLongFunction<? super V> transformer;

        final LongBinaryOperator reducer;

        final long basis;

        long result;

        MapReduceValuesToLongTask<K, V> rights, nextRight;

        MapReduceValuesToLongTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceValuesToLongTask<K, V> nextRight, ToLongFunction<? super V> transformer, long basis, LongBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Long getRawResult() {
            return result;
        }

        public final void compute() {
            final ToLongFunction<? super V> transformer;
            final LongBinaryOperator reducer;
            if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
                long r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    (rights = new MapReduceValuesToLongTask<K, V>(this, batch >>>= 1, baseLimit = h, f, tab, rights, transformer, r, reducer)).fork();
                }
                for (Node<K, V> p; (p = advance()) != null; ) r = reducer.applyAsLong(r, transformer.applyAsLong(p.val));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    MapReduceValuesToLongTask<K, V> t = (MapReduceValuesToLongTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsLong(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    static final class MapReduceEntriesToLongTask<K, V> extends BulkTask<K, V, Long> {

        final ToLongFunction<Map.Entry<K, V>> transformer;

        final LongBinaryOperator reducer;

        final long basis;

        long result;

        MapReduceEntriesToLongTask<K, V> rights, nextRight;

        MapReduceEntriesToLongTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceEntriesToLongTask<K, V> nextRight, ToLongFunction<Map.Entry<K, V>> transformer, long basis, LongBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Long getRawResult() {
            return result;
        }

        public final void compute() {
            final ToLongFunction<Map.Entry<K, V>> transformer;
            final LongBinaryOperator reducer;
            if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
                long r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    (rights = new MapReduceEntriesToLongTask<K, V>(this, batch >>>= 1, baseLimit = h, f, tab, rights, transformer, r, reducer)).fork();
                }
                for (Node<K, V> p; (p = advance()) != null; ) r = reducer.applyAsLong(r, transformer.applyAsLong(p));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    MapReduceEntriesToLongTask<K, V> t = (MapReduceEntriesToLongTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsLong(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    static final class MapReduceMappingsToLongTask<K, V> extends BulkTask<K, V, Long> {

        final ToLongBiFunction<? super K, ? super V> transformer;

        final LongBinaryOperator reducer;

        final long basis;

        long result;

        MapReduceMappingsToLongTask<K, V> rights, nextRight;

        MapReduceMappingsToLongTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceMappingsToLongTask<K, V> nextRight, ToLongBiFunction<? super K, ? super V> transformer, long basis, LongBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Long getRawResult() {
            return result;
        }

        public final void compute() {
            final ToLongBiFunction<? super K, ? super V> transformer;
            final LongBinaryOperator reducer;
            if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
                long r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    (rights = new MapReduceMappingsToLongTask<K, V>(this, batch >>>= 1, baseLimit = h, f, tab, rights, transformer, r, reducer)).fork();
                }
                for (Node<K, V> p; (p = advance()) != null; ) r = reducer.applyAsLong(r, transformer.applyAsLong((K) p.key, p.val));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    MapReduceMappingsToLongTask<K, V> t = (MapReduceMappingsToLongTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsLong(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    static final class MapReduceKeysToIntTask<K, V> extends BulkTask<K, V, Integer> {

        final ToIntFunction<? super K> transformer;

        final IntBinaryOperator reducer;

        final int basis;

        int result;

        MapReduceKeysToIntTask<K, V> rights, nextRight;

        MapReduceKeysToIntTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceKeysToIntTask<K, V> nextRight, ToIntFunction<? super K> transformer, int basis, IntBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Integer getRawResult() {
            return result;
        }

        public final void compute() {
            final ToIntFunction<? super K> transformer;
            final IntBinaryOperator reducer;
            if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
                int r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    (rights = new MapReduceKeysToIntTask<K, V>(this, batch >>>= 1, baseLimit = h, f, tab, rights, transformer, r, reducer)).fork();
                }
                for (Node<K, V> p; (p = advance()) != null; ) r = reducer.applyAsInt(r, transformer.applyAsInt((K) p.key));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    MapReduceKeysToIntTask<K, V> t = (MapReduceKeysToIntTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsInt(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    static final class MapReduceValuesToIntTask<K, V> extends BulkTask<K, V, Integer> {

        final ToIntFunction<? super V> transformer;

        final IntBinaryOperator reducer;

        final int basis;

        int result;

        MapReduceValuesToIntTask<K, V> rights, nextRight;

        MapReduceValuesToIntTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceValuesToIntTask<K, V> nextRight, ToIntFunction<? super V> transformer, int basis, IntBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Integer getRawResult() {
            return result;
        }

        public final void compute() {
            final ToIntFunction<? super V> transformer;
            final IntBinaryOperator reducer;
            if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
                int r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    (rights = new MapReduceValuesToIntTask<K, V>(this, batch >>>= 1, baseLimit = h, f, tab, rights, transformer, r, reducer)).fork();
                }
                for (Node<K, V> p; (p = advance()) != null; ) r = reducer.applyAsInt(r, transformer.applyAsInt(p.val));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    MapReduceValuesToIntTask<K, V> t = (MapReduceValuesToIntTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsInt(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    static final class MapReduceEntriesToIntTask<K, V> extends BulkTask<K, V, Integer> {

        final ToIntFunction<Map.Entry<K, V>> transformer;

        final IntBinaryOperator reducer;

        final int basis;

        int result;

        MapReduceEntriesToIntTask<K, V> rights, nextRight;

        MapReduceEntriesToIntTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceEntriesToIntTask<K, V> nextRight, ToIntFunction<Map.Entry<K, V>> transformer, int basis, IntBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Integer getRawResult() {
            return result;
        }

        public final void compute() {
            final ToIntFunction<Map.Entry<K, V>> transformer;
            final IntBinaryOperator reducer;
            if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
                int r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    (rights = new MapReduceEntriesToIntTask<K, V>(this, batch >>>= 1, baseLimit = h, f, tab, rights, transformer, r, reducer)).fork();
                }
                for (Node<K, V> p; (p = advance()) != null; ) r = reducer.applyAsInt(r, transformer.applyAsInt(p));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    MapReduceEntriesToIntTask<K, V> t = (MapReduceEntriesToIntTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsInt(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    static final class MapReduceMappingsToIntTask<K, V> extends BulkTask<K, V, Integer> {

        final ToIntBiFunction<? super K, ? super V> transformer;

        final IntBinaryOperator reducer;

        final int basis;

        int result;

        MapReduceMappingsToIntTask<K, V> rights, nextRight;

        MapReduceMappingsToIntTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceMappingsToIntTask<K, V> nextRight, ToIntBiFunction<? super K, ? super V> transformer, int basis, IntBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Integer getRawResult() {
            return result;
        }

        public final void compute() {
            final ToIntBiFunction<? super K, ? super V> transformer;
            final IntBinaryOperator reducer;
            if ((transformer = this.transformer) != null && (reducer = this.reducer) != null) {
                int r = this.basis;
                for (int i = baseIndex, f, h; batch > 0 && (h = ((f = baseLimit) + i) >>> 1) > i; ) {
                    addToPendingCount(1);
                    (rights = new MapReduceMappingsToIntTask<K, V>(this, batch >>>= 1, baseLimit = h, f, tab, rights, transformer, r, reducer)).fork();
                }
                for (Node<K, V> p; (p = advance()) != null; ) r = reducer.applyAsInt(r, transformer.applyAsInt((K) p.key, p.val));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    MapReduceMappingsToIntTask<K, V> t = (MapReduceMappingsToIntTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsInt(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    private static final sun.misc.Unsafe U;

    private static final long SIZECTL;

    private static final long TRANSFERINDEX;

    private static final long TRANSFERORIGIN;

    private static final long BASECOUNT;

    private static final long CELLSBUSY;

    private static final long CELLVALUE;

    private static final long ABASE;

    private static final int ASHIFT;

    static {
        try {
            U = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ConcurrentHashMap.class;
            SIZECTL = U.objectFieldOffset(k.getDeclaredField("sizeCtl"));
            TRANSFERINDEX = U.objectFieldOffset(k.getDeclaredField("transferIndex"));
            TRANSFERORIGIN = U.objectFieldOffset(k.getDeclaredField("transferOrigin"));
            BASECOUNT = U.objectFieldOffset(k.getDeclaredField("baseCount"));
            CELLSBUSY = U.objectFieldOffset(k.getDeclaredField("cellsBusy"));
            Class<?> ck = Cell.class;
            CELLVALUE = U.objectFieldOffset(ck.getDeclaredField("value"));
            Class<?> sc = Node[].class;
            ABASE = U.arrayBaseOffset(sc);
            int scale = U.arrayIndexScale(sc);
            if ((scale & (scale - 1)) != 0)
                throw new Error("data type scale not a power of two");
            ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
