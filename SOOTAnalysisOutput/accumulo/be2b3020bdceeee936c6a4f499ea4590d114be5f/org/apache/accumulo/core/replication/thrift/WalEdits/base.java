package org.apache.accumulo.core.replication.thrift;

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
public class WalEdits implements org.apache.thrift.TBase<WalEdits, WalEdits._Fields>, java.io.Serializable, Cloneable, Comparable<WalEdits> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("WalEdits");

    private static final org.apache.thrift.protocol.TField EDITS_FIELD_DESC = new org.apache.thrift.protocol.TField("edits", org.apache.thrift.protocol.TType.LIST, (short) 1);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new WalEditsStandardSchemeFactory());
        schemes.put(TupleScheme.class, new WalEditsTupleSchemeFactory());
    }

    public List<ByteBuffer> edits;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        EDITS((short) 1, "edits");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return EDITS;
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
        tmpMap.put(_Fields.EDITS, new org.apache.thrift.meta_data.FieldMetaData("edits", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true))));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(WalEdits.class, metaDataMap);
    }

    public WalEdits() {
    }

    public WalEdits(List<ByteBuffer> edits) {
        this();
        this.edits = edits;
    }

    public WalEdits(WalEdits other) {
        if (other.isSetEdits()) {
            List<ByteBuffer> __this__edits = new ArrayList<ByteBuffer>(other.edits);
            this.edits = __this__edits;
        }
    }

    public WalEdits deepCopy() {
        return new WalEdits(this);
    }

    @Override
    public void clear() {
        this.edits = null;
    }

    public int getEditsSize() {
        return (this.edits == null) ? 0 : this.edits.size();
    }

    public java.util.Iterator<ByteBuffer> getEditsIterator() {
        return (this.edits == null) ? null : this.edits.iterator();
    }

    public void addToEdits(ByteBuffer elem) {
        if (this.edits == null) {
            this.edits = new ArrayList<ByteBuffer>();
        }
        this.edits.add(elem);
    }

    public List<ByteBuffer> getEdits() {
        return this.edits;
    }

    public WalEdits setEdits(List<ByteBuffer> edits) {
        this.edits = edits;
        return this;
    }

    public void unsetEdits() {
        this.edits = null;
    }

    public boolean isSetEdits() {
        return this.edits != null;
    }

    public void setEditsIsSet(boolean value) {
        if (!value) {
            this.edits = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case EDITS:
                if (value == null) {
                    unsetEdits();
                } else {
                    setEdits((List<ByteBuffer>) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case EDITS:
                return getEdits();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case EDITS:
                return isSetEdits();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof WalEdits)
            return this.equals((WalEdits) that);
        return false;
    }

    public boolean equals(WalEdits that) {
        if (that == null)
            return false;
        boolean this_present_edits = true && this.isSetEdits();
        boolean that_present_edits = true && that.isSetEdits();
        if (this_present_edits || that_present_edits) {
            if (!(this_present_edits && that_present_edits))
                return false;
            if (!this.edits.equals(that.edits))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        List<Object> list = new ArrayList<Object>();
        boolean present_edits = true && (isSetEdits());
        list.add(present_edits);
        if (present_edits)
            list.add(edits);
        return list.hashCode();
    }

    @Override
    public int compareTo(WalEdits other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetEdits()).compareTo(other.isSetEdits());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetEdits()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.edits, other.edits);
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
        StringBuilder sb = new StringBuilder("WalEdits(");
        boolean first = true;
        sb.append("edits:");
        if (this.edits == null) {
            sb.append("null");
        } else {
            org.apache.thrift.TBaseHelper.toString(this.edits, sb);
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

    private static class WalEditsStandardSchemeFactory implements SchemeFactory {

        public WalEditsStandardScheme getScheme() {
            return new WalEditsStandardScheme();
        }
    }

    private static class WalEditsStandardScheme extends StandardScheme<WalEdits> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, WalEdits struct) throws org.apache.thrift.TException {
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
                                struct.edits = new ArrayList<ByteBuffer>(_list0.size);
                                ByteBuffer _elem1;
                                for (int _i2 = 0; _i2 < _list0.size; ++_i2) {
                                    _elem1 = iprot.readBinary();
                                    struct.edits.add(_elem1);
                                }
                                iprot.readListEnd();
                            }
                            struct.setEditsIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, WalEdits struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.edits != null) {
                oprot.writeFieldBegin(EDITS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.edits.size()));
                    for (ByteBuffer _iter3 : struct.edits) {
                        oprot.writeBinary(_iter3);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class WalEditsTupleSchemeFactory implements SchemeFactory {

        public WalEditsTupleScheme getScheme() {
            return new WalEditsTupleScheme();
        }
    }

    private static class WalEditsTupleScheme extends TupleScheme<WalEdits> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, WalEdits struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetEdits()) {
                optionals.set(0);
            }
            oprot.writeBitSet(optionals, 1);
            if (struct.isSetEdits()) {
                {
                    oprot.writeI32(struct.edits.size());
                    for (ByteBuffer _iter4 : struct.edits) {
                        oprot.writeBinary(_iter4);
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, WalEdits struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(1);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TList _list5 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.edits = new ArrayList<ByteBuffer>(_list5.size);
                    ByteBuffer _elem6;
                    for (int _i7 = 0; _i7 < _list5.size; ++_i7) {
                        _elem6 = iprot.readBinary();
                        struct.edits.add(_elem6);
                    }
                }
                struct.setEditsIsSet(true);
            }
        }
    }
}
