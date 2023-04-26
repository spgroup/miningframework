package org.opentripplanner.api.thrift.definition;

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

public class FindNearestEdgesRequest implements org.apache.thrift.TBase<FindNearestEdgesRequest, FindNearestEdgesRequest._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("FindNearestEdgesRequest");

    private static final org.apache.thrift.protocol.TField LOCATION_FIELD_DESC = new org.apache.thrift.protocol.TField("location", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

    private static final org.apache.thrift.protocol.TField ALLOWED_MODES_FIELD_DESC = new org.apache.thrift.protocol.TField("allowed_modes", org.apache.thrift.protocol.TType.SET, (short) 2);

    private static final org.apache.thrift.protocol.TField HEADING_FIELD_DESC = new org.apache.thrift.protocol.TField("heading", org.apache.thrift.protocol.TType.DOUBLE, (short) 3);

    private static final org.apache.thrift.protocol.TField MAX_EDGES_FIELD_DESC = new org.apache.thrift.protocol.TField("max_edges", org.apache.thrift.protocol.TType.I32, (short) 10);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new FindNearestEdgesRequestStandardSchemeFactory());
        schemes.put(TupleScheme.class, new FindNearestEdgesRequestTupleSchemeFactory());
    }

    private org.opentripplanner.api.thrift.definition.Location location;

    private Set<org.opentripplanner.api.thrift.definition.TravelMode> allowed_modes;

    private double heading;

    private int max_edges;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        LOCATION((short) 1, "location"), ALLOWED_MODES((short) 2, "allowed_modes"), HEADING((short) 3, "heading"), MAX_EDGES((short) 10, "max_edges");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return LOCATION;
                case 2:
                    return ALLOWED_MODES;
                case 3:
                    return HEADING;
                case 10:
                    return MAX_EDGES;
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

    private static final int __HEADING_ISSET_ID = 0;

    private static final int __MAX_EDGES_ISSET_ID = 1;

    private BitSet __isset_bit_vector = new BitSet(2);

    private _Fields[] optionals = { _Fields.ALLOWED_MODES, _Fields.HEADING, _Fields.MAX_EDGES };

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.LOCATION, new org.apache.thrift.meta_data.FieldMetaData("location", org.apache.thrift.TFieldRequirementType.REQUIRED, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT, "Location")));
        tmpMap.put(_Fields.ALLOWED_MODES, new org.apache.thrift.meta_data.FieldMetaData("allowed_modes", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.SetMetaData(org.apache.thrift.protocol.TType.SET, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.ENUM, "TravelMode"))));
        tmpMap.put(_Fields.HEADING, new org.apache.thrift.meta_data.FieldMetaData("heading", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.MAX_EDGES, new org.apache.thrift.meta_data.FieldMetaData("max_edges", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(FindNearestEdgesRequest.class, metaDataMap);
    }

    public FindNearestEdgesRequest() {
        this.max_edges = 10;
    }

    public FindNearestEdgesRequest(org.opentripplanner.api.thrift.definition.Location location) {
        this();
        this.location = location;
    }

    public FindNearestEdgesRequest(FindNearestEdgesRequest other) {
        __isset_bit_vector.clear();
        __isset_bit_vector.or(other.__isset_bit_vector);
        if (other.isSetLocation()) {
            this.location = other.location;
        }
        if (other.isSetAllowed_modes()) {
            Set<org.opentripplanner.api.thrift.definition.TravelMode> __this__allowed_modes = new HashSet<org.opentripplanner.api.thrift.definition.TravelMode>();
            for (org.opentripplanner.api.thrift.definition.TravelMode other_element : other.allowed_modes) {
                __this__allowed_modes.add(other_element);
            }
            this.allowed_modes = __this__allowed_modes;
        }
        this.heading = other.heading;
        this.max_edges = other.max_edges;
    }

    public FindNearestEdgesRequest deepCopy() {
        return new FindNearestEdgesRequest(this);
    }

    @Override
    public void clear() {
        this.location = null;
        this.allowed_modes = null;
        setHeadingIsSet(false);
        this.heading = 0.0;
        this.max_edges = 10;
    }

    public org.opentripplanner.api.thrift.definition.Location getLocation() {
        return this.location;
    }

    public void setLocation(org.opentripplanner.api.thrift.definition.Location location) {
        this.location = location;
    }

    public void unsetLocation() {
        this.location = null;
    }

    public boolean isSetLocation() {
        return this.location != null;
    }

    public void setLocationIsSet(boolean value) {
        if (!value) {
            this.location = null;
        }
    }

    public int getAllowed_modesSize() {
        return (this.allowed_modes == null) ? 0 : this.allowed_modes.size();
    }

    public java.util.Iterator<org.opentripplanner.api.thrift.definition.TravelMode> getAllowed_modesIterator() {
        return (this.allowed_modes == null) ? null : this.allowed_modes.iterator();
    }

    public void addToAllowed_modes(org.opentripplanner.api.thrift.definition.TravelMode elem) {
        if (this.allowed_modes == null) {
            this.allowed_modes = new HashSet<org.opentripplanner.api.thrift.definition.TravelMode>();
        }
        this.allowed_modes.add(elem);
    }

    public Set<org.opentripplanner.api.thrift.definition.TravelMode> getAllowed_modes() {
        return this.allowed_modes;
    }

    public void setAllowed_modes(Set<org.opentripplanner.api.thrift.definition.TravelMode> allowed_modes) {
        this.allowed_modes = allowed_modes;
    }

    public void unsetAllowed_modes() {
        this.allowed_modes = null;
    }

    public boolean isSetAllowed_modes() {
        return this.allowed_modes != null;
    }

    public void setAllowed_modesIsSet(boolean value) {
        if (!value) {
            this.allowed_modes = null;
        }
    }

    public double getHeading() {
        return this.heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
        setHeadingIsSet(true);
    }

    public void unsetHeading() {
        __isset_bit_vector.clear(__HEADING_ISSET_ID);
    }

    public boolean isSetHeading() {
        return __isset_bit_vector.get(__HEADING_ISSET_ID);
    }

    public void setHeadingIsSet(boolean value) {
        __isset_bit_vector.set(__HEADING_ISSET_ID, value);
    }

    public int getMax_edges() {
        return this.max_edges;
    }

    public void setMax_edges(int max_edges) {
        this.max_edges = max_edges;
        setMax_edgesIsSet(true);
    }

    public void unsetMax_edges() {
        __isset_bit_vector.clear(__MAX_EDGES_ISSET_ID);
    }

    public boolean isSetMax_edges() {
        return __isset_bit_vector.get(__MAX_EDGES_ISSET_ID);
    }

    public void setMax_edgesIsSet(boolean value) {
        __isset_bit_vector.set(__MAX_EDGES_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case LOCATION:
                if (value == null) {
                    unsetLocation();
                } else {
                    setLocation((org.opentripplanner.api.thrift.definition.Location) value);
                }
                break;
            case ALLOWED_MODES:
                if (value == null) {
                    unsetAllowed_modes();
                } else {
                    setAllowed_modes((Set<org.opentripplanner.api.thrift.definition.TravelMode>) value);
                }
                break;
            case HEADING:
                if (value == null) {
                    unsetHeading();
                } else {
                    setHeading((Double) value);
                }
                break;
            case MAX_EDGES:
                if (value == null) {
                    unsetMax_edges();
                } else {
                    setMax_edges((Integer) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case LOCATION:
                return getLocation();
            case ALLOWED_MODES:
                return getAllowed_modes();
            case HEADING:
                return Double.valueOf(getHeading());
            case MAX_EDGES:
                return Integer.valueOf(getMax_edges());
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case LOCATION:
                return isSetLocation();
            case ALLOWED_MODES:
                return isSetAllowed_modes();
            case HEADING:
                return isSetHeading();
            case MAX_EDGES:
                return isSetMax_edges();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof FindNearestEdgesRequest)
            return this.equals((FindNearestEdgesRequest) that);
        return false;
    }

    public boolean equals(FindNearestEdgesRequest that) {
        if (that == null)
            return false;
        boolean this_present_location = true && this.isSetLocation();
        boolean that_present_location = true && that.isSetLocation();
        if (this_present_location || that_present_location) {
            if (!(this_present_location && that_present_location))
                return false;
            if (!this.location.equals(that.location))
                return false;
        }
        boolean this_present_allowed_modes = true && this.isSetAllowed_modes();
        boolean that_present_allowed_modes = true && that.isSetAllowed_modes();
        if (this_present_allowed_modes || that_present_allowed_modes) {
            if (!(this_present_allowed_modes && that_present_allowed_modes))
                return false;
            if (!this.allowed_modes.equals(that.allowed_modes))
                return false;
        }
        boolean this_present_heading = true && this.isSetHeading();
        boolean that_present_heading = true && that.isSetHeading();
        if (this_present_heading || that_present_heading) {
            if (!(this_present_heading && that_present_heading))
                return false;
            if (this.heading != that.heading)
                return false;
        }
        boolean this_present_max_edges = true && this.isSetMax_edges();
        boolean that_present_max_edges = true && that.isSetMax_edges();
        if (this_present_max_edges || that_present_max_edges) {
            if (!(this_present_max_edges && that_present_max_edges))
                return false;
            if (this.max_edges != that.max_edges)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public int compareTo(FindNearestEdgesRequest other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        FindNearestEdgesRequest typedOther = (FindNearestEdgesRequest) other;
        lastComparison = Boolean.valueOf(isSetLocation()).compareTo(typedOther.isSetLocation());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetLocation()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.location, typedOther.location);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetAllowed_modes()).compareTo(typedOther.isSetAllowed_modes());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetAllowed_modes()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.allowed_modes, typedOther.allowed_modes);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetHeading()).compareTo(typedOther.isSetHeading());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetHeading()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.heading, typedOther.heading);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetMax_edges()).compareTo(typedOther.isSetMax_edges());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetMax_edges()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.max_edges, typedOther.max_edges);
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
        StringBuilder sb = new StringBuilder("FindNearestEdgesRequest(");
        boolean first = true;
        sb.append("location:");
        if (this.location == null) {
            sb.append("null");
        } else {
            sb.append(this.location);
        }
        first = false;
        if (isSetAllowed_modes()) {
            if (!first)
                sb.append(", ");
            sb.append("allowed_modes:");
            if (this.allowed_modes == null) {
                sb.append("null");
            } else {
                sb.append(this.allowed_modes);
            }
            first = false;
        }
        if (isSetHeading()) {
            if (!first)
                sb.append(", ");
            sb.append("heading:");
            sb.append(this.heading);
            first = false;
        }
        if (isSetMax_edges()) {
            if (!first)
                sb.append(", ");
            sb.append("max_edges:");
            sb.append(this.max_edges);
            first = false;
        }
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
        if (!isSetLocation()) {
            throw new org.apache.thrift.protocol.TProtocolException("Required field 'location' is unset! Struct:" + toString());
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

    private static class FindNearestEdgesRequestStandardSchemeFactory implements SchemeFactory {

        public FindNearestEdgesRequestStandardScheme getScheme() {
            return new FindNearestEdgesRequestStandardScheme();
        }
    }

    private static class FindNearestEdgesRequestStandardScheme extends StandardScheme<FindNearestEdgesRequest> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, FindNearestEdgesRequest struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch(schemeField.id) {
                    case 1:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.location = new org.opentripplanner.api.thrift.definition.Location();
                            struct.location.read(iprot);
                            struct.setLocationIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.SET) {
                            {
                                org.apache.thrift.protocol.TSet _set24 = iprot.readSetBegin();
                                struct.allowed_modes = new HashSet<org.opentripplanner.api.thrift.definition.TravelMode>(2 * _set24.size);
                                for (int _i25 = 0; _i25 < _set24.size; ++_i25) {
                                    org.opentripplanner.api.thrift.definition.TravelMode _elem26;
                                    _elem26 = org.opentripplanner.api.thrift.definition.TravelMode.findByValue(iprot.readI32());
                                    struct.allowed_modes.add(_elem26);
                                }
                                iprot.readSetEnd();
                            }
                            struct.setAllowed_modesIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
                            struct.heading = iprot.readDouble();
                            struct.setHeadingIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 10:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.max_edges = iprot.readI32();
                            struct.setMax_edgesIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, FindNearestEdgesRequest struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.location != null) {
                oprot.writeFieldBegin(LOCATION_FIELD_DESC);
                struct.location.write(oprot);
                oprot.writeFieldEnd();
            }
            if (struct.allowed_modes != null) {
                if (struct.isSetAllowed_modes()) {
                    oprot.writeFieldBegin(ALLOWED_MODES_FIELD_DESC);
                    {
                        oprot.writeSetBegin(new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.I32, struct.allowed_modes.size()));
                        for (org.opentripplanner.api.thrift.definition.TravelMode _iter27 : struct.allowed_modes) {
                            oprot.writeI32(_iter27.getValue());
                        }
                        oprot.writeSetEnd();
                    }
                    oprot.writeFieldEnd();
                }
            }
            if (struct.isSetHeading()) {
                oprot.writeFieldBegin(HEADING_FIELD_DESC);
                oprot.writeDouble(struct.heading);
                oprot.writeFieldEnd();
            }
            if (struct.isSetMax_edges()) {
                oprot.writeFieldBegin(MAX_EDGES_FIELD_DESC);
                oprot.writeI32(struct.max_edges);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class FindNearestEdgesRequestTupleSchemeFactory implements SchemeFactory {

        public FindNearestEdgesRequestTupleScheme getScheme() {
            return new FindNearestEdgesRequestTupleScheme();
        }
    }

    private static class FindNearestEdgesRequestTupleScheme extends TupleScheme<FindNearestEdgesRequest> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, FindNearestEdgesRequest struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            struct.location.write(oprot);
            BitSet optionals = new BitSet();
            if (struct.isSetAllowed_modes()) {
                optionals.set(0);
            }
            if (struct.isSetHeading()) {
                optionals.set(1);
            }
            if (struct.isSetMax_edges()) {
                optionals.set(2);
            }
            oprot.writeBitSet(optionals, 3);
            if (struct.isSetAllowed_modes()) {
                {
                    oprot.writeI32(struct.allowed_modes.size());
                    for (org.opentripplanner.api.thrift.definition.TravelMode _iter28 : struct.allowed_modes) {
                        oprot.writeI32(_iter28.getValue());
                    }
                }
            }
            if (struct.isSetHeading()) {
                oprot.writeDouble(struct.heading);
            }
            if (struct.isSetMax_edges()) {
                oprot.writeI32(struct.max_edges);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, FindNearestEdgesRequest struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            struct.location = new org.opentripplanner.api.thrift.definition.Location();
            struct.location.read(iprot);
            struct.setLocationIsSet(true);
            BitSet incoming = iprot.readBitSet(3);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TSet _set29 = new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.I32, iprot.readI32());
                    struct.allowed_modes = new HashSet<org.opentripplanner.api.thrift.definition.TravelMode>(2 * _set29.size);
                    for (int _i30 = 0; _i30 < _set29.size; ++_i30) {
                        org.opentripplanner.api.thrift.definition.TravelMode _elem31;
                        _elem31 = org.opentripplanner.api.thrift.definition.TravelMode.findByValue(iprot.readI32());
                        struct.allowed_modes.add(_elem31);
                    }
                }
                struct.setAllowed_modesIsSet(true);
            }
            if (incoming.get(1)) {
                struct.heading = iprot.readDouble();
                struct.setHeadingIsSet(true);
            }
            if (incoming.get(2)) {
                struct.max_edges = iprot.readI32();
                struct.setMax_edgesIsSet(true);
            }
        }
    }
}
