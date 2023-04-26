package org.apache.accumulo.core.file.blockfile.cache;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClassSize {

    static final Log LOG = LogFactory.getLog(ClassSize.class);

    private static int nrOfRefsPerObj = 2;

    public static int ARRAY = 0;

    public static int ARRAYLIST = 0;

    public static int BYTE_BUFFER = 0;

    public static int INTEGER = 0;

    public static int MAP_ENTRY = 0;

    public static int OBJECT = 0;

    public static int REFERENCE = 0;

    public static int STRING = 0;

    public static int TREEMAP = 0;

    public static int CONCURRENT_HASHMAP = 0;

    public static int CONCURRENT_HASHMAP_ENTRY = 0;

    public static int CONCURRENT_HASHMAP_SEGMENT = 0;

    public static int CONCURRENT_SKIPLISTMAP = 0;

    public static int CONCURRENT_SKIPLISTMAP_ENTRY = 0;

    public static int REENTRANT_LOCK = 0;

    public static int ATOMIC_LONG = 0;

    public static int ATOMIC_INTEGER = 0;

    public static int ATOMIC_BOOLEAN = 0;

    public static int COPYONWRITE_ARRAYSET = 0;

    public static int COPYONWRITE_ARRAYLIST = 0;

    private static final String THIRTY_TWO = "32";

    static {
        Properties sysProps = System.getProperties();
        String arcModel = sysProps.getProperty("sun.arch.data.model");
        REFERENCE = 8;
        if (arcModel.equals(THIRTY_TWO)) {
            REFERENCE = 4;
        }
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

    private static int[] getSizeCoefficients(Class<?> cl, boolean debug) {
        int primitives = 0;
        int arrays = 0;
        int references = nrOfRefsPerObj;
        for (; null != cl; cl = cl.getSuperclass()) {
            Field[] field = cl.getDeclaredFields();
            if (null != field) {
                for (int i = 0; i < field.length; i++) {
                    if (!Modifier.isStatic(field[i].getModifiers())) {
                        Class<?> fieldClass = field[i].getType();
                        if (fieldClass.isArray()) {
                            arrays++;
                            references++;
                        } else if (!fieldClass.isPrimitive()) {
                            references++;
                        } else {
                            String name = fieldClass.getName();
                            if (name.equals("int") || name.equals("I"))
                                primitives += SizeConstants.SIZEOF_INT;
                            else if (name.equals("long") || name.equals("J"))
                                primitives += SizeConstants.SIZEOF_LONG;
                            else if (name.equals("boolean") || name.equals("Z"))
                                primitives += SizeConstants.SIZEOF_BOOLEAN;
                            else if (name.equals("short") || name.equals("S"))
                                primitives += SizeConstants.SIZEOF_SHORT;
                            else if (name.equals("byte") || name.equals("B"))
                                primitives += SizeConstants.SIZEOF_BYTE;
                            else if (name.equals("char") || name.equals("C"))
                                primitives += SizeConstants.SIZEOF_CHAR;
                            else if (name.equals("float") || name.equals("F"))
                                primitives += SizeConstants.SIZEOF_FLOAT;
                            else if (name.equals("double") || name.equals("D"))
                                primitives += SizeConstants.SIZEOF_DOUBLE;
                        }
                        if (debug) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(field[i].getName() + "\n\t" + field[i].getType());
                            }
                        }
                    }
                }
            }
        }
        return new int[] { primitives, arrays, references };
    }

    private static long estimateBaseFromCoefficients(int[] coeff, boolean debug) {
        long size = coeff[0] + align(coeff[1] * ARRAY) + coeff[2] * REFERENCE;
        size = align(size);
        if (debug) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Primitives " + coeff[0] + ", arrays " + coeff[1] + ", references(includes " + nrOfRefsPerObj + " for object overhead) " + coeff[2] + ", refSize " + REFERENCE + ", size " + size);
            }
        }
        return size;
    }

    public static long estimateBase(Class<?> cl, boolean debug) {
        return estimateBaseFromCoefficients(getSizeCoefficients(cl, debug), debug);
    }

    public static int align(int num) {
        return (int) (align((long) num));
    }

    public static long align(long num) {
        return ((num + 7) >> 3) << 3;
    }
}
