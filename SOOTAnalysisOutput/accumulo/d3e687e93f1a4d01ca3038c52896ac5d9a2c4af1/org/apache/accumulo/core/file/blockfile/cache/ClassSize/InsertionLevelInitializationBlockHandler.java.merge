package org.apache.accumulo.core.file.blockfile.cache;

import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClassSize {

    static final Log LOG = LogFactory.getLog(ClassSize.class);

    public static final int ARRAY;

    public static final int ARRAYLIST;

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

    public static final int REENTRANT_LOCK;

    public static final int ATOMIC_LONG;

    public static final int ATOMIC_INTEGER;

    public static final int ATOMIC_BOOLEAN;

    public static final int COPYONWRITE_ARRAYSET;

    public static final int COPYONWRITE_ARRAYLIST;

    private static final String THIRTY_TWO = "32";

    static {
        Properties sysProps = System.getProperties();
        String arcModel = sysProps.getProperty("sun.arch.data.model");
        REFERENCE = arcModel.equals(THIRTY_TWO) ? 4 : 8;
        OBJECT = 2 * REFERENCE;
        ARRAY = 3 * REFERENCE;
        ARRAYLIST = align(OBJECT + align(REFERENCE) + align(ARRAY) + (2 * SizeConstants.SIZEOF_INT));
        BYTE_BUFFER = align(OBJECT + align(REFERENCE) + align(ARRAY) + (5 * SizeConstants.SIZEOF_INT) + (3 * SizeConstants.SIZEOF_BOOLEAN) + SizeConstants.SIZEOF_LONG);
        INTEGER = align(OBJECT + SizeConstants.SIZEOF_INT);
        MAP_ENTRY = align(OBJECT + 5 * REFERENCE + SizeConstants.SIZEOF_BOOLEAN);
        TREEMAP = align(OBJECT + (2 * SizeConstants.SIZEOF_INT) + align(7 * REFERENCE));
        STRING = align(OBJECT + ARRAY + REFERENCE + 3 * SizeConstants.SIZEOF_INT);
        CONCURRENT_HASHMAP = align((2 * SizeConstants.SIZEOF_INT) + ARRAY + (6 * REFERENCE) + OBJECT);
        CONCURRENT_HASHMAP_ENTRY = align(REFERENCE + OBJECT + (3 * REFERENCE) + (2 * SizeConstants.SIZEOF_INT));
        CONCURRENT_HASHMAP_SEGMENT = align(REFERENCE + OBJECT + (3 * SizeConstants.SIZEOF_INT) + SizeConstants.SIZEOF_FLOAT + ARRAY);
        CONCURRENT_SKIPLISTMAP = align(SizeConstants.SIZEOF_INT + OBJECT + (8 * REFERENCE));
        CONCURRENT_SKIPLISTMAP_ENTRY = align(align(OBJECT + (3 * REFERENCE)) + align((OBJECT + (3 * REFERENCE)) / 2));
        REENTRANT_LOCK = align(OBJECT + (3 * REFERENCE));
        ATOMIC_LONG = align(OBJECT + SizeConstants.SIZEOF_LONG);
        ATOMIC_INTEGER = align(OBJECT + SizeConstants.SIZEOF_INT);
        ATOMIC_BOOLEAN = align(OBJECT + SizeConstants.SIZEOF_BOOLEAN);
        COPYONWRITE_ARRAYSET = align(OBJECT + REFERENCE);
        COPYONWRITE_ARRAYLIST = align(OBJECT + (2 * REFERENCE) + ARRAY);
    }

    public static int align(int num) {
        return (int) (align((long) num));
    }

    public static long align(long num) {
        return ((num + 7) >> 3) << 3;
    }
}