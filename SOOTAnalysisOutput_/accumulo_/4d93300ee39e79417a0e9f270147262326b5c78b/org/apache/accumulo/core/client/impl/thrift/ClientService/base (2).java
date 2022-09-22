package org.apache.accumulo.core.client.impl.thrift;

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
public class ClientService {

    public interface Iface {

        public String getRootTabletLocation() throws org.apache.thrift.TException;

        public String getInstanceId() throws org.apache.thrift.TException;

        public String getZooKeepers() throws org.apache.thrift.TException;

        public List<String> bulkImportFiles(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, long tid, String tableId, List<String> files, String errorDir, boolean setTime) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, ThriftTableOperationException, org.apache.thrift.TException;

        public boolean isActive(org.apache.accumulo.trace.thrift.TInfo tinfo, long tid) throws org.apache.thrift.TException;

        public void ping(org.apache.accumulo.core.security.thrift.Credential credential) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException;

        public boolean authenticateUser(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toAuthenticate) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException;

        public Set<String> listUsers(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException;

        public void createUser(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toCreate, List<ByteBuffer> authorizations) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException;

        public void dropUser(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException;

        public void changePassword(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toChange) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException;

        public void changeAuthorizations(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, List<ByteBuffer> authorizations) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException;

        public List<ByteBuffer> getUserAuthorizations(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException;

        public boolean hasSystemPermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte sysPerm) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException;

        public boolean hasTablePermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte tblPerm) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, ThriftTableOperationException, org.apache.thrift.TException;

        public void grantSystemPermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte permission) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException;

        public void revokeSystemPermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte permission) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException;

        public void grantTablePermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte permission) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, ThriftTableOperationException, org.apache.thrift.TException;

        public void revokeTablePermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte permission) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, ThriftTableOperationException, org.apache.thrift.TException;

        public Map<String, String> getConfiguration(ConfigurationType type) throws org.apache.thrift.TException;

        public Map<String, String> getTableConfiguration(String tableName) throws ThriftTableOperationException, org.apache.thrift.TException;

        public boolean checkClass(org.apache.accumulo.trace.thrift.TInfo tinfo, String className, String interfaceMatch) throws org.apache.thrift.TException;
    }

    public interface AsyncIface {

        public void getRootTabletLocation(org.apache.thrift.async.AsyncMethodCallback<AsyncClient.getRootTabletLocation_call> resultHandler) throws org.apache.thrift.TException;

        public void getInstanceId(org.apache.thrift.async.AsyncMethodCallback<AsyncClient.getInstanceId_call> resultHandler) throws org.apache.thrift.TException;

        public void getZooKeepers(org.apache.thrift.async.AsyncMethodCallback<AsyncClient.getZooKeepers_call> resultHandler) throws org.apache.thrift.TException;

        public void bulkImportFiles(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, long tid, String tableId, List<String> files, String errorDir, boolean setTime, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.bulkImportFiles_call> resultHandler) throws org.apache.thrift.TException;

        public void isActive(org.apache.accumulo.trace.thrift.TInfo tinfo, long tid, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.isActive_call> resultHandler) throws org.apache.thrift.TException;

        public void ping(org.apache.accumulo.core.security.thrift.Credential credential, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.ping_call> resultHandler) throws org.apache.thrift.TException;

        public void authenticateUser(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toAuthenticate, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.authenticateUser_call> resultHandler) throws org.apache.thrift.TException;

        public void listUsers(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.listUsers_call> resultHandler) throws org.apache.thrift.TException;

        public void createUser(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toCreate, List<ByteBuffer> authorizations, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.createUser_call> resultHandler) throws org.apache.thrift.TException;

        public void dropUser(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.dropUser_call> resultHandler) throws org.apache.thrift.TException;

        public void changePassword(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toChange, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.changePassword_call> resultHandler) throws org.apache.thrift.TException;

        public void changeAuthorizations(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, List<ByteBuffer> authorizations, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.changeAuthorizations_call> resultHandler) throws org.apache.thrift.TException;

        public void getUserAuthorizations(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.getUserAuthorizations_call> resultHandler) throws org.apache.thrift.TException;

        public void hasSystemPermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte sysPerm, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.hasSystemPermission_call> resultHandler) throws org.apache.thrift.TException;

        public void hasTablePermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte tblPerm, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.hasTablePermission_call> resultHandler) throws org.apache.thrift.TException;

        public void grantSystemPermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte permission, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.grantSystemPermission_call> resultHandler) throws org.apache.thrift.TException;

        public void revokeSystemPermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte permission, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.revokeSystemPermission_call> resultHandler) throws org.apache.thrift.TException;

        public void grantTablePermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte permission, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.grantTablePermission_call> resultHandler) throws org.apache.thrift.TException;

        public void revokeTablePermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte permission, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.revokeTablePermission_call> resultHandler) throws org.apache.thrift.TException;

        public void getConfiguration(ConfigurationType type, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.getConfiguration_call> resultHandler) throws org.apache.thrift.TException;

        public void getTableConfiguration(String tableName, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.getTableConfiguration_call> resultHandler) throws org.apache.thrift.TException;

        public void checkClass(org.apache.accumulo.trace.thrift.TInfo tinfo, String className, String interfaceMatch, org.apache.thrift.async.AsyncMethodCallback<AsyncClient.checkClass_call> resultHandler) throws org.apache.thrift.TException;
    }

    public static class Client extends org.apache.thrift.TServiceClient implements Iface {

        public static class Factory implements org.apache.thrift.TServiceClientFactory<Client> {

            public Factory() {
            }

            public Client getClient(org.apache.thrift.protocol.TProtocol prot) {
                return new Client(prot);
            }

            public Client getClient(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) {
                return new Client(iprot, oprot);
            }
        }

        public Client(org.apache.thrift.protocol.TProtocol prot) {
            super(prot, prot);
        }

        public Client(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) {
            super(iprot, oprot);
        }

        public String getRootTabletLocation() throws org.apache.thrift.TException {
            send_getRootTabletLocation();
            return recv_getRootTabletLocation();
        }

        public void send_getRootTabletLocation() throws org.apache.thrift.TException {
            getRootTabletLocation_args args = new getRootTabletLocation_args();
            sendBase("getRootTabletLocation", args);
        }

        public String recv_getRootTabletLocation() throws org.apache.thrift.TException {
            getRootTabletLocation_result result = new getRootTabletLocation_result();
            receiveBase(result, "getRootTabletLocation");
            if (result.isSetSuccess()) {
                return result.success;
            }
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "getRootTabletLocation failed: unknown result");
        }

        public String getInstanceId() throws org.apache.thrift.TException {
            send_getInstanceId();
            return recv_getInstanceId();
        }

        public void send_getInstanceId() throws org.apache.thrift.TException {
            getInstanceId_args args = new getInstanceId_args();
            sendBase("getInstanceId", args);
        }

        public String recv_getInstanceId() throws org.apache.thrift.TException {
            getInstanceId_result result = new getInstanceId_result();
            receiveBase(result, "getInstanceId");
            if (result.isSetSuccess()) {
                return result.success;
            }
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "getInstanceId failed: unknown result");
        }

        public String getZooKeepers() throws org.apache.thrift.TException {
            send_getZooKeepers();
            return recv_getZooKeepers();
        }

        public void send_getZooKeepers() throws org.apache.thrift.TException {
            getZooKeepers_args args = new getZooKeepers_args();
            sendBase("getZooKeepers", args);
        }

        public String recv_getZooKeepers() throws org.apache.thrift.TException {
            getZooKeepers_result result = new getZooKeepers_result();
            receiveBase(result, "getZooKeepers");
            if (result.isSetSuccess()) {
                return result.success;
            }
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "getZooKeepers failed: unknown result");
        }

        public List<String> bulkImportFiles(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, long tid, String tableId, List<String> files, String errorDir, boolean setTime) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, ThriftTableOperationException, org.apache.thrift.TException {
            send_bulkImportFiles(tinfo, credential, tid, tableId, files, errorDir, setTime);
            return recv_bulkImportFiles();
        }

        public void send_bulkImportFiles(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, long tid, String tableId, List<String> files, String errorDir, boolean setTime) throws org.apache.thrift.TException {
            bulkImportFiles_args args = new bulkImportFiles_args();
            args.setTinfo(tinfo);
            args.setCredential(credential);
            args.setTid(tid);
            args.setTableId(tableId);
            args.setFiles(files);
            args.setErrorDir(errorDir);
            args.setSetTime(setTime);
            sendBase("bulkImportFiles", args);
        }

        public List<String> recv_bulkImportFiles() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, ThriftTableOperationException, org.apache.thrift.TException {
            bulkImportFiles_result result = new bulkImportFiles_result();
            receiveBase(result, "bulkImportFiles");
            if (result.isSetSuccess()) {
                return result.success;
            }
            if (result.sec != null) {
                throw result.sec;
            }
            if (result.tope != null) {
                throw result.tope;
            }
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "bulkImportFiles failed: unknown result");
        }

        public boolean isActive(org.apache.accumulo.trace.thrift.TInfo tinfo, long tid) throws org.apache.thrift.TException {
            send_isActive(tinfo, tid);
            return recv_isActive();
        }

        public void send_isActive(org.apache.accumulo.trace.thrift.TInfo tinfo, long tid) throws org.apache.thrift.TException {
            isActive_args args = new isActive_args();
            args.setTinfo(tinfo);
            args.setTid(tid);
            sendBase("isActive", args);
        }

        public boolean recv_isActive() throws org.apache.thrift.TException {
            isActive_result result = new isActive_result();
            receiveBase(result, "isActive");
            if (result.isSetSuccess()) {
                return result.success;
            }
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "isActive failed: unknown result");
        }

        public void ping(org.apache.accumulo.core.security.thrift.Credential credential) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            send_ping(credential);
            recv_ping();
        }

        public void send_ping(org.apache.accumulo.core.security.thrift.Credential credential) throws org.apache.thrift.TException {
            ping_args args = new ping_args();
            args.setCredential(credential);
            sendBase("ping", args);
        }

        public void recv_ping() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            ping_result result = new ping_result();
            receiveBase(result, "ping");
            if (result.sec != null) {
                throw result.sec;
            }
            return;
        }

        public boolean authenticateUser(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toAuthenticate) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            send_authenticateUser(tinfo, credential, toAuthenticate);
            return recv_authenticateUser();
        }

        public void send_authenticateUser(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toAuthenticate) throws org.apache.thrift.TException {
            authenticateUser_args args = new authenticateUser_args();
            args.setTinfo(tinfo);
            args.setCredential(credential);
            args.setToAuthenticate(toAuthenticate);
            sendBase("authenticateUser", args);
        }

        public boolean recv_authenticateUser() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            authenticateUser_result result = new authenticateUser_result();
            receiveBase(result, "authenticateUser");
            if (result.isSetSuccess()) {
                return result.success;
            }
            if (result.sec != null) {
                throw result.sec;
            }
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "authenticateUser failed: unknown result");
        }

        public Set<String> listUsers(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            send_listUsers(tinfo, credential);
            return recv_listUsers();
        }

        public void send_listUsers(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential) throws org.apache.thrift.TException {
            listUsers_args args = new listUsers_args();
            args.setTinfo(tinfo);
            args.setCredential(credential);
            sendBase("listUsers", args);
        }

        public Set<String> recv_listUsers() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            listUsers_result result = new listUsers_result();
            receiveBase(result, "listUsers");
            if (result.isSetSuccess()) {
                return result.success;
            }
            if (result.sec != null) {
                throw result.sec;
            }
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "listUsers failed: unknown result");
        }

        public void createUser(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toCreate, List<ByteBuffer> authorizations) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            send_createUser(tinfo, credential, toCreate, authorizations);
            recv_createUser();
        }

        public void send_createUser(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toCreate, List<ByteBuffer> authorizations) throws org.apache.thrift.TException {
            createUser_args args = new createUser_args();
            args.setTinfo(tinfo);
            args.setCredential(credential);
            args.setToCreate(toCreate);
            args.setAuthorizations(authorizations);
            sendBase("createUser", args);
        }

        public void recv_createUser() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            createUser_result result = new createUser_result();
            receiveBase(result, "createUser");
            if (result.sec != null) {
                throw result.sec;
            }
            return;
        }

        public void dropUser(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            send_dropUser(tinfo, credential, principal);
            recv_dropUser();
        }

        public void send_dropUser(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal) throws org.apache.thrift.TException {
            dropUser_args args = new dropUser_args();
            args.setTinfo(tinfo);
            args.setCredential(credential);
            args.setPrincipal(principal);
            sendBase("dropUser", args);
        }

        public void recv_dropUser() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            dropUser_result result = new dropUser_result();
            receiveBase(result, "dropUser");
            if (result.sec != null) {
                throw result.sec;
            }
            return;
        }

        public void changePassword(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toChange) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            send_changePassword(tinfo, credential, toChange);
            recv_changePassword();
        }

        public void send_changePassword(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toChange) throws org.apache.thrift.TException {
            changePassword_args args = new changePassword_args();
            args.setTinfo(tinfo);
            args.setCredential(credential);
            args.setToChange(toChange);
            sendBase("changePassword", args);
        }

        public void recv_changePassword() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            changePassword_result result = new changePassword_result();
            receiveBase(result, "changePassword");
            if (result.sec != null) {
                throw result.sec;
            }
            return;
        }

        public void changeAuthorizations(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, List<ByteBuffer> authorizations) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            send_changeAuthorizations(tinfo, credential, principal, authorizations);
            recv_changeAuthorizations();
        }

        public void send_changeAuthorizations(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, List<ByteBuffer> authorizations) throws org.apache.thrift.TException {
            changeAuthorizations_args args = new changeAuthorizations_args();
            args.setTinfo(tinfo);
            args.setCredential(credential);
            args.setPrincipal(principal);
            args.setAuthorizations(authorizations);
            sendBase("changeAuthorizations", args);
        }

        public void recv_changeAuthorizations() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            changeAuthorizations_result result = new changeAuthorizations_result();
            receiveBase(result, "changeAuthorizations");
            if (result.sec != null) {
                throw result.sec;
            }
            return;
        }

        public List<ByteBuffer> getUserAuthorizations(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            send_getUserAuthorizations(tinfo, credential, principal);
            return recv_getUserAuthorizations();
        }

        public void send_getUserAuthorizations(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal) throws org.apache.thrift.TException {
            getUserAuthorizations_args args = new getUserAuthorizations_args();
            args.setTinfo(tinfo);
            args.setCredential(credential);
            args.setPrincipal(principal);
            sendBase("getUserAuthorizations", args);
        }

        public List<ByteBuffer> recv_getUserAuthorizations() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            getUserAuthorizations_result result = new getUserAuthorizations_result();
            receiveBase(result, "getUserAuthorizations");
            if (result.isSetSuccess()) {
                return result.success;
            }
            if (result.sec != null) {
                throw result.sec;
            }
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "getUserAuthorizations failed: unknown result");
        }

        public boolean hasSystemPermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte sysPerm) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            send_hasSystemPermission(tinfo, credential, principal, sysPerm);
            return recv_hasSystemPermission();
        }

        public void send_hasSystemPermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte sysPerm) throws org.apache.thrift.TException {
            hasSystemPermission_args args = new hasSystemPermission_args();
            args.setTinfo(tinfo);
            args.setCredential(credential);
            args.setPrincipal(principal);
            args.setSysPerm(sysPerm);
            sendBase("hasSystemPermission", args);
        }

        public boolean recv_hasSystemPermission() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            hasSystemPermission_result result = new hasSystemPermission_result();
            receiveBase(result, "hasSystemPermission");
            if (result.isSetSuccess()) {
                return result.success;
            }
            if (result.sec != null) {
                throw result.sec;
            }
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "hasSystemPermission failed: unknown result");
        }

        public boolean hasTablePermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte tblPerm) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, ThriftTableOperationException, org.apache.thrift.TException {
            send_hasTablePermission(tinfo, credential, principal, tableName, tblPerm);
            return recv_hasTablePermission();
        }

        public void send_hasTablePermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte tblPerm) throws org.apache.thrift.TException {
            hasTablePermission_args args = new hasTablePermission_args();
            args.setTinfo(tinfo);
            args.setCredential(credential);
            args.setPrincipal(principal);
            args.setTableName(tableName);
            args.setTblPerm(tblPerm);
            sendBase("hasTablePermission", args);
        }

        public boolean recv_hasTablePermission() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, ThriftTableOperationException, org.apache.thrift.TException {
            hasTablePermission_result result = new hasTablePermission_result();
            receiveBase(result, "hasTablePermission");
            if (result.isSetSuccess()) {
                return result.success;
            }
            if (result.sec != null) {
                throw result.sec;
            }
            if (result.tope != null) {
                throw result.tope;
            }
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "hasTablePermission failed: unknown result");
        }

        public void grantSystemPermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte permission) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            send_grantSystemPermission(tinfo, credential, principal, permission);
            recv_grantSystemPermission();
        }

        public void send_grantSystemPermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte permission) throws org.apache.thrift.TException {
            grantSystemPermission_args args = new grantSystemPermission_args();
            args.setTinfo(tinfo);
            args.setCredential(credential);
            args.setPrincipal(principal);
            args.setPermission(permission);
            sendBase("grantSystemPermission", args);
        }

        public void recv_grantSystemPermission() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            grantSystemPermission_result result = new grantSystemPermission_result();
            receiveBase(result, "grantSystemPermission");
            if (result.sec != null) {
                throw result.sec;
            }
            return;
        }

        public void revokeSystemPermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte permission) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            send_revokeSystemPermission(tinfo, credential, principal, permission);
            recv_revokeSystemPermission();
        }

        public void send_revokeSystemPermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte permission) throws org.apache.thrift.TException {
            revokeSystemPermission_args args = new revokeSystemPermission_args();
            args.setTinfo(tinfo);
            args.setCredential(credential);
            args.setPrincipal(principal);
            args.setPermission(permission);
            sendBase("revokeSystemPermission", args);
        }

        public void recv_revokeSystemPermission() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
            revokeSystemPermission_result result = new revokeSystemPermission_result();
            receiveBase(result, "revokeSystemPermission");
            if (result.sec != null) {
                throw result.sec;
            }
            return;
        }

        public void grantTablePermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte permission) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, ThriftTableOperationException, org.apache.thrift.TException {
            send_grantTablePermission(tinfo, credential, principal, tableName, permission);
            recv_grantTablePermission();
        }

        public void send_grantTablePermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte permission) throws org.apache.thrift.TException {
            grantTablePermission_args args = new grantTablePermission_args();
            args.setTinfo(tinfo);
            args.setCredential(credential);
            args.setPrincipal(principal);
            args.setTableName(tableName);
            args.setPermission(permission);
            sendBase("grantTablePermission", args);
        }

        public void recv_grantTablePermission() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, ThriftTableOperationException, org.apache.thrift.TException {
            grantTablePermission_result result = new grantTablePermission_result();
            receiveBase(result, "grantTablePermission");
            if (result.sec != null) {
                throw result.sec;
            }
            if (result.tope != null) {
                throw result.tope;
            }
            return;
        }

        public void revokeTablePermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte permission) throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, ThriftTableOperationException, org.apache.thrift.TException {
            send_revokeTablePermission(tinfo, credential, principal, tableName, permission);
            recv_revokeTablePermission();
        }

        public void send_revokeTablePermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte permission) throws org.apache.thrift.TException {
            revokeTablePermission_args args = new revokeTablePermission_args();
            args.setTinfo(tinfo);
            args.setCredential(credential);
            args.setPrincipal(principal);
            args.setTableName(tableName);
            args.setPermission(permission);
            sendBase("revokeTablePermission", args);
        }

        public void recv_revokeTablePermission() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, ThriftTableOperationException, org.apache.thrift.TException {
            revokeTablePermission_result result = new revokeTablePermission_result();
            receiveBase(result, "revokeTablePermission");
            if (result.sec != null) {
                throw result.sec;
            }
            if (result.tope != null) {
                throw result.tope;
            }
            return;
        }

        public Map<String, String> getConfiguration(ConfigurationType type) throws org.apache.thrift.TException {
            send_getConfiguration(type);
            return recv_getConfiguration();
        }

        public void send_getConfiguration(ConfigurationType type) throws org.apache.thrift.TException {
            getConfiguration_args args = new getConfiguration_args();
            args.setType(type);
            sendBase("getConfiguration", args);
        }

        public Map<String, String> recv_getConfiguration() throws org.apache.thrift.TException {
            getConfiguration_result result = new getConfiguration_result();
            receiveBase(result, "getConfiguration");
            if (result.isSetSuccess()) {
                return result.success;
            }
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "getConfiguration failed: unknown result");
        }

        public Map<String, String> getTableConfiguration(String tableName) throws ThriftTableOperationException, org.apache.thrift.TException {
            send_getTableConfiguration(tableName);
            return recv_getTableConfiguration();
        }

        public void send_getTableConfiguration(String tableName) throws org.apache.thrift.TException {
            getTableConfiguration_args args = new getTableConfiguration_args();
            args.setTableName(tableName);
            sendBase("getTableConfiguration", args);
        }

        public Map<String, String> recv_getTableConfiguration() throws ThriftTableOperationException, org.apache.thrift.TException {
            getTableConfiguration_result result = new getTableConfiguration_result();
            receiveBase(result, "getTableConfiguration");
            if (result.isSetSuccess()) {
                return result.success;
            }
            if (result.tope != null) {
                throw result.tope;
            }
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "getTableConfiguration failed: unknown result");
        }

        public boolean checkClass(org.apache.accumulo.trace.thrift.TInfo tinfo, String className, String interfaceMatch) throws org.apache.thrift.TException {
            send_checkClass(tinfo, className, interfaceMatch);
            return recv_checkClass();
        }

        public void send_checkClass(org.apache.accumulo.trace.thrift.TInfo tinfo, String className, String interfaceMatch) throws org.apache.thrift.TException {
            checkClass_args args = new checkClass_args();
            args.setTinfo(tinfo);
            args.setClassName(className);
            args.setInterfaceMatch(interfaceMatch);
            sendBase("checkClass", args);
        }

        public boolean recv_checkClass() throws org.apache.thrift.TException {
            checkClass_result result = new checkClass_result();
            receiveBase(result, "checkClass");
            if (result.isSetSuccess()) {
                return result.success;
            }
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "checkClass failed: unknown result");
        }
    }

    public static class AsyncClient extends org.apache.thrift.async.TAsyncClient implements AsyncIface {

        public static class Factory implements org.apache.thrift.async.TAsyncClientFactory<AsyncClient> {

            private org.apache.thrift.async.TAsyncClientManager clientManager;

            private org.apache.thrift.protocol.TProtocolFactory protocolFactory;

            public Factory(org.apache.thrift.async.TAsyncClientManager clientManager, org.apache.thrift.protocol.TProtocolFactory protocolFactory) {
                this.clientManager = clientManager;
                this.protocolFactory = protocolFactory;
            }

            public AsyncClient getAsyncClient(org.apache.thrift.transport.TNonblockingTransport transport) {
                return new AsyncClient(protocolFactory, clientManager, transport);
            }
        }

        public AsyncClient(org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.async.TAsyncClientManager clientManager, org.apache.thrift.transport.TNonblockingTransport transport) {
            super(protocolFactory, clientManager, transport);
        }

        public void getRootTabletLocation(org.apache.thrift.async.AsyncMethodCallback<getRootTabletLocation_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            getRootTabletLocation_call method_call = new getRootTabletLocation_call(resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class getRootTabletLocation_call extends org.apache.thrift.async.TAsyncMethodCall {

            public getRootTabletLocation_call(org.apache.thrift.async.AsyncMethodCallback<getRootTabletLocation_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("getRootTabletLocation", org.apache.thrift.protocol.TMessageType.CALL, 0));
                getRootTabletLocation_args args = new getRootTabletLocation_args();
                args.write(prot);
                prot.writeMessageEnd();
            }

            public String getResult() throws org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                return (new Client(prot)).recv_getRootTabletLocation();
            }
        }

        public void getInstanceId(org.apache.thrift.async.AsyncMethodCallback<getInstanceId_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            getInstanceId_call method_call = new getInstanceId_call(resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class getInstanceId_call extends org.apache.thrift.async.TAsyncMethodCall {

            public getInstanceId_call(org.apache.thrift.async.AsyncMethodCallback<getInstanceId_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("getInstanceId", org.apache.thrift.protocol.TMessageType.CALL, 0));
                getInstanceId_args args = new getInstanceId_args();
                args.write(prot);
                prot.writeMessageEnd();
            }

            public String getResult() throws org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                return (new Client(prot)).recv_getInstanceId();
            }
        }

        public void getZooKeepers(org.apache.thrift.async.AsyncMethodCallback<getZooKeepers_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            getZooKeepers_call method_call = new getZooKeepers_call(resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class getZooKeepers_call extends org.apache.thrift.async.TAsyncMethodCall {

            public getZooKeepers_call(org.apache.thrift.async.AsyncMethodCallback<getZooKeepers_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("getZooKeepers", org.apache.thrift.protocol.TMessageType.CALL, 0));
                getZooKeepers_args args = new getZooKeepers_args();
                args.write(prot);
                prot.writeMessageEnd();
            }

            public String getResult() throws org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                return (new Client(prot)).recv_getZooKeepers();
            }
        }

        public void bulkImportFiles(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, long tid, String tableId, List<String> files, String errorDir, boolean setTime, org.apache.thrift.async.AsyncMethodCallback<bulkImportFiles_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            bulkImportFiles_call method_call = new bulkImportFiles_call(tinfo, credential, tid, tableId, files, errorDir, setTime, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class bulkImportFiles_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.Credential credential;

            private long tid;

            private String tableId;

            private List<String> files;

            private String errorDir;

            private boolean setTime;

            public bulkImportFiles_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, long tid, String tableId, List<String> files, String errorDir, boolean setTime, org.apache.thrift.async.AsyncMethodCallback<bulkImportFiles_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credential = credential;
                this.tid = tid;
                this.tableId = tableId;
                this.files = files;
                this.errorDir = errorDir;
                this.setTime = setTime;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("bulkImportFiles", org.apache.thrift.protocol.TMessageType.CALL, 0));
                bulkImportFiles_args args = new bulkImportFiles_args();
                args.setTinfo(tinfo);
                args.setCredential(credential);
                args.setTid(tid);
                args.setTableId(tableId);
                args.setFiles(files);
                args.setErrorDir(errorDir);
                args.setSetTime(setTime);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public List<String> getResult() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, ThriftTableOperationException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                return (new Client(prot)).recv_bulkImportFiles();
            }
        }

        public void isActive(org.apache.accumulo.trace.thrift.TInfo tinfo, long tid, org.apache.thrift.async.AsyncMethodCallback<isActive_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            isActive_call method_call = new isActive_call(tinfo, tid, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class isActive_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private long tid;

            public isActive_call(org.apache.accumulo.trace.thrift.TInfo tinfo, long tid, org.apache.thrift.async.AsyncMethodCallback<isActive_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.tid = tid;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("isActive", org.apache.thrift.protocol.TMessageType.CALL, 0));
                isActive_args args = new isActive_args();
                args.setTinfo(tinfo);
                args.setTid(tid);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public boolean getResult() throws org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                return (new Client(prot)).recv_isActive();
            }
        }

        public void ping(org.apache.accumulo.core.security.thrift.Credential credential, org.apache.thrift.async.AsyncMethodCallback<ping_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            ping_call method_call = new ping_call(credential, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class ping_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.core.security.thrift.Credential credential;

            public ping_call(org.apache.accumulo.core.security.thrift.Credential credential, org.apache.thrift.async.AsyncMethodCallback<ping_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.credential = credential;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("ping", org.apache.thrift.protocol.TMessageType.CALL, 0));
                ping_args args = new ping_args();
                args.setCredential(credential);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                (new Client(prot)).recv_ping();
            }
        }

        public void authenticateUser(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toAuthenticate, org.apache.thrift.async.AsyncMethodCallback<authenticateUser_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            authenticateUser_call method_call = new authenticateUser_call(tinfo, credential, toAuthenticate, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class authenticateUser_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.Credential credential;

            private org.apache.accumulo.core.security.thrift.Credential toAuthenticate;

            public authenticateUser_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toAuthenticate, org.apache.thrift.async.AsyncMethodCallback<authenticateUser_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credential = credential;
                this.toAuthenticate = toAuthenticate;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("authenticateUser", org.apache.thrift.protocol.TMessageType.CALL, 0));
                authenticateUser_args args = new authenticateUser_args();
                args.setTinfo(tinfo);
                args.setCredential(credential);
                args.setToAuthenticate(toAuthenticate);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public boolean getResult() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                return (new Client(prot)).recv_authenticateUser();
            }
        }

        public void listUsers(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.thrift.async.AsyncMethodCallback<listUsers_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            listUsers_call method_call = new listUsers_call(tinfo, credential, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class listUsers_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.Credential credential;

            public listUsers_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.thrift.async.AsyncMethodCallback<listUsers_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credential = credential;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("listUsers", org.apache.thrift.protocol.TMessageType.CALL, 0));
                listUsers_args args = new listUsers_args();
                args.setTinfo(tinfo);
                args.setCredential(credential);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public Set<String> getResult() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                return (new Client(prot)).recv_listUsers();
            }
        }

        public void createUser(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toCreate, List<ByteBuffer> authorizations, org.apache.thrift.async.AsyncMethodCallback<createUser_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            createUser_call method_call = new createUser_call(tinfo, credential, toCreate, authorizations, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class createUser_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.Credential credential;

            private org.apache.accumulo.core.security.thrift.Credential toCreate;

            private List<ByteBuffer> authorizations;

            public createUser_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toCreate, List<ByteBuffer> authorizations, org.apache.thrift.async.AsyncMethodCallback<createUser_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credential = credential;
                this.toCreate = toCreate;
                this.authorizations = authorizations;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("createUser", org.apache.thrift.protocol.TMessageType.CALL, 0));
                createUser_args args = new createUser_args();
                args.setTinfo(tinfo);
                args.setCredential(credential);
                args.setToCreate(toCreate);
                args.setAuthorizations(authorizations);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                (new Client(prot)).recv_createUser();
            }
        }

        public void dropUser(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, org.apache.thrift.async.AsyncMethodCallback<dropUser_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            dropUser_call method_call = new dropUser_call(tinfo, credential, principal, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class dropUser_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.Credential credential;

            private String principal;

            public dropUser_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, org.apache.thrift.async.AsyncMethodCallback<dropUser_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credential = credential;
                this.principal = principal;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("dropUser", org.apache.thrift.protocol.TMessageType.CALL, 0));
                dropUser_args args = new dropUser_args();
                args.setTinfo(tinfo);
                args.setCredential(credential);
                args.setPrincipal(principal);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                (new Client(prot)).recv_dropUser();
            }
        }

        public void changePassword(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toChange, org.apache.thrift.async.AsyncMethodCallback<changePassword_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            changePassword_call method_call = new changePassword_call(tinfo, credential, toChange, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class changePassword_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.Credential credential;

            private org.apache.accumulo.core.security.thrift.Credential toChange;

            public changePassword_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toChange, org.apache.thrift.async.AsyncMethodCallback<changePassword_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credential = credential;
                this.toChange = toChange;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("changePassword", org.apache.thrift.protocol.TMessageType.CALL, 0));
                changePassword_args args = new changePassword_args();
                args.setTinfo(tinfo);
                args.setCredential(credential);
                args.setToChange(toChange);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                (new Client(prot)).recv_changePassword();
            }
        }

        public void changeAuthorizations(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, List<ByteBuffer> authorizations, org.apache.thrift.async.AsyncMethodCallback<changeAuthorizations_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            changeAuthorizations_call method_call = new changeAuthorizations_call(tinfo, credential, principal, authorizations, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class changeAuthorizations_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.Credential credential;

            private String principal;

            private List<ByteBuffer> authorizations;

            public changeAuthorizations_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, List<ByteBuffer> authorizations, org.apache.thrift.async.AsyncMethodCallback<changeAuthorizations_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credential = credential;
                this.principal = principal;
                this.authorizations = authorizations;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("changeAuthorizations", org.apache.thrift.protocol.TMessageType.CALL, 0));
                changeAuthorizations_args args = new changeAuthorizations_args();
                args.setTinfo(tinfo);
                args.setCredential(credential);
                args.setPrincipal(principal);
                args.setAuthorizations(authorizations);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                (new Client(prot)).recv_changeAuthorizations();
            }
        }

        public void getUserAuthorizations(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, org.apache.thrift.async.AsyncMethodCallback<getUserAuthorizations_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            getUserAuthorizations_call method_call = new getUserAuthorizations_call(tinfo, credential, principal, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class getUserAuthorizations_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.Credential credential;

            private String principal;

            public getUserAuthorizations_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, org.apache.thrift.async.AsyncMethodCallback<getUserAuthorizations_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credential = credential;
                this.principal = principal;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("getUserAuthorizations", org.apache.thrift.protocol.TMessageType.CALL, 0));
                getUserAuthorizations_args args = new getUserAuthorizations_args();
                args.setTinfo(tinfo);
                args.setCredential(credential);
                args.setPrincipal(principal);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public List<ByteBuffer> getResult() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                return (new Client(prot)).recv_getUserAuthorizations();
            }
        }

        public void hasSystemPermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte sysPerm, org.apache.thrift.async.AsyncMethodCallback<hasSystemPermission_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            hasSystemPermission_call method_call = new hasSystemPermission_call(tinfo, credential, principal, sysPerm, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class hasSystemPermission_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.Credential credential;

            private String principal;

            private byte sysPerm;

            public hasSystemPermission_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte sysPerm, org.apache.thrift.async.AsyncMethodCallback<hasSystemPermission_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credential = credential;
                this.principal = principal;
                this.sysPerm = sysPerm;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("hasSystemPermission", org.apache.thrift.protocol.TMessageType.CALL, 0));
                hasSystemPermission_args args = new hasSystemPermission_args();
                args.setTinfo(tinfo);
                args.setCredential(credential);
                args.setPrincipal(principal);
                args.setSysPerm(sysPerm);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public boolean getResult() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                return (new Client(prot)).recv_hasSystemPermission();
            }
        }

        public void hasTablePermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte tblPerm, org.apache.thrift.async.AsyncMethodCallback<hasTablePermission_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            hasTablePermission_call method_call = new hasTablePermission_call(tinfo, credential, principal, tableName, tblPerm, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class hasTablePermission_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.Credential credential;

            private String principal;

            private String tableName;

            private byte tblPerm;

            public hasTablePermission_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte tblPerm, org.apache.thrift.async.AsyncMethodCallback<hasTablePermission_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credential = credential;
                this.principal = principal;
                this.tableName = tableName;
                this.tblPerm = tblPerm;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("hasTablePermission", org.apache.thrift.protocol.TMessageType.CALL, 0));
                hasTablePermission_args args = new hasTablePermission_args();
                args.setTinfo(tinfo);
                args.setCredential(credential);
                args.setPrincipal(principal);
                args.setTableName(tableName);
                args.setTblPerm(tblPerm);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public boolean getResult() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, ThriftTableOperationException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                return (new Client(prot)).recv_hasTablePermission();
            }
        }

        public void grantSystemPermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte permission, org.apache.thrift.async.AsyncMethodCallback<grantSystemPermission_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            grantSystemPermission_call method_call = new grantSystemPermission_call(tinfo, credential, principal, permission, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class grantSystemPermission_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.Credential credential;

            private String principal;

            private byte permission;

            public grantSystemPermission_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte permission, org.apache.thrift.async.AsyncMethodCallback<grantSystemPermission_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credential = credential;
                this.principal = principal;
                this.permission = permission;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("grantSystemPermission", org.apache.thrift.protocol.TMessageType.CALL, 0));
                grantSystemPermission_args args = new grantSystemPermission_args();
                args.setTinfo(tinfo);
                args.setCredential(credential);
                args.setPrincipal(principal);
                args.setPermission(permission);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                (new Client(prot)).recv_grantSystemPermission();
            }
        }

        public void revokeSystemPermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte permission, org.apache.thrift.async.AsyncMethodCallback<revokeSystemPermission_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            revokeSystemPermission_call method_call = new revokeSystemPermission_call(tinfo, credential, principal, permission, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class revokeSystemPermission_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.Credential credential;

            private String principal;

            private byte permission;

            public revokeSystemPermission_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte permission, org.apache.thrift.async.AsyncMethodCallback<revokeSystemPermission_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credential = credential;
                this.principal = principal;
                this.permission = permission;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("revokeSystemPermission", org.apache.thrift.protocol.TMessageType.CALL, 0));
                revokeSystemPermission_args args = new revokeSystemPermission_args();
                args.setTinfo(tinfo);
                args.setCredential(credential);
                args.setPrincipal(principal);
                args.setPermission(permission);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                (new Client(prot)).recv_revokeSystemPermission();
            }
        }

        public void grantTablePermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte permission, org.apache.thrift.async.AsyncMethodCallback<grantTablePermission_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            grantTablePermission_call method_call = new grantTablePermission_call(tinfo, credential, principal, tableName, permission, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class grantTablePermission_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.Credential credential;

            private String principal;

            private String tableName;

            private byte permission;

            public grantTablePermission_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte permission, org.apache.thrift.async.AsyncMethodCallback<grantTablePermission_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credential = credential;
                this.principal = principal;
                this.tableName = tableName;
                this.permission = permission;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("grantTablePermission", org.apache.thrift.protocol.TMessageType.CALL, 0));
                grantTablePermission_args args = new grantTablePermission_args();
                args.setTinfo(tinfo);
                args.setCredential(credential);
                args.setPrincipal(principal);
                args.setTableName(tableName);
                args.setPermission(permission);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, ThriftTableOperationException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                (new Client(prot)).recv_grantTablePermission();
            }
        }

        public void revokeTablePermission(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte permission, org.apache.thrift.async.AsyncMethodCallback<revokeTablePermission_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            revokeTablePermission_call method_call = new revokeTablePermission_call(tinfo, credential, principal, tableName, permission, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class revokeTablePermission_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.Credential credential;

            private String principal;

            private String tableName;

            private byte permission;

            public revokeTablePermission_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte permission, org.apache.thrift.async.AsyncMethodCallback<revokeTablePermission_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credential = credential;
                this.principal = principal;
                this.tableName = tableName;
                this.permission = permission;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("revokeTablePermission", org.apache.thrift.protocol.TMessageType.CALL, 0));
                revokeTablePermission_args args = new revokeTablePermission_args();
                args.setTinfo(tinfo);
                args.setCredential(credential);
                args.setPrincipal(principal);
                args.setTableName(tableName);
                args.setPermission(permission);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.accumulo.core.security.thrift.ThriftSecurityException, ThriftTableOperationException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                (new Client(prot)).recv_revokeTablePermission();
            }
        }

        public void getConfiguration(ConfigurationType type, org.apache.thrift.async.AsyncMethodCallback<getConfiguration_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            getConfiguration_call method_call = new getConfiguration_call(type, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class getConfiguration_call extends org.apache.thrift.async.TAsyncMethodCall {

            private ConfigurationType type;

            public getConfiguration_call(ConfigurationType type, org.apache.thrift.async.AsyncMethodCallback<getConfiguration_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.type = type;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("getConfiguration", org.apache.thrift.protocol.TMessageType.CALL, 0));
                getConfiguration_args args = new getConfiguration_args();
                args.setType(type);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public Map<String, String> getResult() throws org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                return (new Client(prot)).recv_getConfiguration();
            }
        }

        public void getTableConfiguration(String tableName, org.apache.thrift.async.AsyncMethodCallback<getTableConfiguration_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            getTableConfiguration_call method_call = new getTableConfiguration_call(tableName, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class getTableConfiguration_call extends org.apache.thrift.async.TAsyncMethodCall {

            private String tableName;

            public getTableConfiguration_call(String tableName, org.apache.thrift.async.AsyncMethodCallback<getTableConfiguration_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tableName = tableName;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("getTableConfiguration", org.apache.thrift.protocol.TMessageType.CALL, 0));
                getTableConfiguration_args args = new getTableConfiguration_args();
                args.setTableName(tableName);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public Map<String, String> getResult() throws ThriftTableOperationException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                return (new Client(prot)).recv_getTableConfiguration();
            }
        }

        public void checkClass(org.apache.accumulo.trace.thrift.TInfo tinfo, String className, String interfaceMatch, org.apache.thrift.async.AsyncMethodCallback<checkClass_call> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            checkClass_call method_call = new checkClass_call(tinfo, className, interfaceMatch, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class checkClass_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private String className;

            private String interfaceMatch;

            public checkClass_call(org.apache.accumulo.trace.thrift.TInfo tinfo, String className, String interfaceMatch, org.apache.thrift.async.AsyncMethodCallback<checkClass_call> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.className = className;
                this.interfaceMatch = interfaceMatch;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("checkClass", org.apache.thrift.protocol.TMessageType.CALL, 0));
                checkClass_args args = new checkClass_args();
                args.setTinfo(tinfo);
                args.setClassName(className);
                args.setInterfaceMatch(interfaceMatch);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public boolean getResult() throws org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                return (new Client(prot)).recv_checkClass();
            }
        }
    }

    public static class Processor<I extends Iface> extends org.apache.thrift.TBaseProcessor<I> implements org.apache.thrift.TProcessor {

        private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class.getName());

        public Processor(I iface) {
            super(iface, getProcessMap(new HashMap<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>>()));
        }

        protected Processor(I iface, Map<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>> processMap) {
            super(iface, getProcessMap(processMap));
        }

        private static <I extends Iface> Map<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>> getProcessMap(Map<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>> processMap) {
            processMap.put("getRootTabletLocation", new getRootTabletLocation());
            processMap.put("getInstanceId", new getInstanceId());
            processMap.put("getZooKeepers", new getZooKeepers());
            processMap.put("bulkImportFiles", new bulkImportFiles());
            processMap.put("isActive", new isActive());
            processMap.put("ping", new ping());
            processMap.put("authenticateUser", new authenticateUser());
            processMap.put("listUsers", new listUsers());
            processMap.put("createUser", new createUser());
            processMap.put("dropUser", new dropUser());
            processMap.put("changePassword", new changePassword());
            processMap.put("changeAuthorizations", new changeAuthorizations());
            processMap.put("getUserAuthorizations", new getUserAuthorizations());
            processMap.put("hasSystemPermission", new hasSystemPermission());
            processMap.put("hasTablePermission", new hasTablePermission());
            processMap.put("grantSystemPermission", new grantSystemPermission());
            processMap.put("revokeSystemPermission", new revokeSystemPermission());
            processMap.put("grantTablePermission", new grantTablePermission());
            processMap.put("revokeTablePermission", new revokeTablePermission());
            processMap.put("getConfiguration", new getConfiguration());
            processMap.put("getTableConfiguration", new getTableConfiguration());
            processMap.put("checkClass", new checkClass());
            return processMap;
        }

        public static class getRootTabletLocation<I extends Iface> extends org.apache.thrift.ProcessFunction<I, getRootTabletLocation_args> {

            public getRootTabletLocation() {
                super("getRootTabletLocation");
            }

            public getRootTabletLocation_args getEmptyArgsInstance() {
                return new getRootTabletLocation_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public getRootTabletLocation_result getResult(I iface, getRootTabletLocation_args args) throws org.apache.thrift.TException {
                getRootTabletLocation_result result = new getRootTabletLocation_result();
                result.success = iface.getRootTabletLocation();
                return result;
            }
        }

        public static class getInstanceId<I extends Iface> extends org.apache.thrift.ProcessFunction<I, getInstanceId_args> {

            public getInstanceId() {
                super("getInstanceId");
            }

            public getInstanceId_args getEmptyArgsInstance() {
                return new getInstanceId_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public getInstanceId_result getResult(I iface, getInstanceId_args args) throws org.apache.thrift.TException {
                getInstanceId_result result = new getInstanceId_result();
                result.success = iface.getInstanceId();
                return result;
            }
        }

        public static class getZooKeepers<I extends Iface> extends org.apache.thrift.ProcessFunction<I, getZooKeepers_args> {

            public getZooKeepers() {
                super("getZooKeepers");
            }

            public getZooKeepers_args getEmptyArgsInstance() {
                return new getZooKeepers_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public getZooKeepers_result getResult(I iface, getZooKeepers_args args) throws org.apache.thrift.TException {
                getZooKeepers_result result = new getZooKeepers_result();
                result.success = iface.getZooKeepers();
                return result;
            }
        }

        public static class bulkImportFiles<I extends Iface> extends org.apache.thrift.ProcessFunction<I, bulkImportFiles_args> {

            public bulkImportFiles() {
                super("bulkImportFiles");
            }

            public bulkImportFiles_args getEmptyArgsInstance() {
                return new bulkImportFiles_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public bulkImportFiles_result getResult(I iface, bulkImportFiles_args args) throws org.apache.thrift.TException {
                bulkImportFiles_result result = new bulkImportFiles_result();
                try {
                    result.success = iface.bulkImportFiles(args.tinfo, args.credential, args.tid, args.tableId, args.files, args.errorDir, args.setTime);
                } catch (org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                } catch (ThriftTableOperationException tope) {
                    result.tope = tope;
                }
                return result;
            }
        }

        public static class isActive<I extends Iface> extends org.apache.thrift.ProcessFunction<I, isActive_args> {

            public isActive() {
                super("isActive");
            }

            public isActive_args getEmptyArgsInstance() {
                return new isActive_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public isActive_result getResult(I iface, isActive_args args) throws org.apache.thrift.TException {
                isActive_result result = new isActive_result();
                result.success = iface.isActive(args.tinfo, args.tid);
                result.setSuccessIsSet(true);
                return result;
            }
        }

        public static class ping<I extends Iface> extends org.apache.thrift.ProcessFunction<I, ping_args> {

            public ping() {
                super("ping");
            }

            public ping_args getEmptyArgsInstance() {
                return new ping_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public ping_result getResult(I iface, ping_args args) throws org.apache.thrift.TException {
                ping_result result = new ping_result();
                try {
                    iface.ping(args.credential);
                } catch (org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                }
                return result;
            }
        }

        public static class authenticateUser<I extends Iface> extends org.apache.thrift.ProcessFunction<I, authenticateUser_args> {

            public authenticateUser() {
                super("authenticateUser");
            }

            public authenticateUser_args getEmptyArgsInstance() {
                return new authenticateUser_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public authenticateUser_result getResult(I iface, authenticateUser_args args) throws org.apache.thrift.TException {
                authenticateUser_result result = new authenticateUser_result();
                try {
                    result.success = iface.authenticateUser(args.tinfo, args.credential, args.toAuthenticate);
                    result.setSuccessIsSet(true);
                } catch (org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                }
                return result;
            }
        }

        public static class listUsers<I extends Iface> extends org.apache.thrift.ProcessFunction<I, listUsers_args> {

            public listUsers() {
                super("listUsers");
            }

            public listUsers_args getEmptyArgsInstance() {
                return new listUsers_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public listUsers_result getResult(I iface, listUsers_args args) throws org.apache.thrift.TException {
                listUsers_result result = new listUsers_result();
                try {
                    result.success = iface.listUsers(args.tinfo, args.credential);
                } catch (org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                }
                return result;
            }
        }

        public static class createUser<I extends Iface> extends org.apache.thrift.ProcessFunction<I, createUser_args> {

            public createUser() {
                super("createUser");
            }

            public createUser_args getEmptyArgsInstance() {
                return new createUser_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public createUser_result getResult(I iface, createUser_args args) throws org.apache.thrift.TException {
                createUser_result result = new createUser_result();
                try {
                    iface.createUser(args.tinfo, args.credential, args.toCreate, args.authorizations);
                } catch (org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                }
                return result;
            }
        }

        public static class dropUser<I extends Iface> extends org.apache.thrift.ProcessFunction<I, dropUser_args> {

            public dropUser() {
                super("dropUser");
            }

            public dropUser_args getEmptyArgsInstance() {
                return new dropUser_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public dropUser_result getResult(I iface, dropUser_args args) throws org.apache.thrift.TException {
                dropUser_result result = new dropUser_result();
                try {
                    iface.dropUser(args.tinfo, args.credential, args.principal);
                } catch (org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                }
                return result;
            }
        }

        public static class changePassword<I extends Iface> extends org.apache.thrift.ProcessFunction<I, changePassword_args> {

            public changePassword() {
                super("changePassword");
            }

            public changePassword_args getEmptyArgsInstance() {
                return new changePassword_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public changePassword_result getResult(I iface, changePassword_args args) throws org.apache.thrift.TException {
                changePassword_result result = new changePassword_result();
                try {
                    iface.changePassword(args.tinfo, args.credential, args.toChange);
                } catch (org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                }
                return result;
            }
        }

        public static class changeAuthorizations<I extends Iface> extends org.apache.thrift.ProcessFunction<I, changeAuthorizations_args> {

            public changeAuthorizations() {
                super("changeAuthorizations");
            }

            public changeAuthorizations_args getEmptyArgsInstance() {
                return new changeAuthorizations_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public changeAuthorizations_result getResult(I iface, changeAuthorizations_args args) throws org.apache.thrift.TException {
                changeAuthorizations_result result = new changeAuthorizations_result();
                try {
                    iface.changeAuthorizations(args.tinfo, args.credential, args.principal, args.authorizations);
                } catch (org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                }
                return result;
            }
        }

        public static class getUserAuthorizations<I extends Iface> extends org.apache.thrift.ProcessFunction<I, getUserAuthorizations_args> {

            public getUserAuthorizations() {
                super("getUserAuthorizations");
            }

            public getUserAuthorizations_args getEmptyArgsInstance() {
                return new getUserAuthorizations_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public getUserAuthorizations_result getResult(I iface, getUserAuthorizations_args args) throws org.apache.thrift.TException {
                getUserAuthorizations_result result = new getUserAuthorizations_result();
                try {
                    result.success = iface.getUserAuthorizations(args.tinfo, args.credential, args.principal);
                } catch (org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                }
                return result;
            }
        }

        public static class hasSystemPermission<I extends Iface> extends org.apache.thrift.ProcessFunction<I, hasSystemPermission_args> {

            public hasSystemPermission() {
                super("hasSystemPermission");
            }

            public hasSystemPermission_args getEmptyArgsInstance() {
                return new hasSystemPermission_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public hasSystemPermission_result getResult(I iface, hasSystemPermission_args args) throws org.apache.thrift.TException {
                hasSystemPermission_result result = new hasSystemPermission_result();
                try {
                    result.success = iface.hasSystemPermission(args.tinfo, args.credential, args.principal, args.sysPerm);
                    result.setSuccessIsSet(true);
                } catch (org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                }
                return result;
            }
        }

        public static class hasTablePermission<I extends Iface> extends org.apache.thrift.ProcessFunction<I, hasTablePermission_args> {

            public hasTablePermission() {
                super("hasTablePermission");
            }

            public hasTablePermission_args getEmptyArgsInstance() {
                return new hasTablePermission_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public hasTablePermission_result getResult(I iface, hasTablePermission_args args) throws org.apache.thrift.TException {
                hasTablePermission_result result = new hasTablePermission_result();
                try {
                    result.success = iface.hasTablePermission(args.tinfo, args.credential, args.principal, args.tableName, args.tblPerm);
                    result.setSuccessIsSet(true);
                } catch (org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                } catch (ThriftTableOperationException tope) {
                    result.tope = tope;
                }
                return result;
            }
        }

        public static class grantSystemPermission<I extends Iface> extends org.apache.thrift.ProcessFunction<I, grantSystemPermission_args> {

            public grantSystemPermission() {
                super("grantSystemPermission");
            }

            public grantSystemPermission_args getEmptyArgsInstance() {
                return new grantSystemPermission_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public grantSystemPermission_result getResult(I iface, grantSystemPermission_args args) throws org.apache.thrift.TException {
                grantSystemPermission_result result = new grantSystemPermission_result();
                try {
                    iface.grantSystemPermission(args.tinfo, args.credential, args.principal, args.permission);
                } catch (org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                }
                return result;
            }
        }

        public static class revokeSystemPermission<I extends Iface> extends org.apache.thrift.ProcessFunction<I, revokeSystemPermission_args> {

            public revokeSystemPermission() {
                super("revokeSystemPermission");
            }

            public revokeSystemPermission_args getEmptyArgsInstance() {
                return new revokeSystemPermission_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public revokeSystemPermission_result getResult(I iface, revokeSystemPermission_args args) throws org.apache.thrift.TException {
                revokeSystemPermission_result result = new revokeSystemPermission_result();
                try {
                    iface.revokeSystemPermission(args.tinfo, args.credential, args.principal, args.permission);
                } catch (org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                }
                return result;
            }
        }

        public static class grantTablePermission<I extends Iface> extends org.apache.thrift.ProcessFunction<I, grantTablePermission_args> {

            public grantTablePermission() {
                super("grantTablePermission");
            }

            public grantTablePermission_args getEmptyArgsInstance() {
                return new grantTablePermission_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public grantTablePermission_result getResult(I iface, grantTablePermission_args args) throws org.apache.thrift.TException {
                grantTablePermission_result result = new grantTablePermission_result();
                try {
                    iface.grantTablePermission(args.tinfo, args.credential, args.principal, args.tableName, args.permission);
                } catch (org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                } catch (ThriftTableOperationException tope) {
                    result.tope = tope;
                }
                return result;
            }
        }

        public static class revokeTablePermission<I extends Iface> extends org.apache.thrift.ProcessFunction<I, revokeTablePermission_args> {

            public revokeTablePermission() {
                super("revokeTablePermission");
            }

            public revokeTablePermission_args getEmptyArgsInstance() {
                return new revokeTablePermission_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public revokeTablePermission_result getResult(I iface, revokeTablePermission_args args) throws org.apache.thrift.TException {
                revokeTablePermission_result result = new revokeTablePermission_result();
                try {
                    iface.revokeTablePermission(args.tinfo, args.credential, args.principal, args.tableName, args.permission);
                } catch (org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                } catch (ThriftTableOperationException tope) {
                    result.tope = tope;
                }
                return result;
            }
        }

        public static class getConfiguration<I extends Iface> extends org.apache.thrift.ProcessFunction<I, getConfiguration_args> {

            public getConfiguration() {
                super("getConfiguration");
            }

            public getConfiguration_args getEmptyArgsInstance() {
                return new getConfiguration_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public getConfiguration_result getResult(I iface, getConfiguration_args args) throws org.apache.thrift.TException {
                getConfiguration_result result = new getConfiguration_result();
                result.success = iface.getConfiguration(args.type);
                return result;
            }
        }

        public static class getTableConfiguration<I extends Iface> extends org.apache.thrift.ProcessFunction<I, getTableConfiguration_args> {

            public getTableConfiguration() {
                super("getTableConfiguration");
            }

            public getTableConfiguration_args getEmptyArgsInstance() {
                return new getTableConfiguration_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public getTableConfiguration_result getResult(I iface, getTableConfiguration_args args) throws org.apache.thrift.TException {
                getTableConfiguration_result result = new getTableConfiguration_result();
                try {
                    result.success = iface.getTableConfiguration(args.tableName);
                } catch (ThriftTableOperationException tope) {
                    result.tope = tope;
                }
                return result;
            }
        }

        public static class checkClass<I extends Iface> extends org.apache.thrift.ProcessFunction<I, checkClass_args> {

            public checkClass() {
                super("checkClass");
            }

            public checkClass_args getEmptyArgsInstance() {
                return new checkClass_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public checkClass_result getResult(I iface, checkClass_args args) throws org.apache.thrift.TException {
                checkClass_result result = new checkClass_result();
                result.success = iface.checkClass(args.tinfo, args.className, args.interfaceMatch);
                result.setSuccessIsSet(true);
                return result;
            }
        }
    }

    public static class getRootTabletLocation_args implements org.apache.thrift.TBase<getRootTabletLocation_args, getRootTabletLocation_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("getRootTabletLocation_args");

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new getRootTabletLocation_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new getRootTabletLocation_argsTupleSchemeFactory());
        }

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {
            ;

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
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
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(getRootTabletLocation_args.class, metaDataMap);
        }

        public getRootTabletLocation_args() {
        }

        public getRootTabletLocation_args(getRootTabletLocation_args other) {
        }

        public getRootTabletLocation_args deepCopy() {
            return new getRootTabletLocation_args(this);
        }

        @Override
        public void clear() {
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof getRootTabletLocation_args)
                return this.equals((getRootTabletLocation_args) that);
            return false;
        }

        public boolean equals(getRootTabletLocation_args that) {
            if (that == null)
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(getRootTabletLocation_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            getRootTabletLocation_args typedOther = (getRootTabletLocation_args) other;
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
            StringBuilder sb = new StringBuilder("getRootTabletLocation_args(");
            boolean first = true;
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

        private static class getRootTabletLocation_argsStandardSchemeFactory implements SchemeFactory {

            public getRootTabletLocation_argsStandardScheme getScheme() {
                return new getRootTabletLocation_argsStandardScheme();
            }
        }

        private static class getRootTabletLocation_argsStandardScheme extends StandardScheme<getRootTabletLocation_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, getRootTabletLocation_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        default:
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                    }
                    iprot.readFieldEnd();
                }
                iprot.readStructEnd();
                struct.validate();
            }

            public void write(org.apache.thrift.protocol.TProtocol oprot, getRootTabletLocation_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class getRootTabletLocation_argsTupleSchemeFactory implements SchemeFactory {

            public getRootTabletLocation_argsTupleScheme getScheme() {
                return new getRootTabletLocation_argsTupleScheme();
            }
        }

        private static class getRootTabletLocation_argsTupleScheme extends TupleScheme<getRootTabletLocation_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, getRootTabletLocation_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, getRootTabletLocation_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
            }
        }
    }

    public static class getRootTabletLocation_result implements org.apache.thrift.TBase<getRootTabletLocation_result, getRootTabletLocation_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("getRootTabletLocation_result");

        private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.STRING, (short) 0);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new getRootTabletLocation_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new getRootTabletLocation_resultTupleSchemeFactory());
        }

        public String success;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SUCCESS((short) 0, "success");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 0:
                        return SUCCESS;
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
            tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(getRootTabletLocation_result.class, metaDataMap);
        }

        public getRootTabletLocation_result() {
        }

        public getRootTabletLocation_result(String success) {
            this();
            this.success = success;
        }

        public getRootTabletLocation_result(getRootTabletLocation_result other) {
            if (other.isSetSuccess()) {
                this.success = other.success;
            }
        }

        public getRootTabletLocation_result deepCopy() {
            return new getRootTabletLocation_result(this);
        }

        @Override
        public void clear() {
            this.success = null;
        }

        public String getSuccess() {
            return this.success;
        }

        public getRootTabletLocation_result setSuccess(String success) {
            this.success = success;
            return this;
        }

        public void unsetSuccess() {
            this.success = null;
        }

        public boolean isSetSuccess() {
            return this.success != null;
        }

        public void setSuccessIsSet(boolean value) {
            if (!value) {
                this.success = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SUCCESS:
                    if (value == null) {
                        unsetSuccess();
                    } else {
                        setSuccess((String) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SUCCESS:
                    return getSuccess();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SUCCESS:
                    return isSetSuccess();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof getRootTabletLocation_result)
                return this.equals((getRootTabletLocation_result) that);
            return false;
        }

        public boolean equals(getRootTabletLocation_result that) {
            if (that == null)
                return false;
            boolean this_present_success = true && this.isSetSuccess();
            boolean that_present_success = true && that.isSetSuccess();
            if (this_present_success || that_present_success) {
                if (!(this_present_success && that_present_success))
                    return false;
                if (!this.success.equals(that.success))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(getRootTabletLocation_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            getRootTabletLocation_result typedOther = (getRootTabletLocation_result) other;
            lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSuccess()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, typedOther.success);
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
            StringBuilder sb = new StringBuilder("getRootTabletLocation_result(");
            boolean first = true;
            sb.append("success:");
            if (this.success == null) {
                sb.append("null");
            } else {
                sb.append(this.success);
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

        private static class getRootTabletLocation_resultStandardSchemeFactory implements SchemeFactory {

            public getRootTabletLocation_resultStandardScheme getScheme() {
                return new getRootTabletLocation_resultStandardScheme();
            }
        }

        private static class getRootTabletLocation_resultStandardScheme extends StandardScheme<getRootTabletLocation_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, getRootTabletLocation_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 0:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.success = iprot.readString();
                                struct.setSuccessIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, getRootTabletLocation_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.success != null) {
                    oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
                    oprot.writeString(struct.success);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class getRootTabletLocation_resultTupleSchemeFactory implements SchemeFactory {

            public getRootTabletLocation_resultTupleScheme getScheme() {
                return new getRootTabletLocation_resultTupleScheme();
            }
        }

        private static class getRootTabletLocation_resultTupleScheme extends TupleScheme<getRootTabletLocation_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, getRootTabletLocation_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSuccess()) {
                    optionals.set(0);
                }
                oprot.writeBitSet(optionals, 1);
                if (struct.isSetSuccess()) {
                    oprot.writeString(struct.success);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, getRootTabletLocation_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.success = iprot.readString();
                    struct.setSuccessIsSet(true);
                }
            }
        }
    }

    public static class getInstanceId_args implements org.apache.thrift.TBase<getInstanceId_args, getInstanceId_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("getInstanceId_args");

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new getInstanceId_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new getInstanceId_argsTupleSchemeFactory());
        }

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {
            ;

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
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
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(getInstanceId_args.class, metaDataMap);
        }

        public getInstanceId_args() {
        }

        public getInstanceId_args(getInstanceId_args other) {
        }

        public getInstanceId_args deepCopy() {
            return new getInstanceId_args(this);
        }

        @Override
        public void clear() {
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof getInstanceId_args)
                return this.equals((getInstanceId_args) that);
            return false;
        }

        public boolean equals(getInstanceId_args that) {
            if (that == null)
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(getInstanceId_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            getInstanceId_args typedOther = (getInstanceId_args) other;
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
            StringBuilder sb = new StringBuilder("getInstanceId_args(");
            boolean first = true;
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

        private static class getInstanceId_argsStandardSchemeFactory implements SchemeFactory {

            public getInstanceId_argsStandardScheme getScheme() {
                return new getInstanceId_argsStandardScheme();
            }
        }

        private static class getInstanceId_argsStandardScheme extends StandardScheme<getInstanceId_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, getInstanceId_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        default:
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                    }
                    iprot.readFieldEnd();
                }
                iprot.readStructEnd();
                struct.validate();
            }

            public void write(org.apache.thrift.protocol.TProtocol oprot, getInstanceId_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class getInstanceId_argsTupleSchemeFactory implements SchemeFactory {

            public getInstanceId_argsTupleScheme getScheme() {
                return new getInstanceId_argsTupleScheme();
            }
        }

        private static class getInstanceId_argsTupleScheme extends TupleScheme<getInstanceId_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, getInstanceId_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, getInstanceId_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
            }
        }
    }

    public static class getInstanceId_result implements org.apache.thrift.TBase<getInstanceId_result, getInstanceId_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("getInstanceId_result");

        private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.STRING, (short) 0);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new getInstanceId_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new getInstanceId_resultTupleSchemeFactory());
        }

        public String success;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SUCCESS((short) 0, "success");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 0:
                        return SUCCESS;
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
            tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(getInstanceId_result.class, metaDataMap);
        }

        public getInstanceId_result() {
        }

        public getInstanceId_result(String success) {
            this();
            this.success = success;
        }

        public getInstanceId_result(getInstanceId_result other) {
            if (other.isSetSuccess()) {
                this.success = other.success;
            }
        }

        public getInstanceId_result deepCopy() {
            return new getInstanceId_result(this);
        }

        @Override
        public void clear() {
            this.success = null;
        }

        public String getSuccess() {
            return this.success;
        }

        public getInstanceId_result setSuccess(String success) {
            this.success = success;
            return this;
        }

        public void unsetSuccess() {
            this.success = null;
        }

        public boolean isSetSuccess() {
            return this.success != null;
        }

        public void setSuccessIsSet(boolean value) {
            if (!value) {
                this.success = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SUCCESS:
                    if (value == null) {
                        unsetSuccess();
                    } else {
                        setSuccess((String) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SUCCESS:
                    return getSuccess();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SUCCESS:
                    return isSetSuccess();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof getInstanceId_result)
                return this.equals((getInstanceId_result) that);
            return false;
        }

        public boolean equals(getInstanceId_result that) {
            if (that == null)
                return false;
            boolean this_present_success = true && this.isSetSuccess();
            boolean that_present_success = true && that.isSetSuccess();
            if (this_present_success || that_present_success) {
                if (!(this_present_success && that_present_success))
                    return false;
                if (!this.success.equals(that.success))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(getInstanceId_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            getInstanceId_result typedOther = (getInstanceId_result) other;
            lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSuccess()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, typedOther.success);
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
            StringBuilder sb = new StringBuilder("getInstanceId_result(");
            boolean first = true;
            sb.append("success:");
            if (this.success == null) {
                sb.append("null");
            } else {
                sb.append(this.success);
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

        private static class getInstanceId_resultStandardSchemeFactory implements SchemeFactory {

            public getInstanceId_resultStandardScheme getScheme() {
                return new getInstanceId_resultStandardScheme();
            }
        }

        private static class getInstanceId_resultStandardScheme extends StandardScheme<getInstanceId_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, getInstanceId_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 0:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.success = iprot.readString();
                                struct.setSuccessIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, getInstanceId_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.success != null) {
                    oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
                    oprot.writeString(struct.success);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class getInstanceId_resultTupleSchemeFactory implements SchemeFactory {

            public getInstanceId_resultTupleScheme getScheme() {
                return new getInstanceId_resultTupleScheme();
            }
        }

        private static class getInstanceId_resultTupleScheme extends TupleScheme<getInstanceId_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, getInstanceId_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSuccess()) {
                    optionals.set(0);
                }
                oprot.writeBitSet(optionals, 1);
                if (struct.isSetSuccess()) {
                    oprot.writeString(struct.success);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, getInstanceId_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.success = iprot.readString();
                    struct.setSuccessIsSet(true);
                }
            }
        }
    }

    public static class getZooKeepers_args implements org.apache.thrift.TBase<getZooKeepers_args, getZooKeepers_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("getZooKeepers_args");

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new getZooKeepers_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new getZooKeepers_argsTupleSchemeFactory());
        }

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {
            ;

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
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
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(getZooKeepers_args.class, metaDataMap);
        }

        public getZooKeepers_args() {
        }

        public getZooKeepers_args(getZooKeepers_args other) {
        }

        public getZooKeepers_args deepCopy() {
            return new getZooKeepers_args(this);
        }

        @Override
        public void clear() {
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof getZooKeepers_args)
                return this.equals((getZooKeepers_args) that);
            return false;
        }

        public boolean equals(getZooKeepers_args that) {
            if (that == null)
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(getZooKeepers_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            getZooKeepers_args typedOther = (getZooKeepers_args) other;
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
            StringBuilder sb = new StringBuilder("getZooKeepers_args(");
            boolean first = true;
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

        private static class getZooKeepers_argsStandardSchemeFactory implements SchemeFactory {

            public getZooKeepers_argsStandardScheme getScheme() {
                return new getZooKeepers_argsStandardScheme();
            }
        }

        private static class getZooKeepers_argsStandardScheme extends StandardScheme<getZooKeepers_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, getZooKeepers_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        default:
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                    }
                    iprot.readFieldEnd();
                }
                iprot.readStructEnd();
                struct.validate();
            }

            public void write(org.apache.thrift.protocol.TProtocol oprot, getZooKeepers_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class getZooKeepers_argsTupleSchemeFactory implements SchemeFactory {

            public getZooKeepers_argsTupleScheme getScheme() {
                return new getZooKeepers_argsTupleScheme();
            }
        }

        private static class getZooKeepers_argsTupleScheme extends TupleScheme<getZooKeepers_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, getZooKeepers_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, getZooKeepers_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
            }
        }
    }

    public static class getZooKeepers_result implements org.apache.thrift.TBase<getZooKeepers_result, getZooKeepers_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("getZooKeepers_result");

        private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.STRING, (short) 0);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new getZooKeepers_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new getZooKeepers_resultTupleSchemeFactory());
        }

        public String success;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SUCCESS((short) 0, "success");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 0:
                        return SUCCESS;
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
            tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(getZooKeepers_result.class, metaDataMap);
        }

        public getZooKeepers_result() {
        }

        public getZooKeepers_result(String success) {
            this();
            this.success = success;
        }

        public getZooKeepers_result(getZooKeepers_result other) {
            if (other.isSetSuccess()) {
                this.success = other.success;
            }
        }

        public getZooKeepers_result deepCopy() {
            return new getZooKeepers_result(this);
        }

        @Override
        public void clear() {
            this.success = null;
        }

        public String getSuccess() {
            return this.success;
        }

        public getZooKeepers_result setSuccess(String success) {
            this.success = success;
            return this;
        }

        public void unsetSuccess() {
            this.success = null;
        }

        public boolean isSetSuccess() {
            return this.success != null;
        }

        public void setSuccessIsSet(boolean value) {
            if (!value) {
                this.success = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SUCCESS:
                    if (value == null) {
                        unsetSuccess();
                    } else {
                        setSuccess((String) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SUCCESS:
                    return getSuccess();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SUCCESS:
                    return isSetSuccess();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof getZooKeepers_result)
                return this.equals((getZooKeepers_result) that);
            return false;
        }

        public boolean equals(getZooKeepers_result that) {
            if (that == null)
                return false;
            boolean this_present_success = true && this.isSetSuccess();
            boolean that_present_success = true && that.isSetSuccess();
            if (this_present_success || that_present_success) {
                if (!(this_present_success && that_present_success))
                    return false;
                if (!this.success.equals(that.success))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(getZooKeepers_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            getZooKeepers_result typedOther = (getZooKeepers_result) other;
            lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSuccess()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, typedOther.success);
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
            StringBuilder sb = new StringBuilder("getZooKeepers_result(");
            boolean first = true;
            sb.append("success:");
            if (this.success == null) {
                sb.append("null");
            } else {
                sb.append(this.success);
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

        private static class getZooKeepers_resultStandardSchemeFactory implements SchemeFactory {

            public getZooKeepers_resultStandardScheme getScheme() {
                return new getZooKeepers_resultStandardScheme();
            }
        }

        private static class getZooKeepers_resultStandardScheme extends StandardScheme<getZooKeepers_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, getZooKeepers_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 0:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.success = iprot.readString();
                                struct.setSuccessIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, getZooKeepers_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.success != null) {
                    oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
                    oprot.writeString(struct.success);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class getZooKeepers_resultTupleSchemeFactory implements SchemeFactory {

            public getZooKeepers_resultTupleScheme getScheme() {
                return new getZooKeepers_resultTupleScheme();
            }
        }

        private static class getZooKeepers_resultTupleScheme extends TupleScheme<getZooKeepers_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, getZooKeepers_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSuccess()) {
                    optionals.set(0);
                }
                oprot.writeBitSet(optionals, 1);
                if (struct.isSetSuccess()) {
                    oprot.writeString(struct.success);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, getZooKeepers_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.success = iprot.readString();
                    struct.setSuccessIsSet(true);
                }
            }
        }
    }

    public static class bulkImportFiles_args implements org.apache.thrift.TBase<bulkImportFiles_args, bulkImportFiles_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("bulkImportFiles_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField CREDENTIAL_FIELD_DESC = new org.apache.thrift.protocol.TField("credential", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

        private static final org.apache.thrift.protocol.TField TID_FIELD_DESC = new org.apache.thrift.protocol.TField("tid", org.apache.thrift.protocol.TType.I64, (short) 3);

        private static final org.apache.thrift.protocol.TField TABLE_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("tableId", org.apache.thrift.protocol.TType.STRING, (short) 4);

        private static final org.apache.thrift.protocol.TField FILES_FIELD_DESC = new org.apache.thrift.protocol.TField("files", org.apache.thrift.protocol.TType.LIST, (short) 5);

        private static final org.apache.thrift.protocol.TField ERROR_DIR_FIELD_DESC = new org.apache.thrift.protocol.TField("errorDir", org.apache.thrift.protocol.TType.STRING, (short) 6);

        private static final org.apache.thrift.protocol.TField SET_TIME_FIELD_DESC = new org.apache.thrift.protocol.TField("setTime", org.apache.thrift.protocol.TType.BOOL, (short) 7);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new bulkImportFiles_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new bulkImportFiles_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.Credential credential;

        public long tid;

        public String tableId;

        public List<String> files;

        public String errorDir;

        public boolean setTime;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 1, "tinfo"),
            CREDENTIAL((short) 2, "credential"),
            TID((short) 3, "tid"),
            TABLE_ID((short) 4, "tableId"),
            FILES((short) 5, "files"),
            ERROR_DIR((short) 6, "errorDir"),
            SET_TIME((short) 7, "setTime");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 1:
                        return TINFO;
                    case 2:
                        return CREDENTIAL;
                    case 3:
                        return TID;
                    case 4:
                        return TABLE_ID;
                    case 5:
                        return FILES;
                    case 6:
                        return ERROR_DIR;
                    case 7:
                        return SET_TIME;
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

        private static final int __TID_ISSET_ID = 0;

        private static final int __SETTIME_ISSET_ID = 1;

        private byte __isset_bitfield = 0;

        public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.CREDENTIAL, new org.apache.thrift.meta_data.FieldMetaData("credential", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.Credential.class)));
            tmpMap.put(_Fields.TID, new org.apache.thrift.meta_data.FieldMetaData("tid", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
            tmpMap.put(_Fields.TABLE_ID, new org.apache.thrift.meta_data.FieldMetaData("tableId", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.FILES, new org.apache.thrift.meta_data.FieldMetaData("files", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
            tmpMap.put(_Fields.ERROR_DIR, new org.apache.thrift.meta_data.FieldMetaData("errorDir", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.SET_TIME, new org.apache.thrift.meta_data.FieldMetaData("setTime", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(bulkImportFiles_args.class, metaDataMap);
        }

        public bulkImportFiles_args() {
        }

        public bulkImportFiles_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, long tid, String tableId, List<String> files, String errorDir, boolean setTime) {
            this();
            this.tinfo = tinfo;
            this.credential = credential;
            this.tid = tid;
            setTidIsSet(true);
            this.tableId = tableId;
            this.files = files;
            this.errorDir = errorDir;
            this.setTime = setTime;
            setSetTimeIsSet(true);
        }

        public bulkImportFiles_args(bulkImportFiles_args other) {
            __isset_bitfield = other.__isset_bitfield;
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredential()) {
                this.credential = new org.apache.accumulo.core.security.thrift.Credential(other.credential);
            }
            this.tid = other.tid;
            if (other.isSetTableId()) {
                this.tableId = other.tableId;
            }
            if (other.isSetFiles()) {
                List<String> __this__files = new ArrayList<String>();
                for (String other_element : other.files) {
                    __this__files.add(other_element);
                }
                this.files = __this__files;
            }
            if (other.isSetErrorDir()) {
                this.errorDir = other.errorDir;
            }
            this.setTime = other.setTime;
        }

        public bulkImportFiles_args deepCopy() {
            return new bulkImportFiles_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credential = null;
            setTidIsSet(false);
            this.tid = 0;
            this.tableId = null;
            this.files = null;
            this.errorDir = null;
            setSetTimeIsSet(false);
            this.setTime = false;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public bulkImportFiles_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
            this.tinfo = tinfo;
            return this;
        }

        public void unsetTinfo() {
            this.tinfo = null;
        }

        public boolean isSetTinfo() {
            return this.tinfo != null;
        }

        public void setTinfoIsSet(boolean value) {
            if (!value) {
                this.tinfo = null;
            }
        }

        public org.apache.accumulo.core.security.thrift.Credential getCredential() {
            return this.credential;
        }

        public bulkImportFiles_args setCredential(org.apache.accumulo.core.security.thrift.Credential credential) {
            this.credential = credential;
            return this;
        }

        public void unsetCredential() {
            this.credential = null;
        }

        public boolean isSetCredential() {
            return this.credential != null;
        }

        public void setCredentialIsSet(boolean value) {
            if (!value) {
                this.credential = null;
            }
        }

        public long getTid() {
            return this.tid;
        }

        public bulkImportFiles_args setTid(long tid) {
            this.tid = tid;
            setTidIsSet(true);
            return this;
        }

        public void unsetTid() {
            __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __TID_ISSET_ID);
        }

        public boolean isSetTid() {
            return EncodingUtils.testBit(__isset_bitfield, __TID_ISSET_ID);
        }

        public void setTidIsSet(boolean value) {
            __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __TID_ISSET_ID, value);
        }

        public String getTableId() {
            return this.tableId;
        }

        public bulkImportFiles_args setTableId(String tableId) {
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

        public int getFilesSize() {
            return (this.files == null) ? 0 : this.files.size();
        }

        public java.util.Iterator<String> getFilesIterator() {
            return (this.files == null) ? null : this.files.iterator();
        }

        public void addToFiles(String elem) {
            if (this.files == null) {
                this.files = new ArrayList<String>();
            }
            this.files.add(elem);
        }

        public List<String> getFiles() {
            return this.files;
        }

        public bulkImportFiles_args setFiles(List<String> files) {
            this.files = files;
            return this;
        }

        public void unsetFiles() {
            this.files = null;
        }

        public boolean isSetFiles() {
            return this.files != null;
        }

        public void setFilesIsSet(boolean value) {
            if (!value) {
                this.files = null;
            }
        }

        public String getErrorDir() {
            return this.errorDir;
        }

        public bulkImportFiles_args setErrorDir(String errorDir) {
            this.errorDir = errorDir;
            return this;
        }

        public void unsetErrorDir() {
            this.errorDir = null;
        }

        public boolean isSetErrorDir() {
            return this.errorDir != null;
        }

        public void setErrorDirIsSet(boolean value) {
            if (!value) {
                this.errorDir = null;
            }
        }

        public boolean isSetTime() {
            return this.setTime;
        }

        public bulkImportFiles_args setSetTime(boolean setTime) {
            this.setTime = setTime;
            setSetTimeIsSet(true);
            return this;
        }

        public void unsetSetTime() {
            __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __SETTIME_ISSET_ID);
        }

        public boolean isSetSetTime() {
            return EncodingUtils.testBit(__isset_bitfield, __SETTIME_ISSET_ID);
        }

        public void setSetTimeIsSet(boolean value) {
            __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __SETTIME_ISSET_ID, value);
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
                        setTinfo((org.apache.accumulo.trace.thrift.TInfo) value);
                    }
                    break;
                case CREDENTIAL:
                    if (value == null) {
                        unsetCredential();
                    } else {
                        setCredential((org.apache.accumulo.core.security.thrift.Credential) value);
                    }
                    break;
                case TID:
                    if (value == null) {
                        unsetTid();
                    } else {
                        setTid((Long) value);
                    }
                    break;
                case TABLE_ID:
                    if (value == null) {
                        unsetTableId();
                    } else {
                        setTableId((String) value);
                    }
                    break;
                case FILES:
                    if (value == null) {
                        unsetFiles();
                    } else {
                        setFiles((List<String>) value);
                    }
                    break;
                case ERROR_DIR:
                    if (value == null) {
                        unsetErrorDir();
                    } else {
                        setErrorDir((String) value);
                    }
                    break;
                case SET_TIME:
                    if (value == null) {
                        unsetSetTime();
                    } else {
                        setSetTime((Boolean) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIAL:
                    return getCredential();
                case TID:
                    return Long.valueOf(getTid());
                case TABLE_ID:
                    return getTableId();
                case FILES:
                    return getFiles();
                case ERROR_DIR:
                    return getErrorDir();
                case SET_TIME:
                    return Boolean.valueOf(isSetTime());
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case TINFO:
                    return isSetTinfo();
                case CREDENTIAL:
                    return isSetCredential();
                case TID:
                    return isSetTid();
                case TABLE_ID:
                    return isSetTableId();
                case FILES:
                    return isSetFiles();
                case ERROR_DIR:
                    return isSetErrorDir();
                case SET_TIME:
                    return isSetSetTime();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof bulkImportFiles_args)
                return this.equals((bulkImportFiles_args) that);
            return false;
        }

        public boolean equals(bulkImportFiles_args that) {
            if (that == null)
                return false;
            boolean this_present_tinfo = true && this.isSetTinfo();
            boolean that_present_tinfo = true && that.isSetTinfo();
            if (this_present_tinfo || that_present_tinfo) {
                if (!(this_present_tinfo && that_present_tinfo))
                    return false;
                if (!this.tinfo.equals(that.tinfo))
                    return false;
            }
            boolean this_present_credential = true && this.isSetCredential();
            boolean that_present_credential = true && that.isSetCredential();
            if (this_present_credential || that_present_credential) {
                if (!(this_present_credential && that_present_credential))
                    return false;
                if (!this.credential.equals(that.credential))
                    return false;
            }
            boolean this_present_tid = true;
            boolean that_present_tid = true;
            if (this_present_tid || that_present_tid) {
                if (!(this_present_tid && that_present_tid))
                    return false;
                if (this.tid != that.tid)
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
            boolean this_present_files = true && this.isSetFiles();
            boolean that_present_files = true && that.isSetFiles();
            if (this_present_files || that_present_files) {
                if (!(this_present_files && that_present_files))
                    return false;
                if (!this.files.equals(that.files))
                    return false;
            }
            boolean this_present_errorDir = true && this.isSetErrorDir();
            boolean that_present_errorDir = true && that.isSetErrorDir();
            if (this_present_errorDir || that_present_errorDir) {
                if (!(this_present_errorDir && that_present_errorDir))
                    return false;
                if (!this.errorDir.equals(that.errorDir))
                    return false;
            }
            boolean this_present_setTime = true;
            boolean that_present_setTime = true;
            if (this_present_setTime || that_present_setTime) {
                if (!(this_present_setTime && that_present_setTime))
                    return false;
                if (this.setTime != that.setTime)
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(bulkImportFiles_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            bulkImportFiles_args typedOther = (bulkImportFiles_args) other;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(typedOther.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, typedOther.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredential()).compareTo(typedOther.isSetCredential());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredential()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credential, typedOther.credential);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTid()).compareTo(typedOther.isSetTid());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTid()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tid, typedOther.tid);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTableId()).compareTo(typedOther.isSetTableId());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTableId()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tableId, typedOther.tableId);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetFiles()).compareTo(typedOther.isSetFiles());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetFiles()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.files, typedOther.files);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetErrorDir()).compareTo(typedOther.isSetErrorDir());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetErrorDir()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.errorDir, typedOther.errorDir);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetSetTime()).compareTo(typedOther.isSetSetTime());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSetTime()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.setTime, typedOther.setTime);
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
            StringBuilder sb = new StringBuilder("bulkImportFiles_args(");
            boolean first = true;
            sb.append("tinfo:");
            if (this.tinfo == null) {
                sb.append("null");
            } else {
                sb.append(this.tinfo);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("credential:");
            if (this.credential == null) {
                sb.append("null");
            } else {
                sb.append(this.credential);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("tid:");
            sb.append(this.tid);
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
            sb.append("files:");
            if (this.files == null) {
                sb.append("null");
            } else {
                sb.append(this.files);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("errorDir:");
            if (this.errorDir == null) {
                sb.append("null");
            } else {
                sb.append(this.errorDir);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("setTime:");
            sb.append(this.setTime);
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credential != null) {
                credential.validate();
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

        private static class bulkImportFiles_argsStandardSchemeFactory implements SchemeFactory {

            public bulkImportFiles_argsStandardScheme getScheme() {
                return new bulkImportFiles_argsStandardScheme();
            }
        }

        private static class bulkImportFiles_argsStandardScheme extends StandardScheme<bulkImportFiles_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, bulkImportFiles_args struct) throws org.apache.thrift.TException {
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
                                struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                                struct.tinfo.read(iprot);
                                struct.setTinfoIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                                struct.credential.read(iprot);
                                struct.setCredentialIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                                struct.tid = iprot.readI64();
                                struct.setTidIsSet(true);
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
                            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                                {
                                    org.apache.thrift.protocol.TList _list0 = iprot.readListBegin();
                                    struct.files = new ArrayList<String>(_list0.size);
                                    for (int _i1 = 0; _i1 < _list0.size; ++_i1) {
                                        String _elem2;
                                        _elem2 = iprot.readString();
                                        struct.files.add(_elem2);
                                    }
                                    iprot.readListEnd();
                                }
                                struct.setFilesIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 6:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.errorDir = iprot.readString();
                                struct.setErrorDirIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 7:
                            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
                                struct.setTime = iprot.readBool();
                                struct.setSetTimeIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, bulkImportFiles_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.credential != null) {
                    oprot.writeFieldBegin(CREDENTIAL_FIELD_DESC);
                    struct.credential.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldBegin(TID_FIELD_DESC);
                oprot.writeI64(struct.tid);
                oprot.writeFieldEnd();
                if (struct.tableId != null) {
                    oprot.writeFieldBegin(TABLE_ID_FIELD_DESC);
                    oprot.writeString(struct.tableId);
                    oprot.writeFieldEnd();
                }
                if (struct.files != null) {
                    oprot.writeFieldBegin(FILES_FIELD_DESC);
                    {
                        oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.files.size()));
                        for (String _iter3 : struct.files) {
                            oprot.writeString(_iter3);
                        }
                        oprot.writeListEnd();
                    }
                    oprot.writeFieldEnd();
                }
                if (struct.errorDir != null) {
                    oprot.writeFieldBegin(ERROR_DIR_FIELD_DESC);
                    oprot.writeString(struct.errorDir);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldBegin(SET_TIME_FIELD_DESC);
                oprot.writeBool(struct.setTime);
                oprot.writeFieldEnd();
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class bulkImportFiles_argsTupleSchemeFactory implements SchemeFactory {

            public bulkImportFiles_argsTupleScheme getScheme() {
                return new bulkImportFiles_argsTupleScheme();
            }
        }

        private static class bulkImportFiles_argsTupleScheme extends TupleScheme<bulkImportFiles_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, bulkImportFiles_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredential()) {
                    optionals.set(1);
                }
                if (struct.isSetTid()) {
                    optionals.set(2);
                }
                if (struct.isSetTableId()) {
                    optionals.set(3);
                }
                if (struct.isSetFiles()) {
                    optionals.set(4);
                }
                if (struct.isSetErrorDir()) {
                    optionals.set(5);
                }
                if (struct.isSetSetTime()) {
                    optionals.set(6);
                }
                oprot.writeBitSet(optionals, 7);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredential()) {
                    struct.credential.write(oprot);
                }
                if (struct.isSetTid()) {
                    oprot.writeI64(struct.tid);
                }
                if (struct.isSetTableId()) {
                    oprot.writeString(struct.tableId);
                }
                if (struct.isSetFiles()) {
                    {
                        oprot.writeI32(struct.files.size());
                        for (String _iter4 : struct.files) {
                            oprot.writeString(_iter4);
                        }
                    }
                }
                if (struct.isSetErrorDir()) {
                    oprot.writeString(struct.errorDir);
                }
                if (struct.isSetSetTime()) {
                    oprot.writeBool(struct.setTime);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, bulkImportFiles_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(7);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                    struct.credential.read(iprot);
                    struct.setCredentialIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.tid = iprot.readI64();
                    struct.setTidIsSet(true);
                }
                if (incoming.get(3)) {
                    struct.tableId = iprot.readString();
                    struct.setTableIdIsSet(true);
                }
                if (incoming.get(4)) {
                    {
                        org.apache.thrift.protocol.TList _list5 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                        struct.files = new ArrayList<String>(_list5.size);
                        for (int _i6 = 0; _i6 < _list5.size; ++_i6) {
                            String _elem7;
                            _elem7 = iprot.readString();
                            struct.files.add(_elem7);
                        }
                    }
                    struct.setFilesIsSet(true);
                }
                if (incoming.get(5)) {
                    struct.errorDir = iprot.readString();
                    struct.setErrorDirIsSet(true);
                }
                if (incoming.get(6)) {
                    struct.setTime = iprot.readBool();
                    struct.setSetTimeIsSet(true);
                }
            }
        }
    }

    public static class bulkImportFiles_result implements org.apache.thrift.TBase<bulkImportFiles_result, bulkImportFiles_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("bulkImportFiles_result");

        private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.LIST, (short) 0);

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField TOPE_FIELD_DESC = new org.apache.thrift.protocol.TField("tope", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new bulkImportFiles_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new bulkImportFiles_resultTupleSchemeFactory());
        }

        public List<String> success;

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException sec;

        public ThriftTableOperationException tope;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SUCCESS((short) 0, "success"), SEC((short) 1, "sec"), TOPE((short) 2, "tope");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 0:
                        return SUCCESS;
                    case 1:
                        return SEC;
                    case 2:
                        return TOPE;
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
            tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
            tmpMap.put(_Fields.SEC, new org.apache.thrift.meta_data.FieldMetaData("sec", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            tmpMap.put(_Fields.TOPE, new org.apache.thrift.meta_data.FieldMetaData("tope", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(bulkImportFiles_result.class, metaDataMap);
        }

        public bulkImportFiles_result() {
        }

        public bulkImportFiles_result(List<String> success, org.apache.accumulo.core.security.thrift.ThriftSecurityException sec, ThriftTableOperationException tope) {
            this();
            this.success = success;
            this.sec = sec;
            this.tope = tope;
        }

        public bulkImportFiles_result(bulkImportFiles_result other) {
            if (other.isSetSuccess()) {
                List<String> __this__success = new ArrayList<String>();
                for (String other_element : other.success) {
                    __this__success.add(other_element);
                }
                this.success = __this__success;
            }
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException(other.sec);
            }
            if (other.isSetTope()) {
                this.tope = new ThriftTableOperationException(other.tope);
            }
        }

        public bulkImportFiles_result deepCopy() {
            return new bulkImportFiles_result(this);
        }

        @Override
        public void clear() {
            this.success = null;
            this.sec = null;
            this.tope = null;
        }

        public int getSuccessSize() {
            return (this.success == null) ? 0 : this.success.size();
        }

        public java.util.Iterator<String> getSuccessIterator() {
            return (this.success == null) ? null : this.success.iterator();
        }

        public void addToSuccess(String elem) {
            if (this.success == null) {
                this.success = new ArrayList<String>();
            }
            this.success.add(elem);
        }

        public List<String> getSuccess() {
            return this.success;
        }

        public bulkImportFiles_result setSuccess(List<String> success) {
            this.success = success;
            return this;
        }

        public void unsetSuccess() {
            this.success = null;
        }

        public boolean isSetSuccess() {
            return this.success != null;
        }

        public void setSuccessIsSet(boolean value) {
            if (!value) {
                this.success = null;
            }
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public bulkImportFiles_result setSec(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this.sec = sec;
            return this;
        }

        public void unsetSec() {
            this.sec = null;
        }

        public boolean isSetSec() {
            return this.sec != null;
        }

        public void setSecIsSet(boolean value) {
            if (!value) {
                this.sec = null;
            }
        }

        public ThriftTableOperationException getTope() {
            return this.tope;
        }

        public bulkImportFiles_result setTope(ThriftTableOperationException tope) {
            this.tope = tope;
            return this;
        }

        public void unsetTope() {
            this.tope = null;
        }

        public boolean isSetTope() {
            return this.tope != null;
        }

        public void setTopeIsSet(boolean value) {
            if (!value) {
                this.tope = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SUCCESS:
                    if (value == null) {
                        unsetSuccess();
                    } else {
                        setSuccess((List<String>) value);
                    }
                    break;
                case SEC:
                    if (value == null) {
                        unsetSec();
                    } else {
                        setSec((org.apache.accumulo.core.security.thrift.ThriftSecurityException) value);
                    }
                    break;
                case TOPE:
                    if (value == null) {
                        unsetTope();
                    } else {
                        setTope((ThriftTableOperationException) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SUCCESS:
                    return getSuccess();
                case SEC:
                    return getSec();
                case TOPE:
                    return getTope();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SUCCESS:
                    return isSetSuccess();
                case SEC:
                    return isSetSec();
                case TOPE:
                    return isSetTope();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof bulkImportFiles_result)
                return this.equals((bulkImportFiles_result) that);
            return false;
        }

        public boolean equals(bulkImportFiles_result that) {
            if (that == null)
                return false;
            boolean this_present_success = true && this.isSetSuccess();
            boolean that_present_success = true && that.isSetSuccess();
            if (this_present_success || that_present_success) {
                if (!(this_present_success && that_present_success))
                    return false;
                if (!this.success.equals(that.success))
                    return false;
            }
            boolean this_present_sec = true && this.isSetSec();
            boolean that_present_sec = true && that.isSetSec();
            if (this_present_sec || that_present_sec) {
                if (!(this_present_sec && that_present_sec))
                    return false;
                if (!this.sec.equals(that.sec))
                    return false;
            }
            boolean this_present_tope = true && this.isSetTope();
            boolean that_present_tope = true && that.isSetTope();
            if (this_present_tope || that_present_tope) {
                if (!(this_present_tope && that_present_tope))
                    return false;
                if (!this.tope.equals(that.tope))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(bulkImportFiles_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            bulkImportFiles_result typedOther = (bulkImportFiles_result) other;
            lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSuccess()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, typedOther.success);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(typedOther.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, typedOther.sec);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTope()).compareTo(typedOther.isSetTope());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTope()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tope, typedOther.tope);
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
            StringBuilder sb = new StringBuilder("bulkImportFiles_result(");
            boolean first = true;
            sb.append("success:");
            if (this.success == null) {
                sb.append("null");
            } else {
                sb.append(this.success);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("sec:");
            if (this.sec == null) {
                sb.append("null");
            } else {
                sb.append(this.sec);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("tope:");
            if (this.tope == null) {
                sb.append("null");
            } else {
                sb.append(this.tope);
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

        private static class bulkImportFiles_resultStandardSchemeFactory implements SchemeFactory {

            public bulkImportFiles_resultStandardScheme getScheme() {
                return new bulkImportFiles_resultStandardScheme();
            }
        }

        private static class bulkImportFiles_resultStandardScheme extends StandardScheme<bulkImportFiles_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, bulkImportFiles_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 0:
                            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                                {
                                    org.apache.thrift.protocol.TList _list8 = iprot.readListBegin();
                                    struct.success = new ArrayList<String>(_list8.size);
                                    for (int _i9 = 0; _i9 < _list8.size; ++_i9) {
                                        String _elem10;
                                        _elem10 = iprot.readString();
                                        struct.success.add(_elem10);
                                    }
                                    iprot.readListEnd();
                                }
                                struct.setSuccessIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tope = new ThriftTableOperationException();
                                struct.tope.read(iprot);
                                struct.setTopeIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, bulkImportFiles_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.success != null) {
                    oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
                    {
                        oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.success.size()));
                        for (String _iter11 : struct.success) {
                            oprot.writeString(_iter11);
                        }
                        oprot.writeListEnd();
                    }
                    oprot.writeFieldEnd();
                }
                if (struct.sec != null) {
                    oprot.writeFieldBegin(SEC_FIELD_DESC);
                    struct.sec.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.tope != null) {
                    oprot.writeFieldBegin(TOPE_FIELD_DESC);
                    struct.tope.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class bulkImportFiles_resultTupleSchemeFactory implements SchemeFactory {

            public bulkImportFiles_resultTupleScheme getScheme() {
                return new bulkImportFiles_resultTupleScheme();
            }
        }

        private static class bulkImportFiles_resultTupleScheme extends TupleScheme<bulkImportFiles_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, bulkImportFiles_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSuccess()) {
                    optionals.set(0);
                }
                if (struct.isSetSec()) {
                    optionals.set(1);
                }
                if (struct.isSetTope()) {
                    optionals.set(2);
                }
                oprot.writeBitSet(optionals, 3);
                if (struct.isSetSuccess()) {
                    {
                        oprot.writeI32(struct.success.size());
                        for (String _iter12 : struct.success) {
                            oprot.writeString(_iter12);
                        }
                    }
                }
                if (struct.isSetSec()) {
                    struct.sec.write(oprot);
                }
                if (struct.isSetTope()) {
                    struct.tope.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, bulkImportFiles_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(3);
                if (incoming.get(0)) {
                    {
                        org.apache.thrift.protocol.TList _list13 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                        struct.success = new ArrayList<String>(_list13.size);
                        for (int _i14 = 0; _i14 < _list13.size; ++_i14) {
                            String _elem15;
                            _elem15 = iprot.readString();
                            struct.success.add(_elem15);
                        }
                    }
                    struct.setSuccessIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.tope = new ThriftTableOperationException();
                    struct.tope.read(iprot);
                    struct.setTopeIsSet(true);
                }
            }
        }
    }

    public static class isActive_args implements org.apache.thrift.TBase<isActive_args, isActive_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("isActive_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField TID_FIELD_DESC = new org.apache.thrift.protocol.TField("tid", org.apache.thrift.protocol.TType.I64, (short) 2);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new isActive_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new isActive_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public long tid;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 1, "tinfo"), TID((short) 2, "tid");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 1:
                        return TINFO;
                    case 2:
                        return TID;
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

        private static final int __TID_ISSET_ID = 0;

        private byte __isset_bitfield = 0;

        public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.TID, new org.apache.thrift.meta_data.FieldMetaData("tid", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(isActive_args.class, metaDataMap);
        }

        public isActive_args() {
        }

        public isActive_args(org.apache.accumulo.trace.thrift.TInfo tinfo, long tid) {
            this();
            this.tinfo = tinfo;
            this.tid = tid;
            setTidIsSet(true);
        }

        public isActive_args(isActive_args other) {
            __isset_bitfield = other.__isset_bitfield;
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            this.tid = other.tid;
        }

        public isActive_args deepCopy() {
            return new isActive_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            setTidIsSet(false);
            this.tid = 0;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public isActive_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
            this.tinfo = tinfo;
            return this;
        }

        public void unsetTinfo() {
            this.tinfo = null;
        }

        public boolean isSetTinfo() {
            return this.tinfo != null;
        }

        public void setTinfoIsSet(boolean value) {
            if (!value) {
                this.tinfo = null;
            }
        }

        public long getTid() {
            return this.tid;
        }

        public isActive_args setTid(long tid) {
            this.tid = tid;
            setTidIsSet(true);
            return this;
        }

        public void unsetTid() {
            __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __TID_ISSET_ID);
        }

        public boolean isSetTid() {
            return EncodingUtils.testBit(__isset_bitfield, __TID_ISSET_ID);
        }

        public void setTidIsSet(boolean value) {
            __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __TID_ISSET_ID, value);
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
                        setTinfo((org.apache.accumulo.trace.thrift.TInfo) value);
                    }
                    break;
                case TID:
                    if (value == null) {
                        unsetTid();
                    } else {
                        setTid((Long) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case TID:
                    return Long.valueOf(getTid());
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case TINFO:
                    return isSetTinfo();
                case TID:
                    return isSetTid();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof isActive_args)
                return this.equals((isActive_args) that);
            return false;
        }

        public boolean equals(isActive_args that) {
            if (that == null)
                return false;
            boolean this_present_tinfo = true && this.isSetTinfo();
            boolean that_present_tinfo = true && that.isSetTinfo();
            if (this_present_tinfo || that_present_tinfo) {
                if (!(this_present_tinfo && that_present_tinfo))
                    return false;
                if (!this.tinfo.equals(that.tinfo))
                    return false;
            }
            boolean this_present_tid = true;
            boolean that_present_tid = true;
            if (this_present_tid || that_present_tid) {
                if (!(this_present_tid && that_present_tid))
                    return false;
                if (this.tid != that.tid)
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(isActive_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            isActive_args typedOther = (isActive_args) other;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(typedOther.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, typedOther.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTid()).compareTo(typedOther.isSetTid());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTid()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tid, typedOther.tid);
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
            StringBuilder sb = new StringBuilder("isActive_args(");
            boolean first = true;
            sb.append("tinfo:");
            if (this.tinfo == null) {
                sb.append("null");
            } else {
                sb.append(this.tinfo);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("tid:");
            sb.append(this.tid);
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
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

        private static class isActive_argsStandardSchemeFactory implements SchemeFactory {

            public isActive_argsStandardScheme getScheme() {
                return new isActive_argsStandardScheme();
            }
        }

        private static class isActive_argsStandardScheme extends StandardScheme<isActive_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, isActive_args struct) throws org.apache.thrift.TException {
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
                                struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                                struct.tinfo.read(iprot);
                                struct.setTinfoIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                                struct.tid = iprot.readI64();
                                struct.setTidIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, isActive_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldBegin(TID_FIELD_DESC);
                oprot.writeI64(struct.tid);
                oprot.writeFieldEnd();
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class isActive_argsTupleSchemeFactory implements SchemeFactory {

            public isActive_argsTupleScheme getScheme() {
                return new isActive_argsTupleScheme();
            }
        }

        private static class isActive_argsTupleScheme extends TupleScheme<isActive_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, isActive_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetTid()) {
                    optionals.set(1);
                }
                oprot.writeBitSet(optionals, 2);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetTid()) {
                    oprot.writeI64(struct.tid);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, isActive_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.tid = iprot.readI64();
                    struct.setTidIsSet(true);
                }
            }
        }
    }

    public static class isActive_result implements org.apache.thrift.TBase<isActive_result, isActive_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("isActive_result");

        private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.BOOL, (short) 0);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new isActive_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new isActive_resultTupleSchemeFactory());
        }

        public boolean success;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SUCCESS((short) 0, "success");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 0:
                        return SUCCESS;
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

        private static final int __SUCCESS_ISSET_ID = 0;

        private byte __isset_bitfield = 0;

        public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(isActive_result.class, metaDataMap);
        }

        public isActive_result() {
        }

        public isActive_result(boolean success) {
            this();
            this.success = success;
            setSuccessIsSet(true);
        }

        public isActive_result(isActive_result other) {
            __isset_bitfield = other.__isset_bitfield;
            this.success = other.success;
        }

        public isActive_result deepCopy() {
            return new isActive_result(this);
        }

        @Override
        public void clear() {
            setSuccessIsSet(false);
            this.success = false;
        }

        public boolean isSuccess() {
            return this.success;
        }

        public isActive_result setSuccess(boolean success) {
            this.success = success;
            setSuccessIsSet(true);
            return this;
        }

        public void unsetSuccess() {
            __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __SUCCESS_ISSET_ID);
        }

        public boolean isSetSuccess() {
            return EncodingUtils.testBit(__isset_bitfield, __SUCCESS_ISSET_ID);
        }

        public void setSuccessIsSet(boolean value) {
            __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __SUCCESS_ISSET_ID, value);
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SUCCESS:
                    if (value == null) {
                        unsetSuccess();
                    } else {
                        setSuccess((Boolean) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SUCCESS:
                    return Boolean.valueOf(isSuccess());
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SUCCESS:
                    return isSetSuccess();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof isActive_result)
                return this.equals((isActive_result) that);
            return false;
        }

        public boolean equals(isActive_result that) {
            if (that == null)
                return false;
            boolean this_present_success = true;
            boolean that_present_success = true;
            if (this_present_success || that_present_success) {
                if (!(this_present_success && that_present_success))
                    return false;
                if (this.success != that.success)
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(isActive_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            isActive_result typedOther = (isActive_result) other;
            lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSuccess()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, typedOther.success);
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
            StringBuilder sb = new StringBuilder("isActive_result(");
            boolean first = true;
            sb.append("success:");
            sb.append(this.success);
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

        private static class isActive_resultStandardSchemeFactory implements SchemeFactory {

            public isActive_resultStandardScheme getScheme() {
                return new isActive_resultStandardScheme();
            }
        }

        private static class isActive_resultStandardScheme extends StandardScheme<isActive_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, isActive_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 0:
                            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
                                struct.success = iprot.readBool();
                                struct.setSuccessIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, isActive_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.isSetSuccess()) {
                    oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
                    oprot.writeBool(struct.success);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class isActive_resultTupleSchemeFactory implements SchemeFactory {

            public isActive_resultTupleScheme getScheme() {
                return new isActive_resultTupleScheme();
            }
        }

        private static class isActive_resultTupleScheme extends TupleScheme<isActive_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, isActive_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSuccess()) {
                    optionals.set(0);
                }
                oprot.writeBitSet(optionals, 1);
                if (struct.isSetSuccess()) {
                    oprot.writeBool(struct.success);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, isActive_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.success = iprot.readBool();
                    struct.setSuccessIsSet(true);
                }
            }
        }
    }

    public static class ping_args implements org.apache.thrift.TBase<ping_args, ping_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ping_args");

        private static final org.apache.thrift.protocol.TField CREDENTIAL_FIELD_DESC = new org.apache.thrift.protocol.TField("credential", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new ping_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new ping_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.core.security.thrift.Credential credential;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            CREDENTIAL((short) 1, "credential");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 1:
                        return CREDENTIAL;
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
            tmpMap.put(_Fields.CREDENTIAL, new org.apache.thrift.meta_data.FieldMetaData("credential", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.Credential.class)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ping_args.class, metaDataMap);
        }

        public ping_args() {
        }

        public ping_args(org.apache.accumulo.core.security.thrift.Credential credential) {
            this();
            this.credential = credential;
        }

        public ping_args(ping_args other) {
            if (other.isSetCredential()) {
                this.credential = new org.apache.accumulo.core.security.thrift.Credential(other.credential);
            }
        }

        public ping_args deepCopy() {
            return new ping_args(this);
        }

        @Override
        public void clear() {
            this.credential = null;
        }

        public org.apache.accumulo.core.security.thrift.Credential getCredential() {
            return this.credential;
        }

        public ping_args setCredential(org.apache.accumulo.core.security.thrift.Credential credential) {
            this.credential = credential;
            return this;
        }

        public void unsetCredential() {
            this.credential = null;
        }

        public boolean isSetCredential() {
            return this.credential != null;
        }

        public void setCredentialIsSet(boolean value) {
            if (!value) {
                this.credential = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case CREDENTIAL:
                    if (value == null) {
                        unsetCredential();
                    } else {
                        setCredential((org.apache.accumulo.core.security.thrift.Credential) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case CREDENTIAL:
                    return getCredential();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case CREDENTIAL:
                    return isSetCredential();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof ping_args)
                return this.equals((ping_args) that);
            return false;
        }

        public boolean equals(ping_args that) {
            if (that == null)
                return false;
            boolean this_present_credential = true && this.isSetCredential();
            boolean that_present_credential = true && that.isSetCredential();
            if (this_present_credential || that_present_credential) {
                if (!(this_present_credential && that_present_credential))
                    return false;
                if (!this.credential.equals(that.credential))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(ping_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            ping_args typedOther = (ping_args) other;
            lastComparison = Boolean.valueOf(isSetCredential()).compareTo(typedOther.isSetCredential());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredential()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credential, typedOther.credential);
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
            StringBuilder sb = new StringBuilder("ping_args(");
            boolean first = true;
            sb.append("credential:");
            if (this.credential == null) {
                sb.append("null");
            } else {
                sb.append(this.credential);
            }
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (credential != null) {
                credential.validate();
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
                read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
            } catch (org.apache.thrift.TException te) {
                throw new java.io.IOException(te);
            }
        }

        private static class ping_argsStandardSchemeFactory implements SchemeFactory {

            public ping_argsStandardScheme getScheme() {
                return new ping_argsStandardScheme();
            }
        }

        private static class ping_argsStandardScheme extends StandardScheme<ping_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, ping_args struct) throws org.apache.thrift.TException {
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
                                struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                                struct.credential.read(iprot);
                                struct.setCredentialIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, ping_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credential != null) {
                    oprot.writeFieldBegin(CREDENTIAL_FIELD_DESC);
                    struct.credential.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class ping_argsTupleSchemeFactory implements SchemeFactory {

            public ping_argsTupleScheme getScheme() {
                return new ping_argsTupleScheme();
            }
        }

        private static class ping_argsTupleScheme extends TupleScheme<ping_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, ping_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetCredential()) {
                    optionals.set(0);
                }
                oprot.writeBitSet(optionals, 1);
                if (struct.isSetCredential()) {
                    struct.credential.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, ping_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                    struct.credential.read(iprot);
                    struct.setCredentialIsSet(true);
                }
            }
        }
    }

    public static class ping_result implements org.apache.thrift.TBase<ping_result, ping_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ping_result");

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new ping_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new ping_resultTupleSchemeFactory());
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException sec;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SEC((short) 1, "sec");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 1:
                        return SEC;
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
            tmpMap.put(_Fields.SEC, new org.apache.thrift.meta_data.FieldMetaData("sec", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ping_result.class, metaDataMap);
        }

        public ping_result() {
        }

        public ping_result(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this();
            this.sec = sec;
        }

        public ping_result(ping_result other) {
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException(other.sec);
            }
        }

        public ping_result deepCopy() {
            return new ping_result(this);
        }

        @Override
        public void clear() {
            this.sec = null;
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public ping_result setSec(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this.sec = sec;
            return this;
        }

        public void unsetSec() {
            this.sec = null;
        }

        public boolean isSetSec() {
            return this.sec != null;
        }

        public void setSecIsSet(boolean value) {
            if (!value) {
                this.sec = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SEC:
                    if (value == null) {
                        unsetSec();
                    } else {
                        setSec((org.apache.accumulo.core.security.thrift.ThriftSecurityException) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SEC:
                    return getSec();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SEC:
                    return isSetSec();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof ping_result)
                return this.equals((ping_result) that);
            return false;
        }

        public boolean equals(ping_result that) {
            if (that == null)
                return false;
            boolean this_present_sec = true && this.isSetSec();
            boolean that_present_sec = true && that.isSetSec();
            if (this_present_sec || that_present_sec) {
                if (!(this_present_sec && that_present_sec))
                    return false;
                if (!this.sec.equals(that.sec))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(ping_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            ping_result typedOther = (ping_result) other;
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(typedOther.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, typedOther.sec);
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
            StringBuilder sb = new StringBuilder("ping_result(");
            boolean first = true;
            sb.append("sec:");
            if (this.sec == null) {
                sb.append("null");
            } else {
                sb.append(this.sec);
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

        private static class ping_resultStandardSchemeFactory implements SchemeFactory {

            public ping_resultStandardScheme getScheme() {
                return new ping_resultStandardScheme();
            }
        }

        private static class ping_resultStandardScheme extends StandardScheme<ping_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, ping_result struct) throws org.apache.thrift.TException {
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
                                struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, ping_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.sec != null) {
                    oprot.writeFieldBegin(SEC_FIELD_DESC);
                    struct.sec.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class ping_resultTupleSchemeFactory implements SchemeFactory {

            public ping_resultTupleScheme getScheme() {
                return new ping_resultTupleScheme();
            }
        }

        private static class ping_resultTupleScheme extends TupleScheme<ping_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, ping_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSec()) {
                    optionals.set(0);
                }
                oprot.writeBitSet(optionals, 1);
                if (struct.isSetSec()) {
                    struct.sec.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, ping_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
            }
        }
    }

    public static class authenticateUser_args implements org.apache.thrift.TBase<authenticateUser_args, authenticateUser_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("authenticateUser_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 4);

        private static final org.apache.thrift.protocol.TField CREDENTIAL_FIELD_DESC = new org.apache.thrift.protocol.TField("credential", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField TO_AUTHENTICATE_FIELD_DESC = new org.apache.thrift.protocol.TField("toAuthenticate", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new authenticateUser_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new authenticateUser_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.Credential credential;

        public org.apache.accumulo.core.security.thrift.Credential toAuthenticate;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 4, "tinfo"), CREDENTIAL((short) 1, "credential"), TO_AUTHENTICATE((short) 2, "toAuthenticate");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 4:
                        return TINFO;
                    case 1:
                        return CREDENTIAL;
                    case 2:
                        return TO_AUTHENTICATE;
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
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.CREDENTIAL, new org.apache.thrift.meta_data.FieldMetaData("credential", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.Credential.class)));
            tmpMap.put(_Fields.TO_AUTHENTICATE, new org.apache.thrift.meta_data.FieldMetaData("toAuthenticate", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.Credential.class)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(authenticateUser_args.class, metaDataMap);
        }

        public authenticateUser_args() {
        }

        public authenticateUser_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toAuthenticate) {
            this();
            this.tinfo = tinfo;
            this.credential = credential;
            this.toAuthenticate = toAuthenticate;
        }

        public authenticateUser_args(authenticateUser_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredential()) {
                this.credential = new org.apache.accumulo.core.security.thrift.Credential(other.credential);
            }
            if (other.isSetToAuthenticate()) {
                this.toAuthenticate = new org.apache.accumulo.core.security.thrift.Credential(other.toAuthenticate);
            }
        }

        public authenticateUser_args deepCopy() {
            return new authenticateUser_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credential = null;
            this.toAuthenticate = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public authenticateUser_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
            this.tinfo = tinfo;
            return this;
        }

        public void unsetTinfo() {
            this.tinfo = null;
        }

        public boolean isSetTinfo() {
            return this.tinfo != null;
        }

        public void setTinfoIsSet(boolean value) {
            if (!value) {
                this.tinfo = null;
            }
        }

        public org.apache.accumulo.core.security.thrift.Credential getCredential() {
            return this.credential;
        }

        public authenticateUser_args setCredential(org.apache.accumulo.core.security.thrift.Credential credential) {
            this.credential = credential;
            return this;
        }

        public void unsetCredential() {
            this.credential = null;
        }

        public boolean isSetCredential() {
            return this.credential != null;
        }

        public void setCredentialIsSet(boolean value) {
            if (!value) {
                this.credential = null;
            }
        }

        public org.apache.accumulo.core.security.thrift.Credential getToAuthenticate() {
            return this.toAuthenticate;
        }

        public authenticateUser_args setToAuthenticate(org.apache.accumulo.core.security.thrift.Credential toAuthenticate) {
            this.toAuthenticate = toAuthenticate;
            return this;
        }

        public void unsetToAuthenticate() {
            this.toAuthenticate = null;
        }

        public boolean isSetToAuthenticate() {
            return this.toAuthenticate != null;
        }

        public void setToAuthenticateIsSet(boolean value) {
            if (!value) {
                this.toAuthenticate = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
                        setTinfo((org.apache.accumulo.trace.thrift.TInfo) value);
                    }
                    break;
                case CREDENTIAL:
                    if (value == null) {
                        unsetCredential();
                    } else {
                        setCredential((org.apache.accumulo.core.security.thrift.Credential) value);
                    }
                    break;
                case TO_AUTHENTICATE:
                    if (value == null) {
                        unsetToAuthenticate();
                    } else {
                        setToAuthenticate((org.apache.accumulo.core.security.thrift.Credential) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIAL:
                    return getCredential();
                case TO_AUTHENTICATE:
                    return getToAuthenticate();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case TINFO:
                    return isSetTinfo();
                case CREDENTIAL:
                    return isSetCredential();
                case TO_AUTHENTICATE:
                    return isSetToAuthenticate();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof authenticateUser_args)
                return this.equals((authenticateUser_args) that);
            return false;
        }

        public boolean equals(authenticateUser_args that) {
            if (that == null)
                return false;
            boolean this_present_tinfo = true && this.isSetTinfo();
            boolean that_present_tinfo = true && that.isSetTinfo();
            if (this_present_tinfo || that_present_tinfo) {
                if (!(this_present_tinfo && that_present_tinfo))
                    return false;
                if (!this.tinfo.equals(that.tinfo))
                    return false;
            }
            boolean this_present_credential = true && this.isSetCredential();
            boolean that_present_credential = true && that.isSetCredential();
            if (this_present_credential || that_present_credential) {
                if (!(this_present_credential && that_present_credential))
                    return false;
                if (!this.credential.equals(that.credential))
                    return false;
            }
            boolean this_present_toAuthenticate = true && this.isSetToAuthenticate();
            boolean that_present_toAuthenticate = true && that.isSetToAuthenticate();
            if (this_present_toAuthenticate || that_present_toAuthenticate) {
                if (!(this_present_toAuthenticate && that_present_toAuthenticate))
                    return false;
                if (!this.toAuthenticate.equals(that.toAuthenticate))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(authenticateUser_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            authenticateUser_args typedOther = (authenticateUser_args) other;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(typedOther.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, typedOther.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredential()).compareTo(typedOther.isSetCredential());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredential()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credential, typedOther.credential);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetToAuthenticate()).compareTo(typedOther.isSetToAuthenticate());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetToAuthenticate()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.toAuthenticate, typedOther.toAuthenticate);
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
            StringBuilder sb = new StringBuilder("authenticateUser_args(");
            boolean first = true;
            sb.append("tinfo:");
            if (this.tinfo == null) {
                sb.append("null");
            } else {
                sb.append(this.tinfo);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("credential:");
            if (this.credential == null) {
                sb.append("null");
            } else {
                sb.append(this.credential);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("toAuthenticate:");
            if (this.toAuthenticate == null) {
                sb.append("null");
            } else {
                sb.append(this.toAuthenticate);
            }
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credential != null) {
                credential.validate();
            }
            if (toAuthenticate != null) {
                toAuthenticate.validate();
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
                read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
            } catch (org.apache.thrift.TException te) {
                throw new java.io.IOException(te);
            }
        }

        private static class authenticateUser_argsStandardSchemeFactory implements SchemeFactory {

            public authenticateUser_argsStandardScheme getScheme() {
                return new authenticateUser_argsStandardScheme();
            }
        }

        private static class authenticateUser_argsStandardScheme extends StandardScheme<authenticateUser_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, authenticateUser_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 4:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                                struct.tinfo.read(iprot);
                                struct.setTinfoIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                                struct.credential.read(iprot);
                                struct.setCredentialIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.toAuthenticate = new org.apache.accumulo.core.security.thrift.Credential();
                                struct.toAuthenticate.read(iprot);
                                struct.setToAuthenticateIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, authenticateUser_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credential != null) {
                    oprot.writeFieldBegin(CREDENTIAL_FIELD_DESC);
                    struct.credential.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.toAuthenticate != null) {
                    oprot.writeFieldBegin(TO_AUTHENTICATE_FIELD_DESC);
                    struct.toAuthenticate.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class authenticateUser_argsTupleSchemeFactory implements SchemeFactory {

            public authenticateUser_argsTupleScheme getScheme() {
                return new authenticateUser_argsTupleScheme();
            }
        }

        private static class authenticateUser_argsTupleScheme extends TupleScheme<authenticateUser_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, authenticateUser_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredential()) {
                    optionals.set(1);
                }
                if (struct.isSetToAuthenticate()) {
                    optionals.set(2);
                }
                oprot.writeBitSet(optionals, 3);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredential()) {
                    struct.credential.write(oprot);
                }
                if (struct.isSetToAuthenticate()) {
                    struct.toAuthenticate.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, authenticateUser_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(3);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                    struct.credential.read(iprot);
                    struct.setCredentialIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.toAuthenticate = new org.apache.accumulo.core.security.thrift.Credential();
                    struct.toAuthenticate.read(iprot);
                    struct.setToAuthenticateIsSet(true);
                }
            }
        }
    }

    public static class authenticateUser_result implements org.apache.thrift.TBase<authenticateUser_result, authenticateUser_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("authenticateUser_result");

        private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.BOOL, (short) 0);

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new authenticateUser_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new authenticateUser_resultTupleSchemeFactory());
        }

        public boolean success;

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException sec;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SUCCESS((short) 0, "success"), SEC((short) 1, "sec");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 0:
                        return SUCCESS;
                    case 1:
                        return SEC;
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

        private static final int __SUCCESS_ISSET_ID = 0;

        private byte __isset_bitfield = 0;

        public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
            tmpMap.put(_Fields.SEC, new org.apache.thrift.meta_data.FieldMetaData("sec", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(authenticateUser_result.class, metaDataMap);
        }

        public authenticateUser_result() {
        }

        public authenticateUser_result(boolean success, org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this();
            this.success = success;
            setSuccessIsSet(true);
            this.sec = sec;
        }

        public authenticateUser_result(authenticateUser_result other) {
            __isset_bitfield = other.__isset_bitfield;
            this.success = other.success;
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException(other.sec);
            }
        }

        public authenticateUser_result deepCopy() {
            return new authenticateUser_result(this);
        }

        @Override
        public void clear() {
            setSuccessIsSet(false);
            this.success = false;
            this.sec = null;
        }

        public boolean isSuccess() {
            return this.success;
        }

        public authenticateUser_result setSuccess(boolean success) {
            this.success = success;
            setSuccessIsSet(true);
            return this;
        }

        public void unsetSuccess() {
            __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __SUCCESS_ISSET_ID);
        }

        public boolean isSetSuccess() {
            return EncodingUtils.testBit(__isset_bitfield, __SUCCESS_ISSET_ID);
        }

        public void setSuccessIsSet(boolean value) {
            __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __SUCCESS_ISSET_ID, value);
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public authenticateUser_result setSec(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this.sec = sec;
            return this;
        }

        public void unsetSec() {
            this.sec = null;
        }

        public boolean isSetSec() {
            return this.sec != null;
        }

        public void setSecIsSet(boolean value) {
            if (!value) {
                this.sec = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SUCCESS:
                    if (value == null) {
                        unsetSuccess();
                    } else {
                        setSuccess((Boolean) value);
                    }
                    break;
                case SEC:
                    if (value == null) {
                        unsetSec();
                    } else {
                        setSec((org.apache.accumulo.core.security.thrift.ThriftSecurityException) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SUCCESS:
                    return Boolean.valueOf(isSuccess());
                case SEC:
                    return getSec();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SUCCESS:
                    return isSetSuccess();
                case SEC:
                    return isSetSec();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof authenticateUser_result)
                return this.equals((authenticateUser_result) that);
            return false;
        }

        public boolean equals(authenticateUser_result that) {
            if (that == null)
                return false;
            boolean this_present_success = true;
            boolean that_present_success = true;
            if (this_present_success || that_present_success) {
                if (!(this_present_success && that_present_success))
                    return false;
                if (this.success != that.success)
                    return false;
            }
            boolean this_present_sec = true && this.isSetSec();
            boolean that_present_sec = true && that.isSetSec();
            if (this_present_sec || that_present_sec) {
                if (!(this_present_sec && that_present_sec))
                    return false;
                if (!this.sec.equals(that.sec))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(authenticateUser_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            authenticateUser_result typedOther = (authenticateUser_result) other;
            lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSuccess()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, typedOther.success);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(typedOther.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, typedOther.sec);
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
            StringBuilder sb = new StringBuilder("authenticateUser_result(");
            boolean first = true;
            sb.append("success:");
            sb.append(this.success);
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("sec:");
            if (this.sec == null) {
                sb.append("null");
            } else {
                sb.append(this.sec);
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

        private static class authenticateUser_resultStandardSchemeFactory implements SchemeFactory {

            public authenticateUser_resultStandardScheme getScheme() {
                return new authenticateUser_resultStandardScheme();
            }
        }

        private static class authenticateUser_resultStandardScheme extends StandardScheme<authenticateUser_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, authenticateUser_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 0:
                            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
                                struct.success = iprot.readBool();
                                struct.setSuccessIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, authenticateUser_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.isSetSuccess()) {
                    oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
                    oprot.writeBool(struct.success);
                    oprot.writeFieldEnd();
                }
                if (struct.sec != null) {
                    oprot.writeFieldBegin(SEC_FIELD_DESC);
                    struct.sec.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class authenticateUser_resultTupleSchemeFactory implements SchemeFactory {

            public authenticateUser_resultTupleScheme getScheme() {
                return new authenticateUser_resultTupleScheme();
            }
        }

        private static class authenticateUser_resultTupleScheme extends TupleScheme<authenticateUser_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, authenticateUser_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSuccess()) {
                    optionals.set(0);
                }
                if (struct.isSetSec()) {
                    optionals.set(1);
                }
                oprot.writeBitSet(optionals, 2);
                if (struct.isSetSuccess()) {
                    oprot.writeBool(struct.success);
                }
                if (struct.isSetSec()) {
                    struct.sec.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, authenticateUser_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    struct.success = iprot.readBool();
                    struct.setSuccessIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
            }
        }
    }

    public static class listUsers_args implements org.apache.thrift.TBase<listUsers_args, listUsers_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("listUsers_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

        private static final org.apache.thrift.protocol.TField CREDENTIAL_FIELD_DESC = new org.apache.thrift.protocol.TField("credential", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new listUsers_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new listUsers_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.Credential credential;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 2, "tinfo"), CREDENTIAL((short) 1, "credential");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 2:
                        return TINFO;
                    case 1:
                        return CREDENTIAL;
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
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.CREDENTIAL, new org.apache.thrift.meta_data.FieldMetaData("credential", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.Credential.class)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(listUsers_args.class, metaDataMap);
        }

        public listUsers_args() {
        }

        public listUsers_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential) {
            this();
            this.tinfo = tinfo;
            this.credential = credential;
        }

        public listUsers_args(listUsers_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredential()) {
                this.credential = new org.apache.accumulo.core.security.thrift.Credential(other.credential);
            }
        }

        public listUsers_args deepCopy() {
            return new listUsers_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credential = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public listUsers_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
            this.tinfo = tinfo;
            return this;
        }

        public void unsetTinfo() {
            this.tinfo = null;
        }

        public boolean isSetTinfo() {
            return this.tinfo != null;
        }

        public void setTinfoIsSet(boolean value) {
            if (!value) {
                this.tinfo = null;
            }
        }

        public org.apache.accumulo.core.security.thrift.Credential getCredential() {
            return this.credential;
        }

        public listUsers_args setCredential(org.apache.accumulo.core.security.thrift.Credential credential) {
            this.credential = credential;
            return this;
        }

        public void unsetCredential() {
            this.credential = null;
        }

        public boolean isSetCredential() {
            return this.credential != null;
        }

        public void setCredentialIsSet(boolean value) {
            if (!value) {
                this.credential = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
                        setTinfo((org.apache.accumulo.trace.thrift.TInfo) value);
                    }
                    break;
                case CREDENTIAL:
                    if (value == null) {
                        unsetCredential();
                    } else {
                        setCredential((org.apache.accumulo.core.security.thrift.Credential) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIAL:
                    return getCredential();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case TINFO:
                    return isSetTinfo();
                case CREDENTIAL:
                    return isSetCredential();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof listUsers_args)
                return this.equals((listUsers_args) that);
            return false;
        }

        public boolean equals(listUsers_args that) {
            if (that == null)
                return false;
            boolean this_present_tinfo = true && this.isSetTinfo();
            boolean that_present_tinfo = true && that.isSetTinfo();
            if (this_present_tinfo || that_present_tinfo) {
                if (!(this_present_tinfo && that_present_tinfo))
                    return false;
                if (!this.tinfo.equals(that.tinfo))
                    return false;
            }
            boolean this_present_credential = true && this.isSetCredential();
            boolean that_present_credential = true && that.isSetCredential();
            if (this_present_credential || that_present_credential) {
                if (!(this_present_credential && that_present_credential))
                    return false;
                if (!this.credential.equals(that.credential))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(listUsers_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            listUsers_args typedOther = (listUsers_args) other;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(typedOther.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, typedOther.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredential()).compareTo(typedOther.isSetCredential());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredential()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credential, typedOther.credential);
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
            StringBuilder sb = new StringBuilder("listUsers_args(");
            boolean first = true;
            sb.append("tinfo:");
            if (this.tinfo == null) {
                sb.append("null");
            } else {
                sb.append(this.tinfo);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("credential:");
            if (this.credential == null) {
                sb.append("null");
            } else {
                sb.append(this.credential);
            }
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credential != null) {
                credential.validate();
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
                read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
            } catch (org.apache.thrift.TException te) {
                throw new java.io.IOException(te);
            }
        }

        private static class listUsers_argsStandardSchemeFactory implements SchemeFactory {

            public listUsers_argsStandardScheme getScheme() {
                return new listUsers_argsStandardScheme();
            }
        }

        private static class listUsers_argsStandardScheme extends StandardScheme<listUsers_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, listUsers_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                                struct.tinfo.read(iprot);
                                struct.setTinfoIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                                struct.credential.read(iprot);
                                struct.setCredentialIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, listUsers_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credential != null) {
                    oprot.writeFieldBegin(CREDENTIAL_FIELD_DESC);
                    struct.credential.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class listUsers_argsTupleSchemeFactory implements SchemeFactory {

            public listUsers_argsTupleScheme getScheme() {
                return new listUsers_argsTupleScheme();
            }
        }

        private static class listUsers_argsTupleScheme extends TupleScheme<listUsers_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, listUsers_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredential()) {
                    optionals.set(1);
                }
                oprot.writeBitSet(optionals, 2);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredential()) {
                    struct.credential.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, listUsers_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                    struct.credential.read(iprot);
                    struct.setCredentialIsSet(true);
                }
            }
        }
    }

    public static class listUsers_result implements org.apache.thrift.TBase<listUsers_result, listUsers_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("listUsers_result");

        private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.SET, (short) 0);

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new listUsers_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new listUsers_resultTupleSchemeFactory());
        }

        public Set<String> success;

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException sec;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SUCCESS((short) 0, "success"), SEC((short) 1, "sec");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 0:
                        return SUCCESS;
                    case 1:
                        return SEC;
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
            tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.SetMetaData(org.apache.thrift.protocol.TType.SET, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
            tmpMap.put(_Fields.SEC, new org.apache.thrift.meta_data.FieldMetaData("sec", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(listUsers_result.class, metaDataMap);
        }

        public listUsers_result() {
        }

        public listUsers_result(Set<String> success, org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this();
            this.success = success;
            this.sec = sec;
        }

        public listUsers_result(listUsers_result other) {
            if (other.isSetSuccess()) {
                Set<String> __this__success = new HashSet<String>();
                for (String other_element : other.success) {
                    __this__success.add(other_element);
                }
                this.success = __this__success;
            }
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException(other.sec);
            }
        }

        public listUsers_result deepCopy() {
            return new listUsers_result(this);
        }

        @Override
        public void clear() {
            this.success = null;
            this.sec = null;
        }

        public int getSuccessSize() {
            return (this.success == null) ? 0 : this.success.size();
        }

        public java.util.Iterator<String> getSuccessIterator() {
            return (this.success == null) ? null : this.success.iterator();
        }

        public void addToSuccess(String elem) {
            if (this.success == null) {
                this.success = new HashSet<String>();
            }
            this.success.add(elem);
        }

        public Set<String> getSuccess() {
            return this.success;
        }

        public listUsers_result setSuccess(Set<String> success) {
            this.success = success;
            return this;
        }

        public void unsetSuccess() {
            this.success = null;
        }

        public boolean isSetSuccess() {
            return this.success != null;
        }

        public void setSuccessIsSet(boolean value) {
            if (!value) {
                this.success = null;
            }
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public listUsers_result setSec(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this.sec = sec;
            return this;
        }

        public void unsetSec() {
            this.sec = null;
        }

        public boolean isSetSec() {
            return this.sec != null;
        }

        public void setSecIsSet(boolean value) {
            if (!value) {
                this.sec = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SUCCESS:
                    if (value == null) {
                        unsetSuccess();
                    } else {
                        setSuccess((Set<String>) value);
                    }
                    break;
                case SEC:
                    if (value == null) {
                        unsetSec();
                    } else {
                        setSec((org.apache.accumulo.core.security.thrift.ThriftSecurityException) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SUCCESS:
                    return getSuccess();
                case SEC:
                    return getSec();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SUCCESS:
                    return isSetSuccess();
                case SEC:
                    return isSetSec();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof listUsers_result)
                return this.equals((listUsers_result) that);
            return false;
        }

        public boolean equals(listUsers_result that) {
            if (that == null)
                return false;
            boolean this_present_success = true && this.isSetSuccess();
            boolean that_present_success = true && that.isSetSuccess();
            if (this_present_success || that_present_success) {
                if (!(this_present_success && that_present_success))
                    return false;
                if (!this.success.equals(that.success))
                    return false;
            }
            boolean this_present_sec = true && this.isSetSec();
            boolean that_present_sec = true && that.isSetSec();
            if (this_present_sec || that_present_sec) {
                if (!(this_present_sec && that_present_sec))
                    return false;
                if (!this.sec.equals(that.sec))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(listUsers_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            listUsers_result typedOther = (listUsers_result) other;
            lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSuccess()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, typedOther.success);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(typedOther.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, typedOther.sec);
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
            StringBuilder sb = new StringBuilder("listUsers_result(");
            boolean first = true;
            sb.append("success:");
            if (this.success == null) {
                sb.append("null");
            } else {
                sb.append(this.success);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("sec:");
            if (this.sec == null) {
                sb.append("null");
            } else {
                sb.append(this.sec);
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

        private static class listUsers_resultStandardSchemeFactory implements SchemeFactory {

            public listUsers_resultStandardScheme getScheme() {
                return new listUsers_resultStandardScheme();
            }
        }

        private static class listUsers_resultStandardScheme extends StandardScheme<listUsers_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, listUsers_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 0:
                            if (schemeField.type == org.apache.thrift.protocol.TType.SET) {
                                {
                                    org.apache.thrift.protocol.TSet _set16 = iprot.readSetBegin();
                                    struct.success = new HashSet<String>(2 * _set16.size);
                                    for (int _i17 = 0; _i17 < _set16.size; ++_i17) {
                                        String _elem18;
                                        _elem18 = iprot.readString();
                                        struct.success.add(_elem18);
                                    }
                                    iprot.readSetEnd();
                                }
                                struct.setSuccessIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, listUsers_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.success != null) {
                    oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
                    {
                        oprot.writeSetBegin(new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.STRING, struct.success.size()));
                        for (String _iter19 : struct.success) {
                            oprot.writeString(_iter19);
                        }
                        oprot.writeSetEnd();
                    }
                    oprot.writeFieldEnd();
                }
                if (struct.sec != null) {
                    oprot.writeFieldBegin(SEC_FIELD_DESC);
                    struct.sec.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class listUsers_resultTupleSchemeFactory implements SchemeFactory {

            public listUsers_resultTupleScheme getScheme() {
                return new listUsers_resultTupleScheme();
            }
        }

        private static class listUsers_resultTupleScheme extends TupleScheme<listUsers_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, listUsers_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSuccess()) {
                    optionals.set(0);
                }
                if (struct.isSetSec()) {
                    optionals.set(1);
                }
                oprot.writeBitSet(optionals, 2);
                if (struct.isSetSuccess()) {
                    {
                        oprot.writeI32(struct.success.size());
                        for (String _iter20 : struct.success) {
                            oprot.writeString(_iter20);
                        }
                    }
                }
                if (struct.isSetSec()) {
                    struct.sec.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, listUsers_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    {
                        org.apache.thrift.protocol.TSet _set21 = new org.apache.thrift.protocol.TSet(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                        struct.success = new HashSet<String>(2 * _set21.size);
                        for (int _i22 = 0; _i22 < _set21.size; ++_i22) {
                            String _elem23;
                            _elem23 = iprot.readString();
                            struct.success.add(_elem23);
                        }
                    }
                    struct.setSuccessIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
            }
        }
    }

    public static class createUser_args implements org.apache.thrift.TBase<createUser_args, createUser_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("createUser_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 5);

        private static final org.apache.thrift.protocol.TField CREDENTIAL_FIELD_DESC = new org.apache.thrift.protocol.TField("credential", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField TO_CREATE_FIELD_DESC = new org.apache.thrift.protocol.TField("toCreate", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

        private static final org.apache.thrift.protocol.TField AUTHORIZATIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("authorizations", org.apache.thrift.protocol.TType.LIST, (short) 4);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new createUser_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new createUser_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.Credential credential;

        public org.apache.accumulo.core.security.thrift.Credential toCreate;

        public List<ByteBuffer> authorizations;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 5, "tinfo"), CREDENTIAL((short) 1, "credential"), TO_CREATE((short) 2, "toCreate"), AUTHORIZATIONS((short) 4, "authorizations");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 5:
                        return TINFO;
                    case 1:
                        return CREDENTIAL;
                    case 2:
                        return TO_CREATE;
                    case 4:
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

        public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.CREDENTIAL, new org.apache.thrift.meta_data.FieldMetaData("credential", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.Credential.class)));
            tmpMap.put(_Fields.TO_CREATE, new org.apache.thrift.meta_data.FieldMetaData("toCreate", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.Credential.class)));
            tmpMap.put(_Fields.AUTHORIZATIONS, new org.apache.thrift.meta_data.FieldMetaData("authorizations", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true))));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(createUser_args.class, metaDataMap);
        }

        public createUser_args() {
        }

        public createUser_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toCreate, List<ByteBuffer> authorizations) {
            this();
            this.tinfo = tinfo;
            this.credential = credential;
            this.toCreate = toCreate;
            this.authorizations = authorizations;
        }

        public createUser_args(createUser_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredential()) {
                this.credential = new org.apache.accumulo.core.security.thrift.Credential(other.credential);
            }
            if (other.isSetToCreate()) {
                this.toCreate = new org.apache.accumulo.core.security.thrift.Credential(other.toCreate);
            }
            if (other.isSetAuthorizations()) {
                List<ByteBuffer> __this__authorizations = new ArrayList<ByteBuffer>();
                for (ByteBuffer other_element : other.authorizations) {
                    ByteBuffer temp_binary_element = org.apache.thrift.TBaseHelper.copyBinary(other_element);
                    ;
                    __this__authorizations.add(temp_binary_element);
                }
                this.authorizations = __this__authorizations;
            }
        }

        public createUser_args deepCopy() {
            return new createUser_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credential = null;
            this.toCreate = null;
            this.authorizations = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public createUser_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
            this.tinfo = tinfo;
            return this;
        }

        public void unsetTinfo() {
            this.tinfo = null;
        }

        public boolean isSetTinfo() {
            return this.tinfo != null;
        }

        public void setTinfoIsSet(boolean value) {
            if (!value) {
                this.tinfo = null;
            }
        }

        public org.apache.accumulo.core.security.thrift.Credential getCredential() {
            return this.credential;
        }

        public createUser_args setCredential(org.apache.accumulo.core.security.thrift.Credential credential) {
            this.credential = credential;
            return this;
        }

        public void unsetCredential() {
            this.credential = null;
        }

        public boolean isSetCredential() {
            return this.credential != null;
        }

        public void setCredentialIsSet(boolean value) {
            if (!value) {
                this.credential = null;
            }
        }

        public org.apache.accumulo.core.security.thrift.Credential getToCreate() {
            return this.toCreate;
        }

        public createUser_args setToCreate(org.apache.accumulo.core.security.thrift.Credential toCreate) {
            this.toCreate = toCreate;
            return this;
        }

        public void unsetToCreate() {
            this.toCreate = null;
        }

        public boolean isSetToCreate() {
            return this.toCreate != null;
        }

        public void setToCreateIsSet(boolean value) {
            if (!value) {
                this.toCreate = null;
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

        public createUser_args setAuthorizations(List<ByteBuffer> authorizations) {
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
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
                        setTinfo((org.apache.accumulo.trace.thrift.TInfo) value);
                    }
                    break;
                case CREDENTIAL:
                    if (value == null) {
                        unsetCredential();
                    } else {
                        setCredential((org.apache.accumulo.core.security.thrift.Credential) value);
                    }
                    break;
                case TO_CREATE:
                    if (value == null) {
                        unsetToCreate();
                    } else {
                        setToCreate((org.apache.accumulo.core.security.thrift.Credential) value);
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
                case TINFO:
                    return getTinfo();
                case CREDENTIAL:
                    return getCredential();
                case TO_CREATE:
                    return getToCreate();
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
                case TINFO:
                    return isSetTinfo();
                case CREDENTIAL:
                    return isSetCredential();
                case TO_CREATE:
                    return isSetToCreate();
                case AUTHORIZATIONS:
                    return isSetAuthorizations();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof createUser_args)
                return this.equals((createUser_args) that);
            return false;
        }

        public boolean equals(createUser_args that) {
            if (that == null)
                return false;
            boolean this_present_tinfo = true && this.isSetTinfo();
            boolean that_present_tinfo = true && that.isSetTinfo();
            if (this_present_tinfo || that_present_tinfo) {
                if (!(this_present_tinfo && that_present_tinfo))
                    return false;
                if (!this.tinfo.equals(that.tinfo))
                    return false;
            }
            boolean this_present_credential = true && this.isSetCredential();
            boolean that_present_credential = true && that.isSetCredential();
            if (this_present_credential || that_present_credential) {
                if (!(this_present_credential && that_present_credential))
                    return false;
                if (!this.credential.equals(that.credential))
                    return false;
            }
            boolean this_present_toCreate = true && this.isSetToCreate();
            boolean that_present_toCreate = true && that.isSetToCreate();
            if (this_present_toCreate || that_present_toCreate) {
                if (!(this_present_toCreate && that_present_toCreate))
                    return false;
                if (!this.toCreate.equals(that.toCreate))
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

        public int compareTo(createUser_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            createUser_args typedOther = (createUser_args) other;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(typedOther.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, typedOther.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredential()).compareTo(typedOther.isSetCredential());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredential()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credential, typedOther.credential);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetToCreate()).compareTo(typedOther.isSetToCreate());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetToCreate()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.toCreate, typedOther.toCreate);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetAuthorizations()).compareTo(typedOther.isSetAuthorizations());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetAuthorizations()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.authorizations, typedOther.authorizations);
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
            StringBuilder sb = new StringBuilder("createUser_args(");
            boolean first = true;
            sb.append("tinfo:");
            if (this.tinfo == null) {
                sb.append("null");
            } else {
                sb.append(this.tinfo);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("credential:");
            if (this.credential == null) {
                sb.append("null");
            } else {
                sb.append(this.credential);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("toCreate:");
            if (this.toCreate == null) {
                sb.append("null");
            } else {
                sb.append(this.toCreate);
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
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credential != null) {
                credential.validate();
            }
            if (toCreate != null) {
                toCreate.validate();
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
                read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
            } catch (org.apache.thrift.TException te) {
                throw new java.io.IOException(te);
            }
        }

        private static class createUser_argsStandardSchemeFactory implements SchemeFactory {

            public createUser_argsStandardScheme getScheme() {
                return new createUser_argsStandardScheme();
            }
        }

        private static class createUser_argsStandardScheme extends StandardScheme<createUser_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, createUser_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 5:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                                struct.tinfo.read(iprot);
                                struct.setTinfoIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                                struct.credential.read(iprot);
                                struct.setCredentialIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.toCreate = new org.apache.accumulo.core.security.thrift.Credential();
                                struct.toCreate.read(iprot);
                                struct.setToCreateIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 4:
                            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                                {
                                    org.apache.thrift.protocol.TList _list24 = iprot.readListBegin();
                                    struct.authorizations = new ArrayList<ByteBuffer>(_list24.size);
                                    for (int _i25 = 0; _i25 < _list24.size; ++_i25) {
                                        ByteBuffer _elem26;
                                        _elem26 = iprot.readBinary();
                                        struct.authorizations.add(_elem26);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, createUser_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credential != null) {
                    oprot.writeFieldBegin(CREDENTIAL_FIELD_DESC);
                    struct.credential.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.toCreate != null) {
                    oprot.writeFieldBegin(TO_CREATE_FIELD_DESC);
                    struct.toCreate.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.authorizations != null) {
                    oprot.writeFieldBegin(AUTHORIZATIONS_FIELD_DESC);
                    {
                        oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.authorizations.size()));
                        for (ByteBuffer _iter27 : struct.authorizations) {
                            oprot.writeBinary(_iter27);
                        }
                        oprot.writeListEnd();
                    }
                    oprot.writeFieldEnd();
                }
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class createUser_argsTupleSchemeFactory implements SchemeFactory {

            public createUser_argsTupleScheme getScheme() {
                return new createUser_argsTupleScheme();
            }
        }

        private static class createUser_argsTupleScheme extends TupleScheme<createUser_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, createUser_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredential()) {
                    optionals.set(1);
                }
                if (struct.isSetToCreate()) {
                    optionals.set(2);
                }
                if (struct.isSetAuthorizations()) {
                    optionals.set(3);
                }
                oprot.writeBitSet(optionals, 4);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredential()) {
                    struct.credential.write(oprot);
                }
                if (struct.isSetToCreate()) {
                    struct.toCreate.write(oprot);
                }
                if (struct.isSetAuthorizations()) {
                    {
                        oprot.writeI32(struct.authorizations.size());
                        for (ByteBuffer _iter28 : struct.authorizations) {
                            oprot.writeBinary(_iter28);
                        }
                    }
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, createUser_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(4);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                    struct.credential.read(iprot);
                    struct.setCredentialIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.toCreate = new org.apache.accumulo.core.security.thrift.Credential();
                    struct.toCreate.read(iprot);
                    struct.setToCreateIsSet(true);
                }
                if (incoming.get(3)) {
                    {
                        org.apache.thrift.protocol.TList _list29 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                        struct.authorizations = new ArrayList<ByteBuffer>(_list29.size);
                        for (int _i30 = 0; _i30 < _list29.size; ++_i30) {
                            ByteBuffer _elem31;
                            _elem31 = iprot.readBinary();
                            struct.authorizations.add(_elem31);
                        }
                    }
                    struct.setAuthorizationsIsSet(true);
                }
            }
        }
    }

    public static class createUser_result implements org.apache.thrift.TBase<createUser_result, createUser_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("createUser_result");

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new createUser_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new createUser_resultTupleSchemeFactory());
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException sec;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SEC((short) 1, "sec");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 1:
                        return SEC;
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
            tmpMap.put(_Fields.SEC, new org.apache.thrift.meta_data.FieldMetaData("sec", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(createUser_result.class, metaDataMap);
        }

        public createUser_result() {
        }

        public createUser_result(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this();
            this.sec = sec;
        }

        public createUser_result(createUser_result other) {
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException(other.sec);
            }
        }

        public createUser_result deepCopy() {
            return new createUser_result(this);
        }

        @Override
        public void clear() {
            this.sec = null;
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public createUser_result setSec(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this.sec = sec;
            return this;
        }

        public void unsetSec() {
            this.sec = null;
        }

        public boolean isSetSec() {
            return this.sec != null;
        }

        public void setSecIsSet(boolean value) {
            if (!value) {
                this.sec = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SEC:
                    if (value == null) {
                        unsetSec();
                    } else {
                        setSec((org.apache.accumulo.core.security.thrift.ThriftSecurityException) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SEC:
                    return getSec();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SEC:
                    return isSetSec();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof createUser_result)
                return this.equals((createUser_result) that);
            return false;
        }

        public boolean equals(createUser_result that) {
            if (that == null)
                return false;
            boolean this_present_sec = true && this.isSetSec();
            boolean that_present_sec = true && that.isSetSec();
            if (this_present_sec || that_present_sec) {
                if (!(this_present_sec && that_present_sec))
                    return false;
                if (!this.sec.equals(that.sec))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(createUser_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            createUser_result typedOther = (createUser_result) other;
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(typedOther.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, typedOther.sec);
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
            StringBuilder sb = new StringBuilder("createUser_result(");
            boolean first = true;
            sb.append("sec:");
            if (this.sec == null) {
                sb.append("null");
            } else {
                sb.append(this.sec);
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

        private static class createUser_resultStandardSchemeFactory implements SchemeFactory {

            public createUser_resultStandardScheme getScheme() {
                return new createUser_resultStandardScheme();
            }
        }

        private static class createUser_resultStandardScheme extends StandardScheme<createUser_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, createUser_result struct) throws org.apache.thrift.TException {
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
                                struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, createUser_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.sec != null) {
                    oprot.writeFieldBegin(SEC_FIELD_DESC);
                    struct.sec.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class createUser_resultTupleSchemeFactory implements SchemeFactory {

            public createUser_resultTupleScheme getScheme() {
                return new createUser_resultTupleScheme();
            }
        }

        private static class createUser_resultTupleScheme extends TupleScheme<createUser_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, createUser_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSec()) {
                    optionals.set(0);
                }
                oprot.writeBitSet(optionals, 1);
                if (struct.isSetSec()) {
                    struct.sec.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, createUser_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
            }
        }
    }

    public static class dropUser_args implements org.apache.thrift.TBase<dropUser_args, dropUser_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("dropUser_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 3);

        private static final org.apache.thrift.protocol.TField CREDENTIAL_FIELD_DESC = new org.apache.thrift.protocol.TField("credential", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField PRINCIPAL_FIELD_DESC = new org.apache.thrift.protocol.TField("principal", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new dropUser_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new dropUser_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.Credential credential;

        public String principal;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 3, "tinfo"), CREDENTIAL((short) 1, "credential"), PRINCIPAL((short) 2, "principal");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 3:
                        return TINFO;
                    case 1:
                        return CREDENTIAL;
                    case 2:
                        return PRINCIPAL;
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
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.CREDENTIAL, new org.apache.thrift.meta_data.FieldMetaData("credential", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.Credential.class)));
            tmpMap.put(_Fields.PRINCIPAL, new org.apache.thrift.meta_data.FieldMetaData("principal", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(dropUser_args.class, metaDataMap);
        }

        public dropUser_args() {
        }

        public dropUser_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal) {
            this();
            this.tinfo = tinfo;
            this.credential = credential;
            this.principal = principal;
        }

        public dropUser_args(dropUser_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredential()) {
                this.credential = new org.apache.accumulo.core.security.thrift.Credential(other.credential);
            }
            if (other.isSetPrincipal()) {
                this.principal = other.principal;
            }
        }

        public dropUser_args deepCopy() {
            return new dropUser_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credential = null;
            this.principal = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public dropUser_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
            this.tinfo = tinfo;
            return this;
        }

        public void unsetTinfo() {
            this.tinfo = null;
        }

        public boolean isSetTinfo() {
            return this.tinfo != null;
        }

        public void setTinfoIsSet(boolean value) {
            if (!value) {
                this.tinfo = null;
            }
        }

        public org.apache.accumulo.core.security.thrift.Credential getCredential() {
            return this.credential;
        }

        public dropUser_args setCredential(org.apache.accumulo.core.security.thrift.Credential credential) {
            this.credential = credential;
            return this;
        }

        public void unsetCredential() {
            this.credential = null;
        }

        public boolean isSetCredential() {
            return this.credential != null;
        }

        public void setCredentialIsSet(boolean value) {
            if (!value) {
                this.credential = null;
            }
        }

        public String getPrincipal() {
            return this.principal;
        }

        public dropUser_args setPrincipal(String principal) {
            this.principal = principal;
            return this;
        }

        public void unsetPrincipal() {
            this.principal = null;
        }

        public boolean isSetPrincipal() {
            return this.principal != null;
        }

        public void setPrincipalIsSet(boolean value) {
            if (!value) {
                this.principal = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
                        setTinfo((org.apache.accumulo.trace.thrift.TInfo) value);
                    }
                    break;
                case CREDENTIAL:
                    if (value == null) {
                        unsetCredential();
                    } else {
                        setCredential((org.apache.accumulo.core.security.thrift.Credential) value);
                    }
                    break;
                case PRINCIPAL:
                    if (value == null) {
                        unsetPrincipal();
                    } else {
                        setPrincipal((String) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIAL:
                    return getCredential();
                case PRINCIPAL:
                    return getPrincipal();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case TINFO:
                    return isSetTinfo();
                case CREDENTIAL:
                    return isSetCredential();
                case PRINCIPAL:
                    return isSetPrincipal();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof dropUser_args)
                return this.equals((dropUser_args) that);
            return false;
        }

        public boolean equals(dropUser_args that) {
            if (that == null)
                return false;
            boolean this_present_tinfo = true && this.isSetTinfo();
            boolean that_present_tinfo = true && that.isSetTinfo();
            if (this_present_tinfo || that_present_tinfo) {
                if (!(this_present_tinfo && that_present_tinfo))
                    return false;
                if (!this.tinfo.equals(that.tinfo))
                    return false;
            }
            boolean this_present_credential = true && this.isSetCredential();
            boolean that_present_credential = true && that.isSetCredential();
            if (this_present_credential || that_present_credential) {
                if (!(this_present_credential && that_present_credential))
                    return false;
                if (!this.credential.equals(that.credential))
                    return false;
            }
            boolean this_present_principal = true && this.isSetPrincipal();
            boolean that_present_principal = true && that.isSetPrincipal();
            if (this_present_principal || that_present_principal) {
                if (!(this_present_principal && that_present_principal))
                    return false;
                if (!this.principal.equals(that.principal))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(dropUser_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            dropUser_args typedOther = (dropUser_args) other;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(typedOther.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, typedOther.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredential()).compareTo(typedOther.isSetCredential());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredential()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credential, typedOther.credential);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetPrincipal()).compareTo(typedOther.isSetPrincipal());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetPrincipal()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.principal, typedOther.principal);
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
            StringBuilder sb = new StringBuilder("dropUser_args(");
            boolean first = true;
            sb.append("tinfo:");
            if (this.tinfo == null) {
                sb.append("null");
            } else {
                sb.append(this.tinfo);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("credential:");
            if (this.credential == null) {
                sb.append("null");
            } else {
                sb.append(this.credential);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("principal:");
            if (this.principal == null) {
                sb.append("null");
            } else {
                sb.append(this.principal);
            }
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credential != null) {
                credential.validate();
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
                read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
            } catch (org.apache.thrift.TException te) {
                throw new java.io.IOException(te);
            }
        }

        private static class dropUser_argsStandardSchemeFactory implements SchemeFactory {

            public dropUser_argsStandardScheme getScheme() {
                return new dropUser_argsStandardScheme();
            }
        }

        private static class dropUser_argsStandardScheme extends StandardScheme<dropUser_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, dropUser_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                                struct.tinfo.read(iprot);
                                struct.setTinfoIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                                struct.credential.read(iprot);
                                struct.setCredentialIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.principal = iprot.readString();
                                struct.setPrincipalIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, dropUser_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credential != null) {
                    oprot.writeFieldBegin(CREDENTIAL_FIELD_DESC);
                    struct.credential.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.principal != null) {
                    oprot.writeFieldBegin(PRINCIPAL_FIELD_DESC);
                    oprot.writeString(struct.principal);
                    oprot.writeFieldEnd();
                }
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class dropUser_argsTupleSchemeFactory implements SchemeFactory {

            public dropUser_argsTupleScheme getScheme() {
                return new dropUser_argsTupleScheme();
            }
        }

        private static class dropUser_argsTupleScheme extends TupleScheme<dropUser_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, dropUser_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredential()) {
                    optionals.set(1);
                }
                if (struct.isSetPrincipal()) {
                    optionals.set(2);
                }
                oprot.writeBitSet(optionals, 3);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredential()) {
                    struct.credential.write(oprot);
                }
                if (struct.isSetPrincipal()) {
                    oprot.writeString(struct.principal);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, dropUser_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(3);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                    struct.credential.read(iprot);
                    struct.setCredentialIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.principal = iprot.readString();
                    struct.setPrincipalIsSet(true);
                }
            }
        }
    }

    public static class dropUser_result implements org.apache.thrift.TBase<dropUser_result, dropUser_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("dropUser_result");

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new dropUser_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new dropUser_resultTupleSchemeFactory());
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException sec;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SEC((short) 1, "sec");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 1:
                        return SEC;
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
            tmpMap.put(_Fields.SEC, new org.apache.thrift.meta_data.FieldMetaData("sec", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(dropUser_result.class, metaDataMap);
        }

        public dropUser_result() {
        }

        public dropUser_result(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this();
            this.sec = sec;
        }

        public dropUser_result(dropUser_result other) {
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException(other.sec);
            }
        }

        public dropUser_result deepCopy() {
            return new dropUser_result(this);
        }

        @Override
        public void clear() {
            this.sec = null;
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public dropUser_result setSec(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this.sec = sec;
            return this;
        }

        public void unsetSec() {
            this.sec = null;
        }

        public boolean isSetSec() {
            return this.sec != null;
        }

        public void setSecIsSet(boolean value) {
            if (!value) {
                this.sec = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SEC:
                    if (value == null) {
                        unsetSec();
                    } else {
                        setSec((org.apache.accumulo.core.security.thrift.ThriftSecurityException) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SEC:
                    return getSec();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SEC:
                    return isSetSec();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof dropUser_result)
                return this.equals((dropUser_result) that);
            return false;
        }

        public boolean equals(dropUser_result that) {
            if (that == null)
                return false;
            boolean this_present_sec = true && this.isSetSec();
            boolean that_present_sec = true && that.isSetSec();
            if (this_present_sec || that_present_sec) {
                if (!(this_present_sec && that_present_sec))
                    return false;
                if (!this.sec.equals(that.sec))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(dropUser_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            dropUser_result typedOther = (dropUser_result) other;
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(typedOther.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, typedOther.sec);
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
            StringBuilder sb = new StringBuilder("dropUser_result(");
            boolean first = true;
            sb.append("sec:");
            if (this.sec == null) {
                sb.append("null");
            } else {
                sb.append(this.sec);
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

        private static class dropUser_resultStandardSchemeFactory implements SchemeFactory {

            public dropUser_resultStandardScheme getScheme() {
                return new dropUser_resultStandardScheme();
            }
        }

        private static class dropUser_resultStandardScheme extends StandardScheme<dropUser_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, dropUser_result struct) throws org.apache.thrift.TException {
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
                                struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, dropUser_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.sec != null) {
                    oprot.writeFieldBegin(SEC_FIELD_DESC);
                    struct.sec.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class dropUser_resultTupleSchemeFactory implements SchemeFactory {

            public dropUser_resultTupleScheme getScheme() {
                return new dropUser_resultTupleScheme();
            }
        }

        private static class dropUser_resultTupleScheme extends TupleScheme<dropUser_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, dropUser_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSec()) {
                    optionals.set(0);
                }
                oprot.writeBitSet(optionals, 1);
                if (struct.isSetSec()) {
                    struct.sec.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, dropUser_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
            }
        }
    }

    public static class changePassword_args implements org.apache.thrift.TBase<changePassword_args, changePassword_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("changePassword_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 4);

        private static final org.apache.thrift.protocol.TField CREDENTIAL_FIELD_DESC = new org.apache.thrift.protocol.TField("credential", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField TO_CHANGE_FIELD_DESC = new org.apache.thrift.protocol.TField("toChange", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new changePassword_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new changePassword_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.Credential credential;

        public org.apache.accumulo.core.security.thrift.Credential toChange;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 4, "tinfo"), CREDENTIAL((short) 1, "credential"), TO_CHANGE((short) 2, "toChange");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 4:
                        return TINFO;
                    case 1:
                        return CREDENTIAL;
                    case 2:
                        return TO_CHANGE;
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
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.CREDENTIAL, new org.apache.thrift.meta_data.FieldMetaData("credential", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.Credential.class)));
            tmpMap.put(_Fields.TO_CHANGE, new org.apache.thrift.meta_data.FieldMetaData("toChange", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.Credential.class)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(changePassword_args.class, metaDataMap);
        }

        public changePassword_args() {
        }

        public changePassword_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, org.apache.accumulo.core.security.thrift.Credential toChange) {
            this();
            this.tinfo = tinfo;
            this.credential = credential;
            this.toChange = toChange;
        }

        public changePassword_args(changePassword_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredential()) {
                this.credential = new org.apache.accumulo.core.security.thrift.Credential(other.credential);
            }
            if (other.isSetToChange()) {
                this.toChange = new org.apache.accumulo.core.security.thrift.Credential(other.toChange);
            }
        }

        public changePassword_args deepCopy() {
            return new changePassword_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credential = null;
            this.toChange = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public changePassword_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
            this.tinfo = tinfo;
            return this;
        }

        public void unsetTinfo() {
            this.tinfo = null;
        }

        public boolean isSetTinfo() {
            return this.tinfo != null;
        }

        public void setTinfoIsSet(boolean value) {
            if (!value) {
                this.tinfo = null;
            }
        }

        public org.apache.accumulo.core.security.thrift.Credential getCredential() {
            return this.credential;
        }

        public changePassword_args setCredential(org.apache.accumulo.core.security.thrift.Credential credential) {
            this.credential = credential;
            return this;
        }

        public void unsetCredential() {
            this.credential = null;
        }

        public boolean isSetCredential() {
            return this.credential != null;
        }

        public void setCredentialIsSet(boolean value) {
            if (!value) {
                this.credential = null;
            }
        }

        public org.apache.accumulo.core.security.thrift.Credential getToChange() {
            return this.toChange;
        }

        public changePassword_args setToChange(org.apache.accumulo.core.security.thrift.Credential toChange) {
            this.toChange = toChange;
            return this;
        }

        public void unsetToChange() {
            this.toChange = null;
        }

        public boolean isSetToChange() {
            return this.toChange != null;
        }

        public void setToChangeIsSet(boolean value) {
            if (!value) {
                this.toChange = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
                        setTinfo((org.apache.accumulo.trace.thrift.TInfo) value);
                    }
                    break;
                case CREDENTIAL:
                    if (value == null) {
                        unsetCredential();
                    } else {
                        setCredential((org.apache.accumulo.core.security.thrift.Credential) value);
                    }
                    break;
                case TO_CHANGE:
                    if (value == null) {
                        unsetToChange();
                    } else {
                        setToChange((org.apache.accumulo.core.security.thrift.Credential) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIAL:
                    return getCredential();
                case TO_CHANGE:
                    return getToChange();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case TINFO:
                    return isSetTinfo();
                case CREDENTIAL:
                    return isSetCredential();
                case TO_CHANGE:
                    return isSetToChange();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof changePassword_args)
                return this.equals((changePassword_args) that);
            return false;
        }

        public boolean equals(changePassword_args that) {
            if (that == null)
                return false;
            boolean this_present_tinfo = true && this.isSetTinfo();
            boolean that_present_tinfo = true && that.isSetTinfo();
            if (this_present_tinfo || that_present_tinfo) {
                if (!(this_present_tinfo && that_present_tinfo))
                    return false;
                if (!this.tinfo.equals(that.tinfo))
                    return false;
            }
            boolean this_present_credential = true && this.isSetCredential();
            boolean that_present_credential = true && that.isSetCredential();
            if (this_present_credential || that_present_credential) {
                if (!(this_present_credential && that_present_credential))
                    return false;
                if (!this.credential.equals(that.credential))
                    return false;
            }
            boolean this_present_toChange = true && this.isSetToChange();
            boolean that_present_toChange = true && that.isSetToChange();
            if (this_present_toChange || that_present_toChange) {
                if (!(this_present_toChange && that_present_toChange))
                    return false;
                if (!this.toChange.equals(that.toChange))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(changePassword_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            changePassword_args typedOther = (changePassword_args) other;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(typedOther.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, typedOther.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredential()).compareTo(typedOther.isSetCredential());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredential()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credential, typedOther.credential);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetToChange()).compareTo(typedOther.isSetToChange());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetToChange()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.toChange, typedOther.toChange);
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
            StringBuilder sb = new StringBuilder("changePassword_args(");
            boolean first = true;
            sb.append("tinfo:");
            if (this.tinfo == null) {
                sb.append("null");
            } else {
                sb.append(this.tinfo);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("credential:");
            if (this.credential == null) {
                sb.append("null");
            } else {
                sb.append(this.credential);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("toChange:");
            if (this.toChange == null) {
                sb.append("null");
            } else {
                sb.append(this.toChange);
            }
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credential != null) {
                credential.validate();
            }
            if (toChange != null) {
                toChange.validate();
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
                read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
            } catch (org.apache.thrift.TException te) {
                throw new java.io.IOException(te);
            }
        }

        private static class changePassword_argsStandardSchemeFactory implements SchemeFactory {

            public changePassword_argsStandardScheme getScheme() {
                return new changePassword_argsStandardScheme();
            }
        }

        private static class changePassword_argsStandardScheme extends StandardScheme<changePassword_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, changePassword_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 4:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                                struct.tinfo.read(iprot);
                                struct.setTinfoIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                                struct.credential.read(iprot);
                                struct.setCredentialIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.toChange = new org.apache.accumulo.core.security.thrift.Credential();
                                struct.toChange.read(iprot);
                                struct.setToChangeIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, changePassword_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credential != null) {
                    oprot.writeFieldBegin(CREDENTIAL_FIELD_DESC);
                    struct.credential.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.toChange != null) {
                    oprot.writeFieldBegin(TO_CHANGE_FIELD_DESC);
                    struct.toChange.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class changePassword_argsTupleSchemeFactory implements SchemeFactory {

            public changePassword_argsTupleScheme getScheme() {
                return new changePassword_argsTupleScheme();
            }
        }

        private static class changePassword_argsTupleScheme extends TupleScheme<changePassword_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, changePassword_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredential()) {
                    optionals.set(1);
                }
                if (struct.isSetToChange()) {
                    optionals.set(2);
                }
                oprot.writeBitSet(optionals, 3);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredential()) {
                    struct.credential.write(oprot);
                }
                if (struct.isSetToChange()) {
                    struct.toChange.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, changePassword_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(3);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                    struct.credential.read(iprot);
                    struct.setCredentialIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.toChange = new org.apache.accumulo.core.security.thrift.Credential();
                    struct.toChange.read(iprot);
                    struct.setToChangeIsSet(true);
                }
            }
        }
    }

    public static class changePassword_result implements org.apache.thrift.TBase<changePassword_result, changePassword_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("changePassword_result");

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new changePassword_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new changePassword_resultTupleSchemeFactory());
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException sec;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SEC((short) 1, "sec");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 1:
                        return SEC;
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
            tmpMap.put(_Fields.SEC, new org.apache.thrift.meta_data.FieldMetaData("sec", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(changePassword_result.class, metaDataMap);
        }

        public changePassword_result() {
        }

        public changePassword_result(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this();
            this.sec = sec;
        }

        public changePassword_result(changePassword_result other) {
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException(other.sec);
            }
        }

        public changePassword_result deepCopy() {
            return new changePassword_result(this);
        }

        @Override
        public void clear() {
            this.sec = null;
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public changePassword_result setSec(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this.sec = sec;
            return this;
        }

        public void unsetSec() {
            this.sec = null;
        }

        public boolean isSetSec() {
            return this.sec != null;
        }

        public void setSecIsSet(boolean value) {
            if (!value) {
                this.sec = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SEC:
                    if (value == null) {
                        unsetSec();
                    } else {
                        setSec((org.apache.accumulo.core.security.thrift.ThriftSecurityException) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SEC:
                    return getSec();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SEC:
                    return isSetSec();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof changePassword_result)
                return this.equals((changePassword_result) that);
            return false;
        }

        public boolean equals(changePassword_result that) {
            if (that == null)
                return false;
            boolean this_present_sec = true && this.isSetSec();
            boolean that_present_sec = true && that.isSetSec();
            if (this_present_sec || that_present_sec) {
                if (!(this_present_sec && that_present_sec))
                    return false;
                if (!this.sec.equals(that.sec))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(changePassword_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            changePassword_result typedOther = (changePassword_result) other;
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(typedOther.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, typedOther.sec);
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
            StringBuilder sb = new StringBuilder("changePassword_result(");
            boolean first = true;
            sb.append("sec:");
            if (this.sec == null) {
                sb.append("null");
            } else {
                sb.append(this.sec);
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

        private static class changePassword_resultStandardSchemeFactory implements SchemeFactory {

            public changePassword_resultStandardScheme getScheme() {
                return new changePassword_resultStandardScheme();
            }
        }

        private static class changePassword_resultStandardScheme extends StandardScheme<changePassword_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, changePassword_result struct) throws org.apache.thrift.TException {
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
                                struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, changePassword_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.sec != null) {
                    oprot.writeFieldBegin(SEC_FIELD_DESC);
                    struct.sec.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class changePassword_resultTupleSchemeFactory implements SchemeFactory {

            public changePassword_resultTupleScheme getScheme() {
                return new changePassword_resultTupleScheme();
            }
        }

        private static class changePassword_resultTupleScheme extends TupleScheme<changePassword_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, changePassword_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSec()) {
                    optionals.set(0);
                }
                oprot.writeBitSet(optionals, 1);
                if (struct.isSetSec()) {
                    struct.sec.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, changePassword_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
            }
        }
    }

    public static class changeAuthorizations_args implements org.apache.thrift.TBase<changeAuthorizations_args, changeAuthorizations_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("changeAuthorizations_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 4);

        private static final org.apache.thrift.protocol.TField CREDENTIAL_FIELD_DESC = new org.apache.thrift.protocol.TField("credential", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField PRINCIPAL_FIELD_DESC = new org.apache.thrift.protocol.TField("principal", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final org.apache.thrift.protocol.TField AUTHORIZATIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("authorizations", org.apache.thrift.protocol.TType.LIST, (short) 3);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new changeAuthorizations_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new changeAuthorizations_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.Credential credential;

        public String principal;

        public List<ByteBuffer> authorizations;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 4, "tinfo"), CREDENTIAL((short) 1, "credential"), PRINCIPAL((short) 2, "principal"), AUTHORIZATIONS((short) 3, "authorizations");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 4:
                        return TINFO;
                    case 1:
                        return CREDENTIAL;
                    case 2:
                        return PRINCIPAL;
                    case 3:
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

        public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.CREDENTIAL, new org.apache.thrift.meta_data.FieldMetaData("credential", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.Credential.class)));
            tmpMap.put(_Fields.PRINCIPAL, new org.apache.thrift.meta_data.FieldMetaData("principal", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.AUTHORIZATIONS, new org.apache.thrift.meta_data.FieldMetaData("authorizations", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true))));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(changeAuthorizations_args.class, metaDataMap);
        }

        public changeAuthorizations_args() {
        }

        public changeAuthorizations_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, List<ByteBuffer> authorizations) {
            this();
            this.tinfo = tinfo;
            this.credential = credential;
            this.principal = principal;
            this.authorizations = authorizations;
        }

        public changeAuthorizations_args(changeAuthorizations_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredential()) {
                this.credential = new org.apache.accumulo.core.security.thrift.Credential(other.credential);
            }
            if (other.isSetPrincipal()) {
                this.principal = other.principal;
            }
            if (other.isSetAuthorizations()) {
                List<ByteBuffer> __this__authorizations = new ArrayList<ByteBuffer>();
                for (ByteBuffer other_element : other.authorizations) {
                    ByteBuffer temp_binary_element = org.apache.thrift.TBaseHelper.copyBinary(other_element);
                    ;
                    __this__authorizations.add(temp_binary_element);
                }
                this.authorizations = __this__authorizations;
            }
        }

        public changeAuthorizations_args deepCopy() {
            return new changeAuthorizations_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credential = null;
            this.principal = null;
            this.authorizations = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public changeAuthorizations_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
            this.tinfo = tinfo;
            return this;
        }

        public void unsetTinfo() {
            this.tinfo = null;
        }

        public boolean isSetTinfo() {
            return this.tinfo != null;
        }

        public void setTinfoIsSet(boolean value) {
            if (!value) {
                this.tinfo = null;
            }
        }

        public org.apache.accumulo.core.security.thrift.Credential getCredential() {
            return this.credential;
        }

        public changeAuthorizations_args setCredential(org.apache.accumulo.core.security.thrift.Credential credential) {
            this.credential = credential;
            return this;
        }

        public void unsetCredential() {
            this.credential = null;
        }

        public boolean isSetCredential() {
            return this.credential != null;
        }

        public void setCredentialIsSet(boolean value) {
            if (!value) {
                this.credential = null;
            }
        }

        public String getPrincipal() {
            return this.principal;
        }

        public changeAuthorizations_args setPrincipal(String principal) {
            this.principal = principal;
            return this;
        }

        public void unsetPrincipal() {
            this.principal = null;
        }

        public boolean isSetPrincipal() {
            return this.principal != null;
        }

        public void setPrincipalIsSet(boolean value) {
            if (!value) {
                this.principal = null;
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

        public changeAuthorizations_args setAuthorizations(List<ByteBuffer> authorizations) {
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
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
                        setTinfo((org.apache.accumulo.trace.thrift.TInfo) value);
                    }
                    break;
                case CREDENTIAL:
                    if (value == null) {
                        unsetCredential();
                    } else {
                        setCredential((org.apache.accumulo.core.security.thrift.Credential) value);
                    }
                    break;
                case PRINCIPAL:
                    if (value == null) {
                        unsetPrincipal();
                    } else {
                        setPrincipal((String) value);
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
                case TINFO:
                    return getTinfo();
                case CREDENTIAL:
                    return getCredential();
                case PRINCIPAL:
                    return getPrincipal();
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
                case TINFO:
                    return isSetTinfo();
                case CREDENTIAL:
                    return isSetCredential();
                case PRINCIPAL:
                    return isSetPrincipal();
                case AUTHORIZATIONS:
                    return isSetAuthorizations();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof changeAuthorizations_args)
                return this.equals((changeAuthorizations_args) that);
            return false;
        }

        public boolean equals(changeAuthorizations_args that) {
            if (that == null)
                return false;
            boolean this_present_tinfo = true && this.isSetTinfo();
            boolean that_present_tinfo = true && that.isSetTinfo();
            if (this_present_tinfo || that_present_tinfo) {
                if (!(this_present_tinfo && that_present_tinfo))
                    return false;
                if (!this.tinfo.equals(that.tinfo))
                    return false;
            }
            boolean this_present_credential = true && this.isSetCredential();
            boolean that_present_credential = true && that.isSetCredential();
            if (this_present_credential || that_present_credential) {
                if (!(this_present_credential && that_present_credential))
                    return false;
                if (!this.credential.equals(that.credential))
                    return false;
            }
            boolean this_present_principal = true && this.isSetPrincipal();
            boolean that_present_principal = true && that.isSetPrincipal();
            if (this_present_principal || that_present_principal) {
                if (!(this_present_principal && that_present_principal))
                    return false;
                if (!this.principal.equals(that.principal))
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

        public int compareTo(changeAuthorizations_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            changeAuthorizations_args typedOther = (changeAuthorizations_args) other;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(typedOther.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, typedOther.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredential()).compareTo(typedOther.isSetCredential());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredential()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credential, typedOther.credential);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetPrincipal()).compareTo(typedOther.isSetPrincipal());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetPrincipal()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.principal, typedOther.principal);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetAuthorizations()).compareTo(typedOther.isSetAuthorizations());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetAuthorizations()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.authorizations, typedOther.authorizations);
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
            StringBuilder sb = new StringBuilder("changeAuthorizations_args(");
            boolean first = true;
            sb.append("tinfo:");
            if (this.tinfo == null) {
                sb.append("null");
            } else {
                sb.append(this.tinfo);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("credential:");
            if (this.credential == null) {
                sb.append("null");
            } else {
                sb.append(this.credential);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("principal:");
            if (this.principal == null) {
                sb.append("null");
            } else {
                sb.append(this.principal);
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
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credential != null) {
                credential.validate();
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
                read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
            } catch (org.apache.thrift.TException te) {
                throw new java.io.IOException(te);
            }
        }

        private static class changeAuthorizations_argsStandardSchemeFactory implements SchemeFactory {

            public changeAuthorizations_argsStandardScheme getScheme() {
                return new changeAuthorizations_argsStandardScheme();
            }
        }

        private static class changeAuthorizations_argsStandardScheme extends StandardScheme<changeAuthorizations_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, changeAuthorizations_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 4:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                                struct.tinfo.read(iprot);
                                struct.setTinfoIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                                struct.credential.read(iprot);
                                struct.setCredentialIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.principal = iprot.readString();
                                struct.setPrincipalIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                                {
                                    org.apache.thrift.protocol.TList _list32 = iprot.readListBegin();
                                    struct.authorizations = new ArrayList<ByteBuffer>(_list32.size);
                                    for (int _i33 = 0; _i33 < _list32.size; ++_i33) {
                                        ByteBuffer _elem34;
                                        _elem34 = iprot.readBinary();
                                        struct.authorizations.add(_elem34);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, changeAuthorizations_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credential != null) {
                    oprot.writeFieldBegin(CREDENTIAL_FIELD_DESC);
                    struct.credential.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.principal != null) {
                    oprot.writeFieldBegin(PRINCIPAL_FIELD_DESC);
                    oprot.writeString(struct.principal);
                    oprot.writeFieldEnd();
                }
                if (struct.authorizations != null) {
                    oprot.writeFieldBegin(AUTHORIZATIONS_FIELD_DESC);
                    {
                        oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.authorizations.size()));
                        for (ByteBuffer _iter35 : struct.authorizations) {
                            oprot.writeBinary(_iter35);
                        }
                        oprot.writeListEnd();
                    }
                    oprot.writeFieldEnd();
                }
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class changeAuthorizations_argsTupleSchemeFactory implements SchemeFactory {

            public changeAuthorizations_argsTupleScheme getScheme() {
                return new changeAuthorizations_argsTupleScheme();
            }
        }

        private static class changeAuthorizations_argsTupleScheme extends TupleScheme<changeAuthorizations_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, changeAuthorizations_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredential()) {
                    optionals.set(1);
                }
                if (struct.isSetPrincipal()) {
                    optionals.set(2);
                }
                if (struct.isSetAuthorizations()) {
                    optionals.set(3);
                }
                oprot.writeBitSet(optionals, 4);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredential()) {
                    struct.credential.write(oprot);
                }
                if (struct.isSetPrincipal()) {
                    oprot.writeString(struct.principal);
                }
                if (struct.isSetAuthorizations()) {
                    {
                        oprot.writeI32(struct.authorizations.size());
                        for (ByteBuffer _iter36 : struct.authorizations) {
                            oprot.writeBinary(_iter36);
                        }
                    }
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, changeAuthorizations_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(4);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                    struct.credential.read(iprot);
                    struct.setCredentialIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.principal = iprot.readString();
                    struct.setPrincipalIsSet(true);
                }
                if (incoming.get(3)) {
                    {
                        org.apache.thrift.protocol.TList _list37 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                        struct.authorizations = new ArrayList<ByteBuffer>(_list37.size);
                        for (int _i38 = 0; _i38 < _list37.size; ++_i38) {
                            ByteBuffer _elem39;
                            _elem39 = iprot.readBinary();
                            struct.authorizations.add(_elem39);
                        }
                    }
                    struct.setAuthorizationsIsSet(true);
                }
            }
        }
    }

    public static class changeAuthorizations_result implements org.apache.thrift.TBase<changeAuthorizations_result, changeAuthorizations_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("changeAuthorizations_result");

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new changeAuthorizations_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new changeAuthorizations_resultTupleSchemeFactory());
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException sec;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SEC((short) 1, "sec");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 1:
                        return SEC;
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
            tmpMap.put(_Fields.SEC, new org.apache.thrift.meta_data.FieldMetaData("sec", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(changeAuthorizations_result.class, metaDataMap);
        }

        public changeAuthorizations_result() {
        }

        public changeAuthorizations_result(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this();
            this.sec = sec;
        }

        public changeAuthorizations_result(changeAuthorizations_result other) {
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException(other.sec);
            }
        }

        public changeAuthorizations_result deepCopy() {
            return new changeAuthorizations_result(this);
        }

        @Override
        public void clear() {
            this.sec = null;
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public changeAuthorizations_result setSec(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this.sec = sec;
            return this;
        }

        public void unsetSec() {
            this.sec = null;
        }

        public boolean isSetSec() {
            return this.sec != null;
        }

        public void setSecIsSet(boolean value) {
            if (!value) {
                this.sec = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SEC:
                    if (value == null) {
                        unsetSec();
                    } else {
                        setSec((org.apache.accumulo.core.security.thrift.ThriftSecurityException) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SEC:
                    return getSec();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SEC:
                    return isSetSec();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof changeAuthorizations_result)
                return this.equals((changeAuthorizations_result) that);
            return false;
        }

        public boolean equals(changeAuthorizations_result that) {
            if (that == null)
                return false;
            boolean this_present_sec = true && this.isSetSec();
            boolean that_present_sec = true && that.isSetSec();
            if (this_present_sec || that_present_sec) {
                if (!(this_present_sec && that_present_sec))
                    return false;
                if (!this.sec.equals(that.sec))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(changeAuthorizations_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            changeAuthorizations_result typedOther = (changeAuthorizations_result) other;
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(typedOther.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, typedOther.sec);
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
            StringBuilder sb = new StringBuilder("changeAuthorizations_result(");
            boolean first = true;
            sb.append("sec:");
            if (this.sec == null) {
                sb.append("null");
            } else {
                sb.append(this.sec);
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

        private static class changeAuthorizations_resultStandardSchemeFactory implements SchemeFactory {

            public changeAuthorizations_resultStandardScheme getScheme() {
                return new changeAuthorizations_resultStandardScheme();
            }
        }

        private static class changeAuthorizations_resultStandardScheme extends StandardScheme<changeAuthorizations_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, changeAuthorizations_result struct) throws org.apache.thrift.TException {
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
                                struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, changeAuthorizations_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.sec != null) {
                    oprot.writeFieldBegin(SEC_FIELD_DESC);
                    struct.sec.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class changeAuthorizations_resultTupleSchemeFactory implements SchemeFactory {

            public changeAuthorizations_resultTupleScheme getScheme() {
                return new changeAuthorizations_resultTupleScheme();
            }
        }

        private static class changeAuthorizations_resultTupleScheme extends TupleScheme<changeAuthorizations_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, changeAuthorizations_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSec()) {
                    optionals.set(0);
                }
                oprot.writeBitSet(optionals, 1);
                if (struct.isSetSec()) {
                    struct.sec.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, changeAuthorizations_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
            }
        }
    }

    public static class getUserAuthorizations_args implements org.apache.thrift.TBase<getUserAuthorizations_args, getUserAuthorizations_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("getUserAuthorizations_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 3);

        private static final org.apache.thrift.protocol.TField CREDENTIAL_FIELD_DESC = new org.apache.thrift.protocol.TField("credential", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField PRINCIPAL_FIELD_DESC = new org.apache.thrift.protocol.TField("principal", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new getUserAuthorizations_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new getUserAuthorizations_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.Credential credential;

        public String principal;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 3, "tinfo"), CREDENTIAL((short) 1, "credential"), PRINCIPAL((short) 2, "principal");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 3:
                        return TINFO;
                    case 1:
                        return CREDENTIAL;
                    case 2:
                        return PRINCIPAL;
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
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.CREDENTIAL, new org.apache.thrift.meta_data.FieldMetaData("credential", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.Credential.class)));
            tmpMap.put(_Fields.PRINCIPAL, new org.apache.thrift.meta_data.FieldMetaData("principal", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(getUserAuthorizations_args.class, metaDataMap);
        }

        public getUserAuthorizations_args() {
        }

        public getUserAuthorizations_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal) {
            this();
            this.tinfo = tinfo;
            this.credential = credential;
            this.principal = principal;
        }

        public getUserAuthorizations_args(getUserAuthorizations_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredential()) {
                this.credential = new org.apache.accumulo.core.security.thrift.Credential(other.credential);
            }
            if (other.isSetPrincipal()) {
                this.principal = other.principal;
            }
        }

        public getUserAuthorizations_args deepCopy() {
            return new getUserAuthorizations_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credential = null;
            this.principal = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public getUserAuthorizations_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
            this.tinfo = tinfo;
            return this;
        }

        public void unsetTinfo() {
            this.tinfo = null;
        }

        public boolean isSetTinfo() {
            return this.tinfo != null;
        }

        public void setTinfoIsSet(boolean value) {
            if (!value) {
                this.tinfo = null;
            }
        }

        public org.apache.accumulo.core.security.thrift.Credential getCredential() {
            return this.credential;
        }

        public getUserAuthorizations_args setCredential(org.apache.accumulo.core.security.thrift.Credential credential) {
            this.credential = credential;
            return this;
        }

        public void unsetCredential() {
            this.credential = null;
        }

        public boolean isSetCredential() {
            return this.credential != null;
        }

        public void setCredentialIsSet(boolean value) {
            if (!value) {
                this.credential = null;
            }
        }

        public String getPrincipal() {
            return this.principal;
        }

        public getUserAuthorizations_args setPrincipal(String principal) {
            this.principal = principal;
            return this;
        }

        public void unsetPrincipal() {
            this.principal = null;
        }

        public boolean isSetPrincipal() {
            return this.principal != null;
        }

        public void setPrincipalIsSet(boolean value) {
            if (!value) {
                this.principal = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
                        setTinfo((org.apache.accumulo.trace.thrift.TInfo) value);
                    }
                    break;
                case CREDENTIAL:
                    if (value == null) {
                        unsetCredential();
                    } else {
                        setCredential((org.apache.accumulo.core.security.thrift.Credential) value);
                    }
                    break;
                case PRINCIPAL:
                    if (value == null) {
                        unsetPrincipal();
                    } else {
                        setPrincipal((String) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIAL:
                    return getCredential();
                case PRINCIPAL:
                    return getPrincipal();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case TINFO:
                    return isSetTinfo();
                case CREDENTIAL:
                    return isSetCredential();
                case PRINCIPAL:
                    return isSetPrincipal();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof getUserAuthorizations_args)
                return this.equals((getUserAuthorizations_args) that);
            return false;
        }

        public boolean equals(getUserAuthorizations_args that) {
            if (that == null)
                return false;
            boolean this_present_tinfo = true && this.isSetTinfo();
            boolean that_present_tinfo = true && that.isSetTinfo();
            if (this_present_tinfo || that_present_tinfo) {
                if (!(this_present_tinfo && that_present_tinfo))
                    return false;
                if (!this.tinfo.equals(that.tinfo))
                    return false;
            }
            boolean this_present_credential = true && this.isSetCredential();
            boolean that_present_credential = true && that.isSetCredential();
            if (this_present_credential || that_present_credential) {
                if (!(this_present_credential && that_present_credential))
                    return false;
                if (!this.credential.equals(that.credential))
                    return false;
            }
            boolean this_present_principal = true && this.isSetPrincipal();
            boolean that_present_principal = true && that.isSetPrincipal();
            if (this_present_principal || that_present_principal) {
                if (!(this_present_principal && that_present_principal))
                    return false;
                if (!this.principal.equals(that.principal))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(getUserAuthorizations_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            getUserAuthorizations_args typedOther = (getUserAuthorizations_args) other;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(typedOther.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, typedOther.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredential()).compareTo(typedOther.isSetCredential());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredential()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credential, typedOther.credential);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetPrincipal()).compareTo(typedOther.isSetPrincipal());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetPrincipal()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.principal, typedOther.principal);
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
            StringBuilder sb = new StringBuilder("getUserAuthorizations_args(");
            boolean first = true;
            sb.append("tinfo:");
            if (this.tinfo == null) {
                sb.append("null");
            } else {
                sb.append(this.tinfo);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("credential:");
            if (this.credential == null) {
                sb.append("null");
            } else {
                sb.append(this.credential);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("principal:");
            if (this.principal == null) {
                sb.append("null");
            } else {
                sb.append(this.principal);
            }
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credential != null) {
                credential.validate();
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
                read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
            } catch (org.apache.thrift.TException te) {
                throw new java.io.IOException(te);
            }
        }

        private static class getUserAuthorizations_argsStandardSchemeFactory implements SchemeFactory {

            public getUserAuthorizations_argsStandardScheme getScheme() {
                return new getUserAuthorizations_argsStandardScheme();
            }
        }

        private static class getUserAuthorizations_argsStandardScheme extends StandardScheme<getUserAuthorizations_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, getUserAuthorizations_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                                struct.tinfo.read(iprot);
                                struct.setTinfoIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                                struct.credential.read(iprot);
                                struct.setCredentialIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.principal = iprot.readString();
                                struct.setPrincipalIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, getUserAuthorizations_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credential != null) {
                    oprot.writeFieldBegin(CREDENTIAL_FIELD_DESC);
                    struct.credential.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.principal != null) {
                    oprot.writeFieldBegin(PRINCIPAL_FIELD_DESC);
                    oprot.writeString(struct.principal);
                    oprot.writeFieldEnd();
                }
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class getUserAuthorizations_argsTupleSchemeFactory implements SchemeFactory {

            public getUserAuthorizations_argsTupleScheme getScheme() {
                return new getUserAuthorizations_argsTupleScheme();
            }
        }

        private static class getUserAuthorizations_argsTupleScheme extends TupleScheme<getUserAuthorizations_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, getUserAuthorizations_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredential()) {
                    optionals.set(1);
                }
                if (struct.isSetPrincipal()) {
                    optionals.set(2);
                }
                oprot.writeBitSet(optionals, 3);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredential()) {
                    struct.credential.write(oprot);
                }
                if (struct.isSetPrincipal()) {
                    oprot.writeString(struct.principal);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, getUserAuthorizations_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(3);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                    struct.credential.read(iprot);
                    struct.setCredentialIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.principal = iprot.readString();
                    struct.setPrincipalIsSet(true);
                }
            }
        }
    }

    public static class getUserAuthorizations_result implements org.apache.thrift.TBase<getUserAuthorizations_result, getUserAuthorizations_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("getUserAuthorizations_result");

        private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.LIST, (short) 0);

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new getUserAuthorizations_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new getUserAuthorizations_resultTupleSchemeFactory());
        }

        public List<ByteBuffer> success;

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException sec;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SUCCESS((short) 0, "success"), SEC((short) 1, "sec");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 0:
                        return SUCCESS;
                    case 1:
                        return SEC;
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
            tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true))));
            tmpMap.put(_Fields.SEC, new org.apache.thrift.meta_data.FieldMetaData("sec", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(getUserAuthorizations_result.class, metaDataMap);
        }

        public getUserAuthorizations_result() {
        }

        public getUserAuthorizations_result(List<ByteBuffer> success, org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this();
            this.success = success;
            this.sec = sec;
        }

        public getUserAuthorizations_result(getUserAuthorizations_result other) {
            if (other.isSetSuccess()) {
                List<ByteBuffer> __this__success = new ArrayList<ByteBuffer>();
                for (ByteBuffer other_element : other.success) {
                    ByteBuffer temp_binary_element = org.apache.thrift.TBaseHelper.copyBinary(other_element);
                    ;
                    __this__success.add(temp_binary_element);
                }
                this.success = __this__success;
            }
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException(other.sec);
            }
        }

        public getUserAuthorizations_result deepCopy() {
            return new getUserAuthorizations_result(this);
        }

        @Override
        public void clear() {
            this.success = null;
            this.sec = null;
        }

        public int getSuccessSize() {
            return (this.success == null) ? 0 : this.success.size();
        }

        public java.util.Iterator<ByteBuffer> getSuccessIterator() {
            return (this.success == null) ? null : this.success.iterator();
        }

        public void addToSuccess(ByteBuffer elem) {
            if (this.success == null) {
                this.success = new ArrayList<ByteBuffer>();
            }
            this.success.add(elem);
        }

        public List<ByteBuffer> getSuccess() {
            return this.success;
        }

        public getUserAuthorizations_result setSuccess(List<ByteBuffer> success) {
            this.success = success;
            return this;
        }

        public void unsetSuccess() {
            this.success = null;
        }

        public boolean isSetSuccess() {
            return this.success != null;
        }

        public void setSuccessIsSet(boolean value) {
            if (!value) {
                this.success = null;
            }
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public getUserAuthorizations_result setSec(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this.sec = sec;
            return this;
        }

        public void unsetSec() {
            this.sec = null;
        }

        public boolean isSetSec() {
            return this.sec != null;
        }

        public void setSecIsSet(boolean value) {
            if (!value) {
                this.sec = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SUCCESS:
                    if (value == null) {
                        unsetSuccess();
                    } else {
                        setSuccess((List<ByteBuffer>) value);
                    }
                    break;
                case SEC:
                    if (value == null) {
                        unsetSec();
                    } else {
                        setSec((org.apache.accumulo.core.security.thrift.ThriftSecurityException) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SUCCESS:
                    return getSuccess();
                case SEC:
                    return getSec();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SUCCESS:
                    return isSetSuccess();
                case SEC:
                    return isSetSec();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof getUserAuthorizations_result)
                return this.equals((getUserAuthorizations_result) that);
            return false;
        }

        public boolean equals(getUserAuthorizations_result that) {
            if (that == null)
                return false;
            boolean this_present_success = true && this.isSetSuccess();
            boolean that_present_success = true && that.isSetSuccess();
            if (this_present_success || that_present_success) {
                if (!(this_present_success && that_present_success))
                    return false;
                if (!this.success.equals(that.success))
                    return false;
            }
            boolean this_present_sec = true && this.isSetSec();
            boolean that_present_sec = true && that.isSetSec();
            if (this_present_sec || that_present_sec) {
                if (!(this_present_sec && that_present_sec))
                    return false;
                if (!this.sec.equals(that.sec))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(getUserAuthorizations_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            getUserAuthorizations_result typedOther = (getUserAuthorizations_result) other;
            lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSuccess()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, typedOther.success);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(typedOther.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, typedOther.sec);
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
            StringBuilder sb = new StringBuilder("getUserAuthorizations_result(");
            boolean first = true;
            sb.append("success:");
            if (this.success == null) {
                sb.append("null");
            } else {
                sb.append(this.success);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("sec:");
            if (this.sec == null) {
                sb.append("null");
            } else {
                sb.append(this.sec);
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

        private static class getUserAuthorizations_resultStandardSchemeFactory implements SchemeFactory {

            public getUserAuthorizations_resultStandardScheme getScheme() {
                return new getUserAuthorizations_resultStandardScheme();
            }
        }

        private static class getUserAuthorizations_resultStandardScheme extends StandardScheme<getUserAuthorizations_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, getUserAuthorizations_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 0:
                            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                                {
                                    org.apache.thrift.protocol.TList _list40 = iprot.readListBegin();
                                    struct.success = new ArrayList<ByteBuffer>(_list40.size);
                                    for (int _i41 = 0; _i41 < _list40.size; ++_i41) {
                                        ByteBuffer _elem42;
                                        _elem42 = iprot.readBinary();
                                        struct.success.add(_elem42);
                                    }
                                    iprot.readListEnd();
                                }
                                struct.setSuccessIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, getUserAuthorizations_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.success != null) {
                    oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
                    {
                        oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.success.size()));
                        for (ByteBuffer _iter43 : struct.success) {
                            oprot.writeBinary(_iter43);
                        }
                        oprot.writeListEnd();
                    }
                    oprot.writeFieldEnd();
                }
                if (struct.sec != null) {
                    oprot.writeFieldBegin(SEC_FIELD_DESC);
                    struct.sec.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class getUserAuthorizations_resultTupleSchemeFactory implements SchemeFactory {

            public getUserAuthorizations_resultTupleScheme getScheme() {
                return new getUserAuthorizations_resultTupleScheme();
            }
        }

        private static class getUserAuthorizations_resultTupleScheme extends TupleScheme<getUserAuthorizations_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, getUserAuthorizations_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSuccess()) {
                    optionals.set(0);
                }
                if (struct.isSetSec()) {
                    optionals.set(1);
                }
                oprot.writeBitSet(optionals, 2);
                if (struct.isSetSuccess()) {
                    {
                        oprot.writeI32(struct.success.size());
                        for (ByteBuffer _iter44 : struct.success) {
                            oprot.writeBinary(_iter44);
                        }
                    }
                }
                if (struct.isSetSec()) {
                    struct.sec.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, getUserAuthorizations_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    {
                        org.apache.thrift.protocol.TList _list45 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                        struct.success = new ArrayList<ByteBuffer>(_list45.size);
                        for (int _i46 = 0; _i46 < _list45.size; ++_i46) {
                            ByteBuffer _elem47;
                            _elem47 = iprot.readBinary();
                            struct.success.add(_elem47);
                        }
                    }
                    struct.setSuccessIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
            }
        }
    }

    public static class hasSystemPermission_args implements org.apache.thrift.TBase<hasSystemPermission_args, hasSystemPermission_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("hasSystemPermission_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 4);

        private static final org.apache.thrift.protocol.TField CREDENTIAL_FIELD_DESC = new org.apache.thrift.protocol.TField("credential", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField PRINCIPAL_FIELD_DESC = new org.apache.thrift.protocol.TField("principal", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final org.apache.thrift.protocol.TField SYS_PERM_FIELD_DESC = new org.apache.thrift.protocol.TField("sysPerm", org.apache.thrift.protocol.TType.BYTE, (short) 3);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new hasSystemPermission_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new hasSystemPermission_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.Credential credential;

        public String principal;

        public byte sysPerm;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 4, "tinfo"), CREDENTIAL((short) 1, "credential"), PRINCIPAL((short) 2, "principal"), SYS_PERM((short) 3, "sysPerm");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 4:
                        return TINFO;
                    case 1:
                        return CREDENTIAL;
                    case 2:
                        return PRINCIPAL;
                    case 3:
                        return SYS_PERM;
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

        private static final int __SYSPERM_ISSET_ID = 0;

        private byte __isset_bitfield = 0;

        public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.CREDENTIAL, new org.apache.thrift.meta_data.FieldMetaData("credential", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.Credential.class)));
            tmpMap.put(_Fields.PRINCIPAL, new org.apache.thrift.meta_data.FieldMetaData("principal", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.SYS_PERM, new org.apache.thrift.meta_data.FieldMetaData("sysPerm", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BYTE)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(hasSystemPermission_args.class, metaDataMap);
        }

        public hasSystemPermission_args() {
        }

        public hasSystemPermission_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte sysPerm) {
            this();
            this.tinfo = tinfo;
            this.credential = credential;
            this.principal = principal;
            this.sysPerm = sysPerm;
            setSysPermIsSet(true);
        }

        public hasSystemPermission_args(hasSystemPermission_args other) {
            __isset_bitfield = other.__isset_bitfield;
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredential()) {
                this.credential = new org.apache.accumulo.core.security.thrift.Credential(other.credential);
            }
            if (other.isSetPrincipal()) {
                this.principal = other.principal;
            }
            this.sysPerm = other.sysPerm;
        }

        public hasSystemPermission_args deepCopy() {
            return new hasSystemPermission_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credential = null;
            this.principal = null;
            setSysPermIsSet(false);
            this.sysPerm = 0;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public hasSystemPermission_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
            this.tinfo = tinfo;
            return this;
        }

        public void unsetTinfo() {
            this.tinfo = null;
        }

        public boolean isSetTinfo() {
            return this.tinfo != null;
        }

        public void setTinfoIsSet(boolean value) {
            if (!value) {
                this.tinfo = null;
            }
        }

        public org.apache.accumulo.core.security.thrift.Credential getCredential() {
            return this.credential;
        }

        public hasSystemPermission_args setCredential(org.apache.accumulo.core.security.thrift.Credential credential) {
            this.credential = credential;
            return this;
        }

        public void unsetCredential() {
            this.credential = null;
        }

        public boolean isSetCredential() {
            return this.credential != null;
        }

        public void setCredentialIsSet(boolean value) {
            if (!value) {
                this.credential = null;
            }
        }

        public String getPrincipal() {
            return this.principal;
        }

        public hasSystemPermission_args setPrincipal(String principal) {
            this.principal = principal;
            return this;
        }

        public void unsetPrincipal() {
            this.principal = null;
        }

        public boolean isSetPrincipal() {
            return this.principal != null;
        }

        public void setPrincipalIsSet(boolean value) {
            if (!value) {
                this.principal = null;
            }
        }

        public byte getSysPerm() {
            return this.sysPerm;
        }

        public hasSystemPermission_args setSysPerm(byte sysPerm) {
            this.sysPerm = sysPerm;
            setSysPermIsSet(true);
            return this;
        }

        public void unsetSysPerm() {
            __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __SYSPERM_ISSET_ID);
        }

        public boolean isSetSysPerm() {
            return EncodingUtils.testBit(__isset_bitfield, __SYSPERM_ISSET_ID);
        }

        public void setSysPermIsSet(boolean value) {
            __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __SYSPERM_ISSET_ID, value);
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
                        setTinfo((org.apache.accumulo.trace.thrift.TInfo) value);
                    }
                    break;
                case CREDENTIAL:
                    if (value == null) {
                        unsetCredential();
                    } else {
                        setCredential((org.apache.accumulo.core.security.thrift.Credential) value);
                    }
                    break;
                case PRINCIPAL:
                    if (value == null) {
                        unsetPrincipal();
                    } else {
                        setPrincipal((String) value);
                    }
                    break;
                case SYS_PERM:
                    if (value == null) {
                        unsetSysPerm();
                    } else {
                        setSysPerm((Byte) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIAL:
                    return getCredential();
                case PRINCIPAL:
                    return getPrincipal();
                case SYS_PERM:
                    return Byte.valueOf(getSysPerm());
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case TINFO:
                    return isSetTinfo();
                case CREDENTIAL:
                    return isSetCredential();
                case PRINCIPAL:
                    return isSetPrincipal();
                case SYS_PERM:
                    return isSetSysPerm();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof hasSystemPermission_args)
                return this.equals((hasSystemPermission_args) that);
            return false;
        }

        public boolean equals(hasSystemPermission_args that) {
            if (that == null)
                return false;
            boolean this_present_tinfo = true && this.isSetTinfo();
            boolean that_present_tinfo = true && that.isSetTinfo();
            if (this_present_tinfo || that_present_tinfo) {
                if (!(this_present_tinfo && that_present_tinfo))
                    return false;
                if (!this.tinfo.equals(that.tinfo))
                    return false;
            }
            boolean this_present_credential = true && this.isSetCredential();
            boolean that_present_credential = true && that.isSetCredential();
            if (this_present_credential || that_present_credential) {
                if (!(this_present_credential && that_present_credential))
                    return false;
                if (!this.credential.equals(that.credential))
                    return false;
            }
            boolean this_present_principal = true && this.isSetPrincipal();
            boolean that_present_principal = true && that.isSetPrincipal();
            if (this_present_principal || that_present_principal) {
                if (!(this_present_principal && that_present_principal))
                    return false;
                if (!this.principal.equals(that.principal))
                    return false;
            }
            boolean this_present_sysPerm = true;
            boolean that_present_sysPerm = true;
            if (this_present_sysPerm || that_present_sysPerm) {
                if (!(this_present_sysPerm && that_present_sysPerm))
                    return false;
                if (this.sysPerm != that.sysPerm)
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(hasSystemPermission_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            hasSystemPermission_args typedOther = (hasSystemPermission_args) other;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(typedOther.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, typedOther.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredential()).compareTo(typedOther.isSetCredential());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredential()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credential, typedOther.credential);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetPrincipal()).compareTo(typedOther.isSetPrincipal());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetPrincipal()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.principal, typedOther.principal);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetSysPerm()).compareTo(typedOther.isSetSysPerm());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSysPerm()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sysPerm, typedOther.sysPerm);
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
            StringBuilder sb = new StringBuilder("hasSystemPermission_args(");
            boolean first = true;
            sb.append("tinfo:");
            if (this.tinfo == null) {
                sb.append("null");
            } else {
                sb.append(this.tinfo);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("credential:");
            if (this.credential == null) {
                sb.append("null");
            } else {
                sb.append(this.credential);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("principal:");
            if (this.principal == null) {
                sb.append("null");
            } else {
                sb.append(this.principal);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("sysPerm:");
            sb.append(this.sysPerm);
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credential != null) {
                credential.validate();
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

        private static class hasSystemPermission_argsStandardSchemeFactory implements SchemeFactory {

            public hasSystemPermission_argsStandardScheme getScheme() {
                return new hasSystemPermission_argsStandardScheme();
            }
        }

        private static class hasSystemPermission_argsStandardScheme extends StandardScheme<hasSystemPermission_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, hasSystemPermission_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 4:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                                struct.tinfo.read(iprot);
                                struct.setTinfoIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                                struct.credential.read(iprot);
                                struct.setCredentialIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.principal = iprot.readString();
                                struct.setPrincipalIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.BYTE) {
                                struct.sysPerm = iprot.readByte();
                                struct.setSysPermIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, hasSystemPermission_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credential != null) {
                    oprot.writeFieldBegin(CREDENTIAL_FIELD_DESC);
                    struct.credential.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.principal != null) {
                    oprot.writeFieldBegin(PRINCIPAL_FIELD_DESC);
                    oprot.writeString(struct.principal);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldBegin(SYS_PERM_FIELD_DESC);
                oprot.writeByte(struct.sysPerm);
                oprot.writeFieldEnd();
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class hasSystemPermission_argsTupleSchemeFactory implements SchemeFactory {

            public hasSystemPermission_argsTupleScheme getScheme() {
                return new hasSystemPermission_argsTupleScheme();
            }
        }

        private static class hasSystemPermission_argsTupleScheme extends TupleScheme<hasSystemPermission_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, hasSystemPermission_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredential()) {
                    optionals.set(1);
                }
                if (struct.isSetPrincipal()) {
                    optionals.set(2);
                }
                if (struct.isSetSysPerm()) {
                    optionals.set(3);
                }
                oprot.writeBitSet(optionals, 4);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredential()) {
                    struct.credential.write(oprot);
                }
                if (struct.isSetPrincipal()) {
                    oprot.writeString(struct.principal);
                }
                if (struct.isSetSysPerm()) {
                    oprot.writeByte(struct.sysPerm);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, hasSystemPermission_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(4);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                    struct.credential.read(iprot);
                    struct.setCredentialIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.principal = iprot.readString();
                    struct.setPrincipalIsSet(true);
                }
                if (incoming.get(3)) {
                    struct.sysPerm = iprot.readByte();
                    struct.setSysPermIsSet(true);
                }
            }
        }
    }

    public static class hasSystemPermission_result implements org.apache.thrift.TBase<hasSystemPermission_result, hasSystemPermission_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("hasSystemPermission_result");

        private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.BOOL, (short) 0);

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new hasSystemPermission_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new hasSystemPermission_resultTupleSchemeFactory());
        }

        public boolean success;

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException sec;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SUCCESS((short) 0, "success"), SEC((short) 1, "sec");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 0:
                        return SUCCESS;
                    case 1:
                        return SEC;
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

        private static final int __SUCCESS_ISSET_ID = 0;

        private byte __isset_bitfield = 0;

        public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
            tmpMap.put(_Fields.SEC, new org.apache.thrift.meta_data.FieldMetaData("sec", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(hasSystemPermission_result.class, metaDataMap);
        }

        public hasSystemPermission_result() {
        }

        public hasSystemPermission_result(boolean success, org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this();
            this.success = success;
            setSuccessIsSet(true);
            this.sec = sec;
        }

        public hasSystemPermission_result(hasSystemPermission_result other) {
            __isset_bitfield = other.__isset_bitfield;
            this.success = other.success;
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException(other.sec);
            }
        }

        public hasSystemPermission_result deepCopy() {
            return new hasSystemPermission_result(this);
        }

        @Override
        public void clear() {
            setSuccessIsSet(false);
            this.success = false;
            this.sec = null;
        }

        public boolean isSuccess() {
            return this.success;
        }

        public hasSystemPermission_result setSuccess(boolean success) {
            this.success = success;
            setSuccessIsSet(true);
            return this;
        }

        public void unsetSuccess() {
            __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __SUCCESS_ISSET_ID);
        }

        public boolean isSetSuccess() {
            return EncodingUtils.testBit(__isset_bitfield, __SUCCESS_ISSET_ID);
        }

        public void setSuccessIsSet(boolean value) {
            __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __SUCCESS_ISSET_ID, value);
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public hasSystemPermission_result setSec(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this.sec = sec;
            return this;
        }

        public void unsetSec() {
            this.sec = null;
        }

        public boolean isSetSec() {
            return this.sec != null;
        }

        public void setSecIsSet(boolean value) {
            if (!value) {
                this.sec = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SUCCESS:
                    if (value == null) {
                        unsetSuccess();
                    } else {
                        setSuccess((Boolean) value);
                    }
                    break;
                case SEC:
                    if (value == null) {
                        unsetSec();
                    } else {
                        setSec((org.apache.accumulo.core.security.thrift.ThriftSecurityException) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SUCCESS:
                    return Boolean.valueOf(isSuccess());
                case SEC:
                    return getSec();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SUCCESS:
                    return isSetSuccess();
                case SEC:
                    return isSetSec();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof hasSystemPermission_result)
                return this.equals((hasSystemPermission_result) that);
            return false;
        }

        public boolean equals(hasSystemPermission_result that) {
            if (that == null)
                return false;
            boolean this_present_success = true;
            boolean that_present_success = true;
            if (this_present_success || that_present_success) {
                if (!(this_present_success && that_present_success))
                    return false;
                if (this.success != that.success)
                    return false;
            }
            boolean this_present_sec = true && this.isSetSec();
            boolean that_present_sec = true && that.isSetSec();
            if (this_present_sec || that_present_sec) {
                if (!(this_present_sec && that_present_sec))
                    return false;
                if (!this.sec.equals(that.sec))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(hasSystemPermission_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            hasSystemPermission_result typedOther = (hasSystemPermission_result) other;
            lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSuccess()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, typedOther.success);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(typedOther.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, typedOther.sec);
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
            StringBuilder sb = new StringBuilder("hasSystemPermission_result(");
            boolean first = true;
            sb.append("success:");
            sb.append(this.success);
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("sec:");
            if (this.sec == null) {
                sb.append("null");
            } else {
                sb.append(this.sec);
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

        private static class hasSystemPermission_resultStandardSchemeFactory implements SchemeFactory {

            public hasSystemPermission_resultStandardScheme getScheme() {
                return new hasSystemPermission_resultStandardScheme();
            }
        }

        private static class hasSystemPermission_resultStandardScheme extends StandardScheme<hasSystemPermission_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, hasSystemPermission_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 0:
                            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
                                struct.success = iprot.readBool();
                                struct.setSuccessIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, hasSystemPermission_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.isSetSuccess()) {
                    oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
                    oprot.writeBool(struct.success);
                    oprot.writeFieldEnd();
                }
                if (struct.sec != null) {
                    oprot.writeFieldBegin(SEC_FIELD_DESC);
                    struct.sec.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class hasSystemPermission_resultTupleSchemeFactory implements SchemeFactory {

            public hasSystemPermission_resultTupleScheme getScheme() {
                return new hasSystemPermission_resultTupleScheme();
            }
        }

        private static class hasSystemPermission_resultTupleScheme extends TupleScheme<hasSystemPermission_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, hasSystemPermission_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSuccess()) {
                    optionals.set(0);
                }
                if (struct.isSetSec()) {
                    optionals.set(1);
                }
                oprot.writeBitSet(optionals, 2);
                if (struct.isSetSuccess()) {
                    oprot.writeBool(struct.success);
                }
                if (struct.isSetSec()) {
                    struct.sec.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, hasSystemPermission_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    struct.success = iprot.readBool();
                    struct.setSuccessIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
            }
        }
    }

    public static class hasTablePermission_args implements org.apache.thrift.TBase<hasTablePermission_args, hasTablePermission_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("hasTablePermission_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 5);

        private static final org.apache.thrift.protocol.TField CREDENTIAL_FIELD_DESC = new org.apache.thrift.protocol.TField("credential", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField PRINCIPAL_FIELD_DESC = new org.apache.thrift.protocol.TField("principal", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final org.apache.thrift.protocol.TField TABLE_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("tableName", org.apache.thrift.protocol.TType.STRING, (short) 3);

        private static final org.apache.thrift.protocol.TField TBL_PERM_FIELD_DESC = new org.apache.thrift.protocol.TField("tblPerm", org.apache.thrift.protocol.TType.BYTE, (short) 4);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new hasTablePermission_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new hasTablePermission_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.Credential credential;

        public String principal;

        public String tableName;

        public byte tblPerm;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 5, "tinfo"), CREDENTIAL((short) 1, "credential"), PRINCIPAL((short) 2, "principal"), TABLE_NAME((short) 3, "tableName"), TBL_PERM((short) 4, "tblPerm");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 5:
                        return TINFO;
                    case 1:
                        return CREDENTIAL;
                    case 2:
                        return PRINCIPAL;
                    case 3:
                        return TABLE_NAME;
                    case 4:
                        return TBL_PERM;
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

        private static final int __TBLPERM_ISSET_ID = 0;

        private byte __isset_bitfield = 0;

        public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.CREDENTIAL, new org.apache.thrift.meta_data.FieldMetaData("credential", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.Credential.class)));
            tmpMap.put(_Fields.PRINCIPAL, new org.apache.thrift.meta_data.FieldMetaData("principal", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.TABLE_NAME, new org.apache.thrift.meta_data.FieldMetaData("tableName", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.TBL_PERM, new org.apache.thrift.meta_data.FieldMetaData("tblPerm", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BYTE)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(hasTablePermission_args.class, metaDataMap);
        }

        public hasTablePermission_args() {
        }

        public hasTablePermission_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte tblPerm) {
            this();
            this.tinfo = tinfo;
            this.credential = credential;
            this.principal = principal;
            this.tableName = tableName;
            this.tblPerm = tblPerm;
            setTblPermIsSet(true);
        }

        public hasTablePermission_args(hasTablePermission_args other) {
            __isset_bitfield = other.__isset_bitfield;
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredential()) {
                this.credential = new org.apache.accumulo.core.security.thrift.Credential(other.credential);
            }
            if (other.isSetPrincipal()) {
                this.principal = other.principal;
            }
            if (other.isSetTableName()) {
                this.tableName = other.tableName;
            }
            this.tblPerm = other.tblPerm;
        }

        public hasTablePermission_args deepCopy() {
            return new hasTablePermission_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credential = null;
            this.principal = null;
            this.tableName = null;
            setTblPermIsSet(false);
            this.tblPerm = 0;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public hasTablePermission_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
            this.tinfo = tinfo;
            return this;
        }

        public void unsetTinfo() {
            this.tinfo = null;
        }

        public boolean isSetTinfo() {
            return this.tinfo != null;
        }

        public void setTinfoIsSet(boolean value) {
            if (!value) {
                this.tinfo = null;
            }
        }

        public org.apache.accumulo.core.security.thrift.Credential getCredential() {
            return this.credential;
        }

        public hasTablePermission_args setCredential(org.apache.accumulo.core.security.thrift.Credential credential) {
            this.credential = credential;
            return this;
        }

        public void unsetCredential() {
            this.credential = null;
        }

        public boolean isSetCredential() {
            return this.credential != null;
        }

        public void setCredentialIsSet(boolean value) {
            if (!value) {
                this.credential = null;
            }
        }

        public String getPrincipal() {
            return this.principal;
        }

        public hasTablePermission_args setPrincipal(String principal) {
            this.principal = principal;
            return this;
        }

        public void unsetPrincipal() {
            this.principal = null;
        }

        public boolean isSetPrincipal() {
            return this.principal != null;
        }

        public void setPrincipalIsSet(boolean value) {
            if (!value) {
                this.principal = null;
            }
        }

        public String getTableName() {
            return this.tableName;
        }

        public hasTablePermission_args setTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public void unsetTableName() {
            this.tableName = null;
        }

        public boolean isSetTableName() {
            return this.tableName != null;
        }

        public void setTableNameIsSet(boolean value) {
            if (!value) {
                this.tableName = null;
            }
        }

        public byte getTblPerm() {
            return this.tblPerm;
        }

        public hasTablePermission_args setTblPerm(byte tblPerm) {
            this.tblPerm = tblPerm;
            setTblPermIsSet(true);
            return this;
        }

        public void unsetTblPerm() {
            __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __TBLPERM_ISSET_ID);
        }

        public boolean isSetTblPerm() {
            return EncodingUtils.testBit(__isset_bitfield, __TBLPERM_ISSET_ID);
        }

        public void setTblPermIsSet(boolean value) {
            __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __TBLPERM_ISSET_ID, value);
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
                        setTinfo((org.apache.accumulo.trace.thrift.TInfo) value);
                    }
                    break;
                case CREDENTIAL:
                    if (value == null) {
                        unsetCredential();
                    } else {
                        setCredential((org.apache.accumulo.core.security.thrift.Credential) value);
                    }
                    break;
                case PRINCIPAL:
                    if (value == null) {
                        unsetPrincipal();
                    } else {
                        setPrincipal((String) value);
                    }
                    break;
                case TABLE_NAME:
                    if (value == null) {
                        unsetTableName();
                    } else {
                        setTableName((String) value);
                    }
                    break;
                case TBL_PERM:
                    if (value == null) {
                        unsetTblPerm();
                    } else {
                        setTblPerm((Byte) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIAL:
                    return getCredential();
                case PRINCIPAL:
                    return getPrincipal();
                case TABLE_NAME:
                    return getTableName();
                case TBL_PERM:
                    return Byte.valueOf(getTblPerm());
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case TINFO:
                    return isSetTinfo();
                case CREDENTIAL:
                    return isSetCredential();
                case PRINCIPAL:
                    return isSetPrincipal();
                case TABLE_NAME:
                    return isSetTableName();
                case TBL_PERM:
                    return isSetTblPerm();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof hasTablePermission_args)
                return this.equals((hasTablePermission_args) that);
            return false;
        }

        public boolean equals(hasTablePermission_args that) {
            if (that == null)
                return false;
            boolean this_present_tinfo = true && this.isSetTinfo();
            boolean that_present_tinfo = true && that.isSetTinfo();
            if (this_present_tinfo || that_present_tinfo) {
                if (!(this_present_tinfo && that_present_tinfo))
                    return false;
                if (!this.tinfo.equals(that.tinfo))
                    return false;
            }
            boolean this_present_credential = true && this.isSetCredential();
            boolean that_present_credential = true && that.isSetCredential();
            if (this_present_credential || that_present_credential) {
                if (!(this_present_credential && that_present_credential))
                    return false;
                if (!this.credential.equals(that.credential))
                    return false;
            }
            boolean this_present_principal = true && this.isSetPrincipal();
            boolean that_present_principal = true && that.isSetPrincipal();
            if (this_present_principal || that_present_principal) {
                if (!(this_present_principal && that_present_principal))
                    return false;
                if (!this.principal.equals(that.principal))
                    return false;
            }
            boolean this_present_tableName = true && this.isSetTableName();
            boolean that_present_tableName = true && that.isSetTableName();
            if (this_present_tableName || that_present_tableName) {
                if (!(this_present_tableName && that_present_tableName))
                    return false;
                if (!this.tableName.equals(that.tableName))
                    return false;
            }
            boolean this_present_tblPerm = true;
            boolean that_present_tblPerm = true;
            if (this_present_tblPerm || that_present_tblPerm) {
                if (!(this_present_tblPerm && that_present_tblPerm))
                    return false;
                if (this.tblPerm != that.tblPerm)
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(hasTablePermission_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            hasTablePermission_args typedOther = (hasTablePermission_args) other;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(typedOther.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, typedOther.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredential()).compareTo(typedOther.isSetCredential());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredential()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credential, typedOther.credential);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetPrincipal()).compareTo(typedOther.isSetPrincipal());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetPrincipal()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.principal, typedOther.principal);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTableName()).compareTo(typedOther.isSetTableName());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTableName()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tableName, typedOther.tableName);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTblPerm()).compareTo(typedOther.isSetTblPerm());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTblPerm()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tblPerm, typedOther.tblPerm);
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
            StringBuilder sb = new StringBuilder("hasTablePermission_args(");
            boolean first = true;
            sb.append("tinfo:");
            if (this.tinfo == null) {
                sb.append("null");
            } else {
                sb.append(this.tinfo);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("credential:");
            if (this.credential == null) {
                sb.append("null");
            } else {
                sb.append(this.credential);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("principal:");
            if (this.principal == null) {
                sb.append("null");
            } else {
                sb.append(this.principal);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("tableName:");
            if (this.tableName == null) {
                sb.append("null");
            } else {
                sb.append(this.tableName);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("tblPerm:");
            sb.append(this.tblPerm);
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credential != null) {
                credential.validate();
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

        private static class hasTablePermission_argsStandardSchemeFactory implements SchemeFactory {

            public hasTablePermission_argsStandardScheme getScheme() {
                return new hasTablePermission_argsStandardScheme();
            }
        }

        private static class hasTablePermission_argsStandardScheme extends StandardScheme<hasTablePermission_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, hasTablePermission_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 5:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                                struct.tinfo.read(iprot);
                                struct.setTinfoIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                                struct.credential.read(iprot);
                                struct.setCredentialIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.principal = iprot.readString();
                                struct.setPrincipalIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.tableName = iprot.readString();
                                struct.setTableNameIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 4:
                            if (schemeField.type == org.apache.thrift.protocol.TType.BYTE) {
                                struct.tblPerm = iprot.readByte();
                                struct.setTblPermIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, hasTablePermission_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credential != null) {
                    oprot.writeFieldBegin(CREDENTIAL_FIELD_DESC);
                    struct.credential.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.principal != null) {
                    oprot.writeFieldBegin(PRINCIPAL_FIELD_DESC);
                    oprot.writeString(struct.principal);
                    oprot.writeFieldEnd();
                }
                if (struct.tableName != null) {
                    oprot.writeFieldBegin(TABLE_NAME_FIELD_DESC);
                    oprot.writeString(struct.tableName);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldBegin(TBL_PERM_FIELD_DESC);
                oprot.writeByte(struct.tblPerm);
                oprot.writeFieldEnd();
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class hasTablePermission_argsTupleSchemeFactory implements SchemeFactory {

            public hasTablePermission_argsTupleScheme getScheme() {
                return new hasTablePermission_argsTupleScheme();
            }
        }

        private static class hasTablePermission_argsTupleScheme extends TupleScheme<hasTablePermission_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, hasTablePermission_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredential()) {
                    optionals.set(1);
                }
                if (struct.isSetPrincipal()) {
                    optionals.set(2);
                }
                if (struct.isSetTableName()) {
                    optionals.set(3);
                }
                if (struct.isSetTblPerm()) {
                    optionals.set(4);
                }
                oprot.writeBitSet(optionals, 5);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredential()) {
                    struct.credential.write(oprot);
                }
                if (struct.isSetPrincipal()) {
                    oprot.writeString(struct.principal);
                }
                if (struct.isSetTableName()) {
                    oprot.writeString(struct.tableName);
                }
                if (struct.isSetTblPerm()) {
                    oprot.writeByte(struct.tblPerm);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, hasTablePermission_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(5);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                    struct.credential.read(iprot);
                    struct.setCredentialIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.principal = iprot.readString();
                    struct.setPrincipalIsSet(true);
                }
                if (incoming.get(3)) {
                    struct.tableName = iprot.readString();
                    struct.setTableNameIsSet(true);
                }
                if (incoming.get(4)) {
                    struct.tblPerm = iprot.readByte();
                    struct.setTblPermIsSet(true);
                }
            }
        }
    }

    public static class hasTablePermission_result implements org.apache.thrift.TBase<hasTablePermission_result, hasTablePermission_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("hasTablePermission_result");

        private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.BOOL, (short) 0);

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField TOPE_FIELD_DESC = new org.apache.thrift.protocol.TField("tope", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new hasTablePermission_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new hasTablePermission_resultTupleSchemeFactory());
        }

        public boolean success;

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException sec;

        public ThriftTableOperationException tope;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SUCCESS((short) 0, "success"), SEC((short) 1, "sec"), TOPE((short) 2, "tope");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 0:
                        return SUCCESS;
                    case 1:
                        return SEC;
                    case 2:
                        return TOPE;
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

        private static final int __SUCCESS_ISSET_ID = 0;

        private byte __isset_bitfield = 0;

        public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
            tmpMap.put(_Fields.SEC, new org.apache.thrift.meta_data.FieldMetaData("sec", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            tmpMap.put(_Fields.TOPE, new org.apache.thrift.meta_data.FieldMetaData("tope", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(hasTablePermission_result.class, metaDataMap);
        }

        public hasTablePermission_result() {
        }

        public hasTablePermission_result(boolean success, org.apache.accumulo.core.security.thrift.ThriftSecurityException sec, ThriftTableOperationException tope) {
            this();
            this.success = success;
            setSuccessIsSet(true);
            this.sec = sec;
            this.tope = tope;
        }

        public hasTablePermission_result(hasTablePermission_result other) {
            __isset_bitfield = other.__isset_bitfield;
            this.success = other.success;
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException(other.sec);
            }
            if (other.isSetTope()) {
                this.tope = new ThriftTableOperationException(other.tope);
            }
        }

        public hasTablePermission_result deepCopy() {
            return new hasTablePermission_result(this);
        }

        @Override
        public void clear() {
            setSuccessIsSet(false);
            this.success = false;
            this.sec = null;
            this.tope = null;
        }

        public boolean isSuccess() {
            return this.success;
        }

        public hasTablePermission_result setSuccess(boolean success) {
            this.success = success;
            setSuccessIsSet(true);
            return this;
        }

        public void unsetSuccess() {
            __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __SUCCESS_ISSET_ID);
        }

        public boolean isSetSuccess() {
            return EncodingUtils.testBit(__isset_bitfield, __SUCCESS_ISSET_ID);
        }

        public void setSuccessIsSet(boolean value) {
            __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __SUCCESS_ISSET_ID, value);
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public hasTablePermission_result setSec(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this.sec = sec;
            return this;
        }

        public void unsetSec() {
            this.sec = null;
        }

        public boolean isSetSec() {
            return this.sec != null;
        }

        public void setSecIsSet(boolean value) {
            if (!value) {
                this.sec = null;
            }
        }

        public ThriftTableOperationException getTope() {
            return this.tope;
        }

        public hasTablePermission_result setTope(ThriftTableOperationException tope) {
            this.tope = tope;
            return this;
        }

        public void unsetTope() {
            this.tope = null;
        }

        public boolean isSetTope() {
            return this.tope != null;
        }

        public void setTopeIsSet(boolean value) {
            if (!value) {
                this.tope = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SUCCESS:
                    if (value == null) {
                        unsetSuccess();
                    } else {
                        setSuccess((Boolean) value);
                    }
                    break;
                case SEC:
                    if (value == null) {
                        unsetSec();
                    } else {
                        setSec((org.apache.accumulo.core.security.thrift.ThriftSecurityException) value);
                    }
                    break;
                case TOPE:
                    if (value == null) {
                        unsetTope();
                    } else {
                        setTope((ThriftTableOperationException) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SUCCESS:
                    return Boolean.valueOf(isSuccess());
                case SEC:
                    return getSec();
                case TOPE:
                    return getTope();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SUCCESS:
                    return isSetSuccess();
                case SEC:
                    return isSetSec();
                case TOPE:
                    return isSetTope();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof hasTablePermission_result)
                return this.equals((hasTablePermission_result) that);
            return false;
        }

        public boolean equals(hasTablePermission_result that) {
            if (that == null)
                return false;
            boolean this_present_success = true;
            boolean that_present_success = true;
            if (this_present_success || that_present_success) {
                if (!(this_present_success && that_present_success))
                    return false;
                if (this.success != that.success)
                    return false;
            }
            boolean this_present_sec = true && this.isSetSec();
            boolean that_present_sec = true && that.isSetSec();
            if (this_present_sec || that_present_sec) {
                if (!(this_present_sec && that_present_sec))
                    return false;
                if (!this.sec.equals(that.sec))
                    return false;
            }
            boolean this_present_tope = true && this.isSetTope();
            boolean that_present_tope = true && that.isSetTope();
            if (this_present_tope || that_present_tope) {
                if (!(this_present_tope && that_present_tope))
                    return false;
                if (!this.tope.equals(that.tope))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(hasTablePermission_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            hasTablePermission_result typedOther = (hasTablePermission_result) other;
            lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSuccess()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, typedOther.success);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(typedOther.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, typedOther.sec);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTope()).compareTo(typedOther.isSetTope());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTope()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tope, typedOther.tope);
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
            StringBuilder sb = new StringBuilder("hasTablePermission_result(");
            boolean first = true;
            sb.append("success:");
            sb.append(this.success);
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("sec:");
            if (this.sec == null) {
                sb.append("null");
            } else {
                sb.append(this.sec);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("tope:");
            if (this.tope == null) {
                sb.append("null");
            } else {
                sb.append(this.tope);
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

        private static class hasTablePermission_resultStandardSchemeFactory implements SchemeFactory {

            public hasTablePermission_resultStandardScheme getScheme() {
                return new hasTablePermission_resultStandardScheme();
            }
        }

        private static class hasTablePermission_resultStandardScheme extends StandardScheme<hasTablePermission_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, hasTablePermission_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 0:
                            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
                                struct.success = iprot.readBool();
                                struct.setSuccessIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tope = new ThriftTableOperationException();
                                struct.tope.read(iprot);
                                struct.setTopeIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, hasTablePermission_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.isSetSuccess()) {
                    oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
                    oprot.writeBool(struct.success);
                    oprot.writeFieldEnd();
                }
                if (struct.sec != null) {
                    oprot.writeFieldBegin(SEC_FIELD_DESC);
                    struct.sec.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.tope != null) {
                    oprot.writeFieldBegin(TOPE_FIELD_DESC);
                    struct.tope.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class hasTablePermission_resultTupleSchemeFactory implements SchemeFactory {

            public hasTablePermission_resultTupleScheme getScheme() {
                return new hasTablePermission_resultTupleScheme();
            }
        }

        private static class hasTablePermission_resultTupleScheme extends TupleScheme<hasTablePermission_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, hasTablePermission_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSuccess()) {
                    optionals.set(0);
                }
                if (struct.isSetSec()) {
                    optionals.set(1);
                }
                if (struct.isSetTope()) {
                    optionals.set(2);
                }
                oprot.writeBitSet(optionals, 3);
                if (struct.isSetSuccess()) {
                    oprot.writeBool(struct.success);
                }
                if (struct.isSetSec()) {
                    struct.sec.write(oprot);
                }
                if (struct.isSetTope()) {
                    struct.tope.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, hasTablePermission_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(3);
                if (incoming.get(0)) {
                    struct.success = iprot.readBool();
                    struct.setSuccessIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.tope = new ThriftTableOperationException();
                    struct.tope.read(iprot);
                    struct.setTopeIsSet(true);
                }
            }
        }
    }

    public static class grantSystemPermission_args implements org.apache.thrift.TBase<grantSystemPermission_args, grantSystemPermission_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("grantSystemPermission_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 4);

        private static final org.apache.thrift.protocol.TField CREDENTIAL_FIELD_DESC = new org.apache.thrift.protocol.TField("credential", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField PRINCIPAL_FIELD_DESC = new org.apache.thrift.protocol.TField("principal", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final org.apache.thrift.protocol.TField PERMISSION_FIELD_DESC = new org.apache.thrift.protocol.TField("permission", org.apache.thrift.protocol.TType.BYTE, (short) 3);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new grantSystemPermission_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new grantSystemPermission_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.Credential credential;

        public String principal;

        public byte permission;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 4, "tinfo"), CREDENTIAL((short) 1, "credential"), PRINCIPAL((short) 2, "principal"), PERMISSION((short) 3, "permission");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 4:
                        return TINFO;
                    case 1:
                        return CREDENTIAL;
                    case 2:
                        return PRINCIPAL;
                    case 3:
                        return PERMISSION;
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

        private static final int __PERMISSION_ISSET_ID = 0;

        private byte __isset_bitfield = 0;

        public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.CREDENTIAL, new org.apache.thrift.meta_data.FieldMetaData("credential", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.Credential.class)));
            tmpMap.put(_Fields.PRINCIPAL, new org.apache.thrift.meta_data.FieldMetaData("principal", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.PERMISSION, new org.apache.thrift.meta_data.FieldMetaData("permission", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BYTE)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(grantSystemPermission_args.class, metaDataMap);
        }

        public grantSystemPermission_args() {
        }

        public grantSystemPermission_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte permission) {
            this();
            this.tinfo = tinfo;
            this.credential = credential;
            this.principal = principal;
            this.permission = permission;
            setPermissionIsSet(true);
        }

        public grantSystemPermission_args(grantSystemPermission_args other) {
            __isset_bitfield = other.__isset_bitfield;
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredential()) {
                this.credential = new org.apache.accumulo.core.security.thrift.Credential(other.credential);
            }
            if (other.isSetPrincipal()) {
                this.principal = other.principal;
            }
            this.permission = other.permission;
        }

        public grantSystemPermission_args deepCopy() {
            return new grantSystemPermission_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credential = null;
            this.principal = null;
            setPermissionIsSet(false);
            this.permission = 0;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public grantSystemPermission_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
            this.tinfo = tinfo;
            return this;
        }

        public void unsetTinfo() {
            this.tinfo = null;
        }

        public boolean isSetTinfo() {
            return this.tinfo != null;
        }

        public void setTinfoIsSet(boolean value) {
            if (!value) {
                this.tinfo = null;
            }
        }

        public org.apache.accumulo.core.security.thrift.Credential getCredential() {
            return this.credential;
        }

        public grantSystemPermission_args setCredential(org.apache.accumulo.core.security.thrift.Credential credential) {
            this.credential = credential;
            return this;
        }

        public void unsetCredential() {
            this.credential = null;
        }

        public boolean isSetCredential() {
            return this.credential != null;
        }

        public void setCredentialIsSet(boolean value) {
            if (!value) {
                this.credential = null;
            }
        }

        public String getPrincipal() {
            return this.principal;
        }

        public grantSystemPermission_args setPrincipal(String principal) {
            this.principal = principal;
            return this;
        }

        public void unsetPrincipal() {
            this.principal = null;
        }

        public boolean isSetPrincipal() {
            return this.principal != null;
        }

        public void setPrincipalIsSet(boolean value) {
            if (!value) {
                this.principal = null;
            }
        }

        public byte getPermission() {
            return this.permission;
        }

        public grantSystemPermission_args setPermission(byte permission) {
            this.permission = permission;
            setPermissionIsSet(true);
            return this;
        }

        public void unsetPermission() {
            __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __PERMISSION_ISSET_ID);
        }

        public boolean isSetPermission() {
            return EncodingUtils.testBit(__isset_bitfield, __PERMISSION_ISSET_ID);
        }

        public void setPermissionIsSet(boolean value) {
            __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __PERMISSION_ISSET_ID, value);
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
                        setTinfo((org.apache.accumulo.trace.thrift.TInfo) value);
                    }
                    break;
                case CREDENTIAL:
                    if (value == null) {
                        unsetCredential();
                    } else {
                        setCredential((org.apache.accumulo.core.security.thrift.Credential) value);
                    }
                    break;
                case PRINCIPAL:
                    if (value == null) {
                        unsetPrincipal();
                    } else {
                        setPrincipal((String) value);
                    }
                    break;
                case PERMISSION:
                    if (value == null) {
                        unsetPermission();
                    } else {
                        setPermission((Byte) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIAL:
                    return getCredential();
                case PRINCIPAL:
                    return getPrincipal();
                case PERMISSION:
                    return Byte.valueOf(getPermission());
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case TINFO:
                    return isSetTinfo();
                case CREDENTIAL:
                    return isSetCredential();
                case PRINCIPAL:
                    return isSetPrincipal();
                case PERMISSION:
                    return isSetPermission();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof grantSystemPermission_args)
                return this.equals((grantSystemPermission_args) that);
            return false;
        }

        public boolean equals(grantSystemPermission_args that) {
            if (that == null)
                return false;
            boolean this_present_tinfo = true && this.isSetTinfo();
            boolean that_present_tinfo = true && that.isSetTinfo();
            if (this_present_tinfo || that_present_tinfo) {
                if (!(this_present_tinfo && that_present_tinfo))
                    return false;
                if (!this.tinfo.equals(that.tinfo))
                    return false;
            }
            boolean this_present_credential = true && this.isSetCredential();
            boolean that_present_credential = true && that.isSetCredential();
            if (this_present_credential || that_present_credential) {
                if (!(this_present_credential && that_present_credential))
                    return false;
                if (!this.credential.equals(that.credential))
                    return false;
            }
            boolean this_present_principal = true && this.isSetPrincipal();
            boolean that_present_principal = true && that.isSetPrincipal();
            if (this_present_principal || that_present_principal) {
                if (!(this_present_principal && that_present_principal))
                    return false;
                if (!this.principal.equals(that.principal))
                    return false;
            }
            boolean this_present_permission = true;
            boolean that_present_permission = true;
            if (this_present_permission || that_present_permission) {
                if (!(this_present_permission && that_present_permission))
                    return false;
                if (this.permission != that.permission)
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(grantSystemPermission_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            grantSystemPermission_args typedOther = (grantSystemPermission_args) other;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(typedOther.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, typedOther.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredential()).compareTo(typedOther.isSetCredential());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredential()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credential, typedOther.credential);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetPrincipal()).compareTo(typedOther.isSetPrincipal());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetPrincipal()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.principal, typedOther.principal);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetPermission()).compareTo(typedOther.isSetPermission());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetPermission()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.permission, typedOther.permission);
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
            StringBuilder sb = new StringBuilder("grantSystemPermission_args(");
            boolean first = true;
            sb.append("tinfo:");
            if (this.tinfo == null) {
                sb.append("null");
            } else {
                sb.append(this.tinfo);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("credential:");
            if (this.credential == null) {
                sb.append("null");
            } else {
                sb.append(this.credential);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("principal:");
            if (this.principal == null) {
                sb.append("null");
            } else {
                sb.append(this.principal);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("permission:");
            sb.append(this.permission);
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credential != null) {
                credential.validate();
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

        private static class grantSystemPermission_argsStandardSchemeFactory implements SchemeFactory {

            public grantSystemPermission_argsStandardScheme getScheme() {
                return new grantSystemPermission_argsStandardScheme();
            }
        }

        private static class grantSystemPermission_argsStandardScheme extends StandardScheme<grantSystemPermission_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, grantSystemPermission_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 4:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                                struct.tinfo.read(iprot);
                                struct.setTinfoIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                                struct.credential.read(iprot);
                                struct.setCredentialIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.principal = iprot.readString();
                                struct.setPrincipalIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.BYTE) {
                                struct.permission = iprot.readByte();
                                struct.setPermissionIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, grantSystemPermission_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credential != null) {
                    oprot.writeFieldBegin(CREDENTIAL_FIELD_DESC);
                    struct.credential.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.principal != null) {
                    oprot.writeFieldBegin(PRINCIPAL_FIELD_DESC);
                    oprot.writeString(struct.principal);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldBegin(PERMISSION_FIELD_DESC);
                oprot.writeByte(struct.permission);
                oprot.writeFieldEnd();
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class grantSystemPermission_argsTupleSchemeFactory implements SchemeFactory {

            public grantSystemPermission_argsTupleScheme getScheme() {
                return new grantSystemPermission_argsTupleScheme();
            }
        }

        private static class grantSystemPermission_argsTupleScheme extends TupleScheme<grantSystemPermission_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, grantSystemPermission_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredential()) {
                    optionals.set(1);
                }
                if (struct.isSetPrincipal()) {
                    optionals.set(2);
                }
                if (struct.isSetPermission()) {
                    optionals.set(3);
                }
                oprot.writeBitSet(optionals, 4);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredential()) {
                    struct.credential.write(oprot);
                }
                if (struct.isSetPrincipal()) {
                    oprot.writeString(struct.principal);
                }
                if (struct.isSetPermission()) {
                    oprot.writeByte(struct.permission);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, grantSystemPermission_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(4);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                    struct.credential.read(iprot);
                    struct.setCredentialIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.principal = iprot.readString();
                    struct.setPrincipalIsSet(true);
                }
                if (incoming.get(3)) {
                    struct.permission = iprot.readByte();
                    struct.setPermissionIsSet(true);
                }
            }
        }
    }

    public static class grantSystemPermission_result implements org.apache.thrift.TBase<grantSystemPermission_result, grantSystemPermission_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("grantSystemPermission_result");

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new grantSystemPermission_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new grantSystemPermission_resultTupleSchemeFactory());
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException sec;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SEC((short) 1, "sec");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 1:
                        return SEC;
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
            tmpMap.put(_Fields.SEC, new org.apache.thrift.meta_data.FieldMetaData("sec", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(grantSystemPermission_result.class, metaDataMap);
        }

        public grantSystemPermission_result() {
        }

        public grantSystemPermission_result(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this();
            this.sec = sec;
        }

        public grantSystemPermission_result(grantSystemPermission_result other) {
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException(other.sec);
            }
        }

        public grantSystemPermission_result deepCopy() {
            return new grantSystemPermission_result(this);
        }

        @Override
        public void clear() {
            this.sec = null;
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public grantSystemPermission_result setSec(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this.sec = sec;
            return this;
        }

        public void unsetSec() {
            this.sec = null;
        }

        public boolean isSetSec() {
            return this.sec != null;
        }

        public void setSecIsSet(boolean value) {
            if (!value) {
                this.sec = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SEC:
                    if (value == null) {
                        unsetSec();
                    } else {
                        setSec((org.apache.accumulo.core.security.thrift.ThriftSecurityException) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SEC:
                    return getSec();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SEC:
                    return isSetSec();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof grantSystemPermission_result)
                return this.equals((grantSystemPermission_result) that);
            return false;
        }

        public boolean equals(grantSystemPermission_result that) {
            if (that == null)
                return false;
            boolean this_present_sec = true && this.isSetSec();
            boolean that_present_sec = true && that.isSetSec();
            if (this_present_sec || that_present_sec) {
                if (!(this_present_sec && that_present_sec))
                    return false;
                if (!this.sec.equals(that.sec))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(grantSystemPermission_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            grantSystemPermission_result typedOther = (grantSystemPermission_result) other;
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(typedOther.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, typedOther.sec);
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
            StringBuilder sb = new StringBuilder("grantSystemPermission_result(");
            boolean first = true;
            sb.append("sec:");
            if (this.sec == null) {
                sb.append("null");
            } else {
                sb.append(this.sec);
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

        private static class grantSystemPermission_resultStandardSchemeFactory implements SchemeFactory {

            public grantSystemPermission_resultStandardScheme getScheme() {
                return new grantSystemPermission_resultStandardScheme();
            }
        }

        private static class grantSystemPermission_resultStandardScheme extends StandardScheme<grantSystemPermission_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, grantSystemPermission_result struct) throws org.apache.thrift.TException {
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
                                struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, grantSystemPermission_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.sec != null) {
                    oprot.writeFieldBegin(SEC_FIELD_DESC);
                    struct.sec.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class grantSystemPermission_resultTupleSchemeFactory implements SchemeFactory {

            public grantSystemPermission_resultTupleScheme getScheme() {
                return new grantSystemPermission_resultTupleScheme();
            }
        }

        private static class grantSystemPermission_resultTupleScheme extends TupleScheme<grantSystemPermission_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, grantSystemPermission_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSec()) {
                    optionals.set(0);
                }
                oprot.writeBitSet(optionals, 1);
                if (struct.isSetSec()) {
                    struct.sec.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, grantSystemPermission_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
            }
        }
    }

    public static class revokeSystemPermission_args implements org.apache.thrift.TBase<revokeSystemPermission_args, revokeSystemPermission_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("revokeSystemPermission_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 4);

        private static final org.apache.thrift.protocol.TField CREDENTIAL_FIELD_DESC = new org.apache.thrift.protocol.TField("credential", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField PRINCIPAL_FIELD_DESC = new org.apache.thrift.protocol.TField("principal", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final org.apache.thrift.protocol.TField PERMISSION_FIELD_DESC = new org.apache.thrift.protocol.TField("permission", org.apache.thrift.protocol.TType.BYTE, (short) 3);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new revokeSystemPermission_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new revokeSystemPermission_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.Credential credential;

        public String principal;

        public byte permission;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 4, "tinfo"), CREDENTIAL((short) 1, "credential"), PRINCIPAL((short) 2, "principal"), PERMISSION((short) 3, "permission");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 4:
                        return TINFO;
                    case 1:
                        return CREDENTIAL;
                    case 2:
                        return PRINCIPAL;
                    case 3:
                        return PERMISSION;
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

        private static final int __PERMISSION_ISSET_ID = 0;

        private byte __isset_bitfield = 0;

        public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.CREDENTIAL, new org.apache.thrift.meta_data.FieldMetaData("credential", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.Credential.class)));
            tmpMap.put(_Fields.PRINCIPAL, new org.apache.thrift.meta_data.FieldMetaData("principal", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.PERMISSION, new org.apache.thrift.meta_data.FieldMetaData("permission", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BYTE)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(revokeSystemPermission_args.class, metaDataMap);
        }

        public revokeSystemPermission_args() {
        }

        public revokeSystemPermission_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, byte permission) {
            this();
            this.tinfo = tinfo;
            this.credential = credential;
            this.principal = principal;
            this.permission = permission;
            setPermissionIsSet(true);
        }

        public revokeSystemPermission_args(revokeSystemPermission_args other) {
            __isset_bitfield = other.__isset_bitfield;
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredential()) {
                this.credential = new org.apache.accumulo.core.security.thrift.Credential(other.credential);
            }
            if (other.isSetPrincipal()) {
                this.principal = other.principal;
            }
            this.permission = other.permission;
        }

        public revokeSystemPermission_args deepCopy() {
            return new revokeSystemPermission_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credential = null;
            this.principal = null;
            setPermissionIsSet(false);
            this.permission = 0;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public revokeSystemPermission_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
            this.tinfo = tinfo;
            return this;
        }

        public void unsetTinfo() {
            this.tinfo = null;
        }

        public boolean isSetTinfo() {
            return this.tinfo != null;
        }

        public void setTinfoIsSet(boolean value) {
            if (!value) {
                this.tinfo = null;
            }
        }

        public org.apache.accumulo.core.security.thrift.Credential getCredential() {
            return this.credential;
        }

        public revokeSystemPermission_args setCredential(org.apache.accumulo.core.security.thrift.Credential credential) {
            this.credential = credential;
            return this;
        }

        public void unsetCredential() {
            this.credential = null;
        }

        public boolean isSetCredential() {
            return this.credential != null;
        }

        public void setCredentialIsSet(boolean value) {
            if (!value) {
                this.credential = null;
            }
        }

        public String getPrincipal() {
            return this.principal;
        }

        public revokeSystemPermission_args setPrincipal(String principal) {
            this.principal = principal;
            return this;
        }

        public void unsetPrincipal() {
            this.principal = null;
        }

        public boolean isSetPrincipal() {
            return this.principal != null;
        }

        public void setPrincipalIsSet(boolean value) {
            if (!value) {
                this.principal = null;
            }
        }

        public byte getPermission() {
            return this.permission;
        }

        public revokeSystemPermission_args setPermission(byte permission) {
            this.permission = permission;
            setPermissionIsSet(true);
            return this;
        }

        public void unsetPermission() {
            __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __PERMISSION_ISSET_ID);
        }

        public boolean isSetPermission() {
            return EncodingUtils.testBit(__isset_bitfield, __PERMISSION_ISSET_ID);
        }

        public void setPermissionIsSet(boolean value) {
            __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __PERMISSION_ISSET_ID, value);
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
                        setTinfo((org.apache.accumulo.trace.thrift.TInfo) value);
                    }
                    break;
                case CREDENTIAL:
                    if (value == null) {
                        unsetCredential();
                    } else {
                        setCredential((org.apache.accumulo.core.security.thrift.Credential) value);
                    }
                    break;
                case PRINCIPAL:
                    if (value == null) {
                        unsetPrincipal();
                    } else {
                        setPrincipal((String) value);
                    }
                    break;
                case PERMISSION:
                    if (value == null) {
                        unsetPermission();
                    } else {
                        setPermission((Byte) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIAL:
                    return getCredential();
                case PRINCIPAL:
                    return getPrincipal();
                case PERMISSION:
                    return Byte.valueOf(getPermission());
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case TINFO:
                    return isSetTinfo();
                case CREDENTIAL:
                    return isSetCredential();
                case PRINCIPAL:
                    return isSetPrincipal();
                case PERMISSION:
                    return isSetPermission();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof revokeSystemPermission_args)
                return this.equals((revokeSystemPermission_args) that);
            return false;
        }

        public boolean equals(revokeSystemPermission_args that) {
            if (that == null)
                return false;
            boolean this_present_tinfo = true && this.isSetTinfo();
            boolean that_present_tinfo = true && that.isSetTinfo();
            if (this_present_tinfo || that_present_tinfo) {
                if (!(this_present_tinfo && that_present_tinfo))
                    return false;
                if (!this.tinfo.equals(that.tinfo))
                    return false;
            }
            boolean this_present_credential = true && this.isSetCredential();
            boolean that_present_credential = true && that.isSetCredential();
            if (this_present_credential || that_present_credential) {
                if (!(this_present_credential && that_present_credential))
                    return false;
                if (!this.credential.equals(that.credential))
                    return false;
            }
            boolean this_present_principal = true && this.isSetPrincipal();
            boolean that_present_principal = true && that.isSetPrincipal();
            if (this_present_principal || that_present_principal) {
                if (!(this_present_principal && that_present_principal))
                    return false;
                if (!this.principal.equals(that.principal))
                    return false;
            }
            boolean this_present_permission = true;
            boolean that_present_permission = true;
            if (this_present_permission || that_present_permission) {
                if (!(this_present_permission && that_present_permission))
                    return false;
                if (this.permission != that.permission)
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(revokeSystemPermission_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            revokeSystemPermission_args typedOther = (revokeSystemPermission_args) other;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(typedOther.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, typedOther.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredential()).compareTo(typedOther.isSetCredential());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredential()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credential, typedOther.credential);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetPrincipal()).compareTo(typedOther.isSetPrincipal());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetPrincipal()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.principal, typedOther.principal);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetPermission()).compareTo(typedOther.isSetPermission());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetPermission()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.permission, typedOther.permission);
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
            StringBuilder sb = new StringBuilder("revokeSystemPermission_args(");
            boolean first = true;
            sb.append("tinfo:");
            if (this.tinfo == null) {
                sb.append("null");
            } else {
                sb.append(this.tinfo);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("credential:");
            if (this.credential == null) {
                sb.append("null");
            } else {
                sb.append(this.credential);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("principal:");
            if (this.principal == null) {
                sb.append("null");
            } else {
                sb.append(this.principal);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("permission:");
            sb.append(this.permission);
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credential != null) {
                credential.validate();
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

        private static class revokeSystemPermission_argsStandardSchemeFactory implements SchemeFactory {

            public revokeSystemPermission_argsStandardScheme getScheme() {
                return new revokeSystemPermission_argsStandardScheme();
            }
        }

        private static class revokeSystemPermission_argsStandardScheme extends StandardScheme<revokeSystemPermission_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, revokeSystemPermission_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 4:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                                struct.tinfo.read(iprot);
                                struct.setTinfoIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                                struct.credential.read(iprot);
                                struct.setCredentialIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.principal = iprot.readString();
                                struct.setPrincipalIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.BYTE) {
                                struct.permission = iprot.readByte();
                                struct.setPermissionIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, revokeSystemPermission_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credential != null) {
                    oprot.writeFieldBegin(CREDENTIAL_FIELD_DESC);
                    struct.credential.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.principal != null) {
                    oprot.writeFieldBegin(PRINCIPAL_FIELD_DESC);
                    oprot.writeString(struct.principal);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldBegin(PERMISSION_FIELD_DESC);
                oprot.writeByte(struct.permission);
                oprot.writeFieldEnd();
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class revokeSystemPermission_argsTupleSchemeFactory implements SchemeFactory {

            public revokeSystemPermission_argsTupleScheme getScheme() {
                return new revokeSystemPermission_argsTupleScheme();
            }
        }

        private static class revokeSystemPermission_argsTupleScheme extends TupleScheme<revokeSystemPermission_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, revokeSystemPermission_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredential()) {
                    optionals.set(1);
                }
                if (struct.isSetPrincipal()) {
                    optionals.set(2);
                }
                if (struct.isSetPermission()) {
                    optionals.set(3);
                }
                oprot.writeBitSet(optionals, 4);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredential()) {
                    struct.credential.write(oprot);
                }
                if (struct.isSetPrincipal()) {
                    oprot.writeString(struct.principal);
                }
                if (struct.isSetPermission()) {
                    oprot.writeByte(struct.permission);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, revokeSystemPermission_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(4);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                    struct.credential.read(iprot);
                    struct.setCredentialIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.principal = iprot.readString();
                    struct.setPrincipalIsSet(true);
                }
                if (incoming.get(3)) {
                    struct.permission = iprot.readByte();
                    struct.setPermissionIsSet(true);
                }
            }
        }
    }

    public static class revokeSystemPermission_result implements org.apache.thrift.TBase<revokeSystemPermission_result, revokeSystemPermission_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("revokeSystemPermission_result");

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new revokeSystemPermission_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new revokeSystemPermission_resultTupleSchemeFactory());
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException sec;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SEC((short) 1, "sec");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 1:
                        return SEC;
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
            tmpMap.put(_Fields.SEC, new org.apache.thrift.meta_data.FieldMetaData("sec", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(revokeSystemPermission_result.class, metaDataMap);
        }

        public revokeSystemPermission_result() {
        }

        public revokeSystemPermission_result(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this();
            this.sec = sec;
        }

        public revokeSystemPermission_result(revokeSystemPermission_result other) {
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException(other.sec);
            }
        }

        public revokeSystemPermission_result deepCopy() {
            return new revokeSystemPermission_result(this);
        }

        @Override
        public void clear() {
            this.sec = null;
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public revokeSystemPermission_result setSec(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this.sec = sec;
            return this;
        }

        public void unsetSec() {
            this.sec = null;
        }

        public boolean isSetSec() {
            return this.sec != null;
        }

        public void setSecIsSet(boolean value) {
            if (!value) {
                this.sec = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SEC:
                    if (value == null) {
                        unsetSec();
                    } else {
                        setSec((org.apache.accumulo.core.security.thrift.ThriftSecurityException) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SEC:
                    return getSec();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SEC:
                    return isSetSec();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof revokeSystemPermission_result)
                return this.equals((revokeSystemPermission_result) that);
            return false;
        }

        public boolean equals(revokeSystemPermission_result that) {
            if (that == null)
                return false;
            boolean this_present_sec = true && this.isSetSec();
            boolean that_present_sec = true && that.isSetSec();
            if (this_present_sec || that_present_sec) {
                if (!(this_present_sec && that_present_sec))
                    return false;
                if (!this.sec.equals(that.sec))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(revokeSystemPermission_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            revokeSystemPermission_result typedOther = (revokeSystemPermission_result) other;
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(typedOther.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, typedOther.sec);
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
            StringBuilder sb = new StringBuilder("revokeSystemPermission_result(");
            boolean first = true;
            sb.append("sec:");
            if (this.sec == null) {
                sb.append("null");
            } else {
                sb.append(this.sec);
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

        private static class revokeSystemPermission_resultStandardSchemeFactory implements SchemeFactory {

            public revokeSystemPermission_resultStandardScheme getScheme() {
                return new revokeSystemPermission_resultStandardScheme();
            }
        }

        private static class revokeSystemPermission_resultStandardScheme extends StandardScheme<revokeSystemPermission_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, revokeSystemPermission_result struct) throws org.apache.thrift.TException {
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
                                struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, revokeSystemPermission_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.sec != null) {
                    oprot.writeFieldBegin(SEC_FIELD_DESC);
                    struct.sec.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class revokeSystemPermission_resultTupleSchemeFactory implements SchemeFactory {

            public revokeSystemPermission_resultTupleScheme getScheme() {
                return new revokeSystemPermission_resultTupleScheme();
            }
        }

        private static class revokeSystemPermission_resultTupleScheme extends TupleScheme<revokeSystemPermission_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, revokeSystemPermission_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSec()) {
                    optionals.set(0);
                }
                oprot.writeBitSet(optionals, 1);
                if (struct.isSetSec()) {
                    struct.sec.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, revokeSystemPermission_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
            }
        }
    }

    public static class grantTablePermission_args implements org.apache.thrift.TBase<grantTablePermission_args, grantTablePermission_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("grantTablePermission_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 5);

        private static final org.apache.thrift.protocol.TField CREDENTIAL_FIELD_DESC = new org.apache.thrift.protocol.TField("credential", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField PRINCIPAL_FIELD_DESC = new org.apache.thrift.protocol.TField("principal", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final org.apache.thrift.protocol.TField TABLE_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("tableName", org.apache.thrift.protocol.TType.STRING, (short) 3);

        private static final org.apache.thrift.protocol.TField PERMISSION_FIELD_DESC = new org.apache.thrift.protocol.TField("permission", org.apache.thrift.protocol.TType.BYTE, (short) 4);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new grantTablePermission_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new grantTablePermission_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.Credential credential;

        public String principal;

        public String tableName;

        public byte permission;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 5, "tinfo"), CREDENTIAL((short) 1, "credential"), PRINCIPAL((short) 2, "principal"), TABLE_NAME((short) 3, "tableName"), PERMISSION((short) 4, "permission");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 5:
                        return TINFO;
                    case 1:
                        return CREDENTIAL;
                    case 2:
                        return PRINCIPAL;
                    case 3:
                        return TABLE_NAME;
                    case 4:
                        return PERMISSION;
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

        private static final int __PERMISSION_ISSET_ID = 0;

        private byte __isset_bitfield = 0;

        public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.CREDENTIAL, new org.apache.thrift.meta_data.FieldMetaData("credential", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.Credential.class)));
            tmpMap.put(_Fields.PRINCIPAL, new org.apache.thrift.meta_data.FieldMetaData("principal", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.TABLE_NAME, new org.apache.thrift.meta_data.FieldMetaData("tableName", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.PERMISSION, new org.apache.thrift.meta_data.FieldMetaData("permission", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BYTE)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(grantTablePermission_args.class, metaDataMap);
        }

        public grantTablePermission_args() {
        }

        public grantTablePermission_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte permission) {
            this();
            this.tinfo = tinfo;
            this.credential = credential;
            this.principal = principal;
            this.tableName = tableName;
            this.permission = permission;
            setPermissionIsSet(true);
        }

        public grantTablePermission_args(grantTablePermission_args other) {
            __isset_bitfield = other.__isset_bitfield;
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredential()) {
                this.credential = new org.apache.accumulo.core.security.thrift.Credential(other.credential);
            }
            if (other.isSetPrincipal()) {
                this.principal = other.principal;
            }
            if (other.isSetTableName()) {
                this.tableName = other.tableName;
            }
            this.permission = other.permission;
        }

        public grantTablePermission_args deepCopy() {
            return new grantTablePermission_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credential = null;
            this.principal = null;
            this.tableName = null;
            setPermissionIsSet(false);
            this.permission = 0;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public grantTablePermission_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
            this.tinfo = tinfo;
            return this;
        }

        public void unsetTinfo() {
            this.tinfo = null;
        }

        public boolean isSetTinfo() {
            return this.tinfo != null;
        }

        public void setTinfoIsSet(boolean value) {
            if (!value) {
                this.tinfo = null;
            }
        }

        public org.apache.accumulo.core.security.thrift.Credential getCredential() {
            return this.credential;
        }

        public grantTablePermission_args setCredential(org.apache.accumulo.core.security.thrift.Credential credential) {
            this.credential = credential;
            return this;
        }

        public void unsetCredential() {
            this.credential = null;
        }

        public boolean isSetCredential() {
            return this.credential != null;
        }

        public void setCredentialIsSet(boolean value) {
            if (!value) {
                this.credential = null;
            }
        }

        public String getPrincipal() {
            return this.principal;
        }

        public grantTablePermission_args setPrincipal(String principal) {
            this.principal = principal;
            return this;
        }

        public void unsetPrincipal() {
            this.principal = null;
        }

        public boolean isSetPrincipal() {
            return this.principal != null;
        }

        public void setPrincipalIsSet(boolean value) {
            if (!value) {
                this.principal = null;
            }
        }

        public String getTableName() {
            return this.tableName;
        }

        public grantTablePermission_args setTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public void unsetTableName() {
            this.tableName = null;
        }

        public boolean isSetTableName() {
            return this.tableName != null;
        }

        public void setTableNameIsSet(boolean value) {
            if (!value) {
                this.tableName = null;
            }
        }

        public byte getPermission() {
            return this.permission;
        }

        public grantTablePermission_args setPermission(byte permission) {
            this.permission = permission;
            setPermissionIsSet(true);
            return this;
        }

        public void unsetPermission() {
            __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __PERMISSION_ISSET_ID);
        }

        public boolean isSetPermission() {
            return EncodingUtils.testBit(__isset_bitfield, __PERMISSION_ISSET_ID);
        }

        public void setPermissionIsSet(boolean value) {
            __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __PERMISSION_ISSET_ID, value);
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
                        setTinfo((org.apache.accumulo.trace.thrift.TInfo) value);
                    }
                    break;
                case CREDENTIAL:
                    if (value == null) {
                        unsetCredential();
                    } else {
                        setCredential((org.apache.accumulo.core.security.thrift.Credential) value);
                    }
                    break;
                case PRINCIPAL:
                    if (value == null) {
                        unsetPrincipal();
                    } else {
                        setPrincipal((String) value);
                    }
                    break;
                case TABLE_NAME:
                    if (value == null) {
                        unsetTableName();
                    } else {
                        setTableName((String) value);
                    }
                    break;
                case PERMISSION:
                    if (value == null) {
                        unsetPermission();
                    } else {
                        setPermission((Byte) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIAL:
                    return getCredential();
                case PRINCIPAL:
                    return getPrincipal();
                case TABLE_NAME:
                    return getTableName();
                case PERMISSION:
                    return Byte.valueOf(getPermission());
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case TINFO:
                    return isSetTinfo();
                case CREDENTIAL:
                    return isSetCredential();
                case PRINCIPAL:
                    return isSetPrincipal();
                case TABLE_NAME:
                    return isSetTableName();
                case PERMISSION:
                    return isSetPermission();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof grantTablePermission_args)
                return this.equals((grantTablePermission_args) that);
            return false;
        }

        public boolean equals(grantTablePermission_args that) {
            if (that == null)
                return false;
            boolean this_present_tinfo = true && this.isSetTinfo();
            boolean that_present_tinfo = true && that.isSetTinfo();
            if (this_present_tinfo || that_present_tinfo) {
                if (!(this_present_tinfo && that_present_tinfo))
                    return false;
                if (!this.tinfo.equals(that.tinfo))
                    return false;
            }
            boolean this_present_credential = true && this.isSetCredential();
            boolean that_present_credential = true && that.isSetCredential();
            if (this_present_credential || that_present_credential) {
                if (!(this_present_credential && that_present_credential))
                    return false;
                if (!this.credential.equals(that.credential))
                    return false;
            }
            boolean this_present_principal = true && this.isSetPrincipal();
            boolean that_present_principal = true && that.isSetPrincipal();
            if (this_present_principal || that_present_principal) {
                if (!(this_present_principal && that_present_principal))
                    return false;
                if (!this.principal.equals(that.principal))
                    return false;
            }
            boolean this_present_tableName = true && this.isSetTableName();
            boolean that_present_tableName = true && that.isSetTableName();
            if (this_present_tableName || that_present_tableName) {
                if (!(this_present_tableName && that_present_tableName))
                    return false;
                if (!this.tableName.equals(that.tableName))
                    return false;
            }
            boolean this_present_permission = true;
            boolean that_present_permission = true;
            if (this_present_permission || that_present_permission) {
                if (!(this_present_permission && that_present_permission))
                    return false;
                if (this.permission != that.permission)
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(grantTablePermission_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            grantTablePermission_args typedOther = (grantTablePermission_args) other;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(typedOther.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, typedOther.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredential()).compareTo(typedOther.isSetCredential());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredential()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credential, typedOther.credential);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetPrincipal()).compareTo(typedOther.isSetPrincipal());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetPrincipal()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.principal, typedOther.principal);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTableName()).compareTo(typedOther.isSetTableName());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTableName()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tableName, typedOther.tableName);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetPermission()).compareTo(typedOther.isSetPermission());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetPermission()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.permission, typedOther.permission);
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
            StringBuilder sb = new StringBuilder("grantTablePermission_args(");
            boolean first = true;
            sb.append("tinfo:");
            if (this.tinfo == null) {
                sb.append("null");
            } else {
                sb.append(this.tinfo);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("credential:");
            if (this.credential == null) {
                sb.append("null");
            } else {
                sb.append(this.credential);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("principal:");
            if (this.principal == null) {
                sb.append("null");
            } else {
                sb.append(this.principal);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("tableName:");
            if (this.tableName == null) {
                sb.append("null");
            } else {
                sb.append(this.tableName);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("permission:");
            sb.append(this.permission);
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credential != null) {
                credential.validate();
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

        private static class grantTablePermission_argsStandardSchemeFactory implements SchemeFactory {

            public grantTablePermission_argsStandardScheme getScheme() {
                return new grantTablePermission_argsStandardScheme();
            }
        }

        private static class grantTablePermission_argsStandardScheme extends StandardScheme<grantTablePermission_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, grantTablePermission_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 5:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                                struct.tinfo.read(iprot);
                                struct.setTinfoIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                                struct.credential.read(iprot);
                                struct.setCredentialIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.principal = iprot.readString();
                                struct.setPrincipalIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.tableName = iprot.readString();
                                struct.setTableNameIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 4:
                            if (schemeField.type == org.apache.thrift.protocol.TType.BYTE) {
                                struct.permission = iprot.readByte();
                                struct.setPermissionIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, grantTablePermission_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credential != null) {
                    oprot.writeFieldBegin(CREDENTIAL_FIELD_DESC);
                    struct.credential.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.principal != null) {
                    oprot.writeFieldBegin(PRINCIPAL_FIELD_DESC);
                    oprot.writeString(struct.principal);
                    oprot.writeFieldEnd();
                }
                if (struct.tableName != null) {
                    oprot.writeFieldBegin(TABLE_NAME_FIELD_DESC);
                    oprot.writeString(struct.tableName);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldBegin(PERMISSION_FIELD_DESC);
                oprot.writeByte(struct.permission);
                oprot.writeFieldEnd();
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class grantTablePermission_argsTupleSchemeFactory implements SchemeFactory {

            public grantTablePermission_argsTupleScheme getScheme() {
                return new grantTablePermission_argsTupleScheme();
            }
        }

        private static class grantTablePermission_argsTupleScheme extends TupleScheme<grantTablePermission_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, grantTablePermission_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredential()) {
                    optionals.set(1);
                }
                if (struct.isSetPrincipal()) {
                    optionals.set(2);
                }
                if (struct.isSetTableName()) {
                    optionals.set(3);
                }
                if (struct.isSetPermission()) {
                    optionals.set(4);
                }
                oprot.writeBitSet(optionals, 5);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredential()) {
                    struct.credential.write(oprot);
                }
                if (struct.isSetPrincipal()) {
                    oprot.writeString(struct.principal);
                }
                if (struct.isSetTableName()) {
                    oprot.writeString(struct.tableName);
                }
                if (struct.isSetPermission()) {
                    oprot.writeByte(struct.permission);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, grantTablePermission_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(5);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                    struct.credential.read(iprot);
                    struct.setCredentialIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.principal = iprot.readString();
                    struct.setPrincipalIsSet(true);
                }
                if (incoming.get(3)) {
                    struct.tableName = iprot.readString();
                    struct.setTableNameIsSet(true);
                }
                if (incoming.get(4)) {
                    struct.permission = iprot.readByte();
                    struct.setPermissionIsSet(true);
                }
            }
        }
    }

    public static class grantTablePermission_result implements org.apache.thrift.TBase<grantTablePermission_result, grantTablePermission_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("grantTablePermission_result");

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField TOPE_FIELD_DESC = new org.apache.thrift.protocol.TField("tope", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new grantTablePermission_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new grantTablePermission_resultTupleSchemeFactory());
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException sec;

        public ThriftTableOperationException tope;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SEC((short) 1, "sec"), TOPE((short) 2, "tope");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 1:
                        return SEC;
                    case 2:
                        return TOPE;
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
            tmpMap.put(_Fields.SEC, new org.apache.thrift.meta_data.FieldMetaData("sec", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            tmpMap.put(_Fields.TOPE, new org.apache.thrift.meta_data.FieldMetaData("tope", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(grantTablePermission_result.class, metaDataMap);
        }

        public grantTablePermission_result() {
        }

        public grantTablePermission_result(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec, ThriftTableOperationException tope) {
            this();
            this.sec = sec;
            this.tope = tope;
        }

        public grantTablePermission_result(grantTablePermission_result other) {
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException(other.sec);
            }
            if (other.isSetTope()) {
                this.tope = new ThriftTableOperationException(other.tope);
            }
        }

        public grantTablePermission_result deepCopy() {
            return new grantTablePermission_result(this);
        }

        @Override
        public void clear() {
            this.sec = null;
            this.tope = null;
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public grantTablePermission_result setSec(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this.sec = sec;
            return this;
        }

        public void unsetSec() {
            this.sec = null;
        }

        public boolean isSetSec() {
            return this.sec != null;
        }

        public void setSecIsSet(boolean value) {
            if (!value) {
                this.sec = null;
            }
        }

        public ThriftTableOperationException getTope() {
            return this.tope;
        }

        public grantTablePermission_result setTope(ThriftTableOperationException tope) {
            this.tope = tope;
            return this;
        }

        public void unsetTope() {
            this.tope = null;
        }

        public boolean isSetTope() {
            return this.tope != null;
        }

        public void setTopeIsSet(boolean value) {
            if (!value) {
                this.tope = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SEC:
                    if (value == null) {
                        unsetSec();
                    } else {
                        setSec((org.apache.accumulo.core.security.thrift.ThriftSecurityException) value);
                    }
                    break;
                case TOPE:
                    if (value == null) {
                        unsetTope();
                    } else {
                        setTope((ThriftTableOperationException) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SEC:
                    return getSec();
                case TOPE:
                    return getTope();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SEC:
                    return isSetSec();
                case TOPE:
                    return isSetTope();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof grantTablePermission_result)
                return this.equals((grantTablePermission_result) that);
            return false;
        }

        public boolean equals(grantTablePermission_result that) {
            if (that == null)
                return false;
            boolean this_present_sec = true && this.isSetSec();
            boolean that_present_sec = true && that.isSetSec();
            if (this_present_sec || that_present_sec) {
                if (!(this_present_sec && that_present_sec))
                    return false;
                if (!this.sec.equals(that.sec))
                    return false;
            }
            boolean this_present_tope = true && this.isSetTope();
            boolean that_present_tope = true && that.isSetTope();
            if (this_present_tope || that_present_tope) {
                if (!(this_present_tope && that_present_tope))
                    return false;
                if (!this.tope.equals(that.tope))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(grantTablePermission_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            grantTablePermission_result typedOther = (grantTablePermission_result) other;
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(typedOther.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, typedOther.sec);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTope()).compareTo(typedOther.isSetTope());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTope()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tope, typedOther.tope);
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
            StringBuilder sb = new StringBuilder("grantTablePermission_result(");
            boolean first = true;
            sb.append("sec:");
            if (this.sec == null) {
                sb.append("null");
            } else {
                sb.append(this.sec);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("tope:");
            if (this.tope == null) {
                sb.append("null");
            } else {
                sb.append(this.tope);
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

        private static class grantTablePermission_resultStandardSchemeFactory implements SchemeFactory {

            public grantTablePermission_resultStandardScheme getScheme() {
                return new grantTablePermission_resultStandardScheme();
            }
        }

        private static class grantTablePermission_resultStandardScheme extends StandardScheme<grantTablePermission_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, grantTablePermission_result struct) throws org.apache.thrift.TException {
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
                                struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tope = new ThriftTableOperationException();
                                struct.tope.read(iprot);
                                struct.setTopeIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, grantTablePermission_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.sec != null) {
                    oprot.writeFieldBegin(SEC_FIELD_DESC);
                    struct.sec.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.tope != null) {
                    oprot.writeFieldBegin(TOPE_FIELD_DESC);
                    struct.tope.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class grantTablePermission_resultTupleSchemeFactory implements SchemeFactory {

            public grantTablePermission_resultTupleScheme getScheme() {
                return new grantTablePermission_resultTupleScheme();
            }
        }

        private static class grantTablePermission_resultTupleScheme extends TupleScheme<grantTablePermission_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, grantTablePermission_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSec()) {
                    optionals.set(0);
                }
                if (struct.isSetTope()) {
                    optionals.set(1);
                }
                oprot.writeBitSet(optionals, 2);
                if (struct.isSetSec()) {
                    struct.sec.write(oprot);
                }
                if (struct.isSetTope()) {
                    struct.tope.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, grantTablePermission_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.tope = new ThriftTableOperationException();
                    struct.tope.read(iprot);
                    struct.setTopeIsSet(true);
                }
            }
        }
    }

    public static class revokeTablePermission_args implements org.apache.thrift.TBase<revokeTablePermission_args, revokeTablePermission_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("revokeTablePermission_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 5);

        private static final org.apache.thrift.protocol.TField CREDENTIAL_FIELD_DESC = new org.apache.thrift.protocol.TField("credential", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField PRINCIPAL_FIELD_DESC = new org.apache.thrift.protocol.TField("principal", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final org.apache.thrift.protocol.TField TABLE_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("tableName", org.apache.thrift.protocol.TType.STRING, (short) 3);

        private static final org.apache.thrift.protocol.TField PERMISSION_FIELD_DESC = new org.apache.thrift.protocol.TField("permission", org.apache.thrift.protocol.TType.BYTE, (short) 4);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new revokeTablePermission_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new revokeTablePermission_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.Credential credential;

        public String principal;

        public String tableName;

        public byte permission;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 5, "tinfo"), CREDENTIAL((short) 1, "credential"), PRINCIPAL((short) 2, "principal"), TABLE_NAME((short) 3, "tableName"), PERMISSION((short) 4, "permission");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 5:
                        return TINFO;
                    case 1:
                        return CREDENTIAL;
                    case 2:
                        return PRINCIPAL;
                    case 3:
                        return TABLE_NAME;
                    case 4:
                        return PERMISSION;
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

        private static final int __PERMISSION_ISSET_ID = 0;

        private byte __isset_bitfield = 0;

        public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.CREDENTIAL, new org.apache.thrift.meta_data.FieldMetaData("credential", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.Credential.class)));
            tmpMap.put(_Fields.PRINCIPAL, new org.apache.thrift.meta_data.FieldMetaData("principal", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.TABLE_NAME, new org.apache.thrift.meta_data.FieldMetaData("tableName", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.PERMISSION, new org.apache.thrift.meta_data.FieldMetaData("permission", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BYTE)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(revokeTablePermission_args.class, metaDataMap);
        }

        public revokeTablePermission_args() {
        }

        public revokeTablePermission_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.Credential credential, String principal, String tableName, byte permission) {
            this();
            this.tinfo = tinfo;
            this.credential = credential;
            this.principal = principal;
            this.tableName = tableName;
            this.permission = permission;
            setPermissionIsSet(true);
        }

        public revokeTablePermission_args(revokeTablePermission_args other) {
            __isset_bitfield = other.__isset_bitfield;
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredential()) {
                this.credential = new org.apache.accumulo.core.security.thrift.Credential(other.credential);
            }
            if (other.isSetPrincipal()) {
                this.principal = other.principal;
            }
            if (other.isSetTableName()) {
                this.tableName = other.tableName;
            }
            this.permission = other.permission;
        }

        public revokeTablePermission_args deepCopy() {
            return new revokeTablePermission_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credential = null;
            this.principal = null;
            this.tableName = null;
            setPermissionIsSet(false);
            this.permission = 0;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public revokeTablePermission_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
            this.tinfo = tinfo;
            return this;
        }

        public void unsetTinfo() {
            this.tinfo = null;
        }

        public boolean isSetTinfo() {
            return this.tinfo != null;
        }

        public void setTinfoIsSet(boolean value) {
            if (!value) {
                this.tinfo = null;
            }
        }

        public org.apache.accumulo.core.security.thrift.Credential getCredential() {
            return this.credential;
        }

        public revokeTablePermission_args setCredential(org.apache.accumulo.core.security.thrift.Credential credential) {
            this.credential = credential;
            return this;
        }

        public void unsetCredential() {
            this.credential = null;
        }

        public boolean isSetCredential() {
            return this.credential != null;
        }

        public void setCredentialIsSet(boolean value) {
            if (!value) {
                this.credential = null;
            }
        }

        public String getPrincipal() {
            return this.principal;
        }

        public revokeTablePermission_args setPrincipal(String principal) {
            this.principal = principal;
            return this;
        }

        public void unsetPrincipal() {
            this.principal = null;
        }

        public boolean isSetPrincipal() {
            return this.principal != null;
        }

        public void setPrincipalIsSet(boolean value) {
            if (!value) {
                this.principal = null;
            }
        }

        public String getTableName() {
            return this.tableName;
        }

        public revokeTablePermission_args setTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public void unsetTableName() {
            this.tableName = null;
        }

        public boolean isSetTableName() {
            return this.tableName != null;
        }

        public void setTableNameIsSet(boolean value) {
            if (!value) {
                this.tableName = null;
            }
        }

        public byte getPermission() {
            return this.permission;
        }

        public revokeTablePermission_args setPermission(byte permission) {
            this.permission = permission;
            setPermissionIsSet(true);
            return this;
        }

        public void unsetPermission() {
            __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __PERMISSION_ISSET_ID);
        }

        public boolean isSetPermission() {
            return EncodingUtils.testBit(__isset_bitfield, __PERMISSION_ISSET_ID);
        }

        public void setPermissionIsSet(boolean value) {
            __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __PERMISSION_ISSET_ID, value);
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
                        setTinfo((org.apache.accumulo.trace.thrift.TInfo) value);
                    }
                    break;
                case CREDENTIAL:
                    if (value == null) {
                        unsetCredential();
                    } else {
                        setCredential((org.apache.accumulo.core.security.thrift.Credential) value);
                    }
                    break;
                case PRINCIPAL:
                    if (value == null) {
                        unsetPrincipal();
                    } else {
                        setPrincipal((String) value);
                    }
                    break;
                case TABLE_NAME:
                    if (value == null) {
                        unsetTableName();
                    } else {
                        setTableName((String) value);
                    }
                    break;
                case PERMISSION:
                    if (value == null) {
                        unsetPermission();
                    } else {
                        setPermission((Byte) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIAL:
                    return getCredential();
                case PRINCIPAL:
                    return getPrincipal();
                case TABLE_NAME:
                    return getTableName();
                case PERMISSION:
                    return Byte.valueOf(getPermission());
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case TINFO:
                    return isSetTinfo();
                case CREDENTIAL:
                    return isSetCredential();
                case PRINCIPAL:
                    return isSetPrincipal();
                case TABLE_NAME:
                    return isSetTableName();
                case PERMISSION:
                    return isSetPermission();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof revokeTablePermission_args)
                return this.equals((revokeTablePermission_args) that);
            return false;
        }

        public boolean equals(revokeTablePermission_args that) {
            if (that == null)
                return false;
            boolean this_present_tinfo = true && this.isSetTinfo();
            boolean that_present_tinfo = true && that.isSetTinfo();
            if (this_present_tinfo || that_present_tinfo) {
                if (!(this_present_tinfo && that_present_tinfo))
                    return false;
                if (!this.tinfo.equals(that.tinfo))
                    return false;
            }
            boolean this_present_credential = true && this.isSetCredential();
            boolean that_present_credential = true && that.isSetCredential();
            if (this_present_credential || that_present_credential) {
                if (!(this_present_credential && that_present_credential))
                    return false;
                if (!this.credential.equals(that.credential))
                    return false;
            }
            boolean this_present_principal = true && this.isSetPrincipal();
            boolean that_present_principal = true && that.isSetPrincipal();
            if (this_present_principal || that_present_principal) {
                if (!(this_present_principal && that_present_principal))
                    return false;
                if (!this.principal.equals(that.principal))
                    return false;
            }
            boolean this_present_tableName = true && this.isSetTableName();
            boolean that_present_tableName = true && that.isSetTableName();
            if (this_present_tableName || that_present_tableName) {
                if (!(this_present_tableName && that_present_tableName))
                    return false;
                if (!this.tableName.equals(that.tableName))
                    return false;
            }
            boolean this_present_permission = true;
            boolean that_present_permission = true;
            if (this_present_permission || that_present_permission) {
                if (!(this_present_permission && that_present_permission))
                    return false;
                if (this.permission != that.permission)
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(revokeTablePermission_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            revokeTablePermission_args typedOther = (revokeTablePermission_args) other;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(typedOther.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, typedOther.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredential()).compareTo(typedOther.isSetCredential());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredential()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credential, typedOther.credential);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetPrincipal()).compareTo(typedOther.isSetPrincipal());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetPrincipal()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.principal, typedOther.principal);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTableName()).compareTo(typedOther.isSetTableName());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTableName()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tableName, typedOther.tableName);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetPermission()).compareTo(typedOther.isSetPermission());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetPermission()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.permission, typedOther.permission);
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
            StringBuilder sb = new StringBuilder("revokeTablePermission_args(");
            boolean first = true;
            sb.append("tinfo:");
            if (this.tinfo == null) {
                sb.append("null");
            } else {
                sb.append(this.tinfo);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("credential:");
            if (this.credential == null) {
                sb.append("null");
            } else {
                sb.append(this.credential);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("principal:");
            if (this.principal == null) {
                sb.append("null");
            } else {
                sb.append(this.principal);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("tableName:");
            if (this.tableName == null) {
                sb.append("null");
            } else {
                sb.append(this.tableName);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("permission:");
            sb.append(this.permission);
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credential != null) {
                credential.validate();
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

        private static class revokeTablePermission_argsStandardSchemeFactory implements SchemeFactory {

            public revokeTablePermission_argsStandardScheme getScheme() {
                return new revokeTablePermission_argsStandardScheme();
            }
        }

        private static class revokeTablePermission_argsStandardScheme extends StandardScheme<revokeTablePermission_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, revokeTablePermission_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 5:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                                struct.tinfo.read(iprot);
                                struct.setTinfoIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                                struct.credential.read(iprot);
                                struct.setCredentialIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.principal = iprot.readString();
                                struct.setPrincipalIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.tableName = iprot.readString();
                                struct.setTableNameIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 4:
                            if (schemeField.type == org.apache.thrift.protocol.TType.BYTE) {
                                struct.permission = iprot.readByte();
                                struct.setPermissionIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, revokeTablePermission_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credential != null) {
                    oprot.writeFieldBegin(CREDENTIAL_FIELD_DESC);
                    struct.credential.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.principal != null) {
                    oprot.writeFieldBegin(PRINCIPAL_FIELD_DESC);
                    oprot.writeString(struct.principal);
                    oprot.writeFieldEnd();
                }
                if (struct.tableName != null) {
                    oprot.writeFieldBegin(TABLE_NAME_FIELD_DESC);
                    oprot.writeString(struct.tableName);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldBegin(PERMISSION_FIELD_DESC);
                oprot.writeByte(struct.permission);
                oprot.writeFieldEnd();
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class revokeTablePermission_argsTupleSchemeFactory implements SchemeFactory {

            public revokeTablePermission_argsTupleScheme getScheme() {
                return new revokeTablePermission_argsTupleScheme();
            }
        }

        private static class revokeTablePermission_argsTupleScheme extends TupleScheme<revokeTablePermission_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, revokeTablePermission_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredential()) {
                    optionals.set(1);
                }
                if (struct.isSetPrincipal()) {
                    optionals.set(2);
                }
                if (struct.isSetTableName()) {
                    optionals.set(3);
                }
                if (struct.isSetPermission()) {
                    optionals.set(4);
                }
                oprot.writeBitSet(optionals, 5);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredential()) {
                    struct.credential.write(oprot);
                }
                if (struct.isSetPrincipal()) {
                    oprot.writeString(struct.principal);
                }
                if (struct.isSetTableName()) {
                    oprot.writeString(struct.tableName);
                }
                if (struct.isSetPermission()) {
                    oprot.writeByte(struct.permission);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, revokeTablePermission_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(5);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credential = new org.apache.accumulo.core.security.thrift.Credential();
                    struct.credential.read(iprot);
                    struct.setCredentialIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.principal = iprot.readString();
                    struct.setPrincipalIsSet(true);
                }
                if (incoming.get(3)) {
                    struct.tableName = iprot.readString();
                    struct.setTableNameIsSet(true);
                }
                if (incoming.get(4)) {
                    struct.permission = iprot.readByte();
                    struct.setPermissionIsSet(true);
                }
            }
        }
    }

    public static class revokeTablePermission_result implements org.apache.thrift.TBase<revokeTablePermission_result, revokeTablePermission_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("revokeTablePermission_result");

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField TOPE_FIELD_DESC = new org.apache.thrift.protocol.TField("tope", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new revokeTablePermission_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new revokeTablePermission_resultTupleSchemeFactory());
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException sec;

        public ThriftTableOperationException tope;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SEC((short) 1, "sec"), TOPE((short) 2, "tope");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 1:
                        return SEC;
                    case 2:
                        return TOPE;
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
            tmpMap.put(_Fields.SEC, new org.apache.thrift.meta_data.FieldMetaData("sec", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            tmpMap.put(_Fields.TOPE, new org.apache.thrift.meta_data.FieldMetaData("tope", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(revokeTablePermission_result.class, metaDataMap);
        }

        public revokeTablePermission_result() {
        }

        public revokeTablePermission_result(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec, ThriftTableOperationException tope) {
            this();
            this.sec = sec;
            this.tope = tope;
        }

        public revokeTablePermission_result(revokeTablePermission_result other) {
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException(other.sec);
            }
            if (other.isSetTope()) {
                this.tope = new ThriftTableOperationException(other.tope);
            }
        }

        public revokeTablePermission_result deepCopy() {
            return new revokeTablePermission_result(this);
        }

        @Override
        public void clear() {
            this.sec = null;
            this.tope = null;
        }

        public org.apache.accumulo.core.security.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public revokeTablePermission_result setSec(org.apache.accumulo.core.security.thrift.ThriftSecurityException sec) {
            this.sec = sec;
            return this;
        }

        public void unsetSec() {
            this.sec = null;
        }

        public boolean isSetSec() {
            return this.sec != null;
        }

        public void setSecIsSet(boolean value) {
            if (!value) {
                this.sec = null;
            }
        }

        public ThriftTableOperationException getTope() {
            return this.tope;
        }

        public revokeTablePermission_result setTope(ThriftTableOperationException tope) {
            this.tope = tope;
            return this;
        }

        public void unsetTope() {
            this.tope = null;
        }

        public boolean isSetTope() {
            return this.tope != null;
        }

        public void setTopeIsSet(boolean value) {
            if (!value) {
                this.tope = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SEC:
                    if (value == null) {
                        unsetSec();
                    } else {
                        setSec((org.apache.accumulo.core.security.thrift.ThriftSecurityException) value);
                    }
                    break;
                case TOPE:
                    if (value == null) {
                        unsetTope();
                    } else {
                        setTope((ThriftTableOperationException) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SEC:
                    return getSec();
                case TOPE:
                    return getTope();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SEC:
                    return isSetSec();
                case TOPE:
                    return isSetTope();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof revokeTablePermission_result)
                return this.equals((revokeTablePermission_result) that);
            return false;
        }

        public boolean equals(revokeTablePermission_result that) {
            if (that == null)
                return false;
            boolean this_present_sec = true && this.isSetSec();
            boolean that_present_sec = true && that.isSetSec();
            if (this_present_sec || that_present_sec) {
                if (!(this_present_sec && that_present_sec))
                    return false;
                if (!this.sec.equals(that.sec))
                    return false;
            }
            boolean this_present_tope = true && this.isSetTope();
            boolean that_present_tope = true && that.isSetTope();
            if (this_present_tope || that_present_tope) {
                if (!(this_present_tope && that_present_tope))
                    return false;
                if (!this.tope.equals(that.tope))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(revokeTablePermission_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            revokeTablePermission_result typedOther = (revokeTablePermission_result) other;
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(typedOther.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, typedOther.sec);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTope()).compareTo(typedOther.isSetTope());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTope()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tope, typedOther.tope);
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
            StringBuilder sb = new StringBuilder("revokeTablePermission_result(");
            boolean first = true;
            sb.append("sec:");
            if (this.sec == null) {
                sb.append("null");
            } else {
                sb.append(this.sec);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("tope:");
            if (this.tope == null) {
                sb.append("null");
            } else {
                sb.append(this.tope);
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

        private static class revokeTablePermission_resultStandardSchemeFactory implements SchemeFactory {

            public revokeTablePermission_resultStandardScheme getScheme() {
                return new revokeTablePermission_resultStandardScheme();
            }
        }

        private static class revokeTablePermission_resultStandardScheme extends StandardScheme<revokeTablePermission_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, revokeTablePermission_result struct) throws org.apache.thrift.TException {
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
                                struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tope = new ThriftTableOperationException();
                                struct.tope.read(iprot);
                                struct.setTopeIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, revokeTablePermission_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.sec != null) {
                    oprot.writeFieldBegin(SEC_FIELD_DESC);
                    struct.sec.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.tope != null) {
                    oprot.writeFieldBegin(TOPE_FIELD_DESC);
                    struct.tope.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class revokeTablePermission_resultTupleSchemeFactory implements SchemeFactory {

            public revokeTablePermission_resultTupleScheme getScheme() {
                return new revokeTablePermission_resultTupleScheme();
            }
        }

        private static class revokeTablePermission_resultTupleScheme extends TupleScheme<revokeTablePermission_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, revokeTablePermission_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSec()) {
                    optionals.set(0);
                }
                if (struct.isSetTope()) {
                    optionals.set(1);
                }
                oprot.writeBitSet(optionals, 2);
                if (struct.isSetSec()) {
                    struct.sec.write(oprot);
                }
                if (struct.isSetTope()) {
                    struct.tope.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, revokeTablePermission_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    struct.sec = new org.apache.accumulo.core.security.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.tope = new ThriftTableOperationException();
                    struct.tope.read(iprot);
                    struct.setTopeIsSet(true);
                }
            }
        }
    }

    public static class getConfiguration_args implements org.apache.thrift.TBase<getConfiguration_args, getConfiguration_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("getConfiguration_args");

        private static final org.apache.thrift.protocol.TField TYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("type", org.apache.thrift.protocol.TType.I32, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new getConfiguration_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new getConfiguration_argsTupleSchemeFactory());
        }

        public ConfigurationType type;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TYPE((short) 1, "type");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 1:
                        return TYPE;
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
            tmpMap.put(_Fields.TYPE, new org.apache.thrift.meta_data.FieldMetaData("type", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, ConfigurationType.class)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(getConfiguration_args.class, metaDataMap);
        }

        public getConfiguration_args() {
        }

        public getConfiguration_args(ConfigurationType type) {
            this();
            this.type = type;
        }

        public getConfiguration_args(getConfiguration_args other) {
            if (other.isSetType()) {
                this.type = other.type;
            }
        }

        public getConfiguration_args deepCopy() {
            return new getConfiguration_args(this);
        }

        @Override
        public void clear() {
            this.type = null;
        }

        public ConfigurationType getType() {
            return this.type;
        }

        public getConfiguration_args setType(ConfigurationType type) {
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

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case TYPE:
                    if (value == null) {
                        unsetType();
                    } else {
                        setType((ConfigurationType) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TYPE:
                    return getType();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case TYPE:
                    return isSetType();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof getConfiguration_args)
                return this.equals((getConfiguration_args) that);
            return false;
        }

        public boolean equals(getConfiguration_args that) {
            if (that == null)
                return false;
            boolean this_present_type = true && this.isSetType();
            boolean that_present_type = true && that.isSetType();
            if (this_present_type || that_present_type) {
                if (!(this_present_type && that_present_type))
                    return false;
                if (!this.type.equals(that.type))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(getConfiguration_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            getConfiguration_args typedOther = (getConfiguration_args) other;
            lastComparison = Boolean.valueOf(isSetType()).compareTo(typedOther.isSetType());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetType()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.type, typedOther.type);
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
            StringBuilder sb = new StringBuilder("getConfiguration_args(");
            boolean first = true;
            sb.append("type:");
            if (this.type == null) {
                sb.append("null");
            } else {
                sb.append(this.type);
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

        private static class getConfiguration_argsStandardSchemeFactory implements SchemeFactory {

            public getConfiguration_argsStandardScheme getScheme() {
                return new getConfiguration_argsStandardScheme();
            }
        }

        private static class getConfiguration_argsStandardScheme extends StandardScheme<getConfiguration_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, getConfiguration_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                                struct.type = ConfigurationType.findByValue(iprot.readI32());
                                struct.setTypeIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, getConfiguration_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.type != null) {
                    oprot.writeFieldBegin(TYPE_FIELD_DESC);
                    oprot.writeI32(struct.type.getValue());
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class getConfiguration_argsTupleSchemeFactory implements SchemeFactory {

            public getConfiguration_argsTupleScheme getScheme() {
                return new getConfiguration_argsTupleScheme();
            }
        }

        private static class getConfiguration_argsTupleScheme extends TupleScheme<getConfiguration_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, getConfiguration_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetType()) {
                    optionals.set(0);
                }
                oprot.writeBitSet(optionals, 1);
                if (struct.isSetType()) {
                    oprot.writeI32(struct.type.getValue());
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, getConfiguration_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.type = ConfigurationType.findByValue(iprot.readI32());
                    struct.setTypeIsSet(true);
                }
            }
        }
    }

    public static class getConfiguration_result implements org.apache.thrift.TBase<getConfiguration_result, getConfiguration_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("getConfiguration_result");

        private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.MAP, (short) 0);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new getConfiguration_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new getConfiguration_resultTupleSchemeFactory());
        }

        public Map<String, String> success;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SUCCESS((short) 0, "success");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 0:
                        return SUCCESS;
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
            tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(getConfiguration_result.class, metaDataMap);
        }

        public getConfiguration_result() {
        }

        public getConfiguration_result(Map<String, String> success) {
            this();
            this.success = success;
        }

        public getConfiguration_result(getConfiguration_result other) {
            if (other.isSetSuccess()) {
                Map<String, String> __this__success = new HashMap<String, String>();
                for (Map.Entry<String, String> other_element : other.success.entrySet()) {
                    String other_element_key = other_element.getKey();
                    String other_element_value = other_element.getValue();
                    String __this__success_copy_key = other_element_key;
                    String __this__success_copy_value = other_element_value;
                    __this__success.put(__this__success_copy_key, __this__success_copy_value);
                }
                this.success = __this__success;
            }
        }

        public getConfiguration_result deepCopy() {
            return new getConfiguration_result(this);
        }

        @Override
        public void clear() {
            this.success = null;
        }

        public int getSuccessSize() {
            return (this.success == null) ? 0 : this.success.size();
        }

        public void putToSuccess(String key, String val) {
            if (this.success == null) {
                this.success = new HashMap<String, String>();
            }
            this.success.put(key, val);
        }

        public Map<String, String> getSuccess() {
            return this.success;
        }

        public getConfiguration_result setSuccess(Map<String, String> success) {
            this.success = success;
            return this;
        }

        public void unsetSuccess() {
            this.success = null;
        }

        public boolean isSetSuccess() {
            return this.success != null;
        }

        public void setSuccessIsSet(boolean value) {
            if (!value) {
                this.success = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SUCCESS:
                    if (value == null) {
                        unsetSuccess();
                    } else {
                        setSuccess((Map<String, String>) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SUCCESS:
                    return getSuccess();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SUCCESS:
                    return isSetSuccess();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof getConfiguration_result)
                return this.equals((getConfiguration_result) that);
            return false;
        }

        public boolean equals(getConfiguration_result that) {
            if (that == null)
                return false;
            boolean this_present_success = true && this.isSetSuccess();
            boolean that_present_success = true && that.isSetSuccess();
            if (this_present_success || that_present_success) {
                if (!(this_present_success && that_present_success))
                    return false;
                if (!this.success.equals(that.success))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(getConfiguration_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            getConfiguration_result typedOther = (getConfiguration_result) other;
            lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSuccess()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, typedOther.success);
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
            StringBuilder sb = new StringBuilder("getConfiguration_result(");
            boolean first = true;
            sb.append("success:");
            if (this.success == null) {
                sb.append("null");
            } else {
                sb.append(this.success);
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

        private static class getConfiguration_resultStandardSchemeFactory implements SchemeFactory {

            public getConfiguration_resultStandardScheme getScheme() {
                return new getConfiguration_resultStandardScheme();
            }
        }

        private static class getConfiguration_resultStandardScheme extends StandardScheme<getConfiguration_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, getConfiguration_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 0:
                            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
                                {
                                    org.apache.thrift.protocol.TMap _map48 = iprot.readMapBegin();
                                    struct.success = new HashMap<String, String>(2 * _map48.size);
                                    for (int _i49 = 0; _i49 < _map48.size; ++_i49) {
                                        String _key50;
                                        String _val51;
                                        _key50 = iprot.readString();
                                        _val51 = iprot.readString();
                                        struct.success.put(_key50, _val51);
                                    }
                                    iprot.readMapEnd();
                                }
                                struct.setSuccessIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, getConfiguration_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.success != null) {
                    oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
                    {
                        oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, struct.success.size()));
                        for (Map.Entry<String, String> _iter52 : struct.success.entrySet()) {
                            oprot.writeString(_iter52.getKey());
                            oprot.writeString(_iter52.getValue());
                        }
                        oprot.writeMapEnd();
                    }
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class getConfiguration_resultTupleSchemeFactory implements SchemeFactory {

            public getConfiguration_resultTupleScheme getScheme() {
                return new getConfiguration_resultTupleScheme();
            }
        }

        private static class getConfiguration_resultTupleScheme extends TupleScheme<getConfiguration_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, getConfiguration_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSuccess()) {
                    optionals.set(0);
                }
                oprot.writeBitSet(optionals, 1);
                if (struct.isSetSuccess()) {
                    {
                        oprot.writeI32(struct.success.size());
                        for (Map.Entry<String, String> _iter53 : struct.success.entrySet()) {
                            oprot.writeString(_iter53.getKey());
                            oprot.writeString(_iter53.getValue());
                        }
                    }
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, getConfiguration_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    {
                        org.apache.thrift.protocol.TMap _map54 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                        struct.success = new HashMap<String, String>(2 * _map54.size);
                        for (int _i55 = 0; _i55 < _map54.size; ++_i55) {
                            String _key56;
                            String _val57;
                            _key56 = iprot.readString();
                            _val57 = iprot.readString();
                            struct.success.put(_key56, _val57);
                        }
                    }
                    struct.setSuccessIsSet(true);
                }
            }
        }
    }

    public static class getTableConfiguration_args implements org.apache.thrift.TBase<getTableConfiguration_args, getTableConfiguration_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("getTableConfiguration_args");

        private static final org.apache.thrift.protocol.TField TABLE_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("tableName", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new getTableConfiguration_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new getTableConfiguration_argsTupleSchemeFactory());
        }

        public String tableName;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TABLE_NAME((short) 2, "tableName");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 2:
                        return TABLE_NAME;
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
            tmpMap.put(_Fields.TABLE_NAME, new org.apache.thrift.meta_data.FieldMetaData("tableName", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(getTableConfiguration_args.class, metaDataMap);
        }

        public getTableConfiguration_args() {
        }

        public getTableConfiguration_args(String tableName) {
            this();
            this.tableName = tableName;
        }

        public getTableConfiguration_args(getTableConfiguration_args other) {
            if (other.isSetTableName()) {
                this.tableName = other.tableName;
            }
        }

        public getTableConfiguration_args deepCopy() {
            return new getTableConfiguration_args(this);
        }

        @Override
        public void clear() {
            this.tableName = null;
        }

        public String getTableName() {
            return this.tableName;
        }

        public getTableConfiguration_args setTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public void unsetTableName() {
            this.tableName = null;
        }

        public boolean isSetTableName() {
            return this.tableName != null;
        }

        public void setTableNameIsSet(boolean value) {
            if (!value) {
                this.tableName = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case TABLE_NAME:
                    if (value == null) {
                        unsetTableName();
                    } else {
                        setTableName((String) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TABLE_NAME:
                    return getTableName();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case TABLE_NAME:
                    return isSetTableName();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof getTableConfiguration_args)
                return this.equals((getTableConfiguration_args) that);
            return false;
        }

        public boolean equals(getTableConfiguration_args that) {
            if (that == null)
                return false;
            boolean this_present_tableName = true && this.isSetTableName();
            boolean that_present_tableName = true && that.isSetTableName();
            if (this_present_tableName || that_present_tableName) {
                if (!(this_present_tableName && that_present_tableName))
                    return false;
                if (!this.tableName.equals(that.tableName))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(getTableConfiguration_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            getTableConfiguration_args typedOther = (getTableConfiguration_args) other;
            lastComparison = Boolean.valueOf(isSetTableName()).compareTo(typedOther.isSetTableName());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTableName()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tableName, typedOther.tableName);
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
            StringBuilder sb = new StringBuilder("getTableConfiguration_args(");
            boolean first = true;
            sb.append("tableName:");
            if (this.tableName == null) {
                sb.append("null");
            } else {
                sb.append(this.tableName);
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

        private static class getTableConfiguration_argsStandardSchemeFactory implements SchemeFactory {

            public getTableConfiguration_argsStandardScheme getScheme() {
                return new getTableConfiguration_argsStandardScheme();
            }
        }

        private static class getTableConfiguration_argsStandardScheme extends StandardScheme<getTableConfiguration_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, getTableConfiguration_args struct) throws org.apache.thrift.TException {
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
                                struct.tableName = iprot.readString();
                                struct.setTableNameIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, getTableConfiguration_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.tableName != null) {
                    oprot.writeFieldBegin(TABLE_NAME_FIELD_DESC);
                    oprot.writeString(struct.tableName);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class getTableConfiguration_argsTupleSchemeFactory implements SchemeFactory {

            public getTableConfiguration_argsTupleScheme getScheme() {
                return new getTableConfiguration_argsTupleScheme();
            }
        }

        private static class getTableConfiguration_argsTupleScheme extends TupleScheme<getTableConfiguration_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, getTableConfiguration_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTableName()) {
                    optionals.set(0);
                }
                oprot.writeBitSet(optionals, 1);
                if (struct.isSetTableName()) {
                    oprot.writeString(struct.tableName);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, getTableConfiguration_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.tableName = iprot.readString();
                    struct.setTableNameIsSet(true);
                }
            }
        }
    }

    public static class getTableConfiguration_result implements org.apache.thrift.TBase<getTableConfiguration_result, getTableConfiguration_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("getTableConfiguration_result");

        private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.MAP, (short) 0);

        private static final org.apache.thrift.protocol.TField TOPE_FIELD_DESC = new org.apache.thrift.protocol.TField("tope", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new getTableConfiguration_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new getTableConfiguration_resultTupleSchemeFactory());
        }

        public Map<String, String> success;

        public ThriftTableOperationException tope;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SUCCESS((short) 0, "success"), TOPE((short) 1, "tope");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 0:
                        return SUCCESS;
                    case 1:
                        return TOPE;
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
            tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
            tmpMap.put(_Fields.TOPE, new org.apache.thrift.meta_data.FieldMetaData("tope", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(getTableConfiguration_result.class, metaDataMap);
        }

        public getTableConfiguration_result() {
        }

        public getTableConfiguration_result(Map<String, String> success, ThriftTableOperationException tope) {
            this();
            this.success = success;
            this.tope = tope;
        }

        public getTableConfiguration_result(getTableConfiguration_result other) {
            if (other.isSetSuccess()) {
                Map<String, String> __this__success = new HashMap<String, String>();
                for (Map.Entry<String, String> other_element : other.success.entrySet()) {
                    String other_element_key = other_element.getKey();
                    String other_element_value = other_element.getValue();
                    String __this__success_copy_key = other_element_key;
                    String __this__success_copy_value = other_element_value;
                    __this__success.put(__this__success_copy_key, __this__success_copy_value);
                }
                this.success = __this__success;
            }
            if (other.isSetTope()) {
                this.tope = new ThriftTableOperationException(other.tope);
            }
        }

        public getTableConfiguration_result deepCopy() {
            return new getTableConfiguration_result(this);
        }

        @Override
        public void clear() {
            this.success = null;
            this.tope = null;
        }

        public int getSuccessSize() {
            return (this.success == null) ? 0 : this.success.size();
        }

        public void putToSuccess(String key, String val) {
            if (this.success == null) {
                this.success = new HashMap<String, String>();
            }
            this.success.put(key, val);
        }

        public Map<String, String> getSuccess() {
            return this.success;
        }

        public getTableConfiguration_result setSuccess(Map<String, String> success) {
            this.success = success;
            return this;
        }

        public void unsetSuccess() {
            this.success = null;
        }

        public boolean isSetSuccess() {
            return this.success != null;
        }

        public void setSuccessIsSet(boolean value) {
            if (!value) {
                this.success = null;
            }
        }

        public ThriftTableOperationException getTope() {
            return this.tope;
        }

        public getTableConfiguration_result setTope(ThriftTableOperationException tope) {
            this.tope = tope;
            return this;
        }

        public void unsetTope() {
            this.tope = null;
        }

        public boolean isSetTope() {
            return this.tope != null;
        }

        public void setTopeIsSet(boolean value) {
            if (!value) {
                this.tope = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SUCCESS:
                    if (value == null) {
                        unsetSuccess();
                    } else {
                        setSuccess((Map<String, String>) value);
                    }
                    break;
                case TOPE:
                    if (value == null) {
                        unsetTope();
                    } else {
                        setTope((ThriftTableOperationException) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SUCCESS:
                    return getSuccess();
                case TOPE:
                    return getTope();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SUCCESS:
                    return isSetSuccess();
                case TOPE:
                    return isSetTope();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof getTableConfiguration_result)
                return this.equals((getTableConfiguration_result) that);
            return false;
        }

        public boolean equals(getTableConfiguration_result that) {
            if (that == null)
                return false;
            boolean this_present_success = true && this.isSetSuccess();
            boolean that_present_success = true && that.isSetSuccess();
            if (this_present_success || that_present_success) {
                if (!(this_present_success && that_present_success))
                    return false;
                if (!this.success.equals(that.success))
                    return false;
            }
            boolean this_present_tope = true && this.isSetTope();
            boolean that_present_tope = true && that.isSetTope();
            if (this_present_tope || that_present_tope) {
                if (!(this_present_tope && that_present_tope))
                    return false;
                if (!this.tope.equals(that.tope))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(getTableConfiguration_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            getTableConfiguration_result typedOther = (getTableConfiguration_result) other;
            lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSuccess()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, typedOther.success);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTope()).compareTo(typedOther.isSetTope());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTope()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tope, typedOther.tope);
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
            StringBuilder sb = new StringBuilder("getTableConfiguration_result(");
            boolean first = true;
            sb.append("success:");
            if (this.success == null) {
                sb.append("null");
            } else {
                sb.append(this.success);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("tope:");
            if (this.tope == null) {
                sb.append("null");
            } else {
                sb.append(this.tope);
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

        private static class getTableConfiguration_resultStandardSchemeFactory implements SchemeFactory {

            public getTableConfiguration_resultStandardScheme getScheme() {
                return new getTableConfiguration_resultStandardScheme();
            }
        }

        private static class getTableConfiguration_resultStandardScheme extends StandardScheme<getTableConfiguration_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, getTableConfiguration_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 0:
                            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
                                {
                                    org.apache.thrift.protocol.TMap _map58 = iprot.readMapBegin();
                                    struct.success = new HashMap<String, String>(2 * _map58.size);
                                    for (int _i59 = 0; _i59 < _map58.size; ++_i59) {
                                        String _key60;
                                        String _val61;
                                        _key60 = iprot.readString();
                                        _val61 = iprot.readString();
                                        struct.success.put(_key60, _val61);
                                    }
                                    iprot.readMapEnd();
                                }
                                struct.setSuccessIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tope = new ThriftTableOperationException();
                                struct.tope.read(iprot);
                                struct.setTopeIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, getTableConfiguration_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.success != null) {
                    oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
                    {
                        oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, struct.success.size()));
                        for (Map.Entry<String, String> _iter62 : struct.success.entrySet()) {
                            oprot.writeString(_iter62.getKey());
                            oprot.writeString(_iter62.getValue());
                        }
                        oprot.writeMapEnd();
                    }
                    oprot.writeFieldEnd();
                }
                if (struct.tope != null) {
                    oprot.writeFieldBegin(TOPE_FIELD_DESC);
                    struct.tope.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class getTableConfiguration_resultTupleSchemeFactory implements SchemeFactory {

            public getTableConfiguration_resultTupleScheme getScheme() {
                return new getTableConfiguration_resultTupleScheme();
            }
        }

        private static class getTableConfiguration_resultTupleScheme extends TupleScheme<getTableConfiguration_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, getTableConfiguration_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSuccess()) {
                    optionals.set(0);
                }
                if (struct.isSetTope()) {
                    optionals.set(1);
                }
                oprot.writeBitSet(optionals, 2);
                if (struct.isSetSuccess()) {
                    {
                        oprot.writeI32(struct.success.size());
                        for (Map.Entry<String, String> _iter63 : struct.success.entrySet()) {
                            oprot.writeString(_iter63.getKey());
                            oprot.writeString(_iter63.getValue());
                        }
                    }
                }
                if (struct.isSetTope()) {
                    struct.tope.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, getTableConfiguration_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    {
                        org.apache.thrift.protocol.TMap _map64 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                        struct.success = new HashMap<String, String>(2 * _map64.size);
                        for (int _i65 = 0; _i65 < _map64.size; ++_i65) {
                            String _key66;
                            String _val67;
                            _key66 = iprot.readString();
                            _val67 = iprot.readString();
                            struct.success.put(_key66, _val67);
                        }
                    }
                    struct.setSuccessIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.tope = new ThriftTableOperationException();
                    struct.tope.read(iprot);
                    struct.setTopeIsSet(true);
                }
            }
        }
    }

    public static class checkClass_args implements org.apache.thrift.TBase<checkClass_args, checkClass_args._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("checkClass_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField CLASS_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("className", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final org.apache.thrift.protocol.TField INTERFACE_MATCH_FIELD_DESC = new org.apache.thrift.protocol.TField("interfaceMatch", org.apache.thrift.protocol.TType.STRING, (short) 3);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new checkClass_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new checkClass_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public String className;

        public String interfaceMatch;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 1, "tinfo"), CLASS_NAME((short) 2, "className"), INTERFACE_MATCH((short) 3, "interfaceMatch");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 1:
                        return TINFO;
                    case 2:
                        return CLASS_NAME;
                    case 3:
                        return INTERFACE_MATCH;
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
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.CLASS_NAME, new org.apache.thrift.meta_data.FieldMetaData("className", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.INTERFACE_MATCH, new org.apache.thrift.meta_data.FieldMetaData("interfaceMatch", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(checkClass_args.class, metaDataMap);
        }

        public checkClass_args() {
        }

        public checkClass_args(org.apache.accumulo.trace.thrift.TInfo tinfo, String className, String interfaceMatch) {
            this();
            this.tinfo = tinfo;
            this.className = className;
            this.interfaceMatch = interfaceMatch;
        }

        public checkClass_args(checkClass_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetClassName()) {
                this.className = other.className;
            }
            if (other.isSetInterfaceMatch()) {
                this.interfaceMatch = other.interfaceMatch;
            }
        }

        public checkClass_args deepCopy() {
            return new checkClass_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.className = null;
            this.interfaceMatch = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public checkClass_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
            this.tinfo = tinfo;
            return this;
        }

        public void unsetTinfo() {
            this.tinfo = null;
        }

        public boolean isSetTinfo() {
            return this.tinfo != null;
        }

        public void setTinfoIsSet(boolean value) {
            if (!value) {
                this.tinfo = null;
            }
        }

        public String getClassName() {
            return this.className;
        }

        public checkClass_args setClassName(String className) {
            this.className = className;
            return this;
        }

        public void unsetClassName() {
            this.className = null;
        }

        public boolean isSetClassName() {
            return this.className != null;
        }

        public void setClassNameIsSet(boolean value) {
            if (!value) {
                this.className = null;
            }
        }

        public String getInterfaceMatch() {
            return this.interfaceMatch;
        }

        public checkClass_args setInterfaceMatch(String interfaceMatch) {
            this.interfaceMatch = interfaceMatch;
            return this;
        }

        public void unsetInterfaceMatch() {
            this.interfaceMatch = null;
        }

        public boolean isSetInterfaceMatch() {
            return this.interfaceMatch != null;
        }

        public void setInterfaceMatchIsSet(boolean value) {
            if (!value) {
                this.interfaceMatch = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
                        setTinfo((org.apache.accumulo.trace.thrift.TInfo) value);
                    }
                    break;
                case CLASS_NAME:
                    if (value == null) {
                        unsetClassName();
                    } else {
                        setClassName((String) value);
                    }
                    break;
                case INTERFACE_MATCH:
                    if (value == null) {
                        unsetInterfaceMatch();
                    } else {
                        setInterfaceMatch((String) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CLASS_NAME:
                    return getClassName();
                case INTERFACE_MATCH:
                    return getInterfaceMatch();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case TINFO:
                    return isSetTinfo();
                case CLASS_NAME:
                    return isSetClassName();
                case INTERFACE_MATCH:
                    return isSetInterfaceMatch();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof checkClass_args)
                return this.equals((checkClass_args) that);
            return false;
        }

        public boolean equals(checkClass_args that) {
            if (that == null)
                return false;
            boolean this_present_tinfo = true && this.isSetTinfo();
            boolean that_present_tinfo = true && that.isSetTinfo();
            if (this_present_tinfo || that_present_tinfo) {
                if (!(this_present_tinfo && that_present_tinfo))
                    return false;
                if (!this.tinfo.equals(that.tinfo))
                    return false;
            }
            boolean this_present_className = true && this.isSetClassName();
            boolean that_present_className = true && that.isSetClassName();
            if (this_present_className || that_present_className) {
                if (!(this_present_className && that_present_className))
                    return false;
                if (!this.className.equals(that.className))
                    return false;
            }
            boolean this_present_interfaceMatch = true && this.isSetInterfaceMatch();
            boolean that_present_interfaceMatch = true && that.isSetInterfaceMatch();
            if (this_present_interfaceMatch || that_present_interfaceMatch) {
                if (!(this_present_interfaceMatch && that_present_interfaceMatch))
                    return false;
                if (!this.interfaceMatch.equals(that.interfaceMatch))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(checkClass_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            checkClass_args typedOther = (checkClass_args) other;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(typedOther.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, typedOther.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetClassName()).compareTo(typedOther.isSetClassName());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetClassName()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.className, typedOther.className);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetInterfaceMatch()).compareTo(typedOther.isSetInterfaceMatch());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetInterfaceMatch()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.interfaceMatch, typedOther.interfaceMatch);
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
            StringBuilder sb = new StringBuilder("checkClass_args(");
            boolean first = true;
            sb.append("tinfo:");
            if (this.tinfo == null) {
                sb.append("null");
            } else {
                sb.append(this.tinfo);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("className:");
            if (this.className == null) {
                sb.append("null");
            } else {
                sb.append(this.className);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("interfaceMatch:");
            if (this.interfaceMatch == null) {
                sb.append("null");
            } else {
                sb.append(this.interfaceMatch);
            }
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
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
                read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
            } catch (org.apache.thrift.TException te) {
                throw new java.io.IOException(te);
            }
        }

        private static class checkClass_argsStandardSchemeFactory implements SchemeFactory {

            public checkClass_argsStandardScheme getScheme() {
                return new checkClass_argsStandardScheme();
            }
        }

        private static class checkClass_argsStandardScheme extends StandardScheme<checkClass_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, checkClass_args struct) throws org.apache.thrift.TException {
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
                                struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                                struct.tinfo.read(iprot);
                                struct.setTinfoIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.className = iprot.readString();
                                struct.setClassNameIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.interfaceMatch = iprot.readString();
                                struct.setInterfaceMatchIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, checkClass_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.className != null) {
                    oprot.writeFieldBegin(CLASS_NAME_FIELD_DESC);
                    oprot.writeString(struct.className);
                    oprot.writeFieldEnd();
                }
                if (struct.interfaceMatch != null) {
                    oprot.writeFieldBegin(INTERFACE_MATCH_FIELD_DESC);
                    oprot.writeString(struct.interfaceMatch);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class checkClass_argsTupleSchemeFactory implements SchemeFactory {

            public checkClass_argsTupleScheme getScheme() {
                return new checkClass_argsTupleScheme();
            }
        }

        private static class checkClass_argsTupleScheme extends TupleScheme<checkClass_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, checkClass_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetClassName()) {
                    optionals.set(1);
                }
                if (struct.isSetInterfaceMatch()) {
                    optionals.set(2);
                }
                oprot.writeBitSet(optionals, 3);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetClassName()) {
                    oprot.writeString(struct.className);
                }
                if (struct.isSetInterfaceMatch()) {
                    oprot.writeString(struct.interfaceMatch);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, checkClass_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(3);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.className = iprot.readString();
                    struct.setClassNameIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.interfaceMatch = iprot.readString();
                    struct.setInterfaceMatchIsSet(true);
                }
            }
        }
    }

    public static class checkClass_result implements org.apache.thrift.TBase<checkClass_result, checkClass_result._Fields>, java.io.Serializable, Cloneable {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("checkClass_result");

        private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.BOOL, (short) 0);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new checkClass_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new checkClass_resultTupleSchemeFactory());
        }

        public boolean success;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SUCCESS((short) 0, "success");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 0:
                        return SUCCESS;
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

        private static final int __SUCCESS_ISSET_ID = 0;

        private byte __isset_bitfield = 0;

        public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(checkClass_result.class, metaDataMap);
        }

        public checkClass_result() {
        }

        public checkClass_result(boolean success) {
            this();
            this.success = success;
            setSuccessIsSet(true);
        }

        public checkClass_result(checkClass_result other) {
            __isset_bitfield = other.__isset_bitfield;
            this.success = other.success;
        }

        public checkClass_result deepCopy() {
            return new checkClass_result(this);
        }

        @Override
        public void clear() {
            setSuccessIsSet(false);
            this.success = false;
        }

        public boolean isSuccess() {
            return this.success;
        }

        public checkClass_result setSuccess(boolean success) {
            this.success = success;
            setSuccessIsSet(true);
            return this;
        }

        public void unsetSuccess() {
            __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __SUCCESS_ISSET_ID);
        }

        public boolean isSetSuccess() {
            return EncodingUtils.testBit(__isset_bitfield, __SUCCESS_ISSET_ID);
        }

        public void setSuccessIsSet(boolean value) {
            __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __SUCCESS_ISSET_ID, value);
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SUCCESS:
                    if (value == null) {
                        unsetSuccess();
                    } else {
                        setSuccess((Boolean) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SUCCESS:
                    return Boolean.valueOf(isSuccess());
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SUCCESS:
                    return isSetSuccess();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof checkClass_result)
                return this.equals((checkClass_result) that);
            return false;
        }

        public boolean equals(checkClass_result that) {
            if (that == null)
                return false;
            boolean this_present_success = true;
            boolean that_present_success = true;
            if (this_present_success || that_present_success) {
                if (!(this_present_success && that_present_success))
                    return false;
                if (this.success != that.success)
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public int compareTo(checkClass_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            checkClass_result typedOther = (checkClass_result) other;
            lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(typedOther.isSetSuccess());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSuccess()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, typedOther.success);
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
            StringBuilder sb = new StringBuilder("checkClass_result(");
            boolean first = true;
            sb.append("success:");
            sb.append(this.success);
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

        private static class checkClass_resultStandardSchemeFactory implements SchemeFactory {

            public checkClass_resultStandardScheme getScheme() {
                return new checkClass_resultStandardScheme();
            }
        }

        private static class checkClass_resultStandardScheme extends StandardScheme<checkClass_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, checkClass_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 0:
                            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
                                struct.success = iprot.readBool();
                                struct.setSuccessIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, checkClass_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.isSetSuccess()) {
                    oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
                    oprot.writeBool(struct.success);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class checkClass_resultTupleSchemeFactory implements SchemeFactory {

            public checkClass_resultTupleScheme getScheme() {
                return new checkClass_resultTupleScheme();
            }
        }

        private static class checkClass_resultTupleScheme extends TupleScheme<checkClass_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, checkClass_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSuccess()) {
                    optionals.set(0);
                }
                oprot.writeBitSet(optionals, 1);
                if (struct.isSetSuccess()) {
                    oprot.writeBool(struct.success);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, checkClass_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.success = iprot.readBool();
                    struct.setSuccessIsSet(true);
                }
            }
        }
    }
}
