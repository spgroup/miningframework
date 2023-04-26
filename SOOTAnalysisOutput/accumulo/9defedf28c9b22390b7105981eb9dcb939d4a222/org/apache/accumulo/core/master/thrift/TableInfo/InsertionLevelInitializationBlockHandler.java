package org.apache.accumulo.core.master.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;
import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
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

@SuppressWarnings("all")
public class TableInfo implements org.apache.thrift.TBase<TableInfo, TableInfo._Fields>, java.io.Serializable, Cloneable, Comparable<TableInfo> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TableInfo");

    private static final org.apache.thrift.protocol.TField RECS_FIELD_DESC = new org.apache.thrift.protocol.TField("recs", org.apache.thrift.protocol.TType.I64, (short) 1);

    private static final org.apache.thrift.protocol.TField RECS_IN_MEMORY_FIELD_DESC = new org.apache.thrift.protocol.TField("recsInMemory", org.apache.thrift.protocol.TType.I64, (short) 2);

    private static final org.apache.thrift.protocol.TField TABLETS_FIELD_DESC = new org.apache.thrift.protocol.TField("tablets", org.apache.thrift.protocol.TType.I32, (short) 3);

    private static final org.apache.thrift.protocol.TField ONLINE_TABLETS_FIELD_DESC = new org.apache.thrift.protocol.TField("onlineTablets", org.apache.thrift.protocol.TType.I32, (short) 4);

    private static final org.apache.thrift.protocol.TField INGEST_RATE_FIELD_DESC = new org.apache.thrift.protocol.TField("ingestRate", org.apache.thrift.protocol.TType.DOUBLE, (short) 5);

    private static final org.apache.thrift.protocol.TField INGEST_BYTE_RATE_FIELD_DESC = new org.apache.thrift.protocol.TField("ingestByteRate", org.apache.thrift.protocol.TType.DOUBLE, (short) 6);

    private static final org.apache.thrift.protocol.TField QUERY_RATE_FIELD_DESC = new org.apache.thrift.protocol.TField("queryRate", org.apache.thrift.protocol.TType.DOUBLE, (short) 7);

    private static final org.apache.thrift.protocol.TField QUERY_BYTE_RATE_FIELD_DESC = new org.apache.thrift.protocol.TField("queryByteRate", org.apache.thrift.protocol.TType.DOUBLE, (short) 8);

    private static final org.apache.thrift.protocol.TField MINORS_FIELD_DESC = new org.apache.thrift.protocol.TField("minors", org.apache.thrift.protocol.TType.STRUCT, (short) 9);

    private static final org.apache.thrift.protocol.TField MAJORS_FIELD_DESC = new org.apache.thrift.protocol.TField("majors", org.apache.thrift.protocol.TType.STRUCT, (short) 10);

    private static final org.apache.thrift.protocol.TField SCANS_FIELD_DESC = new org.apache.thrift.protocol.TField("scans", org.apache.thrift.protocol.TType.STRUCT, (short) 11);

    private static final org.apache.thrift.protocol.TField SCAN_RATE_FIELD_DESC = new org.apache.thrift.protocol.TField("scanRate", org.apache.thrift.protocol.TType.DOUBLE, (short) 12);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new TableInfoStandardSchemeFactory());
        schemes.put(TupleScheme.class, new TableInfoTupleSchemeFactory());
    }

    public long recs;

    public long recsInMemory;

    public int tablets;

    public int onlineTablets;

    public double ingestRate;

    public double ingestByteRate;

    public double queryRate;

    public double queryByteRate;

    public Compacting minors;

    public Compacting majors;

    public Compacting scans;

    public double scanRate;

    @SuppressWarnings("all")
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        RECS((short) 1, "recs"),
        RECS_IN_MEMORY((short) 2, "recsInMemory"),
        TABLETS((short) 3, "tablets"),
        ONLINE_TABLETS((short) 4, "onlineTablets"),
        INGEST_RATE((short) 5, "ingestRate"),
        INGEST_BYTE_RATE((short) 6, "ingestByteRate"),
        QUERY_RATE((short) 7, "queryRate"),
        QUERY_BYTE_RATE((short) 8, "queryByteRate"),
        MINORS((short) 9, "minors"),
        MAJORS((short) 10, "majors"),
        SCANS((short) 11, "scans"),
        SCAN_RATE((short) 12, "scanRate");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return RECS;
                case 2:
                    return RECS_IN_MEMORY;
                case 3:
                    return TABLETS;
                case 4:
                    return ONLINE_TABLETS;
                case 5:
                    return INGEST_RATE;
                case 6:
                    return INGEST_BYTE_RATE;
                case 7:
                    return QUERY_RATE;
                case 8:
                    return QUERY_BYTE_RATE;
                case 9:
                    return MINORS;
                case 10:
                    return MAJORS;
                case 11:
                    return SCANS;
                case 12:
                    return SCAN_RATE;
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

    private static final int __RECS_ISSET_ID = 0;

    private static final int __RECSINMEMORY_ISSET_ID = 1;

    private static final int __TABLETS_ISSET_ID = 2;

    private static final int __ONLINETABLETS_ISSET_ID = 3;

    private static final int __INGESTRATE_ISSET_ID = 4;

    private static final int __INGESTBYTERATE_ISSET_ID = 5;

    private static final int __QUERYRATE_ISSET_ID = 6;

    private static final int __QUERYBYTERATE_ISSET_ID = 7;

    private static final int __SCANRATE_ISSET_ID = 8;

    private short __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.RECS, new org.apache.thrift.meta_data.FieldMetaData("recs", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.RECS_IN_MEMORY, new org.apache.thrift.meta_data.FieldMetaData("recsInMemory", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.TABLETS, new org.apache.thrift.meta_data.FieldMetaData("tablets", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.ONLINE_TABLETS, new org.apache.thrift.meta_data.FieldMetaData("onlineTablets", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.INGEST_RATE, new org.apache.thrift.meta_data.FieldMetaData("ingestRate", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.INGEST_BYTE_RATE, new org.apache.thrift.meta_data.FieldMetaData("ingestByteRate", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.QUERY_RATE, new org.apache.thrift.meta_data.FieldMetaData("queryRate", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.QUERY_BYTE_RATE, new org.apache.thrift.meta_data.FieldMetaData("queryByteRate", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.MINORS, new org.apache.thrift.meta_data.FieldMetaData("minors", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Compacting.class)));
        tmpMap.put(_Fields.MAJORS, new org.apache.thrift.meta_data.FieldMetaData("majors", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Compacting.class)));
        tmpMap.put(_Fields.SCANS, new org.apache.thrift.meta_data.FieldMetaData("scans", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Compacting.class)));
        tmpMap.put(_Fields.SCAN_RATE, new org.apache.thrift.meta_data.FieldMetaData("scanRate", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TableInfo.class, metaDataMap);
    }

    public TableInfo() {
    }

    public TableInfo(long recs, long recsInMemory, int tablets, int onlineTablets, double ingestRate, double ingestByteRate, double queryRate, double queryByteRate, Compacting minors, Compacting majors, Compacting scans, double scanRate) {
        this();
        this.recs = recs;
        setRecsIsSet(true);
        this.recsInMemory = recsInMemory;
        setRecsInMemoryIsSet(true);
        this.tablets = tablets;
        setTabletsIsSet(true);
        this.onlineTablets = onlineTablets;
        setOnlineTabletsIsSet(true);
        this.ingestRate = ingestRate;
        setIngestRateIsSet(true);
        this.ingestByteRate = ingestByteRate;
        setIngestByteRateIsSet(true);
        this.queryRate = queryRate;
        setQueryRateIsSet(true);
        this.queryByteRate = queryByteRate;
        setQueryByteRateIsSet(true);
        this.minors = minors;
        this.majors = majors;
        this.scans = scans;
        this.scanRate = scanRate;
        setScanRateIsSet(true);
    }

    public TableInfo(TableInfo other) {
        __isset_bitfield = other.__isset_bitfield;
        this.recs = other.recs;
        this.recsInMemory = other.recsInMemory;
        this.tablets = other.tablets;
        this.onlineTablets = other.onlineTablets;
        this.ingestRate = other.ingestRate;
        this.ingestByteRate = other.ingestByteRate;
        this.queryRate = other.queryRate;
        this.queryByteRate = other.queryByteRate;
        if (other.isSetMinors()) {
            this.minors = new Compacting(other.minors);
        }
        if (other.isSetMajors()) {
            this.majors = new Compacting(other.majors);
        }
        if (other.isSetScans()) {
            this.scans = new Compacting(other.scans);
        }
        this.scanRate = other.scanRate;
    }

    public TableInfo deepCopy() {
        return new TableInfo(this);
    }

    @Override
    public void clear() {
        setRecsIsSet(false);
        this.recs = 0;
        setRecsInMemoryIsSet(false);
        this.recsInMemory = 0;
        setTabletsIsSet(false);
        this.tablets = 0;
        setOnlineTabletsIsSet(false);
        this.onlineTablets = 0;
        setIngestRateIsSet(false);
        this.ingestRate = 0.0;
        setIngestByteRateIsSet(false);
        this.ingestByteRate = 0.0;
        setQueryRateIsSet(false);
        this.queryRate = 0.0;
        setQueryByteRateIsSet(false);
        this.queryByteRate = 0.0;
        this.minors = null;
        this.majors = null;
        this.scans = null;
        setScanRateIsSet(false);
        this.scanRate = 0.0;
    }

    public long getRecs() {
        return this.recs;
    }

    public TableInfo setRecs(long recs) {
        this.recs = recs;
        setRecsIsSet(true);
        return this;
    }

    public void unsetRecs() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __RECS_ISSET_ID);
    }

    public boolean isSetRecs() {
        return EncodingUtils.testBit(__isset_bitfield, __RECS_ISSET_ID);
    }

    public void setRecsIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __RECS_ISSET_ID, value);
    }

    public long getRecsInMemory() {
        return this.recsInMemory;
    }

    public TableInfo setRecsInMemory(long recsInMemory) {
        this.recsInMemory = recsInMemory;
        setRecsInMemoryIsSet(true);
        return this;
    }

    public void unsetRecsInMemory() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __RECSINMEMORY_ISSET_ID);
    }

    public boolean isSetRecsInMemory() {
        return EncodingUtils.testBit(__isset_bitfield, __RECSINMEMORY_ISSET_ID);
    }

    public void setRecsInMemoryIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __RECSINMEMORY_ISSET_ID, value);
    }

    public int getTablets() {
        return this.tablets;
    }

    public TableInfo setTablets(int tablets) {
        this.tablets = tablets;
        setTabletsIsSet(true);
        return this;
    }

    public void unsetTablets() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __TABLETS_ISSET_ID);
    }

    public boolean isSetTablets() {
        return EncodingUtils.testBit(__isset_bitfield, __TABLETS_ISSET_ID);
    }

    public void setTabletsIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __TABLETS_ISSET_ID, value);
    }

    public int getOnlineTablets() {
        return this.onlineTablets;
    }

    public TableInfo setOnlineTablets(int onlineTablets) {
        this.onlineTablets = onlineTablets;
        setOnlineTabletsIsSet(true);
        return this;
    }

    public void unsetOnlineTablets() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __ONLINETABLETS_ISSET_ID);
    }

    public boolean isSetOnlineTablets() {
        return EncodingUtils.testBit(__isset_bitfield, __ONLINETABLETS_ISSET_ID);
    }

    public void setOnlineTabletsIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __ONLINETABLETS_ISSET_ID, value);
    }

    public double getIngestRate() {
        return this.ingestRate;
    }

    public TableInfo setIngestRate(double ingestRate) {
        this.ingestRate = ingestRate;
        setIngestRateIsSet(true);
        return this;
    }

    public void unsetIngestRate() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __INGESTRATE_ISSET_ID);
    }

    public boolean isSetIngestRate() {
        return EncodingUtils.testBit(__isset_bitfield, __INGESTRATE_ISSET_ID);
    }

    public void setIngestRateIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __INGESTRATE_ISSET_ID, value);
    }

    public double getIngestByteRate() {
        return this.ingestByteRate;
    }

    public TableInfo setIngestByteRate(double ingestByteRate) {
        this.ingestByteRate = ingestByteRate;
        setIngestByteRateIsSet(true);
        return this;
    }

    public void unsetIngestByteRate() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __INGESTBYTERATE_ISSET_ID);
    }

    public boolean isSetIngestByteRate() {
        return EncodingUtils.testBit(__isset_bitfield, __INGESTBYTERATE_ISSET_ID);
    }

    public void setIngestByteRateIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __INGESTBYTERATE_ISSET_ID, value);
    }

    public double getQueryRate() {
        return this.queryRate;
    }

    public TableInfo setQueryRate(double queryRate) {
        this.queryRate = queryRate;
        setQueryRateIsSet(true);
        return this;
    }

    public void unsetQueryRate() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __QUERYRATE_ISSET_ID);
    }

    public boolean isSetQueryRate() {
        return EncodingUtils.testBit(__isset_bitfield, __QUERYRATE_ISSET_ID);
    }

    public void setQueryRateIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __QUERYRATE_ISSET_ID, value);
    }

    public double getQueryByteRate() {
        return this.queryByteRate;
    }

    public TableInfo setQueryByteRate(double queryByteRate) {
        this.queryByteRate = queryByteRate;
        setQueryByteRateIsSet(true);
        return this;
    }

    public void unsetQueryByteRate() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __QUERYBYTERATE_ISSET_ID);
    }

    public boolean isSetQueryByteRate() {
        return EncodingUtils.testBit(__isset_bitfield, __QUERYBYTERATE_ISSET_ID);
    }

    public void setQueryByteRateIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __QUERYBYTERATE_ISSET_ID, value);
    }

    public Compacting getMinors() {
        return this.minors;
    }

    public TableInfo setMinors(Compacting minors) {
        this.minors = minors;
        return this;
    }

    public void unsetMinors() {
        this.minors = null;
    }

    public boolean isSetMinors() {
        return this.minors != null;
    }

    public void setMinorsIsSet(boolean value) {
        if (!value) {
            this.minors = null;
        }
    }

    public Compacting getMajors() {
        return this.majors;
    }

    public TableInfo setMajors(Compacting majors) {
        this.majors = majors;
        return this;
    }

    public void unsetMajors() {
        this.majors = null;
    }

    public boolean isSetMajors() {
        return this.majors != null;
    }

    public void setMajorsIsSet(boolean value) {
        if (!value) {
            this.majors = null;
        }
    }

    public Compacting getScans() {
        return this.scans;
    }

    public TableInfo setScans(Compacting scans) {
        this.scans = scans;
        return this;
    }

    public void unsetScans() {
        this.scans = null;
    }

    public boolean isSetScans() {
        return this.scans != null;
    }

    public void setScansIsSet(boolean value) {
        if (!value) {
            this.scans = null;
        }
    }

    public double getScanRate() {
        return this.scanRate;
    }

    public TableInfo setScanRate(double scanRate) {
        this.scanRate = scanRate;
        setScanRateIsSet(true);
        return this;
    }

    public void unsetScanRate() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __SCANRATE_ISSET_ID);
    }

    public boolean isSetScanRate() {
        return EncodingUtils.testBit(__isset_bitfield, __SCANRATE_ISSET_ID);
    }

    public void setScanRateIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __SCANRATE_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case RECS:
                if (value == null) {
                    unsetRecs();
                } else {
                    setRecs((Long) value);
                }
                break;
            case RECS_IN_MEMORY:
                if (value == null) {
                    unsetRecsInMemory();
                } else {
                    setRecsInMemory((Long) value);
                }
                break;
            case TABLETS:
                if (value == null) {
                    unsetTablets();
                } else {
                    setTablets((Integer) value);
                }
                break;
            case ONLINE_TABLETS:
                if (value == null) {
                    unsetOnlineTablets();
                } else {
                    setOnlineTablets((Integer) value);
                }
                break;
            case INGEST_RATE:
                if (value == null) {
                    unsetIngestRate();
                } else {
                    setIngestRate((Double) value);
                }
                break;
            case INGEST_BYTE_RATE:
                if (value == null) {
                    unsetIngestByteRate();
                } else {
                    setIngestByteRate((Double) value);
                }
                break;
            case QUERY_RATE:
                if (value == null) {
                    unsetQueryRate();
                } else {
                    setQueryRate((Double) value);
                }
                break;
            case QUERY_BYTE_RATE:
                if (value == null) {
                    unsetQueryByteRate();
                } else {
                    setQueryByteRate((Double) value);
                }
                break;
            case MINORS:
                if (value == null) {
                    unsetMinors();
                } else {
                    setMinors((Compacting) value);
                }
                break;
            case MAJORS:
                if (value == null) {
                    unsetMajors();
                } else {
                    setMajors((Compacting) value);
                }
                break;
            case SCANS:
                if (value == null) {
                    unsetScans();
                } else {
                    setScans((Compacting) value);
                }
                break;
            case SCAN_RATE:
                if (value == null) {
                    unsetScanRate();
                } else {
                    setScanRate((Double) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case RECS:
                return Long.valueOf(getRecs());
            case RECS_IN_MEMORY:
                return Long.valueOf(getRecsInMemory());
            case TABLETS:
                return Integer.valueOf(getTablets());
            case ONLINE_TABLETS:
                return Integer.valueOf(getOnlineTablets());
            case INGEST_RATE:
                return Double.valueOf(getIngestRate());
            case INGEST_BYTE_RATE:
                return Double.valueOf(getIngestByteRate());
            case QUERY_RATE:
                return Double.valueOf(getQueryRate());
            case QUERY_BYTE_RATE:
                return Double.valueOf(getQueryByteRate());
            case MINORS:
                return getMinors();
            case MAJORS:
                return getMajors();
            case SCANS:
                return getScans();
            case SCAN_RATE:
                return Double.valueOf(getScanRate());
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case RECS:
                return isSetRecs();
            case RECS_IN_MEMORY:
                return isSetRecsInMemory();
            case TABLETS:
                return isSetTablets();
            case ONLINE_TABLETS:
                return isSetOnlineTablets();
            case INGEST_RATE:
                return isSetIngestRate();
            case INGEST_BYTE_RATE:
                return isSetIngestByteRate();
            case QUERY_RATE:
                return isSetQueryRate();
            case QUERY_BYTE_RATE:
                return isSetQueryByteRate();
            case MINORS:
                return isSetMinors();
            case MAJORS:
                return isSetMajors();
            case SCANS:
                return isSetScans();
            case SCAN_RATE:
                return isSetScanRate();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof TableInfo)
            return this.equals((TableInfo) that);
        return false;
    }

    public boolean equals(TableInfo that) {
        if (that == null)
            return false;
        boolean this_present_recs = true;
        boolean that_present_recs = true;
        if (this_present_recs || that_present_recs) {
            if (!(this_present_recs && that_present_recs))
                return false;
            if (this.recs != that.recs)
                return false;
        }
        boolean this_present_recsInMemory = true;
        boolean that_present_recsInMemory = true;
        if (this_present_recsInMemory || that_present_recsInMemory) {
            if (!(this_present_recsInMemory && that_present_recsInMemory))
                return false;
            if (this.recsInMemory != that.recsInMemory)
                return false;
        }
        boolean this_present_tablets = true;
        boolean that_present_tablets = true;
        if (this_present_tablets || that_present_tablets) {
            if (!(this_present_tablets && that_present_tablets))
                return false;
            if (this.tablets != that.tablets)
                return false;
        }
        boolean this_present_onlineTablets = true;
        boolean that_present_onlineTablets = true;
        if (this_present_onlineTablets || that_present_onlineTablets) {
            if (!(this_present_onlineTablets && that_present_onlineTablets))
                return false;
            if (this.onlineTablets != that.onlineTablets)
                return false;
        }
        boolean this_present_ingestRate = true;
        boolean that_present_ingestRate = true;
        if (this_present_ingestRate || that_present_ingestRate) {
            if (!(this_present_ingestRate && that_present_ingestRate))
                return false;
            if (this.ingestRate != that.ingestRate)
                return false;
        }
        boolean this_present_ingestByteRate = true;
        boolean that_present_ingestByteRate = true;
        if (this_present_ingestByteRate || that_present_ingestByteRate) {
            if (!(this_present_ingestByteRate && that_present_ingestByteRate))
                return false;
            if (this.ingestByteRate != that.ingestByteRate)
                return false;
        }
        boolean this_present_queryRate = true;
        boolean that_present_queryRate = true;
        if (this_present_queryRate || that_present_queryRate) {
            if (!(this_present_queryRate && that_present_queryRate))
                return false;
            if (this.queryRate != that.queryRate)
                return false;
        }
        boolean this_present_queryByteRate = true;
        boolean that_present_queryByteRate = true;
        if (this_present_queryByteRate || that_present_queryByteRate) {
            if (!(this_present_queryByteRate && that_present_queryByteRate))
                return false;
            if (this.queryByteRate != that.queryByteRate)
                return false;
        }
        boolean this_present_minors = true && this.isSetMinors();
        boolean that_present_minors = true && that.isSetMinors();
        if (this_present_minors || that_present_minors) {
            if (!(this_present_minors && that_present_minors))
                return false;
            if (!this.minors.equals(that.minors))
                return false;
        }
        boolean this_present_majors = true && this.isSetMajors();
        boolean that_present_majors = true && that.isSetMajors();
        if (this_present_majors || that_present_majors) {
            if (!(this_present_majors && that_present_majors))
                return false;
            if (!this.majors.equals(that.majors))
                return false;
        }
        boolean this_present_scans = true && this.isSetScans();
        boolean that_present_scans = true && that.isSetScans();
        if (this_present_scans || that_present_scans) {
            if (!(this_present_scans && that_present_scans))
                return false;
            if (!this.scans.equals(that.scans))
                return false;
        }
        boolean this_present_scanRate = true;
        boolean that_present_scanRate = true;
        if (this_present_scanRate || that_present_scanRate) {
            if (!(this_present_scanRate && that_present_scanRate))
                return false;
            if (this.scanRate != that.scanRate)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int compareTo(TableInfo other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetRecs()).compareTo(other.isSetRecs());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetRecs()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.recs, other.recs);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetRecsInMemory()).compareTo(other.isSetRecsInMemory());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetRecsInMemory()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.recsInMemory, other.recsInMemory);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetTablets()).compareTo(other.isSetTablets());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTablets()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tablets, other.tablets);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetOnlineTablets()).compareTo(other.isSetOnlineTablets());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetOnlineTablets()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.onlineTablets, other.onlineTablets);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetIngestRate()).compareTo(other.isSetIngestRate());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetIngestRate()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ingestRate, other.ingestRate);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetIngestByteRate()).compareTo(other.isSetIngestByteRate());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetIngestByteRate()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ingestByteRate, other.ingestByteRate);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetQueryRate()).compareTo(other.isSetQueryRate());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetQueryRate()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.queryRate, other.queryRate);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetQueryByteRate()).compareTo(other.isSetQueryByteRate());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetQueryByteRate()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.queryByteRate, other.queryByteRate);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetMinors()).compareTo(other.isSetMinors());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetMinors()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.minors, other.minors);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetMajors()).compareTo(other.isSetMajors());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetMajors()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.majors, other.majors);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetScans()).compareTo(other.isSetScans());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetScans()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.scans, other.scans);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetScanRate()).compareTo(other.isSetScanRate());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetScanRate()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.scanRate, other.scanRate);
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
        schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
        schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TableInfo(");
        boolean first = true;
        sb.append("recs:");
        sb.append(this.recs);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("recsInMemory:");
        sb.append(this.recsInMemory);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("tablets:");
        sb.append(this.tablets);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("onlineTablets:");
        sb.append(this.onlineTablets);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("ingestRate:");
        sb.append(this.ingestRate);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("ingestByteRate:");
        sb.append(this.ingestByteRate);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("queryRate:");
        sb.append(this.queryRate);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("queryByteRate:");
        sb.append(this.queryByteRate);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("minors:");
        if (this.minors == null) {
            sb.append("null");
        } else {
            sb.append(this.minors);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("majors:");
        if (this.majors == null) {
            sb.append("null");
        } else {
            sb.append(this.majors);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("scans:");
        if (this.scans == null) {
            sb.append("null");
        } else {
            sb.append(this.scans);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("scanRate:");
        sb.append(this.scanRate);
        first = false;
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
        if (minors != null) {
            minors.validate();
        }
        if (majors != null) {
            majors.validate();
        }
        if (scans != null) {
            scans.validate();
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
            __isset_bitfield = 0;
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class TableInfoStandardSchemeFactory implements SchemeFactory {

        public TableInfoStandardScheme getScheme() {
            return new TableInfoStandardScheme();
        }
    }

    private static class TableInfoStandardScheme extends StandardScheme<TableInfo> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, TableInfo struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch(schemeField.id) {
                    case 1:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.recs = iprot.readI64();
                            struct.setRecsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.recsInMemory = iprot.readI64();
                            struct.setRecsInMemoryIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.tablets = iprot.readI32();
                            struct.setTabletsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.onlineTablets = iprot.readI32();
                            struct.setOnlineTabletsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 5:
                        if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
                            struct.ingestRate = iprot.readDouble();
                            struct.setIngestRateIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 6:
                        if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
                            struct.ingestByteRate = iprot.readDouble();
                            struct.setIngestByteRateIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 7:
                        if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
                            struct.queryRate = iprot.readDouble();
                            struct.setQueryRateIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 8:
                        if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
                            struct.queryByteRate = iprot.readDouble();
                            struct.setQueryByteRateIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 9:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.minors = new Compacting();
                            struct.minors.read(iprot);
                            struct.setMinorsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 10:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.majors = new Compacting();
                            struct.majors.read(iprot);
                            struct.setMajorsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 11:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.scans = new Compacting();
                            struct.scans.read(iprot);
                            struct.setScansIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 12:
                        if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
                            struct.scanRate = iprot.readDouble();
                            struct.setScanRateIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    default:
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                }
                iprot.readFieldEnd();
            }
            iprot.readStructEnd();
            struct.validate();
        }

        public void write(org.apache.thrift.protocol.TProtocol oprot, TableInfo struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            oprot.writeFieldBegin(RECS_FIELD_DESC);
            oprot.writeI64(struct.recs);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(RECS_IN_MEMORY_FIELD_DESC);
            oprot.writeI64(struct.recsInMemory);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(TABLETS_FIELD_DESC);
            oprot.writeI32(struct.tablets);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(ONLINE_TABLETS_FIELD_DESC);
            oprot.writeI32(struct.onlineTablets);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(INGEST_RATE_FIELD_DESC);
            oprot.writeDouble(struct.ingestRate);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(INGEST_BYTE_RATE_FIELD_DESC);
            oprot.writeDouble(struct.ingestByteRate);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(QUERY_RATE_FIELD_DESC);
            oprot.writeDouble(struct.queryRate);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(QUERY_BYTE_RATE_FIELD_DESC);
            oprot.writeDouble(struct.queryByteRate);
            oprot.writeFieldEnd();
            if (struct.minors != null) {
                oprot.writeFieldBegin(MINORS_FIELD_DESC);
                struct.minors.write(oprot);
                oprot.writeFieldEnd();
            }
            if (struct.majors != null) {
                oprot.writeFieldBegin(MAJORS_FIELD_DESC);
                struct.majors.write(oprot);
                oprot.writeFieldEnd();
            }
            if (struct.scans != null) {
                oprot.writeFieldBegin(SCANS_FIELD_DESC);
                struct.scans.write(oprot);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(SCAN_RATE_FIELD_DESC);
            oprot.writeDouble(struct.scanRate);
            oprot.writeFieldEnd();
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class TableInfoTupleSchemeFactory implements SchemeFactory {

        public TableInfoTupleScheme getScheme() {
            return new TableInfoTupleScheme();
        }
    }

    private static class TableInfoTupleScheme extends TupleScheme<TableInfo> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, TableInfo struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetRecs()) {
                optionals.set(0);
            }
            if (struct.isSetRecsInMemory()) {
                optionals.set(1);
            }
            if (struct.isSetTablets()) {
                optionals.set(2);
            }
            if (struct.isSetOnlineTablets()) {
                optionals.set(3);
            }
            if (struct.isSetIngestRate()) {
                optionals.set(4);
            }
            if (struct.isSetIngestByteRate()) {
                optionals.set(5);
            }
            if (struct.isSetQueryRate()) {
                optionals.set(6);
            }
            if (struct.isSetQueryByteRate()) {
                optionals.set(7);
            }
            if (struct.isSetMinors()) {
                optionals.set(8);
            }
            if (struct.isSetMajors()) {
                optionals.set(9);
            }
            if (struct.isSetScans()) {
                optionals.set(10);
            }
            if (struct.isSetScanRate()) {
                optionals.set(11);
            }
            oprot.writeBitSet(optionals, 12);
            if (struct.isSetRecs()) {
                oprot.writeI64(struct.recs);
            }
            if (struct.isSetRecsInMemory()) {
                oprot.writeI64(struct.recsInMemory);
            }
            if (struct.isSetTablets()) {
                oprot.writeI32(struct.tablets);
            }
            if (struct.isSetOnlineTablets()) {
                oprot.writeI32(struct.onlineTablets);
            }
            if (struct.isSetIngestRate()) {
                oprot.writeDouble(struct.ingestRate);
            }
            if (struct.isSetIngestByteRate()) {
                oprot.writeDouble(struct.ingestByteRate);
            }
            if (struct.isSetQueryRate()) {
                oprot.writeDouble(struct.queryRate);
            }
            if (struct.isSetQueryByteRate()) {
                oprot.writeDouble(struct.queryByteRate);
            }
            if (struct.isSetMinors()) {
                struct.minors.write(oprot);
            }
            if (struct.isSetMajors()) {
                struct.majors.write(oprot);
            }
            if (struct.isSetScans()) {
                struct.scans.write(oprot);
            }
            if (struct.isSetScanRate()) {
                oprot.writeDouble(struct.scanRate);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, TableInfo struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(12);
            if (incoming.get(0)) {
                struct.recs = iprot.readI64();
                struct.setRecsIsSet(true);
            }
            if (incoming.get(1)) {
                struct.recsInMemory = iprot.readI64();
                struct.setRecsInMemoryIsSet(true);
            }
            if (incoming.get(2)) {
                struct.tablets = iprot.readI32();
                struct.setTabletsIsSet(true);
            }
            if (incoming.get(3)) {
                struct.onlineTablets = iprot.readI32();
                struct.setOnlineTabletsIsSet(true);
            }
            if (incoming.get(4)) {
                struct.ingestRate = iprot.readDouble();
                struct.setIngestRateIsSet(true);
            }
            if (incoming.get(5)) {
                struct.ingestByteRate = iprot.readDouble();
                struct.setIngestByteRateIsSet(true);
            }
            if (incoming.get(6)) {
                struct.queryRate = iprot.readDouble();
                struct.setQueryRateIsSet(true);
            }
            if (incoming.get(7)) {
                struct.queryByteRate = iprot.readDouble();
                struct.setQueryByteRateIsSet(true);
            }
            if (incoming.get(8)) {
                struct.minors = new Compacting();
                struct.minors.read(iprot);
                struct.setMinorsIsSet(true);
            }
            if (incoming.get(9)) {
                struct.majors = new Compacting();
                struct.majors.read(iprot);
                struct.setMajorsIsSet(true);
            }
            if (incoming.get(10)) {
                struct.scans = new Compacting();
                struct.scans.read(iprot);
                struct.setScansIsSet(true);
            }
            if (incoming.get(11)) {
                struct.scanRate = iprot.readDouble();
                struct.setScanRateIsSet(true);
            }
        }
    }
}