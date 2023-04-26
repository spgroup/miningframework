package java.util.concurrent;

import java.util.*;

@SuppressWarnings("unchecked")
public class ConcurrentSkipListMap<K, V> extends AbstractMap<K, V> implements ConcurrentNavigableMap<K, V>, Cloneable, java.io.Serializable {

    private static final long serialVersionUID = -8627078645895051609L;

    private static final Random seedGenerator = new Random();

    private static final Object BASE_HEADER = new Object();

    private transient volatile HeadIndex<K, V> head;

    private final Comparator<? super K> comparator;

    private transient int randomSeed;

    private transient KeySet<K> keySet;

    private transient EntrySet<K, V> entrySet;

    private transient Values<V> values;

    private transient ConcurrentNavigableMap<K, V> descendingMap;

    final void initialize() {
        keySet = null;
        entrySet = null;
        values = null;
        descendingMap = null;
        randomSeed = seedGenerator.nextInt() | 0x0100;
        head = new HeadIndex<K, V>(new Node<K, V>(null, BASE_HEADER, null), null, null, 1);
    }

    private boolean casHead(HeadIndex<K, V> cmp, HeadIndex<K, V> val) {
        return UNSAFE.compareAndSwapObject(this, headOffset, cmp, val);
    }

    static final class Node<K, V> {

        final K key;

        volatile Object value;

        volatile Node<K, V> next;

