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

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class NamespaceNotEmptyException extends TException implements org.apache.thrift.TBase<NamespaceNotEmptyException, NamespaceNotEmptyException._Fields>, java.io.Serializable, Cloneable, Comparable<NamespaceNotEmptyException> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("NamespaceNotEmptyException");

    private static final org.apache.thrift.protocol.TField MSG_FIELD_DESC = new org.apache.thrift.protocol.TField("msg", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new NamespaceNotEmptyExceptionStandardSchemeFactory());
        schemes.put(TupleScheme.class, new NamespaceNotEmptyExceptionTupleSchemeFactory());
    }

    public String msg;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        MSG((short) 1, "msg");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return MSG;
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

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.MSG, new org.apache.thrift.meta_data.FieldMetaData("msg", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(NamespaceNotEmptyException.class, metaDataMap);
    }

    public NamespaceNotEmptyException() {
    }

    public NamespaceNotEmptyException(String msg) {
        this();
        this.msg = msg;
    }

    public NamespaceNotEmptyException(NamespaceNotEmptyException other) {
        if (other.isSetMsg()) {
            this.msg = other.msg;
        }
    }

    public NamespaceNotEmptyException deepCopy() {
        return new NamespaceNotEmptyException(this);
    }

    @Override
    public void clear() {
        this.msg = null;
    }

    public String getMsg() {
        return this.msg;
    }

    public NamespaceNotEmptyException setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public void unsetMsg() {
        this.msg = null;
    }

    public boolean isSetMsg() {
        return this.msg != null;
    }

    public void setMsgIsSet(boolean value) {
        if (!value) {
            this.msg = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case MSG:
                if (value == null) {
                    unsetMsg();
                } else {
                    setMsg((String) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case MSG:
                return getMsg();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case MSG:
                return isSetMsg();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof NamespaceNotEmptyException)
            return this.equals((NamespaceNotEmptyException) that);
        return false;
    }

    public boolean equals(NamespaceNotEmptyException that) {
        if (that == null)
            return false;
        boolean this_present_msg = true && this.isSetMsg();
        boolean that_present_msg = true && that.isSetMsg();
        if (this_present_msg || that_present_msg) {
            if (!(this_present_msg && that_present_msg))
                return false;
            if (!this.msg.equals(that.msg))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        List<Object> list = new ArrayList<Object>();
        boolean present_msg = true && (isSetMsg());
        list.add(present_msg);
        if (present_msg)
            list.add(msg);
        return list.hashCode();
    }

    @Override
    public int compareTo(NamespaceNotEmptyException other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetMsg()).compareTo(other.isSetMsg());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetMsg()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.msg, other.msg);
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
        StringBuilder sb = new StringBuilder("NamespaceNotEmptyException(");
        boolean first = true;
        sb.append("msg:");
        if (this.msg == null) {
            sb.append("null");
        } else {
            sb.append(this.msg);
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
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class NamespaceNotEmptyExceptionStandardSchemeFactory implements SchemeFactory {

        public NamespaceNotEmptyExceptionStandardScheme getScheme() {
            return new NamespaceNotEmptyExceptionStandardScheme();
        }
    }

    private static class NamespaceNotEmptyExceptionStandardScheme extends StandardScheme<NamespaceNotEmptyException> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, NamespaceNotEmptyException struct) throws org.apache.thrift.TException {
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
                            struct.msg = iprot.readString();
                            struct.setMsgIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, NamespaceNotEmptyException struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.msg != null) {
                oprot.writeFieldBegin(MSG_FIELD_DESC);
                oprot.writeString(struct.msg);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class NamespaceNotEmptyExceptionTupleSchemeFactory implements SchemeFactory {

        public NamespaceNotEmptyExceptionTupleScheme getScheme() {
            return new NamespaceNotEmptyExceptionTupleScheme();
        }
    }

    private static class NamespaceNotEmptyExceptionTupleScheme extends TupleScheme<NamespaceNotEmptyException> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, NamespaceNotEmptyException struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetMsg()) {
                optionals.set(0);
            }
            oprot.writeBitSet(optionals, 1);
            if (struct.isSetMsg()) {
                oprot.writeString(struct.msg);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, NamespaceNotEmptyException struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(1);
            if (incoming.get(0)) {
                struct.msg = iprot.readString();
                struct.setMsgIsSet(true);
            }
        }
    }
}