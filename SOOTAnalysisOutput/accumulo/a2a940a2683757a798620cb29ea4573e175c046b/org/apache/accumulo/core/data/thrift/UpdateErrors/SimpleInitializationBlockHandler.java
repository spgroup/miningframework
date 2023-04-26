package org.apache.accumulo.core.data.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;
import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
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
public class UpdateErrors implements org.apache.thrift.TBase<UpdateErrors, UpdateErrors._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("UpdateErrors");

    private static final org.apache.thrift.protocol.TField FAILED_EXTENTS_FIELD_DESC = new org.apache.thrift.protocol.TField("failedExtents", org.apache.thrift.protocol.TType.MAP, (short) 1);

    private static final org.apache.thrift.protocol.TField VIOLATION_SUMMARIES_FIELD_DESC = new org.apache.thrift.protocol.TField("violationSummaries", org.apache.thrift.protocol.TType.LIST, (short) 2);

    private static final org.apache.thrift.protocol.TField AUTHORIZATION_FAILURES_FIELD_DESC = new org.apache.thrift.protocol.TField("authorizationFailures", org.apache.thrift.protocol.TType.LIST, (short) 3);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new UpdateErrorsStandardSchemeFactory());
        schemes.put(TupleScheme.class, new UpdateErrorsTupleSchemeFactory());
    }

    public Map<TKeyExtent, Long> failedExtents;

    public List<TConstraintViolationSummary> violationSummaries;

    public List<TKeyExtent> authorizationFailures;

    @SuppressWarnings("all")
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        FAILED_EXTENTS((short) 1, "failedExtents"), VIOLATION_SUMMARIES((short) 2, "violationSummaries"), AUTHORIZATION_FAILURES((short) 3, "authorizationFailures");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return FAILED_EXTENTS;
                case 2:
                    return VIOLATION_SUMMARIES;
                case 3:
                    return AUTHORIZATION_FAILURES;
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
        tmpMap.put(_Fields.FAILED_EXTENTS, new org.apache.thrift.meta_data.FieldMetaData("failedExtents", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TKeyExtent.class), new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64))));
        tmpMap.put(_Fields.VIOLATION_SUMMARIES, new org.apache.thrift.meta_data.FieldMetaData("violationSummaries", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TConstraintViolationSummary.class))));
        tmpMap.put(_Fields.AUTHORIZATION_FAILURES, new org.apache.thrift.meta_data.FieldMetaData("authorizationFailures", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TKeyExtent.class))));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(UpdateErrors.class, metaDataMap);
    }

    public UpdateErrors() {
    }

    public UpdateErrors(Map<TKeyExtent, Long> failedExtents, List<TConstraintViolationSummary> violationSummaries, List<TKeyExtent> authorizationFailures) {
        this();
        this.failedExtents = failedExtents;
        this.violationSummaries = violationSummaries;
        this.authorizationFailures = authorizationFailures;
    }

    public UpdateErrors(UpdateErrors other) {
        if (other.isSetFailedExtents()) {
            Map<TKeyExtent, Long> __this__failedExtents = new HashMap<TKeyExtent, Long>();
            for (Map.Entry<TKeyExtent, Long> other_element : other.failedExtents.entrySet()) {
                TKeyExtent other_element_key = other_element.getKey();
                Long other_element_value = other_element.getValue();
                TKeyExtent __this__failedExtents_copy_key = new TKeyExtent(other_element_key);
                Long __this__failedExtents_copy_value = other_element_value;
                __this__failedExtents.put(__this__failedExtents_copy_key, __this__failedExtents_copy_value);
            }
            this.failedExtents = __this__failedExtents;
        }
        if (other.isSetViolationSummaries()) {
            List<TConstraintViolationSummary> __this__violationSummaries = new ArrayList<TConstraintViolationSummary>();
            for (TConstraintViolationSummary other_element : other.violationSummaries) {
                __this__violationSummaries.add(new TConstraintViolationSummary(other_element));
            }
            this.violationSummaries = __this__violationSummaries;
        }
        if (other.isSetAuthorizationFailures()) {
            List<TKeyExtent> __this__authorizationFailures = new ArrayList<TKeyExtent>();
            for (TKeyExtent other_element : other.authorizationFailures) {
                __this__authorizationFailures.add(new TKeyExtent(other_element));
            }
            this.authorizationFailures = __this__authorizationFailures;
        }
    }

    public UpdateErrors deepCopy() {
        return new UpdateErrors(this);
    }

    @Override
    public void clear() {
        this.failedExtents = null;
        this.violationSummaries = null;
        this.authorizationFailures = null;
    }

    public int getFailedExtentsSize() {
        return (this.failedExtents == null) ? 0 : this.failedExtents.size();
    }

    public void putToFailedExtents(TKeyExtent key, long val) {
        if (this.failedExtents == null) {
            this.failedExtents = new HashMap<TKeyExtent, Long>();
        }
        this.failedExtents.put(key, val);
    }

    public Map<TKeyExtent, Long> getFailedExtents() {
        return this.failedExtents;
    }

    public UpdateErrors setFailedExtents(Map<TKeyExtent, Long> failedExtents) {
        this.failedExtents = failedExtents;
        return this;
    }

    public void unsetFailedExtents() {
        this.failedExtents = null;
    }

    public boolean isSetFailedExtents() {
        return this.failedExtents != null;
    }

    public void setFailedExtentsIsSet(boolean value) {
        if (!value) {
            this.failedExtents = null;
        }
    }

    public int getViolationSummariesSize() {
        return (this.violationSummaries == null) ? 0 : this.violationSummaries.size();
    }

    public java.util.Iterator<TConstraintViolationSummary> getViolationSummariesIterator() {
        return (this.violationSummaries == null) ? null : this.violationSummaries.iterator();
    }

    public void addToViolationSummaries(TConstraintViolationSummary elem) {
        if (this.violationSummaries == null) {
            this.violationSummaries = new ArrayList<TConstraintViolationSummary>();
        }
        this.violationSummaries.add(elem);
    }

    public List<TConstraintViolationSummary> getViolationSummaries() {
        return this.violationSummaries;
    }

    public UpdateErrors setViolationSummaries(List<TConstraintViolationSummary> violationSummaries) {
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

    public int getAuthorizationFailuresSize() {
        return (this.authorizationFailures == null) ? 0 : this.authorizationFailures.size();
    }

    public java.util.Iterator<TKeyExtent> getAuthorizationFailuresIterator() {
        return (this.authorizationFailures == null) ? null : this.authorizationFailures.iterator();
    }

    public void addToAuthorizationFailures(TKeyExtent elem) {
        if (this.authorizationFailures == null) {
            this.authorizationFailures = new ArrayList<TKeyExtent>();
        }
        this.authorizationFailures.add(elem);
    }

    public List<TKeyExtent> getAuthorizationFailures() {
        return this.authorizationFailures;
    }

    public UpdateErrors setAuthorizationFailures(List<TKeyExtent> authorizationFailures) {
        this.authorizationFailures = authorizationFailures;
        return this;
    }

    public void unsetAuthorizationFailures() {
        this.authorizationFailures = null;
    }

    public boolean isSetAuthorizationFailures() {
        return this.authorizationFailures != null;
    }

    public void setAuthorizationFailuresIsSet(boolean value) {
        if (!value) {
            this.authorizationFailures = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case FAILED_EXTENTS:
                if (value == null) {
                    unsetFailedExtents();
                } else {
                    setFailedExtents((Map<TKeyExtent, Long>) value);
                }
                break;
            case VIOLATION_SUMMARIES:
                if (value == null) {
                    unsetViolationSummaries();
                } else {
                    setViolationSummaries((List<TConstraintViolationSummary>) value);
                }
                break;
            case AUTHORIZATION_FAILURES:
                if (value == null) {
                    unsetAuthorizationFailures();
                } else {
                    setAuthorizationFailures((List<TKeyExtent>) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case FAILED_EXTENTS:
                return getFailedExtents();
            case VIOLATION_SUMMARIES:
                return getViolationSummaries();
            case AUTHORIZATION_FAILURES:
                return getAuthorizationFailures();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case FAILED_EXTENTS:
                return isSetFailedExtents();
            case VIOLATION_SUMMARIES:
                return isSetViolationSummaries();
            case AUTHORIZATION_FAILURES:
                return isSetAuthorizationFailures();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof UpdateErrors)
            return this.equals((UpdateErrors) that);
        return false;
    }

    public boolean equals(UpdateErrors that) {
        if (that == null)
            return false;
        boolean this_present_failedExtents = true && this.isSetFailedExtents();
        boolean that_present_failedExtents = true && that.isSetFailedExtents();
        if (this_present_failedExtents || that_present_failedExtents) {
            if (!(this_present_failedExtents && that_present_failedExtents))
                return false;
            if (!this.failedExtents.equals(that.failedExtents))
                return false;
        }
        boolean this_present_violationSummaries = true && this.isSetViolationSummaries();
        boolean that_present_violationSummaries = true && that.isSetViolationSummaries();
        if (this_present_violationSummaries || that_present_violationSummaries) {
            if (!(this_present_violationSummaries && that_present_violationSummaries))
                return false;
            if (!this.violationSummaries.equals(that.violationSummaries))
                return false;
        }
        boolean this_present_authorizationFailures = true && this.isSetAuthorizationFailures();
        boolean that_present_authorizationFailures = true && that.isSetAuthorizationFailures();
        if (this_present_authorizationFailures || that_present_authorizationFailures) {
            if (!(this_present_authorizationFailures && that_present_authorizationFailures))
                return false;
            if (!this.authorizationFailures.equals(that.authorizationFailures))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public int compareTo(UpdateErrors other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        UpdateErrors typedOther = (UpdateErrors) other;
        lastComparison = Boolean.valueOf(isSetFailedExtents()).compareTo(typedOther.isSetFailedExtents());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetFailedExtents()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.failedExtents, typedOther.failedExtents);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
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
        lastComparison = Boolean.valueOf(isSetAuthorizationFailures()).compareTo(typedOther.isSetAuthorizationFailures());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetAuthorizationFailures()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.authorizationFailures, typedOther.authorizationFailures);
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
        StringBuilder sb = new StringBuilder("UpdateErrors(");
        boolean first = true;
        sb.append("failedExtents:");
        if (this.failedExtents == null) {
            sb.append("null");
        } else {
            sb.append(this.failedExtents);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("violationSummaries:");
        if (this.violationSummaries == null) {
            sb.append("null");
        } else {
            sb.append(this.violationSummaries);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("authorizationFailures:");
        if (this.authorizationFailures == null) {
            sb.append("null");
        } else {
            sb.append(this.authorizationFailures);
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

    private static class UpdateErrorsStandardSchemeFactory implements SchemeFactory {

        public UpdateErrorsStandardScheme getScheme() {
            return new UpdateErrorsStandardScheme();
        }
    }

    private static class UpdateErrorsStandardScheme extends StandardScheme<UpdateErrors> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, UpdateErrors struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch(schemeField.id) {
                    case 1:
                        if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
                            {
                                org.apache.thrift.protocol.TMap _map50 = iprot.readMapBegin();
                                struct.failedExtents = new HashMap<TKeyExtent, Long>(2 * _map50.size);
                                for (int _i51 = 0; _i51 < _map50.size; ++_i51) {
                                    TKeyExtent _key52;
                                    long _val53;
                                    _key52 = new TKeyExtent();
                                    _key52.read(iprot);
                                    _val53 = iprot.readI64();
                                    struct.failedExtents.put(_key52, _val53);
                                }
                                iprot.readMapEnd();
                            }
                            struct.setFailedExtentsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list54 = iprot.readListBegin();
                                struct.violationSummaries = new ArrayList<TConstraintViolationSummary>(_list54.size);
                                for (int _i55 = 0; _i55 < _list54.size; ++_i55) {
                                    TConstraintViolationSummary _elem56;
                                    _elem56 = new TConstraintViolationSummary();
                                    _elem56.read(iprot);
                                    struct.violationSummaries.add(_elem56);
                                }
                                iprot.readListEnd();
                            }
                            struct.setViolationSummariesIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list57 = iprot.readListBegin();
                                struct.authorizationFailures = new ArrayList<TKeyExtent>(_list57.size);
                                for (int _i58 = 0; _i58 < _list57.size; ++_i58) {
                                    TKeyExtent _elem59;
                                    _elem59 = new TKeyExtent();
                                    _elem59.read(iprot);
                                    struct.authorizationFailures.add(_elem59);
                                }
                                iprot.readListEnd();
                            }
                            struct.setAuthorizationFailuresIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, UpdateErrors struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.failedExtents != null) {
                oprot.writeFieldBegin(FAILED_EXTENTS_FIELD_DESC);
                {
                    oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRUCT, org.apache.thrift.protocol.TType.I64, struct.failedExtents.size()));
                    for (Map.Entry<TKeyExtent, Long> _iter60 : struct.failedExtents.entrySet()) {
                        _iter60.getKey().write(oprot);
                        oprot.writeI64(_iter60.getValue());
                    }
                    oprot.writeMapEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.violationSummaries != null) {
                oprot.writeFieldBegin(VIOLATION_SUMMARIES_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.violationSummaries.size()));
                    for (TConstraintViolationSummary _iter61 : struct.violationSummaries) {
                        _iter61.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.authorizationFailures != null) {
                oprot.writeFieldBegin(AUTHORIZATION_FAILURES_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.authorizationFailures.size()));
                    for (TKeyExtent _iter62 : struct.authorizationFailures) {
                        _iter62.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class UpdateErrorsTupleSchemeFactory implements SchemeFactory {

        public UpdateErrorsTupleScheme getScheme() {
            return new UpdateErrorsTupleScheme();
        }
    }

    private static class UpdateErrorsTupleScheme extends TupleScheme<UpdateErrors> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, UpdateErrors struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetFailedExtents()) {
                optionals.set(0);
            }
            if (struct.isSetViolationSummaries()) {
                optionals.set(1);
            }
            if (struct.isSetAuthorizationFailures()) {
                optionals.set(2);
            }
            oprot.writeBitSet(optionals, 3);
            if (struct.isSetFailedExtents()) {
                {
                    oprot.writeI32(struct.failedExtents.size());
                    for (Map.Entry<TKeyExtent, Long> _iter63 : struct.failedExtents.entrySet()) {
                        _iter63.getKey().write(oprot);
                        oprot.writeI64(_iter63.getValue());
                    }
                }
            }
            if (struct.isSetViolationSummaries()) {
                {
                    oprot.writeI32(struct.violationSummaries.size());
                    for (TConstraintViolationSummary _iter64 : struct.violationSummaries) {
                        _iter64.write(oprot);
                    }
                }
            }
            if (struct.isSetAuthorizationFailures()) {
                {
                    oprot.writeI32(struct.authorizationFailures.size());
                    for (TKeyExtent _iter65 : struct.authorizationFailures) {
                        _iter65.write(oprot);
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, UpdateErrors struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(3);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TMap _map66 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRUCT, org.apache.thrift.protocol.TType.I64, iprot.readI32());
                    struct.failedExtents = new HashMap<TKeyExtent, Long>(2 * _map66.size);
                    for (int _i67 = 0; _i67 < _map66.size; ++_i67) {
                        TKeyExtent _key68;
                        long _val69;
                        _key68 = new TKeyExtent();
                        _key68.read(iprot);
                        _val69 = iprot.readI64();
                        struct.failedExtents.put(_key68, _val69);
                    }
                }
                struct.setFailedExtentsIsSet(true);
            }
            if (incoming.get(1)) {
                {
                    org.apache.thrift.protocol.TList _list70 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.violationSummaries = new ArrayList<TConstraintViolationSummary>(_list70.size);
                    for (int _i71 = 0; _i71 < _list70.size; ++_i71) {
                        TConstraintViolationSummary _elem72;
                        _elem72 = new TConstraintViolationSummary();
                        _elem72.read(iprot);
                        struct.violationSummaries.add(_elem72);
                    }
                }
                struct.setViolationSummariesIsSet(true);
            }
            if (incoming.get(2)) {
                {
                    org.apache.thrift.protocol.TList _list73 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.authorizationFailures = new ArrayList<TKeyExtent>(_list73.size);
                    for (int _i74 = 0; _i74 < _list73.size; ++_i74) {
                        TKeyExtent _elem75;
                        _elem75 = new TKeyExtent();
                        _elem75.read(iprot);
                        struct.authorizationFailures.add(_elem75);
                    }
                }
                struct.setAuthorizationFailuresIsSet(true);
            }
        }
    }
}