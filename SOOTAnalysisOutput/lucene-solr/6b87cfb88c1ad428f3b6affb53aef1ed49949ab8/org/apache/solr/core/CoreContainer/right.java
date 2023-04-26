package org.apache.solr.core;

import java.io.Closeable;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.config.Lookup;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.solr.api.CustomContainerPlugins;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.cloud.SolrCloudManager;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.SolrHttpClientBuilder;
import org.apache.solr.client.solrj.impl.SolrHttpClientContextBuilder;
import org.apache.solr.client.solrj.impl.SolrHttpClientContextBuilder.AuthSchemeRegistryProvider;
import org.apache.solr.client.solrj.impl.SolrHttpClientContextBuilder.CredentialsProviderProvider;
import org.apache.solr.client.solrj.io.SolrClientCache;
import org.apache.solr.client.solrj.util.SolrIdentifierValidator;
import org.apache.solr.cloud.CloudDescriptor;
import org.apache.solr.cloud.OverseerTaskQueue;
import org.apache.solr.cloud.ZkController;
import org.apache.solr.common.AlreadyClosedException;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Replica.State;
import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.solr.common.cloud.ZkStateReader;
import org.apache.solr.common.util.ExecutorUtil;
import org.apache.solr.common.util.IOUtils;
import org.apache.solr.common.util.ObjectCache;
import org.apache.solr.common.util.SolrNamedThreadFactory;
import org.apache.solr.common.util.Utils;
import org.apache.solr.core.DirectoryFactory.DirContext;
import org.apache.solr.core.backup.repository.BackupRepository;
import org.apache.solr.core.backup.repository.BackupRepositoryFactory;
import org.apache.solr.filestore.PackageStoreAPI;
import org.apache.solr.handler.ClusterAPI;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.handler.SnapShooter;
import org.apache.solr.handler.admin.CollectionsHandler;
import org.apache.solr.handler.admin.ConfigSetsHandler;
import org.apache.solr.handler.admin.ContainerPluginsApi;
import org.apache.solr.handler.admin.CoreAdminHandler;
import org.apache.solr.handler.admin.HealthCheckHandler;
import org.apache.solr.handler.admin.InfoHandler;
import org.apache.solr.handler.admin.MetricsCollectorHandler;
import org.apache.solr.handler.admin.MetricsHandler;
import org.apache.solr.handler.admin.MetricsHistoryHandler;
import org.apache.solr.handler.admin.SecurityConfHandler;
import org.apache.solr.handler.admin.SecurityConfHandlerLocal;
import org.apache.solr.handler.admin.SecurityConfHandlerZk;
import org.apache.solr.handler.admin.ZookeeperInfoHandler;
import org.apache.solr.handler.admin.ZookeeperReadAPI;
import org.apache.solr.handler.admin.ZookeeperStatusHandler;
import org.apache.solr.handler.component.ShardHandlerFactory;
import org.apache.solr.handler.sql.CalciteSolrDriver;
import org.apache.solr.logging.LogWatcher;
import org.apache.solr.logging.MDCLoggingContext;
import org.apache.solr.metrics.SolrCoreMetricManager;
import org.apache.solr.metrics.SolrMetricManager;
import org.apache.solr.metrics.SolrMetricProducer;
import org.apache.solr.metrics.SolrMetricsContext;
import org.apache.solr.pkg.PackageLoader;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.request.SolrRequestInfo;
import org.apache.solr.search.SolrFieldCacheBean;
import org.apache.solr.security.AuditLoggerPlugin;
import org.apache.solr.security.AuthenticationPlugin;
import org.apache.solr.security.AuthorizationPlugin;
import org.apache.solr.security.HttpClientBuilderPlugin;
import org.apache.solr.security.PKIAuthenticationPlugin;
import org.apache.solr.security.PublicKeyHandler;
import org.apache.solr.security.SecurityPluginHolder;
import org.apache.solr.update.SolrCoreState;
import org.apache.solr.update.UpdateShardHandler;
import org.apache.solr.util.OrderedExecutor;
import org.apache.solr.util.RefCounted;
import org.apache.solr.util.stats.MetricUtils;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.Objects.requireNonNull;
import static org.apache.solr.common.params.CommonParams.AUTHC_PATH;
import static org.apache.solr.common.params.CommonParams.AUTHZ_PATH;
import static org.apache.solr.common.params.CommonParams.COLLECTIONS_HANDLER_PATH;
import static org.apache.solr.common.params.CommonParams.CONFIGSETS_HANDLER_PATH;
import static org.apache.solr.common.params.CommonParams.CORES_HANDLER_PATH;
import static org.apache.solr.common.params.CommonParams.INFO_HANDLER_PATH;
import static org.apache.solr.common.params.CommonParams.METRICS_HISTORY_PATH;
import static org.apache.solr.common.params.CommonParams.METRICS_PATH;
import static org.apache.solr.common.params.CommonParams.ZK_PATH;
import static org.apache.solr.common.params.CommonParams.ZK_STATUS_PATH;
import static org.apache.solr.core.CorePropertiesLocator.PROPERTIES_FILENAME;
import static org.apache.solr.security.AuthenticationPlugin.AUTHENTICATION_PLUGIN_PROP;

public class CoreContainer {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    final SolrCores solrCores = new SolrCores(this);

    public static class CoreLoadFailure {

        public final CoreDescriptor cd;

        public final Exception exception;

        public CoreLoadFailure(CoreDescriptor cd, Exception loadFailure) {
            this.cd = new CoreDescriptor(cd.getName(), cd);
            this.exception = loadFailure;
        }
    }

    private volatile PluginBag<SolrRequestHandler> containerHandlers = new PluginBag<>(SolrRequestHandler.class, null);

    public final Supplier<SolrZkClient> zkClientSupplier = () -> getZkController().getZkClient();

    private final CustomContainerPlugins customContainerPlugins = new CustomContainerPlugins(this, containerHandlers.getApiBag());

    protected final Map<String, CoreLoadFailure> coreInitFailures = new ConcurrentHashMap<>();

    protected volatile CoreAdminHandler coreAdminHandler = null;

    protected volatile CollectionsHandler collectionsHandler = null;

    protected volatile HealthCheckHandler healthCheckHandler = null;

    private volatile InfoHandler infoHandler;

    protected volatile ConfigSetsHandler configSetsHandler = null;

    private volatile PKIAuthenticationPlugin pkiAuthenticationPlugin;

    protected volatile Properties containerProperties;

    private volatile ConfigSetService coreConfigService;

    protected final ZkContainer zkSys = new ZkContainer();

    protected volatile ShardHandlerFactory shardHandlerFactory;

    private volatile UpdateShardHandler updateShardHandler;

    private volatile ExecutorService coreContainerWorkExecutor = ExecutorUtil.newMDCAwareCachedThreadPool(new SolrNamedThreadFactory("coreContainerWorkExecutor"));

    private final OrderedExecutor replayUpdatesExecutor;

    @SuppressWarnings({ "rawtypes" })
    protected volatile LogWatcher logging = null;

    private volatile CloserThread backgroundCloser = null;

    protected final NodeConfig cfg;

    protected final SolrResourceLoader loader;

    protected final Path solrHome;

    protected final CoresLocator coresLocator;

    private volatile String hostName;

    private final BlobRepository blobRepository = new BlobRepository(this);

    private volatile boolean asyncSolrCoreLoad;

    protected volatile SecurityConfHandler securityConfHandler;

    private volatile SecurityPluginHolder<AuthorizationPlugin> authorizationPlugin;

    private volatile SecurityPluginHolder<AuthenticationPlugin> authenticationPlugin;

    private volatile SecurityPluginHolder<AuditLoggerPlugin> auditloggerPlugin;

    private volatile BackupRepositoryFactory backupRepoFactory;

    protected volatile SolrMetricManager metricManager;

    protected volatile String metricTag = SolrMetricProducer.getUniqueMetricTag(this, null);

    protected volatile SolrMetricsContext solrMetricsContext;

    protected MetricsHandler metricsHandler;

    protected volatile MetricsHistoryHandler metricsHistoryHandler;

    protected volatile MetricsCollectorHandler metricsCollectorHandler;

    private volatile SolrClientCache solrClientCache;

    private final ObjectCache objectCache = new ObjectCache();

    private PackageStoreAPI packageStoreAPI;

