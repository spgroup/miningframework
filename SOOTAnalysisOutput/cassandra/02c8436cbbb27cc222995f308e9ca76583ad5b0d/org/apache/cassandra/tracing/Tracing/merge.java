package org.apache.cassandra.tracing;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.util.concurrent.FastThreadLocal;
import org.apache.cassandra.concurrent.ExecutorLocal;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.marshal.TimeUUIDType;
import org.apache.cassandra.io.IVersionedSerializer;
import org.apache.cassandra.io.util.DataInputPlus;
import org.apache.cassandra.io.util.DataOutputPlus;
import org.apache.cassandra.locator.InetAddressAndPort;
import org.apache.cassandra.net.MessageIn;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.net.ParameterType;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.JVMStabilityInspector;
import org.apache.cassandra.utils.UUIDGen;

public abstract class Tracing implements ExecutorLocal<TraceState> {

    public static final IVersionedSerializer<TraceType> traceTypeSerializer = new IVersionedSerializer<TraceType>() {

        public void serialize(TraceType traceType, DataOutputPlus out, int version) throws IOException {
            out.write((byte) traceType.ordinal());
        }

        public TraceType deserialize(DataInputPlus in, int version) throws IOException {
            return TraceType.deserialize(in.readByte());
        }

        public long serializedSize(TraceType traceType, int version) {
            return 1;
        }
    };

    public enum TraceType {

        NONE, QUERY, REPAIR;

        private static final TraceType[] ALL_VALUES = values();

        public static TraceType deserialize(byte b) {
            if (b < 0 || ALL_VALUES.length <= b)
                return NONE;
            return ALL_VALUES[b];
        }

        public static byte serialize(TraceType value) {
            return (byte) value.ordinal();
        }

        private static final int[] TTLS = { DatabaseDescriptor.getTracetypeQueryTTL(), DatabaseDescriptor.getTracetypeQueryTTL(), DatabaseDescriptor.getTracetypeRepairTTL() };

        public int getTTL() {
            return TTLS[ordinal()];
        }
    }

    protected static final Logger logger = LoggerFactory.getLogger(Tracing.class);

    private final InetAddressAndPort localAddress = FBUtilities.getLocalAddressAndPort();

    private final FastThreadLocal<TraceState> state = new FastThreadLocal<>();

    protected final ConcurrentMap<UUID, TraceState> sessions = new ConcurrentHashMap<>();

    public static final Tracing instance;

    static {
        Tracing tracing = null;
        String customTracingClass = System.getProperty("cassandra.custom_tracing_class");
        if (null != customTracingClass) {
            try {
                tracing = FBUtilities.construct(customTracingClass, "Tracing");
                logger.info("Using {} as tracing queries (as requested with -Dcassandra.custom_tracing_class)", customTracingClass);
            } catch (Exception e) {
                JVMStabilityInspector.inspectThrowable(e);
                logger.error(String.format("Cannot use class %s for tracing, ignoring by defaulting to normal tracing", customTracingClass), e);
            }
        }
        instance = null != tracing ? tracing : new TracingImpl();
    }

    public UUID getSessionId() {
        assert isTracing();
        return state.get().sessionId;
    }

    public TraceType getTraceType() {
        assert isTracing();
        return state.get().traceType;
    }

    public int getTTL() {
        assert isTracing();
        return state.get().ttl;
    }

    public static boolean isTracing() {
        return instance.get() != null;
    }

    public UUID newSession(Map<String, ByteBuffer> customPayload) {
        return newSession(TimeUUIDType.instance.compose(ByteBuffer.wrap(UUIDGen.getTimeUUIDBytes())), TraceType.QUERY, customPayload);
    }

    public UUID newSession(TraceType traceType) {
        return newSession(TimeUUIDType.instance.compose(ByteBuffer.wrap(UUIDGen.getTimeUUIDBytes())), traceType, Collections.EMPTY_MAP);
    }

    public UUID newSession(UUID sessionId, Map<String, ByteBuffer> customPayload) {
        return newSession(sessionId, TraceType.QUERY, customPayload);
    }

    protected UUID newSession(UUID sessionId, TraceType traceType, Map<String, ByteBuffer> customPayload) {
        assert get() == null;
        TraceState ts = newTraceState(localAddress, sessionId, traceType);
        set(ts);
        sessions.put(sessionId, ts);
        return sessionId;
    }

    public void doneWithNonLocalSession(TraceState state) {
        if (state.releaseReference() == 0)
            sessions.remove(state.sessionId);
    }

    public void stopSession() {
        TraceState state = get();
        if (state == null) {
            logger.trace("request complete");
        } else {
            stopSessionImpl();
            state.stop();
            sessions.remove(state.sessionId);
            set(null);
        }
    }

    protected abstract void stopSessionImpl();

    public TraceState get() {
        return state.get();
    }

    public TraceState get(UUID sessionId) {
        return sessions.get(sessionId);
    }

    public void set(final TraceState tls) {
        state.set(tls);
    }

    public TraceState begin(final String request, final Map<String, String> parameters) {
        return begin(request, null, parameters);
    }

    public abstract TraceState begin(String request, InetAddress client, Map<String, String> parameters);

    public TraceState initializeFromMessage(final MessageIn<?> message) {
        final UUID sessionId = (UUID) message.parameters.get(ParameterType.TRACE_SESSION);
        if (sessionId == null)
            return null;
        TraceState ts = get(sessionId);
        if (ts != null && ts.acquireReference())
            return ts;
        TraceType tmpType;
        TraceType traceType = TraceType.QUERY;
        if ((tmpType = (TraceType) message.parameters.get(ParameterType.TRACE_TYPE)) != null)
            traceType = tmpType;
        if (message.verb == MessagingService.Verb.REQUEST_RESPONSE) {
            return new ExpiredTraceState(newTraceState(message.from, sessionId, traceType));
        } else {
            ts = newTraceState(message.from, sessionId, traceType);
            sessions.put(sessionId, ts);
            return ts;
        }
    }

    public List<Object> getTraceHeaders() {
        assert isTracing();
        return ImmutableList.of(ParameterType.TRACE_SESSION, Tracing.instance.getSessionId(), ParameterType.TRACE_TYPE, Tracing.instance.getTraceType());
    }

    protected abstract TraceState newTraceState(InetAddressAndPort coordinator, UUID sessionId, Tracing.TraceType traceType);

    public static void traceRepair(String format, Object... args) {
        final TraceState state = instance.get();
        if (state == null)
            return;
        state.trace(format, args);
    }

    public static void trace(String message) {
        final TraceState state = instance.get();
        if (state == null)
            return;
        state.trace(message);
    }

    public static void trace(String format, Object arg) {
        final TraceState state = instance.get();
        if (state == null)
            return;
        state.trace(format, arg);
    }

    public static void trace(String format, Object arg1, Object arg2) {
        final TraceState state = instance.get();
        if (state == null)
            return;
        state.trace(format, arg1, arg2);
    }

    public static void trace(String format, Object... args) {
        final TraceState state = instance.get();
        if (state == null)
            return;
        state.trace(format, args);
    }

    public abstract void trace(ByteBuffer sessionId, String message, int ttl);
}
