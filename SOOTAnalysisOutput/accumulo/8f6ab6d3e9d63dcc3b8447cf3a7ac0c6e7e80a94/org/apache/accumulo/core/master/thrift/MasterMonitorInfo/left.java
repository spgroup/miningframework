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
public class MasterMonitorInfo implements org.apache.thrift.TBase<MasterMonitorInfo, MasterMonitorInfo._Fields>, java.io.Serializable, Cloneable, Comparable<MasterMonitorInfo> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MasterMonitorInfo");

    private static final org.apache.thrift.protocol.TField TABLE_MAP_FIELD_DESC = new org.apache.thrift.protocol.TField("tableMap", org.apache.thrift.protocol.TType.MAP, (short) 1);

    private static final org.apache.thrift.protocol.TField T_SERVER_INFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tServerInfo", org.apache.thrift.protocol.TType.LIST, (short) 2);

    private static final org.apache.thrift.protocol.TField BAD_TSERVERS_FIELD_DESC = new org.apache.thrift.protocol.TField("badTServers", org.apache.thrift.protocol.TType.MAP, (short) 3);

    private static final org.apache.thrift.protocol.TField STATE_FIELD_DESC = new org.apache.thrift.protocol.TField("state", org.apache.thrift.protocol.TType.I32, (short) 6);

    private static final org.apache.thrift.protocol.TField GOAL_STATE_FIELD_DESC = new org.apache.thrift.protocol.TField("goalState", org.apache.thrift.protocol.TType.I32, (short) 8);

    private static final org.apache.thrift.protocol.TField UNASSIGNED_TABLETS_FIELD_DESC = new org.apache.thrift.protocol.TField("unassignedTablets", org.apache.thrift.protocol.TType.I32, (short) 7);

    private static final org.apache.thrift.protocol.TField SERVERS_SHUTTING_DOWN_FIELD_DESC = new org.apache.thrift.protocol.TField("serversShuttingDown", org.apache.thrift.protocol.TType.SET, (short) 9);

    private static final org.apache.thrift.protocol.TField DEAD_TABLET_SERVERS_FIELD_DESC = new org.apache.thrift.protocol.TField("deadTabletServers", org.apache.thrift.protocol.TType.LIST, (short) 10);

    private static final org.apache.thrift.protocol.TField BULK_IMPORTS_FIELD_DESC = new org.apache.thrift.protocol.TField("bulkImports", org.apache.thrift.protocol.TType.LIST, (short) 11);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new MasterMonitorInfoStandardSchemeFactory());
        schemes.put(TupleScheme.class, new MasterMonitorInfoTupleSchemeFactory());
    }

    public Map<String, TableInfo> tableMap;

    public List<TabletServerStatus> tServerInfo;

    public Map<String, Byte> badTServers;

    public MasterState state;

    public MasterGoalState goalState;

    public int unassignedTablets;

    public Set<String> serversShuttingDown;

    public List<DeadServer> deadTabletServers;

    public List<BulkImportStatus> bulkImports;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        TABLE_MAP((short) 1, "tableMap"),
        T_SERVER_INFO((short) 2, "tServerInfo"),
        BAD_TSERVERS((short) 3, "badTServers"),
        STATE((short) 6, "state"),
        GOAL_STATE((short) 8, "goalState"),
        UNASSIGNED_TABLETS((short) 7, "unassignedTablets"),
        SERVERS_SHUTTING_DOWN((short) 9, "serversShuttingDown"),
        DEAD_TABLET_SERVERS((short) 10, "deadTabletServers"),
        BULK_IMPORTS((short) 11, "bulkImports");

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
                    return T_SERVER_INFO;
                case 3:
                    return BAD_TSERVERS;
                case 6:
                    return STATE;
                case 8:
                    return GOAL_STATE;
                case 7:
                    return UNASSIGNED_TABLETS;
                case 9:
                    return SERVERS_SHUTTING_DOWN;
                case 10:
                    return DEAD_TABLET_SERVERS;
                case 11:
                    return BULK_IMPORTS;
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

    private static final int __UNASSIGNEDTABLETS_ISSET_ID = 0;

    private byte __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.TABLE_MAP, new org.apache.thrift.meta_data.FieldMetaData("tableMap", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TableInfo.class))));
        tmpMap.put(_Fields.T_SERVER_INFO, new org.apache.thrift.meta_data.FieldMetaData("tServerInfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TabletServerStatus.class))));
        tmpMap.put(_Fields.BAD_TSERVERS, new org.apache.thrift.meta_data.FieldMetaData("badTServers", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BYTE))));
        tmpMap.put(_Fields.STATE, new org.apache.thrift.meta_data.FieldMetaData("state", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, MasterState.class)));
        tmpMap.put(_Fields.GOAL_STATE, new org.apache.thrift.meta_data.FieldMetaData("goalState", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, MasterGoalState.class)));
        tmpMap.put(_Fields.UNASSIGNED_TABLETS, new org.apache.thrift.meta_data.FieldMetaData("unassignedTablets", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.SERVERS_SHUTTING_DOWN, new org.apache.thrift.meta_data.FieldMetaData("serversShuttingDown", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.SetMetaData(org.apache.thrift.protocol.TType.SET, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
        tmpMap.put(_Fields.DEAD_TABLET_SERVERS, new org.apache.thrift.meta_data.FieldMetaData("deadTabletServers", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, DeadServer.class))));
        tmpMap.put(_Fields.BULK_IMPORTS, new org.apache.thrift.meta_data.FieldMetaData("bulkImports", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, BulkImportStatus.class))));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MasterMonitorInfo.class, metaDataMap);
    }

    public MasterMonitorInfo() {
    }

    public MasterMonitorInfo(Map<String, TableInfo> tableMap, List<TabletServerStatus> tServerInfo, Map<String, Byte> badTServers, MasterState state, MasterGoalState goalState, int unassignedTablets, Set<String> serversShuttingDown, List<DeadServer> deadTabletServers, List<BulkImportStatus> bulkImports) {
        this();
        this.tableMap = tableMap;
        this.tServerInfo = tServerInfo;
        this.badTServers = badTServers;
        this.state = state;
        this.goalState = goalState;
        this.unassignedTablets = unassignedTablets;
        setUnassignedTabletsIsSet(true);
        this.serversShuttingDown = serversShuttingDown;
        this.deadTabletServers = deadTabletServers;
        this.bulkImports = bulkImports;
    }

    public MasterMonitorInfo(MasterMonitorInfo other) {
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
        if (other.isSetTServerInfo()) {
            List<TabletServerStatus> __this__tServerInfo = new ArrayList<TabletServerStatus>(other.tServerInfo.size());
            for (TabletServerStatus other_element : other.tServerInfo) {
                __this__tServerInfo.add(new TabletServerStatus(other_element));
            }
            this.tServerInfo = __this__tServerInfo;
        }
        if (other.isSetBadTServers()) {
            Map<String, Byte> __this__badTServers = new HashMap<String, Byte>(other.badTServers);
            this.badTServers = __this__badTServers;
        }
        if (other.isSetState()) {
            this.state = other.state;
        }
        if (other.isSetGoalState()) {
            this.goalState = other.goalState;
        }
        this.unassignedTablets = other.unassignedTablets;
        if (other.isSetServersShuttingDown()) {
            Set<String> __this__serversShuttingDown = new HashSet<String>(other.serversShuttingDown);
            this.serversShuttingDown = __this__serversShuttingDown;
        }
        if (other.isSetDeadTabletServers()) {
            List<DeadServer> __this__deadTabletServers = new ArrayList<DeadServer>(other.deadTabletServers.size());
            for (DeadServer other_element : other.deadTabletServers) {
                __this__deadTabletServers.add(new DeadServer(other_element));
            }
            this.deadTabletServers = __this__deadTabletServers;
        }
        if (other.isSetBulkImports()) {
            List<BulkImportStatus> __this__bulkImports = new ArrayList<BulkImportStatus>(other.bulkImports.size());
            for (BulkImportStatus other_element : other.bulkImports) {
                __this__bulkImports.add(new BulkImportStatus(other_element));
            }
            this.bulkImports = __this__bulkImports;
        }
    }

    public MasterMonitorInfo deepCopy() {
        return new MasterMonitorInfo(this);
    }

    @Override
    public void clear() {
        this.tableMap = null;
        this.tServerInfo = null;
        this.badTServers = null;
        this.state = null;
        this.goalState = null;
        setUnassignedTabletsIsSet(false);
        this.unassignedTablets = 0;
        this.serversShuttingDown = null;
        this.deadTabletServers = null;
        this.bulkImports = null;
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

    public MasterMonitorInfo setTableMap(Map<String, TableInfo> tableMap) {
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

    public int getTServerInfoSize() {
        return (this.tServerInfo == null) ? 0 : this.tServerInfo.size();
    }

    public java.util.Iterator<TabletServerStatus> getTServerInfoIterator() {
        return (this.tServerInfo == null) ? null : this.tServerInfo.iterator();
    }

    public void addToTServerInfo(TabletServerStatus elem) {
        if (this.tServerInfo == null) {
            this.tServerInfo = new ArrayList<TabletServerStatus>();
        }
        this.tServerInfo.add(elem);
    }

    public List<TabletServerStatus> getTServerInfo() {
        return this.tServerInfo;
    }

    public MasterMonitorInfo setTServerInfo(List<TabletServerStatus> tServerInfo) {
        this.tServerInfo = tServerInfo;
        return this;
    }

    public void unsetTServerInfo() {
        this.tServerInfo = null;
    }

    public boolean isSetTServerInfo() {
        return this.tServerInfo != null;
    }

    public void setTServerInfoIsSet(boolean value) {
        if (!value) {
            this.tServerInfo = null;
        }
    }

    public int getBadTServersSize() {
        return (this.badTServers == null) ? 0 : this.badTServers.size();
    }

    public void putToBadTServers(String key, byte val) {
        if (this.badTServers == null) {
            this.badTServers = new HashMap<String, Byte>();
        }
        this.badTServers.put(key, val);
    }

    public Map<String, Byte> getBadTServers() {
        return this.badTServers;
    }

    public MasterMonitorInfo setBadTServers(Map<String, Byte> badTServers) {
        this.badTServers = badTServers;
        return this;
    }

    public void unsetBadTServers() {
        this.badTServers = null;
    }

    public boolean isSetBadTServers() {
        return this.badTServers != null;
    }

    public void setBadTServersIsSet(boolean value) {
        if (!value) {
            this.badTServers = null;
        }
    }

    public MasterState getState() {
        return this.state;
    }

    public MasterMonitorInfo setState(MasterState state) {
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

    public MasterGoalState getGoalState() {
        return this.goalState;
    }

    public MasterMonitorInfo setGoalState(MasterGoalState goalState) {
        this.goalState = goalState;
        return this;
    }

    public void unsetGoalState() {
        this.goalState = null;
    }

    public boolean isSetGoalState() {
        return this.goalState != null;
    }

    public void setGoalStateIsSet(boolean value) {
        if (!value) {
            this.goalState = null;
        }
    }

    public int getUnassignedTablets() {
        return this.unassignedTablets;
    }

    public MasterMonitorInfo setUnassignedTablets(int unassignedTablets) {
        this.unassignedTablets = unassignedTablets;
        setUnassignedTabletsIsSet(true);
        return this;
    }

    public void unsetUnassignedTablets() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __UNASSIGNEDTABLETS_ISSET_ID);
    }

    public boolean isSetUnassignedTablets() {
        return EncodingUtils.testBit(__isset_bitfield, __UNASSIGNEDTABLETS_ISSET_ID);
    }

    public void setUnassignedTabletsIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __UNASSIGNEDTABLETS_ISSET_ID, value);
    }

    public int getServersShuttingDownSize() {
        return (this.serversShuttingDown == null) ? 0 : this.serversShuttingDown.size();
    }

    public java.util.Iterator<String> getServersShuttingDownIterator() {
        return (this.serversShuttingDown == null) ? null : this.serversShuttingDown.iterator();
    }

    public void addToServersShuttingDown(String elem) {
        if (this.serversShuttingDown == null) {
            this.serversShuttingDown = new HashSet<String>();
        }
        this.serversShuttingDown.add(elem);
    }

    public Set<String> getServersShuttingDown() {
        return this.serversShuttingDown;
    }

    public MasterMonitorInfo setServersShuttingDown(Set<String> serversShuttingDown) {
        this.serversShuttingDown = serversShuttingDown;
        return this;
    }

    public void unsetServersShuttingDown() {
        this.serversShuttingDown = null;
    }

    public boolean isSetServersShuttingDown() {
        return this.serversShuttingDown != null;
    }

    public void setServersShuttingDownIsSet(boolean value) {
        if (!value) {
            this.serversShuttingDown = null;
        }
    }

    public int getDeadTabletServersSize() {
        return (this.deadTabletServers == null) ? 0 : this.deadTabletServers.size();
    }

    public java.util.Iterator<DeadServer> getDeadTabletServersIterator() {
        return (this.deadTabletServers == null) ? null : this.deadTabletServers.iterator();
    }

    public void addToDeadTabletServers(DeadServer elem) {
        if (this.deadTabletServers == null) {
            this.deadTabletServers = new ArrayList<DeadServer>();
        }
        this.deadTabletServers.add(elem);
    }

    public List<DeadServer> getDeadTabletServers() {
        return this.deadTabletServers;
    }

    public MasterMonitorInfo setDeadTabletServers(List<DeadServer> deadTabletServers) {
        this.deadTabletServers = deadTabletServers;
        return this;
    }

    public void unsetDeadTabletServers() {
        this.deadTabletServers = null;
    }

    public boolean isSetDeadTabletServers() {
        return this.deadTabletServers != null;
    }

    public void setDeadTabletServersIsSet(boolean value) {
        if (!value) {
            this.deadTabletServers = null;
        }
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

    public MasterMonitorInfo setBulkImports(List<BulkImportStatus> bulkImports) {
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

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case TABLE_MAP:
                if (value == null) {
                    unsetTableMap();
                } else {
                    setTableMap((Map<String, TableInfo>) value);
                }
                break;
            case T_SERVER_INFO:
                if (value == null) {
                    unsetTServerInfo();
                } else {
                    setTServerInfo((List<TabletServerStatus>) value);
                }
                break;
            case BAD_TSERVERS:
                if (value == null) {
                    unsetBadTServers();
                } else {
                    setBadTServers((Map<String, Byte>) value);
                }
                break;
            case STATE:
                if (value == null) {
                    unsetState();
                } else {
                    setState((MasterState) value);
                }
                break;
            case GOAL_STATE:
                if (value == null) {
                    unsetGoalState();
                } else {
                    setGoalState((MasterGoalState) value);
                }
                break;
            case UNASSIGNED_TABLETS:
                if (value == null) {
                    unsetUnassignedTablets();
                } else {
                    setUnassignedTablets((Integer) value);
                }
                break;
            case SERVERS_SHUTTING_DOWN:
                if (value == null) {
                    unsetServersShuttingDown();
                } else {
                    setServersShuttingDown((Set<String>) value);
                }
                break;
            case DEAD_TABLET_SERVERS:
                if (value == null) {
                    unsetDeadTabletServers();
                } else {
                    setDeadTabletServers((List<DeadServer>) value);
                }
                break;
            case BULK_IMPORTS:
                if (value == null) {
                    unsetBulkImports();
                } else {
                    setBulkImports((List<BulkImportStatus>) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case TABLE_MAP:
                return getTableMap();
            case T_SERVER_INFO:
                return getTServerInfo();
            case BAD_TSERVERS:
                return getBadTServers();
            case STATE:
                return getState();
            case GOAL_STATE:
                return getGoalState();
            case UNASSIGNED_TABLETS:
                return Integer.valueOf(getUnassignedTablets());
            case SERVERS_SHUTTING_DOWN:
                return getServersShuttingDown();
            case DEAD_TABLET_SERVERS:
                return getDeadTabletServers();
            case BULK_IMPORTS:
                return getBulkImports();
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
            case T_SERVER_INFO:
                return isSetTServerInfo();
            case BAD_TSERVERS:
                return isSetBadTServers();
            case STATE:
                return isSetState();
            case GOAL_STATE:
                return isSetGoalState();
            case UNASSIGNED_TABLETS:
                return isSetUnassignedTablets();
            case SERVERS_SHUTTING_DOWN:
                return isSetServersShuttingDown();
            case DEAD_TABLET_SERVERS:
                return isSetDeadTabletServers();
            case BULK_IMPORTS:
                return isSetBulkImports();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof MasterMonitorInfo)
            return this.equals((MasterMonitorInfo) that);
        return false;
    }

    public boolean equals(MasterMonitorInfo that) {
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
        boolean this_present_tServerInfo = true && this.isSetTServerInfo();
        boolean that_present_tServerInfo = true && that.isSetTServerInfo();
        if (this_present_tServerInfo || that_present_tServerInfo) {
            if (!(this_present_tServerInfo && that_present_tServerInfo))
                return false;
            if (!this.tServerInfo.equals(that.tServerInfo))
                return false;
        }
        boolean this_present_badTServers = true && this.isSetBadTServers();
        boolean that_present_badTServers = true && that.isSetBadTServers();
        if (this_present_badTServers || that_present_badTServers) {
            if (!(this_present_badTServers && that_present_badTServers))
                return false;
            if (!this.badTServers.equals(that.badTServers))
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
        boolean this_present_goalState = true && this.isSetGoalState();
        boolean that_present_goalState = true && that.isSetGoalState();
        if (this_present_goalState || that_present_goalState) {
            if (!(this_present_goalState && that_present_goalState))
                return false;
            if (!this.goalState.equals(that.goalState))
                return false;
        }
        boolean this_present_unassignedTablets = true;
        boolean that_present_unassignedTablets = true;
        if (this_present_unassignedTablets || that_present_unassignedTablets) {
            if (!(this_present_unassignedTablets && that_present_unassignedTablets))
                return false;
            if (this.unassignedTablets != that.unassignedTablets)
                return false;
        }
        boolean this_present_serversShuttingDown = true && this.isSetServersShuttingDown();
        boolean that_present_serversShuttingDown = true && that.isSetServersShuttingDown();
        if (this_present_serversShuttingDown || that_present_serversShuttingDown) {
            if (!(this_present_serversShuttingDown && that_present_serversShuttingDown))
                return false;
            if (!this.serversShuttingDown.equals(that.serversShuttingDown))
                return false;
        }
        boolean this_present_deadTabletServers = true && this.isSetDeadTabletServers();
        boolean that_present_deadTabletServers = true && that.isSetDeadTabletServers();
        if (this_present_deadTabletServers || that_present_deadTabletServers) {
            if (!(this_present_deadTabletServers && that_present_deadTabletServers))
                return false;
            if (!this.deadTabletServers.equals(that.deadTabletServers))
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
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int compareTo(MasterMonitorInfo other) {
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
        lastComparison = Boolean.valueOf(isSetTServerInfo()).compareTo(other.isSetTServerInfo());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTServerInfo()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tServerInfo, other.tServerInfo);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetBadTServers()).compareTo(other.isSetBadTServers());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetBadTServers()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.badTServers, other.badTServers);
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
        lastComparison = Boolean.valueOf(isSetGoalState()).compareTo(other.isSetGoalState());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetGoalState()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.goalState, other.goalState);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetUnassignedTablets()).compareTo(other.isSetUnassignedTablets());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetUnassignedTablets()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.unassignedTablets, other.unassignedTablets);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetServersShuttingDown()).compareTo(other.isSetServersShuttingDown());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetServersShuttingDown()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.serversShuttingDown, other.serversShuttingDown);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetDeadTabletServers()).compareTo(other.isSetDeadTabletServers());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetDeadTabletServers()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.deadTabletServers, other.deadTabletServers);
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
        StringBuilder sb = new StringBuilder("MasterMonitorInfo(");
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
        sb.append("tServerInfo:");
        if (this.tServerInfo == null) {
            sb.append("null");
        } else {
            sb.append(this.tServerInfo);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("badTServers:");
        if (this.badTServers == null) {
            sb.append("null");
        } else {
            sb.append(this.badTServers);
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
        sb.append("goalState:");
        if (this.goalState == null) {
            sb.append("null");
        } else {
            sb.append(this.goalState);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("unassignedTablets:");
        sb.append(this.unassignedTablets);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("serversShuttingDown:");
        if (this.serversShuttingDown == null) {
            sb.append("null");
        } else {
            sb.append(this.serversShuttingDown);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("deadTabletServers:");
        if (this.deadTabletServers == null) {
            sb.append("null");
        } else {
            sb.append(this.deadTabletServers);
        }
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

    private static class MasterMonitorInfoStandardSchemeFactory implements SchemeFactory {

        public MasterMonitorInfoStandardScheme getScheme() {
            return new MasterMonitorInfoStandardScheme();
        }
    }

    private static class MasterMonitorInfoStandardScheme extends StandardScheme<MasterMonitorInfo> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, MasterMonitorInfo struct) throws org.apache.thrift.TException {
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
                                org.apache.thrift.protocol.TMap _map26 = iprot.readMapBegin();
                                struct.tableMap = new HashMap<String, TableInfo>(2 * _map26.size);
                                for (int _i27 = 0; _i27 < _map26.size; ++_i27) {
                                    String _key28;
                                    TableInfo _val29;
                                    _key28 = iprot.readString();
                                    _val29 = new TableInfo();
                                    _val29.read(iprot);
                                    struct.tableMap.put(_key28, _val29);
                                }
                                iprot.readMapEnd();
                            }
                            struct.setTableMapIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list30 = iprot.readListBegin();
                                struct.tServerInfo = new ArrayList<TabletServerStatus>(_list30.size);
                                for (int _i31 = 0; _i31 < _list30.size; ++_i31) {
                                    TabletServerStatus _elem32;
                                    _elem32 = new TabletServerStatus();
                                    _elem32.read(iprot);
                                    struct.tServerInfo.add(_elem32);
                                }
                                iprot.readListEnd();
                            }
                            struct.setTServerInfoIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
                            {
                                org.apache.thrift.protocol.TMap _map33 = iprot.readMapBegin();
                                struct.badTServers = new HashMap<String, Byte>(2 * _map33.size);
                                for (int _i34 = 0; _i34 < _map33.size; ++_i34) {
                                    String _key35;
                                    byte _val36;
                                    _key35 = iprot.readString();
                                    _val36 = iprot.readByte();
                                    struct.badTServers.put(_key35, _val36);
                                }
                                iprot.readMapEnd();
                            }
                            struct.setBadTServersIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 6:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.state = MasterState.findByValue(iprot.readI32());
                            struct.setStateIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 8:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.goalState = MasterGoalState.findByValue(iprot.readI32());
                            struct.setGoalStateIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 7:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.unassignedTablets = iprot.readI32();
                            struct.setUnassignedTabletsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 9:
                        if (schemeField.type == org.apache.thrift.protocol.TType.SET) {
                            {
                                org.apache.thrift.protocol.TSet _set37 = iprot.readSetBegin();
                                struct.serversShuttingDown = new HashSet<String>(2 * _set37.size);
                                for (int _i38 = 0; _i38 < _set37.size; ++_i38) {
                                    String _elem39;
                                    _elem39 = iprot.readString();
                                    struct.serversShuttingDown.add(_elem39);
                                }
                                iprot.readSetEnd();
                            }
                            struct.setServersShuttingDownIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 10:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list40 = iprot.readListBegin();
                                struct.deadTabletServers = new ArrayList<DeadServer>(_list40.size);
                                for (int _i41 = 0; _i41 < _list40.size; ++_i41) {
                                    DeadServer _elem42;
                                    _elem42 = new DeadServer();
                                    _elem42.read(iprot);
                                    struct.deadTabletServers.add(_elem42);
                                }
                                iprot.readListEnd();
                            }
                            struct.setDeadTabletServersIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 11:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list43 = iprot.readListBegin();
                                struct.bulkImports = new ArrayList<BulkImportStatus>(_list43.size);
                                for (int _i44 = 0; _i44 < _list43.size; ++_i44) {
                                    BulkImportStatus _elem45;
                                    _elem45 = new BulkImportStatus();
                                    _elem45.read(iprot);
                                    struct.bulkImports.add(_elem45);
                                }
                                iprot.readListEnd();
                            }
                            struct.setBulkImportsIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, MasterMonitorInfo struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.tableMap != null) {
                oprot.writeFieldBegin(TABLE_MAP_FIELD_DESC);
                {
                    oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRUCT, struct.tableMap.size()));
                    for (Map.Entry<String, TableInfo> _iter46 : struct.tableMap.entrySet()) {
                        oprot.writeString(_iter46.getKey());
                        _iter46.getValue().write(oprot);
                    }
                    oprot.writeMapEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.tServerInfo != null) {
                oprot.writeFieldBegin(T_SERVER_INFO_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.tServerInfo.size()));
                    for (TabletServerStatus _iter47 : struct.tServerInfo) {
                        _iter47.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.badTServers != null) {
                oprot.writeFieldBegin(BAD_TSERVERS_FIELD_DESC);
                {
                    oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.BYTE, struct.badTServers.size()));
                    for (Map.Entry<String, Byte> _iter48 : struct.badTServers.entrySet()) {
                        oprot.writeString(_iter48.getKey());
                        oprot.writeByte(_iter48.getValue());
                    }
                    oprot.writeMapEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.state != null) {
                oprot.writeFieldBegin(STATE_FIELD_DESC);
                oprot.writeI32(struct.state.getValue());
                oprot.writeFieldEnd();
            }
            oprot.writeFieldBegin(UNASSIGNED_TABLETS_FIELD_DESC);
            oprot.writeI32(struct.unassignedTablets);
            oprot.writeFieldEnd();
            if (struct.goalState != null) {
                oprot.writeFieldBegin(GOAL_STATE_FIELD_DESC);
                oprot.writeI32(struct.goalState.getValue());
                oprot.writeFieldEnd();
            }
            if (struct.serversShuttingDown != null) {
                oprot.writeFieldBegin(SERVERS_SHUTTING_DOWN_FIELD_DESC);
                {
                    oprot.writeSetBegin(new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.STRING, struct.serversShuttingDown.size()));
                    for (String _iter49 : struct.serversShuttingDown) {
                        oprot.writeString(_iter49);
                    }
                    oprot.writeSetEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.deadTabletServers != null) {
                oprot.writeFieldBegin(DEAD_TABLET_SERVERS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.deadTabletServers.size()));
                    for (DeadServer _iter50 : struct.deadTabletServers) {
                        _iter50.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.bulkImports != null) {
                oprot.writeFieldBegin(BULK_IMPORTS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.bulkImports.size()));
                    for (BulkImportStatus _iter51 : struct.bulkImports) {
                        _iter51.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class MasterMonitorInfoTupleSchemeFactory implements SchemeFactory {

        public MasterMonitorInfoTupleScheme getScheme() {
            return new MasterMonitorInfoTupleScheme();
        }
    }

    private static class MasterMonitorInfoTupleScheme extends TupleScheme<MasterMonitorInfo> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, MasterMonitorInfo struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetTableMap()) {
                optionals.set(0);
            }
            if (struct.isSetTServerInfo()) {
                optionals.set(1);
            }
            if (struct.isSetBadTServers()) {
                optionals.set(2);
            }
            if (struct.isSetState()) {
                optionals.set(3);
            }
            if (struct.isSetGoalState()) {
                optionals.set(4);
            }
            if (struct.isSetUnassignedTablets()) {
                optionals.set(5);
            }
            if (struct.isSetServersShuttingDown()) {
                optionals.set(6);
            }
            if (struct.isSetDeadTabletServers()) {
                optionals.set(7);
            }
            if (struct.isSetBulkImports()) {
                optionals.set(8);
            }
            oprot.writeBitSet(optionals, 9);
            if (struct.isSetTableMap()) {
                {
                    oprot.writeI32(struct.tableMap.size());
                    for (Map.Entry<String, TableInfo> _iter52 : struct.tableMap.entrySet()) {
                        oprot.writeString(_iter52.getKey());
                        _iter52.getValue().write(oprot);
                    }
                }
            }
            if (struct.isSetTServerInfo()) {
                {
                    oprot.writeI32(struct.tServerInfo.size());
                    for (TabletServerStatus _iter53 : struct.tServerInfo) {
                        _iter53.write(oprot);
                    }
                }
            }
            if (struct.isSetBadTServers()) {
                {
                    oprot.writeI32(struct.badTServers.size());
                    for (Map.Entry<String, Byte> _iter54 : struct.badTServers.entrySet()) {
                        oprot.writeString(_iter54.getKey());
                        oprot.writeByte(_iter54.getValue());
                    }
                }
            }
            if (struct.isSetState()) {
                oprot.writeI32(struct.state.getValue());
            }
            if (struct.isSetGoalState()) {
                oprot.writeI32(struct.goalState.getValue());
            }
            if (struct.isSetUnassignedTablets()) {
                oprot.writeI32(struct.unassignedTablets);
            }
            if (struct.isSetServersShuttingDown()) {
                {
                    oprot.writeI32(struct.serversShuttingDown.size());
                    for (String _iter55 : struct.serversShuttingDown) {
                        oprot.writeString(_iter55);
                    }
                }
            }
            if (struct.isSetDeadTabletServers()) {
                {
                    oprot.writeI32(struct.deadTabletServers.size());
                    for (DeadServer _iter56 : struct.deadTabletServers) {
                        _iter56.write(oprot);
                    }
                }
            }
            if (struct.isSetBulkImports()) {
                {
                    oprot.writeI32(struct.bulkImports.size());
                    for (BulkImportStatus _iter57 : struct.bulkImports) {
                        _iter57.write(oprot);
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, MasterMonitorInfo struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(9);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TMap _map58 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.tableMap = new HashMap<String, TableInfo>(2 * _map58.size);
                    for (int _i59 = 0; _i59 < _map58.size; ++_i59) {
                        String _key60;
                        TableInfo _val61;
                        _key60 = iprot.readString();
                        _val61 = new TableInfo();
                        _val61.read(iprot);
                        struct.tableMap.put(_key60, _val61);
                    }
                }
                struct.setTableMapIsSet(true);
            }
            if (incoming.get(1)) {
                {
                    org.apache.thrift.protocol.TList _list62 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.tServerInfo = new ArrayList<TabletServerStatus>(_list62.size);
                    for (int _i63 = 0; _i63 < _list62.size; ++_i63) {
                        TabletServerStatus _elem64;
                        _elem64 = new TabletServerStatus();
                        _elem64.read(iprot);
                        struct.tServerInfo.add(_elem64);
                    }
                }
                struct.setTServerInfoIsSet(true);
            }
            if (incoming.get(2)) {
                {
                    org.apache.thrift.protocol.TMap _map65 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.BYTE, iprot.readI32());
                    struct.badTServers = new HashMap<String, Byte>(2 * _map65.size);
                    for (int _i66 = 0; _i66 < _map65.size; ++_i66) {
                        String _key67;
                        byte _val68;
                        _key67 = iprot.readString();
                        _val68 = iprot.readByte();
                        struct.badTServers.put(_key67, _val68);
                    }
                }
                struct.setBadTServersIsSet(true);
            }
            if (incoming.get(3)) {
                struct.state = MasterState.findByValue(iprot.readI32());
                struct.setStateIsSet(true);
            }
            if (incoming.get(4)) {
                struct.goalState = MasterGoalState.findByValue(iprot.readI32());
                struct.setGoalStateIsSet(true);
            }
            if (incoming.get(5)) {
                struct.unassignedTablets = iprot.readI32();
                struct.setUnassignedTabletsIsSet(true);
            }
            if (incoming.get(6)) {
                {
                    org.apache.thrift.protocol.TSet _set69 = new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.serversShuttingDown = new HashSet<String>(2 * _set69.size);
                    for (int _i70 = 0; _i70 < _set69.size; ++_i70) {
                        String _elem71;
                        _elem71 = iprot.readString();
                        struct.serversShuttingDown.add(_elem71);
                    }
                }
                struct.setServersShuttingDownIsSet(true);
            }
            if (incoming.get(7)) {
                {
                    org.apache.thrift.protocol.TList _list72 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.deadTabletServers = new ArrayList<DeadServer>(_list72.size);
                    for (int _i73 = 0; _i73 < _list72.size; ++_i73) {
                        DeadServer _elem74;
                        _elem74 = new DeadServer();
                        _elem74.read(iprot);
                        struct.deadTabletServers.add(_elem74);
                    }
                }
                struct.setDeadTabletServersIsSet(true);
            }
            if (incoming.get(8)) {
                {
                    org.apache.thrift.protocol.TList _list75 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.bulkImports = new ArrayList<BulkImportStatus>(_list75.size);
                    for (int _i76 = 0; _i76 < _list75.size; ++_i76) {
                        BulkImportStatus _elem77;
                        _elem77 = new BulkImportStatus();
                        _elem77.read(iprot);
                        struct.bulkImports.add(_elem77);
                    }
                }
                struct.setBulkImportsIsSet(true);
            }
        }
    }
}
