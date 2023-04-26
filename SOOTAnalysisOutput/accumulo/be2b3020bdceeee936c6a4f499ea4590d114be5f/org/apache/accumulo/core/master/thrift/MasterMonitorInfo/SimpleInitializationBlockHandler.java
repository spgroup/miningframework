package org.apache.accumulo.core.master.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
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

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new MasterMonitorInfoStandardSchemeFactory();

    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new MasterMonitorInfoTupleSchemeFactory();

    @org.apache.thrift.annotation.Nullable
    public java.util.Map<java.lang.String, TableInfo> tableMap;

    @org.apache.thrift.annotation.Nullable
    public java.util.List<TabletServerStatus> tServerInfo;

    @org.apache.thrift.annotation.Nullable
    public java.util.Map<java.lang.String, java.lang.Byte> badTServers;

    @org.apache.thrift.annotation.Nullable
    public MasterState state;

    @org.apache.thrift.annotation.Nullable
    public MasterGoalState goalState;

    public int unassignedTablets;

    @org.apache.thrift.annotation.Nullable
    public java.util.Set<java.lang.String> serversShuttingDown;

    @org.apache.thrift.annotation.Nullable
    public java.util.List<DeadServer> deadTabletServers;

    @org.apache.thrift.annotation.Nullable
    public java.util.List<BulkImportStatus> bulkImports;

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

    private static final int __UNASSIGNEDTABLETS_ISSET_ID = 0;

    private byte __isset_bitfield = 0;

    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.TABLE_MAP, new org.apache.thrift.meta_data.FieldMetaData("tableMap", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TableInfo.class))));
        tmpMap.put(_Fields.T_SERVER_INFO, new org.apache.thrift.meta_data.FieldMetaData("tServerInfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TabletServerStatus.class))));
        tmpMap.put(_Fields.BAD_TSERVERS, new org.apache.thrift.meta_data.FieldMetaData("badTServers", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BYTE))));
        tmpMap.put(_Fields.STATE, new org.apache.thrift.meta_data.FieldMetaData("state", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, MasterState.class)));
        tmpMap.put(_Fields.GOAL_STATE, new org.apache.thrift.meta_data.FieldMetaData("goalState", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, MasterGoalState.class)));
        tmpMap.put(_Fields.UNASSIGNED_TABLETS, new org.apache.thrift.meta_data.FieldMetaData("unassignedTablets", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.SERVERS_SHUTTING_DOWN, new org.apache.thrift.meta_data.FieldMetaData("serversShuttingDown", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.SetMetaData(org.apache.thrift.protocol.TType.SET, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
        tmpMap.put(_Fields.DEAD_TABLET_SERVERS, new org.apache.thrift.meta_data.FieldMetaData("deadTabletServers", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, DeadServer.class))));
        tmpMap.put(_Fields.BULK_IMPORTS, new org.apache.thrift.meta_data.FieldMetaData("bulkImports", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, BulkImportStatus.class))));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MasterMonitorInfo.class, metaDataMap);
    }

    public MasterMonitorInfo() {
    }

    public MasterMonitorInfo(java.util.Map<java.lang.String, TableInfo> tableMap, java.util.List<TabletServerStatus> tServerInfo, java.util.Map<java.lang.String, java.lang.Byte> badTServers, MasterState state, MasterGoalState goalState, int unassignedTablets, java.util.Set<java.lang.String> serversShuttingDown, java.util.List<DeadServer> deadTabletServers, java.util.List<BulkImportStatus> bulkImports) {
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
            java.util.Map<java.lang.String, TableInfo> __this__tableMap = new java.util.HashMap<java.lang.String, TableInfo>(other.tableMap.size());
            for (java.util.Map.Entry<java.lang.String, TableInfo> other_element : other.tableMap.entrySet()) {
                java.lang.String other_element_key = other_element.getKey();
                TableInfo other_element_value = other_element.getValue();
                java.lang.String __this__tableMap_copy_key = other_element_key;
                TableInfo __this__tableMap_copy_value = new TableInfo(other_element_value);
                __this__tableMap.put(__this__tableMap_copy_key, __this__tableMap_copy_value);
            }
            this.tableMap = __this__tableMap;
        }
        if (other.isSetTServerInfo()) {
            java.util.List<TabletServerStatus> __this__tServerInfo = new java.util.ArrayList<TabletServerStatus>(other.tServerInfo.size());
            for (TabletServerStatus other_element : other.tServerInfo) {
                __this__tServerInfo.add(new TabletServerStatus(other_element));
            }
            this.tServerInfo = __this__tServerInfo;
        }
        if (other.isSetBadTServers()) {
            java.util.Map<java.lang.String, java.lang.Byte> __this__badTServers = new java.util.HashMap<java.lang.String, java.lang.Byte>(other.badTServers);
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
            java.util.Set<java.lang.String> __this__serversShuttingDown = new java.util.HashSet<java.lang.String>(other.serversShuttingDown);
            this.serversShuttingDown = __this__serversShuttingDown;
        }
        if (other.isSetDeadTabletServers()) {
            java.util.List<DeadServer> __this__deadTabletServers = new java.util.ArrayList<DeadServer>(other.deadTabletServers.size());
            for (DeadServer other_element : other.deadTabletServers) {
                __this__deadTabletServers.add(new DeadServer(other_element));
            }
            this.deadTabletServers = __this__deadTabletServers;
        }
        if (other.isSetBulkImports()) {
            java.util.List<BulkImportStatus> __this__bulkImports = new java.util.ArrayList<BulkImportStatus>(other.bulkImports.size());
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

    public void putToTableMap(java.lang.String key, TableInfo val) {
        if (this.tableMap == null) {
            this.tableMap = new java.util.HashMap<java.lang.String, TableInfo>();
        }
        this.tableMap.put(key, val);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.Map<java.lang.String, TableInfo> getTableMap() {
        return this.tableMap;
    }

    public MasterMonitorInfo setTableMap(@org.apache.thrift.annotation.Nullable java.util.Map<java.lang.String, TableInfo> tableMap) {
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

    @org.apache.thrift.annotation.Nullable
    public java.util.Iterator<TabletServerStatus> getTServerInfoIterator() {
        return (this.tServerInfo == null) ? null : this.tServerInfo.iterator();
    }

    public void addToTServerInfo(TabletServerStatus elem) {
        if (this.tServerInfo == null) {
            this.tServerInfo = new java.util.ArrayList<TabletServerStatus>();
        }
        this.tServerInfo.add(elem);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.List<TabletServerStatus> getTServerInfo() {
        return this.tServerInfo;
    }

    public MasterMonitorInfo setTServerInfo(@org.apache.thrift.annotation.Nullable java.util.List<TabletServerStatus> tServerInfo) {
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

    public void putToBadTServers(java.lang.String key, byte val) {
        if (this.badTServers == null) {
            this.badTServers = new java.util.HashMap<java.lang.String, java.lang.Byte>();
        }
        this.badTServers.put(key, val);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.Map<java.lang.String, java.lang.Byte> getBadTServers() {
        return this.badTServers;
    }

    public MasterMonitorInfo setBadTServers(@org.apache.thrift.annotation.Nullable java.util.Map<java.lang.String, java.lang.Byte> badTServers) {
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

    @org.apache.thrift.annotation.Nullable
    public MasterState getState() {
        return this.state;
    }

    public MasterMonitorInfo setState(@org.apache.thrift.annotation.Nullable MasterState state) {
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

    @org.apache.thrift.annotation.Nullable
    public MasterGoalState getGoalState() {
        return this.goalState;
    }

    public MasterMonitorInfo setGoalState(@org.apache.thrift.annotation.Nullable MasterGoalState goalState) {
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
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __UNASSIGNEDTABLETS_ISSET_ID);
    }

    public boolean isSetUnassignedTablets() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __UNASSIGNEDTABLETS_ISSET_ID);
    }

    public void setUnassignedTabletsIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __UNASSIGNEDTABLETS_ISSET_ID, value);
    }

    public int getServersShuttingDownSize() {
        return (this.serversShuttingDown == null) ? 0 : this.serversShuttingDown.size();
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.Iterator<java.lang.String> getServersShuttingDownIterator() {
        return (this.serversShuttingDown == null) ? null : this.serversShuttingDown.iterator();
    }

    public void addToServersShuttingDown(java.lang.String elem) {
        if (this.serversShuttingDown == null) {
            this.serversShuttingDown = new java.util.HashSet<java.lang.String>();
        }
        this.serversShuttingDown.add(elem);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.Set<java.lang.String> getServersShuttingDown() {
        return this.serversShuttingDown;
    }

    public MasterMonitorInfo setServersShuttingDown(@org.apache.thrift.annotation.Nullable java.util.Set<java.lang.String> serversShuttingDown) {
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

    @org.apache.thrift.annotation.Nullable
    public java.util.Iterator<DeadServer> getDeadTabletServersIterator() {
        return (this.deadTabletServers == null) ? null : this.deadTabletServers.iterator();
    }

    public void addToDeadTabletServers(DeadServer elem) {
        if (this.deadTabletServers == null) {
            this.deadTabletServers = new java.util.ArrayList<DeadServer>();
        }
        this.deadTabletServers.add(elem);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.List<DeadServer> getDeadTabletServers() {
        return this.deadTabletServers;
    }

    public MasterMonitorInfo setDeadTabletServers(@org.apache.thrift.annotation.Nullable java.util.List<DeadServer> deadTabletServers) {
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

    @org.apache.thrift.annotation.Nullable
    public java.util.Iterator<BulkImportStatus> getBulkImportsIterator() {
        return (this.bulkImports == null) ? null : this.bulkImports.iterator();
    }

    public void addToBulkImports(BulkImportStatus elem) {
        if (this.bulkImports == null) {
            this.bulkImports = new java.util.ArrayList<BulkImportStatus>();
        }
        this.bulkImports.add(elem);
    }

    @org.apache.thrift.annotation.Nullable
    public java.util.List<BulkImportStatus> getBulkImports() {
        return this.bulkImports;
    }

    public MasterMonitorInfo setBulkImports(@org.apache.thrift.annotation.Nullable java.util.List<BulkImportStatus> bulkImports) {
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

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch(field) {
            case TABLE_MAP:
                if (value == null) {
                    unsetTableMap();
                } else {
                    setTableMap((java.util.Map<java.lang.String, TableInfo>) value);
                }
                break;
            case T_SERVER_INFO:
                if (value == null) {
                    unsetTServerInfo();
                } else {
                    setTServerInfo((java.util.List<TabletServerStatus>) value);
                }
                break;
            case BAD_TSERVERS:
                if (value == null) {
                    unsetBadTServers();
                } else {
                    setBadTServers((java.util.Map<java.lang.String, java.lang.Byte>) value);
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
                    setUnassignedTablets((java.lang.Integer) value);
                }
                break;
            case SERVERS_SHUTTING_DOWN:
                if (value == null) {
                    unsetServersShuttingDown();
                } else {
                    setServersShuttingDown((java.util.Set<java.lang.String>) value);
                }
                break;
            case DEAD_TABLET_SERVERS:
                if (value == null) {
                    unsetDeadTabletServers();
                } else {
                    setDeadTabletServers((java.util.List<DeadServer>) value);
                }
                break;
            case BULK_IMPORTS:
                if (value == null) {
                    unsetBulkImports();
                } else {
                    setBulkImports((java.util.List<BulkImportStatus>) value);
                }
                break;
        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
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
                return getUnassignedTablets();
            case SERVERS_SHUTTING_DOWN:
                return getServersShuttingDown();
            case DEAD_TABLET_SERVERS:
                return getDeadTabletServers();
            case BULK_IMPORTS:
                return getBulkImports();
        }
        throw new java.lang.IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
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
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that == null)
            return false;
        if (that instanceof MasterMonitorInfo)
            return this.equals((MasterMonitorInfo) that);
        return false;
    }

    public boolean equals(MasterMonitorInfo that) {
        if (that == null)
            return false;
        if (this == that)
            return true;
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
        int hashCode = 1;
        hashCode = hashCode * 8191 + ((isSetTableMap()) ? 131071 : 524287);
        if (isSetTableMap())
            hashCode = hashCode * 8191 + tableMap.hashCode();
        hashCode = hashCode * 8191 + ((isSetTServerInfo()) ? 131071 : 524287);
        if (isSetTServerInfo())
            hashCode = hashCode * 8191 + tServerInfo.hashCode();
        hashCode = hashCode * 8191 + ((isSetBadTServers()) ? 131071 : 524287);
        if (isSetBadTServers())
            hashCode = hashCode * 8191 + badTServers.hashCode();
        hashCode = hashCode * 8191 + ((isSetState()) ? 131071 : 524287);
        if (isSetState())
            hashCode = hashCode * 8191 + state.getValue();
        hashCode = hashCode * 8191 + ((isSetGoalState()) ? 131071 : 524287);
        if (isSetGoalState())
            hashCode = hashCode * 8191 + goalState.getValue();
        hashCode = hashCode * 8191 + unassignedTablets;
        hashCode = hashCode * 8191 + ((isSetServersShuttingDown()) ? 131071 : 524287);
        if (isSetServersShuttingDown())
            hashCode = hashCode * 8191 + serversShuttingDown.hashCode();
        hashCode = hashCode * 8191 + ((isSetDeadTabletServers()) ? 131071 : 524287);
        if (isSetDeadTabletServers())
            hashCode = hashCode * 8191 + deadTabletServers.hashCode();
        hashCode = hashCode * 8191 + ((isSetBulkImports()) ? 131071 : 524287);
        if (isSetBulkImports())
            hashCode = hashCode * 8191 + bulkImports.hashCode();
        return hashCode;
    }

    @Override
    public int compareTo(MasterMonitorInfo other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        lastComparison = java.lang.Boolean.valueOf(isSetTableMap()).compareTo(other.isSetTableMap());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTableMap()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tableMap, other.tableMap);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetTServerInfo()).compareTo(other.isSetTServerInfo());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTServerInfo()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tServerInfo, other.tServerInfo);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetBadTServers()).compareTo(other.isSetBadTServers());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetBadTServers()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.badTServers, other.badTServers);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetState()).compareTo(other.isSetState());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetState()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.state, other.state);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetGoalState()).compareTo(other.isSetGoalState());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetGoalState()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.goalState, other.goalState);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetUnassignedTablets()).compareTo(other.isSetUnassignedTablets());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetUnassignedTablets()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.unassignedTablets, other.unassignedTablets);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetServersShuttingDown()).compareTo(other.isSetServersShuttingDown());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetServersShuttingDown()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.serversShuttingDown, other.serversShuttingDown);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetDeadTabletServers()).compareTo(other.isSetDeadTabletServers());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetDeadTabletServers()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.deadTabletServers, other.deadTabletServers);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.valueOf(isSetBulkImports()).compareTo(other.isSetBulkImports());
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
        java.lang.StringBuilder sb = new java.lang.StringBuilder("MasterMonitorInfo(");
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

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
        try {
            __isset_bitfield = 0;
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class MasterMonitorInfoStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public MasterMonitorInfoStandardScheme getScheme() {
            return new MasterMonitorInfoStandardScheme();
        }
    }

    private static class MasterMonitorInfoStandardScheme extends org.apache.thrift.scheme.StandardScheme<MasterMonitorInfo> {

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
                                struct.tableMap = new java.util.HashMap<java.lang.String, TableInfo>(2 * _map26.size);
                                @org.apache.thrift.annotation.Nullable
                                java.lang.String _key27;
                                @org.apache.thrift.annotation.Nullable
                                TableInfo _val28;
                                for (int _i29 = 0; _i29 < _map26.size; ++_i29) {
                                    _key27 = iprot.readString();
                                    _val28 = new TableInfo();
                                    _val28.read(iprot);
                                    struct.tableMap.put(_key27, _val28);
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
                                struct.tServerInfo = new java.util.ArrayList<TabletServerStatus>(_list30.size);
                                @org.apache.thrift.annotation.Nullable
                                TabletServerStatus _elem31;
                                for (int _i32 = 0; _i32 < _list30.size; ++_i32) {
                                    _elem31 = new TabletServerStatus();
                                    _elem31.read(iprot);
                                    struct.tServerInfo.add(_elem31);
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
                                struct.badTServers = new java.util.HashMap<java.lang.String, java.lang.Byte>(2 * _map33.size);
                                @org.apache.thrift.annotation.Nullable
                                java.lang.String _key34;
                                byte _val35;
                                for (int _i36 = 0; _i36 < _map33.size; ++_i36) {
                                    _key34 = iprot.readString();
                                    _val35 = iprot.readByte();
                                    struct.badTServers.put(_key34, _val35);
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
                            struct.state = org.apache.accumulo.core.master.thrift.MasterState.findByValue(iprot.readI32());
                            struct.setStateIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 8:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.goalState = org.apache.accumulo.core.master.thrift.MasterGoalState.findByValue(iprot.readI32());
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
                                struct.serversShuttingDown = new java.util.HashSet<java.lang.String>(2 * _set37.size);
                                @org.apache.thrift.annotation.Nullable
                                java.lang.String _elem38;
                                for (int _i39 = 0; _i39 < _set37.size; ++_i39) {
                                    _elem38 = iprot.readString();
                                    struct.serversShuttingDown.add(_elem38);
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
                                struct.deadTabletServers = new java.util.ArrayList<DeadServer>(_list40.size);
                                @org.apache.thrift.annotation.Nullable
                                DeadServer _elem41;
                                for (int _i42 = 0; _i42 < _list40.size; ++_i42) {
                                    _elem41 = new DeadServer();
                                    _elem41.read(iprot);
                                    struct.deadTabletServers.add(_elem41);
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
                                struct.bulkImports = new java.util.ArrayList<BulkImportStatus>(_list43.size);
                                @org.apache.thrift.annotation.Nullable
                                BulkImportStatus _elem44;
                                for (int _i45 = 0; _i45 < _list43.size; ++_i45) {
                                    _elem44 = new BulkImportStatus();
                                    _elem44.read(iprot);
                                    struct.bulkImports.add(_elem44);
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
                    for (java.util.Map.Entry<java.lang.String, TableInfo> _iter46 : struct.tableMap.entrySet()) {
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
                    for (java.util.Map.Entry<java.lang.String, java.lang.Byte> _iter48 : struct.badTServers.entrySet()) {
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
                    for (java.lang.String _iter49 : struct.serversShuttingDown) {
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

    private static class MasterMonitorInfoTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

        public MasterMonitorInfoTupleScheme getScheme() {
            return new MasterMonitorInfoTupleScheme();
        }
    }

    private static class MasterMonitorInfoTupleScheme extends org.apache.thrift.scheme.TupleScheme<MasterMonitorInfo> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, MasterMonitorInfo struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet optionals = new java.util.BitSet();
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
                    for (java.util.Map.Entry<java.lang.String, TableInfo> _iter52 : struct.tableMap.entrySet()) {
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
                    for (java.util.Map.Entry<java.lang.String, java.lang.Byte> _iter54 : struct.badTServers.entrySet()) {
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
                    for (java.lang.String _iter55 : struct.serversShuttingDown) {
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
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            java.util.BitSet incoming = iprot.readBitSet(9);
            if (incoming.get(0)) {
                {
                    org.apache.thrift.protocol.TMap _map58 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.tableMap = new java.util.HashMap<java.lang.String, TableInfo>(2 * _map58.size);
                    @org.apache.thrift.annotation.Nullable
                    java.lang.String _key59;
                    @org.apache.thrift.annotation.Nullable
                    TableInfo _val60;
                    for (int _i61 = 0; _i61 < _map58.size; ++_i61) {
                        _key59 = iprot.readString();
                        _val60 = new TableInfo();
                        _val60.read(iprot);
                        struct.tableMap.put(_key59, _val60);
                    }
                }
                struct.setTableMapIsSet(true);
            }
            if (incoming.get(1)) {
                {
                    org.apache.thrift.protocol.TList _list62 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.tServerInfo = new java.util.ArrayList<TabletServerStatus>(_list62.size);
                    @org.apache.thrift.annotation.Nullable
                    TabletServerStatus _elem63;
                    for (int _i64 = 0; _i64 < _list62.size; ++_i64) {
                        _elem63 = new TabletServerStatus();
                        _elem63.read(iprot);
                        struct.tServerInfo.add(_elem63);
                    }
                }
                struct.setTServerInfoIsSet(true);
            }
            if (incoming.get(2)) {
                {
                    org.apache.thrift.protocol.TMap _map65 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.BYTE, iprot.readI32());
                    struct.badTServers = new java.util.HashMap<java.lang.String, java.lang.Byte>(2 * _map65.size);
                    @org.apache.thrift.annotation.Nullable
                    java.lang.String _key66;
                    byte _val67;
                    for (int _i68 = 0; _i68 < _map65.size; ++_i68) {
                        _key66 = iprot.readString();
                        _val67 = iprot.readByte();
                        struct.badTServers.put(_key66, _val67);
                    }
                }
                struct.setBadTServersIsSet(true);
            }
            if (incoming.get(3)) {
                struct.state = org.apache.accumulo.core.master.thrift.MasterState.findByValue(iprot.readI32());
                struct.setStateIsSet(true);
            }
            if (incoming.get(4)) {
                struct.goalState = org.apache.accumulo.core.master.thrift.MasterGoalState.findByValue(iprot.readI32());
                struct.setGoalStateIsSet(true);
            }
            if (incoming.get(5)) {
                struct.unassignedTablets = iprot.readI32();
                struct.setUnassignedTabletsIsSet(true);
            }
            if (incoming.get(6)) {
                {
                    org.apache.thrift.protocol.TSet _set69 = new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.serversShuttingDown = new java.util.HashSet<java.lang.String>(2 * _set69.size);
                    @org.apache.thrift.annotation.Nullable
                    java.lang.String _elem70;
                    for (int _i71 = 0; _i71 < _set69.size; ++_i71) {
                        _elem70 = iprot.readString();
                        struct.serversShuttingDown.add(_elem70);
                    }
                }
                struct.setServersShuttingDownIsSet(true);
            }
            if (incoming.get(7)) {
                {
                    org.apache.thrift.protocol.TList _list72 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.deadTabletServers = new java.util.ArrayList<DeadServer>(_list72.size);
                    @org.apache.thrift.annotation.Nullable
                    DeadServer _elem73;
                    for (int _i74 = 0; _i74 < _list72.size; ++_i74) {
                        _elem73 = new DeadServer();
                        _elem73.read(iprot);
                        struct.deadTabletServers.add(_elem73);
                    }
                }
                struct.setDeadTabletServersIsSet(true);
            }
            if (incoming.get(8)) {
                {
                    org.apache.thrift.protocol.TList _list75 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.bulkImports = new java.util.ArrayList<BulkImportStatus>(_list75.size);
                    @org.apache.thrift.annotation.Nullable
                    BulkImportStatus _elem76;
                    for (int _i77 = 0; _i77 < _list75.size; ++_i77) {
                        _elem76 = new BulkImportStatus();
                        _elem76.read(iprot);
                        struct.bulkImports.add(_elem76);
                    }
                }
                struct.setBulkImportsIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }

    private static void unusedMethod() {
    }
}