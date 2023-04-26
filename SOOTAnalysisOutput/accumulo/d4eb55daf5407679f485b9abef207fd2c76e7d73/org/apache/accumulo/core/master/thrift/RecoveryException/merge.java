package org.apache.accumulo.core.master.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class RecoveryException extends org.apache.thrift.TException implements org.apache.thrift.TBase<RecoveryException, RecoveryException._Fields>, java.io.Serializable, Cloneable, Comparable<RecoveryException> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("RecoveryException");

    private static final org.apache.thrift.protocol.TField WHY_FIELD_DESC = new org.apache.thrift.protocol.TField("why", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new RecoveryExceptionStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new RecoveryExceptionTupleSchemeFactory();

    @org.apache.thrift.annotation.Nullable
    public java.lang.String why;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        WHY((short) 1, "why");

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
                    return WHY;
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
        tmpMap.put(_Fields.WHY, new org.apache.thrift.meta_data.FieldMetaData("why", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(RecoveryException.class, metaDataMap);
    }

    public RecoveryException() {
    }

    public RecoveryException(java.lang.String why) {
        this();
        this.why = why;
    }

    public RecoveryException(RecoveryException other) {
        if (other.isSetWhy()) {
            this.why = other.why;
        }
    }

    public RecoveryException deepCopy() {
        return new RecoveryException(this);
    }

    @Override
    public void clear() {
        this.why = null;
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.String getWhy() {
        return this.why;
    }

    public RecoveryException setWhy(@org.apache.thrift.annotation.Nullable java.lang.String why) {
        this.why = why;
        return this;
    }

    public void unsetWhy() {
        this.why = null;
    }

    public boolean isSetWhy() {
        return this.why != null;
    }

    public void setWhyIsSet(boolean value) {
        if (!value) {
            this.why = null;
        }
    }

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case WHY:
                if (value == null) {
                    unsetWhy();
                } else {
                    setWhy((java.lang.String) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case WHY:
                return getWhy();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }
        switch(field) {
            case WHY:
                return isSetWhy();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof RecoveryException)
            return this.equals((RecoveryException) that);
        return false;
    }

    public boolean equals(RecoveryException that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
        boolean this_present_why = true && this.isSetWhy();
        boolean that_present_why = true && that.isSetWhy();
        if (this_present_why || that_present_why) {
            if (!(this_present_why && that_present_why))
                return false;
            if (!this.why.equals(that.why))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 8191 + ((isSetWhy()) ? 131071 : 524287);
        if (isSetWhy())
            hashCode = hashCode * 8191 + why.hashCode();
        return hashCode;
    }

    @Override
    public int compareTo(RecoveryException other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetWhy()).compareTo(other.isSetWhy());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetWhy()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.why, other.why);
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("RecoveryException(");
        boolean first = true;
        sb.append("why:");
        if (this.why == null) {
            sb.append("null");
        } else {
            sb.append(this.why);
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

    private static class RecoveryExceptionStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public RecoveryExceptionStandardScheme getScheme() {
            return new RecoveryExceptionStandardScheme();
        }
    }

    private static class RecoveryExceptionStandardScheme extends org.apache.thrift.scheme.StandardScheme<RecoveryException> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, RecoveryException struct) throws org.apache.thrift.TException {
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
                            struct.why = iprot.readString();
                            struct.setWhyIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, RecoveryException struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.why != null) {
                oprot.writeFieldBegin(WHY_FIELD_DESC);
                oprot.writeString(struct.why);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class RecoveryExceptionTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public RecoveryExceptionTupleScheme getScheme() {
            return new RecoveryExceptionTupleScheme();
        }
    }

    private static class RecoveryExceptionTupleScheme extends org.apache.thrift.scheme.TupleScheme<RecoveryException> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, RecoveryException struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
            if (struct.isSetWhy()) {
                optionals.set(0);
            }
            oprot.writeBitSet(optionals, 1);
            if (struct.isSetWhy()) {
                oprot.writeString(struct.why);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, RecoveryException struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(1);
            if (incoming.get(0)) {
                struct.why = iprot.readString();
                struct.setWhyIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}
