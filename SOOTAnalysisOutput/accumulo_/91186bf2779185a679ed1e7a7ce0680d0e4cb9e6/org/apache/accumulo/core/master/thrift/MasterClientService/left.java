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

@SuppressWarnings("all")
public class MasterClientService {

    public interface Iface extends FateService.Iface {

        public long initiateFlush(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException;

        public void waitForFlush(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, ByteBuffer startRow, ByteBuffer endRow, long flushID, long maxLoops) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException;

        public void setTableProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, String property, String value) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException;

        public void removeTableProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, String property) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException;

        public void setNamespaceProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String ns, String property, String value) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException;

        public void removeNamespaceProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String ns, String property) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException;

        public void setMasterGoalState(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, MasterGoalState state) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException;

        public void shutdown(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, boolean stopTabletServers) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException;

        public void shutdownTabletServer(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tabletServer, boolean force) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException;

        public void setSystemProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String property, String value) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException;

        public void removeSystemProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String property) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException;

        public MasterMonitorInfo getMasterStats(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException;

        public void waitForBalance(org.apache.accumulo.trace.thrift.TInfo tinfo) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException;

        public void reportSplitExtent(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String serverName, TabletSplit split) throws org.apache.thrift.TException;

        public void reportTabletStatus(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String serverName, TabletLoadState status, org.apache.accumulo.core.data.thrift.TKeyExtent tablet) throws org.apache.thrift.TException;
    }

    public interface AsyncIface extends FateService.AsyncIface {

        public void initiateFlush(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;

        public void waitForFlush(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, ByteBuffer startRow, ByteBuffer endRow, long flushID, long maxLoops, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;

        public void setTableProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, String property, String value, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;

        public void removeTableProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, String property, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;

        public void setNamespaceProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String ns, String property, String value, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;

        public void removeNamespaceProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String ns, String property, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;

        public void setMasterGoalState(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, MasterGoalState state, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;

        public void shutdown(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, boolean stopTabletServers, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;

        public void shutdownTabletServer(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tabletServer, boolean force, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;

        public void setSystemProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String property, String value, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;

        public void removeSystemProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String property, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;

        public void getMasterStats(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;

        public void waitForBalance(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;

        public void reportSplitExtent(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String serverName, TabletSplit split, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;

        public void reportTabletStatus(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String serverName, TabletLoadState status, org.apache.accumulo.core.data.thrift.TKeyExtent tablet, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;
    }

    public static class Client extends FateService.Client implements Iface {

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

        public long initiateFlush(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException {
            send_initiateFlush(tinfo, credentials, tableName);
            return recv_initiateFlush();
        }

        public void send_initiateFlush(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName) throws org.apache.thrift.TException {
            initiateFlush_args args = new initiateFlush_args();
            args.setTinfo(tinfo);
            args.setCredentials(credentials);
            args.setTableName(tableName);
            sendBase("initiateFlush", args);
        }

        public long recv_initiateFlush() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException {
            initiateFlush_result result = new initiateFlush_result();
            receiveBase(result, "initiateFlush");
            if (result.isSetSuccess()) {
                return result.success;
            }
            if (result.sec != null) {
                throw result.sec;
            }
            if (result.tope != null) {
                throw result.tope;
            }
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "initiateFlush failed: unknown result");
        }

        public void waitForFlush(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, ByteBuffer startRow, ByteBuffer endRow, long flushID, long maxLoops) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException {
            send_waitForFlush(tinfo, credentials, tableName, startRow, endRow, flushID, maxLoops);
            recv_waitForFlush();
        }

        public void send_waitForFlush(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, ByteBuffer startRow, ByteBuffer endRow, long flushID, long maxLoops) throws org.apache.thrift.TException {
            waitForFlush_args args = new waitForFlush_args();
            args.setTinfo(tinfo);
            args.setCredentials(credentials);
            args.setTableName(tableName);
            args.setStartRow(startRow);
            args.setEndRow(endRow);
            args.setFlushID(flushID);
            args.setMaxLoops(maxLoops);
            sendBase("waitForFlush", args);
        }

        public void recv_waitForFlush() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException {
            waitForFlush_result result = new waitForFlush_result();
            receiveBase(result, "waitForFlush");
            if (result.sec != null) {
                throw result.sec;
            }
            if (result.tope != null) {
                throw result.tope;
            }
            return;
        }

        public void setTableProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, String property, String value) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException {
            send_setTableProperty(tinfo, credentials, tableName, property, value);
            recv_setTableProperty();
        }

        public void send_setTableProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, String property, String value) throws org.apache.thrift.TException {
            setTableProperty_args args = new setTableProperty_args();
            args.setTinfo(tinfo);
            args.setCredentials(credentials);
            args.setTableName(tableName);
            args.setProperty(property);
            args.setValue(value);
            sendBase("setTableProperty", args);
        }

        public void recv_setTableProperty() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException {
            setTableProperty_result result = new setTableProperty_result();
            receiveBase(result, "setTableProperty");
            if (result.sec != null) {
                throw result.sec;
            }
            if (result.tope != null) {
                throw result.tope;
            }
            return;
        }

        public void removeTableProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, String property) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException {
            send_removeTableProperty(tinfo, credentials, tableName, property);
            recv_removeTableProperty();
        }

        public void send_removeTableProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, String property) throws org.apache.thrift.TException {
            removeTableProperty_args args = new removeTableProperty_args();
            args.setTinfo(tinfo);
            args.setCredentials(credentials);
            args.setTableName(tableName);
            args.setProperty(property);
            sendBase("removeTableProperty", args);
        }

        public void recv_removeTableProperty() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException {
            removeTableProperty_result result = new removeTableProperty_result();
            receiveBase(result, "removeTableProperty");
            if (result.sec != null) {
                throw result.sec;
            }
            if (result.tope != null) {
                throw result.tope;
            }
            return;
        }

        public void setNamespaceProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String ns, String property, String value) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException {
            send_setNamespaceProperty(tinfo, credentials, ns, property, value);
            recv_setNamespaceProperty();
        }

        public void send_setNamespaceProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String ns, String property, String value) throws org.apache.thrift.TException {
            setNamespaceProperty_args args = new setNamespaceProperty_args();
            args.setTinfo(tinfo);
            args.setCredentials(credentials);
            args.setNs(ns);
            args.setProperty(property);
            args.setValue(value);
            sendBase("setNamespaceProperty", args);
        }

        public void recv_setNamespaceProperty() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException {
            setNamespaceProperty_result result = new setNamespaceProperty_result();
            receiveBase(result, "setNamespaceProperty");
            if (result.sec != null) {
                throw result.sec;
            }
            if (result.tope != null) {
                throw result.tope;
            }
            return;
        }

        public void removeNamespaceProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String ns, String property) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException {
            send_removeNamespaceProperty(tinfo, credentials, ns, property);
            recv_removeNamespaceProperty();
        }

        public void send_removeNamespaceProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String ns, String property) throws org.apache.thrift.TException {
            removeNamespaceProperty_args args = new removeNamespaceProperty_args();
            args.setTinfo(tinfo);
            args.setCredentials(credentials);
            args.setNs(ns);
            args.setProperty(property);
            sendBase("removeNamespaceProperty", args);
        }

        public void recv_removeNamespaceProperty() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException {
            removeNamespaceProperty_result result = new removeNamespaceProperty_result();
            receiveBase(result, "removeNamespaceProperty");
            if (result.sec != null) {
                throw result.sec;
            }
            if (result.tope != null) {
                throw result.tope;
            }
            return;
        }

        public void setMasterGoalState(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, MasterGoalState state) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
            send_setMasterGoalState(tinfo, credentials, state);
            recv_setMasterGoalState();
        }

        public void send_setMasterGoalState(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, MasterGoalState state) throws org.apache.thrift.TException {
            setMasterGoalState_args args = new setMasterGoalState_args();
            args.setTinfo(tinfo);
            args.setCredentials(credentials);
            args.setState(state);
            sendBase("setMasterGoalState", args);
        }

        public void recv_setMasterGoalState() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
            setMasterGoalState_result result = new setMasterGoalState_result();
            receiveBase(result, "setMasterGoalState");
            if (result.sec != null) {
                throw result.sec;
            }
            return;
        }

        public void shutdown(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, boolean stopTabletServers) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
            send_shutdown(tinfo, credentials, stopTabletServers);
            recv_shutdown();
        }

        public void send_shutdown(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, boolean stopTabletServers) throws org.apache.thrift.TException {
            shutdown_args args = new shutdown_args();
            args.setTinfo(tinfo);
            args.setCredentials(credentials);
            args.setStopTabletServers(stopTabletServers);
            sendBase("shutdown", args);
        }

        public void recv_shutdown() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
            shutdown_result result = new shutdown_result();
            receiveBase(result, "shutdown");
            if (result.sec != null) {
                throw result.sec;
            }
            return;
        }

        public void shutdownTabletServer(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tabletServer, boolean force) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
            send_shutdownTabletServer(tinfo, credentials, tabletServer, force);
            recv_shutdownTabletServer();
        }

        public void send_shutdownTabletServer(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tabletServer, boolean force) throws org.apache.thrift.TException {
            shutdownTabletServer_args args = new shutdownTabletServer_args();
            args.setTinfo(tinfo);
            args.setCredentials(credentials);
            args.setTabletServer(tabletServer);
            args.setForce(force);
            sendBase("shutdownTabletServer", args);
        }

        public void recv_shutdownTabletServer() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
            shutdownTabletServer_result result = new shutdownTabletServer_result();
            receiveBase(result, "shutdownTabletServer");
            if (result.sec != null) {
                throw result.sec;
            }
            return;
        }

        public void setSystemProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String property, String value) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
            send_setSystemProperty(tinfo, credentials, property, value);
            recv_setSystemProperty();
        }

        public void send_setSystemProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String property, String value) throws org.apache.thrift.TException {
            setSystemProperty_args args = new setSystemProperty_args();
            args.setTinfo(tinfo);
            args.setCredentials(credentials);
            args.setProperty(property);
            args.setValue(value);
            sendBase("setSystemProperty", args);
        }

        public void recv_setSystemProperty() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
            setSystemProperty_result result = new setSystemProperty_result();
            receiveBase(result, "setSystemProperty");
            if (result.sec != null) {
                throw result.sec;
            }
            return;
        }

        public void removeSystemProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String property) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
            send_removeSystemProperty(tinfo, credentials, property);
            recv_removeSystemProperty();
        }

        public void send_removeSystemProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String property) throws org.apache.thrift.TException {
            removeSystemProperty_args args = new removeSystemProperty_args();
            args.setTinfo(tinfo);
            args.setCredentials(credentials);
            args.setProperty(property);
            sendBase("removeSystemProperty", args);
        }

        public void recv_removeSystemProperty() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
            removeSystemProperty_result result = new removeSystemProperty_result();
            receiveBase(result, "removeSystemProperty");
            if (result.sec != null) {
                throw result.sec;
            }
            return;
        }

        public MasterMonitorInfo getMasterStats(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
            send_getMasterStats(tinfo, credentials);
            return recv_getMasterStats();
        }

        public void send_getMasterStats(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials) throws org.apache.thrift.TException {
            getMasterStats_args args = new getMasterStats_args();
            args.setTinfo(tinfo);
            args.setCredentials(credentials);
            sendBase("getMasterStats", args);
        }

        public MasterMonitorInfo recv_getMasterStats() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
            getMasterStats_result result = new getMasterStats_result();
            receiveBase(result, "getMasterStats");
            if (result.isSetSuccess()) {
                return result.success;
            }
            if (result.sec != null) {
                throw result.sec;
            }
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "getMasterStats failed: unknown result");
        }

        public void waitForBalance(org.apache.accumulo.trace.thrift.TInfo tinfo) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
            send_waitForBalance(tinfo);
            recv_waitForBalance();
        }

        public void send_waitForBalance(org.apache.accumulo.trace.thrift.TInfo tinfo) throws org.apache.thrift.TException {
            waitForBalance_args args = new waitForBalance_args();
            args.setTinfo(tinfo);
            sendBase("waitForBalance", args);
        }

        public void recv_waitForBalance() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
            waitForBalance_result result = new waitForBalance_result();
            receiveBase(result, "waitForBalance");
            if (result.sec != null) {
                throw result.sec;
            }
            return;
        }

        public void reportSplitExtent(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String serverName, TabletSplit split) throws org.apache.thrift.TException {
            send_reportSplitExtent(tinfo, credentials, serverName, split);
        }

        public void send_reportSplitExtent(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String serverName, TabletSplit split) throws org.apache.thrift.TException {
            reportSplitExtent_args args = new reportSplitExtent_args();
            args.setTinfo(tinfo);
            args.setCredentials(credentials);
            args.setServerName(serverName);
            args.setSplit(split);
            sendBase("reportSplitExtent", args);
        }

        public void reportTabletStatus(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String serverName, TabletLoadState status, org.apache.accumulo.core.data.thrift.TKeyExtent tablet) throws org.apache.thrift.TException {
            send_reportTabletStatus(tinfo, credentials, serverName, status, tablet);
        }

        public void send_reportTabletStatus(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String serverName, TabletLoadState status, org.apache.accumulo.core.data.thrift.TKeyExtent tablet) throws org.apache.thrift.TException {
            reportTabletStatus_args args = new reportTabletStatus_args();
            args.setTinfo(tinfo);
            args.setCredentials(credentials);
            args.setServerName(serverName);
            args.setStatus(status);
            args.setTablet(tablet);
            sendBase("reportTabletStatus", args);
        }
    }

    public static class AsyncClient extends FateService.AsyncClient implements AsyncIface {

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

        public void initiateFlush(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException {
            checkReady();
            initiateFlush_call method_call = new initiateFlush_call(tinfo, credentials, tableName, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class initiateFlush_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.TCredentials credentials;

            private String tableName;

            public initiateFlush_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, org.apache.thrift.async.AsyncMethodCallback resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credentials = credentials;
                this.tableName = tableName;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("initiateFlush", org.apache.thrift.protocol.TMessageType.CALL, 0));
                initiateFlush_args args = new initiateFlush_args();
                args.setTinfo(tinfo);
                args.setCredentials(credentials);
                args.setTableName(tableName);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public long getResult() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                return (new Client(prot)).recv_initiateFlush();
            }
        }

        public void waitForFlush(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, ByteBuffer startRow, ByteBuffer endRow, long flushID, long maxLoops, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException {
            checkReady();
            waitForFlush_call method_call = new waitForFlush_call(tinfo, credentials, tableName, startRow, endRow, flushID, maxLoops, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class waitForFlush_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.TCredentials credentials;

            private String tableName;

            private ByteBuffer startRow;

            private ByteBuffer endRow;

            private long flushID;

            private long maxLoops;

            public waitForFlush_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, ByteBuffer startRow, ByteBuffer endRow, long flushID, long maxLoops, org.apache.thrift.async.AsyncMethodCallback resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credentials = credentials;
                this.tableName = tableName;
                this.startRow = startRow;
                this.endRow = endRow;
                this.flushID = flushID;
                this.maxLoops = maxLoops;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("waitForFlush", org.apache.thrift.protocol.TMessageType.CALL, 0));
                waitForFlush_args args = new waitForFlush_args();
                args.setTinfo(tinfo);
                args.setCredentials(credentials);
                args.setTableName(tableName);
                args.setStartRow(startRow);
                args.setEndRow(endRow);
                args.setFlushID(flushID);
                args.setMaxLoops(maxLoops);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                (new Client(prot)).recv_waitForFlush();
            }
        }

        public void setTableProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, String property, String value, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException {
            checkReady();
            setTableProperty_call method_call = new setTableProperty_call(tinfo, credentials, tableName, property, value, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class setTableProperty_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.TCredentials credentials;

            private String tableName;

            private String property;

            private String value;

            public setTableProperty_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, String property, String value, org.apache.thrift.async.AsyncMethodCallback resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credentials = credentials;
                this.tableName = tableName;
                this.property = property;
                this.value = value;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("setTableProperty", org.apache.thrift.protocol.TMessageType.CALL, 0));
                setTableProperty_args args = new setTableProperty_args();
                args.setTinfo(tinfo);
                args.setCredentials(credentials);
                args.setTableName(tableName);
                args.setProperty(property);
                args.setValue(value);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                (new Client(prot)).recv_setTableProperty();
            }
        }

        public void removeTableProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, String property, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException {
            checkReady();
            removeTableProperty_call method_call = new removeTableProperty_call(tinfo, credentials, tableName, property, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class removeTableProperty_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.TCredentials credentials;

            private String tableName;

            private String property;

            public removeTableProperty_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, String property, org.apache.thrift.async.AsyncMethodCallback resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credentials = credentials;
                this.tableName = tableName;
                this.property = property;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("removeTableProperty", org.apache.thrift.protocol.TMessageType.CALL, 0));
                removeTableProperty_args args = new removeTableProperty_args();
                args.setTinfo(tinfo);
                args.setCredentials(credentials);
                args.setTableName(tableName);
                args.setProperty(property);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                (new Client(prot)).recv_removeTableProperty();
            }
        }

        public void setNamespaceProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String ns, String property, String value, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException {
            checkReady();
            setNamespaceProperty_call method_call = new setNamespaceProperty_call(tinfo, credentials, ns, property, value, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class setNamespaceProperty_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.TCredentials credentials;

            private String ns;

            private String property;

            private String value;

            public setNamespaceProperty_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String ns, String property, String value, org.apache.thrift.async.AsyncMethodCallback resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credentials = credentials;
                this.ns = ns;
                this.property = property;
                this.value = value;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("setNamespaceProperty", org.apache.thrift.protocol.TMessageType.CALL, 0));
                setNamespaceProperty_args args = new setNamespaceProperty_args();
                args.setTinfo(tinfo);
                args.setCredentials(credentials);
                args.setNs(ns);
                args.setProperty(property);
                args.setValue(value);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                (new Client(prot)).recv_setNamespaceProperty();
            }
        }

        public void removeNamespaceProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String ns, String property, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException {
            checkReady();
            removeNamespaceProperty_call method_call = new removeNamespaceProperty_call(tinfo, credentials, ns, property, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class removeNamespaceProperty_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.TCredentials credentials;

            private String ns;

            private String property;

            public removeNamespaceProperty_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String ns, String property, org.apache.thrift.async.AsyncMethodCallback resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credentials = credentials;
                this.ns = ns;
                this.property = property;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("removeNamespaceProperty", org.apache.thrift.protocol.TMessageType.CALL, 0));
                removeNamespaceProperty_args args = new removeNamespaceProperty_args();
                args.setTinfo(tinfo);
                args.setCredentials(credentials);
                args.setNs(ns);
                args.setProperty(property);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                (new Client(prot)).recv_removeNamespaceProperty();
            }
        }

        public void setMasterGoalState(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, MasterGoalState state, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException {
            checkReady();
            setMasterGoalState_call method_call = new setMasterGoalState_call(tinfo, credentials, state, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class setMasterGoalState_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.TCredentials credentials;

            private MasterGoalState state;

            public setMasterGoalState_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, MasterGoalState state, org.apache.thrift.async.AsyncMethodCallback resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credentials = credentials;
                this.state = state;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("setMasterGoalState", org.apache.thrift.protocol.TMessageType.CALL, 0));
                setMasterGoalState_args args = new setMasterGoalState_args();
                args.setTinfo(tinfo);
                args.setCredentials(credentials);
                args.setState(state);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                (new Client(prot)).recv_setMasterGoalState();
            }
        }

        public void shutdown(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, boolean stopTabletServers, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException {
            checkReady();
            shutdown_call method_call = new shutdown_call(tinfo, credentials, stopTabletServers, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class shutdown_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.TCredentials credentials;

            private boolean stopTabletServers;

            public shutdown_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, boolean stopTabletServers, org.apache.thrift.async.AsyncMethodCallback resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credentials = credentials;
                this.stopTabletServers = stopTabletServers;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("shutdown", org.apache.thrift.protocol.TMessageType.CALL, 0));
                shutdown_args args = new shutdown_args();
                args.setTinfo(tinfo);
                args.setCredentials(credentials);
                args.setStopTabletServers(stopTabletServers);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                (new Client(prot)).recv_shutdown();
            }
        }

        public void shutdownTabletServer(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tabletServer, boolean force, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException {
            checkReady();
            shutdownTabletServer_call method_call = new shutdownTabletServer_call(tinfo, credentials, tabletServer, force, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class shutdownTabletServer_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.TCredentials credentials;

            private String tabletServer;

            private boolean force;

            public shutdownTabletServer_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tabletServer, boolean force, org.apache.thrift.async.AsyncMethodCallback resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credentials = credentials;
                this.tabletServer = tabletServer;
                this.force = force;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("shutdownTabletServer", org.apache.thrift.protocol.TMessageType.CALL, 0));
                shutdownTabletServer_args args = new shutdownTabletServer_args();
                args.setTinfo(tinfo);
                args.setCredentials(credentials);
                args.setTabletServer(tabletServer);
                args.setForce(force);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                (new Client(prot)).recv_shutdownTabletServer();
            }
        }

        public void setSystemProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String property, String value, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException {
            checkReady();
            setSystemProperty_call method_call = new setSystemProperty_call(tinfo, credentials, property, value, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class setSystemProperty_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.TCredentials credentials;

            private String property;

            private String value;

            public setSystemProperty_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String property, String value, org.apache.thrift.async.AsyncMethodCallback resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credentials = credentials;
                this.property = property;
                this.value = value;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("setSystemProperty", org.apache.thrift.protocol.TMessageType.CALL, 0));
                setSystemProperty_args args = new setSystemProperty_args();
                args.setTinfo(tinfo);
                args.setCredentials(credentials);
                args.setProperty(property);
                args.setValue(value);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                (new Client(prot)).recv_setSystemProperty();
            }
        }

        public void removeSystemProperty(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String property, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException {
            checkReady();
            removeSystemProperty_call method_call = new removeSystemProperty_call(tinfo, credentials, property, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class removeSystemProperty_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.TCredentials credentials;

            private String property;

            public removeSystemProperty_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String property, org.apache.thrift.async.AsyncMethodCallback resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credentials = credentials;
                this.property = property;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("removeSystemProperty", org.apache.thrift.protocol.TMessageType.CALL, 0));
                removeSystemProperty_args args = new removeSystemProperty_args();
                args.setTinfo(tinfo);
                args.setCredentials(credentials);
                args.setProperty(property);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                (new Client(prot)).recv_removeSystemProperty();
            }
        }

        public void getMasterStats(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException {
            checkReady();
            getMasterStats_call method_call = new getMasterStats_call(tinfo, credentials, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class getMasterStats_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.TCredentials credentials;

            public getMasterStats_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, org.apache.thrift.async.AsyncMethodCallback resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credentials = credentials;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("getMasterStats", org.apache.thrift.protocol.TMessageType.CALL, 0));
                getMasterStats_args args = new getMasterStats_args();
                args.setTinfo(tinfo);
                args.setCredentials(credentials);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public MasterMonitorInfo getResult() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                return (new Client(prot)).recv_getMasterStats();
            }
        }

        public void waitForBalance(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException {
            checkReady();
            waitForBalance_call method_call = new waitForBalance_call(tinfo, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class waitForBalance_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            public waitForBalance_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.thrift.async.AsyncMethodCallback resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("waitForBalance", org.apache.thrift.protocol.TMessageType.CALL, 0));
                waitForBalance_args args = new waitForBalance_args();
                args.setTinfo(tinfo);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                (new Client(prot)).recv_waitForBalance();
            }
        }

        public void reportSplitExtent(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String serverName, TabletSplit split, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException {
            checkReady();
            reportSplitExtent_call method_call = new reportSplitExtent_call(tinfo, credentials, serverName, split, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class reportSplitExtent_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.TCredentials credentials;

            private String serverName;

            private TabletSplit split;

            public reportSplitExtent_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String serverName, TabletSplit split, org.apache.thrift.async.AsyncMethodCallback resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, true);
                this.tinfo = tinfo;
                this.credentials = credentials;
                this.serverName = serverName;
                this.split = split;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("reportSplitExtent", org.apache.thrift.protocol.TMessageType.CALL, 0));
                reportSplitExtent_args args = new reportSplitExtent_args();
                args.setTinfo(tinfo);
                args.setCredentials(credentials);
                args.setServerName(serverName);
                args.setSplit(split);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
            }
        }

        public void reportTabletStatus(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String serverName, TabletLoadState status, org.apache.accumulo.core.data.thrift.TKeyExtent tablet, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException {
            checkReady();
            reportTabletStatus_call method_call = new reportTabletStatus_call(tinfo, credentials, serverName, status, tablet, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class reportTabletStatus_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.TCredentials credentials;

            private String serverName;

            private TabletLoadState status;

            private org.apache.accumulo.core.data.thrift.TKeyExtent tablet;

            public reportTabletStatus_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String serverName, TabletLoadState status, org.apache.accumulo.core.data.thrift.TKeyExtent tablet, org.apache.thrift.async.AsyncMethodCallback resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, true);
                this.tinfo = tinfo;
                this.credentials = credentials;
                this.serverName = serverName;
                this.status = status;
                this.tablet = tablet;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("reportTabletStatus", org.apache.thrift.protocol.TMessageType.CALL, 0));
                reportTabletStatus_args args = new reportTabletStatus_args();
                args.setTinfo(tinfo);
                args.setCredentials(credentials);
                args.setServerName(serverName);
                args.setStatus(status);
                args.setTablet(tablet);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public void getResult() throws org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
            }
        }
    }

    public static class Processor<I extends Iface> extends FateService.Processor<I> implements org.apache.thrift.TProcessor {

        private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class.getName());

        public Processor(I iface) {
            super(iface, getProcessMap(new HashMap<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>>()));
        }

        protected Processor(I iface, Map<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>> processMap) {
            super(iface, getProcessMap(processMap));
        }

        private static <I extends Iface> Map<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>> getProcessMap(Map<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>> processMap) {
            processMap.put("initiateFlush", new initiateFlush());
            processMap.put("waitForFlush", new waitForFlush());
            processMap.put("setTableProperty", new setTableProperty());
            processMap.put("removeTableProperty", new removeTableProperty());
            processMap.put("setNamespaceProperty", new setNamespaceProperty());
            processMap.put("removeNamespaceProperty", new removeNamespaceProperty());
            processMap.put("setMasterGoalState", new setMasterGoalState());
            processMap.put("shutdown", new shutdown());
            processMap.put("shutdownTabletServer", new shutdownTabletServer());
            processMap.put("setSystemProperty", new setSystemProperty());
            processMap.put("removeSystemProperty", new removeSystemProperty());
            processMap.put("getMasterStats", new getMasterStats());
            processMap.put("waitForBalance", new waitForBalance());
            processMap.put("reportSplitExtent", new reportSplitExtent());
            processMap.put("reportTabletStatus", new reportTabletStatus());
            return processMap;
        }

        public static class initiateFlush<I extends Iface> extends org.apache.thrift.ProcessFunction<I, initiateFlush_args> {

            public initiateFlush() {
                super("initiateFlush");
            }

            public initiateFlush_args getEmptyArgsInstance() {
                return new initiateFlush_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public initiateFlush_result getResult(I iface, initiateFlush_args args) throws org.apache.thrift.TException {
                initiateFlush_result result = new initiateFlush_result();
                try {
                    result.success = iface.initiateFlush(args.tinfo, args.credentials, args.tableName);
                    result.setSuccessIsSet(true);
                } catch (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                } catch (org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope) {
                    result.tope = tope;
                }
                return result;
            }
        }

        public static class waitForFlush<I extends Iface> extends org.apache.thrift.ProcessFunction<I, waitForFlush_args> {

            public waitForFlush() {
                super("waitForFlush");
            }

            public waitForFlush_args getEmptyArgsInstance() {
                return new waitForFlush_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public waitForFlush_result getResult(I iface, waitForFlush_args args) throws org.apache.thrift.TException {
                waitForFlush_result result = new waitForFlush_result();
                try {
                    iface.waitForFlush(args.tinfo, args.credentials, args.tableName, args.startRow, args.endRow, args.flushID, args.maxLoops);
                } catch (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                } catch (org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope) {
                    result.tope = tope;
                }
                return result;
            }
        }

        public static class setTableProperty<I extends Iface> extends org.apache.thrift.ProcessFunction<I, setTableProperty_args> {

            public setTableProperty() {
                super("setTableProperty");
            }

            public setTableProperty_args getEmptyArgsInstance() {
                return new setTableProperty_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public setTableProperty_result getResult(I iface, setTableProperty_args args) throws org.apache.thrift.TException {
                setTableProperty_result result = new setTableProperty_result();
                try {
                    iface.setTableProperty(args.tinfo, args.credentials, args.tableName, args.property, args.value);
                } catch (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                } catch (org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope) {
                    result.tope = tope;
                }
                return result;
            }
        }

        public static class removeTableProperty<I extends Iface> extends org.apache.thrift.ProcessFunction<I, removeTableProperty_args> {

            public removeTableProperty() {
                super("removeTableProperty");
            }

            public removeTableProperty_args getEmptyArgsInstance() {
                return new removeTableProperty_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public removeTableProperty_result getResult(I iface, removeTableProperty_args args) throws org.apache.thrift.TException {
                removeTableProperty_result result = new removeTableProperty_result();
                try {
                    iface.removeTableProperty(args.tinfo, args.credentials, args.tableName, args.property);
                } catch (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                } catch (org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope) {
                    result.tope = tope;
                }
                return result;
            }
        }

        public static class setNamespaceProperty<I extends Iface> extends org.apache.thrift.ProcessFunction<I, setNamespaceProperty_args> {

            public setNamespaceProperty() {
                super("setNamespaceProperty");
            }

            public setNamespaceProperty_args getEmptyArgsInstance() {
                return new setNamespaceProperty_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public setNamespaceProperty_result getResult(I iface, setNamespaceProperty_args args) throws org.apache.thrift.TException {
                setNamespaceProperty_result result = new setNamespaceProperty_result();
                try {
                    iface.setNamespaceProperty(args.tinfo, args.credentials, args.ns, args.property, args.value);
                } catch (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                } catch (org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope) {
                    result.tope = tope;
                }
                return result;
            }
        }

        public static class removeNamespaceProperty<I extends Iface> extends org.apache.thrift.ProcessFunction<I, removeNamespaceProperty_args> {

            public removeNamespaceProperty() {
                super("removeNamespaceProperty");
            }

            public removeNamespaceProperty_args getEmptyArgsInstance() {
                return new removeNamespaceProperty_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public removeNamespaceProperty_result getResult(I iface, removeNamespaceProperty_args args) throws org.apache.thrift.TException {
                removeNamespaceProperty_result result = new removeNamespaceProperty_result();
                try {
                    iface.removeNamespaceProperty(args.tinfo, args.credentials, args.ns, args.property);
                } catch (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                } catch (org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope) {
                    result.tope = tope;
                }
                return result;
            }
        }

        public static class setMasterGoalState<I extends Iface> extends org.apache.thrift.ProcessFunction<I, setMasterGoalState_args> {

            public setMasterGoalState() {
                super("setMasterGoalState");
            }

            public setMasterGoalState_args getEmptyArgsInstance() {
                return new setMasterGoalState_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public setMasterGoalState_result getResult(I iface, setMasterGoalState_args args) throws org.apache.thrift.TException {
                setMasterGoalState_result result = new setMasterGoalState_result();
                try {
                    iface.setMasterGoalState(args.tinfo, args.credentials, args.state);
                } catch (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                }
                return result;
            }
        }

        public static class shutdown<I extends Iface> extends org.apache.thrift.ProcessFunction<I, shutdown_args> {

            public shutdown() {
                super("shutdown");
            }

            public shutdown_args getEmptyArgsInstance() {
                return new shutdown_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public shutdown_result getResult(I iface, shutdown_args args) throws org.apache.thrift.TException {
                shutdown_result result = new shutdown_result();
                try {
                    iface.shutdown(args.tinfo, args.credentials, args.stopTabletServers);
                } catch (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                }
                return result;
            }
        }

        public static class shutdownTabletServer<I extends Iface> extends org.apache.thrift.ProcessFunction<I, shutdownTabletServer_args> {

            public shutdownTabletServer() {
                super("shutdownTabletServer");
            }

            public shutdownTabletServer_args getEmptyArgsInstance() {
                return new shutdownTabletServer_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public shutdownTabletServer_result getResult(I iface, shutdownTabletServer_args args) throws org.apache.thrift.TException {
                shutdownTabletServer_result result = new shutdownTabletServer_result();
                try {
                    iface.shutdownTabletServer(args.tinfo, args.credentials, args.tabletServer, args.force);
                } catch (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                }
                return result;
            }
        }

        public static class setSystemProperty<I extends Iface> extends org.apache.thrift.ProcessFunction<I, setSystemProperty_args> {

            public setSystemProperty() {
                super("setSystemProperty");
            }

            public setSystemProperty_args getEmptyArgsInstance() {
                return new setSystemProperty_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public setSystemProperty_result getResult(I iface, setSystemProperty_args args) throws org.apache.thrift.TException {
                setSystemProperty_result result = new setSystemProperty_result();
                try {
                    iface.setSystemProperty(args.tinfo, args.credentials, args.property, args.value);
                } catch (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                }
                return result;
            }
        }

        public static class removeSystemProperty<I extends Iface> extends org.apache.thrift.ProcessFunction<I, removeSystemProperty_args> {

            public removeSystemProperty() {
                super("removeSystemProperty");
            }

            public removeSystemProperty_args getEmptyArgsInstance() {
                return new removeSystemProperty_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public removeSystemProperty_result getResult(I iface, removeSystemProperty_args args) throws org.apache.thrift.TException {
                removeSystemProperty_result result = new removeSystemProperty_result();
                try {
                    iface.removeSystemProperty(args.tinfo, args.credentials, args.property);
                } catch (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                }
                return result;
            }
        }

        public static class getMasterStats<I extends Iface> extends org.apache.thrift.ProcessFunction<I, getMasterStats_args> {

            public getMasterStats() {
                super("getMasterStats");
            }

            public getMasterStats_args getEmptyArgsInstance() {
                return new getMasterStats_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public getMasterStats_result getResult(I iface, getMasterStats_args args) throws org.apache.thrift.TException {
                getMasterStats_result result = new getMasterStats_result();
                try {
                    result.success = iface.getMasterStats(args.tinfo, args.credentials);
                } catch (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                }
                return result;
            }
        }

        public static class waitForBalance<I extends Iface> extends org.apache.thrift.ProcessFunction<I, waitForBalance_args> {

            public waitForBalance() {
                super("waitForBalance");
            }

            public waitForBalance_args getEmptyArgsInstance() {
                return new waitForBalance_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public waitForBalance_result getResult(I iface, waitForBalance_args args) throws org.apache.thrift.TException {
                waitForBalance_result result = new waitForBalance_result();
                try {
                    iface.waitForBalance(args.tinfo);
                } catch (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                }
                return result;
            }
        }

        public static class reportSplitExtent<I extends Iface> extends org.apache.thrift.ProcessFunction<I, reportSplitExtent_args> {

            public reportSplitExtent() {
                super("reportSplitExtent");
            }

            public reportSplitExtent_args getEmptyArgsInstance() {
                return new reportSplitExtent_args();
            }

            protected boolean isOneway() {
                return true;
            }

            public org.apache.thrift.TBase getResult(I iface, reportSplitExtent_args args) throws org.apache.thrift.TException {
                iface.reportSplitExtent(args.tinfo, args.credentials, args.serverName, args.split);
                return null;
            }
        }

        public static class reportTabletStatus<I extends Iface> extends org.apache.thrift.ProcessFunction<I, reportTabletStatus_args> {

            public reportTabletStatus() {
                super("reportTabletStatus");
            }

            public reportTabletStatus_args getEmptyArgsInstance() {
                return new reportTabletStatus_args();
            }

            protected boolean isOneway() {
                return true;
            }

            public org.apache.thrift.TBase getResult(I iface, reportTabletStatus_args args) throws org.apache.thrift.TException {
                iface.reportTabletStatus(args.tinfo, args.credentials, args.serverName, args.status, args.tablet);
                return null;
            }
        }
    }

    public static class AsyncProcessor<I extends AsyncIface> extends FateService.AsyncProcessor<I> {

        private static final Logger LOGGER = LoggerFactory.getLogger(AsyncProcessor.class.getName());

        public AsyncProcessor(I iface) {
            super(iface, getProcessMap(new HashMap<String, org.apache.thrift.AsyncProcessFunction<I, ? extends org.apache.thrift.TBase, ?>>()));
        }

        protected AsyncProcessor(I iface, Map<String, org.apache.thrift.AsyncProcessFunction<I, ? extends org.apache.thrift.TBase, ?>> processMap) {
            super(iface, getProcessMap(processMap));
        }

        private static <I extends AsyncIface> Map<String, org.apache.thrift.AsyncProcessFunction<I, ? extends org.apache.thrift.TBase, ?>> getProcessMap(Map<String, org.apache.thrift.AsyncProcessFunction<I, ? extends org.apache.thrift.TBase, ?>> processMap) {
            processMap.put("initiateFlush", new initiateFlush());
            processMap.put("waitForFlush", new waitForFlush());
            processMap.put("setTableProperty", new setTableProperty());
            processMap.put("removeTableProperty", new removeTableProperty());
            processMap.put("setNamespaceProperty", new setNamespaceProperty());
            processMap.put("removeNamespaceProperty", new removeNamespaceProperty());
            processMap.put("setMasterGoalState", new setMasterGoalState());
            processMap.put("shutdown", new shutdown());
            processMap.put("shutdownTabletServer", new shutdownTabletServer());
            processMap.put("setSystemProperty", new setSystemProperty());
            processMap.put("removeSystemProperty", new removeSystemProperty());
            processMap.put("getMasterStats", new getMasterStats());
            processMap.put("waitForBalance", new waitForBalance());
            processMap.put("reportSplitExtent", new reportSplitExtent());
            processMap.put("reportTabletStatus", new reportTabletStatus());
            return processMap;
        }

        public static class initiateFlush<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, initiateFlush_args, Long> {

            public initiateFlush() {
                super("initiateFlush");
            }

            public initiateFlush_args getEmptyArgsInstance() {
                return new initiateFlush_args();
            }

            public AsyncMethodCallback<Long> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
                final org.apache.thrift.AsyncProcessFunction fcall = this;
                return new AsyncMethodCallback<Long>() {

                    public void onComplete(Long o) {
                        initiateFlush_result result = new initiateFlush_result();
                        result.success = o;
                        result.setSuccessIsSet(true);
                        try {
                            fcall.sendResponse(fb, result, org.apache.thrift.protocol.TMessageType.REPLY, seqid);
                            return;
                        } catch (Exception e) {
                            LOGGER.error("Exception writing to internal frame buffer", e);
                        }
                        fb.close();
                    }

                    public void onError(Exception e) {
                        byte msgType = org.apache.thrift.protocol.TMessageType.REPLY;
                        org.apache.thrift.TBase msg;
                        initiateFlush_result result = new initiateFlush_result();
                        if (e instanceof org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) {
                            result.sec = (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) e;
                            result.setSecIsSet(true);
                            msg = result;
                        } else if (e instanceof org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException) {
                            result.tope = (org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException) e;
                            result.setTopeIsSet(true);
                            msg = result;
                        } else {
                            msgType = org.apache.thrift.protocol.TMessageType.EXCEPTION;
                            msg = (org.apache.thrift.TBase) new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.INTERNAL_ERROR, e.getMessage());
                        }
                        try {
                            fcall.sendResponse(fb, msg, msgType, seqid);
                            return;
                        } catch (Exception ex) {
                            LOGGER.error("Exception writing to internal frame buffer", ex);
                        }
                        fb.close();
                    }
                };
            }

            protected boolean isOneway() {
                return false;
            }

            public void start(I iface, initiateFlush_args args, org.apache.thrift.async.AsyncMethodCallback<Long> resultHandler) throws TException {
                iface.initiateFlush(args.tinfo, args.credentials, args.tableName, resultHandler);
            }
        }

        public static class waitForFlush<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, waitForFlush_args, Void> {

            public waitForFlush() {
                super("waitForFlush");
            }

            public waitForFlush_args getEmptyArgsInstance() {
                return new waitForFlush_args();
            }

            public AsyncMethodCallback<Void> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
                final org.apache.thrift.AsyncProcessFunction fcall = this;
                return new AsyncMethodCallback<Void>() {

                    public void onComplete(Void o) {
                        waitForFlush_result result = new waitForFlush_result();
                        try {
                            fcall.sendResponse(fb, result, org.apache.thrift.protocol.TMessageType.REPLY, seqid);
                            return;
                        } catch (Exception e) {
                            LOGGER.error("Exception writing to internal frame buffer", e);
                        }
                        fb.close();
                    }

                    public void onError(Exception e) {
                        byte msgType = org.apache.thrift.protocol.TMessageType.REPLY;
                        org.apache.thrift.TBase msg;
                        waitForFlush_result result = new waitForFlush_result();
                        if (e instanceof org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) {
                            result.sec = (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) e;
                            result.setSecIsSet(true);
                            msg = result;
                        } else if (e instanceof org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException) {
                            result.tope = (org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException) e;
                            result.setTopeIsSet(true);
                            msg = result;
                        } else {
                            msgType = org.apache.thrift.protocol.TMessageType.EXCEPTION;
                            msg = (org.apache.thrift.TBase) new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.INTERNAL_ERROR, e.getMessage());
                        }
                        try {
                            fcall.sendResponse(fb, msg, msgType, seqid);
                            return;
                        } catch (Exception ex) {
                            LOGGER.error("Exception writing to internal frame buffer", ex);
                        }
                        fb.close();
                    }
                };
            }

            protected boolean isOneway() {
                return false;
            }

            public void start(I iface, waitForFlush_args args, org.apache.thrift.async.AsyncMethodCallback<Void> resultHandler) throws TException {
                iface.waitForFlush(args.tinfo, args.credentials, args.tableName, args.startRow, args.endRow, args.flushID, args.maxLoops, resultHandler);
            }
        }

        public static class setTableProperty<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, setTableProperty_args, Void> {

            public setTableProperty() {
                super("setTableProperty");
            }

            public setTableProperty_args getEmptyArgsInstance() {
                return new setTableProperty_args();
            }

            public AsyncMethodCallback<Void> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
                final org.apache.thrift.AsyncProcessFunction fcall = this;
                return new AsyncMethodCallback<Void>() {

                    public void onComplete(Void o) {
                        setTableProperty_result result = new setTableProperty_result();
                        try {
                            fcall.sendResponse(fb, result, org.apache.thrift.protocol.TMessageType.REPLY, seqid);
                            return;
                        } catch (Exception e) {
                            LOGGER.error("Exception writing to internal frame buffer", e);
                        }
                        fb.close();
                    }

                    public void onError(Exception e) {
                        byte msgType = org.apache.thrift.protocol.TMessageType.REPLY;
                        org.apache.thrift.TBase msg;
                        setTableProperty_result result = new setTableProperty_result();
                        if (e instanceof org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) {
                            result.sec = (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) e;
                            result.setSecIsSet(true);
                            msg = result;
                        } else if (e instanceof org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException) {
                            result.tope = (org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException) e;
                            result.setTopeIsSet(true);
                            msg = result;
                        } else {
                            msgType = org.apache.thrift.protocol.TMessageType.EXCEPTION;
                            msg = (org.apache.thrift.TBase) new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.INTERNAL_ERROR, e.getMessage());
                        }
                        try {
                            fcall.sendResponse(fb, msg, msgType, seqid);
                            return;
                        } catch (Exception ex) {
                            LOGGER.error("Exception writing to internal frame buffer", ex);
                        }
                        fb.close();
                    }
                };
            }

            protected boolean isOneway() {
                return false;
            }

            public void start(I iface, setTableProperty_args args, org.apache.thrift.async.AsyncMethodCallback<Void> resultHandler) throws TException {
                iface.setTableProperty(args.tinfo, args.credentials, args.tableName, args.property, args.value, resultHandler);
            }
        }

        public static class removeTableProperty<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, removeTableProperty_args, Void> {

            public removeTableProperty() {
                super("removeTableProperty");
            }

            public removeTableProperty_args getEmptyArgsInstance() {
                return new removeTableProperty_args();
            }

            public AsyncMethodCallback<Void> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
                final org.apache.thrift.AsyncProcessFunction fcall = this;
                return new AsyncMethodCallback<Void>() {

                    public void onComplete(Void o) {
                        removeTableProperty_result result = new removeTableProperty_result();
                        try {
                            fcall.sendResponse(fb, result, org.apache.thrift.protocol.TMessageType.REPLY, seqid);
                            return;
                        } catch (Exception e) {
                            LOGGER.error("Exception writing to internal frame buffer", e);
                        }
                        fb.close();
                    }

                    public void onError(Exception e) {
                        byte msgType = org.apache.thrift.protocol.TMessageType.REPLY;
                        org.apache.thrift.TBase msg;
                        removeTableProperty_result result = new removeTableProperty_result();
                        if (e instanceof org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) {
                            result.sec = (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) e;
                            result.setSecIsSet(true);
                            msg = result;
                        } else if (e instanceof org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException) {
                            result.tope = (org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException) e;
                            result.setTopeIsSet(true);
                            msg = result;
                        } else {
                            msgType = org.apache.thrift.protocol.TMessageType.EXCEPTION;
                            msg = (org.apache.thrift.TBase) new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.INTERNAL_ERROR, e.getMessage());
                        }
                        try {
                            fcall.sendResponse(fb, msg, msgType, seqid);
                            return;
                        } catch (Exception ex) {
                            LOGGER.error("Exception writing to internal frame buffer", ex);
                        }
                        fb.close();
                    }
                };
            }

            protected boolean isOneway() {
                return false;
            }

            public void start(I iface, removeTableProperty_args args, org.apache.thrift.async.AsyncMethodCallback<Void> resultHandler) throws TException {
                iface.removeTableProperty(args.tinfo, args.credentials, args.tableName, args.property, resultHandler);
            }
        }

        public static class setNamespaceProperty<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, setNamespaceProperty_args, Void> {

            public setNamespaceProperty() {
                super("setNamespaceProperty");
            }

            public setNamespaceProperty_args getEmptyArgsInstance() {
                return new setNamespaceProperty_args();
            }

            public AsyncMethodCallback<Void> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
                final org.apache.thrift.AsyncProcessFunction fcall = this;
                return new AsyncMethodCallback<Void>() {

                    public void onComplete(Void o) {
                        setNamespaceProperty_result result = new setNamespaceProperty_result();
                        try {
                            fcall.sendResponse(fb, result, org.apache.thrift.protocol.TMessageType.REPLY, seqid);
                            return;
                        } catch (Exception e) {
                            LOGGER.error("Exception writing to internal frame buffer", e);
                        }
                        fb.close();
                    }

                    public void onError(Exception e) {
                        byte msgType = org.apache.thrift.protocol.TMessageType.REPLY;
                        org.apache.thrift.TBase msg;
                        setNamespaceProperty_result result = new setNamespaceProperty_result();
                        if (e instanceof org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) {
                            result.sec = (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) e;
                            result.setSecIsSet(true);
                            msg = result;
                        } else if (e instanceof org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException) {
                            result.tope = (org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException) e;
                            result.setTopeIsSet(true);
                            msg = result;
                        } else {
                            msgType = org.apache.thrift.protocol.TMessageType.EXCEPTION;
                            msg = (org.apache.thrift.TBase) new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.INTERNAL_ERROR, e.getMessage());
                        }
                        try {
                            fcall.sendResponse(fb, msg, msgType, seqid);
                            return;
                        } catch (Exception ex) {
                            LOGGER.error("Exception writing to internal frame buffer", ex);
                        }
                        fb.close();
                    }
                };
            }

            protected boolean isOneway() {
                return false;
            }

            public void start(I iface, setNamespaceProperty_args args, org.apache.thrift.async.AsyncMethodCallback<Void> resultHandler) throws TException {
                iface.setNamespaceProperty(args.tinfo, args.credentials, args.ns, args.property, args.value, resultHandler);
            }
        }

        public static class removeNamespaceProperty<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, removeNamespaceProperty_args, Void> {

            public removeNamespaceProperty() {
                super("removeNamespaceProperty");
            }

            public removeNamespaceProperty_args getEmptyArgsInstance() {
                return new removeNamespaceProperty_args();
            }

            public AsyncMethodCallback<Void> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
                final org.apache.thrift.AsyncProcessFunction fcall = this;
                return new AsyncMethodCallback<Void>() {

                    public void onComplete(Void o) {
                        removeNamespaceProperty_result result = new removeNamespaceProperty_result();
                        try {
                            fcall.sendResponse(fb, result, org.apache.thrift.protocol.TMessageType.REPLY, seqid);
                            return;
                        } catch (Exception e) {
                            LOGGER.error("Exception writing to internal frame buffer", e);
                        }
                        fb.close();
                    }

                    public void onError(Exception e) {
                        byte msgType = org.apache.thrift.protocol.TMessageType.REPLY;
                        org.apache.thrift.TBase msg;
                        removeNamespaceProperty_result result = new removeNamespaceProperty_result();
                        if (e instanceof org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) {
                            result.sec = (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) e;
                            result.setSecIsSet(true);
                            msg = result;
                        } else if (e instanceof org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException) {
                            result.tope = (org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException) e;
                            result.setTopeIsSet(true);
                            msg = result;
                        } else {
                            msgType = org.apache.thrift.protocol.TMessageType.EXCEPTION;
                            msg = (org.apache.thrift.TBase) new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.INTERNAL_ERROR, e.getMessage());
                        }
                        try {
                            fcall.sendResponse(fb, msg, msgType, seqid);
                            return;
                        } catch (Exception ex) {
                            LOGGER.error("Exception writing to internal frame buffer", ex);
                        }
                        fb.close();
                    }
                };
            }

            protected boolean isOneway() {
                return false;
            }

            public void start(I iface, removeNamespaceProperty_args args, org.apache.thrift.async.AsyncMethodCallback<Void> resultHandler) throws TException {
                iface.removeNamespaceProperty(args.tinfo, args.credentials, args.ns, args.property, resultHandler);
            }
        }

        public static class setMasterGoalState<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, setMasterGoalState_args, Void> {

            public setMasterGoalState() {
                super("setMasterGoalState");
            }

            public setMasterGoalState_args getEmptyArgsInstance() {
                return new setMasterGoalState_args();
            }

            public AsyncMethodCallback<Void> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
                final org.apache.thrift.AsyncProcessFunction fcall = this;
                return new AsyncMethodCallback<Void>() {

                    public void onComplete(Void o) {
                        setMasterGoalState_result result = new setMasterGoalState_result();
                        try {
                            fcall.sendResponse(fb, result, org.apache.thrift.protocol.TMessageType.REPLY, seqid);
                            return;
                        } catch (Exception e) {
                            LOGGER.error("Exception writing to internal frame buffer", e);
                        }
                        fb.close();
                    }

                    public void onError(Exception e) {
                        byte msgType = org.apache.thrift.protocol.TMessageType.REPLY;
                        org.apache.thrift.TBase msg;
                        setMasterGoalState_result result = new setMasterGoalState_result();
                        if (e instanceof org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) {
                            result.sec = (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) e;
                            result.setSecIsSet(true);
                            msg = result;
                        } else {
                            msgType = org.apache.thrift.protocol.TMessageType.EXCEPTION;
                            msg = (org.apache.thrift.TBase) new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.INTERNAL_ERROR, e.getMessage());
                        }
                        try {
                            fcall.sendResponse(fb, msg, msgType, seqid);
                            return;
                        } catch (Exception ex) {
                            LOGGER.error("Exception writing to internal frame buffer", ex);
                        }
                        fb.close();
                    }
                };
            }

            protected boolean isOneway() {
                return false;
            }

            public void start(I iface, setMasterGoalState_args args, org.apache.thrift.async.AsyncMethodCallback<Void> resultHandler) throws TException {
                iface.setMasterGoalState(args.tinfo, args.credentials, args.state, resultHandler);
            }
        }

        public static class shutdown<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, shutdown_args, Void> {

            public shutdown() {
                super("shutdown");
            }

            public shutdown_args getEmptyArgsInstance() {
                return new shutdown_args();
            }

            public AsyncMethodCallback<Void> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
                final org.apache.thrift.AsyncProcessFunction fcall = this;
                return new AsyncMethodCallback<Void>() {

                    public void onComplete(Void o) {
                        shutdown_result result = new shutdown_result();
                        try {
                            fcall.sendResponse(fb, result, org.apache.thrift.protocol.TMessageType.REPLY, seqid);
                            return;
                        } catch (Exception e) {
                            LOGGER.error("Exception writing to internal frame buffer", e);
                        }
                        fb.close();
                    }

                    public void onError(Exception e) {
                        byte msgType = org.apache.thrift.protocol.TMessageType.REPLY;
                        org.apache.thrift.TBase msg;
                        shutdown_result result = new shutdown_result();
                        if (e instanceof org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) {
                            result.sec = (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) e;
                            result.setSecIsSet(true);
                            msg = result;
                        } else {
                            msgType = org.apache.thrift.protocol.TMessageType.EXCEPTION;
                            msg = (org.apache.thrift.TBase) new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.INTERNAL_ERROR, e.getMessage());
                        }
                        try {
                            fcall.sendResponse(fb, msg, msgType, seqid);
                            return;
                        } catch (Exception ex) {
                            LOGGER.error("Exception writing to internal frame buffer", ex);
                        }
                        fb.close();
                    }
                };
            }

            protected boolean isOneway() {
                return false;
            }

            public void start(I iface, shutdown_args args, org.apache.thrift.async.AsyncMethodCallback<Void> resultHandler) throws TException {
                iface.shutdown(args.tinfo, args.credentials, args.stopTabletServers, resultHandler);
            }
        }

        public static class shutdownTabletServer<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, shutdownTabletServer_args, Void> {

            public shutdownTabletServer() {
                super("shutdownTabletServer");
            }

            public shutdownTabletServer_args getEmptyArgsInstance() {
                return new shutdownTabletServer_args();
            }

            public AsyncMethodCallback<Void> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
                final org.apache.thrift.AsyncProcessFunction fcall = this;
                return new AsyncMethodCallback<Void>() {

                    public void onComplete(Void o) {
                        shutdownTabletServer_result result = new shutdownTabletServer_result();
                        try {
                            fcall.sendResponse(fb, result, org.apache.thrift.protocol.TMessageType.REPLY, seqid);
                            return;
                        } catch (Exception e) {
                            LOGGER.error("Exception writing to internal frame buffer", e);
                        }
                        fb.close();
                    }

                    public void onError(Exception e) {
                        byte msgType = org.apache.thrift.protocol.TMessageType.REPLY;
                        org.apache.thrift.TBase msg;
                        shutdownTabletServer_result result = new shutdownTabletServer_result();
                        if (e instanceof org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) {
                            result.sec = (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) e;
                            result.setSecIsSet(true);
                            msg = result;
                        } else {
                            msgType = org.apache.thrift.protocol.TMessageType.EXCEPTION;
                            msg = (org.apache.thrift.TBase) new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.INTERNAL_ERROR, e.getMessage());
                        }
                        try {
                            fcall.sendResponse(fb, msg, msgType, seqid);
                            return;
                        } catch (Exception ex) {
                            LOGGER.error("Exception writing to internal frame buffer", ex);
                        }
                        fb.close();
                    }
                };
            }

            protected boolean isOneway() {
                return false;
            }

            public void start(I iface, shutdownTabletServer_args args, org.apache.thrift.async.AsyncMethodCallback<Void> resultHandler) throws TException {
                iface.shutdownTabletServer(args.tinfo, args.credentials, args.tabletServer, args.force, resultHandler);
            }
        }

        public static class setSystemProperty<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, setSystemProperty_args, Void> {

            public setSystemProperty() {
                super("setSystemProperty");
            }

            public setSystemProperty_args getEmptyArgsInstance() {
                return new setSystemProperty_args();
            }

            public AsyncMethodCallback<Void> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
                final org.apache.thrift.AsyncProcessFunction fcall = this;
                return new AsyncMethodCallback<Void>() {

                    public void onComplete(Void o) {
                        setSystemProperty_result result = new setSystemProperty_result();
                        try {
                            fcall.sendResponse(fb, result, org.apache.thrift.protocol.TMessageType.REPLY, seqid);
                            return;
                        } catch (Exception e) {
                            LOGGER.error("Exception writing to internal frame buffer", e);
                        }
                        fb.close();
                    }

                    public void onError(Exception e) {
                        byte msgType = org.apache.thrift.protocol.TMessageType.REPLY;
                        org.apache.thrift.TBase msg;
                        setSystemProperty_result result = new setSystemProperty_result();
                        if (e instanceof org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) {
                            result.sec = (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) e;
                            result.setSecIsSet(true);
                            msg = result;
                        } else {
                            msgType = org.apache.thrift.protocol.TMessageType.EXCEPTION;
                            msg = (org.apache.thrift.TBase) new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.INTERNAL_ERROR, e.getMessage());
                        }
                        try {
                            fcall.sendResponse(fb, msg, msgType, seqid);
                            return;
                        } catch (Exception ex) {
                            LOGGER.error("Exception writing to internal frame buffer", ex);
                        }
                        fb.close();
                    }
                };
            }

            protected boolean isOneway() {
                return false;
            }

            public void start(I iface, setSystemProperty_args args, org.apache.thrift.async.AsyncMethodCallback<Void> resultHandler) throws TException {
                iface.setSystemProperty(args.tinfo, args.credentials, args.property, args.value, resultHandler);
            }
        }

        public static class removeSystemProperty<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, removeSystemProperty_args, Void> {

            public removeSystemProperty() {
                super("removeSystemProperty");
            }

            public removeSystemProperty_args getEmptyArgsInstance() {
                return new removeSystemProperty_args();
            }

            public AsyncMethodCallback<Void> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
                final org.apache.thrift.AsyncProcessFunction fcall = this;
                return new AsyncMethodCallback<Void>() {

                    public void onComplete(Void o) {
                        removeSystemProperty_result result = new removeSystemProperty_result();
                        try {
                            fcall.sendResponse(fb, result, org.apache.thrift.protocol.TMessageType.REPLY, seqid);
                            return;
                        } catch (Exception e) {
                            LOGGER.error("Exception writing to internal frame buffer", e);
                        }
                        fb.close();
                    }

                    public void onError(Exception e) {
                        byte msgType = org.apache.thrift.protocol.TMessageType.REPLY;
                        org.apache.thrift.TBase msg;
                        removeSystemProperty_result result = new removeSystemProperty_result();
                        if (e instanceof org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) {
                            result.sec = (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) e;
                            result.setSecIsSet(true);
                            msg = result;
                        } else {
                            msgType = org.apache.thrift.protocol.TMessageType.EXCEPTION;
                            msg = (org.apache.thrift.TBase) new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.INTERNAL_ERROR, e.getMessage());
                        }
                        try {
                            fcall.sendResponse(fb, msg, msgType, seqid);
                            return;
                        } catch (Exception ex) {
                            LOGGER.error("Exception writing to internal frame buffer", ex);
                        }
                        fb.close();
                    }
                };
            }

            protected boolean isOneway() {
                return false;
            }

            public void start(I iface, removeSystemProperty_args args, org.apache.thrift.async.AsyncMethodCallback<Void> resultHandler) throws TException {
                iface.removeSystemProperty(args.tinfo, args.credentials, args.property, resultHandler);
            }
        }

        public static class getMasterStats<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, getMasterStats_args, MasterMonitorInfo> {

            public getMasterStats() {
                super("getMasterStats");
            }

            public getMasterStats_args getEmptyArgsInstance() {
                return new getMasterStats_args();
            }

            public AsyncMethodCallback<MasterMonitorInfo> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
                final org.apache.thrift.AsyncProcessFunction fcall = this;
                return new AsyncMethodCallback<MasterMonitorInfo>() {

                    public void onComplete(MasterMonitorInfo o) {
                        getMasterStats_result result = new getMasterStats_result();
                        result.success = o;
                        try {
                            fcall.sendResponse(fb, result, org.apache.thrift.protocol.TMessageType.REPLY, seqid);
                            return;
                        } catch (Exception e) {
                            LOGGER.error("Exception writing to internal frame buffer", e);
                        }
                        fb.close();
                    }

                    public void onError(Exception e) {
                        byte msgType = org.apache.thrift.protocol.TMessageType.REPLY;
                        org.apache.thrift.TBase msg;
                        getMasterStats_result result = new getMasterStats_result();
                        if (e instanceof org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) {
                            result.sec = (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) e;
                            result.setSecIsSet(true);
                            msg = result;
                        } else {
                            msgType = org.apache.thrift.protocol.TMessageType.EXCEPTION;
                            msg = (org.apache.thrift.TBase) new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.INTERNAL_ERROR, e.getMessage());
                        }
                        try {
                            fcall.sendResponse(fb, msg, msgType, seqid);
                            return;
                        } catch (Exception ex) {
                            LOGGER.error("Exception writing to internal frame buffer", ex);
                        }
                        fb.close();
                    }
                };
            }

            protected boolean isOneway() {
                return false;
            }

            public void start(I iface, getMasterStats_args args, org.apache.thrift.async.AsyncMethodCallback<MasterMonitorInfo> resultHandler) throws TException {
                iface.getMasterStats(args.tinfo, args.credentials, resultHandler);
            }
        }

        public static class waitForBalance<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, waitForBalance_args, Void> {

            public waitForBalance() {
                super("waitForBalance");
            }

            public waitForBalance_args getEmptyArgsInstance() {
                return new waitForBalance_args();
            }

            public AsyncMethodCallback<Void> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
                final org.apache.thrift.AsyncProcessFunction fcall = this;
                return new AsyncMethodCallback<Void>() {

                    public void onComplete(Void o) {
                        waitForBalance_result result = new waitForBalance_result();
                        try {
                            fcall.sendResponse(fb, result, org.apache.thrift.protocol.TMessageType.REPLY, seqid);
                            return;
                        } catch (Exception e) {
                            LOGGER.error("Exception writing to internal frame buffer", e);
                        }
                        fb.close();
                    }

                    public void onError(Exception e) {
                        byte msgType = org.apache.thrift.protocol.TMessageType.REPLY;
                        org.apache.thrift.TBase msg;
                        waitForBalance_result result = new waitForBalance_result();
                        if (e instanceof org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) {
                            result.sec = (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) e;
                            result.setSecIsSet(true);
                            msg = result;
                        } else {
                            msgType = org.apache.thrift.protocol.TMessageType.EXCEPTION;
                            msg = (org.apache.thrift.TBase) new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.INTERNAL_ERROR, e.getMessage());
                        }
                        try {
                            fcall.sendResponse(fb, msg, msgType, seqid);
                            return;
                        } catch (Exception ex) {
                            LOGGER.error("Exception writing to internal frame buffer", ex);
                        }
                        fb.close();
                    }
                };
            }

            protected boolean isOneway() {
                return false;
            }

            public void start(I iface, waitForBalance_args args, org.apache.thrift.async.AsyncMethodCallback<Void> resultHandler) throws TException {
                iface.waitForBalance(args.tinfo, resultHandler);
            }
        }

        public static class reportSplitExtent<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, reportSplitExtent_args, Void> {

            public reportSplitExtent() {
                super("reportSplitExtent");
            }

            public reportSplitExtent_args getEmptyArgsInstance() {
                return new reportSplitExtent_args();
            }

            public AsyncMethodCallback<Void> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
                final org.apache.thrift.AsyncProcessFunction fcall = this;
                return new AsyncMethodCallback<Void>() {

                    public void onComplete(Void o) {
                    }

                    public void onError(Exception e) {
                    }
                };
            }

            protected boolean isOneway() {
                return true;
            }

            public void start(I iface, reportSplitExtent_args args, org.apache.thrift.async.AsyncMethodCallback<Void> resultHandler) throws TException {
                iface.reportSplitExtent(args.tinfo, args.credentials, args.serverName, args.split, resultHandler);
            }
        }

        public static class reportTabletStatus<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, reportTabletStatus_args, Void> {

            public reportTabletStatus() {
                super("reportTabletStatus");
            }

            public reportTabletStatus_args getEmptyArgsInstance() {
                return new reportTabletStatus_args();
            }

            public AsyncMethodCallback<Void> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
                final org.apache.thrift.AsyncProcessFunction fcall = this;
                return new AsyncMethodCallback<Void>() {

                    public void onComplete(Void o) {
                    }

                    public void onError(Exception e) {
                    }
                };
            }

            protected boolean isOneway() {
                return true;
            }

            public void start(I iface, reportTabletStatus_args args, org.apache.thrift.async.AsyncMethodCallback<Void> resultHandler) throws TException {
                iface.reportTabletStatus(args.tinfo, args.credentials, args.serverName, args.status, args.tablet, resultHandler);
            }
        }
    }

    public static class initiateFlush_args implements org.apache.thrift.TBase<initiateFlush_args, initiateFlush_args._Fields>, java.io.Serializable, Cloneable, Comparable<initiateFlush_args> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("initiateFlush_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 3);

        private static final org.apache.thrift.protocol.TField CREDENTIALS_FIELD_DESC = new org.apache.thrift.protocol.TField("credentials", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField TABLE_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("tableName", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new initiateFlush_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new initiateFlush_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.TCredentials credentials;

        public String tableName;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 3, "tinfo"), CREDENTIALS((short) 1, "credentials"), TABLE_NAME((short) 2, "tableName");

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
                        return CREDENTIALS;
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
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.CREDENTIALS, new org.apache.thrift.meta_data.FieldMetaData("credentials", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.TCredentials.class)));
            tmpMap.put(_Fields.TABLE_NAME, new org.apache.thrift.meta_data.FieldMetaData("tableName", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(initiateFlush_args.class, metaDataMap);
        }

        public initiateFlush_args() {
        }

        public initiateFlush_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName) {
            this();
            this.tinfo = tinfo;
            this.credentials = credentials;
            this.tableName = tableName;
        }

        public initiateFlush_args(initiateFlush_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredentials()) {
                this.credentials = new org.apache.accumulo.core.security.thrift.TCredentials(other.credentials);
            }
            if (other.isSetTableName()) {
                this.tableName = other.tableName;
            }
        }

        public initiateFlush_args deepCopy() {
            return new initiateFlush_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credentials = null;
            this.tableName = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public initiateFlush_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
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

        public org.apache.accumulo.core.security.thrift.TCredentials getCredentials() {
            return this.credentials;
        }

        public initiateFlush_args setCredentials(org.apache.accumulo.core.security.thrift.TCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public void unsetCredentials() {
            this.credentials = null;
        }

        public boolean isSetCredentials() {
            return this.credentials != null;
        }

        public void setCredentialsIsSet(boolean value) {
            if (!value) {
                this.credentials = null;
            }
        }

        public String getTableName() {
            return this.tableName;
        }

        public initiateFlush_args setTableName(String tableName) {
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
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
                        setTinfo((org.apache.accumulo.trace.thrift.TInfo) value);
                    }
                    break;
                case CREDENTIALS:
                    if (value == null) {
                        unsetCredentials();
                    } else {
                        setCredentials((org.apache.accumulo.core.security.thrift.TCredentials) value);
                    }
                    break;
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
                case TINFO:
                    return getTinfo();
                case CREDENTIALS:
                    return getCredentials();
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
                case TINFO:
                    return isSetTinfo();
                case CREDENTIALS:
                    return isSetCredentials();
                case TABLE_NAME:
                    return isSetTableName();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof initiateFlush_args)
                return this.equals((initiateFlush_args) that);
            return false;
        }

        public boolean equals(initiateFlush_args that) {
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
            boolean this_present_credentials = true && this.isSetCredentials();
            boolean that_present_credentials = true && that.isSetCredentials();
            if (this_present_credentials || that_present_credentials) {
                if (!(this_present_credentials && that_present_credentials))
                    return false;
                if (!this.credentials.equals(that.credentials))
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
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public int compareTo(initiateFlush_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(other.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, other.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredentials()).compareTo(other.isSetCredentials());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredentials()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credentials, other.credentials);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTableName()).compareTo(other.isSetTableName());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTableName()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tableName, other.tableName);
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
            StringBuilder sb = new StringBuilder("initiateFlush_args(");
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
            sb.append("credentials:");
            if (this.credentials == null) {
                sb.append("null");
            } else {
                sb.append(this.credentials);
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
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credentials != null) {
                credentials.validate();
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

        private static class initiateFlush_argsStandardSchemeFactory implements SchemeFactory {

            public initiateFlush_argsStandardScheme getScheme() {
                return new initiateFlush_argsStandardScheme();
            }
        }

        private static class initiateFlush_argsStandardScheme extends StandardScheme<initiateFlush_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, initiateFlush_args struct) throws org.apache.thrift.TException {
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
                                struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                                struct.credentials.read(iprot);
                                struct.setCredentialsIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, initiateFlush_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credentials != null) {
                    oprot.writeFieldBegin(CREDENTIALS_FIELD_DESC);
                    struct.credentials.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.tableName != null) {
                    oprot.writeFieldBegin(TABLE_NAME_FIELD_DESC);
                    oprot.writeString(struct.tableName);
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

        private static class initiateFlush_argsTupleSchemeFactory implements SchemeFactory {

            public initiateFlush_argsTupleScheme getScheme() {
                return new initiateFlush_argsTupleScheme();
            }
        }

        private static class initiateFlush_argsTupleScheme extends TupleScheme<initiateFlush_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, initiateFlush_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredentials()) {
                    optionals.set(1);
                }
                if (struct.isSetTableName()) {
                    optionals.set(2);
                }
                oprot.writeBitSet(optionals, 3);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredentials()) {
                    struct.credentials.write(oprot);
                }
                if (struct.isSetTableName()) {
                    oprot.writeString(struct.tableName);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, initiateFlush_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(3);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                    struct.credentials.read(iprot);
                    struct.setCredentialsIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.tableName = iprot.readString();
                    struct.setTableNameIsSet(true);
                }
            }
        }
    }

    public static class initiateFlush_result implements org.apache.thrift.TBase<initiateFlush_result, initiateFlush_result._Fields>, java.io.Serializable, Cloneable, Comparable<initiateFlush_result> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("initiateFlush_result");

        private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.I64, (short) 0);

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField TOPE_FIELD_DESC = new org.apache.thrift.protocol.TField("tope", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new initiateFlush_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new initiateFlush_resultTupleSchemeFactory());
        }

        public long success;

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec;

        public org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope;

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
            tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
            tmpMap.put(_Fields.SEC, new org.apache.thrift.meta_data.FieldMetaData("sec", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            tmpMap.put(_Fields.TOPE, new org.apache.thrift.meta_data.FieldMetaData("tope", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(initiateFlush_result.class, metaDataMap);
        }

        public initiateFlush_result() {
        }

        public initiateFlush_result(long success, org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope) {
            this();
            this.success = success;
            setSuccessIsSet(true);
            this.sec = sec;
            this.tope = tope;
        }

        public initiateFlush_result(initiateFlush_result other) {
            __isset_bitfield = other.__isset_bitfield;
            this.success = other.success;
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException(other.sec);
            }
            if (other.isSetTope()) {
                this.tope = new org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException(other.tope);
            }
        }

        public initiateFlush_result deepCopy() {
            return new initiateFlush_result(this);
        }

        @Override
        public void clear() {
            setSuccessIsSet(false);
            this.success = 0;
            this.sec = null;
            this.tope = null;
        }

        public long getSuccess() {
            return this.success;
        }

        public initiateFlush_result setSuccess(long success) {
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

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public initiateFlush_result setSec(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
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

        public org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException getTope() {
            return this.tope;
        }

        public initiateFlush_result setTope(org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope) {
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
                        setSuccess((Long) value);
                    }
                    break;
                case SEC:
                    if (value == null) {
                        unsetSec();
                    } else {
                        setSec((org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) value);
                    }
                    break;
                case TOPE:
                    if (value == null) {
                        unsetTope();
                    } else {
                        setTope((org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SUCCESS:
                    return Long.valueOf(getSuccess());
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
            if (that instanceof initiateFlush_result)
                return this.equals((initiateFlush_result) that);
            return false;
        }

        public boolean equals(initiateFlush_result that) {
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

        @Override
        public int compareTo(initiateFlush_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(other.isSetSuccess());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSuccess()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, other.success);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(other.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, other.sec);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTope()).compareTo(other.isSetTope());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTope()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tope, other.tope);
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
            StringBuilder sb = new StringBuilder("initiateFlush_result(");
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

        private static class initiateFlush_resultStandardSchemeFactory implements SchemeFactory {

            public initiateFlush_resultStandardScheme getScheme() {
                return new initiateFlush_resultStandardScheme();
            }
        }

        private static class initiateFlush_resultStandardScheme extends StandardScheme<initiateFlush_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, initiateFlush_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 0:
                            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                                struct.success = iprot.readI64();
                                struct.setSuccessIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tope = new org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException();
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, initiateFlush_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.isSetSuccess()) {
                    oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
                    oprot.writeI64(struct.success);
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

        private static class initiateFlush_resultTupleSchemeFactory implements SchemeFactory {

            public initiateFlush_resultTupleScheme getScheme() {
                return new initiateFlush_resultTupleScheme();
            }
        }

        private static class initiateFlush_resultTupleScheme extends TupleScheme<initiateFlush_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, initiateFlush_result struct) throws org.apache.thrift.TException {
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
                    oprot.writeI64(struct.success);
                }
                if (struct.isSetSec()) {
                    struct.sec.write(oprot);
                }
                if (struct.isSetTope()) {
                    struct.tope.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, initiateFlush_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(3);
                if (incoming.get(0)) {
                    struct.success = iprot.readI64();
                    struct.setSuccessIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.tope = new org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException();
                    struct.tope.read(iprot);
                    struct.setTopeIsSet(true);
                }
            }
        }
    }

    public static class waitForFlush_args implements org.apache.thrift.TBase<waitForFlush_args, waitForFlush_args._Fields>, java.io.Serializable, Cloneable, Comparable<waitForFlush_args> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("waitForFlush_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 5);

        private static final org.apache.thrift.protocol.TField CREDENTIALS_FIELD_DESC = new org.apache.thrift.protocol.TField("credentials", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField TABLE_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("tableName", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final org.apache.thrift.protocol.TField START_ROW_FIELD_DESC = new org.apache.thrift.protocol.TField("startRow", org.apache.thrift.protocol.TType.STRING, (short) 6);

        private static final org.apache.thrift.protocol.TField END_ROW_FIELD_DESC = new org.apache.thrift.protocol.TField("endRow", org.apache.thrift.protocol.TType.STRING, (short) 7);

        private static final org.apache.thrift.protocol.TField FLUSH_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("flushID", org.apache.thrift.protocol.TType.I64, (short) 3);

        private static final org.apache.thrift.protocol.TField MAX_LOOPS_FIELD_DESC = new org.apache.thrift.protocol.TField("maxLoops", org.apache.thrift.protocol.TType.I64, (short) 4);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new waitForFlush_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new waitForFlush_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.TCredentials credentials;

        public String tableName;

        public ByteBuffer startRow;

        public ByteBuffer endRow;

        public long flushID;

        public long maxLoops;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 5, "tinfo"),
            CREDENTIALS((short) 1, "credentials"),
            TABLE_NAME((short) 2, "tableName"),
            START_ROW((short) 6, "startRow"),
            END_ROW((short) 7, "endRow"),
            FLUSH_ID((short) 3, "flushID"),
            MAX_LOOPS((short) 4, "maxLoops");

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
                        return CREDENTIALS;
                    case 2:
                        return TABLE_NAME;
                    case 6:
                        return START_ROW;
                    case 7:
                        return END_ROW;
                    case 3:
                        return FLUSH_ID;
                    case 4:
                        return MAX_LOOPS;
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

        private static final int __FLUSHID_ISSET_ID = 0;

        private static final int __MAXLOOPS_ISSET_ID = 1;

        private byte __isset_bitfield = 0;

        public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.CREDENTIALS, new org.apache.thrift.meta_data.FieldMetaData("credentials", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.TCredentials.class)));
            tmpMap.put(_Fields.TABLE_NAME, new org.apache.thrift.meta_data.FieldMetaData("tableName", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.START_ROW, new org.apache.thrift.meta_data.FieldMetaData("startRow", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
            tmpMap.put(_Fields.END_ROW, new org.apache.thrift.meta_data.FieldMetaData("endRow", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING, true)));
            tmpMap.put(_Fields.FLUSH_ID, new org.apache.thrift.meta_data.FieldMetaData("flushID", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
            tmpMap.put(_Fields.MAX_LOOPS, new org.apache.thrift.meta_data.FieldMetaData("maxLoops", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(waitForFlush_args.class, metaDataMap);
        }

        public waitForFlush_args() {
        }

        public waitForFlush_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, ByteBuffer startRow, ByteBuffer endRow, long flushID, long maxLoops) {
            this();
            this.tinfo = tinfo;
            this.credentials = credentials;
            this.tableName = tableName;
            this.startRow = startRow;
            this.endRow = endRow;
            this.flushID = flushID;
            setFlushIDIsSet(true);
            this.maxLoops = maxLoops;
            setMaxLoopsIsSet(true);
        }

        public waitForFlush_args(waitForFlush_args other) {
            __isset_bitfield = other.__isset_bitfield;
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredentials()) {
                this.credentials = new org.apache.accumulo.core.security.thrift.TCredentials(other.credentials);
            }
            if (other.isSetTableName()) {
                this.tableName = other.tableName;
            }
            if (other.isSetStartRow()) {
                this.startRow = org.apache.thrift.TBaseHelper.copyBinary(other.startRow);
                ;
            }
            if (other.isSetEndRow()) {
                this.endRow = org.apache.thrift.TBaseHelper.copyBinary(other.endRow);
                ;
            }
            this.flushID = other.flushID;
            this.maxLoops = other.maxLoops;
        }

        public waitForFlush_args deepCopy() {
            return new waitForFlush_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credentials = null;
            this.tableName = null;
            this.startRow = null;
            this.endRow = null;
            setFlushIDIsSet(false);
            this.flushID = 0;
            setMaxLoopsIsSet(false);
            this.maxLoops = 0;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public waitForFlush_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
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

        public org.apache.accumulo.core.security.thrift.TCredentials getCredentials() {
            return this.credentials;
        }

        public waitForFlush_args setCredentials(org.apache.accumulo.core.security.thrift.TCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public void unsetCredentials() {
            this.credentials = null;
        }

        public boolean isSetCredentials() {
            return this.credentials != null;
        }

        public void setCredentialsIsSet(boolean value) {
            if (!value) {
                this.credentials = null;
            }
        }

        public String getTableName() {
            return this.tableName;
        }

        public waitForFlush_args setTableName(String tableName) {
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

        public byte[] getStartRow() {
            setStartRow(org.apache.thrift.TBaseHelper.rightSize(startRow));
            return startRow == null ? null : startRow.array();
        }

        public ByteBuffer bufferForStartRow() {
            return startRow;
        }

        public waitForFlush_args setStartRow(byte[] startRow) {
            setStartRow(startRow == null ? (ByteBuffer) null : ByteBuffer.wrap(startRow));
            return this;
        }

        public waitForFlush_args setStartRow(ByteBuffer startRow) {
            this.startRow = startRow;
            return this;
        }

        public void unsetStartRow() {
            this.startRow = null;
        }

        public boolean isSetStartRow() {
            return this.startRow != null;
        }

        public void setStartRowIsSet(boolean value) {
            if (!value) {
                this.startRow = null;
            }
        }

        public byte[] getEndRow() {
            setEndRow(org.apache.thrift.TBaseHelper.rightSize(endRow));
            return endRow == null ? null : endRow.array();
        }

        public ByteBuffer bufferForEndRow() {
            return endRow;
        }

        public waitForFlush_args setEndRow(byte[] endRow) {
            setEndRow(endRow == null ? (ByteBuffer) null : ByteBuffer.wrap(endRow));
            return this;
        }

        public waitForFlush_args setEndRow(ByteBuffer endRow) {
            this.endRow = endRow;
            return this;
        }

        public void unsetEndRow() {
            this.endRow = null;
        }

        public boolean isSetEndRow() {
            return this.endRow != null;
        }

        public void setEndRowIsSet(boolean value) {
            if (!value) {
                this.endRow = null;
            }
        }

        public long getFlushID() {
            return this.flushID;
        }

        public waitForFlush_args setFlushID(long flushID) {
            this.flushID = flushID;
            setFlushIDIsSet(true);
            return this;
        }

        public void unsetFlushID() {
            __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __FLUSHID_ISSET_ID);
        }

        public boolean isSetFlushID() {
            return EncodingUtils.testBit(__isset_bitfield, __FLUSHID_ISSET_ID);
        }

        public void setFlushIDIsSet(boolean value) {
            __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __FLUSHID_ISSET_ID, value);
        }

        public long getMaxLoops() {
            return this.maxLoops;
        }

        public waitForFlush_args setMaxLoops(long maxLoops) {
            this.maxLoops = maxLoops;
            setMaxLoopsIsSet(true);
            return this;
        }

        public void unsetMaxLoops() {
            __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __MAXLOOPS_ISSET_ID);
        }

        public boolean isSetMaxLoops() {
            return EncodingUtils.testBit(__isset_bitfield, __MAXLOOPS_ISSET_ID);
        }

        public void setMaxLoopsIsSet(boolean value) {
            __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __MAXLOOPS_ISSET_ID, value);
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
                case CREDENTIALS:
                    if (value == null) {
                        unsetCredentials();
                    } else {
                        setCredentials((org.apache.accumulo.core.security.thrift.TCredentials) value);
                    }
                    break;
                case TABLE_NAME:
                    if (value == null) {
                        unsetTableName();
                    } else {
                        setTableName((String) value);
                    }
                    break;
                case START_ROW:
                    if (value == null) {
                        unsetStartRow();
                    } else {
                        setStartRow((ByteBuffer) value);
                    }
                    break;
                case END_ROW:
                    if (value == null) {
                        unsetEndRow();
                    } else {
                        setEndRow((ByteBuffer) value);
                    }
                    break;
                case FLUSH_ID:
                    if (value == null) {
                        unsetFlushID();
                    } else {
                        setFlushID((Long) value);
                    }
                    break;
                case MAX_LOOPS:
                    if (value == null) {
                        unsetMaxLoops();
                    } else {
                        setMaxLoops((Long) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIALS:
                    return getCredentials();
                case TABLE_NAME:
                    return getTableName();
                case START_ROW:
                    return getStartRow();
                case END_ROW:
                    return getEndRow();
                case FLUSH_ID:
                    return Long.valueOf(getFlushID());
                case MAX_LOOPS:
                    return Long.valueOf(getMaxLoops());
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
                case CREDENTIALS:
                    return isSetCredentials();
                case TABLE_NAME:
                    return isSetTableName();
                case START_ROW:
                    return isSetStartRow();
                case END_ROW:
                    return isSetEndRow();
                case FLUSH_ID:
                    return isSetFlushID();
                case MAX_LOOPS:
                    return isSetMaxLoops();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof waitForFlush_args)
                return this.equals((waitForFlush_args) that);
            return false;
        }

        public boolean equals(waitForFlush_args that) {
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
            boolean this_present_credentials = true && this.isSetCredentials();
            boolean that_present_credentials = true && that.isSetCredentials();
            if (this_present_credentials || that_present_credentials) {
                if (!(this_present_credentials && that_present_credentials))
                    return false;
                if (!this.credentials.equals(that.credentials))
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
            boolean this_present_startRow = true && this.isSetStartRow();
            boolean that_present_startRow = true && that.isSetStartRow();
            if (this_present_startRow || that_present_startRow) {
                if (!(this_present_startRow && that_present_startRow))
                    return false;
                if (!this.startRow.equals(that.startRow))
                    return false;
            }
            boolean this_present_endRow = true && this.isSetEndRow();
            boolean that_present_endRow = true && that.isSetEndRow();
            if (this_present_endRow || that_present_endRow) {
                if (!(this_present_endRow && that_present_endRow))
                    return false;
                if (!this.endRow.equals(that.endRow))
                    return false;
            }
            boolean this_present_flushID = true;
            boolean that_present_flushID = true;
            if (this_present_flushID || that_present_flushID) {
                if (!(this_present_flushID && that_present_flushID))
                    return false;
                if (this.flushID != that.flushID)
                    return false;
            }
            boolean this_present_maxLoops = true;
            boolean that_present_maxLoops = true;
            if (this_present_maxLoops || that_present_maxLoops) {
                if (!(this_present_maxLoops && that_present_maxLoops))
                    return false;
                if (this.maxLoops != that.maxLoops)
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public int compareTo(waitForFlush_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(other.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, other.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredentials()).compareTo(other.isSetCredentials());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredentials()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credentials, other.credentials);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTableName()).compareTo(other.isSetTableName());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTableName()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tableName, other.tableName);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetStartRow()).compareTo(other.isSetStartRow());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetStartRow()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.startRow, other.startRow);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetEndRow()).compareTo(other.isSetEndRow());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetEndRow()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.endRow, other.endRow);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetFlushID()).compareTo(other.isSetFlushID());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetFlushID()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.flushID, other.flushID);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetMaxLoops()).compareTo(other.isSetMaxLoops());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetMaxLoops()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.maxLoops, other.maxLoops);
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
            StringBuilder sb = new StringBuilder("waitForFlush_args(");
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
            sb.append("credentials:");
            if (this.credentials == null) {
                sb.append("null");
            } else {
                sb.append(this.credentials);
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
            sb.append("startRow:");
            if (this.startRow == null) {
                sb.append("null");
            } else {
                org.apache.thrift.TBaseHelper.toString(this.startRow, sb);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("endRow:");
            if (this.endRow == null) {
                sb.append("null");
            } else {
                org.apache.thrift.TBaseHelper.toString(this.endRow, sb);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("flushID:");
            sb.append(this.flushID);
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("maxLoops:");
            sb.append(this.maxLoops);
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credentials != null) {
                credentials.validate();
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

        private static class waitForFlush_argsStandardSchemeFactory implements SchemeFactory {

            public waitForFlush_argsStandardScheme getScheme() {
                return new waitForFlush_argsStandardScheme();
            }
        }

        private static class waitForFlush_argsStandardScheme extends StandardScheme<waitForFlush_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, waitForFlush_args struct) throws org.apache.thrift.TException {
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
                                struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                                struct.credentials.read(iprot);
                                struct.setCredentialsIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.tableName = iprot.readString();
                                struct.setTableNameIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 6:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.startRow = iprot.readBinary();
                                struct.setStartRowIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 7:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.endRow = iprot.readBinary();
                                struct.setEndRowIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                                struct.flushID = iprot.readI64();
                                struct.setFlushIDIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 4:
                            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                                struct.maxLoops = iprot.readI64();
                                struct.setMaxLoopsIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, waitForFlush_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credentials != null) {
                    oprot.writeFieldBegin(CREDENTIALS_FIELD_DESC);
                    struct.credentials.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.tableName != null) {
                    oprot.writeFieldBegin(TABLE_NAME_FIELD_DESC);
                    oprot.writeString(struct.tableName);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldBegin(FLUSH_ID_FIELD_DESC);
                oprot.writeI64(struct.flushID);
                oprot.writeFieldEnd();
                oprot.writeFieldBegin(MAX_LOOPS_FIELD_DESC);
                oprot.writeI64(struct.maxLoops);
                oprot.writeFieldEnd();
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.startRow != null) {
                    oprot.writeFieldBegin(START_ROW_FIELD_DESC);
                    oprot.writeBinary(struct.startRow);
                    oprot.writeFieldEnd();
                }
                if (struct.endRow != null) {
                    oprot.writeFieldBegin(END_ROW_FIELD_DESC);
                    oprot.writeBinary(struct.endRow);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class waitForFlush_argsTupleSchemeFactory implements SchemeFactory {

            public waitForFlush_argsTupleScheme getScheme() {
                return new waitForFlush_argsTupleScheme();
            }
        }

        private static class waitForFlush_argsTupleScheme extends TupleScheme<waitForFlush_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, waitForFlush_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredentials()) {
                    optionals.set(1);
                }
                if (struct.isSetTableName()) {
                    optionals.set(2);
                }
                if (struct.isSetStartRow()) {
                    optionals.set(3);
                }
                if (struct.isSetEndRow()) {
                    optionals.set(4);
                }
                if (struct.isSetFlushID()) {
                    optionals.set(5);
                }
                if (struct.isSetMaxLoops()) {
                    optionals.set(6);
                }
                oprot.writeBitSet(optionals, 7);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredentials()) {
                    struct.credentials.write(oprot);
                }
                if (struct.isSetTableName()) {
                    oprot.writeString(struct.tableName);
                }
                if (struct.isSetStartRow()) {
                    oprot.writeBinary(struct.startRow);
                }
                if (struct.isSetEndRow()) {
                    oprot.writeBinary(struct.endRow);
                }
                if (struct.isSetFlushID()) {
                    oprot.writeI64(struct.flushID);
                }
                if (struct.isSetMaxLoops()) {
                    oprot.writeI64(struct.maxLoops);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, waitForFlush_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(7);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                    struct.credentials.read(iprot);
                    struct.setCredentialsIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.tableName = iprot.readString();
                    struct.setTableNameIsSet(true);
                }
                if (incoming.get(3)) {
                    struct.startRow = iprot.readBinary();
                    struct.setStartRowIsSet(true);
                }
                if (incoming.get(4)) {
                    struct.endRow = iprot.readBinary();
                    struct.setEndRowIsSet(true);
                }
                if (incoming.get(5)) {
                    struct.flushID = iprot.readI64();
                    struct.setFlushIDIsSet(true);
                }
                if (incoming.get(6)) {
                    struct.maxLoops = iprot.readI64();
                    struct.setMaxLoopsIsSet(true);
                }
            }
        }
    }

    public static class waitForFlush_result implements org.apache.thrift.TBase<waitForFlush_result, waitForFlush_result._Fields>, java.io.Serializable, Cloneable, Comparable<waitForFlush_result> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("waitForFlush_result");

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField TOPE_FIELD_DESC = new org.apache.thrift.protocol.TField("tope", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new waitForFlush_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new waitForFlush_resultTupleSchemeFactory());
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec;

        public org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope;

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
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(waitForFlush_result.class, metaDataMap);
        }

        public waitForFlush_result() {
        }

        public waitForFlush_result(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope) {
            this();
            this.sec = sec;
            this.tope = tope;
        }

        public waitForFlush_result(waitForFlush_result other) {
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException(other.sec);
            }
            if (other.isSetTope()) {
                this.tope = new org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException(other.tope);
            }
        }

        public waitForFlush_result deepCopy() {
            return new waitForFlush_result(this);
        }

        @Override
        public void clear() {
            this.sec = null;
            this.tope = null;
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public waitForFlush_result setSec(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
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

        public org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException getTope() {
            return this.tope;
        }

        public waitForFlush_result setTope(org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope) {
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
                        setSec((org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) value);
                    }
                    break;
                case TOPE:
                    if (value == null) {
                        unsetTope();
                    } else {
                        setTope((org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException) value);
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
            if (that instanceof waitForFlush_result)
                return this.equals((waitForFlush_result) that);
            return false;
        }

        public boolean equals(waitForFlush_result that) {
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

        @Override
        public int compareTo(waitForFlush_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(other.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, other.sec);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTope()).compareTo(other.isSetTope());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTope()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tope, other.tope);
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
            StringBuilder sb = new StringBuilder("waitForFlush_result(");
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

        private static class waitForFlush_resultStandardSchemeFactory implements SchemeFactory {

            public waitForFlush_resultStandardScheme getScheme() {
                return new waitForFlush_resultStandardScheme();
            }
        }

        private static class waitForFlush_resultStandardScheme extends StandardScheme<waitForFlush_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, waitForFlush_result struct) throws org.apache.thrift.TException {
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
                                struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tope = new org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException();
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, waitForFlush_result struct) throws org.apache.thrift.TException {
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

        private static class waitForFlush_resultTupleSchemeFactory implements SchemeFactory {

            public waitForFlush_resultTupleScheme getScheme() {
                return new waitForFlush_resultTupleScheme();
            }
        }

        private static class waitForFlush_resultTupleScheme extends TupleScheme<waitForFlush_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, waitForFlush_result struct) throws org.apache.thrift.TException {
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
            public void read(org.apache.thrift.protocol.TProtocol prot, waitForFlush_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.tope = new org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException();
                    struct.tope.read(iprot);
                    struct.setTopeIsSet(true);
                }
            }
        }
    }

    public static class setTableProperty_args implements org.apache.thrift.TBase<setTableProperty_args, setTableProperty_args._Fields>, java.io.Serializable, Cloneable, Comparable<setTableProperty_args> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("setTableProperty_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 5);

        private static final org.apache.thrift.protocol.TField CREDENTIALS_FIELD_DESC = new org.apache.thrift.protocol.TField("credentials", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField TABLE_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("tableName", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final org.apache.thrift.protocol.TField PROPERTY_FIELD_DESC = new org.apache.thrift.protocol.TField("property", org.apache.thrift.protocol.TType.STRING, (short) 3);

        private static final org.apache.thrift.protocol.TField VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("value", org.apache.thrift.protocol.TType.STRING, (short) 4);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new setTableProperty_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new setTableProperty_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.TCredentials credentials;

        public String tableName;

        public String property;

        public String value;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 5, "tinfo"), CREDENTIALS((short) 1, "credentials"), TABLE_NAME((short) 2, "tableName"), PROPERTY((short) 3, "property"), VALUE((short) 4, "value");

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
                        return CREDENTIALS;
                    case 2:
                        return TABLE_NAME;
                    case 3:
                        return PROPERTY;
                    case 4:
                        return VALUE;
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
            tmpMap.put(_Fields.CREDENTIALS, new org.apache.thrift.meta_data.FieldMetaData("credentials", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.TCredentials.class)));
            tmpMap.put(_Fields.TABLE_NAME, new org.apache.thrift.meta_data.FieldMetaData("tableName", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.PROPERTY, new org.apache.thrift.meta_data.FieldMetaData("property", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.VALUE, new org.apache.thrift.meta_data.FieldMetaData("value", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(setTableProperty_args.class, metaDataMap);
        }

        public setTableProperty_args() {
        }

        public setTableProperty_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, String property, String value) {
            this();
            this.tinfo = tinfo;
            this.credentials = credentials;
            this.tableName = tableName;
            this.property = property;
            this.value = value;
        }

        public setTableProperty_args(setTableProperty_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredentials()) {
                this.credentials = new org.apache.accumulo.core.security.thrift.TCredentials(other.credentials);
            }
            if (other.isSetTableName()) {
                this.tableName = other.tableName;
            }
            if (other.isSetProperty()) {
                this.property = other.property;
            }
            if (other.isSetValue()) {
                this.value = other.value;
            }
        }

        public setTableProperty_args deepCopy() {
            return new setTableProperty_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credentials = null;
            this.tableName = null;
            this.property = null;
            this.value = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public setTableProperty_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
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

        public org.apache.accumulo.core.security.thrift.TCredentials getCredentials() {
            return this.credentials;
        }

        public setTableProperty_args setCredentials(org.apache.accumulo.core.security.thrift.TCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public void unsetCredentials() {
            this.credentials = null;
        }

        public boolean isSetCredentials() {
            return this.credentials != null;
        }

        public void setCredentialsIsSet(boolean value) {
            if (!value) {
                this.credentials = null;
            }
        }

        public String getTableName() {
            return this.tableName;
        }

        public setTableProperty_args setTableName(String tableName) {
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

        public String getProperty() {
            return this.property;
        }

        public setTableProperty_args setProperty(String property) {
            this.property = property;
            return this;
        }

        public void unsetProperty() {
            this.property = null;
        }

        public boolean isSetProperty() {
            return this.property != null;
        }

        public void setPropertyIsSet(boolean value) {
            if (!value) {
                this.property = null;
            }
        }

        public String getValue() {
            return this.value;
        }

        public setTableProperty_args setValue(String value) {
            this.value = value;
            return this;
        }

        public void unsetValue() {
            this.value = null;
        }

        public boolean isSetValue() {
            return this.value != null;
        }

        public void setValueIsSet(boolean value) {
            if (!value) {
                this.value = null;
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
                case CREDENTIALS:
                    if (value == null) {
                        unsetCredentials();
                    } else {
                        setCredentials((org.apache.accumulo.core.security.thrift.TCredentials) value);
                    }
                    break;
                case TABLE_NAME:
                    if (value == null) {
                        unsetTableName();
                    } else {
                        setTableName((String) value);
                    }
                    break;
                case PROPERTY:
                    if (value == null) {
                        unsetProperty();
                    } else {
                        setProperty((String) value);
                    }
                    break;
                case VALUE:
                    if (value == null) {
                        unsetValue();
                    } else {
                        setValue((String) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIALS:
                    return getCredentials();
                case TABLE_NAME:
                    return getTableName();
                case PROPERTY:
                    return getProperty();
                case VALUE:
                    return getValue();
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
                case CREDENTIALS:
                    return isSetCredentials();
                case TABLE_NAME:
                    return isSetTableName();
                case PROPERTY:
                    return isSetProperty();
                case VALUE:
                    return isSetValue();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof setTableProperty_args)
                return this.equals((setTableProperty_args) that);
            return false;
        }

        public boolean equals(setTableProperty_args that) {
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
            boolean this_present_credentials = true && this.isSetCredentials();
            boolean that_present_credentials = true && that.isSetCredentials();
            if (this_present_credentials || that_present_credentials) {
                if (!(this_present_credentials && that_present_credentials))
                    return false;
                if (!this.credentials.equals(that.credentials))
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
            boolean this_present_property = true && this.isSetProperty();
            boolean that_present_property = true && that.isSetProperty();
            if (this_present_property || that_present_property) {
                if (!(this_present_property && that_present_property))
                    return false;
                if (!this.property.equals(that.property))
                    return false;
            }
            boolean this_present_value = true && this.isSetValue();
            boolean that_present_value = true && that.isSetValue();
            if (this_present_value || that_present_value) {
                if (!(this_present_value && that_present_value))
                    return false;
                if (!this.value.equals(that.value))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public int compareTo(setTableProperty_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(other.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, other.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredentials()).compareTo(other.isSetCredentials());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredentials()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credentials, other.credentials);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTableName()).compareTo(other.isSetTableName());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTableName()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tableName, other.tableName);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetProperty()).compareTo(other.isSetProperty());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetProperty()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.property, other.property);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetValue()).compareTo(other.isSetValue());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetValue()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.value, other.value);
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
            StringBuilder sb = new StringBuilder("setTableProperty_args(");
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
            sb.append("credentials:");
            if (this.credentials == null) {
                sb.append("null");
            } else {
                sb.append(this.credentials);
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
            sb.append("property:");
            if (this.property == null) {
                sb.append("null");
            } else {
                sb.append(this.property);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("value:");
            if (this.value == null) {
                sb.append("null");
            } else {
                sb.append(this.value);
            }
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credentials != null) {
                credentials.validate();
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

        private static class setTableProperty_argsStandardSchemeFactory implements SchemeFactory {

            public setTableProperty_argsStandardScheme getScheme() {
                return new setTableProperty_argsStandardScheme();
            }
        }

        private static class setTableProperty_argsStandardScheme extends StandardScheme<setTableProperty_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, setTableProperty_args struct) throws org.apache.thrift.TException {
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
                                struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                                struct.credentials.read(iprot);
                                struct.setCredentialsIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.tableName = iprot.readString();
                                struct.setTableNameIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.property = iprot.readString();
                                struct.setPropertyIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 4:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.value = iprot.readString();
                                struct.setValueIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, setTableProperty_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credentials != null) {
                    oprot.writeFieldBegin(CREDENTIALS_FIELD_DESC);
                    struct.credentials.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.tableName != null) {
                    oprot.writeFieldBegin(TABLE_NAME_FIELD_DESC);
                    oprot.writeString(struct.tableName);
                    oprot.writeFieldEnd();
                }
                if (struct.property != null) {
                    oprot.writeFieldBegin(PROPERTY_FIELD_DESC);
                    oprot.writeString(struct.property);
                    oprot.writeFieldEnd();
                }
                if (struct.value != null) {
                    oprot.writeFieldBegin(VALUE_FIELD_DESC);
                    oprot.writeString(struct.value);
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

        private static class setTableProperty_argsTupleSchemeFactory implements SchemeFactory {

            public setTableProperty_argsTupleScheme getScheme() {
                return new setTableProperty_argsTupleScheme();
            }
        }

        private static class setTableProperty_argsTupleScheme extends TupleScheme<setTableProperty_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, setTableProperty_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredentials()) {
                    optionals.set(1);
                }
                if (struct.isSetTableName()) {
                    optionals.set(2);
                }
                if (struct.isSetProperty()) {
                    optionals.set(3);
                }
                if (struct.isSetValue()) {
                    optionals.set(4);
                }
                oprot.writeBitSet(optionals, 5);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredentials()) {
                    struct.credentials.write(oprot);
                }
                if (struct.isSetTableName()) {
                    oprot.writeString(struct.tableName);
                }
                if (struct.isSetProperty()) {
                    oprot.writeString(struct.property);
                }
                if (struct.isSetValue()) {
                    oprot.writeString(struct.value);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, setTableProperty_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(5);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                    struct.credentials.read(iprot);
                    struct.setCredentialsIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.tableName = iprot.readString();
                    struct.setTableNameIsSet(true);
                }
                if (incoming.get(3)) {
                    struct.property = iprot.readString();
                    struct.setPropertyIsSet(true);
                }
                if (incoming.get(4)) {
                    struct.value = iprot.readString();
                    struct.setValueIsSet(true);
                }
            }
        }
    }

    public static class setTableProperty_result implements org.apache.thrift.TBase<setTableProperty_result, setTableProperty_result._Fields>, java.io.Serializable, Cloneable, Comparable<setTableProperty_result> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("setTableProperty_result");

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField TOPE_FIELD_DESC = new org.apache.thrift.protocol.TField("tope", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new setTableProperty_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new setTableProperty_resultTupleSchemeFactory());
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec;

        public org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope;

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
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(setTableProperty_result.class, metaDataMap);
        }

        public setTableProperty_result() {
        }

        public setTableProperty_result(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope) {
            this();
            this.sec = sec;
            this.tope = tope;
        }

        public setTableProperty_result(setTableProperty_result other) {
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException(other.sec);
            }
            if (other.isSetTope()) {
                this.tope = new org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException(other.tope);
            }
        }

        public setTableProperty_result deepCopy() {
            return new setTableProperty_result(this);
        }

        @Override
        public void clear() {
            this.sec = null;
            this.tope = null;
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public setTableProperty_result setSec(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
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

        public org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException getTope() {
            return this.tope;
        }

        public setTableProperty_result setTope(org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope) {
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
                        setSec((org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) value);
                    }
                    break;
                case TOPE:
                    if (value == null) {
                        unsetTope();
                    } else {
                        setTope((org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException) value);
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
            if (that instanceof setTableProperty_result)
                return this.equals((setTableProperty_result) that);
            return false;
        }

        public boolean equals(setTableProperty_result that) {
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

        @Override
        public int compareTo(setTableProperty_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(other.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, other.sec);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTope()).compareTo(other.isSetTope());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTope()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tope, other.tope);
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
            StringBuilder sb = new StringBuilder("setTableProperty_result(");
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

        private static class setTableProperty_resultStandardSchemeFactory implements SchemeFactory {

            public setTableProperty_resultStandardScheme getScheme() {
                return new setTableProperty_resultStandardScheme();
            }
        }

        private static class setTableProperty_resultStandardScheme extends StandardScheme<setTableProperty_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, setTableProperty_result struct) throws org.apache.thrift.TException {
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
                                struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tope = new org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException();
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, setTableProperty_result struct) throws org.apache.thrift.TException {
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

        private static class setTableProperty_resultTupleSchemeFactory implements SchemeFactory {

            public setTableProperty_resultTupleScheme getScheme() {
                return new setTableProperty_resultTupleScheme();
            }
        }

        private static class setTableProperty_resultTupleScheme extends TupleScheme<setTableProperty_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, setTableProperty_result struct) throws org.apache.thrift.TException {
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
            public void read(org.apache.thrift.protocol.TProtocol prot, setTableProperty_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.tope = new org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException();
                    struct.tope.read(iprot);
                    struct.setTopeIsSet(true);
                }
            }
        }
    }

    public static class removeTableProperty_args implements org.apache.thrift.TBase<removeTableProperty_args, removeTableProperty_args._Fields>, java.io.Serializable, Cloneable, Comparable<removeTableProperty_args> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("removeTableProperty_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 4);

        private static final org.apache.thrift.protocol.TField CREDENTIALS_FIELD_DESC = new org.apache.thrift.protocol.TField("credentials", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField TABLE_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("tableName", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final org.apache.thrift.protocol.TField PROPERTY_FIELD_DESC = new org.apache.thrift.protocol.TField("property", org.apache.thrift.protocol.TType.STRING, (short) 3);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new removeTableProperty_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new removeTableProperty_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.TCredentials credentials;

        public String tableName;

        public String property;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 4, "tinfo"), CREDENTIALS((short) 1, "credentials"), TABLE_NAME((short) 2, "tableName"), PROPERTY((short) 3, "property");

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
                        return CREDENTIALS;
                    case 2:
                        return TABLE_NAME;
                    case 3:
                        return PROPERTY;
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
            tmpMap.put(_Fields.CREDENTIALS, new org.apache.thrift.meta_data.FieldMetaData("credentials", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.TCredentials.class)));
            tmpMap.put(_Fields.TABLE_NAME, new org.apache.thrift.meta_data.FieldMetaData("tableName", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.PROPERTY, new org.apache.thrift.meta_data.FieldMetaData("property", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(removeTableProperty_args.class, metaDataMap);
        }

        public removeTableProperty_args() {
        }

        public removeTableProperty_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tableName, String property) {
            this();
            this.tinfo = tinfo;
            this.credentials = credentials;
            this.tableName = tableName;
            this.property = property;
        }

        public removeTableProperty_args(removeTableProperty_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredentials()) {
                this.credentials = new org.apache.accumulo.core.security.thrift.TCredentials(other.credentials);
            }
            if (other.isSetTableName()) {
                this.tableName = other.tableName;
            }
            if (other.isSetProperty()) {
                this.property = other.property;
            }
        }

        public removeTableProperty_args deepCopy() {
            return new removeTableProperty_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credentials = null;
            this.tableName = null;
            this.property = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public removeTableProperty_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
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

        public org.apache.accumulo.core.security.thrift.TCredentials getCredentials() {
            return this.credentials;
        }

        public removeTableProperty_args setCredentials(org.apache.accumulo.core.security.thrift.TCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public void unsetCredentials() {
            this.credentials = null;
        }

        public boolean isSetCredentials() {
            return this.credentials != null;
        }

        public void setCredentialsIsSet(boolean value) {
            if (!value) {
                this.credentials = null;
            }
        }

        public String getTableName() {
            return this.tableName;
        }

        public removeTableProperty_args setTableName(String tableName) {
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

        public String getProperty() {
            return this.property;
        }

        public removeTableProperty_args setProperty(String property) {
            this.property = property;
            return this;
        }

        public void unsetProperty() {
            this.property = null;
        }

        public boolean isSetProperty() {
            return this.property != null;
        }

        public void setPropertyIsSet(boolean value) {
            if (!value) {
                this.property = null;
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
                case CREDENTIALS:
                    if (value == null) {
                        unsetCredentials();
                    } else {
                        setCredentials((org.apache.accumulo.core.security.thrift.TCredentials) value);
                    }
                    break;
                case TABLE_NAME:
                    if (value == null) {
                        unsetTableName();
                    } else {
                        setTableName((String) value);
                    }
                    break;
                case PROPERTY:
                    if (value == null) {
                        unsetProperty();
                    } else {
                        setProperty((String) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIALS:
                    return getCredentials();
                case TABLE_NAME:
                    return getTableName();
                case PROPERTY:
                    return getProperty();
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
                case CREDENTIALS:
                    return isSetCredentials();
                case TABLE_NAME:
                    return isSetTableName();
                case PROPERTY:
                    return isSetProperty();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof removeTableProperty_args)
                return this.equals((removeTableProperty_args) that);
            return false;
        }

        public boolean equals(removeTableProperty_args that) {
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
            boolean this_present_credentials = true && this.isSetCredentials();
            boolean that_present_credentials = true && that.isSetCredentials();
            if (this_present_credentials || that_present_credentials) {
                if (!(this_present_credentials && that_present_credentials))
                    return false;
                if (!this.credentials.equals(that.credentials))
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
            boolean this_present_property = true && this.isSetProperty();
            boolean that_present_property = true && that.isSetProperty();
            if (this_present_property || that_present_property) {
                if (!(this_present_property && that_present_property))
                    return false;
                if (!this.property.equals(that.property))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public int compareTo(removeTableProperty_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(other.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, other.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredentials()).compareTo(other.isSetCredentials());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredentials()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credentials, other.credentials);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTableName()).compareTo(other.isSetTableName());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTableName()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tableName, other.tableName);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetProperty()).compareTo(other.isSetProperty());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetProperty()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.property, other.property);
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
            StringBuilder sb = new StringBuilder("removeTableProperty_args(");
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
            sb.append("credentials:");
            if (this.credentials == null) {
                sb.append("null");
            } else {
                sb.append(this.credentials);
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
            sb.append("property:");
            if (this.property == null) {
                sb.append("null");
            } else {
                sb.append(this.property);
            }
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credentials != null) {
                credentials.validate();
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

        private static class removeTableProperty_argsStandardSchemeFactory implements SchemeFactory {

            public removeTableProperty_argsStandardScheme getScheme() {
                return new removeTableProperty_argsStandardScheme();
            }
        }

        private static class removeTableProperty_argsStandardScheme extends StandardScheme<removeTableProperty_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, removeTableProperty_args struct) throws org.apache.thrift.TException {
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
                                struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                                struct.credentials.read(iprot);
                                struct.setCredentialsIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.tableName = iprot.readString();
                                struct.setTableNameIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.property = iprot.readString();
                                struct.setPropertyIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, removeTableProperty_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credentials != null) {
                    oprot.writeFieldBegin(CREDENTIALS_FIELD_DESC);
                    struct.credentials.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.tableName != null) {
                    oprot.writeFieldBegin(TABLE_NAME_FIELD_DESC);
                    oprot.writeString(struct.tableName);
                    oprot.writeFieldEnd();
                }
                if (struct.property != null) {
                    oprot.writeFieldBegin(PROPERTY_FIELD_DESC);
                    oprot.writeString(struct.property);
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

        private static class removeTableProperty_argsTupleSchemeFactory implements SchemeFactory {

            public removeTableProperty_argsTupleScheme getScheme() {
                return new removeTableProperty_argsTupleScheme();
            }
        }

        private static class removeTableProperty_argsTupleScheme extends TupleScheme<removeTableProperty_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, removeTableProperty_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredentials()) {
                    optionals.set(1);
                }
                if (struct.isSetTableName()) {
                    optionals.set(2);
                }
                if (struct.isSetProperty()) {
                    optionals.set(3);
                }
                oprot.writeBitSet(optionals, 4);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredentials()) {
                    struct.credentials.write(oprot);
                }
                if (struct.isSetTableName()) {
                    oprot.writeString(struct.tableName);
                }
                if (struct.isSetProperty()) {
                    oprot.writeString(struct.property);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, removeTableProperty_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(4);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                    struct.credentials.read(iprot);
                    struct.setCredentialsIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.tableName = iprot.readString();
                    struct.setTableNameIsSet(true);
                }
                if (incoming.get(3)) {
                    struct.property = iprot.readString();
                    struct.setPropertyIsSet(true);
                }
            }
        }
    }

    public static class removeTableProperty_result implements org.apache.thrift.TBase<removeTableProperty_result, removeTableProperty_result._Fields>, java.io.Serializable, Cloneable, Comparable<removeTableProperty_result> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("removeTableProperty_result");

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField TOPE_FIELD_DESC = new org.apache.thrift.protocol.TField("tope", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new removeTableProperty_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new removeTableProperty_resultTupleSchemeFactory());
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec;

        public org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope;

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
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(removeTableProperty_result.class, metaDataMap);
        }

        public removeTableProperty_result() {
        }

        public removeTableProperty_result(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope) {
            this();
            this.sec = sec;
            this.tope = tope;
        }

        public removeTableProperty_result(removeTableProperty_result other) {
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException(other.sec);
            }
            if (other.isSetTope()) {
                this.tope = new org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException(other.tope);
            }
        }

        public removeTableProperty_result deepCopy() {
            return new removeTableProperty_result(this);
        }

        @Override
        public void clear() {
            this.sec = null;
            this.tope = null;
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public removeTableProperty_result setSec(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
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

        public org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException getTope() {
            return this.tope;
        }

        public removeTableProperty_result setTope(org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope) {
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
                        setSec((org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) value);
                    }
                    break;
                case TOPE:
                    if (value == null) {
                        unsetTope();
                    } else {
                        setTope((org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException) value);
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
            if (that instanceof removeTableProperty_result)
                return this.equals((removeTableProperty_result) that);
            return false;
        }

        public boolean equals(removeTableProperty_result that) {
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

        @Override
        public int compareTo(removeTableProperty_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(other.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, other.sec);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTope()).compareTo(other.isSetTope());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTope()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tope, other.tope);
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
            StringBuilder sb = new StringBuilder("removeTableProperty_result(");
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

        private static class removeTableProperty_resultStandardSchemeFactory implements SchemeFactory {

            public removeTableProperty_resultStandardScheme getScheme() {
                return new removeTableProperty_resultStandardScheme();
            }
        }

        private static class removeTableProperty_resultStandardScheme extends StandardScheme<removeTableProperty_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, removeTableProperty_result struct) throws org.apache.thrift.TException {
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
                                struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tope = new org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException();
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, removeTableProperty_result struct) throws org.apache.thrift.TException {
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

        private static class removeTableProperty_resultTupleSchemeFactory implements SchemeFactory {

            public removeTableProperty_resultTupleScheme getScheme() {
                return new removeTableProperty_resultTupleScheme();
            }
        }

        private static class removeTableProperty_resultTupleScheme extends TupleScheme<removeTableProperty_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, removeTableProperty_result struct) throws org.apache.thrift.TException {
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
            public void read(org.apache.thrift.protocol.TProtocol prot, removeTableProperty_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.tope = new org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException();
                    struct.tope.read(iprot);
                    struct.setTopeIsSet(true);
                }
            }
        }
    }

    public static class setNamespaceProperty_args implements org.apache.thrift.TBase<setNamespaceProperty_args, setNamespaceProperty_args._Fields>, java.io.Serializable, Cloneable, Comparable<setNamespaceProperty_args> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("setNamespaceProperty_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 5);

        private static final org.apache.thrift.protocol.TField CREDENTIALS_FIELD_DESC = new org.apache.thrift.protocol.TField("credentials", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField NS_FIELD_DESC = new org.apache.thrift.protocol.TField("ns", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final org.apache.thrift.protocol.TField PROPERTY_FIELD_DESC = new org.apache.thrift.protocol.TField("property", org.apache.thrift.protocol.TType.STRING, (short) 3);

        private static final org.apache.thrift.protocol.TField VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("value", org.apache.thrift.protocol.TType.STRING, (short) 4);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new setNamespaceProperty_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new setNamespaceProperty_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.TCredentials credentials;

        public String ns;

        public String property;

        public String value;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 5, "tinfo"), CREDENTIALS((short) 1, "credentials"), NS((short) 2, "ns"), PROPERTY((short) 3, "property"), VALUE((short) 4, "value");

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
                        return CREDENTIALS;
                    case 2:
                        return NS;
                    case 3:
                        return PROPERTY;
                    case 4:
                        return VALUE;
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
            tmpMap.put(_Fields.CREDENTIALS, new org.apache.thrift.meta_data.FieldMetaData("credentials", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.TCredentials.class)));
            tmpMap.put(_Fields.NS, new org.apache.thrift.meta_data.FieldMetaData("ns", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.PROPERTY, new org.apache.thrift.meta_data.FieldMetaData("property", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.VALUE, new org.apache.thrift.meta_data.FieldMetaData("value", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(setNamespaceProperty_args.class, metaDataMap);
        }

        public setNamespaceProperty_args() {
        }

        public setNamespaceProperty_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String ns, String property, String value) {
            this();
            this.tinfo = tinfo;
            this.credentials = credentials;
            this.ns = ns;
            this.property = property;
            this.value = value;
        }

        public setNamespaceProperty_args(setNamespaceProperty_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredentials()) {
                this.credentials = new org.apache.accumulo.core.security.thrift.TCredentials(other.credentials);
            }
            if (other.isSetNs()) {
                this.ns = other.ns;
            }
            if (other.isSetProperty()) {
                this.property = other.property;
            }
            if (other.isSetValue()) {
                this.value = other.value;
            }
        }

        public setNamespaceProperty_args deepCopy() {
            return new setNamespaceProperty_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credentials = null;
            this.ns = null;
            this.property = null;
            this.value = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public setNamespaceProperty_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
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

        public org.apache.accumulo.core.security.thrift.TCredentials getCredentials() {
            return this.credentials;
        }

        public setNamespaceProperty_args setCredentials(org.apache.accumulo.core.security.thrift.TCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public void unsetCredentials() {
            this.credentials = null;
        }

        public boolean isSetCredentials() {
            return this.credentials != null;
        }

        public void setCredentialsIsSet(boolean value) {
            if (!value) {
                this.credentials = null;
            }
        }

        public String getNs() {
            return this.ns;
        }

        public setNamespaceProperty_args setNs(String ns) {
            this.ns = ns;
            return this;
        }

        public void unsetNs() {
            this.ns = null;
        }

        public boolean isSetNs() {
            return this.ns != null;
        }

        public void setNsIsSet(boolean value) {
            if (!value) {
                this.ns = null;
            }
        }

        public String getProperty() {
            return this.property;
        }

        public setNamespaceProperty_args setProperty(String property) {
            this.property = property;
            return this;
        }

        public void unsetProperty() {
            this.property = null;
        }

        public boolean isSetProperty() {
            return this.property != null;
        }

        public void setPropertyIsSet(boolean value) {
            if (!value) {
                this.property = null;
            }
        }

        public String getValue() {
            return this.value;
        }

        public setNamespaceProperty_args setValue(String value) {
            this.value = value;
            return this;
        }

        public void unsetValue() {
            this.value = null;
        }

        public boolean isSetValue() {
            return this.value != null;
        }

        public void setValueIsSet(boolean value) {
            if (!value) {
                this.value = null;
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
                case CREDENTIALS:
                    if (value == null) {
                        unsetCredentials();
                    } else {
                        setCredentials((org.apache.accumulo.core.security.thrift.TCredentials) value);
                    }
                    break;
                case NS:
                    if (value == null) {
                        unsetNs();
                    } else {
                        setNs((String) value);
                    }
                    break;
                case PROPERTY:
                    if (value == null) {
                        unsetProperty();
                    } else {
                        setProperty((String) value);
                    }
                    break;
                case VALUE:
                    if (value == null) {
                        unsetValue();
                    } else {
                        setValue((String) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIALS:
                    return getCredentials();
                case NS:
                    return getNs();
                case PROPERTY:
                    return getProperty();
                case VALUE:
                    return getValue();
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
                case CREDENTIALS:
                    return isSetCredentials();
                case NS:
                    return isSetNs();
                case PROPERTY:
                    return isSetProperty();
                case VALUE:
                    return isSetValue();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof setNamespaceProperty_args)
                return this.equals((setNamespaceProperty_args) that);
            return false;
        }

        public boolean equals(setNamespaceProperty_args that) {
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
            boolean this_present_credentials = true && this.isSetCredentials();
            boolean that_present_credentials = true && that.isSetCredentials();
            if (this_present_credentials || that_present_credentials) {
                if (!(this_present_credentials && that_present_credentials))
                    return false;
                if (!this.credentials.equals(that.credentials))
                    return false;
            }
            boolean this_present_ns = true && this.isSetNs();
            boolean that_present_ns = true && that.isSetNs();
            if (this_present_ns || that_present_ns) {
                if (!(this_present_ns && that_present_ns))
                    return false;
                if (!this.ns.equals(that.ns))
                    return false;
            }
            boolean this_present_property = true && this.isSetProperty();
            boolean that_present_property = true && that.isSetProperty();
            if (this_present_property || that_present_property) {
                if (!(this_present_property && that_present_property))
                    return false;
                if (!this.property.equals(that.property))
                    return false;
            }
            boolean this_present_value = true && this.isSetValue();
            boolean that_present_value = true && that.isSetValue();
            if (this_present_value || that_present_value) {
                if (!(this_present_value && that_present_value))
                    return false;
                if (!this.value.equals(that.value))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public int compareTo(setNamespaceProperty_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(other.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, other.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredentials()).compareTo(other.isSetCredentials());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredentials()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credentials, other.credentials);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetNs()).compareTo(other.isSetNs());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetNs()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ns, other.ns);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetProperty()).compareTo(other.isSetProperty());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetProperty()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.property, other.property);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetValue()).compareTo(other.isSetValue());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetValue()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.value, other.value);
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
            StringBuilder sb = new StringBuilder("setNamespaceProperty_args(");
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
            sb.append("credentials:");
            if (this.credentials == null) {
                sb.append("null");
            } else {
                sb.append(this.credentials);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("ns:");
            if (this.ns == null) {
                sb.append("null");
            } else {
                sb.append(this.ns);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("property:");
            if (this.property == null) {
                sb.append("null");
            } else {
                sb.append(this.property);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("value:");
            if (this.value == null) {
                sb.append("null");
            } else {
                sb.append(this.value);
            }
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credentials != null) {
                credentials.validate();
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

        private static class setNamespaceProperty_argsStandardSchemeFactory implements SchemeFactory {

            public setNamespaceProperty_argsStandardScheme getScheme() {
                return new setNamespaceProperty_argsStandardScheme();
            }
        }

        private static class setNamespaceProperty_argsStandardScheme extends StandardScheme<setNamespaceProperty_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, setNamespaceProperty_args struct) throws org.apache.thrift.TException {
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
                                struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                                struct.credentials.read(iprot);
                                struct.setCredentialsIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.ns = iprot.readString();
                                struct.setNsIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.property = iprot.readString();
                                struct.setPropertyIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 4:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.value = iprot.readString();
                                struct.setValueIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, setNamespaceProperty_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credentials != null) {
                    oprot.writeFieldBegin(CREDENTIALS_FIELD_DESC);
                    struct.credentials.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.ns != null) {
                    oprot.writeFieldBegin(NS_FIELD_DESC);
                    oprot.writeString(struct.ns);
                    oprot.writeFieldEnd();
                }
                if (struct.property != null) {
                    oprot.writeFieldBegin(PROPERTY_FIELD_DESC);
                    oprot.writeString(struct.property);
                    oprot.writeFieldEnd();
                }
                if (struct.value != null) {
                    oprot.writeFieldBegin(VALUE_FIELD_DESC);
                    oprot.writeString(struct.value);
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

        private static class setNamespaceProperty_argsTupleSchemeFactory implements SchemeFactory {

            public setNamespaceProperty_argsTupleScheme getScheme() {
                return new setNamespaceProperty_argsTupleScheme();
            }
        }

        private static class setNamespaceProperty_argsTupleScheme extends TupleScheme<setNamespaceProperty_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, setNamespaceProperty_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredentials()) {
                    optionals.set(1);
                }
                if (struct.isSetNs()) {
                    optionals.set(2);
                }
                if (struct.isSetProperty()) {
                    optionals.set(3);
                }
                if (struct.isSetValue()) {
                    optionals.set(4);
                }
                oprot.writeBitSet(optionals, 5);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredentials()) {
                    struct.credentials.write(oprot);
                }
                if (struct.isSetNs()) {
                    oprot.writeString(struct.ns);
                }
                if (struct.isSetProperty()) {
                    oprot.writeString(struct.property);
                }
                if (struct.isSetValue()) {
                    oprot.writeString(struct.value);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, setNamespaceProperty_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(5);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                    struct.credentials.read(iprot);
                    struct.setCredentialsIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.ns = iprot.readString();
                    struct.setNsIsSet(true);
                }
                if (incoming.get(3)) {
                    struct.property = iprot.readString();
                    struct.setPropertyIsSet(true);
                }
                if (incoming.get(4)) {
                    struct.value = iprot.readString();
                    struct.setValueIsSet(true);
                }
            }
        }
    }

    public static class setNamespaceProperty_result implements org.apache.thrift.TBase<setNamespaceProperty_result, setNamespaceProperty_result._Fields>, java.io.Serializable, Cloneable, Comparable<setNamespaceProperty_result> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("setNamespaceProperty_result");

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField TOPE_FIELD_DESC = new org.apache.thrift.protocol.TField("tope", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new setNamespaceProperty_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new setNamespaceProperty_resultTupleSchemeFactory());
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec;

        public org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope;

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
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(setNamespaceProperty_result.class, metaDataMap);
        }

        public setNamespaceProperty_result() {
        }

        public setNamespaceProperty_result(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope) {
            this();
            this.sec = sec;
            this.tope = tope;
        }

        public setNamespaceProperty_result(setNamespaceProperty_result other) {
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException(other.sec);
            }
            if (other.isSetTope()) {
                this.tope = new org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException(other.tope);
            }
        }

        public setNamespaceProperty_result deepCopy() {
            return new setNamespaceProperty_result(this);
        }

        @Override
        public void clear() {
            this.sec = null;
            this.tope = null;
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public setNamespaceProperty_result setSec(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
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

        public org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException getTope() {
            return this.tope;
        }

        public setNamespaceProperty_result setTope(org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope) {
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
                        setSec((org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) value);
                    }
                    break;
                case TOPE:
                    if (value == null) {
                        unsetTope();
                    } else {
                        setTope((org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException) value);
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
            if (that instanceof setNamespaceProperty_result)
                return this.equals((setNamespaceProperty_result) that);
            return false;
        }

        public boolean equals(setNamespaceProperty_result that) {
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

        @Override
        public int compareTo(setNamespaceProperty_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(other.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, other.sec);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTope()).compareTo(other.isSetTope());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTope()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tope, other.tope);
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
            StringBuilder sb = new StringBuilder("setNamespaceProperty_result(");
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

        private static class setNamespaceProperty_resultStandardSchemeFactory implements SchemeFactory {

            public setNamespaceProperty_resultStandardScheme getScheme() {
                return new setNamespaceProperty_resultStandardScheme();
            }
        }

        private static class setNamespaceProperty_resultStandardScheme extends StandardScheme<setNamespaceProperty_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, setNamespaceProperty_result struct) throws org.apache.thrift.TException {
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
                                struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tope = new org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException();
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, setNamespaceProperty_result struct) throws org.apache.thrift.TException {
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

        private static class setNamespaceProperty_resultTupleSchemeFactory implements SchemeFactory {

            public setNamespaceProperty_resultTupleScheme getScheme() {
                return new setNamespaceProperty_resultTupleScheme();
            }
        }

        private static class setNamespaceProperty_resultTupleScheme extends TupleScheme<setNamespaceProperty_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, setNamespaceProperty_result struct) throws org.apache.thrift.TException {
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
            public void read(org.apache.thrift.protocol.TProtocol prot, setNamespaceProperty_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.tope = new org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException();
                    struct.tope.read(iprot);
                    struct.setTopeIsSet(true);
                }
            }
        }
    }

    public static class removeNamespaceProperty_args implements org.apache.thrift.TBase<removeNamespaceProperty_args, removeNamespaceProperty_args._Fields>, java.io.Serializable, Cloneable, Comparable<removeNamespaceProperty_args> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("removeNamespaceProperty_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 4);

        private static final org.apache.thrift.protocol.TField CREDENTIALS_FIELD_DESC = new org.apache.thrift.protocol.TField("credentials", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField NS_FIELD_DESC = new org.apache.thrift.protocol.TField("ns", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final org.apache.thrift.protocol.TField PROPERTY_FIELD_DESC = new org.apache.thrift.protocol.TField("property", org.apache.thrift.protocol.TType.STRING, (short) 3);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new removeNamespaceProperty_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new removeNamespaceProperty_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.TCredentials credentials;

        public String ns;

        public String property;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 4, "tinfo"), CREDENTIALS((short) 1, "credentials"), NS((short) 2, "ns"), PROPERTY((short) 3, "property");

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
                        return CREDENTIALS;
                    case 2:
                        return NS;
                    case 3:
                        return PROPERTY;
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
            tmpMap.put(_Fields.CREDENTIALS, new org.apache.thrift.meta_data.FieldMetaData("credentials", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.TCredentials.class)));
            tmpMap.put(_Fields.NS, new org.apache.thrift.meta_data.FieldMetaData("ns", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.PROPERTY, new org.apache.thrift.meta_data.FieldMetaData("property", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(removeNamespaceProperty_args.class, metaDataMap);
        }

        public removeNamespaceProperty_args() {
        }

        public removeNamespaceProperty_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String ns, String property) {
            this();
            this.tinfo = tinfo;
            this.credentials = credentials;
            this.ns = ns;
            this.property = property;
        }

        public removeNamespaceProperty_args(removeNamespaceProperty_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredentials()) {
                this.credentials = new org.apache.accumulo.core.security.thrift.TCredentials(other.credentials);
            }
            if (other.isSetNs()) {
                this.ns = other.ns;
            }
            if (other.isSetProperty()) {
                this.property = other.property;
            }
        }

        public removeNamespaceProperty_args deepCopy() {
            return new removeNamespaceProperty_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credentials = null;
            this.ns = null;
            this.property = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public removeNamespaceProperty_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
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

        public org.apache.accumulo.core.security.thrift.TCredentials getCredentials() {
            return this.credentials;
        }

        public removeNamespaceProperty_args setCredentials(org.apache.accumulo.core.security.thrift.TCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public void unsetCredentials() {
            this.credentials = null;
        }

        public boolean isSetCredentials() {
            return this.credentials != null;
        }

        public void setCredentialsIsSet(boolean value) {
            if (!value) {
                this.credentials = null;
            }
        }

        public String getNs() {
            return this.ns;
        }

        public removeNamespaceProperty_args setNs(String ns) {
            this.ns = ns;
            return this;
        }

        public void unsetNs() {
            this.ns = null;
        }

        public boolean isSetNs() {
            return this.ns != null;
        }

        public void setNsIsSet(boolean value) {
            if (!value) {
                this.ns = null;
            }
        }

        public String getProperty() {
            return this.property;
        }

        public removeNamespaceProperty_args setProperty(String property) {
            this.property = property;
            return this;
        }

        public void unsetProperty() {
            this.property = null;
        }

        public boolean isSetProperty() {
            return this.property != null;
        }

        public void setPropertyIsSet(boolean value) {
            if (!value) {
                this.property = null;
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
                case CREDENTIALS:
                    if (value == null) {
                        unsetCredentials();
                    } else {
                        setCredentials((org.apache.accumulo.core.security.thrift.TCredentials) value);
                    }
                    break;
                case NS:
                    if (value == null) {
                        unsetNs();
                    } else {
                        setNs((String) value);
                    }
                    break;
                case PROPERTY:
                    if (value == null) {
                        unsetProperty();
                    } else {
                        setProperty((String) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIALS:
                    return getCredentials();
                case NS:
                    return getNs();
                case PROPERTY:
                    return getProperty();
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
                case CREDENTIALS:
                    return isSetCredentials();
                case NS:
                    return isSetNs();
                case PROPERTY:
                    return isSetProperty();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof removeNamespaceProperty_args)
                return this.equals((removeNamespaceProperty_args) that);
            return false;
        }

        public boolean equals(removeNamespaceProperty_args that) {
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
            boolean this_present_credentials = true && this.isSetCredentials();
            boolean that_present_credentials = true && that.isSetCredentials();
            if (this_present_credentials || that_present_credentials) {
                if (!(this_present_credentials && that_present_credentials))
                    return false;
                if (!this.credentials.equals(that.credentials))
                    return false;
            }
            boolean this_present_ns = true && this.isSetNs();
            boolean that_present_ns = true && that.isSetNs();
            if (this_present_ns || that_present_ns) {
                if (!(this_present_ns && that_present_ns))
                    return false;
                if (!this.ns.equals(that.ns))
                    return false;
            }
            boolean this_present_property = true && this.isSetProperty();
            boolean that_present_property = true && that.isSetProperty();
            if (this_present_property || that_present_property) {
                if (!(this_present_property && that_present_property))
                    return false;
                if (!this.property.equals(that.property))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public int compareTo(removeNamespaceProperty_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(other.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, other.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredentials()).compareTo(other.isSetCredentials());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredentials()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credentials, other.credentials);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetNs()).compareTo(other.isSetNs());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetNs()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ns, other.ns);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetProperty()).compareTo(other.isSetProperty());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetProperty()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.property, other.property);
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
            StringBuilder sb = new StringBuilder("removeNamespaceProperty_args(");
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
            sb.append("credentials:");
            if (this.credentials == null) {
                sb.append("null");
            } else {
                sb.append(this.credentials);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("ns:");
            if (this.ns == null) {
                sb.append("null");
            } else {
                sb.append(this.ns);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("property:");
            if (this.property == null) {
                sb.append("null");
            } else {
                sb.append(this.property);
            }
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credentials != null) {
                credentials.validate();
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

        private static class removeNamespaceProperty_argsStandardSchemeFactory implements SchemeFactory {

            public removeNamespaceProperty_argsStandardScheme getScheme() {
                return new removeNamespaceProperty_argsStandardScheme();
            }
        }

        private static class removeNamespaceProperty_argsStandardScheme extends StandardScheme<removeNamespaceProperty_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, removeNamespaceProperty_args struct) throws org.apache.thrift.TException {
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
                                struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                                struct.credentials.read(iprot);
                                struct.setCredentialsIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.ns = iprot.readString();
                                struct.setNsIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.property = iprot.readString();
                                struct.setPropertyIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, removeNamespaceProperty_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credentials != null) {
                    oprot.writeFieldBegin(CREDENTIALS_FIELD_DESC);
                    struct.credentials.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.ns != null) {
                    oprot.writeFieldBegin(NS_FIELD_DESC);
                    oprot.writeString(struct.ns);
                    oprot.writeFieldEnd();
                }
                if (struct.property != null) {
                    oprot.writeFieldBegin(PROPERTY_FIELD_DESC);
                    oprot.writeString(struct.property);
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

        private static class removeNamespaceProperty_argsTupleSchemeFactory implements SchemeFactory {

            public removeNamespaceProperty_argsTupleScheme getScheme() {
                return new removeNamespaceProperty_argsTupleScheme();
            }
        }

        private static class removeNamespaceProperty_argsTupleScheme extends TupleScheme<removeNamespaceProperty_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, removeNamespaceProperty_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredentials()) {
                    optionals.set(1);
                }
                if (struct.isSetNs()) {
                    optionals.set(2);
                }
                if (struct.isSetProperty()) {
                    optionals.set(3);
                }
                oprot.writeBitSet(optionals, 4);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredentials()) {
                    struct.credentials.write(oprot);
                }
                if (struct.isSetNs()) {
                    oprot.writeString(struct.ns);
                }
                if (struct.isSetProperty()) {
                    oprot.writeString(struct.property);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, removeNamespaceProperty_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(4);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                    struct.credentials.read(iprot);
                    struct.setCredentialsIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.ns = iprot.readString();
                    struct.setNsIsSet(true);
                }
                if (incoming.get(3)) {
                    struct.property = iprot.readString();
                    struct.setPropertyIsSet(true);
                }
            }
        }
    }

    public static class removeNamespaceProperty_result implements org.apache.thrift.TBase<removeNamespaceProperty_result, removeNamespaceProperty_result._Fields>, java.io.Serializable, Cloneable, Comparable<removeNamespaceProperty_result> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("removeNamespaceProperty_result");

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField TOPE_FIELD_DESC = new org.apache.thrift.protocol.TField("tope", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new removeNamespaceProperty_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new removeNamespaceProperty_resultTupleSchemeFactory());
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec;

        public org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope;

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
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(removeNamespaceProperty_result.class, metaDataMap);
        }

        public removeNamespaceProperty_result() {
        }

        public removeNamespaceProperty_result(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec, org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope) {
            this();
            this.sec = sec;
            this.tope = tope;
        }

        public removeNamespaceProperty_result(removeNamespaceProperty_result other) {
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException(other.sec);
            }
            if (other.isSetTope()) {
                this.tope = new org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException(other.tope);
            }
        }

        public removeNamespaceProperty_result deepCopy() {
            return new removeNamespaceProperty_result(this);
        }

        @Override
        public void clear() {
            this.sec = null;
            this.tope = null;
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public removeNamespaceProperty_result setSec(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
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

        public org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException getTope() {
            return this.tope;
        }

        public removeNamespaceProperty_result setTope(org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException tope) {
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
                        setSec((org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) value);
                    }
                    break;
                case TOPE:
                    if (value == null) {
                        unsetTope();
                    } else {
                        setTope((org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException) value);
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
            if (that instanceof removeNamespaceProperty_result)
                return this.equals((removeNamespaceProperty_result) that);
            return false;
        }

        public boolean equals(removeNamespaceProperty_result that) {
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

        @Override
        public int compareTo(removeNamespaceProperty_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(other.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, other.sec);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTope()).compareTo(other.isSetTope());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTope()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tope, other.tope);
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
            StringBuilder sb = new StringBuilder("removeNamespaceProperty_result(");
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

        private static class removeNamespaceProperty_resultStandardSchemeFactory implements SchemeFactory {

            public removeNamespaceProperty_resultStandardScheme getScheme() {
                return new removeNamespaceProperty_resultStandardScheme();
            }
        }

        private static class removeNamespaceProperty_resultStandardScheme extends StandardScheme<removeNamespaceProperty_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, removeNamespaceProperty_result struct) throws org.apache.thrift.TException {
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
                                struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tope = new org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException();
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, removeNamespaceProperty_result struct) throws org.apache.thrift.TException {
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

        private static class removeNamespaceProperty_resultTupleSchemeFactory implements SchemeFactory {

            public removeNamespaceProperty_resultTupleScheme getScheme() {
                return new removeNamespaceProperty_resultTupleScheme();
            }
        }

        private static class removeNamespaceProperty_resultTupleScheme extends TupleScheme<removeNamespaceProperty_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, removeNamespaceProperty_result struct) throws org.apache.thrift.TException {
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
            public void read(org.apache.thrift.protocol.TProtocol prot, removeNamespaceProperty_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.tope = new org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException();
                    struct.tope.read(iprot);
                    struct.setTopeIsSet(true);
                }
            }
        }
    }

    public static class setMasterGoalState_args implements org.apache.thrift.TBase<setMasterGoalState_args, setMasterGoalState_args._Fields>, java.io.Serializable, Cloneable, Comparable<setMasterGoalState_args> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("setMasterGoalState_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 3);

        private static final org.apache.thrift.protocol.TField CREDENTIALS_FIELD_DESC = new org.apache.thrift.protocol.TField("credentials", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField STATE_FIELD_DESC = new org.apache.thrift.protocol.TField("state", org.apache.thrift.protocol.TType.I32, (short) 2);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new setMasterGoalState_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new setMasterGoalState_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.TCredentials credentials;

        public MasterGoalState state;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 3, "tinfo"), CREDENTIALS((short) 1, "credentials"), STATE((short) 2, "state");

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
                        return CREDENTIALS;
                    case 2:
                        return STATE;
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
            tmpMap.put(_Fields.CREDENTIALS, new org.apache.thrift.meta_data.FieldMetaData("credentials", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.TCredentials.class)));
            tmpMap.put(_Fields.STATE, new org.apache.thrift.meta_data.FieldMetaData("state", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, MasterGoalState.class)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(setMasterGoalState_args.class, metaDataMap);
        }

        public setMasterGoalState_args() {
        }

        public setMasterGoalState_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, MasterGoalState state) {
            this();
            this.tinfo = tinfo;
            this.credentials = credentials;
            this.state = state;
        }

        public setMasterGoalState_args(setMasterGoalState_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredentials()) {
                this.credentials = new org.apache.accumulo.core.security.thrift.TCredentials(other.credentials);
            }
            if (other.isSetState()) {
                this.state = other.state;
            }
        }

        public setMasterGoalState_args deepCopy() {
            return new setMasterGoalState_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credentials = null;
            this.state = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public setMasterGoalState_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
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

        public org.apache.accumulo.core.security.thrift.TCredentials getCredentials() {
            return this.credentials;
        }

        public setMasterGoalState_args setCredentials(org.apache.accumulo.core.security.thrift.TCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public void unsetCredentials() {
            this.credentials = null;
        }

        public boolean isSetCredentials() {
            return this.credentials != null;
        }

        public void setCredentialsIsSet(boolean value) {
            if (!value) {
                this.credentials = null;
            }
        }

        public MasterGoalState getState() {
            return this.state;
        }

        public setMasterGoalState_args setState(MasterGoalState state) {
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

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
                        setTinfo((org.apache.accumulo.trace.thrift.TInfo) value);
                    }
                    break;
                case CREDENTIALS:
                    if (value == null) {
                        unsetCredentials();
                    } else {
                        setCredentials((org.apache.accumulo.core.security.thrift.TCredentials) value);
                    }
                    break;
                case STATE:
                    if (value == null) {
                        unsetState();
                    } else {
                        setState((MasterGoalState) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIALS:
                    return getCredentials();
                case STATE:
                    return getState();
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
                case CREDENTIALS:
                    return isSetCredentials();
                case STATE:
                    return isSetState();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof setMasterGoalState_args)
                return this.equals((setMasterGoalState_args) that);
            return false;
        }

        public boolean equals(setMasterGoalState_args that) {
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
            boolean this_present_credentials = true && this.isSetCredentials();
            boolean that_present_credentials = true && that.isSetCredentials();
            if (this_present_credentials || that_present_credentials) {
                if (!(this_present_credentials && that_present_credentials))
                    return false;
                if (!this.credentials.equals(that.credentials))
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
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public int compareTo(setMasterGoalState_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(other.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, other.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredentials()).compareTo(other.isSetCredentials());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredentials()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credentials, other.credentials);
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
            StringBuilder sb = new StringBuilder("setMasterGoalState_args(");
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
            sb.append("credentials:");
            if (this.credentials == null) {
                sb.append("null");
            } else {
                sb.append(this.credentials);
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
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credentials != null) {
                credentials.validate();
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

        private static class setMasterGoalState_argsStandardSchemeFactory implements SchemeFactory {

            public setMasterGoalState_argsStandardScheme getScheme() {
                return new setMasterGoalState_argsStandardScheme();
            }
        }

        private static class setMasterGoalState_argsStandardScheme extends StandardScheme<setMasterGoalState_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, setMasterGoalState_args struct) throws org.apache.thrift.TException {
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
                                struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                                struct.credentials.read(iprot);
                                struct.setCredentialsIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                                struct.state = MasterGoalState.findByValue(iprot.readI32());
                                struct.setStateIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, setMasterGoalState_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credentials != null) {
                    oprot.writeFieldBegin(CREDENTIALS_FIELD_DESC);
                    struct.credentials.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.state != null) {
                    oprot.writeFieldBegin(STATE_FIELD_DESC);
                    oprot.writeI32(struct.state.getValue());
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

        private static class setMasterGoalState_argsTupleSchemeFactory implements SchemeFactory {

            public setMasterGoalState_argsTupleScheme getScheme() {
                return new setMasterGoalState_argsTupleScheme();
            }
        }

        private static class setMasterGoalState_argsTupleScheme extends TupleScheme<setMasterGoalState_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, setMasterGoalState_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredentials()) {
                    optionals.set(1);
                }
                if (struct.isSetState()) {
                    optionals.set(2);
                }
                oprot.writeBitSet(optionals, 3);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredentials()) {
                    struct.credentials.write(oprot);
                }
                if (struct.isSetState()) {
                    oprot.writeI32(struct.state.getValue());
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, setMasterGoalState_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(3);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                    struct.credentials.read(iprot);
                    struct.setCredentialsIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.state = MasterGoalState.findByValue(iprot.readI32());
                    struct.setStateIsSet(true);
                }
            }
        }
    }

    public static class setMasterGoalState_result implements org.apache.thrift.TBase<setMasterGoalState_result, setMasterGoalState_result._Fields>, java.io.Serializable, Cloneable, Comparable<setMasterGoalState_result> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("setMasterGoalState_result");

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new setMasterGoalState_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new setMasterGoalState_resultTupleSchemeFactory());
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec;

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
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(setMasterGoalState_result.class, metaDataMap);
        }

        public setMasterGoalState_result() {
        }

        public setMasterGoalState_result(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
            this();
            this.sec = sec;
        }

        public setMasterGoalState_result(setMasterGoalState_result other) {
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException(other.sec);
            }
        }

        public setMasterGoalState_result deepCopy() {
            return new setMasterGoalState_result(this);
        }

        @Override
        public void clear() {
            this.sec = null;
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public setMasterGoalState_result setSec(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
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
                        setSec((org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) value);
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
            if (that instanceof setMasterGoalState_result)
                return this.equals((setMasterGoalState_result) that);
            return false;
        }

        public boolean equals(setMasterGoalState_result that) {
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

        @Override
        public int compareTo(setMasterGoalState_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(other.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, other.sec);
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
            StringBuilder sb = new StringBuilder("setMasterGoalState_result(");
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

        private static class setMasterGoalState_resultStandardSchemeFactory implements SchemeFactory {

            public setMasterGoalState_resultStandardScheme getScheme() {
                return new setMasterGoalState_resultStandardScheme();
            }
        }

        private static class setMasterGoalState_resultStandardScheme extends StandardScheme<setMasterGoalState_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, setMasterGoalState_result struct) throws org.apache.thrift.TException {
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
                                struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, setMasterGoalState_result struct) throws org.apache.thrift.TException {
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

        private static class setMasterGoalState_resultTupleSchemeFactory implements SchemeFactory {

            public setMasterGoalState_resultTupleScheme getScheme() {
                return new setMasterGoalState_resultTupleScheme();
            }
        }

        private static class setMasterGoalState_resultTupleScheme extends TupleScheme<setMasterGoalState_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, setMasterGoalState_result struct) throws org.apache.thrift.TException {
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
            public void read(org.apache.thrift.protocol.TProtocol prot, setMasterGoalState_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
            }
        }
    }

    public static class shutdown_args implements org.apache.thrift.TBase<shutdown_args, shutdown_args._Fields>, java.io.Serializable, Cloneable, Comparable<shutdown_args> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("shutdown_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 3);

        private static final org.apache.thrift.protocol.TField CREDENTIALS_FIELD_DESC = new org.apache.thrift.protocol.TField("credentials", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField STOP_TABLET_SERVERS_FIELD_DESC = new org.apache.thrift.protocol.TField("stopTabletServers", org.apache.thrift.protocol.TType.BOOL, (short) 2);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new shutdown_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new shutdown_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.TCredentials credentials;

        public boolean stopTabletServers;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 3, "tinfo"), CREDENTIALS((short) 1, "credentials"), STOP_TABLET_SERVERS((short) 2, "stopTabletServers");

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
                        return CREDENTIALS;
                    case 2:
                        return STOP_TABLET_SERVERS;
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

        private static final int __STOPTABLETSERVERS_ISSET_ID = 0;

        private byte __isset_bitfield = 0;

        public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.CREDENTIALS, new org.apache.thrift.meta_data.FieldMetaData("credentials", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.TCredentials.class)));
            tmpMap.put(_Fields.STOP_TABLET_SERVERS, new org.apache.thrift.meta_data.FieldMetaData("stopTabletServers", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(shutdown_args.class, metaDataMap);
        }

        public shutdown_args() {
        }

        public shutdown_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, boolean stopTabletServers) {
            this();
            this.tinfo = tinfo;
            this.credentials = credentials;
            this.stopTabletServers = stopTabletServers;
            setStopTabletServersIsSet(true);
        }

        public shutdown_args(shutdown_args other) {
            __isset_bitfield = other.__isset_bitfield;
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredentials()) {
                this.credentials = new org.apache.accumulo.core.security.thrift.TCredentials(other.credentials);
            }
            this.stopTabletServers = other.stopTabletServers;
        }

        public shutdown_args deepCopy() {
            return new shutdown_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credentials = null;
            setStopTabletServersIsSet(false);
            this.stopTabletServers = false;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public shutdown_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
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

        public org.apache.accumulo.core.security.thrift.TCredentials getCredentials() {
            return this.credentials;
        }

        public shutdown_args setCredentials(org.apache.accumulo.core.security.thrift.TCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public void unsetCredentials() {
            this.credentials = null;
        }

        public boolean isSetCredentials() {
            return this.credentials != null;
        }

        public void setCredentialsIsSet(boolean value) {
            if (!value) {
                this.credentials = null;
            }
        }

        public boolean isStopTabletServers() {
            return this.stopTabletServers;
        }

        public shutdown_args setStopTabletServers(boolean stopTabletServers) {
            this.stopTabletServers = stopTabletServers;
            setStopTabletServersIsSet(true);
            return this;
        }

        public void unsetStopTabletServers() {
            __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __STOPTABLETSERVERS_ISSET_ID);
        }

        public boolean isSetStopTabletServers() {
            return EncodingUtils.testBit(__isset_bitfield, __STOPTABLETSERVERS_ISSET_ID);
        }

        public void setStopTabletServersIsSet(boolean value) {
            __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __STOPTABLETSERVERS_ISSET_ID, value);
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
                case CREDENTIALS:
                    if (value == null) {
                        unsetCredentials();
                    } else {
                        setCredentials((org.apache.accumulo.core.security.thrift.TCredentials) value);
                    }
                    break;
                case STOP_TABLET_SERVERS:
                    if (value == null) {
                        unsetStopTabletServers();
                    } else {
                        setStopTabletServers((Boolean) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIALS:
                    return getCredentials();
                case STOP_TABLET_SERVERS:
                    return Boolean.valueOf(isStopTabletServers());
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
                case CREDENTIALS:
                    return isSetCredentials();
                case STOP_TABLET_SERVERS:
                    return isSetStopTabletServers();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof shutdown_args)
                return this.equals((shutdown_args) that);
            return false;
        }

        public boolean equals(shutdown_args that) {
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
            boolean this_present_credentials = true && this.isSetCredentials();
            boolean that_present_credentials = true && that.isSetCredentials();
            if (this_present_credentials || that_present_credentials) {
                if (!(this_present_credentials && that_present_credentials))
                    return false;
                if (!this.credentials.equals(that.credentials))
                    return false;
            }
            boolean this_present_stopTabletServers = true;
            boolean that_present_stopTabletServers = true;
            if (this_present_stopTabletServers || that_present_stopTabletServers) {
                if (!(this_present_stopTabletServers && that_present_stopTabletServers))
                    return false;
                if (this.stopTabletServers != that.stopTabletServers)
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public int compareTo(shutdown_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(other.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, other.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredentials()).compareTo(other.isSetCredentials());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredentials()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credentials, other.credentials);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetStopTabletServers()).compareTo(other.isSetStopTabletServers());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetStopTabletServers()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.stopTabletServers, other.stopTabletServers);
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
            StringBuilder sb = new StringBuilder("shutdown_args(");
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
            sb.append("credentials:");
            if (this.credentials == null) {
                sb.append("null");
            } else {
                sb.append(this.credentials);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("stopTabletServers:");
            sb.append(this.stopTabletServers);
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credentials != null) {
                credentials.validate();
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

        private static class shutdown_argsStandardSchemeFactory implements SchemeFactory {

            public shutdown_argsStandardScheme getScheme() {
                return new shutdown_argsStandardScheme();
            }
        }

        private static class shutdown_argsStandardScheme extends StandardScheme<shutdown_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, shutdown_args struct) throws org.apache.thrift.TException {
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
                                struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                                struct.credentials.read(iprot);
                                struct.setCredentialsIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
                                struct.stopTabletServers = iprot.readBool();
                                struct.setStopTabletServersIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, shutdown_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credentials != null) {
                    oprot.writeFieldBegin(CREDENTIALS_FIELD_DESC);
                    struct.credentials.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldBegin(STOP_TABLET_SERVERS_FIELD_DESC);
                oprot.writeBool(struct.stopTabletServers);
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

        private static class shutdown_argsTupleSchemeFactory implements SchemeFactory {

            public shutdown_argsTupleScheme getScheme() {
                return new shutdown_argsTupleScheme();
            }
        }

        private static class shutdown_argsTupleScheme extends TupleScheme<shutdown_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, shutdown_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredentials()) {
                    optionals.set(1);
                }
                if (struct.isSetStopTabletServers()) {
                    optionals.set(2);
                }
                oprot.writeBitSet(optionals, 3);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredentials()) {
                    struct.credentials.write(oprot);
                }
                if (struct.isSetStopTabletServers()) {
                    oprot.writeBool(struct.stopTabletServers);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, shutdown_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(3);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                    struct.credentials.read(iprot);
                    struct.setCredentialsIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.stopTabletServers = iprot.readBool();
                    struct.setStopTabletServersIsSet(true);
                }
            }
        }
    }

    public static class shutdown_result implements org.apache.thrift.TBase<shutdown_result, shutdown_result._Fields>, java.io.Serializable, Cloneable, Comparable<shutdown_result> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("shutdown_result");

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new shutdown_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new shutdown_resultTupleSchemeFactory());
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec;

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
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(shutdown_result.class, metaDataMap);
        }

        public shutdown_result() {
        }

        public shutdown_result(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
            this();
            this.sec = sec;
        }

        public shutdown_result(shutdown_result other) {
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException(other.sec);
            }
        }

        public shutdown_result deepCopy() {
            return new shutdown_result(this);
        }

        @Override
        public void clear() {
            this.sec = null;
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public shutdown_result setSec(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
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
                        setSec((org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) value);
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
            if (that instanceof shutdown_result)
                return this.equals((shutdown_result) that);
            return false;
        }

        public boolean equals(shutdown_result that) {
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

        @Override
        public int compareTo(shutdown_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(other.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, other.sec);
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
            StringBuilder sb = new StringBuilder("shutdown_result(");
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

        private static class shutdown_resultStandardSchemeFactory implements SchemeFactory {

            public shutdown_resultStandardScheme getScheme() {
                return new shutdown_resultStandardScheme();
            }
        }

        private static class shutdown_resultStandardScheme extends StandardScheme<shutdown_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, shutdown_result struct) throws org.apache.thrift.TException {
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
                                struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, shutdown_result struct) throws org.apache.thrift.TException {
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

        private static class shutdown_resultTupleSchemeFactory implements SchemeFactory {

            public shutdown_resultTupleScheme getScheme() {
                return new shutdown_resultTupleScheme();
            }
        }

        private static class shutdown_resultTupleScheme extends TupleScheme<shutdown_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, shutdown_result struct) throws org.apache.thrift.TException {
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
            public void read(org.apache.thrift.protocol.TProtocol prot, shutdown_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
            }
        }
    }

    public static class shutdownTabletServer_args implements org.apache.thrift.TBase<shutdownTabletServer_args, shutdownTabletServer_args._Fields>, java.io.Serializable, Cloneable, Comparable<shutdownTabletServer_args> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("shutdownTabletServer_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 3);

        private static final org.apache.thrift.protocol.TField CREDENTIALS_FIELD_DESC = new org.apache.thrift.protocol.TField("credentials", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField TABLET_SERVER_FIELD_DESC = new org.apache.thrift.protocol.TField("tabletServer", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final org.apache.thrift.protocol.TField FORCE_FIELD_DESC = new org.apache.thrift.protocol.TField("force", org.apache.thrift.protocol.TType.BOOL, (short) 4);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new shutdownTabletServer_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new shutdownTabletServer_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.TCredentials credentials;

        public String tabletServer;

        public boolean force;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 3, "tinfo"), CREDENTIALS((short) 1, "credentials"), TABLET_SERVER((short) 2, "tabletServer"), FORCE((short) 4, "force");

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
                        return CREDENTIALS;
                    case 2:
                        return TABLET_SERVER;
                    case 4:
                        return FORCE;
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

        private static final int __FORCE_ISSET_ID = 0;

        private byte __isset_bitfield = 0;

        public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.CREDENTIALS, new org.apache.thrift.meta_data.FieldMetaData("credentials", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.TCredentials.class)));
            tmpMap.put(_Fields.TABLET_SERVER, new org.apache.thrift.meta_data.FieldMetaData("tabletServer", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.FORCE, new org.apache.thrift.meta_data.FieldMetaData("force", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(shutdownTabletServer_args.class, metaDataMap);
        }

        public shutdownTabletServer_args() {
        }

        public shutdownTabletServer_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String tabletServer, boolean force) {
            this();
            this.tinfo = tinfo;
            this.credentials = credentials;
            this.tabletServer = tabletServer;
            this.force = force;
            setForceIsSet(true);
        }

        public shutdownTabletServer_args(shutdownTabletServer_args other) {
            __isset_bitfield = other.__isset_bitfield;
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredentials()) {
                this.credentials = new org.apache.accumulo.core.security.thrift.TCredentials(other.credentials);
            }
            if (other.isSetTabletServer()) {
                this.tabletServer = other.tabletServer;
            }
            this.force = other.force;
        }

        public shutdownTabletServer_args deepCopy() {
            return new shutdownTabletServer_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credentials = null;
            this.tabletServer = null;
            setForceIsSet(false);
            this.force = false;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public shutdownTabletServer_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
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

        public org.apache.accumulo.core.security.thrift.TCredentials getCredentials() {
            return this.credentials;
        }

        public shutdownTabletServer_args setCredentials(org.apache.accumulo.core.security.thrift.TCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public void unsetCredentials() {
            this.credentials = null;
        }

        public boolean isSetCredentials() {
            return this.credentials != null;
        }

        public void setCredentialsIsSet(boolean value) {
            if (!value) {
                this.credentials = null;
            }
        }

        public String getTabletServer() {
            return this.tabletServer;
        }

        public shutdownTabletServer_args setTabletServer(String tabletServer) {
            this.tabletServer = tabletServer;
            return this;
        }

        public void unsetTabletServer() {
            this.tabletServer = null;
        }

        public boolean isSetTabletServer() {
            return this.tabletServer != null;
        }

        public void setTabletServerIsSet(boolean value) {
            if (!value) {
                this.tabletServer = null;
            }
        }

        public boolean isForce() {
            return this.force;
        }

        public shutdownTabletServer_args setForce(boolean force) {
            this.force = force;
            setForceIsSet(true);
            return this;
        }

        public void unsetForce() {
            __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __FORCE_ISSET_ID);
        }

        public boolean isSetForce() {
            return EncodingUtils.testBit(__isset_bitfield, __FORCE_ISSET_ID);
        }

        public void setForceIsSet(boolean value) {
            __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __FORCE_ISSET_ID, value);
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
                case CREDENTIALS:
                    if (value == null) {
                        unsetCredentials();
                    } else {
                        setCredentials((org.apache.accumulo.core.security.thrift.TCredentials) value);
                    }
                    break;
                case TABLET_SERVER:
                    if (value == null) {
                        unsetTabletServer();
                    } else {
                        setTabletServer((String) value);
                    }
                    break;
                case FORCE:
                    if (value == null) {
                        unsetForce();
                    } else {
                        setForce((Boolean) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIALS:
                    return getCredentials();
                case TABLET_SERVER:
                    return getTabletServer();
                case FORCE:
                    return Boolean.valueOf(isForce());
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
                case CREDENTIALS:
                    return isSetCredentials();
                case TABLET_SERVER:
                    return isSetTabletServer();
                case FORCE:
                    return isSetForce();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof shutdownTabletServer_args)
                return this.equals((shutdownTabletServer_args) that);
            return false;
        }

        public boolean equals(shutdownTabletServer_args that) {
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
            boolean this_present_credentials = true && this.isSetCredentials();
            boolean that_present_credentials = true && that.isSetCredentials();
            if (this_present_credentials || that_present_credentials) {
                if (!(this_present_credentials && that_present_credentials))
                    return false;
                if (!this.credentials.equals(that.credentials))
                    return false;
            }
            boolean this_present_tabletServer = true && this.isSetTabletServer();
            boolean that_present_tabletServer = true && that.isSetTabletServer();
            if (this_present_tabletServer || that_present_tabletServer) {
                if (!(this_present_tabletServer && that_present_tabletServer))
                    return false;
                if (!this.tabletServer.equals(that.tabletServer))
                    return false;
            }
            boolean this_present_force = true;
            boolean that_present_force = true;
            if (this_present_force || that_present_force) {
                if (!(this_present_force && that_present_force))
                    return false;
                if (this.force != that.force)
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public int compareTo(shutdownTabletServer_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(other.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, other.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredentials()).compareTo(other.isSetCredentials());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredentials()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credentials, other.credentials);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTabletServer()).compareTo(other.isSetTabletServer());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTabletServer()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tabletServer, other.tabletServer);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetForce()).compareTo(other.isSetForce());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetForce()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.force, other.force);
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
            StringBuilder sb = new StringBuilder("shutdownTabletServer_args(");
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
            sb.append("credentials:");
            if (this.credentials == null) {
                sb.append("null");
            } else {
                sb.append(this.credentials);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("tabletServer:");
            if (this.tabletServer == null) {
                sb.append("null");
            } else {
                sb.append(this.tabletServer);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("force:");
            sb.append(this.force);
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credentials != null) {
                credentials.validate();
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

        private static class shutdownTabletServer_argsStandardSchemeFactory implements SchemeFactory {

            public shutdownTabletServer_argsStandardScheme getScheme() {
                return new shutdownTabletServer_argsStandardScheme();
            }
        }

        private static class shutdownTabletServer_argsStandardScheme extends StandardScheme<shutdownTabletServer_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, shutdownTabletServer_args struct) throws org.apache.thrift.TException {
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
                                struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                                struct.credentials.read(iprot);
                                struct.setCredentialsIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.tabletServer = iprot.readString();
                                struct.setTabletServerIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 4:
                            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
                                struct.force = iprot.readBool();
                                struct.setForceIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, shutdownTabletServer_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credentials != null) {
                    oprot.writeFieldBegin(CREDENTIALS_FIELD_DESC);
                    struct.credentials.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.tabletServer != null) {
                    oprot.writeFieldBegin(TABLET_SERVER_FIELD_DESC);
                    oprot.writeString(struct.tabletServer);
                    oprot.writeFieldEnd();
                }
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldBegin(FORCE_FIELD_DESC);
                oprot.writeBool(struct.force);
                oprot.writeFieldEnd();
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class shutdownTabletServer_argsTupleSchemeFactory implements SchemeFactory {

            public shutdownTabletServer_argsTupleScheme getScheme() {
                return new shutdownTabletServer_argsTupleScheme();
            }
        }

        private static class shutdownTabletServer_argsTupleScheme extends TupleScheme<shutdownTabletServer_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, shutdownTabletServer_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredentials()) {
                    optionals.set(1);
                }
                if (struct.isSetTabletServer()) {
                    optionals.set(2);
                }
                if (struct.isSetForce()) {
                    optionals.set(3);
                }
                oprot.writeBitSet(optionals, 4);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredentials()) {
                    struct.credentials.write(oprot);
                }
                if (struct.isSetTabletServer()) {
                    oprot.writeString(struct.tabletServer);
                }
                if (struct.isSetForce()) {
                    oprot.writeBool(struct.force);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, shutdownTabletServer_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(4);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                    struct.credentials.read(iprot);
                    struct.setCredentialsIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.tabletServer = iprot.readString();
                    struct.setTabletServerIsSet(true);
                }
                if (incoming.get(3)) {
                    struct.force = iprot.readBool();
                    struct.setForceIsSet(true);
                }
            }
        }
    }

    public static class shutdownTabletServer_result implements org.apache.thrift.TBase<shutdownTabletServer_result, shutdownTabletServer_result._Fields>, java.io.Serializable, Cloneable, Comparable<shutdownTabletServer_result> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("shutdownTabletServer_result");

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new shutdownTabletServer_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new shutdownTabletServer_resultTupleSchemeFactory());
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec;

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
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(shutdownTabletServer_result.class, metaDataMap);
        }

        public shutdownTabletServer_result() {
        }

        public shutdownTabletServer_result(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
            this();
            this.sec = sec;
        }

        public shutdownTabletServer_result(shutdownTabletServer_result other) {
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException(other.sec);
            }
        }

        public shutdownTabletServer_result deepCopy() {
            return new shutdownTabletServer_result(this);
        }

        @Override
        public void clear() {
            this.sec = null;
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public shutdownTabletServer_result setSec(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
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
                        setSec((org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) value);
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
            if (that instanceof shutdownTabletServer_result)
                return this.equals((shutdownTabletServer_result) that);
            return false;
        }

        public boolean equals(shutdownTabletServer_result that) {
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

        @Override
        public int compareTo(shutdownTabletServer_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(other.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, other.sec);
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
            StringBuilder sb = new StringBuilder("shutdownTabletServer_result(");
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

        private static class shutdownTabletServer_resultStandardSchemeFactory implements SchemeFactory {

            public shutdownTabletServer_resultStandardScheme getScheme() {
                return new shutdownTabletServer_resultStandardScheme();
            }
        }

        private static class shutdownTabletServer_resultStandardScheme extends StandardScheme<shutdownTabletServer_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, shutdownTabletServer_result struct) throws org.apache.thrift.TException {
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
                                struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, shutdownTabletServer_result struct) throws org.apache.thrift.TException {
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

        private static class shutdownTabletServer_resultTupleSchemeFactory implements SchemeFactory {

            public shutdownTabletServer_resultTupleScheme getScheme() {
                return new shutdownTabletServer_resultTupleScheme();
            }
        }

        private static class shutdownTabletServer_resultTupleScheme extends TupleScheme<shutdownTabletServer_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, shutdownTabletServer_result struct) throws org.apache.thrift.TException {
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
            public void read(org.apache.thrift.protocol.TProtocol prot, shutdownTabletServer_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
            }
        }
    }

    public static class setSystemProperty_args implements org.apache.thrift.TBase<setSystemProperty_args, setSystemProperty_args._Fields>, java.io.Serializable, Cloneable, Comparable<setSystemProperty_args> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("setSystemProperty_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 4);

        private static final org.apache.thrift.protocol.TField CREDENTIALS_FIELD_DESC = new org.apache.thrift.protocol.TField("credentials", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField PROPERTY_FIELD_DESC = new org.apache.thrift.protocol.TField("property", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final org.apache.thrift.protocol.TField VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("value", org.apache.thrift.protocol.TType.STRING, (short) 3);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new setSystemProperty_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new setSystemProperty_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.TCredentials credentials;

        public String property;

        public String value;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 4, "tinfo"), CREDENTIALS((short) 1, "credentials"), PROPERTY((short) 2, "property"), VALUE((short) 3, "value");

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
                        return CREDENTIALS;
                    case 2:
                        return PROPERTY;
                    case 3:
                        return VALUE;
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
            tmpMap.put(_Fields.CREDENTIALS, new org.apache.thrift.meta_data.FieldMetaData("credentials", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.TCredentials.class)));
            tmpMap.put(_Fields.PROPERTY, new org.apache.thrift.meta_data.FieldMetaData("property", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.VALUE, new org.apache.thrift.meta_data.FieldMetaData("value", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(setSystemProperty_args.class, metaDataMap);
        }

        public setSystemProperty_args() {
        }

        public setSystemProperty_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String property, String value) {
            this();
            this.tinfo = tinfo;
            this.credentials = credentials;
            this.property = property;
            this.value = value;
        }

        public setSystemProperty_args(setSystemProperty_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredentials()) {
                this.credentials = new org.apache.accumulo.core.security.thrift.TCredentials(other.credentials);
            }
            if (other.isSetProperty()) {
                this.property = other.property;
            }
            if (other.isSetValue()) {
                this.value = other.value;
            }
        }

        public setSystemProperty_args deepCopy() {
            return new setSystemProperty_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credentials = null;
            this.property = null;
            this.value = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public setSystemProperty_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
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

        public org.apache.accumulo.core.security.thrift.TCredentials getCredentials() {
            return this.credentials;
        }

        public setSystemProperty_args setCredentials(org.apache.accumulo.core.security.thrift.TCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public void unsetCredentials() {
            this.credentials = null;
        }

        public boolean isSetCredentials() {
            return this.credentials != null;
        }

        public void setCredentialsIsSet(boolean value) {
            if (!value) {
                this.credentials = null;
            }
        }

        public String getProperty() {
            return this.property;
        }

        public setSystemProperty_args setProperty(String property) {
            this.property = property;
            return this;
        }

        public void unsetProperty() {
            this.property = null;
        }

        public boolean isSetProperty() {
            return this.property != null;
        }

        public void setPropertyIsSet(boolean value) {
            if (!value) {
                this.property = null;
            }
        }

        public String getValue() {
            return this.value;
        }

        public setSystemProperty_args setValue(String value) {
            this.value = value;
            return this;
        }

        public void unsetValue() {
            this.value = null;
        }

        public boolean isSetValue() {
            return this.value != null;
        }

        public void setValueIsSet(boolean value) {
            if (!value) {
                this.value = null;
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
                case CREDENTIALS:
                    if (value == null) {
                        unsetCredentials();
                    } else {
                        setCredentials((org.apache.accumulo.core.security.thrift.TCredentials) value);
                    }
                    break;
                case PROPERTY:
                    if (value == null) {
                        unsetProperty();
                    } else {
                        setProperty((String) value);
                    }
                    break;
                case VALUE:
                    if (value == null) {
                        unsetValue();
                    } else {
                        setValue((String) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIALS:
                    return getCredentials();
                case PROPERTY:
                    return getProperty();
                case VALUE:
                    return getValue();
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
                case CREDENTIALS:
                    return isSetCredentials();
                case PROPERTY:
                    return isSetProperty();
                case VALUE:
                    return isSetValue();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof setSystemProperty_args)
                return this.equals((setSystemProperty_args) that);
            return false;
        }

        public boolean equals(setSystemProperty_args that) {
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
            boolean this_present_credentials = true && this.isSetCredentials();
            boolean that_present_credentials = true && that.isSetCredentials();
            if (this_present_credentials || that_present_credentials) {
                if (!(this_present_credentials && that_present_credentials))
                    return false;
                if (!this.credentials.equals(that.credentials))
                    return false;
            }
            boolean this_present_property = true && this.isSetProperty();
            boolean that_present_property = true && that.isSetProperty();
            if (this_present_property || that_present_property) {
                if (!(this_present_property && that_present_property))
                    return false;
                if (!this.property.equals(that.property))
                    return false;
            }
            boolean this_present_value = true && this.isSetValue();
            boolean that_present_value = true && that.isSetValue();
            if (this_present_value || that_present_value) {
                if (!(this_present_value && that_present_value))
                    return false;
                if (!this.value.equals(that.value))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public int compareTo(setSystemProperty_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(other.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, other.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredentials()).compareTo(other.isSetCredentials());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredentials()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credentials, other.credentials);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetProperty()).compareTo(other.isSetProperty());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetProperty()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.property, other.property);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetValue()).compareTo(other.isSetValue());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetValue()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.value, other.value);
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
            StringBuilder sb = new StringBuilder("setSystemProperty_args(");
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
            sb.append("credentials:");
            if (this.credentials == null) {
                sb.append("null");
            } else {
                sb.append(this.credentials);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("property:");
            if (this.property == null) {
                sb.append("null");
            } else {
                sb.append(this.property);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("value:");
            if (this.value == null) {
                sb.append("null");
            } else {
                sb.append(this.value);
            }
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credentials != null) {
                credentials.validate();
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

        private static class setSystemProperty_argsStandardSchemeFactory implements SchemeFactory {

            public setSystemProperty_argsStandardScheme getScheme() {
                return new setSystemProperty_argsStandardScheme();
            }
        }

        private static class setSystemProperty_argsStandardScheme extends StandardScheme<setSystemProperty_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, setSystemProperty_args struct) throws org.apache.thrift.TException {
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
                                struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                                struct.credentials.read(iprot);
                                struct.setCredentialsIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.property = iprot.readString();
                                struct.setPropertyIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.value = iprot.readString();
                                struct.setValueIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, setSystemProperty_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credentials != null) {
                    oprot.writeFieldBegin(CREDENTIALS_FIELD_DESC);
                    struct.credentials.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.property != null) {
                    oprot.writeFieldBegin(PROPERTY_FIELD_DESC);
                    oprot.writeString(struct.property);
                    oprot.writeFieldEnd();
                }
                if (struct.value != null) {
                    oprot.writeFieldBegin(VALUE_FIELD_DESC);
                    oprot.writeString(struct.value);
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

        private static class setSystemProperty_argsTupleSchemeFactory implements SchemeFactory {

            public setSystemProperty_argsTupleScheme getScheme() {
                return new setSystemProperty_argsTupleScheme();
            }
        }

        private static class setSystemProperty_argsTupleScheme extends TupleScheme<setSystemProperty_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, setSystemProperty_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredentials()) {
                    optionals.set(1);
                }
                if (struct.isSetProperty()) {
                    optionals.set(2);
                }
                if (struct.isSetValue()) {
                    optionals.set(3);
                }
                oprot.writeBitSet(optionals, 4);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredentials()) {
                    struct.credentials.write(oprot);
                }
                if (struct.isSetProperty()) {
                    oprot.writeString(struct.property);
                }
                if (struct.isSetValue()) {
                    oprot.writeString(struct.value);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, setSystemProperty_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(4);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                    struct.credentials.read(iprot);
                    struct.setCredentialsIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.property = iprot.readString();
                    struct.setPropertyIsSet(true);
                }
                if (incoming.get(3)) {
                    struct.value = iprot.readString();
                    struct.setValueIsSet(true);
                }
            }
        }
    }

    public static class setSystemProperty_result implements org.apache.thrift.TBase<setSystemProperty_result, setSystemProperty_result._Fields>, java.io.Serializable, Cloneable, Comparable<setSystemProperty_result> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("setSystemProperty_result");

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new setSystemProperty_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new setSystemProperty_resultTupleSchemeFactory());
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec;

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
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(setSystemProperty_result.class, metaDataMap);
        }

        public setSystemProperty_result() {
        }

        public setSystemProperty_result(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
            this();
            this.sec = sec;
        }

        public setSystemProperty_result(setSystemProperty_result other) {
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException(other.sec);
            }
        }

        public setSystemProperty_result deepCopy() {
            return new setSystemProperty_result(this);
        }

        @Override
        public void clear() {
            this.sec = null;
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public setSystemProperty_result setSec(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
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
                        setSec((org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) value);
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
            if (that instanceof setSystemProperty_result)
                return this.equals((setSystemProperty_result) that);
            return false;
        }

        public boolean equals(setSystemProperty_result that) {
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

        @Override
        public int compareTo(setSystemProperty_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(other.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, other.sec);
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
            StringBuilder sb = new StringBuilder("setSystemProperty_result(");
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

        private static class setSystemProperty_resultStandardSchemeFactory implements SchemeFactory {

            public setSystemProperty_resultStandardScheme getScheme() {
                return new setSystemProperty_resultStandardScheme();
            }
        }

        private static class setSystemProperty_resultStandardScheme extends StandardScheme<setSystemProperty_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, setSystemProperty_result struct) throws org.apache.thrift.TException {
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
                                struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, setSystemProperty_result struct) throws org.apache.thrift.TException {
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

        private static class setSystemProperty_resultTupleSchemeFactory implements SchemeFactory {

            public setSystemProperty_resultTupleScheme getScheme() {
                return new setSystemProperty_resultTupleScheme();
            }
        }

        private static class setSystemProperty_resultTupleScheme extends TupleScheme<setSystemProperty_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, setSystemProperty_result struct) throws org.apache.thrift.TException {
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
            public void read(org.apache.thrift.protocol.TProtocol prot, setSystemProperty_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
            }
        }
    }

    public static class removeSystemProperty_args implements org.apache.thrift.TBase<removeSystemProperty_args, removeSystemProperty_args._Fields>, java.io.Serializable, Cloneable, Comparable<removeSystemProperty_args> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("removeSystemProperty_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 3);

        private static final org.apache.thrift.protocol.TField CREDENTIALS_FIELD_DESC = new org.apache.thrift.protocol.TField("credentials", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField PROPERTY_FIELD_DESC = new org.apache.thrift.protocol.TField("property", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new removeSystemProperty_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new removeSystemProperty_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.TCredentials credentials;

        public String property;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 3, "tinfo"), CREDENTIALS((short) 1, "credentials"), PROPERTY((short) 2, "property");

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
                        return CREDENTIALS;
                    case 2:
                        return PROPERTY;
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
            tmpMap.put(_Fields.CREDENTIALS, new org.apache.thrift.meta_data.FieldMetaData("credentials", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.TCredentials.class)));
            tmpMap.put(_Fields.PROPERTY, new org.apache.thrift.meta_data.FieldMetaData("property", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(removeSystemProperty_args.class, metaDataMap);
        }

        public removeSystemProperty_args() {
        }

        public removeSystemProperty_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String property) {
            this();
            this.tinfo = tinfo;
            this.credentials = credentials;
            this.property = property;
        }

        public removeSystemProperty_args(removeSystemProperty_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredentials()) {
                this.credentials = new org.apache.accumulo.core.security.thrift.TCredentials(other.credentials);
            }
            if (other.isSetProperty()) {
                this.property = other.property;
            }
        }

        public removeSystemProperty_args deepCopy() {
            return new removeSystemProperty_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credentials = null;
            this.property = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public removeSystemProperty_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
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

        public org.apache.accumulo.core.security.thrift.TCredentials getCredentials() {
            return this.credentials;
        }

        public removeSystemProperty_args setCredentials(org.apache.accumulo.core.security.thrift.TCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public void unsetCredentials() {
            this.credentials = null;
        }

        public boolean isSetCredentials() {
            return this.credentials != null;
        }

        public void setCredentialsIsSet(boolean value) {
            if (!value) {
                this.credentials = null;
            }
        }

        public String getProperty() {
            return this.property;
        }

        public removeSystemProperty_args setProperty(String property) {
            this.property = property;
            return this;
        }

        public void unsetProperty() {
            this.property = null;
        }

        public boolean isSetProperty() {
            return this.property != null;
        }

        public void setPropertyIsSet(boolean value) {
            if (!value) {
                this.property = null;
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
                case CREDENTIALS:
                    if (value == null) {
                        unsetCredentials();
                    } else {
                        setCredentials((org.apache.accumulo.core.security.thrift.TCredentials) value);
                    }
                    break;
                case PROPERTY:
                    if (value == null) {
                        unsetProperty();
                    } else {
                        setProperty((String) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIALS:
                    return getCredentials();
                case PROPERTY:
                    return getProperty();
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
                case CREDENTIALS:
                    return isSetCredentials();
                case PROPERTY:
                    return isSetProperty();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof removeSystemProperty_args)
                return this.equals((removeSystemProperty_args) that);
            return false;
        }

        public boolean equals(removeSystemProperty_args that) {
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
            boolean this_present_credentials = true && this.isSetCredentials();
            boolean that_present_credentials = true && that.isSetCredentials();
            if (this_present_credentials || that_present_credentials) {
                if (!(this_present_credentials && that_present_credentials))
                    return false;
                if (!this.credentials.equals(that.credentials))
                    return false;
            }
            boolean this_present_property = true && this.isSetProperty();
            boolean that_present_property = true && that.isSetProperty();
            if (this_present_property || that_present_property) {
                if (!(this_present_property && that_present_property))
                    return false;
                if (!this.property.equals(that.property))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public int compareTo(removeSystemProperty_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(other.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, other.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredentials()).compareTo(other.isSetCredentials());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredentials()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credentials, other.credentials);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetProperty()).compareTo(other.isSetProperty());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetProperty()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.property, other.property);
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
            StringBuilder sb = new StringBuilder("removeSystemProperty_args(");
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
            sb.append("credentials:");
            if (this.credentials == null) {
                sb.append("null");
            } else {
                sb.append(this.credentials);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("property:");
            if (this.property == null) {
                sb.append("null");
            } else {
                sb.append(this.property);
            }
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credentials != null) {
                credentials.validate();
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

        private static class removeSystemProperty_argsStandardSchemeFactory implements SchemeFactory {

            public removeSystemProperty_argsStandardScheme getScheme() {
                return new removeSystemProperty_argsStandardScheme();
            }
        }

        private static class removeSystemProperty_argsStandardScheme extends StandardScheme<removeSystemProperty_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, removeSystemProperty_args struct) throws org.apache.thrift.TException {
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
                                struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                                struct.credentials.read(iprot);
                                struct.setCredentialsIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.property = iprot.readString();
                                struct.setPropertyIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, removeSystemProperty_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credentials != null) {
                    oprot.writeFieldBegin(CREDENTIALS_FIELD_DESC);
                    struct.credentials.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.property != null) {
                    oprot.writeFieldBegin(PROPERTY_FIELD_DESC);
                    oprot.writeString(struct.property);
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

        private static class removeSystemProperty_argsTupleSchemeFactory implements SchemeFactory {

            public removeSystemProperty_argsTupleScheme getScheme() {
                return new removeSystemProperty_argsTupleScheme();
            }
        }

        private static class removeSystemProperty_argsTupleScheme extends TupleScheme<removeSystemProperty_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, removeSystemProperty_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredentials()) {
                    optionals.set(1);
                }
                if (struct.isSetProperty()) {
                    optionals.set(2);
                }
                oprot.writeBitSet(optionals, 3);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredentials()) {
                    struct.credentials.write(oprot);
                }
                if (struct.isSetProperty()) {
                    oprot.writeString(struct.property);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, removeSystemProperty_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(3);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                    struct.credentials.read(iprot);
                    struct.setCredentialsIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.property = iprot.readString();
                    struct.setPropertyIsSet(true);
                }
            }
        }
    }

    public static class removeSystemProperty_result implements org.apache.thrift.TBase<removeSystemProperty_result, removeSystemProperty_result._Fields>, java.io.Serializable, Cloneable, Comparable<removeSystemProperty_result> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("removeSystemProperty_result");

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new removeSystemProperty_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new removeSystemProperty_resultTupleSchemeFactory());
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec;

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
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(removeSystemProperty_result.class, metaDataMap);
        }

        public removeSystemProperty_result() {
        }

        public removeSystemProperty_result(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
            this();
            this.sec = sec;
        }

        public removeSystemProperty_result(removeSystemProperty_result other) {
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException(other.sec);
            }
        }

        public removeSystemProperty_result deepCopy() {
            return new removeSystemProperty_result(this);
        }

        @Override
        public void clear() {
            this.sec = null;
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public removeSystemProperty_result setSec(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
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
                        setSec((org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) value);
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
            if (that instanceof removeSystemProperty_result)
                return this.equals((removeSystemProperty_result) that);
            return false;
        }

        public boolean equals(removeSystemProperty_result that) {
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

        @Override
        public int compareTo(removeSystemProperty_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(other.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, other.sec);
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
            StringBuilder sb = new StringBuilder("removeSystemProperty_result(");
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

        private static class removeSystemProperty_resultStandardSchemeFactory implements SchemeFactory {

            public removeSystemProperty_resultStandardScheme getScheme() {
                return new removeSystemProperty_resultStandardScheme();
            }
        }

        private static class removeSystemProperty_resultStandardScheme extends StandardScheme<removeSystemProperty_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, removeSystemProperty_result struct) throws org.apache.thrift.TException {
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
                                struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, removeSystemProperty_result struct) throws org.apache.thrift.TException {
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

        private static class removeSystemProperty_resultTupleSchemeFactory implements SchemeFactory {

            public removeSystemProperty_resultTupleScheme getScheme() {
                return new removeSystemProperty_resultTupleScheme();
            }
        }

        private static class removeSystemProperty_resultTupleScheme extends TupleScheme<removeSystemProperty_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, removeSystemProperty_result struct) throws org.apache.thrift.TException {
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
            public void read(org.apache.thrift.protocol.TProtocol prot, removeSystemProperty_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
            }
        }
    }

    public static class getMasterStats_args implements org.apache.thrift.TBase<getMasterStats_args, getMasterStats_args._Fields>, java.io.Serializable, Cloneable, Comparable<getMasterStats_args> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("getMasterStats_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

        private static final org.apache.thrift.protocol.TField CREDENTIALS_FIELD_DESC = new org.apache.thrift.protocol.TField("credentials", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new getMasterStats_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new getMasterStats_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.TCredentials credentials;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 2, "tinfo"), CREDENTIALS((short) 1, "credentials");

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
                        return CREDENTIALS;
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
            tmpMap.put(_Fields.CREDENTIALS, new org.apache.thrift.meta_data.FieldMetaData("credentials", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.TCredentials.class)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(getMasterStats_args.class, metaDataMap);
        }

        public getMasterStats_args() {
        }

        public getMasterStats_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials) {
            this();
            this.tinfo = tinfo;
            this.credentials = credentials;
        }

        public getMasterStats_args(getMasterStats_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredentials()) {
                this.credentials = new org.apache.accumulo.core.security.thrift.TCredentials(other.credentials);
            }
        }

        public getMasterStats_args deepCopy() {
            return new getMasterStats_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credentials = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public getMasterStats_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
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

        public org.apache.accumulo.core.security.thrift.TCredentials getCredentials() {
            return this.credentials;
        }

        public getMasterStats_args setCredentials(org.apache.accumulo.core.security.thrift.TCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public void unsetCredentials() {
            this.credentials = null;
        }

        public boolean isSetCredentials() {
            return this.credentials != null;
        }

        public void setCredentialsIsSet(boolean value) {
            if (!value) {
                this.credentials = null;
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
                case CREDENTIALS:
                    if (value == null) {
                        unsetCredentials();
                    } else {
                        setCredentials((org.apache.accumulo.core.security.thrift.TCredentials) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIALS:
                    return getCredentials();
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
                case CREDENTIALS:
                    return isSetCredentials();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof getMasterStats_args)
                return this.equals((getMasterStats_args) that);
            return false;
        }

        public boolean equals(getMasterStats_args that) {
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
            boolean this_present_credentials = true && this.isSetCredentials();
            boolean that_present_credentials = true && that.isSetCredentials();
            if (this_present_credentials || that_present_credentials) {
                if (!(this_present_credentials && that_present_credentials))
                    return false;
                if (!this.credentials.equals(that.credentials))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public int compareTo(getMasterStats_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(other.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, other.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredentials()).compareTo(other.isSetCredentials());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredentials()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credentials, other.credentials);
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
            StringBuilder sb = new StringBuilder("getMasterStats_args(");
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
            sb.append("credentials:");
            if (this.credentials == null) {
                sb.append("null");
            } else {
                sb.append(this.credentials);
            }
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credentials != null) {
                credentials.validate();
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

        private static class getMasterStats_argsStandardSchemeFactory implements SchemeFactory {

            public getMasterStats_argsStandardScheme getScheme() {
                return new getMasterStats_argsStandardScheme();
            }
        }

        private static class getMasterStats_argsStandardScheme extends StandardScheme<getMasterStats_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, getMasterStats_args struct) throws org.apache.thrift.TException {
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
                                struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                                struct.credentials.read(iprot);
                                struct.setCredentialsIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, getMasterStats_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credentials != null) {
                    oprot.writeFieldBegin(CREDENTIALS_FIELD_DESC);
                    struct.credentials.write(oprot);
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

        private static class getMasterStats_argsTupleSchemeFactory implements SchemeFactory {

            public getMasterStats_argsTupleScheme getScheme() {
                return new getMasterStats_argsTupleScheme();
            }
        }

        private static class getMasterStats_argsTupleScheme extends TupleScheme<getMasterStats_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, getMasterStats_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredentials()) {
                    optionals.set(1);
                }
                oprot.writeBitSet(optionals, 2);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredentials()) {
                    struct.credentials.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, getMasterStats_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                    struct.credentials.read(iprot);
                    struct.setCredentialsIsSet(true);
                }
            }
        }
    }

    public static class getMasterStats_result implements org.apache.thrift.TBase<getMasterStats_result, getMasterStats_result._Fields>, java.io.Serializable, Cloneable, Comparable<getMasterStats_result> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("getMasterStats_result");

        private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.STRUCT, (short) 0);

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new getMasterStats_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new getMasterStats_resultTupleSchemeFactory());
        }

        public MasterMonitorInfo success;

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec;

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
            tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, MasterMonitorInfo.class)));
            tmpMap.put(_Fields.SEC, new org.apache.thrift.meta_data.FieldMetaData("sec", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(getMasterStats_result.class, metaDataMap);
        }

        public getMasterStats_result() {
        }

        public getMasterStats_result(MasterMonitorInfo success, org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
            this();
            this.success = success;
            this.sec = sec;
        }

        public getMasterStats_result(getMasterStats_result other) {
            if (other.isSetSuccess()) {
                this.success = new MasterMonitorInfo(other.success);
            }
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException(other.sec);
            }
        }

        public getMasterStats_result deepCopy() {
            return new getMasterStats_result(this);
        }

        @Override
        public void clear() {
            this.success = null;
            this.sec = null;
        }

        public MasterMonitorInfo getSuccess() {
            return this.success;
        }

        public getMasterStats_result setSuccess(MasterMonitorInfo success) {
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

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public getMasterStats_result setSec(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
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
                        setSuccess((MasterMonitorInfo) value);
                    }
                    break;
                case SEC:
                    if (value == null) {
                        unsetSec();
                    } else {
                        setSec((org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) value);
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
            if (that instanceof getMasterStats_result)
                return this.equals((getMasterStats_result) that);
            return false;
        }

        public boolean equals(getMasterStats_result that) {
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

        @Override
        public int compareTo(getMasterStats_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(other.isSetSuccess());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSuccess()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, other.success);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(other.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, other.sec);
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
            StringBuilder sb = new StringBuilder("getMasterStats_result(");
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
            if (success != null) {
                success.validate();
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

        private static class getMasterStats_resultStandardSchemeFactory implements SchemeFactory {

            public getMasterStats_resultStandardScheme getScheme() {
                return new getMasterStats_resultStandardScheme();
            }
        }

        private static class getMasterStats_resultStandardScheme extends StandardScheme<getMasterStats_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, getMasterStats_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 0:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.success = new MasterMonitorInfo();
                                struct.success.read(iprot);
                                struct.setSuccessIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, getMasterStats_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.success != null) {
                    oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
                    struct.success.write(oprot);
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

        private static class getMasterStats_resultTupleSchemeFactory implements SchemeFactory {

            public getMasterStats_resultTupleScheme getScheme() {
                return new getMasterStats_resultTupleScheme();
            }
        }

        private static class getMasterStats_resultTupleScheme extends TupleScheme<getMasterStats_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, getMasterStats_result struct) throws org.apache.thrift.TException {
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
                    struct.success.write(oprot);
                }
                if (struct.isSetSec()) {
                    struct.sec.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, getMasterStats_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    struct.success = new MasterMonitorInfo();
                    struct.success.read(iprot);
                    struct.setSuccessIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
            }
        }
    }

    public static class waitForBalance_args implements org.apache.thrift.TBase<waitForBalance_args, waitForBalance_args._Fields>, java.io.Serializable, Cloneable, Comparable<waitForBalance_args> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("waitForBalance_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new waitForBalance_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new waitForBalance_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 1, "tinfo");

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
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(waitForBalance_args.class, metaDataMap);
        }

        public waitForBalance_args() {
        }

        public waitForBalance_args(org.apache.accumulo.trace.thrift.TInfo tinfo) {
            this();
            this.tinfo = tinfo;
        }

        public waitForBalance_args(waitForBalance_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
        }

        public waitForBalance_args deepCopy() {
            return new waitForBalance_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public waitForBalance_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
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

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
                        setTinfo((org.apache.accumulo.trace.thrift.TInfo) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
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
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof waitForBalance_args)
                return this.equals((waitForBalance_args) that);
            return false;
        }

        public boolean equals(waitForBalance_args that) {
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
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public int compareTo(waitForBalance_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(other.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, other.tinfo);
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
            StringBuilder sb = new StringBuilder("waitForBalance_args(");
            boolean first = true;
            sb.append("tinfo:");
            if (this.tinfo == null) {
                sb.append("null");
            } else {
                sb.append(this.tinfo);
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

        private static class waitForBalance_argsStandardSchemeFactory implements SchemeFactory {

            public waitForBalance_argsStandardScheme getScheme() {
                return new waitForBalance_argsStandardScheme();
            }
        }

        private static class waitForBalance_argsStandardScheme extends StandardScheme<waitForBalance_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, waitForBalance_args struct) throws org.apache.thrift.TException {
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
                        default:
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                    }
                    iprot.readFieldEnd();
                }
                iprot.readStructEnd();
                struct.validate();
            }

            public void write(org.apache.thrift.protocol.TProtocol oprot, waitForBalance_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class waitForBalance_argsTupleSchemeFactory implements SchemeFactory {

            public waitForBalance_argsTupleScheme getScheme() {
                return new waitForBalance_argsTupleScheme();
            }
        }

        private static class waitForBalance_argsTupleScheme extends TupleScheme<waitForBalance_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, waitForBalance_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                oprot.writeBitSet(optionals, 1);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, waitForBalance_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
            }
        }
    }

    public static class waitForBalance_result implements org.apache.thrift.TBase<waitForBalance_result, waitForBalance_result._Fields>, java.io.Serializable, Cloneable, Comparable<waitForBalance_result> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("waitForBalance_result");

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new waitForBalance_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new waitForBalance_resultTupleSchemeFactory());
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec;

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
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(waitForBalance_result.class, metaDataMap);
        }

        public waitForBalance_result() {
        }

        public waitForBalance_result(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
            this();
            this.sec = sec;
        }

        public waitForBalance_result(waitForBalance_result other) {
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException(other.sec);
            }
        }

        public waitForBalance_result deepCopy() {
            return new waitForBalance_result(this);
        }

        @Override
        public void clear() {
            this.sec = null;
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public waitForBalance_result setSec(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
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
                        setSec((org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) value);
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
            if (that instanceof waitForBalance_result)
                return this.equals((waitForBalance_result) that);
            return false;
        }

        public boolean equals(waitForBalance_result that) {
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

        @Override
        public int compareTo(waitForBalance_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(other.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, other.sec);
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
            StringBuilder sb = new StringBuilder("waitForBalance_result(");
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

        private static class waitForBalance_resultStandardSchemeFactory implements SchemeFactory {

            public waitForBalance_resultStandardScheme getScheme() {
                return new waitForBalance_resultStandardScheme();
            }
        }

        private static class waitForBalance_resultStandardScheme extends StandardScheme<waitForBalance_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, waitForBalance_result struct) throws org.apache.thrift.TException {
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
                                struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, waitForBalance_result struct) throws org.apache.thrift.TException {
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

        private static class waitForBalance_resultTupleSchemeFactory implements SchemeFactory {

            public waitForBalance_resultTupleScheme getScheme() {
                return new waitForBalance_resultTupleScheme();
            }
        }

        private static class waitForBalance_resultTupleScheme extends TupleScheme<waitForBalance_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, waitForBalance_result struct) throws org.apache.thrift.TException {
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
            public void read(org.apache.thrift.protocol.TProtocol prot, waitForBalance_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
            }
        }
    }

    public static class reportSplitExtent_args implements org.apache.thrift.TBase<reportSplitExtent_args, reportSplitExtent_args._Fields>, java.io.Serializable, Cloneable, Comparable<reportSplitExtent_args> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("reportSplitExtent_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 4);

        private static final org.apache.thrift.protocol.TField CREDENTIALS_FIELD_DESC = new org.apache.thrift.protocol.TField("credentials", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField SERVER_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("serverName", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final org.apache.thrift.protocol.TField SPLIT_FIELD_DESC = new org.apache.thrift.protocol.TField("split", org.apache.thrift.protocol.TType.STRUCT, (short) 3);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new reportSplitExtent_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new reportSplitExtent_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.TCredentials credentials;

        public String serverName;

        public TabletSplit split;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 4, "tinfo"), CREDENTIALS((short) 1, "credentials"), SERVER_NAME((short) 2, "serverName"), SPLIT((short) 3, "split");

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
                        return CREDENTIALS;
                    case 2:
                        return SERVER_NAME;
                    case 3:
                        return SPLIT;
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
            tmpMap.put(_Fields.CREDENTIALS, new org.apache.thrift.meta_data.FieldMetaData("credentials", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.TCredentials.class)));
            tmpMap.put(_Fields.SERVER_NAME, new org.apache.thrift.meta_data.FieldMetaData("serverName", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.SPLIT, new org.apache.thrift.meta_data.FieldMetaData("split", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TabletSplit.class)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(reportSplitExtent_args.class, metaDataMap);
        }

        public reportSplitExtent_args() {
        }

        public reportSplitExtent_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String serverName, TabletSplit split) {
            this();
            this.tinfo = tinfo;
            this.credentials = credentials;
            this.serverName = serverName;
            this.split = split;
        }

        public reportSplitExtent_args(reportSplitExtent_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredentials()) {
                this.credentials = new org.apache.accumulo.core.security.thrift.TCredentials(other.credentials);
            }
            if (other.isSetServerName()) {
                this.serverName = other.serverName;
            }
            if (other.isSetSplit()) {
                this.split = new TabletSplit(other.split);
            }
        }

        public reportSplitExtent_args deepCopy() {
            return new reportSplitExtent_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credentials = null;
            this.serverName = null;
            this.split = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public reportSplitExtent_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
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

        public org.apache.accumulo.core.security.thrift.TCredentials getCredentials() {
            return this.credentials;
        }

        public reportSplitExtent_args setCredentials(org.apache.accumulo.core.security.thrift.TCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public void unsetCredentials() {
            this.credentials = null;
        }

        public boolean isSetCredentials() {
            return this.credentials != null;
        }

        public void setCredentialsIsSet(boolean value) {
            if (!value) {
                this.credentials = null;
            }
        }

        public String getServerName() {
            return this.serverName;
        }

        public reportSplitExtent_args setServerName(String serverName) {
            this.serverName = serverName;
            return this;
        }

        public void unsetServerName() {
            this.serverName = null;
        }

        public boolean isSetServerName() {
            return this.serverName != null;
        }

        public void setServerNameIsSet(boolean value) {
            if (!value) {
                this.serverName = null;
            }
        }

        public TabletSplit getSplit() {
            return this.split;
        }

        public reportSplitExtent_args setSplit(TabletSplit split) {
            this.split = split;
            return this;
        }

        public void unsetSplit() {
            this.split = null;
        }

        public boolean isSetSplit() {
            return this.split != null;
        }

        public void setSplitIsSet(boolean value) {
            if (!value) {
                this.split = null;
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
                case CREDENTIALS:
                    if (value == null) {
                        unsetCredentials();
                    } else {
                        setCredentials((org.apache.accumulo.core.security.thrift.TCredentials) value);
                    }
                    break;
                case SERVER_NAME:
                    if (value == null) {
                        unsetServerName();
                    } else {
                        setServerName((String) value);
                    }
                    break;
                case SPLIT:
                    if (value == null) {
                        unsetSplit();
                    } else {
                        setSplit((TabletSplit) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIALS:
                    return getCredentials();
                case SERVER_NAME:
                    return getServerName();
                case SPLIT:
                    return getSplit();
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
                case CREDENTIALS:
                    return isSetCredentials();
                case SERVER_NAME:
                    return isSetServerName();
                case SPLIT:
                    return isSetSplit();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof reportSplitExtent_args)
                return this.equals((reportSplitExtent_args) that);
            return false;
        }

        public boolean equals(reportSplitExtent_args that) {
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
            boolean this_present_credentials = true && this.isSetCredentials();
            boolean that_present_credentials = true && that.isSetCredentials();
            if (this_present_credentials || that_present_credentials) {
                if (!(this_present_credentials && that_present_credentials))
                    return false;
                if (!this.credentials.equals(that.credentials))
                    return false;
            }
            boolean this_present_serverName = true && this.isSetServerName();
            boolean that_present_serverName = true && that.isSetServerName();
            if (this_present_serverName || that_present_serverName) {
                if (!(this_present_serverName && that_present_serverName))
                    return false;
                if (!this.serverName.equals(that.serverName))
                    return false;
            }
            boolean this_present_split = true && this.isSetSplit();
            boolean that_present_split = true && that.isSetSplit();
            if (this_present_split || that_present_split) {
                if (!(this_present_split && that_present_split))
                    return false;
                if (!this.split.equals(that.split))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public int compareTo(reportSplitExtent_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(other.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, other.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredentials()).compareTo(other.isSetCredentials());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredentials()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credentials, other.credentials);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetServerName()).compareTo(other.isSetServerName());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetServerName()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.serverName, other.serverName);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetSplit()).compareTo(other.isSetSplit());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSplit()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.split, other.split);
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
            StringBuilder sb = new StringBuilder("reportSplitExtent_args(");
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
            sb.append("credentials:");
            if (this.credentials == null) {
                sb.append("null");
            } else {
                sb.append(this.credentials);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("serverName:");
            if (this.serverName == null) {
                sb.append("null");
            } else {
                sb.append(this.serverName);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("split:");
            if (this.split == null) {
                sb.append("null");
            } else {
                sb.append(this.split);
            }
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credentials != null) {
                credentials.validate();
            }
            if (split != null) {
                split.validate();
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

        private static class reportSplitExtent_argsStandardSchemeFactory implements SchemeFactory {

            public reportSplitExtent_argsStandardScheme getScheme() {
                return new reportSplitExtent_argsStandardScheme();
            }
        }

        private static class reportSplitExtent_argsStandardScheme extends StandardScheme<reportSplitExtent_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, reportSplitExtent_args struct) throws org.apache.thrift.TException {
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
                                struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                                struct.credentials.read(iprot);
                                struct.setCredentialsIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.serverName = iprot.readString();
                                struct.setServerNameIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.split = new TabletSplit();
                                struct.split.read(iprot);
                                struct.setSplitIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, reportSplitExtent_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credentials != null) {
                    oprot.writeFieldBegin(CREDENTIALS_FIELD_DESC);
                    struct.credentials.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.serverName != null) {
                    oprot.writeFieldBegin(SERVER_NAME_FIELD_DESC);
                    oprot.writeString(struct.serverName);
                    oprot.writeFieldEnd();
                }
                if (struct.split != null) {
                    oprot.writeFieldBegin(SPLIT_FIELD_DESC);
                    struct.split.write(oprot);
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

        private static class reportSplitExtent_argsTupleSchemeFactory implements SchemeFactory {

            public reportSplitExtent_argsTupleScheme getScheme() {
                return new reportSplitExtent_argsTupleScheme();
            }
        }

        private static class reportSplitExtent_argsTupleScheme extends TupleScheme<reportSplitExtent_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, reportSplitExtent_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredentials()) {
                    optionals.set(1);
                }
                if (struct.isSetServerName()) {
                    optionals.set(2);
                }
                if (struct.isSetSplit()) {
                    optionals.set(3);
                }
                oprot.writeBitSet(optionals, 4);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredentials()) {
                    struct.credentials.write(oprot);
                }
                if (struct.isSetServerName()) {
                    oprot.writeString(struct.serverName);
                }
                if (struct.isSetSplit()) {
                    struct.split.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, reportSplitExtent_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(4);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                    struct.credentials.read(iprot);
                    struct.setCredentialsIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.serverName = iprot.readString();
                    struct.setServerNameIsSet(true);
                }
                if (incoming.get(3)) {
                    struct.split = new TabletSplit();
                    struct.split.read(iprot);
                    struct.setSplitIsSet(true);
                }
            }
        }
    }

    public static class reportTabletStatus_args implements org.apache.thrift.TBase<reportTabletStatus_args, reportTabletStatus_args._Fields>, java.io.Serializable, Cloneable, Comparable<reportTabletStatus_args> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("reportTabletStatus_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 5);

        private static final org.apache.thrift.protocol.TField CREDENTIALS_FIELD_DESC = new org.apache.thrift.protocol.TField("credentials", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField SERVER_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("serverName", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final org.apache.thrift.protocol.TField STATUS_FIELD_DESC = new org.apache.thrift.protocol.TField("status", org.apache.thrift.protocol.TType.I32, (short) 3);

        private static final org.apache.thrift.protocol.TField TABLET_FIELD_DESC = new org.apache.thrift.protocol.TField("tablet", org.apache.thrift.protocol.TType.STRUCT, (short) 4);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new reportTabletStatus_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new reportTabletStatus_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.TCredentials credentials;

        public String serverName;

        public TabletLoadState status;

        public org.apache.accumulo.core.data.thrift.TKeyExtent tablet;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 5, "tinfo"), CREDENTIALS((short) 1, "credentials"), SERVER_NAME((short) 2, "serverName"), STATUS((short) 3, "status"), TABLET((short) 4, "tablet");

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
                        return CREDENTIALS;
                    case 2:
                        return SERVER_NAME;
                    case 3:
                        return STATUS;
                    case 4:
                        return TABLET;
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
            tmpMap.put(_Fields.CREDENTIALS, new org.apache.thrift.meta_data.FieldMetaData("credentials", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.TCredentials.class)));
            tmpMap.put(_Fields.SERVER_NAME, new org.apache.thrift.meta_data.FieldMetaData("serverName", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.STATUS, new org.apache.thrift.meta_data.FieldMetaData("status", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, TabletLoadState.class)));
            tmpMap.put(_Fields.TABLET, new org.apache.thrift.meta_data.FieldMetaData("tablet", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.data.thrift.TKeyExtent.class)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(reportTabletStatus_args.class, metaDataMap);
        }

        public reportTabletStatus_args() {
        }

        public reportTabletStatus_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, String serverName, TabletLoadState status, org.apache.accumulo.core.data.thrift.TKeyExtent tablet) {
            this();
            this.tinfo = tinfo;
            this.credentials = credentials;
            this.serverName = serverName;
            this.status = status;
            this.tablet = tablet;
        }

        public reportTabletStatus_args(reportTabletStatus_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredentials()) {
                this.credentials = new org.apache.accumulo.core.security.thrift.TCredentials(other.credentials);
            }
            if (other.isSetServerName()) {
                this.serverName = other.serverName;
            }
            if (other.isSetStatus()) {
                this.status = other.status;
            }
            if (other.isSetTablet()) {
                this.tablet = new org.apache.accumulo.core.data.thrift.TKeyExtent(other.tablet);
            }
        }

        public reportTabletStatus_args deepCopy() {
            return new reportTabletStatus_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credentials = null;
            this.serverName = null;
            this.status = null;
            this.tablet = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public reportTabletStatus_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
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

        public org.apache.accumulo.core.security.thrift.TCredentials getCredentials() {
            return this.credentials;
        }

        public reportTabletStatus_args setCredentials(org.apache.accumulo.core.security.thrift.TCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public void unsetCredentials() {
            this.credentials = null;
        }

        public boolean isSetCredentials() {
            return this.credentials != null;
        }

        public void setCredentialsIsSet(boolean value) {
            if (!value) {
                this.credentials = null;
            }
        }

        public String getServerName() {
            return this.serverName;
        }

        public reportTabletStatus_args setServerName(String serverName) {
            this.serverName = serverName;
            return this;
        }

        public void unsetServerName() {
            this.serverName = null;
        }

        public boolean isSetServerName() {
            return this.serverName != null;
        }

        public void setServerNameIsSet(boolean value) {
            if (!value) {
                this.serverName = null;
            }
        }

        public TabletLoadState getStatus() {
            return this.status;
        }

        public reportTabletStatus_args setStatus(TabletLoadState status) {
            this.status = status;
            return this;
        }

        public void unsetStatus() {
            this.status = null;
        }

        public boolean isSetStatus() {
            return this.status != null;
        }

        public void setStatusIsSet(boolean value) {
            if (!value) {
                this.status = null;
            }
        }

        public org.apache.accumulo.core.data.thrift.TKeyExtent getTablet() {
            return this.tablet;
        }

        public reportTabletStatus_args setTablet(org.apache.accumulo.core.data.thrift.TKeyExtent tablet) {
            this.tablet = tablet;
            return this;
        }

        public void unsetTablet() {
            this.tablet = null;
        }

        public boolean isSetTablet() {
            return this.tablet != null;
        }

        public void setTabletIsSet(boolean value) {
            if (!value) {
                this.tablet = null;
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
                case CREDENTIALS:
                    if (value == null) {
                        unsetCredentials();
                    } else {
                        setCredentials((org.apache.accumulo.core.security.thrift.TCredentials) value);
                    }
                    break;
                case SERVER_NAME:
                    if (value == null) {
                        unsetServerName();
                    } else {
                        setServerName((String) value);
                    }
                    break;
                case STATUS:
                    if (value == null) {
                        unsetStatus();
                    } else {
                        setStatus((TabletLoadState) value);
                    }
                    break;
                case TABLET:
                    if (value == null) {
                        unsetTablet();
                    } else {
                        setTablet((org.apache.accumulo.core.data.thrift.TKeyExtent) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIALS:
                    return getCredentials();
                case SERVER_NAME:
                    return getServerName();
                case STATUS:
                    return getStatus();
                case TABLET:
                    return getTablet();
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
                case CREDENTIALS:
                    return isSetCredentials();
                case SERVER_NAME:
                    return isSetServerName();
                case STATUS:
                    return isSetStatus();
                case TABLET:
                    return isSetTablet();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof reportTabletStatus_args)
                return this.equals((reportTabletStatus_args) that);
            return false;
        }

        public boolean equals(reportTabletStatus_args that) {
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
            boolean this_present_credentials = true && this.isSetCredentials();
            boolean that_present_credentials = true && that.isSetCredentials();
            if (this_present_credentials || that_present_credentials) {
                if (!(this_present_credentials && that_present_credentials))
                    return false;
                if (!this.credentials.equals(that.credentials))
                    return false;
            }
            boolean this_present_serverName = true && this.isSetServerName();
            boolean that_present_serverName = true && that.isSetServerName();
            if (this_present_serverName || that_present_serverName) {
                if (!(this_present_serverName && that_present_serverName))
                    return false;
                if (!this.serverName.equals(that.serverName))
                    return false;
            }
            boolean this_present_status = true && this.isSetStatus();
            boolean that_present_status = true && that.isSetStatus();
            if (this_present_status || that_present_status) {
                if (!(this_present_status && that_present_status))
                    return false;
                if (!this.status.equals(that.status))
                    return false;
            }
            boolean this_present_tablet = true && this.isSetTablet();
            boolean that_present_tablet = true && that.isSetTablet();
            if (this_present_tablet || that_present_tablet) {
                if (!(this_present_tablet && that_present_tablet))
                    return false;
                if (!this.tablet.equals(that.tablet))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public int compareTo(reportTabletStatus_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(other.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, other.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredentials()).compareTo(other.isSetCredentials());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredentials()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credentials, other.credentials);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetServerName()).compareTo(other.isSetServerName());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetServerName()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.serverName, other.serverName);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetStatus()).compareTo(other.isSetStatus());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetStatus()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.status, other.status);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetTablet()).compareTo(other.isSetTablet());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTablet()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tablet, other.tablet);
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
            StringBuilder sb = new StringBuilder("reportTabletStatus_args(");
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
            sb.append("credentials:");
            if (this.credentials == null) {
                sb.append("null");
            } else {
                sb.append(this.credentials);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("serverName:");
            if (this.serverName == null) {
                sb.append("null");
            } else {
                sb.append(this.serverName);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("status:");
            if (this.status == null) {
                sb.append("null");
            } else {
                sb.append(this.status);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("tablet:");
            if (this.tablet == null) {
                sb.append("null");
            } else {
                sb.append(this.tablet);
            }
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
            if (credentials != null) {
                credentials.validate();
            }
            if (tablet != null) {
                tablet.validate();
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

        private static class reportTabletStatus_argsStandardSchemeFactory implements SchemeFactory {

            public reportTabletStatus_argsStandardScheme getScheme() {
                return new reportTabletStatus_argsStandardScheme();
            }
        }

        private static class reportTabletStatus_argsStandardScheme extends StandardScheme<reportTabletStatus_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, reportTabletStatus_args struct) throws org.apache.thrift.TException {
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
                                struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                                struct.credentials.read(iprot);
                                struct.setCredentialsIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.serverName = iprot.readString();
                                struct.setServerNameIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                                struct.status = TabletLoadState.findByValue(iprot.readI32());
                                struct.setStatusIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 4:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tablet = new org.apache.accumulo.core.data.thrift.TKeyExtent();
                                struct.tablet.read(iprot);
                                struct.setTabletIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, reportTabletStatus_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credentials != null) {
                    oprot.writeFieldBegin(CREDENTIALS_FIELD_DESC);
                    struct.credentials.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.serverName != null) {
                    oprot.writeFieldBegin(SERVER_NAME_FIELD_DESC);
                    oprot.writeString(struct.serverName);
                    oprot.writeFieldEnd();
                }
                if (struct.status != null) {
                    oprot.writeFieldBegin(STATUS_FIELD_DESC);
                    oprot.writeI32(struct.status.getValue());
                    oprot.writeFieldEnd();
                }
                if (struct.tablet != null) {
                    oprot.writeFieldBegin(TABLET_FIELD_DESC);
                    struct.tablet.write(oprot);
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

        private static class reportTabletStatus_argsTupleSchemeFactory implements SchemeFactory {

            public reportTabletStatus_argsTupleScheme getScheme() {
                return new reportTabletStatus_argsTupleScheme();
            }
        }

        private static class reportTabletStatus_argsTupleScheme extends TupleScheme<reportTabletStatus_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, reportTabletStatus_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredentials()) {
                    optionals.set(1);
                }
                if (struct.isSetServerName()) {
                    optionals.set(2);
                }
                if (struct.isSetStatus()) {
                    optionals.set(3);
                }
                if (struct.isSetTablet()) {
                    optionals.set(4);
                }
                oprot.writeBitSet(optionals, 5);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredentials()) {
                    struct.credentials.write(oprot);
                }
                if (struct.isSetServerName()) {
                    oprot.writeString(struct.serverName);
                }
                if (struct.isSetStatus()) {
                    oprot.writeI32(struct.status.getValue());
                }
                if (struct.isSetTablet()) {
                    struct.tablet.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, reportTabletStatus_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(5);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                    struct.credentials.read(iprot);
                    struct.setCredentialsIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.serverName = iprot.readString();
                    struct.setServerNameIsSet(true);
                }
                if (incoming.get(3)) {
                    struct.status = TabletLoadState.findByValue(iprot.readI32());
                    struct.setStatusIsSet(true);
                }
                if (incoming.get(4)) {
                    struct.tablet = new org.apache.accumulo.core.data.thrift.TKeyExtent();
                    struct.tablet.read(iprot);
                    struct.setTabletIsSet(true);
                }
            }
        }
    }
}