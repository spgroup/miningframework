package org.apache.hadoop.hbase.protobuf;

import static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.RegionSpecifierType.REGION_NAME;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.concurrent.TimeUnit;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellScanner;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.DoNotRetryIOException;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.Tag;
import org.apache.hadoop.hbase.classification.InterfaceAudience;
import org.apache.hadoop.hbase.client.Append;
import org.apache.hadoop.hbase.client.Consistency;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.metrics.ScanMetrics;
import org.apache.hadoop.hbase.exceptions.DeserializationException;
import org.apache.hadoop.hbase.filter.ByteArrayComparable;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.io.TimeRange;
import org.apache.hadoop.hbase.protobuf.generated.AccessControlProtos;
import org.apache.hadoop.hbase.protobuf.generated.AccessControlProtos.AccessControlService;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos.AdminService;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos.CloseRegionRequest;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos.CloseRegionResponse;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos.GetOnlineRegionRequest;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos.GetOnlineRegionResponse;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos.GetRegionInfoRequest;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos.GetRegionInfoResponse;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos.GetServerInfoRequest;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos.GetServerInfoResponse;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos.GetStoreFileRequest;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos.GetStoreFileResponse;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos.MergeRegionsRequest;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos.OpenRegionRequest;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos.ServerInfo;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos.SplitRegionRequest;
import org.apache.hadoop.hbase.protobuf.generated.AdminProtos.WarmupRegionRequest;
import org.apache.hadoop.hbase.protobuf.generated.AuthenticationProtos;
import org.apache.hadoop.hbase.protobuf.generated.CellProtos;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos.BulkLoadHFileRequest;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos.BulkLoadHFileResponse;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos.ClientService;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos.Column;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos.CoprocessorServiceCall;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos.CoprocessorServiceRequest;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos.CoprocessorServiceResponse;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos.GetRequest;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos.MutationProto;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos.MutationProto.ColumnValue;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos.MutationProto.ColumnValue.QualifierValue;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos.MutationProto.DeleteType;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos.MutationProto.MutationType;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos.ScanRequest;
import org.apache.hadoop.hbase.protobuf.generated.ClusterStatusProtos;
import org.apache.hadoop.hbase.protobuf.generated.ClusterStatusProtos.RegionLoad;
import org.apache.hadoop.hbase.protobuf.generated.ComparatorProtos;
import org.apache.hadoop.hbase.protobuf.generated.FilterProtos;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.RegionSpecifierType;
import org.apache.hadoop.hbase.protobuf.generated.MapReduceProtos;
import org.apache.hadoop.hbase.protobuf.generated.MasterProtos.CreateTableRequest;
import org.apache.hadoop.hbase.protobuf.generated.MasterProtos.GetTableDescriptorsResponse;
import org.apache.hadoop.hbase.protobuf.generated.MasterProtos.MasterService;
import org.apache.hadoop.hbase.protobuf.generated.QuotaProtos;
import org.apache.hadoop.hbase.protobuf.generated.RPCProtos;
import org.apache.hadoop.hbase.protobuf.generated.RegionServerStatusProtos.RegionServerReportRequest;
import org.apache.hadoop.hbase.protobuf.generated.RegionServerStatusProtos.RegionServerStartupRequest;
import org.apache.hadoop.hbase.protobuf.generated.WALProtos;
import org.apache.hadoop.hbase.protobuf.generated.WALProtos.BulkLoadDescriptor;
import org.apache.hadoop.hbase.protobuf.generated.WALProtos.CompactionDescriptor;
import org.apache.hadoop.hbase.protobuf.generated.WALProtos.FlushDescriptor;
import org.apache.hadoop.hbase.protobuf.generated.WALProtos.FlushDescriptor.FlushAction;
import org.apache.hadoop.hbase.protobuf.generated.WALProtos.RegionEventDescriptor;
import org.apache.hadoop.hbase.protobuf.generated.WALProtos.RegionEventDescriptor.EventType;
import org.apache.hadoop.hbase.protobuf.generated.WALProtos.StoreDescriptor;
import org.apache.hadoop.hbase.quotas.QuotaScope;
import org.apache.hadoop.hbase.quotas.QuotaType;
import org.apache.hadoop.hbase.quotas.ThrottleType;
import org.apache.hadoop.hbase.replication.ReplicationLoadSink;
import org.apache.hadoop.hbase.replication.ReplicationLoadSource;
import org.apache.hadoop.hbase.security.access.Permission;
import org.apache.hadoop.hbase.security.access.TablePermission;
import org.apache.hadoop.hbase.security.access.UserPermission;
import org.apache.hadoop.hbase.security.token.AuthenticationTokenIdentifier;
import org.apache.hadoop.hbase.security.visibility.Authorizations;
import org.apache.hadoop.hbase.security.visibility.CellVisibility;
import org.apache.hadoop.hbase.util.ByteStringer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.DynamicClassLoader;
import org.apache.hadoop.hbase.util.ExceptionUtil;
import org.apache.hadoop.hbase.util.Methods;
import org.apache.hadoop.hbase.util.Pair;
import org.apache.hadoop.hbase.util.VersionInfo;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.ipc.RemoteException;
import org.apache.hadoop.security.token.Token;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.google.protobuf.RpcChannel;
import com.google.protobuf.Service;
import com.google.protobuf.ServiceException;
import com.google.protobuf.TextFormat;

@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "DP_CREATE_CLASSLOADER_INSIDE_DO_PRIVILEGED", justification = "None. Address sometime.")
@InterfaceAudience.Private
public final class ProtobufUtil {

    private ProtobufUtil() {
    }

    private final static Map<String, Class<?>> PRIMITIVES = new HashMap<String, Class<?>>();

    private final static Cell[] EMPTY_CELL_ARRAY = new Cell[] {};

    private final static Result EMPTY_RESULT = Result.create(EMPTY_CELL_ARRAY);

    private final static Result EMPTY_RESULT_EXISTS_TRUE = Result.create(null, true);

    private final static Result EMPTY_RESULT_EXISTS_FALSE = Result.create(null, false);

    private final static Result EMPTY_RESULT_STALE = Result.create(EMPTY_CELL_ARRAY, null, true);

    private final static Result EMPTY_RESULT_EXISTS_TRUE_STALE = Result.create((Cell[]) null, true, true);

    private final static Result EMPTY_RESULT_EXISTS_FALSE_STALE = Result.create((Cell[]) null, false, true);

    private final static ClientProtos.Result EMPTY_RESULT_PB;

    private final static ClientProtos.Result EMPTY_RESULT_PB_EXISTS_TRUE;

    private final static ClientProtos.Result EMPTY_RESULT_PB_EXISTS_FALSE;

    private final static ClientProtos.Result EMPTY_RESULT_PB_STALE;

    private final static ClientProtos.Result EMPTY_RESULT_PB_EXISTS_TRUE_STALE;

    private final static ClientProtos.Result EMPTY_RESULT_PB_EXISTS_FALSE_STALE;

    static {
        ClientProtos.Result.Builder builder = ClientProtos.Result.newBuilder();
        builder.setExists(true);
        builder.setAssociatedCellCount(0);
        EMPTY_RESULT_PB_EXISTS_TRUE = builder.build();
        builder.setStale(true);
        EMPTY_RESULT_PB_EXISTS_TRUE_STALE = builder.build();
        builder.clear();
        builder.setExists(false);
        builder.setAssociatedCellCount(0);
        EMPTY_RESULT_PB_EXISTS_FALSE = builder.build();
        builder.setStale(true);
        EMPTY_RESULT_PB_EXISTS_FALSE_STALE = builder.build();
        builder.clear();
        builder.setAssociatedCellCount(0);
        EMPTY_RESULT_PB = builder.build();
        builder.setStale(true);
        EMPTY_RESULT_PB_STALE = builder.build();
    }

    private final static ClassLoader CLASS_LOADER;

    static {
        ClassLoader parent = ProtobufUtil.class.getClassLoader();
        Configuration conf = HBaseConfiguration.create();
        CLASS_LOADER = new DynamicClassLoader(conf, parent);
        PRIMITIVES.put(Boolean.TYPE.getName(), Boolean.TYPE);
        PRIMITIVES.put(Byte.TYPE.getName(), Byte.TYPE);
        PRIMITIVES.put(Character.TYPE.getName(), Character.TYPE);
        PRIMITIVES.put(Short.TYPE.getName(), Short.TYPE);
        PRIMITIVES.put(Integer.TYPE.getName(), Integer.TYPE);
        PRIMITIVES.put(Long.TYPE.getName(), Long.TYPE);
        PRIMITIVES.put(Float.TYPE.getName(), Float.TYPE);
        PRIMITIVES.put(Double.TYPE.getName(), Double.TYPE);
        PRIMITIVES.put(Void.TYPE.getName(), Void.TYPE);
    }

    public static byte[] prependPBMagic(final byte[] bytes) {
        return Bytes.add(ProtobufMagic.PB_MAGIC, bytes);
    }

    public static boolean isPBMagicPrefix(final byte[] bytes) {
        return ProtobufMagic.isPBMagicPrefix(bytes);
    }

    public static boolean isPBMagicPrefix(final byte[] bytes, int offset, int len) {
        return ProtobufMagic.isPBMagicPrefix(bytes, offset, len);
    }

    public static void expectPBMagicPrefix(final byte[] bytes) throws DeserializationException {
        if (!isPBMagicPrefix(bytes)) {
            throw new DeserializationException("Missing pb magic " + Bytes.toString(ProtobufMagic.PB_MAGIC) + " prefix");
        }
    }

    public static int lengthOfPBMagic() {
        return ProtobufMagic.lengthOfPBMagic();
    }

    public static IOException getRemoteException(ServiceException se) {
        Throwable e = se.getCause();
        if (e == null) {
            return new IOException(se);
        }
        if (ExceptionUtil.isInterrupt(e)) {
            return ExceptionUtil.asInterrupt(e);
        }
        if (e instanceof RemoteException) {
            e = ((RemoteException) e).unwrapRemoteException();
        }
        return e instanceof IOException ? (IOException) e : new IOException(se);
    }

    public static HBaseProtos.ServerName toServerName(final ServerName serverName) {
        if (serverName == null)
            return null;
        HBaseProtos.ServerName.Builder builder = HBaseProtos.ServerName.newBuilder();
        builder.setHostName(serverName.getHostname());
        if (serverName.getPort() >= 0) {
            builder.setPort(serverName.getPort());
        }
        if (serverName.getStartcode() >= 0) {
            builder.setStartCode(serverName.getStartcode());
        }
        return builder.build();
    }

    public static ServerName toServerName(final HBaseProtos.ServerName proto) {
        if (proto == null)
            return null;
        String hostName = proto.getHostName();
        long startCode = -1;
        int port = -1;
        if (proto.hasPort()) {
            port = proto.getPort();
        }
        if (proto.hasStartCode()) {
            startCode = proto.getStartCode();
        }
        return ServerName.valueOf(hostName, port, startCode);
    }

    public static HTableDescriptor[] getHTableDescriptorArray(GetTableDescriptorsResponse proto) {
        if (proto == null)
            return null;
        HTableDescriptor[] ret = new HTableDescriptor[proto.getTableSchemaCount()];
        for (int i = 0; i < proto.getTableSchemaCount(); ++i) {
            ret[i] = HTableDescriptor.convert(proto.getTableSchema(i));
        }
        return ret;
    }

    public static byte[][] getSplitKeysArray(final CreateTableRequest proto) {
        byte[][] splitKeys = new byte[proto.getSplitKeysCount()][];
        for (int i = 0; i < proto.getSplitKeysCount(); ++i) {
            splitKeys[i] = proto.getSplitKeys(i).toByteArray();
        }
        return splitKeys;
    }

