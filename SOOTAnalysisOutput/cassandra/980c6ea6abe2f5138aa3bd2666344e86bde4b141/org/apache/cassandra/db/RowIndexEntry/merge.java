package org.apache.cassandra.db;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import com.codahale.metrics.Histogram;
import org.apache.cassandra.cache.IMeasurableMemory;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.io.ISerializer;
import org.apache.cassandra.io.sstable.IndexInfo;
import org.apache.cassandra.io.sstable.format.Version;
import org.apache.cassandra.io.util.DataInputPlus;
import org.apache.cassandra.io.util.DataOutputBuffer;
import org.apache.cassandra.io.util.DataOutputPlus;
import org.apache.cassandra.io.util.FileDataInput;
import org.apache.cassandra.io.util.FileHandle;
import org.apache.cassandra.io.util.RandomAccessReader;
import org.apache.cassandra.io.util.TrackedDataInputPlus;
import org.apache.cassandra.metrics.DefaultNameFactory;
import org.apache.cassandra.metrics.MetricNameFactory;
import org.apache.cassandra.utils.ObjectSizes;
import org.apache.cassandra.utils.vint.VIntCoding;
import org.github.jamm.Unmetered;
import static org.apache.cassandra.metrics.CassandraMetricsRegistry.Metrics;

public class RowIndexEntry<T> implements IMeasurableMemory {

    private static final long EMPTY_SIZE = ObjectSizes.measure(new RowIndexEntry(0));

    static final int CACHE_NOT_INDEXED = 0;

    static final int CACHE_INDEXED = 1;

    static final int CACHE_INDEXED_SHALLOW = 2;

    static final Histogram indexEntrySizeHistogram;

    static final Histogram indexInfoCountHistogram;

    static final Histogram indexInfoGetsHistogram;

    static final Histogram indexInfoReadsHistogram;

    static {
        MetricNameFactory factory = new DefaultNameFactory("Index", "RowIndexEntry");
        indexEntrySizeHistogram = Metrics.histogram(factory.createMetricName("IndexedEntrySize"), false);
        indexInfoCountHistogram = Metrics.histogram(factory.createMetricName("IndexInfoCount"), false);
        indexInfoGetsHistogram = Metrics.histogram(factory.createMetricName("IndexInfoGets"), false);
        indexInfoReadsHistogram = Metrics.histogram(factory.createMetricName("IndexInfoReads"), false);
    }

    public final long position;

    public RowIndexEntry(long position) {
        this.position = position;
    }

    public boolean isIndexed() {
        return columnsIndexCount() > 1;
    }

    public boolean indexOnHeap() {
        return false;
    }

    public DeletionTime deletionTime() {
        throw new UnsupportedOperationException();
    }

    public int columnsIndexCount() {
        return 0;
    }

    public long unsharedHeapSize() {
        return EMPTY_SIZE;
    }

    public static RowIndexEntry<IndexInfo> create(long dataFilePosition, long indexFilePosition, DeletionTime deletionTime, long headerLength, int columnIndexCount, int indexedPartSize, List<IndexInfo> indexSamples, int[] offsets, ISerializer<IndexInfo> idxInfoSerializer) {
        if (indexSamples != null && indexSamples.size() > 1)
            return new IndexedEntry(dataFilePosition, deletionTime, headerLength, indexSamples.toArray(new IndexInfo[indexSamples.size()]), offsets, indexedPartSize, idxInfoSerializer);
        if (columnIndexCount > 1)
            return new ShallowIndexedEntry(dataFilePosition, indexFilePosition, deletionTime, headerLength, columnIndexCount, indexedPartSize, idxInfoSerializer);
        return new RowIndexEntry<>(dataFilePosition);
    }

    public IndexInfoRetriever openWithIndex(FileHandle indexFile) {
        return null;
    }

    public interface IndexSerializer<T> {

        void serialize(RowIndexEntry<T> rie, DataOutputPlus out, ByteBuffer indexInfo) throws IOException;

        RowIndexEntry<T> deserialize(DataInputPlus in, long indexFilePosition) throws IOException;

