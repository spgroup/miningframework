package org.apache.cassandra.thrift;

import org.apache.commons.lang.builder.HashCodeBuilder;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CfDef implements org.apache.thrift.TBase<CfDef, CfDef._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("CfDef");

    private static final org.apache.thrift.protocol.TField KEYSPACE_FIELD_DESC = new org.apache.thrift.protocol.TField("keyspace", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final org.apache.thrift.protocol.TField NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("name", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.protocol.TField COLUMN_TYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("column_type", org.apache.thrift.protocol.TType.STRING, (short) 3);

    private static final org.apache.thrift.protocol.TField COMPARATOR_TYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("comparator_type", org.apache.thrift.protocol.TType.STRING, (short) 5);

    private static final org.apache.thrift.protocol.TField SUBCOMPARATOR_TYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("subcomparator_type", org.apache.thrift.protocol.TType.STRING, (short) 6);

    private static final org.apache.thrift.protocol.TField COMMENT_FIELD_DESC = new org.apache.thrift.protocol.TField("comment", org.apache.thrift.protocol.TType.STRING, (short) 8);

    private static final org.apache.thrift.protocol.TField ROW_CACHE_SIZE_FIELD_DESC = new org.apache.thrift.protocol.TField("row_cache_size", org.apache.thrift.protocol.TType.DOUBLE, (short) 9);

    private static final org.apache.thrift.protocol.TField KEY_CACHE_SIZE_FIELD_DESC = new org.apache.thrift.protocol.TField("key_cache_size", org.apache.thrift.protocol.TType.DOUBLE, (short) 11);

    private static final org.apache.thrift.protocol.TField READ_REPAIR_CHANCE_FIELD_DESC = new org.apache.thrift.protocol.TField("read_repair_chance", org.apache.thrift.protocol.TType.DOUBLE, (short) 12);

    private static final org.apache.thrift.protocol.TField COLUMN_METADATA_FIELD_DESC = new org.apache.thrift.protocol.TField("column_metadata", org.apache.thrift.protocol.TType.LIST, (short) 13);

    private static final org.apache.thrift.protocol.TField GC_GRACE_SECONDS_FIELD_DESC = new org.apache.thrift.protocol.TField("gc_grace_seconds", org.apache.thrift.protocol.TType.I32, (short) 14);

    private static final org.apache.thrift.protocol.TField DEFAULT_VALIDATION_CLASS_FIELD_DESC = new org.apache.thrift.protocol.TField("default_validation_class", org.apache.thrift.protocol.TType.STRING, (short) 15);

    private static final org.apache.thrift.protocol.TField ID_FIELD_DESC = new org.apache.thrift.protocol.TField("id", org.apache.thrift.protocol.TType.I32, (short) 16);

    private static final org.apache.thrift.protocol.TField MIN_COMPACTION_THRESHOLD_FIELD_DESC = new org.apache.thrift.protocol.TField("min_compaction_threshold", org.apache.thrift.protocol.TType.I32, (short) 17);

    private static final org.apache.thrift.protocol.TField MAX_COMPACTION_THRESHOLD_FIELD_DESC = new org.apache.thrift.protocol.TField("max_compaction_threshold", org.apache.thrift.protocol.TType.I32, (short) 18);

    private static final org.apache.thrift.protocol.TField ROW_CACHE_SAVE_PERIOD_IN_SECONDS_FIELD_DESC = new org.apache.thrift.protocol.TField("row_cache_save_period_in_seconds", org.apache.thrift.protocol.TType.I32, (short) 19);

    private static final org.apache.thrift.protocol.TField KEY_CACHE_SAVE_PERIOD_IN_SECONDS_FIELD_DESC = new org.apache.thrift.protocol.TField("key_cache_save_period_in_seconds", org.apache.thrift.protocol.TType.I32, (short) 20);<<<<<<< MINE
=======
private static final org.apache.thrift.protocol.TField MEMTABLE_FLUSH_AFTER_MINS_FIELD_DESC = new org.apache.thrift.protocol.TField("memtable_flush_after_mins", org.apache.thrift.protocol.TType.I32, (short) 21);
>>>>>>> YOURS
<<<<<<< MINE
=======
private static final org.apache.thrift.protocol.TField MEMTABLE_THROUGHPUT_IN_MB_FIELD_DESC = new org.apache.thrift.protocol.TField("memtable_throughput_in_mb", org.apache.thrift.protocol.TType.I32, (short) 22);
>>>>>>> YOURS
<<<<<<< MINE
=======
private static final org.apache.thrift.protocol.TField MEMTABLE_OPERATIONS_IN_MILLIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("memtable_operations_in_millions", org.apache.thrift.protocol.TType.DOUBLE, (short) 23);
>>>>>>> YOURS


    private static final org.apache.thrift.protocol.TField REPLICATE_ON_WRITE_FIELD_DESC = new org.apache.thrift.protocol.TField("replicate_on_write", org.apache.thrift.protocol.TType.BOOL, (short) 24);

    private static final org.apache.thrift.protocol.TField MERGE_SHARDS_CHANCE_FIELD_DESC = new org.apache.thrift.protocol.TField("merge_shards_chance", org.apache.thrift.protocol.TType.DOUBLE, (short) 25);

    private static final org.apache.thrift.protocol.TField KEY_VALIDATION_CLASS_FIELD_DESC = new org.apache.thrift.protocol.TField("key_validation_class", org.apache.thrift.protocol.TType.STRING, (short) 26);

    private static final org.apache.thrift.protocol.TField ROW_CACHE_PROVIDER_FIELD_DESC = new org.apache.thrift.protocol.TField("row_cache_provider", org.apache.thrift.protocol.TType.STRING, (short) 27);

    private static final org.apache.thrift.protocol.TField KEY_ALIAS_FIELD_DESC = new org.apache.thrift.protocol.TField("key_alias", org.apache.thrift.protocol.TType.STRING, (short) 28);

    private static final org.apache.thrift.protocol.TField COMPACTION_STRATEGY_FIELD_DESC = new org.apache.thrift.protocol.TField("compaction_strategy", org.apache.thrift.protocol.TType.STRING, (short) 29);

    private static final org.apache.thrift.protocol.TField COMPACTION_STRATEGY_OPTIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("compaction_strategy_options", org.apache.thrift.protocol.TType.MAP, (short) 30);

    private static final org.apache.thrift.protocol.TField ROW_CACHE_KEYS_TO_SAVE_FIELD_DESC = new org.apache.thrift.protocol.TField("row_cache_keys_to_save", org.apache.thrift.protocol.TType.I32, (short) 31);

    private static final org.apache.thrift.protocol.TField COMPRESSION_OPTIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("compression_options", org.apache.thrift.protocol.TType.MAP, (short) 32);

    private static final org.apache.thrift.protocol.TField BLOOM_FILTER_FP_CHANCE_FIELD_DESC = new org.apache.thrift.protocol.TField("bloom_filter_fp_chance", org.apache.thrift.protocol.TType.DOUBLE, (short) 33);

    public String keyspace;

    public String name;

    public String column_type;

    public String comparator_type;

    public String subcomparator_type;

    public String comment;

    public double row_cache_size;

    public double key_cache_size;

    public double read_repair_chance;

    public List<ColumnDef> column_metadata;

    public int gc_grace_seconds;

    public String default_validation_class;

    public int id;

    public int min_compaction_threshold;

    public int max_compaction_threshold;

    public int row_cache_save_period_in_seconds;

    public int key_cache_save_period_in_seconds;

    public boolean replicate_on_write;

    public double merge_shards_chance;

    public String key_validation_class;

    public String row_cache_provider;

    public ByteBuffer key_alias;

    public String compaction_strategy;

    public Map<String, String> compaction_strategy_options;

    public int row_cache_keys_to_save;

    public Map<String, String> compression_options;

    public double bloom_filter_fp_chance;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        KEYSPACE((short) 1, "keyspace"),
        NAME((short) 2, "name"),
        COLUMN_TYPE((short) 3, "column_type"),
        COMPARATOR_TYPE((short) 5, "comparator_type"),
        SUBCOMPARATOR_TYPE((short) 6, "subcomparator_type"),
        COMMENT((short) 8, "comment"),
        ROW_CACHE_SIZE((short) 9, "row_cache_size"),
        KEY_CACHE_SIZE((short) 11, "key_cache_size"),
        READ_REPAIR_CHANCE((short) 12, "read_repair_chance"),
        COLUMN_METADATA((short) 13, "column_metadata"),
        GC_GRACE_SECONDS((short) 14, "gc_grace_seconds"),
        DEFAULT_VALIDATION_CLASS((short) 15, "default_validation_class"),
        ID((short) 16, "id"),
        MIN_COMPACTION_THRESHOLD((short) 17, "min_compaction_threshold"),
        MAX_COMPACTION_THRESHOLD((short) 18, "max_compaction_threshold"),
        ROW_CACHE_SAVE_PERIOD_IN_SECONDS((short) 19, "row_cache_save_period_in_seconds"),
        KEY_CACHE_SAVE_PERIOD_IN_SECONDS((short) 20, "key_cache_save_period_in_seconds"),,,,
        REPLICATE_ON_WRITE((short) 24, "replicate_on_write"),
        MERGE_SHARDS_CHANCE((short) 25, "merge_shards_chance"),
        KEY_VALIDATION_CLASS((short) 26, "key_validation_class"),
        ROW_CACHE_PROVIDER((short) 27, "row_cache_provider"),
        KEY_ALIAS((short) 28, "key_alias"),
        COMPACTION_STRATEGY((short) 29, "compaction_strategy"),
        COMPACTION_STRATEGY_OPTIONS((short) 30, "compaction_strategy_options"),
        ROW_CACHE_KEYS_TO_SAVE((short) 31, "row_cache_keys_to_save"),
        COMPRESSION_OPTIONS((short) 32, "compression_options"),
        BLOOM_FILTER_FP_CHANCE((short) 33, "bloom_filter_fp_chance");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return KEYSPACE;
                case 2:
                    return NAME;
                case 3:
                    return COLUMN_TYPE;
                case 5:
                    return COMPARATOR_TYPE;
                case 6:
                    return SUBCOMPARATOR_TYPE;
                case 8:
                    return COMMENT;
                case 9:
                    return ROW_CACHE_SIZE;
                case 11:
                    return KEY_CACHE_SIZE;
                case 12:
                    return READ_REPAIR_CHANCE;
                case 13:
                    return COLUMN_METADATA;
                case 14:
                    return GC_GRACE_SECONDS;
                case 15:
                    return DEFAULT_VALIDATION_CLASS;
                case 16:
                    return ID;
                case 17:
                    return MIN_COMPACTION_THRESHOLD;
                case 18:
                    return MAX_COMPACTION_THRESHOLD;
                case 19:
                    return ROW_CACHE_SAVE_PERIOD_IN_SECONDS;
                case 20:
                    return KEY_CACHE_SAVE_PERIOD_IN_SECONDS;
<<<<<<< MINE
                case 24:
                    return REPLICATE_ON_WRITE;
                case 25:
                    return MERGE_SHARDS_CHANCE;
                case 26:
                    return KEY_VALIDATION_CLASS;
                case 27:
                    return ROW_CACHE_PROVIDER;
                case 28:
                    return KEY_ALIAS;
                case 29:
                    return COMPACTION_STRATEGY;
                case 30:
                    return COMPACTION_STRATEGY_OPTIONS;
                case 31:
                    return ROW_CACHE_KEYS_TO_SAVE;
                case 32:
                    return COMPRESSION_OPTIONS;
                case 33:
                    return BLOOM_FILTER_FP_CHANCE;
=======
                case 21:
                    return MEMTABLE_FLUSH_AFTER_MINS;
                case 22:
                    return MEMTABLE_THROUGHPUT_IN_MB;
                case 23:
                    return MEMTABLE_OPERATIONS_IN_MILLIONS;
                case 24:
                    return REPLICATE_ON_WRITE;
                case 25:
                    return MERGE_SHARDS_CHANCE;
                case 26:
                    return KEY_VALIDATION_CLASS;
                case 27:
                    return ROW_CACHE_PROVIDER;
                case 28:
                    return KEY_ALIAS;
>>>>>>> YOURS
                default:
                    return null;
            }
        }

        public static _Fields findByThriftIdOrThrow(int fieldId) {
            _Fields fields = findByThriftId(fieldId);
            if (fields == null)
                throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
            return fields;
        }

        public static _Fields findByName(String name) {
            return byName.get(name);
        }

        private final short _thriftId;

        private final String _fieldName;

        _Fields(short thriftId, String fieldName) {
            _thriftId = thriftId;
            _fieldName = fieldName;
        }

        public short getThriftFieldId() {
            return _thriftId;
        }

        public String getFieldName() {
            return _fieldName;
        }
    }

    private static final int __ROW_CACHE_SIZE_ISSET_ID = 0;

    private static final int __KEY_CACHE_SIZE_ISSET_ID = 1;

    private static final int __READ_REPAIR_CHANCE_ISSET_ID = 2;

    private static final int __GC_GRACE_SECONDS_ISSET_ID = 3;

    private static final int __ID_ISSET_ID = 4;

    private static final int __MIN_COMPACTION_THRESHOLD_ISSET_ID = 5;

    private static final int __MAX_COMPACTION_THRESHOLD_ISSET_ID = 6;

    private static final int __ROW_CACHE_SAVE_PERIOD_IN_SECONDS_ISSET_ID = 7;

    private static final int __KEY_CACHE_SAVE_PERIOD_IN_SECONDS_ISSET_ID = 8;

