package org.apache.hadoop.hive.serde2.thrift.test;

import org.apache.commons.lang.builder.HashCodeBuilder;
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

public class SetIntString implements org.apache.thrift.TBase<SetIntString, SetIntString._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("SetIntString");

    private static final org.apache.thrift.protocol.TField S_INT_STRING_FIELD_DESC = new org.apache.thrift.protocol.TField("sIntString", org.apache.thrift.protocol.TType.SET, (short) 1);

    private static final org.apache.thrift.protocol.TField A_STRING_FIELD_DESC = new org.apache.thrift.protocol.TField("aString", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new SetIntStringStandardSchemeFactory());
        schemes.put(TupleScheme.class, new SetIntStringTupleSchemeFactory());
    }

    private Set<IntString> sIntString;

    private String aString;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        S_INT_STRING((short) 1, "sIntString"), A_STRING((short) 2, "aString");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return S_INT_STRING;
                case 2:
                    return A_STRING;
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
        tmpMap.put(_Fields.S_INT_STRING, new org.apache.thrift.meta_data.FieldMetaData("sIntString", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.SetMetaData(org.apache.thrift.protocol.TType.SET, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, IntString.class))));
        tmpMap.put(_Fields.A_STRING, new org.apache.thrift.meta_data.FieldMetaData("aString", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(SetIntString.class, metaDataMap);
    }

    public SetIntString() {
    }

    public SetIntString(Set<IntString> sIntString, String aString) {
        this();
        this.sIntString = sIntString;
        this.aString = aString;
    }

    public SetIntString(SetIntString other) {
        if (other.isSetSIntString()) {
            Set<IntString> __this__sIntString = new HashSet<IntString>();
            for (IntString other_element : other.sIntString) {
                __this__sIntString.add(new IntString(other_element));
            }
            this.sIntString = __this__sIntString;
        }
        if (other.isSetAString()) {
            this.aString = other.aString;
        }
    }

    public SetIntString deepCopy() {
        return new SetIntString(this);
    }

    @Override
    public void clear() {
        this.sIntString = null;
        this.aString = null;
    }

    public int getSIntStringSize() {
        return (this.sIntString == null) ? 0 : this.sIntString.size();
    }

    public java.util.Iterator<IntString> getSIntStringIterator() {
        return (this.sIntString == null) ? null : this.sIntString.iterator();
    }

    public void addToSIntString(IntString elem) {
        if (this.sIntString == null) {
            this.sIntString = new HashSet<IntString>();
        }
        this.sIntString.add(elem);
    }

    public Set<IntString> getSIntString() {
        return this.sIntString;
    }

    public void setSIntString(Set<IntString> sIntString) {
        this.sIntString = sIntString;
    }

    public void unsetSIntString() {
        this.sIntString = null;
    }

    public boolean isSetSIntString() {
        return this.sIntString != null;
    }

    public void setSIntStringIsSet(boolean value) {
        if (!value) {
            this.sIntString = null;
        }
    }

    public String getAString() {
        return this.aString;
    }

    public void setAString(String aString) {
        this.aString = aString;
    }

    public void unsetAString() {
        this.aString = null;
    }

    public boolean isSetAString() {
        return this.aString != null;
    }

    public void setAStringIsSet(boolean value) {
        if (!value) {
            this.aString = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case S_INT_STRING:
                if (value == null) {
                    unsetSIntString();
                } else {
                    setSIntString((Set<IntString>) value);
                }
                break;
            case A_STRING:
                if (value == null) {
                    unsetAString();
                } else {
                    setAString((String) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case S_INT_STRING:
                return getSIntString();
            case A_STRING:
                return getAString();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case S_INT_STRING:
                return isSetSIntString();
            case A_STRING:
                return isSetAString();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof SetIntString)
            return this.equals((SetIntString) that);
        return false;
    }

    public boolean equals(SetIntString that) {
        if (that == null)
            return false;
        boolean this_present_sIntString = true && this.isSetSIntString();
        boolean that_present_sIntString = true && that.isSetSIntString();
        if (this_present_sIntString || that_present_sIntString) {
            if (!(this_present_sIntString && that_present_sIntString))
                return false;
            if (!this.sIntString.equals(that.sIntString))
                return false;
        }
        boolean this_present_aString = true && this.isSetAString();
        boolean that_present_aString = true && that.isSetAString();
        if (this_present_aString || that_present_aString) {
            if (!(this_present_aString && that_present_aString))
                return false;
            if (!this.aString.equals(that.aString))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        boolean present_sIntString = true && (isSetSIntString());
        builder.append(present_sIntString);
        if (present_sIntString)
            builder.append(sIntString);
        boolean present_aString = true && (isSetAString());
        builder.append(present_aString);
        if (present_aString)
            builder.append(aString);
        return builder.toHashCode();
    }

    public int compareTo(SetIntString other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        SetIntString typedOther = (SetIntString) other;
        lastComparison = Boolean.valueOf(isSetSIntString()).compareTo(typedOther.isSetSIntString());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetSIntString()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sIntString, typedOther.sIntString);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetAString()).compareTo(typedOther.isSetAString());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetAString()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.aString, typedOther.aString);
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
        StringBuilder sb = new StringBuilder("SetIntString(");
        boolean first = true;
        sb.append("sIntString:");
        if (this.sIntString == null) {
            sb.append("null");
        } else {
            sb.append(this.sIntString);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("aString:");
        if (this.aString == null) {
            sb.append("null");
        } else {
            sb.append(this.aString);
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
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class SetIntStringStandardSchemeFactory implements SchemeFactory {

        public SetIntStringStandardScheme getScheme() {
            return new SetIntStringStandardScheme();
        }
    }

    private static class SetIntStringStandardScheme extends StandardScheme<SetIntString> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, SetIntString struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch(schemeField.id) {
                    case 1:
                        if (schemeField.type == org.apache.thrift.protocol.TType.SET) {
                            {
                                org.apache.thrift.protocol.TSet _set82 = iprot.readSetBegin();
                                struct.sIntString = new HashSet<IntString>(2 * _set82.size);
                                for (int _i83 = 0; _i83 < _set82.size; ++_i83) {
                                    IntString _elem84;
                                    _elem84 = new IntString();
                                    _elem84.read(iprot);
                                    struct.sIntString.add(_elem84);
                                }
                                iprot.readSetEnd();
                            }
                            struct.setSIntStringIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.aString = iprot.readString();
                            struct.setAStringIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, SetIntString struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.sIntString != null) {
                oprot.writeFieldBegin(S_INT_STRING_FIELD_DESC);
                {
                    oprot.writeSetBegin(new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.STRUCT, struct.sIntString.size()));
                    for (IntString _iter85 : struct.sIntString) {
                        _iter85.write(oprot);
                    }
                    oprot.writeSetEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.aString != null) {
                oprot.writeFieldBegin(A_STRING_FIELD_DESC);
                oprot.writeString(struct.aString);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class SetIntStringTupleSchemeFactory implements SchemeFactory {

        public SetIntStringTupleScheme getScheme() {
            return new SetIntStringTupleScheme();
        }
    }

    private static class SetIntStringTupleScheme extends TupleScheme<SetIntString> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, SetIntString struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetSIntString()) {
                optionals.set(0);
            }
            if (struct.isSetAString()) {
                optionals.set(1);
            }
            oprot.writeBitSet(optionals, 2);
            if (struct.isSetSIntString()) {
                {
                    oprot.writeI32(struct.sIntString.size());
                    for (IntString _iter86 : struct.sIntString) {
                        _iter86.write(oprot);
                    }
                }
            }
            if (struct.isSetAString()) {
                oprot.writeString(struct.aString);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, SetIntString struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(2);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TSet _set87 = new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.sIntString = new HashSet<IntString>(2 * _set87.size);
                    for (int _i88 = 0; _i88 < _set87.size; ++_i88) {
                        IntString _elem89;
                        _elem89 = new IntString();
                        _elem89.read(iprot);
                        struct.sIntString.add(_elem89);
                    }
                }
                struct.setSIntStringIsSet(true);
            }
            if (incoming.get(1)) {
                struct.aString = iprot.readString();
                struct.setAStringIsSet(true);
            }
        }
    }
}
