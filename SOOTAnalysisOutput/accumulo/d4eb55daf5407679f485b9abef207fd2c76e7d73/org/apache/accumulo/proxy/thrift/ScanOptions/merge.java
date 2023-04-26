package org.apache.accumulo.proxy.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class ScanOptions implements org.apache.thrift.TBase<ScanOptions, ScanOptions._Fields>, java.io.Serializable, Cloneable, Comparable<ScanOptions> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ScanOptions");

    private static final org.apache.thrift.protocol.TField AUTHORIZATIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("authorizations", org.apache.thrift.protocol.TType.SET, (short) 1);

    private static final org.apache.thrift.protocol.TField RANGE_FIELD_DESC = new org.apache.thrift.protocol.TField("range", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

    private static final org.apache.thrift.protocol.TField COLUMNS_FIELD_DESC = new org.apache.thrift.protocol.TField("columns", org.apache.thrift.protocol.TType.LIST, (short) 3);

    private static final org.apache.thrift.protocol.TField ITERATORS_FIELD_DESC = new org.apache.thrift.protocol.TField("iterators", org.apache.thrift.protocol.TType.LIST, (short) 4);

    private static final org.apache.thrift.protocol.TField BUFFER_SIZE_FIELD_DESC = new org.apache.thrift.protocol.TField("bufferSize", org.apache.thrift.protocol.TType.I32, (short) 5);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new ScanOptionsStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new ScanOptionsTupleSchemeFactory();

    @org.apache.thrift.annotation.Nullable
    public java.util.Set<java.nio.ByteBuffer> authorizations;

    @org.apache.thrift.annotation.Nullable
    public Range range;

    @org.apache.thrift.annotation.Nullable
    public java.util.List<ScanColumn> columns;

    @org.apache.thrift.annotation.Nullable
    public java.util.List<IteratorSetting> iterators;

    public int bufferSize;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        AUTHORIZATIONS((short) 1, "authorizations"), RANGE((short) 2, "range"), COLUMNS((short) 3, "columns"), ITERATORS((short) 4, "iterators"), BUFFER_SIZE((short) 5, "bufferSize");

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
                    return AUTHORIZATIONS;
                case 2:
                    return RANGE;
                case 3:
                    return COLUMNS;
                case 4:
                    return ITERATORS;
                case 5:
                    return BUFFER_SIZE;
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

    private static final int __BUFFERSIZE_ISSET_ID = 0;

    private byte __isset_bitfield = 0;

    private static final _Fields[] optionals = { _Fields.AUTHORIZATIONS, _Fields.RANGE, _Fields.COLUMNS, _Fields.ITERATORS, _Fields.BUFFER_SIZE };

    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.AUTHORIZATIONS, new org.apache.thrift.meta_data.FieldMetaData("authorizations", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.SetMetaData(org.apache.thrift.protocol.TType.SET, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true))));
        tmpMap.put(_Fields.RANGE, new org.apache.thrift.meta_data.FieldMetaData("range", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Range.class)));
        tmpMap.put(_Fields.COLUMNS, new org.apache.thrift.meta_data.FieldMetaData("columns", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, ScanColumn.class))));
        tmpMap.put(_Fields.ITERATORS, new org.apache.thrift.meta_data.FieldMetaData("iterators", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, IteratorSetting.class))));
        tmpMap.put(_Fields.BUFFER_SIZE, new org.apache.thrift.meta_data.FieldMetaData("bufferSize", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ScanOptions.class, metaDataMap);
    }

    public ScanOptions() {
    }

    public ScanOptions(ScanOptions other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetAuthorizations()) {
            java.util.Set<java.nio.ByteBuffer> __this__authorizations = new java.util.HashSet<java.nio.ByteBuffer>(other.authorizations);
            this.authorizations = __this__authorizations;
        }
        if (other.isSetRange()) {
            this.range = new Range(other.range);
        }
        if (other.isSetColumns()) {
            java.util.List<ScanColumn> __this__columns = new java.util.ArrayList<ScanColumn>(other.columns.size());
            for (ScanColumn other_element : other.columns) {
                __this__columns.add(new ScanColumn(other_element));
            }
            this.columns = __this__columns;
        }
        if (other.isSetIterators()) {
            java.util.List<IteratorSetting> __this__iterators = new java.util.ArrayList<IteratorSetting>(other.iterators.size());
            for (IteratorSetting other_element : other.iterators) {
                __this__iterators.add(new IteratorSetting(other_element));
            }
            this.iterators = __this__iterators;
        }
        this.bufferSize = other.bufferSize;
    }

    public ScanOptions deepCopy() {
        return new ScanOptions(this);
    }

    @Override
    public void clear() {
        this.authorizations = null;
        this.range = null;
        this.columns = null;
        this.iterators = null;
        setBufferSizeIsSet(false);
        this.bufferSize = 0;
    }

    public int getAuthorizationsSize() {
        return (this.authorizations == null) ? 0 : this.authorizations.size();
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.Iterator<java.nio.ByteBuffer> getAuthorizationsIterator() {
        return (this.authorizations == null) ? null : this.authorizations.iterator();
    }

    public void addToAuthorizations(java.nio.ByteBuffer elem) {
        if (this.authorizations == null) {
            this.authorizations = new java.util.HashSet<java.nio.ByteBuffer>();
        }
        this.authorizations.add(elem);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.Set<java.nio.ByteBuffer> getAuthorizations() {
        return this.authorizations;
    }

    public ScanOptions setAuthorizations(@org.apache.thrift.annotation.Nullable java.util.Set<java.nio.ByteBuffer> authorizations) {
        this.authorizations = authorizations;
        return this;
    }

    public void unsetAuthorizations() {
        this.authorizations = null;
    }

    public boolean isSetAuthorizations() {
        return this.authorizations != null;
    }

    public void setAuthorizationsIsSet(boolean value) {
        if (!value) {
            this.authorizations = null;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public Range getRange() {
        return this.range;
    }

    public ScanOptions setRange(@org.apache.thrift.annotation.Nullable Range range) {
        this.range = range;
        return this;
    }

    public void unsetRange() {
        this.range = null;
    }

    public boolean isSetRange() {
        return this.range != null;
    }

    public void setRangeIsSet(boolean value) {
        if (!value) {
            this.range = null;
        }
    }

    public int getColumnsSize() {
        return (this.columns == null) ? 0 : this.columns.size();
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.Iterator<ScanColumn> getColumnsIterator() {
        return (this.columns == null) ? null : this.columns.iterator();
    }

    public void addToColumns(ScanColumn elem) {
        if (this.columns == null) {
            this.columns = new java.util.ArrayList<ScanColumn>();
        }
        this.columns.add(elem);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.List<ScanColumn> getColumns() {
        return this.columns;
    }

    public ScanOptions setColumns(@org.apache.thrift.annotation.Nullable java.util.List<ScanColumn> columns) {
        this.columns = columns;
        return this;
    }

    public void unsetColumns() {
        this.columns = null;
    }

    public boolean isSetColumns() {
        return this.columns != null;
    }

    public void setColumnsIsSet(boolean value) {
        if (!value) {
            this.columns = null;
        }
    }

    public int getIteratorsSize() {
        return (this.iterators == null) ? 0 : this.iterators.size();
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.Iterator<IteratorSetting> getIteratorsIterator() {
        return (this.iterators == null) ? null : this.iterators.iterator();
    }

    public void addToIterators(IteratorSetting elem) {
        if (this.iterators == null) {
            this.iterators = new java.util.ArrayList<IteratorSetting>();
        }
        this.iterators.add(elem);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.List<IteratorSetting> getIterators() {
        return this.iterators;
    }

    public ScanOptions setIterators(@org.apache.thrift.annotation.Nullable java.util.List<IteratorSetting> iterators) {
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

    public int getBufferSize() {
        return this.bufferSize;
    }

    public ScanOptions setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        setBufferSizeIsSet(true);
        return this;
    }

    public void unsetBufferSize() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __BUFFERSIZE_ISSET_ID);
    }

    public boolean isSetBufferSize() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __BUFFERSIZE_ISSET_ID);
    }

    public void setBufferSizeIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __BUFFERSIZE_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case AUTHORIZATIONS:
                if (value == null) {
                    unsetAuthorizations();
                } else {
                    setAuthorizations((java.util.Set<java.nio.ByteBuffer>) value);
                }
                break;
            case RANGE:
                if (value == null) {
                    unsetRange();
                } else {
                    setRange((Range) value);
                }
                break;
            case COLUMNS:
                if (value == null) {
                    unsetColumns();
                } else {
                    setColumns((java.util.List<ScanColumn>) value);
                }
                break;
            case ITERATORS:
                if (value == null) {
                    unsetIterators();
                } else {
                    setIterators((java.util.List<IteratorSetting>) value);
                }
                break;
            case BUFFER_SIZE:
                if (value == null) {
                    unsetBufferSize();
                } else {
                    setBufferSize((java.lang.Integer) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case AUTHORIZATIONS:
                return getAuthorizations();
            case RANGE:
                return getRange();
            case COLUMNS:
                return getColumns();
            case ITERATORS:
                return getIterators();
            case BUFFER_SIZE:
                return getBufferSize();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }
        switch(field) {
            case AUTHORIZATIONS:
                return isSetAuthorizations();
            case RANGE:
                return isSetRange();
            case COLUMNS:
                return isSetColumns();
            case ITERATORS:
                return isSetIterators();
            case BUFFER_SIZE:
                return isSetBufferSize();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof ScanOptions)
            return this.equals((ScanOptions) that);
        return false;
    }

    public boolean equals(ScanOptions that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
        boolean this_present_authorizations = true && this.isSetAuthorizations();
        boolean that_present_authorizations = true && that.isSetAuthorizations();
        if (this_present_authorizations || that_present_authorizations) {
            if (!(this_present_authorizations && that_present_authorizations))
                return false;
            if (!this.authorizations.equals(that.authorizations))
                return false;
        }
        boolean this_present_range = true && this.isSetRange();
        boolean that_present_range = true && that.isSetRange();
        if (this_present_range || that_present_range) {
            if (!(this_present_range && that_present_range))
                return false;
            if (!this.range.equals(that.range))
                return false;
        }
        boolean this_present_columns = true && this.isSetColumns();
        boolean that_present_columns = true && that.isSetColumns();
        if (this_present_columns || that_present_columns) {
            if (!(this_present_columns && that_present_columns))
                return false;
            if (!this.columns.equals(that.columns))
                return false;
        }
        boolean this_present_iterators = true && this.isSetIterators();
        boolean that_present_iterators = true && that.isSetIterators();
        if (this_present_iterators || that_present_iterators) {
            if (!(this_present_iterators && that_present_iterators))
                return false;
            if (!this.iterators.equals(that.iterators))
                return false;
        }
        boolean this_present_bufferSize = true && this.isSetBufferSize();
        boolean that_present_bufferSize = true && that.isSetBufferSize();
        if (this_present_bufferSize || that_present_bufferSize) {
            if (!(this_present_bufferSize && that_present_bufferSize))
                return false;
            if (this.bufferSize != that.bufferSize)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 8191 + ((isSetAuthorizations()) ? 131071 : 524287);
        if (isSetAuthorizations())
            hashCode = hashCode * 8191 + authorizations.hashCode();
        hashCode = hashCode * 8191 + ((isSetRange()) ? 131071 : 524287);
        if (isSetRange())
            hashCode = hashCode * 8191 + range.hashCode();
        hashCode = hashCode * 8191 + ((isSetColumns()) ? 131071 : 524287);
        if (isSetColumns())
            hashCode = hashCode * 8191 + columns.hashCode();
        hashCode = hashCode * 8191 + ((isSetIterators()) ? 131071 : 524287);
        if (isSetIterators())
            hashCode = hashCode * 8191 + iterators.hashCode();
        hashCode = hashCode * 8191 + ((isSetBufferSize()) ? 131071 : 524287);
        if (isSetBufferSize())
            hashCode = hashCode * 8191 + bufferSize;
        return hashCode;
    }

    @Override
    public int compareTo(ScanOptions other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetAuthorizations()).compareTo(other.isSetAuthorizations());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetAuthorizations()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.authorizations, other.authorizations);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetRange()).compareTo(other.isSetRange());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetRange()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.range, other.range);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetColumns()).compareTo(other.isSetColumns());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetColumns()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.columns, other.columns);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
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
        lastComparison = java.lang.Boolean.valueOf(isSetBufferSize()).compareTo(other.isSetBufferSize());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetBufferSize()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.bufferSize, other.bufferSize);
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("ScanOptions(");
        boolean first = true;
        if (isSetAuthorizations()) {
            sb.append("authorizations:");
            if (this.authorizations == null) {
                sb.append("null");
            } else {
                org.apache.thrift.TBaseHelper.toString(this.authorizations, sb);
            }
            first = false;
        }
        if (isSetRange()) {
            if (!first)
                sb.append(", ");
            sb.append("range:");
            if (this.range == null) {
                sb.append("null");
            } else {
                sb.append(this.range);
            }
            first = false;
        }
        if (isSetColumns()) {
            if (!first)
                sb.append(", ");
            sb.append("columns:");
            if (this.columns == null) {
                sb.append("null");
            } else {
                sb.append(this.columns);
            }
            first = false;
        }
        if (isSetIterators()) {
            if (!first)
                sb.append(", ");
            sb.append("iterators:");
            if (this.iterators == null) {
                sb.append("null");
            } else {
                sb.append(this.iterators);
            }
            first = false;
        }
        if (isSetBufferSize()) {
            if (!first)
                sb.append(", ");
            sb.append("bufferSize:");
            sb.append(this.bufferSize);
            first = false;
        }
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
        if (range != null) {
            range.validate();
        }
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

    private static class ScanOptionsStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public ScanOptionsStandardScheme getScheme() {
            return new ScanOptionsStandardScheme();
        }
    }

    private static class ScanOptionsStandardScheme extends org.apache.thrift.scheme.StandardScheme<ScanOptions> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, ScanOptions struct) throws org.apache.thrift.TException {
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
                                org.apache.thrift.protocol.TSet _set26 = iprot.readSetBegin();
                                struct.authorizations = new java.util.HashSet<java.nio.ByteBuffer>(2 * _set26.size);
                                @org.apache.thrift.annotation.Nullable
                                java.nio.ByteBuffer _elem27;
                                for (int _i28 = 0; _i28 < _set26.size; ++_i28) {
                                    _elem27 = iprot.readBinary();
                                    struct.authorizations.add(_elem27);
                                }
                                iprot.readSetEnd();
                            }
                            struct.setAuthorizationsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.range = new Range();
                            struct.range.read(iprot);
                            struct.setRangeIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list29 = iprot.readListBegin();
                                struct.columns = new java.util.ArrayList<ScanColumn>(_list29.size);
                                @org.apache.thrift.annotation.Nullable
                                ScanColumn _elem30;
                                for (int _i31 = 0; _i31 < _list29.size; ++_i31) {
                                    _elem30 = new ScanColumn();
                                    _elem30.read(iprot);
                                    struct.columns.add(_elem30);
                                }
                                iprot.readListEnd();
                            }
                            struct.setColumnsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list32 = iprot.readListBegin();
                                struct.iterators = new java.util.ArrayList<IteratorSetting>(_list32.size);
                                @org.apache.thrift.annotation.Nullable
                                IteratorSetting _elem33;
                                for (int _i34 = 0; _i34 < _list32.size; ++_i34) {
                                    _elem33 = new IteratorSetting();
                                    _elem33.read(iprot);
                                    struct.iterators.add(_elem33);
                                }
                                iprot.readListEnd();
                            }
                            struct.setIteratorsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 5:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.bufferSize = iprot.readI32();
                            struct.setBufferSizeIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, ScanOptions struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.authorizations != null) {
                if (struct.isSetAuthorizations()) {
                    oprot.writeFieldBegin(AUTHORIZATIONS_FIELD_DESC);
                    {
                        oprot.writeSetBegin(new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.STRING, struct.authorizations.size()));
                        for (java.nio.ByteBuffer _iter35 : struct.authorizations) {
                            oprot.writeBinary(_iter35);
                        }
                        oprot.writeSetEnd();
                    }
                    oprot.writeFieldEnd();
                }
            }
            if (struct.range != null) {
                if (struct.isSetRange()) {
                    oprot.writeFieldBegin(RANGE_FIELD_DESC);
                    struct.range.write(oprot);
                    oprot.writeFieldEnd();
                }
            }
            if (struct.columns != null) {
                if (struct.isSetColumns()) {
                    oprot.writeFieldBegin(COLUMNS_FIELD_DESC);
                    {
                        oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.columns.size()));
                        for (ScanColumn _iter36 : struct.columns) {
                            _iter36.write(oprot);
                        }
                        oprot.writeListEnd();
                    }
                    oprot.writeFieldEnd();
                }
            }
            if (struct.iterators != null) {
                if (struct.isSetIterators()) {
                    oprot.writeFieldBegin(ITERATORS_FIELD_DESC);
                    {
                        oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.iterators.size()));
                        for (IteratorSetting _iter37 : struct.iterators) {
                            _iter37.write(oprot);
                        }
                        oprot.writeListEnd();
                    }
                    oprot.writeFieldEnd();
                }
            }
            if (struct.isSetBufferSize()) {
                oprot.writeFieldBegin(BUFFER_SIZE_FIELD_DESC);
                oprot.writeI32(struct.bufferSize);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class ScanOptionsTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public ScanOptionsTupleScheme getScheme() {
            return new ScanOptionsTupleScheme();
        }
    }

    private static class ScanOptionsTupleScheme extends org.apache.thrift.scheme.TupleScheme<ScanOptions> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, ScanOptions struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
            if (struct.isSetAuthorizations()) {
                optionals.set(0);
            }
            if (struct.isSetRange()) {
                optionals.set(1);
            }
            if (struct.isSetColumns()) {
                optionals.set(2);
            }
            if (struct.isSetIterators()) {
                optionals.set(3);
            }
            if (struct.isSetBufferSize()) {
                optionals.set(4);
            }
            oprot.writeBitSet(optionals, 5);
            if (struct.isSetAuthorizations()) {
                {
                    oprot.writeI32(struct.authorizations.size());
                    for (java.nio.ByteBuffer _iter38 : struct.authorizations) {
                        oprot.writeBinary(_iter38);
                    }
                }
            }
            if (struct.isSetRange()) {
                struct.range.write(oprot);
            }
            if (struct.isSetColumns()) {
                {
                    oprot.writeI32(struct.columns.size());
                    for (ScanColumn _iter39 : struct.columns) {
                        _iter39.write(oprot);
                    }
                }
            }
            if (struct.isSetIterators()) {
                {
                    oprot.writeI32(struct.iterators.size());
                    for (IteratorSetting _iter40 : struct.iterators) {
                        _iter40.write(oprot);
                    }
                }
            }
            if (struct.isSetBufferSize()) {
                oprot.writeI32(struct.bufferSize);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, ScanOptions struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(5);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TSet _set41 = new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.authorizations = new java.util.HashSet<java.nio.ByteBuffer>(2 * _set41.size);
                    @org.apache.thrift.annotation.Nullable
                    java.nio.ByteBuffer _elem42;
                    for (int _i43 = 0; _i43 < _set41.size; ++_i43) {
                        _elem42 = iprot.readBinary();
                        struct.authorizations.add(_elem42);
                    }
                }
                struct.setAuthorizationsIsSet(true);
            }
            if (incoming.get(1)) {
                struct.range = new Range();
                struct.range.read(iprot);
                struct.setRangeIsSet(true);
            }
            if (incoming.get(2)) {
                {
                    org.apache.thrift.protocol.TList _list44 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.columns = new java.util.ArrayList<ScanColumn>(_list44.size);
                    @org.apache.thrift.annotation.Nullable
                    ScanColumn _elem45;
                    for (int _i46 = 0; _i46 < _list44.size; ++_i46) {
                        _elem45 = new ScanColumn();
                        _elem45.read(iprot);
                        struct.columns.add(_elem45);
                    }
                }
                struct.setColumnsIsSet(true);
            }
            if (incoming.get(3)) {
                {
                    org.apache.thrift.protocol.TList _list47 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.iterators = new java.util.ArrayList<IteratorSetting>(_list47.size);
                    @org.apache.thrift.annotation.Nullable
                    IteratorSetting _elem48;
                    for (int _i49 = 0; _i49 < _list47.size; ++_i49) {
                        _elem48 = new IteratorSetting();
                        _elem48.read(iprot);
                        struct.iterators.add(_elem48);
                    }
                }
                struct.setIteratorsIsSet(true);
            }
            if (incoming.get(4)) {
                struct.bufferSize = iprot.readI32();
                struct.setBufferSizeIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}
