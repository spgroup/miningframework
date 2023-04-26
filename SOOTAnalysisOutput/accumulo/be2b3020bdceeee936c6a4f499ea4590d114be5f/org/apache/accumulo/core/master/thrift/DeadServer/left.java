package org.apache.accumulo.core.master.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class DeadServer implements org.apache.thrift.TBase<DeadServer, DeadServer._Fields>, java.io.Serializable, Cloneable, Comparable<DeadServer> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("DeadServer");

    private static final org.apache.thrift.protocol.TField SERVER_FIELD_DESC = new org.apache.thrift.protocol.TField("server", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final org.apache.thrift.protocol.TField LAST_STATUS_FIELD_DESC = new org.apache.thrift.protocol.TField("lastStatus", org.apache.thrift.protocol.TType.I64, (short) 2);

    private static final org.apache.thrift.protocol.TField STATUS_FIELD_DESC = new org.apache.thrift.protocol.TField("status", org.apache.thrift.protocol.TType.STRING, (short) 3);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new DeadServerStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new DeadServerTupleSchemeFactory();

    @org.apache.thrift.annotation.Nullable
    public java.lang.String server;

    public long lastStatus;

    @org.apache.thrift.annotation.Nullable
    public java.lang.String status;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        SERVER((short) 1, "server"), LAST_STATUS((short) 2, "lastStatus"), STATUS((short) 3, "status");

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
                    return SERVER;
                case 2:
                    return LAST_STATUS;
                case 3:
                    return STATUS;
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

    private static final int __LASTSTATUS_ISSET_ID = 0;

    private byte __isset_bitfield = 0;

    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.SERVER, new org.apache.thrift.meta_data.FieldMetaData("server", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.LAST_STATUS, new org.apache.thrift.meta_data.FieldMetaData("lastStatus", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.STATUS, new org.apache.thrift.meta_data.FieldMetaData("status", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(DeadServer.class, metaDataMap);
    }

    public DeadServer() {
    }

    public DeadServer(java.lang.String server, long lastStatus, java.lang.String status) {
        this();
        this.server = server;
        this.lastStatus = lastStatus;
        setLastStatusIsSet(true);
        this.status = status;
    }

    public DeadServer(DeadServer other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetServer()) {
            this.server = other.server;
        }
        this.lastStatus = other.lastStatus;
        if (other.isSetStatus()) {
            this.status = other.status;
        }
    }

    public DeadServer deepCopy() {
        return new DeadServer(this);
    }

    @Override
    public void clear() {
        this.server = null;
        setLastStatusIsSet(false);
        this.lastStatus = 0;
        this.status = null;
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.String getServer() {
        return this.server;
    }

    public DeadServer setServer(@org.apache.thrift.annotation.Nullable java.lang.String server) {
        this.server = server;
        return this;
    }

    public void unsetServer() {
        this.server = null;
    }

    public boolean isSetServer() {
        return this.server != null;
    }

    public void setServerIsSet(boolean value) {
        if (!value) {
            this.server = null;
        }
    }

    public long getLastStatus() {
        return this.lastStatus;
    }

    public DeadServer setLastStatus(long lastStatus) {
        this.lastStatus = lastStatus;
        setLastStatusIsSet(true);
        return this;
    }

    public void unsetLastStatus() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __LASTSTATUS_ISSET_ID);
    }

    public boolean isSetLastStatus() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __LASTSTATUS_ISSET_ID);
    }

    public void setLastStatusIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __LASTSTATUS_ISSET_ID, value);
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.String getStatus() {
        return this.status;
    }

    public DeadServer setStatus(@org.apache.thrift.annotation.Nullable java.lang.String status) {
        this.status = status;
        return this;
    }

    public void unsetStatus() {
        this.status = null;
    }

    public boolean isSetStatus() {
        return this.status != null;
    }

    public void setStatusIsSet(boolean value) {
        if (!value) {
            this.status = null;
        }
    }

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case SERVER:
                if (value == null) {
                    unsetServer();
                } else {
                    setServer((java.lang.String) value);
                }
                break;
            case LAST_STATUS:
                if (value == null) {
                    unsetLastStatus();
                } else {
                    setLastStatus((java.lang.Long) value);
                }
                break;
            case STATUS:
                if (value == null) {
                    unsetStatus();
                } else {
                    setStatus((java.lang.String) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case SERVER:
                return getServer();
            case LAST_STATUS:
                return getLastStatus();
            case STATUS:
                return getStatus();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }
        switch(field) {
            case SERVER:
                return isSetServer();
            case LAST_STATUS:
                return isSetLastStatus();
            case STATUS:
                return isSetStatus();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof DeadServer)
            return this.equals((DeadServer) that);
        return false;
    }

    public boolean equals(DeadServer that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
        boolean this_present_server = true && this.isSetServer();
        boolean that_present_server = true && that.isSetServer();
        if (this_present_server || that_present_server) {
            if (!(this_present_server && that_present_server))
                return false;
            if (!this.server.equals(that.server))
                return false;
        }
        boolean this_present_lastStatus = true;
        boolean that_present_lastStatus = true;
        if (this_present_lastStatus || that_present_lastStatus) {
            if (!(this_present_lastStatus && that_present_lastStatus))
                return false;
            if (this.lastStatus != that.lastStatus)
                return false;
        }
        boolean this_present_status = true && this.isSetStatus();
        boolean that_present_status = true && that.isSetStatus();
        if (this_present_status || that_present_status) {
            if (!(this_present_status && that_present_status))
                return false;
            if (!this.status.equals(that.status))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 8191 + ((isSetServer()) ? 131071 : 524287);
        if (isSetServer())
            hashCode = hashCode * 8191 + server.hashCode();
        hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(lastStatus);
        hashCode = hashCode * 8191 + ((isSetStatus()) ? 131071 : 524287);
        if (isSetStatus())
            hashCode = hashCode * 8191 + status.hashCode();
        return hashCode;
    }

    @Override
    public int compareTo(DeadServer other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetServer()).compareTo(other.isSetServer());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetServer()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.server, other.server);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetLastStatus()).compareTo(other.isSetLastStatus());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetLastStatus()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.lastStatus, other.lastStatus);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetStatus()).compareTo(other.isSetStatus());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetStatus()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.status, other.status);
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("DeadServer(");
        boolean first = true;
        sb.append("server:");
        if (this.server == null) {
            sb.append("null");
        } else {
            sb.append(this.server);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("lastStatus:");
        sb.append(this.lastStatus);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("status:");
        if (this.status == null) {
            sb.append("null");
        } else {
            sb.append(this.status);
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

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
        try {
            __isset_bitfield = 0;
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class DeadServerStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public DeadServerStandardScheme getScheme() {
            return new DeadServerStandardScheme();
        }
    }

    private static class DeadServerStandardScheme extends org.apache.thrift.scheme.StandardScheme<DeadServer> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, DeadServer struct) throws org.apache.thrift.TException {
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
                            struct.server = iprot.readString();
                            struct.setServerIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.lastStatus = iprot.readI64();
                            struct.setLastStatusIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.status = iprot.readString();
                            struct.setStatusIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, DeadServer struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.server != null) {
                oprot.writeFieldBegin(SERVER_FIELD_DESC);
                oprot.writeString(struct.server);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(LAST_STATUS_FIELD_DESC);
            oprot.writeI64(struct.lastStatus);
            oprot.writeFieldEnd();
            if (struct.status != null) {
                oprot.writeFieldBegin(STATUS_FIELD_DESC);
                oprot.writeString(struct.status);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class DeadServerTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public DeadServerTupleScheme getScheme() {
            return new DeadServerTupleScheme();
        }
    }

    private static class DeadServerTupleScheme extends org.apache.thrift.scheme.TupleScheme<DeadServer> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, DeadServer struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
            if (struct.isSetServer()) {
                optionals.set(0);
            }
            if (struct.isSetLastStatus()) {
                optionals.set(1);
            }
            if (struct.isSetStatus()) {
                optionals.set(2);
            }
            oprot.writeBitSet(optionals, 3);
            if (struct.isSetServer()) {
                oprot.writeString(struct.server);
            }
            if (struct.isSetLastStatus()) {
                oprot.writeI64(struct.lastStatus);
            }
            if (struct.isSetStatus()) {
                oprot.writeString(struct.status);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, DeadServer struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(3);
            if (incoming.get(0)) {
                struct.server = iprot.readString();
                struct.setServerIsSet(true);
            }
            if (incoming.get(1)) {
                struct.lastStatus = iprot.readI64();
                struct.setLastStatusIsSet(true);
            }
            if (incoming.get(2)) {
                struct.status = iprot.readString();
                struct.setStatusIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}
