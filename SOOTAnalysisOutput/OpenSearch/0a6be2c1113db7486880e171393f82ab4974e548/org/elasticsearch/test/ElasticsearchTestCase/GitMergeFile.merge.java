package org.elasticsearch.test;

import com.carrotsearch.randomizedtesting.LifecycleScope;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.*;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import org.apache.lucene.store.MockDirectoryWrapper;
import org.apache.lucene.util.AbstractRandomizedTest;
import org.apache.lucene.util.TimeUnits;
import org.apache.lucene.uninverting.UninvertingReader;
import org.elasticsearch.Version;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.routing.DjbHashFunction;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.EsAbortPolicy;
import org.elasticsearch.common.util.concurrent.EsRejectedExecutionException;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.test.cache.recycler.MockBigArrays;
import org.elasticsearch.test.cache.recycler.MockPageCacheRecycler;
import org.elasticsearch.test.junit.listeners.LoggingListener;
import org.elasticsearch.test.search.MockSearchService;
import org.elasticsearch.test.store.MockDirectoryHelper;
import org.elasticsearch.threadpool.ThreadPool;
import org.junit.*;
import java.io.Closeable;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAllFilesClosed;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAllSearchersClosed;

@ThreadLeakScope(Scope.SUITE)
@ThreadLeakLingering(linger = 5000)
@TimeoutSuite(millis = 20 * TimeUnits.MINUTE)
@Listeners(LoggingListener.class)
public abstract class ElasticsearchTestCase extends AbstractRandomizedTest {

    private static Thread.UncaughtExceptionHandler defaultHandler;

    protected final ESLogger logger = Loggers.getLogger(getClass());

    public static final String TESTS_SECURITY_MANAGER = System.getProperty("tests.security.manager");

    public static final String JAVA_SECURTY_POLICY = System.getProperty("java.security.policy");

    private static final String TESTS_COMPATIBILITY = "tests.compatibility";

    private static final Version GLOABL_COMPATIBILITY_VERSION = Version.fromString(compatibilityVersionProperty());

    public static final boolean ASSERTIONS_ENABLED;

    static {
        boolean enabled = false;
        assert enabled = true;
        ASSERTIONS_ENABLED = enabled;
        if (Boolean.parseBoolean(Strings.hasLength(TESTS_SECURITY_MANAGER) ? TESTS_SECURITY_MANAGER : "true") && JAVA_SECURTY_POLICY != null) {
            System.setSecurityManager(new SecurityManager());
        }
    }

    @After
    public void ensureNoFieldCacheUse() {
        String[] entries = UninvertingReader.getUninvertedStats();
        assertEquals("fieldcache must never be used, got=" + Arrays.toString(entries), 0, entries.length);
    }

    public static void assertBusy(Runnable codeBlock) throws Exception {
        assertBusy(Executors.callable(codeBlock), 10, TimeUnit.SECONDS);
    }

    public static void assertBusy(Runnable codeBlock, long maxWaitTime, TimeUnit unit) throws Exception {
        assertBusy(Executors.callable(codeBlock), maxWaitTime, unit);
    }

    public static <V> V assertBusy(Callable<V> codeBlock) throws Exception {
        return assertBusy(codeBlock, 10, TimeUnit.SECONDS);
    }

