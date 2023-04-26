package org.apache.accumulo.core.gc.thrift;

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
public class GCStatus implements org.apache.thrift.TBase<GCStatus, GCStatus._Fields>, java.io.Serializable, Cloneable, Comparable<GCStatus> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("GCStatus");

    private static final org.apache.thrift.protocol.TField LAST_FIELD_DESC = new org.apache.thrift.protocol.TField("last", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

    private static final org.apache.thrift.protocol.TField LAST_LOG_FIELD_DESC = new org.apache.thrift.protocol.TField("lastLog", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

    private static final org.apache.thrift.protocol.TField CURRENT_FIELD_DESC = new org.apache.thrift.protocol.TField("current", org.apache.thrift.protocol.TType.STRUCT, (short) 3);

    private static final org.apache.thrift.protocol.TField CURRENT_LOG_FIELD_DESC = new org.apache.thrift.protocol.TField("currentLog", org.apache.thrift.protocol.TType.STRUCT, (short) 4);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new GCStatusStandardSchemeFactory());
        schemes.put(TupleScheme.class, new GCStatusTupleSchemeFactory());
    }

    public GcCycleStats last;

    public GcCycleStats lastLog;

    public GcCycleStats current;

    public GcCycleStats currentLog;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        LAST((short) 1, "last"), LAST_LOG((short) 2, "lastLog"), CURRENT((short) 3, "current"), CURRENT_LOG((short) 4, "currentLog");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return LAST;
                case 2:
                    return LAST_LOG;
                case 3:
                    return CURRENT;
                case 4:
                    return CURRENT_LOG;
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
        tmpMap.put(_Fields.LAST, new org.apache.thrift.meta_data.FieldMetaData("last", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, GcCycleStats.class)));
        tmpMap.put(_Fields.LAST_LOG, new org.apache.thrift.meta_data.FieldMetaData("lastLog", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, GcCycleStats.class)));
        tmpMap.put(_Fields.CURRENT, new org.apache.thrift.meta_data.FieldMetaData("current", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, GcCycleStats.class)));
        tmpMap.put(_Fields.CURRENT_LOG, new org.apache.thrift.meta_data.FieldMetaData("currentLog", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, GcCycleStats.class)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(GCStatus.class, metaDataMap);
    }

    public GCStatus() {
    }

    public GCStatus(GcCycleStats last, GcCycleStats lastLog, GcCycleStats current, GcCycleStats currentLog) {
        this();
        this.last = last;
        this.lastLog = lastLog;
        this.current = current;
        this.currentLog = currentLog;
    }

    public GCStatus(GCStatus other) {
        if (other.isSetLast()) {
            this.last = new GcCycleStats(other.last);
        }
        if (other.isSetLastLog()) {
            this.lastLog = new GcCycleStats(other.lastLog);
        }
        if (other.isSetCurrent()) {
            this.current = new GcCycleStats(other.current);
        }
        if (other.isSetCurrentLog()) {
            this.currentLog = new GcCycleStats(other.currentLog);
        }
    }

    public GCStatus deepCopy() {
        return new GCStatus(this);
    }

    @Override
    public void clear() {
        this.last = null;
        this.lastLog = null;
        this.current = null;
        this.currentLog = null;
    }

    public GcCycleStats getLast() {
        return this.last;
    }

    public GCStatus setLast(GcCycleStats last) {
        this.last = last;
        return this;
    }

    public void unsetLast() {
        this.last = null;
    }

    public boolean isSetLast() {
        return this.last != null;
    }

    public void setLastIsSet(boolean value) {
        if (!value) {
            this.last = null;
        }
    }

    public GcCycleStats getLastLog() {
        return this.lastLog;
    }

    public GCStatus setLastLog(GcCycleStats lastLog) {
        this.lastLog = lastLog;
        return this;
    }

    public void unsetLastLog() {
        this.lastLog = null;
    }

    public boolean isSetLastLog() {
        return this.lastLog != null;
    }

    public void setLastLogIsSet(boolean value) {
        if (!value) {
            this.lastLog = null;
        }
    }

    public GcCycleStats getCurrent() {
        return this.current;
    }

    public GCStatus setCurrent(GcCycleStats current) {
        this.current = current;
        return this;
    }

    public void unsetCurrent() {
        this.current = null;
    }

    public boolean isSetCurrent() {
        return this.current != null;
    }

    public void setCurrentIsSet(boolean value) {
        if (!value) {
            this.current = null;
        }
    }

    public GcCycleStats getCurrentLog() {
        return this.currentLog;
    }

    public GCStatus setCurrentLog(GcCycleStats currentLog) {
        this.currentLog = currentLog;
        return this;
    }

    public void unsetCurrentLog() {
        this.currentLog = null;
    }

    public boolean isSetCurrentLog() {
        return this.currentLog != null;
    }

    public void setCurrentLogIsSet(boolean value) {
        if (!value) {
            this.currentLog = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case LAST:
                if (value == null) {
                    unsetLast();
                } else {
                    setLast((GcCycleStats) value);
                }
                break;
            case LAST_LOG:
                if (value == null) {
                    unsetLastLog();
                } else {
                    setLastLog((GcCycleStats) value);
                }
                break;
            case CURRENT:
                if (value == null) {
                    unsetCurrent();
                } else {
                    setCurrent((GcCycleStats) value);
                }
                break;
            case CURRENT_LOG:
                if (value == null) {
                    unsetCurrentLog();
                } else {
                    setCurrentLog((GcCycleStats) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case LAST:
                return getLast();
            case LAST_LOG:
                return getLastLog();
            case CURRENT:
                return getCurrent();
            case CURRENT_LOG:
                return getCurrentLog();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case LAST:
                return isSetLast();
            case LAST_LOG:
                return isSetLastLog();
            case CURRENT:
                return isSetCurrent();
            case CURRENT_LOG:
                return isSetCurrentLog();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof GCStatus)
            return this.equals((GCStatus) that);
        return false;
    }

    public boolean equals(GCStatus that) {
        if (that == null)
            return false;
        boolean this_present_last = true && this.isSetLast();
        boolean that_present_last = true && that.isSetLast();
        if (this_present_last || that_present_last) {
            if (!(this_present_last && that_present_last))
                return false;
            if (!this.last.equals(that.last))
                return false;
        }
        boolean this_present_lastLog = true && this.isSetLastLog();
        boolean that_present_lastLog = true && that.isSetLastLog();
        if (this_present_lastLog || that_present_lastLog) {
            if (!(this_present_lastLog && that_present_lastLog))
                return false;
            if (!this.lastLog.equals(that.lastLog))
                return false;
        }
        boolean this_present_current = true && this.isSetCurrent();
        boolean that_present_current = true && that.isSetCurrent();
        if (this_present_current || that_present_current) {
            if (!(this_present_current && that_present_current))
                return false;
            if (!this.current.equals(that.current))
                return false;
        }
        boolean this_present_currentLog = true && this.isSetCurrentLog();
        boolean that_present_currentLog = true && that.isSetCurrentLog();
        if (this_present_currentLog || that_present_currentLog) {
            if (!(this_present_currentLog && that_present_currentLog))
                return false;
            if (!this.currentLog.equals(that.currentLog))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int compareTo(GCStatus other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetLast()).compareTo(other.isSetLast());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetLast()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.last, other.last);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetLastLog()).compareTo(other.isSetLastLog());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetLastLog()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.lastLog, other.lastLog);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetCurrent()).compareTo(other.isSetCurrent());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetCurrent()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.current, other.current);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetCurrentLog()).compareTo(other.isSetCurrentLog());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetCurrentLog()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.currentLog, other.currentLog);
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
        StringBuilder sb = new StringBuilder("GCStatus(");
        boolean first = true;
        sb.append("last:");
        if (this.last == null) {
            sb.append("null");
        } else {
            sb.append(this.last);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("lastLog:");
        if (this.lastLog == null) {
            sb.append("null");
        } else {
            sb.append(this.lastLog);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("current:");
        if (this.current == null) {
            sb.append("null");
        } else {
            sb.append(this.current);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("currentLog:");
        if (this.currentLog == null) {
            sb.append("null");
        } else {
            sb.append(this.currentLog);
        }
        first = false;
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
        if (last != null) {
            last.validate();
        }
        if (lastLog != null) {
            lastLog.validate();
        }
        if (current != null) {
            current.validate();
        }
        if (currentLog != null) {
            currentLog.validate();
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

    private static class GCStatusStandardSchemeFactory implements SchemeFactory {

        public GCStatusStandardScheme getScheme() {
            return new GCStatusStandardScheme();
        }
    }

    private static class GCStatusStandardScheme extends StandardScheme<GCStatus> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, GCStatus struct) throws org.apache.thrift.TException {
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
                            struct.last = new GcCycleStats();
                            struct.last.read(iprot);
                            struct.setLastIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.lastLog = new GcCycleStats();
                            struct.lastLog.read(iprot);
                            struct.setLastLogIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.current = new GcCycleStats();
                            struct.current.read(iprot);
                            struct.setCurrentIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.currentLog = new GcCycleStats();
                            struct.currentLog.read(iprot);
                            struct.setCurrentLogIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, GCStatus struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.last != null) {
                oprot.writeFieldBegin(LAST_FIELD_DESC);
                struct.last.write(oprot);
                oprot.writeFieldEnd();
            }
            if (struct.lastLog != null) {
                oprot.writeFieldBegin(LAST_LOG_FIELD_DESC);
                struct.lastLog.write(oprot);
                oprot.writeFieldEnd();
            }
            if (struct.current != null) {
                oprot.writeFieldBegin(CURRENT_FIELD_DESC);
                struct.current.write(oprot);
                oprot.writeFieldEnd();
            }
            if (struct.currentLog != null) {
                oprot.writeFieldBegin(CURRENT_LOG_FIELD_DESC);
                struct.currentLog.write(oprot);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class GCStatusTupleSchemeFactory implements SchemeFactory {

        public GCStatusTupleScheme getScheme() {
            return new GCStatusTupleScheme();
        }
    }

    private static class GCStatusTupleScheme extends TupleScheme<GCStatus> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, GCStatus struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetLast()) {
                optionals.set(0);
            }
            if (struct.isSetLastLog()) {
                optionals.set(1);
            }
            if (struct.isSetCurrent()) {
                optionals.set(2);
            }
            if (struct.isSetCurrentLog()) {
                optionals.set(3);
            }
            oprot.writeBitSet(optionals, 4);
            if (struct.isSetLast()) {
                struct.last.write(oprot);
            }
            if (struct.isSetLastLog()) {
                struct.lastLog.write(oprot);
            }
            if (struct.isSetCurrent()) {
                struct.current.write(oprot);
            }
            if (struct.isSetCurrentLog()) {
                struct.currentLog.write(oprot);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, GCStatus struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(4);
            if (incoming.get(0)) {
                struct.last = new GcCycleStats();
                struct.last.read(iprot);
                struct.setLastIsSet(true);
            }
            if (incoming.get(1)) {
                struct.lastLog = new GcCycleStats();
                struct.lastLog.read(iprot);
                struct.setLastLogIsSet(true);
            }
            if (incoming.get(2)) {
                struct.current = new GcCycleStats();
                struct.current.read(iprot);
                struct.setCurrentIsSet(true);
            }
            if (incoming.get(3)) {
                struct.currentLog = new GcCycleStats();
                struct.currentLog.read(iprot);
                struct.setCurrentLogIsSet(true);
            }
        }
    }
}