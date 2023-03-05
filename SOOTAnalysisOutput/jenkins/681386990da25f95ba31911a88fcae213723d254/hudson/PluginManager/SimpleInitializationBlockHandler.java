package hudson;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.PluginWrapper.Dependency;
import hudson.init.InitMilestone;
import hudson.init.InitStrategy;
import hudson.init.InitializerFinder;
import hudson.model.AbstractItem;
import hudson.model.AbstractModelObject;
import hudson.model.AdministrativeMonitor;
import hudson.model.Api;
import hudson.model.Descriptor;
import hudson.model.DownloadService;
import hudson.model.Failure;
import hudson.model.ItemGroupMixIn;
import hudson.model.UpdateCenter;
import hudson.model.UpdateCenter.DownloadJob;
import hudson.model.UpdateCenter.InstallationJob;
import hudson.model.UpdateSite;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.security.Permission;
import hudson.security.PermissionScope;
import hudson.util.CyclicGraphDetector;
import hudson.util.CyclicGraphDetector.CycleDetectedException;
import hudson.util.FormValidation;
import hudson.util.PersistedList;
import hudson.util.Retrier;
import hudson.util.Service;
import hudson.util.VersionNumber;
import hudson.util.XStream2;
import jenkins.ClassLoaderReflectionToolkit;
import jenkins.ExtensionRefreshException;
import jenkins.InitReactorRunner;
import jenkins.MissingDependencyException;
import jenkins.RestartRequiredException;
import jenkins.YesNoMaybe;
import jenkins.install.InstallState;
import jenkins.install.InstallUtil;
import jenkins.model.Jenkins;
import jenkins.plugins.DetachedPluginsUtil;
import jenkins.security.CustomClassFilter;
import jenkins.util.SystemProperties;
import jenkins.util.io.OnMaster;
import jenkins.util.xml.RestrictiveEntityResolver;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.acegisecurity.Authentication;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.jenkinsci.Symbol;
import org.jenkinsci.bytecode.Transformer;
import org.jvnet.hudson.reactor.Executable;
import org.jvnet.hudson.reactor.Reactor;
import org.jvnet.hudson.reactor.ReactorException;
import org.jvnet.hudson.reactor.TaskBuilder;
import org.jvnet.hudson.reactor.TaskGraphBuilder;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerOverridable;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import static hudson.init.InitMilestone.*;
import static java.util.logging.Level.*;

@ExportedBean
public abstract class PluginManager extends AbstractModelObject implements OnMaster, StaplerOverridable, StaplerProxy {

    public static final String CUSTOM_PLUGIN_MANAGER = PluginManager.class.getName() + ".className";

    private static final Logger LOGGER = Logger.getLogger(PluginManager.class.getName());

    static int CHECK_UPDATE_SLEEP_TIME_MILLIS;

    static int CHECK_UPDATE_ATTEMPTS;

    static {
        try {
            CHECK_UPDATE_SLEEP_TIME_MILLIS = SystemProperties.getInteger(PluginManager.class.getName() + ".checkUpdateSleepTimeMillis", 1000);
            CHECK_UPDATE_ATTEMPTS = SystemProperties.getInteger(PluginManager.class.getName() + ".checkUpdateAttempts", 1);
        } catch (Exception e) {
            LOGGER.warning(String.format("There was an error initializing the PluginManager. Exception: %s", e));
        } finally {
            CHECK_UPDATE_ATTEMPTS = CHECK_UPDATE_ATTEMPTS > 0 ? CHECK_UPDATE_ATTEMPTS : 1;
            CHECK_UPDATE_SLEEP_TIME_MILLIS = CHECK_UPDATE_SLEEP_TIME_MILLIS > 0 ? CHECK_UPDATE_SLEEP_TIME_MILLIS : 1000;
        }
    }

    private enum PMConstructor {

        JENKINS {

            @Override
            @NonNull
            PluginManager doCreate(@NonNull Class<? extends PluginManager> klass, @NonNull Jenkins jenkins) throws ReflectiveOperationException {
                return klass.getConstructor(Jenkins.class).newInstance(jenkins);
            }
        }, SC_FILE {

            @Override
            @NonNull
            PluginManager doCreate(@NonNull Class<? extends PluginManager> klass, @NonNull Jenkins jenkins) throws ReflectiveOperationException {
                return klass.getConstructor(ServletContext.class, File.class).newInstance(jenkins.servletContext, jenkins.getRootDir());
            }
        }, FILE {

            @Override
            @NonNull
            PluginManager doCreate(@NonNull Class<? extends PluginManager> klass, @NonNull Jenkins jenkins) throws ReflectiveOperationException {
                return klass.getConstructor(File.class).newInstance(jenkins.getRootDir());
            }
        };

        @CheckForNull
        final PluginManager create(@NonNull Class<? extends PluginManager> klass, @NonNull Jenkins jenkins) throws ReflectiveOperationException {
            try {
                return doCreate(klass, jenkins);
            } catch (NoSuchMethodException e) {
                return null;
            }
        }

        @NonNull
        abstract PluginManager doCreate(@NonNull Class<? extends PluginManager> klass, @NonNull Jenkins jenkins) throws ReflectiveOperationException;
    }

    @NonNull
    public static PluginManager createDefault(@NonNull Jenkins jenkins) {
        String pmClassName = SystemProperties.getString(CUSTOM_PLUGIN_MANAGER);
        if (!StringUtils.isBlank(pmClassName)) {
            LOGGER.log(FINE, String.format("Use of custom plugin manager [%s] requested.", pmClassName));
            try {
                final Class<? extends PluginManager> klass = Class.forName(pmClassName).asSubclass(PluginManager.class);
                for (PMConstructor c : PMConstructor.values()) {
                    PluginManager pm = c.create(klass, jenkins);
                    if (pm != null) {
                        return pm;
                    }
                }
                LOGGER.log(WARNING, String.format("Provided custom plugin manager [%s] does not provide any of the suitable constructors. Using default.", pmClassName));
            } catch (NullPointerException e) {
                LOGGER.log(WARNING, String.format("Unable to instantiate custom plugin manager [%s]. Using default.", pmClassName));
            } catch (ClassCastException e) {
                LOGGER.log(WARNING, String.format("Provided class [%s] does not extend PluginManager. Using default.", pmClassName));
            } catch (Exception e) {
                LOGGER.log(WARNING, String.format("Unable to instantiate custom plugin manager [%s]. Using default.", pmClassName), e);
            }
        }
        return new LocalPluginManager(jenkins);
    }

    protected final List<PluginWrapper> plugins = new CopyOnWriteArrayList<>();

    protected final List<PluginWrapper> activePlugins = new CopyOnWriteArrayList<PluginWrapper>();

    protected final List<FailedPlugin> failedPlugins = new ArrayList<FailedPlugin>();

    public final File rootDir;

    private String lastErrorCheckUpdateCenters = null;

    @CheckForNull
    private final File workDir;

    @Deprecated
    public final ServletContext context;

    public final ClassLoader uberClassLoader = new UberClassLoader();

    private final Transformer compatibilityTransformer = new Transformer();

    public volatile boolean pluginUploaded = false;

    private boolean pluginListed = false;

    private final PluginStrategy strategy;

    public PluginManager(ServletContext context, File rootDir) {
        this.context = context;
        this.rootDir = rootDir;
        if (!rootDir.exists())
            rootDir.mkdirs();
        String workDir = SystemProperties.getString(PluginManager.class.getName() + ".workDir");
        this.workDir = StringUtils.isBlank(workDir) ? null : new File(workDir);
        strategy = createPluginStrategy();
        try {
            compatibilityTransformer.loadRules(getClass().getClassLoader());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load compatibility rewrite rules", e);
        }
    }