<<<<<<< MINE
private static final int __REPLICATE_ON_WRITE_ISSET_ID = 9;
=======
private static final int __REPLICATE_ON_WRITE_ISSET_ID = 12;
>>>>>>> YOURS


<<<<<<< MINE
private static final int __MERGE_SHARDS_CHANCE_ISSET_ID = 10;
=======
private static final int __MERGE_SHARDS_CHANCE_ISSET_ID = 13;
>>>>>>> YOURS


    private static final int __ROW_CACHE_KEYS_TO_SAVE_ISSET_ID = 11;

    private static final int __BLOOM_FILTER_FP_CHANCE_ISSET_ID = 12;

<<<<<<< MINE
private BitSet __isset_bit_vector = new BitSet(13);
=======
private BitSet __isset_bit_vector = new BitSet(14);
>>>>>>> YOURS


    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.KEYSPACE, new org.apache.thrift.meta_data.FieldMetaData("keyspace", org.apache.thrift.TFieldRequirementType.REQUIRED, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.NAME, new org.apache.thrift.meta_data.FieldMetaData("name", org.apache.thrift.TFieldRequirementType.REQUIRED, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.COLUMN_TYPE, new org.apache.thrift.meta_data.FieldMetaData("column_type", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.COMPARATOR_TYPE, new org.apache.thrift.meta_data.FieldMetaData("comparator_type", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.SUBCOMPARATOR_TYPE, new org.apache.thrift.meta_data.FieldMetaData("subcomparator_type", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.COMMENT, new org.apache.thrift.meta_data.FieldMetaData("comment", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.ROW_CACHE_SIZE, new org.apache.thrift.meta_data.FieldMetaData("row_cache_size", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.KEY_CACHE_SIZE, new org.apache.thrift.meta_data.FieldMetaData("key_cache_size", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.READ_REPAIR_CHANCE, new org.apache.thrift.meta_data.FieldMetaData("read_repair_chance", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.COLUMN_METADATA, new org.apache.thrift.meta_data.FieldMetaData("column_metadata", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, ColumnDef.class))));
        tmpMap.put(_Fields.GC_GRACE_SECONDS, new org.apache.thrift.meta_data.FieldMetaData("gc_grace_seconds", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.DEFAULT_VALIDATION_CLASS, new org.apache.thrift.meta_data.FieldMetaData("default_validation_class", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.ID, new org.apache.thrift.meta_data.FieldMetaData("id", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.MIN_COMPACTION_THRESHOLD, new org.apache.thrift.meta_data.FieldMetaData("min_compaction_threshold", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.MAX_COMPACTION_THRESHOLD, new org.apache.thrift.meta_data.FieldMetaData("max_compaction_threshold", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.ROW_CACHE_SAVE_PERIOD_IN_SECONDS, new org.apache.thrift.meta_data.FieldMetaData("row_cache_save_period_in_seconds", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.KEY_CACHE_SAVE_PERIOD_IN_SECONDS, new org.apache.thrift.meta_data.FieldMetaData("key_cache_save_period_in_seconds", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.REPLICATE_ON_WRITE, new org.apache.thrift.meta_data.FieldMetaData("replicate_on_write", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
        tmpMap.put(_Fields.MERGE_SHARDS_CHANCE, new org.apache.thrift.meta_data.FieldMetaData("merge_shards_chance", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.KEY_VALIDATION_CLASS, new org.apache.thrift.meta_data.FieldMetaData("key_validation_class", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.ROW_CACHE_PROVIDER, new org.apache.thrift.meta_data.FieldMetaData("row_cache_provider", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.KEY_ALIAS, new org.apache.thrift.meta_data.FieldMetaData("key_alias", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.COMPACTION_STRATEGY, new org.apache.thrift.meta_data.FieldMetaData("compaction_strategy", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.COMPACTION_STRATEGY_OPTIONS, new org.apache.thrift.meta_data.FieldMetaData("compaction_strategy_options", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
        tmpMap.put(_Fields.ROW_CACHE_KEYS_TO_SAVE, new org.apache.thrift.meta_data.FieldMetaData("row_cache_keys_to_save", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.COMPRESSION_OPTIONS, new org.apache.thrift.meta_data.FieldMetaData("compression_options", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
        tmpMap.put(_Fields.BLOOM_FILTER_FP_CHANCE, new org.apache.thrift.meta_data.FieldMetaData("bloom_filter_fp_chance", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(CfDef.class, metaDataMap);
    }

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.KEYSPACE, new org.apache.thrift.meta_data.FieldMetaData("keyspace", org.apache.thrift.TFieldRequirementType.REQUIRED, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.NAME, new org.apache.thrift.meta_data.FieldMetaData("name", org.apache.thrift.TFieldRequirementType.REQUIRED, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.COLUMN_TYPE, new org.apache.thrift.meta_data.FieldMetaData("column_type", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.COMPARATOR_TYPE, new org.apache.thrift.meta_data.FieldMetaData("comparator_type", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.SUBCOMPARATOR_TYPE, new org.apache.thrift.meta_data.FieldMetaData("subcomparator_type", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.COMMENT, new org.apache.thrift.meta_data.FieldMetaData("comment", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.ROW_CACHE_SIZE, new org.apache.thrift.meta_data.FieldMetaData("row_cache_size", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.KEY_CACHE_SIZE, new org.apache.thrift.meta_data.FieldMetaData("key_cache_size", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.READ_REPAIR_CHANCE, new org.apache.thrift.meta_data.FieldMetaData("read_repair_chance", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.COLUMN_METADATA, new org.apache.thrift.meta_data.FieldMetaData("column_metadata", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, ColumnDef.class))));
        tmpMap.put(_Fields.GC_GRACE_SECONDS, new org.apache.thrift.meta_data.FieldMetaData("gc_grace_seconds", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.DEFAULT_VALIDATION_CLASS, new org.apache.thrift.meta_data.FieldMetaData("default_validation_class", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.ID, new org.apache.thrift.meta_data.FieldMetaData("id", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.MIN_COMPACTION_THRESHOLD, new org.apache.thrift.meta_data.FieldMetaData("min_compaction_threshold", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.MAX_COMPACTION_THRESHOLD, new org.apache.thrift.meta_data.FieldMetaData("max_compaction_threshold", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.ROW_CACHE_SAVE_PERIOD_IN_SECONDS, new org.apache.thrift.meta_data.FieldMetaData("row_cache_save_period_in_seconds", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.KEY_CACHE_SAVE_PERIOD_IN_SECONDS, new org.apache.thrift.meta_data.FieldMetaData("key_cache_save_period_in_seconds", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.MEMTABLE_FLUSH_AFTER_MINS, new org.apache.thrift.meta_data.FieldMetaData("memtable_flush_after_mins", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.MEMTABLE_THROUGHPUT_IN_MB, new org.apache.thrift.meta_data.FieldMetaData("memtable_throughput_in_mb", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.MEMTABLE_OPERATIONS_IN_MILLIONS, new org.apache.thrift.meta_data.FieldMetaData("memtable_operations_in_millions", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.REPLICATE_ON_WRITE, new org.apache.thrift.meta_data.FieldMetaData("replicate_on_write", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
        tmpMap.put(_Fields.MERGE_SHARDS_CHANCE, new org.apache.thrift.meta_data.FieldMetaData("merge_shards_chance", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.KEY_VALIDATION_CLASS, new org.apache.thrift.meta_data.FieldMetaData("key_validation_class", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.ROW_CACHE_PROVIDER, new org.apache.thrift.meta_data.FieldMetaData("row_cache_provider", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.KEY_ALIAS, new org.apache.thrift.meta_data.FieldMetaData("key_alias", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(CfDef.class, metaDataMap);
    }

    public CfDef() {
        this.column_type = "Standard";
        this.comparator_type = "BytesType";
        this.row_cache_size = (double) 0;
        this.key_cache_size = (double) 200000;
        this.read_repair_chance = 1;
        this.row_cache_provider = "org.apache.cassandra.cache.ConcurrentLinkedHashCacheProvider";
    }

    public CfDef(String keyspace, String name) {
        this();
        this.keyspace = keyspace;
        this.name = name;
    }

    public CfDef(CfDef other) {
        __isset_bit_vector.clear();
        __isset_bit_vector.or(other.__isset_bit_vector);
        if (other.isSetKeyspace()) {
            this.keyspace = other.keyspace;
        }
        if (other.isSetName()) {
            this.name = other.name;
        }
        if (other.isSetColumn_type()) {
            this.column_type = other.column_type;
        }
        if (other.isSetComparator_type()) {
            this.comparator_type = other.comparator_type;
        }
        if (other.isSetSubcomparator_type()) {
            this.subcomparator_type = other.subcomparator_type;
        }
        if (other.isSetComment()) {
            this.comment = other.comment;
        }
        this.row_cache_size = other.row_cache_size;
        this.key_cache_size = other.key_cache_size;
        this.read_repair_chance = other.read_repair_chance;
        if (other.isSetColumn_metadata()) {
            List<ColumnDef> __this__column_metadata = new ArrayList<ColumnDef>();
            for (ColumnDef other_element : other.column_metadata) {
                __this__column_metadata.add(new ColumnDef(other_element));
            }
            this.column_metadata = __this__column_metadata;
        }
        this.gc_grace_seconds = other.gc_grace_seconds;
        if (other.isSetDefault_validation_class()) {
            this.default_validation_class = other.default_validation_class;
        }
        this.id = other.id;
        this.min_compaction_threshold = other.min_compaction_threshold;
        this.max_compaction_threshold = other.max_compaction_threshold;
        this.row_cache_save_period_in_seconds = other.row_cache_save_period_in_seconds;
        this.key_cache_save_period_in_seconds = other.key_cache_save_period_in_seconds;
<<<<<<< MINE
        this.replicate_on_write = other.replicate_on_write;
        this.merge_shards_chance = other.merge_shards_chance;
        if (other.isSetKey_validation_class()) {
            this.key_validation_class = other.key_validation_class;
        }
        if (other.isSetRow_cache_provider()) {
            this.row_cache_provider = other.row_cache_provider;
        }
        if (other.isSetKey_alias()) {
            this.key_alias = org.apache.thrift.TBaseHelper.copyBinary(other.key_alias);
            ;
        }
        if (other.isSetCompaction_strategy()) {
            this.compaction_strategy = other.compaction_strategy;
        }
        if (other.isSetCompaction_strategy_options()) {
            Map<String, String> __this__compaction_strategy_options = new HashMap<String, String>();
            for (Map.Entry<String, String> other_element : other.compaction_strategy_options.entrySet()) {
                String other_element_key = other_element.getKey();
                String other_element_value = other_element.getValue();
                String __this__compaction_strategy_options_copy_key = other_element_key;
                String __this__compaction_strategy_options_copy_value = other_element_value;
                __this__compaction_strategy_options.put(__this__compaction_strategy_options_copy_key, __this__compaction_strategy_options_copy_value);
            }
            this.compaction_strategy_options = __this__compaction_strategy_options;
        }
        this.row_cache_keys_to_save = other.row_cache_keys_to_save;
        if (other.isSetCompression_options()) {
            Map<String, String> __this__compression_options = new HashMap<String, String>();
            for (Map.Entry<String, String> other_element : other.compression_options.entrySet()) {
                String other_element_key = other_element.getKey();
                String other_element_value = other_element.getValue();
                String __this__compression_options_copy_key = other_element_key;
                String __this__compression_options_copy_value = other_element_value;
                __this__compression_options.put(__this__compression_options_copy_key, __this__compression_options_copy_value);
            }
            this.compression_options = __this__compression_options;
        }
        this.bloom_filter_fp_chance = other.bloom_filter_fp_chance;
=======
        this.memtable_flush_after_mins = other.memtable_flush_after_mins;
        this.memtable_throughput_in_mb = other.memtable_throughput_in_mb;
        this.memtable_operations_in_millions = other.memtable_operations_in_millions;
        this.replicate_on_write = other.replicate_on_write;
        this.merge_shards_chance = other.merge_shards_chance;
        if (other.isSetKey_validation_class()) {
            this.key_validation_class = other.key_validation_class;
        }
        if (other.isSetRow_cache_provider()) {
            this.row_cache_provider = other.row_cache_provider;
        }
        if (other.isSetKey_alias()) {
            this.key_alias = org.apache.thrift.TBaseHelper.copyBinary(other.key_alias);
            ;
        }
>>>>>>> YOURS
    }

    public CfDef deepCopy() {
        return new CfDef(this);
    }

    @Override
    public void clear() {
        this.keyspace = null;
        this.name = null;
        this.column_type = "Standard";
        this.comparator_type = "BytesType";
        this.subcomparator_type = null;
        this.comment = null;
        this.row_cache_size = (double) 0;
        this.key_cache_size = (double) 200000;
        this.read_repair_chance = 1;
        this.column_metadata = null;
        setGc_grace_secondsIsSet(false);
        this.gc_grace_seconds = 0;
        this.default_validation_class = null;
        setIdIsSet(false);
        this.id = 0;
        setMin_compaction_thresholdIsSet(false);
        this.min_compaction_threshold = 0;
        setMax_compaction_thresholdIsSet(false);
        this.max_compaction_threshold = 0;
        setRow_cache_save_period_in_secondsIsSet(false);
        this.row_cache_save_period_in_seconds = 0;
        setKey_cache_save_period_in_secondsIsSet(false);
        this.key_cache_save_period_in_seconds = 0;
<<<<<<< MINE
        setReplicate_on_writeIsSet(false);
        this.replicate_on_write = false;
        setMerge_shards_chanceIsSet(false);
        this.merge_shards_chance = 0.0;
        this.key_validation_class = null;
        this.row_cache_provider = null;
        this.key_alias = null;
        this.compaction_strategy = null;
        this.compaction_strategy_options = null;
        setRow_cache_keys_to_saveIsSet(false);
        this.row_cache_keys_to_save = 0;
        this.compression_options = null;
        setBloom_filter_fp_chanceIsSet(false);
        this.bloom_filter_fp_chance = 0.0;
=======
        setMemtable_flush_after_minsIsSet(false);
        this.memtable_flush_after_mins = 0;
        setMemtable_throughput_in_mbIsSet(false);
        this.memtable_throughput_in_mb = 0;
        setMemtable_operations_in_millionsIsSet(false);
        this.memtable_operations_in_millions = 0.0;
        setReplicate_on_writeIsSet(false);
        this.replicate_on_write = false;
        setMerge_shards_chanceIsSet(false);
        this.merge_shards_chance = 0.0;
        this.key_validation_class = null;
        this.row_cache_provider = "org.apache.cassandra.cache.ConcurrentLinkedHashCacheProvider";
        this.key_alias = null;
>>>>>>> YOURS
    }

    public String getKeyspace() {
        return this.keyspace;
    }

    public CfDef setKeyspace(String keyspace) {
        this.keyspace = keyspace;
        return this;
    }

    public void unsetKeyspace() {
        this.keyspace = null;
    }

    public boolean isSetKeyspace() {
        return this.keyspace != null;
    }

    public void setKeyspaceIsSet(boolean value) {
        if (!value) {
            this.keyspace = null;
        }
    }

    public String getName() {
        return this.name;
    }

    public CfDef setName(String name) {
        this.name = name;
        return this;
    }

    public void unsetName() {
        this.name = null;
    }

    public boolean isSetName() {
        return this.name != null;
    }

    public void setNameIsSet(boolean value) {
        if (!value) {
            this.name = null;
        }
    }

    public String getColumn_type() {
        return this.column_type;
    }

    public CfDef setColumn_type(String column_type) {
        this.column_type = column_type;
        return this;
    }

    public void unsetColumn_type() {
        this.column_type = null;
    }

    public boolean isSetColumn_type() {
        return this.column_type != null;
    }

    public void setColumn_typeIsSet(boolean value) {
        if (!value) {
            this.column_type = null;
        }
    }

    public String getComparator_type() {
        return this.comparator_type;
    }

    public CfDef setComparator_type(String comparator_type) {
        this.comparator_type = comparator_type;
        return this;
    }

    public void unsetComparator_type() {
        this.comparator_type = null;
    }

    public boolean isSetComparator_type() {
        return this.comparator_type != null;
    }

    public void setComparator_typeIsSet(boolean value) {
        if (!value) {
            this.comparator_type = null;
        }
    }

    public String getSubcomparator_type() {
        return this.subcomparator_type;
    }

    public CfDef setSubcomparator_type(String subcomparator_type) {
        this.subcomparator_type = subcomparator_type;
        return this;
    }

    public void unsetSubcomparator_type() {
        this.subcomparator_type = null;
    }

    public boolean isSetSubcomparator_type() {
        return this.subcomparator_type != null;
    }

    public void setSubcomparator_typeIsSet(boolean value) {
        if (!value) {
            this.subcomparator_type = null;
        }
    }

    public String getComment() {
        return this.comment;
    }

    public CfDef setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public void unsetComment() {
        this.comment = null;
    }

    public boolean isSetComment() {
        return this.comment != null;
    }

    public void setCommentIsSet(boolean value) {
        if (!value) {
            this.comment = null;
        }
    }

    public double getRow_cache_size() {
        return this.row_cache_size;
    }

    public CfDef setRow_cache_size(double row_cache_size) {
        this.row_cache_size = row_cache_size;
        setRow_cache_sizeIsSet(true);
        return this;
    }

    public void unsetRow_cache_size() {
        __isset_bit_vector.clear(__ROW_CACHE_SIZE_ISSET_ID);
    }

    public boolean isSetRow_cache_size() {
        return __isset_bit_vector.get(__ROW_CACHE_SIZE_ISSET_ID);
    }

    public void setRow_cache_sizeIsSet(boolean value) {
        __isset_bit_vector.set(__ROW_CACHE_SIZE_ISSET_ID, value);
    }

    public double getKey_cache_size() {
        return this.key_cache_size;
    }

    public CfDef setKey_cache_size(double key_cache_size) {
        this.key_cache_size = key_cache_size;
        setKey_cache_sizeIsSet(true);
        return this;
    }

    public void unsetKey_cache_size() {
        __isset_bit_vector.clear(__KEY_CACHE_SIZE_ISSET_ID);
    }

    public boolean isSetKey_cache_size() {
        return __isset_bit_vector.get(__KEY_CACHE_SIZE_ISSET_ID);
    }

    public void setKey_cache_sizeIsSet(boolean value) {
        __isset_bit_vector.set(__KEY_CACHE_SIZE_ISSET_ID, value);
    }

    public double getRead_repair_chance() {
        return this.read_repair_chance;
    }

    public CfDef setRead_repair_chance(double read_repair_chance) {
        this.read_repair_chance = read_repair_chance;
        setRead_repair_chanceIsSet(true);
        return this;
    }

    public void unsetRead_repair_chance() {
        __isset_bit_vector.clear(__READ_REPAIR_CHANCE_ISSET_ID);
    }

    public boolean isSetRead_repair_chance() {
        return __isset_bit_vector.get(__READ_REPAIR_CHANCE_ISSET_ID);
    }

    public void setRead_repair_chanceIsSet(boolean value) {
        __isset_bit_vector.set(__READ_REPAIR_CHANCE_ISSET_ID, value);
    }

    public int getColumn_metadataSize() {
        return (this.column_metadata == null) ? 0 : this.column_metadata.size();
    }

    public java.util.Iterator<ColumnDef> getColumn_metadataIterator() {
        return (this.column_metadata == null) ? null : this.column_metadata.iterator();
    }

    public void addToColumn_metadata(ColumnDef elem) {
        if (this.column_metadata == null) {
            this.column_metadata = new ArrayList<ColumnDef>();
        }
        this.column_metadata.add(elem);
    }

    public List<ColumnDef> getColumn_metadata() {
        return this.column_metadata;
    }

    public CfDef setColumn_metadata(List<ColumnDef> column_metadata) {
        this.column_metadata = column_metadata;
        return this;
    }

    public void unsetColumn_metadata() {
        this.column_metadata = null;
    }

    public boolean isSetColumn_metadata() {
        return this.column_metadata != null;
    }

    public void setColumn_metadataIsSet(boolean value) {
        if (!value) {
            this.column_metadata = null;
        }
    }

    public int getGc_grace_seconds() {
        return this.gc_grace_seconds;
    }

    public CfDef setGc_grace_seconds(int gc_grace_seconds) {
        this.gc_grace_seconds = gc_grace_seconds;
        setGc_grace_secondsIsSet(true);
        return this;
    }

    public void unsetGc_grace_seconds() {
        __isset_bit_vector.clear(__GC_GRACE_SECONDS_ISSET_ID);
    }

    public boolean isSetGc_grace_seconds() {
        return __isset_bit_vector.get(__GC_GRACE_SECONDS_ISSET_ID);
    }

    public void setGc_grace_secondsIsSet(boolean value) {
        __isset_bit_vector.set(__GC_GRACE_SECONDS_ISSET_ID, value);
    }

    public String getDefault_validation_class() {
        return this.default_validation_class;
    }

    public CfDef setDefault_validation_class(String default_validation_class) {
        this.default_validation_class = default_validation_class;
        return this;
    }

    public void unsetDefault_validation_class() {
        this.default_validation_class = null;
    }

    public boolean isSetDefault_validation_class() {
        return this.default_validation_class != null;
    }

    public void setDefault_validation_classIsSet(boolean value) {
        if (!value) {
            this.default_validation_class = null;
        }
    }

    public int getId() {
        return this.id;
    }

    public CfDef setId(int id) {
        this.id = id;
        setIdIsSet(true);
        return this;
    }

    public void unsetId() {
        __isset_bit_vector.clear(__ID_ISSET_ID);
    }

    public boolean isSetId() {
        return __isset_bit_vector.get(__ID_ISSET_ID);
    }

    public void setIdIsSet(boolean value) {
        __isset_bit_vector.set(__ID_ISSET_ID, value);
    }

    public int getMin_compaction_threshold() {
        return this.min_compaction_threshold;
    }

    public CfDef setMin_compaction_threshold(int min_compaction_threshold) {
        this.min_compaction_threshold = min_compaction_threshold;
        setMin_compaction_thresholdIsSet(true);
        return this;
    }

    public void unsetMin_compaction_threshold() {
        __isset_bit_vector.clear(__MIN_COMPACTION_THRESHOLD_ISSET_ID);
    }

    public boolean isSetMin_compaction_threshold() {
        return __isset_bit_vector.get(__MIN_COMPACTION_THRESHOLD_ISSET_ID);
    }

    public void setMin_compaction_thresholdIsSet(boolean value) {
        __isset_bit_vector.set(__MIN_COMPACTION_THRESHOLD_ISSET_ID, value);
    }

    public int getMax_compaction_threshold() {
        return this.max_compaction_threshold;
    }

    public CfDef setMax_compaction_threshold(int max_compaction_threshold) {
        this.max_compaction_threshold = max_compaction_threshold;
        setMax_compaction_thresholdIsSet(true);
        return this;
    }

    public void unsetMax_compaction_threshold() {
        __isset_bit_vector.clear(__MAX_COMPACTION_THRESHOLD_ISSET_ID);
    }

    public boolean isSetMax_compaction_threshold() {
        return __isset_bit_vector.get(__MAX_COMPACTION_THRESHOLD_ISSET_ID);
    }

    public void setMax_compaction_thresholdIsSet(boolean value) {
        __isset_bit_vector.set(__MAX_COMPACTION_THRESHOLD_ISSET_ID, value);
    }

    public int getRow_cache_save_period_in_seconds() {
        return this.row_cache_save_period_in_seconds;
    }

    public CfDef setRow_cache_save_period_in_seconds(int row_cache_save_period_in_seconds) {
        this.row_cache_save_period_in_seconds = row_cache_save_period_in_seconds;
        setRow_cache_save_period_in_secondsIsSet(true);
        return this;
    }

    public void unsetRow_cache_save_period_in_seconds() {
        __isset_bit_vector.clear(__ROW_CACHE_SAVE_PERIOD_IN_SECONDS_ISSET_ID);
    }

    public boolean isSetRow_cache_save_period_in_seconds() {
        return __isset_bit_vector.get(__ROW_CACHE_SAVE_PERIOD_IN_SECONDS_ISSET_ID);
    }

    public void setRow_cache_save_period_in_secondsIsSet(boolean value) {
        __isset_bit_vector.set(__ROW_CACHE_SAVE_PERIOD_IN_SECONDS_ISSET_ID, value);
    }

    public int getKey_cache_save_period_in_seconds() {
        return this.key_cache_save_period_in_seconds;
    }

    public CfDef setKey_cache_save_period_in_seconds(int key_cache_save_period_in_seconds) {
        this.key_cache_save_period_in_seconds = key_cache_save_period_in_seconds;
        setKey_cache_save_period_in_secondsIsSet(true);
        return this;
    }

    public void unsetKey_cache_save_period_in_seconds() {
        __isset_bit_vector.clear(__KEY_CACHE_SAVE_PERIOD_IN_SECONDS_ISSET_ID);
    }

    public boolean isSetKey_cache_save_period_in_seconds() {
        return __isset_bit_vector.get(__KEY_CACHE_SAVE_PERIOD_IN_SECONDS_ISSET_ID);
    }

    public void setKey_cache_save_period_in_secondsIsSet(boolean value) {
        __isset_bit_vector.set(__KEY_CACHE_SAVE_PERIOD_IN_SECONDS_ISSET_ID, value);
    }

    public boolean isReplicate_on_write() {
        return this.replicate_on_write;
    }

    public CfDef setReplicate_on_write(boolean replicate_on_write) {
        this.replicate_on_write = replicate_on_write;
        setReplicate_on_writeIsSet(true);
        return this;
    }

    public void unsetReplicate_on_write() {
        __isset_bit_vector.clear(__REPLICATE_ON_WRITE_ISSET_ID);
    }

    public boolean isSetReplicate_on_write() {
        return __isset_bit_vector.get(__REPLICATE_ON_WRITE_ISSET_ID);
    }

    public void setReplicate_on_writeIsSet(boolean value) {
        __isset_bit_vector.set(__REPLICATE_ON_WRITE_ISSET_ID, value);
    }

    public double getMerge_shards_chance() {
        return this.merge_shards_chance;
    }

    public CfDef setMerge_shards_chance(double merge_shards_chance) {
        this.merge_shards_chance = merge_shards_chance;
        setMerge_shards_chanceIsSet(true);
        return this;
    }

    public void unsetMerge_shards_chance() {
        __isset_bit_vector.clear(__MERGE_SHARDS_CHANCE_ISSET_ID);
    }

    public boolean isSetMerge_shards_chance() {
        return __isset_bit_vector.get(__MERGE_SHARDS_CHANCE_ISSET_ID);
    }

    public void setMerge_shards_chanceIsSet(boolean value) {
        __isset_bit_vector.set(__MERGE_SHARDS_CHANCE_ISSET_ID, value);
    }

    public String getKey_validation_class() {
        return this.key_validation_class;
    }

    public CfDef setKey_validation_class(String key_validation_class) {
        this.key_validation_class = key_validation_class;
        return this;
    }

    public void unsetKey_validation_class() {
        this.key_validation_class = null;
    }

    public boolean isSetKey_validation_class() {
        return this.key_validation_class != null;
    }

    public void setKey_validation_classIsSet(boolean value) {
        if (!value) {
            this.key_validation_class = null;
        }
    }

    public String getRow_cache_provider() {
        return this.row_cache_provider;
    }

    public CfDef setRow_cache_provider(String row_cache_provider) {
        this.row_cache_provider = row_cache_provider;
        return this;
    }

    public void unsetRow_cache_provider() {
        this.row_cache_provider = null;
    }

    public boolean isSetRow_cache_provider() {
        return this.row_cache_provider != null;
    }

    public void setRow_cache_providerIsSet(boolean value) {
        if (!value) {
            this.row_cache_provider = null;
        }
    }

    public byte[] getKey_alias() {
        setKey_alias(org.apache.thrift.TBaseHelper.rightSize(key_alias));
        return key_alias == null ? null : key_alias.array();
    }

    public ByteBuffer bufferForKey_alias() {
        return key_alias;
    }

    public CfDef setKey_alias(byte[] key_alias) {
        setKey_alias(key_alias == null ? (ByteBuffer) null : ByteBuffer.wrap(key_alias));
        return this;
    }

    public CfDef setKey_alias(ByteBuffer key_alias) {
        this.key_alias = key_alias;
        return this;
    }

    public void unsetKey_alias() {
        this.key_alias = null;
    }

    public boolean isSetKey_alias() {
        return this.key_alias != null;
    }

    public void setKey_aliasIsSet(boolean value) {
        if (!value) {
            this.key_alias = null;
        }
    }

    public String getCompaction_strategy() {
        return this.compaction_strategy;
    }

    public CfDef setCompaction_strategy(String compaction_strategy) {
        this.compaction_strategy = compaction_strategy;
        return this;
    }

    public void unsetCompaction_strategy() {
        this.compaction_strategy = null;
    }

    public boolean isSetCompaction_strategy() {
        return this.compaction_strategy != null;
    }

    public void setCompaction_strategyIsSet(boolean value) {
        if (!value) {
            this.compaction_strategy = null;
        }
    }

    public int getCompaction_strategy_optionsSize() {
        return (this.compaction_strategy_options == null) ? 0 : this.compaction_strategy_options.size();
    }

    public void putToCompaction_strategy_options(String key, String val) {
        if (this.compaction_strategy_options == null) {
            this.compaction_strategy_options = new HashMap<String, String>();
        }
        this.compaction_strategy_options.put(key, val);
    }

    public Map<String, String> getCompaction_strategy_options() {
        return this.compaction_strategy_options;
    }

    public CfDef setCompaction_strategy_options(Map<String, String> compaction_strategy_options) {
        this.compaction_strategy_options = compaction_strategy_options;
        return this;
    }

    public void unsetCompaction_strategy_options() {
        this.compaction_strategy_options = null;
    }

    public boolean isSetCompaction_strategy_options() {
        return this.compaction_strategy_options != null;
    }

    public void setCompaction_strategy_optionsIsSet(boolean value) {
        if (!value) {
            this.compaction_strategy_options = null;
        }
    }

    public int getRow_cache_keys_to_save() {
        return this.row_cache_keys_to_save;
    }

    public CfDef setRow_cache_keys_to_save(int row_cache_keys_to_save) {
        this.row_cache_keys_to_save = row_cache_keys_to_save;
        setRow_cache_keys_to_saveIsSet(true);
        return this;
    }

    public void unsetRow_cache_keys_to_save() {
        __isset_bit_vector.clear(__ROW_CACHE_KEYS_TO_SAVE_ISSET_ID);
    }

    public boolean isSetRow_cache_keys_to_save() {
        return __isset_bit_vector.get(__ROW_CACHE_KEYS_TO_SAVE_ISSET_ID);
    }

    public void setRow_cache_keys_to_saveIsSet(boolean value) {
        __isset_bit_vector.set(__ROW_CACHE_KEYS_TO_SAVE_ISSET_ID, value);
    }

    public int getCompression_optionsSize() {
        return (this.compression_options == null) ? 0 : this.compression_options.size();
    }

    public void putToCompression_options(String key, String val) {
        if (this.compression_options == null) {
            this.compression_options = new HashMap<String, String>();
        }
        this.compression_options.put(key, val);
    }

    public Map<String, String> getCompression_options() {
        return this.compression_options;
    }

    public CfDef setCompression_options(Map<String, String> compression_options) {
        this.compression_options = compression_options;
        return this;
    }

    public void unsetCompression_options() {
        this.compression_options = null;
    }

    public boolean isSetCompression_options() {
        return this.compression_options != null;
    }

    public void setCompression_optionsIsSet(boolean value) {
        if (!value) {
            this.compression_options = null;
        }
    }

    public double getBloom_filter_fp_chance() {
        return this.bloom_filter_fp_chance;
    }

    public CfDef setBloom_filter_fp_chance(double bloom_filter_fp_chance) {
        this.bloom_filter_fp_chance = bloom_filter_fp_chance;
        setBloom_filter_fp_chanceIsSet(true);
        return this;
    }

    public void unsetBloom_filter_fp_chance() {
        __isset_bit_vector.clear(__BLOOM_FILTER_FP_CHANCE_ISSET_ID);
    }

    public boolean isSetBloom_filter_fp_chance() {
        return __isset_bit_vector.get(__BLOOM_FILTER_FP_CHANCE_ISSET_ID);
    }

    public void setBloom_filter_fp_chanceIsSet(boolean value) {
        __isset_bit_vector.set(__BLOOM_FILTER_FP_CHANCE_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case KEYSPACE:
                if (value == null) {
                    unsetKeyspace();
                } else {
                    setKeyspace((String) value);
                }
                break;
            case NAME:
                if (value == null) {
                    unsetName();
                } else {
                    setName((String) value);
                }
                break;
            case COLUMN_TYPE:
                if (value == null) {
                    unsetColumn_type();
                } else {
                    setColumn_type((String) value);
                }
                break;
            case COMPARATOR_TYPE:
                if (value == null) {
                    unsetComparator_type();
                } else {
                    setComparator_type((String) value);
                }
                break;
            case SUBCOMPARATOR_TYPE:
                if (value == null) {
                    unsetSubcomparator_type();
                } else {
                    setSubcomparator_type((String) value);
                }
                break;
            case COMMENT:
                if (value == null) {
                    unsetComment();
                } else {
                    setComment((String) value);
                }
                break;
            case ROW_CACHE_SIZE:
                if (value == null) {
                    unsetRow_cache_size();
                } else {
                    setRow_cache_size((Double) value);
                }
                break;
            case KEY_CACHE_SIZE:
                if (value == null) {
                    unsetKey_cache_size();
                } else {
                    setKey_cache_size((Double) value);
                }
                break;
            case READ_REPAIR_CHANCE:
                if (value == null) {
                    unsetRead_repair_chance();
                } else {
                    setRead_repair_chance((Double) value);
                }
                break;
            case COLUMN_METADATA:
                if (value == null) {
                    unsetColumn_metadata();
                } else {
                    setColumn_metadata((List<ColumnDef>) value);
                }
                break;
            case GC_GRACE_SECONDS:
                if (value == null) {
                    unsetGc_grace_seconds();
                } else {
                    setGc_grace_seconds((Integer) value);
                }
                break;
            case DEFAULT_VALIDATION_CLASS:
                if (value == null) {
                    unsetDefault_validation_class();
                } else {
                    setDefault_validation_class((String) value);
                }
                break;
            case ID:
                if (value == null) {
                    unsetId();
                } else {
                    setId((Integer) value);
                }
                break;
            case MIN_COMPACTION_THRESHOLD:
                if (value == null) {
                    unsetMin_compaction_threshold();
                } else {
                    setMin_compaction_threshold((Integer) value);
                }
                break;
            case MAX_COMPACTION_THRESHOLD:
                if (value == null) {
                    unsetMax_compaction_threshold();
                } else {
                    setMax_compaction_threshold((Integer) value);
                }
                break;
            case ROW_CACHE_SAVE_PERIOD_IN_SECONDS:
                if (value == null) {
                    unsetRow_cache_save_period_in_seconds();
                } else {
                    setRow_cache_save_period_in_seconds((Integer) value);
                }
                break;
            case KEY_CACHE_SAVE_PERIOD_IN_SECONDS:
                if (value == null) {
                    unsetKey_cache_save_period_in_seconds();
                } else {
                    setKey_cache_save_period_in_seconds((Integer) value);
                }
                break;
            case REPLICATE_ON_WRITE:
                if (value == null) {
                    unsetReplicate_on_write();
                } else {
                    setReplicate_on_write((Boolean) value);
                }
                break;
            case MERGE_SHARDS_CHANCE:
                if (value == null) {
                    unsetMerge_shards_chance();
                } else {
                    setMerge_shards_chance((Double) value);
                }
                break;
            case KEY_VALIDATION_CLASS:
                if (value == null) {
                    unsetKey_validation_class();
                } else {
                    setKey_validation_class((String) value);
                }
                break;
            case ROW_CACHE_PROVIDER:
                if (value == null) {
                    unsetRow_cache_provider();
                } else {
                    setRow_cache_provider((String) value);
                }
                break;
            case KEY_ALIAS:
                if (value == null) {
                    unsetKey_alias();
                } else {
                    setKey_alias((ByteBuffer) value);
                }
                break;
            case COMPACTION_STRATEGY:
                if (value == null) {
                    unsetCompaction_strategy();
                } else {
                    setCompaction_strategy((String) value);
                }
                break;
            case COMPACTION_STRATEGY_OPTIONS:
                if (value == null) {
                    unsetCompaction_strategy_options();
                } else {
                    setCompaction_strategy_options((Map<String, String>) value);
                }
                break;
            case ROW_CACHE_KEYS_TO_SAVE:
                if (value == null) {
                    unsetRow_cache_keys_to_save();
                } else {
                    setRow_cache_keys_to_save((Integer) value);
                }
                break;
            case COMPRESSION_OPTIONS:
                if (value == null) {
                    unsetCompression_options();
                } else {
                    setCompression_options((Map<String, String>) value);
                }
                break;
            case BLOOM_FILTER_FP_CHANCE:
                if (value == null) {
                    unsetBloom_filter_fp_chance();
                } else {
                    setBloom_filter_fp_chance((Double) value);
                }
                break;
            case REPLICATE_ON_WRITE:
                if (value == null) {
                    unsetReplicate_on_write();
                } else {
                    setReplicate_on_write((Boolean) value);
                }
                break;
            case MERGE_SHARDS_CHANCE:
                if (value == null) {
                    unsetMerge_shards_chance();
                } else {
                    setMerge_shards_chance((Double) value);
                }
                break;
            case KEY_VALIDATION_CLASS:
                if (value == null) {
                    unsetKey_validation_class();
                } else {
                    setKey_validation_class((String) value);
                }
                break;
            case ROW_CACHE_PROVIDER:
                if (value == null) {
                    unsetRow_cache_provider();
                } else {
                    setRow_cache_provider((String) value);
                }
                break;
            case KEY_ALIAS:
                if (value == null) {
                    unsetKey_alias();
                } else {
                    setKey_alias((ByteBuffer) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case KEYSPACE:
                return getKeyspace();
            case NAME:
                return getName();
            case COLUMN_TYPE:
                return getColumn_type();
            case COMPARATOR_TYPE:
                return getComparator_type();
            case SUBCOMPARATOR_TYPE:
                return getSubcomparator_type();
            case COMMENT:
                return getComment();
            case ROW_CACHE_SIZE:
                return new Double(getRow_cache_size());
            case KEY_CACHE_SIZE:
                return new Double(getKey_cache_size());
            case READ_REPAIR_CHANCE:
                return new Double(getRead_repair_chance());
            case COLUMN_METADATA:
                return getColumn_metadata();
            case GC_GRACE_SECONDS:
                return new Integer(getGc_grace_seconds());
            case DEFAULT_VALIDATION_CLASS:
                return getDefault_validation_class();
            case ID:
                return new Integer(getId());
            case MIN_COMPACTION_THRESHOLD:
                return new Integer(getMin_compaction_threshold());
            case MAX_COMPACTION_THRESHOLD:
                return new Integer(getMax_compaction_threshold());
            case ROW_CACHE_SAVE_PERIOD_IN_SECONDS:
                return new Integer(getRow_cache_save_period_in_seconds());
            case KEY_CACHE_SAVE_PERIOD_IN_SECONDS:
                return new Integer(getKey_cache_save_period_in_seconds());
<<<<<<< MINE
            case REPLICATE_ON_WRITE:
                return new Boolean(isReplicate_on_write());
            case MERGE_SHARDS_CHANCE:
                return new Double(getMerge_shards_chance());
            case KEY_VALIDATION_CLASS:
                return getKey_validation_class();
            case ROW_CACHE_PROVIDER:
                return getRow_cache_provider();
            case KEY_ALIAS:
                return getKey_alias();
            case COMPACTION_STRATEGY:
                return getCompaction_strategy();
            case COMPACTION_STRATEGY_OPTIONS:
                return getCompaction_strategy_options();
            case ROW_CACHE_KEYS_TO_SAVE:
                return new Integer(getRow_cache_keys_to_save());
            case COMPRESSION_OPTIONS:
                return getCompression_options();
            case BLOOM_FILTER_FP_CHANCE:
                return new Double(getBloom_filter_fp_chance());
=======
            case MEMTABLE_FLUSH_AFTER_MINS:
                return new Integer(getMemtable_flush_after_mins());
            case MEMTABLE_THROUGHPUT_IN_MB:
                return new Integer(getMemtable_throughput_in_mb());
            case MEMTABLE_OPERATIONS_IN_MILLIONS:
                return new Double(getMemtable_operations_in_millions());
            case REPLICATE_ON_WRITE:
                return new Boolean(isReplicate_on_write());
            case MERGE_SHARDS_CHANCE:
                return new Double(getMerge_shards_chance());
            case KEY_VALIDATION_CLASS:
                return getKey_validation_class();
            case ROW_CACHE_PROVIDER:
                return getRow_cache_provider();
            case KEY_ALIAS:
                return getKey_alias();
>>>>>>> YOURS
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case KEYSPACE:
                return isSetKeyspace();
            case NAME:
                return isSetName();
            case COLUMN_TYPE:
                return isSetColumn_type();
            case COMPARATOR_TYPE:
                return isSetComparator_type();
            case SUBCOMPARATOR_TYPE:
                return isSetSubcomparator_type();
            case COMMENT:
                return isSetComment();
            case ROW_CACHE_SIZE:
                return isSetRow_cache_size();
            case KEY_CACHE_SIZE:
                return isSetKey_cache_size();
            case READ_REPAIR_CHANCE:
                return isSetRead_repair_chance();
            case COLUMN_METADATA:
                return isSetColumn_metadata();
            case GC_GRACE_SECONDS:
                return isSetGc_grace_seconds();
            case DEFAULT_VALIDATION_CLASS:
                return isSetDefault_validation_class();
            case ID:
                return isSetId();
            case MIN_COMPACTION_THRESHOLD:
                return isSetMin_compaction_threshold();
            case MAX_COMPACTION_THRESHOLD:
                return isSetMax_compaction_threshold();
            case ROW_CACHE_SAVE_PERIOD_IN_SECONDS:
                return isSetRow_cache_save_period_in_seconds();
            case KEY_CACHE_SAVE_PERIOD_IN_SECONDS:
                return isSetKey_cache_save_period_in_seconds();
<<<<<<< MINE
            case REPLICATE_ON_WRITE:
                return isSetReplicate_on_write();
            case MERGE_SHARDS_CHANCE:
                return isSetMerge_shards_chance();
            case KEY_VALIDATION_CLASS:
                return isSetKey_validation_class();
            case ROW_CACHE_PROVIDER:
                return isSetRow_cache_provider();
            case KEY_ALIAS:
                return isSetKey_alias();
            case COMPACTION_STRATEGY:
                return isSetCompaction_strategy();
            case COMPACTION_STRATEGY_OPTIONS:
                return isSetCompaction_strategy_options();
            case ROW_CACHE_KEYS_TO_SAVE:
                return isSetRow_cache_keys_to_save();
            case COMPRESSION_OPTIONS:
                return isSetCompression_options();
            case BLOOM_FILTER_FP_CHANCE:
                return isSetBloom_filter_fp_chance();
=======
            case MEMTABLE_FLUSH_AFTER_MINS:
                return isSetMemtable_flush_after_mins();
            case MEMTABLE_THROUGHPUT_IN_MB:
                return isSetMemtable_throughput_in_mb();
            case MEMTABLE_OPERATIONS_IN_MILLIONS:
                return isSetMemtable_operations_in_millions();
            case REPLICATE_ON_WRITE:
                return isSetReplicate_on_write();
            case MERGE_SHARDS_CHANCE:
                return isSetMerge_shards_chance();
            case KEY_VALIDATION_CLASS:
                return isSetKey_validation_class();
            case ROW_CACHE_PROVIDER:
                return isSetRow_cache_provider();
            case KEY_ALIAS:
                return isSetKey_alias();
>>>>>>> YOURS
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof CfDef)
            return this.equals((CfDef) that);
        return false;
    }

    public boolean equals(CfDef that) {
        if (that == null)
            return false;
        boolean this_present_keyspace = true && this.isSetKeyspace();
        boolean that_present_keyspace = true && that.isSetKeyspace();
        if (this_present_keyspace || that_present_keyspace) {
            if (!(this_present_keyspace && that_present_keyspace))
                return false;
            if (!this.keyspace.equals(that.keyspace))
                return false;
        }
        boolean this_present_name = true && this.isSetName();
        boolean that_present_name = true && that.isSetName();
        if (this_present_name || that_present_name) {
            if (!(this_present_name && that_present_name))
                return false;
            if (!this.name.equals(that.name))
                return false;
        }
        boolean this_present_column_type = true && this.isSetColumn_type();
        boolean that_present_column_type = true && that.isSetColumn_type();
        if (this_present_column_type || that_present_column_type) {
            if (!(this_present_column_type && that_present_column_type))
                return false;
            if (!this.column_type.equals(that.column_type))
                return false;
        }
        boolean this_present_comparator_type = true && this.isSetComparator_type();
        boolean that_present_comparator_type = true && that.isSetComparator_type();
        if (this_present_comparator_type || that_present_comparator_type) {
            if (!(this_present_comparator_type && that_present_comparator_type))
                return false;
            if (!this.comparator_type.equals(that.comparator_type))
                return false;
        }
        boolean this_present_subcomparator_type = true && this.isSetSubcomparator_type();
        boolean that_present_subcomparator_type = true && that.isSetSubcomparator_type();
        if (this_present_subcomparator_type || that_present_subcomparator_type) {
            if (!(this_present_subcomparator_type && that_present_subcomparator_type))
                return false;
            if (!this.subcomparator_type.equals(that.subcomparator_type))
                return false;
        }
        boolean this_present_comment = true && this.isSetComment();
        boolean that_present_comment = true && that.isSetComment();
        if (this_present_comment || that_present_comment) {
            if (!(this_present_comment && that_present_comment))
                return false;
            if (!this.comment.equals(that.comment))
                return false;
        }
        boolean this_present_row_cache_size = true && this.isSetRow_cache_size();
        boolean that_present_row_cache_size = true && that.isSetRow_cache_size();
        if (this_present_row_cache_size || that_present_row_cache_size) {
            if (!(this_present_row_cache_size && that_present_row_cache_size))
                return false;
            if (this.row_cache_size != that.row_cache_size)
                return false;
        }
        boolean this_present_key_cache_size = true && this.isSetKey_cache_size();
        boolean that_present_key_cache_size = true && that.isSetKey_cache_size();
        if (this_present_key_cache_size || that_present_key_cache_size) {
            if (!(this_present_key_cache_size && that_present_key_cache_size))
                return false;
            if (this.key_cache_size != that.key_cache_size)
                return false;
        }
        boolean this_present_read_repair_chance = true && this.isSetRead_repair_chance();
        boolean that_present_read_repair_chance = true && that.isSetRead_repair_chance();
        if (this_present_read_repair_chance || that_present_read_repair_chance) {
            if (!(this_present_read_repair_chance && that_present_read_repair_chance))
                return false;
            if (this.read_repair_chance != that.read_repair_chance)
                return false;
        }
        boolean this_present_column_metadata = true && this.isSetColumn_metadata();
        boolean that_present_column_metadata = true && that.isSetColumn_metadata();
        if (this_present_column_metadata || that_present_column_metadata) {
            if (!(this_present_column_metadata && that_present_column_metadata))
                return false;
            if (!this.column_metadata.equals(that.column_metadata))
                return false;
        }
        boolean this_present_gc_grace_seconds = true && this.isSetGc_grace_seconds();
        boolean that_present_gc_grace_seconds = true && that.isSetGc_grace_seconds();
        if (this_present_gc_grace_seconds || that_present_gc_grace_seconds) {
            if (!(this_present_gc_grace_seconds && that_present_gc_grace_seconds))
                return false;
            if (this.gc_grace_seconds != that.gc_grace_seconds)
                return false;
        }
        boolean this_present_default_validation_class = true && this.isSetDefault_validation_class();
        boolean that_present_default_validation_class = true && that.isSetDefault_validation_class();
        if (this_present_default_validation_class || that_present_default_validation_class) {
            if (!(this_present_default_validation_class && that_present_default_validation_class))
                return false;
            if (!this.default_validation_class.equals(that.default_validation_class))
                return false;
        }
        boolean this_present_id = true && this.isSetId();
        boolean that_present_id = true && that.isSetId();
        if (this_present_id || that_present_id) {
            if (!(this_present_id && that_present_id))
                return false;
            if (this.id != that.id)
                return false;
        }
        boolean this_present_min_compaction_threshold = true && this.isSetMin_compaction_threshold();
        boolean that_present_min_compaction_threshold = true && that.isSetMin_compaction_threshold();
        if (this_present_min_compaction_threshold || that_present_min_compaction_threshold) {
            if (!(this_present_min_compaction_threshold && that_present_min_compaction_threshold))
                return false;
            if (this.min_compaction_threshold != that.min_compaction_threshold)
                return false;
        }
        boolean this_present_max_compaction_threshold = true && this.isSetMax_compaction_threshold();
        boolean that_present_max_compaction_threshold = true && that.isSetMax_compaction_threshold();
        if (this_present_max_compaction_threshold || that_present_max_compaction_threshold) {
            if (!(this_present_max_compaction_threshold && that_present_max_compaction_threshold))
                return false;
            if (this.max_compaction_threshold != that.max_compaction_threshold)
                return false;
        }
        boolean this_present_row_cache_save_period_in_seconds = true && this.isSetRow_cache_save_period_in_seconds();
        boolean that_present_row_cache_save_period_in_seconds = true && that.isSetRow_cache_save_period_in_seconds();
        if (this_present_row_cache_save_period_in_seconds || that_present_row_cache_save_period_in_seconds) {
            if (!(this_present_row_cache_save_period_in_seconds && that_present_row_cache_save_period_in_seconds))
                return false;
            if (this.row_cache_save_period_in_seconds != that.row_cache_save_period_in_seconds)
                return false;
        }
        boolean this_present_key_cache_save_period_in_seconds = true && this.isSetKey_cache_save_period_in_seconds();
        boolean that_present_key_cache_save_period_in_seconds = true && that.isSetKey_cache_save_period_in_seconds();
        if (this_present_key_cache_save_period_in_seconds || that_present_key_cache_save_period_in_seconds) {
            if (!(this_present_key_cache_save_period_in_seconds && that_present_key_cache_save_period_in_seconds))
                return false;
            if (this.key_cache_save_period_in_seconds != that.key_cache_save_period_in_seconds)
                return false;
        }
        boolean this_present_replicate_on_write = true && this.isSetReplicate_on_write();
        boolean that_present_replicate_on_write = true && that.isSetReplicate_on_write();
        if (this_present_replicate_on_write || that_present_replicate_on_write) {
            if (!(this_present_replicate_on_write && that_present_replicate_on_write))
                return false;
            if (this.replicate_on_write != that.replicate_on_write)
                return false;
        }
        boolean this_present_merge_shards_chance = true && this.isSetMerge_shards_chance();
        boolean that_present_merge_shards_chance = true && that.isSetMerge_shards_chance();
        if (this_present_merge_shards_chance || that_present_merge_shards_chance) {
            if (!(this_present_merge_shards_chance && that_present_merge_shards_chance))
                return false;
            if (this.merge_shards_chance != that.merge_shards_chance)
                return false;
        }
        boolean this_present_key_validation_class = true && this.isSetKey_validation_class();
        boolean that_present_key_validation_class = true && that.isSetKey_validation_class();
        if (this_present_key_validation_class || that_present_key_validation_class) {
            if (!(this_present_key_validation_class && that_present_key_validation_class))
                return false;
            if (!this.key_validation_class.equals(that.key_validation_class))
                return false;
        }
        boolean this_present_row_cache_provider = true && this.isSetRow_cache_provider();
        boolean that_present_row_cache_provider = true && that.isSetRow_cache_provider();
        if (this_present_row_cache_provider || that_present_row_cache_provider) {
            if (!(this_present_row_cache_provider && that_present_row_cache_provider))
                return false;
            if (!this.row_cache_provider.equals(that.row_cache_provider))
                return false;
        }
        boolean this_present_key_alias = true && this.isSetKey_alias();
        boolean that_present_key_alias = true && that.isSetKey_alias();
        if (this_present_key_alias || that_present_key_alias) {
            if (!(this_present_key_alias && that_present_key_alias))
                return false;
            if (!this.key_alias.equals(that.key_alias))
                return false;
        }
        boolean this_present_compaction_strategy = true && this.isSetCompaction_strategy();
        boolean that_present_compaction_strategy = true && that.isSetCompaction_strategy();
        if (this_present_compaction_strategy || that_present_compaction_strategy) {
            if (!(this_present_compaction_strategy && that_present_compaction_strategy))
                return false;
            if (!this.compaction_strategy.equals(that.compaction_strategy))
                return false;
        }
        boolean this_present_compaction_strategy_options = true && this.isSetCompaction_strategy_options();
        boolean that_present_compaction_strategy_options = true && that.isSetCompaction_strategy_options();
        if (this_present_compaction_strategy_options || that_present_compaction_strategy_options) {
            if (!(this_present_compaction_strategy_options && that_present_compaction_strategy_options))
                return false;
            if (!this.compaction_strategy_options.equals(that.compaction_strategy_options))
                return false;
        }
        boolean this_present_row_cache_keys_to_save = true && this.isSetRow_cache_keys_to_save();
        boolean that_present_row_cache_keys_to_save = true && that.isSetRow_cache_keys_to_save();
        if (this_present_row_cache_keys_to_save || that_present_row_cache_keys_to_save) {
            if (!(this_present_row_cache_keys_to_save && that_present_row_cache_keys_to_save))
                return false;
            if (this.row_cache_keys_to_save != that.row_cache_keys_to_save)
                return false;
        }
        boolean this_present_compression_options = true && this.isSetCompression_options();
        boolean that_present_compression_options = true && that.isSetCompression_options();
        if (this_present_compression_options || that_present_compression_options) {
            if (!(this_present_compression_options && that_present_compression_options))
                return false;
            if (!this.compression_options.equals(that.compression_options))
                return false;
        }
        boolean this_present_bloom_filter_fp_chance = true && this.isSetBloom_filter_fp_chance();
        boolean that_present_bloom_filter_fp_chance = true && that.isSetBloom_filter_fp_chance();
        if (this_present_bloom_filter_fp_chance || that_present_bloom_filter_fp_chance) {
            if (!(this_present_bloom_filter_fp_chance && that_present_bloom_filter_fp_chance))
                return false;
            if (this.bloom_filter_fp_chance != that.bloom_filter_fp_chance)
                return false;
        }
        boolean this_present_replicate_on_write = true && this.isSetReplicate_on_write();
        boolean that_present_replicate_on_write = true && that.isSetReplicate_on_write();
        if (this_present_replicate_on_write || that_present_replicate_on_write) {
            if (!(this_present_replicate_on_write && that_present_replicate_on_write))
                return false;
            if (this.replicate_on_write != that.replicate_on_write)
                return false;
        }
        boolean this_present_merge_shards_chance = true && this.isSetMerge_shards_chance();
        boolean that_present_merge_shards_chance = true && that.isSetMerge_shards_chance();
        if (this_present_merge_shards_chance || that_present_merge_shards_chance) {
            if (!(this_present_merge_shards_chance && that_present_merge_shards_chance))
                return false;
            if (this.merge_shards_chance != that.merge_shards_chance)
                return false;
        }
        boolean this_present_key_validation_class = true && this.isSetKey_validation_class();
        boolean that_present_key_validation_class = true && that.isSetKey_validation_class();
        if (this_present_key_validation_class || that_present_key_validation_class) {
            if (!(this_present_key_validation_class && that_present_key_validation_class))
                return false;
            if (!this.key_validation_class.equals(that.key_validation_class))
                return false;
        }
        boolean this_present_row_cache_provider = true && this.isSetRow_cache_provider();
        boolean that_present_row_cache_provider = true && that.isSetRow_cache_provider();
        if (this_present_row_cache_provider || that_present_row_cache_provider) {
            if (!(this_present_row_cache_provider && that_present_row_cache_provider))
                return false;
            if (!this.row_cache_provider.equals(that.row_cache_provider))
                return false;
        }
        boolean this_present_key_alias = true && this.isSetKey_alias();
        boolean that_present_key_alias = true && that.isSetKey_alias();
        if (this_present_key_alias || that_present_key_alias) {
            if (!(this_present_key_alias && that_present_key_alias))
                return false;
            if (!this.key_alias.equals(that.key_alias))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        boolean present_keyspace = true && (isSetKeyspace());
        builder.append(present_keyspace);
        if (present_keyspace)
            builder.append(keyspace);
        boolean present_name = true && (isSetName());
        builder.append(present_name);
        if (present_name)
            builder.append(name);
        boolean present_column_type = true && (isSetColumn_type());
        builder.append(present_column_type);
        if (present_column_type)
            builder.append(column_type);
        boolean present_comparator_type = true && (isSetComparator_type());
        builder.append(present_comparator_type);
        if (present_comparator_type)
            builder.append(comparator_type);
        boolean present_subcomparator_type = true && (isSetSubcomparator_type());
        builder.append(present_subcomparator_type);
        if (present_subcomparator_type)
            builder.append(subcomparator_type);
        boolean present_comment = true && (isSetComment());
        builder.append(present_comment);
        if (present_comment)
            builder.append(comment);
        boolean present_row_cache_size = true && (isSetRow_cache_size());
        builder.append(present_row_cache_size);
        if (present_row_cache_size)
            builder.append(row_cache_size);
        boolean present_key_cache_size = true && (isSetKey_cache_size());
        builder.append(present_key_cache_size);
        if (present_key_cache_size)
            builder.append(key_cache_size);
        boolean present_read_repair_chance = true && (isSetRead_repair_chance());
        builder.append(present_read_repair_chance);
        if (present_read_repair_chance)
            builder.append(read_repair_chance);
        boolean present_column_metadata = true && (isSetColumn_metadata());
        builder.append(present_column_metadata);
        if (present_column_metadata)
            builder.append(column_metadata);
        boolean present_gc_grace_seconds = true && (isSetGc_grace_seconds());
        builder.append(present_gc_grace_seconds);
        if (present_gc_grace_seconds)
            builder.append(gc_grace_seconds);
        boolean present_default_validation_class = true && (isSetDefault_validation_class());
        builder.append(present_default_validation_class);
        if (present_default_validation_class)
            builder.append(default_validation_class);
        boolean present_id = true && (isSetId());
        builder.append(present_id);
        if (present_id)
            builder.append(id);
        boolean present_min_compaction_threshold = true && (isSetMin_compaction_threshold());
        builder.append(present_min_compaction_threshold);
        if (present_min_compaction_threshold)
            builder.append(min_compaction_threshold);
        boolean present_max_compaction_threshold = true && (isSetMax_compaction_threshold());
        builder.append(present_max_compaction_threshold);
        if (present_max_compaction_threshold)
            builder.append(max_compaction_threshold);
        boolean present_row_cache_save_period_in_seconds = true && (isSetRow_cache_save_period_in_seconds());
        builder.append(present_row_cache_save_period_in_seconds);
        if (present_row_cache_save_period_in_seconds)
            builder.append(row_cache_save_period_in_seconds);
        boolean present_key_cache_save_period_in_seconds = true && (isSetKey_cache_save_period_in_seconds());
        builder.append(present_key_cache_save_period_in_seconds);
        if (present_key_cache_save_period_in_seconds)
            builder.append(key_cache_save_period_in_seconds);
<<<<<<< MINE
        boolean present_replicate_on_write = true && (isSetReplicate_on_write());
        builder.append(present_replicate_on_write);
        if (present_replicate_on_write)
            builder.append(replicate_on_write);
        boolean present_merge_shards_chance = true && (isSetMerge_shards_chance());
        builder.append(present_merge_shards_chance);
        if (present_merge_shards_chance)
            builder.append(merge_shards_chance);
        boolean present_key_validation_class = true && (isSetKey_validation_class());
        builder.append(present_key_validation_class);
        if (present_key_validation_class)
            builder.append(key_validation_class);
        boolean present_row_cache_provider = true && (isSetRow_cache_provider());
        builder.append(present_row_cache_provider);
        if (present_row_cache_provider)
            builder.append(row_cache_provider);
        boolean present_key_alias = true && (isSetKey_alias());
        builder.append(present_key_alias);
        if (present_key_alias)
            builder.append(key_alias);
        boolean present_compaction_strategy = true && (isSetCompaction_strategy());
        builder.append(present_compaction_strategy);
        if (present_compaction_strategy)
            builder.append(compaction_strategy);
        boolean present_compaction_strategy_options = true && (isSetCompaction_strategy_options());
        builder.append(present_compaction_strategy_options);
        if (present_compaction_strategy_options)
            builder.append(compaction_strategy_options);
        boolean present_row_cache_keys_to_save = true && (isSetRow_cache_keys_to_save());
        builder.append(present_row_cache_keys_to_save);
        if (present_row_cache_keys_to_save)
            builder.append(row_cache_keys_to_save);
        boolean present_compression_options = true && (isSetCompression_options());
        builder.append(present_compression_options);
        if (present_compression_options)
            builder.append(compression_options);
        boolean present_bloom_filter_fp_chance = true && (isSetBloom_filter_fp_chance());
        builder.append(present_bloom_filter_fp_chance);
        if (present_bloom_filter_fp_chance)
            builder.append(bloom_filter_fp_chance);
=======
        boolean present_memtable_flush_after_mins = true && (isSetMemtable_flush_after_mins());
        builder.append(present_memtable_flush_after_mins);
        if (present_memtable_flush_after_mins)
            builder.append(memtable_flush_after_mins);
        boolean present_memtable_throughput_in_mb = true && (isSetMemtable_throughput_in_mb());
        builder.append(present_memtable_throughput_in_mb);
        if (present_memtable_throughput_in_mb)
            builder.append(memtable_throughput_in_mb);
        boolean present_memtable_operations_in_millions = true && (isSetMemtable_operations_in_millions());
        builder.append(present_memtable_operations_in_millions);
        if (present_memtable_operations_in_millions)
            builder.append(memtable_operations_in_millions);
        boolean present_replicate_on_write = true && (isSetReplicate_on_write());
        builder.append(present_replicate_on_write);
        if (present_replicate_on_write)
            builder.append(replicate_on_write);
        boolean present_merge_shards_chance = true && (isSetMerge_shards_chance());
        builder.append(present_merge_shards_chance);
        if (present_merge_shards_chance)
            builder.append(merge_shards_chance);
        boolean present_key_validation_class = true && (isSetKey_validation_class());
        builder.append(present_key_validation_class);
        if (present_key_validation_class)
            builder.append(key_validation_class);
        boolean present_row_cache_provider = true && (isSetRow_cache_provider());
        builder.append(present_row_cache_provider);
        if (present_row_cache_provider)
            builder.append(row_cache_provider);
        boolean present_key_alias = true && (isSetKey_alias());
        builder.append(present_key_alias);
        if (present_key_alias)
            builder.append(key_alias);
>>>>>>> YOURS
        return builder.toHashCode();
    }

    public int compareTo(CfDef other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        CfDef typedOther = (CfDef) other;
        lastComparison = Boolean.valueOf(isSetKeyspace()).compareTo(typedOther.isSetKeyspace());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetKeyspace()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.keyspace, typedOther.keyspace);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetName()).compareTo(typedOther.isSetName());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetName()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.name, typedOther.name);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetColumn_type()).compareTo(typedOther.isSetColumn_type());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetColumn_type()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.column_type, typedOther.column_type);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetComparator_type()).compareTo(typedOther.isSetComparator_type());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetComparator_type()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.comparator_type, typedOther.comparator_type);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetSubcomparator_type()).compareTo(typedOther.isSetSubcomparator_type());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetSubcomparator_type()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.subcomparator_type, typedOther.subcomparator_type);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetComment()).compareTo(typedOther.isSetComment());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetComment()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.comment, typedOther.comment);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetRow_cache_size()).compareTo(typedOther.isSetRow_cache_size());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetRow_cache_size()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.row_cache_size, typedOther.row_cache_size);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetKey_cache_size()).compareTo(typedOther.isSetKey_cache_size());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetKey_cache_size()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.key_cache_size, typedOther.key_cache_size);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetRead_repair_chance()).compareTo(typedOther.isSetRead_repair_chance());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetRead_repair_chance()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.read_repair_chance, typedOther.read_repair_chance);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetColumn_metadata()).compareTo(typedOther.isSetColumn_metadata());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetColumn_metadata()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.column_metadata, typedOther.column_metadata);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetGc_grace_seconds()).compareTo(typedOther.isSetGc_grace_seconds());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetGc_grace_seconds()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.gc_grace_seconds, typedOther.gc_grace_seconds);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetDefault_validation_class()).compareTo(typedOther.isSetDefault_validation_class());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetDefault_validation_class()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.default_validation_class, typedOther.default_validation_class);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetId()).compareTo(typedOther.isSetId());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetId()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.id, typedOther.id);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetMin_compaction_threshold()).compareTo(typedOther.isSetMin_compaction_threshold());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetMin_compaction_threshold()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.min_compaction_threshold, typedOther.min_compaction_threshold);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetMax_compaction_threshold()).compareTo(typedOther.isSetMax_compaction_threshold());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetMax_compaction_threshold()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.max_compaction_threshold, typedOther.max_compaction_threshold);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetRow_cache_save_period_in_seconds()).compareTo(typedOther.isSetRow_cache_save_period_in_seconds());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetRow_cache_save_period_in_seconds()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.row_cache_save_period_in_seconds, typedOther.row_cache_save_period_in_seconds);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetKey_cache_save_period_in_seconds()).compareTo(typedOther.isSetKey_cache_save_period_in_seconds());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetKey_cache_save_period_in_seconds()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.key_cache_save_period_in_seconds, typedOther.key_cache_save_period_in_seconds);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetReplicate_on_write()).compareTo(typedOther.isSetReplicate_on_write());
        if (lastComparison != 0) {
            return lastComparison;
        }
<<<<<<< MINE
        if (isSetReplicate_on_write()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.replicate_on_write, typedOther.replicate_on_write);
=======
        if (isSetMemtable_flush_after_mins()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.memtable_flush_after_mins, typedOther.memtable_flush_after_mins);
>>>>>>> YOURS
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetMerge_shards_chance()).compareTo(typedOther.isSetMerge_shards_chance());
        if (lastComparison != 0) {
            return lastComparison;
        }
<<<<<<< MINE
        if (isSetMerge_shards_chance()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.merge_shards_chance, typedOther.merge_shards_chance);
=======
        if (isSetMemtable_throughput_in_mb()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.memtable_throughput_in_mb, typedOther.memtable_throughput_in_mb);
>>>>>>> YOURS
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetKey_validation_class()).compareTo(typedOther.isSetKey_validation_class());
        if (lastComparison != 0) {
            return lastComparison;
        }
<<<<<<< MINE
        if (isSetKey_validation_class()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.key_validation_class, typedOther.key_validation_class);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetRow_cache_provider()).compareTo(typedOther.isSetRow_cache_provider());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetRow_cache_provider()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.row_cache_provider, typedOther.row_cache_provider);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetKey_alias()).compareTo(typedOther.isSetKey_alias());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetKey_alias()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.key_alias, typedOther.key_alias);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetCompaction_strategy()).compareTo(typedOther.isSetCompaction_strategy());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetCompaction_strategy()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.compaction_strategy, typedOther.compaction_strategy);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetCompaction_strategy_options()).compareTo(typedOther.isSetCompaction_strategy_options());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetCompaction_strategy_options()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.compaction_strategy_options, typedOther.compaction_strategy_options);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetRow_cache_keys_to_save()).compareTo(typedOther.isSetRow_cache_keys_to_save());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetRow_cache_keys_to_save()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.row_cache_keys_to_save, typedOther.row_cache_keys_to_save);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetCompression_options()).compareTo(typedOther.isSetCompression_options());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetCompression_options()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.compression_options, typedOther.compression_options);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetBloom_filter_fp_chance()).compareTo(typedOther.isSetBloom_filter_fp_chance());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetBloom_filter_fp_chance()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.bloom_filter_fp_chance, typedOther.bloom_filter_fp_chance);
=======
        if (isSetMemtable_operations_in_millions()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.memtable_operations_in_millions, typedOther.memtable_operations_in_millions);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetReplicate_on_write()).compareTo(typedOther.isSetReplicate_on_write());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetReplicate_on_write()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.replicate_on_write, typedOther.replicate_on_write);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetMerge_shards_chance()).compareTo(typedOther.isSetMerge_shards_chance());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetMerge_shards_chance()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.merge_shards_chance, typedOther.merge_shards_chance);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetKey_validation_class()).compareTo(typedOther.isSetKey_validation_class());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetKey_validation_class()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.key_validation_class, typedOther.key_validation_class);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetRow_cache_provider()).compareTo(typedOther.isSetRow_cache_provider());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetRow_cache_provider()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.row_cache_provider, typedOther.row_cache_provider);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetKey_alias()).compareTo(typedOther.isSetKey_alias());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetKey_alias()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.key_alias, typedOther.key_alias);
