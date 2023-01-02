package org.graalvm.compiler.jtt.jdk;

import org.junit.Test;
import sun.misc.Unsafe;
import org.graalvm.compiler.jtt.JTTTest;

public class Unsafe_compareAndSwapNullCheck extends JTTTest {

    static final Unsafe unsafe = UnsafeAccess01.getUnsafe();

    static final long valueOffset;

    static {
        try {
            valueOffset = unsafe.objectFieldOffset(Unsafe_compareAndSwap.class.getDeclaredField("value"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    long value;

    long lng;

    public static void test(Unsafe_compareAndSwapNullCheck u, long expected, long newValue) {
        @SuppressWarnings("unused")
        long l = u.lng;
        unsafe.compareAndSwapLong(u, valueOffset, expected, newValue);
    }

    @Test
    public void run0() throws Throwable {
        runTest(EMPTY, false, true, "test", null, 1L, 2L);
    }
}
