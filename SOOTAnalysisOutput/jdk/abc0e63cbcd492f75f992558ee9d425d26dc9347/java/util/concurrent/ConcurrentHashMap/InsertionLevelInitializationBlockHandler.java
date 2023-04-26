package java.util.concurrent;

import java.util.concurrent.locks.*;
import java.util.*;
import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ConcurrentHashMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V>, Serializable {

    private static final long serialVersionUID = 7249069246763182397L;

    static final int DEFAULT_INITIAL_CAPACITY = 16;

    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    static final int DEFAULT_CONCURRENCY_LEVEL = 16;

    static final int MAXIMUM_CAPACITY = 1 << 30;

    static final int MIN_SEGMENT_TABLE_CAPACITY = 2;

    static final int MAX_SEGMENTS = 1 << 16;

    static final int RETRIES_BEFORE_LOCK = 2;

    final int segmentMask;

    final int segmentShift;

    final Segment<K, V>[] segments;

    transient Set<K> keySet;

    transient Set<Map.Entry<K, V>> entrySet;

    transient Collection<V> values;

    static final class HashEntry<K, V> {

        final int hash;

        final K key;

        volatile V value;

        volatile HashEntry<K, V> next;

        HashEntry(int hash, K key, V value, HashEntry<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        final void setNext(HashEntry<K, V> n) {
            UNSAFE.putOrderedObject(this, nextOffset, n);
        }

        static final sun.misc.Unsafe UNSAFE;

        static final long nextOffset;

        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class k = HashEntry.class;
                nextOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("next"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    static final <K, V> HashEntry<K, V> entryAt(HashEntry<K, V>[] tab, int i) {
        return (tab == null) ? null : (HashEntry<K, V>) UNSAFE.getObjectVolatile(tab, ((long) i << TSHIFT) + TBASE);
    }

    static final <K, V> void setEntryAt(HashEntry<K, V>[] tab, int i, HashEntry<K, V> e) {
        UNSAFE.putOrderedObject(tab, ((long) i << TSHIFT) + TBASE, e);
    }

    private static int hash(int h) {
        h += (h << 15) ^ 0xffffcd7d;
        h ^= (h >>> 10);
        h += (h << 3);
        h ^= (h >>> 6);
        h += (h << 2) + (h << 14);
        return h ^ (h >>> 16);
    }

    static final class Segment<K, V> extends ReentrantLock implements Serializable {

        private static final long serialVersionUID = 2249069246763182397L;

        static final int MAX_SCAN_RETRIES = Runtime.getRuntime().availableProcessors() > 1 ? 64 : 1;

        transient volatile HashEntry<K, V>[] table;

        transient int count;

        transient int modCount;

        transient int threshold;

        final float loadFactor;

        Segment(float lf, int threshold, HashEntry<K, V>[] tab) {
            this.loadFactor = lf;
            this.threshold = threshold;
            this.table = tab;
        }

        final V put(K key, int hash, V value, boolean onlyIfAbsent) {
            HashEntry<K, V> node = tryLock() ? null : scanAndLockForPut(key, hash, value);
                V oldValue;
            try {
                HashEntry<K, V>[] tab = table;
                int index = (tab.length - 1) & hash;
                HashEntry<K, V> first = entryAt(tab, index);
                for (HashEntry<K, V> e = first; ; ) {
                if (e != null) {
                        K k;
                        if ((k = e.key) == key || (e.hash == hash && key.equals(k))) {
                    oldValue = e.value;
                            if (!onlyIfAbsent) {
                        e.value = value;
                    ++modCount;
                }
                            break;
                        }
                        e = e.next;
                    } else {
                        if (node != null)
                            node.setNext(first);
                        else
                            node = new HashEntry<K, V>(hash, key, value, first);
                        int c = count + 1;
<<<<<<< MINE
                        if (c > threshold && tab.length < MAXIMUM_CAPACITY)
=======
                        if (c > threshold && first != null && tab.length < MAXIMUM_CAPACITY)
>>>>>>> YOURS
                            rehash(node);
                        else
                            setEntryAt(tab, index, node);
                        ++modCount;
                        count = c;
                        oldValue = null;
                        break;
                    }
                }
            } finally {
                unlock();
            }
            return oldValue;
        }

        @SuppressWarnings("unchecked")
        private void rehash(HashEntry<K, V> node) {
            HashEntry<K, V>[] oldTable = table;
            int oldCapacity = oldTable.length;
            int newCapacity = oldCapacity << 1;
            threshold = (int) (newCapacity * loadFactor);
            HashEntry<K, V>[] newTable = (HashEntry<K, V>[]) new HashEntry[newCapacity];
            int sizeMask = newCapacity - 1;
            for (int i = 0; i < oldCapacity; i++) {
                HashEntry<K, V> e = oldTable[i];
                if (e != null) {
                    HashEntry<K, V> next = e.next;
                    int idx = e.hash & sizeMask;
                    if (next == null)
                        newTable[idx] = e;
                    else {
                        HashEntry<K, V> lastRun = e;
                        int lastIdx = idx;
                        for (HashEntry<K, V> last = next; last != null; last = last.next) {
                            int k = last.hash & sizeMask;
                            if (k != lastIdx) {
                                lastIdx = k;
                                lastRun = last;
                            }
                        }
                        newTable[lastIdx] = lastRun;
                        for (HashEntry<K, V> p = e; p != lastRun; p = p.next) {
                            V v = p.value;
                            int h = p.hash;
                            int k = h & sizeMask;
                            HashEntry<K, V> n = newTable[k];
                            newTable[k] = new HashEntry<K, V>(h, p.key, v, n);
                        }
                    }
                }
            }
            int nodeIndex = node.hash & sizeMask;
            node.setNext(newTable[nodeIndex]);
            newTable[nodeIndex] = node;
            table = newTable;
        }

        private HashEntry<K, V> scanAndLockForPut(K key, int hash, V value) {
            HashEntry<K, V> first = entryForHash(this, hash);
            HashEntry<K, V> e = first;
            HashEntry<K, V> node = null;
            int retries = -1;
            while (!tryLock()) {
                HashEntry<K, V> f;
                if (retries < 0) {
                    if (e == null) {
                        if (node == null)
                            node = new HashEntry<K, V>(hash, key, value, null);
                        retries = 0;
                    } else if (key.equals(e.key))
                        retries = 0;
                    else
                        e = e.next;
                } else if (++retries > MAX_SCAN_RETRIES) {
                    lock();
                    break;
                } else if ((retries & 1) == 0 && (f = entryForHash(this, hash)) != first) {
                    e = first = f;
                    retries = -1;
                }
            }
            return node;
        }

        private void scanAndLock(Object key, int hash) {
            HashEntry<K, V> first = entryForHash(this, hash);
            HashEntry<K, V> e = first;
            int retries = -1;
            while (!tryLock()) {
                HashEntry<K, V> f;
                if (retries < 0) {
                    if (e == null || key.equals(e.key))
                        retries = 0;
                    else
                        e = e.next;
                } else if (++retries > MAX_SCAN_RETRIES) {
                    lock();
                    break;
                } else if ((retries & 1) == 0 && (f = entryForHash(this, hash)) != first) {
                    e = first = f;
                    retries = -1;
                }
            }
        }

        final V remove(Object key, int hash, Object value) {
            if (!tryLock())
                scanAndLock(key, hash);
                V oldValue = null;
            try {
                HashEntry<K, V>[] tab = table;
                int index = (tab.length - 1) & hash;
                HashEntry<K, V> e = entryAt(tab, index);
                HashEntry<K, V> pred = null;
                while (e != null) {
                    K k;
                    HashEntry<K, V> next = e.next;
                    if ((k = e.key) == key || (e.hash == hash && key.equals(k))) {
                    V v = e.value;
                        if (value == null || value == v || value.equals(v)) {
                            if (pred == null)
                                setEntryAt(tab, index, next);
                            else
                                pred.setNext(next);
                        ++modCount;
                            --count;
                            oldValue = v;
                    }
                        break;
                }
                    pred = e;
                    e = next;
                }
            } finally {
                unlock();
            }
            return oldValue;
        }

        final boolean replace(K key, int hash, V oldValue, V newValue) {
            if (!tryLock())
                scanAndLock(key, hash);
                boolean replaced = false;
            try {
                HashEntry<K, V> e;
                for (e = entryForHash(this, hash); e != null; e = e.next) {
                    K k;
                    if ((k = e.key) == key || (e.hash == hash && key.equals(k))) {
                        if (oldValue.equals(e.value)) {
                    e.value = newValue;
                            ++modCount;
                            replaced = true;
                }
                        break;
                    }
                }
            } finally {
                unlock();
            }
            return replaced;
        }

        final V replace(K key, int hash, V value) {
            if (!tryLock())
                scanAndLock(key, hash);
                V oldValue = null;
            try {
                HashEntry<K, V> e;
                for (e = entryForHash(this, hash); e != null; e = e.next) {
                    K k;
                    if ((k = e.key) == key || (e.hash == hash && key.equals(k))) {
                    oldValue = e.value;
                        e.value = value;
                        ++modCount;
                        break;
                }
                }
            } finally {
                unlock();
            }
            return oldValue;
        }

        final void clear() {
                lock();
                try {
                    HashEntry<K, V>[] tab = table;
                for (int i = 0; i < tab.length; i++) setEntryAt(tab, i, null);
                    ++modCount;
                    count = 0;
                } finally {
                    unlock();
                }
            }
    }

    @SuppressWarnings("unchecked")
    static final <K, V> Segment<K, V> segmentAt(Segment<K, V>[] ss, int j) {
        long u = (j << SSHIFT) + SBASE;
        return ss == null ? null : (Segment<K, V>) UNSAFE.getObjectVolatile(ss, u);
    }

    @SuppressWarnings("unchecked")
    private Segment<K, V> ensureSegment(int k) {
        final Segment<K, V>[] ss = this.segments;
        long u = (k << SSHIFT) + SBASE;
        Segment<K, V> seg;
        if ((seg = (Segment<K, V>) UNSAFE.getObjectVolatile(ss, u)) == null) {
            Segment<K, V> proto = ss[0];
            int cap = proto.table.length;
            float lf = proto.loadFactor;
            int threshold = (int) (cap * lf);
            HashEntry<K, V>[] tab = (HashEntry<K, V>[]) new HashEntry[cap];
            if ((seg = (Segment<K, V>) UNSAFE.getObjectVolatile(ss, u)) == null) {
                Segment<K, V> s = new Segment<K, V>(lf, threshold, tab);
                while ((seg = (Segment<K, V>) UNSAFE.getObjectVolatile(ss, u)) == null) {
                    if (UNSAFE.compareAndSwapObject(ss, u, null, seg = s))
                        break;
                }
            }
        }
        return seg;
    }

    @SuppressWarnings("unchecked")
    private Segment<K, V> segmentForHash(int h) {
        long u = (((h >>> segmentShift) & segmentMask) << SSHIFT) + SBASE;
        return (Segment<K, V>) UNSAFE.getObjectVolatile(segments, u);
    }

    @SuppressWarnings("unchecked")
    static final <K, V> HashEntry<K, V> entryForHash(Segment<K, V> seg, int h) {
        HashEntry<K, V>[] tab;
        return (seg == null || (tab = seg.table) == null) ? null : (HashEntry<K, V>) UNSAFE.getObjectVolatile(tab, ((long) (((tab.length - 1) & h)) << TSHIFT) + TBASE);
    }

    @SuppressWarnings("unchecked")
public ConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        if (!(loadFactor > 0) || initialCapacity < 0 || concurrencyLevel <= 0)
            throw new IllegalArgumentException();
        if (concurrencyLevel > MAX_SEGMENTS)
            concurrencyLevel = MAX_SEGMENTS;
        int sshift = 0;
        int ssize = 1;
        while (ssize < concurrencyLevel) {
            ++sshift;
            ssize <<= 1;
        }
        this.segmentShift = 32 - sshift;
        this.segmentMask = ssize - 1;
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        int c = initialCapacity / ssize;
        if (c * ssize < initialCapacity)
            ++c;
        int cap = MIN_SEGMENT_TABLE_CAPACITY;
        while (cap < c) cap <<= 1;
        Segment<K, V> s0 = new Segment<K, V>(loadFactor, (int) (cap * loadFactor), (HashEntry<K, V>[]) new HashEntry[cap]);
        Segment<K, V>[] ss = (Segment<K, V>[]) new Segment[ssize];
        UNSAFE.putOrderedObject(ss, SBASE, s0);
        this.segments = ss;
    }

    public ConcurrentHashMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, DEFAULT_CONCURRENCY_LEVEL);
    }

    public ConcurrentHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
    }

    public ConcurrentHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
    }

    public ConcurrentHashMap(Map<? extends K, ? extends V> m) {
        this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
        putAll(m);
    }

    public boolean isEmpty() {
        long sum = 0L;
        final Segment<K, V>[] segments = this.segments;
        for (int j = 0; j < segments.length; ++j) {
            Segment<K, V> seg = segmentAt(segments, j);
            if (seg != null) {
                if (seg.count != 0)
                return false;
                sum += seg.modCount;
        }
        }
        if (sum != 0L) {
            for (int j = 0; j < segments.length; ++j) {
                Segment<K, V> seg = segmentAt(segments, j);
                if (seg != null) {
                    if (seg.count != 0)
                    return false;
                    sum -= seg.modCount;
            }
        }
            if (sum != 0L)
                return false;
        }
        return true;
    }

    public int size() {
        final Segment<K, V>[] segments = this.segments;
        int size;
        boolean overflow;
        long sum;
        long last = 0L;
        int retries = -1;
        try {
            for (; ; ) {
                if (retries++ == RETRIES_BEFORE_LOCK) {
                    for (int j = 0; j < segments.length; ++j) ensureSegment(j).lock();
            }
                sum = 0L;
                size = 0;
                overflow = false;
                for (int j = 0; j < segments.length; ++j) {
                    Segment<K, V> seg = segmentAt(segments, j);
                    if (seg != null) {
                        sum += seg.modCount;
                        int c = seg.count;
                        if (c < 0 || (size += c) < 0)
                            overflow = true;
                    }
                }
                if (sum == last)
                        break;
                last = sum;
            }
        } finally {
            if (retries > RETRIES_BEFORE_LOCK) {
                for (int j = 0; j < segments.length; ++j) segmentAt(segments, j).unlock();
                    }
                }
        return overflow ? Integer.MAX_VALUE : size;
    }

    public V get(Object key) {
<<<<<<< MINE
        Segment<K, V> s;
        HashEntry<K, V>[] tab;
        int h = hash(key.hashCode());
        long u = (((h >>> segmentShift) & segmentMask) << SSHIFT) + SBASE;
        if ((s = (Segment<K, V>) UNSAFE.getObjectVolatile(segments, u)) != null && (tab = s.table) != null) {
            for (HashEntry<K, V> e = (HashEntry<K, V>) UNSAFE.getObjectVolatile(tab, ((long) (((tab.length - 1) & h)) << TSHIFT) + TBASE); e != null; e = e.next) {
                K k;
                if ((k = e.key) == key || (e.hash == h && key.equals(k)))
                    return e.value;
            }
=======
        int hash = hash(key.hashCode());
        for (HashEntry<K, V> e = entryForHash(segmentForHash(hash), hash); e != null; e = e.next) {
            K k;
            if ((k = e.key) == key || (e.hash == hash && key.equals(k)))
                return e.value;
>>>>>>> YOURS
        }
        return null;
    }

    @SuppressWarnings("unchecked")
public boolean containsKey(Object key) {
<<<<<<< MINE
        Segment<K, V> s;
        HashEntry<K, V>[] tab;
        int h = hash(key.hashCode());
        long u = (((h >>> segmentShift) & segmentMask) << SSHIFT) + SBASE;
        if ((s = (Segment<K, V>) UNSAFE.getObjectVolatile(segments, u)) != null && (tab = s.table) != null) {
            for (HashEntry<K, V> e = (HashEntry<K, V>) UNSAFE.getObjectVolatile(tab, ((long) (((tab.length - 1) & h)) << TSHIFT) + TBASE); e != null; e = e.next) {
                K k;
                if ((k = e.key) == key || (e.hash == h && key.equals(k)))
                    return true;
            }
=======
        int hash = hash(key.hashCode());
        for (HashEntry<K, V> e = entryForHash(segmentForHash(hash), hash); e != null; e = e.next) {
            K k;
            if ((k = e.key) == key || (e.hash == hash && key.equals(k)))
                return true;
>>>>>>> YOURS
        }
        return false;
    }

    public boolean containsValue(Object value) {
        if (value == null)
            throw new NullPointerException();
        final Segment<K, V>[] segments = this.segments;
        boolean found = false;
        long last = 0;
        int retries = -1;
        try {
            outer: for (; ; ) {
                if (retries++ == RETRIES_BEFORE_LOCK) {
                    for (int j = 0; j < segments.length; ++j) ensureSegment(j).lock();
                }
                long hashSum = 0L;
                int sum = 0;
                for (int j = 0; j < segments.length; ++j) {
                    HashEntry<K, V>[] tab;
                    Segment<K, V> seg = segmentAt(segments, j);
                    if (seg != null && (tab = seg.table) != null) {
                        for (int i = 0; i < tab.length; i++) {
                            HashEntry<K, V> e;
                            for (e = entryAt(tab, i); e != null; e = e.next) {
                                V v = e.value;
                                if (v != null && value.equals(v)) {
                                    found = true;
                                    break outer;
                                }
                            }
                        }
                        sum += seg.modCount;
                    }
                }
                if (retries > 0 && sum == last)
                    break;
                last = sum;
            }
        } finally {
            if (retries > RETRIES_BEFORE_LOCK) {
                for (int j = 0; j < segments.length; ++j) segmentAt(segments, j).unlock();
            }
        }
        return found;
    }

    public boolean contains(Object value) {
        return containsValue(value);
    }

    @SuppressWarnings("unchecked")
public V put(K key, V value) {
        Segment<K, V> s;
        if (value == null)
            throw new NullPointerException();
        int hash = hash(key.hashCode());
        int j = (hash >>> segmentShift) & segmentMask;
<<<<<<< MINE
        if ((s = (Segment<K, V>) UNSAFE.getObject(segments, (j << SSHIFT) + SBASE)) == null)
=======
        Segment<K, V> s = segmentAt(segments, j);
        if (s == null)
>>>>>>> YOURS
            s = ensureSegment(j);
        return s.put(key, hash, value, false);
    }

    @SuppressWarnings("unchecked")
public V putIfAbsent(K key, V value) {
        Segment<K, V> s;
        if (value == null)
            throw new NullPointerException();
        int hash = hash(key.hashCode());
        int j = (hash >>> segmentShift) & segmentMask;
<<<<<<< MINE
        if ((s = (Segment<K, V>) UNSAFE.getObject(segments, (j << SSHIFT) + SBASE)) == null)
=======
        Segment<K, V> s = segmentAt(segments, j);
        if (s == null)
>>>>>>> YOURS
            s = ensureSegment(j);
        return s.put(key, hash, value, true);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) put(e.getKey(), e.getValue());
    }

    public V remove(Object key) {
        int hash = hash(key.hashCode());
        Segment<K, V> s = segmentForHash(hash);
        return s == null ? null : s.remove(key, hash, null);
    }

    public boolean remove(Object key, Object value) {
        int hash = hash(key.hashCode());
        Segment<K, V> s;
        return value != null && (s = segmentForHash(hash)) != null && s.remove(key, hash, value) != null;
    }

    public boolean replace(K key, V oldValue, V newValue) {
        int hash = hash(key.hashCode());
        if (oldValue == null || newValue == null)
            throw new NullPointerException();
        Segment<K, V> s = segmentForHash(hash);
        return s != null && s.replace(key, hash, oldValue, newValue);
    }

    public V replace(K key, V value) {
        int hash = hash(key.hashCode());
        if (value == null)
            throw new NullPointerException();
        Segment<K, V> s = segmentForHash(hash);
        return s == null ? null : s.replace(key, hash, value);
    }

    public void clear() {
        final Segment<K, V>[] segments = this.segments;
        for (int j = 0; j < segments.length; ++j) {
            Segment<K, V> s = segmentAt(segments, j);
            if (s != null)
                s.clear();
        }
    }

    public Set<K> keySet() {
        Set<K> ks = keySet;
        return (ks != null) ? ks : (keySet = new KeySet());
    }

    public Collection<V> values() {
        Collection<V> vs = values;
        return (vs != null) ? vs : (values = new Values());
    }

    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> es = entrySet;
        return (es != null) ? es : (entrySet = new EntrySet());
    }

    public Enumeration<K> keys() {
        return new KeyIterator();
    }

    public Enumeration<V> elements() {
        return new ValueIterator();
    }

    abstract class HashIterator {

        int nextSegmentIndex;

        int nextTableIndex;

        HashEntry<K, V>[] currentTable;

        HashEntry<K, V> nextEntry;

        HashEntry<K, V> lastReturned;

        HashIterator() {
            nextSegmentIndex = segments.length - 1;
            nextTableIndex = -1;
            advance();
        }

        final void advance() {
            for (; ; ) {
                if (nextTableIndex >= 0) {
                    if ((nextEntry = entryAt(currentTable, nextTableIndex--)) != null)
                        break;
                } else if (nextSegmentIndex >= 0) {
                    Segment<K, V> seg = segmentAt(segments, nextSegmentIndex--);
                    if (seg != null && (currentTable = seg.table) != null)
                        nextTableIndex = currentTable.length - 1;
                } else
                    break;
            }
        }

        final HashEntry<K, V> nextEntry() {
            HashEntry<K, V> e = nextEntry;
            if (e == null)
                throw new NoSuchElementException();
            lastReturned = e;
            if ((nextEntry = e.next) == null)
            advance();
            return e;
        }

        public final boolean hasNext() {
            return nextEntry != null;
        }

        public final boolean hasMoreElements() {
            return nextEntry != null;
        }

        public final void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();
            ConcurrentHashMap.this.remove(lastReturned.key);
            lastReturned = null;
        }
    }

    final class KeyIterator extends HashIterator implements Iterator<K>, Enumeration<K> {

        public final K next() {
            return super.nextEntry().key;
        }

        public final K nextElement() {
            return super.nextEntry().key;
        }
    }

    final class ValueIterator extends HashIterator implements Iterator<V>, Enumeration<V> {

        public final V next() {
            return super.nextEntry().value;
        }

        public final V nextElement() {
            return super.nextEntry().value;
        }
    }

    final class WriteThroughEntry extends AbstractMap.SimpleEntry<K, V> {

        WriteThroughEntry(K k, V v) {
            super(k, v);
        }

        public V setValue(V value) {
            if (value == null)
                throw new NullPointerException();
            V v = super.setValue(value);
            ConcurrentHashMap.this.put(getKey(), value);
            return v;
        }
    }

    final class EntryIterator extends HashIterator implements Iterator<Entry<K, V>> {

        public Map.Entry<K, V> next() {
            HashEntry<K, V> e = super.nextEntry();
            return new WriteThroughEntry(e.key, e.value);
        }
    }

    final class KeySet extends AbstractSet<K> {

        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        public int size() {
            return ConcurrentHashMap.this.size();
        }

        public boolean isEmpty() {
            return ConcurrentHashMap.this.isEmpty();
        }

        public boolean contains(Object o) {
            return ConcurrentHashMap.this.containsKey(o);
        }

        public boolean remove(Object o) {
            return ConcurrentHashMap.this.remove(o) != null;
        }

        public void clear() {
            ConcurrentHashMap.this.clear();
        }
    }

    final class Values extends AbstractCollection<V> {

        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        public int size() {
            return ConcurrentHashMap.this.size();
        }

        public boolean isEmpty() {
            return ConcurrentHashMap.this.isEmpty();
        }

        public boolean contains(Object o) {
            return ConcurrentHashMap.this.containsValue(o);
        }

        public void clear() {
            ConcurrentHashMap.this.clear();
        }
    }

    final class EntrySet extends AbstractSet<Map.Entry<K, V>> {

        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            V v = ConcurrentHashMap.this.get(e.getKey());
            return v != null && v.equals(e.getValue());
        }

        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return ConcurrentHashMap.this.remove(e.getKey(), e.getValue());
        }

        public int size() {
            return ConcurrentHashMap.this.size();
        }

        public boolean isEmpty() {
            return ConcurrentHashMap.this.isEmpty();
        }

        public void clear() {
            ConcurrentHashMap.this.clear();
        }
    }

    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        for (int k = 0; k < segments.length; ++k) ensureSegment(k);
        s.defaultWriteObject();
        final Segment<K, V>[] segments = this.segments;
        for (int k = 0; k < segments.length; ++k) {
            Segment<K, V> seg = segmentAt(segments, k);
            seg.lock();
            try {
                HashEntry<K, V>[] tab = seg.table;
                for (int i = 0; i < tab.length; ++i) {
                    HashEntry<K, V> e;
                    for (e = entryAt(tab, i); e != null; e = e.next) {
                        s.writeObject(e.key);
                        s.writeObject(e.value);
                    }
                }
            } finally {
                seg.unlock();
            }
        }
        s.writeObject(null);
        s.writeObject(null);
    }

    @SuppressWarnings("unchecked")
