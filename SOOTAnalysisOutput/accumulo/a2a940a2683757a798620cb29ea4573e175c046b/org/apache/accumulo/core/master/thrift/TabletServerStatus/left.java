package org.apache.accumulo.core.master.thrift;

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

@SuppressWarnings("all")
public class TabletServerStatus implements org.apache.thrift.TBase<TabletServerStatus, TabletServerStatus._Fields>, java.io.Serializable, Cloneable {

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
        LOG_SORTS((short) 14, "logSorts");

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

    private BitSet __isset_bit_vector = new BitSet(8);

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
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TabletServerStatus.class, metaDataMap);
    }

    public TabletServerStatus() {
    }

    public TabletServerStatus(Map<String, TableInfo> tableMap, long lastContact, String name, double osLoad, long holdTime, long lookups, long indexCacheHits, long indexCacheRequest, long dataCacheHits, long dataCacheRequest, List<RecoveryStatus> logSorts) {
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
    }

    public TabletServerStatus(TabletServerStatus other) {
        __isset_bit_vector.clear();
        __isset_bit_vector.or(other.__isset_bit_vector);
        if (other.isSetTableMap()) {
            Map<String, TableInfo> __this__tableMap = new HashMap<String, TableInfo>();
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
            List<RecoveryStatus> __this__logSorts = new ArrayList<RecoveryStatus>();
            for (RecoveryStatus other_element : other.logSorts) {
                __this__logSorts.add(new RecoveryStatus(other_element));
            }
            this.logSorts = __this__logSorts;
        }
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
        __isset_bit_vector.clear(__LASTCONTACT_ISSET_ID);
    }

    public boolean isSetLastContact() {
        return __isset_bit_vector.get(__LASTCONTACT_ISSET_ID);
    }

    public void setLastContactIsSet(boolean value) {
        __isset_bit_vector.set(__LASTCONTACT_ISSET_ID, value);
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
        __isset_bit_vector.clear(__OSLOAD_ISSET_ID);
    }

    public boolean isSetOsLoad() {
        return __isset_bit_vector.get(__OSLOAD_ISSET_ID);
    }

    public void setOsLoadIsSet(boolean value) {
        __isset_bit_vector.set(__OSLOAD_ISSET_ID, value);
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
        __isset_bit_vector.clear(__HOLDTIME_ISSET_ID);
    }

    public boolean isSetHoldTime() {
        return __isset_bit_vector.get(__HOLDTIME_ISSET_ID);
    }

    public void setHoldTimeIsSet(boolean value) {
        __isset_bit_vector.set(__HOLDTIME_ISSET_ID, value);
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
        __isset_bit_vector.clear(__LOOKUPS_ISSET_ID);
    }

    public boolean isSetLookups() {
        return __isset_bit_vector.get(__LOOKUPS_ISSET_ID);
    }

    public void setLookupsIsSet(boolean value) {
        __isset_bit_vector.set(__LOOKUPS_ISSET_ID, value);
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
        __isset_bit_vector.clear(__INDEXCACHEHITS_ISSET_ID);
    }

    public boolean isSetIndexCacheHits() {
        return __isset_bit_vector.get(__INDEXCACHEHITS_ISSET_ID);
    }

    public void setIndexCacheHitsIsSet(boolean value) {
        __isset_bit_vector.set(__INDEXCACHEHITS_ISSET_ID, value);
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
        __isset_bit_vector.clear(__INDEXCACHEREQUEST_ISSET_ID);
    }

    public boolean isSetIndexCacheRequest() {
        return __isset_bit_vector.get(__INDEXCACHEREQUEST_ISSET_ID);
    }

    public void setIndexCacheRequestIsSet(boolean value) {
        __isset_bit_vector.set(__INDEXCACHEREQUEST_ISSET_ID, value);
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
        __isset_bit_vector.clear(__DATACACHEHITS_ISSET_ID);
    }

    public boolean isSetDataCacheHits() {
        return __isset_bit_vector.get(__DATACACHEHITS_ISSET_ID);
    }

    public void setDataCacheHitsIsSet(boolean value) {
        __isset_bit_vector.set(__DATACACHEHITS_ISSET_ID, value);
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
        __isset_bit_vector.clear(__DATACACHEREQUEST_ISSET_ID);
    }

    public boolean isSetDataCacheRequest() {
        return __isset_bit_vector.get(__DATACACHEREQUEST_ISSET_ID);
    }

    public void setDataCacheRequestIsSet(boolean value) {
        __isset_bit_vector.set(__DATACACHEREQUEST_ISSET_ID, value);
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
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case TABLE_MAP:
                return getTableMap();
            case LAST_CONTACT:
                return Long.valueOf(getLastContact());
            case NAME:
                return getName();
            case OS_LOAD:
                return Double.valueOf(getOsLoad());
            case HOLD_TIME:
                return Long.valueOf(getHoldTime());
            case LOOKUPS:
                return Long.valueOf(getLookups());
            case INDEX_CACHE_HITS:
                return Long.valueOf(getIndexCacheHits());
            case INDEX_CACHE_REQUEST:
                return Long.valueOf(getIndexCacheRequest());
            case DATA_CACHE_HITS:
                return Long.valueOf(getDataCacheHits());
            case DATA_CACHE_REQUEST:
                return Long.valueOf(getDataCacheRequest());
            case LOG_SORTS:
                return getLogSorts();
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
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public int compareTo(TabletServerStatus other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        TabletServerStatus typedOther = (TabletServerStatus) other;
        lastComparison = Boolean.valueOf(isSetTableMap()).compareTo(typedOther.isSetTableMap());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTableMap()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tableMap, typedOther.tableMap);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetLastContact()).compareTo(typedOther.isSetLastContact());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetLastContact()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.lastContact, typedOther.lastContact);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetName()).compareTo(typedOther.isSetName());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetName()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.name, typedOther.name);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetOsLoad()).compareTo(typedOther.isSetOsLoad());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetOsLoad()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.osLoad, typedOther.osLoad);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetHoldTime()).compareTo(typedOther.isSetHoldTime());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetHoldTime()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.holdTime, typedOther.holdTime);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetLookups()).compareTo(typedOther.isSetLookups());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetLookups()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.lookups, typedOther.lookups);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetIndexCacheHits()).compareTo(typedOther.isSetIndexCacheHits());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetIndexCacheHits()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.indexCacheHits, typedOther.indexCacheHits);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetIndexCacheRequest()).compareTo(typedOther.isSetIndexCacheRequest());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetIndexCacheRequest()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.indexCacheRequest, typedOther.indexCacheRequest);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetDataCacheHits()).compareTo(typedOther.isSetDataCacheHits());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetDataCacheHits()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.dataCacheHits, typedOther.dataCacheHits);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetDataCacheRequest()).compareTo(typedOther.isSetDataCacheRequest());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetDataCacheRequest()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.dataCacheRequest, typedOther.dataCacheRequest);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetLogSorts()).compareTo(typedOther.isSetLogSorts());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetLogSorts()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.logSorts, typedOther.logSorts);
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
            __isset_bit_vector = new BitSet(1);
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
                                for (int _i1 = 0; _i1 < _map0.size; ++_i1) {
                                    String _key2;
                                    TableInfo _val3;
                                    _key2 = iprot.readString();
                                    _val3 = new TableInfo();
                                    _val3.read(iprot);
                                    struct.tableMap.put(_key2, _val3);
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
                                for (int _i5 = 0; _i5 < _list4.size; ++_i5) {
                                    RecoveryStatus _elem6;
                                    _elem6 = new RecoveryStatus();
                                    _elem6.read(iprot);
                                    struct.logSorts.add(_elem6);
                                }
                                iprot.readListEnd();
                            }
                            struct.setLogSortsIsSet(true);
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
                    for (Map.Entry<String, TableInfo> _iter7 : struct.tableMap.entrySet()) {
                        oprot.writeString(_iter7.getKey());
                        _iter7.getValue().write(oprot);
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
                    for (RecoveryStatus _iter8 : struct.logSorts) {
                        _iter8.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
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
            oprot.writeBitSet(optionals, 11);
            if (struct.isSetTableMap()) {
                {
                    oprot.writeI32(struct.tableMap.size());
                    for (Map.Entry<String, TableInfo> _iter9 : struct.tableMap.entrySet()) {
                        oprot.writeString(_iter9.getKey());
                        _iter9.getValue().write(oprot);
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
                    for (RecoveryStatus _iter10 : struct.logSorts) {
                        _iter10.write(oprot);
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, TabletServerStatus struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(11);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TMap _map11 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.tableMap = new HashMap<String, TableInfo>(2 * _map11.size);
                    for (int _i12 = 0; _i12 < _map11.size; ++_i12) {
                        String _key13;
                        TableInfo _val14;
                        _key13 = iprot.readString();
                        _val14 = new TableInfo();
                        _val14.read(iprot);
                        struct.tableMap.put(_key13, _val14);
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
                    org.apache.thrift.protocol.TList _list15 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.logSorts = new ArrayList<RecoveryStatus>(_list15.size);
                    for (int _i16 = 0; _i16 < _list15.size; ++_i16) {
                        RecoveryStatus _elem17;
                        _elem17 = new RecoveryStatus();
                        _elem17.read(iprot);
                        struct.logSorts.add(_elem17);
                    }
                }
                struct.setLogSortsIsSet(true);
            }
        }
    }
}
