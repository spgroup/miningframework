package org.apache.accumulo.proxy.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class Range implements org.apache.thrift.TBase<Range, Range._Fields>, java.io.Serializable, Cloneable, Comparable<Range> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Range");

    private static final org.apache.thrift.protocol.TField START_FIELD_DESC = new org.apache.thrift.protocol.TField("start", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

    private static final org.apache.thrift.protocol.TField START_INCLUSIVE_FIELD_DESC = new org.apache.thrift.protocol.TField("startInclusive", org.apache.thrift.protocol.TType.BOOL, (short) 2);

    private static final org.apache.thrift.protocol.TField STOP_FIELD_DESC = new org.apache.thrift.protocol.TField("stop", org.apache.thrift.protocol.TType.STRUCT, (short) 3);

    private static final org.apache.thrift.protocol.TField STOP_INCLUSIVE_FIELD_DESC = new org.apache.thrift.protocol.TField("stopInclusive", org.apache.thrift.protocol.TType.BOOL, (short) 4);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new RangeStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new RangeTupleSchemeFactory();

    @org.apache.thrift.annotation.Nullable
    public Key start;

    public boolean startInclusive;

    @org.apache.thrift.annotation.Nullable
    public Key stop;

    public boolean stopInclusive;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        START((short) 1, "start"), START_INCLUSIVE((short) 2, "startInclusive"), STOP((short) 3, "stop"), STOP_INCLUSIVE((short) 4, "stopInclusive");

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
                    return START;
                case 2:
                    return START_INCLUSIVE;
                case 3:
                    return STOP;
                case 4:
                    return STOP_INCLUSIVE;
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

    private static final int __STARTINCLUSIVE_ISSET_ID = 0;

    private static final int __STOPINCLUSIVE_ISSET_ID = 1;

    private byte __isset_bitfield = 0;

    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.START, new org.apache.thrift.meta_data.FieldMetaData("start", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Key.class)));
        tmpMap.put(_Fields.START_INCLUSIVE, new org.apache.thrift.meta_data.FieldMetaData("startInclusive", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
        tmpMap.put(_Fields.STOP, new org.apache.thrift.meta_data.FieldMetaData("stop", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Key.class)));
        tmpMap.put(_Fields.STOP_INCLUSIVE, new org.apache.thrift.meta_data.FieldMetaData("stopInclusive", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Range.class, metaDataMap);
    }

    public Range() {
    }

    public Range(Key start, boolean startInclusive, Key stop, boolean stopInclusive) {
        this();
        this.start = start;
        this.startInclusive = startInclusive;
        setStartInclusiveIsSet(true);
        this.stop = stop;
        this.stopInclusive = stopInclusive;
        setStopInclusiveIsSet(true);
    }

    public Range(Range other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetStart()) {
            this.start = new Key(other.start);
        }
        this.startInclusive = other.startInclusive;
        if (other.isSetStop()) {
            this.stop = new Key(other.stop);
        }
        this.stopInclusive = other.stopInclusive;
    }

    public Range deepCopy() {
        return new Range(this);
    }

    @Override
    public void clear() {
        this.start = null;
        setStartInclusiveIsSet(false);
        this.startInclusive = false;
        this.stop = null;
        setStopInclusiveIsSet(false);
        this.stopInclusive = false;
    }

    @org.apache.thrift.annotation.Nullable
    public Key getStart() {
        return this.start;
    }

    public Range setStart(@org.apache.thrift.annotation.Nullable Key start) {
        this.start = start;
        return this;
    }

    public void unsetStart() {
        this.start = null;
    }

    public boolean isSetStart() {
        return this.start != null;
    }

    public void setStartIsSet(boolean value) {
        if (!value) {
            this.start = null;
        }
    }

    public boolean isStartInclusive() {
        return this.startInclusive;
    }

    public Range setStartInclusive(boolean startInclusive) {
        this.startInclusive = startInclusive;
        setStartInclusiveIsSet(true);
        return this;
    }

    public void unsetStartInclusive() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __STARTINCLUSIVE_ISSET_ID);
    }

    public boolean isSetStartInclusive() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __STARTINCLUSIVE_ISSET_ID);
    }

    public void setStartInclusiveIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __STARTINCLUSIVE_ISSET_ID, value);
    }

    @org.apache.thrift.annotation.Nullable
    public Key getStop() {
        return this.stop;
    }

    public Range setStop(@org.apache.thrift.annotation.Nullable Key stop) {
        this.stop = stop;
        return this;
    }

    public void unsetStop() {
        this.stop = null;
    }

    public boolean isSetStop() {
        return this.stop != null;
    }

    public void setStopIsSet(boolean value) {
        if (!value) {
            this.stop = null;
        }
    }

    public boolean isStopInclusive() {
        return this.stopInclusive;
    }

    public Range setStopInclusive(boolean stopInclusive) {
        this.stopInclusive = stopInclusive;
        setStopInclusiveIsSet(true);
        return this;
    }

    public void unsetStopInclusive() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __STOPINCLUSIVE_ISSET_ID);
    }

    public boolean isSetStopInclusive() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __STOPINCLUSIVE_ISSET_ID);
    }

    public void setStopInclusiveIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __STOPINCLUSIVE_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case START:
                if (value == null) {
                    unsetStart();
                } else {
                    setStart((Key) value);
                }
                break;
            case START_INCLUSIVE:
                if (value == null) {
                    unsetStartInclusive();
                } else {
                    setStartInclusive((java.lang.Boolean) value);
                }
                break;
            case STOP:
                if (value == null) {
                    unsetStop();
                } else {
                    setStop((Key) value);
                }
                break;
            case STOP_INCLUSIVE:
                if (value == null) {
                    unsetStopInclusive();
                } else {
                    setStopInclusive((java.lang.Boolean) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case START:
                return getStart();
            case START_INCLUSIVE:
                return isStartInclusive();
            case STOP:
                return getStop();
            case STOP_INCLUSIVE:
                return isStopInclusive();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }
        switch(field) {
            case START:
                return isSetStart();
            case START_INCLUSIVE:
                return isSetStartInclusive();
            case STOP:
                return isSetStop();
            case STOP_INCLUSIVE:
                return isSetStopInclusive();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof Range)
            return this.equals((Range) that);
        return false;
    }

    public boolean equals(Range that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
        boolean this_present_start = true && this.isSetStart();
        boolean that_present_start = true && that.isSetStart();
        if (this_present_start || that_present_start) {
            if (!(this_present_start && that_present_start))
                return false;
            if (!this.start.equals(that.start))
                return false;
        }
        boolean this_present_startInclusive = true;
        boolean that_present_startInclusive = true;
        if (this_present_startInclusive || that_present_startInclusive) {
            if (!(this_present_startInclusive && that_present_startInclusive))
                return false;
            if (this.startInclusive != that.startInclusive)
                return false;
        }
        boolean this_present_stop = true && this.isSetStop();
        boolean that_present_stop = true && that.isSetStop();
        if (this_present_stop || that_present_stop) {
            if (!(this_present_stop && that_present_stop))
                return false;
            if (!this.stop.equals(that.stop))
                return false;
        }
        boolean this_present_stopInclusive = true;
        boolean that_present_stopInclusive = true;
        if (this_present_stopInclusive || that_present_stopInclusive) {
            if (!(this_present_stopInclusive && that_present_stopInclusive))
                return false;
            if (this.stopInclusive != that.stopInclusive)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 8191 + ((isSetStart()) ? 131071 : 524287);
        if (isSetStart())
            hashCode = hashCode * 8191 + start.hashCode();
        hashCode = hashCode * 8191 + ((startInclusive) ? 131071 : 524287);
        hashCode = hashCode * 8191 + ((isSetStop()) ? 131071 : 524287);
        if (isSetStop())
            hashCode = hashCode * 8191 + stop.hashCode();
        hashCode = hashCode * 8191 + ((stopInclusive) ? 131071 : 524287);
        return hashCode;
    }

    @Override
    public int compareTo(Range other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetStart()).compareTo(other.isSetStart());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetStart()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.start, other.start);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetStartInclusive()).compareTo(other.isSetStartInclusive());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetStartInclusive()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.startInclusive, other.startInclusive);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetStop()).compareTo(other.isSetStop());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetStop()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.stop, other.stop);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetStopInclusive()).compareTo(other.isSetStopInclusive());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetStopInclusive()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.stopInclusive, other.stopInclusive);
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("Range(");
        boolean first = true;
        sb.append("start:");
        if (this.start == null) {
            sb.append("null");
        } else {
            sb.append(this.start);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("startInclusive:");
        sb.append(this.startInclusive);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("stop:");
        if (this.stop == null) {
            sb.append("null");
        } else {
            sb.append(this.stop);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("stopInclusive:");
        sb.append(this.stopInclusive);
        first = false;
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
        if (start != null) {
            start.validate();
        }
        if (stop != null) {
            stop.validate();
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
            __isset_bitfield = 0;
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class RangeStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public RangeStandardScheme getScheme() {
            return new RangeStandardScheme();
        }
    }

    private static class RangeStandardScheme extends org.apache.thrift.scheme.StandardScheme<Range> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, Range struct) throws org.apache.thrift.TException {
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
                            struct.start = new Key();
                            struct.start.read(iprot);
                            struct.setStartIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
                            struct.startInclusive = iprot.readBool();
                            struct.setStartInclusiveIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.stop = new Key();
                            struct.stop.read(iprot);
                            struct.setStopIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
                            struct.stopInclusive = iprot.readBool();
                            struct.setStopInclusiveIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, Range struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.start != null) {
                oprot.writeFieldBegin(START_FIELD_DESC);
                struct.start.write(oprot);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(START_INCLUSIVE_FIELD_DESC);
            oprot.writeBool(struct.startInclusive);
            oprot.writeFieldEnd();
            if (struct.stop != null) {
                oprot.writeFieldBegin(STOP_FIELD_DESC);
                struct.stop.write(oprot);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(STOP_INCLUSIVE_FIELD_DESC);
            oprot.writeBool(struct.stopInclusive);
            oprot.writeFieldEnd();
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class RangeTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public RangeTupleScheme getScheme() {
            return new RangeTupleScheme();
        }
    }

    private static class RangeTupleScheme extends org.apache.thrift.scheme.TupleScheme<Range> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, Range struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
            if (struct.isSetStart()) {
                optionals.set(0);
            }
            if (struct.isSetStartInclusive()) {
                optionals.set(1);
            }
            if (struct.isSetStop()) {
                optionals.set(2);
            }
            if (struct.isSetStopInclusive()) {
                optionals.set(3);
            }
            oprot.writeBitSet(optionals, 4);
            if (struct.isSetStart()) {
                struct.start.write(oprot);
            }
            if (struct.isSetStartInclusive()) {
                oprot.writeBool(struct.startInclusive);
            }
            if (struct.isSetStop()) {
                struct.stop.write(oprot);
            }
            if (struct.isSetStopInclusive()) {
                oprot.writeBool(struct.stopInclusive);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, Range struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(4);
            if (incoming.get(0)) {
                struct.start = new Key();
                struct.start.read(iprot);
                struct.setStartIsSet(true);
            }
            if (incoming.get(1)) {
                struct.startInclusive = iprot.readBool();
                struct.setStartInclusiveIsSet(true);
            }
            if (incoming.get(2)) {
                struct.stop = new Key();
                struct.stop.read(iprot);
                struct.setStopIsSet(true);
            }
            if (incoming.get(3)) {
                struct.stopInclusive = iprot.readBool();
                struct.setStopInclusiveIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}
