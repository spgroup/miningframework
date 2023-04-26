package org.activiti.rest.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import junit.framework.AssertionFailedError;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.test.PvmTestCase;
import org.activiti.engine.impl.test.TestHelper;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.conf.ApplicationConfiguration;
import org.activiti.rest.service.api.RestUrlBuilder;
import org.activiti.rest.util.TestServerUtil;
import org.activiti.rest.util.TestServerUtil.TestServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.server.Server;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

public class BaseSpringRestTestCase extends PvmTestCase {

    private static Logger log = LoggerFactory.getLogger(BaseSpringRestTestCase.class);

    protected static final List<String> TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK = Arrays.asList("ACT_GE_PROPERTY");

    protected static String SERVER_URL_PREFIX;

    protected static RestUrlBuilder URL_BUILDER;

    protected static Server server;

    protected static ApplicationContext appContext;

    protected ObjectMapper objectMapper = new ObjectMapper();

    protected static ProcessEngine processEngine;

    protected String deploymentId;

    protected Throwable exception;

    protected static ProcessEngineConfigurationImpl processEngineConfiguration;

    protected static RepositoryService repositoryService;

    protected static RuntimeService runtimeService;

    protected static TaskService taskService;

    protected static FormService formService;

    protected static HistoryService historyService;

    protected static IdentityService identityService;

    protected static ManagementService managementService;

    protected static CloseableHttpClient client;

    protected static LinkedList<CloseableHttpResponse> httpResponses = new LinkedList<CloseableHttpResponse>();

    protected ISO8601DateFormat dateFormat = new ISO8601DateFormat();

