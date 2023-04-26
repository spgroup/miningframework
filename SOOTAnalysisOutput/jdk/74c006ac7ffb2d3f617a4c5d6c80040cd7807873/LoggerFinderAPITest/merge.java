import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import sun.util.logging.PlatformLogger;

public class LoggerFinderAPITest {

    static final Class<java.lang.System.Logger> spiLoggerClass = java.lang.System.Logger.class;

    static final Class<java.lang.System.Logger> jdkLoggerClass = java.lang.System.Logger.class;

    static final Class<sun.util.logging.PlatformLogger.Bridge> bridgeLoggerClass = sun.util.logging.PlatformLogger.Bridge.class;

    static final Class<java.util.logging.Logger> julLoggerClass = java.util.logging.Logger.class;

    static final Class<sun.util.logging.PlatformLogger.Bridge> julLogProducerClass = PlatformLogger.Bridge.class;

    static final Pattern julLogNames = Pattern.compile("^((log(p|rb)?)|severe|warning|info|config|fine|finer|finest|isLoggable)$");

    static final Collection<Method> julLoggerIgnores;

    static {
        List<Method> ignores = new ArrayList<>();
        try {
            ignores.add(julLoggerClass.getDeclaredMethod("log", LogRecord.class));
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new ExceptionInInitializerError(ex);
        }
        julLoggerIgnores = Collections.unmodifiableList(ignores);
    }

    interface LoggerBridgeMethodsWithNoBody extends PlatformLogger.Bridge, java.lang.System.Logger {

        @Override
        public default String getName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public default boolean isLoggable(PlatformLogger.Level level) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public default void log(sun.util.logging.PlatformLogger.Level level, String msg, Throwable thrown) {
        }

        @Override
        public default void log(sun.util.logging.PlatformLogger.Level level, Throwable thrown, Supplier<String> msgSupplier) {
        }

        @Override
        public default void log(sun.util.logging.PlatformLogger.Level level, Supplier<String> msgSupplier) {
        }

        @Override
        public default void log(sun.util.logging.PlatformLogger.Level level, String msg) {
        }

        @Override
        public default void log(sun.util.logging.PlatformLogger.Level level, String format, Object... params) {
        }

        @Override
        public default void logrb(sun.util.logging.PlatformLogger.Level level, ResourceBundle bundle, String key, Throwable thrown) {
        }

        @Override
        public default void logrb(sun.util.logging.PlatformLogger.Level level, ResourceBundle bundle, String format, Object... params) {
        }

        @Override
        public default void logrb(PlatformLogger.Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg, Throwable thrown) {
        }

        @Override
        public default void logrb(PlatformLogger.Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg, Object... params) {
        }

        @Override
        public default void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, Supplier<String> msgSupplier) {
        }