    private PackageLoader packageLoader;

    private Set<Path> allowPaths;

    public final static long LOAD_COMPLETE = 0x1L;

    public final static long CORE_DISCOVERY_COMPLETE = 0x2L;

    public final static long INITIAL_CORE_LOAD_COMPLETE = 0x4L;

    private volatile long status = 0L;

    private ExecutorService coreContainerAsyncTaskExecutor = ExecutorUtil.newMDCAwareCachedThreadPool("Core Container Async Task");

    private enum CoreInitFailedAction {

        fromleader, none
    }

    public BackupRepository newBackupRepository(String repositoryName) {
        BackupRepository repository;
        if (repositoryName != null) {
            repository = backupRepoFactory.newInstance(getResourceLoader(), repositoryName);
        } else {
            repository = backupRepoFactory.newInstance(getResourceLoader());
        }
        return repository;
    }

    public ExecutorService getCoreZkRegisterExecutorService() {
        return zkSys.getCoreZkRegisterExecutorService();
    }

    public SolrRequestHandler getRequestHandler(String path) {
        return RequestHandlerBase.getRequestHandler(path, containerHandlers);
    }

    public PluginBag<SolrRequestHandler> getRequestHandlers() {
        return this.containerHandlers;
    }

    {
        if (log.isDebugEnabled()) {
            log.debug("New CoreContainer {}", System.identityHashCode(this));
        }
    }

    public CoreContainer(Path solrHome, Properties properties) {
        this(SolrXmlConfig.fromSolrHome(solrHome, properties));
    }

    public CoreContainer(NodeConfig config) {
        this(config, new CorePropertiesLocator(config.getCoreRootDirectory()));
    }

    public CoreContainer(NodeConfig config, boolean asyncSolrCoreLoad) {
        this(config, new CorePropertiesLocator(config.getCoreRootDirectory()), asyncSolrCoreLoad);
    }

    public CoreContainer(NodeConfig config, CoresLocator locator) {
        this(config, locator, false);
    }

    public CoreContainer(NodeConfig config, CoresLocator locator, boolean asyncSolrCoreLoad) {
        this.loader = config.getSolrResourceLoader();
        this.solrHome = config.getSolrHome();
        this.cfg = requireNonNull(config);
        try {
            containerHandlers.put(PublicKeyHandler.PATH, new PublicKeyHandler(cfg.getCloudConfig()));
        } catch (IOException | InvalidKeySpecException e) {
            throw new RuntimeException("Bad PublicKeyHandler configuration.", e);
        }
        if (null != this.cfg.getBooleanQueryMaxClauseCount()) {
            IndexSearcher.setMaxClauseCount(this.cfg.getBooleanQueryMaxClauseCount());
        }
        this.coresLocator = locator;
        this.containerProperties = new Properties(config.getSolrProperties());
        this.asyncSolrCoreLoad = asyncSolrCoreLoad;
        this.replayUpdatesExecutor = new OrderedExecutor(cfg.getReplayUpdatesThreads(), ExecutorUtil.newMDCAwareCachedThreadPool(cfg.getReplayUpdatesThreads(), new SolrNamedThreadFactory("replayUpdatesExecutor")));
        this.allowPaths = new java.util.HashSet<>();
        this.allowPaths.add(cfg.getSolrHome());
        this.allowPaths.add(cfg.getCoreRootDirectory());
        if (cfg.getSolrDataHome() != null) {
            this.allowPaths.add(cfg.getSolrDataHome());
        }
        if (!cfg.getAllowPaths().isEmpty()) {
            this.allowPaths.addAll(cfg.getAllowPaths());
            if (log.isInfoEnabled()) {
                log.info("Allowing use of paths: {}", cfg.getAllowPaths());
            }
        }
        Path userFilesPath = getUserFilesPath();
        try {
            Files.createDirectories(userFilesPath);
        } catch (Exception e) {
            log.warn("Unable to create [{}].  Features requiring this directory may fail.", userFilesPath, e);
        }
    }

    @SuppressWarnings({ "unchecked" })
    private synchronized void initializeAuthorizationPlugin(Map<String, Object> authorizationConf) {
        authorizationConf = Utils.getDeepCopy(authorizationConf, 4);
        int newVersion = readVersion(authorizationConf);
        SecurityPluginHolder<AuthorizationPlugin> old = authorizationPlugin;
        SecurityPluginHolder<AuthorizationPlugin> authorizationPlugin = null;
        if (authorizationConf != null) {
            String klas = (String) authorizationConf.get("class");
            if (klas == null) {
                throw new SolrException(ErrorCode.SERVER_ERROR, "class is required for authorization plugin");
            }
            if (old != null && old.getZnodeVersion() == newVersion && newVersion > 0) {
                log.debug("Authorization config not modified");
                return;
            }
            log.info("Initializing authorization plugin: {}", klas);
            authorizationPlugin = new SecurityPluginHolder<>(newVersion, getResourceLoader().newInstance(klas, AuthorizationPlugin.class));
            authorizationPlugin.plugin.init(authorizationConf);
        } else {
            log.debug("Security conf doesn't exist. Skipping setup for authorization module.");
        }
        this.authorizationPlugin = authorizationPlugin;
        if (old != null) {
            try {
                old.plugin.close();
            } catch (Exception e) {
                log.error("Exception while attempting to close old authorization plugin", e);
            }
        }
    }

