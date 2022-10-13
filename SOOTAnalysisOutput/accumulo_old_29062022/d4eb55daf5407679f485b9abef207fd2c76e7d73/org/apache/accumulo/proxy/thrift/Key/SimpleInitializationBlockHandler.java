package org.apache.accumulo.proxy.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class Key implements org.apache.thrift.TBase<Key, Key._Fields>, java.io.Serializable, Cloneable, Comparable<Key> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Key");

    private static final org.apache.thrift.protocol.TField ROW_FIELD_DESC = new org.apache.thrift.protocol.TField("row", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final org.apache.thrift.protocol.TField COL_FAMILY_FIELD_DESC = new org.apache.thrift.protocol.TField("colFamily", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.protocol.TField COL_QUALIFIER_FIELD_DESC = new org.apache.thrift.protocol.TField("colQualifier", org.apache.thrift.protocol.TType.STRING, (short) 3);

    private static final org.apache.thrift.protocol.TField COL_VISIBILITY_FIELD_DESC = new org.apache.thrift.protocol.TField("colVisibility", org.apache.thrift.protocol.TType.STRING, (short) 4);

    private static final org.apache.thrift.protocol.TField TIMESTAMP_FIELD_DESC = new org.apache.thrift.protocol.TField("timestamp", org.apache.thrift.protocol.TType.I64, (short) 5);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new KeyStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new KeyTupleSchemeFactory();

    static {
        schemes.put(StandardScheme.class, new KeyStandardSchemeFactory());
        schemes.put(TupleScheme.class, new KeyTupleSchemeFactory());
    }

    @org.apache.thrift.annotation.Nullable
    public java.nio.ByteBuffer row;

    @org.apache.thrift.annotation.Nullable
    public java.nio.ByteBuffer colFamily;

    @org.apache.thrift.annotation.Nullable
    public java.nio.ByteBuffer colQualifier;

    @org.apache.thrift.annotation.Nullable
    public java.nio.ByteBuffer colVisibility;

    public long timestamp;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        ROW((short) 1, "row"), COL_FAMILY((short) 2, "colFamily"), COL_QUALIFIER((short) 3, "colQualifier"), COL_VISIBILITY((short) 4, "colVisibility"), TIMESTAMP((short) 5, "timestamp");

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

    private static final int __TIMESTAMP_ISSET_ID = 0;

    private byte __isset_bitfield = 0;

    private static final _Fields[] optionals = { _Fields.TIMESTAMP };

    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.ROW, new org.apache.thrift.meta_data.FieldMetaData("row", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.COL_FAMILY, new org.apache.thrift.meta_data.FieldMetaData("colFamily", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.COL_QUALIFIER, new org.apache.thrift.meta_data.FieldMetaData("colQualifier", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.COL_VISIBILITY, new org.apache.thrift.meta_data.FieldMetaData("colVisibility", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.TIMESTAMP, new org.apache.thrift.meta_data.FieldMetaData("timestamp", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Key.class, metaDataMap);
    }

    public Key() {
        this.timestamp = 9223372036854775807L;
    }

    public Key(java.nio.ByteBuffer row, java.nio.ByteBuffer colFamily, java.nio.ByteBuffer colQualifier, java.nio.ByteBuffer colVisibility) {
        this();
        this.row = org.apache.thrift.TBaseHelper.copyBinary(row);
        this.colFamily = org.apache.thrift.TBaseHelper.copyBinary(colFamily);
        this.colQualifier = org.apache.thrift.TBaseHelper.copyBinary(colQualifier);
        this.colVisibility = org.apache.thrift.TBaseHelper.copyBinary(colVisibility);
    }

    public Key(Key other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetRow()) {
            this.row = org.apache.thrift.TBaseHelper.copyBinary(other.row);
        }
        if (other.isSetColFamily()) {
            this.colFamily = org.apache.thrift.TBaseHelper.copyBinary(other.colFamily);
        }
        if (other.isSetColQualifier()) {
            this.colQualifier = org.apache.thrift.TBaseHelper.copyBinary(other.colQualifier);
        }
        if (other.isSetColVisibility()) {
            this.colVisibility = org.apache.thrift.TBaseHelper.copyBinary(other.colVisibility);
        }
        this.timestamp = other.timestamp;
    }

    public Key deepCopy() {
        return new Key(this);
    }

    @Override
    public void clear() {
        this.row = null;
        this.colFamily = null;
        this.colQualifier = null;
        this.colVisibility = null;
        this.timestamp = 9223372036854775807L;
    }

    public byte[] getRow() {
        setRow(org.apache.thrift.TBaseHelper.rightSize(row));
        return row == null ? null : row.array();
    }

    public java.nio.ByteBuffer bufferForRow() {
        return org.apache.thrift.TBaseHelper.copyBinary(row);
    }

    public Key setRow(byte[] row) {
        this.row = row == null ? (java.nio.ByteBuffer) null : java.nio.ByteBuffer.wrap(row.clone());
        return this;
    }

    public Key setRow(@org.apache.thrift.annotation.Nullable java.nio.ByteBuffer row) {
        this.row = org.apache.thrift.TBaseHelper.copyBinary(row);
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

    public java.nio.ByteBuffer bufferForColFamily() {
        return org.apache.thrift.TBaseHelper.copyBinary(colFamily);
    }

    public Key setColFamily(byte[] colFamily) {
        this.colFamily = colFamily == null ? (java.nio.ByteBuffer) null : java.nio.ByteBuffer.wrap(colFamily.clone());
        return this;
    }

    public Key setColFamily(@org.apache.thrift.annotation.Nullable java.nio.ByteBuffer colFamily) {
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

    public Key setColQualifier(byte[] colQualifier) {
        this.colQualifier = colQualifier == null ? (java.nio.ByteBuffer) null : java.nio.ByteBuffer.wrap(colQualifier.clone());
        return this;
    }

    public Key setColQualifier(@org.apache.thrift.annotation.Nullable java.nio.ByteBuffer colQualifier) {
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

    public byte[] getColVisibility() {
        setColVisibility(org.apache.thrift.TBaseHelper.rightSize(colVisibility));
        return colVisibility == null ? null : colVisibility.array();
    }

    public java.nio.ByteBuffer bufferForColVisibility() {
        return org.apache.thrift.TBaseHelper.copyBinary(colVisibility);
    }

    public Key setColVisibility(byte[] colVisibility) {
        this.colVisibility = colVisibility == null ? (java.nio.ByteBuffer) null : java.nio.ByteBuffer.wrap(colVisibility.clone());
        return this;
    }

    public Key setColVisibility(@org.apache.thrift.annotation.Nullable java.nio.ByteBuffer colVisibility) {
        this.colVisibility = org.apache.thrift.TBaseHelper.copyBinary(colVisibility);
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

    public Key setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        setTimestampIsSet(true);
        return this;
    }

    public void unsetTimestamp() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __TIMESTAMP_ISSET_ID);
    }

    public boolean isSetTimestamp() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __TIMESTAMP_ISSET_ID);
    }

    public void setTimestampIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __TIMESTAMP_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case ROW:
                if (value == null) {
                    unsetRow();
                } else {
                    if (value instanceof byte[]) {
                        setRow((byte[]) value);
                    } else {
                        setRow((java.nio.ByteBuffer) value);
                    }
                }
                break;
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
            case COL_VISIBILITY:
                if (value == null) {
                    unsetColVisibility();
                } else {
                    if (value instanceof byte[]) {
                        setColVisibility((byte[]) value);
                    } else {
                        setColVisibility((java.nio.ByteBuffer) value);
                    }
                }
                break;
            case TIMESTAMP:
                if (value == null) {
                    unsetTimestamp();
                } else {
                    setTimestamp((java.lang.Long) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
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
                return getTimestamp();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
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
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof Key)
            return this.equals((Key) that);
        return false;
    }

    public boolean equals(Key that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
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
        boolean this_present_timestamp = true && this.isSetTimestamp();
        boolean that_present_timestamp = true && that.isSetTimestamp();
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
        int hashCode = 1;
        hashCode = hashCode * 8191 + ((isSetRow()) ? 131071 : 524287);
        if (isSetRow())
            hashCode = hashCode * 8191 + row.hashCode();
        hashCode = hashCode * 8191 + ((isSetColFamily()) ? 131071 : 524287);
        if (isSetColFamily())
            hashCode = hashCode * 8191 + colFamily.hashCode();
        hashCode = hashCode * 8191 + ((isSetColQualifier()) ? 131071 : 524287);
        if (isSetColQualifier())
            hashCode = hashCode * 8191 + colQualifier.hashCode();
        hashCode = hashCode * 8191 + ((isSetColVisibility()) ? 131071 : 524287);
        if (isSetColVisibility())
            hashCode = hashCode * 8191 + colVisibility.hashCode();
        hashCode = hashCode * 8191 + ((isSetTimestamp()) ? 131071 : 524287);
        if (isSetTimestamp())
            hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(timestamp);
        return hashCode;
    }

    @Override
    public int compareTo(Key other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetRow()).compareTo(other.isSetRow());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetRow()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.row, other.row);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
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
        lastComparison = java.lang.Boolean.valueOf(isSetColVisibility()).compareTo(other.isSetColVisibility());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetColVisibility()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.colVisibility, other.colVisibility);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetTimestamp()).compareTo(other.isSetTimestamp());
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("Key(");
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
        if (isSetTimestamp()) {
            if (!first)
                sb.append(", ");
            sb.append("timestamp:");
            sb.append(this.timestamp);
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

    private static class KeyStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public KeyStandardScheme getScheme() {
            return new KeyStandardScheme();
        }
    }

    private static class KeyStandardScheme extends org.apache.thrift.scheme.StandardScheme<Key> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, Key struct) throws org.apache.thrift.TException {
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, Key struct) throws org.apache.thrift.TException {
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
            if (struct.isSetTimestamp()) {
                oprot.writeFieldBegin(TIMESTAMP_FIELD_DESC);
                oprot.writeI64(struct.timestamp);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class KeyTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public KeyTupleScheme getScheme() {
            return new KeyTupleScheme();
        }
    }

    private static class KeyTupleScheme extends org.apache.thrift.scheme.TupleScheme<Key> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, Key struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
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
        public void read(org.apache.thrift.protocol.TProtocol prot, Key struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(5);
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

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}