    public static <V> V assertBusy(Callable<V> codeBlock, long maxWaitTime, TimeUnit unit) throws Exception {
        long maxTimeInMillis = TimeUnit.MILLISECONDS.convert(maxWaitTime, unit);
        long iterations = Math.max(Math.round(Math.log10(maxTimeInMillis) / Math.log10(2)), 1);
        long timeInMillis = 1;
        long sum = 0;
        List<AssertionError> failures = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            try {
                return codeBlock.call();
            } catch (AssertionError e) {
                failures.add(e);
            }
            sum += timeInMillis;
            Thread.sleep(timeInMillis);
            timeInMillis *= 2;
        }
        timeInMillis = maxTimeInMillis - sum;
        Thread.sleep(Math.max(timeInMillis, 0));
        try {
            return codeBlock.call();
        } catch (AssertionError e) {
            for (AssertionError failure : failures) {
                e.addSuppressed(failure);
            }
            throw e;
        }
    }

    public static boolean awaitBusy(Predicate<?> breakPredicate) throws InterruptedException {
        return awaitBusy(breakPredicate, 10, TimeUnit.SECONDS);
    }

    public static boolean awaitBusy(Predicate<?> breakPredicate, long maxWaitTime, TimeUnit unit) throws InterruptedException {
        long maxTimeInMillis = TimeUnit.MILLISECONDS.convert(maxWaitTime, unit);
        long iterations = Math.max(Math.round(Math.log10(maxTimeInMillis) / Math.log10(2)), 1);
        long timeInMillis = 1;
        long sum = 0;
        for (int i = 0; i < iterations; i++) {
            if (breakPredicate.apply(null)) {
                return true;
            }
            sum += timeInMillis;
            Thread.sleep(timeInMillis);
            timeInMillis *= 2;
        }
        timeInMillis = maxTimeInMillis - sum;
        Thread.sleep(Math.max(timeInMillis, 0));
        return breakPredicate.apply(null);
    }

    private static final String[] numericTypes = new String[] { "byte", "short", "integer", "long" };

    public static String randomNumericType(Random random) {
        return numericTypes[random.nextInt(numericTypes.length)];
    }

    public Path getResourcePath(String relativePath) {
        URI uri = URI.create(getClass().getResource(relativePath).toString());
        return Paths.get(uri);
    }

    @After
    public void ensureAllPagesReleased() throws Exception {
        MockPageCacheRecycler.ensureAllPagesAreReleased();
    }

    @After
    public void ensureAllArraysReleased() throws Exception {
        MockBigArrays.ensureAllArraysAreReleased();
    }

    @After
    public void ensureAllSearchContextsReleased() throws Exception {
        assertBusy(new Runnable() {

            @Override
            public void run() {
                MockSearchService.assertNoInFLightContext();
            }
        });
    }

    public static boolean hasUnclosedWrapper() {
        for (MockDirectoryWrapper w : MockDirectoryHelper.wrappers) {
            if (w.isOpen()) {
                return true;
            }
        }
        return false;
    }

    @BeforeClass
    public static void setBeforeClass() throws Exception {
        closeAfterSuite(new Closeable() {

            @Override
            public void close() throws IOException {
                assertAllFilesClosed();
            }
        });
        closeAfterSuite(new Closeable() {

            @Override
            public void close() throws IOException {
                assertAllSearchersClosed();
            }
        });
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new ElasticsearchUncaughtExceptionHandler(defaultHandler));
        Requests.CONTENT_TYPE = randomXContentType();
        Requests.INDEX_CONTENT_TYPE = randomXContentType();
    }

    public static XContentType randomXContentType() {
        return randomFrom(XContentType.values());
    }

    @AfterClass
    public static void resetAfterClass() {
        Thread.setDefaultUncaughtExceptionHandler(defaultHandler);
        Requests.CONTENT_TYPE = XContentType.SMILE;
        Requests.INDEX_CONTENT_TYPE = XContentType.JSON;
    }

    public static boolean maybeDocValues() {
        return randomBoolean();
    }

    private static final List<Version> SORTED_VERSIONS;

    static {
        Field[] declaredFields = Version.class.getDeclaredFields();
        Set<Integer> ids = new HashSet<>();
        for (Field field : declaredFields) {
            final int mod = field.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isFinal(mod) && Modifier.isPublic(mod)) {
                if (field.getType() == Version.class) {
                    try {
                        Version object = (Version) field.get(null);
                        ids.add(object.id);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        List<Integer> idList = new ArrayList<>(ids);
        Collections.sort(idList);
        Collections.reverse(idList);
        ImmutableList.Builder<Version> version = ImmutableList.builder();
        for (Integer integer : idList) {
            version.add(Version.fromId(integer));
        }
        SORTED_VERSIONS = version.build();
    }

    public static Version getPreviousVersion() {
        Version version = SORTED_VERSIONS.get(1);
        assert version.before(Version.CURRENT);
        return version;
    }

    public static Version randomVersion() {
        return randomVersion(getRandom());
    }

    public static Version randomVersion(Random random) {
        return SORTED_VERSIONS.get(random.nextInt(SORTED_VERSIONS.size()));
    }

    public static List<Version> allVersions() {
        return Collections.unmodifiableList(SORTED_VERSIONS);
    }

    public static Version randomVersionBetween(Version minVersion, Version maxVersion) {
        return randomVersionBetween(getRandom(), minVersion, maxVersion);
    }

    public static Version randomVersionBetween(Random random, Version minVersion, Version maxVersion) {
        int minVersionIndex = SORTED_VERSIONS.size();
        if (minVersion != null) {
            minVersionIndex = SORTED_VERSIONS.indexOf(minVersion);
        }
        int maxVersionIndex = 0;
        if (maxVersion != null) {
            maxVersionIndex = SORTED_VERSIONS.indexOf(maxVersion);
        }
        if (minVersionIndex == -1) {
            throw new IllegalArgumentException("minVersion [" + minVersion + "] does not exist.");
        } else if (maxVersionIndex == -1) {
            throw new IllegalArgumentException("maxVersion [" + maxVersion + "] does not exist.");
        } else {
            int range = minVersionIndex + 1 - maxVersionIndex;
            return SORTED_VERSIONS.get(maxVersionIndex + random.nextInt(range));
        }
    }

    public static ImmutableSettings.Builder settings(Version version) {
        ImmutableSettings.Builder builder = ImmutableSettings.builder().put(IndexMetaData.SETTING_VERSION_CREATED, version);
        if (version.before(Version.V_2_0_0)) {
            builder.put(IndexMetaData.SETTING_LEGACY_ROUTING_HASH_FUNCTION, DjbHashFunction.class);
        }
        return builder;
    }

    static final class ElasticsearchUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        private final Thread.UncaughtExceptionHandler parent;

        private final ESLogger logger = Loggers.getLogger(getClass());

        private ElasticsearchUncaughtExceptionHandler(Thread.UncaughtExceptionHandler parent) {
            this.parent = parent;
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            if (e instanceof EsRejectedExecutionException) {
                if (e.getMessage().contains(EsAbortPolicy.SHUTTING_DOWN_KEY)) {
                    return;
                }
            } else if (e instanceof OutOfMemoryError) {
                if (e.getMessage().contains("unable to create new native thread")) {
                    printStackDump(logger);
                }
            }
            parent.uncaughtException(t, e);
        }
    }

    protected static final void printStackDump(ESLogger logger) {
        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        logger.error(formatThreadStacks(allStackTraces));
    }

    private static String formatThreadStacks(Map<Thread, StackTraceElement[]> threads) {
        StringBuilder message = new StringBuilder();
        int cnt = 1;
        final Formatter f = new Formatter(message, Locale.ENGLISH);
        for (Map.Entry<Thread, StackTraceElement[]> e : threads.entrySet()) {
            if (e.getKey().isAlive())
                f.format(Locale.ENGLISH, "\n  %2d) %s", cnt++, threadName(e.getKey())).flush();
            if (e.getValue().length == 0) {
                message.append("\n        at (empty stack)");
            } else {
                for (StackTraceElement ste : e.getValue()) {
                    message.append("\n        at ").append(ste);
                }
            }
        }
        return message.toString();
    }

    private static String threadName(Thread t) {
        return "Thread[" + "id=" + t.getId() + ", name=" + t.getName() + ", state=" + t.getState() + ", group=" + groupName(t.getThreadGroup()) + "]";
    }

    private static String groupName(ThreadGroup threadGroup) {
        if (threadGroup == null) {
            return "{null group}";
        } else {
            return threadGroup.getName();
        }
    }

    public static <T> T randomFrom(T... values) {
        return RandomizedTest.randomFrom(values);
    }

    public static String[] generateRandomStringArray(int maxArraySize, int maxStringSize, boolean allowNull) {
        if (allowNull && randomBoolean()) {
            return null;
        }
        String[] array = new String[randomInt(maxArraySize)];
        for (int i = 0; i < array.length; i++) {
            array[i] = randomAsciiOfLength(maxStringSize);
        }
        return array;
    }

    public static String[] generateRandomStringArray(int maxArraySize, int maxStringSize) {
        return generateRandomStringArray(maxArraySize, maxStringSize, false);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE })
    @Ignore
    public @interface CompatibilityVersion {

        int version();
    }

    public static Version globalCompatibilityVersion() {
        return GLOABL_COMPATIBILITY_VERSION;
    }

    public Version compatibilityVersion() {
        return compatibilityVersion(getClass());
    }

    private Version compatibilityVersion(Class<?> clazz) {
        if (clazz == Object.class || clazz == ElasticsearchIntegrationTest.class) {
            return globalCompatibilityVersion();
        }
        CompatibilityVersion annotation = clazz.getAnnotation(CompatibilityVersion.class);
        if (annotation != null) {
            return Version.smallest(Version.fromId(annotation.version()), compatibilityVersion(clazz.getSuperclass()));
        }
        return compatibilityVersion(clazz.getSuperclass());
    }

    private static String compatibilityVersionProperty() {
        final String version = System.getProperty(TESTS_COMPATIBILITY);
        if (Strings.hasLength(version)) {
            return version;
        }
        return System.getProperty(TESTS_BACKWARDS_COMPATIBILITY_VERSION);
    }

    public static boolean terminate(ExecutorService... services) throws InterruptedException {
        boolean terminated = true;
        for (ExecutorService service : services) {
            if (service != null) {
                terminated &= ThreadPool.terminate(service, 10, TimeUnit.SECONDS);
            }
        }
        return terminated;
    }

    public static boolean terminate(ThreadPool service) throws InterruptedException {
        return ThreadPool.terminate(service, 10, TimeUnit.SECONDS);
    }

    public Path newTempFilePath() {
        return newTempFile().toPath();
    }

    public Path newTempDirPath() {
        return newTempDir().toPath();
    }

    public static Path newTempDirPath(LifecycleScope scope) {
        return newTempDir(scope).toPath();
    }

    public String[] tmpPaths() {
        final int numPaths = randomIntBetween(1, 3);
        final String[] absPaths = new String[numPaths];
        for (int i = 0; i < numPaths; i++) {
            absPaths[i] = newTempDirPath().toAbsolutePath().toString();
        }
        return absPaths;
    }

    public NodeEnvironment newNodeEnvironment() throws IOException {
        return newNodeEnvironment(ImmutableSettings.EMPTY);
    }

    public NodeEnvironment newNodeEnvironment(Settings settings) throws IOException {
        Settings build = ImmutableSettings.builder().put(settings).put("path.home", newTempDirPath().toAbsolutePath()).putArray("path.data", tmpPaths()).build();
        return new NodeEnvironment(build, new Environment(build));
    }
}
