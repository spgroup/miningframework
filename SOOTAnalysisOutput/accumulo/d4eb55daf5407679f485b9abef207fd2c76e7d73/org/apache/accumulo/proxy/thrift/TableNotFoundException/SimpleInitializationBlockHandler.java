package org.apache.accumulo.proxy.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class TableNotFoundException extends org.apache.thrift.TException implements org.apache.thrift.TBase<TableNotFoundException, TableNotFoundException._Fields>, java.io.Serializable, Cloneable, Comparable<TableNotFoundException> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TableNotFoundException");

    private static final org.apache.thrift.protocol.TField MSG_FIELD_DESC = new org.apache.thrift.protocol.TField("msg", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new TableNotFoundExceptionStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new TableNotFoundExceptionTupleSchemeFactory();

    @org.apache.thrift.annotation.Nullable
    public java.lang.String msg;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        MSG((short) 1, "msg");

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
                    return MSG;
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
        tmpMap.put(_Fields.MSG, new org.apache.thrift.meta_data.FieldMetaData("msg", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TableNotFoundException.class, metaDataMap);
    }

    public TableNotFoundException() {
    }

    public TableNotFoundException(java.lang.String msg) {
        this();
        this.msg = msg;
    }

    public TableNotFoundException(TableNotFoundException other) {
        if (other.isSetMsg()) {
            this.msg = other.msg;
        }
    }

    public TableNotFoundException deepCopy() {
        return new TableNotFoundException(this);
    }

    @Override
    public void clear() {
        this.msg = null;
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.String getMsg() {
        return this.msg;
    }

    public TableNotFoundException setMsg(@org.apache.thrift.annotation.Nullable java.lang.String msg) {
        this.msg = msg;
        return this;
    }

    public void unsetMsg() {
        this.msg = null;
    }

    public boolean isSetMsg() {
        return this.msg != null;
    }

    public void setMsgIsSet(boolean value) {
        if (!value) {
            this.msg = null;
        }
    }

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case MSG:
                if (value == null) {
                    unsetMsg();
                } else {
                    setMsg((java.lang.String) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case MSG:
                return getMsg();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }
        switch(field) {
            case MSG:
                return isSetMsg();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof TableNotFoundException)
            return this.equals((TableNotFoundException) that);
        return false;
    }

    public boolean equals(TableNotFoundException that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
        boolean this_present_msg = true && this.isSetMsg();
        boolean that_present_msg = true && that.isSetMsg();
        if (this_present_msg || that_present_msg) {
            if (!(this_present_msg && that_present_msg))
                return false;
            if (!this.msg.equals(that.msg))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 8191 + ((isSetMsg()) ? 131071 : 524287);
        if (isSetMsg())
            hashCode = hashCode * 8191 + msg.hashCode();
        return hashCode;
    }

    @Override
    public int compareTo(TableNotFoundException other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetMsg()).compareTo(other.isSetMsg());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetMsg()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.msg, other.msg);
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("TableNotFoundException(");
        boolean first = true;
        sb.append("msg:");
        if (this.msg == null) {
            sb.append("null");
        } else {
            sb.append(this.msg);
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

    private static class TableNotFoundExceptionStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public TableNotFoundExceptionStandardScheme getScheme() {
            return new TableNotFoundExceptionStandardScheme();
        }
    }

    private static class TableNotFoundExceptionStandardScheme extends org.apache.thrift.scheme.StandardScheme<TableNotFoundException> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, TableNotFoundException struct) throws org.apache.thrift.TException {
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
                            struct.msg = iprot.readString();
                            struct.setMsgIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, TableNotFoundException struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.msg != null) {
                oprot.writeFieldBegin(MSG_FIELD_DESC);
                oprot.writeString(struct.msg);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class TableNotFoundExceptionTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public TableNotFoundExceptionTupleScheme getScheme() {
            return new TableNotFoundExceptionTupleScheme();
        }
    }

    private static class TableNotFoundExceptionTupleScheme extends org.apache.thrift.scheme.TupleScheme<TableNotFoundException> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, TableNotFoundException struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
            if (struct.isSetMsg()) {
                optionals.set(0);
            }
            oprot.writeBitSet(optionals, 1);
            if (struct.isSetMsg()) {
                oprot.writeString(struct.msg);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, TableNotFoundException struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(1);
            if (incoming.get(0)) {
                struct.msg = iprot.readString();
                struct.setMsgIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}