package org.apache.accumulo.proxy.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class WriterOptions implements org.apache.thrift.TBase<WriterOptions, WriterOptions._Fields>, java.io.Serializable, Cloneable, Comparable<WriterOptions> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("WriterOptions");

    private static final org.apache.thrift.protocol.TField MAX_MEMORY_FIELD_DESC = new org.apache.thrift.protocol.TField("maxMemory", org.apache.thrift.protocol.TType.I64, (short) 1);

    private static final org.apache.thrift.protocol.TField LATENCY_MS_FIELD_DESC = new org.apache.thrift.protocol.TField("latencyMs", org.apache.thrift.protocol.TType.I64, (short) 2);

    private static final org.apache.thrift.protocol.TField TIMEOUT_MS_FIELD_DESC = new org.apache.thrift.protocol.TField("timeoutMs", org.apache.thrift.protocol.TType.I64, (short) 3);

    private static final org.apache.thrift.protocol.TField THREADS_FIELD_DESC = new org.apache.thrift.protocol.TField("threads", org.apache.thrift.protocol.TType.I32, (short) 4);

    private static final org.apache.thrift.protocol.TField DURABILITY_FIELD_DESC = new org.apache.thrift.protocol.TField("durability", org.apache.thrift.protocol.TType.I32, (short) 5);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new WriterOptionsStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new WriterOptionsTupleSchemeFactory();

    public long maxMemory;

    public long latencyMs;

    public long timeoutMs;

    public int threads;

    @org.apache.thrift.annotation.Nullable
public Durability durability;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        MAX_MEMORY((short) 1, "maxMemory"), LATENCY_MS((short) 2, "latencyMs"), TIMEOUT_MS((short) 3, "timeoutMs"), THREADS((short) 4, "threads"), DURABILITY((short) 5, "durability");

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
                    return MAX_MEMORY;
                case 2:
                    return LATENCY_MS;
                case 3:
                    return TIMEOUT_MS;
                case 4:
                    return THREADS;
                case 5:
                    return DURABILITY;
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

    private static final int __MAXMEMORY_ISSET_ID = 0;

    private static final int __LATENCYMS_ISSET_ID = 1;

    private static final int __TIMEOUTMS_ISSET_ID = 2;

    private static final int __THREADS_ISSET_ID = 3;

    private byte __isset_bitfield = 0;

    private static final _Fields[] optionals = { _Fields.DURABILITY };

    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.MAX_MEMORY, new org.apache.thrift.meta_data.FieldMetaData("maxMemory", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.LATENCY_MS, new org.apache.thrift.meta_data.FieldMetaData("latencyMs", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.TIMEOUT_MS, new org.apache.thrift.meta_data.FieldMetaData("timeoutMs", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.THREADS, new org.apache.thrift.meta_data.FieldMetaData("threads", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.DURABILITY, new org.apache.thrift.meta_data.FieldMetaData("durability", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, Durability.class)));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(WriterOptions.class, metaDataMap);
    }

    public WriterOptions() {
    }

    public WriterOptions(long maxMemory, long latencyMs, long timeoutMs, int threads) {
        this();
        this.maxMemory = maxMemory;
        setMaxMemoryIsSet(true);
        this.latencyMs = latencyMs;
        setLatencyMsIsSet(true);
        this.timeoutMs = timeoutMs;
        setTimeoutMsIsSet(true);
        this.threads = threads;
        setThreadsIsSet(true);
    }

    public WriterOptions(WriterOptions other) {
        __isset_bitfield = other.__isset_bitfield;
        this.maxMemory = other.maxMemory;
        this.latencyMs = other.latencyMs;
        this.timeoutMs = other.timeoutMs;
        this.threads = other.threads;
        if (other.isSetDurability()) {
            this.durability = other.durability;
        }
    }

    public WriterOptions deepCopy() {
        return new WriterOptions(this);
    }

    @Override
    public void clear() {
        setMaxMemoryIsSet(false);
        this.maxMemory = 0;
        setLatencyMsIsSet(false);
        this.latencyMs = 0;
        setTimeoutMsIsSet(false);
        this.timeoutMs = 0;
        setThreadsIsSet(false);
        this.threads = 0;
        this.durability = null;
    }

    public long getMaxMemory() {
        return this.maxMemory;
    }

    public WriterOptions setMaxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
        setMaxMemoryIsSet(true);
        return this;
    }

    public void unsetMaxMemory() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __MAXMEMORY_ISSET_ID);
    }

    public boolean isSetMaxMemory() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __MAXMEMORY_ISSET_ID);
    }

    public void setMaxMemoryIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __MAXMEMORY_ISSET_ID, value);
    }

    public long getLatencyMs() {
        return this.latencyMs;
    }

    public WriterOptions setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
        setLatencyMsIsSet(true);
        return this;
    }

    public void unsetLatencyMs() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __LATENCYMS_ISSET_ID);
    }

    public boolean isSetLatencyMs() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __LATENCYMS_ISSET_ID);
    }

    public void setLatencyMsIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __LATENCYMS_ISSET_ID, value);
    }

    public long getTimeoutMs() {
        return this.timeoutMs;
    }

    public WriterOptions setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
        setTimeoutMsIsSet(true);
        return this;
    }

    public void unsetTimeoutMs() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __TIMEOUTMS_ISSET_ID);
    }

    public boolean isSetTimeoutMs() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __TIMEOUTMS_ISSET_ID);
    }

    public void setTimeoutMsIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __TIMEOUTMS_ISSET_ID, value);
    }

    public int getThreads() {
        return this.threads;
    }

    public WriterOptions setThreads(int threads) {
        this.threads = threads;
        setThreadsIsSet(true);
        return this;
    }

    public void unsetThreads() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __THREADS_ISSET_ID);
    }

    public boolean isSetThreads() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __THREADS_ISSET_ID);
    }

    public void setThreadsIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __THREADS_ISSET_ID, value);
    }

    @org.apache.thrift.annotation.Nullable
