package org.apache.accumulo.core.tabletserver.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class ActiveCompaction implements org.apache.thrift.TBase<ActiveCompaction, ActiveCompaction._Fields>, java.io.Serializable, Cloneable, Comparable<ActiveCompaction> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ActiveCompaction");

    private static final org.apache.thrift.protocol.TField EXTENT_FIELD_DESC = new org.apache.thrift.protocol.TField("extent", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

    private static final org.apache.thrift.protocol.TField AGE_FIELD_DESC = new org.apache.thrift.protocol.TField("age", org.apache.thrift.protocol.TType.I64, (short) 2);

    private static final org.apache.thrift.protocol.TField INPUT_FILES_FIELD_DESC = new org.apache.thrift.protocol.TField("inputFiles", org.apache.thrift.protocol.TType.LIST, (short) 3);

    private static final org.apache.thrift.protocol.TField OUTPUT_FILE_FIELD_DESC = new org.apache.thrift.protocol.TField("outputFile", org.apache.thrift.protocol.TType.STRING, (short) 4);

    private static final org.apache.thrift.protocol.TField TYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("type", org.apache.thrift.protocol.TType.I32, (short) 5);

    private static final org.apache.thrift.protocol.TField REASON_FIELD_DESC = new org.apache.thrift.protocol.TField("reason", org.apache.thrift.protocol.TType.I32, (short) 6);

    private static final org.apache.thrift.protocol.TField LOCALITY_GROUP_FIELD_DESC = new org.apache.thrift.protocol.TField("localityGroup", org.apache.thrift.protocol.TType.STRING, (short) 7);

    private static final org.apache.thrift.protocol.TField ENTRIES_READ_FIELD_DESC = new org.apache.thrift.protocol.TField("entriesRead", org.apache.thrift.protocol.TType.I64, (short) 8);

    private static final org.apache.thrift.protocol.TField ENTRIES_WRITTEN_FIELD_DESC = new org.apache.thrift.protocol.TField("entriesWritten", org.apache.thrift.protocol.TType.I64, (short) 9);

    private static final org.apache.thrift.protocol.TField SSI_LIST_FIELD_DESC = new org.apache.thrift.protocol.TField("ssiList", org.apache.thrift.protocol.TType.LIST, (short) 10);

    private static final org.apache.thrift.protocol.TField SSIO_FIELD_DESC = new org.apache.thrift.protocol.TField("ssio", org.apache.thrift.protocol.TType.MAP, (short) 11);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new ActiveCompactionStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new ActiveCompactionTupleSchemeFactory();

    static {
        schemes.put(StandardScheme.class, new ActiveCompactionStandardSchemeFactory());
        schemes.put(TupleScheme.class, new ActiveCompactionTupleSchemeFactory());
    }

    @org.apache.thrift.annotation.Nullable
    public org.apache.accumulo.core.dataImpl.thrift.TKeyExtent extent;

    public long age;

    @org.apache.thrift.annotation.Nullable
    public java.util.List<java.lang.String> inputFiles;

    @org.apache.thrift.annotation.Nullable
    public java.lang.String outputFile;

    @org.apache.thrift.annotation.Nullable
public CompactionType type;

    @org.apache.thrift.annotation.Nullable
public CompactionReason reason;

    @org.apache.thrift.annotation.Nullable
    public java.lang.String localityGroup;

    public long entriesRead;

    public long entriesWritten;

    @org.apache.thrift.annotation.Nullable
    public java.util.List<org.apache.accumulo.core.dataImpl.thrift.IterInfo> ssiList;

    @org.apache.thrift.annotation.Nullable
    public java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.String>> ssio;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        EXTENT((short) 1, "extent"),
        AGE((short) 2, "age"),
        INPUT_FILES((short) 3, "inputFiles"),
        OUTPUT_FILE((short) 4, "outputFile"),
        TYPE((short) 5, "type"),
        REASON((short) 6, "reason"),
        LOCALITY_GROUP((short) 7, "localityGroup"),
        ENTRIES_READ((short) 8, "entriesRead"),
        ENTRIES_WRITTEN((short) 9, "entriesWritten"),
        SSI_LIST((short) 10, "ssiList"),
        SSIO((short) 11, "ssio");

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
                    return EXTENT;
                case 2:
                    return AGE;
                case 3:
                    return INPUT_FILES;
                case 4:
                    return OUTPUT_FILE;
                case 5:
                    return TYPE;
                case 6:
                    return REASON;
                case 7:
                    return LOCALITY_GROUP;
                case 8:
                    return ENTRIES_READ;
                case 9:
                    return ENTRIES_WRITTEN;
                case 10:
                    return SSI_LIST;
                case 11:
                    return SSIO;
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

    private static final int __AGE_ISSET_ID = 0;

    private static final int __ENTRIESREAD_ISSET_ID = 1;

    private static final int __ENTRIESWRITTEN_ISSET_ID = 2;

    private byte __isset_bitfield = 0;

    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.EXTENT, new org.apache.thrift.meta_data.FieldMetaData("extent", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.dataImpl.thrift.TKeyExtent.class)));
        tmpMap.put(_Fields.AGE, new org.apache.thrift.meta_data.FieldMetaData("age", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.INPUT_FILES, new org.apache.thrift.meta_data.FieldMetaData("inputFiles", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
        tmpMap.put(_Fields.OUTPUT_FILE, new org.apache.thrift.meta_data.FieldMetaData("outputFile", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.TYPE, new org.apache.thrift.meta_data.FieldMetaData("type", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, CompactionType.class)));
        tmpMap.put(_Fields.REASON, new org.apache.thrift.meta_data.FieldMetaData("reason", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, CompactionReason.class)));
        tmpMap.put(_Fields.LOCALITY_GROUP, new org.apache.thrift.meta_data.FieldMetaData("localityGroup", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.ENTRIES_READ, new org.apache.thrift.meta_data.FieldMetaData("entriesRead", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.ENTRIES_WRITTEN, new org.apache.thrift.meta_data.FieldMetaData("entriesWritten", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.SSI_LIST, new org.apache.thrift.meta_data.FieldMetaData("ssiList", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.dataImpl.thrift.IterInfo.class))));
        tmpMap.put(_Fields.SSIO, new org.apache.thrift.meta_data.FieldMetaData("ssio", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)))));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ActiveCompaction.class, metaDataMap);
    }

    public ActiveCompaction() {
    }

    public ActiveCompaction(org.apache.accumulo.core.dataImpl.thrift.TKeyExtent extent, long age, java.util.List<java.lang.String> inputFiles, java.lang.String outputFile, CompactionType type, CompactionReason reason, java.lang.String localityGroup, long entriesRead, long entriesWritten, java.util.List<org.apache.accumulo.core.dataImpl.thrift.IterInfo> ssiList, java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.String>> ssio) {
        this();
        this.extent = extent;
        this.age = age;
        setAgeIsSet(true);
        this.inputFiles = inputFiles;
        this.outputFile = outputFile;
        this.type = type;
        this.reason = reason;
        this.localityGroup = localityGroup;
        this.entriesRead = entriesRead;
        setEntriesReadIsSet(true);
        this.entriesWritten = entriesWritten;
        setEntriesWrittenIsSet(true);
        this.ssiList = ssiList;
        this.ssio = ssio;
    }

    public ActiveCompaction(ActiveCompaction other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetExtent()) {
            this.extent = new org.apache.accumulo.core.dataImpl.thrift.TKeyExtent(other.extent);
        }
        this.age = other.age;
        if (other.isSetInputFiles()) {
            java.util.List<java.lang.String> __this__inputFiles = new java.util.ArrayList<java.lang.String>(other.inputFiles);
            this.inputFiles = __this__inputFiles;
        }
        if (other.isSetOutputFile()) {
            this.outputFile = other.outputFile;
        }
        if (other.isSetType()) {
            this.type = other.type;
        }
        if (other.isSetReason()) {
            this.reason = other.reason;
        }
        if (other.isSetLocalityGroup()) {
            this.localityGroup = other.localityGroup;
        }
        this.entriesRead = other.entriesRead;
        this.entriesWritten = other.entriesWritten;
        if (other.isSetSsiList()) {
            java.util.List<org.apache.accumulo.core.dataImpl.thrift.IterInfo> __this__ssiList = new java.util.ArrayList<org.apache.accumulo.core.dataImpl.thrift.IterInfo>(other.ssiList.size());
            for (org.apache.accumulo.core.dataImpl.thrift.IterInfo other_element : other.ssiList) {
                __this__ssiList.add(new org.apache.accumulo.core.dataImpl.thrift.IterInfo(other_element));
            }
            this.ssiList = __this__ssiList;
        }
        if (other.isSetSsio()) {
            java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.String>> __this__ssio = new java.util.HashMap<java.lang.String, java.util.Map<java.lang.String, java.lang.String>>(other.ssio.size());
            for (java.util.Map.Entry<java.lang.String, java.util.Map<java.lang.String, java.lang.String>> other_element : other.ssio.entrySet()) {
                java.lang.String other_element_key = other_element.getKey();
                java.util.Map<java.lang.String, java.lang.String> other_element_value = other_element.getValue();
                java.lang.String __this__ssio_copy_key = other_element_key;
                java.util.Map<java.lang.String, java.lang.String> __this__ssio_copy_value = new java.util.HashMap<java.lang.String, java.lang.String>(other_element_value);
                __this__ssio.put(__this__ssio_copy_key, __this__ssio_copy_value);
            }
            this.ssio = __this__ssio;
        }
    }

    public ActiveCompaction deepCopy() {
        return new ActiveCompaction(this);
    }

    @Override
    public void clear() {
        this.extent = null;
        setAgeIsSet(false);
        this.age = 0;
        this.inputFiles = null;
        this.outputFile = null;
        this.type = null;
        this.reason = null;
        this.localityGroup = null;
        setEntriesReadIsSet(false);
        this.entriesRead = 0;
        setEntriesWrittenIsSet(false);
        this.entriesWritten = 0;
        this.ssiList = null;
        this.ssio = null;
    }

    @org.apache.thrift.annotation.Nullable
    public org.apache.accumulo.core.dataImpl.thrift.TKeyExtent getExtent() {
        return this.extent;
    }

    public ActiveCompaction setExtent(@org.apache.thrift.annotation.Nullable org.apache.accumulo.core.dataImpl.thrift.TKeyExtent extent) {
        this.extent = extent;
        return this;
    }

    public void unsetExtent() {
        this.extent = null;
    }

    public boolean isSetExtent() {
        return this.extent != null;
    }

    public void setExtentIsSet(boolean value) {
        if (!value) {
            this.extent = null;
        }
    }

    public long getAge() {
        return this.age;
    }

    public ActiveCompaction setAge(long age) {
        this.age = age;
        setAgeIsSet(true);
        return this;
    }

    public void unsetAge() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __AGE_ISSET_ID);
    }

    public boolean isSetAge() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __AGE_ISSET_ID);
    }

    public void setAgeIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __AGE_ISSET_ID, value);
    }

    public int getInputFilesSize() {
        return (this.inputFiles == null) ? 0 : this.inputFiles.size();
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.Iterator<java.lang.String> getInputFilesIterator() {
        return (this.inputFiles == null) ? null : this.inputFiles.iterator();
    }

    public void addToInputFiles(java.lang.String elem) {
        if (this.inputFiles == null) {
            this.inputFiles = new java.util.ArrayList<java.lang.String>();
        }
        this.inputFiles.add(elem);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.List<java.lang.String> getInputFiles() {
        return this.inputFiles;
    }

    public ActiveCompaction setInputFiles(@org.apache.thrift.annotation.Nullable java.util.List<java.lang.String> inputFiles) {
        this.inputFiles = inputFiles;
        return this;
    }

    public void unsetInputFiles() {
        this.inputFiles = null;
    }

    public boolean isSetInputFiles() {
        return this.inputFiles != null;
    }

    public void setInputFilesIsSet(boolean value) {
        if (!value) {
            this.inputFiles = null;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.String getOutputFile() {
        return this.outputFile;
    }

    public ActiveCompaction setOutputFile(@org.apache.thrift.annotation.Nullable java.lang.String outputFile) {
        this.outputFile = outputFile;
        return this;
    }

    public void unsetOutputFile() {
        this.outputFile = null;
    }

    public boolean isSetOutputFile() {
        return this.outputFile != null;
    }

    public void setOutputFileIsSet(boolean value) {
        if (!value) {
            this.outputFile = null;
        }
    }

    @org.apache.thrift.annotation.Nullable
public CompactionType getType() {
        return this.type;
    }

    public ActiveCompaction setType(@org.apache.thrift.annotation.Nullable CompactionType type) {
        this.type = type;
        return this;
    }

    public void unsetType() {
        this.type = null;
    }

    public boolean isSetType() {
        return this.type != null;
    }

    public void setTypeIsSet(boolean value) {
        if (!value) {
            this.type = null;
        }
    }

    @org.apache.thrift.annotation.Nullable
public CompactionReason getReason() {
        return this.reason;
    }

    public ActiveCompaction setReason(@org.apache.thrift.annotation.Nullable CompactionReason reason) {
        this.reason = reason;
        return this;
    }

    public void unsetReason() {
        this.reason = null;
    }

    public boolean isSetReason() {
        return this.reason != null;
    }

    public void setReasonIsSet(boolean value) {
        if (!value) {
            this.reason = null;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.String getLocalityGroup() {
        return this.localityGroup;
    }

    public ActiveCompaction setLocalityGroup(@org.apache.thrift.annotation.Nullable java.lang.String localityGroup) {
        this.localityGroup = localityGroup;
        return this;
    }

    public void unsetLocalityGroup() {
        this.localityGroup = null;
    }

    public boolean isSetLocalityGroup() {
        return this.localityGroup != null;
    }

    public void setLocalityGroupIsSet(boolean value) {
        if (!value) {
            this.localityGroup = null;
        }
    }

    public long getEntriesRead() {
        return this.entriesRead;
    }

    public ActiveCompaction setEntriesRead(long entriesRead) {
        this.entriesRead = entriesRead;
        setEntriesReadIsSet(true);
        return this;
    }

    public void unsetEntriesRead() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __ENTRIESREAD_ISSET_ID);
    }

    public boolean isSetEntriesRead() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __ENTRIESREAD_ISSET_ID);
    }

    public void setEntriesReadIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __ENTRIESREAD_ISSET_ID, value);
    }

    public long getEntriesWritten() {
        return this.entriesWritten;
    }

    public ActiveCompaction setEntriesWritten(long entriesWritten) {
        this.entriesWritten = entriesWritten;
        setEntriesWrittenIsSet(true);
        return this;
    }

    public void unsetEntriesWritten() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __ENTRIESWRITTEN_ISSET_ID);
    }

    public boolean isSetEntriesWritten() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __ENTRIESWRITTEN_ISSET_ID);
    }

    public void setEntriesWrittenIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __ENTRIESWRITTEN_ISSET_ID, value);
    }

    public int getSsiListSize() {
        return (this.ssiList == null) ? 0 : this.ssiList.size();
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.Iterator<org.apache.accumulo.core.dataImpl.thrift.IterInfo> getSsiListIterator() {
        return (this.ssiList == null) ? null : this.ssiList.iterator();
    }

    public void addToSsiList(org.apache.accumulo.core.dataImpl.thrift.IterInfo elem) {
        if (this.ssiList == null) {
            this.ssiList = new java.util.ArrayList<org.apache.accumulo.core.dataImpl.thrift.IterInfo>();
        }
        this.ssiList.add(elem);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.List<org.apache.accumulo.core.dataImpl.thrift.IterInfo> getSsiList() {
        return this.ssiList;
    }

    public ActiveCompaction setSsiList(@org.apache.thrift.annotation.Nullable java.util.List<org.apache.accumulo.core.dataImpl.thrift.IterInfo> ssiList) {
        this.ssiList = ssiList;
        return this;
    }

    public void unsetSsiList() {
        this.ssiList = null;
    }

    public boolean isSetSsiList() {
        return this.ssiList != null;
    }

    public void setSsiListIsSet(boolean value) {
        if (!value) {
            this.ssiList = null;
        }
    }

    public int getSsioSize() {
        return (this.ssio == null) ? 0 : this.ssio.size();
    }

    public void putToSsio(java.lang.String key, java.util.Map<java.lang.String, java.lang.String> val) {
        if (this.ssio == null) {
            this.ssio = new java.util.HashMap<java.lang.String, java.util.Map<java.lang.String, java.lang.String>>();
        }
        this.ssio.put(key, val);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.String>> getSsio() {
        return this.ssio;
    }

    public ActiveCompaction setSsio(@org.apache.thrift.annotation.Nullable java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.String>> ssio) {
        this.ssio = ssio;
        return this;
    }

    public void unsetSsio() {
        this.ssio = null;
    }

    public boolean isSetSsio() {
        return this.ssio != null;
    }

    public void setSsioIsSet(boolean value) {
        if (!value) {
            this.ssio = null;
        }
    }

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case EXTENT:
                if (value == null) {
                    unsetExtent();
                } else {
                    setExtent((org.apache.accumulo.core.dataImpl.thrift.TKeyExtent) value);
                }
                break;
            case AGE:
                if (value == null) {
                    unsetAge();
                } else {
                    setAge((java.lang.Long) value);
                }
                break;
            case INPUT_FILES:
                if (value == null) {
                    unsetInputFiles();
                } else {
                    setInputFiles((java.util.List<java.lang.String>) value);
                }
                break;
            case OUTPUT_FILE:
                if (value == null) {
                    unsetOutputFile();
                } else {
                    setOutputFile((java.lang.String) value);
                }
                break;
            case TYPE:
                if (value == null) {
                    unsetType();
                } else {
                    setType((CompactionType) value);
                }
                break;
            case REASON:
                if (value == null) {
                    unsetReason();
                } else {
                    setReason((CompactionReason) value);
                }
                break;
            case LOCALITY_GROUP:
                if (value == null) {
                    unsetLocalityGroup();
                } else {
                    setLocalityGroup((java.lang.String) value);
                }
                break;
            case ENTRIES_READ:
                if (value == null) {
                    unsetEntriesRead();
                } else {
                    setEntriesRead((java.lang.Long) value);
                }
                break;
            case ENTRIES_WRITTEN:
                if (value == null) {
                    unsetEntriesWritten();
                } else {
                    setEntriesWritten((java.lang.Long) value);
                }
                break;
            case SSI_LIST:
                if (value == null) {
                    unsetSsiList();
                } else {
                    setSsiList((java.util.List<org.apache.accumulo.core.dataImpl.thrift.IterInfo>) value);
                }
                break;
            case SSIO:
                if (value == null) {
                    unsetSsio();
                } else {
                    setSsio((java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.String>>) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch(field) {
            case EXTENT:
                return getExtent();
            case AGE:
                return getAge();
            case INPUT_FILES:
                return getInputFiles();
            case OUTPUT_FILE:
                return getOutputFile();
            case TYPE:
                return getType();
            case REASON:
                return getReason();
            case LOCALITY_GROUP:
                return getLocalityGroup();
            case ENTRIES_READ:
                return getEntriesRead();
            case ENTRIES_WRITTEN:
                return getEntriesWritten();
            case SSI_LIST:
                return getSsiList();
            case SSIO:
                return getSsio();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }
        switch(field) {
            case EXTENT:
                return isSetExtent();
            case AGE:
                return isSetAge();
            case INPUT_FILES:
                return isSetInputFiles();
            case OUTPUT_FILE:
                return isSetOutputFile();
            case TYPE:
                return isSetType();
            case REASON:
                return isSetReason();
            case LOCALITY_GROUP:
                return isSetLocalityGroup();
            case ENTRIES_READ:
                return isSetEntriesRead();
            case ENTRIES_WRITTEN:
                return isSetEntriesWritten();
            case SSI_LIST:
                return isSetSsiList();
            case SSIO:
                return isSetSsio();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof ActiveCompaction)
            return this.equals((ActiveCompaction) that);
        return false;
    }

    public boolean equals(ActiveCompaction that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
        boolean this_present_extent = true && this.isSetExtent();
        boolean that_present_extent = true && that.isSetExtent();
        if (this_present_extent || that_present_extent) {
            if (!(this_present_extent && that_present_extent))
                return false;
            if (!this.extent.equals(that.extent))
                return false;
        }
        boolean this_present_age = true;
        boolean that_present_age = true;
        if (this_present_age || that_present_age) {
            if (!(this_present_age && that_present_age))
                return false;
            if (this.age != that.age)
                return false;
        }
        boolean this_present_inputFiles = true && this.isSetInputFiles();
        boolean that_present_inputFiles = true && that.isSetInputFiles();
        if (this_present_inputFiles || that_present_inputFiles) {
            if (!(this_present_inputFiles && that_present_inputFiles))
                return false;
            if (!this.inputFiles.equals(that.inputFiles))
                return false;
        }
        boolean this_present_outputFile = true && this.isSetOutputFile();
        boolean that_present_outputFile = true && that.isSetOutputFile();
        if (this_present_outputFile || that_present_outputFile) {
            if (!(this_present_outputFile && that_present_outputFile))
                return false;
            if (!this.outputFile.equals(that.outputFile))
                return false;
        }
        boolean this_present_type = true && this.isSetType();
        boolean that_present_type = true && that.isSetType();
        if (this_present_type || that_present_type) {
            if (!(this_present_type && that_present_type))
                return false;
            if (!this.type.equals(that.type))
                return false;
        }
        boolean this_present_reason = true && this.isSetReason();
        boolean that_present_reason = true && that.isSetReason();
        if (this_present_reason || that_present_reason) {
            if (!(this_present_reason && that_present_reason))
                return false;
            if (!this.reason.equals(that.reason))
                return false;
        }
        boolean this_present_localityGroup = true && this.isSetLocalityGroup();
        boolean that_present_localityGroup = true && that.isSetLocalityGroup();
        if (this_present_localityGroup || that_present_localityGroup) {
            if (!(this_present_localityGroup && that_present_localityGroup))
                return false;
            if (!this.localityGroup.equals(that.localityGroup))
                return false;
        }
        boolean this_present_entriesRead = true;
        boolean that_present_entriesRead = true;
        if (this_present_entriesRead || that_present_entriesRead) {
            if (!(this_present_entriesRead && that_present_entriesRead))
                return false;
            if (this.entriesRead != that.entriesRead)
                return false;
        }
        boolean this_present_entriesWritten = true;
        boolean that_present_entriesWritten = true;
        if (this_present_entriesWritten || that_present_entriesWritten) {
            if (!(this_present_entriesWritten && that_present_entriesWritten))
                return false;
            if (this.entriesWritten != that.entriesWritten)
                return false;
        }
        boolean this_present_ssiList = true && this.isSetSsiList();
        boolean that_present_ssiList = true && that.isSetSsiList();
        if (this_present_ssiList || that_present_ssiList) {
            if (!(this_present_ssiList && that_present_ssiList))
                return false;
            if (!this.ssiList.equals(that.ssiList))
                return false;
        }
        boolean this_present_ssio = true && this.isSetSsio();
        boolean that_present_ssio = true && that.isSetSsio();
        if (this_present_ssio || that_present_ssio) {
            if (!(this_present_ssio && that_present_ssio))
                return false;
            if (!this.ssio.equals(that.ssio))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 8191 + ((isSetExtent()) ? 131071 : 524287);
        if (isSetExtent())
            hashCode = hashCode * 8191 + extent.hashCode();
        hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(age);
        hashCode = hashCode * 8191 + ((isSetInputFiles()) ? 131071 : 524287);
        if (isSetInputFiles())
            hashCode = hashCode * 8191 + inputFiles.hashCode();
        hashCode = hashCode * 8191 + ((isSetOutputFile()) ? 131071 : 524287);
        if (isSetOutputFile())
            hashCode = hashCode * 8191 + outputFile.hashCode();
        hashCode = hashCode * 8191 + ((isSetType()) ? 131071 : 524287);
        if (isSetType())
            hashCode = hashCode * 8191 + type.getValue();
        hashCode = hashCode * 8191 + ((isSetReason()) ? 131071 : 524287);
        if (isSetReason())
            hashCode = hashCode * 8191 + reason.getValue();
        hashCode = hashCode * 8191 + ((isSetLocalityGroup()) ? 131071 : 524287);
        if (isSetLocalityGroup())
            hashCode = hashCode * 8191 + localityGroup.hashCode();
        hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(entriesRead);
        hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(entriesWritten);
        hashCode = hashCode * 8191 + ((isSetSsiList()) ? 131071 : 524287);
        if (isSetSsiList())
            hashCode = hashCode * 8191 + ssiList.hashCode();
        hashCode = hashCode * 8191 + ((isSetSsio()) ? 131071 : 524287);
        if (isSetSsio())
            hashCode = hashCode * 8191 + ssio.hashCode();
        return hashCode;
    }

    @Override
    public int compareTo(ActiveCompaction other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetExtent()).compareTo(other.isSetExtent());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetExtent()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.extent, other.extent);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetAge()).compareTo(other.isSetAge());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetAge()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.age, other.age);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetInputFiles()).compareTo(other.isSetInputFiles());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetInputFiles()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.inputFiles, other.inputFiles);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetOutputFile()).compareTo(other.isSetOutputFile());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetOutputFile()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.outputFile, other.outputFile);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetType()).compareTo(other.isSetType());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetType()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.type, other.type);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetReason()).compareTo(other.isSetReason());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetReason()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.reason, other.reason);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetLocalityGroup()).compareTo(other.isSetLocalityGroup());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetLocalityGroup()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.localityGroup, other.localityGroup);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetEntriesRead()).compareTo(other.isSetEntriesRead());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetEntriesRead()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.entriesRead, other.entriesRead);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetEntriesWritten()).compareTo(other.isSetEntriesWritten());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetEntriesWritten()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.entriesWritten, other.entriesWritten);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetSsiList()).compareTo(other.isSetSsiList());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetSsiList()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ssiList, other.ssiList);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetSsio()).compareTo(other.isSetSsio());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetSsio()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ssio, other.ssio);
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("ActiveCompaction(");
        boolean first = true;
        sb.append("extent:");
        if (this.extent == null) {
            sb.append("null");
        } else {
            sb.append(this.extent);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("age:");
        sb.append(this.age);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("inputFiles:");
        if (this.inputFiles == null) {
            sb.append("null");
        } else {
            sb.append(this.inputFiles);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("outputFile:");
        if (this.outputFile == null) {
            sb.append("null");
        } else {
            sb.append(this.outputFile);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("type:");
        if (this.type == null) {
            sb.append("null");
        } else {
            sb.append(this.type);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("reason:");
        if (this.reason == null) {
            sb.append("null");
        } else {
            sb.append(this.reason);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("localityGroup:");
        if (this.localityGroup == null) {
            sb.append("null");
        } else {
            sb.append(this.localityGroup);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("entriesRead:");
        sb.append(this.entriesRead);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("entriesWritten:");
        sb.append(this.entriesWritten);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("ssiList:");
        if (this.ssiList == null) {
            sb.append("null");
        } else {
            sb.append(this.ssiList);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("ssio:");
        if (this.ssio == null) {
            sb.append("null");
        } else {
            sb.append(this.ssio);
        }
        first = false;
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
        if (extent != null) {
            extent.validate();
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

    private static class ActiveCompactionStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public ActiveCompactionStandardScheme getScheme() {
            return new ActiveCompactionStandardScheme();
        }
    }

    private static class ActiveCompactionStandardScheme extends org.apache.thrift.scheme.StandardScheme<ActiveCompaction> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, ActiveCompaction struct) throws org.apache.thrift.TException {
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
                            struct.extent = new org.apache.accumulo.core.dataImpl.thrift.TKeyExtent();
                            struct.extent.read(iprot);
                            struct.setExtentIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.age = iprot.readI64();
                            struct.setAgeIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list52 = iprot.readListBegin();
                                struct.inputFiles = new java.util.ArrayList<java.lang.String>(_list52.size);
                                @org.apache.thrift.annotation.Nullable
                                java.lang.String _elem53;
                                for (int _i54 = 0; _i54 < _list52.size; ++_i54) {
                                    _elem53 = iprot.readString();
                                    struct.inputFiles.add(_elem53);
                                }
                                iprot.readListEnd();
                            }
                            struct.setInputFilesIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.outputFile = iprot.readString();
                            struct.setOutputFileIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 5:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.type = org.apache.accumulo.core.tabletserver.thrift.CompactionType.findByValue(iprot.readI32());
                            struct.setTypeIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 6:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.reason = org.apache.accumulo.core.tabletserver.thrift.CompactionReason.findByValue(iprot.readI32());
                            struct.setReasonIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 7:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.localityGroup = iprot.readString();
                            struct.setLocalityGroupIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 8:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.entriesRead = iprot.readI64();
                            struct.setEntriesReadIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 9:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.entriesWritten = iprot.readI64();
                            struct.setEntriesWrittenIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 10:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list55 = iprot.readListBegin();
                                struct.ssiList = new java.util.ArrayList<org.apache.accumulo.core.dataImpl.thrift.IterInfo>(_list55.size);
                                @org.apache.thrift.annotation.Nullable
                                org.apache.accumulo.core.dataImpl.thrift.IterInfo _elem56;
                                for (int _i57 = 0; _i57 < _list55.size; ++_i57) {
                                    _elem56 = new org.apache.accumulo.core.dataImpl.thrift.IterInfo();
                                    _elem56.read(iprot);
                                    struct.ssiList.add(_elem56);
                                }
                                iprot.readListEnd();
                            }
                            struct.setSsiListIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 11:
                        if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
                            {
                                org.apache.thrift.protocol.TMap _map58 = iprot.readMapBegin();
                                struct.ssio = new java.util.HashMap<java.lang.String, java.util.Map<java.lang.String, java.lang.String>>(2 * _map58.size);
                                @org.apache.thrift.annotation.Nullable
                                java.lang.String _key59;
                                @org.apache.thrift.annotation.Nullable
                                java.util.Map<java.lang.String, java.lang.String> _val60;
                                for (int _i61 = 0; _i61 < _map58.size; ++_i61) {
                                    _key59 = iprot.readString();
                                    {
                                        org.apache.thrift.protocol.TMap _map62 = iprot.readMapBegin();
                                        _val60 = new java.util.HashMap<java.lang.String, java.lang.String>(2 * _map62.size);
                                        @org.apache.thrift.annotation.Nullable
                                        java.lang.String _key63;
                                        @org.apache.thrift.annotation.Nullable
                                        java.lang.String _val64;
                                        for (int _i65 = 0; _i65 < _map62.size; ++_i65) {
                                            _key63 = iprot.readString();
                                            _val64 = iprot.readString();
                                            _val60.put(_key63, _val64);
                                        }
                                        iprot.readMapEnd();
                                    }
                                    struct.ssio.put(_key59, _val60);
                                }
                                iprot.readMapEnd();
                            }
                            struct.setSsioIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, ActiveCompaction struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.extent != null) {
                oprot.writeFieldBegin(EXTENT_FIELD_DESC);
                struct.extent.write(oprot);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(AGE_FIELD_DESC);
            oprot.writeI64(struct.age);
            oprot.writeFieldEnd();
            if (struct.inputFiles != null) {
                oprot.writeFieldBegin(INPUT_FILES_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.inputFiles.size()));
                    for (java.lang.String _iter66 : struct.inputFiles) {
                        oprot.writeString(_iter66);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.outputFile != null) {
                oprot.writeFieldBegin(OUTPUT_FILE_FIELD_DESC);
                oprot.writeString(struct.outputFile);
                oprot.writeFieldEnd();
            }
            if (struct.type != null) {
                oprot.writeFieldBegin(TYPE_FIELD_DESC);
                oprot.writeI32(struct.type.getValue());
                oprot.writeFieldEnd();
            }
            if (struct.reason != null) {
                oprot.writeFieldBegin(REASON_FIELD_DESC);
                oprot.writeI32(struct.reason.getValue());
                oprot.writeFieldEnd();
            }
            if (struct.localityGroup != null) {
                oprot.writeFieldBegin(LOCALITY_GROUP_FIELD_DESC);
                oprot.writeString(struct.localityGroup);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(ENTRIES_READ_FIELD_DESC);
            oprot.writeI64(struct.entriesRead);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(ENTRIES_WRITTEN_FIELD_DESC);
            oprot.writeI64(struct.entriesWritten);
            oprot.writeFieldEnd();
            if (struct.ssiList != null) {
                oprot.writeFieldBegin(SSI_LIST_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.ssiList.size()));
                    for (org.apache.accumulo.core.dataImpl.thrift.IterInfo _iter67 : struct.ssiList) {
                        _iter67.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.ssio != null) {
                oprot.writeFieldBegin(SSIO_FIELD_DESC);
                {
                    oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.MAP, struct.ssio.size()));
                    for (java.util.Map.Entry<java.lang.String, java.util.Map<java.lang.String, java.lang.String>> _iter68 : struct.ssio.entrySet()) {
                        oprot.writeString(_iter68.getKey());
                        {
                            oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, _iter68.getValue().size()));
                            for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter69 : _iter68.getValue().entrySet()) {
                                oprot.writeString(_iter69.getKey());
                                oprot.writeString(_iter69.getValue());
                            }
                            oprot.writeMapEnd();
                        }
                    }
                    oprot.writeMapEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class ActiveCompactionTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public ActiveCompactionTupleScheme getScheme() {
            return new ActiveCompactionTupleScheme();
        }
    }

    private static class ActiveCompactionTupleScheme extends org.apache.thrift.scheme.TupleScheme<ActiveCompaction> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, ActiveCompaction struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
            if (struct.isSetExtent()) {
                optionals.set(0);
            }
            if (struct.isSetAge()) {
                optionals.set(1);
            }
            if (struct.isSetInputFiles()) {
                optionals.set(2);
            }
            if (struct.isSetOutputFile()) {
                optionals.set(3);
            }
            if (struct.isSetType()) {
                optionals.set(4);
            }
            if (struct.isSetReason()) {
                optionals.set(5);
            }
            if (struct.isSetLocalityGroup()) {
                optionals.set(6);
            }
            if (struct.isSetEntriesRead()) {
                optionals.set(7);
            }
            if (struct.isSetEntriesWritten()) {
                optionals.set(8);
            }
            if (struct.isSetSsiList()) {
                optionals.set(9);
            }
            if (struct.isSetSsio()) {
                optionals.set(10);
            }
            oprot.writeBitSet(optionals, 11);
            if (struct.isSetExtent()) {
                struct.extent.write(oprot);
            }
            if (struct.isSetAge()) {
                oprot.writeI64(struct.age);
            }
            if (struct.isSetInputFiles()) {
                {
                    oprot.writeI32(struct.inputFiles.size());
                    for (java.lang.String _iter70 : struct.inputFiles) {
                        oprot.writeString(_iter70);
                    }
                }
            }
            if (struct.isSetOutputFile()) {
                oprot.writeString(struct.outputFile);
            }
            if (struct.isSetType()) {
                oprot.writeI32(struct.type.getValue());
            }
            if (struct.isSetReason()) {
                oprot.writeI32(struct.reason.getValue());
            }
            if (struct.isSetLocalityGroup()) {
                oprot.writeString(struct.localityGroup);
            }
            if (struct.isSetEntriesRead()) {
                oprot.writeI64(struct.entriesRead);
            }
            if (struct.isSetEntriesWritten()) {
                oprot.writeI64(struct.entriesWritten);
            }
            if (struct.isSetSsiList()) {
                {
                    oprot.writeI32(struct.ssiList.size());
                    for (org.apache.accumulo.core.dataImpl.thrift.IterInfo _iter71 : struct.ssiList) {
                        _iter71.write(oprot);
                    }
                }
            }
            if (struct.isSetSsio()) {
                {
                    oprot.writeI32(struct.ssio.size());
                    for (java.util.Map.Entry<java.lang.String, java.util.Map<java.lang.String, java.lang.String>> _iter72 : struct.ssio.entrySet()) {
                        oprot.writeString(_iter72.getKey());
                        {
                            oprot.writeI32(_iter72.getValue().size());
                            for (java.util.Map.Entry<java.lang.String, java.lang.String> _iter73 : _iter72.getValue().entrySet()) {
                                oprot.writeString(_iter73.getKey());
                                oprot.writeString(_iter73.getValue());
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, ActiveCompaction struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(11);
            if (incoming.get(0)) {
                struct.extent = new org.apache.accumulo.core.dataImpl.thrift.TKeyExtent();
                struct.extent.read(iprot);
                struct.setExtentIsSet(true);
            }
            if (incoming.get(1)) {
                struct.age = iprot.readI64();
                struct.setAgeIsSet(true);
            }
            if (incoming.get(2)) {
                {
                    org.apache.thrift.protocol.TList _list74 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.inputFiles = new java.util.ArrayList<java.lang.String>(_list74.size);
                    @org.apache.thrift.annotation.Nullable
                    java.lang.String _elem75;
                    for (int _i76 = 0; _i76 < _list74.size; ++_i76) {
                        _elem75 = iprot.readString();
                        struct.inputFiles.add(_elem75);
                    }
                }
                struct.setInputFilesIsSet(true);
            }
            if (incoming.get(3)) {
                struct.outputFile = iprot.readString();
                struct.setOutputFileIsSet(true);
            }
            if (incoming.get(4)) {
                struct.type = org.apache.accumulo.core.tabletserver.thrift.CompactionType.findByValue(iprot.readI32());
                struct.setTypeIsSet(true);
            }
            if (incoming.get(5)) {
                struct.reason = org.apache.accumulo.core.tabletserver.thrift.CompactionReason.findByValue(iprot.readI32());
                struct.setReasonIsSet(true);
            }
            if (incoming.get(6)) {
                struct.localityGroup = iprot.readString();
                struct.setLocalityGroupIsSet(true);
            }
            if (incoming.get(7)) {
                struct.entriesRead = iprot.readI64();
                struct.setEntriesReadIsSet(true);
            }
            if (incoming.get(8)) {
                struct.entriesWritten = iprot.readI64();
                struct.setEntriesWrittenIsSet(true);
            }
            if (incoming.get(9)) {
                {
                    org.apache.thrift.protocol.TList _list77 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.ssiList = new java.util.ArrayList<org.apache.accumulo.core.dataImpl.thrift.IterInfo>(_list77.size);
                    @org.apache.thrift.annotation.Nullable
                    org.apache.accumulo.core.dataImpl.thrift.IterInfo _elem78;
                    for (int _i79 = 0; _i79 < _list77.size; ++_i79) {
                        _elem78 = new org.apache.accumulo.core.dataImpl.thrift.IterInfo();
                        _elem78.read(iprot);
                        struct.ssiList.add(_elem78);
                    }
                }
                struct.setSsiListIsSet(true);
            }
            if (incoming.get(10)) {
                {
                    org.apache.thrift.protocol.TMap _map80 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.MAP, iprot.readI32());
                    struct.ssio = new java.util.HashMap<java.lang.String, java.util.Map<java.lang.String, java.lang.String>>(2 * _map80.size);
                    @org.apache.thrift.annotation.Nullable
                    java.lang.String _key81;
                    @org.apache.thrift.annotation.Nullable
                    java.util.Map<java.lang.String, java.lang.String> _val82;
                    for (int _i83 = 0; _i83 < _map80.size; ++_i83) {
                        _key81 = iprot.readString();
                        {
                            org.apache.thrift.protocol.TMap _map84 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                            _val82 = new java.util.HashMap<java.lang.String, java.lang.String>(2 * _map84.size);
                            @org.apache.thrift.annotation.Nullable
                            java.lang.String _key85;
                            @org.apache.thrift.annotation.Nullable
                            java.lang.String _val86;
                            for (int _i87 = 0; _i87 < _map84.size; ++_i87) {
                                _key85 = iprot.readString();
                                _val86 = iprot.readString();
                                _val82.put(_key85, _val86);
                            }
                        }
                        struct.ssio.put(_key81, _val82);
                    }
                }
                struct.setSsioIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}