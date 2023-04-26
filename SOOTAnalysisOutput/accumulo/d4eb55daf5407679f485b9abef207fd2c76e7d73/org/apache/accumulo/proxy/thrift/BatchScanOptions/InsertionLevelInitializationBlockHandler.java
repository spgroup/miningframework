package org.apache.accumulo.proxy.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class BatchScanOptions implements org.apache.thrift.TBase<BatchScanOptions, BatchScanOptions._Fields>, java.io.Serializable, Cloneable, Comparable<BatchScanOptions> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("BatchScanOptions");

    private static final org.apache.thrift.protocol.TField AUTHORIZATIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("authorizations", org.apache.thrift.protocol.TType.SET, (short) 1);

    private static final org.apache.thrift.protocol.TField RANGES_FIELD_DESC = new org.apache.thrift.protocol.TField("ranges", org.apache.thrift.protocol.TType.LIST, (short) 2);

    private static final org.apache.thrift.protocol.TField COLUMNS_FIELD_DESC = new org.apache.thrift.protocol.TField("columns", org.apache.thrift.protocol.TType.LIST, (short) 3);

    private static final org.apache.thrift.protocol.TField ITERATORS_FIELD_DESC = new org.apache.thrift.protocol.TField("iterators", org.apache.thrift.protocol.TType.LIST, (short) 4);

    private static final org.apache.thrift.protocol.TField THREADS_FIELD_DESC = new org.apache.thrift.protocol.TField("threads", org.apache.thrift.protocol.TType.I32, (short) 5);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new BatchScanOptionsStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new BatchScanOptionsTupleSchemeFactory();

    @org.apache.thrift.annotation.Nullable
    public java.util.Set<java.nio.ByteBuffer> authorizations;

    @org.apache.thrift.annotation.Nullable
    public java.util.List<Range> ranges;

    @org.apache.thrift.annotation.Nullable
    public java.util.List<ScanColumn> columns;

    @org.apache.thrift.annotation.Nullable
    public java.util.List<IteratorSetting> iterators;

    public int threads;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        AUTHORIZATIONS((short) 1, "authorizations"), RANGES((short) 2, "ranges"), COLUMNS((short) 3, "columns"), ITERATORS((short) 4, "iterators"), THREADS((short) 5, "threads");

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
                    return RANGES;
                case 3:
                    return COLUMNS;
                case 4:
                    return ITERATORS;
                case 5:
                    return THREADS;
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

    private static final int __THREADS_ISSET_ID = 0;

    private byte __isset_bitfield = 0;

    private static final _Fields[] optionals = { _Fields.AUTHORIZATIONS, _Fields.RANGES, _Fields.COLUMNS, _Fields.ITERATORS, _Fields.THREADS };

    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.AUTHORIZATIONS, new org.apache.thrift.meta_data.FieldMetaData("authorizations", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.SetMetaData(org.apache.thrift.protocol.TType.SET, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true))));
        tmpMap.put(_Fields.RANGES, new org.apache.thrift.meta_data.FieldMetaData("ranges", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Range.class))));
        tmpMap.put(_Fields.COLUMNS, new org.apache.thrift.meta_data.FieldMetaData("columns", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, ScanColumn.class))));
        tmpMap.put(_Fields.ITERATORS, new org.apache.thrift.meta_data.FieldMetaData("iterators", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, IteratorSetting.class))));
        tmpMap.put(_Fields.THREADS, new org.apache.thrift.meta_data.FieldMetaData("threads", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(BatchScanOptions.class, metaDataMap);
    }

    public BatchScanOptions() {
    }

    public BatchScanOptions(BatchScanOptions other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetAuthorizations()) {
            java.util.Set<java.nio.ByteBuffer> __this__authorizations = new java.util.HashSet<java.nio.ByteBuffer>(other.authorizations);
            this.authorizations = __this__authorizations;
        }
        if (other.isSetRanges()) {
            java.util.List<Range> __this__ranges = new java.util.ArrayList<Range>(other.ranges.size());
            for (Range other_element : other.ranges) {
                __this__ranges.add(new Range(other_element));
            }
            this.ranges = __this__ranges;
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
        this.threads = other.threads;
    }

    public BatchScanOptions deepCopy() {
        return new BatchScanOptions(this);
    }

    @Override
    public void clear() {
        this.authorizations = null;
        this.ranges = null;
        this.columns = null;
        this.iterators = null;
        setThreadsIsSet(false);
        this.threads = 0;
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

    public BatchScanOptions setAuthorizations(@org.apache.thrift.annotation.Nullable java.util.Set<java.nio.ByteBuffer> authorizations) {
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

    public int getRangesSize() {
        return (this.ranges == null) ? 0 : this.ranges.size();
    }

    @org.apache.thrift.annotation.Nullable
public java.util.Iterator<Range> getRangesIterator() {
        return (this.ranges == null) ? null : this.ranges.iterator();
    }

    public void addToRanges(Range elem) {
        if (this.ranges == null) {
            this.ranges = new java.util.ArrayList<Range>();
        }
        this.ranges.add(elem);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.List<Range> getRanges() {
        return this.ranges;
    }

    public BatchScanOptions setRanges(@org.apache.thrift.annotation.Nullable java.util.List<Range> ranges) {
        this.ranges = ranges;
        return this;
    }

    public void unsetRanges() {
        this.ranges = null;
    }

    public boolean isSetRanges() {
        return this.ranges != null;
    }

    public void setRangesIsSet(boolean value) {
        if (!value) {
            this.ranges = null;
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

    public BatchScanOptions setColumns(@org.apache.thrift.annotation.Nullable java.util.List<ScanColumn> columns) {
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

    public BatchScanOptions setIterators(@org.apache.thrift.annotation.Nullable java.util.List<IteratorSetting> iterators) {
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

    public int getThreads() {
        return this.threads;
    }

    public BatchScanOptions setThreads(int threads) {
        this.threads = threads;
        setThreadsIsSet(true);
        return this;
    }

    public void unsetThreads() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __THREADS_ISSET_ID);
    }

    public boolean isSetThreads() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __THREADS_ISSET_ID);
    }

    public void setThreadsIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __THREADS_ISSET_ID, value);
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
            case RANGES:
                if (value == null) {
                    unsetRanges();
                } else {
                    setRanges((java.util.List<Range>) value);
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
            case THREADS:
                if (value == null) {
                    unsetThreads();
                } else {
                    setThreads((java.lang.Integer) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case AUTHORIZATIONS:
                return getAuthorizations();
            case RANGES:
                return getRanges();
            case COLUMNS:
                return getColumns();
            case ITERATORS:
                return getIterators();
            case THREADS:
                return getThreads();
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
            case RANGES:
                return isSetRanges();
            case COLUMNS:
                return isSetColumns();
            case ITERATORS:
                return isSetIterators();
            case THREADS:
                return isSetThreads();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof BatchScanOptions)
            return this.equals((BatchScanOptions) that);
        return false;
    }

    public boolean equals(BatchScanOptions that) {
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
        boolean this_present_ranges = true && this.isSetRanges();
        boolean that_present_ranges = true && that.isSetRanges();
        if (this_present_ranges || that_present_ranges) {
            if (!(this_present_ranges && that_present_ranges))
                return false;
            if (!this.ranges.equals(that.ranges))
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
        boolean this_present_threads = true && this.isSetThreads();
        boolean that_present_threads = true && that.isSetThreads();
        if (this_present_threads || that_present_threads) {
            if (!(this_present_threads && that_present_threads))
                return false;
            if (this.threads != that.threads)
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
        hashCode = hashCode * 8191 + ((isSetRanges()) ? 131071 : 524287);
        if (isSetRanges())
            hashCode = hashCode * 8191 + ranges.hashCode();
        hashCode = hashCode * 8191 + ((isSetColumns()) ? 131071 : 524287);
        if (isSetColumns())
            hashCode = hashCode * 8191 + columns.hashCode();
        hashCode = hashCode * 8191 + ((isSetIterators()) ? 131071 : 524287);
        if (isSetIterators())
            hashCode = hashCode * 8191 + iterators.hashCode();
        hashCode = hashCode * 8191 + ((isSetThreads()) ? 131071 : 524287);
        if (isSetThreads())
            hashCode = hashCode * 8191 + threads;
        return hashCode;
    }

    @Override
    public int compareTo(BatchScanOptions other) {
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
        lastComparison = java.lang.Boolean.valueOf(isSetRanges()).compareTo(other.isSetRanges());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetRanges()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ranges, other.ranges);
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
        lastComparison = java.lang.Boolean.valueOf(isSetThreads()).compareTo(other.isSetThreads());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetThreads()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.threads, other.threads);
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("BatchScanOptions(");
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
        if (isSetRanges()) {
            if (!first)
                sb.append(", ");
            sb.append("ranges:");
            if (this.ranges == null) {
                sb.append("null");
            } else {
                sb.append(this.ranges);
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
        if (isSetThreads()) {
            if (!first)
                sb.append(", ");
            sb.append("threads:");
            sb.append(this.threads);
            first = false;
        }
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

    private static class BatchScanOptionsStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public BatchScanOptionsStandardScheme getScheme() {
            return new BatchScanOptionsStandardScheme();
        }
    }

    private static class BatchScanOptionsStandardScheme extends org.apache.thrift.scheme.StandardScheme<BatchScanOptions> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, BatchScanOptions struct) throws org.apache.thrift.TException {
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
                                org.apache.thrift.protocol.TSet _set50 = iprot.readSetBegin();
                                struct.authorizations = new java.util.HashSet<java.nio.ByteBuffer>(2 * _set50.size);
                                @org.apache.thrift.annotation.Nullable
                                java.nio.ByteBuffer _elem51;
                                for (int _i52 = 0; _i52 < _set50.size; ++_i52) {
                                    _elem51 = iprot.readBinary();
                                    struct.authorizations.add(_elem51);
                                }
                                iprot.readSetEnd();
                            }
                            struct.setAuthorizationsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list53 = iprot.readListBegin();
                                struct.ranges = new java.util.ArrayList<Range>(_list53.size);
                                @org.apache.thrift.annotation.Nullable
                                Range _elem54;
                                for (int _i55 = 0; _i55 < _list53.size; ++_i55) {
                                    _elem54 = new Range();
                                    _elem54.read(iprot);
                                    struct.ranges.add(_elem54);
                                }
                                iprot.readListEnd();
                            }
                            struct.setRangesIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list56 = iprot.readListBegin();
                                struct.columns = new java.util.ArrayList<ScanColumn>(_list56.size);
                                @org.apache.thrift.annotation.Nullable
                                ScanColumn _elem57;
                                for (int _i58 = 0; _i58 < _list56.size; ++_i58) {
                                    _elem57 = new ScanColumn();
                                    _elem57.read(iprot);
                                    struct.columns.add(_elem57);
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
                                org.apache.thrift.protocol.TList _list59 = iprot.readListBegin();
                                struct.iterators = new java.util.ArrayList<IteratorSetting>(_list59.size);
                                @org.apache.thrift.annotation.Nullable
                                IteratorSetting _elem60;
                                for (int _i61 = 0; _i61 < _list59.size; ++_i61) {
                                    _elem60 = new IteratorSetting();
                                    _elem60.read(iprot);
                                    struct.iterators.add(_elem60);
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
                            struct.threads = iprot.readI32();
                            struct.setThreadsIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, BatchScanOptions struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.authorizations != null) {
                if (struct.isSetAuthorizations()) {
                    oprot.writeFieldBegin(AUTHORIZATIONS_FIELD_DESC);
                    {
                        oprot.writeSetBegin(new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.STRING, struct.authorizations.size()));
                        for (java.nio.ByteBuffer _iter62 : struct.authorizations) {
                            oprot.writeBinary(_iter62);
                        }
                        oprot.writeSetEnd();
                    }
                    oprot.writeFieldEnd();
                }
            }
            if (struct.ranges != null) {
                if (struct.isSetRanges()) {
                    oprot.writeFieldBegin(RANGES_FIELD_DESC);
                    {
                        oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.ranges.size()));
                        for (Range _iter63 : struct.ranges) {
                            _iter63.write(oprot);
                        }
                        oprot.writeListEnd();
                    }
                    oprot.writeFieldEnd();
                }
            }
            if (struct.columns != null) {
                if (struct.isSetColumns()) {
                    oprot.writeFieldBegin(COLUMNS_FIELD_DESC);
                    {
                        oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.columns.size()));
                        for (ScanColumn _iter64 : struct.columns) {
                            _iter64.write(oprot);
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
                        for (IteratorSetting _iter65 : struct.iterators) {
                            _iter65.write(oprot);
                        }
                        oprot.writeListEnd();
                    }
                    oprot.writeFieldEnd();
                }
            }
            if (struct.isSetThreads()) {
                oprot.writeFieldBegin(THREADS_FIELD_DESC);
                oprot.writeI32(struct.threads);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class BatchScanOptionsTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public BatchScanOptionsTupleScheme getScheme() {
            return new BatchScanOptionsTupleScheme();
        }
    }

    private static class BatchScanOptionsTupleScheme extends org.apache.thrift.scheme.TupleScheme<BatchScanOptions> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, BatchScanOptions struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
            if (struct.isSetAuthorizations()) {
                optionals.set(0);
            }
            if (struct.isSetRanges()) {
                optionals.set(1);
            }
            if (struct.isSetColumns()) {
                optionals.set(2);
            }
            if (struct.isSetIterators()) {
                optionals.set(3);
            }
            if (struct.isSetThreads()) {
                optionals.set(4);
            }
            oprot.writeBitSet(optionals, 5);
            if (struct.isSetAuthorizations()) {
                {
                    oprot.writeI32(struct.authorizations.size());
                    for (java.nio.ByteBuffer _iter66 : struct.authorizations) {
                        oprot.writeBinary(_iter66);
                    }
                }
            }
            if (struct.isSetRanges()) {
                {
                    oprot.writeI32(struct.ranges.size());
                    for (Range _iter67 : struct.ranges) {
                        _iter67.write(oprot);
                    }
                }
            }
            if (struct.isSetColumns()) {
                {
                    oprot.writeI32(struct.columns.size());
                    for (ScanColumn _iter68 : struct.columns) {
                        _iter68.write(oprot);
                    }
                }
            }
            if (struct.isSetIterators()) {
                {
                    oprot.writeI32(struct.iterators.size());
                    for (IteratorSetting _iter69 : struct.iterators) {
                        _iter69.write(oprot);
                    }
                }
            }
            if (struct.isSetThreads()) {
                oprot.writeI32(struct.threads);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, BatchScanOptions struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(5);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TSet _set70 = new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.authorizations = new java.util.HashSet<java.nio.ByteBuffer>(2 * _set70.size);
                    @org.apache.thrift.annotation.Nullable
                    java.nio.ByteBuffer _elem71;
                    for (int _i72 = 0; _i72 < _set70.size; ++_i72) {
                        _elem71 = iprot.readBinary();
                        struct.authorizations.add(_elem71);
                    }
                }
                struct.setAuthorizationsIsSet(true);
            }
            if (incoming.get(1)) {
                {
                    org.apache.thrift.protocol.TList _list73 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.ranges = new java.util.ArrayList<Range>(_list73.size);
                    @org.apache.thrift.annotation.Nullable
                    Range _elem74;
                    for (int _i75 = 0; _i75 < _list73.size; ++_i75) {
                        _elem74 = new Range();
                        _elem74.read(iprot);
                        struct.ranges.add(_elem74);
                    }
                }
                struct.setRangesIsSet(true);
            }
            if (incoming.get(2)) {
                {
                    org.apache.thrift.protocol.TList _list76 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.columns = new java.util.ArrayList<ScanColumn>(_list76.size);
                    @org.apache.thrift.annotation.Nullable
                    ScanColumn _elem77;
                    for (int _i78 = 0; _i78 < _list76.size; ++_i78) {
                        _elem77 = new ScanColumn();
                        _elem77.read(iprot);
                        struct.columns.add(_elem77);
                    }
                }
                struct.setColumnsIsSet(true);
            }
            if (incoming.get(3)) {
                {
                    org.apache.thrift.protocol.TList _list79 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.iterators = new java.util.ArrayList<IteratorSetting>(_list79.size);
                    @org.apache.thrift.annotation.Nullable
                    IteratorSetting _elem80;
                    for (int _i81 = 0; _i81 < _list79.size; ++_i81) {
                        _elem80 = new IteratorSetting();
                        _elem80.read(iprot);
                        struct.iterators.add(_elem80);
                    }
                }
                struct.setIteratorsIsSet(true);
            }
            if (incoming.get(4)) {
                struct.threads = iprot.readI32();
                struct.setThreadsIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}