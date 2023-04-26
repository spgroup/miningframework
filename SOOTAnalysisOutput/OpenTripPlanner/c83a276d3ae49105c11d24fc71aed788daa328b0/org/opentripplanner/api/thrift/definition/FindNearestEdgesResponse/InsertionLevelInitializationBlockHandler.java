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

public class FindNearestEdgesResponse implements org.apache.thrift.TBase<FindNearestEdgesResponse, FindNearestEdgesResponse._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("FindNearestEdgesResponse");

    private static final org.apache.thrift.protocol.TField NEAREST_EDGES_FIELD_DESC = new org.apache.thrift.protocol.TField("nearest_edges", org.apache.thrift.protocol.TType.LIST, (short) 1);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new FindNearestEdgesResponseStandardSchemeFactory());
        schemes.put(TupleScheme.class, new FindNearestEdgesResponseTupleSchemeFactory());
    }

    private List<org.opentripplanner.api.thrift.definition.EdgeMatch> nearest_edges;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        NEAREST_EDGES((short) 1, "nearest_edges");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return NEAREST_EDGES;
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

    private _Fields[] optionals = { _Fields.NEAREST_EDGES };

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.NEAREST_EDGES, new org.apache.thrift.meta_data.FieldMetaData("nearest_edges", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT, "EdgeMatch"))));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(FindNearestEdgesResponse.class, metaDataMap);
    }

    public FindNearestEdgesResponse() {
    }

    public FindNearestEdgesResponse(FindNearestEdgesResponse other) {
        if (other.isSetNearest_edges()) {
            List<org.opentripplanner.api.thrift.definition.EdgeMatch> __this__nearest_edges = new ArrayList<org.opentripplanner.api.thrift.definition.EdgeMatch>();
            for (org.opentripplanner.api.thrift.definition.EdgeMatch other_element : other.nearest_edges) {
                __this__nearest_edges.add(other_element);
            }
            this.nearest_edges = __this__nearest_edges;
        }
    }

    public FindNearestEdgesResponse deepCopy() {
        return new FindNearestEdgesResponse(this);
    }

    @Override
    public void clear() {
        this.nearest_edges = null;
    }

    public int getNearest_edgesSize() {
        return (this.nearest_edges == null) ? 0 : this.nearest_edges.size();
    }

    public java.util.Iterator<org.opentripplanner.api.thrift.definition.EdgeMatch> getNearest_edgesIterator() {
        return (this.nearest_edges == null) ? null : this.nearest_edges.iterator();
    }

    public void addToNearest_edges(org.opentripplanner.api.thrift.definition.EdgeMatch elem) {
        if (this.nearest_edges == null) {
            this.nearest_edges = new ArrayList<org.opentripplanner.api.thrift.definition.EdgeMatch>();
        }
        this.nearest_edges.add(elem);
    }

    public List<org.opentripplanner.api.thrift.definition.EdgeMatch> getNearest_edges() {
        return this.nearest_edges;
    }

    public void setNearest_edges(List<org.opentripplanner.api.thrift.definition.EdgeMatch> nearest_edges) {
        this.nearest_edges = nearest_edges;
    }

    public void unsetNearest_edges() {
        this.nearest_edges = null;
    }

    public boolean isSetNearest_edges() {
        return this.nearest_edges != null;
    }

    public void setNearest_edgesIsSet(boolean value) {
        if (!value) {
            this.nearest_edges = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case NEAREST_EDGES:
                if (value == null) {
                    unsetNearest_edges();
                } else {
                    setNearest_edges((List<org.opentripplanner.api.thrift.definition.EdgeMatch>) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case NEAREST_EDGES:
                return getNearest_edges();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case NEAREST_EDGES:
                return isSetNearest_edges();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof FindNearestEdgesResponse)
            return this.equals((FindNearestEdgesResponse) that);
        return false;
    }

    public boolean equals(FindNearestEdgesResponse that) {
        if (that == null)
            return false;
        boolean this_present_nearest_edges = true && this.isSetNearest_edges();
        boolean that_present_nearest_edges = true && that.isSetNearest_edges();
        if (this_present_nearest_edges || that_present_nearest_edges) {
            if (!(this_present_nearest_edges && that_present_nearest_edges))
                return false;
            if (!this.nearest_edges.equals(that.nearest_edges))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public int compareTo(FindNearestEdgesResponse other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        FindNearestEdgesResponse typedOther = (FindNearestEdgesResponse) other;
        lastComparison = Boolean.valueOf(isSetNearest_edges()).compareTo(typedOther.isSetNearest_edges());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetNearest_edges()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.nearest_edges, typedOther.nearest_edges);
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
        StringBuilder sb = new StringBuilder("FindNearestEdgesResponse(");
        boolean first = true;
        if (isSetNearest_edges()) {
            sb.append("nearest_edges:");
            if (this.nearest_edges == null) {
                sb.append("null");
            } else {
                sb.append(this.nearest_edges);
            }
            first = false;
        }
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

    private static class FindNearestEdgesResponseStandardSchemeFactory implements SchemeFactory {

        public FindNearestEdgesResponseStandardScheme getScheme() {
            return new FindNearestEdgesResponseStandardScheme();
        }
    }

    private static class FindNearestEdgesResponseStandardScheme extends StandardScheme<FindNearestEdgesResponse> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, FindNearestEdgesResponse struct) throws org.apache.thrift.TException {
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
                                org.apache.thrift.protocol.TList _list32 = iprot.readListBegin();
                                struct.nearest_edges = new ArrayList<org.opentripplanner.api.thrift.definition.EdgeMatch>(_list32.size);
                                for (int _i33 = 0; _i33 < _list32.size; ++_i33) {
                                    org.opentripplanner.api.thrift.definition.EdgeMatch _elem34;
                                    _elem34 = new org.opentripplanner.api.thrift.definition.EdgeMatch();
                                    _elem34.read(iprot);
                                    struct.nearest_edges.add(_elem34);
                                }
                                iprot.readListEnd();
                            }
                            struct.setNearest_edgesIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, FindNearestEdgesResponse struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.nearest_edges != null) {
                if (struct.isSetNearest_edges()) {
                    oprot.writeFieldBegin(NEAREST_EDGES_FIELD_DESC);
                    {
                        oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.nearest_edges.size()));
                        for (org.opentripplanner.api.thrift.definition.EdgeMatch _iter35 : struct.nearest_edges) {
                            _iter35.write(oprot);
                        }
                        oprot.writeListEnd();
                    }
                    oprot.writeFieldEnd();
                }
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class FindNearestEdgesResponseTupleSchemeFactory implements SchemeFactory {

        public FindNearestEdgesResponseTupleScheme getScheme() {
            return new FindNearestEdgesResponseTupleScheme();
        }
    }

    private static class FindNearestEdgesResponseTupleScheme extends TupleScheme<FindNearestEdgesResponse> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, FindNearestEdgesResponse struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetNearest_edges()) {
                optionals.set(0);
            }
            oprot.writeBitSet(optionals, 1);
            if (struct.isSetNearest_edges()) {
                {
                    oprot.writeI32(struct.nearest_edges.size());
                    for (org.opentripplanner.api.thrift.definition.EdgeMatch _iter36 : struct.nearest_edges) {
                        _iter36.write(oprot);
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, FindNearestEdgesResponse struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(1);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TList _list37 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.nearest_edges = new ArrayList<org.opentripplanner.api.thrift.definition.EdgeMatch>(_list37.size);
                    for (int _i38 = 0; _i38 < _list37.size; ++_i38) {
                        org.opentripplanner.api.thrift.definition.EdgeMatch _elem39;
                        _elem39 = new org.opentripplanner.api.thrift.definition.EdgeMatch();
                        _elem39.read(iprot);
                        struct.nearest_edges.add(_elem39);
                    }
                }
                struct.setNearest_edgesIsSet(true);
            }
        }
    }
}