package org.graalvm.compiler.test;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.internal.ComparisonCriteria;
import org.junit.internal.ExactComparisonCriteria;
import sun.misc.Unsafe;

public class GraalTest {

    public static final Unsafe UNSAFE;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(Unsafe.class);
        } catch (Exception e) {
            throw new RuntimeException("exception while trying to get Unsafe", e);
        }
    }

    public static final boolean Java8OrEarlier = System.getProperty("java.specification.version").compareTo("1.9") < 0;

    protected Method getMethod(String methodName) {
        return getMethod(getClass(), methodName);
    }

    protected Method getMethod(Class<?> clazz, String methodName) {
        Method found = null;
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(methodName)) {
                Assert.assertNull(found);
                found = m;
            }
        }
        if (found == null) {
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equals(methodName)) {
                    Assert.assertNull(found);
                    found = m;
                }
            }
        }
        if (found != null) {
            return found;
        } else {
            throw new RuntimeException("method not found: " + methodName);
        }
    }

    protected Method getMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException("method not found: " + methodName + "" + Arrays.toString(parameterTypes));
        }
    }

    protected void assertDeepEquals(Object expected, Object actual) {
        assertDeepEquals(null, expected, actual);
    }

    protected void assertDeepEquals(String message, Object expected, Object actual) {
        if (ulpsDelta() > 0) {
            assertDeepEquals(message, expected, actual, ulpsDelta());
        } else {
            assertDeepEquals(message, expected, actual, equalFloatsOrDoublesDelta());
        }
    }

    protected void assertDeepEquals(String message, Object expected, Object actual, double delta) {
        if (expected != null && actual != null) {
            Class<?> expectedClass = expected.getClass();
            Class<?> actualClass = actual.getClass();
            if (expectedClass.isArray()) {
                Assert.assertTrue(message, expected != null);
                Assert.assertTrue(message, actual != null);
                Assert.assertEquals(message, expectedClass, actual.getClass());
                if (expected instanceof int[]) {
                    Assert.assertArrayEquals(message, (int[]) expected, (int[]) actual);
                } else if (expected instanceof byte[]) {
                    Assert.assertArrayEquals(message, (byte[]) expected, (byte[]) actual);
                } else if (expected instanceof char[]) {
                    Assert.assertArrayEquals(message, (char[]) expected, (char[]) actual);
                } else if (expected instanceof short[]) {
                    Assert.assertArrayEquals(message, (short[]) expected, (short[]) actual);
                } else if (expected instanceof float[]) {
                    Assert.assertArrayEquals(message, (float[]) expected, (float[]) actual, (float) delta);
                } else if (expected instanceof long[]) {
                    Assert.assertArrayEquals(message, (long[]) expected, (long[]) actual);
                } else if (expected instanceof double[]) {
                    Assert.assertArrayEquals(message, (double[]) expected, (double[]) actual, delta);
                } else if (expected instanceof boolean[]) {
                    new ExactComparisonCriteria().arrayEquals(message, expected, actual);
                } else if (expected instanceof Object[]) {
                    new ComparisonCriteria() {

                        @Override
                        protected void assertElementsEqual(Object e, Object a) {
                            assertDeepEquals(message, e, a, delta);
                        }
                    }.arrayEquals(message, expected, actual);
                } else {
                    Assert.fail((message == null ? "" : message) + "non-array value encountered: " + expected);
                }
            } else if (expectedClass.equals(double.class) && actualClass.equals(double.class)) {
                Assert.assertEquals((double) expected, (double) actual, delta);
            } else if (expectedClass.equals(float.class) && actualClass.equals(float.class)) {
                Assert.assertEquals((float) expected, (float) actual, delta);
            } else {
                Assert.assertEquals(message, expected, actual);
            }
        } else {
            Assert.assertEquals(message, expected, actual);
        }
    }

    protected void assertDeepEquals(String message, Object expected, Object actual, int ulpsDelta) {
        ComparisonCriteria doubleUlpsDeltaCriteria = new ComparisonCriteria() {

            @Override
            protected void assertElementsEqual(Object e, Object a) {
                assertTrue(message, e instanceof Double && a instanceof Double);
                double de = (Double) e;
                double epsilon = (!Double.isNaN(de) && Double.isFinite(de) ? ulpsDelta * Math.ulp(de) : 0);
                Assert.assertEquals(message, (Double) e, (Double) a, epsilon);
            }
        };
        ComparisonCriteria floatUlpsDeltaCriteria = new ComparisonCriteria() {

            @Override
            protected void assertElementsEqual(Object e, Object a) {
                assertTrue(message, e instanceof Float && a instanceof Float);
                float fe = (Float) e;
                float epsilon = (!Float.isNaN(fe) && Float.isFinite(fe) ? ulpsDelta * Math.ulp(fe) : 0);
                Assert.assertEquals(message, (Float) e, (Float) a, epsilon);
            }
        };
        if (expected != null && actual != null) {
            Class<?> expectedClass = expected.getClass();
            Class<?> actualClass = actual.getClass();
            if (expectedClass.isArray()) {
                Assert.assertEquals(message, expectedClass, actualClass);
                if (expected instanceof double[] || expected instanceof Object[]) {
                    doubleUlpsDeltaCriteria.arrayEquals(message, expected, actual);
                    return;
                } else if (expected instanceof float[] || expected instanceof Object[]) {
                    floatUlpsDeltaCriteria.arrayEquals(message, expected, actual);
                    return;
                }
            } else if (expectedClass.equals(double.class) && actualClass.equals(double.class)) {
                doubleUlpsDeltaCriteria.arrayEquals(message, expected, actual);
                return;
            } else if (expectedClass.equals(float.class) && actualClass.equals(float.class)) {
                floatUlpsDeltaCriteria.arrayEquals(message, expected, actual);
                return;
            }
        }
        assertDeepEquals(message, expected, actual, equalFloatsOrDoublesDelta());
    }

    protected double equalFloatsOrDoublesDelta() {
        return 0.0D;
    }

    protected int ulpsDelta() {
        return 0;
    }

    @SuppressWarnings("serial")
    public static class MultiCauseAssertionError extends AssertionError {

        private Throwable[] causes;

        public MultiCauseAssertionError(String message, Throwable... causes) {
            super(message);
            this.causes = causes;
        }

        @Override
        public void printStackTrace(PrintStream out) {
            super.printStackTrace(out);
            int num = 0;
            for (Throwable cause : causes) {
                if (cause != null) {
                    out.print("cause " + (num++));
                    cause.printStackTrace(out);
                }
            }
        }

        @Override
        public void printStackTrace(PrintWriter out) {
            super.printStackTrace(out);
            int num = 0;
            for (Throwable cause : causes) {
                if (cause != null) {
                    out.print("cause " + (num++) + ": ");
                    cause.printStackTrace(out);
                }
            }
        }
    }

    public static void fail(String message, Object... objects) {
        AssertionError e;
        if (message == null) {
            e = new AssertionError();
        } else {
            e = new AssertionError(String.format(message, objects));
        }
        StackTraceElement[] trace = e.getStackTrace();
        int start = 1;
        String thisClassName = GraalTest.class.getName();
        while (start < trace.length && trace[start].getClassName().equals(thisClassName) && (trace[start].getMethodName().equals("assertTrue") || trace[start].getMethodName().equals("assertFalse"))) {
            start++;
        }
        e.setStackTrace(Arrays.copyOfRange(trace, start, trace.length));
        throw e;
    }

    public static void assertTrue(String message, boolean condition) {
        assertTrue(condition, message);
    }

    public static void assertTrue(boolean condition) {
        assertTrue(condition, null);
    }

    public static void assertFalse(String message, boolean condition) {
        assertTrue(!condition, message);
    }

    public static void assertFalse(boolean condition) {
        assertTrue(!condition, null);
    }

    public static void assertTrue(boolean condition, String message, Object... objects) {
        if (!condition) {
            fail(message, objects);
        }
    }

    public static void assertFalse(boolean condition, String message, Object... objects) {
        assertTrue(!condition, message, objects);
    }
}