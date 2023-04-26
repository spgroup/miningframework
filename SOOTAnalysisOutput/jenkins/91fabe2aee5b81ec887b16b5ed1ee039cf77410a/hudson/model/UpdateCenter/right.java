package hudson.model;

import com.google.common.annotations.VisibleForTesting;
import hudson.BulkChange;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.Functions;
import hudson.PluginManager;
import hudson.PluginWrapper;
import hudson.ProxyConfiguration;
import hudson.security.ACLContext;
import hudson.util.VersionNumber;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import jenkins.security.stapler.StaplerDispatchable;
import jenkins.util.SystemProperties;
import hudson.Util;
import hudson.XmlFile;
import static hudson.init.InitMilestone.PLUGINS_STARTED;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import hudson.init.Initializer;
import hudson.lifecycle.Lifecycle;
import hudson.lifecycle.RestartNotSupportedException;
import hudson.model.UpdateSite.Data;
import hudson.model.UpdateSite.Plugin;
import hudson.model.listeners.SaveableListener;
import hudson.remoting.AtmostOneThreadExecutor;
import hudson.security.ACL;
import hudson.util.DaemonThreadFactory;
import hudson.util.FormValidation;
import hudson.util.HttpResponses;
import hudson.util.NamingThreadFactory;
import hudson.util.PersistedList;
import hudson.util.XStream2;
import jenkins.MissingDependencyException;
import jenkins.RestartRequiredException;
import jenkins.install.InstallUtil;
import jenkins.model.Jenkins;
import jenkins.util.io.OnMaster;
import jenkins.util.java.JavaUtils;
import net.sf.json.JSONObject;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.jenkinsci.Symbol;
import org.jvnet.localizer.Localizable;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import javax.annotation.Nonnull;
import javax.net.ssl.SSLHandshakeException;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.io.IOUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.interceptor.RequirePOST;

@ExportedBean
public class UpdateCenter extends AbstractModelObject implements Saveable, OnMaster, StaplerProxy {

    private static final Logger LOGGER;

    private static final String UPDATE_CENTER_URL;

    private static final int PLUGIN_DOWNLOAD_READ_TIMEOUT = SystemProperties.getInteger(UpdateCenter.class.getName() + ".pluginDownloadReadTimeoutSeconds", 60) * 1000;

    public static final String PREDEFINED_UPDATE_SITE_ID = "default";

    public static final String ID_DEFAULT = SystemProperties.getString(UpdateCenter.class.getName() + ".defaultUpdateSiteId", PREDEFINED_UPDATE_SITE_ID);

    @Restricted(NoExternalUse.class)
    public static final String ID_UPLOAD = "_upload";

    private final ExecutorService installerService = new AtmostOneThreadExecutor(new NamingThreadFactory(new DaemonThreadFactory(), "Update center installer thread"));

    protected final ExecutorService updateService = Executors.newCachedThreadPool(new NamingThreadFactory(new DaemonThreadFactory(), "Update site data downloader"));

    private final Vector<UpdateCenterJob> jobs = new Vector<UpdateCenterJob>();

    private final Set<UpdateSite> sourcesUsed = new HashSet<UpdateSite>();

    private final PersistedList<UpdateSite> sites = new PersistedList<UpdateSite>(this);

    private UpdateCenterConfiguration config;

    private boolean requiresRestart;

    static {
        Logger logger = Logger.getLogger(UpdateCenter.class.getName());
        LOGGER = logger;
        String ucOverride = SystemProperties.getString(UpdateCenter.class.getName() + ".updateCenterUrl");
        if (ucOverride != null) {
            logger.log(Level.INFO, "Using a custom update center defined by the system property: {0}", ucOverride);
            UPDATE_CENTER_URL = ucOverride;
        } else {
            UPDATE_CENTER_URL = "https://updates.jenkins.io/";
        }
    }

    @Restricted(NoExternalUse.class)
    static enum ConnectionStatus {

        PRECHECK,
        SKIPPED,
        CHECKING,
        UNCHECKED,
        OK,
        FAILED;

        static final String INTERNET = "internet";

        static final String UPDATE_SITE = "updatesite";
    }

    public UpdateCenter() {
        configure(new UpdateCenterConfiguration());
    }

    UpdateCenter(@Nonnull UpdateCenterConfiguration configuration) {
        configure(configuration);
    }

    @Nonnull
    public static UpdateCenter createUpdateCenter(@CheckForNull UpdateCenterConfiguration config) {
        String requiredClassName = SystemProperties.getString(UpdateCenter.class.getName() + ".className", null);
        if (requiredClassName == null) {
            LOGGER.log(Level.FINE, "Using the default Update Center implementation");
            return createDefaultUpdateCenter(config);
        }
        LOGGER.log(Level.FINE, "Using the custom update center: {0}", requiredClassName);
        try {
            final Class<?> clazz = Class.forName(requiredClassName).asSubclass(UpdateCenter.class);
            if (!UpdateCenter.class.isAssignableFrom(clazz)) {
                LOGGER.log(Level.SEVERE, "The specified custom Update Center {0} is not an instance of {1}. Falling back to default.", new Object[] { requiredClassName, UpdateCenter.class.getName() });
                return createDefaultUpdateCenter(config);
            }
            final Class<? extends UpdateCenter> ucClazz = clazz.asSubclass(UpdateCenter.class);
            final Constructor<? extends UpdateCenter> defaultConstructor = ucClazz.getConstructor();
            final Constructor<? extends UpdateCenter> configConstructor = ucClazz.getConstructor(UpdateCenterConfiguration.class);
            LOGGER.log(Level.FINE, "Using the constructor {0} Update Center configuration for {1}", new Object[] { config != null ? "with" : "without", requiredClassName });
            return config != null ? configConstructor.newInstance(config) : defaultConstructor.newInstance();
        } catch (ClassCastException e) {
            LOGGER.log(WARNING, "UpdateCenter class {0} does not extend hudson.model.UpdateCenter. Using default.", requiredClassName);
        } catch (NoSuchMethodException e) {
            LOGGER.log(WARNING, String.format("UpdateCenter class %s does not define one of the required constructors. Using default", requiredClassName), e);
        } catch (Exception e) {
            LOGGER.log(WARNING, String.format("Unable to instantiate custom plugin manager [%s]. Using default.", requiredClassName), e);
        }
        return createDefaultUpdateCenter(config);
    }

    @Nonnull
    private static UpdateCenter createDefaultUpdateCenter(@CheckForNull UpdateCenterConfiguration config) {
        return config != null ? new UpdateCenter(config) : new UpdateCenter();
    }

    public Api getApi() {
        return new Api(this);
    }

