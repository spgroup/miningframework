package org.apache.accumulo.core.data.thrift;

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
public class TKeyExtent implements org.apache.thrift.TBase<TKeyExtent, TKeyExtent._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TKeyExtent");

    private static final org.apache.thrift.protocol.TField TABLE_FIELD_DESC = new org.apache.thrift.protocol.TField("table", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final org.apache.thrift.protocol.TField END_ROW_FIELD_DESC = new org.apache.thrift.protocol.TField("endRow", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.protocol.TField PREV_END_ROW_FIELD_DESC = new org.apache.thrift.protocol.TField("prevEndRow", org.apache.thrift.protocol.TType.STRING, (short) 3);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new TKeyExtentStandardSchemeFactory());
        schemes.put(TupleScheme.class, new TKeyExtentTupleSchemeFactory());
    }

    public ByteBuffer table;

    public ByteBuffer endRow;

    public ByteBuffer prevEndRow;

    @SuppressWarnings("all")
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        TABLE((short) 1, "table"), END_ROW((short) 2, "endRow"), PREV_END_ROW((short) 3, "prevEndRow");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return TABLE;
                case 2:
                    return END_ROW;
                case 3:
                    return PREV_END_ROW;
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
        tmpMap.put(_Fields.TABLE, new org.apache.thrift.meta_data.FieldMetaData("table", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.END_ROW, new org.apache.thrift.meta_data.FieldMetaData("endRow", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.PREV_END_ROW, new org.apache.thrift.meta_data.FieldMetaData("prevEndRow", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TKeyExtent.class, metaDataMap);
    }

    public TKeyExtent() {
    }

    public TKeyExtent(ByteBuffer table, ByteBuffer endRow, ByteBuffer prevEndRow) {
        this();
        this.table = table;
        this.endRow = endRow;
        this.prevEndRow = prevEndRow;
    }

    public TKeyExtent(TKeyExtent other) {
        if (other.isSetTable()) {
            this.table = org.apache.thrift.TBaseHelper.copyBinary(other.table);
            ;
        }
        if (other.isSetEndRow()) {
            this.endRow = org.apache.thrift.TBaseHelper.copyBinary(other.endRow);
            ;
        }
        if (other.isSetPrevEndRow()) {
            this.prevEndRow = org.apache.thrift.TBaseHelper.copyBinary(other.prevEndRow);
            ;
        }
    }

    public TKeyExtent deepCopy() {
        return new TKeyExtent(this);
    }

    @Override
    public void clear() {
        this.table = null;
        this.endRow = null;
        this.prevEndRow = null;
    }

    public byte[] getTable() {
        setTable(org.apache.thrift.TBaseHelper.rightSize(table));
        return table == null ? null : table.array();
    }

    public ByteBuffer bufferForTable() {
        return table;
    }

    public TKeyExtent setTable(byte[] table) {
        setTable(table == null ? (ByteBuffer) null : ByteBuffer.wrap(table));
        return this;
    }

    public TKeyExtent setTable(ByteBuffer table) {
        this.table = table;
        return this;
    }

    public void unsetTable() {
        this.table = null;
    }

    public boolean isSetTable() {
        return this.table != null;
    }

    public void setTableIsSet(boolean value) {
        if (!value) {
            this.table = null;
        }
    }

    public byte[] getEndRow() {
        setEndRow(org.apache.thrift.TBaseHelper.rightSize(endRow));
        return endRow == null ? null : endRow.array();
    }

    public ByteBuffer bufferForEndRow() {
        return endRow;
    }

    public TKeyExtent setEndRow(byte[] endRow) {
        setEndRow(endRow == null ? (ByteBuffer) null : ByteBuffer.wrap(endRow));
        return this;
    }

    public TKeyExtent setEndRow(ByteBuffer endRow) {
        this.endRow = endRow;
        return this;
    }

    public void unsetEndRow() {
        this.endRow = null;
    }

    public boolean isSetEndRow() {
        return this.endRow != null;
    }

    public void setEndRowIsSet(boolean value) {
        if (!value) {
            this.endRow = null;
        }
    }

    public byte[] getPrevEndRow() {
        setPrevEndRow(org.apache.thrift.TBaseHelper.rightSize(prevEndRow));
        return prevEndRow == null ? null : prevEndRow.array();
    }

    public ByteBuffer bufferForPrevEndRow() {
        return prevEndRow;
    }

    public TKeyExtent setPrevEndRow(byte[] prevEndRow) {
        setPrevEndRow(prevEndRow == null ? (ByteBuffer) null : ByteBuffer.wrap(prevEndRow));
        return this;
    }

    public TKeyExtent setPrevEndRow(ByteBuffer prevEndRow) {
        this.prevEndRow = prevEndRow;
        return this;
    }

    public void unsetPrevEndRow() {
        this.prevEndRow = null;
    }

    public boolean isSetPrevEndRow() {
        return this.prevEndRow != null;
    }

    public void setPrevEndRowIsSet(boolean value) {
        if (!value) {
            this.prevEndRow = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case TABLE:
                if (value == null) {
                    unsetTable();
                } else {
                    setTable((ByteBuffer) value);
                }
                break;
            case END_ROW:
                if (value == null) {
                    unsetEndRow();
                } else {
                    setEndRow((ByteBuffer) value);
                }
                break;
            case PREV_END_ROW:
                if (value == null) {
                    unsetPrevEndRow();
                } else {
                    setPrevEndRow((ByteBuffer) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case TABLE:
                return getTable();
            case END_ROW:
                return getEndRow();
            case PREV_END_ROW:
                return getPrevEndRow();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case TABLE:
                return isSetTable();
            case END_ROW:
                return isSetEndRow();
            case PREV_END_ROW:
                return isSetPrevEndRow();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof TKeyExtent)
            return this.equals((TKeyExtent) that);
        return false;
    }

    public boolean equals(TKeyExtent that) {
        if (that == null)
            return false;
        boolean this_present_table = true && this.isSetTable();
        boolean that_present_table = true && that.isSetTable();
        if (this_present_table || that_present_table) {
            if (!(this_present_table && that_present_table))
                return false;
            if (!this.table.equals(that.table))
                return false;
        }
        boolean this_present_endRow = true && this.isSetEndRow();
        boolean that_present_endRow = true && that.isSetEndRow();
        if (this_present_endRow || that_present_endRow) {
            if (!(this_present_endRow && that_present_endRow))
                return false;
            if (!this.endRow.equals(that.endRow))
                return false;
        }
        boolean this_present_prevEndRow = true && this.isSetPrevEndRow();
        boolean that_present_prevEndRow = true && that.isSetPrevEndRow();
        if (this_present_prevEndRow || that_present_prevEndRow) {
            if (!(this_present_prevEndRow && that_present_prevEndRow))
                return false;
            if (!this.prevEndRow.equals(that.prevEndRow))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public int compareTo(TKeyExtent other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        TKeyExtent typedOther = (TKeyExtent) other;
        lastComparison = Boolean.valueOf(isSetTable()).compareTo(typedOther.isSetTable());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTable()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.table, typedOther.table);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetEndRow()).compareTo(typedOther.isSetEndRow());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetEndRow()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.endRow, typedOther.endRow);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetPrevEndRow()).compareTo(typedOther.isSetPrevEndRow());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetPrevEndRow()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.prevEndRow, typedOther.prevEndRow);
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
        StringBuilder sb = new StringBuilder("TKeyExtent(");
        boolean first = true;
        sb.append("table:");
        if (this.table == null) {
            sb.append("null");
        } else {
            org.apache.thrift.TBaseHelper.toString(this.table, sb);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("endRow:");
        if (this.endRow == null) {
            sb.append("null");
        } else {
            org.apache.thrift.TBaseHelper.toString(this.endRow, sb);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("prevEndRow:");
        if (this.prevEndRow == null) {
            sb.append("null");
        } else {
            org.apache.thrift.TBaseHelper.toString(this.prevEndRow, sb);
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

    private static class TKeyExtentStandardSchemeFactory implements SchemeFactory {

        public TKeyExtentStandardScheme getScheme() {
            return new TKeyExtentStandardScheme();
        }
    }

    private static class TKeyExtentStandardScheme extends StandardScheme<TKeyExtent> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, TKeyExtent struct) throws org.apache.thrift.TException {
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
                            struct.table = iprot.readBinary();
                            struct.setTableIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.endRow = iprot.readBinary();
                            struct.setEndRowIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.prevEndRow = iprot.readBinary();
                            struct.setPrevEndRowIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, TKeyExtent struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.table != null) {
                oprot.writeFieldBegin(TABLE_FIELD_DESC);
                oprot.writeBinary(struct.table);
                oprot.writeFieldEnd();
            }
            if (struct.endRow != null) {
                oprot.writeFieldBegin(END_ROW_FIELD_DESC);
                oprot.writeBinary(struct.endRow);
                oprot.writeFieldEnd();
            }
            if (struct.prevEndRow != null) {
                oprot.writeFieldBegin(PREV_END_ROW_FIELD_DESC);
                oprot.writeBinary(struct.prevEndRow);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class TKeyExtentTupleSchemeFactory implements SchemeFactory {

        public TKeyExtentTupleScheme getScheme() {
            return new TKeyExtentTupleScheme();
        }
    }

    private static class TKeyExtentTupleScheme extends TupleScheme<TKeyExtent> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, TKeyExtent struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetTable()) {
                optionals.set(0);
            }
            if (struct.isSetEndRow()) {
                optionals.set(1);
            }
            if (struct.isSetPrevEndRow()) {
                optionals.set(2);
            }
            oprot.writeBitSet(optionals, 3);
            if (struct.isSetTable()) {
                oprot.writeBinary(struct.table);
            }
            if (struct.isSetEndRow()) {
                oprot.writeBinary(struct.endRow);
            }
            if (struct.isSetPrevEndRow()) {
                oprot.writeBinary(struct.prevEndRow);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, TKeyExtent struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(3);
            if (incoming.get(0)) {
                struct.table = iprot.readBinary();
                struct.setTableIsSet(true);
            }
            if (incoming.get(1)) {
                struct.endRow = iprot.readBinary();
                struct.setEndRowIsSet(true);
            }
            if (incoming.get(2)) {
                struct.prevEndRow = iprot.readBinary();
                struct.setPrevEndRowIsSet(true);
            }
        }
    }
}
