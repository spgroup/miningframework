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

@SuppressWarnings("all")
public class MultiScanResult implements org.apache.thrift.TBase<MultiScanResult, MultiScanResult._Fields>, java.io.Serializable, Cloneable, Comparable<MultiScanResult> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MultiScanResult");

    private static final org.apache.thrift.protocol.TField RESULTS_FIELD_DESC = new org.apache.thrift.protocol.TField("results", org.apache.thrift.protocol.TType.LIST, (short) 1);

    private static final org.apache.thrift.protocol.TField FAILURES_FIELD_DESC = new org.apache.thrift.protocol.TField("failures", org.apache.thrift.protocol.TType.MAP, (short) 2);

    private static final org.apache.thrift.protocol.TField FULL_SCANS_FIELD_DESC = new org.apache.thrift.protocol.TField("fullScans", org.apache.thrift.protocol.TType.LIST, (short) 3);

    private static final org.apache.thrift.protocol.TField PART_SCAN_FIELD_DESC = new org.apache.thrift.protocol.TField("partScan", org.apache.thrift.protocol.TType.STRUCT, (short) 4);

    private static final org.apache.thrift.protocol.TField PART_NEXT_KEY_FIELD_DESC = new org.apache.thrift.protocol.TField("partNextKey", org.apache.thrift.protocol.TType.STRUCT, (short) 5);

    private static final org.apache.thrift.protocol.TField PART_NEXT_KEY_INCLUSIVE_FIELD_DESC = new org.apache.thrift.protocol.TField("partNextKeyInclusive", org.apache.thrift.protocol.TType.BOOL, (short) 6);

    private static final org.apache.thrift.protocol.TField MORE_FIELD_DESC = new org.apache.thrift.protocol.TField("more", org.apache.thrift.protocol.TType.BOOL, (short) 7);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new MultiScanResultStandardSchemeFactory());
        schemes.put(TupleScheme.class, new MultiScanResultTupleSchemeFactory());
    }

    public List<TKeyValue> results;

    public Map<TKeyExtent, List<TRange>> failures;

    public List<TKeyExtent> fullScans;

    public TKeyExtent partScan;

    public TKey partNextKey;

    public boolean partNextKeyInclusive;

    public boolean more;

    @SuppressWarnings("all")
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        RESULTS((short) 1, "results"),
        FAILURES((short) 2, "failures"),
        FULL_SCANS((short) 3, "fullScans"),
        PART_SCAN((short) 4, "partScan"),
        PART_NEXT_KEY((short) 5, "partNextKey"),
        PART_NEXT_KEY_INCLUSIVE((short) 6, "partNextKeyInclusive"),
        MORE((short) 7, "more");

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
                    return FAILURES;
                case 3:
                    return FULL_SCANS;
                case 4:
                    return PART_SCAN;
                case 5:
                    return PART_NEXT_KEY;
                case 6:
                    return PART_NEXT_KEY_INCLUSIVE;
                case 7:
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

    private static final int __PARTNEXTKEYINCLUSIVE_ISSET_ID = 0;

    private static final int __MORE_ISSET_ID = 1;

    private byte __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.RESULTS, new org.apache.thrift.meta_data.FieldMetaData("results", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TKeyValue.class))));
        tmpMap.put(_Fields.FAILURES, new org.apache.thrift.meta_data.FieldMetaData("failures", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.MAP, "ScanBatch")));
        tmpMap.put(_Fields.FULL_SCANS, new org.apache.thrift.meta_data.FieldMetaData("fullScans", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TKeyExtent.class))));
        tmpMap.put(_Fields.PART_SCAN, new org.apache.thrift.meta_data.FieldMetaData("partScan", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TKeyExtent.class)));
        tmpMap.put(_Fields.PART_NEXT_KEY, new org.apache.thrift.meta_data.FieldMetaData("partNextKey", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TKey.class)));
        tmpMap.put(_Fields.PART_NEXT_KEY_INCLUSIVE, new org.apache.thrift.meta_data.FieldMetaData("partNextKeyInclusive", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
        tmpMap.put(_Fields.MORE, new org.apache.thrift.meta_data.FieldMetaData("more", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MultiScanResult.class, metaDataMap);
    }

    public MultiScanResult() {
    }

    public MultiScanResult(List<TKeyValue> results, Map<TKeyExtent, List<TRange>> failures, List<TKeyExtent> fullScans, TKeyExtent partScan, TKey partNextKey, boolean partNextKeyInclusive, boolean more) {
        this();
        this.results = results;
        this.failures = failures;
        this.fullScans = fullScans;
        this.partScan = partScan;
        this.partNextKey = partNextKey;
        this.partNextKeyInclusive = partNextKeyInclusive;
        setPartNextKeyInclusiveIsSet(true);
        this.more = more;
        setMoreIsSet(true);
    }

    public MultiScanResult(MultiScanResult other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetResults()) {
            List<TKeyValue> __this__results = new ArrayList<TKeyValue>(other.results.size());
            for (TKeyValue other_element : other.results) {
                __this__results.add(new TKeyValue(other_element));
            }
            this.results = __this__results;
        }
        if (other.isSetFailures()) {
            this.failures = other.failures;
        }
        if (other.isSetFullScans()) {
            List<TKeyExtent> __this__fullScans = new ArrayList<TKeyExtent>(other.fullScans.size());
            for (TKeyExtent other_element : other.fullScans) {
                __this__fullScans.add(new TKeyExtent(other_element));
            }
            this.fullScans = __this__fullScans;
        }
        if (other.isSetPartScan()) {
            this.partScan = new TKeyExtent(other.partScan);
        }
        if (other.isSetPartNextKey()) {
            this.partNextKey = new TKey(other.partNextKey);
        }
        this.partNextKeyInclusive = other.partNextKeyInclusive;
        this.more = other.more;
    }

    public MultiScanResult deepCopy() {
        return new MultiScanResult(this);
    }

    @Override
    public void clear() {
        this.results = null;
        this.failures = null;
        this.fullScans = null;
        this.partScan = null;
        this.partNextKey = null;
        setPartNextKeyInclusiveIsSet(false);
        this.partNextKeyInclusive = false;
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

    public MultiScanResult setResults(List<TKeyValue> results) {
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

    public int getFailuresSize() {
        return (this.failures == null) ? 0 : this.failures.size();
    }

    public void putToFailures(TKeyExtent key, List<TRange> val) {
        if (this.failures == null) {
            this.failures = new HashMap<TKeyExtent, List<TRange>>();
        }
        this.failures.put(key, val);
    }

    public Map<TKeyExtent, List<TRange>> getFailures() {
        return this.failures;
    }

    public MultiScanResult setFailures(Map<TKeyExtent, List<TRange>> failures) {
        this.failures = failures;
        return this;
    }

    public void unsetFailures() {
        this.failures = null;
    }

    public boolean isSetFailures() {
        return this.failures != null;
    }

    public void setFailuresIsSet(boolean value) {
        if (!value) {
            this.failures = null;
        }
    }

    public int getFullScansSize() {
        return (this.fullScans == null) ? 0 : this.fullScans.size();
    }

    public java.util.Iterator<TKeyExtent> getFullScansIterator() {
        return (this.fullScans == null) ? null : this.fullScans.iterator();
    }

    public void addToFullScans(TKeyExtent elem) {
        if (this.fullScans == null) {
            this.fullScans = new ArrayList<TKeyExtent>();
        }
        this.fullScans.add(elem);
    }

    public List<TKeyExtent> getFullScans() {
        return this.fullScans;
    }

    public MultiScanResult setFullScans(List<TKeyExtent> fullScans) {
        this.fullScans = fullScans;
        return this;
    }

    public void unsetFullScans() {
        this.fullScans = null;
    }

    public boolean isSetFullScans() {
        return this.fullScans != null;
    }

    public void setFullScansIsSet(boolean value) {
        if (!value) {
            this.fullScans = null;
        }
    }

    public TKeyExtent getPartScan() {
        return this.partScan;
    }

    public MultiScanResult setPartScan(TKeyExtent partScan) {
        this.partScan = partScan;
        return this;
    }

    public void unsetPartScan() {
        this.partScan = null;
    }

    public boolean isSetPartScan() {
        return this.partScan != null;
    }

    public void setPartScanIsSet(boolean value) {
        if (!value) {
            this.partScan = null;
        }
    }

    public TKey getPartNextKey() {
        return this.partNextKey;
    }

    public MultiScanResult setPartNextKey(TKey partNextKey) {
        this.partNextKey = partNextKey;
        return this;
    }

    public void unsetPartNextKey() {
        this.partNextKey = null;
    }

    public boolean isSetPartNextKey() {
        return this.partNextKey != null;
    }

    public void setPartNextKeyIsSet(boolean value) {
        if (!value) {
            this.partNextKey = null;
        }
    }

    public boolean isPartNextKeyInclusive() {
        return this.partNextKeyInclusive;
    }

    public MultiScanResult setPartNextKeyInclusive(boolean partNextKeyInclusive) {
        this.partNextKeyInclusive = partNextKeyInclusive;
        setPartNextKeyInclusiveIsSet(true);
        return this;
    }

    public void unsetPartNextKeyInclusive() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __PARTNEXTKEYINCLUSIVE_ISSET_ID);
    }

    public boolean isSetPartNextKeyInclusive() {
        return EncodingUtils.testBit(__isset_bitfield, __PARTNEXTKEYINCLUSIVE_ISSET_ID);
    }

    public void setPartNextKeyInclusiveIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __PARTNEXTKEYINCLUSIVE_ISSET_ID, value);
    }

    public boolean isMore() {
        return this.more;
    }

    public MultiScanResult setMore(boolean more) {
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
            case FAILURES:
                if (value == null) {
                    unsetFailures();
                } else {
                    setFailures((Map<TKeyExtent, List<TRange>>) value);
                }
                break;
            case FULL_SCANS:
                if (value == null) {
                    unsetFullScans();
                } else {
                    setFullScans((List<TKeyExtent>) value);
                }
                break;
            case PART_SCAN:
                if (value == null) {
                    unsetPartScan();
                } else {
                    setPartScan((TKeyExtent) value);
                }
                break;
            case PART_NEXT_KEY:
                if (value == null) {
                    unsetPartNextKey();
                } else {
                    setPartNextKey((TKey) value);
                }
                break;
            case PART_NEXT_KEY_INCLUSIVE:
                if (value == null) {
                    unsetPartNextKeyInclusive();
                } else {
                    setPartNextKeyInclusive((Boolean) value);
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
            case FAILURES:
                return getFailures();
            case FULL_SCANS:
                return getFullScans();
            case PART_SCAN:
                return getPartScan();
            case PART_NEXT_KEY:
                return getPartNextKey();
            case PART_NEXT_KEY_INCLUSIVE:
                return Boolean.valueOf(isPartNextKeyInclusive());
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
            case FAILURES:
                return isSetFailures();
            case FULL_SCANS:
                return isSetFullScans();
            case PART_SCAN:
                return isSetPartScan();
            case PART_NEXT_KEY:
                return isSetPartNextKey();
            case PART_NEXT_KEY_INCLUSIVE:
                return isSetPartNextKeyInclusive();
            case MORE:
                return isSetMore();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof MultiScanResult)
            return this.equals((MultiScanResult) that);
        return false;
    }

    public boolean equals(MultiScanResult that) {
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
        boolean this_present_failures = true && this.isSetFailures();
        boolean that_present_failures = true && that.isSetFailures();
        if (this_present_failures || that_present_failures) {
            if (!(this_present_failures && that_present_failures))
                return false;
            if (!this.failures.equals(that.failures))
                return false;
        }
        boolean this_present_fullScans = true && this.isSetFullScans();
        boolean that_present_fullScans = true && that.isSetFullScans();
        if (this_present_fullScans || that_present_fullScans) {
            if (!(this_present_fullScans && that_present_fullScans))
                return false;
            if (!this.fullScans.equals(that.fullScans))
                return false;
        }
        boolean this_present_partScan = true && this.isSetPartScan();
        boolean that_present_partScan = true && that.isSetPartScan();
        if (this_present_partScan || that_present_partScan) {
            if (!(this_present_partScan && that_present_partScan))
                return false;
            if (!this.partScan.equals(that.partScan))
                return false;
        }
        boolean this_present_partNextKey = true && this.isSetPartNextKey();
        boolean that_present_partNextKey = true && that.isSetPartNextKey();
        if (this_present_partNextKey || that_present_partNextKey) {
            if (!(this_present_partNextKey && that_present_partNextKey))
                return false;
            if (!this.partNextKey.equals(that.partNextKey))
                return false;
        }
        boolean this_present_partNextKeyInclusive = true;
        boolean that_present_partNextKeyInclusive = true;
        if (this_present_partNextKeyInclusive || that_present_partNextKeyInclusive) {
            if (!(this_present_partNextKeyInclusive && that_present_partNextKeyInclusive))
                return false;
            if (this.partNextKeyInclusive != that.partNextKeyInclusive)
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

    @Override
    public int compareTo(MultiScanResult other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetResults()).compareTo(other.isSetResults());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetResults()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.results, other.results);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetFailures()).compareTo(other.isSetFailures());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetFailures()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.failures, other.failures);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetFullScans()).compareTo(other.isSetFullScans());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetFullScans()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.fullScans, other.fullScans);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetPartScan()).compareTo(other.isSetPartScan());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetPartScan()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.partScan, other.partScan);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetPartNextKey()).compareTo(other.isSetPartNextKey());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetPartNextKey()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.partNextKey, other.partNextKey);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetPartNextKeyInclusive()).compareTo(other.isSetPartNextKeyInclusive());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetPartNextKeyInclusive()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.partNextKeyInclusive, other.partNextKeyInclusive);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetMore()).compareTo(other.isSetMore());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetMore()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.more, other.more);
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
        StringBuilder sb = new StringBuilder("MultiScanResult(");
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
        sb.append("failures:");
        if (this.failures == null) {
            sb.append("null");
        } else {
            sb.append(this.failures);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("fullScans:");
        if (this.fullScans == null) {
            sb.append("null");
        } else {
            sb.append(this.fullScans);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("partScan:");
        if (this.partScan == null) {
            sb.append("null");
        } else {
            sb.append(this.partScan);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("partNextKey:");
        if (this.partNextKey == null) {
            sb.append("null");
        } else {
            sb.append(this.partNextKey);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("partNextKeyInclusive:");
        sb.append(this.partNextKeyInclusive);
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
        if (partScan != null) {
            partScan.validate();
        }
        if (partNextKey != null) {
            partNextKey.validate();
        }
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

    private static class MultiScanResultStandardSchemeFactory implements SchemeFactory {

        public MultiScanResultStandardScheme getScheme() {
            return new MultiScanResultStandardScheme();
        }
    }

    private static class MultiScanResultStandardScheme extends StandardScheme<MultiScanResult> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, MultiScanResult struct) throws org.apache.thrift.TException {
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
                                org.apache.thrift.protocol.TList _list24 = iprot.readListBegin();
                                struct.results = new ArrayList<TKeyValue>(_list24.size);
                                for (int _i25 = 0; _i25 < _list24.size; ++_i25) {
                                    TKeyValue _elem26;
                                    _elem26 = new TKeyValue();
                                    _elem26.read(iprot);
                                    struct.results.add(_elem26);
                                }
                                iprot.readListEnd();
                            }
                            struct.setResultsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
                            {
                                org.apache.thrift.protocol.TMap _map27 = iprot.readMapBegin();
                                struct.failures = new HashMap<TKeyExtent, List<TRange>>(2 * _map27.size);
                                for (int _i28 = 0; _i28 < _map27.size; ++_i28) {
                                    TKeyExtent _key29;
                                    List<TRange> _val30;
                                    _key29 = new TKeyExtent();
                                    _key29.read(iprot);
                                    {
                                        org.apache.thrift.protocol.TList _list31 = iprot.readListBegin();
                                        _val30 = new ArrayList<TRange>(_list31.size);
                                        for (int _i32 = 0; _i32 < _list31.size; ++_i32) {
                                            TRange _elem33;
                                            _elem33 = new TRange();
                                            _elem33.read(iprot);
                                            _val30.add(_elem33);
                                        }
                                        iprot.readListEnd();
                                    }
                                    struct.failures.put(_key29, _val30);
                                }
                                iprot.readMapEnd();
                            }
                            struct.setFailuresIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list34 = iprot.readListBegin();
                                struct.fullScans = new ArrayList<TKeyExtent>(_list34.size);
                                for (int _i35 = 0; _i35 < _list34.size; ++_i35) {
                                    TKeyExtent _elem36;
                                    _elem36 = new TKeyExtent();
                                    _elem36.read(iprot);
                                    struct.fullScans.add(_elem36);
                                }
                                iprot.readListEnd();
                            }
                            struct.setFullScansIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.partScan = new TKeyExtent();
                            struct.partScan.read(iprot);
                            struct.setPartScanIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 5:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.partNextKey = new TKey();
                            struct.partNextKey.read(iprot);
                            struct.setPartNextKeyIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 6:
                        if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
                            struct.partNextKeyInclusive = iprot.readBool();
                            struct.setPartNextKeyInclusiveIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 7:
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, MultiScanResult struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.results != null) {
                oprot.writeFieldBegin(RESULTS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.results.size()));
                    for (TKeyValue _iter37 : struct.results) {
                        _iter37.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.failures != null) {
                oprot.writeFieldBegin(FAILURES_FIELD_DESC);
                {
                    oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRUCT, org.apache.thrift.protocol.TType.LIST, struct.failures.size()));
                    for (Map.Entry<TKeyExtent, List<TRange>> _iter38 : struct.failures.entrySet()) {
                        _iter38.getKey().write(oprot);
                        {
                            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, _iter38.getValue().size()));
                            for (TRange _iter39 : _iter38.getValue()) {
                                _iter39.write(oprot);
                            }
                            oprot.writeListEnd();
                        }
                    }
                    oprot.writeMapEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.fullScans != null) {
                oprot.writeFieldBegin(FULL_SCANS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.fullScans.size()));
                    for (TKeyExtent _iter40 : struct.fullScans) {
                        _iter40.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.partScan != null) {
                oprot.writeFieldBegin(PART_SCAN_FIELD_DESC);
                struct.partScan.write(oprot);
                oprot.writeFieldEnd();
            }
            if (struct.partNextKey != null) {
                oprot.writeFieldBegin(PART_NEXT_KEY_FIELD_DESC);
                struct.partNextKey.write(oprot);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(PART_NEXT_KEY_INCLUSIVE_FIELD_DESC);
            oprot.writeBool(struct.partNextKeyInclusive);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(MORE_FIELD_DESC);
            oprot.writeBool(struct.more);
            oprot.writeFieldEnd();
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class MultiScanResultTupleSchemeFactory implements SchemeFactory {

        public MultiScanResultTupleScheme getScheme() {
            return new MultiScanResultTupleScheme();
        }
    }

    private static class MultiScanResultTupleScheme extends TupleScheme<MultiScanResult> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, MultiScanResult struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetResults()) {
                optionals.set(0);
            }
            if (struct.isSetFailures()) {
                optionals.set(1);
            }
            if (struct.isSetFullScans()) {
                optionals.set(2);
            }
            if (struct.isSetPartScan()) {
                optionals.set(3);
            }
            if (struct.isSetPartNextKey()) {
                optionals.set(4);
            }
            if (struct.isSetPartNextKeyInclusive()) {
                optionals.set(5);
            }
            if (struct.isSetMore()) {
                optionals.set(6);
            }
            oprot.writeBitSet(optionals, 7);
            if (struct.isSetResults()) {
                {
                    oprot.writeI32(struct.results.size());
                    for (TKeyValue _iter41 : struct.results) {
                        _iter41.write(oprot);
                    }
                }
            }
            if (struct.isSetFailures()) {
                {
                    oprot.writeI32(struct.failures.size());
                    for (Map.Entry<TKeyExtent, List<TRange>> _iter42 : struct.failures.entrySet()) {
                        _iter42.getKey().write(oprot);
                        {
                            oprot.writeI32(_iter42.getValue().size());
                            for (TRange _iter43 : _iter42.getValue()) {
                                _iter43.write(oprot);
                            }
                        }
                    }
                }
            }
            if (struct.isSetFullScans()) {
                {
                    oprot.writeI32(struct.fullScans.size());
                    for (TKeyExtent _iter44 : struct.fullScans) {
                        _iter44.write(oprot);
                    }
                }
            }
            if (struct.isSetPartScan()) {
                struct.partScan.write(oprot);
            }
            if (struct.isSetPartNextKey()) {
                struct.partNextKey.write(oprot);
            }
            if (struct.isSetPartNextKeyInclusive()) {
                oprot.writeBool(struct.partNextKeyInclusive);
            }
            if (struct.isSetMore()) {
                oprot.writeBool(struct.more);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, MultiScanResult struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(7);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TList _list45 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.results = new ArrayList<TKeyValue>(_list45.size);
                    for (int _i46 = 0; _i46 < _list45.size; ++_i46) {
                        TKeyValue _elem47;
                        _elem47 = new TKeyValue();
                        _elem47.read(iprot);
                        struct.results.add(_elem47);
                    }
                }
                struct.setResultsIsSet(true);
            }
            if (incoming.get(1)) {
                {
                    org.apache.thrift.protocol.TMap _map48 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRUCT, org.apache.thrift.protocol.TType.LIST, iprot.readI32());
                    struct.failures = new HashMap<TKeyExtent, List<TRange>>(2 * _map48.size);
                    for (int _i49 = 0; _i49 < _map48.size; ++_i49) {
                        TKeyExtent _key50;
                        List<TRange> _val51;
                        _key50 = new TKeyExtent();
                        _key50.read(iprot);
                        {
                            org.apache.thrift.protocol.TList _list52 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                            _val51 = new ArrayList<TRange>(_list52.size);
                            for (int _i53 = 0; _i53 < _list52.size; ++_i53) {
                                TRange _elem54;
                                _elem54 = new TRange();
                                _elem54.read(iprot);
                                _val51.add(_elem54);
                            }
                        }
                        struct.failures.put(_key50, _val51);
                    }
                }
                struct.setFailuresIsSet(true);
            }
            if (incoming.get(2)) {
                {
                    org.apache.thrift.protocol.TList _list55 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.fullScans = new ArrayList<TKeyExtent>(_list55.size);
                    for (int _i56 = 0; _i56 < _list55.size; ++_i56) {
                        TKeyExtent _elem57;
                        _elem57 = new TKeyExtent();
                        _elem57.read(iprot);
                        struct.fullScans.add(_elem57);
                    }
                }
                struct.setFullScansIsSet(true);
            }
            if (incoming.get(3)) {
                struct.partScan = new TKeyExtent();
                struct.partScan.read(iprot);
                struct.setPartScanIsSet(true);
            }
            if (incoming.get(4)) {
                struct.partNextKey = new TKey();
                struct.partNextKey.read(iprot);
                struct.setPartNextKeyIsSet(true);
            }
            if (incoming.get(5)) {
                struct.partNextKeyInclusive = iprot.readBool();
                struct.setPartNextKeyInclusiveIsSet(true);
            }
            if (incoming.get(6)) {
                struct.more = iprot.readBool();
                struct.setMoreIsSet(true);
            }
        }
    }
}