    public void configure(UpdateCenterConfiguration config) {
        if (config != null) {
            this.config = config;
        }
    }

    @Exported
    @StaplerDispatchable
    public List<UpdateCenterJob> getJobs() {
        synchronized (jobs) {
            return new ArrayList<UpdateCenterJob>(jobs);
        }
    }

    public UpdateCenterJob getJob(int id) {
        synchronized (jobs) {
            for (UpdateCenterJob job : jobs) {
                if (job.id == id)
                    return job;
            }
        }
        return null;
    }

    public InstallationJob getJob(Plugin plugin) {
        List<UpdateCenterJob> jobList = getJobs();
        Collections.reverse(jobList);
        for (UpdateCenterJob job : jobList) if (job instanceof InstallationJob) {
            InstallationJob ij = (InstallationJob) job;
            if (ij.plugin.name.equals(plugin.name) && ij.plugin.sourceId.equals(plugin.sourceId))
                return ij;
        }
        return null;
    }

    @Restricted(DoNotUse.class)
    public HttpResponse doConnectionStatus(StaplerRequest request) {
        try {
            String siteId = request.getParameter("siteId");
            if (siteId == null) {
                siteId = ID_DEFAULT;
            } else if (siteId.equals("default")) {
                siteId = ID_DEFAULT;
            }
            ConnectionCheckJob checkJob = getConnectionCheckJob(siteId);
            if (checkJob == null) {
                UpdateSite site = getSite(siteId);
                if (site != null) {
                    checkJob = addConnectionCheckJob(site);
                }
            }
            if (checkJob != null) {
                boolean isOffline = false;
                for (ConnectionStatus status : checkJob.connectionStates.values()) {
                    if (ConnectionStatus.FAILED.equals(status)) {
                        isOffline = true;
                        break;
                    }
                }
                if (isOffline) {
                    checkJob.run();
                    isOffline = false;
                    for (ConnectionStatus status : checkJob.connectionStates.values()) {
                        if (ConnectionStatus.FAILED.equals(status)) {
                            isOffline = true;
                            break;
                        }
                    }
                    if (!isOffline) {
                        updateAllSites();
                    }
                }
                return HttpResponses.okJSON(checkJob.connectionStates);
            } else {
                return HttpResponses.errorJSON(String.format("Cannot check connection status of the update site with ID='%s'" + ". This update center cannot be resolved", siteId));
            }
        } catch (Exception e) {
            return HttpResponses.errorJSON(String.format("ERROR: %s", e.getMessage()));
        }
    }

    @Restricted(DoNotUse.class)
    public HttpResponse doIncompleteInstallStatus() {
        try {
            Map<String, String> jobs = InstallUtil.getPersistedInstallStatus();
            if (jobs == null) {
                jobs = Collections.emptyMap();
            }
            return HttpResponses.okJSON(jobs);
        } catch (Exception e) {
            return HttpResponses.errorJSON(String.format("ERROR: %s", e.getMessage()));
        }
    }

    @Restricted(NoExternalUse.class)
    public synchronized void persistInstallStatus() {
        List<UpdateCenterJob> jobs = getJobs();
        boolean activeInstalls = false;
        for (UpdateCenterJob job : jobs) {
            if (job instanceof InstallationJob) {
                InstallationJob installationJob = (InstallationJob) job;
                if (!installationJob.status.isSuccess()) {
                    activeInstalls = true;
                }
            }
        }
        if (activeInstalls) {
            InstallUtil.persistInstallStatus(jobs);
        } else {
            InstallUtil.clearInstallStatus();
        }
    }

    @Restricted(DoNotUse.class)
    public HttpResponse doInstallStatus(StaplerRequest request) {
        try {
            String correlationId = request.getParameter("correlationId");
            Map<String, Object> response = new HashMap<>();
            response.put("state", Jenkins.getInstance().getInstallState().name());
            List<Map<String, String>> installStates = new ArrayList<>();
            response.put("jobs", installStates);
            List<UpdateCenterJob> jobCopy = getJobs();
            for (UpdateCenterJob job : jobCopy) {
                if (job instanceof InstallationJob) {
                    UUID jobCorrelationId = job.getCorrelationId();
                    if (correlationId == null || (jobCorrelationId != null && correlationId.equals(jobCorrelationId.toString()))) {
                        InstallationJob installationJob = (InstallationJob) job;
                        Map<String, String> pluginInfo = new LinkedHashMap<>();
                        pluginInfo.put("name", installationJob.plugin.name);
                        pluginInfo.put("version", installationJob.plugin.version);
                        pluginInfo.put("title", installationJob.plugin.title);
                        pluginInfo.put("installStatus", installationJob.status.getType());
                        pluginInfo.put("requiresRestart", Boolean.toString(installationJob.status.requiresRestart()));
                        if (jobCorrelationId != null) {
                            pluginInfo.put("correlationId", jobCorrelationId.toString());
                        }
                        installStates.add(pluginInfo);
                    }
                }
            }
            return HttpResponses.okJSON(JSONObject.fromObject(response));
        } catch (Exception e) {
            return HttpResponses.errorJSON(String.format("ERROR: %s", e.getMessage()));
        }
    }

    public HudsonUpgradeJob getHudsonJob() {
        List<UpdateCenterJob> jobList = getJobs();
        Collections.reverse(jobList);
        for (UpdateCenterJob job : jobList) if (job instanceof HudsonUpgradeJob)
            return (HudsonUpgradeJob) job;
        return null;
    }

    @StaplerDispatchable
    public PersistedList<UpdateSite> getSites() {
        return sites;
    }

    @Exported(name = "sites")
    public List<UpdateSite> getSiteList() {
        return sites.toList();
    }

    @CheckForNull
    public UpdateSite getSite(String id) {
        return getById(id);
    }

    public String getLastUpdatedString() {
        long newestTs = 0;
        for (UpdateSite s : sites) {
            if (s.getDataTimestamp() > newestTs) {
                newestTs = s.getDataTimestamp();
            }
        }
        if (newestTs == 0) {
            return Messages.UpdateCenter_n_a();
        }
        return Util.getPastTimeString(System.currentTimeMillis() - newestTs);
    }

    @CheckForNull
    public UpdateSite getById(String id) {
        for (UpdateSite s : sites) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        return null;
    }

    @CheckForNull
    public UpdateSite getCoreSource() {
        for (UpdateSite s : sites) {
            Data data = s.getData();
            if (data != null && data.core != null)
                return s;
        }
        return null;
    }

    @Deprecated
    public String getDefaultBaseUrl() {
        return config.getUpdateCenterUrl();
    }

