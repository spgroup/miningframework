package org.activiti.engine.impl.db;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntityImpl;
import org.apache.ibatis.session.SqlSessionFactory;

public class DbSqlSessionFactory implements SessionFactory {

    protected static final Map<String, Map<String, String>> databaseSpecificStatements = new HashMap<String, Map<String, String>>();

    public static final Map<String, String> databaseSpecificLimitBeforeStatements = new HashMap<String, String>();

    public static final Map<String, String> databaseSpecificLimitAfterStatements = new HashMap<String, String>();

    public static final Map<String, String> databaseSpecificLimitBetweenStatements = new HashMap<String, String>();

    public static final Map<String, String> databaseSpecificOrderByStatements = new HashMap<String, String>();

    public static final Map<String, String> databaseOuterJoinLimitBetweenStatements = new HashMap<String, String>();

    public static final Map<String, String> databaseSpecificLimitBeforeNativeQueryStatements = new HashMap<String, String>();

    static {
        String defaultOrderBy = " order by ${orderBy} ";
        databaseSpecificLimitBeforeStatements.put("h2", "");
        databaseSpecificLimitAfterStatements.put("h2", "LIMIT #{maxResults} OFFSET #{firstResult}");
        databaseSpecificLimitBetweenStatements.put("h2", "");
        databaseOuterJoinLimitBetweenStatements.put("h2", "");
        databaseSpecificOrderByStatements.put("h2", defaultOrderBy);
        databaseSpecificLimitBeforeStatements.put("hsql", "");
        databaseSpecificLimitAfterStatements.put("hsql", "LIMIT #{maxResults} OFFSET #{firstResult}");
        databaseSpecificLimitBetweenStatements.put("hsql", "");
        databaseOuterJoinLimitBetweenStatements.put("hsql", "");
        databaseSpecificOrderByStatements.put("hsql", defaultOrderBy);
        databaseSpecificLimitBeforeStatements.put("mysql", "");
        databaseSpecificLimitAfterStatements.put("mysql", "LIMIT #{maxResults} OFFSET #{firstResult}");
        databaseSpecificLimitBetweenStatements.put("mysql", "");
        databaseOuterJoinLimitBetweenStatements.put("mysql", "");
        databaseSpecificOrderByStatements.put("mysql", defaultOrderBy);
        addDatabaseSpecificStatement("mysql", "selectProcessDefinitionsByQueryCriteria", "selectProcessDefinitionsByQueryCriteria_mysql");
        addDatabaseSpecificStatement("mysql", "selectProcessDefinitionCountByQueryCriteria", "selectProcessDefinitionCountByQueryCriteria_mysql");
        addDatabaseSpecificStatement("mysql", "selectDeploymentsByQueryCriteria", "selectDeploymentsByQueryCriteria_mysql");
        addDatabaseSpecificStatement("mysql", "selectDeploymentCountByQueryCriteria", "selectDeploymentCountByQueryCriteria_mysql");
        addDatabaseSpecificStatement("mysql", "selectModelCountByQueryCriteria", "selectModelCountByQueryCriteria_mysql");
        addDatabaseSpecificStatement("mysql", "updateExecutionTenantIdForDeployment", "updateExecutionTenantIdForDeployment_mysql");
        addDatabaseSpecificStatement("mysql", "updateTaskTenantIdForDeployment", "updateTaskTenantIdForDeployment_mysql");
        addDatabaseSpecificStatement("mysql", "updateJobTenantIdForDeployment", "updateJobTenantIdForDeployment_mysql");
        databaseSpecificLimitBeforeStatements.put("postgres", "");
        databaseSpecificLimitAfterStatements.put("postgres", "LIMIT #{maxResults} OFFSET #{firstResult}");
        databaseSpecificLimitBetweenStatements.put("postgres", "");
        databaseOuterJoinLimitBetweenStatements.put("postgres", "");
        databaseSpecificOrderByStatements.put("postgres", defaultOrderBy);
        addDatabaseSpecificStatement("postgres", "insertByteArray", "insertByteArray_postgres");
        addDatabaseSpecificStatement("postgres", "bulkInsertByteArray", "bulkInsertByteArray_postgres");
        addDatabaseSpecificStatement("postgres", "updateByteArray", "updateByteArray_postgres");
        addDatabaseSpecificStatement("postgres", "selectByteArray", "selectByteArray_postgres");
        addDatabaseSpecificStatement("postgres", "selectResourceByDeploymentIdAndResourceName", "selectResourceByDeploymentIdAndResourceName_postgres");
        addDatabaseSpecificStatement("postgres", "selectResourcesByDeploymentId", "selectResourcesByDeploymentId_postgres");
        addDatabaseSpecificStatement("postgres", "insertIdentityInfo", "insertIdentityInfo_postgres");
        addDatabaseSpecificStatement("postgres", "bulkInsertIdentityInfo", "bulkInsertIdentityInfo_postgres");
        addDatabaseSpecificStatement("postgres", "updateIdentityInfo", "updateIdentityInfo_postgres");
        addDatabaseSpecificStatement("postgres", "selectIdentityInfoById", "selectIdentityInfoById_postgres");
        addDatabaseSpecificStatement("postgres", "selectIdentityInfoByUserIdAndKey", "selectIdentityInfoByUserIdAndKey_postgres");
        addDatabaseSpecificStatement("postgres", "selectIdentityInfoByUserId", "selectIdentityInfoByUserId_postgres");
        addDatabaseSpecificStatement("postgres", "selectIdentityInfoDetails", "selectIdentityInfoDetails_postgres");
        addDatabaseSpecificStatement("postgres", "insertComment", "insertComment_postgres");
        addDatabaseSpecificStatement("postgres", "bulkInsertComment", "bulkInsertComment_postgres");
        addDatabaseSpecificStatement("postgres", "selectComment", "selectComment_postgres");
        addDatabaseSpecificStatement("postgres", "selectCommentsByTaskId", "selectCommentsByTaskId_postgres");
        addDatabaseSpecificStatement("postgres", "selectCommentsByProcessInstanceId", "selectCommentsByProcessInstanceId_postgres");
        addDatabaseSpecificStatement("postgres", "selectCommentsByProcessInstanceIdAndType", "selectCommentsByProcessInstanceIdAndType_postgres");
        addDatabaseSpecificStatement("postgres", "selectCommentsByType", "selectCommentsByType_postgres");
        addDatabaseSpecificStatement("postgres", "selectCommentsByTaskIdAndType", "selectCommentsByTaskIdAndType_postgres");
        addDatabaseSpecificStatement("postgres", "selectEventsByTaskId", "selectEventsByTaskId_postgres");
        addDatabaseSpecificStatement("postgres", "insertEventLogEntry", "insertEventLogEntry_postgres");
        addDatabaseSpecificStatement("postgres", "bulkInsertEventLogEntry", "bulkInsertEventLogEntry_postgres");
        addDatabaseSpecificStatement("postgres", "selectAllEventLogEntries", "selectAllEventLogEntries_postgres");
        addDatabaseSpecificStatement("postgres", "selectEventLogEntries", "selectEventLogEntries_postgres");
        addDatabaseSpecificStatement("postgres", "selectEventLogEntriesByProcessInstanceId", "selectEventLogEntriesByProcessInstanceId_postgres");
        databaseSpecificLimitBeforeStatements.put("oracle", "select * from ( select a.*, ROWNUM rnum from (");
        databaseSpecificLimitAfterStatements.put("oracle", "  ) a where ROWNUM < #{lastRow}) where rnum  >= #{firstRow}");
        databaseSpecificLimitBetweenStatements.put("oracle", "");
        databaseOuterJoinLimitBetweenStatements.put("oracle", "");
        databaseSpecificOrderByStatements.put("oracle", defaultOrderBy);
        addDatabaseSpecificStatement("oracle", "selectExclusiveJobsToExecute", "selectExclusiveJobsToExecute_integerBoolean");
        addDatabaseSpecificStatement("oracle", "selectUnlockedTimersByDuedate", "selectUnlockedTimersByDuedate_oracle");
        addDatabaseSpecificStatement("oracle", "insertEventLogEntry", "insertEventLogEntry_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertVariableInstance", "bulkInsertVariableInstance_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertUser", "bulkInsertUser_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertTask", "bulkInsertTask_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertResource", "bulkInsertResource_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertProperty", "bulkInsertProperty_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertProcessDefinition", "bulkInsertProcessDefinition_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertModel", "bulkInsertModel_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertMembership", "bulkInsertMembership_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertTimer", "bulkInsertTimer_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertMessage", "bulkInsertMessage_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertIdentityInfo", "bulkInsertIdentityInfo_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertIdentityLink", "bulkInsertIdentityLink_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertMembership", "bulkInsertMembership_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertTimer", "bulkInsertTimer_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertMessage", "bulkInsertMessage_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertHistoricVariableInstance", "bulkInsertHistoricVariableInstance_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertHistoricTaskInstance", "bulkInsertHistoricTaskInstance_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertHistoricProcessInstance", "bulkInsertHistoricProcessInstance_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertHistoricIdentityLink", "bulkInsertHistoricIdentityLink_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertHistoricDetailVariableInstanceUpdate", "bulkInsertHistoricDetailVariableInstanceUpdate_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertHistoricFormProperty", "bulkInsertHistoricFormProperty_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertHistoricActivityInstance", "bulkInsertHistoricActivityInstance_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertGroup", "bulkInsertGroup_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertExecution", "bulkInsertExecution_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertMessageEventSubscription", "bulkInsertMessageEventSubscription_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertSignalEventSubscription", "bulkInsertSignalEventSubscription_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertCompensateEventSubscription", "bulkInsertCompensateEventSubscription_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertEventLogEntry", "bulkInsertEventLogEntry_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertDeployment", "bulkInsertDeployment_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertComment", "bulkInsertComment_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertByteArray", "bulkInsertByteArray_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertEventLogEntry", "bulkInsertEventLogEntry_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertDeployment", "bulkInsertDeployment_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertComment", "bulkInsertComment_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertByteArray", "bulkInsertByteArray_oracle");
        addDatabaseSpecificStatement("oracle", "bulkInsertAttachment", "bulkInsertAttachment_oracle");
        databaseSpecificLimitBeforeStatements.put("db2", "SELECT SUB.* FROM (");
        databaseSpecificLimitAfterStatements.put("db2", ")RES ) SUB WHERE SUB.rnk >= #{firstRow} AND SUB.rnk < #{lastRow}");
        databaseSpecificLimitBetweenStatements.put("db2", ", row_number() over (ORDER BY ${orderBy}) rnk FROM ( select distinct RES.* ");
        databaseOuterJoinLimitBetweenStatements.put("db2", ", row_number() over (ORDER BY ${mssqlOrDB2OrderBy}) rnk FROM ( select distinct ");
        databaseSpecificOrderByStatements.put("db2", "");
        databaseSpecificLimitBeforeNativeQueryStatements.put("db2", "SELECT SUB.* FROM ( select RES.* , row_number() over (ORDER BY ${orderBy}) rnk FROM (");
        addDatabaseSpecificStatement("db2", "selectExclusiveJobsToExecute", "selectExclusiveJobsToExecute_integerBoolean");
        addDatabaseSpecificStatement("db2", "selectExecutionByNativeQuery", "selectExecutionByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("db2", "selectHistoricActivityInstanceByNativeQuery", "selectHistoricActivityInstanceByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("db2", "selectHistoricProcessInstanceByNativeQuery", "selectHistoricProcessInstanceByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("db2", "selectHistoricTaskInstanceByNativeQuery", "selectHistoricTaskInstanceByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("db2", "selectTaskByNativeQuery", "selectTaskByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("db2", "selectProcessDefinitionByNativeQuery", "selectProcessDefinitionByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("db2", "selectDeploymentByNativeQuery", "selectDeploymentByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("db2", "selectGroupByNativeQuery", "selectGroupByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("db2", "selectUserByNativeQuery", "selectUserByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("db2", "selectModelByNativeQuery", "selectModelByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("db2", "selectHistoricDetailByNativeQuery", "selectHistoricDetailByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("db2", "selectHistoricVariableInstanceByNativeQuery", "selectHistoricVariableInstanceByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("db2", "selectTaskWithVariablesByQueryCriteria", "selectTaskWithVariablesByQueryCriteria_mssql_or_db2");
        addDatabaseSpecificStatement("db2", "selectProcessInstanceWithVariablesByQueryCriteria", "selectProcessInstanceWithVariablesByQueryCriteria_mssql_or_db2");
        addDatabaseSpecificStatement("db2", "selectHistoricProcessInstancesWithVariablesByQueryCriteria", "selectHistoricProcessInstancesWithVariablesByQueryCriteria_mssql_or_db2");
        addDatabaseSpecificStatement("db2", "selectHistoricTaskInstancesWithVariablesByQueryCriteria", "selectHistoricTaskInstancesWithVariablesByQueryCriteria_mssql_or_db2");
        databaseSpecificLimitBeforeStatements.put("mssql", "SELECT SUB.* FROM (");
        databaseSpecificLimitAfterStatements.put("mssql", ")RES ) SUB WHERE SUB.rnk >= #{firstRow} AND SUB.rnk < #{lastRow}");
        databaseSpecificLimitBetweenStatements.put("mssql", ", row_number() over (ORDER BY ${orderBy}) rnk FROM ( select distinct RES.* ");
        databaseOuterJoinLimitBetweenStatements.put("mssql", ", row_number() over (ORDER BY ${mssqlOrDB2OrderBy}) rnk FROM ( select distinct ");
        databaseSpecificOrderByStatements.put("mssql", "");
        databaseSpecificLimitBeforeNativeQueryStatements.put("mssql", "SELECT SUB.* FROM ( select RES.* , row_number() over (ORDER BY ${orderBy}) rnk FROM (");
        addDatabaseSpecificStatement("mssql", "selectExclusiveJobsToExecute", "selectExclusiveJobsToExecute_integerBoolean");
        addDatabaseSpecificStatement("mssql", "selectExecutionByNativeQuery", "selectExecutionByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("mssql", "selectHistoricActivityInstanceByNativeQuery", "selectHistoricActivityInstanceByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("mssql", "selectHistoricProcessInstanceByNativeQuery", "selectHistoricProcessInstanceByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("mssql", "selectHistoricTaskInstanceByNativeQuery", "selectHistoricTaskInstanceByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("mssql", "selectTaskByNativeQuery", "selectTaskByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("mssql", "selectProcessDefinitionByNativeQuery", "selectProcessDefinitionByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("mssql", "selectDeploymentByNativeQuery", "selectDeploymentByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("mssql", "selectGroupByNativeQuery", "selectGroupByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("mssql", "selectUserByNativeQuery", "selectUserByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("mssql", "selectModelByNativeQuery", "selectModelByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("mssql", "selectHistoricDetailByNativeQuery", "selectHistoricDetailByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("mssql", "selectHistoricVariableInstanceByNativeQuery", "selectHistoricVariableInstanceByNativeQuery_mssql_or_db2");
        addDatabaseSpecificStatement("mssql", "selectTaskWithVariablesByQueryCriteria", "selectTaskWithVariablesByQueryCriteria_mssql_or_db2");
        addDatabaseSpecificStatement("mssql", "selectProcessInstanceWithVariablesByQueryCriteria", "selectProcessInstanceWithVariablesByQueryCriteria_mssql_or_db2");
        addDatabaseSpecificStatement("mssql", "selectHistoricProcessInstancesWithVariablesByQueryCriteria", "selectHistoricProcessInstancesWithVariablesByQueryCriteria_mssql_or_db2");
        addDatabaseSpecificStatement("mssql", "selectHistoricTaskInstancesWithVariablesByQueryCriteria", "selectHistoricTaskInstancesWithVariablesByQueryCriteria_mssql_or_db2");
    }

    protected static Map<Class<? extends Entity>, Boolean> bulkInsertableMap;

    protected String databaseType;

    protected String databaseTablePrefix = "";

    private boolean tablePrefixIsSchema;

    protected String databaseCatalog;

    protected String databaseSchema;

    protected SqlSessionFactory sqlSessionFactory;

    protected IdGenerator idGenerator;

    protected Map<String, String> statementMappings;

    protected Map<Class<?>, String> insertStatements = new ConcurrentHashMap<Class<?>, String>();

    protected Map<Class<?>, String> bulkInsertStatements = new ConcurrentHashMap<Class<?>, String>();

    protected Map<Class<?>, String> updateStatements = new ConcurrentHashMap<Class<?>, String>();

    protected Map<Class<?>, String> deleteStatements = new ConcurrentHashMap<Class<?>, String>();

    protected Map<Class<?>, String> bulkDeleteStatements = new ConcurrentHashMap<Class<?>, String>();

    protected Map<Class<?>, String> selectStatements = new ConcurrentHashMap<Class<?>, String>();

    protected boolean isDbIdentityUsed = true;

    protected boolean isDbHistoryUsed = true;

    protected int maxNrOfStatementsInBulkInsert = 100;

    public Class<?> getSessionType() {
        return DbSqlSession.class;
    }

    public Session openSession(CommandContext commandContext) {
        return new DbSqlSession(this, commandContext.getEntityCache());
    }

    public String getInsertStatement(Entity object) {
        return getStatement(object.getClass(), insertStatements, "insert");
    }

    public String getInsertStatement(Class<? extends Entity> clazz) {
        return getStatement(clazz, insertStatements, "insert");
    }

    @SuppressWarnings("rawtypes")
    public String getBulkInsertStatement(Class clazz) {
        return getStatement(clazz, bulkInsertStatements, "bulkInsert");
    }

    public String getUpdateStatement(Entity object) {
        return getStatement(object.getClass(), updateStatements, "update");
    }

    public String getDeleteStatement(Class<?> entityClass) {
        return getStatement(entityClass, deleteStatements, "delete");
    }

    public String getBulkDeleteStatement(Class<?> entityClass) {
        return getStatement(entityClass, bulkDeleteStatements, "bulkDelete");
    }

    public String getSelectStatement(Class<?> entityClass) {
        return getStatement(entityClass, selectStatements, "select");
    }

    private String getStatement(Class<?> entityClass, Map<Class<?>, String> cachedStatements, String prefix) {
        String statement = cachedStatements.get(entityClass);
        if (statement != null) {
            return statement;
        }
        statement = prefix + entityClass.getSimpleName();
        if (statement.endsWith("Impl")) {
            statement = statement.substring(0, statement.length() - 10);
        } else {
            statement = statement.substring(0, statement.length() - 6);
        }
        cachedStatements.put(entityClass, statement);
        return statement;
    }

    protected static void addDatabaseSpecificStatement(String databaseType, String activitiStatement, String ibatisStatement) {
        Map<String, String> specificStatements = databaseSpecificStatements.get(databaseType);
        if (specificStatements == null) {
            specificStatements = new HashMap<String, String>();
            databaseSpecificStatements.put(databaseType, specificStatements);
        }
        specificStatements.put(activitiStatement, ibatisStatement);
    }

    public String mapStatement(String statement) {
        if (statementMappings == null) {
            return statement;
        }
        String mappedStatement = statementMappings.get(statement);
        return (mappedStatement != null ? mappedStatement : statement);
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
        this.statementMappings = databaseSpecificStatements.get(databaseType);
    }

    public void setBulkInsertEnabled(boolean isBulkInsertEnabled, String databaseType) {
        if (isBulkInsertEnabled) {
            initBulkInsertEnabledMap(databaseType);
        }
    }

    protected void initBulkInsertEnabledMap(String databaseType) {
        bulkInsertableMap = new HashMap<Class<? extends Entity>, Boolean>();
        for (Class<? extends Entity> clazz : EntityDependencyOrder.INSERT_ORDER) {
            bulkInsertableMap.put(clazz, Boolean.TRUE);
        }
        if ("oracle".equals(databaseType)) {
            bulkInsertableMap.put(EventLogEntryEntityImpl.class, Boolean.FALSE);
        }
    }

    public Boolean isBulkInsertable(Class<? extends Entity> entityClass) {
        return bulkInsertableMap != null && bulkInsertableMap.containsKey(entityClass) && bulkInsertableMap.get(entityClass) == true;
    }

    public SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public Map<String, String> getStatementMappings() {
        return statementMappings;
    }

    public void setStatementMappings(Map<String, String> statementMappings) {
        this.statementMappings = statementMappings;
    }

    public Map<Class<?>, String> getInsertStatements() {
        return insertStatements;
    }

    public void setInsertStatements(Map<Class<?>, String> insertStatements) {
        this.insertStatements = insertStatements;
    }

    public Map<Class<?>, String> getBulkInsertStatements() {
        return bulkInsertStatements;
    }

    public void setBulkInsertStatements(Map<Class<?>, String> bulkInsertStatements) {
        this.bulkInsertStatements = bulkInsertStatements;
    }

    public Map<Class<?>, String> getUpdateStatements() {
        return updateStatements;
    }

    public void setUpdateStatements(Map<Class<?>, String> updateStatements) {
        this.updateStatements = updateStatements;
    }

    public Map<Class<?>, String> getDeleteStatements() {
        return deleteStatements;
    }

    public void setDeleteStatements(Map<Class<?>, String> deleteStatements) {
        this.deleteStatements = deleteStatements;
    }

    public Map<Class<?>, String> getBulkDeleteStatements() {
        return bulkDeleteStatements;
    }

    public void setBulkDeleteStatements(Map<Class<?>, String> bulkDeleteStatements) {
        this.bulkDeleteStatements = bulkDeleteStatements;
    }

    public Map<Class<?>, String> getSelectStatements() {
        return selectStatements;
    }

    public void setSelectStatements(Map<Class<?>, String> selectStatements) {
        this.selectStatements = selectStatements;
    }

    public boolean isDbIdentityUsed() {
        return isDbIdentityUsed;
    }

    public void setDbIdentityUsed(boolean isDbIdentityUsed) {
        this.isDbIdentityUsed = isDbIdentityUsed;
    }

    public boolean isDbHistoryUsed() {
        return isDbHistoryUsed;
    }

    public void setDbHistoryUsed(boolean isDbHistoryUsed) {
        this.isDbHistoryUsed = isDbHistoryUsed;
    }

    public void setDatabaseTablePrefix(String databaseTablePrefix) {
        this.databaseTablePrefix = databaseTablePrefix;
    }

    public String getDatabaseTablePrefix() {
        return databaseTablePrefix;
    }

    public String getDatabaseCatalog() {
        return databaseCatalog;
    }

    public void setDatabaseCatalog(String databaseCatalog) {
        this.databaseCatalog = databaseCatalog;
    }

    public String getDatabaseSchema() {
        return databaseSchema;
    }

    public void setDatabaseSchema(String databaseSchema) {
        this.databaseSchema = databaseSchema;
    }

    public void setTablePrefixIsSchema(boolean tablePrefixIsSchema) {
        this.tablePrefixIsSchema = tablePrefixIsSchema;
    }

    public boolean isTablePrefixIsSchema() {
        return tablePrefixIsSchema;
    }

    public int getMaxNrOfStatementsInBulkInsert() {
        return maxNrOfStatementsInBulkInsert;
    }

    public void setMaxNrOfStatementsInBulkInsert(int maxNrOfStatementsInBulkInsert) {
        this.maxNrOfStatementsInBulkInsert = maxNrOfStatementsInBulkInsert;
    }
}