    public static Durability toDurability(final ClientProtos.MutationProto.Durability proto) {
        switch(proto) {
            case USE_DEFAULT:
                return Durability.USE_DEFAULT;
            case SKIP_WAL:
                return Durability.SKIP_WAL;
            case ASYNC_WAL:
                return Durability.ASYNC_WAL;
            case SYNC_WAL:
                return Durability.SYNC_WAL;
            case FSYNC_WAL:
                return Durability.FSYNC_WAL;
            default:
                return Durability.USE_DEFAULT;
        }
    }

    public static ClientProtos.MutationProto.Durability toDurability(final Durability d) {
        switch(d) {
            case USE_DEFAULT:
                return ClientProtos.MutationProto.Durability.USE_DEFAULT;
            case SKIP_WAL:
                return ClientProtos.MutationProto.Durability.SKIP_WAL;
            case ASYNC_WAL:
                return ClientProtos.MutationProto.Durability.ASYNC_WAL;
            case SYNC_WAL:
                return ClientProtos.MutationProto.Durability.SYNC_WAL;
            case FSYNC_WAL:
                return ClientProtos.MutationProto.Durability.FSYNC_WAL;
            default:
                return ClientProtos.MutationProto.Durability.USE_DEFAULT;
        }
    }

    public static Get toGet(final ClientProtos.Get proto) throws IOException {
        if (proto == null)
            return null;
        byte[] row = proto.getRow().toByteArray();
        Get get = new Get(row);
        if (proto.hasCacheBlocks()) {
            get.setCacheBlocks(proto.getCacheBlocks());
        }
        if (proto.hasMaxVersions()) {
            get.setMaxVersions(proto.getMaxVersions());
        }
        if (proto.hasStoreLimit()) {
            get.setMaxResultsPerColumnFamily(proto.getStoreLimit());
        }
        if (proto.hasStoreOffset()) {
            get.setRowOffsetPerColumnFamily(proto.getStoreOffset());
        }
        if (proto.hasTimeRange()) {
            HBaseProtos.TimeRange timeRange = proto.getTimeRange();
            long minStamp = 0;
            long maxStamp = Long.MAX_VALUE;
            if (timeRange.hasFrom()) {
                minStamp = timeRange.getFrom();
            }
            if (timeRange.hasTo()) {
                maxStamp = timeRange.getTo();
            }
            get.setTimeRange(minStamp, maxStamp);
        }
        if (proto.hasFilter()) {
            FilterProtos.Filter filter = proto.getFilter();
            get.setFilter(ProtobufUtil.toFilter(filter));
        }
        for (NameBytesPair attribute : proto.getAttributeList()) {
            get.setAttribute(attribute.getName(), attribute.getValue().toByteArray());
        }
        if (proto.getColumnCount() > 0) {
            for (Column column : proto.getColumnList()) {
                byte[] family = column.getFamily().toByteArray();
                if (column.getQualifierCount() > 0) {
                    for (ByteString qualifier : column.getQualifierList()) {
                        get.addColumn(family, qualifier.toByteArray());
                    }
                } else {
                    get.addFamily(family);
                }
            }
        }
        if (proto.hasExistenceOnly() && proto.getExistenceOnly()) {
            get.setCheckExistenceOnly(true);
        }
        if (proto.hasConsistency()) {
            get.setConsistency(toConsistency(proto.getConsistency()));
        }
        return get;
    }

    public static Consistency toConsistency(ClientProtos.Consistency consistency) {
        switch(consistency) {
            case STRONG:
                return Consistency.STRONG;
            case TIMELINE:
                return Consistency.TIMELINE;
            default:
                return Consistency.STRONG;
        }
    }

    public static ClientProtos.Consistency toConsistency(Consistency consistency) {
        switch(consistency) {
            case STRONG:
                return ClientProtos.Consistency.STRONG;
            case TIMELINE:
                return ClientProtos.Consistency.TIMELINE;
            default:
                return ClientProtos.Consistency.STRONG;
        }
    }

    public static Put toPut(final MutationProto proto) throws IOException {
        return toPut(proto, null);
    }

    public static Put toPut(final MutationProto proto, final CellScanner cellScanner) throws IOException {
        MutationType type = proto.getMutateType();
        assert type == MutationType.PUT : type.name();
        long timestamp = proto.hasTimestamp() ? proto.getTimestamp() : HConstants.LATEST_TIMESTAMP;
        Put put = null;
        int cellCount = proto.hasAssociatedCellCount() ? proto.getAssociatedCellCount() : 0;
        if (cellCount > 0) {
            if (cellScanner == null) {
                throw new DoNotRetryIOException("Cell count of " + cellCount + " but no cellScanner: " + toShortString(proto));
            }
            for (int i = 0; i < cellCount; i++) {
                if (!cellScanner.advance()) {
                    throw new DoNotRetryIOException("Cell count of " + cellCount + " but at index " + i + " no cell returned: " + toShortString(proto));
                }
                Cell cell = cellScanner.current();
                if (put == null) {
                    put = new Put(cell.getRowArray(), cell.getRowOffset(), cell.getRowLength(), timestamp);
                }
                put.add(cell);
            }
        } else {
            if (proto.hasRow()) {
                put = new Put(proto.getRow().asReadOnlyByteBuffer(), timestamp);
            } else {
                throw new IllegalArgumentException("row cannot be null");
            }
            for (ColumnValue column : proto.getColumnValueList()) {
                byte[] family = column.getFamily().toByteArray();
                for (QualifierValue qv : column.getQualifierValueList()) {
                    if (!qv.hasValue()) {
                        throw new DoNotRetryIOException("Missing required field: qualifier value");
                    }
                    ByteBuffer qualifier = qv.hasQualifier() ? qv.getQualifier().asReadOnlyByteBuffer() : null;
                    ByteBuffer value = qv.hasValue() ? qv.getValue().asReadOnlyByteBuffer() : null;
                    long ts = timestamp;
                    if (qv.hasTimestamp()) {
                        ts = qv.getTimestamp();
                    }
                    byte[] tags;
                    if (qv.hasTags()) {
                        tags = qv.getTags().toByteArray();
                        Object[] array = Tag.asList(tags, 0, (short) tags.length).toArray();
                        Tag[] tagArray = new Tag[array.length];
                        for (int i = 0; i < array.length; i++) {
                            tagArray[i] = (Tag) array[i];
                        }
                        if (qv.hasDeleteType()) {
                            byte[] qual = qv.hasQualifier() ? qv.getQualifier().toByteArray() : null;
                            put.add(new KeyValue(proto.getRow().toByteArray(), family, qual, ts, fromDeleteType(qv.getDeleteType()), null, tags));
                        } else {
                            put.addImmutable(family, qualifier, ts, value, tagArray);
                        }
                    } else {
                        if (qv.hasDeleteType()) {
                            byte[] qual = qv.hasQualifier() ? qv.getQualifier().toByteArray() : null;
                            put.add(new KeyValue(proto.getRow().toByteArray(), family, qual, ts, fromDeleteType(qv.getDeleteType())));
                        } else {
                            put.addImmutable(family, qualifier, ts, value);
                        }
                    }
                }
            }
        }
        put.setDurability(toDurability(proto.getDurability()));
        for (NameBytesPair attribute : proto.getAttributeList()) {
            put.setAttribute(attribute.getName(), attribute.getValue().toByteArray());
        }
        return put;
    }

    public static Delete toDelete(final MutationProto proto) throws IOException {
        return toDelete(proto, null);
    }

    public static Delete toDelete(final MutationProto proto, final CellScanner cellScanner) throws IOException {
        MutationType type = proto.getMutateType();
        assert type == MutationType.DELETE : type.name();
        byte[] row = proto.hasRow() ? proto.getRow().toByteArray() : null;
        long timestamp = HConstants.LATEST_TIMESTAMP;
        if (proto.hasTimestamp()) {
            timestamp = proto.getTimestamp();
        }
        Delete delete = null;
        int cellCount = proto.hasAssociatedCellCount() ? proto.getAssociatedCellCount() : 0;
        if (cellCount > 0) {
            if (cellScanner == null) {
                throw new DoNotRetryIOException("Cell count of " + cellCount + " but no cellScanner: " + TextFormat.shortDebugString(proto));
            }
            for (int i = 0; i < cellCount; i++) {
                if (!cellScanner.advance()) {
                    throw new DoNotRetryIOException("Cell count of " + cellCount + " but at index " + i + " no cell returned: " + TextFormat.shortDebugString(proto));
                }
                Cell cell = cellScanner.current();
                if (delete == null) {
                    delete = new Delete(cell.getRowArray(), cell.getRowOffset(), cell.getRowLength(), timestamp);
                }
                delete.addDeleteMarker(cell);
            }
        } else {
            delete = new Delete(row, timestamp);
            for (ColumnValue column : proto.getColumnValueList()) {
                byte[] family = column.getFamily().toByteArray();
                for (QualifierValue qv : column.getQualifierValueList()) {
                    DeleteType deleteType = qv.getDeleteType();
                    byte[] qualifier = null;
                    if (qv.hasQualifier()) {
                        qualifier = qv.getQualifier().toByteArray();
                    }
                    long ts = HConstants.LATEST_TIMESTAMP;
                    if (qv.hasTimestamp()) {
                        ts = qv.getTimestamp();
                    }
                    if (deleteType == DeleteType.DELETE_ONE_VERSION) {
                        delete.deleteColumn(family, qualifier, ts);
                    } else if (deleteType == DeleteType.DELETE_MULTIPLE_VERSIONS) {
                        delete.deleteColumns(family, qualifier, ts);
                    } else if (deleteType == DeleteType.DELETE_FAMILY_VERSION) {
                        delete.deleteFamilyVersion(family, ts);
                    } else {
                        delete.deleteFamily(family, ts);
                    }
                }
            }
        }
        delete.setDurability(toDurability(proto.getDurability()));
        for (NameBytesPair attribute : proto.getAttributeList()) {
            delete.setAttribute(attribute.getName(), attribute.getValue().toByteArray());
        }
        return delete;
    }

    public static Append toAppend(final MutationProto proto, final CellScanner cellScanner) throws IOException {
        MutationType type = proto.getMutateType();
        assert type == MutationType.APPEND : type.name();
        byte[] row = proto.hasRow() ? proto.getRow().toByteArray() : null;
        Append append = null;
        int cellCount = proto.hasAssociatedCellCount() ? proto.getAssociatedCellCount() : 0;
        if (cellCount > 0) {
            if (cellScanner == null) {
                throw new DoNotRetryIOException("Cell count of " + cellCount + " but no cellScanner: " + toShortString(proto));
            }
            for (int i = 0; i < cellCount; i++) {
                if (!cellScanner.advance()) {
                    throw new DoNotRetryIOException("Cell count of " + cellCount + " but at index " + i + " no cell returned: " + toShortString(proto));
                }
                Cell cell = cellScanner.current();
                if (append == null) {
                    append = new Append(cell.getRowArray(), cell.getRowOffset(), cell.getRowLength());
                }
                append.add(cell);
            }
        } else {
            append = new Append(row);
            for (ColumnValue column : proto.getColumnValueList()) {
                byte[] family = column.getFamily().toByteArray();
                for (QualifierValue qv : column.getQualifierValueList()) {
                    byte[] qualifier = qv.getQualifier().toByteArray();
                    if (!qv.hasValue()) {
                        throw new DoNotRetryIOException("Missing required field: qualifier value");
                    }
                    byte[] value = qv.getValue().toByteArray();
                    byte[] tags = null;
                    if (qv.hasTags()) {
                        tags = qv.getTags().toByteArray();
                    }
                    append.add(CellUtil.createCell(row, family, qualifier, qv.getTimestamp(), KeyValue.Type.Put, value, tags));
                }
            }
        }
        append.setDurability(toDurability(proto.getDurability()));
        for (NameBytesPair attribute : proto.getAttributeList()) {
            append.setAttribute(attribute.getName(), attribute.getValue().toByteArray());
        }
        return append;
    }

