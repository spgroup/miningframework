package org.apache.hadoop.hbase.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.classification.InterfaceAudience;

@InterfaceAudience.Private
public class ClassSize {

    private static final Log LOG = LogFactory.getLog(ClassSize.class);

    public static final int ARRAY;

    public static final int ARRAYLIST;

    public static final int LINKEDLIST;

    public static final int LINKEDLIST_ENTRY;

    public static final int BYTE_BUFFER;

    public static final int INTEGER;

    public static final int MAP_ENTRY;

    public static final int OBJECT;

    public static final int REFERENCE;

    public static final int STRING;

    public static final int TREEMAP;

    public static final int CONCURRENT_HASHMAP;

    public static final int CONCURRENT_HASHMAP_ENTRY;

    public static final int CONCURRENT_HASHMAP_SEGMENT;

    public static final int CONCURRENT_SKIPLISTMAP;

    public static final int CONCURRENT_SKIPLISTMAP_ENTRY;

    public static final int CELL_ARRAY_MAP;

    public static final int CELL_ARRAY_MAP_ENTRY;

    public static final int REENTRANT_LOCK;

    public static final int ATOMIC_LONG;

    public static final int ATOMIC_INTEGER;

    public static final int ATOMIC_BOOLEAN;

    public static final int ATOMIC_REFERENCE;

    public static final int COPYONWRITE_ARRAYSET;

    public static final int COPYONWRITE_ARRAYLIST;

    public static final int TIMERANGE;

    public static final int TIMERANGE_TRACKER;

    public static final int CELL_SET;

    public static final int STORE_SERVICES;

    private static final boolean JDK7;

    static {
        final String version = System.getProperty("java.version");
        if (version == null || !version.matches("\\d\\.\\d\\..*")) {
            throw new RuntimeException("Unexpected version format: " + version);
        }
        int major = (int) (version.charAt(0) - '0');
        int minor = (int) (version.charAt(2) - '0');
        JDK7 = major == 1 && minor == 7;
    }

    private static class MemoryLayout {

        int headerSize() {
            return 2 * oopSize();
        }

        int arrayHeaderSize() {
            return (int) align(3 * oopSize());
        }

        int oopSize() {
            return is32BitJVM() ? 4 : 8;
        }

        public long align(long num) {
            return ((num + 7) >> 3) << 3;
        }

        long sizeOf(byte[] b, int len) {
            return align(arrayHeaderSize() + len);
        }
    }

    private static class UnsafeLayout extends MemoryLayout {

        @SuppressWarnings("unused")
        private static final class HeaderSize {

            private byte a;
        }

        public UnsafeLayout() {
        }

        @Override
        int headerSize() {
            try {
                return (int) UnsafeAccess.theUnsafe.objectFieldOffset(HeaderSize.class.getDeclaredField("a"));
            } catch (NoSuchFieldException | SecurityException e) {
                LOG.error(e);
            }
            return super.headerSize();
        }

        @Override
        int arrayHeaderSize() {
            return UnsafeAccess.theUnsafe.arrayBaseOffset(byte[].class);
        }

        @Override
        @SuppressWarnings("static-access")
        int oopSize() {
            return UnsafeAccess.theUnsafe.ARRAY_OBJECT_INDEX_SCALE;
        }

        @Override
        @SuppressWarnings("static-access")
        long sizeOf(byte[] b, int len) {
            return align(arrayHeaderSize() + len * UnsafeAccess.theUnsafe.ARRAY_BYTE_INDEX_SCALE);
        }
    }

    private static MemoryLayout getMemoryLayout() {
        String enabled = System.getProperty("hbase.memorylayout.use.unsafe");
        if (UnsafeAvailChecker.isAvailable() && (enabled == null || Boolean.parseBoolean(enabled))) {
            LOG.debug("Using Unsafe to estimate memory layout");
            return new UnsafeLayout();
        }
        LOG.debug("Not using Unsafe to estimate memory layout");
        return new MemoryLayout();
    }

    private static final MemoryLayout memoryLayout = getMemoryLayout();