        Node(K key, Object value, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        Node(Node<K, V> next) {
            this.key = null;
            this.value = this;
            this.next = next;
        }

        boolean casValue(Object cmp, Object val) {
            return UNSAFE.compareAndSwapObject(this, valueOffset, cmp, val);
        }

        boolean casNext(Node<K, V> cmp, Node<K, V> val) {
            return UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
        }

        boolean isMarker() {
            return value == this;
        }

        boolean isBaseHeader() {
            return value == BASE_HEADER;
        }

        boolean appendMarker(Node<K, V> f) {
            return casNext(f, new Node<K, V>(f));
        }

        void helpDelete(Node<K, V> b, Node<K, V> f) {
            if (f == next && this == b.next) {
                if (f == null || f.value != f)
                    appendMarker(f);
                else
                    b.casNext(this, f.next);
            }
        }

        V getValidValue() {
            Object v = value;
            if (v == this || v == BASE_HEADER)
                return null;
            return (V) v;
        }

        AbstractMap.SimpleImmutableEntry<K, V> createSnapshot() {
            V v = getValidValue();
            if (v == null)
                return null;
            return new AbstractMap.SimpleImmutableEntry<K, V>(key, v);
        }

        private static final sun.misc.Unsafe UNSAFE;

        private static final long valueOffset;

        private static final long nextOffset;

        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = Node.class;
                valueOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("value"));
                nextOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("next"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    static class Index<K, V> {

        final Node<K, V> node;

        final Index<K, V> down;

        volatile Index<K, V> right;

        Index(Node<K, V> node, Index<K, V> down, Index<K, V> right) {
            this.node = node;
            this.down = down;
            this.right = right;
        }

        final boolean casRight(Index<K, V> cmp, Index<K, V> val) {
            return UNSAFE.compareAndSwapObject(this, rightOffset, cmp, val);
        }

        final boolean indexesDeletedNode() {
            return node.value == null;
        }

        final boolean link(Index<K, V> succ, Index<K, V> newSucc) {
            Node<K, V> n = node;
            newSucc.right = succ;
            return n.value != null && casRight(succ, newSucc);
        }

        final boolean unlink(Index<K, V> succ) {
            return !indexesDeletedNode() && casRight(succ, succ.right);
        }

        private static final sun.misc.Unsafe UNSAFE;

        private static final long rightOffset;

        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = Index.class;
                rightOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("right"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    static final class HeadIndex<K, V> extends Index<K, V> {

        final int level;

        HeadIndex(Node<K, V> node, Index<K, V> down, Index<K, V> right, int level) {
            super(node, down, right);
            this.level = level;
        }
    }

    static final class ComparableUsingComparator<K> implements Comparable<K> {

        final K actualKey;

        final Comparator<? super K> cmp;

        ComparableUsingComparator(K key, Comparator<? super K> cmp) {
            this.actualKey = key;
            this.cmp = cmp;
        }

        public int compareTo(K k2) {
            return cmp.compare(actualKey, k2);
        }
    }

    private Comparable<? super K> comparable(Object key) throws ClassCastException {
        if (key == null)
            throw new NullPointerException();
        if (comparator != null)
            return new ComparableUsingComparator<K>((K) key, comparator);
        else
            return (Comparable<? super K>) key;
    }

    int compare(K k1, K k2) throws ClassCastException {
        Comparator<? super K> cmp = comparator;
        if (cmp != null)
            return cmp.compare(k1, k2);
        else
            return ((Comparable<? super K>) k1).compareTo(k2);
    }

    boolean inHalfOpenRange(K key, K least, K fence) {
        if (key == null)
            throw new NullPointerException();
        return ((least == null || compare(key, least) >= 0) && (fence == null || compare(key, fence) < 0));
    }

    boolean inOpenRange(K key, K least, K fence) {
        if (key == null)
            throw new NullPointerException();
        return ((least == null || compare(key, least) >= 0) && (fence == null || compare(key, fence) <= 0));
    }

    private Node<K, V> findPredecessor(Comparable<? super K> key) {
        if (key == null)
            throw new NullPointerException();
        for (; ; ) {
            Index<K, V> q = head;
            Index<K, V> r = q.right;
            for (; ; ) {
                if (r != null) {
                    Node<K, V> n = r.node;
                    K k = n.key;
                    if (n.value == null) {
                        if (!q.unlink(r))
                            break;
                        r = q.right;
                        continue;
                    }
                    if (key.compareTo(k) > 0) {
                        q = r;
                        r = r.right;
                        continue;
                    }
                }
                Index<K, V> d = q.down;
                if (d != null) {
                    q = d;
                    r = d.right;
                } else
                    return q.node;
            }
        }
    }

    private Node<K, V> findNode(Comparable<? super K> key) {
        for (; ; ) {
            Node<K, V> b = findPredecessor(key);
            Node<K, V> n = b.next;
            for (; ; ) {
                if (n == null)
                    return null;
                Node<K, V> f = n.next;
                if (n != b.next)
                    break;
                Object v = n.value;
                if (v == null) {
                    n.helpDelete(b, f);
                    break;
                }
                if (v == n || b.value == null)
                    break;
                int c = key.compareTo(n.key);
                if (c == 0)
                    return n;
                if (c < 0)
                    return null;
                b = n;
                n = f;
            }
        }
    }

    private V doGet(Object okey) {
        Comparable<? super K> key = comparable(okey);
        for (; ; ) {
            Node<K, V> n = findNode(key);
            if (n == null)
                return null;
            Object v = n.value;
            if (v != null)
                return (V) v;
        }
    }

    private V doPut(K kkey, V value, boolean onlyIfAbsent) {
        Comparable<? super K> key = comparable(kkey);
        for (; ; ) {
            Node<K, V> b = findPredecessor(key);
            Node<K, V> n = b.next;
            for (; ; ) {
                if (n != null) {
                    Node<K, V> f = n.next;
                    if (n != b.next)
                        break;
                    Object v = n.value;
                    if (v == null) {
                        n.helpDelete(b, f);
                        break;
                    }
                    if (v == n || b.value == null)
                        break;
                    int c = key.compareTo(n.key);
                    if (c > 0) {
                        b = n;
                        n = f;
                        continue;
                    }
                    if (c == 0) {
                        if (onlyIfAbsent || n.casValue(v, value))
                            return (V) v;
                        else
                            break;
                    }
                }
                Node<K, V> z = new Node<K, V>(kkey, value, n);
                if (!b.casNext(n, z))
                    break;
                int level = randomLevel();
                if (level > 0)
                    insertIndex(z, level);
                return null;
            }
        }
    }

    private int randomLevel() {
        int x = randomSeed;
        x ^= x << 13;
        x ^= x >>> 17;
        randomSeed = x ^= x << 5;
        if ((x & 0x80000001) != 0)
            return 0;
        int level = 1;
        while (((x >>>= 1) & 1) != 0) ++level;
        return level;
    }

    private void insertIndex(Node<K, V> z, int level) {
        HeadIndex<K, V> h = head;
        int max = h.level;
        if (level <= max) {
            Index<K, V> idx = null;
            for (int i = 1; i <= level; ++i) idx = new Index<K, V>(z, idx, null);
            addIndex(idx, h, level);
        } else {
            level = max + 1;
            Index<K, V>[] idxs = (Index<K, V>[]) new Index<?, ?>[level + 1];
            Index<K, V> idx = null;
            for (int i = 1; i <= level; ++i) idxs[i] = idx = new Index<K, V>(z, idx, null);
            HeadIndex<K, V> oldh;
            int k;
            for (; ; ) {
                oldh = head;
                int oldLevel = oldh.level;
                if (level <= oldLevel) {
                    k = level;
                    break;
                }
                HeadIndex<K, V> newh = oldh;
                Node<K, V> oldbase = oldh.node;
                for (int j = oldLevel + 1; j <= level; ++j) newh = new HeadIndex<K, V>(oldbase, newh, idxs[j], j);
                if (casHead(oldh, newh)) {
                    k = oldLevel;
                    break;
                }
            }
            addIndex(idxs[k], oldh, k);
        }
    }

    private void addIndex(Index<K, V> idx, HeadIndex<K, V> h, int indexLevel) {
        int insertionLevel = indexLevel;
        Comparable<? super K> key = comparable(idx.node.key);
        if (key == null)
            throw new NullPointerException();
        for (; ; ) {
            int j = h.level;
            Index<K, V> q = h;
            Index<K, V> r = q.right;
            Index<K, V> t = idx;
            for (; ; ) {
                if (r != null) {
                    Node<K, V> n = r.node;
                    int c = key.compareTo(n.key);
                    if (n.value == null) {
                        if (!q.unlink(r))
                            break;
                        r = q.right;
                        continue;
                    }
                    if (c > 0) {
                        q = r;
                        r = r.right;
                        continue;
                    }
                }
                if (j == insertionLevel) {
                    if (t.indexesDeletedNode()) {
                        findNode(key);
                        return;
                    }
                    if (!q.link(r, t))
                        break;
                    if (--insertionLevel == 0) {
                        if (t.indexesDeletedNode())
                            findNode(key);
                        return;
                    }
                }
                if (--j >= insertionLevel && j < indexLevel)
                    t = t.down;
                q = q.down;
                r = q.right;
            }
        }
    }

    final V doRemove(Object okey, Object value) {
        Comparable<? super K> key = comparable(okey);
        for (; ; ) {
            Node<K, V> b = findPredecessor(key);
            Node<K, V> n = b.next;
            for (; ; ) {
                if (n == null)
                    return null;
                Node<K, V> f = n.next;
                if (n != b.next)
                    break;
                Object v = n.value;
                if (v == null) {
                    n.helpDelete(b, f);
                    break;
                }
                if (v == n || b.value == null)
                    break;
                int c = key.compareTo(n.key);
                if (c < 0)
                    return null;
                if (c > 0) {
                    b = n;
                    n = f;
                    continue;
                }
                if (value != null && !value.equals(v))
                    return null;
                if (!n.casValue(v, null))
                    break;
                if (!n.appendMarker(f) || !b.casNext(n, f))
                    findNode(key);
                else {
                    findPredecessor(key);
                    if (head.right == null)
                        tryReduceLevel();
                }
                return (V) v;
            }
        }
    }

    private void tryReduceLevel() {
        HeadIndex<K, V> h = head;
        HeadIndex<K, V> d;
        HeadIndex<K, V> e;
        if (h.level > 3 && (d = (HeadIndex<K, V>) h.down) != null && (e = (HeadIndex<K, V>) d.down) != null && e.right == null && d.right == null && h.right == null && casHead(h, d) && h.right != null)
            casHead(d, h);
    }

    Node<K, V> findFirst() {
        for (; ; ) {
            Node<K, V> b = head.node;
            Node<K, V> n = b.next;
            if (n == null)
                return null;
            if (n.value != null)
                return n;
            n.helpDelete(b, n.next);
        }
    }

    Map.Entry<K, V> doRemoveFirstEntry() {
        for (; ; ) {
            Node<K, V> b = head.node;
            Node<K, V> n = b.next;
            if (n == null)
                return null;
            Node<K, V> f = n.next;
            if (n != b.next)
                continue;
            Object v = n.value;
            if (v == null) {
                n.helpDelete(b, f);
                continue;
            }
            if (!n.casValue(v, null))
                continue;
            if (!n.appendMarker(f) || !b.casNext(n, f))
                findFirst();
            clearIndexToFirst();
            return new AbstractMap.SimpleImmutableEntry<K, V>(n.key, (V) v);
        }
    }

    private void clearIndexToFirst() {
        for (; ; ) {
            Index<K, V> q = head;
            for (; ; ) {
                Index<K, V> r = q.right;
                if (r != null && r.indexesDeletedNode() && !q.unlink(r))
                    break;
                if ((q = q.down) == null) {
                    if (head.right == null)
                        tryReduceLevel();
                    return;
                }
            }
        }
    }

    Node<K, V> findLast() {
        Index<K, V> q = head;
        for (; ; ) {
            Index<K, V> d, r;
            if ((r = q.right) != null) {
                if (r.indexesDeletedNode()) {
                    q.unlink(r);
                    q = head;
                } else
                    q = r;
            } else if ((d = q.down) != null) {
                q = d;
            } else {
                Node<K, V> b = q.node;
                Node<K, V> n = b.next;
                for (; ; ) {
                    if (n == null)
                        return b.isBaseHeader() ? null : b;
                    Node<K, V> f = n.next;
                    if (n != b.next)
                        break;
                    Object v = n.value;
                    if (v == null) {
                        n.helpDelete(b, f);
                        break;
                    }
                    if (v == n || b.value == null)
                        break;
                    b = n;
                    n = f;
                }
                q = head;
            }
        }
    }

    private Node<K, V> findPredecessorOfLast() {
        for (; ; ) {
            Index<K, V> q = head;
            for (; ; ) {
                Index<K, V> d, r;
                if ((r = q.right) != null) {
                    if (r.indexesDeletedNode()) {
                        q.unlink(r);
                        break;
                    }
                    if (r.node.next != null) {
                        q = r;
                        continue;
                    }
                }
                if ((d = q.down) != null)
                    q = d;
                else
                    return q.node;
            }
        }
    }

    Map.Entry<K, V> doRemoveLastEntry() {
        for (; ; ) {
            Node<K, V> b = findPredecessorOfLast();
            Node<K, V> n = b.next;
            if (n == null) {
                if (b.isBaseHeader())
                    return null;
                else
                    continue;
            }
            for (; ; ) {
                Node<K, V> f = n.next;
                if (n != b.next)
                    break;
                Object v = n.value;
                if (v == null) {
                    n.helpDelete(b, f);
                    break;
                }
                if (v == n || b.value == null)
                    break;
                if (f != null) {
                    b = n;
                    n = f;
                    continue;
                }
                if (!n.casValue(v, null))
                    break;
                K key = n.key;
                Comparable<? super K> ck = comparable(key);
                if (!n.appendMarker(f) || !b.casNext(n, f))
                    findNode(ck);
                else {
                    findPredecessor(ck);
                    if (head.right == null)
                        tryReduceLevel();
                }
                return new AbstractMap.SimpleImmutableEntry<K, V>(key, (V) v);
            }
        }
    }

    private static final int EQ = 1;

    private static final int LT = 2;

    private static final int GT = 0;

    Node<K, V> findNear(K kkey, int rel) {
        Comparable<? super K> key = comparable(kkey);
        for (; ; ) {
            Node<K, V> b = findPredecessor(key);
            Node<K, V> n = b.next;
            for (; ; ) {
                if (n == null)
                    return ((rel & LT) == 0 || b.isBaseHeader()) ? null : b;
                Node<K, V> f = n.next;
                if (n != b.next)
                    break;
                Object v = n.value;
                if (v == null) {
                    n.helpDelete(b, f);
                    break;
                }
                if (v == n || b.value == null)
                    break;
                int c = key.compareTo(n.key);
                if ((c == 0 && (rel & EQ) != 0) || (c < 0 && (rel & LT) == 0))
                    return n;
                if (c <= 0 && (rel & LT) != 0)
                    return b.isBaseHeader() ? null : b;
                b = n;
                n = f;
            }
        }
    }

    AbstractMap.SimpleImmutableEntry<K, V> getNear(K key, int rel) {
        for (; ; ) {
            Node<K, V> n = findNear(key, rel);
            if (n == null)
                return null;
            AbstractMap.SimpleImmutableEntry<K, V> e = n.createSnapshot();
            if (e != null)
                return e;
        }
    }

    public ConcurrentSkipListMap() {
        this.comparator = null;
        initialize();
    }

    public ConcurrentSkipListMap(Comparator<? super K> comparator) {
        this.comparator = comparator;
        initialize();
    }

    public ConcurrentSkipListMap(Map<? extends K, ? extends V> m) {
        this.comparator = null;
        initialize();
        putAll(m);
    }

    public ConcurrentSkipListMap(SortedMap<K, ? extends V> m) {
        this.comparator = m.comparator();
        initialize();
        buildFromSorted(m);
    }

    public ConcurrentSkipListMap<K, V> clone() {
        try {
            @SuppressWarnings("unchecked")
            ConcurrentSkipListMap<K, V> clone = (ConcurrentSkipListMap<K, V>) super.clone();
            clone.initialize();
            clone.buildFromSorted(this);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    private void buildFromSorted(SortedMap<K, ? extends V> map) {
        if (map == null)
            throw new NullPointerException();
        HeadIndex<K, V> h = head;
        Node<K, V> basepred = h.node;
        ArrayList<Index<K, V>> preds = new ArrayList<Index<K, V>>();
        for (int i = 0; i <= h.level; ++i) preds.add(null);
        Index<K, V> q = h;
        for (int i = h.level; i > 0; --i) {
            preds.set(i, q);
            q = q.down;
        }
        Iterator<? extends Map.Entry<? extends K, ? extends V>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<? extends K, ? extends V> e = it.next();
            int j = randomLevel();
            if (j > h.level)
                j = h.level + 1;
            K k = e.getKey();
            V v = e.getValue();
            if (k == null || v == null)
                throw new NullPointerException();
            Node<K, V> z = new Node<K, V>(k, v, null);
            basepred.next = z;
            basepred = z;
            if (j > 0) {
                Index<K, V> idx = null;
                for (int i = 1; i <= j; ++i) {
                    idx = new Index<K, V>(z, idx, null);
                    if (i > h.level)
                        h = new HeadIndex<K, V>(h.node, h, idx, i);
                    if (i < preds.size()) {
                        preds.get(i).right = idx;
                        preds.set(i, idx);
                    } else
                        preds.add(idx);
                }
            }
        }
        head = h;
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.defaultWriteObject();
        for (Node<K, V> n = findFirst(); n != null; n = n.next) {
            V v = n.getValidValue();
            if (v != null) {
                s.writeObject(n.key);
                s.writeObject(v);
            }
        }
        s.writeObject(null);
    }

    private void readObject(final java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        initialize();
        HeadIndex<K, V> h = head;
        Node<K, V> basepred = h.node;
        ArrayList<Index<K, V>> preds = new ArrayList<Index<K, V>>();
        for (int i = 0; i <= h.level; ++i) preds.add(null);
        Index<K, V> q = h;
        for (int i = h.level; i > 0; --i) {
            preds.set(i, q);
            q = q.down;
        }
        for (; ; ) {
            Object k = s.readObject();
            if (k == null)
                break;
            Object v = s.readObject();
            if (v == null)
                throw new NullPointerException();
            K key = (K) k;
            V val = (V) v;
            int j = randomLevel();
            if (j > h.level)
                j = h.level + 1;
            Node<K, V> z = new Node<K, V>(key, val, null);
            basepred.next = z;
            basepred = z;
            if (j > 0) {
                Index<K, V> idx = null;
                for (int i = 1; i <= j; ++i) {
                    idx = new Index<K, V>(z, idx, null);
                    if (i > h.level)
                        h = new HeadIndex<K, V>(h.node, h, idx, i);
                    if (i < preds.size()) {
                        preds.get(i).right = idx;
                        preds.set(i, idx);
                    } else
                        preds.add(idx);
                }
            }
        }
        head = h;
    }

    public boolean containsKey(Object key) {
        return doGet(key) != null;
    }

    public V get(Object key) {
        return doGet(key);
    }

    public V put(K key, V value) {
        if (value == null)
            throw new NullPointerException();
        return doPut(key, value, false);
    }

    public V remove(Object key) {
        return doRemove(key, null);
    }

    public boolean containsValue(Object value) {
        if (value == null)
            throw new NullPointerException();
        for (Node<K, V> n = findFirst(); n != null; n = n.next) {
            V v = n.getValidValue();
            if (v != null && value.equals(v))
                return true;
        }
        return false;
    }

    public int size() {
        long count = 0;
        for (Node<K, V> n = findFirst(); n != null; n = n.next) {
            if (n.getValidValue() != null)
                ++count;
        }
        return (count >= Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) count;
    }

    public boolean isEmpty() {
        return findFirst() == null;
    }

    public void clear() {
        initialize();
    }

    public NavigableSet<K> keySet() {
        KeySet<K> ks = keySet;
        return (ks != null) ? ks : (keySet = new KeySet<K>(this));
    }

    public NavigableSet<K> navigableKeySet() {
        KeySet<K> ks = keySet;
        return (ks != null) ? ks : (keySet = new KeySet<K>(this));
    }

    public Collection<V> values() {
        Values<V> vs = values;
        return (vs != null) ? vs : (values = new Values<V>(this));
    }

    public Set<Map.Entry<K, V>> entrySet() {
        EntrySet<K, V> es = entrySet;
        return (es != null) ? es : (entrySet = new EntrySet<K, V>(this));
    }

    public ConcurrentNavigableMap<K, V> descendingMap() {
        ConcurrentNavigableMap<K, V> dm = descendingMap;
        return (dm != null) ? dm : (descendingMap = new SubMap<K, V>(this, null, false, null, false, true));
    }

    public NavigableSet<K> descendingKeySet() {
        return descendingMap().navigableKeySet();
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Map))
            return false;
        Map<?, ?> m = (Map<?, ?>) o;
        try {
            for (Map.Entry<K, V> e : this.entrySet()) if (!e.getValue().equals(m.get(e.getKey())))
                return false;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                Object k = e.getKey();
                Object v = e.getValue();
                if (k == null || v == null || !v.equals(get(k)))
                    return false;
            }
            return true;
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
    }

    public V putIfAbsent(K key, V value) {
        if (value == null)
            throw new NullPointerException();
        return doPut(key, value, true);
    }

    public boolean remove(Object key, Object value) {
        if (key == null)
            throw new NullPointerException();
        if (value == null)
            return false;
        return doRemove(key, value) != null;
    }

    public boolean replace(K key, V oldValue, V newValue) {
        if (oldValue == null || newValue == null)
            throw new NullPointerException();
        Comparable<? super K> k = comparable(key);
        for (; ; ) {
            Node<K, V> n = findNode(k);
            if (n == null)
                return false;
            Object v = n.value;
            if (v != null) {
                if (!oldValue.equals(v))
                    return false;
                if (n.casValue(v, newValue))
                    return true;
            }
        }
    }

    public V replace(K key, V value) {
        if (value == null)
            throw new NullPointerException();
        Comparable<? super K> k = comparable(key);
        for (; ; ) {
            Node<K, V> n = findNode(k);
            if (n == null)
                return null;
            Object v = n.value;
            if (v != null && n.casValue(v, value))
                return (V) v;
        }
    }

    public Comparator<? super K> comparator() {
        return comparator;
    }

    public K firstKey() {
        Node<K, V> n = findFirst();
        if (n == null)
            throw new NoSuchElementException();
        return n.key;
    }

    public K lastKey() {
        Node<K, V> n = findLast();
        if (n == null)
            throw new NoSuchElementException();
        return n.key;
    }

    public ConcurrentNavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        if (fromKey == null || toKey == null)
            throw new NullPointerException();
        return new SubMap<K, V>(this, fromKey, fromInclusive, toKey, toInclusive, false);
    }

    public ConcurrentNavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        if (toKey == null)
            throw new NullPointerException();
        return new SubMap<K, V>(this, null, false, toKey, inclusive, false);
    }

