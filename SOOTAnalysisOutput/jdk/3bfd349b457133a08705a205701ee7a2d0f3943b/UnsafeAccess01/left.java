package org.graalvm.compiler.jtt.jdk;

import java.lang.reflect.Field;
import org.junit.Test;
import sun.misc.Unsafe;
import org.graalvm.compiler.jtt.JTTTest;

public class UnsafeAccess01 extends JTTTest {

    private static int randomValue = 100;

    private static final Unsafe unsafe;

    private static final long offset;

    private static Object staticObject = new TestClass();

    static {
        unsafe = getUnsafe();
        Field field = null;
        try {
            field = TestClass.class.getDeclaredField("field");
        } catch (NoSuchFieldException e) {
        } catch (SecurityException e) {
        }
        offset = unsafe.objectFieldOffset(field);
    }

    private static class TestClass {

        private int field = 42;
    }

    public static int test() {
        final TestClass object = new TestClass();
        final int value = unsafe.getInt(object, offset);
        return value;
    }

    static Unsafe getUnsafe() {
        try {
            final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (Unsafe) unsafeField.get(null);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Test
    public void run0() throws Throwable {
        runTest("test");
    }

    @Test
    public void runDiamond() throws Throwable {
        runTest("testDiamond");
    }

    public static int testDiamond() {
        final Object object = staticObject;
        final int oldValue = ((TestClass) object).field;
        if (randomValue == 100) {
            unsafe.putInt(object, offset, 41);
        } else {
            unsafe.putInt(object, offset, 40);
        }
        unsafe.putInt(object, offset, 42);
        return oldValue;
    }
}
