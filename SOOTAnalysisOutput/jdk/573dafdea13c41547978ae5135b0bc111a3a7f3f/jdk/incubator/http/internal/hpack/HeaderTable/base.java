package jdk.incubator.http.internal.hpack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import static java.lang.String.format;

final class HeaderTable {

    private static final HeaderField[] staticTable = { null, new HeaderField(":authority"), new HeaderField(":method", "GET"), new HeaderField(":method", "POST"), new HeaderField(":path", "/"), new HeaderField(":path", "/index.html"), new HeaderField(":scheme", "http"), new HeaderField(":scheme", "https"), new HeaderField(":status", "200"), new HeaderField(":status", "204"), new HeaderField(":status", "206"), new HeaderField(":status", "304"), new HeaderField(":status", "400"), new HeaderField(":status", "404"), new HeaderField(":status", "500"), new HeaderField("accept-charset"), new HeaderField("accept-encoding", "gzip, deflate"), new HeaderField("accept-language"), new HeaderField("accept-ranges"), new HeaderField("accept"), new HeaderField("access-control-allow-origin"), new HeaderField("age"), new HeaderField("allow"), new HeaderField("authorization"), new HeaderField("cache-control"), new HeaderField("content-disposition"), new HeaderField("content-encoding"), new HeaderField("content-language"), new HeaderField("content-length"), new HeaderField("content-location"), new HeaderField("content-range"), new HeaderField("content-type"), new HeaderField("cookie"), new HeaderField("date"), new HeaderField("etag"), new HeaderField("expect"), new HeaderField("expires"), new HeaderField("from"), new HeaderField("host"), new HeaderField("if-match"), new HeaderField("if-modified-since"), new HeaderField("if-none-match"), new HeaderField("if-range"), new HeaderField("if-unmodified-since"), new HeaderField("last-modified"), new HeaderField("link"), new HeaderField("location"), new HeaderField("max-forwards"), new HeaderField("proxy-authenticate"), new HeaderField("proxy-authorization"), new HeaderField("range"), new HeaderField("referer"), new HeaderField("refresh"), new HeaderField("retry-after"), new HeaderField("server"), new HeaderField("set-cookie"), new HeaderField("strict-transport-security"), new HeaderField("transfer-encoding"), new HeaderField("user-agent"), new HeaderField("vary"), new HeaderField("via"), new HeaderField("www-authenticate") };

    private static final int STATIC_TABLE_LENGTH = staticTable.length - 1;

    private static final int ENTRY_SIZE = 32;

    private static final Map<String, LinkedHashMap<String, Integer>> staticIndexes;

    static {
        staticIndexes = new HashMap<>(STATIC_TABLE_LENGTH);
        for (int i = 1; i <= STATIC_TABLE_LENGTH; i++) {
            HeaderField f = staticTable[i];
            Map<String, Integer> values = staticIndexes.computeIfAbsent(f.name, k -> new LinkedHashMap<>());
            values.put(f.value, i);
        }
    }

    private final Table dynamicTable = new Table(0);

    private int maxSize;

    private int size;

    public HeaderTable(int maxSize) {
        setMaxSize(maxSize);
    }

    public int indexOf(CharSequence name, CharSequence value) {
        String n = name.toString();
        String v = value.toString();
        Map<String, Integer> values = staticIndexes.get(n);
        if (values != null) {
            Integer idx = values.get(v);
            if (idx != null) {
                return idx;
            }
        }
        int didx = dynamicTable.indexOf(n, v);
        if (didx > 0) {
            return STATIC_TABLE_LENGTH + didx;
        } else if (didx < 0) {
            if (values != null) {
                return -values.values().iterator().next();
            } else {
                return -STATIC_TABLE_LENGTH + didx;
            }
        } else {
            if (values != null) {
                return -values.values().iterator().next();
            } else {
                return 0;
            }
        }
    }

    public int size() {
        return size;
    }

    public int maxSize() {
        return maxSize;
    }

    public int length() {
        return STATIC_TABLE_LENGTH + dynamicTable.size();
    }

    HeaderField get(int index) {
        checkIndex(index);
        if (index <= STATIC_TABLE_LENGTH) {
            return staticTable[index];
        } else {
            return dynamicTable.get(index - STATIC_TABLE_LENGTH);
        }
    }

    void put(CharSequence name, CharSequence value) {
        put(new HeaderField(name.toString(), value.toString()));
    }

    private void put(HeaderField h) {
        int entrySize = sizeOf(h);
        while (entrySize > maxSize - size && size != 0) {
            evictEntry();
        }
        if (entrySize > maxSize - size) {
            return;
        }
        size += entrySize;
        dynamicTable.add(h);
    }

