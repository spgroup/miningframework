package org.apache.cassandra.io.sstable;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import com.google.common.collect.ImmutableMap;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.Config;
import org.apache.cassandra.config.KSMetaData;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.cql3.*;
import org.apache.cassandra.cql3.statements.CreateTableStatement;
import org.apache.cassandra.cql3.statements.ParsedStatement;
import org.apache.cassandra.cql3.statements.UpdateStatement;
import org.apache.cassandra.db.ArrayBackedSortedColumns;
import org.apache.cassandra.db.Cell;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.composites.Composite;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.Murmur3Partitioner;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.io.sstable.format.SSTableFormat;
import org.apache.cassandra.locator.AbstractReplicationStrategy;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.utils.Pair;

public class CQLSSTableWriter implements Closeable {

    static {
        Config.setClientMode(true);
    }

    private final AbstractSSTableSimpleWriter writer;

    private final UpdateStatement insert;

    private final List<ColumnSpecification> boundNames;

    private CQLSSTableWriter(AbstractSSTableSimpleWriter writer, UpdateStatement insert, List<ColumnSpecification> boundNames) {
        this.writer = writer;
        this.insert = insert;
        this.boundNames = boundNames;
    }

    public static Builder builder() {
        return new Builder();
    }

    public CQLSSTableWriter addRow(Object... values) throws InvalidRequestException, IOException {
        return addRow(Arrays.asList(values));
    }

    public CQLSSTableWriter addRow(List<Object> values) throws InvalidRequestException, IOException {
        int size = Math.min(values.size(), boundNames.size());
        List<ByteBuffer> rawValues = new ArrayList<>(size);
        for (int i = 0; i < size; i++) rawValues.add(values.get(i) == null ? null : ((AbstractType) boundNames.get(i).type).decompose(values.get(i)));
        return rawAddRow(rawValues);
    }

