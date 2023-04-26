package org.apache.accumulo.proxy.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class ScanResult implements org.apache.thrift.TBase<ScanResult, ScanResult._Fields>, java.io.Serializable, Cloneable, Comparable<ScanResult> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ScanResult");

    private static final org.apache.thrift.protocol.TField RESULTS_FIELD_DESC = new org.apache.thrift.protocol.TField("results", org.apache.thrift.protocol.TType.LIST, (short) 1);

    private static final org.apache.thrift.protocol.TField MORE_FIELD_DESC = new org.apache.thrift.protocol.TField("more", org.apache.thrift.protocol.TType.BOOL, (short) 2);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new ScanResultStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new ScanResultTupleSchemeFactory();

    @org.apache.thrift.annotation.Nullable
    public java.util.List<KeyValue> results;

    public boolean more;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        RESULTS((short) 1, "results"), MORE((short) 2, "more");

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
                    return RESULTS;
                case 2:
                    return MORE;
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

    private static final int __MORE_ISSET_ID = 0;

    private byte __isset_bitfield = 0;

    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.RESULTS, new org.apache.thrift.meta_data.FieldMetaData("results", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, KeyValue.class))));
        tmpMap.put(_Fields.MORE, new org.apache.thrift.meta_data.FieldMetaData("more", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ScanResult.class, metaDataMap);
    }

    public ScanResult() {
    }

    public ScanResult(java.util.List<KeyValue> results, boolean more) {
        this();
        this.results = results;
        this.more = more;
        setMoreIsSet(true);
    }

    public ScanResult(ScanResult other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetResults()) {
            java.util.List<KeyValue> __this__results = new java.util.ArrayList<KeyValue>(other.results.size());
            for (KeyValue other_element : other.results) {
                __this__results.add(new KeyValue(other_element));
            }
            this.results = __this__results;
        }
        this.more = other.more;
    }

    public ScanResult deepCopy() {
        return new ScanResult(this);
    }

    @Override
    public void clear() {
        this.results = null;
        setMoreIsSet(false);
        this.more = false;
    }

    public int getResultsSize() {
        return (this.results == null) ? 0 : this.results.size();
    }

    @org.apache.thrift.annotation.Nullable
public java.util.Iterator<KeyValue> getResultsIterator() {
        return (this.results == null) ? null : this.results.iterator();
    }

    public void addToResults(KeyValue elem) {
        if (this.results == null) {
            this.results = new java.util.ArrayList<KeyValue>();
        }
        this.results.add(elem);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.List<KeyValue> getResults() {
        return this.results;
    }

    public ScanResult setResults(@org.apache.thrift.annotation.Nullable java.util.List<KeyValue> results) {
        this.results = results;
        return this;
    }

    public void unsetResults() {
        this.results = null;
    }

    public boolean isSetResults() {
        return this.results != null;
    }

    public void setResultsIsSet(boolean value) {
        if (!value) {
            this.results = null;
        }
    }

    public boolean isMore() {
        return this.more;
    }

    public ScanResult setMore(boolean more) {
        this.more = more;
        setMoreIsSet(true);
        return this;
    }

    public void unsetMore() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __MORE_ISSET_ID);
    }

    public boolean isSetMore() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __MORE_ISSET_ID);
    }

    public void setMoreIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __MORE_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case RESULTS:
                if (value == null) {
                    unsetResults();
                } else {
                    setResults((java.util.List<KeyValue>) value);
                }
                break;
            case MORE:
                if (value == null) {
                    unsetMore();
                } else {
                    setMore((java.lang.Boolean) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case RESULTS:
                return getResults();
            case MORE:
                return isMore();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }
        switch(field) {
            case RESULTS:
                return isSetResults();
            case MORE:
                return isSetMore();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof ScanResult)
            return this.equals((ScanResult) that);
        return false;
    }

    public boolean equals(ScanResult that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
        boolean this_present_results = true && this.isSetResults();
        boolean that_present_results = true && that.isSetResults();
        if (this_present_results || that_present_results) {
            if (!(this_present_results && that_present_results))
                return false;
            if (!this.results.equals(that.results))
                return false;
        }
        boolean this_present_more = true;
        boolean that_present_more = true;
        if (this_present_more || that_present_more) {
            if (!(this_present_more && that_present_more))
                return false;
            if (this.more != that.more)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 8191 + ((isSetResults()) ? 131071 : 524287);
        if (isSetResults())
            hashCode = hashCode * 8191 + results.hashCode();
        hashCode = hashCode * 8191 + ((more) ? 131071 : 524287);
        return hashCode;
    }

    @Override
    public int compareTo(ScanResult other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetResults()).compareTo(other.isSetResults());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetResults()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.results, other.results);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetMore()).compareTo(other.isSetMore());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetMore()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.more, other.more);
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("ScanResult(");
        boolean first = true;
        sb.append("results:");
        if (this.results == null) {
            sb.append("null");
        } else {
            sb.append(this.results);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("more:");
        sb.append(this.more);
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

    private static class ScanResultStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public ScanResultStandardScheme getScheme() {
            return new ScanResultStandardScheme();
        }
    }

    private static class ScanResultStandardScheme extends org.apache.thrift.scheme.StandardScheme<ScanResult> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, ScanResult struct) throws org.apache.thrift.TException {
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
                                org.apache.thrift.protocol.TList _list8 = iprot.readListBegin();
                                struct.results = new java.util.ArrayList<KeyValue>(_list8.size);
                                @org.apache.thrift.annotation.Nullable
                                KeyValue _elem9;
                                for (int _i10 = 0; _i10 < _list8.size; ++_i10) {
                                    _elem9 = new KeyValue();
                                    _elem9.read(iprot);
                                    struct.results.add(_elem9);
                                }
                                iprot.readListEnd();
                            }
                            struct.setResultsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
                            struct.more = iprot.readBool();
                            struct.setMoreIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, ScanResult struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.results != null) {
                oprot.writeFieldBegin(RESULTS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.results.size()));
                    for (KeyValue _iter11 : struct.results) {
                        _iter11.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(MORE_FIELD_DESC);
            oprot.writeBool(struct.more);
            oprot.writeFieldEnd();
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class ScanResultTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public ScanResultTupleScheme getScheme() {
            return new ScanResultTupleScheme();
        }
    }

    private static class ScanResultTupleScheme extends org.apache.thrift.scheme.TupleScheme<ScanResult> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, ScanResult struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
            if (struct.isSetResults()) {
                optionals.set(0);
            }
            if (struct.isSetMore()) {
                optionals.set(1);
            }
            oprot.writeBitSet(optionals, 2);
            if (struct.isSetResults()) {
                {
                    oprot.writeI32(struct.results.size());
                    for (KeyValue _iter12 : struct.results) {
                        _iter12.write(oprot);
                    }
                }
            }
            if (struct.isSetMore()) {
                oprot.writeBool(struct.more);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, ScanResult struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(2);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TList _list13 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.results = new java.util.ArrayList<KeyValue>(_list13.size);
                    @org.apache.thrift.annotation.Nullable
                    KeyValue _elem14;
                    for (int _i15 = 0; _i15 < _list13.size; ++_i15) {
                        _elem14 = new KeyValue();
                        _elem14.read(iprot);
                        struct.results.add(_elem14);
                    }
                }
                struct.setResultsIsSet(true);
            }
            if (incoming.get(1)) {
                struct.more = iprot.readBool();
                struct.setMoreIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}