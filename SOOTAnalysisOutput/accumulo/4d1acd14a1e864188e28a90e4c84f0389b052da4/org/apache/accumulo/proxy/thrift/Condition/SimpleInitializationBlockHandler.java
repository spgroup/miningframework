package org.apache.accumulo.proxy.thrift;

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
public class Condition implements org.apache.thrift.TBase<Condition, Condition._Fields>, java.io.Serializable, Cloneable, Comparable<Condition> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Condition");

    private static final org.apache.thrift.protocol.TField COLUMN_FIELD_DESC = new org.apache.thrift.protocol.TField("column", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

    private static final org.apache.thrift.protocol.TField TIMESTAMP_FIELD_DESC = new org.apache.thrift.protocol.TField("timestamp", org.apache.thrift.protocol.TType.I64, (short) 2);

    private static final org.apache.thrift.protocol.TField VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("value", org.apache.thrift.protocol.TType.STRING, (short) 3);

    private static final org.apache.thrift.protocol.TField ITERATORS_FIELD_DESC = new org.apache.thrift.protocol.TField("iterators", org.apache.thrift.protocol.TType.LIST, (short) 4);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new ConditionStandardSchemeFactory());
        schemes.put(TupleScheme.class, new ConditionTupleSchemeFactory());
    }

    public Column column;

    public long timestamp;

    public ByteBuffer value;

    public List<IteratorSetting> iterators;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        COLUMN((short) 1, "column"), TIMESTAMP((short) 2, "timestamp"), VALUE((short) 3, "value"), ITERATORS((short) 4, "iterators");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return COLUMN;
                case 2:
                    return TIMESTAMP;
                case 3:
                    return VALUE;
                case 4:
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

    private static final int __TIMESTAMP_ISSET_ID = 0;

    private byte __isset_bitfield = 0;

    private _Fields[] optionals = { _Fields.TIMESTAMP, _Fields.VALUE, _Fields.ITERATORS };

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.COLUMN, new org.apache.thrift.meta_data.FieldMetaData("column", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Column.class)));
        tmpMap.put(_Fields.TIMESTAMP, new org.apache.thrift.meta_data.FieldMetaData("timestamp", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.VALUE, new org.apache.thrift.meta_data.FieldMetaData("value", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.ITERATORS, new org.apache.thrift.meta_data.FieldMetaData("iterators", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, IteratorSetting.class))));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Condition.class, metaDataMap);
    }

    public Condition() {
    }

    public Condition(Column column) {
        this();
        this.column = column;
    }

    public Condition(Condition other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetColumn()) {
            this.column = new Column(other.column);
        }
        this.timestamp = other.timestamp;
        if (other.isSetValue()) {
            this.value = org.apache.thrift.TBaseHelper.copyBinary(other.value);
            ;
        }
        if (other.isSetIterators()) {
            List<IteratorSetting> __this__iterators = new ArrayList<IteratorSetting>(other.iterators.size());
            for (IteratorSetting other_element : other.iterators) {
                __this__iterators.add(new IteratorSetting(other_element));
            }
            this.iterators = __this__iterators;
        }
    }

    public Condition deepCopy() {
        return new Condition(this);
    }

    @Override
    public void clear() {
        this.column = null;
        setTimestampIsSet(false);
        this.timestamp = 0;
        this.value = null;
        this.iterators = null;
    }

    public Column getColumn() {
        return this.column;
    }

    public Condition setColumn(Column column) {
        this.column = column;
        return this;
    }

    public void unsetColumn() {
        this.column = null;
    }

    public boolean isSetColumn() {
        return this.column != null;
    }

    public void setColumnIsSet(boolean value) {
        if (!value) {
            this.column = null;
        }
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public Condition setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        setTimestampIsSet(true);
        return this;
    }

    public void unsetTimestamp() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __TIMESTAMP_ISSET_ID);
    }

    public boolean isSetTimestamp() {
        return EncodingUtils.testBit(__isset_bitfield, __TIMESTAMP_ISSET_ID);
    }

    public void setTimestampIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __TIMESTAMP_ISSET_ID, value);
    }

    public byte[] getValue() {
        setValue(org.apache.thrift.TBaseHelper.rightSize(value));
        return value == null ? null : value.array();
    }

    public ByteBuffer bufferForValue() {
        return value;
    }

    public Condition setValue(byte[] value) {
        setValue(value == null ? (ByteBuffer) null : ByteBuffer.wrap(value));
        return this;
    }

    public Condition setValue(ByteBuffer value) {
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

    public int getIteratorsSize() {
        return (this.iterators == null) ? 0 : this.iterators.size();
    }

    public java.util.Iterator<IteratorSetting> getIteratorsIterator() {
        return (this.iterators == null) ? null : this.iterators.iterator();
    }

    public void addToIterators(IteratorSetting elem) {
        if (this.iterators == null) {
            this.iterators = new ArrayList<IteratorSetting>();
        }
        this.iterators.add(elem);
    }

    public List<IteratorSetting> getIterators() {
        return this.iterators;
    }

    public Condition setIterators(List<IteratorSetting> iterators) {
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
            case COLUMN:
                if (value == null) {
                    unsetColumn();
                } else {
                    setColumn((Column) value);
                }
                break;
            case TIMESTAMP:
                if (value == null) {
                    unsetTimestamp();
                } else {
                    setTimestamp((Long) value);
                }
                break;
            case VALUE:
                if (value == null) {
                    unsetValue();
                } else {
                    setValue((ByteBuffer) value);
                }
                break;
            case ITERATORS:
                if (value == null) {
                    unsetIterators();
                } else {
                    setIterators((List<IteratorSetting>) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case COLUMN:
                return getColumn();
            case TIMESTAMP:
                return Long.valueOf(getTimestamp());
            case VALUE:
                return getValue();
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
            case COLUMN:
                return isSetColumn();
            case TIMESTAMP:
                return isSetTimestamp();
            case VALUE:
                return isSetValue();
            case ITERATORS:
                return isSetIterators();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof Condition)
            return this.equals((Condition) that);
        return false;
    }

    public boolean equals(Condition that) {
        if (that == null)
            return false;
        boolean this_present_column = true && this.isSetColumn();
        boolean that_present_column = true && that.isSetColumn();
        if (this_present_column || that_present_column) {
            if (!(this_present_column && that_present_column))
                return false;
            if (!this.column.equals(that.column))
                return false;
        }
        boolean this_present_timestamp = true && this.isSetTimestamp();
        boolean that_present_timestamp = true && that.isSetTimestamp();
        if (this_present_timestamp || that_present_timestamp) {
            if (!(this_present_timestamp && that_present_timestamp))
                return false;
            if (this.timestamp != that.timestamp)
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
    public int compareTo(Condition other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetColumn()).compareTo(other.isSetColumn());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetColumn()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.column, other.column);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetTimestamp()).compareTo(other.isSetTimestamp());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTimestamp()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.timestamp, other.timestamp);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetValue()).compareTo(other.isSetValue());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetValue()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.value, other.value);
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
        StringBuilder sb = new StringBuilder("Condition(");
        boolean first = true;
        sb.append("column:");
        if (this.column == null) {
            sb.append("null");
        } else {
            sb.append(this.column);
        }
        first = false;
        if (isSetTimestamp()) {
            if (!first)
                sb.append(", ");
            sb.append("timestamp:");
            sb.append(this.timestamp);
            first = false;
        }
        if (isSetValue()) {
            if (!first)
                sb.append(", ");
            sb.append("value:");
            if (this.value == null) {
                sb.append("null");
            } else {
                org.apache.thrift.TBaseHelper.toString(this.value, sb);
            }
            first = false;
        }
        if (isSetIterators()) {
            if (!first)
                sb.append(", ");
            sb.append("iterators:");
            if (this.iterators == null) {
                sb.append("null");
            } else {
                sb.append(this.iterators);
            }
            first = false;
        }
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
        if (column != null) {
            column.validate();
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

    private static class ConditionStandardSchemeFactory implements SchemeFactory {

        public ConditionStandardScheme getScheme() {
            return new ConditionStandardScheme();
        }
    }

    private static class ConditionStandardScheme extends StandardScheme<Condition> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, Condition struct) throws org.apache.thrift.TException {
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
                            struct.column = new Column();
                            struct.column.read(iprot);
                            struct.setColumnIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.timestamp = iprot.readI64();
                            struct.setTimestampIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.value = iprot.readBinary();
                            struct.setValueIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list82 = iprot.readListBegin();
                                struct.iterators = new ArrayList<IteratorSetting>(_list82.size);
                                for (int _i83 = 0; _i83 < _list82.size; ++_i83) {
                                    IteratorSetting _elem84;
                                    _elem84 = new IteratorSetting();
                                    _elem84.read(iprot);
                                    struct.iterators.add(_elem84);
                                }
                                iprot.readListEnd();
                            }
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, Condition struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.column != null) {
                oprot.writeFieldBegin(COLUMN_FIELD_DESC);
                struct.column.write(oprot);
                oprot.writeFieldEnd();
            }
            if (struct.isSetTimestamp()) {
                oprot.writeFieldBegin(TIMESTAMP_FIELD_DESC);
                oprot.writeI64(struct.timestamp);
                oprot.writeFieldEnd();
            }
            if (struct.value != null) {
                if (struct.isSetValue()) {
                    oprot.writeFieldBegin(VALUE_FIELD_DESC);
                    oprot.writeBinary(struct.value);
                    oprot.writeFieldEnd();
                }
            }
            if (struct.iterators != null) {
                if (struct.isSetIterators()) {
                    oprot.writeFieldBegin(ITERATORS_FIELD_DESC);
                    {
                        oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.iterators.size()));
                        for (IteratorSetting _iter85 : struct.iterators) {
                            _iter85.write(oprot);
                        }
                        oprot.writeListEnd();
                    }
                    oprot.writeFieldEnd();
                }
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class ConditionTupleSchemeFactory implements SchemeFactory {

        public ConditionTupleScheme getScheme() {
            return new ConditionTupleScheme();
        }
    }

    private static class ConditionTupleScheme extends TupleScheme<Condition> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, Condition struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetColumn()) {
                optionals.set(0);
            }
            if (struct.isSetTimestamp()) {
                optionals.set(1);
            }
            if (struct.isSetValue()) {
                optionals.set(2);
            }
            if (struct.isSetIterators()) {
                optionals.set(3);
            }
            oprot.writeBitSet(optionals, 4);
            if (struct.isSetColumn()) {
                struct.column.write(oprot);
            }
            if (struct.isSetTimestamp()) {
                oprot.writeI64(struct.timestamp);
            }
            if (struct.isSetValue()) {
                oprot.writeBinary(struct.value);
            }
            if (struct.isSetIterators()) {
                {
                    oprot.writeI32(struct.iterators.size());
                    for (IteratorSetting _iter86 : struct.iterators) {
                        _iter86.write(oprot);
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, Condition struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(4);
            if (incoming.get(0)) {
                struct.column = new Column();
                struct.column.read(iprot);
                struct.setColumnIsSet(true);
            }
            if (incoming.get(1)) {
                struct.timestamp = iprot.readI64();
                struct.setTimestampIsSet(true);
            }
            if (incoming.get(2)) {
                struct.value = iprot.readBinary();
                struct.setValueIsSet(true);
            }
            if (incoming.get(3)) {
                {
                    org.apache.thrift.protocol.TList _list87 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.iterators = new ArrayList<IteratorSetting>(_list87.size);
                    for (int _i88 = 0; _i88 < _list87.size; ++_i88) {
                        IteratorSetting _elem89;
                        _elem89 = new IteratorSetting();
                        _elem89.read(iprot);
                        struct.iterators.add(_elem89);
                    }
                }
                struct.setIteratorsIsSet(true);
            }
        }
    }
}