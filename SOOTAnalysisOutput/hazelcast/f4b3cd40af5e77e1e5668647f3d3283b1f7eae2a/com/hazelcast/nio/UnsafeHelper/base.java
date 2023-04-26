package com.hazelcast.nio;

import com.hazelcast.logging.Logger;
import sun.misc.Unsafe;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

public final class UnsafeHelper {

    public static final Unsafe UNSAFE;

    public static final boolean UNSAFE_AVAILABLE;

    public static final long BYTE_ARRAY_BASE_OFFSET;

    public static final long SHORT_ARRAY_BASE_OFFSET;

    public static final long CHAR_ARRAY_BASE_OFFSET;

    public static final long INT_ARRAY_BASE_OFFSET;

    public static final long FLOAT_ARRAY_BASE_OFFSET;

    public static final long LONG_ARRAY_BASE_OFFSET;

    public static final long DOUBLE_ARRAY_BASE_OFFSET;

    public static final int BYTE_ARRAY_INDEX_SCALE;

    public static final int SHORT_ARRAY_INDEX_SCALE;

    public static final int CHAR_ARRAY_INDEX_SCALE;

    public static final int INT_ARRAY_INDEX_SCALE;

    public static final int FLOAT_ARRAY_INDEX_SCALE;

    public static final int LONG_ARRAY_INDEX_SCALE;

    public static final int DOUBLE_ARRAY_INDEX_SCALE;

    public static final int MEM_COPY_THRESHOLD = 1024 * 1024;

    private static final String UNSAFE_WARNING = "sun.misc.Unsafe isn't available, some features might be not available";

    static {
        Unsafe unsafe;
        try {
            unsafe = findUnsafe();
        } catch (RuntimeException e) {
            unsafe = null;
        }
        UNSAFE = unsafe;
        BYTE_ARRAY_BASE_OFFSET = arrayBaseOffset(byte[].class, unsafe);
        SHORT_ARRAY_BASE_OFFSET = arrayBaseOffset(short[].class, unsafe);
        CHAR_ARRAY_BASE_OFFSET = arrayBaseOffset(char[].class, unsafe);
        INT_ARRAY_BASE_OFFSET = arrayBaseOffset(int[].class, unsafe);
        FLOAT_ARRAY_BASE_OFFSET = arrayBaseOffset(float[].class, unsafe);
        LONG_ARRAY_BASE_OFFSET = arrayBaseOffset(long[].class, unsafe);
        DOUBLE_ARRAY_BASE_OFFSET = arrayBaseOffset(double[].class, unsafe);
        BYTE_ARRAY_INDEX_SCALE = arrayIndexScale(byte[].class, unsafe);
        SHORT_ARRAY_INDEX_SCALE = arrayIndexScale(short[].class, unsafe);
        CHAR_ARRAY_INDEX_SCALE = arrayIndexScale(char[].class, unsafe);
        INT_ARRAY_INDEX_SCALE = arrayIndexScale(int[].class, unsafe);
        FLOAT_ARRAY_INDEX_SCALE = arrayIndexScale(float[].class, unsafe);
        LONG_ARRAY_INDEX_SCALE = arrayIndexScale(long[].class, unsafe);
        DOUBLE_ARRAY_INDEX_SCALE = arrayIndexScale(double[].class, unsafe);
        boolean unsafeAvailable = false;
        try {
            if (unsafe != null) {
                byte[] buffer = new byte[8];
                unsafe.putChar(buffer, BYTE_ARRAY_BASE_OFFSET, '0');
                unsafe.putShort(buffer, BYTE_ARRAY_BASE_OFFSET, (short) 1);
                unsafe.putInt(buffer, BYTE_ARRAY_BASE_OFFSET, 2);
                unsafe.putFloat(buffer, BYTE_ARRAY_BASE_OFFSET, 3f);
                unsafe.putLong(buffer, BYTE_ARRAY_BASE_OFFSET, 4L);
                unsafe.putDouble(buffer, BYTE_ARRAY_BASE_OFFSET, 5d);
                unsafe.copyMemory(new byte[8], BYTE_ARRAY_BASE_OFFSET, buffer, BYTE_ARRAY_BASE_OFFSET, buffer.length);
                unsafeAvailable = true;
            }
        } catch (Throwable e) {
            Logger.getLogger(UnsafeHelper.class).warning(UNSAFE_WARNING);
        }
        UNSAFE_AVAILABLE = unsafeAvailable;
    }

    private UnsafeHelper() {
    }

    private static long arrayBaseOffset(Class<?> type, Unsafe unsafe) {
        return unsafe == null ? -1 : unsafe.arrayBaseOffset(type);
    }

    private static int arrayIndexScale(Class<?> type, Unsafe unsafe) {
        return unsafe == null ? -1 : unsafe.arrayIndexScale(type);
    }

    private static Unsafe findUnsafe() {
        try {
            return Unsafe.getUnsafe();
        } catch (SecurityException se) {
            return AccessController.doPrivileged(new PrivilegedAction<Unsafe>() {

                @Override
                public Unsafe run() {
                    try {
                        Class<Unsafe> type = Unsafe.class;
                        try {
                            Field field = type.getDeclaredField("theUnsafe");
                            field.setAccessible(true);
                            return type.cast(field.get(type));
                        } catch (Exception e) {
                            for (Field field : type.getDeclaredFields()) {
                                if (type.isAssignableFrom(field.getType())) {
                                    field.setAccessible(true);
                                    return type.cast(field.get(type));
                                }
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Unsafe unavailable", e);
                    }
                    throw new RuntimeException("Unsafe unavailable");
                }
            });
        }
    }
}