    static {
        TestServer testServer = TestServerUtil.createAndStartServer(ApplicationConfiguration.class);
        server = testServer.getServer();
        appContext = testServer.getApplicationContext();
        SERVER_URL_PREFIX = testServer.getServerUrlPrefix();
        URL_BUILDER = RestUrlBuilder.usingBaseUrl(SERVER_URL_PREFIX);
        processEngine = appContext.getBean("processEngine", ProcessEngine.class);
        processEngineConfiguration = appContext.getBean(ProcessEngineConfigurationImpl.class);
        repositoryService = appContext.getBean(RepositoryService.class);
        runtimeService = appContext.getBean(RuntimeService.class);
        taskService = appContext.getBean(TaskService.class);
        formService = appContext.getBean(FormService.class);
        historyService = appContext.getBean(HistoryService.class);
        identityService = appContext.getBean(IdentityService.class);
        managementService = appContext.getBean(ManagementService.class);
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("kermit", "kermit");
        provider.setCredentials(AuthScope.ANY, credentials);
        client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                if (client != null) {
                    try {
                        client.close();
                    } catch (IOException e) {
                        log.error("Could not close http client", e);
                    }
                }
                if (server != null && server.isRunning()) {
                    try {
                        server.stop();
                    } catch (Exception e) {
                        log.error("Error stopping server", e);
                    }
                }
            }
        });
    }

    @Override
    public void runBare() throws Throwable {
        createUsers();
        try {
            deploymentId = TestHelper.annotationDeploymentSetUp(processEngine, getClass(), getName());
            super.runBare();
        } catch (AssertionFailedError e) {
            log.error(EMPTY_LINE);
            log.error("ASSERTION FAILED: {}", e, e);
            exception = e;
            throw e;
        } catch (Throwable e) {
            log.error(EMPTY_LINE);
            log.error("EXCEPTION: {}", e, e);
            exception = e;
            throw e;
        } finally {
            TestHelper.annotationDeploymentTearDown(processEngine, deploymentId, getClass(), getName());
            dropUsers();
            assertAndEnsureCleanDb();
            processEngineConfiguration.getClock().reset();
            closeHttpConnections();
        }
    }

    protected void createUsers() {
        User user = identityService.newUser("kermit");
        user.setFirstName("Kermit");
        user.setLastName("the Frog");
        user.setPassword("kermit");
        identityService.saveUser(user);
        Group group = identityService.newGroup("admin");
        group.setName("Administrators");
        identityService.saveGroup(group);
        identityService.createMembership(user.getId(), group.getId());
    }

    public CloseableHttpResponse executeRequest(HttpUriRequest request, int expectedStatusCode) {
        return internalExecuteRequest(request, expectedStatusCode, true);
    }

    public CloseableHttpResponse executeBinaryRequest(HttpUriRequest request, int expectedStatusCode) {
        return internalExecuteRequest(request, expectedStatusCode, false);
    }

    protected CloseableHttpResponse internalExecuteRequest(HttpUriRequest request, int expectedStatusCode, boolean addJsonContentType) {
        CloseableHttpResponse response = null;
        try {
            if (addJsonContentType && request.getFirstHeader(HttpHeaders.CONTENT_TYPE) == null) {
                request.addHeader(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"));
            }
            response = client.execute(request);
            Assert.assertNotNull(response.getStatusLine());
            Assert.assertEquals(expectedStatusCode, response.getStatusLine().getStatusCode());
            httpResponses.add(response);
            return response;
        } catch (ClientProtocolException e) {
            Assert.fail(e.getMessage());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        return null;
    }

    public void closeResponse(CloseableHttpResponse response) {
        if (response != null) {
            try {
                response.close();
            } catch (IOException e) {
                fail("Could not close http connection");
            }
        }
    }

    protected void dropUsers() {
        IdentityService identityService = processEngine.getIdentityService();
        identityService.deleteUser("kermit");
        identityService.deleteGroup("admin");
        identityService.deleteMembership("kermit", "admin");
    }

    protected void assertAndEnsureCleanDb() throws Throwable {
        log.debug("verifying that db is clean after test");
        Map<String, Long> tableCounts = managementService.getTableCount();
        StringBuilder outputMessage = new StringBuilder();
        for (String tableName : tableCounts.keySet()) {
            String tableNameWithoutPrefix = tableName.replace(processEngineConfiguration.getDatabaseTablePrefix(), "");
            if (!TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK.contains(tableNameWithoutPrefix)) {
                Long count = tableCounts.get(tableName);
                if (count != 0L) {
                    outputMessage.append("  " + tableName + ": " + count + " record(s) ");
                }
            }
        }
        if (outputMessage.length() > 0) {
            outputMessage.insert(0, "DB NOT CLEAN: \n");
            log.error(EMPTY_LINE);
            log.error(outputMessage.toString());
            log.info("dropping and recreating db");
            CommandExecutor commandExecutor = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration().getCommandExecutor();
            commandExecutor.execute(new Command<Object>() {

                public Object execute(CommandContext commandContext) {
                    DbSqlSession session = commandContext.getSession(DbSqlSession.class);
                    session.dbSchemaDrop();
                    session.dbSchemaCreate();
                    return null;
                }
            });
            if (exception != null) {
                throw exception;
            } else {
                Assert.fail(outputMessage.toString());
            }
        } else {
            log.info("database was clean");
        }
    }

    protected void closeHttpConnections() {
        for (CloseableHttpResponse response : httpResponses) {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    log.error("Could not close http connection", e);
                }
            }
        }
        httpResponses.clear();
    }

    protected String encode(String string) {
        if (string != null) {
            try {
                return URLEncoder.encode(string, "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                throw new IllegalStateException("JVM does not support UTF-8 encoding.", uee);
            }
        }
        return null;
    }

    public void assertProcessEnded(final String processInstanceId) {
        ProcessInstance processInstance = processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (processInstance != null) {
            throw new AssertionFailedError("Expected finished process instance '" + processInstanceId + "' but it was still in the db");
        }
    }

    public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
        JobExecutor jobExecutor = null;
        AsyncExecutor asyncExecutor = null;
        if (processEngineConfiguration.isAsyncExecutorEnabled() == false) {
            jobExecutor = processEngineConfiguration.getJobExecutor();
            jobExecutor.start();
        } else {
            asyncExecutor = processEngineConfiguration.getAsyncExecutor();
            asyncExecutor.start();
        }
        try {
            Timer timer = new Timer();
            InteruptTask task = new InteruptTask(Thread.currentThread());
            timer.schedule(task, maxMillisToWait);
            boolean areJobsAvailable = true;
            try {
                while (areJobsAvailable && !task.isTimeLimitExceeded()) {
                    Thread.sleep(intervalMillis);
                    areJobsAvailable = areJobsAvailable();
                }
            } catch (InterruptedException e) {
            } finally {
                timer.cancel();
            }
            if (areJobsAvailable) {
                throw new ActivitiException("time limit of " + maxMillisToWait + " was exceeded");
            }
        } finally {
            if (processEngineConfiguration.isAsyncExecutorEnabled() == false) {
                jobExecutor.shutdown();
            } else {
                asyncExecutor.shutdown();
            }
        }
    }

    public void waitForJobExecutorOnCondition(long maxMillisToWait, long intervalMillis, Callable<Boolean> condition) {
        JobExecutor jobExecutor = null;
        AsyncExecutor asyncExecutor = null;
        if (processEngineConfiguration.isAsyncExecutorEnabled() == false) {
            jobExecutor = processEngineConfiguration.getJobExecutor();
            jobExecutor.start();
        } else {
            asyncExecutor = processEngineConfiguration.getAsyncExecutor();
            asyncExecutor.start();
        }
        try {
            Timer timer = new Timer();
            InteruptTask task = new InteruptTask(Thread.currentThread());
            timer.schedule(task, maxMillisToWait);
            boolean conditionIsViolated = true;
            try {
                while (conditionIsViolated) {
                    Thread.sleep(intervalMillis);
                    conditionIsViolated = !condition.call();
                }
            } catch (InterruptedException e) {
            } catch (Exception e) {
                throw new ActivitiException("Exception while waiting on condition: " + e.getMessage(), e);
            } finally {
                timer.cancel();
            }
            if (conditionIsViolated) {
                throw new ActivitiException("time limit of " + maxMillisToWait + " was exceeded");
            }
        } finally {
            if (processEngineConfiguration.isAsyncExecutorEnabled() == false) {
                jobExecutor.shutdown();
            } else {
                asyncExecutor.shutdown();
            }
        }
    }

    public boolean areJobsAvailable() {
        return !managementService.createJobQuery().list().isEmpty();
    }

    private static class InteruptTask extends TimerTask {

        protected boolean timeLimitExceeded = false;

        protected Thread thread;

        public InteruptTask(Thread thread) {
            this.thread = thread;
        }

        public boolean isTimeLimitExceeded() {
            return timeLimitExceeded;
        }

        public void run() {
            timeLimitExceeded = true;
            thread.interrupt();
        }
    }

    protected void assertResultsPresentInDataResponse(String url, String... expectedResourceIds) throws JsonProcessingException, IOException {
        int numberOfResultsExpected = expectedResourceIds.length;
        CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + url), HttpStatus.SC_OK);
        JsonNode dataNode = objectMapper.readTree(response.getEntity().getContent()).get("data");
        closeResponse(response);
        assertEquals(numberOfResultsExpected, dataNode.size());
        List<String> toBeFound = new ArrayList<String>(Arrays.asList(expectedResourceIds));
        Iterator<JsonNode> it = dataNode.iterator();
        while (it.hasNext()) {
            String id = it.next().get("id").textValue();
            toBeFound.remove(id);
        }
        assertTrue("Not all process-definitions have been found in result, missing: " + StringUtils.join(toBeFound, ", "), toBeFound.isEmpty());
    }

    protected void assertEmptyResultsPresentInDataResponse(String url) throws JsonProcessingException, IOException {
        CloseableHttpResponse response = executeRequest(new HttpGet(SERVER_URL_PREFIX + url), HttpStatus.SC_OK);
        JsonNode dataNode = objectMapper.readTree(response.getEntity().getContent()).get("data");
        closeResponse(response);
        assertEquals(0, dataNode.size());
    }

    protected void assertResultsPresentInPostDataResponse(String url, ObjectNode body, String... expectedResourceIds) throws JsonProcessingException, IOException {
        assertResultsPresentInPostDataResponseWithStatusCheck(url, body, HttpStatus.SC_OK, expectedResourceIds);
    }

    protected void assertResultsPresentInPostDataResponseWithStatusCheck(String url, ObjectNode body, int expectedStatusCode, String... expectedResourceIds) throws JsonProcessingException, IOException {
        int numberOfResultsExpected = 0;
        if (expectedResourceIds != null) {
            numberOfResultsExpected = expectedResourceIds.length;
        }
        HttpPost post = new HttpPost(SERVER_URL_PREFIX + url);
        post.setEntity(new StringEntity(body.toString()));
        CloseableHttpResponse response = executeRequest(post, expectedStatusCode);
        if (expectedStatusCode == HttpStatus.SC_OK) {
            JsonNode rootNode = objectMapper.readTree(response.getEntity().getContent());
            JsonNode dataNode = rootNode.get("data");
            assertEquals(numberOfResultsExpected, dataNode.size());
            if (expectedResourceIds != null) {
                List<String> toBeFound = new ArrayList<String>(Arrays.asList(expectedResourceIds));
                Iterator<JsonNode> it = dataNode.iterator();
                while (it.hasNext()) {
                    String id = it.next().get("id").textValue();
                    toBeFound.remove(id);
                }
                assertTrue("Not all entries have been found in result, missing: " + StringUtils.join(toBeFound, ", "), toBeFound.isEmpty());
            }
        }
        closeResponse(response);
    }

    protected void assertErrorResult(String url, ObjectNode body, int statusCode) throws IOException {
        HttpPost post = new HttpPost(SERVER_URL_PREFIX + url);
        post.setEntity(new StringEntity(body.toString()));
        closeResponse(executeRequest(post, statusCode));
    }

    protected Date getDateFromISOString(String isoString) {
        DateTimeFormatter dateFormat = ISODateTimeFormat.dateTime();
        try {
            return dateFormat.parseDateTime(isoString).toDate();
        } catch (IllegalArgumentException iae) {
            fail("Illegal date provided: " + isoString);
            return null;
        }
    }

    protected String getISODateString(Date time) {
        return dateFormat.format(time);
    }

    protected String buildUrl(String[] fragments, Object... arguments) {
        return URL_BUILDER.buildUrl(fragments, arguments);
    }
}