        default RowIndexEntry<T> deserialize(RandomAccessReader reader) throws IOException {
            return deserialize(reader, reader.getFilePointer());
        }

        default RowIndexEntry<T> deserialize(FileDataInput input) throws IOException {
            return deserialize(input, input.getFilePointer());
        }

        void serializeForCache(RowIndexEntry<T> rie, DataOutputPlus out) throws IOException;

        RowIndexEntry<T> deserializeForCache(DataInputPlus in) throws IOException;

        long deserializePositionAndSkip(DataInputPlus in) throws IOException;

        ISerializer<T> indexInfoSerializer();
    }

    public static final class Serializer implements IndexSerializer<IndexInfo> {

        private final IndexInfo.Serializer idxInfoSerializer;

        private final Version version;

        public Serializer(Version version, SerializationHeader header) {
            this.idxInfoSerializer = IndexInfo.serializer(version, header);
            this.version = version;
        }

        public IndexInfo.Serializer indexInfoSerializer() {
            return idxInfoSerializer;
        }

        public void serialize(RowIndexEntry<IndexInfo> rie, DataOutputPlus out, ByteBuffer indexInfo) throws IOException {
            rie.serialize(out, indexInfo);
        }

        public void serializeForCache(RowIndexEntry<IndexInfo> rie, DataOutputPlus out) throws IOException {
            rie.serializeForCache(out);
        }

        public RowIndexEntry<IndexInfo> deserializeForCache(DataInputPlus in) throws IOException {
            long position = in.readUnsignedVInt();
            switch(in.readByte()) {
                case CACHE_NOT_INDEXED:
                    return new RowIndexEntry<>(position);
                case CACHE_INDEXED:
                    return new IndexedEntry(position, in, idxInfoSerializer);
                case CACHE_INDEXED_SHALLOW:
                    return new ShallowIndexedEntry(position, in, idxInfoSerializer);
                default:
                    throw new AssertionError();
            }
        }

        public static void skipForCache(DataInputPlus in) throws IOException {
            in.readUnsignedVInt();
            switch(in.readByte()) {
                case CACHE_NOT_INDEXED:
                    break;
                case CACHE_INDEXED:
                    IndexedEntry.skipForCache(in);
                    break;
                case CACHE_INDEXED_SHALLOW:
                    ShallowIndexedEntry.skipForCache(in);
                    break;
                default:
                    assert false;
            }
        }

        public RowIndexEntry<IndexInfo> deserialize(DataInputPlus in, long indexFilePosition) throws IOException {
            long position = in.readUnsignedVInt();
            int size = (int) in.readUnsignedVInt();
            if (size == 0) {
                return new RowIndexEntry<>(position);
            } else {
                long headerLength = in.readUnsignedVInt();
                DeletionTime deletionTime = DeletionTime.serializer.deserialize(in);
                int columnsIndexCount = (int) in.readUnsignedVInt();
                int indexedPartSize = size - serializedSize(deletionTime, headerLength, columnsIndexCount);
                if (size <= DatabaseDescriptor.getColumnIndexCacheSize()) {
                    return new IndexedEntry(position, in, deletionTime, headerLength, columnsIndexCount, idxInfoSerializer, indexedPartSize);
                } else {
                    in.skipBytes(indexedPartSize);
                    return new ShallowIndexedEntry(position, indexFilePosition, deletionTime, headerLength, columnsIndexCount, indexedPartSize, idxInfoSerializer);
                }
            }
        }

        public long deserializePositionAndSkip(DataInputPlus in) throws IOException {
            long position = in.readUnsignedVInt();
            int size = (int) in.readUnsignedVInt();
            if (size > 0)
                in.skipBytesFully(size);
            return position;
        }

        public static long readPosition(DataInputPlus in) throws IOException {
            return in.readUnsignedVInt();
        }

        public static void skip(DataInputPlus in, Version version) throws IOException {
            readPosition(in);
            skipPromotedIndex(in);
        }

        private static void skipPromotedIndex(DataInputPlus in) throws IOException {
            int size = (int) in.readUnsignedVInt();
            if (size <= 0)
                return;
            in.skipBytesFully(size);
        }

