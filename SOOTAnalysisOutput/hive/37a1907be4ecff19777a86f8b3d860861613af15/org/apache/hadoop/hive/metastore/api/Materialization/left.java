package org.apache.hadoop.hive.metastore.api;

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
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked" })
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)")
@org.apache.hadoop.classification.InterfaceAudience.Public
@org.apache.hadoop.classification.InterfaceStability.Stable
public class Materialization implements org.apache.thrift.TBase<Materialization, Materialization._Fields>, java.io.Serializable, Cloneable, Comparable<Materialization> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Materialization");

    private static final org.apache.thrift.protocol.TField TABLES_USED_FIELD_DESC = new org.apache.thrift.protocol.TField("tablesUsed", org.apache.thrift.protocol.TType.SET, (short) 1);

    private static final org.apache.thrift.protocol.TField VALID_TXN_LIST_FIELD_DESC = new org.apache.thrift.protocol.TField("validTxnList", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.protocol.TField INVALIDATION_TIME_FIELD_DESC = new org.apache.thrift.protocol.TField("invalidationTime", org.apache.thrift.protocol.TType.I64, (short) 3);

    private static final org.apache.thrift.protocol.TField SOURCE_TABLES_UPDATE_DELETE_MODIFIED_FIELD_DESC = new org.apache.thrift.protocol.TField("sourceTablesUpdateDeleteModified", org.apache.thrift.protocol.TType.BOOL, (short) 4);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new MaterializationStandardSchemeFactory());
        schemes.put(TupleScheme.class, new MaterializationTupleSchemeFactory());
    }

    private Set<String> tablesUsed;

    private String validTxnList;

    private long invalidationTime;

    private boolean sourceTablesUpdateDeleteModified;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        TABLES_USED((short) 1, "tablesUsed"), VALID_TXN_LIST((short) 2, "validTxnList"), INVALIDATION_TIME((short) 3, "invalidationTime"), SOURCE_TABLES_UPDATE_DELETE_MODIFIED((short) 4, "sourceTablesUpdateDeleteModified");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return TABLES_USED;
                case 2:
                    return VALID_TXN_LIST;
                case 3:
                    return INVALIDATION_TIME;
                case 4:
                    return SOURCE_TABLES_UPDATE_DELETE_MODIFIED;
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

    private static final int __INVALIDATIONTIME_ISSET_ID = 0;

    private static final int __SOURCETABLESUPDATEDELETEMODIFIED_ISSET_ID = 1;

    private byte __isset_bitfield = 0;

    private static final _Fields[] optionals = { _Fields.VALID_TXN_LIST, _Fields.INVALIDATION_TIME, _Fields.SOURCE_TABLES_UPDATE_DELETE_MODIFIED };

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.TABLES_USED, new org.apache.thrift.meta_data.FieldMetaData("tablesUsed", org.apache.thrift.TFieldRequirementType.REQUIRED, new org.apache.thrift.meta_data.SetMetaData(org.apache.thrift.protocol.TType.SET, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
        tmpMap.put(_Fields.VALID_TXN_LIST, new org.apache.thrift.meta_data.FieldMetaData("validTxnList", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.INVALIDATION_TIME, new org.apache.thrift.meta_data.FieldMetaData("invalidationTime", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.SOURCE_TABLES_UPDATE_DELETE_MODIFIED, new org.apache.thrift.meta_data.FieldMetaData("sourceTablesUpdateDeleteModified", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Materialization.class, metaDataMap);
    }

    public Materialization() {
    }

    public Materialization(Set<String> tablesUsed) {
        this();
        this.tablesUsed = tablesUsed;
    }

    public Materialization(Materialization other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetTablesUsed()) {
            Set<String> __this__tablesUsed = new HashSet<String>(other.tablesUsed);
            this.tablesUsed = __this__tablesUsed;
        }
        if (other.isSetValidTxnList()) {
            this.validTxnList = other.validTxnList;
        }
        this.invalidationTime = other.invalidationTime;
        this.sourceTablesUpdateDeleteModified = other.sourceTablesUpdateDeleteModified;
    }

    public Materialization deepCopy() {
        return new Materialization(this);
    }

    @Override
    public void clear() {
        this.tablesUsed = null;
        this.validTxnList = null;
        setInvalidationTimeIsSet(false);
        this.invalidationTime = 0;
        setSourceTablesUpdateDeleteModifiedIsSet(false);
        this.sourceTablesUpdateDeleteModified = false;
    }

    public int getTablesUsedSize() {
        return (this.tablesUsed == null) ? 0 : this.tablesUsed.size();
    }

    public java.util.Iterator<String> getTablesUsedIterator() {
        return (this.tablesUsed == null) ? null : this.tablesUsed.iterator();
    }

    public void addToTablesUsed(String elem) {
        if (this.tablesUsed == null) {
            this.tablesUsed = new HashSet<String>();
        }
        this.tablesUsed.add(elem);
    }

    public Set<String> getTablesUsed() {
        return this.tablesUsed;
    }

    public void setTablesUsed(Set<String> tablesUsed) {
        this.tablesUsed = tablesUsed;
    }

    public void unsetTablesUsed() {
        this.tablesUsed = null;
    }

    public boolean isSetTablesUsed() {
        return this.tablesUsed != null;
    }

    public void setTablesUsedIsSet(boolean value) {
        if (!value) {
            this.tablesUsed = null;
        }
    }

    public String getValidTxnList() {
        return this.validTxnList;
    }

    public void setValidTxnList(String validTxnList) {
        this.validTxnList = validTxnList;
    }

    public void unsetValidTxnList() {
        this.validTxnList = null;
    }

    public boolean isSetValidTxnList() {
        return this.validTxnList != null;
    }

    public void setValidTxnListIsSet(boolean value) {
        if (!value) {
            this.validTxnList = null;
        }
    }

    public long getInvalidationTime() {
        return this.invalidationTime;
    }

    public void setInvalidationTime(long invalidationTime) {
        this.invalidationTime = invalidationTime;
        setInvalidationTimeIsSet(true);
    }

    public void unsetInvalidationTime() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __INVALIDATIONTIME_ISSET_ID);
    }

    public boolean isSetInvalidationTime() {
        return EncodingUtils.testBit(__isset_bitfield, __INVALIDATIONTIME_ISSET_ID);
    }

    public void setInvalidationTimeIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __INVALIDATIONTIME_ISSET_ID, value);
    }

    public boolean isSourceTablesUpdateDeleteModified() {
        return this.sourceTablesUpdateDeleteModified;
    }

    public void setSourceTablesUpdateDeleteModified(boolean sourceTablesUpdateDeleteModified) {
        this.sourceTablesUpdateDeleteModified = sourceTablesUpdateDeleteModified;
        setSourceTablesUpdateDeleteModifiedIsSet(true);
    }

    public void unsetSourceTablesUpdateDeleteModified() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __SOURCETABLESUPDATEDELETEMODIFIED_ISSET_ID);
    }

    public boolean isSetSourceTablesUpdateDeleteModified() {
        return EncodingUtils.testBit(__isset_bitfield, __SOURCETABLESUPDATEDELETEMODIFIED_ISSET_ID);
    }

    public void setSourceTablesUpdateDeleteModifiedIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __SOURCETABLESUPDATEDELETEMODIFIED_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case TABLES_USED:
                if (value == null) {
                    unsetTablesUsed();
                } else {
                    setTablesUsed((Set<String>) value);
                }
                break;
            case VALID_TXN_LIST:
                if (value == null) {
                    unsetValidTxnList();
                } else {
                    setValidTxnList((String) value);
                }
                break;
            case INVALIDATION_TIME:
                if (value == null) {
                    unsetInvalidationTime();
                } else {
                    setInvalidationTime((Long) value);
                }
                break;
            case SOURCE_TABLES_UPDATE_DELETE_MODIFIED:
                if (value == null) {
                    unsetSourceTablesUpdateDeleteModified();
                } else {
                    setSourceTablesUpdateDeleteModified((Boolean) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case TABLES_USED:
                return getTablesUsed();
            case VALID_TXN_LIST:
                return getValidTxnList();
            case INVALIDATION_TIME:
                return getInvalidationTime();
            case SOURCE_TABLES_UPDATE_DELETE_MODIFIED:
                return isSourceTablesUpdateDeleteModified();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case TABLES_USED:
                return isSetTablesUsed();
            case VALID_TXN_LIST:
                return isSetValidTxnList();
            case INVALIDATION_TIME:
                return isSetInvalidationTime();
            case SOURCE_TABLES_UPDATE_DELETE_MODIFIED:
                return isSetSourceTablesUpdateDeleteModified();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof Materialization)
            return this.equals((Materialization) that);
        return false;
    }

    public boolean equals(Materialization that) {
        if (that == null)
            return false;
        boolean this_present_tablesUsed = true && this.isSetTablesUsed();
        boolean that_present_tablesUsed = true && that.isSetTablesUsed();
        if (this_present_tablesUsed || that_present_tablesUsed) {
            if (!(this_present_tablesUsed && that_present_tablesUsed))
                return false;
            if (!this.tablesUsed.equals(that.tablesUsed))
                return false;
        }
        boolean this_present_validTxnList = true && this.isSetValidTxnList();
        boolean that_present_validTxnList = true && that.isSetValidTxnList();
        if (this_present_validTxnList || that_present_validTxnList) {
            if (!(this_present_validTxnList && that_present_validTxnList))
                return false;
            if (!this.validTxnList.equals(that.validTxnList))
                return false;
        }
        boolean this_present_invalidationTime = true && this.isSetInvalidationTime();
        boolean that_present_invalidationTime = true && that.isSetInvalidationTime();
        if (this_present_invalidationTime || that_present_invalidationTime) {
            if (!(this_present_invalidationTime && that_present_invalidationTime))
                return false;
            if (this.invalidationTime != that.invalidationTime)
                return false;
        }
        boolean this_present_sourceTablesUpdateDeleteModified = true && this.isSetSourceTablesUpdateDeleteModified();
        boolean that_present_sourceTablesUpdateDeleteModified = true && that.isSetSourceTablesUpdateDeleteModified();
        if (this_present_sourceTablesUpdateDeleteModified || that_present_sourceTablesUpdateDeleteModified) {
            if (!(this_present_sourceTablesUpdateDeleteModified && that_present_sourceTablesUpdateDeleteModified))
                return false;
            if (this.sourceTablesUpdateDeleteModified != that.sourceTablesUpdateDeleteModified)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        List<Object> list = new ArrayList<Object>();
        boolean present_tablesUsed = true && (isSetTablesUsed());
        list.add(present_tablesUsed);
        if (present_tablesUsed)
            list.add(tablesUsed);
        boolean present_validTxnList = true && (isSetValidTxnList());
        list.add(present_validTxnList);
        if (present_validTxnList)
            list.add(validTxnList);
        boolean present_invalidationTime = true && (isSetInvalidationTime());
        list.add(present_invalidationTime);
        if (present_invalidationTime)
            list.add(invalidationTime);
        boolean present_sourceTablesUpdateDeleteModified = true && (isSetSourceTablesUpdateDeleteModified());
        list.add(present_sourceTablesUpdateDeleteModified);
        if (present_sourceTablesUpdateDeleteModified)
            list.add(sourceTablesUpdateDeleteModified);
        return list.hashCode();
    }

    @Override
    public int compareTo(Materialization other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetTablesUsed()).compareTo(other.isSetTablesUsed());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTablesUsed()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tablesUsed, other.tablesUsed);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetValidTxnList()).compareTo(other.isSetValidTxnList());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetValidTxnList()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.validTxnList, other.validTxnList);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetInvalidationTime()).compareTo(other.isSetInvalidationTime());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetInvalidationTime()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.invalidationTime, other.invalidationTime);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetSourceTablesUpdateDeleteModified()).compareTo(other.isSetSourceTablesUpdateDeleteModified());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetSourceTablesUpdateDeleteModified()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sourceTablesUpdateDeleteModified, other.sourceTablesUpdateDeleteModified);
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
        StringBuilder sb = new StringBuilder("Materialization(");
        boolean first = true;
        sb.append("tablesUsed:");
        if (this.tablesUsed == null) {
            sb.append("null");
        } else {
            sb.append(this.tablesUsed);
        }
        first = false;
        if (isSetValidTxnList()) {
            if (!first)
                sb.append(", ");
            sb.append("validTxnList:");
            if (this.validTxnList == null) {
                sb.append("null");
            } else {
                sb.append(this.validTxnList);
            }
            first = false;
        }
        if (isSetInvalidationTime()) {
            if (!first)
                sb.append(", ");
            sb.append("invalidationTime:");
            sb.append(this.invalidationTime);
            first = false;
        }
        if (isSetSourceTablesUpdateDeleteModified()) {
            if (!first)
                sb.append(", ");
            sb.append("sourceTablesUpdateDeleteModified:");
            sb.append(this.sourceTablesUpdateDeleteModified);
            first = false;
        }
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
        if (!isSetTablesUsed()) {
            throw new org.apache.thrift.protocol.TProtocolException("Required field 'tablesUsed' is unset! Struct:" + toString());
        }
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

    private static class MaterializationStandardSchemeFactory implements SchemeFactory {

        public MaterializationStandardScheme getScheme() {
            return new MaterializationStandardScheme();
        }
    }

    private static class MaterializationStandardScheme extends StandardScheme<Materialization> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, Materialization struct) throws org.apache.thrift.TException {
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
                                org.apache.thrift.protocol.TSet _set872 = iprot.readSetBegin();
                                struct.tablesUsed = new HashSet<String>(2 * _set872.size);
                                String _elem873;
                                for (int _i874 = 0; _i874 < _set872.size; ++_i874) {
                                    _elem873 = iprot.readString();
                                    struct.tablesUsed.add(_elem873);
                                }
                                iprot.readSetEnd();
                            }
                            struct.setTablesUsedIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.validTxnList = iprot.readString();
                            struct.setValidTxnListIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.invalidationTime = iprot.readI64();
                            struct.setInvalidationTimeIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
                            struct.sourceTablesUpdateDeleteModified = iprot.readBool();
                            struct.setSourceTablesUpdateDeleteModifiedIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, Materialization struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.tablesUsed != null) {
                oprot.writeFieldBegin(TABLES_USED_FIELD_DESC);
                {
                    oprot.writeSetBegin(new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.STRING, struct.tablesUsed.size()));
                    for (String _iter875 : struct.tablesUsed) {
                        oprot.writeString(_iter875);
                    }
                    oprot.writeSetEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.validTxnList != null) {
                if (struct.isSetValidTxnList()) {
                    oprot.writeFieldBegin(VALID_TXN_LIST_FIELD_DESC);
                    oprot.writeString(struct.validTxnList);
                    oprot.writeFieldEnd();
                }
            }
            if (struct.isSetInvalidationTime()) {
                oprot.writeFieldBegin(INVALIDATION_TIME_FIELD_DESC);
                oprot.writeI64(struct.invalidationTime);
                oprot.writeFieldEnd();
            }
            if (struct.isSetSourceTablesUpdateDeleteModified()) {
                oprot.writeFieldBegin(SOURCE_TABLES_UPDATE_DELETE_MODIFIED_FIELD_DESC);
                oprot.writeBool(struct.sourceTablesUpdateDeleteModified);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class MaterializationTupleSchemeFactory implements SchemeFactory {

        public MaterializationTupleScheme getScheme() {
            return new MaterializationTupleScheme();
        }
    }

    private static class MaterializationTupleScheme extends TupleScheme<Materialization> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, Materialization struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            {
                oprot.writeI32(struct.tablesUsed.size());
                for (String _iter876 : struct.tablesUsed) {
                    oprot.writeString(_iter876);
                }
            }
            BitSet optionals = new BitSet();
            if (struct.isSetValidTxnList()) {
                optionals.set(0);
            }
            if (struct.isSetInvalidationTime()) {
                optionals.set(1);
            }
            if (struct.isSetSourceTablesUpdateDeleteModified()) {
                optionals.set(2);
            }
            oprot.writeBitSet(optionals, 3);
            if (struct.isSetValidTxnList()) {
                oprot.writeString(struct.validTxnList);
            }
            if (struct.isSetInvalidationTime()) {
                oprot.writeI64(struct.invalidationTime);
            }
            if (struct.isSetSourceTablesUpdateDeleteModified()) {
                oprot.writeBool(struct.sourceTablesUpdateDeleteModified);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, Materialization struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            {
                org.apache.thrift.protocol.TSet _set877 = new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                struct.tablesUsed = new HashSet<String>(2 * _set877.size);
                String _elem878;
                for (int _i879 = 0; _i879 < _set877.size; ++_i879) {
                    _elem878 = iprot.readString();
                    struct.tablesUsed.add(_elem878);
                }
            }
            struct.setTablesUsedIsSet(true);
            BitSet incoming = iprot.readBitSet(3);
            if (incoming.get(0)) {
                struct.validTxnList = iprot.readString();
                struct.setValidTxnListIsSet(true);
            }
            if (incoming.get(1)) {
                struct.invalidationTime = iprot.readI64();
                struct.setInvalidationTimeIsSet(true);
            }
            if (incoming.get(2)) {
                struct.sourceTablesUpdateDeleteModified = iprot.readBool();
                struct.setSourceTablesUpdateDeleteModifiedIsSet(true);
            }
        }
    }
}
