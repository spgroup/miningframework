package org.apache.accumulo.core.tabletserver.thrift;

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

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class TSamplerConfiguration implements org.apache.thrift.TBase<TSamplerConfiguration, TSamplerConfiguration._Fields>, java.io.Serializable, Cloneable, Comparable<TSamplerConfiguration> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TSamplerConfiguration");

    private static final org.apache.thrift.protocol.TField CLASS_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("className", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final org.apache.thrift.protocol.TField OPTIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("options", org.apache.thrift.protocol.TType.MAP, (short) 2);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new TSamplerConfigurationStandardSchemeFactory());
        schemes.put(TupleScheme.class, new TSamplerConfigurationTupleSchemeFactory());
    }

    public String className;

    public Map<String, String> options;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        CLASS_NAME((short) 1, "className"), OPTIONS((short) 2, "options");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

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
        tmpMap.put(_Fields.CLASS_NAME, new org.apache.thrift.meta_data.FieldMetaData("className", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.OPTIONS, new org.apache.thrift.meta_data.FieldMetaData("options", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TSamplerConfiguration.class, metaDataMap);
    }

    public TSamplerConfiguration() {
    }

    public TSamplerConfiguration(String className, Map<String, String> options) {
        this();
        this.className = className;
        this.options = options;
    }

    public TSamplerConfiguration(TSamplerConfiguration other) {
        if (other.isSetClassName()) {
            this.className = other.className;
        }
        if (other.isSetOptions()) {
            Map<String, String> __this__options = new HashMap<String, String>(other.options);
            this.options = __this__options;
        }
    }

    public TSamplerConfiguration deepCopy() {
        return new TSamplerConfiguration(this);
    }

    @Override
    public void clear() {
        this.className = null;
        this.options = null;
    }

    public String getClassName() {
        return this.className;
    }

    public TSamplerConfiguration setClassName(String className) {
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

    public void putToOptions(String key, String val) {
        if (this.options == null) {
            this.options = new HashMap<String, String>();
        }
        this.options.put(key, val);
    }

    public Map<String, String> getOptions() {
        return this.options;
    }

    public TSamplerConfiguration setOptions(Map<String, String> options) {
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

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case CLASS_NAME:
                if (value == null) {
                    unsetClassName();
                } else {
                    setClassName((String) value);
                }
                break;
            case OPTIONS:
                if (value == null) {
                    unsetOptions();
                } else {
                    setOptions((Map<String, String>) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case CLASS_NAME:
                return getClassName();
            case OPTIONS:
                return getOptions();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case CLASS_NAME:
                return isSetClassName();
            case OPTIONS:
                return isSetOptions();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof TSamplerConfiguration)
            return this.equals((TSamplerConfiguration) that);
        return false;
    }

    public boolean equals(TSamplerConfiguration that) {
        if (that == null)
            return false;
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
        List<Object> list = new ArrayList<Object>();
        boolean present_className = true && (isSetClassName());
        list.add(present_className);
        if (present_className)
            list.add(className);
        boolean present_options = true && (isSetOptions());
        list.add(present_options);
        if (present_options)
            list.add(options);
        return list.hashCode();
    }

    @Override
    public int compareTo(TSamplerConfiguration other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
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
        lastComparison = Boolean.valueOf(isSetOptions()).compareTo(other.isSetOptions());
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
        StringBuilder sb = new StringBuilder("TSamplerConfiguration(");
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

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        try {
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class TSamplerConfigurationStandardSchemeFactory implements SchemeFactory {

        public TSamplerConfigurationStandardScheme getScheme() {
            return new TSamplerConfigurationStandardScheme();
        }
    }

    private static class TSamplerConfigurationStandardScheme extends StandardScheme<TSamplerConfiguration> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, TSamplerConfiguration struct) throws org.apache.thrift.TException {
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
                                org.apache.thrift.protocol.TMap _map106 = iprot.readMapBegin();
                                struct.options = new HashMap<String, String>(2 * _map106.size);
                                String _key107;
                                String _val108;
                                for (int _i109 = 0; _i109 < _map106.size; ++_i109) {
                                    _key107 = iprot.readString();
                                    _val108 = iprot.readString();
                                    struct.options.put(_key107, _val108);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, TSamplerConfiguration struct) throws org.apache.thrift.TException {
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
                    for (Map.Entry<String, String> _iter110 : struct.options.entrySet()) {
                        oprot.writeString(_iter110.getKey());
                        oprot.writeString(_iter110.getValue());
                    }
                    oprot.writeMapEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class TSamplerConfigurationTupleSchemeFactory implements SchemeFactory {

        public TSamplerConfigurationTupleScheme getScheme() {
            return new TSamplerConfigurationTupleScheme();
        }
    }

    private static class TSamplerConfigurationTupleScheme extends TupleScheme<TSamplerConfiguration> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, TSamplerConfiguration struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
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
                    for (Map.Entry<String, String> _iter111 : struct.options.entrySet()) {
                        oprot.writeString(_iter111.getKey());
                        oprot.writeString(_iter111.getValue());
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, TSamplerConfiguration struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(2);
            if (incoming.get(0)) {
                struct.className = iprot.readString();
                struct.setClassNameIsSet(true);
            }
            if (incoming.get(1)) {
                {
                    org.apache.thrift.protocol.TMap _map112 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.options = new HashMap<String, String>(2 * _map112.size);
                    String _key113;
                    String _val114;
                    for (int _i115 = 0; _i115 < _map112.size; ++_i115) {
                        _key113 = iprot.readString();
                        _val114 = iprot.readString();
                        struct.options.put(_key113, _val114);
                    }
                }
                struct.setOptionsIsSet(true);
            }
        }
    }
}
