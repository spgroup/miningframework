package org.apache.cassandra.cql3;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.MoreExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.antlr.runtime.*;
import org.apache.cassandra.concurrent.ScheduledExecutors;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.schema.Schema;
import org.apache.cassandra.schema.SchemaChangeListener;
import org.apache.cassandra.schema.SchemaConstants;
import org.apache.cassandra.config.SchemaConstants;
import org.apache.cassandra.cql3.functions.Function;
import org.apache.cassandra.cql3.functions.FunctionName;
import org.apache.cassandra.cql3.statements.*;
import org.apache.cassandra.db.*;
import org.apache.cassandra.db.rows.RowIterator;
import org.apache.cassandra.db.partitions.PartitionIterator;
import org.apache.cassandra.db.partitions.PartitionIterators;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.exceptions.*;
import org.apache.cassandra.metrics.CQLMetrics;
import org.apache.cassandra.service.*;
import org.apache.cassandra.service.pager.QueryPager;
import org.apache.cassandra.tracing.Tracing;
import org.apache.cassandra.transport.ProtocolVersion;
import org.apache.cassandra.transport.messages.ResultMessage;
import org.apache.cassandra.utils.*;
import static org.apache.cassandra.cql3.statements.RequestValidations.checkTrue;

public class QueryProcessor implements QueryHandler {<<<<<<< MINE

=======


  private static class MigrationSubscriber extends MigrationListener {

    private static void removeInvalidPreparedStatements(String ksName, String cfName) {
      removeInvalidPreparedStatements(internalStatements.values().iterator(), ksName, cfName);
      removeInvalidPersistentPreparedStatements(preparedStatements.entrySet().iterator(), ksName, cfName);
      removeInvalidPreparedStatements(thriftPreparedStatements.values().iterator(), ksName, cfName);
    }

    private static void removeInvalidPreparedStatementsForFunction(String ksName, String functionName) {
      Predicate<Function> matchesFunction = f -> ksName.equals(f.name().keyspace) && functionName.equals(f.name().name);
      for (Iterator<Map.Entry<MD5Digest, ParsedStatement.Prepared>> iter = preparedStatements.entrySet().iterator(); iter.hasNext(); ) {
        Map.Entry<MD5Digest, ParsedStatement.Prepared> pstmt = iter.next();
        if (Iterables.any(pstmt.getValue().statement.getFunctions(), matchesFunction)) {
          SystemKeyspace.removePreparedStatement(pstmt.getKey());
          iter.remove();
        }
      }
      Iterators.removeIf(internalStatements.values().iterator(), statement -> Iterables.any(statement.statement.getFunctions(), matchesFunction));
      Iterators.removeIf(thriftPreparedStatements.values().iterator(), statement -> Iterables.any(statement.statement.getFunctions(), matchesFunction));
    }

    private static void removeInvalidPersistentPreparedStatements(Iterator<Map.Entry<MD5Digest, ParsedStatement.Prepared>> iterator, String ksName, String cfName) {
      while (iterator.hasNext()) {
        Map.Entry<MD5Digest, ParsedStatement.Prepared> entry = iterator.next();
        if (shouldInvalidate(ksName, cfName, entry.getValue().statement)) {
          SystemKeyspace.removePreparedStatement(entry.getKey());
          iterator.remove();
        }
      }
    }

    private static void removeInvalidPreparedStatements(Iterator<ParsedStatement.Prepared> iterator, String ksName, String cfName) {
      while (iterator.hasNext()) {
        if (shouldInvalidate(ksName, cfName, iterator.next().statement))
          iterator.remove();
      }
    }

    private static boolean shouldInvalidate(String ksName, String cfName, CQLStatement statement) {
      String statementKsName;
      String statementCfName;
      if (statement instanceof ModificationStatement) {
        ModificationStatement modificationStatement = ((ModificationStatement) statement);
        statementKsName = modificationStatement.keyspace();
        statementCfName = modificationStatement.columnFamily();
      } else if (statement instanceof SelectStatement) {
        SelectStatement selectStatement = ((SelectStatement) statement);
        statementKsName = selectStatement.keyspace();
        statementCfName = selectStatement.columnFamily();
      } else if (statement instanceof BatchStatement) {
        BatchStatement batchStatement = ((BatchStatement) statement);
        for (ModificationStatement stmt : batchStatement.getStatements()) {
          if (shouldInvalidate(ksName, cfName, stmt))
            return true;
        }
        return false;
      } else {
        return false;
      }
      return ksName.equals(statementKsName) && (cfName == null || cfName.equals(statementCfName));
    }