    @CheckForNull
    public Plugin getPlugin(String artifactId) {
        for (UpdateSite s : sites) {
            Plugin p = s.getPlugin(artifactId);
            if (p != null)
                return p;
        }
        return null;
    }

    @CheckForNull
    public Plugin getPlugin(String artifactId, @CheckForNull VersionNumber minVersion) {
        if (minVersion == null) {
            return getPlugin(artifactId);
        }
        for (UpdateSite s : sites) {
            Plugin p = s.getPlugin(artifactId);
            if (p != null) {
                if (minVersion.isNewerThan(new VersionNumber(p.version)))
                    continue;
                return p;
            }
        }
        return null;
    }

    @RequirePOST
    public void doUpgrade(StaplerResponse rsp) throws IOException, ServletException {
        HudsonUpgradeJob job = new HudsonUpgradeJob(getCoreSource(), Jenkins.getAuthentication());
        if (!Lifecycle.get().canRewriteHudsonWar()) {
            sendError("Jenkins upgrade not supported in this running mode");
            return;
        }
        LOGGER.info("Scheduling the core upgrade");
        addJob(job);
        rsp.sendRedirect2(".");
    }

    @RequirePOST
    public HttpResponse doInvalidateData() {
        for (UpdateSite site : sites) {
            site.doInvalidateData();
        }
        return HttpResponses.ok();
    }

    @RequirePOST
    public void doSafeRestart(StaplerRequest request, StaplerResponse response) throws IOException, ServletException {
        synchronized (jobs) {
            if (!isRestartScheduled()) {
                addJob(new RestartJenkinsJob(getCoreSource()));
                LOGGER.info("Scheduling Jenkins reboot");
            }
        }
        response.sendRedirect2(".");
    }

    @RequirePOST
    public void doCancelRestart(StaplerResponse response) throws IOException, ServletException {
        synchronized (jobs) {
            for (UpdateCenterJob job : jobs) {
                if (job instanceof RestartJenkinsJob) {
                    if (((RestartJenkinsJob) job).cancel()) {
                        LOGGER.info("Scheduled Jenkins reboot unscheduled");
                    }
                }
            }
        }
        response.sendRedirect2(".");
    }

    @Exported
    public boolean isRestartRequiredForCompletion() {
        return requiresRestart;
    }