public Durability getDurability() {
        return this.durability;
    }

    public WriterOptions setDurability(@org.apache.thrift.annotation.Nullable Durability durability) {
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

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case MAX_MEMORY:
                if (value == null) {
                    unsetMaxMemory();
                } else {
                    setMaxMemory((java.lang.Long) value);
                }
                break;
            case LATENCY_MS:
                if (value == null) {
                    unsetLatencyMs();
                } else {
                    setLatencyMs((java.lang.Long) value);
                }
                break;
            case TIMEOUT_MS:
                if (value == null) {
                    unsetTimeoutMs();
                } else {
                    setTimeoutMs((java.lang.Long) value);
                }
                break;
            case THREADS:
                if (value == null) {
                    unsetThreads();
                } else {
                    setThreads((java.lang.Integer) value);
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

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case MAX_MEMORY:
                return getMaxMemory();
            case LATENCY_MS:
                return getLatencyMs();
            case TIMEOUT_MS:
                return getTimeoutMs();
            case THREADS:
                return getThreads();
            case DURABILITY:
                return getDurability();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }
        switch(field) {
            case MAX_MEMORY:
                return isSetMaxMemory();
            case LATENCY_MS:
                return isSetLatencyMs();
            case TIMEOUT_MS:
                return isSetTimeoutMs();
            case THREADS:
                return isSetThreads();
            case DURABILITY:
                return isSetDurability();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof WriterOptions)
            return this.equals((WriterOptions) that);
        return false;
    }

    public boolean equals(WriterOptions that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
        boolean this_present_maxMemory = true;
        boolean that_present_maxMemory = true;
        if (this_present_maxMemory || that_present_maxMemory) {
            if (!(this_present_maxMemory && that_present_maxMemory))
                return false;
            if (this.maxMemory != that.maxMemory)
                return false;
        }
        boolean this_present_latencyMs = true;
        boolean that_present_latencyMs = true;
        if (this_present_latencyMs || that_present_latencyMs) {
            if (!(this_present_latencyMs && that_present_latencyMs))
                return false;
            if (this.latencyMs != that.latencyMs)
                return false;
        }
        boolean this_present_timeoutMs = true;
        boolean that_present_timeoutMs = true;
        if (this_present_timeoutMs || that_present_timeoutMs) {
            if (!(this_present_timeoutMs && that_present_timeoutMs))
                return false;
            if (this.timeoutMs != that.timeoutMs)
                return false;
        }
        boolean this_present_threads = true;
        boolean that_present_threads = true;
        if (this_present_threads || that_present_threads) {
            if (!(this_present_threads && that_present_threads))
                return false;
            if (this.threads != that.threads)
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
        int hashCode = 1;
        hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(maxMemory);
        hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(latencyMs);
        hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(timeoutMs);
        hashCode = hashCode * 8191 + threads;
        hashCode = hashCode * 8191 + ((isSetDurability()) ? 131071 : 524287);
        if (isSetDurability())
            hashCode = hashCode * 8191 + durability.getValue();
        return hashCode;
    }

    @Override
    public int compareTo(WriterOptions other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetMaxMemory()).compareTo(other.isSetMaxMemory());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetMaxMemory()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.maxMemory, other.maxMemory);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetLatencyMs()).compareTo(other.isSetLatencyMs());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetLatencyMs()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.latencyMs, other.latencyMs);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetTimeoutMs()).compareTo(other.isSetTimeoutMs());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTimeoutMs()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.timeoutMs, other.timeoutMs);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetThreads()).compareTo(other.isSetThreads());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetThreads()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.threads, other.threads);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetDurability()).compareTo(other.isSetDurability());
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("WriterOptions(");
        boolean first = true;
        sb.append("maxMemory:");
        sb.append(this.maxMemory);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("latencyMs:");
        sb.append(this.latencyMs);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("timeoutMs:");
        sb.append(this.timeoutMs);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("threads:");
        sb.append(this.threads);
        first = false;
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

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
        try {
            __isset_bitfield = 0;
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class WriterOptionsStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public WriterOptionsStandardScheme getScheme() {
            return new WriterOptionsStandardScheme();
        }
    }

    private static class WriterOptionsStandardScheme extends org.apache.thrift.scheme.StandardScheme<WriterOptions> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, WriterOptions struct) throws org.apache.thrift.TException {
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
                            struct.latencyMs = iprot.readI64();
                            struct.setLatencyMsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.timeoutMs = iprot.readI64();
                            struct.setTimeoutMsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.threads = iprot.readI32();
                            struct.setThreadsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 5:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.durability = org.apache.accumulo.proxy.thrift.Durability.findByValue(iprot.readI32());
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, WriterOptions struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            oprot.writeFieldBegin(MAX_MEMORY_FIELD_DESC);
            oprot.writeI64(struct.maxMemory);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(LATENCY_MS_FIELD_DESC);
            oprot.writeI64(struct.latencyMs);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(TIMEOUT_MS_FIELD_DESC);
            oprot.writeI64(struct.timeoutMs);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(THREADS_FIELD_DESC);
            oprot.writeI32(struct.threads);
            oprot.writeFieldEnd();
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

    private static class WriterOptionsTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public WriterOptionsTupleScheme getScheme() {
            return new WriterOptionsTupleScheme();
        }
    }

    private static class WriterOptionsTupleScheme extends org.apache.thrift.scheme.TupleScheme<WriterOptions> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, WriterOptions struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
            if (struct.isSetMaxMemory()) {
                optionals.set(0);
            }
            if (struct.isSetLatencyMs()) {
                optionals.set(1);
            }
            if (struct.isSetTimeoutMs()) {
                optionals.set(2);
            }
            if (struct.isSetThreads()) {
                optionals.set(3);
            }
            if (struct.isSetDurability()) {
                optionals.set(4);
            }
            oprot.writeBitSet(optionals, 5);
            if (struct.isSetMaxMemory()) {
                oprot.writeI64(struct.maxMemory);
            }
            if (struct.isSetLatencyMs()) {
                oprot.writeI64(struct.latencyMs);
            }
            if (struct.isSetTimeoutMs()) {
                oprot.writeI64(struct.timeoutMs);
            }
            if (struct.isSetThreads()) {
                oprot.writeI32(struct.threads);
            }
            if (struct.isSetDurability()) {
                oprot.writeI32(struct.durability.getValue());
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, WriterOptions struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(5);
            if (incoming.get(0)) {
                struct.maxMemory = iprot.readI64();
                struct.setMaxMemoryIsSet(true);
            }
            if (incoming.get(1)) {
                struct.latencyMs = iprot.readI64();
                struct.setLatencyMsIsSet(true);
            }
            if (incoming.get(2)) {
                struct.timeoutMs = iprot.readI64();
                struct.setTimeoutMsIsSet(true);
            }
            if (incoming.get(3)) {
                struct.threads = iprot.readI32();
                struct.setThreadsIsSet(true);
            }
            if (incoming.get(4)) {
                struct.durability = org.apache.accumulo.proxy.thrift.Durability.findByValue(iprot.readI32());
                struct.setDurabilityIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}