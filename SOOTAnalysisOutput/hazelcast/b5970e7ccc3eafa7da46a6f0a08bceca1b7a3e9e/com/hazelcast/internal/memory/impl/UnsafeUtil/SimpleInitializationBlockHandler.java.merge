package com.hazelcast.internal.memory.impl;

import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.nio.Bits;
import sun.misc.Unsafe;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import static com.hazelcast.util.ExceptionUtil.rethrow;
import static com.hazelcast.util.QuickMath.normalize;

public final class UnsafeUtil {

    static final boolean UNSAFE_AVAILABLE;

    static final Unsafe UNSAFE;

    private static final ILogger LOGGER = Logger.getLogger(UnsafeUtil.class);

    static {
        Unsafe unsafe;
        try {
            unsafe = findUnsafe();
            if (unsafe != null) {
                checkUnsafeInstance(unsafe);
            }
        } catch (Throwable t) {
            unsafe = null;
            logFailureToFindUnsafeDueTo(t);
        }
        UNSAFE = unsafe;
        UNSAFE_AVAILABLE = UNSAFE != null;
    }

    private UnsafeUtil() {
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
                    } catch (Throwable t) {
                        throw rethrow(t);
                    }
                    throw new RuntimeException("Unsafe unavailable");
                }
            });
        }
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private static void checkUnsafeInstance(Unsafe unsafe) {
        long arrayBaseOffset = unsafe.arrayBaseOffset(byte[].class);
        byte[] buffer = new byte[(int) arrayBaseOffset + (2 * Bits.LONG_SIZE_IN_BYTES)];
        unsafe.putByte(buffer, arrayBaseOffset, (byte) 0x00);
        unsafe.putBoolean(buffer, arrayBaseOffset, false);
        unsafe.putChar(buffer, normalize(arrayBaseOffset, Bits.CHAR_SIZE_IN_BYTES), '0');
        unsafe.putShort(buffer, normalize(arrayBaseOffset, Bits.SHORT_SIZE_IN_BYTES), (short) 1);
        unsafe.putInt(buffer, normalize(arrayBaseOffset, Bits.INT_SIZE_IN_BYTES), 2);
        unsafe.putFloat(buffer, normalize(arrayBaseOffset, Bits.FLOAT_SIZE_IN_BYTES), 3f);
        unsafe.putLong(buffer, normalize(arrayBaseOffset, Bits.LONG_SIZE_IN_BYTES), 4L);
        unsafe.putDouble(buffer, normalize(arrayBaseOffset, Bits.DOUBLE_SIZE_IN_BYTES), 5d);
        unsafe.copyMemory(new byte[buffer.length], arrayBaseOffset, buffer, arrayBaseOffset, buffer.length);
    }

    private static void logFailureToFindUnsafeDueTo(final Throwable reason) {
        if (LOGGER.isFinestEnabled()) {
            LOGGER.finest("Unable to get an instance of Unsafe. Unsafe-based operations will be unavailable", reason);
        } else {
            LOGGER.warning("Unable to get an instance of Unsafe. Unsafe-based operations will be unavailable");
        }
    }
}