    @SuppressWarnings({ "unchecked" })
    private void initializeAuditloggerPlugin(Map<String, Object> auditConf) {
        auditConf = Utils.getDeepCopy(auditConf, 4);
        int newVersion = readVersion(auditConf);
        SecurityPluginHolder<AuditLoggerPlugin> old = auditloggerPlugin;
        SecurityPluginHolder<AuditLoggerPlugin> newAuditloggerPlugin = null;
        if (auditConf != null) {
            String klas = (String) auditConf.get("class");
            if (klas == null) {
                throw new SolrException(ErrorCode.SERVER_ERROR, "class is required for auditlogger plugin");
            }
            if (old != null && old.getZnodeVersion() == newVersion && newVersion > 0) {
                log.debug("Auditlogger config not modified");
                return;
            }
            log.info("Initializing auditlogger plugin: {}", klas);
            newAuditloggerPlugin = new SecurityPluginHolder<>(newVersion, getResourceLoader().newInstance(klas, AuditLoggerPlugin.class));
            newAuditloggerPlugin.plugin.init(auditConf);
            newAuditloggerPlugin.plugin.initializeMetrics(solrMetricsContext, "/auditlogging");
        } else {
            log.debug("Security conf doesn't exist. Skipping setup for audit logging module.");
        }
        this.auditloggerPlugin = newAuditloggerPlugin;
        if (old != null) {
            try {
                old.plugin.close();
            } catch (Exception e) {
                log.error("Exception while attempting to close old auditlogger plugin", e);
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private synchronized void initializeAuthenticationPlugin(Map<String, Object> authenticationConfig) {
        authenticationConfig = Utils.getDeepCopy(authenticationConfig, 4);
        int newVersion = readVersion(authenticationConfig);
        String pluginClassName = null;
        if (authenticationConfig != null) {
            if (authenticationConfig.containsKey("class")) {
                pluginClassName = String.valueOf(authenticationConfig.get("class"));
            } else {
                throw new SolrException(ErrorCode.SERVER_ERROR, "No 'class' specified for authentication in ZK.");
            }
        }
        if (pluginClassName != null) {
            log.debug("Authentication plugin class obtained from security.json: {}", pluginClassName);
        } else if (System.getProperty(AUTHENTICATION_PLUGIN_PROP) != null) {
            pluginClassName = System.getProperty(AUTHENTICATION_PLUGIN_PROP);
            log.debug("Authentication plugin class obtained from system property '{}': {}", AUTHENTICATION_PLUGIN_PROP, pluginClassName);
        } else {
            log.debug("No authentication plugin used.");
        }
        SecurityPluginHolder<AuthenticationPlugin> old = authenticationPlugin;
        SecurityPluginHolder<AuthenticationPlugin> authenticationPlugin = null;
        if (old != null && old.getZnodeVersion() == newVersion && newVersion > 0) {
            log.debug("Authentication config not modified");
            return;
        }
        if (pluginClassName != null) {
            log.info("Initializing authentication plugin: {}", pluginClassName);
            authenticationPlugin = new SecurityPluginHolder<>(newVersion, getResourceLoader().newInstance(pluginClassName, AuthenticationPlugin.class, null, new Class[] { CoreContainer.class }, new Object[] { this }));
        }
        if (authenticationPlugin != null) {
            authenticationPlugin.plugin.init(authenticationConfig);
            setupHttpClientForAuthPlugin(authenticationPlugin.plugin);
            authenticationPlugin.plugin.initializeMetrics(solrMetricsContext, "/authentication");
        }
        this.authenticationPlugin = authenticationPlugin;
        try {
            if (old != null)
                old.plugin.close();
        } catch (Exception e) {
            log.error("Exception while attempting to close old authentication plugin", e);
        }
    }

    private void setupHttpClientForAuthPlugin(Object authcPlugin) {
        if (authcPlugin instanceof HttpClientBuilderPlugin) {
            HttpClientBuilderPlugin builderPlugin = ((HttpClientBuilderPlugin) authcPlugin);
            SolrHttpClientBuilder builder = builderPlugin.getHttpClientBuilder(HttpClientUtil.getHttpClientBuilder());
            shardHandlerFactory.setSecurityBuilder(builderPlugin);
            updateShardHandler.setSecurityBuilder(builderPlugin);
            log.debug("Reconfiguring HttpClient settings.");
            SolrHttpClientContextBuilder httpClientBuilder = new SolrHttpClientContextBuilder();
            if (builder.getCredentialsProviderProvider() != null) {
                httpClientBuilder.setDefaultCredentialsProvider(new CredentialsProviderProvider() {

                    @Override
                    public CredentialsProvider getCredentialsProvider() {
                        return builder.getCredentialsProviderProvider().getCredentialsProvider();
                    }
                });
            }
            if (builder.getAuthSchemeRegistryProvider() != null) {
                httpClientBuilder.setAuthSchemeRegistryProvider(new AuthSchemeRegistryProvider() {

                    @Override
                    public Lookup<AuthSchemeProvider> getAuthSchemeRegistry() {
                        return builder.getAuthSchemeRegistryProvider().getAuthSchemeRegistry();
                    }
                });
            }
            HttpClientUtil.setHttpClientRequestContextBuilder(httpClientBuilder);
        }
        if (pkiAuthenticationPlugin != null && !pkiAuthenticationPlugin.isInterceptorRegistered()) {
            pkiAuthenticationPlugin.getHttpClientBuilder(HttpClientUtil.getHttpClientBuilder());
            shardHandlerFactory.setSecurityBuilder(pkiAuthenticationPlugin);
            updateShardHandler.setSecurityBuilder(pkiAuthenticationPlugin);
        }
    }

    @SuppressWarnings({ "rawtypes" })
    private static int readVersion(Map<String, Object> conf) {
        if (conf == null)
            return -1;
        Map meta = (Map) conf.get("");
        if (meta == null)
            return -1;
        Number v = (Number) meta.get("v");
        return v == null ? -1 : v.intValue();
    }

    protected CoreContainer(Object testConstructor) {
        solrHome = null;
        loader = null;
        coresLocator = null;
        cfg = null;
        containerProperties = null;
        replayUpdatesExecutor = null;
    }

    public static CoreContainer createAndLoad(Path solrHome) {
        return createAndLoad(solrHome, solrHome.resolve(SolrXmlConfig.SOLR_XML_FILE));
    }

    public static CoreContainer createAndLoad(Path solrHome, Path configFile) {
        CoreContainer cc = new CoreContainer(SolrXmlConfig.fromFile(solrHome, configFile, new Properties()));
        try {
            cc.load();
        } catch (Exception e) {
            cc.shutdown();
            throw e;
        }
        return cc;
    }

    public Properties getContainerProperties() {
        return containerProperties;
    }

    public PKIAuthenticationPlugin getPkiAuthenticationPlugin() {
        return pkiAuthenticationPlugin;
    }

    public SolrMetricManager getMetricManager() {
        return metricManager;
    }

    public MetricsHandler getMetricsHandler() {
        return metricsHandler;
    }

    public MetricsHistoryHandler getMetricsHistoryHandler() {
        return metricsHistoryHandler;
    }

    public OrderedExecutor getReplayUpdatesExecutor() {
        return replayUpdatesExecutor;
    }

    public PackageLoader getPackageLoader() {
        return packageLoader;
    }

    public PackageStoreAPI getPackageStoreAPI() {
        return packageStoreAPI;
    }

    public SolrClientCache getSolrClientCache() {
        return solrClientCache;
    }

    public ObjectCache getObjectCache() {
        return objectCache;
    }

    public void load() {
        if (log.isDebugEnabled()) {
            log.debug("Loading cores into CoreContainer [instanceDir={}]", getSolrHome());
        }
        Set<String> libDirs = new LinkedHashSet<>();
        libDirs.add("lib");
        if (!StringUtils.isBlank(cfg.getSharedLibDirectory())) {
            List<String> sharedLibs = Arrays.asList(cfg.getSharedLibDirectory().split("\\s*,\\s*"));
            libDirs.addAll(sharedLibs);
        }
        boolean modified = false;
        for (String libDir : libDirs) {
            Path libPath = Paths.get(getSolrHome()).resolve(libDir);
            if (Files.exists(libPath)) {
                try {
                    loader.addToClassLoader(SolrResourceLoader.getURLs(libPath));
                    modified = true;
                } catch (IOException e) {
                    throw new SolrException(ErrorCode.SERVER_ERROR, "Couldn't load libs: " + e, e);
                }
            }
        }
        if (modified) {
            loader.reloadLuceneSPI();
        }
        packageStoreAPI = new PackageStoreAPI(this);
        containerHandlers.getApiBag().registerObject(packageStoreAPI.readAPI);
        containerHandlers.getApiBag().registerObject(packageStoreAPI.writeAPI);
        metricManager = new SolrMetricManager(loader, cfg.getMetricsConfig());
        String registryName = SolrMetricManager.getRegistryName(SolrInfoBean.Group.node);
        solrMetricsContext = new SolrMetricsContext(metricManager, registryName, metricTag);
        coreContainerWorkExecutor = MetricUtils.instrumentedExecutorService(coreContainerWorkExecutor, null, metricManager.registry(SolrMetricManager.getRegistryName(SolrInfoBean.Group.node)), SolrMetricManager.mkName("coreContainerWorkExecutor", SolrInfoBean.Category.CONTAINER.toString(), "threadPool"));
        shardHandlerFactory = ShardHandlerFactory.newInstance(cfg.getShardHandlerFactoryPluginInfo(), loader);
        if (shardHandlerFactory instanceof SolrMetricProducer) {
            SolrMetricProducer metricProducer = (SolrMetricProducer) shardHandlerFactory;
            metricProducer.initializeMetrics(solrMetricsContext, "httpShardHandler");
        }
        updateShardHandler = new UpdateShardHandler(cfg.getUpdateShardHandlerConfig());
        updateShardHandler.initializeMetrics(solrMetricsContext, "updateShardHandler");
        solrClientCache = new SolrClientCache(updateShardHandler.getDefaultHttpClient());
        CalciteSolrDriver.INSTANCE.setSolrClientCache(solrClientCache);
        solrCores.load(loader);
        logging = LogWatcher.newRegisteredLogWatcher(cfg.getLogWatcherConfig(), loader);
        hostName = cfg.getNodeName();
        zkSys.initZooKeeper(this, cfg.getCloudConfig());
        if (isZooKeeperAware()) {
            pkiAuthenticationPlugin = new PKIAuthenticationPlugin(this, zkSys.getZkController().getNodeName(), (PublicKeyHandler) containerHandlers.get(PublicKeyHandler.PATH));
            pkiAuthenticationPlugin.initializeMetrics(solrMetricsContext, "/authentication/pki");
            TracerConfigurator.loadTracer(loader, cfg.getTracerConfiguratorPluginInfo(), getZkController().getZkStateReader());
            packageLoader = new PackageLoader(this);
            containerHandlers.getApiBag().registerObject(packageLoader.getPackageAPI().editAPI);
            containerHandlers.getApiBag().registerObject(packageLoader.getPackageAPI().readAPI);
            ZookeeperReadAPI zookeeperReadAPI = new ZookeeperReadAPI(this);
            containerHandlers.getApiBag().registerObject(zookeeperReadAPI);
        }
        MDCLoggingContext.setNode(this);
        securityConfHandler = isZooKeeperAware() ? new SecurityConfHandlerZk(this) : new SecurityConfHandlerLocal(this);
        reloadSecurityProperties();
        warnUsersOfInsecureSettings();
        this.backupRepoFactory = new BackupRepositoryFactory(cfg.getBackupRepositoryPlugins());
        createHandler(ZK_PATH, ZookeeperInfoHandler.class.getName(), ZookeeperInfoHandler.class);
        createHandler(ZK_STATUS_PATH, ZookeeperStatusHandler.class.getName(), ZookeeperStatusHandler.class);
        collectionsHandler = createHandler(COLLECTIONS_HANDLER_PATH, cfg.getCollectionsHandlerClass(), CollectionsHandler.class);
        ClusterAPI clusterAPI = new ClusterAPI(collectionsHandler);
        containerHandlers.getApiBag().registerObject(clusterAPI);
        containerHandlers.getApiBag().registerObject(clusterAPI.commands);
        healthCheckHandler = loader.newInstance(cfg.getHealthCheckHandlerClass(), HealthCheckHandler.class, null, new Class<?>[] { CoreContainer.class }, new Object[] { this });
        infoHandler = createHandler(INFO_HANDLER_PATH, cfg.getInfoHandlerClass(), InfoHandler.class);
        coreAdminHandler = createHandler(CORES_HANDLER_PATH, cfg.getCoreAdminHandlerClass(), CoreAdminHandler.class);
        configSetsHandler = createHandler(CONFIGSETS_HANDLER_PATH, cfg.getConfigSetsHandlerClass(), ConfigSetsHandler.class);
        metricsHandler = new MetricsHandler(this);
        containerHandlers.put(METRICS_PATH, metricsHandler);
        metricsHandler.initializeMetrics(solrMetricsContext, METRICS_PATH);
        createMetricsHistoryHandler();
        metricsCollectorHandler = createHandler(MetricsCollectorHandler.HANDLER_PATH, MetricsCollectorHandler.class.getName(), MetricsCollectorHandler.class);
        metricsCollectorHandler.init(null);
        containerHandlers.put(AUTHZ_PATH, securityConfHandler);
        securityConfHandler.initializeMetrics(solrMetricsContext, AUTHZ_PATH);
        containerHandlers.put(AUTHC_PATH, securityConfHandler);
        PluginInfo[] metricReporters = cfg.getMetricsConfig().getMetricReporters();
        metricManager.loadReporters(metricReporters, loader, this, null, null, SolrInfoBean.Group.node);
        metricManager.loadReporters(metricReporters, loader, this, null, null, SolrInfoBean.Group.jvm);
        metricManager.loadReporters(metricReporters, loader, this, null, null, SolrInfoBean.Group.jetty);
        coreConfigService = ConfigSetService.createConfigSetService(cfg, loader, zkSys.zkController);
        containerProperties.putAll(cfg.getSolrProperties());
        solrMetricsContext.gauge(() -> solrCores.getCores().size(), true, "loaded", SolrInfoBean.Category.CONTAINER.toString(), "cores");
        solrMetricsContext.gauge(() -> solrCores.getLoadedCoreNames().size() - solrCores.getCores().size(), true, "lazy", SolrInfoBean.Category.CONTAINER.toString(), "cores");
        solrMetricsContext.gauge(() -> solrCores.getAllCoreNames().size() - solrCores.getLoadedCoreNames().size(), true, "unloaded", SolrInfoBean.Category.CONTAINER.toString(), "cores");
        Path dataHome = cfg.getSolrDataHome() != null ? cfg.getSolrDataHome() : cfg.getCoreRootDirectory();
        solrMetricsContext.gauge(() -> dataHome.toFile().getTotalSpace(), true, "totalSpace", SolrInfoBean.Category.CONTAINER.toString(), "fs");
        solrMetricsContext.gauge(() -> dataHome.toFile().getUsableSpace(), true, "usableSpace", SolrInfoBean.Category.CONTAINER.toString(), "fs");
        solrMetricsContext.gauge(() -> dataHome.toString(), true, "path", SolrInfoBean.Category.CONTAINER.toString(), "fs");
        solrMetricsContext.gauge(() -> {
            try {
                return org.apache.lucene.util.IOUtils.spins(dataHome);
            } catch (IOException e) {
                return true;
            }
        }, true, "spins", SolrInfoBean.Category.CONTAINER.toString(), "fs");
        solrMetricsContext.gauge(() -> cfg.getCoreRootDirectory().toFile().getTotalSpace(), true, "totalSpace", SolrInfoBean.Category.CONTAINER.toString(), "fs", "coreRoot");
        solrMetricsContext.gauge(() -> cfg.getCoreRootDirectory().toFile().getUsableSpace(), true, "usableSpace", SolrInfoBean.Category.CONTAINER.toString(), "fs", "coreRoot");
        solrMetricsContext.gauge(() -> cfg.getCoreRootDirectory().toString(), true, "path", SolrInfoBean.Category.CONTAINER.toString(), "fs", "coreRoot");
        solrMetricsContext.gauge(() -> {
            try {
                return org.apache.lucene.util.IOUtils.spins(cfg.getCoreRootDirectory());
            } catch (IOException e) {
                return true;
            }
        }, true, "spins", SolrInfoBean.Category.CONTAINER.toString(), "fs", "coreRoot");
        solrMetricsContext.gauge(() -> this.getClass().getPackage().getSpecificationVersion(), true, "specification", SolrInfoBean.Category.CONTAINER.toString(), "version");
        solrMetricsContext.gauge(() -> this.getClass().getPackage().getImplementationVersion(), true, "implementation", SolrInfoBean.Category.CONTAINER.toString(), "version");
        SolrFieldCacheBean fieldCacheBean = new SolrFieldCacheBean();
        fieldCacheBean.initializeMetrics(solrMetricsContext, null);
        if (isZooKeeperAware()) {
            metricManager.loadClusterReporters(metricReporters, this);
        }
        ExecutorService coreLoadExecutor = MetricUtils.instrumentedExecutorService(ExecutorUtil.newMDCAwareFixedThreadPool(cfg.getCoreLoadThreadCount(isZooKeeperAware()), new SolrNamedThreadFactory("coreLoadExecutor")), null, metricManager.registry(SolrMetricManager.getRegistryName(SolrInfoBean.Group.node)), SolrMetricManager.mkName("coreLoadExecutor", SolrInfoBean.Category.CONTAINER.toString(), "threadPool"));
        final List<Future<SolrCore>> futures = new ArrayList<>();
        try {
            List<CoreDescriptor> cds = coresLocator.discover(this);
            cds = CoreSorter.sortCores(this, cds);
            checkForDuplicateCoreNames(cds);
            status |= CORE_DISCOVERY_COMPLETE;
            for (final CoreDescriptor cd : cds) {
                if (cd.isTransient() || !cd.isLoadOnStartup()) {
                    solrCores.addCoreDescriptor(cd);
                } else if (asyncSolrCoreLoad) {
                    solrCores.markCoreAsLoading(cd);
                }
                if (cd.isLoadOnStartup()) {
                    futures.add(coreLoadExecutor.submit(() -> {
                        SolrCore core;
                        try {
                            if (zkSys.getZkController() != null) {
                                zkSys.getZkController().throwErrorIfReplicaReplaced(cd);
                            }
                            solrCores.waitAddPendingCoreOps(cd.getName());
                            core = createFromDescriptor(cd, false, false);
                        } finally {
                            solrCores.removeFromPendingOps(cd.getName());
                            if (asyncSolrCoreLoad) {
                                solrCores.markCoreAsNotLoading(cd);
                            }
                        }
                        try {
                            zkSys.registerInZk(core, true, false);
                        } catch (RuntimeException e) {
                            SolrException.log(log, "Error registering SolrCore", e);
                        }
                        return core;
                    }));
                }
            }
            backgroundCloser = new CloserThread(this, solrCores, cfg);
            backgroundCloser.start();
        } finally {
            if (asyncSolrCoreLoad && futures != null) {
                coreContainerWorkExecutor.submit(() -> {
                    try {
                        for (Future<SolrCore> future : futures) {
                            try {
                                future.get();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            } catch (ExecutionException e) {
                                log.error("Error waiting for SolrCore to be loaded on startup", e);
                            }
                        }
                    } finally {
                        ExecutorUtil.shutdownAndAwaitTermination(coreLoadExecutor);
                    }
                });
            } else {
                ExecutorUtil.shutdownAndAwaitTermination(coreLoadExecutor);
            }
        }
        if (isZooKeeperAware()) {
            customContainerPlugins.refresh();
            getZkController().zkStateReader.registerClusterPropertiesListener(customContainerPlugins);
            ContainerPluginsApi containerPluginsApi = new ContainerPluginsApi(this);
            containerHandlers.getApiBag().registerObject(containerPluginsApi.readAPI);
            containerHandlers.getApiBag().registerObject(containerPluginsApi.editAPI);
            zkSys.getZkController().checkOverseerDesignate();
        }
        status |= LOAD_COMPLETE | INITIAL_CORE_LOAD_COMPLETE;
    }

    @SuppressWarnings({ "unchecked" })
    private void createMetricsHistoryHandler() {
        PluginInfo plugin = cfg.getMetricsConfig().getHistoryHandler();
        Map<String, Object> initArgs;
        if (plugin != null && plugin.initArgs != null) {
            initArgs = plugin.initArgs.asMap(5);
            initArgs.putIfAbsent(MetricsHistoryHandler.ENABLE_PROP, plugin.isEnabled());
        } else {
            initArgs = new HashMap<>();
        }
        String name;
        SolrCloudManager cloudManager;
        SolrClient client;
        if (isZooKeeperAware()) {
            name = getZkController().getNodeName();
            cloudManager = getZkController().getSolrCloudManager();
            client = new CloudSolrClient.Builder(Collections.singletonList(getZkController().getZkServerAddress()), Optional.empty()).withSocketTimeout(30000).withConnectionTimeout(15000).withHttpClient(updateShardHandler.getDefaultHttpClient()).build();
        } else {
            name = getNodeConfig().getNodeName();
            if (name == null || name.isEmpty()) {
                name = "localhost";
            }
            cloudManager = null;
            client = new EmbeddedSolrServer(this, null) {

                @Override
                public void close() throws IOException {
                }
            };
            initArgs.putIfAbsent(MetricsHistoryHandler.ENABLE_NODES_PROP, true);
            initArgs.putIfAbsent(MetricsHistoryHandler.ENABLE_REPLICAS_PROP, true);
        }
        metricsHistoryHandler = new MetricsHistoryHandler(name, metricsHandler, client, cloudManager, initArgs);
        containerHandlers.put(METRICS_HISTORY_PATH, metricsHistoryHandler);
        metricsHistoryHandler.initializeMetrics(solrMetricsContext, METRICS_HISTORY_PATH);
    }

    public void securityNodeChanged() {
        log.info("Security node changed, reloading security.json");
        reloadSecurityProperties();
    }

    @SuppressWarnings({ "unchecked" })
    private void reloadSecurityProperties() {
        SecurityConfHandler.SecurityConfig securityConfig = securityConfHandler.getSecurityConfig(false);
        initializeAuthorizationPlugin((Map<String, Object>) securityConfig.getData().get("authorization"));
        initializeAuthenticationPlugin((Map<String, Object>) securityConfig.getData().get("authentication"));
        initializeAuditloggerPlugin((Map<String, Object>) securityConfig.getData().get("auditlogging"));
    }

    private void warnUsersOfInsecureSettings() {
        if (authenticationPlugin == null || authorizationPlugin == null) {
            log.warn("Not all security plugins configured!  authentication={} authorization={}.  Solr is only as secure as " + "you make it. Consider configuring authentication/authorization before exposing Solr to users internal or " + "external.  See https://s.apache.org/solrsecurity for more info", (authenticationPlugin != null) ? "enabled" : "disabled", (authorizationPlugin != null) ? "enabled" : "disabled");
        }
        if (authenticationPlugin != null && StringUtils.isEmpty(System.getProperty("solr.jetty.https.port"))) {
            log.warn("Solr authentication is enabled, but SSL is off.  Consider enabling SSL to protect user credentials and data with encryption.");
        }
    }

    private static void checkForDuplicateCoreNames(List<CoreDescriptor> cds) {
        Map<String, Path> addedCores = Maps.newHashMap();
        for (CoreDescriptor cd : cds) {
            final String name = cd.getName();
            if (addedCores.containsKey(name))
                throw new SolrException(ErrorCode.SERVER_ERROR, String.format(Locale.ROOT, "Found multiple cores with the name [%s], with instancedirs [%s] and [%s]", name, addedCores.get(name), cd.getInstanceDir()));
            addedCores.put(name, cd.getInstanceDir());
        }
    }

    private volatile boolean isShutDown = false;

    public boolean isShutDown() {
        return isShutDown;
    }

    public void shutdown() {
        ZkController zkController = getZkController();
        if (zkController != null) {
            OverseerTaskQueue overseerCollectionQueue = zkController.getOverseerCollectionQueue();
            overseerCollectionQueue.allowOverseerPendingTasksToComplete();
        }
        if (log.isInfoEnabled()) {
            log.info("Shutting down CoreContainer instance={}", System.identityHashCode(this));
        }
        ExecutorUtil.shutdownAndAwaitTermination(coreContainerAsyncTaskExecutor);
        ExecutorService customThreadPool = ExecutorUtil.newMDCAwareCachedThreadPool(new SolrNamedThreadFactory("closeThreadPool"));
        isShutDown = true;
        try {
            if (isZooKeeperAware()) {
                cancelCoreRecoveries();
                zkSys.zkController.preClose();
            }
            ExecutorUtil.shutdownAndAwaitTermination(coreContainerWorkExecutor);
            synchronized (solrCores.getModifyLock()) {
                solrCores.getModifyLock().notifyAll();
            }
            if (backgroundCloser != null) {
                try {
                    while (true) {
                        backgroundCloser.join(15000);
                        if (backgroundCloser.isAlive()) {
                            synchronized (solrCores.getModifyLock()) {
                                solrCores.getModifyLock().notifyAll();
                            }
                        } else {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    if (log.isDebugEnabled()) {
                        log.debug("backgroundCloser thread was interrupted before finishing");
                    }
                }
            }
            solrCores.close();
            objectCache.clear();
            synchronized (solrCores.getModifyLock()) {
                solrCores.getModifyLock().notifyAll();
            }
            customThreadPool.submit(() -> {
                replayUpdatesExecutor.shutdownAndAwaitTermination();
            });
            if (metricsHistoryHandler != null) {
                metricsHistoryHandler.close();
                IOUtils.closeQuietly(metricsHistoryHandler.getSolrClient());
            }
            if (metricManager != null) {
                metricManager.closeReporters(SolrMetricManager.getRegistryName(SolrInfoBean.Group.node));
                metricManager.closeReporters(SolrMetricManager.getRegistryName(SolrInfoBean.Group.jvm));
                metricManager.closeReporters(SolrMetricManager.getRegistryName(SolrInfoBean.Group.jetty));
                metricManager.unregisterGauges(SolrMetricManager.getRegistryName(SolrInfoBean.Group.node), metricTag);
                metricManager.unregisterGauges(SolrMetricManager.getRegistryName(SolrInfoBean.Group.jvm), metricTag);
                metricManager.unregisterGauges(SolrMetricManager.getRegistryName(SolrInfoBean.Group.jetty), metricTag);
            }
            if (isZooKeeperAware()) {
                cancelCoreRecoveries();
                if (metricManager != null) {
                    metricManager.closeReporters(SolrMetricManager.getRegistryName(SolrInfoBean.Group.cluster));
                }
            }
            try {
                if (coreAdminHandler != null) {
                    customThreadPool.submit(() -> {
                        coreAdminHandler.shutdown();
                    });
                }
            } catch (Exception e) {
                log.warn("Error shutting down CoreAdminHandler. Continuing to close CoreContainer.", e);
            }
            if (solrClientCache != null) {
                solrClientCache.close();
            }
        } finally {
            try {
                if (shardHandlerFactory != null) {
                    customThreadPool.submit(() -> {
                        shardHandlerFactory.close();
                    });
                }
            } finally {
                try {
                    if (updateShardHandler != null) {
                        customThreadPool.submit(() -> Collections.singleton(shardHandlerFactory).parallelStream().forEach(c -> {
                            updateShardHandler.close();
                        }));
                    }
                } finally {
                    try {
                        zkSys.close();
                    } finally {
                        ExecutorUtil.shutdownAndAwaitTermination(customThreadPool);
                    }
                }
            }
        }
        try {
            if (authorizationPlugin != null) {
                authorizationPlugin.plugin.close();
            }
        } catch (IOException e) {
            log.warn("Exception while closing authorization plugin.", e);
        }
        try {
            if (authenticationPlugin != null) {
                authenticationPlugin.plugin.close();
                authenticationPlugin = null;
            }
        } catch (Exception e) {
            log.warn("Exception while closing authentication plugin.", e);
        }
        try {
            if (auditloggerPlugin != null) {
                auditloggerPlugin.plugin.close();
                auditloggerPlugin = null;
            }
        } catch (Exception e) {
            log.warn("Exception while closing auditlogger plugin.", e);
        }
        if (packageLoader != null) {
            org.apache.lucene.util.IOUtils.closeWhileHandlingException(packageLoader);
        }
        org.apache.lucene.util.IOUtils.closeWhileHandlingException(loader);
    }

    public void cancelCoreRecoveries() {
        List<SolrCore> cores = solrCores.getCores();
        for (SolrCore core : cores) {
            try {
                core.getSolrCoreState().cancelRecovery();
            } catch (Exception e) {
                SolrException.log(log, "Error canceling recovery for core", e);
            }
        }
    }

    public CoresLocator getCoresLocator() {
        return coresLocator;
    }

    protected SolrCore registerCore(CoreDescriptor cd, SolrCore core, boolean registerInZk, boolean skipRecovery) {
        if (core == null) {
            throw new RuntimeException("Can not register a null core.");
        }
        if (isShutDown) {
            core.close();
            throw new IllegalStateException("This CoreContainer has been closed");
        }
        assert core.getName().equals(cd.getName()) : "core name " + core.getName() + " != cd " + cd.getName();
        SolrCore old = solrCores.putCore(cd, core);
        coreInitFailures.remove(cd.getName());
        if (old == null || old == core) {
            if (log.isDebugEnabled()) {
                log.debug("registering core: {}", cd.getName());
            }
            if (registerInZk) {
                zkSys.registerInZk(core, false, skipRecovery);
            }
            return null;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("replacing core: {}", cd.getName());
            }
            old.close();
            if (registerInZk) {
                zkSys.registerInZk(core, false, skipRecovery);
            }
            return old;
        }
    }

    public SolrCore create(String coreName, Map<String, String> parameters) {
        return create(coreName, cfg.getCoreRootDirectory().resolve(coreName), parameters, false);
    }

    public SolrCore create(String coreName, Path instancePath, Map<String, String> parameters, boolean newCollection) {
        CoreDescriptor cd = new CoreDescriptor(coreName, instancePath, parameters, getContainerProperties(), getZkController());
        if (getAllCoreNames().contains(coreName)) {
            log.warn("Creating a core with existing name is not allowed");
            throw new SolrException(ErrorCode.SERVER_ERROR, "Core with name '" + coreName + "' already exists.");
        }
        assertPathAllowed(cd.getInstanceDir());
        assertPathAllowed(Paths.get(cd.getDataDir()));
        boolean preExisitingZkEntry = false;
        try {
            if (getZkController() != null) {
                if (cd.getCloudDescriptor().getCoreNodeName() == null) {
                    throw new SolrException(ErrorCode.SERVER_ERROR, "coreNodeName missing " + parameters.toString());
                }
                preExisitingZkEntry = getZkController().checkIfCoreNodeNameAlreadyExists(cd);
            }
            coresLocator.create(this, cd);
            SolrCore core = null;
            try {
                solrCores.waitAddPendingCoreOps(cd.getName());
                core = createFromDescriptor(cd, true, newCollection);
                coresLocator.persist(this, cd);
            } finally {
                solrCores.removeFromPendingOps(cd.getName());
            }
            return core;
        } catch (Exception ex) {
            coresLocator.delete(this, cd);
            if (isZooKeeperAware() && !preExisitingZkEntry) {
                try {
                    getZkController().unregister(coreName, cd);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    SolrException.log(log, null, e);
                } catch (KeeperException e) {
                    SolrException.log(log, null, e);
                } catch (Exception e) {
                    SolrException.log(log, null, e);
                }
            }
            Throwable tc = ex;
            Throwable c = null;
            do {
                tc = tc.getCause();
                if (tc != null) {
                    c = tc;
                }
            } while (tc != null);
            String rootMsg = "";
            if (c != null) {
                rootMsg = " Caused by: " + c.getMessage();
            }
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Error CREATEing SolrCore '" + coreName + "': " + ex.getMessage() + rootMsg, ex);
        }
    }

    public void assertPathAllowed(Path pathToAssert) throws SolrException {
        SolrPaths.assertPathAllowed(pathToAssert, allowPaths);
    }

    @VisibleForTesting
    public Set<Path> getAllowPaths() {
        return allowPaths;
    }

    @SuppressWarnings("resource")
    private SolrCore createFromDescriptor(CoreDescriptor dcore, boolean publishState, boolean newCollection) {
        if (isShutDown) {
            throw new SolrException(ErrorCode.SERVICE_UNAVAILABLE, "Solr has been shutdown.");
        }
        SolrCore core = null;
        try {
            MDCLoggingContext.setCoreDescriptor(this, dcore);
            SolrIdentifierValidator.validateCoreName(dcore.getName());
            if (zkSys.getZkController() != null) {
                zkSys.getZkController().preRegister(dcore, publishState);
            }
            ConfigSet coreConfig = coreConfigService.loadConfigSet(dcore);
            dcore.setConfigSetTrusted(coreConfig.isTrusted());
            if (log.isInfoEnabled()) {
                log.info("Creating SolrCore '{}' using configuration from {}, trusted={}", dcore.getName(), coreConfig.getName(), dcore.isConfigSetTrusted());
            }
            try {
                core = new SolrCore(this, dcore, coreConfig);
            } catch (SolrException e) {
                core = processCoreCreateException(e, dcore, coreConfig);
            }
            if (!isZooKeeperAware() && core.getUpdateHandler().getUpdateLog() != null) {
                core.getUpdateHandler().getUpdateLog().recoverFromLog();
            }
            registerCore(dcore, core, publishState, newCollection);
            return core;
        } catch (Exception e) {
            coreInitFailures.put(dcore.getName(), new CoreLoadFailure(dcore, e));
            if (e instanceof ZkController.NotInClusterStateException && !newCollection) {
                unload(dcore.getName(), true, true, true);
                throw e;
            }
            solrCores.removeCoreDescriptor(dcore);
            final SolrException solrException = new SolrException(ErrorCode.SERVER_ERROR, "Unable to create core [" + dcore.getName() + "]", e);
            if (core != null && !core.isClosed())
                IOUtils.closeQuietly(core);
            throw solrException;
        } catch (Throwable t) {
            SolrException e = new SolrException(ErrorCode.SERVER_ERROR, "JVM Error creating core [" + dcore.getName() + "]: " + t.getMessage(), t);
            coreInitFailures.put(dcore.getName(), new CoreLoadFailure(dcore, e));
            solrCores.removeCoreDescriptor(dcore);
            if (core != null && !core.isClosed())
                IOUtils.closeQuietly(core);
            throw t;
        } finally {
            MDCLoggingContext.clear();
        }
    }

    public boolean isSharedFs(CoreDescriptor cd) {
        try (SolrCore core = this.getCore(cd.getName())) {
            if (core != null) {
                return core.getDirectoryFactory().isSharedStorage();
            } else {
                ConfigSet configSet = coreConfigService.loadConfigSet(cd);
                return DirectoryFactory.loadDirectoryFactory(configSet.getSolrConfig(), this, null).isSharedStorage();
            }
        }
    }

    private SolrCore processCoreCreateException(SolrException original, CoreDescriptor dcore, ConfigSet coreConfig) {
        Throwable cause = original;
        while ((cause = cause.getCause()) != null) {
            if (cause instanceof CorruptIndexException) {
                break;
            }
        }
        if (cause == null)
            throw original;
        CoreInitFailedAction action = CoreInitFailedAction.valueOf(System.getProperty(CoreInitFailedAction.class.getSimpleName(), "none"));
        log.debug("CorruptIndexException while creating core, will attempt to repair via {}", action);
        switch(action) {
            case fromleader:
                if (isZooKeeperAware()) {
                    CloudDescriptor desc = dcore.getCloudDescriptor();
                    try {
                        Replica leader = getZkController().getClusterState().getCollection(desc.getCollectionName()).getSlice(desc.getShardId()).getLeader();
                        if (leader != null && leader.getState() == State.ACTIVE) {
                            log.info("Found active leader, will attempt to create fresh core and recover.");
                            resetIndexDirectory(dcore, coreConfig);
                            getZkController().getShardTerms(desc.getCollectionName(), desc.getShardId()).setTermToZero(desc.getCoreNodeName());
                            return new SolrCore(this, dcore, coreConfig);
                        }
                    } catch (SolrException se) {
                        se.addSuppressed(original);
                        throw se;
                    }
                }
                throw original;
            case none:
                throw original;
            default:
                log.warn("Failed to create core, and did not recognize specified 'CoreInitFailedAction': [{}]. Valid options are {}.", action, Arrays.asList(CoreInitFailedAction.values()));
                throw original;
        }
    }

    private void resetIndexDirectory(CoreDescriptor dcore, ConfigSet coreConfig) {
        SolrConfig config = coreConfig.getSolrConfig();
        String registryName = SolrMetricManager.getRegistryName(SolrInfoBean.Group.core, dcore.getName());
        DirectoryFactory df = DirectoryFactory.loadDirectoryFactory(config, this, registryName);
        String dataDir = SolrCore.findDataDir(df, null, config, dcore);
        String tmpIdxDirName = "index." + new SimpleDateFormat(SnapShooter.DATE_FMT, Locale.ROOT).format(new Date());
        SolrCore.modifyIndexProps(df, dataDir, config, tmpIdxDirName);
        Directory dir = null;
        try {
            dir = df.get(dataDir, DirContext.META_DATA, config.indexConfig.lockType);
        } catch (IOException e) {
            throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e);
        } finally {
            try {
                df.release(dir);
                df.doneWithDirectory(dir);
            } catch (IOException e) {
                SolrException.log(log, e);
            }
        }
    }

    public Collection<SolrCore> getCores() {
        return solrCores.getCores();
    }

    public Collection<String> getLoadedCoreNames() {
        return solrCores.getLoadedCoreNames();
    }

    public Collection<String> getAllCoreNames() {
        return solrCores.getAllCoreNames();
    }

    public Map<String, CoreLoadFailure> getCoreInitFailures() {
        return ImmutableMap.copyOf(coreInitFailures);
    }

    private CoreDescriptor reloadCoreDescriptor(CoreDescriptor oldDesc) {
        if (oldDesc == null) {
            return null;
        }
        CorePropertiesLocator cpl = new CorePropertiesLocator(null);
        CoreDescriptor ret = cpl.buildCoreDescriptor(oldDesc.getInstanceDir().resolve(PROPERTIES_FILENAME), this);
        if (ret == null) {
            oldDesc.loadExtraProperties();
            return oldDesc;
        }
        if (ret.getCloudDescriptor() != null) {
            ret.getCloudDescriptor().reload(oldDesc.getCloudDescriptor());
        }
        return ret;
    }

    public void reload(String name) {
        reload(name, null);
    }

    public void reload(String name, UUID coreId, boolean async) {
        if (async) {
            runAsync(() -> reload(name, coreId));
        } else {
            reload(name, coreId);
        }
    }

    public void reload(String name, UUID coreId) {
        if (isShutDown) {
            throw new AlreadyClosedException();
        }
        SolrCore newCore = null;
        SolrCore core = solrCores.getCoreFromAnyList(name, false, coreId);
        if (core != null) {
            CoreDescriptor cd = reloadCoreDescriptor(core.getCoreDescriptor());
            solrCores.addCoreDescriptor(cd);
            Closeable oldCore = null;
            boolean success = false;
            try {
                solrCores.waitAddPendingCoreOps(cd.getName());
                ConfigSet coreConfig = coreConfigService.loadConfigSet(cd);
                if (log.isInfoEnabled()) {
                    log.info("Reloading SolrCore '{}' using configuration from {}", cd.getName(), coreConfig.getName());
                }
                newCore = core.reload(coreConfig);
                DocCollection docCollection = null;
                if (getZkController() != null) {
                    docCollection = getZkController().getClusterState().getCollection(cd.getCollectionName());
                    if (docCollection.getBool(ZkStateReader.READ_ONLY, false)) {
                        newCore.readOnly = true;
                    }
                }
                registerCore(cd, newCore, false, false);
                if (newCore.readOnly) {
                    RefCounted<IndexWriter> iwRef = core.getSolrCoreState().getIndexWriter(null);
                    if (iwRef != null) {
                        IndexWriter iw = iwRef.get();
                        core.readOnly = true;
                        try {
                            if (iw != null) {
                                iw.commit();
                            }
                        } finally {
                            iwRef.decref();
                        }
                    }
                }
                if (docCollection != null) {
                    Replica replica = docCollection.getReplica(cd.getCloudDescriptor().getCoreNodeName());
                    assert replica != null;
                    if (replica.getType() == Replica.Type.TLOG) {
                        getZkController().stopReplicationFromLeader(core.getName());
                        if (!cd.getCloudDescriptor().isLeader()) {
                            getZkController().startReplicationFromLeader(newCore.getName(), true);
                        }
                    } else if (replica.getType() == Replica.Type.PULL) {
                        getZkController().stopReplicationFromLeader(core.getName());
                        getZkController().startReplicationFromLeader(newCore.getName(), false);
                    }
                }
                success = true;
            } catch (SolrCoreState.CoreIsClosedException e) {
                throw e;
            } catch (Exception e) {
                coreInitFailures.put(cd.getName(), new CoreLoadFailure(cd, e));
                throw new SolrException(ErrorCode.SERVER_ERROR, "Unable to reload core [" + cd.getName() + "]", e);
            } finally {
                if (!success && newCore != null && newCore.getOpenCount() > 0) {
                    IOUtils.closeQuietly(newCore);
                }
                solrCores.removeFromPendingOps(cd.getName());
            }
        } else {
            CoreLoadFailure clf = coreInitFailures.get(name);
            if (clf != null) {
                try {
                    solrCores.waitAddPendingCoreOps(clf.cd.getName());
                    createFromDescriptor(clf.cd, true, false);
                } finally {
                    solrCores.removeFromPendingOps(clf.cd.getName());
                }
            } else {
                throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "No such core: " + name);
            }
        }
    }

    public void swap(String n0, String n1) {
        apiAssumeStandalone();
        if (n0 == null || n1 == null) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Can not swap unnamed cores.");
        }
        solrCores.swap(n0, n1);
        coresLocator.swap(this, solrCores.getCoreDescriptor(n0), solrCores.getCoreDescriptor(n1));
        log.info("swapped: {} with {}", n0, n1);
    }

    public void unload(String name) {
        unload(name, false, false, false);
    }

    public void unload(String name, boolean deleteIndexDir, boolean deleteDataDir, boolean deleteInstanceDir) {
        CoreDescriptor cd = solrCores.getCoreDescriptor(name);
        if (name != null) {
            CoreLoadFailure loadFailure = coreInitFailures.remove(name);
            if (loadFailure != null) {
                SolrCore.deleteUnloadedCore(loadFailure.cd, deleteDataDir, deleteInstanceDir);
                if (cd != null) {
                    solrCores.removeCoreDescriptor(cd);
                    coresLocator.delete(this, cd);
                }
                return;
            }
        }
        if (cd == null) {
            throw new SolrException(ErrorCode.BAD_REQUEST, "Cannot unload non-existent core [" + name + "]");
        }
        boolean close = solrCores.isLoadedNotPendingClose(name);
        SolrCore core = solrCores.remove(name);
        solrCores.removeCoreDescriptor(cd);
        coresLocator.delete(this, cd);
        if (core == null) {
            SolrCore.deleteUnloadedCore(cd, deleteDataDir, deleteInstanceDir);
            return;
        }
        metricManager.removeRegistry(core.getCoreMetricManager().getRegistryName());
        if (zkSys.getZkController() != null) {
            core.getSolrCoreState().cancelRecovery();
            if (cd.getCloudDescriptor().getReplicaType() == Replica.Type.PULL || cd.getCloudDescriptor().getReplicaType() == Replica.Type.TLOG) {
                zkSys.getZkController().stopReplicationFromLeader(name);
            }
        }
        core.unloadOnClose(cd, deleteIndexDir, deleteDataDir, deleteInstanceDir);
        if (close)
            core.closeAndWait();
        if (zkSys.getZkController() != null) {
            try {
                zkSys.getZkController().unregister(name, cd);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SolrException(ErrorCode.SERVER_ERROR, "Interrupted while unregistering core [" + name + "] from cloud state");
            } catch (KeeperException e) {
                throw new SolrException(ErrorCode.SERVER_ERROR, "Error unregistering core [" + name + "] from cloud state", e);
            } catch (Exception e) {
                throw new SolrException(ErrorCode.SERVER_ERROR, "Error unregistering core [" + name + "] from cloud state", e);
            }
        }
    }

    public void rename(String name, String toName) {
        apiAssumeStandalone();
        SolrIdentifierValidator.validateCoreName(toName);
        try (SolrCore core = getCore(name)) {
            if (core != null) {
                String oldRegistryName = core.getCoreMetricManager().getRegistryName();
                String newRegistryName = SolrCoreMetricManager.createRegistryName(core, toName);
                metricManager.swapRegistries(oldRegistryName, newRegistryName);
                CoreDescriptor cd = core.getCoreDescriptor();
                solrCores.removeCoreDescriptor(cd);
                cd.setProperty("name", toName);
                solrCores.addCoreDescriptor(cd);
                core.setName(toName);
                registerCore(cd, core, true, false);
                SolrCore old = solrCores.remove(name);
                coresLocator.rename(this, old.getCoreDescriptor(), core.getCoreDescriptor());
            }
        }
    }

    private void apiAssumeStandalone() {
        if (getZkController() != null) {
            throw new SolrException(ErrorCode.BAD_REQUEST, "Not supported in SolrCloud");
        }
    }

    public List<CoreDescriptor> getCoreDescriptors() {
        return solrCores.getCoreDescriptors();
    }

    public CoreDescriptor getCoreDescriptor(String coreName) {
        return solrCores.getCoreDescriptor(coreName);
    }

    public Path getCoreRootDirectory() {
        return cfg.getCoreRootDirectory();
    }

    public SolrCore getCore(String name) {
        return getCore(name, null);
    }

    public SolrCore getCore(String name, UUID id) {
        SolrCore core = solrCores.getCoreFromAnyList(name, true, id);
        if (core != null) {
            return core;
        }
        CoreDescriptor desc = solrCores.getCoreDescriptor(name);
        CoreLoadFailure loadFailure = getCoreInitFailures().get(name);
        if (null != loadFailure) {
            throw new SolrCoreInitializationException(name, loadFailure.exception);
        }
        if (desc == null || zkSys.getZkController() != null)
            return null;
        core = solrCores.waitAddPendingCoreOps(name);
        if (isShutDown)
            return null;
        try {
            if (core == null) {
                if (zkSys.getZkController() != null) {
                    zkSys.getZkController().throwErrorIfReplicaReplaced(desc);
                }
                core = createFromDescriptor(desc, true, false);
            }
            core.open();
        } finally {
            solrCores.removeFromPendingOps(name);
        }
        return core;
    }

    public BlobRepository getBlobRepository() {
        return blobRepository;
    }

    public void waitForLoadingCoresToFinish(long timeoutMs) {
        solrCores.waitForLoadingCoresToFinish(timeoutMs);
    }

    public void waitForLoadingCore(String name, long timeoutMs) {
        solrCores.waitForLoadingCoreToFinish(name, timeoutMs);
    }

    @SuppressWarnings({ "rawtypes" })
    protected <T> T createHandler(String path, String handlerClass, Class<T> clazz) {
        T handler = loader.newInstance(handlerClass, clazz, null, new Class[] { CoreContainer.class }, new Object[] { this });
        if (handler instanceof SolrRequestHandler) {
            containerHandlers.put(path, (SolrRequestHandler) handler);
        }
        if (handler instanceof SolrMetricProducer) {
            ((SolrMetricProducer) handler).initializeMetrics(solrMetricsContext, path);
        }
        return handler;
    }

    public CoreAdminHandler getMultiCoreHandler() {
        return coreAdminHandler;
    }

    public CollectionsHandler getCollectionsHandler() {
        return collectionsHandler;
    }

    public HealthCheckHandler getHealthCheckHandler() {
        return healthCheckHandler;
    }

    public InfoHandler getInfoHandler() {
        return infoHandler;
    }

    public ConfigSetsHandler getConfigSetsHandler() {
        return configSetsHandler;
    }

    public String getHostName() {
        return this.hostName;
    }

    public String getManagementPath() {
        return cfg.getManagementPath();
    }

    @SuppressWarnings({ "rawtypes" })
    public LogWatcher getLogging() {
        return logging;
    }

    public boolean isLoaded(String name) {
        return solrCores.isLoaded(name);
    }

    public boolean isLoadedNotPendingClose(String name) {
        return solrCores.isLoadedNotPendingClose(name);
    }

    public void queueCoreToClose(SolrCore coreToClose) {
        solrCores.queueCoreToClose(coreToClose);
    }

    public CoreDescriptor getUnloadedCoreDescriptor(String cname) {
        return solrCores.getUnloadedCoreDescriptor(cname);
    }

    public String getSolrHome() {
        return solrHome.toString();
    }

    public Path getUserFilesPath() {
        return solrHome.resolve("userfiles");
    }

    public boolean isZooKeeperAware() {
        return zkSys.getZkController() != null;
    }

    public ZkController getZkController() {
        return zkSys.getZkController();
    }

    public NodeConfig getConfig() {
        return cfg;
    }

    public ShardHandlerFactory getShardHandlerFactory() {
        return shardHandlerFactory;
    }

    public UpdateShardHandler getUpdateShardHandler() {
        return updateShardHandler;
    }

    public SolrResourceLoader getResourceLoader() {
        return loader;
    }

    public boolean isCoreLoading(String name) {
        return solrCores.isCoreLoading(name);
    }

    public AuthorizationPlugin getAuthorizationPlugin() {
        return authorizationPlugin == null ? null : authorizationPlugin.plugin;
    }

    public AuthenticationPlugin getAuthenticationPlugin() {
        return authenticationPlugin == null ? null : authenticationPlugin.plugin;
    }

    public AuditLoggerPlugin getAuditLoggerPlugin() {
        return auditloggerPlugin == null ? null : auditloggerPlugin.plugin;
    }

    public NodeConfig getNodeConfig() {
        return cfg;
    }

    public long getStatus() {
        return status;
    }

    public TransientSolrCoreCache getTransientCache() {
        return solrCores.getTransientCacheHandler();
    }

    public boolean checkTragicException(SolrCore solrCore) {
        Throwable tragicException;
        try {
            tragicException = solrCore.getSolrCoreState().getTragicException();
        } catch (IOException e) {
            tragicException = e;
        }
        if (tragicException != null && isZooKeeperAware()) {
            getZkController().giveupLeadership(solrCore.getCoreDescriptor(), tragicException);
        }
        return tragicException != null;
    }

    public CustomContainerPlugins getCustomContainerPlugins() {
        return customContainerPlugins;
    }

    static {
        ExecutorUtil.addThreadLocalProvider(SolrRequestInfo.getInheritableThreadLocalProvider());
    }

    public void runAsync(Runnable r) {
        coreContainerAsyncTaskExecutor.submit(r);
    }
}

class CloserThread extends Thread {

    CoreContainer container;

    SolrCores solrCores;

    NodeConfig cfg;

    CloserThread(CoreContainer container, SolrCores solrCores, NodeConfig cfg) {
        this.container = container;
        this.solrCores = solrCores;
        this.cfg = cfg;
    }

    @Override
    public void run() {
        while (!container.isShutDown()) {
            synchronized (solrCores.getModifyLock()) {
                try {
                    solrCores.getModifyLock().wait();
                } catch (InterruptedException e) {
                }
            }
            for (SolrCore removeMe = solrCores.getCoreToClose(); removeMe != null && !container.isShutDown(); removeMe = solrCores.getCoreToClose()) {
                try {
                    removeMe.close();
                } finally {
                    solrCores.removeFromPendingOps(removeMe.getName());
                }
            }
        }
    }
}
