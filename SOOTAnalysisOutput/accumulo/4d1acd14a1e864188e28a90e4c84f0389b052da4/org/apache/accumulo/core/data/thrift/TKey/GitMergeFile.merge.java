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
public class TKey implements org.apache.thrift.TBase<TKey, TKey._Fields>, java.io.Serializable, Cloneable, Comparable<TKey> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TKey");

    private static final org.apache.thrift.protocol.TField ROW_FIELD_DESC = new org.apache.thrift.protocol.TField("row", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final org.apache.thrift.protocol.TField COL_FAMILY_FIELD_DESC = new org.apache.thrift.protocol.TField("colFamily", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.protocol.TField COL_QUALIFIER_FIELD_DESC = new org.apache.thrift.protocol.TField("colQualifier", org.apache.thrift.protocol.TType.STRING, (short) 3);

    private static final org.apache.thrift.protocol.TField COL_VISIBILITY_FIELD_DESC = new org.apache.thrift.protocol.TField("colVisibility", org.apache.thrift.protocol.TType.STRING, (short) 4);

    private static final org.apache.thrift.protocol.TField TIMESTAMP_FIELD_DESC = new org.apache.thrift.protocol.TField("timestamp", org.apache.thrift.protocol.TType.I64, (short) 5);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new TKeyStandardSchemeFactory());
        schemes.put(TupleScheme.class, new TKeyTupleSchemeFactory());
    }

    public ByteBuffer row;

    public ByteBuffer colFamily;

    public ByteBuffer colQualifier;

    public ByteBuffer colVisibility;

    public long timestamp;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        ROW((short) 1, "row"), COL_FAMILY((short) 2, "colFamily"), COL_QUALIFIER((short) 3, "colQualifier"), COL_VISIBILITY((short) 4, "colVisibility"), TIMESTAMP((short) 5, "timestamp");

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
                    return COL_FAMILY;
                case 3:
                    return COL_QUALIFIER;
                case 4:
                    return COL_VISIBILITY;
                case 5:
                    return TIMESTAMP;
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

    private static final int __TIMESTAMP_ISSET_ID = 0;

    private byte __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.ROW, new org.apache.thrift.meta_data.FieldMetaData("row", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.COL_FAMILY, new org.apache.thrift.meta_data.FieldMetaData("colFamily", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.COL_QUALIFIER, new org.apache.thrift.meta_data.FieldMetaData("colQualifier", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.COL_VISIBILITY, new org.apache.thrift.meta_data.FieldMetaData("colVisibility", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.TIMESTAMP, new org.apache.thrift.meta_data.FieldMetaData("timestamp", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TKey.class, metaDataMap);
    }

    public TKey() {
    }

    public TKey(ByteBuffer row, ByteBuffer colFamily, ByteBuffer colQualifier, ByteBuffer colVisibility, long timestamp) {
        this();
        this.row = row;
        this.colFamily = colFamily;
        this.colQualifier = colQualifier;
        this.colVisibility = colVisibility;
        this.timestamp = timestamp;
        setTimestampIsSet(true);
    }

    public TKey(TKey other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetRow()) {
            this.row = org.apache.thrift.TBaseHelper.copyBinary(other.row);
            ;
        }
        if (other.isSetColFamily()) {
            this.colFamily = org.apache.thrift.TBaseHelper.copyBinary(other.colFamily);
            ;
        }
        if (other.isSetColQualifier()) {
            this.colQualifier = org.apache.thrift.TBaseHelper.copyBinary(other.colQualifier);
            ;
        }
        if (other.isSetColVisibility()) {
            this.colVisibility = org.apache.thrift.TBaseHelper.copyBinary(other.colVisibility);
            ;
        }
        this.timestamp = other.timestamp;
    }

    public TKey deepCopy() {
        return new TKey(this);
    }

    @Override
    public void clear() {
        this.row = null;
        this.colFamily = null;
        this.colQualifier = null;
        this.colVisibility = null;
        setTimestampIsSet(false);
        this.timestamp = 0;
    }

    public byte[] getRow() {
        setRow(org.apache.thrift.TBaseHelper.rightSize(row));
        return row == null ? null : row.array();
    }

    public ByteBuffer bufferForRow() {
        return row;
    }

    public TKey setRow(byte[] row) {
        setRow(row == null ? (ByteBuffer) null : ByteBuffer.wrap(row));
        return this;
    }

    public TKey setRow(ByteBuffer row) {
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

    public byte[] getColFamily() {
        setColFamily(org.apache.thrift.TBaseHelper.rightSize(colFamily));
        return colFamily == null ? null : colFamily.array();
    }

    public ByteBuffer bufferForColFamily() {
        return colFamily;
    }

    public TKey setColFamily(byte[] colFamily) {
        setColFamily(colFamily == null ? (ByteBuffer) null : ByteBuffer.wrap(colFamily));
        return this;
    }

    public TKey setColFamily(ByteBuffer colFamily) {
        this.colFamily = colFamily;
        return this;
    }

    public void unsetColFamily() {
        this.colFamily = null;
    }

    public boolean isSetColFamily() {
        return this.colFamily != null;
    }

    public void setColFamilyIsSet(boolean value) {
        if (!value) {
            this.colFamily = null;
        }
    }

    public byte[] getColQualifier() {
        setColQualifier(org.apache.thrift.TBaseHelper.rightSize(colQualifier));
        return colQualifier == null ? null : colQualifier.array();
    }

    public ByteBuffer bufferForColQualifier() {
        return colQualifier;
    }

    public TKey setColQualifier(byte[] colQualifier) {
        setColQualifier(colQualifier == null ? (ByteBuffer) null : ByteBuffer.wrap(colQualifier));
        return this;
    }

    public TKey setColQualifier(ByteBuffer colQualifier) {
        this.colQualifier = colQualifier;
        return this;
    }

    public void unsetColQualifier() {
        this.colQualifier = null;
    }

    public boolean isSetColQualifier() {
        return this.colQualifier != null;
    }

    public void setColQualifierIsSet(boolean value) {
        if (!value) {
            this.colQualifier = null;
        }
    }

    public byte[] getColVisibility() {
        setColVisibility(org.apache.thrift.TBaseHelper.rightSize(colVisibility));
        return colVisibility == null ? null : colVisibility.array();
    }

    public ByteBuffer bufferForColVisibility() {
        return colVisibility;
    }

    public TKey setColVisibility(byte[] colVisibility) {
        setColVisibility(colVisibility == null ? (ByteBuffer) null : ByteBuffer.wrap(colVisibility));
        return this;
    }

    public TKey setColVisibility(ByteBuffer colVisibility) {
        this.colVisibility = colVisibility;
        return this;
    }

    public void unsetColVisibility() {
        this.colVisibility = null;
    }

    public boolean isSetColVisibility() {
        return this.colVisibility != null;
    }

    public void setColVisibilityIsSet(boolean value) {
        if (!value) {
            this.colVisibility = null;
        }
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public TKey setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        setTimestampIsSet(true);
        return this;
    }

    public void unsetTimestamp() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __TIMESTAMP_ISSET_ID);
    }

    public boolean isSetTimestamp() {
        return EncodingUtils.testBit(__isset_bitfield, __TIMESTAMP_ISSET_ID);
    }

    public void setTimestampIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __TIMESTAMP_ISSET_ID, value);
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
            case COL_FAMILY:
                if (value == null) {
                    unsetColFamily();
                } else {
                    setColFamily((ByteBuffer) value);
                }
                break;
            case COL_QUALIFIER:
                if (value == null) {
                    unsetColQualifier();
                } else {
                    setColQualifier((ByteBuffer) value);
                }
                break;
            case COL_VISIBILITY:
                if (value == null) {
                    unsetColVisibility();
                } else {
                    setColVisibility((ByteBuffer) value);
                }
                break;
            case TIMESTAMP:
                if (value == null) {
                    unsetTimestamp();
                } else {
                    setTimestamp((Long) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case ROW:
                return getRow();
            case COL_FAMILY:
                return getColFamily();
            case COL_QUALIFIER:
                return getColQualifier();
            case COL_VISIBILITY:
                return getColVisibility();
            case TIMESTAMP:
                return Long.valueOf(getTimestamp());
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
            case COL_FAMILY:
                return isSetColFamily();
            case COL_QUALIFIER:
                return isSetColQualifier();
            case COL_VISIBILITY:
                return isSetColVisibility();
            case TIMESTAMP:
                return isSetTimestamp();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof TKey)
            return this.equals((TKey) that);
        return false;
    }

    public boolean equals(TKey that) {
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
        boolean this_present_colFamily = true && this.isSetColFamily();
        boolean that_present_colFamily = true && that.isSetColFamily();
        if (this_present_colFamily || that_present_colFamily) {
            if (!(this_present_colFamily && that_present_colFamily))
                return false;
            if (!this.colFamily.equals(that.colFamily))
                return false;
        }
        boolean this_present_colQualifier = true && this.isSetColQualifier();
        boolean that_present_colQualifier = true && that.isSetColQualifier();
        if (this_present_colQualifier || that_present_colQualifier) {
            if (!(this_present_colQualifier && that_present_colQualifier))
                return false;
            if (!this.colQualifier.equals(that.colQualifier))
                return false;
        }
        boolean this_present_colVisibility = true && this.isSetColVisibility();
        boolean that_present_colVisibility = true && that.isSetColVisibility();
        if (this_present_colVisibility || that_present_colVisibility) {
            if (!(this_present_colVisibility && that_present_colVisibility))
                return false;
            if (!this.colVisibility.equals(that.colVisibility))
                return false;
        }
        boolean this_present_timestamp = true;
        boolean that_present_timestamp = true;
        if (this_present_timestamp || that_present_timestamp) {
            if (!(this_present_timestamp && that_present_timestamp))
                return false;
            if (this.timestamp != that.timestamp)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int compareTo(TKey other) {
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
        lastComparison = Boolean.valueOf(isSetColFamily()).compareTo(other.isSetColFamily());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetColFamily()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.colFamily, other.colFamily);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetColQualifier()).compareTo(other.isSetColQualifier());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetColQualifier()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.colQualifier, other.colQualifier);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetColVisibility()).compareTo(other.isSetColVisibility());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetColVisibility()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.colVisibility, other.colVisibility);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetTimestamp()).compareTo(other.isSetTimestamp());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTimestamp()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.timestamp, other.timestamp);
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
        StringBuilder sb = new StringBuilder("TKey(");
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
        sb.append("colFamily:");
        if (this.colFamily == null) {
            sb.append("null");
        } else {
            org.apache.thrift.TBaseHelper.toString(this.colFamily, sb);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("colQualifier:");
        if (this.colQualifier == null) {
            sb.append("null");
        } else {
            org.apache.thrift.TBaseHelper.toString(this.colQualifier, sb);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("colVisibility:");
        if (this.colVisibility == null) {
            sb.append("null");
        } else {
            org.apache.thrift.TBaseHelper.toString(this.colVisibility, sb);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("timestamp:");
        sb.append(this.timestamp);
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

    private static class TKeyStandardSchemeFactory implements SchemeFactory {

        public TKeyStandardScheme getScheme() {
            return new TKeyStandardScheme();
        }
    }

    private static class TKeyStandardScheme extends StandardScheme<TKey> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, TKey struct) throws org.apache.thrift.TException {
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
                            struct.colFamily = iprot.readBinary();
                            struct.setColFamilyIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.colQualifier = iprot.readBinary();
                            struct.setColQualifierIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.colVisibility = iprot.readBinary();
                            struct.setColVisibilityIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 5:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.timestamp = iprot.readI64();
                            struct.setTimestampIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, TKey struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.row != null) {
                oprot.writeFieldBegin(ROW_FIELD_DESC);
                oprot.writeBinary(struct.row);
                oprot.writeFieldEnd();
            }
            if (struct.colFamily != null) {
                oprot.writeFieldBegin(COL_FAMILY_FIELD_DESC);
                oprot.writeBinary(struct.colFamily);
                oprot.writeFieldEnd();
            }
            if (struct.colQualifier != null) {
                oprot.writeFieldBegin(COL_QUALIFIER_FIELD_DESC);
                oprot.writeBinary(struct.colQualifier);
                oprot.writeFieldEnd();
            }
            if (struct.colVisibility != null) {
                oprot.writeFieldBegin(COL_VISIBILITY_FIELD_DESC);
                oprot.writeBinary(struct.colVisibility);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(TIMESTAMP_FIELD_DESC);
            oprot.writeI64(struct.timestamp);
            oprot.writeFieldEnd();
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class TKeyTupleSchemeFactory implements SchemeFactory {

        public TKeyTupleScheme getScheme() {
            return new TKeyTupleScheme();
        }
    }

    private static class TKeyTupleScheme extends TupleScheme<TKey> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, TKey struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetRow()) {
                optionals.set(0);
            }
            if (struct.isSetColFamily()) {
                optionals.set(1);
            }
            if (struct.isSetColQualifier()) {
                optionals.set(2);
            }
            if (struct.isSetColVisibility()) {
                optionals.set(3);
            }
            if (struct.isSetTimestamp()) {
                optionals.set(4);
            }
            oprot.writeBitSet(optionals, 5);
            if (struct.isSetRow()) {
                oprot.writeBinary(struct.row);
            }
            if (struct.isSetColFamily()) {
                oprot.writeBinary(struct.colFamily);
            }
            if (struct.isSetColQualifier()) {
                oprot.writeBinary(struct.colQualifier);
            }
            if (struct.isSetColVisibility()) {
                oprot.writeBinary(struct.colVisibility);
            }
            if (struct.isSetTimestamp()) {
                oprot.writeI64(struct.timestamp);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, TKey struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(5);
            if (incoming.get(0)) {
                struct.row = iprot.readBinary();
                struct.setRowIsSet(true);
            }
            if (incoming.get(1)) {
                struct.colFamily = iprot.readBinary();
                struct.setColFamilyIsSet(true);
            }
            if (incoming.get(2)) {
                struct.colQualifier = iprot.readBinary();
                struct.setColQualifierIsSet(true);
            }
            if (incoming.get(3)) {
                struct.colVisibility = iprot.readBinary();
                struct.setColVisibilityIsSet(true);
            }
            if (incoming.get(4)) {
                struct.timestamp = iprot.readI64();
                struct.setTimestampIsSet(true);
            }
        }
    }
}
