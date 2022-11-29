package com.orientechnologies.orient.server.task;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.Callable;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.config.OServerUserConfiguration;
import com.orientechnologies.orient.server.distributed.ODistributedAbstractPlugin;
import com.orientechnologies.orient.server.distributed.ODistributedServerManager;
import com.orientechnologies.orient.server.distributed.ODistributedServerManager.EXECUTION_MODE;
import com.orientechnologies.orient.server.distributed.OStorageSynchronizer;

public abstract class OAbstractDistributedTask<T> implements Callable<T>, Externalizable {

    private static final long serialVersionUID = 1L;

    public enum STATUS {

        DISTRIBUTE, REMOTE_EXEC, ALIGN, LOCAL_EXEC
    }

    public enum EXEC_TYPE {

        LOCAL_ONLY, REMOTE_ONLY, BOTH
    }

    protected String nodeSource;

    protected String databaseName;

    protected long runId;

    protected long operationSerial;

    protected EXECUTION_MODE mode;

    protected STATUS status;

    protected EXEC_TYPE executionType = EXEC_TYPE.BOTH;

    protected boolean inheritedDatabase;

    protected static OServerUserConfiguration replicatorUser;

    static {
        replicatorUser = OServerMain.server().getUser(ODistributedAbstractPlugin.REPLICATOR_USER);
    }

    public OAbstractDistributedTask() {
        status = STATUS.REMOTE_EXEC;
    }

    public OAbstractDistributedTask(final long iRunId, final long iOperationId) {
        this.runId = iRunId;
        this.operationSerial = iOperationId;
        this.status = STATUS.ALIGN;
    }

    public OAbstractDistributedTask(final String nodeSource, final String databaseName, final EXECUTION_MODE iMode) {
        this.nodeSource = nodeSource;
        this.databaseName = databaseName;
        this.mode = iMode;
        this.status = STATUS.DISTRIBUTE;
        this.runId = getDistributedServerManager().getRunId();
        this.operationSerial = getDistributedServerManager().incrementDistributedSerial(databaseName);
    }

    public void handleConflict(final String iRemoteNode, Object localResult, Object remoteResult) {
    }

    public abstract String getName();

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(nodeSource);
        out.writeUTF(databaseName);
        out.writeLong(runId);
        out.writeLong(operationSerial);
        out.writeByte(mode.ordinal());
        out.writeByte(status.ordinal());
        out.writeByte(executionType.ordinal());
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        nodeSource = in.readUTF();
        databaseName = in.readUTF();
        runId = in.readLong();
        operationSerial = in.readLong();
        mode = EXECUTION_MODE.values()[in.readByte()];
        status = STATUS.values()[in.readByte()];
        executionType = EXEC_TYPE.values()[in.readByte()];
    }

    public String getNodeSource() {
        return nodeSource;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public long getOperationSerial() {
        return operationSerial;
    }

    public long getRunId() {
        return runId;
    }

    public EXECUTION_MODE getMode() {
        return mode;
    }

    public void setMode(final EXECUTION_MODE iMode) {
        mode = iMode;
    }

    public STATUS getStatus() {
        return status;
    }

    public OAbstractDistributedTask<T> setStatus(final STATUS status) {
        this.status = status;
        return this;
    }

    public void setNodeSource(String nodeSource) {
        this.nodeSource = nodeSource;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public String toString() {
        return getName();
    }

    protected OStorageSynchronizer getDatabaseSynchronizer() {
        return getDistributedServerManager().getDatabaseSynchronizer(databaseName);
    }

    protected ODistributedServerManager getDistributedServerManager() {
        return (ODistributedServerManager) OServerMain.server().getVariable("ODistributedAbstractPlugin");
    }

    protected void setAsCompleted(final OStorageSynchronizer dbSynchronizer, long operationLogOffset) throws IOException {
        dbSynchronizer.getLog().changeOperationStatus(operationLogOffset, null);
    }

    protected ODatabaseDocumentTx openDatabase() {
        inheritedDatabase = true;
        final ODatabaseRecord db = ODatabaseRecordThreadLocal.INSTANCE.getIfDefined();
        if (db != null && db.getName().equals(databaseName) && !db.isClosed()) {
            if (db instanceof ODatabaseDocumentTx)
                return (ODatabaseDocumentTx) db;
            else if (db.getDatabaseOwner() instanceof ODatabaseDocumentTx)
                return (ODatabaseDocumentTx) db.getDatabaseOwner();
        }
        inheritedDatabase = false;
        return (ODatabaseDocumentTx) OServerMain.server().openDatabase("document", databaseName, replicatorUser.name, replicatorUser.password);
    }

    protected void closeDatabase(final ODatabaseDocumentTx iDatabase) {
        if (!inheritedDatabase)
            iDatabase.close();
    }

    public EXEC_TYPE getExecutionType() {
        return executionType;
    }

    public void setExecutionType(EXEC_TYPE executionMode) {
        this.executionType = executionMode;
    }
}
