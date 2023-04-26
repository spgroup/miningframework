package org.apache.accumulo.core.master.thrift;

<<<<<<< MINE
=======
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

>>>>>>> YOURS
@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class TabletSplit implements org.apache.thrift.TBase<TabletSplit, TabletSplit._Fields>, java.io.Serializable, Cloneable, Comparable<TabletSplit> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TabletSplit");

    private static final org.apache.thrift.protocol.TField OLD_TABLET_FIELD_DESC = new org.apache.thrift.protocol.TField("oldTablet", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

    private static final org.apache.thrift.protocol.TField NEW_TABLETS_FIELD_DESC = new org.apache.thrift.protocol.TField("newTablets", org.apache.thrift.protocol.TType.LIST, (short) 2);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new TabletSplitStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new TabletSplitTupleSchemeFactory();

    @org.apache.thrift.annotation.Nullable
    public org.apache.accumulo.core.dataImpl.thrift.TKeyExtent oldTablet;

    @org.apache.thrift.annotation.Nullable
    public java.util.List<org.apache.accumulo.core.dataImpl.thrift.TKeyExtent> newTablets;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        OLD_TABLET((short) 1, "oldTablet"), NEW_TABLETS((short) 2, "newTablets");

        private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

        static {
            for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        @org.apache.thrift.annotation.Nullable
        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return OLD_TABLET;
                case 2:
                    return NEW_TABLETS;
                default:
                    return null;
            }
        }

        public static _Fields findByThriftIdOrThrow(int fieldId) {
            _Fields fields = findByThriftId(fieldId);
            if (fields == null)
                throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
            return fields;
        }

        @org.apache.thrift.annotation.Nullable
        public static _Fields findByName(java.lang.String name) {
            return byName.get(name);
        }

        private final short _thriftId;

        private final java.lang.String _fieldName;

        _Fields(short thriftId, java.lang.String fieldName) {
            _thriftId = thriftId;
            _fieldName = fieldName;
        }

        public short getThriftFieldId() {
            return _thriftId;
        }

        public java.lang.String getFieldName() {
            return _fieldName;
        }
    }

    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.OLD_TABLET, new org.apache.thrift.meta_data.FieldMetaData("oldTablet", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.dataImpl.thrift.TKeyExtent.class)));
        tmpMap.put(_Fields.NEW_TABLETS, new org.apache.thrift.meta_data.FieldMetaData("newTablets", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.dataImpl.thrift.TKeyExtent.class))));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TabletSplit.class, metaDataMap);
    }

    public TabletSplit() {
    }

    public TabletSplit(org.apache.accumulo.core.dataImpl.thrift.TKeyExtent oldTablet, java.util.List<org.apache.accumulo.core.dataImpl.thrift.TKeyExtent> newTablets) {
        this();
        this.oldTablet = oldTablet;
        this.newTablets = newTablets;
    }

    public TabletSplit(TabletSplit other) {
        if (other.isSetOldTablet()) {
            this.oldTablet = new org.apache.accumulo.core.dataImpl.thrift.TKeyExtent(other.oldTablet);
        }
        if (other.isSetNewTablets()) {
            java.util.List<org.apache.accumulo.core.dataImpl.thrift.TKeyExtent> __this__newTablets = new java.util.ArrayList<org.apache.accumulo.core.dataImpl.thrift.TKeyExtent>(other.newTablets.size());
            for (org.apache.accumulo.core.dataImpl.thrift.TKeyExtent other_element : other.newTablets) {
                __this__newTablets.add(new org.apache.accumulo.core.dataImpl.thrift.TKeyExtent(other_element));
            }
            this.newTablets = __this__newTablets;
        }
    }

    public TabletSplit deepCopy() {
        return new TabletSplit(this);
    }

    @Override
    public void clear() {
        this.oldTablet = null;
        this.newTablets = null;
    }

    @org.apache.thrift.annotation.Nullable
    public org.apache.accumulo.core.dataImpl.thrift.TKeyExtent getOldTablet() {
        return this.oldTablet;
    }

    public TabletSplit setOldTablet(@org.apache.thrift.annotation.Nullable org.apache.accumulo.core.dataImpl.thrift.TKeyExtent oldTablet) {
        this.oldTablet = oldTablet;
        return this;
    }

    public void unsetOldTablet() {
        this.oldTablet = null;
    }

    public boolean isSetOldTablet() {
        return this.oldTablet != null;
    }

    public void setOldTabletIsSet(boolean value) {
        if (!value) {
            this.oldTablet = null;
        }
    }

    public int getNewTabletsSize() {
        return (this.newTablets == null) ? 0 : this.newTablets.size();
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.Iterator<org.apache.accumulo.core.dataImpl.thrift.TKeyExtent> getNewTabletsIterator() {
        return (this.newTablets == null) ? null : this.newTablets.iterator();
    }

    public void addToNewTablets(org.apache.accumulo.core.dataImpl.thrift.TKeyExtent elem) {
        if (this.newTablets == null) {
            this.newTablets = new java.util.ArrayList<org.apache.accumulo.core.dataImpl.thrift.TKeyExtent>();
        }
        this.newTablets.add(elem);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.List<org.apache.accumulo.core.dataImpl.thrift.TKeyExtent> getNewTablets() {
        return this.newTablets;
    }

    public TabletSplit setNewTablets(@org.apache.thrift.annotation.Nullable java.util.List<org.apache.accumulo.core.dataImpl.thrift.TKeyExtent> newTablets) {
        this.newTablets = newTablets;
        return this;
    }

    public void unsetNewTablets() {
        this.newTablets = null;
    }

    public boolean isSetNewTablets() {
        return this.newTablets != null;
    }

    public void setNewTabletsIsSet(boolean value) {
        if (!value) {
            this.newTablets = null;
        }
    }

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case OLD_TABLET:
                if (value == null) {
                    unsetOldTablet();
                } else {
                    setOldTablet((org.apache.accumulo.core.dataImpl.thrift.TKeyExtent) value);
                }
                break;
            case NEW_TABLETS:
                if (value == null) {
                    unsetNewTablets();
                } else {
                    setNewTablets((java.util.List<org.apache.accumulo.core.dataImpl.thrift.TKeyExtent>) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case OLD_TABLET:
                return getOldTablet();
            case NEW_TABLETS:
                return getNewTablets();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }
        switch(field) {
            case OLD_TABLET:
                return isSetOldTablet();
            case NEW_TABLETS:
                return isSetNewTablets();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof TabletSplit)
            return this.equals((TabletSplit) that);
        return false;
    }

    public boolean equals(TabletSplit that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
        boolean this_present_oldTablet = true && this.isSetOldTablet();
        boolean that_present_oldTablet = true && that.isSetOldTablet();
        if (this_present_oldTablet || that_present_oldTablet) {
            if (!(this_present_oldTablet && that_present_oldTablet))
                return false;
            if (!this.oldTablet.equals(that.oldTablet))
                return false;
        }
        boolean this_present_newTablets = true && this.isSetNewTablets();
        boolean that_present_newTablets = true && that.isSetNewTablets();
        if (this_present_newTablets || that_present_newTablets) {
            if (!(this_present_newTablets && that_present_newTablets))
                return false;
            if (!this.newTablets.equals(that.newTablets))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 8191 + ((isSetOldTablet()) ? 131071 : 524287);
        if (isSetOldTablet())
            hashCode = hashCode * 8191 + oldTablet.hashCode();
        hashCode = hashCode * 8191 + ((isSetNewTablets()) ? 131071 : 524287);
        if (isSetNewTablets())
            hashCode = hashCode * 8191 + newTablets.hashCode();
        return hashCode;
    }

    @Override
    public int compareTo(TabletSplit other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetOldTablet()).compareTo(other.isSetOldTablet());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetOldTablet()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.oldTablet, other.oldTablet);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetNewTablets()).compareTo(other.isSetNewTablets());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetNewTablets()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.newTablets, other.newTablets);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        return 0;
    }

    @org.apache.thrift.annotation.Nullable
    public _Fields fieldForId(int fieldId) {
        return _Fields.findByThriftId(fieldId);
    }

    public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
        scheme(iprot).read(iprot, this);
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
        scheme(oprot).write(oprot, this);
    }

    @Override
    public java.lang.String toString() {
        java.lang.StringBuilder sb = new java.lang.StringBuilder("TabletSplit(");
        boolean first = true;
        sb.append("oldTablet:");
        if (this.oldTablet == null) {
            sb.append("null");
        } else {
            sb.append(this.oldTablet);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("newTablets:");
        if (this.newTablets == null) {
            sb.append("null");
        } else {
            sb.append(this.newTablets);
        }
        first = false;
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
        if (oldTablet != null) {
            oldTablet.validate();
        }
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        try {
            write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
        try {
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class TabletSplitStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public TabletSplitStandardScheme getScheme() {
            return new TabletSplitStandardScheme();
        }
    }

    private static class TabletSplitStandardScheme extends org.apache.thrift.scheme.StandardScheme<TabletSplit> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, TabletSplit struct) throws org.apache.thrift.TException {
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
                            struct.oldTablet = new org.apache.accumulo.core.dataImpl.thrift.TKeyExtent();
                            struct.oldTablet.read(iprot);
                            struct.setOldTabletIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list78 = iprot.readListBegin();
                                struct.newTablets = new java.util.ArrayList<org.apache.accumulo.core.dataImpl.thrift.TKeyExtent>(_list78.size);
                                @org.apache.thrift.annotation.Nullable
                                org.apache.accumulo.core.dataImpl.thrift.TKeyExtent _elem79;
                                for (int _i80 = 0; _i80 < _list78.size; ++_i80) {
                                    _elem79 = new org.apache.accumulo.core.dataImpl.thrift.TKeyExtent();
                                    _elem79.read(iprot);
                                    struct.newTablets.add(_elem79);
                                }
                                iprot.readListEnd();
                            }
                            struct.setNewTabletsIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, TabletSplit struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.oldTablet != null) {
                oprot.writeFieldBegin(OLD_TABLET_FIELD_DESC);
                struct.oldTablet.write(oprot);
                oprot.writeFieldEnd();
            }
            if (struct.newTablets != null) {
                oprot.writeFieldBegin(NEW_TABLETS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.newTablets.size()));
                    for (org.apache.accumulo.core.dataImpl.thrift.TKeyExtent _iter81 : struct.newTablets) {
                        _iter81.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class TabletSplitTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public TabletSplitTupleScheme getScheme() {
            return new TabletSplitTupleScheme();
        }
    }

    private static class TabletSplitTupleScheme extends org.apache.thrift.scheme.TupleScheme<TabletSplit> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, TabletSplit struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
            if (struct.isSetOldTablet()) {
                optionals.set(0);
            }
            if (struct.isSetNewTablets()) {
                optionals.set(1);
            }
            oprot.writeBitSet(optionals, 2);
            if (struct.isSetOldTablet()) {
                struct.oldTablet.write(oprot);
            }
            if (struct.isSetNewTablets()) {
                {
                    oprot.writeI32(struct.newTablets.size());
                    for (org.apache.accumulo.core.dataImpl.thrift.TKeyExtent _iter82 : struct.newTablets) {
                        _iter82.write(oprot);
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, TabletSplit struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(2);
            if (incoming.get(0)) {
                struct.oldTablet = new org.apache.accumulo.core.dataImpl.thrift.TKeyExtent();
                struct.oldTablet.read(iprot);
                struct.setOldTabletIsSet(true);
            }
            if (incoming.get(1)) {
                {
                    org.apache.thrift.protocol.TList _list83 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.newTablets = new java.util.ArrayList<org.apache.accumulo.core.dataImpl.thrift.TKeyExtent>(_list83.size);
                    @org.apache.thrift.annotation.Nullable
                    org.apache.accumulo.core.dataImpl.thrift.TKeyExtent _elem84;
                    for (int _i85 = 0; _i85 < _list83.size; ++_i85) {
                        _elem84 = new org.apache.accumulo.core.dataImpl.thrift.TKeyExtent();
                        _elem84.read(iprot);
                        struct.newTablets.add(_elem84);
                    }
                }
                struct.setNewTabletsIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}
