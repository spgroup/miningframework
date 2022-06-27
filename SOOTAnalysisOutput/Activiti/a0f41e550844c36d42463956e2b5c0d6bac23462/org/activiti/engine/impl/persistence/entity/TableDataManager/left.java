package org.activiti.engine.impl.persistence.entity;

import java.util.List;
import java.util.Map;
import org.activiti.engine.impl.TablePageQueryImpl;
import org.activiti.engine.management.TableMetaData;
import org.activiti.engine.management.TablePage;

public interface TableDataManager {

    Map<String, Long> getTableCount();

    List<String> getTablesPresentInDatabase();

    TablePage getTablePage(TablePageQueryImpl tablePageQuery, int firstResult, int maxResults);

    String getTableName(Class<?> entityClass, boolean withPrefix);

    TableMetaData getTableMetaData(String tableName);
}
