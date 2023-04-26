package org.apache.accumulo.core.replication.thrift;

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

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class ReplicationCoordinatorException extends TException implements org.apache.thrift.TBase<ReplicationCoordinatorException, ReplicationCoordinatorException._Fields>, java.io.Serializable, Cloneable, Comparable<ReplicationCoordinatorException> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ReplicationCoordinatorException");

    private static final org.apache.thrift.protocol.TField CODE_FIELD_DESC = new org.apache.thrift.protocol.TField("code", org.apache.thrift.protocol.TType.I32, (short) 1);

    private static final org.apache.thrift.protocol.TField REASON_FIELD_DESC = new org.apache.thrift.protocol.TField("reason", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new ReplicationCoordinatorExceptionStandardSchemeFactory());
        schemes.put(TupleScheme.class, new ReplicationCoordinatorExceptionTupleSchemeFactory());
    }

    public ReplicationCoordinatorErrorCode code;

    public String reason;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        CODE((short) 1, "code"), REASON((short) 2, "reason");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return CODE;
                case 2:
                    return REASON;
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

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.CODE, new org.apache.thrift.meta_data.FieldMetaData("code", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, ReplicationCoordinatorErrorCode.class)));
        tmpMap.put(_Fields.REASON, new org.apache.thrift.meta_data.FieldMetaData("reason", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ReplicationCoordinatorException.class, metaDataMap);
    }

    public ReplicationCoordinatorException() {
    }

    public ReplicationCoordinatorException(ReplicationCoordinatorErrorCode code, String reason) {
        this();
        this.code = code;
        this.reason = reason;
    }

    public ReplicationCoordinatorException(ReplicationCoordinatorException other) {
        if (other.isSetCode()) {
            this.code = other.code;
        }
        if (other.isSetReason()) {
            this.reason = other.reason;
        }
    }

    public ReplicationCoordinatorException deepCopy() {
        return new ReplicationCoordinatorException(this);
    }

    @Override
    public void clear() {
        this.code = null;
        this.reason = null;
    }

    public ReplicationCoordinatorErrorCode getCode() {
        return this.code;
    }

    public ReplicationCoordinatorException setCode(ReplicationCoordinatorErrorCode code) {
        this.code = code;
        return this;
    }

    public void unsetCode() {
        this.code = null;
    }

    public boolean isSetCode() {
        return this.code != null;
    }

    public void setCodeIsSet(boolean value) {
        if (!value) {
            this.code = null;
        }
    }

    public String getReason() {
        return this.reason;
    }

    public ReplicationCoordinatorException setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public void unsetReason() {
        this.reason = null;
    }

    public boolean isSetReason() {
        return this.reason != null;
    }

    public void setReasonIsSet(boolean value) {
        if (!value) {
            this.reason = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case CODE:
                if (value == null) {
                    unsetCode();
                } else {
                    setCode((ReplicationCoordinatorErrorCode) value);
                }
                break;
            case REASON:
                if (value == null) {
                    unsetReason();
                } else {
                    setReason((String) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case CODE:
                return getCode();
            case REASON:
                return getReason();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case CODE:
                return isSetCode();
            case REASON:
                return isSetReason();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof ReplicationCoordinatorException)
            return this.equals((ReplicationCoordinatorException) that);
        return false;
    }

    public boolean equals(ReplicationCoordinatorException that) {
        if (that == null)
            return false;
        boolean this_present_code = true && this.isSetCode();
        boolean that_present_code = true && that.isSetCode();
        if (this_present_code || that_present_code) {
            if (!(this_present_code && that_present_code))
                return false;
            if (!this.code.equals(that.code))
                return false;
        }
        boolean this_present_reason = true && this.isSetReason();
        boolean that_present_reason = true && that.isSetReason();
        if (this_present_reason || that_present_reason) {
            if (!(this_present_reason && that_present_reason))
                return false;
            if (!this.reason.equals(that.reason))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        List<Object> list = new ArrayList<Object>();
        boolean present_code = true && (isSetCode());
        list.add(present_code);
        if (present_code)
            list.add(code.getValue());
        boolean present_reason = true && (isSetReason());
        list.add(present_reason);
        if (present_reason)
            list.add(reason);
        return list.hashCode();
    }

    @Override
    public int compareTo(ReplicationCoordinatorException other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetCode()).compareTo(other.isSetCode());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetCode()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.code, other.code);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetReason()).compareTo(other.isSetReason());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetReason()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.reason, other.reason);
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
        StringBuilder sb = new StringBuilder("ReplicationCoordinatorException(");
        boolean first = true;
        sb.append("code:");
        if (this.code == null) {
            sb.append("null");
        } else {
            sb.append(this.code);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("reason:");
        if (this.reason == null) {
            sb.append("null");
        } else {
            sb.append(this.reason);
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
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class ReplicationCoordinatorExceptionStandardSchemeFactory implements SchemeFactory {

        public ReplicationCoordinatorExceptionStandardScheme getScheme() {
            return new ReplicationCoordinatorExceptionStandardScheme();
        }
    }

    private static class ReplicationCoordinatorExceptionStandardScheme extends StandardScheme<ReplicationCoordinatorException> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, ReplicationCoordinatorException struct) throws org.apache.thrift.TException {
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
                            struct.code = org.apache.accumulo.core.replication.thrift.ReplicationCoordinatorErrorCode.findByValue(iprot.readI32());
                            struct.setCodeIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.reason = iprot.readString();
                            struct.setReasonIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, ReplicationCoordinatorException struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.code != null) {
                oprot.writeFieldBegin(CODE_FIELD_DESC);
                oprot.writeI32(struct.code.getValue());
                oprot.writeFieldEnd();
            }
            if (struct.reason != null) {
                oprot.writeFieldBegin(REASON_FIELD_DESC);
                oprot.writeString(struct.reason);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class ReplicationCoordinatorExceptionTupleSchemeFactory implements SchemeFactory {

        public ReplicationCoordinatorExceptionTupleScheme getScheme() {
            return new ReplicationCoordinatorExceptionTupleScheme();
        }
    }

    private static class ReplicationCoordinatorExceptionTupleScheme extends TupleScheme<ReplicationCoordinatorException> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, ReplicationCoordinatorException struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetCode()) {
                optionals.set(0);
            }
            if (struct.isSetReason()) {
                optionals.set(1);
            }
            oprot.writeBitSet(optionals, 2);
            if (struct.isSetCode()) {
                oprot.writeI32(struct.code.getValue());
            }
            if (struct.isSetReason()) {
                oprot.writeString(struct.reason);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, ReplicationCoordinatorException struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(2);
            if (incoming.get(0)) {
                struct.code = org.apache.accumulo.core.replication.thrift.ReplicationCoordinatorErrorCode.findByValue(iprot.readI32());
                struct.setCodeIsSet(true);
            }
            if (incoming.get(1)) {
                struct.reason = iprot.readString();
                struct.setReasonIsSet(true);
            }
        }
    }
}