        public static void serializeOffsets(DataOutputBuffer out, int[] indexOffsets, int columnIndexCount) throws IOException {
            for (int i = 0; i < columnIndexCount; i++) out.writeInt(indexOffsets[i]);
        }
    }

    private static int serializedSize(DeletionTime deletionTime, long headerLength, int columnIndexCount) {
        return TypeSizes.sizeofUnsignedVInt(headerLength) + (int) DeletionTime.serializer.serializedSize(deletionTime) + TypeSizes.sizeofUnsignedVInt(columnIndexCount);
    }

    public void serialize(DataOutputPlus out, ByteBuffer indexInfo) throws IOException {
        out.writeUnsignedVInt(position);
        out.writeUnsignedVInt(0);
    }

    public void serializeForCache(DataOutputPlus out) throws IOException {
        out.writeUnsignedVInt(position);
        out.writeByte(CACHE_NOT_INDEXED);
    }

    private static final class IndexedEntry extends RowIndexEntry<IndexInfo> {

        private static final long BASE_SIZE;

        static {
            BASE_SIZE = ObjectSizes.measure(new IndexedEntry(0, DeletionTime.LIVE, 0, null, null, 0, null));
        }

        private final DeletionTime deletionTime;

        private final long headerLength;

        private final IndexInfo[] columnsIndex;

        private final int[] offsets;

        private final int indexedPartSize;

        @Unmetered
        private final ISerializer<IndexInfo> idxInfoSerializer;

        private IndexedEntry(long dataFilePosition, DeletionTime deletionTime, long headerLength, IndexInfo[] columnsIndex, int[] offsets, int indexedPartSize, ISerializer<IndexInfo> idxInfoSerializer) {
            super(dataFilePosition);
            this.headerLength = headerLength;
            this.deletionTime = deletionTime;
            this.columnsIndex = columnsIndex;
            this.offsets = offsets;
            this.indexedPartSize = indexedPartSize;
            this.idxInfoSerializer = idxInfoSerializer;
        }

        private IndexedEntry(long dataFilePosition, DataInputPlus in, DeletionTime deletionTime, long headerLength, int columnIndexCount, IndexInfo.Serializer idxInfoSerializer, int indexedPartSize) throws IOException {
            super(dataFilePosition);
            this.headerLength = headerLength;
            this.deletionTime = deletionTime;
            int columnsIndexCount = columnIndexCount;
            this.columnsIndex = new IndexInfo[columnsIndexCount];
            for (int i = 0; i < columnsIndexCount; i++) this.columnsIndex[i] = idxInfoSerializer.deserialize(in);
            this.offsets = new int[this.columnsIndex.length];
            for (int i = 0; i < offsets.length; i++) offsets[i] = in.readInt();
            this.indexedPartSize = indexedPartSize;
            this.idxInfoSerializer = idxInfoSerializer;
        }

        private IndexedEntry(long dataFilePosition, DataInputPlus in, ISerializer<IndexInfo> idxInfoSerializer) throws IOException {
            super(dataFilePosition);
            this.headerLength = in.readUnsignedVInt();
            this.deletionTime = DeletionTime.serializer.deserialize(in);
            int columnsIndexCount = (int) in.readUnsignedVInt();
            TrackedDataInputPlus trackedIn = new TrackedDataInputPlus(in);
            this.columnsIndex = new IndexInfo[columnsIndexCount];
            for (int i = 0; i < columnsIndexCount; i++) this.columnsIndex[i] = idxInfoSerializer.deserialize(trackedIn);
            this.offsets = null;
            this.indexedPartSize = (int) trackedIn.getBytesRead();
            this.idxInfoSerializer = idxInfoSerializer;
        }

        @Override
        public boolean indexOnHeap() {
            return true;
        }

        @Override
        public int columnsIndexCount() {
            return columnsIndex.length;
        }

        @Override
        public DeletionTime deletionTime() {
            return deletionTime;
        }

