package org.apache.cassandra.cql3;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.Ints;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.EntryWeigher;
import org.antlr.runtime.*;
import org.github.jamm.MemoryMeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.cql3.statements.*;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.composites.*;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.exceptions.*;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.service.pager.QueryPager;
import org.apache.cassandra.service.pager.QueryPagers;
import org.apache.cassandra.thrift.ThriftClientState;
import org.apache.cassandra.tracing.Tracing;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.MD5Digest;
import org.apache.cassandra.utils.SemanticVersion;

public class QueryProcessor implements QueryHandler {

    public static final SemanticVersion CQL_VERSION = new SemanticVersion("3.2.0");

    public static final QueryProcessor instance = new QueryProcessor();

    private static final Logger logger = LoggerFactory.getLogger(QueryProcessor.class);

    private static final MemoryMeter meter = new MemoryMeter().withGuessing(MemoryMeter.Guess.FALLBACK_BEST);

    private static final long MAX_CACHE_PREPARED_MEMORY = Runtime.getRuntime().maxMemory() / 256;

    private static EntryWeigher<MD5Digest, ParsedStatement.Prepared> cqlMemoryUsageWeigher = new EntryWeigher<MD5Digest, ParsedStatement.Prepared>() {

        @Override
        public int weightOf(MD5Digest key, ParsedStatement.Prepared value) {
            return Ints.checkedCast(measure(key) + measure(value.statement) + measure(value.boundNames));
        }
    };

    private static EntryWeigher<Integer, CQLStatement> thriftMemoryUsageWeigher = new EntryWeigher<Integer, CQLStatement>() {

        @Override
        public int weightOf(Integer key, CQLStatement value) {
            return Ints.checkedCast(measure(key) + measure(value));
        }
    };

    private static final ConcurrentLinkedHashMap<MD5Digest, ParsedStatement.Prepared> preparedStatements;

    private static final ConcurrentLinkedHashMap<Integer, CQLStatement> thriftPreparedStatements;

    private static final ConcurrentMap<String, ParsedStatement.Prepared> internalStatements = new ConcurrentHashMap<>();<<<<<<< MINE
@VisibleForTesting
    public static final CqlStatementMetrics metrics = new CqlStatementMetrics();
=======
>>>>>>> YOURS


<<<<<<< MINE
static {
        preparedStatements = new ConcurrentLinkedHashMap.Builder<MD5Digest, ParsedStatement.Prepared>().maximumWeightedCapacity(MAX_CACHE_PREPARED_MEMORY).weigher(cqlMemoryUsageWeigher).listener(new EvictionListener<MD5Digest, ParsedStatement.Prepared>() {

            @Override
            public void onEviction(MD5Digest md5Digest, ParsedStatement.Prepared prepared) {
                metrics.activePreparedStatements.dec();
                metrics.evictedPreparedStatements.inc();
                evictionCount.incrementAndGet();
            }
        }).build();
        thriftPreparedStatements = new ConcurrentLinkedHashMap.Builder<Integer, CQLStatement>().maximumWeightedCapacity(MAX_CACHE_PREPARED_MEMORY).weigher(thriftMemoryUsageWeigher).listener(new EvictionListener<Integer, CQLStatement>() {

            @Override
            public void onEviction(Integer integer, CQLStatement cqlStatement) {
                metrics.activePreparedStatements.dec();
                metrics.evictedPreparedStatements.inc();
                evictionCount.incrementAndGet();
            }
        }).build();
        StorageService.scheduledTasks.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                long count = evictionCount.getAndSet(0);
                if (count > 0) {
                    logger.info("{} prepared statements discarded in the last minute because cache limit reached (cache limit = {} bytes)", count, MAX_CACHE_PREPARED_MEMORY);
                }
            }
        }, 1, 1, TimeUnit.MINUTES);
    }
