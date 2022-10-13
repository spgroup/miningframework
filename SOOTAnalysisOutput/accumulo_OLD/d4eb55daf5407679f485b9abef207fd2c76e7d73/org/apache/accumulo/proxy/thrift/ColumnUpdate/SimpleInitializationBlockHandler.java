package org.apache.accumulo.proxy.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class ColumnUpdate implements org.apache.thrift.TBase<ColumnUpdate, ColumnUpdate._Fields>, java.io.Serializable, Cloneable, Comparable<ColumnUpdate> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ColumnUpdate");

    private static final org.apache.thrift.protocol.TField COL_FAMILY_FIELD_DESC = new org.apache.thrift.protocol.TField("colFamily", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final org.apache.thrift.protocol.TField COL_QUALIFIER_FIELD_DESC = new org.apache.thrift.protocol.TField("colQualifier", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.protocol.TField COL_VISIBILITY_FIELD_DESC = new org.apache.thrift.protocol.TField("colVisibility", org.apache.thrift.protocol.TType.STRING, (short) 3);

    private static final org.apache.thrift.protocol.TField TIMESTAMP_FIELD_DESC = new org.apache.thrift.protocol.TField("timestamp", org.apache.thrift.protocol.TType.I64, (short) 4);

    private static final org.apache.thrift.protocol.TField VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("value", org.apache.thrift.protocol.TType.STRING, (short) 5);

    private static final org.apache.thrift.protocol.TField DELETE_CELL_FIELD_DESC = new org.apache.thrift.protocol.TField("deleteCell", org.apache.thrift.protocol.TType.BOOL, (short) 6);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new ColumnUpdateStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new ColumnUpdateTupleSchemeFactory();

    static {
        schemes.put(StandardScheme.class, new ColumnUpdateStandardSchemeFactory());
        schemes.put(TupleScheme.class, new ColumnUpdateTupleSchemeFactory());
    }

    @org.apache.thrift.annotation.Nullable
    public java.nio.ByteBuffer colFamily;

    @org.apache.thrift.annotation.Nullable
    public java.nio.ByteBuffer colQualifier;

    @org.apache.thrift.annotation.Nullable
    public java.nio.ByteBuffer colVisibility;

    public long timestamp;

    @org.apache.thrift.annotation.Nullable
    public java.nio.ByteBuffer value;

    public boolean deleteCell;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        COL_FAMILY((short) 1, "colFamily"),
        COL_QUALIFIER((short) 2, "colQualifier"),
        COL_VISIBILITY((short) 3, "colVisibility"),
        TIMESTAMP((short) 4, "timestamp"),
        VALUE((short) 5, "value"),
        DELETE_CELL((short) 6, "deleteCell");

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
                case 3:
                    return COL_VISIBILITY;
                case 4:
                    return TIMESTAMP;
                case 5:
                    return VALUE;
                case 6:
                    return DELETE_CELL;
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

    private static final int __DELETECELL_ISSET_ID = 1;

    private byte __isset_bitfield = 0;

    private static final _Fields[] optionals = { _Fields.COL_VISIBILITY, _Fields.TIMESTAMP, _Fields.VALUE, _Fields.DELETE_CELL };

    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.COL_FAMILY, new org.apache.thrift.meta_data.FieldMetaData("colFamily", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.COL_QUALIFIER, new org.apache.thrift.meta_data.FieldMetaData("colQualifier", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.COL_VISIBILITY, new org.apache.thrift.meta_data.FieldMetaData("colVisibility", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.TIMESTAMP, new org.apache.thrift.meta_data.FieldMetaData("timestamp", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.VALUE, new org.apache.thrift.meta_data.FieldMetaData("value", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.DELETE_CELL, new org.apache.thrift.meta_data.FieldMetaData("deleteCell", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ColumnUpdate.class, metaDataMap);
    }

    public ColumnUpdate() {
    }

    public ColumnUpdate(java.nio.ByteBuffer colFamily, java.nio.ByteBuffer colQualifier) {
        this();
        this.colFamily = org.apache.thrift.TBaseHelper.copyBinary(colFamily);
        this.colQualifier = org.apache.thrift.TBaseHelper.copyBinary(colQualifier);
    }

    public ColumnUpdate(ColumnUpdate other) {
        __isset_bitfield = other.__isset_bitfield;
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
        if (other.isSetValue()) {
            this.value = org.apache.thrift.TBaseHelper.copyBinary(other.value);
        }
        this.deleteCell = other.deleteCell;
    }

    public ColumnUpdate deepCopy() {
        return new ColumnUpdate(this);
    }

    @Override
    public void clear() {
        this.colFamily = null;
        this.colQualifier = null;
        this.colVisibility = null;
        setTimestampIsSet(false);
        this.timestamp = 0;
        this.value = null;
        setDeleteCellIsSet(false);
        this.deleteCell = false;
    }

    public byte[] getColFamily() {
        setColFamily(org.apache.thrift.TBaseHelper.rightSize(colFamily));
        return colFamily == null ? null : colFamily.array();
    }

    public java.nio.ByteBuffer bufferForColFamily() {
        return org.apache.thrift.TBaseHelper.copyBinary(colFamily);
    }

    public ColumnUpdate setColFamily(byte[] colFamily) {
        this.colFamily = colFamily == null ? (java.nio.ByteBuffer) null : java.nio.ByteBuffer.wrap(colFamily.clone());
        return this;
    }

    public ColumnUpdate setColFamily(@org.apache.thrift.annotation.Nullable java.nio.ByteBuffer colFamily) {
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

    public ColumnUpdate setColQualifier(byte[] colQualifier) {
        this.colQualifier = colQualifier == null ? (java.nio.ByteBuffer) null : java.nio.ByteBuffer.wrap(colQualifier.clone());
        return this;
    }

    public ColumnUpdate setColQualifier(@org.apache.thrift.annotation.Nullable java.nio.ByteBuffer colQualifier) {
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

    public ColumnUpdate setColVisibility(byte[] colVisibility) {
        this.colVisibility = colVisibility == null ? (java.nio.ByteBuffer) null : java.nio.ByteBuffer.wrap(colVisibility.clone());
        return this;
    }

    public ColumnUpdate setColVisibility(@org.apache.thrift.annotation.Nullable java.nio.ByteBuffer colVisibility) {
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

    public ColumnUpdate setTimestamp(long timestamp) {
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

    public byte[] getValue() {
        setValue(org.apache.thrift.TBaseHelper.rightSize(value));
        return value == null ? null : value.array();
    }

    public java.nio.ByteBuffer bufferForValue() {
        return org.apache.thrift.TBaseHelper.copyBinary(value);
    }

    public ColumnUpdate setValue(byte[] value) {
        this.value = value == null ? (java.nio.ByteBuffer) null : java.nio.ByteBuffer.wrap(value.clone());
        return this;
    }

    public ColumnUpdate setValue(@org.apache.thrift.annotation.Nullable java.nio.ByteBuffer value) {
        this.value = org.apache.thrift.TBaseHelper.copyBinary(value);
        return this;
    }

    public void unsetValue() {
        this.value = null;
    }

    public boolean isSetValue() {
        return this.value != null;
    }

    public void setValueIsSet(boolean value) {
        if (!value) {
            this.value = null;
        }
    }

    public boolean isDeleteCell() {
        return this.deleteCell;
    }

    public ColumnUpdate setDeleteCell(boolean deleteCell) {
        this.deleteCell = deleteCell;
        setDeleteCellIsSet(true);
        return this;
    }

    public void unsetDeleteCell() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __DELETECELL_ISSET_ID);
    }

    public boolean isSetDeleteCell() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __DELETECELL_ISSET_ID);
    }

    public void setDeleteCellIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __DELETECELL_ISSET_ID, value);
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
            case VALUE:
                if (value == null) {
                    unsetValue();
                } else {
                    if (value instanceof byte[]) {
                        setValue((byte[]) value);
                    } else {
                        setValue((java.nio.ByteBuffer) value);
                    }
                }
                break;
            case DELETE_CELL:
                if (value == null) {
                    unsetDeleteCell();
                } else {
                    setDeleteCell((java.lang.Boolean) value);
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
            case COL_VISIBILITY:
                return getColVisibility();
            case TIMESTAMP:
                return getTimestamp();
            case VALUE:
                return getValue();
            case DELETE_CELL:
                return isDeleteCell();
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
            case COL_VISIBILITY:
                return isSetColVisibility();
            case TIMESTAMP:
                return isSetTimestamp();
            case VALUE:
                return isSetValue();
            case DELETE_CELL:
                return isSetDeleteCell();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof ColumnUpdate)
            return this.equals((ColumnUpdate) that);
        return false;
    }

    public boolean equals(ColumnUpdate that) {
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
        boolean this_present_value = true && this.isSetValue();
        boolean that_present_value = true && that.isSetValue();
        if (this_present_value || that_present_value) {
            if (!(this_present_value && that_present_value))
                return false;
            if (!this.value.equals(that.value))
                return false;
        }
        boolean this_present_deleteCell = true && this.isSetDeleteCell();
        boolean that_present_deleteCell = true && that.isSetDeleteCell();
        if (this_present_deleteCell || that_present_deleteCell) {
            if (!(this_present_deleteCell && that_present_deleteCell))
                return false;
            if (this.deleteCell != that.deleteCell)
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
        hashCode = hashCode * 8191 + ((isSetColVisibility()) ? 131071 : 524287);
        if (isSetColVisibility())
            hashCode = hashCode * 8191 + colVisibility.hashCode();
        hashCode = hashCode * 8191 + ((isSetTimestamp()) ? 131071 : 524287);
        if (isSetTimestamp())
            hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(timestamp);
        hashCode = hashCode * 8191 + ((isSetValue()) ? 131071 : 524287);
        if (isSetValue())
            hashCode = hashCode * 8191 + value.hashCode();
        hashCode = hashCode * 8191 + ((isSetDeleteCell()) ? 131071 : 524287);
        if (isSetDeleteCell())
            hashCode = hashCode * 8191 + ((deleteCell) ? 131071 : 524287);
        return hashCode;
    }

    @Override
    public int compareTo(ColumnUpdate other) {
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
        lastComparison = java.lang.Boolean.valueOf(isSetValue()).compareTo(other.isSetValue());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetValue()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.value, other.value);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetDeleteCell()).compareTo(other.isSetDeleteCell());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetDeleteCell()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.deleteCell, other.deleteCell);
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("ColumnUpdate(");
        boolean first = true;
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
        if (isSetColVisibility()) {
            if (!first)
                sb.append(", ");
            sb.append("colVisibility:");
            if (this.colVisibility == null) {
                sb.append("null");
            } else {
                org.apache.thrift.TBaseHelper.toString(this.colVisibility, sb);
            }
            first = false;
        }
        if (isSetTimestamp()) {
            if (!first)
                sb.append(", ");
            sb.append("timestamp:");
            sb.append(this.timestamp);
            first = false;
        }
        if (isSetValue()) {
            if (!first)
                sb.append(", ");
            sb.append("value:");
            if (this.value == null) {
                sb.append("null");
            } else {
                org.apache.thrift.TBaseHelper.toString(this.value, sb);
            }
            first = false;
        }
        if (isSetDeleteCell()) {
            if (!first)
                sb.append(", ");
            sb.append("deleteCell:");
            sb.append(this.deleteCell);
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

    private static class ColumnUpdateStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public ColumnUpdateStandardScheme getScheme() {
            return new ColumnUpdateStandardScheme();
        }
    }

    private static class ColumnUpdateStandardScheme extends org.apache.thrift.scheme.StandardScheme<ColumnUpdate> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, ColumnUpdate struct) throws org.apache.thrift.TException {
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
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.colVisibility = iprot.readBinary();
                            struct.setColVisibilityIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.timestamp = iprot.readI64();
                            struct.setTimestampIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 5:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.value = iprot.readBinary();
                            struct.setValueIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 6:
                        if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
                            struct.deleteCell = iprot.readBool();
                            struct.setDeleteCellIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, ColumnUpdate struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
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
                if (struct.isSetColVisibility()) {
                    oprot.writeFieldBegin(COL_VISIBILITY_FIELD_DESC);
                    oprot.writeBinary(struct.colVisibility);
                    oprot.writeFieldEnd();
                }
            }
            if (struct.isSetTimestamp()) {
                oprot.writeFieldBegin(TIMESTAMP_FIELD_DESC);
                oprot.writeI64(struct.timestamp);
                oprot.writeFieldEnd();
            }
            if (struct.value != null) {
                if (struct.isSetValue()) {
                    oprot.writeFieldBegin(VALUE_FIELD_DESC);
                    oprot.writeBinary(struct.value);
                    oprot.writeFieldEnd();
                }
            }
            if (struct.isSetDeleteCell()) {
                oprot.writeFieldBegin(DELETE_CELL_FIELD_DESC);
                oprot.writeBool(struct.deleteCell);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class ColumnUpdateTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public ColumnUpdateTupleScheme getScheme() {
            return new ColumnUpdateTupleScheme();
        }
    }

    private static class ColumnUpdateTupleScheme extends org.apache.thrift.scheme.TupleScheme<ColumnUpdate> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, ColumnUpdate struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
            if (struct.isSetColFamily()) {
                optionals.set(0);
            }
            if (struct.isSetColQualifier()) {
                optionals.set(1);
            }
            if (struct.isSetColVisibility()) {
                optionals.set(2);
            }
            if (struct.isSetTimestamp()) {
                optionals.set(3);
            }
            if (struct.isSetValue()) {
                optionals.set(4);
            }
            if (struct.isSetDeleteCell()) {
                optionals.set(5);
            }
            oprot.writeBitSet(optionals, 6);
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
            if (struct.isSetValue()) {
                oprot.writeBinary(struct.value);
            }
            if (struct.isSetDeleteCell()) {
                oprot.writeBool(struct.deleteCell);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, ColumnUpdate struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(6);
            if (incoming.get(0)) {
                struct.colFamily = iprot.readBinary();
                struct.setColFamilyIsSet(true);
            }
            if (incoming.get(1)) {
                struct.colQualifier = iprot.readBinary();
                struct.setColQualifierIsSet(true);
            }
            if (incoming.get(2)) {
                struct.colVisibility = iprot.readBinary();
                struct.setColVisibilityIsSet(true);
            }
            if (incoming.get(3)) {
                struct.timestamp = iprot.readI64();
                struct.setTimestampIsSet(true);
            }
            if (incoming.get(4)) {
                struct.value = iprot.readBinary();
                struct.setValueIsSet(true);
            }
            if (incoming.get(5)) {
                struct.deleteCell = iprot.readBool();
                struct.setDeleteCellIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}