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
public class Compacting implements org.apache.thrift.TBase<Compacting, Compacting._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Compacting");

    private static final org.apache.thrift.protocol.TField RUNNING_FIELD_DESC = new org.apache.thrift.protocol.TField("running", org.apache.thrift.protocol.TType.I32, (short) 1);

    private static final org.apache.thrift.protocol.TField QUEUED_FIELD_DESC = new org.apache.thrift.protocol.TField("queued", org.apache.thrift.protocol.TType.I32, (short) 2);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new CompactingStandardSchemeFactory());
        schemes.put(TupleScheme.class, new CompactingTupleSchemeFactory());
    }

    public int running;

    public int queued;

    @SuppressWarnings("all")
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        RUNNING((short) 1, "running"), QUEUED((short) 2, "queued");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return RUNNING;
                case 2:
                    return QUEUED;
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

    private static final int __RUNNING_ISSET_ID = 0;

    private static final int __QUEUED_ISSET_ID = 1;

    private byte __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.RUNNING, new org.apache.thrift.meta_data.FieldMetaData("running", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.QUEUED, new org.apache.thrift.meta_data.FieldMetaData("queued", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Compacting.class, metaDataMap);
    }

    public Compacting() {
    }

    public Compacting(int running, int queued) {
        this();
        this.running = running;
        setRunningIsSet(true);
        this.queued = queued;
        setQueuedIsSet(true);
    }

    public Compacting(Compacting other) {
        __isset_bitfield = other.__isset_bitfield;
        this.running = other.running;
        this.queued = other.queued;
    }

    public Compacting deepCopy() {
        return new Compacting(this);
    }

    @Override
    public void clear() {
        setRunningIsSet(false);
        this.running = 0;
        setQueuedIsSet(false);
        this.queued = 0;
    }

    public int getRunning() {
        return this.running;
    }

    public Compacting setRunning(int running) {
        this.running = running;
        setRunningIsSet(true);
        return this;
    }

    public void unsetRunning() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __RUNNING_ISSET_ID);
    }

    public boolean isSetRunning() {
        return EncodingUtils.testBit(__isset_bitfield, __RUNNING_ISSET_ID);
    }

    public void setRunningIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __RUNNING_ISSET_ID, value);
    }

    public int getQueued() {
        return this.queued;
    }

    public Compacting setQueued(int queued) {
        this.queued = queued;
        setQueuedIsSet(true);
        return this;
    }

    public void unsetQueued() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __QUEUED_ISSET_ID);
    }

    public boolean isSetQueued() {
        return EncodingUtils.testBit(__isset_bitfield, __QUEUED_ISSET_ID);
    }

    public void setQueuedIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __QUEUED_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case RUNNING:
                if (value == null) {
                    unsetRunning();
                } else {
                    setRunning((Integer) value);
                }
                break;
            case QUEUED:
                if (value == null) {
                    unsetQueued();
                } else {
                    setQueued((Integer) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case RUNNING:
                return Integer.valueOf(getRunning());
            case QUEUED:
                return Integer.valueOf(getQueued());
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case RUNNING:
                return isSetRunning();
            case QUEUED:
                return isSetQueued();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof Compacting)
            return this.equals((Compacting) that);
        return false;
    }

    public boolean equals(Compacting that) {
        if (that == null)
            return false;
        boolean this_present_running = true;
        boolean that_present_running = true;
        if (this_present_running || that_present_running) {
            if (!(this_present_running && that_present_running))
                return false;
            if (this.running != that.running)
                return false;
        }
        boolean this_present_queued = true;
        boolean that_present_queued = true;
        if (this_present_queued || that_present_queued) {
            if (!(this_present_queued && that_present_queued))
                return false;
            if (this.queued != that.queued)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public int compareTo(Compacting other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        Compacting typedOther = (Compacting) other;
        lastComparison = Boolean.valueOf(isSetRunning()).compareTo(typedOther.isSetRunning());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetRunning()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.running, typedOther.running);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetQueued()).compareTo(typedOther.isSetQueued());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetQueued()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.queued, typedOther.queued);
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
        StringBuilder sb = new StringBuilder("Compacting(");
        boolean first = true;
        sb.append("running:");
        sb.append(this.running);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("queued:");
        sb.append(this.queued);
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

    private static class CompactingStandardSchemeFactory implements SchemeFactory {

        public CompactingStandardScheme getScheme() {
            return new CompactingStandardScheme();
        }
    }

    private static class CompactingStandardScheme extends StandardScheme<Compacting> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, Compacting struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch(schemeField.id) {
                    case 1:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.running = iprot.readI32();
                            struct.setRunningIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.queued = iprot.readI32();
                            struct.setQueuedIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, Compacting struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            oprot.writeFieldBegin(RUNNING_FIELD_DESC);
            oprot.writeI32(struct.running);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(QUEUED_FIELD_DESC);
            oprot.writeI32(struct.queued);
            oprot.writeFieldEnd();
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class CompactingTupleSchemeFactory implements SchemeFactory {

        public CompactingTupleScheme getScheme() {
            return new CompactingTupleScheme();
        }
    }

    private static class CompactingTupleScheme extends TupleScheme<Compacting> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, Compacting struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetRunning()) {
                optionals.set(0);
            }
            if (struct.isSetQueued()) {
                optionals.set(1);
            }
            oprot.writeBitSet(optionals, 2);
            if (struct.isSetRunning()) {
                oprot.writeI32(struct.running);
            }
            if (struct.isSetQueued()) {
                oprot.writeI32(struct.queued);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, Compacting struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(2);
            if (incoming.get(0)) {
                struct.running = iprot.readI32();
                struct.setRunningIsSet(true);
            }
            if (incoming.get(1)) {
                struct.queued = iprot.readI32();
                struct.setQueuedIsSet(true);
            }
        }
    }
}