=======
>>>>>>> YOURS


    private static enum InternalStateInstance {

        INSTANCE;

        private final QueryState queryState;

        InternalStateInstance() {
            ClientState state = ClientState.forInternalCalls();
            try {
                state.setKeyspace(Keyspace.SYSTEM_KS);
            } catch (InvalidRequestException e) {
                throw new RuntimeException();
            }
            this.queryState = new QueryState(state);
        }
    }

    private static QueryState internalQueryState() {
        return InternalStateInstance.INSTANCE.queryState;
    }

    static {
        if (MemoryMeter.isInitialized()) {
            preparedStatements = new ConcurrentLinkedHashMap.Builder<MD5Digest, CQLStatement>().maximumWeightedCapacity(MAX_CACHE_PREPARED_MEMORY).weigher(cqlMemoryUsageWeigher).build();
            thriftPreparedStatements = new ConcurrentLinkedHashMap.Builder<Integer, CQLStatement>().maximumWeightedCapacity(MAX_CACHE_PREPARED_MEMORY).weigher(thriftMemoryUsageWeigher).build();
        } else {
            logger.error("Unable to initialize MemoryMeter (jamm not specified as javaagent).  This means " + "Cassandra will be unable to measure object sizes accurately and may consequently OOM.");
            preparedStatements = new ConcurrentLinkedHashMap.Builder<MD5Digest, CQLStatement>().maximumWeightedCapacity(MAX_CACHE_PREPARED_COUNT).build();
            thriftPreparedStatements = new ConcurrentLinkedHashMap.Builder<Integer, CQLStatement>().maximumWeightedCapacity(MAX_CACHE_PREPARED_COUNT).build();
        }
    }

    private QueryProcessor() {
    }

    public ParsedStatement.Prepared getPrepared(MD5Digest id) {
        return preparedStatements.get(id);
    }

    public CQLStatement getPreparedForThrift(Integer id) {
        return thriftPreparedStatements.get(id);
    }

    public static void validateKey(ByteBuffer key) throws InvalidRequestException {
        if (key == null || key.remaining() == 0) {
            throw new InvalidRequestException("Key may not be empty");
        }
        if (key.remaining() > FBUtilities.MAX_UNSIGNED_SHORT) {
            throw new InvalidRequestException("Key length of " + key.remaining() + " is longer than maximum of " + FBUtilities.MAX_UNSIGNED_SHORT);
        }
    }

    public static void validateCellNames(Iterable<CellName> cellNames, CellNameType type) throws InvalidRequestException {
        for (CellName name : cellNames) validateCellName(name, type);
    }

    public static void validateCellName(CellName name, CellNameType type) throws InvalidRequestException {
        validateComposite(name, type);
        if (name.isEmpty())
            throw new InvalidRequestException("Invalid empty value for clustering column of COMPACT TABLE");
    }

    public static void validateComposite(Composite name, CType type) throws InvalidRequestException {
        long serializedSize = type.serializer().serializedSize(name, TypeSizes.NATIVE);
        if (serializedSize > Cell.MAX_NAME_LENGTH)
            throw new InvalidRequestException(String.format("The sum of all clustering columns is too long (%s > %s)", serializedSize, Cell.MAX_NAME_LENGTH));
    }

    private static ResultMessage processStatement(CQLStatement statement, QueryState queryState, QueryOptions options) throws RequestExecutionException, RequestValidationException {
        logger.trace("Process {} @CL.{}", statement, options.getConsistency());
        ClientState clientState = queryState.getClientState();
        statement.checkAccess(clientState);
        statement.validate(clientState);
        ResultMessage result = statement.execute(queryState, options);
        return result == null ? new ResultMessage.Void() : result;
    }

    public static ResultMessage process(String queryString, ConsistencyLevel cl, QueryState queryState) throws RequestExecutionException, RequestValidationException {
        return instance.process(queryString, queryState, QueryOptions.forInternalCalls(cl, Collections.<ByteBuffer>emptyList()));
    }

    public ResultMessage process(String queryString, QueryState queryState, QueryOptions options) throws RequestExecutionException, RequestValidationException {
        ParsedStatement.Prepared p = getStatement(queryString, queryState.getClientState());
        options.prepare(p.boundNames);
        CQLStatement prepared = p.statement;
        if (prepared.getBoundTerms() != options.getValues().size())
            throw new InvalidRequestException("Invalid amount of bind variables");
        return processStatement(prepared, queryState, options);
    }

    public static ParsedStatement.Prepared parseStatement(String queryStr, QueryState queryState) throws RequestValidationException {
        return getStatement(queryStr, queryState.getClientState());
    }

    public static UntypedResultSet process(String query, ConsistencyLevel cl) throws RequestExecutionException {
        try {
            ResultMessage result = instance.process(query, QueryState.forInternalCalls(), QueryOptions.forInternalCalls(cl, Collections.<ByteBuffer>emptyList()));
            if (result instanceof ResultMessage.Rows)
                return UntypedResultSet.create(((ResultMessage.Rows) result).result);
            else
                return null;
        } catch (RequestValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private static QueryOptions makeInternalOptions(ParsedStatement.Prepared prepared, Object[] values) {
        if (prepared.boundNames.size() != values.length)
            throw new IllegalArgumentException(String.format("Invalid number of values. Expecting %d but got %d", prepared.boundNames.size(), values.length));
        List<ByteBuffer> boundValues = new ArrayList<ByteBuffer>(values.length);
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            AbstractType type = prepared.boundNames.get(i).type;
            boundValues.add(value instanceof ByteBuffer || value == null ? (ByteBuffer) value : type.decompose(value));
        }
        return QueryOptions.forInternalCalls(boundValues);
    }

    private static ParsedStatement.Prepared prepareInternal(String query) throws RequestValidationException {
        ParsedStatement.Prepared prepared = internalStatements.get(query);
        if (prepared != null)
            return prepared;
        prepared = parseStatement(query, internalQueryState());
        prepared.statement.validate(internalQueryState().getClientState());
        internalStatements.putIfAbsent(query, prepared);
        return prepared;
    }

    public static UntypedResultSet executeInternal(String query, Object... values) {
        try {
            ParsedStatement.Prepared prepared = prepareInternal(query);
            ResultMessage result = prepared.statement.executeInternal(internalQueryState(), makeInternalOptions(prepared, values));
            if (result instanceof ResultMessage.Rows)
                return UntypedResultSet.create(((ResultMessage.Rows) result).result);
            else
                return null;
        } catch (RequestExecutionException e) {
            throw new RuntimeException(e);
        } catch (RequestValidationException e) {
            throw new RuntimeException("Error validating " + query, e);
        }
    }

    public static UntypedResultSet executeInternalWithPaging(String query, int pageSize, Object... values) {
        try {
            ParsedStatement.Prepared prepared = prepareInternal(query);
            if (!(prepared.statement instanceof SelectStatement))
                throw new IllegalArgumentException("Only SELECTs can be paged");
            SelectStatement select = (SelectStatement) prepared.statement;
            QueryPager pager = QueryPagers.localPager(select.getPageableCommand(makeInternalOptions(prepared, values)));
            return UntypedResultSet.create(select, pager, pageSize);
        } catch (RequestValidationException e) {
            throw new RuntimeException("Error validating query" + e);
        }
    }

    public static UntypedResultSet executeOnceInternal(String query, Object... values) {
        try {
            ParsedStatement.Prepared prepared = parseStatement(query, internalQueryState());
            prepared.statement.validate(internalQueryState().getClientState());
            ResultMessage result = prepared.statement.executeInternal(internalQueryState(), makeInternalOptions(prepared, values));
            if (result instanceof ResultMessage.Rows)
                return UntypedResultSet.create(((ResultMessage.Rows) result).result);
            else
                return null;
        } catch (RequestExecutionException e) {
            throw new RuntimeException(e);
        } catch (RequestValidationException e) {
            throw new RuntimeException("Error validating query " + query, e);
        }
    }

    public static UntypedResultSet resultify(String query, Row row) {
        return resultify(query, Collections.singletonList(row));
    }

    public static UntypedResultSet resultify(String query, List<Row> rows) {
        try {
            SelectStatement ss = (SelectStatement) getStatement(query, null).statement;
            ResultSet cqlRows = ss.process(rows);
            return UntypedResultSet.create(cqlRows);
        } catch (RequestValidationException e) {
            throw new AssertionError(e);
        }
    }

    public ResultMessage.Prepared prepare(String queryString, QueryState queryState) throws RequestValidationException {
        ClientState cState = queryState.getClientState();
        return prepare(queryString, cState, cState instanceof ThriftClientState);
    }

    public static ResultMessage.Prepared prepare(String queryString, ClientState clientState, boolean forThrift) throws RequestValidationException {
        ParsedStatement.Prepared prepared = getStatement(queryString, clientState);
        int boundTerms = prepared.statement.getBoundTerms();
        if (boundTerms > FBUtilities.MAX_UNSIGNED_SHORT)
            throw new InvalidRequestException(String.format("Too many markers(?). %d markers exceed the allowed maximum of %d", boundTerms, FBUtilities.MAX_UNSIGNED_SHORT));
        assert boundTerms == prepared.boundNames.size();
        return storePreparedStatement(queryString, clientState.getRawKeyspace(), prepared, forThrift);
    }

    private static ResultMessage.Prepared storePreparedStatement(String queryString, String keyspace, ParsedStatement.Prepared prepared, boolean forThrift) throws InvalidRequestException {
        String toHash = keyspace == null ? queryString : keyspace + queryString;
        long statementSize = measure(prepared.statement);
        if (statementSize > MAX_CACHE_PREPARED_MEMORY)
            throw new InvalidRequestException(String.format("Prepared statement of size %d bytes is larger than allowed maximum of %d bytes.", statementSize, MAX_CACHE_PREPARED_MEMORY));
        if (forThrift) {
            int statementId = toHash.hashCode();
            thriftPreparedStatements.put(statementId, prepared.statement);
<<<<<<< MINE
            metrics.activePreparedStatements.inc();
            logger.trace("Stored prepared statement #{} with {} bind markers", statementId, prepared.statement.getBoundTerms());
=======
            logger.trace(String.format("Stored prepared statement #%d with %d bind markers", statementId, prepared.statement.getBoundTerms()));
>>>>>>> YOURS
            return ResultMessage.Prepared.forThrift(statementId, prepared.boundNames);
        } else {
            MD5Digest statementId = MD5Digest.compute(toHash);
<<<<<<< MINE
            preparedStatements.put(statementId, prepared);
            metrics.activePreparedStatements.inc();
            logger.trace("Stored prepared statement #{} with {} bind markers", statementId, prepared.statement.getBoundTerms());
=======
            preparedStatements.put(statementId, prepared.statement);
            logger.trace(String.format("Stored prepared statement %s with %d bind markers", statementId, prepared.statement.getBoundTerms()));
>>>>>>> YOURS
            return new ResultMessage.Prepared(statementId, prepared);
        }
    }

    public ResultMessage processPrepared(CQLStatement statement, QueryState queryState, QueryOptions options) throws RequestExecutionException, RequestValidationException {
        List<ByteBuffer> variables = options.getValues();
        if (!(variables.isEmpty() && (statement.getBoundTerms() == 0))) {
            if (variables.size() != statement.getBoundTerms())
                throw new InvalidRequestException(String.format("there were %d markers(?) in CQL but %d bound variables", statement.getBoundTerms(), variables.size()));
            if (logger.isTraceEnabled())
                for (int i = 0; i < variables.size(); i++) logger.trace("[{}] '{}'", i + 1, variables.get(i));
        }
        return processStatement(statement, queryState, options);
    }

    public ResultMessage processBatch(BatchStatement batch, QueryState queryState, BatchQueryOptions options) throws RequestExecutionException, RequestValidationException {
        ClientState clientState = queryState.getClientState();
        batch.checkAccess(clientState);
        batch.validate();
        batch.validate(clientState);
        return batch.execute(queryState, options);
    }

    public static ParsedStatement.Prepared getStatement(String queryStr, ClientState clientState) throws RequestValidationException {
        Tracing.trace("Parsing {}", queryStr);
        ParsedStatement statement = parseStatement(queryStr);
        if (statement instanceof CFStatement)
            ((CFStatement) statement).prepareKeyspace(clientState);
        Tracing.trace("Preparing statement");
        return statement.prepare();
    }

    public static ParsedStatement parseStatement(String queryStr) throws SyntaxException {
        try {
            ErrorCollector errorCollector = new ErrorCollector(queryStr);
            CharStream stream = new ANTLRStringStream(queryStr);
            CqlLexer lexer = new CqlLexer(stream);
            lexer.addErrorListener(errorCollector);
            TokenStream tokenStream = new CommonTokenStream(lexer);
            CqlParser parser = new CqlParser(tokenStream);
            parser.addErrorListener(errorCollector);
            ParsedStatement statement = parser.query();
            errorCollector.throwLastSyntaxError();
            return statement;
        } catch (RuntimeException re) {
            throw new SyntaxException(String.format("Failed parsing statement: [%s] reason: %s %s", queryStr, re.getClass().getSimpleName(), re.getMessage()));
        } catch (RecognitionException e) {
            throw new SyntaxException("Invalid or malformed CQL query string: " + e.getMessage());
        }
    }

    private static long measure(Object key) {
        return key instanceof MeasurableForPreparedCache ? ((MeasurableForPreparedCache) key).measureForPreparedCache(meter) : meter.measureDeep(key);
    }
}