package java.util.concurrent;

import java.io.ObjectStreamField;
import java.io.Serializable;
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
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
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

public class ConcurrentHashMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V>, Serializable {

    private static final long serialVersionUID = 7249069246763182397L;

    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private static final int DEFAULT_CAPACITY = 16;

    static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;

    private static final float LOAD_FACTOR = 0.75f;

    static final int TREEIFY_THRESHOLD = 8;

    static final int UNTREEIFY_THRESHOLD = 6;

    static final int MIN_TREEIFY_CAPACITY = 64;

    private static final int MIN_TRANSFER_STRIDE = 16;

    static final int MOVED = -1;

    static final int TREEBIN = -2;

    static final int RESERVED = -3;

    static final int HASH_BITS = 0x7fffffff;

    static final int NCPU = Runtime.getRuntime().availableProcessors();

    private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField("segments", Segment[].class), new ObjectStreamField("segmentMask", Integer.TYPE), new ObjectStreamField("segmentShift", Integer.TYPE) };

    static class Node<K, V> implements Map.Entry<K, V> {

        final int hash;

        final K key;

        volatile V val;

        volatile Node<K, V> next;

        Node(int hash, K key, V val, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.val = val;
            this.next = next;
        }

        public final K getKey() {
            return key;
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

        Node<K, V> find(int h, Object k) {
            Node<K, V> e = this;
            if (k != null) {
                do {
                    K ek;
                    if (e.hash == h && ((ek = e.key) == k || (ek != null && k.equals(ek))))
                        return e;
                } while ((e = e.next) != null);
            }
            return null;
        }
    }

    static final int spread(int h) {
        return (h ^ (h >>> 16)) & HASH_BITS;
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

    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Class<?> c;
            Type[] ts, as;
            Type t;
            ParameterizedType p;
            if ((c = x.getClass()) == String.class)
                return c;
            if ((ts = c.getGenericInterfaces()) != null) {
                for (int i = 0; i < ts.length; ++i) {
                    if (((t = ts[i]) instanceof ParameterizedType) && ((p = (ParameterizedType) t).getRawType() == Comparable.class) && (as = p.getActualTypeArguments()) != null && as.length == 1 && as[0] == c)
                        return c;
                }
            }
        }
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 : ((Comparable) k).compareTo(x));
    }

    @SuppressWarnings("unchecked")
    static final <K, V> Node<K, V> tabAt(Node<K, V>[] tab, int i) {
        return (Node<K, V>) U.getObjectVolatile(tab, ((long) i << ASHIFT) + ABASE);
    }

    static final <K, V> boolean casTabAt(Node<K, V>[] tab, int i, Node<K, V> c, Node<K, V> v) {
        return U.compareAndSwapObject(tab, ((long) i << ASHIFT) + ABASE, c, v);
    }

    static final <K, V> void setTabAt(Node<K, V>[] tab, int i, Node<K, V> v) {
        U.putObjectVolatile(tab, ((long) i << ASHIFT) + ABASE, v);
    }

    transient volatile Node<K, V>[] table;

    private transient volatile Node<K, V>[] nextTable;

    private transient volatile long baseCount;

    private transient volatile int sizeCtl;

    private transient volatile int transferIndex;

    private transient volatile int transferOrigin;

    private transient volatile int cellsBusy;

    private transient volatile CounterCell[] counterCells;

    private transient KeySetView<K, V> keySet;

    private transient ValuesView<K, V> values;

    private transient EntrySetView<K, V> entrySet;

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
        putAll(m);
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

    public int size() {
        long n = sumCount();
        return ((n < 0L) ? 0 : (n > (long) Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) n);
    }

    public boolean isEmpty() {
        return sumCount() <= 0L;
    }

    public V get(Object key) {
        Node<K, V>[] tab;
        Node<K, V> e, p;
        int n, eh;
        K ek;
        int h = spread(key.hashCode());
        if ((tab = table) != null && (n = tab.length) > 0 && (e = tabAt(tab, (n - 1) & h)) != null) {
            if ((eh = e.hash) == h) {
                if ((ek = e.key) == key || (ek != null && key.equals(ek)))
                    return e.val;
            } else if (eh < 0)
                return (p = e.find(h, key)) != null ? p.val : null;
            while ((e = e.next) != null) {
                if (e.hash == h && ((ek = e.key) == key || (ek != null && key.equals(ek))))
                    return e.val;
            }
        }
        return null;
    }

    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    public boolean containsValue(Object value) {
        if (value == null)
            throw new NullPointerException();
        Node<K, V>[] t;
        if ((t = table) != null) {
            Traverser<K, V> it = new Traverser<K, V>(t, t.length, 0, t.length);
            for (Node<K, V> p; (p = it.advance()) != null; ) {
                V v;
                if ((v = p.val) == value || (v != null && value.equals(v)))
                    return true;
            }
        }
        return false;
    }

    public V put(K key, V value) {
        return putVal(key, value, false);
    }

    final V putVal(K key, V value, boolean onlyIfAbsent) {
        if (key == null || value == null)
            throw new NullPointerException();
        int hash = spread(key.hashCode());
        int binCount = 0;
        for (Node<K, V>[] tab = table; ; ) {
            Node<K, V> f;
            int n, i, fh;
            if (tab == null || (n = tab.length) == 0)
                tab = initTable();
            else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
                if (casTabAt(tab, i, null, new Node<K, V>(hash, key, value, null)))
                    break;
            } else if ((fh = f.hash) == MOVED)
                tab = helpTransfer(tab, f);
            else {
                V oldVal = null;
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        if (fh >= 0) {
                            binCount = 1;
                            for (Node<K, V> e = f; ; ++binCount) {
                                K ek;
                                if (e.hash == hash && ((ek = e.key) == key || (ek != null && key.equals(ek)))) {
                                    oldVal = e.val;
                                    if (!onlyIfAbsent)
                                        e.val = value;
                                    break;
                                }
                                Node<K, V> pred = e;
                                if ((e = e.next) == null) {
                                    pred.next = new Node<K, V>(hash, key, value, null);
                                    break;
                                }
                            }
                        } else if (f instanceof TreeBin) {
                            Node<K, V> p;
                            binCount = 2;
                            if ((p = ((TreeBin<K, V>) f).putTreeVal(hash, key, value)) != null) {
                                oldVal = p.val;
                                if (!onlyIfAbsent)
                                    p.val = value;
                            }
                        }
                    }
                }
                if (binCount != 0) {
                    if (binCount >= TREEIFY_THRESHOLD)
                        treeifyBin(tab, i);
                    if (oldVal != null)
                        return oldVal;
                    break;
                }
            }
        }
        addCount(1L, binCount);
        return null;
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        tryPresize(m.size());
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) putVal(e.getKey(), e.getValue(), false);
    }

    public V remove(Object key) {
        return replaceNode(key, null, null);
    }

    final V replaceNode(Object key, V value, Object cv) {
        int hash = spread(key.hashCode());
        for (Node<K, V>[] tab = table; ; ) {
            Node<K, V> f;
            int n, i, fh;
            if (tab == null || (n = tab.length) == 0 || (f = tabAt(tab, i = (n - 1) & hash)) == null)
                break;
            else if ((fh = f.hash) == MOVED)
                tab = helpTransfer(tab, f);
            else {
                V oldVal = null;
                boolean validated = false;
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        if (fh >= 0) {
                            validated = true;
                            for (Node<K, V> e = f, pred = null; ; ) {
                                K ek;
                                if (e.hash == hash && ((ek = e.key) == key || (ek != null && key.equals(ek)))) {
                                    V ev = e.val;
                                    if (cv == null || cv == ev || (ev != null && cv.equals(ev))) {
                                        oldVal = ev;
                                        if (value != null)
                                            e.val = value;
                                        else if (pred != null)
                                            pred.next = e.next;
                                        else
                                            setTabAt(tab, i, e.next);
                                    }
                                    break;
                                }
                                pred = e;
                                if ((e = e.next) == null)
                                    break;
                            }
                        } else if (f instanceof TreeBin) {
                            validated = true;
                            TreeBin<K, V> t = (TreeBin<K, V>) f;
                            TreeNode<K, V> r, p;
                            if ((r = t.root) != null && (p = r.findTreeNode(hash, key, null)) != null) {
                                V pv = p.val;
                                if (cv == null || cv == pv || (pv != null && cv.equals(pv))) {
                                    oldVal = pv;
                                    if (value != null)
                                        p.val = value;
                                    else if (t.removeTreeNode(p))
                                        setTabAt(tab, i, untreeify(t.first));
                                }
                            }
                        }
                    }
                }
                if (validated) {
                    if (oldVal != null) {
                        if (value == null)
                            addCount(-1L, -1);
                        return oldVal;
                    }
                    break;
                }
            }
        }
        return null;
    }

    public void clear() {
        long delta = 0L;
        int i = 0;
        Node<K, V>[] tab = table;
        while (tab != null && i < tab.length) {
            int fh;
            Node<K, V> f = tabAt(tab, i);
            if (f == null)
                ++i;
            else if ((fh = f.hash) == MOVED) {
                tab = helpTransfer(tab, f);
                i = 0;
            } else {
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        Node<K, V> p = (fh >= 0 ? f : (f instanceof TreeBin) ? ((TreeBin<K, V>) f).first : null);
                        while (p != null) {
                            --delta;
                            p = p.next;
                        }
                        setTabAt(tab, i++, null);
                    }
                }
            }
        }
        if (delta != 0L)
            addCount(delta, -1);
    }

    public KeySetView<K, V> keySet() {
        KeySetView<K, V> ks;
        return (ks = keySet) != null ? ks : (keySet = new KeySetView<K, V>(this, null));
    }

    public Collection<V> values() {
        ValuesView<K, V> vs;
        return (vs = values) != null ? vs : (values = new ValuesView<K, V>(this));
    }

    public Set<Map.Entry<K, V>> entrySet() {
        EntrySetView<K, V> es;
        return (es = entrySet) != null ? es : (entrySet = new EntrySetView<K, V>(this));
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
                K k = p.key;
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
                if ((mk = e.getKey()) == null || (mv = e.getValue()) == null || (v = get(mk)) == null || (mv != v && !mv.equals(v)))
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
        @SuppressWarnings("unchecked")
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
        sizeCtl = -1;
        s.defaultReadObject();
        long size = 0L;
        Node<K, V> p = null;
        for (; ; ) {
            @SuppressWarnings("unchecked")
            K k = (K) s.readObject();
            @SuppressWarnings("unchecked")
            V v = (V) s.readObject();
            if (k != null && v != null) {
                p = new Node<K, V>(spread(k.hashCode()), k, v, p);
                ++size;
            } else
                break;
        }
        if (size == 0L)
            sizeCtl = 0;
        else {
            int n;
            if (size >= (long) (MAXIMUM_CAPACITY >>> 1))
                n = MAXIMUM_CAPACITY;
            else {
                int sz = (int) size;
                n = tableSizeFor(sz + (sz >>> 1) + 1);
            }
            @SuppressWarnings({ "rawtypes", "unchecked" })
            Node<K, V>[] tab = (Node<K, V>[]) new Node[n];
            int mask = n - 1;
            long added = 0L;
            while (p != null) {
                boolean insertAtFront;
                Node<K, V> next = p.next, first;
                int h = p.hash, j = h & mask;
                if ((first = tabAt(tab, j)) == null)
                    insertAtFront = true;
                else {
                    K k = p.key;
                    if (first.hash < 0) {
                        TreeBin<K, V> t = (TreeBin<K, V>) first;
                        if (t.putTreeVal(h, k, p.val) == null)
                            ++added;
                        insertAtFront = false;
                    } else {
                        int binCount = 0;
                        insertAtFront = true;
                        Node<K, V> q;
                        K qk;
                        for (q = first; q != null; q = q.next) {
                            if (q.hash == h && ((qk = q.key) == k || (qk != null && k.equals(qk)))) {
                                insertAtFront = false;
                                break;
                            }
                            ++binCount;
                        }
                        if (insertAtFront && binCount >= TREEIFY_THRESHOLD) {
                            insertAtFront = false;
                            ++added;
                            p.next = first;
                            TreeNode<K, V> hd = null, tl = null;
                            for (q = p; q != null; q = q.next) {
                                TreeNode<K, V> t = new TreeNode<K, V>(q.hash, q.key, q.val, null, null);
                                if ((t.prev = tl) == null)
                                    hd = t;
                                else
                                    tl.next = t;
                                tl = t;
                            }
                            setTabAt(tab, j, new TreeBin<K, V>(hd));
                        }
                    }
                }
                if (insertAtFront) {
                    ++added;
                    p.next = first;
                    setTabAt(tab, j, p);
                }
                p = next;
            }
            table = tab;
            sizeCtl = n - (n >>> 2);
            baseCount = added;
        }
    }

    public V putIfAbsent(K key, V value) {
        return putVal(key, value, true);
    }

    public boolean remove(Object key, Object value) {
        if (key == null)
            throw new NullPointerException();
        return value != null && replaceNode(key, null, value) != null;
    }

    public boolean replace(K key, V oldValue, V newValue) {
        if (key == null || oldValue == null || newValue == null)
            throw new NullPointerException();
        return replaceNode(key, newValue, oldValue) != null;
    }

    public V replace(K key, V value) {
        if (key == null || value == null)
            throw new NullPointerException();
        return replaceNode(key, value, null);
    }

    public V getOrDefault(Object key, V defaultValue) {
        V v;
        return (v = get(key)) == null ? defaultValue : v;
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        if (action == null)
            throw new NullPointerException();
        Node<K, V>[] t;
        if ((t = table) != null) {
            Traverser<K, V> it = new Traverser<K, V>(t, t.length, 0, t.length);
            for (Node<K, V> p; (p = it.advance()) != null; ) {
                action.accept(p.key, p.val);
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
                V oldValue = p.val;
                for (K key = p.key; ; ) {
                    V newValue = function.apply(key, oldValue);
                    if (newValue == null)
                        throw new NullPointerException();
                    if (replaceNode(key, newValue, oldValue) != null || (oldValue = get(key)) == null)
                        break;
                }
            }
        }
    }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        if (key == null || mappingFunction == null)
            throw new NullPointerException();
        int h = spread(key.hashCode());
        V val = null;
        int binCount = 0;
        for (Node<K, V>[] tab = table; ; ) {
            Node<K, V> f;
            int n, i, fh;
            if (tab == null || (n = tab.length) == 0)
                tab = initTable();
            else if ((f = tabAt(tab, i = (n - 1) & h)) == null) {
                Node<K, V> r = new ReservationNode<K, V>();
                synchronized (r) {
                    if (casTabAt(tab, i, null, r)) {
                        binCount = 1;
                        Node<K, V> node = null;
                        try {
                            if ((val = mappingFunction.apply(key)) != null)
                                node = new Node<K, V>(h, key, val, null);
                        } finally {
                            setTabAt(tab, i, node);
                        }
                    }
                }
                if (binCount != 0)
                    break;
            } else if ((fh = f.hash) == MOVED)
                tab = helpTransfer(tab, f);
            else {
                boolean added = false;
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        if (fh >= 0) {
                            binCount = 1;
                            for (Node<K, V> e = f; ; ++binCount) {
                                K ek;
                                V ev;
                                if (e.hash == h && ((ek = e.key) == key || (ek != null && key.equals(ek)))) {
                                    val = e.val;
                                    break;
                                }
                                Node<K, V> pred = e;
                                if ((e = e.next) == null) {
                                    if ((val = mappingFunction.apply(key)) != null) {
                                        added = true;
                                        pred.next = new Node<K, V>(h, key, val, null);
                                    }
                                    break;
                                }
                            }
                        } else if (f instanceof TreeBin) {
                            binCount = 2;
                            TreeBin<K, V> t = (TreeBin<K, V>) f;
                            TreeNode<K, V> r, p;
                            if ((r = t.root) != null && (p = r.findTreeNode(h, key, null)) != null)
                                val = p.val;
                            else if ((val = mappingFunction.apply(key)) != null) {
                                added = true;
                                t.putTreeVal(h, key, val);
                            }
                        }
                    }
                }
                if (binCount != 0) {
                    if (binCount >= TREEIFY_THRESHOLD)
                        treeifyBin(tab, i);
                    if (!added)
                        return val;
                    break;
                }
            }
        }
        if (val != null)
            addCount(1L, binCount);
        return val;
    }

    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (key == null || remappingFunction == null)
            throw new NullPointerException();
        int h = spread(key.hashCode());
        V val = null;
        int delta = 0;
        int binCount = 0;
        for (Node<K, V>[] tab = table; ; ) {
            Node<K, V> f;
            int n, i, fh;
            if (tab == null || (n = tab.length) == 0)
                tab = initTable();
            else if ((f = tabAt(tab, i = (n - 1) & h)) == null)
                break;
            else if ((fh = f.hash) == MOVED)
                tab = helpTransfer(tab, f);
            else {
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        if (fh >= 0) {
                            binCount = 1;
                            for (Node<K, V> e = f, pred = null; ; ++binCount) {
                                K ek;
                                if (e.hash == h && ((ek = e.key) == key || (ek != null && key.equals(ek)))) {
                                    val = remappingFunction.apply(key, e.val);
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
                                if ((e = e.next) == null)
                                    break;
                            }
                        } else if (f instanceof TreeBin) {
                            binCount = 2;
                            TreeBin<K, V> t = (TreeBin<K, V>) f;
                            TreeNode<K, V> r, p;
                            if ((r = t.root) != null && (p = r.findTreeNode(h, key, null)) != null) {
                                val = remappingFunction.apply(key, p.val);
                                if (val != null)
                                    p.val = val;
                                else {
                                    delta = -1;
                                    if (t.removeTreeNode(p))
                                        setTabAt(tab, i, untreeify(t.first));
                                }
                            }
                        }
                    }
                }
                if (binCount != 0)
                    break;
            }
        }
        if (delta != 0)
            addCount((long) delta, binCount);
        return val;
    }

    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (key == null || remappingFunction == null)
            throw new NullPointerException();
        int h = spread(key.hashCode());
        V val = null;
        int delta = 0;
        int binCount = 0;
        for (Node<K, V>[] tab = table; ; ) {
            Node<K, V> f;
            int n, i, fh;
            if (tab == null || (n = tab.length) == 0)
                tab = initTable();
            else if ((f = tabAt(tab, i = (n - 1) & h)) == null) {
                Node<K, V> r = new ReservationNode<K, V>();
                synchronized (r) {
                    if (casTabAt(tab, i, null, r)) {
                        binCount = 1;
                        Node<K, V> node = null;
                        try {
                            if ((val = remappingFunction.apply(key, null)) != null) {
                                delta = 1;
                                node = new Node<K, V>(h, key, val, null);
                            }
                        } finally {
                            setTabAt(tab, i, node);
                        }
                    }
                }
                if (binCount != 0)
                    break;
            } else if ((fh = f.hash) == MOVED)
                tab = helpTransfer(tab, f);
            else {
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        if (fh >= 0) {
                            binCount = 1;
                            for (Node<K, V> e = f, pred = null; ; ++binCount) {
                                K ek;
                                if (e.hash == h && ((ek = e.key) == key || (ek != null && key.equals(ek)))) {
                                    val = remappingFunction.apply(key, e.val);
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
                                    val = remappingFunction.apply(key, null);
                                    if (val != null) {
                                        delta = 1;
                                        pred.next = new Node<K, V>(h, key, val, null);
                                    }
                                    break;
                                }
                            }
                        } else if (f instanceof TreeBin) {
                            binCount = 1;
                            TreeBin<K, V> t = (TreeBin<K, V>) f;
                            TreeNode<K, V> r, p;
                            if ((r = t.root) != null)
                                p = r.findTreeNode(h, key, null);
                            else
                                p = null;
                            V pv = (p == null) ? null : p.val;
                            val = remappingFunction.apply(key, pv);
                            if (val != null) {
                                if (p != null)
                                    p.val = val;
                                else {
                                    delta = 1;
                                    t.putTreeVal(h, key, val);
                                }
                            } else if (p != null) {
                                delta = -1;
                                if (t.removeTreeNode(p))
                                    setTabAt(tab, i, untreeify(t.first));
                            }
                        }
                    }
                }
                if (binCount != 0) {
                    if (binCount >= TREEIFY_THRESHOLD)
                        treeifyBin(tab, i);
                    break;
                }
            }
        }
        if (delta != 0)
            addCount((long) delta, binCount);
        return val;
    }

    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (key == null || value == null || remappingFunction == null)
            throw new NullPointerException();
        int h = spread(key.hashCode());
        V val = null;
        int delta = 0;
        int binCount = 0;
        for (Node<K, V>[] tab = table; ; ) {
            Node<K, V> f;
            int n, i, fh;
            if (tab == null || (n = tab.length) == 0)
                tab = initTable();
            else if ((f = tabAt(tab, i = (n - 1) & h)) == null) {
                if (casTabAt(tab, i, null, new Node<K, V>(h, key, value, null))) {
                    delta = 1;
                    val = value;
                    break;
                }
            } else if ((fh = f.hash) == MOVED)
                tab = helpTransfer(tab, f);
            else {
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        if (fh >= 0) {
                            binCount = 1;
                            for (Node<K, V> e = f, pred = null; ; ++binCount) {
                                K ek;
                                if (e.hash == h && ((ek = e.key) == key || (ek != null && key.equals(ek)))) {
                                    val = remappingFunction.apply(e.val, value);
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
                                    val = value;
                                    pred.next = new Node<K, V>(h, key, val, null);
                                    break;
                                }
                            }
                        } else if (f instanceof TreeBin) {
                            binCount = 2;
                            TreeBin<K, V> t = (TreeBin<K, V>) f;
                            TreeNode<K, V> r = t.root;
                            TreeNode<K, V> p = (r == null) ? null : r.findTreeNode(h, key, null);
                            val = (p == null) ? value : remappingFunction.apply(p.val, value);
                            if (val != null) {
                                if (p != null)
                                    p.val = val;
                                else {
                                    delta = 1;
                                    t.putTreeVal(h, key, val);
                                }
                            } else if (p != null) {
                                delta = -1;
                                if (t.removeTreeNode(p))
                                    setTabAt(tab, i, untreeify(t.first));
                            }
                        }
                    }
                }
                if (binCount != 0) {
                    if (binCount >= TREEIFY_THRESHOLD)
                        treeifyBin(tab, i);
                    break;
                }
            }
        }
        if (delta != 0)
            addCount((long) delta, binCount);
        return val;
    }

    public boolean contains(Object value) {
        return containsValue(value);
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

    public long mappingCount() {
        long n = sumCount();
        return (n < 0L) ? 0L : n;
    }

    public static <K> KeySetView<K, Boolean> newKeySet() {
        return new KeySetView<K, Boolean>(new ConcurrentHashMap<K, Boolean>(), Boolean.TRUE);
    }

    public static <K> KeySetView<K, Boolean> newKeySet(int initialCapacity) {
        return new KeySetView<K, Boolean>(new ConcurrentHashMap<K, Boolean>(initialCapacity), Boolean.TRUE);
    }

    public KeySetView<K, V> keySet(V mappedValue) {
        if (mappedValue == null)
            throw new NullPointerException();
        return new KeySetView<K, V>(this, mappedValue);
    }

    static final class ForwardingNode<K, V> extends Node<K, V> {

        final Node<K, V>[] nextTable;

        ForwardingNode(Node<K, V>[] tab) {
            super(MOVED, null, null, null);
            this.nextTable = tab;
        }

        Node<K, V> find(int h, Object k) {
            outer: for (Node<K, V>[] tab = nextTable; ; ) {
                Node<K, V> e;
                int n;
                if (k == null || tab == null || (n = tab.length) == 0 || (e = tabAt(tab, (n - 1) & h)) == null)
                    return null;
                for (; ; ) {
                    int eh;
                    K ek;
                    if ((eh = e.hash) == h && ((ek = e.key) == k || (ek != null && k.equals(ek))))
                        return e;
                    if (eh < 0) {
                        if (e instanceof ForwardingNode) {
                            tab = ((ForwardingNode<K, V>) e).nextTable;
                            continue outer;
                        } else
                            return e.find(h, k);
                    }
                    if ((e = e.next) == null)
                        return null;
                }
            }
        }
    }

    static final class ReservationNode<K, V> extends Node<K, V> {

        ReservationNode() {
            super(RESERVED, null, null, null);
        }

        Node<K, V> find(int h, Object k) {
            return null;
        }
    }

    private final Node<K, V>[] initTable() {
        Node<K, V>[] tab;
        int sc;
        while ((tab = table) == null || tab.length == 0) {
            if ((sc = sizeCtl) < 0)
                Thread.yield();
            else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
                try {
                    if ((tab = table) == null || tab.length == 0) {
                        int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                        @SuppressWarnings({ "rawtypes", "unchecked" })
                        Node<K, V>[] nt = (Node<K, V>[]) new Node[n];
                        table = tab = nt;
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
        CounterCell[] as;
        long b, s;
        if ((as = counterCells) != null || !U.compareAndSwapLong(this, BASECOUNT, b = baseCount, s = b + x)) {
            CounterCell a;
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

    final Node<K, V>[] helpTransfer(Node<K, V>[] tab, Node<K, V> f) {
        Node<K, V>[] nextTab;
        int sc;
        if ((f instanceof ForwardingNode) && (nextTab = ((ForwardingNode<K, V>) f).nextTable) != null) {
            if (nextTab == nextTable && tab == table && transferIndex > transferOrigin && (sc = sizeCtl) < -1 && U.compareAndSwapInt(this, SIZECTL, sc, sc - 1))
                transfer(tab, nextTab);
            return nextTab;
        }
        return table;
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
                            @SuppressWarnings({ "rawtypes", "unchecked" })
                            Node<K, V>[] nt = (Node<K, V>[]) new Node[n];
                            table = nt;
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
                @SuppressWarnings({ "rawtypes", "unchecked" })
                Node<K, V>[] nt = (Node<K, V>[]) new Node[n << 1];
                nextTab = nt;
            } catch (Throwable ex) {
                sizeCtl = Integer.MAX_VALUE;
                return;
            }
            nextTable = nextTab;
            transferOrigin = n;
            transferIndex = n;
            ForwardingNode<K, V> rev = new ForwardingNode<K, V>(tab);
            for (int k = n; k > 0; ) {
                int nextk = (k > stride) ? k - stride : 0;
                for (int m = nextk; m < k; ++m) nextTab[m] = rev;
                for (int m = n + nextk; m < n + k; ++m) nextTab[m] = rev;
                U.putOrderedInt(this, TRANSFERORIGIN, k = nextk);
            }
        }
        int nextn = nextTab.length;
        ForwardingNode<K, V> fwd = new ForwardingNode<K, V>(nextTab);
        boolean advance = true;
        boolean finishing = false;
        for (int i = 0, bound = 0; ; ) {
            int nextIndex, nextBound, fh;
            Node<K, V> f;
            while (advance) {
                if (--i >= bound || finishing)
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
                if (finishing) {
                    nextTable = null;
                    table = nextTab;
                    sizeCtl = (n << 1) - (n >>> 1);
                    return;
                }
                for (int sc; ; ) {
                    if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, ++sc)) {
                        if (sc != -1)
                            return;
                        finishing = advance = true;
                        i = n;
                        break;
                    }
                }
            } else if ((f = tabAt(tab, i)) == null) {
                if (casTabAt(tab, i, null, fwd)) {
                    setTabAt(nextTab, i, null);
                    setTabAt(nextTab, i + n, null);
                    advance = true;
                }
            } else if ((fh = f.hash) == MOVED)
                advance = true;
            else {
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        Node<K, V> ln, hn;
                        if (fh >= 0) {
                            int runBit = fh & n;
                            Node<K, V> lastRun = f;
                            for (Node<K, V> p = f.next; p != null; p = p.next) {
                                int b = p.hash & n;
                                if (b != runBit) {
                                    runBit = b;
                                    lastRun = p;
                                }
                            }
                            if (runBit == 0) {
                                ln = lastRun;
                                hn = null;
                            } else {
                                hn = lastRun;
                                ln = null;
                            }
                            for (Node<K, V> p = f; p != lastRun; p = p.next) {
                                int ph = p.hash;
                                K pk = p.key;
                                V pv = p.val;
                                if ((ph & n) == 0)
                                    ln = new Node<K, V>(ph, pk, pv, ln);
                                else
                                    hn = new Node<K, V>(ph, pk, pv, hn);
                            }
                            setTabAt(nextTab, i, ln);
                            setTabAt(nextTab, i + n, hn);
                            setTabAt(tab, i, fwd);
                            advance = true;
                        } else if (f instanceof TreeBin) {
                            TreeBin<K, V> t = (TreeBin<K, V>) f;
                            TreeNode<K, V> lo = null, loTail = null;
                            TreeNode<K, V> hi = null, hiTail = null;
                            int lc = 0, hc = 0;
                            for (Node<K, V> e = t.first; e != null; e = e.next) {
                                int h = e.hash;
                                TreeNode<K, V> p = new TreeNode<K, V>(h, e.key, e.val, null, null);
                                if ((h & n) == 0) {
                                    if ((p.prev = loTail) == null)
                                        lo = p;
                                    else
                                        loTail.next = p;
                                    loTail = p;
                                    ++lc;
                                } else {
                                    if ((p.prev = hiTail) == null)
                                        hi = p;
                                    else
                                        hiTail.next = p;
                                    hiTail = p;
                                    ++hc;
                                }
                            }
                            ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) : (hc != 0) ? new TreeBin<K, V>(lo) : t;
                            hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) : (lc != 0) ? new TreeBin<K, V>(hi) : t;
                            setTabAt(nextTab, i, ln);
                            setTabAt(nextTab, i + n, hn);
                            setTabAt(tab, i, fwd);
                            advance = true;
                        }
                    }
                }
            }
        }
    }

    @sun.misc.Contended
    static final class CounterCell {

        volatile long value;

        CounterCell(long x) {
            value = x;
        }
    }

    final long sumCount() {
        CounterCell[] as = counterCells;
        CounterCell a;
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
            CounterCell[] as;
            CounterCell a;
            int n;
            long v;
            if ((as = counterCells) != null && (n = as.length) > 0) {
                if ((a = as[(n - 1) & h]) == null) {
                    if (cellsBusy == 0) {
                        CounterCell r = new CounterCell(x);
                        if (cellsBusy == 0 && U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                            boolean created = false;
                            try {
                                CounterCell[] rs;
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
                            CounterCell[] rs = new CounterCell[n << 1];
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
                        CounterCell[] rs = new CounterCell[2];
                        rs[h & 1] = new CounterCell(x);
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

    private final void treeifyBin(Node<K, V>[] tab, int index) {
        Node<K, V> b;
        int n, sc;
        if (tab != null) {
            if ((n = tab.length) < MIN_TREEIFY_CAPACITY) {
                if (tab == table && (sc = sizeCtl) >= 0 && U.compareAndSwapInt(this, SIZECTL, sc, -2))
                    transfer(tab, null);
            } else if ((b = tabAt(tab, index)) != null && b.hash >= 0) {
                synchronized (b) {
                    if (tabAt(tab, index) == b) {
                        TreeNode<K, V> hd = null, tl = null;
                        for (Node<K, V> e = b; e != null; e = e.next) {
                            TreeNode<K, V> p = new TreeNode<K, V>(e.hash, e.key, e.val, null, null);
                            if ((p.prev = tl) == null)
                                hd = p;
                            else
                                tl.next = p;
                            tl = p;
                        }
                        setTabAt(tab, index, new TreeBin<K, V>(hd));
                    }
                }
            }
        }
    }

    static <K, V> Node<K, V> untreeify(Node<K, V> b) {
        Node<K, V> hd = null, tl = null;
        for (Node<K, V> q = b; q != null; q = q.next) {
            Node<K, V> p = new Node<K, V>(q.hash, q.key, q.val, null);
            if (tl == null)
                hd = p;
            else
                tl.next = p;
            tl = p;
        }
        return hd;
    }

    static final class TreeNode<K, V> extends Node<K, V> {

        TreeNode<K, V> parent;

        TreeNode<K, V> left;

        TreeNode<K, V> right;

        TreeNode<K, V> prev;

        boolean red;

        TreeNode(int hash, K key, V val, Node<K, V> next, TreeNode<K, V> parent) {
            super(hash, key, val, next);
            this.parent = parent;
        }

        Node<K, V> find(int h, Object k) {
            return findTreeNode(h, k, null);
        }

        final TreeNode<K, V> findTreeNode(int h, Object k, Class<?> kc) {
            if (k != null) {
                TreeNode<K, V> p = this;
                do {
                    int ph, dir;
                    K pk;
                    TreeNode<K, V> q;
                    TreeNode<K, V> pl = p.left, pr = p.right;
                    if ((ph = p.hash) > h)
                        p = pl;
                    else if (ph < h)
                        p = pr;
                    else if ((pk = p.key) == k || (pk != null && k.equals(pk)))
                        return p;
                    else if (pl == null)
                        p = pr;
                    else if (pr == null)
                        p = pl;
                    else if ((kc != null || (kc = comparableClassFor(k)) != null) && (dir = compareComparables(kc, k, pk)) != 0)
                        p = (dir < 0) ? pl : pr;
                    else if ((q = pr.findTreeNode(h, k, kc)) != null)
                        return q;
                    else
                        p = pl;
                } while (p != null);
            }
            return null;
        }
    }

    static final class TreeBin<K, V> extends Node<K, V> {

        TreeNode<K, V> root;

        volatile TreeNode<K, V> first;

        volatile Thread waiter;

        volatile int lockState;

        static final int WRITER = 1;

        static final int WAITER = 2;

        static final int READER = 4;

        static int tieBreakOrder(Object a, Object b) {
            int d;
            if (a == null || b == null || (d = a.getClass().getName().compareTo(b.getClass().getName())) == 0)
                d = (System.identityHashCode(a) <= System.identityHashCode(b) ? -1 : 1);
            return d;
        }

        TreeBin(TreeNode<K, V> b) {
            super(TREEBIN, null, null, null);
            this.first = b;
            TreeNode<K, V> r = null;
            for (TreeNode<K, V> x = b, next; x != null; x = next) {
                next = (TreeNode<K, V>) x.next;
                x.left = x.right = null;
                if (r == null) {
                    x.parent = null;
                    x.red = false;
                    r = x;
                } else {
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    for (TreeNode<K, V> p = r; ; ) {
                        int dir, ph;
                        K pk = p.key;
                        if ((ph = p.hash) > h)
                            dir = -1;
                        else if (ph < h)
                            dir = 1;
                        else if ((kc == null && (kc = comparableClassFor(k)) == null) || (dir = compareComparables(kc, k, pk)) == 0)
                            dir = tieBreakOrder(k, pk);
                        TreeNode<K, V> xp = p;
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp;
                            if (dir <= 0)
                                xp.left = x;
                            else
                                xp.right = x;
                            r = balanceInsertion(r, x);
                            break;
                        }
                    }
                }
            }
            this.root = r;
            assert checkInvariants(root);
        }

        private final void lockRoot() {
            if (!U.compareAndSwapInt(this, LOCKSTATE, 0, WRITER))
                contendedLock();
        }

        private final void unlockRoot() {
            lockState = 0;
        }

        private final void contendedLock() {
            boolean waiting = false;
            for (int s; ; ) {
                if (((s = lockState) & WRITER) == 0) {
                    if (U.compareAndSwapInt(this, LOCKSTATE, s, WRITER)) {
                        if (waiting)
                            waiter = null;
                        return;
                    }
                } else if ((s | WAITER) == 0) {
                    if (U.compareAndSwapInt(this, LOCKSTATE, s, s | WAITER)) {
                        waiting = true;
                        waiter = Thread.currentThread();
                    }
                } else if (waiting)
                    LockSupport.park(this);
            }
        }

        final Node<K, V> find(int h, Object k) {
            if (k != null) {
                for (Node<K, V> e = first; e != null; e = e.next) {
                    int s;
                    K ek;
                    if (((s = lockState) & (WAITER | WRITER)) != 0) {
                        if (e.hash == h && ((ek = e.key) == k || (ek != null && k.equals(ek))))
                            return e;
                    } else if (U.compareAndSwapInt(this, LOCKSTATE, s, s + READER)) {
                        TreeNode<K, V> r, p;
                        try {
                            p = ((r = root) == null ? null : r.findTreeNode(h, k, null));
                        } finally {
                            Thread w;
                            if (U.getAndAddInt(this, LOCKSTATE, -READER) == (READER | WAITER) && (w = waiter) != null)
                                LockSupport.unpark(w);
                        }
                        return p;
                    }
                }
            }
            return null;
        }

        final TreeNode<K, V> putTreeVal(int h, K k, V v) {
            Class<?> kc = null;
            boolean searched = false;
            for (TreeNode<K, V> p = root; ; ) {
                int dir, ph;
                K pk;
                if (p == null) {
                    first = root = new TreeNode<K, V>(h, k, v, null, null);
                    break;
                } else if ((ph = p.hash) > h)
                    dir = -1;
                else if (ph < h)
                    dir = 1;
                else if ((pk = p.key) == k || (pk != null && k.equals(pk)))
                    return p;
                else if ((kc == null && (kc = comparableClassFor(k)) == null) || (dir = compareComparables(kc, k, pk)) == 0) {
                    if (!searched) {
                        TreeNode<K, V> q, ch;
                        searched = true;
                        if (((ch = p.left) != null && (q = ch.findTreeNode(h, k, kc)) != null) || ((ch = p.right) != null && (q = ch.findTreeNode(h, k, kc)) != null))
                            return q;
                    }
                    dir = tieBreakOrder(k, pk);
                }
                TreeNode<K, V> xp = p;
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    TreeNode<K, V> x, f = first;
                    first = x = new TreeNode<K, V>(h, k, v, f, xp);
                    if (f != null)
                        f.prev = x;
                    if (dir <= 0)
                        xp.left = x;
                    else
                        xp.right = x;
                    if (!xp.red)
                        x.red = true;
                    else {
                        lockRoot();
                        try {
                            root = balanceInsertion(root, x);
                        } finally {
                            unlockRoot();
                        }
                    }
                    break;
                }
            }
            assert checkInvariants(root);
            return null;
        }

        final boolean removeTreeNode(TreeNode<K, V> p) {
            TreeNode<K, V> next = (TreeNode<K, V>) p.next;
            TreeNode<K, V> pred = p.prev;
            TreeNode<K, V> r, rl;
            if (pred == null)
                first = next;
            else
                pred.next = next;
            if (next != null)
                next.prev = pred;
            if (first == null) {
                root = null;
                return true;
            }
            if ((r = root) == null || r.right == null || (rl = r.left) == null || rl.left == null)
                return true;
            lockRoot();
            try {
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
                        r = s;
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
                        r = replacement;
                    else if (p == pp.left)
                        pp.left = replacement;
                    else
                        pp.right = replacement;
                    p.left = p.right = p.parent = null;
                }
                root = (p.red) ? r : balanceDeletion(r, replacement);
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
            } finally {
                unlockRoot();
            }
            assert checkInvariants(root);
            return false;
        }

        static <K, V> TreeNode<K, V> rotateLeft(TreeNode<K, V> root, TreeNode<K, V> p) {
            TreeNode<K, V> r, pp, rl;
            if (p != null && (r = p.right) != null) {
                if ((rl = p.right = r.left) != null)
                    rl.parent = p;
                if ((pp = r.parent = p.parent) == null)
                    (root = r).red = false;
                else if (pp.left == p)
                    pp.left = r;
                else
                    pp.right = r;
                r.left = p;
                p.parent = r;
            }
            return root;
        }

        static <K, V> TreeNode<K, V> rotateRight(TreeNode<K, V> root, TreeNode<K, V> p) {
            TreeNode<K, V> l, pp, lr;
            if (p != null && (l = p.left) != null) {
                if ((lr = p.left = l.right) != null)
                    lr.parent = p;
                if ((pp = l.parent = p.parent) == null)
                    (root = l).red = false;
                else if (pp.right == p)
                    pp.right = l;
                else
                    pp.left = l;
                l.right = p;
                p.parent = l;
            }
            return root;
        }

        static <K, V> TreeNode<K, V> balanceInsertion(TreeNode<K, V> root, TreeNode<K, V> x) {
            x.red = true;
            for (TreeNode<K, V> xp, xpp, xppl, xppr; ; ) {
                if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                } else if (!xp.red || (xpp = xp.parent) == null)
                    return root;
                if (xp == (xppl = xpp.left)) {
                    if ((xppr = xpp.right) != null && xppr.red) {
                        xppr.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    } else {
                        if (x == xp.right) {
                            root = rotateLeft(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateRight(root, xpp);
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
                            root = rotateRight(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateLeft(root, xpp);
                            }
                        }
                    }
                }
            }
        }

        static <K, V> TreeNode<K, V> balanceDeletion(TreeNode<K, V> root, TreeNode<K, V> x) {
            for (TreeNode<K, V> xp, xpl, xpr; ; ) {
                if (x == null || x == root)
                    return root;
                else if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                } else if (x.red) {
                    x.red = false;
                    return root;
                } else if ((xpl = xp.left) == x) {
                    if ((xpr = xp.right) != null && xpr.red) {
                        xpr.red = false;
                        xp.red = true;
                        root = rotateLeft(root, xp);
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
                                root = rotateRight(root, xpr);
                                xpr = (xp = x.parent) == null ? null : xp.right;
                            }
                            if (xpr != null) {
                                xpr.red = (xp == null) ? false : xp.red;
                                if ((sr = xpr.right) != null)
                                    sr.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateLeft(root, xp);
                            }
                            x = root;
                        }
                    }
                } else {
                    if (xpl != null && xpl.red) {
                        xpl.red = false;
                        xp.red = true;
                        root = rotateRight(root, xp);
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
                                root = rotateLeft(root, xpl);
                                xpl = (xp = x.parent) == null ? null : xp.left;
                            }
                            if (xpl != null) {
                                xpl.red = (xp == null) ? false : xp.red;
                                if ((sl = xpl.left) != null)
                                    sl.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateRight(root, xp);
                            }
                            x = root;
                        }
                    }
                }
            }
        }

        static <K, V> boolean checkInvariants(TreeNode<K, V> t) {
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
            if (tl != null && !checkInvariants(tl))
                return false;
            if (tr != null && !checkInvariants(tr))
                return false;
            return true;
        }

        private static final sun.misc.Unsafe U;

        private static final long LOCKSTATE;

        static {
            try {
                U = sun.misc.Unsafe.getUnsafe();
                Class<?> k = TreeBin.class;
                LOCKSTATE = U.objectFieldOffset(k.getDeclaredField("lockState"));
            } catch (Exception e) {
                throw new Error(e);
            }
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
                K ek;
                if (e != null)
                    return next = e;
                if (baseIndex >= baseLimit || (t = tab) == null || (n = t.length) <= (i = index) || i < 0)
                    return next = null;
                if ((e = tabAt(t, index)) != null && e.hash < 0) {
                    if (e instanceof ForwardingNode) {
                        tab = ((ForwardingNode<K, V>) e).nextTable;
                        e = null;
                        continue;
                    } else if (e instanceof TreeBin)
                        e = ((TreeBin<K, V>) e).first;
                    else
                        e = null;
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
            map.replaceNode(p.key, null, null);
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
            K k = p.key;
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
            K k = p.key;
            V v = p.val;
            lastReturned = p;
            advance();
            return new MapEntry<K, V>(k, v, map);
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
            for (Node<K, V> p; (p = advance()) != null; ) action.accept(p.key);
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            if (action == null)
                throw new NullPointerException();
            Node<K, V> p;
            if ((p = advance()) == null)
                return false;
            action.accept(p.key);
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
            for (Node<K, V> p; (p = advance()) != null; ) action.accept(new MapEntry<K, V>(p.key, p.val, map));
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
            if (action == null)
                throw new NullPointerException();
            Node<K, V> p;
            if ((p = advance()) == null)
                return false;
            action.accept(new MapEntry<K, V>(p.key, p.val, map));
            return true;
        }

        public long estimateSize() {
            return est;
        }

        public int characteristics() {
            return Spliterator.DISTINCT | Spliterator.CONCURRENT | Spliterator.NONNULL;
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

    public double reduceToDouble(long parallelismThreshold, ToDoubleBiFunction<? super K, ? super V> transformer, double basis, DoubleBinaryOperator reducer) {
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

        @SuppressWarnings("unchecked")
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
            return map.putVal(e, v, true) == null;
        }

        public boolean addAll(Collection<? extends K> c) {
            boolean added = false;
            V v;
            if ((v = value) == null)
                throw new UnsupportedOperationException();
            for (K e : c) {
                if (map.putVal(e, v, true) == null)
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
                for (Node<K, V> p; (p = it.advance()) != null; ) action.accept(p.key);
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
            return map.putVal(e.getKey(), e.getValue(), false) == null;
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
                for (Node<K, V> p; (p = it.advance()) != null; ) action.accept(new MapEntry<K, V>(p.key, p.val, map));
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
                K ek;
                if (e != null)
                    return next = e;
                if (baseIndex >= baseLimit || (t = tab) == null || (n = t.length) <= (i = index) || i < 0)
                    return next = null;
                if ((e = tabAt(t, index)) != null && e.hash < 0) {
                    if (e instanceof ForwardingNode) {
                        tab = ((ForwardingNode<K, V>) e).nextTable;
                        e = null;
                        continue;
                    } else if (e instanceof TreeBin)
                        e = ((TreeBin<K, V>) e).first;
                    else
                        e = null;
                }
                if ((index += baseSize) >= n)
                    index = ++baseIndex;
            }
        }
    }

    @SuppressWarnings("serial")
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
                for (Node<K, V> p; (p = advance()) != null; ) action.accept(p.key);
                propagateCompletion();
            }
        }
    }

    @SuppressWarnings("serial")
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

    @SuppressWarnings("serial")
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

    @SuppressWarnings("serial")
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
                for (Node<K, V> p; (p = advance()) != null; ) action.accept(p.key, p.val);
                propagateCompletion();
            }
        }
    }

    @SuppressWarnings("serial")
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
                    if ((u = transformer.apply(p.key)) != null)
                        action.accept(u);
                }
                propagateCompletion();
            }
        }
    }

    @SuppressWarnings("serial")
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

    @SuppressWarnings("serial")
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

    @SuppressWarnings("serial")
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
                    if ((u = transformer.apply(p.key, p.val)) != null)
                        action.accept(u);
                }
                propagateCompletion();
            }
        }
    }

    @SuppressWarnings("serial")
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
                    if ((u = searchFunction.apply(p.key)) != null) {
                        if (result.compareAndSet(null, u))
                            quietlyCompleteRoot();
                        break;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
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

    @SuppressWarnings("serial")
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

    @SuppressWarnings("serial")
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
                    if ((u = searchFunction.apply(p.key, p.val)) != null) {
                        if (result.compareAndSet(null, u))
                            quietlyCompleteRoot();
                        break;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
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
                    K u = p.key;
                    r = (r == null) ? u : u == null ? r : reducer.apply(r, u);
                }
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("serial")
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
                    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("serial")
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
                    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("serial")
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
                    if ((u = transformer.apply(p.key)) != null)
                        r = (r == null) ? u : reducer.apply(r, u);
                }
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("serial")
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
                    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("serial")
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
                    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("serial")
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
                    if ((u = transformer.apply(p.key, p.val)) != null)
                        r = (r == null) ? u : reducer.apply(r, u);
                }
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("serial")
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
                for (Node<K, V> p; (p = advance()) != null; ) r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.key));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceKeysToDoubleTask<K, V> t = (MapReduceKeysToDoubleTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsDouble(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
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
                    @SuppressWarnings("unchecked")
                    MapReduceValuesToDoubleTask<K, V> t = (MapReduceValuesToDoubleTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsDouble(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
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
                    @SuppressWarnings("unchecked")
                    MapReduceEntriesToDoubleTask<K, V> t = (MapReduceEntriesToDoubleTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsDouble(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
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
                for (Node<K, V> p; (p = advance()) != null; ) r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.key, p.val));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceMappingsToDoubleTask<K, V> t = (MapReduceMappingsToDoubleTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsDouble(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
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
                for (Node<K, V> p; (p = advance()) != null; ) r = reducer.applyAsLong(r, transformer.applyAsLong(p.key));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceKeysToLongTask<K, V> t = (MapReduceKeysToLongTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsLong(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
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
                    @SuppressWarnings("unchecked")
                    MapReduceValuesToLongTask<K, V> t = (MapReduceValuesToLongTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsLong(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
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
                    @SuppressWarnings("unchecked")
                    MapReduceEntriesToLongTask<K, V> t = (MapReduceEntriesToLongTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsLong(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
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
                for (Node<K, V> p; (p = advance()) != null; ) r = reducer.applyAsLong(r, transformer.applyAsLong(p.key, p.val));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceMappingsToLongTask<K, V> t = (MapReduceMappingsToLongTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsLong(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
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
                for (Node<K, V> p; (p = advance()) != null; ) r = reducer.applyAsInt(r, transformer.applyAsInt(p.key));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
                    MapReduceKeysToIntTask<K, V> t = (MapReduceKeysToIntTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsInt(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
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
                    @SuppressWarnings("unchecked")
                    MapReduceValuesToIntTask<K, V> t = (MapReduceValuesToIntTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsInt(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
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
                    @SuppressWarnings("unchecked")
                    MapReduceEntriesToIntTask<K, V> t = (MapReduceEntriesToIntTask<K, V>) c, s = t.rights;
                    while (s != null) {
                        t.result = reducer.applyAsInt(t.result, s.result);
                        s = t.rights = s.nextRight;
                    }
                }
            }
        }
    }

    @SuppressWarnings("serial")
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
                for (Node<K, V> p; (p = advance()) != null; ) r = reducer.applyAsInt(r, transformer.applyAsInt(p.key, p.val));
                result = r;
                CountedCompleter<?> c;
                for (c = firstComplete(); c != null; c = c.nextComplete()) {
                    @SuppressWarnings("unchecked")
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
            Class<?> ck = CounterCell.class;
            CELLVALUE = U.objectFieldOffset(ck.getDeclaredField("value"));
            Class<?> ak = Node[].class;
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
