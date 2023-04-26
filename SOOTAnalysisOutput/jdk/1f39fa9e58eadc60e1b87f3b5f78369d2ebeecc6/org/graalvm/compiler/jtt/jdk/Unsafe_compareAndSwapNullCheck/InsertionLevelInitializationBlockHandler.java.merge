package org.graalvm.compiler.jtt.jdk;

import org.graalvm.compiler.jtt.JTTTest;
import org.junit.Assume;
import org.junit.Test;

public class Unsafe_compareAndSwapNullCheck extends JTTTest {

    static final long valueOffset;

    static {
        try {
            valueOffset = UNSAFE.objectFieldOffset(Unsafe_compareAndSwap.class.getDeclaredField("value"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    long value;

    long lng;

    public static void test(Unsafe_compareAndSwapNullCheck u, long expected, long newValue) {
        @SuppressWarnings("unused")
        long l = u.lng;
        UNSAFE.compareAndSwapLong(u, valueOffset, expected, newValue);
    }

    @Test
    public void run0() throws Throwable {
        Assume.assumeTrue(Java8OrEarlier);
        runTest(getInitialOptions(), EMPTY, false, true, "test", null, 1L, 2L);
    }
}