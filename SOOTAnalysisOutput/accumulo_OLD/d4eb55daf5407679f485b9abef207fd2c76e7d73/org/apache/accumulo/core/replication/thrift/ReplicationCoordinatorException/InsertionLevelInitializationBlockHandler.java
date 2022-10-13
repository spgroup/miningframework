package org.apache.accumulo.core.replication.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class ReplicationCoordinatorException extends org.apache.thrift.TException implements org.apache.thrift.TBase<ReplicationCoordinatorException, ReplicationCoordinatorException._Fields>, java.io.Serializable, Cloneable, Comparable<ReplicationCoordinatorException> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ReplicationCoordinatorException");

    private static final org.apache.thrift.protocol.TField CODE_FIELD_DESC = new org.apache.thrift.protocol.TField("code", org.apache.thrift.protocol.TType.I32, (short) 1);

    private static final org.apache.thrift.protocol.TField REASON_FIELD_DESC = new org.apache.thrift.protocol.TField("reason", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new ReplicationCoordinatorExceptionStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new ReplicationCoordinatorExceptionTupleSchemeFactory();

    @org.apache.thrift.annotation.Nullable
public ReplicationCoordinatorErrorCode code;

    @org.apache.thrift.annotation.Nullable
    public java.lang.String reason;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        CODE((short) 1, "code"), REASON((short) 2, "reason");

        private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

        static {
            for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        @org.apache.thrift.annotation.Nullable
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
                throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
            return fields;
        }

        @org.apache.thrift.annotation.Nullable
        public static _Fields findByName(java.lang.String name) {
            return byName.get(name);
        }

        private final short _thriftId;

        private final java.lang.String _fieldName;

        _Fields(short thriftId, java.lang.String fieldName) {
            _thriftId = thriftId;
            _fieldName = fieldName;
        }

        public short getThriftFieldId() {
            return _thriftId;
        }

        public java.lang.String getFieldName() {
            return _fieldName;
        }
    }

    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.CODE, new org.apache.thrift.meta_data.FieldMetaData("code", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, ReplicationCoordinatorErrorCode.class)));
        tmpMap.put(_Fields.REASON, new org.apache.thrift.meta_data.FieldMetaData("reason", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ReplicationCoordinatorException.class, metaDataMap);
    }

    public ReplicationCoordinatorException() {
    }

    public ReplicationCoordinatorException(ReplicationCoordinatorErrorCode code, java.lang.String reason) {
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

    @org.apache.thrift.annotation.Nullable
public ReplicationCoordinatorErrorCode getCode() {
        return this.code;
    }

    public ReplicationCoordinatorException setCode(@org.apache.thrift.annotation.Nullable ReplicationCoordinatorErrorCode code) {
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

    @org.apache.thrift.annotation.Nullable
    public java.lang.String getReason() {
        return this.reason;
    }

    public ReplicationCoordinatorException setReason(@org.apache.thrift.annotation.Nullable java.lang.String reason) {
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

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
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
                    setReason((java.lang.String) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case CODE:
                return getCode();
            case REASON:
                return getReason();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }
        switch(field) {
            case CODE:
                return isSetCode();
            case REASON:
                return isSetReason();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof ReplicationCoordinatorException)
            return this.equals((ReplicationCoordinatorException) that);
        return false;
    }

    public boolean equals(ReplicationCoordinatorException that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
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
        int hashCode = 1;
        hashCode = hashCode * 8191 + ((isSetCode()) ? 131071 : 524287);
        if (isSetCode())
            hashCode = hashCode * 8191 + code.getValue();
        hashCode = hashCode * 8191 + ((isSetReason()) ? 131071 : 524287);
        if (isSetReason())
            hashCode = hashCode * 8191 + reason.hashCode();
        return hashCode;
    }

    @Override
    public int compareTo(ReplicationCoordinatorException other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetCode()).compareTo(other.isSetCode());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetCode()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.code, other.code);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetReason()).compareTo(other.isSetReason());
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

    @org.apache.thrift.annotation.Nullable
public _Fields fieldForId(int fieldId) {
        return _Fields.findByThriftId(fieldId);
    }

    public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
        scheme(iprot).read(iprot, this);
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
        scheme(oprot).write(oprot, this);
    }

    @Override
    public java.lang.String toString() {
        java.lang.StringBuilder sb = new java.lang.StringBuilder("ReplicationCoordinatorException(");
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

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
        try {
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class ReplicationCoordinatorExceptionStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public ReplicationCoordinatorExceptionStandardScheme getScheme() {
            return new ReplicationCoordinatorExceptionStandardScheme();
        }
    }

    private static class ReplicationCoordinatorExceptionStandardScheme extends org.apache.thrift.scheme.StandardScheme<ReplicationCoordinatorException> {

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

    private static class ReplicationCoordinatorExceptionTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public ReplicationCoordinatorExceptionTupleScheme getScheme() {
            return new ReplicationCoordinatorExceptionTupleScheme();
        }
    }

    private static class ReplicationCoordinatorExceptionTupleScheme extends org.apache.thrift.scheme.TupleScheme<ReplicationCoordinatorException> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, ReplicationCoordinatorException struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
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
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(2);
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

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}