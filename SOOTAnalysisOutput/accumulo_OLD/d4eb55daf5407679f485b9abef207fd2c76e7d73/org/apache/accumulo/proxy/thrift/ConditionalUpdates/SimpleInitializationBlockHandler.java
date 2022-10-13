package org.apache.accumulo.proxy.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class ConditionalUpdates implements org.apache.thrift.TBase<ConditionalUpdates, ConditionalUpdates._Fields>, java.io.Serializable, Cloneable, Comparable<ConditionalUpdates> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ConditionalUpdates");

    private static final org.apache.thrift.protocol.TField CONDITIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("conditions", org.apache.thrift.protocol.TType.LIST, (short) 2);

    private static final org.apache.thrift.protocol.TField UPDATES_FIELD_DESC = new org.apache.thrift.protocol.TField("updates", org.apache.thrift.protocol.TType.LIST, (short) 3);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new ConditionalUpdatesStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new ConditionalUpdatesTupleSchemeFactory();

    static {
        schemes.put(StandardScheme.class, new ConditionalUpdatesStandardSchemeFactory());
        schemes.put(TupleScheme.class, new ConditionalUpdatesTupleSchemeFactory());
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.List<Condition> conditions;

    @org.apache.thrift.annotation.Nullable
    public java.util.List<ColumnUpdate> updates;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        CONDITIONS((short) 2, "conditions"), UPDATES((short) 3, "updates");

        private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

        static {
            for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        @org.apache.thrift.annotation.Nullable
public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 2:
                    return CONDITIONS;
                case 3:
                    return UPDATES;
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
        tmpMap.put(_Fields.CONDITIONS, new org.apache.thrift.meta_data.FieldMetaData("conditions", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Condition.class))));
        tmpMap.put(_Fields.UPDATES, new org.apache.thrift.meta_data.FieldMetaData("updates", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, ColumnUpdate.class))));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ConditionalUpdates.class, metaDataMap);
    }

    public ConditionalUpdates() {
    }

    public ConditionalUpdates(java.util.List<Condition> conditions, java.util.List<ColumnUpdate> updates) {
        this();
        this.conditions = conditions;
        this.updates = updates;
    }

    public ConditionalUpdates(ConditionalUpdates other) {
        if (other.isSetConditions()) {
            java.util.List<Condition> __this__conditions = new java.util.ArrayList<Condition>(other.conditions.size());
            for (Condition other_element : other.conditions) {
                __this__conditions.add(new Condition(other_element));
            }
            this.conditions = __this__conditions;
        }
        if (other.isSetUpdates()) {
            java.util.List<ColumnUpdate> __this__updates = new java.util.ArrayList<ColumnUpdate>(other.updates.size());
            for (ColumnUpdate other_element : other.updates) {
                __this__updates.add(new ColumnUpdate(other_element));
            }
            this.updates = __this__updates;
        }
    }

    public ConditionalUpdates deepCopy() {
        return new ConditionalUpdates(this);
    }

    @Override
    public void clear() {
        this.conditions = null;
        this.updates = null;
    }

    public int getConditionsSize() {
        return (this.conditions == null) ? 0 : this.conditions.size();
    }

    @org.apache.thrift.annotation.Nullable
