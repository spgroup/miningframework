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

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class RecoveryException extends TException implements org.apache.thrift.TBase<RecoveryException, RecoveryException._Fields>, java.io.Serializable, Cloneable, Comparable<RecoveryException> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("RecoveryException");

    private static final org.apache.thrift.protocol.TField WHY_FIELD_DESC = new org.apache.thrift.protocol.TField("why", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new RecoveryExceptionStandardSchemeFactory());
        schemes.put(TupleScheme.class, new RecoveryExceptionTupleSchemeFactory());
    }

    public String why;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        WHY((short) 1, "why");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return WHY;
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
        tmpMap.put(_Fields.WHY, new org.apache.thrift.meta_data.FieldMetaData("why", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(RecoveryException.class, metaDataMap);
    }

    public RecoveryException() {
    }

    public RecoveryException(String why) {
        this();
        this.why = why;
    }

    public RecoveryException(RecoveryException other) {
        if (other.isSetWhy()) {
            this.why = other.why;
        }
    }

    public RecoveryException deepCopy() {
        return new RecoveryException(this);
    }

    @Override
    public void clear() {
        this.why = null;
    }

    public String getWhy() {
        return this.why;
    }

    public RecoveryException setWhy(String why) {
        this.why = why;
        return this;
    }

    public void unsetWhy() {
        this.why = null;
    }

    public boolean isSetWhy() {
        return this.why != null;
    }

    public void setWhyIsSet(boolean value) {
        if (!value) {
            this.why = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case WHY:
                if (value == null) {
                    unsetWhy();
                } else {
                    setWhy((String) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case WHY:
                return getWhy();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case WHY:
                return isSetWhy();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof RecoveryException)
            return this.equals((RecoveryException) that);
        return false;
    }

    public boolean equals(RecoveryException that) {
        if (that == null)
            return false;
        boolean this_present_why = true && this.isSetWhy();
        boolean that_present_why = true && that.isSetWhy();
        if (this_present_why || that_present_why) {
            if (!(this_present_why && that_present_why))
                return false;
            if (!this.why.equals(that.why))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        List<Object> list = new ArrayList<Object>();
        boolean present_why = true && (isSetWhy());
        list.add(present_why);
        if (present_why)
            list.add(why);
        return list.hashCode();
    }

    @Override
    public int compareTo(RecoveryException other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetWhy()).compareTo(other.isSetWhy());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetWhy()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.why, other.why);
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
        StringBuilder sb = new StringBuilder("RecoveryException(");
        boolean first = true;
        sb.append("why:");
        if (this.why == null) {
            sb.append("null");
        } else {
            sb.append(this.why);
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

    private static class RecoveryExceptionStandardSchemeFactory implements SchemeFactory {

        public RecoveryExceptionStandardScheme getScheme() {
            return new RecoveryExceptionStandardScheme();
        }
    }

    private static class RecoveryExceptionStandardScheme extends StandardScheme<RecoveryException> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, RecoveryException struct) throws org.apache.thrift.TException {
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
                            struct.why = iprot.readString();
                            struct.setWhyIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, RecoveryException struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.why != null) {
                oprot.writeFieldBegin(WHY_FIELD_DESC);
                oprot.writeString(struct.why);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class RecoveryExceptionTupleSchemeFactory implements SchemeFactory {

        public RecoveryExceptionTupleScheme getScheme() {
            return new RecoveryExceptionTupleScheme();
        }
    }

    private static class RecoveryExceptionTupleScheme extends TupleScheme<RecoveryException> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, RecoveryException struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetWhy()) {
                optionals.set(0);
            }
            oprot.writeBitSet(optionals, 1);
            if (struct.isSetWhy()) {
                oprot.writeString(struct.why);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, RecoveryException struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(1);
            if (incoming.get(0)) {
                struct.why = iprot.readString();
                struct.setWhyIsSet(true);
            }
        }
    }
}