    public void onCreateFunction(String ksName, String functionName, List<AbstractType<?>> argTypes) {
      onCreateFunctionInternal(ksName, functionName, argTypes);
    }

    public void onCreateAggregate(String ksName, String aggregateName, List<AbstractType<?>> argTypes) {
      onCreateFunctionInternal(ksName, aggregateName, argTypes);
    }

    private static void onCreateFunctionInternal(String ksName, String functionName, List<AbstractType<?>> argTypes) {
      if (Schema.instance.getKSMetaData(ksName).functions.get(new FunctionName(ksName, functionName)).size() > 1)
        removeInvalidPreparedStatementsForFunction(ksName, functionName);
    }

    public void onUpdateColumnFamily(String ksName, String cfName, boolean affectsStatements) {
      logger.trace("Column definitions for {}.{} changed, invalidating related prepared statements", ksName, cfName);
      if (affectsStatements)
        removeInvalidPreparedStatements(ksName, cfName);
    }

    public void onUpdateFunction(String ksName, String functionName, List<AbstractType<?>> argTypes) {
      removeInvalidPreparedStatementsForFunction(ksName, functionName);
    }

    public void onUpdateAggregate(String ksName, String aggregateName, List<AbstractType<?>> argTypes) {
      removeInvalidPreparedStatementsForFunction(ksName, aggregateName);
    }

    public void onDropKeyspace(String ksName) {
      logger.trace("Keyspace {} was dropped, invalidating related prepared statements", ksName);
      removeInvalidPreparedStatements(ksName, null);
    }

    public void onDropColumnFamily(String ksName, String cfName) {
      logger.trace("Table {}.{} was dropped, invalidating related prepared statements", ksName, cfName);
      removeInvalidPreparedStatements(ksName, cfName);
    }

    public void onDropFunction(String ksName, String functionName, List<AbstractType<?>> argTypes) {
      removeInvalidPreparedStatementsForFunction(ksName, functionName);
    }

    public void onDropAggregate(String ksName, String aggregateName, List<AbstractType<?>> argTypes) {
      removeInvalidPreparedStatementsForFunction(ksName, aggregateName);
    }
  }
>>>>>>> YOURS

<<<<<<< MINE
public static final CassandraVersion CQL_VERSION = new CassandraVersion("3.4.5");
=======
public static final CassandraVersion CQL_VERSION = new CassandraVersion("3.4.4");
>>>>>>> YOURS


    public static final QueryProcessor instance = new QueryProcessor();

    private static final Logger logger = LoggerFactory.getLogger(QueryProcessor.class);

    private static final Cache<MD5Digest, ParsedStatement.Prepared> preparedStatements;

    private static final ConcurrentMap<String, ParsedStatement.Prepared> internalStatements = new ConcurrentHashMap<>();

    public static final CQLMetrics metrics = new CQLMetrics();

    private static final AtomicInteger lastMinuteEvictionsCount = new AtomicInteger(0);

