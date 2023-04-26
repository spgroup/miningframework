package org.apache.hadoop.hive.metastore.api;

import org.apache.commons.lang.builder.HashCodeBuilder;
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

public class SkewedInfo implements org.apache.thrift.TBase<SkewedInfo, SkewedInfo._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("SkewedInfo");

    private static final org.apache.thrift.protocol.TField SKEWED_COL_NAMES_FIELD_DESC = new org.apache.thrift.protocol.TField("skewedColNames", org.apache.thrift.protocol.TType.LIST, (short) 1);

    private static final org.apache.thrift.protocol.TField SKEWED_COL_VALUES_FIELD_DESC = new org.apache.thrift.protocol.TField("skewedColValues", org.apache.thrift.protocol.TType.LIST, (short) 2);

    private static final org.apache.thrift.protocol.TField SKEWED_COL_VALUE_LOCATION_MAPS_FIELD_DESC = new org.apache.thrift.protocol.TField("skewedColValueLocationMaps", org.apache.thrift.protocol.TType.MAP, (short) 3);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new SkewedInfoStandardSchemeFactory());
        schemes.put(TupleScheme.class, new SkewedInfoTupleSchemeFactory());
    }

    private List<String> skewedColNames;

    private List<List<String>> skewedColValues;

    private Map<List<String>, String> skewedColValueLocationMaps;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        SKEWED_COL_NAMES((short) 1, "skewedColNames"), SKEWED_COL_VALUES((short) 2, "skewedColValues"), SKEWED_COL_VALUE_LOCATION_MAPS((short) 3, "skewedColValueLocationMaps");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return SKEWED_COL_NAMES;
                case 2:
                    return SKEWED_COL_VALUES;
                case 3:
                    return SKEWED_COL_VALUE_LOCATION_MAPS;
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
        tmpMap.put(_Fields.SKEWED_COL_NAMES, new org.apache.thrift.meta_data.FieldMetaData("skewedColNames", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
        tmpMap.put(_Fields.SKEWED_COL_VALUES, new org.apache.thrift.meta_data.FieldMetaData("skewedColValues", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)))));
        tmpMap.put(_Fields.SKEWED_COL_VALUE_LOCATION_MAPS, new org.apache.thrift.meta_data.FieldMetaData("skewedColValueLocationMaps", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)), new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(SkewedInfo.class, metaDataMap);
    }

    public SkewedInfo() {
    }

    public SkewedInfo(List<String> skewedColNames, List<List<String>> skewedColValues, Map<List<String>, String> skewedColValueLocationMaps) {
        this();
        this.skewedColNames = skewedColNames;
        this.skewedColValues = skewedColValues;
        this.skewedColValueLocationMaps = skewedColValueLocationMaps;
    }

    public SkewedInfo(SkewedInfo other) {
        if (other.isSetSkewedColNames()) {
            List<String> __this__skewedColNames = new ArrayList<String>();
            for (String other_element : other.skewedColNames) {
                __this__skewedColNames.add(other_element);
            }
            this.skewedColNames = __this__skewedColNames;
        }
        if (other.isSetSkewedColValues()) {
            List<List<String>> __this__skewedColValues = new ArrayList<List<String>>();
            for (List<String> other_element : other.skewedColValues) {
                List<String> __this__skewedColValues_copy = new ArrayList<String>();
                for (String other_element_element : other_element) {
                    __this__skewedColValues_copy.add(other_element_element);
                }
                __this__skewedColValues.add(__this__skewedColValues_copy);
            }
            this.skewedColValues = __this__skewedColValues;
        }
        if (other.isSetSkewedColValueLocationMaps()) {
            Map<List<String>, String> __this__skewedColValueLocationMaps = new HashMap<List<String>, String>();
            for (Map.Entry<List<String>, String> other_element : other.skewedColValueLocationMaps.entrySet()) {
                List<String> other_element_key = other_element.getKey();
                String other_element_value = other_element.getValue();
                List<String> __this__skewedColValueLocationMaps_copy_key = new ArrayList<String>();
                for (String other_element_key_element : other_element_key) {
                    __this__skewedColValueLocationMaps_copy_key.add(other_element_key_element);
                }
                String __this__skewedColValueLocationMaps_copy_value = other_element_value;
                __this__skewedColValueLocationMaps.put(__this__skewedColValueLocationMaps_copy_key, __this__skewedColValueLocationMaps_copy_value);
            }
            this.skewedColValueLocationMaps = __this__skewedColValueLocationMaps;
        }
    }

    public SkewedInfo deepCopy() {
        return new SkewedInfo(this);
    }

    @Override
    public void clear() {
        this.skewedColNames = null;
        this.skewedColValues = null;
        this.skewedColValueLocationMaps = null;
    }

    public int getSkewedColNamesSize() {
        return (this.skewedColNames == null) ? 0 : this.skewedColNames.size();
    }

    public java.util.Iterator<String> getSkewedColNamesIterator() {
        return (this.skewedColNames == null) ? null : this.skewedColNames.iterator();
    }

    public void addToSkewedColNames(String elem) {
        if (this.skewedColNames == null) {
            this.skewedColNames = new ArrayList<String>();
        }
        this.skewedColNames.add(elem);
    }

    public List<String> getSkewedColNames() {
        return this.skewedColNames;
    }

    public void setSkewedColNames(List<String> skewedColNames) {
        this.skewedColNames = skewedColNames;
    }

    public void unsetSkewedColNames() {
        this.skewedColNames = null;
    }

    public boolean isSetSkewedColNames() {
        return this.skewedColNames != null;
    }

    public void setSkewedColNamesIsSet(boolean value) {
        if (!value) {
            this.skewedColNames = null;
        }
    }

    public int getSkewedColValuesSize() {
        return (this.skewedColValues == null) ? 0 : this.skewedColValues.size();
    }

    public java.util.Iterator<List<String>> getSkewedColValuesIterator() {
        return (this.skewedColValues == null) ? null : this.skewedColValues.iterator();
    }

    public void addToSkewedColValues(List<String> elem) {
        if (this.skewedColValues == null) {
            this.skewedColValues = new ArrayList<List<String>>();
        }
        this.skewedColValues.add(elem);
    }

    public List<List<String>> getSkewedColValues() {
        return this.skewedColValues;
    }

    public void setSkewedColValues(List<List<String>> skewedColValues) {
        this.skewedColValues = skewedColValues;
    }

    public void unsetSkewedColValues() {
        this.skewedColValues = null;
    }

    public boolean isSetSkewedColValues() {
        return this.skewedColValues != null;
    }

    public void setSkewedColValuesIsSet(boolean value) {
        if (!value) {
            this.skewedColValues = null;
        }
    }

    public int getSkewedColValueLocationMapsSize() {
        return (this.skewedColValueLocationMaps == null) ? 0 : this.skewedColValueLocationMaps.size();
    }

    public void putToSkewedColValueLocationMaps(List<String> key, String val) {
        if (this.skewedColValueLocationMaps == null) {
            this.skewedColValueLocationMaps = new HashMap<List<String>, String>();
        }
        this.skewedColValueLocationMaps.put(key, val);
    }

    public Map<List<String>, String> getSkewedColValueLocationMaps() {
        return this.skewedColValueLocationMaps;
    }

    public void setSkewedColValueLocationMaps(Map<List<String>, String> skewedColValueLocationMaps) {
        this.skewedColValueLocationMaps = skewedColValueLocationMaps;
    }

    public void unsetSkewedColValueLocationMaps() {
        this.skewedColValueLocationMaps = null;
    }

    public boolean isSetSkewedColValueLocationMaps() {
        return this.skewedColValueLocationMaps != null;
    }

    public void setSkewedColValueLocationMapsIsSet(boolean value) {
        if (!value) {
            this.skewedColValueLocationMaps = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case SKEWED_COL_NAMES:
                if (value == null) {
                    unsetSkewedColNames();
                } else {
                    setSkewedColNames((List<String>) value);
                }
                break;
            case SKEWED_COL_VALUES:
                if (value == null) {
                    unsetSkewedColValues();
                } else {
                    setSkewedColValues((List<List<String>>) value);
                }
                break;
            case SKEWED_COL_VALUE_LOCATION_MAPS:
                if (value == null) {
                    unsetSkewedColValueLocationMaps();
                } else {
                    setSkewedColValueLocationMaps((Map<List<String>, String>) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case SKEWED_COL_NAMES:
                return getSkewedColNames();
            case SKEWED_COL_VALUES:
                return getSkewedColValues();
            case SKEWED_COL_VALUE_LOCATION_MAPS:
                return getSkewedColValueLocationMaps();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case SKEWED_COL_NAMES:
                return isSetSkewedColNames();
            case SKEWED_COL_VALUES:
                return isSetSkewedColValues();
            case SKEWED_COL_VALUE_LOCATION_MAPS:
                return isSetSkewedColValueLocationMaps();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof SkewedInfo)
            return this.equals((SkewedInfo) that);
        return false;
    }

    public boolean equals(SkewedInfo that) {
        if (that == null)
            return false;
        boolean this_present_skewedColNames = true && this.isSetSkewedColNames();
        boolean that_present_skewedColNames = true && that.isSetSkewedColNames();
        if (this_present_skewedColNames || that_present_skewedColNames) {
            if (!(this_present_skewedColNames && that_present_skewedColNames))
                return false;
            if (!this.skewedColNames.equals(that.skewedColNames))
                return false;
        }
        boolean this_present_skewedColValues = true && this.isSetSkewedColValues();
        boolean that_present_skewedColValues = true && that.isSetSkewedColValues();
        if (this_present_skewedColValues || that_present_skewedColValues) {
            if (!(this_present_skewedColValues && that_present_skewedColValues))
                return false;
            if (!this.skewedColValues.equals(that.skewedColValues))
                return false;
        }
        boolean this_present_skewedColValueLocationMaps = true && this.isSetSkewedColValueLocationMaps();
        boolean that_present_skewedColValueLocationMaps = true && that.isSetSkewedColValueLocationMaps();
        if (this_present_skewedColValueLocationMaps || that_present_skewedColValueLocationMaps) {
            if (!(this_present_skewedColValueLocationMaps && that_present_skewedColValueLocationMaps))
                return false;
            if (!this.skewedColValueLocationMaps.equals(that.skewedColValueLocationMaps))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        boolean present_skewedColNames = true && (isSetSkewedColNames());
        builder.append(present_skewedColNames);
        if (present_skewedColNames)
            builder.append(skewedColNames);
        boolean present_skewedColValues = true && (isSetSkewedColValues());
        builder.append(present_skewedColValues);
        if (present_skewedColValues)
            builder.append(skewedColValues);
        boolean present_skewedColValueLocationMaps = true && (isSetSkewedColValueLocationMaps());
        builder.append(present_skewedColValueLocationMaps);
        if (present_skewedColValueLocationMaps)
            builder.append(skewedColValueLocationMaps);
        return builder.toHashCode();
    }

    public int compareTo(SkewedInfo other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        SkewedInfo typedOther = (SkewedInfo) other;
        lastComparison = Boolean.valueOf(isSetSkewedColNames()).compareTo(typedOther.isSetSkewedColNames());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetSkewedColNames()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.skewedColNames, typedOther.skewedColNames);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetSkewedColValues()).compareTo(typedOther.isSetSkewedColValues());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetSkewedColValues()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.skewedColValues, typedOther.skewedColValues);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetSkewedColValueLocationMaps()).compareTo(typedOther.isSetSkewedColValueLocationMaps());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetSkewedColValueLocationMaps()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.skewedColValueLocationMaps, typedOther.skewedColValueLocationMaps);
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
        StringBuilder sb = new StringBuilder("SkewedInfo(");
        boolean first = true;
        sb.append("skewedColNames:");
        if (this.skewedColNames == null) {
            sb.append("null");
        } else {
            sb.append(this.skewedColNames);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("skewedColValues:");
        if (this.skewedColValues == null) {
            sb.append("null");
        } else {
            sb.append(this.skewedColValues);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("skewedColValueLocationMaps:");
        if (this.skewedColValueLocationMaps == null) {
            sb.append("null");
        } else {
            sb.append(this.skewedColValueLocationMaps);
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

    private static class SkewedInfoStandardSchemeFactory implements SchemeFactory {

        public SkewedInfoStandardScheme getScheme() {
            return new SkewedInfoStandardScheme();
        }
    }

    private static class SkewedInfoStandardScheme extends StandardScheme<SkewedInfo> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, SkewedInfo struct) throws org.apache.thrift.TException {
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
                                org.apache.thrift.protocol.TList _list114 = iprot.readListBegin();
                                struct.skewedColNames = new ArrayList<String>(_list114.size);
                                for (int _i115 = 0; _i115 < _list114.size; ++_i115) {
                                    String _elem116;
                                    _elem116 = iprot.readString();
                                    struct.skewedColNames.add(_elem116);
                                }
                                iprot.readListEnd();
                            }
                            struct.setSkewedColNamesIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list117 = iprot.readListBegin();
                                struct.skewedColValues = new ArrayList<List<String>>(_list117.size);
                                for (int _i118 = 0; _i118 < _list117.size; ++_i118) {
                                    List<String> _elem119;
                                    {
                                        org.apache.thrift.protocol.TList _list120 = iprot.readListBegin();
                                        _elem119 = new ArrayList<String>(_list120.size);
                                        for (int _i121 = 0; _i121 < _list120.size; ++_i121) {
                                            String _elem122;
                                            _elem122 = iprot.readString();
                                            _elem119.add(_elem122);
                                        }
                                        iprot.readListEnd();
                                    }
                                    struct.skewedColValues.add(_elem119);
                                }
                                iprot.readListEnd();
                            }
                            struct.setSkewedColValuesIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
                            {
                                org.apache.thrift.protocol.TMap _map123 = iprot.readMapBegin();
                                struct.skewedColValueLocationMaps = new HashMap<List<String>, String>(2 * _map123.size);
                                for (int _i124 = 0; _i124 < _map123.size; ++_i124) {
                                    List<String> _key125;
                                    String _val126;
                                    {
                                        org.apache.thrift.protocol.TList _list127 = iprot.readListBegin();
                                        _key125 = new ArrayList<String>(_list127.size);
                                        for (int _i128 = 0; _i128 < _list127.size; ++_i128) {
                                            String _elem129;
                                            _elem129 = iprot.readString();
                                            _key125.add(_elem129);
                                        }
                                        iprot.readListEnd();
                                    }
                                    _val126 = iprot.readString();
                                    struct.skewedColValueLocationMaps.put(_key125, _val126);
                                }
                                iprot.readMapEnd();
                            }
                            struct.setSkewedColValueLocationMapsIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, SkewedInfo struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.skewedColNames != null) {
                oprot.writeFieldBegin(SKEWED_COL_NAMES_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.skewedColNames.size()));
                    for (String _iter130 : struct.skewedColNames) {
                        oprot.writeString(_iter130);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.skewedColValues != null) {
                oprot.writeFieldBegin(SKEWED_COL_VALUES_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.LIST, struct.skewedColValues.size()));
                    for (List<String> _iter131 : struct.skewedColValues) {
                        {
                            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, _iter131.size()));
                            for (String _iter132 : _iter131) {
                                oprot.writeString(_iter132);
                            }
                            oprot.writeListEnd();
                        }
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.skewedColValueLocationMaps != null) {
                oprot.writeFieldBegin(SKEWED_COL_VALUE_LOCATION_MAPS_FIELD_DESC);
                {
                    oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.LIST, org.apache.thrift.protocol.TType.STRING, struct.skewedColValueLocationMaps.size()));
                    for (Map.Entry<List<String>, String> _iter133 : struct.skewedColValueLocationMaps.entrySet()) {
                        {
                            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, _iter133.getKey().size()));
                            for (String _iter134 : _iter133.getKey()) {
                                oprot.writeString(_iter134);
                            }
                            oprot.writeListEnd();
                        }
                        oprot.writeString(_iter133.getValue());
                    }
                    oprot.writeMapEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class SkewedInfoTupleSchemeFactory implements SchemeFactory {

        public SkewedInfoTupleScheme getScheme() {
            return new SkewedInfoTupleScheme();
        }
    }

    private static class SkewedInfoTupleScheme extends TupleScheme<SkewedInfo> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, SkewedInfo struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetSkewedColNames()) {
                optionals.set(0);
            }
            if (struct.isSetSkewedColValues()) {
                optionals.set(1);
            }
            if (struct.isSetSkewedColValueLocationMaps()) {
                optionals.set(2);
            }
            oprot.writeBitSet(optionals, 3);
            if (struct.isSetSkewedColNames()) {
                {
                    oprot.writeI32(struct.skewedColNames.size());
                    for (String _iter135 : struct.skewedColNames) {
                        oprot.writeString(_iter135);
                    }
                }
            }
            if (struct.isSetSkewedColValues()) {
                {
                    oprot.writeI32(struct.skewedColValues.size());
                    for (List<String> _iter136 : struct.skewedColValues) {
                        {
                            oprot.writeI32(_iter136.size());
                            for (String _iter137 : _iter136) {
                                oprot.writeString(_iter137);
                            }
                        }
                    }
                }
            }
            if (struct.isSetSkewedColValueLocationMaps()) {
                {
                    oprot.writeI32(struct.skewedColValueLocationMaps.size());
                    for (Map.Entry<List<String>, String> _iter138 : struct.skewedColValueLocationMaps.entrySet()) {
                        {
                            oprot.writeI32(_iter138.getKey().size());
                            for (String _iter139 : _iter138.getKey()) {
                                oprot.writeString(_iter139);
                            }
                        }
                        oprot.writeString(_iter138.getValue());
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, SkewedInfo struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(3);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TList _list140 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.skewedColNames = new ArrayList<String>(_list140.size);
                    for (int _i141 = 0; _i141 < _list140.size; ++_i141) {
                        String _elem142;
                        _elem142 = iprot.readString();
                        struct.skewedColNames.add(_elem142);
                    }
                }
                struct.setSkewedColNamesIsSet(true);
            }
            if (incoming.get(1)) {
                {
                    org.apache.thrift.protocol.TList _list143 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.LIST, iprot.readI32());
                    struct.skewedColValues = new ArrayList<List<String>>(_list143.size);
                    for (int _i144 = 0; _i144 < _list143.size; ++_i144) {
                        List<String> _elem145;
                        {
                            org.apache.thrift.protocol.TList _list146 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                            _elem145 = new ArrayList<String>(_list146.size);
                            for (int _i147 = 0; _i147 < _list146.size; ++_i147) {
                                String _elem148;
                                _elem148 = iprot.readString();
                                _elem145.add(_elem148);
                            }
                        }
                        struct.skewedColValues.add(_elem145);
                    }
                }
                struct.setSkewedColValuesIsSet(true);
            }
            if (incoming.get(2)) {
                {
                    org.apache.thrift.protocol.TMap _map149 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.LIST, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.skewedColValueLocationMaps = new HashMap<List<String>, String>(2 * _map149.size);
                    for (int _i150 = 0; _i150 < _map149.size; ++_i150) {
                        List<String> _key151;
                        String _val152;
                        {
                            org.apache.thrift.protocol.TList _list153 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                            _key151 = new ArrayList<String>(_list153.size);
                            for (int _i154 = 0; _i154 < _list153.size; ++_i154) {
                                String _elem155;
                                _elem155 = iprot.readString();
                                _key151.add(_elem155);
                            }
                        }
                        _val152 = iprot.readString();
                        struct.skewedColValueLocationMaps.put(_key151, _val152);
                    }
                }
                struct.setSkewedColValueLocationMapsIsSet(true);
            }
        }
    }
}
