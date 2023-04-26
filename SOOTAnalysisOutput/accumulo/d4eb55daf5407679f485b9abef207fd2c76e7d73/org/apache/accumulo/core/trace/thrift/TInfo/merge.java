package org.apache.accumulo.core.trace.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class TInfo implements org.apache.thrift.TBase<TInfo, TInfo._Fields>, java.io.Serializable, Cloneable, Comparable<TInfo> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TInfo");

    private static final org.apache.thrift.protocol.TField TRACE_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("traceId", org.apache.thrift.protocol.TType.I64, (short) 1);

    private static final org.apache.thrift.protocol.TField PARENT_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("parentId", org.apache.thrift.protocol.TType.I64, (short) 2);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new TInfoStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new TInfoTupleSchemeFactory();

    public long traceId;

    public long parentId;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        TRACE_ID((short) 1, "traceId"), PARENT_ID((short) 2, "parentId");

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
                    return TRACE_ID;
                case 2:
                    return PARENT_ID;
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

    private static final int __TRACEID_ISSET_ID = 0;

    private static final int __PARENTID_ISSET_ID = 1;

    private byte __isset_bitfield = 0;

    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.TRACE_ID, new org.apache.thrift.meta_data.FieldMetaData("traceId", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.PARENT_ID, new org.apache.thrift.meta_data.FieldMetaData("parentId", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TInfo.class, metaDataMap);
    }

    public TInfo() {
    }

    public TInfo(long traceId, long parentId) {
        this();
        this.traceId = traceId;
        setTraceIdIsSet(true);
        this.parentId = parentId;
        setParentIdIsSet(true);
    }

    public TInfo(TInfo other) {
        __isset_bitfield = other.__isset_bitfield;
        this.traceId = other.traceId;
        this.parentId = other.parentId;
    }

    public TInfo deepCopy() {
        return new TInfo(this);
    }

    @Override
    public void clear() {
        setTraceIdIsSet(false);
        this.traceId = 0;
        setParentIdIsSet(false);
        this.parentId = 0;
    }

    public long getTraceId() {
        return this.traceId;
    }

    public TInfo setTraceId(long traceId) {
        this.traceId = traceId;
        setTraceIdIsSet(true);
        return this;
    }

    public void unsetTraceId() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __TRACEID_ISSET_ID);
    }

    public boolean isSetTraceId() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __TRACEID_ISSET_ID);
    }

    public void setTraceIdIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __TRACEID_ISSET_ID, value);
    }

    public long getParentId() {
        return this.parentId;
    }

    public TInfo setParentId(long parentId) {
        this.parentId = parentId;
        setParentIdIsSet(true);
        return this;
    }

    public void unsetParentId() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __PARENTID_ISSET_ID);
    }

    public boolean isSetParentId() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __PARENTID_ISSET_ID);
    }

    public void setParentIdIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __PARENTID_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case TRACE_ID:
                if (value == null) {
                    unsetTraceId();
                } else {
                    setTraceId((java.lang.Long) value);
                }
                break;
            case PARENT_ID:
                if (value == null) {
                    unsetParentId();
                } else {
                    setParentId((java.lang.Long) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case TRACE_ID:
                return getTraceId();
            case PARENT_ID:
                return getParentId();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }
        switch(field) {
            case TRACE_ID:
                return isSetTraceId();
            case PARENT_ID:
                return isSetParentId();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof TInfo)
            return this.equals((TInfo) that);
        return false;
    }

    public boolean equals(TInfo that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
        boolean this_present_traceId = true;
        boolean that_present_traceId = true;
        if (this_present_traceId || that_present_traceId) {
            if (!(this_present_traceId && that_present_traceId))
                return false;
            if (this.traceId != that.traceId)
                return false;
        }
        boolean this_present_parentId = true;
        boolean that_present_parentId = true;
        if (this_present_parentId || that_present_parentId) {
            if (!(this_present_parentId && that_present_parentId))
                return false;
            if (this.parentId != that.parentId)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(traceId);
        hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(parentId);
        return hashCode;
    }

    @Override
    public int compareTo(TInfo other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetTraceId()).compareTo(other.isSetTraceId());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTraceId()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.traceId, other.traceId);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetParentId()).compareTo(other.isSetParentId());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetParentId()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.parentId, other.parentId);
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("TInfo(");
        boolean first = true;
        sb.append("traceId:");
        sb.append(this.traceId);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("parentId:");
        sb.append(this.parentId);
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

    private static class TInfoStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public TInfoStandardScheme getScheme() {
            return new TInfoStandardScheme();
        }
    }

    private static class TInfoStandardScheme extends org.apache.thrift.scheme.StandardScheme<TInfo> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, TInfo struct) throws org.apache.thrift.TException {
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
                            struct.traceId = iprot.readI64();
                            struct.setTraceIdIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.parentId = iprot.readI64();
                            struct.setParentIdIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, TInfo struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            oprot.writeFieldBegin(TRACE_ID_FIELD_DESC);
            oprot.writeI64(struct.traceId);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(PARENT_ID_FIELD_DESC);
            oprot.writeI64(struct.parentId);
            oprot.writeFieldEnd();
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class TInfoTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public TInfoTupleScheme getScheme() {
            return new TInfoTupleScheme();
        }
    }

    private static class TInfoTupleScheme extends org.apache.thrift.scheme.TupleScheme<TInfo> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, TInfo struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
            if (struct.isSetTraceId()) {
                optionals.set(0);
            }
            if (struct.isSetParentId()) {
                optionals.set(1);
            }
            oprot.writeBitSet(optionals, 2);
            if (struct.isSetTraceId()) {
                oprot.writeI64(struct.traceId);
            }
            if (struct.isSetParentId()) {
                oprot.writeI64(struct.parentId);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, TInfo struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(2);
            if (incoming.get(0)) {
                struct.traceId = iprot.readI64();
                struct.setTraceIdIsSet(true);
            }
            if (incoming.get(1)) {
                struct.parentId = iprot.readI64();
                struct.setParentIdIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}