>>>>>>> YOURS
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        return 0;
    }

    public _Fields fieldForId(int fieldId) {
        return _Fields.findByThriftId(fieldId);
    }

    public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
        org.apache.thrift.protocol.TField field;
        iprot.readStructBegin();
        while (true) {
            field = iprot.readFieldBegin();
            if (field.type == org.apache.thrift.protocol.TType.STOP) {
                break;
            }
            switch(field.id) {
                case 1:
                    if (field.type == org.apache.thrift.protocol.TType.STRING) {
                        this.keyspace = iprot.readString();
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 2:
                    if (field.type == org.apache.thrift.protocol.TType.STRING) {
                        this.name = iprot.readString();
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 3:
                    if (field.type == org.apache.thrift.protocol.TType.STRING) {
                        this.column_type = iprot.readString();
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 5:
                    if (field.type == org.apache.thrift.protocol.TType.STRING) {
                        this.comparator_type = iprot.readString();
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 6:
                    if (field.type == org.apache.thrift.protocol.TType.STRING) {
                        this.subcomparator_type = iprot.readString();
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 8:
                    if (field.type == org.apache.thrift.protocol.TType.STRING) {
                        this.comment = iprot.readString();
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 9:
                    if (field.type == org.apache.thrift.protocol.TType.DOUBLE) {
                        this.row_cache_size = iprot.readDouble();
                        setRow_cache_sizeIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 11:
                    if (field.type == org.apache.thrift.protocol.TType.DOUBLE) {
                        this.key_cache_size = iprot.readDouble();
                        setKey_cache_sizeIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 12:
                    if (field.type == org.apache.thrift.protocol.TType.DOUBLE) {
                        this.read_repair_chance = iprot.readDouble();
                        setRead_repair_chanceIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 13:
                    if (field.type == org.apache.thrift.protocol.TType.LIST) {
                        {
<<<<<<< MINE
                            org.apache.thrift.protocol.TList _list42 = iprot.readListBegin();
                            this.column_metadata = new ArrayList<ColumnDef>(_list42.size);
                            for (int _i43 = 0; _i43 < _list42.size; ++_i43) {
                                ColumnDef _elem44;
                                _elem44 = new ColumnDef();
                                _elem44.read(iprot);
                                this.column_metadata.add(_elem44);
                            }
                            iprot.readListEnd();
                        }
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 14:
                    if (field.type == org.apache.thrift.protocol.TType.I32) {
                        this.gc_grace_seconds = iprot.readI32();
                        setGc_grace_secondsIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 15:
                    if (field.type == org.apache.thrift.protocol.TType.STRING) {
                        this.default_validation_class = iprot.readString();
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 16:
                    if (field.type == org.apache.thrift.protocol.TType.I32) {
                        this.id = iprot.readI32();
                        setIdIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 17:
                    if (field.type == org.apache.thrift.protocol.TType.I32) {
                        this.min_compaction_threshold = iprot.readI32();
                        setMin_compaction_thresholdIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 18:
                    if (field.type == org.apache.thrift.protocol.TType.I32) {
                        this.max_compaction_threshold = iprot.readI32();
                        setMax_compaction_thresholdIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 19:
                    if (field.type == org.apache.thrift.protocol.TType.I32) {
                        this.row_cache_save_period_in_seconds = iprot.readI32();
                        setRow_cache_save_period_in_secondsIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 20:
                    if (field.type == org.apache.thrift.protocol.TType.I32) {
                        this.key_cache_save_period_in_seconds = iprot.readI32();
                        setKey_cache_save_period_in_secondsIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 24:
                    if (field.type == org.apache.thrift.protocol.TType.BOOL) {
                        this.replicate_on_write = iprot.readBool();
                        setReplicate_on_writeIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 25:
                    if (field.type == org.apache.thrift.protocol.TType.DOUBLE) {
                        this.merge_shards_chance = iprot.readDouble();
                        setMerge_shards_chanceIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 26:
                    if (field.type == org.apache.thrift.protocol.TType.STRING) {
                        this.key_validation_class = iprot.readString();
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 27:
                    if (field.type == org.apache.thrift.protocol.TType.STRING) {
                        this.row_cache_provider = iprot.readString();
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 28:
                    if (field.type == org.apache.thrift.protocol.TType.STRING) {
                        this.key_alias = iprot.readBinary();
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 29:
                    if (field.type == org.apache.thrift.protocol.TType.STRING) {
                        this.compaction_strategy = iprot.readString();
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 30:
                    if (field.type == org.apache.thrift.protocol.TType.MAP) {
                        {
                            org.apache.thrift.protocol.TMap _map45 = iprot.readMapBegin();
                            this.compaction_strategy_options = new HashMap<String, String>(2 * _map45.size);
                            for (int _i46 = 0; _i46 < _map45.size; ++_i46) {
                                String _key47;
                                String _val48;
                                _key47 = iprot.readString();
                                _val48 = iprot.readString();
                                this.compaction_strategy_options.put(_key47, _val48);
                            }
                            iprot.readMapEnd();
                        }
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 31:
                    if (field.type == org.apache.thrift.protocol.TType.I32) {
                        this.row_cache_keys_to_save = iprot.readI32();
                        setRow_cache_keys_to_saveIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 32:
                    if (field.type == org.apache.thrift.protocol.TType.MAP) {
                        {
                            org.apache.thrift.protocol.TMap _map49 = iprot.readMapBegin();
                            this.compression_options = new HashMap<String, String>(2 * _map49.size);
                            for (int _i50 = 0; _i50 < _map49.size; ++_i50) {
                                String _key51;
                                String _val52;
                                _key51 = iprot.readString();
                                _val52 = iprot.readString();
                                this.compression_options.put(_key51, _val52);
                            }
                            iprot.readMapEnd();
                        }
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 33:
                    if (field.type == org.apache.thrift.protocol.TType.DOUBLE) {
                        this.bloom_filter_fp_chance = iprot.readDouble();
                        setBloom_filter_fp_chanceIsSet(true);
=======
                            org.apache.thrift.protocol.TList _list33 = iprot.readListBegin();
                            this.column_metadata = new ArrayList<ColumnDef>(_list33.size);
                            for (int _i34 = 0; _i34 < _list33.size; ++_i34) {
                                ColumnDef _elem35;
                                _elem35 = new ColumnDef();
                                _elem35.read(iprot);
                                this.column_metadata.add(_elem35);
                            }
                            iprot.readListEnd();
                        }
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 14:
                    if (field.type == org.apache.thrift.protocol.TType.I32) {
                        this.gc_grace_seconds = iprot.readI32();
                        setGc_grace_secondsIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 15:
                    if (field.type == org.apache.thrift.protocol.TType.STRING) {
                        this.default_validation_class = iprot.readString();
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 16:
                    if (field.type == org.apache.thrift.protocol.TType.I32) {
                        this.id = iprot.readI32();
                        setIdIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 17:
                    if (field.type == org.apache.thrift.protocol.TType.I32) {
                        this.min_compaction_threshold = iprot.readI32();
                        setMin_compaction_thresholdIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 18:
                    if (field.type == org.apache.thrift.protocol.TType.I32) {
                        this.max_compaction_threshold = iprot.readI32();
                        setMax_compaction_thresholdIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 19:
                    if (field.type == org.apache.thrift.protocol.TType.I32) {
                        this.row_cache_save_period_in_seconds = iprot.readI32();
                        setRow_cache_save_period_in_secondsIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 20:
                    if (field.type == org.apache.thrift.protocol.TType.I32) {
                        this.key_cache_save_period_in_seconds = iprot.readI32();
                        setKey_cache_save_period_in_secondsIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 21:
                    if (field.type == org.apache.thrift.protocol.TType.I32) {
                        this.memtable_flush_after_mins = iprot.readI32();
                        setMemtable_flush_after_minsIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 22:
                    if (field.type == org.apache.thrift.protocol.TType.I32) {
                        this.memtable_throughput_in_mb = iprot.readI32();
                        setMemtable_throughput_in_mbIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 23:
                    if (field.type == org.apache.thrift.protocol.TType.DOUBLE) {
                        this.memtable_operations_in_millions = iprot.readDouble();
                        setMemtable_operations_in_millionsIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 24:
                    if (field.type == org.apache.thrift.protocol.TType.BOOL) {
                        this.replicate_on_write = iprot.readBool();
                        setReplicate_on_writeIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 25:
                    if (field.type == org.apache.thrift.protocol.TType.DOUBLE) {
                        this.merge_shards_chance = iprot.readDouble();
                        setMerge_shards_chanceIsSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 26:
                    if (field.type == org.apache.thrift.protocol.TType.STRING) {
                        this.key_validation_class = iprot.readString();
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 27:
                    if (field.type == org.apache.thrift.protocol.TType.STRING) {
                        this.row_cache_provider = iprot.readString();
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 28:
                    if (field.type == org.apache.thrift.protocol.TType.STRING) {
                        this.key_alias = iprot.readBinary();
>>>>>>> YOURS
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                default:
                    org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            }
            iprot.readFieldEnd();
        }
        iprot.readStructEnd();
        validate();
    }

<<<<<<< MINE

=======
    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
        validate();
        oprot.writeStructBegin(STRUCT_DESC);
        if (this.keyspace != null) {
            oprot.writeFieldBegin(KEYSPACE_FIELD_DESC);
            oprot.writeString(this.keyspace);
            oprot.writeFieldEnd();
        }
        if (this.name != null) {
            oprot.writeFieldBegin(NAME_FIELD_DESC);
            oprot.writeString(this.name);
            oprot.writeFieldEnd();
        }
        if (this.column_type != null) {
            if (isSetColumn_type()) {
                oprot.writeFieldBegin(COLUMN_TYPE_FIELD_DESC);
                oprot.writeString(this.column_type);
                oprot.writeFieldEnd();
            }
        }
        if (this.comparator_type != null) {
            if (isSetComparator_type()) {
                oprot.writeFieldBegin(COMPARATOR_TYPE_FIELD_DESC);
                oprot.writeString(this.comparator_type);
                oprot.writeFieldEnd();
            }
        }
        if (this.subcomparator_type != null) {
            if (isSetSubcomparator_type()) {
                oprot.writeFieldBegin(SUBCOMPARATOR_TYPE_FIELD_DESC);
                oprot.writeString(this.subcomparator_type);
                oprot.writeFieldEnd();
            }
        }
        if (this.comment != null) {
            if (isSetComment()) {
                oprot.writeFieldBegin(COMMENT_FIELD_DESC);
                oprot.writeString(this.comment);
                oprot.writeFieldEnd();
            }
        }
        if (isSetRow_cache_size()) {
            oprot.writeFieldBegin(ROW_CACHE_SIZE_FIELD_DESC);
            oprot.writeDouble(this.row_cache_size);
            oprot.writeFieldEnd();
        }
        if (isSetKey_cache_size()) {
            oprot.writeFieldBegin(KEY_CACHE_SIZE_FIELD_DESC);
            oprot.writeDouble(this.key_cache_size);
            oprot.writeFieldEnd();
        }
        if (isSetRead_repair_chance()) {
            oprot.writeFieldBegin(READ_REPAIR_CHANCE_FIELD_DESC);
            oprot.writeDouble(this.read_repair_chance);
            oprot.writeFieldEnd();
        }
        if (this.column_metadata != null) {
            if (isSetColumn_metadata()) {
                oprot.writeFieldBegin(COLUMN_METADATA_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, this.column_metadata.size()));
                    for (ColumnDef _iter36 : this.column_metadata) {
                        _iter36.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
        }
        if (isSetGc_grace_seconds()) {
            oprot.writeFieldBegin(GC_GRACE_SECONDS_FIELD_DESC);
            oprot.writeI32(this.gc_grace_seconds);
            oprot.writeFieldEnd();
        }
        if (this.default_validation_class != null) {
            if (isSetDefault_validation_class()) {
                oprot.writeFieldBegin(DEFAULT_VALIDATION_CLASS_FIELD_DESC);
                oprot.writeString(this.default_validation_class);
                oprot.writeFieldEnd();
            }
        }
        if (isSetId()) {
            oprot.writeFieldBegin(ID_FIELD_DESC);
            oprot.writeI32(this.id);
            oprot.writeFieldEnd();
        }
        if (isSetMin_compaction_threshold()) {
            oprot.writeFieldBegin(MIN_COMPACTION_THRESHOLD_FIELD_DESC);
            oprot.writeI32(this.min_compaction_threshold);
            oprot.writeFieldEnd();
        }
        if (isSetMax_compaction_threshold()) {
            oprot.writeFieldBegin(MAX_COMPACTION_THRESHOLD_FIELD_DESC);
            oprot.writeI32(this.max_compaction_threshold);
            oprot.writeFieldEnd();
        }
        if (isSetRow_cache_save_period_in_seconds()) {
            oprot.writeFieldBegin(ROW_CACHE_SAVE_PERIOD_IN_SECONDS_FIELD_DESC);
            oprot.writeI32(this.row_cache_save_period_in_seconds);
            oprot.writeFieldEnd();
        }
        if (isSetKey_cache_save_period_in_seconds()) {
            oprot.writeFieldBegin(KEY_CACHE_SAVE_PERIOD_IN_SECONDS_FIELD_DESC);
            oprot.writeI32(this.key_cache_save_period_in_seconds);
            oprot.writeFieldEnd();
        }
        if (isSetMemtable_flush_after_mins()) {
            oprot.writeFieldBegin(MEMTABLE_FLUSH_AFTER_MINS_FIELD_DESC);
            oprot.writeI32(this.memtable_flush_after_mins);
            oprot.writeFieldEnd();
        }
        if (isSetMemtable_throughput_in_mb()) {
            oprot.writeFieldBegin(MEMTABLE_THROUGHPUT_IN_MB_FIELD_DESC);
            oprot.writeI32(this.memtable_throughput_in_mb);
            oprot.writeFieldEnd();
        }
        if (isSetMemtable_operations_in_millions()) {
            oprot.writeFieldBegin(MEMTABLE_OPERATIONS_IN_MILLIONS_FIELD_DESC);
            oprot.writeDouble(this.memtable_operations_in_millions);
            oprot.writeFieldEnd();
        }
        if (isSetReplicate_on_write()) {
            oprot.writeFieldBegin(REPLICATE_ON_WRITE_FIELD_DESC);
            oprot.writeBool(this.replicate_on_write);
            oprot.writeFieldEnd();
        }
        if (isSetMerge_shards_chance()) {
            oprot.writeFieldBegin(MERGE_SHARDS_CHANCE_FIELD_DESC);
            oprot.writeDouble(this.merge_shards_chance);
            oprot.writeFieldEnd();
        }
        if (this.key_validation_class != null) {
            if (isSetKey_validation_class()) {
                oprot.writeFieldBegin(KEY_VALIDATION_CLASS_FIELD_DESC);
                oprot.writeString(this.key_validation_class);
                oprot.writeFieldEnd();
            }
        }
        if (this.row_cache_provider != null) {
            if (isSetRow_cache_provider()) {
                oprot.writeFieldBegin(ROW_CACHE_PROVIDER_FIELD_DESC);
                oprot.writeString(this.row_cache_provider);
                oprot.writeFieldEnd();
            }
        }
        if (this.key_alias != null) {
            if (isSetKey_alias()) {
                oprot.writeFieldBegin(KEY_ALIAS_FIELD_DESC);
                oprot.writeBinary(this.key_alias);
                oprot.writeFieldEnd();
            }
        }
        oprot.writeFieldStop();
        oprot.writeStructEnd();
    }
>>>>>>> YOURS

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CfDef(");
        boolean first = true;
        sb.append("keyspace:");
        if (this.keyspace == null) {
            sb.append("null");
        } else {
            sb.append(this.keyspace);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("name:");
        if (this.name == null) {
            sb.append("null");
        } else {
            sb.append(this.name);
        }
        first = false;
        if (isSetColumn_type()) {
            if (!first)
                sb.append(", ");
            sb.append("column_type:");
            if (this.column_type == null) {
                sb.append("null");
            } else {
                sb.append(this.column_type);
            }
            first = false;
        }
        if (isSetComparator_type()) {
            if (!first)
                sb.append(", ");
            sb.append("comparator_type:");
            if (this.comparator_type == null) {
                sb.append("null");
            } else {
                sb.append(this.comparator_type);
            }
            first = false;
        }
        if (isSetSubcomparator_type()) {
            if (!first)
                sb.append(", ");
            sb.append("subcomparator_type:");
            if (this.subcomparator_type == null) {
                sb.append("null");
            } else {
                sb.append(this.subcomparator_type);
            }
            first = false;
        }
        if (isSetComment()) {
            if (!first)
                sb.append(", ");
            sb.append("comment:");
            if (this.comment == null) {
                sb.append("null");
            } else {
                sb.append(this.comment);
            }
            first = false;
        }
        if (isSetRow_cache_size()) {
            if (!first)
                sb.append(", ");
            sb.append("row_cache_size:");
            sb.append(this.row_cache_size);
            first = false;
        }
        if (isSetKey_cache_size()) {
            if (!first)
                sb.append(", ");
            sb.append("key_cache_size:");
            sb.append(this.key_cache_size);
            first = false;
        }
        if (isSetRead_repair_chance()) {
            if (!first)
                sb.append(", ");
            sb.append("read_repair_chance:");
            sb.append(this.read_repair_chance);
            first = false;
        }
        if (isSetColumn_metadata()) {
            if (!first)
                sb.append(", ");
            sb.append("column_metadata:");
            if (this.column_metadata == null) {
                sb.append("null");
            } else {
                sb.append(this.column_metadata);
            }
            first = false;
        }
        if (isSetGc_grace_seconds()) {
            if (!first)
                sb.append(", ");
            sb.append("gc_grace_seconds:");
            sb.append(this.gc_grace_seconds);
            first = false;
        }
        if (isSetDefault_validation_class()) {
            if (!first)
                sb.append(", ");
            sb.append("default_validation_class:");
            if (this.default_validation_class == null) {
                sb.append("null");
            } else {
                sb.append(this.default_validation_class);
            }
            first = false;
        }
        if (isSetId()) {
            if (!first)
                sb.append(", ");
            sb.append("id:");
            sb.append(this.id);
            first = false;
        }
        if (isSetMin_compaction_threshold()) {
            if (!first)
                sb.append(", ");
            sb.append("min_compaction_threshold:");
            sb.append(this.min_compaction_threshold);
            first = false;
        }
        if (isSetMax_compaction_threshold()) {
            if (!first)
                sb.append(", ");
            sb.append("max_compaction_threshold:");
            sb.append(this.max_compaction_threshold);
            first = false;
        }
        if (isSetRow_cache_save_period_in_seconds()) {
            if (!first)
                sb.append(", ");
            sb.append("row_cache_save_period_in_seconds:");
            sb.append(this.row_cache_save_period_in_seconds);
            first = false;
        }
        if (isSetKey_cache_save_period_in_seconds()) {
            if (!first)
                sb.append(", ");
            sb.append("key_cache_save_period_in_seconds:");
            sb.append(this.key_cache_save_period_in_seconds);
            first = false;
        }
        if (isSetReplicate_on_write()) {
            if (!first)
                sb.append(", ");
            sb.append("replicate_on_write:");
            sb.append(this.replicate_on_write);
            first = false;
        }
        if (isSetMerge_shards_chance()) {
            if (!first)
                sb.append(", ");
            sb.append("merge_shards_chance:");
            sb.append(this.merge_shards_chance);
            first = false;
        }
        if (isSetKey_validation_class()) {
            if (!first)
                sb.append(", ");
            sb.append("key_validation_class:");
            if (this.key_validation_class == null) {
                sb.append("null");
            } else {
                sb.append(this.key_validation_class);
            }
            first = false;
        }
        if (isSetRow_cache_provider()) {
            if (!first)
                sb.append(", ");
            sb.append("row_cache_provider:");
            if (this.row_cache_provider == null) {
                sb.append("null");
            } else {
                sb.append(this.row_cache_provider);
            }
            first = false;
        }
        if (isSetKey_alias()) {
            if (!first)
                sb.append(", ");
            sb.append("key_alias:");
            if (this.key_alias == null) {
                sb.append("null");
            } else {
                org.apache.thrift.TBaseHelper.toString(this.key_alias, sb);
            }
            first = false;
        }
        if (isSetCompaction_strategy()) {
            if (!first)
                sb.append(", ");
            sb.append("compaction_strategy:");
            if (this.compaction_strategy == null) {
                sb.append("null");
            } else {
                sb.append(this.compaction_strategy);
            }
            first = false;
        }
        if (isSetCompaction_strategy_options()) {
            if (!first)
                sb.append(", ");
            sb.append("compaction_strategy_options:");
            if (this.compaction_strategy_options == null) {
                sb.append("null");
            } else {
                sb.append(this.compaction_strategy_options);
            }
            first = false;
        }
        if (isSetRow_cache_keys_to_save()) {
            if (!first)
                sb.append(", ");
            sb.append("row_cache_keys_to_save:");
            sb.append(this.row_cache_keys_to_save);
            first = false;
        }
        if (isSetCompression_options()) {
            if (!first)
                sb.append(", ");
            sb.append("compression_options:");
            if (this.compression_options == null) {
                sb.append("null");
            } else {
                sb.append(this.compression_options);
            }
            first = false;
        }
        if (isSetBloom_filter_fp_chance()) {
            if (!first)
                sb.append(", ");
            sb.append("bloom_filter_fp_chance:");
            sb.append(this.bloom_filter_fp_chance);
            first = false;
        }
        if (isSetReplicate_on_write()) {
            if (!first)
                sb.append(", ");
            sb.append("replicate_on_write:");
            sb.append(this.replicate_on_write);
            first = false;
        }
        if (isSetMerge_shards_chance()) {
            if (!first)
                sb.append(", ");
            sb.append("merge_shards_chance:");
            sb.append(this.merge_shards_chance);
            first = false;
        }
        if (isSetKey_validation_class()) {
            if (!first)
                sb.append(", ");
            sb.append("key_validation_class:");
            if (this.key_validation_class == null) {
                sb.append("null");
            } else {
                sb.append(this.key_validation_class);
            }
            first = false;
        }
        if (isSetRow_cache_provider()) {
            if (!first)
                sb.append(", ");
            sb.append("row_cache_provider:");
            if (this.row_cache_provider == null) {
                sb.append("null");
            } else {
                sb.append(this.row_cache_provider);
            }
            first = false;
        }
        if (isSetKey_alias()) {
            if (!first)
                sb.append(", ");
            sb.append("key_alias:");
            if (this.key_alias == null) {
                sb.append("null");
            } else {
                org.apache.thrift.TBaseHelper.toString(this.key_alias, sb);
            }
            first = false;
        }
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
        if (keyspace == null) {
            throw new org.apache.thrift.protocol.TProtocolException("Required field 'keyspace' was not present! Struct: " + toString());
        }
        if (name == null) {
            throw new org.apache.thrift.protocol.TProtocolException("Required field 'name' was not present! Struct: " + toString());
        }
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        try {
            write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        try {
            __isset_bit_vector = new BitSet(1);
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }
}