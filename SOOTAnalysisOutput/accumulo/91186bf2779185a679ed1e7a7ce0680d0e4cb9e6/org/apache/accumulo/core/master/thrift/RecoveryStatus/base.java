package org.apache.accumulo.core.master.thrift;

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
public class RecoveryStatus implements org.apache.thrift.TBase<RecoveryStatus, RecoveryStatus._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("RecoveryStatus");

    private static final org.apache.thrift.protocol.TField NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("name", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.protocol.TField RUNTIME_FIELD_DESC = new org.apache.thrift.protocol.TField("runtime", org.apache.thrift.protocol.TType.I32, (short) 5);

    private static final org.apache.thrift.protocol.TField PROGRESS_FIELD_DESC = new org.apache.thrift.protocol.TField("progress", org.apache.thrift.protocol.TType.DOUBLE, (short) 6);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new RecoveryStatusStandardSchemeFactory());
        schemes.put(TupleScheme.class, new RecoveryStatusTupleSchemeFactory());
    }

    public String name;

    public int runtime;

    public double progress;

    @SuppressWarnings("all")
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        NAME((short) 2, "name"), RUNTIME((short) 5, "runtime"), PROGRESS((short) 6, "progress");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 2:
                    return NAME;
                case 5:
                    return RUNTIME;
                case 6:
                    return PROGRESS;
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

    private static final int __RUNTIME_ISSET_ID = 0;

    private static final int __PROGRESS_ISSET_ID = 1;

    private byte __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.NAME, new org.apache.thrift.meta_data.FieldMetaData("name", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.RUNTIME, new org.apache.thrift.meta_data.FieldMetaData("runtime", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.PROGRESS, new org.apache.thrift.meta_data.FieldMetaData("progress", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(RecoveryStatus.class, metaDataMap);
    }

    public RecoveryStatus() {
    }

    public RecoveryStatus(String name, int runtime, double progress) {
        this();
        this.name = name;
        this.runtime = runtime;
        setRuntimeIsSet(true);
        this.progress = progress;
        setProgressIsSet(true);
    }

    public RecoveryStatus(RecoveryStatus other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetName()) {
            this.name = other.name;
        }
        this.runtime = other.runtime;
        this.progress = other.progress;
    }

    public RecoveryStatus deepCopy() {
        return new RecoveryStatus(this);
    }

    @Override
    public void clear() {
        this.name = null;
        setRuntimeIsSet(false);
        this.runtime = 0;
        setProgressIsSet(false);
        this.progress = 0.0;
    }

    public String getName() {
        return this.name;
    }

    public RecoveryStatus setName(String name) {
        this.name = name;
        return this;
    }

    public void unsetName() {
        this.name = null;
    }

    public boolean isSetName() {
        return this.name != null;
    }

    public void setNameIsSet(boolean value) {
        if (!value) {
            this.name = null;
        }
    }

    public int getRuntime() {
        return this.runtime;
    }

    public RecoveryStatus setRuntime(int runtime) {
        this.runtime = runtime;
        setRuntimeIsSet(true);
        return this;
    }

    public void unsetRuntime() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __RUNTIME_ISSET_ID);
    }

    public boolean isSetRuntime() {
        return EncodingUtils.testBit(__isset_bitfield, __RUNTIME_ISSET_ID);
    }

    public void setRuntimeIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __RUNTIME_ISSET_ID, value);
    }

    public double getProgress() {
        return this.progress;
    }

    public RecoveryStatus setProgress(double progress) {
        this.progress = progress;
        setProgressIsSet(true);
        return this;
    }

    public void unsetProgress() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __PROGRESS_ISSET_ID);
    }

    public boolean isSetProgress() {
        return EncodingUtils.testBit(__isset_bitfield, __PROGRESS_ISSET_ID);
    }

    public void setProgressIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __PROGRESS_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case NAME:
                if (value == null) {
                    unsetName();
                } else {
                    setName((String) value);
                }
                break;
            case RUNTIME:
                if (value == null) {
                    unsetRuntime();
                } else {
                    setRuntime((Integer) value);
                }
                break;
            case PROGRESS:
                if (value == null) {
                    unsetProgress();
                } else {
                    setProgress((Double) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case NAME:
                return getName();
            case RUNTIME:
                return Integer.valueOf(getRuntime());
            case PROGRESS:
                return Double.valueOf(getProgress());
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case NAME:
                return isSetName();
            case RUNTIME:
                return isSetRuntime();
            case PROGRESS:
                return isSetProgress();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof RecoveryStatus)
            return this.equals((RecoveryStatus) that);
        return false;
    }

    public boolean equals(RecoveryStatus that) {
        if (that == null)
            return false;
        boolean this_present_name = true && this.isSetName();
        boolean that_present_name = true && that.isSetName();
        if (this_present_name || that_present_name) {
            if (!(this_present_name && that_present_name))
                return false;
            if (!this.name.equals(that.name))
                return false;
        }
        boolean this_present_runtime = true;
        boolean that_present_runtime = true;
        if (this_present_runtime || that_present_runtime) {
            if (!(this_present_runtime && that_present_runtime))
                return false;
            if (this.runtime != that.runtime)
                return false;
        }
        boolean this_present_progress = true;
        boolean that_present_progress = true;
        if (this_present_progress || that_present_progress) {
            if (!(this_present_progress && that_present_progress))
                return false;
            if (this.progress != that.progress)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public int compareTo(RecoveryStatus other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        RecoveryStatus typedOther = (RecoveryStatus) other;
        lastComparison = Boolean.valueOf(isSetName()).compareTo(typedOther.isSetName());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetName()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.name, typedOther.name);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetRuntime()).compareTo(typedOther.isSetRuntime());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetRuntime()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.runtime, typedOther.runtime);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetProgress()).compareTo(typedOther.isSetProgress());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetProgress()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.progress, typedOther.progress);
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
        StringBuilder sb = new StringBuilder("RecoveryStatus(");
        boolean first = true;
        sb.append("name:");
        if (this.name == null) {
            sb.append("null");
        } else {
            sb.append(this.name);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("runtime:");
        sb.append(this.runtime);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("progress:");
        sb.append(this.progress);
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

    private static class RecoveryStatusStandardSchemeFactory implements SchemeFactory {

        public RecoveryStatusStandardScheme getScheme() {
            return new RecoveryStatusStandardScheme();
        }
    }

    private static class RecoveryStatusStandardScheme extends StandardScheme<RecoveryStatus> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, RecoveryStatus struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch(schemeField.id) {
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.name = iprot.readString();
                            struct.setNameIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 5:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.runtime = iprot.readI32();
                            struct.setRuntimeIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 6:
                        if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
                            struct.progress = iprot.readDouble();
                            struct.setProgressIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, RecoveryStatus struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.name != null) {
                oprot.writeFieldBegin(NAME_FIELD_DESC);
                oprot.writeString(struct.name);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(RUNTIME_FIELD_DESC);
            oprot.writeI32(struct.runtime);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(PROGRESS_FIELD_DESC);
            oprot.writeDouble(struct.progress);
            oprot.writeFieldEnd();
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class RecoveryStatusTupleSchemeFactory implements SchemeFactory {

        public RecoveryStatusTupleScheme getScheme() {
            return new RecoveryStatusTupleScheme();
        }
    }

    private static class RecoveryStatusTupleScheme extends TupleScheme<RecoveryStatus> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, RecoveryStatus struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetName()) {
                optionals.set(0);
            }
            if (struct.isSetRuntime()) {
                optionals.set(1);
            }
            if (struct.isSetProgress()) {
                optionals.set(2);
            }
            oprot.writeBitSet(optionals, 3);
            if (struct.isSetName()) {
                oprot.writeString(struct.name);
            }
            if (struct.isSetRuntime()) {
                oprot.writeI32(struct.runtime);
            }
            if (struct.isSetProgress()) {
                oprot.writeDouble(struct.progress);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, RecoveryStatus struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(3);
            if (incoming.get(0)) {
                struct.name = iprot.readString();
                struct.setNameIsSet(true);
            }
            if (incoming.get(1)) {
                struct.runtime = iprot.readI32();
                struct.setRuntimeIsSet(true);
            }
            if (incoming.get(2)) {
                struct.progress = iprot.readDouble();
                struct.setProgressIsSet(true);
            }
        }
    }
}
