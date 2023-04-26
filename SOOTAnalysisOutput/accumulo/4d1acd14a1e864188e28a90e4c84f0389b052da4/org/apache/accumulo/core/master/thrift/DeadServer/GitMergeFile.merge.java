package org.apache.accumulo.core.master.thrift;

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
public class DeadServer implements org.apache.thrift.TBase<DeadServer, DeadServer._Fields>, java.io.Serializable, Cloneable, Comparable<DeadServer> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("DeadServer");

    private static final org.apache.thrift.protocol.TField SERVER_FIELD_DESC = new org.apache.thrift.protocol.TField("server", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final org.apache.thrift.protocol.TField LAST_STATUS_FIELD_DESC = new org.apache.thrift.protocol.TField("lastStatus", org.apache.thrift.protocol.TType.I64, (short) 2);

    private static final org.apache.thrift.protocol.TField STATUS_FIELD_DESC = new org.apache.thrift.protocol.TField("status", org.apache.thrift.protocol.TType.STRING, (short) 3);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new DeadServerStandardSchemeFactory());
        schemes.put(TupleScheme.class, new DeadServerTupleSchemeFactory());
    }

    public String server;

    public long lastStatus;

    public String status;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        SERVER((short) 1, "server"), LAST_STATUS((short) 2, "lastStatus"), STATUS((short) 3, "status");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

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

    private static final int __LASTSTATUS_ISSET_ID = 0;

    private byte __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.SERVER, new org.apache.thrift.meta_data.FieldMetaData("server", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.LAST_STATUS, new org.apache.thrift.meta_data.FieldMetaData("lastStatus", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.STATUS, new org.apache.thrift.meta_data.FieldMetaData("status", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(DeadServer.class, metaDataMap);
    }

    public DeadServer() {
    }

    public DeadServer(String server, long lastStatus, String status) {
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

    public String getServer() {
        return this.server;
    }

    public DeadServer setServer(String server) {
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
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __LASTSTATUS_ISSET_ID);
    }

    public boolean isSetLastStatus() {
        return EncodingUtils.testBit(__isset_bitfield, __LASTSTATUS_ISSET_ID);
    }

    public void setLastStatusIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __LASTSTATUS_ISSET_ID, value);
    }

    public String getStatus() {
        return this.status;
    }

    public DeadServer setStatus(String status) {
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

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case SERVER:
                if (value == null) {
                    unsetServer();
                } else {
                    setServer((String) value);
                }
                break;
            case LAST_STATUS:
                if (value == null) {
                    unsetLastStatus();
                } else {
                    setLastStatus((Long) value);
                }
                break;
            case STATUS:
                if (value == null) {
                    unsetStatus();
                } else {
                    setStatus((String) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case SERVER:
                return getServer();
            case LAST_STATUS:
                return Long.valueOf(getLastStatus());
            case STATUS:
                return getStatus();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case SERVER:
                return isSetServer();
            case LAST_STATUS:
                return isSetLastStatus();
            case STATUS:
                return isSetStatus();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof DeadServer)
            return this.equals((DeadServer) that);
        return false;
    }

    public boolean equals(DeadServer that) {
        if (that == null)
            return false;
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
        return 0;
    }

    @Override
    public int compareTo(DeadServer other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetServer()).compareTo(other.isSetServer());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetServer()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.server, other.server);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetLastStatus()).compareTo(other.isSetLastStatus());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetLastStatus()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.lastStatus, other.lastStatus);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetStatus()).compareTo(other.isSetStatus());
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
        StringBuilder sb = new StringBuilder("DeadServer(");
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

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        try {
            __isset_bitfield = 0;
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class DeadServerStandardSchemeFactory implements SchemeFactory {

        public DeadServerStandardScheme getScheme() {
            return new DeadServerStandardScheme();
        }
    }

    private static class DeadServerStandardScheme extends StandardScheme<DeadServer> {

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

    private static class DeadServerTupleSchemeFactory implements SchemeFactory {

        public DeadServerTupleScheme getScheme() {
            return new DeadServerTupleScheme();
        }
    }

    private static class DeadServerTupleScheme extends TupleScheme<DeadServer> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, DeadServer struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
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
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(3);
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
}
