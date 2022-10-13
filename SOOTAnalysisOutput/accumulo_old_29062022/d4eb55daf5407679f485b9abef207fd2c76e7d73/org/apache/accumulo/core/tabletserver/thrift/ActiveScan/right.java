package org.apache.accumulo.core.tabletserver.thrift;

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
public class ActiveScan implements org.apache.thrift.TBase<ActiveScan, ActiveScan._Fields>, java.io.Serializable, Cloneable, Comparable<ActiveScan> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ActiveScan");

    private static final org.apache.thrift.protocol.TField CLIENT_FIELD_DESC = new org.apache.thrift.protocol.TField("client", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.protocol.TField USER_FIELD_DESC = new org.apache.thrift.protocol.TField("user", org.apache.thrift.protocol.TType.STRING, (short) 3);

    private static final org.apache.thrift.protocol.TField TABLE_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("tableId", org.apache.thrift.protocol.TType.STRING, (short) 4);

    private static final org.apache.thrift.protocol.TField AGE_FIELD_DESC = new org.apache.thrift.protocol.TField("age", org.apache.thrift.protocol.TType.I64, (short) 5);

    private static final org.apache.thrift.protocol.TField IDLE_TIME_FIELD_DESC = new org.apache.thrift.protocol.TField("idleTime", org.apache.thrift.protocol.TType.I64, (short) 6);

    private static final org.apache.thrift.protocol.TField TYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("type", org.apache.thrift.protocol.TType.I32, (short) 7);

    private static final org.apache.thrift.protocol.TField STATE_FIELD_DESC = new org.apache.thrift.protocol.TField("state", org.apache.thrift.protocol.TType.I32, (short) 8);

    private static final org.apache.thrift.protocol.TField EXTENT_FIELD_DESC = new org.apache.thrift.protocol.TField("extent", org.apache.thrift.protocol.TType.STRUCT, (short) 9);

    private static final org.apache.thrift.protocol.TField COLUMNS_FIELD_DESC = new org.apache.thrift.protocol.TField("columns", org.apache.thrift.protocol.TType.LIST, (short) 10);

    private static final org.apache.thrift.protocol.TField SSI_LIST_FIELD_DESC = new org.apache.thrift.protocol.TField("ssiList", org.apache.thrift.protocol.TType.LIST, (short) 11);

    private static final org.apache.thrift.protocol.TField SSIO_FIELD_DESC = new org.apache.thrift.protocol.TField("ssio", org.apache.thrift.protocol.TType.MAP, (short) 12);

    private static final org.apache.thrift.protocol.TField AUTHORIZATIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("authorizations", org.apache.thrift.protocol.TType.LIST, (short) 13);

    private static final org.apache.thrift.protocol.TField SCAN_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("scanId", org.apache.thrift.protocol.TType.I64, (short) 14);

    private static final org.apache.thrift.protocol.TField CLASS_LOADER_CONTEXT_FIELD_DESC = new org.apache.thrift.protocol.TField("classLoaderContext", org.apache.thrift.protocol.TType.STRING, (short) 15);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new ActiveScanStandardSchemeFactory());
        schemes.put(TupleScheme.class, new ActiveScanTupleSchemeFactory());
    }

    public String client;

    public String user;

    public String tableId;

    public long age;

    public long idleTime;

    public ScanType type;

    public ScanState state;

    public org.apache.accumulo.core.data.thrift.TKeyExtent extent;

    public List<org.apache.accumulo.core.data.thrift.TColumn> columns;

    public List<org.apache.accumulo.core.data.thrift.IterInfo> ssiList;

    public Map<String, Map<String, String>> ssio;

    public List<ByteBuffer> authorizations;

    public long scanId;

    public String classLoaderContext;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        CLIENT((short) 2, "client"),
        USER((short) 3, "user"),
        TABLE_ID((short) 4, "tableId"),
        AGE((short) 5, "age"),
        IDLE_TIME((short) 6, "idleTime"),
        TYPE((short) 7, "type"),
        STATE((short) 8, "state"),
        EXTENT((short) 9, "extent"),
        COLUMNS((short) 10, "columns"),
        SSI_LIST((short) 11, "ssiList"),
        SSIO((short) 12, "ssio"),
        AUTHORIZATIONS((short) 13, "authorizations"),
        SCAN_ID((short) 14, "scanId"),
        CLASS_LOADER_CONTEXT((short) 15, "classLoaderContext");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 2:
                    return CLIENT;
                case 3:
                    return USER;
                case 4:
                    return TABLE_ID;
                case 5:
                    return AGE;
                case 6:
                    return IDLE_TIME;
                case 7:
                    return TYPE;
                case 8:
                    return STATE;
                case 9:
                    return EXTENT;
                case 10:
                    return COLUMNS;
                case 11:
                    return SSI_LIST;
                case 12:
                    return SSIO;
                case 13:
                    return AUTHORIZATIONS;
                case 14:
                    return SCAN_ID;
                case 15:
                    return CLASS_LOADER_CONTEXT;
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

    private static final int __AGE_ISSET_ID = 0;

    private static final int __IDLETIME_ISSET_ID = 1;

    private static final int __SCANID_ISSET_ID = 2;

    private byte __isset_bitfield = 0;

    private static final _Fields[] optionals = { _Fields.SCAN_ID };

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.CLIENT, new org.apache.thrift.meta_data.FieldMetaData("client", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.USER, new org.apache.thrift.meta_data.FieldMetaData("user", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.TABLE_ID, new org.apache.thrift.meta_data.FieldMetaData("tableId", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.AGE, new org.apache.thrift.meta_data.FieldMetaData("age", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.IDLE_TIME, new org.apache.thrift.meta_data.FieldMetaData("idleTime", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.TYPE, new org.apache.thrift.meta_data.FieldMetaData("type", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, ScanType.class)));
        tmpMap.put(_Fields.STATE, new org.apache.thrift.meta_data.FieldMetaData("state", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, ScanState.class)));
        tmpMap.put(_Fields.EXTENT, new org.apache.thrift.meta_data.FieldMetaData("extent", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.data.thrift.TKeyExtent.class)));
        tmpMap.put(_Fields.COLUMNS, new org.apache.thrift.meta_data.FieldMetaData("columns", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.data.thrift.TColumn.class))));
        tmpMap.put(_Fields.SSI_LIST, new org.apache.thrift.meta_data.FieldMetaData("ssiList", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.data.thrift.IterInfo.class))));
        tmpMap.put(_Fields.SSIO, new org.apache.thrift.meta_data.FieldMetaData("ssio", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)))));
        tmpMap.put(_Fields.AUTHORIZATIONS, new org.apache.thrift.meta_data.FieldMetaData("authorizations", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true))));
        tmpMap.put(_Fields.SCAN_ID, new org.apache.thrift.meta_data.FieldMetaData("scanId", org.apache.thrift.TFieldRequirementType.OPTIONAL, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.CLASS_LOADER_CONTEXT, new org.apache.thrift.meta_data.FieldMetaData("classLoaderContext", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ActiveScan.class, metaDataMap);
    }

    public ActiveScan() {
    }

    public ActiveScan(String client, String user, String tableId, long age, long idleTime, ScanType type, ScanState state, org.apache.accumulo.core.data.thrift.TKeyExtent extent, List<org.apache.accumulo.core.data.thrift.TColumn> columns, List<org.apache.accumulo.core.data.thrift.IterInfo> ssiList, Map<String, Map<String, String>> ssio, List<ByteBuffer> authorizations, String classLoaderContext) {
        this();
        this.client = client;
        this.user = user;
        this.tableId = tableId;
        this.age = age;
        setAgeIsSet(true);
        this.idleTime = idleTime;
        setIdleTimeIsSet(true);
        this.type = type;
        this.state = state;
        this.extent = extent;
        this.columns = columns;
        this.ssiList = ssiList;
        this.ssio = ssio;
        this.authorizations = authorizations;
        this.classLoaderContext = classLoaderContext;
    }

    public ActiveScan(ActiveScan other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetClient()) {
            this.client = other.client;
        }
        if (other.isSetUser()) {
            this.user = other.user;
        }
        if (other.isSetTableId()) {
            this.tableId = other.tableId;
        }
        this.age = other.age;
        this.idleTime = other.idleTime;
        if (other.isSetType()) {
            this.type = other.type;
        }
        if (other.isSetState()) {
            this.state = other.state;
        }
        if (other.isSetExtent()) {
            this.extent = new org.apache.accumulo.core.data.thrift.TKeyExtent(other.extent);
        }
        if (other.isSetColumns()) {
            List<org.apache.accumulo.core.data.thrift.TColumn> __this__columns = new ArrayList<org.apache.accumulo.core.data.thrift.TColumn>(other.columns.size());
            for (org.apache.accumulo.core.data.thrift.TColumn other_element : other.columns) {
                __this__columns.add(new org.apache.accumulo.core.data.thrift.TColumn(other_element));
            }
            this.columns = __this__columns;
        }
        if (other.isSetSsiList()) {
            List<org.apache.accumulo.core.data.thrift.IterInfo> __this__ssiList = new ArrayList<org.apache.accumulo.core.data.thrift.IterInfo>(other.ssiList.size());
            for (org.apache.accumulo.core.data.thrift.IterInfo other_element : other.ssiList) {
                __this__ssiList.add(new org.apache.accumulo.core.data.thrift.IterInfo(other_element));
            }
            this.ssiList = __this__ssiList;
        }
        if (other.isSetSsio()) {
            Map<String, Map<String, String>> __this__ssio = new HashMap<String, Map<String, String>>(other.ssio.size());
            for (Map.Entry<String, Map<String, String>> other_element : other.ssio.entrySet()) {
                String other_element_key = other_element.getKey();
                Map<String, String> other_element_value = other_element.getValue();
                String __this__ssio_copy_key = other_element_key;
                Map<String, String> __this__ssio_copy_value = new HashMap<String, String>(other_element_value);
                __this__ssio.put(__this__ssio_copy_key, __this__ssio_copy_value);
            }
            this.ssio = __this__ssio;
        }
        if (other.isSetAuthorizations()) {
            List<ByteBuffer> __this__authorizations = new ArrayList<ByteBuffer>(other.authorizations);
            this.authorizations = __this__authorizations;
        }
        this.scanId = other.scanId;
        if (other.isSetClassLoaderContext()) {
            this.classLoaderContext = other.classLoaderContext;
        }
    }

    public ActiveScan deepCopy() {
        return new ActiveScan(this);
    }

    @Override
    public void clear() {
        this.client = null;
        this.user = null;
        this.tableId = null;
        setAgeIsSet(false);
        this.age = 0;
        setIdleTimeIsSet(false);
        this.idleTime = 0;
        this.type = null;
        this.state = null;
        this.extent = null;
        this.columns = null;
        this.ssiList = null;
        this.ssio = null;
        this.authorizations = null;
        setScanIdIsSet(false);
        this.scanId = 0;
        this.classLoaderContext = null;
    }

    public String getClient() {
        return this.client;
    }

    public ActiveScan setClient(String client) {
        this.client = client;
        return this;
    }

    public void unsetClient() {
        this.client = null;
    }

    public boolean isSetClient() {
        return this.client != null;
    }

    public void setClientIsSet(boolean value) {
        if (!value) {
            this.client = null;
        }
    }

    public String getUser() {
        return this.user;
    }

    public ActiveScan setUser(String user) {
        this.user = user;
        return this;
    }

    public void unsetUser() {
        this.user = null;
    }

    public boolean isSetUser() {
        return this.user != null;
    }

    public void setUserIsSet(boolean value) {
        if (!value) {
            this.user = null;
        }
    }

    public String getTableId() {
        return this.tableId;
    }

    public ActiveScan setTableId(String tableId) {
        this.tableId = tableId;
        return this;
    }

    public void unsetTableId() {
        this.tableId = null;
    }

    public boolean isSetTableId() {
        return this.tableId != null;
    }

    public void setTableIdIsSet(boolean value) {
        if (!value) {
            this.tableId = null;
        }
    }

    public long getAge() {
        return this.age;
    }

    public ActiveScan setAge(long age) {
        this.age = age;
        setAgeIsSet(true);
        return this;
    }

    public void unsetAge() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __AGE_ISSET_ID);
    }

    public boolean isSetAge() {
        return EncodingUtils.testBit(__isset_bitfield, __AGE_ISSET_ID);
    }

    public void setAgeIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __AGE_ISSET_ID, value);
    }

    public long getIdleTime() {
        return this.idleTime;
    }

    public ActiveScan setIdleTime(long idleTime) {
        this.idleTime = idleTime;
        setIdleTimeIsSet(true);
        return this;
    }

    public void unsetIdleTime() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __IDLETIME_ISSET_ID);
    }

    public boolean isSetIdleTime() {
        return EncodingUtils.testBit(__isset_bitfield, __IDLETIME_ISSET_ID);
    }

    public void setIdleTimeIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __IDLETIME_ISSET_ID, value);
    }

    public ScanType getType() {
        return this.type;
    }

    public ActiveScan setType(ScanType type) {
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

    public ScanState getState() {
        return this.state;
    }

    public ActiveScan setState(ScanState state) {
        this.state = state;
        return this;
    }

    public void unsetState() {
        this.state = null;
    }

    public boolean isSetState() {
        return this.state != null;
    }

    public void setStateIsSet(boolean value) {
        if (!value) {
            this.state = null;
        }
    }

    public org.apache.accumulo.core.data.thrift.TKeyExtent getExtent() {
        return this.extent;
    }

    public ActiveScan setExtent(org.apache.accumulo.core.data.thrift.TKeyExtent extent) {
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

    public int getColumnsSize() {
        return (this.columns == null) ? 0 : this.columns.size();
    }

    public java.util.Iterator<org.apache.accumulo.core.data.thrift.TColumn> getColumnsIterator() {
        return (this.columns == null) ? null : this.columns.iterator();
    }

    public void addToColumns(org.apache.accumulo.core.data.thrift.TColumn elem) {
        if (this.columns == null) {
            this.columns = new ArrayList<org.apache.accumulo.core.data.thrift.TColumn>();
        }
        this.columns.add(elem);
    }

    public List<org.apache.accumulo.core.data.thrift.TColumn> getColumns() {
        return this.columns;
    }

    public ActiveScan setColumns(List<org.apache.accumulo.core.data.thrift.TColumn> columns) {
        this.columns = columns;
        return this;
    }

    public void unsetColumns() {
        this.columns = null;
    }

    public boolean isSetColumns() {
        return this.columns != null;
    }

    public void setColumnsIsSet(boolean value) {
        if (!value) {
            this.columns = null;
        }
    }

    public int getSsiListSize() {
        return (this.ssiList == null) ? 0 : this.ssiList.size();
    }

    public java.util.Iterator<org.apache.accumulo.core.data.thrift.IterInfo> getSsiListIterator() {
        return (this.ssiList == null) ? null : this.ssiList.iterator();
    }

    public void addToSsiList(org.apache.accumulo.core.data.thrift.IterInfo elem) {
        if (this.ssiList == null) {
            this.ssiList = new ArrayList<org.apache.accumulo.core.data.thrift.IterInfo>();
        }
        this.ssiList.add(elem);
    }

    public List<org.apache.accumulo.core.data.thrift.IterInfo> getSsiList() {
        return this.ssiList;
    }

    public ActiveScan setSsiList(List<org.apache.accumulo.core.data.thrift.IterInfo> ssiList) {
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

    public void putToSsio(String key, Map<String, String> val) {
        if (this.ssio == null) {
            this.ssio = new HashMap<String, Map<String, String>>();
        }
        this.ssio.put(key, val);
    }

    public Map<String, Map<String, String>> getSsio() {
        return this.ssio;
    }

    public ActiveScan setSsio(Map<String, Map<String, String>> ssio) {
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

    public int getAuthorizationsSize() {
        return (this.authorizations == null) ? 0 : this.authorizations.size();
    }

    public java.util.Iterator<ByteBuffer> getAuthorizationsIterator() {
        return (this.authorizations == null) ? null : this.authorizations.iterator();
    }

    public void addToAuthorizations(ByteBuffer elem) {
        if (this.authorizations == null) {
            this.authorizations = new ArrayList<ByteBuffer>();
        }
        this.authorizations.add(elem);
    }

    public List<ByteBuffer> getAuthorizations() {
        return this.authorizations;
    }

    public ActiveScan setAuthorizations(List<ByteBuffer> authorizations) {
        this.authorizations = authorizations;
        return this;
    }

    public void unsetAuthorizations() {
        this.authorizations = null;
    }

    public boolean isSetAuthorizations() {
        return this.authorizations != null;
    }

    public void setAuthorizationsIsSet(boolean value) {
        if (!value) {
            this.authorizations = null;
        }
    }

    public long getScanId() {
        return this.scanId;
    }

    public ActiveScan setScanId(long scanId) {
        this.scanId = scanId;
        setScanIdIsSet(true);
        return this;
    }

    public void unsetScanId() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __SCANID_ISSET_ID);
    }

    public boolean isSetScanId() {
        return EncodingUtils.testBit(__isset_bitfield, __SCANID_ISSET_ID);
    }

    public void setScanIdIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __SCANID_ISSET_ID, value);
    }

    public String getClassLoaderContext() {
        return this.classLoaderContext;
    }

    public ActiveScan setClassLoaderContext(String classLoaderContext) {
        this.classLoaderContext = classLoaderContext;
        return this;
    }

    public void unsetClassLoaderContext() {
        this.classLoaderContext = null;
    }

    public boolean isSetClassLoaderContext() {
        return this.classLoaderContext != null;
    }

    public void setClassLoaderContextIsSet(boolean value) {
        if (!value) {
            this.classLoaderContext = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case CLIENT:
                if (value == null) {
                    unsetClient();
                } else {
                    setClient((String) value);
                }
                break;
            case USER:
                if (value == null) {
                    unsetUser();
                } else {
                    setUser((String) value);
                }
                break;
            case TABLE_ID:
                if (value == null) {
                    unsetTableId();
                } else {
                    setTableId((String) value);
                }
                break;
            case AGE:
                if (value == null) {
                    unsetAge();
                } else {
                    setAge((Long) value);
                }
                break;
            case IDLE_TIME:
                if (value == null) {
                    unsetIdleTime();
                } else {
                    setIdleTime((Long) value);
                }
                break;
            case TYPE:
                if (value == null) {
                    unsetType();
                } else {
                    setType((ScanType) value);
                }
                break;
            case STATE:
                if (value == null) {
                    unsetState();
                } else {
                    setState((ScanState) value);
                }
                break;
            case EXTENT:
                if (value == null) {
                    unsetExtent();
                } else {
                    setExtent((org.apache.accumulo.core.data.thrift.TKeyExtent) value);
                }
                break;
            case COLUMNS:
                if (value == null) {
                    unsetColumns();
                } else {
                    setColumns((List<org.apache.accumulo.core.data.thrift.TColumn>) value);
                }
                break;
            case SSI_LIST:
                if (value == null) {
                    unsetSsiList();
                } else {
                    setSsiList((List<org.apache.accumulo.core.data.thrift.IterInfo>) value);
                }
                break;
            case SSIO:
                if (value == null) {
                    unsetSsio();
                } else {
                    setSsio((Map<String, Map<String, String>>) value);
                }
                break;
            case AUTHORIZATIONS:
                if (value == null) {
                    unsetAuthorizations();
                } else {
                    setAuthorizations((List<ByteBuffer>) value);
                }
                break;
            case SCAN_ID:
                if (value == null) {
                    unsetScanId();
                } else {
                    setScanId((Long) value);
                }
                break;
            case CLASS_LOADER_CONTEXT:
                if (value == null) {
                    unsetClassLoaderContext();
                } else {
                    setClassLoaderContext((String) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case CLIENT:
                return getClient();
            case USER:
                return getUser();
            case TABLE_ID:
                return getTableId();
            case AGE:
                return getAge();
            case IDLE_TIME:
                return getIdleTime();
            case TYPE:
                return getType();
            case STATE:
                return getState();
            case EXTENT:
                return getExtent();
            case COLUMNS:
                return getColumns();
            case SSI_LIST:
                return getSsiList();
            case SSIO:
                return getSsio();
            case AUTHORIZATIONS:
                return getAuthorizations();
            case SCAN_ID:
                return getScanId();
            case CLASS_LOADER_CONTEXT:
                return getClassLoaderContext();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case CLIENT:
                return isSetClient();
            case USER:
                return isSetUser();
            case TABLE_ID:
                return isSetTableId();
            case AGE:
                return isSetAge();
            case IDLE_TIME:
                return isSetIdleTime();
            case TYPE:
                return isSetType();
            case STATE:
                return isSetState();
            case EXTENT:
                return isSetExtent();
            case COLUMNS:
                return isSetColumns();
            case SSI_LIST:
                return isSetSsiList();
            case SSIO:
                return isSetSsio();
            case AUTHORIZATIONS:
                return isSetAuthorizations();
            case SCAN_ID:
                return isSetScanId();
            case CLASS_LOADER_CONTEXT:
                return isSetClassLoaderContext();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof ActiveScan)
            return this.equals((ActiveScan) that);
        return false;
    }

    public boolean equals(ActiveScan that) {
        if (that == null)
            return false;
        boolean this_present_client = true && this.isSetClient();
        boolean that_present_client = true && that.isSetClient();
        if (this_present_client || that_present_client) {
            if (!(this_present_client && that_present_client))
                return false;
            if (!this.client.equals(that.client))
                return false;
        }
        boolean this_present_user = true && this.isSetUser();
        boolean that_present_user = true && that.isSetUser();
        if (this_present_user || that_present_user) {
            if (!(this_present_user && that_present_user))
                return false;
            if (!this.user.equals(that.user))
                return false;
        }
        boolean this_present_tableId = true && this.isSetTableId();
        boolean that_present_tableId = true && that.isSetTableId();
        if (this_present_tableId || that_present_tableId) {
            if (!(this_present_tableId && that_present_tableId))
                return false;
            if (!this.tableId.equals(that.tableId))
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
        boolean this_present_idleTime = true;
        boolean that_present_idleTime = true;
        if (this_present_idleTime || that_present_idleTime) {
            if (!(this_present_idleTime && that_present_idleTime))
                return false;
            if (this.idleTime != that.idleTime)
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
        boolean this_present_state = true && this.isSetState();
        boolean that_present_state = true && that.isSetState();
        if (this_present_state || that_present_state) {
            if (!(this_present_state && that_present_state))
                return false;
            if (!this.state.equals(that.state))
                return false;
        }
        boolean this_present_extent = true && this.isSetExtent();
        boolean that_present_extent = true && that.isSetExtent();
        if (this_present_extent || that_present_extent) {
            if (!(this_present_extent && that_present_extent))
                return false;
            if (!this.extent.equals(that.extent))
                return false;
        }
        boolean this_present_columns = true && this.isSetColumns();
        boolean that_present_columns = true && that.isSetColumns();
        if (this_present_columns || that_present_columns) {
            if (!(this_present_columns && that_present_columns))
                return false;
            if (!this.columns.equals(that.columns))
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
        boolean this_present_authorizations = true && this.isSetAuthorizations();
        boolean that_present_authorizations = true && that.isSetAuthorizations();
        if (this_present_authorizations || that_present_authorizations) {
            if (!(this_present_authorizations && that_present_authorizations))
                return false;
            if (!this.authorizations.equals(that.authorizations))
                return false;
        }
        boolean this_present_scanId = true && this.isSetScanId();
        boolean that_present_scanId = true && that.isSetScanId();
        if (this_present_scanId || that_present_scanId) {
            if (!(this_present_scanId && that_present_scanId))
                return false;
            if (this.scanId != that.scanId)
                return false;
        }
        boolean this_present_classLoaderContext = true && this.isSetClassLoaderContext();
        boolean that_present_classLoaderContext = true && that.isSetClassLoaderContext();
        if (this_present_classLoaderContext || that_present_classLoaderContext) {
            if (!(this_present_classLoaderContext && that_present_classLoaderContext))
                return false;
            if (!this.classLoaderContext.equals(that.classLoaderContext))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        List<Object> list = new ArrayList<Object>();
        boolean present_client = true && (isSetClient());
        list.add(present_client);
        if (present_client)
            list.add(client);
        boolean present_user = true && (isSetUser());
        list.add(present_user);
        if (present_user)
            list.add(user);
        boolean present_tableId = true && (isSetTableId());
        list.add(present_tableId);
        if (present_tableId)
            list.add(tableId);
        boolean present_age = true;
        list.add(present_age);
        if (present_age)
            list.add(age);
        boolean present_idleTime = true;
        list.add(present_idleTime);
        if (present_idleTime)
            list.add(idleTime);
        boolean present_type = true && (isSetType());
        list.add(present_type);
        if (present_type)
            list.add(type.getValue());
        boolean present_state = true && (isSetState());
        list.add(present_state);
        if (present_state)
            list.add(state.getValue());
        boolean present_extent = true && (isSetExtent());
        list.add(present_extent);
        if (present_extent)
            list.add(extent);
        boolean present_columns = true && (isSetColumns());
        list.add(present_columns);
        if (present_columns)
            list.add(columns);
        boolean present_ssiList = true && (isSetSsiList());
        list.add(present_ssiList);
        if (present_ssiList)
            list.add(ssiList);
        boolean present_ssio = true && (isSetSsio());
        list.add(present_ssio);
        if (present_ssio)
            list.add(ssio);
        boolean present_authorizations = true && (isSetAuthorizations());
        list.add(present_authorizations);
        if (present_authorizations)
            list.add(authorizations);
        boolean present_scanId = true && (isSetScanId());
        list.add(present_scanId);
        if (present_scanId)
            list.add(scanId);
        boolean present_classLoaderContext = true && (isSetClassLoaderContext());
        list.add(present_classLoaderContext);
        if (present_classLoaderContext)
            list.add(classLoaderContext);
        return list.hashCode();
    }

    @Override
    public int compareTo(ActiveScan other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = Boolean.valueOf(isSetClient()).compareTo(other.isSetClient());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetClient()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.client, other.client);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetUser()).compareTo(other.isSetUser());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetUser()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.user, other.user);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetTableId()).compareTo(other.isSetTableId());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTableId()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tableId, other.tableId);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetAge()).compareTo(other.isSetAge());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetAge()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.age, other.age);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetIdleTime()).compareTo(other.isSetIdleTime());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetIdleTime()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.idleTime, other.idleTime);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetType()).compareTo(other.isSetType());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetType()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.type, other.type);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetState()).compareTo(other.isSetState());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetState()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.state, other.state);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetExtent()).compareTo(other.isSetExtent());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetExtent()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.extent, other.extent);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetColumns()).compareTo(other.isSetColumns());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetColumns()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.columns, other.columns);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetSsiList()).compareTo(other.isSetSsiList());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetSsiList()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ssiList, other.ssiList);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetSsio()).compareTo(other.isSetSsio());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetSsio()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ssio, other.ssio);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetAuthorizations()).compareTo(other.isSetAuthorizations());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetAuthorizations()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.authorizations, other.authorizations);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetScanId()).compareTo(other.isSetScanId());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetScanId()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.scanId, other.scanId);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetClassLoaderContext()).compareTo(other.isSetClassLoaderContext());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetClassLoaderContext()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.classLoaderContext, other.classLoaderContext);
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
        StringBuilder sb = new StringBuilder("ActiveScan(");
        boolean first = true;
        sb.append("client:");
        if (this.client == null) {
            sb.append("null");
        } else {
            sb.append(this.client);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("user:");
        if (this.user == null) {
            sb.append("null");
        } else {
            sb.append(this.user);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("tableId:");
        if (this.tableId == null) {
            sb.append("null");
        } else {
            sb.append(this.tableId);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("age:");
        sb.append(this.age);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("idleTime:");
        sb.append(this.idleTime);
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
        sb.append("state:");
        if (this.state == null) {
            sb.append("null");
        } else {
            sb.append(this.state);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("extent:");
        if (this.extent == null) {
            sb.append("null");
        } else {
            sb.append(this.extent);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("columns:");
        if (this.columns == null) {
            sb.append("null");
        } else {
            sb.append(this.columns);
        }
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
        if (!first)
            sb.append(", ");
        sb.append("authorizations:");
        if (this.authorizations == null) {
            sb.append("null");
        } else {
            org.apache.thrift.TBaseHelper.toString(this.authorizations, sb);
        }
        first = false;
        if (isSetScanId()) {
            if (!first)
                sb.append(", ");
            sb.append("scanId:");
            sb.append(this.scanId);
            first = false;
        }
        if (!first)
            sb.append(", ");
        sb.append("classLoaderContext:");
        if (this.classLoaderContext == null) {
            sb.append("null");
        } else {
            sb.append(this.classLoaderContext);
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

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        try {
            __isset_bitfield = 0;
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class ActiveScanStandardSchemeFactory implements SchemeFactory {

        public ActiveScanStandardScheme getScheme() {
            return new ActiveScanStandardScheme();
        }
    }

    private static class ActiveScanStandardScheme extends StandardScheme<ActiveScan> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, ActiveScan struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch(schemeField.id) {
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.client = iprot.readString();
                            struct.setClientIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.user = iprot.readString();
                            struct.setUserIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.tableId = iprot.readString();
                            struct.setTableIdIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 5:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.age = iprot.readI64();
                            struct.setAgeIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 6:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.idleTime = iprot.readI64();
                            struct.setIdleTimeIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 7:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.type = org.apache.accumulo.core.tabletserver.thrift.ScanType.findByValue(iprot.readI32());
                            struct.setTypeIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 8:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.state = org.apache.accumulo.core.tabletserver.thrift.ScanState.findByValue(iprot.readI32());
                            struct.setStateIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 9:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.extent = new org.apache.accumulo.core.data.thrift.TKeyExtent();
                            struct.extent.read(iprot);
                            struct.setExtentIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 10:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list8 = iprot.readListBegin();
                                struct.columns = new ArrayList<org.apache.accumulo.core.data.thrift.TColumn>(_list8.size);
                                org.apache.accumulo.core.data.thrift.TColumn _elem9;
                                for (int _i10 = 0; _i10 < _list8.size; ++_i10) {
                                    _elem9 = new org.apache.accumulo.core.data.thrift.TColumn();
                                    _elem9.read(iprot);
                                    struct.columns.add(_elem9);
                                }
                                iprot.readListEnd();
                            }
                            struct.setColumnsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 11:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list11 = iprot.readListBegin();
                                struct.ssiList = new ArrayList<org.apache.accumulo.core.data.thrift.IterInfo>(_list11.size);
                                org.apache.accumulo.core.data.thrift.IterInfo _elem12;
                                for (int _i13 = 0; _i13 < _list11.size; ++_i13) {
                                    _elem12 = new org.apache.accumulo.core.data.thrift.IterInfo();
                                    _elem12.read(iprot);
                                    struct.ssiList.add(_elem12);
                                }
                                iprot.readListEnd();
                            }
                            struct.setSsiListIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 12:
                        if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
                            {
                                org.apache.thrift.protocol.TMap _map14 = iprot.readMapBegin();
                                struct.ssio = new HashMap<String, Map<String, String>>(2 * _map14.size);
                                String _key15;
                                Map<String, String> _val16;
                                for (int _i17 = 0; _i17 < _map14.size; ++_i17) {
                                    _key15 = iprot.readString();
                                    {
                                        org.apache.thrift.protocol.TMap _map18 = iprot.readMapBegin();
                                        _val16 = new HashMap<String, String>(2 * _map18.size);
                                        String _key19;
                                        String _val20;
                                        for (int _i21 = 0; _i21 < _map18.size; ++_i21) {
                                            _key19 = iprot.readString();
                                            _val20 = iprot.readString();
                                            _val16.put(_key19, _val20);
                                        }
                                        iprot.readMapEnd();
                                    }
                                    struct.ssio.put(_key15, _val16);
                                }
                                iprot.readMapEnd();
                            }
                            struct.setSsioIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 13:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list22 = iprot.readListBegin();
                                struct.authorizations = new ArrayList<ByteBuffer>(_list22.size);
                                ByteBuffer _elem23;
                                for (int _i24 = 0; _i24 < _list22.size; ++_i24) {
                                    _elem23 = iprot.readBinary();
                                    struct.authorizations.add(_elem23);
                                }
                                iprot.readListEnd();
                            }
                            struct.setAuthorizationsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 14:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.scanId = iprot.readI64();
                            struct.setScanIdIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 15:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.classLoaderContext = iprot.readString();
                            struct.setClassLoaderContextIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, ActiveScan struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.client != null) {
                oprot.writeFieldBegin(CLIENT_FIELD_DESC);
                oprot.writeString(struct.client);
                oprot.writeFieldEnd();
            }
            if (struct.user != null) {
                oprot.writeFieldBegin(USER_FIELD_DESC);
                oprot.writeString(struct.user);
                oprot.writeFieldEnd();
            }
            if (struct.tableId != null) {
                oprot.writeFieldBegin(TABLE_ID_FIELD_DESC);
                oprot.writeString(struct.tableId);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(AGE_FIELD_DESC);
            oprot.writeI64(struct.age);
            oprot.writeFieldEnd();
            oprot.writeFieldBegin(IDLE_TIME_FIELD_DESC);
            oprot.writeI64(struct.idleTime);
            oprot.writeFieldEnd();
            if (struct.type != null) {
                oprot.writeFieldBegin(TYPE_FIELD_DESC);
                oprot.writeI32(struct.type.getValue());
                oprot.writeFieldEnd();
            }
            if (struct.state != null) {
                oprot.writeFieldBegin(STATE_FIELD_DESC);
                oprot.writeI32(struct.state.getValue());
                oprot.writeFieldEnd();
            }
            if (struct.extent != null) {
                oprot.writeFieldBegin(EXTENT_FIELD_DESC);
                struct.extent.write(oprot);
                oprot.writeFieldEnd();
            }
            if (struct.columns != null) {
                oprot.writeFieldBegin(COLUMNS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.columns.size()));
                    for (org.apache.accumulo.core.data.thrift.TColumn _iter25 : struct.columns) {
                        _iter25.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.ssiList != null) {
                oprot.writeFieldBegin(SSI_LIST_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.ssiList.size()));
                    for (org.apache.accumulo.core.data.thrift.IterInfo _iter26 : struct.ssiList) {
                        _iter26.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.ssio != null) {
                oprot.writeFieldBegin(SSIO_FIELD_DESC);
                {
                    oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.MAP, struct.ssio.size()));
                    for (Map.Entry<String, Map<String, String>> _iter27 : struct.ssio.entrySet()) {
                        oprot.writeString(_iter27.getKey());
                        {
                            oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, _iter27.getValue().size()));
                            for (Map.Entry<String, String> _iter28 : _iter27.getValue().entrySet()) {
                                oprot.writeString(_iter28.getKey());
                                oprot.writeString(_iter28.getValue());
                            }
                            oprot.writeMapEnd();
                        }
                    }
                    oprot.writeMapEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.authorizations != null) {
                oprot.writeFieldBegin(AUTHORIZATIONS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.authorizations.size()));
                    for (ByteBuffer _iter29 : struct.authorizations) {
                        oprot.writeBinary(_iter29);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.isSetScanId()) {
                oprot.writeFieldBegin(SCAN_ID_FIELD_DESC);
                oprot.writeI64(struct.scanId);
                oprot.writeFieldEnd();
            }
            if (struct.classLoaderContext != null) {
                oprot.writeFieldBegin(CLASS_LOADER_CONTEXT_FIELD_DESC);
                oprot.writeString(struct.classLoaderContext);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class ActiveScanTupleSchemeFactory implements SchemeFactory {

        public ActiveScanTupleScheme getScheme() {
            return new ActiveScanTupleScheme();
        }
    }

    private static class ActiveScanTupleScheme extends TupleScheme<ActiveScan> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, ActiveScan struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetClient()) {
                optionals.set(0);
            }
            if (struct.isSetUser()) {
                optionals.set(1);
            }
            if (struct.isSetTableId()) {
                optionals.set(2);
            }
            if (struct.isSetAge()) {
                optionals.set(3);
            }
            if (struct.isSetIdleTime()) {
                optionals.set(4);
            }
            if (struct.isSetType()) {
                optionals.set(5);
            }
            if (struct.isSetState()) {
                optionals.set(6);
            }
            if (struct.isSetExtent()) {
                optionals.set(7);
            }
            if (struct.isSetColumns()) {
                optionals.set(8);
            }
            if (struct.isSetSsiList()) {
                optionals.set(9);
            }
            if (struct.isSetSsio()) {
                optionals.set(10);
            }
            if (struct.isSetAuthorizations()) {
                optionals.set(11);
            }
            if (struct.isSetScanId()) {
                optionals.set(12);
            }
            if (struct.isSetClassLoaderContext()) {
                optionals.set(13);
            }
            oprot.writeBitSet(optionals, 14);
            if (struct.isSetClient()) {
                oprot.writeString(struct.client);
            }
            if (struct.isSetUser()) {
                oprot.writeString(struct.user);
            }
            if (struct.isSetTableId()) {
                oprot.writeString(struct.tableId);
            }
            if (struct.isSetAge()) {
                oprot.writeI64(struct.age);
            }
            if (struct.isSetIdleTime()) {
                oprot.writeI64(struct.idleTime);
            }
            if (struct.isSetType()) {
                oprot.writeI32(struct.type.getValue());
            }
            if (struct.isSetState()) {
                oprot.writeI32(struct.state.getValue());
            }
            if (struct.isSetExtent()) {
                struct.extent.write(oprot);
            }
            if (struct.isSetColumns()) {
                {
                    oprot.writeI32(struct.columns.size());
                    for (org.apache.accumulo.core.data.thrift.TColumn _iter30 : struct.columns) {
                        _iter30.write(oprot);
                    }
                }
            }
            if (struct.isSetSsiList()) {
                {
                    oprot.writeI32(struct.ssiList.size());
                    for (org.apache.accumulo.core.data.thrift.IterInfo _iter31 : struct.ssiList) {
                        _iter31.write(oprot);
                    }
                }
            }
            if (struct.isSetSsio()) {
                {
                    oprot.writeI32(struct.ssio.size());
                    for (Map.Entry<String, Map<String, String>> _iter32 : struct.ssio.entrySet()) {
                        oprot.writeString(_iter32.getKey());
                        {
                            oprot.writeI32(_iter32.getValue().size());
                            for (Map.Entry<String, String> _iter33 : _iter32.getValue().entrySet()) {
                                oprot.writeString(_iter33.getKey());
                                oprot.writeString(_iter33.getValue());
                            }
                        }
                    }
                }
            }
            if (struct.isSetAuthorizations()) {
                {
                    oprot.writeI32(struct.authorizations.size());
                    for (ByteBuffer _iter34 : struct.authorizations) {
                        oprot.writeBinary(_iter34);
                    }
                }
            }
            if (struct.isSetScanId()) {
                oprot.writeI64(struct.scanId);
            }
            if (struct.isSetClassLoaderContext()) {
                oprot.writeString(struct.classLoaderContext);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, ActiveScan struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(14);
            if (incoming.get(0)) {
                struct.client = iprot.readString();
                struct.setClientIsSet(true);
            }
            if (incoming.get(1)) {
                struct.user = iprot.readString();
                struct.setUserIsSet(true);
            }
            if (incoming.get(2)) {
                struct.tableId = iprot.readString();
                struct.setTableIdIsSet(true);
            }
            if (incoming.get(3)) {
                struct.age = iprot.readI64();
                struct.setAgeIsSet(true);
            }
            if (incoming.get(4)) {
                struct.idleTime = iprot.readI64();
                struct.setIdleTimeIsSet(true);
            }
            if (incoming.get(5)) {
                struct.type = org.apache.accumulo.core.tabletserver.thrift.ScanType.findByValue(iprot.readI32());
                struct.setTypeIsSet(true);
            }
            if (incoming.get(6)) {
                struct.state = org.apache.accumulo.core.tabletserver.thrift.ScanState.findByValue(iprot.readI32());
                struct.setStateIsSet(true);
            }
            if (incoming.get(7)) {
                struct.extent = new org.apache.accumulo.core.data.thrift.TKeyExtent();
                struct.extent.read(iprot);
                struct.setExtentIsSet(true);
            }
            if (incoming.get(8)) {
                {
                    org.apache.thrift.protocol.TList _list35 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.columns = new ArrayList<org.apache.accumulo.core.data.thrift.TColumn>(_list35.size);
                    org.apache.accumulo.core.data.thrift.TColumn _elem36;
                    for (int _i37 = 0; _i37 < _list35.size; ++_i37) {
                        _elem36 = new org.apache.accumulo.core.data.thrift.TColumn();
                        _elem36.read(iprot);
                        struct.columns.add(_elem36);
                    }
                }
                struct.setColumnsIsSet(true);
            }
            if (incoming.get(9)) {
                {
                    org.apache.thrift.protocol.TList _list38 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.ssiList = new ArrayList<org.apache.accumulo.core.data.thrift.IterInfo>(_list38.size);
                    org.apache.accumulo.core.data.thrift.IterInfo _elem39;
                    for (int _i40 = 0; _i40 < _list38.size; ++_i40) {
                        _elem39 = new org.apache.accumulo.core.data.thrift.IterInfo();
                        _elem39.read(iprot);
                        struct.ssiList.add(_elem39);
                    }
                }
                struct.setSsiListIsSet(true);
            }
            if (incoming.get(10)) {
                {
                    org.apache.thrift.protocol.TMap _map41 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.MAP, iprot.readI32());
                    struct.ssio = new HashMap<String, Map<String, String>>(2 * _map41.size);
                    String _key42;
                    Map<String, String> _val43;
                    for (int _i44 = 0; _i44 < _map41.size; ++_i44) {
                        _key42 = iprot.readString();
                        {
                            org.apache.thrift.protocol.TMap _map45 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                            _val43 = new HashMap<String, String>(2 * _map45.size);
                            String _key46;
                            String _val47;
                            for (int _i48 = 0; _i48 < _map45.size; ++_i48) {
                                _key46 = iprot.readString();
                                _val47 = iprot.readString();
                                _val43.put(_key46, _val47);
                            }
                        }
                        struct.ssio.put(_key42, _val43);
                    }
                }
                struct.setSsioIsSet(true);
            }
            if (incoming.get(11)) {
                {
                    org.apache.thrift.protocol.TList _list49 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.authorizations = new ArrayList<ByteBuffer>(_list49.size);
                    ByteBuffer _elem50;
                    for (int _i51 = 0; _i51 < _list49.size; ++_i51) {
                        _elem50 = iprot.readBinary();
                        struct.authorizations.add(_elem50);
                    }
                }
                struct.setAuthorizationsIsSet(true);
            }
            if (incoming.get(12)) {
                struct.scanId = iprot.readI64();
                struct.setScanIdIsSet(true);
            }
            if (incoming.get(13)) {
                struct.classLoaderContext = iprot.readString();
                struct.setClassLoaderContextIsSet(true);
            }
        }
    }
}