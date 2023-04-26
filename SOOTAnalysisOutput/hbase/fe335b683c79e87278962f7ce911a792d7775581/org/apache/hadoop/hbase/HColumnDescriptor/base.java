package org.apache.hadoop.hbase;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.hbase.exceptions.DeserializationException;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
import org.apache.hadoop.hbase.protobuf.ProtobufUtil;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair;
import org.apache.hadoop.hbase.regionserver.BloomType;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.PrettyPrinter;
import org.apache.hadoop.hbase.util.PrettyPrinter.Unit;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import com.google.common.base.Preconditions;
import org.apache.hadoop.hbase.util.ByteStringer;
import com.google.protobuf.InvalidProtocolBufferException;

@InterfaceAudience.Public
@InterfaceStability.Evolving
public class HColumnDescriptor implements WritableComparable<HColumnDescriptor> {

    private static final byte COLUMN_DESCRIPTOR_VERSION = (byte) 11;

    public static final String COMPRESSION = "COMPRESSION";

    public static final String COMPRESSION_COMPACT = "COMPRESSION_COMPACT";

    public static final String ENCODE_ON_DISK = "ENCODE_ON_DISK";

    public static final String DATA_BLOCK_ENCODING = "DATA_BLOCK_ENCODING";

    public static final String BLOCKCACHE = "BLOCKCACHE";

    public static final String CACHE_DATA_ON_WRITE = "CACHE_DATA_ON_WRITE";

    public static final String CACHE_INDEX_ON_WRITE = "CACHE_INDEX_ON_WRITE";

    public static final String CACHE_BLOOMS_ON_WRITE = "CACHE_BLOOMS_ON_WRITE";

    public static final String EVICT_BLOCKS_ON_CLOSE = "EVICT_BLOCKS_ON_CLOSE";

    public static final String CACHE_DATA_IN_L1 = "CACHE_DATA_IN_L1";

    public static final String PREFETCH_BLOCKS_ON_OPEN = "PREFETCH_BLOCKS_ON_OPEN";

    public static final String BLOCKSIZE = "BLOCKSIZE";

    public static final String LENGTH = "LENGTH";

    public static final String TTL = "TTL";

    public static final String BLOOMFILTER = "BLOOMFILTER";

    public static final String FOREVER = "FOREVER";

    public static final String REPLICATION_SCOPE = "REPLICATION_SCOPE";

    public static final byte[] REPLICATION_SCOPE_BYTES = Bytes.toBytes(REPLICATION_SCOPE);

    public static final String MIN_VERSIONS = "MIN_VERSIONS";

    public static final String KEEP_DELETED_CELLS = "KEEP_DELETED_CELLS";

    public static final String COMPRESS_TAGS = "COMPRESS_TAGS";

    public static final String ENCRYPTION = "ENCRYPTION";

    public static final String ENCRYPTION_KEY = "ENCRYPTION_KEY";

    public static final String DEFAULT_COMPRESSION = Compression.Algorithm.NONE.getName();

    public static final boolean DEFAULT_ENCODE_ON_DISK = true;

    public static final String DEFAULT_DATA_BLOCK_ENCODING = DataBlockEncoding.NONE.toString();

    public static final int DEFAULT_VERSIONS = HBaseConfiguration.create().getInt("hbase.column.max.version", 1);

    public static final int DEFAULT_MIN_VERSIONS = 0;

    private volatile Integer blocksize = null;

    public static final boolean DEFAULT_IN_MEMORY = false;

    public static final boolean DEFAULT_KEEP_DELETED = false;

    public static final boolean DEFAULT_BLOCKCACHE = true;

    public static final boolean DEFAULT_CACHE_DATA_ON_WRITE = false;

    public static final boolean DEFAULT_CACHE_DATA_IN_L1 = false;

    public static final boolean DEFAULT_CACHE_INDEX_ON_WRITE = false;

    public static final int DEFAULT_BLOCKSIZE = HConstants.DEFAULT_BLOCKSIZE;

    public static final String DEFAULT_BLOOMFILTER = BloomType.ROW.toString();

