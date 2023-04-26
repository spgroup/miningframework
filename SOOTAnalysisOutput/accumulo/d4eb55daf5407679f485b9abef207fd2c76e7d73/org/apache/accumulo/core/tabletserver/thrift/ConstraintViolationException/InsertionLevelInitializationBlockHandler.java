package org.apache.accumulo.core.tabletserver.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class ConstraintViolationException extends org.apache.thrift.TException implements org.apache.thrift.TBase<ConstraintViolationException, ConstraintViolationException._Fields>, java.io.Serializable, Cloneable, Comparable<ConstraintViolationException> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ConstraintViolationException");

    private static final org.apache.thrift.protocol.TField VIOLATION_SUMMARIES_FIELD_DESC = new org.apache.thrift.protocol.TField("violationSummaries", org.apache.thrift.protocol.TType.LIST, (short) 1);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new ConstraintViolationExceptionStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new ConstraintViolationExceptionTupleSchemeFactory();

    @org.apache.thrift.annotation.Nullable
    public java.util.List<org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary> violationSummaries;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        VIOLATION_SUMMARIES((short) 1, "violationSummaries");

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
                    return VIOLATION_SUMMARIES;
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
        tmpMap.put(_Fields.VIOLATION_SUMMARIES, new org.apache.thrift.meta_data.FieldMetaData("violationSummaries", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary.class))));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ConstraintViolationException.class, metaDataMap);
    }

    public ConstraintViolationException() {
    }

    public ConstraintViolationException(java.util.List<org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary> violationSummaries) {
        this();
        this.violationSummaries = violationSummaries;
    }

    public ConstraintViolationException(ConstraintViolationException other) {
        if (other.isSetViolationSummaries()) {
            java.util.List<org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary> __this__violationSummaries = new java.util.ArrayList<org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary>(other.violationSummaries.size());
            for (org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary other_element : other.violationSummaries) {
                __this__violationSummaries.add(new org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary(other_element));
            }
            this.violationSummaries = __this__violationSummaries;
        }
    }

    public ConstraintViolationException deepCopy() {
        return new ConstraintViolationException(this);
    }

    @Override
    public void clear() {
        this.violationSummaries = null;
    }

    public int getViolationSummariesSize() {
        return (this.violationSummaries == null) ? 0 : this.violationSummaries.size();
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.Iterator<org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary> getViolationSummariesIterator() {
        return (this.violationSummaries == null) ? null : this.violationSummaries.iterator();
    }

    public void addToViolationSummaries(org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary elem) {
        if (this.violationSummaries == null) {
            this.violationSummaries = new java.util.ArrayList<org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary>();
        }
        this.violationSummaries.add(elem);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.List<org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary> getViolationSummaries() {
        return this.violationSummaries;
    }

    public ConstraintViolationException setViolationSummaries(@org.apache.thrift.annotation.Nullable java.util.List<org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary> violationSummaries) {
        this.violationSummaries = violationSummaries;
        return this;
    }

    public void unsetViolationSummaries() {
        this.violationSummaries = null;
    }

    public boolean isSetViolationSummaries() {
        return this.violationSummaries != null;
    }

    public void setViolationSummariesIsSet(boolean value) {
        if (!value) {
            this.violationSummaries = null;
        }
    }

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case VIOLATION_SUMMARIES:
                if (value == null) {
                    unsetViolationSummaries();
                } else {
                    setViolationSummaries((java.util.List<org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary>) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case VIOLATION_SUMMARIES:
                return getViolationSummaries();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }
        switch(field) {
            case VIOLATION_SUMMARIES:
                return isSetViolationSummaries();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof ConstraintViolationException)
            return this.equals((ConstraintViolationException) that);
        return false;
    }

    public boolean equals(ConstraintViolationException that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
        boolean this_present_violationSummaries = true && this.isSetViolationSummaries();
        boolean that_present_violationSummaries = true && that.isSetViolationSummaries();
        if (this_present_violationSummaries || that_present_violationSummaries) {
            if (!(this_present_violationSummaries && that_present_violationSummaries))
                return false;
            if (!this.violationSummaries.equals(that.violationSummaries))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 8191 + ((isSetViolationSummaries()) ? 131071 : 524287);
        if (isSetViolationSummaries())
            hashCode = hashCode * 8191 + violationSummaries.hashCode();
        return hashCode;
    }

    @Override
    public int compareTo(ConstraintViolationException other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetViolationSummaries()).compareTo(other.isSetViolationSummaries());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetViolationSummaries()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.violationSummaries, other.violationSummaries);
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("ConstraintViolationException(");
        boolean first = true;
        sb.append("violationSummaries:");
        if (this.violationSummaries == null) {
            sb.append("null");
        } else {
            sb.append(this.violationSummaries);
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

    private static class ConstraintViolationExceptionStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public ConstraintViolationExceptionStandardScheme getScheme() {
            return new ConstraintViolationExceptionStandardScheme();
        }
    }

    private static class ConstraintViolationExceptionStandardScheme extends org.apache.thrift.scheme.StandardScheme<ConstraintViolationException> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, ConstraintViolationException struct) throws org.apache.thrift.TException {
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
                                org.apache.thrift.protocol.TList _list0 = iprot.readListBegin();
                                struct.violationSummaries = new java.util.ArrayList<org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary>(_list0.size);
                                @org.apache.thrift.annotation.Nullable
                                org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary _elem1;
                                for (int _i2 = 0; _i2 < _list0.size; ++_i2) {
                                    _elem1 = new org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary();
                                    _elem1.read(iprot);
                                    struct.violationSummaries.add(_elem1);
                                }
                                iprot.readListEnd();
                            }
                            struct.setViolationSummariesIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, ConstraintViolationException struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.violationSummaries != null) {
                oprot.writeFieldBegin(VIOLATION_SUMMARIES_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.violationSummaries.size()));
                    for (org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary _iter3 : struct.violationSummaries) {
                        _iter3.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class ConstraintViolationExceptionTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public ConstraintViolationExceptionTupleScheme getScheme() {
            return new ConstraintViolationExceptionTupleScheme();
        }
    }

    private static class ConstraintViolationExceptionTupleScheme extends org.apache.thrift.scheme.TupleScheme<ConstraintViolationException> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, ConstraintViolationException struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
            if (struct.isSetViolationSummaries()) {
                optionals.set(0);
            }
            oprot.writeBitSet(optionals, 1);
            if (struct.isSetViolationSummaries()) {
                {
                    oprot.writeI32(struct.violationSummaries.size());
                    for (org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary _iter4 : struct.violationSummaries) {
                        _iter4.write(oprot);
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, ConstraintViolationException struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(1);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TList _list5 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.violationSummaries = new java.util.ArrayList<org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary>(_list5.size);
                    @org.apache.thrift.annotation.Nullable
                    org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary _elem6;
                    for (int _i7 = 0; _i7 < _list5.size; ++_i7) {
                        _elem6 = new org.apache.accumulo.core.dataImpl.thrift.TConstraintViolationSummary();
                        _elem6.read(iprot);
                        struct.violationSummaries.add(_elem6);
                    }
                }
                struct.setViolationSummariesIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}