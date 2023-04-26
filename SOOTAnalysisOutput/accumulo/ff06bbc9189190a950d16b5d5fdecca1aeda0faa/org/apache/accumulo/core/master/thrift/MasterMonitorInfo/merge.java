package org.apache.accumulo.core.master.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;
import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
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
public class MasterMonitorInfo implements org.apache.thrift.TBase<MasterMonitorInfo, MasterMonitorInfo._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MasterMonitorInfo");

    private static final org.apache.thrift.protocol.TField TABLE_MAP_FIELD_DESC = new org.apache.thrift.protocol.TField("tableMap", org.apache.thrift.protocol.TType.MAP, (short) 1);

    private static final org.apache.thrift.protocol.TField T_SERVER_INFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tServerInfo", org.apache.thrift.protocol.TType.LIST, (short) 2);

    private static final org.apache.thrift.protocol.TField BAD_TSERVERS_FIELD_DESC = new org.apache.thrift.protocol.TField("badTServers", org.apache.thrift.protocol.TType.MAP, (short) 3);

    private static final org.apache.thrift.protocol.TField STATE_FIELD_DESC = new org.apache.thrift.protocol.TField("state", org.apache.thrift.protocol.TType.I32, (short) 6);

    private static final org.apache.thrift.protocol.TField GOAL_STATE_FIELD_DESC = new org.apache.thrift.protocol.TField("goalState", org.apache.thrift.protocol.TType.I32, (short) 8);

    private static final org.apache.thrift.protocol.TField UNASSIGNED_TABLETS_FIELD_DESC = new org.apache.thrift.protocol.TField("unassignedTablets", org.apache.thrift.protocol.TType.I32, (short) 7);

    private static final org.apache.thrift.protocol.TField SERVERS_SHUTTING_DOWN_FIELD_DESC = new org.apache.thrift.protocol.TField("serversShuttingDown", org.apache.thrift.protocol.TType.SET, (short) 9);

    private static final org.apache.thrift.protocol.TField DEAD_TABLET_SERVERS_FIELD_DESC = new org.apache.thrift.protocol.TField("deadTabletServers", org.apache.thrift.protocol.TType.LIST, (short) 10);

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

    @SuppressWarnings("all")
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        TABLE_MAP((short) 1, "tableMap"),
        T_SERVER_INFO((short) 2, "tServerInfo"),
        BAD_TSERVERS((short) 3, "badTServers"),
        STATE((short) 6, "state"),
        GOAL_STATE((short) 8, "goalState"),
        UNASSIGNED_TABLETS((short) 7, "unassignedTablets"),
        SERVERS_SHUTTING_DOWN((short) 9, "serversShuttingDown"),
        DEAD_TABLET_SERVERS((short) 10, "deadTabletServers");

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
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MasterMonitorInfo.class, metaDataMap);
    }

    public MasterMonitorInfo() {
    }

    public MasterMonitorInfo(Map<String, TableInfo> tableMap, List<TabletServerStatus> tServerInfo, Map<String, Byte> badTServers, MasterState state, MasterGoalState goalState, int unassignedTablets, Set<String> serversShuttingDown, List<DeadServer> deadTabletServers) {
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
    }

    public MasterMonitorInfo(MasterMonitorInfo other) {
        __isset_bitfield = other.__isset_bitfield;
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
        if (other.isSetTServerInfo()) {
            List<TabletServerStatus> __this__tServerInfo = new ArrayList<TabletServerStatus>();
            for (TabletServerStatus other_element : other.tServerInfo) {
                __this__tServerInfo.add(new TabletServerStatus(other_element));
            }
            this.tServerInfo = __this__tServerInfo;
        }
        if (other.isSetBadTServers()) {
            Map<String, Byte> __this__badTServers = new HashMap<String, Byte>();
            for (Map.Entry<String, Byte> other_element : other.badTServers.entrySet()) {
                String other_element_key = other_element.getKey();
                Byte other_element_value = other_element.getValue();
                String __this__badTServers_copy_key = other_element_key;
                Byte __this__badTServers_copy_value = other_element_value;
                __this__badTServers.put(__this__badTServers_copy_key, __this__badTServers_copy_value);
            }
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
            Set<String> __this__serversShuttingDown = new HashSet<String>();
            for (String other_element : other.serversShuttingDown) {
                __this__serversShuttingDown.add(other_element);
            }
            this.serversShuttingDown = __this__serversShuttingDown;
        }
        if (other.isSetDeadTabletServers()) {
            List<DeadServer> __this__deadTabletServers = new ArrayList<DeadServer>();
            for (DeadServer other_element : other.deadTabletServers) {
                __this__deadTabletServers.add(new DeadServer(other_element));
            }
            this.deadTabletServers = __this__deadTabletServers;
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
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public int compareTo(MasterMonitorInfo other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        MasterMonitorInfo typedOther = (MasterMonitorInfo) other;
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
        lastComparison = Boolean.valueOf(isSetTServerInfo()).compareTo(typedOther.isSetTServerInfo());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTServerInfo()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tServerInfo, typedOther.tServerInfo);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetBadTServers()).compareTo(typedOther.isSetBadTServers());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetBadTServers()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.badTServers, typedOther.badTServers);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetState()).compareTo(typedOther.isSetState());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetState()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.state, typedOther.state);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetGoalState()).compareTo(typedOther.isSetGoalState());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetGoalState()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.goalState, typedOther.goalState);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetUnassignedTablets()).compareTo(typedOther.isSetUnassignedTablets());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetUnassignedTablets()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.unassignedTablets, typedOther.unassignedTablets);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetServersShuttingDown()).compareTo(typedOther.isSetServersShuttingDown());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetServersShuttingDown()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.serversShuttingDown, typedOther.serversShuttingDown);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetDeadTabletServers()).compareTo(typedOther.isSetDeadTabletServers());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetDeadTabletServers()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.deadTabletServers, typedOther.deadTabletServers);
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
                                org.apache.thrift.protocol.TMap _map18 = iprot.readMapBegin();
                                struct.tableMap = new HashMap<String, TableInfo>(2 * _map18.size);
                                for (int _i19 = 0; _i19 < _map18.size; ++_i19) {
                                    String _key20;
                                    TableInfo _val21;
                                    _key20 = iprot.readString();
                                    _val21 = new TableInfo();
                                    _val21.read(iprot);
                                    struct.tableMap.put(_key20, _val21);
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
                                org.apache.thrift.protocol.TList _list22 = iprot.readListBegin();
                                struct.tServerInfo = new ArrayList<TabletServerStatus>(_list22.size);
                                for (int _i23 = 0; _i23 < _list22.size; ++_i23) {
                                    TabletServerStatus _elem24;
                                    _elem24 = new TabletServerStatus();
                                    _elem24.read(iprot);
                                    struct.tServerInfo.add(_elem24);
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
                                org.apache.thrift.protocol.TMap _map25 = iprot.readMapBegin();
                                struct.badTServers = new HashMap<String, Byte>(2 * _map25.size);
                                for (int _i26 = 0; _i26 < _map25.size; ++_i26) {
                                    String _key27;
                                    byte _val28;
                                    _key27 = iprot.readString();
                                    _val28 = iprot.readByte();
                                    struct.badTServers.put(_key27, _val28);
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
                                org.apache.thrift.protocol.TSet _set29 = iprot.readSetBegin();
                                struct.serversShuttingDown = new HashSet<String>(2 * _set29.size);
                                for (int _i30 = 0; _i30 < _set29.size; ++_i30) {
                                    String _elem31;
                                    _elem31 = iprot.readString();
                                    struct.serversShuttingDown.add(_elem31);
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
                                org.apache.thrift.protocol.TList _list32 = iprot.readListBegin();
                                struct.deadTabletServers = new ArrayList<DeadServer>(_list32.size);
                                for (int _i33 = 0; _i33 < _list32.size; ++_i33) {
                                    DeadServer _elem34;
                                    _elem34 = new DeadServer();
                                    _elem34.read(iprot);
                                    struct.deadTabletServers.add(_elem34);
                                }
                                iprot.readListEnd();
                            }
                            struct.setDeadTabletServersIsSet(true);
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
                    for (Map.Entry<String, TableInfo> _iter35 : struct.tableMap.entrySet()) {
                        oprot.writeString(_iter35.getKey());
                        _iter35.getValue().write(oprot);
                    }
                    oprot.writeMapEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.tServerInfo != null) {
                oprot.writeFieldBegin(T_SERVER_INFO_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.tServerInfo.size()));
                    for (TabletServerStatus _iter36 : struct.tServerInfo) {
                        _iter36.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.badTServers != null) {
                oprot.writeFieldBegin(BAD_TSERVERS_FIELD_DESC);
                {
                    oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.BYTE, struct.badTServers.size()));
                    for (Map.Entry<String, Byte> _iter37 : struct.badTServers.entrySet()) {
                        oprot.writeString(_iter37.getKey());
                        oprot.writeByte(_iter37.getValue());
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
                    for (String _iter38 : struct.serversShuttingDown) {
                        oprot.writeString(_iter38);
                    }
                    oprot.writeSetEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.deadTabletServers != null) {
                oprot.writeFieldBegin(DEAD_TABLET_SERVERS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.deadTabletServers.size()));
                    for (DeadServer _iter39 : struct.deadTabletServers) {
                        _iter39.write(oprot);
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
            oprot.writeBitSet(optionals, 8);
            if (struct.isSetTableMap()) {
                {
                    oprot.writeI32(struct.tableMap.size());
                    for (Map.Entry<String, TableInfo> _iter40 : struct.tableMap.entrySet()) {
                        oprot.writeString(_iter40.getKey());
                        _iter40.getValue().write(oprot);
                    }
                }
            }
            if (struct.isSetTServerInfo()) {
                {
                    oprot.writeI32(struct.tServerInfo.size());
                    for (TabletServerStatus _iter41 : struct.tServerInfo) {
                        _iter41.write(oprot);
                    }
                }
            }
            if (struct.isSetBadTServers()) {
                {
                    oprot.writeI32(struct.badTServers.size());
                    for (Map.Entry<String, Byte> _iter42 : struct.badTServers.entrySet()) {
                        oprot.writeString(_iter42.getKey());
                        oprot.writeByte(_iter42.getValue());
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
                    for (String _iter43 : struct.serversShuttingDown) {
                        oprot.writeString(_iter43);
                    }
                }
            }
            if (struct.isSetDeadTabletServers()) {
                {
                    oprot.writeI32(struct.deadTabletServers.size());
                    for (DeadServer _iter44 : struct.deadTabletServers) {
                        _iter44.write(oprot);
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, MasterMonitorInfo struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(8);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TMap _map45 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.tableMap = new HashMap<String, TableInfo>(2 * _map45.size);
                    for (int _i46 = 0; _i46 < _map45.size; ++_i46) {
                        String _key47;
                        TableInfo _val48;
                        _key47 = iprot.readString();
                        _val48 = new TableInfo();
                        _val48.read(iprot);
                        struct.tableMap.put(_key47, _val48);
                    }
                }
                struct.setTableMapIsSet(true);
            }
            if (incoming.get(1)) {
                {
                    org.apache.thrift.protocol.TList _list49 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.tServerInfo = new ArrayList<TabletServerStatus>(_list49.size);
                    for (int _i50 = 0; _i50 < _list49.size; ++_i50) {
                        TabletServerStatus _elem51;
                        _elem51 = new TabletServerStatus();
                        _elem51.read(iprot);
                        struct.tServerInfo.add(_elem51);
                    }
                }
                struct.setTServerInfoIsSet(true);
            }
            if (incoming.get(2)) {
                {
                    org.apache.thrift.protocol.TMap _map52 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.BYTE, iprot.readI32());
                    struct.badTServers = new HashMap<String, Byte>(2 * _map52.size);
                    for (int _i53 = 0; _i53 < _map52.size; ++_i53) {
                        String _key54;
                        byte _val55;
                        _key54 = iprot.readString();
                        _val55 = iprot.readByte();
                        struct.badTServers.put(_key54, _val55);
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
                    org.apache.thrift.protocol.TSet _set56 = new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.serversShuttingDown = new HashSet<String>(2 * _set56.size);
                    for (int _i57 = 0; _i57 < _set56.size; ++_i57) {
                        String _elem58;
                        _elem58 = iprot.readString();
                        struct.serversShuttingDown.add(_elem58);
                    }
                }
                struct.setServersShuttingDownIsSet(true);
            }
            if (incoming.get(7)) {
                {
                    org.apache.thrift.protocol.TList _list59 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.deadTabletServers = new ArrayList<DeadServer>(_list59.size);
                    for (int _i60 = 0; _i60 < _list59.size; ++_i60) {
                        DeadServer _elem61;
                        _elem61 = new DeadServer();
                        _elem61.read(iprot);
                        struct.deadTabletServers.add(_elem61);
                    }
                }
                struct.setDeadTabletServersIsSet(true);
            }
        }
    }
}
