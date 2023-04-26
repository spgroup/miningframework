package org.elasticsearch.test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Listeners;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakLingering;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;
import com.carrotsearch.randomizedtesting.annotations.TimeoutSuite;
import com.carrotsearch.randomizedtesting.generators.CodepointSetGenerator;
import com.carrotsearch.randomizedtesting.generators.RandomNumbers;
import com.carrotsearch.randomizedtesting.generators.RandomPicks;
import com.carrotsearch.randomizedtesting.generators.RandomStrings;
import com.carrotsearch.randomizedtesting.rules.TestRuleAdapter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.status.StatusConsoleListener;
import org.apache.logging.log4j.status.StatusData;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util.LuceneTestCase.SuppressCodecs;
import org.apache.lucene.util.TestRuleMarkFailure;
import org.apache.lucene.util.TestUtil;
import org.apache.lucene.util.TimeUnits;
import org.elasticsearch.Version;
import org.elasticsearch.bootstrap.BootstrapForTesting;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.ClusterModule;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.CheckedBiFunction;
import org.elasticsearch.common.CheckedRunnable;
import org.elasticsearch.common.SuppressForbidden;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.common.io.PathUtilsForTesting;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.io.stream.NamedWriteable;
import org.elasticsearch.common.io.stream.NamedWriteableAwareStreamInput;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.Streamable;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.logging.DeprecationLogger;
import org.elasticsearch.common.logging.LogConfigurator;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.util.MockBigArrays;
import org.elasticsearch.common.util.MockPageCacheRecycler;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentParser.Token;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.env.TestEnvironment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AnalysisRegistry;
import org.elasticsearch.index.analysis.CharFilterFactory;
import org.elasticsearch.index.analysis.IndexAnalyzers;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MetadataFieldMapper;
import org.elasticsearch.indices.IndicesModule;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.MapperPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.ScriptPlugin;
import org.elasticsearch.script.MockScriptEngine;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.script.ScriptEngine;
import org.elasticsearch.script.ScriptModule;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.MockSearchService;
import org.elasticsearch.test.junit.listeners.LoggingListener;
import org.elasticsearch.test.junit.listeners.ReproduceInfoPrinter;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.MockTcpTransportPlugin;
import org.elasticsearch.transport.nio.MockNioTransportPlugin;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.RuleChain;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.elasticsearch.common.util.CollectionUtils.arrayAsArrayList;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

@Listeners({ ReproduceInfoPrinter.class, LoggingListener.class })
@ThreadLeakScope(Scope.SUITE)
@ThreadLeakLingering(linger = 5000)
@TimeoutSuite(millis = 20 * TimeUnits.MINUTE)
@LuceneTestCase.SuppressSysoutChecks(bugUrl = "we log a lot on purpose")
@SuppressCodecs({ "SimpleText", "Memory", "CheapBastard", "Direct", "Compressing", "FST50", "FSTOrd50", "TestBloomFilteredLucenePostings", "MockRandom", "BlockTreeOrds", "LuceneFixedGap", "LuceneVarGapFixedInterval", "LuceneVarGapDocFreqInterval", "Lucene50" })
@LuceneTestCase.SuppressReproduceLine
public abstract class ESTestCase extends LuceneTestCase {

    private static final List<String> JODA_TIMEZONE_IDS;

    private static final List<String> JAVA_TIMEZONE_IDS;

    private static final List<String> JAVA_ZONE_IDS;

    private static final AtomicInteger portGenerator = new AtomicInteger();

    private static final Collection<String> nettyLoggedLeaks = new ArrayList<>();

    @AfterClass
    public static void resetPortCounter() {
        portGenerator.set(0);
    }