private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        int cap = MIN_SEGMENT_TABLE_CAPACITY;
        final Segment<K, V>[] segments = this.segments;
        for (int k = 0; k < segments.length; ++k) {
            Segment<K, V> seg = segments[k];
            if (seg != null) {
                seg.threshold = (int) (cap * seg.loadFactor);
                seg.table = (HashEntry<K, V>[]) new HashEntry[cap];
            }
        }
        for (; ; ) {
            K key = (K) s.readObject();
            V value = (V) s.readObject();
            if (key == null)
                break;
            put(key, value);
        }
    }

    private static final sun.misc.Unsafe UNSAFE;

    private static final long SBASE;

    private static final int SSHIFT;

    private static final long TBASE;

    private static final int TSHIFT;

    static {
        int ss, ts;
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class tc = HashEntry[].class;
            Class sc = Segment[].class;
            TBASE = UNSAFE.arrayBaseOffset(tc);
            SBASE = UNSAFE.arrayBaseOffset(sc);
            ts = UNSAFE.arrayIndexScale(tc);
            ss = UNSAFE.arrayIndexScale(sc);
        } catch (Exception e) {
            throw new Error(e);
        }
        if ((ss & (ss - 1)) != 0 || (ts & (ts - 1)) != 0)
            throw new Error("data type scale not a power of two");
        SSHIFT = 31 - Integer.numberOfLeadingZeros(ss);
        TSHIFT = 31 - Integer.numberOfLeadingZeros(ts);
    }
}