    public boolean isRestartScheduled() {
        for (UpdateCenterJob job : getJobs()) {
            if (job instanceof RestartJenkinsJob) {
                RestartJenkinsJob.RestartJenkinsJobStatus status = ((RestartJenkinsJob) job).status;
                if (status instanceof RestartJenkinsJob.Pending || status instanceof RestartJenkinsJob.Running) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isDowngradable() {
        return new File(Lifecycle.get().getHudsonWar() + ".bak").exists();
    }

    @RequirePOST
    public void doDowngrade(StaplerResponse rsp) throws IOException, ServletException {
        if (!isDowngradable()) {
            sendError("Jenkins downgrade is not possible, probably backup does not exist");
            return;
        }
        HudsonDowngradeJob job = new HudsonDowngradeJob(getCoreSource(), Jenkins.getAuthentication());
        LOGGER.info("Scheduling the core downgrade");
        addJob(job);
        rsp.sendRedirect2(".");
    }

    @RequirePOST
    public void doRestart(StaplerResponse rsp) throws IOException, ServletException {
        HudsonDowngradeJob job = new HudsonDowngradeJob(getCoreSource(), Jenkins.getAuthentication());
        LOGGER.info("Scheduling the core downgrade");
        addJob(job);
        rsp.sendRedirect2(".");
    }

    public String getBackupVersion() {
        try {
            try (JarFile backupWar = new JarFile(new File(Lifecycle.get().getHudsonWar() + ".bak"))) {
                Attributes attrs = backupWar.getManifest().getMainAttributes();
                String v = attrs.getValue("Jenkins-Version");
                if (v == null)
                    v = attrs.getValue("Hudson-Version");
                return v;
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to read backup version ", e);
            return null;
        }
    }

    synchronized Future<UpdateCenterJob> addJob(UpdateCenterJob job) {
        addConnectionCheckJob(job.site);
        return job.submit();
    }

    @Nonnull
    private ConnectionCheckJob addConnectionCheckJob(@Nonnull UpdateSite site) {
        if (sourcesUsed.add(site)) {
            ConnectionCheckJob connectionCheckJob = newConnectionCheckJob(site);
            connectionCheckJob.submit();
            return connectionCheckJob;
        } else {
            ConnectionCheckJob connectionCheckJob = getConnectionCheckJob(site);
            if (connectionCheckJob != null) {
                return connectionCheckJob;
            } else {
                throw new IllegalStateException("Illegal addition of an UpdateCenter job without calling UpdateCenter.addJob. " + "No ConnectionCheckJob found for the site.");
            }
        }
    }

    @Restricted(NoExternalUse.class)
    ConnectionCheckJob newConnectionCheckJob(UpdateSite site) {
        return new ConnectionCheckJob(site);
    }

    @CheckForNull
    private ConnectionCheckJob getConnectionCheckJob(@Nonnull String siteId) {
        UpdateSite site = getSite(siteId);
        if (site == null) {
            return null;
        }
        return getConnectionCheckJob(site);
    }

    @CheckForNull
    private ConnectionCheckJob getConnectionCheckJob(@Nonnull UpdateSite site) {
        synchronized (jobs) {
            for (UpdateCenterJob job : jobs) {
                if (job instanceof ConnectionCheckJob && job.site.getId().equals(site.getId())) {
                    return (ConnectionCheckJob) job;
                }
            }
        }
        return null;
    }

    public String getDisplayName() {
        return Messages.UpdateCenter_DisplayName();
    }

    public String getSearchUrl() {
        return "updateCenter";
    }

    public synchronized void save() {
        if (BulkChange.contains(this))
            return;
        try {
            getConfigFile().write(sites);
            SaveableListener.fireOnChange(this, getConfigFile());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to save " + getConfigFile(), e);
        }
    }

    public synchronized void load() throws IOException {
        XmlFile file = getConfigFile();
        if (file.exists()) {
            try {
                sites.replaceBy(((PersistedList) file.unmarshal(sites)).toList());
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to load " + file, e);
            }
            boolean defaultSiteExists = false;
            for (UpdateSite site : sites) {
                if (site.isLegacyDefault()) {
                    sites.remove(site);
                } else if (ID_DEFAULT.equals(site.getId())) {
                    defaultSiteExists = true;
                }
            }
            if (!defaultSiteExists) {
                sites.add(createDefaultUpdateSite());
            }
        } else {
            if (sites.isEmpty()) {
                sites.add(createDefaultUpdateSite());
            }
        }
    }

    protected UpdateSite createDefaultUpdateSite() {
        return new UpdateSite(PREDEFINED_UPDATE_SITE_ID, config.getUpdateCenterUrl() + "update-center.json");
    }

    private XmlFile getConfigFile() {
        return new XmlFile(XSTREAM, new File(Jenkins.getInstance().root, UpdateCenter.class.getName() + ".xml"));
    }

    @Exported
    public List<Plugin> getAvailables() {
        Map<String, Plugin> pluginMap = new LinkedHashMap<String, Plugin>();
        for (UpdateSite site : sites) {
            for (Plugin plugin : site.getAvailables()) {
                final Plugin existing = pluginMap.get(plugin.name);
                if (existing == null) {
                    pluginMap.put(plugin.name, plugin);
                } else if (!existing.version.equals(plugin.version)) {
                    final String altKey = plugin.name + ":" + plugin.version;
                    if (!pluginMap.containsKey(altKey)) {
                        pluginMap.put(altKey, plugin);
                    }
                }
            }
        }
        return new ArrayList<Plugin>(pluginMap.values());
    }

    public PluginEntry[] getCategorizedAvailables() {
        TreeSet<PluginEntry> entries = new TreeSet<PluginEntry>();
        for (Plugin p : getAvailables()) {
            if (p.categories == null || p.categories.length == 0)
                entries.add(new PluginEntry(p, getCategoryDisplayName(null)));
            else
                for (String c : p.categories) entries.add(new PluginEntry(p, getCategoryDisplayName(c)));
        }
        return entries.toArray(new PluginEntry[entries.size()]);
    }

    private static String getCategoryDisplayName(String category) {
        if (category == null)
            return Messages.UpdateCenter_PluginCategory_misc();
        try {
            return (String) Messages.class.getMethod("UpdateCenter_PluginCategory_" + category.replace('-', '_')).invoke(null);
        } catch (Exception ex) {
            return Messages.UpdateCenter_PluginCategory_unrecognized(category);
        }
    }

    public List<Plugin> getUpdates() {
        Map<String, Plugin> pluginMap = new LinkedHashMap<String, Plugin>();
        for (UpdateSite site : sites) {
            for (Plugin plugin : site.getUpdates()) {
                final Plugin existing = pluginMap.get(plugin.name);
                if (existing == null) {
                    pluginMap.put(plugin.name, plugin);
                } else if (!existing.version.equals(plugin.version)) {
                    final String altKey = plugin.name + ":" + plugin.version;
                    if (!pluginMap.containsKey(altKey)) {
                        pluginMap.put(altKey, plugin);
                    }
                }
            }
        }
        return new ArrayList<Plugin>(pluginMap.values());
    }

    public List<FormValidation> updateAllSites() throws InterruptedException, ExecutionException {
        List<Future<FormValidation>> futures = new ArrayList<Future<FormValidation>>();
        for (UpdateSite site : getSites()) {
            Future<FormValidation> future = site.updateDirectly(DownloadService.signatureCheck);
            if (future != null) {
                futures.add(future);
            }
        }
        List<FormValidation> results = new ArrayList<FormValidation>();
        for (Future<FormValidation> f : futures) {
            results.add(f.get());
        }
        return results;
    }

    @Extension
    @Symbol("coreUpdate")
    public static final class CoreUpdateMonitor extends AdministrativeMonitor {

        @Override
        public String getDisplayName() {
            return Messages.UpdateCenter_CoreUpdateMonitor_DisplayName();
        }

        public boolean isActivated() {
            Data data = getData();
            return data != null && data.hasCoreUpdates();
        }

        public Data getData() {
            UpdateSite cs = Jenkins.getInstance().getUpdateCenter().getCoreSource();
            if (cs != null)
                return cs.getData();
            return null;
        }
    }

    @SuppressWarnings({ "UnusedDeclaration" })
    public static class UpdateCenterConfiguration implements ExtensionPoint {

        public UpdateCenterConfiguration() {
        }

        public void checkConnection(ConnectionCheckJob job, String connectionCheckUrl) throws IOException {
            testConnection(new URL(connectionCheckUrl));
        }

        public void checkUpdateCenter(ConnectionCheckJob job, String updateCenterUrl) throws IOException {
            testConnection(toUpdateCenterCheckUrl(updateCenterUrl));
        }

        static URL toUpdateCenterCheckUrl(String updateCenterUrl) throws MalformedURLException {
            URL url;
            if (updateCenterUrl.startsWith("http://") || updateCenterUrl.startsWith("https://")) {
                url = new URL(updateCenterUrl + (updateCenterUrl.indexOf('?') == -1 ? "?uctest" : "&uctest"));
            } else {
                url = new URL(updateCenterUrl);
            }
            return url;
        }

        public void preValidate(DownloadJob job, URL src) throws IOException {
        }

        public void postValidate(DownloadJob job, File src) throws IOException {
        }

        public File download(DownloadJob job, URL src) throws IOException {
            MessageDigest sha1 = null;
            MessageDigest sha256 = null;
            MessageDigest sha512 = null;
            try {
                sha1 = MessageDigest.getInstance("SHA-1");
                sha256 = MessageDigest.getInstance("SHA-256");
                sha512 = MessageDigest.getInstance("SHA-512");
            } catch (NoSuchAlgorithmException nsa) {
                LOGGER.log(Level.WARNING, "Failed to instantiate message digest algorithm, may only have weak or no verification of downloaded file", nsa);
            }
            URLConnection con = null;
            try {
                con = connect(job, src);
                con.setReadTimeout(PLUGIN_DOWNLOAD_READ_TIMEOUT);
                int total = con.getContentLength();
                byte[] buf = new byte[8192];
                int len;
                File dst = job.getDestination();
                File tmp = new File(dst.getPath() + ".tmp");
                LOGGER.info("Downloading " + job.getName());
                Thread t = Thread.currentThread();
                String oldName = t.getName();
                t.setName(oldName + ": " + src);
                try (OutputStream _out = Files.newOutputStream(tmp.toPath());
                    OutputStream out = sha1 != null ? new DigestOutputStream(sha256 != null ? new DigestOutputStream(sha512 != null ? new DigestOutputStream(_out, sha512) : _out, sha256) : _out, sha1) : _out;
                    InputStream in = con.getInputStream();
                    CountingInputStream cin = new CountingInputStream(in)) {
                    while ((len = cin.read(buf)) >= 0) {
                        out.write(buf, 0, len);
                        job.status = job.new Installing(total == -1 ? -1 : cin.getCount() * 100 / total);
                    }
                } catch (IOException | InvalidPathException e) {
                    throw new IOException("Failed to load " + src + " to " + tmp, e);
                } finally {
                    t.setName(oldName);
                }
                if (total != -1 && total != tmp.length()) {
                    throw new IOException("Inconsistent file length: expected " + total + " but only got " + tmp.length());
                }
                if (sha1 != null) {
                    byte[] digest = sha1.digest();
                    job.computedSHA1 = Base64.encodeBase64String(digest);
                }
                if (sha256 != null) {
                    byte[] digest = sha256.digest();
                    job.computedSHA256 = Base64.encodeBase64String(digest);
                }
                if (sha512 != null) {
                    byte[] digest = sha512.digest();
                    job.computedSHA512 = Base64.encodeBase64String(digest);
                }
                return tmp;
            } catch (IOException e) {
                String extraMessage = "";
                if (con != null && con.getURL() != null && !src.toString().equals(con.getURL().toString())) {
                    extraMessage = " (redirected to: " + con.getURL() + ")";
                }
                throw new IOException("Failed to download from " + src + extraMessage, e);
            }
        }

        protected URLConnection connect(DownloadJob job, URL src) throws IOException {
            return ProxyConfiguration.open(src);
        }

        public void install(DownloadJob job, File src, File dst) throws IOException {
            job.replace(dst, src);
        }

        public void upgrade(DownloadJob job, File src, File dst) throws IOException {
            job.replace(dst, src);
        }

        @Deprecated
        public String getConnectionCheckUrl() {
            return "http://www.google.com";
        }

        @Deprecated
        public String getUpdateCenterUrl() {
            return UPDATE_CENTER_URL;
        }

        @Deprecated
        public String getPluginRepositoryBaseUrl() {
            return "http://jenkins-ci.org/";
        }

        private void testConnection(URL url) throws IOException {
            try {
                URLConnection connection = (URLConnection) ProxyConfiguration.open(url);
                if (connection instanceof HttpURLConnection) {
                    int responseCode = ((HttpURLConnection) connection).getResponseCode();
                    if (HttpURLConnection.HTTP_OK != responseCode) {
                        throw new HttpRetryException("Invalid response code (" + responseCode + ") from URL: " + url, responseCode);
                    }
                } else {
                    try (InputStream is = connection.getInputStream()) {
                        IOUtils.copy(is, new NullOutputStream());
                    }
                }
            } catch (SSLHandshakeException e) {
                if (e.getMessage().contains("PKIX path building failed"))
                    throw new IOException("Failed to validate the SSL certificate of " + url, e);
            }
        }
    }

    @ExportedBean
    public abstract class UpdateCenterJob implements Runnable {

        @Exported
        public final int id = iota.incrementAndGet();

        public final UpdateSite site;

        private UUID correlationId = null;

        protected Throwable error;

        protected UpdateCenterJob(UpdateSite site) {
            this.site = site;
        }

        public Api getApi() {
            return new Api(this);
        }

        public UUID getCorrelationId() {
            return correlationId;
        }

        public void setCorrelationId(UUID correlationId) {
            if (this.correlationId != null) {
                throw new IllegalStateException("Illegal call to set the 'correlationId'. Already set.");
            }
            this.correlationId = correlationId;
        }

        @Deprecated
        public void schedule() {
            submit();
        }

        @Exported
        public String getType() {
            return getClass().getSimpleName();
        }

        public Future<UpdateCenterJob> submit() {
            LOGGER.fine("Scheduling " + this + " to installerService");
            jobs.add(this);
            return installerService.submit(this, this);
        }

        @Exported
        public String getErrorMessage() {
            return error != null ? error.getMessage() : null;
        }

        public Throwable getError() {
            return error;
        }
    }

    public class RestartJenkinsJob extends UpdateCenterJob {

        @Exported(inline = true)
        public volatile RestartJenkinsJobStatus status = new Pending();

        private String authentication;

        public synchronized boolean cancel() {
            if (status instanceof Pending) {
                status = new Canceled();
                return true;
            }
            return false;
        }

        public RestartJenkinsJob(UpdateSite site) {
            super(site);
            this.authentication = Jenkins.getAuthentication().getName();
        }

        public synchronized void run() {
            if (!(status instanceof Pending)) {
                return;
            }
            status = new Running();
            try {
                try (ACLContext acl = ACL.as(User.get(authentication, false, Collections.emptyMap()))) {
                    Jenkins.getInstance().safeRestart();
                }
            } catch (RestartNotSupportedException exception) {
                status = new Failure();
                error = exception;
            }
        }

        @ExportedBean
        public abstract class RestartJenkinsJobStatus {

            @Exported
            public final int id = iota.incrementAndGet();
        }

        public class Pending extends RestartJenkinsJobStatus {

            @Exported
            public String getType() {
                return getClass().getSimpleName();
            }
        }

        public class Running extends RestartJenkinsJobStatus {
        }

        public class Failure extends RestartJenkinsJobStatus {
        }

        public class Canceled extends RestartJenkinsJobStatus {
        }
    }

    public final class ConnectionCheckJob extends UpdateCenterJob {

        private final Vector<String> statuses = new Vector<String>();

        final Map<String, ConnectionStatus> connectionStates = new ConcurrentHashMap<>();

        public ConnectionCheckJob(UpdateSite site) {
            super(site);
            connectionStates.put(ConnectionStatus.INTERNET, ConnectionStatus.PRECHECK);
            connectionStates.put(ConnectionStatus.UPDATE_SITE, ConnectionStatus.PRECHECK);
        }

        public void run() {
            connectionStates.put(ConnectionStatus.INTERNET, ConnectionStatus.UNCHECKED);
            connectionStates.put(ConnectionStatus.UPDATE_SITE, ConnectionStatus.UNCHECKED);
            if (ID_UPLOAD.equals(site.getId())) {
                return;
            }
            LOGGER.fine("Doing a connectivity check");
            Future<?> internetCheck = null;
            try {
                final String connectionCheckUrl = site.getConnectionCheckUrl();
                if (connectionCheckUrl != null) {
                    connectionStates.put(ConnectionStatus.INTERNET, ConnectionStatus.CHECKING);
                    statuses.add(Messages.UpdateCenter_Status_CheckingInternet());
                    internetCheck = updateService.submit(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                config.checkConnection(ConnectionCheckJob.this, connectionCheckUrl);
                            } catch (Exception e) {
                                if (e.getMessage().contains("Connection timed out")) {
                                    connectionStates.put(ConnectionStatus.INTERNET, ConnectionStatus.FAILED);
                                    statuses.add(Messages.UpdateCenter_Status_ConnectionFailed(Functions.xmlEscape(connectionCheckUrl)));
                                    return;
                                }
                            }
                            connectionStates.put(ConnectionStatus.INTERNET, ConnectionStatus.OK);
                        }
                    });
                } else {
                    LOGGER.log(WARNING, "Update site ''{0}'' does not declare the connection check URL. " + "Skipping the network availability check.", site.getId());
                    connectionStates.put(ConnectionStatus.INTERNET, ConnectionStatus.SKIPPED);
                }
                connectionStates.put(ConnectionStatus.UPDATE_SITE, ConnectionStatus.CHECKING);
                statuses.add(Messages.UpdateCenter_Status_CheckingJavaNet());
                config.checkUpdateCenter(this, site.getUrl());
                connectionStates.put(ConnectionStatus.UPDATE_SITE, ConnectionStatus.OK);
                statuses.add(Messages.UpdateCenter_Status_Success());
            } catch (UnknownHostException e) {
                connectionStates.put(ConnectionStatus.UPDATE_SITE, ConnectionStatus.FAILED);
                statuses.add(Messages.UpdateCenter_Status_UnknownHostException(Functions.xmlEscape(e.getMessage())));
                addStatus(e);
                error = e;
            } catch (Exception e) {
                connectionStates.put(ConnectionStatus.UPDATE_SITE, ConnectionStatus.FAILED);
                addStatus(e);
                error = e;
            }
            if (internetCheck != null) {
                try {
                    internetCheck.get();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error completing internet connectivity check: " + e.getMessage(), e);
                }
            }
        }

        private void addStatus(Throwable e) {
            statuses.add("<pre>" + Functions.xmlEscape(Functions.printThrowable(e)) + "</pre>");
        }

        public String[] getStatuses() {
            synchronized (statuses) {
                return statuses.toArray(new String[statuses.size()]);
            }
        }
    }

    public class EnableJob extends InstallationJob {

        public EnableJob(UpdateSite site, Authentication auth, @Nonnull Plugin plugin, boolean dynamicLoad) {
            super(plugin, site, auth, dynamicLoad);
        }

        public Plugin getPlugin() {
            return plugin;
        }

        @Override
        public void run() {
            try {
                PluginWrapper installed = plugin.getInstalled();
                synchronized (installed) {
                    if (!installed.isEnabled()) {
                        try {
                            installed.enable();
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, "Failed to enable " + plugin.getDisplayName(), e);
                            error = e;
                            status = new Failure(e);
                        }
                        if (dynamicLoad) {
                            try {
                                pm.dynamicLoad(getDestination(), true);
                            } catch (Exception e) {
                                LOGGER.log(Level.SEVERE, "Failed to dynamically load " + plugin.getDisplayName(), e);
                                error = e;
                                requiresRestart = true;
                                status = new Failure(e);
                            }
                        } else {
                            requiresRestart = true;
                        }
                    }
                }
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, "An unexpected error occurred while attempting to enable " + plugin.getDisplayName(), e);
                error = e;
                requiresRestart = true;
                status = new Failure(e);
            }
            if (status instanceof Pending) {
                status = new Success();
            }
        }
    }

    public class NoOpJob extends EnableJob {

        public NoOpJob(UpdateSite site, Authentication auth, @Nonnull Plugin plugin) {
            super(site, auth, plugin, false);
        }

        @Override
        public void run() {
            status = new Success();
        }
    }

    @Restricted(NoExternalUse.class)
    interface WithComputedChecksums {

        String getComputedSHA1();

        String getComputedSHA256();

        String getComputedSHA512();
    }

    public abstract class DownloadJob extends UpdateCenterJob implements WithComputedChecksums {

        @Exported(inline = true)
        public volatile InstallationStatus status = new Pending();

        protected abstract URL getURL() throws MalformedURLException;

        protected abstract File getDestination();

        @Exported
        public abstract String getName();

        protected abstract void onSuccess();

        @CheckForNull
        public String getComputedSHA1() {
            return computedSHA1;
        }

        private String computedSHA1;

        @CheckForNull
        public String getComputedSHA256() {
            return computedSHA256;
        }

        private String computedSHA256;

        @CheckForNull
        public String getComputedSHA512() {
            return computedSHA512;
        }

        private String computedSHA512;

        private Authentication authentication;

        public Authentication getUser() {
            return this.authentication;
        }

        protected DownloadJob(UpdateSite site, Authentication authentication) {
            super(site);
            this.authentication = authentication;
        }

        public void run() {
            try {
                LOGGER.info("Starting the installation of " + getName() + " on behalf of " + getUser().getName());
                _run();
                LOGGER.info("Installation successful: " + getName());
                status = new Success();
                onSuccess();
            } catch (InstallationStatus e) {
                status = e;
                if (status.isSuccess())
                    onSuccess();
                requiresRestart |= status.requiresRestart();
            } catch (MissingDependencyException e) {
                LOGGER.log(Level.SEVERE, "Failed to install {0}: {1}", new Object[] { getName(), e.getMessage() });
                status = new Failure(e);
                error = e;
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, "Failed to install " + getName(), e);
                status = new Failure(e);
                error = e;
            }
        }

        protected void _run() throws IOException, InstallationStatus {
            URL src = getURL();
            config.preValidate(this, src);
            File dst = getDestination();
            File tmp = config.download(this, src);
            config.postValidate(this, tmp);
            config.install(this, tmp, dst);
        }

        protected void replace(File dst, File src) throws IOException {
            File bak = Util.changeExtension(dst, ".bak");
            bak.delete();
            dst.renameTo(bak);
            dst.delete();
            if (!src.renameTo(dst)) {
                throw new IOException("Failed to rename " + src + " to " + dst);
            }
        }

        @ExportedBean
        public abstract class InstallationStatus extends Throwable {

            public final int id = iota.incrementAndGet();

            @Exported
            public boolean isSuccess() {
                return false;
            }

            @Exported
            public final String getType() {
                return getClass().getSimpleName();
            }

            public boolean requiresRestart() {
                return false;
            }
        }

        public class Failure extends InstallationStatus {

            public final Throwable problem;

            public Failure(Throwable problem) {
                this.problem = problem;
            }

            public String getProblemStackTrace() {
                return Functions.printThrowable(problem);
            }
        }

        public class SuccessButRequiresRestart extends Success {

            private final Localizable message;

            public SuccessButRequiresRestart(Localizable message) {
                this.message = message;
            }

            public String getMessage() {
                return message.toString();
            }

            @Override
            public boolean requiresRestart() {
                return true;
            }
        }

        public class Success extends InstallationStatus {

            @Override
            public boolean isSuccess() {
                return true;
            }
        }

        public class Skipped extends InstallationStatus {

            @Override
            public boolean isSuccess() {
                return true;
            }
        }

        public class Pending extends InstallationStatus {
        }

        public class Installing extends InstallationStatus {

            public final int percentage;

            public Installing(int percentage) {
                this.percentage = percentage;
            }
        }
    }

    private static VerificationResult verifyChecksums(String expectedDigest, String actualDigest, boolean caseSensitive) {
        if (expectedDigest == null) {
            return VerificationResult.NOT_PROVIDED;
        }
        if (actualDigest == null) {
            return VerificationResult.NOT_COMPUTED;
        }
        if (caseSensitive ? expectedDigest.equals(actualDigest) : expectedDigest.equalsIgnoreCase(actualDigest)) {
            return VerificationResult.PASS;
        }
        return VerificationResult.FAIL;
    }

    private static enum VerificationResult {

        PASS, NOT_PROVIDED, NOT_COMPUTED, FAIL
    }

    private static void throwVerificationFailure(String expected, String actual, File file, String algorithm) throws IOException {
        throw new IOException("Downloaded file " + file.getAbsolutePath() + " does not match expected " + algorithm + ", expected '" + expected + "', actual '" + actual + "'");
    }

    @VisibleForTesting
    @Restricted(NoExternalUse.class)
    static void verifyChecksums(WithComputedChecksums job, UpdateSite.Entry entry, File file) throws IOException {
        VerificationResult result512 = verifyChecksums(entry.getSha512(), job.getComputedSHA512(), false);
        switch(result512) {
            case PASS:
                return;
            case FAIL:
                throwVerificationFailure(entry.getSha512(), job.getComputedSHA512(), file, "SHA-512");
            case NOT_COMPUTED:
                LOGGER.log(WARNING, "Attempt to verify a downloaded file (" + file.getName() + ") using SHA-512 failed since it could not be computed. Falling back to weaker algorithms. Update your JRE.");
                break;
            case NOT_PROVIDED:
                break;
        }
        VerificationResult result256 = verifyChecksums(entry.getSha256(), job.getComputedSHA256(), false);
        switch(result256) {
            case PASS:
                return;
            case FAIL:
                throwVerificationFailure(entry.getSha256(), job.getComputedSHA256(), file, "SHA-256");
            case NOT_COMPUTED:
            case NOT_PROVIDED:
                break;
        }
        if (result512 == VerificationResult.NOT_PROVIDED && result256 == VerificationResult.NOT_PROVIDED) {
            LOGGER.log(INFO, "Attempt to verify a downloaded file (" + file.getName() + ") using SHA-512 or SHA-256 failed since your configured update site does not provide either of those checksums. Falling back to SHA-1.");
        }
        VerificationResult result1 = verifyChecksums(entry.getSha1(), job.getComputedSHA1(), true);
        switch(result1) {
            case PASS:
                return;
            case FAIL:
                throwVerificationFailure(entry.getSha1(), job.getComputedSHA1(), file, "SHA-1");
            case NOT_COMPUTED:
                throw new IOException("Failed to compute SHA-1 of downloaded file, refusing installation");
            case NOT_PROVIDED:
                throw new IOException("Unable to confirm integrity of downloaded file, refusing installation");
        }
    }

    public class InstallationJob extends DownloadJob {

        @Exported
        public final Plugin plugin;

        protected final PluginManager pm = Jenkins.getInstance().getPluginManager();

        protected final boolean dynamicLoad;

        @Deprecated
        public InstallationJob(Plugin plugin, UpdateSite site, Authentication auth) {
            this(plugin, site, auth, false);
        }

        public InstallationJob(Plugin plugin, UpdateSite site, Authentication auth, boolean dynamicLoad) {
            super(site, auth);
            this.plugin = plugin;
            this.dynamicLoad = dynamicLoad;
        }

        protected URL getURL() throws MalformedURLException {
            return new URL(plugin.url);
        }

        protected File getDestination() {
            File baseDir = pm.rootDir;
            return new File(baseDir, plugin.name + ".jpi");
        }

        private File getLegacyDestination() {
            File baseDir = pm.rootDir;
            return new File(baseDir, plugin.name + ".hpi");
        }

        public String getName() {
            return plugin.getDisplayName();
        }

        @Override
        public void _run() throws IOException, InstallationStatus {
            if (wasInstalled()) {
                LOGGER.info("Skipping duplicate install of: " + plugin.getDisplayName() + "@" + plugin.version);
                return;
            }
            try {
                super._run();
                PluginWrapper pw = plugin.getInstalled();
                if (pw != null && pw.isBundled()) {
                    SecurityContext oldContext = ACL.impersonate(ACL.SYSTEM);
                    try {
                        pw.doPin();
                    } finally {
                        SecurityContextHolder.setContext(oldContext);
                    }
                }
                if (dynamicLoad) {
                    try {
                        pm.dynamicLoad(getDestination());
                    } catch (RestartRequiredException e) {
                        throw new SuccessButRequiresRestart(e.message);
                    } catch (Exception e) {
                        throw new IOException("Failed to dynamically deploy this plugin", e);
                    }
                } else {
                    throw new SuccessButRequiresRestart(Messages._UpdateCenter_DownloadButNotActivated());
                }
            } finally {
                synchronized (this) {
                    LOGGER.fine("Install complete for: " + plugin.getDisplayName() + "@" + plugin.version);
                    status = new Skipped();
                    notifyAll();
                }
            }
        }

        protected boolean wasInstalled() {
            synchronized (UpdateCenter.this) {
                for (UpdateCenterJob job : getJobs()) {
                    if (job == this) {
                        return false;
                    }
                    if (job instanceof InstallationJob) {
                        InstallationJob ij = (InstallationJob) job;
                        if (ij.plugin.equals(plugin) && ij.plugin.version.equals(plugin.version)) {
                            synchronized (ij) {
                                if (ij.status instanceof Installing || ij.status instanceof Pending) {
                                    try {
                                        LOGGER.fine("Waiting for other plugin install of: " + plugin.getDisplayName() + "@" + plugin.version);
                                        ij.wait();
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                if (ij.status instanceof Success) {
                                    return true;
                                }
                            }
                        }
                    }
                }
                return false;
            }
        }

        protected void onSuccess() {
            pm.pluginUploaded = true;
        }

        @Override
        public String toString() {
            return super.toString() + "[plugin=" + plugin.title + "]";
        }

        @Override
        protected void replace(File dst, File src) throws IOException {
            if (!site.getId().equals(ID_UPLOAD)) {
                verifyChecksums(this, plugin, src);
            }
            File bak = Util.changeExtension(dst, ".bak");
            bak.delete();
            final File legacy = getLegacyDestination();
            if (legacy.exists()) {
                if (!legacy.renameTo(bak)) {
                    legacy.delete();
                }
            }
            if (dst.exists()) {
                if (!dst.renameTo(bak)) {
                    dst.delete();
                }
            }
            if (!src.renameTo(dst)) {
                throw new IOException("Failed to rename " + src + " to " + dst);
            }
        }
    }

    public final class PluginDowngradeJob extends DownloadJob {

        public final Plugin plugin;

        private final PluginManager pm = Jenkins.getInstance().getPluginManager();

        public PluginDowngradeJob(Plugin plugin, UpdateSite site, Authentication auth) {
            super(site, auth);
            this.plugin = plugin;
        }

        protected URL getURL() throws MalformedURLException {
            return new URL(plugin.url);
        }

        protected File getDestination() {
            File baseDir = pm.rootDir;
            final File legacy = new File(baseDir, plugin.name + ".hpi");
            if (legacy.exists()) {
                return legacy;
            }
            return new File(baseDir, plugin.name + ".jpi");
        }

        protected File getBackup() {
            File baseDir = pm.rootDir;
            return new File(baseDir, plugin.name + ".bak");
        }

        public String getName() {
            return plugin.getDisplayName();
        }

        @Override
        public void run() {
            try {
                LOGGER.info("Starting the downgrade of " + getName() + " on behalf of " + getUser().getName());
                _run();
                LOGGER.info("Downgrade successful: " + getName());
                status = new Success();
                onSuccess();
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, "Failed to downgrade " + getName(), e);
                status = new Failure(e);
                error = e;
            }
        }

        @Override
        protected void _run() throws IOException {
            File dst = getDestination();
            File backup = getBackup();
            config.install(this, backup, dst);
        }

        @Override
        protected void replace(File dst, File backup) throws IOException {
            dst.delete();
            if (!backup.renameTo(dst)) {
                throw new IOException("Failed to rename " + backup + " to " + dst);
            }
        }

        protected void onSuccess() {
            pm.pluginUploaded = true;
        }

        @Override
        public String toString() {
            return super.toString() + "[plugin=" + plugin.title + "]";
        }
    }

    public final class HudsonUpgradeJob extends DownloadJob {

        public HudsonUpgradeJob(UpdateSite site, Authentication auth) {
            super(site, auth);
        }

        protected URL getURL() throws MalformedURLException {
            return new URL(site.getData().core.url);
        }

        protected File getDestination() {
            return Lifecycle.get().getHudsonWar();
        }

        public String getName() {
            return "jenkins.war";
        }

        protected void onSuccess() {
            status = new Success();
        }

        @Override
        protected void replace(File dst, File src) throws IOException {
            verifyChecksums(this, site.getData().core, src);
            Lifecycle.get().rewriteHudsonWar(src);
        }
    }

    public final class HudsonDowngradeJob extends DownloadJob {

        public HudsonDowngradeJob(UpdateSite site, Authentication auth) {
            super(site, auth);
        }

        protected URL getURL() throws MalformedURLException {
            return new URL(site.getData().core.url);
        }

        protected File getDestination() {
            return Lifecycle.get().getHudsonWar();
        }

        public String getName() {
            return "jenkins.war";
        }

        protected void onSuccess() {
            status = new Success();
        }

        @Override
        public void run() {
            try {
                LOGGER.info("Starting the downgrade of " + getName() + " on behalf of " + getUser().getName());
                _run();
                LOGGER.info("Downgrading successful: " + getName());
                status = new Success();
                onSuccess();
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, "Failed to downgrade " + getName(), e);
                status = new Failure(e);
                error = e;
            }
        }

        @Override
        protected void _run() throws IOException {
            File backup = new File(Lifecycle.get().getHudsonWar() + ".bak");
            File dst = getDestination();
            config.install(this, backup, dst);
        }

        @Override
        protected void replace(File dst, File src) throws IOException {
            Lifecycle.get().rewriteHudsonWar(src);
        }
    }

    public static final class PluginEntry implements Comparable<PluginEntry> {

        public Plugin plugin;

        public String category;

        private PluginEntry(Plugin p, String c) {
            plugin = p;
            category = c;
        }

        public int compareTo(PluginEntry o) {
            int r = category.compareTo(o.category);
            if (r == 0)
                r = plugin.name.compareToIgnoreCase(o.plugin.name);
            if (r == 0)
                r = new VersionNumber(plugin.version).compareTo(new VersionNumber(o.plugin.version));
            return r;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PluginEntry that = (PluginEntry) o;
            if (!category.equals(that.category)) {
                return false;
            }
            if (!plugin.name.equals(that.plugin.name)) {
                return false;
            }
            return plugin.version.equals(that.plugin.version);
        }

        @Override
        public int hashCode() {
            int result = category.hashCode();
            result = 31 * result + plugin.name.hashCode();
            result = 31 * result + plugin.version.hashCode();
            return result;
        }
    }

    @Extension
    public static class PageDecoratorImpl extends PageDecorator {
    }

    @Initializer(after = PLUGINS_STARTED, fatal = false)
    public static void init(Jenkins h) throws IOException {
        h.getUpdateCenter().load();
    }

    @Restricted(NoExternalUse.class)
    public static void updateDefaultSite() {
        final UpdateSite site = Jenkins.getInstance().getUpdateCenter().getSite(UpdateCenter.ID_DEFAULT);
        if (site == null) {
            LOGGER.log(Level.SEVERE, "Upgrading Jenkins. Cannot retrieve the default Update Site ''{0}''. " + "Plugin installation may fail.", UpdateCenter.ID_DEFAULT);
            return;
        }
        try {
            site.updateDirectlyNow(true);
        } catch (Exception e) {
            LOGGER.log(WARNING, "Upgrading Jenkins. Failed to update the default Update Site '" + UpdateCenter.ID_DEFAULT + "'. Plugin upgrades may fail.", e);
        }
    }

    @Override
    @Restricted(NoExternalUse.class)
    public Object getTarget() {
        if (!SKIP_PERMISSION_CHECK) {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
        }
        return this;
    }

    @Restricted(NoExternalUse.class)
    public static boolean SKIP_PERMISSION_CHECK = Boolean.getBoolean(UpdateCenter.class.getName() + ".skipPermissionCheck");

    private static final AtomicInteger iota = new AtomicInteger();

    @Deprecated
    public static boolean neverUpdate = SystemProperties.getBoolean(UpdateCenter.class.getName() + ".never");

    public static final XStream2 XSTREAM = new XStream2();

    static {
        XSTREAM.alias("site", UpdateSite.class);
        XSTREAM.alias("sites", PersistedList.class);
    }
}
