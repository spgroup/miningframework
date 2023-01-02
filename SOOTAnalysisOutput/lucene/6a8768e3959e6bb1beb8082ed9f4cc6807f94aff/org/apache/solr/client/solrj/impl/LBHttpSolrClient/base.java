package org.apache.solr.client.solrj.impl;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.IsUpdateRequest;
import org.apache.solr.client.solrj.request.RequestWriter;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SolrjNamedThreadFactory;
import org.slf4j.MDC;
import static org.apache.solr.common.params.CommonParams.ADMIN_PATHS;

public class LBHttpSolrClient extends SolrClient {

    private static Set<Integer> RETRY_CODES = new HashSet<>(4);

    static {
        RETRY_CODES.add(404);
        RETRY_CODES.add(403);
        RETRY_CODES.add(503);
        RETRY_CODES.add(500);
    }

    private final Map<String, ServerWrapper> aliveServers = new LinkedHashMap<>();

    protected final Map<String, ServerWrapper> zombieServers = new ConcurrentHashMap<>();

    private volatile ServerWrapper[] aliveServerList = new ServerWrapper[0];

    private ScheduledExecutorService aliveCheckExecutor;

    private final HttpClient httpClient;

    private final boolean clientIsInternal;

    private HttpSolrClient.Builder httpSolrClientBuilder;

    private final AtomicInteger counter = new AtomicInteger(-1);

    private static final SolrQuery solrQuery = new SolrQuery("*:*");

    private volatile ResponseParser parser;

    private volatile RequestWriter requestWriter;

    private Set<String> queryParams = new HashSet<>();

    private Integer connectionTimeout;

    private Integer soTimeout;

    static {
        solrQuery.setRows(0);
        solrQuery.setSort(SolrQuery.DOCID, SolrQuery.ORDER.asc);
        solrQuery.setDistrib(false);
    }

    protected static class ServerWrapper {

        final HttpSolrClient client;

        boolean standard = true;

        int failedPings = 0;

        public ServerWrapper(HttpSolrClient client) {
            this.client = client;
        }

        @Override
        public String toString() {
            return client.getBaseURL();
        }

        public String getKey() {
            return client.getBaseURL();
        }

