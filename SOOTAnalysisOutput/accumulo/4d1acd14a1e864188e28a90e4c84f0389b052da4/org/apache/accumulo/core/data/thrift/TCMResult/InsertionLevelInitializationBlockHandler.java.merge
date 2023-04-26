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
public class TCMResult implements org.apache.thrift.TBase<TCMResult, TCMResult._Fields>, java.io.Serializable, Cloneable, Comparable<TCMResult> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TCMResult");

    private static final org.apache.thrift.protocol.TField CMID_FIELD_DESC = new org.apache.thrift.protocol.TField("cmid", org.apache.thrift.protocol.TType.I64, (short) 1);

    private static final org.apache.thrift.protocol.TField STATUS_FIELD_DESC = new org.apache.thrift.protocol.TField("status", org.apache.thrift.protocol.TType.I32, (short) 2);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new TCMResultStandardSchemeFactory());
        schemes.put(TupleScheme.class, new TCMResultTupleSchemeFactory());
    }

    public long cmid;

    public TCMStatus status;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        CMID((short) 1, "cmid"), STATUS((short) 2, "status");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return CMID;
                case 2:
                    return STATUS;
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

    private static final int __CMID_ISSET_ID = 0;

    private byte __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.CMID, new org.apache.thrift.meta_data.FieldMetaData("cmid", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.STATUS, new org.apache.thrift.meta_data.FieldMetaData("status", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, TCMStatus.class)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TCMResult.class, metaDataMap);
    }

    public TCMResult() {
    }

    public TCMResult(long cmid, TCMStatus status) {
        this();
        this.cmid = cmid;
        setCmidIsSet(true);
        this.status = status;
    }

    public TCMResult(TCMResult other) {
        __isset_bitfield = other.__isset_bitfield;
        this.cmid = other.cmid;
        if (other.isSetStatus()) {
            this.status = other.status;
        }
    }

    public TCMResult deepCopy() {
        return new TCMResult(this);
    }

    @Override
    public void clear() {
        setCmidIsSet(false);
        this.cmid = 0;
        this.status = null;
    }

    public long getCmid() {
        return this.cmid;
    }

    public TCMResult setCmid(long cmid) {
        this.cmid = cmid;
        setCmidIsSet(true);
        return this;
    }

    public void unsetCmid() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __CMID_ISSET_ID);
    }

    public boolean isSetCmid() {
        return EncodingUtils.testBit(__isset_bitfield, __CMID_ISSET_ID);
    }

    public void setCmidIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __CMID_ISSET_ID, value);
    }

    public TCMStatus getStatus() {
        return this.status;
    }

    public TCMResult setStatus(TCMStatus status) {
        this.status = status;
        return this;
    }

    public void unsetStatus() {
        this.status = null;
    }

    public boolean isSetStatus() {
        return this.status != null;
    }

    public void setStatusIsSet(boolean value) {
        if (!value) {
            this.status = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case CMID:
                if (value == null) {
                    unsetCmid();
                } else {
                    setCmid((Long) value);
                }
                break;
            case STATUS:
                if (value == null) {
                    unsetStatus();
                } else {
                    setStatus((TCMStatus) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case CMID:
                return Long.valueOf(getCmid());
            case STATUS:
                return getStatus();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case CMID:
                return isSetCmid();
            case STATUS:
                return isSetStatus();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof TCMResult)
            return this.equals((TCMResult) that);
        return false;
    }

    public boolean equals(TCMResult that) {
        if (that == null)
            return false;
        boolean this_present_cmid = true;
        boolean that_present_cmid = true;
        if (this_present_cmid || that_present_cmid) {
            if (!(this_present_cmid && that_present_cmid))
                return false;
            if (this.cmid != that.cmid)
                return false;
        }
        boolean this_present_status = true && this.isSetStatus();
        boolean that_present_status = true && that.isSetStatus();
        if (this_present_status || that_present_status) {
            if (!(this_present_status && that_present_status))
                return false;
            if (!this.status.equals(that.status))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int compareTo(TCMResult other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetCmid()).compareTo(other.isSetCmid());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetCmid()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.cmid, other.cmid);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetStatus()).compareTo(other.isSetStatus());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetStatus()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.status, other.status);
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
        StringBuilder sb = new StringBuilder("TCMResult(");
        boolean first = true;
        sb.append("cmid:");
        sb.append(this.cmid);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("status:");
        if (this.status == null) {
            sb.append("null");
        } else {
            sb.append(this.status);
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

    private static class TCMResultStandardSchemeFactory implements SchemeFactory {

        public TCMResultStandardScheme getScheme() {
            return new TCMResultStandardScheme();
        }
    }

    private static class TCMResultStandardScheme extends StandardScheme<TCMResult> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, TCMResult struct) throws org.apache.thrift.TException {
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
                            struct.cmid = iprot.readI64();
                            struct.setCmidIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.status = TCMStatus.findByValue(iprot.readI32());
                            struct.setStatusIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, TCMResult struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            oprot.writeFieldBegin(CMID_FIELD_DESC);
            oprot.writeI64(struct.cmid);
            oprot.writeFieldEnd();
            if (struct.status != null) {
                oprot.writeFieldBegin(STATUS_FIELD_DESC);
                oprot.writeI32(struct.status.getValue());
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class TCMResultTupleSchemeFactory implements SchemeFactory {

        public TCMResultTupleScheme getScheme() {
            return new TCMResultTupleScheme();
        }
    }

    private static class TCMResultTupleScheme extends TupleScheme<TCMResult> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, TCMResult struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetCmid()) {
                optionals.set(0);
            }
            if (struct.isSetStatus()) {
                optionals.set(1);
            }
            oprot.writeBitSet(optionals, 2);
            if (struct.isSetCmid()) {
                oprot.writeI64(struct.cmid);
            }
            if (struct.isSetStatus()) {
                oprot.writeI32(struct.status.getValue());
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, TCMResult struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(2);
            if (incoming.get(0)) {
                struct.cmid = iprot.readI64();
                struct.setCmidIsSet(true);
            }
            if (incoming.get(1)) {
                struct.status = TCMStatus.findByValue(iprot.readI32());
                struct.setStatusIsSet(true);
            }
        }
    }
}