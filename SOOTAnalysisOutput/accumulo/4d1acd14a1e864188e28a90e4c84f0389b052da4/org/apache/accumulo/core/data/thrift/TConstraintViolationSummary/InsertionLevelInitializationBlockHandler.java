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
public class TConstraintViolationSummary implements org.apache.thrift.TBase<TConstraintViolationSummary, TConstraintViolationSummary._Fields>, java.io.Serializable, Cloneable, Comparable<TConstraintViolationSummary> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TConstraintViolationSummary");

    private static final org.apache.thrift.protocol.TField CONSTRAIN_CLASS_FIELD_DESC = new org.apache.thrift.protocol.TField("constrainClass", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final org.apache.thrift.protocol.TField VIOLATION_CODE_FIELD_DESC = new org.apache.thrift.protocol.TField("violationCode", org.apache.thrift.protocol.TType.I16, (short) 2);

    private static final org.apache.thrift.protocol.TField VIOLATION_DESCRIPTION_FIELD_DESC = new org.apache.thrift.protocol.TField("violationDescription", org.apache.thrift.protocol.TType.STRING, (short) 3);

    private static final org.apache.thrift.protocol.TField NUMBER_OF_VIOLATING_MUTATIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("numberOfViolatingMutations", org.apache.thrift.protocol.TType.I64, (short) 4);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new TConstraintViolationSummaryStandardSchemeFactory());
        schemes.put(TupleScheme.class, new TConstraintViolationSummaryTupleSchemeFactory());
    }

    public String constrainClass;

    public short violationCode;

    public String violationDescription;

    public long numberOfViolatingMutations;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        CONSTRAIN_CLASS((short) 1, "constrainClass"), VIOLATION_CODE((short) 2, "violationCode"), VIOLATION_DESCRIPTION((short) 3, "violationDescription"), NUMBER_OF_VIOLATING_MUTATIONS((short) 4, "numberOfViolatingMutations");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return CONSTRAIN_CLASS;
                case 2:
                    return VIOLATION_CODE;
                case 3:
                    return VIOLATION_DESCRIPTION;
                case 4:
                    return NUMBER_OF_VIOLATING_MUTATIONS;
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

    private static final int __VIOLATIONCODE_ISSET_ID = 0;

    private static final int __NUMBEROFVIOLATINGMUTATIONS_ISSET_ID = 1;

    private byte __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.CONSTRAIN_CLASS, new org.apache.thrift.meta_data.FieldMetaData("constrainClass", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.VIOLATION_CODE, new org.apache.thrift.meta_data.FieldMetaData("violationCode", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I16)));
        tmpMap.put(_Fields.VIOLATION_DESCRIPTION, new org.apache.thrift.meta_data.FieldMetaData("violationDescription", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.NUMBER_OF_VIOLATING_MUTATIONS, new org.apache.thrift.meta_data.FieldMetaData("numberOfViolatingMutations", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TConstraintViolationSummary.class, metaDataMap);
    }

    public TConstraintViolationSummary() {
    }

    public TConstraintViolationSummary(String constrainClass, short violationCode, String violationDescription, long numberOfViolatingMutations) {
        this();
        this.constrainClass = constrainClass;
        this.violationCode = violationCode;
        setViolationCodeIsSet(true);
        this.violationDescription = violationDescription;
        this.numberOfViolatingMutations = numberOfViolatingMutations;
        setNumberOfViolatingMutationsIsSet(true);
    }

    public TConstraintViolationSummary(TConstraintViolationSummary other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetConstrainClass()) {
            this.constrainClass = other.constrainClass;
        }
        this.violationCode = other.violationCode;
        if (other.isSetViolationDescription()) {
            this.violationDescription = other.violationDescription;
        }
        this.numberOfViolatingMutations = other.numberOfViolatingMutations;
    }

    public TConstraintViolationSummary deepCopy() {
        return new TConstraintViolationSummary(this);
    }

    @Override
    public void clear() {
        this.constrainClass = null;
        setViolationCodeIsSet(false);
        this.violationCode = 0;
        this.violationDescription = null;
        setNumberOfViolatingMutationsIsSet(false);
        this.numberOfViolatingMutations = 0;
    }

    public String getConstrainClass() {
        return this.constrainClass;
    }

    public TConstraintViolationSummary setConstrainClass(String constrainClass) {
        this.constrainClass = constrainClass;
        return this;
    }

    public void unsetConstrainClass() {
        this.constrainClass = null;
    }

    public boolean isSetConstrainClass() {
        return this.constrainClass != null;
    }

    public void setConstrainClassIsSet(boolean value) {
        if (!value) {
            this.constrainClass = null;
        }
    }

    public short getViolationCode() {
        return this.violationCode;
    }

    public TConstraintViolationSummary setViolationCode(short violationCode) {
        this.violationCode = violationCode;
        setViolationCodeIsSet(true);
        return this;
    }

    public void unsetViolationCode() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __VIOLATIONCODE_ISSET_ID);
    }

    public boolean isSetViolationCode() {
        return EncodingUtils.testBit(__isset_bitfield, __VIOLATIONCODE_ISSET_ID);
    }

    public void setViolationCodeIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __VIOLATIONCODE_ISSET_ID, value);
    }

    public String getViolationDescription() {
        return this.violationDescription;
    }

    public TConstraintViolationSummary setViolationDescription(String violationDescription) {
        this.violationDescription = violationDescription;
        return this;
    }

    public void unsetViolationDescription() {
        this.violationDescription = null;
    }

    public boolean isSetViolationDescription() {
        return this.violationDescription != null;
    }

    public void setViolationDescriptionIsSet(boolean value) {
        if (!value) {
            this.violationDescription = null;
        }
    }

    public long getNumberOfViolatingMutations() {
        return this.numberOfViolatingMutations;
    }

    public TConstraintViolationSummary setNumberOfViolatingMutations(long numberOfViolatingMutations) {
        this.numberOfViolatingMutations = numberOfViolatingMutations;
        setNumberOfViolatingMutationsIsSet(true);
        return this;
    }

    public void unsetNumberOfViolatingMutations() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __NUMBEROFVIOLATINGMUTATIONS_ISSET_ID);
    }

    public boolean isSetNumberOfViolatingMutations() {
        return EncodingUtils.testBit(__isset_bitfield, __NUMBEROFVIOLATINGMUTATIONS_ISSET_ID);
    }

    public void setNumberOfViolatingMutationsIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __NUMBEROFVIOLATINGMUTATIONS_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case CONSTRAIN_CLASS:
                if (value == null) {
                    unsetConstrainClass();
                } else {
                    setConstrainClass((String) value);
                }
                break;
            case VIOLATION_CODE:
                if (value == null) {
                    unsetViolationCode();
                } else {
                    setViolationCode((Short) value);
                }
                break;
            case VIOLATION_DESCRIPTION:
                if (value == null) {
                    unsetViolationDescription();
                } else {
                    setViolationDescription((String) value);
                }
                break;
            case NUMBER_OF_VIOLATING_MUTATIONS:
                if (value == null) {
                    unsetNumberOfViolatingMutations();
                } else {
                    setNumberOfViolatingMutations((Long) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case CONSTRAIN_CLASS:
                return getConstrainClass();
            case VIOLATION_CODE:
                return Short.valueOf(getViolationCode());
            case VIOLATION_DESCRIPTION:
                return getViolationDescription();
            case NUMBER_OF_VIOLATING_MUTATIONS:
                return Long.valueOf(getNumberOfViolatingMutations());
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case CONSTRAIN_CLASS:
                return isSetConstrainClass();
            case VIOLATION_CODE:
                return isSetViolationCode();
            case VIOLATION_DESCRIPTION:
                return isSetViolationDescription();
            case NUMBER_OF_VIOLATING_MUTATIONS:
                return isSetNumberOfViolatingMutations();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof TConstraintViolationSummary)
            return this.equals((TConstraintViolationSummary) that);
        return false;
    }

    public boolean equals(TConstraintViolationSummary that) {
        if (that == null)
            return false;
        boolean this_present_constrainClass = true && this.isSetConstrainClass();
        boolean that_present_constrainClass = true && that.isSetConstrainClass();
        if (this_present_constrainClass || that_present_constrainClass) {
            if (!(this_present_constrainClass && that_present_constrainClass))
                return false;
            if (!this.constrainClass.equals(that.constrainClass))
                return false;
        }
        boolean this_present_violationCode = true;
        boolean that_present_violationCode = true;
        if (this_present_violationCode || that_present_violationCode) {
            if (!(this_present_violationCode && that_present_violationCode))
                return false;
            if (this.violationCode != that.violationCode)
                return false;
        }
        boolean this_present_violationDescription = true && this.isSetViolationDescription();
        boolean that_present_violationDescription = true && that.isSetViolationDescription();
        if (this_present_violationDescription || that_present_violationDescription) {
            if (!(this_present_violationDescription && that_present_violationDescription))
                return false;
            if (!this.violationDescription.equals(that.violationDescription))
                return false;
        }
        boolean this_present_numberOfViolatingMutations = true;
        boolean that_present_numberOfViolatingMutations = true;
        if (this_present_numberOfViolatingMutations || that_present_numberOfViolatingMutations) {
            if (!(this_present_numberOfViolatingMutations && that_present_numberOfViolatingMutations))
                return false;
            if (this.numberOfViolatingMutations != that.numberOfViolatingMutations)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int compareTo(TConstraintViolationSummary other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetConstrainClass()).compareTo(other.isSetConstrainClass());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetConstrainClass()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.constrainClass, other.constrainClass);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetViolationCode()).compareTo(other.isSetViolationCode());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetViolationCode()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.violationCode, other.violationCode);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetViolationDescription()).compareTo(other.isSetViolationDescription());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetViolationDescription()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.violationDescription, other.violationDescription);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetNumberOfViolatingMutations()).compareTo(other.isSetNumberOfViolatingMutations());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetNumberOfViolatingMutations()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.numberOfViolatingMutations, other.numberOfViolatingMutations);
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
        StringBuilder sb = new StringBuilder("TConstraintViolationSummary(");
        boolean first = true;
        sb.append("constrainClass:");
        if (this.constrainClass == null) {
            sb.append("null");
        } else {
            sb.append(this.constrainClass);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("violationCode:");
        sb.append(this.violationCode);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("violationDescription:");
        if (this.violationDescription == null) {
            sb.append("null");
        } else {
            sb.append(this.violationDescription);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("numberOfViolatingMutations:");
        sb.append(this.numberOfViolatingMutations);
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

    private static class TConstraintViolationSummaryStandardSchemeFactory implements SchemeFactory {

        public TConstraintViolationSummaryStandardScheme getScheme() {
            return new TConstraintViolationSummaryStandardScheme();
        }
    }

    private static class TConstraintViolationSummaryStandardScheme extends StandardScheme<TConstraintViolationSummary> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, TConstraintViolationSummary struct) throws org.apache.thrift.TException {
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
                            struct.constrainClass = iprot.readString();
                            struct.setConstrainClassIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I16) {
                            struct.violationCode = iprot.readI16();
                            struct.setViolationCodeIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.violationDescription = iprot.readString();
                            struct.setViolationDescriptionIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.numberOfViolatingMutations = iprot.readI64();
                            struct.setNumberOfViolatingMutationsIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, TConstraintViolationSummary struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.constrainClass != null) {
                oprot.writeFieldBegin(CONSTRAIN_CLASS_FIELD_DESC);
                oprot.writeString(struct.constrainClass);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(VIOLATION_CODE_FIELD_DESC);
            oprot.writeI16(struct.violationCode);
            oprot.writeFieldEnd();
            if (struct.violationDescription != null) {
                oprot.writeFieldBegin(VIOLATION_DESCRIPTION_FIELD_DESC);
                oprot.writeString(struct.violationDescription);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(NUMBER_OF_VIOLATING_MUTATIONS_FIELD_DESC);
            oprot.writeI64(struct.numberOfViolatingMutations);
            oprot.writeFieldEnd();
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class TConstraintViolationSummaryTupleSchemeFactory implements SchemeFactory {

        public TConstraintViolationSummaryTupleScheme getScheme() {
            return new TConstraintViolationSummaryTupleScheme();
        }
    }

    private static class TConstraintViolationSummaryTupleScheme extends TupleScheme<TConstraintViolationSummary> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, TConstraintViolationSummary struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetConstrainClass()) {
                optionals.set(0);
            }
            if (struct.isSetViolationCode()) {
                optionals.set(1);
            }
            if (struct.isSetViolationDescription()) {
                optionals.set(2);
            }
            if (struct.isSetNumberOfViolatingMutations()) {
                optionals.set(3);
            }
            oprot.writeBitSet(optionals, 4);
            if (struct.isSetConstrainClass()) {
                oprot.writeString(struct.constrainClass);
            }
            if (struct.isSetViolationCode()) {
                oprot.writeI16(struct.violationCode);
            }
            if (struct.isSetViolationDescription()) {
                oprot.writeString(struct.violationDescription);
            }
            if (struct.isSetNumberOfViolatingMutations()) {
                oprot.writeI64(struct.numberOfViolatingMutations);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, TConstraintViolationSummary struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(4);
            if (incoming.get(0)) {
                struct.constrainClass = iprot.readString();
                struct.setConstrainClassIsSet(true);
            }
            if (incoming.get(1)) {
                struct.violationCode = iprot.readI16();
                struct.setViolationCodeIsSet(true);
            }
            if (incoming.get(2)) {
                struct.violationDescription = iprot.readString();
                struct.setViolationDescriptionIsSet(true);
            }
            if (incoming.get(3)) {
                struct.numberOfViolatingMutations = iprot.readI64();
                struct.setNumberOfViolatingMutationsIsSet(true);
            }
        }
    }
}