package org.apache.accumulo.proxy.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class ConditionalWriterOptions implements org.apache.thrift.TBase<ConditionalWriterOptions, ConditionalWriterOptions._Fields>, java.io.Serializable, Cloneable, Comparable<ConditionalWriterOptions> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ConditionalWriterOptions");

    private static final org.apache.thrift.protocol.TField MAX_MEMORY_FIELD_DESC = new org.apache.thrift.protocol.TField("maxMemory", org.apache.thrift.protocol.TType.I64, (short) 1);

    private static final org.apache.thrift.protocol.TField TIMEOUT_MS_FIELD_DESC = new org.apache.thrift.protocol.TField("timeoutMs", org.apache.thrift.protocol.TType.I64, (short) 2);

    private static final org.apache.thrift.protocol.TField THREADS_FIELD_DESC = new org.apache.thrift.protocol.TField("threads", org.apache.thrift.protocol.TType.I32, (short) 3);

    private static final org.apache.thrift.protocol.TField AUTHORIZATIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("authorizations", org.apache.thrift.protocol.TType.SET, (short) 4);

    private static final org.apache.thrift.protocol.TField DURABILITY_FIELD_DESC = new org.apache.thrift.protocol.TField("durability", org.apache.thrift.protocol.TType.I32, (short) 5);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new ConditionalWriterOptionsStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new ConditionalWriterOptionsTupleSchemeFactory();

    public long maxMemory;

    public long timeoutMs;

    public int threads;

    @org.apache.thrift.annotation.Nullable
    public java.util.Set<java.nio.ByteBuffer> authorizations;

    @org.apache.thrift.annotation.Nullable
    public Durability durability;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        MAX_MEMORY((short) 1, "maxMemory"), TIMEOUT_MS((short) 2, "timeoutMs"), THREADS((short) 3, "threads"), AUTHORIZATIONS((short) 4, "authorizations"), DURABILITY((short) 5, "durability");

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

    private static final int __TIMEOUTMS_ISSET_ID = 1;

    private static final int __THREADS_ISSET_ID = 2;

    private byte __isset_bitfield = 0;

    private static final _Fields[] optionals = { _Fields.MAX_MEMORY, _Fields.TIMEOUT_MS, _Fields.THREADS, _Fields.AUTHORIZATIONS, _Fields.DURABILITY };

    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.MAX_MEMORY, new org.apache.thrift.meta_data.FieldMetaData("maxMemory", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.TIMEOUT_MS, new org.apache.thrift.meta_data.FieldMetaData("timeoutMs", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.THREADS, new org.apache.thrift.meta_data.FieldMetaData("threads", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.AUTHORIZATIONS, new org.apache.thrift.meta_data.FieldMetaData("authorizations", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.SetMetaData(org.apache.thrift.protocol.TType.SET, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true))));
        tmpMap.put(_Fields.DURABILITY, new org.apache.thrift.meta_data.FieldMetaData("durability", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, Durability.class)));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
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
            java.util.Set<java.nio.ByteBuffer> __this__authorizations = new java.util.HashSet<java.nio.ByteBuffer>(other.authorizations);
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
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __MAXMEMORY_ISSET_ID);
    }

    public boolean isSetMaxMemory() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __MAXMEMORY_ISSET_ID);
    }

    public void setMaxMemoryIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __MAXMEMORY_ISSET_ID, value);
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

    public ConditionalWriterOptions setThreads(int threads) {
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

    public int getAuthorizationsSize() {
        return (this.authorizations == null) ? 0 : this.authorizations.size();
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.Iterator<java.nio.ByteBuffer> getAuthorizationsIterator() {
        return (this.authorizations == null) ? null : this.authorizations.iterator();
    }

    public void addToAuthorizations(java.nio.ByteBuffer elem) {
        if (this.authorizations == null) {
            this.authorizations = new java.util.HashSet<java.nio.ByteBuffer>();
        }
        this.authorizations.add(elem);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.Set<java.nio.ByteBuffer> getAuthorizations() {
        return this.authorizations;
    }

    public ConditionalWriterOptions setAuthorizations(@org.apache.thrift.annotation.Nullable java.util.Set<java.nio.ByteBuffer> authorizations) {
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

    @org.apache.thrift.annotation.Nullable
    public Durability getDurability() {
        return this.durability;
    }

    public ConditionalWriterOptions setDurability(@org.apache.thrift.annotation.Nullable Durability durability) {
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
            case AUTHORIZATIONS:
                if (value == null) {
                    unsetAuthorizations();
                } else {
                    setAuthorizations((java.util.Set<java.nio.ByteBuffer>) value);
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
            case TIMEOUT_MS:
                return getTimeoutMs();
            case THREADS:
                return getThreads();
            case AUTHORIZATIONS:
                return getAuthorizations();
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
            case TIMEOUT_MS:
                return isSetTimeoutMs();
            case THREADS:
                return isSetThreads();
            case AUTHORIZATIONS:
                return isSetAuthorizations();
            case DURABILITY:
                return isSetDurability();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof ConditionalWriterOptions)
            return this.equals((ConditionalWriterOptions) that);
        return false;
    }

    public boolean equals(ConditionalWriterOptions that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
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
        int hashCode = 1;
        hashCode = hashCode * 8191 + ((isSetMaxMemory()) ? 131071 : 524287);
        if (isSetMaxMemory())
            hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(maxMemory);
        hashCode = hashCode * 8191 + ((isSetTimeoutMs()) ? 131071 : 524287);
        if (isSetTimeoutMs())
            hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(timeoutMs);
        hashCode = hashCode * 8191 + ((isSetThreads()) ? 131071 : 524287);
        if (isSetThreads())
            hashCode = hashCode * 8191 + threads;
        hashCode = hashCode * 8191 + ((isSetAuthorizations()) ? 131071 : 524287);
        if (isSetAuthorizations())
            hashCode = hashCode * 8191 + authorizations.hashCode();
        hashCode = hashCode * 8191 + ((isSetDurability()) ? 131071 : 524287);
        if (isSetDurability())
            hashCode = hashCode * 8191 + durability.getValue();
        return hashCode;
    }

    @Override
    public int compareTo(ConditionalWriterOptions other) {
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
        lastComparison = java.lang.Boolean.valueOf(isSetAuthorizations()).compareTo(other.isSetAuthorizations());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetAuthorizations()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.authorizations, other.authorizations);
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("ConditionalWriterOptions(");
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
                org.apache.thrift.TBaseHelper.toString(this.authorizations, sb);
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

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
        try {
            __isset_bitfield = 0;
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class ConditionalWriterOptionsStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public ConditionalWriterOptionsStandardScheme getScheme() {
            return new ConditionalWriterOptionsStandardScheme();
        }
    }

    private static class ConditionalWriterOptionsStandardScheme extends org.apache.thrift.scheme.StandardScheme<ConditionalWriterOptions> {

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
                                struct.authorizations = new java.util.HashSet<java.nio.ByteBuffer>(2 * _set106.size);
                                @org.apache.thrift.annotation.Nullable
                                java.nio.ByteBuffer _elem107;
                                for (int _i108 = 0; _i108 < _set106.size; ++_i108) {
                                    _elem107 = iprot.readBinary();
                                    struct.authorizations.add(_elem107);
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
                        for (java.nio.ByteBuffer _iter109 : struct.authorizations) {
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

    private static class ConditionalWriterOptionsTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public ConditionalWriterOptionsTupleScheme getScheme() {
            return new ConditionalWriterOptionsTupleScheme();
        }
    }

    private static class ConditionalWriterOptionsTupleScheme extends org.apache.thrift.scheme.TupleScheme<ConditionalWriterOptions> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, ConditionalWriterOptions struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
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
                    for (java.nio.ByteBuffer _iter110 : struct.authorizations) {
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
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(5);
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
                    struct.authorizations = new java.util.HashSet<java.nio.ByteBuffer>(2 * _set111.size);
                    @org.apache.thrift.annotation.Nullable
                    java.nio.ByteBuffer _elem112;
                    for (int _i113 = 0; _i113 < _set111.size; ++_i113) {
                        _elem112 = iprot.readBinary();
                        struct.authorizations.add(_elem112);
                    }
                }
                struct.setAuthorizationsIsSet(true);
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