    public CQLSSTableWriter addRow(Map<String, Object> values) throws InvalidRequestException, IOException {
        int size = boundNames.size();
        List<ByteBuffer> rawValues = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ColumnSpecification spec = boundNames.get(i);
            Object value = values.get(spec.name.toString());
            rawValues.add(value == null ? null : ((AbstractType) spec.type).decompose(value));
        }
        return rawAddRow(rawValues);
    }

    public CQLSSTableWriter rawAddRow(ByteBuffer... values) throws InvalidRequestException, IOException {
        return rawAddRow(Arrays.asList(values));
    }

    public CQLSSTableWriter rawAddRow(List<ByteBuffer> values) throws InvalidRequestException, IOException {
        if (values.size() != boundNames.size())
            throw new InvalidRequestException(String.format("Invalid number of arguments, expecting %d values but got %d", boundNames.size(), values.size()));
        QueryOptions options = QueryOptions.forInternalCalls(null, values);
        List<ByteBuffer> keys = insert.buildPartitionKeyNames(options);
        Composite clusteringPrefix = insert.createClusteringPrefix(options);
        long now = System.currentTimeMillis() * 1000;
        UpdateParameters params = new UpdateParameters(insert.cfm, options, insert.getTimestamp(now, options), insert.getTimeToLive(options), Collections.<ByteBuffer, CQL3Row>emptyMap());
        try {
            for (ByteBuffer key : keys) {
                if (writer.shouldStartNewRow() || !key.equals(writer.currentKey().getKey()))
                    writer.newRow(key);
                insert.addUpdateForKey(writer.currentColumnFamily(), key, clusteringPrefix, params, false);
            }
            return this;
        } catch (BufferedWriter.SyncException e) {
            throw (IOException) e.getCause();
        }
    }

    public CQLSSTableWriter rawAddRow(Map<String, ByteBuffer> values) throws InvalidRequestException, IOException {
        int size = Math.min(values.size(), boundNames.size());
        List<ByteBuffer> rawValues = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ColumnSpecification spec = boundNames.get(i);
            rawValues.add(values.get(spec.name.toString()));
        }
        return rawAddRow(rawValues);
    }

    public void close() throws IOException {
        writer.close();
    }

    public Descriptor getCurrentDescriptor() {
        return writer.getCurrentDescriptor();
    }

    public CFMetaData getCFMetaData() {
        return writer.metadata;
    }

    public static class Builder {

        private File directory;

        private IPartitioner partitioner = Murmur3Partitioner.instance;

        protected SSTableFormat.Type formatType = null;

        private CFMetaData schema;

        private UpdateStatement insert;

        private List<ColumnSpecification> boundNames;

        private boolean sorted = false;

        private long bufferSizeInMB = 128;

        protected Builder() {
        }

        public Builder inDirectory(String directory) {
            return inDirectory(new File(directory));
        }

        public Builder inDirectory(File directory) {
            if (!directory.exists())
                throw new IllegalArgumentException(directory + " doesn't exists");
            if (!directory.canWrite())
                throw new IllegalArgumentException(directory + " exists but is not writable");
            this.directory = directory;
            return this;
        }

        public Builder forTable(String schema) {
            try {
                synchronized (CQLSSTableWriter.class) {
                    this.schema = getStatement(schema, CreateTableStatement.class, "CREATE TABLE").left.getCFMetaData().rebuild();
                    KSMetaData ksm = Schema.instance.getKSMetaData(this.schema.ksName);
                    if (ksm == null) {
                        createKeyspaceWithTable(this.schema);
                    } else if (Schema.instance.getCFMetaData(this.schema.ksName, this.schema.cfName) == null) {
                        addTableToKeyspace(ksm, this.schema);
                    }
                    return this;
                }
            } catch (RequestValidationException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }

        CFMetaData metadata() {
            return schema;
        }

        private static void createKeyspaceWithTable(CFMetaData table) {
            KSMetaData ksm;
            ksm = KSMetaData.newKeyspace(table.ksName, AbstractReplicationStrategy.getClass("org.apache.cassandra.locator.SimpleStrategy"), ImmutableMap.of("replication_factor", "1"), true, Collections.singleton(table));
            Schema.instance.load(ksm);
        }

        private static void addTableToKeyspace(KSMetaData keyspace, CFMetaData table) {
            KSMetaData clone = keyspace.cloneWithTableAdded(table);
            Schema.instance.load(table);
            Schema.instance.setKeyspaceDefinition(clone);
        }

        public Builder withPartitioner(IPartitioner partitioner) {
            this.partitioner = partitioner;
            return this;
        }

        public Builder using(String insertStatement) {
            if (schema == null)
                throw new IllegalStateException("You need to define the schema by calling forTable() prior to this call.");
            Pair<UpdateStatement, List<ColumnSpecification>> p = getStatement(insertStatement, UpdateStatement.class, "INSERT");
            this.insert = p.left;
            this.boundNames = p.right;
            if (this.insert.hasConditions())
                throw new IllegalArgumentException("Conditional statements are not supported");
            if (this.insert.isCounter())
                throw new IllegalArgumentException("Counter update statements are not supported");
            if (this.boundNames.isEmpty())
                throw new IllegalArgumentException("Provided insert statement has no bind variables");
            return this;
        }

        public Builder withBufferSizeInMB(int size) {
            this.bufferSizeInMB = size;
            return this;
        }

        public Builder sorted() {
            this.sorted = true;
            return this;
        }

        private static <T extends CQLStatement> Pair<T, List<ColumnSpecification>> getStatement(String query, Class<T> klass, String type) {
            try {
                ClientState state = ClientState.forInternalCalls();
                ParsedStatement.Prepared prepared = QueryProcessor.getStatement(query, state);
                CQLStatement stmt = prepared.statement;
                stmt.validate(state);
                if (!stmt.getClass().equals(klass))
                    throw new IllegalArgumentException("Invalid query, must be a " + type + " statement");
                return Pair.create(klass.cast(stmt), prepared.boundNames);
            } catch (RequestValidationException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }

        @SuppressWarnings("resource")
        public CQLSSTableWriter build() {
            if (directory == null)
                throw new IllegalStateException("No ouptut directory specified, you should provide a directory with inDirectory()");
            if (schema == null)
                throw new IllegalStateException("Missing schema, you should provide the schema for the SSTable to create with forTable()");
            if (insert == null)
                throw new IllegalStateException("No insert statement specified, you should provide an insert statement through using()");
            AbstractSSTableSimpleWriter writer = sorted ? new SSTableSimpleWriter(directory, schema, partitioner) : new BufferedWriter(directory, schema, partitioner, bufferSizeInMB);
            if (formatType != null)
                writer.setSSTableFormatType(formatType);
            return new CQLSSTableWriter(writer, insert, boundNames);
        }
    }

    private static class BufferedWriter extends SSTableSimpleUnsortedWriter {

        private boolean needsSync = false;

        public BufferedWriter(File directory, CFMetaData metadata, IPartitioner partitioner, long bufferSizeInMB) {
            super(directory, metadata, partitioner, bufferSizeInMB);
        }

        @Override
        protected ColumnFamily createColumnFamily() {
            return new ArrayBackedSortedColumns(metadata, false) {

                @Override
                public void addColumn(Cell cell) {
                    super.addColumn(cell);
                    try {
                        countColumn(cell);
                    } catch (IOException e) {
                        throw new SyncException(e);
                    }
                }
            };
        }

        @Override
        protected void replaceColumnFamily() throws IOException {
            needsSync = true;
        }

        @Override
        boolean shouldStartNewRow() throws IOException {
            if (needsSync) {
                needsSync = false;
                super.sync();
                return true;
            }
            return super.shouldStartNewRow();
        }

        protected void addColumn(Cell cell) throws IOException {
            throw new UnsupportedOperationException();
        }

        static class SyncException extends RuntimeException {

            SyncException(IOException ioe) {
                super(ioe);
            }
        }
    }
}
