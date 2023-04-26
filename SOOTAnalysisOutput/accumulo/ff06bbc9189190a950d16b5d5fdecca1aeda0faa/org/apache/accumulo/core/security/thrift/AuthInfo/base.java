package org.apache.accumulo.core.security.thrift;

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
public class AuthInfo implements org.apache.thrift.TBase<AuthInfo, AuthInfo._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("AuthInfo");

    private static final org.apache.thrift.protocol.TField USER_FIELD_DESC = new org.apache.thrift.protocol.TField("user", org.apache.thrift.protocol.TType.STRING, (short) 1);

    private static final org.apache.thrift.protocol.TField PASSWORD_FIELD_DESC = new org.apache.thrift.protocol.TField("password", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.protocol.TField INSTANCE_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("instanceId", org.apache.thrift.protocol.TType.STRING, (short) 3);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new AuthInfoStandardSchemeFactory());
        schemes.put(TupleScheme.class, new AuthInfoTupleSchemeFactory());
    }

    public String user;

    public ByteBuffer password;

    public String instanceId;

    @SuppressWarnings("all")
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        USER((short) 1, "user"), PASSWORD((short) 2, "password"), INSTANCE_ID((short) 3, "instanceId");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return USER;
                case 2:
                    return PASSWORD;
                case 3:
                    return INSTANCE_ID;
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

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.USER, new org.apache.thrift.meta_data.FieldMetaData("user", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.PASSWORD, new org.apache.thrift.meta_data.FieldMetaData("password", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
        tmpMap.put(_Fields.INSTANCE_ID, new org.apache.thrift.meta_data.FieldMetaData("instanceId", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(AuthInfo.class, metaDataMap);
    }

    public AuthInfo() {
    }

    public AuthInfo(String user, ByteBuffer password, String instanceId) {
        this();
        this.user = user;
        this.password = password;
        this.instanceId = instanceId;
    }

    public AuthInfo(AuthInfo other) {
        if (other.isSetUser()) {
            this.user = other.user;
        }
        if (other.isSetPassword()) {
            this.password = org.apache.thrift.TBaseHelper.copyBinary(other.password);
            ;
        }
        if (other.isSetInstanceId()) {
            this.instanceId = other.instanceId;
        }
    }

    public AuthInfo deepCopy() {
        return new AuthInfo(this);
    }

    @Override
    public void clear() {
        this.user = null;
        this.password = null;
        this.instanceId = null;
    }

    public String getUser() {
        return this.user;
    }

    public AuthInfo setUser(String user) {
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

    public byte[] getPassword() {
        setPassword(org.apache.thrift.TBaseHelper.rightSize(password));
        return password == null ? null : password.array();
    }

    public ByteBuffer bufferForPassword() {
        return password;
    }

    public AuthInfo setPassword(byte[] password) {
        setPassword(password == null ? (ByteBuffer) null : ByteBuffer.wrap(password));
        return this;
    }

    public AuthInfo setPassword(ByteBuffer password) {
        this.password = password;
        return this;
    }

    public void unsetPassword() {
        this.password = null;
    }

    public boolean isSetPassword() {
        return this.password != null;
    }

    public void setPasswordIsSet(boolean value) {
        if (!value) {
            this.password = null;
        }
    }

    public String getInstanceId() {
        return this.instanceId;
    }

    public AuthInfo setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public void unsetInstanceId() {
        this.instanceId = null;
    }

    public boolean isSetInstanceId() {
        return this.instanceId != null;
    }

    public void setInstanceIdIsSet(boolean value) {
        if (!value) {
            this.instanceId = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case USER:
                if (value == null) {
                    unsetUser();
                } else {
                    setUser((String) value);
                }
                break;
            case PASSWORD:
                if (value == null) {
                    unsetPassword();
                } else {
                    setPassword((ByteBuffer) value);
                }
                break;
            case INSTANCE_ID:
                if (value == null) {
                    unsetInstanceId();
                } else {
                    setInstanceId((String) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case USER:
                return getUser();
            case PASSWORD:
                return getPassword();
            case INSTANCE_ID:
                return getInstanceId();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case USER:
                return isSetUser();
            case PASSWORD:
                return isSetPassword();
            case INSTANCE_ID:
                return isSetInstanceId();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof AuthInfo)
            return this.equals((AuthInfo) that);
        return false;
    }

    public boolean equals(AuthInfo that) {
        if (that == null)
            return false;
        boolean this_present_user = true && this.isSetUser();
        boolean that_present_user = true && that.isSetUser();
        if (this_present_user || that_present_user) {
            if (!(this_present_user && that_present_user))
                return false;
            if (!this.user.equals(that.user))
                return false;
        }
        boolean this_present_password = true && this.isSetPassword();
        boolean that_present_password = true && that.isSetPassword();
        if (this_present_password || that_present_password) {
            if (!(this_present_password && that_present_password))
                return false;
            if (!this.password.equals(that.password))
                return false;
        }
        boolean this_present_instanceId = true && this.isSetInstanceId();
        boolean that_present_instanceId = true && that.isSetInstanceId();
        if (this_present_instanceId || that_present_instanceId) {
            if (!(this_present_instanceId && that_present_instanceId))
                return false;
            if (!this.instanceId.equals(that.instanceId))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public int compareTo(AuthInfo other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        AuthInfo typedOther = (AuthInfo) other;
        lastComparison = Boolean.valueOf(isSetUser()).compareTo(typedOther.isSetUser());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetUser()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.user, typedOther.user);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetPassword()).compareTo(typedOther.isSetPassword());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetPassword()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.password, typedOther.password);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetInstanceId()).compareTo(typedOther.isSetInstanceId());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetInstanceId()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.instanceId, typedOther.instanceId);
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
        StringBuilder sb = new StringBuilder("AuthInfo(");
        boolean first = true;
        sb.append("user:");
        if (this.user == null) {
            sb.append("null");
        } else {
            sb.append(this.user);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("password:");
        if (this.password == null) {
            sb.append("null");
        } else {
            org.apache.thrift.TBaseHelper.toString(this.password, sb);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("instanceId:");
        if (this.instanceId == null) {
            sb.append("null");
        } else {
            sb.append(this.instanceId);
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
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class AuthInfoStandardSchemeFactory implements SchemeFactory {

        public AuthInfoStandardScheme getScheme() {
            return new AuthInfoStandardScheme();
        }
    }

    private static class AuthInfoStandardScheme extends StandardScheme<AuthInfo> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, AuthInfo struct) throws org.apache.thrift.TException {
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
                            struct.user = iprot.readString();
                            struct.setUserIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.password = iprot.readBinary();
                            struct.setPasswordIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.instanceId = iprot.readString();
                            struct.setInstanceIdIsSet(true);
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

        public void write(org.apache.thrift.protocol.TProtocol oprot, AuthInfo struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.user != null) {
                oprot.writeFieldBegin(USER_FIELD_DESC);
                oprot.writeString(struct.user);
                oprot.writeFieldEnd();
            }
            if (struct.password != null) {
                oprot.writeFieldBegin(PASSWORD_FIELD_DESC);
                oprot.writeBinary(struct.password);
                oprot.writeFieldEnd();
            }
            if (struct.instanceId != null) {
                oprot.writeFieldBegin(INSTANCE_ID_FIELD_DESC);
                oprot.writeString(struct.instanceId);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class AuthInfoTupleSchemeFactory implements SchemeFactory {

        public AuthInfoTupleScheme getScheme() {
            return new AuthInfoTupleScheme();
        }
    }

    private static class AuthInfoTupleScheme extends TupleScheme<AuthInfo> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, AuthInfo struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetUser()) {
                optionals.set(0);
            }
            if (struct.isSetPassword()) {
                optionals.set(1);
            }
            if (struct.isSetInstanceId()) {
                optionals.set(2);
            }
            oprot.writeBitSet(optionals, 3);
            if (struct.isSetUser()) {
                oprot.writeString(struct.user);
            }
            if (struct.isSetPassword()) {
                oprot.writeBinary(struct.password);
            }
            if (struct.isSetInstanceId()) {
                oprot.writeString(struct.instanceId);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, AuthInfo struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(3);
            if (incoming.get(0)) {
                struct.user = iprot.readString();
                struct.setUserIsSet(true);
            }
            if (incoming.get(1)) {
                struct.password = iprot.readBinary();
                struct.setPasswordIsSet(true);
            }
            if (incoming.get(2)) {
                struct.instanceId = iprot.readString();
                struct.setInstanceIdIsSet(true);
            }
        }
    }
}
