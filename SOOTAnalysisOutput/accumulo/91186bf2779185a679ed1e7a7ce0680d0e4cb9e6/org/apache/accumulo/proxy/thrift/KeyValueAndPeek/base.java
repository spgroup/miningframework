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
public class KeyValueAndPeek implements org.apache.thrift.TBase<KeyValueAndPeek, KeyValueAndPeek._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("KeyValueAndPeek");

    private static final org.apache.thrift.protocol.TField KEY_VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("keyValue", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

    private static final org.apache.thrift.protocol.TField HAS_NEXT_FIELD_DESC = new org.apache.thrift.protocol.TField("hasNext", org.apache.thrift.protocol.TType.BOOL, (short) 2);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new KeyValueAndPeekStandardSchemeFactory());
        schemes.put(TupleScheme.class, new KeyValueAndPeekTupleSchemeFactory());
    }

    public KeyValue keyValue;

    public boolean hasNext;

    @SuppressWarnings("all")
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        KEY_VALUE((short) 1, "keyValue"), HAS_NEXT((short) 2, "hasNext");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return KEY_VALUE;
                case 2:
                    return HAS_NEXT;
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

    private static final int __HASNEXT_ISSET_ID = 0;

    private byte __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.KEY_VALUE, new org.apache.thrift.meta_data.FieldMetaData("keyValue", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, KeyValue.class)));
        tmpMap.put(_Fields.HAS_NEXT, new org.apache.thrift.meta_data.FieldMetaData("hasNext", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(KeyValueAndPeek.class, metaDataMap);
    }

    public KeyValueAndPeek() {
    }

    public KeyValueAndPeek(KeyValue keyValue, boolean hasNext) {
        this();
        this.keyValue = keyValue;
        this.hasNext = hasNext;
        setHasNextIsSet(true);
    }

    public KeyValueAndPeek(KeyValueAndPeek other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetKeyValue()) {
            this.keyValue = new KeyValue(other.keyValue);
        }
        this.hasNext = other.hasNext;
    }

    public KeyValueAndPeek deepCopy() {
        return new KeyValueAndPeek(this);
    }

    @Override
    public void clear() {
        this.keyValue = null;
        setHasNextIsSet(false);
        this.hasNext = false;
    }

    public KeyValue getKeyValue() {
        return this.keyValue;
    }

    public KeyValueAndPeek setKeyValue(KeyValue keyValue) {
        this.keyValue = keyValue;
        return this;
    }

    public void unsetKeyValue() {
        this.keyValue = null;
    }

    public boolean isSetKeyValue() {
        return this.keyValue != null;
    }

    public void setKeyValueIsSet(boolean value) {
        if (!value) {
            this.keyValue = null;
        }
    }

    public boolean isHasNext() {
        return this.hasNext;
    }

    public KeyValueAndPeek setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
        setHasNextIsSet(true);
        return this;
    }

    public void unsetHasNext() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __HASNEXT_ISSET_ID);
    }

    public boolean isSetHasNext() {
        return EncodingUtils.testBit(__isset_bitfield, __HASNEXT_ISSET_ID);
    }

    public void setHasNextIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __HASNEXT_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case KEY_VALUE:
                if (value == null) {
                    unsetKeyValue();
                } else {
                    setKeyValue((KeyValue) value);
                }
                break;
            case HAS_NEXT:
                if (value == null) {
                    unsetHasNext();
                } else {
                    setHasNext((Boolean) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case KEY_VALUE:
                return getKeyValue();
            case HAS_NEXT:
                return Boolean.valueOf(isHasNext());
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case KEY_VALUE:
                return isSetKeyValue();
            case HAS_NEXT:
                return isSetHasNext();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof KeyValueAndPeek)
            return this.equals((KeyValueAndPeek) that);
        return false;
    }

    public boolean equals(KeyValueAndPeek that) {
        if (that == null)
            return false;
        boolean this_present_keyValue = true && this.isSetKeyValue();
        boolean that_present_keyValue = true && that.isSetKeyValue();
        if (this_present_keyValue || that_present_keyValue) {
            if (!(this_present_keyValue && that_present_keyValue))
                return false;
            if (!this.keyValue.equals(that.keyValue))
                return false;
        }
        boolean this_present_hasNext = true;
        boolean that_present_hasNext = true;
        if (this_present_hasNext || that_present_hasNext) {
            if (!(this_present_hasNext && that_present_hasNext))
                return false;
            if (this.hasNext != that.hasNext)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public int compareTo(KeyValueAndPeek other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        KeyValueAndPeek typedOther = (KeyValueAndPeek) other;
        lastComparison = Boolean.valueOf(isSetKeyValue()).compareTo(typedOther.isSetKeyValue());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetKeyValue()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.keyValue, typedOther.keyValue);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetHasNext()).compareTo(typedOther.isSetHasNext());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetHasNext()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.hasNext, typedOther.hasNext);
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
        StringBuilder sb = new StringBuilder("KeyValueAndPeek(");
        boolean first = true;
        sb.append("keyValue:");
        if (this.keyValue == null) {
            sb.append("null");
        } else {
            sb.append(this.keyValue);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("hasNext:");
        sb.append(this.hasNext);
        first = false;
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
        if (keyValue != null) {
            keyValue.validate();
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
            __isset_bitfield = 0;
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class KeyValueAndPeekStandardSchemeFactory implements SchemeFactory {

        public KeyValueAndPeekStandardScheme getScheme() {
            return new KeyValueAndPeekStandardScheme();
        }
    }

    private static class KeyValueAndPeekStandardScheme extends StandardScheme<KeyValueAndPeek> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, KeyValueAndPeek struct) throws org.apache.thrift.TException {
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
                            struct.keyValue = new KeyValue();
                            struct.keyValue.read(iprot);
                            struct.setKeyValueIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
                            struct.hasNext = iprot.readBool();
                            struct.setHasNextIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, KeyValueAndPeek struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.keyValue != null) {
                oprot.writeFieldBegin(KEY_VALUE_FIELD_DESC);
                struct.keyValue.write(oprot);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(HAS_NEXT_FIELD_DESC);
            oprot.writeBool(struct.hasNext);
            oprot.writeFieldEnd();
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class KeyValueAndPeekTupleSchemeFactory implements SchemeFactory {

        public KeyValueAndPeekTupleScheme getScheme() {
            return new KeyValueAndPeekTupleScheme();
        }
    }

    private static class KeyValueAndPeekTupleScheme extends TupleScheme<KeyValueAndPeek> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, KeyValueAndPeek struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetKeyValue()) {
                optionals.set(0);
            }
            if (struct.isSetHasNext()) {
                optionals.set(1);
            }
            oprot.writeBitSet(optionals, 2);
            if (struct.isSetKeyValue()) {
                struct.keyValue.write(oprot);
            }
            if (struct.isSetHasNext()) {
                oprot.writeBool(struct.hasNext);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, KeyValueAndPeek struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(2);
            if (incoming.get(0)) {
                struct.keyValue = new KeyValue();
                struct.keyValue.read(iprot);
                struct.setKeyValueIsSet(true);
            }
            if (incoming.get(1)) {
                struct.hasNext = iprot.readBool();
                struct.setHasNextIsSet(true);
            }
        }
    }
}
