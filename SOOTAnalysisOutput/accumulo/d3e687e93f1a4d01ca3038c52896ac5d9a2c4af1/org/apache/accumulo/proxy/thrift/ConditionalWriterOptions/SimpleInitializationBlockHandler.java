package org.apache.accumulo.proxy.thrift;

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
public class ConditionalWriterOptions implements org.apache.thrift.TBase<ConditionalWriterOptions, ConditionalWriterOptions._Fields>, java.io.Serializable, Cloneable, Comparable<ConditionalWriterOptions> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ConditionalWriterOptions");

    private static final org.apache.thrift.protocol.TField MAX_MEMORY_FIELD_DESC = new org.apache.thrift.protocol.TField("maxMemory", org.apache.thrift.protocol.TType.I64, (short) 1);

    private static final org.apache.thrift.protocol.TField TIMEOUT_MS_FIELD_DESC = new org.apache.thrift.protocol.TField("timeoutMs", org.apache.thrift.protocol.TType.I64, (short) 2);

    private static final org.apache.thrift.protocol.TField THREADS_FIELD_DESC = new org.apache.thrift.protocol.TField("threads", org.apache.thrift.protocol.TType.I32, (short) 3);

    private static final org.apache.thrift.protocol.TField AUTHORIZATIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("authorizations", org.apache.thrift.protocol.TType.SET, (short) 4);

    private static final org.apache.thrift.protocol.TField DURABILITY_FIELD_DESC = new org.apache.thrift.protocol.TField("durability", org.apache.thrift.protocol.TType.I32, (short) 5);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new ConditionalWriterOptionsStandardSchemeFactory());
        schemes.put(TupleScheme.class, new ConditionalWriterOptionsTupleSchemeFactory());
    }

    public long maxMemory;

    public long timeoutMs;

    public int threads;

    public Set<ByteBuffer> authorizations;

    public Durability durability;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        MAX_MEMORY((short) 1, "maxMemory"), TIMEOUT_MS((short) 2, "timeoutMs"), THREADS((short) 3, "threads"), AUTHORIZATIONS((short) 4, "authorizations"), DURABILITY((short) 5, "durability");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return MAX_MEMORY;
                case 2:
                    return TIMEOUT_MS;
                case 3:
                    return THREADS;
                case 4:
                    return AUTHORIZATIONS;
                case 5:
                    return DURABILITY;
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

    private static final int __MAXMEMORY_ISSET_ID = 0;

    private static final int __TIMEOUTMS_ISSET_ID = 1;

    private static final int __THREADS_ISSET_ID = 2;

    private byte __isset_bitfield = 0;

    private _Fields[] optionals = { _Fields.MAX_MEMORY, _Fields.TIMEOUT_MS, _Fields.THREADS, _Fields.AUTHORIZATIONS, _Fields.DURABILITY };

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.MAX_MEMORY, new org.apache.thrift.meta_data.FieldMetaData("maxMemory", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.TIMEOUT_MS, new org.apache.thrift.meta_data.FieldMetaData("timeoutMs", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.THREADS, new org.apache.thrift.meta_data.FieldMetaData("threads", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.AUTHORIZATIONS, new org.apache.thrift.meta_data.FieldMetaData("authorizations", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.SetMetaData(org.apache.thrift.protocol.TType.SET, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true))));
        tmpMap.put(_Fields.DURABILITY, new org.apache.thrift.meta_data.FieldMetaData("durability", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, Durability.class)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ConditionalWriterOptions.class, metaDataMap);
    }

    public ConditionalWriterOptions() {
    }

    public ConditionalWriterOptions(ConditionalWriterOptions other) {
        __isset_bitfield = other.__isset_bitfield;
        this.maxMemory = other.maxMemory;
        this.timeoutMs = other.timeoutMs;
        this.threads = other.threads;
        if (other.isSetAuthorizations()) {
            Set<ByteBuffer> __this__authorizations = new HashSet<ByteBuffer>(other.authorizations);
            this.authorizations = __this__authorizations;
        }
        if (other.isSetDurability()) {
            this.durability = other.durability;
        }
    }

    public ConditionalWriterOptions deepCopy() {
        return new ConditionalWriterOptions(this);
    }

    @Override
    public void clear() {
        setMaxMemoryIsSet(false);
        this.maxMemory = 0;
        setTimeoutMsIsSet(false);
        this.timeoutMs = 0;
        setThreadsIsSet(false);
        this.threads = 0;
        this.authorizations = null;
        this.durability = null;
    }

    public long getMaxMemory() {
        return this.maxMemory;
    }

    public ConditionalWriterOptions setMaxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
        setMaxMemoryIsSet(true);
        return this;
    }

    public void unsetMaxMemory() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __MAXMEMORY_ISSET_ID);
    }

    public boolean isSetMaxMemory() {
        return EncodingUtils.testBit(__isset_bitfield, __MAXMEMORY_ISSET_ID);
    }

    public void setMaxMemoryIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __MAXMEMORY_ISSET_ID, value);
    }

    public long getTimeoutMs() {
        return this.timeoutMs;
    }

    public ConditionalWriterOptions setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
        setTimeoutMsIsSet(true);
        return this;
    }

    public void unsetTimeoutMs() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __TIMEOUTMS_ISSET_ID);
    }

    public boolean isSetTimeoutMs() {
        return EncodingUtils.testBit(__isset_bitfield, __TIMEOUTMS_ISSET_ID);
    }

    public void setTimeoutMsIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __TIMEOUTMS_ISSET_ID, value);
    }

    public int getThreads() {
        return this.threads;
    }

    public ConditionalWriterOptions setThreads(int threads) {
        this.threads = threads;
        setThreadsIsSet(true);
        return this;
    }

    public void unsetThreads() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __THREADS_ISSET_ID);
    }

    public boolean isSetThreads() {
        return EncodingUtils.testBit(__isset_bitfield, __THREADS_ISSET_ID);
    }

    public void setThreadsIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __THREADS_ISSET_ID, value);
    }

    public int getAuthorizationsSize() {
        return (this.authorizations == null) ? 0 : this.authorizations.size();
    }

    public java.util.Iterator<ByteBuffer> getAuthorizationsIterator() {
        return (this.authorizations == null) ? null : this.authorizations.iterator();
    }

    public void addToAuthorizations(ByteBuffer elem) {
        if (this.authorizations == null) {
            this.authorizations = new HashSet<ByteBuffer>();
        }
        this.authorizations.add(elem);
    }

    public Set<ByteBuffer> getAuthorizations() {
        return this.authorizations;
    }

    public ConditionalWriterOptions setAuthorizations(Set<ByteBuffer> authorizations) {
        this.authorizations = authorizations;
        return this;
    }

    public void unsetAuthorizations() {
        this.authorizations = null;
    }

    public boolean isSetAuthorizations() {
        return this.authorizations != null;
    }

    public void setAuthorizationsIsSet(boolean value) {
        if (!value) {
            this.authorizations = null;
        }
    }

    public Durability getDurability() {
        return this.durability;
    }

    public ConditionalWriterOptions setDurability(Durability durability) {
        this.durability = durability;
        return this;
    }

    public void unsetDurability() {
        this.durability = null;
    }

    public boolean isSetDurability() {
        return this.durability != null;
    }

    public void setDurabilityIsSet(boolean value) {
        if (!value) {
            this.durability = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case MAX_MEMORY:
                if (value == null) {
                    unsetMaxMemory();
                } else {
                    setMaxMemory((Long) value);
                }
                break;
            case TIMEOUT_MS:
                if (value == null) {
                    unsetTimeoutMs();
                } else {
                    setTimeoutMs((Long) value);
                }
                break;
            case THREADS:
                if (value == null) {
                    unsetThreads();
                } else {
                    setThreads((Integer) value);
                }
                break;
            case AUTHORIZATIONS:
                if (value == null) {
                    unsetAuthorizations();
                } else {
                    setAuthorizations((Set<ByteBuffer>) value);
                }
                break;
            case DURABILITY:
                if (value == null) {
                    unsetDurability();
                } else {
                    setDurability((Durability) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case MAX_MEMORY:
                return Long.valueOf(getMaxMemory());
            case TIMEOUT_MS:
                return Long.valueOf(getTimeoutMs());
            case THREADS:
                return Integer.valueOf(getThreads());
            case AUTHORIZATIONS:
                return getAuthorizations();
            case DURABILITY:
                return getDurability();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case MAX_MEMORY:
                return isSetMaxMemory();
            case TIMEOUT_MS:
                return isSetTimeoutMs();
            case THREADS:
                return isSetThreads();
            case AUTHORIZATIONS:
                return isSetAuthorizations();
            case DURABILITY:
                return isSetDurability();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof ConditionalWriterOptions)
            return this.equals((ConditionalWriterOptions) that);
        return false;
    }

    public boolean equals(ConditionalWriterOptions that) {
        if (that == null)
            return false;
        boolean this_present_maxMemory = true && this.isSetMaxMemory();
        boolean that_present_maxMemory = true && that.isSetMaxMemory();
        if (this_present_maxMemory || that_present_maxMemory) {
            if (!(this_present_maxMemory && that_present_maxMemory))
                return false;
            if (this.maxMemory != that.maxMemory)
                return false;
        }
        boolean this_present_timeoutMs = true && this.isSetTimeoutMs();
        boolean that_present_timeoutMs = true && that.isSetTimeoutMs();
        if (this_present_timeoutMs || that_present_timeoutMs) {
            if (!(this_present_timeoutMs && that_present_timeoutMs))
                return false;
            if (this.timeoutMs != that.timeoutMs)
                return false;
        }
        boolean this_present_threads = true && this.isSetThreads();
        boolean that_present_threads = true && that.isSetThreads();
        if (this_present_threads || that_present_threads) {
            if (!(this_present_threads && that_present_threads))
                return false;
            if (this.threads != that.threads)
                return false;
        }
        boolean this_present_authorizations = true && this.isSetAuthorizations();
        boolean that_present_authorizations = true && that.isSetAuthorizations();
        if (this_present_authorizations || that_present_authorizations) {
            if (!(this_present_authorizations && that_present_authorizations))
                return false;
            if (!this.authorizations.equals(that.authorizations))
                return false;
        }
        boolean this_present_durability = true && this.isSetDurability();
        boolean that_present_durability = true && that.isSetDurability();
        if (this_present_durability || that_present_durability) {
            if (!(this_present_durability && that_present_durability))
                return false;
            if (!this.durability.equals(that.durability))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int compareTo(ConditionalWriterOptions other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetMaxMemory()).compareTo(other.isSetMaxMemory());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetMaxMemory()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.maxMemory, other.maxMemory);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetTimeoutMs()).compareTo(other.isSetTimeoutMs());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTimeoutMs()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.timeoutMs, other.timeoutMs);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetThreads()).compareTo(other.isSetThreads());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetThreads()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.threads, other.threads);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetAuthorizations()).compareTo(other.isSetAuthorizations());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetAuthorizations()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.authorizations, other.authorizations);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetDurability()).compareTo(other.isSetDurability());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetDurability()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.durability, other.durability);
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
        StringBuilder sb = new StringBuilder("ConditionalWriterOptions(");
        boolean first = true;
        if (isSetMaxMemory()) {
            sb.append("maxMemory:");
            sb.append(this.maxMemory);
            first = false;
        }
        if (isSetTimeoutMs()) {
            if (!first)
                sb.append(", ");
            sb.append("timeoutMs:");
            sb.append(this.timeoutMs);
            first = false;
        }
        if (isSetThreads()) {
            if (!first)
                sb.append(", ");
            sb.append("threads:");
            sb.append(this.threads);
            first = false;
        }
        if (isSetAuthorizations()) {
            if (!first)
                sb.append(", ");
            sb.append("authorizations:");
            if (this.authorizations == null) {
                sb.append("null");
            } else {
                sb.append(this.authorizations);
            }
            first = false;
        }
        if (isSetDurability()) {
            if (!first)
                sb.append(", ");
            sb.append("durability:");
            if (this.durability == null) {
                sb.append("null");
            } else {
                sb.append(this.durability);
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

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        try {
            __isset_bitfield = 0;
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class ConditionalWriterOptionsStandardSchemeFactory implements SchemeFactory {

        public ConditionalWriterOptionsStandardScheme getScheme() {
            return new ConditionalWriterOptionsStandardScheme();
        }
    }

    private static class ConditionalWriterOptionsStandardScheme extends StandardScheme<ConditionalWriterOptions> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, ConditionalWriterOptions struct) throws org.apache.thrift.TException {
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
                            struct.maxMemory = iprot.readI64();
                            struct.setMaxMemoryIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.timeoutMs = iprot.readI64();
                            struct.setTimeoutMsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.threads = iprot.readI32();
                            struct.setThreadsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.SET) {
                            {
                                org.apache.thrift.protocol.TSet _set106 = iprot.readSetBegin();
                                struct.authorizations = new HashSet<ByteBuffer>(2 * _set106.size);
                                for (int _i107 = 0; _i107 < _set106.size; ++_i107) {
                                    ByteBuffer _elem108;
                                    _elem108 = iprot.readBinary();
                                    struct.authorizations.add(_elem108);
                                }
                                iprot.readSetEnd();
                            }
                            struct.setAuthorizationsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 5:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.durability = Durability.findByValue(iprot.readI32());
                            struct.setDurabilityIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, ConditionalWriterOptions struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.isSetMaxMemory()) {
                oprot.writeFieldBegin(MAX_MEMORY_FIELD_DESC);
                oprot.writeI64(struct.maxMemory);
                oprot.writeFieldEnd();
            }
            if (struct.isSetTimeoutMs()) {
                oprot.writeFieldBegin(TIMEOUT_MS_FIELD_DESC);
                oprot.writeI64(struct.timeoutMs);
                oprot.writeFieldEnd();
            }
            if (struct.isSetThreads()) {
                oprot.writeFieldBegin(THREADS_FIELD_DESC);
                oprot.writeI32(struct.threads);
                oprot.writeFieldEnd();
            }
            if (struct.authorizations != null) {
                if (struct.isSetAuthorizations()) {
                    oprot.writeFieldBegin(AUTHORIZATIONS_FIELD_DESC);
                    {
                        oprot.writeSetBegin(new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.STRING, struct.authorizations.size()));
                        for (ByteBuffer _iter109 : struct.authorizations) {
                            oprot.writeBinary(_iter109);
                        }
                        oprot.writeSetEnd();
                    }
                    oprot.writeFieldEnd();
                }
            }
            if (struct.durability != null) {
                if (struct.isSetDurability()) {
                    oprot.writeFieldBegin(DURABILITY_FIELD_DESC);
                    oprot.writeI32(struct.durability.getValue());
                    oprot.writeFieldEnd();
                }
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class ConditionalWriterOptionsTupleSchemeFactory implements SchemeFactory {

        public ConditionalWriterOptionsTupleScheme getScheme() {
            return new ConditionalWriterOptionsTupleScheme();
        }
    }

    private static class ConditionalWriterOptionsTupleScheme extends TupleScheme<ConditionalWriterOptions> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, ConditionalWriterOptions struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetMaxMemory()) {
                optionals.set(0);
            }
            if (struct.isSetTimeoutMs()) {
                optionals.set(1);
            }
            if (struct.isSetThreads()) {
                optionals.set(2);
            }
            if (struct.isSetAuthorizations()) {
                optionals.set(3);
            }
            if (struct.isSetDurability()) {
                optionals.set(4);
            }
            oprot.writeBitSet(optionals, 5);
            if (struct.isSetMaxMemory()) {
                oprot.writeI64(struct.maxMemory);
            }
            if (struct.isSetTimeoutMs()) {
                oprot.writeI64(struct.timeoutMs);
            }
            if (struct.isSetThreads()) {
                oprot.writeI32(struct.threads);
            }
            if (struct.isSetAuthorizations()) {
                {
                    oprot.writeI32(struct.authorizations.size());
                    for (ByteBuffer _iter110 : struct.authorizations) {
                        oprot.writeBinary(_iter110);
                    }
                }
            }
            if (struct.isSetDurability()) {
                oprot.writeI32(struct.durability.getValue());
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, ConditionalWriterOptions struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(5);
            if (incoming.get(0)) {
                struct.maxMemory = iprot.readI64();
                struct.setMaxMemoryIsSet(true);
            }
            if (incoming.get(1)) {
                struct.timeoutMs = iprot.readI64();
                struct.setTimeoutMsIsSet(true);
            }
            if (incoming.get(2)) {
                struct.threads = iprot.readI32();
                struct.setThreadsIsSet(true);
            }
            if (incoming.get(3)) {
                {
                    org.apache.thrift.protocol.TSet _set111 = new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.authorizations = new HashSet<ByteBuffer>(2 * _set111.size);
                    for (int _i112 = 0; _i112 < _set111.size; ++_i112) {
                        ByteBuffer _elem113;
                        _elem113 = iprot.readBinary();
                        struct.authorizations.add(_elem113);
                    }
                }
                struct.setAuthorizationsIsSet(true);
            }
            if (incoming.get(4)) {
                struct.durability = Durability.findByValue(iprot.readI32());
                struct.setDurabilityIsSet(true);
            }
        }
    }
}