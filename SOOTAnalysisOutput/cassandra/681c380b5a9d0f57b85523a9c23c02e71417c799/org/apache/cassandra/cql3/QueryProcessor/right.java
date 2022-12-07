package org.apache.cassandra.cql3;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import com.google.common.primitives.Ints;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.EntryWeigher;
import com.googlecode.concurrentlinkedhashmap.EvictionListener;
import org.antlr.runtime.*;
import org.github.jamm.MemoryMeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.cql3.statements.*;
import org.apache.cassandra.db.*;
import org.apache.cassandra.exceptions.*;
import org.apache.cassandra.metrics.CqlStatementMetrics;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.service.QueryState;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.thrift.ThriftClientState;
import org.apache.cassandra.tracing.Tracing;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.MD5Digest;
import org.apache.cassandra.utils.SemanticVersion;

public class QueryProcessor implements QueryHandler {

    public static final SemanticVersion CQL_VERSION = new SemanticVersion("3.1.7");

    public static final QueryProcessor instance = new QueryProcessor();

    private static final Logger logger = LoggerFactory.getLogger(QueryProcessor.class);

    private static final MemoryMeter meter = new MemoryMeter();

    private static final long MAX_CACHE_PREPARED_MEMORY = Runtime.getRuntime().maxMemory() / 256;

    private static final int MAX_CACHE_PREPARED_COUNT = 10000;

    private static EntryWeigher<MD5Digest, CQLStatement> cqlMemoryUsageWeigher = new EntryWeigher<MD5Digest, CQLStatement>() {

        @Override
        public int weightOf(MD5Digest key, CQLStatement value) {
            return Ints.checkedCast(measure(key) + measure(value));
        }
    };

    private static EntryWeigher<Integer, CQLStatement> thriftMemoryUsageWeigher = new EntryWeigher<Integer, CQLStatement>() {

        @Override
        public int weightOf(Integer key, CQLStatement value) {
            return Ints.checkedCast(measure(key) + measure(value));
        }
    };

    private static final ConcurrentLinkedHashMap<MD5Digest, CQLStatement> preparedStatements;

    private static final ConcurrentLinkedHashMap<Integer, CQLStatement> thriftPreparedStatements;

    public static final CqlStatementMetrics metrics = new CqlStatementMetrics();

    private static AtomicLong evictionCount = new AtomicLong(0);