    static {
        REFERENCE = memoryLayout.oopSize();
        OBJECT = memoryLayout.headerSize();
        ARRAY = memoryLayout.arrayHeaderSize();
        ARRAYLIST = align(OBJECT + REFERENCE + (2 * Bytes.SIZEOF_INT)) + align(ARRAY);
        LINKEDLIST = align(OBJECT + (2 * Bytes.SIZEOF_INT) + (2 * REFERENCE));
        LINKEDLIST_ENTRY = align(OBJECT + (2 * REFERENCE));
        BYTE_BUFFER = align(OBJECT + REFERENCE + (5 * Bytes.SIZEOF_INT) + (3 * Bytes.SIZEOF_BOOLEAN) + Bytes.SIZEOF_LONG) + align(ARRAY);
        INTEGER = align(OBJECT + Bytes.SIZEOF_INT);
        MAP_ENTRY = align(OBJECT + 5 * REFERENCE + Bytes.SIZEOF_BOOLEAN);
        TREEMAP = align(OBJECT + (2 * Bytes.SIZEOF_INT) + 7 * REFERENCE);
        STRING = (int) estimateBase(String.class, false);
        CONCURRENT_HASHMAP = (int) estimateBase(ConcurrentHashMap.class, false);
        CONCURRENT_HASHMAP_ENTRY = align(REFERENCE + OBJECT + (3 * REFERENCE) + (2 * Bytes.SIZEOF_INT));
        CONCURRENT_HASHMAP_SEGMENT = align(REFERENCE + OBJECT + (3 * Bytes.SIZEOF_INT) + Bytes.SIZEOF_FLOAT + ARRAY);
        CONCURRENT_SKIPLISTMAP = (int) estimateBase(ConcurrentSkipListMap.class, false);
        CELL_ARRAY_MAP = align(OBJECT + 2 * Bytes.SIZEOF_INT + Bytes.SIZEOF_BOOLEAN + ARRAY + 2 * REFERENCE);
        CONCURRENT_SKIPLISTMAP_ENTRY = align(align(OBJECT + (3 * REFERENCE)) + align((OBJECT + (3 * REFERENCE)) / 2));
        CELL_ARRAY_MAP_ENTRY = align(REFERENCE);
        REENTRANT_LOCK = align(OBJECT + (3 * REFERENCE));
        ATOMIC_LONG = align(OBJECT + Bytes.SIZEOF_LONG);
        ATOMIC_INTEGER = align(OBJECT + Bytes.SIZEOF_INT);
        ATOMIC_BOOLEAN = align(OBJECT + Bytes.SIZEOF_BOOLEAN);
        ATOMIC_REFERENCE = align(OBJECT + REFERENCE);
        COPYONWRITE_ARRAYSET = align(OBJECT + REFERENCE);
        COPYONWRITE_ARRAYLIST = align(OBJECT + (2 * REFERENCE) + ARRAY);
        TIMERANGE = align(ClassSize.OBJECT + Bytes.SIZEOF_LONG * 2 + Bytes.SIZEOF_BOOLEAN);
        TIMERANGE_TRACKER = align(ClassSize.OBJECT + Bytes.SIZEOF_LONG * 2);
        CELL_SET = align(OBJECT + REFERENCE);
        STORE_SERVICES = align(OBJECT + REFERENCE + ATOMIC_LONG);
    }

    @SuppressWarnings("unchecked")
    private static int[] getSizeCoefficients(Class cl, boolean debug) {
        int primitives = 0;
        int arrays = 0;
        int references = 0;
        int index = 0;
        for (; null != cl; cl = cl.getSuperclass()) {
            Field[] field = cl.getDeclaredFields();
            if (null != field) {
                for (Field aField : field) {
                    if (Modifier.isStatic(aField.getModifiers()))
                        continue;
                    Class fieldClass = aField.getType();
                    if (fieldClass.isArray()) {
                        arrays++;
                        references++;
                    } else if (!fieldClass.isPrimitive()) {
                        references++;
                    } else {
                        String name = fieldClass.getName();
                        if (name.equals("int") || name.equals("I"))
                            primitives += Bytes.SIZEOF_INT;
                        else if (name.equals("long") || name.equals("J"))
                            primitives += Bytes.SIZEOF_LONG;
                        else if (name.equals("boolean") || name.equals("Z"))
                            primitives += Bytes.SIZEOF_BOOLEAN;
                        else if (name.equals("short") || name.equals("S"))
                            primitives += Bytes.SIZEOF_SHORT;
                        else if (name.equals("byte") || name.equals("B"))
                            primitives += Bytes.SIZEOF_BYTE;
                        else if (name.equals("char") || name.equals("C"))
                            primitives += Bytes.SIZEOF_CHAR;
                        else if (name.equals("float") || name.equals("F"))
                            primitives += Bytes.SIZEOF_FLOAT;
                        else if (name.equals("double") || name.equals("D"))
                            primitives += Bytes.SIZEOF_DOUBLE;
                    }
                    if (debug) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("" + index + " " + aField.getName() + " " + aField.getType());
                        }
                    }
                    index++;
                }
            }
        }
        return new int[] { primitives, arrays, references };
    }

    private static long estimateBaseFromCoefficients(int[] coeff, boolean debug) {
        long prealign_size = OBJECT + coeff[0] + coeff[2] * REFERENCE;
        long size = align(prealign_size) + align(coeff[1] * ARRAY);
        if (debug) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Primitives=" + coeff[0] + ", arrays=" + coeff[1] + ", references=" + coeff[2] + ", refSize " + REFERENCE + ", size=" + size + ", prealign_size=" + prealign_size);
            }
        }
        return size;
    }

    @SuppressWarnings("unchecked")
    public static long estimateBase(Class cl, boolean debug) {
        return estimateBaseFromCoefficients(getSizeCoefficients(cl, debug), debug);
    }

    public static int align(int num) {
        return (int) (align((long) num));
    }

    public static long align(long num) {
        return memoryLayout.align(num);
    }

    public static boolean is32BitJVM() {
        final String model = System.getProperty("sun.arch.data.model");
        return model != null && model.equals("32");
    }

    public static long sizeOf(byte[] b, int len) {
        return memoryLayout.sizeOf(b, len);
    }
}