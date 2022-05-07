/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.persistence.entity;

import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.TablePageQueryImpl;
import org.activiti.engine.management.TableMetaData;
import org.activiti.engine.management.TablePage;

/**
 * @author Joram Barrez
 */
public interface TableDataManager {

  Map<String, Long> getTableCount();

<<<<<<< MINE
List<String> getTablesPresentInDatabase();
=======
public List<String> getTablesPresentInDatabase() {
    List<String> tableNames = new ArrayList<String>();
    Connection connection = null;
    try {
      connection = getDbSqlSession().getSqlSession().getConnection();
      DatabaseMetaData databaseMetaData = connection.getMetaData();
      ResultSet tables = null;
      try {
        log.debug("retrieving activiti tables from jdbc metadata");
        String databaseTablePrefix = getDbSqlSession().getDbSqlSessionFactory().getDatabaseTablePrefix();
        String tableNameFilter = databaseTablePrefix+"ACT_%";
        if ("postgres".equals(getDbSqlSession().getDbSqlSessionFactory().getDatabaseType())) {
          tableNameFilter = databaseTablePrefix+"act\\_%";
        }
        if ("oracle".equals(getDbSqlSession().getDbSqlSessionFactory().getDatabaseType())) {
          tableNameFilter = databaseTablePrefix+"ACT" + databaseMetaData.getSearchStringEscape() + "_%";
        }
        
        String catalog = null;
        if (getProcessEngineConfiguration().getDatabaseCatalog() != null && getProcessEngineConfiguration().getDatabaseCatalog().length() > 0) {
          catalog = getProcessEngineConfiguration().getDatabaseCatalog();
        }
        
        String schema = null;
        if (getProcessEngineConfiguration().getDatabaseSchema() != null && getProcessEngineConfiguration().getDatabaseSchema().length() > 0) {
          if ("oracle".equals(getDbSqlSession().getDbSqlSessionFactory().getDatabaseType())) {
            schema = getProcessEngineConfiguration().getDatabaseSchema().toUpperCase();
          } else {
            schema = getProcessEngineConfiguration().getDatabaseSchema();
          }
        }
        
        tables = databaseMetaData.getTables(catalog, schema, tableNameFilter, getDbSqlSession().JDBC_METADATA_TABLE_TYPES);
        while (tables.next()) {
          String tableName = tables.getString("TABLE_NAME");
          tableName = tableName.toUpperCase();
          tableNames.add(tableName);
          log.debug("  retrieved activiti table name {}", tableName);
        }
      } finally {
        tables.close();
      }
    } catch (Exception e) {
      throw new ActivitiException("couldn't get activiti table names using metadata: "+e.getMessage(), e); 
    }
    return tableNames;
  }
>>>>>>> YOURS


  TablePage getTablePage(TablePageQueryImpl tablePageQuery, int firstResult, int maxResults);

  String getTableName(Class<?> entityClass, boolean withPrefix);

<<<<<<< MINE
TableMetaData getTableMetaData(String tableName);
=======
public TableMetaData getTableMetaData(String tableName) {
    TableMetaData result = new TableMetaData();
    try {
      result.setTableName(tableName);
      DatabaseMetaData metaData = getDbSqlSession()
        .getSqlSession()
        .getConnection()
        .getMetaData();

      if ("postgres".equals(getDbSqlSession().getDbSqlSessionFactory().getDatabaseType())) {
        tableName = tableName.toLowerCase();
      }
      
      String catalog = null;
      if (getProcessEngineConfiguration().getDatabaseCatalog() != null && getProcessEngineConfiguration().getDatabaseCatalog().length() > 0) {
        catalog = getProcessEngineConfiguration().getDatabaseCatalog();
      }
      
      String schema = null;
      if (getProcessEngineConfiguration().getDatabaseSchema() != null && getProcessEngineConfiguration().getDatabaseSchema().length() > 0) {
        if ("oracle".equals(getDbSqlSession().getDbSqlSessionFactory().getDatabaseType())) {
          schema = getProcessEngineConfiguration().getDatabaseSchema().toUpperCase();
        } else {
          schema = getProcessEngineConfiguration().getDatabaseSchema();
        }
      }

      ResultSet resultSet = metaData.getColumns(catalog, schema, tableName, null);
      while(resultSet.next()) {
        boolean wrongSchema = false;
        if (schema != null && schema.length() > 0) {
          for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
            String columnName = resultSet.getMetaData().getColumnName(i+1);
            if ("TABLE_SCHEM".equalsIgnoreCase(columnName) || "TABLE_SCHEMA".equalsIgnoreCase(columnName)) {
              if (schema.equalsIgnoreCase(resultSet.getString(resultSet.getMetaData().getColumnName(i+1))) == false) {
                wrongSchema = true;
              }
              break;
            }
          }
        }
        
        if (wrongSchema == false) {
          String name = resultSet.getString("COLUMN_NAME").toUpperCase();
          String type = resultSet.getString("TYPE_NAME").toUpperCase();
          result.addColumnMetaData(name, type);
        }
      }
      
    } catch (SQLException e) {
      throw new ActivitiException("Could not retrieve database metadata: " + e.getMessage());
    }

    if(result.getColumnNames().isEmpty()) {
      // According to API, when a table doesn't exist, null should be returned
      result = null;
    }
    return result;
  }
>>>>>>> YOURS


}