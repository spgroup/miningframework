package org.apache.accumulo.core.data.thrift;

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
public class TConditionalSession implements org.apache.thrift.TBase<TConditionalSession, TConditionalSession._Fields>, java.io.Serializable, Cloneable, Comparable<TConditionalSession> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TConditionalSession");

    private static final org.apache.thrift.protocol.TField SESSION_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("sessionId", org.apache.thrift.protocol.TType.I64, (short) 1);

    private static final org.apache.thrift.protocol.TField TSERVER_LOCK_FIELD_DESC = new org.apache.thrift.protocol.TField("tserverLock", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.protocol.TField TTL_FIELD_DESC = new org.apache.thrift.protocol.TField("ttl", org.apache.thrift.protocol.TType.I64, (short) 3);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new TConditionalSessionStandardSchemeFactory());
        schemes.put(TupleScheme.class, new TConditionalSessionTupleSchemeFactory());
    }

    public long sessionId;

    public String tserverLock;

    public long ttl;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        SESSION_ID((short) 1, "sessionId"), TSERVER_LOCK((short) 2, "tserverLock"), TTL((short) 3, "ttl");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return SESSION_ID;
                case 2:
                    return TSERVER_LOCK;
                case 3:
                    return TTL;
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

    private static final int __SESSIONID_ISSET_ID = 0;

    private static final int __TTL_ISSET_ID = 1;

    private byte __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.SESSION_ID, new org.apache.thrift.meta_data.FieldMetaData("sessionId", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.TSERVER_LOCK, new org.apache.thrift.meta_data.FieldMetaData("tserverLock", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.TTL, new org.apache.thrift.meta_data.FieldMetaData("ttl", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TConditionalSession.class, metaDataMap);
    }

    public TConditionalSession() {
    }

    public TConditionalSession(long sessionId, String tserverLock, long ttl) {
        this();
        this.sessionId = sessionId;
        setSessionIdIsSet(true);
        this.tserverLock = tserverLock;
        this.ttl = ttl;
        setTtlIsSet(true);
    }

    public TConditionalSession(TConditionalSession other) {
        __isset_bitfield = other.__isset_bitfield;
        this.sessionId = other.sessionId;
        if (other.isSetTserverLock()) {
            this.tserverLock = other.tserverLock;
        }
        this.ttl = other.ttl;
    }

    public TConditionalSession deepCopy() {
        return new TConditionalSession(this);
    }

    @Override
    public void clear() {
        setSessionIdIsSet(false);
        this.sessionId = 0;
        this.tserverLock = null;
        setTtlIsSet(false);
        this.ttl = 0;
    }

    public long getSessionId() {
        return this.sessionId;
    }

    public TConditionalSession setSessionId(long sessionId) {
        this.sessionId = sessionId;
        setSessionIdIsSet(true);
        return this;
    }

    public void unsetSessionId() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __SESSIONID_ISSET_ID);
    }

    public boolean isSetSessionId() {
        return EncodingUtils.testBit(__isset_bitfield, __SESSIONID_ISSET_ID);
    }

    public void setSessionIdIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __SESSIONID_ISSET_ID, value);
    }

    public String getTserverLock() {
        return this.tserverLock;
    }

    public TConditionalSession setTserverLock(String tserverLock) {
        this.tserverLock = tserverLock;
        return this;
    }

    public void unsetTserverLock() {
        this.tserverLock = null;
    }

    public boolean isSetTserverLock() {
        return this.tserverLock != null;
    }

    public void setTserverLockIsSet(boolean value) {
        if (!value) {
            this.tserverLock = null;
        }
    }

    public long getTtl() {
        return this.ttl;
    }

    public TConditionalSession setTtl(long ttl) {
        this.ttl = ttl;
        setTtlIsSet(true);
        return this;
    }

    public void unsetTtl() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __TTL_ISSET_ID);
    }

    public boolean isSetTtl() {
        return EncodingUtils.testBit(__isset_bitfield, __TTL_ISSET_ID);
    }

    public void setTtlIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __TTL_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case SESSION_ID:
                if (value == null) {
                    unsetSessionId();
                } else {
                    setSessionId((Long) value);
                }
                break;
            case TSERVER_LOCK:
                if (value == null) {
                    unsetTserverLock();
                } else {
                    setTserverLock((String) value);
                }
                break;
            case TTL:
                if (value == null) {
                    unsetTtl();
                } else {
                    setTtl((Long) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case SESSION_ID:
                return Long.valueOf(getSessionId());
            case TSERVER_LOCK:
                return getTserverLock();
            case TTL:
                return Long.valueOf(getTtl());
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case SESSION_ID:
                return isSetSessionId();
            case TSERVER_LOCK:
                return isSetTserverLock();
            case TTL:
                return isSetTtl();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof TConditionalSession)
            return this.equals((TConditionalSession) that);
        return false;
    }

    public boolean equals(TConditionalSession that) {
        if (that == null)
            return false;
        boolean this_present_sessionId = true;
        boolean that_present_sessionId = true;
        if (this_present_sessionId || that_present_sessionId) {
            if (!(this_present_sessionId && that_present_sessionId))
                return false;
            if (this.sessionId != that.sessionId)
                return false;
        }
        boolean this_present_tserverLock = true && this.isSetTserverLock();
        boolean that_present_tserverLock = true && that.isSetTserverLock();
        if (this_present_tserverLock || that_present_tserverLock) {
            if (!(this_present_tserverLock && that_present_tserverLock))
                return false;
            if (!this.tserverLock.equals(that.tserverLock))
                return false;
        }
        boolean this_present_ttl = true;
        boolean that_present_ttl = true;
        if (this_present_ttl || that_present_ttl) {
            if (!(this_present_ttl && that_present_ttl))
                return false;
            if (this.ttl != that.ttl)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int compareTo(TConditionalSession other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetSessionId()).compareTo(other.isSetSessionId());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetSessionId()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sessionId, other.sessionId);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetTserverLock()).compareTo(other.isSetTserverLock());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTserverLock()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tserverLock, other.tserverLock);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetTtl()).compareTo(other.isSetTtl());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTtl()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ttl, other.ttl);
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
        StringBuilder sb = new StringBuilder("TConditionalSession(");
        boolean first = true;
        sb.append("sessionId:");
        sb.append(this.sessionId);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("tserverLock:");
        if (this.tserverLock == null) {
            sb.append("null");
        } else {
            sb.append(this.tserverLock);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("ttl:");
        sb.append(this.ttl);
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

    private static class TConditionalSessionStandardSchemeFactory implements SchemeFactory {

        public TConditionalSessionStandardScheme getScheme() {
            return new TConditionalSessionStandardScheme();
        }
    }

    private static class TConditionalSessionStandardScheme extends StandardScheme<TConditionalSession> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, TConditionalSession struct) throws org.apache.thrift.TException {
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
                            struct.sessionId = iprot.readI64();
                            struct.setSessionIdIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.tserverLock = iprot.readString();
                            struct.setTserverLockIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.ttl = iprot.readI64();
                            struct.setTtlIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, TConditionalSession struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            oprot.writeFieldBegin(SESSION_ID_FIELD_DESC);
            oprot.writeI64(struct.sessionId);
            oprot.writeFieldEnd();
            if (struct.tserverLock != null) {
                oprot.writeFieldBegin(TSERVER_LOCK_FIELD_DESC);
                oprot.writeString(struct.tserverLock);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(TTL_FIELD_DESC);
            oprot.writeI64(struct.ttl);
            oprot.writeFieldEnd();
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class TConditionalSessionTupleSchemeFactory implements SchemeFactory {

        public TConditionalSessionTupleScheme getScheme() {
            return new TConditionalSessionTupleScheme();
        }
    }

    private static class TConditionalSessionTupleScheme extends TupleScheme<TConditionalSession> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, TConditionalSession struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetSessionId()) {
                optionals.set(0);
            }
            if (struct.isSetTserverLock()) {
                optionals.set(1);
            }
            if (struct.isSetTtl()) {
                optionals.set(2);
            }
            oprot.writeBitSet(optionals, 3);
            if (struct.isSetSessionId()) {
                oprot.writeI64(struct.sessionId);
            }
            if (struct.isSetTserverLock()) {
                oprot.writeString(struct.tserverLock);
            }
            if (struct.isSetTtl()) {
                oprot.writeI64(struct.ttl);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, TConditionalSession struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(3);
            if (incoming.get(0)) {
                struct.sessionId = iprot.readI64();
                struct.setSessionIdIsSet(true);
            }
            if (incoming.get(1)) {
                struct.tserverLock = iprot.readString();
                struct.setTserverLockIsSet(true);
            }
            if (incoming.get(2)) {
                struct.ttl = iprot.readI64();
                struct.setTtlIsSet(true);
            }
        }
    }
}
