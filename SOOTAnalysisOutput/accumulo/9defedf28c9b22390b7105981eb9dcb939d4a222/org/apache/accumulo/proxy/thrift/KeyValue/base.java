package org.apache.accumulo.proxy.thrift;

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
public class KeyValue implements org.apache.thrift.TBase<KeyValue, KeyValue._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("KeyValue");

    private static final org.apache.thrift.protocol.TField KEY_FIELD_DESC = new org.apache.thrift.protocol.TField("key", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

    private static final org.apache.thrift.protocol.TField VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("value", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new KeyValueStandardSchemeFactory());
        schemes.put(TupleScheme.class, new KeyValueTupleSchemeFactory());
    }

    public Key key;

    public ByteBuffer value;

    @SuppressWarnings("all")
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        KEY((short) 1, "key"), VALUE((short) 2, "value");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

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
        tmpMap.put(_Fields.KEY, new org.apache.thrift.meta_data.FieldMetaData("key", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Key.class)));
        tmpMap.put(_Fields.VALUE, new org.apache.thrift.meta_data.FieldMetaData("value", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(KeyValue.class, metaDataMap);
    }

    public KeyValue() {
    }

    public KeyValue(Key key, ByteBuffer value) {
        this();
        this.key = key;
        this.value = value;
    }

    public KeyValue(KeyValue other) {
        if (other.isSetKey()) {
            this.key = new Key(other.key);
        }
        if (other.isSetValue()) {
            this.value = org.apache.thrift.TBaseHelper.copyBinary(other.value);
            ;
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

    public Key getKey() {
        return this.key;
    }

    public KeyValue setKey(Key key) {
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

    public ByteBuffer bufferForValue() {
        return value;
    }

    public KeyValue setValue(byte[] value) {
        setValue(value == null ? (ByteBuffer) null : ByteBuffer.wrap(value));
        return this;
    }

    public KeyValue setValue(ByteBuffer value) {
        this.value = value;
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

    public void setFieldValue(_Fields field, Object value) {
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
                    setValue((ByteBuffer) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case KEY:
                return getKey();
            case VALUE:
                return getValue();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case KEY:
                return isSetKey();
            case VALUE:
                return isSetValue();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof KeyValue)
            return this.equals((KeyValue) that);
        return false;
    }

    public boolean equals(KeyValue that) {
        if (that == null)
            return false;
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
        return 0;
    }

    public int compareTo(KeyValue other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        KeyValue typedOther = (KeyValue) other;
        lastComparison = Boolean.valueOf(isSetKey()).compareTo(typedOther.isSetKey());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetKey()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.key, typedOther.key);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetValue()).compareTo(typedOther.isSetValue());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetValue()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.value, typedOther.value);
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
        StringBuilder sb = new StringBuilder("KeyValue(");
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

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        try {
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class KeyValueStandardSchemeFactory implements SchemeFactory {

        public KeyValueStandardScheme getScheme() {
            return new KeyValueStandardScheme();
        }
    }

    private static class KeyValueStandardScheme extends StandardScheme<KeyValue> {

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

    private static class KeyValueTupleSchemeFactory implements SchemeFactory {

        public KeyValueTupleScheme getScheme() {
            return new KeyValueTupleScheme();
        }
    }

    private static class KeyValueTupleScheme extends TupleScheme<KeyValue> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, KeyValue struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
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
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(2);
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
}
