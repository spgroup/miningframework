package org.apache.accumulo.core.master.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class Compacting implements org.apache.thrift.TBase<Compacting, Compacting._Fields>, java.io.Serializable, Cloneable, Comparable<Compacting> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Compacting");

    private static final org.apache.thrift.protocol.TField RUNNING_FIELD_DESC = new org.apache.thrift.protocol.TField("running", org.apache.thrift.protocol.TType.I32, (short) 1);

    private static final org.apache.thrift.protocol.TField QUEUED_FIELD_DESC = new org.apache.thrift.protocol.TField("queued", org.apache.thrift.protocol.TType.I32, (short) 2);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new CompactingStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new CompactingTupleSchemeFactory();

    static {
        schemes.put(StandardScheme.class, new CompactingStandardSchemeFactory());
        schemes.put(TupleScheme.class, new CompactingTupleSchemeFactory());
    }

    public int running;

    public int queued;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        RUNNING((short) 1, "running"), QUEUED((short) 2, "queued");

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

    private static final int __RUNNING_ISSET_ID = 0;

    private static final int __QUEUED_ISSET_ID = 1;

    private byte __isset_bitfield = 0;

    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.RUNNING, new org.apache.thrift.meta_data.FieldMetaData("running", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.QUEUED, new org.apache.thrift.meta_data.FieldMetaData("queued", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
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
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __RUNNING_ISSET_ID);
    }

    public boolean isSetRunning() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __RUNNING_ISSET_ID);
    }

    public void setRunningIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __RUNNING_ISSET_ID, value);
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
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __QUEUED_ISSET_ID);
    }

    public boolean isSetQueued() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __QUEUED_ISSET_ID);
    }

    public void setQueuedIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __QUEUED_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case RUNNING:
                if (value == null) {
                    unsetRunning();
                } else {
                    setRunning((java.lang.Integer) value);
                }
                break;
            case QUEUED:
                if (value == null) {
                    unsetQueued();
                } else {
                    setQueued((java.lang.Integer) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case RUNNING:
                return getRunning();
            case QUEUED:
                return getQueued();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }
        switch(field) {
            case RUNNING:
                return isSetRunning();
            case QUEUED:
                return isSetQueued();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof Compacting)
            return this.equals((Compacting) that);
        return false;
    }

    public boolean equals(Compacting that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
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
        int hashCode = 1;
        hashCode = hashCode * 8191 + running;
        hashCode = hashCode * 8191 + queued;
        return hashCode;
    }

    @Override
    public int compareTo(Compacting other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetRunning()).compareTo(other.isSetRunning());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetRunning()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.running, other.running);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetQueued()).compareTo(other.isSetQueued());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetQueued()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.queued, other.queued);
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("Compacting(");
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

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
        try {
            __isset_bitfield = 0;
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class CompactingStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public CompactingStandardScheme getScheme() {
            return new CompactingStandardScheme();
        }
    }

    private static class CompactingStandardScheme extends org.apache.thrift.scheme.StandardScheme<Compacting> {

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

    private static class CompactingTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public CompactingTupleScheme getScheme() {
            return new CompactingTupleScheme();
        }
    }

    private static class CompactingTupleScheme extends org.apache.thrift.scheme.TupleScheme<Compacting> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, Compacting struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
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
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(2);
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

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}