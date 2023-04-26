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
public class IteratorSetting implements org.apache.thrift.TBase<IteratorSetting, IteratorSetting._Fields>, java.io.Serializable, Cloneable, Comparable<IteratorSetting> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("IteratorSetting");

    private static final org.apache.thrift.protocol.TField PRIORITY_FIELD_DESC = new org.apache.thrift.protocol.TField("priority", org.apache.thrift.protocol.TType.I32, (short) 1);

    private static final org.apache.thrift.protocol.TField NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("name", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.protocol.TField ITERATOR_CLASS_FIELD_DESC = new org.apache.thrift.protocol.TField("iteratorClass", org.apache.thrift.protocol.TType.STRING, (short) 3);

    private static final org.apache.thrift.protocol.TField PROPERTIES_FIELD_DESC = new org.apache.thrift.protocol.TField("properties", org.apache.thrift.protocol.TType.MAP, (short) 4);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new IteratorSettingStandardSchemeFactory());
        schemes.put(TupleScheme.class, new IteratorSettingTupleSchemeFactory());
    }

    public int priority;

    public String name;

    public String iteratorClass;

    public Map<String, String> properties;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        PRIORITY((short) 1, "priority"), NAME((short) 2, "name"), ITERATOR_CLASS((short) 3, "iteratorClass"), PROPERTIES((short) 4, "properties");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return PRIORITY;
                case 2:
                    return NAME;
                case 3:
                    return ITERATOR_CLASS;
                case 4:
                    return PROPERTIES;
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

    private static final int __PRIORITY_ISSET_ID = 0;

    private byte __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.PRIORITY, new org.apache.thrift.meta_data.FieldMetaData("priority", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.NAME, new org.apache.thrift.meta_data.FieldMetaData("name", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.ITERATOR_CLASS, new org.apache.thrift.meta_data.FieldMetaData("iteratorClass", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.PROPERTIES, new org.apache.thrift.meta_data.FieldMetaData("properties", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(IteratorSetting.class, metaDataMap);
    }

    public IteratorSetting() {
    }

    public IteratorSetting(int priority, String name, String iteratorClass, Map<String, String> properties) {
        this();
        this.priority = priority;
        setPriorityIsSet(true);
        this.name = name;
        this.iteratorClass = iteratorClass;
        this.properties = properties;
    }

    public IteratorSetting(IteratorSetting other) {
        __isset_bitfield = other.__isset_bitfield;
        this.priority = other.priority;
        if (other.isSetName()) {
            this.name = other.name;
        }
        if (other.isSetIteratorClass()) {
            this.iteratorClass = other.iteratorClass;
        }
        if (other.isSetProperties()) {
            Map<String, String> __this__properties = new HashMap<String, String>(other.properties);
            this.properties = __this__properties;
        }
    }

    public IteratorSetting deepCopy() {
        return new IteratorSetting(this);
    }

    @Override
    public void clear() {
        setPriorityIsSet(false);
        this.priority = 0;
        this.name = null;
        this.iteratorClass = null;
        this.properties = null;
    }

    public int getPriority() {
        return this.priority;
    }

    public IteratorSetting setPriority(int priority) {
        this.priority = priority;
        setPriorityIsSet(true);
        return this;
    }

    public void unsetPriority() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __PRIORITY_ISSET_ID);
    }

    public boolean isSetPriority() {
        return EncodingUtils.testBit(__isset_bitfield, __PRIORITY_ISSET_ID);
    }

    public void setPriorityIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __PRIORITY_ISSET_ID, value);
    }

    public String getName() {
        return this.name;
    }

    public IteratorSetting setName(String name) {
        this.name = name;
        return this;
    }

    public void unsetName() {
        this.name = null;
    }

    public boolean isSetName() {
        return this.name != null;
    }

    public void setNameIsSet(boolean value) {
        if (!value) {
            this.name = null;
        }
    }

    public String getIteratorClass() {
        return this.iteratorClass;
    }

    public IteratorSetting setIteratorClass(String iteratorClass) {
        this.iteratorClass = iteratorClass;
        return this;
    }

    public void unsetIteratorClass() {
        this.iteratorClass = null;
    }

    public boolean isSetIteratorClass() {
        return this.iteratorClass != null;
    }

    public void setIteratorClassIsSet(boolean value) {
        if (!value) {
            this.iteratorClass = null;
        }
    }

    public int getPropertiesSize() {
        return (this.properties == null) ? 0 : this.properties.size();
    }

    public void putToProperties(String key, String val) {
        if (this.properties == null) {
            this.properties = new HashMap<String, String>();
        }
        this.properties.put(key, val);
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    public IteratorSetting setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    public void unsetProperties() {
        this.properties = null;
    }

    public boolean isSetProperties() {
        return this.properties != null;
    }

    public void setPropertiesIsSet(boolean value) {
        if (!value) {
            this.properties = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case PRIORITY:
                if (value == null) {
                    unsetPriority();
                } else {
                    setPriority((Integer) value);
                }
                break;
            case NAME:
                if (value == null) {
                    unsetName();
                } else {
                    setName((String) value);
                }
                break;
            case ITERATOR_CLASS:
                if (value == null) {
                    unsetIteratorClass();
                } else {
                    setIteratorClass((String) value);
                }
                break;
            case PROPERTIES:
                if (value == null) {
                    unsetProperties();
                } else {
                    setProperties((Map<String, String>) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case PRIORITY:
                return Integer.valueOf(getPriority());
            case NAME:
                return getName();
            case ITERATOR_CLASS:
                return getIteratorClass();
            case PROPERTIES:
                return getProperties();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case PRIORITY:
                return isSetPriority();
            case NAME:
                return isSetName();
            case ITERATOR_CLASS:
                return isSetIteratorClass();
            case PROPERTIES:
                return isSetProperties();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof IteratorSetting)
            return this.equals((IteratorSetting) that);
        return false;
    }

    public boolean equals(IteratorSetting that) {
        if (that == null)
            return false;
        boolean this_present_priority = true;
        boolean that_present_priority = true;
        if (this_present_priority || that_present_priority) {
            if (!(this_present_priority && that_present_priority))
                return false;
            if (this.priority != that.priority)
                return false;
        }
        boolean this_present_name = true && this.isSetName();
        boolean that_present_name = true && that.isSetName();
        if (this_present_name || that_present_name) {
            if (!(this_present_name && that_present_name))
                return false;
            if (!this.name.equals(that.name))
                return false;
        }
        boolean this_present_iteratorClass = true && this.isSetIteratorClass();
        boolean that_present_iteratorClass = true && that.isSetIteratorClass();
        if (this_present_iteratorClass || that_present_iteratorClass) {
            if (!(this_present_iteratorClass && that_present_iteratorClass))
                return false;
            if (!this.iteratorClass.equals(that.iteratorClass))
                return false;
        }
        boolean this_present_properties = true && this.isSetProperties();
        boolean that_present_properties = true && that.isSetProperties();
        if (this_present_properties || that_present_properties) {
            if (!(this_present_properties && that_present_properties))
                return false;
            if (!this.properties.equals(that.properties))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int compareTo(IteratorSetting other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetPriority()).compareTo(other.isSetPriority());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetPriority()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.priority, other.priority);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetName()).compareTo(other.isSetName());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetName()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.name, other.name);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetIteratorClass()).compareTo(other.isSetIteratorClass());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetIteratorClass()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.iteratorClass, other.iteratorClass);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetProperties()).compareTo(other.isSetProperties());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetProperties()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.properties, other.properties);
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
        StringBuilder sb = new StringBuilder("IteratorSetting(");
        boolean first = true;
        sb.append("priority:");
        sb.append(this.priority);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("name:");
        if (this.name == null) {
            sb.append("null");
        } else {
            sb.append(this.name);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("iteratorClass:");
        if (this.iteratorClass == null) {
            sb.append("null");
        } else {
            sb.append(this.iteratorClass);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("properties:");
        if (this.properties == null) {
            sb.append("null");
        } else {
            sb.append(this.properties);
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

    private static class IteratorSettingStandardSchemeFactory implements SchemeFactory {

        public IteratorSettingStandardScheme getScheme() {
            return new IteratorSettingStandardScheme();
        }
    }

    private static class IteratorSettingStandardScheme extends StandardScheme<IteratorSetting> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, IteratorSetting struct) throws org.apache.thrift.TException {
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
                            struct.priority = iprot.readI32();
                            struct.setPriorityIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.name = iprot.readString();
                            struct.setNameIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.iteratorClass = iprot.readString();
                            struct.setIteratorClassIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
                            {
                                org.apache.thrift.protocol.TMap _map16 = iprot.readMapBegin();
                                struct.properties = new HashMap<String, String>(2 * _map16.size);
                                for (int _i17 = 0; _i17 < _map16.size; ++_i17) {
                                    String _key18;
                                    String _val19;
                                    _key18 = iprot.readString();
                                    _val19 = iprot.readString();
                                    struct.properties.put(_key18, _val19);
                                }
                                iprot.readMapEnd();
                            }
                            struct.setPropertiesIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, IteratorSetting struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            oprot.writeFieldBegin(PRIORITY_FIELD_DESC);
            oprot.writeI32(struct.priority);
            oprot.writeFieldEnd();
            if (struct.name != null) {
                oprot.writeFieldBegin(NAME_FIELD_DESC);
                oprot.writeString(struct.name);
                oprot.writeFieldEnd();
            }
            if (struct.iteratorClass != null) {
                oprot.writeFieldBegin(ITERATOR_CLASS_FIELD_DESC);
                oprot.writeString(struct.iteratorClass);
                oprot.writeFieldEnd();
            }
            if (struct.properties != null) {
                oprot.writeFieldBegin(PROPERTIES_FIELD_DESC);
                {
                    oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, struct.properties.size()));
                    for (Map.Entry<String, String> _iter20 : struct.properties.entrySet()) {
                        oprot.writeString(_iter20.getKey());
                        oprot.writeString(_iter20.getValue());
                    }
                    oprot.writeMapEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class IteratorSettingTupleSchemeFactory implements SchemeFactory {

        public IteratorSettingTupleScheme getScheme() {
            return new IteratorSettingTupleScheme();
        }
    }

    private static class IteratorSettingTupleScheme extends TupleScheme<IteratorSetting> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, IteratorSetting struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetPriority()) {
                optionals.set(0);
            }
            if (struct.isSetName()) {
                optionals.set(1);
            }
            if (struct.isSetIteratorClass()) {
                optionals.set(2);
            }
            if (struct.isSetProperties()) {
                optionals.set(3);
            }
            oprot.writeBitSet(optionals, 4);
            if (struct.isSetPriority()) {
                oprot.writeI32(struct.priority);
            }
            if (struct.isSetName()) {
                oprot.writeString(struct.name);
            }
            if (struct.isSetIteratorClass()) {
                oprot.writeString(struct.iteratorClass);
            }
            if (struct.isSetProperties()) {
                {
                    oprot.writeI32(struct.properties.size());
                    for (Map.Entry<String, String> _iter21 : struct.properties.entrySet()) {
                        oprot.writeString(_iter21.getKey());
                        oprot.writeString(_iter21.getValue());
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, IteratorSetting struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(4);
            if (incoming.get(0)) {
                struct.priority = iprot.readI32();
                struct.setPriorityIsSet(true);
            }
            if (incoming.get(1)) {
                struct.name = iprot.readString();
                struct.setNameIsSet(true);
            }
            if (incoming.get(2)) {
                struct.iteratorClass = iprot.readString();
                struct.setIteratorClassIsSet(true);
            }
            if (incoming.get(3)) {
                {
                    org.apache.thrift.protocol.TMap _map22 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.properties = new HashMap<String, String>(2 * _map22.size);
                    for (int _i23 = 0; _i23 < _map22.size; ++_i23) {
                        String _key24;
                        String _val25;
                        _key24 = iprot.readString();
                        _val25 = iprot.readString();
                        struct.properties.put(_key24, _val25);
                    }
                }
                struct.setPropertiesIsSet(true);
            }
        }
    }
}