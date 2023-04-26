package org.apache.accumulo.proxy.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;
import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
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
public class BatchScanOptions implements org.apache.thrift.TBase<BatchScanOptions, BatchScanOptions._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("BatchScanOptions");

    private static final org.apache.thrift.protocol.TField AUTHORIZATIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("authorizations", org.apache.thrift.protocol.TType.SET, (short) 1);

    private static final org.apache.thrift.protocol.TField RANGES_FIELD_DESC = new org.apache.thrift.protocol.TField("ranges", org.apache.thrift.protocol.TType.LIST, (short) 2);

    private static final org.apache.thrift.protocol.TField COLUMNS_FIELD_DESC = new org.apache.thrift.protocol.TField("columns", org.apache.thrift.protocol.TType.LIST, (short) 3);

    private static final org.apache.thrift.protocol.TField ITERATORS_FIELD_DESC = new org.apache.thrift.protocol.TField("iterators", org.apache.thrift.protocol.TType.LIST, (short) 4);

    private static final org.apache.thrift.protocol.TField THREADS_FIELD_DESC = new org.apache.thrift.protocol.TField("threads", org.apache.thrift.protocol.TType.I32, (short) 5);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new BatchScanOptionsStandardSchemeFactory());
        schemes.put(TupleScheme.class, new BatchScanOptionsTupleSchemeFactory());
    }

    public Set<ByteBuffer> authorizations;

    public List<Range> ranges;

    public List<ScanColumn> columns;

    public List<IteratorSetting> iterators;

    public int threads;

    @SuppressWarnings("all")
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        AUTHORIZATIONS((short) 1, "authorizations"), RANGES((short) 2, "ranges"), COLUMNS((short) 3, "columns"), ITERATORS((short) 4, "iterators"), THREADS((short) 5, "threads");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

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

    private static final int __THREADS_ISSET_ID = 0;

    private byte __isset_bitfield = 0;

    private _Fields[] optionals = { _Fields.AUTHORIZATIONS, _Fields.RANGES, _Fields.COLUMNS, _Fields.ITERATORS, _Fields.THREADS };

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.AUTHORIZATIONS, new org.apache.thrift.meta_data.FieldMetaData("authorizations", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.SetMetaData(org.apache.thrift.protocol.TType.SET, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true))));
        tmpMap.put(_Fields.RANGES, new org.apache.thrift.meta_data.FieldMetaData("ranges", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Range.class))));
        tmpMap.put(_Fields.COLUMNS, new org.apache.thrift.meta_data.FieldMetaData("columns", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, ScanColumn.class))));
        tmpMap.put(_Fields.ITERATORS, new org.apache.thrift.meta_data.FieldMetaData("iterators", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, IteratorSetting.class))));
        tmpMap.put(_Fields.THREADS, new org.apache.thrift.meta_data.FieldMetaData("threads", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(BatchScanOptions.class, metaDataMap);
    }

    public BatchScanOptions() {
    }

    public BatchScanOptions(BatchScanOptions other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetAuthorizations()) {
            Set<ByteBuffer> __this__authorizations = new HashSet<ByteBuffer>();
            for (ByteBuffer other_element : other.authorizations) {
                ByteBuffer temp_binary_element = org.apache.thrift.TBaseHelper.copyBinary(other_element);
                ;
                __this__authorizations.add(temp_binary_element);
            }
            this.authorizations = __this__authorizations;
        }
        if (other.isSetRanges()) {
            List<Range> __this__ranges = new ArrayList<Range>();
            for (Range other_element : other.ranges) {
                __this__ranges.add(new Range(other_element));
            }
            this.ranges = __this__ranges;
        }
        if (other.isSetColumns()) {
            List<ScanColumn> __this__columns = new ArrayList<ScanColumn>();
            for (ScanColumn other_element : other.columns) {
                __this__columns.add(new ScanColumn(other_element));
            }
            this.columns = __this__columns;
        }
        if (other.isSetIterators()) {
            List<IteratorSetting> __this__iterators = new ArrayList<IteratorSetting>();
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

    public java.util.Iterator<ByteBuffer> getAuthorizationsIterator() {
        return (this.authorizations == null) ? null : this.authorizations.iterator();
    }

    public void addToAuthorizations(ByteBuffer elem) {
        if (this.authorizations == null) {
            this.authorizations = new HashSet<ByteBuffer>();
        }
        this.authorizations.add(elem);
    }

    public Set<ByteBuffer> getAuthorizations() {
        return this.authorizations;
    }

    public BatchScanOptions setAuthorizations(Set<ByteBuffer> authorizations) {
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

    public java.util.Iterator<Range> getRangesIterator() {
        return (this.ranges == null) ? null : this.ranges.iterator();
    }

    public void addToRanges(Range elem) {
        if (this.ranges == null) {
            this.ranges = new ArrayList<Range>();
        }
        this.ranges.add(elem);
    }

    public List<Range> getRanges() {
        return this.ranges;
    }

    public BatchScanOptions setRanges(List<Range> ranges) {
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

    public java.util.Iterator<ScanColumn> getColumnsIterator() {
        return (this.columns == null) ? null : this.columns.iterator();
    }

    public void addToColumns(ScanColumn elem) {
        if (this.columns == null) {
            this.columns = new ArrayList<ScanColumn>();
        }
        this.columns.add(elem);
    }

    public List<ScanColumn> getColumns() {
        return this.columns;
    }

    public BatchScanOptions setColumns(List<ScanColumn> columns) {
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

    public java.util.Iterator<IteratorSetting> getIteratorsIterator() {
        return (this.iterators == null) ? null : this.iterators.iterator();
    }

    public void addToIterators(IteratorSetting elem) {
        if (this.iterators == null) {
            this.iterators = new ArrayList<IteratorSetting>();
        }
        this.iterators.add(elem);
    }

    public List<IteratorSetting> getIterators() {
        return this.iterators;
    }

    public BatchScanOptions setIterators(List<IteratorSetting> iterators) {
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
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __THREADS_ISSET_ID);
    }

    public boolean isSetThreads() {
        return EncodingUtils.testBit(__isset_bitfield, __THREADS_ISSET_ID);
    }

    public void setThreadsIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __THREADS_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case AUTHORIZATIONS:
                if (value == null) {
                    unsetAuthorizations();
                } else {
                    setAuthorizations((Set<ByteBuffer>) value);
                }
                break;
            case RANGES:
                if (value == null) {
                    unsetRanges();
                } else {
                    setRanges((List<Range>) value);
                }
                break;
            case COLUMNS:
                if (value == null) {
                    unsetColumns();
                } else {
                    setColumns((List<ScanColumn>) value);
                }
                break;
            case ITERATORS:
                if (value == null) {
                    unsetIterators();
                } else {
                    setIterators((List<IteratorSetting>) value);
                }
                break;
            case THREADS:
                if (value == null) {
                    unsetThreads();
                } else {
                    setThreads((Integer) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
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
                return Integer.valueOf(getThreads());
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
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
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof BatchScanOptions)
            return this.equals((BatchScanOptions) that);
        return false;
    }

    public boolean equals(BatchScanOptions that) {
        if (that == null)
            return false;
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
        return 0;
    }

    public int compareTo(BatchScanOptions other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        BatchScanOptions typedOther = (BatchScanOptions) other;
        lastComparison = Boolean.valueOf(isSetAuthorizations()).compareTo(typedOther.isSetAuthorizations());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetAuthorizations()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.authorizations, typedOther.authorizations);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetRanges()).compareTo(typedOther.isSetRanges());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetRanges()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ranges, typedOther.ranges);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetColumns()).compareTo(typedOther.isSetColumns());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetColumns()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.columns, typedOther.columns);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetIterators()).compareTo(typedOther.isSetIterators());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetIterators()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.iterators, typedOther.iterators);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetThreads()).compareTo(typedOther.isSetThreads());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetThreads()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.threads, typedOther.threads);
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
        StringBuilder sb = new StringBuilder("BatchScanOptions(");
        boolean first = true;
        if (isSetAuthorizations()) {
            sb.append("authorizations:");
            if (this.authorizations == null) {
                sb.append("null");
            } else {
                sb.append(this.authorizations);
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

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        try {
            __isset_bitfield = 0;
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class BatchScanOptionsStandardSchemeFactory implements SchemeFactory {

        public BatchScanOptionsStandardScheme getScheme() {
            return new BatchScanOptionsStandardScheme();
        }
    }

    private static class BatchScanOptionsStandardScheme extends StandardScheme<BatchScanOptions> {

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
                                struct.authorizations = new HashSet<ByteBuffer>(2 * _set50.size);
                                for (int _i51 = 0; _i51 < _set50.size; ++_i51) {
                                    ByteBuffer _elem52;
                                    _elem52 = iprot.readBinary();
                                    struct.authorizations.add(_elem52);
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
                                struct.ranges = new ArrayList<Range>(_list53.size);
                                for (int _i54 = 0; _i54 < _list53.size; ++_i54) {
                                    Range _elem55;
                                    _elem55 = new Range();
                                    _elem55.read(iprot);
                                    struct.ranges.add(_elem55);
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
                                struct.columns = new ArrayList<ScanColumn>(_list56.size);
                                for (int _i57 = 0; _i57 < _list56.size; ++_i57) {
                                    ScanColumn _elem58;
                                    _elem58 = new ScanColumn();
                                    _elem58.read(iprot);
                                    struct.columns.add(_elem58);
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
                                struct.iterators = new ArrayList<IteratorSetting>(_list59.size);
                                for (int _i60 = 0; _i60 < _list59.size; ++_i60) {
                                    IteratorSetting _elem61;
                                    _elem61 = new IteratorSetting();
                                    _elem61.read(iprot);
                                    struct.iterators.add(_elem61);
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
                        for (ByteBuffer _iter62 : struct.authorizations) {
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

    private static class BatchScanOptionsTupleSchemeFactory implements SchemeFactory {

        public BatchScanOptionsTupleScheme getScheme() {
            return new BatchScanOptionsTupleScheme();
        }
    }

    private static class BatchScanOptionsTupleScheme extends TupleScheme<BatchScanOptions> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, BatchScanOptions struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
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
                    for (ByteBuffer _iter66 : struct.authorizations) {
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
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(5);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TSet _set70 = new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.authorizations = new HashSet<ByteBuffer>(2 * _set70.size);
                    for (int _i71 = 0; _i71 < _set70.size; ++_i71) {
                        ByteBuffer _elem72;
                        _elem72 = iprot.readBinary();
                        struct.authorizations.add(_elem72);
                    }
                }
                struct.setAuthorizationsIsSet(true);
            }
            if (incoming.get(1)) {
                {
                    org.apache.thrift.protocol.TList _list73 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.ranges = new ArrayList<Range>(_list73.size);
                    for (int _i74 = 0; _i74 < _list73.size; ++_i74) {
                        Range _elem75;
                        _elem75 = new Range();
                        _elem75.read(iprot);
                        struct.ranges.add(_elem75);
                    }
                }
                struct.setRangesIsSet(true);
            }
            if (incoming.get(2)) {
                {
                    org.apache.thrift.protocol.TList _list76 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.columns = new ArrayList<ScanColumn>(_list76.size);
                    for (int _i77 = 0; _i77 < _list76.size; ++_i77) {
                        ScanColumn _elem78;
                        _elem78 = new ScanColumn();
                        _elem78.read(iprot);
                        struct.columns.add(_elem78);
                    }
                }
                struct.setColumnsIsSet(true);
            }
            if (incoming.get(3)) {
                {
                    org.apache.thrift.protocol.TList _list79 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.iterators = new ArrayList<IteratorSetting>(_list79.size);
                    for (int _i80 = 0; _i80 < _list79.size; ++_i80) {
                        IteratorSetting _elem81;
                        _elem81 = new IteratorSetting();
                        _elem81.read(iprot);
                        struct.iterators.add(_elem81);
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
}
