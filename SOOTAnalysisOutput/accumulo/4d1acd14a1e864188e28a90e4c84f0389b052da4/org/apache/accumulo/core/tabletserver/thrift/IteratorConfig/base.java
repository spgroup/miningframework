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

@SuppressWarnings("all")
public class IteratorConfig implements org.apache.thrift.TBase<IteratorConfig, IteratorConfig._Fields>, java.io.Serializable, Cloneable, Comparable<IteratorConfig> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("IteratorConfig");

    private static final org.apache.thrift.protocol.TField ITERATORS_FIELD_DESC = new org.apache.thrift.protocol.TField("iterators", org.apache.thrift.protocol.TType.LIST, (short) 1);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new IteratorConfigStandardSchemeFactory());
        schemes.put(TupleScheme.class, new IteratorConfigTupleSchemeFactory());
    }

    public List<TIteratorSetting> iterators;

    @SuppressWarnings("all")
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        ITERATORS((short) 1, "iterators");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
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

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.ITERATORS, new org.apache.thrift.meta_data.FieldMetaData("iterators", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TIteratorSetting.class))));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(IteratorConfig.class, metaDataMap);
    }

    public IteratorConfig() {
    }

    public IteratorConfig(List<TIteratorSetting> iterators) {
        this();
        this.iterators = iterators;
    }

    public IteratorConfig(IteratorConfig other) {
        if (other.isSetIterators()) {
            List<TIteratorSetting> __this__iterators = new ArrayList<TIteratorSetting>(other.iterators.size());
            for (TIteratorSetting other_element : other.iterators) {
                __this__iterators.add(new TIteratorSetting(other_element));
            }
            this.iterators = __this__iterators;
        }
    }

    public IteratorConfig deepCopy() {
        return new IteratorConfig(this);
    }

    @Override
    public void clear() {
        this.iterators = null;
    }

    public int getIteratorsSize() {
        return (this.iterators == null) ? 0 : this.iterators.size();
    }

    public java.util.Iterator<TIteratorSetting> getIteratorsIterator() {
        return (this.iterators == null) ? null : this.iterators.iterator();
    }

    public void addToIterators(TIteratorSetting elem) {
        if (this.iterators == null) {
            this.iterators = new ArrayList<TIteratorSetting>();
        }
        this.iterators.add(elem);
    }

    public List<TIteratorSetting> getIterators() {
        return this.iterators;
    }

    public IteratorConfig setIterators(List<TIteratorSetting> iterators) {
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
            case ITERATORS:
                if (value == null) {
                    unsetIterators();
                } else {
                    setIterators((List<TIteratorSetting>) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
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
            case ITERATORS:
                return isSetIterators();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof IteratorConfig)
            return this.equals((IteratorConfig) that);
        return false;
    }

    public boolean equals(IteratorConfig that) {
        if (that == null)
            return false;
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
    public int compareTo(IteratorConfig other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
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
        StringBuilder sb = new StringBuilder("IteratorConfig(");
        boolean first = true;
        sb.append("iterators:");
        if (this.iterators == null) {
            sb.append("null");
        } else {
            sb.append(this.iterators);
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

    private static class IteratorConfigStandardSchemeFactory implements SchemeFactory {

        public IteratorConfigStandardScheme getScheme() {
            return new IteratorConfigStandardScheme();
        }
    }

    private static class IteratorConfigStandardScheme extends StandardScheme<IteratorConfig> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, IteratorConfig struct) throws org.apache.thrift.TException {
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
                                org.apache.thrift.protocol.TList _list98 = iprot.readListBegin();
                                struct.iterators = new ArrayList<TIteratorSetting>(_list98.size);
                                for (int _i99 = 0; _i99 < _list98.size; ++_i99) {
                                    TIteratorSetting _elem100;
                                    _elem100 = new TIteratorSetting();
                                    _elem100.read(iprot);
                                    struct.iterators.add(_elem100);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, IteratorConfig struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.iterators != null) {
                oprot.writeFieldBegin(ITERATORS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.iterators.size()));
                    for (TIteratorSetting _iter101 : struct.iterators) {
                        _iter101.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class IteratorConfigTupleSchemeFactory implements SchemeFactory {

        public IteratorConfigTupleScheme getScheme() {
            return new IteratorConfigTupleScheme();
        }
    }

    private static class IteratorConfigTupleScheme extends TupleScheme<IteratorConfig> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, IteratorConfig struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetIterators()) {
                optionals.set(0);
            }
            oprot.writeBitSet(optionals, 1);
            if (struct.isSetIterators()) {
                {
                    oprot.writeI32(struct.iterators.size());
                    for (TIteratorSetting _iter102 : struct.iterators) {
                        _iter102.write(oprot);
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, IteratorConfig struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(1);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TList _list103 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.iterators = new ArrayList<TIteratorSetting>(_list103.size);
                    for (int _i104 = 0; _i104 < _list103.size; ++_i104) {
                        TIteratorSetting _elem105;
                        _elem105 = new TIteratorSetting();
                        _elem105.read(iprot);
                        struct.iterators.add(_elem105);
                    }
                }
                struct.setIteratorsIsSet(true);
            }
        }
    }
}
