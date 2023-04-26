package org.apache.accumulo.test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.accumulo.core.data.ByteSequence;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.PartialKey;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.IteratorEnvironment;
import org.apache.accumulo.core.iterators.IteratorUtil;
import org.apache.accumulo.core.iterators.SortedKeyValueIterator;
import org.apache.accumulo.core.util.PeekingIterator;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HardListIterator implements SortedKeyValueIterator<Key, Value> {

    private static final Logger log = LoggerFactory.getLogger(HardListIterator.class);

    public static final SortedMap<Key, Value> allEntriesToInject;

    static {
        SortedMap<Key, Value> t = new TreeMap<>();
        t.put(new Key(new Text("a1"), new Text("colF3"), new Text("colQ3"), System.currentTimeMillis()), new Value("1"));
        t.put(new Key(new Text("c1"), new Text("colF3"), new Text("colQ3"), System.currentTimeMillis()), new Value("1"));
        t.put(new Key(new Text("m1"), new Text("colF3"), new Text("colQ3"), System.currentTimeMillis()), new Value("1"));
        allEntriesToInject = Collections.unmodifiableSortedMap(t);
    }

    private PeekingIterator<Map.Entry<Key, Value>> inner;

    private Range seekRng;

    @Override
    public void init(SortedKeyValueIterator<Key, Value> source, Map<String, String> options, IteratorEnvironment env) {
        if (source != null) {
            log.info("HardListIterator ignores/replaces parent source passed in init(): {}", source);
        }
        IteratorUtil.IteratorScope scope = env.getIteratorScope();
        log.debug("{}: init on scope {}{}", this.getClass(), scope, scope == IteratorUtil.IteratorScope.majc ? " fullScan=" + env.isFullMajorCompaction() : "");
        inner = new PeekingIterator<>(allEntriesToInject.entrySet().iterator());
    }

    @Override
    public SortedKeyValueIterator<Key, Value> deepCopy(IteratorEnvironment env) {
        HardListIterator newInstance;
        try {
            newInstance = HardListIterator.class.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        newInstance.inner = new PeekingIterator<>(allEntriesToInject.tailMap(inner.peek().getKey()).entrySet().iterator());
        return newInstance;
    }

    @Override
    public boolean hasTop() {
        if (!inner.hasNext()) {
            return false;
        }
        Key k = inner.peek().getKey();
        return seekRng.contains(k);
    }

    @Override
    public void next() {
        inner.next();
    }

    @Override
    public void seek(Range range, Collection<ByteSequence> columnFamilies, boolean inclusive) {
        seekRng = range;
        if (range.isInfiniteStartKey()) {
            inner = new PeekingIterator<>(allEntriesToInject.entrySet().iterator());
        } else if (range.isStartKeyInclusive()) {
            inner = new PeekingIterator<>(allEntriesToInject.tailMap(range.getStartKey()).entrySet().iterator());
        } else {
            inner = new PeekingIterator<>(allEntriesToInject.tailMap(range.getStartKey().followingKey(PartialKey.ROW_COLFAM_COLQUAL_COLVIS_TIME)).entrySet().iterator());
        }
    }

    @Override
    public Key getTopKey() {
        return hasTop() ? inner.peek().getKey() : null;
    }

    @Override
    public Value getTopValue() {
        return hasTop() ? inner.peek().getValue() : null;
    }
}