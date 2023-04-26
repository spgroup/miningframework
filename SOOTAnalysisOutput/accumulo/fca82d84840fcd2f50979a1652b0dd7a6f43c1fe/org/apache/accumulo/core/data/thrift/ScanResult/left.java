package org.apache.accumulo.core.data.thrift;

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
public class ScanResult implements org.apache.thrift.TBase<ScanResult, ScanResult._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ScanResult");

    private static final org.apache.thrift.protocol.TField RESULTS_FIELD_DESC = new org.apache.thrift.protocol.TField("results", org.apache.thrift.protocol.TType.LIST, (short) 1);

    private static final org.apache.thrift.protocol.TField MORE_FIELD_DESC = new org.apache.thrift.protocol.TField("more", org.apache.thrift.protocol.TType.BOOL, (short) 2);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new ScanResultStandardSchemeFactory());
        schemes.put(TupleScheme.class, new ScanResultTupleSchemeFactory());
    }

    public List<TKeyValue> results;

    public boolean more;

    @SuppressWarnings("all")
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        RESULTS((short) 1, "results"), MORE((short) 2, "more");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return RESULTS;
                case 2:
                    return MORE;
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

    private static final int __MORE_ISSET_ID = 0;

    private byte __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.RESULTS, new org.apache.thrift.meta_data.FieldMetaData("results", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TKeyValue.class))));
        tmpMap.put(_Fields.MORE, new org.apache.thrift.meta_data.FieldMetaData("more", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ScanResult.class, metaDataMap);
    }

    public ScanResult() {
    }

    public ScanResult(List<TKeyValue> results, boolean more) {
        this();
        this.results = results;
        this.more = more;
        setMoreIsSet(true);
    }

    public ScanResult(ScanResult other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetResults()) {
            List<TKeyValue> __this__results = new ArrayList<TKeyValue>();
            for (TKeyValue other_element : other.results) {
                __this__results.add(new TKeyValue(other_element));
            }
            this.results = __this__results;
        }
        this.more = other.more;
    }

    public ScanResult deepCopy() {
        return new ScanResult(this);
    }

    @Override
    public void clear() {
        this.results = null;
        setMoreIsSet(false);
        this.more = false;
    }

    public int getResultsSize() {
        return (this.results == null) ? 0 : this.results.size();
    }

    public java.util.Iterator<TKeyValue> getResultsIterator() {
        return (this.results == null) ? null : this.results.iterator();
    }

    public void addToResults(TKeyValue elem) {
        if (this.results == null) {
            this.results = new ArrayList<TKeyValue>();
        }
        this.results.add(elem);
    }

    public List<TKeyValue> getResults() {
        return this.results;
    }

    public ScanResult setResults(List<TKeyValue> results) {
        this.results = results;
        return this;
    }

    public void unsetResults() {
        this.results = null;
    }

    public boolean isSetResults() {
        return this.results != null;
    }

    public void setResultsIsSet(boolean value) {
        if (!value) {
            this.results = null;
        }
    }

    public boolean isMore() {
        return this.more;
    }

    public ScanResult setMore(boolean more) {
        this.more = more;
        setMoreIsSet(true);
        return this;
    }

    public void unsetMore() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __MORE_ISSET_ID);
    }

    public boolean isSetMore() {
        return EncodingUtils.testBit(__isset_bitfield, __MORE_ISSET_ID);
    }

    public void setMoreIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __MORE_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case RESULTS:
                if (value == null) {
                    unsetResults();
                } else {
                    setResults((List<TKeyValue>) value);
                }
                break;
            case MORE:
                if (value == null) {
                    unsetMore();
                } else {
                    setMore((Boolean) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case RESULTS:
                return getResults();
            case MORE:
                return Boolean.valueOf(isMore());
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case RESULTS:
                return isSetResults();
            case MORE:
                return isSetMore();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof ScanResult)
            return this.equals((ScanResult) that);
        return false;
    }

    public boolean equals(ScanResult that) {
        if (that == null)
            return false;
        boolean this_present_results = true && this.isSetResults();
        boolean that_present_results = true && that.isSetResults();
        if (this_present_results || that_present_results) {
            if (!(this_present_results && that_present_results))
                return false;
            if (!this.results.equals(that.results))
                return false;
        }
        boolean this_present_more = true;
        boolean that_present_more = true;
        if (this_present_more || that_present_more) {
            if (!(this_present_more && that_present_more))
                return false;
            if (this.more != that.more)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public int compareTo(ScanResult other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        ScanResult typedOther = (ScanResult) other;
        lastComparison = Boolean.valueOf(isSetResults()).compareTo(typedOther.isSetResults());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetResults()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.results, typedOther.results);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetMore()).compareTo(typedOther.isSetMore());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetMore()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.more, typedOther.more);
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
        StringBuilder sb = new StringBuilder("ScanResult(");
        boolean first = true;
        sb.append("results:");
        if (this.results == null) {
            sb.append("null");
        } else {
            sb.append(this.results);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("more:");
        sb.append(this.more);
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

    private static class ScanResultStandardSchemeFactory implements SchemeFactory {

        public ScanResultStandardScheme getScheme() {
            return new ScanResultStandardScheme();
        }
    }

    private static class ScanResultStandardScheme extends StandardScheme<ScanResult> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, ScanResult struct) throws org.apache.thrift.TException {
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
                                org.apache.thrift.protocol.TList _list16 = iprot.readListBegin();
                                struct.results = new ArrayList<TKeyValue>(_list16.size);
                                for (int _i17 = 0; _i17 < _list16.size; ++_i17) {
                                    TKeyValue _elem18;
                                    _elem18 = new TKeyValue();
                                    _elem18.read(iprot);
                                    struct.results.add(_elem18);
                                }
                                iprot.readListEnd();
                            }
                            struct.setResultsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
                            struct.more = iprot.readBool();
                            struct.setMoreIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, ScanResult struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.results != null) {
                oprot.writeFieldBegin(RESULTS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.results.size()));
                    for (TKeyValue _iter19 : struct.results) {
                        _iter19.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(MORE_FIELD_DESC);
            oprot.writeBool(struct.more);
            oprot.writeFieldEnd();
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class ScanResultTupleSchemeFactory implements SchemeFactory {

        public ScanResultTupleScheme getScheme() {
            return new ScanResultTupleScheme();
        }
    }

    private static class ScanResultTupleScheme extends TupleScheme<ScanResult> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, ScanResult struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetResults()) {
                optionals.set(0);
            }
            if (struct.isSetMore()) {
                optionals.set(1);
            }
            oprot.writeBitSet(optionals, 2);
            if (struct.isSetResults()) {
                {
                    oprot.writeI32(struct.results.size());
                    for (TKeyValue _iter20 : struct.results) {
                        _iter20.write(oprot);
                    }
                }
            }
            if (struct.isSetMore()) {
                oprot.writeBool(struct.more);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, ScanResult struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(2);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TList _list21 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.results = new ArrayList<TKeyValue>(_list21.size);
                    for (int _i22 = 0; _i22 < _list21.size; ++_i22) {
                        TKeyValue _elem23;
                        _elem23 = new TKeyValue();
                        _elem23.read(iprot);
                        struct.results.add(_elem23);
                    }
                }
                struct.setResultsIsSet(true);
            }
            if (incoming.get(1)) {
                struct.more = iprot.readBool();
                struct.setMoreIsSet(true);
            }
        }
    }
}
