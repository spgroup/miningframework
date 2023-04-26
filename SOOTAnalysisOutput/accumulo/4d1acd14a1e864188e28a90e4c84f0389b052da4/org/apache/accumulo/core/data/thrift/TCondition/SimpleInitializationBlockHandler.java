package org.apache.accumulo.core.data.thrift;

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
public class TCondition implements org.apache.thrift.TBase<TCondition, TCondition._Fields>, java.io.Serializable, Cloneable, Comparable<TCondition> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TCondition");

    private static final org.apache.thrift.protocol.TField CF_FIELD_DESC = new org.apache.thrift.protocol.TField("cf", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final org.apache.thrift.protocol.TField CQ_FIELD_DESC = new org.apache.thrift.protocol.TField("cq", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.protocol.TField CV_FIELD_DESC = new org.apache.thrift.protocol.TField("cv", org.apache.thrift.protocol.TType.STRING, (short) 3);

    private static final org.apache.thrift.protocol.TField TS_FIELD_DESC = new org.apache.thrift.protocol.TField("ts", org.apache.thrift.protocol.TType.I64, (short) 4);

    private static final org.apache.thrift.protocol.TField HAS_TIMESTAMP_FIELD_DESC = new org.apache.thrift.protocol.TField("hasTimestamp", org.apache.thrift.protocol.TType.BOOL, (short) 5);

    private static final org.apache.thrift.protocol.TField VAL_FIELD_DESC = new org.apache.thrift.protocol.TField("val", org.apache.thrift.protocol.TType.STRING, (short) 6);

    private static final org.apache.thrift.protocol.TField ITERATORS_FIELD_DESC = new org.apache.thrift.protocol.TField("iterators", org.apache.thrift.protocol.TType.STRING, (short) 7);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new TConditionStandardSchemeFactory());
        schemes.put(TupleScheme.class, new TConditionTupleSchemeFactory());
    }

    public ByteBuffer cf;

    public ByteBuffer cq;

    public ByteBuffer cv;

    public long ts;

    public boolean hasTimestamp;

    public ByteBuffer val;

    public ByteBuffer iterators;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        CF((short) 1, "cf"),
        CQ((short) 2, "cq"),
        CV((short) 3, "cv"),
        TS((short) 4, "ts"),
        HAS_TIMESTAMP((short) 5, "hasTimestamp"),
        VAL((short) 6, "val"),
        ITERATORS((short) 7, "iterators");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return CF;
                case 2:
                    return CQ;
                case 3:
                    return CV;
                case 4:
                    return TS;
                case 5:
                    return HAS_TIMESTAMP;
                case 6:
                    return VAL;
                case 7:
                    return ITERATORS;
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

    private static final int __TS_ISSET_ID = 0;

    private static final int __HASTIMESTAMP_ISSET_ID = 1;

    private byte __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.CF, new org.apache.thrift.meta_data.FieldMetaData("cf", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.CQ, new org.apache.thrift.meta_data.FieldMetaData("cq", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.CV, new org.apache.thrift.meta_data.FieldMetaData("cv", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.TS, new org.apache.thrift.meta_data.FieldMetaData("ts", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.HAS_TIMESTAMP, new org.apache.thrift.meta_data.FieldMetaData("hasTimestamp", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
        tmpMap.put(_Fields.VAL, new org.apache.thrift.meta_data.FieldMetaData("val", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.ITERATORS, new org.apache.thrift.meta_data.FieldMetaData("iterators", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TCondition.class, metaDataMap);
    }

    public TCondition() {
    }

    public TCondition(ByteBuffer cf, ByteBuffer cq, ByteBuffer cv, long ts, boolean hasTimestamp, ByteBuffer val, ByteBuffer iterators) {
        this();
        this.cf = cf;
        this.cq = cq;
        this.cv = cv;
        this.ts = ts;
        setTsIsSet(true);
        this.hasTimestamp = hasTimestamp;
        setHasTimestampIsSet(true);
        this.val = val;
        this.iterators = iterators;
    }

    public TCondition(TCondition other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetCf()) {
            this.cf = org.apache.thrift.TBaseHelper.copyBinary(other.cf);
            ;
        }
        if (other.isSetCq()) {
            this.cq = org.apache.thrift.TBaseHelper.copyBinary(other.cq);
            ;
        }
        if (other.isSetCv()) {
            this.cv = org.apache.thrift.TBaseHelper.copyBinary(other.cv);
            ;
        }
        this.ts = other.ts;
        this.hasTimestamp = other.hasTimestamp;
        if (other.isSetVal()) {
            this.val = org.apache.thrift.TBaseHelper.copyBinary(other.val);
            ;
        }
        if (other.isSetIterators()) {
            this.iterators = org.apache.thrift.TBaseHelper.copyBinary(other.iterators);
            ;
        }
    }

    public TCondition deepCopy() {
        return new TCondition(this);
    }

    @Override
    public void clear() {
        this.cf = null;
        this.cq = null;
        this.cv = null;
        setTsIsSet(false);
        this.ts = 0;
        setHasTimestampIsSet(false);
        this.hasTimestamp = false;
        this.val = null;
        this.iterators = null;
    }

    public byte[] getCf() {
        setCf(org.apache.thrift.TBaseHelper.rightSize(cf));
        return cf == null ? null : cf.array();
    }

    public ByteBuffer bufferForCf() {
        return cf;
    }

    public TCondition setCf(byte[] cf) {
        setCf(cf == null ? (ByteBuffer) null : ByteBuffer.wrap(cf));
        return this;
    }

    public TCondition setCf(ByteBuffer cf) {
        this.cf = cf;
        return this;
    }

    public void unsetCf() {
        this.cf = null;
    }

    public boolean isSetCf() {
        return this.cf != null;
    }

    public void setCfIsSet(boolean value) {
        if (!value) {
            this.cf = null;
        }
    }

    public byte[] getCq() {
        setCq(org.apache.thrift.TBaseHelper.rightSize(cq));
        return cq == null ? null : cq.array();
    }

    public ByteBuffer bufferForCq() {
        return cq;
    }

    public TCondition setCq(byte[] cq) {
        setCq(cq == null ? (ByteBuffer) null : ByteBuffer.wrap(cq));
        return this;
    }

    public TCondition setCq(ByteBuffer cq) {
        this.cq = cq;
        return this;
    }

    public void unsetCq() {
        this.cq = null;
    }

    public boolean isSetCq() {
        return this.cq != null;
    }

    public void setCqIsSet(boolean value) {
        if (!value) {
            this.cq = null;
        }
    }

    public byte[] getCv() {
        setCv(org.apache.thrift.TBaseHelper.rightSize(cv));
        return cv == null ? null : cv.array();
    }

    public ByteBuffer bufferForCv() {
        return cv;
    }

    public TCondition setCv(byte[] cv) {
        setCv(cv == null ? (ByteBuffer) null : ByteBuffer.wrap(cv));
        return this;
    }

    public TCondition setCv(ByteBuffer cv) {
        this.cv = cv;
        return this;
    }

    public void unsetCv() {
        this.cv = null;
    }

    public boolean isSetCv() {
        return this.cv != null;
    }

    public void setCvIsSet(boolean value) {
        if (!value) {
            this.cv = null;
        }
    }

    public long getTs() {
        return this.ts;
    }

    public TCondition setTs(long ts) {
        this.ts = ts;
        setTsIsSet(true);
        return this;
    }

    public void unsetTs() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __TS_ISSET_ID);
    }

    public boolean isSetTs() {
        return EncodingUtils.testBit(__isset_bitfield, __TS_ISSET_ID);
    }

    public void setTsIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __TS_ISSET_ID, value);
    }

    public boolean isHasTimestamp() {
        return this.hasTimestamp;
    }

    public TCondition setHasTimestamp(boolean hasTimestamp) {
        this.hasTimestamp = hasTimestamp;
        setHasTimestampIsSet(true);
        return this;
    }

    public void unsetHasTimestamp() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __HASTIMESTAMP_ISSET_ID);
    }

    public boolean isSetHasTimestamp() {
        return EncodingUtils.testBit(__isset_bitfield, __HASTIMESTAMP_ISSET_ID);
    }

    public void setHasTimestampIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __HASTIMESTAMP_ISSET_ID, value);
    }

    public byte[] getVal() {
        setVal(org.apache.thrift.TBaseHelper.rightSize(val));
        return val == null ? null : val.array();
    }

    public ByteBuffer bufferForVal() {
        return val;
    }

    public TCondition setVal(byte[] val) {
        setVal(val == null ? (ByteBuffer) null : ByteBuffer.wrap(val));
        return this;
    }

    public TCondition setVal(ByteBuffer val) {
        this.val = val;
        return this;
    }

    public void unsetVal() {
        this.val = null;
    }

    public boolean isSetVal() {
        return this.val != null;
    }

    public void setValIsSet(boolean value) {
        if (!value) {
            this.val = null;
        }
    }

    public byte[] getIterators() {
        setIterators(org.apache.thrift.TBaseHelper.rightSize(iterators));
        return iterators == null ? null : iterators.array();
    }

    public ByteBuffer bufferForIterators() {
        return iterators;
    }

    public TCondition setIterators(byte[] iterators) {
        setIterators(iterators == null ? (ByteBuffer) null : ByteBuffer.wrap(iterators));
        return this;
    }

    public TCondition setIterators(ByteBuffer iterators) {
        this.iterators = iterators;
        return this;
    }

    public void unsetIterators() {
        this.iterators = null;
    }

    public boolean isSetIterators() {
        return this.iterators != null;
    }

    public void setIteratorsIsSet(boolean value) {
        if (!value) {
            this.iterators = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case CF:
                if (value == null) {
                    unsetCf();
                } else {
                    setCf((ByteBuffer) value);
                }
                break;
            case CQ:
                if (value == null) {
                    unsetCq();
                } else {
                    setCq((ByteBuffer) value);
                }
                break;
            case CV:
                if (value == null) {
                    unsetCv();
                } else {
                    setCv((ByteBuffer) value);
                }
                break;
            case TS:
                if (value == null) {
                    unsetTs();
                } else {
                    setTs((Long) value);
                }
                break;
            case HAS_TIMESTAMP:
                if (value == null) {
                    unsetHasTimestamp();
                } else {
                    setHasTimestamp((Boolean) value);
                }
                break;
            case VAL:
                if (value == null) {
                    unsetVal();
                } else {
                    setVal((ByteBuffer) value);
                }
                break;
            case ITERATORS:
                if (value == null) {
                    unsetIterators();
                } else {
                    setIterators((ByteBuffer) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case CF:
                return getCf();
            case CQ:
                return getCq();
            case CV:
                return getCv();
            case TS:
                return Long.valueOf(getTs());
            case HAS_TIMESTAMP:
                return Boolean.valueOf(isHasTimestamp());
            case VAL:
                return getVal();
            case ITERATORS:
                return getIterators();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case CF:
                return isSetCf();
            case CQ:
                return isSetCq();
            case CV:
                return isSetCv();
            case TS:
                return isSetTs();
            case HAS_TIMESTAMP:
                return isSetHasTimestamp();
            case VAL:
                return isSetVal();
            case ITERATORS:
                return isSetIterators();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof TCondition)
            return this.equals((TCondition) that);
        return false;
    }

    public boolean equals(TCondition that) {
        if (that == null)
            return false;
        boolean this_present_cf = true && this.isSetCf();
        boolean that_present_cf = true && that.isSetCf();
        if (this_present_cf || that_present_cf) {
            if (!(this_present_cf && that_present_cf))
                return false;
            if (!this.cf.equals(that.cf))
                return false;
        }
        boolean this_present_cq = true && this.isSetCq();
        boolean that_present_cq = true && that.isSetCq();
        if (this_present_cq || that_present_cq) {
            if (!(this_present_cq && that_present_cq))
                return false;
            if (!this.cq.equals(that.cq))
                return false;
        }
        boolean this_present_cv = true && this.isSetCv();
        boolean that_present_cv = true && that.isSetCv();
        if (this_present_cv || that_present_cv) {
            if (!(this_present_cv && that_present_cv))
                return false;
            if (!this.cv.equals(that.cv))
                return false;
        }
        boolean this_present_ts = true;
        boolean that_present_ts = true;
        if (this_present_ts || that_present_ts) {
            if (!(this_present_ts && that_present_ts))
                return false;
            if (this.ts != that.ts)
                return false;
        }
        boolean this_present_hasTimestamp = true;
        boolean that_present_hasTimestamp = true;
        if (this_present_hasTimestamp || that_present_hasTimestamp) {
            if (!(this_present_hasTimestamp && that_present_hasTimestamp))
                return false;
            if (this.hasTimestamp != that.hasTimestamp)
                return false;
        }
        boolean this_present_val = true && this.isSetVal();
        boolean that_present_val = true && that.isSetVal();
        if (this_present_val || that_present_val) {
            if (!(this_present_val && that_present_val))
                return false;
            if (!this.val.equals(that.val))
                return false;
        }
        boolean this_present_iterators = true && this.isSetIterators();
        boolean that_present_iterators = true && that.isSetIterators();
        if (this_present_iterators || that_present_iterators) {
            if (!(this_present_iterators && that_present_iterators))
                return false;
            if (!this.iterators.equals(that.iterators))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int compareTo(TCondition other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetCf()).compareTo(other.isSetCf());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetCf()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.cf, other.cf);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetCq()).compareTo(other.isSetCq());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetCq()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.cq, other.cq);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetCv()).compareTo(other.isSetCv());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetCv()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.cv, other.cv);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetTs()).compareTo(other.isSetTs());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTs()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ts, other.ts);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetHasTimestamp()).compareTo(other.isSetHasTimestamp());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetHasTimestamp()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.hasTimestamp, other.hasTimestamp);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetVal()).compareTo(other.isSetVal());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetVal()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.val, other.val);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetIterators()).compareTo(other.isSetIterators());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetIterators()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.iterators, other.iterators);
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
        StringBuilder sb = new StringBuilder("TCondition(");
        boolean first = true;
        sb.append("cf:");
        if (this.cf == null) {
            sb.append("null");
        } else {
            org.apache.thrift.TBaseHelper.toString(this.cf, sb);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("cq:");
        if (this.cq == null) {
            sb.append("null");
        } else {
            org.apache.thrift.TBaseHelper.toString(this.cq, sb);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("cv:");
        if (this.cv == null) {
            sb.append("null");
        } else {
            org.apache.thrift.TBaseHelper.toString(this.cv, sb);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("ts:");
        sb.append(this.ts);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("hasTimestamp:");
        sb.append(this.hasTimestamp);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("val:");
        if (this.val == null) {
            sb.append("null");
        } else {
            org.apache.thrift.TBaseHelper.toString(this.val, sb);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("iterators:");
        if (this.iterators == null) {
            sb.append("null");
        } else {
            org.apache.thrift.TBaseHelper.toString(this.iterators, sb);
        }
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

    private static class TConditionStandardSchemeFactory implements SchemeFactory {

        public TConditionStandardScheme getScheme() {
            return new TConditionStandardScheme();
        }
    }

    private static class TConditionStandardScheme extends StandardScheme<TCondition> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, TCondition struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch(schemeField.id) {
                    case 1:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.cf = iprot.readBinary();
                            struct.setCfIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.cq = iprot.readBinary();
                            struct.setCqIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.cv = iprot.readBinary();
                            struct.setCvIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.ts = iprot.readI64();
                            struct.setTsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 5:
                        if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
                            struct.hasTimestamp = iprot.readBool();
                            struct.setHasTimestampIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 6:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.val = iprot.readBinary();
                            struct.setValIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 7:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.iterators = iprot.readBinary();
                            struct.setIteratorsIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, TCondition struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.cf != null) {
                oprot.writeFieldBegin(CF_FIELD_DESC);
                oprot.writeBinary(struct.cf);
                oprot.writeFieldEnd();
            }
            if (struct.cq != null) {
                oprot.writeFieldBegin(CQ_FIELD_DESC);
                oprot.writeBinary(struct.cq);
                oprot.writeFieldEnd();
            }
            if (struct.cv != null) {
                oprot.writeFieldBegin(CV_FIELD_DESC);
                oprot.writeBinary(struct.cv);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(TS_FIELD_DESC);
            oprot.writeI64(struct.ts);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(HAS_TIMESTAMP_FIELD_DESC);
            oprot.writeBool(struct.hasTimestamp);
            oprot.writeFieldEnd();
            if (struct.val != null) {
                oprot.writeFieldBegin(VAL_FIELD_DESC);
                oprot.writeBinary(struct.val);
                oprot.writeFieldEnd();
            }
            if (struct.iterators != null) {
                oprot.writeFieldBegin(ITERATORS_FIELD_DESC);
                oprot.writeBinary(struct.iterators);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class TConditionTupleSchemeFactory implements SchemeFactory {

        public TConditionTupleScheme getScheme() {
            return new TConditionTupleScheme();
        }
    }

    private static class TConditionTupleScheme extends TupleScheme<TCondition> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, TCondition struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetCf()) {
                optionals.set(0);
            }
            if (struct.isSetCq()) {
                optionals.set(1);
            }
            if (struct.isSetCv()) {
                optionals.set(2);
            }
            if (struct.isSetTs()) {
                optionals.set(3);
            }
            if (struct.isSetHasTimestamp()) {
                optionals.set(4);
            }
            if (struct.isSetVal()) {
                optionals.set(5);
            }
            if (struct.isSetIterators()) {
                optionals.set(6);
            }
            oprot.writeBitSet(optionals, 7);
            if (struct.isSetCf()) {
                oprot.writeBinary(struct.cf);
            }
            if (struct.isSetCq()) {
                oprot.writeBinary(struct.cq);
            }
            if (struct.isSetCv()) {
                oprot.writeBinary(struct.cv);
            }
            if (struct.isSetTs()) {
                oprot.writeI64(struct.ts);
            }
            if (struct.isSetHasTimestamp()) {
                oprot.writeBool(struct.hasTimestamp);
            }
            if (struct.isSetVal()) {
                oprot.writeBinary(struct.val);
            }
            if (struct.isSetIterators()) {
                oprot.writeBinary(struct.iterators);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, TCondition struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(7);
            if (incoming.get(0)) {
                struct.cf = iprot.readBinary();
                struct.setCfIsSet(true);
            }
            if (incoming.get(1)) {
                struct.cq = iprot.readBinary();
                struct.setCqIsSet(true);
            }
            if (incoming.get(2)) {
                struct.cv = iprot.readBinary();
                struct.setCvIsSet(true);
            }
            if (incoming.get(3)) {
                struct.ts = iprot.readI64();
                struct.setTsIsSet(true);
            }
            if (incoming.get(4)) {
                struct.hasTimestamp = iprot.readBool();
                struct.setHasTimestampIsSet(true);
            }
            if (incoming.get(5)) {
                struct.val = iprot.readBinary();
                struct.setValIsSet(true);
            }
            if (incoming.get(6)) {
                struct.iterators = iprot.readBinary();
                struct.setIteratorsIsSet(true);
            }
        }
    }
}