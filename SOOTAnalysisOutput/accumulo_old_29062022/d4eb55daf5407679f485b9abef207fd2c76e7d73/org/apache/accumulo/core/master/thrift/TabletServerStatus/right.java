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

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class TabletServerStatus implements org.apache.thrift.TBase<TabletServerStatus, TabletServerStatus._Fields>, java.io.Serializable, Cloneable, Comparable<TabletServerStatus> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TabletServerStatus");

    private static final org.apache.thrift.protocol.TField TABLE_MAP_FIELD_DESC = new org.apache.thrift.protocol.TField("tableMap", org.apache.thrift.protocol.TType.MAP, (short) 1);

    private static final org.apache.thrift.protocol.TField LAST_CONTACT_FIELD_DESC = new org.apache.thrift.protocol.TField("lastContact", org.apache.thrift.protocol.TType.I64, (short) 2);

    private static final org.apache.thrift.protocol.TField NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("name", org.apache.thrift.protocol.TType.STRING, (short) 3);

    private static final org.apache.thrift.protocol.TField OS_LOAD_FIELD_DESC = new org.apache.thrift.protocol.TField("osLoad", org.apache.thrift.protocol.TType.DOUBLE, (short) 5);

    private static final org.apache.thrift.protocol.TField HOLD_TIME_FIELD_DESC = new org.apache.thrift.protocol.TField("holdTime", org.apache.thrift.protocol.TType.I64, (short) 7);

    private static final org.apache.thrift.protocol.TField LOOKUPS_FIELD_DESC = new org.apache.thrift.protocol.TField("lookups", org.apache.thrift.protocol.TType.I64, (short) 8);

    private static final org.apache.thrift.protocol.TField INDEX_CACHE_HITS_FIELD_DESC = new org.apache.thrift.protocol.TField("indexCacheHits", org.apache.thrift.protocol.TType.I64, (short) 10);

    private static final org.apache.thrift.protocol.TField INDEX_CACHE_REQUEST_FIELD_DESC = new org.apache.thrift.protocol.TField("indexCacheRequest", org.apache.thrift.protocol.TType.I64, (short) 11);

    private static final org.apache.thrift.protocol.TField DATA_CACHE_HITS_FIELD_DESC = new org.apache.thrift.protocol.TField("dataCacheHits", org.apache.thrift.protocol.TType.I64, (short) 12);

    private static final org.apache.thrift.protocol.TField DATA_CACHE_REQUEST_FIELD_DESC = new org.apache.thrift.protocol.TField("dataCacheRequest", org.apache.thrift.protocol.TType.I64, (short) 13);

    private static final org.apache.thrift.protocol.TField LOG_SORTS_FIELD_DESC = new org.apache.thrift.protocol.TField("logSorts", org.apache.thrift.protocol.TType.LIST, (short) 14);

    private static final org.apache.thrift.protocol.TField FLUSHS_FIELD_DESC = new org.apache.thrift.protocol.TField("flushs", org.apache.thrift.protocol.TType.I64, (short) 15);

    private static final org.apache.thrift.protocol.TField SYNCS_FIELD_DESC = new org.apache.thrift.protocol.TField("syncs", org.apache.thrift.protocol.TType.I64, (short) 16);

    private static final org.apache.thrift.protocol.TField BULK_IMPORTS_FIELD_DESC = new org.apache.thrift.protocol.TField("bulkImports", org.apache.thrift.protocol.TType.LIST, (short) 17);

    private static final org.apache.thrift.protocol.TField RESPONSE_TIME_FIELD_DESC = new org.apache.thrift.protocol.TField("responseTime", org.apache.thrift.protocol.TType.I64, (short) 18);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new TabletServerStatusStandardSchemeFactory());
        schemes.put(TupleScheme.class, new TabletServerStatusTupleSchemeFactory());
    }

    public Map<String, TableInfo> tableMap;

    public long lastContact;

    public String name;

    public double osLoad;

    public long holdTime;

    public long lookups;

    public long indexCacheHits;

    public long indexCacheRequest;

    public long dataCacheHits;

    public long dataCacheRequest;

    public List<RecoveryStatus> logSorts;

    public long flushs;

    public long syncs;

    public List<BulkImportStatus> bulkImports;

    public long responseTime;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        TABLE_MAP((short) 1, "tableMap"),
        LAST_CONTACT((short) 2, "lastContact"),
        NAME((short) 3, "name"),
        OS_LOAD((short) 5, "osLoad"),
        HOLD_TIME((short) 7, "holdTime"),
        LOOKUPS((short) 8, "lookups"),
        INDEX_CACHE_HITS((short) 10, "indexCacheHits"),
        INDEX_CACHE_REQUEST((short) 11, "indexCacheRequest"),
        DATA_CACHE_HITS((short) 12, "dataCacheHits"),
        DATA_CACHE_REQUEST((short) 13, "dataCacheRequest"),
        LOG_SORTS((short) 14, "logSorts"),
        FLUSHS((short) 15, "flushs"),
        SYNCS((short) 16, "syncs"),
        BULK_IMPORTS((short) 17, "bulkImports"),
        RESPONSE_TIME((short) 18, "responseTime");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return TABLE_MAP;
                case 2:
                    return LAST_CONTACT;
                case 3:
                    return NAME;
                case 5:
                    return OS_LOAD;
                case 7:
                    return HOLD_TIME;
                case 8:
                    return LOOKUPS;
                case 10:
                    return INDEX_CACHE_HITS;
                case 11:
                    return INDEX_CACHE_REQUEST;
                case 12:
                    return DATA_CACHE_HITS;
                case 13:
                    return DATA_CACHE_REQUEST;
                case 14:
                    return LOG_SORTS;
                case 15:
                    return FLUSHS;
                case 16:
                    return SYNCS;
                case 17:
                    return BULK_IMPORTS;
                case 18:
                    return RESPONSE_TIME;
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

    private static final int __LASTCONTACT_ISSET_ID = 0;

    private static final int __OSLOAD_ISSET_ID = 1;

    private static final int __HOLDTIME_ISSET_ID = 2;

    private static final int __LOOKUPS_ISSET_ID = 3;

    private static final int __INDEXCACHEHITS_ISSET_ID = 4;

    private static final int __INDEXCACHEREQUEST_ISSET_ID = 5;

    private static final int __DATACACHEHITS_ISSET_ID = 6;

    private static final int __DATACACHEREQUEST_ISSET_ID = 7;

    private static final int __FLUSHS_ISSET_ID = 8;

    private static final int __SYNCS_ISSET_ID = 9;

    private static final int __RESPONSETIME_ISSET_ID = 10;

    private short __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.TABLE_MAP, new org.apache.thrift.meta_data.FieldMetaData("tableMap", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TableInfo.class))));
        tmpMap.put(_Fields.LAST_CONTACT, new org.apache.thrift.meta_data.FieldMetaData("lastContact", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.NAME, new org.apache.thrift.meta_data.FieldMetaData("name", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.OS_LOAD, new org.apache.thrift.meta_data.FieldMetaData("osLoad", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
        tmpMap.put(_Fields.HOLD_TIME, new org.apache.thrift.meta_data.FieldMetaData("holdTime", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.LOOKUPS, new org.apache.thrift.meta_data.FieldMetaData("lookups", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.INDEX_CACHE_HITS, new org.apache.thrift.meta_data.FieldMetaData("indexCacheHits", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.INDEX_CACHE_REQUEST, new org.apache.thrift.meta_data.FieldMetaData("indexCacheRequest", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.DATA_CACHE_HITS, new org.apache.thrift.meta_data.FieldMetaData("dataCacheHits", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.DATA_CACHE_REQUEST, new org.apache.thrift.meta_data.FieldMetaData("dataCacheRequest", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.LOG_SORTS, new org.apache.thrift.meta_data.FieldMetaData("logSorts", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, RecoveryStatus.class))));
        tmpMap.put(_Fields.FLUSHS, new org.apache.thrift.meta_data.FieldMetaData("flushs", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.SYNCS, new org.apache.thrift.meta_data.FieldMetaData("syncs", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.BULK_IMPORTS, new org.apache.thrift.meta_data.FieldMetaData("bulkImports", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, BulkImportStatus.class))));
        tmpMap.put(_Fields.RESPONSE_TIME, new org.apache.thrift.meta_data.FieldMetaData("responseTime", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TabletServerStatus.class, metaDataMap);
    }

    public TabletServerStatus() {
    }

    public TabletServerStatus(Map<String, TableInfo> tableMap, long lastContact, String name, double osLoad, long holdTime, long lookups, long indexCacheHits, long indexCacheRequest, long dataCacheHits, long dataCacheRequest, List<RecoveryStatus> logSorts, long flushs, long syncs, List<BulkImportStatus> bulkImports, long responseTime) {
        this();
        this.tableMap = tableMap;
        this.lastContact = lastContact;
        setLastContactIsSet(true);
        this.name = name;
        this.osLoad = osLoad;
        setOsLoadIsSet(true);
        this.holdTime = holdTime;
        setHoldTimeIsSet(true);
        this.lookups = lookups;
        setLookupsIsSet(true);
        this.indexCacheHits = indexCacheHits;
        setIndexCacheHitsIsSet(true);
        this.indexCacheRequest = indexCacheRequest;
        setIndexCacheRequestIsSet(true);
        this.dataCacheHits = dataCacheHits;
        setDataCacheHitsIsSet(true);
        this.dataCacheRequest = dataCacheRequest;
        setDataCacheRequestIsSet(true);
        this.logSorts = logSorts;
        this.flushs = flushs;
        setFlushsIsSet(true);
        this.syncs = syncs;
        setSyncsIsSet(true);
        this.bulkImports = bulkImports;
        this.responseTime = responseTime;
        setResponseTimeIsSet(true);
    }

    public TabletServerStatus(TabletServerStatus other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetTableMap()) {
            Map<String, TableInfo> __this__tableMap = new HashMap<String, TableInfo>(other.tableMap.size());
            for (Map.Entry<String, TableInfo> other_element : other.tableMap.entrySet()) {
                String other_element_key = other_element.getKey();
                TableInfo other_element_value = other_element.getValue();
                String __this__tableMap_copy_key = other_element_key;
                TableInfo __this__tableMap_copy_value = new TableInfo(other_element_value);
                __this__tableMap.put(__this__tableMap_copy_key, __this__tableMap_copy_value);
            }
            this.tableMap = __this__tableMap;
        }
        this.lastContact = other.lastContact;
        if (other.isSetName()) {
            this.name = other.name;
        }
        this.osLoad = other.osLoad;
        this.holdTime = other.holdTime;
        this.lookups = other.lookups;
        this.indexCacheHits = other.indexCacheHits;
        this.indexCacheRequest = other.indexCacheRequest;
        this.dataCacheHits = other.dataCacheHits;
        this.dataCacheRequest = other.dataCacheRequest;
        if (other.isSetLogSorts()) {
            List<RecoveryStatus> __this__logSorts = new ArrayList<RecoveryStatus>(other.logSorts.size());
            for (RecoveryStatus other_element : other.logSorts) {
                __this__logSorts.add(new RecoveryStatus(other_element));
            }
            this.logSorts = __this__logSorts;
        }
        this.flushs = other.flushs;
        this.syncs = other.syncs;
        if (other.isSetBulkImports()) {
            List<BulkImportStatus> __this__bulkImports = new ArrayList<BulkImportStatus>(other.bulkImports.size());
            for (BulkImportStatus other_element : other.bulkImports) {
                __this__bulkImports.add(new BulkImportStatus(other_element));
            }
            this.bulkImports = __this__bulkImports;
        }
        this.responseTime = other.responseTime;
    }

    public TabletServerStatus deepCopy() {
        return new TabletServerStatus(this);
    }

    @Override
    public void clear() {
        this.tableMap = null;
        setLastContactIsSet(false);
        this.lastContact = 0;
        this.name = null;
        setOsLoadIsSet(false);
        this.osLoad = 0.0;
        setHoldTimeIsSet(false);
        this.holdTime = 0;
        setLookupsIsSet(false);
        this.lookups = 0;
        setIndexCacheHitsIsSet(false);
        this.indexCacheHits = 0;
        setIndexCacheRequestIsSet(false);
        this.indexCacheRequest = 0;
        setDataCacheHitsIsSet(false);
        this.dataCacheHits = 0;
        setDataCacheRequestIsSet(false);
        this.dataCacheRequest = 0;
        this.logSorts = null;
        setFlushsIsSet(false);
        this.flushs = 0;
        setSyncsIsSet(false);
        this.syncs = 0;
        this.bulkImports = null;
        setResponseTimeIsSet(false);
        this.responseTime = 0;
    }

    public int getTableMapSize() {
        return (this.tableMap == null) ? 0 : this.tableMap.size();
    }

    public void putToTableMap(String key, TableInfo val) {
        if (this.tableMap == null) {
            this.tableMap = new HashMap<String, TableInfo>();
        }
        this.tableMap.put(key, val);
    }

    public Map<String, TableInfo> getTableMap() {
        return this.tableMap;
    }

    public TabletServerStatus setTableMap(Map<String, TableInfo> tableMap) {
        this.tableMap = tableMap;
        return this;
    }

    public void unsetTableMap() {
        this.tableMap = null;
    }

    public boolean isSetTableMap() {
        return this.tableMap != null;
    }

    public void setTableMapIsSet(boolean value) {
        if (!value) {
            this.tableMap = null;
        }
    }

    public long getLastContact() {
        return this.lastContact;
    }

    public TabletServerStatus setLastContact(long lastContact) {
        this.lastContact = lastContact;
        setLastContactIsSet(true);
        return this;
    }

    public void unsetLastContact() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __LASTCONTACT_ISSET_ID);
    }

    public boolean isSetLastContact() {
        return EncodingUtils.testBit(__isset_bitfield, __LASTCONTACT_ISSET_ID);
    }

    public void setLastContactIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __LASTCONTACT_ISSET_ID, value);
    }

    public String getName() {
        return this.name;
    }

    public TabletServerStatus setName(String name) {
        this.name = name;
        return this;
    }

    public void unsetName() {
        this.name = null;
    }

    public boolean isSetName() {
        return this.name != null;
    }

    public void setNameIsSet(boolean value) {
        if (!value) {
            this.name = null;
        }
    }

    public double getOsLoad() {
        return this.osLoad;
    }

    public TabletServerStatus setOsLoad(double osLoad) {
        this.osLoad = osLoad;
        setOsLoadIsSet(true);
        return this;
    }

    public void unsetOsLoad() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __OSLOAD_ISSET_ID);
    }

    public boolean isSetOsLoad() {
        return EncodingUtils.testBit(__isset_bitfield, __OSLOAD_ISSET_ID);
    }

    public void setOsLoadIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __OSLOAD_ISSET_ID, value);
    }

    public long getHoldTime() {
        return this.holdTime;
    }

    public TabletServerStatus setHoldTime(long holdTime) {
        this.holdTime = holdTime;
        setHoldTimeIsSet(true);
        return this;
    }

    public void unsetHoldTime() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __HOLDTIME_ISSET_ID);
    }

    public boolean isSetHoldTime() {
        return EncodingUtils.testBit(__isset_bitfield, __HOLDTIME_ISSET_ID);
    }

    public void setHoldTimeIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __HOLDTIME_ISSET_ID, value);
    }

    public long getLookups() {
        return this.lookups;
    }

    public TabletServerStatus setLookups(long lookups) {
        this.lookups = lookups;
        setLookupsIsSet(true);
        return this;
    }

    public void unsetLookups() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __LOOKUPS_ISSET_ID);
    }

    public boolean isSetLookups() {
        return EncodingUtils.testBit(__isset_bitfield, __LOOKUPS_ISSET_ID);
    }

    public void setLookupsIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __LOOKUPS_ISSET_ID, value);
    }

    public long getIndexCacheHits() {
        return this.indexCacheHits;
    }

    public TabletServerStatus setIndexCacheHits(long indexCacheHits) {
        this.indexCacheHits = indexCacheHits;
        setIndexCacheHitsIsSet(true);
        return this;
    }

    public void unsetIndexCacheHits() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __INDEXCACHEHITS_ISSET_ID);
    }

    public boolean isSetIndexCacheHits() {
        return EncodingUtils.testBit(__isset_bitfield, __INDEXCACHEHITS_ISSET_ID);
    }

    public void setIndexCacheHitsIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __INDEXCACHEHITS_ISSET_ID, value);
    }

    public long getIndexCacheRequest() {
        return this.indexCacheRequest;
    }

    public TabletServerStatus setIndexCacheRequest(long indexCacheRequest) {
        this.indexCacheRequest = indexCacheRequest;
        setIndexCacheRequestIsSet(true);
        return this;
    }

    public void unsetIndexCacheRequest() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __INDEXCACHEREQUEST_ISSET_ID);
    }

    public boolean isSetIndexCacheRequest() {
        return EncodingUtils.testBit(__isset_bitfield, __INDEXCACHEREQUEST_ISSET_ID);
    }

    public void setIndexCacheRequestIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __INDEXCACHEREQUEST_ISSET_ID, value);
    }

    public long getDataCacheHits() {
        return this.dataCacheHits;
    }

    public TabletServerStatus setDataCacheHits(long dataCacheHits) {
        this.dataCacheHits = dataCacheHits;
        setDataCacheHitsIsSet(true);
        return this;
    }

    public void unsetDataCacheHits() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __DATACACHEHITS_ISSET_ID);
    }

    public boolean isSetDataCacheHits() {
        return EncodingUtils.testBit(__isset_bitfield, __DATACACHEHITS_ISSET_ID);
    }

    public void setDataCacheHitsIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __DATACACHEHITS_ISSET_ID, value);
    }

    public long getDataCacheRequest() {
        return this.dataCacheRequest;
    }

    public TabletServerStatus setDataCacheRequest(long dataCacheRequest) {
        this.dataCacheRequest = dataCacheRequest;
        setDataCacheRequestIsSet(true);
        return this;
    }

    public void unsetDataCacheRequest() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __DATACACHEREQUEST_ISSET_ID);
    }

    public boolean isSetDataCacheRequest() {
        return EncodingUtils.testBit(__isset_bitfield, __DATACACHEREQUEST_ISSET_ID);
    }

    public void setDataCacheRequestIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __DATACACHEREQUEST_ISSET_ID, value);
    }

    public int getLogSortsSize() {
        return (this.logSorts == null) ? 0 : this.logSorts.size();
    }

    public java.util.Iterator<RecoveryStatus> getLogSortsIterator() {
        return (this.logSorts == null) ? null : this.logSorts.iterator();
    }

    public void addToLogSorts(RecoveryStatus elem) {
        if (this.logSorts == null) {
            this.logSorts = new ArrayList<RecoveryStatus>();
        }
        this.logSorts.add(elem);
    }

    public List<RecoveryStatus> getLogSorts() {
        return this.logSorts;
    }

    public TabletServerStatus setLogSorts(List<RecoveryStatus> logSorts) {
        this.logSorts = logSorts;
        return this;
    }

    public void unsetLogSorts() {
        this.logSorts = null;
    }

    public boolean isSetLogSorts() {
        return this.logSorts != null;
    }

    public void setLogSortsIsSet(boolean value) {
        if (!value) {
            this.logSorts = null;
        }
    }

    public long getFlushs() {
        return this.flushs;
    }

    public TabletServerStatus setFlushs(long flushs) {
        this.flushs = flushs;
        setFlushsIsSet(true);
        return this;
    }

    public void unsetFlushs() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __FLUSHS_ISSET_ID);
    }

    public boolean isSetFlushs() {
        return EncodingUtils.testBit(__isset_bitfield, __FLUSHS_ISSET_ID);
    }

    public void setFlushsIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __FLUSHS_ISSET_ID, value);
    }

    public long getSyncs() {
        return this.syncs;
    }

    public TabletServerStatus setSyncs(long syncs) {
        this.syncs = syncs;
        setSyncsIsSet(true);
        return this;
    }

    public void unsetSyncs() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __SYNCS_ISSET_ID);
    }

    public boolean isSetSyncs() {
        return EncodingUtils.testBit(__isset_bitfield, __SYNCS_ISSET_ID);
    }

    public void setSyncsIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __SYNCS_ISSET_ID, value);
    }

    public int getBulkImportsSize() {
        return (this.bulkImports == null) ? 0 : this.bulkImports.size();
    }

    public java.util.Iterator<BulkImportStatus> getBulkImportsIterator() {
        return (this.bulkImports == null) ? null : this.bulkImports.iterator();
    }

    public void addToBulkImports(BulkImportStatus elem) {
        if (this.bulkImports == null) {
            this.bulkImports = new ArrayList<BulkImportStatus>();
        }
        this.bulkImports.add(elem);
    }

    public List<BulkImportStatus> getBulkImports() {
        return this.bulkImports;
    }

    public TabletServerStatus setBulkImports(List<BulkImportStatus> bulkImports) {
        this.bulkImports = bulkImports;
        return this;
    }

    public void unsetBulkImports() {
        this.bulkImports = null;
    }

    public boolean isSetBulkImports() {
        return this.bulkImports != null;
    }

    public void setBulkImportsIsSet(boolean value) {
        if (!value) {
            this.bulkImports = null;
        }
    }

    public long getResponseTime() {
        return this.responseTime;
    }

    public TabletServerStatus setResponseTime(long responseTime) {
        this.responseTime = responseTime;
        setResponseTimeIsSet(true);
        return this;
    }

    public void unsetResponseTime() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __RESPONSETIME_ISSET_ID);
    }

    public boolean isSetResponseTime() {
        return EncodingUtils.testBit(__isset_bitfield, __RESPONSETIME_ISSET_ID);
    }

    public void setResponseTimeIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __RESPONSETIME_ISSET_ID, value);
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case TABLE_MAP:
                if (value == null) {
                    unsetTableMap();
                } else {
                    setTableMap((Map<String, TableInfo>) value);
                }
                break;
            case LAST_CONTACT:
                if (value == null) {
                    unsetLastContact();
                } else {
                    setLastContact((Long) value);
                }
                break;
            case NAME:
                if (value == null) {
                    unsetName();
                } else {
                    setName((String) value);
                }
                break;
            case OS_LOAD:
                if (value == null) {
                    unsetOsLoad();
                } else {
                    setOsLoad((Double) value);
                }
                break;
            case HOLD_TIME:
                if (value == null) {
                    unsetHoldTime();
                } else {
                    setHoldTime((Long) value);
                }
                break;
            case LOOKUPS:
                if (value == null) {
                    unsetLookups();
                } else {
                    setLookups((Long) value);
                }
                break;
            case INDEX_CACHE_HITS:
                if (value == null) {
                    unsetIndexCacheHits();
                } else {
                    setIndexCacheHits((Long) value);
                }
                break;
            case INDEX_CACHE_REQUEST:
                if (value == null) {
                    unsetIndexCacheRequest();
                } else {
                    setIndexCacheRequest((Long) value);
                }
                break;
            case DATA_CACHE_HITS:
                if (value == null) {
                    unsetDataCacheHits();
                } else {
                    setDataCacheHits((Long) value);
                }
                break;
            case DATA_CACHE_REQUEST:
                if (value == null) {
                    unsetDataCacheRequest();
                } else {
                    setDataCacheRequest((Long) value);
                }
                break;
            case LOG_SORTS:
                if (value == null) {
                    unsetLogSorts();
                } else {
                    setLogSorts((List<RecoveryStatus>) value);
                }
                break;
            case FLUSHS:
                if (value == null) {
                    unsetFlushs();
                } else {
                    setFlushs((Long) value);
                }
                break;
            case SYNCS:
                if (value == null) {
                    unsetSyncs();
                } else {
                    setSyncs((Long) value);
                }
                break;
            case BULK_IMPORTS:
                if (value == null) {
                    unsetBulkImports();
                } else {
                    setBulkImports((List<BulkImportStatus>) value);
                }
                break;
            case RESPONSE_TIME:
                if (value == null) {
                    unsetResponseTime();
                } else {
                    setResponseTime((Long) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case TABLE_MAP:
                return getTableMap();
            case LAST_CONTACT:
                return getLastContact();
            case NAME:
                return getName();
            case OS_LOAD:
                return getOsLoad();
            case HOLD_TIME:
                return getHoldTime();
            case LOOKUPS:
                return getLookups();
            case INDEX_CACHE_HITS:
                return getIndexCacheHits();
            case INDEX_CACHE_REQUEST:
                return getIndexCacheRequest();
            case DATA_CACHE_HITS:
                return getDataCacheHits();
            case DATA_CACHE_REQUEST:
                return getDataCacheRequest();
            case LOG_SORTS:
                return getLogSorts();
            case FLUSHS:
                return getFlushs();
            case SYNCS:
                return getSyncs();
            case BULK_IMPORTS:
                return getBulkImports();
            case RESPONSE_TIME:
                return getResponseTime();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case TABLE_MAP:
                return isSetTableMap();
            case LAST_CONTACT:
                return isSetLastContact();
            case NAME:
                return isSetName();
            case OS_LOAD:
                return isSetOsLoad();
            case HOLD_TIME:
                return isSetHoldTime();
            case LOOKUPS:
                return isSetLookups();
            case INDEX_CACHE_HITS:
                return isSetIndexCacheHits();
            case INDEX_CACHE_REQUEST:
                return isSetIndexCacheRequest();
            case DATA_CACHE_HITS:
                return isSetDataCacheHits();
            case DATA_CACHE_REQUEST:
                return isSetDataCacheRequest();
            case LOG_SORTS:
                return isSetLogSorts();
            case FLUSHS:
                return isSetFlushs();
            case SYNCS:
                return isSetSyncs();
            case BULK_IMPORTS:
                return isSetBulkImports();
            case RESPONSE_TIME:
                return isSetResponseTime();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof TabletServerStatus)
            return this.equals((TabletServerStatus) that);
        return false;
    }

    public boolean equals(TabletServerStatus that) {
        if (that == null)
            return false;
        boolean this_present_tableMap = true && this.isSetTableMap();
        boolean that_present_tableMap = true && that.isSetTableMap();
        if (this_present_tableMap || that_present_tableMap) {
            if (!(this_present_tableMap && that_present_tableMap))
                return false;
            if (!this.tableMap.equals(that.tableMap))
                return false;
        }
        boolean this_present_lastContact = true;
        boolean that_present_lastContact = true;
        if (this_present_lastContact || that_present_lastContact) {
            if (!(this_present_lastContact && that_present_lastContact))
                return false;
            if (this.lastContact != that.lastContact)
                return false;
        }
        boolean this_present_name = true && this.isSetName();
        boolean that_present_name = true && that.isSetName();
        if (this_present_name || that_present_name) {
            if (!(this_present_name && that_present_name))
                return false;
            if (!this.name.equals(that.name))
                return false;
        }
        boolean this_present_osLoad = true;
        boolean that_present_osLoad = true;
        if (this_present_osLoad || that_present_osLoad) {
            if (!(this_present_osLoad && that_present_osLoad))
                return false;
            if (this.osLoad != that.osLoad)
                return false;
        }
        boolean this_present_holdTime = true;
        boolean that_present_holdTime = true;
        if (this_present_holdTime || that_present_holdTime) {
            if (!(this_present_holdTime && that_present_holdTime))
                return false;
            if (this.holdTime != that.holdTime)
                return false;
        }
        boolean this_present_lookups = true;
        boolean that_present_lookups = true;
        if (this_present_lookups || that_present_lookups) {
            if (!(this_present_lookups && that_present_lookups))
                return false;
            if (this.lookups != that.lookups)
                return false;
        }
        boolean this_present_indexCacheHits = true;
        boolean that_present_indexCacheHits = true;
        if (this_present_indexCacheHits || that_present_indexCacheHits) {
            if (!(this_present_indexCacheHits && that_present_indexCacheHits))
                return false;
            if (this.indexCacheHits != that.indexCacheHits)
                return false;
        }
        boolean this_present_indexCacheRequest = true;
        boolean that_present_indexCacheRequest = true;
        if (this_present_indexCacheRequest || that_present_indexCacheRequest) {
            if (!(this_present_indexCacheRequest && that_present_indexCacheRequest))
                return false;
            if (this.indexCacheRequest != that.indexCacheRequest)
                return false;
        }
        boolean this_present_dataCacheHits = true;
        boolean that_present_dataCacheHits = true;
        if (this_present_dataCacheHits || that_present_dataCacheHits) {
            if (!(this_present_dataCacheHits && that_present_dataCacheHits))
                return false;
            if (this.dataCacheHits != that.dataCacheHits)
                return false;
        }
        boolean this_present_dataCacheRequest = true;
        boolean that_present_dataCacheRequest = true;
        if (this_present_dataCacheRequest || that_present_dataCacheRequest) {
            if (!(this_present_dataCacheRequest && that_present_dataCacheRequest))
                return false;
            if (this.dataCacheRequest != that.dataCacheRequest)
                return false;
        }
        boolean this_present_logSorts = true && this.isSetLogSorts();
        boolean that_present_logSorts = true && that.isSetLogSorts();
        if (this_present_logSorts || that_present_logSorts) {
            if (!(this_present_logSorts && that_present_logSorts))
                return false;
            if (!this.logSorts.equals(that.logSorts))
                return false;
        }
        boolean this_present_flushs = true;
        boolean that_present_flushs = true;
        if (this_present_flushs || that_present_flushs) {
            if (!(this_present_flushs && that_present_flushs))
                return false;
            if (this.flushs != that.flushs)
                return false;
        }
        boolean this_present_syncs = true;
        boolean that_present_syncs = true;
        if (this_present_syncs || that_present_syncs) {
            if (!(this_present_syncs && that_present_syncs))
                return false;
            if (this.syncs != that.syncs)
                return false;
        }
        boolean this_present_bulkImports = true && this.isSetBulkImports();
        boolean that_present_bulkImports = true && that.isSetBulkImports();
        if (this_present_bulkImports || that_present_bulkImports) {
            if (!(this_present_bulkImports && that_present_bulkImports))
                return false;
            if (!this.bulkImports.equals(that.bulkImports))
                return false;
        }
        boolean this_present_responseTime = true;
        boolean that_present_responseTime = true;
        if (this_present_responseTime || that_present_responseTime) {
            if (!(this_present_responseTime && that_present_responseTime))
                return false;
            if (this.responseTime != that.responseTime)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        List<Object> list = new ArrayList<Object>();
        boolean present_tableMap = true && (isSetTableMap());
        list.add(present_tableMap);
        if (present_tableMap)
            list.add(tableMap);
        boolean present_lastContact = true;
        list.add(present_lastContact);
        if (present_lastContact)
            list.add(lastContact);
        boolean present_name = true && (isSetName());
        list.add(present_name);
        if (present_name)
            list.add(name);
        boolean present_osLoad = true;
        list.add(present_osLoad);
        if (present_osLoad)
            list.add(osLoad);
        boolean present_holdTime = true;
        list.add(present_holdTime);
        if (present_holdTime)
            list.add(holdTime);
        boolean present_lookups = true;
        list.add(present_lookups);
        if (present_lookups)
            list.add(lookups);
        boolean present_indexCacheHits = true;
        list.add(present_indexCacheHits);
        if (present_indexCacheHits)
            list.add(indexCacheHits);
        boolean present_indexCacheRequest = true;
        list.add(present_indexCacheRequest);
        if (present_indexCacheRequest)
            list.add(indexCacheRequest);
        boolean present_dataCacheHits = true;
        list.add(present_dataCacheHits);
        if (present_dataCacheHits)
            list.add(dataCacheHits);
        boolean present_dataCacheRequest = true;
        list.add(present_dataCacheRequest);
        if (present_dataCacheRequest)
            list.add(dataCacheRequest);
        boolean present_logSorts = true && (isSetLogSorts());
        list.add(present_logSorts);
        if (present_logSorts)
            list.add(logSorts);
        boolean present_flushs = true;
        list.add(present_flushs);
        if (present_flushs)
            list.add(flushs);
        boolean present_syncs = true;
        list.add(present_syncs);
        if (present_syncs)
            list.add(syncs);
        boolean present_bulkImports = true && (isSetBulkImports());
        list.add(present_bulkImports);
        if (present_bulkImports)
            list.add(bulkImports);
        boolean present_responseTime = true;
        list.add(present_responseTime);
        if (present_responseTime)
            list.add(responseTime);
        return list.hashCode();
    }

    @Override
    public int compareTo(TabletServerStatus other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetTableMap()).compareTo(other.isSetTableMap());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTableMap()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tableMap, other.tableMap);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetLastContact()).compareTo(other.isSetLastContact());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetLastContact()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.lastContact, other.lastContact);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetName()).compareTo(other.isSetName());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetName()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.name, other.name);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetOsLoad()).compareTo(other.isSetOsLoad());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetOsLoad()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.osLoad, other.osLoad);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetHoldTime()).compareTo(other.isSetHoldTime());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetHoldTime()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.holdTime, other.holdTime);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetLookups()).compareTo(other.isSetLookups());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetLookups()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.lookups, other.lookups);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetIndexCacheHits()).compareTo(other.isSetIndexCacheHits());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetIndexCacheHits()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.indexCacheHits, other.indexCacheHits);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetIndexCacheRequest()).compareTo(other.isSetIndexCacheRequest());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetIndexCacheRequest()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.indexCacheRequest, other.indexCacheRequest);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetDataCacheHits()).compareTo(other.isSetDataCacheHits());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetDataCacheHits()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.dataCacheHits, other.dataCacheHits);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetDataCacheRequest()).compareTo(other.isSetDataCacheRequest());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetDataCacheRequest()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.dataCacheRequest, other.dataCacheRequest);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetLogSorts()).compareTo(other.isSetLogSorts());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetLogSorts()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.logSorts, other.logSorts);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetFlushs()).compareTo(other.isSetFlushs());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetFlushs()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.flushs, other.flushs);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetSyncs()).compareTo(other.isSetSyncs());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetSyncs()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.syncs, other.syncs);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetBulkImports()).compareTo(other.isSetBulkImports());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetBulkImports()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.bulkImports, other.bulkImports);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetResponseTime()).compareTo(other.isSetResponseTime());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetResponseTime()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.responseTime, other.responseTime);
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
        StringBuilder sb = new StringBuilder("TabletServerStatus(");
        boolean first = true;
        sb.append("tableMap:");
        if (this.tableMap == null) {
            sb.append("null");
        } else {
            sb.append(this.tableMap);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("lastContact:");
        sb.append(this.lastContact);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("name:");
        if (this.name == null) {
            sb.append("null");
        } else {
            sb.append(this.name);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("osLoad:");
        sb.append(this.osLoad);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("holdTime:");
        sb.append(this.holdTime);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("lookups:");
        sb.append(this.lookups);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("indexCacheHits:");
        sb.append(this.indexCacheHits);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("indexCacheRequest:");
        sb.append(this.indexCacheRequest);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("dataCacheHits:");
        sb.append(this.dataCacheHits);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("dataCacheRequest:");
        sb.append(this.dataCacheRequest);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("logSorts:");
        if (this.logSorts == null) {
            sb.append("null");
        } else {
            sb.append(this.logSorts);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("flushs:");
        sb.append(this.flushs);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("syncs:");
        sb.append(this.syncs);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("bulkImports:");
        if (this.bulkImports == null) {
            sb.append("null");
        } else {
            sb.append(this.bulkImports);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("responseTime:");
        sb.append(this.responseTime);
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
            __isset_bitfield = 0;
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class TabletServerStatusStandardSchemeFactory implements SchemeFactory {

        public TabletServerStatusStandardScheme getScheme() {
            return new TabletServerStatusStandardScheme();
        }
    }

    private static class TabletServerStatusStandardScheme extends StandardScheme<TabletServerStatus> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, TabletServerStatus struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch(schemeField.id) {
                    case 1:
                        if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
                            {
                                org.apache.thrift.protocol.TMap _map0 = iprot.readMapBegin();
                                struct.tableMap = new HashMap<String, TableInfo>(2 * _map0.size);
                                String _key1;
                                TableInfo _val2;
                                for (int _i3 = 0; _i3 < _map0.size; ++_i3) {
                                    _key1 = iprot.readString();
                                    _val2 = new TableInfo();
                                    _val2.read(iprot);
                                    struct.tableMap.put(_key1, _val2);
                                }
                                iprot.readMapEnd();
                            }
                            struct.setTableMapIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.lastContact = iprot.readI64();
                            struct.setLastContactIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.name = iprot.readString();
                            struct.setNameIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 5:
                        if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
                            struct.osLoad = iprot.readDouble();
                            struct.setOsLoadIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 7:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.holdTime = iprot.readI64();
                            struct.setHoldTimeIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 8:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.lookups = iprot.readI64();
                            struct.setLookupsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 10:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.indexCacheHits = iprot.readI64();
                            struct.setIndexCacheHitsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 11:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.indexCacheRequest = iprot.readI64();
                            struct.setIndexCacheRequestIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 12:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.dataCacheHits = iprot.readI64();
                            struct.setDataCacheHitsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 13:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.dataCacheRequest = iprot.readI64();
                            struct.setDataCacheRequestIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 14:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list4 = iprot.readListBegin();
                                struct.logSorts = new ArrayList<RecoveryStatus>(_list4.size);
                                RecoveryStatus _elem5;
                                for (int _i6 = 0; _i6 < _list4.size; ++_i6) {
                                    _elem5 = new RecoveryStatus();
                                    _elem5.read(iprot);
                                    struct.logSorts.add(_elem5);
                                }
                                iprot.readListEnd();
                            }
                            struct.setLogSortsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 15:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.flushs = iprot.readI64();
                            struct.setFlushsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 16:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.syncs = iprot.readI64();
                            struct.setSyncsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 17:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list7 = iprot.readListBegin();
                                struct.bulkImports = new ArrayList<BulkImportStatus>(_list7.size);
                                BulkImportStatus _elem8;
                                for (int _i9 = 0; _i9 < _list7.size; ++_i9) {
                                    _elem8 = new BulkImportStatus();
                                    _elem8.read(iprot);
                                    struct.bulkImports.add(_elem8);
                                }
                                iprot.readListEnd();
                            }
                            struct.setBulkImportsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 18:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.responseTime = iprot.readI64();
                            struct.setResponseTimeIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, TabletServerStatus struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.tableMap != null) {
                oprot.writeFieldBegin(TABLE_MAP_FIELD_DESC);
                {
                    oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRUCT, struct.tableMap.size()));
                    for (Map.Entry<String, TableInfo> _iter10 : struct.tableMap.entrySet()) {
                        oprot.writeString(_iter10.getKey());
                        _iter10.getValue().write(oprot);
                    }
                    oprot.writeMapEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(LAST_CONTACT_FIELD_DESC);
            oprot.writeI64(struct.lastContact);
            oprot.writeFieldEnd();
            if (struct.name != null) {
                oprot.writeFieldBegin(NAME_FIELD_DESC);
                oprot.writeString(struct.name);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(OS_LOAD_FIELD_DESC);
            oprot.writeDouble(struct.osLoad);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(HOLD_TIME_FIELD_DESC);
            oprot.writeI64(struct.holdTime);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(LOOKUPS_FIELD_DESC);
            oprot.writeI64(struct.lookups);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(INDEX_CACHE_HITS_FIELD_DESC);
            oprot.writeI64(struct.indexCacheHits);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(INDEX_CACHE_REQUEST_FIELD_DESC);
            oprot.writeI64(struct.indexCacheRequest);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(DATA_CACHE_HITS_FIELD_DESC);
            oprot.writeI64(struct.dataCacheHits);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(DATA_CACHE_REQUEST_FIELD_DESC);
            oprot.writeI64(struct.dataCacheRequest);
            oprot.writeFieldEnd();
            if (struct.logSorts != null) {
                oprot.writeFieldBegin(LOG_SORTS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.logSorts.size()));
                    for (RecoveryStatus _iter11 : struct.logSorts) {
                        _iter11.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(FLUSHS_FIELD_DESC);
            oprot.writeI64(struct.flushs);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(SYNCS_FIELD_DESC);
            oprot.writeI64(struct.syncs);
            oprot.writeFieldEnd();
            if (struct.bulkImports != null) {
                oprot.writeFieldBegin(BULK_IMPORTS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.bulkImports.size()));
                    for (BulkImportStatus _iter12 : struct.bulkImports) {
                        _iter12.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(RESPONSE_TIME_FIELD_DESC);
            oprot.writeI64(struct.responseTime);
            oprot.writeFieldEnd();
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class TabletServerStatusTupleSchemeFactory implements SchemeFactory {

        public TabletServerStatusTupleScheme getScheme() {
            return new TabletServerStatusTupleScheme();
        }
    }

    private static class TabletServerStatusTupleScheme extends TupleScheme<TabletServerStatus> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, TabletServerStatus struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetTableMap()) {
                optionals.set(0);
            }
            if (struct.isSetLastContact()) {
                optionals.set(1);
            }
            if (struct.isSetName()) {
                optionals.set(2);
            }
            if (struct.isSetOsLoad()) {
                optionals.set(3);
            }
            if (struct.isSetHoldTime()) {
                optionals.set(4);
            }
            if (struct.isSetLookups()) {
                optionals.set(5);
            }
            if (struct.isSetIndexCacheHits()) {
                optionals.set(6);
            }
            if (struct.isSetIndexCacheRequest()) {
                optionals.set(7);
            }
            if (struct.isSetDataCacheHits()) {
                optionals.set(8);
            }
            if (struct.isSetDataCacheRequest()) {
                optionals.set(9);
            }
            if (struct.isSetLogSorts()) {
                optionals.set(10);
            }
            if (struct.isSetFlushs()) {
                optionals.set(11);
            }
            if (struct.isSetSyncs()) {
                optionals.set(12);
            }
            if (struct.isSetBulkImports()) {
                optionals.set(13);
            }
            if (struct.isSetResponseTime()) {
                optionals.set(14);
            }
            oprot.writeBitSet(optionals, 15);
            if (struct.isSetTableMap()) {
                {
                    oprot.writeI32(struct.tableMap.size());
                    for (Map.Entry<String, TableInfo> _iter13 : struct.tableMap.entrySet()) {
                        oprot.writeString(_iter13.getKey());
                        _iter13.getValue().write(oprot);
                    }
                }
            }
            if (struct.isSetLastContact()) {
                oprot.writeI64(struct.lastContact);
            }
            if (struct.isSetName()) {
                oprot.writeString(struct.name);
            }
            if (struct.isSetOsLoad()) {
                oprot.writeDouble(struct.osLoad);
            }
            if (struct.isSetHoldTime()) {
                oprot.writeI64(struct.holdTime);
            }
            if (struct.isSetLookups()) {
                oprot.writeI64(struct.lookups);
            }
            if (struct.isSetIndexCacheHits()) {
                oprot.writeI64(struct.indexCacheHits);
            }
            if (struct.isSetIndexCacheRequest()) {
                oprot.writeI64(struct.indexCacheRequest);
            }
            if (struct.isSetDataCacheHits()) {
                oprot.writeI64(struct.dataCacheHits);
            }
            if (struct.isSetDataCacheRequest()) {
                oprot.writeI64(struct.dataCacheRequest);
            }
            if (struct.isSetLogSorts()) {
                {
                    oprot.writeI32(struct.logSorts.size());
                    for (RecoveryStatus _iter14 : struct.logSorts) {
                        _iter14.write(oprot);
                    }
                }
            }
            if (struct.isSetFlushs()) {
                oprot.writeI64(struct.flushs);
            }
            if (struct.isSetSyncs()) {
                oprot.writeI64(struct.syncs);
            }
            if (struct.isSetBulkImports()) {
                {
                    oprot.writeI32(struct.bulkImports.size());
                    for (BulkImportStatus _iter15 : struct.bulkImports) {
                        _iter15.write(oprot);
                    }
                }
            }
            if (struct.isSetResponseTime()) {
                oprot.writeI64(struct.responseTime);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, TabletServerStatus struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(15);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TMap _map16 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.tableMap = new HashMap<String, TableInfo>(2 * _map16.size);
                    String _key17;
                    TableInfo _val18;
                    for (int _i19 = 0; _i19 < _map16.size; ++_i19) {
                        _key17 = iprot.readString();
                        _val18 = new TableInfo();
                        _val18.read(iprot);
                        struct.tableMap.put(_key17, _val18);
                    }
                }
                struct.setTableMapIsSet(true);
            }
            if (incoming.get(1)) {
                struct.lastContact = iprot.readI64();
                struct.setLastContactIsSet(true);
            }
            if (incoming.get(2)) {
                struct.name = iprot.readString();
                struct.setNameIsSet(true);
            }
            if (incoming.get(3)) {
                struct.osLoad = iprot.readDouble();
                struct.setOsLoadIsSet(true);
            }
            if (incoming.get(4)) {
                struct.holdTime = iprot.readI64();
                struct.setHoldTimeIsSet(true);
            }
            if (incoming.get(5)) {
                struct.lookups = iprot.readI64();
                struct.setLookupsIsSet(true);
            }
            if (incoming.get(6)) {
                struct.indexCacheHits = iprot.readI64();
                struct.setIndexCacheHitsIsSet(true);
            }
            if (incoming.get(7)) {
                struct.indexCacheRequest = iprot.readI64();
                struct.setIndexCacheRequestIsSet(true);
            }
            if (incoming.get(8)) {
                struct.dataCacheHits = iprot.readI64();
                struct.setDataCacheHitsIsSet(true);
            }
            if (incoming.get(9)) {
                struct.dataCacheRequest = iprot.readI64();
                struct.setDataCacheRequestIsSet(true);
            }
            if (incoming.get(10)) {
                {
                    org.apache.thrift.protocol.TList _list20 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.logSorts = new ArrayList<RecoveryStatus>(_list20.size);
                    RecoveryStatus _elem21;
                    for (int _i22 = 0; _i22 < _list20.size; ++_i22) {
                        _elem21 = new RecoveryStatus();
                        _elem21.read(iprot);
                        struct.logSorts.add(_elem21);
                    }
                }
                struct.setLogSortsIsSet(true);
            }
            if (incoming.get(11)) {
                struct.flushs = iprot.readI64();
                struct.setFlushsIsSet(true);
            }
            if (incoming.get(12)) {
                struct.syncs = iprot.readI64();
                struct.setSyncsIsSet(true);
            }
            if (incoming.get(13)) {
                {
                    org.apache.thrift.protocol.TList _list23 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.bulkImports = new ArrayList<BulkImportStatus>(_list23.size);
                    BulkImportStatus _elem24;
                    for (int _i25 = 0; _i25 < _list23.size; ++_i25) {
                        _elem24 = new BulkImportStatus();
                        _elem24.read(iprot);
                        struct.bulkImports.add(_elem24);
                    }
                }
                struct.setBulkImportsIsSet(true);
            }
            if (incoming.get(14)) {
                struct.responseTime = iprot.readI64();
                struct.setResponseTimeIsSet(true);
            }
        }
    }
}