    static {
        if (MemoryMeter.isInitialized()) {
            preparedStatements = new ConcurrentLinkedHashMap.Builder<MD5Digest, CQLStatement>().maximumWeightedCapacity(MAX_CACHE_PREPARED_MEMORY).weigher(cqlMemoryUsageWeigher).listener(new EvictionListener<MD5Digest, CQLStatement>() {

                @Override
                public void onEviction(MD5Digest md5Digest, CQLStatement prepared) {
                    metrics.activePreparedStatements.dec();
                    metrics.evictedPreparedStatements.inc();
                    evictionCount.incrementAndGet();
                }
            }).build();
            thriftPreparedStatements = new ConcurrentLinkedHashMap.Builder<Integer, CQLStatement>().maximumWeightedCapacity(MAX_CACHE_PREPARED_MEMORY).weigher(thriftMemoryUsageWeigher).listener(new EvictionListener<Integer, CQLStatement>() {

                @Override
                public void onEviction(Integer i, CQLStatement prepared) {
                    metrics.activePreparedStatements.dec();
                    metrics.evictedPreparedStatements.inc();
                    evictionCount.incrementAndGet();
                }
            }).build();
        } else {
            logger.error("Unable to initialize MemoryMeter (jamm not specified as javaagent).  This means " + "Cassandra will be unable to measure object sizes accurately and may consequently OOM.");
            preparedStatements = new ConcurrentLinkedHashMap.Builder<MD5Digest, CQLStatement>().maximumWeightedCapacity(MAX_CACHE_PREPARED_COUNT).build();
            thriftPreparedStatements = new ConcurrentLinkedHashMap.Builder<Integer, CQLStatement>().maximumWeightedCapacity(MAX_CACHE_PREPARED_COUNT).build();
        }
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

    private QueryProcessor() {
    }

    public CQLStatement getPrepared(MD5Digest id) {
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

    public static void validateCellNames(Iterable<ByteBuffer> cellNames) throws InvalidRequestException {
        for (ByteBuffer name : cellNames) validateCellName(name);
    }

    public static void validateCellName(ByteBuffer name) throws InvalidRequestException {
        if (name.remaining() > Column.MAX_NAME_LENGTH)
            throw new InvalidRequestException(String.format("The sum of all clustering columns is too long (%s > %s)", name.remaining(), Column.MAX_NAME_LENGTH));
        if (name.remaining() == 0)
            throw new InvalidRequestException("Invalid empty value for clustering column of COMPACT TABLE");
    }

    public static ResultMessage processStatement(CQLStatement statement, QueryState queryState, QueryOptions options) throws RequestExecutionException, RequestValidationException {
        logger.trace("Process {} @CL.{}", statement, options.getConsistency());
        ClientState clientState = queryState.getClientState();
        statement.checkAccess(clientState);
        statement.validate(clientState);
        ResultMessage result = statement.execute(queryState, options);
        return result == null ? new ResultMessage.Void() : result;
    }

    public static ResultMessage process(String queryString, ConsistencyLevel cl, QueryState queryState) throws RequestExecutionException, RequestValidationException {
        return instance.process(queryString, queryState, new QueryOptions(cl, Collections.<ByteBuffer>emptyList()));
    }

    public ResultMessage process(String queryString, QueryState queryState, QueryOptions options) throws RequestExecutionException, RequestValidationException {
        CQLStatement prepared = getStatement(queryString, queryState.getClientState()).statement;
        if (prepared.getBoundTerms() != options.getValues().size())
            throw new InvalidRequestException("Invalid amount of bind variables");
        if (!queryState.getClientState().isInternal)
            metrics.executedUnprepared.inc();
        return processStatement(prepared, queryState, options);
    }

    public static CQLStatement parseStatement(String queryStr, QueryState queryState) throws RequestValidationException {
        return getStatement(queryStr, queryState.getClientState()).statement;
    }

    public static UntypedResultSet process(String query, ConsistencyLevel cl) throws RequestExecutionException {
        try {
            ResultMessage result = instance.process(query, QueryState.forInternalCalls(), new QueryOptions(cl, Collections.<ByteBuffer>emptyList()));
            if (result instanceof ResultMessage.Rows)
                return new UntypedResultSet(((ResultMessage.Rows) result).result);
            else
                return null;
        } catch (RequestValidationException e) {
            throw new RuntimeException(e);
        }
    }

    public static UntypedResultSet processInternal(String query) {
        try {
            ClientState state = ClientState.forInternalCalls();
            QueryState qState = new QueryState(state);
            state.setKeyspace(Keyspace.SYSTEM_KS);
            CQLStatement statement = getStatement(query, state).statement;
            statement.validate(state);
            ResultMessage result = statement.executeInternal(qState, QueryOptions.DEFAULT);
            if (result instanceof ResultMessage.Rows)
                return new UntypedResultSet(((ResultMessage.Rows) result).result);
            else
                return null;
        } catch (RequestExecutionException e) {
            throw new RuntimeException(e);
        } catch (RequestValidationException e) {
            throw new RuntimeException("Error validating " + query, e);
        }
    }

    public static UntypedResultSet resultify(String query, Row row) {
        try {
            SelectStatement ss = (SelectStatement) getStatement(query, null).statement;
            ResultSet cqlRows = ss.process(Collections.singletonList(row));
            return new UntypedResultSet(cqlRows);
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
            metrics.activePreparedStatements.inc();
            logger.trace(String.format("Stored prepared statement #%d with %d bind markers", statementId, prepared.statement.getBoundTerms()));
            return ResultMessage.Prepared.forThrift(statementId, prepared.boundNames);
        } else {
            MD5Digest statementId = MD5Digest.compute(toHash);
            preparedStatements.put(statementId, prepared.statement);
            metrics.activePreparedStatements.inc();
            logger.trace(String.format("Stored prepared statement %s with %d bind markers", statementId, prepared.statement.getBoundTerms()));
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
        metrics.executedPrepared.inc();
        return processStatement(statement, queryState, options);
    }

    public ResultMessage processBatch(BatchStatement batch, QueryState queryState, BatchQueryOptions options) throws RequestExecutionException, RequestValidationException {
        ClientState clientState = queryState.getClientState();
        batch.checkAccess(clientState);
        batch.validate(clientState);
        return batch.executeWithPerStatementVariables(options.getConsistency(), queryState, options.getValues());
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
            CharStream stream = new ANTLRStringStream(queryStr);
            CqlLexer lexer = new CqlLexer(stream);
            TokenStream tokenStream = new CommonTokenStream(lexer);
            CqlParser parser = new CqlParser(tokenStream);
            ParsedStatement statement = parser.query();
            lexer.throwLastRecognitionError();
            parser.throwLastRecognitionError();
            return statement;
        } catch (RuntimeException re) {
            throw new SyntaxException(String.format("Failed parsing statement: [%s] reason: %s %s", queryStr, re.getClass().getSimpleName(), re.getMessage()));
        } catch (RecognitionException e) {
            throw new SyntaxException("Invalid or malformed CQL query string: " + e.getMessage());
        }
    }

    private static long measure(Object key) {
        if (!MemoryMeter.isInitialized())
            return 1;
        return key instanceof MeasurableForPreparedCache ? ((MeasurableForPreparedCache) key).measureForPreparedCache(meter) : meter.measureDeep(key);
    }
}