    public Transformer getCompatibilityTransformer() {
        return compatibilityTransformer;
    }

    public Api getApi() {
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
        return new Api(this);
    }

    @CheckForNull
    public File getWorkDir() {
        return workDir;
    }

    @Override
    public Collection<PluginManagerStaplerOverride> getOverrides() {
        return PluginManagerStaplerOverride.all();
    }

    public TaskBuilder initTasks(final InitStrategy initStrategy) {
        TaskBuilder builder;
        if (!pluginListed) {
            builder = new TaskGraphBuilder() {

                List<File> archives;

                Collection<String> bundledPlugins;

                {
                    Handle loadBundledPlugins = add("Loading bundled plugins", new Executable() {

                        public void run(Reactor session) throws Exception {
                            bundledPlugins = loadBundledPlugins();
                        }
                    });
                    Handle listUpPlugins = requires(loadBundledPlugins).add("Listing up plugins", new Executable() {

                        public void run(Reactor session) throws Exception {
                            archives = initStrategy.listPluginArchives(PluginManager.this);
                        }
                    });
                    requires(listUpPlugins).attains(PLUGINS_LISTED).add("Preparing plugins", new Executable() {

                        public void run(Reactor session) throws Exception {
                            TaskGraphBuilder g = new TaskGraphBuilder();
                            final Map<String, File> inspectedShortNames = new HashMap<String, File>();
                            for (final File arc : archives) {
                                g.followedBy().notFatal().attains(PLUGINS_LISTED).add("Inspecting plugin " + arc, new Executable() {

                                    public void run(Reactor session1) throws Exception {
                                        try {
                                            PluginWrapper p = strategy.createPluginWrapper(arc);
                                            if (isDuplicate(p))
                                                return;
                                            p.isBundled = containsHpiJpi(bundledPlugins, arc.getName());
                                            plugins.add(p);
                                        } catch (IOException e) {
                                            failedPlugins.add(new FailedPlugin(arc.getName(), e));
                                            throw e;
                                        }
                                    }

                                    private boolean isDuplicate(PluginWrapper p) {
                                        String shortName = p.getShortName();
                                        if (inspectedShortNames.containsKey(shortName)) {
                                            LOGGER.info("Ignoring " + arc + " because " + inspectedShortNames.get(shortName) + " is already loaded");
                                            return true;
                                        }
                                        inspectedShortNames.put(shortName, arc);
                                        return false;
                                    }
                                });
                            }
                            g.followedBy().attains(PLUGINS_LISTED).add("Checking cyclic dependencies", new Executable() {

                                public void run(Reactor reactor) throws Exception {
                                    try {
                                        CyclicGraphDetector<PluginWrapper> cgd = new CyclicGraphDetector<PluginWrapper>() {

                                            @Override
                                            protected List<PluginWrapper> getEdges(PluginWrapper p) {
                                                List<PluginWrapper> next = new ArrayList<PluginWrapper>();
                                                addTo(p.getDependencies(), next);
                                                addTo(p.getOptionalDependencies(), next);
                                                return next;
                                            }

                                            private void addTo(List<Dependency> dependencies, List<PluginWrapper> r) {
                                                for (Dependency d : dependencies) {
                                                    PluginWrapper p = getPlugin(d.shortName);
                                                    if (p != null)
                                                        r.add(p);
                                                }
                                            }

                                            @Override
                                            protected void reactOnCycle(PluginWrapper q, List<PluginWrapper> cycle) throws hudson.util.CyclicGraphDetector.CycleDetectedException {
                                                LOGGER.log(Level.SEVERE, "found cycle in plugin dependencies: (root=" + q + ", deactivating all involved) " + Util.join(cycle, " -> "));
                                                for (PluginWrapper pluginWrapper : cycle) {
                                                    pluginWrapper.setHasCycleDependency(true);
                                                    failedPlugins.add(new FailedPlugin(pluginWrapper.getShortName(), new CycleDetectedException(cycle)));
                                                }
                                            }
                                        };
                                        cgd.run(getPlugins());
                                        for (PluginWrapper p : cgd.getSorted()) {
                                            if (p.isActive())
                                                activePlugins.add(p);
                                        }
                                    } catch (CycleDetectedException e) {
                                        stop();
                                        throw e;
                                    }
                                }
                            });
                            session.addAll(g.discoverTasks(session));
                            pluginListed = true;
                        }
                    });
                }
            };
        } else {
            builder = TaskBuilder.EMPTY_BUILDER;
        }
        final InitializerFinder initializerFinder = new InitializerFinder(uberClassLoader);
        return TaskBuilder.union(initializerFinder, builder, new TaskGraphBuilder() {

            {
                requires(PLUGINS_LISTED).attains(PLUGINS_PREPARED).add("Loading plugins", new Executable() {

                    public void run(Reactor session) throws Exception {
                        Jenkins.getInstance().lookup.set(PluginInstanceStore.class, new PluginInstanceStore());
                        TaskGraphBuilder g = new TaskGraphBuilder();
                        for (final PluginWrapper p : activePlugins.toArray(new PluginWrapper[activePlugins.size()])) {
                            g.followedBy().notFatal().attains(PLUGINS_PREPARED).add(String.format("Loading plugin %s v%s (%s)", p.getLongName(), p.getVersion(), p.getShortName()), new Executable() {

                                public void run(Reactor session) throws Exception {
                                    try {
                                        p.resolvePluginDependencies();
                                        strategy.load(p);
                                    } catch (MissingDependencyException e) {
                                        failedPlugins.add(new FailedPlugin(p.getShortName(), e));
                                        activePlugins.remove(p);
                                        plugins.remove(p);
                                        LOGGER.log(Level.SEVERE, "Failed to install {0}: {1}", new Object[] { p.getShortName(), e.getMessage() });
                                        return;
                                    } catch (IOException e) {
                                        failedPlugins.add(new FailedPlugin(p.getShortName(), e));
                                        activePlugins.remove(p);
                                        plugins.remove(p);
                                        throw e;
                                    }
                                }
                            });
                        }
                        for (final PluginWrapper p : activePlugins.toArray(new PluginWrapper[activePlugins.size()])) {
                            g.followedBy().notFatal().attains(PLUGINS_STARTED).add("Initializing plugin " + p.getShortName(), new Executable() {

                                public void run(Reactor session) throws Exception {
                                    if (!activePlugins.contains(p)) {
                                        return;
                                    }
                                    try {
                                        p.getPlugin().postInitialize();
                                    } catch (Exception e) {
                                        failedPlugins.add(new FailedPlugin(p.getShortName(), e));
                                        activePlugins.remove(p);
                                        plugins.remove(p);
                                        throw e;
                                    }
                                }
                            });
                        }
                        g.followedBy().attains(PLUGINS_STARTED).add("Discovering plugin initialization tasks", new Executable() {

                            public void run(Reactor reactor) throws Exception {
                                reactor.addAll(initializerFinder.discoverTasks(reactor));
                            }
                        });
                        session.addAll(g.discoverTasks(session));
                    }
                });
                requires(PLUGINS_PREPARED).attains(COMPLETED).add("Resolving Dependant Plugins Graph", new Executable() {

                    @Override
                    public void run(Reactor reactor) throws Exception {
                        resolveDependantPlugins();
                    }
                });
            }
        });
    }

    @Nonnull
    protected Set<String> loadPluginsFromWar(@Nonnull String fromPath) {
        return loadPluginsFromWar(fromPath, null);
    }

