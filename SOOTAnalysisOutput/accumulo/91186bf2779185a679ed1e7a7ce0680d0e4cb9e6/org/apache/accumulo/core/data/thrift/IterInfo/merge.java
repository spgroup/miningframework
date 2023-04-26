package org.apache.accumulo.core.data.thrift;

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

@SuppressWarnings("all")
public class IterInfo implements org.apache.thrift.TBase<IterInfo, IterInfo._Fields>, java.io.Serializable, Cloneable, Comparable<IterInfo> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("IterInfo");

    private static final org.apache.thrift.protocol.TField PRIORITY_FIELD_DESC = new org.apache.thrift.protocol.TField("priority", org.apache.thrift.protocol.TType.I32, (short) 1);

    private static final org.apache.thrift.protocol.TField CLASS_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("className", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.protocol.TField ITER_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("iterName", org.apache.thrift.protocol.TType.STRING, (short) 3);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new IterInfoStandardSchemeFactory());
        schemes.put(TupleScheme.class, new IterInfoTupleSchemeFactory());
    }

    public int priority;

    public String className;

    public String iterName;

    @SuppressWarnings("all")
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        PRIORITY((short) 1, "priority"), CLASS_NAME((short) 2, "className"), ITER_NAME((short) 3, "iterName");

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
                    return CLASS_NAME;
                case 3:
                    return ITER_NAME;
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
        tmpMap.put(_Fields.CLASS_NAME, new org.apache.thrift.meta_data.FieldMetaData("className", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.ITER_NAME, new org.apache.thrift.meta_data.FieldMetaData("iterName", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(IterInfo.class, metaDataMap);
    }

    public IterInfo() {
    }

    public IterInfo(int priority, String className, String iterName) {
        this();
        this.priority = priority;
        setPriorityIsSet(true);
        this.className = className;
        this.iterName = iterName;
    }

    public IterInfo(IterInfo other) {
        __isset_bitfield = other.__isset_bitfield;
        this.priority = other.priority;
        if (other.isSetClassName()) {
            this.className = other.className;
        }
        if (other.isSetIterName()) {
            this.iterName = other.iterName;
        }
    }

    public IterInfo deepCopy() {
        return new IterInfo(this);
    }

    @Override
    public void clear() {
        setPriorityIsSet(false);
        this.priority = 0;
        this.className = null;
        this.iterName = null;
    }

    public int getPriority() {
        return this.priority;
    }

    public IterInfo setPriority(int priority) {
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

    public String getClassName() {
        return this.className;
    }

    public IterInfo setClassName(String className) {
        this.className = className;
        return this;
    }

    public void unsetClassName() {
        this.className = null;
    }

    public boolean isSetClassName() {
        return this.className != null;
    }

    public void setClassNameIsSet(boolean value) {
        if (!value) {
            this.className = null;
        }
    }

    public String getIterName() {
        return this.iterName;
    }

    public IterInfo setIterName(String iterName) {
        this.iterName = iterName;
        return this;
    }

    public void unsetIterName() {
        this.iterName = null;
    }

    public boolean isSetIterName() {
        return this.iterName != null;
    }

    public void setIterNameIsSet(boolean value) {
        if (!value) {
            this.iterName = null;
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
            case CLASS_NAME:
                if (value == null) {
                    unsetClassName();
                } else {
                    setClassName((String) value);
                }
                break;
            case ITER_NAME:
                if (value == null) {
                    unsetIterName();
                } else {
                    setIterName((String) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case PRIORITY:
                return Integer.valueOf(getPriority());
            case CLASS_NAME:
                return getClassName();
            case ITER_NAME:
                return getIterName();
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
            case CLASS_NAME:
                return isSetClassName();
            case ITER_NAME:
                return isSetIterName();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof IterInfo)
            return this.equals((IterInfo) that);
        return false;
    }

    public boolean equals(IterInfo that) {
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
        boolean this_present_className = true && this.isSetClassName();
        boolean that_present_className = true && that.isSetClassName();
        if (this_present_className || that_present_className) {
            if (!(this_present_className && that_present_className))
                return false;
            if (!this.className.equals(that.className))
                return false;
        }
        boolean this_present_iterName = true && this.isSetIterName();
        boolean that_present_iterName = true && that.isSetIterName();
        if (this_present_iterName || that_present_iterName) {
            if (!(this_present_iterName && that_present_iterName))
                return false;
            if (!this.iterName.equals(that.iterName))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int compareTo(IterInfo other) {
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
        lastComparison = Boolean.valueOf(isSetClassName()).compareTo(other.isSetClassName());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetClassName()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.className, other.className);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetIterName()).compareTo(other.isSetIterName());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetIterName()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.iterName, other.iterName);
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
        StringBuilder sb = new StringBuilder("IterInfo(");
        boolean first = true;
        sb.append("priority:");
        sb.append(this.priority);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("className:");
        if (this.className == null) {
            sb.append("null");
        } else {
            sb.append(this.className);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("iterName:");
        if (this.iterName == null) {
            sb.append("null");
        } else {
            sb.append(this.iterName);
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

    private static class IterInfoStandardSchemeFactory implements SchemeFactory {

        public IterInfoStandardScheme getScheme() {
            return new IterInfoStandardScheme();
        }
    }

    private static class IterInfoStandardScheme extends StandardScheme<IterInfo> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, IterInfo struct) throws org.apache.thrift.TException {
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
                            struct.className = iprot.readString();
                            struct.setClassNameIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.iterName = iprot.readString();
                            struct.setIterNameIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, IterInfo struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            oprot.writeFieldBegin(PRIORITY_FIELD_DESC);
            oprot.writeI32(struct.priority);
            oprot.writeFieldEnd();
            if (struct.className != null) {
                oprot.writeFieldBegin(CLASS_NAME_FIELD_DESC);
                oprot.writeString(struct.className);
                oprot.writeFieldEnd();
            }
            if (struct.iterName != null) {
                oprot.writeFieldBegin(ITER_NAME_FIELD_DESC);
                oprot.writeString(struct.iterName);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class IterInfoTupleSchemeFactory implements SchemeFactory {

        public IterInfoTupleScheme getScheme() {
            return new IterInfoTupleScheme();
        }
    }

    private static class IterInfoTupleScheme extends TupleScheme<IterInfo> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, IterInfo struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetPriority()) {
                optionals.set(0);
            }
            if (struct.isSetClassName()) {
                optionals.set(1);
            }
            if (struct.isSetIterName()) {
                optionals.set(2);
            }
            oprot.writeBitSet(optionals, 3);
            if (struct.isSetPriority()) {
                oprot.writeI32(struct.priority);
            }
            if (struct.isSetClassName()) {
                oprot.writeString(struct.className);
            }
            if (struct.isSetIterName()) {
                oprot.writeString(struct.iterName);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, IterInfo struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(3);
            if (incoming.get(0)) {
                struct.priority = iprot.readI32();
                struct.setPriorityIsSet(true);
            }
            if (incoming.get(1)) {
                struct.className = iprot.readString();
                struct.setClassNameIsSet(true);
            }
            if (incoming.get(2)) {
                struct.iterName = iprot.readString();
                struct.setIterNameIsSet(true);
            }
        }
    }
}