public java.util.Iterator<Condition> getConditionsIterator() {
        return (this.conditions == null) ? null : this.conditions.iterator();
    }

    public void addToConditions(Condition elem) {
        if (this.conditions == null) {
            this.conditions = new java.util.ArrayList<Condition>();
        }
        this.conditions.add(elem);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.List<Condition> getConditions() {
        return this.conditions;
    }

    public ConditionalUpdates setConditions(@org.apache.thrift.annotation.Nullable java.util.List<Condition> conditions) {
        this.conditions = conditions;
        return this;
    }

    public void unsetConditions() {
        this.conditions = null;
    }

    public boolean isSetConditions() {
        return this.conditions != null;
    }

    public void setConditionsIsSet(boolean value) {
        if (!value) {
            this.conditions = null;
        }
    }

    public int getUpdatesSize() {
        return (this.updates == null) ? 0 : this.updates.size();
    }

    @org.apache.thrift.annotation.Nullable
public java.util.Iterator<ColumnUpdate> getUpdatesIterator() {
        return (this.updates == null) ? null : this.updates.iterator();
    }

    public void addToUpdates(ColumnUpdate elem) {
        if (this.updates == null) {
            this.updates = new java.util.ArrayList<ColumnUpdate>();
        }
        this.updates.add(elem);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.List<ColumnUpdate> getUpdates() {
        return this.updates;
    }

    public ConditionalUpdates setUpdates(@org.apache.thrift.annotation.Nullable java.util.List<ColumnUpdate> updates) {
        this.updates = updates;
        return this;
    }

    public void unsetUpdates() {
        this.updates = null;
    }

    public boolean isSetUpdates() {
        return this.updates != null;
    }

    public void setUpdatesIsSet(boolean value) {
        if (!value) {
            this.updates = null;
        }
    }

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case CONDITIONS:
                if (value == null) {
                    unsetConditions();
                } else {
                    setConditions((java.util.List<Condition>) value);
                }
                break;
            case UPDATES:
                if (value == null) {
                    unsetUpdates();
                } else {
                    setUpdates((java.util.List<ColumnUpdate>) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case CONDITIONS:
                return getConditions();
            case UPDATES:
                return getUpdates();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }
        switch(field) {
            case CONDITIONS:
                return isSetConditions();
            case UPDATES:
                return isSetUpdates();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof ConditionalUpdates)
            return this.equals((ConditionalUpdates) that);
        return false;
    }

    public boolean equals(ConditionalUpdates that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
        boolean this_present_conditions = true && this.isSetConditions();
        boolean that_present_conditions = true && that.isSetConditions();
        if (this_present_conditions || that_present_conditions) {
            if (!(this_present_conditions && that_present_conditions))
                return false;
            if (!this.conditions.equals(that.conditions))
                return false;
        }
        boolean this_present_updates = true && this.isSetUpdates();
        boolean that_present_updates = true && that.isSetUpdates();
        if (this_present_updates || that_present_updates) {
            if (!(this_present_updates && that_present_updates))
                return false;
            if (!this.updates.equals(that.updates))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 8191 + ((isSetConditions()) ? 131071 : 524287);
        if (isSetConditions())
            hashCode = hashCode * 8191 + conditions.hashCode();
        hashCode = hashCode * 8191 + ((isSetUpdates()) ? 131071 : 524287);
        if (isSetUpdates())
            hashCode = hashCode * 8191 + updates.hashCode();
        return hashCode;
    }

    @Override
    public int compareTo(ConditionalUpdates other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetConditions()).compareTo(other.isSetConditions());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetConditions()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.conditions, other.conditions);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetUpdates()).compareTo(other.isSetUpdates());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetUpdates()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.updates, other.updates);
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("ConditionalUpdates(");
        boolean first = true;
        sb.append("conditions:");
        if (this.conditions == null) {
            sb.append("null");
        } else {
            sb.append(this.conditions);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("updates:");
        if (this.updates == null) {
            sb.append("null");
        } else {
            sb.append(this.updates);
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

    private static class ConditionalUpdatesStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public ConditionalUpdatesStandardScheme getScheme() {
            return new ConditionalUpdatesStandardScheme();
        }
    }

    private static class ConditionalUpdatesStandardScheme extends org.apache.thrift.scheme.StandardScheme<ConditionalUpdates> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, ConditionalUpdates struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch(schemeField.id) {
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list90 = iprot.readListBegin();
                                struct.conditions = new java.util.ArrayList<Condition>(_list90.size);
                                @org.apache.thrift.annotation.Nullable
                                Condition _elem91;
                                for (int _i92 = 0; _i92 < _list90.size; ++_i92) {
                                    _elem91 = new Condition();
                                    _elem91.read(iprot);
                                    struct.conditions.add(_elem91);
                                }
                                iprot.readListEnd();
                            }
                            struct.setConditionsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list93 = iprot.readListBegin();
                                struct.updates = new java.util.ArrayList<ColumnUpdate>(_list93.size);
                                @org.apache.thrift.annotation.Nullable
                                ColumnUpdate _elem94;
                                for (int _i95 = 0; _i95 < _list93.size; ++_i95) {
                                    _elem94 = new ColumnUpdate();
                                    _elem94.read(iprot);
                                    struct.updates.add(_elem94);
                                }
                                iprot.readListEnd();
                            }
                            struct.setUpdatesIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, ConditionalUpdates struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.conditions != null) {
                oprot.writeFieldBegin(CONDITIONS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.conditions.size()));
                    for (Condition _iter96 : struct.conditions) {
                        _iter96.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.updates != null) {
                oprot.writeFieldBegin(UPDATES_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.updates.size()));
                    for (ColumnUpdate _iter97 : struct.updates) {
                        _iter97.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class ConditionalUpdatesTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public ConditionalUpdatesTupleScheme getScheme() {
            return new ConditionalUpdatesTupleScheme();
        }
    }

    private static class ConditionalUpdatesTupleScheme extends org.apache.thrift.scheme.TupleScheme<ConditionalUpdates> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, ConditionalUpdates struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
            if (struct.isSetConditions()) {
                optionals.set(0);
            }
            if (struct.isSetUpdates()) {
                optionals.set(1);
            }
            oprot.writeBitSet(optionals, 2);
            if (struct.isSetConditions()) {
                {
                    oprot.writeI32(struct.conditions.size());
                    for (Condition _iter98 : struct.conditions) {
                        _iter98.write(oprot);
                    }
                }
            }
            if (struct.isSetUpdates()) {
                {
                    oprot.writeI32(struct.updates.size());
                    for (ColumnUpdate _iter99 : struct.updates) {
                        _iter99.write(oprot);
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, ConditionalUpdates struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(2);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TList _list100 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.conditions = new java.util.ArrayList<Condition>(_list100.size);
                    @org.apache.thrift.annotation.Nullable
                    Condition _elem101;
                    for (int _i102 = 0; _i102 < _list100.size; ++_i102) {
                        _elem101 = new Condition();
                        _elem101.read(iprot);
                        struct.conditions.add(_elem101);
                    }
                }
                struct.setConditionsIsSet(true);
            }
            if (incoming.get(1)) {
                {
                    org.apache.thrift.protocol.TList _list103 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.updates = new java.util.ArrayList<ColumnUpdate>(_list103.size);
                    @org.apache.thrift.annotation.Nullable
                    ColumnUpdate _elem104;
                    for (int _i105 = 0; _i105 < _list103.size; ++_i105) {
                        _elem104 = new ColumnUpdate();
                        _elem104.read(iprot);
                        struct.updates.add(_elem104);
                    }
                }
                struct.setUpdatesIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}