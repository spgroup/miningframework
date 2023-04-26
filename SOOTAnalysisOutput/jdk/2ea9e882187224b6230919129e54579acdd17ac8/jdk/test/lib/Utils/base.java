package jdk.test.lib;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jdk.internal.misc.Unsafe;
import static jdk.test.lib.Asserts.assertTrue;
import jdk.test.lib.process.ProcessTools;
import jdk.test.lib.process.OutputAnalyzer;

public final class Utils {

    public static final String TEST_CLASS_PATH = System.getProperty("test.class.path", ".");

    public static final String NEW_LINE = System.getProperty("line.separator");

    public static final String VM_OPTIONS = System.getProperty("test.vm.opts", "").trim();

    public static final String JAVA_OPTIONS = System.getProperty("test.java.opts", "").trim();

    public static final String TEST_SRC = System.getProperty("test.src", "").trim();

    private static Unsafe unsafe = null;

    public static final String SEED_PROPERTY_NAME = "jdk.test.lib.random.seed";

    private static volatile Random RANDOM_GENERATOR;

    public static final long SEED = Long.getLong(SEED_PROPERTY_NAME, new Random().nextLong());

    public static final double TIMEOUT_FACTOR;

    static {
        String toFactor = System.getProperty("test.timeout.factor", "1.0");
        TIMEOUT_FACTOR = Double.parseDouble(toFactor);
    }

    public static final long DEFAULT_TEST_TIMEOUT = TimeUnit.SECONDS.toMillis(120);

    private Utils() {
    }

    public static List<String> getVmOptions() {
        return Arrays.asList(safeSplitString(VM_OPTIONS));
    }

    public static List<String> getForwardVmOptions() {
        String[] opts = safeSplitString(VM_OPTIONS);
        for (int i = 0; i < opts.length; i++) {
            opts[i] = "-J" + opts[i];
        }
        return Arrays.asList(opts);
    }

    public static String[] getTestJavaOpts() {
        List<String> opts = new ArrayList<String>();
        Collections.addAll(opts, safeSplitString(VM_OPTIONS));
        Collections.addAll(opts, safeSplitString(JAVA_OPTIONS));
        return opts.toArray(new String[0]);
    }

    public static String[] addTestJavaOpts(String... userArgs) {
        List<String> opts = new ArrayList<String>();
        Collections.addAll(opts, getTestJavaOpts());
        Collections.addAll(opts, userArgs);
        return opts.toArray(new String[0]);
    }

    private static final Pattern useGcPattern = Pattern.compile("(?:\\-XX\\:[\\+\\-]Use.+GC)" + "|(?:\\-Xconcgc)");

    public static List<String> removeGcOpts(List<String> opts) {
        List<String> optsWithoutGC = new ArrayList<String>();
        for (String opt : opts) {
            if (useGcPattern.matcher(opt).matches()) {
                System.out.println("removeGcOpts: removed " + opt);
            } else {
                optsWithoutGC.add(opt);
            }
        }
        return optsWithoutGC;
    }

    public static String[] getFilteredTestJavaOpts(String... filters) {
        String[] options = getTestJavaOpts();
        if (filters.length == 0) {
            return options;
        }
        List<String> filteredOptions = new ArrayList<String>(options.length);
        Pattern[] patterns = new Pattern[filters.length];
        for (int i = 0; i < filters.length; i++) {
            patterns[i] = Pattern.compile(filters[i]);
        }
        for (String option : options) {
            boolean matched = false;
            for (int i = 0; i < patterns.length && !matched; i++) {
                Matcher matcher = patterns[i].matcher(option);
                matched = matcher.find();
            }
            if (!matched) {
                filteredOptions.add(option);
            }
        }
        return filteredOptions.toArray(new String[filteredOptions.size()]);
    }

    private static String[] safeSplitString(String s) {
        if (s == null || s.trim().isEmpty()) {
            return new String[] {};
        }
        return s.trim().split("\\s+");
    }

    public static String getCommandLine(ProcessBuilder pb) {
        StringBuilder cmd = new StringBuilder();
        for (String s : pb.command()) {
            cmd.append(s).append(" ");
        }
        return cmd.toString();
    }

