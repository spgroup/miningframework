package org.apache.accumulo.proxy.thrift;

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

@SuppressWarnings("all")
public class ActiveScan implements org.apache.thrift.TBase<ActiveScan, ActiveScan._Fields>, java.io.Serializable, Cloneable, Comparable<ActiveScan> {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ActiveScan");

    private static final org.apache.thrift.protocol.TField CLIENT_FIELD_DESC = new org.apache.thrift.protocol.TField("client", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final org.apache.thrift.protocol.TField USER_FIELD_DESC = new org.apache.thrift.protocol.TField("user", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.protocol.TField TABLE_FIELD_DESC = new org.apache.thrift.protocol.TField("table", org.apache.thrift.protocol.TType.STRING, (short) 3);

    private static final org.apache.thrift.protocol.TField AGE_FIELD_DESC = new org.apache.thrift.protocol.TField("age", org.apache.thrift.protocol.TType.I64, (short) 4);

    private static final org.apache.thrift.protocol.TField IDLE_TIME_FIELD_DESC = new org.apache.thrift.protocol.TField("idleTime", org.apache.thrift.protocol.TType.I64, (short) 5);

    private static final org.apache.thrift.protocol.TField TYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("type", org.apache.thrift.protocol.TType.I32, (short) 6);

    private static final org.apache.thrift.protocol.TField STATE_FIELD_DESC = new org.apache.thrift.protocol.TField("state", org.apache.thrift.protocol.TType.I32, (short) 7);

    private static final org.apache.thrift.protocol.TField EXTENT_FIELD_DESC = new org.apache.thrift.protocol.TField("extent", org.apache.thrift.protocol.TType.STRUCT, (short) 8);

    private static final org.apache.thrift.protocol.TField COLUMNS_FIELD_DESC = new org.apache.thrift.protocol.TField("columns", org.apache.thrift.protocol.TType.LIST, (short) 9);

    private static final org.apache.thrift.protocol.TField ITERATORS_FIELD_DESC = new org.apache.thrift.protocol.TField("iterators", org.apache.thrift.protocol.TType.LIST, (short) 10);

    private static final org.apache.thrift.protocol.TField AUTHORIZATIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("authorizations", org.apache.thrift.protocol.TType.LIST, (short) 11);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new ActiveScanStandardSchemeFactory());
        schemes.put(TupleScheme.class, new ActiveScanTupleSchemeFactory());
    }

    public String client;

    public String user;

    public String table;

    public long age;

    public long idleTime;

    public ScanType type;

    public ScanState state;

    public KeyExtent extent;

    public List<Column> columns;

    public List<IteratorSetting> iterators;

    public List<ByteBuffer> authorizations;

    @SuppressWarnings("all")
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        CLIENT((short) 1, "client"),
        USER((short) 2, "user"),
        TABLE((short) 3, "table"),
        AGE((short) 4, "age"),
        IDLE_TIME((short) 5, "idleTime"),
        TYPE((short) 6, "type"),
        STATE((short) 7, "state"),
        EXTENT((short) 8, "extent"),
        COLUMNS((short) 9, "columns"),
        ITERATORS((short) 10, "iterators"),
        AUTHORIZATIONS((short) 11, "authorizations");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return CLIENT;
                case 2:
                    return USER;
                case 3:
                    return TABLE;
                case 4:
                    return AGE;
                case 5:
                    return IDLE_TIME;
                case 6:
                    return TYPE;
                case 7:
                    return STATE;
                case 8:
                    return EXTENT;
                case 9:
                    return COLUMNS;
                case 10:
                    return ITERATORS;
                case 11:
                    return AUTHORIZATIONS;
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

