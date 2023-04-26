package org.elasticsearch;

import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.logging.support.LoggerMessageFormat;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.rest.RestStatus;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ElasticsearchException extends RuntimeException implements ToXContent {

    public static final String REST_EXCEPTION_SKIP_CAUSE = "rest.exception.cause.skip";

    public static final String REST_EXCEPTION_SKIP_STACK_TRACE = "rest.exception.stacktrace.skip";

    public static final boolean REST_EXCEPTION_SKIP_STACK_TRACE_DEFAULT = true;

    public static final boolean REST_EXCEPTION_SKIP_CAUSE_DEFAULT = false;

    private static final String INDEX_HEADER_KEY = "es.index";

    private static final String SHARD_HEADER_KEY = "es.shard";

    private static final String RESOURCE_HEADER_TYPE_KEY = "es.resource.type";

    private static final String RESOURCE_HEADER_ID_KEY = "es.resource.id";

    private static final Constructor<? extends ElasticsearchException>[] ID_TO_SUPPLIER;

    private static final Map<Class<? extends ElasticsearchException>, Integer> CLASS_TO_ID;

    private final Map<String, List<String>> headers = new HashMap<>();

    public ElasticsearchException(String msg, Object... args) {
        super(LoggerMessageFormat.format(msg, args));
    }

    public ElasticsearchException(String msg, Throwable cause, Object... args) {
        super(LoggerMessageFormat.format(msg, args), cause);
    }

    public ElasticsearchException(StreamInput in) throws IOException {
        super(in.readOptionalString(), in.readThrowable());
        readStackTrace(this, in);
        int numKeys = in.readVInt();
        for (int i = 0; i < numKeys; i++) {
            final String key = in.readString();
            final int numValues = in.readVInt();
            final ArrayList<String> values = new ArrayList<>(numValues);
            for (int j = 0; j < numValues; j++) {
                values.add(in.readString());
            }
            headers.put(key, values);
        }
    }

    public void addHeader(String key, String... value) {
        this.headers.put(key, Arrays.asList(value));
    }

    public void addHeader(String key, List<String> value) {
        this.headers.put(key, value);
    }

    public Set<String> getHeaderKeys() {
        return headers.keySet();
    }

    public List<String> getHeader(String key) {
        return headers.get(key);
    }

    public RestStatus status() {
        Throwable cause = unwrapCause();
        if (cause == this) {
            return RestStatus.INTERNAL_SERVER_ERROR;
        } else {
            return ExceptionsHelper.status(cause);
        }
    }

    public Throwable unwrapCause() {
        return ExceptionsHelper.unwrapCause(this);
    }

    public String getDetailedMessage() {
        if (getCause() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(toString()).append("; ");
            if (getCause() instanceof ElasticsearchException) {
                sb.append(((ElasticsearchException) getCause()).getDetailedMessage());
            } else {
                sb.append(getCause());
            }
            return sb.toString();
        } else {
            return super.toString();
        }
    }

    public Throwable getRootCause() {
        Throwable rootCause = this;
        Throwable cause = getCause();
        while (cause != null && cause != rootCause) {
            rootCause = cause;
            cause = cause.getCause();
        }
        return rootCause;
    }

    public boolean contains(Class exType) {
        if (exType == null) {
            return false;
        }
        if (exType.isInstance(this)) {
            return true;
        }
        Throwable cause = getCause();
        if (cause == this) {
            return false;
        }
        if (cause instanceof ElasticsearchException) {
            return ((ElasticsearchException) cause).contains(exType);
        } else {
            while (cause != null) {
                if (exType.isInstance(cause)) {
                    return true;
                }
                if (cause.getCause() == cause) {
                    break;
                }
                cause = cause.getCause();
            }
            return false;
        }
    }

    public void writeTo(StreamOutput out) throws IOException {
        out.writeOptionalString(this.getMessage());
        out.writeThrowable(this.getCause());
        writeStackTraces(this, out);
        out.writeVInt(headers.size());
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            out.writeString(entry.getKey());
            out.writeVInt(entry.getValue().size());
            for (String v : entry.getValue()) {
                out.writeString(v);
            }
        }
    }

    public static ElasticsearchException readException(StreamInput input, int id) throws IOException {
        Constructor<? extends ElasticsearchException> elasticsearchException = ID_TO_SUPPLIER[id];
        if (elasticsearchException == null) {
            throw new IllegalStateException("unknown exception for id: " + id);
        }
        try {
            return elasticsearchException.newInstance(input);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IOException("failed to read exception for id [" + id + "]", e);
        }
    }

    public static boolean isRegistered(Class<? extends Throwable> exception) {
        return CLASS_TO_ID.containsKey(exception);
    }

    static Set<Class<? extends ElasticsearchException>> getRegisteredKeys() {
        return CLASS_TO_ID.keySet();
    }

    public static int getId(Class<? extends ElasticsearchException> exception) {
        return CLASS_TO_ID.get(exception).intValue();
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        Throwable ex = ExceptionsHelper.unwrapCause(this);
        if (ex != this) {
            toXContent(builder, params, this);
        } else {
            builder.field("type", getExceptionName());
            builder.field("reason", getMessage());
            for (String key : headers.keySet()) {
                if (key.startsWith("es.")) {
                    List<String> values = headers.get(key);
                    xContentHeader(builder, key.substring("es.".length()), values);
                }
            }
            innerToXContent(builder, params);
            renderHeader(builder, params);
            if (params.paramAsBoolean(REST_EXCEPTION_SKIP_STACK_TRACE, REST_EXCEPTION_SKIP_STACK_TRACE_DEFAULT) == false) {
                builder.field("stack_trace", ExceptionsHelper.stackTrace(this));
            }
        }
        return builder;
    }

    protected void innerToXContent(XContentBuilder builder, Params params) throws IOException {
        causeToXContent(builder, params);
    }

    protected void causeToXContent(XContentBuilder builder, Params params) throws IOException {
        final Throwable cause = getCause();
        if (cause != null && params.paramAsBoolean(REST_EXCEPTION_SKIP_CAUSE, REST_EXCEPTION_SKIP_CAUSE_DEFAULT) == false) {
            builder.field("caused_by");
            builder.startObject();
            toXContent(builder, params, cause);
            builder.endObject();
        }
    }

    protected final void renderHeader(XContentBuilder builder, Params params) throws IOException {
        boolean hasHeader = false;
        for (String key : headers.keySet()) {
            if (key.startsWith("es.")) {
                continue;
            }
            if (hasHeader == false) {
                builder.startObject("header");
                hasHeader = true;
            }
            List<String> values = headers.get(key);
            xContentHeader(builder, key, values);
        }
        if (hasHeader) {
            builder.endObject();
        }
    }

    private void xContentHeader(XContentBuilder builder, String key, List<String> values) throws IOException {
        if (values != null && values.isEmpty() == false) {
            if (values.size() == 1) {
                builder.field(key, values.get(0));
            } else {
                builder.startArray(key);
                for (String value : values) {
                    builder.value(value);
                }
                builder.endArray();
            }
        }
    }

    public static void toXContent(XContentBuilder builder, Params params, Throwable ex) throws IOException {
        ex = ExceptionsHelper.unwrapCause(ex);
        if (ex instanceof ElasticsearchException) {
            ((ElasticsearchException) ex).toXContent(builder, params);
        } else {
            builder.field("type", getExceptionName(ex));
            builder.field("reason", ex.getMessage());
            if (ex.getCause() != null) {
                builder.field("caused_by");
                builder.startObject();
                toXContent(builder, params, ex.getCause());
                builder.endObject();
            }
            if (params.paramAsBoolean(REST_EXCEPTION_SKIP_STACK_TRACE, REST_EXCEPTION_SKIP_STACK_TRACE_DEFAULT) == false) {
                builder.field("stack_trace", ExceptionsHelper.stackTrace(ex));
            }
        }
    }

    public ElasticsearchException[] guessRootCauses() {
        final Throwable cause = getCause();
        if (cause != null && cause instanceof ElasticsearchException) {
            return ((ElasticsearchException) cause).guessRootCauses();
        }
        return new ElasticsearchException[] { this };
    }

    public static ElasticsearchException[] guessRootCauses(Throwable t) {
        Throwable ex = ExceptionsHelper.unwrapCause(t);
        if (ex instanceof ElasticsearchException) {
            return ((ElasticsearchException) ex).guessRootCauses();
        }
        return new ElasticsearchException[] { new ElasticsearchException(t.getMessage(), t) {

            @Override
            protected String getExceptionName() {
                return getExceptionName(getCause());
            }
        } };
    }

    protected String getExceptionName() {
        return getExceptionName(this);
    }

    public static String getExceptionName(Throwable ex) {
        String simpleName = ex.getClass().getSimpleName();
        if (simpleName.startsWith("Elasticsearch")) {
            simpleName = simpleName.substring("Elasticsearch".length());
        }
        return Strings.toUnderscoreCase(simpleName);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (headers.containsKey(INDEX_HEADER_KEY)) {
            builder.append('[').append(getIndex()).append(']');
            if (headers.containsKey(SHARD_HEADER_KEY)) {
                builder.append('[').append(getShardId()).append(']');
            }
            builder.append(' ');
        }
        return builder.append(ExceptionsHelper.detailedMessage(this).trim()).toString();
    }

    public static <T extends Throwable> T readStackTrace(T throwable, StreamInput in) throws IOException {
        final int stackTraceElements = in.readVInt();
        StackTraceElement[] stackTrace = new StackTraceElement[stackTraceElements];
        for (int i = 0; i < stackTraceElements; i++) {
            final String declaringClasss = in.readString();
            final String fileName = in.readOptionalString();
            final String methodName = in.readString();
            final int lineNumber = in.readVInt();
            stackTrace[i] = new StackTraceElement(declaringClasss, methodName, fileName, lineNumber);
        }
        throwable.setStackTrace(stackTrace);
        int numSuppressed = in.readVInt();
        for (int i = 0; i < numSuppressed; i++) {
            throwable.addSuppressed(in.readThrowable());
        }
        return throwable;
    }

    public static <T extends Throwable> T writeStackTraces(T throwable, StreamOutput out) throws IOException {
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        out.writeVInt(stackTrace.length);
        for (StackTraceElement element : stackTrace) {
            out.writeString(element.getClassName());
            out.writeOptionalString(element.getFileName());
            out.writeString(element.getMethodName());
            out.writeVInt(element.getLineNumber());
        }
        Throwable[] suppressed = throwable.getSuppressed();
        out.writeVInt(suppressed.length);
        for (Throwable t : suppressed) {
            out.writeThrowable(t);
        }
        return throwable;
    }

    static {
<<<<<<< MINE
        Class<? extends ElasticsearchException>[] exceptions = new Class[] { org.elasticsearch.common.settings.SettingsException.class, org.elasticsearch.index.snapshots.IndexShardSnapshotFailedException.class, org.elasticsearch.index.engine.IndexFailedEngineException.class, org.elasticsearch.indices.recovery.RecoverFilesRecoveryException.class, org.elasticsearch.index.translog.TruncatedTranslogException.class, org.elasticsearch.repositories.RepositoryException.class, org.elasticsearch.index.engine.DocumentSourceMissingException.class, org.elasticsearch.index.engine.DocumentMissingException.class, org.elasticsearch.common.util.concurrent.EsRejectedExecutionException.class, org.elasticsearch.cluster.routing.RoutingException.class, org.elasticsearch.common.lucene.Lucene.EarlyTerminationException.class, org.elasticsearch.indices.InvalidAliasNameException.class, org.elasticsearch.index.engine.EngineCreationFailureException.class, org.elasticsearch.index.snapshots.IndexShardRestoreFailedException.class, org.elasticsearch.script.groovy.GroovyScriptCompilationException.class, org.elasticsearch.cluster.routing.RoutingValidationException.class, org.elasticsearch.snapshots.SnapshotMissingException.class, org.elasticsearch.index.shard.IndexShardRecoveryException.class, org.elasticsearch.action.search.SearchPhaseExecutionException.class, org.elasticsearch.common.util.concurrent.UncategorizedExecutionException.class, org.elasticsearch.index.engine.SnapshotFailedEngineException.class, org.elasticsearch.action.search.ReduceSearchPhaseException.class, org.elasticsearch.action.RoutingMissingException.class, org.elasticsearch.index.engine.DeleteFailedEngineException.class, org.elasticsearch.indices.recovery.RecoveryFailedException.class, org.elasticsearch.search.builder.SearchSourceBuilderException.class, org.elasticsearch.index.engine.RefreshFailedEngineException.class, org.elasticsearch.index.snapshots.IndexShardSnapshotException.class, org.elasticsearch.search.query.QueryPhaseExecutionException.class, org.elasticsearch.cluster.metadata.ProcessClusterEventTimeoutException.class, org.elasticsearch.index.percolator.PercolatorException.class, org.elasticsearch.snapshots.ConcurrentSnapshotExecutionException.class, org.elasticsearch.indices.IndexTemplateAlreadyExistsException.class, org.elasticsearch.indices.InvalidIndexNameException.class, org.elasticsearch.indices.recovery.DelayRecoveryException.class, org.elasticsearch.indices.AliasFilterParsingException.class, org.elasticsearch.indices.InvalidIndexTemplateException.class, org.elasticsearch.http.HttpException.class, org.elasticsearch.index.shard.IndexShardNotRecoveringException.class, org.elasticsearch.indices.IndexPrimaryShardNotAllocatedException.class, org.elasticsearch.action.UnavailableShardsException.class, org.elasticsearch.transport.ActionNotFoundTransportException.class, org.elasticsearch.index.shard.TranslogRecoveryPerformer.BatchOperationException.class, org.elasticsearch.ElasticsearchException.class, org.elasticsearch.index.shard.IndexShardClosedException.class, org.elasticsearch.client.transport.NoNodeAvailableException.class, org.elasticsearch.cluster.block.ClusterBlockException.class, org.elasticsearch.action.FailedNodeException.class, org.elasticsearch.indices.TypeMissingException.class, org.elasticsearch.indices.InvalidTypeNameException.class, org.elasticsearch.transport.netty.SizeHeaderFrameDecoder.HttpOnTransportException.class, org.elasticsearch.common.util.CancellableThreads.ExecutionCancelledException.class, org.elasticsearch.snapshots.SnapshotCreationException.class, org.elasticsearch.script.groovy.GroovyScriptExecutionException.class, org.elasticsearch.indices.IndexTemplateMissingException.class, org.elasticsearch.transport.NodeNotConnectedException.class, org.elasticsearch.index.shard.IndexShardRecoveringException.class, org.elasticsearch.index.shard.IndexShardStartedException.class, org.elasticsearch.indices.IndexClosedException.class, org.elasticsearch.repositories.RepositoryMissingException.class, org.elasticsearch.search.warmer.IndexWarmerMissingException.class, org.elasticsearch.percolator.PercolateException.class, org.elasticsearch.index.engine.EngineException.class, org.elasticsearch.script.expression.ExpressionScriptExecutionException.class, org.elasticsearch.action.NoShardAvailableActionException.class, org.elasticsearch.transport.ReceiveTimeoutTransportException.class, org.elasticsearch.http.BindHttpException.class, org.elasticsearch.transport.RemoteTransportException.class, org.elasticsearch.index.shard.IndexShardRelocatedException.class, org.elasticsearch.snapshots.InvalidSnapshotNameException.class, org.elasticsearch.repositories.RepositoryVerificationException.class, org.elasticsearch.search.SearchException.class, org.elasticsearch.transport.ActionTransportException.class, org.elasticsearch.common.settings.NoClassSettingsException.class, org.elasticsearch.transport.NodeShouldNotConnectException.class, org.elasticsearch.index.mapper.MapperParsingException.class, org.elasticsearch.action.support.replication.TransportReplicationAction.RetryOnReplicaException.class, org.elasticsearch.search.dfs.DfsPhaseExecutionException.class, org.elasticsearch.index.engine.VersionConflictEngineException.class, org.elasticsearch.snapshots.SnapshotRestoreException.class, org.elasticsearch.script.Script.ScriptParseException.class, org.elasticsearch.ElasticsearchGenerationException.class, org.elasticsearch.action.TimestampParsingException.class, org.elasticsearch.action.NoSuchNodeException.class, org.elasticsearch.transport.BindTransportException.class, org.elasticsearch.snapshots.SnapshotException.class, org.elasticsearch.index.mapper.MapperException.class, org.elasticsearch.transport.TransportException.class, org.elasticsearch.search.SearchContextException.class, org.elasticsearch.index.translog.TranslogCorruptedException.class, org.elasticsearch.transport.TransportSerializationException.class, org.elasticsearch.cluster.IncompatibleClusterStateVersionException.class, org.elasticsearch.indices.IndexCreationException.class, org.elasticsearch.index.mapper.MergeMappingException.class, org.elasticsearch.transport.NotSerializableTransportException.class, org.elasticsearch.ElasticsearchTimeoutException.class, org.elasticsearch.search.SearchContextMissingException.class, org.elasticsearch.transport.SendRequestTransportException.class, org.elasticsearch.index.IndexShardAlreadyExistsException.class, org.elasticsearch.indices.IndexAlreadyExistsException.class, org.elasticsearch.index.engine.DocumentAlreadyExistsException.class, org.elasticsearch.transport.ConnectTransportException.class, org.elasticsearch.gateway.GatewayException.class, org.elasticsearch.script.ScriptException.class, org.elasticsearch.script.expression.ExpressionScriptCompilationException.class, org.elasticsearch.index.shard.IndexShardNotStartedException.class, org.elasticsearch.index.mapper.StrictDynamicMappingException.class, org.elasticsearch.index.engine.EngineClosedException.class, AliasesNotFoundException.class, org.elasticsearch.transport.ResponseHandlerFailureTransportException.class, org.elasticsearch.search.SearchParseException.class, org.elasticsearch.search.fetch.FetchPhaseExecutionException.class, org.elasticsearch.transport.NodeDisconnectedException.class, org.elasticsearch.common.breaker.CircuitBreakingException.class, org.elasticsearch.search.aggregations.AggregationInitializationException.class, org.elasticsearch.search.aggregations.InvalidAggregationPathException.class, org.elasticsearch.cluster.routing.IllegalShardRoutingStateException.class, org.elasticsearch.index.engine.FlushFailedEngineException.class, org.elasticsearch.index.AlreadyExpiredException.class, org.elasticsearch.index.translog.TranslogException.class, org.elasticsearch.index.engine.FlushNotAllowedEngineException.class, org.elasticsearch.index.engine.RecoveryEngineException.class, org.elasticsearch.common.blobstore.BlobStoreException.class, org.elasticsearch.index.snapshots.IndexShardRestoreException.class, org.elasticsearch.index.query.QueryShardException.class, org.elasticsearch.index.query.QueryParsingException.class, org.elasticsearch.action.support.replication.TransportReplicationAction.RetryOnPrimaryException.class, org.elasticsearch.index.engine.DeleteByQueryFailedEngineException.class, org.elasticsearch.discovery.MasterNotDiscoveredException.class, org.elasticsearch.action.support.broadcast.BroadcastShardOperationFailedException.class, org.elasticsearch.node.NodeClosedException.class, org.elasticsearch.search.aggregations.AggregationExecutionException.class, org.elasticsearch.ElasticsearchParseException.class, org.elasticsearch.action.PrimaryMissingActionException.class, org.elasticsearch.index.engine.CreateFailedEngineException.class, org.elasticsearch.index.shard.IllegalIndexShardStateException.class, ElasticsearchSecurityException.class, ResourceNotFoundException.class, IndexNotFoundException.class, ShardNotFoundException.class, NotSerializableExceptionWrapper.class, Discovery.FailedToCommitClusterStateException.class };
        Map<String, Constructor<? extends ElasticsearchException>> mapping = new HashMap<>(exceptions.length);
        for (Class<? extends ElasticsearchException> e : exceptions) {
            String name = e.getName();
=======
        final Map<Class<? extends ElasticsearchException>, Integer> exceptions = new HashMap<>();
        exceptions.put(org.elasticsearch.index.snapshots.IndexShardSnapshotFailedException.class, 0);
        exceptions.put(org.elasticsearch.search.dfs.DfsPhaseExecutionException.class, 1);
        exceptions.put(org.elasticsearch.common.util.CancellableThreads.ExecutionCancelledException.class, 2);
        exceptions.put(org.elasticsearch.discovery.MasterNotDiscoveredException.class, 3);
        exceptions.put(org.elasticsearch.ElasticsearchSecurityException.class, 4);
        exceptions.put(org.elasticsearch.index.snapshots.IndexShardRestoreException.class, 5);
        exceptions.put(org.elasticsearch.indices.IndexClosedException.class, 6);
        exceptions.put(org.elasticsearch.http.BindHttpException.class, 7);
        exceptions.put(org.elasticsearch.action.search.ReduceSearchPhaseException.class, 8);
        exceptions.put(org.elasticsearch.node.NodeClosedException.class, 9);
        exceptions.put(org.elasticsearch.index.engine.SnapshotFailedEngineException.class, 10);
        exceptions.put(org.elasticsearch.index.shard.ShardNotFoundException.class, 11);
        exceptions.put(org.elasticsearch.transport.ConnectTransportException.class, 12);
        exceptions.put(org.elasticsearch.transport.NotSerializableTransportException.class, 13);
        exceptions.put(org.elasticsearch.transport.ResponseHandlerFailureTransportException.class, 14);
        exceptions.put(org.elasticsearch.indices.IndexCreationException.class, 15);
        exceptions.put(org.elasticsearch.index.IndexNotFoundException.class, 16);
        exceptions.put(org.elasticsearch.cluster.routing.IllegalShardRoutingStateException.class, 17);
        exceptions.put(org.elasticsearch.action.support.broadcast.BroadcastShardOperationFailedException.class, 18);
        exceptions.put(org.elasticsearch.ResourceNotFoundException.class, 19);
        exceptions.put(org.elasticsearch.transport.ActionTransportException.class, 20);
        exceptions.put(org.elasticsearch.ElasticsearchGenerationException.class, 21);
        exceptions.put(org.elasticsearch.index.engine.CreateFailedEngineException.class, 22);
        exceptions.put(org.elasticsearch.index.shard.IndexShardStartedException.class, 23);
        exceptions.put(org.elasticsearch.search.SearchContextMissingException.class, 24);
        exceptions.put(org.elasticsearch.script.ScriptException.class, 25);
        exceptions.put(org.elasticsearch.index.shard.TranslogRecoveryPerformer.BatchOperationException.class, 26);
        exceptions.put(org.elasticsearch.snapshots.SnapshotCreationException.class, 27);
        exceptions.put(org.elasticsearch.index.engine.DeleteFailedEngineException.class, 28);
        exceptions.put(org.elasticsearch.index.engine.DocumentMissingException.class, 29);
        exceptions.put(org.elasticsearch.snapshots.SnapshotException.class, 30);
        exceptions.put(org.elasticsearch.indices.InvalidAliasNameException.class, 31);
        exceptions.put(org.elasticsearch.indices.InvalidIndexNameException.class, 32);
        exceptions.put(org.elasticsearch.indices.IndexPrimaryShardNotAllocatedException.class, 33);
        exceptions.put(org.elasticsearch.transport.TransportException.class, 34);
        exceptions.put(org.elasticsearch.ElasticsearchParseException.class, 35);
        exceptions.put(org.elasticsearch.search.SearchException.class, 36);
        exceptions.put(org.elasticsearch.index.mapper.MapperException.class, 37);
        exceptions.put(org.elasticsearch.indices.InvalidTypeNameException.class, 38);
        exceptions.put(org.elasticsearch.snapshots.SnapshotRestoreException.class, 39);
        exceptions.put(org.elasticsearch.index.query.QueryParsingException.class, 40);
        exceptions.put(org.elasticsearch.index.shard.IndexShardClosedException.class, 41);
        exceptions.put(org.elasticsearch.script.expression.ExpressionScriptCompilationException.class, 42);
        exceptions.put(org.elasticsearch.indices.recovery.RecoverFilesRecoveryException.class, 43);
        exceptions.put(org.elasticsearch.index.translog.TruncatedTranslogException.class, 44);
        exceptions.put(org.elasticsearch.indices.recovery.RecoveryFailedException.class, 45);
        exceptions.put(org.elasticsearch.index.shard.IndexShardRelocatedException.class, 46);
        exceptions.put(org.elasticsearch.transport.NodeShouldNotConnectException.class, 47);
        exceptions.put(org.elasticsearch.indices.IndexTemplateAlreadyExistsException.class, 48);
        exceptions.put(org.elasticsearch.index.translog.TranslogCorruptedException.class, 49);
        exceptions.put(org.elasticsearch.cluster.block.ClusterBlockException.class, 50);
        exceptions.put(org.elasticsearch.search.fetch.FetchPhaseExecutionException.class, 51);
        exceptions.put(org.elasticsearch.index.IndexShardAlreadyExistsException.class, 52);
        exceptions.put(org.elasticsearch.index.engine.VersionConflictEngineException.class, 53);
        exceptions.put(org.elasticsearch.index.engine.EngineException.class, 54);
        exceptions.put(org.elasticsearch.index.engine.DocumentAlreadyExistsException.class, 55);
        exceptions.put(org.elasticsearch.action.NoSuchNodeException.class, 56);
        exceptions.put(org.elasticsearch.common.settings.SettingsException.class, 57);
        exceptions.put(org.elasticsearch.indices.IndexTemplateMissingException.class, 58);
        exceptions.put(org.elasticsearch.transport.SendRequestTransportException.class, 59);
        exceptions.put(org.elasticsearch.common.util.concurrent.EsRejectedExecutionException.class, 60);
        exceptions.put(org.elasticsearch.common.lucene.Lucene.EarlyTerminationException.class, 61);
        exceptions.put(org.elasticsearch.cluster.routing.RoutingValidationException.class, 62);
        exceptions.put(org.elasticsearch.common.io.stream.NotSerializableExceptionWrapper.class, 63);
        exceptions.put(org.elasticsearch.indices.AliasFilterParsingException.class, 64);
        exceptions.put(org.elasticsearch.index.engine.DeleteByQueryFailedEngineException.class, 65);
        exceptions.put(org.elasticsearch.gateway.GatewayException.class, 66);
        exceptions.put(org.elasticsearch.index.shard.IndexShardNotRecoveringException.class, 67);
        exceptions.put(org.elasticsearch.http.HttpException.class, 68);
        exceptions.put(org.elasticsearch.ElasticsearchException.class, 69);
        exceptions.put(org.elasticsearch.snapshots.SnapshotMissingException.class, 70);
        exceptions.put(org.elasticsearch.action.PrimaryMissingActionException.class, 71);
        exceptions.put(org.elasticsearch.action.FailedNodeException.class, 72);
        exceptions.put(org.elasticsearch.search.SearchParseException.class, 73);
        exceptions.put(org.elasticsearch.snapshots.ConcurrentSnapshotExecutionException.class, 74);
        exceptions.put(org.elasticsearch.common.blobstore.BlobStoreException.class, 75);
        exceptions.put(org.elasticsearch.cluster.IncompatibleClusterStateVersionException.class, 76);
        exceptions.put(org.elasticsearch.index.engine.RecoveryEngineException.class, 77);
        exceptions.put(org.elasticsearch.common.util.concurrent.UncategorizedExecutionException.class, 78);
        exceptions.put(org.elasticsearch.action.TimestampParsingException.class, 79);
        exceptions.put(org.elasticsearch.action.RoutingMissingException.class, 80);
        exceptions.put(org.elasticsearch.index.engine.IndexFailedEngineException.class, 81);
        exceptions.put(org.elasticsearch.index.snapshots.IndexShardRestoreFailedException.class, 82);
        exceptions.put(org.elasticsearch.repositories.RepositoryException.class, 83);
        exceptions.put(org.elasticsearch.transport.ReceiveTimeoutTransportException.class, 84);
        exceptions.put(org.elasticsearch.transport.NodeDisconnectedException.class, 85);
        exceptions.put(org.elasticsearch.index.AlreadyExpiredException.class, 86);
        exceptions.put(org.elasticsearch.search.aggregations.AggregationExecutionException.class, 87);
        exceptions.put(org.elasticsearch.index.mapper.MergeMappingException.class, 88);
        exceptions.put(org.elasticsearch.indices.InvalidIndexTemplateException.class, 89);
        exceptions.put(org.elasticsearch.percolator.PercolateException.class, 90);
        exceptions.put(org.elasticsearch.index.engine.RefreshFailedEngineException.class, 91);
        exceptions.put(org.elasticsearch.search.aggregations.AggregationInitializationException.class, 92);
        exceptions.put(org.elasticsearch.indices.recovery.DelayRecoveryException.class, 93);
        exceptions.put(org.elasticsearch.search.warmer.IndexWarmerMissingException.class, 94);
        exceptions.put(org.elasticsearch.client.transport.NoNodeAvailableException.class, 95);
        exceptions.put(org.elasticsearch.script.groovy.GroovyScriptCompilationException.class, 96);
        exceptions.put(org.elasticsearch.snapshots.InvalidSnapshotNameException.class, 97);
        exceptions.put(org.elasticsearch.index.shard.IllegalIndexShardStateException.class, 98);
        exceptions.put(org.elasticsearch.index.snapshots.IndexShardSnapshotException.class, 99);
        exceptions.put(org.elasticsearch.index.shard.IndexShardNotStartedException.class, 100);
        exceptions.put(org.elasticsearch.action.search.SearchPhaseExecutionException.class, 101);
        exceptions.put(org.elasticsearch.transport.ActionNotFoundTransportException.class, 102);
        exceptions.put(org.elasticsearch.transport.TransportSerializationException.class, 103);
        exceptions.put(org.elasticsearch.transport.RemoteTransportException.class, 104);
        exceptions.put(org.elasticsearch.index.engine.EngineCreationFailureException.class, 105);
        exceptions.put(org.elasticsearch.cluster.routing.RoutingException.class, 106);
        exceptions.put(org.elasticsearch.index.shard.IndexShardRecoveryException.class, 107);
        exceptions.put(org.elasticsearch.repositories.RepositoryMissingException.class, 108);
        exceptions.put(org.elasticsearch.script.expression.ExpressionScriptExecutionException.class, 109);
        exceptions.put(org.elasticsearch.index.percolator.PercolatorException.class, 110);
        exceptions.put(org.elasticsearch.index.engine.DocumentSourceMissingException.class, 111);
        exceptions.put(org.elasticsearch.index.engine.FlushNotAllowedEngineException.class, 112);
        exceptions.put(org.elasticsearch.common.settings.NoClassSettingsException.class, 113);
        exceptions.put(org.elasticsearch.transport.BindTransportException.class, 114);
        exceptions.put(org.elasticsearch.rest.action.admin.indices.alias.delete.AliasesNotFoundException.class, 115);
        exceptions.put(org.elasticsearch.index.shard.IndexShardRecoveringException.class, 116);
        exceptions.put(org.elasticsearch.index.translog.TranslogException.class, 117);
        exceptions.put(org.elasticsearch.cluster.metadata.ProcessClusterEventTimeoutException.class, 118);
        exceptions.put(org.elasticsearch.action.support.replication.TransportReplicationAction.RetryOnPrimaryException.class, 119);
        exceptions.put(org.elasticsearch.ElasticsearchTimeoutException.class, 120);
        exceptions.put(org.elasticsearch.search.query.QueryPhaseExecutionException.class, 121);
        exceptions.put(org.elasticsearch.repositories.RepositoryVerificationException.class, 122);
        exceptions.put(org.elasticsearch.search.aggregations.InvalidAggregationPathException.class, 123);
        exceptions.put(org.elasticsearch.script.groovy.GroovyScriptExecutionException.class, 124);
        exceptions.put(org.elasticsearch.indices.IndexAlreadyExistsException.class, 125);
        exceptions.put(org.elasticsearch.script.Script.ScriptParseException.class, 126);
        exceptions.put(org.elasticsearch.transport.netty.SizeHeaderFrameDecoder.HttpOnTransportException.class, 127);
        exceptions.put(org.elasticsearch.index.mapper.MapperParsingException.class, 128);
        exceptions.put(org.elasticsearch.search.SearchContextException.class, 129);
        exceptions.put(org.elasticsearch.search.builder.SearchSourceBuilderException.class, 130);
        exceptions.put(org.elasticsearch.index.engine.EngineClosedException.class, 131);
        exceptions.put(org.elasticsearch.action.NoShardAvailableActionException.class, 132);
        exceptions.put(org.elasticsearch.action.UnavailableShardsException.class, 133);
        exceptions.put(org.elasticsearch.index.engine.FlushFailedEngineException.class, 134);
        exceptions.put(org.elasticsearch.common.breaker.CircuitBreakingException.class, 135);
        exceptions.put(org.elasticsearch.transport.NodeNotConnectedException.class, 136);
        exceptions.put(org.elasticsearch.index.mapper.StrictDynamicMappingException.class, 137);
        exceptions.put(org.elasticsearch.action.support.replication.TransportReplicationAction.RetryOnReplicaException.class, 138);
        exceptions.put(org.elasticsearch.indices.TypeMissingException.class, 139);
        exceptions.put(org.elasticsearch.discovery.Discovery.FailedToCommitClusterStateException.class, 140);
        final int maxOrd = 140;
        assert exceptions.size() == maxOrd + 1;
        Constructor<? extends ElasticsearchException>[] idToSupplier = new Constructor[maxOrd + 1];
        for (Map.Entry<Class<? extends ElasticsearchException>, Integer> e : exceptions.entrySet()) {
>>>>>>> YOURS
            try {
                Constructor<? extends ElasticsearchException> constructor = e.getKey().getDeclaredConstructor(StreamInput.class);
                if (constructor == null) {
                    throw new IllegalStateException(e.getKey().getName() + " has not StreamInput ctor");
                }
                assert e.getValue().intValue() >= 0;
                if (idToSupplier[e.getValue().intValue()] != null) {
                    throw new IllegalStateException("ordinal [" + e.getValue().intValue() + "] is used more than once");
                }
                idToSupplier[e.getValue().intValue()] = constructor;
            } catch (NoSuchMethodException t) {
                throw new RuntimeException("failed to register [" + e.getKey().getName() + "] exception must have a public StreamInput ctor", t);
            }
        }
        for (int i = 0; i < idToSupplier.length; i++) {
            if (idToSupplier[i] == null) {
                throw new IllegalStateException("missing exception for ordinal [" + i + "]");
            }
        }
        ID_TO_SUPPLIER = idToSupplier;
        CLASS_TO_ID = Collections.unmodifiableMap(exceptions);
    }

    public String getIndex() {
        List<String> index = getHeader(INDEX_HEADER_KEY);
        if (index != null && index.isEmpty() == false) {
            return index.get(0);
        }
        return null;
    }

    public ShardId getShardId() {
        List<String> shard = getHeader(SHARD_HEADER_KEY);
        if (shard != null && shard.isEmpty() == false) {
            return new ShardId(getIndex(), Integer.parseInt(shard.get(0)));
        }
        return null;
    }

    public void setIndex(Index index) {
        if (index != null) {
            addHeader(INDEX_HEADER_KEY, index.getName());
        }
    }

    public void setIndex(String index) {
        if (index != null) {
            addHeader(INDEX_HEADER_KEY, index);
        }
    }

    public void setShard(ShardId shardId) {
        if (shardId != null) {
            addHeader(INDEX_HEADER_KEY, shardId.getIndex());
            addHeader(SHARD_HEADER_KEY, Integer.toString(shardId.id()));
        }
    }

    public void setResources(String type, String... id) {
        assert type != null;
        addHeader(RESOURCE_HEADER_ID_KEY, id);
        addHeader(RESOURCE_HEADER_TYPE_KEY, type);
    }

    public List<String> getResourceId() {
        return getHeader(RESOURCE_HEADER_ID_KEY);
    }

    public String getResourceType() {
        List<String> header = getHeader(RESOURCE_HEADER_TYPE_KEY);
        if (header != null && header.isEmpty() == false) {
            assert header.size() == 1;
            return header.get(0);
        }
        return null;
    }

    public static void renderThrowable(XContentBuilder builder, Params params, Throwable t) throws IOException {
        builder.startObject("error");
        final ElasticsearchException[] rootCauses = ElasticsearchException.guessRootCauses(t);
        builder.field("root_cause");
        builder.startArray();
        for (ElasticsearchException rootCause : rootCauses) {
            builder.startObject();
            rootCause.toXContent(builder, new ToXContent.DelegatingMapParams(Collections.singletonMap(ElasticsearchException.REST_EXCEPTION_SKIP_CAUSE, "true"), params));
            builder.endObject();
        }
        builder.endArray();
        ElasticsearchException.toXContent(builder, params, t);
        builder.endObject();
    }
}