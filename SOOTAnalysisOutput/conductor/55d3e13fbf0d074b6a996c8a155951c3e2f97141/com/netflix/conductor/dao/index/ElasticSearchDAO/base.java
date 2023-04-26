package com.netflix.conductor.dao.index;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.annotations.Trace;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.run.SearchResult;
import com.netflix.conductor.common.run.TaskSummary;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.common.run.WorkflowSummary;
import com.netflix.conductor.core.config.Configuration;
import com.netflix.conductor.core.execution.ApplicationException;
import com.netflix.conductor.core.execution.ApplicationException.Code;
import com.netflix.conductor.dao.IndexDAO;
import com.netflix.conductor.dao.index.query.parser.Expression;
import com.netflix.conductor.dao.index.query.parser.ParserException;
import com.netflix.conductor.metrics.Monitors;

@Trace
@Singleton
public class ElasticSearchDAO implements IndexDAO {

    private static Logger log = LoggerFactory.getLogger(ElasticSearchDAO.class);

    private static final String WORKFLOW_DOC_TYPE = "workflow";

    private static final String TASK_DOC_TYPE = "task";

    private static final String className = ElasticSearchDAO.class.getSimpleName();

    private String indexName;

    private ObjectMapper om;

    private Client client;

    @Inject
    public ElasticSearchDAO(Client client, Configuration config, ObjectMapper om) {
        this.om = om;
        this.client = client;
        this.indexName = config.getProperty("workflow.elasticsearch.index.name", null);
    }

    @Override
    public void index(Workflow workflow) {
        try {
            String id = workflow.getWorkflowId();
            WorkflowSummary summary = new WorkflowSummary(workflow);
            byte[] doc = om.writeValueAsBytes(summary);
            UpdateRequest req = new UpdateRequest(indexName, WORKFLOW_DOC_TYPE, id);
            req.doc(doc);
            req.upsert(doc);
            req.retryOnConflict(5);
            BulkResponse response = client.prepareBulk().add(req).execute().actionGet();
            BulkItemResponse[] indexedItems = response.getItems();
            for (BulkItemResponse indexedItem : indexedItems) {
                if (indexedItem.isFailed()) {
                    log.error("Indexing failed for {}, {}", indexedItem.getType(), indexedItem.getFailureMessage());
                }
            }
        } catch (Throwable e) {
            log.error("Indexing failed {}", e.getMessage(), e);
            Monitors.error(className, "index");
        }
    }

    @Override
    public void index(Task task) {
        try {
            String id = task.getTaskId();
            TaskSummary summary = new TaskSummary(task);
            byte[] doc = om.writeValueAsBytes(summary);
            UpdateRequest req = new UpdateRequest(indexName, TASK_DOC_TYPE, id);
            req.doc(doc);
            req.upsert(doc);
            BulkResponse response = client.prepareBulk().add(req).execute().actionGet();
            BulkItemResponse[] indexedItems = response.getItems();
            for (BulkItemResponse indexedItem : indexedItems) {
                if (indexedItem.isFailed()) {
                    log.error("Indexing failed for {}, {}", indexedItem.getType(), indexedItem.getFailureMessage());
                }
            }
        } catch (Throwable e) {
            log.error("Indexing failed {}", e.getMessage(), e);
            Monitors.error(className, "index");
        }
    }

    @Override
    public SearchResult<String> searchWorkflows(String query, String freeText, int start, int count, List<String> sort) {
        try {
            return search(query, start, count, sort, freeText);
        } catch (ParserException e) {
            throw new ApplicationException(Code.BACKEND_ERROR, e.getMessage(), e);
        }
    }

    @Override
    public void remove(String workflowId) {
        try {
            DeleteRequest req = new DeleteRequest(indexName, WORKFLOW_DOC_TYPE, workflowId);
            DeleteResponse response = client.delete(req).actionGet();
            if (!response.isFound()) {
                log.error("Index removal failed - document not found by id " + workflowId);
            }
        } catch (Throwable e) {
            log.error("Index removal failed failed {}", e.getMessage(), e);
            Monitors.error(className, "remove");
        }
    }

    @Override
    public void update(String workflowInstanceId, String key, Object value) {
        try {
            log.info("updating {} with {} and {}", workflowInstanceId, key, value);
            UpdateRequest request = new UpdateRequest(indexName, WORKFLOW_DOC_TYPE, workflowInstanceId);
            Map<String, Object> source = new HashMap<>();
            source.put(key, value);
            request.doc(source);
            client.update(request).actionGet();
        } catch (Throwable e) {
            log.error("Index update failed {}", e.getMessage(), e);
            Monitors.error(className, "update");
        }
    }

    private SearchResult<String> search(String structuredQuery, int start, int size, List<String> sortOptions, String freeTextQuery) throws ParserException {
        QueryBuilder qf = QueryBuilders.matchAllQuery();
        if (StringUtils.isNotEmpty(structuredQuery)) {
            Expression expression = Expression.fromString(structuredQuery);
            qf = expression.getFilterBuilder();
        }
        BoolQueryBuilder filterQuery = QueryBuilders.boolQuery().must(qf);
        QueryStringQueryBuilder stringQuery = QueryBuilders.queryStringQuery(freeTextQuery);
        BoolQueryBuilder fq = QueryBuilders.boolQuery().must(stringQuery).must(filterQuery);
        final SearchRequestBuilder srb = client.prepareSearch(indexName).setQuery(fq).setTypes(WORKFLOW_DOC_TYPE).setNoFields().setFrom(start).setSize(size);
        if (sortOptions != null) {
            sortOptions.forEach(sortOption -> {
                SortOrder order = SortOrder.ASC;
                String field = sortOption;
                int indx = sortOption.indexOf(':');
                if (indx > 0) {
                    field = sortOption.substring(0, indx);
                    order = SortOrder.valueOf(sortOption.substring(indx + 1));
                }
                srb.addSort(field, order);
            });
        }
        List<String> result = new LinkedList<String>();
        SearchResponse response = srb.execute().actionGet();
        response.getHits().forEach(hit -> {
            result.add(hit.getId());
        });
        long count = response.getHits().getTotalHits();
        return new SearchResult<String>(count, result);
    }
}
