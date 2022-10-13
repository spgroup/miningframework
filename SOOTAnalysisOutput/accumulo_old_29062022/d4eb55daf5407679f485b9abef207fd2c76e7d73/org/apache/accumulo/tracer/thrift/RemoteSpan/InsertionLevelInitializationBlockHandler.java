package org.apache.accumulo.tracer.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class RemoteSpan implements org.apache.thrift.TBase<RemoteSpan, RemoteSpan._Fields>, java.io.Serializable, Cloneable, Comparable<RemoteSpan> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("RemoteSpan");

    private static final org.apache.thrift.protocol.TField SENDER_FIELD_DESC = new org.apache.thrift.protocol.TField("sender", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final org.apache.thrift.protocol.TField SVC_FIELD_DESC = new org.apache.thrift.protocol.TField("svc", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.protocol.TField TRACE_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("traceId", org.apache.thrift.protocol.TType.I64, (short) 3);

    private static final org.apache.thrift.protocol.TField SPAN_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("spanId", org.apache.thrift.protocol.TType.I64, (short) 4);

    private static final org.apache.thrift.protocol.TField PARENT_IDS_FIELD_DESC = new org.apache.thrift.protocol.TField("parentIds", org.apache.thrift.protocol.TType.LIST, (short) 11);

    private static final org.apache.thrift.protocol.TField START_FIELD_DESC = new org.apache.thrift.protocol.TField("start", org.apache.thrift.protocol.TType.I64, (short) 6);

    private static final org.apache.thrift.protocol.TField STOP_FIELD_DESC = new org.apache.thrift.protocol.TField("stop", org.apache.thrift.protocol.TType.I64, (short) 7);

    private static final org.apache.thrift.protocol.TField DESCRIPTION_FIELD_DESC = new org.apache.thrift.protocol.TField("description", org.apache.thrift.protocol.TType.STRING, (short) 8);

    private static final org.apache.thrift.protocol.TField DATA_FIELD_DESC = new org.apache.thrift.protocol.TField("data", org.apache.thrift.protocol.TType.MAP, (short) 9);

    private static final org.apache.thrift.protocol.TField ANNOTATIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("annotations", org.apache.thrift.protocol.TType.LIST, (short) 10);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new RemoteSpanStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new RemoteSpanTupleSchemeFactory();

    @org.apache.thrift.annotation.Nullable
    public java.lang.String sender;

    @org.apache.thrift.annotation.Nullable
    public java.lang.String svc;

    public long traceId;

    public long spanId;

    @org.apache.thrift.annotation.Nullable
    public java.util.List<java.lang.Long> parentIds;

    public long start;

    public long stop;

    @org.apache.thrift.annotation.Nullable
    public java.lang.String description;

    @org.apache.thrift.annotation.Nullable
    public java.util.Map<java.lang.String, java.lang.String> data;

    @org.apache.thrift.annotation.Nullable
    public java.util.List<Annotation> annotations;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        SENDER((short) 1, "sender"),
        SVC((short) 2, "svc"),
        TRACE_ID((short) 3, "traceId"),
        SPAN_ID((short) 4, "spanId"),
        PARENT_IDS((short) 11, "parentIds"),,
        START((short) 6, "start"),
        STOP((short) 7, "stop"),
        DESCRIPTION((short) 8, "description"),
        DATA((short) 9, "data"),
        ANNOTATIONS((short) 10, "annotations");

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
                    return SENDER;
                case 2:
                    return SVC;
                case 3:
                    return TRACE_ID;
                case 4:
                    return SPAN_ID;
                case 11:
                    return PARENT_IDS;
                case 6:
                    return START;
                case 7:
                    return STOP;
                case 8:
                    return DESCRIPTION;
                case 9:
                    return DATA;
                case 10:
                    return ANNOTATIONS;
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

    private static final int __TRACEID_ISSET_ID = 0;

    private static final int __SPANID_ISSET_ID = 1;

    private static final int __START_ISSET_ID = 2;

    private static final int __STOP_ISSET_ID = 3;

    private byte __isset_bitfield = 0;

    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.SENDER, new org.apache.thrift.meta_data.FieldMetaData("sender", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.SVC, new org.apache.thrift.meta_data.FieldMetaData("svc", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.TRACE_ID, new org.apache.thrift.meta_data.FieldMetaData("traceId", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.SPAN_ID, new org.apache.thrift.meta_data.FieldMetaData("spanId", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.PARENT_IDS, new org.apache.thrift.meta_data.FieldMetaData("parentIds", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64))));
        tmpMap.put(_Fields.START, new org.apache.thrift.meta_data.FieldMetaData("start", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.STOP, new org.apache.thrift.meta_data.FieldMetaData("stop", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.DESCRIPTION, new org.apache.thrift.meta_data.FieldMetaData("description", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.DATA, new org.apache.thrift.meta_data.FieldMetaData("data", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
        tmpMap.put(_Fields.ANNOTATIONS, new org.apache.thrift.meta_data.FieldMetaData("annotations", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Annotation.class))));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(RemoteSpan.class, metaDataMap);
    }

    public RemoteSpan() {
    }

    public RemoteSpan(java.lang.String sender, java.lang.String svc, long traceId, long spanId, java.util.List<java.lang.Long> parentIds, long start, long stop, java.lang.String description, java.util.Map<java.lang.String, java.lang.String> data, java.util.List<Annotation> annotations) {
        this();
        this.sender = sender;
        this.svc = svc;
        this.traceId = traceId;
        setTraceIdIsSet(true);
        this.spanId = spanId;
        setSpanIdIsSet(true);
        this.parentIds = parentIds;
        this.start = start;
        setStartIsSet(true);
        this.stop = stop;
        setStopIsSet(true);
        this.description = description;
        this.data = data;
        this.annotations = annotations;
    }

    public RemoteSpan(RemoteSpan other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetSender()) {
            this.sender = other.sender;
        }
        if (other.isSetSvc()) {
            this.svc = other.svc;
        }
        this.traceId = other.traceId;
        this.spanId = other.spanId;
        if (other.isSetParentIds()) {
            java.util.List<java.lang.Long> __this__parentIds = new java.util.ArrayList<java.lang.Long>(other.parentIds);
            this.parentIds = __this__parentIds;
        }
        this.start = other.start;
        this.stop = other.stop;
        if (other.isSetDescription()) {
            this.description = other.description;
        }
        if (other.isSetData()) {
            java.util.Map<java.lang.String, java.lang.String> __this__data = new java.util.HashMap<java.lang.String, java.lang.String>(other.data);
            this.data = __this__data;
        }
        if (other.isSetAnnotations()) {
            java.util.List<Annotation> __this__annotations = new java.util.ArrayList<Annotation>(other.annotations.size());
            for (Annotation other_element : other.annotations) {
                __this__annotations.add(new Annotation(other_element));
            }
            this.annotations = __this__annotations;
        }
    }

    public RemoteSpan deepCopy() {
        return new RemoteSpan(this);
    }

    @Override
    public void clear() {
        this.sender = null;
        this.svc = null;
        setTraceIdIsSet(false);
        this.traceId = 0;
        setSpanIdIsSet(false);
        this.spanId = 0;
        this.parentIds = null;
        setStartIsSet(false);
        this.start = 0;
        setStopIsSet(false);
        this.stop = 0;
        this.description = null;
        this.data = null;
        this.annotations = null;
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.String getSender() {
        return this.sender;
    }

    public RemoteSpan setSender(@org.apache.thrift.annotation.Nullable java.lang.String sender) {
        this.sender = sender;
        return this;
    }

    public void unsetSender() {
        this.sender = null;
    }

    public boolean isSetSender() {
        return this.sender != null;
    }

    public void setSenderIsSet(boolean value) {
        if (!value) {
            this.sender = null;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.String getSvc() {
        return this.svc;
    }

    public RemoteSpan setSvc(@org.apache.thrift.annotation.Nullable java.lang.String svc) {
        this.svc = svc;
        return this;
    }

    public void unsetSvc() {
        this.svc = null;
    }

    public boolean isSetSvc() {
        return this.svc != null;
    }

    public void setSvcIsSet(boolean value) {
        if (!value) {
            this.svc = null;
        }
    }

    public long getTraceId() {
        return this.traceId;
    }

    public RemoteSpan setTraceId(long traceId) {
        this.traceId = traceId;
        setTraceIdIsSet(true);
        return this;
    }

    public void unsetTraceId() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __TRACEID_ISSET_ID);
    }

    public boolean isSetTraceId() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __TRACEID_ISSET_ID);
    }

    public void setTraceIdIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __TRACEID_ISSET_ID, value);
    }

    public long getSpanId() {
        return this.spanId;
    }

    public RemoteSpan setSpanId(long spanId) {
        this.spanId = spanId;
        setSpanIdIsSet(true);
        return this;
    }

    public void unsetSpanId() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __SPANID_ISSET_ID);
    }

    public boolean isSetSpanId() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __SPANID_ISSET_ID);
    }

    public void setSpanIdIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __SPANID_ISSET_ID, value);
    }

    public int getParentIdsSize() {
        return (this.parentIds == null) ? 0 : this.parentIds.size();
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.Iterator<java.lang.Long> getParentIdsIterator() {
        return (this.parentIds == null) ? null : this.parentIds.iterator();
    }

    public void addToParentIds(long elem) {
        if (this.parentIds == null) {
            this.parentIds = new java.util.ArrayList<java.lang.Long>();
        }
        this.parentIds.add(elem);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.List<java.lang.Long> getParentIds() {
        return this.parentIds;
    }

    public RemoteSpan setParentIds(@org.apache.thrift.annotation.Nullable java.util.List<java.lang.Long> parentIds) {
        this.parentIds = parentIds;
        return this;
    }

    public void unsetParentIds() {
        this.parentIds = null;
    }

    public boolean isSetParentIds() {
        return this.parentIds != null;
    }

    public void setParentIdsIsSet(boolean value) {
        if (!value) {
            this.parentIds = null;
        }
    }

    public long getStart() {
        return this.start;
    }

    public RemoteSpan setStart(long start) {
        this.start = start;
        setStartIsSet(true);
        return this;
    }

    public void unsetStart() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __START_ISSET_ID);
    }

    public boolean isSetStart() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __START_ISSET_ID);
    }

    public void setStartIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __START_ISSET_ID, value);
    }

    public long getStop() {
        return this.stop;
    }

    public RemoteSpan setStop(long stop) {
        this.stop = stop;
        setStopIsSet(true);
        return this;
    }

    public void unsetStop() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __STOP_ISSET_ID);
    }

    public boolean isSetStop() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __STOP_ISSET_ID);
    }

    public void setStopIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __STOP_ISSET_ID, value);
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.String getDescription() {
        return this.description;
    }

    public RemoteSpan setDescription(@org.apache.thrift.annotation.Nullable java.lang.String description) {
        this.description = description;
        return this;
    }

    public void unsetDescription() {
        this.description = null;
    }

    public boolean isSetDescription() {
        return this.description != null;
    }

    public void setDescriptionIsSet(boolean value) {
        if (!value) {
            this.description = null;
        }
    }

    public int getDataSize() {
        return (this.data == null) ? 0 : this.data.size();
    }

    public void putToData(java.lang.String key, java.lang.String val) {
        if (this.data == null) {
            this.data = new java.util.HashMap<java.lang.String, java.lang.String>();
        }
        this.data.put(key, val);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.Map<java.lang.String, java.lang.String> getData() {
        return this.data;
    }

    public RemoteSpan setData(@org.apache.thrift.annotation.Nullable java.util.Map<java.lang.String, java.lang.String> data) {
        this.data = data;
        return this;
    }

    public void unsetData() {
        this.data = null;
    }

    public boolean isSetData() {
        return this.data != null;
    }

    public void setDataIsSet(boolean value) {
        if (!value) {
            this.data = null;
        }
    }

    public int getAnnotationsSize() {
        return (this.annotations == null) ? 0 : this.annotations.size();
    }

    @org.apache.thrift.annotation.Nullable
public java.util.Iterator<Annotation> getAnnotationsIterator() {
        return (this.annotations == null) ? null : this.annotations.iterator();
    }

    public void addToAnnotations(Annotation elem) {
        if (this.annotations == null) {
            this.annotations = new java.util.ArrayList<Annotation>();
        }
        this.annotations.add(elem);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.List<Annotation> getAnnotations() {
        return this.annotations;
    }

    public RemoteSpan setAnnotations(@org.apache.thrift.annotation.Nullable java.util.List<Annotation> annotations) {
        this.annotations = annotations;
        return this;
    }

    public void unsetAnnotations() {
        this.annotations = null;
    }

    public boolean isSetAnnotations() {
        return this.annotations != null;
    }

    public void setAnnotationsIsSet(boolean value) {
        if (!value) {
            this.annotations = null;
        }
    }

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case SENDER:
                if (value == null) {
                    unsetSender();
                } else {
                    setSender((java.lang.String) value);
                }
                break;
            case SVC:
                if (value == null) {
                    unsetSvc();
                } else {
                    setSvc((java.lang.String) value);
                }
                break;
            case TRACE_ID:
                if (value == null) {
                    unsetTraceId();
                } else {
                    setTraceId((java.lang.Long) value);
                }
                break;
            case SPAN_ID:
                if (value == null) {
                    unsetSpanId();
                } else {
                    setSpanId((java.lang.Long) value);
                }
                break;
            case PARENT_IDS:
                if (value == null) {
                    unsetParentIds();
                } else {
                    setParentIds((java.util.List<java.lang.Long>) value);
                }
                break;
            case START:
                if (value == null) {
                    unsetStart();
                } else {
                    setStart((java.lang.Long) value);
                }
                break;
            case STOP:
                if (value == null) {
                    unsetStop();
                } else {
                    setStop((java.lang.Long) value);
                }
                break;
            case DESCRIPTION:
                if (value == null) {
                    unsetDescription();
                } else {
                    setDescription((java.lang.String) value);
                }
                break;
            case DATA:
                if (value == null) {
                    unsetData();
                } else {
                    setData((java.util.Map<java.lang.String, java.lang.String>) value);
                }
                break;
            case ANNOTATIONS:
                if (value == null) {
                    unsetAnnotations();
                } else {
                    setAnnotations((java.util.List<Annotation>) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case SENDER:
                return getSender();
            case SVC:
                return getSvc();
            case TRACE_ID:
                return getTraceId();
            case SPAN_ID:
                return getSpanId();
            case PARENT_IDS:
                return getParentIds();
            case START:
                return getStart();
            case STOP:
                return getStop();
            case DESCRIPTION:
                return getDescription();
            case DATA:
                return getData();
            case ANNOTATIONS:
                return getAnnotations();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }
        switch(field) {
            case SENDER:
                return isSetSender();
            case SVC:
                return isSetSvc();
            case TRACE_ID:
                return isSetTraceId();
            case SPAN_ID:
                return isSetSpanId();
            case PARENT_IDS:
                return isSetParentIds();
            case START:
                return isSetStart();
            case STOP:
                return isSetStop();
            case DESCRIPTION:
                return isSetDescription();
            case DATA:
                return isSetData();
            case ANNOTATIONS:
                return isSetAnnotations();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof RemoteSpan)
            return this.equals((RemoteSpan) that);
        return false;
    }

    public boolean equals(RemoteSpan that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
        boolean this_present_sender = true && this.isSetSender();
        boolean that_present_sender = true && that.isSetSender();
        if (this_present_sender || that_present_sender) {
            if (!(this_present_sender && that_present_sender))
                return false;
            if (!this.sender.equals(that.sender))
                return false;
        }
        boolean this_present_svc = true && this.isSetSvc();
        boolean that_present_svc = true && that.isSetSvc();
        if (this_present_svc || that_present_svc) {
            if (!(this_present_svc && that_present_svc))
                return false;
            if (!this.svc.equals(that.svc))
                return false;
        }
        boolean this_present_traceId = true;
        boolean that_present_traceId = true;
        if (this_present_traceId || that_present_traceId) {
            if (!(this_present_traceId && that_present_traceId))
                return false;
            if (this.traceId != that.traceId)
                return false;
        }
        boolean this_present_spanId = true;
        boolean that_present_spanId = true;
        if (this_present_spanId || that_present_spanId) {
            if (!(this_present_spanId && that_present_spanId))
                return false;
            if (this.spanId != that.spanId)
                return false;
        }
        boolean this_present_parentIds = true && this.isSetParentIds();
        boolean that_present_parentIds = true && that.isSetParentIds();
        if (this_present_parentIds || that_present_parentIds) {
            if (!(this_present_parentIds && that_present_parentIds))
                return false;
            if (!this.parentIds.equals(that.parentIds))
                return false;
        }
        boolean this_present_start = true;
        boolean that_present_start = true;
        if (this_present_start || that_present_start) {
            if (!(this_present_start && that_present_start))
                return false;
            if (this.start != that.start)
                return false;
        }
        boolean this_present_stop = true;
        boolean that_present_stop = true;
        if (this_present_stop || that_present_stop) {
            if (!(this_present_stop && that_present_stop))
                return false;
            if (this.stop != that.stop)
                return false;
        }
        boolean this_present_description = true && this.isSetDescription();
        boolean that_present_description = true && that.isSetDescription();
        if (this_present_description || that_present_description) {
            if (!(this_present_description && that_present_description))
                return false;
            if (!this.description.equals(that.description))
                return false;
        }
        boolean this_present_data = true && this.isSetData();
        boolean that_present_data = true && that.isSetData();
        if (this_present_data || that_present_data) {
            if (!(this_present_data && that_present_data))
                return false;
            if (!this.data.equals(that.data))
                return false;
        }
        boolean this_present_annotations = true && this.isSetAnnotations();
        boolean that_present_annotations = true && that.isSetAnnotations();
        if (this_present_annotations || that_present_annotations) {
            if (!(this_present_annotations && that_present_annotations))
                return false;
            if (!this.annotations.equals(that.annotations))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 8191 + ((isSetSender()) ? 131071 : 524287);
        if (isSetSender())
            hashCode = hashCode * 8191 + sender.hashCode();
        hashCode = hashCode * 8191 + ((isSetSvc()) ? 131071 : 524287);
        if (isSetSvc())
            hashCode = hashCode * 8191 + svc.hashCode();
        hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(traceId);
        hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(spanId);
        hashCode = hashCode * 8191 + ((isSetParentIds()) ? 131071 : 524287);
        if (isSetParentIds())
            hashCode = hashCode * 8191 + parentIds.hashCode();
        hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(start);
        hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(stop);
        hashCode = hashCode * 8191 + ((isSetDescription()) ? 131071 : 524287);
        if (isSetDescription())
            hashCode = hashCode * 8191 + description.hashCode();
        hashCode = hashCode * 8191 + ((isSetData()) ? 131071 : 524287);
        if (isSetData())
            hashCode = hashCode * 8191 + data.hashCode();
        hashCode = hashCode * 8191 + ((isSetAnnotations()) ? 131071 : 524287);
        if (isSetAnnotations())
            hashCode = hashCode * 8191 + annotations.hashCode();
        return hashCode;
    }

    @Override
    public int compareTo(RemoteSpan other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetSender()).compareTo(other.isSetSender());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetSender()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sender, other.sender);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetSvc()).compareTo(other.isSetSvc());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetSvc()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.svc, other.svc);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetTraceId()).compareTo(other.isSetTraceId());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTraceId()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.traceId, other.traceId);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetSpanId()).compareTo(other.isSetSpanId());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetSpanId()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.spanId, other.spanId);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetParentIds()).compareTo(other.isSetParentIds());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetParentIds()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.parentIds, other.parentIds);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
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
        lastComparison = java.lang.Boolean.valueOf(isSetDescription()).compareTo(other.isSetDescription());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetDescription()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.description, other.description);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetData()).compareTo(other.isSetData());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetData()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.data, other.data);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetAnnotations()).compareTo(other.isSetAnnotations());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetAnnotations()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.annotations, other.annotations);
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("RemoteSpan(");
        boolean first = true;
        sb.append("sender:");
        if (this.sender == null) {
            sb.append("null");
        } else {
            sb.append(this.sender);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("svc:");
        if (this.svc == null) {
            sb.append("null");
        } else {
            sb.append(this.svc);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("traceId:");
        sb.append(this.traceId);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("spanId:");
        sb.append(this.spanId);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("parentIds:");
        if (this.parentIds == null) {
            sb.append("null");
        } else {
            sb.append(this.parentIds);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("start:");
        sb.append(this.start);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("stop:");
        sb.append(this.stop);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("description:");
        if (this.description == null) {
            sb.append("null");
        } else {
            sb.append(this.description);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("data:");
        if (this.data == null) {
            sb.append("null");
        } else {
            sb.append(this.data);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("annotations:");
        if (this.annotations == null) {
            sb.append("null");
        } else {
            sb.append(this.annotations);
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

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
        try {
            __isset_bitfield = 0;
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class RemoteSpanStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public RemoteSpanStandardScheme getScheme() {
            return new RemoteSpanStandardScheme();
        }
    }

    private static class RemoteSpanStandardScheme extends org.apache.thrift.scheme.StandardScheme<RemoteSpan> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, RemoteSpan struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch(schemeField.id) {
                    case 1:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.sender = iprot.readString();
                            struct.setSenderIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.svc = iprot.readString();
                            struct.setSvcIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.traceId = iprot.readI64();
                            struct.setTraceIdIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.spanId = iprot.readI64();
                            struct.setSpanIdIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 11:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list0 = iprot.readListBegin();
                                struct.parentIds = new java.util.ArrayList<java.lang.Long>(_list0.size);
                                long _elem1;
                                for (int _i2 = 0; _i2 < _list0.size; ++_i2) {
                                    _elem1 = iprot.readI64();
                                    struct.parentIds.add(_elem1);
                                }
                                iprot.readListEnd();
                            }
                            struct.setParentIdsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 6:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.start = iprot.readI64();
                            struct.setStartIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 7:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.stop = iprot.readI64();
                            struct.setStopIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 8:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.description = iprot.readString();
                            struct.setDescriptionIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 9:
                        if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
                            {
                                org.apache.thrift.protocol.TMap _map3 = iprot.readMapBegin();
                                struct.data = new java.util.HashMap<java.lang.String, java.lang.String>(2 * _map3.size);
                                @org.apache.thrift.annotation.Nullable
                                java.lang.String _key4;
                                @org.apache.thrift.annotation.Nullable
                                java.lang.String _val5;
                                for (int _i6 = 0; _i6 < _map3.size; ++_i6) {
                                    _key4 = iprot.readString();
                                    _val5 = iprot.readString();
                                    struct.data.put(_key4, _val5);
                                }
                                iprot.readMapEnd();
                            }
                            struct.setDataIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 10:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list7 = iprot.readListBegin();
                                struct.annotations = new java.util.ArrayList<Annotation>(_list7.size);
                                @org.apache.thrift.annotation.Nullable
                                Annotation _elem8;
                                for (int _i9 = 0; _i9 < _list7.size; ++_i9) {
                                    _elem8 = new Annotation();
                                    _elem8.read(iprot);
                                    struct.annotations.add(_elem8);
                                }
                                iprot.readListEnd();
                            }
                            struct.setAnnotationsIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, RemoteSpan struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.sender != null) {
                oprot.writeFieldBegin(SENDER_FIELD_DESC);
                oprot.writeString(struct.sender);
                oprot.writeFieldEnd();
            }
            if (struct.svc != null) {
                oprot.writeFieldBegin(SVC_FIELD_DESC);
                oprot.writeString(struct.svc);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(TRACE_ID_FIELD_DESC);
            oprot.writeI64(struct.traceId);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(SPAN_ID_FIELD_DESC);
            oprot.writeI64(struct.spanId);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(START_FIELD_DESC);
            oprot.writeI64(struct.start);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(STOP_FIELD_DESC);
            oprot.writeI64(struct.stop);
            oprot.writeFieldEnd();
            if (struct.description != null) {
                oprot.writeFieldBegin(DESCRIPTION_FIELD_DESC);
                oprot.writeString(struct.description);
                oprot.writeFieldEnd();
            }
            if (struct.data != null) {
                oprot.writeFieldBegin(DATA_FIELD_DESC);
                {
                    oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, struct.data.size()));
                    for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter10 : struct.data.entrySet()) {
                        oprot.writeString(_iter10.getKey());
                        oprot.writeString(_iter10.getValue());
                    }
                    oprot.writeMapEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.annotations != null) {
                oprot.writeFieldBegin(ANNOTATIONS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.annotations.size()));
                    for (Annotation _iter11 : struct.annotations) {
                        _iter11.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.parentIds != null) {
                oprot.writeFieldBegin(PARENT_IDS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.I64, struct.parentIds.size()));
                    for (long _iter12 : struct.parentIds) {
                        oprot.writeI64(_iter12);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class RemoteSpanTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public RemoteSpanTupleScheme getScheme() {
            return new RemoteSpanTupleScheme();
        }
    }

    private static class RemoteSpanTupleScheme extends org.apache.thrift.scheme.TupleScheme<RemoteSpan> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, RemoteSpan struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
            if (struct.isSetSender()) {
                optionals.set(0);
            }
            if (struct.isSetSvc()) {
                optionals.set(1);
            }
            if (struct.isSetTraceId()) {
                optionals.set(2);
            }
            if (struct.isSetSpanId()) {
                optionals.set(3);
            }
            if (struct.isSetParentIds()) {
                optionals.set(4);
            }
            if (struct.isSetStart()) {
                optionals.set(5);
            }
            if (struct.isSetStop()) {
                optionals.set(6);
            }
            if (struct.isSetDescription()) {
                optionals.set(7);
            }
            if (struct.isSetData()) {
                optionals.set(8);
            }
            if (struct.isSetAnnotations()) {
                optionals.set(9);
            }
            oprot.writeBitSet(optionals, 10);
            if (struct.isSetSender()) {
                oprot.writeString(struct.sender);
            }
            if (struct.isSetSvc()) {
                oprot.writeString(struct.svc);
            }
            if (struct.isSetTraceId()) {
                oprot.writeI64(struct.traceId);
            }
            if (struct.isSetSpanId()) {
                oprot.writeI64(struct.spanId);
            }
            if (struct.isSetParentIds()) {
                {
                    oprot.writeI32(struct.parentIds.size());
                    for (long _iter13 : struct.parentIds) {
                        oprot.writeI64(_iter13);
                    }
                }
            }
            if (struct.isSetStart()) {
                oprot.writeI64(struct.start);
            }
            if (struct.isSetStop()) {
                oprot.writeI64(struct.stop);
            }
            if (struct.isSetDescription()) {
                oprot.writeString(struct.description);
            }
            if (struct.isSetData()) {
                {
                    oprot.writeI32(struct.data.size());
                    for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter14 : struct.data.entrySet()) {
                        oprot.writeString(_iter14.getKey());
                        oprot.writeString(_iter14.getValue());
                    }
                }
            }
            if (struct.isSetAnnotations()) {
                {
                    oprot.writeI32(struct.annotations.size());
                    for (Annotation _iter15 : struct.annotations) {
                        _iter15.write(oprot);
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, RemoteSpan struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(10);
            if (incoming.get(0)) {
                struct.sender = iprot.readString();
                struct.setSenderIsSet(true);
            }
            if (incoming.get(1)) {
                struct.svc = iprot.readString();
                struct.setSvcIsSet(true);
            }
            if (incoming.get(2)) {
                struct.traceId = iprot.readI64();
                struct.setTraceIdIsSet(true);
            }
            if (incoming.get(3)) {
                struct.spanId = iprot.readI64();
                struct.setSpanIdIsSet(true);
            }
            if (incoming.get(4)) {
                {
                    org.apache.thrift.protocol.TList _list16 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.I64, iprot.readI32());
                    struct.parentIds = new java.util.ArrayList<java.lang.Long>(_list16.size);
                    long _elem17;
                    for (int _i18 = 0; _i18 < _list16.size; ++_i18) {
                        _elem17 = iprot.readI64();
                        struct.parentIds.add(_elem17);
                    }
                }
                struct.setParentIdsIsSet(true);
            }
            if (incoming.get(5)) {
                struct.start = iprot.readI64();
                struct.setStartIsSet(true);
            }
            if (incoming.get(6)) {
                struct.stop = iprot.readI64();
                struct.setStopIsSet(true);
            }
            if (incoming.get(7)) {
                struct.description = iprot.readString();
                struct.setDescriptionIsSet(true);
            }
            if (incoming.get(8)) {
                {
                    org.apache.thrift.protocol.TMap _map19 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.data = new java.util.HashMap<java.lang.String, java.lang.String>(2 * _map19.size);
                    @org.apache.thrift.annotation.Nullable
                    java.lang.String _key20;
                    @org.apache.thrift.annotation.Nullable
                    java.lang.String _val21;
                    for (int _i22 = 0; _i22 < _map19.size; ++_i22) {
                        _key20 = iprot.readString();
                        _val21 = iprot.readString();
                        struct.data.put(_key20, _val21);
                    }
                }
                struct.setDataIsSet(true);
            }
            if (incoming.get(9)) {
                {
                    org.apache.thrift.protocol.TList _list23 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.annotations = new java.util.ArrayList<Annotation>(_list23.size);
                    @org.apache.thrift.annotation.Nullable
                    Annotation _elem24;
                    for (int _i25 = 0; _i25 < _list23.size; ++_i25) {
                        _elem24 = new Annotation();
                        _elem24.read(iprot);
                        struct.annotations.add(_elem24);
                    }
                }
                struct.setAnnotationsIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}