    void setMaxSize(int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize >= 0: maxSize=" + maxSize);
        }
        while (maxSize < size && size != 0) {
            evictEntry();
        }
        this.maxSize = maxSize;
        int upperBound = (maxSize / ENTRY_SIZE) + 1;
        this.dynamicTable.setCapacity(upperBound);
    }

    HeaderField evictEntry() {
        HeaderField f = dynamicTable.remove();
        size -= sizeOf(f);
        return f;
    }

    @Override
    public String toString() {
        double used = maxSize == 0 ? 0 : 100 * (((double) size) / maxSize);
        return format("entries: %d; used %s/%s (%.1f%%)", dynamicTable.size(), size, maxSize, used);
    }

    int checkIndex(int index) {
        if (index < 1 || index > STATIC_TABLE_LENGTH + dynamicTable.size()) {
            throw new IllegalArgumentException(format("1 <= index <= length(): index=%s, length()=%s", index, length()));
        }
        return index;
    }

    int sizeOf(HeaderField f) {
        return f.name.length() + f.value.length() + ENTRY_SIZE;
    }

    String getStateString() {
        if (size == 0) {
            return "empty.";
        }
        StringBuilder b = new StringBuilder();
        for (int i = 1, size = dynamicTable.size(); i <= size; i++) {
            HeaderField e = dynamicTable.get(i);
            b.append(format("[%3d] (s = %3d) %s: %s\n", i, sizeOf(e), e.name, e.value));
        }
        b.append(format("      Table size:%4s", this.size));
        return b.toString();
    }

    static final class HeaderField {

        final String name;

        final String value;

        public HeaderField(String name) {
            this(name, "");
        }

        public HeaderField(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return value.isEmpty() ? name : name + ": " + value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            HeaderField that = (HeaderField) o;
            return name.equals(that.name) && value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return 31 * name.hashCode() + value.hashCode();
        }
    }

    private static final class Table {

        private final Map<String, Map<String, Long>> map;

        private final CircularBuffer<HeaderField> buffer;

        private long counter = 1;

        Table(int capacity) {
            buffer = new CircularBuffer<>(capacity);
            map = new HashMap<>(capacity);
        }

        void add(HeaderField f) {
            buffer.add(f);
            Map<String, Long> values = map.computeIfAbsent(f.name, k -> new HashMap<>());
            values.put(f.value, counter++);
        }

        HeaderField get(int index) {
            return buffer.get(index - 1);
        }

        int indexOf(String name, String value) {
            Map<String, Long> values = map.get(name);
            if (values == null) {
                return 0;
            }
            Long index = values.get(value);
            if (index != null) {
                return (int) (counter - index);
            } else {
                assert !values.isEmpty();
                Long any = values.values().iterator().next();
                return -(int) (counter - any);
            }
        }

        HeaderField remove() {
            HeaderField f = buffer.remove();
            Map<String, Long> values = map.get(f.name);
            Long index = values.remove(f.value);
            assert index != null;
            if (values.isEmpty()) {
                map.remove(f.name);
            }
            return f;
        }

        int size() {
            return buffer.size;
        }

        public void setCapacity(int capacity) {
            buffer.resize(capacity);
        }
    }

    static final class CircularBuffer<E> {

        int tail, head, size, capacity;

        Object[] elements;

        CircularBuffer(int capacity) {
            this.capacity = capacity;
            elements = new Object[capacity];
        }

        void add(E elem) {
            if (size == capacity) {
                throw new IllegalStateException(format("No room for '%s': capacity=%s", elem, capacity));
            }
            elements[head] = elem;
            head = (head + 1) % capacity;
            size++;
        }

        @SuppressWarnings("unchecked")
        E remove() {
            if (size == 0) {
                throw new NoSuchElementException("Empty");
            }
            E elem = (E) elements[tail];
            elements[tail] = null;
            tail = (tail + 1) % capacity;
            size--;
            return elem;
        }

        @SuppressWarnings("unchecked")
        E get(int index) {
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException(format("0 <= index <= capacity: index=%s, capacity=%s", index, capacity));
            }
            int idx = (tail + (size - index - 1)) % capacity;
            return (E) elements[idx];
        }

        public void resize(int newCapacity) {
            if (newCapacity < size) {
                throw new IllegalStateException(format("newCapacity >= size: newCapacity=%s, size=%s", newCapacity, size));
            }
            Object[] newElements = new Object[newCapacity];
            if (tail < head || size == 0) {
                System.arraycopy(elements, tail, newElements, 0, size);
            } else {
                System.arraycopy(elements, tail, newElements, 0, elements.length - tail);
                System.arraycopy(elements, 0, newElements, elements.length - tail, head);
            }
            elements = newElements;
            tail = 0;
            head = size;
            this.capacity = newCapacity;
        }
    }
}
