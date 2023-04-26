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
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.status.StatusConsoleListener;
import org.apache.logging.log4j.status.StatusData;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.lucene.uninverting.UninvertingReader;
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
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.common.io.PathUtilsForTesting;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.io.stream.NamedWriteable;
import org.elasticsearch.common.io.stream.NamedWriteableAwareStreamInput;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.logging.DeprecationLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.util.MockBigArrays;
import org.elasticsearch.common.util.MockPageCacheRecycler;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.NodeEnvironment;
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
import org.elasticsearch.script.MockScriptEngine;
import org.elasticsearch.script.ScriptModule;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.search.MockSearchService;
import org.elasticsearch.test.junit.listeners.LoggingListener;
import org.elasticsearch.test.junit.listeners.ReproduceInfoPrinter;
import org.elasticsearch.threadpool.ThreadPool;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.elasticsearch.common.util.CollectionUtils.arrayAsArrayList;
import static org.hamcrest.Matchers.containsString;
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

    private static final AtomicInteger portGenerator = new AtomicInteger();

    @AfterClass
    public static void resetPortCounter() {
        portGenerator.set(0);
    }

    static {
        System.setProperty("log4j.shutdownHookEnabled", "false");
        System.setProperty("log4j2.disable.jmx", "true");
        System.setProperty("log4j.skipJansi", "true");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            Configurator.shutdown(context);
        }));
        BootstrapForTesting.ensureInitialized();
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
        logger.info("[{}]: before test", getTestName());
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
        checkStaticState();
        if (threadContext != null) {
            ensureNoWarnings();
            threadContext = null;
        }
        ensureAllSearchContextsReleased();
        ensureCheckIndexPassed();
        logger.info("[{}]: after test", getTestName());
    }

    private void ensureNoWarnings() throws IOException {
        try {
            final List<String> warnings = threadContext.getResponseHeaders().get(DeprecationLogger.WARNING_HEADER);
            assertNull("unexpected warning headers", warnings);
        } finally {
            resetDeprecationLogger();
        }
    }

    protected final void assertWarnings(String... expectedWarnings) {
        if (enableWarningsCheck() == false) {
            throw new IllegalStateException("unable to check warning headers if the test is not set to do so");
        }
        try {
            final List<String> actualWarnings = threadContext.getResponseHeaders().get(DeprecationLogger.WARNING_HEADER);
            for (String msg : expectedWarnings) {
                assertThat(actualWarnings, hasItem(containsString(msg)));
            }
            assertEquals("Expected " + expectedWarnings.length + " warnings but found " + actualWarnings.size() + "\nExpected: " + Arrays.asList(expectedWarnings) + "\nActual: " + actualWarnings, expectedWarnings.length, actualWarnings.size());
        } finally {
            resetDeprecationLogger();
        }
    }

    private void resetDeprecationLogger() {
        DeprecationLogger.removeThreadContext(this.threadContext);
        try {
            this.threadContext.close();
        } catch (IOException ex) {
            throw new AssertionError("IOException thrown while closing deprecation logger's thread context", ex);
        }
        this.threadContext = new ThreadContext(Settings.EMPTY);
        DeprecationLogger.setThreadContext(this.threadContext);
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

    protected static void checkStaticState() throws Exception {
        MockPageCacheRecycler.ensureAllPagesAreReleased();
        MockBigArrays.ensureAllArraysAreReleased();
        String[] entries = UninvertingReader.getUninvertedStats();
        assertEquals("fieldcache must never be used, got=" + Arrays.toString(entries), 0, entries.length);
        assertThat(StatusLogger.getLogger().getLevel(), equalTo(Level.WARN));
        synchronized (statusData) {
            try {
                assertThat(statusData.stream().map(status -> status.getMessage().getFormattedMessage()).collect(Collectors.toList()), empty());
            } finally {
                statusData.clear();
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

    public static short randomShort() {
        return (short) random().nextInt();
    }

    public static int randomInt() {
        return random().nextInt();
    }

    public static long randomNonNegativeLong() {
        long randomLong;
        do {
            randomLong = randomLong();
        } while (randomLong == Long.MIN_VALUE);
        return Math.abs(randomLong);
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

    public static String randomAsciiOfLengthBetween(int minCodeUnits, int maxCodeUnits) {
        return RandomizedTest.randomAsciiOfLengthBetween(minCodeUnits, maxCodeUnits);
    }

    public static String randomAsciiOfLength(int codeUnits) {
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

    public static String[] generateRandomStringArray(int maxArraySize, int maxStringSize, boolean allowNull, boolean allowEmpty) {
        if (allowNull && random().nextBoolean()) {
            return null;
        }
        int arraySize = randomIntBetween(allowEmpty ? 0 : 1, maxArraySize);
        String[] array = new String[arraySize];
        for (int i = 0; i < arraySize; i++) {
            array[i] = RandomStrings.randomAsciiOfLength(random(), maxStringSize);
        }
        return array;
    }

    public static String[] generateRandomStringArray(int maxArraySize, int maxStringSize, boolean allowNull) {
        return generateRandomStringArray(maxArraySize, maxStringSize, allowNull, true);
    }

    private static String[] TIME_SUFFIXES = new String[] { "d", "h", "ms", "s", "m" };

    private static String randomTimeValue(int lower, int upper) {
        return randomIntBetween(lower, upper) + randomFrom(TIME_SUFFIXES);
    }

    public static String randomTimeValue() {
        return randomTimeValue(0, 1000);
    }

    public static String randomPositiveTimeValue() {
        return randomTimeValue(1, 1000);
    }

    public static DateTimeZone randomDateTimeZone() {
        List<String> ids = new ArrayList<>(DateTimeZone.getAvailableIDs());
        Collections.sort(ids);
        return DateTimeZone.forID(randomFrom(ids));
    }

    public static <T> void maybeSet(Consumer<T> consumer, T value) {
        if (randomBoolean()) {
            consumer.accept(value);
        }
    }

    public static <T> T randomValueOtherThan(T input, Supplier<T> randomSupplier) {
        if (input != null) {
            return randomValueOtherThanMany(input::equals, randomSupplier);
        }
        return (randomSupplier.get());
    }

    public static <T> T randomValueOtherThanMany(Predicate<T> input, Supplier<T> randomSupplier) {
        T randomValue = null;
        do {
            randomValue = randomSupplier.get();
        } while (input.test(randomValue));
        return randomValue;
    }

    public static void assertBusy(Runnable codeBlock) throws Exception {
        assertBusy(codeBlock, 10, TimeUnit.SECONDS);
    }

    public static void assertBusy(Runnable codeBlock, long maxWaitTime, TimeUnit unit) throws Exception {
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

    public static boolean terminate(ThreadPool service) throws InterruptedException {
        return ThreadPool.terminate(service, 10, TimeUnit.SECONDS);
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
        Settings build = Settings.builder().put(settings).put(Environment.PATH_HOME_SETTING.getKey(), createTempDir().toAbsolutePath()).putArray(Environment.PATH_DATA_SETTING.getKey(), tmpPaths()).build();
        return new NodeEnvironment(build, new Environment(build));
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
        return randomSubsetOf(randomInt(Math.max(collection.size() - 1, 0)), collection);
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

    private static final GeohashGenerator geohashGenerator = new GeohashGenerator();

    public static class GeohashGenerator extends CodepointSetGenerator {

        private static final char[] ASCII_SET = "0123456789bcdefghjkmnpqrstuvwxyz".toCharArray();

        public GeohashGenerator() {
            super(ASCII_SET);
        }
    }

    public XContentBuilder shuffleXContent(XContentBuilder builder, String... exceptFieldNames) throws IOException {
        XContentParser parser = createParser(builder);
        Map<String, Object> shuffledMap = shuffleMap(parser.mapOrdered(), new HashSet<>(Arrays.asList(exceptFieldNames)));
        XContentBuilder xContentBuilder = XContentFactory.contentBuilder(builder.contentType());
        if (builder.isPrettyPrint()) {
            xContentBuilder.prettyPrint();
        }
        return xContentBuilder.map(shuffledMap);
    }

    private static Map<String, Object> shuffleMap(Map<String, Object> map, Set<String> exceptFields) {
        List<String> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);
        Map<String, Object> targetMap = new TreeMap<>();
        Collections.shuffle(keys, random());
        for (String key : keys) {
            Object value = map.get(key);
            if (value instanceof Map && exceptFields.contains(key) == false) {
                targetMap.put(key, shuffleMap((Map) value, exceptFields));
            } else {
                targetMap.put(key, value);
            }
        }
        return targetMap;
    }

    public static <T extends Writeable> T copyWriteable(T original, NamedWriteableRegistry namedWritabelRegistry, Writeable.Reader<T> reader) throws IOException {
        try (BytesStreamOutput output = new BytesStreamOutput()) {
            original.writeTo(output);
            try (StreamInput in = new NamedWriteableAwareStreamInput(output.bytes().streamInput(), namedWritabelRegistry)) {
                return reader.read(in);
            }
        }
    }

    public static boolean assertionsEnabled() {
        boolean enabled = false;
        assert (enabled = true);
        return enabled;
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

    protected final XContentParser createParser(XContentBuilder builder) throws IOException {
        return builder.generator().contentType().xContent().createParser(xContentRegistry(), builder.bytes());
    }

    protected final XContentParser createParser(XContent xContent, String data) throws IOException {
        return xContent.createParser(xContentRegistry(), data);
    }

    protected final XContentParser createParser(XContent xContent, InputStream data) throws IOException {
        return xContent.createParser(xContentRegistry(), data);
    }

    protected final XContentParser createParser(XContent xContent, byte[] data) throws IOException {
        return xContent.createParser(xContentRegistry(), data);
    }

    protected final XContentParser createParser(XContent xContent, BytesReference data) throws IOException {
        return xContent.createParser(xContentRegistry(), data);
    }

    protected NamedXContentRegistry xContentRegistry() {
        return new NamedXContentRegistry(ClusterModule.getNamedXWriteables());
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
        long nanosecondsInMillisecond = TimeUnit.NANOSECONDS.convert(1, TimeUnit.MILLISECONDS);
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
        Environment env = new Environment(nodeSettings);
        AnalysisModule analysisModule = new AnalysisModule(env, Arrays.asList(analysisPlugins));
        AnalysisRegistry analysisRegistry = analysisModule.getAnalysisRegistry();
        return new TestAnalysis(analysisRegistry.build(indexSettings), analysisRegistry.buildTokenFilterFactories(indexSettings), analysisRegistry.buildTokenizerFactories(indexSettings), analysisRegistry.buildCharFilterFactories(indexSettings));
    }

    public static ScriptModule newTestScriptModule() {
        Settings settings = Settings.builder().put(Environment.PATH_HOME_SETTING.getKey(), createTempDir()).put(ScriptService.SCRIPT_AUTO_RELOAD_ENABLED_SETTING.getKey(), false).build();
        Environment environment = new Environment(settings);
        MockScriptEngine scriptEngine = new MockScriptEngine(MockScriptEngine.NAME, Collections.singletonMap("1", script -> "1"));
        return new ScriptModule(settings, environment, null, singletonList(scriptEngine), emptyList());
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
}