    public static Mutation toMutation(final MutationProto proto) throws IOException {
        MutationType type = proto.getMutateType();
        if (type == MutationType.APPEND) {
            return toAppend(proto, null);
        }
        if (type == MutationType.DELETE) {
            return toDelete(proto, null);
        }
        if (type == MutationType.PUT) {
            return toPut(proto, null);
        }
        throw new IOException("Unknown mutation type " + type);
    }

    public static Increment toIncrement(final MutationProto proto, final CellScanner cellScanner) throws IOException {
        MutationType type = proto.getMutateType();
        assert type == MutationType.INCREMENT : type.name();
        byte[] row = proto.hasRow() ? proto.getRow().toByteArray() : null;
        Increment increment = null;
        int cellCount = proto.hasAssociatedCellCount() ? proto.getAssociatedCellCount() : 0;
        if (cellCount > 0) {
            if (cellScanner == null) {
                throw new DoNotRetryIOException("Cell count of " + cellCount + " but no cellScanner: " + TextFormat.shortDebugString(proto));
            }
            for (int i = 0; i < cellCount; i++) {
                if (!cellScanner.advance()) {
                    throw new DoNotRetryIOException("Cell count of " + cellCount + " but at index " + i + " no cell returned: " + TextFormat.shortDebugString(proto));
                }
                Cell cell = cellScanner.current();
                if (increment == null) {
                    increment = new Increment(cell.getRowArray(), cell.getRowOffset(), cell.getRowLength());
                }
                increment.add(cell);
            }
        } else {
            increment = new Increment(row);
            for (ColumnValue column : proto.getColumnValueList()) {
                byte[] family = column.getFamily().toByteArray();
                for (QualifierValue qv : column.getQualifierValueList()) {
                    byte[] qualifier = qv.getQualifier().toByteArray();
                    if (!qv.hasValue()) {
                        throw new DoNotRetryIOException("Missing required field: qualifier value");
                    }
                    byte[] value = qv.getValue().toByteArray();
                    byte[] tags = null;
                    if (qv.hasTags()) {
                        tags = qv.getTags().toByteArray();
                    }
                    increment.add(CellUtil.createCell(row, family, qualifier, qv.getTimestamp(), KeyValue.Type.Put, value, tags));
                }
            }
        }
        if (proto.hasTimeRange()) {
            HBaseProtos.TimeRange timeRange = proto.getTimeRange();
            long minStamp = 0;
            long maxStamp = Long.MAX_VALUE;
            if (timeRange.hasFrom()) {
                minStamp = timeRange.getFrom();
            }
            if (timeRange.hasTo()) {
                maxStamp = timeRange.getTo();
            }
            increment.setTimeRange(minStamp, maxStamp);
        }
        increment.setDurability(toDurability(proto.getDurability()));
        for (NameBytesPair attribute : proto.getAttributeList()) {
            increment.setAttribute(attribute.getName(), attribute.getValue().toByteArray());
        }
        return increment;
    }

    public static ClientProtos.Scan toScan(final Scan scan) throws IOException {
        ClientProtos.Scan.Builder scanBuilder = ClientProtos.Scan.newBuilder();
        scanBuilder.setCacheBlocks(scan.getCacheBlocks());
        if (scan.getBatch() > 0) {
            scanBuilder.setBatchSize(scan.getBatch());
        }
        if (scan.getMaxResultSize() > 0) {
            scanBuilder.setMaxResultSize(scan.getMaxResultSize());
        }
        if (scan.isSmall()) {
            scanBuilder.setSmall(scan.isSmall());
        }
        Boolean loadColumnFamiliesOnDemand = scan.getLoadColumnFamiliesOnDemandValue();
        if (loadColumnFamiliesOnDemand != null) {
            scanBuilder.setLoadColumnFamiliesOnDemand(loadColumnFamiliesOnDemand.booleanValue());
        }
        scanBuilder.setMaxVersions(scan.getMaxVersions());
        TimeRange timeRange = scan.getTimeRange();
        if (!timeRange.isAllTime()) {
            HBaseProtos.TimeRange.Builder timeRangeBuilder = HBaseProtos.TimeRange.newBuilder();
            timeRangeBuilder.setFrom(timeRange.getMin());
            timeRangeBuilder.setTo(timeRange.getMax());
            scanBuilder.setTimeRange(timeRangeBuilder.build());
        }
        Map<String, byte[]> attributes = scan.getAttributesMap();
        if (!attributes.isEmpty()) {
            NameBytesPair.Builder attributeBuilder = NameBytesPair.newBuilder();
            for (Map.Entry<String, byte[]> attribute : attributes.entrySet()) {
                attributeBuilder.setName(attribute.getKey());
                attributeBuilder.setValue(ByteStringer.wrap(attribute.getValue()));
                scanBuilder.addAttribute(attributeBuilder.build());
            }
        }
        byte[] startRow = scan.getStartRow();
        if (startRow != null && startRow.length > 0) {
            scanBuilder.setStartRow(ByteStringer.wrap(startRow));
        }
        byte[] stopRow = scan.getStopRow();
        if (stopRow != null && stopRow.length > 0) {
            scanBuilder.setStopRow(ByteStringer.wrap(stopRow));
        }
        if (scan.hasFilter()) {
            scanBuilder.setFilter(ProtobufUtil.toFilter(scan.getFilter()));
        }
        if (scan.hasFamilies()) {
            Column.Builder columnBuilder = Column.newBuilder();
            for (Map.Entry<byte[], NavigableSet<byte[]>> family : scan.getFamilyMap().entrySet()) {
                columnBuilder.setFamily(ByteStringer.wrap(family.getKey()));
                NavigableSet<byte[]> qualifiers = family.getValue();
                columnBuilder.clearQualifier();
                if (qualifiers != null && qualifiers.size() > 0) {
                    for (byte[] qualifier : qualifiers) {
                        columnBuilder.addQualifier(ByteStringer.wrap(qualifier));
                    }
                }
                scanBuilder.addColumn(columnBuilder.build());
            }
        }
        if (scan.getMaxResultsPerColumnFamily() >= 0) {
            scanBuilder.setStoreLimit(scan.getMaxResultsPerColumnFamily());
        }
        if (scan.getRowOffsetPerColumnFamily() > 0) {
            scanBuilder.setStoreOffset(scan.getRowOffsetPerColumnFamily());
        }
        if (scan.isReversed()) {
            scanBuilder.setReversed(scan.isReversed());
        }
        if (scan.getConsistency() == Consistency.TIMELINE) {
            scanBuilder.setConsistency(toConsistency(scan.getConsistency()));
        }
        if (scan.getCaching() > 0) {
            scanBuilder.setCaching(scan.getCaching());
        }
        return scanBuilder.build();
    }

    public static Scan toScan(final ClientProtos.Scan proto) throws IOException {
        byte[] startRow = HConstants.EMPTY_START_ROW;
        byte[] stopRow = HConstants.EMPTY_END_ROW;
        if (proto.hasStartRow()) {
            startRow = proto.getStartRow().toByteArray();
        }
        if (proto.hasStopRow()) {
            stopRow = proto.getStopRow().toByteArray();
        }
        Scan scan = new Scan(startRow, stopRow);
        if (proto.hasCacheBlocks()) {
            scan.setCacheBlocks(proto.getCacheBlocks());
        }
        if (proto.hasMaxVersions()) {
            scan.setMaxVersions(proto.getMaxVersions());
        }
        if (proto.hasStoreLimit()) {
            scan.setMaxResultsPerColumnFamily(proto.getStoreLimit());
        }
        if (proto.hasStoreOffset()) {
            scan.setRowOffsetPerColumnFamily(proto.getStoreOffset());
        }
        if (proto.hasLoadColumnFamiliesOnDemand()) {
            scan.setLoadColumnFamiliesOnDemand(proto.getLoadColumnFamiliesOnDemand());
        }
        if (proto.hasTimeRange()) {
            HBaseProtos.TimeRange timeRange = proto.getTimeRange();
            long minStamp = 0;
            long maxStamp = Long.MAX_VALUE;
            if (timeRange.hasFrom()) {
                minStamp = timeRange.getFrom();
            }
            if (timeRange.hasTo()) {
                maxStamp = timeRange.getTo();
            }
            scan.setTimeRange(minStamp, maxStamp);
        }
        if (proto.hasFilter()) {
            FilterProtos.Filter filter = proto.getFilter();
            scan.setFilter(ProtobufUtil.toFilter(filter));
        }
        if (proto.hasBatchSize()) {
            scan.setBatch(proto.getBatchSize());
        }
        if (proto.hasMaxResultSize()) {
            scan.setMaxResultSize(proto.getMaxResultSize());
        }
        if (proto.hasSmall()) {
            scan.setSmall(proto.getSmall());
        }
        for (NameBytesPair attribute : proto.getAttributeList()) {
            scan.setAttribute(attribute.getName(), attribute.getValue().toByteArray());
        }
        if (proto.getColumnCount() > 0) {
            for (Column column : proto.getColumnList()) {
                byte[] family = column.getFamily().toByteArray();
                if (column.getQualifierCount() > 0) {
                    for (ByteString qualifier : column.getQualifierList()) {
                        scan.addColumn(family, qualifier.toByteArray());
                    }
                } else {
                    scan.addFamily(family);
                }
            }
        }
        if (proto.hasReversed()) {
            scan.setReversed(proto.getReversed());
        }
        if (proto.hasConsistency()) {
            scan.setConsistency(toConsistency(proto.getConsistency()));
        }
        if (proto.hasCaching()) {
            scan.setCaching(proto.getCaching());
        }
        return scan;
    }

    public static ClientProtos.Get toGet(final Get get) throws IOException {
        ClientProtos.Get.Builder builder = ClientProtos.Get.newBuilder();
        builder.setRow(ByteStringer.wrap(get.getRow()));
        builder.setCacheBlocks(get.getCacheBlocks());
        builder.setMaxVersions(get.getMaxVersions());
        if (get.getFilter() != null) {
            builder.setFilter(ProtobufUtil.toFilter(get.getFilter()));
        }
        TimeRange timeRange = get.getTimeRange();
        if (!timeRange.isAllTime()) {
            HBaseProtos.TimeRange.Builder timeRangeBuilder = HBaseProtos.TimeRange.newBuilder();
            timeRangeBuilder.setFrom(timeRange.getMin());
            timeRangeBuilder.setTo(timeRange.getMax());
            builder.setTimeRange(timeRangeBuilder.build());
        }
        Map<String, byte[]> attributes = get.getAttributesMap();
        if (!attributes.isEmpty()) {
            NameBytesPair.Builder attributeBuilder = NameBytesPair.newBuilder();
            for (Map.Entry<String, byte[]> attribute : attributes.entrySet()) {
                attributeBuilder.setName(attribute.getKey());
                attributeBuilder.setValue(ByteStringer.wrap(attribute.getValue()));
                builder.addAttribute(attributeBuilder.build());
            }
        }
        if (get.hasFamilies()) {
            Column.Builder columnBuilder = Column.newBuilder();
            Map<byte[], NavigableSet<byte[]>> families = get.getFamilyMap();
            for (Map.Entry<byte[], NavigableSet<byte[]>> family : families.entrySet()) {
                NavigableSet<byte[]> qualifiers = family.getValue();
                columnBuilder.setFamily(ByteStringer.wrap(family.getKey()));
                columnBuilder.clearQualifier();
                if (qualifiers != null && qualifiers.size() > 0) {
                    for (byte[] qualifier : qualifiers) {
                        columnBuilder.addQualifier(ByteStringer.wrap(qualifier));
                    }
                }
                builder.addColumn(columnBuilder.build());
            }
        }
        if (get.getMaxResultsPerColumnFamily() >= 0) {
            builder.setStoreLimit(get.getMaxResultsPerColumnFamily());
        }
        if (get.getRowOffsetPerColumnFamily() > 0) {
            builder.setStoreOffset(get.getRowOffsetPerColumnFamily());
        }
        if (get.isCheckExistenceOnly()) {
            builder.setExistenceOnly(true);
        }
        if (get.getConsistency() != null && get.getConsistency() != Consistency.STRONG) {
            builder.setConsistency(toConsistency(get.getConsistency()));
        }
        return builder.build();
    }

