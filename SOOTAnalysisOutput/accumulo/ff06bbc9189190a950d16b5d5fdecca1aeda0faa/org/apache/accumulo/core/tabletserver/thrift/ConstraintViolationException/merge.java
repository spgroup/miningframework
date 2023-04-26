package org.apache.accumulo.core.tabletserver.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;
import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
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

@SuppressWarnings("all")
public class ConstraintViolationException extends TException implements org.apache.thrift.TBase<ConstraintViolationException, ConstraintViolationException._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ConstraintViolationException");

    private static final org.apache.thrift.protocol.TField VIOLATION_SUMMARIES_FIELD_DESC = new org.apache.thrift.protocol.TField("violationSummaries", org.apache.thrift.protocol.TType.LIST, (short) 1);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new ConstraintViolationExceptionStandardSchemeFactory());
        schemes.put(TupleScheme.class, new ConstraintViolationExceptionTupleSchemeFactory());
    }

    public List<org.apache.accumulo.core.data.thrift.TConstraintViolationSummary> violationSummaries;

    @SuppressWarnings("all")
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        VIOLATION_SUMMARIES((short) 1, "violationSummaries");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return VIOLATION_SUMMARIES;
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
        tmpMap.put(_Fields.VIOLATION_SUMMARIES, new org.apache.thrift.meta_data.FieldMetaData("violationSummaries", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.data.thrift.TConstraintViolationSummary.class))));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ConstraintViolationException.class, metaDataMap);
    }

    public ConstraintViolationException() {
    }

    public ConstraintViolationException(List<org.apache.accumulo.core.data.thrift.TConstraintViolationSummary> violationSummaries) {
        this();
        this.violationSummaries = violationSummaries;
    }

    public ConstraintViolationException(ConstraintViolationException other) {
        if (other.isSetViolationSummaries()) {
            List<org.apache.accumulo.core.data.thrift.TConstraintViolationSummary> __this__violationSummaries = new ArrayList<org.apache.accumulo.core.data.thrift.TConstraintViolationSummary>();
            for (org.apache.accumulo.core.data.thrift.TConstraintViolationSummary other_element : other.violationSummaries) {
                __this__violationSummaries.add(new org.apache.accumulo.core.data.thrift.TConstraintViolationSummary(other_element));
            }
            this.violationSummaries = __this__violationSummaries;
        }
    }

    public ConstraintViolationException deepCopy() {
        return new ConstraintViolationException(this);
    }

    @Override
    public void clear() {
        this.violationSummaries = null;
    }

    public int getViolationSummariesSize() {
        return (this.violationSummaries == null) ? 0 : this.violationSummaries.size();
    }

    public java.util.Iterator<org.apache.accumulo.core.data.thrift.TConstraintViolationSummary> getViolationSummariesIterator() {
        return (this.violationSummaries == null) ? null : this.violationSummaries.iterator();
    }

    public void addToViolationSummaries(org.apache.accumulo.core.data.thrift.TConstraintViolationSummary elem) {
        if (this.violationSummaries == null) {
            this.violationSummaries = new ArrayList<org.apache.accumulo.core.data.thrift.TConstraintViolationSummary>();
        }
        this.violationSummaries.add(elem);
    }

    public List<org.apache.accumulo.core.data.thrift.TConstraintViolationSummary> getViolationSummaries() {
        return this.violationSummaries;
    }

    public ConstraintViolationException setViolationSummaries(List<org.apache.accumulo.core.data.thrift.TConstraintViolationSummary> violationSummaries) {
        this.violationSummaries = violationSummaries;
        return this;
    }

    public void unsetViolationSummaries() {
        this.violationSummaries = null;
    }

    public boolean isSetViolationSummaries() {
        return this.violationSummaries != null;
    }

    public void setViolationSummariesIsSet(boolean value) {
        if (!value) {
            this.violationSummaries = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case VIOLATION_SUMMARIES:
                if (value == null) {
                    unsetViolationSummaries();
                } else {
                    setViolationSummaries((List<org.apache.accumulo.core.data.thrift.TConstraintViolationSummary>) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case VIOLATION_SUMMARIES:
                return getViolationSummaries();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case VIOLATION_SUMMARIES:
                return isSetViolationSummaries();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof ConstraintViolationException)
            return this.equals((ConstraintViolationException) that);
        return false;
    }

    public boolean equals(ConstraintViolationException that) {
        if (that == null)
            return false;
        boolean this_present_violationSummaries = true && this.isSetViolationSummaries();
        boolean that_present_violationSummaries = true && that.isSetViolationSummaries();
        if (this_present_violationSummaries || that_present_violationSummaries) {
            if (!(this_present_violationSummaries && that_present_violationSummaries))
                return false;
            if (!this.violationSummaries.equals(that.violationSummaries))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public int compareTo(ConstraintViolationException other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        ConstraintViolationException typedOther = (ConstraintViolationException) other;
        lastComparison = Boolean.valueOf(isSetViolationSummaries()).compareTo(typedOther.isSetViolationSummaries());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetViolationSummaries()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.violationSummaries, typedOther.violationSummaries);
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
        StringBuilder sb = new StringBuilder("ConstraintViolationException(");
        boolean first = true;
        sb.append("violationSummaries:");
        if (this.violationSummaries == null) {
            sb.append("null");
        } else {
            sb.append(this.violationSummaries);
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

    private static class ConstraintViolationExceptionStandardSchemeFactory implements SchemeFactory {

        public ConstraintViolationExceptionStandardScheme getScheme() {
            return new ConstraintViolationExceptionStandardScheme();
        }
    }

    private static class ConstraintViolationExceptionStandardScheme extends StandardScheme<ConstraintViolationException> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, ConstraintViolationException struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch(schemeField.id) {
                    case 1:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list0 = iprot.readListBegin();
                                struct.violationSummaries = new ArrayList<org.apache.accumulo.core.data.thrift.TConstraintViolationSummary>(_list0.size);
                                for (int _i1 = 0; _i1 < _list0.size; ++_i1) {
                                    org.apache.accumulo.core.data.thrift.TConstraintViolationSummary _elem2;
                                    _elem2 = new org.apache.accumulo.core.data.thrift.TConstraintViolationSummary();
                                    _elem2.read(iprot);
                                    struct.violationSummaries.add(_elem2);
                                }
                                iprot.readListEnd();
                            }
                            struct.setViolationSummariesIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, ConstraintViolationException struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.violationSummaries != null) {
                oprot.writeFieldBegin(VIOLATION_SUMMARIES_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.violationSummaries.size()));
                    for (org.apache.accumulo.core.data.thrift.TConstraintViolationSummary _iter3 : struct.violationSummaries) {
                        _iter3.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class ConstraintViolationExceptionTupleSchemeFactory implements SchemeFactory {

        public ConstraintViolationExceptionTupleScheme getScheme() {
            return new ConstraintViolationExceptionTupleScheme();
        }
    }

    private static class ConstraintViolationExceptionTupleScheme extends TupleScheme<ConstraintViolationException> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, ConstraintViolationException struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetViolationSummaries()) {
                optionals.set(0);
            }
            oprot.writeBitSet(optionals, 1);
            if (struct.isSetViolationSummaries()) {
                {
                    oprot.writeI32(struct.violationSummaries.size());
                    for (org.apache.accumulo.core.data.thrift.TConstraintViolationSummary _iter4 : struct.violationSummaries) {
                        _iter4.write(oprot);
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, ConstraintViolationException struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(1);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TList _list5 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.violationSummaries = new ArrayList<org.apache.accumulo.core.data.thrift.TConstraintViolationSummary>(_list5.size);
                    for (int _i6 = 0; _i6 < _list5.size; ++_i6) {
                        org.apache.accumulo.core.data.thrift.TConstraintViolationSummary _elem7;
                        _elem7 = new org.apache.accumulo.core.data.thrift.TConstraintViolationSummary();
                        _elem7.read(iprot);
                        struct.violationSummaries.add(_elem7);
                    }
                }
                struct.setViolationSummariesIsSet(true);
            }
        }
    }
}
