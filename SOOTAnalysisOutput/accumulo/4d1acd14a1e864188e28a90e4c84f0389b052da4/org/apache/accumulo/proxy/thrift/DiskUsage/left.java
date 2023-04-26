package org.apache.accumulo.proxy.thrift;

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
public class DiskUsage implements org.apache.thrift.TBase<DiskUsage, DiskUsage._Fields>, java.io.Serializable, Cloneable, Comparable<DiskUsage> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("DiskUsage");

    private static final org.apache.thrift.protocol.TField TABLES_FIELD_DESC = new org.apache.thrift.protocol.TField("tables", org.apache.thrift.protocol.TType.LIST, (short) 1);

    private static final org.apache.thrift.protocol.TField USAGE_FIELD_DESC = new org.apache.thrift.protocol.TField("usage", org.apache.thrift.protocol.TType.I64, (short) 2);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new DiskUsageStandardSchemeFactory());
        schemes.put(TupleScheme.class, new DiskUsageTupleSchemeFactory());
    }

    public List<String> tables;

    public long usage;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        TABLES((short) 1, "tables"), USAGE((short) 2, "usage");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return TABLES;
                case 2:
                    return USAGE;
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

    private static final int __USAGE_ISSET_ID = 0;

    private byte __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.TABLES, new org.apache.thrift.meta_data.FieldMetaData("tables", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
        tmpMap.put(_Fields.USAGE, new org.apache.thrift.meta_data.FieldMetaData("usage", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(DiskUsage.class, metaDataMap);
    }

    public DiskUsage() {
    }

    public DiskUsage(List<String> tables, long usage) {
        this();
        this.tables = tables;
        this.usage = usage;
        setUsageIsSet(true);
    }

    public DiskUsage(DiskUsage other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetTables()) {
            List<String> __this__tables = new ArrayList<String>(other.tables);
            this.tables = __this__tables;
        }
        this.usage = other.usage;
    }

    public DiskUsage deepCopy() {
        return new DiskUsage(this);
    }

    @Override
    public void clear() {
        this.tables = null;
        setUsageIsSet(false);
        this.usage = 0;
    }

    public int getTablesSize() {
        return (this.tables == null) ? 0 : this.tables.size();
    }

    public java.util.Iterator<String> getTablesIterator() {
        return (this.tables == null) ? null : this.tables.iterator();
    }

    public void addToTables(String elem) {
        if (this.tables == null) {
            this.tables = new ArrayList<String>();
        }
        this.tables.add(elem);
    }

    public List<String> getTables() {
        return this.tables;
    }

    public DiskUsage setTables(List<String> tables) {
        this.tables = tables;
        return this;
    }

    public void unsetTables() {
        this.tables = null;
    }

    public boolean isSetTables() {
        return this.tables != null;
    }

    public void setTablesIsSet(boolean value) {
        if (!value) {
            this.tables = null;
        }
    }

    public long getUsage() {
        return this.usage;
    }

    public DiskUsage setUsage(long usage) {
        this.usage = usage;
        setUsageIsSet(true);
        return this;
    }

    public void unsetUsage() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __USAGE_ISSET_ID);
    }

    public boolean isSetUsage() {
        return EncodingUtils.testBit(__isset_bitfield, __USAGE_ISSET_ID);
    }

    public void setUsageIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __USAGE_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case TABLES:
                if (value == null) {
                    unsetTables();
                } else {
                    setTables((List<String>) value);
                }
                break;
            case USAGE:
                if (value == null) {
                    unsetUsage();
                } else {
                    setUsage((Long) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case TABLES:
                return getTables();
            case USAGE:
                return Long.valueOf(getUsage());
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case TABLES:
                return isSetTables();
            case USAGE:
                return isSetUsage();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof DiskUsage)
            return this.equals((DiskUsage) that);
        return false;
    }

    public boolean equals(DiskUsage that) {
        if (that == null)
            return false;
        boolean this_present_tables = true && this.isSetTables();
        boolean that_present_tables = true && that.isSetTables();
        if (this_present_tables || that_present_tables) {
            if (!(this_present_tables && that_present_tables))
                return false;
            if (!this.tables.equals(that.tables))
                return false;
        }
        boolean this_present_usage = true;
        boolean that_present_usage = true;
        if (this_present_usage || that_present_usage) {
            if (!(this_present_usage && that_present_usage))
                return false;
            if (this.usage != that.usage)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int compareTo(DiskUsage other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetTables()).compareTo(other.isSetTables());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTables()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tables, other.tables);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetUsage()).compareTo(other.isSetUsage());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetUsage()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.usage, other.usage);
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
        StringBuilder sb = new StringBuilder("DiskUsage(");
        boolean first = true;
        sb.append("tables:");
        if (this.tables == null) {
            sb.append("null");
        } else {
            sb.append(this.tables);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("usage:");
        sb.append(this.usage);
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
            __isset_bitfield = 0;
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class DiskUsageStandardSchemeFactory implements SchemeFactory {

        public DiskUsageStandardScheme getScheme() {
            return new DiskUsageStandardScheme();
        }
    }

    private static class DiskUsageStandardScheme extends StandardScheme<DiskUsage> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, DiskUsage struct) throws org.apache.thrift.TException {
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
                                struct.tables = new ArrayList<String>(_list0.size);
                                for (int _i1 = 0; _i1 < _list0.size; ++_i1) {
                                    String _elem2;
                                    _elem2 = iprot.readString();
                                    struct.tables.add(_elem2);
                                }
                                iprot.readListEnd();
                            }
                            struct.setTablesIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.usage = iprot.readI64();
                            struct.setUsageIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, DiskUsage struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.tables != null) {
                oprot.writeFieldBegin(TABLES_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.tables.size()));
                    for (String _iter3 : struct.tables) {
                        oprot.writeString(_iter3);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(USAGE_FIELD_DESC);
            oprot.writeI64(struct.usage);
            oprot.writeFieldEnd();
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class DiskUsageTupleSchemeFactory implements SchemeFactory {

        public DiskUsageTupleScheme getScheme() {
            return new DiskUsageTupleScheme();
        }
    }

    private static class DiskUsageTupleScheme extends TupleScheme<DiskUsage> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, DiskUsage struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetTables()) {
                optionals.set(0);
            }
            if (struct.isSetUsage()) {
                optionals.set(1);
            }
            oprot.writeBitSet(optionals, 2);
            if (struct.isSetTables()) {
                {
                    oprot.writeI32(struct.tables.size());
                    for (String _iter4 : struct.tables) {
                        oprot.writeString(_iter4);
                    }
                }
            }
            if (struct.isSetUsage()) {
                oprot.writeI64(struct.usage);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, DiskUsage struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(2);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TList _list5 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.tables = new ArrayList<String>(_list5.size);
                    for (int _i6 = 0; _i6 < _list5.size; ++_i6) {
                        String _elem7;
                        _elem7 = iprot.readString();
                        struct.tables.add(_elem7);
                    }
                }
                struct.setTablesIsSet(true);
            }
            if (incoming.get(1)) {
                struct.usage = iprot.readI64();
                struct.setUsageIsSet(true);
            }
        }
    }
}
