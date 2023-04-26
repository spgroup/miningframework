package org.apache.accumulo.core.tabletserver.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;
import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
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
public class ActionStats implements org.apache.thrift.TBase<ActionStats, ActionStats._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ActionStats");

    private static final org.apache.thrift.protocol.TField STATUS_FIELD_DESC = new org.apache.thrift.protocol.TField("status", org.apache.thrift.protocol.TType.I32, (short) 1);

    private static final org.apache.thrift.protocol.TField ELAPSED_FIELD_DESC = new org.apache.thrift.protocol.TField("elapsed", org.apache.thrift.protocol.TType.DOUBLE, (short) 2);

    private static final org.apache.thrift.protocol.TField NUM_FIELD_DESC = new org.apache.thrift.protocol.TField("num", org.apache.thrift.protocol.TType.I32, (short) 3);

    private static final org.apache.thrift.protocol.TField COUNT_FIELD_DESC = new org.apache.thrift.protocol.TField("count", org.apache.thrift.protocol.TType.I64, (short) 4);

    private static final org.apache.thrift.protocol.TField SUM_DEV_FIELD_DESC = new org.apache.thrift.protocol.TField("sumDev", org.apache.thrift.protocol.TType.DOUBLE, (short) 5);

    private static final org.apache.thrift.protocol.TField FAIL_FIELD_DESC = new org.apache.thrift.protocol.TField("fail", org.apache.thrift.protocol.TType.I32, (short) 6);

    private static final org.apache.thrift.protocol.TField QUEUE_TIME_FIELD_DESC = new org.apache.thrift.protocol.TField("queueTime", org.apache.thrift.protocol.TType.DOUBLE, (short) 7);

    private static final org.apache.thrift.protocol.TField QUEUE_SUM_DEV_FIELD_DESC = new org.apache.thrift.protocol.TField("queueSumDev", org.apache.thrift.protocol.TType.DOUBLE, (short) 8);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new ActionStatsStandardSchemeFactory());
        schemes.put(TupleScheme.class, new ActionStatsTupleSchemeFactory());
    }

    public int status;

    public double elapsed;

    public int num;

    public long count;

    public double sumDev;

    public int fail;

    public double queueTime;

    public double queueSumDev;

    @SuppressWarnings("all")
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        STATUS((short) 1, "status"),
        ELAPSED((short) 2, "elapsed"),
        NUM((short) 3, "num"),
        COUNT((short) 4, "count"),
        SUM_DEV((short) 5, "sumDev"),
        FAIL((short) 6, "fail"),
        QUEUE_TIME((short) 7, "queueTime"),
        QUEUE_SUM_DEV((short) 8, "queueSumDev");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return STATUS;
                case 2:
                    return ELAPSED;
                case 3:
                    return NUM;
                case 4:
                    return COUNT;
                case 5:
                    return SUM_DEV;
                case 6:
                    return FAIL;
                case 7:
                    return QUEUE_TIME;
                case 8:
                    return QUEUE_SUM_DEV;
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

    private static final int __STATUS_ISSET_ID = 0;

    private static final int __ELAPSED_ISSET_ID = 1;

    private static final int __NUM_ISSET_ID = 2;

    private static final int __COUNT_ISSET_ID = 3;

    private static final int __SUMDEV_ISSET_ID = 4;

    private static final int __FAIL_ISSET_ID = 5;

    private static final int __QUEUETIME_ISSET_ID = 6;

    private static final int __QUEUESUMDEV_ISSET_ID = 7;

    private byte __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.STATUS, new org.apache.thrift.meta_data.FieldMetaData("status", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.ELAPSED, new org.apache.thrift.meta_data.FieldMetaData("elapsed", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.NUM, new org.apache.thrift.meta_data.FieldMetaData("num", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.COUNT, new org.apache.thrift.meta_data.FieldMetaData("count", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.SUM_DEV, new org.apache.thrift.meta_data.FieldMetaData("sumDev", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.FAIL, new org.apache.thrift.meta_data.FieldMetaData("fail", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.QUEUE_TIME, new org.apache.thrift.meta_data.FieldMetaData("queueTime", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.QUEUE_SUM_DEV, new org.apache.thrift.meta_data.FieldMetaData("queueSumDev", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ActionStats.class, metaDataMap);
    }

    public ActionStats() {
    }

    public ActionStats(int status, double elapsed, int num, long count, double sumDev, int fail, double queueTime, double queueSumDev) {
        this();
        this.status = status;
        setStatusIsSet(true);
        this.elapsed = elapsed;
        setElapsedIsSet(true);
        this.num = num;
        setNumIsSet(true);
        this.count = count;
        setCountIsSet(true);
        this.sumDev = sumDev;
        setSumDevIsSet(true);
        this.fail = fail;
        setFailIsSet(true);
        this.queueTime = queueTime;
        setQueueTimeIsSet(true);
        this.queueSumDev = queueSumDev;
        setQueueSumDevIsSet(true);
    }

    public ActionStats(ActionStats other) {
        __isset_bitfield = other.__isset_bitfield;
        this.status = other.status;
        this.elapsed = other.elapsed;
        this.num = other.num;
        this.count = other.count;
        this.sumDev = other.sumDev;
        this.fail = other.fail;
        this.queueTime = other.queueTime;
        this.queueSumDev = other.queueSumDev;
    }

    public ActionStats deepCopy() {
        return new ActionStats(this);
    }

    @Override
    public void clear() {
        setStatusIsSet(false);
        this.status = 0;
        setElapsedIsSet(false);
        this.elapsed = 0.0;
        setNumIsSet(false);
        this.num = 0;
        setCountIsSet(false);
        this.count = 0;
        setSumDevIsSet(false);
        this.sumDev = 0.0;
        setFailIsSet(false);
        this.fail = 0;
        setQueueTimeIsSet(false);
        this.queueTime = 0.0;
        setQueueSumDevIsSet(false);
        this.queueSumDev = 0.0;
    }

    public int getStatus() {
        return this.status;
    }

    public ActionStats setStatus(int status) {
        this.status = status;
        setStatusIsSet(true);
        return this;
    }

    public void unsetStatus() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __STATUS_ISSET_ID);
    }

    public boolean isSetStatus() {
        return EncodingUtils.testBit(__isset_bitfield, __STATUS_ISSET_ID);
    }

    public void setStatusIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __STATUS_ISSET_ID, value);
    }

    public double getElapsed() {
        return this.elapsed;
    }

    public ActionStats setElapsed(double elapsed) {
        this.elapsed = elapsed;
        setElapsedIsSet(true);
        return this;
    }

    public void unsetElapsed() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __ELAPSED_ISSET_ID);
    }

    public boolean isSetElapsed() {
        return EncodingUtils.testBit(__isset_bitfield, __ELAPSED_ISSET_ID);
    }

    public void setElapsedIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __ELAPSED_ISSET_ID, value);
    }

    public int getNum() {
        return this.num;
    }

    public ActionStats setNum(int num) {
        this.num = num;
        setNumIsSet(true);
        return this;
    }

    public void unsetNum() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __NUM_ISSET_ID);
    }

    public boolean isSetNum() {
        return EncodingUtils.testBit(__isset_bitfield, __NUM_ISSET_ID);
    }

    public void setNumIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __NUM_ISSET_ID, value);
    }

    public long getCount() {
        return this.count;
    }

    public ActionStats setCount(long count) {
        this.count = count;
        setCountIsSet(true);
        return this;
    }

    public void unsetCount() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __COUNT_ISSET_ID);
    }

    public boolean isSetCount() {
        return EncodingUtils.testBit(__isset_bitfield, __COUNT_ISSET_ID);
    }

    public void setCountIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __COUNT_ISSET_ID, value);
    }

    public double getSumDev() {
        return this.sumDev;
    }

    public ActionStats setSumDev(double sumDev) {
        this.sumDev = sumDev;
        setSumDevIsSet(true);
        return this;
    }

    public void unsetSumDev() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __SUMDEV_ISSET_ID);
    }

    public boolean isSetSumDev() {
        return EncodingUtils.testBit(__isset_bitfield, __SUMDEV_ISSET_ID);
    }

    public void setSumDevIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __SUMDEV_ISSET_ID, value);
    }

    public int getFail() {
        return this.fail;
    }

    public ActionStats setFail(int fail) {
        this.fail = fail;
        setFailIsSet(true);
        return this;
    }

    public void unsetFail() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __FAIL_ISSET_ID);
    }

    public boolean isSetFail() {
        return EncodingUtils.testBit(__isset_bitfield, __FAIL_ISSET_ID);
    }

    public void setFailIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __FAIL_ISSET_ID, value);
    }

    public double getQueueTime() {
        return this.queueTime;
    }

    public ActionStats setQueueTime(double queueTime) {
        this.queueTime = queueTime;
        setQueueTimeIsSet(true);
        return this;
    }

    public void unsetQueueTime() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __QUEUETIME_ISSET_ID);
    }

    public boolean isSetQueueTime() {
        return EncodingUtils.testBit(__isset_bitfield, __QUEUETIME_ISSET_ID);
    }

    public void setQueueTimeIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __QUEUETIME_ISSET_ID, value);
    }

    public double getQueueSumDev() {
        return this.queueSumDev;
    }

    public ActionStats setQueueSumDev(double queueSumDev) {
        this.queueSumDev = queueSumDev;
        setQueueSumDevIsSet(true);
        return this;
    }

    public void unsetQueueSumDev() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __QUEUESUMDEV_ISSET_ID);
    }

    public boolean isSetQueueSumDev() {
        return EncodingUtils.testBit(__isset_bitfield, __QUEUESUMDEV_ISSET_ID);
    }

    public void setQueueSumDevIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __QUEUESUMDEV_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case STATUS:
                if (value == null) {
                    unsetStatus();
                } else {
                    setStatus((Integer) value);
                }
                break;
            case ELAPSED:
                if (value == null) {
                    unsetElapsed();
                } else {
                    setElapsed((Double) value);
                }
                break;
            case NUM:
                if (value == null) {
                    unsetNum();
                } else {
                    setNum((Integer) value);
                }
                break;
            case COUNT:
                if (value == null) {
                    unsetCount();
                } else {
                    setCount((Long) value);
                }
                break;
            case SUM_DEV:
                if (value == null) {
                    unsetSumDev();
                } else {
                    setSumDev((Double) value);
                }
                break;
            case FAIL:
                if (value == null) {
                    unsetFail();
                } else {
                    setFail((Integer) value);
                }
                break;
            case QUEUE_TIME:
                if (value == null) {
                    unsetQueueTime();
                } else {
                    setQueueTime((Double) value);
                }
                break;
            case QUEUE_SUM_DEV:
                if (value == null) {
                    unsetQueueSumDev();
                } else {
                    setQueueSumDev((Double) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case STATUS:
                return Integer.valueOf(getStatus());
            case ELAPSED:
                return Double.valueOf(getElapsed());
            case NUM:
                return Integer.valueOf(getNum());
            case COUNT:
                return Long.valueOf(getCount());
            case SUM_DEV:
                return Double.valueOf(getSumDev());
            case FAIL:
                return Integer.valueOf(getFail());
            case QUEUE_TIME:
                return Double.valueOf(getQueueTime());
            case QUEUE_SUM_DEV:
                return Double.valueOf(getQueueSumDev());
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case STATUS:
                return isSetStatus();
            case ELAPSED:
                return isSetElapsed();
            case NUM:
                return isSetNum();
            case COUNT:
                return isSetCount();
            case SUM_DEV:
                return isSetSumDev();
            case FAIL:
                return isSetFail();
            case QUEUE_TIME:
                return isSetQueueTime();
            case QUEUE_SUM_DEV:
                return isSetQueueSumDev();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof ActionStats)
            return this.equals((ActionStats) that);
        return false;
    }

    public boolean equals(ActionStats that) {
        if (that == null)
            return false;
        boolean this_present_status = true;
        boolean that_present_status = true;
        if (this_present_status || that_present_status) {
            if (!(this_present_status && that_present_status))
                return false;
            if (this.status != that.status)
                return false;
        }
        boolean this_present_elapsed = true;
        boolean that_present_elapsed = true;
        if (this_present_elapsed || that_present_elapsed) {
            if (!(this_present_elapsed && that_present_elapsed))
                return false;
            if (this.elapsed != that.elapsed)
                return false;
        }
        boolean this_present_num = true;
        boolean that_present_num = true;
        if (this_present_num || that_present_num) {
            if (!(this_present_num && that_present_num))
                return false;
            if (this.num != that.num)
                return false;
        }
        boolean this_present_count = true;
        boolean that_present_count = true;
        if (this_present_count || that_present_count) {
            if (!(this_present_count && that_present_count))
                return false;
            if (this.count != that.count)
                return false;
        }
        boolean this_present_sumDev = true;
        boolean that_present_sumDev = true;
        if (this_present_sumDev || that_present_sumDev) {
            if (!(this_present_sumDev && that_present_sumDev))
                return false;
            if (this.sumDev != that.sumDev)
                return false;
        }
        boolean this_present_fail = true;
        boolean that_present_fail = true;
        if (this_present_fail || that_present_fail) {
            if (!(this_present_fail && that_present_fail))
                return false;
            if (this.fail != that.fail)
                return false;
        }
        boolean this_present_queueTime = true;
        boolean that_present_queueTime = true;
        if (this_present_queueTime || that_present_queueTime) {
            if (!(this_present_queueTime && that_present_queueTime))
                return false;
            if (this.queueTime != that.queueTime)
                return false;
        }
        boolean this_present_queueSumDev = true;
        boolean that_present_queueSumDev = true;
        if (this_present_queueSumDev || that_present_queueSumDev) {
            if (!(this_present_queueSumDev && that_present_queueSumDev))
                return false;
            if (this.queueSumDev != that.queueSumDev)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public int compareTo(ActionStats other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        ActionStats typedOther = (ActionStats) other;
        lastComparison = Boolean.valueOf(isSetStatus()).compareTo(typedOther.isSetStatus());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetStatus()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.status, typedOther.status);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetElapsed()).compareTo(typedOther.isSetElapsed());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetElapsed()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.elapsed, typedOther.elapsed);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetNum()).compareTo(typedOther.isSetNum());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetNum()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.num, typedOther.num);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetCount()).compareTo(typedOther.isSetCount());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetCount()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.count, typedOther.count);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetSumDev()).compareTo(typedOther.isSetSumDev());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetSumDev()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sumDev, typedOther.sumDev);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetFail()).compareTo(typedOther.isSetFail());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetFail()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.fail, typedOther.fail);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetQueueTime()).compareTo(typedOther.isSetQueueTime());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetQueueTime()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.queueTime, typedOther.queueTime);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetQueueSumDev()).compareTo(typedOther.isSetQueueSumDev());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetQueueSumDev()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.queueSumDev, typedOther.queueSumDev);
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
        StringBuilder sb = new StringBuilder("ActionStats(");
        boolean first = true;
        sb.append("status:");
        sb.append(this.status);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("elapsed:");
        sb.append(this.elapsed);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("num:");
        sb.append(this.num);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("count:");
        sb.append(this.count);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("sumDev:");
        sb.append(this.sumDev);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("fail:");
        sb.append(this.fail);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("queueTime:");
        sb.append(this.queueTime);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("queueSumDev:");
        sb.append(this.queueSumDev);
        first = false;
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
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

    private static class ActionStatsStandardSchemeFactory implements SchemeFactory {

        public ActionStatsStandardScheme getScheme() {
            return new ActionStatsStandardScheme();
        }
    }

    private static class ActionStatsStandardScheme extends StandardScheme<ActionStats> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, ActionStats struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch(schemeField.id) {
                    case 1:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.status = iprot.readI32();
                            struct.setStatusIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
                            struct.elapsed = iprot.readDouble();
                            struct.setElapsedIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.num = iprot.readI32();
                            struct.setNumIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.count = iprot.readI64();
                            struct.setCountIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 5:
                        if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
                            struct.sumDev = iprot.readDouble();
                            struct.setSumDevIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 6:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.fail = iprot.readI32();
                            struct.setFailIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 7:
                        if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
                            struct.queueTime = iprot.readDouble();
                            struct.setQueueTimeIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 8:
                        if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
                            struct.queueSumDev = iprot.readDouble();
                            struct.setQueueSumDevIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, ActionStats struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            oprot.writeFieldBegin(STATUS_FIELD_DESC);
            oprot.writeI32(struct.status);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(ELAPSED_FIELD_DESC);
            oprot.writeDouble(struct.elapsed);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(NUM_FIELD_DESC);
            oprot.writeI32(struct.num);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(COUNT_FIELD_DESC);
            oprot.writeI64(struct.count);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(SUM_DEV_FIELD_DESC);
            oprot.writeDouble(struct.sumDev);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(FAIL_FIELD_DESC);
            oprot.writeI32(struct.fail);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(QUEUE_TIME_FIELD_DESC);
            oprot.writeDouble(struct.queueTime);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(QUEUE_SUM_DEV_FIELD_DESC);
            oprot.writeDouble(struct.queueSumDev);
            oprot.writeFieldEnd();
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class ActionStatsTupleSchemeFactory implements SchemeFactory {

        public ActionStatsTupleScheme getScheme() {
            return new ActionStatsTupleScheme();
        }
    }

    private static class ActionStatsTupleScheme extends TupleScheme<ActionStats> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, ActionStats struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetStatus()) {
                optionals.set(0);
            }
            if (struct.isSetElapsed()) {
                optionals.set(1);
            }
            if (struct.isSetNum()) {
                optionals.set(2);
            }
            if (struct.isSetCount()) {
                optionals.set(3);
            }
            if (struct.isSetSumDev()) {
                optionals.set(4);
            }
            if (struct.isSetFail()) {
                optionals.set(5);
            }
            if (struct.isSetQueueTime()) {
                optionals.set(6);
            }
            if (struct.isSetQueueSumDev()) {
                optionals.set(7);
            }
            oprot.writeBitSet(optionals, 8);
            if (struct.isSetStatus()) {
                oprot.writeI32(struct.status);
            }
            if (struct.isSetElapsed()) {
                oprot.writeDouble(struct.elapsed);
            }
            if (struct.isSetNum()) {
                oprot.writeI32(struct.num);
            }
            if (struct.isSetCount()) {
                oprot.writeI64(struct.count);
            }
            if (struct.isSetSumDev()) {
                oprot.writeDouble(struct.sumDev);
            }
            if (struct.isSetFail()) {
                oprot.writeI32(struct.fail);
            }
            if (struct.isSetQueueTime()) {
                oprot.writeDouble(struct.queueTime);
            }
            if (struct.isSetQueueSumDev()) {
                oprot.writeDouble(struct.queueSumDev);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, ActionStats struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(8);
            if (incoming.get(0)) {
                struct.status = iprot.readI32();
                struct.setStatusIsSet(true);
            }
            if (incoming.get(1)) {
                struct.elapsed = iprot.readDouble();
                struct.setElapsedIsSet(true);
            }
            if (incoming.get(2)) {
                struct.num = iprot.readI32();
                struct.setNumIsSet(true);
            }
            if (incoming.get(3)) {
                struct.count = iprot.readI64();
                struct.setCountIsSet(true);
            }
            if (incoming.get(4)) {
                struct.sumDev = iprot.readDouble();
                struct.setSumDevIsSet(true);
            }
            if (incoming.get(5)) {
                struct.fail = iprot.readI32();
                struct.setFailIsSet(true);
            }
            if (incoming.get(6)) {
                struct.queueTime = iprot.readDouble();
                struct.setQueueTimeIsSet(true);
            }
            if (incoming.get(7)) {
                struct.queueSumDev = iprot.readDouble();
                struct.setQueueSumDevIsSet(true);
            }
        }
    }
}