    public ConcurrentNavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        if (fromKey == null)
            throw new NullPointerException();
        return new SubMap<K, V>(this, fromKey, inclusive, null, false, false);
    }

    public ConcurrentNavigableMap<K, V> subMap(K fromKey, K toKey) {
        return subMap(fromKey, true, toKey, false);
    }

    public ConcurrentNavigableMap<K, V> headMap(K toKey) {
        return headMap(toKey, false);
    }

    public ConcurrentNavigableMap<K, V> tailMap(K fromKey) {
        return tailMap(fromKey, true);
    }

    public Map.Entry<K, V> lowerEntry(K key) {
        return getNear(key, LT);
    }

    public K lowerKey(K key) {
        Node<K, V> n = findNear(key, LT);
        return (n == null) ? null : n.key;
    }

    public Map.Entry<K, V> floorEntry(K key) {
        return getNear(key, LT | EQ);
    }

    public K floorKey(K key) {
        Node<K, V> n = findNear(key, LT | EQ);
        return (n == null) ? null : n.key;
    }

    public Map.Entry<K, V> ceilingEntry(K key) {
        return getNear(key, GT | EQ);
    }

    public K ceilingKey(K key) {
        Node<K, V> n = findNear(key, GT | EQ);
        return (n == null) ? null : n.key;
    }