    static {
        setTestSysProps();
        LogConfigurator.loadLog4jPlugins();
        String leakLoggerName = "io.netty.util.ResourceLeakDetector";
        Logger leakLogger = LogManager.getLogger(leakLoggerName);
        Appender leakAppender = new AbstractAppender(leakLoggerName, null, PatternLayout.newBuilder().withPattern("%m").build()) {

            @Override
            public void append(LogEvent event) {
                String message = event.getMessage().getFormattedMessage();
                if (Level.ERROR.equals(event.getLevel()) && message.contains("LEAK:")) {
                    synchronized (nettyLoggedLeaks) {
                        nettyLoggedLeaks.add(message);
                    }
                }
            }
        };
        leakAppender.start();
        Loggers.addAppender(leakLogger, leakAppender);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            leakAppender.stop();
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            Configurator.shutdown(context);
        }));
        BootstrapForTesting.ensureInitialized();
        List<String> jodaTZIds = new ArrayList<>(DateTimeZone.getAvailableIDs());
        Collections.sort(jodaTZIds);
        JODA_TIMEZONE_IDS = Collections.unmodifiableList(jodaTZIds);
        List<String> javaTZIds = Arrays.asList(TimeZone.getAvailableIDs());
        Collections.sort(javaTZIds);
        JAVA_TIMEZONE_IDS = Collections.unmodifiableList(javaTZIds);
        List<String> javaZoneIds = new ArrayList<>(ZoneId.getAvailableZoneIds());
        Collections.sort(javaZoneIds);
        JAVA_ZONE_IDS = Collections.unmodifiableList(javaZoneIds);
    }

    @SuppressForbidden(reason = "force log4j and netty sysprops")
    private static void setTestSysProps() {
        System.setProperty("log4j.shutdownHookEnabled", "false");
        System.setProperty("log4j2.disable.jmx", "true");
        System.setProperty("io.netty.leakDetection.level", "paranoid");
    }

    protected final Logger logger = Loggers.getLogger(getClass());

    protected final DeprecationLogger deprecationLogger = new DeprecationLogger(logger);

    private ThreadContext threadContext;

    @Rule
    public RuleChain failureAndSuccessEvents = RuleChain.outerRule(new TestRuleAdapter() {

        @Override
        protected void afterIfSuccessful() throws Throwable {
            ESTestCase.this.afterIfSuccessful();
        }

        @Override
        protected void afterAlways(List<Throwable> errors) throws Throwable {
            if (errors != null && errors.isEmpty() == false) {
                boolean allAssumption = true;
                for (Throwable error : errors) {
                    if (false == error instanceof AssumptionViolatedException) {
                        allAssumption = false;
                        break;
                    }
                }
                if (false == allAssumption) {
                    ESTestCase.this.afterIfFailed(errors);
                }
            }
            super.afterAlways(errors);
        }
    });

    public static TransportAddress buildNewFakeTransportAddress() {
        return new TransportAddress(TransportAddress.META_ADDRESS, portGenerator.incrementAndGet());
    }

    protected void afterIfFailed(List<Throwable> errors) {
    }

    protected void afterIfSuccessful() throws Exception {
    }

    @BeforeClass
    public static void setFileSystem() throws Exception {
        PathUtilsForTesting.setup();
    }

    @AfterClass
    public static void restoreFileSystem() throws Exception {
        PathUtilsForTesting.teardown();
    }

    @BeforeClass
    public static void setContentType() throws Exception {
        Requests.CONTENT_TYPE = randomFrom(XContentType.values());
        Requests.INDEX_CONTENT_TYPE = randomFrom(XContentType.values());
    }

    @AfterClass
    public static void restoreContentType() {
        Requests.CONTENT_TYPE = XContentType.SMILE;
        Requests.INDEX_CONTENT_TYPE = XContentType.JSON;
    }

    @Before
    public final void before() {
        logger.info("{}before test", getTestParamsForLogging());
        assertNull("Thread context initialized twice", threadContext);
        if (enableWarningsCheck()) {
            this.threadContext = new ThreadContext(Settings.EMPTY);
            DeprecationLogger.setThreadContext(threadContext);
        }
    }

    protected boolean enableWarningsCheck() {
        return true;
    }

    @After
    public final void after() throws Exception {
        checkStaticState(false);
        if (threadContext != null) {
            ensureNoWarnings();
            assert threadContext == null;
        }
        ensureAllSearchContextsReleased();
        ensureCheckIndexPassed();
        logger.info("{}after test", getTestParamsForLogging());
    }

    private String getTestParamsForLogging() {
        String name = getTestName();
        int start = name.indexOf('{');
        if (start < 0)
            return "";
        int end = name.lastIndexOf('}');
        if (end < 0)
            return "";
        return "[" + name.substring(start + 1, end) + "] ";
    }

    private void ensureNoWarnings() throws IOException {
        try {
            final List<String> warnings = threadContext.getResponseHeaders().get("Warning");
            assertNull("unexpected warning headers", warnings);
        } finally {
            resetDeprecationLogger(false);
        }
    }

    protected final void assertSettingDeprecationsAndWarnings(final Setting<?>[] settings, final String... warnings) {
        assertSettingDeprecationsAndWarnings(Arrays.stream(settings).map(Setting::getKey).toArray(String[]::new), warnings);
    }

    protected final void assertSettingDeprecationsAndWarnings(final String[] settings, final String... warnings) {
        assertWarnings(Stream.concat(Arrays.stream(settings).map(k -> "[" + k + "] setting was deprecated in Elasticsearch and will be removed in a future release! " + "See the breaking changes documentation for the next major version."), Arrays.stream(warnings)).toArray(String[]::new));
    }

    protected final void assertWarnings(String... expectedWarnings) {
        if (enableWarningsCheck() == false) {
            throw new IllegalStateException("unable to check warning headers if the test is not set to do so");
        }
        try {
            final List<String> actualWarnings = threadContext.getResponseHeaders().get("Warning");
            assertNotNull(actualWarnings);
            final Set<String> actualWarningValues = actualWarnings.stream().map(DeprecationLogger::extractWarningValueFromWarningHeader).collect(Collectors.toSet());
            for (String msg : expectedWarnings) {
                assertThat(actualWarningValues, hasItem(DeprecationLogger.escapeAndEncode(msg)));
            }
            assertEquals("Expected " + expectedWarnings.length + " warnings but found " + actualWarnings.size() + "\nExpected: " + Arrays.asList(expectedWarnings) + "\nActual: " + actualWarnings, expectedWarnings.length, actualWarnings.size());
        } finally {
            resetDeprecationLogger(true);
        }
    }

    private void resetDeprecationLogger(final boolean setNewThreadContext) {
        DeprecationLogger.removeThreadContext(this.threadContext);
        try {
            this.threadContext.close();
        } catch (IOException ex) {
            throw new AssertionError("IOException thrown while closing deprecation logger's thread context", ex);
        }
        if (setNewThreadContext) {
            this.threadContext = new ThreadContext(Settings.EMPTY);
            DeprecationLogger.setThreadContext(this.threadContext);
        } else {
            this.threadContext = null;
        }
    }

    private static final List<StatusData> statusData = new ArrayList<>();

    static {
        StatusLogger.getLogger().setLevel(Level.WARN);
        StatusLogger.getLogger().registerListener(new StatusConsoleListener(Level.WARN) {

            @Override
            public void log(StatusData data) {
                synchronized (statusData) {
                    statusData.add(data);
                }
            }
        });
    }

    protected static void checkStaticState(boolean afterClass) throws Exception {
        if (afterClass) {
            MockPageCacheRecycler.ensureAllPagesAreReleased();
        }
        MockBigArrays.ensureAllArraysAreReleased();
        assertThat(StatusLogger.getLogger().getLevel(), equalTo(Level.WARN));
        synchronized (statusData) {
            try {
                assertThat(statusData.stream().map(status -> status.getMessage().getFormattedMessage()).collect(Collectors.toList()), empty());
            } finally {
                statusData.clear();
            }
        }
        synchronized (nettyLoggedLeaks) {
            try {
                assertThat(nettyLoggedLeaks, empty());
            } finally {
                nettyLoggedLeaks.clear();
            }
        }
    }

    public final void ensureAllSearchContextsReleased() throws Exception {
        assertBusy(() -> MockSearchService.assertNoInFlightContext());
    }

    public static boolean checkIndexFailed;

    @Before
    public final void resetCheckIndexStatus() throws Exception {
        checkIndexFailed = false;
    }

    public final void ensureCheckIndexPassed() throws Exception {
        assertFalse("at least one shard failed CheckIndex", checkIndexFailed);
    }

    public static int scaledRandomIntBetween(int min, int max) {
        return RandomizedTest.scaledRandomIntBetween(min, max);
    }

    public static int randomIntBetween(int min, int max) {
        return RandomNumbers.randomIntBetween(random(), min, max);
    }

    public static long randomLongBetween(long min, long max) {
        return RandomNumbers.randomLongBetween(random(), min, max);
    }

    public static int iterations(int min, int max) {
        return scaledRandomIntBetween(min, max);
    }

    public static int between(int min, int max) {
        return randomIntBetween(min, max);
    }

    public static boolean frequently() {
        return !rarely();
    }

    public static boolean randomBoolean() {
        return random().nextBoolean();
    }

    public static byte randomByte() {
        return (byte) random().nextInt();
    }

    public static byte[] randomByteArrayOfLength(int size) {
        byte[] bytes = new byte[size];
        for (int i = 0; i < size; i++) {
            bytes[i] = randomByte();
        }
        return bytes;
    }

    public static short randomShort() {
        return (short) random().nextInt();
    }

    public static int randomInt() {
        return random().nextInt();
    }

    public static long randomNonNegativeLong() {
        long randomLong = randomLong();
        return randomLong == Long.MIN_VALUE ? 0 : Math.abs(randomLong);
    }

    public static float randomFloat() {
        return random().nextFloat();
    }

    public static double randomDouble() {
        return random().nextDouble();
    }

    public static double randomDoubleBetween(double start, double end, boolean lowerInclusive) {
        double result = 0.0;
        if (start == -Double.MAX_VALUE || end == Double.MAX_VALUE) {
            result = Double.longBitsToDouble(randomLong());
            while (result < start || result > end || Double.isNaN(result)) {
                result = Double.longBitsToDouble(randomLong());
            }
        } else {
            result = randomDouble();
            if (lowerInclusive == false) {
                while (result <= 0.0) {
                    result = randomDouble();
                }
            }
            result = result * end + (1.0 - result) * start;
        }
        return result;
    }

    public static long randomLong() {
        return random().nextLong();
    }

    public static int randomInt(int max) {
        return RandomizedTest.randomInt(max);
    }

    public static <T> T randomFrom(T... array) {
        return randomFrom(random(), array);
    }

    public static <T> T randomFrom(Random random, T... array) {
        return RandomPicks.randomFrom(random, array);
    }

    public static <T> T randomFrom(List<T> list) {
        return RandomPicks.randomFrom(random(), list);
    }

    public static <T> T randomFrom(Collection<T> collection) {
        return randomFrom(random(), collection);
    }

    public static <T> T randomFrom(Random random, Collection<T> collection) {
        return RandomPicks.randomFrom(random, collection);
    }

    public static String randomAlphaOfLengthBetween(int minCodeUnits, int maxCodeUnits) {
        return RandomizedTest.randomAsciiOfLengthBetween(minCodeUnits, maxCodeUnits);
    }

    public static String randomAlphaOfLength(int codeUnits) {
        return RandomizedTest.randomAsciiOfLength(codeUnits);
    }

    public static String randomUnicodeOfLengthBetween(int minCodeUnits, int maxCodeUnits) {
        return RandomizedTest.randomUnicodeOfLengthBetween(minCodeUnits, maxCodeUnits);
    }

    public static String randomUnicodeOfLength(int codeUnits) {
        return RandomizedTest.randomUnicodeOfLength(codeUnits);
    }

    public static String randomUnicodeOfCodepointLengthBetween(int minCodePoints, int maxCodePoints) {
        return RandomizedTest.randomUnicodeOfCodepointLengthBetween(minCodePoints, maxCodePoints);
    }

    public static String randomUnicodeOfCodepointLength(int codePoints) {
        return RandomizedTest.randomUnicodeOfCodepointLength(codePoints);
    }

    public static String randomRealisticUnicodeOfLengthBetween(int minCodeUnits, int maxCodeUnits) {
        return RandomizedTest.randomRealisticUnicodeOfLengthBetween(minCodeUnits, maxCodeUnits);
    }

    public static String randomRealisticUnicodeOfLength(int codeUnits) {
        return RandomizedTest.randomRealisticUnicodeOfLength(codeUnits);
    }

    public static String randomRealisticUnicodeOfCodepointLengthBetween(int minCodePoints, int maxCodePoints) {
        return RandomizedTest.randomRealisticUnicodeOfCodepointLengthBetween(minCodePoints, maxCodePoints);
    }

    public static String randomRealisticUnicodeOfCodepointLength(int codePoints) {
        return RandomizedTest.randomRealisticUnicodeOfCodepointLength(codePoints);
    }

    public static String[] generateRandomStringArray(int maxArraySize, int stringSize, boolean allowNull, boolean allowEmpty) {
        if (allowNull && random().nextBoolean()) {
            return null;
        }
        int arraySize = randomIntBetween(allowEmpty ? 0 : 1, maxArraySize);
        String[] array = new String[arraySize];
        for (int i = 0; i < arraySize; i++) {
            array[i] = RandomStrings.randomAsciiOfLength(random(), stringSize);
        }
        return array;
    }

    public static String[] generateRandomStringArray(int maxArraySize, int stringSize, boolean allowNull) {
        return generateRandomStringArray(maxArraySize, stringSize, allowNull, true);
    }

    public static <T> T[] randomArray(int maxArraySize, IntFunction<T[]> arrayConstructor, Supplier<T> valueConstructor) {
        return randomArray(0, maxArraySize, arrayConstructor, valueConstructor);
    }

    public static <T> T[] randomArray(int minArraySize, int maxArraySize, IntFunction<T[]> arrayConstructor, Supplier<T> valueConstructor) {
        final int size = randomIntBetween(minArraySize, maxArraySize);
        final T[] array = arrayConstructor.apply(size);
        for (int i = 0; i < array.length; i++) {
            array[i] = valueConstructor.get();
        }
        return array;
    }

    private static final String[] TIME_SUFFIXES = new String[] { "d", "h", "ms", "s", "m", "micros", "nanos" };

    public static String randomTimeValue(int lower, int upper, String... suffixes) {
        return randomIntBetween(lower, upper) + randomFrom(suffixes);
    }

    public static String randomTimeValue(int lower, int upper) {
        return randomTimeValue(lower, upper, TIME_SUFFIXES);
    }

    public static String randomTimeValue() {
        return randomTimeValue(0, 1000);
    }

    public static String randomPositiveTimeValue() {
        return randomTimeValue(1, 1000);
    }

    public static DateTimeZone randomDateTimeZone() {
        return DateTimeZone.forID(randomFrom(JODA_TIMEZONE_IDS));
    }

    public static TimeZone randomTimeZone() {
        return TimeZone.getTimeZone(randomFrom(JAVA_TIMEZONE_IDS));
    }

    public static ZoneId randomZone() {
        return ZoneId.of(randomFrom(JAVA_ZONE_IDS));
    }

    public static <T> void maybeSet(Consumer<T> consumer, T value) {
        if (randomBoolean()) {
            consumer.accept(value);
        }
    }

    public static <T> T randomValueOtherThan(T input, Supplier<T> randomSupplier) {
        return randomValueOtherThanMany(v -> Objects.equals(input, v), randomSupplier);
    }

    public static <T> T randomValueOtherThanMany(Predicate<T> input, Supplier<T> randomSupplier) {
        T randomValue = null;
        do {
            randomValue = randomSupplier.get();
        } while (input.test(randomValue));
        return randomValue;
    }

    public static void assertBusy(CheckedRunnable<Exception> codeBlock) throws Exception {
        assertBusy(codeBlock, 10, TimeUnit.SECONDS);
    }

    public static void assertBusy(CheckedRunnable<Exception> codeBlock, long maxWaitTime, TimeUnit unit) throws Exception {
        long maxTimeInMillis = TimeUnit.MILLISECONDS.convert(maxWaitTime, unit);
        long iterations = Math.max(Math.round(Math.log10(maxTimeInMillis) / Math.log10(2)), 1);
        long timeInMillis = 1;
        long sum = 0;
        List<AssertionError> failures = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            try {
                codeBlock.run();
                return;
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
            codeBlock.run();
        } catch (AssertionError e) {
            for (AssertionError failure : failures) {
                e.addSuppressed(failure);
            }
            throw e;
        }
    }

    public static boolean awaitBusy(BooleanSupplier breakSupplier) throws InterruptedException {
        return awaitBusy(breakSupplier, 10, TimeUnit.SECONDS);
    }

    private static final long AWAIT_BUSY_THRESHOLD = 1000L;

    public static boolean awaitBusy(BooleanSupplier breakSupplier, long maxWaitTime, TimeUnit unit) throws InterruptedException {
        long maxTimeInMillis = TimeUnit.MILLISECONDS.convert(maxWaitTime, unit);
        long timeInMillis = 1;
        long sum = 0;
        while (sum + timeInMillis < maxTimeInMillis) {
            if (breakSupplier.getAsBoolean()) {
                return true;
            }
            Thread.sleep(timeInMillis);
            sum += timeInMillis;
            timeInMillis = Math.min(AWAIT_BUSY_THRESHOLD, timeInMillis * 2);
        }
        timeInMillis = maxTimeInMillis - sum;
        Thread.sleep(Math.max(timeInMillis, 0));
        return breakSupplier.getAsBoolean();
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

    public static boolean terminate(ThreadPool threadPool) throws InterruptedException {
        return ThreadPool.terminate(threadPool, 10, TimeUnit.SECONDS);
    }

    @Override
    public Path getDataPath(String relativePath) {
        try {
            return PathUtils.get(getClass().getResource(relativePath).toURI());
        } catch (Exception e) {
            throw new RuntimeException("resource not found: " + relativePath, e);
        }
    }

    public Path getBwcIndicesPath() {
        return getDataPath("/indices/bwc");
    }

    public String[] tmpPaths() {
        final int numPaths = TestUtil.nextInt(random(), 1, 3);
        final String[] absPaths = new String[numPaths];
        for (int i = 0; i < numPaths; i++) {
            absPaths[i] = createTempDir().toAbsolutePath().toString();
        }
        return absPaths;
    }

    public NodeEnvironment newNodeEnvironment() throws IOException {
        return newNodeEnvironment(Settings.EMPTY);
    }

    public NodeEnvironment newNodeEnvironment(Settings settings) throws IOException {
        Settings build = Settings.builder().put(settings).put(Environment.PATH_HOME_SETTING.getKey(), createTempDir().toAbsolutePath()).putList(Environment.PATH_DATA_SETTING.getKey(), tmpPaths()).build();
        return new NodeEnvironment(build, TestEnvironment.newEnvironment(build), nodeId -> {
        });
    }

    public static Settings.Builder settings(Version version) {
        Settings.Builder builder = Settings.builder().put(IndexMetaData.SETTING_VERSION_CREATED, version);
        return builder;
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

    public static <T> List<T> randomSubsetOf(int size, T... values) {
        List<T> list = arrayAsArrayList(values);
        return randomSubsetOf(size, list);
    }

    public static <T> List<T> randomSubsetOf(Collection<T> collection) {
        return randomSubsetOf(randomInt(collection.size()), collection);
    }

    public static <T> List<T> randomSubsetOf(int size, Collection<T> collection) {
        if (size > collection.size()) {
            throw new IllegalArgumentException("Can\'t pick " + size + " random objects from a collection of " + collection.size() + " objects");
        }
        List<T> tempList = new ArrayList<>(collection);
        Collections.shuffle(tempList, random());
        return tempList.subList(0, size);
    }

    public static <T> Set<T> randomUnique(Supplier<T> supplier, int targetCount) {
        Set<T> things = new HashSet<>();
        int maxTries = targetCount * 10;
        for (int t = 0; t < maxTries; t++) {
            if (things.size() == targetCount) {
                return things;
            }
            things.add(supplier.get());
        }
        return things;
    }

    public static String randomGeohash(int minPrecision, int maxPrecision) {
        return geohashGenerator.ofStringLength(random(), minPrecision, maxPrecision);
    }

    private static boolean useNio;

    @BeforeClass
    public static void setUseNio() throws Exception {
        useNio = randomBoolean();
    }

    public static String getTestTransportType() {
        return useNio ? MockNioTransportPlugin.MOCK_NIO_TRANSPORT_NAME : MockTcpTransportPlugin.MOCK_TCP_TRANSPORT_NAME;
    }

    public static Class<? extends Plugin> getTestTransportPlugin() {
        return useNio ? MockNioTransportPlugin.class : MockTcpTransportPlugin.class;
    }

    private static final GeohashGenerator geohashGenerator = new GeohashGenerator();

    public static class GeohashGenerator extends CodepointSetGenerator {

        private static final char[] ASCII_SET = "0123456789bcdefghjkmnpqrstuvwxyz".toCharArray();

        public GeohashGenerator() {
            super(ASCII_SET);
        }
    }

    protected final BytesReference toShuffledXContent(ToXContent toXContent, XContentType xContentType, ToXContent.Params params, boolean humanReadable, String... exceptFieldNames) throws IOException {
        return toShuffledXContent(toXContent, xContentType, params, humanReadable, this::createParser, exceptFieldNames);
    }

    protected static BytesReference toShuffledXContent(ToXContent toXContent, XContentType xContentType, ToXContent.Params params, boolean humanReadable, CheckedBiFunction<XContent, BytesReference, XContentParser, IOException> parserFunction, String... exceptFieldNames) throws IOException {
        BytesReference bytes = XContentHelper.toXContent(toXContent, xContentType, params, humanReadable);
        try (XContentParser parser = parserFunction.apply(xContentType.xContent(), bytes)) {
            try (XContentBuilder builder = shuffleXContent(parser, rarely(), exceptFieldNames)) {
                return BytesReference.bytes(builder);
            }
        }
    }

    protected final XContentBuilder shuffleXContent(XContentBuilder builder, String... exceptFieldNames) throws IOException {
        try (XContentParser parser = createParser(builder)) {
            return shuffleXContent(parser, builder.isPrettyPrint(), exceptFieldNames);
        }
    }

    public static XContentBuilder shuffleXContent(XContentParser parser, boolean prettyPrint, String... exceptFieldNames) throws IOException {
        XContentBuilder xContentBuilder = XContentFactory.contentBuilder(parser.contentType());
        if (prettyPrint) {
            xContentBuilder.prettyPrint();
        }
        Token token = parser.currentToken() == null ? parser.nextToken() : parser.currentToken();
        if (token == Token.START_ARRAY) {
            List<Object> shuffledList = shuffleList(parser.listOrderedMap(), new HashSet<>(Arrays.asList(exceptFieldNames)));
            return xContentBuilder.value(shuffledList);
        }
        Map<String, Object> shuffledMap = shuffleMap((LinkedHashMap<String, Object>) parser.mapOrdered(), new HashSet<>(Arrays.asList(exceptFieldNames)));
        return xContentBuilder.map(shuffledMap);
    }

    @SuppressWarnings("unchecked")
    private static List<Object> shuffleList(List<Object> list, Set<String> exceptFields) {
        List<Object> targetList = new ArrayList<>();
        for (Object value : list) {
            if (value instanceof Map) {
                LinkedHashMap<String, Object> valueMap = (LinkedHashMap<String, Object>) value;
                targetList.add(shuffleMap(valueMap, exceptFields));
            } else if (value instanceof List) {
                targetList.add(shuffleList((List) value, exceptFields));
            } else {
                targetList.add(value);
            }
        }
        return targetList;
    }

    @SuppressWarnings("unchecked")
    public static LinkedHashMap<String, Object> shuffleMap(LinkedHashMap<String, Object> map, Set<String> exceptFields) {
        List<String> keys = new ArrayList<>(map.keySet());
        LinkedHashMap<String, Object> targetMap = new LinkedHashMap<>();
        Collections.shuffle(keys, random());
        for (String key : keys) {
            Object value = map.get(key);
            if (value instanceof Map && exceptFields.contains(key) == false) {
                LinkedHashMap<String, Object> valueMap = (LinkedHashMap<String, Object>) value;
                targetMap.put(key, shuffleMap(valueMap, exceptFields));
            } else if (value instanceof List && exceptFields.contains(key) == false) {
                targetMap.put(key, shuffleList((List) value, exceptFields));
            } else {
                targetMap.put(key, value);
            }
        }
        return targetMap;
    }

    public static <T extends Writeable> T copyWriteable(T original, NamedWriteableRegistry namedWriteableRegistry, Writeable.Reader<T> reader) throws IOException {
        return copyWriteable(original, namedWriteableRegistry, reader, Version.CURRENT);
    }

    public static <T extends Writeable> T copyWriteable(T original, NamedWriteableRegistry namedWriteableRegistry, Writeable.Reader<T> reader, Version version) throws IOException {
        return copyInstance(original, namedWriteableRegistry, (out, value) -> value.writeTo(out), reader, version);
    }

    public static <T extends Streamable> T copyStreamable(T original, NamedWriteableRegistry namedWriteableRegistry, Supplier<T> supplier, Version version) throws IOException {
        return copyInstance(original, namedWriteableRegistry, (out, value) -> value.writeTo(out), Streamable.newWriteableReader(supplier), version);
    }

    private static <T> T copyInstance(T original, NamedWriteableRegistry namedWriteableRegistry, Writeable.Writer<T> writer, Writeable.Reader<T> reader, Version version) throws IOException {
        try (BytesStreamOutput output = new BytesStreamOutput()) {
            output.setVersion(version);
            writer.write(output, original);
            try (StreamInput in = new NamedWriteableAwareStreamInput(output.bytes().streamInput(), namedWriteableRegistry)) {
                in.setVersion(version);
                return reader.read(in);
            }
        }
    }

    public void assertAllIndicesRemovedAndDeletionCompleted(Iterable<IndicesService> indicesServices) throws Exception {
        for (IndicesService indicesService : indicesServices) {
            assertBusy(() -> assertFalse(indicesService.iterator().hasNext()), 1, TimeUnit.MINUTES);
            assertBusy(() -> assertFalse(indicesService.hasUncompletedPendingDeletes()), 1, TimeUnit.MINUTES);
        }
    }

    public void assertPathHasBeenCleared(Path path) {
        logger.info("--> checking that [{}] has been cleared", path);
        int count = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (Files.exists(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path file : stream) {
                    if (file.getFileName().toString().startsWith("extra")) {
                        continue;
                    }
                    logger.info("--> found file: [{}]", file.toAbsolutePath().toString());
                    if (Files.isDirectory(file)) {
                        assertPathHasBeenCleared(file);
                    } else if (Files.isRegularFile(file)) {
                        count++;
                        sb.append(file.toAbsolutePath().toString());
                        sb.append("\n");
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        sb.append("]");
        assertThat(count + " files exist that should have been cleaned:\n" + sb.toString(), count, equalTo(0));
    }

    public static <T extends ToXContent> void assertEqualsWithErrorMessageFromXContent(T expected, T actual) {
        if (Objects.equals(expected, actual)) {
            return;
        }
        if (expected == null) {
            throw new AssertionError("Expected null be actual was [" + actual.toString() + "]");
        }
        if (actual == null) {
            throw new AssertionError("Didn't expect null but actual was [null]");
        }
        try (XContentBuilder actualJson = JsonXContent.contentBuilder();
            XContentBuilder expectedJson = JsonXContent.contentBuilder()) {
            actualJson.startObject();
            actual.toXContent(actualJson, ToXContent.EMPTY_PARAMS);
            actualJson.endObject();
            expectedJson.startObject();
            expected.toXContent(expectedJson, ToXContent.EMPTY_PARAMS);
            expectedJson.endObject();
            NotEqualMessageBuilder message = new NotEqualMessageBuilder();
            message.compareMaps(XContentHelper.convertToMap(BytesReference.bytes(actualJson), false).v2(), XContentHelper.convertToMap(BytesReference.bytes(expectedJson), false).v2());
            throw new AssertionError("Didn't match expected value:\n" + message);
        } catch (IOException e) {
            throw new AssertionError("IOException while building failure message", e);
        }
    }

    protected final XContentParser createParser(XContentBuilder builder) throws IOException {
        return builder.generator().contentType().xContent().createParser(xContentRegistry(), LoggingDeprecationHandler.INSTANCE, BytesReference.bytes(builder).streamInput());
    }

    protected final XContentParser createParser(XContent xContent, String data) throws IOException {
        return xContent.createParser(xContentRegistry(), LoggingDeprecationHandler.INSTANCE, data);
    }

    protected final XContentParser createParser(XContent xContent, InputStream data) throws IOException {
        return xContent.createParser(xContentRegistry(), LoggingDeprecationHandler.INSTANCE, data);
    }

    protected final XContentParser createParser(XContent xContent, byte[] data) throws IOException {
        return xContent.createParser(xContentRegistry(), LoggingDeprecationHandler.INSTANCE, data);
    }

    protected final XContentParser createParser(XContent xContent, BytesReference data) throws IOException {
        return xContent.createParser(xContentRegistry(), LoggingDeprecationHandler.INSTANCE, data.streamInput());
    }

    protected NamedXContentRegistry xContentRegistry() {
        return new NamedXContentRegistry(ClusterModule.getNamedXWriteables());
    }

    protected NamedWriteableRegistry writableRegistry() {
        return new NamedWriteableRegistry(ClusterModule.getNamedWriteables());
    }

    public static final Script mockScript(String id) {
        return new Script(ScriptType.INLINE, MockScriptEngine.NAME, id, emptyMap());
    }

    public static TestRuleMarkFailure getSuiteFailureMarker() {
        return suiteFailureMarker;
    }

    public static void assertArrayEquals(StackTraceElement[] expected, StackTraceElement[] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i]);
        }
    }

    public static void assertEquals(StackTraceElement expected, StackTraceElement actual) {
        assertEquals(expected.getClassName(), actual.getClassName());
        assertEquals(expected.getMethodName(), actual.getMethodName());
        assertEquals(expected.getFileName(), actual.getFileName());
        assertEquals(expected.getLineNumber(), actual.getLineNumber());
        assertEquals(expected.isNativeMethod(), actual.isNativeMethod());
    }

    protected static long spinForAtLeastOneMillisecond() {
        return spinForAtLeastNMilliseconds(1);
    }

    protected static long spinForAtLeastNMilliseconds(final long ms) {
        long nanosecondsInMillisecond = TimeUnit.NANOSECONDS.convert(ms, TimeUnit.MILLISECONDS);
        long start = System.nanoTime();
        long elapsed;
        while ((elapsed = (System.nanoTime() - start)) < nanosecondsInMillisecond) {
        }
        return elapsed;
    }

    public static TestAnalysis createTestAnalysis(Index index, Settings settings, AnalysisPlugin... analysisPlugins) throws IOException {
        Settings nodeSettings = Settings.builder().put(Environment.PATH_HOME_SETTING.getKey(), createTempDir()).build();
        return createTestAnalysis(index, nodeSettings, settings, analysisPlugins);
    }

    public static TestAnalysis createTestAnalysis(Index index, Settings nodeSettings, Settings settings, AnalysisPlugin... analysisPlugins) throws IOException {
        Settings indexSettings = Settings.builder().put(settings).put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT).build();
        return createTestAnalysis(IndexSettingsModule.newIndexSettings(index, indexSettings), nodeSettings, analysisPlugins);
    }

    public static TestAnalysis createTestAnalysis(IndexSettings indexSettings, Settings nodeSettings, AnalysisPlugin... analysisPlugins) throws IOException {
        Environment env = TestEnvironment.newEnvironment(nodeSettings);
        AnalysisModule analysisModule = new AnalysisModule(env, Arrays.asList(analysisPlugins));
        AnalysisRegistry analysisRegistry = analysisModule.getAnalysisRegistry();
        return new TestAnalysis(analysisRegistry.build(indexSettings), analysisRegistry.buildTokenFilterFactories(indexSettings), analysisRegistry.buildTokenizerFactories(indexSettings), analysisRegistry.buildCharFilterFactories(indexSettings));
    }

    public static ScriptModule newTestScriptModule() {
        return new ScriptModule(Settings.EMPTY, singletonList(new ScriptPlugin() {

            @Override
            public ScriptEngine getScriptEngine(Settings settings, Collection<ScriptContext<?>> contexts) {
                return new MockScriptEngine(MockScriptEngine.NAME, Collections.singletonMap("1", script -> "1"));
            }
        }));
    }

    public static IndicesModule newTestIndicesModule(Map<String, Mapper.TypeParser> extraMappers, Map<String, MetadataFieldMapper.TypeParser> extraMetadataMappers) {
        return new IndicesModule(Collections.singletonList(new MapperPlugin() {

            @Override
            public Map<String, Mapper.TypeParser> getMappers() {
                return extraMappers;
            }

            @Override
            public Map<String, MetadataFieldMapper.TypeParser> getMetadataMappers() {
                return extraMetadataMappers;
            }
        }));
    }

    public static final class TestAnalysis {

        public final IndexAnalyzers indexAnalyzers;

        public final Map<String, TokenFilterFactory> tokenFilter;

        public final Map<String, TokenizerFactory> tokenizer;

        public final Map<String, CharFilterFactory> charFilter;

        public TestAnalysis(IndexAnalyzers indexAnalyzers, Map<String, TokenFilterFactory> tokenFilter, Map<String, TokenizerFactory> tokenizer, Map<String, CharFilterFactory> charFilter) {
            this.indexAnalyzers = indexAnalyzers;
            this.tokenFilter = tokenFilter;
            this.tokenizer = tokenizer;
            this.charFilter = charFilter;
        }
    }

    public static boolean inFipsJvm() {
        return Security.getProviders()[0].getName().toLowerCase(Locale.ROOT).contains("fips");
    }
}