    public static int getFreePort() throws InterruptedException, IOException {
        int port = -1;
        while (port <= 0) {
            Thread.sleep(100);
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(0);
                port = serverSocket.getLocalPort();
            } finally {
                serverSocket.close();
            }
        }
        return port;
    }

    public static String getHostname() throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getLocalHost();
        String hostName = inetAddress.getHostName();
        assertTrue((hostName != null && !hostName.isEmpty()), "Cannot get hostname");
        return hostName;
    }

    public static int waitForJvmPid(String key) throws Throwable {
        final long iterationSleepMillis = 250;
        System.out.println("waitForJvmPid: Waiting for key '" + key + "'");
        System.out.flush();
        while (true) {
            int pid = tryFindJvmPid(key);
            if (pid >= 0) {
                return pid;
            }
            Thread.sleep(iterationSleepMillis);
        }
    }

    public static int tryFindJvmPid(String key) throws Throwable {
        OutputAnalyzer output = null;
        try {
            JDKToolLauncher jcmdLauncher = JDKToolLauncher.create("jcmd");
            jcmdLauncher.addToolArg("-l");
            output = ProcessTools.executeProcess(jcmdLauncher.getCommand());
            output.shouldHaveExitValue(0);
            Pattern pattern = Pattern.compile("([0-9]+)\\s.*(" + key + ").*\\r?\\n");
            Matcher matcher = pattern.matcher(output.getStdout());
            int pid = -1;
            if (matcher.find()) {
                pid = Integer.parseInt(matcher.group(1));
                System.out.println("findJvmPid.pid: " + pid);
                if (matcher.find()) {
                    throw new Exception("Found multiple JVM pids for key: " + key);
                }
            }
            return pid;
        } catch (Throwable t) {
            System.out.println(String.format("Utils.findJvmPid(%s) failed: %s", key, t));
            throw t;
        }
    }

    public static long adjustTimeout(long tOut) {
        return Math.round(tOut * Utils.TIMEOUT_FACTOR);
    }

    public static String fileAsString(String filename) throws IOException {
        Path filePath = Paths.get(filename);
        if (!Files.exists(filePath))
            return null;
        return new String(Files.readAllBytes(filePath));
    }

    public static synchronized Unsafe getUnsafe() {
        if (unsafe == null) {
            try {
                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                unsafe = (Unsafe) f.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Unable to get Unsafe instance.", e);
            }
        }
        return unsafe;
    }

    private static final char[] hexArray = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    public static String toHexString(byte[] bytes) {
        char[] hexView = new char[bytes.length * 3];
        int i = 0;
        for (byte b : bytes) {
            hexView[i++] = hexArray[(b >> 4) & 0x0F];
            hexView[i++] = hexArray[b & 0x0F];
            hexView[i++] = ' ';
        }
        return new String(hexView);
    }

    public static Random getRandomInstance() {
        if (RANDOM_GENERATOR == null) {
            synchronized (Utils.class) {
                if (RANDOM_GENERATOR == null) {
                    RANDOM_GENERATOR = new Random(SEED);
                    System.out.printf("For random generator using seed: %d%n", SEED);
                    System.out.printf("To re-run test with same seed value please add \"-D%s=%d\" to command line.%n", SEED_PROPERTY_NAME, SEED);
                }
            }
        }
        return RANDOM_GENERATOR;
    }

    public static <T> T getRandomElement(Collection<T> collection) throws IllegalArgumentException {
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("Empty collection");
        }
        Random random = getRandomInstance();
        int elementIndex = 1 + random.nextInt(collection.size() - 1);
        Iterator<T> iterator = collection.iterator();
        while (--elementIndex != 0) {
            iterator.next();
        }
        return iterator.next();
    }

    public static <T> T getRandomElement(T[] array) throws IllegalArgumentException {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Empty or null array");
        }
        Random random = getRandomInstance();
        return array[random.nextInt(array.length)];
    }

    public static final void waitForCondition(BooleanSupplier condition) {
        waitForCondition(condition, -1L, 100L);
    }

    public static final boolean waitForCondition(BooleanSupplier condition, long timeout) {
        return waitForCondition(condition, timeout, 100L);
    }

    public static final boolean waitForCondition(BooleanSupplier condition, long timeout, long sleepTime) {
        long startTime = System.currentTimeMillis();
        while (!(condition.getAsBoolean() || (timeout != -1L && ((System.currentTimeMillis() - startTime) > timeout)))) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new Error(e);
            }
        }
        return condition.getAsBoolean();
    }

    public static interface ThrowingRunnable {

        void run() throws Throwable;
    }

    public static Throwable filterException(ThrowingRunnable test, Function<Throwable, Boolean> filter) throws Throwable {
        try {
            test.run();
        } catch (Throwable t) {
            if (filter.apply(t)) {
                return t;
            } else {
                throw t;
            }
        }
        return null;
    }

    public static void ensureClassIsLoaded(Class<?> aClass) {
        if (aClass == null) {
            throw new Error("Requested null class");
        }
        try {
            Class.forName(aClass.getName(), true, ClassLoader.getSystemClassLoader());
        } catch (ClassNotFoundException e) {
            throw new Error("Class not found", e);
        }
    }

    public static URLClassLoader getTestClassPathURLClassLoader(ClassLoader parent) {
        URL[] urls = Arrays.stream(TEST_CLASS_PATH.split(File.pathSeparator)).map(Paths::get).map(Path::toUri).map(x -> {
            try {
                return x.toURL();
            } catch (MalformedURLException ex) {
                throw new Error("Test issue. JTREG property" + " 'test.class.path'" + " is not defined correctly", ex);
            }
        }).toArray(URL[]::new);
        return new URLClassLoader(urls, parent);
    }

    public static void runAndCheckException(Runnable runnable, Class<? extends Throwable> expectedException) {
        runAndCheckException(runnable, t -> {
            if (t == null) {
                if (expectedException != null) {
                    throw new AssertionError("Didn't get expected exception " + expectedException.getSimpleName());
                }
            } else {
                String message = "Got unexpected exception " + t.getClass().getSimpleName();
                if (expectedException == null) {
                    throw new AssertionError(message, t);
                } else if (!expectedException.isAssignableFrom(t.getClass())) {
                    message += " instead of " + expectedException.getSimpleName();
                    throw new AssertionError(message, t);
                }
            }
        });
    }

    public static void runAndCheckException(Runnable runnable, Consumer<Throwable> checkException) {
        try {
            runnable.run();
            checkException.accept(null);
        } catch (Throwable t) {
            checkException.accept(t);
        }
    }

    public static String toJVMTypeSignature(Class<?> type) {
        if (type.isPrimitive()) {
            if (type == boolean.class) {
                return "Z";
            } else if (type == byte.class) {
                return "B";
            } else if (type == char.class) {
                return "C";
            } else if (type == double.class) {
                return "D";
            } else if (type == float.class) {
                return "F";
            } else if (type == int.class) {
                return "I";
            } else if (type == long.class) {
                return "J";
            } else if (type == short.class) {
                return "S";
            } else if (type == void.class) {
                return "V";
            } else {
                throw new Error("Unsupported type: " + type);
            }
        }
        String result = type.getName().replaceAll("\\.", "/");
        if (!type.isArray()) {
            return "L" + result + ";";
        }
        return result;
    }

    public static Object[] getNullValues(Class<?>... types) {
        Object[] result = new Object[types.length];
        int i = 0;
        for (Class<?> type : types) {
            result[i++] = NULL_VALUES.get(type);
        }
        return result;
    }

    private static Map<Class<?>, Object> NULL_VALUES = new HashMap<>();

    static {
        NULL_VALUES.put(boolean.class, false);
        NULL_VALUES.put(byte.class, (byte) 0);
        NULL_VALUES.put(short.class, (short) 0);
        NULL_VALUES.put(char.class, '\0');
        NULL_VALUES.put(int.class, 0);
        NULL_VALUES.put(long.class, 0L);
        NULL_VALUES.put(float.class, 0.0f);
        NULL_VALUES.put(double.class, 0.0d);
    }

    public static String getMandatoryProperty(String propName) {
        Objects.requireNonNull(propName, "Requested null property");
        String prop = System.getProperty(propName);
        Objects.requireNonNull(prop, String.format("A mandatory property '%s' isn't set", propName));
        return prop;
    }
}