    public Map.Entry<K, V> higherEntry(K key) {
        return getNear(key, GT);
    }

    public K higherKey(K key) {
        Node<K, V> n = findNear(key, GT);
        return (n == null) ? null : n.key;
    }

    public Map.Entry<K, V> firstEntry() {
        for (; ; ) {
            Node<K, V> n = findFirst();
            if (n == null)
                return null;
            AbstractMap.SimpleImmutableEntry<K, V> e = n.createSnapshot();
            if (e != null)
                return e;
        }
    }

    public Map.Entry<K, V> lastEntry() {
        for (; ; ) {
            Node<K, V> n = findLast();
            if (n == null)
                return null;
            AbstractMap.SimpleImmutableEntry<K, V> e = n.createSnapshot();
            if (e != null)
                return e;
        }
    }

    public Map.Entry<K, V> pollFirstEntry() {
        return doRemoveFirstEntry();
    }

    public Map.Entry<K, V> pollLastEntry() {
        return doRemoveLastEntry();
    }

    abstract class Iter<T> implements Iterator<T> {

        Node<K, V> lastReturned;

        Node<K, V> next;

        V nextValue;

        Iter() {
            for (; ; ) {
                next = findFirst();
                if (next == null)
                    break;
                Object x = next.value;
                if (x != null && x != next) {
                    nextValue = (V) x;
                    break;
                }
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final void advance() {
            if (next == null)
                throw new NoSuchElementException();
            lastReturned = next;
            for (; ; ) {
                next = next.next;
                if (next == null)
                    break;
                Object x = next.value;
                if (x != null && x != next) {
                    nextValue = (V) x;
                    break;
                }
            }
        }

        public void remove() {
            Node<K, V> l = lastReturned;
            if (l == null)
                throw new IllegalStateException();
            ConcurrentSkipListMap.this.remove(l.key);
            lastReturned = null;
        }
    }

    final class ValueIterator extends Iter<V> {

        public V next() {
            V v = nextValue;
            advance();
            return v;
        }
    }

    final class KeyIterator extends Iter<K> {

        public K next() {
            Node<K, V> n = next;
            advance();
            return n.key;
        }
    }

    final class EntryIterator extends Iter<Map.Entry<K, V>> {

        public Map.Entry<K, V> next() {
            Node<K, V> n = next;
            V v = nextValue;
            advance();
            return new AbstractMap.SimpleImmutableEntry<K, V>(n.key, v);
        }
    }

    Iterator<K> keyIterator() {
        return new KeyIterator();
    }

    Iterator<V> valueIterator() {
        return new ValueIterator();
    }

    Iterator<Map.Entry<K, V>> entryIterator() {
        return new EntryIterator();
    }

    static final <E> List<E> toList(Collection<E> c) {
        List<E> list = new ArrayList<E>();
        for (E e : c) list.add(e);
        return list;
    }

    static final class KeySet<E> extends AbstractSet<E> implements NavigableSet<E> {

        private final ConcurrentNavigableMap<E, ?> m;

        KeySet(ConcurrentNavigableMap<E, ?> map) {
            m = map;
        }

        public int size() {
            return m.size();
        }

        public boolean isEmpty() {
            return m.isEmpty();
        }

        public boolean contains(Object o) {
            return m.containsKey(o);
        }

        public boolean remove(Object o) {
            return m.remove(o) != null;
        }

        public void clear() {
            m.clear();
        }

        public E lower(E e) {
            return m.lowerKey(e);
        }

        public E floor(E e) {
            return m.floorKey(e);
        }

        public E ceiling(E e) {
            return m.ceilingKey(e);
        }

        public E higher(E e) {
            return m.higherKey(e);
        }

        public Comparator<? super E> comparator() {
            return m.comparator();
        }

        public E first() {
            return m.firstKey();
        }

        public E last() {
            return m.lastKey();
        }

        public E pollFirst() {
            Map.Entry<E, ?> e = m.pollFirstEntry();
            return (e == null) ? null : e.getKey();
        }

        public E pollLast() {
            Map.Entry<E, ?> e = m.pollLastEntry();
            return (e == null) ? null : e.getKey();
        }

        public Iterator<E> iterator() {
            if (m instanceof ConcurrentSkipListMap)
                return ((ConcurrentSkipListMap<E, Object>) m).keyIterator();
            else
                return ((ConcurrentSkipListMap.SubMap<E, Object>) m).keyIterator();
        }

        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Set))
                return false;
            Collection<?> c = (Collection<?>) o;
            try {
                return containsAll(c) && c.containsAll(this);
            } catch (ClassCastException unused) {
                return false;
            } catch (NullPointerException unused) {
                return false;
            }
        }

