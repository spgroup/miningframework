package org.apache.accumulo.proxy.thrift;

<<<<<<< MINE
=======
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

>>>>>>> YOURS
@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class CompactionStrategyConfig implements org.apache.thrift.TBase<CompactionStrategyConfig, CompactionStrategyConfig._Fields>, java.io.Serializable, Cloneable, Comparable<CompactionStrategyConfig> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("CompactionStrategyConfig");

    private static final org.apache.thrift.protocol.TField CLASS_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("className", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final org.apache.thrift.protocol.TField OPTIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("options", org.apache.thrift.protocol.TType.MAP, (short) 2);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new CompactionStrategyConfigStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new CompactionStrategyConfigTupleSchemeFactory();

    @org.apache.thrift.annotation.Nullable
    public java.lang.String className;

    @org.apache.thrift.annotation.Nullable
    public java.util.Map<java.lang.String, java.lang.String> options;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        CLASS_NAME((short) 1, "className"), OPTIONS((short) 2, "options");

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
                    return CLASS_NAME;
                case 2:
                    return OPTIONS;
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
        tmpMap.put(_Fields.CLASS_NAME, new org.apache.thrift.meta_data.FieldMetaData("className", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.OPTIONS, new org.apache.thrift.meta_data.FieldMetaData("options", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(CompactionStrategyConfig.class, metaDataMap);
    }

    public CompactionStrategyConfig() {
    }

    public CompactionStrategyConfig(java.lang.String className, java.util.Map<java.lang.String, java.lang.String> options) {
        this();
        this.className = className;
        this.options = options;
    }

    public CompactionStrategyConfig(CompactionStrategyConfig other) {
        if (other.isSetClassName()) {
            this.className = other.className;
        }
        if (other.isSetOptions()) {
            java.util.Map<java.lang.String, java.lang.String> __this__options = new java.util.HashMap<java.lang.String, java.lang.String>(other.options);
            this.options = __this__options;
        }
    }

    public CompactionStrategyConfig deepCopy() {
        return new CompactionStrategyConfig(this);
    }

    @Override
    public void clear() {
        this.className = null;
        this.options = null;
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.String getClassName() {
        return this.className;
    }

    public CompactionStrategyConfig setClassName(@org.apache.thrift.annotation.Nullable java.lang.String className) {
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

    public int getOptionsSize() {
        return (this.options == null) ? 0 : this.options.size();
    }

    public void putToOptions(java.lang.String key, java.lang.String val) {
        if (this.options == null) {
            this.options = new java.util.HashMap<java.lang.String, java.lang.String>();
        }
        this.options.put(key, val);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.Map<java.lang.String, java.lang.String> getOptions() {
        return this.options;
    }

    public CompactionStrategyConfig setOptions(@org.apache.thrift.annotation.Nullable java.util.Map<java.lang.String, java.lang.String> options) {
        this.options = options;
        return this;
    }

    public void unsetOptions() {
        this.options = null;
    }

    public boolean isSetOptions() {
        return this.options != null;
    }

    public void setOptionsIsSet(boolean value) {
        if (!value) {
            this.options = null;
        }
    }

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case CLASS_NAME:
                if (value == null) {
                    unsetClassName();
                } else {
                    setClassName((java.lang.String) value);
                }
                break;
            case OPTIONS:
                if (value == null) {
                    unsetOptions();
                } else {
                    setOptions((java.util.Map<java.lang.String, java.lang.String>) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case CLASS_NAME:
                return getClassName();
            case OPTIONS:
                return getOptions();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }
        switch(field) {
            case CLASS_NAME:
                return isSetClassName();
            case OPTIONS:
                return isSetOptions();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof CompactionStrategyConfig)
            return this.equals((CompactionStrategyConfig) that);
        return false;
    }

    public boolean equals(CompactionStrategyConfig that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
        boolean this_present_className = true && this.isSetClassName();
        boolean that_present_className = true && that.isSetClassName();
        if (this_present_className || that_present_className) {
            if (!(this_present_className && that_present_className))
                return false;
            if (!this.className.equals(that.className))
                return false;
        }
        boolean this_present_options = true && this.isSetOptions();
        boolean that_present_options = true && that.isSetOptions();
        if (this_present_options || that_present_options) {
            if (!(this_present_options && that_present_options))
                return false;
            if (!this.options.equals(that.options))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 8191 + ((isSetClassName()) ? 131071 : 524287);
        if (isSetClassName())
            hashCode = hashCode * 8191 + className.hashCode();
        hashCode = hashCode * 8191 + ((isSetOptions()) ? 131071 : 524287);
        if (isSetOptions())
            hashCode = hashCode * 8191 + options.hashCode();
        return hashCode;
    }

    @Override
    public int compareTo(CompactionStrategyConfig other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetClassName()).compareTo(other.isSetClassName());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetClassName()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.className, other.className);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetOptions()).compareTo(other.isSetOptions());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetOptions()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.options, other.options);
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("CompactionStrategyConfig(");
        boolean first = true;
        sb.append("className:");
        if (this.className == null) {
            sb.append("null");
        } else {
            sb.append(this.className);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("options:");
        if (this.options == null) {
            sb.append("null");
        } else {
            sb.append(this.options);
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

    private static class CompactionStrategyConfigStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public CompactionStrategyConfigStandardScheme getScheme() {
            return new CompactionStrategyConfigStandardScheme();
        }
    }

    private static class CompactionStrategyConfigStandardScheme extends org.apache.thrift.scheme.StandardScheme<CompactionStrategyConfig> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, CompactionStrategyConfig struct) throws org.apache.thrift.TException {
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
                            struct.className = iprot.readString();
                            struct.setClassNameIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
                            {
                                org.apache.thrift.protocol.TMap _map154 = iprot.readMapBegin();
                                struct.options = new java.util.HashMap<java.lang.String, java.lang.String>(2 * _map154.size);
                                @org.apache.thrift.annotation.Nullable
                                java.lang.String _key155;
                                @org.apache.thrift.annotation.Nullable
                                java.lang.String _val156;
                                for (int _i157 = 0; _i157 < _map154.size; ++_i157) {
                                    _key155 = iprot.readString();
                                    _val156 = iprot.readString();
                                    struct.options.put(_key155, _val156);
                                }
                                iprot.readMapEnd();
                            }
                            struct.setOptionsIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, CompactionStrategyConfig struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.className != null) {
                oprot.writeFieldBegin(CLASS_NAME_FIELD_DESC);
                oprot.writeString(struct.className);
                oprot.writeFieldEnd();
            }
            if (struct.options != null) {
                oprot.writeFieldBegin(OPTIONS_FIELD_DESC);
                {
                    oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, struct.options.size()));
                    for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter158 : struct.options.entrySet()) {
                        oprot.writeString(_iter158.getKey());
                        oprot.writeString(_iter158.getValue());
                    }
                    oprot.writeMapEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class CompactionStrategyConfigTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public CompactionStrategyConfigTupleScheme getScheme() {
            return new CompactionStrategyConfigTupleScheme();
        }
    }

    private static class CompactionStrategyConfigTupleScheme extends org.apache.thrift.scheme.TupleScheme<CompactionStrategyConfig> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, CompactionStrategyConfig struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
            if (struct.isSetClassName()) {
                optionals.set(0);
            }
            if (struct.isSetOptions()) {
                optionals.set(1);
            }
            oprot.writeBitSet(optionals, 2);
            if (struct.isSetClassName()) {
                oprot.writeString(struct.className);
            }
            if (struct.isSetOptions()) {
                {
                    oprot.writeI32(struct.options.size());
                    for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter159 : struct.options.entrySet()) {
                        oprot.writeString(_iter159.getKey());
                        oprot.writeString(_iter159.getValue());
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, CompactionStrategyConfig struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(2);
            if (incoming.get(0)) {
                struct.className = iprot.readString();
                struct.setClassNameIsSet(true);
            }
            if (incoming.get(1)) {
                {
                    org.apache.thrift.protocol.TMap _map160 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.options = new java.util.HashMap<java.lang.String, java.lang.String>(2 * _map160.size);
                    @org.apache.thrift.annotation.Nullable
                    java.lang.String _key161;
                    @org.apache.thrift.annotation.Nullable
                    java.lang.String _val162;
                    for (int _i163 = 0; _i163 < _map160.size; ++_i163) {
                        _key161 = iprot.readString();
                        _val162 = iprot.readString();
                        struct.options.put(_key161, _val162);
                    }
                }
                struct.setOptionsIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}