        @Override
        public int hashCode() {
            return this.getKey().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof ServerWrapper))
                return false;
            return this.getKey().equals(((ServerWrapper) obj).getKey());
        }
    }

    public static class Req {

        protected SolrRequest request;

        protected List<String> servers;

        protected int numDeadServersToTry;

        public Req(SolrRequest request, List<String> servers) {
            this.request = request;
            this.servers = servers;
            this.numDeadServersToTry = servers.size();
        }

        public SolrRequest getRequest() {
            return request;
        }

        public List<String> getServers() {
            return servers;
        }

        public int getNumDeadServersToTry() {
            return numDeadServersToTry;
        }

        public void setNumDeadServersToTry(int numDeadServersToTry) {
            this.numDeadServersToTry = numDeadServersToTry;
        }
    }

    public static class Rsp {

        protected String server;

        protected NamedList<Object> rsp;

        public NamedList<Object> getResponse() {
            return rsp;
        }

        public String getServer() {
            return server;
        }
    }

    @Deprecated
    public LBHttpSolrClient(String... solrServerUrls) throws MalformedURLException {
        this(null, solrServerUrls);
    }

    @Deprecated
    public LBHttpSolrClient(HttpClient httpClient, String... solrServerUrl) {
        this(httpClient, new BinaryResponseParser(), solrServerUrl);
    }

    public LBHttpSolrClient(HttpSolrClient.Builder httpSolrClientBuilder, HttpClient httpClient, String... solrServerUrl) {
        clientIsInternal = httpClient == null;
        this.httpSolrClientBuilder = httpSolrClientBuilder;
        httpClient = constructClient(null);
        this.httpClient = httpClient;
        if (solrServerUrl != null) {
            for (String s : solrServerUrl) {
                ServerWrapper wrapper = new ServerWrapper(makeSolrClient(s));
                aliveServers.put(wrapper.getKey(), wrapper);
            }
        }
        updateAliveList();
    }

    @Deprecated
    public LBHttpSolrClient(HttpClient httpClient, ResponseParser parser, String... solrServerUrl) {
        clientIsInternal = (httpClient == null);
        this.httpClient = httpClient == null ? constructClient(solrServerUrl) : httpClient;
        this.parser = parser;
        for (String s : solrServerUrl) {
            ServerWrapper wrapper = new ServerWrapper(makeSolrClient(s));
            aliveServers.put(wrapper.getKey(), wrapper);
        }
        updateAliveList();
    }

    private HttpClient constructClient(String[] solrServerUrl) {
        ModifiableSolrParams params = new ModifiableSolrParams();
        if (solrServerUrl != null && solrServerUrl.length > 1) {
            params.set(HttpClientUtil.PROP_USE_RETRY, false);
        } else {
            params.set(HttpClientUtil.PROP_USE_RETRY, true);
        }
        return HttpClientUtil.createClient(params);
    }

    public Set<String> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Set<String> queryParams) {
        this.queryParams = queryParams;
    }

    public void addQueryParams(String queryOnlyParam) {
        this.queryParams.add(queryOnlyParam);
    }

    public static String normalize(String server) {
        if (server.endsWith("/"))
            server = server.substring(0, server.length() - 1);
        return server;
    }

    protected HttpSolrClient makeSolrClient(String server) {
        HttpSolrClient client;
        if (httpSolrClientBuilder != null) {
            synchronized (this) {
                client = httpSolrClientBuilder.withBaseSolrUrl(server).withHttpClient(httpClient).build();
            }
        } else {
            client = new HttpSolrClient.Builder(server).withHttpClient(httpClient).withResponseParser(parser).build();
        }
        if (requestWriter != null) {
            client.setRequestWriter(requestWriter);
        }
        if (queryParams != null) {
            client.setQueryParams(queryParams);
        }
        return client;
    }

    public Rsp request(Req req) throws SolrServerException, IOException {
        Rsp rsp = new Rsp();
        Exception ex = null;
        boolean isNonRetryable = req.request instanceof IsUpdateRequest || ADMIN_PATHS.contains(req.request.getPath());
        List<ServerWrapper> skipped = null;
        long timeAllowedNano = getTimeAllowedInNanos(req.getRequest());
        long timeOutTime = System.nanoTime() + timeAllowedNano;
        for (String serverStr : req.getServers()) {
            if (isTimeExceeded(timeAllowedNano, timeOutTime)) {
                break;
            }
            serverStr = normalize(serverStr);
            ServerWrapper wrapper = zombieServers.get(serverStr);
            if (wrapper != null) {
                final int numDeadServersToTry = req.getNumDeadServersToTry();
                if (numDeadServersToTry > 0) {
                    if (skipped == null) {
                        skipped = new ArrayList<>(numDeadServersToTry);
                        skipped.add(wrapper);
                    } else if (skipped.size() < numDeadServersToTry) {
                        skipped.add(wrapper);
                    }
                }
                continue;
            }
            try {
                MDC.put("LBHttpSolrClient.url", serverStr);
                HttpSolrClient client = makeSolrClient(serverStr);
                ex = doRequest(client, req, rsp, isNonRetryable, false, null);
                if (ex == null) {
                    return rsp;
                }
            } finally {
                MDC.remove("LBHttpSolrClient.url");
            }
        }
        if (skipped != null) {
            for (ServerWrapper wrapper : skipped) {
                if (isTimeExceeded(timeAllowedNano, timeOutTime)) {
                    break;
                }
                ex = doRequest(wrapper.client, req, rsp, isNonRetryable, true, wrapper.getKey());
                if (ex == null) {
                    return rsp;
                }
            }
        }
        if (ex == null) {
            throw new SolrServerException("No live SolrServers available to handle this request");
        } else {
            throw new SolrServerException("No live SolrServers available to handle this request:" + zombieServers.keySet(), ex);
        }
    }

    protected Exception addZombie(HttpSolrClient server, Exception e) {
        ServerWrapper wrapper;
        wrapper = new ServerWrapper(server);
        wrapper.standard = false;
        zombieServers.put(wrapper.getKey(), wrapper);
        startAliveCheckExecutor();
        return e;
    }

    protected Exception doRequest(HttpSolrClient client, Req req, Rsp rsp, boolean isNonRetryable, boolean isZombie, String zombieKey) throws SolrServerException, IOException {
        Exception ex = null;
        try {
            rsp.server = client.getBaseURL();
            rsp.rsp = client.request(req.getRequest(), (String) null);
            if (isZombie) {
                zombieServers.remove(zombieKey);
            }
        } catch (SolrException e) {
            if (!isNonRetryable && RETRY_CODES.contains(e.code())) {
                ex = (!isZombie) ? addZombie(client, e) : e;
            } else {
                if (isZombie) {
                    zombieServers.remove(zombieKey);
                }
                throw e;
            }
        } catch (SocketException e) {
            if (!isNonRetryable || e instanceof ConnectException) {
                ex = (!isZombie) ? addZombie(client, e) : e;
            } else {
                throw e;
            }
        } catch (SocketTimeoutException e) {
            if (!isNonRetryable) {
                ex = (!isZombie) ? addZombie(client, e) : e;
            } else {
                throw e;
            }
        } catch (SolrServerException e) {
            Throwable rootCause = e.getRootCause();
            if (!isNonRetryable && rootCause instanceof IOException) {
                ex = (!isZombie) ? addZombie(client, e) : e;
            } else if (isNonRetryable && rootCause instanceof ConnectException) {
                ex = (!isZombie) ? addZombie(client, e) : e;
            } else {
                throw e;
            }
        } catch (Exception e) {
            throw new SolrServerException(e);
        }
        return ex;
    }

    private void updateAliveList() {
        synchronized (aliveServers) {
            aliveServerList = aliveServers.values().toArray(new ServerWrapper[aliveServers.size()]);
        }
    }

    private ServerWrapper removeFromAlive(String key) {
        synchronized (aliveServers) {
            ServerWrapper wrapper = aliveServers.remove(key);
            if (wrapper != null)
                updateAliveList();
            return wrapper;
        }
    }

    private void addToAlive(ServerWrapper wrapper) {
        synchronized (aliveServers) {
            ServerWrapper prev = aliveServers.put(wrapper.getKey(), wrapper);
            updateAliveList();
        }
    }

    public void addSolrServer(String server) throws MalformedURLException {
        HttpSolrClient client = makeSolrClient(server);
        addToAlive(new ServerWrapper(client));
    }

    public String removeSolrServer(String server) {
        try {
            server = new URL(server).toExternalForm();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        if (server.endsWith("/")) {
            server = server.substring(0, server.length() - 1);
        }
        removeFromAlive(server);
        zombieServers.remove(server);
        return null;
    }

    public void setConnectionTimeout(int timeout) {
        this.connectionTimeout = timeout;
        synchronized (aliveServers) {
            Iterator<ServerWrapper> wrappersIt = aliveServers.values().iterator();
            while (wrappersIt.hasNext()) {
                wrappersIt.next().client.setConnectionTimeout(timeout);
            }
        }
        Iterator<ServerWrapper> wrappersIt = zombieServers.values().iterator();
        while (wrappersIt.hasNext()) {
            wrappersIt.next().client.setConnectionTimeout(timeout);
        }
    }

    public void setSoTimeout(int timeout) {
        this.soTimeout = timeout;
        synchronized (aliveServers) {
            Iterator<ServerWrapper> wrappersIt = aliveServers.values().iterator();
            while (wrappersIt.hasNext()) {
                wrappersIt.next().client.setSoTimeout(timeout);
            }
        }
        Iterator<ServerWrapper> wrappersIt = zombieServers.values().iterator();
        while (wrappersIt.hasNext()) {
            wrappersIt.next().client.setSoTimeout(timeout);
        }
    }

    @Override
    public void close() {
        if (aliveCheckExecutor != null) {
            aliveCheckExecutor.shutdownNow();
        }
        if (clientIsInternal) {
            HttpClientUtil.close(httpClient);
        }
    }

    @Override
    public NamedList<Object> request(final SolrRequest request, String collection) throws SolrServerException, IOException {
        Exception ex = null;
        ServerWrapper[] serverList = aliveServerList;
        int maxTries = serverList.length;
        Map<String, ServerWrapper> justFailed = null;
        long timeAllowedNano = getTimeAllowedInNanos(request);
        long timeOutTime = System.nanoTime() + timeAllowedNano;
        for (int attempts = 0; attempts < maxTries; attempts++) {
            if (isTimeExceeded(timeAllowedNano, timeOutTime)) {
                break;
            }
            int count = counter.incrementAndGet() & Integer.MAX_VALUE;
            ServerWrapper wrapper = serverList[count % serverList.length];
            try {
                return wrapper.client.request(request, collection);
            } catch (SolrException e) {
                throw e;
            } catch (SolrServerException e) {
                if (e.getRootCause() instanceof IOException) {
                    ex = e;
                    moveAliveToDead(wrapper);
                    if (justFailed == null)
                        justFailed = new HashMap<>();
                    justFailed.put(wrapper.getKey(), wrapper);
                } else {
                    throw e;
                }
            } catch (Exception e) {
                throw new SolrServerException(e);
            }
        }
        for (ServerWrapper wrapper : zombieServers.values()) {
            if (isTimeExceeded(timeAllowedNano, timeOutTime)) {
                break;
            }
            if (wrapper.standard == false || justFailed != null && justFailed.containsKey(wrapper.getKey()))
                continue;
            try {
                NamedList<Object> rsp = wrapper.client.request(request, collection);
                zombieServers.remove(wrapper.getKey());
                addToAlive(wrapper);
                return rsp;
            } catch (SolrException e) {
                throw e;
            } catch (SolrServerException e) {
                if (e.getRootCause() instanceof IOException) {
                    ex = e;
                } else {
                    throw e;
                }
            } catch (Exception e) {
                throw new SolrServerException(e);
            }
        }
        if (ex == null) {
            throw new SolrServerException("No live SolrServers available to handle this request");
        } else {
            throw new SolrServerException("No live SolrServers available to handle this request", ex);
        }
    }

    private long getTimeAllowedInNanos(final SolrRequest req) {
        SolrParams reqParams = req.getParams();
        return reqParams == null ? -1 : TimeUnit.NANOSECONDS.convert(reqParams.getInt(CommonParams.TIME_ALLOWED, -1), TimeUnit.MILLISECONDS);
    }

    private boolean isTimeExceeded(long timeAllowedNano, long timeOutTime) {
        return timeAllowedNano > 0 && System.nanoTime() > timeOutTime;
    }

    private void checkAZombieServer(ServerWrapper zombieServer) {
        try {
            QueryResponse resp = zombieServer.client.query(solrQuery);
            if (resp.getStatus() == 0) {
                ServerWrapper wrapper = zombieServers.remove(zombieServer.getKey());
                if (wrapper != null) {
                    wrapper.failedPings = 0;
                    if (wrapper.standard) {
                        addToAlive(wrapper);
                    }
                } else {
                }
            }
        } catch (Exception e) {
            zombieServer.failedPings++;
            if (!zombieServer.standard && zombieServer.failedPings >= NONSTANDARD_PING_LIMIT) {
                zombieServers.remove(zombieServer.getKey());
            }
        }
    }

    private void moveAliveToDead(ServerWrapper wrapper) {
        wrapper = removeFromAlive(wrapper.getKey());
        if (wrapper == null)
            return;
        zombieServers.put(wrapper.getKey(), wrapper);
        startAliveCheckExecutor();
    }

    private int interval = CHECK_INTERVAL;

    public void setAliveCheckInterval(int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Alive check interval must be " + "positive, specified value = " + interval);
        }
        this.interval = interval;
    }

    private void startAliveCheckExecutor() {
        if (aliveCheckExecutor == null) {
            synchronized (this) {
                if (aliveCheckExecutor == null) {
                    aliveCheckExecutor = Executors.newSingleThreadScheduledExecutor(new SolrjNamedThreadFactory("aliveCheckExecutor"));
                    aliveCheckExecutor.scheduleAtFixedRate(getAliveCheckRunner(new WeakReference<>(this)), this.interval, this.interval, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    private static Runnable getAliveCheckRunner(final WeakReference<LBHttpSolrClient> lbRef) {
        return () -> {
            LBHttpSolrClient lb = lbRef.get();
            if (lb != null && lb.zombieServers != null) {
                for (ServerWrapper zombieServer : lb.zombieServers.values()) {
                    lb.checkAZombieServer(zombieServer);
                }
            }
        };
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public ResponseParser getParser() {
        return parser;
    }

    public void setParser(ResponseParser parser) {
        this.parser = parser;
    }

    public void setRequestWriter(RequestWriter requestWriter) {
        this.requestWriter = requestWriter;
    }

    public RequestWriter getRequestWriter() {
        return requestWriter;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (this.aliveCheckExecutor != null)
                this.aliveCheckExecutor.shutdownNow();
        } finally {
            super.finalize();
        }
    }

    private static final int CHECK_INTERVAL = 60 * 1000;

    private static final int NONSTANDARD_PING_LIMIT = 5;

    public static class Builder {

        private final List<String> baseSolrUrls;

        private HttpClient httpClient;

        private ResponseParser responseParser;

        private HttpSolrClient.Builder httpSolrClientBuilder;

        public Builder() {
            this.baseSolrUrls = new ArrayList<>();
            this.responseParser = new BinaryResponseParser();
        }

        public HttpSolrClient.Builder getHttpSolrClientBuilder() {
            return httpSolrClientBuilder;
        }

        public Builder withBaseSolrUrl(String baseSolrUrl) {
            this.baseSolrUrls.add(baseSolrUrl);
            return this;
        }

        public Builder withBaseSolrUrls(String... solrUrls) {
            for (String baseSolrUrl : solrUrls) {
                this.baseSolrUrls.add(baseSolrUrl);
            }
            return this;
        }

        public Builder withHttpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder withResponseParser(ResponseParser responseParser) {
            this.responseParser = responseParser;
            return this;
        }

        public Builder withHttpSolrClientBuilder(HttpSolrClient.Builder builder) {
            this.httpSolrClientBuilder = builder;
            return this;
        }

        public LBHttpSolrClient build() {
            final String[] baseUrlArray = new String[baseSolrUrls.size()];
            String[] solrServerUrls = baseSolrUrls.toArray(baseUrlArray);
            return httpSolrClientBuilder != null ? new LBHttpSolrClient(httpSolrClientBuilder, httpClient, solrServerUrls) : new LBHttpSolrClient(httpClient, responseParser, solrServerUrls);
        }
    }
}
