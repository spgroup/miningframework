package org.apache.cassandra.cql3;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import org.junit.AfterClass;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static junit.framework.Assert.assertNotNull;
import org.apache.cassandra.SchemaLoader;
import org.apache.cassandra.concurrent.ScheduledExecutors;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.db.Directories;
import org.apache.cassandra.db.Keyspace;
import org.apache.cassandra.db.marshal.*;
import org.apache.cassandra.dht.Murmur3Partitioner;
import org.apache.cassandra.exceptions.*;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.serializers.TypeSerializer;

public abstract class CQLTester {

    protected static final Logger logger = LoggerFactory.getLogger(CQLTester.class);

    public static final String KEYSPACE = "cql_test_keyspace";

    private static final boolean USE_PREPARED_VALUES = Boolean.valueOf(System.getProperty("cassandra.test.use_prepared", "true"));

    protected static final long ROW_CACHE_SIZE_IN_MB = Integer.valueOf(System.getProperty("cassandra.test.row_cache_size_in_mb", "0"));

    private static final AtomicInteger seqNumber = new AtomicInteger();

    static {
        SchemaLoader.prepareServer();
    }

    private List<String> tables = new ArrayList<>();

    private List<String> types = new ArrayList<>();

    @BeforeClass
    public static void setUpClass() {
        if (ROW_CACHE_SIZE_IN_MB > 0)
            DatabaseDescriptor.setRowCacheSizeInMB(ROW_CACHE_SIZE_IN_MB);
        DatabaseDescriptor.setPartitioner(new Murmur3Partitioner());
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void beforeTest() throws Throwable {
        schemaChange(String.format("CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}", KEYSPACE));
    }

    @After
    public void afterTest() throws Throwable {
        final List<String> tablesToDrop = copy(tables);
        final List<String> typesToDrop = copy(types);
        tables = null;
        types = null;
        ScheduledExecutors.optionalTasks.execute(new Runnable() {

            public void run() {
                try {
                    for (int i = tablesToDrop.size() - 1; i >= 0; i--) schemaChange(String.format("DROP TABLE IF EXISTS %s.%s", KEYSPACE, tablesToDrop.get(i)));
                    for (int i = typesToDrop.size() - 1; i >= 0; i--) schemaChange(String.format("DROP TYPE IF EXISTS %s.%s", KEYSPACE, typesToDrop.get(i)));
                    final CountDownLatch latch = new CountDownLatch(1);
                    ScheduledExecutors.nonPeriodicTasks.execute(new Runnable() {

                        public void run() {
                            latch.countDown();
                        }
                    });
                    latch.await(2, TimeUnit.SECONDS);
                    removeAllSSTables(KEYSPACE, tablesToDrop);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static List<String> copy(List<String> list) {
        return list.isEmpty() ? Collections.<String>emptyList() : new ArrayList<>(list);
    }

    public void flush() {
        try {
            String currentTable = currentTable();
            if (currentTable != null)
                Keyspace.open(KEYSPACE).getColumnFamilyStore(currentTable).forceFlush().get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void compact() {
        try {
            String currentTable = currentTable();
            if (currentTable != null)
                Keyspace.open(KEYSPACE).getColumnFamilyStore(currentTable).forceMajorCompaction();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void cleanupCache() {
        String currentTable = currentTable();
        if (currentTable != null)
            Keyspace.open(KEYSPACE).getColumnFamilyStore(currentTable).cleanupCache();
    }

    private static void removeAllSSTables(String ks, List<String> tables) {
        for (File d : Directories.getKSChildDirectories(ks)) {
            if (d.exists() && containsAny(d.getName(), tables))
                FileUtils.deleteRecursive(d);
        }
    }

    private static boolean containsAny(String filename, List<String> tables) {
        for (int i = 0, m = tables.size(); i < m; i++) if (filename.contains(tables.get(i)))
            return true;
        return false;
    }

    protected String keyspace() {
        return KEYSPACE;
    }

    protected String currentTable() {
        if (tables.isEmpty())
            return null;
        return tables.get(tables.size() - 1);
    }

    protected String createType(String query) {
        String typeName = "type_" + seqNumber.getAndIncrement();
        String fullQuery = String.format(query, KEYSPACE + "." + typeName);
        types.add(typeName);
        logger.info(fullQuery);
        schemaChange(fullQuery);
        return typeName;
    }

    protected String createTable(String query) {
        String currentTable = createTableName();
        String fullQuery = String.format(query, KEYSPACE + "." + currentTable);
        logger.info(fullQuery);
        schemaChange(fullQuery);
        return currentTable;
    }

    protected String createTableName() {
        String currentTable = "table_" + seqNumber.getAndIncrement();
        tables.add(currentTable);
        return currentTable;
    }

    protected void createTableMayThrow(String query) throws Throwable {
        String currentTable = "table_" + seqNumber.getAndIncrement();
        tables.add(currentTable);
        String fullQuery = String.format(query, KEYSPACE + "." + currentTable);
        logger.info(fullQuery);
        try {
            QueryProcessor.executeOnceInternal(fullQuery);
        } catch (RuntimeException ex) {
            throw ex.getCause();
        }
    }

    protected void alterTable(String query) {
        String fullQuery = String.format(query, KEYSPACE + "." + currentTable());
        logger.info(fullQuery);
        schemaChange(fullQuery);
    }

    protected void alterTableMayThrow(String query) throws Throwable {
        String fullQuery = String.format(query, KEYSPACE + "." + currentTable());
        logger.info(fullQuery);
        try {
            QueryProcessor.executeOnceInternal(fullQuery);
        } catch (RuntimeException ex) {
            throw ex.getCause();
        }
    }

    protected void dropTable(String query) {
        String fullQuery = String.format(query, KEYSPACE + "." + currentTable());
        logger.info(fullQuery);
        schemaChange(fullQuery);
    }

    protected void createIndex(String query) {
        String fullQuery = String.format(query, KEYSPACE + "." + currentTable());
        logger.info(fullQuery);
        schemaChange(fullQuery);
    }

    protected boolean waitForIndex(String keyspace, String table, String index) throws Throwable {
        long start = System.currentTimeMillis();
        boolean indexCreated = false;
        String indedName = String.format("%s.%s", table, index);
        while (!indexCreated) {
            Object[][] results = getRows(execute("select index_name from system.\"IndexInfo\" where table_name = ?", keyspace));
            for (int i = 0; i < results.length; i++) {
                if (indedName.equals(results[i][0])) {
                    indexCreated = true;
                    break;
                }
            }
            if (System.currentTimeMillis() - start > 5000)
                break;
            Thread.sleep(10);
        }
        return indexCreated;
    }

    protected void createIndexMayThrow(String query) throws Throwable {
        String fullQuery = String.format(query, KEYSPACE + "." + currentTable());
        logger.info(fullQuery);
        try {
            QueryProcessor.executeOnceInternal(fullQuery);
        } catch (RuntimeException ex) {
            throw ex.getCause();
        }
    }

    protected void dropIndex(String query) throws Throwable {
        String fullQuery = String.format(query, KEYSPACE);
        logger.info(fullQuery);
        schemaChange(fullQuery);
    }

    protected static void schemaChange(String query) {
        try {
            QueryProcessor.executeOnceInternal(query);
        } catch (Exception e) {
            throw new RuntimeException("Error setting schema for test (query was: " + query + ")", e);
        }
    }

    protected CFMetaData currentTableMetadata() {
        return Schema.instance.getCFMetaData(KEYSPACE, currentTable());
    }

    protected UntypedResultSet execute(String query, Object... values) throws Throwable {
        try {
            query = currentTable() == null ? query : String.format(query, KEYSPACE + "." + currentTable());
            UntypedResultSet rs;
            if (USE_PREPARED_VALUES) {
                logger.info("Executing: {} with values {}", query, formatAllValues(values));
                rs = QueryProcessor.executeOnceInternal(query, transformValues(values));
            } else {
                query = replaceValues(query, values);
                logger.info("Executing: {}", query);
                rs = QueryProcessor.executeOnceInternal(query);
            }
            if (rs != null)
                logger.info("Got {} rows", rs.size());
            return rs;
        } catch (RuntimeException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            logger.info("Got error: {}", cause.getMessage() == null ? cause.toString() : cause.getMessage());
            throw cause;
        }
    }

    protected void assertRows(UntypedResultSet result, Object[]... rows) {
        if (result == null) {
            if (rows.length > 0)
                Assert.fail(String.format("No rows returned by query but %d expected", rows.length));
            return;
        }
        List<ColumnSpecification> meta = result.metadata();
        Iterator<UntypedResultSet.Row> iter = result.iterator();
        int i = 0;
        while (iter.hasNext() && i < rows.length) {
            Object[] expected = rows[i];
            UntypedResultSet.Row actual = iter.next();
            Assert.assertEquals(String.format("Invalid number of (expected) values provided for row %d", i), expected.length, meta.size());
            for (int j = 0; j < meta.size(); j++) {
                ColumnSpecification column = meta.get(j);
                Object expectedValue = expected[j];
                ByteBuffer expectedByteValue = makeByteBuffer(expected[j], (AbstractType) column.type);
                ByteBuffer actualValue = actual.getBytes(column.name.toString());
                if (!Objects.equal(expectedByteValue, actualValue)) {
                    Object actualValueDecoded = column.type.getSerializer().deserialize(actualValue);
                    if (!actualValueDecoded.equals(expected[j]))
                        Assert.fail(String.format("Invalid value for row %d column %d (%s of type %s), expected <%s> but got <%s>", i, j, column.name, column.type.asCQL3Type(), formatValue(expectedByteValue, column.type), formatValue(actualValue, column.type)));
                }
            }
            i++;
        }
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                iter.next();
                i++;
            }
            Assert.fail(String.format("Got less rows than expected. Expected %d but got %d.", rows.length, i));
        }
        Assert.assertTrue(String.format("Got more rows than expected. Expected %d but got %d", rows.length, i), i == rows.length);
    }

    protected void assertRowCount(UntypedResultSet result, int numExpectedRows) {
        if (result == null) {
            if (numExpectedRows > 0)
                Assert.fail(String.format("No rows returned by query but %d expected", numExpectedRows));
            return;
        }
        List<ColumnSpecification> meta = result.metadata();
        Iterator<UntypedResultSet.Row> iter = result.iterator();
        int i = 0;
        while (iter.hasNext() && i < numExpectedRows) {
            UntypedResultSet.Row actual = iter.next();
            assertNotNull(actual);
            i++;
        }
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                iter.next();
                i++;
            }
            Assert.fail(String.format("Got less rows than expected. Expected %d but got %d.", numExpectedRows, i));
        }
        Assert.assertTrue(String.format("Got %s rows than expected. Expected %d but got %d", numExpectedRows > i ? "less" : "more", numExpectedRows, i), i == numExpectedRows);
    }

    protected Object[][] getRows(UntypedResultSet result) {
        if (result == null)
            return new Object[0][];
        List<Object[]> ret = new ArrayList<>();
        List<ColumnSpecification> meta = result.metadata();
        Iterator<UntypedResultSet.Row> iter = result.iterator();
        while (iter.hasNext()) {
            UntypedResultSet.Row rowVal = iter.next();
            Object[] row = new Object[meta.size()];
            for (int j = 0; j < meta.size(); j++) {
                ColumnSpecification column = meta.get(j);
                ByteBuffer val = rowVal.getBytes(column.name.toString());
                row[j] = val == null ? null : column.type.getSerializer().deserialize(val);
            }
            ret.add(row);
        }
        Object[][] a = new Object[ret.size()][];
        return ret.toArray(a);
    }

    protected void assertAllRows(Object[]... rows) throws Throwable {
        assertRows(execute("SELECT * FROM %s"), rows);
    }

    protected Object[] row(Object... expected) {
        return expected;
    }

    protected void assertEmpty(UntypedResultSet result) throws Throwable {
        if (result != null && result.size() != 0)
            throw new InvalidRequestException(String.format("Expected empty result but got %d rows", result.size()));
    }

    protected void assertInvalid(String query, Object... values) throws Throwable {
        assertInvalidMessage(null, query, values);
    }

    protected void assertInvalidMessage(String errorMessage, String query, Object... values) throws Throwable {
        assertInvalidThrowMessage(errorMessage, null, query, values);
    }

    protected void assertInvalidThrow(Class<? extends Throwable> exception, String query, Object... values) throws Throwable {
        assertInvalidThrowMessage(null, exception, query, values);
    }

    protected void assertInvalidThrowMessage(String errorMessage, Class<? extends Throwable> exception, String query, Object... values) throws Throwable {
        try {
            execute(query, values);
            String q = USE_PREPARED_VALUES ? query + " (values: " + formatAllValues(values) + ")" : replaceValues(query, values);
            Assert.fail("Query should be invalid but no error was thrown. Query is: " + q);
        } catch (CassandraException e) {
            if (exception != null && !exception.isAssignableFrom(e.getClass())) {
                Assert.fail("Query should be invalid but wrong error was thrown. " + "Expected: " + exception.getName() + ", got: " + e.getClass().getName() + ". " + "Query is: " + queryInfo(query, values));
            }
            if (errorMessage != null) {
                assertMessageContains(errorMessage, e);
            }
        }
    }

    private static String queryInfo(String query, Object[] values) {
        return USE_PREPARED_VALUES ? query + " (values: " + formatAllValues(values) + ")" : replaceValues(query, values);
    }

    protected void assertInvalidSyntax(String query, Object... values) throws Throwable {
        assertInvalidSyntaxMessage(null, query, values);
    }

    protected void assertInvalidSyntaxMessage(String errorMessage, String query, Object... values) throws Throwable {
        try {
            execute(query, values);
            String q = USE_PREPARED_VALUES ? query + " (values: " + formatAllValues(values) + ")" : replaceValues(query, values);
            Assert.fail("Query should have invalid syntax but no error was thrown. Query is: " + q);
        } catch (SyntaxException e) {
            if (errorMessage != null) {
                assertMessageContains(errorMessage, e);
            }
        }
    }

    private static void assertMessageContains(String text, Exception e) {
        Assert.assertTrue("Expected error message to contain '" + text + "', but got '" + e.getMessage() + "'", e.getMessage().contains(text));
    }

    private static String replaceValues(String query, Object[] values) {
        StringBuilder sb = new StringBuilder();
        int last = 0;
        int i = 0;
        int idx;
        while ((idx = query.indexOf('?', last)) > 0) {
            if (i >= values.length)
                throw new IllegalArgumentException(String.format("Not enough values provided. The query has at least %d variables but only %d values provided", i, values.length));
            sb.append(query.substring(last, idx));
            Object value = values[i++];
            if (idx >= 3 && value instanceof List && query.substring(idx - 3, idx).equalsIgnoreCase("IN ")) {
                List l = (List) value;
                sb.append("(");
                for (int j = 0; j < l.size(); j++) {
                    if (j > 0)
                        sb.append(", ");
                    sb.append(formatForCQL(l.get(j)));
                }
                sb.append(")");
            } else {
                sb.append(formatForCQL(value));
            }
            last = idx + 1;
        }
        sb.append(query.substring(last));
        return sb.toString();
    }

    private static Object[] transformValues(Object[] values) {
        Object[] buffers = new ByteBuffer[values.length];
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            if (value == null) {
                buffers[i] = null;
                continue;
            }
            try {
                buffers[i] = typeFor(value).decompose(serializeTuples(value));
            } catch (Exception ex) {
                logger.info("Error serializing query parameter {}:", value, ex);
                throw ex;
            }
        }
        return buffers;
    }

    private static Object serializeTuples(Object value) {
        if (value instanceof TupleValue) {
            return ((TupleValue) value).toByteBuffer();
        }
        if (value instanceof List) {
            List l = (List) value;
            List n = new ArrayList(l.size());
            for (Object o : l) n.add(serializeTuples(o));
            return n;
        }
        if (value instanceof Set) {
            Set s = (Set) value;
            Set n = new LinkedHashSet(s.size());
            for (Object o : s) n.add(serializeTuples(o));
            return n;
        }
        if (value instanceof Map) {
            Map m = (Map) value;
            Map n = new LinkedHashMap(m.size());
            for (Object entry : m.entrySet()) n.put(serializeTuples(((Map.Entry) entry).getKey()), serializeTuples(((Map.Entry) entry).getValue()));
            return n;
        }
        return value;
    }

    private static String formatAllValues(Object[] values) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(formatForCQL(values[i]));
        }
        sb.append("]");
        return sb.toString();
    }

    private static String formatForCQL(Object value) {
        if (value == null)
            return "null";
        if (value instanceof TupleValue)
            return ((TupleValue) value).toCQLString();
        if (value instanceof Collection || value instanceof Map) {
            StringBuilder sb = new StringBuilder();
            if (value instanceof List) {
                List l = (List) value;
                sb.append("[");
                for (int i = 0; i < l.size(); i++) {
                    if (i > 0)
                        sb.append(", ");
                    sb.append(formatForCQL(l.get(i)));
                }
                sb.append("]");
            } else if (value instanceof Set) {
                Set s = (Set) value;
                sb.append("{");
                Iterator iter = s.iterator();
                while (iter.hasNext()) {
                    sb.append(formatForCQL(iter.next()));
                    if (iter.hasNext())
                        sb.append(", ");
                }
                sb.append("}");
            } else {
                Map m = (Map) value;
                sb.append("{");
                Iterator iter = m.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    sb.append(formatForCQL(entry.getKey())).append(": ").append(formatForCQL(entry.getValue()));
                    if (iter.hasNext())
                        sb.append(", ");
                }
                sb.append("}");
            }
            return sb.toString();
        }
        AbstractType type = typeFor(value);
        String s = type.getString(type.decompose(value));
        if (type instanceof UTF8Type)
            return String.format("'%s'", s.replaceAll("'", "''"));
        if (type instanceof BytesType)
            return "0x" + s;
        return s;
    }

    private static ByteBuffer makeByteBuffer(Object value, AbstractType type) {
        if (value == null)
            return null;
        if (value instanceof TupleValue)
            return ((TupleValue) value).toByteBuffer();
        if (value instanceof ByteBuffer)
            return (ByteBuffer) value;
        return type.decompose(value);
    }

    private static String formatValue(ByteBuffer bb, AbstractType<?> type) {
        if (bb == null)
            return "null";
        if (type instanceof CollectionType) {
            TypeSerializer ser = type.getSerializer();
            return ser.toString(ser.deserialize(bb));
        }
        return type.getString(bb);
    }

    protected Object tuple(Object... values) {
        return new TupleValue(values);
    }

    protected Object userType(Object... values) {
        return new TupleValue(values).toByteBuffer();
    }

    protected Object list(Object... values) {
        return Arrays.asList(values);
    }

    protected Object set(Object... values) {
        return ImmutableSet.copyOf(values);
    }

    protected Object map(Object... values) {
        if (values.length % 2 != 0)
            throw new IllegalArgumentException();
        int size = values.length / 2;
        Map m = new LinkedHashMap(size);
        for (int i = 0; i < size; i++) m.put(values[2 * i], values[(2 * i) + 1]);
        return m;
    }

    private static AbstractType typeFor(Object value) {
        if (value instanceof ByteBuffer || value instanceof TupleValue || value == null)
            return BytesType.instance;
        if (value instanceof Integer)
            return Int32Type.instance;
        if (value instanceof Long)
            return LongType.instance;
        if (value instanceof Float)
            return FloatType.instance;
        if (value instanceof Double)
            return DoubleType.instance;
        if (value instanceof BigInteger)
            return IntegerType.instance;
        if (value instanceof BigDecimal)
            return DecimalType.instance;
        if (value instanceof String)
            return UTF8Type.instance;
        if (value instanceof Boolean)
            return BooleanType.instance;
        if (value instanceof InetAddress)
            return InetAddressType.instance;
        if (value instanceof Date)
            return TimestampType.instance;
        if (value instanceof UUID)
            return UUIDType.instance;
        if (value instanceof List) {
            List l = (List) value;
            AbstractType elt = l.isEmpty() ? BytesType.instance : typeFor(l.get(0));
            return ListType.getInstance(elt, true);
        }
        if (value instanceof Set) {
            Set s = (Set) value;
            AbstractType elt = s.isEmpty() ? BytesType.instance : typeFor(s.iterator().next());
            return SetType.getInstance(elt, true);
        }
        if (value instanceof Map) {
            Map m = (Map) value;
            AbstractType keys, values;
            if (m.isEmpty()) {
                keys = BytesType.instance;
                values = BytesType.instance;
            } else {
                Map.Entry entry = (Map.Entry) m.entrySet().iterator().next();
                keys = typeFor(entry.getKey());
                values = typeFor(entry.getValue());
            }
            return MapType.getInstance(keys, values, true);
        }
        throw new IllegalArgumentException("Unsupported value type (value is " + value + ")");
    }

    private static class TupleValue {

        private final Object[] values;

        TupleValue(Object[] values) {
            this.values = values;
        }

        public ByteBuffer toByteBuffer() {
            ByteBuffer[] bbs = new ByteBuffer[values.length];
            for (int i = 0; i < values.length; i++) bbs[i] = makeByteBuffer(values[i], typeFor(values[i]));
            return TupleType.buildValue(bbs);
        }

        public String toCQLString() {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i = 0; i < values.length; i++) {
                if (i > 0)
                    sb.append(", ");
                sb.append(formatForCQL(values[i]));
            }
            sb.append(")");
            return sb.toString();
        }

        public String toString() {
            return "TupleValue" + toCQLString();
        }
    }
}
