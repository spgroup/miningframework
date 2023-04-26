package org.apache.accumulo.tserver;

import java.io.File;
import java.lang.ref.Cleaner.Cleanable;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.accumulo.core.client.SampleNotPresentException;
import org.apache.accumulo.core.conf.Property;
import org.apache.accumulo.core.data.ByteSequence;
import org.apache.accumulo.core.data.ColumnUpdate;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.IterationInterruptedException;
import org.apache.accumulo.core.iterators.IteratorEnvironment;
import org.apache.accumulo.core.iterators.SortedKeyValueIterator;
import org.apache.accumulo.core.iteratorsImpl.system.InterruptibleIterator;
import org.apache.accumulo.core.util.PreAllocatedArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class NativeMap implements Iterable<Map.Entry<Key, Value>> {

    private static final Logger log = LoggerFactory.getLogger(NativeMap.class);

    private static AtomicBoolean loadedNativeLibraries = new AtomicBoolean(false);

    static {
        List<File> directories = new ArrayList<>();
        String accumuloNativeLibDirs = System.getProperty("accumulo.native.lib.path");
        if (accumuloNativeLibDirs != null) {
            for (String libDir : accumuloNativeLibDirs.split(":")) {
                directories.add(new File(libDir));
            }
        }
        loadNativeLib(directories);
        if (!isLoaded()) {
            log.error("Tried and failed to load Accumulo native library from {}", accumuloNativeLibDirs);
            String ldLibraryPath = System.getProperty("java.library.path");
            try {
                System.loadLibrary("accumulo");
                loadedNativeLibraries.set(true);
                log.info("Loaded native map shared library from {}", ldLibraryPath);
            } catch (Exception | UnsatisfiedLinkError e) {
                log.error("Tried and failed to load Accumulo native library from {}", ldLibraryPath, e);
            }
        }
        if (!isLoaded()) {
            log.error("FATAL! Accumulo native libraries were requested but could not" + " be be loaded. Either set '{}' to false in accumulo.properties or make" + " sure native libraries are created in directories set by the JVM" + " system property 'accumulo.native.lib.path' in accumulo-env.sh!", Property.TSERV_NATIVEMAP_ENABLED);
            System.exit(1);
        }
    }

    @SuppressFBWarnings(value = "PATH_TRAVERSAL_IN", justification = "search paths provided by admin")
    public static void loadNativeLib(List<File> searchPath) {
        if (!isLoaded()) {
            List<String> names = getValidLibraryNames();
            List<File> tryList = new ArrayList<>(searchPath.size() * names.size());
            for (File p : searchPath) if (p.exists() && p.isDirectory())
                for (String name : names) tryList.add(new File(p, name));
            else
                tryList.add(p);
            for (File f : tryList) if (loadNativeLib(f))
                break;
        }
    }

    public static boolean isLoaded() {
        return loadedNativeLibraries.get();
    }

    private static List<String> getValidLibraryNames() {
        ArrayList<String> names = new ArrayList<>(3);
        String libname = System.mapLibraryName("accumulo");
        names.add(libname);
        int dot = libname.lastIndexOf(".");
        String prefix = dot < 0 ? libname : libname.substring(0, dot);
        if ("Mac OS X".equals(System.getProperty("os.name")))
            for (String ext : new String[] { ".dylib", ".jnilib" }) if (!libname.endsWith(ext))
                names.add(prefix + ext);
        return names;
    }

    private static boolean loadNativeLib(File libFile) {
        log.debug("Trying to load native map library {}", libFile);
        if (libFile.exists() && libFile.isFile()) {
            try {
                System.load(libFile.getAbsolutePath());
                loadedNativeLibraries.set(true);
                log.info("Loaded native map shared library {}", libFile);
                return true;
            } catch (Exception | UnsatisfiedLinkError e) {
                log.error("Tried and failed to load native map library " + libFile, e);
            }
        } else {
            log.debug("Native map library {} not found or is not a file.", libFile);
        }
        return false;
    }

    private final AtomicLong nmPtr = new AtomicLong(0);

    private final ReadWriteLock rwLock;

    private final Lock rlock;

    private final Lock wlock;

    private int modCount = 0;

    private static native long createNM();

    private static native void singleUpdate(long nmPointer, byte[] row, byte[] cf, byte[] cq, byte[] cv, long ts, boolean del, byte[] value, int mutationCount);

    private static native long startUpdate(long nmPointer, byte[] row);

    private static native void update(long nmPointer, long updateID, byte[] cf, byte[] cq, byte[] cv, long ts, boolean del, byte[] value, int mutationCount);

    private static native int sizeNM(long nmPointer);

    private static native long memoryUsedNM(long nmPointer);

    private static native long deleteNM(long nmPointer);

    private static boolean init = false;

    private static long totalAllocations;

    private static HashSet<Long> allocatedNativeMaps;

    private static synchronized long createNativeMap() {
        if (!init) {
            allocatedNativeMaps = new HashSet<>();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (allocatedNativeMaps.size() > 0) {
                    log.info("There are {} allocated native maps", allocatedNativeMaps.size());
                }
                log.debug("{} native maps were allocated", totalAllocations);
            }));
            init = true;
        }
        long nmPtr = createNM();
        if (allocatedNativeMaps.contains(nmPtr)) {
            throw new RuntimeException(String.format("Duplicate native map pointer 0x%016x ", nmPtr));
        }
        totalAllocations++;
        allocatedNativeMaps.add(nmPtr);
        return nmPtr;
    }

    private static synchronized void deleteNativeMap(long nmPtr) {
        if (allocatedNativeMaps.contains(nmPtr)) {
            deleteNM(nmPtr);
            allocatedNativeMaps.remove(nmPtr);
        } else {
            throw new RuntimeException(String.format("Attempt to delete native map that is not allocated 0x%016x ", nmPtr));
        }
    }

    static void _deleteNativeMap(long nmPtr) {
        deleteNativeMap(nmPtr);
    }

    private static native long createNMI(long nmp, int[] fieldLens);

    private static native long createNMI(long nmp, byte[] row, byte[] cf, byte[] cq, byte[] cv, long ts, boolean del, int[] fieldLens);

    private static native boolean nmiNext(long nmiPointer, int[] fieldLens);

    private static native void nmiGetData(long nmiPointer, byte[] row, byte[] cf, byte[] cq, byte[] cv, byte[] valData);

    private static native long nmiGetTS(long nmiPointer);

    private static native void deleteNMI(long nmiPointer);

    static void _deleteNMI(long nmiPointer) {
        deleteNMI(nmiPointer);
    }

    private class ConcurrentIterator implements Iterator<Map.Entry<Key, Value>> {

        private static final int MAX_READ_AHEAD_ENTRIES = 16;

        private static final int READ_AHEAD_BYTES = 4096;

        private NMIterator source;

        private PreAllocatedArray<Entry<Key, Value>> nextEntries;

        private int index;

        private int end;

        ConcurrentIterator() {
            this(new MemKey());
        }

        ConcurrentIterator(Key key) {
            nextEntries = new PreAllocatedArray<>(1);
            rlock.lock();
            try {
                source = new NMIterator(key);
                fill();
            } finally {
                rlock.unlock();
            }
        }

        private void fill() {
            end = 0;
            index = 0;
            if (source.hasNext())
                source.doNextPreCheck();
            int amountRead = 0;
            if (nextEntries.length < MAX_READ_AHEAD_ENTRIES)
                nextEntries = new PreAllocatedArray<>(Math.min(nextEntries.length * 2, MAX_READ_AHEAD_ENTRIES));
            while (source.hasNext() && end < nextEntries.length) {
                Entry<Key, Value> ne = source.next();
                nextEntries.set(end++, ne);
                amountRead += ne.getKey().getSize() + ne.getValue().getSize();
                if (amountRead > READ_AHEAD_BYTES)
                    break;
            }
        }

        @Override
        public boolean hasNext() {
            return end != 0;
        }

        @Override
        public Entry<Key, Value> next() {
            if (end == 0) {
                throw new NoSuchElementException();
            }
            Entry<Key, Value> ret = nextEntries.get(index++);
            if (index == end) {
                rlock.lock();
                try {
                    fill();
                } catch (ConcurrentModificationException cme) {
                    source.delete();
                    source = new NMIterator(ret.getKey());
                    fill();
                    if (end > 0 && nextEntries.get(0).getKey().equals(ret.getKey())) {
                        index++;
                        if (index == end) {
                            fill();
                        }
                    }
                } finally {
                    rlock.unlock();
                }
            }
            return ret;
        }

        public void delete() {
            source.delete();
        }
    }

    private class NMIterator implements Iterator<Map.Entry<Key, Value>> {

        private final AtomicLong nmiPtr = new AtomicLong(0);

        private boolean hasNext;

        private int expectedModCount;

        private int[] fieldsLens = new int[7];

        private byte[] lastRow;

        private final Cleanable cleanableNMI;

        NMIterator(Key key) {
            final long nmPointer = nmPtr.get();
            checkDeletedNM(nmPointer);
            expectedModCount = modCount;
            final long nmiPointer = createNMI(nmPointer, key.getRowData().toArray(), key.getColumnFamilyData().toArray(), key.getColumnQualifierData().toArray(), key.getColumnVisibilityData().toArray(), key.getTimestamp(), key.isDeleted(), fieldsLens);
            hasNext = nmiPointer != 0;
            nmiPtr.set(nmiPointer);
            cleanableNMI = NativeMapCleanerUtil.deleteNMIterator(this, nmiPtr);
        }

        public synchronized void delete() {
            final long nmiPointer = nmiPtr.getAndSet(0);
            if (nmiPointer != 0) {
                cleanableNMI.clean();
                deleteNMI(nmiPointer);
            }
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        private void doNextPreCheck() {
            checkDeletedNM(nmPtr.get());
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public synchronized Entry<Key, Value> next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            final long nmiPointer = nmiPtr.get();
            if (nmiPointer == 0) {
                throw new IllegalStateException("Native Map Iterator Deleted");
            }
            byte[] row = null;
            if (fieldsLens[0] >= 0) {
                row = new byte[fieldsLens[0]];
                lastRow = row;
            }
            byte[] cf = new byte[fieldsLens[1]];
            byte[] cq = new byte[fieldsLens[2]];
            byte[] cv = new byte[fieldsLens[3]];
            boolean deleted = fieldsLens[4] != 0;
            byte[] val = new byte[fieldsLens[5]];
            nmiGetData(nmiPointer, row, cf, cq, cv, val);
            long ts = nmiGetTS(nmiPointer);
            Key k = new MemKey(lastRow, cf, cq, cv, ts, deleted, false, fieldsLens[6]);
            Value v = new Value(val, false);
            hasNext = nmiNext(nmiPointer, fieldsLens);
            return new SimpleImmutableEntry<>(k, v);
        }
    }

    private final Cleanable cleanableNM;

    public NativeMap() {
        final long nmPointer = createNativeMap();
        nmPtr.set(nmPointer);
        cleanableNM = NativeMapCleanerUtil.deleteNM(this, log, nmPtr);
        rwLock = new ReentrantReadWriteLock();
        rlock = rwLock.readLock();
        wlock = rwLock.writeLock();
        log.debug(String.format("Allocated native map 0x%016x", nmPointer));
    }

    private static void checkDeletedNM(final long nmPointer) {
        if (nmPointer == 0) {
            throw new IllegalStateException("Native Map Deleted");
        }
    }

    private int _mutate(final long nmPointer, Mutation mutation, int mutationCount) {
        List<ColumnUpdate> updates = mutation.getUpdates();
        if (updates.size() == 1) {
            ColumnUpdate update = updates.get(0);
            singleUpdate(nmPointer, mutation.getRow(), update.getColumnFamily(), update.getColumnQualifier(), update.getColumnVisibility(), update.getTimestamp(), update.isDeleted(), update.getValue(), mutationCount++);
        } else if (updates.size() > 1) {
            long uid = startUpdate(nmPointer, mutation.getRow());
            for (ColumnUpdate update : updates) {
                update(nmPointer, uid, update.getColumnFamily(), update.getColumnQualifier(), update.getColumnVisibility(), update.getTimestamp(), update.isDeleted(), update.getValue(), mutationCount++);
            }
        }
        return mutationCount;
    }

    void mutate(List<Mutation> mutations, int mutationCount) {
        Iterator<Mutation> iter = mutations.iterator();
        while (iter.hasNext()) {
            wlock.lock();
            try {
                final long nmPointer = nmPtr.get();
                checkDeletedNM(nmPointer);
                modCount++;
                int count = 0;
                while (iter.hasNext() && count < 10) {
                    Mutation mutation = iter.next();
                    mutationCount = _mutate(nmPointer, mutation, mutationCount);
                    count += mutation.size();
                }
            } finally {
                wlock.unlock();
            }
        }
    }

    @VisibleForTesting
    public void put(Key key, Value value) {
        wlock.lock();
        try {
            final long nmPointer = nmPtr.get();
            checkDeletedNM(nmPointer);
            modCount++;
            singleUpdate(nmPointer, key.getRowData().toArray(), key.getColumnFamilyData().toArray(), key.getColumnQualifierData().toArray(), key.getColumnVisibilityData().toArray(), key.getTimestamp(), key.isDeleted(), value.get(), 0);
        } finally {
            wlock.unlock();
        }
    }

    public Value get(Key key) {
        rlock.lock();
        try {
            Value ret = null;
            NMIterator nmi = new NMIterator(key);
            if (nmi.hasNext()) {
                Entry<Key, Value> entry = nmi.next();
                if (entry.getKey().equals(key)) {
                    ret = entry.getValue();
                }
            }
            nmi.delete();
            return ret;
        } finally {
            rlock.unlock();
        }
    }

    public int size() {
        rlock.lock();
        try {
            final long nmPointer = nmPtr.get();
            checkDeletedNM(nmPointer);
            return sizeNM(nmPointer);
        } finally {
            rlock.unlock();
        }
    }

    public long getMemoryUsed() {
        rlock.lock();
        try {
            final long nmPointer = nmPtr.get();
            checkDeletedNM(nmPointer);
            return memoryUsedNM(nmPointer);
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public Iterator<Map.Entry<Key, Value>> iterator() {
        rlock.lock();
        try {
            final long nmPointer = nmPtr.get();
            checkDeletedNM(nmPointer);
            return new ConcurrentIterator();
        } finally {
            rlock.unlock();
        }
    }

    public Iterator<Map.Entry<Key, Value>> iterator(Key startKey) {
        rlock.lock();
        try {
            final long nmPointer = nmPtr.get();
            checkDeletedNM(nmPointer);
            return new ConcurrentIterator(startKey);
        } finally {
            rlock.unlock();
        }
    }

    public void delete() {
        wlock.lock();
        try {
            final long nmPointer = nmPtr.getAndSet(0);
            checkDeletedNM(nmPointer);
            cleanableNM.clean();
            log.debug(String.format("Deallocating native map 0x%016x", nmPointer));
            deleteNativeMap(nmPointer);
        } finally {
            wlock.unlock();
        }
    }

    private static class NMSKVIter implements InterruptibleIterator {

        private ConcurrentIterator iter;

        private Entry<Key, Value> entry;

        private NativeMap map;

        private Range range;

        private AtomicBoolean interruptFlag;

        private int interruptCheckCount = 0;

        private NMSKVIter(NativeMap map, AtomicBoolean interruptFlag) {
            this.map = map;
            this.range = new Range();
            iter = map.new ConcurrentIterator();
            if (iter.hasNext())
                entry = iter.next();
            else
                entry = null;
            this.interruptFlag = interruptFlag;
        }

        public NMSKVIter(NativeMap map) {
            this(map, null);
        }

        @Override
        public Key getTopKey() {
            return entry.getKey();
        }

        @Override
        public Value getTopValue() {
            return entry.getValue();
        }

        @Override
        public boolean hasTop() {
            return entry != null;
        }

        @Override
        public void next() {
            if (entry == null)
                throw new NoSuchElementException();
            if (interruptFlag != null && interruptCheckCount++ % 100 == 0 && interruptFlag.get())
                throw new IterationInterruptedException();
            if (iter.hasNext()) {
                entry = iter.next();
                if (range.afterEndKey(entry.getKey())) {
                    entry = null;
                }
            } else
                entry = null;
        }

        @Override
        public void seek(Range range, Collection<ByteSequence> columnFamilies, boolean inclusive) {
            if (interruptFlag != null && interruptFlag.get())
                throw new IterationInterruptedException();
            iter.delete();
            this.range = range;
            Key key = range.getStartKey();
            if (key == null) {
                key = new MemKey();
            }
            iter = map.new ConcurrentIterator(key);
            if (iter.hasNext()) {
                entry = iter.next();
                if (range.afterEndKey(entry.getKey())) {
                    entry = null;
                }
            } else
                entry = null;
            while (hasTop() && range.beforeStartKey(getTopKey())) {
                next();
            }
        }

        @Override
        public void init(SortedKeyValueIterator<Key, Value> source, Map<String, String> options, IteratorEnvironment env) {
            throw new UnsupportedOperationException("init");
        }

        @Override
        public SortedKeyValueIterator<Key, Value> deepCopy(IteratorEnvironment env) {
            if (env != null && env.isSamplingEnabled()) {
                throw new SampleNotPresentException();
            }
            return new NMSKVIter(map, interruptFlag);
        }

        @Override
        public void setInterruptFlag(AtomicBoolean flag) {
            this.interruptFlag = flag;
        }
    }

    public SortedKeyValueIterator<Key, Value> skvIterator() {
        return new NMSKVIter(this);
    }
}