    @SuppressFBWarnings(value = "DMI_COLLECTION_OF_URLS", justification = "Plugin loading happens only once on Jenkins startup")
    @Nonnull
    protected Set<String> loadPluginsFromWar(@Nonnull String fromPath, @CheckForNull FilenameFilter filter) {
        Set<String> names = new HashSet();
        ServletContext context = Jenkins.getActiveInstance().servletContext;
        Set<String> plugins = Util.fixNull((Set<String>) context.getResourcePaths(fromPath));
        Set<URL> copiedPlugins = new HashSet<>();
        Set<URL> dependencies = new HashSet<>();
        for (String pluginPath : plugins) {
            String fileName = pluginPath.substring(pluginPath.lastIndexOf('/') + 1);
            if (fileName.length() == 0) {
                continue;
            }
            try {
                URL url = context.getResource(pluginPath);
                if (filter != null && url != null) {
                    if (!filter.accept(new File(url.getFile()).getParentFile(), fileName)) {
                        continue;
                    }
                }
                names.add(fileName);
                copyBundledPlugin(url, fileName);
                copiedPlugins.add(url);
                try {
                    addDependencies(url, fromPath, dependencies);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to resolve dependencies for the bundled plugin " + fileName, e);
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to extract the bundled plugin " + fileName, e);
            }
        }
        for (URL dependency : dependencies) {
            if (copiedPlugins.contains(dependency)) {
                continue;
            }
            String fileName = new File(dependency.getFile()).getName();
            try {
                names.add(fileName);
                copyBundledPlugin(dependency, fileName);
                copiedPlugins.add(dependency);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to extract the bundled dependency plugin " + fileName, e);
            }
        }
        return names;
    }

    @SuppressFBWarnings(value = "DMI_COLLECTION_OF_URLS", justification = "Plugin loading happens only once on Jenkins startup")
    protected static void addDependencies(URL hpiResUrl, String fromPath, Set<URL> dependencySet) throws URISyntaxException, MalformedURLException {
        if (dependencySet.contains(hpiResUrl)) {
            return;
        }
        Manifest manifest = parsePluginManifest(hpiResUrl);
        String dependencySpec = manifest.getMainAttributes().getValue("Plugin-Dependencies");
        if (dependencySpec != null) {
            String[] dependencyTokens = dependencySpec.split(",");
            ServletContext context = Jenkins.getActiveInstance().servletContext;
            for (String dependencyToken : dependencyTokens) {
                if (dependencyToken.endsWith(";resolution:=optional")) {
                    continue;
                }
                String[] artifactIdVersionPair = dependencyToken.split(":");
                String artifactId = artifactIdVersionPair[0];
                VersionNumber dependencyVersion = new VersionNumber(artifactIdVersionPair[1]);
                PluginManager manager = Jenkins.getActiveInstance().getPluginManager();
                VersionNumber installedVersion = manager.getPluginVersion(manager.rootDir, artifactId);
                if (installedVersion != null && !installedVersion.isOlderThan(dependencyVersion)) {
                    continue;
                }
                URL dependencyURL = context.getResource(fromPath + "/" + artifactId + ".hpi");
                if (dependencyURL == null) {
                    dependencyURL = context.getResource(fromPath + "/" + artifactId + ".jpi");
                }
                if (dependencyURL != null) {
                    addDependencies(dependencyURL, fromPath, dependencySet);
                    dependencySet.add(dependencyURL);
                }
            }
        }
    }

    protected void loadDetachedPlugins() {
        VersionNumber lastExecVersion = new VersionNumber(InstallUtil.getLastExecVersion());
        if (lastExecVersion.isNewerThan(InstallUtil.NEW_INSTALL_VERSION) && lastExecVersion.isOlderThan(Jenkins.getVersion())) {
            LOGGER.log(INFO, "Upgrading Jenkins. The last running version was {0}. This Jenkins is version {1}.", new Object[] { lastExecVersion, Jenkins.VERSION });
            final List<DetachedPluginsUtil.DetachedPlugin> detachedPlugins = DetachedPluginsUtil.getDetachedPlugins(lastExecVersion);
            Set<String> loadedDetached = loadPluginsFromWar("/WEB-INF/detached-plugins", new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    name = normalisePluginName(name);
                    if (DetachedPluginsUtil.isDetachedPlugin(name)) {
                        VersionNumber installedVersion = getPluginVersion(rootDir, name);
                        VersionNumber bundledVersion = getPluginVersion(dir, name);
                        if (installedVersion != null && bundledVersion != null) {
                            return installedVersion.isOlderThan(bundledVersion);
                        }
                    }
                    for (DetachedPluginsUtil.DetachedPlugin detachedPlugin : detachedPlugins) {
                        if (detachedPlugin.getShortName().equals(name)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
            LOGGER.log(INFO, "Upgraded Jenkins from version {0} to version {1}. Loaded detached plugins (and dependencies): {2}", new Object[] { lastExecVersion, Jenkins.VERSION, loadedDetached });
            InstallUtil.saveLastExecVersion();
        } else {
            final Set<DetachedPluginsUtil.DetachedPlugin> forceUpgrade = new HashSet<>();
            for (DetachedPluginsUtil.DetachedPlugin p : DetachedPluginsUtil.getDetachedPlugins()) {
                VersionNumber installedVersion = getPluginVersion(rootDir, p.getShortName());
                VersionNumber requiredVersion = p.getRequiredVersion();
                if (installedVersion != null && installedVersion.isOlderThan(requiredVersion)) {
                    LOGGER.log(Level.WARNING, "Detached plugin {0} found at version {1}, required minimum version is {2}", new Object[] { p.getShortName(), installedVersion, requiredVersion });
                    forceUpgrade.add(p);
                }
            }
            if (!forceUpgrade.isEmpty()) {
                Set<String> loadedDetached = loadPluginsFromWar("/WEB-INF/detached-plugins", new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String name) {
                        name = normalisePluginName(name);
                        for (DetachedPluginsUtil.DetachedPlugin detachedPlugin : forceUpgrade) {
                            if (detachedPlugin.getShortName().equals(name)) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
                LOGGER.log(INFO, "Upgraded detached plugins (and dependencies): {0}", new Object[] { loadedDetached });
            }
        }
    }

    private String normalisePluginName(@Nonnull String name) {
        return name.replace(".jpi", "").replace(".hpi", "");
    }

    @CheckForNull
    private VersionNumber getPluginVersion(@Nonnull File dir, @Nonnull String pluginId) {
        VersionNumber version = getPluginVersion(new File(dir, pluginId + ".jpi"));
        if (version == null) {
            version = getPluginVersion(new File(dir, pluginId + ".hpi"));
        }
        return version;
    }

    @CheckForNull
    private VersionNumber getPluginVersion(@Nonnull File pluginFile) {
        if (!pluginFile.exists()) {
            return null;
        }
        try {
            return getPluginVersion(pluginFile.toURI().toURL());
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @CheckForNull
    private VersionNumber getPluginVersion(@Nonnull URL pluginURL) {
        Manifest manifest = parsePluginManifest(pluginURL);
        if (manifest == null) {
            return null;
        }
        String versionSpec = manifest.getMainAttributes().getValue("Plugin-Version");
        return new VersionNumber(versionSpec);
    }

    private boolean containsHpiJpi(Collection<String> bundledPlugins, String name) {
        return bundledPlugins.contains(name.replaceAll("\\.hpi", ".jpi")) || bundledPlugins.contains(name.replaceAll("\\.jpi", ".hpi"));
    }

    @Deprecated
    @CheckForNull
    public Manifest getBundledPluginManifest(String shortName) {
        return null;
    }

    public void dynamicLoad(File arc) throws IOException, InterruptedException, RestartRequiredException {
        dynamicLoad(arc, false);
    }

    @Restricted(NoExternalUse.class)
    public void dynamicLoad(File arc, boolean removeExisting) throws IOException, InterruptedException, RestartRequiredException {
        try (ACLContext context = ACL.as(ACL.SYSTEM)) {
            LOGGER.info("Attempting to dynamic load " + arc);
            PluginWrapper p = null;
            String sn;
            try {
                sn = strategy.getShortName(arc);
            } catch (AbstractMethodError x) {
                LOGGER.log(WARNING, "JENKINS-12753 fix not active: {0}", x.getMessage());
                p = strategy.createPluginWrapper(arc);
                sn = p.getShortName();
            }
            PluginWrapper pw = getPlugin(sn);
            if (pw != null) {
                if (removeExisting) {
                    for (Iterator<PluginWrapper> i = plugins.iterator(); i.hasNext(); ) {
                        pw = i.next();
                        if (sn.equals(pw.getShortName())) {
                            i.remove();
                            pw = null;
                            break;
                        }
                    }
                } else {
                    throw new RestartRequiredException(Messages._PluginManager_PluginIsAlreadyInstalled_RestartRequired(sn));
                }
            }
            if (p == null) {
                p = strategy.createPluginWrapper(arc);
            }
            if (p.supportsDynamicLoad() == YesNoMaybe.NO)
                throw new RestartRequiredException(Messages._PluginManager_PluginDoesntSupportDynamicLoad_RestartRequired(sn));
            plugins.add(p);
            if (p.isActive())
                activePlugins.add(p);
            synchronized (((UberClassLoader) uberClassLoader).loaded) {
                ((UberClassLoader) uberClassLoader).loaded.clear();
            }
            CustomClassFilter.Contributed.load();
            try {
                p.resolvePluginDependencies();
                strategy.load(p);
                Jenkins.getInstance().refreshExtensions();
                p.getPlugin().postInitialize();
            } catch (Exception e) {
                failedPlugins.add(new FailedPlugin(sn, e));
                activePlugins.remove(p);
                plugins.remove(p);
                throw new IOException("Failed to install " + sn + " plugin", e);
            }
            Reactor r = new Reactor(InitMilestone.ordering());
            final ClassLoader loader = p.classLoader;
            r.addAll(new InitializerFinder(loader) {

                @Override
                protected boolean filter(Method e) {
                    return e.getDeclaringClass().getClassLoader() != loader || super.filter(e);
                }
            }.discoverTasks(r));
            try {
                new InitReactorRunner().run(r);
            } catch (ReactorException e) {
                throw new IOException("Failed to initialize " + sn + " plugin", e);
            }
            for (PluginWrapper depender : plugins) {
                if (depender.equals(p)) {
                    continue;
                }
                for (Dependency d : depender.getOptionalDependencies()) {
                    if (d.shortName.equals(p.getShortName())) {
                        getPluginStrategy().updateDependency(depender, p);
                        break;
                    }
                }
            }
            resolveDependantPlugins();
            try {
                Jenkins.get().refreshExtensions();
            } catch (ExtensionRefreshException e) {
                throw new IOException("Failed to refresh extensions after installing " + sn + " plugin", e);
            }
            LOGGER.info("Plugin " + p.getShortName() + ":" + p.getVersion() + " dynamically installed");
        }
    }

    @Restricted(NoExternalUse.class)
    public synchronized void resolveDependantPlugins() {
        for (PluginWrapper plugin : plugins) {
            Set<String> optionalDependants = new HashSet<>();
            Set<String> dependants = new HashSet<>();
            for (PluginWrapper possibleDependant : plugins) {
                if (possibleDependant.getShortName().equals(plugin.getShortName())) {
                    continue;
                }
                if (possibleDependant.isDeleted()) {
                    continue;
                }
                List<Dependency> dependencies = possibleDependant.getDependencies();
                for (Dependency dependency : dependencies) {
                    if (dependency.shortName.equals(plugin.getShortName())) {
                        dependants.add(possibleDependant.getShortName());
                        if (dependency.optional) {
                            optionalDependants.add(possibleDependant.getShortName());
                        }
                        break;
                    }
                }
            }
            plugin.setDependants(dependants);
            plugin.setOptionalDependants(optionalDependants);
        }
    }

    protected abstract Collection<String> loadBundledPlugins() throws Exception;

    protected void copyBundledPlugin(URL src, String fileName) throws IOException {
        LOGGER.log(FINE, "Copying {0}", src);
        fileName = fileName.replace(".hpi", ".jpi");
        String legacyName = fileName.replace(".jpi", ".hpi");
        long lastModified = getModificationDate(src);
        File file = new File(rootDir, fileName);
        rename(new File(rootDir, legacyName), file);
        if (!file.exists() || file.lastModified() != lastModified) {
            FileUtils.copyURLToFile(src, file);
            file.setLastModified(getModificationDate(src));
        }
    }

    @CheckForNull
    static Manifest parsePluginManifest(URL bundledJpi) {
        try {
            URLClassLoader cl = new URLClassLoader(new URL[] { bundledJpi });
            InputStream in = null;
            try {
                URL res = cl.findResource(PluginWrapper.MANIFEST_FILENAME);
                if (res != null) {
                    in = getBundledJpiManifestStream(res);
                    return new Manifest(in);
                }
            } finally {
                Util.closeAndLogFailures(in, LOGGER, PluginWrapper.MANIFEST_FILENAME, bundledJpi.toString());
                if (cl instanceof Closeable)
                    ((Closeable) cl).close();
            }
        } catch (IOException e) {
            LOGGER.log(WARNING, "Failed to parse manifest of " + bundledJpi, e);
        }
        return null;
    }

    @Nonnull
    static InputStream getBundledJpiManifestStream(@Nonnull URL url) throws IOException {
        URLConnection uc = url.openConnection();
        InputStream in = null;
        if (uc instanceof JarURLConnection) {
            final JarURLConnection jarURLConnection = (JarURLConnection) uc;
            final String entryName = jarURLConnection.getEntryName();
            try (final JarFile jarFile = jarURLConnection.getJarFile()) {
                final JarEntry entry = (entryName != null && jarFile != null) ? jarFile.getJarEntry(entryName) : null;
                if (entry != null && jarFile != null) {
                    try (InputStream i = jarFile.getInputStream(entry)) {
                        byte[] manifestBytes = IOUtils.toByteArray(i);
                        in = new ByteArrayInputStream(manifestBytes);
                    }
                } else {
                    LOGGER.log(Level.WARNING, "Failed to locate the JAR file for {0}" + "The default URLConnection stream access will be used, file descriptor may be leaked.", url);
                }
            }
        }
        if (in == null) {
            in = url.openStream();
        }
        return in;
    }

    @Nonnull
    static long getModificationDate(@Nonnull URL url) throws IOException {
        URLConnection uc = url.openConnection();
        if (uc instanceof JarURLConnection) {
            final JarURLConnection connection = (JarURLConnection) uc;
            final URL jarURL = connection.getJarFileURL();
            if (jarURL.getProtocol().equals("file")) {
                uc = null;
                String file = jarURL.getFile();
                return new File(file).lastModified();
            } else {
                if (connection.getEntryName() != null) {
                    LOGGER.log(WARNING, "Accessing modification date of {0} file, which is an entry in JAR file. " + "The access protocol is not file:, falling back to the default logic (risk of file descriptor leak).", url);
                }
            }
        }
        return uc.getLastModified();
    }

    private void rename(File legacyFile, File newFile) throws IOException {
        if (!legacyFile.exists())
            return;
        if (newFile.exists()) {
            Util.deleteFile(newFile);
        }
        if (!legacyFile.renameTo(newFile)) {
            LOGGER.warning("Failed to rename " + legacyFile + " to " + newFile);
        }
    }

    protected PluginStrategy createPluginStrategy() {
        String strategyName = SystemProperties.getString(PluginStrategy.class.getName());
        if (strategyName != null) {
            try {
                Class<?> klazz = getClass().getClassLoader().loadClass(strategyName);
                Object strategy = klazz.getConstructor(PluginManager.class).newInstance(this);
                if (strategy instanceof PluginStrategy) {
                    LOGGER.info("Plugin strategy: " + strategyName);
                    return (PluginStrategy) strategy;
                } else {
                    LOGGER.warning("Plugin strategy (" + strategyName + ") is not an instance of hudson.PluginStrategy");
                }
            } catch (ClassNotFoundException e) {
                LOGGER.warning("Plugin strategy class not found: " + strategyName);
            } catch (Exception e) {
                LOGGER.log(WARNING, "Could not instantiate plugin strategy: " + strategyName + ". Falling back to ClassicPluginStrategy", e);
            }
            LOGGER.info("Falling back to ClassicPluginStrategy");
        }
        return new ClassicPluginStrategy(this);
    }

    public PluginStrategy getPluginStrategy() {
        return strategy;
    }

    public boolean isPluginUploaded() {
        return pluginUploaded;
    }

    @Exported
    public List<PluginWrapper> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    public List<FailedPlugin> getFailedPlugins() {
        return failedPlugins;
    }

    @CheckForNull
    public PluginWrapper getPlugin(String shortName) {
        for (PluginWrapper p : getPlugins()) {
            if (p.getShortName().equals(shortName))
                return p;
        }
        return null;
    }

    @CheckForNull
    public PluginWrapper getPlugin(Class<? extends Plugin> pluginClazz) {
        for (PluginWrapper p : getPlugins()) {
            if (pluginClazz.isInstance(p.getPlugin()))
                return p;
        }
        return null;
    }

    public List<PluginWrapper> getPlugins(Class<? extends Plugin> pluginSuperclass) {
        List<PluginWrapper> result = new ArrayList<PluginWrapper>();
        for (PluginWrapper p : getPlugins()) {
            if (pluginSuperclass.isInstance(p.getPlugin()))
                result.add(p);
        }
        return Collections.unmodifiableList(result);
    }

    public String getDisplayName() {
        return Messages.PluginManager_DisplayName();
    }

    public String getSearchUrl() {
        return "pluginManager";
    }

    @Deprecated
    public <T> Collection<Class<? extends T>> discover(Class<T> spi) {
        Set<Class<? extends T>> result = new HashSet<Class<? extends T>>();
        for (PluginWrapper p : activePlugins) {
            Service.load(spi, p.classLoader, result);
        }
        return result;
    }

    public PluginWrapper whichPlugin(Class c) {
        PluginWrapper oneAndOnly = null;
        ClassLoader cl = c.getClassLoader();
        for (PluginWrapper p : activePlugins) {
            if (p.classLoader == cl) {
                if (oneAndOnly != null)
                    return null;
                oneAndOnly = p;
            }
        }
        return oneAndOnly;
    }

    public void stop() {
        for (PluginWrapper p : activePlugins) {
            p.stop();
            p.releaseClassLoader();
        }
        activePlugins.clear();
        LogFactory.release(uberClassLoader);
    }

    @Restricted(DoNotUse.class)
    public HttpResponse doPlugins() {
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
        JSONArray response = new JSONArray();
        Map<String, JSONObject> allPlugins = new HashMap<>();
        for (PluginWrapper plugin : plugins) {
            JSONObject pluginInfo = new JSONObject();
            pluginInfo.put("installed", true);
            pluginInfo.put("name", plugin.getShortName());
            pluginInfo.put("title", plugin.getDisplayName());
            pluginInfo.put("active", plugin.isActive());
            pluginInfo.put("enabled", plugin.isEnabled());
            pluginInfo.put("bundled", plugin.isBundled);
            pluginInfo.put("deleted", plugin.isDeleted());
            pluginInfo.put("downgradable", plugin.isDowngradable());
            pluginInfo.put("website", plugin.getUrl());
            List<Dependency> dependencies = plugin.getDependencies();
            if (dependencies != null && !dependencies.isEmpty()) {
                Map<String, String> dependencyMap = new HashMap<>();
                for (Dependency dependency : dependencies) {
                    dependencyMap.put(dependency.shortName, dependency.version);
                }
                pluginInfo.put("dependencies", dependencyMap);
            } else {
                pluginInfo.put("dependencies", Collections.emptyMap());
            }
            response.add(pluginInfo);
        }
        for (UpdateSite site : Jenkins.getActiveInstance().getUpdateCenter().getSiteList()) {
            for (UpdateSite.Plugin plugin : site.getAvailables()) {
                JSONObject pluginInfo = allPlugins.get(plugin.name);
                if (pluginInfo == null) {
                    pluginInfo = new JSONObject();
                    pluginInfo.put("installed", false);
                }
                pluginInfo.put("name", plugin.name);
                pluginInfo.put("title", plugin.getDisplayName());
                pluginInfo.put("excerpt", plugin.excerpt);
                pluginInfo.put("site", site.getId());
                pluginInfo.put("dependencies", plugin.dependencies);
                pluginInfo.put("website", plugin.wiki);
                response.add(pluginInfo);
            }
        }
        return hudson.util.HttpResponses.okJSON(response);
    }

    @RequirePOST
    public HttpResponse doUpdateSources(StaplerRequest req) throws IOException {
        Jenkins.getInstance().checkPermission(CONFIGURE_UPDATECENTER);
        if (req.hasParameter("remove")) {
            UpdateCenter uc = Jenkins.getInstance().getUpdateCenter();
            BulkChange bc = new BulkChange(uc);
            try {
                for (String id : req.getParameterValues("sources")) uc.getSites().remove(uc.getById(id));
            } finally {
                bc.commit();
            }
        } else if (req.hasParameter("add"))
            return new HttpRedirect("addSite");
        return new HttpRedirect("./sites");
    }

    @RequirePOST
    @Restricted(DoNotUse.class)
    public void doInstallPluginsDone() {
        Jenkins j = Jenkins.getInstance();
        j.checkPermission(Jenkins.ADMINISTER);
        InstallUtil.proceedToNextStateFrom(InstallState.INITIAL_PLUGINS_INSTALLING);
    }

    @RequirePOST
    public void doInstall(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
        Set<String> plugins = new LinkedHashSet<>();
        Enumeration<String> en = req.getParameterNames();
        while (en.hasMoreElements()) {
            String n = en.nextElement();
            if (n.startsWith("plugin.")) {
                n = n.substring(7);
                plugins.add(n);
            }
        }
        boolean dynamicLoad = req.getParameter("dynamicLoad") != null;
        install(plugins, dynamicLoad);
        rsp.sendRedirect("../updateCenter/");
    }

    @RequirePOST
    @Restricted(DoNotUse.class)
    public HttpResponse doInstallPlugins(StaplerRequest req) throws IOException {
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
        String payload = IOUtils.toString(req.getInputStream(), req.getCharacterEncoding());
        JSONObject request = JSONObject.fromObject(payload);
        JSONArray pluginListJSON = request.getJSONArray("plugins");
        List<String> plugins = new ArrayList<>();
        for (int i = 0; i < pluginListJSON.size(); i++) {
            plugins.add(pluginListJSON.getString(i));
        }
        UUID correlationId = UUID.randomUUID();
        try {
            boolean dynamicLoad = request.getBoolean("dynamicLoad");
            install(plugins, dynamicLoad, correlationId);
            JSONObject responseData = new JSONObject();
            responseData.put("correlationId", correlationId.toString());
            return hudson.util.HttpResponses.okJSON(responseData);
        } catch (Exception e) {
            return hudson.util.HttpResponses.errorJSON(e.getMessage());
        }
    }

    @Restricted(NoExternalUse.class)
    public List<Future<UpdateCenter.UpdateCenterJob>> install(@Nonnull Collection<String> plugins, boolean dynamicLoad) {
        return install(plugins, dynamicLoad, null);
    }

    private List<Future<UpdateCenter.UpdateCenterJob>> install(@Nonnull Collection<String> plugins, boolean dynamicLoad, @CheckForNull UUID correlationId) {
        List<Future<UpdateCenter.UpdateCenterJob>> installJobs = new ArrayList<>();
        for (String n : plugins) {
            int index = n.indexOf('.');
            UpdateSite.Plugin p = null;
            if (index == -1) {
                p = getPlugin(n, UpdateCenter.ID_DEFAULT);
            } else {
                while (index != -1) {
                    if (index + 1 >= n.length()) {
                        break;
                    }
                    String pluginName = n.substring(0, index);
                    String siteName = n.substring(index + 1);
                    UpdateSite.Plugin plugin = getPlugin(pluginName, siteName);
                    if (plugin != null) {
                        if (p != null) {
                            throw new Failure("Ambiguous plugin: " + n);
                        }
                        p = plugin;
                    }
                    index = n.indexOf('.', index + 1);
                }
            }
            if (p == null) {
                throw new Failure("No such plugin: " + n);
            }
            Future<UpdateCenter.UpdateCenterJob> jobFuture = p.deploy(dynamicLoad, correlationId);
            installJobs.add(jobFuture);
        }
        trackInitialPluginInstall(installJobs);
        return installJobs;
    }

    private void trackInitialPluginInstall(@Nonnull final List<Future<UpdateCenter.UpdateCenterJob>> installJobs) {
        final Jenkins jenkins = Jenkins.getInstance();
        final UpdateCenter updateCenter = jenkins.getUpdateCenter();
        final Authentication currentAuth = Jenkins.getAuthentication();
        if (!Jenkins.getInstance().getInstallState().isSetupComplete()) {
            jenkins.setInstallState(InstallState.INITIAL_PLUGINS_INSTALLING);
            updateCenter.persistInstallStatus();
            new Thread() {

                @Override
                public void run() {
                    boolean failures = false;
                    INSTALLING: while (true) {
                        try {
                            updateCenter.persistInstallStatus();
                            Thread.sleep(500);
                            failures = false;
                            for (Future<UpdateCenter.UpdateCenterJob> jobFuture : installJobs) {
                                if (!jobFuture.isDone() && !jobFuture.isCancelled()) {
                                    continue INSTALLING;
                                }
                                UpdateCenter.UpdateCenterJob job = jobFuture.get();
                                if (job instanceof InstallationJob && ((InstallationJob) job).status instanceof DownloadJob.Failure) {
                                    failures = true;
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.log(WARNING, "Unexpected error while waiting for initial plugin set to install.", e);
                        }
                        break;
                    }
                    updateCenter.persistInstallStatus();
                    if (!failures) {
                        try (ACLContext acl = ACL.as(currentAuth)) {
                            InstallUtil.proceedToNextStateFrom(InstallState.INITIAL_PLUGINS_INSTALLING);
                        }
                    }
                }
            }.start();
        }
        new Thread() {

            @Override
            public void run() {
                INSTALLING: while (true) {
                    for (Future<UpdateCenter.UpdateCenterJob> deployJob : installJobs) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            LOGGER.log(SEVERE, "Unexpected error while waiting for some plugins to install. Plugin Manager state may be invalid. Please restart Jenkins ASAP.", e);
                        }
                        if (!deployJob.isCancelled() && !deployJob.isDone()) {
                            continue INSTALLING;
                        }
                    }
                    resolveDependantPlugins();
                    break;
                }
            }
        }.start();
    }

    private UpdateSite.Plugin getPlugin(String pluginName, String siteName) {
        UpdateSite updateSite = Jenkins.getInstance().getUpdateCenter().getById(siteName);
        if (updateSite == null) {
            throw new Failure("No such update center: " + siteName);
        }
        return updateSite.getPlugin(pluginName);
    }

    @RequirePOST
    public HttpResponse doSiteConfigure(@QueryParameter String site) throws IOException {
        Jenkins hudson = Jenkins.getInstance();
        hudson.checkPermission(CONFIGURE_UPDATECENTER);
        UpdateCenter uc = hudson.getUpdateCenter();
        PersistedList<UpdateSite> sites = uc.getSites();
        for (UpdateSite s : sites) {
            if (s.getId().equals(UpdateCenter.ID_DEFAULT))
                sites.remove(s);
        }
        sites.add(new UpdateSite(UpdateCenter.ID_DEFAULT, site));
        return new HttpRedirect("advanced");
    }

    @RequirePOST
    public HttpResponse doProxyConfigure(StaplerRequest req) throws IOException, ServletException {
        Jenkins jenkins = Jenkins.getInstance();
        jenkins.checkPermission(CONFIGURE_UPDATECENTER);
        ProxyConfiguration pc = req.bindJSON(ProxyConfiguration.class, req.getSubmittedForm());
        if (pc.name == null) {
            jenkins.proxy = null;
            ProxyConfiguration.getXmlFile().delete();
        } else {
            jenkins.proxy = pc;
            jenkins.proxy.save();
        }
        return new HttpRedirect("advanced");
    }

    @RequirePOST
    public HttpResponse doUploadPlugin(StaplerRequest req) throws IOException, ServletException {
        try {
            Jenkins.getInstance().checkPermission(UPLOAD_PLUGINS);
            ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
            FileItem fileItem = upload.parseRequest(req).get(0);
            String fileName = Util.getFileName(fileItem.getName());
            if ("".equals(fileName)) {
                return new HttpRedirect("advanced");
            }
            if (!fileName.endsWith(".jpi") && !fileName.endsWith(".hpi")) {
                throw new Failure(hudson.model.Messages.Hudson_NotAPlugin(fileName));
            }
            File t = File.createTempFile("uploaded", ".jpi");
            t.deleteOnExit();
            try {
                fileItem.write(t);
            } catch (Exception e) {
                throw new ServletException(e);
            }
            fileItem.delete();
            final String baseName = identifyPluginShortName(t);
            pluginUploaded = true;
            JSONArray dependencies = new JSONArray();
            try {
                Manifest m = new JarFile(t).getManifest();
                String deps = m.getMainAttributes().getValue("Plugin-Dependencies");
                if (StringUtils.isNotBlank(deps)) {
                    String[] plugins = deps.split(",");
                    for (String p : plugins) {
                        String[] attrs = p.split("[:;]");
                        dependencies.add(new JSONObject().element("name", attrs[0]).element("version", attrs[1]).element("optional", p.contains("resolution:=optional")));
                    }
                }
            } catch (IOException e) {
                LOGGER.log(WARNING, "Unable to setup dependency list for plugin upload", e);
            }
            JSONObject cfg = new JSONObject().element("name", baseName).element("version", "0").element("url", t.toURI().toString()).element("dependencies", dependencies);
            new UpdateSite(UpdateCenter.ID_UPLOAD, null).new Plugin(UpdateCenter.ID_UPLOAD, cfg).deploy(true);
            return new HttpRedirect("../updateCenter");
        } catch (FileUploadException e) {
            throw new ServletException(e);
        }
    }

    @Restricted(NoExternalUse.class)
    @RequirePOST
    public HttpResponse doCheckUpdatesServer() throws IOException {
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
        Retrier<FormValidation> updateServerRetrier = new Retrier.Builder<>(this::checkUpdatesServer, (currentAttempt, result) -> result.kind == FormValidation.Kind.OK, "check updates server").withAttempts(CHECK_UPDATE_ATTEMPTS).withDelay(CHECK_UPDATE_SLEEP_TIME_MILLIS).withDuringActionExceptions(new Class[] { Exception.class }).withDuringActionExceptionListener((attempt, e) -> FormValidation.errorWithMarkup(e.getClass().getSimpleName() + ": " + e.getLocalizedMessage())).build();
        try {
            FormValidation result = updateServerRetrier.start();
            if (!FormValidation.Kind.OK.equals(result.kind)) {
                LOGGER.log(Level.SEVERE, Messages.PluginManager_UpdateSiteError(CHECK_UPDATE_ATTEMPTS, result.getMessage()));
                if (CHECK_UPDATE_ATTEMPTS > 1 && !Logger.getLogger(Retrier.class.getName()).isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.SEVERE, Messages.PluginManager_UpdateSiteChangeLogLevel(Retrier.class.getName()));
                }
                lastErrorCheckUpdateCenters = Messages.PluginManager_CheckUpdateServerError(result.getMessage());
            } else {
                lastErrorCheckUpdateCenters = null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, Messages.PluginManager_UnexpectedException(), e);
            throw new IOException(e);
            }
            return HttpResponses.forwardToPreviousPage();
    }

    private FormValidation checkUpdatesServer() throws Exception {
        for (UpdateSite site : Jenkins.get().getUpdateCenter().getSites()) {
            FormValidation v = site.updateDirectlyNow(DownloadService.signatureCheck);
            if (v.kind != FormValidation.Kind.OK) {
                return v;
            }
        }
        for (DownloadService.Downloadable d : DownloadService.Downloadable.all()) {
            FormValidation v = d.updateNow();
            if (v.kind != FormValidation.Kind.OK) {
                return v;
            }
        }
        return FormValidation.ok();
    }

    public String getLastErrorCheckUpdateCenters() {
        return lastErrorCheckUpdateCenters;
    }

    protected String identifyPluginShortName(File t) {
        try {
            JarFile j = new JarFile(t);
            try {
                String name = j.getManifest().getMainAttributes().getValue("Short-Name");
                if (name != null)
                    return name;
            } finally {
                j.close();
            }
        } catch (IOException e) {
            LOGGER.log(WARNING, "Failed to identify the short name from " + t, e);
        }
        return FilenameUtils.getBaseName(t.getName());
    }

    public Descriptor<ProxyConfiguration> getProxyDescriptor() {
        return Jenkins.getInstance().getDescriptor(ProxyConfiguration.class);
    }

    public List<Future<UpdateCenter.UpdateCenterJob>> prevalidateConfig(InputStream configXml) throws IOException {
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
        List<Future<UpdateCenter.UpdateCenterJob>> jobs = new ArrayList<Future<UpdateCenter.UpdateCenterJob>>();
        UpdateCenter uc = Jenkins.getInstance().getUpdateCenter();
        for (Map.Entry<String, VersionNumber> requestedPlugin : parseRequestedPlugins(configXml).entrySet()) {
            PluginWrapper pw = getPlugin(requestedPlugin.getKey());
            if (pw == null) {
                UpdateSite.Plugin toInstall = uc.getPlugin(requestedPlugin.getKey(), requestedPlugin.getValue());
                if (toInstall == null) {
                    LOGGER.log(WARNING, "No such plugin {0} to install", requestedPlugin.getKey());
                    continue;
                }
                if (new VersionNumber(toInstall.version).compareTo(requestedPlugin.getValue()) < 0) {
                    LOGGER.log(WARNING, "{0} can only be satisfied in @{1}", new Object[] { requestedPlugin, toInstall.version });
                }
                if (toInstall.isForNewerHudson()) {
                    LOGGER.log(WARNING, "{0}@{1} was built for a newer Jenkins", new Object[] { toInstall.name, toInstall.version });
                }
                if (toInstall.isForNewerJava()) {
                    LOGGER.log(WARNING, "{0}@{1} was built for a newer Java", new Object[] { toInstall.name, toInstall.version });
                }
                jobs.add(toInstall.deploy(true));
            } else if (pw.isOlderThan(requestedPlugin.getValue())) {
                UpdateSite.Plugin toInstall = uc.getPlugin(requestedPlugin.getKey(), requestedPlugin.getValue());
                if (toInstall == null) {
                    LOGGER.log(WARNING, "No such plugin {0} to upgrade", requestedPlugin.getKey());
                    continue;
                }
                if (!pw.isOlderThan(new VersionNumber(toInstall.version))) {
                    LOGGER.log(WARNING, "{0}@{1} is no newer than what we already have", new Object[] { toInstall.name, toInstall.version });
                    continue;
                }
                if (new VersionNumber(toInstall.version).compareTo(requestedPlugin.getValue()) < 0) {
                    LOGGER.log(WARNING, "{0} can only be satisfied in @{1}", new Object[] { requestedPlugin, toInstall.version });
                }
                if (toInstall.isForNewerHudson()) {
                    LOGGER.log(WARNING, "{0}@{1} was built for a newer Jenkins", new Object[] { toInstall.name, toInstall.version });
                }
                if (toInstall.isForNewerJava()) {
                    LOGGER.log(WARNING, "{0}@{1} was built for a newer Java", new Object[] { toInstall.name, toInstall.version });
                }
                if (!toInstall.isCompatibleWithInstalledVersion()) {
                    LOGGER.log(WARNING, "{0}@{1} is incompatible with the installed @{2}", new Object[] { toInstall.name, toInstall.version, pw.getVersion() });
                }
                jobs.add(toInstall.deploy(true));
            }
        }
        return jobs;
    }

    @RequirePOST
    public JSONArray doPrevalidateConfig(StaplerRequest req) throws IOException {
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
        JSONArray response = new JSONArray();
        for (Map.Entry<String, VersionNumber> p : parseRequestedPlugins(req.getInputStream()).entrySet()) {
            PluginWrapper pw = getPlugin(p.getKey());
            JSONObject j = new JSONObject().accumulate("name", p.getKey()).accumulate("version", p.getValue().toString());
            if (pw == null) {
                response.add(j.accumulate("mode", "missing"));
            } else if (pw.isOlderThan(p.getValue())) {
                response.add(j.accumulate("mode", "old"));
            }
        }
        return response;
    }

    @RequirePOST
    public HttpResponse doInstallNecessaryPlugins(StaplerRequest req) throws IOException {
        prevalidateConfig(req.getInputStream());
        return HttpResponses.redirectViaContextPath("updateCenter");
    }

    public Map<String, VersionNumber> parseRequestedPlugins(InputStream configXml) throws IOException {
        final Map<String, VersionNumber> requestedPlugins = new TreeMap<String, VersionNumber>();
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(configXml, new DefaultHandler() {

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    String plugin = attributes.getValue("plugin");
                    if (plugin == null) {
                        return;
                    }
                    if (!plugin.matches("[^@]+@[^@]+")) {
                        throw new SAXException("Malformed plugin attribute: " + plugin);
                    }
                    int at = plugin.indexOf('@');
                    String shortName = plugin.substring(0, at);
                    VersionNumber existing = requestedPlugins.get(shortName);
                    VersionNumber requested = new VersionNumber(plugin.substring(at + 1));
                    if (existing == null || existing.compareTo(requested) < 0) {
                        requestedPlugins.put(shortName, requested);
                    }
                }

                @Override
                public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
                    return RestrictiveEntityResolver.INSTANCE.resolveEntity(publicId, systemId);
                }
            });
        } catch (SAXException x) {
            throw new IOException("Failed to parse XML", x);
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
        return requestedPlugins;
    }

    @Restricted(DoNotUse.class)
    public MetadataCache createCache() {
        return new MetadataCache();
    }

    @NonNull
    public List<PluginWrapper.PluginDisableResult> disablePlugins(@NonNull PluginWrapper.PluginDisableStrategy strategy, @NonNull List<String> plugins) throws IOException {
        List<PluginWrapper.PluginDisableResult> results = new ArrayList<>(plugins.size());
        for (String pluginName : plugins) {
            PluginWrapper plugin = this.getPlugin(pluginName);
            if (plugin == null) {
                results.add(new PluginWrapper.PluginDisableResult(pluginName, PluginWrapper.PluginDisableStatus.NO_SUCH_PLUGIN, Messages.PluginWrapper_NoSuchPlugin(pluginName)));
            } else {
                results.add(plugin.disable(strategy));
            }
        }
        return results;
    }

    @Restricted(NoExternalUse.class)
    public static final class MetadataCache {

        private final Map<String, Object> data = new HashMap<>();

        public <T> T of(String key, Class<T> type, Supplier<T> func) {
            return type.cast(data.computeIfAbsent(key, _ignored -> func.get()));
        }
    }

    public final class UberClassLoader extends ClassLoader {

        private ConcurrentMap<String, WeakReference<Class>> generatedClasses = new ConcurrentHashMap<String, WeakReference<Class>>();

        private final Map<String, Class<?>> loaded = new HashMap<String, Class<?>>();

        public UberClassLoader() {
            super(PluginManager.class.getClassLoader());
        }

        public void addNamedClass(String className, Class c) {
            generatedClasses.put(className, new WeakReference<Class>(c));
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            WeakReference<Class> wc = generatedClasses.get(name);
            if (wc != null) {
                Class c = wc.get();
                if (c != null)
                    return c;
                else
                    generatedClasses.remove(name, wc);
            }
            if (name.startsWith("SimpleTemplateScript")) {
                throw new ClassNotFoundException("ignoring " + name);
            }
            synchronized (loaded) {
                if (loaded.containsKey(name)) {
                    Class<?> c = loaded.get(name);
                    if (c != null) {
                        return c;
                    } else {
                        throw new ClassNotFoundException("cached miss for " + name);
                    }
                }
            }
            if (FAST_LOOKUP) {
                for (PluginWrapper p : activePlugins) {
                    try {
                        Class<?> c = ClassLoaderReflectionToolkit._findLoadedClass(p.classLoader, name);
                        if (c != null) {
                            synchronized (loaded) {
                                loaded.put(name, c);
                            }
                            return c;
                        }
                        c = ClassLoaderReflectionToolkit._findClass(p.classLoader, name);
                        synchronized (loaded) {
                            loaded.put(name, c);
                        }
                        return c;
                    } catch (ClassNotFoundException e) {
                    }
                }
            } else {
                for (PluginWrapper p : activePlugins) {
                    try {
                        return p.classLoader.loadClass(name);
                    } catch (ClassNotFoundException e) {
                    }
                }
            }
            synchronized (loaded) {
                loaded.put(name, null);
            }
            throw new ClassNotFoundException(name);
        }

        @Override
        protected URL findResource(String name) {
            if (FAST_LOOKUP) {
                for (PluginWrapper p : activePlugins) {
                    URL url = ClassLoaderReflectionToolkit._findResource(p.classLoader, name);
                    if (url != null)
                        return url;
                }
            } else {
                for (PluginWrapper p : activePlugins) {
                    URL url = p.classLoader.getResource(name);
                    if (url != null)
                        return url;
                }
            }
            return null;
        }

        @Override
        protected Enumeration<URL> findResources(String name) throws IOException {
            List<URL> resources = new ArrayList<URL>();
            if (FAST_LOOKUP) {
                for (PluginWrapper p : activePlugins) {
                    resources.addAll(Collections.list(ClassLoaderReflectionToolkit._findResources(p.classLoader, name)));
                }
            } else {
                for (PluginWrapper p : activePlugins) {
                    resources.addAll(Collections.list(p.classLoader.getResources(name)));
                }
            }
            return Collections.enumeration(resources);
        }

        @Override
        public String toString() {
            return "classLoader " + getClass().getName();
        }
    }

    public static boolean FAST_LOOKUP = !SystemProperties.getBoolean(PluginManager.class.getName() + ".noFastLookup");

    public static final Permission UPLOAD_PLUGINS = new Permission(Jenkins.PERMISSIONS, "UploadPlugins", Messages._PluginManager_UploadPluginsPermission_Description(), Jenkins.ADMINISTER, PermissionScope.JENKINS);

    public static final Permission CONFIGURE_UPDATECENTER = new Permission(Jenkins.PERMISSIONS, "ConfigureUpdateCenter", Messages._PluginManager_ConfigureUpdateCenterPermission_Description(), Jenkins.ADMINISTER, PermissionScope.JENKINS);

    public static final class FailedPlugin {

        public final String name;

        public final Exception cause;

        public FailedPlugin(String name, Exception cause) {
            this.name = name;
            this.cause = cause;
        }

        public String getExceptionString() {
            return Functions.printThrowable(cause);
        }
    }

    static final class PluginInstanceStore {

        final Map<PluginWrapper, Plugin> store = new ConcurrentHashMap<PluginWrapper, Plugin>();
    }

    @Extension
    @Symbol("pluginCycleDependencies")
    public static final class PluginCycleDependenciesMonitor extends AdministrativeMonitor {

        @Override
        public String getDisplayName() {
            return Messages.PluginManager_PluginCycleDependenciesMonitor_DisplayName();
        }

        private transient volatile boolean isActive = false;

        private transient volatile List<PluginWrapper> pluginsWithCycle;

        public boolean isActivated() {
            if (pluginsWithCycle == null) {
                pluginsWithCycle = new ArrayList<>();
                for (PluginWrapper p : Jenkins.getInstance().getPluginManager().getPlugins()) {
                    if (p.hasCycleDependency()) {
                        pluginsWithCycle.add(p);
                        isActive = true;
                    }
                }
            }
            return isActive;
        }

        public List<PluginWrapper> getPluginsWithCycle() {
            return pluginsWithCycle;
        }
    }

    @Extension
    @Symbol("pluginUpdate")
    public static final class PluginUpdateMonitor extends AdministrativeMonitor {

        private Map<String, PluginUpdateInfo> pluginsToBeUpdated = new HashMap<String, PluginManager.PluginUpdateMonitor.PluginUpdateInfo>();

        public static PluginUpdateMonitor getInstance() {
            return ExtensionList.lookupSingleton(PluginUpdateMonitor.class);
        }

        public void ifPluginOlderThenReport(String pluginName, String requiredVersion, String message) {
            Plugin plugin = Jenkins.getInstance().getPlugin(pluginName);
            if (plugin != null) {
                if (plugin.getWrapper().getVersionNumber().isOlderThan(new VersionNumber(requiredVersion))) {
                    pluginsToBeUpdated.put(pluginName, new PluginUpdateInfo(pluginName, message));
                }
            }
        }

        public boolean isActivated() {
            return !pluginsToBeUpdated.isEmpty();
        }

        @Override
        public String getDisplayName() {
            return Messages.PluginManager_PluginUpdateMonitor_DisplayName();
        }

        public void addPluginToUpdate(String pluginName, String message) {
            this.pluginsToBeUpdated.put(pluginName, new PluginUpdateInfo(pluginName, message));
        }

        public Collection<PluginUpdateInfo> getPluginsToBeUpdated() {
            return pluginsToBeUpdated.values();
        }

        public static class PluginUpdateInfo {

            public final String pluginName;

            public final String message;

            private PluginUpdateInfo(String pluginName, String message) {
                this.pluginName = pluginName;
                this.message = message;
            }
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
    public static boolean SKIP_PERMISSION_CHECK = Boolean.getBoolean(PluginManager.class.getName() + ".skipPermissionCheck");
}