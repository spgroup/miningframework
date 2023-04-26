package backtype.storm.generated;

import org.apache.commons.lang.builder.HashCodeBuilder;
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

public class ExecutorStats implements org.apache.thrift.TBase<ExecutorStats, ExecutorStats._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ExecutorStats");

    private static final org.apache.thrift.protocol.TField EMITTED_FIELD_DESC = new org.apache.thrift.protocol.TField("emitted", org.apache.thrift.protocol.TType.MAP, (short) 1);

    private static final org.apache.thrift.protocol.TField TRANSFERRED_FIELD_DESC = new org.apache.thrift.protocol.TField("transferred", org.apache.thrift.protocol.TType.MAP, (short) 2);

    private static final org.apache.thrift.protocol.TField SPECIFIC_FIELD_DESC = new org.apache.thrift.protocol.TField("specific", org.apache.thrift.protocol.TType.STRUCT, (short) 3);

    private static final org.apache.thrift.protocol.TField RATE_FIELD_DESC = new org.apache.thrift.protocol.TField("rate", org.apache.thrift.protocol.TType.DOUBLE, (short) 4);

    private Map<String, Map<String, Long>> emitted;

    private Map<String, Map<String, Long>> transferred;

    private ExecutorSpecificStats specific;

    private double rate;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        EMITTED((short) 1, "emitted"), TRANSFERRED((short) 2, "transferred"), SPECIFIC((short) 3, "specific"), RATE((short) 4, "rate");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return EMITTED;
                case 2:
                    return TRANSFERRED;
                case 3:
                    return SPECIFIC;
                case 4:
                    return RATE;
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

    private static final int __RATE_ISSET_ID = 0;

    private BitSet __isset_bit_vector = new BitSet(1);

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.EMITTED, new org.apache.thrift.meta_data.FieldMetaData("emitted", org.apache.thrift.TFieldRequirementType.REQUIRED, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)))));
        tmpMap.put(_Fields.TRANSFERRED, new org.apache.thrift.meta_data.FieldMetaData("transferred", org.apache.thrift.TFieldRequirementType.REQUIRED, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)))));
        tmpMap.put(_Fields.SPECIFIC, new org.apache.thrift.meta_data.FieldMetaData("specific", org.apache.thrift.TFieldRequirementType.REQUIRED, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, ExecutorSpecificStats.class)));
        tmpMap.put(_Fields.RATE, new org.apache.thrift.meta_data.FieldMetaData("rate", org.apache.thrift.TFieldRequirementType.REQUIRED, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ExecutorStats.class, metaDataMap);
    }

    public ExecutorStats() {
    }

    public ExecutorStats(Map<String, Map<String, Long>> emitted, Map<String, Map<String, Long>> transferred, ExecutorSpecificStats specific, double rate) {
        this();
        this.emitted = emitted;
        this.transferred = transferred;
        this.specific = specific;
        this.rate = rate;
        set_rate_isSet(true);
    }

    public ExecutorStats(ExecutorStats other) {
        __isset_bit_vector.clear();
        __isset_bit_vector.or(other.__isset_bit_vector);
        if (other.is_set_emitted()) {
            Map<String, Map<String, Long>> __this__emitted = new HashMap<String, Map<String, Long>>();
            for (Map.Entry<String, Map<String, Long>> other_element : other.emitted.entrySet()) {
                String other_element_key = other_element.getKey();
                Map<String, Long> other_element_value = other_element.getValue();
                String __this__emitted_copy_key = other_element_key;
                Map<String, Long> __this__emitted_copy_value = new HashMap<String, Long>();
                for (Map.Entry<String, Long> other_element_value_element : other_element_value.entrySet()) {
                    String other_element_value_element_key = other_element_value_element.getKey();
                    Long other_element_value_element_value = other_element_value_element.getValue();
                    String __this__emitted_copy_value_copy_key = other_element_value_element_key;
                    Long __this__emitted_copy_value_copy_value = other_element_value_element_value;
                    __this__emitted_copy_value.put(__this__emitted_copy_value_copy_key, __this__emitted_copy_value_copy_value);
                }
                __this__emitted.put(__this__emitted_copy_key, __this__emitted_copy_value);
            }
            this.emitted = __this__emitted;
        }
        if (other.is_set_transferred()) {
            Map<String, Map<String, Long>> __this__transferred = new HashMap<String, Map<String, Long>>();
            for (Map.Entry<String, Map<String, Long>> other_element : other.transferred.entrySet()) {
                String other_element_key = other_element.getKey();
                Map<String, Long> other_element_value = other_element.getValue();
                String __this__transferred_copy_key = other_element_key;
                Map<String, Long> __this__transferred_copy_value = new HashMap<String, Long>();
                for (Map.Entry<String, Long> other_element_value_element : other_element_value.entrySet()) {
                    String other_element_value_element_key = other_element_value_element.getKey();
                    Long other_element_value_element_value = other_element_value_element.getValue();
                    String __this__transferred_copy_value_copy_key = other_element_value_element_key;
                    Long __this__transferred_copy_value_copy_value = other_element_value_element_value;
                    __this__transferred_copy_value.put(__this__transferred_copy_value_copy_key, __this__transferred_copy_value_copy_value);
                }
                __this__transferred.put(__this__transferred_copy_key, __this__transferred_copy_value);
            }
            this.transferred = __this__transferred;
        }
        if (other.is_set_specific()) {
            this.specific = new ExecutorSpecificStats(other.specific);
        }
        this.rate = other.rate;
    }

    public ExecutorStats deepCopy() {
        return new ExecutorStats(this);
    }

    @Override
    public void clear() {
        this.emitted = null;
        this.transferred = null;
        this.specific = null;
        set_rate_isSet(false);
        this.rate = 0.0;
    }

    public int get_emitted_size() {
        return (this.emitted == null) ? 0 : this.emitted.size();
    }

    public void put_to_emitted(String key, Map<String, Long> val) {
        if (this.emitted == null) {
            this.emitted = new HashMap<String, Map<String, Long>>();
        }
        this.emitted.put(key, val);
    }

    public Map<String, Map<String, Long>> get_emitted() {
        return this.emitted;
    }

    public void set_emitted(Map<String, Map<String, Long>> emitted) {
        this.emitted = emitted;
    }

    public void unset_emitted() {
        this.emitted = null;
    }

    public boolean is_set_emitted() {
        return this.emitted != null;
    }

    public void set_emitted_isSet(boolean value) {
        if (!value) {
            this.emitted = null;
        }
    }

    public int get_transferred_size() {
        return (this.transferred == null) ? 0 : this.transferred.size();
    }

    public void put_to_transferred(String key, Map<String, Long> val) {
        if (this.transferred == null) {
            this.transferred = new HashMap<String, Map<String, Long>>();
        }
        this.transferred.put(key, val);
    }

    public Map<String, Map<String, Long>> get_transferred() {
        return this.transferred;
    }

    public void set_transferred(Map<String, Map<String, Long>> transferred) {
        this.transferred = transferred;
    }

    public void unset_transferred() {
        this.transferred = null;
    }

    public boolean is_set_transferred() {
        return this.transferred != null;
    }

    public void set_transferred_isSet(boolean value) {
        if (!value) {
            this.transferred = null;
        }
    }

    public ExecutorSpecificStats get_specific() {
        return this.specific;
    }

    public void set_specific(ExecutorSpecificStats specific) {
        this.specific = specific;
    }

    public void unset_specific() {
        this.specific = null;
    }

    public boolean is_set_specific() {
        return this.specific != null;
    }

    public void set_specific_isSet(boolean value) {
        if (!value) {
            this.specific = null;
        }
    }

    public double get_rate() {
        return this.rate;
    }

    public void set_rate(double rate) {
        this.rate = rate;
        set_rate_isSet(true);
    }

    public void unset_rate() {
        __isset_bit_vector.clear(__RATE_ISSET_ID);
    }

    public boolean is_set_rate() {
        return __isset_bit_vector.get(__RATE_ISSET_ID);
    }

    public void set_rate_isSet(boolean value) {
        __isset_bit_vector.set(__RATE_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case EMITTED:
                if (value == null) {
                    unset_emitted();
                } else {
                    set_emitted((Map<String, Map<String, Long>>) value);
                }
                break;
            case TRANSFERRED:
                if (value == null) {
                    unset_transferred();
                } else {
                    set_transferred((Map<String, Map<String, Long>>) value);
                }
                break;
            case SPECIFIC:
                if (value == null) {
                    unset_specific();
                } else {
                    set_specific((ExecutorSpecificStats) value);
                }
                break;
            case RATE:
                if (value == null) {
                    unset_rate();
                } else {
                    set_rate((Double) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case EMITTED:
                return get_emitted();
            case TRANSFERRED:
                return get_transferred();
            case SPECIFIC:
                return get_specific();
            case RATE:
                return Double.valueOf(get_rate());
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case EMITTED:
                return is_set_emitted();
            case TRANSFERRED:
                return is_set_transferred();
            case SPECIFIC:
                return is_set_specific();
            case RATE:
                return is_set_rate();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof ExecutorStats)
            return this.equals((ExecutorStats) that);
        return false;
    }

    public boolean equals(ExecutorStats that) {
        if (that == null)
            return false;
        boolean this_present_emitted = true && this.is_set_emitted();
        boolean that_present_emitted = true && that.is_set_emitted();
        if (this_present_emitted || that_present_emitted) {
            if (!(this_present_emitted && that_present_emitted))
                return false;
            if (!this.emitted.equals(that.emitted))
                return false;
        }
        boolean this_present_transferred = true && this.is_set_transferred();
        boolean that_present_transferred = true && that.is_set_transferred();
        if (this_present_transferred || that_present_transferred) {
            if (!(this_present_transferred && that_present_transferred))
                return false;
            if (!this.transferred.equals(that.transferred))
                return false;
        }
        boolean this_present_specific = true && this.is_set_specific();
        boolean that_present_specific = true && that.is_set_specific();
        if (this_present_specific || that_present_specific) {
            if (!(this_present_specific && that_present_specific))
                return false;
            if (!this.specific.equals(that.specific))
                return false;
        }
        boolean this_present_rate = true;
        boolean that_present_rate = true;
        if (this_present_rate || that_present_rate) {
            if (!(this_present_rate && that_present_rate))
                return false;
            if (this.rate != that.rate)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        boolean present_emitted = true && (is_set_emitted());
        builder.append(present_emitted);
        if (present_emitted)
            builder.append(emitted);
        boolean present_transferred = true && (is_set_transferred());
        builder.append(present_transferred);
        if (present_transferred)
            builder.append(transferred);
        boolean present_specific = true && (is_set_specific());
        builder.append(present_specific);
        if (present_specific)
            builder.append(specific);
        boolean present_rate = true;
        builder.append(present_rate);
        if (present_rate)
            builder.append(rate);
        return builder.toHashCode();
    }

    public int compareTo(ExecutorStats other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        ExecutorStats typedOther = (ExecutorStats) other;
        lastComparison = Boolean.valueOf(is_set_emitted()).compareTo(typedOther.is_set_emitted());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (is_set_emitted()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.emitted, typedOther.emitted);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(is_set_transferred()).compareTo(typedOther.is_set_transferred());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (is_set_transferred()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.transferred, typedOther.transferred);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(is_set_specific()).compareTo(typedOther.is_set_specific());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (is_set_specific()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.specific, typedOther.specific);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(is_set_rate()).compareTo(typedOther.is_set_rate());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (is_set_rate()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.rate, typedOther.rate);
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
        org.apache.thrift.protocol.TField field;
        iprot.readStructBegin();
        while (true) {
            field = iprot.readFieldBegin();
            if (field.type == org.apache.thrift.protocol.TType.STOP) {
                break;
            }
            switch(field.id) {
                case 1:
                    if (field.type == org.apache.thrift.protocol.TType.MAP) {
                        {
                            org.apache.thrift.protocol.TMap _map125 = iprot.readMapBegin();
                            this.emitted = new HashMap<String, Map<String, Long>>(2 * _map125.size);
                            for (int _i126 = 0; _i126 < _map125.size; ++_i126) {
                                String _key127;
                                Map<String, Long> _val128;
                                _key127 = iprot.readString();
                                {
                                    org.apache.thrift.protocol.TMap _map129 = iprot.readMapBegin();
                                    _val128 = new HashMap<String, Long>(2 * _map129.size);
                                    for (int _i130 = 0; _i130 < _map129.size; ++_i130) {
                                        String _key131;
                                        long _val132;
                                        _key131 = iprot.readString();
                                        _val132 = iprot.readI64();
                                        _val128.put(_key131, _val132);
                                    }
                                    iprot.readMapEnd();
                                }
                                this.emitted.put(_key127, _val128);
                            }
                            iprot.readMapEnd();
                        }
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 2:
                    if (field.type == org.apache.thrift.protocol.TType.MAP) {
                        {
                            org.apache.thrift.protocol.TMap _map133 = iprot.readMapBegin();
                            this.transferred = new HashMap<String, Map<String, Long>>(2 * _map133.size);
                            for (int _i134 = 0; _i134 < _map133.size; ++_i134) {
                                String _key135;
                                Map<String, Long> _val136;
                                _key135 = iprot.readString();
                                {
                                    org.apache.thrift.protocol.TMap _map137 = iprot.readMapBegin();
                                    _val136 = new HashMap<String, Long>(2 * _map137.size);
                                    for (int _i138 = 0; _i138 < _map137.size; ++_i138) {
                                        String _key139;
                                        long _val140;
                                        _key139 = iprot.readString();
                                        _val140 = iprot.readI64();
                                        _val136.put(_key139, _val140);
                                    }
                                    iprot.readMapEnd();
                                }
                                this.transferred.put(_key135, _val136);
                            }
                            iprot.readMapEnd();
                        }
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 3:
                    if (field.type == org.apache.thrift.protocol.TType.STRUCT) {
                        this.specific = new ExecutorSpecificStats();
                        this.specific.read(iprot);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                case 4:
                    if (field.type == org.apache.thrift.protocol.TType.DOUBLE) {
                        this.rate = iprot.readDouble();
                        set_rate_isSet(true);
                    } else {
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
                    }
                    break;
                default:
                    org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
            }
            iprot.readFieldEnd();
        }
        iprot.readStructEnd();
        validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
        validate();
        oprot.writeStructBegin(STRUCT_DESC);
        if (this.emitted != null) {
            oprot.writeFieldBegin(EMITTED_FIELD_DESC);
            {
                oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.MAP, this.emitted.size()));
                for (Map.Entry<String, Map<String, Long>> _iter141 : this.emitted.entrySet()) {
                    oprot.writeString(_iter141.getKey());
                    {
                        oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.I64, _iter141.getValue().size()));
                        for (Map.Entry<String, Long> _iter142 : _iter141.getValue().entrySet()) {
                            oprot.writeString(_iter142.getKey());
                            oprot.writeI64(_iter142.getValue());
                        }
                        oprot.writeMapEnd();
                    }
                }
                oprot.writeMapEnd();
            }
            oprot.writeFieldEnd();
        }
        if (this.transferred != null) {
            oprot.writeFieldBegin(TRANSFERRED_FIELD_DESC);
            {
                oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.MAP, this.transferred.size()));
                for (Map.Entry<String, Map<String, Long>> _iter143 : this.transferred.entrySet()) {
                    oprot.writeString(_iter143.getKey());
                    {
                        oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.I64, _iter143.getValue().size()));
                        for (Map.Entry<String, Long> _iter144 : _iter143.getValue().entrySet()) {
                            oprot.writeString(_iter144.getKey());
                            oprot.writeI64(_iter144.getValue());
                        }
                        oprot.writeMapEnd();
                    }
                }
                oprot.writeMapEnd();
            }
            oprot.writeFieldEnd();
        }
        if (this.specific != null) {
            oprot.writeFieldBegin(SPECIFIC_FIELD_DESC);
            this.specific.write(oprot);
            oprot.writeFieldEnd();
        }
        oprot.writeFieldBegin(RATE_FIELD_DESC);
        oprot.writeDouble(this.rate);
        oprot.writeFieldEnd();
        oprot.writeFieldStop();
        oprot.writeStructEnd();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ExecutorStats(");
        boolean first = true;
        sb.append("emitted:");
        if (this.emitted == null) {
            sb.append("null");
        } else {
            sb.append(this.emitted);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("transferred:");
        if (this.transferred == null) {
            sb.append("null");
        } else {
            sb.append(this.transferred);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("specific:");
        if (this.specific == null) {
            sb.append("null");
        } else {
            sb.append(this.specific);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("rate:");
        sb.append(this.rate);
        first = false;
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
        if (!is_set_emitted()) {
            throw new org.apache.thrift.protocol.TProtocolException("Required field 'emitted' is unset! Struct:" + toString());
        }
        if (!is_set_transferred()) {
            throw new org.apache.thrift.protocol.TProtocolException("Required field 'transferred' is unset! Struct:" + toString());
        }
        if (!is_set_specific()) {
            throw new org.apache.thrift.protocol.TProtocolException("Required field 'specific' is unset! Struct:" + toString());
        }
        if (!is_set_rate()) {
            throw new org.apache.thrift.protocol.TProtocolException("Required field 'rate' is unset! Struct:" + toString());
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
            __isset_bit_vector = new BitSet(1);
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }
}