    public static MutationProto toMutation(final Increment increment, final MutationProto.Builder builder, long nonce) {
        builder.setRow(ByteStringer.wrap(increment.getRow()));
        builder.setMutateType(MutationType.INCREMENT);
        builder.setDurability(toDurability(increment.getDurability()));
        if (nonce != HConstants.NO_NONCE) {
            builder.setNonce(nonce);
        }
        TimeRange timeRange = increment.getTimeRange();
        if (!timeRange.isAllTime()) {
            HBaseProtos.TimeRange.Builder timeRangeBuilder = HBaseProtos.TimeRange.newBuilder();
            timeRangeBuilder.setFrom(timeRange.getMin());
            timeRangeBuilder.setTo(timeRange.getMax());
            builder.setTimeRange(timeRangeBuilder.build());
        }
        ColumnValue.Builder columnBuilder = ColumnValue.newBuilder();
        QualifierValue.Builder valueBuilder = QualifierValue.newBuilder();
        for (Map.Entry<byte[], List<Cell>> family : increment.getFamilyCellMap().entrySet()) {
            columnBuilder.setFamily(ByteStringer.wrap(family.getKey()));
            columnBuilder.clearQualifierValue();
            List<Cell> values = family.getValue();
            if (values != null && values.size() > 0) {
                for (Cell cell : values) {
                    valueBuilder.clear();
                    valueBuilder.setQualifier(ByteStringer.wrap(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()));
                    valueBuilder.setValue(ByteStringer.wrap(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
                    if (cell.getTagsLength() > 0) {
                        valueBuilder.setTags(ByteStringer.wrap(cell.getTagsArray(), cell.getTagsOffset(), cell.getTagsLength()));
                    }
                    columnBuilder.addQualifierValue(valueBuilder.build());
                }
            }
            builder.addColumnValue(columnBuilder.build());
        }
        Map<String, byte[]> attributes = increment.getAttributesMap();
        if (!attributes.isEmpty()) {
            NameBytesPair.Builder attributeBuilder = NameBytesPair.newBuilder();
            for (Map.Entry<String, byte[]> attribute : attributes.entrySet()) {
                attributeBuilder.setName(attribute.getKey());
                attributeBuilder.setValue(ByteStringer.wrap(attribute.getValue()));
                builder.addAttribute(attributeBuilder.build());
            }
        }
        return builder.build();
    }

    public static MutationProto toMutation(final MutationType type, final Mutation mutation) throws IOException {
        return toMutation(type, mutation, HConstants.NO_NONCE);
    }

    public static MutationProto toMutation(final MutationType type, final Mutation mutation, final long nonce) throws IOException {
        return toMutation(type, mutation, MutationProto.newBuilder(), nonce);
    }

    public static MutationProto toMutation(final MutationType type, final Mutation mutation, MutationProto.Builder builder) throws IOException {
        return toMutation(type, mutation, builder, HConstants.NO_NONCE);
    }

    @SuppressWarnings("deprecation")
    public static MutationProto toMutation(final MutationType type, final Mutation mutation, MutationProto.Builder builder, long nonce) throws IOException {
        builder = getMutationBuilderAndSetCommonFields(type, mutation, builder);
        if (nonce != HConstants.NO_NONCE) {
            builder.setNonce(nonce);
        }
        ColumnValue.Builder columnBuilder = ColumnValue.newBuilder();
        QualifierValue.Builder valueBuilder = QualifierValue.newBuilder();
        for (Map.Entry<byte[], List<Cell>> family : mutation.getFamilyCellMap().entrySet()) {
            columnBuilder.clear();
            columnBuilder.setFamily(ByteStringer.wrap(family.getKey()));
            for (Cell cell : family.getValue()) {
                valueBuilder.clear();
                valueBuilder.setQualifier(ByteStringer.wrap(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()));
                valueBuilder.setValue(ByteStringer.wrap(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
                valueBuilder.setTimestamp(cell.getTimestamp());
                if (cell.getTagsLength() > 0) {
                    valueBuilder.setTags(ByteStringer.wrap(cell.getTagsArray(), cell.getTagsOffset(), cell.getTagsLength()));
                }
                if (type == MutationType.DELETE || (type == MutationType.PUT && CellUtil.isDelete(cell))) {
                    KeyValue.Type keyValueType = KeyValue.Type.codeToType(cell.getTypeByte());
                    valueBuilder.setDeleteType(toDeleteType(keyValueType));
                }
                columnBuilder.addQualifierValue(valueBuilder.build());
            }
            builder.addColumnValue(columnBuilder.build());
        }
        return builder.build();
    }

    public static MutationProto toMutationNoData(final MutationType type, final Mutation mutation, final MutationProto.Builder builder) throws IOException {
        return toMutationNoData(type, mutation, builder, HConstants.NO_NONCE);
    }

    public static MutationProto toMutationNoData(final MutationType type, final Mutation mutation) throws IOException {
        MutationProto.Builder builder = MutationProto.newBuilder();
        return toMutationNoData(type, mutation, builder);
    }

    public static MutationProto toMutationNoData(final MutationType type, final Mutation mutation, final MutationProto.Builder builder, long nonce) throws IOException {
        getMutationBuilderAndSetCommonFields(type, mutation, builder);
        builder.setAssociatedCellCount(mutation.size());
        if (nonce != HConstants.NO_NONCE) {
            builder.setNonce(nonce);
        }
        return builder.build();
    }

    private static MutationProto.Builder getMutationBuilderAndSetCommonFields(final MutationType type, final Mutation mutation, MutationProto.Builder builder) {
        builder.setRow(ByteStringer.wrap(mutation.getRow()));
        builder.setMutateType(type);
        builder.setDurability(toDurability(mutation.getDurability()));
        builder.setTimestamp(mutation.getTimeStamp());
        Map<String, byte[]> attributes = mutation.getAttributesMap();
        if (!attributes.isEmpty()) {
            NameBytesPair.Builder attributeBuilder = NameBytesPair.newBuilder();
            for (Map.Entry<String, byte[]> attribute : attributes.entrySet()) {
                attributeBuilder.setName(attribute.getKey());
                attributeBuilder.setValue(ByteStringer.wrap(attribute.getValue()));
                builder.addAttribute(attributeBuilder.build());
            }
        }
        return builder;
    }

    public static ClientProtos.Result toResult(final Result result) {
        if (result.getExists() != null) {
            return toResult(result.getExists(), result.isStale());
        }
        Cell[] cells = result.rawCells();
        if (cells == null || cells.length == 0) {
            return result.isStale() ? EMPTY_RESULT_PB_STALE : EMPTY_RESULT_PB;
        }
        ClientProtos.Result.Builder builder = ClientProtos.Result.newBuilder();
        for (Cell c : cells) {
            builder.addCell(toCell(c));
        }
        builder.setStale(result.isStale());
        builder.setPartial(result.isPartial());
        return builder.build();
    }

    public static ClientProtos.Result toResult(final boolean existence, boolean stale) {
        if (stale) {
            return existence ? EMPTY_RESULT_PB_EXISTS_TRUE_STALE : EMPTY_RESULT_PB_EXISTS_FALSE_STALE;
        } else {
            return existence ? EMPTY_RESULT_PB_EXISTS_TRUE : EMPTY_RESULT_PB_EXISTS_FALSE;
        }
    }

    public static ClientProtos.Result toResultNoData(final Result result) {
        if (result.getExists() != null)
            return toResult(result.getExists(), result.isStale());
        int size = result.size();
        if (size == 0)
            return result.isStale() ? EMPTY_RESULT_PB_STALE : EMPTY_RESULT_PB;
        ClientProtos.Result.Builder builder = ClientProtos.Result.newBuilder();
        builder.setAssociatedCellCount(size);
        builder.setStale(result.isStale());
        return builder.build();
    }

    public static Result toResult(final ClientProtos.Result proto) {
        if (proto.hasExists()) {
            if (proto.getStale()) {
                return proto.getExists() ? EMPTY_RESULT_EXISTS_TRUE_STALE : EMPTY_RESULT_EXISTS_FALSE_STALE;
            }
            return proto.getExists() ? EMPTY_RESULT_EXISTS_TRUE : EMPTY_RESULT_EXISTS_FALSE;
        }
        List<CellProtos.Cell> values = proto.getCellList();
        if (values.isEmpty()) {
            return proto.getStale() ? EMPTY_RESULT_STALE : EMPTY_RESULT;
        }
        List<Cell> cells = new ArrayList<Cell>(values.size());
        for (CellProtos.Cell c : values) {
            cells.add(toCell(c));
        }
        return Result.create(cells, null, proto.getStale(), proto.getPartial());
    }

    public static Result toResult(final ClientProtos.Result proto, final CellScanner scanner) throws IOException {
        List<CellProtos.Cell> values = proto.getCellList();
        if (proto.hasExists()) {
            if ((values != null && !values.isEmpty()) || (proto.hasAssociatedCellCount() && proto.getAssociatedCellCount() > 0)) {
                throw new IllegalArgumentException("bad proto: exists with cells is no allowed " + proto);
            }
            if (proto.getStale()) {
                return proto.getExists() ? EMPTY_RESULT_EXISTS_TRUE_STALE : EMPTY_RESULT_EXISTS_FALSE_STALE;
            }
            return proto.getExists() ? EMPTY_RESULT_EXISTS_TRUE : EMPTY_RESULT_EXISTS_FALSE;
        }
        List<Cell> cells = null;
        if (proto.hasAssociatedCellCount()) {
            int count = proto.getAssociatedCellCount();
            cells = new ArrayList<Cell>(count + values.size());
            for (int i = 0; i < count; i++) {
                if (!scanner.advance())
                    throw new IOException("Failed get " + i + " of " + count);
                cells.add(scanner.current());
            }
        }
        if (!values.isEmpty()) {
            if (cells == null)
                cells = new ArrayList<Cell>(values.size());
            for (CellProtos.Cell c : values) {
                cells.add(toCell(c));
            }
        }
        return (cells == null || cells.isEmpty()) ? (proto.getStale() ? EMPTY_RESULT_STALE : EMPTY_RESULT) : Result.create(cells, null, proto.getStale());
    }

    public static ComparatorProtos.Comparator toComparator(ByteArrayComparable comparator) {
        ComparatorProtos.Comparator.Builder builder = ComparatorProtos.Comparator.newBuilder();
        builder.setName(comparator.getClass().getName());
        builder.setSerializedComparator(ByteStringer.wrap(comparator.toByteArray()));
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    public static ByteArrayComparable toComparator(ComparatorProtos.Comparator proto) throws IOException {
        String type = proto.getName();
        String funcName = "parseFrom";
        byte[] value = proto.getSerializedComparator().toByteArray();
        try {
            Class<? extends ByteArrayComparable> c = (Class<? extends ByteArrayComparable>) Class.forName(type, true, CLASS_LOADER);
            Method parseFrom = c.getMethod(funcName, byte[].class);
            if (parseFrom == null) {
                throw new IOException("Unable to locate function: " + funcName + " in type: " + type);
            }
            return (ByteArrayComparable) parseFrom.invoke(null, value);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Filter toFilter(FilterProtos.Filter proto) throws IOException {
        String type = proto.getName();
        final byte[] value = proto.getSerializedFilter().toByteArray();
        String funcName = "parseFrom";
        try {
            Class<? extends Filter> c = (Class<? extends Filter>) Class.forName(type, true, CLASS_LOADER);
            Method parseFrom = c.getMethod(funcName, byte[].class);
            if (parseFrom == null) {
                throw new IOException("Unable to locate function: " + funcName + " in type: " + type);
            }
            return (Filter) parseFrom.invoke(c, value);
        } catch (Exception e) {
            throw new DoNotRetryIOException(e);
        }
    }

    public static FilterProtos.Filter toFilter(Filter filter) throws IOException {
        FilterProtos.Filter.Builder builder = FilterProtos.Filter.newBuilder();
        builder.setName(filter.getClass().getName());
        builder.setSerializedFilter(ByteStringer.wrap(filter.toByteArray()));
        return builder.build();
    }

    public static DeleteType toDeleteType(KeyValue.Type type) throws IOException {
        switch(type) {
            case Delete:
                return DeleteType.DELETE_ONE_VERSION;
            case DeleteColumn:
                return DeleteType.DELETE_MULTIPLE_VERSIONS;
            case DeleteFamily:
                return DeleteType.DELETE_FAMILY;
            case DeleteFamilyVersion:
                return DeleteType.DELETE_FAMILY_VERSION;
            default:
                throw new IOException("Unknown delete type: " + type);
        }
    }

    public static KeyValue.Type fromDeleteType(DeleteType type) throws IOException {
        switch(type) {
            case DELETE_ONE_VERSION:
                return KeyValue.Type.Delete;
            case DELETE_MULTIPLE_VERSIONS:
                return KeyValue.Type.DeleteColumn;
            case DELETE_FAMILY:
                return KeyValue.Type.DeleteFamily;
            case DELETE_FAMILY_VERSION:
                return KeyValue.Type.DeleteFamilyVersion;
            default:
                throw new IOException("Unknown delete type: " + type);
        }
    }

    @SuppressWarnings("unchecked")
    public static Throwable toException(final NameBytesPair parameter) throws IOException {
        if (parameter == null || !parameter.hasValue())
            return null;
        String desc = parameter.getValue().toStringUtf8();
        String type = parameter.getName();
        try {
            Class<? extends Throwable> c = (Class<? extends Throwable>) Class.forName(type, true, CLASS_LOADER);
            Constructor<? extends Throwable> cn = null;
            try {
                cn = c.getDeclaredConstructor(String.class);
                return cn.newInstance(desc);
            } catch (NoSuchMethodException e) {
                cn = c.getDeclaredConstructor(String.class, String.class);
                return cn.newInstance(type, desc);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static boolean bulkLoadHFile(final ClientService.BlockingInterface client, final List<Pair<byte[], String>> familyPaths, final byte[] regionName, boolean assignSeqNum) throws IOException {
        BulkLoadHFileRequest request = RequestConverter.buildBulkLoadHFileRequest(familyPaths, regionName, assignSeqNum);
        try {
            BulkLoadHFileResponse response = client.bulkLoadHFile(null, request);
            return response.getLoaded();
        } catch (ServiceException se) {
            throw getRemoteException(se);
        }
    }

    public static CoprocessorServiceResponse execService(final ClientService.BlockingInterface client, final CoprocessorServiceCall call, final byte[] regionName) throws IOException {
        CoprocessorServiceRequest request = CoprocessorServiceRequest.newBuilder().setCall(call).setRegion(RequestConverter.buildRegionSpecifier(REGION_NAME, regionName)).build();
        try {
            CoprocessorServiceResponse response = client.execService(null, request);
            return response;
        } catch (ServiceException se) {
            throw getRemoteException(se);
        }
    }

    public static CoprocessorServiceResponse execService(final MasterService.BlockingInterface client, final CoprocessorServiceCall call) throws IOException {
        CoprocessorServiceRequest request = CoprocessorServiceRequest.newBuilder().setCall(call).setRegion(RequestConverter.buildRegionSpecifier(REGION_NAME, HConstants.EMPTY_BYTE_ARRAY)).build();
        try {
            CoprocessorServiceResponse response = client.execMasterService(null, request);
            return response;
        } catch (ServiceException se) {
            throw getRemoteException(se);
        }
    }

    public static CoprocessorServiceResponse execRegionServerService(final ClientService.BlockingInterface client, final CoprocessorServiceCall call) throws IOException {
        CoprocessorServiceRequest request = CoprocessorServiceRequest.newBuilder().setCall(call).setRegion(RequestConverter.buildRegionSpecifier(REGION_NAME, HConstants.EMPTY_BYTE_ARRAY)).build();
        try {
            CoprocessorServiceResponse response = client.execRegionServerService(null, request);
            return response;
        } catch (ServiceException se) {
            throw getRemoteException(se);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Service> T newServiceStub(Class<T> service, RpcChannel channel) throws Exception {
        return (T) Methods.call(service, null, "newStub", new Class[] { RpcChannel.class }, new Object[] { channel });
    }

    public static HRegionInfo getRegionInfo(final AdminService.BlockingInterface admin, final byte[] regionName) throws IOException {
        try {
            GetRegionInfoRequest request = RequestConverter.buildGetRegionInfoRequest(regionName);
            GetRegionInfoResponse response = admin.getRegionInfo(null, request);
            return HRegionInfo.convert(response.getRegionInfo());
        } catch (ServiceException se) {
            throw getRemoteException(se);
        }
    }

    public static void closeRegion(final AdminService.BlockingInterface admin, final ServerName server, final byte[] regionName) throws IOException {
        CloseRegionRequest closeRegionRequest = RequestConverter.buildCloseRegionRequest(server, regionName);
        try {
            admin.closeRegion(null, closeRegionRequest);
        } catch (ServiceException se) {
            throw getRemoteException(se);
        }
    }

    public static boolean closeRegion(final AdminService.BlockingInterface admin, final ServerName server, final byte[] regionName, final ServerName destinationServer) throws IOException {
        CloseRegionRequest closeRegionRequest = RequestConverter.buildCloseRegionRequest(server, regionName, destinationServer);
        try {
            CloseRegionResponse response = admin.closeRegion(null, closeRegionRequest);
            return ResponseConverter.isClosed(response);
        } catch (ServiceException se) {
            throw getRemoteException(se);
        }
    }

    public static void warmupRegion(final AdminService.BlockingInterface admin, final HRegionInfo regionInfo) throws IOException {
        try {
            WarmupRegionRequest warmupRegionRequest = RequestConverter.buildWarmupRegionRequest(regionInfo);
            admin.warmupRegion(null, warmupRegionRequest);
        } catch (ServiceException e) {
            throw getRemoteException(e);
        }
    }

    public static void openRegion(final AdminService.BlockingInterface admin, ServerName server, final HRegionInfo region) throws IOException {
        OpenRegionRequest request = RequestConverter.buildOpenRegionRequest(server, region, null, null);
        try {
            admin.openRegion(null, request);
        } catch (ServiceException se) {
            throw ProtobufUtil.getRemoteException(se);
        }
    }

    public static List<HRegionInfo> getOnlineRegions(final AdminService.BlockingInterface admin) throws IOException {
        GetOnlineRegionRequest request = RequestConverter.buildGetOnlineRegionRequest();
        GetOnlineRegionResponse response = null;
        try {
            response = admin.getOnlineRegion(null, request);
        } catch (ServiceException se) {
            throw getRemoteException(se);
        }
        return getRegionInfos(response);
    }

    static List<HRegionInfo> getRegionInfos(final GetOnlineRegionResponse proto) {
        if (proto == null)
            return null;
        List<HRegionInfo> regionInfos = new ArrayList<HRegionInfo>();
        for (RegionInfo regionInfo : proto.getRegionInfoList()) {
            regionInfos.add(HRegionInfo.convert(regionInfo));
        }
        return regionInfos;
    }

    public static ServerInfo getServerInfo(final AdminService.BlockingInterface admin) throws IOException {
        GetServerInfoRequest request = RequestConverter.buildGetServerInfoRequest();
        try {
            GetServerInfoResponse response = admin.getServerInfo(null, request);
            return response.getServerInfo();
        } catch (ServiceException se) {
            throw getRemoteException(se);
        }
    }

    public static List<String> getStoreFiles(final AdminService.BlockingInterface admin, final byte[] regionName, final byte[] family) throws IOException {
        GetStoreFileRequest request = RequestConverter.buildGetStoreFileRequest(regionName, family);
        try {
            GetStoreFileResponse response = admin.getStoreFile(null, request);
            return response.getStoreFileList();
        } catch (ServiceException se) {
            throw ProtobufUtil.getRemoteException(se);
        }
    }

    public static void split(final AdminService.BlockingInterface admin, final HRegionInfo hri, byte[] splitPoint) throws IOException {
        SplitRegionRequest request = RequestConverter.buildSplitRegionRequest(hri.getRegionName(), splitPoint);
        try {
            admin.splitRegion(null, request);
        } catch (ServiceException se) {
            throw ProtobufUtil.getRemoteException(se);
        }
    }

    public static void mergeRegions(final AdminService.BlockingInterface admin, final HRegionInfo region_a, final HRegionInfo region_b, final boolean forcible) throws IOException {
        MergeRegionsRequest request = RequestConverter.buildMergeRegionsRequest(region_a.getRegionName(), region_b.getRegionName(), forcible);
        try {
            admin.mergeRegions(null, request);
        } catch (ServiceException se) {
            throw ProtobufUtil.getRemoteException(se);
        }
    }

    public static long getTotalRequestsCount(RegionLoad rl) {
        if (rl == null) {
            return 0;
        }
        return rl.getReadRequestsCount() + rl.getWriteRequestsCount();
    }

    public static byte[] toDelimitedByteArray(final Message m) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
        baos.write(ProtobufMagic.PB_MAGIC);
        m.writeDelimitedTo(baos);
        return baos.toByteArray();
    }

    public static Permission toPermission(AccessControlProtos.Permission proto) {
        if (proto.getType() != AccessControlProtos.Permission.Type.Global) {
            return toTablePermission(proto);
        } else {
            List<Permission.Action> actions = toPermissionActions(proto.getGlobalPermission().getActionList());
            return new Permission(actions.toArray(new Permission.Action[actions.size()]));
        }
    }

    public static TablePermission toTablePermission(AccessControlProtos.Permission proto) {
        if (proto.getType() == AccessControlProtos.Permission.Type.Global) {
            AccessControlProtos.GlobalPermission perm = proto.getGlobalPermission();
            List<Permission.Action> actions = toPermissionActions(perm.getActionList());
            return new TablePermission(null, null, null, actions.toArray(new Permission.Action[actions.size()]));
        }
        if (proto.getType() == AccessControlProtos.Permission.Type.Namespace) {
            AccessControlProtos.NamespacePermission perm = proto.getNamespacePermission();
            List<Permission.Action> actions = toPermissionActions(perm.getActionList());
            if (!proto.hasNamespacePermission()) {
                throw new IllegalStateException("Namespace must not be empty in NamespacePermission");
            }
            String namespace = perm.getNamespaceName().toStringUtf8();
            return new TablePermission(namespace, actions.toArray(new Permission.Action[actions.size()]));
        }
        if (proto.getType() == AccessControlProtos.Permission.Type.Table) {
            AccessControlProtos.TablePermission perm = proto.getTablePermission();
            List<Permission.Action> actions = toPermissionActions(perm.getActionList());
            byte[] qualifier = null;
            byte[] family = null;
            TableName table = null;
            if (!perm.hasTableName()) {
                throw new IllegalStateException("TableName cannot be empty");
            }
            table = ProtobufUtil.toTableName(perm.getTableName());
            if (perm.hasFamily())
                family = perm.getFamily().toByteArray();
            if (perm.hasQualifier())
                qualifier = perm.getQualifier().toByteArray();
            return new TablePermission(table, family, qualifier, actions.toArray(new Permission.Action[actions.size()]));
        }
        throw new IllegalStateException("Unrecognize Perm Type: " + proto.getType());
    }

    public static AccessControlProtos.Permission toPermission(Permission perm) {
        AccessControlProtos.Permission.Builder ret = AccessControlProtos.Permission.newBuilder();
        if (perm instanceof TablePermission) {
            TablePermission tablePerm = (TablePermission) perm;
            if (tablePerm.hasNamespace()) {
                ret.setType(AccessControlProtos.Permission.Type.Namespace);
                AccessControlProtos.NamespacePermission.Builder builder = AccessControlProtos.NamespacePermission.newBuilder();
                builder.setNamespaceName(ByteString.copyFromUtf8(tablePerm.getNamespace()));
                Permission.Action[] actions = perm.getActions();
                if (actions != null) {
                    for (Permission.Action a : actions) {
                        builder.addAction(toPermissionAction(a));
                    }
                }
                ret.setNamespacePermission(builder);
                return ret.build();
            } else if (tablePerm.hasTable()) {
                ret.setType(AccessControlProtos.Permission.Type.Table);
                AccessControlProtos.TablePermission.Builder builder = AccessControlProtos.TablePermission.newBuilder();
                builder.setTableName(ProtobufUtil.toProtoTableName(tablePerm.getTableName()));
                if (tablePerm.hasFamily()) {
                    builder.setFamily(ByteStringer.wrap(tablePerm.getFamily()));
                }
                if (tablePerm.hasQualifier()) {
                    builder.setQualifier(ByteStringer.wrap(tablePerm.getQualifier()));
                }
                Permission.Action[] actions = perm.getActions();
                if (actions != null) {
                    for (Permission.Action a : actions) {
                        builder.addAction(toPermissionAction(a));
                    }
                }
                ret.setTablePermission(builder);
                return ret.build();
            }
        }
        ret.setType(AccessControlProtos.Permission.Type.Global);
        AccessControlProtos.GlobalPermission.Builder builder = AccessControlProtos.GlobalPermission.newBuilder();
        Permission.Action[] actions = perm.getActions();
        if (actions != null) {
            for (Permission.Action a : actions) {
                builder.addAction(toPermissionAction(a));
            }
        }
        ret.setGlobalPermission(builder);
        return ret.build();
    }

    public static List<Permission.Action> toPermissionActions(List<AccessControlProtos.Permission.Action> protoActions) {
        List<Permission.Action> actions = new ArrayList<Permission.Action>(protoActions.size());
        for (AccessControlProtos.Permission.Action a : protoActions) {
            actions.add(toPermissionAction(a));
        }
        return actions;
    }

    public static Permission.Action toPermissionAction(AccessControlProtos.Permission.Action action) {
        switch(action) {
            case READ:
                return Permission.Action.READ;
            case WRITE:
                return Permission.Action.WRITE;
            case EXEC:
                return Permission.Action.EXEC;
            case CREATE:
                return Permission.Action.CREATE;
            case ADMIN:
                return Permission.Action.ADMIN;
        }
        throw new IllegalArgumentException("Unknown action value " + action.name());
    }

    public static AccessControlProtos.Permission.Action toPermissionAction(Permission.Action action) {
        switch(action) {
            case READ:
                return AccessControlProtos.Permission.Action.READ;
            case WRITE:
                return AccessControlProtos.Permission.Action.WRITE;
            case EXEC:
                return AccessControlProtos.Permission.Action.EXEC;
            case CREATE:
                return AccessControlProtos.Permission.Action.CREATE;
            case ADMIN:
                return AccessControlProtos.Permission.Action.ADMIN;
        }
        throw new IllegalArgumentException("Unknown action value " + action.name());
    }

    public static AccessControlProtos.UserPermission toUserPermission(UserPermission perm) {
        return AccessControlProtos.UserPermission.newBuilder().setUser(ByteStringer.wrap(perm.getUser())).setPermission(toPermission(perm)).build();
    }

    public static UserPermission toUserPermission(AccessControlProtos.UserPermission proto) {
        return new UserPermission(proto.getUser().toByteArray(), toTablePermission(proto.getPermission()));
    }

    public static AccessControlProtos.UsersAndPermissions toUserTablePermissions(ListMultimap<String, TablePermission> perm) {
        AccessControlProtos.UsersAndPermissions.Builder builder = AccessControlProtos.UsersAndPermissions.newBuilder();
        for (Map.Entry<String, Collection<TablePermission>> entry : perm.asMap().entrySet()) {
            AccessControlProtos.UsersAndPermissions.UserPermissions.Builder userPermBuilder = AccessControlProtos.UsersAndPermissions.UserPermissions.newBuilder();
            userPermBuilder.setUser(ByteString.copyFromUtf8(entry.getKey()));
            for (TablePermission tablePerm : entry.getValue()) {
                userPermBuilder.addPermissions(toPermission(tablePerm));
            }
            builder.addUserPermissions(userPermBuilder.build());
        }
        return builder.build();
    }

    public static void grant(AccessControlService.BlockingInterface protocol, String userShortName, Permission.Action... actions) throws ServiceException {
        List<AccessControlProtos.Permission.Action> permActions = Lists.newArrayListWithCapacity(actions.length);
        for (Permission.Action a : actions) {
            permActions.add(ProtobufUtil.toPermissionAction(a));
        }
        AccessControlProtos.GrantRequest request = RequestConverter.buildGrantRequest(userShortName, permActions.toArray(new AccessControlProtos.Permission.Action[actions.length]));
        protocol.grant(null, request);
    }

    public static void grant(AccessControlService.BlockingInterface protocol, String userShortName, TableName tableName, byte[] f, byte[] q, Permission.Action... actions) throws ServiceException {
        List<AccessControlProtos.Permission.Action> permActions = Lists.newArrayListWithCapacity(actions.length);
        for (Permission.Action a : actions) {
            permActions.add(ProtobufUtil.toPermissionAction(a));
        }
        AccessControlProtos.GrantRequest request = RequestConverter.buildGrantRequest(userShortName, tableName, f, q, permActions.toArray(new AccessControlProtos.Permission.Action[actions.length]));
        protocol.grant(null, request);
    }

    public static void grant(AccessControlService.BlockingInterface protocol, String userShortName, String namespace, Permission.Action... actions) throws ServiceException {
        List<AccessControlProtos.Permission.Action> permActions = Lists.newArrayListWithCapacity(actions.length);
        for (Permission.Action a : actions) {
            permActions.add(ProtobufUtil.toPermissionAction(a));
        }
        AccessControlProtos.GrantRequest request = RequestConverter.buildGrantRequest(userShortName, namespace, permActions.toArray(new AccessControlProtos.Permission.Action[actions.length]));
        protocol.grant(null, request);
    }

    public static void revoke(AccessControlService.BlockingInterface protocol, String userShortName, Permission.Action... actions) throws ServiceException {
        List<AccessControlProtos.Permission.Action> permActions = Lists.newArrayListWithCapacity(actions.length);
        for (Permission.Action a : actions) {
            permActions.add(ProtobufUtil.toPermissionAction(a));
        }
        AccessControlProtos.RevokeRequest request = RequestConverter.buildRevokeRequest(userShortName, permActions.toArray(new AccessControlProtos.Permission.Action[actions.length]));
        protocol.revoke(null, request);
    }

    public static void revoke(AccessControlService.BlockingInterface protocol, String userShortName, TableName tableName, byte[] f, byte[] q, Permission.Action... actions) throws ServiceException {
        List<AccessControlProtos.Permission.Action> permActions = Lists.newArrayListWithCapacity(actions.length);
        for (Permission.Action a : actions) {
            permActions.add(ProtobufUtil.toPermissionAction(a));
        }
        AccessControlProtos.RevokeRequest request = RequestConverter.buildRevokeRequest(userShortName, tableName, f, q, permActions.toArray(new AccessControlProtos.Permission.Action[actions.length]));
        protocol.revoke(null, request);
    }

    public static void revoke(AccessControlService.BlockingInterface protocol, String userShortName, String namespace, Permission.Action... actions) throws ServiceException {
        List<AccessControlProtos.Permission.Action> permActions = Lists.newArrayListWithCapacity(actions.length);
        for (Permission.Action a : actions) {
            permActions.add(ProtobufUtil.toPermissionAction(a));
        }
        AccessControlProtos.RevokeRequest request = RequestConverter.buildRevokeRequest(userShortName, namespace, permActions.toArray(new AccessControlProtos.Permission.Action[actions.length]));
        protocol.revoke(null, request);
    }

    public static List<UserPermission> getUserPermissions(AccessControlService.BlockingInterface protocol) throws ServiceException {
        AccessControlProtos.GetUserPermissionsRequest.Builder builder = AccessControlProtos.GetUserPermissionsRequest.newBuilder();
        builder.setType(AccessControlProtos.Permission.Type.Global);
        AccessControlProtos.GetUserPermissionsRequest request = builder.build();
        AccessControlProtos.GetUserPermissionsResponse response = protocol.getUserPermissions(null, request);
        List<UserPermission> perms = new ArrayList<UserPermission>(response.getUserPermissionCount());
        for (AccessControlProtos.UserPermission perm : response.getUserPermissionList()) {
            perms.add(ProtobufUtil.toUserPermission(perm));
        }
        return perms;
    }

    public static List<UserPermission> getUserPermissions(AccessControlService.BlockingInterface protocol, TableName t) throws ServiceException {
        AccessControlProtos.GetUserPermissionsRequest.Builder builder = AccessControlProtos.GetUserPermissionsRequest.newBuilder();
        if (t != null) {
            builder.setTableName(ProtobufUtil.toProtoTableName(t));
        }
        builder.setType(AccessControlProtos.Permission.Type.Table);
        AccessControlProtos.GetUserPermissionsRequest request = builder.build();
        AccessControlProtos.GetUserPermissionsResponse response = protocol.getUserPermissions(null, request);
        List<UserPermission> perms = new ArrayList<UserPermission>(response.getUserPermissionCount());
        for (AccessControlProtos.UserPermission perm : response.getUserPermissionList()) {
            perms.add(ProtobufUtil.toUserPermission(perm));
        }
        return perms;
    }

    public static List<UserPermission> getUserPermissions(AccessControlService.BlockingInterface protocol, byte[] namespace) throws ServiceException {
        AccessControlProtos.GetUserPermissionsRequest.Builder builder = AccessControlProtos.GetUserPermissionsRequest.newBuilder();
        if (namespace != null) {
            builder.setNamespaceName(ByteStringer.wrap(namespace));
        }
        builder.setType(AccessControlProtos.Permission.Type.Namespace);
        AccessControlProtos.GetUserPermissionsRequest request = builder.build();
        AccessControlProtos.GetUserPermissionsResponse response = protocol.getUserPermissions(null, request);
        List<UserPermission> perms = new ArrayList<UserPermission>(response.getUserPermissionCount());
        for (AccessControlProtos.UserPermission perm : response.getUserPermissionList()) {
            perms.add(ProtobufUtil.toUserPermission(perm));
        }
        return perms;
    }

    public static ListMultimap<String, TablePermission> toUserTablePermissions(AccessControlProtos.UsersAndPermissions proto) {
        ListMultimap<String, TablePermission> perms = ArrayListMultimap.create();
        AccessControlProtos.UsersAndPermissions.UserPermissions userPerm;
        for (int i = 0; i < proto.getUserPermissionsCount(); i++) {
            userPerm = proto.getUserPermissions(i);
            for (int j = 0; j < userPerm.getPermissionsCount(); j++) {
                TablePermission tablePerm = toTablePermission(userPerm.getPermissions(j));
                perms.put(userPerm.getUser().toStringUtf8(), tablePerm);
            }
        }
        return perms;
    }

    public static AuthenticationProtos.Token toToken(Token<AuthenticationTokenIdentifier> token) {
        AuthenticationProtos.Token.Builder builder = AuthenticationProtos.Token.newBuilder();
        builder.setIdentifier(ByteStringer.wrap(token.getIdentifier()));
        builder.setPassword(ByteStringer.wrap(token.getPassword()));
        if (token.getService() != null) {
            builder.setService(ByteString.copyFromUtf8(token.getService().toString()));
        }
        return builder.build();
    }

    public static Token<AuthenticationTokenIdentifier> toToken(AuthenticationProtos.Token proto) {
        return new Token<AuthenticationTokenIdentifier>(proto.hasIdentifier() ? proto.getIdentifier().toByteArray() : null, proto.hasPassword() ? proto.getPassword().toByteArray() : null, AuthenticationTokenIdentifier.AUTH_TOKEN_TYPE, proto.hasService() ? new Text(proto.getService().toStringUtf8()) : null);
    }

    public static String getRegionEncodedName(final RegionSpecifier regionSpecifier) throws DoNotRetryIOException {
        byte[] value = regionSpecifier.getValue().toByteArray();
        RegionSpecifierType type = regionSpecifier.getType();
        switch(type) {
            case REGION_NAME:
                return HRegionInfo.encodeRegionName(value);
            case ENCODED_REGION_NAME:
                return Bytes.toString(value);
            default:
                throw new DoNotRetryIOException("Unsupported region specifier type: " + type);
        }
    }

    public static ScanMetrics toScanMetrics(final byte[] bytes) {
        Parser<MapReduceProtos.ScanMetrics> parser = MapReduceProtos.ScanMetrics.PARSER;
        MapReduceProtos.ScanMetrics pScanMetrics = null;
        try {
            pScanMetrics = parser.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
        }
        ScanMetrics scanMetrics = new ScanMetrics();
        if (pScanMetrics != null) {
            for (HBaseProtos.NameInt64Pair pair : pScanMetrics.getMetricsList()) {
                if (pair.hasName() && pair.hasValue()) {
                    scanMetrics.setCounter(pair.getName(), pair.getValue());
                }
            }
        }
        return scanMetrics;
    }

    public static MapReduceProtos.ScanMetrics toScanMetrics(ScanMetrics scanMetrics) {
        MapReduceProtos.ScanMetrics.Builder builder = MapReduceProtos.ScanMetrics.newBuilder();
        Map<String, Long> metrics = scanMetrics.getMetricsMap();
        for (Entry<String, Long> e : metrics.entrySet()) {
            HBaseProtos.NameInt64Pair nameInt64Pair = HBaseProtos.NameInt64Pair.newBuilder().setName(e.getKey()).setValue(e.getValue()).build();
            builder.addMetrics(nameInt64Pair);
        }
        return builder.build();
    }

    public static void toIOException(ServiceException se) throws IOException {
        if (se == null) {
            throw new NullPointerException("Null service exception passed!");
        }
        Throwable cause = se.getCause();
        if (cause != null && cause instanceof IOException) {
            throw (IOException) cause;
        }
        throw new IOException(se);
    }

    public static CellProtos.Cell toCell(final Cell kv) {
        CellProtos.Cell.Builder kvbuilder = CellProtos.Cell.newBuilder();
        kvbuilder.setRow(ByteStringer.wrap(kv.getRowArray(), kv.getRowOffset(), kv.getRowLength()));
        kvbuilder.setFamily(ByteStringer.wrap(kv.getFamilyArray(), kv.getFamilyOffset(), kv.getFamilyLength()));
        kvbuilder.setQualifier(ByteStringer.wrap(kv.getQualifierArray(), kv.getQualifierOffset(), kv.getQualifierLength()));
        kvbuilder.setCellType(CellProtos.CellType.valueOf(kv.getTypeByte()));
        kvbuilder.setTimestamp(kv.getTimestamp());
        kvbuilder.setValue(ByteStringer.wrap(kv.getValueArray(), kv.getValueOffset(), kv.getValueLength()));
        return kvbuilder.build();
    }

    public static Cell toCell(final CellProtos.Cell cell) {
        return CellUtil.createCell(cell.getRow().toByteArray(), cell.getFamily().toByteArray(), cell.getQualifier().toByteArray(), cell.getTimestamp(), (byte) cell.getCellType().getNumber(), cell.getValue().toByteArray());
    }

    public static HBaseProtos.NamespaceDescriptor toProtoNamespaceDescriptor(NamespaceDescriptor ns) {
        HBaseProtos.NamespaceDescriptor.Builder b = HBaseProtos.NamespaceDescriptor.newBuilder().setName(ByteString.copyFromUtf8(ns.getName()));
        for (Map.Entry<String, String> entry : ns.getConfiguration().entrySet()) {
            b.addConfiguration(HBaseProtos.NameStringPair.newBuilder().setName(entry.getKey()).setValue(entry.getValue()));
        }
        return b.build();
    }

    public static NamespaceDescriptor toNamespaceDescriptor(HBaseProtos.NamespaceDescriptor desc) throws IOException {
        NamespaceDescriptor.Builder b = NamespaceDescriptor.create(desc.getName().toStringUtf8());
        for (HBaseProtos.NameStringPair prop : desc.getConfigurationList()) {
            b.addConfiguration(prop.getName(), prop.getValue());
        }
        return b.build();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Message> T getParsedGenericInstance(Class<?> runtimeClass, int position, ByteString b) throws IOException {
        Type type = runtimeClass.getGenericSuperclass();
        Type argType = ((ParameterizedType) type).getActualTypeArguments()[position];
        Class<T> classType = (Class<T>) argType;
        T inst;
        try {
            Method m = classType.getMethod("parseFrom", ByteString.class);
            inst = (T) m.invoke(null, b);
            return inst;
        } catch (SecurityException e) {
            throw new IOException(e);
        } catch (NoSuchMethodException e) {
            throw new IOException(e);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        } catch (InvocationTargetException e) {
            throw new IOException(e);
        } catch (IllegalAccessException e) {
            throw new IOException(e);
        }
    }

    @SuppressWarnings("deprecation")
    public static CompactionDescriptor toCompactionDescriptor(HRegionInfo info, byte[] family, List<Path> inputPaths, List<Path> outputPaths, Path storeDir) {
        CompactionDescriptor.Builder builder = CompactionDescriptor.newBuilder().setTableName(ByteStringer.wrap(info.getTable().toBytes())).setEncodedRegionName(ByteStringer.wrap(info.getEncodedNameAsBytes())).setFamilyName(ByteStringer.wrap(family)).setStoreHomeDir(storeDir.getName());
        for (Path inputPath : inputPaths) {
            builder.addCompactionInput(inputPath.getName());
        }
        for (Path outputPath : outputPaths) {
            builder.addCompactionOutput(outputPath.getName());
        }
        builder.setRegionName(ByteStringer.wrap(info.getRegionName()));
        return builder.build();
    }

    public static FlushDescriptor toFlushDescriptor(FlushAction action, HRegionInfo hri, long flushSeqId, Map<byte[], List<Path>> committedFiles) {
        FlushDescriptor.Builder desc = FlushDescriptor.newBuilder().setAction(action).setEncodedRegionName(ByteStringer.wrap(hri.getEncodedNameAsBytes())).setRegionName(ByteStringer.wrap(hri.getRegionName())).setFlushSequenceNumber(flushSeqId).setTableName(ByteStringer.wrap(hri.getTable().getName()));
        for (Map.Entry<byte[], List<Path>> entry : committedFiles.entrySet()) {
            WALProtos.FlushDescriptor.StoreFlushDescriptor.Builder builder = WALProtos.FlushDescriptor.StoreFlushDescriptor.newBuilder().setFamilyName(ByteStringer.wrap(entry.getKey())).setStoreHomeDir(Bytes.toString(entry.getKey()));
            if (entry.getValue() != null) {
                for (Path path : entry.getValue()) {
                    builder.addFlushOutput(path.getName());
                }
            }
            desc.addStoreFlushes(builder);
        }
        return desc.build();
    }

    public static RegionEventDescriptor toRegionEventDescriptor(EventType eventType, HRegionInfo hri, long seqId, ServerName server, Map<byte[], List<Path>> storeFiles) {
        RegionEventDescriptor.Builder desc = RegionEventDescriptor.newBuilder().setEventType(eventType).setTableName(ByteStringer.wrap(hri.getTable().getName())).setEncodedRegionName(ByteStringer.wrap(hri.getEncodedNameAsBytes())).setRegionName(ByteStringer.wrap(hri.getRegionName())).setLogSequenceNumber(seqId).setServer(toServerName(server));
        for (Map.Entry<byte[], List<Path>> entry : storeFiles.entrySet()) {
            StoreDescriptor.Builder builder = StoreDescriptor.newBuilder().setFamilyName(ByteStringer.wrap(entry.getKey())).setStoreHomeDir(Bytes.toString(entry.getKey()));
            for (Path path : entry.getValue()) {
                builder.addStoreFile(path.getName());
            }
            desc.addStores(builder);
        }
        return desc.build();
    }

    public static String getShortTextFormat(Message m) {
        if (m == null)
            return "null";
        if (m instanceof ScanRequest) {
            return TextFormat.shortDebugString(m);
        } else if (m instanceof RegionServerReportRequest) {
            RegionServerReportRequest r = (RegionServerReportRequest) m;
            return "server " + TextFormat.shortDebugString(r.getServer()) + " load { numberOfRequests: " + r.getLoad().getNumberOfRequests() + " }";
        } else if (m instanceof RegionServerStartupRequest) {
            return TextFormat.shortDebugString(m);
        } else if (m instanceof MutationProto) {
            return toShortString((MutationProto) m);
        } else if (m instanceof GetRequest) {
            GetRequest r = (GetRequest) m;
            return "region= " + getStringForByteString(r.getRegion().getValue()) + ", row=" + getStringForByteString(r.getGet().getRow());
        } else if (m instanceof ClientProtos.MultiRequest) {
            ClientProtos.MultiRequest r = (ClientProtos.MultiRequest) m;
            ClientProtos.RegionAction actions = r.getRegionActionList().get(0);
            String row = actions.getActionCount() <= 0 ? "" : getStringForByteString(actions.getAction(0).hasGet() ? actions.getAction(0).getGet().getRow() : actions.getAction(0).getMutation().getRow());
            return "region= " + getStringForByteString(actions.getRegion().getValue()) + ", for " + r.getRegionActionCount() + " actions and 1st row key=" + row;
        } else if (m instanceof ClientProtos.MutateRequest) {
            ClientProtos.MutateRequest r = (ClientProtos.MutateRequest) m;
            return "region= " + getStringForByteString(r.getRegion().getValue()) + ", row=" + getStringForByteString(r.getMutation().getRow());
        }
        return "TODO: " + m.getClass().toString();
    }

    private static String getStringForByteString(ByteString bs) {
        return Bytes.toStringBinary(bs.toByteArray());
    }

    static String toShortString(final MutationProto proto) {
        return "row=" + Bytes.toString(proto.getRow().toByteArray()) + ", type=" + proto.getMutateType().toString();
    }

    public static TableName toTableName(HBaseProtos.TableName tableNamePB) {
        return TableName.valueOf(tableNamePB.getNamespace().asReadOnlyByteBuffer(), tableNamePB.getQualifier().asReadOnlyByteBuffer());
    }

    public static HBaseProtos.TableName toProtoTableName(TableName tableName) {
        return HBaseProtos.TableName.newBuilder().setNamespace(ByteStringer.wrap(tableName.getNamespace())).setQualifier(ByteStringer.wrap(tableName.getQualifier())).build();
    }

    public static TableName[] getTableNameArray(List<HBaseProtos.TableName> tableNamesList) {
        if (tableNamesList == null) {
            return new TableName[0];
        }
        TableName[] tableNames = new TableName[tableNamesList.size()];
        for (int i = 0; i < tableNamesList.size(); i++) {
            tableNames[i] = toTableName(tableNamesList.get(i));
        }
        return tableNames;
    }

    public static CellVisibility toCellVisibility(ClientProtos.CellVisibility proto) {
        if (proto == null)
            return null;
        return new CellVisibility(proto.getExpression());
    }

    public static CellVisibility toCellVisibility(byte[] protoBytes) throws DeserializationException {
        if (protoBytes == null)
            return null;
        ClientProtos.CellVisibility.Builder builder = ClientProtos.CellVisibility.newBuilder();
        ClientProtos.CellVisibility proto = null;
        try {
            proto = builder.mergeFrom(protoBytes).build();
        } catch (InvalidProtocolBufferException e) {
            throw new DeserializationException(e);
        }
        return toCellVisibility(proto);
    }

    public static ClientProtos.CellVisibility toCellVisibility(CellVisibility cellVisibility) {
        ClientProtos.CellVisibility.Builder builder = ClientProtos.CellVisibility.newBuilder();
        builder.setExpression(cellVisibility.getExpression());
        return builder.build();
    }

    public static Authorizations toAuthorizations(ClientProtos.Authorizations proto) {
        if (proto == null)
            return null;
        return new Authorizations(proto.getLabelList());
    }

    public static Authorizations toAuthorizations(byte[] protoBytes) throws DeserializationException {
        if (protoBytes == null)
            return null;
        ClientProtos.Authorizations.Builder builder = ClientProtos.Authorizations.newBuilder();
        ClientProtos.Authorizations proto = null;
        try {
            proto = builder.mergeFrom(protoBytes).build();
        } catch (InvalidProtocolBufferException e) {
            throw new DeserializationException(e);
        }
        return toAuthorizations(proto);
    }

    public static ClientProtos.Authorizations toAuthorizations(Authorizations authorizations) {
        ClientProtos.Authorizations.Builder builder = ClientProtos.Authorizations.newBuilder();
        for (String label : authorizations.getLabels()) {
            builder.addLabel(label);
        }
        return builder.build();
    }

    public static AccessControlProtos.UsersAndPermissions toUsersAndPermissions(String user, Permission perms) {
        return AccessControlProtos.UsersAndPermissions.newBuilder().addUserPermissions(AccessControlProtos.UsersAndPermissions.UserPermissions.newBuilder().setUser(ByteString.copyFromUtf8(user)).addPermissions(toPermission(perms)).build()).build();
    }

    public static AccessControlProtos.UsersAndPermissions toUsersAndPermissions(ListMultimap<String, Permission> perms) {
        AccessControlProtos.UsersAndPermissions.Builder builder = AccessControlProtos.UsersAndPermissions.newBuilder();
        for (Map.Entry<String, Collection<Permission>> entry : perms.asMap().entrySet()) {
            AccessControlProtos.UsersAndPermissions.UserPermissions.Builder userPermBuilder = AccessControlProtos.UsersAndPermissions.UserPermissions.newBuilder();
            userPermBuilder.setUser(ByteString.copyFromUtf8(entry.getKey()));
            for (Permission perm : entry.getValue()) {
                userPermBuilder.addPermissions(toPermission(perm));
            }
            builder.addUserPermissions(userPermBuilder.build());
        }
        return builder.build();
    }

    public static ListMultimap<String, Permission> toUsersAndPermissions(AccessControlProtos.UsersAndPermissions proto) {
        ListMultimap<String, Permission> result = ArrayListMultimap.create();
        for (AccessControlProtos.UsersAndPermissions.UserPermissions userPerms : proto.getUserPermissionsList()) {
            String user = userPerms.getUser().toStringUtf8();
            for (AccessControlProtos.Permission perm : userPerms.getPermissionsList()) {
                result.put(user, toPermission(perm));
            }
        }
        return result;
    }

    public static TimeUnit toTimeUnit(final HBaseProtos.TimeUnit proto) {
        switch(proto) {
            case NANOSECONDS:
                return TimeUnit.NANOSECONDS;
            case MICROSECONDS:
                return TimeUnit.MICROSECONDS;
            case MILLISECONDS:
                return TimeUnit.MILLISECONDS;
            case SECONDS:
                return TimeUnit.SECONDS;
            case MINUTES:
                return TimeUnit.MINUTES;
            case HOURS:
                return TimeUnit.HOURS;
            case DAYS:
                return TimeUnit.DAYS;
        }
        throw new RuntimeException("Invalid TimeUnit " + proto);
    }

    public static HBaseProtos.TimeUnit toProtoTimeUnit(final TimeUnit timeUnit) {
        switch(timeUnit) {
            case NANOSECONDS:
                return HBaseProtos.TimeUnit.NANOSECONDS;
            case MICROSECONDS:
                return HBaseProtos.TimeUnit.MICROSECONDS;
            case MILLISECONDS:
                return HBaseProtos.TimeUnit.MILLISECONDS;
            case SECONDS:
                return HBaseProtos.TimeUnit.SECONDS;
            case MINUTES:
                return HBaseProtos.TimeUnit.MINUTES;
            case HOURS:
                return HBaseProtos.TimeUnit.HOURS;
            case DAYS:
                return HBaseProtos.TimeUnit.DAYS;
        }
        throw new RuntimeException("Invalid TimeUnit " + timeUnit);
    }

    public static ThrottleType toThrottleType(final QuotaProtos.ThrottleType proto) {
        switch(proto) {
            case REQUEST_NUMBER:
                return ThrottleType.REQUEST_NUMBER;
            case REQUEST_SIZE:
                return ThrottleType.REQUEST_SIZE;
            case WRITE_NUMBER:
                return ThrottleType.WRITE_NUMBER;
            case WRITE_SIZE:
                return ThrottleType.WRITE_SIZE;
            case READ_NUMBER:
                return ThrottleType.READ_NUMBER;
            case READ_SIZE:
                return ThrottleType.READ_SIZE;
        }
        throw new RuntimeException("Invalid ThrottleType " + proto);
    }

    public static QuotaProtos.ThrottleType toProtoThrottleType(final ThrottleType type) {
        switch(type) {
            case REQUEST_NUMBER:
                return QuotaProtos.ThrottleType.REQUEST_NUMBER;
            case REQUEST_SIZE:
                return QuotaProtos.ThrottleType.REQUEST_SIZE;
            case WRITE_NUMBER:
                return QuotaProtos.ThrottleType.WRITE_NUMBER;
            case WRITE_SIZE:
                return QuotaProtos.ThrottleType.WRITE_SIZE;
            case READ_NUMBER:
                return QuotaProtos.ThrottleType.READ_NUMBER;
            case READ_SIZE:
                return QuotaProtos.ThrottleType.READ_SIZE;
        }
        throw new RuntimeException("Invalid ThrottleType " + type);
    }

    public static QuotaScope toQuotaScope(final QuotaProtos.QuotaScope proto) {
        switch(proto) {
            case CLUSTER:
                return QuotaScope.CLUSTER;
            case MACHINE:
                return QuotaScope.MACHINE;
        }
        throw new RuntimeException("Invalid QuotaScope " + proto);
    }

    public static QuotaProtos.QuotaScope toProtoQuotaScope(final QuotaScope scope) {
        switch(scope) {
            case CLUSTER:
                return QuotaProtos.QuotaScope.CLUSTER;
            case MACHINE:
                return QuotaProtos.QuotaScope.MACHINE;
        }
        throw new RuntimeException("Invalid QuotaScope " + scope);
    }

    public static QuotaType toQuotaScope(final QuotaProtos.QuotaType proto) {
        switch(proto) {
            case THROTTLE:
                return QuotaType.THROTTLE;
        }
        throw new RuntimeException("Invalid QuotaType " + proto);
    }

    public static QuotaProtos.QuotaType toProtoQuotaScope(final QuotaType type) {
        switch(type) {
            case THROTTLE:
                return QuotaProtos.QuotaType.THROTTLE;
        }
        throw new RuntimeException("Invalid QuotaType " + type);
    }

    public static QuotaProtos.TimedQuota toTimedQuota(final long limit, final TimeUnit timeUnit, final QuotaScope scope) {
        return QuotaProtos.TimedQuota.newBuilder().setSoftLimit(limit).setTimeUnit(toProtoTimeUnit(timeUnit)).setScope(toProtoQuotaScope(scope)).build();
    }

    public static WALProtos.BulkLoadDescriptor toBulkLoadDescriptor(TableName tableName, ByteString encodedRegionName, Map<byte[], List<Path>> storeFiles, long bulkloadSeqId) {
        BulkLoadDescriptor.Builder desc = BulkLoadDescriptor.newBuilder().setTableName(ProtobufUtil.toProtoTableName(tableName)).setEncodedRegionName(encodedRegionName).setBulkloadSeqNum(bulkloadSeqId);
        for (Map.Entry<byte[], List<Path>> entry : storeFiles.entrySet()) {
            WALProtos.StoreDescriptor.Builder builder = StoreDescriptor.newBuilder().setFamilyName(ByteStringer.wrap(entry.getKey())).setStoreHomeDir(Bytes.toString(entry.getKey()));
            for (Path path : entry.getValue()) {
                builder.addStoreFile(path.getName());
            }
            desc.addStores(builder);
        }
        return desc.build();
    }

    public static ReplicationLoadSink toReplicationLoadSink(ClusterStatusProtos.ReplicationLoadSink cls) {
        return new ReplicationLoadSink(cls.getAgeOfLastAppliedOp(), cls.getTimeStampsOfLastAppliedOp());
    }

    public static ReplicationLoadSource toReplicationLoadSource(ClusterStatusProtos.ReplicationLoadSource cls) {
        return new ReplicationLoadSource(cls.getPeerID(), cls.getAgeOfLastShippedOp(), cls.getSizeOfLogQueue(), cls.getTimeStampOfLastShippedOp(), cls.getReplicationLag());
    }

    public static List<ReplicationLoadSource> toReplicationLoadSourceList(List<ClusterStatusProtos.ReplicationLoadSource> clsList) {
        ArrayList<ReplicationLoadSource> rlsList = new ArrayList<ReplicationLoadSource>();
        for (ClusterStatusProtos.ReplicationLoadSource cls : clsList) {
            rlsList.add(toReplicationLoadSource(cls));
        }
        return rlsList;
    }

    public static RPCProtos.VersionInfo getVersionInfo() {
        RPCProtos.VersionInfo.Builder builder = RPCProtos.VersionInfo.newBuilder();
        builder.setVersion(VersionInfo.getVersion());
        builder.setUrl(VersionInfo.getUrl());
        builder.setRevision(VersionInfo.getRevision());
        builder.setUser(VersionInfo.getUser());
        builder.setDate(VersionInfo.getDate());
        builder.setSrcChecksum(VersionInfo.getSrcChecksum());
        return builder.build();
    }
}
