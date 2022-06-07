package org.apache.accumulo.core.tabletserver.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class IteratorConfig implements org.apache.thrift.TBase<IteratorConfig, IteratorConfig._Fields>, java.io.Serializable, Cloneable, Comparable<IteratorConfig> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("IteratorConfig");

    private static final org.apache.thrift.protocol.TField ITERATORS_FIELD_DESC = new org.apache.thrift.protocol.TField("iterators", org.apache.thrift.protocol.TType.LIST, (short) 1);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new IteratorConfigStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new IteratorConfigTupleSchemeFactory();

    @org.apache.thrift.annotation.Nullable
    public java.util.List<TIteratorSetting> iterators;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        ITERATORS((short) 1, "iterators");

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
                    return ITERATORS;
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
        tmpMap.put(_Fields.ITERATORS, new org.apache.thrift.meta_data.FieldMetaData("iterators", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TIteratorSetting.class))));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(IteratorConfig.class, metaDataMap);
    }

    public IteratorConfig() {
    }

    public IteratorConfig(java.util.List<TIteratorSetting> iterators) {
        this();
        this.iterators = iterators;
    }

    public IteratorConfig(IteratorConfig other) {
        if (other.isSetIterators()) {
            java.util.List<TIteratorSetting> __this__iterators = new java.util.ArrayList<TIteratorSetting>(other.iterators.size());
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

    @org.apache.thrift.annotation.Nullable
    public java.util.Iterator<TIteratorSetting> getIteratorsIterator() {
        return (this.iterators == null) ? null : this.iterators.iterator();
    }

    public void addToIterators(TIteratorSetting elem) {
        if (this.iterators == null) {
            this.iterators = new java.util.ArrayList<TIteratorSetting>();
        }
        this.iterators.add(elem);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.List<TIteratorSetting> getIterators() {
        return this.iterators;
    }

    public IteratorConfig setIterators(@org.apache.thrift.annotation.Nullable java.util.List<TIteratorSetting> iterators) {
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

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case ITERATORS:
                if (value == null) {
                    unsetIterators();
                } else {
                    setIterators((java.util.List<TIteratorSetting>) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case ITERATORS:
                return getIterators();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }
        switch(field) {
            case ITERATORS:
                return isSetIterators();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof IteratorConfig)
            return this.equals((IteratorConfig) that);
        return false;
    }

    public boolean equals(IteratorConfig that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
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
        int hashCode = 1;
        hashCode = hashCode * 8191 + ((isSetIterators()) ? 131071 : 524287);
        if (isSetIterators())
            hashCode = hashCode * 8191 + iterators.hashCode();
        return hashCode;
    }

    @Override
    public int compareTo(IteratorConfig other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetIterators()).compareTo(other.isSetIterators());
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("IteratorConfig(");
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

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
        try {
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class IteratorConfigStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public IteratorConfigStandardScheme getScheme() {
            return new IteratorConfigStandardScheme();
        }
    }

    private static class IteratorConfigStandardScheme extends org.apache.thrift.scheme.StandardScheme<IteratorConfig> {

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
                                struct.iterators = new java.util.ArrayList<TIteratorSetting>(_list98.size);
                                @org.apache.thrift.annotation.Nullable
                                TIteratorSetting _elem99;
                                for (int _i100 = 0; _i100 < _list98.size; ++_i100) {
                                    _elem99 = new TIteratorSetting();
                                    _elem99.read(iprot);
                                    struct.iterators.add(_elem99);
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

    private static class IteratorConfigTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public IteratorConfigTupleScheme getScheme() {
            return new IteratorConfigTupleScheme();
        }
    }

    private static class IteratorConfigTupleScheme extends org.apache.thrift.scheme.TupleScheme<IteratorConfig> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, IteratorConfig struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
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
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(1);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TList _list103 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.iterators = new java.util.ArrayList<TIteratorSetting>(_list103.size);
                    @org.apache.thrift.annotation.Nullable
                    TIteratorSetting _elem104;
                    for (int _i105 = 0; _i105 < _list103.size; ++_i105) {
                        _elem104 = new TIteratorSetting();
                        _elem104.read(iprot);
                        struct.iterators.add(_elem104);
                    }
                }
                struct.setIteratorsIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}
