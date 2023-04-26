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
public class InitialMultiScan implements org.apache.thrift.TBase<InitialMultiScan, InitialMultiScan._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("InitialMultiScan");

    private static final org.apache.thrift.protocol.TField SCAN_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("scanID", org.apache.thrift.protocol.TType.I64, (short) 1);

    private static final org.apache.thrift.protocol.TField RESULT_FIELD_DESC = new org.apache.thrift.protocol.TField("result", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new InitialMultiScanStandardSchemeFactory());
        schemes.put(TupleScheme.class, new InitialMultiScanTupleSchemeFactory());
    }

    public long scanID;

    public MultiScanResult result;

    @SuppressWarnings("all")
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        SCAN_ID((short) 1, "scanID"), RESULT((short) 2, "result");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return SCAN_ID;
                case 2:
                    return RESULT;
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

    private static final int __SCANID_ISSET_ID = 0;

    private byte __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.SCAN_ID, new org.apache.thrift.meta_data.FieldMetaData("scanID", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64, "ScanID")));
        tmpMap.put(_Fields.RESULT, new org.apache.thrift.meta_data.FieldMetaData("result", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, MultiScanResult.class)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(InitialMultiScan.class, metaDataMap);
    }

    public InitialMultiScan() {
    }

    public InitialMultiScan(long scanID, MultiScanResult result) {
        this();
        this.scanID = scanID;
        setScanIDIsSet(true);
        this.result = result;
    }

    public InitialMultiScan(InitialMultiScan other) {
        __isset_bitfield = other.__isset_bitfield;
        this.scanID = other.scanID;
        if (other.isSetResult()) {
            this.result = new MultiScanResult(other.result);
        }
    }

    public InitialMultiScan deepCopy() {
        return new InitialMultiScan(this);
    }

    @Override
    public void clear() {
        setScanIDIsSet(false);
        this.scanID = 0;
        this.result = null;
    }

    public long getScanID() {
        return this.scanID;
    }

    public InitialMultiScan setScanID(long scanID) {
        this.scanID = scanID;
        setScanIDIsSet(true);
        return this;
    }

    public void unsetScanID() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __SCANID_ISSET_ID);
    }

    public boolean isSetScanID() {
        return EncodingUtils.testBit(__isset_bitfield, __SCANID_ISSET_ID);
    }

    public void setScanIDIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __SCANID_ISSET_ID, value);
    }

    public MultiScanResult getResult() {
        return this.result;
    }

    public InitialMultiScan setResult(MultiScanResult result) {
        this.result = result;
        return this;
    }

    public void unsetResult() {
        this.result = null;
    }

    public boolean isSetResult() {
        return this.result != null;
    }

    public void setResultIsSet(boolean value) {
        if (!value) {
            this.result = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case SCAN_ID:
                if (value == null) {
                    unsetScanID();
                } else {
                    setScanID((Long) value);
                }
                break;
            case RESULT:
                if (value == null) {
                    unsetResult();
                } else {
                    setResult((MultiScanResult) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case SCAN_ID:
                return Long.valueOf(getScanID());
            case RESULT:
                return getResult();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case SCAN_ID:
                return isSetScanID();
            case RESULT:
                return isSetResult();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof InitialMultiScan)
            return this.equals((InitialMultiScan) that);
        return false;
    }

    public boolean equals(InitialMultiScan that) {
        if (that == null)
            return false;
        boolean this_present_scanID = true;
        boolean that_present_scanID = true;
        if (this_present_scanID || that_present_scanID) {
            if (!(this_present_scanID && that_present_scanID))
                return false;
            if (this.scanID != that.scanID)
                return false;
        }
        boolean this_present_result = true && this.isSetResult();
        boolean that_present_result = true && that.isSetResult();
        if (this_present_result || that_present_result) {
            if (!(this_present_result && that_present_result))
                return false;
            if (!this.result.equals(that.result))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public int compareTo(InitialMultiScan other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        InitialMultiScan typedOther = (InitialMultiScan) other;
        lastComparison = Boolean.valueOf(isSetScanID()).compareTo(typedOther.isSetScanID());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetScanID()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.scanID, typedOther.scanID);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetResult()).compareTo(typedOther.isSetResult());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetResult()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.result, typedOther.result);
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
        StringBuilder sb = new StringBuilder("InitialMultiScan(");
        boolean first = true;
        sb.append("scanID:");
        sb.append(this.scanID);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("result:");
        if (this.result == null) {
            sb.append("null");
        } else {
            sb.append(this.result);
        }
        first = false;
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
        if (result != null) {
            result.validate();
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

    private static class InitialMultiScanStandardSchemeFactory implements SchemeFactory {

        public InitialMultiScanStandardScheme getScheme() {
            return new InitialMultiScanStandardScheme();
        }
    }

    private static class InitialMultiScanStandardScheme extends StandardScheme<InitialMultiScan> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, InitialMultiScan struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch(schemeField.id) {
                    case 1:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.scanID = iprot.readI64();
                            struct.setScanIDIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.result = new MultiScanResult();
                            struct.result.read(iprot);
                            struct.setResultIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, InitialMultiScan struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            oprot.writeFieldBegin(SCAN_ID_FIELD_DESC);
            oprot.writeI64(struct.scanID);
            oprot.writeFieldEnd();
            if (struct.result != null) {
                oprot.writeFieldBegin(RESULT_FIELD_DESC);
                struct.result.write(oprot);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class InitialMultiScanTupleSchemeFactory implements SchemeFactory {

        public InitialMultiScanTupleScheme getScheme() {
            return new InitialMultiScanTupleScheme();
        }
    }

    private static class InitialMultiScanTupleScheme extends TupleScheme<InitialMultiScan> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, InitialMultiScan struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetScanID()) {
                optionals.set(0);
            }
            if (struct.isSetResult()) {
                optionals.set(1);
            }
            oprot.writeBitSet(optionals, 2);
            if (struct.isSetScanID()) {
                oprot.writeI64(struct.scanID);
            }
            if (struct.isSetResult()) {
                struct.result.write(oprot);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, InitialMultiScan struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(2);
            if (incoming.get(0)) {
                struct.scanID = iprot.readI64();
                struct.setScanIDIsSet(true);
            }
            if (incoming.get(1)) {
                struct.result = new MultiScanResult();
                struct.result.read(iprot);
                struct.setResultIsSet(true);
            }
        }
    }
}
