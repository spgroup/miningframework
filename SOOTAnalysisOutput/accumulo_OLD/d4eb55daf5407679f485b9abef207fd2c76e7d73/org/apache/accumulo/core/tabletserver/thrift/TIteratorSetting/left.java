package org.apache.accumulo.core.tabletserver.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class TIteratorSetting implements org.apache.thrift.TBase<TIteratorSetting, TIteratorSetting._Fields>, java.io.Serializable, Cloneable, Comparable<TIteratorSetting> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TIteratorSetting");

    private static final org.apache.thrift.protocol.TField PRIORITY_FIELD_DESC = new org.apache.thrift.protocol.TField("priority", org.apache.thrift.protocol.TType.I32, (short) 1);

    private static final org.apache.thrift.protocol.TField NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("name", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.protocol.TField ITERATOR_CLASS_FIELD_DESC = new org.apache.thrift.protocol.TField("iteratorClass", org.apache.thrift.protocol.TType.STRING, (short) 3);

    private static final org.apache.thrift.protocol.TField PROPERTIES_FIELD_DESC = new org.apache.thrift.protocol.TField("properties", org.apache.thrift.protocol.TType.MAP, (short) 4);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new TIteratorSettingStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new TIteratorSettingTupleSchemeFactory();

    public int priority;

    @org.apache.thrift.annotation.Nullable
    public java.lang.String name;

    @org.apache.thrift.annotation.Nullable
    public java.lang.String iteratorClass;

    @org.apache.thrift.annotation.Nullable
    public java.util.Map<java.lang.String, java.lang.String> properties;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        PRIORITY((short) 1, "priority"), NAME((short) 2, "name"), ITERATOR_CLASS((short) 3, "iteratorClass"), PROPERTIES((short) 4, "properties");

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

    private static final int __PRIORITY_ISSET_ID = 0;

    private byte __isset_bitfield = 0;

    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.PRIORITY, new org.apache.thrift.meta_data.FieldMetaData("priority", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.NAME, new org.apache.thrift.meta_data.FieldMetaData("name", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.ITERATOR_CLASS, new org.apache.thrift.meta_data.FieldMetaData("iteratorClass", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.PROPERTIES, new org.apache.thrift.meta_data.FieldMetaData("properties", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TIteratorSetting.class, metaDataMap);
    }

    public TIteratorSetting() {
    }

    public TIteratorSetting(int priority, java.lang.String name, java.lang.String iteratorClass, java.util.Map<java.lang.String, java.lang.String> properties) {
        this();
        this.priority = priority;
        setPriorityIsSet(true);
        this.name = name;
        this.iteratorClass = iteratorClass;
        this.properties = properties;
    }

    public TIteratorSetting(TIteratorSetting other) {
        __isset_bitfield = other.__isset_bitfield;
        this.priority = other.priority;
        if (other.isSetName()) {
            this.name = other.name;
        }
        if (other.isSetIteratorClass()) {
            this.iteratorClass = other.iteratorClass;
        }
        if (other.isSetProperties()) {
            java.util.Map<java.lang.String, java.lang.String> __this__properties = new java.util.HashMap<java.lang.String, java.lang.String>(other.properties);
            this.properties = __this__properties;
        }
    }

    public TIteratorSetting deepCopy() {
        return new TIteratorSetting(this);
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

    public TIteratorSetting setPriority(int priority) {
        this.priority = priority;
        setPriorityIsSet(true);
        return this;
    }

    public void unsetPriority() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __PRIORITY_ISSET_ID);
    }

    public boolean isSetPriority() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __PRIORITY_ISSET_ID);
    }

    public void setPriorityIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __PRIORITY_ISSET_ID, value);
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.String getName() {
        return this.name;
    }

    public TIteratorSetting setName(@org.apache.thrift.annotation.Nullable java.lang.String name) {
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

    @org.apache.thrift.annotation.Nullable
    public java.lang.String getIteratorClass() {
        return this.iteratorClass;
    }

    public TIteratorSetting setIteratorClass(@org.apache.thrift.annotation.Nullable java.lang.String iteratorClass) {
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

    public void putToProperties(java.lang.String key, java.lang.String val) {
        if (this.properties == null) {
            this.properties = new java.util.HashMap<java.lang.String, java.lang.String>();
        }
        this.properties.put(key, val);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.Map<java.lang.String, java.lang.String> getProperties() {
        return this.properties;
    }

    public TIteratorSetting setProperties(@org.apache.thrift.annotation.Nullable java.util.Map<java.lang.String, java.lang.String> properties) {
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

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case PRIORITY:
                if (value == null) {
                    unsetPriority();
                } else {
                    setPriority((java.lang.Integer) value);
                }
                break;
            case NAME:
                if (value == null) {
                    unsetName();
                } else {
                    setName((java.lang.String) value);
                }
                break;
            case ITERATOR_CLASS:
                if (value == null) {
                    unsetIteratorClass();
                } else {
                    setIteratorClass((java.lang.String) value);
                }
                break;
            case PROPERTIES:
                if (value == null) {
                    unsetProperties();
                } else {
                    setProperties((java.util.Map<java.lang.String, java.lang.String>) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case PRIORITY:
                return getPriority();
            case NAME:
                return getName();
            case ITERATOR_CLASS:
                return getIteratorClass();
            case PROPERTIES:
                return getProperties();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
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
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof TIteratorSetting)
            return this.equals((TIteratorSetting) that);
        return false;
    }

    public boolean equals(TIteratorSetting that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
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
        int hashCode = 1;
        hashCode = hashCode * 8191 + priority;
        hashCode = hashCode * 8191 + ((isSetName()) ? 131071 : 524287);
        if (isSetName())
            hashCode = hashCode * 8191 + name.hashCode();
        hashCode = hashCode * 8191 + ((isSetIteratorClass()) ? 131071 : 524287);
        if (isSetIteratorClass())
            hashCode = hashCode * 8191 + iteratorClass.hashCode();
        hashCode = hashCode * 8191 + ((isSetProperties()) ? 131071 : 524287);
        if (isSetProperties())
            hashCode = hashCode * 8191 + properties.hashCode();
        return hashCode;
    }

    @Override
    public int compareTo(TIteratorSetting other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetPriority()).compareTo(other.isSetPriority());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetPriority()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.priority, other.priority);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetName()).compareTo(other.isSetName());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetName()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.name, other.name);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetIteratorClass()).compareTo(other.isSetIteratorClass());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetIteratorClass()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.iteratorClass, other.iteratorClass);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetProperties()).compareTo(other.isSetProperties());
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("TIteratorSetting(");
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

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
        try {
            __isset_bitfield = 0;
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class TIteratorSettingStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public TIteratorSettingStandardScheme getScheme() {
            return new TIteratorSettingStandardScheme();
        }
    }

    private static class TIteratorSettingStandardScheme extends org.apache.thrift.scheme.StandardScheme<TIteratorSetting> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, TIteratorSetting struct) throws org.apache.thrift.TException {
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
                                org.apache.thrift.protocol.TMap _map88 = iprot.readMapBegin();
                                struct.properties = new java.util.HashMap<java.lang.String, java.lang.String>(2 * _map88.size);
                                @org.apache.thrift.annotation.Nullable
                                java.lang.String _key89;
                                @org.apache.thrift.annotation.Nullable
                                java.lang.String _val90;
                                for (int _i91 = 0; _i91 < _map88.size; ++_i91) {
                                    _key89 = iprot.readString();
                                    _val90 = iprot.readString();
                                    struct.properties.put(_key89, _val90);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, TIteratorSetting struct) throws org.apache.thrift.TException {
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
                    for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter92 : struct.properties.entrySet()) {
                        oprot.writeString(_iter92.getKey());
                        oprot.writeString(_iter92.getValue());
                    }
                    oprot.writeMapEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class TIteratorSettingTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public TIteratorSettingTupleScheme getScheme() {
            return new TIteratorSettingTupleScheme();
        }
    }

    private static class TIteratorSettingTupleScheme extends org.apache.thrift.scheme.TupleScheme<TIteratorSetting> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, TIteratorSetting struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
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
                    for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter93 : struct.properties.entrySet()) {
                        oprot.writeString(_iter93.getKey());
                        oprot.writeString(_iter93.getValue());
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, TIteratorSetting struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(4);
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
                    org.apache.thrift.protocol.TMap _map94 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.properties = new java.util.HashMap<java.lang.String, java.lang.String>(2 * _map94.size);
                    @org.apache.thrift.annotation.Nullable
                    java.lang.String _key95;
                    @org.apache.thrift.annotation.Nullable
                    java.lang.String _val96;
                    for (int _i97 = 0; _i97 < _map94.size; ++_i97) {
                        _key95 = iprot.readString();
                        _val96 = iprot.readString();
                        struct.properties.put(_key95, _val96);
                    }
                }
                struct.setPropertiesIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}