        @Override
        public IndexInfoRetriever openWithIndex(FileHandle indexFile) {
            indexEntrySizeHistogram.update(serializedSize(deletionTime, headerLength, columnsIndex.length) + indexedPartSize);
            indexInfoCountHistogram.update(columnsIndex.length);
            return new IndexInfoRetriever() {

                private int retrievals;

                @Override
                public IndexInfo columnsIndex(int index) {
                    retrievals++;
                    return columnsIndex[index];
                }

                public void close() {
                    indexInfoGetsHistogram.update(retrievals);
                }
            };
        }

        @Override
        public long unsharedHeapSize() {
            long entrySize = 0;
            for (IndexInfo idx : columnsIndex) entrySize += idx.unsharedHeapSize();
            return BASE_SIZE + entrySize + ObjectSizes.sizeOfReferenceArray(columnsIndex.length);
        }

        public void serialize(DataOutputPlus out, ByteBuffer indexInfo) throws IOException {
            assert indexedPartSize != Integer.MIN_VALUE;
            out.writeUnsignedVInt(position);
            out.writeUnsignedVInt(serializedSize(deletionTime, headerLength, columnsIndex.length) + indexedPartSize);
            out.writeUnsignedVInt(headerLength);
            DeletionTime.serializer.serialize(deletionTime, out);
            out.writeUnsignedVInt(columnsIndex.length);
            for (IndexInfo info : columnsIndex) idxInfoSerializer.serialize(info, out);
            for (int offset : offsets) out.writeInt(offset);
        }

        @Override
        public void serializeForCache(DataOutputPlus out) throws IOException {
            out.writeUnsignedVInt(position);
            out.writeByte(CACHE_INDEXED);
            out.writeUnsignedVInt(headerLength);
            DeletionTime.serializer.serialize(deletionTime, out);
            out.writeUnsignedVInt(columnsIndexCount());
            for (IndexInfo indexInfo : columnsIndex) idxInfoSerializer.serialize(indexInfo, out);
        }

        static void skipForCache(DataInputPlus in) throws IOException {
            in.readUnsignedVInt();
            DeletionTime.serializer.skip(in);
            in.readUnsignedVInt();
            in.readUnsignedVInt();
        }
    }

    private static final class ShallowIndexedEntry extends RowIndexEntry<IndexInfo> {

        private static final long BASE_SIZE;

        static {
            BASE_SIZE = ObjectSizes.measure(new ShallowIndexedEntry(0, 0, DeletionTime.LIVE, 0, 10, 0, null));
        }

        private final long indexFilePosition;

        private final DeletionTime deletionTime;

        private final long headerLength;

        private final int columnsIndexCount;

        private final int indexedPartSize;

        private final int offsetsOffset;

        @Unmetered
        private final ISerializer<IndexInfo> idxInfoSerializer;

        private final int fieldsSerializedSize;

        private ShallowIndexedEntry(long dataFilePosition, long indexFilePosition, DeletionTime deletionTime, long headerLength, int columnIndexCount, int indexedPartSize, ISerializer<IndexInfo> idxInfoSerializer) {
            super(dataFilePosition);
            assert columnIndexCount > 1;
            this.indexFilePosition = indexFilePosition;
            this.headerLength = headerLength;
            this.deletionTime = deletionTime;
            this.columnsIndexCount = columnIndexCount;
            this.indexedPartSize = indexedPartSize;
            this.idxInfoSerializer = idxInfoSerializer;
            this.fieldsSerializedSize = serializedSize(deletionTime, headerLength, columnIndexCount);
            this.offsetsOffset = indexedPartSize + fieldsSerializedSize - columnsIndexCount * TypeSizes.INT_SIZE;
        }

        private ShallowIndexedEntry(long dataFilePosition, DataInputPlus in, IndexInfo.Serializer idxInfoSerializer) throws IOException {
            super(dataFilePosition);
            this.indexFilePosition = in.readUnsignedVInt();
            this.headerLength = in.readUnsignedVInt();
            this.deletionTime = DeletionTime.serializer.deserialize(in);
            this.columnsIndexCount = (int) in.readUnsignedVInt();
            this.indexedPartSize = (int) in.readUnsignedVInt();
            this.idxInfoSerializer = idxInfoSerializer;
            this.fieldsSerializedSize = serializedSize(deletionTime, headerLength, columnsIndexCount);
            this.offsetsOffset = indexedPartSize + fieldsSerializedSize - columnsIndexCount * TypeSizes.INT_SIZE;
        }

