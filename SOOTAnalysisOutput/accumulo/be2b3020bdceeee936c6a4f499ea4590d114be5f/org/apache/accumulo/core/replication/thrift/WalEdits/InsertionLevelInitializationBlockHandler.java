package org.apache.accumulo.core.replication.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class WalEdits implements org.apache.thrift.TBase<WalEdits, WalEdits._Fields>, java.io.Serializable, Cloneable, Comparable<WalEdits> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("WalEdits");

    private static final org.apache.thrift.protocol.TField EDITS_FIELD_DESC = new org.apache.thrift.protocol.TField("edits", org.apache.thrift.protocol.TType.LIST, (short) 1);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new WalEditsStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new WalEditsTupleSchemeFactory();

    @org.apache.thrift.annotation.Nullable
    public java.util.List<java.nio.ByteBuffer> edits;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        EDITS((short) 1, "edits");

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
                    return EDITS;
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
        tmpMap.put(_Fields.EDITS, new org.apache.thrift.meta_data.FieldMetaData("edits", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true))));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(WalEdits.class, metaDataMap);
    }

    public WalEdits() {
    }

    public WalEdits(java.util.List<java.nio.ByteBuffer> edits) {
        this();
        this.edits = edits;
    }

    public WalEdits(WalEdits other) {
        if (other.isSetEdits()) {
            java.util.List<java.nio.ByteBuffer> __this__edits = new java.util.ArrayList<java.nio.ByteBuffer>(other.edits);
            this.edits = __this__edits;
        }
    }

    public WalEdits deepCopy() {
        return new WalEdits(this);
    }

    @Override
    public void clear() {
        this.edits = null;
    }

    public int getEditsSize() {
        return (this.edits == null) ? 0 : this.edits.size();
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.Iterator<java.nio.ByteBuffer> getEditsIterator() {
        return (this.edits == null) ? null : this.edits.iterator();
    }

    public void addToEdits(java.nio.ByteBuffer elem) {
        if (this.edits == null) {
            this.edits = new java.util.ArrayList<java.nio.ByteBuffer>();
        }
        this.edits.add(elem);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.List<java.nio.ByteBuffer> getEdits() {
        return this.edits;
    }

    public WalEdits setEdits(@org.apache.thrift.annotation.Nullable java.util.List<java.nio.ByteBuffer> edits) {
        this.edits = edits;
        return this;
    }

    public void unsetEdits() {
        this.edits = null;
    }

    public boolean isSetEdits() {
        return this.edits != null;
    }

    public void setEditsIsSet(boolean value) {
        if (!value) {
            this.edits = null;
        }
    }

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case EDITS:
                if (value == null) {
                    unsetEdits();
                } else {
                    setEdits((java.util.List<java.nio.ByteBuffer>) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case EDITS:
                return getEdits();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }
        switch(field) {
            case EDITS:
                return isSetEdits();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof WalEdits)
            return this.equals((WalEdits) that);
        return false;
    }

    public boolean equals(WalEdits that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
        boolean this_present_edits = true && this.isSetEdits();
        boolean that_present_edits = true && that.isSetEdits();
        if (this_present_edits || that_present_edits) {
            if (!(this_present_edits && that_present_edits))
                return false;
            if (!this.edits.equals(that.edits))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 8191 + ((isSetEdits()) ? 131071 : 524287);
        if (isSetEdits())
            hashCode = hashCode * 8191 + edits.hashCode();
        return hashCode;
    }

    @Override
    public int compareTo(WalEdits other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetEdits()).compareTo(other.isSetEdits());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetEdits()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.edits, other.edits);
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("WalEdits(");
        boolean first = true;
        sb.append("edits:");
        if (this.edits == null) {
            sb.append("null");
        } else {
            org.apache.thrift.TBaseHelper.toString(this.edits, sb);
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

    private static class WalEditsStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public WalEditsStandardScheme getScheme() {
            return new WalEditsStandardScheme();
        }
    }

    private static class WalEditsStandardScheme extends org.apache.thrift.scheme.StandardScheme<WalEdits> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, WalEdits struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch(schemeField.id) {
                    case 1:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list0 = iprot.readListBegin();
                                struct.edits = new java.util.ArrayList<java.nio.ByteBuffer>(_list0.size);
                                @org.apache.thrift.annotation.Nullable
                                java.nio.ByteBuffer _elem1;
                                for (int _i2 = 0; _i2 < _list0.size; ++_i2) {
                                    _elem1 = iprot.readBinary();
                                    struct.edits.add(_elem1);
                                }
                                iprot.readListEnd();
                            }
                            struct.setEditsIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, WalEdits struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.edits != null) {
                oprot.writeFieldBegin(EDITS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.edits.size()));
                    for (java.nio.ByteBuffer _iter3 : struct.edits) {
                        oprot.writeBinary(_iter3);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class WalEditsTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public WalEditsTupleScheme getScheme() {
            return new WalEditsTupleScheme();
        }
    }

    private static class WalEditsTupleScheme extends org.apache.thrift.scheme.TupleScheme<WalEdits> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, WalEdits struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
            if (struct.isSetEdits()) {
                optionals.set(0);
            }
            oprot.writeBitSet(optionals, 1);
            if (struct.isSetEdits()) {
                {
                    oprot.writeI32(struct.edits.size());
                    for (java.nio.ByteBuffer _iter4 : struct.edits) {
                        oprot.writeBinary(_iter4);
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, WalEdits struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(1);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TList _list5 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.edits = new java.util.ArrayList<java.nio.ByteBuffer>(_list5.size);
                    @org.apache.thrift.annotation.Nullable
                    java.nio.ByteBuffer _elem6;
                    for (int _i7 = 0; _i7 < _list5.size; ++_i7) {
                        _elem6 = iprot.readBinary();
                        struct.edits.add(_elem6);
                    }
                }
                struct.setEditsIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}