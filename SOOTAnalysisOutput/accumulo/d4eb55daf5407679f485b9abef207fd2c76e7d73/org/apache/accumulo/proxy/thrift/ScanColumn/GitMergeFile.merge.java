package org.apache.accumulo.proxy.thrift;

<<<<<<< MINE
=======
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

>>>>>>> YOURS
@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class ScanColumn implements org.apache.thrift.TBase<ScanColumn, ScanColumn._Fields>, java.io.Serializable, Cloneable, Comparable<ScanColumn> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ScanColumn");

    private static final org.apache.thrift.protocol.TField COL_FAMILY_FIELD_DESC = new org.apache.thrift.protocol.TField("colFamily", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final org.apache.thrift.protocol.TField COL_QUALIFIER_FIELD_DESC = new org.apache.thrift.protocol.TField("colQualifier", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new ScanColumnStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new ScanColumnTupleSchemeFactory();

    @org.apache.thrift.annotation.Nullable
    public java.nio.ByteBuffer colFamily;

    @org.apache.thrift.annotation.Nullable
    public java.nio.ByteBuffer colQualifier;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        COL_FAMILY((short) 1, "colFamily"), COL_QUALIFIER((short) 2, "colQualifier");

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
                    return COL_FAMILY;
                case 2:
                    return COL_QUALIFIER;
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

    private static final _Fields[] optionals = { _Fields.COL_QUALIFIER };

    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.COL_FAMILY, new org.apache.thrift.meta_data.FieldMetaData("colFamily", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.COL_QUALIFIER, new org.apache.thrift.meta_data.FieldMetaData("colQualifier", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ScanColumn.class, metaDataMap);
    }

    public ScanColumn() {
    }

    public ScanColumn(java.nio.ByteBuffer colFamily) {
        this();
        this.colFamily = org.apache.thrift.TBaseHelper.copyBinary(colFamily);
    }

    public ScanColumn(ScanColumn other) {
        if (other.isSetColFamily()) {
            this.colFamily = org.apache.thrift.TBaseHelper.copyBinary(other.colFamily);
        }
        if (other.isSetColQualifier()) {
            this.colQualifier = org.apache.thrift.TBaseHelper.copyBinary(other.colQualifier);
        }
    }

    public ScanColumn deepCopy() {
        return new ScanColumn(this);
    }

    @Override
    public void clear() {
        this.colFamily = null;
        this.colQualifier = null;
    }

    public byte[] getColFamily() {
        setColFamily(org.apache.thrift.TBaseHelper.rightSize(colFamily));
        return colFamily == null ? null : colFamily.array();
    }

    public java.nio.ByteBuffer bufferForColFamily() {
        return org.apache.thrift.TBaseHelper.copyBinary(colFamily);
    }

    public ScanColumn setColFamily(byte[] colFamily) {
        this.colFamily = colFamily == null ? (java.nio.ByteBuffer) null : java.nio.ByteBuffer.wrap(colFamily.clone());
        return this;
    }

    public ScanColumn setColFamily(@org.apache.thrift.annotation.Nullable java.nio.ByteBuffer colFamily) {
        this.colFamily = org.apache.thrift.TBaseHelper.copyBinary(colFamily);
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

    public java.nio.ByteBuffer bufferForColQualifier() {
        return org.apache.thrift.TBaseHelper.copyBinary(colQualifier);
    }

    public ScanColumn setColQualifier(byte[] colQualifier) {
        this.colQualifier = colQualifier == null ? (java.nio.ByteBuffer) null : java.nio.ByteBuffer.wrap(colQualifier.clone());
        return this;
    }

    public ScanColumn setColQualifier(@org.apache.thrift.annotation.Nullable java.nio.ByteBuffer colQualifier) {
        this.colQualifier = org.apache.thrift.TBaseHelper.copyBinary(colQualifier);
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

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case COL_FAMILY:
                if (value == null) {
                    unsetColFamily();
                } else {
                    if (value instanceof byte[]) {
                        setColFamily((byte[]) value);
                    } else {
                        setColFamily((java.nio.ByteBuffer) value);
                    }
                }
                break;
            case COL_QUALIFIER:
                if (value == null) {
                    unsetColQualifier();
                } else {
                    if (value instanceof byte[]) {
                        setColQualifier((byte[]) value);
                    } else {
                        setColQualifier((java.nio.ByteBuffer) value);
                    }
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case COL_FAMILY:
                return getColFamily();
            case COL_QUALIFIER:
                return getColQualifier();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }
        switch(field) {
            case COL_FAMILY:
                return isSetColFamily();
            case COL_QUALIFIER:
                return isSetColQualifier();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof ScanColumn)
            return this.equals((ScanColumn) that);
        return false;
    }

    public boolean equals(ScanColumn that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
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
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 8191 + ((isSetColFamily()) ? 131071 : 524287);
        if (isSetColFamily())
            hashCode = hashCode * 8191 + colFamily.hashCode();
        hashCode = hashCode * 8191 + ((isSetColQualifier()) ? 131071 : 524287);
        if (isSetColQualifier())
            hashCode = hashCode * 8191 + colQualifier.hashCode();
        return hashCode;
    }

    @Override
    public int compareTo(ScanColumn other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetColFamily()).compareTo(other.isSetColFamily());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetColFamily()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.colFamily, other.colFamily);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetColQualifier()).compareTo(other.isSetColQualifier());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetColQualifier()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.colQualifier, other.colQualifier);
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("ScanColumn(");
        boolean first = true;
        sb.append("colFamily:");
        if (this.colFamily == null) {
            sb.append("null");
        } else {
            org.apache.thrift.TBaseHelper.toString(this.colFamily, sb);
        }
        first = false;
        if (isSetColQualifier()) {
            if (!first)
                sb.append(", ");
            sb.append("colQualifier:");
            if (this.colQualifier == null) {
                sb.append("null");
            } else {
                org.apache.thrift.TBaseHelper.toString(this.colQualifier, sb);
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

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
        try {
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class ScanColumnStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public ScanColumnStandardScheme getScheme() {
            return new ScanColumnStandardScheme();
        }
    }

    private static class ScanColumnStandardScheme extends org.apache.thrift.scheme.StandardScheme<ScanColumn> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, ScanColumn struct) throws org.apache.thrift.TException {
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
                            struct.colFamily = iprot.readBinary();
                            struct.setColFamilyIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.colQualifier = iprot.readBinary();
                            struct.setColQualifierIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, ScanColumn struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.colFamily != null) {
                oprot.writeFieldBegin(COL_FAMILY_FIELD_DESC);
                oprot.writeBinary(struct.colFamily);
                oprot.writeFieldEnd();
            }
            if (struct.colQualifier != null) {
                if (struct.isSetColQualifier()) {
                    oprot.writeFieldBegin(COL_QUALIFIER_FIELD_DESC);
                    oprot.writeBinary(struct.colQualifier);
                    oprot.writeFieldEnd();
                }
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class ScanColumnTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public ScanColumnTupleScheme getScheme() {
            return new ScanColumnTupleScheme();
        }
    }

    private static class ScanColumnTupleScheme extends org.apache.thrift.scheme.TupleScheme<ScanColumn> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, ScanColumn struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
            if (struct.isSetColFamily()) {
                optionals.set(0);
            }
            if (struct.isSetColQualifier()) {
                optionals.set(1);
            }
            oprot.writeBitSet(optionals, 2);
            if (struct.isSetColFamily()) {
                oprot.writeBinary(struct.colFamily);
            }
            if (struct.isSetColQualifier()) {
                oprot.writeBinary(struct.colQualifier);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, ScanColumn struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(2);
            if (incoming.get(0)) {
                struct.colFamily = iprot.readBinary();
                struct.setColFamilyIsSet(true);
            }
            if (incoming.get(1)) {
                struct.colQualifier = iprot.readBinary();
                struct.setColQualifierIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}