    public static final boolean DEFAULT_CACHE_BLOOMS_ON_WRITE = false;

    public static final int DEFAULT_TTL = HConstants.FOREVER;

    public static final int DEFAULT_REPLICATION_SCOPE = HConstants.REPLICATION_SCOPE_LOCAL;

    public static final boolean DEFAULT_EVICT_BLOCKS_ON_CLOSE = false;

    public static final boolean DEFAULT_COMPRESS_TAGS = true;

    public static final boolean DEFAULT_PREFETCH_BLOCKS_ON_OPEN = false;

    private final static Map<String, String> DEFAULT_VALUES = new HashMap<String, String>();

    private final static Set<ImmutableBytesWritable> RESERVED_KEYWORDS = new HashSet<ImmutableBytesWritable>();

    static {
        DEFAULT_VALUES.put(BLOOMFILTER, DEFAULT_BLOOMFILTER);
        DEFAULT_VALUES.put(REPLICATION_SCOPE, String.valueOf(DEFAULT_REPLICATION_SCOPE));
        DEFAULT_VALUES.put(HConstants.VERSIONS, String.valueOf(DEFAULT_VERSIONS));
        DEFAULT_VALUES.put(MIN_VERSIONS, String.valueOf(DEFAULT_MIN_VERSIONS));
        DEFAULT_VALUES.put(COMPRESSION, DEFAULT_COMPRESSION);
        DEFAULT_VALUES.put(TTL, String.valueOf(DEFAULT_TTL));
        DEFAULT_VALUES.put(BLOCKSIZE, String.valueOf(DEFAULT_BLOCKSIZE));
        DEFAULT_VALUES.put(HConstants.IN_MEMORY, String.valueOf(DEFAULT_IN_MEMORY));
        DEFAULT_VALUES.put(BLOCKCACHE, String.valueOf(DEFAULT_BLOCKCACHE));
        DEFAULT_VALUES.put(KEEP_DELETED_CELLS, String.valueOf(DEFAULT_KEEP_DELETED));
        DEFAULT_VALUES.put(DATA_BLOCK_ENCODING, String.valueOf(DEFAULT_DATA_BLOCK_ENCODING));
        DEFAULT_VALUES.put(CACHE_DATA_ON_WRITE, String.valueOf(DEFAULT_CACHE_DATA_ON_WRITE));
        DEFAULT_VALUES.put(CACHE_DATA_IN_L1, String.valueOf(DEFAULT_CACHE_DATA_IN_L1));
        DEFAULT_VALUES.put(CACHE_INDEX_ON_WRITE, String.valueOf(DEFAULT_CACHE_INDEX_ON_WRITE));
        DEFAULT_VALUES.put(CACHE_BLOOMS_ON_WRITE, String.valueOf(DEFAULT_CACHE_BLOOMS_ON_WRITE));
        DEFAULT_VALUES.put(EVICT_BLOCKS_ON_CLOSE, String.valueOf(DEFAULT_EVICT_BLOCKS_ON_CLOSE));
        DEFAULT_VALUES.put(PREFETCH_BLOCKS_ON_OPEN, String.valueOf(DEFAULT_PREFETCH_BLOCKS_ON_OPEN));
        for (String s : DEFAULT_VALUES.keySet()) {
            RESERVED_KEYWORDS.add(new ImmutableBytesWritable(Bytes.toBytes(s)));
        }
        RESERVED_KEYWORDS.add(new ImmutableBytesWritable(Bytes.toBytes(ENCRYPTION)));
        RESERVED_KEYWORDS.add(new ImmutableBytesWritable(Bytes.toBytes(ENCRYPTION_KEY)));
    }

    private static final int UNINITIALIZED = -1;

    private byte[] name;

    private final Map<ImmutableBytesWritable, ImmutableBytesWritable> values = new HashMap<ImmutableBytesWritable, ImmutableBytesWritable>();

    private final Map<String, String> configuration = new HashMap<String, String>();

    private int cachedMaxVersions = UNINITIALIZED;

    @Deprecated
    public HColumnDescriptor() {
        this.name = null;
    }

