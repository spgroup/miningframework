package org.apache.accumulo.core.client.impl.thrift;

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
public class ThriftTableOperationException extends TException implements org.apache.thrift.TBase<ThriftTableOperationException, ThriftTableOperationException._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ThriftTableOperationException");

    private static final org.apache.thrift.protocol.TField TABLE_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("tableId", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final org.apache.thrift.protocol.TField TABLE_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("tableName", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.protocol.TField OP_FIELD_DESC = new org.apache.thrift.protocol.TField("op", org.apache.thrift.protocol.TType.I32, (short) 3);

    private static final org.apache.thrift.protocol.TField TYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("type", org.apache.thrift.protocol.TType.I32, (short) 4);

    private static final org.apache.thrift.protocol.TField DESCRIPTION_FIELD_DESC = new org.apache.thrift.protocol.TField("description", org.apache.thrift.protocol.TType.STRING, (short) 5);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new ThriftTableOperationExceptionStandardSchemeFactory());
        schemes.put(TupleScheme.class, new ThriftTableOperationExceptionTupleSchemeFactory());
    }

    public String tableId;

    public String tableName;

    public TableOperation op;

    public TableOperationExceptionType type;

    public String description;

    @SuppressWarnings("all")
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        TABLE_ID((short) 1, "tableId"), TABLE_NAME((short) 2, "tableName"), OP((short) 3, "op"), TYPE((short) 4, "type"), DESCRIPTION((short) 5, "description");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return TABLE_ID;
                case 2:
                    return TABLE_NAME;
                case 3:
                    return OP;
                case 4:
                    return TYPE;
                case 5:
                    return DESCRIPTION;
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

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.TABLE_ID, new org.apache.thrift.meta_data.FieldMetaData("tableId", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.TABLE_NAME, new org.apache.thrift.meta_data.FieldMetaData("tableName", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.OP, new org.apache.thrift.meta_data.FieldMetaData("op", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, TableOperation.class)));
        tmpMap.put(_Fields.TYPE, new org.apache.thrift.meta_data.FieldMetaData("type", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, TableOperationExceptionType.class)));
        tmpMap.put(_Fields.DESCRIPTION, new org.apache.thrift.meta_data.FieldMetaData("description", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ThriftTableOperationException.class, metaDataMap);
    }

    public ThriftTableOperationException() {
    }

    public ThriftTableOperationException(String tableId, String tableName, TableOperation op, TableOperationExceptionType type, String description) {
        this();
        this.tableId = tableId;
        this.tableName = tableName;
        this.op = op;
        this.type = type;
        this.description = description;
    }

    public ThriftTableOperationException(ThriftTableOperationException other) {
        if (other.isSetTableId()) {
            this.tableId = other.tableId;
        }
        if (other.isSetTableName()) {
            this.tableName = other.tableName;
        }
        if (other.isSetOp()) {
            this.op = other.op;
        }
        if (other.isSetType()) {
            this.type = other.type;
        }
        if (other.isSetDescription()) {
            this.description = other.description;
        }
    }

    public ThriftTableOperationException deepCopy() {
        return new ThriftTableOperationException(this);
    }

    @Override
    public void clear() {
        this.tableId = null;
        this.tableName = null;
        this.op = null;
        this.type = null;
        this.description = null;
    }

    public String getTableId() {
        return this.tableId;
    }

    public ThriftTableOperationException setTableId(String tableId) {
        this.tableId = tableId;
        return this;
    }

    public void unsetTableId() {
        this.tableId = null;
    }

    public boolean isSetTableId() {
        return this.tableId != null;
    }

    public void setTableIdIsSet(boolean value) {
        if (!value) {
            this.tableId = null;
        }
    }

    public String getTableName() {
        return this.tableName;
    }

    public ThriftTableOperationException setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public void unsetTableName() {
        this.tableName = null;
    }

    public boolean isSetTableName() {
        return this.tableName != null;
    }

    public void setTableNameIsSet(boolean value) {
        if (!value) {
            this.tableName = null;
        }
    }

    public TableOperation getOp() {
        return this.op;
    }

    public ThriftTableOperationException setOp(TableOperation op) {
        this.op = op;
        return this;
    }

    public void unsetOp() {
        this.op = null;
    }

    public boolean isSetOp() {
        return this.op != null;
    }

    public void setOpIsSet(boolean value) {
        if (!value) {
            this.op = null;
        }
    }

    public TableOperationExceptionType getType() {
        return this.type;
    }

    public ThriftTableOperationException setType(TableOperationExceptionType type) {
        this.type = type;
        return this;
    }

    public void unsetType() {
        this.type = null;
    }

    public boolean isSetType() {
        return this.type != null;
    }

    public void setTypeIsSet(boolean value) {
        if (!value) {
            this.type = null;
        }
    }

    public String getDescription() {
        return this.description;
    }

    public ThriftTableOperationException setDescription(String description) {
        this.description = description;
        return this;
    }

    public void unsetDescription() {
        this.description = null;
    }

    public boolean isSetDescription() {
        return this.description != null;
    }

    public void setDescriptionIsSet(boolean value) {
        if (!value) {
            this.description = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case TABLE_ID:
                if (value == null) {
                    unsetTableId();
                } else {
                    setTableId((String) value);
                }
                break;
            case TABLE_NAME:
                if (value == null) {
                    unsetTableName();
                } else {
                    setTableName((String) value);
                }
                break;
            case OP:
                if (value == null) {
                    unsetOp();
                } else {
                    setOp((TableOperation) value);
                }
                break;
            case TYPE:
                if (value == null) {
                    unsetType();
                } else {
                    setType((TableOperationExceptionType) value);
                }
                break;
            case DESCRIPTION:
                if (value == null) {
                    unsetDescription();
                } else {
                    setDescription((String) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case TABLE_ID:
                return getTableId();
            case TABLE_NAME:
                return getTableName();
            case OP:
                return getOp();
            case TYPE:
                return getType();
            case DESCRIPTION:
                return getDescription();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case TABLE_ID:
                return isSetTableId();
            case TABLE_NAME:
                return isSetTableName();
            case OP:
                return isSetOp();
            case TYPE:
                return isSetType();
            case DESCRIPTION:
                return isSetDescription();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof ThriftTableOperationException)
            return this.equals((ThriftTableOperationException) that);
        return false;
    }

    public boolean equals(ThriftTableOperationException that) {
        if (that == null)
            return false;
        boolean this_present_tableId = true && this.isSetTableId();
        boolean that_present_tableId = true && that.isSetTableId();
        if (this_present_tableId || that_present_tableId) {
            if (!(this_present_tableId && that_present_tableId))
                return false;
            if (!this.tableId.equals(that.tableId))
                return false;
        }
        boolean this_present_tableName = true && this.isSetTableName();
        boolean that_present_tableName = true && that.isSetTableName();
        if (this_present_tableName || that_present_tableName) {
            if (!(this_present_tableName && that_present_tableName))
                return false;
            if (!this.tableName.equals(that.tableName))
                return false;
        }
        boolean this_present_op = true && this.isSetOp();
        boolean that_present_op = true && that.isSetOp();
        if (this_present_op || that_present_op) {
            if (!(this_present_op && that_present_op))
                return false;
            if (!this.op.equals(that.op))
                return false;
        }
        boolean this_present_type = true && this.isSetType();
        boolean that_present_type = true && that.isSetType();
        if (this_present_type || that_present_type) {
            if (!(this_present_type && that_present_type))
                return false;
            if (!this.type.equals(that.type))
                return false;
        }
        boolean this_present_description = true && this.isSetDescription();
        boolean that_present_description = true && that.isSetDescription();
        if (this_present_description || that_present_description) {
            if (!(this_present_description && that_present_description))
                return false;
            if (!this.description.equals(that.description))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public int compareTo(ThriftTableOperationException other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        ThriftTableOperationException typedOther = (ThriftTableOperationException) other;
        lastComparison = Boolean.valueOf(isSetTableId()).compareTo(typedOther.isSetTableId());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTableId()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tableId, typedOther.tableId);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetTableName()).compareTo(typedOther.isSetTableName());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTableName()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tableName, typedOther.tableName);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetOp()).compareTo(typedOther.isSetOp());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetOp()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.op, typedOther.op);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetType()).compareTo(typedOther.isSetType());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetType()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.type, typedOther.type);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetDescription()).compareTo(typedOther.isSetDescription());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetDescription()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.description, typedOther.description);
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
        StringBuilder sb = new StringBuilder("ThriftTableOperationException(");
        boolean first = true;
        sb.append("tableId:");
        if (this.tableId == null) {
            sb.append("null");
        } else {
            sb.append(this.tableId);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("tableName:");
        if (this.tableName == null) {
            sb.append("null");
        } else {
            sb.append(this.tableName);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("op:");
        if (this.op == null) {
            sb.append("null");
        } else {
            sb.append(this.op);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("type:");
        if (this.type == null) {
            sb.append("null");
        } else {
            sb.append(this.type);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("description:");
        if (this.description == null) {
            sb.append("null");
        } else {
            sb.append(this.description);
        }
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
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class ThriftTableOperationExceptionStandardSchemeFactory implements SchemeFactory {

        public ThriftTableOperationExceptionStandardScheme getScheme() {
            return new ThriftTableOperationExceptionStandardScheme();
        }
    }

    private static class ThriftTableOperationExceptionStandardScheme extends StandardScheme<ThriftTableOperationException> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, ThriftTableOperationException struct) throws org.apache.thrift.TException {
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
                            struct.tableId = iprot.readString();
                            struct.setTableIdIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.tableName = iprot.readString();
                            struct.setTableNameIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.op = TableOperation.findByValue(iprot.readI32());
                            struct.setOpIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.type = TableOperationExceptionType.findByValue(iprot.readI32());
                            struct.setTypeIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 5:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.description = iprot.readString();
                            struct.setDescriptionIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, ThriftTableOperationException struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.tableId != null) {
                oprot.writeFieldBegin(TABLE_ID_FIELD_DESC);
                oprot.writeString(struct.tableId);
                oprot.writeFieldEnd();
            }
            if (struct.tableName != null) {
                oprot.writeFieldBegin(TABLE_NAME_FIELD_DESC);
                oprot.writeString(struct.tableName);
                oprot.writeFieldEnd();
            }
            if (struct.op != null) {
                oprot.writeFieldBegin(OP_FIELD_DESC);
                oprot.writeI32(struct.op.getValue());
                oprot.writeFieldEnd();
            }
            if (struct.type != null) {
                oprot.writeFieldBegin(TYPE_FIELD_DESC);
                oprot.writeI32(struct.type.getValue());
                oprot.writeFieldEnd();
            }
            if (struct.description != null) {
                oprot.writeFieldBegin(DESCRIPTION_FIELD_DESC);
                oprot.writeString(struct.description);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class ThriftTableOperationExceptionTupleSchemeFactory implements SchemeFactory {

        public ThriftTableOperationExceptionTupleScheme getScheme() {
            return new ThriftTableOperationExceptionTupleScheme();
        }
    }

    private static class ThriftTableOperationExceptionTupleScheme extends TupleScheme<ThriftTableOperationException> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, ThriftTableOperationException struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetTableId()) {
                optionals.set(0);
            }
            if (struct.isSetTableName()) {
                optionals.set(1);
            }
            if (struct.isSetOp()) {
                optionals.set(2);
            }
            if (struct.isSetType()) {
                optionals.set(3);
            }
            if (struct.isSetDescription()) {
                optionals.set(4);
            }
            oprot.writeBitSet(optionals, 5);
            if (struct.isSetTableId()) {
                oprot.writeString(struct.tableId);
            }
            if (struct.isSetTableName()) {
                oprot.writeString(struct.tableName);
            }
            if (struct.isSetOp()) {
                oprot.writeI32(struct.op.getValue());
            }
            if (struct.isSetType()) {
                oprot.writeI32(struct.type.getValue());
            }
            if (struct.isSetDescription()) {
                oprot.writeString(struct.description);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, ThriftTableOperationException struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(5);
            if (incoming.get(0)) {
                struct.tableId = iprot.readString();
                struct.setTableIdIsSet(true);
            }
            if (incoming.get(1)) {
                struct.tableName = iprot.readString();
                struct.setTableNameIsSet(true);
            }
            if (incoming.get(2)) {
                struct.op = TableOperation.findByValue(iprot.readI32());
                struct.setOpIsSet(true);
            }
            if (incoming.get(3)) {
                struct.type = TableOperationExceptionType.findByValue(iprot.readI32());
                struct.setTypeIsSet(true);
            }
            if (incoming.get(4)) {
                struct.description = iprot.readString();
                struct.setDescriptionIsSet(true);
            }
        }
    }
}