package org.graalvm.compiler.test;

import static org.graalvm.compiler.debug.DebugContext.DEFAULT_LOG_STREAM;
import static org.graalvm.compiler.debug.DebugContext.NO_DESCRIPTION;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.graalvm.compiler.debug.DebugContext;
import org.graalvm.compiler.debug.DebugDumpHandler;
import org.graalvm.compiler.debug.DebugHandlersFactory;
import org.graalvm.compiler.debug.GlobalMetrics;
import org.graalvm.compiler.options.OptionValues;
import org.graalvm.compiler.serviceprovider.GraalServices;
import org.graalvm.compiler.serviceprovider.GraalUnsafeAccess;
import org.junit.After;
import org.junit.Assert;
import org.junit.AssumptionViolatedException;
import org.junit.internal.ComparisonCriteria;
import org.junit.internal.ExactComparisonCriteria;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import sun.misc.Unsafe;

public class GraalTest {

    public static final Unsafe UNSAFE = GraalUnsafeAccess.getUnsafe();

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

    protected Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
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

    public static void assumeManagementLibraryIsLoadable() {
        try {
            GraalServices.getCurrentThreadAllocatedBytes();
        } catch (UnsatisfiedLinkError | NoClassDefFoundError | UnsupportedOperationException e) {
            throw new AssumptionViolatedException("Management interface is unavailable: " + e);
        }
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

    protected Collection<DebugHandlersFactory> getDebugHandlersFactories() {
        return Collections.emptyList();
    }

    protected DebugContext getDebugContext(OptionValues options) {
        return getDebugContext(options, null, null);
    }

    protected DebugContext getDebugContext(OptionValues options, String id, ResolvedJavaMethod method) {
        List<DebugContext> cached = cachedDebugs.get();
        if (cached == null) {
            cached = new ArrayList<>();
            cachedDebugs.set(cached);
        }
        for (DebugContext debug : cached) {
            if (debug.getOptions() == options) {
                return debug;
            }
        }
        final DebugContext.Description descr;
        if (method == null) {
            descr = NO_DESCRIPTION;
        } else {
            descr = new DebugContext.Description(method, id == null ? method.getName() : id);
        }
        DebugContext debug = DebugContext.create(options, descr, globalMetrics, DEFAULT_LOG_STREAM, getDebugHandlersFactories());
        cached.add(debug);
        return debug;
    }

    private static final GlobalMetrics globalMetrics = new GlobalMetrics();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread("GlobalMetricsPrinter") {

            @Override
            public void run() {
            }
        });
    }

    private final ThreadLocal<List<DebugContext>> cachedDebugs = new ThreadLocal<>();

    @After
    public void afterTest() {
        List<DebugContext> cached = cachedDebugs.get();
        if (cached != null) {
            for (DebugContext debug : cached) {
                debug.close();
                debug.closeDumpHandlers(true);
            }
        }
    }

    private static final double TIMEOUT_SCALING_FACTOR = Double.parseDouble(System.getProperty("graaltest.timeout.factor", "1.0"));

    public static TestRule createTimeout(long length, TimeUnit timeUnit) {
        Timeout timeout = new Timeout((long) (length * TIMEOUT_SCALING_FACTOR), timeUnit);
        try {
            return new DisableOnDebug(timeout);
        } catch (LinkageError ex) {
            return timeout;
        }
    }

    public static TestRule createTimeoutSeconds(int seconds) {
        return createTimeout(seconds, TimeUnit.SECONDS);
    }

    public static TestRule createTimeoutMillis(long milliseconds) {
        return createTimeout(milliseconds, TimeUnit.MILLISECONDS);
    }

    public static class TemporaryDirectory implements AutoCloseable {

        public final Path path;

        private IOException closeException;

        public TemporaryDirectory(Path dir, String prefix, FileAttribute<?>... attrs) throws IOException {
            path = Files.createTempDirectory(dir == null ? Paths.get(".") : dir, prefix, attrs);
        }

        @Override
        public void close() {
            closeException = removeDirectory(path);
        }

        public IOException getCloseException() {
            return closeException;
        }

        @Override
        public String toString() {
            return path.toString();
        }
    }

    public static IOException removeDirectory(Path directory) {
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.err.println(e);
            return e;
        }
        return null;
    }
}