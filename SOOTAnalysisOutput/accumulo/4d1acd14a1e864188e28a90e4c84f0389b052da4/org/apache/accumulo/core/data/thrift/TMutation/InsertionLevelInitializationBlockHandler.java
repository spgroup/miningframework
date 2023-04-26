package org.apache.accumulo.core.data.thrift;

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

@SuppressWarnings({ "unchecked", "serial", "rawtypes", "unused" })
public class TMutation implements org.apache.thrift.TBase<TMutation, TMutation._Fields>, java.io.Serializable, Cloneable, Comparable<TMutation> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TMutation");

    private static final org.apache.thrift.protocol.TField ROW_FIELD_DESC = new org.apache.thrift.protocol.TField("row", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final org.apache.thrift.protocol.TField DATA_FIELD_DESC = new org.apache.thrift.protocol.TField("data", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.protocol.TField VALUES_FIELD_DESC = new org.apache.thrift.protocol.TField("values", org.apache.thrift.protocol.TType.LIST, (short) 3);

    private static final org.apache.thrift.protocol.TField ENTRIES_FIELD_DESC = new org.apache.thrift.protocol.TField("entries", org.apache.thrift.protocol.TType.I32, (short) 4);

    private static final org.apache.thrift.protocol.TField SOURCES_FIELD_DESC = new org.apache.thrift.protocol.TField("sources", org.apache.thrift.protocol.TType.LIST, (short) 5);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new TMutationStandardSchemeFactory());
        schemes.put(TupleScheme.class, new TMutationTupleSchemeFactory());
    }

    public ByteBuffer row;

    public ByteBuffer data;

    public List<ByteBuffer> values;

    public int entries;

    public List<String> sources;

        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        ROW((short) 1, "row"), DATA((short) 2, "data"), VALUES((short) 3, "values"), ENTRIES((short) 4, "entries"), SOURCES((short) 5, "sources");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return ROW;
                case 2:
                    return DATA;
                case 3:
                    return VALUES;
                case 4:
                    return ENTRIES;
                case 5:
                    return SOURCES;
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

    private static final int __ENTRIES_ISSET_ID = 0;

    private byte __isset_bitfield = 0;

    private _Fields[] optionals = { _Fields.SOURCES };

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.ROW, new org.apache.thrift.meta_data.FieldMetaData("row", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.DATA, new org.apache.thrift.meta_data.FieldMetaData("data", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.VALUES, new org.apache.thrift.meta_data.FieldMetaData("values", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true))));
        tmpMap.put(_Fields.ENTRIES, new org.apache.thrift.meta_data.FieldMetaData("entries", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.SOURCES, new org.apache.thrift.meta_data.FieldMetaData("sources", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TMutation.class, metaDataMap);
    }

    public TMutation() {
    }

    public TMutation(ByteBuffer row, ByteBuffer data, List<ByteBuffer> values, int entries) {
        this();
        this.row = row;
        this.data = data;
        this.values = values;
        this.entries = entries;
        setEntriesIsSet(true);
    }

    public TMutation(TMutation other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetRow()) {
            this.row = org.apache.thrift.TBaseHelper.copyBinary(other.row);
            ;
        }
        if (other.isSetData()) {
            this.data = org.apache.thrift.TBaseHelper.copyBinary(other.data);
            ;
        }
        if (other.isSetValues()) {
            List<ByteBuffer> __this__values = new ArrayList<ByteBuffer>(other.values);
            this.values = __this__values;
        }
        this.entries = other.entries;
        if (other.isSetSources()) {
            List<String> __this__sources = new ArrayList<String>(other.sources);
            this.sources = __this__sources;
        }
    }

    public TMutation deepCopy() {
        return new TMutation(this);
    }

    @Override
    public void clear() {
        this.row = null;
        this.data = null;
        this.values = null;
        setEntriesIsSet(false);
        this.entries = 0;
        this.sources = null;
    }

    public byte[] getRow() {
        setRow(org.apache.thrift.TBaseHelper.rightSize(row));
        return row == null ? null : row.array();
    }

    public ByteBuffer bufferForRow() {
        return row;
    }

    public TMutation setRow(byte[] row) {
        setRow(row == null ? (ByteBuffer) null : ByteBuffer.wrap(row));
        return this;
    }

    public TMutation setRow(ByteBuffer row) {
        this.row = row;
        return this;
    }

    public void unsetRow() {
        this.row = null;
    }

    public boolean isSetRow() {
        return this.row != null;
    }

    public void setRowIsSet(boolean value) {
        if (!value) {
            this.row = null;
        }
    }

    public byte[] getData() {
        setData(org.apache.thrift.TBaseHelper.rightSize(data));
        return data == null ? null : data.array();
    }

    public ByteBuffer bufferForData() {
        return data;
    }

    public TMutation setData(byte[] data) {
        setData(data == null ? (ByteBuffer) null : ByteBuffer.wrap(data));
        return this;
    }

    public TMutation setData(ByteBuffer data) {
        this.data = data;
        return this;
    }

    public void unsetData() {
        this.data = null;
    }

    public boolean isSetData() {
        return this.data != null;
    }

    public void setDataIsSet(boolean value) {
        if (!value) {
            this.data = null;
        }
    }

    public int getValuesSize() {
        return (this.values == null) ? 0 : this.values.size();
    }

    public java.util.Iterator<ByteBuffer> getValuesIterator() {
        return (this.values == null) ? null : this.values.iterator();
    }

    public void addToValues(ByteBuffer elem) {
        if (this.values == null) {
            this.values = new ArrayList<ByteBuffer>();
        }
        this.values.add(elem);
    }

    public List<ByteBuffer> getValues() {
        return this.values;
    }

    public TMutation setValues(List<ByteBuffer> values) {
        this.values = values;
        return this;
    }

    public void unsetValues() {
        this.values = null;
    }

    public boolean isSetValues() {
        return this.values != null;
    }

    public void setValuesIsSet(boolean value) {
        if (!value) {
            this.values = null;
        }
    }

    public int getEntries() {
        return this.entries;
    }

    public TMutation setEntries(int entries) {
        this.entries = entries;
        setEntriesIsSet(true);
        return this;
    }

    public void unsetEntries() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __ENTRIES_ISSET_ID);
    }

    public boolean isSetEntries() {
        return EncodingUtils.testBit(__isset_bitfield, __ENTRIES_ISSET_ID);
    }

    public void setEntriesIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __ENTRIES_ISSET_ID, value);
    }

    public int getSourcesSize() {
        return (this.sources == null) ? 0 : this.sources.size();
    }

    public java.util.Iterator<String> getSourcesIterator() {
        return (this.sources == null) ? null : this.sources.iterator();
    }

    public void addToSources(String elem) {
        if (this.sources == null) {
            this.sources = new ArrayList<String>();
        }
        this.sources.add(elem);
    }

    public List<String> getSources() {
        return this.sources;
    }

    public TMutation setSources(List<String> sources) {
        this.sources = sources;
        return this;
    }

    public void unsetSources() {
        this.sources = null;
    }

    public boolean isSetSources() {
        return this.sources != null;
    }

    public void setSourcesIsSet(boolean value) {
        if (!value) {
            this.sources = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case ROW:
                if (value == null) {
                    unsetRow();
                } else {
                    setRow((ByteBuffer) value);
                }
                break;
            case DATA:
                if (value == null) {
                    unsetData();
                } else {
                    setData((ByteBuffer) value);
                }
                break;
            case VALUES:
                if (value == null) {
                    unsetValues();
                } else {
                    setValues((List<ByteBuffer>) value);
                }
                break;
            case ENTRIES:
                if (value == null) {
                    unsetEntries();
                } else {
                    setEntries((Integer) value);
                }
                break;
            case SOURCES:
                if (value == null) {
                    unsetSources();
                } else {
                    setSources((List<String>) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case ROW:
                return getRow();
            case DATA:
                return getData();
            case VALUES:
                return getValues();
            case ENTRIES:
                return Integer.valueOf(getEntries());
            case SOURCES:
                return getSources();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case ROW:
                return isSetRow();
            case DATA:
                return isSetData();
            case VALUES:
                return isSetValues();
            case ENTRIES:
                return isSetEntries();
            case SOURCES:
                return isSetSources();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof TMutation)
            return this.equals((TMutation) that);
        return false;
    }

    public boolean equals(TMutation that) {
        if (that == null)
            return false;
        boolean this_present_row = true && this.isSetRow();
        boolean that_present_row = true && that.isSetRow();
        if (this_present_row || that_present_row) {
            if (!(this_present_row && that_present_row))
                return false;
            if (!this.row.equals(that.row))
                return false;
        }
        boolean this_present_data = true && this.isSetData();
        boolean that_present_data = true && that.isSetData();
        if (this_present_data || that_present_data) {
            if (!(this_present_data && that_present_data))
                return false;
            if (!this.data.equals(that.data))
                return false;
        }
        boolean this_present_values = true && this.isSetValues();
        boolean that_present_values = true && that.isSetValues();
        if (this_present_values || that_present_values) {
            if (!(this_present_values && that_present_values))
                return false;
            if (!this.values.equals(that.values))
                return false;
        }
        boolean this_present_entries = true;
        boolean that_present_entries = true;
        if (this_present_entries || that_present_entries) {
            if (!(this_present_entries && that_present_entries))
                return false;
            if (this.entries != that.entries)
                return false;
        }
        boolean this_present_sources = true && this.isSetSources();
        boolean that_present_sources = true && that.isSetSources();
        if (this_present_sources || that_present_sources) {
            if (!(this_present_sources && that_present_sources))
                return false;
            if (!this.sources.equals(that.sources))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int compareTo(TMutation other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetRow()).compareTo(other.isSetRow());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetRow()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.row, other.row);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetData()).compareTo(other.isSetData());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetData()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.data, other.data);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetValues()).compareTo(other.isSetValues());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetValues()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.values, other.values);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetEntries()).compareTo(other.isSetEntries());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetEntries()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.entries, other.entries);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetSources()).compareTo(other.isSetSources());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetSources()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sources, other.sources);
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
        StringBuilder sb = new StringBuilder("TMutation(");
        boolean first = true;
        sb.append("row:");
        if (this.row == null) {
            sb.append("null");
        } else {
            org.apache.thrift.TBaseHelper.toString(this.row, sb);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("data:");
        if (this.data == null) {
            sb.append("null");
        } else {
            org.apache.thrift.TBaseHelper.toString(this.data, sb);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("values:");
        if (this.values == null) {
            sb.append("null");
        } else {
            sb.append(this.values);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("entries:");
        sb.append(this.entries);
        first = false;
        if (isSetSources()) {
            if (!first)
                sb.append(", ");
            sb.append("sources:");
            if (this.sources == null) {
                sb.append("null");
            } else {
                sb.append(this.sources);
            }
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

    private static class TMutationStandardSchemeFactory implements SchemeFactory {

        public TMutationStandardScheme getScheme() {
            return new TMutationStandardScheme();
        }
    }

    private static class TMutationStandardScheme extends StandardScheme<TMutation> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, TMutation struct) throws org.apache.thrift.TException {
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
                            struct.row = iprot.readBinary();
                            struct.setRowIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.data = iprot.readBinary();
                            struct.setDataIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list0 = iprot.readListBegin();
                                struct.values = new ArrayList<ByteBuffer>(_list0.size);
                                for (int _i1 = 0; _i1 < _list0.size; ++_i1) {
                                    ByteBuffer _elem2;
                                    _elem2 = iprot.readBinary();
                                    struct.values.add(_elem2);
                                }
                                iprot.readListEnd();
                            }
                            struct.setValuesIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.entries = iprot.readI32();
                            struct.setEntriesIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 5:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list3 = iprot.readListBegin();
                                struct.sources = new ArrayList<String>(_list3.size);
                                for (int _i4 = 0; _i4 < _list3.size; ++_i4) {
                                    String _elem5;
                                    _elem5 = iprot.readString();
                                    struct.sources.add(_elem5);
                                }
                                iprot.readListEnd();
                            }
                            struct.setSourcesIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, TMutation struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.row != null) {
                oprot.writeFieldBegin(ROW_FIELD_DESC);
                oprot.writeBinary(struct.row);
                oprot.writeFieldEnd();
            }
            if (struct.data != null) {
                oprot.writeFieldBegin(DATA_FIELD_DESC);
                oprot.writeBinary(struct.data);
                oprot.writeFieldEnd();
            }
            if (struct.values != null) {
                oprot.writeFieldBegin(VALUES_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.values.size()));
                    for (ByteBuffer _iter6 : struct.values) {
                        oprot.writeBinary(_iter6);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(ENTRIES_FIELD_DESC);
            oprot.writeI32(struct.entries);
            oprot.writeFieldEnd();
            if (struct.sources != null) {
                if (struct.isSetSources()) {
                    oprot.writeFieldBegin(SOURCES_FIELD_DESC);
                    {
                        oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.sources.size()));
                        for (String _iter7 : struct.sources) {
                            oprot.writeString(_iter7);
                        }
                        oprot.writeListEnd();
                    }
                    oprot.writeFieldEnd();
                }
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class TMutationTupleSchemeFactory implements SchemeFactory {

        public TMutationTupleScheme getScheme() {
            return new TMutationTupleScheme();
        }
    }

    private static class TMutationTupleScheme extends TupleScheme<TMutation> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, TMutation struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetRow()) {
                optionals.set(0);
            }
            if (struct.isSetData()) {
                optionals.set(1);
            }
            if (struct.isSetValues()) {
                optionals.set(2);
            }
            if (struct.isSetEntries()) {
                optionals.set(3);
            }
            if (struct.isSetSources()) {
                optionals.set(4);
            }
            oprot.writeBitSet(optionals, 5);
            if (struct.isSetRow()) {
                oprot.writeBinary(struct.row);
            }
            if (struct.isSetData()) {
                oprot.writeBinary(struct.data);
            }
            if (struct.isSetValues()) {
                {
                    oprot.writeI32(struct.values.size());
                    for (ByteBuffer _iter8 : struct.values) {
                        oprot.writeBinary(_iter8);
                    }
                }
            }
            if (struct.isSetEntries()) {
                oprot.writeI32(struct.entries);
            }
            if (struct.isSetSources()) {
                {
                    oprot.writeI32(struct.sources.size());
                    for (String _iter9 : struct.sources) {
                        oprot.writeString(_iter9);
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, TMutation struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(5);
            if (incoming.get(0)) {
                struct.row = iprot.readBinary();
                struct.setRowIsSet(true);
            }
            if (incoming.get(1)) {
                struct.data = iprot.readBinary();
                struct.setDataIsSet(true);
            }
            if (incoming.get(2)) {
                {
                    org.apache.thrift.protocol.TList _list10 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.values = new ArrayList<ByteBuffer>(_list10.size);
                    for (int _i11 = 0; _i11 < _list10.size; ++_i11) {
                        ByteBuffer _elem12;
                        _elem12 = iprot.readBinary();
                        struct.values.add(_elem12);
                    }
                }
                struct.setValuesIsSet(true);
            }
            if (incoming.get(3)) {
                struct.entries = iprot.readI32();
                struct.setEntriesIsSet(true);
            }
            if (incoming.get(4)) {
                {
                    org.apache.thrift.protocol.TList _list13 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.sources = new ArrayList<String>(_list13.size);
                    for (int _i14 = 0; _i14 < _list13.size; ++_i14) {
                        String _elem15;
                        _elem15 = iprot.readString();
                        struct.sources.add(_elem15);
                    }
                }
                struct.setSourcesIsSet(true);
            }
        }
    }
}