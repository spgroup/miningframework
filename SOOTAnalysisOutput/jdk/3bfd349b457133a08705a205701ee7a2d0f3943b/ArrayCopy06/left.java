package org.graalvm.compiler.jtt.optimize;

import org.junit.Before;
import org.junit.Test;
import org.graalvm.compiler.jtt.JTTTest;

public class ArrayCopy06 extends JTTTest {

    public static short[] array = new short[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

    public static short[] array0 = new short[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

    static {
        System.arraycopy(array, 0, array, 0, array.length);
    }

    @Before
    public void setUp() {
        System.currentTimeMillis();
        for (int i = 0; i < array.length; i++) {
            array[i] = array0[i];
        }
    }

    public static short[] test(int srcPos, int destPos, int length) {
        System.arraycopy(array, srcPos, array, destPos, length);
        return array;
    }

    @Test
    public void run0() throws Throwable {
        runTest("test", 0, 0, 0);
    }

    @Test
    public void run1() throws Throwable {
        runTest("test", 0, 0, -1);
    }

    @Test
    public void run2() throws Throwable {
        runTest("test", -1, 0, 0);
    }

    @Test
    public void run3() throws Throwable {
        runTest("test", 0, -1, 0);
    }

    @Test
    public void run4() throws Throwable {
        runTest("test", 0, 0, 2);
    }

    @Test
    public void run5() throws Throwable {
        runTest("test", 0, 1, 11);
    }

    @Test
    public void run6() throws Throwable {
        runTest("test", 1, 0, 11);
    }

    @Test
    public void run7() throws Throwable {
        runTest("test", 1, 1, -1);
    }

    @Test
    public void run8() throws Throwable {
        runTest("test", 0, 1, 2);
    }

    @Test
    public void run9() throws Throwable {
        runTest("test", 1, 0, 2);
    }

    @Test
    public void run10() throws Throwable {
        runTest("test", 1, 1, 2);
    }

    @Test
    public void run11() throws Throwable {
        runTest("test", 0, 0, 6);
    }

    @Test
    public void run12() throws Throwable {
        runTest("test", 0, 1, 5);
    }

    @Test
    public void run13() throws Throwable {
        runTest("test", 1, 0, 5);
    }

    @Test
    public void run14() throws Throwable {
        runTest("test", 1, 1, 5);
    }

    @Test
    public void run15() throws Throwable {
        runTest("test", 0, 0, 11);
    }

    @Test
    public void run16() throws Throwable {
        runTest("test", 0, 1, 10);
    }

    @Test
    public void run17() throws Throwable {
        runTest("test", 1, 0, 10);
    }
}