    static {
<<<<<<< MINE
        preparedStatements = Caffeine.newBuilder().executor(MoreExecutors.directExecutor()).maximumWeight(capacityToBytes(DatabaseDescriptor.getPreparedStatementsCacheSizeMB())).weigher(QueryProcessor::measure).removalListener((key, prepared, cause) -> {
            MD5Digest md5Digest = (MD5Digest) key;
            if (cause.wasEvicted()) {
=======
        preparedStatements = new ConcurrentLinkedHashMap.Builder<MD5Digest, ParsedStatement.Prepared>().maximumWeightedCapacity(capacityToBytes(DatabaseDescriptor.getPreparedStatementsCacheSizeMB())).weigher(QueryProcessor::measure).listener((md5Digest, prepared) -> {
>>>>>>> YOURS
                metrics.preparedStatementsEvicted.inc();
                lastMinuteEvictionsCount.incrementAndGet();
                SystemKeyspace.removePreparedStatement(md5Digest);
<<<<<<< MINE
            }
=======
>>>>>>> YOURS
        }).build();
<<<<<<< MINE
=======
        thriftPreparedStatements = new ConcurrentLinkedHashMap.Builder<Integer, ParsedStatement.Prepared>().maximumWeightedCapacity(capacityToBytes(DatabaseDescriptor.getThriftPreparedStatementsCacheSizeMB())).weigher(QueryProcessor::measure).listener((integer, prepared) -> {
            metrics.preparedStatementsEvicted.inc();
            thriftLastMinuteEvictionsCount.incrementAndGet();
        }).build();
>>>>>>> YOURS
        ScheduledExecutors.scheduledTasks.scheduleAtFixedRate(() -> {
                long count = lastMinuteEvictionsCount.getAndSet(0);
                if (count > 0)
                logger.warn("{} prepared statements discarded in the last minute because cache limit reached ({} MB)", count, DatabaseDescriptor.getPreparedStatementsCacheSizeMB());
<<<<<<< MINE
=======
            count = thriftLastMinuteEvictionsCount.getAndSet(0);
            if (count > 0)
                logger.warn("{} prepared Thrift statements discarded in the last minute because cache limit reached ({} MB)", count, DatabaseDescriptor.getThriftPreparedStatementsCacheSizeMB());
>>>>>>> YOURS
        }, 1, 1, TimeUnit.MINUTES);
<<<<<<< MINE
        logger.info("Initialized prepared statement caches with {} MB", DatabaseDescriptor.getPreparedStatementsCacheSizeMB());
=======
        logger.info("Initialized prepared statement caches with {} MB (native) and {} MB (Thrift)", DatabaseDescriptor.getPreparedStatementsCacheSizeMB(), DatabaseDescriptor.getThriftPreparedStatementsCacheSizeMB());
>>>>>>> YOURS
    }

    private static final AtomicInteger thriftLastMinuteEvictionsCount = new AtomicInteger(0);

    private static long capacityToBytes(long cacheSizeMB) {
        return cacheSizeMB * 1024 * 1024;
    }

    public static int preparedStatementsCount() {
        return preparedStatements.asMap().size();
    }

    private static enum InternalStateInstance {

        INSTANCE;

        private final QueryState queryState;

        InternalStateInstance() {
            ClientState state = ClientState.forInternalCalls();
            state.setKeyspace(SchemaConstants.SYSTEM_KEYSPACE_NAME);
            this.queryState = new QueryState(state);
        }
    }

    public static void preloadPreparedStatement() {
        ClientState clientState = ClientState.forInternalCalls();
        int count = 0;
        for (Pair<String, String> useKeyspaceAndCQL : SystemKeyspace.loadPreparedStatements()) {
            try {
                clientState.setKeyspace(useKeyspaceAndCQL.left);
<<<<<<< MINE
                prepare(useKeyspaceAndCQL.right, clientState);
=======
                prepare(useKeyspaceAndCQL.right, clientState, false);
>>>>>>> YOURS
                count++;
            } catch (RequestValidationException e) {
                logger.warn("prepared statement recreation error: {}", useKeyspaceAndCQL.right, e);
            }
        }
        logger.info("Preloaded {} prepared statements", count);
    }

    @VisibleForTesting
    public static void clearPreparedStatements(boolean memoryOnly) {
<<<<<<< MINE
        preparedStatements.invalidateAll();
=======
        preparedStatements.clear();
        thriftPreparedStatements.clear();
>>>>>>> YOURS
        if (!memoryOnly)
            SystemKeyspace.resetPreparedStatements();
    }

    private static QueryState internalQueryState() {
        return InternalStateInstance.INSTANCE.queryState;
    }

    private QueryProcessor() {
        Schema.instance.registerListener(new StatementInvalidatingListener());
    }

    public ParsedStatement.Prepared getPrepared(MD5Digest id) {
        return preparedStatements.getIfPresent(id);
    }

    public static void validateKey(ByteBuffer key) throws InvalidRequestException {
        if (key == null || key.remaining() == 0) {
            throw new InvalidRequestException("Key may not be empty");
        }
        if (key == ByteBufferUtil.UNSET_BYTE_BUFFER)
            throw new InvalidRequestException("Key may not be unset");
        if (key.remaining() > FBUtilities.MAX_UNSIGNED_SHORT) {
            throw new InvalidRequestException("Key length of " + key.remaining() + " is longer than maximum of " + FBUtilities.MAX_UNSIGNED_SHORT);
        }
    }

    public ResultMessage processStatement(CQLStatement statement, QueryState queryState, QueryOptions options, long queryStartNanoTime) throws RequestExecutionException, RequestValidationException {
        logger.trace("Process {} @CL.{}", statement, options.getConsistency());
        ClientState clientState = queryState.getClientState();
        statement.checkAccess(clientState);
        statement.validate(clientState);
        ResultMessage result = statement.execute(queryState, options, queryStartNanoTime);
        return result == null ? new ResultMessage.Void() : result;
    }

    public static ResultMessage process(String queryString, ConsistencyLevel cl, QueryState queryState, long queryStartNanoTime) throws RequestExecutionException, RequestValidationException {
        return instance.process(queryString, queryState, QueryOptions.forInternalCalls(cl, Collections.<ByteBuffer>emptyList()), queryStartNanoTime);
    }

    public ResultMessage process(String query, QueryState state, QueryOptions options, Map<String, ByteBuffer> customPayload, long queryStartNanoTime) throws RequestExecutionException, RequestValidationException {
        return process(query, state, options, queryStartNanoTime);
    }

    public ResultMessage process(String queryString, QueryState queryState, QueryOptions options, long queryStartNanoTime) throws RequestExecutionException, RequestValidationException {
<<<<<<< MINE
        ParsedStatement.Prepared p = getStatement(queryString, queryState.getClientState().cloneWithKeyspaceIfSet(options.getKeyspace()));
=======
        ParsedStatement.Prepared p = getStatement(queryString, queryState.getClientState());
>>>>>>> YOURS
        options.prepare(p.boundNames);
        CQLStatement prepared = p.statement;
        if (prepared.getBoundTerms() != options.getValues().size())
            throw new InvalidRequestException("Invalid amount of bind variables");
        if (!queryState.getClientState().isInternal)
            metrics.regularStatementsExecuted.inc();
        return processStatement(prepared, queryState, options, queryStartNanoTime);
    }

    public static ParsedStatement.Prepared parseStatement(String queryStr, ClientState clientState) throws RequestValidationException {
        return getStatement(queryStr, clientState);
    }

    public static UntypedResultSet process(String query, ConsistencyLevel cl) throws RequestExecutionException {
        return process(query, cl, Collections.<ByteBuffer>emptyList());
    }

    public static UntypedResultSet process(String query, ConsistencyLevel cl, List<ByteBuffer> values) throws RequestExecutionException {
        ResultMessage result = instance.process(query, QueryState.forInternalCalls(), QueryOptions.forInternalCalls(cl, values), System.nanoTime());
        if (result instanceof ResultMessage.Rows)
            return UntypedResultSet.create(((ResultMessage.Rows) result).result);
        else
            return null;
    }

    private static QueryOptions makeInternalOptions(ParsedStatement.Prepared prepared, Object[] values) {
        return makeInternalOptions(prepared, values, ConsistencyLevel.ONE);
    }

    private static QueryOptions makeInternalOptions(ParsedStatement.Prepared prepared, Object[] values, ConsistencyLevel cl) {
        if (prepared.boundNames.size() != values.length)
            throw new IllegalArgumentException(String.format("Invalid number of values. Expecting %d but got %d", prepared.boundNames.size(), values.length));
        List<ByteBuffer> boundValues = new ArrayList<ByteBuffer>(values.length);
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            AbstractType type = prepared.boundNames.get(i).type;
            boundValues.add(value instanceof ByteBuffer || value == null ? (ByteBuffer) value : type.decompose(value));
        }
        return QueryOptions.forInternalCalls(cl, boundValues);
    }

    public static ParsedStatement.Prepared prepareInternal(String query) throws RequestValidationException {
        ParsedStatement.Prepared prepared = internalStatements.get(query);
        if (prepared != null)
            return prepared;
        prepared = parseStatement(query, internalQueryState().getClientState());
        prepared.statement.validate(internalQueryState().getClientState());
        internalStatements.putIfAbsent(query, prepared);
        return prepared;
    }

    public static UntypedResultSet executeInternal(String query, Object... values) {
        ParsedStatement.Prepared prepared = prepareInternal(query);
        ResultMessage result = prepared.statement.executeInternal(internalQueryState(), makeInternalOptions(prepared, values));
        if (result instanceof ResultMessage.Rows)
            return UntypedResultSet.create(((ResultMessage.Rows) result).result);
        else
            return null;
    }

    public static UntypedResultSet execute(String query, ConsistencyLevel cl, Object... values) throws RequestExecutionException {
        return execute(query, cl, internalQueryState(), values);
    }

    public static UntypedResultSet execute(String query, ConsistencyLevel cl, QueryState state, Object... values) throws RequestExecutionException {
        try {
            ParsedStatement.Prepared prepared = prepareInternal(query);
            ResultMessage result = prepared.statement.execute(state, makeInternalOptions(prepared, values, cl), System.nanoTime());
            if (result instanceof ResultMessage.Rows)
                return UntypedResultSet.create(((ResultMessage.Rows) result).result);
            else
                return null;
        } catch (RequestValidationException e) {
            throw new RuntimeException("Error validating " + query, e);
        }
    }

    public static UntypedResultSet executeInternalWithPaging(String query, int pageSize, Object... values) {
        ParsedStatement.Prepared prepared = prepareInternal(query);
        if (!(prepared.statement instanceof SelectStatement))
            throw new IllegalArgumentException("Only SELECTs can be paged");
        SelectStatement select = (SelectStatement) prepared.statement;
        QueryPager pager = select.getQuery(makeInternalOptions(prepared, values), FBUtilities.nowInSeconds()).getPager(null, ProtocolVersion.CURRENT);
        return UntypedResultSet.create(select, pager, pageSize);
    }

    public static UntypedResultSet executeOnceInternal(String query, Object... values) {
        ParsedStatement.Prepared prepared = parseStatement(query, internalQueryState().getClientState());
        prepared.statement.validate(internalQueryState().getClientState());
        ResultMessage result = prepared.statement.executeInternal(internalQueryState(), makeInternalOptions(prepared, values));
        if (result instanceof ResultMessage.Rows)
            return UntypedResultSet.create(((ResultMessage.Rows) result).result);
        else
            return null;
    }

    public static UntypedResultSet executeInternalWithNow(int nowInSec, long queryStartNanoTime, String query, Object... values) {
        ParsedStatement.Prepared prepared = prepareInternal(query);
        assert prepared.statement instanceof SelectStatement;
        SelectStatement select = (SelectStatement) prepared.statement;
        ResultMessage result = select.executeInternal(internalQueryState(), makeInternalOptions(prepared, values), nowInSec, queryStartNanoTime);
        assert result instanceof ResultMessage.Rows;
        return UntypedResultSet.create(((ResultMessage.Rows) result).result);
    }

    public static UntypedResultSet resultify(String query, RowIterator partition) {
        return resultify(query, PartitionIterators.singletonIterator(partition));
    }

    public static UntypedResultSet resultify(String query, PartitionIterator partitions) {
        try (PartitionIterator iter = partitions) {
            SelectStatement ss = (SelectStatement) getStatement(query, null).statement;
            ResultSet cqlRows = ss.process(iter, FBUtilities.nowInSeconds());
            return UntypedResultSet.create(cqlRows);
        }
    }

    public ResultMessage.Prepared prepare(String query, ClientState clientState, Map<String, ByteBuffer> customPayload) throws RequestValidationException {
        return prepare(query, clientState);
    }

    public static ResultMessage.Prepared prepare(String queryString, ClientState clientState) {
        ResultMessage.Prepared existing = getStoredPreparedStatement(queryString, clientState.getRawKeyspace());
        if (existing != null)
            return existing;
        ParsedStatement.Prepared prepared = getStatement(queryString, clientState);
        prepared.rawCQLStatement = queryString;
        int boundTerms = prepared.statement.getBoundTerms();
        if (boundTerms > FBUtilities.MAX_UNSIGNED_SHORT)
            throw new InvalidRequestException(String.format("Too many markers(?). %d markers exceed the allowed maximum of %d", boundTerms, FBUtilities.MAX_UNSIGNED_SHORT));
        assert boundTerms == prepared.boundNames.size();
        return storePreparedStatement(queryString, clientState.getRawKeyspace(), prepared);
    }

    private static MD5Digest computeId(String queryString, String keyspace) {
        String toHash = keyspace == null ? queryString : keyspace + queryString;
        return MD5Digest.compute(toHash);
    }

    private static ResultMessage.Prepared getStoredPreparedStatement(String queryString, String keyspace) throws InvalidRequestException {
        MD5Digest statementId = computeId(queryString, keyspace);
        ParsedStatement.Prepared existing = preparedStatements.getIfPresent(statementId);
        if (existing == null)
            return null;
        checkTrue(queryString.equals(existing.rawCQLStatement), String.format("MD5 hash collision: query with the same MD5 hash was already prepared. \n Existing: '%s'", existing.rawCQLStatement));
        ResultSet.PreparedMetadata preparedMetadata = ResultSet.PreparedMetadata.fromPrepared(existing);
        ResultSet.ResultMetadata resultMetadata = ResultSet.ResultMetadata.fromPrepared(existing);
        return new ResultMessage.Prepared(statementId, resultMetadata.getResultMetadataId(), preparedMetadata, resultMetadata);
    }

    private static ResultMessage.Prepared storePreparedStatement(String queryString, String keyspace, ParsedStatement.Prepared prepared) throws InvalidRequestException {
        long statementSize = ObjectSizes.measureDeep(prepared.statement);
        if (statementSize > capacityToBytes(DatabaseDescriptor.getPreparedStatementsCacheSizeMB()))
            throw new InvalidRequestException(String.format("Prepared statement of size %d bytes is larger than allowed maximum of %d MB: %s...", statementSize, DatabaseDescriptor.getPreparedStatementsCacheSizeMB(), queryString.substring(0, 200)));
        MD5Digest statementId = computeId(queryString, keyspace);
        preparedStatements.put(statementId, prepared);
        SystemKeyspace.writePreparedStatement(keyspace, statementId, queryString);
        ResultSet.PreparedMetadata preparedMetadata = ResultSet.PreparedMetadata.fromPrepared(prepared);
        ResultSet.ResultMetadata resultMetadata = ResultSet.ResultMetadata.fromPrepared(prepared);
        return new ResultMessage.Prepared(statementId, resultMetadata.getResultMetadataId(), preparedMetadata, resultMetadata);
    }<<<<<<< MINE
=======
private static ResultMessage.Prepared getStoredPreparedStatement(String queryString, String keyspace, boolean forThrift) throws InvalidRequestException {
        if (forThrift) {
            Integer thriftStatementId = computeThriftId(queryString, keyspace);
            ParsedStatement.Prepared existing = thriftPreparedStatements.get(thriftStatementId);
            if (existing == null)
                return null;
            checkTrue(queryString.equals(existing.rawCQLStatement), String.format("MD5 hash collision: query with the same MD5 hash was already prepared. \n Existing: '%s'", existing.rawCQLStatement));
            return ResultMessage.Prepared.forThrift(thriftStatementId, existing.boundNames);
        } else {
            MD5Digest statementId = computeId(queryString, keyspace);
            ParsedStatement.Prepared existing = preparedStatements.get(statementId);
            if (existing == null)
                return null;
            checkTrue(queryString.equals(existing.rawCQLStatement), String.format("MD5 hash collision: query with the same MD5 hash was already prepared. \n Existing: '%s'", existing.rawCQLStatement));
            return new ResultMessage.Prepared(statementId, existing);
        }
    }
>>>>>>> YOURS
<<<<<<< MINE
=======
private static ResultMessage.Prepared storePreparedStatement(String queryString, String keyspace, ParsedStatement.Prepared prepared, boolean forThrift) throws InvalidRequestException {
        long statementSize = ObjectSizes.measureDeep(prepared.statement);
        if (forThrift) {
            if (statementSize > capacityToBytes(DatabaseDescriptor.getThriftPreparedStatementsCacheSizeMB()))
                throw new InvalidRequestException(String.format("Prepared statement of size %d bytes is larger than allowed maximum of %d MB: %s...", statementSize, DatabaseDescriptor.getThriftPreparedStatementsCacheSizeMB(), queryString.substring(0, 200)));
            Integer statementId = computeThriftId(queryString, keyspace);
            thriftPreparedStatements.put(statementId, prepared);
            return ResultMessage.Prepared.forThrift(statementId, prepared.boundNames);
        } else {
            if (statementSize > capacityToBytes(DatabaseDescriptor.getPreparedStatementsCacheSizeMB()))
                throw new InvalidRequestException(String.format("Prepared statement of size %d bytes is larger than allowed maximum of %d MB: %s...", statementSize, DatabaseDescriptor.getPreparedStatementsCacheSizeMB(), queryString.substring(0, 200)));
            MD5Digest statementId = computeId(queryString, keyspace);
            preparedStatements.put(statementId, prepared);
            SystemKeyspace.writePreparedStatement(keyspace, statementId, queryString);
            return new ResultMessage.Prepared(statementId, prepared);
        }
    }
>>>>>>> YOURS


    public ResultMessage processPrepared(CQLStatement statement, QueryState state, QueryOptions options, Map<String, ByteBuffer> customPayload, long queryStartNanoTime) throws RequestExecutionException, RequestValidationException {
        return processPrepared(statement, state, options, queryStartNanoTime);
    }

    public ResultMessage processPrepared(CQLStatement statement, QueryState queryState, QueryOptions options, long queryStartNanoTime) throws RequestExecutionException, RequestValidationException {
        List<ByteBuffer> variables = options.getValues();
        if (!(variables.isEmpty() && (statement.getBoundTerms() == 0))) {
            if (variables.size() != statement.getBoundTerms())
                throw new InvalidRequestException(String.format("there were %d markers(?) in CQL but %d bound variables", statement.getBoundTerms(), variables.size()));
            if (logger.isTraceEnabled())
                for (int i = 0; i < variables.size(); i++) logger.trace("[{}] '{}'", i + 1, variables.get(i));
        }
        metrics.preparedStatementsExecuted.inc();
        return processStatement(statement, queryState, options, queryStartNanoTime);
    }

    public ResultMessage processBatch(BatchStatement statement, QueryState state, BatchQueryOptions options, Map<String, ByteBuffer> customPayload, long queryStartNanoTime) throws RequestExecutionException, RequestValidationException {
        return processBatch(statement, state, options, queryStartNanoTime);
    }

    public ResultMessage processBatch(BatchStatement batch, QueryState queryState, BatchQueryOptions options, long queryStartNanoTime) throws RequestExecutionException, RequestValidationException {
<<<<<<< MINE
        ClientState clientState = queryState.getClientState().cloneWithKeyspaceIfSet(options.getKeyspace());
=======
        ClientState clientState = queryState.getClientState();
>>>>>>> YOURS
        batch.checkAccess(clientState);
        batch.validate();
        batch.validate(clientState);
        return batch.execute(queryState, options, queryStartNanoTime);
    }

    public static ParsedStatement.Prepared getStatement(String queryStr, ClientState clientState) throws RequestValidationException {
        Tracing.trace("Parsing {}", queryStr);
        ParsedStatement statement = parseStatement(queryStr);
        if (statement instanceof CFStatement)
            ((CFStatement) statement).prepareKeyspace(clientState);
        Tracing.trace("Preparing statement");
        return statement.prepare();
    }

    public static <T extends ParsedStatement> T parseStatement(String queryStr, Class<T> klass, String type) throws SyntaxException {
        try {
            ParsedStatement stmt = parseStatement(queryStr);
            if (!klass.isAssignableFrom(stmt.getClass()))
                throw new IllegalArgumentException("Invalid query, must be a " + type + " statement but was: " + stmt.getClass());
            return klass.cast(stmt);
        } catch (RequestValidationException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public static ParsedStatement parseStatement(String queryStr) throws SyntaxException {
        try {
            return CQLFragmentParser.parseAnyUnhandled(CqlParser::query, queryStr);
        } catch (CassandraException ce) {
            throw ce;
        } catch (RuntimeException re) {
            logger.error(String.format("The statement: [%s] could not be parsed.", queryStr), re);
            throw new SyntaxException(String.format("Failed parsing statement: [%s] reason: %s %s", queryStr, re.getClass().getSimpleName(), re.getMessage()));
        } catch (RecognitionException e) {
            throw new SyntaxException("Invalid or malformed CQL query string: " + e.getMessage());
        }
    }

    private static int measure(Object key, ParsedStatement.Prepared value) {
        return Ints.checkedCast(ObjectSizes.measureDeep(key) + ObjectSizes.measureDeep(value));
    }

    @VisibleForTesting
    public static void clearInternalStatementsCache() {
        internalStatements.clear();
    }

    private static class StatementInvalidatingListener extends SchemaChangeListener {

<<<<<<< MINE

=======
        private static void removeInvalidPreparedStatements(String ksName, String cfName) {
            removeInvalidPreparedStatements(internalStatements.values().iterator(), ksName, cfName);
            removeInvalidPersistentPreparedStatements(preparedStatements.entrySet().iterator(), ksName, cfName);
            removeInvalidPreparedStatements(thriftPreparedStatements.values().iterator(), ksName, cfName);
        }
>>>>>>> YOURS

        private static void removeInvalidPreparedStatementsForFunction(String ksName, String functionName) {
            Predicate<Function> matchesFunction = f -> ksName.equals(f.name().keyspace) && functionName.equals(f.name().name);
            for (Iterator<Map.Entry<MD5Digest, ParsedStatement.Prepared>> iter = preparedStatements.asMap().entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry<MD5Digest, ParsedStatement.Prepared> pstmt = iter.next();
                if (Iterables.any(pstmt.getValue().statement.getFunctions(), matchesFunction)) {
                    SystemKeyspace.removePreparedStatement(pstmt.getKey());
                    iter.remove();
                }
            }
            Iterators.removeIf(internalStatements.values().iterator(), statement -> Iterables.any(statement.statement.getFunctions(), matchesFunction));
        }

        private static void removeInvalidPersistentPreparedStatements(Iterator<Map.Entry<MD5Digest, ParsedStatement.Prepared>> iterator, String ksName, String cfName) {
            while (iterator.hasNext()) {
                Map.Entry<MD5Digest, ParsedStatement.Prepared> entry = iterator.next();
                if (shouldInvalidate(ksName, cfName, entry.getValue().statement)) {
                    SystemKeyspace.removePreparedStatement(entry.getKey());
                    iterator.remove();
                }
            }
        }

        private static void removeInvalidPreparedStatements(Iterator<ParsedStatement.Prepared> iterator, String ksName, String cfName) {
            while (iterator.hasNext()) {
                if (shouldInvalidate(ksName, cfName, iterator.next().statement))
                    iterator.remove();
            }
        }

        private static boolean shouldInvalidate(String ksName, String cfName, CQLStatement statement) {
            String statementKsName;
            String statementCfName;
            if (statement instanceof ModificationStatement) {
                ModificationStatement modificationStatement = ((ModificationStatement) statement);
                statementKsName = modificationStatement.keyspace();
                statementCfName = modificationStatement.columnFamily();
            } else if (statement instanceof SelectStatement) {
                SelectStatement selectStatement = ((SelectStatement) statement);
                statementKsName = selectStatement.keyspace();
                statementCfName = selectStatement.columnFamily();
            } else if (statement instanceof BatchStatement) {
                BatchStatement batchStatement = ((BatchStatement) statement);
                for (ModificationStatement stmt : batchStatement.getStatements()) {
                    if (shouldInvalidate(ksName, cfName, stmt))
                        return true;
                }
                return false;
            } else {
                return false;
            }
            return ksName.equals(statementKsName) && (cfName == null || cfName.equals(statementCfName));
        }

        public void onCreateFunction(String ksName, String functionName, List<AbstractType<?>> argTypes) {
            onCreateFunctionInternal(ksName, functionName, argTypes);
        }

        public void onCreateAggregate(String ksName, String aggregateName, List<AbstractType<?>> argTypes) {
            onCreateFunctionInternal(ksName, aggregateName, argTypes);
        }

        private static void onCreateFunctionInternal(String ksName, String functionName, List<AbstractType<?>> argTypes) {
            if (Schema.instance.getKeyspaceMetadata(ksName).functions.get(new FunctionName(ksName, functionName)).size() > 1)
                removeInvalidPreparedStatementsForFunction(ksName, functionName);
        }

        public void onAlterTable(String ksName, String cfName, boolean affectsStatements) {
            logger.trace("Column definitions for {}.{} changed, invalidating related prepared statements", ksName, cfName);
            if (affectsStatements)
                removeInvalidPreparedStatements(ksName, cfName);
        }

<<<<<<< MINE
public void onAlterFunction(String ksName, String functionName, List<AbstractType<?>> argTypes) {
            removeInvalidPreparedStatementsForFunction(ksName, functionName);
=======
public void onUpdateAggregate(String ksName, String aggregateName, List<AbstractType<?>> argTypes) {
            removeInvalidPreparedStatementsForFunction(ksName, aggregateName);
>>>>>>> YOURS
        }

        public void onAlterAggregate(String ksName, String aggregateName, List<AbstractType<?>> argTypes) {
            removeInvalidPreparedStatementsForFunction(ksName, aggregateName);
        }

        public void onDropKeyspace(String ksName) {
            logger.trace("Keyspace {} was dropped, invalidating related prepared statements", ksName);
            removeInvalidPreparedStatements(ksName, null);
        }

        public void onDropTable(String ksName, String cfName) {
            logger.trace("Table {}.{} was dropped, invalidating related prepared statements", ksName, cfName);
            removeInvalidPreparedStatements(ksName, cfName);
        }

        public void onDropFunction(String ksName, String functionName, List<AbstractType<?>> argTypes) {
            removeInvalidPreparedStatementsForFunction(ksName, functionName);
        }

        public void onDropAggregate(String ksName, String aggregateName, List<AbstractType<?>> argTypes) {
            removeInvalidPreparedStatementsForFunction(ksName, aggregateName);
        }
    }
}