        @Override
        public int columnsIndexCount() {
            return columnsIndexCount;
        }

        @Override
        public DeletionTime deletionTime() {
            return deletionTime;
        }

        @Override
        public IndexInfoRetriever openWithIndex(FileHandle indexFile) {
            indexEntrySizeHistogram.update(indexedPartSize + fieldsSerializedSize);
            indexInfoCountHistogram.update(columnsIndexCount);
            return new ShallowInfoRetriever(indexFilePosition + VIntCoding.computeUnsignedVIntSize(position) + VIntCoding.computeUnsignedVIntSize(indexedPartSize + fieldsSerializedSize) + fieldsSerializedSize, offsetsOffset - fieldsSerializedSize, indexFile.createReader(), idxInfoSerializer);
        }

        @Override
        public long unsharedHeapSize() {
            return BASE_SIZE;
        }

        @Override
        public void serialize(DataOutputPlus out, ByteBuffer indexInfo) throws IOException {
            out.writeUnsignedVInt(position);
            out.writeUnsignedVInt(fieldsSerializedSize + indexInfo.limit());
            out.writeUnsignedVInt(headerLength);
            DeletionTime.serializer.serialize(deletionTime, out);
            out.writeUnsignedVInt(columnsIndexCount);
            out.write(indexInfo);
        }

        @Override
        public void serializeForCache(DataOutputPlus out) throws IOException {
            out.writeUnsignedVInt(position);
            out.writeByte(CACHE_INDEXED_SHALLOW);
            out.writeUnsignedVInt(indexFilePosition);
            out.writeUnsignedVInt(headerLength);
            DeletionTime.serializer.serialize(deletionTime, out);
            out.writeUnsignedVInt(columnsIndexCount);
            out.writeUnsignedVInt(indexedPartSize);
        }

        static void skipForCache(DataInputPlus in) throws IOException {
            in.readUnsignedVInt();
            in.readUnsignedVInt();
            DeletionTime.serializer.skip(in);
            in.readUnsignedVInt();
            in.readUnsignedVInt();
        }
    }

    private static final class ShallowInfoRetriever extends FileIndexInfoRetriever {

        private final int offsetsOffset;

        private ShallowInfoRetriever(long indexInfoFilePosition, int offsetsOffset, FileDataInput indexReader, ISerializer<IndexInfo> idxInfoSerializer) {
            super(indexInfoFilePosition, indexReader, idxInfoSerializer);
            this.offsetsOffset = offsetsOffset;
        }

        IndexInfo fetchIndex(int index) throws IOException {
            indexReader.seek(indexInfoFilePosition + offsetsOffset + index * TypeSizes.INT_SIZE);
            int indexInfoPos = indexReader.readInt();
            indexReader.seek(indexInfoFilePosition + indexInfoPos);
            return idxInfoSerializer.deserialize(indexReader);
        }
    }

    public interface IndexInfoRetriever extends AutoCloseable {

        IndexInfo columnsIndex(int index) throws IOException;

        void close() throws IOException;
    }

    private abstract static class FileIndexInfoRetriever implements IndexInfoRetriever {

        final long indexInfoFilePosition;

        final ISerializer<IndexInfo> idxInfoSerializer;

        final FileDataInput indexReader;

        int retrievals;

        FileIndexInfoRetriever(long indexInfoFilePosition, FileDataInput indexReader, ISerializer<IndexInfo> idxInfoSerializer) {
            this.indexInfoFilePosition = indexInfoFilePosition;
            this.idxInfoSerializer = idxInfoSerializer;
            this.indexReader = indexReader;
        }

        public final IndexInfo columnsIndex(int index) throws IOException {
            retrievals++;
            return fetchIndex(index);
        }

        abstract IndexInfo fetchIndex(int index) throws IOException;

        public void close() throws IOException {
            indexReader.close();
            indexInfoGetsHistogram.update(retrievals);
            indexInfoReadsHistogram.update(retrievals);
        }
    }
}