    private byte __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.CLIENT, new org.apache.thrift.meta_data.FieldMetaData("client", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.USER, new org.apache.thrift.meta_data.FieldMetaData("user", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.TABLE, new org.apache.thrift.meta_data.FieldMetaData("table", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.AGE, new org.apache.thrift.meta_data.FieldMetaData("age", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.IDLE_TIME, new org.apache.thrift.meta_data.FieldMetaData("idleTime", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.TYPE, new org.apache.thrift.meta_data.FieldMetaData("type", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, ScanType.class)));
        tmpMap.put(_Fields.STATE, new org.apache.thrift.meta_data.FieldMetaData("state", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, ScanState.class)));
        tmpMap.put(_Fields.EXTENT, new org.apache.thrift.meta_data.FieldMetaData("extent", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, KeyExtent.class)));
        tmpMap.put(_Fields.COLUMNS, new org.apache.thrift.meta_data.FieldMetaData("columns", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Column.class))));
        tmpMap.put(_Fields.ITERATORS, new org.apache.thrift.meta_data.FieldMetaData("iterators", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, IteratorSetting.class))));
        tmpMap.put(_Fields.AUTHORIZATIONS, new org.apache.thrift.meta_data.FieldMetaData("authorizations", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true))));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ActiveScan.class, metaDataMap);
    }

    public ActiveScan() {
    }

    public ActiveScan(String client, String user, String table, long age, long idleTime, ScanType type, ScanState state, KeyExtent extent, List<Column> columns, List<IteratorSetting> iterators, List<ByteBuffer> authorizations) {
        this();
        this.client = client;
        this.user = user;
        this.table = table;
        this.age = age;
        setAgeIsSet(true);
        this.idleTime = idleTime;
        setIdleTimeIsSet(true);
        this.type = type;
        this.state = state;
        this.extent = extent;
        this.columns = columns;
        this.iterators = iterators;
        this.authorizations = authorizations;
    }

    public ActiveScan(ActiveScan other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetClient()) {
            this.client = other.client;
        }
        if (other.isSetUser()) {
            this.user = other.user;
        }
        if (other.isSetTable()) {
            this.table = other.table;
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
            this.extent = new KeyExtent(other.extent);
        }
        if (other.isSetColumns()) {
            List<Column> __this__columns = new ArrayList<Column>(other.columns.size());
            for (Column other_element : other.columns) {
                __this__columns.add(new Column(other_element));
            }
            this.columns = __this__columns;
        }
        if (other.isSetIterators()) {
            List<IteratorSetting> __this__iterators = new ArrayList<IteratorSetting>(other.iterators.size());
            for (IteratorSetting other_element : other.iterators) {
                __this__iterators.add(new IteratorSetting(other_element));
            }
            this.iterators = __this__iterators;
        }
        if (other.isSetAuthorizations()) {
            List<ByteBuffer> __this__authorizations = new ArrayList<ByteBuffer>(other.authorizations);
            this.authorizations = __this__authorizations;
        }
    }

    public ActiveScan deepCopy() {
        return new ActiveScan(this);
    }

    @Override
    public void clear() {
        this.client = null;
        this.user = null;
        this.table = null;
        setAgeIsSet(false);
        this.age = 0;
        setIdleTimeIsSet(false);
        this.idleTime = 0;
        this.type = null;
        this.state = null;
        this.extent = null;
        this.columns = null;
        this.iterators = null;
        this.authorizations = null;
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

    public String getTable() {
        return this.table;
    }

    public ActiveScan setTable(String table) {
        this.table = table;
        return this;
    }

    public void unsetTable() {
        this.table = null;
    }

    public boolean isSetTable() {
        return this.table != null;
    }

    public void setTableIsSet(boolean value) {
        if (!value) {
            this.table = null;
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

    public KeyExtent getExtent() {
        return this.extent;
    }

    public ActiveScan setExtent(KeyExtent extent) {
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

    public java.util.Iterator<Column> getColumnsIterator() {
        return (this.columns == null) ? null : this.columns.iterator();
    }

    public void addToColumns(Column elem) {
        if (this.columns == null) {
            this.columns = new ArrayList<Column>();
        }
        this.columns.add(elem);
    }

    public List<Column> getColumns() {
        return this.columns;
    }

    public ActiveScan setColumns(List<Column> columns) {
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

    public int getIteratorsSize() {
        return (this.iterators == null) ? 0 : this.iterators.size();
    }

    public java.util.Iterator<IteratorSetting> getIteratorsIterator() {
        return (this.iterators == null) ? null : this.iterators.iterator();
    }

    public void addToIterators(IteratorSetting elem) {
        if (this.iterators == null) {
            this.iterators = new ArrayList<IteratorSetting>();
        }
        this.iterators.add(elem);
    }

    public List<IteratorSetting> getIterators() {
        return this.iterators;
    }

    public ActiveScan setIterators(List<IteratorSetting> iterators) {
        this.iterators = iterators;
        return this;
    }

    public void unsetIterators() {
        this.iterators = null;
    }

    public boolean isSetIterators() {
        return this.iterators != null;
    }

    public void setIteratorsIsSet(boolean value) {
        if (!value) {
            this.iterators = null;
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
            case TABLE:
                if (value == null) {
                    unsetTable();
                } else {
                    setTable((String) value);
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
                    setExtent((KeyExtent) value);
                }
                break;
            case COLUMNS:
                if (value == null) {
                    unsetColumns();
                } else {
                    setColumns((List<Column>) value);
                }
                break;
            case ITERATORS:
                if (value == null) {
                    unsetIterators();
                } else {
                    setIterators((List<IteratorSetting>) value);
                }
                break;
            case AUTHORIZATIONS:
                if (value == null) {
                    unsetAuthorizations();
                } else {
                    setAuthorizations((List<ByteBuffer>) value);
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
            case TABLE:
                return getTable();
            case AGE:
                return Long.valueOf(getAge());
            case IDLE_TIME:
                return Long.valueOf(getIdleTime());
            case TYPE:
                return getType();
            case STATE:
                return getState();
            case EXTENT:
                return getExtent();
            case COLUMNS:
                return getColumns();
            case ITERATORS:
                return getIterators();
            case AUTHORIZATIONS:
                return getAuthorizations();
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
            case TABLE:
                return isSetTable();
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
            case ITERATORS:
                return isSetIterators();
            case AUTHORIZATIONS:
                return isSetAuthorizations();
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
        boolean this_present_table = true && this.isSetTable();
        boolean that_present_table = true && that.isSetTable();
        if (this_present_table || that_present_table) {
            if (!(this_present_table && that_present_table))
                return false;
            if (!this.table.equals(that.table))
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
        boolean this_present_iterators = true && this.isSetIterators();
        boolean that_present_iterators = true && that.isSetIterators();
        if (this_present_iterators || that_present_iterators) {
            if (!(this_present_iterators && that_present_iterators))
                return false;
            if (!this.iterators.equals(that.iterators))
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
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
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
        lastComparison = Boolean.valueOf(isSetTable()).compareTo(other.isSetTable());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetTable()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.table, other.table);
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
        lastComparison = Boolean.valueOf(isSetIterators()).compareTo(other.isSetIterators());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetIterators()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.iterators, other.iterators);
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
        sb.append("table:");
        if (this.table == null) {
            sb.append("null");
        } else {
            sb.append(this.table);
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
        sb.append("iterators:");
        if (this.iterators == null) {
            sb.append("null");
        } else {
            sb.append(this.iterators);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("authorizations:");
        if (this.authorizations == null) {
            sb.append("null");
        } else {
            sb.append(this.authorizations);
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
                    case 1:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.client = iprot.readString();
                            struct.setClientIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.user = iprot.readString();
                            struct.setUserIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.table = iprot.readString();
                            struct.setTableIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.age = iprot.readI64();
                            struct.setAgeIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 5:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.idleTime = iprot.readI64();
                            struct.setIdleTimeIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 6:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.type = ScanType.findByValue(iprot.readI32());
                            struct.setTypeIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 7:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.state = ScanState.findByValue(iprot.readI32());
                            struct.setStateIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 8:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.extent = new KeyExtent();
                            struct.extent.read(iprot);
                            struct.setExtentIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 9:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list114 = iprot.readListBegin();
                                struct.columns = new ArrayList<Column>(_list114.size);
                                for (int _i115 = 0; _i115 < _list114.size; ++_i115) {
                                    Column _elem116;
                                    _elem116 = new Column();
                                    _elem116.read(iprot);
                                    struct.columns.add(_elem116);
                                }
                                iprot.readListEnd();
                            }
                            struct.setColumnsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 10:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list117 = iprot.readListBegin();
                                struct.iterators = new ArrayList<IteratorSetting>(_list117.size);
                                for (int _i118 = 0; _i118 < _list117.size; ++_i118) {
                                    IteratorSetting _elem119;
                                    _elem119 = new IteratorSetting();
                                    _elem119.read(iprot);
                                    struct.iterators.add(_elem119);
                                }
                                iprot.readListEnd();
                            }
                            struct.setIteratorsIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 11:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list120 = iprot.readListBegin();
                                struct.authorizations = new ArrayList<ByteBuffer>(_list120.size);
                                for (int _i121 = 0; _i121 < _list120.size; ++_i121) {
                                    ByteBuffer _elem122;
                                    _elem122 = iprot.readBinary();
                                    struct.authorizations.add(_elem122);
                                }
                                iprot.readListEnd();
                            }
                            struct.setAuthorizationsIsSet(true);
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
            if (struct.table != null) {
                oprot.writeFieldBegin(TABLE_FIELD_DESC);
                oprot.writeString(struct.table);
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
                    for (Column _iter123 : struct.columns) {
                        _iter123.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.iterators != null) {
                oprot.writeFieldBegin(ITERATORS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.iterators.size()));
                    for (IteratorSetting _iter124 : struct.iterators) {
                        _iter124.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.authorizations != null) {
                oprot.writeFieldBegin(AUTHORIZATIONS_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.authorizations.size()));
                    for (ByteBuffer _iter125 : struct.authorizations) {
                        oprot.writeBinary(_iter125);
                    }
                    oprot.writeListEnd();
                }
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
            if (struct.isSetTable()) {
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
            if (struct.isSetIterators()) {
                optionals.set(9);
            }
            if (struct.isSetAuthorizations()) {
                optionals.set(10);
            }
            oprot.writeBitSet(optionals, 11);
            if (struct.isSetClient()) {
                oprot.writeString(struct.client);
            }
            if (struct.isSetUser()) {
                oprot.writeString(struct.user);
            }
            if (struct.isSetTable()) {
                oprot.writeString(struct.table);
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
                    for (Column _iter126 : struct.columns) {
                        _iter126.write(oprot);
                    }
                }
            }
            if (struct.isSetIterators()) {
                {
                    oprot.writeI32(struct.iterators.size());
                    for (IteratorSetting _iter127 : struct.iterators) {
                        _iter127.write(oprot);
                    }
                }
            }
            if (struct.isSetAuthorizations()) {
                {
                    oprot.writeI32(struct.authorizations.size());
                    for (ByteBuffer _iter128 : struct.authorizations) {
                        oprot.writeBinary(_iter128);
                    }
                }
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, ActiveScan struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(11);
            if (incoming.get(0)) {
                struct.client = iprot.readString();
                struct.setClientIsSet(true);
            }
            if (incoming.get(1)) {
                struct.user = iprot.readString();
                struct.setUserIsSet(true);
            }
            if (incoming.get(2)) {
                struct.table = iprot.readString();
                struct.setTableIsSet(true);
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
                struct.type = ScanType.findByValue(iprot.readI32());
                struct.setTypeIsSet(true);
            }
            if (incoming.get(6)) {
                struct.state = ScanState.findByValue(iprot.readI32());
                struct.setStateIsSet(true);
            }
            if (incoming.get(7)) {
                struct.extent = new KeyExtent();
                struct.extent.read(iprot);
                struct.setExtentIsSet(true);
            }
            if (incoming.get(8)) {
                {
                    org.apache.thrift.protocol.TList _list129 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.columns = new ArrayList<Column>(_list129.size);
                    for (int _i130 = 0; _i130 < _list129.size; ++_i130) {
                        Column _elem131;
                        _elem131 = new Column();
                        _elem131.read(iprot);
                        struct.columns.add(_elem131);
                    }
                }
                struct.setColumnsIsSet(true);
            }
            if (incoming.get(9)) {
                {
                    org.apache.thrift.protocol.TList _list132 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.iterators = new ArrayList<IteratorSetting>(_list132.size);
                    for (int _i133 = 0; _i133 < _list132.size; ++_i133) {
                        IteratorSetting _elem134;
                        _elem134 = new IteratorSetting();
                        _elem134.read(iprot);
                        struct.iterators.add(_elem134);
                    }
                }
                struct.setIteratorsIsSet(true);
            }
            if (incoming.get(10)) {
                {
                    org.apache.thrift.protocol.TList _list135 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.authorizations = new ArrayList<ByteBuffer>(_list135.size);
                    for (int _i136 = 0; _i136 < _list135.size; ++_i136) {
                        ByteBuffer _elem137;
                        _elem137 = iprot.readBinary();
                        struct.authorizations.add(_elem137);
                    }
                }
                struct.setAuthorizationsIsSet(true);
            }
        }
    }
}
