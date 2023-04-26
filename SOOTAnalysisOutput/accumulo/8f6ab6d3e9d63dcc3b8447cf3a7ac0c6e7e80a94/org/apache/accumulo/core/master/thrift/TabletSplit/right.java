package org.apache.accumulo.core.master.thrift;

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
public class TabletSplit implements org.apache.thrift.TBase<TabletSplit, TabletSplit._Fields>, java.io.Serializable, Cloneable, Comparable<TabletSplit> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TabletSplit");

    private static final org.apache.thrift.protocol.TField OLD_TABLET_FIELD_DESC = new org.apache.thrift.protocol.TField("oldTablet", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

    private static final org.apache.thrift.protocol.TField NEW_TABLETS_FIELD_DESC = new org.apache.thrift.protocol.TField("newTablets", org.apache.thrift.protocol.TType.LIST, (short) 2);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new TabletSplitStandardSchemeFactory());
        schemes.put(TupleScheme.class, new TabletSplitTupleSchemeFactory());
    }

    public org.apache.accumulo.core.data.thrift.TKeyExtent oldTablet;

    public List<org.apache.accumulo.core.data.thrift.TKeyExtent> newTablets;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        OLD_TABLET((short) 1, "oldTablet"), NEW_TABLETS((short) 2, "newTablets");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

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
        tmpMap.put(_Fields.OLD_TABLET, new org.apache.thrift.meta_data.FieldMetaData("oldTablet", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.data.thrift.TKeyExtent.class)));
        tmpMap.put(_Fields.NEW_TABLETS, new org.apache.thrift.meta_data.FieldMetaData("newTablets", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.data.thrift.TKeyExtent.class))));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TabletSplit.class, metaDataMap);
    }

    public TabletSplit() {
    }

    public TabletSplit(org.apache.accumulo.core.data.thrift.TKeyExtent oldTablet, List<org.apache.accumulo.core.data.thrift.TKeyExtent> newTablets) {
        this();
        this.oldTablet = oldTablet;
        this.newTablets = newTablets;
    }

    public TabletSplit(TabletSplit other) {
        if (other.isSetOldTablet()) {
            this.oldTablet = new org.apache.accumulo.core.data.thrift.TKeyExtent(other.oldTablet);
        }
        if (other.isSetNewTablets()) {
            List<org.apache.accumulo.core.data.thrift.TKeyExtent> __this__newTablets = new ArrayList<org.apache.accumulo.core.data.thrift.TKeyExtent>(other.newTablets.size());
            for (org.apache.accumulo.core.data.thrift.TKeyExtent other_element : other.newTablets) {
                __this__newTablets.add(new org.apache.accumulo.core.data.thrift.TKeyExtent(other_element));
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

    public org.apache.accumulo.core.data.thrift.TKeyExtent getOldTablet() {
        return this.oldTablet;
    }

    public TabletSplit setOldTablet(org.apache.accumulo.core.data.thrift.TKeyExtent oldTablet) {
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

    public java.util.Iterator<org.apache.accumulo.core.data.thrift.TKeyExtent> getNewTabletsIterator() {
        return (this.newTablets == null) ? null : this.newTablets.iterator();
    }

    public void addToNewTablets(org.apache.accumulo.core.data.thrift.TKeyExtent elem) {
        if (this.newTablets == null) {
            this.newTablets = new ArrayList<org.apache.accumulo.core.data.thrift.TKeyExtent>();
        }
        this.newTablets.add(elem);
    }

    public List<org.apache.accumulo.core.data.thrift.TKeyExtent> getNewTablets() {
        return this.newTablets;
    }

    public TabletSplit setNewTablets(List<org.apache.accumulo.core.data.thrift.TKeyExtent> newTablets) {
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

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case OLD_TABLET:
                if (value == null) {
                    unsetOldTablet();
                } else {
                    setOldTablet((org.apache.accumulo.core.data.thrift.TKeyExtent) value);
                }
                break;
            case NEW_TABLETS:
                if (value == null) {
                    unsetNewTablets();
                } else {
                    setNewTablets((List<org.apache.accumulo.core.data.thrift.TKeyExtent>) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case OLD_TABLET:
                return getOldTablet();
            case NEW_TABLETS:
                return getNewTablets();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case OLD_TABLET:
                return isSetOldTablet();
            case NEW_TABLETS:
                return isSetNewTablets();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof TabletSplit)
            return this.equals((TabletSplit) that);
        return false;
    }

    public boolean equals(TabletSplit that) {
        if (that == null)
            return false;
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
        return 0;
    }

    @Override
    public int compareTo(TabletSplit other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetOldTablet()).compareTo(other.isSetOldTablet());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetOldTablet()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.oldTablet, other.oldTablet);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetNewTablets()).compareTo(other.isSetNewTablets());
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
        StringBuilder sb = new StringBuilder("TabletSplit(");
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

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        try {
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class TabletSplitStandardSchemeFactory implements SchemeFactory {

        public TabletSplitStandardScheme getScheme() {
            return new TabletSplitStandardScheme();
        }
    }

    private static class TabletSplitStandardScheme extends StandardScheme<TabletSplit> {

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
                            struct.oldTablet = new org.apache.accumulo.core.data.thrift.TKeyExtent();
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
                                struct.newTablets = new ArrayList<org.apache.accumulo.core.data.thrift.TKeyExtent>(_list78.size);
                                for (int _i79 = 0; _i79 < _list78.size; ++_i79) {
                                    org.apache.accumulo.core.data.thrift.TKeyExtent _elem80;
                                    _elem80 = new org.apache.accumulo.core.data.thrift.TKeyExtent();
                                    _elem80.read(iprot);
                                    struct.newTablets.add(_elem80);
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
                    for (org.apache.accumulo.core.data.thrift.TKeyExtent _iter81 : struct.newTablets) {
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

    private static class TabletSplitTupleSchemeFactory implements SchemeFactory {

        public TabletSplitTupleScheme getScheme() {
            return new TabletSplitTupleScheme();
        }
    }

    private static class TabletSplitTupleScheme extends TupleScheme<TabletSplit> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, TabletSplit struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
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
                    for (org.apache.accumulo.core.data.thrift.TKeyExtent _iter82 : struct.newTablets) {
                        _iter82.write(oprot);
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, TabletSplit struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(2);
            if (incoming.get(0)) {
                struct.oldTablet = new org.apache.accumulo.core.data.thrift.TKeyExtent();
                struct.oldTablet.read(iprot);
                struct.setOldTabletIsSet(true);
            }
            if (incoming.get(1)) {
                {
                    org.apache.thrift.protocol.TList _list83 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.newTablets = new ArrayList<org.apache.accumulo.core.data.thrift.TKeyExtent>(_list83.size);
                    for (int _i84 = 0; _i84 < _list83.size; ++_i84) {
                        org.apache.accumulo.core.data.thrift.TKeyExtent _elem85;
                        _elem85 = new org.apache.accumulo.core.data.thrift.TKeyExtent();
                        _elem85.read(iprot);
                        struct.newTablets.add(_elem85);
                    }
                }
                struct.setNewTabletsIsSet(true);
            }
        }
    }
}
