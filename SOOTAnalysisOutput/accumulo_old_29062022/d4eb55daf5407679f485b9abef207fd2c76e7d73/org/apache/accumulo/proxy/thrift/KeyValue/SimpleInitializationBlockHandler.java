package org.apache.accumulo.proxy.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class KeyValue implements org.apache.thrift.TBase<KeyValue, KeyValue._Fields>, java.io.Serializable, Cloneable, Comparable<KeyValue> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("KeyValue");

    private static final org.apache.thrift.protocol.TField KEY_FIELD_DESC = new org.apache.thrift.protocol.TField("key", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

    private static final org.apache.thrift.protocol.TField VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("value", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new KeyValueStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new KeyValueTupleSchemeFactory();

    static {
        schemes.put(StandardScheme.class, new KeyValueStandardSchemeFactory());
        schemes.put(TupleScheme.class, new KeyValueTupleSchemeFactory());
    }

    @org.apache.thrift.annotation.Nullable
public Key key;

    @org.apache.thrift.annotation.Nullable
    public java.nio.ByteBuffer value;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        KEY((short) 1, "key"), VALUE((short) 2, "value");

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
                    return KEY;
                case 2:
                    return VALUE;
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
        tmpMap.put(_Fields.KEY, new org.apache.thrift.meta_data.FieldMetaData("key", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Key.class)));
        tmpMap.put(_Fields.VALUE, new org.apache.thrift.meta_data.FieldMetaData("value", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(KeyValue.class, metaDataMap);
    }

    public KeyValue() {
    }

    public KeyValue(Key key, java.nio.ByteBuffer value) {
        this();
        this.key = key;
        this.value = org.apache.thrift.TBaseHelper.copyBinary(value);
    }

    public KeyValue(KeyValue other) {
        if (other.isSetKey()) {
            this.key = new Key(other.key);
        }
        if (other.isSetValue()) {
            this.value = org.apache.thrift.TBaseHelper.copyBinary(other.value);
        }
    }

    public KeyValue deepCopy() {
        return new KeyValue(this);
    }

    @Override
    public void clear() {
        this.key = null;
        this.value = null;
    }

    @org.apache.thrift.annotation.Nullable
public Key getKey() {
        return this.key;
    }

    public KeyValue setKey(@org.apache.thrift.annotation.Nullable Key key) {
        this.key = key;
        return this;
    }

    public void unsetKey() {
        this.key = null;
    }

    public boolean isSetKey() {
        return this.key != null;
    }

    public void setKeyIsSet(boolean value) {
        if (!value) {
            this.key = null;
        }
    }

    public byte[] getValue() {
        setValue(org.apache.thrift.TBaseHelper.rightSize(value));
        return value == null ? null : value.array();
    }

    public java.nio.ByteBuffer bufferForValue() {
        return org.apache.thrift.TBaseHelper.copyBinary(value);
    }

    public KeyValue setValue(byte[] value) {
        this.value = value == null ? (java.nio.ByteBuffer) null : java.nio.ByteBuffer.wrap(value.clone());
        return this;
    }

    public KeyValue setValue(@org.apache.thrift.annotation.Nullable java.nio.ByteBuffer value) {
        this.value = org.apache.thrift.TBaseHelper.copyBinary(value);
        return this;
    }

    public void unsetValue() {
        this.value = null;
    }

    public boolean isSetValue() {
        return this.value != null;
    }

    public void setValueIsSet(boolean value) {
        if (!value) {
            this.value = null;
        }
    }

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case KEY:
                if (value == null) {
                    unsetKey();
                } else {
                    setKey((Key) value);
                }
                break;
            case VALUE:
                if (value == null) {
                    unsetValue();
                } else {
                    if (value instanceof byte[]) {
                        setValue((byte[]) value);
                    } else {
                        setValue((java.nio.ByteBuffer) value);
                    }
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case KEY:
                return getKey();
            case VALUE:
                return getValue();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }
        switch(field) {
            case KEY:
                return isSetKey();
            case VALUE:
                return isSetValue();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof KeyValue)
            return this.equals((KeyValue) that);
        return false;
    }

    public boolean equals(KeyValue that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
        boolean this_present_key = true && this.isSetKey();
        boolean that_present_key = true && that.isSetKey();
        if (this_present_key || that_present_key) {
            if (!(this_present_key && that_present_key))
                return false;
            if (!this.key.equals(that.key))
                return false;
        }
        boolean this_present_value = true && this.isSetValue();
        boolean that_present_value = true && that.isSetValue();
        if (this_present_value || that_present_value) {
            if (!(this_present_value && that_present_value))
                return false;
            if (!this.value.equals(that.value))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 8191 + ((isSetKey()) ? 131071 : 524287);
        if (isSetKey())
            hashCode = hashCode * 8191 + key.hashCode();
        hashCode = hashCode * 8191 + ((isSetValue()) ? 131071 : 524287);
        if (isSetValue())
            hashCode = hashCode * 8191 + value.hashCode();
        return hashCode;
    }

    @Override
    public int compareTo(KeyValue other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetKey()).compareTo(other.isSetKey());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetKey()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.key, other.key);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetValue()).compareTo(other.isSetValue());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetValue()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.value, other.value);
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("KeyValue(");
        boolean first = true;
        sb.append("key:");
        if (this.key == null) {
            sb.append("null");
        } else {
            sb.append(this.key);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("value:");
        if (this.value == null) {
            sb.append("null");
        } else {
            org.apache.thrift.TBaseHelper.toString(this.value, sb);
        }
        first = false;
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
        if (key != null) {
            key.validate();
        }
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

    private static class KeyValueStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public KeyValueStandardScheme getScheme() {
            return new KeyValueStandardScheme();
        }
    }

    private static class KeyValueStandardScheme extends org.apache.thrift.scheme.StandardScheme<KeyValue> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, KeyValue struct) throws org.apache.thrift.TException {
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
                            struct.key = new Key();
                            struct.key.read(iprot);
                            struct.setKeyIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.value = iprot.readBinary();
                            struct.setValueIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, KeyValue struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.key != null) {
                oprot.writeFieldBegin(KEY_FIELD_DESC);
                struct.key.write(oprot);
                oprot.writeFieldEnd();
            }
            if (struct.value != null) {
                oprot.writeFieldBegin(VALUE_FIELD_DESC);
                oprot.writeBinary(struct.value);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class KeyValueTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public KeyValueTupleScheme getScheme() {
            return new KeyValueTupleScheme();
        }
    }

    private static class KeyValueTupleScheme extends org.apache.thrift.scheme.TupleScheme<KeyValue> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, KeyValue struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
            if (struct.isSetKey()) {
                optionals.set(0);
            }
            if (struct.isSetValue()) {
                optionals.set(1);
            }
            oprot.writeBitSet(optionals, 2);
            if (struct.isSetKey()) {
                struct.key.write(oprot);
            }
            if (struct.isSetValue()) {
                oprot.writeBinary(struct.value);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, KeyValue struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(2);
            if (incoming.get(0)) {
                struct.key = new Key();
                struct.key.read(iprot);
                struct.setKeyIsSet(true);
            }
            if (incoming.get(1)) {
                struct.value = iprot.readBinary();
                struct.setValueIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}