        public Object[] toArray() {
            return toList(this).toArray();
        }

        public <T> T[] toArray(T[] a) {
            return toList(this).toArray(a);
        }

        public Iterator<E> descendingIterator() {
            return descendingSet().iterator();
        }

        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
            return new KeySet<E>(m.subMap(fromElement, fromInclusive, toElement, toInclusive));
        }

        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            return new KeySet<E>(m.headMap(toElement, inclusive));
        }

        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            return new KeySet<E>(m.tailMap(fromElement, inclusive));
        }

        public NavigableSet<E> subSet(E fromElement, E toElement) {
            return subSet(fromElement, true, toElement, false);
        }

        public NavigableSet<E> headSet(E toElement) {
            return headSet(toElement, false);
        }

        public NavigableSet<E> tailSet(E fromElement) {
            return tailSet(fromElement, true);
        }

        public NavigableSet<E> descendingSet() {
            return new KeySet<E>(m.descendingMap());
        }
    }

    static final class Values<E> extends AbstractCollection<E> {

        private final ConcurrentNavigableMap<?, E> m;

        Values(ConcurrentNavigableMap<?, E> map) {
            m = map;
        }

        public Iterator<E> iterator() {
            if (m instanceof ConcurrentSkipListMap)
                return ((ConcurrentSkipListMap<?, E>) m).valueIterator();
            else
                return ((SubMap<?, E>) m).valueIterator();
        }

        public boolean isEmpty() {
            return m.isEmpty();
        }

        public int size() {
            return m.size();
        }

        public boolean contains(Object o) {
            return m.containsValue(o);
        }

        public void clear() {
            m.clear();
        }

        public Object[] toArray() {
            return toList(this).toArray();
        }

        public <T> T[] toArray(T[] a) {
            return toList(this).toArray(a);
        }
    }

    static final class EntrySet<K1, V1> extends AbstractSet<Map.Entry<K1, V1>> {

        private final ConcurrentNavigableMap<K1, V1> m;

        EntrySet(ConcurrentNavigableMap<K1, V1> map) {
            m = map;
        }

        public Iterator<Map.Entry<K1, V1>> iterator() {
            if (m instanceof ConcurrentSkipListMap)
                return ((ConcurrentSkipListMap<K1, V1>) m).entryIterator();
            else
                return ((SubMap<K1, V1>) m).entryIterator();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            V1 v = m.get(e.getKey());
            return v != null && v.equals(e.getValue());
        }

        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return m.remove(e.getKey(), e.getValue());
        }

        public boolean isEmpty() {
            return m.isEmpty();
        }

        public int size() {
            return m.size();
        }

        public void clear() {
            m.clear();
        }

        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Set))
                return false;
            Collection<?> c = (Collection<?>) o;
            try {
                return containsAll(c) && c.containsAll(this);
            } catch (ClassCastException unused) {
                return false;
            } catch (NullPointerException unused) {
                return false;
            }
        }

        public Object[] toArray() {
            return toList(this).toArray();
        }

        public <T> T[] toArray(T[] a) {
            return toList(this).toArray(a);
        }
    }

    static final class SubMap<K, V> extends AbstractMap<K, V> implements ConcurrentNavigableMap<K, V>, Cloneable, java.io.Serializable {

        private static final long serialVersionUID = -7647078645895051609L;

        private final ConcurrentSkipListMap<K, V> m;

        private final K lo;

        private final K hi;

        private final boolean loInclusive;

        private final boolean hiInclusive;

        private final boolean isDescending;

        private transient KeySet<K> keySetView;

        private transient Set<Map.Entry<K, V>> entrySetView;

        private transient Collection<V> valuesView;

        SubMap(ConcurrentSkipListMap<K, V> map, K fromKey, boolean fromInclusive, K toKey, boolean toInclusive, boolean isDescending) {
            if (fromKey != null && toKey != null && map.compare(fromKey, toKey) > 0)
                throw new IllegalArgumentException("inconsistent range");
            this.m = map;
            this.lo = fromKey;
            this.hi = toKey;
            this.loInclusive = fromInclusive;
            this.hiInclusive = toInclusive;
            this.isDescending = isDescending;
        }

        private boolean tooLow(K key) {
            if (lo != null) {
                int c = m.compare(key, lo);
                if (c < 0 || (c == 0 && !loInclusive))
                    return true;
            }
            return false;
        }

        private boolean tooHigh(K key) {
            if (hi != null) {
                int c = m.compare(key, hi);
                if (c > 0 || (c == 0 && !hiInclusive))
                    return true;
            }
            return false;
        }

        private boolean inBounds(K key) {
            return !tooLow(key) && !tooHigh(key);
        }

        private void checkKeyBounds(K key) throws IllegalArgumentException {
            if (key == null)
                throw new NullPointerException();
            if (!inBounds(key))
                throw new IllegalArgumentException("key out of range");
        }

        private boolean isBeforeEnd(ConcurrentSkipListMap.Node<K, V> n) {
            if (n == null)
                return false;
            if (hi == null)
                return true;
            K k = n.key;
            if (k == null)
                return true;
            int c = m.compare(k, hi);
            if (c > 0 || (c == 0 && !hiInclusive))
                return false;
            return true;
        }

        private ConcurrentSkipListMap.Node<K, V> loNode() {
            if (lo == null)
                return m.findFirst();
            else if (loInclusive)
                return m.findNear(lo, GT | EQ);
            else
                return m.findNear(lo, GT);
        }

        private ConcurrentSkipListMap.Node<K, V> hiNode() {
            if (hi == null)
                return m.findLast();
            else if (hiInclusive)
                return m.findNear(hi, LT | EQ);
            else
                return m.findNear(hi, LT);
        }

        private K lowestKey() {
            ConcurrentSkipListMap.Node<K, V> n = loNode();
            if (isBeforeEnd(n))
                return n.key;
            else
                throw new NoSuchElementException();
        }

        private K highestKey() {
            ConcurrentSkipListMap.Node<K, V> n = hiNode();
            if (n != null) {
                K last = n.key;
                if (inBounds(last))
                    return last;
            }
            throw new NoSuchElementException();
        }

        private Map.Entry<K, V> lowestEntry() {
            for (; ; ) {
                ConcurrentSkipListMap.Node<K, V> n = loNode();
                if (!isBeforeEnd(n))
                    return null;
                Map.Entry<K, V> e = n.createSnapshot();
                if (e != null)
                    return e;
            }
        }

        private Map.Entry<K, V> highestEntry() {
            for (; ; ) {
                ConcurrentSkipListMap.Node<K, V> n = hiNode();
                if (n == null || !inBounds(n.key))
                    return null;
                Map.Entry<K, V> e = n.createSnapshot();
                if (e != null)
                    return e;
            }
        }

        private Map.Entry<K, V> removeLowest() {
            for (; ; ) {
                Node<K, V> n = loNode();
                if (n == null)
                    return null;
                K k = n.key;
                if (!inBounds(k))
                    return null;
                V v = m.doRemove(k, null);
                if (v != null)
                    return new AbstractMap.SimpleImmutableEntry<K, V>(k, v);
            }
        }

        private Map.Entry<K, V> removeHighest() {
            for (; ; ) {
                Node<K, V> n = hiNode();
                if (n == null)
                    return null;
                K k = n.key;
                if (!inBounds(k))
                    return null;
                V v = m.doRemove(k, null);
                if (v != null)
                    return new AbstractMap.SimpleImmutableEntry<K, V>(k, v);
            }
        }

        private Map.Entry<K, V> getNearEntry(K key, int rel) {
            if (isDescending) {
                if ((rel & LT) == 0)
                    rel |= LT;
                else
                    rel &= ~LT;
            }
            if (tooLow(key))
                return ((rel & LT) != 0) ? null : lowestEntry();
            if (tooHigh(key))
                return ((rel & LT) != 0) ? highestEntry() : null;
            for (; ; ) {
                Node<K, V> n = m.findNear(key, rel);
                if (n == null || !inBounds(n.key))
                    return null;
                K k = n.key;
                V v = n.getValidValue();
                if (v != null)
                    return new AbstractMap.SimpleImmutableEntry<K, V>(k, v);
            }
        }

        private K getNearKey(K key, int rel) {
            if (isDescending) {
                if ((rel & LT) == 0)
                    rel |= LT;
                else
                    rel &= ~LT;
            }
            if (tooLow(key)) {
                if ((rel & LT) == 0) {
                    ConcurrentSkipListMap.Node<K, V> n = loNode();
                    if (isBeforeEnd(n))
                        return n.key;
                }
                return null;
            }
            if (tooHigh(key)) {
                if ((rel & LT) != 0) {
                    ConcurrentSkipListMap.Node<K, V> n = hiNode();
                    if (n != null) {
                        K last = n.key;
                        if (inBounds(last))
                            return last;
                    }
                }
                return null;
            }
            for (; ; ) {
                Node<K, V> n = m.findNear(key, rel);
                if (n == null || !inBounds(n.key))
                    return null;
                K k = n.key;
                V v = n.getValidValue();
                if (v != null)
                    return k;
            }
        }

        public boolean containsKey(Object key) {
            if (key == null)
                throw new NullPointerException();
            K k = (K) key;
            return inBounds(k) && m.containsKey(k);
        }

        public V get(Object key) {
            if (key == null)
                throw new NullPointerException();
            K k = (K) key;
            return (!inBounds(k)) ? null : m.get(k);
        }

        public V put(K key, V value) {
            checkKeyBounds(key);
            return m.put(key, value);
        }

        public V remove(Object key) {
            K k = (K) key;
            return (!inBounds(k)) ? null : m.remove(k);
        }

        public int size() {
            long count = 0;
            for (ConcurrentSkipListMap.Node<K, V> n = loNode(); isBeforeEnd(n); n = n.next) {
                if (n.getValidValue() != null)
                    ++count;
            }
            return count >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) count;
        }

        public boolean isEmpty() {
            return !isBeforeEnd(loNode());
        }

        public boolean containsValue(Object value) {
            if (value == null)
                throw new NullPointerException();
            for (ConcurrentSkipListMap.Node<K, V> n = loNode(); isBeforeEnd(n); n = n.next) {
                V v = n.getValidValue();
                if (v != null && value.equals(v))
                    return true;
            }
            return false;
        }

        public void clear() {
            for (ConcurrentSkipListMap.Node<K, V> n = loNode(); isBeforeEnd(n); n = n.next) {
                if (n.getValidValue() != null)
                    m.remove(n.key);
            }
        }

        public V putIfAbsent(K key, V value) {
            checkKeyBounds(key);
            return m.putIfAbsent(key, value);
        }

        public boolean remove(Object key, Object value) {
            K k = (K) key;
            return inBounds(k) && m.remove(k, value);
        }

        public boolean replace(K key, V oldValue, V newValue) {
            checkKeyBounds(key);
            return m.replace(key, oldValue, newValue);
        }

        public V replace(K key, V value) {
            checkKeyBounds(key);
            return m.replace(key, value);
        }

        public Comparator<? super K> comparator() {
            Comparator<? super K> cmp = m.comparator();
            if (isDescending)
                return Collections.reverseOrder(cmp);
            else
                return cmp;
        }

        private SubMap<K, V> newSubMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            if (isDescending) {
                K tk = fromKey;
                fromKey = toKey;
                toKey = tk;
                boolean ti = fromInclusive;
                fromInclusive = toInclusive;
                toInclusive = ti;
            }
            if (lo != null) {
                if (fromKey == null) {
                    fromKey = lo;
                    fromInclusive = loInclusive;
                } else {
                    int c = m.compare(fromKey, lo);
                    if (c < 0 || (c == 0 && !loInclusive && fromInclusive))
                        throw new IllegalArgumentException("key out of range");
                }
            }
            if (hi != null) {
                if (toKey == null) {
                    toKey = hi;
                    toInclusive = hiInclusive;
                } else {
                    int c = m.compare(toKey, hi);
                    if (c > 0 || (c == 0 && !hiInclusive && toInclusive))
                        throw new IllegalArgumentException("key out of range");
                }
            }
            return new SubMap<K, V>(m, fromKey, fromInclusive, toKey, toInclusive, isDescending);
        }

        public SubMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            if (fromKey == null || toKey == null)
                throw new NullPointerException();
            return newSubMap(fromKey, fromInclusive, toKey, toInclusive);
        }

        public SubMap<K, V> headMap(K toKey, boolean inclusive) {
            if (toKey == null)
                throw new NullPointerException();
            return newSubMap(null, false, toKey, inclusive);
        }

        public SubMap<K, V> tailMap(K fromKey, boolean inclusive) {
            if (fromKey == null)
                throw new NullPointerException();
            return newSubMap(fromKey, inclusive, null, false);
        }

        public SubMap<K, V> subMap(K fromKey, K toKey) {
            return subMap(fromKey, true, toKey, false);
        }

        public SubMap<K, V> headMap(K toKey) {
            return headMap(toKey, false);
        }

        public SubMap<K, V> tailMap(K fromKey) {
            return tailMap(fromKey, true);
        }

        public SubMap<K, V> descendingMap() {
            return new SubMap<K, V>(m, lo, loInclusive, hi, hiInclusive, !isDescending);
        }

        public Map.Entry<K, V> ceilingEntry(K key) {
            return getNearEntry(key, GT | EQ);
        }

        public K ceilingKey(K key) {
            return getNearKey(key, GT | EQ);
        }

        public Map.Entry<K, V> lowerEntry(K key) {
            return getNearEntry(key, LT);
        }

        public K lowerKey(K key) {
            return getNearKey(key, LT);
        }

        public Map.Entry<K, V> floorEntry(K key) {
            return getNearEntry(key, LT | EQ);
        }

        public K floorKey(K key) {
            return getNearKey(key, LT | EQ);
        }

        public Map.Entry<K, V> higherEntry(K key) {
            return getNearEntry(key, GT);
        }

        public K higherKey(K key) {
            return getNearKey(key, GT);
        }

        public K firstKey() {
            return isDescending ? highestKey() : lowestKey();
        }

        public K lastKey() {
            return isDescending ? lowestKey() : highestKey();
        }

        public Map.Entry<K, V> firstEntry() {
            return isDescending ? highestEntry() : lowestEntry();
        }

        public Map.Entry<K, V> lastEntry() {
            return isDescending ? lowestEntry() : highestEntry();
        }

        public Map.Entry<K, V> pollFirstEntry() {
            return isDescending ? removeHighest() : removeLowest();
        }

        public Map.Entry<K, V> pollLastEntry() {
            return isDescending ? removeLowest() : removeHighest();
        }

        public NavigableSet<K> keySet() {
            KeySet<K> ks = keySetView;
            return (ks != null) ? ks : (keySetView = new KeySet<K>(this));
        }

        public NavigableSet<K> navigableKeySet() {
            KeySet<K> ks = keySetView;
            return (ks != null) ? ks : (keySetView = new KeySet<K>(this));
        }

        public Collection<V> values() {
            Collection<V> vs = valuesView;
            return (vs != null) ? vs : (valuesView = new Values<V>(this));
        }

        public Set<Map.Entry<K, V>> entrySet() {
            Set<Map.Entry<K, V>> es = entrySetView;
            return (es != null) ? es : (entrySetView = new EntrySet<K, V>(this));
        }

        public NavigableSet<K> descendingKeySet() {
            return descendingMap().navigableKeySet();
        }

        Iterator<K> keyIterator() {
            return new SubMapKeyIterator();
        }

        Iterator<V> valueIterator() {
            return new SubMapValueIterator();
        }

        Iterator<Map.Entry<K, V>> entryIterator() {
            return new SubMapEntryIterator();
        }

        abstract class SubMapIter<T> implements Iterator<T> {

            Node<K, V> lastReturned;

            Node<K, V> next;

            V nextValue;

            SubMapIter() {
                for (; ; ) {
                    next = isDescending ? hiNode() : loNode();
                    if (next == null)
                        break;
                    Object x = next.value;
                    if (x != null && x != next) {
                        if (!inBounds(next.key))
                            next = null;
                        else
                            nextValue = (V) x;
                        break;
                    }
                }
            }

            public final boolean hasNext() {
                return next != null;
            }

            final void advance() {
                if (next == null)
                    throw new NoSuchElementException();
                lastReturned = next;
                if (isDescending)
                    descend();
                else
                    ascend();
            }

            private void ascend() {
                for (; ; ) {
                    next = next.next;
                    if (next == null)
                        break;
                    Object x = next.value;
                    if (x != null && x != next) {
                        if (tooHigh(next.key))
                            next = null;
                        else
                            nextValue = (V) x;
                        break;
                    }
                }
            }

            private void descend() {
                for (; ; ) {
                    next = m.findNear(lastReturned.key, LT);
                    if (next == null)
                        break;
                    Object x = next.value;
                    if (x != null && x != next) {
                        if (tooLow(next.key))
                            next = null;
                        else
                            nextValue = (V) x;
                        break;
                    }
                }
            }

            public void remove() {
                Node<K, V> l = lastReturned;
                if (l == null)
                    throw new IllegalStateException();
                m.remove(l.key);
                lastReturned = null;
            }
        }

        final class SubMapValueIterator extends SubMapIter<V> {

            public V next() {
                V v = nextValue;
                advance();
                return v;
            }
        }

        final class SubMapKeyIterator extends SubMapIter<K> {

            public K next() {
                Node<K, V> n = next;
                advance();
                return n.key;
            }
        }

        final class SubMapEntryIterator extends SubMapIter<Map.Entry<K, V>> {

            public Map.Entry<K, V> next() {
                Node<K, V> n = next;
                V v = nextValue;
                advance();
                return new AbstractMap.SimpleImmutableEntry<K, V>(n.key, v);
            }
        }
    }

    private static final sun.misc.Unsafe UNSAFE;

    private static final long headOffset;

    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ConcurrentSkipListMap.class;
            headOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("head"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
