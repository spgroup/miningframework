package org.apache.accumulo.core.tabletserver.thrift;

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

@SuppressWarnings({ "unchecked", "serial", "rawtypes", "unused" })
public class TabletStats implements org.apache.thrift.TBase<TabletStats, TabletStats._Fields>, java.io.Serializable, Cloneable, Comparable<TabletStats> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TabletStats");

    private static final org.apache.thrift.protocol.TField EXTENT_FIELD_DESC = new org.apache.thrift.protocol.TField("extent", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

    private static final org.apache.thrift.protocol.TField MAJORS_FIELD_DESC = new org.apache.thrift.protocol.TField("majors", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

    private static final org.apache.thrift.protocol.TField MINORS_FIELD_DESC = new org.apache.thrift.protocol.TField("minors", org.apache.thrift.protocol.TType.STRUCT, (short) 3);

    private static final org.apache.thrift.protocol.TField SPLITS_FIELD_DESC = new org.apache.thrift.protocol.TField("splits", org.apache.thrift.protocol.TType.STRUCT, (short) 4);

    private static final org.apache.thrift.protocol.TField NUM_ENTRIES_FIELD_DESC = new org.apache.thrift.protocol.TField("numEntries", org.apache.thrift.protocol.TType.I64, (short) 5);

    private static final org.apache.thrift.protocol.TField INGEST_RATE_FIELD_DESC = new org.apache.thrift.protocol.TField("ingestRate", org.apache.thrift.protocol.TType.DOUBLE, (short) 6);

    private static final org.apache.thrift.protocol.TField QUERY_RATE_FIELD_DESC = new org.apache.thrift.protocol.TField("queryRate", org.apache.thrift.protocol.TType.DOUBLE, (short) 7);

    private static final org.apache.thrift.protocol.TField SPLIT_CREATION_TIME_FIELD_DESC = new org.apache.thrift.protocol.TField("splitCreationTime", org.apache.thrift.protocol.TType.I64, (short) 8);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new TabletStatsStandardSchemeFactory());
        schemes.put(TupleScheme.class, new TabletStatsTupleSchemeFactory());
    }

    public org.apache.accumulo.core.data.thrift.TKeyExtent extent;

    public ActionStats majors;

    public ActionStats minors;

    public ActionStats splits;

    public long numEntries;

    public double ingestRate;

    public double queryRate;

    public long splitCreationTime;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        EXTENT((short) 1, "extent"),
        MAJORS((short) 2, "majors"),
        MINORS((short) 3, "minors"),
        SPLITS((short) 4, "splits"),
        NUM_ENTRIES((short) 5, "numEntries"),
        INGEST_RATE((short) 6, "ingestRate"),
        QUERY_RATE((short) 7, "queryRate"),
        SPLIT_CREATION_TIME((short) 8, "splitCreationTime");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return EXTENT;
                case 2:
                    return MAJORS;
                case 3:
                    return MINORS;
                case 4:
                    return SPLITS;
                case 5:
                    return NUM_ENTRIES;
                case 6:
                    return INGEST_RATE;
                case 7:
                    return QUERY_RATE;
                case 8:
                    return SPLIT_CREATION_TIME;
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

    private static final int __NUMENTRIES_ISSET_ID = 0;

    private static final int __INGESTRATE_ISSET_ID = 1;

    private static final int __QUERYRATE_ISSET_ID = 2;

    private static final int __SPLITCREATIONTIME_ISSET_ID = 3;

    private byte __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.EXTENT, new org.apache.thrift.meta_data.FieldMetaData("extent", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.data.thrift.TKeyExtent.class)));
        tmpMap.put(_Fields.MAJORS, new org.apache.thrift.meta_data.FieldMetaData("majors", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, ActionStats.class)));
        tmpMap.put(_Fields.MINORS, new org.apache.thrift.meta_data.FieldMetaData("minors", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, ActionStats.class)));
        tmpMap.put(_Fields.SPLITS, new org.apache.thrift.meta_data.FieldMetaData("splits", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, ActionStats.class)));
        tmpMap.put(_Fields.NUM_ENTRIES, new org.apache.thrift.meta_data.FieldMetaData("numEntries", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.INGEST_RATE, new org.apache.thrift.meta_data.FieldMetaData("ingestRate", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.QUERY_RATE, new org.apache.thrift.meta_data.FieldMetaData("queryRate", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.SPLIT_CREATION_TIME, new org.apache.thrift.meta_data.FieldMetaData("splitCreationTime", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TabletStats.class, metaDataMap);
    }

    public TabletStats() {
    }

    public TabletStats(org.apache.accumulo.core.data.thrift.TKeyExtent extent, ActionStats majors, ActionStats minors, ActionStats splits, long numEntries, double ingestRate, double queryRate, long splitCreationTime) {
        this();
        this.extent = extent;
        this.majors = majors;
        this.minors = minors;
        this.splits = splits;
        this.numEntries = numEntries;
        setNumEntriesIsSet(true);
        this.ingestRate = ingestRate;
        setIngestRateIsSet(true);
        this.queryRate = queryRate;
        setQueryRateIsSet(true);
        this.splitCreationTime = splitCreationTime;
        setSplitCreationTimeIsSet(true);
    }

    public TabletStats(TabletStats other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetExtent()) {
            this.extent = new org.apache.accumulo.core.data.thrift.TKeyExtent(other.extent);
        }
        if (other.isSetMajors()) {
            this.majors = new ActionStats(other.majors);
        }
        if (other.isSetMinors()) {
            this.minors = new ActionStats(other.minors);
        }
        if (other.isSetSplits()) {
            this.splits = new ActionStats(other.splits);
        }
        this.numEntries = other.numEntries;
        this.ingestRate = other.ingestRate;
        this.queryRate = other.queryRate;
        this.splitCreationTime = other.splitCreationTime;
    }

    public TabletStats deepCopy() {
        return new TabletStats(this);
    }

    @Override
    public void clear() {
        this.extent = null;
        this.majors = null;
        this.minors = null;
        this.splits = null;
        setNumEntriesIsSet(false);
        this.numEntries = 0;
        setIngestRateIsSet(false);
        this.ingestRate = 0.0;
        setQueryRateIsSet(false);
        this.queryRate = 0.0;
        setSplitCreationTimeIsSet(false);
        this.splitCreationTime = 0;
    }

    public org.apache.accumulo.core.data.thrift.TKeyExtent getExtent() {
        return this.extent;
    }

    public TabletStats setExtent(org.apache.accumulo.core.data.thrift.TKeyExtent extent) {
        this.extent = extent;
        return this;
    }

    public void unsetExtent() {
        this.extent = null;
    }

    public boolean isSetExtent() {
        return this.extent != null;
    }

    public void setExtentIsSet(boolean value) {
        if (!value) {
            this.extent = null;
        }
    }

    public ActionStats getMajors() {
        return this.majors;
    }

    public TabletStats setMajors(ActionStats majors) {
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

    public ActionStats getMinors() {
        return this.minors;
    }

    public TabletStats setMinors(ActionStats minors) {
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

    public ActionStats getSplits() {
        return this.splits;
    }

    public TabletStats setSplits(ActionStats splits) {
        this.splits = splits;
        return this;
    }

    public void unsetSplits() {
        this.splits = null;
    }

    public boolean isSetSplits() {
        return this.splits != null;
    }

    public void setSplitsIsSet(boolean value) {
        if (!value) {
            this.splits = null;
        }
    }

    public long getNumEntries() {
        return this.numEntries;
    }

    public TabletStats setNumEntries(long numEntries) {
        this.numEntries = numEntries;
        setNumEntriesIsSet(true);
        return this;
    }

    public void unsetNumEntries() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __NUMENTRIES_ISSET_ID);
    }

    public boolean isSetNumEntries() {
        return EncodingUtils.testBit(__isset_bitfield, __NUMENTRIES_ISSET_ID);
    }

    public void setNumEntriesIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __NUMENTRIES_ISSET_ID, value);
    }

    public double getIngestRate() {
        return this.ingestRate;
    }

    public TabletStats setIngestRate(double ingestRate) {
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

    public double getQueryRate() {
        return this.queryRate;
    }

    public TabletStats setQueryRate(double queryRate) {
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

    public long getSplitCreationTime() {
        return this.splitCreationTime;
    }

    public TabletStats setSplitCreationTime(long splitCreationTime) {
        this.splitCreationTime = splitCreationTime;
        setSplitCreationTimeIsSet(true);
        return this;
    }

    public void unsetSplitCreationTime() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __SPLITCREATIONTIME_ISSET_ID);
    }

    public boolean isSetSplitCreationTime() {
        return EncodingUtils.testBit(__isset_bitfield, __SPLITCREATIONTIME_ISSET_ID);
    }

    public void setSplitCreationTimeIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __SPLITCREATIONTIME_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case EXTENT:
                if (value == null) {
                    unsetExtent();
                } else {
                    setExtent((org.apache.accumulo.core.data.thrift.TKeyExtent) value);
                }
                break;
            case MAJORS:
                if (value == null) {
                    unsetMajors();
                } else {
                    setMajors((ActionStats) value);
                }
                break;
            case MINORS:
                if (value == null) {
                    unsetMinors();
                } else {
                    setMinors((ActionStats) value);
                }
                break;
            case SPLITS:
                if (value == null) {
                    unsetSplits();
                } else {
                    setSplits((ActionStats) value);
                }
                break;
            case NUM_ENTRIES:
                if (value == null) {
                    unsetNumEntries();
                } else {
                    setNumEntries((Long) value);
                }
                break;
            case INGEST_RATE:
                if (value == null) {
                    unsetIngestRate();
                } else {
                    setIngestRate((Double) value);
                }
                break;
            case QUERY_RATE:
                if (value == null) {
                    unsetQueryRate();
                } else {
                    setQueryRate((Double) value);
                }
                break;
            case SPLIT_CREATION_TIME:
                if (value == null) {
                    unsetSplitCreationTime();
                } else {
                    setSplitCreationTime((Long) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case EXTENT:
                return getExtent();
            case MAJORS:
                return getMajors();
            case MINORS:
                return getMinors();
            case SPLITS:
                return getSplits();
            case NUM_ENTRIES:
                return Long.valueOf(getNumEntries());
            case INGEST_RATE:
                return Double.valueOf(getIngestRate());
            case QUERY_RATE:
                return Double.valueOf(getQueryRate());
            case SPLIT_CREATION_TIME:
                return Long.valueOf(getSplitCreationTime());
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case EXTENT:
                return isSetExtent();
            case MAJORS:
                return isSetMajors();
            case MINORS:
                return isSetMinors();
            case SPLITS:
                return isSetSplits();
            case NUM_ENTRIES:
                return isSetNumEntries();
            case INGEST_RATE:
                return isSetIngestRate();
            case QUERY_RATE:
                return isSetQueryRate();
            case SPLIT_CREATION_TIME:
                return isSetSplitCreationTime();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof TabletStats)
            return this.equals((TabletStats) that);
        return false;
    }

    public boolean equals(TabletStats that) {
        if (that == null)
            return false;
        boolean this_present_extent = true && this.isSetExtent();
        boolean that_present_extent = true && that.isSetExtent();
        if (this_present_extent || that_present_extent) {
            if (!(this_present_extent && that_present_extent))
                return false;
            if (!this.extent.equals(that.extent))
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
        boolean this_present_minors = true && this.isSetMinors();
        boolean that_present_minors = true && that.isSetMinors();
        if (this_present_minors || that_present_minors) {
            if (!(this_present_minors && that_present_minors))
                return false;
            if (!this.minors.equals(that.minors))
                return false;
        }
        boolean this_present_splits = true && this.isSetSplits();
        boolean that_present_splits = true && that.isSetSplits();
        if (this_present_splits || that_present_splits) {
            if (!(this_present_splits && that_present_splits))
                return false;
            if (!this.splits.equals(that.splits))
                return false;
        }
        boolean this_present_numEntries = true;
        boolean that_present_numEntries = true;
        if (this_present_numEntries || that_present_numEntries) {
            if (!(this_present_numEntries && that_present_numEntries))
                return false;
            if (this.numEntries != that.numEntries)
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
        boolean this_present_queryRate = true;
        boolean that_present_queryRate = true;
        if (this_present_queryRate || that_present_queryRate) {
            if (!(this_present_queryRate && that_present_queryRate))
                return false;
            if (this.queryRate != that.queryRate)
                return false;
        }
        boolean this_present_splitCreationTime = true;
        boolean that_present_splitCreationTime = true;
        if (this_present_splitCreationTime || that_present_splitCreationTime) {
            if (!(this_present_splitCreationTime && that_present_splitCreationTime))
                return false;
            if (this.splitCreationTime != that.splitCreationTime)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int compareTo(TabletStats other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetExtent()).compareTo(other.isSetExtent());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetExtent()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.extent, other.extent);
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
        lastComparison = Boolean.valueOf(isSetSplits()).compareTo(other.isSetSplits());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetSplits()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.splits, other.splits);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetNumEntries()).compareTo(other.isSetNumEntries());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetNumEntries()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.numEntries, other.numEntries);
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
        lastComparison = Boolean.valueOf(isSetSplitCreationTime()).compareTo(other.isSetSplitCreationTime());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetSplitCreationTime()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.splitCreationTime, other.splitCreationTime);
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
        StringBuilder sb = new StringBuilder("TabletStats(");
        boolean first = true;
        sb.append("extent:");
        if (this.extent == null) {
            sb.append("null");
        } else {
            sb.append(this.extent);
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
        sb.append("minors:");
        if (this.minors == null) {
            sb.append("null");
        } else {
            sb.append(this.minors);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("splits:");
        if (this.splits == null) {
            sb.append("null");
        } else {
            sb.append(this.splits);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("numEntries:");
        sb.append(this.numEntries);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("ingestRate:");
        sb.append(this.ingestRate);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("queryRate:");
        sb.append(this.queryRate);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("splitCreationTime:");
        sb.append(this.splitCreationTime);
        first = false;
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
        if (extent != null) {
            extent.validate();
        }
        if (majors != null) {
            majors.validate();
        }
        if (minors != null) {
            minors.validate();
        }
        if (splits != null) {
            splits.validate();
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

    private static class TabletStatsStandardSchemeFactory implements SchemeFactory {

        public TabletStatsStandardScheme getScheme() {
            return new TabletStatsStandardScheme();
        }
    }

    private static class TabletStatsStandardScheme extends StandardScheme<TabletStats> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, TabletStats struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch(schemeField.id) {
                    case 1:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.extent = new org.apache.accumulo.core.data.thrift.TKeyExtent();
                            struct.extent.read(iprot);
                            struct.setExtentIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.majors = new ActionStats();
                            struct.majors.read(iprot);
                            struct.setMajorsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.minors = new ActionStats();
                            struct.minors.read(iprot);
                            struct.setMinorsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.splits = new ActionStats();
                            struct.splits.read(iprot);
                            struct.setSplitsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 5:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.numEntries = iprot.readI64();
                            struct.setNumEntriesIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 6:
                        if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
                            struct.ingestRate = iprot.readDouble();
                            struct.setIngestRateIsSet(true);
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
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.splitCreationTime = iprot.readI64();
                            struct.setSplitCreationTimeIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, TabletStats struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.extent != null) {
                oprot.writeFieldBegin(EXTENT_FIELD_DESC);
                struct.extent.write(oprot);
                oprot.writeFieldEnd();
            }
            if (struct.majors != null) {
                oprot.writeFieldBegin(MAJORS_FIELD_DESC);
                struct.majors.write(oprot);
                oprot.writeFieldEnd();
            }
            if (struct.minors != null) {
                oprot.writeFieldBegin(MINORS_FIELD_DESC);
                struct.minors.write(oprot);
                oprot.writeFieldEnd();
            }
            if (struct.splits != null) {
                oprot.writeFieldBegin(SPLITS_FIELD_DESC);
                struct.splits.write(oprot);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(NUM_ENTRIES_FIELD_DESC);
            oprot.writeI64(struct.numEntries);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(INGEST_RATE_FIELD_DESC);
            oprot.writeDouble(struct.ingestRate);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(QUERY_RATE_FIELD_DESC);
            oprot.writeDouble(struct.queryRate);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(SPLIT_CREATION_TIME_FIELD_DESC);
            oprot.writeI64(struct.splitCreationTime);
            oprot.writeFieldEnd();
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class TabletStatsTupleSchemeFactory implements SchemeFactory {

        public TabletStatsTupleScheme getScheme() {
            return new TabletStatsTupleScheme();
        }
    }

    private static class TabletStatsTupleScheme extends TupleScheme<TabletStats> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, TabletStats struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetExtent()) {
                optionals.set(0);
            }
            if (struct.isSetMajors()) {
                optionals.set(1);
            }
            if (struct.isSetMinors()) {
                optionals.set(2);
            }
            if (struct.isSetSplits()) {
                optionals.set(3);
            }
            if (struct.isSetNumEntries()) {
                optionals.set(4);
            }
            if (struct.isSetIngestRate()) {
                optionals.set(5);
            }
            if (struct.isSetQueryRate()) {
                optionals.set(6);
            }
            if (struct.isSetSplitCreationTime()) {
                optionals.set(7);
            }
            oprot.writeBitSet(optionals, 8);
            if (struct.isSetExtent()) {
                struct.extent.write(oprot);
            }
            if (struct.isSetMajors()) {
                struct.majors.write(oprot);
            }
            if (struct.isSetMinors()) {
                struct.minors.write(oprot);
            }
            if (struct.isSetSplits()) {
                struct.splits.write(oprot);
            }
            if (struct.isSetNumEntries()) {
                oprot.writeI64(struct.numEntries);
            }
            if (struct.isSetIngestRate()) {
                oprot.writeDouble(struct.ingestRate);
            }
            if (struct.isSetQueryRate()) {
                oprot.writeDouble(struct.queryRate);
            }
            if (struct.isSetSplitCreationTime()) {
                oprot.writeI64(struct.splitCreationTime);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, TabletStats struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(8);
            if (incoming.get(0)) {
                struct.extent = new org.apache.accumulo.core.data.thrift.TKeyExtent();
                struct.extent.read(iprot);
                struct.setExtentIsSet(true);
            }
            if (incoming.get(1)) {
                struct.majors = new ActionStats();
                struct.majors.read(iprot);
                struct.setMajorsIsSet(true);
            }
            if (incoming.get(2)) {
                struct.minors = new ActionStats();
                struct.minors.read(iprot);
                struct.setMinorsIsSet(true);
            }
            if (incoming.get(3)) {
                struct.splits = new ActionStats();
                struct.splits.read(iprot);
                struct.setSplitsIsSet(true);
            }
            if (incoming.get(4)) {
                struct.numEntries = iprot.readI64();
                struct.setNumEntriesIsSet(true);
            }
            if (incoming.get(5)) {
                struct.ingestRate = iprot.readDouble();
                struct.setIngestRateIsSet(true);
            }
            if (incoming.get(6)) {
                struct.queryRate = iprot.readDouble();
                struct.setQueryRateIsSet(true);
            }
            if (incoming.get(7)) {
                struct.splitCreationTime = iprot.readI64();
                struct.setSplitCreationTimeIsSet(true);
            }
        }
    }
}