    public HColumnDescriptor(final String familyName) {
        this(Bytes.toBytes(familyName));
    }

    public HColumnDescriptor(final byte[] familyName) {
        this(familyName == null || familyName.length <= 0 ? HConstants.EMPTY_BYTE_ARRAY : familyName, DEFAULT_VERSIONS, DEFAULT_COMPRESSION, DEFAULT_IN_MEMORY, DEFAULT_BLOCKCACHE, DEFAULT_TTL, DEFAULT_BLOOMFILTER);
    }

    public HColumnDescriptor(HColumnDescriptor desc) {
        super();
        this.name = desc.name.clone();
        for (Map.Entry<ImmutableBytesWritable, ImmutableBytesWritable> e : desc.values.entrySet()) {
            this.values.put(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, String> e : desc.configuration.entrySet()) {
            this.configuration.put(e.getKey(), e.getValue());
        }
        setMaxVersions(desc.getMaxVersions());
    }

    @Deprecated
    public HColumnDescriptor(final byte[] familyName, final int maxVersions, final String compression, final boolean inMemory, final boolean blockCacheEnabled, final int timeToLive, final String bloomFilter) {
        this(familyName, maxVersions, compression, inMemory, blockCacheEnabled, DEFAULT_BLOCKSIZE, timeToLive, bloomFilter, DEFAULT_REPLICATION_SCOPE);
    }

    @Deprecated
    public HColumnDescriptor(final byte[] familyName, final int maxVersions, final String compression, final boolean inMemory, final boolean blockCacheEnabled, final int blocksize, final int timeToLive, final String bloomFilter, final int scope) {
        this(familyName, DEFAULT_MIN_VERSIONS, maxVersions, DEFAULT_KEEP_DELETED, compression, DEFAULT_ENCODE_ON_DISK, DEFAULT_DATA_BLOCK_ENCODING, inMemory, blockCacheEnabled, blocksize, timeToLive, bloomFilter, scope);
    }

    @Deprecated
    public HColumnDescriptor(final byte[] familyName, final int minVersions, final int maxVersions, final boolean keepDeletedCells, final String compression, final boolean encodeOnDisk, final String dataBlockEncoding, final boolean inMemory, final boolean blockCacheEnabled, final int blocksize, final int timeToLive, final String bloomFilter, final int scope) {
        isLegalFamilyName(familyName);
        this.name = familyName;
        if (maxVersions <= 0) {
            throw new IllegalArgumentException("Maximum versions must be positive");
        }
        if (minVersions > 0) {
            if (timeToLive == HConstants.FOREVER) {
                throw new IllegalArgumentException("Minimum versions requires TTL.");
            }
            if (minVersions >= maxVersions) {
                throw new IllegalArgumentException("Minimum versions must be < " + "maximum versions.");
            }
        }
        setMaxVersions(maxVersions);
        setMinVersions(minVersions);
        setKeepDeletedCells(keepDeletedCells);
        setInMemory(inMemory);
        setBlockCacheEnabled(blockCacheEnabled);
        setTimeToLive(timeToLive);
        setCompressionType(Compression.Algorithm.valueOf(compression.toUpperCase()));
        setDataBlockEncoding(DataBlockEncoding.valueOf(dataBlockEncoding.toUpperCase()));
        setBloomFilterType(BloomType.valueOf(bloomFilter.toUpperCase()));
        setBlocksize(blocksize);
        setScope(scope);
    }

    public static byte[] isLegalFamilyName(final byte[] b) {
        if (b == null) {
            return b;
        }
        Preconditions.checkArgument(b.length != 0, "Family name can not be empty");
        if (b[0] == '.') {
            throw new IllegalArgumentException("Family names cannot start with a " + "period: " + Bytes.toString(b));
        }
        for (int i = 0; i < b.length; i++) {
            if (Character.isISOControl(b[i]) || b[i] == ':' || b[i] == '\\' || b[i] == '/') {
                throw new IllegalArgumentException("Illegal character <" + b[i] + ">. Family names cannot contain control characters or colons: " + Bytes.toString(b));
            }
        }
        byte[] recoveredEdit = Bytes.toBytes(HConstants.RECOVERED_EDITS_DIR);
        if (Bytes.equals(recoveredEdit, b)) {
            throw new IllegalArgumentException("Family name cannot be: " + HConstants.RECOVERED_EDITS_DIR);
        }
        return b;
    }

    public byte[] getName() {
        return name;
    }

    public String getNameAsString() {
        return Bytes.toString(this.name);
    }

    public byte[] getValue(byte[] key) {
        ImmutableBytesWritable ibw = values.get(new ImmutableBytesWritable(key));
        if (ibw == null)
            return null;
        return ibw.get();
    }

    public String getValue(String key) {
        byte[] value = getValue(Bytes.toBytes(key));
        if (value == null)
            return null;
        return Bytes.toString(value);
    }

    public Map<ImmutableBytesWritable, ImmutableBytesWritable> getValues() {
        return Collections.unmodifiableMap(values);
    }

    public HColumnDescriptor setValue(byte[] key, byte[] value) {
        values.put(new ImmutableBytesWritable(key), new ImmutableBytesWritable(value));
        return this;
    }

    public void remove(final byte[] key) {
        values.remove(new ImmutableBytesWritable(key));
    }

    public HColumnDescriptor setValue(String key, String value) {
        if (value == null) {
            remove(Bytes.toBytes(key));
        } else {
            setValue(Bytes.toBytes(key), Bytes.toBytes(value));
        }
        return this;
    }

    public Compression.Algorithm getCompression() {
        String n = getValue(COMPRESSION);
        if (n == null) {
            return Compression.Algorithm.NONE;
        }
        return Compression.Algorithm.valueOf(n.toUpperCase());
    }

    public Compression.Algorithm getCompactionCompression() {
        String n = getValue(COMPRESSION_COMPACT);
        if (n == null) {
            return getCompression();
        }
        return Compression.Algorithm.valueOf(n.toUpperCase());
    }

    public int getMaxVersions() {
        if (this.cachedMaxVersions == UNINITIALIZED) {
            String v = getValue(HConstants.VERSIONS);
            this.cachedMaxVersions = Integer.parseInt(v);
        }
        return this.cachedMaxVersions;
    }

    public HColumnDescriptor setMaxVersions(int maxVersions) {
        if (maxVersions <= 0) {
            throw new IllegalArgumentException("Maximum versions must be positive");
        }
        if (maxVersions < this.getMinVersions()) {
            throw new IllegalArgumentException("Set MaxVersion to " + maxVersions + " while minVersion is " + this.getMinVersions() + ". Maximum versions must be >= minimum versions ");
        }
        setValue(HConstants.VERSIONS, Integer.toString(maxVersions));
        cachedMaxVersions = maxVersions;
        return this;
    }

    public synchronized int getBlocksize() {
        if (this.blocksize == null) {
            String value = getValue(BLOCKSIZE);
            this.blocksize = (value != null) ? Integer.decode(value) : Integer.valueOf(DEFAULT_BLOCKSIZE);
        }
        return this.blocksize.intValue();
    }

    public HColumnDescriptor setBlocksize(int s) {
        setValue(BLOCKSIZE, Integer.toString(s));
        this.blocksize = null;
        return this;
    }

    public Compression.Algorithm getCompressionType() {
        return getCompression();
    }

    public HColumnDescriptor setCompressionType(Compression.Algorithm type) {
        return setValue(COMPRESSION, type.getName().toUpperCase());
    }

    @Deprecated
    public DataBlockEncoding getDataBlockEncodingOnDisk() {
        return getDataBlockEncoding();
    }

    @Deprecated
    public HColumnDescriptor setEncodeOnDisk(boolean encodeOnDisk) {
        return this;
    }

    public DataBlockEncoding getDataBlockEncoding() {
        String type = getValue(DATA_BLOCK_ENCODING);
        if (type == null) {
            type = DEFAULT_DATA_BLOCK_ENCODING;
        }
        return DataBlockEncoding.valueOf(type);
    }

    public HColumnDescriptor setDataBlockEncoding(DataBlockEncoding type) {
        String name;
        if (type != null) {
            name = type.toString();
        } else {
            name = DataBlockEncoding.NONE.toString();
        }
        return setValue(DATA_BLOCK_ENCODING, name);
    }

    public HColumnDescriptor setCompressTags(boolean compressTags) {
        return setValue(COMPRESS_TAGS, String.valueOf(compressTags));
    }

    public boolean shouldCompressTags() {
        String compressTagsStr = getValue(COMPRESS_TAGS);
        boolean compressTags = DEFAULT_COMPRESS_TAGS;
        if (compressTagsStr != null) {
            compressTags = Boolean.valueOf(compressTagsStr);
        }
        return compressTags;
    }

    public Compression.Algorithm getCompactionCompressionType() {
        return getCompactionCompression();
    }

    public HColumnDescriptor setCompactionCompressionType(Compression.Algorithm type) {
        return setValue(COMPRESSION_COMPACT, type.getName().toUpperCase());
    }

    public boolean isInMemory() {
        String value = getValue(HConstants.IN_MEMORY);
        if (value != null)
            return Boolean.valueOf(value).booleanValue();
        return DEFAULT_IN_MEMORY;
    }

    public HColumnDescriptor setInMemory(boolean inMemory) {
        return setValue(HConstants.IN_MEMORY, Boolean.toString(inMemory));
    }

    public boolean getKeepDeletedCells() {
        String value = getValue(KEEP_DELETED_CELLS);
        if (value != null) {
            return Boolean.valueOf(value).booleanValue();
        }
        return DEFAULT_KEEP_DELETED;
    }

    public HColumnDescriptor setKeepDeletedCells(boolean keepDeletedCells) {
        return setValue(KEEP_DELETED_CELLS, Boolean.toString(keepDeletedCells));
    }

    public int getTimeToLive() {
        String value = getValue(TTL);
        return (value != null) ? Integer.valueOf(value).intValue() : DEFAULT_TTL;
    }

    public HColumnDescriptor setTimeToLive(int timeToLive) {
        return setValue(TTL, Integer.toString(timeToLive));
    }

    public int getMinVersions() {
        String value = getValue(MIN_VERSIONS);
        return (value != null) ? Integer.valueOf(value).intValue() : 0;
    }

    public HColumnDescriptor setMinVersions(int minVersions) {
        return setValue(MIN_VERSIONS, Integer.toString(minVersions));
    }

    public boolean isBlockCacheEnabled() {
        String value = getValue(BLOCKCACHE);
        if (value != null)
            return Boolean.valueOf(value).booleanValue();
        return DEFAULT_BLOCKCACHE;
    }

    public HColumnDescriptor setBlockCacheEnabled(boolean blockCacheEnabled) {
        return setValue(BLOCKCACHE, Boolean.toString(blockCacheEnabled));
    }

    public BloomType getBloomFilterType() {
        String n = getValue(BLOOMFILTER);
        if (n == null) {
            n = DEFAULT_BLOOMFILTER;
        }
        return BloomType.valueOf(n.toUpperCase());
    }

    public HColumnDescriptor setBloomFilterType(final BloomType bt) {
        return setValue(BLOOMFILTER, bt.toString());
    }

    public int getScope() {
        byte[] value = getValue(REPLICATION_SCOPE_BYTES);
        if (value != null) {
            return Integer.valueOf(Bytes.toString(value));
        }
        return DEFAULT_REPLICATION_SCOPE;
    }

    public HColumnDescriptor setScope(int scope) {
        return setValue(REPLICATION_SCOPE, Integer.toString(scope));
    }

    public boolean shouldCacheDataOnWrite() {
        return setAndGetBoolean(CACHE_DATA_ON_WRITE, DEFAULT_CACHE_DATA_ON_WRITE);
    }

    public HColumnDescriptor setCacheDataOnWrite(boolean value) {
        return setValue(CACHE_DATA_ON_WRITE, Boolean.toString(value));
    }

    public boolean shouldCacheDataInL1() {
        return setAndGetBoolean(CACHE_DATA_IN_L1, DEFAULT_CACHE_DATA_IN_L1);
    }

    public HColumnDescriptor setCacheDataInL1(boolean value) {
        return setValue(CACHE_DATA_IN_L1, Boolean.toString(value));
    }

    private boolean setAndGetBoolean(final String key, final boolean defaultSetting) {
        String value = getValue(key);
        if (value != null)
            return Boolean.valueOf(value).booleanValue();
        return defaultSetting;
    }

    public boolean shouldCacheIndexesOnWrite() {
        return setAndGetBoolean(CACHE_INDEX_ON_WRITE, DEFAULT_CACHE_INDEX_ON_WRITE);
    }

    public HColumnDescriptor setCacheIndexesOnWrite(boolean value) {
        return setValue(CACHE_INDEX_ON_WRITE, Boolean.toString(value));
    }

    public boolean shouldCacheBloomsOnWrite() {
        return setAndGetBoolean(CACHE_BLOOMS_ON_WRITE, DEFAULT_CACHE_BLOOMS_ON_WRITE);
    }

    public HColumnDescriptor setCacheBloomsOnWrite(boolean value) {
        return setValue(CACHE_BLOOMS_ON_WRITE, Boolean.toString(value));
    }

    public boolean shouldEvictBlocksOnClose() {
        return setAndGetBoolean(EVICT_BLOCKS_ON_CLOSE, DEFAULT_EVICT_BLOCKS_ON_CLOSE);
    }

    public HColumnDescriptor setEvictBlocksOnClose(boolean value) {
        return setValue(EVICT_BLOCKS_ON_CLOSE, Boolean.toString(value));
    }

    public boolean shouldPrefetchBlocksOnOpen() {
        return setAndGetBoolean(PREFETCH_BLOCKS_ON_OPEN, DEFAULT_PREFETCH_BLOCKS_ON_OPEN);
    }

    public HColumnDescriptor setPrefetchBlocksOnOpen(boolean value) {
        return setValue(PREFETCH_BLOCKS_ON_OPEN, Boolean.toString(value));
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append('{');
        s.append(HConstants.NAME);
        s.append(" => '");
        s.append(Bytes.toString(name));
        s.append("'");
        s.append(getValues(true));
        s.append('}');
        return s.toString();
    }

    public String toStringCustomizedValues() {
        StringBuilder s = new StringBuilder();
        s.append('{');
        s.append(HConstants.NAME);
        s.append(" => '");
        s.append(Bytes.toString(name));
        s.append("'");
        s.append(getValues(false));
        s.append('}');
        return s.toString();
    }

    private StringBuilder getValues(boolean printDefaults) {
        StringBuilder s = new StringBuilder();
        boolean hasConfigKeys = false;
        for (ImmutableBytesWritable k : values.keySet()) {
            if (!RESERVED_KEYWORDS.contains(k)) {
                hasConfigKeys = true;
                continue;
            }
            String key = Bytes.toString(k.get());
            String value = Bytes.toStringBinary(values.get(k).get());
            if (printDefaults || !DEFAULT_VALUES.containsKey(key) || !DEFAULT_VALUES.get(key).equalsIgnoreCase(value)) {
                s.append(", ");
                s.append(key);
                s.append(" => ");
                s.append('\'').append(PrettyPrinter.format(value, getUnit(key))).append('\'');
            }
        }
        if (hasConfigKeys) {
            s.append(", ");
            s.append(HConstants.METADATA).append(" => ");
            s.append('{');
            boolean printComma = false;
            for (ImmutableBytesWritable k : values.keySet()) {
                if (RESERVED_KEYWORDS.contains(k)) {
                    continue;
                }
                String key = Bytes.toString(k.get());
                String value = Bytes.toStringBinary(values.get(k).get());
                if (printComma) {
                    s.append(", ");
                }
                printComma = true;
                s.append('\'').append(key).append('\'');
                s.append(" => ");
                s.append('\'').append(PrettyPrinter.format(value, getUnit(key))).append('\'');
            }
            s.append('}');
        }
        if (!configuration.isEmpty()) {
            s.append(", ");
            s.append(HConstants.CONFIGURATION).append(" => ");
            s.append('{');
            boolean printCommaForConfiguration = false;
            for (Map.Entry<String, String> e : configuration.entrySet()) {
                if (printCommaForConfiguration)
                    s.append(", ");
                printCommaForConfiguration = true;
                s.append('\'').append(e.getKey()).append('\'');
                s.append(" => ");
                s.append('\'').append(PrettyPrinter.format(e.getValue(), getUnit(e.getKey()))).append('\'');
            }
            s.append("}");
        }
        return s;
    }

    public static Unit getUnit(String key) {
        Unit unit;
        if (key.equals(HColumnDescriptor.TTL)) {
            unit = Unit.TIME_INTERVAL;
        } else {
            unit = Unit.NONE;
        }
        return unit;
    }

    public static Map<String, String> getDefaultValues() {
        return Collections.unmodifiableMap(DEFAULT_VALUES);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof HColumnDescriptor)) {
            return false;
        }
        return compareTo((HColumnDescriptor) obj) == 0;
    }

    @Override
    public int hashCode() {
        int result = Bytes.hashCode(this.name);
        result ^= Byte.valueOf(COLUMN_DESCRIPTOR_VERSION).hashCode();
        result ^= values.hashCode();
        result ^= configuration.hashCode();
        return result;
    }

    @Deprecated
    public void readFields(DataInput in) throws IOException {
        int version = in.readByte();
        if (version < 6) {
            if (version <= 2) {
                Text t = new Text();
                t.readFields(in);
                this.name = t.getBytes();
            } else {
                this.name = Bytes.readByteArray(in);
            }
            this.values.clear();
            setMaxVersions(in.readInt());
            int ordinal = in.readInt();
            setCompressionType(Compression.Algorithm.values()[ordinal]);
            setInMemory(in.readBoolean());
            setBloomFilterType(in.readBoolean() ? BloomType.ROW : BloomType.NONE);
            if (getBloomFilterType() != BloomType.NONE && version < 5) {
                throw new UnsupportedClassVersionError(this.getClass().getName() + " does not support backward compatibility with versions older " + "than version 5");
            }
            if (version > 1) {
                setBlockCacheEnabled(in.readBoolean());
            }
            if (version > 2) {
                setTimeToLive(in.readInt());
            }
        } else {
            this.name = Bytes.readByteArray(in);
            this.values.clear();
            int numValues = in.readInt();
            for (int i = 0; i < numValues; i++) {
                ImmutableBytesWritable key = new ImmutableBytesWritable();
                ImmutableBytesWritable value = new ImmutableBytesWritable();
                key.readFields(in);
                value.readFields(in);
                if (version < 8 && Bytes.toString(key.get()).equals(BLOOMFILTER)) {
                    value.set(Bytes.toBytes(Boolean.getBoolean(Bytes.toString(value.get())) ? BloomType.ROW.toString() : BloomType.NONE.toString()));
                }
                values.put(key, value);
            }
            if (version == 6) {
                setValue(COMPRESSION, Compression.Algorithm.NONE.getName());
            }
            String value = getValue(HConstants.VERSIONS);
            this.cachedMaxVersions = (value != null) ? Integer.valueOf(value).intValue() : DEFAULT_VERSIONS;
            if (version > 10) {
                configuration.clear();
                int numConfigs = in.readInt();
                for (int i = 0; i < numConfigs; i++) {
                    ImmutableBytesWritable key = new ImmutableBytesWritable();
                    ImmutableBytesWritable val = new ImmutableBytesWritable();
                    key.readFields(in);
                    val.readFields(in);
                    configuration.put(Bytes.toString(key.get(), key.getOffset(), key.getLength()), Bytes.toString(val.get(), val.getOffset(), val.getLength()));
                }
            }
        }
    }

    @Deprecated
    public void write(DataOutput out) throws IOException {
        out.writeByte(COLUMN_DESCRIPTOR_VERSION);
        Bytes.writeByteArray(out, this.name);
        out.writeInt(values.size());
        for (Map.Entry<ImmutableBytesWritable, ImmutableBytesWritable> e : values.entrySet()) {
            e.getKey().write(out);
            e.getValue().write(out);
        }
        out.writeInt(configuration.size());
        for (Map.Entry<String, String> e : configuration.entrySet()) {
            new ImmutableBytesWritable(Bytes.toBytes(e.getKey())).write(out);
            new ImmutableBytesWritable(Bytes.toBytes(e.getValue())).write(out);
        }
    }

    public int compareTo(HColumnDescriptor o) {
        int result = Bytes.compareTo(this.name, o.getName());
        if (result == 0) {
            result = this.values.hashCode() - o.values.hashCode();
            if (result < 0)
                result = -1;
            else if (result > 0)
                result = 1;
        }
        if (result == 0) {
            result = this.configuration.hashCode() - o.configuration.hashCode();
            if (result < 0)
                result = -1;
            else if (result > 0)
                result = 1;
        }
        return result;
    }

    public byte[] toByteArray() {
        return ProtobufUtil.prependPBMagic(convert().toByteArray());
    }

    public static HColumnDescriptor parseFrom(final byte[] bytes) throws DeserializationException {
        if (!ProtobufUtil.isPBMagicPrefix(bytes))
            throw new DeserializationException("No magic");
        int pblen = ProtobufUtil.lengthOfPBMagic();
        ColumnFamilySchema.Builder builder = ColumnFamilySchema.newBuilder();
        ColumnFamilySchema cfs = null;
        try {
            cfs = builder.mergeFrom(bytes, pblen, bytes.length - pblen).build();
        } catch (InvalidProtocolBufferException e) {
            throw new DeserializationException(e);
        }
        return convert(cfs);
    }

    public static HColumnDescriptor convert(final ColumnFamilySchema cfs) {
        HColumnDescriptor hcd = new HColumnDescriptor();
        hcd.name = cfs.getName().toByteArray();
        for (BytesBytesPair a : cfs.getAttributesList()) {
            hcd.setValue(a.getFirst().toByteArray(), a.getSecond().toByteArray());
        }
        for (NameStringPair a : cfs.getConfigurationList()) {
            hcd.setConfiguration(a.getName(), a.getValue());
        }
        return hcd;
    }

    public ColumnFamilySchema convert() {
        ColumnFamilySchema.Builder builder = ColumnFamilySchema.newBuilder();
        builder.setName(ByteStringer.wrap(getName()));
        for (Map.Entry<ImmutableBytesWritable, ImmutableBytesWritable> e : this.values.entrySet()) {
            BytesBytesPair.Builder aBuilder = BytesBytesPair.newBuilder();
            aBuilder.setFirst(ByteStringer.wrap(e.getKey().get()));
            aBuilder.setSecond(ByteStringer.wrap(e.getValue().get()));
            builder.addAttributes(aBuilder.build());
        }
        for (Map.Entry<String, String> e : this.configuration.entrySet()) {
            NameStringPair.Builder aBuilder = NameStringPair.newBuilder();
            aBuilder.setName(e.getKey());
            aBuilder.setValue(e.getValue());
            builder.addConfiguration(aBuilder.build());
        }
        return builder.build();
    }

    public String getConfigurationValue(String key) {
        return configuration.get(key);
    }

    public Map<String, String> getConfiguration() {
        return Collections.unmodifiableMap(configuration);
    }

    public void setConfiguration(String key, String value) {
        if (value == null) {
            removeConfiguration(key);
        } else {
            configuration.put(key, value);
        }
    }

    public void removeConfiguration(final String key) {
        configuration.remove(key);
    }

    public String getEncryptionType() {
        return getValue(ENCRYPTION);
    }

    public HColumnDescriptor setEncryptionType(String algorithm) {
        setValue(ENCRYPTION, algorithm);
        return this;
    }

    public byte[] getEncryptionKey() {
        return getValue(Bytes.toBytes(ENCRYPTION_KEY));
    }

    public HColumnDescriptor setEncryptionKey(byte[] keyBytes) {
        setValue(Bytes.toBytes(ENCRYPTION_KEY), keyBytes);
        return this;
    }
}