        @Override
        public default void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, String msg, Object... params) {
        }

        @Override
        public default void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown) {
        }

        @Override
        public default void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, String msg) {
        }

        @Override
        public default void logp(PlatformLogger.Level level, String sourceClass, String sourceMethod, Throwable thrown, Supplier<String> msgSupplier) {
        }

        static boolean requiresDefaultBodyFor(Method m) {
            try {
                Method m2 = LoggerBridgeMethodsWithNoBody.class.getDeclaredMethod(m.getName(), m.getParameterTypes());
                return !m2.isDefault();
            } catch (NoSuchMethodException x) {
                return true;
            }
        }
    }

    final boolean warnDuplicateMappings;

    public LoggerFinderAPITest(boolean verbose) {
        this.warnDuplicateMappings = verbose;
        for (Handler h : Logger.getLogger("").getHandlers()) {
            if (h instanceof ConsoleHandler) {
                Logger.getLogger("").removeHandler(h);
            }
        }
        Logger.getLogger("").addHandler(new Handler() {

            @Override
            public void publish(LogRecord record) {
                StringBuilder builder = new StringBuilder();
                builder.append("GOT LogRecord: ").append(record.getLevel().getLocalizedName()).append(": [").append(record.getLoggerName()).append("] ").append(record.getSourceClassName()).append('.').append(record.getSourceMethodName()).append(" -> ").append(record.getMessage()).append(' ').append(record.getParameters() == null ? "" : Arrays.toString(record.getParameters()));
                System.out.println(builder);
                if (record.getThrown() != null) {
                    record.getThrown().printStackTrace(System.out);
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
    }

    public Stream<Method> getJulLogMethodStream(Class<?> loggerClass) {
        return Stream.of(loggerClass.getMethods()).filter((x) -> {
            final Matcher m = julLogNames.matcher(x.getName());
            return m.matches() ? x.getAnnotation(Deprecated.class) == null : false;
        });
    }

    public boolean canBeInvokedAs(Method origin, Method target, Map<Class<?>, Class<?>> substitutes) {
        final Class<?>[] xParams = target.getParameterTypes();
        final Class<?>[] mParams = Stream.of(origin.getParameterTypes()).map((x) -> substitutes.getOrDefault(x, x)).collect(Collectors.toList()).toArray(new Class<?>[0]);
        if (Arrays.deepEquals(xParams, mParams))
            return true;
        if (target.isVarArgs()) {
            if (xParams.length == mParams.length) {
                if (xParams[xParams.length - 1].isArray()) {
                    return mParams[mParams.length - 1].equals(xParams[xParams.length - 1].getComponentType());
                }
            } else if (xParams.length == mParams.length + 1) {
                return Arrays.deepEquals(Arrays.copyOfRange(xParams, 0, xParams.length - 1), mParams);
            }
        }
        return false;
    }

    public Stream<Method> findInvokable(Method m, Class<?> otherClass) {
        final Map<Class<?>, Class<?>> substitues = Collections.singletonMap(java.util.logging.Level.class, sun.util.logging.PlatformLogger.Level.class);
        return Stream.of(otherClass.getMethods()).filter((x) -> m.getName().equals(x.getName())).filter((x) -> canBeInvokedAs(m, x, substitues));
    }

    StringBuilder testDefaultJULLogger(java.lang.System.Logger julLogger) {
        final StringBuilder errors = new StringBuilder();
        if (!bridgeLoggerClass.isInstance(julLogger)) {
            final String errorMsg = "Logger returned by LoggerFactory.getLogger(\"foo\") is not a " + bridgeLoggerClass + "\n\t" + julLogger;
            System.err.println(errorMsg);
            errors.append(errorMsg).append('\n');
        }
        final Class<? extends java.lang.System.Logger> xClass = julLogger.getClass();
        List<Method> notOverridden = Stream.of(bridgeLoggerClass.getDeclaredMethods()).filter((m) -> {
            try {
                Method x = xClass.getDeclaredMethod(m.getName(), m.getParameterTypes());
                return x == null;
            } catch (NoSuchMethodException ex) {
                return !Modifier.isStatic(m.getModifiers());
            }
        }).collect(Collectors.toList());
        notOverridden.stream().filter((x) -> {
            boolean shouldOverride = true;
            try {
                final Method m = xClass.getMethod(x.getName(), x.getParameterTypes());
                Method m2 = null;
                try {
                    m2 = jdkLoggerClass.getDeclaredMethod(x.getName(), x.getParameterTypes());
                } catch (Exception e) {
                }
                shouldOverride = m.isDefault() || m2 == null;
            } catch (Exception e) {
            }
            return shouldOverride;
        }).forEach(x -> {
            final String errorMsg = xClass.getName() + " should override\n\t" + x.toString();
            System.err.println(errorMsg);
            errors.append(errorMsg).append('\n');
        });
        if (notOverridden.isEmpty()) {
            System.out.println(xClass + " overrides all methods from " + bridgeLoggerClass);
        }
        return errors;
    }

    public static class ResourceBundeParam extends ResourceBundle {

        Map<String, String> map = Collections.synchronizedMap(new LinkedHashMap<>());

        @Override
        protected Object handleGetObject(String key) {
            map.putIfAbsent(key, "${" + key + "}");
            return map.get(key);
        }

        @Override
        public Enumeration<String> getKeys() {
            return Collections.enumeration(new LinkedHashSet<>(map.keySet()));
        }
    }

    final ResourceBundle bundleParam = ResourceBundle.getBundle(ResourceBundeParam.class.getName());

    public static class ResourceBundeLocalized extends ResourceBundle {

        Map<String, String> map = Collections.synchronizedMap(new LinkedHashMap<>());

        @Override
        protected Object handleGetObject(String key) {
            map.putIfAbsent(key, "Localized:${" + key + "}");
            return map.get(key);
        }

        @Override
        public Enumeration<String> getKeys() {
            return Collections.enumeration(new LinkedHashSet<>(map.keySet()));
        }
    }

    final static ResourceBundle bundleLocalized = ResourceBundle.getBundle(ResourceBundeLocalized.class.getName());

    final Map<Class<?>, Object> params = new HashMap<>();

    {
        params.put(String.class, "TestString");
        params.put(sun.util.logging.PlatformLogger.Level.class, sun.util.logging.PlatformLogger.Level.WARNING);
        params.put(java.lang.System.Logger.Level.class, java.lang.System.Logger.Level.WARNING);
        params.put(ResourceBundle.class, bundleParam);
        params.put(Throwable.class, new Throwable("TestThrowable (Please ignore it!)"));
        params.put(Object[].class, new Object[] { "One", "Two" });
        params.put(Object.class, new Object() {

            @Override
            public String toString() {
                return "I am an object!";
            }
        });
    }

    public Object[] getParamsFor(Method m) {
        final Object[] res = new Object[m.getParameterCount()];
        final Class<?>[] sig = m.getParameterTypes();
        if (res.length == 0) {
            return res;
        }
        for (int i = 0; i < res.length; i++) {
            Object p = params.get(sig[i]);
            if (p == null && sig[i].equals(Supplier.class)) {
                final String msg = "SuppliedMsg[" + i + "]";
                p = (Supplier<String>) () -> msg;
            }
            if (p instanceof String) {
                res[i] = String.valueOf(p) + "[" + i + "]";
            } else {
                res[i] = p;
            }
        }
        return res;
    }

    public void invokeOn(java.lang.System.Logger logger, Method m) {
        Object[] p = getParamsFor(m);
        try {
            m.invoke(logger, p);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke " + m.toString(), e);
        }
    }

    public void testAllJdkExtensionMethods(java.lang.System.Logger logger) {
        Stream.of(jdkLoggerClass.getDeclaredMethods()).filter(m -> !Modifier.isStatic(m.getModifiers())).forEach((m) -> invokeOn(logger, m));
    }

    public void testAllAPIMethods(java.lang.System.Logger logger) {
        Stream.of(spiLoggerClass.getDeclaredMethods()).filter(m -> !Modifier.isStatic(m.getModifiers())).forEach((m) -> invokeOn(logger, m));
    }

    public void testAllBridgeMethods(java.lang.System.Logger logger) {
        Stream.of(bridgeLoggerClass.getDeclaredMethods()).filter(m -> !Modifier.isStatic(m.getModifiers())).forEach((m) -> invokeOn(logger, m));
    }

    public void testAllLogProducerMethods(java.lang.System.Logger logger) {
        Stream.of(julLogProducerClass.getDeclaredMethods()).filter(m -> !Modifier.isStatic(m.getModifiers())).forEach((m) -> invokeOn(logger, m));
    }

    public StringBuilder testGetLoggerOverriddenOnSpi() {
        final StringBuilder errors = new StringBuilder();
        Stream.of(jdkLoggerClass.getDeclaredMethods()).filter(m -> Modifier.isStatic(m.getModifiers())).filter(m -> Modifier.isPublic(m.getModifiers())).filter(m -> !m.getName().equals("getLoggerFinder")).filter(m -> {
            try {
                final Method x = bridgeLoggerClass.getDeclaredMethod(m.getName(), m.getParameterTypes());
                return x == null;
            } catch (NoSuchMethodException ex) {
                return true;
            }
        }).forEach(m -> {
            final String errorMsg = bridgeLoggerClass.getName() + " should override\n\t" + m.toString();
            System.err.println(errorMsg);
            errors.append(errorMsg).append('\n');
        });
        if (errors.length() == 0) {
            System.out.println(bridgeLoggerClass + " overrides all static methods from " + jdkLoggerClass);
        } else {
            if (errors.length() > 0)
                throw new RuntimeException(errors.toString());
        }
        return errors;
    }

    public static void main(String[] argv) throws Exception {
        final LoggerFinderAPITest test = new LoggerFinderAPITest(false);
        final StringBuilder errors = new StringBuilder();
        errors.append(test.testGetLoggerOverriddenOnSpi());
        java.lang.System.Logger julLogger = java.lang.System.LoggerFinder.getLoggerFinder().getLogger("foo", LoggerFinderAPITest.class);
        errors.append(test.testDefaultJULLogger(julLogger));
        if (errors.length() > 0)
            throw new RuntimeException(errors.toString());
        java.lang.System.Logger julSystemLogger = java.lang.System.LoggerFinder.getLoggerFinder().getLogger("bar", Thread.class);
        errors.append(test.testDefaultJULLogger(julSystemLogger));
        if (errors.length() > 0)
            throw new RuntimeException(errors.toString());
        java.lang.System.Logger julLocalizedLogger = (java.lang.System.Logger) System.getLogger("baz", bundleLocalized);
        java.lang.System.Logger julLocalizedSystemLogger = java.lang.System.LoggerFinder.getLoggerFinder().getLocalizedLogger("oof", bundleLocalized, Thread.class);
        final String error = errors.toString();
        if (!error.isEmpty())
            throw new RuntimeException(error);
        for (java.lang.System.Logger logger : new java.lang.System.Logger[] { julLogger, julSystemLogger, julLocalizedLogger, julLocalizedSystemLogger }) {
            test.testAllJdkExtensionMethods(logger);
            test.testAllAPIMethods(logger);
            test.testAllBridgeMethods(logger);
            test.testAllLogProducerMethods(logger);
        }
    }
}
